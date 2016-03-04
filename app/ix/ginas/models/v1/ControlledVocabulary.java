package ix.ginas.models.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.models.Indexable;
import ix.core.models.IxModel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


@Entity
@Table(name="ix_ginas_controlled_vocab")
public class ControlledVocabulary extends IxModel{

	private static final long serialVersionUID = 5455592961232451608L;

	@JsonIgnore
	public Long getId(){
		return this.id;
	};

	@Column(unique=true)
	@Indexable(name="Domain", facet=true)
	public String domain;

	public boolean editable = true;

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


}
