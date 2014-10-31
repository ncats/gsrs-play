package ix.core.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name="ix_core_value")
@Inheritance
@DiscriminatorValue("VAL")
public class Value extends Model {
    @Id
    public Long id;
    public String label;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_value_attribute")
    public List<Attribute> attrs = new ArrayList<Attribute>();

    @ManyToOne(cascade=CascadeType.ALL)
    public Curation curation;
    
    public Value () {}
    public Value (String label) {
        this.label = label;
    }

    @JsonIgnore
    public Object getValue () {
        throw new UnsupportedOperationException
            ("getValue is not defined for class "+getClass().getName());
    }
}
