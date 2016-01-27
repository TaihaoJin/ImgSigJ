/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SQL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import utilities.CommonGuiMethods;

/**
 *
 * @author Taihao
 */
public class CommonSQLMethods {
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost/";

   //  Database credentials
   static final String USER = "Taihao";
   static final String PASS = "Logo1923#";
   
   public static Connection con = null;
   
   static Statement stmt;

   public static Connection getDefaultSQLConnection(){
       return getDefaultSQLConnection(false);
   }
   public static Connection getDefaultSQLConnection(boolean reconnect){
       if(reconnect) con=null;       
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
       return con;
   }
    public static Statement getStatement(){
        if(stmt!=null) return stmt;
        if(con==null) con=getDefaultSQLConnection();        
        try{
           stmt = con.createStatement();
        }catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        }   
        return stmt;
    }
   static public void createDatabase(String sDBName){
   // JDBC driver name and database URL
        if(con==null) con=getDefaultSQLConnection();
        Statement stmt = null;
        try{
           stmt = con.createStatement();

           String sql = "CREATE DATABASE "+sDBName;
           stmt.executeUpdate(sql);
           System.out.println("Database created successfully...");
        }catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        }catch(Exception e){
           //Handle errors for Class.forName
           e.printStackTrace();
        }finally{
           //finally block used to close resources
           try{
              if(stmt!=null)
                 stmt.close();
           }catch(SQLException se2){
           }// nothing we can do
        }//end try
        System.out.println("Goodbye!");        
    }
   public static void dropDatabase(String sDBName){
        // JDBC driver name and database URL   
        Statement stmt = null;
        try{
           stmt = con.createStatement();

           String sql = "DROP DATABASE "+sDBName;
           stmt.executeUpdate(sql);
           System.out.println("Database deleted successfully...");
        }catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        }catch(Exception e){
           //Handle errors for Class.forName
           e.printStackTrace();
        }finally{
           //finally block used to close resources
           try{
              if(stmt!=null)
                 stmt.close();
           }catch(SQLException se){
           }// do nothing
        }//end try
        System.out.println("Goodbye!");       
   }
   public static void createTable(String sDBName, String sTableName){
        Statement stmt = null;
        try{
           System.out.println("Creating table in given database...");
           stmt = con.createStatement();
           String sql="use "+sDBName;
           stmt.executeUpdate(sql);
           sql = "CREATE TABLE "+sTableName +
                        "(id INTEGER not NULL, " +
                        " first VARCHAR(255), " + 
                        " last VARCHAR(255), " + 
                        " age INTEGER, " + 
                        " PRIMARY KEY ( id ))"; 

           stmt.executeUpdate(sql);
           System.out.println("Created table in given database...");
        }catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        }catch(Exception e){
           //Handle errors for Class.forName
           e.printStackTrace();
        }finally{
           //finally block used to close resources
           try{
              if(stmt!=null)
                 stmt.close();
           }catch(SQLException se){
           }// do nothing
        }//end try
   }
   static public void dropTable(String sDBName, String sTableName){
        Statement stmt = null;
        try{
           stmt = con.createStatement();
           String sql="use "+sDBName;
           stmt.executeUpdate(sql);
           sql = "DROP TABLE "+sTableName;

           stmt.executeUpdate(sql);
           System.out.println("Table  deleted in given database...");
        }catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        }catch(Exception e){
           //Handle errors for Class.forName
           e.printStackTrace();
        }finally{
           //finally block used to close resources
           try{
              if(stmt!=null)
                 stmt.close();
           }catch(SQLException se){
           }// do nothing
        }//end try
   }
}
