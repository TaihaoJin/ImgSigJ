/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.Hashtable;
import utilities.io.PrintAssist;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author Taihao
 */
public class ArgumentDecoder {
    ArrayList <String> m_svArgs;
    String m_sDelimiters;
    Hashtable m_cArgTable;
    boolean m_bApplyingQuotation;
    public ArgumentDecoder (String args, String sDelimiters, boolean bApplyingQuotation){
        m_bApplyingQuotation=bApplyingQuotation;
        m_sDelimiters=sDelimiters;
        m_svArgs=PrintAssist.TokenizeString(args, m_sDelimiters, m_bApplyingQuotation);
        m_cArgTable=buildHashtable(m_svArgs);
    }
    public ArgumentDecoder (ArrayList <String> svArgs, String sDelimiters, boolean bApplyingQuotation){
        m_svArgs=svArgs;
        m_cArgTable=buildHashtable(svArgs);
    }
    public static Hashtable buildHashtable(ArrayList <String> svArgs){
        Hashtable hTable=new Hashtable();
        int num=svArgs.size();
        num/=2;
        String key, value;
        int index=0;
        for(int i=0;i<num;i++){
            key=svArgs.get(index);
            index++;
            value=svArgs.get(index);
            index++;
            hTable.put(key, value);
        }
        return hTable;
    }
    public Object get(String key){
        return m_cArgTable.get(key);
    }
    public String getString(String key){
        return (String) m_cArgTable.get(key);
    }
    public double getDouble(String key){
        return Double.valueOf((String) m_cArgTable.get(key));
    }
    public int getInteger(String key){
        return Integer.valueOf( (String) m_cArgTable.get(key));
    }
    public static String getLine(Hashtable hTable){
        Enumeration en=hTable.keys();
        String key, value;
        String line=new String();
        while(en.hasMoreElements()){
            key=(String) en.nextElement();
            if(key.contains(" ")) key='"'+key+'"';
            line+=key;
            line+=' ';
            value=(String) hTable.get(key);
            if(value.contains(" ")) value='"'+value+'"';
            line+=value;
            line+=' ';
        }
        int index=line.length()-2;
        if(index>=0) line=line.substring(0,index);
        return line;
    }
    public static ArrayList <String> getKeysBeginningWith(Hashtable hTable,String start){
        Enumeration en=hTable.keys();
        String key;
        ArrayList <String> keys=new ArrayList();
        while(en.hasMoreElements()){
            key=(String) en.nextElement();
            if(key.startsWith(start)) keys.add(key);
        }
        return keys;
    }
    public static ArrayList <String> getKeysAsStrings(Hashtable hTable){
        Enumeration en=hTable.keys();
        String key;
        ArrayList <String> keys=new ArrayList();
        while(en.hasMoreElements()){
            key=(String) en.nextElement();
            keys.add(key);
        }
        return keys;
    }
}
