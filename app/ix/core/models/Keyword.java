package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@DiscriminatorValue("KEY")
public class Keyword extends Value {
    @Column(length=255)
    @Indexable(facet=true, suggest=true, name="Keyword")
    public String term;

    public Keyword () {}
    public Keyword (String term) {
        this.term = term;
    }

    @Override
    public String getValue () { return term; }
}
