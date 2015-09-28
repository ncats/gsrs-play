package ix.idg.models;

import java.util.List;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        "IDG Development Level";

    public enum TDL {
        Tclin ("Tclin",
               "These targets have activities in DrugDB (i.e., approved drugs) with known mechanism of action that satisfy the activity thresholds detailed below.",
               "primary"),
        Tchem ("Tchem",
               "These targets have activities in ChEMBL that satisfy the activity thresholds detailed below. In some cases, targets have been manually migrated to Tchem by human curation based on small molecule activities from other sources.",
               "info"),
        Tbio ("Tbio",
"These targets do not have known drug or small molecule activities that satisfy the activity thresholds detailed below AND satisfy one or more of the following criteria:"+
"<ul>"+
"<li><p align='left'>target is above the cutoff criteria for Tdark"+
"<li><p align='left'>target is annotated with a Gene Ontology Molecular Function or Biological Process leaf term(s) with an Experimental Evidence code"+
"<li><p align='left'>target has confirmed OMIM phenotype(s)"+
"<ul>",
                "warning"),
        Tdark ("Tdark",
"These are targets about which virtually nothing is known. They do not have known drug or small molecule activities that satisfy the activity thresholds detailed below AND satisfy two or more of the following criteria:"+
"<ul><li><p align='left'>A PubMed text-mining score from Jensen Lab &lt; 5"+
"<li><p align='left'>&le; 3 Gene RIFs"+
"<li><p align='left'>&le; 50 Antibodies available according to http://antibodypedia.com</ul>","danger");

        final public String name;
        final public String desc;
        final public String label;

        TDL (String name, String desc, String label) {
            this.name = name;
            this.desc = desc;
            this.label = label;
        }
        public String toString () { return name; }
        public static TDL fromString (String s) {
            for (TDL t : EnumSet.allOf(TDL.class)) {
                if (t.name.equals(s))
                    return t;
            }
            return null;
        }
    }
        
    @Column(length=1024)
    @Indexable(suggest=true,name="Target")
    public String name;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String description;

    @JsonView(BeanViews.Full.class)
    @OneToOne(cascade=CascadeType.ALL)
    public Keyword organism;

    @Column(length=128)
    @Indexable(facet=true,name=IDG_FAMILY)
    public String idgFamily;

    @Column(length=10)
    @Indexable(facet=true,name=IDG_DEVELOPMENT)
    public TDL idgTDL; // target development level

    @Indexable(sortable=true,name="Novelty")
    public Double novelty;

    @Indexable(sortable=true,name="Antibody Count",
               ranges={0,40,80,120,160,200,300,400,500,600})
    public Integer antibodyCount;
    
    @Indexable(sortable=true,name="Monoclonal Count",
               ranges={0,10,20,30,40,50,60,70,80,100})
    public Integer monoclonalCount;

    @Indexable(sortable=true,name="PubMed Count",
               ranges={0,20,40,80,160,320,640,1280})
    public Integer pubmedCount;

    @Indexable(sortable=true,name="Patent Count",
               ranges={0,20,40,80,160,320,640,1280,2540,5120,10240})
    public Integer patentCount;

    @Indexable(sortable=true,name="Grant Count",
               ranges={0,50,100,150,200,300,500,1000,2000,3000})
    public Integer grantCount;

    @Indexable(sortable=true,name="Grant Total Cost")
    public Double grantTotalCost;

    @Indexable(sortable=true,name="R01 Count")
    public Integer r01Count;

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

    @PostLoad
    public void _sortPublications () {
        Collections.sort(publications, new Comparator<Publication> () {
                public int compare (Publication p1, Publication p2) {
                    if (p2.journal == null
                        || p1.journal == null
                        || p2.journal.year == null
                        || p1.journal.year == null)
                        return (int)(p2.pmid - p1.pmid);
                    
                    int d = p2.journal.year - p1.journal.year;
                    if (d == 0)
                        d = (int)(p2.pmid - p1.pmid);
                    return d;
                }
            });
    }
}
