package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_journal")
public class Journal extends Model {
    @Id
    public Long id;

    @Column(length=10)
    public String issn;

    public String volume;
    public String issue;

    @Indexable(facet=true,name="Year")
    public Integer year;

    @Column(length=10)
    public String month;

    @Column(length=256)
    @Indexable(facet=true, name="Journal")
    public String title;

    public String isoAbbr; // iso abbreviation
    public Double factor; // impact factor

    public Journal () {}
    public Journal (String issn) {
        this.issn = issn;
    }
}
