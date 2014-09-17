package crosstalk.core.models;

import java.util.*;
import play.Logger;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_investigator")
public class Investigator extends Model {
    public enum Role {
        PI, Contact
    }

    @Id
    public Long id; // internal id
    public String name;
    public Long piId; // PI id
    
    @ManyToOne(cascade=CascadeType.ALL)
    public Organization organization;

    public Role role;

    public Investigator () {}
}
