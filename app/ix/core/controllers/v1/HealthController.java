package ix.core.controllers.v1;

import be.objectify.deadbolt.java.actions.Dynamic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.plugins.IxCache;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.Global;
import play.Configuration;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HealthController extends Controller {

    private static String UP_STATUS = "{\"status\":\"UP\"}";

    public static Result isUp(){
        return Results.ok(UP_STATUS);
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result info() throws Exception{
        return Results.ok((JsonNode) new ObjectMapper().valueToTree(Application.createFromCurrentRuntime()));
    }

    public static int[] uptime () {
        int[] ups = null;
        if (Global.epoch != null) {
            ups = new int[3];
            // epoch in seconds
            long u = (System.currentTimeMillis()
                    - Global.epoch.getTime())/1000;
            ups[0] = (int)(u/3600); // hour
            ups[1] = (int)((u/60) % 60); // min
            ups[2] = (int)((u%60)); // sec
        }
        return ups;
    }

    public static class RuntimeInfo{
        public long availableProcessors;
        public long freeMemory;
        public long totalMemory;
        public long maxMemory;

        public RuntimeInfo(){}
        public RuntimeInfo(Runtime rt){
            this.availableProcessors = rt.availableProcessors();
            this.freeMemory = rt.freeMemory();
            this.maxMemory = rt.maxMemory();
            this.totalMemory = rt.totalMemory();
        }
    }
    public static class Application{

        public Date epoch;
        public UptimeInfo uptime;
        public long threads;
        public long runningThreads;
        public String javaVersion;

        public String hostname;
        public RuntimeInfo runtime;

        public List<DataBaseInfo> databaseInformation;

        public CacheInfo cacheInfo;

        public static Application createFromCurrentRuntime() throws Exception {
            return createFrom(Runtime.getRuntime());
        }
        public static Application createFrom(Runtime rt) throws Exception {
            Application app = new Application();
            app.uptime = new UptimeInfo();
            int[] uptime = uptime();
            app.uptime.hours = uptime[0];
            app.uptime.minutes = uptime[1];
            app.uptime.seconds = uptime[2];
            app.epoch = Global.epoch;

            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            app.threads = threads.size();
            app.runningThreads = threads.stream()
                                        .filter(t-> (t.getState()==Thread.State.RUNNABLE))
                                        .count();
            app.javaVersion = System.getProperty("java.version");

            app.hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();

            app.runtime = new RuntimeInfo(rt);

            app.databaseInformation = DBConfigInfo.getDefinedDatabases()
                                                .stream()
                                                .map(DataBaseInfo::create)
                                                .collect(Collectors.toList());

            app.cacheInfo = new CacheInfo();
            Configuration conf = Play.application().configuration();
            app.cacheInfo.timeToLive = conf.getInt(IxCache.CACHE_TIME_TO_LIVE);
            app.cacheInfo.timeToIdle = conf.getInt(IxCache.CACHE_TIME_TO_IDLE);
            app.cacheInfo.maxCacheElements = conf.getInt(IxCache.CACHE_MAX_ELEMENTS);
            app.cacheInfo.maxNotEvictableCacheElements = conf.getInt(IxCache.CACHE_MAX_NOT_EVICTABLE_ELEMENTS);
            return app;
        }
    }

    /*
    application: {
    epoch: number,
    uptime: Array[number] //some backend processing done in App.java,
could be done frontend by getting current time
    threads: number,
    runningThreads: number,
    javaVersion: string,
    hostName: string,
    runtime: {
        availableProcessors: number,
        freeMemory:number,
        totalMemory:number,
        maxMemory:number,
    },
databaseInformation: Array [ {
    database: string;
    driver: string;
    product: string;
    connected: boolean;
    latency: number;
}]

     */

    public static class CacheInfo{
        public long maxCacheElements;
        public long maxNotEvictableCacheElements;
        public long timeToLive;
        public long timeToIdle;
        /*
         <div class="panel-heading">
       		<h3 class="panel-title">Cache Configuration</h3>
    	</div>
	    <div class="panel-body">
	       <table class="table table-striped">
	          <tr>
	            <td>Max Cache Elements</td>
	        	<td>@Play.application().configuration().getString(IxCache.CACHE_MAX_ELEMENTS)</td>
	          </tr>
	          <tr>
	        <td>Time to Live (seconds)</td>
	        <td>@Play.application().configuration().getInt(IxCache.CACHE_TIME_TO_LIVE)</td>
	          </tr>
	          <tr>
	        <td>Time to Idle (seconds)</td>
	        <td>@Play.application().configuration().getInt(IxCache.CACHE_TIME_TO_IDLE)</td>
	          </tr>
	       </table>
         */
    }
    public static class UptimeInfo{
        public long hours;
        public int minutes;
        public int seconds;



    }
    public static class DataBaseInfo{
        public String database;
        public String driver;
        public String product;
        public Long latency;
        public boolean connected;

        public DataBaseInfo(){}

        public static  DataBaseInfo create(DBConfigInfo info){
            DataBaseInfo dbInfo = new DataBaseInfo();
            dbInfo.database = info.getName();
            dbInfo.driver = info.getDriver();
            dbInfo.product = info.getProduct();
            dbInfo.latency = info.getLatency();
            dbInfo.connected = info.getConnected();
            return dbInfo;

        }
    }
}
