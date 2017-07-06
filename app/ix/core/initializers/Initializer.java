package ix.core.initializers;

import java.util.Map;

import play.Application;

public interface Initializer {
    
    /**
     * Settings to initialize with, passed from the config file.
     * @param m
     */
    default Initializer initializeWith(Map<String,?> m){
        return this;
    }
    
    void onStart(Application app);
}
