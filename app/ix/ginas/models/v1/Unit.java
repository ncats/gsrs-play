package ix.ginas.models.v1;

import javax.persistence.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.VIntArray;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.IntArraySerializer;
import ix.ginas.models.IntArrayDeserializer;

@Entity
@Table(name="ix_ginas_unit")
public class Unit extends GinasCommonSubData {
    @OneToOne(cascade=CascadeType.ALL)
    @JsonSerialize(using = IntArraySerializer.class)
    @JsonDeserialize(using = IntArrayDeserializer.class)
    public VIntArray amap;

    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;
    public Integer attachmentCount;
    public String label;
    @Lob
    @Basic(fetch=FetchType.EAGER)
    //should be changed to structure
    public String structure;
    public String type;

    public Unit () {}
}
