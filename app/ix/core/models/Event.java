package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="ix_core_event")
public class Event extends BaseModel {
    public enum Resolution {
        CENTURIES,
        YEARS,
        MONTHS,
        WEEKS,
        DAYS,
        HOURS,
        SECONDS,
        MILLISECONDS
    }
    
    @Id
    public Long id;

    @Indexable(facet=true,name="Event")
    public String title;
    @Lob
    public String description;
    @Column(length=1024)
    public String url;

    @Column(name="start_time")
    public Long start;
    @Column(name="end_time")
    public Long end;

    // time unit for start/end    
    public Resolution unit = Resolution.SECONDS;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_event_prop")
    public List<Value> properties = new ArrayList<Value>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_event_link")
    public List<XRef> links = new ArrayList<XRef>();

    public Event () {}
    public Event (String title) {
        this.title = title;
    }
}
