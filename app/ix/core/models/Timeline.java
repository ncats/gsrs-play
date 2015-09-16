package ix.core.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="ix_core_timeline")
public class Timeline extends Model {
    @Id
    public Long id;
    public String name;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_timeline_event")
    public List<Event> events = new ArrayList<Event>();
    
    public Timeline () {}
    public Timeline (String name) {
        this.name = name;
    }
}
