package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.SingleParent;
import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.serialization.KeywordDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

@JSONEntity(title = "Name", isFinal = true)
@Entity
@Table(name="ix_ginas_name")
@SingleParent
public class Name extends CommonDataElementOfCollection {
    private static final String SRS_LOCATOR = "SRS_LOCATOR";
    

    @JSONEntity(title = "Name", isRequired = true)
    @Column(nullable=false)
    @Indexable(name="Name", suggest=true)
    public String name;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    @JsonIgnore
    public String fullName;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    @JsonView(BeanViews.Internal.class)
    public String stdName;
    
    
    @JSONEntity(title = "Name Type", format = JSONConstants.CV_NAME_TYPE, values = "JSONConstants.ENUM_NAMETYPE")
    @Column(length=32)
    public String type="cn";
    
    @JSONEntity(title = "Domains", format = "table", itemsTitle = "Domain", itemsFormat = JSONConstants.CV_NAME_DOMAIN)
    @JsonSerialize(using=KeywordListSerializer.class)
    @JsonDeserialize(contentUsing=KeywordDeserializer.DomainDeserializer.class)
    @Basic(fetch=FetchType.LAZY)
    public EmbeddedKeywordList domains = new EmbeddedKeywordList();
    
    @JSONEntity(title = "Languages", format = "table", itemsTitle = "Language", itemsFormat = JSONConstants.CV_LANGUAGE)
    @JsonSerialize(using=KeywordListSerializer.class)
    @JsonDeserialize(contentUsing=KeywordDeserializer.LanguageDeserializer.class)
    @Basic(fetch=FetchType.LAZY)
    public EmbeddedKeywordList languages = new EmbeddedKeywordList();
    
    @JSONEntity(title = "Naming Jurisdictions", format = "table", itemsTitle = "Jurisdiction", itemsFormat = JSONConstants.CV_JURISDICTION)
    @JsonSerialize(using=KeywordListSerializer.class)    
    @JsonDeserialize(contentUsing=KeywordDeserializer.JurisdictionDeserializer.class)
    @Basic(fetch=FetchType.LAZY)
    public EmbeddedKeywordList nameJurisdiction = new EmbeddedKeywordList();
    
    @JSONEntity(title = "Naming Organizations", format = "table")

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<NameOrg> nameOrgs = new ArrayList<NameOrg>();
    
    
    @JSONEntity(title = "Preferred Term")
    /**
     * There can be many preferred terms per substance
     */
    public boolean preferred;
    
    /**
     * There can only be 1 display name per substance
     */
    public boolean displayName;

    public Name () {}
    public Name (String name) {
    }

    public String getName () {
        return fullName != null ? fullName : name;
    }

    @PrePersist
    @PreUpdate
    public void tidyName () {
        if (name.getBytes().length > 255) {
            fullName = name;
            name = truncateString(name,254);
            
        }
    }
    
    private static String truncateString(String s, int maxBytes){
    	byte[] b = (s+"   ").getBytes();
    	if(maxBytes>=b.length){
    		return s;
    	}
    	boolean lastComplete=false;
    	for(int i=maxBytes;i>=0;i--){
    		if(lastComplete)
    			return new String(Arrays.copyOf(b, i));
    		if((b[i] & 0x80) ==0){
    			return new String(Arrays.copyOf(b, i));
    		}
    		if(b[i]==-79){
    			lastComplete=true;
    		}
    	}
    	
    	return "";
    }
    
    public void addLocator(Substance sub, String loc){
    	Reference r = new Reference();
    	r.docType=Name.SRS_LOCATOR;
    	r.citation=this.name + " [" + loc + "]";
    	r.publicDomain=true;
    	this.addReference(r,sub);
    	sub.addTagString(loc);
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
	    	for(Keyword ref: this.getReferences()){
	    		//System.out.println(ref.getValue());
	    		Reference r=sub.getReferenceByUUID(ref.getValue().toString());
	    		
	    		if(r!=null && r.docType!=null){
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
     *      1) Display Name
     * 		2) Preferred status
     * 		3) Official status
     * 		4) English first
     * 		5) Number of References
     * 		6) Name Type
     * 		7) Alphabetical
     * 
     */
    public static List<Name> sortNames(List<Name> nameList){
    	Collections.sort(nameList, new Comparator<Name>(){
			@Override
			public int compare(Name o1, Name o2) {
				if(o1.isDisplayName()!= o2.isDisplayName()){
					if(o1.isDisplayName())return 1;
					return -1;
				}
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
				int refDiff=o2.getReferences().size()-o1.getReferences().size();
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
    
    
    public boolean isDisplayName() {
    	return displayName;
	}
    
    
    @JsonIgnore
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
	
	
	@PreUpdate
	public void updateImmutables(){
		super.updateImmutables();
		this.languages= new EmbeddedKeywordList(this.languages);
		this.domains= new EmbeddedKeywordList(this.domains);
		this.nameJurisdiction= new EmbeddedKeywordList(this.nameJurisdiction);
	}
	
}
