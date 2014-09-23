package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@Entity
@Table(name="ct_core_event")
public class Event extends Model {
    @Id
    public Long id;

    @Column(length=1024)
    public String title;
    @Lob
    public String description;
    @Column(length=1024)
    public String url;

    public Date start;
    public Date end;
    public boolean isDuration;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_event_figure")
    public List<Figure> figures = new ArrayList<Figure>();

    public Event () {}
    public Event (String title) {
        this.title = title;
    }
}
