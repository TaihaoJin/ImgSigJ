/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import utilities.CommonGuiMethods;
import utilities.CommonMethods;
import utilities.io.AsciiInputAssist;
import utilities.io.FileAssist;

/**
 *
 * @author Taihao
 */
public class PrefHandler {
    static public String MySQLDir=File.separator;
    static public String PrefPath="ImgSigJ_Prefs.txt";
    static public int loadPrefs(){
        String path=PrefPath;
        File f=new File(path);
        if(!f.exists()) return -1;
        ArrayList<String> sts=new ArrayList();
        AsciiInputAssist.getFileContentsAsStringArray(path, sts);
        if(sts.isEmpty()) return -1;
        String st=sts.get(0);
        int index=st.indexOf(":");
        MySQLDir=st.substring(index+2);
        return 1;
    }
    static public void updatePref(String key, String Val){
        if(key.equalsIgnoreCase("MySQLDir")) {
            File f=new File(Val);
            if(f!=null){
                if(!f.isDirectory()) Val=FileAssist.getDirectory(Val);
                MySQLDir=Val;
            }
        }
    }
    static public int savePrefs(){
        Formatter qf=CommonMethods.QuickFormatter(PrefPath);
        String st="MySQLDir: "+MySQLDir+System.lineSeparator();
        qf.format(st);
        qf.close();
        return 1;
    }
    static public String getPref(String key){
         if(key.equalsIgnoreCase("MySQLDir")) return MySQLDir;
         CommonGuiMethods.showMessage("invalid pref key: "+key);
         return null;
    }    
}
