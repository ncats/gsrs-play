package ix.utils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ix.core.util.IOUtil;
import org.reflections.Reflections;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.TransactionEventListener;

import ix.core.NamedResource;
import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.controllers.v1.InstantiatedNamedResource;
import ix.core.controllers.v1.RouteFactory;
import ix.core.factories.InitializerFactory;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import ix.core.plugins.IxContext;
import ix.core.util.EntityUtils.EntityWrapper;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import scala.collection.JavaConverters;

public class Global extends GlobalSettings {
	
	public static final Logger.ALogger PersistFailLogger = Logger.of("persistFail");
	public static final Logger.ALogger TransformFailLogger = Logger.of("transformFail");
	public static final Logger.ALogger ExtractFailLogger = Logger.of("extractFail");

	static Global _instance;
	
	Application app;
	

	public static Global getInstance() {
		return _instance;
	}

//	// lookup of class name to resource
//	private Map<String, String> names = new TreeMap<String, String>();
	//private Set<Class<?>> resources;
	private IxContext ctx;

	public static Date epoch;

	protected void init(Application app) throws Exception {
		ctx = app.plugin(IxContext.class);
		if (ctx == null) {
			throw new IllegalStateException("Plugin " + IxContext.class.getName() + " is not loaded!");
		}

	}

	@Override
	public void onStart(Application app) {
		this.app=app;
		ServerConfig sc = new ServerConfig();
		sc.add(new TransactionEventListener() {

			@Override
			public void postTransactionCommit(Transaction arg0) {
				System.out.println("Saved a transaction");
			}

			@Override
			public void postTransactionRollback(Transaction arg0, Throwable arg1) {
				// TODO Auto-generated method stub

			}

		});
		try {
			init(app);
			epoch = new Date();
			Logger.info("Global instance " + this + " started at " + epoch);
			_instance = this;
		} catch (Exception ex) {
			Logger.trace("Can't initialize app!", ex);
		}

		RouteFactory._registry.get();
//		loadDefaultUsers();
		
		InitializerFactory.getInstance(app)
		                  .getInitializers()
		                  .forEach(i->i.onStart(app));
		
	}

	void loadDefaultUsers() {
		List<Object> ls = app.configuration().getList("ix.core.users", null);

		if (ls != null) {
			for (Object o : ls) {
				if (o instanceof Map) {
					Map m = (Map) o;
					String username = (String) m.get("username");
					String email = (String) m.get("email");
					String password = (String) m.get("password");
					List roles = (List) m.get("roles");
					List groups = (List) m.get("groups");

					Principal p = new Principal(username, email);
					
					Principal p2 = PrincipalFactory.byUserName(username);
					if (p2 == null) {
						try {
							UserProfile up = UserProfileFactory.addActiveUser(p, password, roles, groups);
						} catch (Exception e) {
							Logger.error(username + "failed");
							e.printStackTrace();
						}
					} else {

					}
				}
			}
		}
	}

	void dumpRoute() {
		Collection list = JavaConverters.asJavaCollectionConverter(play.api.Play.current().routes().toList())
				.asJavaCollection();
		for (Object l : list) {
			ix.Routes$ route = (ix.Routes$) l;
			String prefix = route.prefix();
			Logger.debug("PREFIX: " + prefix);
			for (java.lang.reflect.Method m : route.getClass().getMethods()) {
				if (m.getName().indexOf("_Routes") > 0) {
					try {
						Object obj = m.invoke(route);
						Logger.debug("++ " + m + " => " + obj + " [" + obj.getClass() + "]");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				// Logger.debug("+"+m);
			}

			if (prefix.length() > 0) {
				// now check to see any of the paths matches with {prefix}, if
				// so then we contract /{prefix}/{prefix}/{path}
				// to /{prefix}/{path}

				// method, url, action
				List<scala.Tuple3<String, String, String>> seq = JavaConverters
						.seqAsJavaListConverter(route.documentation()).asJava();
				// Logger.debug("route: "+seq);
				for (scala.Tuple3 t : seq) {
					// skip the /{prefix} prefix
					Logger.debug(" .." + t._1() + " \"" + t._2() + "\" " + t._3());
					String path = t._2().toString().substring(prefix.length());
					if (path.startsWith(prefix)) {
						Logger.debug(path + " => " + t._2());
					}
				}
			}
		}
	}

	@Override
	public void onStop(Application app) {
		Logger.info("## stopping");
	}

	@Override
	public <T extends EssentialFilter> Class<T>[] filters() {
		return new Class[] { GzipFilter.class, julienrf.play.jsonp.JsonpJava.class };
	}

	public boolean debug(int level) {
		return ctx.debug(level);
	}

	public IxContext context() {
		return ctx;
	}

	public static IxContext getContext() {
		return getInstance().context();
	}

	/**
	 * Return the registered NamedResource given a class name
	 */
	public static String getResource(Class<?> cls) {
		InstantiatedNamedResource resource = RouteFactory._registry
									.get()
									.getResourceFor(cls);
		
		if(resource==null) return null;
		return resource.getName();
	}

	public static boolean DEBUG(int level) {
		return getInstance().debug(level);
	}

	public static String getRef(Object instance) {
		if (instance == null)
			return null;
		Global g = getInstance();
		
		EntityWrapper ew = EntityWrapper.of(instance);

		if (!ew.isEntity() || !ew.hasKey()) {
			Logger.error(instance + " isn't an Entity with an ID!");
			throw new IllegalArgumentException("Instance is not an Entity");
		}

		String name = getResource(ew.getEntityClass());
		
		if (name == null) {
			return null;
		}
		
		return getNamespace() + "/" + name + "(" + ew.getKey().getIdString() + ")";
	}

	public static String getHost() {
		try {

			String h = _instance.ctx.host();
			if (h == null) {
				Http.Request req = Controller.request();
				h = (req.secure() ? "https" : "http") + "://" + req.host();
			}
			return h;
		} catch (Exception ex) {
			// no available http context..
			Logger.trace("No available HTTP context!");
		}
		return "";
	}

	public static String getNamespace() {
		return getHost() + _instance.ctx.context() + _instance.ctx.api();
	}

	public static String getRef(Class<?> type, Object id) {
		Global g = getInstance();
		try {
			String name = g.getResource(type);
			if (name == null)
				throw new IllegalArgumentException("Class " + type + " isn't a NamedResource!");
			return getNamespace() + "/" + name + "(" + id + ")";
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public static String getRef(String type, Object id) {
		Global g = getInstance();
		try {
			String name = g.getResource(IOUtil.getGinasClassLoader().loadClass(type));
			if (name == null)
				throw new IllegalArgumentException("Class " + type + " isn't a NamedResource!");
			return getNamespace() + "/" + name + "(" + id + ")";
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	// For CORS
	public class ActionWrapper extends Action.Simple {
		public ActionWrapper(Action<?> action) {
			this.delegate = action;
		}


		//For reasons that remain unclear, this never gets called.
		@Override
		public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
			Promise<Result> result = this.delegate.call(ctx);
			Http.Response response = ctx.response();
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, PATCH, DELETE"); // Only
																					// allow
																					// POST
			response.setHeader("Access-Control-Max-Age", "300"); // Cache
																	// response
																	// for 5
																	// minutes
			response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept"); // Ensure
																													// this
																													// header
																													// is
																													// also
																													// allowed!

			return result;
		}
	}

	@Override
	public Action<?> onRequest(Http.Request request, java.lang.reflect.Method actionMethod) {
		return new ActionWrapper(super.onRequest(request, actionMethod));
	}

	@Override
	public Promise<Result> onHandlerNotFound(RequestHeader request) {
		if (!request.path().endsWith("/"))
			return super.onHandlerNotFound(request);
		// return Promise.<Result>((request.path().substring(0,
		// request.path().length()-1));
		return Promise
				.<Result> pure(Controller.movedPermanently(request.path().substring(0, request.path().length() - 1)));
	}

}
