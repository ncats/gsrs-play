package crosstalk.ncats.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_ncats_employee")
public class Employee extends NIHAuthor {
    public enum Role {
        Biology,
            Chemistry,
            Informatics
    }

    @Id
    public Long id;
    public boolean isLead;
    public Role role;

    public Employee () {
        ncatsEmployee = true;
    }
    public Employee (Role role) {
        this.role = role;
        ncatsEmployee = true;
    }
}
