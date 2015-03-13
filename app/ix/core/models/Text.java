package ix.core.models;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("TXT")
public class Text extends Value {
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String text;

    public Text () {}
    public Text (String label) {
        super (label);
    }
    public Text (String label, String value) {
        super (label);
        text = value;
    }

    public String getText () { return text; }
    public void setText (String text) { this.text = text; }

    @Override
    public String getValue () { return text; }
}
