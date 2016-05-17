package ix.ginas.models.v1;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.SingleParent;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.GinasCommonData;
import ix.ginas.models.serialization.KeywordDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

@JSONEntity(title = "Reference", isFinal = true)
@Entity
@Table(name="ix_ginas_reference")
@SingleParent
public class Reference extends GinasCommonData {

	@ManyToOne(cascade = CascadeType.PERSIST)
	private Substance owner;
	
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

	public static String PUBLIC_DOMAIN_REF="PUBLIC_DOMAIN_RELEASE";

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
    public void addTag(String tag){
    	this.tags.add(new Keyword(GinasCommonData.REFERENCE_TAG, tag));
    }
    
    @PreUpdate
   	public void updateImmutables(){
   		this.tags= new EmbeddedKeywordList(this.tags);
   	}
}
