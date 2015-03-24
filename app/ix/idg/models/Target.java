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
    public static final String IDG_DEVELOPMENT =
        "IDG Target Development";

    public enum TDL {
        Tclin_p("Tclin+",
                "Targets have activities better than 1μM (10μM for ion channels) in DrugDB involving an approved drug WITH a known mechanism of action",
                "success"),
        Tclin ("Tclin",
               "Targets have activities better than 1μM (10μM for ion channels) in DrugDB involving an approved drug",
               "primary"),
        Tchem ("Tchem",
               "Targets have standardizable activities better than 1μM (10μM for ion channels) in ChEMBL involving non-drug small molecule(s)",
               "info"),
        Tmacro ("Tmacro",
                "Targets do not have ChEMBL activities and are above the cutoffs for Tgray",
                "warning"),
        Tgray ("Tgray",
               "Targets are above the cutoffs for Tdark and have at least 2 of the following:"
+"<ul>"
+"    <li><p align='left'>&le; 5 Gene RIFs</p></li>"
+"    <li><p align='left'>&le; 84 Antibodies available according to http://antibodypedia.com</p></li>"
+"    <li><p align='left'>A PubMed text-mining score from Jensen Lab of &le; 10.55</p></li>"
+"</ul>", "default"),
        Tdark ("Tdark",
               "Targets have at least 2 of the following:"
+"<ul>"
+"    <li><p align='left'>&le; 1 Gene RIFs</p></li>"
+"    <li><p align='left'>&le; 38 Antibodies available according to http://antibodypedia.com</p></li>"
+"    <li><p align='left'>A PubMed text-mining score from Jensen Lab of &le; 1.23</p></li>"
+"</ul>", "danger");

        final public String name;
        final public String desc;
        final public String label;

        TDL (String name, String desc, String label) {
            this.name = name;
            this.desc = desc;
            this.label = label;
        }
        public String toString () { return name; }
    }
        
    @Column(length=1024)
    @Indexable(suggest=true,name="Target")
    public String name;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String description;

    @JsonView(BeanViews.Full.class)
    @OneToOne
    public Keyword organism;

    @Column(length=128)
    @Indexable(facet=true,name=IDG_FAMILY)
    public String idgFamily;

    @Column(length=10)
    @Indexable(facet=true,name=IDG_DEVELOPMENT)
    public TDL idgTDL; // target development level

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

    /**
     * return the first synonym that matches the given label
     */
    public Keyword getSynonym (String label) {
        for (Keyword kw : synonyms) {
            if (label.equals(kw.label))
                return kw;
        }
        return null;
    }
}
