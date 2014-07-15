package models.core;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
public class Keyword extends Model {
    @Id
    public Long id;

    @Column(length=255)
    public String term;

    public Keyword () {}
    public Keyword (String term) {
        this.term = term;
    }
}
