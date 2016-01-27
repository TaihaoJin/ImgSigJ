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
public class FormattedReader {
    public BufferedReader br;
    public FormattedReader(){
    }
    public FormattedReader(String path){        
            File file = new File(path);                
            try{FileReader f=new FileReader(file);
            br=new BufferedReader(f);
            }
            catch(FileNotFoundException e){
            System.out.println("File not Found: "+path);            
            }
    }
}
