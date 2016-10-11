package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.SingleParent;
import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_parameter")
@JSONEntity(title = "Parameter", isFinal = true)
@SingleParent
public class Parameter extends GinasCommonSubData {
	@ManyToOne(cascade = CascadeType.PERSIST)
	private Property owner;
	
	//TP: added 05-19-2016
    //Needed for some properties
	@OneToOne(cascade=CascadeType.ALL)
    private SubstanceReference referencedSubstance;
	
    @JSONEntity(title = "Parameter Name", isRequired = true)
    @Column(nullable=false)
    private String name;
    
    @JSONEntity(title = "Parameter Type", values = "JSONConstants.ENUM_PROPERTY_TYPE", isRequired = true)
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JSONEntity(title = "Parameter Value")
    @OneToOne(cascade=CascadeType.ALL)
    private Amount value;

    public Parameter () {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Amount getValue() {
        return value;
    }

    public void setValue(Amount value) {
        this.value = value;
    }

    public int hashCode(){
    	return (this.name+"").hashCode();
    }

    public boolean equals(Object o){
		if(!super.equals(o))return false;
    	if(o==null)return false;
    	if(o instanceof Parameter){
    		Parameter p =(Parameter)o;
    		if(!p.name.equals(this.name)){
    			return false;
    		}
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public String toString(){
    	return "Property=(" + getUuid() + ")  [" +  name + "]";
    }
}
