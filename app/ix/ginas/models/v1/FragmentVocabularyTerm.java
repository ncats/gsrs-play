package ix.ginas.models.v1;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

import ix.core.SingleParent;

/**
 * Created by peryeata on 02/17/2016.
 */

@Entity
@Inheritance
@DiscriminatorValue("FRAG")
@SingleParent
public class FragmentVocabularyTerm extends VocabularyTerm{

	public String fragmentStructure;
	public String simplifiedStructure;
	
	public String getFragmentStructure() {
		return fragmentStructure;
	}

	public void setFragmentStructure(String fragmentStructure) {
		this.fragmentStructure = fragmentStructure;
	}
	
	public String getSimplifiedStructure() {
		return simplifiedStructure;
	}

	public void setSimplifiedStructure(String simplifiedStructure) {
		this.simplifiedStructure = simplifiedStructure;
	}
	
	public FragmentVocabularyTerm(){};
    
    
}
