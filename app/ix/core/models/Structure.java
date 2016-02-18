package ix.core.models;

import ix.utils.Global;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@MappedSuperclass
@Entity
@Inheritance
@DiscriminatorValue("DEF")
@Table(name = "ix_core_structure")
public class Structure extends Model{

    @Id
    public UUID id;

    @Version
    public Long version;
    
    public final Date created = new Date();
    public Date lastEdited;
    public boolean deprecated;

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
        ABSOLUTE, ACHIRAL, RACEMIC, MIXED, EPIMERIC, UNKNOWN;
    }

        // optical activity
    public enum Optical {
        PLUS("( + )"),
        MINUS("( - )"),
        PLUS_MINUS("( + / - )"),
        UNSPECIFIED("UNSPECIFIED"),
        UNKNOWN("NONE");
        
        final String value;

        Optical(String value) {
            this.value = value;
        }

        @JsonValue
        public String toValue() {
            return value;
        }

        @JsonCreator
        public static Optical forValue(String value) {
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
        No, Yes, Unknown
    }
    
    @Column(length = 128)
    public String digest; // digest checksum of the original structure
    
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Indexable(indexed = false, structure=true)
    public String molfile;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Indexable(indexed = false)
    public String smiles;

    @Indexable(name = "Molecular Formula", facet = true)
    public String formula;

    @JsonProperty("stereochemistry")
    @Column(name = "stereo")
    @Indexable(name = "StereoChemistry", facet = true)
    public Stereo stereoChemistry;
    
    @Column(name = "optical")
    public Optical opticalActivity;
    
    @Column(name = "atropi")
    public NYU atropisomerism;
    
    @Lob
    @Basic(fetch = FetchType.EAGER)
    public String stereoComments;
    
    @Indexable(name = "Stereocenters", ranges = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    public Integer stereoCenters; // count of possible stereocenters
    
    @Indexable(name = "Defined Stereocenters", ranges = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    public Integer definedStereo; // count of defined stereocenters
    
    public Integer ezCenters; // counter of E/Z centers
    public Integer charge; // formal charge
    @Indexable(name = "Molecular Weight", dranges = { 0, 200, 400, 600, 800, 1000 }, format = "%1$.0f")
    public Double mwt; // molecular weight
    
    @ManyToMany(cascade = CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    @JoinTable(name = "ix_core_structure_property")
    public List<Value> properties = new ArrayList<Value>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    @JoinTable(name = "ix_core_structure_link")
    public List<XRef> links = new ArrayList<XRef>();

    @Transient
    private ObjectMapper mapper = new ObjectMapper();

        /*
         * @Transient
         * 
         * @JsonIgnore public transient Object mol; // a transient mol object
         */
    public Integer count = 1; // moiety count?
    public Structure() {
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_properties")
    public JsonNode getJsonProperties() {
        JsonNode node = null;
        if (id != null) {
            if (!properties.isEmpty()) {
                ObjectNode obj = mapper.createObjectNode();
                obj.put("count", properties.size());
                obj.put("href", Global.getRef(getClass(), id) + "/properties");
                node = obj;
            }
        } else {
            // node = mapper.valueToTree(properties);
        }
        return node;
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_links")
    public JsonNode getJsonLinks() {
        JsonNode node = null;
        if (id != null) {
            if (!links.isEmpty()) {
                ObjectNode obj = mapper.createObjectNode();
                obj.put("count", links.size());
                obj.put("href", Global.getRef(getClass(), id) + "/links");
                node = obj;
            }
        } else {
            // node = mapper.valueToTree(links);
        }
        return node;
    }

    public String getSelf() {
        return id != null ? Global.getRef(this) + "?view=full" : null;
    }

    @PrePersist
    @PreUpdate
    public void modified() {
        Date newDate = new Date();
        if (this.lastEdited == null) {
            this.lastEdited = newDate;
        }
        this.lastEdited = newDate;
    }

    public String getId() {
        return id != null ? id.toString() : null;
    }
    
    @JsonIgnore
    public String getLychiv4Hash(){
    	 String newhash=null;
    	 for (Value val : this.properties) {
             if (Structure.H_LyChI_L4.equals(val.label)) {
            	 try{
            		 newhash=val.getValue()+"";
            	 }catch(Exception e){
            		 
            	 }
             }
         }
    	 return newhash;
    }
}
