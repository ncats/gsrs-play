package models.core;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;


@Entity
@Table(name="ct_payload")
public class Payload extends Model {
    @Id
    public Long id;

    @Column(length=1024)
    public String name;
    @Column(length=40)
    public String sha1;
    @Column(length=128)
    public String mime; // mime type
    public Long size;
    public Date created;

    @OneToMany(cascade=CascadeType.ALL)
    public List<Property> properties = new ArrayList<Property>();

    public Payload () {}
}
