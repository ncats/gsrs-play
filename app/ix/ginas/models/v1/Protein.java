package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;
import ix.core.models.Indexable;
import ix.core.models.Value;

@Entity
@Table(name="ix_ginas_protein")
public class Protein extends Ginas {
    @Indexable(facet=true,name="Protein Type")
    public String proteinType;
    
    @Indexable(facet=true,name="Protein Subtype")
    public String proteinSubType;
    
    @Indexable(facet=true,name="Sequence Origin")
    public String sequenceOrigin;
    
    @Indexable(facet=true,name="Sequence Type")
    public String sequenceType;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_disulfide")
    public List<DisulfideLink> disulfideLinks = new ArrayList<DisulfideLink>();

    @OneToOne(cascade=CascadeType.ALL)
    public Glycosylation glycosylation;

    @OneToOne(cascade=CascadeType.ALL)
    public Modifications modifications;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_subunit")
    public List<Subunit> subunits = new ArrayList<Subunit>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_otherlinks")
    public List<OtherLinks> otherLinks = new ArrayList<OtherLinks>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_reference")
    @JsonSerialize(using=ReferenceListSerializer.class)
    @JsonDeserialize(using=ReferenceListDeserializer.class)
    public List<Value> references = new ArrayList<Value>();

    public Protein () {}
}
