package ix.core.plugins;

import ix.core.factories.ApiFunctionFactory;
import play.Application;
import play.Plugin;

public class ApiFunctionPlugin extends Plugin {
	Application app;
	ApiFunctionFactory factory;
	
	public ApiFunctionPlugin(Application app) {
		this.app=app;
		factory=ApiFunctionFactory.getInstance(app);
    }

    @Override
    public void onStart() {
    	
    }

    @Override
    public void onStop() {
    	
    }
}
