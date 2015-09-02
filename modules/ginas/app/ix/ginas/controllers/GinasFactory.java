package ix.ginas.controllers;

import java.util.List;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.models.Principal;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasFactory extends EntityFactory {
	public static final Model.Finder<Long, Principal> finder = new Model.Finder(
			Long.class, Principal.class);

	public static Result index() {
		GinasApp.getCV();
		return ok(ix.ginas.views.html.index.render());
	}

	public static Result app() {
		return ok(ix.ginas.views.html.index.render());
	}

	public static Result register() {
		return ok(ix.ginas.views.html.register.render());
	}

	public static String getSequence(String id) {
		return getSequence(id, 0);
	}

	public static String getSequence(String id, int max) {
		String seq = PayloadFactory.getString(id);
		if (seq != null) {
			seq = seq.replaceAll("[\n\t\\s]", "");
			if (max > 0 && max + 3 < seq.length()) {
				return seq.substring(0, max) + "...";
			}
			return seq;
		}
		return null;
	}

	public static Result sequence(String id) {
		return ok(ix.ginas.views.html.sequence.render(id));
	}

	public static Result structuresearch() {
		return ok(ix.ginas.views.html.structuresearch.render());
	}

	public static Result report() {
		return ok(ix.ginas.views.html.report.render());
	}

	public static Result wizard(String kind) {
		Logger.info(kind);
		return ok(ix.ginas.views.html.wizard.render(kind,null));
	}

	public static Result edit(String substanceId) {
		List<Substance> substances = GinasApp.resolve(SubstanceFactory.finder,
				substanceId);

		try {
			if (substances.size() == 1) {
				ObjectMapper om = new ObjectMapper();
				String json = om.valueToTree(substances.get(0)).toString();
				return ok(ix.ginas.views.html.wizard.render(
						substances.get(0).substanceClass.toString(), json));
			}
			throw new IllegalStateException("More than one substance matches that term");
		} catch (Exception ex) {
			return GinasApp._internalServerError(ex);
		}
	}

	public static Result login() {
		return ok(ix.ginas.views.html.login.render());
	}

	public static Principal byUsername(String user) {
		// return GinasApp.SubstanceResult
		return finder.where().eq("username", user).findUnique();
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

}
