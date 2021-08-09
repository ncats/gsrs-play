package ix.ginas.utils.validation.validators;

import ix.core.UserFetcher;
import ix.core.models.UserProfile;
import ix.ginas.initializers.LoadValidatorInitializer;
import ix.ginas.utils.validation.ValidatorPlugin;

/**
 * Created by katzelda on 5/7/18.
 */
public abstract class AbstractValidatorPlugin<T> implements ValidatorPlugin<T> {

    public boolean addNote = true;

    @Override
    public boolean supports(T newValue, T oldValue, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType) {
        if(methodType == LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE.IGNORE){
            return false;
        }
        return true;
    }

    protected UserProfile getCurrentUser(){
        UserProfile up= UserFetcher.getActingUserProfile(true);
        if(up==null){
            up=UserProfile.GUEST();
        }
        return up;
    }
}
