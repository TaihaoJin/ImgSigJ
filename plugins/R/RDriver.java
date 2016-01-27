/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package R;

/**
 *
 * @author Taihao
 */
import java.awt.Frame;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Enumeration;

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.RMainLoopCallbacks;

class TextConsole implements RMainLoopCallbacks{
    //This is a class from examples of JRI package
    public void rWriteConsole(Rengine re, String text, int oType) {
        System.out.print(text);
    }
    
    public void rBusy(Rengine re, int which) {
        System.out.println("rBusy("+which+")");
    }
    
    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        System.out.print(prompt);
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
            String s=br.readLine();
            return (s==null||s.length()==0)?s:s+"\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: "+e.getMessage());
        }
        return null;
    }
    
    public void rShowMessage(Rengine re, String message) {
        System.out.println("rShowMessage \""+message+"\"");
    }
	
    public String rChooseFile(Rengine re, int newFile) {
	FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
	fd.show();
	String res=null;
	if (fd.getDirectory()!=null) res=fd.getDirectory();
	if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
	return res;
    }
    
    public void   rFlushConsole (Rengine re) {
    }
	
    public void   rLoadHistory  (Rengine re, String filename) {
    }			
    
    public void   rSaveHistory  (Rengine re, String filename) {
    }			
}

public class RDriver {
    public static Rengine RE;
    public static void main(String[] args) {
	// just making sure we have the right version of everything
	if (!Rengine.versionCheck()) {
	    System.err.println("** Version mismatch - Java files don't match library version.");
	    System.exit(1);
	}
        String args1[]={};
        System.out.println("Creating Rengine (with arguments)");
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		//    (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
        if(RE==null) RE=new Rengine(args1, true, new TextConsole());
    }    
    public static Rengine getRengine(){
        String args[]={};
        if(RE==null) RE=new Rengine(args, true, new TextConsole());
        return RE;
    }
}
