/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CommonStatisticsMethods;
import java.util.ArrayList;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.MeanSem1;
import utilities.statistics.MeanSem0;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.MeanSem1;
import utilities.QuickSort;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import utilities.statistics.LineSegmentRegressionEvaluater;
import utilities.CommonMethods;
import utilities.CustomDataTypes.IntPair;
import utilities.CommonMethods;
import utilities.QuickSortInteger;
import utilities.CustomDataTypes.DoubleRange;
import utilities.io.PrintAssist;
import utilities.CustomDataTypes.IntpairCouple;
import utilities.statistics.PolynomialLineFitter;

/**
 *
 * @author Taihao
 */
public class PiecewisePolynomialLineFitter_ProgressiveSegmenting  extends PolynomialLineFitter{
    double[] m_pdX,m_pdY,m_pdDev;
    double[] m_pdPWDevSig,m_pdTiltingSig,m_pdSidenessSig;
    int[] m_pnLxPositions,m_pnLnPositions;
    ArrayList<Integer> m_nvLx, m_nvLn,m_nvSegmentHeads;
    ArrayList<PolynomialLineFittingSegmentNode> m_cvFittedSegments;
    ArrayList<PolynomialLineFittingSegmentNode> m_cvFittedSegments_Stored;
    ArrayList<PolynomialLineFittingSegmentNode> m_cvSplittedSegments;
    String m_sSignalID,m_sVerification;
    int nDataSize,nMaxRisingInterval,minSegLen,maxSegLen,lastStart,firstStart;
    double[] m_pdSD;
    boolean[] pbSelected,pbValidDev;
    double m_dPChiSQ,m_dPTilting,m_dPPWDev,m_dOutliarRatio,m_dPSideness;
    int[] m_pnRisingIntervals;
    int m_nWsSD;
    int m_nWsTilting, m_nWsPWDev, nOrder;
    int standardSegLength;
    intRange fittingRange;
    ArrayList<intRange> cvBreakingRanges;
    PolynomialLineFittingSegmentNode[] m_pcOptimalSegments;
    ArrayList<LineFittingSegmentGroup> m_cvSegmentGroups;
    ArrayList<intRange> m_cvSmoothRanges;
    ArrayList<Integer> m_nvUnmergedBreaks;
    ArrayList<String[]> m_svMergingSegStringArray;
    ArrayList<String> m_svBreakStatus;
    ArrayList<double[]> m_pdvLevelEstimationLines;
    ArrayList<Double> m_dvJumpSignificance1,m_dvJumpSignificance2,m_dvSignal,m_dvDelta,m_dvProjectionSig,m_dvProjectionSigMW,m_dvDifferenceSigTTest,m_dvDifferenceSigMW,m_dvDifferenceSigChiSQ,m_dvSD;
    int[] m_pnBreakIndexes;
    public PiecewisePolynomialLineFitter_ProgressiveSegmenting(String sSignalID, double[] pdX, double[] pdY, double[] pdSD, ArrayList<intRange> irs, boolean[] pbSelected, int nMaxRisingInterval, int nOrder, int maxSegLen,int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness,double dPPWDev,double dOutliarRatio){
        this(sSignalID,pdX,pdY,pdSD,irs,pbSelected,nMaxRisingInterval,nOrder,maxSegLen, nWsSD,nWsTilting, nWsPWDev, dPChiSQ, dPTilting, dPSideness, dPPWDev, dOutliarRatio, true);
        cvBreakingRanges=irs;
        this.m_dOutliarRatio=dOutliarRatio;
        standardSegLength=20;
        m_nWsSD=nWsSD;
        nDataSize=pdX.length;
        m_pdX=pdX;
        m_pdY=pdY;
        m_pdPWDevSig=new double[nDataSize];
        m_pdTiltingSig=new double[nDataSize];
        m_pdSidenessSig=new double[nDataSize];
        pbValidDev=new boolean[nDataSize];
        this.nOrder=nOrder;
        m_nWsTilting=nWsTilting;
        m_nWsPWDev=nWsPWDev;
        m_dPChiSQ=dPChiSQ;
        m_dPTilting=dPTilting;
        m_dPSideness=dPSideness;
        m_dPPWDev=dPPWDev;
        minSegLen=nOrder+3;
        m_pdDev=new double[nDataSize];
        this.pbSelected=pbSelected;
        calFittingRange();
        calLastStart();
        this.nMaxRisingInterval=nMaxRisingInterval;
        m_pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        this.maxSegLen=maxSegLen;
        this.m_pdSD=pdSD;
//        calSD();
        m_nvLn=new ArrayList();
        m_nvLx=new ArrayList();
        m_sSignalID=sSignalID;
        m_sVerification="NV";
        CommonMethods.LocalExtrema(m_pdY,0,m_pdX.length-1,m_nvLn,m_nvLx);
        markLocalExtremaPositions();
    }
    public PiecewisePolynomialLineFitter_ProgressiveSegmenting(String sSignalID, double[] pdX, double[] pdY, double[] pdSD, ArrayList<intRange> irs, boolean[] pbSelected, int nMaxRisingInterval, int nOrder, int maxSegLen,int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness,double dPPWDev,double dOutliarRatio, boolean defaultCompletion){
        cvBreakingRanges=irs;
        this.m_dOutliarRatio=dOutliarRatio;
        standardSegLength=20;
        m_nWsSD=nWsSD;
        nDataSize=pdX.length;
        m_pdX=pdX;
        m_pdY=pdY;
        m_pdPWDevSig=new double[nDataSize];
        m_pdTiltingSig=new double[nDataSize];
        m_pdSidenessSig=new double[nDataSize];
        pbValidDev=new boolean[nDataSize];
        this.nOrder=nOrder;
        m_nWsTilting=nWsTilting;
        m_nWsPWDev=nWsPWDev;
        m_dPChiSQ=dPChiSQ;
        m_dPTilting=dPTilting;
        m_dPSideness=dPSideness;
        m_dPPWDev=dPPWDev;
        minSegLen=nOrder+3;
        m_pdDev=new double[nDataSize];
        this.pbSelected=pbSelected;
        calFittingRange();
        calLastStart();
        this.nMaxRisingInterval=nMaxRisingInterval;
        m_pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        this.maxSegLen=maxSegLen;
        this.m_pdSD=pdSD;
//        calSD();
        m_nvLn=new ArrayList();
        m_nvLx=new ArrayList();
        m_sSignalID=sSignalID;
        m_sVerification="NV";
        CommonMethods.LocalExtrema(m_pdY,0,m_pdX.length-1,m_nvLn,m_nvLx);
        markLocalExtremaPositions();
        if(defaultCompletion) Complete_Default();
    }
    void Complete_Default(){
        fitLine();
        refineSegments();
        registerSegments();
//        registerSegmentGroups();
        fillBestAproximationSegs();
        
    }
    public void setSelection(boolean[] pbSelected){
        this.pbSelected=pbSelected;
    }
    public void completeSegments(ArrayList<IntpairCouple> cvSegments){
        fitLine(cvSegments);
        registerSegments();
//        registerSegmentGroups();
        fillBestAproximationSegs();
    }
    void removeShortSegments(){
        int i,len=m_cvFittedSegments.size();
        for(i=len-1;i>=0;i--){
            if(m_cvFittedSegments.get(i).isShort()) m_cvFittedSegments.remove(i);
        }
    }
    void replaceFittedSegmentsWithGroups(){
        int i,len=m_cvSegmentGroups.size();
        storeFittedSegments();
        m_cvFittedSegments.clear();
        for(i=0;i<len;i++){
            m_cvFittedSegments.add(m_cvSegmentGroups.get(i));
        }
    }
    void storeFittedSegments(){
        int i,len=m_cvFittedSegments.size();
        if(m_cvFittedSegments_Stored==null) m_cvFittedSegments_Stored=new ArrayList();
        m_cvFittedSegments_Stored.clear();
        for(i=0;i<len;i++){
            m_cvFittedSegments_Stored.add(m_cvFittedSegments.get(i));
        }
    }
    ArrayList<LineFittingSegmentGroup> getFittedSegmentGroups(){
        return m_cvSegmentGroups;
    }
    void calFittingRange(){
        fittingRange=new intRange();
        int i,len=pbSelected.length;
        for(i=0;i<len;i++){
            if(pbSelected[i]) fittingRange.expandRange(i);
        }
    }
    void markLocalExtremaPositions(){//index=m_pnLxPositions[i], index is the index of the local maximum at the immediate
        //right side of i. i is the index of a data point
        //for the point in the right side of the largest local extremum, in give the index eaqual the size of the m_nvLn, so need to check before getting the index
        m_pnLxPositions=new int[m_pdX.length];
        m_pnLnPositions=new int[m_pdX.length];
        CommonStatisticsMethods.setElements(m_pnLxPositions,-1);
        CommonStatisticsMethods.setElements(m_pnLnPositions,-1);
        int i,index,len=m_nvLx.size(),j;
        int index0=0;
        for(i=0;i<len;i++){
            index=m_nvLx.get(i);
            for(j=index0;j<index;j++){
                m_pnLxPositions[j]=i;
            }
            index0=index;
        }

        for(i=index0;i<m_pdX.length;i++){
            m_pnLxPositions[i]=len;
        }

        len=m_nvLn.size();
        index0=0;
        for(i=0;i<len;i++){
            index=m_nvLn.get(i);
            for(j=index0;j<index;j++){
                m_pnLnPositions[j]=i;
            }
            index0=index;
        }
        for(i=index0;i<m_pdX.length;i++){
            m_pnLnPositions[i]=len;
        }
    }
    int calLastStart(){
        lastStart=nDataSize-1;
        int len=0;
        while(len<minSegLen){
            if(pbSelected[lastStart])len++;
            lastStart--;
        }
        firstStart=0;
        while(!pbSelected[firstStart])
            firstStart++;
        return 1;
    }
    public boolean isMerged(){
        if(m_svMergingSegStringArray==null||m_svMergingSegStringArray.isEmpty()) return false;
        return true;
    }
    void fitLine(){
        m_pcOptimalSegments=new PolynomialLineFittingSegmentNode[nDataSize];
        m_nvSegmentHeads=new ArrayList();
        m_cvFittedSegments=new ArrayList();
        m_cvSplittedSegments=new ArrayList();
        if(m_svMergingSegStringArray!=null) m_svMergingSegStringArray.clear();
        if(m_svMergingSegStringArray!=null) m_svMergingSegStringArray.clear();

        m_nvSegmentHeads.add(0);
        PolynomialLineFittingSegmentNode segL,segR,currentSeg;
        ArrayList<Integer> nvInvalidSegIndexes=new ArrayList();
        int i,len=nvInvalidSegIndexes.size(),index;
        int start=0,end;
        intRange ir;
        len=cvBreakingRanges.size();
        for(i=0;i<len;i++){
            ir=cvBreakingRanges.get(i);
            end=ir.getMin();
            fittingRange.setRange(start, end);
            currentSeg=new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,fittingRange.getMin(),fittingRange.getMax(),nOrder,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio);
            m_cvFittedSegments.add(currentSeg);
            start=ir.getMax();
        }
        
        end=lastStart+minSegLen;
        fittingRange.setRange(start, end);
        currentSeg=new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,fittingRange.getMin(),fittingRange.getMax(),nOrder,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio);
        m_cvFittedSegments.add(currentSeg);

        ArrayList<PolynomialLineFittingSegmentNode> cvSplittedSegs=new ArrayList();

        nvInvalidSegIndexes=getInvalidSegs(m_cvFittedSegments);
        len=nvInvalidSegIndexes.size();
        while (len>0){
            for(i=len-1;i>=0;i--){
                index=nvInvalidSegIndexes.get(i);
                currentSeg=m_cvFittedSegments.get(index);
                if(!currentSeg.isValid()) {
                    m_cvFittedSegments.remove(index);
                    continue;
                }
                cvSplittedSegs.clear();
                splitSegment(currentSeg,cvSplittedSegs);
                segL=cvSplittedSegs.get(0);
                segR=cvSplittedSegs.get(1);
                m_cvFittedSegments.remove(index);
                if(segR!=null) m_cvFittedSegments.add(index,segR);
                if(segL!=null) m_cvFittedSegments.add(index,segL);
                m_cvSplittedSegments.add(segL);
                m_cvSplittedSegments.add(segR);
            }
            nvInvalidSegIndexes=getInvalidSegs(m_cvFittedSegments);
            len=nvInvalidSegIndexes.size();
        }
    }
    void fitLine(ArrayList<IntpairCouple> segmentRanges){
        m_pcOptimalSegments=new PolynomialLineFittingSegmentNode[nDataSize];
        m_nvSegmentHeads=new ArrayList();
        m_cvFittedSegments=new ArrayList();
        m_cvSplittedSegments=new ArrayList();
        if(m_svMergingSegStringArray!=null) m_svMergingSegStringArray.clear();
        if(m_svMergingSegStringArray!=null) m_svMergingSegStringArray.clear();
        
        int i,len=segmentRanges.size(),left,right;
        IntpairCouple ipr;
        for(i=0;i<len;i++){
            ipr=segmentRanges.get(i);
            left=ipr.first.left;
            right=ipr.second.right;
            m_cvFittedSegments.add(makeSegment(left,right,nOrder,m_dPPWDev,m_dOutliarRatio));
        }        
   }
    void splitSegment(PolynomialLineFittingSegmentNode cPSeg, ArrayList<PolynomialLineFittingSegmentNode> cvDSegs){
        int i,start=cPSeg.nStart,end=cPSeg.nEnd,lenMin=nOrder+3;
        for(i=start;i<=end;i++){
            m_pdDev[i]=m_pdY[i]-cPSeg.predict(m_pdX[i]);
        }
//        intRange ir=getLargestDevSumRange(m_pdDev,start,end);
        intRange ir=new intRange(start, end);//need to fix later

        double sseMin=Double.POSITIVE_INFINITY,sse,sseL,sseR;
        PolynomialLineFittingSegmentNode segL=null, segR=null,segLn=null,segRn=null;
        PolynomialRegression cPr;
        int l,r,order,len;
        for(i=ir.getMin();i<=ir.getMax();i++){
            if(i>lastStart) continue;
            l=i;
            r=nextIndependentSelectedPosition(l,1);
            if(r<0) {
                continue;
            }
//            r=l+1;
            if(l-start<3||end-r<3) continue;
            order=nOrder;
            if(l-start<lenMin) order=l-start-2;
//            segL=new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,start,l,order,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio);
            segL=makeSegment(start,l,order,-1,-1);
            if(!segL.isValid()) continue;
            order=nOrder;
            if(end-r<lenMin) order=end-r-2;
//            segR=new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,r,end,order,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio);
            segR=makeSegment(r,end,order,-1,-1);
            if(!segR.isValid()) continue;
            cPr=segL.cPr;
            len=cPr.nDataSize;
            sseL=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
            cPr=segR.cPr;
            len=cPr.nDataSize;
            sseR=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
            sse=sseL+sseR;
            if(sse<sseMin){
                sseMin=sse;
                segLn=segL;
                segRn=segR;
            }
        }
        cvDSegs.clear();
        cvDSegs.add(segLn);
        cvDSegs.add(segRn);
    }
    void splitSegment0(PolynomialLineFittingSegmentNode cPSeg, ArrayList<PolynomialLineFittingSegmentNode> cvDSegs){
        int i,start=cPSeg.nStart,end=cPSeg.nEnd,lenMin=nOrder+3;
        for(i=start;i<=end;i++){
            m_pdDev[i]=m_pdY[i]-cPSeg.predict(m_pdX[i]);
        }
//        intRange ir=getLargestDevSumRange(m_pdDev,start,end);
        intRange ir=new intRange(start, end);//need to fix later

        double sseMin=Double.POSITIVE_INFINITY,sse,sseL,sseR;
        PolynomialLineFittingSegmentNode segL=null, segR=null,segLn=null,segRn=null;
        PolynomialRegression cPr;
        int l,r,order,len;
        for(i=ir.getMin();i<=ir.getMax();i++){
            if(i>lastStart) continue;
            l=i;
            r=nextIndependentSelectedPosition(l,1);
            if(r<0) {
                continue;
            }
//            r=l+1;
            if(l-start<3||end-r<3) continue;
            order=nOrder;
            if(l-start<lenMin) order=l-start-2;
            segL=new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,start,l,order,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio);
            if(!segL.isValid()) continue;
            order=nOrder;
            if(end-r<lenMin) order=end-r-2;
            segR=new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,r,end,order,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio);
            if(!segR.isValid()) continue;
            cPr=segL.cPr;
            len=cPr.nDataSize;
            sseL=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
            cPr=segR.cPr;
            len=cPr.nDataSize;
            sseR=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
            sse=sseL+sseR;
            if(sse<sseMin){
                sseMin=sse;
                segLn=segL;
                segRn=segR;
            }
        }
        cvDSegs.clear();
        cvDSegs.add(segLn);
        cvDSegs.add(segRn);
    }
    int nextIndependentSelectedPosition(int index,int delta){
        if(!pbSelected[index])index=getNextSelectedPosition(index,delta);
        if(index>=m_pnRisingIntervals.length&&delta>0)return -1;
        int interval=CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals, index, delta),len=m_pdX.length;
        double x0=m_pdX[index],dx;
        index+=delta;
        if(index<0||index>=len) return -1;
        dx=Math.abs(m_pdX[index]-x0);
        while(dx<interval){
            index+=delta;
            if(index<0||index>=len) return -1;
            dx=Math.abs(m_pdX[index]-x0);
        }
        return index;
    }
    int getNextSelectedPosition(int index,int delta){
        index+=delta;
        if(index<0||index>=pbSelected.length) return -1;
        while(!pbSelected[index]){
            index+=delta;
            if(index<0||index>=pbSelected.length) return -1;
        }
        return index;
    }
    /*
    int nextIndependentSelectedPosition(int index,int delta){
        int index0=m_nvSelectedIndexes.get(index);
        if(index0>=m_pnRisingIntervals.length&&delta>0)return -1;
        int interval=CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals, index0, delta),lenS=m_nvSelectedIndexes.size(),len=m_pdX.length;
        double x0=m_pdX[index0],dx;
        index+=delta;
        if(index<0||index>=lenS) return -1;
        dx=Math.abs(m_pdX[m_nvSelectedIndexes.get(index)]-x0);
        while(dx<interval){
            index+=delta;
            if(index<0||index>=lenS) return -1;
            dx=Math.abs(m_pdX[m_nvSelectedIndexes.get(index)]-x0);
        }
        return index;
    }*/
    public static intRange getLargestDevSumRange(double[] pdDev, int start, int end){
        ArrayList<Integer> cvCrossingPoints=CommonStatisticsMethods.getCrossingPositions(pdDev, 0,start,end,1);
        int len=cvCrossingPoints.size();
        if(len==0) return new intRange(start,end);
        int iI=start,iF=cvCrossingPoints.get(0),i;
        intRange irX=new intRange(iI,iF);
        double sum,maxSum=Math.abs(CommonStatisticsMethods.getSum(pdDev, iI, iF, 1));
        for(i=1;i<len;i++){
            iI=iF+1;
            if(i<len-1)
                iF=cvCrossingPoints.get(i+1);
            else
                iF=end;
            sum=Math.abs(CommonStatisticsMethods.getSum(pdDev, iI, iF, 1));
            if(sum>maxSum) {
                irX=new intRange(iI,iF);
                maxSum=sum;
            }
        }
        return irX;
    }
    ArrayList<Integer> getInvalidSegs(ArrayList<PolynomialLineFittingSegmentNode> segs){
        ArrayList<Integer> indexes=new ArrayList();
        PolynomialLineFittingSegmentNode seg;
        int i,len=segs.size();
        for(i=0;i<len;i++){
            seg=segs.get(i);
            if(seg==null) continue;
            if(!LineSegmentRegressionEvaluater.validDeviation(seg.getRegressionNode(), m_pdX, m_pdY,m_pdTiltingSig,m_pdSidenessSig,m_pdPWDevSig,m_pdSD,pbSelected, seg.nStart, seg.nEnd, m_dPChiSQ, m_dPTilting, m_dPSideness,m_dPPWDev, m_nWsTilting, m_nWsPWDev)) indexes.add(i);
        }
        return indexes;
    }
    int getSegmentEnd(int start, int len0){
        int end=start,len=0,lastEnd=m_pdX.length-1;
        while(len<len0){
            if(pbSelected[end])len++;
            end++;
            if(end>lastEnd) {
                end=-1;
                break;
            }
        }
        return end;
    }
    void registerSegments(){
        PolynomialLineFittingSegmentNode seg;
        int i,len=m_cvFittedSegments.size(),start,end,j,end0=m_pdX.length,index;
        for(i=0;i<len;i++){
            seg=m_cvFittedSegments.get(i);
            if(seg==null) continue;
            start=seg.nStart;
            end=seg.nEnd;
            for(j=start;j<=end;j++){
                m_pcOptimalSegments[j]=seg;
            }
            if(i>0){
                for(j=end0+1;j<=start;j++){
                    m_pcOptimalSegments[j]=seg;
                }
            }
        }
    }
    void registerSegmentGroups(){
        PolynomialLineFittingSegmentNode seg;
        int i,len=m_cvSegmentGroups.size(),start,end,j,end0=m_pdX.length,index;
        for(i=0;i<len;i++){
            seg=m_cvSegmentGroups.get(i);
            if(seg==null) continue;
            start=seg.nStart;
            end=seg.nEnd;
            for(j=start;j<=end;j++){
                m_pcOptimalSegments[j]=seg;
            }
            if(i>0){
                for(j=end0+1;j<=start;j++){
                    m_pcOptimalSegments[j]=seg;
                }
            }
        }
    }
    void fillBestAproximationSegs(){
        int i,len=m_pcOptimalSegments.length;
        PolynomialLineFittingSegmentNode segL,segR;
        double x,y;
        for(i=0;i<len;i++){
            if(m_pcOptimalSegments[i]!=null) continue;
            segL=getNextSegment(m_pcOptimalSegments,i,-1);
            segR=getNextSegment(m_pcOptimalSegments,i,1);
            x=m_pdX[i];
            y=m_pdY[i];
            if(segL==null&&segR==null){
                continue;//should not happen, unless bug
            }
            m_pcOptimalSegments[i]=PolynomialLineFittingSegmentNode.getBetterSegment(segL, segR, x, y);
        }
    }
    PolynomialLineFittingSegmentNode getNextSegment(PolynomialLineFittingSegmentNode[] pcSegs, int index, int delta){
        PolynomialLineFittingSegmentNode seg=null;
        int len=pcSegs.length;
        index+=delta;
        while(index>=0&&index<len){
            seg=pcSegs[index];
            if(seg!=null) return seg;
            index+=delta;
        }
        return seg;
    }
    void  calSD(){
        double[] pbDelta=CommonStatisticsMethods.getDeltaArray(m_pdY,null,nMaxRisingInterval);
        m_pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(m_pdY, nMaxRisingInterval, m_nWsSD);
    }
    public PolynomialLineFittingSegmentNode[] getOptimalRegressions(){
        return m_pcOptimalSegments;
    }
    public PolynomialLineFittingSegmentNode[] getStartingRegressions(){
        return m_pcOptimalSegments;
    }
    public PolynomialLineFittingSegmentNode[] getEndingRegressions(){
        return m_pcOptimalSegments;
    }
    public PolynomialLineFittingSegmentNode[] getLongRegressions(){
        return m_pcOptimalSegments;
    }
    double getTotalDeviation(SimpleRegression sr, int start, int end){
        double dev=0;
        for(int i=start;i<end;i++){
            dev+=Math.abs(m_pdY[i]-sr.predict(m_pdX[i]));
        }
        return dev;
    }
    public int[] getRisingIntervals(){
        return m_pnRisingIntervals;
    }
    public String[][] getSignalJumpAsStringArray(){
        int i,len=m_cvFittedSegments.size();
        for(i=len-1;i>=0;i--){
            if(m_cvFittedSegments.get(i)==null) m_cvFittedSegments.remove(i);
        }
        len=m_cvFittedSegments.size();
        String[][] psData=new String[len][],psT;
        StringBuffer cST=new StringBuffer();
        String line;
        for(i=1;i<len;i++){
            LineSegmentRegressionEvaluater.getSignalJumpSignificance_ModelComparison(m_cvFittedSegments.get(i-1), m_cvFittedSegments.get(i), -1, cST);
            psT=CommonStatisticsMethods.getDelimitedStringAsStringArray(cST.toString(), LineSegmentRegressionEvaluater.newString, LineSegmentRegressionEvaluater.newLine);
            if(i==1) psData[0]=psT[0];
            psT[1][0]=""+i;
            psData[i]=psT[1];
        }
        return psData;
    }
    public String[][] getSplittedSegmentsAsStringArray(){
        int i,len=m_cvSplittedSegments.size()/2,position=0;
        PolynomialLineFittingSegmentNode segL,segR;
        String[][] psData=new String[len][],psT;
        StringBuffer cST=new StringBuffer();
        ArrayList<String[]> svRows=new ArrayList();
        String[] psHead=null;
        for(i=1;i<len;i++){
            segL=m_cvSplittedSegments.get(position);
            position++;
            segR=m_cvSplittedSegments.get(position);
            position++;
            if(segL==null||segR==null) continue;
            LineSegmentRegressionEvaluater.getSignalJumpSignificance_ModelComparison(segL,segR, -1, cST);
            psT=CommonStatisticsMethods.getDelimitedStringAsStringArray(cST.toString(), LineSegmentRegressionEvaluater.newString, LineSegmentRegressionEvaluater.newLine);
            if(i==1) psHead=psT[0];
            psT[1][0]=""+i;
            svRows.add(psT[1]);
        }
        len=svRows.size();
        psData=new String[len+1][];
        psData[0]=psHead;
        for(i=0;i<len;i++){
            psData[i+1]=svRows.get(i);
        }
        return psData;
    }
    public ArrayList<PolynomialLineFittingSegmentNode> getFittedSegments(){
        return m_cvFittedSegments;
    }
    int reconnectSegments(){
        m_cvSegmentGroups.clear();
        int i,len=m_cvFittedSegments.size();
        return 1;
    }
    public ArrayList<double[]> getLevelEstimationLines(){
        return m_pdvLevelEstimationLines;
    }
    int mergeSegments(){
        int i,index,len=m_cvFittedSegments.size(),left,right;
        if(m_svMergingSegStringArray==null) m_svMergingSegStringArray=new ArrayList();
        if(m_svBreakStatus==null) m_svBreakStatus=new ArrayList();
        m_svMergingSegStringArray.clear();
        m_svBreakStatus.clear();
        if(m_dvProjectionSig==null) m_dvProjectionSig=new ArrayList();
        if(m_dvProjectionSigMW==null) m_dvProjectionSigMW=new ArrayList();
        m_dvProjectionSig.clear();
        m_dvProjectionSigMW.clear();
        
        if(m_dvJumpSignificance1==null) m_dvJumpSignificance1=new ArrayList();
        if(m_dvJumpSignificance2==null) m_dvJumpSignificance2=new ArrayList();
        m_dvJumpSignificance1.clear();
        m_dvJumpSignificance2.clear();
        if(m_dvSignal==null) m_dvSignal=new ArrayList();
        if(m_dvDelta==null) m_dvDelta=new ArrayList();
        m_dvSignal.clear();
        m_dvDelta.clear();

        if(m_dvDifferenceSigTTest==null) m_dvDifferenceSigTTest=new ArrayList();
        m_dvDifferenceSigTTest.clear();
        if(m_dvDifferenceSigMW==null) m_dvDifferenceSigMW=new ArrayList();
        m_dvDifferenceSigMW.clear();
        if(m_dvDifferenceSigChiSQ==null) m_dvDifferenceSigChiSQ=new ArrayList();
        m_dvDifferenceSigChiSQ.clear();
        if(m_dvSD==null) m_dvSD=new ArrayList();
        m_dvSD.clear();
        if(m_pdvLevelEstimationLines==null) m_pdvLevelEstimationLines=new ArrayList();
        m_pdvLevelEstimationLines.clear();

        String[][] psT;
        double dL,dR,delta,signal;

        double[] pdDiffSigs=new double[len-1];
        m_pnBreakIndexes=CommonStatisticsMethods.getIntArray(m_pnBreakIndexes,len-1);
        for(i=1;i<len;i++){
            pdDiffSigs[i-1]=-LineSegmentRegressionEvaluater.getSignalJumpSignificance_ModelComparison(m_cvFittedSegments.get(i-1), m_cvFittedSegments.get(i), -1, null);
            m_pnBreakIndexes[i-1]=i-1;
        }
        QuickSort.quicksort(pdDiffSigs, m_pnBreakIndexes);
        ArrayList<Integer> nvMergedSegIndexes=new ArrayList();

        ArrayList<IntPair> cvMergedSegPairs=new ArrayList();
        ArrayList<PolynomialLineFittingSegmentNode> cvMergedSegs=new ArrayList(),cvSegs=new ArrayList();
        PolynomialLineFittingSegmentNode seg,segL, segR, segM1,segM2,segM3,segLt,segRt,segLL,segRL;
        ArrayList<Integer> nvMergedBreaks=new ArrayList();
        m_nvUnmergedBreaks=new ArrayList();
        double dPPWDev=0.05,dOutliarRatio=0.2;
        double[] pdX=m_cvFittedSegments.get(0).pdX;
        double jumpSig1,jumpSig2,sigProjection,sigProjectionMW,diffSigTTest,diffSigMW,diffSigChiSQ,dSD,lineDelta=0.1*(pdX[pdX.length-1]-pdX[0])/(pdX.length-1);
        int nMaxSegLen=20,start,end,len1,index0,startL,endL,nExt;
        ArrayList<double[]> pdvT=new ArrayList();
        ArrayList<Double> dvXs=new ArrayList();
        int nNumExtrema;

        for(i=0;i<len-1;i++){
            index0=m_pnBreakIndexes[i];
            if(index0==4){
                index0=index0;
            }
            seg=m_cvFittedSegments.get(index0);
            left=seg.nEnd;
            right=left+m_pnRisingIntervals[left];

            getMergedSegIndexes(cvMergedSegPairs,nvMergedSegIndexes,index0,-1);
            len1=nvMergedSegIndexes.size();
//            index=nvMergedSegIndexes.get(len1-1);
            index=index0;
            segL=m_cvFittedSegments.get(index);
            nNumExtrema=getNumExtrema(segL.nStart,segL.nEnd);
            if(nNumExtrema<3&&index>0) segL=m_cvFittedSegments.get(index-1);

            getMergedSegIndexes(cvMergedSegPairs,nvMergedSegIndexes,index0+1,1);
            len1=nvMergedSegIndexes.size();
//            index=nvMergedSegIndexes.get(len1-1);
            index=index0+1;
            segR=m_cvFittedSegments.get(index);
            nNumExtrema=getNumExtrema(segR.nStart,segR.nEnd);
            if(nNumExtrema<3&&index<m_cvFittedSegments.size()-1) segR=m_cvFittedSegments.get(index+1);

            start=Math.max(segL.nStart,left-nMaxSegLen);
            end=Math.min(segR.nEnd,right+nMaxSegLen);

            nExt=10;
            segLL=makeSegment(start,left,nOrder,-1,-1);
            segRL=makeSegment(right,end,nOrder,-1,-1);
            segLt=makeSegment(start,left,-1,-1,-1);
            segRt=makeSegment(right,end,-1,-1,-1);

            startL=Math.max(start, right-nExt);
            endL=Math.min(end,left+nExt);

            dvXs.clear();
            dvXs.add(pdX[start]);
            dvXs.add(pdX[left]);
            dvXs.add(pdX[endL]);
            cvSegs.clear();
            cvSegs.add(segLt);
            cvSegs.add(segLL);
            LineSegmentRegressionEvaluater.getLine(cvSegs, dvXs, lineDelta, pdvT);
            m_pdvLevelEstimationLines.add(pdvT.get(0));
            m_pdvLevelEstimationLines.add(pdvT.get(1));

            dvXs.clear();
            dvXs.add(pdX[startL]);
            dvXs.add(pdX[right]);
            dvXs.add(pdX[end]);
            cvSegs.clear();
            cvSegs.add(segRL);
            cvSegs.add(segRt);
            LineSegmentRegressionEvaluater.getLine(cvSegs, dvXs, lineDelta, pdvT);
            m_pdvLevelEstimationLines.add(pdvT.get(0));
            m_pdvLevelEstimationLines.add(pdvT.get(1));

            dL=seg.pdX[left];
            signal=segLt.predict(dL);
            dR=seg.pdX[right];
            delta=signal-segRt.predict(dR);

            m_dvSignal.add(signal);
            m_dvDelta.add(delta);

            jumpSig1=LineSegmentRegressionEvaluater.getSignalJumpSignificance_ModelComparison(segLt, segRt, nOrder, null);
            jumpSig2=LineSegmentRegressionEvaluater.getDifferenceSignificance_ModelComparison(segLt, segRt);
            m_dvJumpSignificance1.add(jumpSig1);
            m_dvJumpSignificance2.add(jumpSig2);
            sigProjection=LineSegmentRegressionEvaluater.getBreakSignificance_Projection(segLL,segRL, nExt);
            sigProjectionMW=LineSegmentRegressionEvaluater.getBreakSignificance_ProjectionMW(segLL,segRL, nExt);
            m_dvProjectionSig.add(sigProjection);
            m_dvProjectionSigMW.add(sigProjectionMW);
            diffSigTTest=LineSegmentRegressionEvaluater.getDifferenceSignificance_TTest(segLt,segRt);
            diffSigChiSQ=LineSegmentRegressionEvaluater.getDifferenceSignificance_ChiSQ(segLt,segRt);
            diffSigMW=LineSegmentRegressionEvaluater.getDifferenceSignificance_MW(segLt,segRt);
            m_dvDifferenceSigTTest.add(diffSigTTest);
            m_dvDifferenceSigChiSQ.add(diffSigChiSQ);
            m_dvDifferenceSigMW.add(diffSigMW);
            dSD=CommonStatisticsMethods.getMean(m_pdSD, null, start, end, 1);
            m_dvSD.add(dSD);

            segM1=makeSegment(start,end,nOrder,-1,-1);
            psT=segM1.getRegressionResultsAsStringArray("SegM1");
            if(i==0) m_svMergingSegStringArray.add(psT[0]);
            m_svMergingSegStringArray.add(psT[1]);
            segM2=makeSegment(start,end,nOrder,dPPWDev,dOutliarRatio);
            psT=segM2.getRegressionResultsAsStringArray("SegM2");
            m_svMergingSegStringArray.add(psT[1]);
            segM3=makeSegment(start,end,-1,dPPWDev,dOutliarRatio);
            psT=segM3.getRegressionResultsAsStringArray("SegM3");
            m_svMergingSegStringArray.add(psT[1]);
            if(segM1.validDev) {
                cvMergedSegPairs.add(new IntPair(index0,index0+1));
                nvMergedBreaks.add(index0);
            }else{
                if(segM2.validDev){
                    cvMergedSegPairs.add(new IntPair(index0,index0+1));
                    nvMergedBreaks.add(index0);
                }else{
                    if(end-start>20){
                        if(segM3.validDev){
                            cvMergedSegPairs.add(new IntPair(index0,index0+1));
                            nvMergedBreaks.add(index0);
                        }else
                            m_nvUnmergedBreaks.add(index0);
                    }else
                        m_nvUnmergedBreaks.add(index0);
                }
            }
            m_svBreakStatus.add("NV");
        }
        QuickSortInteger.quicksort(m_nvUnmergedBreaks);
        return 1;
    }
    public int setBreakStatus(DoubleRange xRange, String status){
        ArrayList<Integer> indexes=getBreakIndex(xRange);
        if(indexes.isEmpty()) return -1;
        int i,index,len=indexes.size();
        for(i=0;i<len;i++){
            index=indexes.get(i);
            if(index>=m_svBreakStatus.size()) continue;
            m_svBreakStatus.set(index, status);
        }
        return 1;
    }
    public void setVerification(String sVerification){
        m_sVerification=sVerification;
    }
    ArrayList<Integer> getBreakIndex(DoubleRange xRange){
        ArrayList<Integer> indexes=new ArrayList();
        if(!isMerged()) return indexes;
        int i,len=m_cvFittedSegments_Stored.size();
        PolynomialLineFittingSegmentNode seg;
        for(i=0;i<len;i++){
            seg=m_cvFittedSegments_Stored.get(i);
            if(xRange.contains(seg.pdX[seg.nEnd])) indexes.add(i);
        }
        return indexes;
    }
    void getMergedSegIndexes(ArrayList<IntPair> cvMergedSegIndexPairs,ArrayList<Integer> nvMergedSegIndexes, int index, int direction){
        int i,len=cvMergedSegIndexPairs.size(),j,len1;
        ArrayList<Integer> nvPairedIndexes=CommonStatisticsMethods.getPairedNumbers(cvMergedSegIndexPairs, index);
        ArrayList<Integer> nvT;
        nvMergedSegIndexes.clear();
        nvMergedSegIndexes.add(index);
        if(direction<0)
            nvT=CommonStatisticsMethods.getSmallerElements(nvPairedIndexes,index);
        else
            nvT=CommonStatisticsMethods.getLargerElements(nvPairedIndexes,index);
        while(!nvT.isEmpty()){
            index=nvT.get(0);
            nvMergedSegIndexes.add(index);
            nvT.clear();
            if(direction<0)
                nvT=CommonStatisticsMethods.getSmallerElements(nvPairedIndexes,index);
            else
                nvT=CommonStatisticsMethods.getLargerElements(nvPairedIndexes,index);
        }
    }
    int refineSegments(){
        double dPPWDev=0.05, dOutliarRatio=0.2;
//        buildSmoothRanges(dPPWDev,dOutliarRatio);
        removeShortSegments();
        mergeSegments();
        buildSegmentGroups_MergedBreaks(dPPWDev,dOutliarRatio);
        replaceFittedSegmentsWithGroups();
        return 1;
    }
    int buildSegmentGroups_MergedBreaks(double dPPWDev, double dOutliarRatio){
        int i,len=m_nvUnmergedBreaks.size();
        intRange ir;
        if(m_cvSegmentGroups==null)
            m_cvSegmentGroups=new ArrayList();
        else
            m_cvSegmentGroups.clear();

        int start=m_cvFittedSegments.get(0).nStart,end,index;
        PolynomialLineFittingSegmentNode seg;
        for(i=0;i<len;i++){
            index=m_nvUnmergedBreaks.get(i);
            seg=m_cvFittedSegments.get(index);
            end=seg.nEnd;
            m_cvSegmentGroups.add(makeSegmentGroup(new intRange(start,end),standardSegLength,dPPWDev,dOutliarRatio));
            seg=m_cvFittedSegments.get(index+1);
            start=seg.nStart;
        }
        seg=m_cvFittedSegments.get(m_cvFittedSegments.size()-1);
        end=seg.nEnd;
        m_cvSegmentGroups.add(makeSegmentGroup(new intRange(start,end),standardSegLength,dPPWDev,dOutliarRatio));
        return 1;
    }
    int buildSegmentGroups_SmoothRegions(double dPPWDev, double dOutliarRatio){
        int i,len=m_cvSmoothRanges.size();
        intRange ir;
        if(m_cvSegmentGroups==null)
            m_cvSegmentGroups=new ArrayList();
        else
            m_cvSegmentGroups.clear();

        for(i=0;i<len;i++){
            ir=m_cvSmoothRanges.get(i);
            m_cvSegmentGroups.add(makeSegmentGroup(ir,standardSegLength,dPPWDev,dOutliarRatio));
        }
        return 1;
    }
    LineFittingSegmentGroup makeSegmentGroup(intRange ir, int minSegLen,double dPPWDev,double dOutliarRatio){
        int i,nSegs=Math.max(1,ir.getRange()/minSegLen),segLen=ir.getRange()/nSegs;
        int left=ir.getMin(),right,limit=ir.getMax();
        LineFittingSegmentGroup segGroup=new LineFittingSegmentGroup();
        PolynomialLineFittingSegmentNode seg;
        for(i=0;i<nSegs;i++){
            right=left+segLen;
            if(right>limit) right=limit;
            seg=makeSegment(left,right,nOrder,dPPWDev,dOutliarRatio);
            if(!seg.isValid()) continue;
            segGroup.addSegment(seg);
            left=right+1;
        }
        return segGroup;
    }
    int buildSmoothRanges(double dPPWDev, double dOutliarRatio){
        int i,index,len=m_cvFittedSegments.size(),len1;
        PolynomialLineFittingSegmentNode seg;
        double[] segLength=new double[len];
        int indexes[]=new int[len];
        for(i=0;i<len;i++){
            seg=m_cvFittedSegments.get(i);
            segLength[i]=seg.nEnd-seg.nStart;
            indexes[i]=i;
        }
        QuickSort.quicksort(segLength, indexes);

        m_cvSmoothRanges=new ArrayList();
        intRange smoothRange;
        for(i=0;i<len;i++){
            index=indexes[len-1-i];
            seg=m_cvFittedSegments.get(index);
            smoothRange=new intRange(seg.nStart,seg.nEnd);
            if(intRange.enclosed(m_cvSmoothRanges, smoothRange)) continue;
            smoothRange=getSmoothRange(seg,dPPWDev,dOutliarRatio);
            m_cvSmoothRanges.add(smoothRange);
        }
        return 1;
    }
    intRange getSmoothRange(PolynomialLineFittingSegmentNode seg, double dPPWDev, double dOutliarRatio){
        intRange SmoothRange=new intRange(getSmoothSegmentlimit(seg,-1,dPPWDev,dOutliarRatio),getSmoothSegmentlimit(seg,1,dPPWDev,dOutliarRatio));
        return SmoothRange;
    }
    int getSmoothSegmentlimit(PolynomialLineFittingSegmentNode seg, int direction, double dPPWDev, double dOutliarRatio){
        int limit=0,start,end,segStart,segEnd,newStart,newEnd,newEnd0;
        int SlidingLen=20,stepSize=5;
        if(direction>0){
            segStart=seg.nStart;
            segEnd=seg.nEnd;
            start=0;
            end=m_pdX.length-1;
        }else{
            segStart=seg.nEnd;
            segEnd=seg.nStart;
            start=m_pdX.length-1;
            end=0;
        }
        newStart=segStart;
        newEnd=segEnd+direction*stepSize;
        if(newEnd*direction>end) newEnd=end;
        int len=Math.abs(segEnd-segStart);
        if(len>SlidingLen) newStart=newEnd-direction*SlidingLen;
        limit=getSmoothLimit(start,newStart,end,newEnd,direction,SlidingLen,stepSize,seg.nOrder,dPPWDev,dOutliarRatio);
        while(limit*direction>=newEnd*direction&&limit*direction<end){
            newEnd+=direction*stepSize;
            if(newEnd*direction>end) newEnd=end;
            len=Math.abs(newStart-newEnd);
            if(len>SlidingLen) newStart=newEnd-direction*SlidingLen;
            limit=getSmoothLimit(start,newStart,end,newEnd,direction,SlidingLen,stepSize,seg.nOrder,dPPWDev,dOutliarRatio);
        }
        newEnd=limit;
//        newEnd=getRetractedEnd(start,newStart,end,newEnd,direction,SlidingLen,stepSize,seg.nOrder,dPPWDev,dOutliarRatio);
        if(newEnd*direction<=segEnd) return segEnd;

        PolynomialLineFittingSegmentNode newSeg0=makeSegment(newStart,newEnd,seg.nOrder,dPPWDev,dOutliarRatio);
        newEnd-=direction;
        if(newEnd*direction<=segEnd) return segEnd;
        PolynomialLineFittingSegmentNode newSeg1=makeSegment(newStart,newEnd,seg.nOrder,dPPWDev,dOutliarRatio);

        while(newSeg1.dSigChiSQ<newSeg0.dSigChiSQ){
            if(newStart!=segStart)newStart-=direction;
            newEnd-=direction;
            if(direction*newEnd<=direction*segEnd) return segEnd;
            newSeg0=newSeg1;
            newSeg1=makeSegment(newStart,newEnd,seg.nOrder,dPPWDev,dOutliarRatio);
        }
        intRange effectiveRange=newSeg0.getEffectRange();
        if(direction<0)
            newEnd=effectiveRange.getMin();
        else
            newEnd=effectiveRange.getMax();
        return newEnd;
    }
    int getRetractedEnd(int start, int segStart, int end, int segEnd, int direction, int SlidingLen, int stepSize, int order, double dPPWDev, double dOutliarRatio){
        int newEnd=0;
/*        PolynomialLineFittingSegmentNode newSeg=makeSegment(newStart,newEnd,seg.nOrder,dPPWDev,dOutliarRatio);
        while(!newSeg.validDev){
            if(newStart!=segStart)newStart-=direction;
            newEnd-=direction;
            if(direction*newEnd<=direction*segEnd) break;
            newSeg=makeSegment(newStart,newEnd,seg.nOrder,dPPWDev,dOutliarRatio);
        }*/
        return newEnd;
    }
    int getSmoothLimit (int start, int segStart, int end, int segEnd, int direction, int SlidingLen, int stepSize, int order, double dPPWDev, double dOutliarRatio){
        PolynomialLineFittingSegmentNode ExtSeg=makeSegment(segStart,segEnd,order,dPPWDev,dOutliarRatio);
        if(ExtSeg.validDev){
            intRange extRange=new intRange(Math.min(segEnd+direction*stepSize, segEnd),Math.max(segEnd+direction*stepSize, segEnd)),ir;
            ArrayList<intRange> cvOutliarRegions=ExtSeg.cvOutliarRegions;
            ArrayList<intRange> extOutliarRanges=intRange.getOverlappedRanges(cvOutliarRegions, extRange);
            if(extOutliarRanges.isEmpty()) return segEnd;
            ir=intRange.getContainingRange(extOutliarRanges, segEnd);
            if(ir!=null){
               int nextCrossingPoint=ExtSeg.getNextCrossingPoint(segEnd,direction);
               if(nextCrossingPoint<0){
                   return direction*Math.min(direction*ir.getMin(), direction*ir.getMax());
               }
               int newEnd=nextCrossingPoint+direction*stepSize;
               if(direction*newEnd>end) newEnd=end;
               ExtSeg=makeSegment(segStart,newEnd,order,dPPWDev,dOutliarRatio);
               if(ExtSeg.validDev)
                   return nextCrossingPoint;
               else
                   return direction*Math.min(direction*ir.getMin(), direction*ir.getMax());
            }else{
                return segEnd;
            }
        }
        return segEnd-direction;
    }
    PolynomialLineFittingSegmentNode makeSegment(int start, int end, int order, double dPPWDev, double dOutliarRatio){
        int order0=Math.abs(order),maxOrder=2;
        int nNumExtrema=getNumExtrema(start,end);
        if(maxOrder>nNumExtrema/4) maxOrder=nNumExtrema/4;
        if(dOutliarRatio<0) dOutliarRatio=m_dOutliarRatio;
        if(order<0) order0=nOrder;
        if(dPPWDev<0) dPPWDev=m_dPPWDev;
        PolynomialLineFittingSegmentNode seg0= new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,start,end,order0,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,dPPWDev,dOutliarRatio);
        if(order>0||order0>=maxOrder) return seg0;
        order0++;
        PolynomialLineFittingSegmentNode seg= new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,start,end,order0,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,dPPWDev,dOutliarRatio);
        while(seg.dSigChiSQ>seg0.dSigChiSQ&&order0<=maxOrder){
            seg0=seg;
            order0++;
            seg= new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,m_pdTiltingSig,null,m_pdPWDevSig,m_pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,start,end,order0,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,dPPWDev,dOutliarRatio);
        }
        return seg0;
    }
    public String[][] getMergingSegmentsAsStraingArray(){
        String[] psT=m_svMergingSegStringArray.get(1);
        PolynomialLineFittingSegmentNode seg,segR;
        int i,len=m_svMergingSegStringArray.size(),len1=psT.length,j,row,col,nMaxLen=20;
        String[][] psData=new String[len/3+1][3*psT.length+16];
        double sigJump1,sigJump2,sigProjection,sigProjectionMW,dSignal,dDelta,diffSigTTest,diffSigChiSQ,diffSigMW;
        col=0;
        psData[0][col]="SignalID";
        col++;
        psData[0][col]="Verification";
        col++;
        psData[0][col]="Break";
        col++;
        psData[0][col]="Position";
        col++;
        psData[0][col]="Status";
        col++;
        psData[0][col]="Signal";
        col++;
        psData[0][col]="Delta";
        col++;
        psData[0][col]="Delta_N";
        col++;
        psData[0][col]="SNR";
        col++;
        psData[0][col]="ProjectionSig";
        col++;
        psData[0][col]="ProjectionMW";
        col++;
        psData[0][col]="ModelComp1";
        col++;
        psData[0][col]="ModelComp2";
        col++;
        psData[0][col]="DiffSigChiSQ";
        col++;
        psData[0][col]="DiffSigTTest";
        col++;
        psData[0][col]="DiffSigMW";
        col++;
        psT=m_svMergingSegStringArray.get(0);
        for(i=0;i<3;i++){
            for(j=0;j<len1;j++){
                psData[0][col]=psT[j];
                col++;
            }
        }

        row=1;
        col=0;
        int index;
        double dMaxDelta=Math.abs(CommonStatisticsMethods.getRange(m_dvDelta).getMax());
        double x;
        for(i=0;i<len/3;i++){
            col=0;
            index=m_pnBreakIndexes[i];
            seg=m_cvFittedSegments_Stored.get(index);
            segR=m_cvFittedSegments_Stored.get(index+1);
            x=seg.pdX[seg.nEnd];
            psData[index+1][col]=m_sSignalID;
            col++;
            psData[index+1][col]=m_sVerification;
            col++;
            psData[index+1][col]=""+m_pnBreakIndexes[i];
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(x, 3);
            col++;
            psData[index+1][col]=m_svBreakStatus.get(index);//the status is assigned after fitting, and the index is not changed
            col++;
            sigProjection=m_dvProjectionSig.get(i);
            sigProjectionMW=m_dvProjectionSigMW.get(i);
            dSignal=m_dvSignal.get(i);
            dDelta=m_dvDelta.get(i);
            sigJump1=m_dvJumpSignificance1.get(i);
            sigJump2=m_dvJumpSignificance2.get(i);
            diffSigTTest=m_dvDifferenceSigTTest.get(i);
            diffSigChiSQ=m_dvDifferenceSigChiSQ.get(i);
            diffSigMW=m_dvDifferenceSigMW.get(i);
            psData[index+1][col]=PrintAssist.ToStringScientific(dSignal, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(dDelta, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(dDelta/dMaxDelta, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(dDelta/m_dvSD.get(i), 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(sigProjection, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(sigProjectionMW, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(sigJump1, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(sigJump2, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(diffSigChiSQ, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(diffSigTTest, 3);
            col++;
            psData[index+1][col]=PrintAssist.ToStringScientific(diffSigMW, 3);
            col++;
            psT=m_svMergingSegStringArray.get(row);
            for(j=0;j<len1;j++){
                psData[index+1][col]=psT[j];
                col++;
            }
            row++;
            psT=m_svMergingSegStringArray.get(row);
            for(j=0;j<len1;j++){
                psData[index+1][col]=psT[j];
                col++;
            }
            row++;
            psT=m_svMergingSegStringArray.get(row);
            for(j=0;j<len1;j++){
                psData[index+1][col]=psT[j];
                col++;
            }
            row++;
        }
        return psData;
    }
    public int getNumExtrema(int start, int end){
        return m_pnLnPositions[end]-m_pnLnPositions[start]+m_pnLxPositions[end]-m_pnLxPositions[start];
    }
    public double[] getDataX(){
        return m_pdX;
    }
    public double[] getDataY(){
        return m_pdY;
    }
    public int getMaxRisingInterval(){
        return nMaxRisingInterval;
    }
    public boolean[] getDataSelection(){
        return pbSelected;
    }
    public PolynomialLineFittingSegmentNode getLineSegment(int start, int end){
        PolynomialLineFittingSegmentNode seg=this.makeSegment(start, end, nOrder, m_dPPWDev, m_dOutliarRatio);
        return seg;
    }
    public intRange getSmoothRange(int position){
        return new intRange();
    }
}
