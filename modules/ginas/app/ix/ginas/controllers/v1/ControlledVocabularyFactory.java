package ix.ginas.controllers.v1;

import java.util.*;
import java.io.*;

import com.fasterxml.jackson.databind.JsonNode;
import ix.ginas.utils.GinasProcessingStrategy;
import play.*;
import play.db.ebean.*;
import play.mvc.*;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.*;
import ix.core.NamedResource;

@NamedResource(name = "vocabularies", type = ControlledVocabulary.class, description = "Resource for handling of CV used in GInAS")
public class ControlledVocabularyFactory extends EntityFactory {
	static public final Model.Finder<Long, ControlledVocabulary> finder = new Model.Finder(
			Long.class, ControlledVocabulary.class);

	static public boolean isloaded=false;
	
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

	public static void loadSeedCV(InputStream is) {
		
		Set<String> fragmentDomains = new HashSet<String>();
		fragmentDomains.add("NUCLEIC_ACID_SUGAR");
		fragmentDomains.add("NUCLEIC_ACID_LINKAGE");
		fragmentDomains.add("NUCLEIC_ACID_BASE");
		fragmentDomains.add("AMINO_ACID_RESIDUE");
		Set<String> codeSystemDomains = new HashSet<String>();
		codeSystemDomains.add("CODE_SYSTEM");
		
		
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
				if(fragmentDomains.contains(category)){
					cv= new FragmentVocabularyTerm();
					if (cvTerm.length >= 7) {
						((FragmentVocabularyTerm)cv).setSimplifiedStructure(cvTerm[6]);
					}
					if (cvTerm.length >= 8) {
						((FragmentVocabularyTerm)cv).setFragmentStructure(cvTerm[7]);
					}
				}else if(codeSystemDomains.contains(category)){
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
		if(!isloaded){
			isloaded=!finder.all().isEmpty();
		}
		return isloaded;
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
        return create (ControlledVocabulary.class, finder);
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
		return updateEntity (ControlledVocabulary.class);
	}


	public static Result update (Long uuid, String field) {
        return update (uuid, field, ControlledVocabulary.class, finder);
    }
}
