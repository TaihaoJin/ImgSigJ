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
import utilities.statistics.MeanSem1;
import utilities.Non_LinearFitting.LineEnveloper;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class Line_Fitter_Coninuous {
    public static double[] fitLine(int nI, int nF, int nDelta, int nWs, int nMinWs, Non_Linear_Fitter fitter,
            Fitting_Function func, double[][] pdX, double pdData0[], double[] pdPars, ArrayList<double[]> pdvFittedPars,
            int nMaxDevLen, ArrayList<Integer> nvSegmentingPoints, double dSlopeFactor){
        int nRWAWs=20;
        double[] pdRWA=CommonStatisticsMethods.getRunningWindowAverage(pdData0, nI, nF, nDelta, nRWAWs, false);
        MeanSem1 ms;
        ms=CommonStatisticsMethods.getDeltaMeanSem(pdRWA, 0, pdRWA.length-1, 1, 1, nMaxDevLen, false);

        double dMaxSlope=(Math.max(Math.abs(ms.max), Math.abs(ms.min)))/nMaxDevLen;

        pdvFittedPars.clear();
        nvSegmentingPoints.clear();


        int len=(nF-nI)/nDelta+1;
        int len1=2*nWs+1,i,j;
        if(len1>len) len1=len;

        double[] pdFittedPars, pdLine;
        int nVars=pdX[0].length;
        int pnFixedParIndexes[]=new int[1];
        pnFixedParIndexes[0]=0;
     //   pnFixedParIndexes=null;

        double[] pdOrigin=new double[nVars];
        ArrayList<Integer> crossPositions;

        int left=nI,right;
        int nFactor=1;
        nvSegmentingPoints.add(nI);

        pdLine=new double[len];
        pdLine[0]=pdRWA[0];
        ArrayList<double[]> segmentFittedPars=new ArrayList();
        int nLongest=0;
        int segLen=0,lMax=0;
        double dFactor=1,dReduction=3./4.;
        ArrayList<Integer> nvDevLengs=new ArrayList();
        ArrayList<Integer> nvScaledDevLengs=new ArrayList();
        int index,lent;
        double dSlope=1;
        int right0=0;
        double[] pdLinet=new double[len1];
        int firstCross,segs;
        double cur1,cur2;
        int iF;

        while(nF-left>nMaxDevLen){
            CommonStatisticsMethods.copyArray(pdX[left], pdOrigin);
            double y0=pdRWA[left];
            pdPars[0]=y0;

            right=Math.min(nF, left+(int)(len1*dFactor));
            if(left==13016){
                left=left;
            }
            lent=(right-left)/nDelta+1;
            fitter.update(pdX, pdData0, pdPars, func, fitter.getMinimizationOption(), fitter.getMinimizationMethod(), left, right, nDelta, pnFixedParIndexes);
            fitter.setOrigin(pdX[left]);
            fitter.fitData_Apache();
            pdFittedPars=fitter.getFittedPars();
            crossPositions=getCrosspoints(pdData0,nI+left*nDelta,nI+right*nDelta,nDelta,func,pdFittedPars,pdX,nI+nDelta*left,nDelta,dMaxSlope,dSlopeFactor,nvDevLengs,nvScaledDevLengs);
            crossPositions.add(0,left);
//            right=getFirstLongDevPoint(crossPositions,nMaxDevLen);
            index=CommonStatisticsMethods.getLargerElementIndex(nvScaledDevLengs, nMaxDevLen);
            if(index<0) index=nvScaledDevLengs.size()-1;
            right=crossPositions.get(index);//crossPositions has an extra element
            right=getClosestCrosspoint(pdRWA,right,lent,-1,func,pdFittedPars,pdX,nI+right*nDelta,-nDelta);
            segLen=right-left+1;
            if(segLen>lMax){
                nLongest=segmentFittedPars.size();
                lMax=segLen;
                segmentFittedPars.add(CommonStatisticsMethods.copyArray(pdFittedPars));
            }
            dSlope=(pdData0[right]-pdData0[left])/(right-left+1);
            if(lMax<lent*dReduction&&lent*dReduction>(nMinWs/(1+(dSlope*dSlopeFactor)/dMaxSlope))){
  //              IJ.error("unacceptable deviation of the fitting from the starting point, half window size is used");
                dFactor*=dReduction;
                continue;
            }

            dFactor=1;
            right=left+lMax-1;
            nvSegmentingPoints.add(right);
            pdvFittedPars.add(segmentFittedPars.get(nLongest));
            pdFittedPars=segmentFittedPars.get(nLongest);

            if(left==13016){
                left=left;
            }

            segs=nvSegmentingPoints.size();
            if(segs>0){//making smoother connection
                firstCross=getClosestCrosspoint(pdRWA,left+1,lent,1,func,pdFittedPars,pdX,nI+left*nDelta,nDelta);
                firstCross=getClosestCrosspoint(pdLine,left+1,(Math.min(firstCross,right0)-left)/nDelta,1,func,pdFittedPars,pdX,nI+left*nDelta+1,nDelta);
                if(firstCross>0){
                    cur1=func.fun(pdFittedPars, pdX[nI+(firstCross+1)*nDelta])-func.fun(pdFittedPars, pdX[nI+firstCross*nDelta]);
                    cur1-=func.fun(pdFittedPars, pdX[nI+(left+1)*nDelta])-func.fun(pdFittedPars, pdX[nI+left*nDelta]);
                    cur2=(pdLine[firstCross]-pdLine[firstCross-1]);
                    cur2-=(pdLine[left]-pdLine[left-1]);
                    if(Math.abs(cur1)>Math.abs((cur2))){
                        lent-=(firstCross-left);
                        left=firstCross;
                    }
                }
            }


            right0=left+len1;
            if(right0>=len) right0=len-1;
            if(right0>=pdLine.length||right0*nDelta+nI>=pdX.length){
                len=len;
            }
            for(i=left+1;i<=right0;i++){
                pdLine[i]=func.fun(pdFittedPars, pdX[nI+i*nDelta]);
            }
            left=right;
            segmentFittedPars.clear();
            nLongest=0;
            lMax=0;
        }
        return pdLine;
    }
    public static double[] fitLine_Enveloping(int nWs, Non_Linear_Fitter fitter,
            Fitting_Function func, double[][] pdX, double pdData0[], double[] pdPars, ArrayList<double[]> pdvFittedPars,
            ArrayList<Integer> nvSegmentingPoints,
            double[] pdLoLine, double[] pdHiLine, int nRanking, ArrayList<Integer> nvHiLine, ArrayList nvLoLine){
        int nRWAWs=20;
        int len=pdData0.length;
        double[] pdRWA=CommonStatisticsMethods.getRunningWindowAverage(pdData0, 0,len-1, 1, nRWAWs, false);
//        MeanSem1 ms;
//        ms=CommonStatisticsMethods.getDeltaMeanSem(pdRWA, 0, pdRWA.length-1, 1, 1, nMaxDevLen, false);

//        double dMaxSlope=(Math.max(Math.abs(ms.max), Math.abs(ms.min)))/nMaxDevLen;

        pdvFittedPars.clear();
        nvSegmentingPoints.clear();

        int len1=2*nWs+1,i,j;

        LineEnveloper cEnveloper=new LineEnveloper(pdData0, nWs, nRanking, pdLoLine, pdHiLine);
        cEnveloper.getAnchors(nvHiLine,true);
        cEnveloper.getAnchors(nvLoLine,false);

        if(len1>len) len1=len;

        double[] pdFittedPars, pdLine;
        int nVars=pdX[0].length;
        int pnFixedParIndexes[]=new int[1];
        pnFixedParIndexes[0]=0;
     //   pnFixedParIndexes=null;

        double[] pdOrigin=new double[nVars];
        ArrayList<Integer> crossPositions;

        int left=0,right;
        nvSegmentingPoints.add(0);

        pdLine=new double[len];
        pdLine[0]=pdRWA[0];
        ArrayList<double[]> segmentFittedPars=new ArrayList();
        int nLongest=0;
        int segLen=0,lMax=0;
        double dFactor=1,dReduction=3./4.;
        ArrayList<Integer> nvDevLengs=new ArrayList();
        ArrayList<Integer> nvScaledDevLengs=new ArrayList();
        int index,lent;
        double dSlope=1;
        int right0=0;
        int firstCross,segs;
        double cur1,cur2;
        int iF,nI=0,nF=len-1,nDelta=1;
        int lCrossing=0,hCrossing=0;
        boolean bRWABased=true;

        left=nF+1;
//        while(nF-left>nMaxDevLen){
        while(nF>left){
            CommonStatisticsMethods.copyArray(pdX[left], pdOrigin);
            double y0=pdRWA[left];
            pdPars[0]=y0;

            right=Math.min(nF, left+(int)(len1*dFactor));
            if(left==22084){
                left=left;
            }
            lent=(right-left)/nDelta+1;
            fitter.update(pdX, pdData0, pdPars, func, fitter.getMinimizationOption(), fitter.getMinimizationMethod(), left, right, nDelta, pnFixedParIndexes);
            fitter.setOrigin(pdX[left]);
            fitter.fitData_Apache();
            pdFittedPars=fitter.getFittedPars();
/*            crossPositions=getCrosspoints(pdData0,nI+left*nDelta,nI+right*nDelta,nDelta,func,pdFittedPars,pdX,nI+nDelta*left,nDelta,dMaxSlope,dSlopeFactor,nvDevLengs,nvScaledDevLengs);
            crossPositions.add(0,left);
//            right=getFirstLongDevPoint(crossPositions,nMaxDevLen);
            index=CommonStatisticsMethods.getLargerElementIndex(nvScaledDevLengs, nMaxDevLen);
            if(index<0) index=nvScaledDevLengs.size()-1;
            right=crossPositions.get(index);//crossPositions has an extra element
            right=getClosestCrosspoint(pdRWA,right,lent,-1,func,pdFittedPars,pdX,nI+right*nDelta,-nDelta);
 */
            lCrossing=getClosestCrosspoint(pdLoLine,left,lent,1,func,pdFittedPars,pdX,left,1);
            if(lCrossing==-1) lCrossing=left+lent-1;
            hCrossing=getClosestCrosspoint(pdHiLine,left,lent,1,func,pdFittedPars,pdX,left,1);
            if(hCrossing==-1) hCrossing=left+lent-1;
            right=Math.min(lCrossing, hCrossing);
//            right=getFirstExcessiveDeviation(pdRWA,left,lent,1,func,pdFittedPars,pdX,left,1,dMaxPeakToPeak);
            if(right==-1) right=left+lent-1;

            if(bRWABased)
                right=getClosestCrosspoint(pdRWA,right,lent,-1,func,pdFittedPars,pdX,right,-1);
            else
                right=getClosestCrosspoint(pdLine,right,lent,-1,func,pdFittedPars,pdX,right,-1);
            
            segLen=right-left+1;
            if(segLen>lMax){
                nLongest=segmentFittedPars.size();
                lMax=segLen;
                segmentFittedPars.add(CommonStatisticsMethods.copyArray(pdFittedPars));
            }
            if(lMax<lent*dReduction&&right>=left){//right could be smaller than right when bRWABased==false
  //              IJ.error("unacceptable deviation of the fitting from the starting point, half window size is used");
                dFactor*=dReduction;
                continue;
            }

            dFactor=1;
            if(lMax>1){//an effective segmentation
                right=left+lMax-1;
                nvSegmentingPoints.add(right);
                pdvFittedPars.add(segmentFittedPars.get(nLongest));
                pdFittedPars=segmentFittedPars.get(nLongest);

                if(left==13016){
                    left=left;
                }

                segs=nvSegmentingPoints.size();
                if(segs>0){//making smoother connection
                    firstCross=getClosestCrosspoint(pdRWA,left+1,lent,1,func,pdFittedPars,pdX,nI+left*nDelta,nDelta);
                    firstCross=getClosestCrosspoint(pdLine,left+1,(Math.min(firstCross,right0)-left)/nDelta,1,func,pdFittedPars,pdX,nI+left*nDelta+1,nDelta);
                    if(firstCross>0){
                        cur1=func.fun(pdFittedPars, pdX[nI+(firstCross+1)*nDelta])-func.fun(pdFittedPars, pdX[nI+firstCross*nDelta]);
                        cur1-=func.fun(pdFittedPars, pdX[nI+(left+1)*nDelta])-func.fun(pdFittedPars, pdX[nI+left*nDelta]);
                        cur2=(pdLine[firstCross]-pdLine[firstCross-1]);
                        cur2-=(pdLine[left]-pdLine[left-1]);
                        if(Math.abs(cur1)>Math.abs((cur2))){
                            lent-=(firstCross-left);
                            left=firstCross;
                        }
                    }
                }
            }


            right0=left+len1;
            if(right0>=len) right0=len-1;
            if(right0>=pdLine.length||right0*nDelta+nI>=pdX.length){
                len=len;
            }
            for(i=left+1;i<=right0;i++){
                pdLine[i]=func.fun(pdFittedPars, pdX[nI+i*nDelta]);
            }
            if(right>left){
                left=right;
                bRWABased=true;
            }else{//
                if(bRWABased)
                    bRWABased=false;
                else//would be cycling infinitely.
                    break;
            }
            segmentFittedPars.clear();
            nLongest=0;
            lMax=0;
        }
        return pdLine;
    }
    public static int getFirstLongDevPoint(ArrayList<Integer> pts,int nMaxLen){
        int position0=pts.get(0),i,len=pts.size(),position=0;
        for(i=1;i<len;i++){
            position=pts.get(i);
            if(position-position0>nMaxLen) break;
            position0=position;
        }
        return position0;
    }
    public static ArrayList<Integer> getCrosspoints(double pdData[], int nI, int nF, int nDelta1, Fitting_Function func, double[] pdPars, double[][] pdX, int nIx, int nDelta2){
        ArrayList<Integer> positions=new ArrayList();
        double diff0=pdData[nI]-func.fun(pdPars,pdX[nIx]),diff;
        int i,index,len=pdX.length;
        int ieh0=1, ieh1=0;

        for(i=nI+nDelta1;i<=nF;i+=nDelta1){
            index=nIx+((i-nI)/nDelta1)*nDelta2;
            if(index>=len) break;
            diff=pdData[i]-func.fun(pdPars,pdX[index]);
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
    public static ArrayList<Integer> getCrosspoints(double pdData[], int nI, int nF, int nDelta1, Fitting_Function func, double[] pdPars,
            double[][] pdX, int nIx, int nDelta2, double dMaxSlope, double dSlopeFactor, ArrayList<Integer> nvDevLengs, ArrayList<Integer> nvScaledDevLengs){
        ArrayList<Integer> positions=new ArrayList();
        nvScaledDevLengs.clear();
        nvDevLengs.clear();
        int i,index,len=pdX.length;
        int ieh0=1, ieh1=0;
        int lastCross=nI;
        double dv=0,dv0=func.fun(pdPars,pdX[nIx]);
        double diff0=pdData[nI]-dv0,diff,dSlope;
        double slope;
        int nDevLen;

        for(i=nI+nDelta1;i<=nF;i+=nDelta1){
            index=nIx+((i-nI)/nDelta1)*nDelta2;
            if(index>=len) break;
            dv=func.fun(pdPars,pdX[index]);
            diff=pdData[i]-dv;
            if(diff==0){
                if(ieh0>ieh1){//first position when two lines equal
                    ieh0=i;
                }
                ieh1=i;
            }else{
                if(ieh0<=ieh1){//first non-equal position
                    positions.add((ieh0+ieh1)/2);
                    nDevLen=ieh0-lastCross;
                    nvDevLengs.add(nDevLen);
                    slope=(dv-dv0)/(i-lastCross);
                    nvScaledDevLengs.add((int)(nDevLen*(1.+dSlopeFactor*Math.abs(slope)/dMaxSlope)));
                    lastCross=ieh1;
                    ieh0=ieh1+1;
                    dv0=dv;
                }else{
                    if(diff*diff0<0){
                        positions.add(i);
                        nDevLen=i-lastCross;
                        nvDevLengs.add(nDevLen);
                        slope=(dv-dv0)/(i-lastCross);
                        nvScaledDevLengs.add((int)(nDevLen*(1.+dSlopeFactor*Math.abs(slope)/dMaxSlope)));
                        lastCross=i;
                        dv0=dv;
                    }
                }
            }
            diff0=diff;
        }
        return positions;
    }
    public static int getClosestCrosspoint(double pdData[], int nI, int len, int nDelta1, Fitting_Function func, double[] pdPars, double[][] pdX, int nIx, int nDelta2){
        double diff0=pdData[nI]-func.fun(pdPars,pdX[nIx]),diff;
        int len1=pdData.length,len2=pdX.length;
        int i,index1=0,index2;
        for(i=0;i<len;i++){
            index1=nI+i*nDelta1;
            if(index1<0||index1>=len1) return -1;
            index2=nIx+i*nDelta2;
            if(index2<0||index2>=len2) return -1;
            diff=pdData[index1]-func.fun(pdPars,pdX[index2]);
            if(diff*diff0<=0) {
                return index1;
            }
        }
        return -1;
    }
    public static int getFirstExcessiveDeviation(double pdData[], int nI, int len, int nDelta1, Fitting_Function func, double[] pdPars, double[][] pdX, int nIx, int nDelta2,double dMaxDev){
        double dev;
        dMaxDev=Math.abs(dMaxDev);
        int len1=pdData.length,len2=pdX.length;
        int i,index1=0,index2;
        for(i=0;i<len;i++){
            index1=nI+i*nDelta1;
            if(index1<0||index1>=len1) return -1;
            index2=nIx+i*nDelta2;
            if(index2<0||index2>=len2) return -1;
            dev=pdData[index1]-func.fun(pdPars,pdX[index2]);
            if(Math.abs(dev)>dMaxDev) {
                return index1;
            }
        }
        return -1;
    }
}
