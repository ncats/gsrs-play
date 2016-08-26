package ix.ginas.models.v1;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import ix.core.models.Indexable;
import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

@JSONEntity (title = "Code", isFinal = true)
@Entity
@Table(name="ix_ginas_code")
public class Code extends CommonDataElementOfCollection{
	
	
    @JSONEntity(title = "Code system", format = JSONConstants.CV_CODE_SYSTEM)
    @Indexable(facet=true, name="Code System")
    public String codeSystem;
    
    @JSONEntity(title = "Code", isRequired = true)
    @Column(nullable=false)
    @Indexable(name="Code", suggest=true)
    public String code;
    
    public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

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
    
    public String toString(){
		return "Code[" + this.codeSystem + "]:\"" +this.getCode()  +"\"";
	}
    
    
    
}
