package ix.ginas.utils.views.cards;

import java.util.ArrayList;

import ix.core.util.ConfigHelper;

public class FacetHelper {

	
	/**
	 * returns the default facets turned on for the supplied view
	 * @param viewName
	 * @return
	 */
	public static String[] getDefaultFacets(String viewName){
	        return ConfigHelper
	                .getStringList("ix.ginas.facets." + viewName + ".default", new ArrayList<String>())
	                .toArray(new String[0]);
	}
}
