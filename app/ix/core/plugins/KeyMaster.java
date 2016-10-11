package ix.core.plugins;

import java.util.Set;

/**
 * Created by katzelda on 5/24/16.
 */
public interface KeyMaster {
    Set<String> getAllAdaptedKeys(String baseKey);

    void addKey(String baseKey, String adaptKey);

    void removeKey(String baseKey, String adaptKey);


    default void add(String baseKey) {
        addKey(baseKey, adaptKey(baseKey));
    }


    public String adaptKey(String baseKey);

    public String unAdaptKey(String adaptedKey);

    void removeAll();
    
}
