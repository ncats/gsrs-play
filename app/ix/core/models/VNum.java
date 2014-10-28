package ix.core.models;

import javax.persistence.*;

@Entity
@DiscriminatorValue("NUM")
public class VNum extends Value {
    public Double vnum;
    public VNum () {}
    public VNum (Double value) {
        vnum = value;
    }
}
