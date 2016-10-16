package ix.ncats.controllers.security;

import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import ix.core.models.Role;
import ix.core.util.CachedSupplier;
import play.Logger;
import play.Play;
import play.mvc.Http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class IxDynamicResourceHandler implements DynamicResourceHandler {
	public static final String CAN_APPROVE = "canApprove";
	public static final String CAN_REGISTER = "canRegister";
	public static final String CAN_UPDATE = "canUpdate";
	public static final String CAN_SEARCH = "canSearch";
	public static final String IS_ADMIN = "isAdmin";
	public static final String IS_USER_PRESENT = "isUserPresent";
	private static CachedSupplier<Map<String, DynamicResourceHandler>> HANDLERS = CachedSupplier.of(()->{
		Map<String,DynamicResourceHandler> handlers= new HashMap<String, DynamicResourceHandler>();

		handlers.put(IS_ADMIN, new IsAdminHandler());
		handlers.put(CAN_APPROVE,
				new SimpleRoleDynamicResourceHandler(
						Role.Approver
						));
		handlers.put(CAN_REGISTER,
				new SimpleRoleDynamicResourceHandler(
						Role.DataEntry,
						Role.SuperDataEntry
						));
		handlers.put(CAN_UPDATE,
				new SimpleRoleDynamicResourceHandler(
						Role.Updater,
						Role.SuperUpdate
						));
		handlers.put(CAN_SEARCH,  new AbstractDynamicResourceHandler() {
			public boolean isAllowed(final String name,
					final String meta,
					final DeadboltHandler deadboltHandler,
					final Http.Context ctx){
				return true;
			}

		});

		handlers.put(IS_USER_PRESENT,
				new AbstractDynamicResourceHandler() {
			public boolean isAllowed(final String name,
					final String meta,
					final DeadboltHandler deadboltHandler,
					final Http.Context context) {
				Subject subject = deadboltHandler.getSubject(context);
				boolean allowed=false;
				if (subject != null &&(subject.getIdentifier() != null || !subject.getIdentifier().equals(""))) {
					allowed = true;
				}
				return allowed;
			}
		});
		return handlers;
	});

	static {
		init();
	}

	public static class IsAdminHandler extends AbstractDynamicResourceHandler {
		CachedSupplier<Boolean> isAdminForced = CachedSupplier.of(()->{
			return Play.application().configuration().getBoolean("ix.admin", false);
		}) ;

		public boolean isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler,
				final Http.Context context) {
			if(isAdminForced.get()){
				return true;
			}

			Subject subject = deadboltHandler.getSubject(context);
			if (subject != null &&(subject.getIdentifier() != null || !subject.getIdentifier().equals(""))) {
				DeadboltAnalyzer analyzer = new DeadboltAnalyzer();

				if (analyzer.hasRole(subject, Role.Admin.toString())) {
					return true;
				}
			}
			return false;
		}
	}

	public static void init(){
		
	}

	public static class SimpleRoleDynamicResourceHandler extends AbstractDynamicResourceHandler{
		Role[] roles;
		public SimpleRoleDynamicResourceHandler(Role... kind){
			roles=kind;
		}
		public boolean isAllowed(final String name,
				final String meta,
				final DeadboltHandler deadboltHandler,
				final Http.Context context) {
			try{
				DynamicResourceHandler adminHandle =HANDLERS.get().get(IS_ADMIN);
				if (adminHandle.isAllowed(name, meta, deadboltHandler, context)){
					return true;
				}
				Subject subject=deadboltHandler.getSubject(context);
				DeadboltAnalyzer analyzer = new DeadboltAnalyzer();

				return Arrays.stream(roles).anyMatch(r->analyzer.hasRole(subject, r.toString()));
			}catch(Exception e){
				e.printStackTrace();
			}
			return false;
		}

	}


	// this will be invoked for Dynamic
	public boolean isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
		DynamicResourceHandler handler = HANDLERS.get().get(name);
		
		if (handler != null) {
			return handler.isAllowed(name, meta, deadboltHandler, context);
		}else{
			Logger.error("No handler available for " + name);
		}
		return false;
	}


	//This is not currently used, but this
	//may be invoked for custom Pattern checking
	public boolean checkPermission(final String permissionValue,
			final DeadboltHandler deadboltHandler,
			final Http.Context ctx) {
		Subject subject = deadboltHandler.getSubject(ctx);
		if (subject != null) {
			return subject.getPermissions()
					.stream()
					.anyMatch(p->p.getValue().contains(permissionValue));
		}
		return false;
	}
}
