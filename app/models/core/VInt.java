package models.core;

import javax.persistence.*;

@Entity
@Table(name="ct_vint")
public class VInt extends Value {
    public Long value;
    public VInt () {}
    public VInt (String label, Property property, Long value) {
        super (label, property);
        this.value = value;
    }
}
