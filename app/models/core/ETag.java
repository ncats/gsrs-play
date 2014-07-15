package models.core;

import play.db.ebean.Model;

import java.util.Date;
import javax.persistence.*;

@Entity
public class ETag extends Model {
    @Id
    @Column(length=16)
    public String etag;

    @Column(length=4000)
    public String uri;
    @Column(length=10)
    public String method;
    @Column(length=40)
    public String hash; // SHA1

    public Integer total;
    public Integer count;
    public Integer skip;
    public Integer top;

    public Integer status;
    public Date timestamp = new Date ();
    public Date modified = new Date ();

    @Column(length=1024)
    public String filter;

    public ETag () {}
    public ETag (String etag) { this.etag = etag; }
}
