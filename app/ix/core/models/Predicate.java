package ix.core.models;

import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_predicate")
@Inheritance
@DiscriminatorValue("PRE")
public class Predicate extends Model {
    @Id
    public Long id;

    @Column(nullable=false)
    @Indexable(name="Predicate",facet=true)
    public String name;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_predicate_annotation")
    public List<Value> annotations = new ArrayList<Value>();

    @OneToOne
    @Column(nullable=false)
    public XRef object;

    public Predicate () { }
    public Predicate (String name, XRef object) {
        this.name = name;
        this.object = object;
    }
}
