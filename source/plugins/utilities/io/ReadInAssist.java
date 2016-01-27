/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.io;
import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Taihao
 */
public class ReadInAssist {
    public static String gotoLine_Contains (BufferedReader br, String string)throws IOException{
        String sLine=br.readLine();
        while(sLine!=null){//while the end of the stream is not reached
            if(sLine.contains(string)) break;
            sLine=br.readLine();
        }
        return sLine;
    }
    public static String gotoLine_startsWith (BufferedReader br, String string)throws IOException{
        String sLine=br.readLine();
        while(sLine!=null){//while the end of the stream is not reached
            if(sLine.startsWith(string)) break;
            sLine=br.readLine();
        }
        return sLine;
    }
    public static String gotoLine_contains (BufferedReader br, String string)throws IOException{
        String sLine=br.readLine();
        while(sLine!=null){//while the end of the stream is not reached
            if(sLine.contains(string)) break;
            sLine=br.readLine();
        }
        return sLine;
    }
    public static String nextNoneBlankLine(BufferedReader br){
        String sLine="";
        try{
            sLine=br.readLine();
        }
        catch(IOException e){
            return sLine;
        }
        while(isWhiteString(sLine)){
            try{
                sLine=br.readLine();
            }
            catch(IOException e){
                return sLine;
            }
        }
        return sLine;
    }
    public static boolean isBlank(String[] line){
        int i,j,len=line.length;
        String st;
        for(i=0;i<len;i++){
            st=line[i];
            if(!isWhiteString(st)) return false;
        }

        return true;
    }
    public static boolean isWhiteString(String st){
        int i,len=st.length();
        for(i=0;i<len;i++){
            if(st.charAt(i)!=' ') return false;
        }
        return true;
    }
}
