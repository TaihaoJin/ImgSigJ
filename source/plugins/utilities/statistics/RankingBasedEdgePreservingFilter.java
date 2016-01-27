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
import java.util.ArrayDeque;
import javax.xml.stream.events.StartDocument;

/**
 *
 * @author Taihao
 */
public class RankingBasedEdgePreservingFilter {
    class AdjustNode{
        public int left,right,start,end,peak,base,sign,subPeak,subBase;
        ArrayList<Integer> subpeakExtrema;
        public AdjustNode(){
            subPeak=-1;
            subpeakExtrema=new ArrayList();
        }
        //sign==1 for local maxima and -1 for local minima
        //start and end are the indexes of same sign extrema, indicating the start and the end of the node.
        //the multiplicity of the node equals to end-start+1.
        //left and right the indexes of the oposite sign extrema. The extremum (oposite sign) left is immediately left to the extremum (same sign) start. also right is immediately right to the extremum end.
        //peak is indexe of same sign extremum. Y(peak)*sign>=Y(i)*sign, where Y(i) indicate the value of a same sign extremum whose index is i.
        //base is left or right, Y(base)*sign=max(Y(left)*sign,Y(right)*sign),
        //all oposite sign extrema in the node (between left and right) insure that Y(left)*sign<Y(j) and Y(right)*sign<Y(j).
        //subBase is left or right that is not base. subPeak is the the same sign extrema whose value is 
        //closest to base among all same sign extrema whose values are in between base and subBase
    }
    ArrayList<Integer> m_nvLx,m_nvLn,m_nvLXX,m_nvLNN;
    double[] m_pdX,m_pdY,m_pdYFilted0,m_pdYFilted,m_pdMedianFiltedY;
    ArrayList<AdjustNode> m_cvAdjustNodes;
    boolean[] pbValid,pbModified;
    int[] m_pnLnPositions,m_pnLxPositions;
    public RankingBasedEdgePreservingFilter(double[] pdX, double[] pdY){
        m_pdY=pdY;
        m_pdX=pdX;
        if(m_nvLn==null) m_nvLn=new ArrayList();
        m_nvLn.clear();
        if(m_nvLx==null) m_nvLx=new ArrayList();
        m_nvLx.clear();
        CommonMethods.LocalExtrema(pdY, m_nvLn, m_nvLx);
        m_pnLnPositions=new int[m_pdX.length];
        m_pnLxPositions=new int[m_pdX.length];
        CommonStatisticsMethods.getLocalExtremaPositions(m_pdY,m_nvLn,m_nvLx,m_pnLnPositions,m_pnLxPositions);
        pbValid=CommonStatisticsMethods.getEmptyBooleanArray(pbValid, m_pdX.length);
        CommonStatisticsMethods.setElements(pbValid, true);
    }
    public double[] getTransitionPreservingFiltedData(int ws, int iterations){
        m_pdYFilted=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted, m_pdY.length);
        m_pdYFilted0=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted0, m_pdY.length);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted0);
//        ws=1;
//        iterations=4;
        for(int i=0;i<iterations;i++){
            buildAdjustNodes(ws);
            filterData_AdjustingNode();
            CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdYFilted0);
            ws*=2;
        }
        return m_pdYFilted;
    }
    void filterData_AdjustingNode(){
        int i,len=m_cvAdjustNodes.size();
        AdjustNode adjNode;
        for(i=1;i<len-1;i++){
            adjNode=m_cvAdjustNodes.get(i);
            filterData_AdjustingNode(adjNode);
        }
    }
    void filterData_AdjustingNode(AdjustNode adjNode){//this is after the peak positions have been adjusted
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
        int shift=0;
        int[] pnPeakPositions=m_pnLxPositions;
        if(adjNode.sign<0){
            nv0=m_nvLn;
            nv1=m_nvLx;
            shift=1;
            pnPeakPositions=m_pnLnPositions;
        }
        int index,position,sign=adjNode.sign,peak=adjNode.peak,base=adjNode.base,subBase=adjNode.subBase;
        double dy,dBase=m_pdMedianFiltedY[base],dPeak=m_pdYFilted0[peak],dY0,dY;
          
        int start=adjNode.start,end=adjNode.end;
        if(end==101){
            end=end;
        }
        int leftPeak=pnPeakPositions[start],rightPeak=pnPeakPositions[end];
        if(leftPeak<0){
            if(start==end)
                leftPeak=start;
            else
                leftPeak=nv0.get(pnPeakPositions[start+1]);
        }else
            leftPeak=nv0.get(leftPeak);
        
        if(leftPeak<start||leftPeak>end) leftPeak=start;
        
        if(rightPeak<0){
            if(start==end)
                rightPeak=end;
            else
                rightPeak=nv0.get(pnPeakPositions[end-1]);
        }else
            rightPeak=nv0.get(rightPeak);
        
        if(rightPeak<start||rightPeak>end) rightPeak=end;
//        if(pnPeakPositions[end-1]==rightPeak) rightPeak--;
        double wb=5,wp=1;
        for(position=start;position<=leftPeak;position++){
            dy=m_pdYFilted0[position];
            if(sign*(dy-dBase)<0)continue;
            m_pdYFilted[position]=(wb*dBase+wp*dy)/(wp+wb);
        }
        
        for(position=leftPeak+1;position<rightPeak;position++){
            dy=m_pdYFilted0[position];
            m_pdYFilted[position]=(wb*dBase+wp*dy)/(wp+wb);
        }
        
        for(position=rightPeak;position<=end;position++){
            dy=m_pdYFilted0[position];
            if(sign*(dy-dBase)<0)continue;
            m_pdYFilted[position]=(wb*dBase+wp*dy)/(wp+wb);
        }        
    }
    public void buildAdjustNodes(int ws){
        ArrayList<Integer> nvIndexes=null;
        m_pdMedianFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(m_pdY, ws, ws, nvIndexes);
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(m_pdYFilted0, m_pdMedianFiltedY, 0, m_pdY.length-1, 1);
        int i,j,len=crossingPoints.size(),position0=0,position=0,start,end,peak,sign;
        double dPeak,dy;
        if(m_cvAdjustNodes==null)m_cvAdjustNodes=new ArrayList();
        m_cvAdjustNodes.clear();
        for(i=0;i<len;i++){
            position=crossingPoints.get(i);
            peak=position0;
            dPeak=Math.abs(m_pdYFilted0[position0]-m_pdMedianFiltedY[position0]);
            for(j=position0+1;j<=position;j++){
                dy=Math.abs(m_pdYFilted0[j]-m_pdMedianFiltedY[j]);
                if(dy>dPeak){
                    dPeak=dy;
                    peak=j;
                }
            }
            sign=1;
            if(m_pdY[peak]-m_pdMedianFiltedY[peak]<0) sign=-1;
            AdjustNode adjNode=new AdjustNode();
            adjNode.sign=sign;
            adjNode.start=position0;
            adjNode.end=position;
            adjNode.peak=peak;
            adjNode.subBase=CommonStatisticsMethods.getSmallerElimentIndex(m_pdMedianFiltedY, position, position0,sign);
            adjNode.base=CommonStatisticsMethods.getTheOther(adjNode.subBase,position,position0);
            m_cvAdjustNodes.add(adjNode);
            position0=position+1;
        }
    }
}
