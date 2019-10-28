package ix.test.server;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidationMessage;
import ix.core.controllers.EntityFactory;
import ix.core.util.EntityUtils;
import ix.ginas.modelBuilders.AbstractSubstanceBuilder;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.JsonSubstanceFactory;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.test.SubstanceJsonUtil;
import ix.ginas.modelBuilders.SubstanceBuilder;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

/**
 * Created by katzelda on 3/17/16.
 */
public class SubstanceAPI {

    //TODO: Refactor
    
    private static final String API_URL_VALIDATE = "ginas/app/api/v1/substances/@validate";
    private static final String API_URL_SUGGEST = "ginas/app/api/v1/suggest";
    private static final String API_URL_SUBMIT_SUBSTANCE = "ginas/app/api/v1/substances";
    private static final String API_URL_SUBMIT_CV = "ginas/app/api/v1/vocabularies";

    private static final String API_URL_VOCABULARIES_FETCH = "ginas/app/api/v1/vocabularies($ID$)?view=full";

    private static final String API_URL_FETCH = "ginas/app/api/v1/substances($UUID$)?view=full";
    private static final String API_URL_FETCH_BASIC = "ginas/app/api/v1/substances($UUID$)";
    private static final String API_URL_HISTORY = "ginas/app/api/v1/substances($UUID$)/@edits";
    private static final String API_URL_MOL = "ginas/app/structure";

    private static final String API_URL_APPROVE = "ginas/app/api/v1/substances($UUID$)/@approve";
    private static final String API_URL_UPDATE = "ginas/app/api/v1/substances";

    private static final String API_URL_SUBSTANCES_SEARCH = "ginas/app/api/v1/substances/search";
    public static final String API_URL_STRUCTURE_BROWSE = "ginas/app/api/v1/structures";


    private static final String UI_URL_SUBSTANCE_SEARCH_TEXT="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE_SEARCH_FLEX="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE_SEARCH_SUB="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE_SEARCH_SEQ="ginas/app/sequence";
    private static final String UI_URL_SUBSTANCE_BROWSE="ginas/app/substances";
    private static final String UI_URL_SUBSTANCE="ginas/app/substance/$ID$";
    private static final String UI_URL_SUBSTANCE_VERSION="ginas/app/substance/$ID$/v/$VERSION$";
    private static final String EXPORT_URL="ginas/app/export/$ID$.$FORMAT$";
    private static final String IMAGE_URL="ginas/app/img/$ID$.$FORMAT$";


    private static final JsonPointer VALIDATION_MESSAGE_PATH = JsonPointer.valueOf("/validationMessages");


    private final RestSession session;

    private final long timeout = 10_000L;
    private final long update_timeout = 60_000L;
    
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

    public JsonNode submitSubstance(Consumer<? super AbstractSubstanceBuilder> substanceFunction){
        SubstanceBuilder b = new SubstanceBuilder();
        substanceFunction.accept(b);

        JsonNode[] ret = new JsonNode[1];

        b.buildJsonAnd(j -> {
            ret[0] = j;
            ensurePass(submitSubstance(j));
        });
        return ret[0];
    }
    public void submitSubstance(AbstractSubstanceBuilder substanceBuilder){
        substanceBuilder.buildJsonAnd(j -> ensurePass(submitSubstance((JsonNode) j)));
    }

    public JsonNode submitSubstance(Substance s){
        return ensurePass(submitSubstance(EntityUtils.EntityWrapper.of(s).toFullJsonNode()));
    }

    public JsonNode submitSubstanceJson(JsonNode js) {
        return session.extractJSON(submitSubstance(js));
    }

    public WSResponse updateSubstance(JsonNode js) {
        return session.createRequestHolder(API_URL_UPDATE).put(js).get(update_timeout);

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


    public Substance fetchSubstanceObjectByUuid(String uuid){

        return JsonSubstanceFactory.makeSubstance(fetchSubstanceJsonByUuid(uuid));
    }


    public <T extends Substance> T fetchSubstanceObjectByUuid(String uuid, Class<T> substancetype){
        try {
            return  EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().treeToValue(fetchSubstanceByUuid(uuid).asJson(), substancetype);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("error processing json", e);
        }

    }
    public ValidationResponse validateSubstance(JsonNode js){
        return new ValidationResponse(session.createRequestHolder(API_URL_VALIDATE).post(js).get(timeout));
    }

    public WSResponse fetchSubstanceByUuid(String uuid){
        return session.createRequestHolder(API_URL_FETCH.replace("$UUID$", uuid)).get().get(timeout);
    }
    
    public WSResponse fetchCVById(String id){
        return session.createRequestHolder(API_URL_VOCABULARIES_FETCH.replace("$ID$", id)).get().get(timeout);
    }
    public JsonNode fetchCVJsonByUuid(String id){
        return session.extractJSON(fetchCVById(id));
    }

    public String fetchSubstanceLychiv4ByUuid(String uuid){
    	return session.extractJSON(session.createRequestHolder(API_URL_FETCH_BASIC.replace("$UUID$", uuid)+"/structure/properties(label:LyChI_L4)!(term)($0)").get().get(timeout)).asText();
    }
    public JsonNode fetchSubstanceJsonByUuid(UUID uuid) {
        return fetchSubstanceJsonByUuid(uuid.toString());
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
    
    public JsonNode getSuggestPrefixJson(String uuid){
        return session.extractJSON( getSuggestPrefix(uuid));
    }
    
    public WSResponse getSuggestPrefix(String term){
    	 return session.createRequestHolder(API_URL_SUGGEST)
         			.setQueryParameter("q", term).get().get(timeout);
    }
    //API_URL_SUGGEST
    
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
                .get()
                .get(timeout);
    }
    
    
    public WSResponse fetchAllSubstanceHistory(String uuid){
        return session.createRequestHolder(API_URL_HISTORY.replace("$UUID$", uuid))
                .get().get(timeout);
    }

    public JsonNode fetchSubstancesSearchFacetsJSON() {
        WSResponse response= session.createRequestHolder(API_URL_SUBSTANCES_SEARCH + "/@facets").get().get(timeout);
        return session.extractJSON(response);
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
        
        assertEquals(1, edits.size());

		JsonNode edit = edits.iterator().next();
		if (	  (edit.at("/path").isMissingNode() || edit.at("/path").isNull())
				&& edit.at("/version").asText().equals(version + "")) {
			JsonNode oldv = session.urlJSON(edit.get("oldValue").asText());
			JsonNode newv = session.urlJSON(edit.get("newValue").asText());
			return new JsonHistoryResult(edit, oldv, newv);
		}
		throw new NoSuchElementException("No edit found with version:" + version);
    }

	public WSResponse submitCVDomain(JsonNode newCV) {
		return session.createRequestHolder(API_URL_SUBMIT_CV).post(newCV).get(timeout);
	}

	public WSResponse updateCVDomain(JsonNode newCV) {
		return session.createRequestHolder(API_URL_SUBMIT_CV).put(newCV).get(timeout);
	}
	public JsonNode submitCVDomainJson(JsonNode newCV) {
		return this.session.extractJSON(submitCVDomain(newCV));
	}

	public JsonNode updateCVDomainJson(JsonNode newCV) {
		return this.session.extractJSON(updateCVDomain(newCV));
	}

	public WSResponse export(String id, String format) {
		return session.createRequestHolder(EXPORT_URL.replace("$ID$", id).replace("$FORMAT$", format)).get().get(timeout);
	}
	
	public String exportHTML(String id, String format) {
		return export(id,format).getBody();
	}
	
	public WSResponse image(String id, String format, int size) {
		return session.createRequestHolder(IMAGE_URL.replace("$ID$", id).replace("$FORMAT$", format) + "?size=" + size)
				.get()
				.get(timeout);

	}
	
	public String imageSVG(String id, int size) {
		return image(id,"svg",size).getBody();

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
	
	
	public SubstanceBrowseResult fetchSubstanceBrowseResult(){
		return new DefaultSubstanceBrowseResult(session.createRequestHolder(UI_URL_SUBSTANCE_BROWSE).setQueryParameter("wait", "true"));
	}
	
	public interface SubstanceBrowsePage{
		public int getRecordsOnPage();
		public String getHtml();
	}
	
	public interface SubstanceBrowseResult{
		public int getPageCount();
		public int getRecordCount();
		public SubstanceBrowsePage getPage(int p);
		default SubstanceBrowsePage getLastPage(){
			return getPage(getPageCount());
		}
	}
	
	public static class DefaultSubstanceBrowsePage implements SubstanceBrowsePage{
		String html;
		
		public DefaultSubstanceBrowsePage(String html){
			this.html=html;
		}
		@Override
		public int getRecordsOnPage() {
			throw new RuntimeException ("Not yet able to do this");
		}

		@Override
		public String getHtml() {
			return html;
		}
		
	}
	
	public class DefaultSubstanceBrowseResult implements SubstanceBrowseResult{
		int pageCount;
		int recordCount;
		WSRequestHolder ws;
		boolean loaded=false;
		
		
		public DefaultSubstanceBrowseResult(WSRequestHolder ws){
			this.ws=ws;
			
		}
		
		private void refresh(){
			String html = ws.get().get(timeout).getBody();
			recordCount=getRecordCountFromHtml(html);
			pageCount=((recordCount-1)/16)+1; //TODO: not a safe assumption
			loaded=true;
		}
		
		@Override
		public int getPageCount() {
			if(!loaded)refresh();
			return pageCount;
		}

		@Override
		public int getRecordCount() {
			if(!loaded)refresh();
			return recordCount;
		}

		@Override
		public SubstanceBrowsePage getPage(int p) {
			if(!loaded)refresh();
			String html = ws
	    			.setQueryParameter("page", p + "")
	        		.get().get(timeout).getBody();
			return new DefaultSubstanceBrowsePage(html);
		}
		
	}
	
	private static int getRecordCountFromHtml(String html){
    	String recStart = "<span id=\"record-count\" class=\"label label-default\">";
    	int io=html.indexOf(recStart);
    	int ei=html.indexOf("<", io + 3);
    	if(ei>0 && io >0){
    		String c=html.substring(io + recStart.length(),ei);
    		try{
    		return Integer.parseInt(c.trim());
    		}catch(Exception e){}
    	}
    	return -1;
    }


    public final class ValidationResponse{
        private final WSResponse response;

        private JsonNode js;


        public ValidationResponse(WSResponse response) {
            this.response = response;
        }

        public int getHttpStatus(){
            return response.getStatus();
        }
        public boolean isValid(){
            if(getHttpStatus() !=200){
                return false;
            }
            if(response.getBody().isEmpty()){
                return false;
            }
            return SubstanceJsonUtil.isValid(asJson());
        }

        public boolean isNull(){
            return SubstanceJsonUtil.isLiteralNull(asJson());
        }

        public JsonNode asJson(){
            if(js ==null) {
                js= session.extractJSON(response);
            }
            return js;
        }

        public List<ValidationMessage> getMessages(){
            List<ValidationMessage> list = new ArrayList<>();
            try {
                for(JsonNode node : asJson().at(VALIDATION_MESSAGE_PATH)){
                    list.add(new ObjectMapper().treeToValue(node, GinasProcessingMessage.class));
                }
               return list;
            }catch(Exception e){
                throw new IllegalStateException("error unmarshalling json",e);
            }
        }

        @Override
        public String toString() {
            return "ValidationResponse{" +
                    "response=" + response +
                    ", js=" + asJson() +
                    "\nmessages = " + getMessages() +
                    '}';
        }
        
        
        public void assertValid(){
        	try{
        		assertTrue(this.isValid());
        	}catch(Throwable t){
        		String msgblob=getMessages().stream()
        			.map(s->s.getMessageType() + ":" + s.getMessage())
        			.collect(Collectors.joining("\n"));
        		assertTrue(msgblob, this.isValid());
        		throw t;
        	}
        }
    }

}
