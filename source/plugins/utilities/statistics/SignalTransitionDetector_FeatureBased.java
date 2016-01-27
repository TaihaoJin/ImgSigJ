/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CommonStatisticsMethods;
import java.util.ArrayList;
import utilities.CommonGuiMethods;
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
import utilities.CustomDataTypes.DoubleRange;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class SignalTransitionDetector_FeatureBased {
    double m_dSmoothnessCutoff,m_dSegmentationCutoff,m_dMaxDelta;
    int m_nMaxRisingInterval;
    int[] m_pnRisingIntervals;
    LineFeatureExtracter2 cFeatureExtracter;
    ArrayList<Integer> m_nvTransitionsDownward,m_nvTransitionsUpward;
    ArrayList<intRange> m_cvEnvSegments; //the min and max of a m_cvSmoothLimits element (an intRange) mark the end and begining of the smooth
    ArrayList<Double> m_dvEnvDelta;
    PolynomialLineFittingSegmentNode[] m_pcStartingSegments,m_pcEndingSegments;
    double[] pdX, pdY;
    ArrayList<LineSegmentNode> m_cvSegments;
    
    public SignalTransitionDetector_FeatureBased(LineFeatureExtracter2 cFeatureExtracter){        
        this.cFeatureExtracter=cFeatureExtracter;
        m_nMaxRisingInterval=cFeatureExtracter.nMaxRisingInterval;
        m_dSegmentationCutoff=0.5;
        m_dSmoothnessCutoff=0.3;
        pdX=cFeatureExtracter.pdX;
        pdY=cFeatureExtracter.pdY;
        m_pnRisingIntervals=cFeatureExtracter.m_pnRisingIntervals_Downward;
//        segmentizeEnvLine();
//        detectTransitions();
        detectTransitions_ClusteringBased();
    }
    void segmentizeEnvLine(){
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        m_dvEnvDelta=new ArrayList();
        m_pnRisingIntervals=cFeatureExtracter.m_pnRisingIntervals_Downward;
        double[] pdDelta=cFeatureExtracter.pdDeltaProbeTrail_Downward;
        pdDelta=cFeatureExtracter.pdFiltedEnvDelta_Downward;
        
        int len=pdDelta.length,position,minLen=-1,i;//not limitting the length by set minLen=-1
//        CommonMethods.LocalExtrema(m_pdDeltaL, nvLn, nvLx);
        for(position=0;position<minLen;position++){
            pdDelta[position]=0;
            pdDelta[pdDelta.length-1-position]=0;
        }
        DoubleRange cDeltaRange=CommonStatisticsMethods.getRange(pdDelta);
        
        m_nvTransitionsDownward=new ArrayList();
        m_nvTransitionsUpward=new ArrayList();
        m_cvEnvSegments=new ArrayList();
       
        double[] pdDeltaA=CommonStatisticsMethods.copyArray(pdDelta);
        for(i=0;i<pdDelta.length;i++){
            pdDeltaA[i]=Math.abs(pdDelta[i]);
        }
        CommonMethods.LocalExtrema(pdDeltaA, nvLn, nvLx);
        
        m_dMaxDelta=Math.max(Math.abs(cDeltaRange.getMin()),Math.abs(cDeltaRange.getMax()));
        
        double delta,cutoff=m_dSmoothnessCutoff*m_dMaxDelta;
        position=0;
        
        double dt=pdDelta[position];
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
                delta=pdDelta[right];
                if(Math.abs(delta)>cutoff){
                    m_cvEnvSegments.add(new intRange(left,right));
                    left=right+m_pnRisingIntervals[right];
                    m_dvEnvDelta.add(delta);
                }
            }
//            if(i<l1){
            if(i<-1){//do not check local minimum, now that it is the abs of delta                
                right=nv1.get(i);
                if(right<left){
                    i=i;
                }
                if(right-left<minLen) continue;
                delta=pdDelta[right];
                if(Math.abs(delta)>cutoff){
                    m_cvEnvSegments.add(new intRange(left,right));
                    left=right+m_pnRisingIntervals[right];
                    m_dvEnvDelta.add(delta);
                }
            }
        }
        right=cFeatureExtracter.pdY.length-1;
        m_dvEnvDelta.add(0.);
        if(right-left>=minLen) m_cvEnvSegments.add(new intRange(left,right));        
    }
    void detectTransitions(){
        int i,len=m_cvEnvSegments.size(),l,r,sign,p;
        double[] pdDLR=cFeatureExtracter.pdDeltaOLR;
        PolynomialLineFittingSegmentNode segL,segR;
        m_pcStartingSegments=cFeatureExtracter.m_pcStartingSegments;
        m_pcEndingSegments=cFeatureExtracter.m_pcEndingSegments;
        ArrayList<Integer> nvPeaks=new ArrayList();
        double dMax,delta;
        ArrayList<Double> dvDelta=new ArrayList();
        int minLen=5,maxLen0=20,maxLen;
        double dXL,dXR,dYL,dYR;
        
        intRange irL=m_cvEnvSegments.get(0),irR;
        dMax=0;
        String[] display={"i","xL","xR","lenL","lenR","delEnv","delY0","delYm"};
        ArrayList<String[]> svDisp=new ArrayList();
        svDisp.add(display);
        int lenD=display.length,line=0;;
        intRange ir;
        double deltam;
        for(i=1;i<len;i++){
            display=new String[lenD];
            irR=m_cvEnvSegments.get(i);
            l=irL.getMax();
            r=irR.getMin();
            sign=1;
            if(m_dvEnvDelta.get(i-1)<0) sign=-1;
            p=CommonStatisticsMethods.getFirstExtremumIndex(pdDLR, l, sign, 1);
            nvPeaks.add(p);
            r=p+m_pnRisingIntervals[p];
            
            
            
            dXL=pdX[p];
            dXR=pdX[r];
            segL=m_pcEndingSegments[p];
            if(segL!=null)
                dYL=segL.predict(dXL);
            else
                dYL=cFeatureExtracter.pdYRWMean_Downward[p];
            
            segR=m_pcStartingSegments[r];        
            if(segR!=null)
                dYR=segR.predict(dXR);
            else
                dYR=cFeatureExtracter.pdYRWMean_Downward[r];
            
            delta=dYL-dYR;
            deltam=LineFeatureExtracter2.getModifiedDelta(segL.getRegressionNode(), segR.getRegressionNode(), pdX, cFeatureExtracter.pdYRWMean_Downward, p, r);
//display={"i","xL","xR","lenL","lenR","delEnv","delY0","delYm"};                    
            dvDelta.add(deltam);
            if(deltam>dMax) dMax=deltam;
            line=0;
            display[line]=""+i;
            line++;
            display[line]=PrintAssist.ToString(dXL, 0);
            line++;
            display[line]=PrintAssist.ToString(dXR, 0);
            line++;
            if(segL!=null)
                display[line]=PrintAssist.ToString(segL.getRegressionNode().getDataSize(), 0);
            else
                display[line]="Null";
            line++;
            if(segR!=null)
                display[line]=PrintAssist.ToString(segR.getRegressionNode().getDataSize(), 0);
            else
                display[line]="Null";
            line++;
            display[line]=PrintAssist.ToString(m_dvEnvDelta.get(i-1), 1);
            line++;
            display[line]=PrintAssist.ToString(delta, 2);
            line++;
            display[line]=PrintAssist.ToString(deltam, 2);
            line++;
            irL=irR;
            svDisp.add(display);
        }
        double cutoff=m_dSegmentationCutoff*dMax;
        m_cvSegments=new ArrayList();
        len=nvPeaks.size();
        l=0;
        for(i=0;i<len;i++){
            delta=dvDelta.get(i);
            if(delta<cutoff) continue;
            r=nvPeaks.get(i);
            if(r<l) continue;
            maxLen=Math.min(maxLen0, r-l+1);
            if(maxLen<=0){
                i=i;
            }
            m_cvSegments.add(new LineSegmentNode(pdX,pdY,cFeatureExtracter.pbSelected,l,r,minLen,maxLen,-1));
            l=r+m_pnRisingIntervals[r];
        }
        r=pdX.length-1;
        maxLen=Math.min(maxLen0, r-l+1);
        m_cvSegments.add(new LineSegmentNode(pdX,pdY,cFeatureExtracter.pbSelected,l,r,minLen,maxLen,-1));
        CommonGuiMethods.displayTable("Segment Candidates", svDisp);
    }
    public ArrayList<LineSegmentNode> getSegments(){
        return m_cvSegments;
    }
    void detectTransitions_ClusteringBased(){
        if(m_cvSegments==null)
            m_cvSegments=new ArrayList();
        else
            m_cvSegments.clear();
        
        ArrayList<Integer> hightDeltaPoints=cFeatureExtracter.getDetectedTransitionPointsE();
        int i,len=hightDeltaPoints.size(),p;
        int left=0,right;
        int minLen=5,maxLen=20;
        for(i=0;i<len;i++){
            right=hightDeltaPoints.get(i);
            if(left>right) left=right;
            m_cvSegments.add(new LineSegmentNode(pdX,pdY,cFeatureExtracter.pbSelected,left,right,minLen,maxLen,-1));
            left=right+m_nMaxRisingInterval;
        }
        right=pdX.length-1;
        if(left>right) left=right;
        m_cvSegments.add(new LineSegmentNode(pdX,pdY,cFeatureExtracter.pbSelected,left,right,minLen,maxLen,-1));
    }
}
