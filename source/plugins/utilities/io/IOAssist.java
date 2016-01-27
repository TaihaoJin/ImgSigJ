/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.io;
import ij.IJ;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Point;

/**
 *
 * @author Taihao
 */
public class IOAssist {
    public static final int IntSize=Integer.SIZE/Byte.SIZE,CharSize=Character.SIZE/Byte.SIZE,
            DoubleSize=Double.SIZE/Byte.SIZE,FloatSize=Float.SIZE/Byte.SIZE,ShortSize=Short.SIZE/Byte.SIZE;

    public static void writeString(DataOutputStream ds, String st)throws IOException {
        int len;
        ds.writeInt(st.length());
        ds.writeChars(st);        
    }
    public static String readString(BufferedInputStream bf)throws IOException {
        int len;
        byte[] pb=new byte[IntSize];
        bf.read(pb);
        len=ByteConverter.toInt(pb);
        pb=new byte[CharSize*len];
        bf.read(pb);
        String st=new String();
        char ch;
        for(int i=0;i<len;i++){
            ch= (char)(
                    (0xff & pb[i*CharSize]) << 8   |
                    (0xff & pb[i*CharSize+1]) << 0
                    );
            st+=ch;
        }
        return st;
    }
    public static int readInt(BufferedInputStream bf)throws IOException {
        byte[] pb=new byte[IntSize];
        bf.read(pb);
        return ByteConverter.toInt(pb);
    }
    public static double readDouble(BufferedInputStream bf)throws IOException {
        byte[] pb=new byte[DoubleSize];
        bf.read(pb);
        return ByteConverter.toDouble(pb);
    }
    public static String readLine(BufferedReader br){
        String line;
        try{line=br.readLine(); }
        catch (IOException e){
            return null;
        }
        return line;
    }
    public static double[] readDoubleArray(BufferedInputStream bf, int len)throws IOException {
        double[] pdt=new double[len];
        byte[] pb=new byte[IOAssist.DoubleSize*len];
        bf.read(pb);
        ByteConverter.getDoubleArray(pb, 0, IOAssist.DoubleSize*len, pdt, 0, len);
        return pdt;
    }
    public static double[] readDoubleArray(BufferedInputStream bf)throws IOException {
        int len=readInt(bf);
        return readDoubleArray(bf,len);
    }
    public static void readDoubleArrayList(BufferedInputStream bf,ArrayList<Double> dv)throws IOException {
        dv.clear();
        int len=readInt(bf);
        for(int i=0;i<len;i++){
            dv.add(readDouble(bf));
        }
    }
    public static void writeDoubleArrayList(DataOutputStream ds, ArrayList<Double> dv) throws IOException{
        int i,len=dv.size();
        ds.writeInt(len);
        for(i=0;i<len;i++){
            ds.writeDouble(dv.get(i));
        }
    }
    public static void readPointArrayList(BufferedInputStream bf,ArrayList<Point> dv)throws IOException {
        dv.clear();
        int len=readInt(bf),x,y;
        for(int i=0;i<len;i++){
            x=readInt(bf);
            y=readInt(bf);
//            x=(int)(readDouble(bf)+0.5);
//            y=(int)(readDouble(bf)+0.5);
            dv.add(new Point(x,y));
        }
    }
    public static void writePointArrayList(DataOutputStream ds, ArrayList<Point> dv) throws IOException{
        int i,len=dv.size();
        Point p;
        ds.writeInt(len);
        for(i=0;i<len;i++){
            p=dv.get(i);
            ds.writeInt(p.x);
            ds.writeInt(p.y);
        }
    }
    public static void writeDoubleArray(DataOutputStream ds, double[] pdt) throws IOException{
        int i,len=pdt.length;
        ds.writeInt(len);
        for(i=0;i<len;i++){
            ds.writeDouble(pdt[i]);
        }
    }
}
