package models.core;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_value")
public class Value extends Model {
    @Id
    public Long id;

    @Column(nullable=false)
    public String label; // label used for stitching

    @ManyToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    public Property property;

    public Value () {}
    public Value (String label, Property property) {
        this.label = label;
        this.property = property;
    }
}
