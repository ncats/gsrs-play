package ix.ginas.models.v1;

import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonDeserialize(using = MoietyDeserializer.class)
@JsonSerialize(using = MoietySerializer.class)
@JSONEntity(name = "moiety", title = "Moiety")
@Entity
@Table(name = "ix_ginas_moiety")
public class Moiety extends GinasCommonSubData {
	
    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    public GinasChemicalStructure structure;
    
    @JSONEntity(title = "Count")
    public Integer count;

    public Moiety () {}
    
    @Column(unique=true)
    public String innerUuid;
    
    @PrePersist
    @PreUpdate
    public void enforce(){
    	if(structure.id==null){
    		structure.id=UUID.randomUUID();
    	}
    	this.innerUuid=structure.id.toString();
    }
    
    
    
    
}