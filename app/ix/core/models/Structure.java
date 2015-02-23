package ix.core.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.utils.Global;

@MappedSuperclass
@Entity
@Table(name="ix_core_structure")
public class Structure extends IxModel {
    public static final String LyChI_HASH_L1 = "LyChI Hash L1";
    public static final String LyChI_HASH_L2 = "LyChI Hash L2";
    public static final String LyChI_HASH_L3 = "LyChI Hash L3";
    public static final String LyChI_HASH_L4 = "LyChI Hash L4";    
    public static final String LyChI_SMILES = "LyChI SMILES";
    public static final String LyChI_MOLFILE = "LyChI MolFile";
    
    public static final String InChI = "InChI";
    public static final String InChI_KEY = "InChI Key";
    public static final String FORMAT_MDL = "Format MDL";
    public static final String FORMAT_SMILES = "Format SMILES";
    public static final String FORMAT_MRV = "Format MRV";
    public static final String PATH_FINGERPRINT = "Path Fingerprint";

    // stereochemistry
    public enum Stereo {
        ABSOLUTE,
        ACHIRAL,
        RACEMIC,
        MIXED,
        EPIMERIC,
        UNKNOWN
        ;
    }

    // optical activity
    public enum Optical {
        PLUS, // (+)
        MINUS, // (-)
        PLUS_MINUS, // (+/-)
        UNSPECIFIED,
        UNKNOWN
        ;
    }
    
    @Column(length=128)
    public String digest; // digest checksum of the original structure
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String molfile;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String smiles;
    
    @Indexable(name="Molecular Formla", facet=true)
    public String formula;
    @JsonProperty("stereochemistry")
    public Stereo stereoChemistry;
    public Optical opticalActivity;
    public boolean atropisomerism;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String stereoComments;
    
    @Indexable(name="Stereocenters", ranges={0,1,2,5,7,10})
    public Integer stereoCenters; // count of possible stereocenters
    
    @Indexable(name="Defined Stereocenters",ranges={0,1,2,5,7,10})
    public Integer definedStereo; // count of defined stereocenters
    
    public Integer ezCenters; // counter of E/Z centers
    public Integer charge; // formal charge
    @Indexable(name="Molecular Weight",
               dranges={0,200,400,600,800}, format="%1$.0f")
    public Double mwt; // molecular weight

    @ManyToMany(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)    
    @JoinTable(name="ix_ginas_structure_property")
    public List<Value> properties = new ArrayList<Value>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    @JoinTable(name="ix_idg_structure_link")
    public List<XRef> links = new ArrayList<XRef>();
    
    @Transient
    private ObjectMapper mapper = new ObjectMapper ();
    
    public Structure () {}

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

    public String getSelf () {
        return Global.getRef(this)+"?view=full";
    }
}
