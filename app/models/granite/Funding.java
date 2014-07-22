package models.granite;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_granite_funding")
public class Funding extends Model {
    @Id
    public Long id; 

    public String ic;
    public Integer amount;

    public Funding () {}
    public Funding (String ic, Integer amount) {
        this.ic = ic;
        this.amount = amount;
    }
}
