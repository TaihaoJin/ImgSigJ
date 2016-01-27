/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Demo;
import ij.plugin.PlugIn;
import BrainSimulations.DataClasses.IDNode;
import java.io.*;
import utilities.StringReader;

/**
 *
 * @author Taihao
 */
public class FileReadingDemo {
    public FileReadingDemo(){
        String s="it is a test";
        int i=10;
        int length;
        float f=8.33f;
        char c='a';
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\";
        String fileName="test.dat";
        try{FileOutputStream fo=new FileOutputStream(dir+fileName);
        DataOutputStream DO=new DataOutputStream(fo);
        length=fileName.length();
        DO.writeInt(length);
        DO.writeChars(fileName);
        DO.writeFloat(f);
        length=s.length();
        DO.writeInt(length);
        DO.writeChars(s);
        DO.writeInt(i);
        length=dir.length();
        DO.writeInt(length);
        DO.writeChars(dir);
        DO.writeChar(i);
        DO.close();
        fo.close();
        StringReader sr=new StringReader();
        FileInputStream fi=new FileInputStream(dir+fileName);
        BufferedInputStream bi=new BufferedInputStream(fi);
        long la=bi.available();
        DataInputStream DI=new DataInputStream(bi);
        int l=DI.readInt();
        String f1=sr.readString(DI, l);
        float fl=DI.readFloat();
        l=DI.readInt();
        String f2=sr.readString(DI, l);
        DI.close();
        bi.close();
        fi.close();}
        catch (FileNotFoundException e1){}
        catch (IOException e2){};
    }
}
