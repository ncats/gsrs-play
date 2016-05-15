package ix.core.auth;

import ix.core.models.UserProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by katzelda on 5/5/16.
 */
public final class Authenticators {

    private static List<? extends Authenticator> chain = Arrays.asList( new DefaultAuthenticator());

    public static UserProfile authenticate(AuthenticationCredentials creds){
        Objects.requireNonNull(creds);

        for(Authenticator auth : chain){
            UserProfile profile = auth.authenticate(creds);
            if(profile !=null){
                return profile;
            }
        }
        return null;
    }
}
