package ix.core.models;

import java.util.*;
import play.Logger;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_investigator")
public class Investigator extends LongBaseModel {
    public enum Role {
        PI, Contact
    }

    @Id
    public Long id; // internal id

    // can be contact..
    @Indexable(facet=true, name="Investigator")
    public String name;

    public Long piId; // PI id
    
    @ManyToOne(cascade = CascadeType.PERSIST)
    public Organization organization;

    public Role role;

    public Investigator () {}
}
