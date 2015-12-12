package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.KeywordListSerializer;

@Entity
@Table(name="ix_ginas_polymerclass")
public class PolymerClassification extends GinasCommonSubData {
    @Indexable(facet=true,name="Polymer Class")
    public String polymerClass;
    @Indexable(facet=true,name="Polymer Geometry")
    public String polymerGeometry;
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_polymerclass_sub")
    @JsonSerialize(using=KeywordListSerializer.class)    
    public List<Keyword> polymerSubclass = new ArrayList<Keyword>();
    public String sourceType;
    
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference parentSubstance;

    public PolymerClassification () {}
}
