package ix.test.server;

import ix.core.util.ConfigHelper;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;

public class GinasHttpResolver {
    private final GinasTestServer ts;



    private final String appContext;
    public GinasHttpResolver(GinasTestServer ts) {
        this.ts = ts;
        appContext = ts.getApplicationContext();
    }

    public String apiV1(String path){
        return apiV1(path, true);
    }

    public String substance(String path){
        return substance( path, true);
    }

    public String get(String path){
        return get(path, true);
    }
    public String apiV1(String path, boolean withContext){
        return resolve("/api/v1", path, withContext);
    }

    public String substance(String path, boolean withContext){
        return resolve("/substance", path, withContext);
    }

    public String get(String path, boolean withContext){
        return resolve(null, path, withContext);
    }

    private String resolve(String prefix, String path, boolean withContext){
        StringBuilder builder = new StringBuilder(appContext.length() + path.length() + 20);
        if(withContext) {
            builder.append(appContext);
        }
        if(prefix !=null){
            builder.append(prefix);
        }
        if(path.startsWith("/")){
            builder.append(path);
        }else{
            builder.append('/').append(path);
        }
        return builder.toString();
    }
}
