package ix.core.controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.util.CachedSupplier;
import ix.core.util.IOUtil;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for Rest API response codes.  Adds a JSON response body
 * comtaining the status code and a message.  If an Throwable is passed in then
 * the Throwable's message is used.
 * @author katzelda
 */
public final class GsrsApiUtil {

    private GsrsApiUtil(){
        //can no instantiate
    }

    private static String API_ERROR_CODE_FLAG_DEFAULT = "error_response";

    private static CachedSupplier<String> API_ERROR_CODE_FLAG_SUPPLIER = CachedSupplier.of(new Supplier<String>() {
        @Override
        public String get() {
            return Play.application().configuration().getString("ix.api.errorCodeParameter", API_ERROR_CODE_FLAG_DEFAULT);

        }
    });

    private static CachedSupplier<Integer> API_ERROR_CODE_OVERRIDE = CachedSupplier.of(new Supplier<Integer>() {
        @Override
        public Integer get() {
            return Play.application().configuration().getInt("ix.api.forceErrorCodeValue");

        }
    });



    private static JsonNode getError(Throwable t, int status){

        Map m=new HashMap();
        if(t instanceof InvocationTargetException){
            m.put("message", ((InvocationTargetException)t).getTargetException().getMessage());
        }else{
            m.put("message", t.getMessage());
        }
        m.put("status", status);
        ObjectMapper om = new ObjectMapper();
        t.printStackTrace();
        return om.valueToTree(m);
    }

    private static JsonNode createStatusJson(String message, int status){
        Map m=new HashMap();
        m.put("message", message);

        m.put("status", status);
        ObjectMapper om = new ObjectMapper();

        return om.valueToTree(m);
    }

    public static Result badRequest(Throwable t){
        int status = overrideErrorCodeIfNeeded(Http.Status.BAD_REQUEST);
        return Results.status(status,getError(t, status));
    }
    public static Result internalServerError(Throwable t){
        int status = overrideErrorCodeIfNeeded(play.mvc.Http.Status.INTERNAL_SERVER_ERROR);
        return Results.status(status, getError(t, status));
    }
    public static Result unauthorized(Throwable t){
        return Results.internalServerError(getError(t, play.mvc.Http.Status.UNAUTHORIZED));
    }
    public static Result notFound(Throwable t){
        int status = overrideErrorCodeIfNeeded(play.mvc.Http.Status.NOT_FOUND);
        return Results.status(status, getError(new Throwable(t), status));
    }

    private static int overrideErrorCodeIfNeeded(int defaultStatus){
        //GSRS-1598 force not found error to sometimes be a 500 instead of 404
        //if requests tells us
        try {

            Http.Request request = Controller.request();
            if(request !=null){
                String specifiedResponse = request.getQueryString(API_ERROR_CODE_FLAG_SUPPLIER.get());
                if(specifiedResponse !=null){
                    int askedForStatus = Integer.parseInt(specifiedResponse);
                    //status must be a 4xx or 5xx so people can't make it 200
                    if(isValidErrorCode(askedForStatus)){
                        return askedForStatus;
                    }
                }
            }
        }catch(Exception e){
            //no request?
        }
        Integer forceErrorOverride = API_ERROR_CODE_OVERRIDE.get();
        if(forceErrorOverride!=null){
            int asInt = forceErrorOverride.intValue();
            if(isValidErrorCode(asInt)){
                return asInt;
            }
        }
        //use default
        return defaultStatus;
    }

    private static boolean isValidErrorCode(int askedForStatus) {
        return askedForStatus >=400 && askedForStatus< 600;
    }

    public static Result created(String message){
        return Results.created(createStatusJson(message, Http.Status.CREATED));
    }

    /**
     * Returns a status codeo of 204 no content. but the json body contains the status and the given message.
     */
    public static Result deleted(String message){
        return Results.created(createStatusJson(message, Http.Status.NO_CONTENT));
    }
    public static Result badRequest(String t){
        int status = overrideErrorCodeIfNeeded(Http.Status.BAD_REQUEST);
        return Results.status(status, createStatusJson(t, status));
    }
    public static Result internalServerError(String t){
        int status = overrideErrorCodeIfNeeded(play.mvc.Http.Status.INTERNAL_SERVER_ERROR);
        return Results.status(status, getError(new Throwable(t), status));
    }
    public static Result unauthorized(String t){
        return Results.internalServerError(createStatusJson(t, play.mvc.Http.Status.UNAUTHORIZED));
    }
    public static Result notFound(String t){
        int status = overrideErrorCodeIfNeeded(Http.Status.NOT_FOUND);
        return Results.status(status, createStatusJson(t, status));
    }
    public static Result forbidden(String t) {
        return Results.forbidden(getError(new Throwable(t), play.mvc.Http.Status.FORBIDDEN));
    }
}
