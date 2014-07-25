package models.core;

import javax.persistence.*;

@Entity
@Table(name="ct_vnum")
public class VNum extends Value {
    public Double value;
    public VNum () {}
    public VNum (String label, Property property, Double value) {
        super (label, property);
        this.value = value;
    }
}
