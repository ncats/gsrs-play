package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@DiscriminatorValue("AUT")
public class Author extends Principal {
    public String lastname;
    public String forename;
    public String initials;

    @Column(length=20)
    public String suffix; // Ph.D, M.D., etc.

    @Column(length=1024)
    @Indexable(facet=true,name="Affiliation")
    public String affiliation;

    @Column(length=20)
    public String orcid; // http://orcid.org/

    @Column(length=1024)
    public String url;

    public Author () {}
    public Author (String lastname, String firstname) {
        this.lastname = lastname;
        this.forename = forename;
    }

    @Indexable(facet=true, suggest=true, name="Author")
    public String getName () { return lastname+", "+forename; }
}
