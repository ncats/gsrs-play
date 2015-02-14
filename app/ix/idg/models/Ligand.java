package ix.idg.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.core.models.Publication;
import ix.core.models.BeanViews;
import ix.core.models.EntityModel;
import ix.ginas.models.Structure;
import ix.utils.Global;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO: this class should eventually be obsoleted in favor of
 * GInAS' substance!
 */
@Entity
@Table(name="ix_idg_ligand")
public class Ligand extends EntityModel {
    @Column(length=1024)
    @Indexable(suggest=true,name="Ligand")
    public String name;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String description;

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_ligand_synonym",
               joinColumns=@JoinColumn(name="ix_idg_ligand_synonym_id",
                                       referencedColumnName="id")
               )
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_ligand_property")
    public List<Value> properties = new ArrayList<Value>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_ligand_link")
    public List<XRef> links = new ArrayList<XRef>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_ligand_publication")
    @JsonView(BeanViews.Full.class)
    public List<Publication> publications = new ArrayList<Publication>();
    
    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_ligand_structure")
    public List<Structure> structures = new ArrayList<Structure>();
        
    public Ligand () {}
    public String getName () { return name; }
    public String getDescription () { return description; }
    public List<Keyword> getSynonyms () { return synonyms; }
    public List<Value> getProperties () { return properties; }
    public List<XRef> getLinks () { return links; }
    public List<Publication> getPublications () { return publications; }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_structures")
    public JsonNode getJsonStructures () {
        ObjectNode node = null;
        if (!structures.isEmpty()) {
            node = mapper.createObjectNode();
            node.put("count", structures.size());
            node.put("href", Global.getRef(getClass (), id)+"/structures");
        }
        return node;
    }
}
