package ix.test.server;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

/**
 * Created by katzelda on 1/4/18.
 */
public class GinasTestServerFactory {
    private static Class<?> instanceClass;

    private static MethodHandle emptyConstructor, portConstructor, mapConstructor, mapAndPortConstructor;

    static{
        String qualitifedPath = ConfigUtil.getDefault().getValueAsString("test.ginas.testServer");
        if(qualitifedPath !=null) {
            try {
                Class cls = Class.forName(qualitifedPath);
                if( GinasTestServer.class.isAssignableFrom(cls)){
                    instanceClass = cls;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(instanceClass ==null){
            instanceClass = GinasTestServer.class;
        }
        try {
            emptyConstructor = MethodHandles.publicLookup().findConstructor(instanceClass, MethodType.methodType(void.class));
        }catch(Exception ignore){
            ignore.printStackTrace();
        }

        try {
            portConstructor = MethodHandles.publicLookup().findConstructor(instanceClass, MethodType.methodType(void.class, int.class));
        }catch(Exception ignore){
            ignore.printStackTrace();
        }

        try {
            mapAndPortConstructor = MethodHandles.publicLookup().findConstructor(instanceClass, MethodType.methodType(void.class, int.class, Map.class));
        }catch(Exception ignore){
            ignore.printStackTrace();
        }
        try {
            mapConstructor = MethodHandles.publicLookup().findConstructor(instanceClass, MethodType.methodType(void.class, Map.class));
        }catch(Exception ignore){
            ignore.printStackTrace();
        }


    }

    public static GinasTestServer createNewTestServer(int port){

        try {
            return (GinasTestServer)  emptyConstructor.invokeExact(port);
        }catch(Throwable e){
            throw new InstanceCreationException(e);
        }
    }
    public static GinasTestServer createNewTestServer(){
        try {
            return (GinasTestServer)  emptyConstructor.invokeExact();
        }catch(Throwable e){
            throw new InstanceCreationException(e);
        }
    }
    public static GinasTestServer createNewTestServer(Map<String,Object> additionalConfig){

        try {
            return (GinasTestServer)  mapAndPortConstructor.invokeExact(additionalConfig);
        }catch(Throwable e){
            throw new InstanceCreationException(e);
        }
    }
    public static GinasTestServer createNewTestServer(int port, Map<String,Object> additionalConfig){

        try {
            return (GinasTestServer)  mapAndPortConstructor.invokeExact(port, additionalConfig);
        }catch(Throwable e){
            throw new InstanceCreationException(e);
        }
    }

    public static class InstanceCreationException extends RuntimeException{
        public InstanceCreationException(String message) {
            super(message);
        }

        public InstanceCreationException(String message, Throwable cause) {
            super(message, cause);
        }

        public InstanceCreationException(Throwable cause) {
            super(cause);
        }
    }
}
