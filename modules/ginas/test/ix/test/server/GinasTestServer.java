package ix.test.server;

import static play.test.Helpers.fakeApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.sql.DataSource;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.factories.EntityProcessorFactory;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jolbox.bonecp.BoneCPDataSource;
import com.typesafe.config.Config;

import ix.core.UserFetcher;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.Group;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.util.CachedSupplier;
import ix.ncats.controllers.App;
import ix.seqaln.SequenceIndexer.CachedSup;
import ix.test.util.TestUtil;
import ix.utils.Util;
import net.sf.ehcache.CacheManager;
import play.api.Application;
import play.db.ebean.Model;
import play.libs.ws.WSCookie;
import play.libs.ws.WSResponse;
import play.test.TestServer;

/**
 * JUnit Rule to handle starting and stopping
 * a Ginas Server around each @Test method.
 *
 * <p>
 *     Example usage:
 *
 *     <pre>
 *         @Rule
 *         public GinasTestServer ts = new GinasTestServer(9001);
 *
 *         ...
 *
 *         @Test
 *         public void myTest(){
 *             ts.run(new Callable<Void>() {
 *
 *                public Void call(){
 *                 //do stuff
 *                 return null;
 *             });
 *         }
 *
 *         @Test
 *         public void withJava8Lambda(){
 *             ts.run( () -> {
 *                //do stuff
 *                });
 *         }
 *     </pre>
 *
 *
 * </p>
 *
 *
 *
 *
 * Created by katzelda on 2/25/16.
 */
public class GinasTestServer extends ExternalResource{


    public static int DEFAULT_PORT = 9001;

     private static final String FAKE_USERNAME_PREFIX="FAKE";
     private static final String FAKE_PASSWORD_PREFIX="pa$$word";
     
	 public static final String FAKE_USER_1="fakeuser1";
	 public static final String FAKE_USER_2="fakeuser2";
	 public static final String FAKE_USER_3="fakeuser3";

	 
	 public static final String FAKE_PASSWORD_1="madeup1";
	 public static final String FAKE_PASSWORD_2="madeup2";
	 public static final String FAKE_PASSWORD_3="madeup3";


    public static final String FAKE_ADMIN = "fakeAdmin1";



    private final BrowserSession defaultBrowserSession;
    private final RestSession defaultRestSession;

    private List<AbstractSession> sessions = new ArrayList<>();

    private TestServer ts;
    private int port;
    private Group fakeUserGroup;

    private static final List<Role> superUserRoles = Role.roles(Role.SuperUpdate,Role.SuperDataEntry );
    private static final List<Role> normalUserRoles = Role.roles(Role.DataEntry,Role.Updater );
    private static final List<Role> adminUserRoles = Role.roles(Role.values() );
    private static final List<Role> approverUserRoles = Role.roles(Role.DataEntry,Role.Updater, Role.Approver);

    
    private int userCount=0;

    private boolean running=false;

   private Model.Finder<Long, Principal> principleFinder;


    private Map<String, Object> originalAdditionalConfiguration = new HashMap<>();
    private Map<String, Object> additionalConfiguration = new HashMap<>();
    private Map<String, Object> testSpecificAdditionalConfiguration = new HashMap<>();
    private File storage;
    private CacheManager cacheManager;

    public URL getHomeUrl() throws IOException{
        return new URL(defaultBrowserSession.constructUrlFor("ginas/app"));
    }




    public static class User{
    	private final String username;
    	private String password;

        public User(String username, String password) {
            Objects.requireNonNull(username);
            Objects.requireNonNull(password);

            this.username = username;
            this.password = password;
        }

        public String getPassword(){
            return password;
        }
        public String getUserName() {
            return username;
        }


        public String getEmail() {
            return username + "@example.com";
        }

        public Principal asPrincipal() {
            return new Principal(username, getEmail());
        }
    }

    /**
     * Create a new instance listening on the default port.
     */
    public GinasTestServer(){
        this(DEFAULT_PORT);
    }


    /**
     * Create a new GinasTestServer instance using the default port, with the given additional
     * default configuration.
     * @param additionalConfiguration additional config key-value pairs to add
     *                                to the application config before the server is started.
     *                                This can be further modified using the modifyConfig methods.
     */
    public GinasTestServer(Map<String, Object> additionalConfiguration){
        this(DEFAULT_PORT, additionalConfiguration);
    }

    /**
     * Create a new GinasTestServer instance using the given port.
     * @param port the port number to use.
     */
    public GinasTestServer(int port) {
        this(port, null);
    }
    /**
     * Create a new GinasTestServer instance using the given port, with the given additional
     * default configuration.
     * @param port the port number to use.
     * @param additionalConfiguration additional config key-value pairs to add
     *                                to the application config before the server is started.
     *                                This can be further modified using the modifyConfig methods.
     */
    public GinasTestServer(int port, Map<String, Object> additionalConfiguration){
        this.port = port;
        if(additionalConfiguration !=null) {
            this.originalAdditionalConfiguration.putAll(additionalConfiguration);
            this.additionalConfiguration.putAll(additionalConfiguration);
        }

        defaultBrowserSession = new BrowserSession(port){
            @Override
            protected WSResponse doLogout() {
                //no-op
                return new WSResponse() {
                    @Override
                    public Map<String, List<String>> getAllHeaders() {
                        return Collections.emptyMap();
                    }

                    @Override
                    public Object getUnderlying() {
                        return null;
                    }

                    @Override
                    public int getStatus() {
                        return 200;
                    }

                    @Override
                    public String getStatusText() {
                        return "";
                    }

                    @Override
                    public String getHeader(String key) {
                        return null;
                    }

                    @Override
                    public List<WSCookie> getCookies() {
                        return Collections.emptyList();
                    }

                    @Override
                    public WSCookie getCookie(String name) {
                        return null;
                    }

                    @Override
                    public String getBody() {
                        return "";
                    }

                    @Override
                    public Document asXml() {
                        return null;
                    }

                    @Override
                    public JsonNode asJson() {
                        try {
                            return new ObjectMapper().readTree("");
                        } catch (IOException e) {
                            //can't happen
                            return null;
                        }
                    }

                        @Override
                    public InputStream getBodyAsStream() {
                        return null;
                    }

                    @Override
                    public byte[] asByteArray() {
                        return new byte[0];
                    }

                    @Override
                    public URI getUri() {
                        return null;
                    }
                };
            }

            @Override
            public boolean isLoggedIn() {
                return true;
            }
        };
        defaultRestSession = new RestSession(port){
            @Override
            protected Void doLogout() {
                //no-op
                return null;
            }

            @Override
            public boolean isLoggedIn() {
                return true;
            }
        };
    }

    public GinasTestServer(Supplier<Map<String,Object>> sup) {
    	this(sup.get());
	}

	private void createInitialFakeUsers() {
        fakeUserGroup= AdminFactory.groupfinder.get().where().eq("name", "fake").findUnique();
        if(fakeUserGroup ==null){
            fakeUserGroup=new Group("fake");
        }
        List<Group> groups = Collections.singletonList(fakeUserGroup);

        UserProfileFactory.addActiveUser(FAKE_USER_1,FAKE_PASSWORD_1, superUserRoles,groups);
        UserProfileFactory.addActiveUser(FAKE_USER_2,FAKE_PASSWORD_2, superUserRoles,groups);

        UserProfileFactory.addActiveUser(FAKE_USER_3,FAKE_PASSWORD_3,normalUserRoles,groups);

        UserProfileFactory.addActiveUser(FAKE_ADMIN,FAKE_PASSWORD_1, adminUserRoles,groups);
    }


    public User getFakeUser1(){
        return new User(FAKE_USER_1,FAKE_PASSWORD_1);
    }
    public User getFakeUser2(){
        return new User(FAKE_USER_2,FAKE_PASSWORD_2);
    }
    public User getFakeUser3(){
        return new User(FAKE_USER_3,FAKE_PASSWORD_3);
    }

    public User getAdmin() {
        return new User(FAKE_ADMIN,FAKE_PASSWORD_1);
    }

    @FunctionalInterface
    public interface VoidUserAction<E extends Exception>{
        void doIt() throws E;
    }

    @FunctionalInterface
    public interface UserAction<R, E extends Exception>{
        R doIt() throws E;
    }
    public static <E extends Exception> void doAsUser(User user, VoidUserAction<E> action) throws E{
        Principal oldP = UserFetcher.getActingUser();
        try{
            UserFetcher.setLocalThreadUser(user.asPrincipal());
            action.doIt();
        }finally{
            UserFetcher.setLocalThreadUser(oldP);
        }
    }
    public static <R, E extends Exception> R doAsUser(User user, UserAction<R, E> action) throws E{
        Principal oldP = UserFetcher.getActingUser();
        try{
            UserFetcher.setLocalThreadUser(user.asPrincipal());
            return action.doIt();
        }finally{
            UserFetcher.setLocalThreadUser(oldP);
        }
    }



    public RestSession notLoggedInRestSession(){
        return defaultRestSession;
    }
    public BrowserSession notLoggedInBrowserSession(){
        return defaultBrowserSession;
    }
    public BrowserSession newBrowserSession(User user){
        BrowserSession session = new BrowserSession(user, port);
        sessions.add(session);
        return session;
    }

    public RestSession newRestSession(User user){
        RestSession session= new RestSession(user, port);
        sessions.add(session);
        return session;
    }
    public RestSession newRestSession(User user, RestSession.AUTH_TYPE type){
        RestSession session= new RestSession(user, port, type);
        sessions.add(session);
        return session;
    }

    public User createApprover(String username, String password){
        return createUser(username, password, approverUserRoles);
    }


    public User createAdmin(String username, String password){
        return createUser(username, password, adminUserRoles);
    }

    public User createNormalUser(String username, String password){
        return createUser(username, password, normalUserRoles);
    }
    public User createUser(Collection<Role> roles){
        if(roles.isEmpty()){
            throw new IllegalArgumentException("can not have empty roles");
        }
        userCount++;
        return createUser(
                FAKE_USERNAME_PREFIX + userCount,
                FAKE_PASSWORD_PREFIX + userCount, new ArrayList<>(roles));
    }
    public User createUser(Role role, Role ... roles){
        userCount++;
        return createUser(
                FAKE_USERNAME_PREFIX + userCount,
                FAKE_PASSWORD_PREFIX + userCount, role, roles);
    }
    public User createUser(String username, String password, Role role, Role ... roles){
        List<Role> list = new ArrayList<>();
        list.add(role);
        for(Role r: roles){
            list.add(r);
        }
        return createUser(username, password,list);
    }
    public User createUser(String username, String password, List<Role> roles){
    	if(roles.isEmpty()){
            throw new IllegalArgumentException("roles can not be empty");
        }

        Principal existingUser = principleFinder.where().ieq("username", username).findUnique();
        if(existingUser !=null){
            throw new IllegalArgumentException("user already exists: " + username);
        }
    	UserProfile up=UserProfileFactory.addActiveUser(username, password, roles, Collections.singletonList(fakeUserGroup));
    	return new User(up.getIdentifier(), password);
    }

    public boolean isOracleDB(){
        String dbUrl = ConfigUtil.getDefault().getValueAsString("db.default.url");
        return dbUrl.contains("jdbc:oracle:thin");
    }

    protected void createExtraTables(Supplier<Connection> con) throws Throwable{

    }

    protected void dropExtraTables(Supplier<Connection> con) throws Throwable{

    }

    public DataSource getDataSource() {
        Config confFile = ConfigUtil.getDefault().getConfig();
        String source = "default";
        Config dbconf = confFile.getConfig("db");
        Config db = dbconf.getConfig(source);
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setJdbcUrl(db.getString("url"));
        ds.setUsername(db.getString("user"));
        ds.setPassword(db.getString("password"));
        return ds;
    }

    @Override
    protected void before() throws Throwable {
        testSpecificAdditionalConfiguration.clear();

       if(isOracleDB()){
           //System.out.println("in the Oracle db loop");
           dropOracleDb();
       }else { //h2 for now
           //System.out.println("in the h2 db loop");
           deleteH2Db();
       }

        //This cleans out the old eh-cache
        //and forces us to use a new one with each test
        cacheManager = CacheManager.getInstance();
        cacheManager.removalAll();
        cacheManager.shutdown();

        EntityProcessorFactory.clearInstance();

        extendedBefore(ConfigUtil.getDefault().getConfig());

        testSpecificAdditionalConfiguration.put("ix.cache.clearpersist",true);
        start();
        testSpecificAdditionalConfiguration.remove("ix.cache.clearpersist");
        
        
   }

    /**
     * Override this method to add any additional setup as part
     * of the Before phase for example to add additional tables
     * to the database.
     * @param defaultConfig the default config being used can be used
     *                      to get the config properties.  This doesn't include any additional config overrides
     */
    protected void extendedBefore(Config defaultConfig){

    }

    

    private void initializeControllers() {
    	CachedSupplier.resetAllCaches();
    	CachedSup.resetAllCaches();
    }

    private void deleteH2Db() throws Throwable {
        DataSource ds = getDataSource();

        dropExtraTables(createConnection(ds));
        File home = ConfigUtil.getDefault().getValueAsFile("ix.home");
        TestUtil.tryToDeleteRecursively(home);


        createExtraTables(createConnection(ds));

    }

    private static Supplier<Connection> createConnection(DataSource ds){
       return () -> {
           try {
               return ds.getConnection();
           } catch (SQLException e) {
               throw new RuntimeException(e);
           }
       };
    }


    public void dropOracleDb() throws Throwable {


        BoneCPDataSource datasource = (BoneCPDataSource)getDataSource();



        try {

            final Connection con = datasource.getConnection();
            Statement stm = con.createStatement();

            String evolutionContent = FileUtils.readFileToString(new File("conf/evolutions/default/1.sql"));
            String[] splittedEvolutionContent = evolutionContent.split("# --- !Ups");
            String[] upsDowns = splittedEvolutionContent[1].split("# --- !Downs");
            String createDdl = upsDowns[0];
            String dropDdl = upsDowns[1];
            String[] ups = createDdl.split(";");
            String[] downs = dropDdl.split(";");
            List tableName = new ArrayList<String>();
            List seqName = new ArrayList<String>();



            PreparedStatement ps = con.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES");
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                tableName.add( rs.getString(1).trim());
            }
            System.out.println("DROP TABLES XXXXXXXXXXXXXXXXXX");

            for(int i = 0; i<tableName.size(); i++)
            {
                String tName = tableName.get(i).toString();
                String query = "drop table " + tName + " cascade constraints purge";
                //System.out.println("query:" + query);
               try {
                   stm.executeQuery(query);
               }catch(Exception e){
                  System.out.println(e.getMessage());
               }
            }

            PreparedStatement ps1 = con.prepareStatement("select SEQUENCE_NAME from dba_sequences where SEQUENCE_OWNER = '" + datasource.getUsername() +"'");
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next())
            {
                seqName.add( rs1.getString(1).trim());
            }
            System.out.println("DROP SEQUENCES XXXXXXXXXXXXXXXXXX");
            for(int i = 0; i<seqName.size(); i++)
            {
                String sName = seqName.get(i).toString();
                String query = "drop sequence " + sName;
                try {
                    stm.executeQuery(query);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }

            dropExtraTables(() -> con);
           System.out.println("CREATE TABLES XXXXXXXXXXXXXXXXXX");
           for(int i = 0; i<ups.length; i++)
            {
                try{
                   if(ups[i] != null && !ups[i].trim().equals("")) {
                       stm.executeQuery(ups[i]);
                   }
                }catch(Exception e){
                    System.out.println("query - " + ups[i] +" : " + e.getMessage());
                }
            }

            createExtraTables(() -> con);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    @Override
    protected void after() {
        stop();
        additionalConfiguration = new HashMap<>(originalAdditionalConfiguration);
    }

    /**
     * Start the server, stopping the
     * old one if it's still running.
     *
     * This does not delete the h2 database.
     *
     * This is the same as calling:
     *
     * <pre>
     *     stop(true);
     *     start();
     *
     *
     * </pre>
     */
    public void restart(){
        stop(true);
        start();
    }

    public Application getApplication(){
        return ts.application();
    }
    
        
    public void start() {
        if(running){
            return;
        }
        running = true;

        try {
            Map<String, Object> map = new HashMap<>(additionalConfiguration);
            map.putAll(testSpecificAdditionalConfiguration);

            ts = new TestServer(port, fakeApplication(unflatten(map)));
            ts.start();

            principleFinder =
                    new Model.Finder(Long.class, Principal.class);

            initializeControllers();
            //we have to wait to create the users until after Play has started.
            createInitialFakeUsers();
        } catch(Throwable ex){
            running = false;
            throw ex;
        }
    }
    
    public static class ExpandedMap{
    	Map<String,Object> unflattened = new HashMap<String,Object>();
    	public ExpandedMap put(String key, Object value){
    		String[] path=key.split("[.]");
    		Object val=value;
    		for(int i=path.length-1;i>=0;i--){
    			val=Util.toMap(path[i], val);
    		}
    		if(val instanceof Map){
    			mixIn(unflattened, (Map<String,Object>)val);
    		}
    		return this;
    	}
    	
    	private static void mixIn(Map<String,Object> m1, Map<String,Object> m2){
    		Set<String> commonKeys= new HashSet<String>(m1.keySet());
    		commonKeys.retainAll(m2.keySet());
    		
    		for(String k:m2.keySet()){
    			if(!commonKeys.contains(k)){
    				m1.put(k, m2.get(k));
    			}
    		}
    		for(String k:commonKeys){
    			mixIn((Map)m1.get(k), (Map)m2.get(k)); //will fail if one isn't a map
    		}
    	}
    	public Map<String,Object> build(){
    		return this.unflattened;
    	}
    }
    
    
    private Map<String, Object> unflatten(Map<String,Object> settings){
    	ExpandedMap em = new ExpandedMap();
    	
    	settings.entrySet()
    			 .stream()
    			 .forEach(es-> em.put(es.getKey(), es.getValue()));
    	return em.build();
    }

    /**
     * Remove the given configuration property from the application
     * config.  This change will take affect the next time
     * the app is Started.   Any changes performed to the config
     * are restored before the next test is run.
     * @param key the key to remove
     * @return this
     */
    public GinasTestServer removeConfigProperty(String key){
        testSpecificAdditionalConfiguration.remove(key);
        additionalConfiguration.remove(key);
        return this;
    }

    /**
     * Add the given key value pair to the application config.
     * This change will take affect the next time
     * the app is Started.  Any changes performed to the config
     * are restored before the next test is run.
     *
     * @param key the key to add
     * @param value the value for this key.
     * @return this
     */
    public GinasTestServer modifyConfig(String key, Object value){
        testSpecificAdditionalConfiguration.put(key, value);
        return this;
    }
    /**
     * Add the multiple key value pairs to the application config.
     * This change will take affect the next time
     * the app is Started.   Any changes performed to the config
     * are restored before the next test is run.
     *
     * This is the same as calling {@link #modifyConfig(String, Object)}
     * multiple times, once for each entry in the map.
     *
     * @param confData a map of key-value pairs to add to the config.
     * @return this
     */
    public GinasTestServer modifyConfig(Map<String, Object> confData){
        testSpecificAdditionalConfiguration.putAll(confData);
        return this;
    }

    /**
     * Stop the server and optionally preserve
     * the h2 databases and indexes computed so far
     * so that on the next start of the server, we re-use
     * them.
     * @param preserveDatabase {@code true} if the h2 database and indexes
     *                         should be preserved; {@code false} if they should be deleted.
     */
    public void stop(boolean preserveDatabase) {
        try {
            if (running) {
                try {
                    running = false;
                    for (AbstractSession session : sessions) {
                        session.logout();
                    }
                    sessions.clear();
                    //explicitly shutdown indexer to clear file locks
                    App.getTextIndexer().shutdown();
                } finally {
                    ts.stop();
                }

            }
        }finally{
            if (preserveDatabase) {
                TextIndexerPlugin.prepareTestRestart();
            }
        }
    }

    /**
     * Stop the server and do not preserve
     * the databases and indexes.  This is the same
     * as {@link #stop(boolean) stop(false}.
     *
     * @see #stop(boolean)
     */
    public void stop() {
       stop(false);
    }

    public File getStorageRootDir(){
        return TextIndexerPlugin.getStorageRootDir();
    }


}
