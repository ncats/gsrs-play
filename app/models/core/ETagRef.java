package models.core;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_etag_ref")
public class ETagRef extends Model {
    @Id
    public Long id;

    @ManyToOne
    public ETag etag;

    public Long refId;

    public ETagRef () {}
    public ETagRef (ETag etag, Long refId) { 
        this.etag = etag; 
        this.refId = refId;
    }
}
