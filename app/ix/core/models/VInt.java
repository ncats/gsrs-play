package ix.core.models;

import javax.persistence.*;

@Entity
@DiscriminatorValue("INT")
public class VInt extends Value {
    public Long vint;
    public VInt () {}
    public VInt (Long value) {
        vint = value;
    }
}
