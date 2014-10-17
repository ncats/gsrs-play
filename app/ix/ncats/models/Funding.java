package ix.ncats.models;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;

@Entity
@Table(name="ix_ncats_funding")
public class Funding extends Model {
    @Id
    public Long id; 

    @Indexable(facet=true, name="Grant Funding IC")
    public String ic;
    public Integer amount;

    public Funding () {}
    public Funding (String ic, Integer amount) {
        this.ic = ic;
        this.amount = amount;
    }
}
