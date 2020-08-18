package ix.ginas.controllers.plugins;

import gov.nih.ncats.common.functions.ThrowableFunction;
import gov.nih.ncats.common.functions.ThrowableSupplier;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.text.IndexListener;
import ix.core.search.text.IndexerService;
import ix.core.search.text.Lucene4IndexService;
import ix.core.search.text.TextIndexer;
import ix.core.services.RendererService;
import ix.ncats.controllers.App;
import ix.ncats.services.DefaultRenderer;
import org.jcvi.jillion.core.util.streams.ThrowingFunction;
import play.Application;
import play.api.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GsrsDependencyPlugin implements Plugin {

    private final Dependencies dependencies;

    public GsrsDependencyPlugin(Application app) {
        String dependenciesClassname = app.configuration().getString("gsrs.dependencies.classname");
        if(dependenciesClassname ==null){
            dependencies = new Dependencies();
        }else{
            try {
                dependencies = (Dependencies) Class.forName(dependenciesClassname).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("could not instantiate Dependencies class "+dependenciesClassname);
            }
        }

    }
    @Override
    public void onStart() {
        App.setRendererService(dependencies.getRendererService());
    }

    public IndexerService createIndexerService(File dir) throws IOException{
        return dependencies.indexerServiceSupplier().apply(dir);
    }

    @Override
    public void onStop() {
        //no-op
    }

    @Override
    public boolean enabled() {
        return true;
    }



    public static class Dependencies{

        public Dependencies(){}

        public RendererService getRendererService(){
            return DefaultRenderer.INSTANCE;
        }

        public List<IndexListener> getIndexListeners(){
            return Collections.emptyList();
        }

        public ThrowableSupplier<IndexerService, IOException> inMemoryIndexerServiceSupplier(){
            return ()->new Lucene4IndexService();
        }

        public ThrowableFunction<File, IndexerService, IOException> indexerServiceSupplier(){
            return (file)->new Lucene4IndexService(file);
        }
    }
}
