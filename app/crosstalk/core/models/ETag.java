package crosstalk.core.models;

import play.db.ebean.Model;

import java.security.SecureRandom;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name="ct_core_etag")
public class ETag extends Model {
    @Id
    public Long id;

    @Column(length=16)
    public final String etag;

    @Column(length=4000)
    public String uri;
    public String path;
    @Column(length=10)
    public String method;
    @Column(length=40)
    public String sha1; // SHA1

    public Integer total;
    public Integer count;
    public Integer skip;
    public Integer top;

    public Integer status;
    public final Date timestamp = new Date ();
    public Date modified = new Date ();

    @Column(length=2048)
    public String query;

    public ETag () {
        this (nextETag ());
    }
    public ETag (String etag) { this.etag = etag; }


    static final SecureRandom rand = new SecureRandom ();    
    public static String nextETag () {
        return nextETag (8);
    }

    public static String nextETag (int size) {
        byte[] buf = new byte[size];
        rand.nextBytes(buf);

        StringBuilder id = new StringBuilder ();
        for (int i = 0; i < buf.length; ++i)
            id.append(String.format("%1$02x", buf[i]&0xff));

        return id.toString();
    }
}
