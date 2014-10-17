package ix.core.models;

import javax.persistence.*;

@Entity
@Table(name="ix_core_vint")
public class VInt extends Value {
    public Long value;
    public VInt () {}
    public VInt (Long value) {
        this.value = value;
    }
}
