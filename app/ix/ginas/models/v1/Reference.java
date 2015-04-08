package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.core.models.Keyword;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.Ginas;

@JSONEntity(title = "Reference", isFinal = true)
@Entity
@Table(name="ix_ginas_reference")
public class Reference extends Ginas {
    @JSONEntity(title = "Citation Text", isRequired = true)
    @Column(nullable=false)
    public String citation;
    
    @JSONEntity(title = "Reference Type", format = JSONConstants.CV_DOCUMENT_TYPE, values = "JSONConstants.ENUM_DOCUMENTTYPE", isRequired = true)
    public String docType;
    
    @JSONEntity(title = "Date Accessed", format = "date")
    public Date documentDate;
    
    @JSONEntity(title = "Public Domain Reference")
    public boolean publicDomain;
    
    @JSONEntity(title = "Tags", format = "table", itemsTitle = "Tag", itemsFormat = JSONConstants.CV_DOCUMENT_COLLECTION, isUniqueItems = true)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_reference_tag")
    @JsonSerialize(using=KeywordListSerializer.class)    
    public List<Keyword> tags = new ArrayList<Keyword>();
    
    @JSONEntity(title = "Uploaded Document")
    @Column(length=1024)
    public String uploadedFile;
    
    @JSONEntity(title = "Ref_ID")
    public String id;
    
    @JSONEntity(title = "Reference URL", format = "URI")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String url;

    public Reference () {}
}
