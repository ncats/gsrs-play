package ix.test.server;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * Utility class for getting data
 * from the config file that is passed to
 * Play.
 *
 * Created by katzelda on 4/6/16.
 */
public final class ConfigUtil {


    private static final ConfigUtil DEFAULT = new ConfigUtil();

    private final Config config;


    public static ConfigUtil getDefault(){
        return DEFAULT;

    }

    private ConfigUtil(){
        this(ConfigFactory.load());
    }
    private ConfigUtil(Config config){
        Objects.requireNonNull(config);
        this.config = config;
    }

    public Config getConfig(){
        return config;
    }


    public String getValueFromPath(String...path){
        if(path.length ==0){
            return null;
        }
        if(path.length ==1){
            return getValueAsString(path[0]);
        }
        Iterator<String> iter = Arrays.asList(path).iterator();
        Config currentConf = config;

        while(iter.hasNext()) {
            String p = iter.next();
            if (iter.hasNext()) {
                currentConf = currentConf.getConfig(p);
            } else {
                //leaf
                return currentConf.getString(p);
            }
        }
        //shouldn't be possible
        //but makes compiler happy
        return null;
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
    public File getValueAsFile(String key){
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
    public File getValueAsFile(String key, File defaultValue){
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
    public String getValueAsString(String key){
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
    public String getValueAsString(String key, String defaultValue){
        Objects.requireNonNull(key);

        try{
            return config.getString(key);
        }catch(ConfigException.Missing e){
            return defaultValue;
        }
    }
}
