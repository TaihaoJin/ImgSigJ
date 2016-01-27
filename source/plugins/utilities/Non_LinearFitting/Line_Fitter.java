/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import utilities.CommonMethods;
import java.util.ArrayList;
import utilities.statistics.MeanSem0;
import utilities.CommonStatisticsMethods;
import utilities.Non_LinearFitting.Fitting_Function;
import utilities.Non_LinearFitting.StandardFittingFunction;

/**
 *
 * @author Taihao
 */
public class Line_Fitter {
    double[] m_pdData;
    float[] m_pfData;
    int m_nI, m_nF, m_nDelta, m_nWs, m_nNumScannings, m_nDataReductionFactor;
    int m_nMaxDevLen;//maximum number of cosecutive data points located on one side of the fitted line.
    boolean m_bExpandableModel;
    public Line_Fitter(){
        m_bExpandableModel=true;
    }
    public void updata(int nI, int nF, int nDelta, int nWs, int nNumScannings, int nDataReductionFactor, boolean bExpandableModel){
        m_nI=nI;
        m_nF=nF;
        m_nWs=nWs;
        m_nNumScannings=nNumScannings;
        m_nDataReductionFactor=nDataReductionFactor;
        m_bExpandableModel=bExpandableModel;
        m_nDelta=nDelta;
    }
    public void clearData(){
        m_pfData=null;
        m_pdData=null;
        Runtime.getRuntime().gc();
    }
    public void setData(float[] pfData){
        clearData();
        m_pfData=pfData;
    }
    public void setData(double[] pdData){
        clearData();
        m_pdData=pdData;
    }
    public void setMaxDevLen(int nMaxDevLen){
        m_nMaxDevLen=nMaxDevLen;
    }
    public static ArrayList<double[]> fitLine(int nI, int nF, int nDelta, int nWs, int nNumScannings, Non_Linear_Fitter fitter,
            Fitting_Function func, double[][] pdX, double pdData0[], double[] pdPars, ArrayList<double[]> pdvFittedPars, int nMaxDevLen){
        ArrayList<double[]> pdvResults=new ArrayList();
        int len=(nF-nI)/nDelta+1;

        ArrayList<double[]> pdvLines=new ArrayList();
        ArrayList<double[]> pdvData=new ArrayList();
        pdvFittedPars.clear();
        int numScannings=4;
        int[] pnLags=new int[numScannings];
        double pdLinec[]=new double[len];

        int len1=2*nWs+1,i,j;
        if(len1>len) len1=len;
        int lag=len1/numScannings;
        double[] pdLags=new double[numScannings];
        for(i=0;i<numScannings;i++){
            pnLags[i]=i*lag;
            pdLags[i]=i*lag;
            pdvLines.add(new double[len]);
        }

        int iI=nI, iF=nI+2*nWs;
//        int index1=iI,index2=0;

        int seg,offset1,offset2;
        int nSegments=len/len1;
        if(len%len1>0) nSegments++;
        double[] pdFittedPars, pdLine;
        int nDataSize=0;
        int index;
        int nVars=pdX[0].length;
        
        double[] pdOrigin=new double[nVars];
        double[] pdXPrime=new double[nVars];

        int[] pnFixedParIndexes=new int[1];
        pnFixedParIndexes[0]=0;
        
        for(seg=0;seg<nSegments;seg++){
            offset1=iI+seg*len1*nDelta;
            for(i=0;i<numScannings;i++){
                offset2=offset1+pnLags[i]*nDelta;
                iF=offset2+(len1-1)*nDelta;
                if(iF>nF) iF=nF;
                fitter.update(pdX, pdData0, pdPars, func, fitter.Least_Square, fitter.Simplex,offset2,iF,nDelta,pnFixedParIndexes);

                CommonStatisticsMethods.copyArray(pdX[offset2], pdOrigin);
                fitter.setOrigin(pdOrigin);

                fitter.fitData_Apache();
                pdFittedPars=fitter.getFittedPars();
                pdvFittedPars.add(pdFittedPars);
                pdLine=pdvLines.get(i);
                for(index=offset2;index<=iF;index+=nDelta){
                    j=(index-offset2)*nDelta;
                    CommonStatisticsMethods.copyArray(pdX[offset2+j*nDelta],pdXPrime);
                    CommonStatisticsMethods.subtractArray(pdXPrime, pdOrigin);
                    pdLine[nDataSize+j]=func.fun(pdFittedPars, pdXPrime);
                }
            }
            nDataSize+=len1;
        }

        double[] pdData=new double[len];
        for(i=0;i<len;i++){
            pdData[i]=pdData0[nI+nDelta*i];
        }
        
        double[] pdRWA=CommonStatisticsMethods.getRunningWindowAverage(pdData0, nI, nF,nDelta,20, false);

        linkSegments(pdvLines,pdData,pdRWA,pnLags,nSegments,len1);
        pdLine=pdvLines.get(0);
        for(i=0;i<len;i++){
            pdLinec[i]=pdLine[i];
        }

        pdLine=pdvLines.get(0);
        for(i=1;i<numScannings;i++){
            optimizePath(pdLinec,pdvLines.get(i),pnLags[0],pnLags[i],pdData,0,len-1);
        }

        ArrayList<Integer> crossPositions=getCrossoverPositions(pdLinec,pdData,0,len-1,0);
        crossPositions.add(len-1);
        int segs=crossPositions.size();
        int left=0,right=0;
        for(seg=0;seg<segs;seg++){
            right=crossPositions.get(seg);
            if(right-left>nMaxDevLen){
                int nWs1=nWs/2;
                int left1=Math.max(0, left-nWs1);
                int right1=Math.min(len-1, right);
                int len11=right1-left1+1;
                if(2*nWs1+1>len11) nWs1=(len11+1)/2;
                ArrayList<double[]> pdvResults1=fitLine(nI+left1*nDelta,nI+right1*nDelta,nDelta,nWs1,nNumScannings,fitter,func,pdX,pdData0,pdPars,pdvFittedPars,nMaxDevLen);
                double[] pdLinec1=pdvResults1.get(0);
                weldingLine(pdLinec,pdLinec1,left1,nWs1);
            }
            left=right;
        }

        pdvResults.add(pdLinec);
        for(i=0;i<numScannings;i++){
            pdvResults.add(pdvLines.get(i));
        }
        pdvResults.add(pdLags);
        return pdvResults;
    }
    public static ArrayList<Integer> getCrossoverPositions(double[] pdData1, double[] pdData2, int indexI, int indexF, int lag){
        ArrayList<Integer> positions=new ArrayList();
        double diff0,diff;
        int i;
        diff0=pdData1[indexI]-pdData2[indexI-lag];
        int ieh0=1, ieh1=0;
        for(i=indexI+1;i<=indexF;i++){
            diff=pdData1[i]-pdData2[i-lag];
            if(diff==0){
                if(ieh0>ieh1){//first position when two lines equal
                    ieh0=i;
                }
                ieh1=i;
            }else{
                if(ieh0<=ieh1){//first non-equal position
                    positions.add((ieh0+ieh1)/2);
                    ieh0=ieh1+1;
                }else{
                    if(diff*diff0<0){
                        positions.add(i);
                    }
                }
                diff0=diff;
            }
        }
        return positions;
    }
    public static int getClosestPosition(double[] pdData1, double[] pdData2, int indexI, int indexF, int lag){
        int position=indexI,i;
        double small=Double.MAX_VALUE,diff;
        for(i=indexI;i<=indexF;i++){
            diff=Math.abs(pdData1[i]-pdData2[i-lag]);
            if(diff<small) {
                position=i;
                small=diff;
            }
        }
        return position;
    }
    public static double getSSE(double[] pdData1, double[] pdData2, int indexI, int indexF, int lag){
        double sse=0,dt;
        for(int i=indexI;i<=indexF;i++){
            dt=pdData1[i]-pdData2[i-lag];
            sse+=dt*dt;
        }
        return sse;
    }

    public static int[] getSegmentEnds(int position0, int lag, int len){
        int ends[]=new int[2];
        int position=position0-lag;
        int seg=position/len;
        ends[0]=len*seg+lag;
        ends[1]=ends[0]+len-1;
        return ends;
    }//checked until this line
    public static void linkSegments(ArrayList<double[]> pdvLines, double[] pdData, double[] pdRWA, int[] pnLags, int nSegs, int nSegLen){
        int seg,i,j,cp1,cp2,cp10,cp20,crossPositions[],crossPositions2[],nLines=pdvLines.size(),line,lag1,lag2,offset,len=nSegLen;
        double sse10=0,sse20=0,sse1=0,sse2=0;
        int left,right,lag,lenT=pdData.length;
        double[] pdLine1, pdLine2;
        int line2,cp3,cp4,iI1,iI2,iF1,iF2;
        double[] pdBridge=new double[pdData.length];
        int segmentEnds[]=new int[2];
        for(line=0;line<nLines;line++){
            pdLine1=pdvLines.get(line);
            lag1=pnLags[line];
            for(seg=0;seg<nSegs-1;seg++){
                offset=seg*len;
                cp1=offset+len-1;
                cp2=offset+len;
                iI1=offset;
                iF1=offset+2*len-1;
                iI1=Math.max(iI1, -lag1);
                iF1=Math.min(iF1, lenT-1-lag1);//making sure pdRWA or pdData will not go out of index ranges
//                crossPositions=getNeighboringCPs(pdLine1,pdRWA, iI1,cp1,cp2,iF1,-lag1);
                crossPositions=null;//not using the direct line connections.
                if(crossPositions!=null) {
                    cp1=crossPositions[0];
                    cp2=crossPositions[1];
                    bridgingWithLine(pdBridge,cp1,pdRWA[cp1+lag1],cp2,pdRWA[cp2+lag1]);
                    sse10=getSSE(pdBridge,pdData,cp1,cp2,-lag1);
                    left=cp1;
                    right=cp2;
                }
                for(line2=0;line2<nLines;line2++){
                    if(line2==line) continue;
                    pdLine2=pdvLines.get(line2);
                    lag2=pnLags[line2];
                    lag=lag2-lag1;
                    cp3=offset+len-1;
                    cp4=offset+len;
                    iI2=iI1;
                    iF2=iF1;
                    segmentEnds=getSegmentEnds(cp3,lag,len);
                    if(iI2<segmentEnds[0]) iI2=segmentEnds[0];
                    if(iF2>segmentEnds[1]) iF2=segmentEnds[1];
                    if(cp3>=iF2||cp4<=iI2) continue;
                    crossPositions2=getNeighboringCPs(pdLine1,pdLine2,iI2,cp3,cp4,iF2,lag);
                    if(crossPositions2==null) continue;
                    cp3=crossPositions2[0];
                    cp4=crossPositions2[1];
//                    if(cp3-lag+lag2<0) continue;//the index for pdData would be negative otherwise.
                    sse20=getSSE(pdLine2,pdData,cp3-lag,cp4-lag,-lag2);
                    if(crossPositions==null){
                        cp1=cp3;
                        cp2=cp4;
                        left=cp1;
                        right=cp2;
                        for(j=cp3;j<=cp4;j++){
                            pdBridge[j]=pdLine2[j-lag];
                        }
                        sse10=sse20;
                        continue;
                    }else{
                        left=Math.min(cp1, cp3);
                        right=Math.max(cp2, cp4);
                        sse1=sse10;
                        sse1+=getSSE(pdLine1,pdData,left,cp1-1,-lag1);
                        sse1+=getSSE(pdLine1,pdData,cp2+1,right,-lag1);
                        sse2=sse20;
                        sse2+=getSSE(pdLine2,pdData,left-lag,cp3-1-lag,-lag2);
                        sse2+=getSSE(pdLine2,pdData,cp4+1-lag,right-lag,-lag2);

                        if(sse1>sse2){
                            for(j=cp3;j<=cp4;j++){
                                pdBridge[j]=pdLine2[j-lag];
                            }
                            cp1=cp3;
                            cp2=cp4;
                            sse10=sse20;
                        }
                    }
                }
               for(j=cp1;j<=cp2;j++){
                   pdLine1[j]=pdBridge[j];
               }
            }
        }
    }

    public static void bridgingWithLine(double pdBridge[],int cp1, double y1, int cp2, double y2){
        double k;
        k=(y2-y1)/(cp2-cp1);
        for(int i=cp1;i<=cp2;i++){
            pdBridge[i]=y1+k*(i-cp1);
        }
    }

    public static int[] getNeighboringCPs(double[] pdData1, double[] pdData2, int positionI,int position0,int position1, int positionF,int lag){
        int[] crossPoints=new int[2];
        double diff0=pdData1[position0]-pdData2[position0-lag],diff;
        int i,I0,I1,cp1,cp2,it;
        I0=positionI;
        if(I0<lag) I0=lag;
        cp1=-1;
        for(i=position0-1;i>=I0;i--){
            diff=pdData1[i]-pdData2[i-lag];
            if(diff*diff0<=0){
                cp1=i;
                break;
            }
            diff0=diff;
        }
        if(cp1<0) return null;
        crossPoints[0]=cp1;

        I1=positionF;
        int len2=pdData2.length;
        if((I1-lag)>=len2) I1=len2+lag-1;

        cp2=-1;
        diff0=pdData1[position1]-pdData2[position1-lag];
        for(i=position1+1;i<=I1;i++){
            diff=pdData1[i]-pdData2[i-lag];
            if(diff0*diff<=0){
                cp2=i;
                break;
            }
        }
        if(cp2<0) return null;
        crossPoints[1]=cp2;
        return crossPoints;
    }
    public static void optimizePath(double[] pdLine1,double[] pdLine2, int lag1, int lag2,double[] pdData, int left0, int right0){
        double pdMidLine[]=new double[right0-left0+1];
        int lag=lag2-lag1;
        left0=Math.max(left0, lag);
        int len=pdLine2.length;
        right0=Math.min(right0, len+lag-1);
        getMidLine(pdLine1,pdLine2,pdMidLine,left0,right0,lag);
        ArrayList <Integer> crossPositions=getCrossoverPositions(pdLine1,pdLine2,left0,right0,lag);
        int segs=crossPositions.size(),left=left0,right=left0;
        double sse1,sse2;
        int best,i;
        for(int seg=0;seg<segs;seg++){
            best=0;
            left=right;
            right=crossPositions.get(seg);
            sse1=getSSE(pdLine1,pdData,left,right,-lag1);
            sse2=getSSE(pdMidLine,pdData,left,right,-lag1);
            if(sse2<sse1){
                sse1=sse2;
                best=1;
            }
            sse2=getSSE(pdData,pdLine2,left-lag1,right-lag1,lag2);
            if(sse2<sse1) best=2;
            switch(best){
                case 1:
                    for(i=left;i<=right;i++){
                        pdLine1[i]=pdMidLine[i];
                    }
                    break;
                case 2:
                    for(i=left;i<=right;i++){
                        pdLine1[i]=pdLine2[i-lag];
                    }
                    break;
            }
        }
    }
    public static void getMidLine (double[] pdLine1,double[] pdLine2,double[] pdMidLine, int left0, int right0,int lag){
        left0=Math.max(left0, lag);
        int len=pdLine2.length;
        right0=Math.min(right0, len+lag-1);
        for(int i=left0;i<=right0;i++){
            pdMidLine[i]=0.5*(pdLine1[i]+pdLine2[i-lag]);
        }
    }
    public static void weldingLine(double[] pdLine1, double[] pdLine2, int lag, int overlap){
        int left=lag,right=0,i,len=pdLine2.length;
        double diff=Math.abs(pdLine1[lag]-pdLine2[0]),nx=Double.MAX_VALUE;
        for(i=0;i<overlap;i++){
            diff=Math.abs(pdLine1[lag+i]-pdLine2[i]);
            if(diff<nx){
                left=i;
                nx=diff;
            }
        }
        nx=Double.MAX_VALUE;
        for(i=len-overlap-1;i<len;i++){
            diff=Math.abs(pdLine1[lag+i]-pdLine2[i]);
            if(diff<nx){
                right=i;
                nx=diff;
            }
        }
        for(i=left;i<=right;i++){
            pdLine1[i+lag]=pdLine2[i];
        }
    }
}
