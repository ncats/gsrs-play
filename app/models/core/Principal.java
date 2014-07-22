package models.core;

import java.util.List;
import java.util.ArrayList;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_principal")
public class Principal extends Model {
    @Id
    public Long id;

    @Column(unique=true)
    public String name;

    @OneToMany(cascade=CascadeType.ALL)
    public List<Permission> permissions = new ArrayList<Permission>();

    public Principal () {}
    public Principal (String name) {
        this.name = name;
    }
}
