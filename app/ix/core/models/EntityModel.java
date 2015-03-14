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
        JsonNode node = null;
        if (!getLinks().isEmpty()) {
            try {
                ObjectNode n = mapper.createObjectNode();
                n.put("count", getLinks().size());
                n.put("href", Global.getRef(getClass (), id)+"/links");
                node = n;
            }
            catch (Exception ex) {
                node = mapper.valueToTree(getLinks ());
            }
        }
        return node;
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_properties")
    public JsonNode getJsonProperties () {
        JsonNode node = null;
        if (!getProperties().isEmpty()) {
            try {
                ObjectNode n = mapper.createObjectNode();
                n.put("count", getProperties().size());
                n.put("href", Global.getRef(getClass (), id)+"/properties");
                node = n;
            }
            catch (Exception ex) {
                // this means that the class doesn't have the NamedResource
                // annotation, so we can't resolve the context
                node = mapper.valueToTree(getProperties ());
            }
        }
        return node;
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_synonyms")
    public JsonNode getJsonSynonyms () {
        JsonNode node = null;
        if (!getSynonyms().isEmpty()) {
            try {
                ObjectNode n = mapper.createObjectNode();
                n.put("count", getSynonyms().size());
                n.put("href", Global.getRef(getClass (), id)+"/synonyms");
                node = n;
            }
            catch (Exception ex) {
                node = mapper.valueToTree(getSynonyms ());
            }
        }
        return node;
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_publications")
    public JsonNode getJsonPublications () {
        JsonNode node = null;
        if (!getPublications().isEmpty()) {
            try {
                ObjectNode n = mapper.createObjectNode();
                n.put("count", getPublications().size());
                n.put("href", Global.getRef(getClass (), id)+"/publications");
                node = n;
            }
            catch (Exception ex) {
                node = mapper.valueToTree(getPublications ());
            }
        }
        return node;
    }

    @Indexable(indexed=false)
    public String getSelf () {
        return Global.getRef(this)+"?view=full";
    }
}
