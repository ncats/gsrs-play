package ix.ginas.utils;

import java.io.*;
import java.util.zip.*;
import java.sql.*;

import ix.core.processing.RecordExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.zaxxer.hikari.HikariDataSource;

public class CheckJsonDump {
    /* to run this class, try something like this:
sbt ginas/"runMain ix.ginas.utils.CheckJsonDump jsonDump2016-04-25.gsrs jdbc:mysql://hostname:3306/ixginas?user=ginas&password=foobar"
     */
    public static void main (String[] argv) throws Exception {
        if (argv.length < 2) {
            System.out.println("Usage: ix.ginas.utils.CheckJsonDump FILE JDBC_URL");
            System.exit(1);
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(argv[1]);
        
        Connection con = ds.getConnection();
        PreparedStatement pstm = con.prepareStatement
            ("select 1 from ix_ginas_substance where uuid = ?");
        try {
            InputStream is = new GZIPInputStream
                (new FileInputStream (argv[0]));
            BufferedReader br = new BufferedReader (new InputStreamReader (is));
            int count = 0, not = 0;
            PrintStream ps = new PrintStream
                (new FileOutputStream ("not_matched.txt"));
            for (String line; (line = br.readLine()) != null; ++count) {
                String[] tokens = line.split("\t");
                String unii = tokens[0].split("\\s")[1];
                System.out.print(String.format("%1$ 10d: ", count)
                                 +" "+unii+" "+tokens[1]+"...");
                pstm.setString(1, tokens[1]);
                ResultSet rset = pstm.executeQuery();
                if (rset.next()) {
                    System.out.print("yes");
                }
                else {
                    ++not;
                    System.out.print("no");
                    ps.println(line);
                }
                rset.close();           
                System.out.println();
            }
            is.close();
            ps.close();
            System.out.println(count+" record(s) read; "+not+" not found!");
        }
        finally {
            con.close();
        }
    }
}
