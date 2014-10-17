package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name="ix_core_attribute")
public class Attribute extends Model {
    @Id
    public Long id;

    public String name;
    public String type;

    @ManyToOne(cascade=CascadeType.ALL)
    public Resource resource;
    public String label; // label used for stitching

    public Attribute () {}
    public Attribute (String name, String type) {
        this.name = name;
        this.type = type;
    }
}
