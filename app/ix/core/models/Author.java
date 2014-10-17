package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_author")
public class Author extends Principal {
    @Id
    public Long id; // internal id

    public String lastname;
    public String forename;
    public String initials;

    @Column(length=1024)
    @Indexable(facet=true,name="Affiliation")
    public String affiliation;

    @Column(length=20)
    public String orcid; // http://orcid.org/

    @Column(length=1024)
    public String url;

    public Author () {}

    @Indexable(facet=true, suggest=true, name="Author")
    public String getName () { return lastname+", "+forename; }
}
