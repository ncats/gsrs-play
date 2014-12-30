package ix.core.models;

import java.util.Date;
import javax.persistence.*;
import play.db.ebean.Model;

@MappedSuperclass
public class IxModel extends Model {
    public final Date created = new Date ();
    public Date modified;

    @PrePersist    
    @PreUpdate
    public void modified () {
        this.modified = new Date ();
    }

    public IxModel () {}
}
