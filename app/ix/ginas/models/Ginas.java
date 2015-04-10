package ix.ginas.models;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import play.db.ebean.Model;
import ix.core.models.Indexable;
import ix.core.models.Principal;

@MappedSuperclass
public class Ginas extends Model {
    static public final String REFERENCE = "GInAS Reference";
    
    @Id
    public UUID uuid;

    public final Date created = new Date ();
    public Date lastModified;
    
    @OneToOne(cascade=CascadeType.ALL)
    @JsonSerialize(using = PrincipalSerializer.class)
    @JsonDeserialize(using = PrincipalDeserializer.class)
    public Principal lastEditedBy;
    public boolean deprecated;
    
    public Ginas () {
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.lastModified = new Date ();
    }
}
