package ix.core.auth;

import ix.core.factories.AuthenticatorFactory;
import ix.core.models.UserProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by katzelda on 5/5/16.
 * 
 * @deprecated Use {@link AuthenticatorFactory} instead.
 */
@Deprecated
public final class Authenticators {

    private static List<? extends Authenticator> chain = Arrays.asList( new DefaultAuthenticator());

    
    /**
     * 
     * @param creds
     * @return
     * 
     * @deprecated Use {@link AuthenticatorFactory#getAuthenticator()} instead. 
     */
    @Deprecated
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
