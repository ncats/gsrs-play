package ix.ncats.controllers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ix.core.util.ConfigHelper;
import play.db.DB;

public class DBConfigInfo{
	private String dbname;
	private String dbdriver;
	private String dbproduct;
	private boolean connected=false;
	private long latency=-1;
	public DBConfigInfo(String name, String driver, String product, boolean connected, long lat){
		this.dbname=name;
		this.dbdriver=driver;
		this.dbproduct=product;
		this.connected=connected;
		this.latency=lat;
		
	}
	public String getName(){
		return this.dbname;
	}
	public String getDriver(){
		return this.dbdriver;
	}
	public String getProduct(){
		return this.dbproduct;
	}
	public boolean getConnected(){
		return this.connected;
	}
	public Long getLatency(){
		if(latency>=0) return latency;
		return null;
	}
	/**
     * Returns a list of known databases in the configuration
     * file, along with basic information about the connection
     * if one can be made
     * @return
     */
    public static List<DBConfigInfo> getDefinedDatabases(){
    	Object dbs=ConfigHelper.getOrDefault("db", null);
    	List<DBConfigInfo> dblist = new ArrayList<DBConfigInfo>();
    	if(dbs instanceof Map){
    		Map<String,Object> databases = (Map<String,Object>)dbs;
    		databases.forEach((dbname,dbc)->{
    			Map<String,Object> dbconf=(Map<String,Object>)dbc;
    			String productName=null;
    			boolean connectable=false;
    			String driverName = (String)dbconf.get("driver");
    			long latency=-1;
    			try(Connection c = DB.getConnection(dbname)){
    				long start=System.currentTimeMillis();
	    			DatabaseMetaData meta = c.getMetaData();
	    			productName=meta.getDatabaseProductName() + " " +meta.getDatabaseProductVersion();
	    			long end=System.currentTimeMillis();
	    			connectable=true;
	    			latency=end-start;
	    			c.close();
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}
    			dblist.add(new DBConfigInfo(dbname,driverName,productName,connectable,latency));
    		});
    	}
    	return dblist;
    }
}