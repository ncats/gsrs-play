package ix.test.ix.test.server;

import com.fasterxml.jackson.databind.JsonNode;
import play.api.mvc.Controller;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Created by katzelda on 3/17/16.
 */
public class RestSession extends AbstractSession<Void>{

    private static final String API_URL_USERFETCH = "ginas/app/api/v1/whoami";

    private static final String API_CV_LIST="ginas/app/api/v1/vocabularies";

    public enum AUTH_TYPE{
        USERNAME_PASSWORD,
        USERNAME_KEY,
        TOKEN,
        NONE
    }

    private String key;
    private String token;
    private long deadtime=0;

    private AUTH_TYPE authType = AUTH_TYPE.NONE;

    public RestSession(int port) {
        super(port);
    }
    public RestSession(GinasTestServer.User user, int port){
        this(user, port, AUTH_TYPE.USERNAME_PASSWORD);
    }
    public RestSession(GinasTestServer.User user, int port, AUTH_TYPE type) {
        super(user, port);
        Objects.requireNonNull(type);
        this.authType = type;
    }

    public WSRequestHolder createRequestHolder(String path){
        return url(constructUrlFor(path));
    }
    public WSRequestHolder url(String url){
        WSRequestHolder ws = WS.url(url);


        if(isLoggedIn()) {
            switch (authType) {
                case TOKEN:
                    refreshTokenIfNeccesarry();
                    ws.setHeader("auth-token", this.token);
                    break;
                case USERNAME_KEY:
                    refreshTokenIfNeccesarry();
                    ws.setHeader("auth-username", getUser().getUserName());
                    ws.setHeader("auth-key", this.key);
                    break;
                case USERNAME_PASSWORD:
                    ws.setHeader("auth-username", getUser().getUserName());
                    ws.setHeader("auth-password", getUser().getPassword());
                    break;
                default:
                    break;
            }
        }
        return ws;
    }

    private void refreshTokenIfNeccesarry(){
        if(System.currentTimeMillis()>this.deadtime){
            refreshAuthInfoByUserNamePassword();
        }
    }



    private void refreshAuthInfoByUserNamePassword(){

        WSRequestHolder  ws = WS.url(constructUrlFor(API_URL_USERFETCH));
        ws.setHeader("auth-username", getUser().getUserName());
        ws.setHeader("auth-password", getUser().getPassword());

        WSResponse wsr=ws.get().get(timeout);

        //System.out.println(wsr.getBody());
        JsonNode userinfo=wsr.asJson();
        token=userinfo.get("computedToken").asText();
        key=userinfo.get("key").asText();
        deadtime=System.currentTimeMillis()+userinfo.get("tokenTimeToExpireMS").asLong();


    }

    @Override
    protected Void doLogout() {

        this.key=null;
        this.token=null;
        this.deadtime=0;

        return null;

    }

    @Override
    public WSResponse get(String path) {
        return url(constructUrlFor(path)).get().get(timeout);
    }

    public JsonNode getAsJson(String path){
        return exctractJSON(get(path));
    }

    public JsonNode exctractJSON(WSResponse wsResponse1){
        Objects.requireNonNull(wsResponse1);

        int status2 = wsResponse1.getStatus();
        if(status2>300){
            System.out.println("That's an error!");
            System.out.println(wsResponse1.getBody());
        }
        if(status2 != 200 && status2 != 201){
            throw new IllegalStateException("response status Not OK : " + status2);
        }
        JsonNode returned = wsResponse1.asJson();
        Objects.requireNonNull(returned);
        return returned;
    }

    public WSResponse whoAmI(){
        return get(API_URL_USERFETCH);
    }
    public JsonNode whoAmIJson(){
        return getAsJson(API_URL_USERFETCH);
    }

    public JsonNode urlJSON(String fullUrl){
        return exctractJSON(url(fullUrl).get().get(timeout));
    }

    public ControlledVocab getControlledVocabulary(){
        return new ControlledVocab(getAsJson(API_CV_LIST));
    }
}