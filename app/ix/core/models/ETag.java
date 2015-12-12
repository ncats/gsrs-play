package ix.core.models;

import play.db.ebean.Model;

import java.security.SecureRandom;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="ix_core_etag")
@Indexable(indexed=false)
public class ETag extends IxModel {

    @Column(length=16,unique=true)
    public final String etag;

    @Column(length=4000)
    public String uri;
    public String getNextPageUri(){
    	Integer skip=this.skip;
    	String nskip = "skip=" + (skip+top);
    	String nuri = (uri+"").replace("skip=" + this.skip, nskip);
    	if(!uri.contains("skip=" + skip)){
    		if(nuri.contains("?")){
    			nuri=nuri +"&" + nskip;
    		}else{
    			nuri=nuri +"?" + nskip;
    		}
    	}
    	return nuri;
    }
    public String getPreviosPageUri(){
    	Integer skip=this.skip;
    	String nskip = "skip=" + Math.max((skip-top),0);
    	String nuri = (uri+"").replace("skip=" + skip, nskip);
    	if(!uri.contains("skip=" + skip)){
    		if(nuri.contains("?")){
    			nuri=nuri +"&" + nskip;
    		}else{
    			nuri=nuri +"?" + nskip;
    		}
    	}
    	return nuri;
    }
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

    @Column(length=2048)
    public String query;
    @Column(length=4000)
    public String filter;

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
