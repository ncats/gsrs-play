package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

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
    private static final String SRS_LOCATOR = "SRS_LOCATOR";

    @JSONEntity(title = "Name", isRequired = true)
    @Column(nullable=false)
    @Indexable(name="Name", suggest=true)
    public String name;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String fullName;
    
    @JSONEntity(title = "Name Type", format = JSONConstants.CV_NAME_TYPE, values = "JSONConstants.ENUM_NAMETYPE")
    @Column(length=32)
    public String type="cn";
    
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
    
    /**
     * Returns the locators that have been added to this name record.
     * 
     * These are tags that are used for searching and display.
     * 
     * Currently, this requires the parent substance in order to 
     * make it work.
     * 
     * @param sub the parent substance of this name
     * @return
     */
    public List<String> getLocators(Substance sub){
    	List<String> locators = new ArrayList<String>();
    	//locators.add("TEST");
    	if(sub!=null){
    		//System.out.println("Real sub");
	    	for(Keyword ref: this.references){
	    		//System.out.println(ref.getValue());
	    		Reference r=sub.getReferenceByUUID(ref.getValue());
	    		
	    		if(r!=null){
	    			//System.out.println(r.citation);
	    			if(r.docType.equals(Name.SRS_LOCATOR)){
	    				try{
	    					String tag=r.citation.split("\\[")[1].split("\\]")[0];
	    					locators.add(tag);
	    				}catch(Exception e){
	    					
	    				}
	    			}
	    		}
	    	}
    	}
    	return new ArrayList<String>(new TreeSet<String>(locators));
    }
    
    
    /*
     * Utility function to sort names in nice display order.
     * 
     * Sort criteria:
     * 		1) Preferred status
     * 		2) Official status
     * 		3) English first
     * 		4) Number of References
     * 		5) Name Type
     * 		6) Alphabetical
     * 
     */
    public static List<Name> sortNames(List<Name> nameList){
    	Collections.sort(nameList, new Comparator<Name>(){
			@Override
			public int compare(Name o1, Name o2) {
				if(o1.preferred!=o2.preferred){
					if(o2.preferred)return 1;
					return -1;
				}		
				if(o1.isOfficial()!=o2.isOfficial()){
					if(o2.isOfficial())return 1;
					return -1;
				}
				if(o1.isLanguage("en")!=o2.isLanguage("en")){
					if(o2.isLanguage("en"))return 1;
					return -1;
				}
				int refDiff=o2.references.size()-o1.references.size();
				if(refDiff!=0){
					return refDiff;
				}
				if(!o2.type.equals(o1.type)){
					return -o2.type.compareTo(o1.type);
				}
				return -o2.name.compareTo(o1.name);
			}    		
    	});
    	return nameList;
    }
    
	public boolean isOfficial() {
		if(this.type.equals("of"))return true;
		return false;
	}
	public boolean isLanguage(String lang){
		for(Keyword k:this.languages){
			if(k.getValue().equals(lang))return true;
		}
		return false;
	}
}
