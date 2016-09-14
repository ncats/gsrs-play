package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.utils.JSONEntity;

@JSONEntity(title = "Property", isFinal = true)
@Entity
@Table(name = "ix_ginas_property")
public class Property extends CommonDataElementOfCollection {
	
	
    @JSONEntity(title = "Property Name", isRequired = true)
    @Column(nullable = false)
    private String name;

    @JSONEntity(title = "Value Type", values = "JSONConstants.ENUM_PROPERTY_TYPE", isRequired = true)
    private String type;

    @JSONEntity(title = "Property Type")
    private String propertyType;

    @JSONEntity(title = "Property Value")
    @OneToOne(cascade = CascadeType.ALL)
    private Amount value;
    
    //TP: added 05-19-2016
    //Needed for some properties
    @OneToOne(cascade=CascadeType.ALL)
    private SubstanceReference referencedSubstance;
    
    
    
    

    @JSONEntity(title = "Defining")
    private Boolean defining;

    @JSONEntity(title = "Parameters", format = "table")
    @OneToMany(mappedBy="owner",cascade = CascadeType.ALL)
    private List<Parameter> parameters = new ArrayList<Parameter>();

    public Property() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Amount getValue() {
        return value;
    }

    public void setValue(Amount value) {
        this.value = value;
    }

    public SubstanceReference getReferencedSubstance() {
        return referencedSubstance;
    }

    public void setReferencedSubstance(SubstanceReference referencedSubstance) {
        this.referencedSubstance = referencedSubstance;
    }

    public Boolean isDefining() {
        return defining;
    }

    public void setDefining(Boolean defining) {
        this.defining = defining;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
    
    public boolean equals(Object o){
    	if(!super.equals(o))return false;
    	Property p = (Property)o;
    	if(!(p.name+"").equals(this.name+""))return false;
    	if(p.parameters.size()!=this.parameters.size()){
    		return false;
    	}
    	if(!p.parameters.containsAll(this.parameters))return false;
    	return true;
    		
    	
    }
    
    
    
    
}
