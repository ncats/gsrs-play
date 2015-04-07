package ix.ginas.models.v1;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.core.models.Keyword;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@JSONEntity(name = "substance", title = "Substance")
@Entity
@Table(name="ix_ginas_substance")
@Inheritance
@DiscriminatorValue("SUB")
public class Substance extends Ginas {
    /**
     * sigh.. can we be at least case-consistent?
     */
    public enum SubstanceClass {
        chemical,
        protein,
        nucleicacid,
        polymer,
        structurallyDiverse,
        mixture,
        specifiedSubstanceG1,
        specifiedSubstanceG2,
        specifiedSubstanceG3,
        specifiedSubstanceG4,
        unspecifiedSubstance,   
        VIRTUAL
    }
    
    @JSONEntity(title = "Substance Type", values = "JSONConstants.ENUM_SUBSTANCETYPES", isRequired = true)
    public SubstanceClass substanceClass;
    
    @JSONEntity(title = "Names", minItems = 1, isRequired = true)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_name")
    public List<Name> names = new ArrayList<Name>();
    
    // TOOD original schema has superfluous name = codes in the schema here and
    // in all of Code's properties
    @JSONEntity(title = "Codes")
    @JoinTable(name="ix_ginas_substance_code")
    public List<Code> codes = new ArrayList<Code>();

    @OneToOne
    public Modifications modifications;
    
    @JSONEntity(title = "Notes")
    @JoinTable(name="ix_ginas_substance_note")
    public List<Note> notes = new ArrayList<Note>();
    
    @JSONEntity(title = "Properties")
    @JoinTable(name="ix_ginas_substance_property")
    public List<Property> properties = new ArrayList<Property>();
    
    @JSONEntity(title = "Relationships")
    @JoinTable(name="ix_ginas_substance_relationship")
    public List<Relationship> relationships = new ArrayList<Relationship>();
    
    @JSONEntity(title = "References", minItems = 1, isRequired = true)
    @JoinTable(name="ix_ginas_substance_reference")
    public List<Reference> references = new ArrayList<Reference>();
    
    @JSONEntity(title = "Approval ID", isReadOnly = true)
    @Column(length=10)
    public String unii;
    
    // TODO in original schema, this field is missing its items: String
    @JSONEntity(title = "Tags", format = "table", isUniqueItems = true)
    @JoinTable(name="ix_ginas_substance_tag")
    public List<Keyword> tags = new ArrayList<Keyword>();

    public Substance () {
        this (SubstanceClass.VIRTUAL);
    }
    public Substance (SubstanceClass subcls) {
        substanceClass = subcls;
    }
}
