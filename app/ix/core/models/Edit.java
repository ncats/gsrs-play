package ix.core.models;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import play.db.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;

import ix.utils.Global;

@Entity
@Table(name="ix_core_edit")
public class Edit extends Model {
    @JsonIgnore
    @Id
    public UUID id; // internal random id

    public final Date created = new Date ();
    public Date modified;

    public Long refid; // edited entity
    public String kind;

    @ManyToOne(cascade=CascadeType.ALL)
    public Principal editor;

    @Column(length=1024)
    public String path;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;

    @Basic(fetch=FetchType.EAGER)
    @Lob
    @JsonDeserialize(as=JsonNode.class)
    public String oldValue; // value as Json

    @Basic(fetch=FetchType.EAGER)
    @Lob
    @JsonDeserialize(as=JsonNode.class)
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
    
    public String getOldValue () {
        return Global.getNamespace()+"/edits/"+id+"/$oldValue";
    }
    public String getNewValue () {
        return Global.getNamespace()+"/edits/"+id+"/$newValue";
    }
}
