package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_author")
public class Author extends Model {
    @Id
    public Long id; // internal id

    public String lastname;
    public String forename;
    public String initials;
    public String affiliation;
    @Column(length=20)
    public String orcid; // http://orcid.org/
    public String email;
    @Column(length=1024)
    public String url;

    public Author () {}
}
