/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import utilities.CommonStatisticsMethods;
import utilities.CommonMethods;
import utilities.CustomDataTypes.intRange;
import java.util.ArrayList;
import utilities.statistics.MeanSem1;
import utilities.CustomDataTypes.DoubleRange;
import utilities.QuickSort;
import java.util.ArrayDeque;
import utilities.Gui.PlotWindowPlus;
import utilities.CustomDataTypes.IntpairCouple;
import utilities.CustomDataTypes.IntPair;
import utilities.statistics.LineSegmentNode;
import utilities.statistics.PolynomialRegression;
import utilities.statistics.LineSegmentNode;

/**
 *
 * @author Taihao
 */
public class LineSegmenter_FiltedAmpBased {
    ArrayList<double[]> m_pdvFiltedY;//FiltedY, elements are differed by the scales of filtering
    double[] m_pdX, m_pdY,m_pdDeltaL,m_pdDeltaS,m_pdYFilted,m_pdYFiltedF,m_pdYFiltedR;
    ArrayList<intRange> m_cvSegments; //the min and max of a m_cvSmoothLimits element (an intRange) mark the end and begining of the smooth
    ArrayList<IntpairCouple> m_cvSegEndRanges;
    double m_dSmoothnessCutoff,m_dSegmentationCutoff,m_dMaxDelta;
    int m_nMaxRisingInterval,m_nNumFiltedY;
    ArrayList<Integer> m_nvTransitionsDownward,m_nvTransitionsUpward;
    DoubleRange m_cDeltaRange;
    int[] m_pnRisingIntervals;
    ArrayList<LineSegmentNode> m_cvSegmentNodes;
    int m_nWs;
    public LineSegmenter_FiltedAmpBased(double[] pdX, double[] pdY, ArrayList<double[]> pdvFiltedY,int nMaxRisingInterval){
        m_pdX=pdX;
        m_pdY=pdY;
        m_nWs=0;
//        m_pdYFilted=CommonStatisticsMethods.copyArray(pdY);
        ArrayList<Integer> nvIndexes=null;
        m_pdYFilted=CommonStatisticsMethods.getRunningWindowQuantile(pdY, m_nWs, m_nWs, nvIndexes);
        int i,len=m_pdY.length,left,right;
        m_pdYFiltedF=new double[len];
        m_pdYFiltedR=new double[len];
        for(i=0;i<len;i++){
            m_pdYFiltedF[i]=m_pdYFilted[Math.min(i+m_nWs,len-1)];
            m_pdYFiltedR[i]=m_pdYFilted[Math.max(i-m_nWs,0)];
        }
        m_pdvFiltedY=pdvFiltedY;
        m_dSegmentationCutoff=0.5;
        m_dSmoothnessCutoff=0.3;
        m_nMaxRisingInterval=nMaxRisingInterval;
        m_nNumFiltedY=pdvFiltedY.size();
        m_cvSegmentNodes=new ArrayList();
        segmentizeLine();
        CompleteSegments();
        buildSegmentNodes();
    }
    
    void segmentizeLine(){
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        
        m_pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(m_pdvFiltedY.get(m_nNumFiltedY-1), m_nMaxRisingInterval);
        m_pdDeltaL=CommonStatisticsMethods.getDeltaArray(m_pdvFiltedY.get(m_nNumFiltedY-1), m_pnRisingIntervals, m_nMaxRisingInterval);
        m_pdDeltaS=CommonStatisticsMethods.getDeltaArray(m_pdvFiltedY.get(0), m_pnRisingIntervals, m_nMaxRisingInterval);
        int len=m_pdDeltaL.length,position,minLen=-1,i;//not limitting the length by set minLen=-1
//        CommonMethods.LocalExtrema(m_pdDeltaL, nvLn, nvLx);
        double[] pdDelta=CommonStatisticsMethods.copyArray(m_pdDeltaL);
        for(position=0;position<minLen;position++){
            pdDelta[position]=0;
            pdDelta[pdDelta.length-1-position]=0;
        }
        m_cDeltaRange=CommonStatisticsMethods.getRange(pdDelta);
        
        m_nvTransitionsDownward=new ArrayList();
        m_nvTransitionsUpward=new ArrayList();
        m_cvSegments=new ArrayList();
        
        for(i=0;i<pdDelta.length;i++){
            pdDelta[i]=Math.abs(pdDelta[i]);
        }
        CommonMethods.LocalExtrema(pdDelta, nvLn, nvLx);
        
        m_dMaxDelta=Math.max(Math.abs(m_cDeltaRange.getMin()),Math.abs(m_cDeltaRange.getMax()));
        double delta,cutoff=m_dSmoothnessCutoff*m_dMaxDelta;
        position=0;
        double dt=m_pdDeltaL[position];
        int left=0,right;
        ArrayList<intRange> irs=new ArrayList();
        
        ArrayList<Integer> nv0=nvLx,nv1=nvLn;
        if(nvLx.get(0)>nvLn.get(0)){
            nv0=nvLn;
            nv1=nvLx;
        }
        if(pdDelta[len-1]>pdDelta[len-2]) {
            nv0.add(pdDelta.length-1);            
        }//count the last element as a local maxima
        
        int l0=nv0.size(),l1=nv1.size();
        left=0;
        for(i=0;i<Math.max(l0, l1);i++){
            if(i<l0){            
                right=nv0.get(i);
                if(right<left){
                    i=i;
                }
                if(right-left<minLen) continue;
                delta=m_pdDeltaL[right];
                if(Math.abs(delta)>cutoff){
                    m_cvSegments.add(new intRange(left,right));
                    left=right+m_pnRisingIntervals[right];
                }
            }
//            if(i<l1){
            if(i<-1){//do not check local minimum, now that it is the abs of delta                
                right=nv1.get(i);
                if(right<left){
                    i=i;
                }
                if(right-left<minLen) continue;
                delta=m_pdDeltaL[right];
                if(Math.abs(delta)>cutoff){
                    m_cvSegments.add(new intRange(left,right));
                    left=right+m_pnRisingIntervals[right];
                }
            }
        }
        right=m_pdY.length-1;
        if(right-left>=minLen) m_cvSegments.add(new intRange(left,right));        
    }
    void CompleteSegments(){
        int len=m_cvSegments.size(),i;
        m_cvSegEndRanges=new ArrayList();
        intRange ir;
        for(i=0;i<len;i++){
            m_cvSegEndRanges.add(makeSegment(m_cvSegments.get(i)));
        }
    }
    IntpairCouple makeSegment(intRange ir){
        int i,left,right,nMaxLen=30,minLen=30,len=m_pdDeltaS.length;
        if(ir.getRange()<minLen){
            return new IntpairCouple(new IntPair(ir.getMin(),ir.getMax()),new IntPair(ir.getMin(),ir.getMax()));
        }
        IntPair start, end;
        if(ir.getRange()<nMaxLen) nMaxLen=ir.getRange();
        left=ir.getMin();
        right=Math.min(left+minLen-1,ir.getMax());
        if(right>=len) right=len-1;
        double dt=m_pdDeltaS[right],cutoff=m_dSmoothnessCutoff*m_dMaxDelta;
        right++;
        while(dt<cutoff&&right-left<nMaxLen){
            if(right>=len) break;
            dt=m_pdDeltaS[right];
            right++;
        }
        right--;
        start=new IntPair(left,right);
        
        right=ir.getMax();
        left=Math.max(right-minLen+1, ir.getMin());
        dt=m_pdDeltaS[left];
        left--;
        while(dt<cutoff&&left>=ir.getMin()){
            dt=m_pdDeltaS[left];
            left--;
            if(right-left>nMaxLen) break;
        }
        left++;
        end=new IntPair(left,right);
        return new IntpairCouple(start,end);
    }
    public ArrayList<LineSegmentNode> getSegments(){
        return m_cvSegmentNodes;
    }

    void buildSegmentNodes(){
        IntpairCouple ipc;
        DoubleRange xRange=new DoubleRange();
        PolynomialRegression cprL,cprR;
        intRange ir,irL, irR;
        double dOutliarRatio=0.3,deltaMax=0,delta;
        int i,len=m_cvSegEndRanges.size(),iL,iR;
        LineSegmentNode seg0=null,seg;
        int minLen=5,maxLen0=20,maxLen;
        for(i=0;i<len;i++){
            ipc=m_cvSegEndRanges.get(i);
            
            iL=ipc.first.left;
            if(i==16){
                i=i;
            }
            maxLen=Math.min(maxLen0, ipc.second.right-iL+1);
            cprL=CommonStatisticsMethods.getOptimalStartingRegression(m_pdX, m_pdY,null, iL, minLen, maxLen, -1);
            iR=iL+cprL.getDataSize()-1;
            ipc.first.right=iR;
            irL=new intRange(ipc.first.left,ipc.first.right);
            
            iR=ipc.second.right;
            cprR=CommonStatisticsMethods.getOptimalEndingRegression(m_pdX, m_pdY,null, iR, minLen, maxLen, -1);
            iL=iR-cprR.getDataSize()+1;
            ipc.second.left=iL;
            irR=new intRange(ipc.second.left,ipc.second.right);
            
            seg=new LineSegmentNode(m_pdX,m_pdY,irL,irR,cprL,cprR);
            if(seg0!=null){
                delta=Math.abs(seg0.dEndY-seg.dStartY);
                if(delta>deltaMax){
                    deltaMax=delta;
                }
            }
            m_cvSegmentNodes.add(seg);
            seg0=seg;            
        }
        
        double cutoff=deltaMax*m_dSegmentationCutoff;
        seg0=m_cvSegmentNodes.get(len-1);
        for(i=len-2;i>=0;i--){
            seg=m_cvSegmentNodes.get(i);
            delta=Math.abs(seg.dEndY-seg0.dStartY);
            if(delta<cutoff){
                ir=new intRange(seg.start,seg0.end);
                
                iL=seg.start;
                maxLen=Math.min(maxLen0, seg0.end-seg.start+1);
                cprL=CommonStatisticsMethods.getOptimalStartingRegression(m_pdX, m_pdY,null, iL, minLen, maxLen, -1);
                iR=iL+cprL.getDataSize()-1;
                irL=new intRange(iL,iR);

                iR=seg0.end;
                cprR=CommonStatisticsMethods.getOptimalEndingRegression(m_pdX, m_pdY,null, iR, minLen, maxLen, -1);
                iL=iR-cprR.getDataSize()+1;
                irR=new intRange(iL,iR);            
                seg=new LineSegmentNode(m_pdX,m_pdY,irL,irR,cprL,cprR);
                
                m_cvSegmentNodes.remove(i+1);
                m_cvSegmentNodes.set(i, seg);
            }
            seg0=seg;
        }
    }
 
    void buildSegmentNodes0(){
        IntpairCouple ipc;
        DoubleRange xRange=new DoubleRange();
        PolynomialRegression cprL,cprR;
        intRange ir,irL, irR;
        double dOutliarRatio=0.3,deltaMax=0,delta;
        int i,len=m_cvSegEndRanges.size();
        LineSegmentNode seg0=null,seg;
        for(i=0;i<len;i++){
            ipc=m_cvSegEndRanges.get(i);
            irL=new intRange(ipc.first.left,ipc.first.right);
            xRange.setRange(m_pdX[irL.getMin()], m_pdX[irL.getMax()]);
            if(xRange.getRange()<3){
                i=i;
            }
            cprL=new PolynomialRegression(m_pdX,m_pdYFiltedF,null,xRange,-1,1,null,dOutliarRatio);
            
            irR=new intRange(ipc.second.left,ipc.second.right);
            xRange.setRange(m_pdX[irR.getMin()], m_pdX[irR.getMax()]);
            cprR=new PolynomialRegression(m_pdX,m_pdYFiltedR,null,xRange,-1,1,null,dOutliarRatio);
            
            seg=new LineSegmentNode(m_pdX,m_pdY,irL,irR,cprL,cprR);
            if(seg0!=null){
                delta=Math.abs(seg0.dEndY-seg.dStartY);
                if(delta>deltaMax){
                    deltaMax=delta;
                }
            }
            m_cvSegmentNodes.add(seg);
            seg0=seg;
            
        }
        
        double cutoff=deltaMax*m_dSegmentationCutoff;
        seg0=m_cvSegmentNodes.get(len-1);
        for(i=len-2;i>=0;i--){
            seg=m_cvSegmentNodes.get(i);
            delta=Math.abs(seg.dEndY-seg0.dStartY);
            if(delta<cutoff){
                ir=new intRange(seg.start,seg0.end);
                ipc=makeSegment(ir);
                
                irL=new intRange(ipc.first.left,ipc.first.right);
                xRange.setRange(m_pdX[irL.getMin()], m_pdX[irL.getMax()]);
                cprL=new PolynomialRegression(m_pdX,m_pdYFiltedF,null,xRange,-1,1,null,dOutliarRatio);

                irR=new intRange(ipc.second.left,ipc.second.right);
                xRange.setRange(m_pdX[irR.getMin()], m_pdX[irR.getMax()]);
                cprR=new PolynomialRegression(m_pdX,m_pdYFiltedR,null,xRange,-1,1,null,dOutliarRatio);

                seg=new LineSegmentNode(m_pdX,m_pdY,irL,irR,cprL,cprR);
                m_cvSegmentNodes.remove(i+1);
                m_cvSegmentNodes.set(i, seg);
            }
            seg0=seg;
        }
    }
    ArrayList<LineSegmentNode> getSegmentNodes(){
        return m_cvSegmentNodes;
    }
}
