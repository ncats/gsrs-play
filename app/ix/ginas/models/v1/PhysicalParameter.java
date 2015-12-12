package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_physicalpar")
@JSONEntity(title = "Physical Parameter", isFinal = true)
public class PhysicalParameter extends GinasCommonSubData {
    @JSONEntity(title = "Parameter Name", isRequired = true)
    public String parameterName;
    
    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;

    public PhysicalParameter () {}
    
    public String toString(){
        return parameterName + "," + amount.toString();
    }
}
