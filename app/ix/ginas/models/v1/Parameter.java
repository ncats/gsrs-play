package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasSubData;

@Entity
@Table(name="ix_ginas_parameter")
@JSONEntity(title = "Parameter", isFinal = true)
public class Parameter extends GinasSubData {
    @JSONEntity(title = "Parameter Name", isRequired = true)
    @Column(nullable=false)
    public String name;
    
    @JSONEntity(title = "Parameter Type", values = "JSONConstants.ENUM_PROPERTY_TYPE", isRequired = true)
    public String type;
    
    @JSONEntity(title = "Parameter Value")
    @OneToOne(cascade=CascadeType.ALL)
    public Amount value;

    public Parameter () {}
    
    public String toString(){
    	return name + "," + type + "," + value.toString();
    }
}
