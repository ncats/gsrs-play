package ix.core.models;

import java.util.*;
import play.Logger;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_investigator")
public class Investigator extends Model {
    public enum Role {
        PI, Contact
    }

    @Id
    public Long id; // internal id

    // can be contact..
    @Indexable(facet=true, name="Investigator")
    public String name;

    public Long piId; // PI id
    
    @ManyToOne(cascade=CascadeType.ALL)
    public Organization organization;

    public Role role;

    public Investigator () {}
}
