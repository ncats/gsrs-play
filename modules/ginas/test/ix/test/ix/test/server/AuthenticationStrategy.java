package ix.test.ix.test.server;

import play.libs.ws.WSRequestHolder;

import java.io.Closeable;

/**
 * Created by katzelda on 9/8/16.
 */
public interface AuthenticationStrategy extends Closeable{

    void login(GinasTestServer.User user);

    void logout();

    void modifyRequest(WSRequestHolder request);

    @Override
    default void close(){
        logout();
    }
}
