package crosstalk.core.models;

import java.lang.reflect.*;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_link")
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
    public Link (Object src, Object dst) {
        this (src, dst, Dir.Undirected);
    }
    public Link (Object src, Object dst, Dir dir) {
        if (src == null || dst == null)
            throw new IllegalArgumentException
                ("Source and/or target object is null");

        try {
            source = src.getClass().getName();
            Method m = src.getClass().getMethod("getId");
            sourceId = (Long)m.invoke(src);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException
                ("Source object does not have getId method");
        }

        try {
            target = dst.getClass().getName();
            Method m = dst.getClass().getMethod("getId");
            targetId = (Long)m.invoke(dst);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException
                ("Target object does not have getId method");
        }
    }
}
