package ix.ginas.exporters;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;

public class SubstanceCategoryGenerator implements SingleColumnValueRecipe<Substance> {
	static Set<String> ingredientCategoryTypes =  new HashSet<String>();
	
	@Override
	public int writeHeaderValues(Spreadsheet.SpreadsheetRow row, int currentOffset) {
		return 0;
	}

	static{
		ingredientCategoryTypes.add("IONIC MOIETY");
		ingredientCategoryTypes.add("MOLECULAR FRAGMENT");
		ingredientCategoryTypes.add("UNSPECIFIED INGREDIENT");
		ingredientCategoryTypes.add("SPECIFIED SUBSTANCE");
	}
	
	@Override
	public boolean containsColumnName(String name) {
		return false;
	}

	@Override
	public ColumnValueRecipe<Substance> replaceColumnName(String oldName, String newName) {
		return null;
	}

	/**
	 * This returns a list of the "reflexive" relationship types,
	 * which are sometimes used for categorizing ingredient types.
	 * 
	 * @return
	 */
	private static Set<String> getIngredientCategoryFlags(Substance s){
		Set<String> types = new TreeSet<String>();
		for(Relationship rs : s.relationships){
			String rtype = rs.type;
			if(ingredientCategoryTypes.contains(rtype)){
				types.add(rtype);
			}
		}
		return types;
	}
	
	private static String getIngredientCategory(Substance s){
		Set<String> types = getIngredientCategoryFlags(s);
		
		if(types.size()==0){
			return "INGREDIENT SUBSTANCE";
		}
		
		return types.iterator().next();
	}

	@Override
	public void writeValue(Substance object, SpreadsheetCell cell) {
		String val = getIngredientCategory(object);
		cell.writeString(val);
	}
}
