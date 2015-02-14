package ix.core.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_payload")
public class Payload extends Model {
    @Id
    public UUID uuid;
    public final Date created = new Date ();
    
    @OneToOne
    public Namespace namespace;
    
    @Column(length=1024)
    public String name;

    @Column(length=40)
    public String sha1;
    
    @Column(length=128)
    public String mime; // mime type
    public Long size;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_payload_property")
    public List<Value> properties = new ArrayList<Value>();
    
    public Payload () {}
}
