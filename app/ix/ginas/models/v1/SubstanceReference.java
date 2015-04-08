package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_substance_reference")
@JSONEntity(name = "substanceReference", isFinal = true)
public class SubstanceReference extends Ginas {
    @JSONEntity(title = "Substance Name")
    public String refPname;
    
    @JSONEntity(isRequired = true)
    @Column(nullable=false, length=128)
    public String refuuid;
    
    @JSONEntity(values = "JSONConstants.ENUM_REFERENCE")
    public String substanceClass;
    
    @Column(length=32)
    public String approvalID;

    public SubstanceReference () {}
}
