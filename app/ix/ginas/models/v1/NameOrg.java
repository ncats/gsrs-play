package ix.ginas.models.v1;

import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.GinasSubData;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;

@Entity
@Table(name="ix_ginas_nameorg")
@JSONEntity(name = "nameOrg", title = "Naming Org", isFinal = true)
public class NameOrg extends GinasSubData {
    @JSONEntity(title = "Naming Organization", format = JSONConstants.CV_NAME_ORG, isRequired = true)
    @Column(nullable=false)
    public String nameOrg;
    
    // TODO Is this the only thing that can reasonably be deprecated?!!
    @JSONEntity(title = "Deprecated")
    public boolean deprecated;
    
    @JSONEntity(title = "Deprecated Date", format = "date")
    public Date deprecatedDate;

    public NameOrg () {}
    public NameOrg (String nameOrg) {
        this.nameOrg = nameOrg;
    }
}
