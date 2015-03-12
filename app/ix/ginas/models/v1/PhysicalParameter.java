package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_physical_parameter")
@JSONEntity(title = "Physical Parameter", isFinal = true)
public class PhysicalParameter extends Ginas {
    @JSONEntity(title = "Parameter Name", isRequired = true)
    public String parameterName;
    
    @OneToOne
    public Amount amount;

    public PhysicalParameter () {}
}
