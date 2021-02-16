package ix.ginas.initializers;

import ix.core.initializers.Initializer;
import ix.core.models.BaseModel;
import ix.core.models.Keyword;
import ix.core.plugins.SchedulerPlugin;
import ix.core.util.TimeUtil;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.models.v1.Substance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import ix.core.util.LogUtil;


/**
 *
 * @author Mitch Miller
 */
public class DefinitionalHashKeyReportTask extends ScheduledTaskInitializer {
    private String path;
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HHmmss");

    @Override
    public void run(SchedulerPlugin.TaskListener l)
    {
        LogUtil.trace(()->"Starting in DefinitionalHashKeyReportTask.run");

        l.message("Initializing definitional hash key listing");
        ProcessListener listen = ProcessListener.onCountChange((sofar, total) ->
        {
            LogUtil.trace(()->" onCountChange");
            if (total != null)
            {
                l.message(String.format( "Processed: %d of %d records", sofar, total ));
            } else
            {
                l.message(String.format("Processed: %d records so far", sofar));
            }
        });

        LogUtil.trace(()->" about to init PrintWriter");

        try(PrintWriter printOut = new PrintWriter(new FileOutputStream(getReportFile()))){

            printOut.println("SUBSTANCE UUID\tType\tName\tApproval ID\tDef Hash\tDef Hash Key");
            LogUtil.trace(()->" printed header");

            AtomicInteger count = new AtomicInteger(0);
            new ProcessExecutionService(5, 10).buildProcess(Substance.class)
                    .streamSupplier(ProcessExecutionService.CommonStreamSuppliers.allFor(Substance.class))
                    .consumer((Substance s) ->{
                        //LogUtil.trace(()->" in consumer " +count.incrementAndGet() + " class " + s.substanceClass);
                        String line =String.format("%s\t%s\t%s\t%s\t%s\t%s", s.getUuid().toString(),
                                s.substanceClass,
                                s.getName(), (s.getApprovalID() ==  null? "[no approval id]" :s.getApprovalID()),
                                s.getDefHashString(),
                                s.getDefHashKeyString());
                        LogUtil.trace(()->line);
                        printOut.println(line);

                    })
                    .listener(listen)
                    .build()
                    .execute();
        } catch (IOException ex)
        {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Returns the File used to output the report
     *
     * @return
     */
    public File getReportFile() {
        String date = formatter.format(TimeUtil.getCurrentLocalDateTime());
        String time = formatterTime.format(TimeUtil.getCurrentLocalDateTime());

        String fpath = path.replace("%DATE%", date)
                .replace("%TIME%", time);

        File f=new File(fpath);
        File p=f.getParentFile();
        p.mkdirs();
        return f;
    }
    @Override
    public String getDescription()
    {
        return "Create a report of substances and keys based on their definitional hashes ";
    }

    @Override
    public Initializer initializeWith(Map<String, ?> m)
    {
        super.initializeWith(m);
        path = (String) m.get("output.path");

        if (path == null) {
            path = "reports/" + "substance_def_hash_key_report" + "-%DATE%-%TIME%.txt";
        }
        return this;
    }

}
