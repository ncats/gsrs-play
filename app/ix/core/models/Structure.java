package ix.core.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.utils.Global;

@MappedSuperclass
@Entity
@Table(name="ix_core_structure")
public class Structure extends IxModel {
    /**
     * Property labels
     */
    public static final String F_InChI = "InChI";
    public static final String F_MDL = "MDL";
    public static final String F_SMILES = "SMILES";
    public static final String F_MRV = "MRV";
    public static final String F_LyChI_SMILES = "LyChI_SMILES";
    public static final String H_LyChI_L1 = "LyChI_L1";
    public static final String H_LyChI_L2 = "LyChI_L2";
    public static final String H_LyChI_L3 = "LyChI_L3";
    public static final String H_LyChI_L4 = "LyChI_L4";    
    public static final String H_InChI_Key = "InChI_Key";
        
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
        PLUS ("( + )"),
        MINUS ("( - )"),
        PLUS_MINUS ("( + / - )"),
        UNSPECIFIED ("unspecified"),
        UNKNOWN ("none")
        ;

        final String value;
        Optical (String value) {
            this.value = value;
        }

        @JsonValue
        public String toValue () {
            return value;
        }
        
        @JsonCreator
        public static Optical forValue (String value) {
            if (value.equals("( + )") || value.equals("(+)"))
                return PLUS;
            if (value.equals("( - )") || value.equals("(-)"))
                return MINUS;
            if (value.equals("( + / - )") || value.equals("(+/-)"))
                return PLUS_MINUS;
            if (value.equalsIgnoreCase("unspecified"))
                return UNSPECIFIED;
            if (value.equalsIgnoreCase("none")
                || value.equalsIgnoreCase("unknown"))
                return UNKNOWN;
            return null;
        }
    }

    public enum NYU {
        No,
        Yes,
        Unknown
    }
    
    @Column(length=128)
    public String digest; // digest checksum of the original structure
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    @Indexable(indexed=false)
    public String molfile;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    @Indexable(indexed=false)
    public String smiles;
    
    @Indexable(name="Molecular Formla", facet=true)
    public String formula;
    @JsonProperty("stereochemistry")
    public Stereo stereoChemistry;
    public Optical opticalActivity;
    public NYU atropisomerism;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String stereoComments;
    
    @Indexable(name="Stereocenters", ranges={0,1,2,3,4,5,6,7,8,9,10})
    public Integer stereoCenters; // count of possible stereocenters
    
    @Indexable(name="Defined Stereocenters",ranges={0,1,2,3,4,5,6,7,8,9,10})
    public Integer definedStereo; // count of defined stereocenters
    
    public Integer ezCenters; // counter of E/Z centers
    public Integer charge; // formal charge
    @Indexable(name="Molecular Weight",
               dranges={0,200,400,600,800}, format="%1$.0f")
    public Double mwt; // molecular weight

    @ManyToMany(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)    
    @JoinTable(name="ix_core_structure_property")
    public List<Value> properties = new ArrayList<Value>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    @JoinTable(name="ix_core_structure_link")
    public List<XRef> links = new ArrayList<XRef>();
    
    @Transient
    private ObjectMapper mapper = new ObjectMapper ();
    
    @Transient
    @JsonIgnore
    public transient Object mol; // a transient mol object
    
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
