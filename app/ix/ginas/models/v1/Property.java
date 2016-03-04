package ix.ginas.models.v1;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ix.core.models.Keyword;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.KeywordListSerializer;
import ix.ginas.models.utils.JSONEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@JSONEntity(title = "Property", isFinal = true)
@Entity
@Table(name = "ix_ginas_property")
public class Property extends GinasCommonSubData {
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

    @JSONEntity(title = "Defining")
    private Boolean defining;

    @JSONEntity(title = "Parameters", format = "table")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ix_ginas_property_parameter")
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
}
