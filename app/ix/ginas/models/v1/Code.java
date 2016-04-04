package ix.ginas.models.v1;

import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@JSONEntity(title = "Code", isFinal = true)
@Entity
@Table(name="ix_ginas_code")
public class Code extends GinasCommonSubData {
    @JSONEntity(title = "Code system", format = JSONConstants.CV_CODE_SYSTEM)
    @Indexable(facet=true, name="Code System")
    public String codeSystem;
    
    @JSONEntity(title = "Code", isRequired = true)
    @Column(nullable=false)
    public String code;
    
    @JSONEntity(title = "Code Comments")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;

    @JSONEntity(title = "Code Text")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String codeText;
    
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
