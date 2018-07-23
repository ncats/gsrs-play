package ix.ginas.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import play.mvc.Result;
import play.libs.F;

import ix.core.controllers.search.SearchRequest;
import ix.core.search.SearchResult;
import ix.ginas.models.v1.Substance;
import ix.utils.RequestHelper;
import ix.utils.Tuple;

public enum ViewType{
	Substance("substance","Substances", "substances",FacetList.build()),
	Drug("drug","Drugs", "drugs",FacetList.build()
			                              .add("Substance Form","Principal Form")
			                              .add("Development Status","US Approved Rx")
			                              .add("Development Status","US Approved OTC")),


	Clinical("clinical","Clinical", "clinicals",FacetList.build()
            .add("Substance Form","Principal Form")
            .add("Development Status","Clinical")),


	Marketed("marketed","Marketed", "marketeds",FacetList.build()
            .add("Substance Form","Principal Form")
            .add("Development Status","US Unapproved, Marketed")
        //  .add("Development Status","Marketed")
            .add("Development Status","US Approved Rx")
            .add("Development Status","US Approved OTC"));
	
	private String plural;
	private String singular;
	private String resource;
	private List<Tuple<String,String>> facets = new ArrayList<Tuple<String,String>>();


	ViewType(String singular, String plural, String resource, FacetList flist){
		this.plural=plural;
		this.singular=singular;
		this.resource=resource;
		this.facets=flist.facets;
	}
	
	/*
	 * Title to be displayed
	 */
	public String getTitle() {
		return this.plural;
	}

	public String getSingular(){
		return this.singular;
	}
	

	public String getResourceName(){
		return this.resource;
	}
	
	public static ViewType fromSingular(String s){
		return Arrays.stream(ViewType.values())
    		      .filter(vt->vt.getSingular().equals(s))
    		      .findFirst()
    		      .orElse(null);
	}
	
	public List<Tuple<String,String>> getFacets(){
		return this.facets;
	}

	public void setRequestContext(){
		facets.stream()
		      .forEach(f->{
		    	  RequestHelper.addAdditionalParam("facet",f.k() + "/" + f.v());
		      });
	}
	
	public String getRequestContextAsUrlString(){
		return facets.stream()
	      .map(f->"facet=" + f.k() + "/" + f.v())
	      .collect(Collectors.joining("&"));
	}
	
	public F.Promise<Result> asSubstanceRedirect(String q, int rows, int page){
		String urlstart=ix.ginas.controllers.routes.GinasApp.substances(q, rows,page).toString();
    	if(urlstart.contains("?")){
    		urlstart+="&";
    	}else{
    		urlstart+="?";
    	}

    	StringBuilder urlBuilder = new StringBuilder(urlstart)
                .append(getRequestContextAsUrlString());
    	//2018-02-09 wrike+208909179
        //need to append any extra facets in the request we are redirecting from
        String[] additionlFacets = RequestHelper.request().queryString().get("facet");

    	if(additionlFacets !=null){

    	    for(String f : additionlFacets) {
                urlBuilder.append("&facet="+f);
            }
        }
    	String finalUrl = urlBuilder.toString();
    	return  F.Promise.promise(()->play.mvc.Controller.redirect(finalUrl));
	}
	
	public SearchRequest toSpecificRequest(SearchRequest sr1){

		facets.stream()
	      .forEach(f->{
	    	  sr1.getOptions().addFacet(f.k(),f.v());
	      });
		return sr1;
	}

	public long count(){
	    	try{
		    	SearchRequest request = new SearchRequest.Builder()
				 .kind(Substance.class)
		         .fdim(1)

		         .top(9999)
		         .skip(0)
		         .build();

		    	SearchResult result = toSpecificRequest(request).execute();

		        result.waitForFinish();
		        return result.getCount();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		return -1;
	    	}
	}

}