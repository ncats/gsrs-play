package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_parameter")
@JSONEntity(title = "Parameter", isFinal = true)
public class Parameter extends GinasCommonSubData {
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


    
/*    public String toString(){
    	return name + "," + type + "," + value.toString();
    }*/
}
