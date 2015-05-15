package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_glycosylation")
public class Glycosylation extends Ginas {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_glycosylation_csite",
               joinColumns=@JoinColumn
               (name="ix_ginas_glycosylation_c_uuid",
                referencedColumnName="uuid"))
    public List<Site> CGlycosylationSites = new ArrayList<Site>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_glycosylation_nsite",
               joinColumns=@JoinColumn
               (name="ix_ginas_glycosylation_n_uuid",
                referencedColumnName="uuid"))
    public List<Site> NGlycosylationSites = new ArrayList<Site>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_glycosylation_osite",
               joinColumns=@JoinColumn
               (name="ix_ginas_glycosylation_o_uuid",
                referencedColumnName="uuid"))
    public List<Site> OGlycosylationSites = new ArrayList<Site>();
    
    @Indexable(facet=true,name="Glycosylation Type")
    public String glycosylationType;

    public Glycosylation () {}
}
