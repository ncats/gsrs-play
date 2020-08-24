package ix.core.controllers.services;

import ix.core.plugins.MicroServicePlugin;
import ix.core.util.CachedSupplier;
import play.Play;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WS;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractService {
    private static CachedSupplier<MicroServicePlugin> gatewayUrlSupplier = CachedSupplier.of(new Supplier<MicroServicePlugin>() {
        @Override
        public MicroServicePlugin get() {
            return Play.application().plugin(MicroServicePlugin.class);
        }
    });

    private String serviceName;

    protected AbstractService(String serviceName){
        this.serviceName = Objects.requireNonNull(serviceName);
    }
    protected WSRequestHolder createRequestFor(String path){
        return  gatewayUrlSupplier.get().url( path==null?serviceName: serviceName+path);
    }
    protected WSRequestHolder createRequestFor(String path, Map<String,String> params){
        WSRequestHolder requestHolder = createRequestFor(path);
        for(Map.Entry<String, String> param : params.entrySet()) {
            requestHolder.setQueryParameter(param.getKey(), param.getValue());
        }
        return requestHolder;
    }
}
