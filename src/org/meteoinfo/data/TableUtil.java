/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataTable;

/**
 *
 * @author yaqiang
 */
public class TableUtil {

    /**
     * Read data table from ASCII file
     *
     * @param fileName File name
     * @param delimiter Delimiter
     * @param headerLines Number of lines to skip at begining of the file
     * @param formatSpec Format specifiers string
     * @param encoding Fle encoding
     * @return TableData object
     * @throws java.io.FileNotFoundException
     */
    public static TableData readASCIIFile(String fileName, String delimiter, int headerLines, String formatSpec, String encoding) throws FileNotFoundException, IOException, Exception {
        DataTable dTable = new DataTable();

        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
        if (headerLines > 0) {
            for (int i = 0; i < headerLines; i++) {
                sr.readLine();
            }
        }

        String title = sr.readLine().trim();
        String[] titleArray = GlobalUtil.split(title, delimiter);
        int colNum = titleArray.length;
        if (headerLines == -1){
            for (int i = 0; i < colNum; i++){
                titleArray[i] = "Col_" + String.valueOf(i);
            }
        }
        boolean hasTimeCol = false;
        String tcolName = null;
        if (titleArray.length < 2) {
            System.out.println("File Format Error!");
            sr.close();
        } else {
            //Get fields
            String[] colFormats;
            if (formatSpec == null) {
                colFormats = new String[colNum];
                for (int i = 0; i < colNum; i++) {
                    colFormats[i] = "C";
                }
            } else {
                colFormats = formatSpec.split("%");
            }

            int idx = 0;
            boolean isBreak = false;
            for (String colFormat : colFormats) {
                if (colFormat.isEmpty()) {
                    continue;
                }

                int num = 1;
                if (colFormat.length() > 1 && !colFormat.substring(0, 1).equals("{")) {
                    int index = colFormat.indexOf("{");
                    if (index < 0) {
                        index = colFormat.length() - 1;
                    }
                    num = Integer.parseInt(colFormat.substring(0, index));
                    colFormat = colFormat.substring(index);
                }
                for (int i = 0; i < num; i++) {
                    if (colFormat.equals("C") || colFormat.equals("s")) //String
                    {
                        dTable.addColumn(titleArray[idx], DataTypes.String);
                    } else if (colFormat.equals("i")) //Integer
                    {
                        dTable.addColumn(titleArray[idx], DataTypes.Integer);
                    } else if (colFormat.equals("f")) //Float
                    {
                        dTable.addColumn(titleArray[idx], DataTypes.Float);
                    } else if (colFormat.equals("d")) //Double
                    {
                        dTable.addColumn(titleArray[idx], DataTypes.Double);
                    } else if (colFormat.equals("B")) //Boolean
                    {
                        dTable.addColumn(titleArray[idx], DataTypes.Boolean);
                    } else {
                        if (colFormat.substring(0, 1).equals("{")) {    //Date
                            int eidx = colFormat.indexOf("}");
                            String formatStr = colFormat.substring(1, eidx);
                            dTable.addColumn(new DataColumn(titleArray[idx], DataTypes.Date, formatStr));
                            hasTimeCol = true;
                            if (tcolName == null) {
                                tcolName = titleArray[idx];
                            }
                        } else {
                            dTable.addColumn(titleArray[idx], DataTypes.String);
                        }
                    }
                    idx += 1; 
                    if (idx == colNum) {
                        isBreak = true;
                        break;
                    }
                }
                if (isBreak)
                    break;
            }

            if (idx < colNum) {
                for (int i = idx; i < colNum; i++) {
                    dTable.addColumn(titleArray[i], DataTypes.String);
                }
            }

            String[] dataArray;
            int rn = 0;
            String line;
            if (headerLines == -1)
                line = title;
            else
                line = sr.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    line = sr.readLine();
                    continue;
                }
                dataArray = GlobalUtil.split(line, delimiter);
//                if (dataArray.length < colNum) {
//                    continue;
//                }

                dTable.addRow();
                int cn = 0;
                for (int i = 0; i < dataArray.length; i++) {
                    dTable.setValue(rn, cn, dataArray[i]);
                    cn++;
                }
                if (cn < colNum){
                    for (int i = cn; i < colNum; i++)
                        dTable.setValue(rn, i, "");
                }

                rn += 1;
                line = sr.readLine();
            }

            sr.close();
        }

        if (hasTimeCol) {
            TimeTableData tableData = new TimeTableData();
            tableData.dataTable = dTable;
            tableData.setTimeColName(tcolName);
            return tableData;
        } else {
            TableData tableData = new TableData();
            tableData.dataTable = dTable;
            return tableData;
        }
    }

    /**
     * To data type - MeteoInfo
     *
     * @param dt Data type string
     * @return Data type
     */
    public static DataTypes toDataTypes(String dt) {
        if (dt.contains("%")) {
            dt = dt.split("%")[1];
        }
        switch (dt.toLowerCase()) {
            case "c":
            case "s":
            case "string":
                return DataTypes.String;
            case "i":
            case "int":
                return DataTypes.Integer;
            case "f":
            case "float":
                return DataTypes.Float;
            case "d":
            case "double":
                return DataTypes.Double;
            default:
                if (dt.substring(0, 1).equals("{")) {    //Date
                    return DataTypes.Date;
                } else {
                    return DataTypes.String;
                }
        }
    }

    /**
     * Get date format string
     *
     * @param dt Format string
     * @return Date format string
     */
    public static String getDateFormat(String dt) {
        int sidx = dt.indexOf("{");
        int eidx = dt.indexOf("}");
        String formatStr = dt.substring(sidx + 1, eidx);
        return formatStr;
    }
}
