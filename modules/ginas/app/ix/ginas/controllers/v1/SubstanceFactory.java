package ix.ginas.controllers.v1;

import java.util.*;
import java.util.stream.Collectors;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;

import ix.core.NamedResource;
import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.EditFactory;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.v1.RouteFactory;
import ix.core.models.Edit;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import ix.core.util.EntityUtils;
import ix.core.util.TimeUtil;
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
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.Subunit;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.GinasV1ProblemHandler;
import ix.ginas.utils.validation.DefaultSubstanceValidator;
import ix.seqaln.SequenceIndexer;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.seqaln.SequenceIndexer.ResultEnumeration;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

@NamedResource(name = "substances", type = Substance.class, description = "Resource for handling of GInAS substances")
public class SubstanceFactory extends EntityFactory {
	private static final String CODE_TYPE_PRIMARY = "PRIMARY";
	private static final double SEQUENCE_IDENTITY_CUTOFF = 0.85;
	static public Model.Finder<UUID, Substance> finder;

	// Do we still need these?
	// Yes used in GinasApp
	static public Model.Finder<UUID, ProteinSubstance> protfinder;

	static {
		init();
	}

	public static void init() {
		finder = new Model.Finder(UUID.class, Substance.class);
		protfinder = new Model.Finder(UUID.class, ProteinSubstance.class);
	}

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
		
		//TODO: make generic somewhere
		// for instance, couldn't edit fetching happen even on 
		// the EnityWrapper?
		// It would be awesome if we could say something 
		// on any given record like:
		//	.getEdits()
		//	.getVersion(int i)
		// etc ...
		//
		// Instead, it's quite hodge-podge now
		
		List<Expression> kindExpressions=
			Arrays.stream(Substance.getAllClasses())
					.map(c -> Expr.eq("kind",c.getName()))
					.collect(Collectors.toList());
				
		
		Query<Edit> q = EditFactory.finder.where(andAll(Expr.eq("refid", id.toString()),
												  orAll(kindExpressions.toArray(new Expression[0])),
												  		Expr.eq("version", version), 
												  		Expr.isNull("path")));
		Edit e=q.findUnique(); //AH!
		System.out.println("edit: " + e);
		try{
			//Good idea? Maybe, Maybe not.
			return (Substance) EntityUtils.getEntityInfoFor(e.kind).fromJson(e.oldValue);
		}catch(Exception e1){
			e1.printStackTrace();
		}

		return null;

	}

	public static Substance getSubstance(UUID uuid) {
		return getEntity(uuid, finder);
	}

	public static Result get(UUID id, String select) {
		return get(id, select, finder);
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
		sublist = finder.where()
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
		List<Substance> list = finder.where().ieq("approvalID", approvalID).findList();
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	public static String getMostRecentCode(String codeSystem, String like) {
		List<Substance> subs = finder.where()
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
		List<Substance> substances = filter(new FetchOptions(top, skip, filter), finder);
		return substances;
	}

	// TODO: Doesn't support top/skip
	public static List<Substance> getSubstancesWithExactName(int top, int skip, String name) {
		return finder.where().eq("names.name", name).findList();
	}

	// TODO: Doesn't support top/skip
	public static List<Substance> getSubstancesWithExactCode(int top, int skip, String code, String codeSystem) {
		return finder.where(andAll(
				 com.avaje.ebean.Expr.eq("codes.code", code),
				 com.avaje.ebean.Expr.eq("codes.codeSystem", codeSystem),
				 com.avaje.ebean.Expr.eq("codes.type", CODE_TYPE_PRIMARY)
				))
				.findList();
	}

	public static Integer getCount() {
		try {
			return getCount(finder);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Result count() {
		return count(finder);
	}

	public static Result page(int top, int skip) {
		return page(top, skip, null);
	}

	public static Result page(int top, int skip, String filter) {
		return page(top, skip, filter, finder);
	}

	public static Result edits(UUID uuid) {
		return edits(uuid, Substance.getAllClasses());
	}

	public static Result getUUID(UUID uuid, String expand) {
		return get(uuid, expand, finder);
	}

	public static Result field(UUID uuid, String path) {
		return field(uuid, path, finder);
	}

	public static Result create() {

		JsonNode value = request().body().asJson();
		Class subClass = getClassFromJson(value);
		DefaultSubstanceValidator sv = DefaultSubstanceValidator
				.NEW_SUBSTANCE_VALIDATOR(GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS().markFailed());
		
		return create(subClass, finder, sv);
	}

	public static Result validate() {
		JsonNode value = request().body().asJson();
		Class subClass = getClassFromJson(value);
		DefaultSubstanceValidator sv = new DefaultSubstanceValidator(
				GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS_MARK_FAILED());
		return validate(subClass, finder, sv);
	}

	public static Result delete(UUID uuid) {
		return delete(uuid, finder);
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

	public static Result update(UUID uuid, String field) {
		DefaultSubstanceValidator sv = DefaultSubstanceValidator
				.UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS());

		// if(true)return ok("###");
		try {
			JsonNode value = request().body().asJson();
			Class subClass = getClassFromJson(value);
			return update(uuid, field, subClass, finder, new GinasV1ProblemHandler(), sv);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// silly test
	// silly test
	public static List<Substance> getCollsionChemicalSubstances(int top, int skip, ChemicalSubstance cs) {
		// System.out.println("Dupe chack");
		String hash = cs.structure.getLychiv4Hash();
		List<Substance> dupeList = new ArrayList<Substance>();
		dupeList = finder.where().eq("structure.properties.term", hash).setFirstRow(skip).setMaxRows(top).findList();
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
				List<Substance> proteins = SubstanceFactory.finder.where().eq("protein.subunits.uuid", r.id).findList();
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
			Substance s = finder.byId(UUID.fromString(name));
			if (s != null) {
				List<Substance> retlist = new ArrayList<Substance>();
				retlist.add(s);
				return retlist;
			}
		} catch (Exception e) {

		}

		List<Substance> values = new ArrayList<Substance>();
		if (name.length() == 8) { // might be uuid
			values = finder.where().istartsWith("uuid", name).findList();
		}

		if (values.isEmpty()) {
			values = finder.where().ieq("approvalID", name).findList();
			if (values.isEmpty()) {
				values = finder.where().ieq("names.name", name).findList();
				if (values.isEmpty()) // last resort..
					values = finder.where().ieq("codes.code", name).findList();
			}
		}

		if (values.size() > 1) {
			Logger.warn("\"" + name + "\" yields " + values.size() + " matches!");
		}
		return values;
	}

	public static synchronized void approveSubstance(Substance s) {

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
		if (s.isNonSubstanceConcept()) {
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
}
