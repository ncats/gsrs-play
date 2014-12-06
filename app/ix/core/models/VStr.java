package ix.core.models;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("STR")
public class VStr extends Value {
    @Column(length=1024)
    public String strval;

    public VStr () {}
    public VStr (String label) {
        super (label);
    }
    public VStr (String label, String value) {
        super (label);
        strval = value;
    }

    @Override
    public String getValue () { return strval; }
}
