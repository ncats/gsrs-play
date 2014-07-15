package models.granite;

import play.Logger;
import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;

import com.fasterxml.jackson.databind.ObjectMapper;


public  class InvestigatorPersistAdapter extends BeanPersistAdapter {
    ObjectMapper mapper = new ObjectMapper ();
    public InvestigatorPersistAdapter () {}
    
    public boolean isRegisterFor (Class<?> cls) {
        return cls.isAssignableFrom(Investigator.class);
    }
    
    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
        Investigator inv = (Investigator)request.getBean();
        try {
            Logger.debug("postUpdate["+inv.id+"]: old="
                         +mapper.writeValueAsString(request.getOldValues())
                         +" new="+mapper.writeValueAsString(inv));
        }
        catch (Exception ex) {
            Logger.error("Json serialization", ex);
        }
    }
}
