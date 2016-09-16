package ix.core.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.fge.jsonpatch.diff.JsonDiff;

import ix.core.UserFetcher;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.TimeUtil;
import ix.utils.Global;

@Entity
@Table(name="ix_core_edit")
public class Edit extends BaseModel {
    @JsonIgnore
    @Id
    public UUID id; // internal random id
    public final Long created = TimeUtil.getCurrentTimeMillis();

    public String refid; // edited entity
    public String kind;

    // this edit belongs to a chain of edit history
    @Column(length=64)
    public String batch;
        
    @ManyToOne(cascade=CascadeType.PERSIST)
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
    @Indexable(indexed=false)
    public String oldValue; // value as Json

    @Basic(fetch=FetchType.EAGER)
    @Lob
    @JsonDeserialize(as=JsonNode.class)
    @Indexable(indexed=false)
    public String newValue; // value as Json

    public Edit () {}
    
    
    public Edit (Class<?> type, Object refid) {
        this.kind = type.getName();
        this.refid = refid.toString();
    }
    
    public Edit (EntityWrapper<?> entity) {
        this.kind = entity.getKind();
        this.refid = entity.getKey().getIdString();
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
    
    @Override
    public String fetchGlobalId(){
    	if(this.id==null)return null;
    	return this.id.toString();
    }
    
    @JsonIgnore
    public Date getCreatedDate(){
    	return new Date(this.created);
    }
    
    @JsonIgnore
    public JsonNode getDiff(){
    	try{
	    	ObjectMapper om = new ObjectMapper();
	    	JsonNode js1=om.readTree(oldValue);
	    	JsonNode js2=om.readTree(newValue);
	    	return JsonDiff.asJson(js1, js2);
    	}catch(Exception e){
    		return null;
    	}
    }
}
