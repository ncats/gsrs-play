package ix.core.models;

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

import ix.utils.Global;

@MappedSuperclass
public abstract class EntityModel extends IxModel {

    protected EntityModel () {}

    public abstract String getName ();
    public abstract String getDescription ();
    public abstract List<Keyword> getSynonyms ();
    public abstract List<Value> getProperties ();
    public abstract List<XRef> getLinks ();
    public abstract List<Publication> getPublications ();

    @Transient
    protected ObjectMapper mapper = new ObjectMapper ();

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_links")
    public JsonNode getJsonLinks () {
        ObjectNode node = null;
        if (!getLinks().isEmpty()) {
            node = mapper.createObjectNode();
            node.put("count", getLinks().size());
            node.put("href", Global.getRef(getClass (), id)+"/links");
        }
        return node;
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_properties")
    public JsonNode getJsonProperties () {
        ObjectNode node = null;
        if (!getProperties().isEmpty()) {
            node = mapper.createObjectNode();
            node.put("count", getProperties().size());
            node.put("href", Global.getRef(getClass (), id)+"/properties");
        }
        return node;
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_synonyms")
    public JsonNode getJsonSynonyms () {
        ObjectNode node = null;
        if (!getSynonyms().isEmpty()) {
            node = mapper.createObjectNode();
            node.put("count", getSynonyms().size());
            node.put("href", Global.getRef(getClass (), id)+"/synonyms");
        }
        return node;
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_publications")
    public JsonNode getJsonPublications () {
        ObjectNode node = null;
        if (!getPublications().isEmpty()) {
            node = mapper.createObjectNode();
            node.put("count", getPublications().size());
            node.put("href", Global.getRef(getClass (), id)+"/publications");
        }
        return node;
    }

    public String getSelf () {
        return Global.getRef(this)+"?view=full";
    }
}
