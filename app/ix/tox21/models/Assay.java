package ix.tox21.models;

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
import ix.utils.Global;

@Entity
@Table(name="ix_tox21_assay")
public class Assay extends EntityModel {
    public String name;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String description;

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_assay_synonym",
               joinColumns=@JoinColumn(name="ix_tox21_assay_synonym_id",
                                       referencedColumnName="id")
               )
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_assay_property")
    public List<Value> properties = new ArrayList<Value>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_assay_link")
    public List<XRef> links = new ArrayList<XRef>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_assay_publication")
    @JsonView(BeanViews.Full.class)
    public List<Publication> publications = new ArrayList<Publication>();
    
    public Assay () {}
    public Assay (String name) {
        this.name = name;
    }
    
    @Column(length=1024)
    @Indexable(suggest=true,facet=true, name="Assay")
    public String getName () { return name; }
    public String getDescription () { return description; }
    public List<Keyword> getSynonyms () { return synonyms; }
    public List<Value> getProperties () { return properties; }
    public List<XRef> getLinks () { return links; }
    public List<Publication> getPublications () { return publications; }
}
