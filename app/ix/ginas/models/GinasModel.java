package ix.ginas.models;

import java.util.UUID;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import play.db.ebean.Model;
import ix.core.models.IxModel;
import ix.utils.Global;
import ix.core.models.BeanViews;
import ix.core.models.XRef;
import ix.core.models.Namespace;
import ix.core.models.Indexable;

@MappedSuperclass
public class GinasModel extends Model {
    @Id
    public UUID id;

    @ManyToOne(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public Namespace namespace; // namespace of dictionary, ontology, etc.
    
    public final Date created = new Date ();
    public Date modified;
    public boolean deprecated;

    @Indexable(name="Public Domain", facet=true)
    public boolean isPublicDomain;

    public GinasModel () {
    }
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_namespace")
    public String getJsonNamespace () {
        return Global.getRef(namespace);
    }

    @PrePersist
    @PreUpdate
    public void modified () {
        this.modified = new Date ();
    }
}
