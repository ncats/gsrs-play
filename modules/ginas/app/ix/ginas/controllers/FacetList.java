package ix.ginas.controllers;

import java.util.ArrayList;
import java.util.List;

import ix.utils.Tuple;

public class FacetList{
	List<Tuple<String,String>> facets = new ArrayList<Tuple<String,String>>();

	public FacetList add(String fName, String fValue){
		facets.add(Tuple.of(fName,fValue));
		return this;
	}
	public static FacetList build(){
		return new FacetList();
	}

}