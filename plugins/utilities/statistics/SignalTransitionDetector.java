/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.statistics.LineFitter_AdaptivePolynomial;
import utilities.statistics.LineSegmentRegressionEvaluater;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import utilities.statistics.PolynomialRegression;
import utilities.CommonStatisticsMethods;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
import utilities.Gui.PlotWindowPlus;
import ij.gui.PlotWindow;
import java.awt.Color;
import utilities.CommonGuiMethods;
import utilities.statistics.PiecewisePolynomialLineFitter_ProgressiveSegmenting;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class SignalTransitionDetector {
    PolynomialLineFitter m_cLineFitter;
    PolynomialLineFittingSegmentNode[] m_pcOptimalSegments;
    PolynomialLineFittingSegmentNode[] m_pcOptimalStartingSegments;
    PolynomialLineFittingSegmentNode[] m_pcOptimalEndingSegments;
    PolynomialLineFittingSegmentNode[] m_pcOptimalLongSegments;
    double[] m_pdDeltaSignificance;
    double[] m_pdDeltaMagnitudes;
    double[] m_pdDeltaMagnitudesLR;
    boolean[] m_pbSelectedPoints;
    double[] m_pdX,m_pdY;
    int[] m_pnRisingIntervals;
    int m_nDataSize,m_nMaxRisingInterval,m_nFirstEndingSegPosition,m_nLastStartingSegPosition;
    public SignalTransitionDetector(PolynomialLineFitter cLineFitter){
        m_cLineFitter=cLineFitter;
        m_pcOptimalSegments=m_cLineFitter.getOptimalRegressions();
        m_pcOptimalStartingSegments=m_cLineFitter.getStartingRegressions();
        m_pcOptimalEndingSegments=m_cLineFitter.getEndingRegressions();
        m_pcOptimalLongSegments=m_cLineFitter.getLongRegressions();
        m_pbSelectedPoints=m_cLineFitter.getDataSelection();
        m_nMaxRisingInterval=m_cLineFitter.getMaxRisingInterval();
        m_pdX=m_cLineFitter.getDataX();
        m_pdY=m_cLineFitter.getDataY();
        m_nDataSize=m_pdX.length;
        m_pdDeltaSignificance=new double[m_nDataSize];
        m_pdDeltaMagnitudes=new double[m_nDataSize];
        m_pdDeltaMagnitudesLR=new double[m_nDataSize];
        m_pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(m_pdY, m_nMaxRisingInterval);
        calEndSegPositions();
        calDeltaSignificance();
    }
    void calEndSegPositions(){
        m_nFirstEndingSegPosition=0;
        m_nLastStartingSegPosition=m_nDataSize-1;
        while(m_pcOptimalEndingSegments[m_nFirstEndingSegPosition]==null){
            m_nFirstEndingSegPosition++;
        }
        while(m_pcOptimalStartingSegments[m_nLastStartingSegPosition]==null){
            m_nLastStartingSegPosition--;
        }
    }
    void calDeltaSignificance(){
        PolynomialLineFittingSegmentNode segL,segR,segOL,segOR;
        CommonStatisticsMethods.setElements(m_pdDeltaSignificance, 1.1);
        double xL,xR;
        int index;
        for(index=m_nFirstEndingSegPosition;index<=m_nLastStartingSegPosition-m_nMaxRisingInterval;index++){
            if(index==16){
                index=index;
            }
            xL=m_pdX[index];
            xR=m_pdX[index+m_pnRisingIntervals[index]];
            segL=m_pcOptimalEndingSegments[index];
            segR=m_pcOptimalStartingSegments[index+m_pnRisingIntervals[index]];
            segOL=m_pcOptimalSegments[index];
            segOR=m_pcOptimalSegments[index+m_pnRisingIntervals[index]];
            m_pdDeltaSignificance[index]=LineSegmentRegressionEvaluater.getDifferenceSignificance_ModelComparison(segL, segR);
            if(segOL!=null&&segOR!=null) m_pdDeltaMagnitudes[index]=segOL.predict(xL)-segOR.predict(xR);
            if(segL!=null&&segR!=null) m_pdDeltaMagnitudesLR[index]=segL.predict(xL)-segR.predict(xR);
        }
    }
    public int getOptimalRegressionLine(ArrayList<double[]> pdvXY,ArrayList<Color> colors,String Option){
        PolynomialLineFittingSegmentNode[] segments=null;
        pdvXY.clear();
        if(Option.contentEquals("OptimalRegressionLine")){
            segments=m_pcOptimalSegments;
        } else if (Option.contentEquals("OptimalSegments")) {
            if(!(m_cLineFitter instanceof PiecewisePolynomialLineFitter_ProgressiveSegmenting)) return -1;
            PiecewisePolynomialLineFitter_ProgressiveSegmenting cFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) m_cLineFitter;
            PolynomialLineFittingSegmentNode seg;
            ArrayList<PolynomialLineFittingSegmentNode> cvSegments=cFitter.m_cvFittedSegments_Stored;
            int i,len=cvSegments.size();
            ArrayList<double[]> pdvXYT=new ArrayList();
            for(i=0;i<len;i++){
                seg=cvSegments.get(i);
                if(seg==null) continue;
                LineSegmentRegressionEvaluater.getPredictedValues(seg, pdvXYT);
                pdvXY.add(pdvXYT.get(0));
                pdvXY.add(pdvXYT.get(1));
                if(seg instanceof LineFittingSegmentGroup) colors.add(CommonMethods.randomColor());
            }
            return 1;
        }  else if (Option.contentEquals("MergedSegments")) {
            if(!(m_cLineFitter instanceof PiecewisePolynomialLineFitter_ProgressiveSegmenting)) return -1;
            PiecewisePolynomialLineFitter_ProgressiveSegmenting cFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) m_cLineFitter;
            PolynomialLineFittingSegmentNode seg;
            ArrayList<PolynomialLineFittingSegmentNode> cvSegments=cFitter.m_cvFittedSegments;
            int i,len=cvSegments.size();
            ArrayList<double[]> pdvXYT=new ArrayList();
            for(i=0;i<len;i++){
                seg=cvSegments.get(i);
                if(seg==null) continue;
                LineSegmentRegressionEvaluater.getPredictedValues(seg, pdvXYT);
                pdvXY.add(pdvXYT.get(0));
                pdvXY.add(pdvXYT.get(1));
                if(seg instanceof LineFittingSegmentGroup) colors.add(CommonMethods.randomColor());
            }
            return 1;
        } else if (Option.contentEquals("OptimalStartingRegressionLine"))
            segments=m_pcOptimalStartingSegments;
        else if(Option.contentEquals("OptimalEndingRegressionLine"))
            segments=m_pcOptimalEndingSegments;
        else if(Option.contentEquals("OptimalLongRegressionLine"))
            segments=m_pcOptimalLongSegments;
        else if(Option.contentEquals("OptimalRegressionLineDelta")){
            pdvXY.add(m_pdX);
            pdvXY.add(m_pdDeltaMagnitudes);
            return 1;
        }   else if(Option.contentEquals("OptimalRegressionLineDeltaLR")){
            pdvXY.add(m_pdX);
            pdvXY.add(m_pdDeltaMagnitudesLR);
            return 1;
        } else
            return -1;
        intRange ir=CommonStatisticsMethods.getNonNullElementRange(segments);
        int i,len=ir.getRange();
        double[] pdY=new double[len],pdX=new double[len];
        int iI=ir.getMin(),iF=ir.getMax();
        for(i=iI;i<=iF;i++){
            if(segments[i]==null) continue;
            pdX[i-iI]=m_pdX[i];
            pdY[i-iI]=segments[i].predict(m_pdX[i]);
        }
        pdvXY.add(pdX);
        pdvXY.add(pdY);
        return 1;
    }
    public int drawOptimalRegressionLine(PlotWindowPlus pw, String Option){
        int lw=2,shape=PlotWindow.LINE;
        Color c;
        ArrayList<double[]> pdvXY=new ArrayList();
        ArrayList<Color> colors=new ArrayList();
        getOptimalRegressionLine(pdvXY,colors,Option);
        if(Option.contentEquals("OptimalRegressionLine")){
            c=Color.BLACK;
        } else if (Option.contentEquals("OptimalSegments")){
            c=Color.BLUE;
        }   else if (Option.contentEquals("MergedSegments")){
            c=Color.RED;
        }  else if (Option.contentEquals("OptimalStartingRegressionLine")){
            c=Color.RED;
        } else if (Option.contentEquals("OptimalEndingRegressionLine")){
            c=Color.BLUE;
        } else if (Option.contentEquals("OptimalLongRegressionLine")){
            c=Color.GREEN;
        } else if (Option.contentEquals("OptimalRegressionLineDelta")){
            c=Color.BLUE;
        } else if (Option.contentEquals("OptimalRegressionLineDeltaLR")){
            c=Color.MAGENTA;
        } else
            return -1;
        drawOptimalRegressionLine(pw,Option,lw,shape,c);
        return 1;
    }
    public int drawOptimalRegressionLine(PlotWindowPlus pw, String Option, int lw, int shape, Color c){
        ArrayList<double[]> pdvXY=new ArrayList();
        ArrayList<Color> colors=new ArrayList();
        getOptimalRegressionLine(pdvXY,colors,Option);
        if(pdvXY.isEmpty()) return -1;
        int i,len=pdvXY.size()/2;
        for(i=0;i<len;i++){
            if(i<colors.size()) c=colors.get(i);
            CommonGuiMethods.addPlot(pw, Option, pdvXY.get(i*2), pdvXY.get(2*i+1), lw, shape, c, true);
        }
        return 1;
    }
    public PlotWindowPlus displayDifferenceSignificance(PlotWindowPlus pw){
        String title="LogDiffSignificane";
        int lw=1,shape=PlotWindow.LINE;
        Color c=Color.BLACK;
        double[] pdT=CommonStatisticsMethods.copyLogrithmOfArray(m_pdDeltaSignificance);
        return CommonGuiMethods.addPlot(pw,title,m_pdX,pdT,lw,shape,c,true);
    }
    public void displayTransitions(PlotWindowPlus pw, double pValue){
        double logP=Math.log10(pValue);
        int i,len=m_pdDeltaSignificance.length;
        ArrayList<double[]> pdvXY=new ArrayList();
        int num=0,ir;
        for(i=0;i<len;i++){
            if(m_pdDeltaSignificance[i]>pValue) continue;
            num++;
            ir=i+m_pnRisingIntervals[i];
            if(ir>=len) continue;
            if(m_pcOptimalEndingSegments[i]!=null) CommonGuiMethods.displayLineSegment(pw, m_pcOptimalEndingSegments[i], "TransitionL"+num, 2, PlotWindow.LINE, Color.red);
            if(m_pcOptimalStartingSegments[ir]!=null) CommonGuiMethods.displayLineSegment(pw, m_pcOptimalStartingSegments[ir], "TransitionR"+num, 2, PlotWindow.LINE, Color.blue);
        }
    }
}
