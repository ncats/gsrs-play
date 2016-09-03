package ix.test.ix.test.server;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import ix.seqaln.SequenceIndexer.CutoffType;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.function.Function;

import static org.junit.Assert.assertTrue;

/**
 * Created by katzelda on 3/17/16.
 */
public class SubstanceAPI {

    private static final String API_URL_VALIDATE = "ginas/app/api/v1/substances/@validate";
    private static final String API_URL_SUBMIT_SUBSTANCE = "ginas/app/api/v1/substances";
    private static final String API_URL_SUBMIT_CV = "ginas/app/api/v1/vocabularies";
    private static final String API_URL_FETCH = "ginas/app/api/v1/substances($UUID$)?view=full";
    private static final String API_URL_HISTORY = "ginas/app/api/v1/substances($UUID$)/@edits";
    private static final String API_URL_MOL = "ginas/app/structure";

    private static final String API_URL_APPROVE = "ginas/app/api/v1/substances($UUID$)/@approve";
    private static final String API_URL_UPDATE = "ginas/app/api/v1/substances";

    private static final String API_URL_SUBSTANCES_SEARCH = "ginas/app/api/v1/substances/search";
    private static final String API_URL_STRUCTURE_BROWSE = "ginas/app/api/v1/structures";


    private static final String UI_URL_SUBSTANCE_SEARCH_TEXT="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE_SEARCH_FLEX="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE_SEARCH_SUB="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE_SEARCH_SEQ="ginas/app/sequence";
    private static final String UI_URL_SUBSTANCE_BROWSE="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE="ginas/app/substance/$ID$";
    private static final String UI_URL_SUBSTANCE_VERSION="ginas/app/substance/$ID$/v/$VERSION$";
    private static final String EXPORT_URL="ginas/app/export/$ID$.$FORMAT$";
    private final RestSession session;

    private final long timeout = 10_000L;

    public SubstanceAPI(RestSession session) {
        Objects.requireNonNull(session);
        this.session = session;
    }

    public RestSession getSession() {
        return session;
    }

    public JsonNode searchJson() {
        return session.getAsJson(API_URL_SUBSTANCES_SEARCH);
    }

    public WSResponse search() {
        return session.get(API_URL_SUBSTANCES_SEARCH);
    }


    public WSResponse submitSubstance(JsonNode js) {
        return session.createRequestHolder(API_URL_SUBMIT_SUBSTANCE).post(js).get(timeout);
    }

    public JsonNode submitSubstanceJson(JsonNode js) {
        return session.extractJSON(submitSubstance(js));
    }

    public WSResponse updateSubstance(JsonNode js) {
        return session.createRequestHolder(API_URL_UPDATE).put(js).get(timeout);

    }

    public JsonNode updateSubstanceJson(JsonNode js) {
        return session.extractJSON(updateSubstance(js));

    }

    public JsonNode substructureSearch(String smiles) throws IOException{

        WSResponse response = session.createRequestHolder("ginas/app/substances")
                .setQueryParameter("type", "Substructure")
                .setQueryParameter("q", smiles)
                .get()
                .get(timeout);

    ///ginas/app/substances?type=Substructure&q=C1%3DCC%3DCC%3DC1
        return session.extractJSON(response);
    }





    public WSResponse validateSubstance(JsonNode js){
        return session.createRequestHolder(API_URL_VALIDATE).post(js).get(timeout);
    }
    public JsonNode validateSubstanceJson(JsonNode js){
        return session.extractJSON( validateSubstance(js));
    }

    public WSResponse fetchSubstanceByUuid(String uuid){
        return session.createRequestHolder(API_URL_FETCH.replace("$UUID$", uuid)).get().get(timeout);
    }
    public JsonNode fetchSubstanceJsonByUuid(String uuid){
        return session.extractJSON(fetchSubstanceByUuid(uuid));
    }

    public WSResponse fetchSubstance(String id){
        return session.createRequestHolder(UI_URL_SUBSTANCE.replace("$ID$", id)).get().get(timeout);
    }
    public WSResponse fetchSubstance(String id, int version){
        return session.createRequestHolder(UI_URL_SUBSTANCE_VERSION.replace("$ID$", id).replace("$VERSION$", Integer.toString(version))).get().get(timeout);
    }

    public WSResponse approveSubstance(String uuid){
        return session.createRequestHolder(API_URL_APPROVE.replace("$UUID$", uuid)).get().get(timeout);
    }

    public WSResponse getFlexMatch(String smiles){
        return session.createRequestHolder(UI_URL_SUBSTANCE_SEARCH_FLEX)
        		.setQueryParameter("type", "flex")
                .setQueryParameter("q", smiles).get().get(timeout);
    }
    
    
    public WSResponse getTextSearch(String q){
        return session.createRequestHolder(UI_URL_SUBSTANCE_SEARCH_TEXT)
                .setQueryParameter("q", q).get().get(timeout);
    }
    
    public WSResponse getExactMatch(String smiles){
        return session.createRequestHolder(UI_URL_SUBSTANCE_SEARCH_FLEX)
        		.setQueryParameter("type", "exact")
                .setQueryParameter("q", smiles).get().get(timeout);
    }

    public String getTextSearchHTML(String q){
    	WSResponse wsr= getTextSearch(q);
    	return wsr.getBody();
    }
    
    
    
    public String getFlexMatchHTML(String smiles){
    	WSResponse wsr= getFlexMatch(smiles);
    	return wsr.getBody();
    }
    
    public String getExactMatchHTML(String smiles){
    	WSResponse wsr= getExactMatch(smiles);
    	return wsr.getBody();
    }

    public WSResponse getSubstructureMatch(String smiles){
        return session.createRequestHolder(UI_URL_SUBSTANCE_SEARCH_SUB)
        		.setQueryParameter("type", "Substructure")
                .setQueryParameter("q", smiles).get().get(timeout);
    }
    
    public String getSubstructureMatchHTML(String smiles){
    	WSResponse wsr= getSubstructureMatch(smiles);
    	return wsr.getBody();
    }
    
    public WSResponse getSequenceSearch(String seq, CutoffType cot, double cutoff){
    	return session.createRequestHolder(UI_URL_SUBSTANCE_SEARCH_SEQ)
    			.setContentType("application/x-www-form-urlencoded; charset=utf-8") 
    			.post("identityType=" + cot.toString() + "&sequence=" +seq + "&identity=" +cutoff 
    				//	+"&wait=true"
    					)
        		//.setQueryParameter("identityType", cot.toString())
        		//.setQueryParameter("identity", cutoff + "")
                //.setQueryParameter("sequence", seq)
                //.execute("POST")
                .get(timeout);
    }
    public String getSequenceSearchHTML(String seq, CutoffType cot, double cutoff){
    	WSResponse wsr= getSequenceSearch(seq, cot, cutoff);
    	return wsr.getBody();
    }
    
    public WSResponse instrumentMol(String mol){
        return session.createRequestHolder(API_URL_MOL).post(mol).get(timeout);
    }

    public JsonNode approveSubstanceJson(String uuid){
        return session.extractJSON( approveSubstance(uuid));
    }
    
    public JsonNode fetchInstrumentJson(String molfile){
    	return session.extractJSON( instrumentMol(molfile));
    	
    }
    

    public WSResponse fetchSubstanceHistory(String uuid, int version){
        return session.createRequestHolder(API_URL_HISTORY.replace("$UUID$", uuid))
                .setQueryParameter("filter","path=null AND version=\'" + version + "\'") //intentionally null
                .get().get(timeout);
    }


    public JsonNode fetchSubstancesSearchJSON() {
        return session.extractJSON(fetchSubstancesSearch());
    }
    public WSResponse fetchSubstancesSearch() {
        return session.createRequestHolder(API_URL_SUBSTANCES_SEARCH).get().get(timeout);
    }
    public WSResponse fetchSubstancesUIBrowse(boolean showDeprecated) {
        if(showDeprecated){
	    	return session.createRequestHolder(UI_URL_SUBSTANCE_BROWSE)
	    			.setQueryParameter("showDeprecated", "true")
	        		.get().get(timeout);
        }else{
        	return session.createRequestHolder(UI_URL_SUBSTANCE_BROWSE)
            		.get().get(timeout);
        		
    	}
    }
    
//    public static class BrowseRequest{
//    	
//    	
//    	public BrowseRequest(){}
//    	
//    	public BrowseRequest withParam(String k, String v){
//    		
//    	}
//    	
//    	
//    }
    
    public WSResponse fetchSubstancesUISearch(String searchString,String facet, String order) {
        Function<WSRequestHolder,WSRequestHolder> map = (k->k);
        
        if(searchString!=null){map=map.andThen(w->w.setQueryParameter("q", searchString));}
        if(facet!=null){map=map.andThen(w->w.setQueryParameter("facet", facet));}
        if(order!=null){map=map.andThen(w->w.setQueryParameter("order", order));}
        
        return map.apply(session.createRequestHolder(UI_URL_SUBSTANCE_BROWSE))
        		.get().get(timeout);
    }
    public String fetchSubstancesUISearchHTML(String searchString, String facet, String order) {
    	return fetchSubstancesUISearch(searchString,facet, order).getBody();
    }
    public String fetchSubstancesUIBrowseHTML() {
        return fetchSubstancesUIBrowse(false).getBody();
    }
    
    public String fetchSubstancesWithDeprecatedUIBrowseHTML() {
        return fetchSubstancesUIBrowse(true).getBody();
    }
    

    /**
     * Get the summary JSON which contains the oldValue and newValue URLs
     * for this version change.
     * @param uuid the UUID of the substance to fetch.
     *
     * @param version the version to of the substance to fetch.
     * @return the JsonNode , should not be null.
     */
    public JsonNode fetchSubstanceHistoryJson(String uuid, int version){
        return session.extractJSON(fetchSubstanceHistory(uuid, version));
    }

    public JsonHistoryResult fetchSubstanceJsonByUuid(String uuid, int version){
        JsonNode edits = fetchSubstanceHistoryJson(uuid,version);
        //should only have 1 edit...so this should be safe
        JsonNode edit = edits.iterator().next();
        JsonNode oldv= session.urlJSON(edit.get("oldValue").asText());
        JsonNode newv= session.urlJSON(edit.get("newValue").asText());



        return new JsonHistoryResult(edit, oldv, newv);
    }

	public WSResponse submitCVDomain(JsonNode newCV) {
		return session.createRequestHolder(API_URL_SUBMIT_CV).post(newCV).get(timeout);
	}
	public JsonNode submitCVDomainJson(JsonNode newCV) {
		return this.session.extractJSON(submitCVDomain(newCV));
	}

	public WSResponse export(String id, String format) {
		return session.createRequestHolder(EXPORT_URL.replace("$ID$", id).replace("$FORMAT$", format)).get().get(timeout);
	}
	
	public String exportHTML(String id, String format) {
		return export(id,format).getBody();
	}

	public JsonNode fetchStructureBrowseJSON() {
		return this.session.extractJSON(fetchStructureBrowse());
	}
	
	public int fetchStructureBrowseCount() {
		JsonNode jsn= this.fetchStructureBrowseJSON();
		return jsn.at("/total").asInt();
	}
	
	public WSResponse fetchStructureBrowse() {
		return session.createRequestHolder(API_URL_STRUCTURE_BROWSE).get().get(timeout);
	}


}
