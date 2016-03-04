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

import ix.core.UserFetcher;
import ix.utils.Global;

@Entity
@Table(name="ix_core_edit")
public class Edit extends Model {
    @JsonIgnore
    @Id
    public UUID id; // internal random id
    public final Long created = System.currentTimeMillis();

    public String refid; // edited entity
    public String kind;

    // this edit belongs to a chain of edit history
    @Column(length=64)
    public String batch;
        
    @ManyToOne(cascade=CascadeType.ALL)
    public Principal editor;

    @Column(length=1024)
    public String path;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;
    
    public String version=null;

    @Basic(fetch=FetchType.EAGER)
    @Lob
    @JsonDeserialize(as=JsonNode.class)
    public String oldValue; // value as Json

    @Basic(fetch=FetchType.EAGER)
    @Lob
    @JsonDeserialize(as=JsonNode.class)
    public String newValue; // value as Json

    public Edit () {}
    public Edit (Class<?> type, Object refid) {
        this.kind = type.getName();
        this.refid = refid.toString();
    }
    
    @PrePersist
    public void preCommit(){
    	this.editor=UserFetcher.getActingUser();
    }
    
    
    
    public String getEditor(){
    	if(editor==null)return null;
    	return editor.username;
    }
    
    public String getOldValue () {
        return oldValue != null
            ? Global.getNamespace()+"/edits/"+id+"/$oldValue" : null;
    }
    public String getNewValue () {
        return newValue != null
            ? Global.getNamespace()+"/edits/"+id+"/$newValue" : null;
    }
}
