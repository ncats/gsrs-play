package ix.core.models;

import java.util.List;
import java.util.ArrayList;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_group")
public class Group extends Model {
    @Id
    public Long id;

    @Column(unique=true)
    public String name;

    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ix_core_group_principal")
    public List<Principal> members = new ArrayList<Principal>();

    public Group () {}
    public Group (String name) {
        this.name = name;
    }
}
