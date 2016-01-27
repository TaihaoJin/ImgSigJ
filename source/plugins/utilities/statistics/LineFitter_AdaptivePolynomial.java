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
import utilities.statistics.PolynomialLineFitter;
import utilities.CustomDataTypes.IntPair;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.Constrains.ParValidityChecker;
/**
 *
 * @author Taihao
 */
public class LineFitter_AdaptivePolynomial extends PolynomialLineFitter{
    double[] m_pdX,m_pdY,m_pdSD,m_pdSelectedX,m_pdSelectedY,m_pdSelectedSD;
    int[] m_pnLxPositions,m_pnLnPositions;
    ArrayList<Integer> m_nvLx, m_nvLn,m_nvSelectedIndexes;
    int nDataSize,nMaxRisingInterval,minSegLen,maxSegLen,lastStart;
    boolean[] pbSelected,pbSelectedT;
    double m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio,m_dPTerminals;
    int[] m_pnRisingIntervals;
    int m_nWsTilting, m_nWsPWDev, nOrder,m_nWsSD;
    boolean[] pbSmoothPositionL,pbSmoothPositionR;
    int[] pnSmoothRegions;
    int[] pnSmoothSegLengths;
    int[] pnRisingIntervals;
    int firstSelectedPosition;
    String m_sDataSelectionOption;
//    PolynomialRegression[] m_pcOptRegressions,m_pcStartingRegressions,m_pcEndRegressions;
    PolynomialLineFittingSegmentNode[] m_pcOptimalSegments,m_pcOptimalStartingSegments,m_pcOptimalEndingSegments,m_pcOptimalLongSegments;
    double dSDSelected;
    double dCrossingPointRatio;
    IntPair SmoothSegTerminalLength;
    LineFeatureExtracter2 m_cLineFeatureExtracter;
    ArrayList<ParValidityChecker> cvParValidityCheckers;
    public LineFitter_AdaptivePolynomial(){
        SmoothSegTerminalLength=new IntPair(0,0);
        cvParValidityCheckers=null;
        m_cLineFeatureExtracter=null;
    }
//    public LineFitter_AdaptivePolynomial(ArrayList<Double> dvX, ArrayList<Double> dvY, int nMaxRisingInterval, int nWs, double pCutoff){
    public LineFitter_AdaptivePolynomial(double[] pdX, double[] pdY, boolean[] pbSelected, int nRisingInterval, int nOrder, int maxSegLen, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
        this(pdX,pdY,pbSelected,nRisingInterval,nOrder,maxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals,dOutliarRatio,-1);
    }
    
    public LineFitter_AdaptivePolynomial(double[] pdX, double[] pdY, boolean[] pbSelected, int nRisingInterval, int nOrder, int maxSegLen, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio, double dCrossingPointRatio){
        this();
        setCrossingPointsRatio(dCrossingPointRatio);
        fit(pdX,pdY,pbSelected,nRisingInterval,nOrder,maxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals,dOutliarRatio);
    }
    
    public void setCrossingPointsRatio(double dCrossingPointRatio){
        this.dCrossingPointRatio=dCrossingPointRatio;
    }
    
    public void fit(double[] pdX, double[] pdY, boolean[] pbSelected, int nRisingInterval, int nOrder, int maxSegLen, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
        int minSegLen=nOrder+3;  
        fit(pdX, pdY, pbSelected, nRisingInterval, nOrder, minSegLen, maxSegLen, nWsSD, nWsTilting, nWsPWDev, dPChiSQ, dPTilting,  dPSideness, dPPWDev, dPTerminals, dOutliarRatio);
    }
    public void fit(double[] pdX, double[] pdY, boolean[] pbSelected, int nRisingInterval, int nOrder, int minSegLen, int maxSegLen, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
        
        this.m_dOutliarRatio=dOutliarRatio;
        m_nWsSD=nWsSD;
        nDataSize=pdX.length;
        m_pdX=pdX;
        m_pdY=pdY;
        this.nOrder=nOrder;
        m_nWsTilting=nWsTilting;
        m_nWsPWDev=nWsPWDev;
        m_dPChiSQ=dPChiSQ;
        m_dPTilting=dPTilting;
        m_dPPWDev=dPPWDev;
        m_dPSideness=dPSideness;
        m_dPTerminals=dPTerminals;
        this.minSegLen=minSegLen;
        pbSmoothPositionL=new boolean[nDataSize];
        pbSmoothPositionR=new boolean[nDataSize];
        pnSmoothSegLengths=new int[nDataSize];
        
        CommonStatisticsMethods.setElements(pbSmoothPositionL, false);
        CommonStatisticsMethods.setElements(pbSmoothPositionR, false);
        CommonStatisticsMethods.setElements(pnSmoothSegLengths, 0);
        
        this.pbSelected=pbSelected;
        calLastStart();
        this.nMaxRisingInterval=nRisingInterval;
        m_pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        this.maxSegLen=maxSegLen;
        if(m_pdSD==null) calSD();
        m_nvLn=new ArrayList();
        m_nvLx=new ArrayList();
        CommonMethods.LocalExtrema(m_pdY,0,m_pdX.length-1,m_nvLn,m_nvLx);
        markLocalExtremaPositions();
        selectData();
        fitLine();
    }
    public void setSD(double[] pdSD){
        m_pdSD=pdSD;
    }
    public double[] getSD(){
        return m_pdSD;
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
            lastStart--;
            if(lastStart<0)
                break;
            if(pbSelected[lastStart])len++;
        }
        if(lastStart<0) lastStart=0;
        return 1;
    }
    void fitLine(){
        m_pcOptimalSegments=new PolynomialLineFittingSegmentNode[nDataSize];
        m_pcOptimalStartingSegments=new PolynomialLineFittingSegmentNode[nDataSize];
        m_pcOptimalEndingSegments=new PolynomialLineFittingSegmentNode[nDataSize];
        m_pcOptimalLongSegments=new PolynomialLineFittingSegmentNode[nDataSize];
        for(int index=0;index<nDataSize;index++){
            if(!pbSelected[index])continue;
            fitSegments(index);
        }
    }
    public int getNumExtrema(int start, int end){
        return m_pnLnPositions[end]-m_pnLnPositions[start]+m_pnLxPositions[end]-m_pnLxPositions[start];
    }
    void fitSegments(int start){
        if(start==150){
            start=start;
        }
        if(start>lastStart)
            start=lastStart;
        PolynomialLineFittingSegmentNode bestSeg=null,currentSeg=null;
        int end,len,lastEnd=m_pdX.length-1;
        for(len=minSegLen;len<=maxSegLen;len++){
            end=getSegmentEnd(start,len);
            if(end<0||end>lastEnd)
                break;//can not make a segment of this length
            if(start==16&&end==46){
                start=start;
            }
            currentSeg=new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,null,null,null,m_pdSD,pbSelected,null,m_pnLnPositions,m_pnLxPositions,start, end,nOrder,1,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dPTerminals,m_dOutliarRatio);
            
            if(invalidPars(currentSeg))
                    currentSeg.bValidPars=false;
    //            currentSeg=new PolynomialLineFittingSegmentNode(m_pdSelectedX,m_pdSelectedY,null,null,null,m_pdSelectedSD,pbSelectedT,null,m_pnLnPositions,m_pnLxPositions,start, end,nOrder,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dOutliarRatio);
            registerSegment(currentSeg);
            
        }
    }
    public boolean invalidPars(PolynomialLineFittingSegmentNode seg){
        if(cvParValidityCheckers==null) return false;
        int left=seg.getStart(0, 1),right=seg.getStart(0, -1);
        if(left==50&&right==62){
            left=left;
        }
        for(int i=0;i<cvParValidityCheckers.size();i++){
            if(cvParValidityCheckers.get(i).invalidPars(seg, m_pdX[left], m_pdX[right])) return true;
        }
        return false;
    }
    public void setSelection(boolean[] pbSelected){
        this.pbSelected=pbSelected;
    }
    public PolynomialLineFittingSegmentNode getLineSegment(int start, int end){
        return new PolynomialLineFittingSegmentNode(m_pdX,m_pdY,null,null,null,m_pdSD,pbSelected,null,m_pnLnPositions,m_pnLxPositions,start, end,nOrder,1,m_nWsSD,m_nWsTilting,m_nWsPWDev,m_dPChiSQ,m_dPTilting,m_dPSideness,m_dPPWDev,m_dPTerminals,m_dOutliarRatio);
    }
    int getSegmentEnd(int start, int len0){
        int end=start,len=0,lastEnd=m_pdX.length-1;
         if(pbSelected[end])len++;
        while(len<len0){
            end++;
            if(end>lastEnd) {
                end=-1;
                break;
            }
            if(pbSelected[end])len++;
        }
        return end;
    }
    void registerSegment(PolynomialLineFittingSegmentNode seg){
        PolynomialLineFittingSegmentNode seg0=null;
        int positiont=90;
        if(positiont>=m_pdX.length) positiont=m_pdX.length-1;
        seg0=m_pcOptimalLongSegments[positiont];
        int i,start=seg.nStart,end=seg.nEnd;
        if(start==90&&end==99){
            start=start;
        }
        int left,right;
        if(m_cLineFeatureExtracter==null){
            left=seg.getStart(0, -1);
            right=seg.getStart(0, 1);//left>right, so the segment is not going to be registed for smooth segs
        }else{
            intRange ir=m_cLineFeatureExtracter.getValidSmoothRange(seg);
            left=ir.getMin();
            right=ir.getMax();
        }
        
        if(left<0)
            left=start;
        if(right<0) 
            right=end;
        for(i=start;i<=end;i++){
            if(i==14){
                i=i;
            }
            m_pcOptimalSegments[i]=seg.getBetterFittingNode(seg, m_pcOptimalSegments[i]);
            if(i>=left&&i<=right) 
                m_pcOptimalLongSegments[i]=seg.getSmootherSegment(seg, m_pcOptimalLongSegments[i],i,(double) nMaxRisingInterval);
            if(i==positiont&&m_pcOptimalLongSegments[i]!=seg0){
                i=i;
            };
        }
        m_pcOptimalStartingSegments[start]=seg.getBetterStartingNode(seg, m_pcOptimalStartingSegments[start]);
        if(seg==seg.getBetterStartingNode(seg, m_pcOptimalStartingSegments[start])&&start==19){
            seg=seg;
        }
        if(end==97){
            end=end;
        }
        if(seg==seg.getBetterEndingNode(seg, m_pcOptimalEndingSegments[end])&&end==97){
            seg=seg;
        }
        m_pcOptimalEndingSegments[end]=seg.getBetterEndingNode(seg, m_pcOptimalEndingSegments[end]);
        markSmoothPositions(seg);
    }
    void  calSD(){
//        m_pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(m_pdY, nMaxRisingInterval, m_nWsSD);
        calFirstSelectedPosition();
        pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(m_pdY,nMaxRisingInterval);
        m_pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(m_pdX,m_pdY, pnRisingIntervals,pbSelected, m_nWsSD);
    }
    void calFirstSelectedPosition(){
        firstSelectedPosition=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, 0, 1);
    }
    public PolynomialLineFittingSegmentNode[] getOptimalRegressions(){
        return m_pcOptimalSegments;
    }
    public PolynomialLineFittingSegmentNode[] getStartingRegressions(){
        return m_pcOptimalStartingSegments;
    }
    public PolynomialLineFittingSegmentNode[] getEndingRegressions(){
        return m_pcOptimalEndingSegments;
    }
    public PolynomialLineFittingSegmentNode[] getLongRegressions(){
        return m_pcOptimalLongSegments;
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
    void selectData(){
        int i,len=m_pdX.length;
        m_nvSelectedIndexes=new ArrayList();
        for(i=0;i<len;i++){
            if(pbSelected[i]) m_nvSelectedIndexes.add(i);
        }
        len=m_nvSelectedIndexes.size();
        m_pdSelectedX=new double[len];
        m_pdSelectedY=new double[len];
        pbSelectedT=new boolean[len];
        m_pdSelectedSD=new double[len];
        for(i=0;i<len;i++){
            m_pdSelectedX[i]=m_pdX[m_nvSelectedIndexes.get(i)];
            m_pdSelectedY[i]=m_pdY[m_nvSelectedIndexes.get(i)];
            m_pdSelectedSD[i]=m_pdSD[m_nvSelectedIndexes.get(i)];
            pbSelectedT[i]=true;
        }
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
    int markSmoothPositions(PolynomialLineFittingSegmentNode seg){
        if(seg==null) return-1;
        if(!seg.isValid()||!seg.isValidDev()) return -1;
        double ratio=(double)seg.nNumCrossing/(double)(seg.nEnd-seg.nStart+1);
        if(ratio<dCrossingPointRatio) return -1;
        int len=seg.nEnd-seg.nStart,start=seg.nStart,end=seg.nEnd;
        if(start<3&&end>3){
            end=end;
        }
        int left=CommonStatisticsMethods.getNextCrossingPosition(seg.getRegressionNode(), m_pdX, m_pdY, pbSelected, seg.nStart, seg.nEnd, 1);//need to consider the selections at her. //12910
        if(seg.nStart==firstSelectedPosition) left=seg.nStart;
        if(left<0) return -1;
        int right=CommonStatisticsMethods.getNextCrossingPosition(seg.getRegressionNode(), m_pdX, m_pdY, pbSelected, seg.nEnd, seg.nStart, -1);
        if(right<0) return -1;
        if(left<297&&right>19){
            left=left;            
        }
        if(left>=right) return -1;
//        int segLen=seg.nEnd-seg.nStart+1;
        int segLen=seg.getRegressionNode().getDataSize();
        int  i;
/*        
        double dev,sd,sigPW;
        boolean[] validPWDev=new boolean[len];
        double[] pdDev=new double[len];
        for(i=0;i<len;i++){
            pdDev[i]=m_pdY[i]-seg.predict(m_pdX[i]);
        }
*/      
        left=start+SmoothSegTerminalLength.left;
        left=seg.getStart(SmoothSegTerminalLength.left,1);
        right=seg.getStart(SmoothSegTerminalLength.right,-1);
        if(left<0||right<0) return -1;
        
        if(start<2) left=start;
        right=end-SmoothSegTerminalLength.right;//12926
        if(right<left) return -1;
        
        if(m_cLineFeatureExtracter!=null){
            intRange ir=m_cLineFeatureExtracter.getValidSmoothRange(seg);
            if(ir.emptyRange()) return -1;
            left=ir.getMin();
            right=ir.getMax();
        }
        if(left<0) left=0;
        for(i=left+1;i<right;i++){
            pbSmoothPositionL[i]=true;
            pbSmoothPositionR[i]=true;
            if(i<=right-nMaxRisingInterval&&segLen>pnSmoothSegLengths[i]) {
                /*dev=Math.abs(m_pdY[i]-seg.predict(m_pdX[i]));
                sd=m_pdSD[i];
                sigPW=1.-GaussianDistribution.Phi(dev, 0, sd);
                if(sigPW>0.1) */
                if(i==90){
                    i=i;
                }
                    pnSmoothSegLengths[i]=segLen;
            }           
        }
        if(left<=3&&right>=3&&segLen>15){
            left=left;
        }
        pbSmoothPositionL[right]=true;
        pbSmoothPositionR[left]=true;
        if(left<=right-nMaxRisingInterval&&segLen>pnSmoothSegLengths[left]){
          /*  dev=Math.abs(m_pdY[left]-seg.predict(m_pdX[left]));
            sd=m_pdSD[left];
            sigPW=1.-GaussianDistribution.Phi(dev, 0, sd);
            if(sigPW>0.1) 
            */ 
            pnSmoothSegLengths[left]=segLen;
                if(left==152){
                    left=left;            
                }
        }
        return 1;
    }
    public boolean[] getLeftSmoothPositions(){
        return pbSmoothPositionL;
    }
    public boolean[] getRightSmoothPositions(){
        return pbSmoothPositionR;
    }
    public int markSmoothRegions(){
        pnSmoothRegions=new int[nDataSize];        
        int position=0,len=pbSmoothPositionL.length,region=0,i;
        for(i=0;i<nDataSize;i++){
            pnSmoothRegions[i]=-i+1;
        }
        boolean lSmooth,rSmooth;
        while(position<len){
            lSmooth=pbSmoothPositionL[position];
            rSmooth=pbSmoothPositionR[position];
            pnSmoothRegions[position]=region;
            while(rSmooth){
                position++;
                if(position>=len) break;
                lSmooth=pbSmoothPositionL[position];
                rSmooth=pbSmoothPositionR[position];
                pnSmoothRegions[position]=region;
                if(!lSmooth) ij.IJ.error("In consistent smooth mark----markSmoothRegions()");
            }
            position++;
            region++;
        }
        return 1;
    }
    public int[] getSmoothSegLengths(){
        if(pnSmoothSegLengths==null) markSmoothRegions();
        return pnSmoothSegLengths;
    }
    public intRange getSmoothRange(int position0){
        int l=position0,r=position0,position=position0;
        if(position0>=pbSmoothPositionR.length) return null;
        boolean lSmooth=pbSmoothPositionL[position], rSmooth=pbSmoothPositionR[position];
        if(!lSmooth&&!rSmooth) return new intRange();
        while(lSmooth){
            l=position;
            if(!pbSmoothPositionR[position]&&position<position0){
                ij.IJ.error("inconsistent left and right smoothness");
            }
            position--;
            if(position<0) break;
            lSmooth=pbSmoothPositionL[position];
        }
        position=position0;
        while(rSmooth){
            r=position;
            if(!pbSmoothPositionL[position]&&position<position0){
                ij.IJ.error("inconsistent left and right smoothness");
            }
            position++;
            if(position>=nDataSize) break;
            rSmooth=pbSmoothPositionR[position];
        }
        if(l>0&&pbSmoothPositionL[l]){
            if(pbSmoothPositionR[l-1]) 
                l--;
            else
                ij.IJ.error("inconsistent left and right smoothness");
        }
        if(r<nDataSize-1&&pbSmoothPositionR[r]){
            if(pbSmoothPositionL[r+1])
                r++;
            else
                ij.IJ.error("inconsistent left and right smoothness");
        }
        return new intRange(l,r);
    }
    public void setSmoothSegTerminalLength(IntPair SmoothSegTerminalLength){
        this.SmoothSegTerminalLength=SmoothSegTerminalLength;
    }
    public void setParValidityCheckters(ArrayList<ParValidityChecker> checkters){
        cvParValidityCheckers=checkters;
    }
    public void setLineFeatureExtracter(LineFeatureExtracter2 cFE){
        m_cLineFeatureExtracter=cFE;
    }
    public void setRisingInterval(int interval){
        nMaxRisingInterval=interval;
    }
}
