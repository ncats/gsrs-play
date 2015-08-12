package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.core.models.Keyword;
import ix.ginas.models.*;

@JSONEntity(title = "Name", isFinal = true)
@Entity
@Table(name="ix_ginas_name")
public class Name extends Ginas {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_name_access")
    @JsonSerialize(using = PrincipalListSerializer.class)
    @JsonDeserialize(using = PrincipalListDeserializer.class)
    public List<Principal> access = new ArrayList<Principal>();
    
    @JSONEntity(title = "Name", isRequired = true)
    @Column(nullable=false)
    @Indexable(name="Name", suggest=true)
    public String name;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String fullName;
    
    @JSONEntity(title = "Name Type", format = JSONConstants.CV_NAME_TYPE, values = "JSONConstants.ENUM_NAMETYPE")
    @Column(length=32)
    public String type;
    
    @JSONEntity(title = "Domains", format = "table", itemsTitle = "Domain", itemsFormat = JSONConstants.CV_NAME_DOMAIN)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_name_domain",
               joinColumns=@JoinColumn
               (name="ix_ginas_name_domain_uuid",
               referencedColumnName="uuid")
    )
    @JsonSerialize(using=KeywordListSerializer.class)
    public List<Keyword> domains = new ArrayList<Keyword>();
    
    @JSONEntity(title = "Languages", format = "table", itemsTitle = "Language", itemsFormat = JSONConstants.CV_LANGUAGE)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_name_lang",
               joinColumns=@JoinColumn
               (name="ix_ginas_name_lang_uuid",
               referencedColumnName="uuid")
    )
    @JsonSerialize(using=KeywordListSerializer.class)    
    public List<Keyword> languages = new ArrayList<Keyword>();
    
    @JSONEntity(title = "Naming Jurisdictions", format = "table", itemsTitle = "Jurisdiction", itemsFormat = JSONConstants.CV_JURISDICTION)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_name_juris",
               joinColumns=@JoinColumn
               (name="ix_ginas_name_juris_uuid",
               referencedColumnName="uuid")
    )
    @JsonSerialize(using=KeywordListSerializer.class)    
    public List<Keyword> nameJurisdiction = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_name_ref",
               joinColumns=@JoinColumn
               (name="ix_ginas_name_ref_uuid",
               referencedColumnName="uuid")
    )
    @JsonSerialize(using=KeywordListSerializer.class)    
    public List<Keyword> references = new ArrayList<Keyword>();    
    
    @JSONEntity(title = "Naming Organizations", format = "table")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_name_nameorg")
    public List<NameOrg> nameOrgs = new ArrayList<NameOrg>();
    
    @JSONEntity(title = "Preferred Term")
    public boolean preferred;

    public Name () {}
    public Name (String name) {
    }

    public String getName () {
        return fullName != null ? fullName : name;
    }

    @PrePersist
    @PreUpdate
    public void tidyName () {
        if (name.length() > 255) {
            fullName = name;
            name = name.substring(0,254);
        }
    }
    
    //TODO
    public List<String> getLocators(){
    	List<String> locators = new ArrayList<String>();
    	//this.references.get(0).
    	return null;
    }
}
