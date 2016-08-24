package ix.ginas.controllers.plugins;

import ix.ginas.exporters.Exporter;
import ix.ginas.exporters.SubstanceExporterFactory;
import ix.ginas.models.v1.Substance;
import play.Application;
import play.Play;
import play.api.Plugin;

import java.util.*;

/**
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

        for(String clazz :app.configuration().getStringList("ginas.exportFactories")){
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


    public SubstanceExporterFactory.OutputFormat getFormatFor(String extension){
        return extensionMap.get(extension);
    }
    public SubstanceExporterFactory getExporterFor(SubstanceExporterFactory.Parameters params){
        for(SubstanceExporterFactory factory : exporters){
            if(factory.supports(params)){
                return factory;
            }
        }
        return null;
    }

    public Set<SubstanceExporterFactory.OutputFormat> getAllSupportedFormats(){
        Set<SubstanceExporterFactory.OutputFormat> set = new LinkedHashSet<>();

       List<SubstanceExporterFactory.OutputFormat> list = new ArrayList<>();

        for(SubstanceExporterFactory factory : exporters){
            list.addAll(factory.getSupportedFormats());
        }
        //go in reverse order to prefer the factories listed first
        ListIterator<SubstanceExporterFactory.OutputFormat> iterator = list.listIterator(list.size());
        while(iterator.hasPrevious()){
            set.add(iterator.previous());
        }
        return set;
    }
}
