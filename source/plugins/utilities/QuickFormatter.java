/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.io.*;
import java.util.Formatter;

/**
 *
 * @author Taihao
 */
public class QuickFormatter {
    protected Formatter m_fm;
    public QuickFormatter(){
        
    }
    public QuickFormatter (String path){        
        File file=new File(path);        
        try {
            m_fm= new Formatter(file);
        }
        catch (FileNotFoundException e){
            System.out.print("File not found"+path);
        }
    }
    public Formatter getFormatter(){
        return m_fm;
    }
    public static Formatter getFormatter(String path){
        Formatter fm=null;
        File file=new File(path);
        try {
            fm= new Formatter(file);
        }
        catch (FileNotFoundException e){
            System.out.print("File not found"+path);
        }
        return fm;
    }
}
