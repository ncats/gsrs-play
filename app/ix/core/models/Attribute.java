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
    @Column(length=1024)
    public String value;

    @ManyToOne(cascade=CascadeType.ALL)
    public Resource resource;

    public Attribute () {}
    public Attribute (String name, String value) {
        this.name = name;
        this.value = value;
    }
}
