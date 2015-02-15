package ix.ginas.plugins;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

import play.Logger;
import play.Plugin;
import play.Application;
import play.cache.Cache;
import play.libs.Akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.RouterConfig;
import akka.routing.FromConfig;
import akka.routing.RoundRobinRouter;
import akka.routing.SmallestMailboxRouter;
import akka.actor.Props;
import akka.actor.Inbox;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

import ix.core.plugins.IxContext;
import ix.core.models.Payload;
import ix.core.controllers.PayloadFactory;
import ix.ginas.chem.*;
import ix.ginas.models.*;

public class StructureProcessorPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private ActorSystem system;
    private ActorRef processor;
    private Inbox inbox;

    public static class PayloadProcessor {
        public final UUID id;
        public PayloadProcessor (Payload payload) {
            this.id = payload.id;
        }
        public PayloadProcessor (UUID id) {
            this.id = id;
        }
    }

    public static class PayloadStatus implements Serializable {
        public final Long id;
        public PayloadStatus (Long id) {
            this.id = id;
        }
    }

    public static class Reporter extends UntypedActor {
        LoggingAdapter log = Logging.getLogger(getContext().system(), this);
        
        public void onReceive (Object mesg) {
            if (mesg instanceof PayloadStatus) {
                PayloadStatus status = (PayloadStatus)mesg;
            }
            else {
                unhandled (mesg);
            }
        }
    }

    public static class Processor extends UntypedActor {
        LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        public void onReceive (Object mesg) {
            if (mesg instanceof PayloadProcessor) {
                PayloadProcessor payload = (PayloadProcessor)mesg;
                log.info("Received payload "+payload.id);

                // now spawn child processor to proces the payload stream
                ActorRef children = context().actorOf
                    (Props.create(Processor.class).withRouter
                     (new FromConfig().withFallback
                      (new SmallestMailboxRouter (2))), payload.id.toString());
                try {
                    int count = process (children, payload);
                    log.info("pushed "+count+" molecules out for processing!");
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                //sender().tell(new Status (mesg+" ok"), self ());
            }
            else if (mesg instanceof Molecule) {
                Molecule mol = (Molecule)mesg;
                log.info("processing "+mol.getName());
                Structure struc = StructureProcessor.instrument(mol);
                try {
                    struc.save();
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            else {
                unhandled (mesg);
            }
        }
    }

    public static class ProcessorManager extends UntypedActor {
        LoggingAdapter log = Logging.getLogger(getContext().system(), this);    
        ActorRef router;
        ActorRef reporter;
        
        public void onReceive (Object mesg) {
            if (mesg instanceof PayloadStatus) {
                log.info(sender()+" => "+mesg);
            }
            else if (mesg instanceof PayloadProcessor) {
                router.tell(mesg, self ());
            }
            else {
                unhandled (mesg);
            }
        }

        @Override
        public void preStart () {
            RouterConfig config = new SmallestMailboxRouter (2);
            router = context().actorOf
                (Props.create(Processor.class).withRouter
                 (new FromConfig().withFallback(config)), "router");
            reporter = context().actorOf
                (Props.create(Reporter.class).withRouter
                 (new FromConfig().withFallback(config)), "reporter");
        }

        @Override
        public void postStop () {
            if (router != null)
                context().stop(router);
            if (reporter != null)
                context().stop(reporter);
        }
    }

    public StructureProcessorPlugin (Application app) {
        this.app = app;
    }

    public void onStart () {
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        
        system = ActorSystem.create("StructureProcessor");
        Logger.info("Plugin "+getClass().getName()
                    +" initialized; Akka version "+system.Version());
        processor = system.actorOf
            (Props.create(ProcessorManager.class), "processor");
        inbox = Inbox.create(system);
    }

    public void onStop () {
        if (system != null)
            system.shutdown();
        Logger.info("Plugin "+getClass().getName()+" stopped!");
    }

    public boolean enabled () { return true; }
    public void submit (Payload payload) {
        // first see if this payload has already processed..
        inbox.send(processor, new PayloadProcessor (payload));
    }
    public void submit (UUID payload) {
        inbox.send(processor, new PayloadProcessor (payload));
    }

    static int process (ActorRef proc, PayloadProcessor pp) throws Exception {
        InputStream is = PayloadFactory.getStream(pp.id);
        if (is == null)
            throw new IllegalArgumentException
                ("Unkown payload "+pp.id+" specified!");
        int count = -1; 
        try {
            MolImporter mi = new MolImporter (is);
            count = 0;
            for (Molecule m; (m = mi.read()) != null; ++count) {
                proc.tell(m, proc);
            }
            mi.close();
        }
        catch (Exception ex) {
            Logger.trace("Can't processing payload "+pp.id, ex);
        }
        return count;
    }
}
