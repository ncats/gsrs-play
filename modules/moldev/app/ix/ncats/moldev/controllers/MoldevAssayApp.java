package ix.ncats.moldev.controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Result;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Rajarshi Guha
 */
public class MoldevAssayApp extends MoldevApp {
    public static Result getAssays() throws SQLException {
        makeConnection();
        PreparedStatement pst = hcsConn.prepareStatement("select assay_id, assay_name, settings_name, table_id, shape_table_name, to_delete from assays order by assay_id");
        ResultSet rs = pst.executeQuery();
        ArrayNode root = mapper.createArrayNode();
        while (rs.next()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("assay_id", rs.getInt("assay_id"));
            node.put("assay_name", rs.getString("assay_name"));
            node.put("settings_name", rs.getString("settings_name"));
            node.put("table_id", rs.getString("table_id"));
            node.put("shape_table_name", rs.getString("shape_table_name"));
            node.put("to_delete", rs.getInt("to_delete"));
            root.add(node);
        }
        pst.clearParameters();
        pst.close();
        closeConnection();
        return ok(root);
    }
}
