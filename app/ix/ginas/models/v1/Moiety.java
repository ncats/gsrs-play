package ix.ginas.models.v1;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.serialization.MoietyDeserializer;
import ix.ginas.models.utils.JSONEntity;


@JsonDeserialize(using = MoietyDeserializer.class)
@JSONEntity(name = "moiety", title = "Moiety")
@Entity
@Table(name = "ix_ginas_moiety")
public class Moiety extends CommonDataElementOfCollection {
	
	
    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    @JsonUnwrapped
    public GinasChemicalStructure structure;
    
    @JSONEntity(title = "Count")
    @OneToOne(cascade=CascadeType.ALL)
    private Amount count;

    public Moiety () {}
    
    @Column(unique=true)
    @JsonIgnore
    public String innerUuid;
    
    @PrePersist
    @PreUpdate
    public void enforce(){
    	if(structure.id==null){
    		structure.id=UUID.randomUUID();
    	}
    	this.innerUuid=structure.id.toString();
    }
    
    public void setCount(Integer i){
    	count=intToAmount(i);
    }
    public Integer getCount(){
    	if(count==null)return null;
    	if(count.average == null)return null;
    	return (int) count.average.longValue();
    }
    public static Amount intToAmount(Integer i){
    	Amount count=new Amount();
    	if(i!=null){
    		count.average=i.doubleValue();
    	}
    	count.type="MOL RATIO";
    	count.units="MOL RATIO";
    	return count;
    }

	public void setCountAmount(Amount amnt) {
		count=amnt;	
	}
	
	public Amount getCountAmount() {
		return count;
	}
	
	
	public UUID getUUID(){
		return UUID.fromString(this.innerUuid);
	}
	
	@JsonProperty("id")
	public String getId(){
		return null;
	}
}