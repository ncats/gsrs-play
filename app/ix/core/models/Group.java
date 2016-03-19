package ix.core.models;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import play.db.ebean.Model;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name="ix_core_group")
public class Group extends BaseModel {
    @Id
    public Long id;

    @Column(unique=true)
    public String name;

    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ix_core_group_principal")
    @JsonView(BeanViews.Full.class)
    public Set<Principal> members = new HashSet<Principal>();

    public Group () {}
    public Group (String name) {
        this.name = name;
    }
}
