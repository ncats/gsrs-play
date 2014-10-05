package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_keyword")
public class Keyword extends Value {
    @Column(length=255)
    @Indexable(facet=true, suggest=true, name="Keyword")
    public String term;

    public Keyword () {}
    public Keyword (String term) {
        this.term = term;
    }
}
