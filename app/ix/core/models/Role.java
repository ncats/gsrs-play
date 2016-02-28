package ix.core.models;

import java.util.ArrayList;
import java.util.List;

public class Role implements be.objectify.deadbolt.core.models.Role {
    public enum Kind {
        Query,
        DataEntry,
        SuperDataEntry,
        Updater,
        SuperUpdate,
        Admin;
        //Guest, Owner, Admin, User; //authenticated user
    }
    public Kind role;
    public Role(Kind role) {
        this.role = role;
    }
    @Override
    public String getName() {
        return role.name();
    }

    public static List<String> options(){
        List<String> vals = new ArrayList<String>();
        for (Kind role: Kind.values()) {
            vals.add(role.name());
        }
        return vals;
    }
    

}
