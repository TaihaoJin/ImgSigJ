/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ScriptManager;
import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import utilities.CommonGuiMethods;
import utilities.CommonMethods;
import utilities.io.AsciiInputAssist;
import utilities.Gui.PrefHandler;
import utilities.io.FileAssist;

/**
 *
 * @author Taihao
 */
public class MySQL_ScriptRunner implements PlugIn{
    Statement stmt = null;
    String m_sScriptFileName;
    String m_sScriptFileDirectory;
    String m_sHomeDir=System.getProperty("user.home");
    ArrayList <String> m_vsCommondLines;
    Hashtable m_cStoredVariables=new Hashtable();
    int m_nNumCommands;
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost/";

   //  Database credentials
   static final String USER = "Taihao";
   static final String PASS = "Logo1923#";
   
   public static Connection con = null;
    public void run(String Arg){
        m_sScriptFileName="Not Opened";
        runScript();
    }
    void runScript(){
        long startTime = System.currentTimeMillis();
//        getScriptFileName();
        inputScript();
        int nSize=m_vsCommondLines.size();
        for(int i=0;i<nSize;i++){
            runMySQLCommand(m_vsCommondLines.get(i));
        }
        long endTime = System.currentTimeMillis();
        IJ.showMessage(nSize+" commonds", "Elapsed Time (ms): "+Long.toString(endTime-startTime));
    }
    void getScriptFileName()
    {
        JFileChooser jfc=CommonGuiMethods.openFileDialogExtFilter("input a MySQL script file", JFileChooser.OPEN_DIALOG, "script file", "Mql",OpenDialog.getDefaultDirectory());
        m_sScriptFileName=jfc.getSelectedFile().getName();
        m_sScriptFileDirectory=jfc.getSelectedFile().getAbsolutePath();
        m_sScriptFileDirectory=CommonMethods.getDirectory(m_sScriptFileDirectory);
        m_sHomeDir=m_sScriptFileDirectory;
    }
    public int runMySQLCommand(String command){
         if(command.contentEquals("NewConnection;")){
             getDefaultSQLConnection();
             return 1;
         }
         if(stmt==null) createStatement();
         try{
            stmt.execute(command);
         }catch(SQLException se){
           //Handle errors for JDBC
            se.printStackTrace();
         }   
         return 1;
    }
    public void createStatement(){
        if(con==null) getDefaultSQLConnection();
        stmt=null;
        try{
           stmt = con.createStatement();
        }catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        }   
    }
    void inputScript () {
        if(m_vsCommondLines==null) m_vsCommondLines=new ArrayList();
        m_vsCommondLines.clear();
        String path;
        PrefHandler.loadPrefs();
        path=FileAssist.getFilePath("Import a MySQL script file", PrefHandler.getPref("MySQLDir"), "inp file", "Mql", true);
        PrefHandler.updatePref("MySQLDir", path);
        PrefHandler.savePrefs();
        
        ArrayList <String> vsInputLines=new ArrayList <String>();
        AsciiInputAssist.getFileContentsAsStringArray(path, vsInputLines);
        buildScript(vsInputLines);
    }
    void buildScript(ArrayList<String> vsInputLines){
        //Current version allows no more than one ';' in each line, and the line has to end with ';' if it does have one.
        m_vsCommondLines=new ArrayList();
        int i,j,size=vsInputLines.size();
        String st,st1,commond="";
        for(i=0;i<size;i++){
            st=vsInputLines.get(i);
            if(st.startsWith("!")) continue;
            commond+=st;
            st.trim();
            if(st.endsWith(";")){
                m_vsCommondLines.add(commond);
                commond="";
            }
        }
    }    

   public static void getDefaultSQLConnection(){
       if(con==null){
       try {
           Class.forName(JDBC_DRIVER);
           ArrayList<String> labels=new ArrayList(),texts=new ArrayList();
           String title="Login info for MySQL server";
           labels.add("DB_URL");
           texts.add("jdbc:mysql://localhost/");
           labels.add("User");
           texts.add("Taihao");
           texts=CommonGuiMethods.getLoginInfo(title, labels, texts);
           con = DriverManager.getConnection(texts.get(0), texts.get(1), texts.get(2));
       } catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        }catch(Exception e){
           //Handle errors for Class.forName
           e.printStackTrace();
        }finally{
           //finally block used to close resources
        }//end try
       }
   }

}
