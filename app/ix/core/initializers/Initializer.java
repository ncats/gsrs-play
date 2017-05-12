package ix.core.initializers;

import java.util.Map;

import play.Application;

public interface Initializer {
    
    /**
     * Settings to initialize with, passed from the config file.
     * @param m
     */
    public default Initializer initializeWith(Map<String,?> m){
        return this;
    }
    
    public void onStart(Application app);
}
