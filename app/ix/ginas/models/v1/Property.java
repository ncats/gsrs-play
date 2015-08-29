package ix.ginas.models.v1;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ix.core.models.Keyword;
import ix.ginas.models.Ginas;
import ix.ginas.models.KeywordListSerializer;
import ix.ginas.models.utils.JSONEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@JSONEntity(title = "Property", isFinal = true)
@Entity
@Table(name = "ix_ginas_property")
public class Property extends Ginas {
    @JSONEntity(title = "Property Name", isRequired = true)
    @Column(nullable = false)
    public String name;

    @JSONEntity(title = "Value Type", values = "JSONConstants.ENUM_PROPERTY_TYPE", isRequired = true)
    public String type;

    @JSONEntity(title = "Property Type")
    public String propertyType;

    @JSONEntity(title = "Property Value")
    @OneToOne(cascade = CascadeType.ALL)
    public Amount value;

    @JSONEntity(title = "Defining")
    public Boolean defining;

    @JSONEntity(title = "Parameters", format = "table")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ix_ginas_property_parameter")
    public List<Parameter> parameters = new ArrayList<Parameter>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ix_ginas_property_reference")
    @JsonSerialize(using = KeywordListSerializer.class)
    public List<Keyword> references = new ArrayList<Keyword>();

    public Property() {
    }
}
