package ix.core.models;

import java.util.Date;
import javax.persistence.*;

import ix.core.util.TimeUtil;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import ix.utils.Global;

@MappedSuperclass
public class IxModel extends BaseModel {
    @Id public Long id;
    @Version public Long version;

    @ManyToOne(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public Namespace namespace; // namespace of dictionary, ontology, etc.
    
    public Date created = TimeUtil.getCurrentDate();
    public Date modified;
    public boolean deprecated;

    public IxModel () {}

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_namespace")
    public String getJsonNamespace () {
        return Global.getRef(namespace);
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.modified = TimeUtil.getCurrentDate();
    }

	@Override
	public String fetchGlobalId() {
		if(id!=null)return this.getClass().getName() + ":" + id.toString();
		return null;
	}
}
