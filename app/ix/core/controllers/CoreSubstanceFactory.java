package ix.core.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ix.core.util.CachedSupplier;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.utils.UUIDUtil;
import ix.utils.Util;

import play.Logger;
import play.db.ebean.Model;

public class CoreSubstanceFactory extends EntityFactory {
	private static final String CODE_TYPE_PRIMARY = "PRIMARY";
	static public CachedSupplier<Model.Finder<UUID, Substance>> finder = Util.finderFor(UUID.class, Substance.class);

	/**
	 * Get a Substance by it's UUID
	 * @param uuid
	 * @return
	 */
	public static Substance getSubstance(String uuid) {
		if (uuid == null ||!UUIDUtil.isUUID(uuid)) {
			return null;
	}
		return getSubstance(UUID.fromString(uuid));
	}
	public static Substance getSubstance(UUID uuid) {
		return getEntity(uuid, finder.get());
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
			Substance s = null;

			if(uuid != null){
				try{
					s=getSubstance(uuid);
				}catch(Exception e){
					e.printStackTrace();
				}
			}

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
}