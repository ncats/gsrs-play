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
        File workingDir;

        if(Play.isTest()){
            //ginas tests are run from the module directory
            workingDir = new File("../../");
        }else{
            workingDir = new File(".");
        }
        String cvPath = (String) m.get("path");
        if(cvPath ==null){
            throw new IllegalStateException("controlled vocabulary path file must be set");
        }
        this.cvPath = new File(workingDir, cvPath);
        if(!this.cvPath.canRead()){
            throw new IllegalStateException("could not read controlled vocabulary json : " + this.cvPath.getAbsolutePath());
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
