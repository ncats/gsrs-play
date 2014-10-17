package ix.ncats.models;

import play.db.ebean.Model;
import javax.persistence.*;
import ix.core.models.Indexable;

@Entity
@Table(name="ix_ncats_employee")
public class Employee extends NIHAuthor {
    public enum Role {
        Biology,
            Chemistry,
            Informatics,
            Admin,
            Other
    }

    public enum Department {
        Probe,
            TRND,
            OD,
            Other
            }
            
    public boolean isLead;

    @Indexable(facet=true)
    public Department dept;

    @Indexable(facet=true)
    public Role role;

    public Employee () {
        ncatsEmployee = true;
    }

    public Employee (Role role) {
        this.role = role;
        ncatsEmployee = true;
    }
}
