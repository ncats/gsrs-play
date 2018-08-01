package ix.ginas.models.v1;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonDeserialize(contentUsing=KeywordDeserializer.ReferenceTagDeserializer.class) 
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

    /**
     * Returns true if the value of any of the tags
     * equals the supplied string.
     * @param tag
     * @return
     */
	public boolean containsTag(String tag) {
		for(Keyword k:this.tags){
			if(k.getValue().equals(tag)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds the appropriate logic to ensure that this reference is 
	 * associated with the act of making the record public
	 * 
	 */
	public void makePublicReleaseReference(){
		this.addTag(Reference.PUBLIC_DOMAIN_REF);
		this.publicDomain=true;
	}

	/**
	 * Returns true if the reference is associated with the 
	 * act of making the record public.
	 * 
	 * This is distinct from both "isPublic" and "isPublicDomain".
	 * @return
	 */
	@JsonIgnore
	public boolean isPublicReleaseReference(){
		if(this.containsTag(Reference.PUBLIC_DOMAIN_REF)){
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns true if the reference is public domain.
	 * 
	 * This is distinct from the "isPublic" method,
	 * which simply checks if the record has any access
	 * restrictions. This method, by contrast, ensures that
	 * the reference is explicitly set to be public domain.
	 * @return
	 */
	public boolean isPublicDomain(){
		return this.publicDomain;
	}

	@Override
	public String toString() {
		return "Reference{" +
				"owner=" + owner +
				", citation='" + citation + '\'' +
				", docType='" + docType + '\'' +
				", documentDate=" + documentDate +
				", publicDomain=" + publicDomain +
				", tags=" + tags +
				", uploadedFile='" + uploadedFile + '\'' +
				", id='" + id + '\'' +
				", url='" + url + '\'' +
				'}';
	}

	/**
	 * DO NOT USE. - ONLY USED FOR SORTING IN HIDDEN ELEMENT IN HTML.
	 * @return the document date as a sortable String.
	 */
	@JsonIgnore
	public String getDocumentSortPrefix(){
		if(documentDate ==null){
			return "";
		}
		return LocalDateTime.ofInstant(documentDate.toInstant(), ZoneId.systemDefault())
							.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}
