package ix.core.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ix.core.util.TimeUtil;
import play.db.ebean.Model;

@Entity
@Table(name="ix_core_payload")
public class Payload extends BaseModel {
    @Id
    public UUID id;

    @ManyToOne(cascade=CascadeType.ALL)
    public Namespace namespace;
    
    
    public final Date created = TimeUtil.getCurrentDate();
    
    @Column(length=1024)
    public String name;

    @Column(length=40)
    public String sha1;
    
    @Column(length=128)
    public String mimeType; // mime type
    @Column(name="capacity")
    public Long size;
    

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_payload_property")
    public List<Value> properties = new ArrayList<Value>();

    public Payload () {
    	
    }
    
    public Value addIfAbsent (Value prop) {
        if (prop != null) {
            if (prop.id != null) 
                for (Value p : properties) {
                    if (p.id.equals(prop.id))
                        return p;
                }
            properties.add(prop);
        }
        return prop;
    }
}
