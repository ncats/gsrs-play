package ix.ncats.models;

import ix.core.models.Author;
import ix.core.models.Indexable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@DiscriminatorValue("NIH")
public class NIHAuthor extends Author {
    @Indexable(facet=true, name="NCATS Employee")
    public boolean ncatsEmployee;

    @Column(length=1024)
    public String dn; // distinguished name
    
    @Column(name = "u_id")
    public Long uid; // unique id

    @Column(length=32)
    public String phone;

    @Lob
    public String biography;
    public String title;

    @Lob
    public String research;

    public NIHAuthor () {}
    public NIHAuthor (String lastname, String forename) {
        super (lastname, forename);
    }
}
