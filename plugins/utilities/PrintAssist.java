/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.StringTokenizer;
import utilities.ArrayofArrays.DoubleArray;

/**
 *
 * @author Taihao
 */
public class PrintAssist {
    static public String newline = System.getProperty("line.separator");
    static public String ToString(int n)
    {
        String s;
        s=Integer.toString(n);
        return s;
    }

    static public String ToString(double dNum, int nPrecision)
    {
        String s;
        s=Double.toString(dNum);
        int index=s.indexOf('.');
        int len0=s.length();
        int len=index+nPrecision+1;
        int i;
        if(len0<len){
            for(i=len0;i<len;i++){
                s+="0";
            }
        }else{
            s=s.substring(0,len);
        }
        return s;
    }


    static public int getDigits(int n)
    {
        int digits=1;
        n/=10;
        while(n!=0){
            digits++;
            n/=10;
        }
        return digits;
    }
    static public int getPrecision(double dV){
        int np=0;
        return np;
    }

    static public void printString(Formatter fm, String sT, int nW){
        int len=sT.length();
        int nNumSpaces=nW-len;
        String spaces=new String();
        for(int i=0;i<nNumSpaces;i++){
            spaces+=' ';
        }
        fm.format("%s",spaces);
        fm.format("%s",sT);
     }
    static public void printNumber(Formatter fm, double dNum, int nWidth, int nPrecision){
        String sT=ToString(dNum,nPrecision);
        printString(fm,sT,nWidth);
    }
    static public void endLine(Formatter fm){
        fm.format("%s",newline);
    }
    static public int getDigits(double dNum){
        int nMaxIter=10, nIter;
        double dV=Math.abs(dNum);
        int nd=0;
        if(dV>1.){
            nIter=0;
            while(dV>1&&nIter<nMaxIter){
                nd++;
                dV/=10.;
                nIter++;
            }
        }else if(dV<1.){
            nIter=0;
            while(dV<1.&&nIter<nMaxIter){
                nd--;
                nIter++;
                dV*=10.;
            }
        }else{
            nd=1;
        }
        return nd;
    }
     static public int getPrintingLengthF(double dV, int nPrecision){
        int len=getDigits((int)dV);
        if(dV<0) len++;
        if(nPrecision>0) len+=nPrecision+1;
        return len;
    }
     static public int getPrintgLength(ArrayList<Double> array, int nPrecision){
        int len,len1,len2;
        boolean first;
        double dmin=array.get(0),dmax=array.get(0),dx;
        int size=array.size();
        for(int i=1;i<size;i++){
            dx=array.get(i);
            if(dx>dmax) dmax=dx;
            if(dx<dmin) dmin=dx;
        }
        len=getPrintingLengthF(dmin,nPrecision);
        len2=getPrintingLengthF(dmax,nPrecision);
        if(len2>len) len=len2;
        return len;
    }
     static public double round(double dV, int nPrecision){
        int sign=1;
        if(dV<0) sign=-1;
        dV=Math.abs(dV);
        int factor=1;
        for(int i=0;i<nPrecision;i++){
            factor*=10;
        }
        dV*=factor;
        if((dV-(int)dV)>=0.5) dV+=1.;
        dV/=factor;
        return dV;
    }

     static public int getPrintingPrecisionF(double dV, int nMaxPrecision){//
        dV=Math.abs(dV);
        int nd=0;
        int nCon=2;//nCon consecutive 0 or 9
        while(dV<1.)
        {
            dV*=10;
            nd++;
            if(nd>nMaxPrecision) return nd;
        }
        int dig0,dig1;
        dig0=(int)dV;
        dV-=dig0;
        dV*=10;
        nd++;
        dig1=(int)dV;
        dV-=dig1;
        while(true){
            if(dig0==0&&dig1==0)
                return nd-2;
            if(dig0==9&&dig1==9)
                return nd-2;
            if(nd>nMaxPrecision) break;
            dV*=10;
            nd++;
            dig0=dig1;
            dig1=(int)dV;
            dV-=dig1;
        }
        return nd;
    }
    
     static public boolean StringDelimiter(char ch, String sDelimiters, int nQuotationOpened[]){
        int len=sDelimiters.length();
        if(ch=='"'){
            if(nQuotationOpened[0]==1){//Quotation mark is possibly applying, and a quotation is opened
                nQuotationOpened[0]=-1; //the quotation is closed
                return true;
            }else if(nQuotationOpened[0]==-1){//Quotation mark is possibly applying
                nQuotationOpened[0]=1;//quotation opened
                return true;
            }
        }
        if(nQuotationOpened[0]==1) return false;//Ignore the quoted delimiters"
        for(int i=0;i<len;i++){
            if(ch==sDelimiters.charAt(i)) return true;
        }
        return false;
    }

    static public ArrayList <String> TokenizeString(String sline, String sDelimiters, boolean bApplyQuotation){//delimiters not applying to quoted strings
   //not including the delimiters
        StringTokenizer st;
        int nQuotationOpened[]=new int[1];
        nQuotationOpened[0]=0;
        if(bApplyQuotation) nQuotationOpened[0]=-1;//quotation mark apply, but the quotation is not open
        ArrayList <String> Strings=new ArrayList<String>();
        int len=sline.length();
        char ch;
        int index=0;
        //ch=(index<len)?sline.charAt(index):'';
        while(index<len){
            String s=new String();
            ch=sline.charAt(index);
            while(!StringDelimiter(ch,sDelimiters,nQuotationOpened)){
                s+=ch;
                index++;
                if(index>=len) break;
                ch=sline.charAt(index);
            }
            if(s.length()>0)Strings.add(s);
            index++;
        }
        return Strings;
    }

     static public String ToStringScientific(double dValue, int nPrecision){
        int nd=getDigits(dValue);
        int nShift=0;
        double dF=.1;
        int nDel=-1;
        if(nd<0){
            dF=10.;
            nDel=1;
            nd++;
        }
        double dV=dValue;
        while(nd!=0){
            dV*=dF;
            nShift-=nDel;
            nd+=nDel;
        }
        String ct=ToString(dV,nPrecision);
        ct+="E";
        ct+=ToString(nShift);
        return ct;
    }

     static public void printNumberScientific(Formatter fm, double dV, int nFieldWidth, int nPrecision){
        String st=ToStringScientific(dV,nPrecision);
        int nMinFW=st.length()+1;
        if(nFieldWidth<nMinFW) nFieldWidth=nMinFW;
        printString(fm,st,nFieldWidth);
    }
     static public void printArrays(Formatter fm, ArrayList <String> columnTitles, ArrayList <Integer> precisions, ArrayList <DoubleArray> arrays){
        int nSize=columnTitles.size();
        int i,j;
        ArrayList <Integer> FWs=new ArrayList<Integer>();
        int fw,len;
        for(i=0;i<nSize;i++){
            fw=getPrintgLength(arrays.get(i).m_DoubleArray,precisions.get(i));
            len=columnTitles.get(i).length();
            if(len>fw) fw=len;
            FWs.add(fw+2);
            printString(fm,columnTitles.get(i),fw+2);
        }
        int rows=arrays.get(0).m_DoubleArray.size();
        fm.format("%s", newline);
        for(i=0;i<rows;i++){
            for(j=0;j<nSize;j++){
                printNumber(fm,arrays.get(j).m_DoubleArray.get(i),FWs.get(j),precisions.get(j));
            }
            endLine(fm);
        }
    }
     static public String makeLine(ArrayList <String> strings, int index1, int index2){
         String aString=new String(), st=new String();
         for(int i=index1;i<=index2;i++){
             st=strings.get(i);
             if(st.contains(" "))st='"'+st+'"';
             aString+=" "+st;
         }
         return aString;
     }
}
