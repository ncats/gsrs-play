package ix.core.models;

import java.util.ArrayList;
import java.util.List;

public enum Role implements be.objectify.deadbolt.core.models.Role {
        Query,
        DataEntry,
        SuperDataEntry,
        Updater,
        SuperUpdate,
        Admin;
        //Guest, Owner, Admin, User; //authenticated user

        @Override
        public String getName() {
            return name();
        }

        public static List<String> options(){
            List<String> vals = new ArrayList<String>();
            for (Role role: Role.values()) {
                vals.add(role.name());
            }
            return vals;
        }

    }

