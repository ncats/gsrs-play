package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name="ct_core_value")
public class Value extends Model {
    @Id
    public Long id;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_value_attribute")
    public List<Attribute> attrs = new ArrayList<Attribute>();

    @ManyToOne(cascade=CascadeType.ALL)
    public Curation curation;

    public Value () {}
}
