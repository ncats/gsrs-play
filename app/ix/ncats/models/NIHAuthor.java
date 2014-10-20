package ix.ncats.models;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Author;
import ix.core.models.Figure;
import ix.core.models.Indexable;

@Entity
@DiscriminatorValue("NIH")
public class NIHAuthor extends Author {
    @Indexable(facet=true, name="NCATS Employee")
    public boolean ncatsEmployee;

    @Column(length=1024)
    public String dn; // distinguished name
    public Long uid; // unique id

    @Column(length=15)
    public String phone;

    @Lob
    public String biography;
    public String title;

    public NIHAuthor () {}
    public NIHAuthor (String lastname, String forename) {
        super (lastname, forename);
    }
}
