package ix.ginas.controllers.v1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.NamedResource;
import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.chem.StructureProcessor;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.controllers.search.SearchRequest;
import ix.core.controllers.v1.RouteFactory;
import ix.core.models.Edit;
import ix.core.models.Principal;
import ix.core.models.Structure;
import ix.core.models.UserProfile;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.ResultProcessor;
import ix.core.search.SearchResult;
import ix.core.search.SearchResultContext;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.util.Java8Util;
import ix.core.util.TimeUtil;
import ix.core.util.pojopointer.PojoPointer;
import ix.ginas.controllers.GinasApp.StructureSearchResultProcessor;
import ix.ginas.controllers.GinasApp;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceClass;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.Subunit;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.GinasV1ProblemHandler;
import ix.ginas.utils.validation.DefaultSubstanceValidator;
import ix.ncats.controllers.App;
import ix.ncats.controllers.App.SearcherTask;
import ix.seqaln.SequenceIndexer;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.seqaln.SequenceIndexer.ResultEnumeration;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Result;

@NamedResource(name = "substances", type = Substance.class, description = "Resource for handling of GInAS substances")
public class SubstanceFactory extends EntityFactory {
	private static final String CODE_TYPE_PRIMARY = "PRIMARY";
	private static final double SEQUENCE_IDENTITY_CUTOFF = 0.85;
	static public CachedSupplier<Model.Finder<UUID, Substance>> finder = Util.finderFor(UUID.class, Substance.class);

	// Do we still need this?
	// Yes used in GinasApp
	static public CachedSupplier<Model.Finder<UUID, ProteinSubstance>> protfinder=Util.finderFor(UUID.class, ProteinSubstance.class);

	public static Substance getSubstance(String id) {
		if (id == null)
			return null;
		return getSubstance(UUID.fromString(id));
	}

	public static Substance getSubstanceVersion(String id, String version) {
		if (id == null)
			return null;

		Substance s = SubstanceFactory.getSubstance(id);
		if (s != null) {
			if (s.version.equals(version)) {
				return s;
			}
		}
		return EntityWrapper.of(s)
				.getEdits()
				.stream()
				.filter(e->(version.equals(e.version))) //version -1 is the right thing
				.findFirst()
				.map(e->{
					try{
						return (Substance) EntityUtils
							.getEntityInfoFor(e.kind)
							.fromJsonNode(e.getOldValue().rawJson());
					}catch(Exception ex){
						throw new IllegalArgumentException(ex);
					}
				}).orElse(null);
	}

	public static Substance getSubstance(UUID uuid) {
		return getEntity(uuid, finder.get());
	}

	public static Result get(UUID id, String select) {
		return get(id, select, finder.get());
	}

	public static Substance getFullSubstance(SubstanceReference subRef) {
		try {
			if (subRef == null)
				return null;
			return getSubstanceByApprovalIDOrUUID(subRef.approvalID, subRef.refuuid);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	public static List<Substance> getSubstanceWithAlternativeDefinition(Substance altSub) {
		List<Substance> sublist = new ArrayList<Substance>();
		sublist = finder.get().where()
				.and(com.avaje.ebean.Expr.eq("relationships.relatedSubstance.refuuid",
						altSub.getOrGenerateUUID().toString()),
				com.avaje.ebean.Expr.eq("relationships.type", Substance.ALTERNATE_SUBSTANCE_REL)).findList();

		List<Substance> realList = new ArrayList<Substance>();
		for (Substance sub : sublist) {
			for (SubstanceReference sref : sub.getAlternativeDefinitionReferences()) {
				if (sref.refuuid.equals(altSub.getUuid().toString())) {
					realList.add(sub);
					break;
				}
			}
		}
		return realList;
	}

	/**
	 * Returns the substance corresponding to the supplied uuid or approvalID.
	 * 
	 * If either is null, it will not be used in resolving. This method returns
	 * first based on the UUID, and falls back to the approvalID if nothing is
	 * found.
	 * 
	 * @param approvalID
	 * @param uuid
	 * @return
	 */
	private static Substance getSubstanceByApprovalIDOrUUID(String approvalID, String uuid) {
		try {
			if (approvalID == null && uuid == null)
				return null;

			Substance s = (uuid != null) ? getSubstance(uuid) : null;
			if (s == null && approvalID != null) {
				s = getSubstanceByApprovalID(approvalID);
			}
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		// return finder.where().eq("approvalID", approvalID).findUnique();
	}

	public static Substance getSubstanceByApprovalID(String approvalID) {
		List<Substance> list = finder.get().where().ieq("approvalID", approvalID).findList();
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	public static String getMostRecentCode(String codeSystem, String like) {
		List<Substance> subs = finder.get().where()
				.and(com.avaje.ebean.Expr.like("codes.code", like),
						com.avaje.ebean.Expr.eq("codes.codeSystem", codeSystem))
				.orderBy("codes.code").setMaxRows(1).findList();
		List<String> retCodes = new ArrayList<String>();
		if (subs != null) {
			if (subs.size() >= 1) {
				Substance sub = subs.get(0);
				for (Code c : sub.codes) {
					if (c.codeSystem.equals(codeSystem)) {
						retCodes.add(c.code);
					}
				}
			}
		}
		if (retCodes.size() == 0)
			return null;
		Collections.sort(retCodes);
		return retCodes.get(0);
	}

	public static List<Substance> getSubstances(int top, int skip, String filter) {
		List<Substance> substances = filter(new FetchOptions(top, skip, filter), finder.get());
		return substances;
	}

	// TODO: Doesn't support top/skip
	public static List<Substance> getSubstancesWithExactName(int top, int skip, String name) {
		return finder.get().where().eq("names.name", name).findList();
	}

	// TODO: Doesn't support top/skip
	public static List<Substance> getSubstancesWithExactCode(int top, int skip, String code, String codeSystem) {
		return finder.get().where(Util.andAll(
				 com.avaje.ebean.Expr.eq("codes.code", code),
				 com.avaje.ebean.Expr.eq("codes.codeSystem", codeSystem),
				 com.avaje.ebean.Expr.eq("codes.type", CODE_TYPE_PRIMARY)
				))
				.findList();
	}

	public static Integer getCount() {
		try {
			return getCount(finder.get());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Result count() {
		return count(finder.get());
	}

	
	public static Result stream(String field, int top, int skip){
		return stream(field, top, skip, finder.get());
	}
	
	public static Result page(int top, int skip) {
		return page(top, skip, null);
	}

	public static Result page(int top, int skip, String filter) {
		return page(top, skip, filter, finder.get());
	}

	public static Result edits(UUID uuid) {
		return edits(uuid, Substance.getAllClasses());
	}

	public static Result getUUID(UUID uuid, String expand) {
		return get(uuid, expand, finder.get());
	}

	public static Result field(UUID uuid, String path) {
		return field(uuid, path, finder.get());
	}

	public static Result create() {

		JsonNode value = request().body().asJson();
		Class subClass = getClassFromJson(value);
		DefaultSubstanceValidator sv = DefaultSubstanceValidator
				.NEW_SUBSTANCE_VALIDATOR(GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS().markFailed());
		
		return create(subClass, finder.get(), sv);
	}

	public static Result validate() {
		JsonNode value = request().body().asJson();
		Class subClass = getClassFromJson(value);
		DefaultSubstanceValidator sv = new DefaultSubstanceValidator(
				GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS_MARK_FAILED());
		return validate(subClass, finder.get(), sv);
	}

	public static Result delete(UUID uuid) {
		return delete(uuid, finder.get());
	}

	public static Class<? extends Substance> getClassFromJson(JsonNode json) {
		Class<? extends Substance> subClass = Substance.class;

		String cls = null;

		try {
			cls = json.get("substanceClass").asText();
			Substance.SubstanceClass type = Substance.SubstanceClass.valueOf(cls);
			switch (type) {
			case chemical:
				subClass = ChemicalSubstance.class;
				break;
			case protein:
				subClass = ProteinSubstance.class;
				break;
			case mixture:
				subClass = MixtureSubstance.class;
				break;
			case polymer:
				subClass = PolymerSubstance.class;
				break;
			case nucleicAcid:
				subClass = NucleicAcidSubstance.class;
				break;
			case structurallyDiverse:
				subClass = StructurallyDiverseSubstance.class;
				break;
			case specifiedSubstanceG1:
				subClass = SpecifiedSubstanceGroup1Substance.class;
				break;
			case concept:
			default:
				subClass = Substance.class;
				break;
			}
		} catch (Exception ex) {
			Logger.warn("Unknown substance class: " + cls + "; treating as generic substance!");
			// throw ex;
		}
		return subClass;
	}

	public static Result updateEntity() {
		DefaultSubstanceValidator sv = DefaultSubstanceValidator
				.UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS());

		if (!request().method().equalsIgnoreCase("PUT")) {
			return badRequest("Only PUT is accepted!");
		}

		String content = request().getHeader("Content-Type");
		if (content == null || (content.indexOf("application/json") < 0 && content.indexOf("text/json") < 0)) {
			return badRequest("Mime type \"" + content + "\" not supported!");
		}
		JsonNode json = request().body().asJson();

		Class<? extends Substance> subClass = getClassFromJson(json);
		return updateEntity(json, subClass, sv);
	}
	
	
	public static Result patch(UUID uuid) throws Exception {
	    DefaultSubstanceValidator sv = DefaultSubstanceValidator
                .UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS());
	    
	    Key k = Key.of(Substance.class, uuid);
	    return EntityFactory.patch(k, sv);
	}
	
	
	
	public static Result update(UUID uuid, String field) throws Exception {
		DefaultSubstanceValidator sv = DefaultSubstanceValidator
				.UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS());
		if (!request().method().equalsIgnoreCase("PUT")) {
            return badRequest("Only PUT is accepted!");
        }

        String content = request().getHeader("Content-Type");
        if (content == null || (content.indexOf("application/json") < 0 && content.indexOf("text/json") < 0)) {
            return badRequest("Mime type \"" + content + "\" not supported!");
        }
		
		// if(true)return ok("###");
		try {
			JsonNode value = request().body().asJson();
			Class<? extends Substance> subClass = getClassFromJson(value);
			System.out.println("Got:" + value.toString());
			Key k=Key.of(subClass, uuid);
			PojoPointer pp = PojoPointer.fromURIPath(field);
			
			return EntityFactory.updateField(k, pp, value, sv);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// silly test
	public static List<Substance> getCollsionChemicalSubstances(int top, int skip, ChemicalSubstance cs) {
		// System.out.println("Dupe chack");
		String hash = cs.structure.getLychiv4Hash();
		List<Substance> dupeList = new ArrayList<Substance>();
		dupeList = finder.get().where().eq("structure.properties.term", hash).setFirstRow(skip).setMaxRows(top).findList();
		return dupeList;
	}
	

	public static SequenceIndexer getSeqIndexer() {
		return EntityPersistAdapter.getSequenceIndexer();
	}

	public static List<Substance> getNearCollsionProteinSubstancesToSubunit(int top, int skip, Subunit subunit) {
		Set<Substance> dupes = new LinkedHashSet<Substance>();
		try {
			ResultEnumeration re = getSeqIndexer().search(subunit.sequence, SubstanceFactory.SEQUENCE_IDENTITY_CUTOFF,
					CutoffType.GLOBAL);
			int i = 0;
			while (re.hasMoreElements()) {
				SequenceIndexer.Result r = re.nextElement();
				List<Substance> proteins = SubstanceFactory.finder.get().where().eq("protein.subunits.uuid", r.id).findList();
				if (proteins != null && !proteins.isEmpty()) {

					for (Substance s : proteins) {
						if (dupes.size() >= top) {
							break;
						}
						if (i >= skip) {
							dupes.add(s);
						}
						i++;

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<Substance>(dupes);
	}

	public static Substance approve(Substance s){
		EntityUtils.EntityWrapper<Substance> changed= EntityPersistAdapter.performChangeOn(s, csub->{
			SubstanceFactory.approveSubstance(csub);
			csub.save();
			return Optional.of(csub);
		});
		if(changed==null){
			throw new IllegalStateException("Approval encountered an error");
		}else{
			return changed.getValue();
		}
	}

	public static Result approve(String substanceId) {

		List<Substance> substances = SubstanceFactory.resolve(substanceId);

		try {
			if (substances.size() == 1) {
				Substance s = substances.get(0);
				Substance sapproved= approve(s);
				return ok(EntityUtils.EntityWrapper.of(sapproved).toFullJsonNode());
			}
			throw new IllegalStateException("More than one substance matches that term");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalStateException();
		}
	}

	public static Result approve(UUID substanceId) {
		return approve(substanceId.toString());
	}

	public static List<Substance> resolve(String name) {
		if (name == null) {
			return null;
		}

		try {
			Substance s = finder.get().byId(UUID.fromString(name));
			if (s != null) {
				List<Substance> retlist = new ArrayList<Substance>();
				retlist.add(s);
				return retlist;
			}
		} catch (Exception e) {

		}

		List<Substance> values = new ArrayList<Substance>();
		if (name.length() == 8) { // might be uuid
			values = finder.get().where().istartsWith("uuid", name).findList();
		}

		if (values.isEmpty()) {
			values = finder.get().where().ieq("approvalID", name).findList();
			if (values.isEmpty()) {
				values = finder.get().where().ieq("names.name", name).findList();
				if (values.isEmpty()) // last resort..
					values = finder.get().where().ieq("codes.code", name).findList();
			}
		}

		if (values.size() > 1) {
			Logger.warn("\"" + name + "\" yields " + values.size() + " matches!");
		}
		return values;
	}

	private static synchronized void approveSubstance(Substance s) {

		UserProfile up = UserFetcher.getActingUserProfile(false);
		Principal user = null;
		if (s.status == Substance.STATUS_APPROVED) {
			throw new IllegalStateException("Cannot approve an approved substance");
		}
		if (up == null || up.user == null) {
			throw new IllegalStateException("Must be logged in user to approve substance");
		}
		user = up.user;
		if (s.getLastEditedBy() == null) {
			throw new IllegalStateException(
					"There is no last editor associated with this record. One must be present to allow approval. Please contact your system administrator.");
		} else {
			if (s.getLastEditedBy().username.equals(user.username)) {
				throw new IllegalStateException(
						"You cannot approve a substance if you are the last editor of the substance.");
			}
		}
		if (!s.isPrimaryDefinition()) {
			throw new IllegalStateException("Cannot approve non-primary definitions.");
		}
		if (s.substanceClass.equals(SubstanceClass.concept)) {
			throw new IllegalStateException("Cannot approve non-substance concepts.");
		}
		for (SubstanceReference sr : s.getDependsOnSubstanceReferences()) {
			Substance s2 = SubstanceFactory.getFullSubstance(sr);
			if (s2 == null) {
				throw new IllegalStateException("Cannot approve substance that depends on " + sr.toString()
						+ " which is not found in database.");
			}
			if (!s2.isValidated()) {
				throw new IllegalStateException(
						"Cannot approve substance that depends on " + sr.toString() + " which is not approved.");
			}
		}

		s.approvalID = GinasUtils.getAPPROVAL_ID_GEN().generateID();
		s.approved = TimeUtil.getCurrentDate();
		s.approvedBy = user;
		s.status = Substance.STATUS_APPROVED;
	}
	
	public static List<Edit> getEdits(UUID uuid) {
		return getEdits(uuid, Substance.getAllClasses());
	}
	
	public static Result structureSearch(String q, 
										 String type, 
										 double cutoff, 
										 int top, 
										 int skip, 
										 int fdim,
										 String field) throws Exception{
		SearchResultContext context;
		
		if(type.toLowerCase().startsWith("sub")){
			context = App.substructure(q, 
					/*min=*/ 1, 
					new StructureSearchResultProcessor())
					.getFocused(top, skip, fdim, field);
		}else if(type.toLowerCase().startsWith("sim")){
			context = App.similarity(q, 
					cutoff,
					/*min=*/ 1, 
					new StructureSearchResultProcessor())
					.getFocused(top, skip, fdim, field);
		}else if(type.toLowerCase().startsWith("fle")){
		    //I don't like this section of the code
		    
		    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		    //NEEDS CLEANUP
		    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Structure struc2 = StructureProcessor.instrument(q, null, true); // don't
                                                                             // standardize
            String hash = struc2.getLychiv3Hash();
            SearchRequest request = new SearchRequest.Builder()
                   .kind(Substance.class)
                   .fdim(fdim)
                   .query(hash)
                   .top(Integer.MAX_VALUE)
                   .build()
                   ;
            TextSearchTask task = new TextSearchTask(request);
            context = App.search(task, task.getProcessor());
            
            
		}else if(type.toLowerCase().startsWith("exa")){
		    //I don't like this section of the code
            
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //NEEDS CLEANUP
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Structure struc2 = StructureProcessor.instrument(q, null, true); // don't
                                                                             // standardize
            String hash = "root_structure_properties_term:" + struc2.getLychiv4Hash();
            SearchRequest request = new SearchRequest.Builder()
                   .kind(Substance.class)
                   .fdim(fdim)
                   .query(hash)
                   .top(Integer.MAX_VALUE)
                   .build()
                   ;
            TextSearchTask task = new TextSearchTask(request);
            context = App.search(task, task.getProcessor());
        }else{
			throw new UnsupportedOperationException("Unsupported search type:" + type);
		}
		
        return detailedSearch(context);
	}
	
	
    public static Result sequenceSearch(String q, CutoffType type, double cutoff, int top, int skip, int fdim,
            String field) throws Exception {
        SearchResultContext context;
        context =App.sequence(q, cutoff,type, 1, new ix.ginas.controllers.GinasApp.GinasSequenceResultProcessor())
                    .getFocused(top, skip, fdim, field);
        
        return detailedSearch(context);
    }
    
    private static Result detailedSearch(SearchResultContext context) throws InterruptedException, ExecutionException{
        context.setAdapter((srequest, ctx) -> {
            try {
                SearchResult sr = App.getResultFor(ctx, srequest);
                
                List<Substance> rlist = new ArrayList<Substance>();
                
                sr.copyTo(rlist, srequest.getOptions().getSkip(), srequest.getOptions().getTop(), true); // synchronous
                for (Substance s : rlist) {
                    s.setMatchContextFromID(ctx.getId());
                }
                return sr;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Error fetching search result", e);
            }
        });

        String s = play.mvc.Controller.request().getQueryString("sync");

        if ("true".equals(s) || "".equals(s)) {
            try {
                context.getDeterminedFuture().get(1, TimeUnit.MINUTES);
                return redirect(context.getResultCall());
            } catch (TimeoutException e) {
                Logger.warn("Structure search timed out!", e);
            }
        }
        return Java8Util.ok(EntityFactory.getEntityMapper().valueToTree(context));
    }

    private static class TextSearchTask implements SearcherTask{

        private SearchRequest request;
        private String key;
        
        
        public TextSearchTask(SearchRequest request){
            this.request=request;
            String q = request.getQuery();
            request.getOptions().asQueryParams();
            key=Util.sha1("search" + q, request.getOptions().asQueryParams(), 
                                        "kind",
                                        "filter",
                                        "facet",
                                        "order", 
                                        "fdim");
        }
        @Override
        public String getKey() {
            return key;
        }

        @Override
        public void search(ResultProcessor processor) throws Exception {
            //do nothing. Nothing needs to be processed
        }

        @Override
        public long getLastUpdatedTime() {
            return Play.application().plugin(TextIndexerPlugin.class).getIndexer().lastModified();
        }
        
        
        public ResultProcessor getProcessor(){
            return new SearchResultWrappingResultProcessor(this.request);
        }
    }
    
    private static class SearchResultWrappingResultProcessor implements ResultProcessor<Object, Object>{
        private CachedSupplier<SearchResultContext> result;
        
        public SearchResultWrappingResultProcessor(SearchRequest request){
            result=CachedSupplier.ofCallable(()->{
                return new SearchResultContext(SearchFactory.search(request)); 
            });
        }
        @Override
        public Stream map(Object result) {
               throw new UnsupportedOperationException(this.getClass() + " doesn't support mapping");
        }

        @Override
        public SearchResultContext getContext() {
                return result.get();
        }

        @Override
        public void setUnadaptedResults(Iterator results) {
            throw new UnsupportedOperationException(this.getClass() + " doesn't support setting an iterator");            
        }

        @Override
        public Iterator getUnadaptedResults() {
            throw new UnsupportedOperationException(this.getClass() + " doesn't support getting an iterator");
        }
        
    }
    
    
    
}
