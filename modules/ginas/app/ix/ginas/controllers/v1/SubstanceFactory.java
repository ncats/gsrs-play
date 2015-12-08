package ix.ginas.controllers.v1;

import static ix.ncats.controllers.auth.Authentication.getUserProfile;
import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;
import ix.core.models.Group;
import ix.core.models.Principal;
import ix.core.models.Structure;
import ix.core.models.UserProfile;
import ix.core.models.Value;
import ix.ginas.controllers.GinasApp;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.utils.GinasV1ProblemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

@NamedResource(name="substances",
               type=Substance.class,
               description="Resource for handling of GInAS substances")
public class SubstanceFactory extends EntityFactory {
    static public final Model.Finder<UUID, Substance> finder =
        new Model.Finder(UUID.class, Substance.class);
    static public final Model.Finder<UUID, ChemicalSubstance> chemfinder =
            new Model.Finder(UUID.class, ChemicalSubstance.class);
    static public final Model.Finder<UUID, ProteinSubstance> protfinder =
            new Model.Finder(UUID.class, ProteinSubstance.class);

    public static Substance getSubstance (String id) {
        if(id==null)return null;
        return getSubstance (UUID.fromString(id));
    }

    public static Substance getSubstance (UUID uuid) {
        return getEntity (uuid, finder);
    }

    public static Result get (UUID id, String select) {
        return get (id, select, finder);
    }

    public static Substance getFullSubstance(SubstanceReference subRef){
        return getSubstanceByApprovalIDOrUUID(subRef.approvalID, subRef.refuuid);
    }
    
    private static Substance getSubstanceByApprovalIDOrUUID (String approvalID, String uuid) {
        Substance s=getSubstance(uuid);
        if(s!=null)return s;
        
        List<Substance> list=GinasApp.resolve(finder,approvalID);
        if(list!=null && list.size()>0){
                return list.get(0);
        }
        return null;
        //return finder.where().eq("approvalID", approvalID).findUnique();
    }

    public static List<Substance> getSubstances
        (int top, int skip, String filter) {
        SubstanceFilter subFilter = new SubstanceFilter();
        List<Substance> substances = filter (new FetchOptions (top, skip, filter), finder);
        return subFilter.filterByAccess(substances);
    }

    //TODO: Doesn't support top/skip
    public static List<Substance> getSubstancesWithExactName
    (int top, int skip, String name) {
                return finder.where().eq("names.name", name).findList();
        }
    
    //TODO: Doesn't support top/skip
    public static List<Substance> getSubstancesWithExactCode
    (int top, int skip, String code, String codeSystem) {
                return finder.where().and(com.avaje.ebean.Expr.eq("codes.code",code), com.avaje.ebean.Expr.eq("codes.codeSystem",codeSystem)).findList();
        }
        
    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Result count () {
        return count (finder);
    }

    public static Result page (int top, int skip) {
        return page (top, skip, null);
    }

    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (UUID uuid) {
        return edits (uuid, Substance.class);
    }

    public static Result getUUID (UUID uuid, String expand) {
        return get (uuid, expand, finder);
    }

    public static Result field (UUID uuid, String path) {
        return field (uuid, path, finder);
    }

    public static Result create () {
        return create (Substance.class, finder);
    }

    public static Result validate () {
        return validate (Substance.class, finder);
    }

    public static Result delete (UUID uuid) {
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

        Class<? extends Substance> subClass = Substance.class;
        String cls = json.get("substanceClass").asText();      
        try {
            Substance.SubstanceClass type =
                Substance.SubstanceClass.valueOf(cls);
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
        }
        catch (Exception ex) {
            Logger.warn("Unknown substance class: "+cls
                        +"; treating as generic substance!");
        }
        
        return updateEntity (json, subClass);
    }
    
    public static Result update (UUID uuid, String field) {
        //if(true)return ok("###");
        try {
            JsonNode value = request().body().asJson();
            Class subClass = Substance.class;
            String typ = value.get("substanceClass").asText();
            Substance.SubstanceClass type;
            try {
                type = Substance.SubstanceClass.valueOf(typ);
            } catch (Exception e) {
                throw new IllegalStateException("Unimplemented substance class:" + typ);
            }
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
            case structurallyDiverse:
                subClass = StructurallyDiverseSubstance.class;
                break;
            case specifiedSubstanceG1:
                subClass = SpecifiedSubstanceGroup1Substance.class;
                break;
            case concept:
                subClass = Substance.class;
                break;
            }
            return update(uuid, field, subClass, finder, new GinasV1ProblemHandler());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Substance> getCollsionChemicalSubstances(int i, int j,
                                                                ChemicalSubstance cs) {
        String hash=null;
        for (Value val : cs.structure.properties) {
            if (Structure.H_LyChI_L4.equals(val.label)) {
                hash=val.getValue()+"";
            }
        }
        return finder.where().eq("structure.properties.term", hash).findList();
    }


	public static class SubstanceFilter implements EntityFilter {

		UserProfile profile = getUserProfile();
		Principal user = profile != null ? profile.user : null;
		boolean access = false;

		public boolean hasAccess(Object grp, Object sub) {
			Group group = (Group) grp;
			Substance substance = (Substance) sub;
			return substance.getAccess().contains(group);
		}

		public List<Substance> filterByAccess(List<Substance> results) {
			List<Substance> filteredSubstances = new ArrayList<Substance>();

			for (Substance sub : results) {
				Set<Group> accessG = sub.getAccess();
				if (accessG == null || accessG.isEmpty() || accessG.size() == 0) {
					filteredSubstances.add(sub);
				}else{
					if (user != null) {
						for (Group grp : profile.getGroups()) {
							if (hasAccess(grp, sub)) {
								filteredSubstances.add(sub);
							}
						}
					}
				}
			}
			return filteredSubstances;
		}
	}
}
