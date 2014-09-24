package crosstalk.core.models;

import java.util.*;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_journal")
public class Journal extends Model {
    @Id
    public Long id;

    @Column(length=10)
    public String issn;
    public String volume;
    public Integer issue;
    public Integer year;
    @Column(length=10)
    public String month;
    @Column(length=256)
    public String title;
    public String isoAbbr; // iso abbreviation

    public Journal () {}
    public Journal (String issn) {
        this.issn = issn;
    }
}
