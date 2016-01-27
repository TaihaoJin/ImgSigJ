/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

/**
 *
 * @author Taihao
 */
import java.util.ArrayList;
import utilities.io.PrintAssist;
import utilities.CommonGuiMethods;
import utilities.CustomDataTypes.intRange;
import java.util.StringTokenizer;
public class CommonStringMethods {
    public static ArrayList<String> getLabelsInRange(String compoundLabel){
        ArrayList labels=new ArrayList();
        if(!compoundLabel.contains("-")) {
            labels.add(compoundLabel);
            return labels;
        }
        ArrayList<String> sts=PrintAssist.TokenizeString(compoundLabel, "-", true);
        if(sts.size()!=2) {
            CommonGuiMethods.showMessage("Worng Format for the method \"getLabelsInRange\"");
            return null;
        }
        String label1=sts.get(0),label2=sts.get(1);
        ArrayList<String> ls1=new ArrayList(),ns1=new ArrayList(),ls2=new ArrayList(),ns2=new ArrayList();
        splitNumberAndLetters(label1,ls1,ns1);
        splitNumberAndLetters(label2,ls2,ns2);
        
        boolean wrongLabel=false;
        if(ls1.size()!=1||ns1.size()!=1||ls2.size()!=1||ns2.size()!=1) {
            if(ls2.size()==0&&ls1.size()==1) 
                ls2.add(ls1.get(0));//A1-3 become A1-A3
            else if(ns1.size()==0&&ns2.size()==1){
                ns1.add(ns2.get(0));//A-H3 become A3-H3
            }else{
                wrongLabel=true;
            }
        }
        if(!ls1.get(0).equalsIgnoreCase(ls2.get(0))&&!ns1.get(0).equalsIgnoreCase(ns2.get(0))) wrongLabel=true;
        if(ls1.get(0).length()!=1||ls2.get(0).length()!=1) wrongLabel=true;//need to change it to accomadate different plate formats.
        if(wrongLabel){
            CommonGuiMethods.showMessage("Worng Format for the method \"getLabelsInRange\""+System.lineSeparator()+compoundLabel);
            return labels;
        }
        String letters=ls1.get(0),label;
        int n;
        if(!ls1.get(0).equalsIgnoreCase(ls2.get(0))) {
            char c1=ls1.get(0).charAt(0),c2=ls2.get(0).charAt(0),c=c1;
            while(c<=c2){
                labels.add(c+ns1.get(0));
                c++;
            }
        }else{
            for(int i=Integer.parseInt(ns1.get(0));i<=Integer.parseInt(ns2.get(0));i++){
                labels.add(letters+i);
            }
        }
        return labels;
    }
    static public char increaseLetter(char c){
        c++;
        char c2=c;
        return c2;
    }
    public static boolean IsInt_ByRegex(String str)
    {
        return str.matches("^-?\\d+$");
    }
    public static boolean IsUpperCaseLetter(char c){
        return c>='A'&&c<='Z';
    }
    public static boolean IsUpperCaseString(String st){
        for(int i=0;i<st.length();i++){
            if(!IsUpperCaseLetter(st.charAt(i))) return false;
        }
        return true;
    }
    static public int splitNumberAndLetters(String st, ArrayList<String> letterStrings, ArrayList<String> numberStrings){
        ArrayList<intRange> letterIndexRanges=new ArrayList(), numberIndexRanges=new ArrayList();
        splitNumberAndLetterIndexRanges(st,letterIndexRanges,numberIndexRanges);
        letterStrings.clear();
        numberStrings.clear();
        int i,len=numberIndexRanges.size();
        intRange ir;
        for(i=0;i<len;i++){
            ir=numberIndexRanges.get(i);
            numberStrings.add(st.substring(ir.getMin(), ir.getMax()+1));
        }
        len=letterIndexRanges.size();
        for(i=0;i<len;i++){
            ir=letterIndexRanges.get(i);
            letterStrings.add(st.substring(ir.getMin(), ir.getMax()+1));
        }
        return 1;
    }
    static public int splitNumberAndLetterIndexRanges(String st,ArrayList<intRange> letterIndexRanges, ArrayList<intRange> numberIndexRanges){
        intRange letterRange=new intRange(),numberRange=new intRange(),ir;
        ArrayList<intRange> irs;
        boolean addRange;
        for(int i=0;i<st.length();i++){
            addRange=false;
            if(isNumber(st.charAt(i))){
                ir=numberRange;
                irs=numberIndexRanges;
            } else {
                ir=letterRange;
                irs=letterIndexRanges;
            }
            
            if(ir.contains(i-1))
                ir.expandRange(i);
            else if(ir.emptyRange()){
                ir.expandRange(i);
                addRange=true;
            } else {
                ir=new intRange(i,i);
                addRange=true;
            }
            if(addRange) irs.add(ir);
        }
        return 1;
    }
    public static boolean isNumber(char c){
        return (c>=48&&c<=57);
    }
    public static int appendStrings(ArrayList<String> sts1, ArrayList<String> sts2){
        for(int i=0;i<sts2.size();i++){
            sts1.add(sts2.get(i));
        }
        return 1;
    }
    public static boolean startsWithIgnoreCase(String st1, String st2){
        int len=st2.length();
        if(len>st1.length()) return false;
        String s=st1.substring(0, len);
        return s.equalsIgnoreCase(st2);
    }
    public static String getArrayListAsString(ArrayList<Integer> nv){
        if(nv.size()==0) return "";
        String st=""+nv.get(0);
        for(int i=1;i<nv.size();i++){
            st+=","+nv.get(i);
        }
        return st;
    }
    public static String getCommonSubString(String st1, String st2, boolean startingOnly){
        String st="";
        int i,len=Math.min(st1.length(), st2.length()),p=0;
        for(i=0;i<len;i++){
            if(st1.charAt(i)!=st2.charAt(i)) break;
            p++;
        }   
        return st1.substring(0,p);
    }
    public static String getCommonSubstring(ArrayList<String> sv,boolean startingOnly){
        String st=sv.get(0);
        for(int i=1;i<sv.size();i++){
            st=getCommonSubString(st,sv.get(i),startingOnly);
        }
        return st;
    }
    public static boolean ContainsString(ArrayList<String> svSts, String st, boolean ignorCase){
        for(int i=0;i<svSts.size();i++){
            if(!ignorCase)
                if(svSts.get(i).contentEquals(st)) return true;
            else
                if(svSts.get(i).equalsIgnoreCase(st)) return true;    
        }
        return false;
    }
    public static int getIndex(ArrayList<String> svSts, String st, boolean ignorCase){
        int i,index=-1,size=svSts.size();
        for(i=0;i<size;i++){
            if(!ignorCase){
                if(svSts.get(i).contentEquals(st)) return i;
            } else {
                if(svSts.get(i).equalsIgnoreCase(st)) return i;    
            }
        }
        return -1;
    }
    public static int NumOfStrings(ArrayList<String> svSts, String st, boolean ignorCase){
        int num=0;
        for(int i=0;i<svSts.size();i++){
            if(!ignorCase){
                if(svSts.get(i).contentEquals(st)) num++;
            } else {
                if(svSts.get(i).equalsIgnoreCase(st)) num++;    
            }
        }
        return num;
    }
    public static ArrayList<String> getContainedStrings(String st0, String delimiters){
        StringTokenizer stk=new StringTokenizer(st0,delimiters);
        ArrayList<String> svt=new ArrayList();
        String st;
        while(stk.hasMoreElements()){
            st=stk.nextToken();
            st.trim();
            svt.add(st);
        }
        return svt;        
    }
    public static String[] getWhiteStringArray(int len){
        String[] ps=new String[len];
        for(int i=0;i<len;i++){
            ps[i]=" ";
        }
        return ps;
    }
}

