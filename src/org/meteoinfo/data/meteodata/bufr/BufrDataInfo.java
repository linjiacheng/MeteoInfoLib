/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.bufr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.global.DataConvert;
import ucar.nc2.iosp.bufr.Descriptor;
import ucar.nc2.iosp.bufr.Message;
import ucar.nc2.iosp.bufr.MessageScanner;

/**
 *
 * @author Yaqiang Wang
 */
public class BufrDataInfo {
    // <editor-fold desc="Variables">
     private RandomAccessFile bw = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
     /**
      * Read first message
      * @param fileName Bufr File Name
      * @return First message
      * @throws FileNotFoundException
      * @throws IOException 
      */
    public Message readFirstMessage(String fileName) throws FileNotFoundException, IOException{
        ucar.unidata.io.RandomAccessFile br = new ucar.unidata.io.RandomAccessFile(fileName, "r");
        MessageScanner ms = new MessageScanner(br);
        Message m = ms.getFirstDataMessage();
        br.close();
        return m;
    }
     
     /**
     * Create Bufr binary data file
     *
     * @param fileName File name
     */
    public void createDataFile(String fileName) {
        try {
            bw = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BufrDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Close the data file created by previos step
     */
    public void closeDataFile() {
        try {
            bw.close();
            bw = null;
        } catch (IOException ex) {
            Logger.getLogger(BufrDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Write indicator section
     * @param bufrLength  The total length of the message
     * @param edition Bufr edition
     * @throws IOException 
     */
    public void writeIndicatorSection(int bufrLength, int edition) throws IOException{
        bw.writeBytes("BUFR");
        int[] ints = DataConvert.toUint3Int(bufrLength);
        for (int i : ints){
            bw.write(i);
        }
        bw.write(edition);
    }
    
    /**
     * Write identification section
     * @param len Section length
     * @param master_table Master table
     * @param subcenter_id Subcenter id
     * @param center_id Center id
     * @param update_sequence Update sequency
     * @param optional Optional
     * @param category Category
     * @param sub_category Sub category
     * @param master_table_version Master table version
     * @param local_table_version Local table version
     * @param year Year
     * @param month Month
     * @param day Day
     * @param hour Hour
     * @param minute Minute
     * @throws IOException 
     */
    public void writeIdentificationSection(int len, int master_table, int subcenter_id, int center_id,
            int update_sequence, int optional, int category, int sub_category, int master_table_version,
            int local_table_version, int year, int month, int day, int hour, int minute) throws IOException{
        int[] ints = DataConvert.toUint3Int(len);
        for (int i : ints){
            bw.write(i);
        }
        bw.write(master_table);
        bw.write(subcenter_id);
        bw.write(center_id);
        bw.write(update_sequence);
        bw.write(optional);
        bw.write(category);
        bw.write(sub_category);
        bw.write(master_table_version);
        bw.write(local_table_version);
        bw.write(year);
        bw.write(month);
        bw.write(day);
        bw.write(hour);
        bw.write(minute);
        bw.write(0);
    }
    
    /**
     * Write data description section
     * @param len Section length
     * @param ndatasets Number of datasets
     * @param datatype Data type
     * @param descriptors Data descriptors
     * @throws IOException 
     */
    public void writeDataDescriptionSection(int len, int ndatasets, int datatype,
            List<String> descriptors) throws IOException{
        int[] ints = DataConvert.toUint3Int(len);
        for (int i : ints){
            bw.write(i);
        }
        bw.write(ndatasets);
        bw.write(datatype);
        for (String des : descriptors){
            short fxy = Descriptor.getFxy(des);
            bw.writeShort(fxy);
        }
    }
    // </editor-fold>
}
