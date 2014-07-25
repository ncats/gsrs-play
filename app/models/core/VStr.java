package models.core;

import javax.persistence.*;

@Entity
@Table(name="ct_vstr")
public class VStr extends Value {
    @Column(length=1024)
    public String value;
    public VStr () {}
    public VStr (String label, Property property, String value) {
        super (label, property);
        this.value = value;
    }
}
