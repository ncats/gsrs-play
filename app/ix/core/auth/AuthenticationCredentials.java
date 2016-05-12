package ix.core.auth;

import play.mvc.Http;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by katzelda on 5/5/16.
 */
public class AuthenticationCredentials implements Closeable{

    private final String username;
    private final char[] password;
    private final Http.Context ctx;
    private volatile boolean closed;

    public static AuthenticationCredentials create(String username, String password){
        Objects.requireNonNull(username);
        return new AuthenticationCredentials(username, password.toCharArray(), null);
    }
    public static AuthenticationCredentials create(Http.Context ctx){
        Objects.requireNonNull(ctx);
        return new AuthenticationCredentials(null,null, ctx);
    }

    private AuthenticationCredentials(String username, char[] password, Http.Context ctx) {
        this.username = username;
        this.password = password;
        this.ctx = ctx;
    }

    public String getUsername(){
        verifyNotClosed();
        return username;
    }

    public char[] getPassword(){
        verifyNotClosed();
        if(password ==null){
            return null;
        }
        //defensive copy so clients
        //can't modify password
        return Arrays.copyOf(password, password.length);
    }

    public Http.Context getContext(){
        verifyNotClosed();
        return ctx;
    }

    private void verifyNotClosed() {
        if(closed){
            throw new IllegalStateException("Credentials are closed");
        }
    }

    @Override
    public void close() throws IOException {
        if(password !=null){
            Arrays.fill(password, ' ');
        }
        closed = true;
    }
}
