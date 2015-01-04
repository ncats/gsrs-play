package ix.ginas.models;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="ix_ginas_subunit")
public class Subunit extends GinasModel {
    @Lob
    public String sequence;
    
    @JsonProperty("subunitIndex")
    public int position;

    public Subunit () {}
    public Integer getLength () {
	return sequence != null ? sequence.length() : null;
    }
}
