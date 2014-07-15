package models.granite;

import java.util.*;
import play.Logger;
import play.db.ebean.Model;
import javax.persistence.*;


@Entity
public class Investigator extends Model {
    public enum Role {
        PI, Contact
    }

    @Id
    public Long id; // internal id
    public String name;

    @Column(unique=true)
    public Long piId; // PI id
    
    @OneToOne(cascade=CascadeType.ALL)
    public Organization organization;

    public Role role;

    public Investigator () {}
}
