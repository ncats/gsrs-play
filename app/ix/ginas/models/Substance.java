package ix.ginas.models;

import java.util.*;
import play.db.ebean.*;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.EntityModel;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.utils.Global;


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
        SpecifiedSubstanceG1,
        SpecifiedSubstanceG2,
        SpecifiedSubstanceG3,
        SpecifiedSubstanceG4,
        UnspecifiedSubstance,
        Virtual
    }
    
    @Indexable(name="Substance Class",facet=true)
    public SubstanceClass substanceClass;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_name")
    public List<Name> names = new ArrayList<Name>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_code")
    public List<Keyword> codes = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_citation")
    public List<Citation> references = new ArrayList<Citation>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_property")
    public List<Value> properties = new ArrayList<Value>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_structure_link")
    public List<XRef> links = new ArrayList<XRef>();
    
    public Substance () {
    }
    public Substance (SubstanceClass clz) {
        substanceClass = clz;
    }

    @Indexable(indexed=false)
    public String getSelf () {
        return Global.getRef(this)+"?view=full";
    }
}
