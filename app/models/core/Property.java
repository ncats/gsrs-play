package models.core;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_property")
public class Property extends Model {
    @Id
    public Long id;

    public String name;
    public String type;

    public Property () {}
}
