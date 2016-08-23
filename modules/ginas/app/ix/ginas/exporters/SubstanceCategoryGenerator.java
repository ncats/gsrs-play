package ix.ginas.exporters;

import java.util.HashSet;
import java.util.Set;

import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;

public class SubstanceCategoryGenerator {
	static Set<String> ingredientCategoryTypes =  new HashSet<String>();
	
	static{
		ingredientCategoryTypes.add("IONIC MOIETY");
		ingredientCategoryTypes.add("MOLECULAR FRAGMENT");
		ingredientCategoryTypes.add("UNSPECIFIED INGREDIENT");
		ingredientCategoryTypes.add("SPECIFIED SUBSTANCE");
	}
	
	/**
	 * This returns a list of the "reflexive" relationship types,
	 * which are sometimes used for categorizing ingredient types.
	 * 
	 * @return
	 */
	public static Set<String> getIngredientCategoryFlags(Substance s){
		Set<String> types = new HashSet<String>();
		for(Relationship rs : s.relationships){
			String rtype = rs.type;
			if(ingredientCategoryTypes.contains(rtype)){
				types.add(rtype);
			}
		}
		return types;
	}
	
	public static String getIngredientCategory(Substance s){
		Set<String> types = getIngredientCategoryFlags(s);
		
		if(types.size()==0){
			return "INGREDIENT SUBSTANCE";
		}
		
		return types.iterator().next();
	}
}
