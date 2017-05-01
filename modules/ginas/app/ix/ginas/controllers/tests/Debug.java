package ix.ginas.controllers.tests;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.search.SearchRequest;
import ix.core.models.Principal;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.SearchResult;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.TimeUtil;
import ix.core.util.Unchecked;
import ix.core.utils.executor.MultiProcessListener;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessExecutionService.Before;
import ix.core.utils.executor.ProcessExecutionService.CommonConsumers;
import ix.core.utils.executor.ProcessExecutionService.CommonStreamSuppliers;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.controllers.GinasApp;
import ix.ginas.exporters.ExportMetaData;
import ix.ginas.exporters.ExportProcess;
import ix.ginas.exporters.ExportProcessFactory;
import ix.ginas.models.v1.Substance;
import ix.test.modelsb.Wat;
import play.Application;
import play.Play;
import play.db.DB;

public class Debug {


    public static void onInitialize(Application app){

        
        ScheduledTask.of((l) -> {
            System.out.println("Running export");
            try {
                
                Principal user = new Principal("admin", null);
                String collectionID = "export-all-gsrs";
                String extension = "gsrs";
                boolean publicOnlyBool = false;

                ExportMetaData emd = new ExportMetaData(collectionID, null, user, publicOnlyBool, extension)
                        .onTotalChanged((c)->{
                            l.message("Exported " + c + " records");
                        });
                

                LocalDate ld=TimeUtil.getCurrentLocalDate();
                String date=ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String fname = "auto-export-" + date + ".gsrs";
                
                emd.setDisplayFilename(fname);
                emd.originalQuery = null;
                
                

                ExportProcess p = new ExportProcessFactory().getProcess(emd,
                        ProcessExecutionService.CommonStreamSuppliers.allForDeep(Substance.class));

                p.run(out -> Unchecked.uncheck(() -> GinasApp.getSubstanceExporterFor(extension, out, publicOnlyBool)))
                 .get();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        })
        .dailyAtHourAndMinute(16, 42)
        .description("Full Export")
        .submit();
        
        
        

//        Wat mod= new Wat();
//        mod.id=UUID.randomUUID().toString();
//        mod.t="t2";
//        System.out.println(EntityWrapper.of(mod).getDataSource());
//        mod.save(EntityWrapper.of(mod).getDataSource());
        sillyTest();
        
    }
    
    
    
    public static void sillyTest(){
        String db=EntityUtils.getEntityInfoFor(Wat.class).getDatasource();
        try(Connection c=DB.getDataSource(db).getConnection()){
            
            c.createStatement().execute("CREATE OR REPLACE VIEW ix_some_table2 AS SELECT uuid||'G123' as id, approval_id as t, class as type FROM ix_ginas_substance;");
//            ResultSet rs=c.createStatement().executeQuery("Select * FROM ix_ginas_substance;");
//            StreamUtil.from(rs)
//            .streamWhile(r->Unchecked.uncheck(()->r.next()))
//            .map(r->Unchecked.uncheck(()->r.getString(1)))
//            .forEach(s->System.out.println(s));
//            rs.close();
         
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        List<Wat> subset = EntityUtils.getEntityInfoFor(Wat.class)
                .getFinder()
                .all()
                .stream()
                .map(w->(Wat)w)
                .filter(w->w.type==1)
                .collect(Collectors.toList());
//                .stream()
//                .forEach(t->{
//                    System.out.println(EntityWrapper.of(t).toFullJson());                   
//                });


        try{
            
            SearchRequest request = new SearchRequest.Builder()
//                    .withRequest(req)
                    .top(16)
                    .kind(Wat.class)
                    .subset(subset)
                    .fdim(10)
                    .build();
            
            new ProcessExecutionService(1,1).buildProcess(Wat.class)
                .before(Before.removeIndexFor(Wat.class))
                .build()
                .execute();
            
            System.out.println("First:" + request.execute().getCount());
            
            new ProcessExecutionService(5,10).buildProcess(Wat.class)
//                .streamSupplier(CommonStreamSuppliers.allFrom(Wat.class, f->f.query().where().eq("t", "Y4907O6MFD")))
                .streamSupplier(CommonStreamSuppliers.allFor(Wat.class))
                .consumer(CommonConsumers.REINDEX_COMPLETE)
                .build()
                .execute();
            
            
            SearchResult sr = request.execute();
            System.out.println("Second:" + sr.getCount());
            
            
            sr.getMatches()
                   .forEach(t->{
                       System.out.println(EntityWrapper.of(t).toFullJson());  
                   });
            
            
            sr.getFacets().forEach(f->{
               System.out.println(f.getName());
               System.out.println("==============");
               f.getValues().forEach(fv->{
                   System.out.println(fv.getLabel() + ":" + fv.getCount());
               });
            });


        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public static <T> SearchResult indexedFor(Collection<T> subset) throws IOException{
        return new SearchRequest.Builder()
              .top(16)
              .fdim(10)
              //.withRequest(request())
              .subset(subset)
              .build()
              .execute();
    }
    
    
    
    
}
