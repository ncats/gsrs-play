package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;

import java.util.List;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.Ginas;

@JSONEntity(title = "Code", isFinal = true)
@Entity
@Table(name="ix_ginas_code")
public class Code extends Ginas {
    @JSONEntity(title = "Code system", format = JSONConstants.CV_CODE_SYSTEM)
    public String codeSystem;
    
    @JSONEntity(title = "Code", isRequired = true)
    @Column(nullable=false)
    public String code;
    
    @JSONEntity(title = "Code Comments")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;
    
    @JSONEntity(title = "Code Type", format = JSONConstants.CV_CODE_TYPE)
    public String type;
    
    @JSONEntity(title = "Code URL", format = "uri")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String url;

    public Code () {}
    public Code (String code) {
        this.code = code;
    }
}
