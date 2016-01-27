/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.io;
import java.io.*;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class LittleEndianIO {
    public static short readShortLittleEndian(DataInputStream ds){
        int lo=0, hi=0;
        try{
            lo=0xff&ds.readByte();
        } catch (IOException e){
            IJ.error(e+" --- in readShortLittleEndian LittleEndianIO");
        }
        try{
            hi=0xff&ds.readByte();
        } catch (IOException e){
            IJ.error(e+" --- in readShortLittleEndian LittleEndianIO");
        }
        short s=(short)((hi<<8)|(lo));
        return s;
    }
    public static int readIntLittleEndian(DataInputStream ds){
        int it=0,t=0;
        for(int i=0;i<4;i++){
            try{
                t=0xff&ds.readByte();
            } catch (IOException e){
                IJ.error(e+" --- in readIntLittleEndian LittleEndianIO");
            }
            it|=t<<i*8;
        }
        return it;
    }
    public static float readFloatLittleEndian(DataInputStream ds){
        int it=readIntLittleEndian(ds);
        float f=Float.intBitsToFloat(it);
        return f;
    }
    public static void writeShortLittleEndian(DataOutputStream ds,short s){
        int hi=0xff&(s>>8), lo=0xff&(s);
        try{
            ds.writeByte((byte)lo);
            ds.writeByte((byte)hi);
        } catch (IOException e){
            IJ.error(e+" --- in writeShortLittleEndian LittleEndianIO");
        }
    }
    public static void writeIntLittleEndian(DataOutputStream ds, int it){
        for(int i=0;i<4;i++){
            try{
                ds.writeByte((byte)(0xff&it));
            } catch (IOException e){
                IJ.error(e+" --- in writeIntLittleEndian LittleEndianIO");
            }
            it=it>>8;
        }
    }
    public static void writeFloatLittleEndian(DataOutputStream ds, float ft){
        int it=Float.floatToRawIntBits(ft);
        writeIntLittleEndian(ds,it);
    }
}
