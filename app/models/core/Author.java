package models.core;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_author")
public class Author extends Model {
    @Id
    public Long id; // internal id

    public String lastname;
    public String forename;
    public String initials;
    public String affiliation;
    public String orcid; // http://orcid.org/
    public String email;
    public String url;

    public Author () {}
}
