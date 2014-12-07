package ix.core.models;

import play.db.ebean.Model;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;

@Entity
@Table(name="ix_core_edit")
public class Edit extends Model {
    @Id
    public Long id;
    public String kind;
    public Long refid;
    public final Date created = new Date ();
    public Date modified;

    @ManyToOne(cascade=CascadeType.ALL)
    public Principal principal;

    @Column(length=1024)
    public String path;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;

    @Basic(fetch=FetchType.EAGER)
    @Lob
    @JsonDeserialize(using=JsonNodeDeserializer.class,as=JsonNode.class)
    public String oldValue; // value as Json

    @Basic(fetch=FetchType.EAGER)
    @Lob
    @JsonDeserialize(using=JsonNodeDeserializer.class,as=JsonNode.class)
    public String newValue; // value as Json
    
    public Edit () {}
    public Edit (Class<?> type, Long refid) {
        this.kind = type.getName();
        this.refid = refid;
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.modified = new Date ();
    }
}
