package ix.ginas.models.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.models.IxModel;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.serialization.KeywordDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;

/**
 * Created by sheilstk on 6/29/15.
 */

@Entity
@Table(name="ix_ginas_vocabulary_term")
@Inheritance
@DiscriminatorValue("VOCAB")
public class VocabularyTerm extends IxModel{
    /**
	 * 
	 */
	
	@ManyToOne(cascade = CascadeType.PERSIST)
	private ControlledVocabulary owner;
	
    @JsonIgnore
    public Long getId(){
    	return this.id;
    };

	private static final long serialVersionUID = -5625533710493695789L;
	public String value;
    public String display;
    public String description;
    public String origin;
//    public String filter;
    
    @JsonSerialize(using=KeywordListSerializer.class)
    @JsonDeserialize(contentUsing=KeywordDeserializer.DomainDeserializer.class)
    @Basic(fetch=FetchType.LAZY)
    public EmbeddedKeywordList filters = new EmbeddedKeywordList();

    public boolean hidden=false;
    public boolean selected=false;

    public VocabularyTerm(){};
    
    
    
}
