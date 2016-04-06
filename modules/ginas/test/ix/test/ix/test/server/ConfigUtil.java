package ix.test.ix.test.server;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Objects;

/**
 * Utility class for getting data
 * from the config file that is passed to
 * Play.
 *
 * Created by katzelda on 4/6/16.
 */
public final class ConfigUtil {

    private ConfigUtil(){
        //can not instantiate
    }
    /**
     * Get the value of the given key as a File object
     * or the given {@code null} if the key is not present.
     *
     * @param key the key to get; can not be null.
     *
     * @return the File object for the key or the null if
     * the key is not in the config.  The path to the returned
     * File object may not exist.
     */
    public static File getValueAsFile(String key){
        return getValueAsFile(key, null);
    }
    /**
     * Get the value of the given key as a File object
     * or the given defaultValue if the key is not present.
     *
     * @param key the key to get; can not be null.
     * @param defaultValue the default File object to return
     *                     if the key is not present; may be null.
     * @return the File object for the key or the default value if
     * the key is not in the config.  The path to the returned
     * File object may not exist.
     */
    public static File getValueAsFile(String key, File defaultValue){
        String path = getValueAsString(key);
        if(path ==null){
            return defaultValue;
        }
        return new File(path);
    }

    /**
     * Get the value of the given key as a String
     * or {@code null} if the key is not present.
     *
     * This is the same as {@link #getValueAsString(String, String) getValueAsString(key, null)}.
     *
     * @param key the key to get can not be null.
     *
     * @return the value of the key as a String, or the {@code null} if
     * the key is not in the config.
     */
    public static String getValueAsString(String key){
        return getValueAsString(key, null);
    }
    /**
     * Get the value for the given key as a String.
     * @param key the key to get can not be null.
     * @param defaultValue the value to return if the given key is not in the config
     *                     (may be null).
     * @return the value of the key as a String, or the defaultValue if
     * the key is not in the config.
     */
    public static String getValueAsString(String key, String defaultValue){
        Objects.requireNonNull(key);

        Config load = ConfigFactory.load();
        try{
            return load.getString(key);
        }catch(ConfigException.Missing e){
            return defaultValue;
        }
    }
}
