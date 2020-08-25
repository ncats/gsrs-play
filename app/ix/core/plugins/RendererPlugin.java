package ix.core.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nih.ncats.molwitch.renderer.ChemicalRenderer;
import gov.nih.ncats.molwitch.renderer.RendererOptions;
import play.Application;
import play.Configuration;
import play.Plugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RendererPlugin extends Plugin {
    private final Application app;

    private Map<String, RendererConfiguration> rendererConfigurationMap;

    private ChemicalRenderer defaultRenderer;

    public RendererPlugin(Application app) {
        this.app = app;
    }

    private Map<String,?> optionMap;
    @Override
    public void onStart() {
        rendererConfigurationMap = new LinkedHashMap<>();
        rendererConfigurationMap.put("DEFAULT", createRenderer("DEFAULT", new ChemicalRenderer()));
        rendererConfigurationMap.put("INN", createRenderer("INN", new ChemicalRenderer(RendererOptions.createINNLike())));
        ChemicalRenderer usp = new ChemicalRenderer(RendererOptions.createUSPLike());
        usp.setShadowVisible(false);

        rendererConfigurationMap.put("USP", createRenderer("USP", usp));


        Configuration configuration = app.configuration();
        List<Object> list = configuration.getList("gsrs.renderers.list");
        ObjectMapper mapper = new ObjectMapper();
        if(list !=null){
           for(Object o : list){
               RendererConfiguration conf = mapper.convertValue(o, RendererConfiguration.class);
               rendererConfigurationMap.put(conf.getName(), conf);
           }
        }
        String selected = configuration.getString("gsrs.renderers.selected");
        RendererConfiguration selectedRenderer=null;
        if(selected !=null){
            selectedRenderer=rendererConfigurationMap.get(selected);
        }
        if(selectedRenderer==null){
            defaultRenderer = rendererConfigurationMap.get("DEFAULT").getRenderer();
        }else{
            defaultRenderer = selectedRenderer.getRenderer();
        }
        optionMap = defaultRenderer.getOptions().asNonDefaultMap();
    }

    private RendererConfiguration createRenderer(String name, ChemicalRenderer renderer){
        RendererConfiguration conf = new RendererConfiguration();
        conf.setName(name);
        conf.setRenderer(renderer);
        return conf;
    }

    public RendererOptions newRendererOptions(){
       return RendererOptions.createFromMap(optionMap);

    }

    public ChemicalRenderer newChemicalRenderer(RendererOptions opts){
        ChemicalRenderer renderer = new ChemicalRenderer(opts);
        renderer.setBackgroundColor(defaultRenderer.getBackgroundColor().asColor());
        renderer.setBorderColor(defaultRenderer.getBorderColor().asColor());
        renderer.setShadowVisible(defaultRenderer.isShadowVisible());
        renderer.setBorderVisible(defaultRenderer.isBorderVisible());

        return renderer;
    }
    public ChemicalRenderer getRenderer(){
        return defaultRenderer;
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean enabled() {
        return true;
    }


    public static class RendererConfiguration{
        public String name;
        public ChemicalRenderer renderer;



        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ChemicalRenderer getRenderer() {
            return renderer;
        }

        public void setRenderer(ChemicalRenderer renderer) {
            this.renderer = renderer;
        }
    }
}
