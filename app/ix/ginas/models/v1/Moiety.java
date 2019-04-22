package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.serialization.MoietyDeserializer;
import ix.ginas.models.utils.JSONEntity;


@JsonDeserialize(using = MoietyDeserializer.class)
@JSONEntity(name = "moiety", title = "Moiety")
@Entity
@Table(name = "ix_ginas_moiety")
//@JsonIgnoreProperties({ "id" })
public class Moiety extends CommonDataElementOfCollection implements Comparable<Moiety>{
	public static String JSON_NULL="JSON_NULL";
	
    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    @JsonUnwrapped //TODO: Probably not covered well 
                   // by some other tools
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
	
	
	@Id
	public UUID getUUID(){
		if(this.innerUuid!=null){
			return UUID.fromString(this.innerUuid);
		}else{
			return null;
		}
	}
	
	@Override
	public void forceUpdate() {
		structure.forceUpdate();
		super.forceUpdate();
	}
	
	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		temp.addAll(structure.getAllChildrenAndSelfCapableOfHavingReferences());
		if(count!=null){
			temp.addAll(count.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}

	@Override
	public int compareTo(Moiety o) {
		if(innerUuid ==null){
			if(o.innerUuid ==null) {
				return 0;
			}
			return 1;
		}
		if(o.innerUuid ==null){
			return -1;
		}
		return innerUuid.compareTo(o.innerUuid);
	}
}