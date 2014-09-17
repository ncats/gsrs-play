package crosstalk.ncats.models;

import play.Logger;
import com.avaje.ebean.*;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.ServerConfigStartup;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;

import crosstalk.core.adapters.InvestigatorPersistAdapter;

public class GrantServerConfigStartup implements ServerConfigStartup {    
    @Override
    public void onStart (ServerConfig serverConfig) {
        Logger.debug("ServerConfigStartup.onStart()");
        serverConfig.add(new InvestigatorPersistAdapter ());
    }
}
