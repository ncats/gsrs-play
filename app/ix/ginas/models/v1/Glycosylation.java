package ix.ginas.models.v1;


import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_glycosylation")
@JSONEntity(name = "glycosylation", title = "Glycosylation", isFinal = true)
public class Glycosylation extends GinasCommonSubData {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_glyco_csite",
               joinColumns=@JoinColumn
               (name="ix_ginas_glyco_c_uuid",
                referencedColumnName="uuid"))
    public List<Site> CGlycosylationSites = new ArrayList<Site>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_glyco_nsite",
               joinColumns=@JoinColumn
               (name="ix_ginas_glyco_n_uuid",
                referencedColumnName="uuid"))
    public List<Site> NGlycosylationSites = new ArrayList<Site>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_glyco_osite",
               joinColumns=@JoinColumn
               (name="ix_ginas_glyco_o_uuid",
                referencedColumnName="uuid"))
    public List<Site> OGlycosylationSites = new ArrayList<Site>();
    
    @Indexable(facet=true,name="Glycosylation Type")
    public String glycosylationType;

    public Glycosylation () {}

}
