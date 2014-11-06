package ix.core.models;

import javax.persistence.*;

import ix.utils.Global;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@DiscriminatorValue("THU")
public class Thumbnail extends Figure {
    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    public Figure parent;

    public Thumbnail () {}

    @JsonProperty("parent_xref")
    public String getParentXRef () {
        if (parent != null) {
            return Global.getRef(parent);
        }
        return null;
    }
    public boolean isThumbnail () { return true; }
}
