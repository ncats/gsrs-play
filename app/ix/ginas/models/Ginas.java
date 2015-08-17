package ix.ginas.models;

import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.utils.Global;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import play.Logger;
import play.db.ebean.Model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@MappedSuperclass
public class Ginas extends Model {
    static public final String REFERENCE = "GInAS Reference";
    static public final String TAG = "GInAS Tag";
    
    @Id
    //@Column(name = "DATA_UUID")
    public UUID uuid;

    public final Date created = new Date ();
    public Date lastEdited;
    
    @OneToOne(cascade=CascadeType.ALL)
    @JsonSerialize(using = PrincipalSerializer.class)
    @JsonDeserialize(using = PrincipalDeserializer.class)
    public Principal lastEditedBy;
    
    //Where did this come from?
    public boolean deprecated;
    
    @ManyToMany(cascade=CascadeType.PERSIST)
    //@JoinColumn(name="id", referencedColumnName="uuid")
//    @ManyToMany//(cascade=CascadeType.ALL)
//    @JoinTable(name="ix_ginas_access",
//	    joinColumns=@JoinColumn
//	    (name="id", referencedColumnName="uuid")
//	)
    @JsonSerialize(using = PrincipalListSerializer.class)
    @JsonDeserialize(using = PrincipalListDeserializer.class)
    public List<Principal> access = new ArrayList<Principal>();
    
    public Ginas () {
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.lastEdited = new Date ();
    }

    @Indexable(indexed=false)
    public String getSelf () {
        if (uuid != null) {
            try {
                String ref = Global.getRef(this);
                if (ref != null)
                    return ref+"?view=full";
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Not a valid persistence Entity", ex);
            }
        }
        return null;
    }
}
