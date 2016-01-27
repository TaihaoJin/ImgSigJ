/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.QuickSort;
import utilities.CustomDataTypes.IntPair;
import utilities.ArrayofArrays.IntArray;
import utilities.statistics.MeanSem1;
import utilities.RunningWindowRankingKeeper;
/**
 *
 * @author Taihao
 */
public class LineEnveloper {
    double[] m_pdLine, m_pdRWA, m_pdLoLine, m_pdHiLine;
    ArrayList<Integer> m_nvHiLine, m_nvLoLine;
    ArrayList<Integer> m_nvLn,m_nvLx;
    ArrayList<Boolean> m_bOriginalAnchorsL,m_bOriginalAnchorsH;
    ArrayList<IntArray> m_nvReliableAnchorsL,m_nvReliableAnchorsH;
    ArrayList<IntArray> m_nvTranslocatableAnchorsL,m_nvTranslocatableAnchorsH;
    ArrayList<IntPair> m_cvParallelEnvLineSegs;
    ArrayList<IntPair> m_cvPRSegsL,m_cvPRSegsH;
    MeanSem1 m_cDevLo,m_cDevHi,m_cDevLoN,m_cDevHiN,m_cDevLoX,m_cDevHiX;
//    double m_dMaxDev;
    int m_nWs,m_nRanking, m_nMaxDevLen;
    double m_dParallelCutoff=0.5;
    double m_dInternalYScale;
    public LineEnveloper (double[] pdLine, int nWs, int nRanking, double[] pdLoLine, double[] pdHiLine){
        m_pdLine=pdLine;
        m_nWs=nWs;
        m_nRanking=nRanking;
//        m_dMaxDev=dMaxDev;
        int len=pdLine.length,i;
        m_nvLoLine=new ArrayList();
        m_nvHiLine=new ArrayList();
        m_bOriginalAnchorsL=new ArrayList();
        m_bOriginalAnchorsH=new ArrayList();
        m_nvReliableAnchorsL=new ArrayList();
        m_nvReliableAnchorsH=new ArrayList();
        m_nvLn=new ArrayList();
        m_nvLx=new ArrayList();
        m_pdRWA=CommonStatisticsMethods.getRunningWindowAverage(pdLine, 0, len-1, 1, 10, false);
        m_pdLoLine=pdLoLine;
        m_pdHiLine=pdHiLine;
        CommonMethods.LocalExtrema(m_pdLine, 0, m_pdLine.length-1, m_nvLn,m_nvLx);

        buildEnvelopingLines();
//        refineEnvLines(pdLine,nWs,nRanking,m_nvLoLine,dMaxDev,false);
//        refineEnvLines(pdLine,nWs,nRanking,m_nvHiLine,dMaxDev,true);
//        int nMaxCrossings=2*nRanking;
//        int len1=m_nvLoLine.size();
 //       ArrayList<Integer> nvLeft=new ArrayList(), nvRight=new ArrayList();
 //       checkExcessiveCrossingSegs(pdLine,m_nvLoLine,0,len1-1,nvLeft,nvRight,nMaxCrossings);
 //       buildLine(pdLine,m_nvLoLine,pdLoLine);
//        buildLine(pdLine,m_nvHiLine,pdHiLine);
    }
    public void getAnchors(ArrayList <Integer> nvEnvLine, boolean bHi){
        ArrayList <Integer> nvEnvLinet;
        if(bHi)
            nvEnvLinet=m_nvHiLine;
        else
            nvEnvLinet=m_nvLoLine;
        int len=nvEnvLinet.size(),i;
        for(i=0;i<len;i++){
            nvEnvLine.add(nvEnvLinet.get(i));
        }
    }
    int buildEnvelopingLines(){
        int len=m_pdLine.length,i;

        len=m_pdLine.length;
        buildEnvelopingLine(m_pdLine,m_nWs,0,len-1,m_nRanking,m_nvHiLine,true,0);
        buildEnvelopingLine(m_pdLine,m_nWs,0,len-1,m_nRanking,m_nvLoLine,false,0);

        buildLine(m_pdLine,m_nvLoLine,m_pdLoLine);
        buildLine(m_pdLine,m_nvHiLine,m_pdHiLine);
        pickReliableAnchors(m_pdLine,m_nvLoLine,0,m_nvLoLine.size(),m_nvReliableAnchorsL,false);
        pickReliableAnchors(m_pdLine,m_nvHiLine,0,m_nvLoLine.size(),m_nvReliableAnchorsH,true);
        pickParallelEnvLineSegs();
//        parallelSegmentsOnly();
//        ReliableSegmentsOnly();
        calDevMeanSem();
        setInternalYScale();
        len=m_pdLine.length;
        buildEnvelopingLine(m_pdLine,m_nWs,0,len-1,m_nRanking,m_nvHiLine,true,m_dInternalYScale);
        buildEnvelopingLine(m_pdLine,m_nWs,0,len-1,m_nRanking,m_nvLoLine,false,m_dInternalYScale);
        int firstAnchor=5;
        int nAnchors=m_nvHiLine.size();
        if(nAnchors<10) firstAnchor=nAnchors/2;
        ArrayList<Integer> nvHiLine=new ArrayList();
        buildEnvelopingLine(m_pdLine,m_nWs,m_nvHiLine.get(firstAnchor),0,m_nRanking,nvHiLine,true,m_dInternalYScale);
        len=m_nvHiLine.size();
        CommonStatisticsMethods.invertArray(nvHiLine);
        nvHiLine.remove(0);//ignore the first anchor at the beginning of the line
        for(i=firstAnchor;i<len;i++){
            nvHiLine.add(m_nvHiLine.get(i));
        }
        m_nvHiLine=nvHiLine;

        nAnchors=m_nvLoLine.size();
        if(nAnchors<10) firstAnchor=nAnchors/2;
        ArrayList<Integer> nvLoLine=new ArrayList();
        buildEnvelopingLine(m_pdLine,m_nWs,m_nvLoLine.get(firstAnchor),0,m_nRanking,nvLoLine,false,m_dInternalYScale);
        len=m_nvLoLine.size();
        CommonStatisticsMethods.invertArray(nvLoLine);
        nvLoLine.remove(0);//ignore the first anchor at the beginning of the line
        for(i=firstAnchor;i<len;i++){
            nvLoLine.add(m_nvLoLine.get(i));
        }
        m_nvLoLine=nvLoLine;


        buildLine(m_pdLine,m_nvLoLine,m_pdLoLine);
        buildLine(m_pdLine,m_nvHiLine,m_pdHiLine);
        pickReliableAnchors(m_pdLine,m_nvLoLine,0,m_nvLoLine.size(),m_nvReliableAnchorsL,false);
        pickReliableAnchors(m_pdLine,m_nvHiLine,0,m_nvLoLine.size(),m_nvReliableAnchorsH,true);
        pickParallelEnvLineSegs();
        pickParallelOrReliableSegs();
        translocateReliableSegs();
        return 1;
    }
    void parallelSegmentsOnly(){
        int len=m_cvParallelEnvLineSegs.size(), i, j,left, right;
        double[] pdL=m_pdLoLine, pdH=m_pdHiLine;
        int lent=m_pdLoLine.length;
        pdL=new double[lent];
        pdH=new double[lent];
        IntPair ip;
        for(i=0;i<lent;i++){
            pdL[i]=m_pdLoLine[i];
            pdH[i]=m_pdHiLine[i];
            m_pdLoLine[i]=0;
            m_pdHiLine[i]=0;
        }
        for(i=0;i<len;i++){
            ip=m_cvParallelEnvLineSegs.get(i);
            left=ip.left;
            right=ip.right;
            for(j=left;j<=right;j++){
                m_pdLoLine[j]=pdL[j];
                m_pdHiLine[j]=pdH[j];
            }
        }
    }
    void pickParallelOrReliableSegs(){
        int len=m_pdLine.length,len1,len2;
        boolean[] pbPR=new boolean[len];
        int i,j,k,left,right;
        IntPair ip;

        for(i=0;i<len;i++){
            pbPR[i]=false;
        }

        len1=m_cvParallelEnvLineSegs.size();
        for(i=0;i<len1;i++){
            ip=m_cvParallelEnvLineSegs.get(i);
            left=ip.left;
            right=ip.right;
            for(j=left;j<=right;j++){
                pbPR[j]=true;
            }
        }

        IntArray ia;
        len1=m_nvReliableAnchorsL.size();
        for(i=0;i<len1;i++){
            ia=m_nvReliableAnchorsL.get(i);
            len2=ia.m_intArray.size();
            left=ia.m_intArray.get(0);
            for(j=1;j<len2;j++){
                right=ia.m_intArray.get(j);
                for(k=left;k<=right;k++){
                    pbPR[k]=true;
                }
                left=right;
            }
        }

        m_cvPRSegsL=new ArrayList();

        boolean b0=pbPR[0],b;
        left=-1;
        if(b0) left=0;
        for(i=0;i<len;i++){
            b=pbPR[i];
            if(b0){
                if(!b||i==len-1){
                    right=i;
                    m_cvPRSegsL.add(new IntPair(left,right));
                }
            }else{
                if(b) left=i;
            }
            b0=b;
        }
        
        for(i=0;i<len;i++){
            pbPR[i]=false;
        }


        len1=m_cvParallelEnvLineSegs.size();
        for(i=0;i<len1;i++){
            ip=m_cvParallelEnvLineSegs.get(i);
            left=ip.left;
            right=ip.right;
            for(j=left;j<=right;j++){
                pbPR[j]=true;
            }
        }

        len1=m_nvReliableAnchorsH.size();
        for(i=0;i<len1;i++){
            ia=m_nvReliableAnchorsH.get(i);
            len2=ia.m_intArray.size();
            left=ia.m_intArray.get(0);
            for(j=1;j<len2;j++){
                right=ia.m_intArray.get(j);
                for(k=left;k<=right;k++){
                    pbPR[k]=true;
                }
                left=right;
            }
        }

        m_cvPRSegsH=new ArrayList();
        b0=pbPR[0];
        left=-1;
        if(b0) left=0;
        for(i=0;i<len;i++){
            b=pbPR[i];
            if(b0){
                if(!b||i==len-1){
                    right=i;
                    m_cvPRSegsH.add(new IntPair(left,right));
                }
            }else{
                if(b) left=i;
            }
            b0=b;
        }
    }
    void parallelOrReliableSegmentsOnly(){
        int len, i, j,left, right;
        double[] pdL=m_pdLoLine, pdH=m_pdHiLine;
        int lent=m_pdLoLine.length;
        pdL=new double[lent];
        pdH=new double[lent];
        IntPair ip;
        for(i=0;i<lent;i++){
            pdL[i]=m_pdLoLine[i];
            pdH[i]=m_pdHiLine[i];
            m_pdLoLine[i]=0;
            m_pdHiLine[i]=0;
        }

        len=m_cvPRSegsL.size();
        for(i=0;i<len;i++){
            ip=m_cvPRSegsL.get(i);
            left=ip.left;
            right=ip.right;
            for(j=left;j<=right;j++){
                m_pdLoLine[j]=pdL[j];
            }
        }

        len=m_cvPRSegsH.size();
        for(i=0;i<len;i++){
            ip=m_cvPRSegsH.get(i);
            left=ip.left;
            right=ip.right;
            for(j=left;j<=right;j++){
                m_pdHiLine[j]=pdH[j];
            }
        }
    }
    void ReliableSegmentsOnly(){
        int len=m_cvParallelEnvLineSegs.size(), i, j,k,left, right,len1,len2;
        IntArray ia;
        double yl,yr,y;
        double[] pdL=m_pdLoLine, pdH=m_pdHiLine;
        int lent=m_pdLoLine.length;
        pdL=new double[lent];
        pdH=new double[lent];
        IntPair ip;
        for(i=0;i<lent;i++){
            pdL[i]=m_pdLoLine[i];
            pdH[i]=m_pdHiLine[i];
            m_pdLoLine[i]=0;
            m_pdHiLine[i]=0;
        }
        len=m_nvReliableAnchorsL.size();
        for(i=0;i<len;i++){
            ia=m_nvReliableAnchorsL.get(i);
            len1=ia.m_intArray.size();
            left=ia.m_intArray.get(0);
            for(j=1;j<len1;j++){
                right=ia.m_intArray.get(j);
                yl=m_pdLine[left];
                yr=m_pdLine[right];
                for(k=left;k<=right;k++){
                    y=CommonMethods.interpolation(left, yl, right,yr, k);
                    m_pdLoLine[k]=y;
                }
                left=right;
            }
        }
        len=m_nvReliableAnchorsH.size();
        for(i=0;i<len;i++){
            ia=m_nvReliableAnchorsH.get(i);
            len1=ia.m_intArray.size();
            left=ia.m_intArray.get(0);
            for(j=1;j<len1;j++){
                right=ia.m_intArray.get(j);
                yl=m_pdLine[left];
                yr=m_pdLine[right];
                for(k=left;k<=right;k++){
                    y=CommonMethods.interpolation(left, yl, right,yr, k);
                    m_pdHiLine[k]=y;
                }
                left=right;
            }
        }
    }
    void setInternalYScale(){
        m_dInternalYScale=m_nWs/(2*Math.max(m_cDevLoX.max,m_cDevHiN.max));
    }
    void calDevMeanSem(){
        int i,j,k,index;
        MeanSem1 ms;
        m_cDevLoN=new MeanSem1();
        m_cDevHiN=new MeanSem1();
        m_cDevLoX=new MeanSem1();
        m_cDevHiX=new MeanSem1();
        m_cDevLo=new MeanSem1();
        m_cDevHi=new MeanSem1();
        IntArray ia;
        ArrayList<Integer> nvLn=new ArrayList(),nvLx=new ArrayList();;
        double[] pdExtrema;
        double[] pdExtremaN;
        double[] pdExtremaX;
        double yl,yr,y;

        int len=m_nvReliableAnchorsL.size(),len1,len2,left,right;
        for(i=0;i<len;i++){
            ia=m_nvReliableAnchorsL.get(i);
            len1=ia.m_intArray.size();
            left=ia.m_intArray.get(0);
            for(j=1;j<len1;j++){
                right=ia.m_intArray.get(j);
                yl=m_pdLine[left];
                yr=m_pdLine[right];
                CommonMethods.LocalExtrema(m_pdRWA, left, right, nvLn, nvLx);
                len2=nvLn.size();
                pdExtrema=new double[len2];
                for(k=0;k<len2;k++){
                    index=nvLn.get(k);
                    y=CommonMethods.interpolation(left, yl, right, yr,index );
                    pdExtrema[k]=m_pdRWA[index]-y;
                    if(pdExtrema[k]>80){
                        k=k;
                    }
                }
                ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len2-1, 1);
                m_cDevLo.mergeSems(ms);

                CommonMethods.LocalExtrema(m_pdLine, left, right, nvLn, nvLx);
                len2=nvLn.size();
                pdExtrema=new double[len2];
                for(k=0;k<len2;k++){
                    index=nvLn.get(k);
                    y=CommonMethods.interpolation(left, yl, right, yr,index );
                    pdExtrema[k]=m_pdLine[index]-y;
                    if(pdExtrema[k]>80){
                        k=k;
                    }
                }
                ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len2-1, 1);
                m_cDevLoN.mergeSems(ms);

                len2=nvLx.size();
                pdExtrema=new double[len2];
                for(k=0;k<len2;k++){
                    index=nvLx.get(k);
                    y=CommonMethods.interpolation(left, yl, right, yr,index );
                    pdExtrema[k]=m_pdLine[index]-y;
                    if(pdExtrema[k]>80){
                        k=k;
                    }
                }
                ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len2-1, 1);
                m_cDevLoX.mergeSems(ms);
                left=right;
            }
        }

        len=m_nvReliableAnchorsH.size();
        for(i=0;i<len;i++){
            ia=m_nvReliableAnchorsH.get(i);
            len1=ia.m_intArray.size();
            left=ia.m_intArray.get(0);
            for(j=1;j<len1;j++){
                right=ia.m_intArray.get(j);
                yl=m_pdLine[left];
                yr=m_pdLine[right];
                CommonMethods.LocalExtrema(m_pdRWA, left, right, nvLn, nvLx);
                len2=nvLx.size();
                pdExtrema=new double[len2];
                for(k=0;k<len2;k++){
                    index=nvLx.get(k);
                    y=CommonMethods.interpolation(left, yl, right, yr,index );
                    pdExtrema[k]=y-m_pdRWA[index];
                }
                ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len2-1, 1);
                m_cDevHi.mergeSems(ms);

                CommonMethods.LocalExtrema(m_pdLine, left, right, nvLn, nvLx);
                len2=nvLx.size();
                pdExtrema=new double[len2];
                for(k=0;k<len2;k++){
                    index=nvLx.get(k);
                    y=CommonMethods.interpolation(left, yl, right, yr,index );
                    pdExtrema[k]=y-m_pdLine[index];
                }
                ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len2-1, 1);
                m_cDevHiX.mergeSems(ms);

                len2=nvLn.size();
                pdExtrema=new double[len2];
                for(k=0;k<len2;k++){
                    index=nvLn.get(k);
                    y=CommonMethods.interpolation(left, yl, right, yr,index );
                    pdExtrema[k]=y-m_pdLine[index];
                    if(pdExtrema[k]<0.01){
                        j=j;
                    }
                }
                ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len2-1, 1);
                m_cDevHiN.mergeSems(ms);
                left=right;
            }
        }

        len=m_cvParallelEnvLineSegs.size();
        IntPair ip;
        for(i=0;i<len;i++){
            ip=m_cvParallelEnvLineSegs.get(i);
            left=ip.left;
            right=ip.right;
            CommonMethods.LocalExtrema(m_pdLine, left, right, nvLn, nvLx);

            len1=nvLn.size();
            pdExtrema=new double[len1];

            for(j=0;j<len1;j++){
                index=nvLn.get(j);
                y=m_pdLoLine[index];
                pdExtrema[j]=m_pdLine[index]-y;
            }
            ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len1-1, 1);
            m_cDevLoN.mergeSems(ms);

            for(j=0;j<len1;j++){
                index=nvLn.get(j);
                y=m_pdHiLine[index];
                pdExtrema[j]=y-m_pdLine[index];
                if(pdExtrema[j]<0.01){
                    j=j;
                }
            }
            ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len1-1, 1);
            m_cDevHiN.mergeSems(ms);

            len1=nvLx.size();
            pdExtrema=new double[len1];

            for(j=0;j<len1;j++){
                index=nvLx.get(j);
                y=m_pdLoLine[index];
                pdExtrema[j]=m_pdLine[index]-y;
            }
            ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len1-1, 1);
            m_cDevLoX.mergeSems(ms);

            for(j=0;j<len1;j++){
                index=nvLx.get(j);
                y=m_pdHiLine[index];
                pdExtrema[j]=y-m_pdLine[index];
            }
            ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len1-1, 1);
            m_cDevHiX.mergeSems(ms);


            CommonMethods.LocalExtrema(m_pdRWA, left, right, nvLn, nvLx);
            len1=nvLn.size();
            pdExtrema=new double[len1];

            for(j=0;j<len1;j++){
                index=nvLn.get(j);
                y=m_pdLoLine[index];
                pdExtrema[j]=m_pdRWA[index]-y;
            }
            ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len1-1, 1);
            m_cDevLo.mergeSems(ms);

            len1=nvLx.size();
            pdExtrema=new double[len1];

            for(j=0;j<len1;j++){
                index=nvLx.get(j);
                y=m_pdHiLine[index];
                pdExtrema[j]=y-m_pdRWA[index];
            }
            ms=CommonStatisticsMethods.buildMeanSem1(pdExtrema, 0, len1-1, 1);
            m_cDevHi.mergeSems(ms);
        }
    }
    public static void pickReliableAnchors(double[] pdLine,ArrayList<Integer>nvEnvLine,int nI, int nF, ArrayList<IntArray> nvaRelaibleAnchors,boolean bHi){
        double dSign=1;
        if(bHi)dSign=-1;
        nvaRelaibleAnchors.clear();
        double dk=0,dk0=0;
        int len=nvEnvLine.size();
        int i=1,j,i0,i1;
        while(i<len-3){
            dk=calSlopeDiff(pdLine, nvEnvLine.get(i-1),nvEnvLine.get(i),nvEnvLine.get(i+1));
            i0=i;
            while(dk*dSign>0&&i<len-2){
                i++;
                dk=calSlopeDiff(pdLine, nvEnvLine.get(i-1),nvEnvLine.get(i),nvEnvLine.get(i+1));
            }
            if(dk*dSign>0) 
                i1=i;
            else
                i1=i-1;
            if(i1-i0>1){
                IntArray ia=new IntArray();
                for(j=i0;j<=i1;j++){
                    ia.m_intArray.add(nvEnvLine.get(j));
                }
                nvaRelaibleAnchors.add(ia);
            }
            i++;
        }
    }
    public static double calSlopeDiff(double[] pdLine, int i0, int i1, int i2){
        return (pdLine[i2]-pdLine[i1])/(i2-i1)-(pdLine[i1]-pdLine[i0])/(i1-i0);
    }
    public static double getSlope(double[] pdLine, int iI, int iF){
        return (pdLine[iF]-pdLine[iI])/(iF-iI);
    }
    public static void buildLine(double pdData[],ArrayList<Integer> nvIndexes,double[] pdLine,int nI, int nF){
        int i,index,left,right;
        double yl,yr,y;
        left=nvIndexes.get(nI);
        for(i=nI+1;i<=nF;i++){
            yl=pdData[left];
            right=nvIndexes.get(i);
            yr=pdData[right];
            for(index=left;index<right;index++){
                y=CommonMethods.interpolation(left, yl, right, yr, index);
                pdLine[index]=y;
            }
            left=right;
        }
    }
    public static void buildLine(double pdData[],ArrayList<Integer> nvIndexes,double[] pdLine){
        int len=pdData.length,i,index,left,right;
        double yl,yr,y;
        int nSize=nvIndexes.size();
        left=nvIndexes.get(0);
        right=nvIndexes.get(1);
        yl=pdData[left];
        yr=pdData[right];
        for(i=0;i<left;i++){
            y=CommonMethods.interpolation(left, yl, right, yr, i);
            pdLine[i]=y;
        }
        left=nvIndexes.get(0);
        for(i=1;i<nSize;i++){
            yl=pdData[left];
            right=nvIndexes.get(i);
            yr=pdData[right];
            for(index=left;index<right;index++){
                y=CommonMethods.interpolation(left, yl, right, yr, index);
                pdLine[index]=y;
            }
            left=right;
        }
        int len1=nvIndexes.size();
        left=nvIndexes.get(len1-2);
        right=nvIndexes.get(len1-1);
        yl=pdData[left];
        yr=pdData[right];
        for(i=right;i<len;i++){
            y=CommonMethods.interpolation(left, yl, right, yr, i);
            pdLine[i]=y;
        }
    }
    public static void buildEnvelopingLine_Wheel(double[] pdLine,int nWs, int nI, int nF, int nRanking, ArrayList<Integer> nvEnvLine,boolean bHi, double dInternalYScale){
        //Not working as expected. Need to fix it. 11/8/2010
        boolean br=!bHi;
        if(nF<nI) br=bHi;
        RunningWindowRankingKeeper RankingKeeper=new RunningWindowRankingKeeper(nRanking+1,br);
        RunningWindowRankingKeeper RankingKeeperOut=new RunningWindowRankingKeeper(nRanking+1,br);
        boolean bOutOfYRange=false,bBackIntoYRange=false;
        nvEnvLine.clear();
        int nDelta=1;
        if(nF<nI) nDelta=-1;
        int iI=nI,iF=iI+nDelta*nWs-1,len=pdLine.length,nLn,i,left,right,nAnchor=-1;
        double yl,yr,dSlope,dSlope0,dSlope1;
        left=nI;

        double dSign=1;
        if(!bHi) dSign=-1;
        if(nDelta<0) dSign*=-1;
        int right1;
        int nReentering=-1;

        while(nDelta*(left+2*nDelta)<nF*nDelta){
            yl=pdLine[left];
            right=left;

            right+=nDelta;
            dSlope0=getWheelCenterY(pdLine,left,right,bHi,nWs,dInternalYScale);

            right+=nDelta;
            dSlope=getWheelCenterY(pdLine,left,right,bHi,nWs,dInternalYScale);;
            right1=right;

            nAnchor=-1;
            nReentering=-1;

            while(right1+nDelta<len&&right1+nDelta>=0){
                right1+=nDelta;
                yr=pdLine[right1];
                if(getDist2(left,pdLine[left],right1,pdLine[right1],nWs,dInternalYScale)>=1) {
                        if(!bOutOfYRange) bOutOfYRange=true;
                        if(bBackIntoYRange) bBackIntoYRange=false;
                    continue;
                }
                dSlope1=getWheelCenterY(pdLine,left,right1,bHi,nWs,dInternalYScale);
                if(right==198151){
                    right=right;
                }

                if(bOutOfYRange&&!bBackIntoYRange) {
//                            RankingKeeper.merge(RankingKeeperOut);
//                    RankingKeeperOut.reset();
                    bBackIntoYRange=true;
                    nReentering=right1;
                    dSlope0=dSlope1;
                }

                if(right1-nReentering==1){
                    dSlope0=dSlope1;
                    right=right1;
                    continue;
                }else if(right1-nReentering==2){
                    dSlope=dSlope1;
                    right=right1;
                    continue;
                }

                if(nAnchor<0){

                }
                if((dSlope-dSlope0)*dSign>0&&(dSlope-dSlope1)*dSign>0){
                    RankingKeeper.updateRankings(dSlope, right);
                    nAnchor=RankingKeeper.getIndex(nRanking);
                }
                if(nDelta*(right1-left)+1>=nWs||(right1+nDelta)*nDelta>nDelta*nF) {
                    break;
                }
                if(dSlope!=dSlope1) {
                    dSlope0=dSlope;
                }
                dSlope=dSlope1;
                right=right1;
            }
            if(nAnchor>=0){
                nvEnvLine.add(nAnchor);
                left=nAnchor;
                RankingKeeper.reset();
                RankingKeeperOut.reset();
                bOutOfYRange=false;
                bBackIntoYRange=false;
            }else{
                break;
            }
        }
    }
    public static void buildEnvelopingLine(double[] pdLine,int nWs, int nI, int nF, int nRanking, ArrayList<Integer> nvEnvLine,boolean bHi, double dInternalYScale){
        boolean br=!bHi;
        if(nF<nI) br=bHi;
        RunningWindowRankingKeeper RankingKeeper=new RunningWindowRankingKeeper(nRanking+1,br);
        RunningWindowRankingKeeper RankingKeeperOut=new RunningWindowRankingKeeper(nRanking+1,br);
        boolean bOutOfYRange=false,bBackIntoYRange=false;
        nvEnvLine.clear();
        int nDelta=1;
        if(nF<nI) nDelta=-1;
        int iI=nI,iF=iI+nDelta*nWs-1,len=pdLine.length,nLn,i,left,right,nAnchor=-1;
        double yl,yr,dx,dy,dSlope,dSlope0,dSlope1;
        double dist2=0,dMaxDist2=nWs*nWs,dYScale2=dInternalYScale*dInternalYScale;
        left=nI;

        double dSign=1;
        if(!bHi) dSign=-1;
        if(nDelta<0) dSign*=-1;
        int right1;

        while(nDelta*(left+2*nDelta)<nF*nDelta){
            dist2=0;
            yl=pdLine[left];
            right=left;

//            right+=nDelta;
//            dSlope0=getSlope(pdLine,left,right);//11n21
            if(dSign>0)
                dSlope0=Double.NEGATIVE_INFINITY;
            else
                dSlope0=Double.POSITIVE_INFINITY;

            right+=nDelta;
            dSlope=getSlope(pdLine,left,right);;
            right1=right;

            nAnchor=-1;

            while(right1+nDelta<len&&right1+nDelta>=0){
                right1+=nDelta;
                yr=pdLine[right1];
                dx=right1-left;
                dy=yr-yl;
                dSlope1=getSlope(pdLine,left,right1);
                if(nAnchor<0) dist2=0;//not using dYScale
                if((dSlope-dSlope0)*dSign>0&&(dSlope-dSlope1)*dSign>0){
                    if(nAnchor<0) dist2=0;//not using dYScale for a few first slope local extrema
                    if(dist2>dMaxDist2) {
                        RankingKeeperOut.updateRankings(dSlope, right);
                        if(!bOutOfYRange) bOutOfYRange=true;
                        if(bBackIntoYRange) bBackIntoYRange=false;
                    }else{
                        if(bOutOfYRange&&!bBackIntoYRange) {
                            RankingKeeper.merge(RankingKeeperOut);
                            RankingKeeperOut.reset();
                            bBackIntoYRange=true;
                        }
                        RankingKeeper.updateRankings(dSlope, right);
                        nAnchor=RankingKeeper.getIndex(nRanking);
                    }
                }
                if(nDelta*(right1-left)+1>=nWs||(right1+nDelta)*nDelta>nDelta*nF) {
                    break;
                }
                if(dSlope!=dSlope1) {
                    dSlope0=dSlope;
                }
                dSlope=dSlope1;
                right=right1;
            }
            if(nAnchor>=0){
                nvEnvLine.add(nAnchor);
                left=nAnchor;
                RankingKeeper.reset();
                RankingKeeperOut.reset();
                bOutOfYRange=false;
                bBackIntoYRange=false;
            }else{
                break;
            }
        }
    }
    public static void buildEnvelopingLine0(double[] pdLine,int nWs, int nI, int nF, int nRanking, ArrayList<Integer> nvEnvLine,boolean bHi, double dInternalYScale){
        //this is before the modifications on 11/21/2011
        boolean br=!bHi;
        if(nF<nI) br=bHi;
        RunningWindowRankingKeeper RankingKeeper=new RunningWindowRankingKeeper(nRanking+1,br);
        RunningWindowRankingKeeper RankingKeeperOut=new RunningWindowRankingKeeper(nRanking+1,br);
        boolean bOutOfYRange=false,bBackIntoYRange=false;
        nvEnvLine.clear();
        int nDelta=1;
        if(nF<nI) nDelta=-1;
        int iI=nI,iF=iI+nDelta*nWs-1,len=pdLine.length,nLn,i,left,right,nAnchor=-1;
        double yl,yr,dx,dy,dSlope,dSlope0,dSlope1;
        double dist2=0,dMaxDist2=nWs*nWs,dYScale2=dInternalYScale*dInternalYScale;
        left=nI;

        double dSign=1;
        if(!bHi) dSign=-1;
        if(nDelta<0) dSign*=-1;
        int right1;

        while(nDelta*(left+2*nDelta)<nF*nDelta){
            dist2=0;
            yl=pdLine[left];
            right=left;

            right+=nDelta;
            dSlope0=getSlope(pdLine,left,right);

            right+=nDelta;
            dSlope=getSlope(pdLine,left,right);;
            right1=right;

            nAnchor=-1;

            while(right1+nDelta<len&&right1+nDelta>=0){
                right1+=nDelta;
                yr=pdLine[right1];
                dx=right1-left;
                dy=yr-yl;
                dSlope1=getSlope(pdLine,left,right1);
                if(right==112240){
                    right=right;
                }
                dist2=dx*dx+dy*dy*dYScale2;
                if(nAnchor<0) dist2=0;//not using dYScale
                if((dSlope-dSlope0)*dSign>0&&(dSlope-dSlope1)*dSign>0){
                    if(nAnchor<0) dist2=0;//not using dYScale for a few first slope local extrema
                    if(dist2>dMaxDist2) {
                        RankingKeeperOut.updateRankings(dSlope, right);
                        if(!bOutOfYRange) bOutOfYRange=true;
                        if(bBackIntoYRange) bBackIntoYRange=false;
                    }else{
                        if(bOutOfYRange&&!bBackIntoYRange) {
                            RankingKeeper.merge(RankingKeeperOut);
                            RankingKeeperOut.reset();
                            bBackIntoYRange=true;
                        }
                        RankingKeeper.updateRankings(dSlope, right);
                        nAnchor=RankingKeeper.getIndex(nRanking);
                    }
                }
                if(nDelta*(right1-left)+1>=nWs||(right1+nDelta)*nDelta>nDelta*nF) {
                    break;
                }
                if(dSlope!=dSlope1) {
                    dSlope0=dSlope;
                }
                dSlope=dSlope1;
                right=right1;
            }
            if(nAnchor>=0){
                nvEnvLine.add(nAnchor);
                left=nAnchor;
                RankingKeeper.reset();
                RankingKeeperOut.reset();
                bOutOfYRange=false;
                bBackIntoYRange=false;
            }else{
                break;
            }
        }
    }
    public static void getSlope(double pdLine[], int iI, int iF, double pdSlope[]){
        int nDelta=1;
        if(iF<iI) nDelta=-1;
        int len=iF-iI;
        for(int i=0; i<len;i++){
            pdSlope[i]=(pdLine[iI+nDelta*(i+1)]-pdLine[iI])/(nDelta*(i+1));
        }
    }
    
    int pickParallelEnvLineSegs(){
        int lenL=m_nvLoLine.size(),lenH=m_nvHiLine.size(),len;
        IntPair ipL=new IntPair(0,0),ipH=new IntPair(0,0);
        m_cvParallelEnvLineSegs=new ArrayList();
        int indexL=2;
        int indexH=CommonMethods.getNumOfSmallerOrEqualElements(m_nvHiLine,m_nvLoLine.get(indexL))-1;
        if(indexH<2){
            indexH=2;
            indexL=CommonMethods.getNumOfSmallerOrEqualElements(m_nvLoLine,m_nvHiLine.get(indexH))-1;
        }

        IntPair indexPair=pickNextParallelEnvLineSeg(indexL,indexH,ipL,ipH);
        indexL=indexPair.left;
        indexH=indexPair.right;
        while(indexL<lenL-2&&indexH<lenH-2){
            int left=Math.max(ipL.left, ipH.left);
            double diff,dMean,dMin=Double.POSITIVE_INFINITY, dMax=Double.NEGATIVE_INFINITY;
            int right =left;
            while(right<m_pdLine.length){
                diff=m_pdHiLine[right]-m_pdLoLine[right];
                if(diff<dMin) dMin=diff;
                if(diff>dMax) dMax=diff;
                dMean=0.5*(dMin+dMax);
                if((dMax-dMin)/dMean>m_dParallelCutoff) break;
                right++;
            }
            indexL=CommonMethods.getNumOfSmallerOrEqualElements(m_nvLoLine, right)-1;
            indexH=CommonMethods.getNumOfSmallerOrEqualElements(m_nvHiLine, right)-1;
            right=Math.min(m_nvLoLine.get(indexL), m_nvHiLine.get(indexH));
            len=right-left+1;
            if(len>=m_nWs){
                IntPair ip=new IntPair(left,right);
                m_cvParallelEnvLineSegs.add(ip);
            }
            indexPair=pickNextParallelEnvLineSeg(indexL,indexH,ipL,ipH);
            indexL=indexPair.left;
            indexH=indexPair.right;
        }
        return 1;
    }

    IntPair pickNextParallelEnvLineSeg(int indexL, int indexH, IntPair ipL, IntPair ipH){
        int lenL=m_nvLoLine.size(),lenH=m_nvHiLine.size();
        int lL,rL,lH,rH;
        if(indexL<1) indexL=1;
        IntPair indexPair=new IntPair(lenL,lenH);
        while (indexL<lenL-2&&indexH<lenH-2){
            lL=m_nvLoLine.get(indexL);
            rL=m_nvLoLine.get(indexL+1);
            lH=m_nvHiLine.get(indexH);
            rH=m_nvHiLine.get(indexH+1);
            if(parallelEnvLines(lL,rL,lH,rH)){
                ipL.left=lL;
                ipL.right=rL;
                ipH.left=lH;
                ipH.right=rH;
                indexPair.left=indexL;
                indexPair.right=indexH;
                break;
            }
            if(rL>rH){
                indexH++;
            }else if(rH>rL){
                indexL++;
            }else{
                indexL++;
                indexH++;
            }
        }
        return indexPair;
    }
    boolean parallelEnvLines(int lL, int rL, int lH, int rH){
        double ylL=m_pdLine[lL],yrL=m_pdLine[rL],ylH=m_pdLine[lH],yrH=m_pdLine[rH];
        int left=Math.max(lL, lH),right=Math.min(rL, rH);
        int mid=(left+right)/2;
        double yL=CommonMethods.interpolation(lL, ylL, rL, yrL, mid);
        double yH=CommonMethods.interpolation(lH, ylH, rH, yrH, mid);
        double dist=yH-yL;
        double dSlopeDiff=(yrH-ylH)/(rH-lH)-(yrL-ylL)/(rL-lL);
        boolean parallel=(Math.abs(dSlopeDiff*m_nWs)<Math.abs(m_dParallelCutoff*dist));
        return parallel;
    }

    void translocateReliableSegs(){
        translocateReliableSegs(true);
        translocateReliableSegs(false);
    }
    void translocateReliableSegs(boolean bHi){
        ArrayList<IntArray> nvReliableAnchors;
        if(bHi)
            nvReliableAnchors=m_nvReliableAnchorsH;
        else
            nvReliableAnchors=m_nvReliableAnchorsL;

        int len=nvReliableAnchors.size(),len1,len2,i,j,anchor,anchor0;

        ArrayList<IntArray> translocatableAnchors=new ArrayList();
        boolean b,b0;

        IntArray iaPR,ia;
        ArrayList<Integer> nvAnchors;
        for(i=0;i<len;i++){
            iaPR=nvReliableAnchors.get(i);
            len1=iaPR.m_intArray.size();
            anchor=iaPR.m_intArray.get(0);
            nvAnchors=new ArrayList();
            b0=!withinPRSegment(anchor,!bHi);
            if(b0) nvAnchors.add(anchor);
            for(j=1;j<len1;j++){
                anchor=iaPR.m_intArray.get(j);
                if(anchor==15012){
                    i=i;
                }
                b=!withinPRSegment(anchor,!bHi);
                if(b0){
                    if(!b){
                        ia=new IntArray();
                        ia.m_intArray=nvAnchors;
                        translocatableAnchors.add(ia);
                        nvAnchors=new ArrayList();
                    }else{
                        nvAnchors.add(anchor);
                    }
                }else{
                    if(b) nvAnchors.add(anchor);
                }
                b0=b;
            }
            if(b0){
                ia=new IntArray();
                ia.m_intArray=nvAnchors;
                translocatableAnchors.add(ia);
            }
        }
        if(bHi)
            m_nvTranslocatableAnchorsH=translocatableAnchors;
        else
            m_nvTranslocatableAnchorsL=translocatableAnchors;

        len=translocatableAnchors.size();
        for(i=0;i<len;i++){
            translocateSegments(translocatableAnchors.get(i).m_intArray,bHi);
        }
    }

    void translocateSegments(ArrayList<Integer> ia, boolean bHi){
        double[] pdEnvLine;
        int len,i,left,right;
        len=ia.size();
        double dSign,diff,dMax;
        if(bHi){
            dSign=-1;
            pdEnvLine=m_pdHiLine;
        }else{
            dSign=1;
            pdEnvLine=m_pdLoLine;
        }

        left=ia.get(0);
        right=ia.get(len-1);
        dMax=Double.NEGATIVE_INFINITY;
        for(i=left;i<right;i++){
            diff=dSign*(m_pdLine[i]-pdEnvLine[i]);
            if(diff>dMax){
                dMax=diff;
            }
        }
        len=right-left+1;
        double pdSeg[]=new double[len];
        for(i=left;i<=right;i++){
            pdSeg[i-left]=pdEnvLine[i]+dSign*dMax;
        }
        patchEnvSegment(pdSeg,left,right,bHi);
    }
    boolean withinPRSegment(int position, boolean bHi){
        ArrayList<IntPair> cvPRSegs;
        if(bHi)
            cvPRSegs=m_cvPRSegsH;
        else
            cvPRSegs=m_cvPRSegsL;
        int len=cvPRSegs.size(),i;
        IntPair ip;
        for(i=0;i<len;i++){
            ip=cvPRSegs.get(i);
            if(ip.left<=position&&ip.right>=position) return true;
        }
        return false;
    }
    void patchEnvSegment(double[] pdSeg, int left0, int right0, boolean bHi){
        ArrayList<Integer> nvEnvLineC,nvEnvLineT=new ArrayList();
        double[] pdEnvLine,pdEnvLineC;//pdSeg is part of pdEnvLine and will be replacing part of pdEnvLineC
        double dSign;
        if(bHi){
            pdEnvLine=m_pdHiLine;
            nvEnvLineC=m_nvLoLine;
            pdEnvLineC=m_pdLoLine;
            dSign=-1;
        }
        else{
            pdEnvLine=m_pdLoLine;
            nvEnvLineC=m_nvHiLine;
            pdEnvLineC=m_pdHiLine;
            dSign=1;
        }

        int i,left,right,right2;
        int idl=CommonMethods.getNumOfSmallerOrEqualElements(nvEnvLineC, left0)-1;
        int idr=CommonMethods.getNumOfSmallerOrEqualElements(nvEnvLineC, right0);
        if(idl<0) idl=0;
        if(idr>=nvEnvLineC.size()) idr=nvEnvLineC.size()-1;
        int iI=nvEnvLineC.get(idl),iF=nvEnvLineC.get(idr);
        double dSlope1,dSlope2;

        RunningWindowRankingKeeper RankingKeeper=new RunningWindowRankingKeeper(m_nRanking+1,bHi);
        RunningWindowRankingKeeper RankingKeeperOut=new RunningWindowRankingKeeper(m_nRanking+1,bHi);
        nvEnvLineT.clear();
        double yl,yr,dx,dy,dSlope;

        left=iI;
        int nAnchor;
        double dist2=0,dMaxDist2=m_nWs*m_nWs,dYScale2=m_dInternalYScale*m_dInternalYScale;
        boolean bOutOfYRange=false;
        boolean bBackIntoYRange=false;

        while(left-1<iF){
            yl=m_pdLine[left]-pdEnvLine[left];
            right2=left;
            right0=left;

            right2++;
            right=right2;
            yr=m_pdLine[right2]-pdEnvLine[right2];
            dx=right2-left;
            dy=yr-yl;
            dSlope1=dy/dx;

            right2++;
            yr=m_pdLine[right2]-pdEnvLine[right2];
            dx=right2-left;
            dy=yr-yl;
            dSlope=dy/dx;
            
            nAnchor=-1;

            while(true){
                right2++;
                yr=m_pdLine[right2]-pdEnvLine[right2];
                dx=right2-left;
                dy=yr-yl;
                dSlope2=dy/dx;
                dist2=dx*dx+dy*dy*dYScale2;

                if(dSign*(dSlope-dSlope1)>0&&dSign*(dSlope-dSlope2)>0){
                    if(dist2>dMaxDist2){
                        if(!bOutOfYRange)bOutOfYRange=true;
                        if(bBackIntoYRange)bBackIntoYRange=false;
                        RankingKeeperOut.updateRankings(dSlope, right);
                    }else{
                        if(bOutOfYRange&&!bBackIntoYRange){
                            RankingKeeper.merge(RankingKeeperOut);
                            bBackIntoYRange=true;
                            RankingKeeperOut.reset();
                        }
                        RankingKeeper.updateRankings(dSlope, right);
                        nAnchor=RankingKeeper.getIndex(m_nRanking);
                    }
                }

                if(right-left>=m_nWs||right2-1>=iF) {
                    break;
                }
                if(dSlope1!=dSlope2){
                    dSlope1=dSlope;
                }
                dSlope=dSlope2;
                right=right2;
            }
            if(nAnchor>=0){
                if(RankingKeeper.containsIndex(iF)){
                    nAnchor=iF;//in order to make a smooth patching
                }
                nvEnvLineT.add(nAnchor);
                left=nAnchor;
                RankingKeeper.reset();
                RankingKeeperOut.reset();
                bOutOfYRange=false;
                bBackIntoYRange=false;
            }else{
                break;
            }
        }

        double dShift,slope;
        nvEnvLineT.add(iF);
        left=iI;
        int len1=nvEnvLineT.size(),position;
        for(i=0;i<len1;i++){
            dShift=m_pdLine[left]-pdEnvLine[left];
            right=nvEnvLineT.get(i);
            slope=((m_pdLine[right]-pdEnvLine[right])-dShift)/(right-left);
            for(position=left+1;position<=right;position++){
                pdEnvLineC[position]=pdEnvLine[position]+dShift+slope*(position-left);
            }
            left=right;
        }
    }

    int getFirstRPAnchor(boolean bHi){
        ArrayList<IntPair> cvPRSegs;
        if(bHi)
            cvPRSegs=m_cvPRSegsH;
        else
            cvPRSegs=m_cvPRSegsH;
        if(cvPRSegs.size()>0) return cvPRSegs.get(0).left;
        return -1;
    }
    public static double getWheelCenterY(double[] pdLine, int left, int right, boolean bHi, double xScale, double yScale){
        double[] center=new double[2];
        getWheelCenter(pdLine,left,right,bHi,center,xScale,yScale);
        return center[1];
    }
    public static void getWheelCenter(double[] pdLine, int left, int right, boolean bHi, double[] center,double xScale, double yScale){
        getWheelCenter(left,pdLine[left],right,pdLine[right],bHi,center,xScale,yScale);
    }
    public static void getWheelCenter(double xl, double yl, double xr, double yr, boolean bHi, double[] center,double xScale, double yScale){
        double dSign,xScale2=xScale*xScale,yScale2=yScale*yScale;
        if(bHi)
            dSign=1;
        else
            dSign=-1;
        double xm,ym,r2;

        xl/=xScale;
        xr/=xScale;
        yl/=yScale;
        yr/=yScale;

        r2=1;
        xm=0.5*(xl+xr);
        ym=0.5*(yl+yr);
        double k2=-(xr-xl)/(yr-yl);
        double dx=xr-xl,dy=yr-yl;
        double d12=(xm-xl)*(xm-xl)+(ym-yl)*(ym-yl);
        double d22=r2-d12;

        double yo=dSign*Math.sqrt(d22*dx*dx/(dx*dx+dy*dy))+ym;
        double xo;
        xo=xm+(yo-ym)/k2;
        center[0]=xo*xScale;
        center[1]=yo*yScale;
    }
    public static double getDist2(double xl, double yl, double xr, double yr, double xScale, double yScale){
        double dx=(xr-xl)/xScale,dy=(yr-yl)/yScale;
        return dx*dx+dy*dy;
    }
}
