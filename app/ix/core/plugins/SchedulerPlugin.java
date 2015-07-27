package ix.core.plugins;

import java.util.*;
import java.sql.*;
import javax.sql.DataSource;

import play.Logger;
import play.Plugin;
import play.Application;
import play.db.DB;
import play.db.ebean.Model;
import play.db.ebean.Transactional;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.DBConnectionManager;
import org.quartz.utils.ConnectionProvider;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

import ix.core.chem.StructureProcessor;

public class SchedulerPlugin extends Plugin {

    static class IxConnectionProvider implements ConnectionProvider {
        final DataSource ds;

        IxConnectionProvider () throws SQLException {
            ds = DB.getDataSource("quartz");
            if (ds == null)
                throw new IllegalArgumentException
                    ("No data source 'quartz' defined!");
        }
        
        public Connection getConnection () throws SQLException {
            return ds.getConnection();
        }
        
        public void initialize () throws SQLException {
        }
        
        public void shutdown () throws SQLException {
        }
    }

    static public class StrucProcJob implements Job {
        public StrucProcJob () {}

        public void execute (JobExecutionContext context)
            throws JobExecutionException {
            Logger.debug(Thread.currentThread()+": Executing trigger {}",
                         context.getJobDetail().getKey());
        }
    }
    
    private final Application app;
    private Scheduler scheduler;

    public SchedulerPlugin (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        try {
            DBConnectionManager dbman = DBConnectionManager.getInstance();
            dbman.addConnectionProvider
                ("QuartzDS", new IxConnectionProvider ());
            
            StdSchedulerFactory factory = new StdSchedulerFactory();
            scheduler = factory.getScheduler();
            scheduler.start();
            
            Logger.info("Plugin "+getClass().getName()+" initialized");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStop () {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Logger.info("Plugin "+getClass().getName()+" stopped!");        
    }

    public Scheduler getScheduler () { return scheduler; }
    public void submit (String payload) {
        JobDetail job = newJob(StrucProcJob.class)
            .withIdentity(payload)
            .storeDurably(true)
            .build();
        Trigger trigger = newTrigger()
            .withIdentity(payload)
            .forJob(job)
            .startNow()
            .build();
        try {
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }
}
