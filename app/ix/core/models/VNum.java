package ix.core.models;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("NUM")
public class VNum extends Value {
    public Double numval;

    public VNum () {}
    public VNum (String label, Double value) {
        super (label);
        numval = value;
    }

    @Override
    public Double getValue () { return numval; }
}
