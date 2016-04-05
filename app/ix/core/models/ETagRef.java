package ix.core.models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ix_core_etagref")
public class ETagRef extends LongBaseModel {
    @Id
    public Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    public ETag etag;

    public Long refId;

    public ETagRef () {}
    public ETagRef (ETag etag, Long refId) { 
        this.etag = etag; 
        this.refId = refId;
    }
}
