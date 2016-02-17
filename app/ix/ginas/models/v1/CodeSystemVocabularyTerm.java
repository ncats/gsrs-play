package ix.ginas.models.v1;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

/**
 * Created by peryeata on 6/29/15.
 */

@Entity
@Inheritance
@DiscriminatorValue("SYS")
public class CodeSystemVocabularyTerm extends VocabularyTerm{
    
    String systemCategory;
    String regex;
    
		
	
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		if(regex.equals("")){
			regex=null;
		}else{
			this.regex = regex;
		}
	}
	public String getSystemCategory() {
		return systemCategory;
	}
	public void setSystemCategory(String systemCategory) {
		this.systemCategory = systemCategory;
	}
	
	
	public CodeSystemVocabularyTerm(){};
    
}
