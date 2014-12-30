package ix.ginas.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.VStr;
import ix.core.models.VIntArray;

import ix.ginas.chem.CoreChem;

@MappedSuperclass
@Entity
@Table(name="ix_ginas_structure")
public class Structure extends GinasModel {
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String molfile;
    @JsonProperty("stereochemistry")
    public CoreChem.Stereo stereoChemistry;
    public CoreChem.Optical opticalActivity;
    public boolean atropisomerism;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String stereoComments;
    public Integer stereoCenters; // count of possible stereocenters
    public Integer definedStereo; // count of defined stereocenters
    public Integer ezCenters; // counter of E/Z centers
    public Integer charge; // formal charge
    public Double mwt; // molecular weight
    public Integer count; // component count

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_structure_hash")
    public List<VStr> hashkeys = new ArrayList<VStr>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_structure_fingerprint")
    public List<VIntArray> fingerprints = new ArrayList<VIntArray>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_structure_citation")
    public List<Citation> references = new ArrayList<Citation>();

    public Structure () {}
}
