package ix.core.models;

import javax.persistence.*;

@Entity
@DiscriminatorValue("STR")
public class VStr extends Value {
    @Column(length=1024)
    public String vstr;
    public VStr () {}
    public VStr (String value) {
        vstr = value;
    }
}
