package ix.ginas.controllers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.validator.GinasProcessingMessage;
import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.StructureFactory;
import ix.core.models.Edit;
import ix.core.models.Principal;
import ix.core.models.Structure;
import ix.core.models.UserProfile;
import ix.core.util.CachedSupplier;
import ix.core.util.Java8Util;
import ix.core.util.TimeUtil;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceClass;
import ix.ginas.models.v1.Unit;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.validation.ValidationUtils;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.UUIDUtil;
import ix.utils.Util;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Result;

public class GinasFactory extends EntityFactory {
	private static CachedSupplier<Model.Finder<Long, Principal>> finder=Util.finderFor(Long.class, Principal.class);
	private static CachedSupplier<Model.Finder<UUID, Unit>> unitFinder=Util.finderFor(UUID.class, Unit.class);

	private static final long EXPIRE_LOCK_TIME_MS = 1 * 60 * 1000; // one minute
	private static CachedSupplier<Map<String, EditLock>> currentlyEditing =
			CachedSupplier.of(()->new HashMap<String, EditLock>());


	public static Unit findUnitById(String uuidString) {
		return findUnitById(UUID.fromString(uuidString));
	}

	public static Unit findUnitById(UUID uuid) {
		return unitFinder.get().byId(uuid);
	}


	private static void addEditLock(EditLock el) {
		synchronized (currentlyEditing) {
			currentlyEditing.get().put(el.id, el);
		}
	}

	private static EditLock getEditLock(String id) {
		synchronized (currentlyEditing) {
			EditLock elock = currentlyEditing.get().get(id);
			if (elock == null || elock.isExpired()) {
				currentlyEditing.get().remove(id);
				return null;
			}
			return elock;
		}

	}

	public static class EditLock {
		String id;
		UserProfile user;
		long lockTime = TimeUtil.getCurrentTimeMillis();

		public EditLock(UserProfile user, String id) {
			this.id = id;
			this.user = user;
		}

		public boolean isExpired() {
			long expiretime = lockTime + EXPIRE_LOCK_TIME_MS;
			return TimeUtil.getCurrentTimeMillis() > expiretime;
		}

		public void updateLock() {
			lockTime = TimeUtil.getCurrentTimeMillis();
		}

		public boolean isUser(UserProfile up) {
			return this.user.getIdentifier().equals(up.getIdentifier());
		}
	}

	//TODO: move to Ginas App
	//***************
	/**
	 * @deprecated Use {@link GinasApp#index()} instead
	 */
	public static Result index() {
		return GinasApp.index();
	}

	/**
	 * @deprecated Use {@link GinasApp#app()} instead
	 */
	public static Result app() {
		return GinasApp.app();
	}

	/**
	 * @deprecated Use {@link GinasApp#register()} instead
	 */
	@Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result register() {
		return GinasApp.register();
	}
	//***************

	public static String getSmiles(String id) {
		return getSmiles(id, 0);
	}

	public static String getSmiles(String id, int max) {
		if (id != null) {
			String seq = null;
			if (!UUIDUtil.isUUID(id)) {
				seq = id;
			} else {
				Structure structure = StructureFactory.getStructure(id);
				if (structure != null) {
					seq = structure.smiles;
				}
			}

			if (seq != null) {
				seq = seq.replaceAll("[\n\t\\s]", "");
				if (max > 0 && max + 3 < seq.length()) {
					return seq.substring(0, max) + "...";
				}
				return seq;
			}
		}
		return id;
	}

	public static String getSequence(String id) {
		return getSequence(id, 0);
	}

	public static String getSequence(String id, int max) {
		if (id != null) {
			String seq = null;
			try {
				seq = PayloadFactory.getString(id);
			} catch (IllegalArgumentException e) {
				seq = id;
			}
			if (seq == null) {
				seq = EntityPersistAdapter.getSequenceIndexer().getSeq(id);
			}
			if (seq != null) {
				seq = seq.replaceAll("[\n\t\\s]", "");
				if (max > 0 && max + 3 < seq.length()) {
					return seq.substring(0, max) + "...";
				}
				return seq;
			}
		}
		return null;
	}

	/**
	 * @deprecated Use {@link GinasApp#sequence(String)} instead
	 */
	@Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result sequence(String id, String seqType) {
		return GinasApp.sequence(id,seqType);
	}

	/**
	 * @deprecated Use {@link GinasApp#structuresearch(String)} instead
	 */
	public static Result structuresearch(String q) {
		return GinasApp.structuresearch(q);
	}

	/**
	 * @deprecated Use {@link GinasApp#report()} instead
	 */
	@Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result report() {
		return GinasApp.report();
	}

	/**
	 * @deprecated Use {@link GinasApp#wizard(String)} instead
	 */
	@Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result wizard(String kind) {
		return GinasApp.wizard(kind);
	}

	public static class LockResponse {
		public boolean hasLock = false;

		public LockResponse(boolean haslock) {
			this.hasLock = haslock;
		}

		public static LockResponse HAS_LOCK() {
			return new LockResponse(true);
		}

		public static LockResponse DOES_NOT_HAVE_LOCK() {
			return new LockResponse(false);
		}
	}

	@Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result lock(String uuid) {
		UserProfile up = UserFetcher.getActingUserProfile(false);
		synchronized (currentlyEditing) {
			EditLock elock = getEditLock(uuid);
			LockResponse resp;
			if (elock == null) {
				EditLock newLock = new EditLock(up, uuid.toString());
				GinasFactory.addEditLock(newLock);
				resp = LockResponse.HAS_LOCK();
			} else if (elock.isUser(up)) {
				elock.updateLock();
				resp = LockResponse.HAS_LOCK();
			} else {
				resp = LockResponse.DOES_NOT_HAVE_LOCK();
				return ok("currently locked by another user");
			}
			EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();

			return Java8Util.ok(em.valueToTree(resp));
		}

	}
	
	public static Result edit(String substanceId) {
	    return edit(substanceId,null);
	}

	@Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result edit(String substanceId, String sclass) {
		List<Substance> substances = GinasApp.resolveName(substanceId);

		try {
			if (substances.size() == 1) {
				Substance s = substances.get(0);
				UUID uuid = s.getUuid();
				synchronized (currentlyEditing) {
					EditLock elock = getEditLock(uuid.toString());
					UserProfile up = UserFetcher.getActingUserProfile(false);
					if (elock == null) {
						EditLock newlock = new EditLock(up, uuid.toString());
						GinasFactory.addEditLock(newlock);
					} else {
						if (up != null && elock.user.getIdentifier().equals(up.getIdentifier())) {
							elock = null;
						}
						// there's a user editting this
					}
					if(sclass==null){
                        sclass=s.substanceClass.toString();
                    }
					s.substanceClass=SubstanceClass.valueOf(sclass);
					
					EntityMapper om = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
					String json = om.toJson(s);
					
					
					
					return ok(ix.ginas.views.html.wizard.render(sclass,
					        json,
							elock));
				}
			} else if (substances.size() == 0) {
				throw new IllegalStateException("No substance found matching that term");
			} else {
				throw new IllegalStateException("More than one substance found matching that term");
			}

		} catch (Exception ex) {
			return GinasApp._internalServerError(ex);
		}
	}
	
	

	/**
	 * @deprecated Use {@link GinasApp#approve(String)} instead
	 */
	@Dynamic(value = IxDynamicResourceHandler.CAN_APPROVE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result approve(String substanceId) {
		List<Substance> substances = SubstanceFactory.resolve(substanceId);

		try {
			if (substances.size() == 1) {
				Substance s = substances.get(0);
				Substance sapproved=SubstanceFactory.approve(s);
    			String resp = "Substance approved with approvalID:" + sapproved.getApprovalID();
				return ok(ix.ginas.views.html.response.render(resp));
			}
			throw new IllegalStateException("More than one substance matches that term");
		} catch (Exception ex) {
			return GinasApp._internalServerError(ex);
		}
	}

	public static Principal byUsername(String user) {
		return finder.get().where().eq("username", user).findUnique();
	}

	public static Principal registerIfAbsent(String user) {
		Principal p = byUsername(user);
		if (p == null) {
			p = new Principal();
			p.username = user;
			p.save();
		}
		return p;
	}

	@Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result validateChemicalDuplicates() {
		String mappingsjson = extractSubstanceJSON();
		Substance sub = null;
		List<GinasProcessingMessage> messages = new ArrayList<GinasProcessingMessage>();

		try {
			System.out.println(mappingsjson);
			GinasUtils.GinasJSONExtractor ex = new GinasUtils.GinasJSONExtractor(mappingsjson);
			JsonNode jn = ex.getNextRecord();
			sub = GinasUtils.makeSubstance(jn);
			if (sub instanceof ChemicalSubstance) {
				messages.addAll(ValidationUtils.validateAndPrepareChemical((ChemicalSubstance) sub,
						GinasProcessingStrategy.ACCEPT_APPLY_ALL()));
			} else {
				messages.add(GinasProcessingMessage.ERROR_MESSAGE("Subsance is not a chemical substance"));
			}
		} catch (IllegalStateException e) {
			messages.add(GinasProcessingMessage.ERROR_MESSAGE(e.getMessage()));
		} catch (UnsupportedEncodingException e) {
			messages.add(GinasProcessingMessage.ERROR_MESSAGE("Problem decoding JSON:" + e.getMessage()));
		} catch (Exception e) {
			messages.add(GinasProcessingMessage.ERROR_MESSAGE("Problem decoding JSON:" + e.getMessage()));
		}
		if (GinasProcessingMessage.ALL_VALID(messages)) {
			messages.add(GinasProcessingMessage.SUCCESS_MESSAGE("Structure is valid and unique"));
		}
		ObjectMapper om = new ObjectMapper();
		return Java8Util.ok(om.valueToTree(messages));
	}

	public static String extractSubstanceJSON() {
		String mappingsjson = null;
		try {
			mappingsjson = request().body().asJson().toString();
		} catch (Exception e) {
			DynamicForm requestData = Form.form().bindFromRequest();
			mappingsjson = requestData.get("substance");
		}
		return mappingsjson;
	}

	// This won't typically work, as it will collide with existing CV
	/**
	 * @deprecated Use {@link GinasApp#loadCV()} instead
	 */
	public static Result loadCV() {
		return GinasApp.loadCV();
	}

	
	public static List<Edit> getEdits(Substance sub){
		return SubstanceFactory.getEdits(sub.uuid);
	}
	
	
}
