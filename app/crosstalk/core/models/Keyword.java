package crosstalk.core.models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_keyword")
public class Keyword extends Value {
    @Id
    public Long id;

    @Column(length=255)
    public String term;

    public Keyword () {}
    public Keyword (String term) {
        this.term = term;
    }
    public Keyword (Property property, String term) {
        super (property);
        this.term = term;
    }
}
