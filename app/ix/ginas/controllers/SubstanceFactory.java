package ix.ginas.controllers;

import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;
import ix.core.plugins.EutilsPlugin;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;

import java.util.List;

import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Result;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@NamedResource(name="substances",
type=Substance.class,
description="Resource for handling ofginas substances")
public class SubstanceFactory extends EntityFactory {
	public static final Model.Finder<Long, ChemicalSubstance> csfinder = 
			new Model.Finder(Long.class, ChemicalSubstance.class);
	public static final Model.Finder<Long, Substance> finder = 
			new Model.Finder(Long.class, Substance.class);
	public static final EutilsPlugin eutils =
			Play.application().plugin(EutilsPlugin.class);

	public static List<Substance> all () { return all (finder); }
	public static Result count () { return count (finder); }
	public static Result page (int top, int skip, String filter) {
		return page (top, skip, filter, finder);
	}

	public static List<Substance> filter (int top, int skip) {
		return filter (top, skip, null);
	}

	public static List<Substance> filter (int top, int skip, String filter) {
		return filter (new FetchOptions (top, skip, filter), finder);
	}

	public static List<Substance> filter (FetchOptions options) {
		return filter (options, finder);
	}

	public static List<Substance> filter (JsonNode json, int top, int skip) {
		return filter (json, top, skip, finder);
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

	public static Result get (Long id, String expand) {
		String type = request().getQueryString("type");
		if (type != null) {
			if ("unii".equalsIgnoreCase(type)) {
				Query<Substance> q = finder.query();
				if (expand != null) {
					q = q.fetch(expand);
				}
				//unii may be for multiple substances
				Substance s = q.where().eq("unii", id).findUnique();
				if (s != null) {
					ObjectMapper mapper = getEntityMapper ();
					return ok (mapper.valueToTree(s));
				}

				return notFound ("Not found: "+request().uri());
			}

			return badRequest ("Unknown type: "+type);
		}
		return get (id, expand, finder);
	}

	public static ChemicalSubstance byUnii (String unii) {
		return csfinder.where().eq("unii", unii).findUnique();
	}    

	public static Result edits (Long id) {
		return edits (id, Substance.class);
	}

	public static Substance getSubstance (Long id) {
		return getEntity (id, finder);
	}

	public static Result field (Long id, String path) {
		return field (id, path, finder);
	}

	public static Result create () {
		return create (Substance.class, finder);
	}

	public static Result delete (Long id) {
		return delete (id, finder);
	}

	public static Result update (Long id, String field) {
		return update (id, field, Substance.class, finder);
	}

	public static ChemicalSubstance registerIfAbsent (String unii) {
	    ChemicalSubstance cs = byUnii (unii);
		if (cs == null) {
			Logger.info("null object ---creating new Chemical Substance");	
		}
		else if(cs != null) {
	Logger.info("returning Chemical Structure for updating");
			}
		return cs;
	}

	public static Substance getEntity( long id){
		return getEntity (id, finder);
	}
}
