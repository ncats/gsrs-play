package ix.ginas.models.v1;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.core.models.Value;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@JSONEntity(title = "Property", isFinal = true)
@Entity
@Table(name="ix_ginas_property")
public class Property extends Ginas {
    @JSONEntity(title = "Property Name", isRequired = true)
    @Column(nullable=false)
    public String name;
    
    @JSONEntity(title = "Value Type", values = "JSONConstants.ENUM_PROPERTY_TYPE", isRequired = true)
    public String type;

    @JSONEntity(title = "Property Type")
    public String propertyType;
    
    @JSONEntity(title = "Property Value")
    @OneToOne(cascade=CascadeType.ALL)
    public Amount value;
    
    @JSONEntity(title = "Defining")
    public Boolean defining;
    
    @JSONEntity(title = "Parameters", format = "table")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_property_parameter")
    public List<Parameter> parameters = new ArrayList<Parameter>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_property_reference")
    @JsonSerialize(using=ReferenceListSerializer.class)
    @JsonDeserialize(using=ReferenceListDeserializer.class)
    public List<Value> references = new ArrayList<Value>();
    
    public Property () {}
}
