package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.Indexable;
import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.GinasAccessReferenceControlled;
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

    @Indexable(name="name")
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

    @Indexable(name="propertyType")
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @Indexable(name="value")
    public Amount getValue() {
        return value;
    }

    public void setValue(Amount value) {
        this.value = value;
    }

    @Indexable(name="referencedSubstance")
    public SubstanceReference getReferencedSubstance() {
        return referencedSubstance;
    }

    public void setReferencedSubstance(SubstanceReference referencedSubstance) {
        this.referencedSubstance = referencedSubstance;
    }

    @Indexable(name="defining")
    public boolean isDefining() {
        return (defining == null) ? false : defining;
    }

    public void setDefining(boolean defining) {
        this.defining = defining;
    }

    @Indexable(name="parameters")
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
    
    @JsonIgnore
	@Override
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {

		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		if(this.value!=null){
			temp.addAll(this.value.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		if(this.parameters!=null){
			for(Parameter p : this.parameters){
				temp.addAll(p.getAllChildrenAndSelfCapableOfHavingReferences());
			}
		}
		if(this.referencedSubstance!=null){
			temp.addAll(this.referencedSubstance.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", value=" + value +
                ", referencedSubstance=" + referencedSubstance +
                ", defining=" + defining +
                ", parameters=" + parameters +
                "} " + super.toString();
    }
}
