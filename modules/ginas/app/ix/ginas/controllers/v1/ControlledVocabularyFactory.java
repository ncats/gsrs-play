package ix.ginas.controllers.v1;

import java.util.*;
import java.io.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.*;
import play.mvc.Http.MultipartFormData;
import play.db.ebean.*;
import play.mvc.*;
import ix.ginas.models.v1.*;
import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;

@NamedResource(name = "vocabularies", type = ControlledVocabulary.class, description = "Resource for handling of CV used in GInAS")
public class ControlledVocabularyFactory extends EntityFactory {
	static public final Model.Finder<Long, ControlledVocabulary> finder = new Model.Finder(
			Long.class, ControlledVocabulary.class);

	public static Result _internalServerError(Throwable t) {
		t.printStackTrace();
		return internalServerError(ix.ginas.views.html.error.render(500,
				"Internal server error: " + t.getMessage()));
	}

/*	private static Class<? extends VocabularyTerm> getTermClass (String domain){


*//*
		if(fragmentDomains.contains(domain)){
			return FragmentVocabularyTerm.class;
		}else if(codeSystemDomains.contains(domain)){
			return CodeSystemVocabularyTerm.class;
		}else{
			return VocabularyTerm.class;
		}*//*
	}*/

	private static Class<? extends ControlledVocabulary> getCVClass (String domain){
		Set<String> fragmentDomains = new HashSet<String>();
		fragmentDomains.add("NUCLEIC_ACID_SUGAR");
		fragmentDomains.add("NUCLEIC_ACID_LINKAGE");
		fragmentDomains.add("NUCLEIC_ACID_BASE");
		fragmentDomains.add("AMINO_ACID_RESIDUE");
		Set<String> codeSystemDomains = new HashSet<String>();
		codeSystemDomains.add("CODE_SYSTEM");
		codeSystemDomains.add("DOCUMENT_TYPE");

		if(fragmentDomains.contains(domain)){
			return FragmentControlledVocabulary.class;
		}else if(codeSystemDomains.contains(domain)){
			return CodeSystemControlledVocabulary.class;
		}else{
			return ControlledVocabulary.class;
		}
	}

	public static ControlledVocabulary getControlledVocabulary(String domain) {
		return finder.where().eq("domain", domain).findUnique();
	}
	
	public static String getDisplayFor(String domain, String value) {
		ControlledVocabulary cv=getControlledVocabulary(domain);
		if(cv!=null){
			for(VocabularyTerm t : cv.terms){
	        	if(t.value.equals(value)){
	        		return t.display;
	        	}
	        }
		}
		return null;
	}

	public static List<ControlledVocabulary> getDomain() {
		return finder.where().select("domain").findList();
	}

	// TODO: 7/7/16 Need to a function to delete the old cv before adding a new one
	public static void loadCVFile() {
		MultipartFormData requestData = request().body().asMultipartFormData();
		MultipartFormData.FilePart filepart = requestData.getFile("file-name");
		File file = filepart.getFile();
		try {
				FileInputStream is = new FileInputStream(file);
				loadCVJson(is);
		} catch (IOException ex) {
				ex.printStackTrace();
		}
	}

	public static void loadCVJson(InputStream is) {
		JsonFactory f = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper ();
		mapper.addHandler(new DeserializationProblemHandler() {
			public boolean handleUnknownProperty
					(DeserializationContext ctx, JsonParser parser,
					 JsonDeserializer deser, Object bean, String property) {
				try {
					Logger.warn("Unknown property \""
							+property+"\" (token="
							+parser.getCurrentToken()
							+") while parsing "
							+bean+"; skipping it..");
					parser.skipChildren();
				}catch (IOException ex) {
					Logger.error
							("Unable to handle unknown property!", ex);
					return false;
				}
				return true;
			}
		});

		try {
			JsonNode rawValues = mapper.readTree(is);
			for(JsonNode cvValue: rawValues){
				
				String domain=cvValue.at("/domain").asText();
				JsonNode vtype=cvValue.at("/vocabularyTermType");
				String termType=null;
				
				
				if(!vtype.isTextual()){
					ObjectNode objn = (ObjectNode)cvValue;
					//Sometimes stored as an object, instead of a text value
					objn.set("vocabularyTermType", cvValue.at("/vocabularyTermType/value"));
				}
				
				termType=cvValue.at("/vocabularyTermType").asText();
				
				ControlledVocabulary cv =  (ControlledVocabulary) mapper.treeToValue(cvValue, Class.forName(termType));
				//if there was an ID with this object, get rid of it
				//it was added by mistake
				cv.id=null;
				
				if(cv.terms!=null){ //Terms can be null sometimes now
					for(VocabularyTerm vt:cv.terms){
						vt.id=null;
					}
				}
				
				cv.setVocabularyTermType(getCVClass(domain).getName());
				cv.save();
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void loadSeedCV(InputStream is) {
		

		
		
		Map<String, List<VocabularyTerm>> map = new TreeMap<>();
		String line = "";
		String cvsSplitBy = "\t";
		ControlledVocabulary domains = new ControlledVocabulary();
		domains.domain="CV_DOMAIN";

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"));
			while ((line = br.readLine()) != null) {
				String[] cvTerm = line.split(cvsSplitBy);
				String category = cvTerm[0];
				String field = cvTerm[1];
				VocabularyTerm domainTerm = new VocabularyTerm();
				domainTerm.value = field;
				domainTerm.display = category;
				Logger.info("field " +field);
				VocabularyTerm cv;
				Class<? extends ControlledVocabulary> c = getCVClass(category);

				if(c == FragmentControlledVocabulary.class){
					cv= new FragmentVocabularyTerm();
					if (cvTerm.length >= 7) {
						((FragmentVocabularyTerm)cv).setSimplifiedStructure(cvTerm[6]);
					}
					if (cvTerm.length >= 8) {
						((FragmentVocabularyTerm)cv).setFragmentStructure(cvTerm[7]);
					}
				}else if(c == CodeSystemControlledVocabulary.class){
					cv= new CodeSystemVocabularyTerm();
					if (cvTerm.length >= 7) {
						((CodeSystemVocabularyTerm)cv).setSystemCategory(cvTerm[6]);
					}
					if (cvTerm.length >= 8) {
						((CodeSystemVocabularyTerm)cv).setRegex(cvTerm[7]);
					}
				}else{
					cv= new VocabularyTerm();
				}
				
				int l = cvTerm.length;
				if (l >= 3) {
					cv.value = cvTerm[2];
				} else {
					cv.value = null;
				}
				
				if (l >= 4) {
					cv.display = cvTerm[3];
				} else {
					cv.display = null;
				}
				if (l >= 5) {
					cv.description = cvTerm[4];
				} else {
					cv.description = null;
				}
				if (l >= 6) {
					cv.origin = cvTerm[5];
				} else {
					cv.origin = null;
				}
				
				List<VocabularyTerm> temp = map.get(category);
				if (temp == null) {
					temp = new ArrayList<VocabularyTerm>();
					map.put(category, temp);
					domains.terms.add(domainTerm);
				}
				temp.add(cv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Logger.debug("Pre loaded");
		for (String domain : map.keySet()) {
			ControlledVocabulary cv = new ControlledVocabulary();
			cv.domain = domain;
			cv.terms = map.get(domain);
			try{
				for(VocabularyTerm vt:cv.terms){
					vt.save();
				}
				cv.save();
				//System.err.println("Worked:" + domain);
			}catch(Exception e){
				//System.err.println("Failed:" + domain);
			}
			try{
				for(VocabularyTerm vt:domains.terms){
					vt.save();
				}
				domains.save();
				//System.err.println("Worked:" + domain);
			}catch(Exception e){
				//System.err.println("Failed:" + domain);
			}
		}
	}

	public static boolean isloaded() {
		return (finder.findRowCount()!=0);
	}

	public static int size() {
		return finder.all().size();
	}
	
	public static Result count () { return count (finder); }
	
	public static Integer getCount () {
	        try {
	            return getCount (finder);
	        }
	        catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return null;
	    }
	
	
	public static Result page (int top, int skip) {
        return page (top, skip, null);
    }

    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (Long uuid) {
        return edits (uuid, ControlledVocabulary.class);
    }

    public static Result get (Long uuid, String expand) {
        return get (uuid, expand, finder);
    }

    public static Result field (Long uuid, String path) {
        return field (uuid, path, finder);
    }

    public static Result create () {
    	
		try {
			if (!request().method().equalsIgnoreCase("POST")) {
				return badRequest ("Only POST is accepted!");
			}
			String content = request().getHeader("Content-Type");
			if (content == null || (content.indexOf("application/json") < 0
					&& content.indexOf("text/json") < 0)) {
				return badRequest ("Mime type \""+content+"\" not supported!");
			}
			JsonNode json = request().body().asJson();
			
			Class <ControlledVocabulary> c =ControlledVocabulary.class;

			
			JsonNode vocabType = json.get("vocabularyTermType");
			if(vocabType!=null && !vocabType.isNull() && !vocabType.isMissingNode()){
				c = (Class<ControlledVocabulary>)Class.forName(vocabType.asText());
			}
			
		
			return create (c, finder);
		}catch(Exception e){
			e.printStackTrace();
			return internalServerError(e.getMessage());
		}

	}






	public static Result delete (Long uuid) {
        return delete (uuid, finder);
    }

	public static Result updateEntity () {
		if (!request().method().equalsIgnoreCase("PUT")) {
			return badRequest ("Only PUT is accepted!");
		}
		String content = request().getHeader("Content-Type");
		if (content == null || (content.indexOf("application/json") < 0
				&& content.indexOf("text/json") < 0)) {
			return badRequest ("Mime type \""+content+"\" not supported!");
		}
		JsonNode json = request().body().asJson();
		String str = json.get("vocabularyTermType").asText();
		try {
			Class<? extends ControlledVocabulary> c = (Class<? extends ControlledVocabulary>)Class.forName(str);
			return updateEntity(json, c);
		}catch(Exception e){
			e.printStackTrace();
			return internalServerError(e.getMessage());
		}

	}


	public static Result update (Long uuid, String field) {
        return update (uuid, field, ControlledVocabulary.class, finder);
    }
}
