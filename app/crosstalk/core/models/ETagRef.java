package crosstalk.core.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_etagref")
public class ETagRef extends Model {
    @Id
    public Long id;

    @ManyToOne(cascade=CascadeType.ALL)
    public ETag etag;

    public Long refId;

    public ETagRef () {}
    public ETagRef (ETag etag, Long refId) { 
        this.etag = etag; 
        this.refId = refId;
    }
}
