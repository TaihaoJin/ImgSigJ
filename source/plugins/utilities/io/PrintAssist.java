/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.io;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.StringTokenizer;
import utilities.ArrayofArrays.DoubleArray;
import utilities.QuickFormatter;
import javax.swing.*;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.IntPair;
import utilities.CustomDataTypes.DoubleRange;

/**
 *
 * @author Taihao
 */
public class PrintAssist {
    //the term printing precision used in this class is the number of digits after the
    //decimal point.
    //the term printing accuracy used in this class is the round off errors because
    //of the limited printing precision.
    static public String newline = System.getProperty("line.separator");
    static public final String UppercaseLetters[]={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    static int defaultPrintingPrecision=4;
    static int defaultMaxPrintingPrecision=8;
    static double defaultPrintingAccuracy=0.001;
    static double minFractionNumberToPrint=0.000000001;
    public static void setDefaultMaxPrintingPrecision(int precision){
        defaultMaxPrintingPrecision=precision;
    }
    public static void setDefaultPrintingPrecision(int precision){
        defaultPrintingPrecision=precision;
    }
    public static void setDefaultPrintingAccuracy(int accuracy){
        defaultPrintingAccuracy=accuracy;
    }
    public static String ToString_Order(int i0){
        String st=ToString(i0);
        if(i0==1)
            st+="-st";
        else if(i0==2)
            st+="-nd";
        else if(i0==3)
            st+="-rd";
        else
            st+="-th";
        return st;
    }
    public static String ToString(int n0)
    {
        int n=Math.abs(n0);
        int index=0;
        int digits=getDigits(n);
        ArrayList<Character> vChars=new ArrayList();
        int it,i;
        char ct;
        for(i=0;i<digits;i++){
            it=n%10;
            n/=10;
            ct=(char)(it+48);
            vChars.add(ct);
        }
        String s=new String();
        if(n0<0) s+="-";
        for(i=0;i<digits;i++){
            it=digits-1-i;
            s+=vChars.get(it);
        }
        return s;
    }

    public static String ToString(double dNum0, int nPrecision)
    {
        double dNum=dNum0;
        String s=new String();
        int i;
        if(dNum<0){
            s+="-";
            dNum*=-1;
        }
        double dF=0.5;
        for(i=0;i<nPrecision;i++){
            dF/=10.;
        }
        dNum+=dF;

        int it=(int) dNum;
        s+=ToString(it);
        dNum-=it;
        if(nPrecision>0){
            s+=".";
            for(i=0;i<nPrecision;i++){
                dNum*=10;
                it=(int)dNum;
                dNum-=it;
                s+=ToString(it);
            }
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

    static public void printString(Formatter fm, String sT0, int nW){
        String sT=sT0;
        int len=sT.length();
        int i;

        if(len>nW){
            len=Math.max(1, nW-2);
            sT=new String();
            for(i=0;i<len;i++){
                sT+="*";
            }
        }
        int nNumSpaces=nW-len;
        String spaces=new String();
        for(i=0;i<nNumSpaces;i++){
            spaces+=' ';
        }
        fm.format("%s",spaces);
        fm.format("%s",sT);
     }
    static public int printNumber(Formatter fm, double dNum, int nWidth, int nPrecision){
        if(nPrecision<0) {
            printNumberScientific(fm,dNum,nWidth,defaultPrintingPrecision);
            return -1;
        }
        String sT=ToString(dNum,nPrecision);
        if(sT.length()>=nWidth) {
            printNumberScientific(fm,dNum,nWidth,defaultPrintingPrecision);
            return -1;
        }
        printString(fm,sT,nWidth);
        return 1;
    }
    static public void endLine(Formatter fm){
        fm.format("%s",newline);
    }
    static public int getDigits(double dNum){
        //this method return the number of digits of abs(dNum)
        //for dNum>=1, this methods returns the number of the digits
        //of the whole number part, with positive sign.
        //for dNum<1, it returns the number of digits until the first none
        //zero digit in the fractional part of the number,with negative sign
        int nMaxIter=20, nIter;
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
     static public int getPrintgLength(ArrayList<Double> array, ArrayList<Integer> precisions){
        int len,lenx=0,nPrecision=4;
        int size=array.size();
        double dx;
        for(int i=1;i<size;i++){
            dx=array.get(i);
            len=getPrintingLengthF(dx,nPrecision);
            if(len>lenx) lenx=len;
        }
        return lenx;
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
     static public int getPrintgLength(double pdData[], int nPrecision){
        int len,len1,len2;
        boolean first;
        double dmin=pdData[0],dmax=pdData[0],dx;
        int size=pdData.length;
        for(int i=1;i<size;i++){
            dx=pdData[i];
            if(dx>dmax) dmax=dx;
            if(dx<dmin) dmin=dx;
        }
        len=getPrintingLengthF(dmin,nPrecision);
        len2=getPrintingLengthF(dmax,nPrecision);
        if(len2>len) len=len2;
        return len;
    }
     static public int getPrintgLength(double pdData[][], int col, int nPrecision){
        int len,len1,len2;
        boolean first;
        double dmin=pdData[0][col],dmax=pdData[0][col],dx;
        int size=pdData.length;
        for(int i=1;i<size;i++){
            dx=pdData[i][col];
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
        int nCon=2;//nCon: number of 0 or 9 in consecutive digital positions
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
        int nMinFW=st.length()+2;
        if(nFieldWidth<nMinFW) nFieldWidth=nMinFW;
        printString(fm,st,nFieldWidth);
    }
    static public void printArrays(Formatter fm, ArrayList <String> columnTitles, ArrayList <Integer> precisions, ArrayList <DoubleArray> arrays){
        printArrays(fm,null,columnTitles,precisions,null,true,arrays);
    }
    static public void printArrays(Formatter fm, ArrayList<String> rowTitles,ArrayList <String> columnTitles, ArrayList <Integer> precisions, ArrayList<IntPair> spacerLines,boolean precisionsAppliedToColumn,ArrayList <DoubleArray> arrays){
        int nSize=columnTitles.size();
        int i,j,lines=0,numSpacers=0,spacerPosition=0,spacers=0;
        if(spacerLines!=null) {
            numSpacers=spacerLines.size();
            if(numSpacers>0) spacerPosition=spacerLines.get(0).left;
        }

        ArrayList <Integer> FWs=new ArrayList<Integer>();
        int fw,len,rowW=0,offsetR=0;
        if(rowTitles!=null) {
            offsetR=1;
            rowW=getFieldWidth(rowTitles)+2;
            printString(fm,rowTitles.get(0),rowW);
        }
        for(i=0;i<nSize;i++){
            if(precisionsAppliedToColumn){
                fw=getPrintgLength(arrays.get(i).m_DoubleArray,precisions.get(i));
            }else{
                fw=getPrintgLength(arrays.get(i).m_DoubleArray,precisions);
            }
            len=columnTitles.get(i).length();
            if(len>fw) fw=len;
            FWs.add(fw+2);
            printString(fm,columnTitles.get(i),fw+2);
        }
        int rows=arrays.get(0).m_DoubleArray.size();
        fm.format("%s", newline);
        int precision=4;
        for(i=0;i<rows;i++){
            if(rowTitles!=null) printString(fm,rowTitles.get(i+offsetR),rowW);
            if(!precisionsAppliedToColumn) precision=precisions.get(i);
            for(j=0;j<nSize;j++){
                if(precisionsAppliedToColumn) precision=precisions.get(j);
                printNumber(fm,arrays.get(j).m_DoubleArray.get(i),FWs.get(j),precision);
            }
            endLine(fm);
            lines++;
            if(lines==spacerPosition){
                for(j=0;j<spacerLines.get(spacers).right;j++){
                    endLine(fm);
                }
                spacers++;
                if(spacers<numSpacers) spacerPosition=spacerLines.get(spacers).left;
            }
        }
    }
    static public String ToString(String st0, int fw){
        int len=st0.length();
        char c;
        String st=new String();
        for(int i=0;i<fw-len;i++){
            st+=" ";
        }
        for(int i=0;i<len;i++){
            st+=st0.charAt(i);
        }
        return st;
    }
    static public ArrayList<String[]> ToStringArrays(ArrayList<String> rowTitles,ArrayList <String> columnTitles, ArrayList <Integer> precisions, ArrayList<IntPair> spacerLines,boolean precisionsAppliedToColumn,ArrayList <DoubleArray> arrays){
        ArrayList<String[]> stringArray=new ArrayList();
        int nSize=columnTitles.size(),nCols;//number of data column
        int i,j,lines=0,numSpacers=0,spacerPosition=0,spacers=0;
        if(spacerLines!=null) {
            numSpacers=spacerLines.size();
            if(numSpacers>0) spacerPosition=spacerLines.get(0).left;
        }

        ArrayList <Integer> FWs=new ArrayList<Integer>();
        int fw,len,rowW=0,offsetR=0;
        String[] line;
        String st;
        int col=0;
        if(rowTitles!=null) {
            offsetR=1;
            rowW=getFieldWidth(rowTitles)+2;
            st=PrintAssist.ToString(rowTitles.get(0),rowW);
            nCols=nSize+1;
            line=new String[nCols];
            line[col]=st;
            col++;
        }else{
            nCols=nSize;
            line=new String[nCols];
        }

        for(i=0;i<nSize;i++){
            if(precisionsAppliedToColumn){
                fw=getPrintgLength(arrays.get(i).m_DoubleArray,precisions.get(i));
            }else{
                fw=getPrintgLength(arrays.get(i).m_DoubleArray,precisions);
            }
            len=columnTitles.get(i).length();
            if(len>fw) fw=len;
            FWs.add(fw+2);
            line[col]=PrintAssist.ToString(columnTitles.get(i), fw+2);
            col++;
        }
        //The field with for each column is all determined
        int rows=arrays.get(0).m_DoubleArray.size();

        stringArray.add(line);
        line=new String[nCols];
        col=0;

        int precision=4;
        int k;
        for(i=0;i<rows;i++){
            if(rowTitles!=null){
                line[col]=PrintAssist.ToString(rowTitles.get(i+offsetR), rowW);
                col++;
            }
            if(!precisionsAppliedToColumn) precision=precisions.get(i);
            for(j=0;j<nSize;j++){
                if(precisionsAppliedToColumn) precision=precisions.get(j);
                st=PrintAssist.ToString(arrays.get(j).m_DoubleArray.get(i),FWs.get(j), precision);
                line[col]=st;
                col++;
            }
            stringArray.add(line);
            line=new String[nCols];
            col=0;
            lines++;
            if(lines==spacerPosition){
                for(j=0;j<spacerLines.get(spacers).right;j++){
                    if(rowTitles!=null){
                        line[col]=PrintAssist.ToString(new String(" "), rowW);
                        col++;
                    }
                    for(k=0;k<nSize;k++){
                        line[col]=ToString(new String(" "),FWs.get(k));
                        col++;
                    }
                    stringArray.add(line);
                    line=new String[nCols];
                    col=0;
//                    lines++;
                }
                spacers++;
                if(spacers<numSpacers) spacerPosition=spacerLines.get(spacers).left;
            }
        }
        return stringArray;
    }
    static public String ToString(double dv,int fw,int precision){
        String st=ToString(dv,precision);
        return ToString(st,fw);
    }
    static public ArrayList<String> getSequencedStrings(String text,int num){
        ArrayList <String> strings=new ArrayList();

        for(int i=0;i<num;i++){
            String string=new String(text);
            string+=i;
            strings.add(string);
        }
        return strings;
    }
    static public void TransposeArraysToPrint(ArrayList<String> rowTitles,ArrayList <String> columnTitles, ArrayList <Integer> precisions, ArrayList <DoubleArray> arrays){
        int rows=arrays.size(),cols=arrays.get(0).m_DoubleArray.size();
        transposeArrayTitlesToPrint(rowTitles,columnTitles,rows,cols);
        getUniformPrecisions_Max(precisions,rows);
        CommonStatisticsMethods.transposeArrays(arrays);
    }
    static public void TransposeArraysToPrint(ArrayList<String> rowTitles,ArrayList <String> columnTitles, ArrayList <Integer> precisions, double[][] pdData, double[][] pdDataT){
        int rows=pdData.length,cols=pdData[0].length;
        transposeArrayTitlesToPrint(rowTitles,columnTitles,rows,cols);
        getUniformPrecisions_Max(precisions,rows);
        CommonStatisticsMethods.transposeArrays(pdData,pdDataT);
    }
    static public void getUniformPrecisions_Max(ArrayList <Integer> precisions,int num){
        int cols=precisions.size();
        int i,nt,nx=0;
        for(i=0;i<cols;i++){
            nt=precisions.get(i);
            if(nt>nx) nx=nt;
        }
        precisions.clear();
        for(i=0;i<num;i++){
            precisions.add(nx);
        }
    }
    static public void transposeArrayTitlesToPrint(ArrayList<String> rowTitles,ArrayList <String> columnTitles,int rows, int cols){
        int len,i,j;
        ArrayList<String> strings;
        if(rowTitles==null){
            int mc = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog (null, "rowTitles should not be null in ", "Errors", mc);
        }else{
            len=rowTitles.size();
            if(len<rows){
                rowTitles.clear();
                strings=getSequencedStrings("row",len);
                rowTitles.add("rows");
                for(i=0;i<len;i++){
                    rowTitles.add(strings.get(i));
                }
            }
        }

        if(columnTitles==null){
            int mc = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog (null, "columnTitles should not be null in ", "Errors", mc);
        }else{
            len=columnTitles.size();
            if(len<cols){
                columnTitles.clear();
                strings=getSequencedStrings("collumn",len);
                for(i=0;i<len;i++){
                    columnTitles.add(strings.get(i));
                }
            }
        }
        ArrayList<String> rts=new ArrayList(rowTitles),cts=new ArrayList(columnTitles);
        rowTitles.clear();
        columnTitles.clear();
        len=rts.size();
        if(len>rows){
            String st=rts.get(0);
            rts.remove(0);
            rowTitles.add(st);
        }else{
            rowTitles.add("Titles");
        }
        len=rts.size();
        for(i=0;i<len;i++){
            columnTitles.add(rts.get(i));
        }
        len=cts.size();
        for(i=0;i<len;i++){
            rowTitles.add(cts.get(i));
        }
    }
    static public int getFieldWidth(ArrayList<String> vsStrings){
        int len=vsStrings.size(),lx=0,l;
        for(int i=0;i<len;i++){
            l=vsStrings.get(i).length();
            if(l>lx)lx=l;
        }
        return lx;
    }
    static public void printArrays(Formatter fm, ArrayList <String> columnTitles, ArrayList <Integer> precisions, double [][] pdData){
        printArrays(fm,null,columnTitles,precisions,null,true,pdData);
    }
    static public void printArrays(Formatter fm, ArrayList<String> rowTitles, ArrayList <String> columnTitles, ArrayList <Integer> precisions, ArrayList<IntPair> spacerLines, boolean precesionsAppliedToColumns,double [][] pdData){
        ArrayList<DoubleArray> arrays=new ArrayList();
        int rows=pdData.length,cols=pdData[0].length;
        int r,c;
        for(c=0;c<cols;c++){
            DoubleArray da=new DoubleArray();
            for(r=0;r<rows;r++){
                da.m_DoubleArray.add(pdData[r][c]);
            }
            arrays.add(da);
        }
        printArrays(fm,rowTitles,columnTitles,precisions, spacerLines,precesionsAppliedToColumns,arrays);
    }
    static public ArrayList<String[]> ToStringArrays(ArrayList<String> rowTitles, ArrayList <String> columnTitles, ArrayList <Integer> precisions, ArrayList<IntPair> spacerLines, boolean precesionsAppliedToColumns,double [][] pdData){
        ArrayList<DoubleArray> arrays=new ArrayList();
        int rows=pdData.length,cols=pdData[0].length;
        int r,c;
        for(c=0;c<cols;c++){
            DoubleArray da=new DoubleArray();
            for(r=0;r<rows;r++){
                da.m_DoubleArray.add(pdData[r][c]);
            }
            arrays.add(da);
        }
        return ToStringArrays(rowTitles,columnTitles,precisions, spacerLines,precesionsAppliedToColumns,arrays);
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
     public static Formatter getQuickFormatter(String path){
         return QuickFormatter.getFormatter(path);
     }
     public static void printString(Formatter fm, String st){
         printString(fm,st,st.length()+2);
     }
     public static void printNumber(Formatter fm, double dt, int nPrecision){
         int len=getDigits((int)dt)+nPrecision+3;
         printNumber(fm,dt,len,nPrecision);
     }
     public static void printArraySingleLine(Formatter fm,double[] pdA,int nPrecision){
         int len=pdA.length,pl;
         double dv;
         for(int i=0;i<len;i++){
            dv=pdA[i];
            printNumber(fm,dv,nPrecision);
         }
     }
     public static int getPrintingPrecision(DoubleRange dr, int nMaxPrecision){
         double dv=dr.getMax()-dr.getMin();
         return getPrintingPrecisionF(dv,nMaxPrecision);
     }
     public static int getPrintingPrecisionF(double dv0, double printingAccuracy){
         //printingAccuracy is the roundoff error because of printing

         dv0=Math.abs(dv0);
         if(dv0<minFractionNumberToPrint) return 0;
         double dv=dv0*printingAccuracy;
         if(dv>=1) return 0;
         int precision=0;
         if(dv<1) precision=Math.abs(getDigits(dv));

         int nV0=(int)(dv0*Math.pow(10, precision)+0.5);
         int nV=nV0%10;
         while(nV==0&&nV0>=1){
             if(precision==0) break;
             precision--;
             nV0/=10;
             nV=nV0%10;
         }
         return precision;
     }
     public static int getPrintingPrecision_Column(double[][] pdData, int column, int iI, int iF, int nDelta, double printingAccuracy){
         int np,nx=-1;
         double dv;
         for(int i=iI;i<=iF;i+=nDelta){
             dv=pdData[i][column];
             np=getPrintingPrecisionF(dv,printingAccuracy);
             if(np>nx) nx=np;
         }
         return nx;
     }
     public static int getPrintingPrecisionF(double[]pdData, int iI, int iF, int nDelta, double printingAccuracy){
         int np,nx=-1;
         double dv;
         for(int i=iI;i<=iF;i+=nDelta){
             dv=pdData[i];
             np=getPrintingPrecisionF(dv,printingAccuracy);
             if(np>nx) nx=np;
         }
         return nx;
     }
     public static int getPrintingPrecision_Row(double[][] pdData, int row, int iI, int iF, int nDelta, double printingAccuracy){
         int np,nx=-1;
         double dv;
         for(int i=iI;i<=iF;i+=nDelta){
             dv=pdData[row][i];
             np=getPrintingPrecisionF(dv,printingAccuracy);
             if(np>nx) nx=np;
         }
         return nx;
     }
     public static double trimDigits(double dv, int digits){
         double accuracy=Math.pow(10, digits);
         dv/=accuracy;
         dv=(int)dv;
         dv*=accuracy;
         return dv;
     }
     public static double trimDigits(double dv, double da){
         int digits=getDigits(dv);
         return trimDigits(dv,digits);
     }
    public static String getCoordinatesAsString(double pdX[],double accuracy){
        int len=pdX.length,precision,i;
        String st="(";
        for(i=0;i<len;i++){
            if(i>0) st+=",";
            precision=PrintAssist.getPrintingPrecisionF(pdX[i], accuracy);
            st+=PrintAssist.ToString(pdX[i], precision);
        }
        st+=")";
        return st;
    }
}
