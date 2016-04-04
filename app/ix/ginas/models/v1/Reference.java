package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.core.models.Keyword;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.*;

@JSONEntity(title = "Reference", isFinal = true)
@Entity
@Table(name="ix_ginas_reference")
public class Reference extends GinasCommonSubData {

    @JSONEntity(title = "Citation Text", isRequired = true)
    @Lob
    public String citation;
    
    @JSONEntity(title = "Reference Type", format = JSONConstants.CV_DOCUMENT_TYPE, values = "JSONConstants.ENUM_DOCUMENTTYPE", isRequired = true)
    @Indexable(facet=true,name="Reference Type")
    public String docType;
    
    @JSONEntity(title = "Date Accessed", format = "date")
    public Date documentDate;
    
    @JSONEntity(title = "Public Domain Reference")
    public boolean publicDomain;
    
    @JSONEntity(title = "Tags", format = "table", itemsTitle = "Tag", itemsFormat = JSONConstants.CV_DOCUMENT_COLLECTION, isUniqueItems = true)
//    @ManyToMany(cascade=CascadeType.ALL)
//    @JoinTable(name="ix_ginas_reference_tag")
    @JsonSerialize(using=KeywordListSerializer.class) 
    @JsonDeserialize(contentUsing=KeywordDeserializer.TagDeserializer.class) 
    @Basic(fetch=FetchType.LAZY)
    public EmbeddedKeywordList tags = new EmbeddedKeywordList();
    
    @JSONEntity(title = "Uploaded Document")
    @Column(length=1024)
    public String uploadedFile;
    
    @JSONEntity(title = "Ref_ID")
    public String id;
    
    @JSONEntity(title = "Reference URL", format = "URI")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String url;

    public Reference () {
    	this.setUuid(UUID.randomUUID());
    	
    }
    
    public static Reference SYSTEM_ASSUMED(){
    	Reference r = new Reference();
		r.citation="Assumed or asserted";
		r.docType="SYSTEM";
		return r;
    }
    
    public static Reference SYSTEM_GENERATED(){
    	Reference r = new Reference();
		r.citation="System generated";
		r.docType="SYSTEM";
		return r;
    }
}
