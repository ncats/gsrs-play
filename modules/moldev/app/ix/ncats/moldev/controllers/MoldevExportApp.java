package ix.ncats.moldev.controllers;

import gov.nih.ncgc.rnai.statistics.BaseStats;
import gov.nih.ncgc.util.GuhaMisc;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import play.Logger;
import play.mvc.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rajarshi Guha
 */
public class MoldevExportApp extends MoldevApp {
    static Logger log;

    static Object[][] data;
    static List<String> header, keys;


    public static Result export(String format, String plateName, String settingsName, String aid, String aggFunc)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {
        response().setHeader(CACHE_CONTROL, "max-age=3600");
        switch (format) {
            case "acumen":
                return exportAcumen(plateName, settingsName, aid, aggFunc);
            case "excel":
                return exportExcel(plateName, settingsName, aid, aggFunc);
            default:
                return badRequest("Invalid format was specified");
        }
    }

    public static Result exportAcumen(String plateName, String settingsName, String aid, String aggFunc)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

        try {
            exportData(plateName, settingsName, aid, aggFunc);
        } catch (IllegalArgumentException e) {
            return badRequest("Invalid plate name and/or settings name");
        } catch (IOException e) {
            return notFound(e.getMessage());
        }

        StringBuilder sb = new StringBuilder();

        // set up header
        sb.append("Well Row, Well Column,");
        sb.append(StringUtils.join(header, ",")).append("\n");

        Pattern p = Pattern.compile("([A-Z]*)([0-9]*)");
        // now add in rows
        for (int i = 0; i < keys.size(); i++) {

            Matcher m = p.matcher(keys.get(i));
            m.find();
            String rowString = m.group(1);
            int colString = Integer.parseInt(m.group(2));

            sb.append(rowString).append(",").append(colString).append(",");
            sb.append(StringUtils.join(data[i], ",").replace("NA", "")).append("\n");
        }
        response().setHeader("Content-disposition", "attachment; filename=" + plateName + settingsName + ".csv");
        return ok(sb.toString()).as("text/csv");
    }

    public static Result exportExcel(String plateName, String settingsName, String aid, String aggFunc)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {
        makeConnection();
        if (aid != null) plateName = getPlateNameByAssayId(aid, hcsConn);
        closeConnection();

        try {
            exportData(plateName, settingsName, aid, aggFunc);
        } catch (IllegalArgumentException e) {
            return badRequest("Invalid plate name and/or settings name");
        } catch (IOException e) {
            return notFound(e.getMessage());
        }

        HSSFWorkbook wb = new HSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();

        HSSFFont headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(HSSFColor.BLACK.index);
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        CellStyle style = wb.createCellStyle();
        style.setFont(headerFont);
        style.setBorderBottom(CellStyle.BORDER_THIN);

        for (int col = 0; col < header.size(); col++) {
            String aHeader = header.get(col);

            // add in the col numbers
            String sheetName = aHeader.split("\\.").length > 1 ? aHeader.split("\\.")[1] : "Foo";
            sheetName = sheetName.replace("/", "");
            sheetName = sheetName.replace(":", "");
            sheetName = sheetName.replace(" ", "");
            sheetName = sheetName.replace("Intensity", "Intens");
            sheetName = sheetName.replace("Average", "Avg");
            sheetName = sheetName.replace("Integrated", "Int");
            sheetName = sheetName.replace("Index", "Idx");
            HSSFSheet sheet = wb.createSheet(sheetName);

            Row row = sheet.createRow(0);
            for (int i = 1; i < 49; i++) {
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                cell.setCellValue(i);
                cell.setCellStyle(style);
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            }

            // lets reformat the data for this column
            Object[][] aCol = new Object[32][48]; // we'll never work with more than 1536 ?!
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 48; j++) aCol[i][j] = null;
            }
            for (int i = 0; i < keys.size(); i++) {
                int[] rowcol = GuhaMisc.rowKeyToIndex(keys.get(i));
                Object value = data[i][col];
                aCol[rowcol[0] - 1][rowcol[1] - 1] = value;
            }

            // now put this matrix in
            for (int i = 0; i < 32; i++) {
                row = sheet.createRow(i + 1);
                Cell cell = row.createCell(0);
                cell.setCellValue(i + 1);
                cell.setCellStyle(style);
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                for (int j = 0; j < 48; j++) {
                    if (aCol[i][j] == null) continue;
                    cell = row.createCell(j + 1);
                    if (aCol[i][j] instanceof String) {
                        cell.setCellValue((String) aCol[i][j]);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                    } else if (aCol[i][j] instanceof Double) {
                        cell.setCellValue((Double) aCol[i][j]);
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    } else if (aCol[i][j] instanceof Integer) {
                        cell.setCellValue((Integer) aCol[i][j]);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                    }
                }
            }

            for (int i = 0; i <= header.size(); i++) sheet.autoSizeColumn((short) i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);

        response().setHeader("Content-disposition", "attachment; filename=" + plateName + settingsName + ".xls");
        return ok(baos.toByteArray()).as("application/vnd.ms-excel");

    }


    private static Object[] aggregate(List<Object[]> rows, int[] columnTypes) {
        int ncol = rows.get(0).length;
        Object[] ret = new Object[ncol];


        for (int i = 0; i < ncol; i++) {

            // gather the data for this col
            List<Object> data = new ArrayList<Object>();
            for (Object[] row : rows) {
                if (row[i] != null) data.add(row[i]);
            }
            if (data.size() == 0) ret[i] = "NA";
            else {
                if (columnTypes[i] == java.sql.Types.INTEGER ||
                        columnTypes[i] == java.sql.Types.FLOAT ||
                        columnTypes[i] == java.sql.Types.DOUBLE ||
                        columnTypes[i] == java.sql.Types.NUMERIC ||
                        columnTypes[i] == java.sql.Types.DECIMAL) {
                    ret[i] = aggregateNumber(data);
                } else if (columnTypes[i] == java.sql.Types.VARCHAR) ret[i] = aggregateText(data);
            }
        }
        return ret;
    }

    private static double aggregateNumber(List<Object> data) {
        double[] dat = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) instanceof BigDecimal) {
                dat[i] = ((BigDecimal) data.get(i)).doubleValue();
            } else
                dat[i] = (Double) data.get(i);
        }
        return BaseStats.sampleMedian(dat);
    }

    // assume data can be coerced to List<String>
    private static String aggregateText(List<Object> data) {
        Map<String, Integer> table = new HashMap<String, Integer>();
        for (Object datum : data) {
            if (table.keySet().contains(datum)) {
                Integer value = table.get(datum);
                table.put((String) datum, value + 1);
            } else {
                table.put((String) datum, 1);
            }
        }

        // get key whose value is max
        int max = -99999;
        String maxKey = "";
        for (String key : table.keySet()) {
            if (table.get(key) > max) {
                max = table.get(key);
                maxKey = key;
            }
        }
        return maxKey;
    }

    public static void exportData(
            String plateName,
            String settingsName,
            String aid,
            String aggFunc
    ) throws IllegalAccessException, SQLException, InstantiationException, ClassNotFoundException, IOException {
        if (plateName == null && settingsName == null && aid == null)
            throw new IllegalArgumentException();

        // first see whether we can pull out assays
        makeConnection();
        PreparedStatement pst;

        String tableName = null;

        if (aid == null) {
            pst = hcsConn.prepareStatement("SELECT assays.assay_id," +
                    "  assay_name," +
                    "  settings_name," +
                    "  table_id," +
                    "  row_count," +
                    "  assay_plates.*" +
                    " FROM assays, plates, assay_plates" +
                    " WHERE plate_name    like ? " +
                    " AND assay_plates.plate_id = plates.plate_id" +
                    " AND assays.assay_id        = assay_plates.assay_id" +
                    " AND assays.settings_name like ?" +
                    " AND assay_plates.to_delete = 0");
            pst.setString(1, "%" + plateName + "%");
            pst.setString(2, "%" + settingsName + "%");
            ResultSet resultSet = pst.executeQuery();

            int nassay = 0;
            int assayid = -1;
            tableName = null;
            while (resultSet.next()) {
                assayid = resultSet.getInt("assay_id");
                tableName = resultSet.getString("table_id");
                nassay++;
            }
            if (tableName == null) {
                log.error("No assay data for this combination of plate & settings name");
                throw new IOException("No assay data for this combination of plate & settings name");
            }
            if (nassay != 1) {
                log.error("Multiple assay tables for this combination of plate & settings name");
                throw new IOException("Multiple assay tables for for this combination of plate & settings name");
            }
            pst.clearParameters();
        } else { // just query by aid
            pst = hcsConn.prepareStatement("SELECT assays.assay_id, table_id FROM assays where assays.assay_id = ?");
            pst.setInt(1, Integer.parseInt(aid));
            ResultSet resultSet = pst.executeQuery();
            resultSet.next();
            tableName = resultSet.getString("table_id");
        }

        log.info("Got assay (" + tableName + ") for  " + plateName + " | " + settingsName + "  ");
        // OK, now pull out the table data
        pst = hcsConn.prepareStatement("select * from " + tableName + " order by well, cell_id");
        ResultSet resultSet = pst.executeQuery();
        ResultSetMetaData meta = resultSet.getMetaData();

        // ok lets do aggregation
        Map<String, Object[]> wellData = new HashMap<String, Object[]>();
        int nCol = meta.getColumnCount();
        int nDataCol = nCol - 12;

        // get column types
        List<Integer> tmp = new ArrayList<Integer>();
        for (int i = 13; i <= nCol; i++) tmp.add(meta.getColumnType(i));
        int[] dataColumnTypes = new int[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) dataColumnTypes[i] = tmp.get(i);

        //read in the first row
        boolean status = resultSet.next();
        if (!status) {
            log.error("Assay table [" + tableName + "] had no data!");
            throw new IOException("Assay table [" + tableName + "] had no data!");
        }

        String wellId = resultSet.getString("well");
        String oldWellId = new String(wellId);
        List<Object[]> wellRows = new ArrayList<Object[]>();
        Object[] wellRowData = new Object[nDataCol];
        for (int i = 13; i <= nCol; i++) wellRowData[i - 13] = resultSet.getObject(i);
        wellRows.add(wellRowData);

        while (true) {
            if (!resultSet.next()) break;

            wellId = resultSet.getString("well");
            if (!wellId.equals(oldWellId)) {

                // aggregate this well
                Object[] aggregatedValues = aggregate(wellRows, dataColumnTypes);
                wellData.put(oldWellId, aggregatedValues);

                // store the current well we just read in
                wellRows.clear();
                oldWellId = wellId;
                wellRowData = new Object[nDataCol];
                for (int i = 13; i <= nCol; i++) wellRowData[i - 13] = resultSet.getObject(i);
                wellRows.add(wellRowData);

            } else {
                oldWellId = wellId;
                wellId = resultSet.getString("well");
                wellRowData = new Object[nDataCol];
                for (int i = 13; i <= nCol; i++) wellRowData[i - 13] = resultSet.getObject(i);
                wellRows.add(wellRowData);
            }
        }
        pst.clearParameters();

        // at this point we should, the last set of rows to be aggregated
        Object[] aggregatedValues = aggregate(wellRows, dataColumnTypes);
        wellData.put(oldWellId, aggregatedValues);


        // before we return the data, get info for the header
        pst = hcsConn.prepareStatement("select column_name, meas_name, function_name from table_columns where table_id = ?");
        pst.setString(1, tableName);
        resultSet = pst.executeQuery();
        Map<String, String> colNames = new HashMap<String, String>();
        while (resultSet.next()) {
            colNames.put(resultSet.getString("column_name"), resultSet.getString("function_name") + "." + resultSet.getString("meas_name"));
        }

        // now prepare for output
        keys = new ArrayList<String>(wellData.keySet());
        Collections.sort(keys);

        // work out the header
        header = new ArrayList<String>();
        for (int i = 13; i < nCol; i++) {
            String tableHeaderName = meta.getColumnName(i);
            header.add(colNames.get(tableHeaderName));
        }
        header.add(colNames.get(meta.getColumnName(nCol)));

        // lets sort the keys so AA comes at the end
        List<String> skeys = new ArrayList<String>();
        for (String key : keys) {
            if (!Character.isLetter(key.charAt(1))) skeys.add(key);
        }
        for (String key : keys) {
            if (Character.isLetter(key.charAt(1))) skeys.add(key);
        }
        keys = new ArrayList<String>(skeys);


        data = new Object[keys.size()][];
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            data[i] = wellData.get(key);
        }

        pst.close();
        resultSet.close();
        closeConnection();
        log.info("Done");
    }

    private static String getPlateNameByAssayId(String aid, Connection con) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        PreparedStatement pst = con.prepareStatement("select plate_name from plates a, assay_plates b where assay_id = ? and b.to_delete = 0 and a.plate_id = b.plate_id");
        pst.setInt(1, Integer.parseInt(aid));
        ResultSet rs = pst.executeQuery();
        rs.next();
        String plateName = rs.getString(1);
        pst.close();
        rs.close();
        return plateName;
    }


}
