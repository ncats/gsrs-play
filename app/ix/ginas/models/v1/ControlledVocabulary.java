package ix.ginas.models.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ix.core.models.Indexable;
import ix.core.models.IxModel;
import ix.core.models.Keyword;
import ix.ginas.models.serialization.KeywordListDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


@Entity
@Table(name="ix_ginas_controlled_vocab")
@Inheritance
@DiscriminatorValue("CTLV")


public class ControlledVocabulary extends IxModel{

	private static final long serialVersionUID = 5455592961232451608L;

	//@JsonIgnore
	public Long getId(){
		return this.id;
	};

	@Column(unique=true)
	@Indexable(name="Domain", facet=true)
	public String domain;


	public void setTerms(List<VocabularyTerm> terms) {
		this.terms = terms;
	}

	private String vocabularyTermType = VocabularyTerm.class.getName();

    @ManyToMany(cascade=CascadeType.ALL)
    @JsonSerialize(using=KeywordListSerializer.class)
    @JsonDeserialize(using=KeywordListDeserializer.class)
	@Indexable(name="Field")
	public List<Keyword> fields = new ArrayList<Keyword>();

	public boolean editable = true;

	public boolean filterable = false;


	@ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_cv_terms")
    //@JsonView(BeanViews.Full.class)
	public List<VocabularyTerm> terms;

	public VocabularyTerm getTermWithValue(String val){
		for(VocabularyTerm vt:this.terms){
			//System.out.println("Looking at value:" + vt.display);
			if(vt.value.equals(val)){
				
				return vt;
			}
		}
		return null;
	}

	public List<? extends VocabularyTerm> getTerms() {
		return terms;
	}

	public <T extends VocabularyTerm> void addTerms(T term) {
		this.terms.add(term);
	}

	public void setVocabularyTermType(String vocabularyTermType) {
		this.vocabularyTermType = vocabularyTermType;
	}

	public String getVocabularyTermType() {
		return vocabularyTermType;
	}


}
