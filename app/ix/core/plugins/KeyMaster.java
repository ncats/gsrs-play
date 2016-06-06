package ix.core.plugins;

import ix.core.UserFetcher;
import ix.utils.Util;

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


    default String adaptKey(String baseKey) {
        final String user = UserFetcher.getActingUser(true).username;
        return "!" + baseKey + "#" + Util.sha1(user);
    }

    default String unAdaptKey(String adaptedKey) {
        if (!adaptedKey.startsWith("!")) {
            return adaptedKey;
        }
        return adaptedKey.substring(1, adaptedKey.lastIndexOf('#'));
    }

}
