package ix.core.auth;

import ix.core.models.UserProfile;
import play.mvc.Http;

/**
 * Created by katzelda on 5/4/16.
 */
public interface Authenticator {

    UserProfile authenticate(Http.Context ctx);
}
