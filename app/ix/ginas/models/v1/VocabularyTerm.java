package ix.ginas.models.v1;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.SingleParent;
import ix.core.models.IxModel;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.serialization.KeywordDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;

/**
 * Created by sheilstk on 6/29/15.
 */

@Entity
@Table(name="ix_ginas_vocabulary_term")
@Inheritance
@DiscriminatorValue("VOCAB")
@SingleParent
public class VocabularyTerm extends IxModel{
	/**
	 * 
	 */
	@ManyToOne(cascade = CascadeType.PERSIST)
	private ControlledVocabulary owner;


	/**
	 * At some point, this was set to be ignored.
	 *
	 * It's not clear why it was, but it's no longer ignored now
	 * to allow for reordering on saves
	 *
	 * @return
	 */
	//@JsonIgnore
	public Long getId(){
		return this.id;
	};

	private static final long serialVersionUID = -5625533710493695789L;
	@Column(length=4000)
	public String value;

	@Column(length=4000)
	public String display;

	@Column(length=4000)
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


	@PreUpdate
	public void updateImmutables(){
		this.filters= new EmbeddedKeywordList(this.filters);
	}

}
