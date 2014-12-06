package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@DiscriminatorValue("RNG")
public class VRange extends Value {
    public Double lval;
    public Double rval;
    public Double average;

    public VRange () {}
    public VRange (String label, Double lval, Double rval) {
        super (label);
        this.lval = lval;
        this.rval = rval;
    }
}
