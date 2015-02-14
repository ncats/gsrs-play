package ix.ginas.plugins;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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

import ix.core.plugins.IxContext;
import ix.ginas.chem.*;
import ix.ginas.models.*;

public class StructureProcessorPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private ActorSystem system;
    private ActorRef procRef;
    private Inbox inbox;
    
    public static class Payload implements Serializable {
    }

    public static class Status implements Serializable {
        final String status;
        public Status (String status) {
            this.status = status;
        }

        public String status () { return status; }
        public String toString () { return status; }
    }

    public static class Processor extends UntypedActor {
        LoggingAdapter log = Logging.getLogger(getContext().system(), this);
        
        public void onReceive (Object mesg) {
            log.info("processor: "+ mesg+"; now I'm sleeping...");
            
            try {
                Thread.sleep(5000);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            sender().tell(new Status (mesg+" ok"), self ());
        }

        @Override
        public void preStart () {
            //log.info("preStart()");
        }

        @Override
        public void postStop () {
            //log.info("postStop()");
        }
    }

    public static class ProcessorManager extends UntypedActor {
        LoggingAdapter log = Logging.getLogger(getContext().system(), this);    
        ActorRef router;
        
        public void onReceive (Object mesg) {
            if (mesg instanceof Status) {
                log.info(sender()+" => "+mesg);
            }
            else {
                router.tell(mesg, self ());
            }
        }

        @Override
        public void preStart () {
            RouterConfig config = new SmallestMailboxRouter (2);
            router = context().actorOf
                (Props.create(Processor.class).withRouter
                 (new FromConfig ().withFallback(config)), "router");
        }

        @Override
        public void postStop () {
            if (router != null)
                context().stop(router);
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
        procRef = system.actorOf
            (Props.create(ProcessorManager.class), "processor");
        inbox = Inbox.create(system);
    }

    public void onStop () {
        system.stop(procRef);
        system.shutdown();
        Logger.info("Plugin "+getClass().getName()+" stopped!");
    }

    public boolean enabled () { return true; }
    public void process (String mesg) {
        inbox.send(procRef, mesg);
    }
}
