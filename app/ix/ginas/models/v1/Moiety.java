package ix.ginas.models.v1;

import java.util.UUID;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Structure;
import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasCommonSubData;


@JsonDeserialize(using = MoietyDeserializer.class)
@JsonSerialize(using = MoietySerializer.class)
@JSONEntity(name = "moiety", title = "Moiety")
@Entity
@Table(name = "ix_ginas_moiety")
public class Moiety extends GinasCommonSubData {
	@Id
	public UUID uuid;
	
    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    public GinasChemicalStructure structure;
    
    @JSONEntity(title = "Count")
    public Integer count;

    public Moiety () {}
    
}