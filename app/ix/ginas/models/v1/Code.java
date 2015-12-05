package ix.ginas.models.v1;

import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.ginas.models.GinasSubData;
import ix.ginas.models.KeywordListSerializer;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JSONEntity(title = "Code", isFinal = true)
@Entity
@Table(name="ix_ginas_code")
public class Code extends GinasSubData {
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
    
    @JSONEntity(title = "Code Type", format = JSONConstants.CV_CODE_TYPE)
    public String type;
    
    @JSONEntity(title = "Code URL", format = "uri")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String url;

//    @ManyToMany(cascade=CascadeType.ALL)
//    @JoinTable(name="ix_ginas_code_reference")
//    @JsonSerialize(using=KeywordListSerializer.class)    
//    public List<Keyword> references = new ArrayList<Keyword>();
    
    public Code () {}
    public Code (String code) {
        this.code = code;
    }
}
