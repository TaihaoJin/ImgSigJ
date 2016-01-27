/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.io.*;

/**
 *
 * @author Taihao
 */
public class StringReader {
    public StringReader(){        
    }
    static public String readString(DataInputStream DI, int length) throws IOException {
        String s="";
        for(int i=0;i<length;i++){
            s=s+DI.readChar();
        }
        return s;
    }
}
