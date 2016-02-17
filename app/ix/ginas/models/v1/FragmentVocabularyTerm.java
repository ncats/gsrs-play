package ix.ginas.models.v1;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

/**
 * Created by peryeata on 6/29/15.
 */

@Entity
@Inheritance
@DiscriminatorValue("FRAG")
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
    
    
    /*
    NUCLEIC_ACID_SUGAR
    NUCLEIC_ACID_LINKAGE
    NUCLEIC_ACID_BASE
    AMINO_ACID_RESIDUE
    */
    
}
