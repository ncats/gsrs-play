package ix.ncats.moldev.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.nih.ncgc.imaging.MolDevUtils;
import play.mvc.Result;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MoldevPlateApp extends MoldevApp {

    public static Result getPlates() throws SQLException {
        makeConnection();
        PreparedStatement pst = hcsConn.prepareStatement("select plate_id, plate_name, plate_description, time_created, global_id from plates order by plate_id");
        ResultSet rs = pst.executeQuery();
        ArrayNode root = mapper.createArrayNode();
        while (rs.next()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("plate_id", rs.getInt("plate_id"));
            node.put("plate_name", rs.getString("plate_name"));
            node.put("plate_description", rs.getString("plate_description"));
            node.put("time_created", rs.getDate("time_created").toString());
            node.put("global_id", rs.getString("global_id"));
            root.add(node);
        }
        pst.clearParameters();
        pst.close();
        closeConnection();
        return ok(root);
    }

    public static Result getPlateImages(String id, String wavelength, String montage) throws SQLException {
        makeConnection();
        MolDevUtils mdu = new MolDevUtils(getRepositoryPath());
        boolean makeMontage = montage != null && montage.toLowerCase().equals("true");

        Long plateId;
        if (isNumber(id)) plateId = Long.parseLong(id);
        else plateId = mdu.getMXPlateId(id);
        if (plateId == null) return notFound("No such plate: " + id);

        PreparedStatement pst = hcsConn.prepareStatement("select plate_id, plate_name, plate_description, time_created, global_id, x_wells, y_wells from plates where plate_id = ?");
        pst.setLong(1, plateId);
        ResultSet rs = pst.executeQuery();

        String plateName = "";
        while (rs.next()) {
            plateName = rs.getString("plate_name");
        }
        rs.close();
        pst.close();

        ObjectNode wellNodes = mapper.createObjectNode();

        HashMap<String, WellImage> whash = new HashMap<>();
        pst = hcsConn.prepareStatement("select well_x, well_y, SITE_ID FROM MDCSTORE.SITE where PLATE_ID = ?  order by well_x, well_y, site_id");
        pst.setLong(1, plateId);
        rs = pst.executeQuery();
        while (rs.next()) {
            int row = rs.getInt("well_y");
            int col = rs.getInt("well_x");
            long sid = rs.getLong("site_id");


            String path;
            if (!makeMontage) path = "/images/" + plateName + "/" + row + "/" + col + "/" + sid + "/" + wavelength;
            else path = "/images/" + plateName + "/" + row + "/" + col + "/-1/" + wavelength;

            WellImage wi;
            String key = row + "#" + col;
            if (whash.containsKey(key)) {
                wi = whash.get(key);
                wi.paths.add(path);
            } else {
                wi = new WellImage();
                wi.setCol(col);
                wi.setRow(row);
                wi.paths.addAll(Arrays.asList(new String[]{path}));
            }
            whash.put(key, wi);
        }
        pst.close();
        rs.close();


        JsonNode wells = mapper.valueToTree(whash.values());
        wellNodes.put("wells", wells);
        return ok(wellNodes);
    }

    public static Result getPlate(String id) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        makeConnection();
        MolDevUtils mdu = new MolDevUtils(getRepositoryPath());

        Long plateId;
        if (isNumber(id)) plateId = Long.parseLong(id);
        else plateId = mdu.getMXPlateId(id);

        if (plateId == null) return notFound("No such plate: " + id);

        PreparedStatement pst = hcsConn.prepareStatement("select plate_id, plate_name, plate_description, time_created, global_id, x_wells, y_wells from plates where plate_id = ?");
        pst.setLong(1, plateId);
        ResultSet rs = pst.executeQuery();

        ObjectNode node = mapper.createObjectNode();
        int nrow = -1;
        int ncol = -1;

        while (rs.next()) {
            node.put("plate_id", plateId);
            node.put("plate_name", rs.getString("plate_name"));
            node.put("plate_description", rs.getString("plate_description"));
            node.put("time_created", rs.getDate("time_created").toString());
            node.put("global_id", rs.getString("global_id"));
            nrow = rs.getInt("y_wells");
            ncol = rs.getInt("x_wells");
            node.put("nrow", nrow);
            node.put("ncol", ncol);
        }
        rs.close();
        pst.close();

        List<String> wls = mdu.getWavelengths(node.get("plate_name").textValue());
        ArrayNode wlsNode = mapper.createArrayNode();
        for (String wl : wls) wlsNode.add(wl);
        node.put("wavelengths", wlsNode);
        return ok(node);
    }

}