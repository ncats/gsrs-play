package crosstalk.ncats.models;

import play.db.ebean.Model;
import javax.persistence.*;

import crosstalk.core.models.Indexable;

@Entity
@Table(name="ct_ncats_funding")
public class Funding extends Model {
    @Id
    public Long id; 

    @Indexable(facet=true, name="GrantFundingIC")
    public String ic;
    public Integer amount;

    public Funding () {}
    public Funding (String ic, Integer amount) {
        this.ic = ic;
        this.amount = amount;
    }
}
