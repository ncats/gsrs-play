package ix.ginas.models;

import ix.core.models.Group;
import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.utils.Global;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import play.Logger;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@MappedSuperclass
public class Ginas extends Model {
    static public final String REFERENCE = "GInAS Reference";
    static public final String TAG = "GInAS Tag";
    
    @Id
    //@Column(name = "DATA_UUID")
    public UUID uuid;

    
    //TP: why is this final?
    public final Date created = new Date ();
    
    @Indexable(facet = true, name = "Last Edited Date")
    public Date lastEdited;
    
    //TP: why is this one-to-one?
    @OneToOne(cascade=CascadeType.ALL)
    @JsonSerialize(using = PrincipalSerializer.class)
    @JsonDeserialize(using = PrincipalDeserializer.class)
    @Indexable(facet = true, name = "Last Edited By")
    public Principal lastEditedBy;
    
    //Where did this come from?
    public boolean deprecated;
    
    @ManyToMany(cascade=CascadeType.PERSIST)
//    @ManyToMany//(cascade=CascadeType.ALL)
//    @JoinTable(name="ix_ginas_access",
//          joinColumns=@JoinColumn
//          (name="id", referencedColumnName="uuid")
//      )
    @JsonSerialize(using = GroupListSerializer.class)
    @JsonDeserialize(using = GroupListDeserializer.class)
    public List<Group> access = new ArrayList<Group>();
    
    public Ginas () {
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.lastEdited = new Date ();
    }

    @JsonProperty("_self")
    @Indexable(indexed=false)
    public String getself () {
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
    
    @JsonIgnore
    public UUID getOrGenerateUUID(){
    	if(uuid!=null)return uuid;
    	this.uuid=UUID.randomUUID();
    	return uuid;
    }
}
