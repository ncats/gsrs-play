package ix.test.server;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.util.EntityUtils;
import ix.ginas.exporters.ExportMetaData;
import ix.ginas.exporters.ExportProcessFactory;
import ix.test.util.WaitChecker;
import play.libs.ws.WSResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class WebExportRequest {
    private String format;
    private String key;
    private boolean publicOnly=true;

    private long timeout;
    private final AbstractSession session;

    public WebExportRequest(String key, String format, AbstractSession session){
        this.format=format;
        this.key=key;
        this.session = session;
        this.timeout = session.getDefaultTimeout();
    }

    public WebExportRequest setPublicOnly(boolean publicOnly){
        this.publicOnly = publicOnly;
        return this;
    }
    public WebExportRequest setTimeout(long timeout){
        this.timeout=timeout;
        return this;
    }
    public WebExportRequest setKey(String key){
        this.key=key;
        return this;
    }
    public WebExportRequest setFormat(String format){
        this.format=format;
        return this;
    }

    public InputStream getInputStream(){
        return getInputStream(true);
    }
    public InputStream getInputStream(boolean forceReDownload){
        if(!forceReDownload){
            JsonNode metaData = getMeta();
            if(metaData.at("/isCached").asBoolean()){
                try {
                    ExportMetaData cached = EntityUtils.getEntityInfoFor(ExportMetaData.class).fromJson(metaData.at("/cached").toString());
                    return ExportProcessFactory.download(cached.username, cached.getFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //if we get here either we ar forcing a redownload or something went wrong getting the cached version.
        return getWSResponse()
                .getBodyAsStream();


    }

    public WSResponse getWSResponse(){
        String url=getMeta().at("/url").asText();

        WSResponse osess = session.get(url);

        if(osess.getStatus()>=400){
            return osess;
        }

        JsonNode status =  osess.asJson();
        System.out.println("STATUS = " + status);
        String pingUrl = status.at("/self").asText();
//        System.out.println("PING URL = " + pingUrl);
        /*
        long timeoutTime = System.currentTimeMillis()+10_000;
        while(System.currentTimeMillis()<timeoutTime){
            if(status.at("/complete").asBoolean()){
                String dl=status.at("/downloadUrl").asText();
                return BrowserSubstanceSearcher.this.session.get(dl, timeout);
            }
            status = BrowserSubstanceSearcher.this.session.get(pingUrl, timeout).asJson();

        }
        */
        try {
            Optional<WSResponse> resp =new WaitChecker<>(
                    ()->session.get(pingUrl).asJson(),
                    n -> n.at("/complete").asBoolean(),
                    n -> session.get(n.at("/downloadUrl").asText())
            ).setMaxNumTries(10)
//                        .setAwaitTime(1, TimeUnit.SECONDS)  //default is 1 sec
                    .execute();

            return resp.orElseThrow( () -> new IllegalStateException("Export timed out"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public JsonNode getMeta(){
        WSResponse resp = session.get(session.getTestSever().getHttpResolver().get( "/setExport?id="+key
                + "&format="+format + "&publicOnly=" + (publicOnly?1:0)));
        return resp.asJson();
    }

    public boolean isReady(){
        return getMeta().at("/isReady").asBoolean();
    }
}
