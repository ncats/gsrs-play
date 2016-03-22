package ix.ginas.controllers;


import be.objectify.deadbolt.java.actions.Dynamic;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
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

        private static final long EXPIRE_LOCK_TIME_MS = 1*60*1000; //one minute
        private static ConcurrentHashMap<String,EditLock> currentlyEditing;
        
        public static void init(){
        	currentlyEditing =  new ConcurrentHashMap<String,EditLock>();
        }
        
        private static void addEditLock(EditLock el){
        	if(currentlyEditing==null){
        		currentlyEditing=  new ConcurrentHashMap<String,EditLock>();
        	}
        	currentlyEditing.put(el.id, el);
        }
        private static EditLock getEditLock(String id){
        	if(currentlyEditing==null){
        		currentlyEditing=  new ConcurrentHashMap<String,EditLock>();
        	}
        	EditLock elock= currentlyEditing.get(id);
        	if(elock==null || elock.isExpired()){
        		currentlyEditing.remove(id);
        		return null;
        	}else{
        		return elock;
        	}
        	
        }
        public static class EditLock{
        	String id;
        	UserProfile user;
        	Date lockTime = new Date();
        	public EditLock(UserProfile user, String id){
        		this.id=id;
        		this.user=user;
        	}
        	public boolean isExpired(){
        		long expiretime = lockTime.getTime()+EXPIRE_LOCK_TIME_MS;
        		if(System.currentTimeMillis()>expiretime){
        			return true;
        		}
        		return false;
        	}
        	public void updateLock(){
        		lockTime=new Date();
        	}
			public boolean isUser(UserProfile up) {
				if(this.user.getIdentifier().equals(up.getIdentifier())){
					return true;
				}else{
					return false;
				}
			}
        }
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
                return ok(ix.ginas.views.html.wizard.render(kind,"{}",null));
        }
        public static class LockResponse{
        	public boolean hasLock=false;
        	public LockResponse(boolean haslock){
        		this.hasLock=haslock;
        	}
        	public static LockResponse HAS_LOCK(){
        		return new LockResponse(true);
        	}
        	public static LockResponse DOES_NOT_HAVE_LOCK(){
        		return new LockResponse(false);
        	}
        }
        @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
        public static Result lock(String uuid) {
        	UserProfile up =UserFetcher.getActingUserProfile(false);
        	EditLock elock = getEditLock(uuid);
        	LockResponse resp = LockResponse.DOES_NOT_HAVE_LOCK();
        	if(elock!=null && elock.isUser(up)){
        		elock.updateLock();
        		resp = LockResponse.HAS_LOCK();
        	}else if(elock==null){
        		EditLock newelock = new EditLock(UserFetcher.getActingUserProfile(false),uuid.toString());
    			GinasFactory.addEditLock(newelock);
    			resp = LockResponse.HAS_LOCK();
        	}else{
        		resp = LockResponse.DOES_NOT_HAVE_LOCK();
        		return ok("currently locked by another user");
        	}
        	EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
        	
    		return ok(em.valueToTree(resp));
        	
        }

        @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
        public static Result edit(String substanceId) {
                List<Substance> substances = GinasApp.resolve(SubstanceFactory.finder,
                                substanceId);

                try {
                        if (substances.size() == 1) {
                        		Substance s=substances.get(0);
                        		UUID uuid=s.getUuid();
                        		EditLock elock = getEditLock(uuid.toString());
                        		if(elock==null){
                        			EditLock newelock = new EditLock(UserFetcher.getActingUserProfile(false),uuid.toString());
                        			GinasFactory.addEditLock(newelock);
                        		}else{
                        			UserProfile up =UserFetcher.getActingUserProfile(false);
                        			
                        			if(up!=null && elock.user.getIdentifier().equals(up.getIdentifier())){
                        				elock=null;
                        			}
                        			//there's a user editting this
                        		}
                        		EntityMapper om = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
                                String json = om.toJson(s);
                                return ok(ix.ginas.views.html.wizard.render(
                                                substances.get(0).substanceClass.toString(), json, elock));
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
