package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Structure;
import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;

@JSONEntity(name = "chemicalSubstance", title = "Chemical Substance")
@Entity
@Inheritance
@DiscriminatorValue("CHE")
public class ChemicalSubstance extends Substance {
    @JSONEntity(isRequired = true)
    @OneToOne
    @Column(nullable=false)
    @JsonSerialize(using=StructureSerializer.class)
    public Structure structure;
    
    @JSONEntity(title = "Chemical Moieties", isRequired = true, minItems = 1)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_chemical_moiety")
    public List<Moiety> moieties = new ArrayList<Moiety>();

    public ChemicalSubstance () {
        super (SubstanceClass.chemical);
    }
}
