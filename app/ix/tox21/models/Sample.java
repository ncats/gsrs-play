package ix.tox21.models;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.utils.Global;
import ix.core.models.*;

@Entity
@Table(name="ix_tox21_sample")
public class Sample extends EntityModel {
    /**
     * Synonym labels
     */
    public static final String S_TOX21 = "Tox21";
    public static final String S_NCGC = "NCGC";
    public static final String S_CASRN = "CASRN";
    public static final String S_DSSTOX = "DSSTox";
    public static final String S_SID = "SID";
    public static final String S_CID = "CID";
    public static final String S_UNII = "UNII";
    public static final String S_InChIKey = "InChIKey";
    public static final String S_SYN = "SYNONYM"; // generic synonym

    public static final String P_SMILES_ISO = "SMILES_ISO";
    public static final String P_MOLFILE = "MOLFILE";

    @Indexable(facet=true,suggest=true,name="Sample")
    public String name;

    //@JsonView(BeanViews.Full.class)
    @OneToOne    
    public Structure structure;
    
    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_synonym",
               joinColumns=@JoinColumn(name="ix_tox21_sample_synonym_id",
                                       referencedColumnName="id")
               )
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_property")
    public List<Value> properties = new ArrayList<Value>();

    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_link")
    public List<XRef> links = new ArrayList<XRef>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_tox21_sample_publication")
    @JsonView(BeanViews.Full.class)
    public List<Publication> publications = new ArrayList<Publication>();

    public Sample () {}
    public Sample (String name) {
        this.name = name;
    }
    public String getName () { return name; }
    public String getDescription () { return null; }
    public List<Keyword> getSynonyms () { return synonyms; }
    public List<Value> getProperties () { return properties; }
    public List<XRef> getLinks () { return links; }
    public List<Publication> getPublications () { return publications; }

    public Keyword getSynonym (String label) {
        for (Keyword kw : synonyms) {
            if (label.equalsIgnoreCase(kw.label))
                return kw;
        }
        return null;
    }
    public Value getProperty (String label) {
        for (Value v : properties)
            if (label.equalsIgnoreCase(v.label))
                return v;
        return null;
    }
}
