package ix.ginas.controllers.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ix.ginas.exporters.SubstanceExporterFactory;
import play.Application;
import play.api.Plugin;

/**
 * This Plugin finds all the {@link SubstanceExporterFactory} implementations
 * to use as specified in the config file.
 *
 * Created by katzelda on 8/23/16.
 */
public class GinasSubstanceExporterFactoryPlugin implements Plugin {

    private final Set<SubstanceExporterFactory> exporters = new LinkedHashSet<>();

    private Map<String, SubstanceExporterFactory.OutputFormat> extensionMap = new LinkedHashMap<>();

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

        for(String clazz :app.configuration().getStringList("ix.ginas.exportFactories")){
            try{
                Class<SubstanceExporterFactory> exporter = (Class<SubstanceExporterFactory>) Class.forName(clazz);
                exporters.add(exporter.newInstance());
            }catch(Exception e){
                e.printStackTrace();
                //ignore
            }
        }

        for(SubstanceExporterFactory.OutputFormat fmt : getAllSupportedFormats()){
            extensionMap.put(fmt.getExtension(), fmt);
        }
    }

    @Override
    public void onStop() {
        exporters.clear();
        extensionMap.clear();
    }

    /**
     * Get the {@link ix.ginas.exporters.SubstanceExporterFactory.OutputFormat} associated
     * with the given file extension.
     * @param extension the extension to look for.
     * @return the {@link ix.ginas.exporters.SubstanceExporterFactory.OutputFormat} for that extension;
     * or {@code null} if no format is mapped.
     */
    public SubstanceExporterFactory.OutputFormat getFormatFor(String extension){
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
     * Get all the Supported {@link ix.ginas.exporters.SubstanceExporterFactory.OutputFormat}s
     * by this plugin.
     * @return a Set of {@link ix.ginas.exporters.SubstanceExporterFactory.OutputFormat}s, may be an
     * empty set if no exporters are found.
     */
    public Set<SubstanceExporterFactory.OutputFormat> getAllSupportedFormats(){


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


        List<SubstanceExporterFactory.OutputFormat> list = getAllOutputsAsList();
        //go in reverse order to prefer the factories listed first
        ListIterator<SubstanceExporterFactory.OutputFormat> iterator = list.listIterator(list.size());
        Set<SubstanceExporterFactory.OutputFormat> set = new LinkedHashSet<>();
        while(iterator.hasPrevious()){
            set.add(iterator.previous());
        }
        //reverse it again
        List<SubstanceExporterFactory.OutputFormat> resortList = new ArrayList<>(set.size());

        for(SubstanceExporterFactory.OutputFormat f : set){
            resortList.add(f);
        }
        Collections.reverse(resortList);


        return new LinkedHashSet<>(resortList);
    }

    private List<SubstanceExporterFactory.OutputFormat> getAllOutputsAsList() {
        List<SubstanceExporterFactory.OutputFormat> list = new ArrayList<>();

        for(SubstanceExporterFactory factory : exporters){
            list.addAll(factory.getSupportedFormats());
        }
        return list;
    }
}
