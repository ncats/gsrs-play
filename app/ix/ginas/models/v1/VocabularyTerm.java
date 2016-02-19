package ix.ginas.models.v1;

import ix.core.models.IxModel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

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
	private static final long serialVersionUID = -5625533710493695789L;
	public String value;
    public String display;
    public String description;
    public String origin;
    public boolean hidden=false;
    public VocabularyTerm(){};
}
