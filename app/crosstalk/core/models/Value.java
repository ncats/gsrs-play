package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name="ct_core_value")
public class Value extends Model {
    @Id
    public Long id;

    @ManyToOne(cascade=CascadeType.ALL)
    public Property property;

    @ManyToOne(cascade=CascadeType.ALL)
    public Curation curation;

    public Value () {}
    public Value (Property property) {
        this.property = property;
    }
}
