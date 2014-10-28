package ix.core.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name="ix_core_value")
@Inheritance
@DiscriminatorValue("VAL")
public class Value extends Model {
    @Id
    public Long id;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_value_attribute",
               joinColumns=@JoinColumn(name="ix_core_value_id"))
    public List<Attribute> attrs = new ArrayList<Attribute>();

    @ManyToOne(cascade=CascadeType.ALL)
    public Curation curation;

    public Value () {}
}
