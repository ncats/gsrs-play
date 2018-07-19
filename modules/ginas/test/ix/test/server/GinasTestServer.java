package ix.test.server;

import static play.test.Helpers.fakeApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import ix.core.factories.EntityProcessorFactory;
import ix.core.models.*;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
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
import play.Configuration;
import play.api.Application;
import play.db.DB;
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

    public enum ConfigOptions{
        THIS_TEST_ONLY("testSpecificConfigOperations"),
        ALL_TESTS("acrossTestConfigOperations")
        ;

        private Field f;

        ConfigOptions(String fieldName){
            try {
                f = GinasTestServer.class.getDeclaredField(fieldName);
                f.setAccessible(true);
            }catch(Exception e){
                    f=null;
                }
        }
        protected void thenApply(GinasTestServer instance, Function<Config, Config> function){

            try {
                f.set(instance, ((CompletableFuture<Config>) f.get(instance)).thenApply( c-> function.apply(c)));
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class EntityProcessorConfig{
        private final Class<?> entityClass, processorClass;

        private final Map<String, Object> with;

        private EntityProcessorConfig(Builder builder){
            this.entityClass = builder.entityClass;
            this.processorClass = builder.processorClass;
            this.with = new LinkedHashMap<>(builder.with);
        }

        @JsonProperty("with")
        public Map<String, Object> getWith(){
            return with;
        }
        @JsonProperty("class")
        public Class<?> getEntityClass() {
            return entityClass;
        }
        @JsonProperty("processor")
        public Class<?> getProcessorClass() {
            return processorClass;
        }

        @Override
        public String toString() {
            return "EntityProcessorConfig{" +
                    "entityClass=" + entityClass +
                    ", processorClass=" + processorClass +
                    ", with=" + with +
                    '}';
        }

        public static class Builder{
            private final Class<?> entityClass, processorClass;

            private Map<String, Object> with = new LinkedHashMap<>();

            public Builder(Class<?> entityClass, Class<?> processorClass) {
                this.entityClass = entityClass;
                this.processorClass = processorClass;
            }

            public Builder with(String key, Object value){
                with.put(key, value);
                return this;
            }

            public EntityProcessorConfig build(){
                return new EntityProcessorConfig(this);
            }
        }
    }

    public static int DEFAULT_PORT = 9005;

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


    CompletableFuture<Config> testSpecificConfigOperations;
    CompletableFuture<Config> acrossTestConfigOperations;

    private int userCount=0;

    private boolean running=false;

   private Model.Finder<Long, Principal> principleFinder;

    private TemporaryFolder exportDir = new TemporaryFolder();



    private Config defaultConfig, additionalConfig, testSpecificConfig;
    private File storage;
    private CacheManager cacheManager;


    private Predicate<EntityProcessorConfig> entityProcessorFilter =  null;
    public URL getHomeUrl() throws IOException{
        return new URL(defaultBrowserSession.constructUrlFor("ginas/app"));
    }

    public File getExportRootDir() {
        return exportDir.getRoot();
    }

    public File getUserExportDir(User u){
        return new File(getExportRootDir(), u.username);
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
        this(port, (String) null);
    }
    /**
     * Create a new GinasTestServer instance using the given port, with the given additional
     * default configuration.
     * @param additionalConf additional config String in HOCON format that will overwrite
     *                       the normal conf as if the first line was "include $config.File "
     *                       (but don't actually type that to put that).
     */
    public GinasTestServer(String additionalConf) {
        this(DEFAULT_PORT, additionalConf);
    }
    /**
     * Create a new GinasTestServer instance using the given port, with the given additional
     * default configuration.
     * @param port the port number to use.
     * @param additionalConf additional config String in HOCON format that will overwrite
     *                       the normal conf as if the first line was "include $config.File "
     *                       (but don't actually type that to put that).
     */
    private GinasTestServer(int port,Config additionalConf){
        this.port = port;
        defaultConfig = ConfigFactory.load();
        testSpecificConfig = ConfigFactory.empty();

        if(additionalConf ==null) {
            additionalConfig = ConfigFactory.empty();

        }else {
            additionalConfig = additionalConf;
        }

        acrossTestConfigOperations = CompletableFuture.completedFuture(additionalConf);

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
        defaultRestSession = new RestSession(this, port){
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
        /**
         * Create a new GinasTestServer instance using the given port, with the given additional
         * default configuration.
         * @param port the port number to use.
         * @param additionalConf additional config String in HOCON format that will overwrite
         *                       the normal conf as if the first line was "include $config.File "
         *                       (but don't actually type that to put that).
         */
    public GinasTestServer(int port,String additionalConf){
        this(port, additionalConf ==null? ConfigFactory.empty() : ConfigFactory.parseString(additionalConf));

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
        this(port, ConfigFactory.parseMap(additionalConfiguration));
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
        RestSession session= new RestSession(this, user, port);
        sessions.add(session);
        return session;
    }
    public RestSession newRestSession(User user, RestSession.AUTH_TYPE type){
        RestSession session= new RestSession(this, user, port, type);
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
        return getDataSource("default");
    }

    public DataSource getDataSource(String source){
        Config confFile = ConfigUtil.getDefault().getConfig();
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
        testSpecificConfig = ConfigFactory.empty();

        exportDir.create();
        File actualExportDir = exportDir.getRoot();
        testSpecificConfig = testSpecificConfig.withValue("export.path.root", ConfigValueFactory.fromAnyRef(actualExportDir.getAbsolutePath()))
                                                .withFallback(acrossTestConfigOperations.join())
                                                .withFallback(defaultConfig)
                                                .resolve();

       if(isOracleDB()){
           //System.out.println("in the Oracle db loop");
           dropOracleDb();
       }else { //h2 for now
           //System.out.println("in the h2 db loop");
           deleteH2Db();
       }

        testSpecificConfigOperations = CompletableFuture.completedFuture(testSpecificConfig);

        //This cleans out the old eh-cache
        //and forces us to use a new one with each test
        cacheManager = CacheManager.getInstance();
        cacheManager.removalAll();
        cacheManager.shutdown();

        EntityProcessorFactory.clearInstance();

        extendedBefore(() -> testSpecificConfig.resolve());

        //old map technique set clearpersist to true then removed it so future
        //calls to start didn't affect anything
        Config configToUse = testSpecificConfig.withValue("ix.cache.clearpersist",ConfigValueFactory.fromAnyRef(true))

                            .resolve();
        start(configToUse);
//        testSpecificAdditionalConfiguration.put("ix.cache.clearpersist",true);
//        start();
//        testSpecificAdditionalConfiguration.remove("ix.cache.clearpersist");
//
        
   }

    /**
     * Override this method to add any additional setup as part
     * of the Before phase for example to add additional tables
     * to the database.
     * @param confSupplier A supplier that will create a Config containing
     *                     all the current conf values INCLUDING any modifications made
     *                     by the test server at construction time.
     */
    protected void extendedBefore(Supplier<Config> confSupplier){

    }
    public void addEntityProcessor( EntityProcessorConfig processorConfig) {
        addEntityProcessor(ConfigOptions.THIS_TEST_ONLY, processorConfig);
    }

    public void addEntityProcessor(ConfigOptions options, EntityProcessorConfig processorConfig) {
        Objects.requireNonNull(processorConfig);
        String processorJson;
        try {
            processorJson = new ObjectMapper().writer().writeValueAsString(processorConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.modifyConfig("ix.core.entityprocessors += " + processorJson, options);


    }

    /**
     * Remove Any Entity processors AT START UP TIME that match
     * the given predicate.  Calling this method multiple times
     * will chain the predicates together with "or".
     *
     * @param predicate if the predicate returns true, remove that processor before starting;
     *                  can not be null.
     */
    public void removeEntityProcessors(java.util.function.Predicate<EntityProcessorConfig> predicate){

        Objects.requireNonNull(predicate);

        if(entityProcessorFilter == null){
            entityProcessorFilter = predicate;
        }else{
            entityProcessorFilter= entityProcessorFilter.or(predicate);
        }

    }

    public void addEntityProcessor(Class<?> substanceClass, Class<?> myProcessorClass) {
        this.addEntityProcessor(ConfigOptions.THIS_TEST_ONLY, substanceClass, myProcessorClass);

    }
    public void addEntityProcessor(ConfigOptions options, Class<?> substanceClass, Class<?> myProcessorClass) {

        this.addEntityProcessor(options, new EntityProcessorConfig.Builder(substanceClass, myProcessorClass).build());


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
        testSpecificConfig = ConfigFactory.empty();
        exportDir.delete();
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
        start(
//                defaultConfig
                testSpecificConfigOperations.join()
//                        .withFallback(acrossTestConfigOperations.join())

                .resolve());
    }
    private void start(Config config) {
        if(running){
            return;
        }
        running = true;


        if(entityProcessorFilter !=null){
            List<EntityProcessorConfig> filteredEntities = config.getObjectList("ix.core.entityprocessors")
                    .stream()
                    .map(co -> {
                        Map<String, Object> map = co.unwrapped();
                        try{
                            ClassLoader classLoader = GinasTestServer.class.getClassLoader();
                            //We have to use loadClass instead of Class.forName
                            //because forName will also initialize the class
                            //and Play processors have static initializers that
                            //will crash when the application isn't running (which it isn't yet).

                            Class<?> entity = classLoader.loadClass((String) map.get("class"));
                            Class<?> processor = classLoader.loadClass((String) map.get("processor"));

                            Map<String, ?> with = (Map<String, ?>) map.get("with");

                            EntityProcessorConfig.Builder builder = new EntityProcessorConfig.Builder(entity, processor);
                            if(with !=null && !with.isEmpty()){
                                with.forEach( builder::with);
                            }
                            return builder.build();
                        }catch(Throwable t){
                            throw new RuntimeException(t);
                        }
                    })
                    .filter(entityProcessorFilter.negate())
                    .collect(Collectors.toList());

            try {
                String json = "ix.core.entityprocessors = " + new ObjectMapper().writer().writeValueAsString(filteredEntities);

                config = ConfigFactory.parseString(json)
                        .withFallback(config);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

//        System.out.println("before");
//
//        new Configuration(config).asMap().entrySet()
//                .forEach( e-> {
//                    System.out.println(e.getKey() + "\n\t" + e.getValue());
//
//                    fakeApplication(new Configuration(config.withOnlyPath(e.getKey())).asMap());
//                });
//        System.out.println("after");
        try {


            ts = new TestServer(port, fakeApplication(new Configuration(config.withoutPath("akka")).asMap()));
            ts.start();

            principleFinder =
                    new Model.Finder(Long.class, Principal.class);

            initializeControllers();
            createExtraTables(createConnection(DB.getDataSource()));

            //we have to wait to create the users until after Play has started.
            createInitialFakeUsers();
        } catch(Throwable ex){
            running = false;
            throw new RuntimeException(ex);
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
    


    /**
     * Remove the given configuration property from the application
     * config.  This change will take affect the next time
     * the app is Started.   Any changes performed to the config
     * are restored before the next test is run.
     * @param key the key to remove
     * @return this
     */
    public GinasTestServer removeConfigProperty(String key){

        testSpecificConfigOperations = testSpecificConfigOperations.thenApply(c-> c.withoutPath(key));

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

       return modifyConfig(key, value, ConfigOptions.THIS_TEST_ONLY);
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
    public GinasTestServer modifyConfig(String key, Object value, ConfigOptions configOptions){

        configOptions.thenApply(this, c-> c.withValue(key, ConfigValueFactory.fromAnyRef(value)));

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
     * @param newConf A string of HOCON config overrides can have multiple lines
     * @return this
     */
    public GinasTestServer modifyConfig(String newConf){

       return modifyConfig(newConf, ConfigOptions.THIS_TEST_ONLY);
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
     * @param newConf A string of HOCON config overrides can have multiple lines
     * @return this
     */
    public GinasTestServer modifyConfig(String newConf, ConfigOptions option){
        option.thenApply(this, c->ConfigFactory.parseString(newConf)
                                                        .withFallback(c));

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
     * @param newConf A string of HOCON config overrides can have multiple lines
     * @return this
     */
    public GinasTestServer modifyConfig(Config newConf){

        return modifyConfig(newConf, ConfigOptions.THIS_TEST_ONLY);
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
     * @param newConf A string of HOCON config overrides can have multiple lines
     * @return this
     */
    public GinasTestServer modifyConfig(Config newConf, ConfigOptions option){

         option.thenApply(this, c-> newConf.withFallback(c));

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
    public GinasTestServer modifyConfig(Map<String, Object> confData) {

        return modifyConfig(confData, ConfigOptions.THIS_TEST_ONLY);
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
     *
     * @param confOption The {@link ConfigOptions} to use to specify how
     *                   persistent the conf change should be.
     * @return this
     */
    public GinasTestServer modifyConfig(Map<String, Object> confData, ConfigOptions confOption) {
        confOption.thenApply(this, c->ConfigFactory.parseMap(confData)
                                                            .withFallback(c));

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

                    EntityProcessorFactory.clearInstance();
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
