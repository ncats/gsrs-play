package ix.idg.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ix.core.models.Indexable;
import ix.core.models.Value;
import ix.core.models.Keyword;
import ix.core.models.Publication;
import ix.core.models.XRef;
import ix.core.models.BeanViews;
import ix.utils.Global;

@Entity
@Table(name="ix_idg_entity")
@Inheritance
@DiscriminatorValue("ENT")
public class EntityModel extends Model {
    @Id
    public Long id;

    @Column(length=1024)
    @Indexable(suggest=true,name="Entity")
    public String name;

    @Lob
    public String description;

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_synonym",
	       joinColumns=@JoinColumn(name="ix_idg_entity_synonym_id",
				       referencedColumnName="id")
	       )
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_property")
    public List<Value> properties = new ArrayList<Value>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_link")
    public List<XRef> links = new ArrayList<XRef>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_entity_publication")
    @JsonView(BeanViews.Full.class)
    public List<Publication> publications = new ArrayList<Publication>();

    public EntityModel () {}

    @Transient
    protected ObjectMapper mapper = new ObjectMapper ();

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_links")
    public JsonNode getJsonLinks () {
	ObjectNode node = null;
	if (!links.isEmpty()) {
	    node = mapper.createObjectNode();
	    node.put("count", links.size());
	    node.put("href", Global.getRef(getClass (), id)+"/links");
	}
	return node;
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_properties")
    public JsonNode getJsonProperties () {
	ObjectNode node = null;
	if (!properties.isEmpty()) {
	    node = mapper.createObjectNode();
	    node.put("count", properties.size());
	    node.put("href", Global.getRef(getClass (), id)+"/properties");
	}
	return node;
    }

    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_synonyms")
    public JsonNode getJsonSynonyms () {
	ObjectNode node = null;
	if (!synonyms.isEmpty()) {
	    node = mapper.createObjectNode();
	    node.put("count", synonyms.size());
	    node.put("href", Global.getRef(getClass (), id)+"/synonyms");
	}
	return node;
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_publications")
    public JsonNode getJsonPublications () {
	ObjectNode node = null;
	if (!publications.isEmpty()) {
	    node = mapper.createObjectNode();
	    node.put("count", publications.size());
	    node.put("href", Global.getRef(getClass (), id)+"/publications");
	}
	return node;
    }

    public String getSelf () {
	return Global.getRef(this)+"?view=full";
    }
}
