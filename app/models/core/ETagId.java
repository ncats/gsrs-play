package models.core;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_etag_id")
public class ETagId extends Model {
    @ManyToOne
    public ETag etag;
    public Long id;

    public ETagId () {}
    public ETagId (ETag etag, Long id) { 
        this.etag = etag; 
        this.id = id;
    }
}
