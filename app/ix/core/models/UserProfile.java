package ix.core.models;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_userprof")
public class UserProfile extends IxModel {
    @OneToOne(cascade=CascadeType.ALL)
    public Principal user;
    
    // is the profile currently active? authorization should take
    // this into account
    public boolean active;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_userprof_prop")
    public List<Value> properties = new ArrayList<Value>();

    public UserProfile () {}
    public UserProfile (Principal user) {
        this.user = user;
    }
}
