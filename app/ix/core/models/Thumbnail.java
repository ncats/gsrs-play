package ix.core.models;

import javax.persistence.*;

import ix.utils.Global;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("THU")
public class Thumbnail extends Figure {
    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    public Figure parent;

    public Thumbnail () {}

    public String getXRef () {
        if (parent != null) {
            return Global.getRef(parent);
        }
        return null;
    }
    public boolean isThumbnail () { return true; }
}
