/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.io;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.util.Formatter;
import java.io.FileNotFoundException;
import ij.IJ;
import ij.Prefs;
import utilities.CommonGuiMethods;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import utilities.io.IOAssist;

/**
 *
 * @author Taihao
 */
public class FileAssist {
    public static String defaultDirectory = Prefs.getString(Prefs.DIR_IMAGE);//the folder for images
    public static String getUserHome(){//the home directory of user
        return System.getProperty("user.home")+ File.separator;
    }
    public static String getUserDir(){//where ImageJ is installed
        return System.getProperty("user.dir")+ File.separator;
    }
    public static boolean fileExists(String path){
        File file=new File(path);
        return (file.exists());
    }
    public static String getFileName(String path){
        String name;
        int position=path.lastIndexOf(File.separator);
        if(position>0){
            name=path.substring(position+1);
        }else{
            name=path;
        }
        position=name.lastIndexOf('.');
        if(position>0){
            name=name.substring(0,position);
        }
        return name;
    }
    public static String getDirectory(String path){
        String name;
        int position=path.lastIndexOf(File.separator);
        if(position>0){
            name=path.substring(0,position+1);//in order to contain the pather separator as part of the directory
        }else{
            name="";
        }
        return name;
    }
    public static String getFileExtention(String title){
        String ext;
        int position=title.lastIndexOf('.');
        if(position>0){
            ext=title.substring(position+1);//in order not to contain the dot '.'
        }else{
            ext="";
        }
        return ext;
    }
    public static JFileChooser openFileDialogExtFilter(String sTitle,int type, String sFileType, String sExt, String sDir){//type=JFileChooser.OPEN_DIALOG or JFileChooser.SAVE_DIALOG
            JFileChooser chooser = new JFileChooser();
            if(sExt.length()==3){
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    sFileType, sExt);
                chooser.setFileFilter(filter);
            }
            chooser.setDialogTitle(sTitle);
            chooser.setDialogType(type);
            chooser.setCurrentDirectory(new File(sDir));
            int returnVal = chooser.showOpenDialog(null);
            return chooser;
    }
    static public Formatter getFormatter (String path){
        Formatter fm=null;
        File file=new File(path);
        try {
                fm= new Formatter(file);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Fount");
        }
        return fm;
    }
    
    static public Formatter getFormatter (String path, boolean append){
        Formatter fm=null;
        File file=new File(path);
        FileWriter writer=null;
        try{
            writer=new FileWriter(file,append);
        }
        catch (IOException e){
             IJ.error("IOException in getFormatter (String path, boolean append)");
        }
        fm= new Formatter(writer);
        return fm;
    }
    
    public static String getFilePath(String sTitle, String sDefaultDirectory,String sFileType, String sExt,boolean bOpening){
        String path;
        int nType;
        if(bOpening){
            nType=JFileChooser.OPEN_DIALOG;
        }else{
            nType=JFileChooser.SAVE_DIALOG;
        }
        if(sDefaultDirectory.equals(""))sDefaultDirectory=defaultDirectory;
        JFileChooser jfc=CommonGuiMethods.openFileDialogExtFilter(sTitle, nType, sFileType, sExt, sDefaultDirectory);
        path=jfc.getSelectedFile().getAbsolutePath();
        if(!bOpening){
            if(!path.endsWith(sExt)) path+="."+sExt;
        }else{
            if(!FileAssist.fileExists(path)) path=null;
        }
        return path;
    }     
     
    public static String getFilePath(String sTitle, String sDefaultDirectory,ArrayList<String> sFileTypes, ArrayList<String> sExts,boolean bOpening){
        String path;
        int nType;
        if(bOpening){
            nType=JFileChooser.OPEN_DIALOG;
        }else{
            nType=JFileChooser.SAVE_DIALOG;
        }
//        FileSystemView fsv=FileSystemView.getFileSystemView();
        if(sDefaultDirectory.equals(""))sDefaultDirectory=defaultDirectory;
        JFileChooser jfc=CommonGuiMethods.openFileDialogExtFilter(sTitle, nType, sFileTypes, sExts, sDefaultDirectory);
        path=jfc.getSelectedFile().getAbsolutePath();
        return path;
    }
    public static String changeExt(String path, String sExt){
        return getDirectory(path)+getFileName(path)+"."+sExt;
    }
    public static boolean containsFileNameExtension(String sFileName){
        String sExt=getFileExtention(sFileName);
        return (sExt.length()==3);
    }
    public static String getExtendedFileName(String sFileName, String sExt){
        String dir=getDirectory(sFileName),name=getFileName(sFileName),ext=getFileExtention(sFileName);
        return dir+name+sExt+"."+ext;
    }
    public static ArrayList<String> getFileNamesContainString(ArrayList<String> fileNames,String sub){
        ArrayList<String> names=new ArrayList();
        String st;
        for(int i=0;i<fileNames.size();i++){
            st=fileNames.get(i);
            if(ContainsIgnoreCase(getFileName(st),sub)) 
                names.add(st);
        }
        return names;
    }
    public static boolean ContainsIgnoreCase(String st, String sub){
        int i,len=st.length(),len1=sub.length();
        String s;
        for(i=0;i<=len-len1;i++){
            if(st.substring(i, i+len1).equalsIgnoreCase(sub)) return true;
        }
        return false;
    }
}
