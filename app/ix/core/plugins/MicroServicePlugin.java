package ix.core.plugins;

import ix.core.util.CachedSupplier;
import play.Application;
import play.Logger;
import play.Play;
import play.api.Plugin;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;

import java.util.function.Supplier;

public class MicroServicePlugin implements Plugin {
    private static String gatewayUrl;

    private boolean shouldUse;
    private boolean useLoopback;

    private static CachedSupplier<LoopbackWebRequestPlugin> loopbackPlugin = CachedSupplier.of(new Supplier<LoopbackWebRequestPlugin>() {
        @Override
        public LoopbackWebRequestPlugin get() {
            return Play.application().plugin(LoopbackWebRequestPlugin.class);
        }
    });

    public MicroServicePlugin(Application app){
        Boolean isEnabled = app.configuration().getBoolean("gsrs.microservices.enabled");
        shouldUse = isEnabled ==null? false: isEnabled;

        Logger.debug("microservice is enabled ? " + shouldUse);
        if(!shouldUse){
            return;
        }
        Boolean shouldLoopback = app.configuration().getBoolean("gsrs.microservices.useLoopback");
        useLoopback = shouldLoopback==null?false:shouldLoopback;
        String url = app.configuration().getString("gsrs.microservices.gateway-url");

        if(url ==null){
            throw new IllegalStateException("must declare gsrs.microservices.gateway-url");
        }
        //add trailing / if missing
        char lastChar = url.charAt(url.length()-1);
        if(lastChar != '/'){
            url = url +'/';
        }
        gatewayUrl = url;
    }

    public boolean shouldUse(){
        return shouldUse;
    }
    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {

    }

    public static String getGatewayUrl(){
        return gatewayUrl;
    }

    public WSRequestHolder url(String path){
        //gateway url should always end in '/' so raw concat is OK
        if(useLoopback){
            return loopbackPlugin.get().createNewLoopbackRequestFromCurrentRequest(gatewayUrl+path);
        }
        return WS.url(gatewayUrl+path);
    }
}
