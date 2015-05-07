package ix.ncats.models.hcs;

import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;
import ix.core.models.Value;
import ix.core.models.IxModel;
import ix.core.models.Indexable;
import ix.core.models.Keyword;

@Entity
@Table(name="ix_ncats_hcs_reagent")
public class Reagent extends IxModel {
    public enum CellType {
        Fixable,
        Fixed,
        Live
    }
    
    @Indexable(suggest=true)
    public String name;
    @Indexable(suggest=true)
    public String barcode;
    
    @Indexable(facet=true,name="Cell Type",suggest=true)
    public CellType celltype;
    
    @Indexable(facet=true,name="Application Type",suggest=true)
    public String apptype;
    
    @Indexable(facet=true,name="Color")
    public String color;
    
    @Indexable(facet=true,name="Application",suggest=true)
    public String application;

    @Indexable(facet=true,name="Excitation")
    public Integer excitation;
    @Indexable(facet=true,name="Emission")
    public Integer emission;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_hcs_reagent_tag")
    public List<Keyword> tags = new ArrayList<Keyword>();
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_hcs_reagent_property")
    public List<Value> properties = new ArrayList<Value>();

    public Reagent () {}
}
