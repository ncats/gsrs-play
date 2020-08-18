package ix.ginas.initializers;

import ix.core.initializers.Initializer;
import ix.core.plugins.SchedulerPlugin;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import play.Application;
import play.Play;

import java.io.*;
import java.util.Map;

/**
 * Created by katzelda on 4/30/18.
 */
public class LoadControlledVocabInitializer implements Initializer {

    private File cvPath;
    @Override
    public Initializer initializeWith(Map<String, ?> m)
    {
//		m.keySet().forEach((key) ->
//		{
//			System.out.println(String.format("key: %s; value: %s", key, m.get(key).toString()));
//		});
        cvPath = new File((String) m.get("cv.path"));
        if(!cvPath.canRead()){
            throw new IllegalStateException("could not read controlled vocabulary json : " + cvPath.getAbsolutePath());
        }
        return this;
    }
    @Override
    public void onStart(Application app) {
        if(app.configuration()
                .getBoolean("ix.ginas.init.loadCV", true) &&
                !ControlledVocabularyFactory.isloaded()) {
            try(InputStream is = new BufferedInputStream(new FileInputStream(cvPath))) {
                ControlledVocabularyFactory.loadCVJson(is);
            }catch(IOException e){
                throw new UncheckedIOException(e);
            }
        }


    }


}
