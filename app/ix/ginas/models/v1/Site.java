package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_site")
@JSONEntity(name = "site", title = "Site", isFinal = true)
public class Site extends Ginas {
    @JSONEntity(title = "Subunit Index")
    public Integer subunitIndex;
    
    @JSONEntity(title = "Residue Index")
    public Integer residueIndex;

    public Site () {}
    
    public String toString(){
    	return this.subunitIndex + "_" + this.residueIndex;
    }
}
