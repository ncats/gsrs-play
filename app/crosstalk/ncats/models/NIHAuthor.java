package crosstalk.ncats.models;

import play.db.ebean.Model;
import javax.persistence.*;

import crosstalk.core.models.Author;

@Entity
@Table(name="ct_ncats_author")
public class NIHAuthor extends Author {
    public boolean ncatsEmployee;
    public String ic; // institute/center

    @Column(length=1024)
    public String dn; // distinguished name
    public Long uid; // unique id

    @Lob
    public String biography;
    public String title;

    public NIHAuthor () {}
}
