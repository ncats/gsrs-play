package ix.ginas.models.v1;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_subunit")
public class Subunit extends Ginas {
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String sequence;
    public Integer subunitIndex;

    public Subunit () {}
}
