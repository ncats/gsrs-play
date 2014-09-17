package crosstalk.core.models;

import javax.persistence.*;

@Entity
@Table(name="ct_core_vnum")
public class VNum extends Value {
    public Double value;
    public VNum () {}
    public VNum (Property property, Double value) {
        super (property);
        this.value = value;
    }
}
