package ix.idg.models;

import java.util.List;
import java.util.ArrayList;

import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.core.models.Publication;
import ix.core.models.BeanViews;
import ix.core.models.EntityModel;
import ix.utils.Global;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="ix_idg_target")
public class Target extends EntityModel {
    public static final String IDG_FAMILY = "IDG Target Family";
    public static final String IDG_CLASSIFICATION =
        "IDG Target Classification";
        
    @Column(length=1024)
    @Indexable(suggest=true,name="Target")
    public String name;

    @Lob
    public String description;

    @JsonView(BeanViews.Full.class)
    @OneToOne
    public Keyword organism;

    @Column(length=128)
    @Indexable(facet=true,name=IDG_FAMILY)
    public String idgFamily;

    @Column(length=10)
    @Indexable(facet=true,name=IDG_CLASSIFICATION)
    public String idgClass;

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_target_synonym",
               joinColumns=@JoinColumn(name="ix_idg_target_synonym_id",
                                       referencedColumnName="id")
               )
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_target_property")
    public List<Value> properties = new ArrayList<Value>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_target_link")
    public List<XRef> links = new ArrayList<XRef>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_target_publication")
    @JsonView(BeanViews.Full.class)
    public List<Publication> publications = new ArrayList<Publication>();
    

    public Target () {}
    public String getName () { return name; }
    public String getDescription () { return description; }
    public List<Keyword> getSynonyms () { return synonyms; }
    public List<Value> getProperties () { return properties; }
    public List<XRef> getLinks () { return links; }
    public List<Publication> getPublications () { return publications; }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_organism")
    public String getJsonOrganism () {
        return Global.getRef(organism);
    }
}
