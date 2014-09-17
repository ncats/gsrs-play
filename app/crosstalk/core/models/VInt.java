package crosstalk.core.models;

import javax.persistence.*;

@Entity
@Table(name="ct_core_vint")
public class VInt extends Value {
    public Long value;
    public VInt () {}
    public VInt (Property property, Long value) {
        super (property);
        this.value = value;
    }
}
