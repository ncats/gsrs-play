package models.core;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_permission")
public class Permission extends Model {
    @Id
    public Long id;

    public String resource;
    public String access;

    public Permission () {}
    public Permission (String resource, String access) {
        this.resource = resource;
        this.access = access;
    }
}
