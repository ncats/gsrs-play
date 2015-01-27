package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@DiscriminatorValue("AUT")
public class Author extends Principal {
    public String lastname;
    public String forename;
    public String initials;
    public String prefname; // preferred name

    @Column(length=20)
    public String suffix; // Ph.D, M.D., etc.

    @Basic(fetch=FetchType.EAGER)
    @Indexable(facet=true,name="Affiliation")
    @Lob
    public String affiliation;
    public String orcid; // http://orcid.org/

    @ManyToOne(cascade=CascadeType.ALL)
    public Organization institution;

    public Author () {}
    public Author (String lastname, String firstname) {
        this.lastname = lastname;
        this.forename = forename;
    }

    @Indexable(facet=true, suggest=true, name="Author")
    public String getName () {
        return getName (true);
    }

    public String getName (boolean lastfirst) {
        if (prefname != null)
            return prefname;
        else if (lastname != null && forename != null)
            return lastfirst
                ? (lastname+", "+forename) : (forename+" "+lastname); 
        else if (lastname != null)
            return lastname;
        else if (forename != null)
            return forename;

        return null;
    }
}
