package ix.ginas.initializers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ix.core.initializers.Initializer;
import ix.core.initializers.ScheduledTaskInitializer;
import ix.core.models.BaseModel;
import ix.core.models.Keyword;
import ix.core.plugins.SchedulerPlugin;
import ix.core.util.EntityUtils;
import ix.core.util.TimeUtil;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.models.v1.Substance;

/**
 *
 * @author Mitch Miller
 */
public class UnusedRefReportTaskInitializer extends ScheduledTaskInitializer
{

    public static enum ACTION{
        FIX(true,true),
        REPORT(false,true),
        DO_NOTHING(false,false);
        boolean shouldReport;
        boolean shouldFix;
        ACTION(boolean fix, boolean report){
            this.shouldFix=fix;
            this.shouldReport=report;
        }
    }

    private ACTION action = ACTION.REPORT;
    private String path;
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HHmmss");

    @Override
    public void run(SchedulerPlugin.TaskListener l)
    {


        AtomicInteger faultyCount = new AtomicInteger(0);

        l.message("Initializing reference analysis");
        ProcessListener listen = ProcessListener.onCountChange((sofar, total) ->
        {
            if (total != null)
            {
                l.message("Examined:" + sofar + " of " + total + " (" + faultyCount.get() + " problems found)");
            } else
            {
                l.message("Examined:" + sofar + " (" + faultyCount.get() + " problems found)");
            }
        });

        try(PrintWriter printOut = new PrintWriter(new FileOutputStream(getWriteFile()))){


            printOut.println("SUBSTANCE UUID"+ "\tApproval ID" + "\tName" + "\tData Class" + "\tData UUID" + "\tMissing References");

            new ProcessExecutionService(5, 10).buildProcess(Substance.class)
                    .streamSupplier(ProcessExecutionService.CommonStreamSuppliers.allFor(Substance.class))
                    .consumer((Substance s) ->{
                        Set<UUID> refs = s.references.stream()
                                .map(r->r.getUuid())
                                //.peek(u->System.out.println("adding ref with UUID " + u))
                                .collect(Collectors.toSet());
                        List<BaseModel> mods = new ArrayList<BaseModel>();

                        s.getAllChildrenCapableOfHavingReferences()
                                .stream()
                                .filter(d->!refs.containsAll(d.getReferencesAsUUIDs())
                                )
                                .forEach(r->{
                                    Set<Keyword> removeRefs= r.getReferences().stream()
                                            .filter(k->!refs.contains(UUID.fromString(k.getValue())))
                                            .collect(Collectors.toSet());
                                    String dataID=null;
                                    if(r instanceof BaseModel){
                                        mods.add((BaseModel)r);
                                        dataID=((BaseModel)r).fetchGlobalId();
                                    }
                                    if(action.shouldReport){
                                        String badLine = s.getOrGenerateUUID() + "\t" + s.getApprovalID() + "\t" + s.getName() + "\t" + r.getClass().getName() + "\t" +dataID + "\t" + removeRefs.stream().map(v->v.getValue()).collect(Collectors.joining(","));
                                        printOut.println(badLine);
                                    }

                                    Set<Keyword> keepRefs= r.getReferences().stream()
                                            .filter(k->refs.contains(UUID.fromString(k.getValue())))
                                            .collect(Collectors.toSet());
                                    r.setReferences(keepRefs);

                                });


                        if(!mods.isEmpty()){
                            faultyCount.incrementAndGet();
                            if(action.shouldFix){
                                for(BaseModel m:mods){
                                    m.save();
                                }
                            }
                        }

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
    public File getWriteFile() {
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
        if(this.action==ACTION.REPORT){
            return "Produce a list of unused references";
        }else if(this.action==ACTION.FIX){
            return "Fix records with unused references";
        }else{
            return "Iterate through records looking for unused references";
        }
    }

    @Override
    public Initializer initializeWith(Map<String, ?> m)
    {
        super.initializeWith(m);
        String act=(String) m.get("action");
        try{
            this.action=ACTION.valueOf(act.toUpperCase());

        }catch(Exception e){
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Action: \"" + act + "\" not a known action, defaulting to " + "\"" + this.action.toString() + "\"");
        }
        path = (String) m.get("output.path");

        if (path == null) {
            path = "reports/" + "orphanReferences" + "-%DATE%-%TIME%.txt";
        }

        return this;
    }

}