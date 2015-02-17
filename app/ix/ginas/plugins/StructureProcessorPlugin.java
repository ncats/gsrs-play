package ix.ginas.plugins;

import java.util.*;
import java.util.concurrent.TimeoutException;
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
import play.db.ebean.Transactional;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Inbox;
import akka.actor.Terminated;
import akka.routing.Broadcast;
import akka.routing.RouterConfig;
import akka.routing.FromConfig;
import akka.routing.RoundRobinRouter;
import akka.routing.SmallestMailboxRouter;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.collection.immutable.Iterable;
import scala.collection.JavaConverters;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import ix.core.plugins.IxContext;
import ix.core.models.XRef;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.ProcessingJobFactory;
import ix.ginas.chem.*;
import ix.ginas.models.*;
import ix.utils.Util;

public class StructureProcessorPlugin extends Plugin {
    final static Molecule DONE = new Molecule();
    
    private final Application app;
    private IxContext ctx;
    private ActorSystem system;
    private ActorRef processor;
    private Inbox inbox;
    static final Random rand = new Random ();

    public static class PayloadProcessor implements Serializable {
        public final Payload payload;
        public final String id;
        public final String key;
        
        public PayloadProcessor (Payload payload) {
            this.payload = payload;
            this.key = randomKey (10);
            this.id = payload.id + ":" +this.key;
        }

        static String randomKey (int size) {
            byte[] b = new byte[size];
            rand.nextBytes(b);
            return Util.toHex(b);
        }
    }

    public static class PayloadRecord implements Serializable {
        public final ProcessingJob job;
        public final Molecule mol;

        public PayloadRecord (ProcessingJob job, Molecule mol) {
            this.job = job;
            this.mol = mol;
        }
    }

    public static class PayloadProcessed implements Serializable {
        public final ProcessingJob job;
        public PayloadProcessed (ProcessingJob job) {
            this.job = job;
        }
    }


    public static class Processor extends UntypedActor {
        LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        @Override
        public void preStart () {
        }
        
        @Override
        public void postStop () {
        }

        @Transactional
        public void onReceive (Object mesg) {
            if (mesg instanceof PayloadProcessor) {
                PayloadProcessor payload = (PayloadProcessor)mesg;
                log.info("Received payload "+payload.id);

                // now spawn child processor to proces the payload stream
                ActorRef child = null;
                Collection<ActorRef> children = JavaConverters
                    .asJavaCollectionConverter(context().children())
                    .asJavaCollection();
                for (ActorRef ref : children) {
                    //ActorRef ref = iter.next();
                    if (ref.path().name()
                        .indexOf(payload.payload.id.toString()) >= 0) {
                        // this child is current running
                        child = ref;
                    }
                }
                
                if (child != null) {
                    // the given payload is currently processing
                    // at the moment!
                    ProcessingJob job = new ProcessingJob (payload.key);
                    job.start = job.stop = System.currentTimeMillis();
                    job.driver = StructureProcessorPlugin.class.getName();
                    job.status = ProcessingJob.Status.NOT_RUN;
                    job.message = "Payload "+payload.payload.id+" is "
                        +"currently being processed.";
                    job.payload = payload.payload;

                    save (job);
                    sender().tell(new PayloadProcessed (job), self ());
                }
                else {
                    child = context().actorOf
                        (Props.create(Processor.class).withRouter
                         (new FromConfig().withFallback
                          (new SmallestMailboxRouter (2))), payload.id);
                    context().watch(child);

                    try {
                        ProcessingJob job = process (child, self (), payload);
                        log.info("Job "+job.id+" submitted!");
                        child.tell(new Broadcast (PoisonPill.getInstance()),
                                   self ());
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        sender().tell(ex, self ());
                    }
                }
            }
            else if (mesg instanceof PayloadRecord) {
                PayloadRecord pr = (PayloadRecord)mesg;
                //log.info("processing "+pr.record.getName());
                ProcessingRecord rec = new ProcessingRecord ();
                rec.name = pr.mol.getName();
                rec.job = pr.job;
                rec.start = System.currentTimeMillis();
                Structure struc = null;
                try {
                    struc = StructureProcessor.instrument(pr.mol);
                    rec.stop = System.currentTimeMillis();
                    rec.status = ProcessingRecord.Status.OK;
                }
                catch (Throwable t) {
                    rec.stop = System.currentTimeMillis();
                    rec.status = ProcessingRecord.Status.FAILED;
                    rec.message = t.getMessage();
                    t.printStackTrace();
                }
                save (struc, rec);
            }
            else if (mesg instanceof Terminated) {
                ActorRef actor = ((Terminated)mesg).actor();
                context().unwatch(actor);
                
                String id = actor.path().name();
                int pos = id.indexOf(':');
                if (pos > 0) {
                    String jid = id.substring(pos+1);
                    try {                   
                        ProcessingJob job = ProcessingJobFactory.getJob(jid);
                        if (job != null) {
                            job.stop = System.currentTimeMillis();
                            job.status = ProcessingJob.Status.COMPLETE;
                            job.update();
                            log.info("done processing job {}!", job.key);
                        }
                        else {
                            log.error("Failed to retrieve job "+jid);
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        log.error("Failed to retrieve job "
                                  +jid+"; "+ex.getMessage());
                    }
                }
                else {
                    log.error("Invalid job id: "+id);
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
        
        public void onReceive (Object mesg) {
            if (mesg instanceof PayloadProcessor) {
                router.tell(mesg, self ());
            }
            else if (mesg instanceof PayloadProcessed) {
                PayloadProcessed pp = (PayloadProcessed)mesg;
                log.info(pp.job.message);
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
    public String submit (Payload payload) {
        // first see if this payload has already processed..
        PayloadProcessor pp = new PayloadProcessor (payload);
        inbox.send(processor, pp);
        return pp.key;
    }

    static ProcessingJob process (ActorRef proc, ActorRef sender,
                                  PayloadProcessor pp) throws Exception {  
        InputStream is = PayloadFactory.getStream(pp.payload);
        ProcessingJob job = new ProcessingJob (pp.key);
        job.start = System.currentTimeMillis();
        job.driver = StructureProcessorPlugin.class.getName();
        job.status = ProcessingJob.Status.RUNNING;
        job.payload = pp.payload;
        try {
            job.save();
            
            MolImporter mi = new MolImporter (is);
            int total = 0;
            for (Molecule m; (m = mi.read()) != null; ++total) {
                proc.tell(new PayloadRecord (job, m), sender);
            }
            mi.close();
        }
        catch (Throwable t) {
            job.message = t.getMessage();
            Logger.trace("Can't processing payload "+pp.payload.id, t);
            job.status = ProcessingJob.Status.FAILED;
            job.save();
        }
        return job;
    }

    @Transactional
    synchronized static void save (Structure struc, ProcessingRecord rec) {
        try {
            if (struc != null) {
                struc.save();
                rec.xref = new XRef (struc);
                rec.xref.save();
            }
            rec.save();
            Logger.debug("Saved struc "+(struc != null ? struc.id:null)
                         +" record "+rec.id);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Transactional
    synchronized static void save (ProcessingJob job) {
        job.save();
    }
}
