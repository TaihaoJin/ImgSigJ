/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.io;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class AsciiInputAssist {
    public static double[][] readAsciiArray(String path){
        double pdData[][];
        int pnDim[]=new int[3];
        getArrayFormat(path,pnDim);
        pdData=new double[pnDim[0]][pnDim[1]];
        readAsciiArray(path,pdData,pnDim);
        return pdData;
    }
    public static BufferedReader getBufferedReader(String path){
        File f=new File(path);
        FileReader fr=null;
        try{
            fr=new FileReader(f);
        }
        catch(FileNotFoundException e){
            IJ.error("IOException in getBufferedReader");
        }
        BufferedReader br=new BufferedReader(fr);
        return br;
    }
    public static String readLine(BufferedReader br){
        String line=null;
        try{
            line=br.readLine();
        }
        catch(IOException e){
            IJ.error("IOException in readLine");
        }
        if(line.isEmpty()) line=readLine(br);
        return line;
    }
    public static void readAsciiArray(String path, double[][] pdData, int[] pnDim){
        int rows=pnDim[0];
        int cols=pnDim[1];
        int offset=pnDim[2];
        File f=new File(path);
        FileReader fr=null;
        try{
            fr=new FileReader(f);
        }
        catch(FileNotFoundException e){
            IJ.error("IOException in readAsciiArray");
        }
        BufferedReader br=new BufferedReader(fr);
        String line;
        int i,j;
        for(i=0;i<offset;i++){
            try{
                line=br.readLine();
            }
            catch(IOException e){
                IJ.error("IOException in readAsciiArray");
            }
        }
        for(i=0;i<rows;i++){
            try{
                line=br.readLine();
                readNumbers(line,pdData[i],cols);
            }
            catch(IOException e){
                IJ.error("IOException in readAsciiArray");
                break;
            }

        }
    }
    public static int getArrayFormats(String path, ArrayList<int[]> pvforms){
        //getting the dimension of all arraies int the file
        pvforms.clear();
        
        File f=new File(path);
        FileReader fr=null;
        try{
            fr=new FileReader(f);
        }
        catch(FileNotFoundException e){
            IJ.error("IOException in readAsciiArray");
        }
        BufferedReader br=new BufferedReader(fr);
        String line;
        int start=-1,end=1,lines=0;
        int offset=0,rows=0,cols=0;
        ArrayList <Integer> nvNumPositions0=new ArrayList(), nvNumPositions=new ArrayList();
        try{
            line=br.readLine();
            getNumberPositions(line,nvNumPositions0);
        }
        catch (IOException e){
                IJ.error("IOException in readAsciiArray");
                return -1;
        }
        lines=1;
        while(true){
            try{
                line=br.readLine();
                if(line==null) {
                    if(start>=0){
                        rows=end-start+1;
                        cols=nvNumPositions0.size();
                        offset=start;
                        int[] form={rows,cols,offset};
                        pvforms.add(form);
                        start=-1;
                        end=-1;
                    }
                    break;
                }
                getNumberPositions(line,nvNumPositions);
                lines++;
                if(nvNumPositions0.isEmpty()&&nvNumPositions.isEmpty()) continue;
                if(CommonMethods.equal(nvNumPositions0, nvNumPositions)){
                    if(start<0){//starting an array
                        start=lines-2;
                        end=lines-1;
                    }else{
                        end=lines-1;
                    }
                }else{
                    if(start>=0){
                        rows=end-start+1;
                        cols=nvNumPositions0.size();
                        offset=start;
                        int[] form={rows,cols,offset};
                        pvforms.add(form);
                        start=-1;
                        end=-1;
                    }
                    CommonStatisticsMethods.copyArray(nvNumPositions, nvNumPositions0);
                }
            }
            catch(IOException e){
                IJ.error("IOException in readAsciiArray");
                break;
            }
        }
        return 1;
    }
    public static int getArrayFormat(String path, int[] forms){
        //getting the dimension of the array in the file which have the largest number of elements
        ArrayList<int[]> pvforms=new ArrayList();
        getArrayFormats(path,pvforms);
        
        int i,len=pvforms.size(),ix=0,size,sx=0;
        if(len==0) {//no array found in the file.
            forms[0]=-1;
            forms[1]=-1;
            forms[2]=-1;
            return -1;
        }
        
        //choosing the arrays with the largest size        
        int[] form=pvforms.get(0);
        sx=form[0]*form[1];
        for(i=1;i<len;i++){
            form=pvforms.get(i);
            size=form[0]*form[1];
            if(size>sx){
                sx=size;
                ix=i;
            }
        }
        CommonStatisticsMethods.copyArray(pvforms.get(ix), forms);
        return 1;
    }
    public static void  getFileContentsAsStringArray(String path, ArrayList<String> lines){
        File f=new File(path);
        FileReader fr=null;
        try{
            fr=new FileReader(f);
        }
        catch(FileNotFoundException e){
            IJ.error("IOException in readAsciiArray");
        }
        BufferedReader br=new BufferedReader(fr);
        String line;
        lines.clear();
        ArrayList <Integer> nvNumPositions0=new ArrayList(), nvNumPositions=new ArrayList();
        while(true){
            try{
                line=br.readLine();
                if(line==null) break;
                lines.add(line);
            }
            catch(IOException e){
                IJ.error("IOException in readAsciiArray");
                break;
            }
        }
    }
    public static void getNumberPositions(String st, ArrayList<Integer> positions){
        positions.clear();
        int position=0;
        StringTokenizer stk=new StringTokenizer(st);
        while(stk.hasMoreTokens()){
            if(isNumber(stk.nextToken())){
                positions.add(position);
            }
            position++;
        }
    }
    public static ArrayList<Double> getNumbers(String st, String dilimiters){
        ArrayList<String> ds=getNumberStrings(st,dilimiters);
        ArrayList<Double> dv=new ArrayList();
        for(int i=0;i<ds.size();i++){
            dv.add(Double.parseDouble(ds.get(i)));
        }
        return dv;
    }
    public static ArrayList<String> getNumberStrings(String st, String dilimiters){
        int n=0;
        StringTokenizer stk=new StringTokenizer(st,dilimiters);
        ArrayList<String> ds=new ArrayList();
        String s;
        while(stk.hasMoreTokens()){
            s=stk.nextToken();
            s.trim();
            if(isNumber(s)){
                ds.add(s);
            }
        }
        return ds;
    }
    public static void readNumbers(String st, double pdV[], int nNum){
        int n=0;
        StringTokenizer stk=new StringTokenizer(st);
        String s;
        while(stk.hasMoreTokens()){
            s=stk.nextToken();
            if(isNumber(s)){
                pdV[n]=Double.parseDouble(s);
                n++;
                if(n>=nNum) break;
            }
        }
    }
    public static boolean isNumber(String s){
        try {
            Double.parseDouble(s);
        }
        catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public static int getInt(StringTokenizer stk, int index){
        int i=0,nt=0;
        if(index>=stk.countTokens()) IJ.error("not enough tokens in the string tonkenizer");
        while(stk.hasMoreTokens()){
            if(i==index){
                nt=Integer.parseInt(stk.nextToken());
                break;
            }else{
                stk.nextToken();
                i++;
            }
        }
        return nt;
    }

    public static double getDouble(StringTokenizer stk, int index){
        int i=0;
        double dt=0;
        if(index>=stk.countTokens()) IJ.error("not enough tokens in the string tonkenizer");
        while(stk.hasMoreTokens()){
            if(i==index){
                dt=Double.parseDouble(stk.nextToken());
                break;
            }else{
                stk.nextToken();
                i++;
            }
        }
        return dt;
    }
    public static String getString(StringTokenizer stk, int index){
        int i=0;
        String st="";
        if(index>=stk.countTokens()) IJ.error("not enough tokens in the string tonkenizer");
        while(stk.hasMoreTokens()){
            if(i==index){
                st=stk.nextToken();
                break;
            }else{
                stk.nextToken();
                i++;
            }
        }
        return st;
    }
}
