package ix.core.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nih.ncats.common.util.Holder;
import ix.core.util.IOUtil;
import play.Application;
import play.Configuration;
import play.Plugin;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.mvc.Http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class LoopbackWebRequestPlugin extends Plugin {

    private final Application app;

    private Map<String, RequestAdapter> adapterMapByContext;
    private RequestAdapter defaultAdapter;

    private String protocol, hostname;
    int port;
    public LoopbackWebRequestPlugin(Application app) {
        this.app = app;
        Configuration conf = app.configuration();
        hostname = conf.getString("gsrs.loopback.hostname");
        protocol = conf.getString("gsrs.loopback.protocol");
        port = Integer.parseInt(conf.getString("gsrs.loopback.port"));
    }

    public WSRequestHolder createNewLoopbackRequestFromCurrentRequest(String url){
        return createNewLoopbackRequestFromCurrentRequest(url, null);
    }
    public WSRequestHolder createNewLoopbackRequestFromCurrentRequest(String url, String context){

        return adapt(url, Http.Context.current().request(), adapterMapByContext.get(context));
    }
    public WSRequestHolder createNewLoopbackRequestFrom(String url, play.mvc.Http.Request currentRequest){
        return adapt(url, currentRequest, null);
    }
    public WSRequestHolder createNewLoopbackRequestFrom(String url, play.mvc.Http.Request currentRequest, String context){
        RequestAdapter adapter = adapterMapByContext.get(context);
        return adapt(url, currentRequest, adapter);


    }

    private WSRequestHolder adapt(String url, Http.Request currentRequest, RequestAdapter adapter) {
        String transformedURL;
        try {
            URI urlObj = new URL(url).toURI();


            transformedURL = new URI(protocol, hostname +":"+port,
                    urlObj.getPath(), urlObj.getQuery(), urlObj.getFragment())
            .toString();
//            System.out.println("transformed URL = " + transformedURL);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        WSRequestHolder requestHolder = WS.url(transformedURL);
        if(adapter !=null) {
            return adapter.adaptHolder(requestHolder, currentRequest);
        }
        return defaultAdapter.adaptHolder(requestHolder, currentRequest);
    }

    @Override
    public void onStart() {
        List<?> list = app.configuration().getList("gsrs.loopback.requests");
        ObjectMapper mapper = new ObjectMapper();
        Holder<RequestAdapter> defaultHolder = Holder.hold(null);
        adapterMapByContext = list.stream()
                .map(m-> mapper.convertValue(m, WebRequestConfig.class))
                .filter(m->{
                    if(m.isDefault()){
                        defaultHolder.set( getRequestAdapter(mapper, m));
                        return false;
                    }else{
                        return true;
                    }
                })
                .collect(Collectors.toMap(WebRequestConfig::getContext, c-> getRequestAdapter(mapper, c)));
        defaultAdapter = defaultHolder.get();
    }

    private RequestAdapter getRequestAdapter(ObjectMapper mapper, WebRequestConfig c) {
        Map<String, Object> map;
        if(c.getParameters() ==null){
           map = Collections.emptyMap();
        }else{
            map = c.getParameters();
        }
        try {
            return (RequestAdapter) mapper.convertValue(map, IOUtil.getGinasClassLoader().loadClass(c.getClassname()));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public void onStop() {

    }

    @Override
    public boolean enabled() {
        return true;
    }

    public interface RequestAdapter{
        WSRequestHolder adaptHolder(WSRequestHolder holder, play.mvc.Http.Request parentRequest);
    }
    public static class WebRequestConfig{
        public String context;
        public String classname;
        public boolean isDefault;
        public Map<String, Object> parameters;

        public boolean isDefault() {
            return isDefault;
        }

        public void setDefault(boolean aDefault) {
            isDefault = aDefault;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public String getClassname() {
            return classname;
        }

        public void setClassname(String classname) {
            this.classname = classname;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    public static class AuthHeaderRequestAdapter implements RequestAdapter{

        private Set<String> authHeaders = new HashSet<>();

        public Set<String> getAuthHeaders() {
            return authHeaders;
        }

        public void setAuthHeaders(Set<String> authHeaders) {
            this.authHeaders = authHeaders;
        }

        @Override
        public WSRequestHolder adaptHolder(WSRequestHolder holder, Http.Request parentRequest) {
            if(authHeaders.isEmpty()){
                return holder;
            }
            Map<String, String[]> headers = parentRequest.headers();
            for(String key : authHeaders){
                String[] value = headers.get(key);
                if(value !=null && value.length > 0){
                    for(String v : value) {
//                        System.out.println("setting "+ key + "  to " + v);
                        holder.setHeader(key, v);
                    }
                }
            }
            return holder;
        }
    }
}
