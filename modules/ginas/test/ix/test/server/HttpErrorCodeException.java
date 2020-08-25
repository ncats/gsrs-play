package ix.test.server;

import play.libs.ws.WSResponse;

import java.io.IOException;

public class HttpErrorCodeException extends IOException {
    private WSResponse response;

    public HttpErrorCodeException(WSResponse response) {
        super(response.getStatus() + " : " + response.getStatusText());
        this.response = response;
    }

    public WSResponse getResponse() {
        return response;
    }

    public int getStatus(){
        return response.getStatus();
    }
}
