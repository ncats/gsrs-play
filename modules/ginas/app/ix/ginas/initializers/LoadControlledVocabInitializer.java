package ix.ginas.initializers;

import ix.core.initializers.Initializer;
import ix.core.plugins.SchedulerPlugin;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import play.Application;
import play.Play;

/**
 * Created by katzelda on 4/30/18.
 */
public class LoadControlledVocabInitializer implements Initializer {

    @Override
    public void onStart(Application app) {
        if(app.configuration()
                .getBoolean("ix.ginas.init.loadCV", true) &&
                !ControlledVocabularyFactory.isloaded()) {
            ControlledVocabularyFactory.loadCVJson(app.resourceAsStream("cv.json"));
        }


    }


}
