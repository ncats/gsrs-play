package ix.ginas.controllers;


import be.objectify.deadbolt.java.actions.Dynamic;
import java.util.List;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.models.Principal;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasUtils;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
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
                return ok(ix.ginas.views.html.index.render());
        }

        public static Result app() {
                return ok(ix.ginas.views.html.index.render());
        }

        @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
        public static Result register() {
                return ok(ix.ginas.views.html.register.render());
        }

        public static String getSequence(String id) {
                return getSequence(id, 0);
        }

        public static String getSequence(String id, int max) {
            if (id != null) {
                String seq=null;
                try{
                    seq = PayloadFactory.getString(id);
                }catch(IllegalArgumentException e){
                    seq=id;
                }
                if(seq==null) {
                    seq = EntityPersistAdapter
                        .getSequenceIndexer().getSeq(id);
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

        @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
        public static Result sequence(String id) {
                return ok(ix.ginas.views.html.sequence.render(id));
        }

        public static Result structuresearch() {
                return ok(ix.ginas.views.html.structuresearch.render());
        }

       @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	    public static Result report() {
                return ok(ix.ginas.views.html.report.render());
        }
        @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
        public static Result wizard(String kind) {
                Logger.info(kind);
                return ok(ix.ginas.views.html.wizard.render(kind,"{}"));
        }

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
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
        
        @Dynamic(value = IxDynamicResourceHandler.CAN_APPROVE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
        public static Result approve(String substanceId) {
                List<Substance> substances = SubstanceFactory.resolve(
                                substanceId);

                try {
                        if (substances.size() == 1) {
                                Substance s=substances.get(0);
                                SubstanceFactory.approveSubstance(s);
                                s.save();
                                return ok("Substance approved with approvalID:" + s.approvalID);
                        }
                        throw new IllegalStateException("More than one substance matches that term");
                } catch (Exception ex) {
                        return GinasApp._internalServerError(ex);
                }
        }

        public static Principal byUsername(String user) {
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
