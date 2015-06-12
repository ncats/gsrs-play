package ix.ginas.models.v1;

import ix.core.models.BeanViews;
import ix.core.models.Structure;
import ix.ginas.models.utils.JSONEntity;
import ix.ncats.controllers.App;
import ix.utils.Global;
import ix.core.chem.Chem;
import ix.core.models.Indexable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("serial")
@JSONEntity(name = "chemicalSubstance", title = "Chemical Substance")
@Entity
@Inheritance
@DiscriminatorValue("CHE")
public class ChemicalSubstance extends Substance {
    @JSONEntity(isRequired = true)
    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    @JsonSerialize(using=StructureSerializer.class)
    public Structure structure;
    
    @JSONEntity(title = "Chemical Moieties", isRequired = true, minItems = 1)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_chemical_moiety")
    @JsonView(BeanViews.Full.class)
    public List<Moiety> moieties = new ArrayList<Moiety>();

    public ChemicalSubstance () {
        super (SubstanceClass.chemical);
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_moieties")
    public JsonNode getJsonMoieties () {
        JsonNode node = null;
        if (!moieties.isEmpty()) {
            try {
                ObjectNode n = mapper.createObjectNode();
                n.put("count", moieties.size());
                n.put("href", Global.getRef(getClass (), uuid)
                      +"/moieties");
                node = n;
            }
            catch (Exception ex) {
                // this means that the class doesn't have the NamedResource
                // annotation, so we can't resolve the context
                node = mapper.valueToTree(moieties);
            }
        }
        return node;
    }

    @Indexable(name="SubstanceStereoChemistry", facet=true)
    public Structure.Stereo getStereoChemistry () {
        return structure != null ? structure.stereoChemistry : null;
    }

    @Override
    public void save () {
        // now index the structure for searching
        try {
            Chem.setFormula(structure);
            structure.save();
            // it's bad to reference App from here!!!!
            App.strucIndexer.add(String.valueOf(structure.id),
                                 structure.molfile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (Moiety m : moieties)
            m.structure.save();
        super.save();
    }
}
