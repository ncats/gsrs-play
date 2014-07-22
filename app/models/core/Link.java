package models.core;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_link")
public class Link extends Model {
    public enum Dir {
        Undirected,
        Directed,
        Self,
        Unknown
    }

    @Id
    public Long id;

    public String name;
    public Dir dir = Dir.Undirected;

    @Column(length=1024)
    public String uri;

    public String source;
    public Long sourceId;

    public String target;
    public Long targetId;

    public Link () {}
}
