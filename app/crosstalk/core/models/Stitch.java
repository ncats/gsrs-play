package crosstalk.core.models;

import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_stitch")
public class Stitch extends Model {
    @Id
    public Long id;

    public String name;

    // name of class that implements the stitching algorithms
    @Column(length=1024)
    public String impl;

    @Lob
    public String description; // stitching algorithm description

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_stitch_attribute")
    public List<Attribute> attrs = new ArrayList<Attribute>();

    public Stitch () {}
    public Stitch (String name) { this.name = name; }
}
