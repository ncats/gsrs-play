package ix.test.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.util.TimeUtil;
import ix.ginas.exporters.ExportMetaData;
import ix.utils.Util;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

/**
 * Created by katzelda on 3/17/16.
 */
public class RestSession extends AbstractSession<Void>{

    private static final String API_URL_USERFETCH = "ginas/app/api/v1/whoami";

    private static final String API_CV_LIST="ginas/app/api/v1/vocabularies";

    public SubstanceAPI newSubstanceAPI() {
        return new SubstanceAPI(this);
    }

    public BrowserSession newBrowserSession() {
        return new BrowserSession(getUser(), getPort());
    }



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
    Map<String, String> extraHeaders = new HashMap<>();

    private final GinasTestServer ts;

    public RestSession(GinasTestServer ts, int port) {
        super(port);
        this.ts = Objects.requireNonNull(ts);
    }
    public RestSession(GinasTestServer ts, GinasTestServer.User user, int port){
        this(ts, user, port, AUTH_TYPE.USERNAME_PASSWORD);
    }
    public RestSession(GinasTestServer ts, GinasTestServer.User user, int port, AUTH_TYPE type) {
        super(user, port);
        Objects.requireNonNull(type);
        this.authType = type;
        this.ts = Objects.requireNonNull(ts);
    }

    public WSRequestHolder createRequestHolder(String path){
        return urlWithLogin(constructUrlFor(path));
    }
    public WSRequestHolder urlWithLogin(String url){
        WSRequestHolder ws = WS.url(url);
        return withLogin(ws);
    }
    public WSRequestHolder withLogin(WSRequestHolder ws){
        
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
        extraHeaders.forEach((k,v)->{
            ws.setHeader(k, v);
        });
        return ws;
    }
    
    public void setAdditionalHeader(String key, String value){
    	extraHeaders.put(key, value);
    }
    public void clearAdditionalHeaders(){
    	extraHeaders.clear();
    }

    private void refreshTokenIfNeccesarry(){
        if(TimeUtil.getCurrentTimeMillis()>this.deadtime){
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
        deadtime= TimeUtil.getCurrentTimeMillis() + userinfo.get("tokenTimeToExpireMS").asLong();


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
        return getRequest(path).get().get(timeout);
    }
    
    public WSRequestHolder getRequest(String path){
        return urlWithLogin(constructUrlFor(path));
    }
    
    public JsonNode getAsJson(WSRequestHolder hold){
        return extractJSON(hold.get().get(timeout));
    }

    public JsonNode getAsJson(String path){
        return extractJSON(get(path));
    }

    
    public String getSha1For(String path){
    	return Util.sha1(get(path).getBody());
    }


    public WSResponse whoAmI(){
        return get(API_URL_USERFETCH);
    }
    public JsonNode whoAmIJson(){
        return getAsJson(API_URL_USERFETCH);
    }

    public JsonNode urlJSON(String fullUrl){
        return extractJSON(urlWithLogin(fullUrl).get().get(timeout));
    }

    public ControlledVocab getControlledVocabulary(){
        return new ControlledVocab(getAsJson(API_CV_LIST));
    }


    public MyDownloadsAPI newDownloadAPI(){
        return new MyDownloadsAPI(this, ts);
    }
}
