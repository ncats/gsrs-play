package crosstalk.core.models;

import javax.persistence.*;

@Entity
@Table(name="ct_core_vstr")
public class VStr extends Value {
    @Column(length=1024)
    public String value;
    public VStr () {}
    public VStr (Property property, String value) {
        super (property);
        this.value = value;
    }
}
