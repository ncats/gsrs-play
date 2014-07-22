package models.core;

import java.util.*;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_journal")
public class Journal extends Model {
    @Id
    public Long id;

    @Column(length=10)
    public String issn;
    public Integer volume;
    public Integer issue;
    public Integer year;
    public Integer month;
    public String title;
    public String isoAbbr; // iso abbreviation

    public Journal () {}
    public Journal (String issn) {
        this.issn = issn;
    }
}
