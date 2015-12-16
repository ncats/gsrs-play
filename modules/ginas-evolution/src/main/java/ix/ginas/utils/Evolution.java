package ix.ginas.utils;

import java.util.regex.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.sql.*;

import play.*;
import play.db.*;

import com.avaje.ebean.*;
import com.avaje.ebean.config.*;
import com.avaje.ebeaninternal.server.ddl.*;
import com.avaje.ebeaninternal.api.*;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.sql.DataSource;

import com.jolbox.bonecp.*;


public class Evolution {
    String source;
    File ddl;
    DataSource datasource;
    MessageDigest md;
    Map<String, String> symbols = new TreeMap<String, String>();
    
    public Evolution (String file, String source) throws Exception {
        this.source = source;
        this.md = MessageDigest.getInstance("SHA1");
        this.ddl = new File ("modules/ginas/conf/evolutions/"+source+"/1.sql");
        
        String postsqlfile = "conf/sql/post/ginas-oracle.sql";
        System.out.println("=============================");
        String postSQL=null;
        try{
        	postSQL = new Scanner(new File(postsqlfile)).useDelimiter("\\Z").next();
        	System.out.println("postSQL:" + postSQL);
        	
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        Config root = ConfigFactory.parseFile(new File (file));
        Config ebean = root.getConfig("ebean");
        Config dbconf = root.getConfig("db");
        
        String models = ebean.getString(source);           
        System.err.println("Evolving "+source+ " => "+models);

        Config db = dbconf.getConfig(source);
        String driver = db.getString("driver");
        BoneCPDataSource ds = new BoneCPDataSource ();
        ds.setJdbcUrl(db.getString("url"));
        ds.setUsername(db.getString("user"));
        ds.setPassword(db.getString("password"));
        datasource = ds;
        
        createDdl (models.split(","), postSQL);
        
        ds.close();
    }

    void execute (String sql) throws SQLException {
        Connection con = datasource.getConnection();
        try {
            Statement stm = con.createStatement();
            for (String s : sql.split(";")) {
                s = s.trim();
                if (s.length() > 0) {
                    try {
                        stm.execute(s);
                    }
                    catch (SQLException ex) {
                        System.err.println("Can't execute statement: \""
                                           +s+"\"; "+ex.getMessage());
                    }
                }
            }
            stm.close();
        }
        finally {
            con.close();
        }
    }

    public void createDdl (String[] models, String postSQL) throws Exception {
        ServerConfig config = new ServerConfig();
        config.setName(source);
        config.loadFromProperties();
        config.setDataSource(datasource);
        config.setDefaultServer(true);

        Set<String> classes = new TreeSet<String>();
        for(String load: models) {
            load = load.trim();
            if (load.endsWith(".*")) {
                Reflections reflections = new Reflections
                    (load.substring(0, load.length()-2));
                Set<Class<?>> resources =
                    reflections.getTypesAnnotatedWith(Entity.class);
                for (Class<?> c : resources) {
                    Table tab = (Table)c.getAnnotation(Table.class);
                    if (tab != null) {
                        symbols.put(tab.name(), c.getName());
                        //System.out.println(c.getName()+" => "+tab.name());
                    }
                    classes.add(c.getName());
                }
            }
            else {
                classes.add(load);
            }
        }

        for (String c : classes) {
            //System.out.println(c);
            try {
                config.addClass(Class.forName
                                (c, true, this.getClass().getClassLoader()));
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        EbeanServer ebean = EbeanServerFactory.create(config);
        String sql = applyDdl (ebean, config,postSQL);
        if (sql != null) {
            if (!write (sql, false)) {
                // check to see if if it's the same; if not, 
                if (!checkDdl (sql))
                    write (sql, true);
            }
        }
    }

    boolean write (String sql, boolean override) throws IOException {
        if (override || !ddl.exists()) {
            PrintStream ps = new PrintStream (new FileOutputStream (ddl));
            ps.print(sql);
            ps.close();
            System.err.println("DDL written to file..."+ddl);
            return true;
        }
        return false;
    }

    boolean checkDdl (String sql) throws Exception {
        DigestInputStream dis = new DigestInputStream
            (new FileInputStream (ddl), md);
        byte[] buf = new byte[1024];
        for (int nb; (nb = dis.read(buf, 0, buf.length)) != -1; )
            ;
        dis.close();
        
        String sha1 = toHex (md.digest());
        return sha1.equals(sha1 (sql));
    }

    Pattern TABLE = Pattern.compile("\\s+(table)\\s+([^\\s\\(\\.]+)");
    Pattern CONSTRAINT = Pattern.compile("\\s+(constraint)\\s+([^\\s\\(\\.]+)");
    Pattern INDEX = Pattern.compile("\\s+(index)\\s+([^\\s\\(\\.]+)");

    String tidySQL (Pattern pattern, String sql, String prefix,
                    Map<String, String> mapping) {
        Matcher m = pattern.matcher(sql);
        StringBuilder sb = new StringBuilder ();
        
        int pos = 0;
        while (m.find()) {
            String cons = m.group(2);
            int i = m.start(2);
            sb.append(sql.substring(pos, i));
            String map = mapping.get(cons);
            if (map == null) {
                mapping.put(cons, map = prefix + sha1 (cons, 8));
            }
            pos = m.end(2);
            sb.append(map);
            if (Character.isJavaIdentifierPart(sql.charAt(pos))) {
                sb.append(' ');
            }
        }
        sb.append(sql.substring(pos));
        
        return sb.toString();
    }

    String sha1 (String name) {
        return sha1 (name, 0);
    }

    String sha1 (String name, int length) {
        try {
            byte[] digest = md.digest(name.getBytes("utf8"));
            if (length > 0)
                return toHex (digest).substring(0, length);
            return toHex (digest);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    static String toHex (byte[] d) {
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < d.length; ++i)
            sb.append(String.format("%1$02x", d[i]& 0xff));
        return sb.toString();
    }
        
    public String applyDdl (EbeanServer server, ServerConfig config, String postSQL)
        throws SQLException {
        DdlGenerator ddl = new DdlGenerator();
        ddl.setup((SpiEbeanServer)server, config.getDatabasePlatform(), config);

        Map<String, String> mapping = new HashMap<String, String>();    
        String ups = tidySQL
            (INDEX, tidySQL (CONSTRAINT,
                             ddl.generateCreateDdl(),
                             "c_", mapping), "i_", mapping);
        //System.out.println(ups);
        
        String downs = tidySQL
            (INDEX, tidySQL (CONSTRAINT,
                             ddl.generateDropDdl(),
                             "c_", mapping), "i_", mapping);
        
        if (ups == null || ups.trim().isEmpty()) {
            return null;
        }
        if(postSQL!=null)
        	ups+="\n"+postSQL;

        Connection con = datasource.getConnection();
        try {
//            Statement stm = con.createStatement();
//            ResultSet rset = stm.executeQuery
//                ("select count(*) from play_evolutions");
//            if (rset.next()) {
//                int count = rset.getInt(1);
//                if (count > 0)
//                    execute (downs);
//            }
//            rset.close();
            execute (downs);
            execute (ups);
            
            // if we get here, then all is good.. so truncate the
            //  play_evolutions table
//            stm.execute("delete from play_evolutions");
//            stm.close();
            /*
            PreparedStatement pstm = con.prepareStatement
                ("insert into play_evolutions values(?, ?, ?, ?, ?, ?, ?)");
            pstm.setInt(1, 1);
            pstm.setString(2, sha1 (downs.trim()+ups.trim()));
            pstm.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstm.setString(4, ups);
            pstm.setString(5, downs);
            pstm.setString(6, "applied");
            pstm.setString(7, "");
            pstm.executeUpdate();
            pstm.close();
            */
        }
        finally {
            con.close();
        }
        
        return (
            "# --- Created by Ebean DDL\r\n" +
            "# To stop Ebean DDL generation, remove this comment and start using Evolutions\r\n" +
            "\r\n" + 
            "# --- !Ups\r\n" +
            "\r\n" + 
            ups +
            "\r\n" + 
            "# --- !Downs\r\n" +
            "\r\n" +
            downs
                     );
    }
    
    public static void main (String[] argv) throws Exception {
        String file = System.getProperty("config.file");
        System.err.println("config.file="+file);

        new Evolution (file, "default");
    }
}
