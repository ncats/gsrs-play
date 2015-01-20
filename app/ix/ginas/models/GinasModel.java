package ix.ginas.models;

import java.util.UUID;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import play.db.ebean.Model;

@MappedSuperclass
public class GinasModel extends Model {
    @Id
    public UUID uuid;

    public final Date created = new Date ();
    public Date modified;
    public boolean deprecated;

    public GinasModel () {
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.modified = new Date ();
    }
}
