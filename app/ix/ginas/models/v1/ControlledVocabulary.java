package ix.ginas.models.v1;

import ix.core.models.IxModel;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


@Entity
@Table(name="ix_ginas_controlled_vocab")
public class ControlledVocabulary extends IxModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5455592961232451608L;

	@Column(unique=true)
	public String domain;
	
	@ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_cv_terms")
    //@JsonView(BeanViews.Full.class)
	public List<VocabularyTerm> terms;	

}



