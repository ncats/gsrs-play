package ix.ginas.models;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.core.models.VStr;
import ix.core.models.Value;

@Entity
@Table(name="ix_ginas_substance")
@Inheritance
@DiscriminatorValue("SUB")
public class Substance extends GinasModel {
    public enum SubstanceClass {
	Chemical,
	Protein,
	NucleicAcid,
	Polymer,
	StructurallyDiverse,
	Mixture,
	SpecifiedSubstance,
	SpecifiedSubstanceG2,
	SpecifiedSubstanceG3,
	SpecifiedSubstanceG4,
	Virtual
    }
    
    @Indexable(name="Substance Class",facet=true)
    public SubstanceClass substanceClass;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_name")
    public List<Name> names = new ArrayList<Name>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_code")
    public List<VStr> codes = new ArrayList<VStr>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_property")
    public List<Value> properties = new ArrayList<Value>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_citation")
    public List<Citation> references = new ArrayList<Citation>();

    public Substance () {
    }
    public Substance (SubstanceClass clz) {
	substanceClass = clz;
    }
}
