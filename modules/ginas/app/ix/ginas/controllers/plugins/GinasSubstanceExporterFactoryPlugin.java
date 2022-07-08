package ix.ginas.controllers.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import ix.core.util.IOUtil;
import ix.core.exporters.OutputFormat;
import ix.core.exporters.SubstanceExporterFactory;
import org.apache.poi.util.DefaultTempFileCreationStrategy;
import org.apache.poi.util.TempFile;
import org.apache.poi.util.TempFileCreationStrategy;
import play.Application;
import play.Logger;
import play.api.Plugin;

/**
 * This Plugin finds all the {@link SubstanceExporterFactory} implementations
 * to use as specified in the config file.
 *
 * Created by katzelda on 8/23/16.
 */
public class GinasSubstanceExporterFactoryPlugin implements Plugin {

    private final Set<SubstanceExporterFactory> exporters = new LinkedHashSet<>();

    private Map<String, OutputFormat> extensionMap = new LinkedHashMap<>();

    private ThreadPoolExecutor executor;

    private final Application app;

    public GinasSubstanceExporterFactoryPlugin(Application app) {
        this.app = app;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public void onStart() {
        exporters.clear();
        extensionMap.clear();

        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

        String tmpDirPath = app.configuration().getString("ix.ginas.export.tmpDir");
        File tmpDir = tmpDirPath ==null ? null : new File(tmpDirPath);

        if(tmpDir !=null){

            if(tmpDir.exists() && ! Files.isWritable(tmpDir.toPath())){
                throw new IllegalStateException("export temp directory is not writable " + tmpDir.getAbsolutePath());
            }
        }
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy(tmpDir));

        for(String clazz :app.configuration().getStringList("ix.ginas.exportFactories",new ArrayList<String>())){
            try{
                Class<SubstanceExporterFactory> exporter = (Class<SubstanceExporterFactory>) IOUtil.getGinasClassLoader().loadClass(clazz);
                exporters.add(exporter.newInstance());
            }catch(Exception e){
                Logger.error("Error initializing exporter:" + clazz);
            	e.printStackTrace();
                //ignore
            }
        }

        for(OutputFormat fmt : getAllSupportedFormats()){
            extensionMap.put(fmt.getExtension(), fmt);
        }
    }

    @Override
    public void onStop() {
        exporters.clear();
        extensionMap.clear();

        executor.shutdownNow();
    }

    /**
     * Get the {@link OutputFormat} associated
     * with the given file extension.
     * @param extension the extension to look for.
     * @return the {@link OutputFormat} for that extension;
     * or {@code null} if no format is mapped.
     */
    public OutputFormat getFormatFor(String extension){
        return extensionMap.get(extension);
    }

    /**
     * Get the {@link SubstanceExporterFactory} for the given {@link ix.ginas.exporters.SubstanceExporterFactory.Parameters}.
     * @param params the parameters to use to find the Exporter.
     * @return a {@link SubstanceExporterFactory} or {@code null} if no exporter is found that supports
     * those parameter configurations.
     *
     * @throws NullPointerException if params is null.
     */
    public SubstanceExporterFactory getExporterFor(SubstanceExporterFactory.Parameters params){
        Objects.requireNonNull(params);

        for(SubstanceExporterFactory factory : exporters){
            if(factory.supports(params)){
                return factory;
            }
        }
        return null;
    }

    /**
     * Get all the Supported {@link OutputFormat}s
     * by this plugin.
     * @return a Set of {@link OutputFormat}s, may be an
     * empty set if no exporters are found.
     */
    public Set<OutputFormat> getAllSupportedFormats(){


        //This mess with reverse iterating and then reversing again
        //is because we want the exporters listed first in the config file
        //to have priority over later listed exporters.  So if an earlier
        //exporter has the same extension as a later exporter, the earlier one should
        //have priority.
        //
        //So we go through the list backwards so earlier exporter's extensions
        //overwrite later exporters
        //
        //But then we have to reverse the list again so
        //the final display order matches the input list order.


        List<OutputFormat> list = getAllOutputsAsList();
        //go in reverse order to prefer the factories listed first
        ListIterator<OutputFormat> iterator = list.listIterator(list.size());
        Set<OutputFormat> set = new LinkedHashSet<>();
        while(iterator.hasPrevious()){
            set.add(iterator.previous());
        }
        //reverse it again
        List<OutputFormat> resortList = new ArrayList<>(set.size());

        for(OutputFormat f : set){
            resortList.add(f);
        }
        Collections.reverse(resortList);


        return new LinkedHashSet<>(resortList);
    }

    private List<OutputFormat> getAllOutputsAsList() {
        List<OutputFormat> list = new ArrayList<>();

        for(SubstanceExporterFactory factory : exporters){
            list.addAll(factory.getSupportedFormats());
        }
        return list;
    }

    public Future<?> submit(Runnable r) {
        return executor.submit(r);
    }

    public boolean isReady() {
        return executor.getActiveCount()<executor.getMaximumPoolSize();
    }



}
