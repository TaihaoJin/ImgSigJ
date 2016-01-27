/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import ij.gui.PlotWindow;
import java.awt.event.MouseListener;
import java.util.StringTokenizer;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.awt.Color;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.PointRoi;
import utilities.CommonGuiMethods;
import java.awt.Point;
import utilities.Gui.PlotWindowTuner;
import ij.gui.Plot;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.Gui.PlotWindowPlus;
import utilities.statistics.PolynomialRegression;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.statistics.MeanSem1;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import utilities.statistics.LineFitter_AdaptivePolynomial;
import utilities.io.PrintAssist;
import utilities.Gui.TableFrame;
import utilities.statistics.SignalTransitionDetector;
import utilities.statistics.PiecewisePolynomialLineFitter_ProgressiveSegmenting;
import utilities.statistics.LineSegmentRegressionEvaluater;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.SpikeHandler;
import utilities.statistics.ProbingBall;
import utilities.statistics.EdgePreservingFilter;
import utilities.statistics.RankingBasedEdgePreservingFilter;
import utilities.statistics.EdgePreservingFilter_Comprehensive;
import utilities.Non_LinearFitting.ModePars;
import utilities.statistics.LineFeatureExtracter2;
import utilities.statistics.PolynomialLineFitter;

/**
 *
 * @author Taihao
 */
public class IPOGTrackPlotWindow {
    ArrayList<PlotWindowPlus> cvTrackPlots;
    ArrayList<Integer> nvRegressionLines;
    ArrayList<Integer> nvLn,nvLx;
    ArrayList<intRange> cvExcessiveDeltaRanges;
    public PlotWindowPlus pw;
    public IPOGTrackNode IPOGT;
    public String plotOption;
    int sliceI,sliceF;
    double[] pdX,pdY,pdSD,pdSDSelected;
    double[] pdXOriginal,pdYOriginal;
    double[] pdTiltingSig,pdPWDevSig,pdSidenessSig;
    public ArrayList<Color> m_cvPlotColors;
    boolean[] pbSelected;
    int nMaxRisingInterval;
//    PiecewisePolynomialLineFitter_ProgressiveSegmenting m_cTrackFitter;
    PolynomialLineFitter m_cTrackFitter;
    PolynomialLineFittingSegmentNode[] m_pcOptimalSegments,m_pcStartingSegments,m_pcEndingSegments,m_pcLongSegments;
    SignalTransitionDetector m_cTransitionDetector;
    double dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals;
    double[] pdFiltedY;
    int nWsSD;
    int[] m_pnLnPositions,m_pnLxPositions;
    String m_sImageID;
    ArrayList<intRange> cvSpikeRanges;

    public IPOGTrackPlotWindow(){
        nvLx=new ArrayList();
        nvLn=new ArrayList();
        cvTrackPlots=new ArrayList();
        nvRegressionLines=new ArrayList();

        m_cvPlotColors=new ArrayList();

        m_cvPlotColors.add(Color.red);
        m_cvPlotColors.add(Color.blue);
        m_cvPlotColors.add(Color.green);
        m_cvPlotColors.add(Color.magenta);
        m_cvPlotColors.add(Color.orange);
        m_cvPlotColors.add(Color.pink);
        dPChiSQ=0.05;
        dPTilting=0.01;
        dPSideness=0.01;
        dPPWDev=0.001;
        dPTerminals=0.01;
        cvExcessiveDeltaRanges=new ArrayList();
        nWsSD=20;
        m_sImageID="";
    }

    public void addPlot(String title, double[] pdX, double[] pdY, int lw, int shape){
        pw.addPlot(title, pdX, pdY, lw, shape);
    }

    public void segImageID(String ImageID){
        m_sImageID=ImageID;
    }

    public Color getNewPlotColor(int plots){
        int colors=m_cvPlotColors.size();;
        if(plots<colors) return m_cvPlotColors.get(plots);
        return CommonMethods.randomColor();
    }
    IPOGTrackPlotWindow(IPOGTrackNode IPOGT, String plotOption){
        this();
        this.IPOGT=IPOGT;
        this.plotOption=plotOption;
        sliceI=IPOGT.firstSlice;
        sliceF=IPOGT.lastSlice;
        drawTrack(IPOGT);
    }
    IPOGTrackPlotWindow(IPOGTrackNode IPOGT, String plotOption, int nMaxRisingInterval, int sliceI,int sliceF){
        this();
        this.nMaxRisingInterval=nMaxRisingInterval;
        this.IPOGT=IPOGT;
        this.plotOption=plotOption;
        this.sliceI=Math.max(IPOGT.firstSlice,sliceI);
        this.sliceF=Math.min(IPOGT.lastSlice,sliceF);
        drawTrack(IPOGT);
    }
    public void drawTrack(IPOGTrackNode IPOGT){
//        pw=IPOGT.plotAmp(plotOption,sliceI,sliceF);
        ArrayList<Double> dvX=new ArrayList(),dvY=new ArrayList();
        IPOGT.getTrackData(dvX, dvY, plotOption);
        int i,len=dvX.size();
        if(len<1){
            len=len;
        }
        pdY=new double[len];
        pdX=new double[len];
        pbSelected=new boolean[len];
        CommonStatisticsMethods.setElements(pbSelected, true);
        for(i=0;i<len;i++){
            pdX[i]=dvX.get(i);
            pdY[i]=dvY.get(i);
        }
        pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdY, nMaxRisingInterval, nWsSD);
        pdXOriginal=pdX;
        pdYOriginal=pdY;
        CommonMethods.LocalExtrema(dvY, nvLn, nvLx);
        markLocalExtremaPositions();
//        svAdditionalPlotTitles.add("mainData");//will be always there
//        pdvAdditionalPlotData.add(pdY);    public PlotWindowPlus(float[] pfX, float[] pfY, String plotTitle, String xTitle, String yTitle, Color c){

        pw=new PlotWindowPlus(pdX,pdY,"Track"+IPOGT.TrackIndex,"Slice",plotOption,1,PlotWindow.LINE,Color.BLACK);
        for(i=0;i<cvTrackPlots.size();i++){
            cvTrackPlots.get(i).setProtection(false);
        }
        pw.setProtection(true);
        cvTrackPlots.add(pw);
        pw.draw();
        pw.setVisible(false);
        
    }
    public void clearTrackPlotProtections(){
        for(int i=0;i<cvTrackPlots.size();i++){
            cvTrackPlots.get(i).setProtection(false);
        }
    }
    public void addMouseListener(MouseListener ml){
        pw.getCanvas().addMouseListener(ml);
    }
    public int highlighTrackCurve(int slice){
        IPOGaussianNode IPOG=IPOGT.getIPOG(slice);
        if(IPOG==null) return -1;
        Point position=pw.getPixelCoordinates(slice, IPOG.getValue(plotOption));
        if(position==null){
            slice=slice;
        }
        return 1;
    }
    public String getCoordinates(){
        return pw.getCoordinate();
    }
    Point getCursorLocation(){
        return pw.getMousePosition();
    }
    public IPOGaussianNode getCurrentIPO(){
        String coor=pw.getCoordinate();
        StringTokenizer stk=new StringTokenizer(coor,"X=Y(,)");
        if(!stk.hasMoreElements())return null;
        int sliceIndex=(int)(Double.parseDouble(stk.nextToken())+0.5);
        int index=sliceIndex-IPOGT.m_cvIPOGs.get(0).sliceIndex;
        if(index<0) index=0;
        if(index>=IPOGT.m_cvIPOGs.size()) index=IPOGT.m_cvIPOGs.size()-1;
        return IPOGT.m_cvIPOGs.get(index);
    }
    public static IPOGaussianNode getCurrentIPO(ArrayList<IPOGTrackPlotWindow> cvIPOGTPWs,MouseEvent me){
        Object src=me.getSource();
        int i,len=cvIPOGTPWs.size();
        IPOGTrackPlotWindow srcpw=null;
        for(i=0;i<len;i++){
            srcpw=cvIPOGTPWs.get(i);
            if(srcpw.pw.getCanvas()==src) break;
            srcpw=null;
        }
        if(srcpw==null) return null;
        return srcpw.getCurrentIPO();
    }
    public static IPOGTrackNode getCurrentIPOT(ArrayList<IPOGTrackPlotWindow> cvIPOGTPWs,MouseEvent me){
        IPOGTrackPlotWindow srcpw=getCurrentIPOGTPW(cvIPOGTPWs, me);
        if(srcpw==null) return null;
        return srcpw.IPOGT;
    }
    public static IPOGTrackPlotWindow getCurrentIPOGTPW(ArrayList<IPOGTrackPlotWindow> cvIPOGTPWs,MouseEvent me){
        Object src=me.getSource();
        int i,len=cvIPOGTPWs.size();
        IPOGTrackPlotWindow srcpw=null;
        for(i=0;i<len;i++){
            srcpw=cvIPOGTPWs.get(i);
            if(srcpw.pw.getCanvas()==src) break;
            srcpw=null;
        }
        return srcpw;
    }
    public static IPOGTrackPlotWindow getCurrentIPOGTPW(ArrayList<IPOGTrackPlotWindow> cvIPOGTPWs,IPOGTrackNode IPOGT){
        int i,len=cvIPOGTPWs.size();
        IPOGTrackPlotWindow srcpw=null;
        for(i=0;i<len;i++){
            srcpw=cvIPOGTPWs.get(i);
            if(srcpw.IPOGT==IPOGT) break;
            srcpw=null;
        }
        return srcpw;
    }
    public static ArrayList<IPOGTrackPlotWindow> getAssociatedOpenIPOGTPW(ArrayList<IPOGTrackPlotWindow> cvIPOGTPWs,IPOGTrackNode IPOGT){
        int i,len=cvIPOGTPWs.size();
        IPOGTrackPlotWindow srcpw=null;
        ArrayList <IPOGTrackPlotWindow> IPOGTPWs=new ArrayList();
        for(i=len-1;i>=0;i--){
            srcpw=cvIPOGTPWs.get(i);
            if(srcpw.IPOGT==IPOGT)
                IPOGTPWs.add(srcpw);
            else if(!srcpw.pw.isVisible())
            {
                cvIPOGTPWs.remove(i);
                continue;
            }
        }
        return IPOGTPWs;
    }
    public static int highlightAssociatedTrackPlots(ArrayList<IPOGTrackPlotWindow> cvIPOGTPWs,IPOGTrackNode IPOGT,int slice){
        ArrayList<IPOGTrackPlotWindow> IPOGTPWs=getAssociatedOpenIPOGTPW(cvIPOGTPWs,IPOGT);
        int i,len=IPOGTPWs.size();
        for(i=0;i<len;i++){
            IPOGTPWs.get(i).highlighTrackCurve(slice);
        }
        return 1;
    }
    public int highlightCurrentDataPoint(int x, int y, Color c){
        ImagePlus im=pw.getImagePlus();
        CommonGuiMethods.highlightPoint(im, new Point(x,y), c);
        return 1;
    }
    public int setLimit(int sI, int sF){
        if(sI<sliceI) sI=sliceI;
        if(sF>sliceF) sF=sliceF;
        pw.setLimit(sI, sF);
        return 1;
    }
    void zoomOut(){
        int factor=4;
        DoubleRange xRange=new DoubleRange(), yRange=new DoubleRange();
        pw.getDisplayingRange(xRange, yRange);
        int nL=(int)(xRange.getMin()+0.5), nR=(int)(xRange.getMax());
        int len0=nR-nL+1,len=factor*len0;
        int sM=(nL+nR)/2;
        int sI=Math.max(sliceI, sM-len/2),sF=Math.min(sliceF, sM+len/2);
        PolynomialLineFittingSegmentNode seg;
        setLimit(sI,sF);
    }
    public void calSelectedSD(){
        double[] pdXR=pdX,pdYR=pdY;
        int nActive=pw.getActiveCurveIndex();
        if(nActive>=0){
            pdXR=pw.getXValues(nActive);
            pdYR=pw.getYValues(nActive);
        }
        int[] pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdYR, nMaxRisingInterval);
        pdSDSelected=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdXR, pdYR, pnRisingIntervals, pbSelected, nWsSD);
    }
    ArrayList<double[]> getActiveData(){
        ArrayList<double[]> activeData=new ArrayList();
        double[] pdXR=pdX,pdYR=pdY;
        int nActive=pw.getActiveCurveIndex();
        if(nActive>=0){
            pdXR=pw.getXValues(nActive);
            pdYR=pw.getYValues(nActive);
        }
        activeData.add(pdXR);
        activeData.add(pdYR);
        return activeData;
    }
    public PolynomialLineFittingSegmentNode showRegression(int order,int modes, double dPChiSQ,double dPTilting,double dpSideness, double dPPWDev, int nWsTilting, int nWsPWDev, double dPTerminals, double dOutliarRatio, String SDOption){
//        double sd=CommonStatisticsMethods.getSD_DeltabasedOutliarExcludingMeanSem(this.pdY,IPOGT.m_nRisingInterval,pValue);
        double[] pdXR=pdX,pdYR=pdY;
        boolean[] pbSelected=this.pbSelected;
        int nActive=pw.getActiveCurveIndex();
        if(nActive>0){
            pdXR=pw.getXValues(nActive);
            pdYR=pw.getYValues(nActive);
            pbSelected=CommonStatisticsMethods.getSelection(pdXR,pdX,this.pbSelected);
        }
        int i,len=pdXR.length,j;
        intRange ir=CommonStatisticsMethods.getSelectedRange(pbSelected,true);
        pdSidenessSig=CommonStatisticsMethods.getEmptyDoubleArray(pdSidenessSig, len);
        pdTiltingSig=CommonStatisticsMethods.getEmptyDoubleArray(pdTiltingSig, len);
        pdPWDevSig=CommonStatisticsMethods.getEmptyDoubleArray(pdPWDevSig, len);
        boolean pbValidDev[]=new boolean[len];
        this.dPTerminals=dPTerminals;
        double[] pdSD=null;
//        if(pdSDSelected==null) 
        if(SDOption.contentEquals("Local"))            
            pdSD=null;
        else if(SDOption.contentEquals("Signal")){
            double[] pdSDt=LineFeatureExtracter2.calSD(pdXOriginal, pdYOriginal, this.pbSelected, nMaxRisingInterval, nWsSD);
            pdSD=CommonStatisticsMethods.getLinearInterpolation(pdXOriginal, pdSDt,pdXR);
        } else if(SDOption.contentEquals("Active Curve")){
            ArrayList<double[]> activeData=getActiveData();
            ArrayList<double[]> pdvMeanSD=CommonStatisticsMethods.calRWSD_DevBased(pdXR, pdYR, nMaxRisingInterval, nWsSD, 0.01);
//            pdSD=LineFeatureExtracter2.calSD(activeData.get(0), activeData.get(1), pbSelected, nMaxRisingInterval, nWsSD);
            pdSD=pdvMeanSD.get(1);
        } else if(SDOption.contentEquals("Input Number")){
            String sSd=CommonGuiMethods.getOneTextInput("Input S. D.", "S. D.", "10.0");
            pdSD=new double[pdXR.length];
            CommonStatisticsMethods.setElements(pdSD, Double.parseDouble(sSd));
        }
        PolynomialLineFittingSegmentNode seg=new PolynomialLineFittingSegmentNode(pdXR,pdYR,pdTiltingSig,pdSidenessSig,pdPWDevSig,pdSD,pbSelected,pbValidDev,m_pnLnPositions,m_pnLxPositions,ir.getMin(),ir.getMax(),order,modes,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals, dOutliarRatio);
//        CommonGuiMethods.displayNewPlotWindowPlus("RWSD", pdXR, pdSD, 1, 2, Color.BLACK);
        PolynomialRegression sr=seg.getRegressionNode();
        double[] pdXT=sr.getFittedDataX();
        len=pdXT.length;
        double[] pdYT=new double[len];
        int nModes=sr.getModes();
//        if(nModes==FittingModelNode.CentralMode) nModes=3;
        ArrayList<double[]> pdvYTs=new ArrayList();
        
        for(i=0;i<len;i++){
            pdYT[i]=sr.predict(pdXT[i]);
        }
        
        if(nModes>1){
            ModePars cMP=sr.getModePars();
            int index=cMP.nMainComponent;
            double[] pdMeans=cMP.pdMeans;
            for(i=0;i<nModes-1;i++){
                pdvYTs.add(new double[len]);
            }
        
            int num=0;
            for(i=0;i<len;i++){
                num=0;
                for(j=0;j<nModes;j++){
                    if(j==index)
                        pdYT[i]+=pdMeans[j];
                    else{
                        pdvYTs.get(num)[i]=pdYT[i]+pdMeans[j];
                        num++;
                    }                
                }
            }
        }
        
        int curveIndex=pw.getNumCurves();
        nvRegressionLines.add(curveIndex);
        Color c=getNewPlotColor(nvRegressionLines.size());
        pw.addPlot("RegressLine"+sliceI+"to"+sliceF,pdXT,pdYT,2,Plot.LINE,c);
        for(j=0;j<nModes-1;j++){
            curveIndex=pw.getNumCurves();
            nvRegressionLines.add(curveIndex);
            pw.addPlot("RegressLine"+sliceI+"to"+sliceF,pdXT,pdvYTs.get(j),1,Plot.LINE,c);
        }
        sr.showRWASignificance();
        LineSegmentRegressionEvaluater.markExcludedPoints(seg);

        pdXT=CommonStatisticsMethods.copyArrayToArray(pdXR,pbSelected, ir.getMin(),ir.getMax(),1);
        pdYT=CommonStatisticsMethods.copyArrayToArray(pdTiltingSig,pbSelected, ir.getMin(),ir.getMax(),1);
//        CommonGuiMethods.showPValuesLog10("SigTilting",pdXT,pdYT);
//        CommonGuiMethods.displayNewPlotWindowPlus("SigTilting", pdXT, pdYT, 1, 2, Color.black);
        
        pdYT=CommonStatisticsMethods.copyArrayToArray(pdSidenessSig,pbSelected, ir.getMin(),ir.getMax(),1);
//        CommonGuiMethods.showPValuesLog10("SigSideness",pdXT,pdYT);
//        CommonGuiMethods.displayNewPlotWindowPlus("SigSideness", pdXT, pdYT, 1, 2, Color.black);
        return seg;
    }
    public void clearRegressions(){
        pw.removePlotGroup("Regress");
    }
    int getIndex(int slice){
        if(slice<sliceI||slice>sliceF) return -1;
        int i,len=pdX.length,index;
        for(i=0;i<len;i++){
            if(slice==(int)(pdX[i]+0.5)) return i;
        }
        return -1;
    }
    public void showPlotData(){
        pw.showPlotData();
    }
    public void setRisingInterval(int interval){
        nMaxRisingInterval=interval;
    }
    public void pickLocalMinima(){
        CommonStatisticsMethods.setElements(pbSelected, false);
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        int i,len=nvLn.size();
        for(i=0;i<len;i++){
            pbSelected[nvLn.get(i)]=true;
        }
    }
    public void pickLocalMaxima(){
        CommonStatisticsMethods.setElements(pbSelected,false);
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        int i,len=nvLx.size();
        for(i=0;i<len;i++){
            pbSelected[nvLx.get(i)]=true;
        }
    }
    public void excludeExcessiveDeltaPoints(double pValue){
        int i,j,len;
        len=cvExcessiveDeltaRanges.size();
        markExcessiveDeltaPoints(pValue);
        intRange ir;
        for(i=0;i<len;i++){
            ir=cvExcessiveDeltaRanges.get(i);
            for(j=ir.getMin();j<=ir.getMax();j++){
                pbSelected[j]=false;
            }
        }
    }
    public void markExcessiveDeltaPoints(double pValue){
        CommonStatisticsMethods.markExcessiveDeltaPoints(pdY, pdSD,Math.sqrt(2), cvExcessiveDeltaRanges, nMaxRisingInterval, pValue);
    }
    public void highlightExcessiveDelta(Color c){
        int lw=2,shape=PlotWindow.LINE,i,len=cvExcessiveDeltaRanges.size(),len1;
        double[] pXT,pYT;
        intRange ir;
        for(i=0;i<len;i++){
            ir=cvExcessiveDeltaRanges.get(i);
            pXT=CommonStatisticsMethods.copyArrayToArray(pdX, ir.getMin(), ir.getMax(), 1);
            pYT=CommonStatisticsMethods.copyArrayToArray(pdY, ir.getMin(), ir.getMax(), 1);
            pw.addPlot("Excessive Delta"+i, pXT, pYT, lw, shape, c, false);
        }
        pw.refreshPlot();
    }
    public void highlightSpikes(Color c){
        int lw=2,shape=PlotWindow.LINE,i,len=cvSpikeRanges.size(),len1;
        double[] pXT,pYT;
        intRange ir;
        for(i=0;i<len;i++){
            ir=cvSpikeRanges.get(i);
            pXT=CommonStatisticsMethods.copyArrayToArray(pdX, ir.getMin(), ir.getMax(), 1);
            pYT=CommonStatisticsMethods.copyArrayToArray(pdY, ir.getMin(), ir.getMax(), 1);
            pw.addPlot("Spikes"+i, pXT, pYT, lw, shape, c, false);
        }
        pw.refreshPlot();
    }
    public void includeExcessiveDeltaPoints(){
        int i,j,len=cvExcessiveDeltaRanges.size();
        intRange ir;
        for(i=0;i<len;i++){
            ir=cvExcessiveDeltaRanges.get(i);
            for(j=ir.getMin();j<=ir.getMax();j++){
                pbSelected[j]=true;
            }
        }
        cvExcessiveDeltaRanges.clear();
    }
    public void removeExcessiveDeltaHighlights(){
        pw.removePlotGroup("Excessive");
    }
    public void removeSpikeHighlights(){
        pw.removePlotGroup("Spikes");
    }
    public double[] getDataX(){
        return pdX;
    }
    public double[] getDataY(){
        return pdY;
    }
//   public PiecewisePolynomialLineFitter_ProgressiveSegmenting fitTrackSignal(String sDataSelectionOption, int order, double pPDelta,double dPChiSQ,double dPTilting,double dPSideness, double dPPWDev,double dOutliarRatio, boolean defaultCompletion){
   public PolynomialLineFitter fitTrackSignal(String sFitterOption, String sDataSelectionOption, int order, double pPDelta,double dPChiSQ,double dPTilting,double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio, boolean defaultCompletion){
        if(sFitterOption==null) sFitterOption="Progressive Segmentation";
        int nMaxSegLen=30;
        int len=pdX.length,i,j;
        int nWsTilting=3,nWsPWDev=0;
        this.dPChiSQ=dPChiSQ;
        this.dPPWDev=dPPWDev;
        this.dPTilting=dPTilting;
        this.dPSideness=dPSideness;
        this.dPTerminals=dPTerminals;
       
        if(sFitterOption.contentEquals("Progressive Segmentation"))
            m_cTrackFitter=new PiecewisePolynomialLineFitter_ProgressiveSegmenting(m_sImageID+"_Track"+IPOGT.TrackIndex+"_"+plotOption,pdX,pdY,pdSD,cvExcessiveDeltaRanges,pbSelected,nMaxRisingInterval,order,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dOutliarRatio,defaultCompletion);
        else if(sFitterOption.contentEquals("Adptive Polynomial"))
            m_cTrackFitter=new LineFitter_AdaptivePolynomial(pdX,pdY,pbSelected,nMaxRisingInterval,order,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals,dOutliarRatio);
//     public LineFitter_AdaptivePolynomial(double[] pdX, double[] pdY, boolean[] pbSelected, int nRisingInterval, int nOrder, int maxSegLen, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness, double dPPWDev,double dOutliarRatio){
        
        m_pcOptimalSegments=m_cTrackFitter.getOptimalRegressions();
        m_pcStartingSegments=m_cTrackFitter.getStartingRegressions();
        m_pcEndingSegments=m_cTrackFitter.getEndingRegressions();
        m_pcLongSegments=m_cTrackFitter.getLongRegressions();
        return m_cTrackFitter;
    }
    void detectTrackSignalTransitions(String sFitterOption, String sDataSelectionOption, int order, double dPDelta, double dPTransition,double dOutliarRatio){
        if(m_cTrackFitter==null) fitTrackSignal(sFitterOption, sDataSelectionOption, order, dPDelta,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals,dOutliarRatio,true);
        m_cTransitionDetector=new SignalTransitionDetector(m_cTrackFitter);
    }
    void exportLinearSegLength(TableFrame tableFrame, int order, double dPDelta){//need to fix it for position dependent rising interval, but this method is obsolet.
        if(m_cTrackFitter==null) fitTrackSignal(null,"AllPoints",order,dPDelta,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals,0,true);
        int len=pdX.length,i,j;
        PolynomialLineFittingSegmentNode[] pcSegmentsO=new PolynomialLineFittingSegmentNode[len],pcSegmentsL=new PolynomialLineFittingSegmentNode[len],pcSegmentsR=new PolynomialLineFittingSegmentNode[len];
        String[] ColumnHead={"Slice","Optimal LS","Ending LS","Y","Starting LS","DeltaO","DeltaLR","LeftStart","RightEnd"};
        String[][] psData=new String[len][ColumnHead.length];
        int iR;
        intRange ir=null,irR=null;
        double x,xR;
        for(i=0;i<len;i++){
            iR=Math.min(len-1,i+nMaxRisingInterval);
            x=pdX[i];
            xR=pdX[iR];
            psData[i][0]=PrintAssist.ToString(pdX[i],0);
            if(m_pcOptimalSegments[i]!=null)psData[i][1]=PrintAssist.ToString(m_pcOptimalSegments[i].predict(x),1);
            if(m_pcEndingSegments[i]!=null)psData[i][2]=PrintAssist.ToString(m_pcEndingSegments[i].predict(x),1);
            psData[i][3]=PrintAssist.ToString(pdY[i],1);
            if(m_pcStartingSegments[iR]!=null)psData[i][4]=PrintAssist.ToString(m_pcStartingSegments[iR].predict(xR),1);
            if(m_pcOptimalSegments[i]!=null&&m_pcStartingSegments[iR]!=null)psData[i][5]=PrintAssist.ToString(m_pcOptimalSegments[i].predict(x)-m_pcStartingSegments[iR].predict(xR),1);
            if(m_pcEndingSegments[i]!=null&&m_pcStartingSegments[iR]!=null)psData[i][6]=PrintAssist.ToString(m_pcEndingSegments[i].predict(x)-m_pcStartingSegments[iR].predict(xR),1);
            if(m_pcOptimalSegments[i]!=null)ir=m_pcOptimalSegments[i].getRange();
            if(m_pcOptimalSegments[iR]!=null)irR=m_pcOptimalSegments[iR].getRange();
            if(irR==null||ir==null){
                irR=irR;
                continue;
            }
            psData[i][7]=PrintAssist.ToString(pdX[ir.getMin()],0);
            psData[i][8]=PrintAssist.ToString(pdX[irR.getMax()],0);
        }

        if(tableFrame==null)
            tableFrame=new TableFrame("Regression Lines",ColumnHead,psData,0,len-1);
        else
            tableFrame.update("Le", ColumnHead, psData, 0, psData.length-1);
    }
    public PolynomialLineFitter getTrackFitter(){
        return m_cTrackFitter;
    }
    public int getXIndex(double x){
        int nX=(int)(x+0.5);
        if(nX<sliceI) nX=sliceI;
//        if(nX>sliceF) nX=sliceF;
        int index=nX-sliceI;
        if(index>=pdX.length) index=pdX.length-1;
        int xt=(int)(pdX[index]+0.5);
        while(xt>nX){//because of gaps in the track
            index--;
            xt=(int)(pdX[index]+0.5);
        }
        return index;
    }
    public SignalTransitionDetector getSignalTransitionDetector(){
        return m_cTransitionDetector;
    }
    public void highlightSelectedDataPoints(Color c){
        pw.highlightDataSelection(pdX,pbSelected, c);
    }
    public boolean[] getDataSelection(){
        return pbSelected;
    }
    public ArrayList<Integer> getLocalMinima(){
        return nvLn;
    }
    public ArrayList<Integer> getLocalMaxima(){
        return nvLx;
    }
    void markLocalExtremaPositions(){//index=m_pnLxPositions[i], index is the index of the local maximum at the immediate
        //right side of i. i is the index of a data point
        //for the point in the right side of the largest local extremum, in give the index eaqual the size of the m_nvLn, so need to check before getting the index
        m_pnLxPositions=new int[pdX.length];
        m_pnLnPositions=new int[pdX.length];
        CommonStatisticsMethods.setElements(m_pnLxPositions,-1);
        CommonStatisticsMethods.setElements(m_pnLnPositions,-1);
        int i,index,len=nvLx.size(),j;
        int index0=0;
        for(i=0;i<len;i++){
            index=nvLx.get(i);
            for(j=index0;j<index;j++){
                m_pnLxPositions[j]=i;
            }
            index0=index;
        }

        for(i=index0;i<pdX.length;i++){
            m_pnLxPositions[i]=len;
        }

        len=nvLn.size();
        index0=0;
        for(i=0;i<len;i++){
            index=nvLn.get(i);
            for(j=index0;j<index;j++){
                m_pnLnPositions[j]=i;
            }
            index0=index;
        }
        for(i=index0;i<pdX.length;i++){
            m_pnLnPositions[i]=len;
        }
    }
    public void setImageID(String ImageID){
        m_sImageID=ImageID;
    }
    public void markSpikes(double dPValue,int nMultiplicity){
        SpikeHandler detector=new SpikeHandler(pdX,pdY,nMultiplicity,dPValue);
        cvSpikeRanges=detector.getSpikeRanges();
    }
    public void filtTrackData_Median(Color c){
        int ws=5;
        ArrayList<String> svLabels=new ArrayList(), svTexts=new ArrayList(),inputs;
        String title="Running Window Size";
        svLabels.add("Half Window Size");
        svTexts.add("5");
        inputs=CommonGuiMethods.inputTexts(title, svLabels, svTexts);
        ws=Integer.parseInt(inputs.get(0));
        double[] pdYFilted0=CommonStatisticsMethods.copyArray(pdY);
        ArrayList<int[]> pnvIndexes=new ArrayList();
        int iterations=2,i,j,p,index;
//        ws=1;
//        if(true){//12d31 to see the effects of probe contacting maxima selection
            double dRx=5;
            int nRanking=0;
            ProbingBall pb=new ProbingBall(pdX,pdY,dRx,-1,nRanking);
            ArrayList<Integer> nv=pb.getProbeContactingPositions(pb.Downward);
            int len=pdX.length,len1=nv.size();
            boolean[] pbt=new boolean[len];
            CommonStatisticsMethods.setElements(pbt, false);
            for(i=0;i<len1;i++){
                p=nv.get(i);
                if(!CommonStatisticsMethods.isLocalExtrema(pdY, p, 1)) continue;
                pbt[p]=true;
            }
//        }
        double[] trail=pb.getProbeTrail(pb.Downward);
//        trail=CommonStatisticsMethods.getExtremaLine(pdY);
        int[] pnIndexes=new int[len],pnIndexes0=new int[len];
        CommonStatisticsMethods.copyArray(trail, pdYFilted0);
//        CommonStatisticsMethods.copyArray(pdY, pdYFilted0);
        PlotWindowPlus pwt=CommonGuiMethods.displayNewPlotWindowPlus("Trail and median filters", pdX, pdYFilted0, 1, 2, Color.BLACK);
        
        for(i=0;i<len;i++){
            pnIndexes0[i]=i;
        }
        for(i=0;i<iterations;i++){
//            pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(pdYFilted0,pbt,ws,ws,null);
//            pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(trail,ws,ws,null);
            pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(pdYFilted0,ws,ws,pnIndexes);
            
            int[] pnIndexest=new int[len];
            for(j=0;j<len;j++){
                index=pnIndexes[j];
                pnIndexest[j]=pnIndexes0[index];
                pdFiltedY[j]=pdYFilted0[index];
            }
            pnIndexes0=pnIndexest;
            
            if(iterations>0) {
                c=CommonGuiMethods.getDefaultColor(i+1);
            }
            pw.addPlot("Median Filter WS="+ws, pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
            pwt.addPlot("Median Filter WS="+ws, pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
            CommonStatisticsMethods.copyArray(pdFiltedY, pdYFilted0);//not good to filt the filtered data of the previous iterations.
            ws*=3;
        }
//        pw.addPlot("Median Filter", pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
    }
    public void filtTrackData_Mean(Color c){
        int ws=5;
        ArrayList<String> svLabels=new ArrayList(), svTexts=new ArrayList(),inputs;
        String title="Running Window Size";
        svLabels.add("Half Window Size");
        svTexts.add("5");
        inputs=CommonGuiMethods.inputTexts(title, svLabels, svTexts);
        ws=Integer.parseInt(inputs.get(0));
        double[] pdYFilted0=CommonStatisticsMethods.copyArray(pdY);
        int iterations=1,i;
        
        for(i=0;i<iterations;i++){
            pdFiltedY=CommonStatisticsMethods.getRunningWindowAverage(pdYFilted0, 0,pdY.length-1, ws, true);
            if(iterations>0) {
                c=CommonGuiMethods.getDefaultColor(i+1);
            }
            pw.addPlot("Median Filter WS="+ws, pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
            CommonStatisticsMethods.copyArray(pdFiltedY, pdYFilted0);//not good to filt the filtered data of the previous iterations.
            ws*=3;
        }
//        pw.addPlot("Median Filter", pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
    }
    public void filtTrackData_RollingBall(Color c){
        double dRx=10;
        int nRanking=0;
        ArrayList<Double> dvXUp=new ArrayList(),dvYUp=new ArrayList(), dvXDown=new ArrayList(), dvYDown=new ArrayList();
        ProbingBall pb=new ProbingBall(pdX,pdY,dRx,-1,nRanking);

        pb.getProbContactingPoints(dvXUp, dvYUp,ProbingBall.Downward);
//        pw.addPlot("FiltedDownward", dvXUp,dvYUp, 2, PlotWindow.LINE,c,true);
        pw.addPlot("BallTrailDownward", pdX, pb.getProbeTrail(ProbingBall.Downward), 2, 2,Color.red);

        pb.getProbContactingPoints(dvXDown, dvYDown,ProbingBall.Upward);
//        pw.addPlot("FiltedUpward", dvXDown,dvYDown, 2, PlotWindow.LINE,Color.CYAN,true);
        pw.addPlot("BallTrailUpward", pdX,pb.getProbeTrail(ProbingBall.Upward), 2, PlotWindow.LINE,Color.BLUE,true);
        double[] pdYC=CommonStatisticsMethods.getWeightedDeltaLine(pb.getProbeTrail(pb.Downward), pb.getProbeTrail(pb.Upward));
        CommonGuiMethods.displayNewPlotWindowPlus("Weighted Center Lin", CommonStatisticsMethods.getTrimmedDoubleArray(pdX, 0, pdYC.length-1, 1), pdYC, 2, 2, Color.BLUE);
//        pw.addPlot("BallTrailCenter", CommonStatisticsMethods.getTrimmedDoubleArray(pdX, 0, pdYC.length-1, 1),pdYC, 2, PlotWindow.LINE,Color.blue,true);
        pb.displayProbeCircle(pw);
    }
    public void filtTrackData_RollingBall_SpikeRemoved(Color c){
//        pdFiltedY=pdY;
        double[] pdFiltedY=new double[pdY.length];
        double[] pdt1,pdt2;
        CommonStatisticsMethods.copyArray(pdY,pdFiltedY);
            SpikeHandler detector=new SpikeHandler(pdX,pdY,-2,0.05);
//            pdFiltedY=detector.getTransitionPreservingFiltedData_GeneralSpikes(Math.abs(nMaxMultiplicity));
//            pdFiltedY=detector.removeSpikes_Progressive(Math.abs(2));
        double dRx=10;
        int nRanking=0;
        ArrayList<Integer> nvProbingPointsUp,nvProbingPointsDown;
        ArrayList<intRange> spikesUp,spikesDown;

        ProbingBall pb=new ProbingBall(pdX,pdFiltedY,dRx,-1,nRanking);
        nvProbingPointsUp=pb.getProbeContactingPositions(ProbingBall.Upward);
        detector.setSelection(nvProbingPointsUp, false);

        CommonStatisticsMethods.copyArray(pdY,pdFiltedY);
        pb=new ProbingBall(pdX,pdFiltedY,dRx,-1,nRanking);
        nvProbingPointsDown=pb.getProbeContactingPositions(ProbingBall.Downward);
        detector.setSelection(nvProbingPointsDown, true);

        detector.calDeviations();
        detector.findSpikes(1);
        spikesUp=detector.getSpikeRanges(1);
        detector.findSpikes(-1);
        spikesDown=detector.getSpikeRanges(-1);

        CommonStatisticsMethods.copyArray(pdY,pdFiltedY);
        CommonStatisticsMethods.removeSpikes(pdFiltedY, spikesUp, 1);
        pb=new ProbingBall(pdX,pdFiltedY,dRx,-1,nRanking);
        pdt1=pb.getProbeTrail(ProbingBall.Upward);
        pw.addPlot("FiltedDownward", pdX, pdt1, 2, PlotWindow.LINE,c,true);

        CommonStatisticsMethods.copyArray(pdY,pdFiltedY);
        CommonStatisticsMethods.removeSpikes(pdFiltedY, spikesDown, -1);
        pb=new ProbingBall(pdX,pdFiltedY,dRx,-1,nRanking);
        pdt2=pb.getProbeTrail(ProbingBall.Upward);
        pw.addPlot("FiltedDownward", pdX, pdt2, 2, PlotWindow.LINE,Color.CYAN,true);
        pb.displayProbeCircle(pw);
    }
    public ArrayList<double[]> filtTrackData(double dPValue, int nMaxMultiplicity, Color c, boolean display){
        SpikeHandler detector=new SpikeHandler(pdX,pdY,nMaxMultiplicity,dPValue);
        int iterations=1,ws=1;
        Color c1;
        double[] filtedY=CommonStatisticsMethods.copyArray(pdY);
        double[] filtedY0=CommonStatisticsMethods.copyArray(pdY);
        ArrayList<double[]> pdvFiltedY=new ArrayList();
        nMaxMultiplicity=-1;
        if(nMaxMultiplicity>0){
            pdFiltedY=detector.removeSpikes_Progressive(Math.abs(nMaxMultiplicity));
        } else {
//            nMaxMultiplicity=3;
            pdFiltedY=CommonStatisticsMethods.copyArray(pdY);
//            pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(filtedY0,ws,ws,null);
//            detector=new SpikeHandler(pdX,pdFiltedY,nMaxMultiplicity,dPValue);
//            pdFiltedY=detector.removeSpikes_Progressive(Math.abs(nMaxMultiplicity));
//            CommonStatisticsMethods.copyArray(pdFiltedY,pdY);
            for(int i=0;i<iterations;i++){
//                EdgePreservingFilter_Comprehensive filter=new EdgePreservingFilter_Comprehensive(pdX,pdFiltedY);
                EdgePreservingFilter filter=new EdgePreservingFilter(pdX,pdFiltedY,nMaxMultiplicity);
                filtedY=filter.getTransitionPreservingFiltedData(Math.abs(nMaxMultiplicity),iterations);
                CommonStatisticsMethods.copyArray(filtedY, pdFiltedY);
                if(iterations>1)
                    c1=CommonGuiMethods.getDefaultColor(i);
                else
                    c1=c;
                if(display&&i==iterations-1) pw.addPlot("Filted", pdX, filtedY, 2, PlotWindow.LINE,c1,true);
                nMaxMultiplicity++;
                pdvFiltedY.add(filtedY);
//            pw.markPoints("BasePoints",pdX,pdY,filter.m_nvBasePointsN,3,PlotWindow.CIRCLE,Color.CYAN);
            }
        }
        if(nMaxMultiplicity>0) pw.addPlot("Filted", pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
        return pdvFiltedY;
    }
    
    public void getFiltedTrackData(ArrayList<double[]> pdvXY){
        pdvXY.clear();
        pdvXY.add(pdX);
        pdvXY.add(pdFiltedY);
    }
    public void updateFiltedData(){
//        CommonStatisticsMethods.copyArray(pdFiltedY, pdY);
        if(pdFiltedY!=null) pdY=CommonStatisticsMethods.copyArray(pdFiltedY);
    }
     public void selectCurve(){
        Roi roi=pw.getImagePlus().getRoi();
        pw.selectActiveCurve(roi);
        if(pw.isActiveCurveSelected()){
            int index=pw.getActiveCurveIndex();
            pdX=pw.getXValues(index);
            pdY=pw.getYValues(index);
        }else{
            pdX=pdXOriginal;
            pdY=pdYOriginal;
        }
    }
     void markRankingExtrema(Color c){
        int ws=3;
        int rankingX=ws;
        int rankingN=ws;
        ArrayList <String> labels=new ArrayList(),texts=new ArrayList();
        String title="Ranking Extrema Parameters";
        labels.add("Window Size");
        labels.add("Maxima Ranking");
        labels.add("Minima Ranking");
        texts.add(PrintAssist.ToString(ws));
        texts.add(PrintAssist.ToString(rankingX));
        texts.add(PrintAssist.ToString(rankingN));
        ArrayList<String> inputs=CommonGuiMethods.inputTexts(title, labels, texts);
        ws=Integer.parseInt(inputs.get(0));
        rankingN=Integer.parseInt(inputs.get(1));
        rankingX=Integer.parseInt(inputs.get(2));
        ArrayList<Integer> nvX=new ArrayList(),nvN=new ArrayList();
        int i,index;
        ArrayList<Double> dvX=new ArrayList(), dvY=new ArrayList();

        for(i=0;i<nvLx.size();i++){
            index=nvLx.get(i);
            dvY.add(pdY[index]);
        }
        CommonStatisticsMethods.getRunningWindowQuantile(dvY, ws, rankingX, nvX);
        dvY.clear();
        for(i=0;i<nvLn.size();i++){
            index=nvLn.get(i);
            dvY.add(pdY[index]);
        }
        CommonStatisticsMethods.getRunningWindowQuantile(dvY, ws, rankingN, nvN);
        
        utilities.QuickSortInteger.quicksort(nvN);
        utilities.QuickSortInteger.quicksort(nvX);

        int iterations=1;
        EdgePreservingFilter filter=new EdgePreservingFilter(pdX,pdY,1);
        pdFiltedY=filter.getTransitionPreservingFiltedData_RankingExtrema(nvX, nvN, iterations);

        for(i=0;i<nvX.size();i++){
            index=nvX.get(i);
            nvX.set(i, nvLx.get(index));
        }

        for(i=0;i<nvN.size();i++){
            index=nvN.get(i);
            nvN.set(i, nvLn.get(index));
        }

        pw.markPoints("Ranking Maxima", pdX, pdY, nvX, 3, PlotWindow.CIRCLE, c);
        pw.markPoints("Ranking Maxima", pdX, pdY, nvN, 3, PlotWindow.CIRCLE, Color.CYAN);
        pw.addPlot("Filted", pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
     }
     public ArrayList<double[]> filtTrack_RankingBasedEdgeProtection(Color c, boolean display){
//         RankingBasedEdgePreservingFilter filter=new RankingBasedEdgePreservingFilter(pdX,pdY);
         
        int ws=5,iterations=4,iter1=1;
        ArrayList<double[]> pdvFiltedY=new ArrayList();
        ArrayList<String> svLabels=new ArrayList(), svTexts=new ArrayList(),inputs;
        String title="Running Window Size";
        svLabels.add("Half Window Size");
        svTexts.add("5");
//        inputs=CommonGuiMethods.inputTexts(title, svLabels, svTexts);
//        ws=Integer.parseInt(inputs.get(0));
        pdFiltedY=CommonStatisticsMethods.copyArray(pdY);
        ws=2;
        for(int i=0;i<iterations;i++){
            RankingBasedEdgePreservingFilter filter=new RankingBasedEdgePreservingFilter(pdX,pdFiltedY);
            double[] filtedY=filter.getTransitionPreservingFiltedData(ws, iter1);
            pdvFiltedY.add(filtedY);
            if(display) pw.addPlot("RankingBased", pdX, filtedY, 2, PlotWindow.LINE,CommonGuiMethods.getDefaultColor(i),true);
            CommonStatisticsMethods.copyArray(filtedY, pdFiltedY);
            ws*=2;
        }
        return pdvFiltedY;
     }
     public void showAntiMedianFilter(int temp){
        int ws=5,iterations=4,iter1=1;;
        ArrayList<String> svLabels=new ArrayList(), svTexts=new ArrayList(),inputs;
        String title="Running Window Size";
        svLabels.add("Half Window Size");
        svTexts.add("5");
        inputs=CommonGuiMethods.inputTexts(title, svLabels, svTexts);
        ArrayList<Integer> nvIndexes=null;
        ws=Integer.parseInt(inputs.get(0));
         double[] pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(pdY, ws, ws, nvIndexes);
         double[] pdXf=CommonStatisticsMethods.copyArray(pdX),pdXr=CommonStatisticsMethods.copyArray(pdX);
         int len=pdXf.length,i,left,right,len1=2*ws+1;
         double[] pdYf=new double[len], pdYr=new double[len],pdDelta=new double[len];
         for(i=0;i<len;i++){
             left=i-ws;
             right=i+ws+nMaxRisingInterval;
             pdXf[i]=pdX[i]-ws;
             pdXr[i]=pdX[i]+ws;
             if(left<0||right>=len) continue;
             pdDelta[i]=pdFiltedY[left]-pdFiltedY[right];
         }
         pw.addPlot("Forward Median WS: "+ws, pdXf, pdFiltedY, 2, PlotWindow.LINE,CommonGuiMethods.getDefaultColor(0),true);
         pw.addPlot("Forward Median WS: "+ws, pdXr, pdFiltedY, 2, PlotWindow.LINE,CommonGuiMethods.getDefaultColor(1),true);
         pw.addPlot("Forward Median WS: "+ws, pdX, pdDelta, 2, PlotWindow.LINE,CommonGuiMethods.getDefaultColor(2),true);
     }
     public void setTrackFiter(PolynomialLineFitter cTrackFitter){
         m_cTrackFitter=cTrackFitter;
     }
}
