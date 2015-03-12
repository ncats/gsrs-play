package ix.ginas.models;

import java.util.UUID;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import play.db.ebean.Model;
import ix.core.models.Indexable;
import ix.core.models.Principal;

@MappedSuperclass
public class Ginas extends Model {
    @Id
    public UUID uuid;

    public final Date created = new Date ();
    public Date lastModified;
    
    @OneToOne(cascade=CascadeType.ALL)
    public Principal lastEditedby;
    public boolean deprecated;

    public Ginas () {
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.lastModified = new Date ();
    }
}
