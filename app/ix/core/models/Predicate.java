package ix.core.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

@Entity
@Table(name="ix_core_predicate")
@Inheritance
@DiscriminatorValue("PRE")
public class Predicate extends IxModel {
    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    public XRef subject;

    @Column(nullable=false)
    @Indexable(name="Predicate",facet=true)
    public String predicate;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_predicate_object")
    @Column(nullable=false)
    public List<XRef> objects = new ArrayList<XRef>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_predicate_property")
    public List<Value> properties = new ArrayList<Value>();

    public Predicate () { }
    public Predicate (String predicate) {
        this.predicate = predicate;
    }

    public XRef addIfAbsent (XRef xref) {
        for (XRef xr : objects) {
            if (xr.refid.equals(xref.refid)
                && xr.kind.equals(xref.kind))
                return xr;
        }
        objects.add(xref);
        return xref;
    }
}
