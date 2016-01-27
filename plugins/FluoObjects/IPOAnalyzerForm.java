/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * IPOAnalyzer.java
 *
 * Created on Aug 23, 2011, 12:31:48 PM
 */

package FluoObjects;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import ij.ImagePlus;
import utilities.CommonGuiMethods;
import ij.WindowManager;
import utilities.CommonMethods;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.intRange;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import utilities.io.FileAssist;
import java.io.FileNotFoundException;
import java.io.IOException;
import utilities.io.PrintAssist;
import utilities.io.ByteConverter;
import utilities.CommonStatisticsMethods;
import utilities.Gui.ImageComparisonViewer;
import ScriptManager.Script_Runner;
import ij.gui.Roi;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import utilities.Gui.ImageSeriesDisplayer;
import utilities.Gui.HighlightingRoiCollectionNode;
import ij.gui.PointRoi;
import utilities.Gui.AssociatedImageDisplayer;
import utilities.Gui.AnalysisMasterForm;
import utilities.Gui.ComponentBackgroundMaker;
import utilities.Geometry.ImageShapes.*;
import ij.IJ;
import utilities.Non_LinearFitting.FittingResultsNode;
import java.io.DataInputStream;
import FluoObjects.IPOGTLevelTransitionAnalyzer;
import FluoObjects.IPOGTLevelTransitionAnalyzerForm;
import ImageAnalysis.ContourFollower;
import javax.swing.event.*;
import java.util.Formatter;
import utilities.io.AsciiInputAssist;
import java.io.BufferedReader;
import ij.gui.Line;
import ij.process.EllipseFitter;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.Gui.*;
import FluoObjects.IPOGTLevelInfoAnalyzer;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import ImageAnalysis.SubpixelGaussianMean;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.MeanSem1;
import utilities.Gui.PlotWindowPlus;

/**
 *
 * @author Taihao
 */
public class IPOAnalyzerForm extends javax.swing.JFrame implements MouseMotionListener,MouseListener,TableModelListener,ListSelectionListener, ActionListener,KeyListener{

    class SliceGaussianIPOFitter{
        public ArrayList<IPOGaussianNode> IPOs;
        public String ThreadName;
        public SliceGaussianIPOFitter(final ImagePlus impl0, final ImagePlus implCompen, final ImagePlus implp, final int sliceI, final int sliceF, final int delta, final int offset, ActionListener al, final ArrayList<IPOGaussianNode>[] pcIPOs,final double[] pdHCutoff, final double[] pdSCutoff,String ThreadName){
            boolean silent=SilentFittingRB.isSelected();
            this.ThreadName=ThreadName;
            final IPOGaussianFitter fitter=new IPOGaussianFitter(silent,false,false);
            fitter.setThreadName(ThreadName);
            final int h=impl0.getHeight(),w=impl0.getWidth();
            final ImagePlus implt=CommonMethods.cloneImage(impl0, sliceI);//implt is only a frame;
            fitter.addFitterListener(al);
                Thread m_cFittingThread=new Thread(new Runnable() {
                    public void run() {
                        int[][]pnScratch=new int[h][w];
                        int slice=sliceI+offset;
                        boolean firstSlice=true;
                        while(slice<=sliceF){
                            final ImagePlus impl=implt;
                            synchronized(impl0){
                                impl0.setSlice(slice);
                                CommonMethods.getPixelValue(impl0, slice, pnScratch);
                                CommonMethods.setPixels(impl, pnScratch);
                            }
                            if(firstSlice){
                                fitter.fitImage(impl);
                                firstSlice=false;
                            }else{
                                fitter.updateAndFitImage(impl);
                            }
                            IPOs=IPOGaussianNodeHandler.buildIPOGaussianNodes(fitter.m_cvRegionNodes,fitter.m_cvComplexNodes);
                            synchronized(implCompen){
                                implCompen.setSlice(slice);
                                fitter.loadCompemsatedImage(implCompen);
                            }
                            synchronized(implp){
                                implp.setSlice(slice);
                                fitter.loadProcessedImage(implp);
                            }
                            //assigning the region peak location for each IPOG //11925
                            int i,len=IPOs.size();
                            IPOGaussianNode IPOG;
                            Point peak;
                            for(i=0;i<len;i++){
                                IPOG=IPOs.get(i);
                                peak=fitter.getRegionPeak((int)(IPOG.xc+0.5), (int)(IPOG.yc+0.5));
                                IPOG.xcr=peak.x;
                                IPOG.ycr=peak.y;
                            }

                            pcIPOs[slice-sliceI]=IPOs;
                            pdHCutoff[slice-sliceI]=fitter.getPeakHeightCutoff();
                            pdSCutoff[slice-sliceI]=fitter.getTogalSignalCutoff();
                            slice+=delta;
                        }
                    }
                });
                m_cFittingThread.start();
            }
    }
    
    
    
    static boolean InteractiveMode;
    IPOGTrackPlotWindow m_cIPOGTPW;
    ArrayList<IPOGTrackPlotWindow> m_cvIPOGTPWs;
    ArrayList<IPOGaussianNode> m_cvIPONodes;
    ArrayList<IPOGaussianNode> m_cvSelectedIPOs;
    ArrayList<IPOGaussianNode> m_cvSelectedIPOso;
    ImagePlus implInt,implComposed;
    JTable m_cIPOTable,m_cSelectedIPOTable,IPOGTrackTable;
    double overlappingCutoff;
    IPOGaussianNodeHandler handler;
    StackIPOGaussianNode m_cStackIPOs;
    ImagePlus implOriginal,implp,implCompen,implSignal,implSeries;
    int[][] pixels,pixelsCompen,pixelsCanvus,pnScratch,pnScratch1,pixelsAveIPOs;
    double[][] pdPixels;
    double RegionHCutoff,TotalSignalCutoff;
    ImageComparisonViewer m_cViewer;
    StackIPOGTracksNode m_cStackIPOT;
    ArrayList<IPOGTrackNode> m_cvSelectedIPOGTracks;
    boolean ControlDown;
    IPOGTrackNode m_cSelectedIPOGT;
    static int StatusIPOG=0,StatusTrack=1;
    int nIPOTableStatus;
    int[][][] pixelsCompenStack;
    int[][][] pixelsStack;
    ImageSeriesDisplayer m_cTrackDisplayer;
    IPOGTrackNode m_cSelectedTrack;
    IPOGTrackNode m_cIPOGTOnSeriesImage;
    ImagePlus implAssociated0,implAssociated;
    ArrayList<IPOGTrackNode> m_cvHighlightedIPOGTs;
    ArrayList<Color> m_cvTrackColors;
    ArrayList<IPOGaussianNode> m_cvUntrackedIPOGsOnSeriesImage;
    int previousSlice;
    /** Creates new form IPOAnalyzer */
    AnalysisMasterForm m_cMasterForm;
    String sOriginalImagePath;
    JViewport m_cStoredTable2Viewport,m_cTable2Viewport,m_cTable1Viewport,IPOGSelectionChoiceViewPort,IPOGTSelectionChoiceViewPort;

    IPOSelectionChoiceForm m_cIPOGSelector;
    IPOGTrackSelectionChoiceForm m_cIPOGTSelector;
    ImagePlus implAveIPOs;
    IPOGTLevelTransitionAnalyzer m_cIPOGTTransitionAnalyzer;
    ArrayList<IPOGTLevelInfoSummaryNode> m_cvLevelInfoSummaryNodes;
    JViewport m_cLevelInfoViewport;
    JTable m_cLevelInfoTable;
    intRange pixelRange, pixelRangeRaw;
    ImagePlus implSelection;
    static int lastSlice;
    public void setMasterForm(AnalysisMasterForm cMasterForm){
        m_cMasterForm=cMasterForm;
    }
    public static double[][] ppdHeightCutoff,ppdGroovePixelValueCutoff,ppdGrooveHeightCutoof;
    public static double[] pdHeightCutoffPs={0.1,0.05,0.01,0.001,0.0001,0.00001,0.000001,0.0000001,0.000000001};
    public static int firstSliceForCutoff;
    public static boolean HeightCutoffIsComputed;
    public IPOAnalyzerForm() {
        initComponents();
        m_cViewer=new ImageComparisonViewer();
        m_cvSelectedIPOs=new ArrayList();
        m_cvSelectedIPOso=new ArrayList();
        m_cvSelectedIPOGTracks=new ArrayList();
        RegionHCutoff=-1;
        TotalSignalCutoff=-1;
        m_cStackIPOs=null;
        ControlDown=false;
        m_cvIPOGTPWs=new ArrayList();
        setFocusable(true);
        m_cStoredTable2Viewport=null;
        m_cIPOGSelector=new IPOSelectionChoiceForm();        
        m_cIPOGTSelector=new IPOGTrackSelectionChoiceForm();
        IPOGSelectionChoiceViewPort=new JViewport();
        IPOGSelectionChoiceViewPort.setView(m_cIPOGSelector.getContentPane());
        IPOGTSelectionChoiceViewPort=new JViewport();
        IPOGTSelectionChoiceViewPort.setView(m_cIPOGTSelector.getContentPane());
        InteractiveMode=true;
        HeightCutoffIsComputed=false;
        setSelector();
        
    }
    static public boolean isInteractive(){
        return InteractiveMode;
    }
    static public void nullifyHeightCutoffs(){
        ppdHeightCutoff=null;
        ppdGroovePixelValueCutoff=null;
        ppdGrooveHeightCutoof=null;
    }
    public static void setInteractive(boolean interactive){
        InteractiveMode=interactive;
    }
    void setSelector(){
        String option=(String)SelectionTargetOptionCB.getSelectedItem();
        if(option.contentEquals("IPOGs")) {
            if(m_cIPOGSelector.selectEntireStack())
                m_cIPOGSelector.updateIPOSelectionChoices(m_cStackIPOs);
            else
                m_cIPOGSelector.updateIPOSelectionChoices(m_cvIPONodes);

            IPOSelectionChoiceScrollPane.setViewport(IPOGSelectionChoiceViewPort);
        }
        if(option.contentEquals("Tracks")) {
            m_cIPOGTSelector.updateIPOGTSelectionChoice(m_cStackIPOT,(String)TrackPlotOptionCB.getSelectedItem());
            IPOSelectionChoiceScrollPane.setViewport(IPOGTSelectionChoiceViewPort);
        }
        if(option.contentEquals("Tracks DF")) {
            selectTracks_Default(true);
            showSelectedTracks();
        }
        if(option.contentEquals("NormalShape")) {
            selectTracks_NormalShape();
            showSelectedTracks();
        }
        if(option.contentEquals("AbnormalShape")) {
            selectTracks_AbnormalShape();
            showSelectedTracks();
        }
        if(option.contentEquals("MixedShape")) {
            selectTracks_MixedShape();
            showSelectedTracks();
        }
    }
    public void setIPOs(ArrayList<IPOGaussianNode> cvIPOs){
        this.m_cvIPONodes=cvIPOs;
        updateSelectionChoices();
    }
    void updateSelectionChoices(){
        if(((String)SelectionTargetOptionCB.getSelectedItem()).contentEquals("IPOs"))
            updateIPOSelectionChoices();
        else
            updateTrackSelectionChoices();
    }
    void updateIPOSelectionChoices(){
        m_cIPOGSelector.updateIPOSelectionChoices(m_cvIPONodes);
    }
    public void updateIPOs(ArrayList<IPOGaussianNode> cvIPOs){
        if(implComposed!=null) implComposed.getWindow().getCanvas().addMouseListener(this);
        if(implInt!=null) implInt.getWindow().getCanvas().addMouseListener(this);
        setIPOs(cvIPOs);
        updateIPOTable();
        updateSelectedIPOTable(m_cvSelectedIPOs);
        this.setVisible(true);
        addKeyListener(this);
    }
    public IPOAnalyzerForm(ArrayList<IPOGaussianNode> cvIPOs){
        this();
        updateIPOs(cvIPOs);
    }
    public void buildHeightCutoff(){
        int i,len=pixelsStack.length,len1=pdHeightCutoffPs.length;
        ppdHeightCutoff=new double[len][len1];
        ppdGroovePixelValueCutoff=new double[len][len1];
        int[][] pixels=pixelsStack[0];
        int h=pixels.length,w=pixels[0].length;
        ImageShape cIS=new CircleImage(3),cBkg=new Ring(3,4);
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cBkg.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        for(i=0;i<len;i++){
            pixels=pixelsStack[i];
            CommonMethods.calHeightCutoff(pixels,cIS,cBkg,pdHeightCutoffPs,ppdHeightCutoff[i],ppdGroovePixelValueCutoff[i]);
        }
        m_cStackIPOT.ppdHeightCutoff=ppdHeightCutoff;
        HeightCutoffIsComputed=true;
    }
    static public int showHeightCutoff(){
        if(!HeightCutoffIsComputed) return -1;
        int m=pdHeightCutoffPs.length,n=ppdHeightCutoff.length,i,j;
        double[] pdt=new double[n],pdX=new double[n];
        for(i=0;i<n;i++){
            pdX[i]=firstSliceForCutoff+i;
            pdt[i]=ppdHeightCutoff[i][0];
        }
        
        PlotWindowPlus pw=CommonGuiMethods.displayNewPlotWindowPlus("Height Cutoff", pdX, pdt, 1, 2, Color.black);
        for(j=1;j<m;j++){
            pdt=new double[n];
            for(i=0;i<n;i++){
                pdt[i]=ppdHeightCutoff[i][j];
            }
            pw.addPlot("p="+PrintAssist.ToString(pdHeightCutoffPs[j], 1), pdX, pdt, 1, 2,CommonGuiMethods.getDefaultColor(j));
        }
        return 1;
    }
    static public double getHeightCutoff(int slice, double p){
        int index=getHeightCutoffPIndex(p),sliceIndex=slice-firstSliceForCutoff;
        return ppdHeightCutoff[sliceIndex][index];
    }
    static public int getHeightCutoffPIndex(double p){
        int i,n=pdHeightCutoffPs.length;
        for(i=0;i<n;i++){
            if(pdHeightCutoffPs[i]<=p) return i;
        }
        return n-1;
    }
    void updateIPOTable(){
        nIPOTableStatus=StatusIPOG;
        ArrayList<String> parNames=new ArrayList(), parValues=new ArrayList();
        String[][] ppsPars=IPOGaussianNodeHandler.getIPOsAsStrings(m_cvIPONodes, SimpleIPOGsRB.isSelected());
        String[] columnHead=ppsPars[0];
        int i,j,rows=ppsPars.length-1;
        Object[][] poData=new Object[rows][];

        for(i=0;i<rows;i++){
            poData[i]=ppsPars[i+1];
        }

        m_cTable2Viewport=new JViewport();
        m_cStoredTable2Viewport=null;

        m_cIPOTable=new JTable(poData,columnHead);
        CommonGuiMethods.setTableCellAlignmentH(m_cIPOTable, JLabel.RIGHT);
        m_cIPOTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_cIPOTable=CommonGuiMethods.autoResizeColWidth(m_cIPOTable, new javax.swing.table.DefaultTableModel());

        m_cTable2Viewport.setView(m_cIPOTable);

        showTable(m_cTable2Viewport,1);
        m_cIPOTable.getModel().addTableModelListener(this);
        m_cIPOTable.getSelectionModel().addListSelectionListener(this);
        updateSelectionChoices();
        TotalIPONumberLB.setText("("+m_cvIPONodes.size()+")");
        if(RegionHCutoff>0) RegionHeightCutoffLB.setText("HCutoff "+PrintAssist.ToString(RegionHCutoff, 0));
        if(TotalSignalCutoff>0) TotalSignalCutoffLB.setText("SCutoff "+PrintAssist.ToString(TotalSignalCutoff, 0));
        ViewTrackRB.setSelected(false);
    }

    void updateTrackTable(String option){
        nIPOTableStatus=StatusTrack;
        ArrayList<IPOGTrackNode> IPOGTracks=m_cStackIPOT.m_cvIPOGTracks;
        if(option.contentEquals("SelectedTracks")){
            IPOGTracks=m_cvSelectedIPOGTracks;
        }
        if(option.contentEquals("Track Bundles")){
            IPOGTracks=m_cStackIPOT.getBundlesAsTracks();
        }

        IPOTableTitleLB.setText("Tracks ("+IPOGTracks.size()+")");
        
        ArrayList<String> parNames=new ArrayList(), parValues=new ArrayList();
        
        String[][] ppsPars=IPOGTrackHandler.getIPOGTracksAsStrings(IPOGTracks,(String)TrackPlotOptionCB.getSelectedItem());
        String[] columnHead=ppsPars[0];
        int i,j,rows=ppsPars.length-1;
        Object[][] poData=new Object[rows][];

        for(i=0;i<rows;i++){
            poData[i]=ppsPars[i+1];
        }

        JViewport viewport=new JViewport();
        
        m_cIPOTable=new JTable(poData,columnHead);
        CommonGuiMethods.setTableCellAlignmentH(m_cIPOTable, JLabel.RIGHT);
        m_cIPOTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_cIPOTable=CommonGuiMethods.autoResizeColWidth(m_cIPOTable, new javax.swing.table.DefaultTableModel());
        viewport.setView(m_cIPOTable);
        Table2Pane.setViewport(viewport);
        m_cIPOTable.getModel().addTableModelListener(this);
        m_cIPOTable.getSelectionModel().addListSelectionListener(this);
//        updateSelectionChoices();
    }
    int selectTrack(IPOGaussianNode IPOG){
        SelectTrackRB.setSelected(false);
        int tkIndex=IPOG.TrackIndex;
        if(!validTrackIndex(tkIndex)) return -1;
        IPOGTrackNode IPOGT=m_cStackIPOT.getIPOGT(tkIndex);
/*
        if(BuildRWAIPOShapesRB.isSelected()&&pixelsCompenStack!=null) {
            showIPOAveShape(IPOGT,IPOGT.firstSlice);
        }
*/
        m_cSelectedIPOGT=IPOGT;
        ArrayList<IPOGaussianNode> cvIPOGs=IPOGT.m_cvIPOGs;
        updateSelectedIPOTable(cvIPOGs);
//        IPOGT.plotAmp((String)TrackPlotOptionCB.getSelectedItem());
        plotTrack(m_cSelectedIPOGT,IPOGTLevelTransitionAnalyzer.getMaxRizingInterval((String)TrackPlotOptionCB.getSelectedItem()));
        if(DisplayTrackImageRB.isSelected()) displayIPOGTOnSeriesImage(m_cSelectedIPOGT);
        SelectTrackRB.setSelected(true);
        return 1;
    }
    /*
    int showIPOAveShape(IPOGTrackNode IPOGT, int sliceI){
        if(!BuildRWAIPOShapesRB.isSelected()||pixelsCompenStack==null) return -1;
        int firstSlice=IPOGT.firstSlice;
        int RWs=Integer.parseInt(IPORWAShapeWSTF.getText());
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
        pnScratch1=CommonStatisticsMethods.getIntArray2(pnScratch1, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
        IPOGT.calRWAIPOShapes(pixelsCompenStack, pnScratch, pnScratch1, RWs);
        ImageShape cIS=IPOGT.cISAveIPOs;

        intRange xRangeRWA=cIS.getXrange(),yRangeRWA=cIS.getYrange();
        int wa=xRangeRWA.getRange(),ha=yRangeRWA.getRange();
        int rs=5,cs=5;
        int W=cs*(wa+1)-1,H=rs*(ha+1)-1;
        if(implAveIPOs==null){
            implAveIPOs=CommonMethods.getBlankImage(ImagePlus.GRAY16, W, H);
        }else if(implAveIPOs.getWidth()!=W||implAveIPOs.getHeight()!=H){
            implAveIPOs.close();
            implAveIPOs=CommonMethods.getBlankImage(ImagePlus.GRAY16, W, H);
        }
        implAveIPOs.setTitle("Track"+IPOGT.TrackIndex+", slice"+sliceI);
        pixelsAveIPOs=CommonStatisticsMethods.getIntArray2(pixelsAveIPOs, W, H);
        int nSlices=rs*cs,slice,index,sliceF,x0,y0,r,c;
        sliceF=sliceI+nSlices-1;
        IPOGaussianNode IPOG;
        DoubleRange dRange=new DoubleRange();
        for(r=0;r<rs;r++){
            y0=r*(ha+1);
            for(c=0;c<cs;c++){
                x0=c*(wa+1);
                slice=sliceI+r*cs+c;
                IPOG=IPOGT.m_cvIPOGsAve.get(slice-firstSlice);
                if(IPOG==null) continue;
                IPOG.cModel.copyData(pixelsAveIPOs,x0,y0);
                dRange.expandRange(IPOG.cModel.getDataRange());
            }
        }
        int pixeln=(int)(dRange.getMin()),pixelx=(int)(dRange.getMax()+0.5);
        CommonStatisticsMethods.setGridLines(pixelsAveIPOs, wa, ha, pixeln);
        CommonMethods.setPixels(implAveIPOs, pixelsAveIPOs);
        implAveIPOs.getProcessor().setMinAndMax(pixeln, pixelx);
        CommonMethods.refreshImage(implAveIPOs);
        implAveIPOs.getWindow().getCanvas().setMagnification(12);
        implAveIPOs.getWindow().toFront();
        CommonGuiMethods.optimizeWindowSize(implAveIPOs);
        return 1;
    }*/
    void plotTrack(IPOGTrackNode IPOGT, int nMaxRisingInterval){
        plotTrack(IPOGT, true,nMaxRisingInterval,IPOGT.firstSlice,IPOGT.lastSlice);
    }
    void plotTrack(IPOGTrackNode IPOGT, boolean detectTransition, int nMaxRisingInterval, int sliceI, int sliceF){
        final IPOGTrackPlotWindow IPOGTPW=new IPOGTrackPlotWindow(IPOGT,(String)TrackPlotOptionCB.getSelectedItem(),nMaxRisingInterval,sliceI,sliceF);
        m_cIPOGTPW=IPOGTPW;
        if(m_cvIPOGTPWs==null) m_cvIPOGTPWs=new ArrayList();
        if(IPOGT.lastSliceE<IPOGT.lastSlice) extendIPOGTracks();
        if(isInteractive())m_cvIPOGTPWs.add(IPOGTPW);

        if(m_cIPOGTTransitionAnalyzer==null) {
            intRange ir=pixelRange;
            if(((String)TrackPlotOptionCB.getSelectedItem()).contains("Raw")) ir=pixelRangeRaw;
            m_cIPOGTTransitionAnalyzer=new IPOGTLevelTransitionAnalyzer(this,pixelsStack,pixelsCompenStack,m_cStackIPOT.sliceI,ir);
            m_cIPOGTTransitionAnalyzer.setImageID(implOriginal.getTitle());
            m_cIPOGTTransitionAnalyzer.addActionListener(this);
            IPOGTLevelTransitionAnalyzerForm form=new IPOGTLevelTransitionAnalyzerForm(m_cIPOGTTransitionAnalyzer);
            form.setVisible(true);
//            m_cIPOGTTransitionAnalyzer.setVisible(true);
        }else if(m_cIPOGTTransitionAnalyzer.getDisplayingForm()==null){
            IPOGTLevelTransitionAnalyzerForm form=new IPOGTLevelTransitionAnalyzerForm(m_cIPOGTTransitionAnalyzer);
            form.setVisible(true);
        }else if(!m_cIPOGTTransitionAnalyzer.getDisplayingForm().isVisible()){
            m_cIPOGTTransitionAnalyzer.getDisplayingForm().setVisible(true);
        }
        
        IPOGTPW.pw.getCanvas().addMouseMotionListener(this);
        IPOGTPW.pw.getCanvas().addMouseListener(this);
        IPOGTPW.pw.addWindowListener(new WindowAdapter(){
            public void WindowClosing(WindowEvent we){
                IPOGTrackPlotWindow IPOGTPWt;
                int i,len=m_cvIPOGTPWs.size();
                for(i=0;i<len;i++){
                    IPOGTPWt=m_cvIPOGTPWs.get(i);
                    if(IPOGTPWt.pw==we.getWindow())m_cvIPOGTPWs.remove(IPOGTPWt);
                }
            }
        });
        if(isInteractive()){
            if(detectTransition){
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        m_cIPOGTTransitionAnalyzer.updateIPOGTPW(IPOGTPW,(String)TrackPlotOptionCB.getSelectedItem());
                    }
                });
            }
        }else{
            m_cIPOGTTransitionAnalyzer.updateIPOGTPW(IPOGTPW,(String)TrackPlotOptionCB.getSelectedItem());
        }
    }
    boolean validTrackIndex(int id){
        return m_cStackIPOT.validTrackIndex(id);
    }
    int storeSelectedIPOGs(ArrayList<IPOGaussianNode> cvIPOs){
        if(m_cvSelectedIPOso==cvIPOs) return -1;
        int i,len=cvIPOs.size();
        m_cvSelectedIPOso.clear();
        for(i=0;i<len;i++){
            m_cvSelectedIPOso.add(cvIPOs.get(i));
        }
        return 1;
    }
    ArrayList<Color> m_cvSelectedIPOColors;
    int updateSelectedIPOTable(ArrayList<IPOGaussianNode> cvIPOs,ArrayList<Color> colors){
        m_cvSelectedIPOColors=colors;
        updateSelectedIPOTable(cvIPOs);
        m_cvSelectedIPOColors=null;
        return 1;
    }
    void refreshSelectedIPOTable(ArrayList<IPOGaussianNode> cvIPOs){
        boolean selected=SelectTrackRB.isSelected();
        SelectTrackRB.setSelected(false);
        updateSelectedIPOTable(cvIPOs);
        SelectTrackRB.setSelected(selected);
    }
    int updateSelectedIPOTable(ArrayList<IPOGaussianNode> cvIPOs){
        if(cvIPOs==null) return -1;
        if(SelectTrackRB.isSelected()){
            if(!cvIPOs.isEmpty())selectTrack(cvIPOs.get(0));
            return 1;
        }
        storeSelectedIPOGs(cvIPOs);
        ArrayList<String> parNames=new ArrayList(), parValues=new ArrayList();
        String[][] ppsPars=IPOGaussianNodeHandler.getIPOsAsStrings(cvIPOs, SimpleIPOGsRB.isSelected());
        String[] columnHead=ppsPars[0];
        int i,j,rows=ppsPars.length-1;
        Object[][] poData=new Object[rows][];

        for(i=0;i<rows;i++){
            poData[i]=ppsPars[i+1];
        }

        m_cTable1Viewport=new JViewport();
        m_cSelectedIPOTable=new JTable(poData,columnHead);
        ComponentBackgroundMaker cc;
        m_cSelectedIPOTable.getModel().addTableModelListener(this);
        CommonGuiMethods.setTableCellAlignmentH(m_cSelectedIPOTable, JLabel.RIGHT);
        m_cSelectedIPOTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_cSelectedIPOTable=CommonGuiMethods.autoResizeColWidth(m_cSelectedIPOTable, new javax.swing.table.DefaultTableModel());
        if(m_cvSelectedIPOColors!=null){
            cc=new ComponentBackgroundMaker(m_cSelectedIPOTable,1,m_cvSelectedIPOColors);
        }

        m_cTable1Viewport.setView(m_cSelectedIPOTable);
        showTable(m_cTable1Viewport,0);

        m_cSelectedIPOTable.getModel().addTableModelListener(this);
        m_cSelectedIPOTable.getSelectionModel().addListSelectionListener(this);
        updateSignalImage(m_cvSelectedIPOs);
        SelectedIPONumberLB.setText("("+m_cvSelectedIPOs.size()+")");
        return 1;
    }
    void updateSignalImage(IPOGaussianNode IPOG){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        IPOGs.add(IPOG);
        updateSignalImage(IPOGs);
    }
    int updateSignalImage(ArrayList<IPOGaussianNode> IPOs){
        if(implSignal==null) return -1;
        if(!implSignal.isVisible())return -1;
        int slice=implCompen.getCurrentSlice();
        if(slice!=implSignal.getCurrentSlice()) implSignal.setSlice(slice);
        int i,len=IPOs.size();
        CommonStatisticsMethods.copyArray(pixelsCompen, pnScratch);
        for(i=0;i<len;i++){
            IPOGaussianNodeHandler.getSuperImposition(pnScratch, IPOs.get(i));
        }
        CommonMethods.setPixels(implSignal, pnScratch);
        CommonMethods.refreshImage(implSignal);
        return 1;
    }
    int updateFittedSliceTable(int sliceIndex){
        int cols=10,rows=sliceIndex/cols+1,i,j;//sliceIndex starts from 0
        if(rows<2) rows=2;
        String[][] ppsPars=new String[rows][cols];
        String[] columnHead=ppsPars[0];

        int index,o;
        for(i=0;i<rows;i++){
            o=cols*i;
            for(j=0;j<cols;j++){
                index=o+j;
                if(index<=sliceIndex)
                    ppsPars[i][j]=""+index;
                else
                    ppsPars[i][j]=" ";
            }
        }
        Object[][] poData=new Object[rows][cols];

        for(i=0;i<rows-1;i++){
            poData[i]=ppsPars[i+1];
        }

        JViewport viewport=new JViewport();
        JTable sliceTable=new JTable(poData,columnHead);
        CommonGuiMethods.setTableCellAlignmentH(sliceTable, JLabel.RIGHT);
        sliceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sliceTable=CommonGuiMethods.autoResizeColWidth(sliceTable, new javax.swing.table.DefaultTableModel());
        viewport.setView(sliceTable);
        ThreadStatusSP.setViewport(viewport);
        return 1;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hightlightIPOsBT = new javax.swing.JButton();
        ClickToSelectRB = new javax.swing.JRadioButton();
        Table2Pane = new javax.swing.JScrollPane();
        IPOGaussianFittingBT = new javax.swing.JButton();
        IPOTableTitleLB = new javax.swing.JLabel();
        Table1Pane = new javax.swing.JScrollPane();
        SelectedIPOTitleLB = new javax.swing.JLabel();
        IncludeOverlappingIPOsBT = new javax.swing.JButton();
        IncludeClustersBT = new javax.swing.JButton();
        FitStackRB = new javax.swing.JRadioButton();
        ImportFittedIPOsBT = new javax.swing.JButton();
        OverlapCutoffTF = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        StatusLB = new javax.swing.JLabel();
        HighlightChoiceCB = new javax.swing.JComboBox();
        SilentFittingRB = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        FirstSliceTF = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        LastSliceTF = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        NumOfThreadTF = new javax.swing.JTextField();
        MasterStatusLB = new javax.swing.JLabel();
        ThreadStatusSP = new javax.swing.JScrollPane();
        jLabel12 = new javax.swing.JLabel();
        AnalyzeSliceLB = new javax.swing.JLabel();
        AnalyzeSliceTF = new javax.swing.JTextField();
        AssociateImageBT = new javax.swing.JButton();
        ShowAssociatedImageRB = new javax.swing.JRadioButton();
        StackSliceRangeLB = new javax.swing.JLabel();
        ViewSelectedIPORB = new javax.swing.JRadioButton();
        MonitingIPOSBT = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        SelectedIPONumberLB = new javax.swing.JLabel();
        TotalIPONumberLB = new javax.swing.JLabel();
        RegionHeightCutoffLB = new javax.swing.JLabel();
        TotalSignalCutoffLB = new javax.swing.JLabel();
        ImportStackIPOTBT = new javax.swing.JButton();
        ViewTrackRB = new javax.swing.JRadioButton();
        TrackTableOptionCB = new javax.swing.JComboBox();
        AppendSelectionRB = new javax.swing.JRadioButton();
        IPOCoordinatesTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        RebuildStackIPOsBT = new javax.swing.JButton();
        LinkIPOGsBT = new javax.swing.JButton();
        SelectTrackRB = new javax.swing.JRadioButton();
        TrackPlotOptionCB = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        TrackImageRadiusTF = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        TrackImageMagTF = new javax.swing.JTextField();
        DisplayTrackImageRB = new javax.swing.JRadioButton();
        jLabel16 = new javax.swing.JLabel();
        TrackImageITF = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        TrackImageBlockDimTF = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        TrackImageIncrementTF = new javax.swing.JTextField();
        HighlightTracksOnSeriesImageRB = new javax.swing.JRadioButton();
        ShowAssociatedImageStackRB = new javax.swing.JRadioButton();
        HighlightTracksOnAssociatedImagesRB = new javax.swing.JRadioButton();
        DefaultSettingBT = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        BringTrackPlotsToFront = new javax.swing.JButton();
        MoveToTable2BT = new javax.swing.JButton();
        RestoreTable2BT = new javax.swing.JButton();
        LockTable1RB = new javax.swing.JRadioButton();
        LockTable2RB = new javax.swing.JRadioButton();
        RoiTraceBT = new javax.swing.JButton();
        ShowIPOTBundleRB = new javax.swing.JRadioButton();
        SaveIPOGTSToABFBT = new javax.swing.JButton();
        IPOSelectionContainerPane = new javax.swing.JPanel();
        SelectIPOBT = new javax.swing.JButton();
        SelectionTargetOptionCB = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        IPOSelectionChoiceScrollPane = new javax.swing.JScrollPane();
        IPOGComplexRB = new javax.swing.JRadioButton();
        BuildRWAIPOShapesRB = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        IPORWAShapeWSTF = new javax.swing.JTextField();
        ExportTracksBT = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        RisingIntervalTF = new javax.swing.JTextField();
        ExportLevelInfoBT = new javax.swing.JButton();
        ImportLevelInfoBT = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        SortIPOsBT = new javax.swing.JButton();
        SimpleIPOGsRB = new javax.swing.JRadioButton();
        ComputeRawPeaksBT = new javax.swing.JButton();
        AdjustFirstSliceRB = new javax.swing.JRadioButton();
        AdjustTrackCentersRB = new javax.swing.JRadioButton();
        ExtendTracksCB = new javax.swing.JCheckBox();
        DetectTransitionsBT = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("IPOAnalyzer");

        hightlightIPOsBT.setText("hightlight IPOs");
        hightlightIPOsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hightlightIPOsBTActionPerformed(evt);
            }
        });

        ClickToSelectRB.setText("Click to Select IPO");
        ClickToSelectRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClickToSelectRBActionPerformed(evt);
            }
        });

        IPOGaussianFittingBT.setText("IPOGaussian Fitting");
        IPOGaussianFittingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IPOGaussianFittingBTActionPerformed(evt);
            }
        });

        IPOTableTitleLB.setText("IPOs");

        SelectedIPOTitleLB.setText("Selected IPOs");

        IncludeOverlappingIPOsBT.setText("Include overlapping IPOs");
        IncludeOverlappingIPOsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IncludeOverlappingIPOsBTActionPerformed(evt);
            }
        });

        IncludeClustersBT.setText("Include clusters");
        IncludeClustersBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IncludeClustersBTActionPerformed(evt);
            }
        });

        FitStackRB.setText("Fit Stack");
        FitStackRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FitStackRBActionPerformed(evt);
            }
        });

        ImportFittedIPOsBT.setText("import fitted IPOs");
        ImportFittedIPOsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportFittedIPOsBTActionPerformed(evt);
            }
        });

        OverlapCutoffTF.setText("0.1");
        OverlapCutoffTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OverlapCutoffTFActionPerformed(evt);
            }
        });

        jLabel7.setText("OVLP Cutoff");

        StatusLB.setText("Status");

        HighlightChoiceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "centers", "contour2", "contour3", "shape2", "shape3" }));

        SilentFittingRB.setSelected(true);
        SilentFittingRB.setText("silent");
        SilentFittingRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SilentFittingRBActionPerformed(evt);
            }
        });

        jLabel8.setText("slice");

        FirstSliceTF.setText("10");
        FirstSliceTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FirstSliceTFActionPerformed(evt);
            }
        });

        jLabel9.setText("to");

        LastSliceTF.setText("500");

        jLabel10.setText("Threads");

        NumOfThreadTF.setText("2");

        MasterStatusLB.setText("Master status:");

        jLabel12.setText("Fitted Slices");

        AnalyzeSliceLB.setText("Aanalyze slice");

        AnalyzeSliceTF.setText("100");
        AnalyzeSliceTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AnalyzeSliceTFActionPerformed(evt);
            }
        });

        AssociateImageBT.setText("link original img");
        AssociateImageBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AssociateImageBTActionPerformed(evt);
            }
        });

        ShowAssociatedImageRB.setText("show asc imgs");
        ShowAssociatedImageRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowAssociatedImageRBActionPerformed(evt);
            }
        });

        StackSliceRangeLB.setText("10 to 500");

        ViewSelectedIPORB.setText("view selected IPO");
        ViewSelectedIPORB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewSelectedIPORBActionPerformed(evt);
            }
        });

        MonitingIPOSBT.setText("Moniting IPOs");
        MonitingIPOSBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MonitingIPOSBTActionPerformed(evt);
            }
        });

        SelectedIPONumberLB.setText("      ");

        TotalIPONumberLB.setText("      ");

        RegionHeightCutoffLB.setText("HCutoff:");

        TotalSignalCutoffLB.setText("SCutoff:");

        ImportStackIPOTBT.setText("import StackIPOT");
        ImportStackIPOTBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportStackIPOTBTActionPerformed(evt);
            }
        });

        ViewTrackRB.setText("View Tracks");
        ViewTrackRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewTrackRBActionPerformed(evt);
            }
        });

        TrackTableOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AllTracks", "SelectedTracks", "Track Bundles", " " }));
        TrackTableOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TrackTableOptionCBActionPerformed(evt);
            }
        });

        AppendSelectionRB.setText("Append Selection");

        IPOCoordinatesTF.setText("150,150,150");
        IPOCoordinatesTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IPOCoordinatesTFActionPerformed(evt);
            }
        });

        jLabel5.setText("Select IPO");

        RebuildStackIPOsBT.setText("Rebuild StackIPOs");
        RebuildStackIPOsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RebuildStackIPOsBTActionPerformed(evt);
            }
        });

        LinkIPOGsBT.setText("link IPOGs");
        LinkIPOGsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LinkIPOGsBTActionPerformed(evt);
            }
        });

        SelectTrackRB.setText("Select Track");

        TrackPlotOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Peak1", "Peak3", "Peak1 Raw", "Peak3 Raw", "Amp", "SignalCal", "Background", "Height", "Signal", "None", "Area", "BundleTotal", "Drift" }));
        TrackPlotOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TrackPlotOptionCBActionPerformed(evt);
            }
        });

        jLabel6.setText("Plot Option");

        jLabel15.setText("Trk Disp Options");

        jLabel18.setText("Disp Radius");

        TrackImageRadiusTF.setText("10");
        TrackImageRadiusTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TrackImageRadiusTFActionPerformed(evt);
            }
        });

        jLabel19.setText("Mag");

        TrackImageMagTF.setText("6");

        DisplayTrackImageRB.setText("Display Trk Img");
        DisplayTrackImageRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisplayTrackImageRBActionPerformed(evt);
            }
        });

        jLabel16.setText("from");

        TrackImageITF.setText("-5");

        jLabel17.setText("Img blokcs");

        TrackImageBlockDimTF.setText("5x5");

        jLabel20.setText("increment");

        TrackImageIncrementTF.setText("1");

        HighlightTracksOnSeriesImageRB.setText("highlight Tracks");
        HighlightTracksOnSeriesImageRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HighlightTracksOnSeriesImageRBActionPerformed(evt);
            }
        });

        ShowAssociatedImageStackRB.setText("entire stack");
        ShowAssociatedImageStackRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowAssociatedImageStackRBActionPerformed(evt);
            }
        });

        HighlightTracksOnAssociatedImagesRB.setText("on Asso images");
        HighlightTracksOnAssociatedImagesRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HighlightTracksOnAssociatedImagesRBActionPerformed(evt);
            }
        });

        DefaultSettingBT.setText("default setting");
        DefaultSettingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DefaultSettingBTActionPerformed(evt);
            }
        });

        jButton1.setText("front");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        BringTrackPlotsToFront.setText("front");
        BringTrackPlotsToFront.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BringTrackPlotsToFrontActionPerformed(evt);
            }
        });

        MoveToTable2BT.setText("move to table2");
        MoveToTable2BT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MoveToTable2BTActionPerformed(evt);
            }
        });

        RestoreTable2BT.setText("restore table2");
        RestoreTable2BT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RestoreTable2BTActionPerformed(evt);
            }
        });

        LockTable1RB.setText("Lock Table1");
        LockTable1RB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LockTable1RBActionPerformed(evt);
            }
        });

        LockTable2RB.setText("Lock Table2");

        RoiTraceBT.setText("Roi Trace");
        RoiTraceBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RoiTraceBTActionPerformed(evt);
            }
        });

        ShowIPOTBundleRB.setText("show IPOT bundles");

        SaveIPOGTSToABFBT.setText("save trks as abf");
        SaveIPOGTSToABFBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveIPOGTSToABFBTActionPerformed(evt);
            }
        });

        SelectIPOBT.setText("Select");
        SelectIPOBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectIPOBTActionPerformed(evt);
            }
        });

        SelectionTargetOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "IPOGs", "Tracks", "Tracks DF", "NormalShape", "AbnormalShape", "MixedShape" }));
        SelectionTargetOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectionTargetOptionCBActionPerformed(evt);
            }
        });

        jLabel1.setText("Selection Criteria");

        javax.swing.GroupLayout IPOSelectionContainerPaneLayout = new javax.swing.GroupLayout(IPOSelectionContainerPane);
        IPOSelectionContainerPane.setLayout(IPOSelectionContainerPaneLayout);
        IPOSelectionContainerPaneLayout.setHorizontalGroup(
            IPOSelectionContainerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(IPOSelectionContainerPaneLayout.createSequentialGroup()
                .addGroup(IPOSelectionContainerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(IPOSelectionContainerPaneLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(SelectIPOBT)
                        .addGap(18, 18, 18)
                        .addComponent(SelectionTargetOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1))
                    .addGroup(IPOSelectionContainerPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(IPOSelectionChoiceScrollPane)))
                .addContainerGap())
        );
        IPOSelectionContainerPaneLayout.setVerticalGroup(
            IPOSelectionContainerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(IPOSelectionContainerPaneLayout.createSequentialGroup()
                .addGroup(IPOSelectionContainerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SelectIPOBT)
                    .addComponent(SelectionTargetOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(IPOSelectionChoiceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
        );

        IPOGComplexRB.setText("IPOG Complex");

        BuildRWAIPOShapesRB.setText("build RWA IPO Shapes");
        BuildRWAIPOShapesRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BuildRWAIPOShapesRBActionPerformed(evt);
            }
        });

        jLabel2.setText("ws");

        IPORWAShapeWSTF.setText("20");

        ExportTracksBT.setText("Export Tracks");
        ExportTracksBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExportTracksBTActionPerformed(evt);
            }
        });

        jLabel3.setText("rising interval");

        RisingIntervalTF.setText("3");

        ExportLevelInfoBT.setText("Export Level Info");
        ExportLevelInfoBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExportLevelInfoBTActionPerformed(evt);
            }
        });

        ImportLevelInfoBT.setText("Import Level Info");
        ImportLevelInfoBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportLevelInfoBTActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        SortIPOsBT.setText("sort IPOs");
        SortIPOsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SortIPOsBTActionPerformed(evt);
            }
        });

        SimpleIPOGsRB.setText("Display Simple IPOGs");

        ComputeRawPeaksBT.setText("Compute Raw Peaks");
        ComputeRawPeaksBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComputeRawPeaksBTActionPerformed(evt);
            }
        });

        AdjustFirstSliceRB.setText("Adjust First Slice");
        AdjustFirstSliceRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AdjustFirstSliceRBActionPerformed(evt);
            }
        });

        AdjustTrackCentersRB.setText("Adjust Track Centers");
        AdjustTrackCentersRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AdjustTrackCentersRBActionPerformed(evt);
            }
        });

        ExtendTracksCB.setText("Extend Tacks");
        ExtendTracksCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExtendTracksCBActionPerformed(evt);
            }
        });

        DetectTransitionsBT.setText("Detect Transitions");
        DetectTransitionsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DetectTransitionsBTActionPerformed(evt);
            }
        });

        jButton2.setText("Show Selection");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Table2Pane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(SelectedIPOTitleLB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel14)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(SelectedIPONumberLB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(ViewSelectedIPORB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(AppendSelectionRB)
                                        .addGap(18, 18, 18)
                                        .addComponent(MoveToTable2BT)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(hightlightIPOsBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(HighlightChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(IncludeOverlappingIPOsBT)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(IncludeClustersBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(OverlapCutoffTF, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(IPOGaussianFittingBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(SilentFittingRB))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(2, 2, 2)
                                        .addComponent(FitStackRB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(FirstSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(LastSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(NumOfThreadTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel10))
                                    .addComponent(ThreadStatusSP, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ClickToSelectRB)
                                    .addComponent(MonitingIPOSBT)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(ShowAssociatedImageRB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ShowAssociatedImageStackRB))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(ImportFittedIPOsBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(AssociateImageBT))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(AnalyzeSliceLB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(AnalyzeSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(StackSliceRangeLB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(AdjustFirstSliceRB))
                                    .addComponent(IPOSelectionContainerPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(2, 2, 2)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(RoiTraceBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ShowIPOTBundleRB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ExportLevelInfoBT)
                                .addGap(18, 18, 18)
                                .addComponent(ImportLevelInfoBT))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(IPOCoordinatesTF, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(SelectTrackRB)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel6))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(BuildRWAIPOShapesRB)
                                        .addGap(18, 18, 18)
                                        .addComponent(ExportTracksBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(RisingIntervalTF, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(36, 36, 36)
                                        .addComponent(BringTrackPlotsToFront))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(TrackPlotOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ComputeRawPeaksBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton2))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(32, 32, 32)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(RebuildStackIPOsBT)
                                                    .addComponent(ImportStackIPOTBT))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(ViewTrackRB)
                                                    .addComponent(IPOGComplexRB))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(LinkIPOGsBT)
                                                    .addComponent(DefaultSettingBT)))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(77, 77, 77)
                                                .addComponent(jLabel15))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel18)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(TrackImageRadiusTF, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jButton1)
                                                        .addGroup(layout.createSequentialGroup()
                                                            .addComponent(jLabel16)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(TrackImageITF, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addGap(24, 24, 24)
                                                            .addComponent(jLabel17))))
                                                .addGap(8, 8, 8)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(HighlightTracksOnSeriesImageRB)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(HighlightTracksOnAssociatedImagesRB))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel19)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(TrackImageMagTF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(TrackImageBlockDimTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(26, 26, 26)
                                                        .addComponent(jLabel20)
                                                        .addGap(4, 4, 4)
                                                        .addComponent(TrackImageIncrementTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(21, 21, 21)
                                                        .addComponent(ExtendTracksCB))))))
                                    .addComponent(DisplayTrackImageRB, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AdjustTrackCentersRB))))
                    .addComponent(Table1Pane)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(StatusLB)
                                .addGap(58, 58, 58)
                                .addComponent(LockTable1RB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(LockTable2RB)
                                .addGap(33, 33, 33)
                                .addComponent(SaveIPOGTSToABFBT)
                                .addGap(30, 30, 30)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(IPORWAShapeWSTF, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(48, 48, 48)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SortIPOsBT))
                            .addComponent(MasterStatusLB, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(IPOTableTitleLB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TotalIPONumberLB)
                                .addGap(158, 158, 158)
                                .addComponent(RegionHeightCutoffLB)
                                .addGap(98, 98, 98)
                                .addComponent(TotalSignalCutoffLB)
                                .addGap(88, 88, 88)
                                .addComponent(TrackTableOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(RestoreTable2BT)
                                .addGap(18, 18, 18)
                                .addComponent(SimpleIPOGsRB)
                                .addGap(44, 44, 44)
                                .addComponent(DetectTransitionsBT)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hightlightIPOsBT)
                            .addComponent(HighlightChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(IncludeOverlappingIPOsBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(IncludeClustersBT)
                            .addComponent(jLabel7)
                            .addComponent(OverlapCutoffTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(IPOGaussianFittingBT)
                            .addComponent(SilentFittingRB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(FitStackRB)
                            .addComponent(jLabel8)
                            .addComponent(FirstSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(LastSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NumOfThreadTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ThreadStatusSP, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ImportStackIPOTBT)
                            .addComponent(ViewTrackRB)
                            .addComponent(DefaultSettingBT))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(RebuildStackIPOsBT)
                            .addComponent(LinkIPOGsBT)
                            .addComponent(IPOGComplexRB))
                        .addGap(13, 13, 13)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(TrackImageITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TrackImageBlockDimTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel20)
                            .addComponent(TrackImageIncrementTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ExtendTracksCB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(TrackImageRadiusTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19)
                            .addComponent(TrackImageMagTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ComputeRawPeaksBT)
                            .addComponent(jButton2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(RoiTraceBT)
                            .addComponent(ShowIPOTBundleRB)
                            .addComponent(ExportLevelInfoBT)
                            .addComponent(ImportLevelInfoBT))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DisplayTrackImageRB)
                            .addComponent(jButton1)
                            .addComponent(HighlightTracksOnSeriesImageRB)
                            .addComponent(HighlightTracksOnAssociatedImagesRB)
                            .addComponent(AdjustTrackCentersRB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(IPOCoordinatesTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SelectTrackRB)
                            .addComponent(jLabel6)
                            .addComponent(BringTrackPlotsToFront)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(IPOSelectionContainerPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7)
                        .addComponent(ClickToSelectRB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ImportFittedIPOsBT)
                            .addComponent(AssociateImageBT))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AnalyzeSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AnalyzeSliceLB)
                            .addComponent(StackSliceRangeLB)
                            .addComponent(AdjustFirstSliceRB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ShowAssociatedImageRB)
                            .addComponent(ShowAssociatedImageStackRB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MonitingIPOSBT)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(SelectedIPOTitleLB)
                        .addComponent(jLabel14)
                        .addComponent(ViewSelectedIPORB)
                        .addComponent(AppendSelectionRB)
                        .addComponent(MoveToTable2BT)
                        .addComponent(SelectedIPONumberLB))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(BuildRWAIPOShapesRB)
                        .addComponent(ExportTracksBT)
                        .addComponent(jLabel3)
                        .addComponent(RisingIntervalTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TrackPlotOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Table1Pane, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(IPOTableTitleLB)
                    .addComponent(TotalIPONumberLB)
                    .addComponent(RestoreTable2BT)
                    .addComponent(TrackTableOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TotalSignalCutoffLB)
                    .addComponent(RegionHeightCutoffLB)
                    .addComponent(SimpleIPOGsRB)
                    .addComponent(DetectTransitionsBT))
                .addGap(10, 10, 10)
                .addComponent(Table2Pane, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(StatusLB)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(LockTable1RB)
                        .addComponent(LockTable2RB)
                        .addComponent(SaveIPOGTSToABFBT)
                        .addComponent(jLabel2)
                        .addComponent(IPORWAShapeWSTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(SortIPOsBT)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MasterStatusLB)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void hightlightIPOsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hightlightIPOsBTActionPerformed
        // TODO add your handling code here:
        highlightSelectedIPOs();
    }//GEN-LAST:event_hightlightIPOsBTActionPerformed

    private void IPOGaussianFittingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IPOGaussianFittingBTActionPerformed
        // TODO add your handling code here:
        boolean fitStack=FitStackRB.isSelected();
        String path;
        if(fitStack){
                Thread m_cFittingThread=new Thread(new Runnable() {
                    public void run() {
                        int i,nFiles=0;
                        ArrayList<String> svPaths=new ArrayList();
                        ArrayList<String> svTypes=new ArrayList(),svExts=new ArrayList();
                        svTypes.add("a tiff file");
                        svTypes.add("a file path list");
                        svExts.add("tif");
                        svExts.add("txt");
                        
                        String path=FileAssist.getFilePath("importing an image file or a file containing a list of paths", FileAssist.defaultDirectory, svTypes, svExts, true);
                        String logPath=FileAssist.changeExt(path, "log");
                        Formatter fmLog=null;

                        if(FileAssist.getFileExtention(path).contentEquals("tif"))
                            svPaths.add(path);
                        else{
                            File f=new File(path);
                            try{
                                    FileInputStream fs=new FileInputStream(f);
                                    DataInputStream ds=new DataInputStream(fs);
                                    String line=ds.readLine();
                                    StringTokenizer stk=new StringTokenizer(line);
                                    nFiles=Integer.parseInt(stk.nextToken());
                                    for(i=0;i<nFiles;i++){
                                        svPaths.add(ds.readLine());
                                    }
                            }
                            catch(FileNotFoundException e){
                                    IJ.error("File Not Found in IPOGaussianFittingBTActionPerformed");
                            }
                            catch(IOException e){
                                    IJ.error("IOException in IPOGaussianFittingBTActionPerformed");
                            }
                        }
                        fmLog=PrintAssist.getQuickFormatter(logPath);
                        nFiles=svPaths.size();
                        for (i=0;i<nFiles;i++){
                            path=svPaths.get(i);
                            if(!FileAssist.fileExists(path)) {
                                PrintAssist.printString(fmLog, "File Not Found: "+path);
                                continue;
                            }
                            if(FileAssist.fileExists(FileAssist.changeExt(path, "IGN"))) continue;
                            try{fitIPOGaussian_Stack(path);}
                            catch (FileNotFoundException e){
                            }
                            catch (IOException e){
                            }
                        }
                        if(fmLog!=null)fmLog.close();
                    }
                });
                m_cFittingThread.start();
       } else
            fitIPOGaussian_CurrentSlice(WindowManager.getCurrentImage());

    }//GEN-LAST:event_IPOGaussianFittingBTActionPerformed
    public void fitIPOGaussian_Stack(String path) throws FileNotFoundException,IOException{
        importAndSetOriginalImage(path);
        ImagePlus impl=implOriginal;

        ImagePlus implCompen=CommonMethods.cloneImage(impl);
        ImagePlus implp=CommonMethods.cloneImage(impl);

        int peakSlice=CommonMethods.getIntesityPeakSliceIndex(impl);
//        int sliceI=Integer.parseInt(FirstSliceTF.getText()),sliceF=Math.max(Integer.parseInt(LastSliceTF.getText()), impl.getNSlices()),slice;
        int sliceI=peakSlice,sliceF=impl.getNSlices(),slice;
        FirstSliceTF.setText(""+sliceI);
        LastSliceTF.setText(""+sliceF);

        int nNumThreads=Integer.parseInt(NumOfThreadTF.getText());
        int slices=sliceF-sliceI+1;
        if(nNumThreads>slices) nNumThreads=slices;
        SliceGaussianIPOFitter pcIPOFitters[]=new SliceGaussianIPOFitter[slices];
        ArrayList<IPOGaussianNode> pcvIPOs[]=new ArrayList[slices];
        double[] pdHCutoff=new double[slices],pdSCutoff=new double[slices];
        path=FileAssist.changeExt(sOriginalImagePath, "IGN");
//        String path=FileAssist.getFilePath("output path for IPOGaussianNodes for the stack", FileAssist.defaultDirectory, "IPOGaussianNode file", "IGN", false);
        File f=new File(path);
        long startTime=System.currentTimeMillis();
        long endTime=System.currentTimeMillis();
        for(int t=0;t<nNumThreads;t++){
            pcIPOFitters[t]=new SliceGaussianIPOFitter(impl,implCompen,implp,sliceI,sliceF,nNumThreads,t,this,pcvIPOs,pdHCutoff,pdSCutoff,"Thread"+t);
        }
        String status;
        FileOutputStream fo=new FileOutputStream(f);
        DataOutputStream df=new DataOutputStream(fo);
        int currentSlice=sliceI;
        ArrayList<IPOGaussianNode> IPOs;
        df.writeInt(slices);
        int nNumAdditionalParsPerIPO=6;
        df.writeInt(nNumAdditionalParsPerIPO);
        int index;

        double RegionHeightCutoff,TotalSignalCutoff;
        while(currentSlice<=sliceF){
            index=currentSlice-sliceI;
            IPOs=pcvIPOs[index];
            while(IPOs==null){
                try{Thread.sleep(500);}
                catch(Exception e){}
                IPOs=pcvIPOs[index];
            }
            endTime=System.currentTimeMillis();
            status="Saving slice "+currentSlice+" elapsed time(s): "+PrintAssist.ToString((endTime-startTime)/1000, 1);
            MasterStatusLB.setText(status);
            df.writeInt(currentSlice);
            df.writeInt(IPOs.size());
            df.writeDouble(pdHCutoff[index]);
            df.writeDouble(pdSCutoff[index]);
            IPOGaussianNodeHandler.exportIPOs(df,IPOs,nNumAdditionalParsPerIPO);
            df.flush();
            currentSlice++;
            updateFittedSliceTable(index);
        }
        df.close();
        fo.close();
        endTime=System.currentTimeMillis();
        status="Finished! elapsed time(s): "+PrintAssist.ToString((endTime-startTime)/1000, 1);
        CommonMethods.saveImage(implCompen, FileAssist.getExtendedFileName(sOriginalImagePath," -compensated image"));
        CommonMethods.saveImage(implp, FileAssist.getExtendedFileName(sOriginalImagePath," -processed image"));
        MasterStatusLB.setText(status);
    }

    public void importFittedIPOs()throws FileNotFoundException,IOException{
        String path=FileAssist.getFilePath("import a fitted GaussinaNode file", FileAssist.defaultDirectory, "IGN file", "IGN", true);
        importFittedIPOs(path);
    }
    public void importFittedIPOs(String path)throws FileNotFoundException,IOException{
        m_cStackIPOs=IPOGaussianNodeHandler.importFittedStackIPOs(path);
        int firstSlice=m_cStackIPOs.sliceI,lastSlice=m_cStackIPOs.sliceF;
        m_cvIPONodes=m_cStackIPOs.getIPOGs(lastSlice);
        StackSliceRangeLB.setText("Slice "+firstSlice+" to "+lastSlice);
        AnalyzeSliceTF.setText(""+lastSlice);
    }
    public void fitIPOGaussian_CurrentSlice(final ImagePlus impl){
        boolean silent=SilentFittingRB.isSelected();
        final IPOGaussianFitter fitter=new IPOGaussianFitter(silent);
        fitter.addFitterListener(this);
            Thread m_cFittingThread=new Thread(new Runnable() {
                public void run() {
                    fitter.fitImage(impl);
                    implComposed=CommonMethods.cloneImage(fitter.implComposed);
                    implComposed.setTitle("Composed Image");
                    implInt=CommonMethods.cloneImage(fitter.implInt);
                    implInt.setTitle("Integrated Image");
//                    CommonMethods.closeAllImages();
                    implComposed.show();
                    implInt.show();
                    overlappingCutoff=Double.parseDouble(OverlapCutoffTF.getText());
                    handler=new IPOGaussianNodeHandler(fitter.m_cvRegionNodes,fitter.m_cvComplexNodes,overlappingCutoff,implComposed.getWidth(),implComposed.getHeight());
                    updateIPOs(handler.m_cvIPOGaussianNodes);
                }
            });
            m_cFittingThread.start();
    }
    private void SelectIPOBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectIPOBTActionPerformed
        // TODO add your handling code here:
        if(((String)SelectionTargetOptionCB.getSelectedItem()).contentEquals("IPOGs")){
            selectIPOs();
            updateSelectedIPOTable(m_cvSelectedIPOs);
        }else{
            selectTracks();
            showSelectedTracks();
        }
    }//GEN-LAST:event_SelectIPOBTActionPerformed
    public int showSelectedTracks(){
//        TrackTableOptionCB.setSelectedItem("SelectedTracks");
        updateTrackTable("Selected Tracks");
        return 1;
    }
    public void selectTracks_Default(boolean bTraining){
        m_cStackIPOT.selectTracks_Deltault();        
        ArrayList<IPOGTrackNode> IPOGTs=m_cStackIPOT.m_cvSelectedIPOGTracks;
//        buildHeadIPOGs(IPOGTs);
        String option=(String)TrackPlotOptionCB.getSelectedItem();
//        DoubleRange cHRange=calHeadValueRange(IPOGTs,option);
//        double cutoff=Math.max(0.20*cHRange.getMax(),2.5*cHRange.getMin());
        double pV=0.001;
        double cutoff=4*getHeightCutoff(m_cStackIPOT.sliceI,pV);
//        double cutoff=Math.max(0.20*cHRange.getMax(),1.*cHRange.getMin());
//        cutoff=0;//12n13
        int i,len=m_cStackIPOT.m_cvSelectedIPOGTracks.size();
        IPOGTrackNode IPOGT;
        for(i=0;i<len;i++){
            IPOGT=m_cStackIPOT.m_cvSelectedIPOGTracks.get(len-1-i);
            if(!IPOGT.normalTrackHeadShape()&&!bTraining){
//                m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
//                continue;
            }
            if(IPOGT.getHeadValueAve(option)<cutoff) 
                m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
        }
        m_cvSelectedIPOGTracks=new ArrayList();
        m_cStackIPOT.copySelectedTracks(m_cvSelectedIPOGTracks);
        showSelectedTracks();
    }
    public void selectTracks_NormalShape(){
        m_cStackIPOT.selectTracks_Deltault();        
        ArrayList<IPOGTrackNode> IPOGTs=m_cStackIPOT.m_cvSelectedIPOGTracks;
        String option=(String)TrackPlotOptionCB.getSelectedItem();
        int i,len=m_cStackIPOT.m_cvSelectedIPOGTracks.size();
        IPOGTLevelInfoNode aInfo;
        IPOGTrackNode IPOGT;
        for(i=0;i<len;i++){
            IPOGT=m_cStackIPOT.m_cvSelectedIPOGTracks.get(len-1-i);
            IPOGT.activateLevelInfo(option);
            aInfo=IPOGT.m_cLevelInfo;
            if(aInfo==null) {
                m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
                continue;
            }
            if(aInfo.isNormalShape(1)) continue;
            m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
        }
        m_cvSelectedIPOGTracks=new ArrayList();
        m_cStackIPOT.copySelectedTracks(m_cvSelectedIPOGTracks);
        showSelectedTracks();
    }
    public void selectTracks_AbnormalShape(){
        m_cStackIPOT.selectTracks_Deltault();        
        ArrayList<IPOGTrackNode> IPOGTs=m_cStackIPOT.m_cvSelectedIPOGTracks;
        String option=(String)TrackPlotOptionCB.getSelectedItem();
        int i,len=m_cStackIPOT.m_cvSelectedIPOGTracks.size();
        IPOGTLevelInfoNode aInfo;
        IPOGTrackNode IPOGT;
        for(i=0;i<len;i++){
            IPOGT=m_cStackIPOT.m_cvSelectedIPOGTracks.get(len-1-i);
            IPOGT.activateLevelInfo(option);
            aInfo=IPOGT.m_cLevelInfo;
            if(aInfo==null) {
                m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
                continue;
            }
            if(aInfo.isNormalShape(2)) m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
        }
        m_cvSelectedIPOGTracks=new ArrayList();
        m_cStackIPOT.copySelectedTracks(m_cvSelectedIPOGTracks);
        showSelectedTracks();
    }
    public void selectTracks_MixedShape(){
        m_cStackIPOT.selectTracks_Deltault();        
        ArrayList<IPOGTrackNode> IPOGTs=m_cStackIPOT.m_cvSelectedIPOGTracks;
        String option=(String)TrackPlotOptionCB.getSelectedItem();
        int i,len=m_cStackIPOT.m_cvSelectedIPOGTracks.size();
        IPOGTLevelInfoNode aInfo;
        IPOGTrackNode IPOGT;
        for(i=0;i<len;i++){
            IPOGT=m_cStackIPOT.m_cvSelectedIPOGTracks.get(len-1-i);
            IPOGT.activateLevelInfo(option);
            aInfo=IPOGT.m_cLevelInfo;
            if(aInfo==null) {
                m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
                continue;
            }
            if(!aInfo.isNormalShape(-1)) m_cStackIPOT.m_cvSelectedIPOGTracks.remove(IPOGT);
        }
        m_cvSelectedIPOGTracks=new ArrayList();
        m_cStackIPOT.copySelectedTracks(m_cvSelectedIPOGTracks);
        showSelectedTracks();
    }
    void appendFittedTracks(){
        ArrayList<IPOGTrackNode> cvIPOGTs=m_cStackIPOT.m_cvIPOGTracks,cvSelectedTracks=m_cStackIPOT.m_cvSelectedIPOGTracks;        
        IPOGTrackNode IPOGT;
        int i,len=cvIPOGTs.size(),len1=cvSelectedTracks.size(),j;
        boolean selected=false;
        for(i=0;i<len;i++){
            IPOGT=cvIPOGTs.get(i);
            if(IPOGT.m_cvLevelInfoNodes==null) continue;
            if(IPOGT.m_cvLevelInfoNodes.isEmpty()) continue;
            selected=false;
            for(j=0;j<len1;j++){
                if(IPOGT==cvSelectedTracks.get(j)){
                    selected=true;
                    break;
                }
            }
            if(!selected) cvSelectedTracks.add(IPOGT);
        }
    }
    private void ClickToSelectRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClickToSelectRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ClickToSelectRBActionPerformed

    private void IncludeOverlappingIPOsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncludeOverlappingIPOsBTActionPerformed
        // TODO add your handling code here:
        includeOverlappingIPOs();
    }//GEN-LAST:event_IncludeOverlappingIPOsBTActionPerformed

    private void IncludeClustersBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncludeClustersBTActionPerformed
        // TODO add your handling code here:
        includeIPOsInClusters();
    }//GEN-LAST:event_IncludeClustersBTActionPerformed

    private void OverlapCutoffTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OverlapCutoffTFActionPerformed
        // TODO add your handling code here:
        double cutoff=Double.parseDouble(OverlapCutoffTF.getText());
        if(m_cStackIPOs!=null){
            calStackOverlap(cutoff);
        }else{
            IPOGaussianNodeHandler handler=new IPOGaussianNodeHandler();
            handler.setOverlapCutoff(cutoff);
            if(m_cvIPONodes!=null) {
                handler.updateIPOGaussianNodes(m_cvIPONodes);
                m_cvIPONodes=handler.m_cvIPOGaussianNodes;
            }
        }
        if(m_cvIPONodes!=null) updateIPOs(m_cvIPONodes);
    }//GEN-LAST:event_OverlapCutoffTFActionPerformed

    int calStackOverlap(double cutoff){
        if(m_cStackIPOs==null) return -1;
        IPOGaussianNodeHandler handler=new IPOGaussianNodeHandler();
        handler.setOverlapCutoff(cutoff);
        int i,len=m_cStackIPOs.nSlices;
        for(i=0;i<len;i++){
            handler.updateIPOGaussianNodes(m_cStackIPOs.SliceIPOs.get(i).IPOs);
        }
        return 1;
    }
    private void SilentFittingRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SilentFittingRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SilentFittingRBActionPerformed

    private void ImportFittedIPOsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportFittedIPOsBTActionPerformed
        // TODO add your handling code here:
        try{importFittedIPOs();}
        catch(FileNotFoundException e){}
        catch(IOException e){}
        updateIPOTable();
    }//GEN-LAST:event_ImportFittedIPOsBTActionPerformed

    private void AnalyzeSliceTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AnalyzeSliceTFActionPerformed
        // TODO add your handling code here:
        int slice=Integer.parseInt(AnalyzeSliceTF.getText());
        if(AdjustFirstSliceRB.isSelected()){
            m_cStackIPOT.setFirstSlice(slice);
            AdjustFirstSliceRB.setSelected(false);
        } else
            analyzeSlice(slice);
    }//GEN-LAST:event_AnalyzeSliceTFActionPerformed

    private void AssociateImageBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AssociateImageBTActionPerformed
        // TODO add your handling code here:
        setOriginalImage(WindowManager.getCurrentImage());
    }//GEN-LAST:event_AssociateImageBTActionPerformed
    void setOriginalImage(ImagePlus impl){
        if(implOriginal!=null) implOriginal.getWindow().getCanvas().removePaintListener(this);
        implOriginal=impl;
        implOriginal.getWindow().getCanvas().addPaintListener(this);
        CommonGuiMethods.addMouseListener(implOriginal, this);

        int nSlices=implOriginal.getNSlices();
        this.setTitle(this.getTitle()+": "+implOriginal.getTitle());
    }
    void importAndSetOriginalImage(String path){
        sOriginalImagePath=path;
//        sOriginalImagePath=FileAssist.getFilePath("importing original image", FileAssist.defaultDirectory, "tif file", "tif", true);
        implOriginal=CommonMethods.importImage(sOriginalImagePath);
        implOriginal.show();
        setOriginalImage(implOriginal);
//        setStackPixels();
    }
    void showFittingSliceRange(){
        int nSlices=implOriginal.getNSlices(),slice0=implOriginal.getCurrentSlice();
        int peakSlice=CommonMethods.getIntesityPeakSliceIndex(implOriginal)+1;
        implOriginal.setSlice(slice0);
        FirstSliceTF.setText(""+peakSlice);
        LastSliceTF.setText(""+nSlices);
    }
    private void ShowAssociatedImageRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowAssociatedImageRBActionPerformed
        // TODO add your handling code here:
        if(ShowAssociatedImageRB.isSelected())
            showAssociatedImages();
        else
            closeAssociatedImages();
    }//GEN-LAST:event_ShowAssociatedImageRBActionPerformed

    private void FirstSliceTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FirstSliceTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FirstSliceTFActionPerformed

    private void MonitingIPOSBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MonitingIPOSBTActionPerformed
        // TODO add your handling code here:
        monitingIPOs();
    }//GEN-LAST:event_MonitingIPOSBTActionPerformed

    private void ImportStackIPOTBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportStackIPOTBTActionPerformed
        // TODO add your handling code here:
        importStackIPOT();
    }//GEN-LAST:event_ImportStackIPOTBTActionPerformed

    private void ViewTrackRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewTrackRBActionPerformed
        // TODO add your handling code here:
        viewIPOsOrTracks();
    }//GEN-LAST:event_ViewTrackRBActionPerformed

    private void IPOCoordinatesTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IPOCoordinatesTFActionPerformed
        // TODO add your handling code here:
        String st=IPOCoordinatesTF.getText();
        StringTokenizer stk=new StringTokenizer(st," ,");
        int x=Integer.parseInt(stk.nextToken()),y=Integer.parseInt(stk.nextToken()),z=Integer.parseInt(stk.nextToken());
        ArrayList<IPOGaussianNode> IPOGs=selectIPOG(x,y,z);
        updateSelectedIPOTable(IPOGs);
    }//GEN-LAST:event_IPOCoordinatesTFActionPerformed

    private void RebuildStackIPOsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RebuildStackIPOsBTActionPerformed
        // TODO add your handling code here:
        rebuildStackIPOs();
    }//GEN-LAST:event_RebuildStackIPOsBTActionPerformed

    private void LinkIPOGsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LinkIPOGsBTActionPerformed
        // TODO add your handling code here:
        linkIPOGs();
    }//GEN-LAST:event_LinkIPOGsBTActionPerformed

    private void SelectionTargetOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectionTargetOptionCBActionPerformed
        // TODO add your handling code here:
        updateSelectionCriteria();
    }//GEN-LAST:event_SelectionTargetOptionCBActionPerformed

    private void TrackImageRadiusTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TrackImageRadiusTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TrackImageRadiusTFActionPerformed

    private void DisplayTrackImageRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisplayTrackImageRBActionPerformed
        if(DisplayTrackImageRB.isSelected()){
            createTrackDisplayer();
            m_cIPOGTOnSeriesImage=m_cSelectedIPOGT;
            displayIPOGTOnSeriesImage(m_cSelectedIPOGT);
        }
    }//GEN-LAST:event_DisplayTrackImageRBActionPerformed
    void displayIPOGTOnSeriesImage(IPOGTrackNode IPOGT){
        if(m_cTrackDisplayer==null) createTrackDisplayer();
        m_cIPOGTOnSeriesImage=IPOGT;
        if(IPOGT!=null) m_cTrackDisplayer.displayIPOGTrack(IPOGT);
    }
    private void HighlightTracksOnSeriesImageRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HighlightTracksOnSeriesImageRBActionPerformed
        // TODO add your handling code here:
        if(m_cTrackDisplayer!=null&&HighlightTracksOnSeriesImageRB.isSelected()){
            highlightTracksOnSeriesImage();
        }
    }//GEN-LAST:event_HighlightTracksOnSeriesImageRBActionPerformed

    private void HighlightTracksOnAssociatedImagesRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HighlightTracksOnAssociatedImagesRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_HighlightTracksOnAssociatedImagesRBActionPerformed

    private void DefaultSettingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DefaultSettingBTActionPerformed
        // TODO add your handling code here:
        defaultInit();
    }//GEN-LAST:event_DefaultSettingBTActionPerformed

    private void ShowAssociatedImageStackRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowAssociatedImageStackRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ShowAssociatedImageStackRBActionPerformed

    private void ViewSelectedIPORBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewSelectedIPORBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ViewSelectedIPORBActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        implSeries.getWindow().toFront();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void BringTrackPlotsToFrontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BringTrackPlotsToFrontActionPerformed
        // TODO add your handling code here:
        int i,len=m_cvIPOGTPWs.size();
        for(i=0;i<len;i++){
            m_cvIPOGTPWs.get(i).pw.toFront();
        }
    }//GEN-LAST:event_BringTrackPlotsToFrontActionPerformed

    private void MoveToTable2BTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MoveToTable2BTActionPerformed
        // TODO add your handling code here:
        displayTable1OnTable2();
    }//GEN-LAST:event_MoveToTable2BTActionPerformed

    private void RestoreTable2BTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RestoreTable2BTActionPerformed
        // TODO add your handling code here:
        restoreTable2();
    }//GEN-LAST:event_RestoreTable2BTActionPerformed

    private void LockTable1RBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LockTable1RBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LockTable1RBActionPerformed

    private void TrackPlotOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TrackPlotOptionCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TrackPlotOptionCBActionPerformed

    private void RoiTraceBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RoiTraceBTActionPerformed
        // TODO add your handling code here:
        ImagePlus impl=WindowManager.getCurrentImage();
        drawRoiTrace(impl);
    }//GEN-LAST:event_RoiTraceBTActionPerformed

    private void SaveIPOGTSToABFBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveIPOGTSToABFBTActionPerformed
        // TODO add your handling code here:
        ArrayList<IPOGTrackNode> IPOGTracks=m_cStackIPOT.m_cvIPOGTracks;
        String option=(String)TrackTableOptionCB.getSelectedItem();
        if(option.contentEquals("SelectedTracks")) IPOGTracks=m_cvSelectedIPOGTracks;
        m_cStackIPOT.exportTracksToAbf_GaussianNodeGroup(FileAssist.changeExt(sOriginalImagePath, "dat"), IPOGTracks);
    }//GEN-LAST:event_SaveIPOGTSToABFBTActionPerformed

    private void TrackTableOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TrackTableOptionCBActionPerformed
        // TODO add your handling code here:
        updateTrackTable();
    }//GEN-LAST:event_TrackTableOptionCBActionPerformed

    private void FitStackRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FitStackRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FitStackRBActionPerformed

    private void ExportTracksBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExportTracksBTActionPerformed
        exportTracks();
    }//GEN-LAST:event_ExportTracksBTActionPerformed

    private void ExportLevelInfoBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExportLevelInfoBTActionPerformed
        // TODO add your handling code here:
        exportLevelInfo();
    }//GEN-LAST:event_ExportLevelInfoBTActionPerformed

    private void ImportLevelInfoBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportLevelInfoBTActionPerformed
        // TODO add your handling code here:
        importLevelInfo();
    }//GEN-LAST:event_ImportLevelInfoBTActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void SortIPOsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SortIPOsBTActionPerformed
        int i,len=m_cvIPONodes.size();
        /*
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        for(i=0;i<len;i++){
            IPOGs.add(m_cvIPONodes.get(i));
        }
         *
         */
        IPOGaussianNodeHandler.sortIPOGs(m_cvIPONodes,(String)TrackPlotOptionCB.getSelectedItem());
        m_cIPOTable=IPOGTLevelTransitionAnalyzer.buildCommonTable(m_cvIPONodes, SimpleIPOGsRB.isSelected());
        m_cIPOTable.getModel().addTableModelListener(this);
        JViewport jvp=new JViewport();
        jvp.setView(m_cIPOTable);
        Table2Pane.setViewport(jvp);
    }//GEN-LAST:event_SortIPOsBTActionPerformed

    private void ComputeRawPeaksBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComputeRawPeaksBTActionPerformed
        // TODO add your handling code here:
        computeRawPeaks();
    }//GEN-LAST:event_ComputeRawPeaksBTActionPerformed

    private void AdjustFirstSliceRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AdjustFirstSliceRBActionPerformed
        if(AdjustFirstSliceRB.isSelected())
            AnalyzeSliceLB.setText("Adjust First Slice");
        else
            AnalyzeSliceLB.setText("Analyze Slice");
    }//GEN-LAST:event_AdjustFirstSliceRBActionPerformed

    private void AdjustTrackCentersRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AdjustTrackCentersRBActionPerformed
        // TODO add your handling code here:
        m_cTrackDisplayer.adjustDisplayingCenter(AdjustTrackCentersRB.isSelected());
    }//GEN-LAST:event_AdjustTrackCentersRBActionPerformed

    private void ExtendTracksCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExtendTracksCBActionPerformed
        // TODO add your handling code here:
        extendIPOGTracks();
    }//GEN-LAST:event_ExtendTracksCBActionPerformed

    private void BuildRWAIPOShapesRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BuildRWAIPOShapesRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BuildRWAIPOShapesRBActionPerformed

    private void DetectTransitionsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DetectTransitionsBTActionPerformed
        // TODO add your handling code here:
        setInteractive(false);
        detectTransitionsInSelectedTracks();
    }//GEN-LAST:event_DetectTransitionsBTActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        showSelection();
    }//GEN-LAST:event_jButton2ActionPerformed
    int drawRoiTrace(ImagePlus impl){
        Roi roi=impl.getRoi();
        if(roi==null) return -1;
        if(impl==implSeries){
            Rectangle r=roi.getBoundingRect();
            Point pt=new Point(r.x,r.y);
            Point pto=m_cTrackDisplayer.getOriginalCoordinates(pt.x, pt.y);
            int shiftX=pto.x-pt.x,shiftY=pto.y-pt.y;
            intRange sliceRange=m_cTrackDisplayer.getSliceRange();
            CommonGuiMethods.drawRoiMeanTrace(implOriginal,roi,sliceRange.getMin(),sliceRange.getMax(),shiftX,shiftY);
        }else{
            CommonGuiMethods.drawRoiMeanTrace(impl,roi);
        }
        return 1;
    }
    void displayTable1OnTable2(){
        m_cStoredTable2Viewport=m_cTable2Viewport;
        showTable(m_cTable1Viewport,1);
    }
    void restoreTable2(){
        if(m_cTable2Viewport!=null) Table2Pane.setViewport(m_cStoredTable2Viewport);
    }
    int defaultInit(){
//        String path=FileAssist.getFilePath("input the original image", FileAssist.defaultDirectory, "a tif file", "tif", true);

        utilities.Gui.Dialogs.PhotobleachingImagePathDialog dlg=new utilities.Gui.Dialogs.PhotobleachingImagePathDialog(new javax.swing.JFrame(), true);
        String path=dlg.getImagePath();
        importAndSetOriginalImage(path);
        if(implOriginal==null) return -1;
        pixelRange=CommonMethods.getPixelRange(implOriginal);

        path=FileAssist.changeExt(sOriginalImagePath, "TLH");
        if(FileAssist.fileExists(path)){
            importStackIPOT_LevelInfo(path);
            m_cStackIPOT.checkTrackindexes();
        } else {
            path = FileAssist.changeExt(sOriginalImagePath, "TRK");
            importStackIPOT(path);
        }
//        m_cStackIPOT.setFirstSlice(m_cStackIPOT.sliceI+1);//running of this method causes inconsistency between trackindexes and the positions of the track in the array, m_cStackIPOT.m_cIPOGTracks
        //this can be circomvented by either reassign the track indexes or not removing empty tracks in this method.
        m_cStackIPOT.checkTrackindexes();
        rebuildStackIPOs();
//        linkIPOGs();
        m_cStackIPOT.checkTrackindexes();
        if(implOriginal.getNSlices()>1) ShowAssociatedImageStackRB.setSelected(true);
        ShowAssociatedImageRB.setSelected(true);
        ShowAssociatedImageRBActionPerformed(null);
        m_cViewer.SynchronizeSlice();
        m_cStackIPOT.checkTrackindexes();

        m_cViewer.setMagnification(4);
        implOriginal.getWindow().getCanvas().setSrcRect(new Rectangle(0,0,25,25));
        m_cViewer.synchronizeDisplay();
        m_cViewer.setContrastChoice(implInt, ImageComparisonViewer.LocalPixelRange);
        m_cViewer.updateAssociatedImages();
        implAssociated=m_cViewer.getAssociatedImage();

        ViewSelectedIPORB.setSelected(true);
        ClickToSelectRB.setSelected(true);
        SelectTrackRB.setSelected(true);
        IPOGTrackNode IPOGT=m_cStackIPOT.m_cvIPOGTracks.get(0);
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        IPOGs.add(IPOGT.m_cvIPOGs.get(0));
        updateSelectedIPOTable(IPOGs);
        DisplayTrackImageRB.setSelected(true);
        DisplayTrackImageRBActionPerformed(null);

        m_cViewer.setMagnification(implAssociated, 8);
        m_cViewer.setMagnification(implSeries, 4);
        buildHeightCutoff();
        showHeightCutoff();
        HighlightTracksOnSeriesImageRB.setSelected(true);
        HighlightTracksOnSeriesImageRBActionPerformed(null);
        HighlightTracksOnAssociatedImagesRB.setSelected(true);
        CommonGuiMethods.optimizeWindowSize(implSeries);
        CommonGuiMethods.optimizeWindowSize(implAssociated);
        m_cStackIPOT.checkTrackindexes();
        extendIPOGTracks();
        computeRawPeaks();
        TrackPlotOptionCB.setSelectedItem("Peak3 Raw");
        plotTrack(m_cSelectedIPOGT,IPOGTLevelTransitionAnalyzer.getMaxRizingInterval((String)TrackPlotOptionCB.getSelectedItem()));
        selectTracks_Default(false);
        return 1;
    }
    
    int createTrackDisplayer(){
        if(implOriginal==null) return -1;
        int rows,cols,frameShift,displayRadius;
        String st=TrackImageRadiusTF.getText();
        displayRadius=Integer.parseInt(st);
        StringTokenizer stk=new StringTokenizer(TrackImageBlockDimTF.getText(),"xX");
        rows=Integer.parseInt(stk.nextToken());
        cols=Integer.parseInt(stk.nextToken());
        double mag=Double.parseDouble(TrackImageMagTF.getText());
        int increment=Integer.parseInt(TrackImageIncrementTF.getText());
        frameShift=Integer.parseInt(TrackImageITF.getText());
        m_cTrackDisplayer=new ImageSeriesDisplayer("Track Image of"+implOriginal.getTitle(),implOriginal,rows,cols,frameShift,increment,displayRadius,displayRadius,mag);
        implSeries=m_cTrackDisplayer.getDisplayImage();
        implSeries.getWindow().getCanvas().addMouseListener(this);
        implSeries.getWindow().getCanvas().addMouseMotionListener(this);
        implSeries.getWindow().getCanvas().addPaintListener(this);
        return 1;
    }
    void updateSelectionCriteria(){
        setSelector();
    }
    int updateTrackSelectionChoices(){
        if(m_cStackIPOT==null) return -1;
        m_cStackIPOT.calParRanges();
        m_cIPOGTSelector.updateIPOGTSelectionChoice(m_cStackIPOT,(String)TrackPlotOptionCB.getSelectedItem());
        return 1;
    }
    int selectTracks(){
        if(m_cStackIPOT==null) return -1;
        m_cStackIPOT.calParRanges();
        m_cvSelectedIPOGTracks.clear();
        m_cIPOGTSelector.selectTracks((String)TrackPlotOptionCB.getSelectedItem());
        m_cvSelectedIPOGTracks=m_cIPOGTSelector.getSelectedTracks();
        return 1;
    }
    int linkIPOGs(){
        if(m_cStackIPOT==null||m_cStackIPOs==null) return -1;
        m_cStackIPOT.calSliceRange();
        int sliceI=m_cStackIPOT.sliceI,sliceF=m_cStackIPOT.sliceF,slice,i,len,j;
        if(m_cStackIPOs.sliceI!=sliceI||m_cStackIPOs.sliceF!=sliceF) return -1;
        IPOGTrackNode IPOGT;
        ArrayList<IPOGaussianNode> IPOGs;
        IPOGaussianNode IPOG,IPOGo;
        for(i=0;i<m_cStackIPOT.nNumTracks;i++){
            IPOGT=m_cStackIPOT.m_cvIPOGTracks.get(i);
            IPOGs=IPOGT.m_cvIPOGs;
            IPOGs=IPOGaussianNodeHandler.getSimpleIPOGs(IPOGs);
            len=IPOGs.size();
            for(j=0;j<len;j++){
                IPOG=IPOGs.get(j);
                if(IPOG==null) {
                    continue;
                }
                slice=IPOG.sliceIndex;
                IPOGo=IPOGaussianNodeHandler.getClosetIPOG(m_cStackIPOs.SliceIPOs.get(slice-sliceI).IPOs,IPOG.xc,IPOG.yc);
                if(IPOGo==null) {
                    continue;
                }
                IPOGo.TrackIndex=IPOG.TrackIndex;
                IPOGo.BundleIndex=IPOG.BundleIndex;
                IPOGo.dBackground=IPOG.dBackground;
                IPOGo.dTotalSignal=IPOG.dTotalSignal;
                IPOGo.peak1=IPOG.peak1;
                IPOGo.peak3=IPOG.peak3;
                IPOGo.area=IPOG.area;
                IPOGs.set(j, IPOGo);
            }
        }
        return 1;
    }
    int rebuildStackIPOs(){
        if(m_cStackIPOT==null) return -1;
        boolean buildComplex=IPOGComplexRB.isSelected();
        m_cStackIPOT.calSliceRange();
        int sliceI=m_cStackIPOT.sliceI,sliceF=m_cStackIPOT.sliceF,slice,i,len,j;
        ArrayList<SliceIPOGaussianNode> cvSIPOGs=new ArrayList();
        for(slice=sliceI;slice<=sliceF;slice++){
            cvSIPOGs.add(new SliceIPOGaussianNode());
        }
        IPOGTrackNode IPOGT;
        ArrayList<IPOGaussianNode> IPOGs;
        IPOGaussianNode IPOG;
        for(i=0;i<m_cStackIPOT.nNumTracks;i++){
            IPOGT=m_cStackIPOT.m_cvIPOGTracks.get(i);
//            IPOGs=IPOGaussianNodeHandler.getSimpleIPOGs(IPOGT.m_cvIPOGs);
            IPOGs=IPOGT.m_cvIPOGs;
            len=IPOGs.size();
            for(j=0;j<len;j++){
                IPOG=IPOGs.get(j);
                if(IPOG==null) continue;
                slice=IPOG.sliceIndex;
                cvSIPOGs.get(slice-sliceI).IPOs.add(IPOG);
            }
        }
        SliceIPOGaussianNode SIPOG;
        for(slice=sliceI;slice<=sliceF;slice++){
            SIPOG=cvSIPOGs.get(slice-sliceI);
            SIPOG.slice=slice;
            SIPOG.nNumIPOs=SIPOG.IPOs.size();
        }
        m_cStackIPOs=new StackIPOGaussianNode();
        m_cStackIPOs.setSliceIPOs(cvSIPOGs);
        return 1;
    }
    ArrayList<IPOGaussianNode> selectIPOG(int x,int y,int z){
        ArrayList<IPOGaussianNode> IPOGs=getIPOGs(x,y,z);
        selectIPOGs(IPOGs);
        return IPOGs;
    }
    void selectIPOGs(ArrayList<IPOGaussianNode> IPOGs){
        int len=IPOGs.size();
        if(!AppendSelectionRB.isSelected()) m_cvSelectedIPOs.clear();
        for(int i=0;i<len;i++){
            m_cvSelectedIPOs.add(IPOGs.get(i));
        }
        updateSelectedIPOTable(IPOGs);
    }
    ArrayList<IPOGaussianNode> getIPOGs(int x, int y, int z){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        if(z<m_cStackIPOs.sliceI-1||z>=m_cStackIPOs.sliceF) return IPOGs;
        int slice0=m_cStackIPOs.sliceI;
        int index=z+1-slice0;
        ArrayList<IPOGaussianNode> IPOGst=m_cStackIPOs.SliceIPOs.get(index).IPOs;
        IPOGaussianNode IPOG;
        int len=IPOGst.size();
        for(int i=0;i<len;i++){
            IPOG=IPOGst.get(i);
            if(IPOG.contains2(x, y)) IPOGs.add(IPOG);
        }
        return IPOGs;
    }
    ArrayList<IPOGaussianNode> getIPOGs(int x, int y, int z, Roi roi){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        if(z<m_cStackIPOs.sliceI-1||z>=m_cStackIPOs.sliceF) return IPOGs;
        int slice0=m_cStackIPOs.sliceI;
        int index=z+1-slice0;
        ArrayList<IPOGaussianNode> IPOGst=m_cStackIPOs.SliceIPOs.get(index).IPOs;
        IPOGaussianNode IPOG;
        int len=IPOGst.size();
        Point pt;
        for(int i=0;i<len;i++){
            IPOG=IPOGst.get(i);
            pt=m_cTrackDisplayer.getDisplayCoordinates(IPOG.getXc(), IPOG.getYc(), IPOG.sliceIndex);
            if(pt==null) continue;
            if(roi.contains(pt.x, pt.y)) IPOGs.add(IPOG);
        }
        return IPOGs;
    }
    void viewIPOsOrTracks(){
        boolean viewTracks=ViewTrackRB.isSelected();
        if(viewTracks)updateTrackTable();
    }
    void updateTrackTable(){
        String option=(String)TrackTableOptionCB.getSelectedItem();
        updateTrackTable(option);
    }
    public void importStackIPOT(){
        String path=FileAssist.getFilePath("import stack IPOGTack file", FileAssist.defaultDirectory, "TRK files", "TRK", true);
        importStackIPOT(path);
    }
    public void importStackIPOT(String path){
        m_cStackIPOT=new StackIPOGTracksNode();
        m_cStackIPOT.addActionListener(this);
        m_cStackIPOT.importIPOGTracks(path);
        firstSliceForCutoff=m_cStackIPOT.sliceI;
        if(m_cStackIPOT.pdHeightCutoffPs!=null){
            CommonStatisticsMethods.copyArray(m_cStackIPOT.pdHeightCutoffPs,pdHeightCutoffPs);
            ppdHeightCutoff=m_cStackIPOT.ppdHeightCutoff;
            HeightCutoffIsComputed=true;
        }else{
            buildHeightCutoff();
        }
        setStackPixels();
//        setIPOGTComplexity();
        showSliceRange();
    }
    public static boolean HeightCutoffIsComputed(){
        return HeightCutoffIsComputed;
    }
    public void importStackIPOT_LevelInfo(String path){
        nullifyHeightCutoffs();
        m_cStackIPOT=new StackIPOGTracksNode();
        m_cStackIPOT.addActionListener(this);
        m_cStackIPOT.importIPOGTrackLevelInfo(path, (String)TrackPlotOptionCB.getSelectedItem(), Integer.parseInt(RisingIntervalTF.getText()));
        firstSliceForCutoff=m_cStackIPOT.sliceI;
        if(m_cStackIPOT.pdHeightCutoffPs!=null){
            CommonStatisticsMethods.copyArray(m_cStackIPOT.pdHeightCutoffPs,pdHeightCutoffPs);
            ppdHeightCutoff=m_cStackIPOT.ppdHeightCutoff;
            HeightCutoffIsComputed=true;
        }
        showSliceRange();
    }
    void monitingIPOs(){
        Thread monitor=new Thread(new Runnable() {
            public void run() {
                Script_Runner runner=new Script_Runner();
                runner.run(null);
                IJ.showStatus("finished monitingIPOs");
            }
        });
        monitor.start();
    }
    int updateAssociatedImagePixels(float xSigma,float ySigma,float fAccuracy){
        return 1;
    }
    int setStackPixels(){
        if(implOriginal==null) return -1;
        int sliceI=m_cStackIPOT.sliceI,sliceF=m_cStackIPOT.sliceF;
        int h=implOriginal.getHeight(),w=implOriginal.getWidth();
        int len=sliceF-sliceI+1;
        pixelsStack=new int[len][h][w];
        int slice=implOriginal.getCurrentSlice();
        for(slice=sliceI;slice<=sliceF;slice++){
            implOriginal.setSlice(slice);
            pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
            CommonMethods.getPixelValue(implOriginal, slice, pixels);
            pnScratch1=CommonStatisticsMethods.getIntArray2(pnScratch1, w, h);
            pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, w, h);
            pdPixels=CommonStatisticsMethods.getDoubleArray2(pdPixels, w, h);
            CommonStatisticsMethods.copyArray(pixels, pixelsStack[slice-sliceI]);
        }
        return 1;
    }

    int showAssociatedImageStacks(){
        if(implOriginal==null) return -1;
        int slice=implOriginal.getCurrentSlice();
        int background=0;
        String imageTitle=implOriginal.getTitle();
        float xSigma=1f,ySigma=1f,fAccuracy=0.001f;
        implp=CommonMethods.cloneImage(implOriginal);
        implp.setTitle(imageTitle+" -processed image");
        implInt=CommonMethods.cloneImage(implOriginal);
        implInt.setTitle(imageTitle+" -Integrated image");
        implComposed=CommonMethods.cloneImage(implOriginal);
        implComposed.setTitle(imageTitle+" -Composed image");
        implCompen=CommonMethods.cloneImage(implOriginal);
        implCompen.setTitle(imageTitle+" -Compensated image");
        implSignal=CommonMethods.cloneImage(implOriginal);
        implSignal.setTitle(imageTitle+" -Signal image");

        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);
        int h=implOriginal.getHeight(),w=implOriginal.getWidth();

        ArrayList<IPOGaussianNode> IPOGs;

        int sliceI=m_cStackIPOs.sliceI,sliceF=m_cStackIPOs.sliceF;
        int len=sliceF-sliceI+1;
        pixelsCompenStack=new int[len][h][w];
        pixelsStack=new int[len][h][w];
        for(slice=sliceI;slice<=sliceF;slice++){
            implOriginal.setSlice(slice);
            implCompen.setSlice(slice);
            implComposed.setSlice(slice);
            implInt.setSlice(slice);

            IPOGs=m_cStackIPOs.getIPOGs(slice);
            IPOGs=IPOGaussianNodeHandler.getSimpleIPOGs(IPOGs);
            pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
            CommonMethods.getPixelValue(implOriginal, slice, pixels);

            pixelsCompen=CommonStatisticsMethods.getIntArray2(pixelsCompen, w, h);
            pixelsCanvus=CommonStatisticsMethods.getIntArray2(pixelsCanvus, w, h);
            pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, w, h);
            pdPixels=CommonStatisticsMethods.getDoubleArray2(pdPixels, w, h);

            CommonStatisticsMethods.setElements(pdPixels, 0);
            CommonStatisticsMethods.setElements(pixelsCanvus, background);

            IPOGaussianNodeHandler.getSuperImposition(pdPixels, IPOGs);
            CommonStatisticsMethods.copyArray(pdPixels, pnScratch);
            CommonMethods.setPixels(implInt, pnScratch);

            IPOGaussianNodeHandler.getCanvusPixels_Progressive(pixelsCanvus, pdPixels, IPOGs);
            CommonStatisticsMethods.replaceElement(pixels, pixelsCanvus, background);
            CommonStatisticsMethods.addArray(pixels, pnScratch, pixelsCompen,-1);
            CommonMethods.setPixels(implCompen, pixelsCompen);

            CommonStatisticsMethods.addArray(pixelsCanvus, pnScratch, pnScratch,1);
            CommonMethods.setPixels(implComposed, pnScratch);
            CommonStatisticsMethods.copyArray(pixelsCompen, pixelsCompenStack[slice-sliceI]);
            CommonStatisticsMethods.copyArray(pixels, pixelsStack[slice-sliceI]);
        }

        implp.show();
        CommonGuiMethods.addMouseListener(implp, this);
        implCompen.show();
        CommonGuiMethods.addMouseListener(implCompen, this);
        implInt.show();
        CommonGuiMethods.addMouseListener(implInt, this);
        implSignal=CommonMethods.cloneImage(implOriginal);
        implSignal.setTitle(imageTitle+" -Signal image");
        implSignal.show();
        CommonGuiMethods.addMouseListener(implSignal, this);
        implComposed.show();
        CommonGuiMethods.addMouseListener(implComposed, this);

        if(m_cMasterForm==null){
            if(m_cViewer==null){
                 m_cViewer=new ImageComparisonViewer();
                 m_cViewer.setVisible(true);
            }else{
                if(!m_cViewer.isVisible()) m_cViewer.setVisible(true);
            }
        }else{
            m_cViewer=m_cMasterForm.getComparisonViewer();
        }
        

         m_cViewer.pickSourceImage(implOriginal);
         m_cViewer.pickTargetImage(implComposed);
         m_cViewer.pickTargetImage(implp);
         m_cViewer.pickTargetImage(implSignal);
         m_cViewer.pickTargetImage(implInt);
         m_cViewer.pickTargetImage(implCompen);
         m_cViewer.setMagnification(1);
         m_cViewer.setWindowSize(260, 260);

        return 1;
    }
    int buildAssociatedImageStack(){
        if(implOriginal==null) return -1;
        int slice=implOriginal.getCurrentSlice();
        int background=0;
        String imageTitle=implOriginal.getTitle();
        float xSigma=1f,ySigma=1f,fAccuracy=0.001f;
        if(implp!=null) implp=CommonMethods.cloneImage(implOriginal);
        if(implp!=null) implp.setTitle(imageTitle+" -processed image");
/*        implInt=CommonMethods.cloneImage(implOriginal);
        implInt.setTitle(imageTitle+" -Integrated image");
        implComposed=CommonMethods.cloneImage(implOriginal);
        implComposed.setTitle(imageTitle+" -Composed image");
        implCompen=CommonMethods.cloneImage(implOriginal);
        implCompen.setTitle(imageTitle+" -Compensated image");
        implSignal=CommonMethods.cloneImage(implOriginal);
        implSignal.setTitle(imageTitle+" -Signal image");*/

        if(implp!=null) CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);
        int h=implOriginal.getHeight(),w=implOriginal.getWidth();

        ArrayList<IPOGaussianNode> IPOGs;

        int sliceI=m_cStackIPOs.sliceI,sliceF=m_cStackIPOs.sliceF;
        int len=sliceF-sliceI+1;
        pixelsCompenStack=new int[len][h][w];
        pixelsStack=new int[len][h][w];
        for(slice=sliceI;slice<=sliceF;slice++){
            implOriginal.setSlice(slice);
//            implCompen.setSlice(slice);
//            implComposed.setSlice(slice);
//            implInt.setSlice(slice);

            IPOGs=m_cStackIPOs.getIPOGs(slice);
            IPOGs=IPOGaussianNodeHandler.getSimpleIPOGs(IPOGs);
            pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
            CommonMethods.getPixelValue(implOriginal, slice, pixels);

            pixelsCompen=CommonStatisticsMethods.getIntArray2(pixelsCompen, w, h);
            pixelsCanvus=CommonStatisticsMethods.getIntArray2(pixelsCanvus, w, h);
            pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, w, h);
            pdPixels=CommonStatisticsMethods.getDoubleArray2(pdPixels, w, h);

            CommonStatisticsMethods.setElements(pdPixels, 0);
            CommonStatisticsMethods.setElements(pixelsCanvus, background);

            IPOGaussianNodeHandler.getSuperImposition(pdPixels, IPOGs);
            CommonStatisticsMethods.copyArray(pdPixels, pnScratch);
//            CommonMethods.setPixels(implInt, pnScratch);

            IPOGaussianNodeHandler.getCanvusPixels_Progressive(pixelsCanvus, pdPixels, IPOGs);
            CommonStatisticsMethods.replaceElement(pixels, pixelsCanvus, background);
            CommonStatisticsMethods.addArray(pixels, pnScratch, pixelsCompen,-1);
//            CommonMethods.setPixels(implCompen, pixelsCompen);

            CommonStatisticsMethods.addArray(pixelsCanvus, pnScratch, pnScratch,1);
//            CommonMethods.setPixels(implComposed, pnScratch);
            CommonStatisticsMethods.copyArray(pixelsCompen, pixelsCompenStack[slice-sliceI]);
            CommonStatisticsMethods.copyArray(pixels, pixelsStack[slice-sliceI]);
        }

//        implp.show();
////        CommonGuiMethods.addMouseListener(implp, this);
//        implCompen.show();
//        CommonGuiMethods.addMouseListener(implCompen, this);
//        implInt.show();
//        CommonGuiMethods.addMouseListener(implInt, this);
//        implSignal=CommonMethods.cloneImage(implOriginal);
//        implSignal.setTitle(imageTitle+" -Signal image");
//        implSignal.show();
//        CommonGuiMethods.addMouseListener(implSignal, this);
//        implComposed.show();
//        CommonGuiMethods.addMouseListener(implComposed, this);
/*
        if(m_cMasterForm==null){
            if(m_cViewer==null){
                 m_cViewer=new ImageComparisonViewer();
                 m_cViewer.setVisible(true);
            }else{
                if(!m_cViewer.isVisible()) m_cViewer.setVisible(true);
            }
        }else{
            m_cViewer=m_cMasterForm.getComparisonViewer();
        }
        
/*
         m_cViewer.pickSourceImage(implOriginal);
         m_cViewer.pickTargetImage(implComposed);
         m_cViewer.pickTargetImage(implp);
         m_cViewer.pickTargetImage(implSignal);
         m_cViewer.pickTargetImage(implInt);
         m_cViewer.pickTargetImage(implCompen);
         m_cViewer.setMagnification(1);
         m_cViewer.setWindowSize(260, 260);*/

        return 1;
    }
    int showProcessedImage(){
        if(implOriginal==null) return -1;
        int slice=implOriginal.getCurrentSlice();
        int background=0;
        String imageTitle=implOriginal.getTitle();
        float xSigma=1f,ySigma=1f,fAccuracy=0.001f;
        implp=CommonMethods.cloneImage(implOriginal, slice);
        implp.setTitle(imageTitle+" -processed image");
        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);
        implp.show();
        return 1;
    }
    int showAssociatedImages(){
        if(ShowAssociatedImageStackRB.isSelected()){
            showAssociatedImageStacks();
            return 1;
        }
        if(implOriginal==null) return -1;
        int slice=implOriginal.getCurrentSlice();
        int background=0;
        String imageTitle=implOriginal.getTitle();
        float xSigma=1f,ySigma=1f,fAccuracy=0.001f;
        implp=CommonMethods.cloneImage(implOriginal, slice);
        implp.setTitle(imageTitle+" -processed image");
        implInt=CommonMethods.cloneImage(implOriginal, slice);
        implInt.setTitle(imageTitle+" -Integrated image");
        implComposed=CommonMethods.cloneImage(implOriginal, slice);
        implComposed.setTitle(imageTitle+" -Composed image");
        implCompen=CommonMethods.cloneImage(implOriginal, slice);
        implCompen.setTitle(imageTitle+" -Compensated image");

        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);
        int h=implOriginal.getHeight(),w=implOriginal.getWidth();
        pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
        CommonMethods.getPixelValue(implOriginal, slice, pixels);

        pixelsCompen=CommonStatisticsMethods.getIntArray2(pixelsCompen, w, h);
        pixelsCanvus=CommonStatisticsMethods.getIntArray2(pixelsCanvus, w, h);
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, w, h);
        pdPixels=CommonStatisticsMethods.getDoubleArray2(pdPixels, w, h);

        CommonStatisticsMethods.setElements(pdPixels, 0);
        CommonStatisticsMethods.setElements(pixelsCanvus, background);

        IPOGaussianNodeHandler.getSuperImposition(pdPixels, m_cvIPONodes);
        CommonStatisticsMethods.copyArray(pdPixels, pnScratch);
        CommonMethods.setPixels(implInt, pnScratch);

        IPOGaussianNodeHandler.getCanvusPixels_Progressive(pixelsCanvus, pdPixels, m_cvIPONodes);
        CommonStatisticsMethods.replaceElement(pixels, pixelsCanvus, background);

        CommonStatisticsMethods.addArray(pixels, pnScratch, pixelsCompen,-1);
        CommonMethods.setPixels(implCompen, pixelsCompen);

        CommonStatisticsMethods.addArray(pixelsCanvus, pnScratch, pnScratch,1);
        CommonMethods.setPixels(implComposed, pnScratch);

        implp.show();
        CommonGuiMethods.addMouseListener(implp, this);

        implCompen.show();
        CommonGuiMethods.addMouseListener(implCompen, this);

        implInt.show();
        CommonGuiMethods.addMouseListener(implInt, this);

        implSignal=CommonMethods.cloneImage(implOriginal, slice);
        implSignal.setTitle(imageTitle+" -Signal image");
        implSignal.show();
        CommonGuiMethods.addMouseListener(implSignal, this);
        
        implComposed.show();
        CommonGuiMethods.addMouseListener(implComposed, this);


        if(m_cMasterForm==null){
            if(m_cViewer==null){
                 m_cViewer=new ImageComparisonViewer();
                 m_cViewer.setVisible(true);
            }else{
                if(!m_cViewer.isVisible()) m_cViewer.setVisible(true);
            }
        }else{
            m_cViewer=m_cMasterForm.getComparisonViewer();
        }

         m_cViewer.pickSourceImage(implOriginal);
         m_cViewer.pickTargetImage(implComposed);
         m_cViewer.pickTargetImage(implp);
         m_cViewer.pickTargetImage(implSignal);
         m_cViewer.pickTargetImage(implInt);
         m_cViewer.pickTargetImage(implCompen);
         m_cViewer.setMagnification(1);
         m_cViewer.setWindowSize(260, 260);

         return 1;
    }
    public static int getCompensatedPixels(ArrayList<IPOGaussianNode> IPOs,double pdPixels[][], int[][]pixels,int[][] pnScratch, int[][] pixelsCompen){
        CommonStatisticsMethods.setElements(pdPixels, 0);
        IPOGaussianNodeHandler.getSuperImposition(pdPixels, IPOs);
        CommonStatisticsMethods.copyArray(pdPixels, pnScratch);
        CommonStatisticsMethods.addArray(pixels, pnScratch, pixelsCompen,-1);
        return 1;
    }
    int closeAssociatedImages(){
        m_cViewer.removeAllTargetImages();
        if(implp==null) return -1;
        implp.close();
        if(implCompen!=null) implCompen.close();
        if(implInt!=null)implInt.close();
        if(implSignal!=null)implSignal.close();
        if(implComposed!=null)implComposed.close();
        return 1;
    }
    void closeAssociatedImages0(){
        m_cViewer.removeAllTargetImages();
        implp.hide();
        implCompen.hide();
        implInt.hide();
        implSignal.hide();
        implComposed.hide();
    }
    int analyzeSlice(int slice){
        SliceIPOGaussianNode aSliceNode;
        if(m_cStackIPOs==null) return -1;
        int sliceI=m_cStackIPOs.SliceIPOs.get(0).slice;
        int sliceF=sliceI+m_cStackIPOs.nSlices-1;
        if(slice<sliceI||slice>sliceF) return -1;
        AnalyzeSliceTF.setText(""+slice);
        int index=slice-sliceI;
        aSliceNode=m_cStackIPOs.SliceIPOs.get(index);
        m_cvIPONodes=aSliceNode.IPOs;
        m_cvSelectedIPOs.clear();
        RegionHCutoff=aSliceNode.RegionHCutoff;
        TotalSignalCutoff=aSliceNode.TotalSignalCutoff;
        updateIPOTable();
        updateSelectedIPOTable(m_cvSelectedIPOs);
        setCurrentImageSlice(slice);
        return 1;
    }
    void setCurrentImageSlice(int slice){
        ImagePlus impl=implOriginal;
        int nSlices=impl.getNSlices();
        if(slice<nSlices) impl.setSlice(slice);
    }
    void includeOverlappingIPOs(){
        ArrayList<IPOGaussianNode> IPOs=new ArrayList(),IPOst;
        IPOGaussianNode IPO;
        int len=m_cvSelectedIPOs.size(),i,len1,j;
        for(i=0;i<len;i++){
            IPO=m_cvSelectedIPOs.get(i);
            if(!CommonMethods.containContent(IPOs, IPO))IPOs.add(IPO);
            IPOst=IPO.cvNeighboringIPOs;
            len1=IPOst.size();
            for(j=0;j<len1;j++){
                IPO=IPOst.get(j);
                if(!CommonMethods.containContent(IPOs, IPO))IPOs.add(IPO);
            }
        }
        m_cvSelectedIPOs.clear();
        m_cvSelectedIPOs=IPOs;
        updateSelectedIPOTable(m_cvSelectedIPOs);
    }
    void includeIPOsInClusters(){
        ArrayList<IPOGaussianNode> IPOs=new ArrayList(),IPOst;
        IPOGaussianNode IPO;
        ArrayList<Integer> nvT;
        int len=m_cvSelectedIPOs.size(),i,len1,j,index;
        for(i=0;i<len;i++){
            IPO=m_cvSelectedIPOs.get(i);
            if(!CommonMethods.containContent(IPOs, IPO))IPOs.add(IPO);
            nvT=IPO.cvIndexesInCluster;
            len1=nvT.size();
            for(j=0;j<len1;j++){
                index=nvT.get(j);
                IPO=m_cvIPONodes.get(index);
                if(!CommonMethods.containContent(IPOs, IPO))IPOs.add(IPO);
            }
        }
        m_cvSelectedIPOs.clear();
        m_cvSelectedIPOs=IPOs;
        updateSelectedIPOTable(m_cvSelectedIPOs);
    }

    void selectIPOs(){
        m_cIPOGSelector.updateSelectionCriteria();
        if(m_cIPOGSelector.selectEntireStack())
            m_cIPOGSelector.selectIPOs(m_cStackIPOs);
        else
            m_cIPOGSelector.selectIPOGs(m_cvIPONodes);
        
        m_cvSelectedIPOs=m_cIPOGSelector.getSelectedIPOGs();
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IPOAnalyzerForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton AdjustFirstSliceRB;
    private javax.swing.JRadioButton AdjustTrackCentersRB;
    private javax.swing.JLabel AnalyzeSliceLB;
    private javax.swing.JTextField AnalyzeSliceTF;
    private javax.swing.JRadioButton AppendSelectionRB;
    private javax.swing.JButton AssociateImageBT;
    private javax.swing.JButton BringTrackPlotsToFront;
    private javax.swing.JRadioButton BuildRWAIPOShapesRB;
    private javax.swing.JRadioButton ClickToSelectRB;
    private javax.swing.JButton ComputeRawPeaksBT;
    private javax.swing.JButton DefaultSettingBT;
    private javax.swing.JButton DetectTransitionsBT;
    private javax.swing.JRadioButton DisplayTrackImageRB;
    private javax.swing.JButton ExportLevelInfoBT;
    private javax.swing.JButton ExportTracksBT;
    private javax.swing.JCheckBox ExtendTracksCB;
    private javax.swing.JTextField FirstSliceTF;
    private javax.swing.JRadioButton FitStackRB;
    private javax.swing.JComboBox HighlightChoiceCB;
    private javax.swing.JRadioButton HighlightTracksOnAssociatedImagesRB;
    private javax.swing.JRadioButton HighlightTracksOnSeriesImageRB;
    private javax.swing.JTextField IPOCoordinatesTF;
    private javax.swing.JRadioButton IPOGComplexRB;
    private javax.swing.JButton IPOGaussianFittingBT;
    private javax.swing.JTextField IPORWAShapeWSTF;
    private javax.swing.JScrollPane IPOSelectionChoiceScrollPane;
    private javax.swing.JPanel IPOSelectionContainerPane;
    private javax.swing.JLabel IPOTableTitleLB;
    private javax.swing.JButton ImportFittedIPOsBT;
    private javax.swing.JButton ImportLevelInfoBT;
    private javax.swing.JButton ImportStackIPOTBT;
    private javax.swing.JButton IncludeClustersBT;
    private javax.swing.JButton IncludeOverlappingIPOsBT;
    private javax.swing.JTextField LastSliceTF;
    private javax.swing.JButton LinkIPOGsBT;
    private javax.swing.JRadioButton LockTable1RB;
    private javax.swing.JRadioButton LockTable2RB;
    private javax.swing.JLabel MasterStatusLB;
    private javax.swing.JButton MonitingIPOSBT;
    private javax.swing.JButton MoveToTable2BT;
    private javax.swing.JTextField NumOfThreadTF;
    private javax.swing.JTextField OverlapCutoffTF;
    private javax.swing.JButton RebuildStackIPOsBT;
    private javax.swing.JLabel RegionHeightCutoffLB;
    private javax.swing.JButton RestoreTable2BT;
    private javax.swing.JTextField RisingIntervalTF;
    private javax.swing.JButton RoiTraceBT;
    private javax.swing.JButton SaveIPOGTSToABFBT;
    private javax.swing.JButton SelectIPOBT;
    private javax.swing.JRadioButton SelectTrackRB;
    private javax.swing.JLabel SelectedIPONumberLB;
    private javax.swing.JLabel SelectedIPOTitleLB;
    private javax.swing.JComboBox SelectionTargetOptionCB;
    private javax.swing.JRadioButton ShowAssociatedImageRB;
    private javax.swing.JRadioButton ShowAssociatedImageStackRB;
    private javax.swing.JRadioButton ShowIPOTBundleRB;
    private javax.swing.JRadioButton SilentFittingRB;
    private javax.swing.JRadioButton SimpleIPOGsRB;
    private javax.swing.JButton SortIPOsBT;
    private javax.swing.JLabel StackSliceRangeLB;
    private javax.swing.JLabel StatusLB;
    private javax.swing.JScrollPane Table1Pane;
    private javax.swing.JScrollPane Table2Pane;
    private javax.swing.JScrollPane ThreadStatusSP;
    private javax.swing.JLabel TotalIPONumberLB;
    private javax.swing.JLabel TotalSignalCutoffLB;
    private javax.swing.JTextField TrackImageBlockDimTF;
    private javax.swing.JTextField TrackImageITF;
    private javax.swing.JTextField TrackImageIncrementTF;
    private javax.swing.JTextField TrackImageMagTF;
    private javax.swing.JTextField TrackImageRadiusTF;
    private javax.swing.JComboBox TrackPlotOptionCB;
    private javax.swing.JComboBox TrackTableOptionCB;
    private javax.swing.JRadioButton ViewSelectedIPORB;
    private javax.swing.JRadioButton ViewTrackRB;
    private javax.swing.JButton hightlightIPOsBT;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
    public void mousePressed(MouseEvent e){}
    public void mouseClicked(MouseEvent eo){

        requestFocusInWindow();
        IPOGaussianNode IPOG=null;
        ImagePlus impl=CommonGuiMethods.getSourceImage(eo);
        Point cursor=null;
        cursor=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
        if(m_cvIPOGTPWs!=null) IPOG=IPOGTrackPlotWindow.getCurrentIPO(m_cvIPOGTPWs, eo);//The event is from the plot window
        if(IPOG!=null){//event is originated from the track plot image
            if(ShowAssociatedImageRB.isSelected()) {
                displayIPOG(IPOG);
            }
            if(DisplayTrackImageRB.isSelected()){
                displayIPOGTOnSeriesImage(IPOGTrackPlotWindow.getCurrentIPOT(m_cvIPOGTPWs, eo));
            }
            processTrackPlotMouseClick(IPOGTrackPlotWindow.getCurrentIPOGTPW(m_cvIPOGTPWs, eo),eo);
        }else if(impl==implOriginal||isAssociatedImage(impl)){
            int slice=impl.getCurrentSlice();
            analyzeSlice(slice);
            if(ClickToSelectRB.isSelected()) {
                Roi roi=impl.getRoi();
                if(roi==null||roi instanceof PointRoi)
                    selectNearbyIPOs(cursor);
                else {
                    Rectangle rec=roi.getBoundingRect();
                    intRange xRange=new intRange(rec.x,rec.x+rec.width),yRange=new intRange(rec.y,rec.y+rec.height);
                    m_cvSelectedIPOs=collectIPOGsInScope(m_cvIPONodes,xRange,yRange);
                }
            }
            updateSelectedIPOTable(m_cvSelectedIPOs);
        }else if(impl==implSeries){
            Point pt=m_cTrackDisplayer.getOriginalCoordinates(cursor.x, cursor.y);
            if(pt!=null){
                int slice=m_cTrackDisplayer.getSliceIndex(cursor.x, cursor.y);
                if(eo.isShiftDown()){
                    m_cTrackDisplayer.displayIPOGTrack(m_cIPOGTOnSeriesImage);
                }else{
                    displayPoint(pt.x,pt.y,slice-1);
                }
            }
        }else if(impl==implAssociated){
            if(ClickToSelectRB.isSelected()) {
                Roi roi=impl.getRoi();
                Point pt=m_cViewer.getAssociatedDisplayer().getOriginalCoordinates(cursor.x, cursor.y);
                int slice=implOriginal.getCurrentSlice();
                analyzeSlice(slice);
                if(roi==null||roi instanceof PointRoi)
                    selectNearbyIPOs(pt);
                else {
                    Rectangle rec=roi.getBoundingRect();
                    intRange xRange=new intRange(rec.x,rec.x+rec.width),yRange=new intRange(rec.y,rec.y+rec.height);
                    m_cvSelectedIPOs=collectIPOGsInScope(m_cvIPONodes,xRange,yRange);
                }
            }
            updateSelectedIPOTable(m_cvSelectedIPOs);
        }else if(eo.getSource()==m_cIPOGTTransitionAnalyzer.m_cIPOGTPW.pw.getImagePlus().getWindow().getCanvas()){
            processTrackPlotMouseClick(m_cIPOGTTransitionAnalyzer.m_cIPOGTPW,eo);
        }
    }
    boolean isAssociatedImage(ImagePlus impl){
        if(impl==implp) return true;
        if(impl==implInt) return true;
        if(impl==implCompen) return true;
        if(impl==implSignal) return true;
        if(impl==implComposed) return true;
        return false;
    }
    void selectNearbyIPOs(Point pt){
        if(!AppendSelectionRB.isSelected())m_cvSelectedIPOs.clear();
        int i,len=m_cvIPONodes.size();
        IPOGaussianNode IPO;
        for(i=0;i<len;i++){
            IPO=m_cvIPONodes.get(i);
            if(IPO.cXRange2.contains(pt.x)&&IPO.cYRange2.contains(pt.y)){
                m_cvSelectedIPOs.add(IPO);
            }
        }
    }
    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e){}

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e){
        ImagePlus impl=CommonGuiMethods.getSourceImage(e);
        IPOGTrackNode IPOGT=null;
        if(m_cvIPOGTPWs!=null)IPOGT=IPOGTrackPlotWindow.getCurrentIPOT(m_cvIPOGTPWs, e);
        if(IPOGT!=null){
            refreshSelectedIPOTable(IPOGT.m_cvIPOGs);
        }
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){
        ImagePlus impl=CommonGuiMethods.getSourceImage(e);
        IPOGTrackNode IPOGT=null;
        if(m_cvIPOGTPWs!=null)IPOGT=IPOGTrackPlotWindow.getCurrentIPOT(m_cvIPOGTPWs, e);
        if(IPOGT!=null&&m_cTrackDisplayer!=null){
            if(IPOGT==m_cTrackDisplayer.getMainTrack()){
                if(HighlightTracksOnSeriesImageRB.isSelected()){
                    highlightTracksOnSeriesImage();
                }
            }
        }
    }
    public void mouseDragged(MouseEvent e){}

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public int applyMouseMove(MouseEvent e){
        ImagePlus impl=CommonGuiMethods.getSourceImage(e);
        Point cursor=null;
        int slice;
        String plotValue;
        if(impl==null){
            impl=IPOGTrackPlotWindow.getCurrentIPOGTPW(m_cvIPOGTPWs, e).pw.getImagePlus();
        }    
        cursor=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
        if(cursor==null) return -1;
        IPOGTrackNode IPOGT=null;
        if(m_cvIPOGTPWs!=null)IPOGT=IPOGTrackPlotWindow.getCurrentIPOT(m_cvIPOGTPWs, e);
        if(IPOGT!=null&&m_cTrackDisplayer!=null){//the MouseEvent is from one of the plot windows
            if(IPOGT==m_cTrackDisplayer.getMainTrack()){
                IPOGaussianNode IPOG=IPOGTrackPlotWindow.getCurrentIPO(m_cvIPOGTPWs, e);
                if(IPOG!=null&&m_cTrackDisplayer!=null) {
                    slice=IPOG.sliceIndex;
                    m_cTrackDisplayer.frameSlice(slice,Color.green);
                    plotValue=IPOGTrackPlotWindow.getCurrentIPOGTPW(m_cvIPOGTPWs, e).pw.getPoingCoordinate(cursor.x, cursor.y);
                    MasterStatusLB.setText("Master Status: "+plotValue);
                    IPOGTrackPlotWindow.highlightAssociatedTrackPlots(m_cvIPOGTPWs, m_cTrackDisplayer.getMainTrack(),slice);
               }
            }
        }else if(impl==implSeries){
            Point pt=m_cTrackDisplayer.getOriginalCoordinates(cursor.x, cursor.y);
            if(pt!=null){
                slice=m_cTrackDisplayer.getSliceIndex(cursor.x, cursor.y);
                if(slice>0){

                    IPOGTrackPlotWindow.highlightAssociatedTrackPlots(m_cvIPOGTPWs, m_cTrackDisplayer.getMainTrack(),slice);


                    displayPoint(pt.x,pt.y,slice-1);
                    boolean selectTrack=SelectTrackRB.isSelected();

                    SelectTrackRB.setSelected(false);//select and display IPOG (simple)'s
                        Roi roi=impl.getRoi();

                        ArrayList<IPOGaussianNode> IPOGs;
                        boolean bRoi=false;
                        if(roi!=null){
                            if(roi.contains(cursor.x, cursor.y)) bRoi=true;
                        }

                        if(bRoi)
                            IPOGs=getIPOGs(pt.x,pt.y,slice-1,roi);
                        else
                            IPOGs=getIPOGs(pt.x,pt.y,slice-1);

                        if(!e.isShiftDown()) IPOGs.clear();

                        updateSelectedIPOTable(IPOGs,retrieveHighlightColors(IPOGs));
                    SelectTrackRB.setSelected(selectTrack);

                    int i,len=IPOGs.size();
                    int x,y;
                    IPOGaussianNode IPOG;

                    HighlightingRoiCollectionNode aCollection=m_cTrackDisplayer.getCollection("NearbyIPOGs");
                    if(aCollection==null){
                        aCollection=new HighlightingRoiCollectionNode(m_cTrackDisplayer.getDisplayImage(),"NearbyIPOGs");
                        m_cTrackDisplayer.addCollection(aCollection);
                    } else{
                        aCollection.clear();
                    }
                    for(i=0;i<len;i++){
                        IPOG=IPOGs.get(i);
                        x=IPOG.getXc();
                        y=IPOG.getYc();
                        slice=IPOG.sliceIndex;
                        aCollection.addRoi(m_cTrackDisplayer.makeHighlighterOnSeriesImage(x, y, slice, Color.green),Color.green);
                    }
                    if(len>0) {
        //                implSeries.draw();
                        m_cTrackDisplayer.showHighlightCollections();
                    }
                }
            }
        }
        return 1;
    }
    public void mouseMoved(MouseEvent e){
        if(!e.isAltDown()) applyMouseMove(e);
    }
    public void keyPressed(KeyEvent ke){
        int code=ke.getKeyCode();
        if(code==15) ControlDown=true;
    }
    public void keyReleased(KeyEvent ke){
        int code=ke.getKeyCode();
        if(code==15) ControlDown=false;
    }
    public void keyTyped(KeyEvent ke){

    }
    public void valueChanged(ListSelectionEvent e) {/*
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        int row=0;
        if(lsm.equals(m_cIPOTable.getSelectionModel())){
            row=m_cIPOTable.getSelectedRow();
        }
        IPOGaussianNode IPO=m_cvSelectedIPOs.get(row);
        int x=(int)(IPO.xc+0.5),y=(int)(IPO.yc+0.5);
        implInt.setRoi(new PointRoi(x,y));*/
    }
    
    int handleTableChange(TableModelEvent te){
        Object o=te.getSource();
        int col=te.getColumn();
        JTable table;
        if(m_cIPOTable==null){
            table=m_cSelectedIPOTable;
        }else if(o==m_cIPOTable.getModel()){
            table=m_cIPOTable;
        }else{
            table=m_cSelectedIPOTable;
        }

        IPOGaussianNode IPO=null;

        int fr=te.getFirstRow();

        ArrayList<IPOGaussianNode> IPOs=m_cvSelectedIPOs;

        if(!AppendSelectionRB.isSelected()) IPOs.clear();

        if(table==m_cIPOTable){
            if(LockTable2RB.isSelected()) return -1;
            if(nIPOTableStatus==StatusTrack){
                ArrayList<IPOGaussianNode> cvIPOGs;
                if(((String)TrackTableOptionCB.getSelectedItem()).contentEquals("AllTracks"))
                    cvIPOGs=m_cStackIPOT.m_cvIPOGTracks.get(col).m_cvIPOGs;
                else if(((String)TrackTableOptionCB.getSelectedItem()).contentEquals("SelectedTracks")){
                    if(col>=m_cvSelectedIPOGTracks.size()) m_cvSelectedIPOGTracks=m_cStackIPOT.m_cvSelectedIPOGTracks;
                    cvIPOGs=m_cvSelectedIPOGTracks.get(col).m_cvIPOGs;
                } else
                    cvIPOGs=m_cStackIPOT.m_cvIPOGTBs.get(col).m_cvIPOGs;
                if(fr<cvIPOGs.size()){
                    IPO=cvIPOGs.get(fr);
                    if(IPO!=null){
                        IPOs.add(IPO);
                        updateSelectedIPOTable(IPOs);
                    }
                }
            }else{
                String st=(String)table.getValueAt(fr, 1);
//                int index=Integer.parseInt(st);
                int index=fr;
                IPO=m_cvIPONodes.get(index);
                if(IPO!=null){
                    IPOs.add(IPO);
                    updateSelectedIPOTable(IPOs);
                }
            }
        }else{
           if(LockTable1RB.isSelected()) return -1;
           IPO=m_cvSelectedIPOso.get(fr);
        }
        if(IPO!=null) displayIPOG(IPO);
        if(table==m_cSelectedIPOTable&&col==11) highlightOverlappingHeighbors(IPO);
        return 1;
    }
    public void tableChanged(TableModelEvent te){
        handleTableChange(te);
    }
    void displayIPOG(IPOGaussianNode IPOG){
        if(ViewSelectedIPORB.isSelected()&&m_cViewer!=null&&IPOG!=null){
            updateSignalImage(IPOG);
            int x=(int)(IPOG.xc+0.5);
            int y=(int)(IPOG.yc+0.5);
            int slice=IPOG.sliceIndex;
            displayPoint(x,y,slice-1);
        }
    }
    int displayPoint(int x, int y, int z){
        if(m_cViewer==null||implOriginal==null) return -1;
        int slice=z+1;
        Point pt=new Point(x,y);
        implOriginal.setSlice(slice);
        m_cViewer.setViewCenter(implOriginal, pt);
        m_cViewer.highlightPoint(pt);
        return 1;
    }
    void highlightOverlappingHeighbors(IPOGaussianNode IPOG){
        ArrayList<IPOGaussianNode> IPOst=m_cvSelectedIPOs;
        m_cvSelectedIPOs=new ArrayList();
        m_cvSelectedIPOs.add(IPOG);
        int i,len=IPOG.cvNeighboringIPOs.size();
        for(i=0;i<len;i++){
            m_cvSelectedIPOs.add(IPOG.cvNeighboringIPOs.get(i));
        }
        highlightSelectedIPOs();
        m_cvSelectedIPOs.clear();
        m_cvSelectedIPOs=IPOst;
    }
    public void actionPerformed(ActionEvent ae){
        StatusLB.setText(ae.getActionCommand());
        if(ae.getActionCommand().contentEquals("Painted")){
            ImagePlus impl=CommonGuiMethods.getSourceImage(ae);
            implAssociated=m_cViewer.getAssociatedImage();
            if(implAssociated!=null&&implAssociated!=implAssociated0) {
                implAssociated.getWindow().getCanvas().addPaintListener(this);
                implAssociated.getWindow().getCanvas().addMouseListener(this);
            }
            if(impl==implAssociated){
                if(implAssociated!=null){
                    if(HighlightTracksOnAssociatedImagesRB.isSelected()){
                        m_cViewer.getAssociatedDisplayer().setHighlight(true);
                        highlightIPOGsOnAssociatedImages();
                    } else
                        m_cViewer.getAssociatedDisplayer().setHighlight(false);
                }
            }
            if(impl==implSeries){
                if(HighlightTracksOnSeriesImageRB.isSelected()){
                    m_cTrackDisplayer.setHighlight(true);
                    highlightTracksOnSeriesImage();
                } else
                    m_cTrackDisplayer.setHighlight(false);
            }
            implAssociated0=implAssociated;
        }
        if(ae.getActionCommand().contentEquals("Next Track")){
            ArrayList<IPOGTrackNode> IPOGTs=m_cvSelectedIPOGTracks;
            if(m_cvSelectedIPOGTracks.isEmpty()) m_cvSelectedIPOGTracks=m_cStackIPOT.m_cvIPOGTracks;
            IPOGTrackNode IPOGT=getNextSelectedTrack(m_cSelectedIPOGT,1);
            m_cvSelectedIPOGTracks=IPOGTs;
            if(IPOGT!=null) selectTrack(IPOGT.m_cvIPOGs.get(0));
        }
        if(ae.getActionCommand().contentEquals("Previous Track")){
            ArrayList<IPOGTrackNode> IPOGTs=m_cvSelectedIPOGTracks;
            if(m_cvSelectedIPOGTracks.isEmpty()) m_cvSelectedIPOGTracks=m_cStackIPOT.m_cvIPOGTracks;
            IPOGTrackNode IPOGT=getNextSelectedTrack(m_cSelectedIPOGT,-1);
            m_cvSelectedIPOGTracks=IPOGTs;
            if(IPOGT!=null) selectTrack(IPOGT.m_cvIPOGs.get(0));
        }
        if(ae.getActionCommand().contentEquals("SetComplexity")){
            setIPOGTComplexity();
        }
    }
    
    IPOGTrackNode getNextSelectedTrack(IPOGTrackNode IPOGT, int direction){
        if(m_cvSelectedIPOGTracks==null) return null;
        int i,len=m_cvSelectedIPOGTracks.size(),index;
        for(i=0;i<len;i++){
            if(IPOGT==m_cvSelectedIPOGTracks.get(i)){
                index=CommonMethods.circularAddition(len, i, direction);
                return m_cvSelectedIPOGTracks.get(index);
            }
        }
        return null;
    }

    void highlightSelectedIPOs(){
        int w=pixels[0].length,h=pixels.length;
        String choice=(String)HighlightChoiceCB.getSelectedItem();
        ArrayList <Point> points=null;
        if(choice.contentEquals("centers")) points=IPOGaussianNodeHandler.getIPOCenters(m_cvSelectedIPOs);
        if(choice.contentEquals("contour2")) points=IPOGaussianNodeHandler.getIPOContour2(m_cvSelectedIPOs);
        if(choice.contentEquals("contour3")) points=IPOGaussianNodeHandler.getIPOContour3(m_cvSelectedIPOs);
        if(choice.contentEquals("shape2")) points=IPOGaussianNodeHandler.getIS2(m_cvSelectedIPOs,w,h);
        if(choice.contentEquals("shape3")) points=IPOGaussianNodeHandler.getIS3(m_cvSelectedIPOs,w,h);
        ImagePlus impl=WindowManager.getCurrentImage();
        CommonGuiMethods.highlightPoints(impl, points, Color.red);
    }
    ArrayList<IPOGaussianNode> collectIPOGsInScope(int slice,intRange xRange, intRange yRange){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList(), cvIPOGs=m_cStackIPOs.getIPOGs(slice);
        return collectIPOGsInScope(m_cStackIPOs.getIPOGs(slice),xRange,yRange);
    }
    ArrayList<IPOGaussianNode> collectIPOGsInScope(ArrayList<IPOGaussianNode> cvIPOGs,intRange xRange, intRange yRange){
        if(cvIPOGs==null) return null;
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        IPOGaussianNode IPOG;
        Point pt;
        int i,len=cvIPOGs.size();
        for(i=0;i<len;i++){
            IPOG=cvIPOGs.get(i);
            pt=IPOG.getCenter();
            if(xRange.contains(pt.x)&&yRange.contains(pt.y)) IPOGs.add(IPOG);
        }
        return IPOGs;
    }
    public int collectIPOGTsInScope(ArrayList<IPOGTrackNode> cvIPOGTs,ArrayList<IPOGaussianNode> cvUntrackedIPOs, int sliceI, int sliceF, ArrayList<intRange> xRanges, ArrayList<intRange> yRanges){
        ArrayList<Integer> TrackIndexes=new ArrayList();
        ArrayList<IPOGTrackNode> IPOGTs=m_cStackIPOT.m_cvIPOGTracks;
        ArrayList<IPOGaussianNode> IPOGs;
        intRange xRange,yRange;
        IPOGaussianNode IPOG;
        int slice, i, j, len,tkIndex;
        for(slice=sliceI;slice<=sliceF;slice++){
            xRange=xRanges.get(slice-sliceI);
            yRange=yRanges.get(slice-sliceI);
            IPOGs=collectIPOGsInScope(slice,xRange,yRange);
            if(IPOGs==null) continue;
            len=IPOGs.size();
            for(i=0;i<len;i++){
                IPOG=IPOGs.get(i);
                tkIndex=IPOG.TrackIndex;
                if(tkIndex==-1){
                    cvUntrackedIPOs.add(IPOG);
                    continue;
                }
                if(!CommonMethods.containsContent(TrackIndexes, tkIndex)){
                    TrackIndexes.add(tkIndex);
                    cvIPOGTs.add(IPOGTs.get(tkIndex));
                }
            }
        }
        return 1;
    }
    int highlightTracksOnSeriesImage(){
        if(m_cTrackDisplayer==null) return -1;

        ArrayList<intRange> xRanges=new ArrayList(),yRanges=new ArrayList();
        intRange sliceRange=new intRange();
        m_cTrackDisplayer.getDisplayScope(sliceRange, xRanges, yRanges);
        if(sliceRange.emptyRange()){
            return -1;
        }
        ArrayList<IPOGTrackNode> IPOGTs=new ArrayList();
        m_cvUntrackedIPOGsOnSeriesImage=new ArrayList();
        collectIPOGTsInScope(IPOGTs,m_cvUntrackedIPOGsOnSeriesImage,sliceRange.getMin(),sliceRange.getMax(),xRanges,yRanges);
        Color c=Color.black;
        highlightIPOGsOnSeriesImage(m_cvUntrackedIPOGsOnSeriesImage,c);
        highlightTracksOnSeriesImage(IPOGTs);
        return 1;
    }
    int highlightTracksOnSeriesImage(ArrayList<IPOGTrackNode> IPOGTs){
        if(m_cTrackDisplayer==null) return -1;
        boolean showBundle=ShowIPOTBundleRB.isSelected();
        m_cTrackDisplayer.removeCollection("IPOGT*");
        int i,len=IPOGTs.size(),num=0;
        IPOGTrackNode IPOGT,IPOGTt;
        if(m_cvTrackColors==null) m_cvTrackColors=new ArrayList();
        m_cvTrackColors.clear();

        ArrayList<Integer> nvBundleIndexes=new ArrayList();
        int BundleIndex;

        Color c,ct;
        for(i=0;i<len;i++){
            IPOGT=IPOGTs.get(i);
            if(IPOGT==m_cIPOGTOnSeriesImage) {
                c=Color.red;
                m_cvTrackColors.add(c);
                //position the main IPOGT to the first position, it's neccesory for showing the bundle color corectly
                IPOGTt=IPOGTs.get(0);
                IPOGTs.set(0, IPOGT);
                IPOGTs.set(i, IPOGTt);

                ct=m_cvTrackColors.get(0);
                m_cvTrackColors.set(0, c);
                m_cvTrackColors.set(i, ct);
                continue;
            }
            num++;
            c=m_cTrackDisplayer.getColor(num);
            m_cTrackDisplayer.addCollection(makeHighlightCollectionNode(IPOGT,c));
            m_cvTrackColors.add(c);
        }
        m_cvHighlightedIPOGTs=IPOGTs;
        return 1;
    }
    void setBundleColorsOnSeriesImage(){
        IPOGTrackNode IPOGT;
        Color c;//cmt: the main track color
        int i,len=m_cvHighlightedIPOGTs.size(),bundleIndex,index0;
        ArrayList<Integer> BundleIndexes=new ArrayList();
        for(i=0;i<len;i++){
            IPOGT=m_cvHighlightedIPOGTs.get(i);
            bundleIndex=IPOGT.BundleIndex;
            index0=CommonMethods.IndexOf(BundleIndexes, bundleIndex);
            if(index0<0) continue;
            c=m_cvTrackColors.get(index0);
            m_cvTrackColors.set(i, c);
        }
    }
    ArrayList<Color> retrieveHighlightColors(ArrayList<IPOGaussianNode> IPOGs){
        ArrayList<Color> colors=new ArrayList();
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            colors.add(retrieveHighlightColor(IPOGs.get(i)));
        }
        return colors;
    }
    Color retrieveHighlightColor(IPOGaussianNode IPOG){
        Color c=null;
        int i,len=m_cvHighlightedIPOGTs.size();
        int trackIndex=IPOG.TrackIndex;
        if(trackIndex<0) return c.BLACK;
        for(i=0;i<len;i++){
            if(m_cvHighlightedIPOGTs.get(i).TrackIndex==trackIndex) return m_cvTrackColors.get(i);
        }
        return c;
    }
    HighlightingRoiCollectionNode makeHighlightCollectionNode(IPOGTrackNode IPOGT,Color c){
        intRange ir=m_cTrackDisplayer.getSliceRange();
        Point pt;
        IPOGaussianNode IPOG;
        Roi roi;
        HighlightingRoiCollectionNode aCollection=new HighlightingRoiCollectionNode(implSeries,"IPOGT"+IPOGT.TrackIndex);
        int sliceI=ir.getMin(),sliceF=ir.getMax(),slice,slice0=IPOGT.m_cvIPOGs.get(0).sliceIndex,len=IPOGT.m_cvIPOGs.size(),index;
        for(slice=sliceI;slice<=sliceF;slice++){
            index=slice-slice0;
            if(index<0) continue;
            if(index>=len) break;
            IPOG=IPOGT.m_cvIPOGs.get(index);
            if(IPOG==null) continue;
            pt=IPOGT.m_cvIPOGs.get(index).getCenter();
            roi=m_cTrackDisplayer.makeHighlighterOnSeriesImage(pt.x, pt.y, slice, c);
            if(roi==null) continue;
            aCollection.addRoi(roi,c);
        }
        return aCollection;
    }
    int highlightIPOGsOnSeriesImage(ArrayList<IPOGaussianNode> IPOGs, Color c){
        if(m_cTrackDisplayer==null) return -1;
        HighlightingRoiCollectionNode aCollection=m_cTrackDisplayer.getCollection("UntrackedIPOGs");
        if(aCollection==null){
            aCollection=new HighlightingRoiCollectionNode(implSeries,"UntrackedIPOGs");
            m_cTrackDisplayer.addCollection(aCollection);
        }else{
            aCollection.clear();
        }
        int i,len=IPOGs.size();
        IPOGaussianNode IPOG;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            if(IPOG==null) continue;
            aCollection.addRoi(m_cTrackDisplayer.makeHighlighterOnSeriesImage(IPOG.getXc(), IPOG.getYc(), IPOG.sliceIndex, c),c);
        }
        return 1;
    }
    int highlightIPOGsOnAssociatedImages(){
        AssociatedImageDisplayer displayer=m_cViewer.getAssociatedDisplayer();
        if(m_cViewer==null) return -1;
        implAssociated=m_cViewer.getAssociatedImage();
        if(implAssociated==null) return -1;
        int slice=implOriginal.getCurrentSlice();
//        if(implAssociated==implAssociated0&&previousSlice==slice) return -1;
        if(m_cvHighlightedIPOGTs==null) return -1;
        int i,len=m_cvHighlightedIPOGTs.size();
        IPOGaussianNode IPOG;
        IPOGTrackNode IPOGT;
        Point pt;
        Color c;
        HighlightingRoiCollectionNode aCollection;
        displayer.removeCollection("IPOG*");
        int num=0;
        for(i=0;i<len;i++){
            IPOGT=m_cvHighlightedIPOGTs.get(i);
            IPOG=IPOGT.getIPOG(slice);
            if(IPOG==null) continue;
            num++;
            pt=IPOG.getCenter();
            c=m_cvTrackColors.get(i);
            aCollection=displayer.makeCorrespondingPointsHLCollection("IPOG"+num, pt, c);
            displayer.addCollection(aCollection);
        }
        len=m_cvUntrackedIPOGsOnSeriesImage.size();
        for(i=0;i<len;i++){
            IPOG=m_cvUntrackedIPOGsOnSeriesImage.get(i);
            if(IPOG==null) continue;
            if(IPOG.sliceIndex!=slice) continue;
            pt=IPOG.getCenter();
            aCollection=displayer.makeCorrespondingPointsHLCollection("IPOG"+num, pt, Color.black);
            displayer.addCollection(aCollection);
        }
        previousSlice=slice;
        implAssociated0=implAssociated;
                return 1;
    }
    void showTable(JViewport viewport, int index){
        switch (index){
            case 0:
                if(!LockTable1RB.isSelected()) Table1Pane.setViewport(viewport);
                break;
            case 1:
                if(!LockTable2RB.isSelected()) Table2Pane.setViewport(viewport);
                break;
            default:
                break;
        }
    }
    void showSliceRange(){
        int sliceI,sliceF;
        if(m_cStackIPOT!=null){
            sliceI=m_cStackIPOT.sliceI;
            sliceF=m_cStackIPOT.sliceF;
            StackSliceRangeLB.setText("Slice "+sliceI+" to "+sliceF);
            AnalyzeSliceTF.setText(""+sliceI);
        }else if(m_cStackIPOs!=null){
            sliceI=m_cStackIPOs.sliceI;
            sliceF=m_cStackIPOs.sliceF;
            StackSliceRangeLB.setText("Slice "+sliceI+" to "+sliceF);
            AnalyzeSliceTF.setText(""+sliceI);
        }
    }
    int processTrackPlotMouseClick(IPOGTrackPlotWindow tpw,MouseEvent me){
        int nRegressionOrder=m_cIPOGTTransitionAnalyzer.getRegressionOrder();
        DoubleRange xRange=new DoubleRange(),yRange=new DoubleRange();
        ImagePlus impl=tpw.pw.getImagePlus(),impls=CommonGuiMethods.getSourceImage(me);
        Rectangle rect;
        if(impls==null) {
            impls=m_cIPOGTTransitionAnalyzer.m_cIPOGTPW.pw.getImagePlus();
            if(impls.getWindow().getCanvas()!=me.getSource())return -1;
        }
        if(impl!=impls) return -1;
        Roi roi=impl.getRoi();
        Point cursor=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
        int status;
        if(roi!=null){
            rect=roi.getBoundingRect();
            status=CommonGuiMethods.getCoordinateRanges(tpw.pw,roi,xRange,yRange);
            if(status==-1) return -1;
            int sliceI=(int)(xRange.getMin()+0.5),sliceF=(int)(xRange.getMax()+0.5);
            if(roi.contains(cursor.x, cursor.y)){
                if(me.isControlDown()){
                    if(sliceF>=sliceI) showTrackAveIPO(tpw.IPOGT,sliceI,sliceF);
                }else{
                    tpw.setLimit(sliceI, sliceF);
                }
            }
        }else{

        }
        return 1;
    }
    
    IPOGaussianNodeComplex buildTrackAveIPOG(IPOGTrackNode IPOGT, int sliceI, int sliceF){
        intRange xRange=new intRange(), yRange=new intRange();
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
        pnScratch1=CommonStatisticsMethods.getIntArray2(pnScratch1, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
/*        IPOGaussianNode IPOGAve=IPOGT.buildAveIPO(pixelsCompenStack, pnScratch, pnScratch1, sliceI, sliceF,xRange,yRange);
        IPOGaussianNode IPOGAve=IPOGTrackNode.buildAveIPOG_FullModel(IPOGT, pixelsCompenStack, pnScratch, pnScratch1, sliceI, sliceF);
        FittingResultsNode aResultsNode=IPOGaussianFitter.getFullModelFitting(IPOGAve.cModel, 0.001,1);
        ArrayList<IPOGaussianNode> cvIPOGs=new ArrayList();
        int nModels=aResultsNode.m_cvModels.size();
        if(nModels<2){
            nModels=nModels;
        }
        IPOGaussianNodeHandler.buildIPOGaussianNode(cvIPOGs, aResultsNode.getFittedModel(), -1,-1);
        IPOGaussianNodeComplex IPOG=new IPOGaussianNodeComplex(cvIPOGs);*/
        IPOGaussianNodeComplex IPOG=IPOGTrackNode.buildAveIPOG_FullModel(IPOGT, pixelsCompenStack, pnScratch, pnScratch1, sliceI, sliceF,null,null);
        IPOG.sortIPOGs(xRange, yRange);
        return IPOG;
    }
    void buildHeadIPOGs(ArrayList<IPOGTrackNode> IPOGTs){
        int i,len=IPOGTs.size();
        IPOGTrackNode IPOGT;
        for(i=0;i<len;i++){
            IPOGT=IPOGTs.get(i);
            IPOGT.m_cHeadIPOG=buildHeadIPOG(IPOGT);
        }
    }
    public IPOGaussianNodeComplex buildHeadIPOG(IPOGTrackNode IPOGT){
        int sI=IPOGT.firstSlice,sF=Math.min(IPOGT.lastSlice,sI+10);
        return buildTrackAveIPOG(IPOGT,sI,sF);
    }
    public DoubleRange calHeadValueRange(ArrayList<IPOGTrackNode> IPOGTs, String plottingOption){
        DoubleRange dr=new DoubleRange();
        int i,len=IPOGTs.size(),sI,sF,length=5,j,num;;
        IPOGTrackNode IPOGT;
        IPOGaussianNode IPOGt;
        
        double ave=0;   
        int numN=0;
        for(i=0;i<len;i++){
            IPOGT=IPOGTs.get(i);
            if(!IPOGT.normalTrackHeadShape()) continue;
            sI=IPOGT.firstSlice;
            sF=Math.min(IPOGT.lastSlice,sI+length);
            ave=0;
            num=0;
            for(j=sI;j<=sF;j++){
                IPOGt=IPOGT.getIPOG(j);
                if(IPOGt==null) continue;
                ave+=IPOGt.getValue(plottingOption);
                num++;
            }
            if(num==0) continue;
            ave/=num;
            if(dr.contains(ave)) continue;
            
            if(IPOGT.m_cHeadIPOG==null){
                IPOGT.m_cHeadIPOG=buildHeadIPOG(IPOGT);
            }
            if(!IPOGaussianNodeHandler.isNormalShape(IPOGT.m_cHeadIPOG))continue;
            numN++;
            dr.expandRange(ave);
        }
        return dr;
    }

    int showTrackAveIPO(IPOGTrackNode IPOGT,int sliceI, int sliceF0){
        int sliceF=sliceF0,h=pixelsCompenStack.length,w=pixelsCompenStack[0].length;
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
        pnScratch1=CommonStatisticsMethods.getIntArray2(pnScratch1, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
        intRange xRange=new intRange(), yRange=new intRange();
//        SubpixelGaussianMean sgm=new SubpixelGaussianMean(pnScratch,1.,5,xRange,yRange);
        IPOGaussianNodeComplex IPOGAve=IPOGTrackNode.buildAveIPOG_FullModel(IPOGT, pixelsCompenStack, pnScratch, pnScratch1, sliceI, sliceF0,xRange,yRange,true,true,IPOGContourParameterNode.GaussianMean);
        IPOGTrackNode.buildAveIPOsRawPixelContour(IPOGAve, IPOGT, pixelsStack, pnScratch, pnScratch, sliceI, sliceF, true);
        IPOGContourParameterNode cP1=IPOGAve.getContour(IPOGContourParameterNode.IPOG);
        IPOGContourParameterNode cP2=IPOGAve.getContour(IPOGContourParameterNode.GaussianMean);
        IPOGContourParameterNode cP3=IPOGAve.getContour(IPOGContourParameterNode.GaussianMeanRaw);
        
        String[][] psData,psDataT;
        psData=cP1.getContourParsAsStrings();
        int i,len=psData[0].length;
        psDataT=new String[4][len+1];
        
        psDataT[0][0]="Mode";
        psDataT[1][0]="IPOG";
        psDataT[2][0]="GaussianMean";
        psDataT[3][0]="MeanRaw";
        for(i=0;i<len;i++){
            psDataT[0][i+1]=psData[0][i];
        }
        for(i=0;i<len;i++){
            psDataT[1][i+1]=psData[1][i];
        }
        if(cP2!=null){
            psData=cP2.getContourParsAsStrings();
            for(i=0;i<len;i++){
                psDataT[2][i+1]=psData[1][i];
            }
        }
        if(cP3!=null){
            psData=cP3.getContourParsAsStrings();
            for(i=0;i<len;i++){
                psDataT[3][i+1]=psData[1][i];
            }
        }

        String stSliceRange=" slice"+IPOGAve.sliceI+" to slice"+IPOGAve.sliceF;
        CommonGuiMethods.displayTable("Contour Parameters: "+stSliceRange, psDataT);
        
        PlotWindowPlus pw1=new PlotWindowPlus(cP1.dvSmoothedDentDepth,"Smoothed Dent Depths"+stSliceRange,"IPOG_X","IPOG_Y",2,2,Color.BLACK);
        pw1.addPlot("Gaussian Mean", cP2.dvSmoothedDentDepth, 2, 2, Color.red, ControlDown);
        pw1.addPlot("Gaussian Mean Raw", cP3.dvSmoothedDentDepth, 2, 2, Color.blue, ControlDown);
        PlotWindowPlus pw2=new PlotWindowPlus(cP1.dvSmoothedDist,"Smoothed Distance"+stSliceRange,"IPOG_X","IPOG_Y",2,2,Color.BLACK);
        pw2.addPlot("Gaussian Mean", cP2.dvSmoothedDist, 2, 2, Color.red, ControlDown);
        pw2.addPlot("Gaussian Mean Raw", cP3.dvSmoothedDist, 2, 2, Color.blue, ControlDown);
        
//            public SubpixelGaussianMean(int[][] pixels, Double Sigma, double R, intRange xFRange, intRange yFRange){
        
//        IPOGAve.buildContour_Percentage();
//        FittingResultsNode aResultsNode=IPOGaussianFitter.getFullModelFitting(IPOGAve.cModel, 0.0001);
/*        while(aResultsNode==null){
            sliceF-=2;
            if(sliceF<sliceI) {
                aResultsNode=new FittingResultsNode();
                aResultsNode.m_cvModels.add(IPOGaussianExpander.getDummyIPOGModel());
                break;
            }
            IPOGAve=IPOGT.buildAveIPO(pixelsCompenStack, pnScratch, pnScratch1, sliceI, sliceF,null,null);
            aResultsNode=IPOGaussianFitter.getFullModelFitting(IPOGAve.cModel, 0.0001);
        }*/
        ImageShape cIS=IPOGAve.cModel.m_cIS;

        intRange xRangeRWA=cIS.getXrange(),yRangeRWA=cIS.getYrange();
        int wa=xRangeRWA.getRange(),ha=yRangeRWA.getRange();

        Point p0=cIS.getCenter();
        ImagePlus impl=CommonMethods.getBlankImage(ImagePlus.GRAY16, wa, ha);
        
        impl.setTitle("Track"+IPOGT.TrackIndex+", slice"+sliceI+" to slice"+sliceF+" cx="+p0.x+" cy="+p0.y);
        pixelsAveIPOs=CommonStatisticsMethods.getIntArray2(pixelsAveIPOs, wa, ha);

        //the following is to show the contour
        int[][] pixels=new int[ha][wa];
        IPOGAve.cModel.copyData(pixels, 0, 0);
        CommonMethods.setPixels(impl, pixels);
        
        intRange ir=CommonStatisticsMethods.getRange(pixels);
        double mean=CommonStatisticsMethods.getMean(pixels, null);  
        int diff=ir.getMax()-(int)mean;
        IPOGAve.cModel.calDataRange();
        DoubleRange dRange=IPOGAve.cModel.cDataRange;

        int pixeln=(int)(dRange.getMin()),pixelx=(int)(dRange.getMax()+0.5);

//        CommonMethods.setPixels(impl, pixels);
        impl.getProcessor().setMinAndMax(pixeln, pixelx);
        impl.show();
        impl.getWindow().getCanvas().setMagnification(12);
        CommonGuiMethods.optimizeWindowSize(impl);
        CommonMethods.refreshImage(impl);
        impl.getWindow().toFront();
        IPOGAve.cModel.toGaussian2D_GaussianPars();
        m_cMasterForm.getFittingGUI().getImageFitter().updateFittingModel(IPOGAve.cModel);
        m_cMasterForm.getFittingGUI().getImageFitter().addFittingImage(impl);
        return 1;
    }
    public void exportTracks(){
        String path;
        m_cStackIPOT.sImagePath=sOriginalImagePath;
        if(sOriginalImagePath!=null)
            path=FileAssist.changeExt(sOriginalImagePath, "TLF");
        else
            path=FileAssist.getFilePath("export level detected IPOG tracks", FileAssist.defaultDirectory, "Track Level Files", "TLF", false);
        m_cStackIPOT.exportTracksLevelInfo(path);
    }
    public double[] getStackMinima(String key){
        return m_cStackIPOT.getStackMinima(key);
    }
    public int[] getStackMinIndexes(String key){
        return m_cStackIPOT.getStackMinIndexes(key);
    }
    void exportLevelInfo(){
//        String path0=FileAssist.getFilePath("Export Track Level Info", FileAssist.defaultDirectory, "Level Info File", "txt", false);
        String path0=FileAssist.changeExt(sOriginalImagePath, "txt");
        exportLevelInfo(path0);
    }
    public void exportLevelInfo(String path0){        
        String path=path0;
        path=FileAssist.getExtendedFileName(path0, "_Auotomated_Verified");
        Formatter fm_Auotomated_Verified=FileAssist.getFormatter(path,true);
        
        path=FileAssist.getExtendedFileName(path0, "_Automated_Excluded");
        Formatter fm_Automated_Excluded=FileAssist.getFormatter(path,true);
        
        path=FileAssist.getExtendedFileName(path0, "_Manual_Verified");
        Formatter fm_Manually_Verified=FileAssist.getFormatter(path,true);
        
        path=FileAssist.getExtendedFileName(path0, "_Manual_Excluded");
        Formatter fm_Manually_Excluded=FileAssist.getFormatter(path,true);
        
        path=FileAssist.getExtendedFileName(path0, "_NotAnalyzable");
        Formatter fm_NotAnalyzable=FileAssist.getFormatter(path,true);
        
        IPOGTrackNode IPOGT;
        ArrayList<IPOGTrackNode> IPOGTs=m_cStackIPOT.m_cvIPOGTracks;
        int len=IPOGTs.size(),i;
        String LevelInfo=null;
        StringBuffer names=new StringBuffer(),values=new StringBuffer();
        String quantityName=(String)TrackPlotOptionCB.getSelectedItem();
        boolean firstAV=true,firstMV=true,firstME=true,firstAE=true,firstNV=true;
        for(i=0;i<len;i++){
            IPOGT=IPOGTs.get(i);
            if(IPOGT.m_cLevelInfo==null) continue;
            clear(names);
            clear(values);
            IPOGT.getLevelInfoAsString(names,values,quantityName,false);
            
            names.append(",recordPath");
            values.append(","+sOriginalImagePath);
            
            if(IPOGT.m_cLevelInfo.Verified_Automatically()){
                if(firstAV){
                    PrintAssist.printString(fm_Auotomated_Verified,names.toString());
                    PrintAssist.endLine(fm_Auotomated_Verified);
                    firstAV=false;
                }
                PrintAssist.printString(fm_Auotomated_Verified,values.toString());
                PrintAssist.endLine(fm_Auotomated_Verified);}
            else if(IPOGT.m_cLevelInfo.Excluded_Automatically()){
                if(firstAE){
                    PrintAssist.printString(fm_Automated_Excluded,names.toString());
                    PrintAssist.endLine(fm_Automated_Excluded);
                    firstAE=false;
                }
                PrintAssist.printString(fm_Automated_Excluded,values.toString());
                PrintAssist.endLine(fm_Automated_Excluded);}
                        
           if(IPOGT.m_cLevelInfo.NotAnalyzable()){
                if(firstNV){ 
                    PrintAssist.printString(fm_NotAnalyzable,names.toString());
                    PrintAssist.endLine(fm_NotAnalyzable);
                    firstNV=false;
                }
                PrintAssist.printString(fm_NotAnalyzable,values.toString());
                PrintAssist.endLine(fm_NotAnalyzable);}
            
            clear(names);
            clear(values);
            if(IPOGT.m_cLevelInfo.m_cvLevelNodes_M.isEmpty()) continue;
            IPOGT.getLevelInfoAsString(names,values,quantityName,true);
            
            names.append(",recordPath");
            values.append(","+sOriginalImagePath);
            
            if(IPOGT.m_cLevelInfo.Verified_Manually()){
                if(firstMV){
                    PrintAssist.printString(fm_Manually_Verified,names.toString());
                    PrintAssist.endLine(fm_Manually_Verified);
                    firstMV=false;
                }
                PrintAssist.printString(fm_Manually_Verified,values.toString());                
                PrintAssist.endLine(fm_Manually_Verified);}
            else if(IPOGT.m_cLevelInfo.Excluded_Automatically()){
                if(firstME=true){
                    PrintAssist.printString(fm_Manually_Excluded,names.toString());
                    PrintAssist.endLine(fm_Manually_Excluded);
                    firstME=false;
                }
                PrintAssist.printString(fm_Manually_Excluded,values.toString());
                PrintAssist.endLine(fm_Manually_Excluded);}
            
        }
        fm_Auotomated_Verified.close();
        fm_Automated_Excluded.close();
        fm_Manually_Verified.close();
        fm_Manually_Excluded.close();
        fm_NotAnalyzable.close();
    }
    void clear(StringBuffer sb){
        int len=sb.length();
        if(len>0) sb.delete(0, len);
    }
    public void importLevelInfo(){
        String path=FileAssist.getFilePath("import level info file", FileAssist.defaultDirectory, "IPOGT Level Info File", "txt", true);
        ArrayList<String> lines=new ArrayList();
        AsciiInputAssist.getFileContentsAsStringArray(path, lines);
        int i,nLines=lines.size();
        String line;
        int nMaxEvents=0,nLevel,nMaxLevel=-1;
        if(m_cvLevelInfoSummaryNodes==null)
            m_cvLevelInfoSummaryNodes=new ArrayList();
        else
            m_cvLevelInfoSummaryNodes.clear();

        IPOGTLevelInfoSummaryNode lNode;
        for(i=0;i<nLines;i++){
            if(i%2==0) continue;
            line=lines.get(i);
            lNode=new IPOGTLevelInfoSummaryNode(line);
            m_cvLevelInfoSummaryNodes.add(lNode);
            nLevel=Integer.parseInt(lNode.nLevels);
            if(nLevel>nMaxLevel) nMaxLevel=nLevel;
        }
        nMaxEvents=nMaxLevel+1;
        lNode=m_cvLevelInfoSummaryNodes.get(0);
        int len=lNode.getNumString(nMaxEvents);

        nLines=m_cvLevelInfoSummaryNodes.size();

        String[] ColumnHead=lNode.getParNamesAsStringArray(len);
        String[][] psData=new String[nLines][];

        for(i=0;i<nLines;i++){
            psData[i]=m_cvLevelInfoSummaryNodes.get(i).getParsAsStringArray(len);
        }
        
        m_cLevelInfoTable=CommonGuiMethods.buildCommonTable(ColumnHead, psData, 0, psData.length-1);
        m_cLevelInfoTable.getModel().addTableModelListener(this);
        m_cLevelInfoTable.setRowSelectionAllowed(true);
        m_cLevelInfoTable.setColumnSelectionAllowed(true);
        m_cLevelInfoViewport=new JViewport();
        m_cLevelInfoViewport.setView(m_cLevelInfoTable);
        Table1Pane.setViewport(m_cLevelInfoViewport);
    }
    int setIPOGTComplexity(){
        if(m_cStackIPOT==null) return -1;
        if(pixelsStack==null) setStackPixels();
        ArrayList<IPOGTrackNode> IPOGTs=m_cStackIPOT.m_cvIPOGTracks;
        int i,len=IPOGTs.size();
        int ws=20;
        for(i=0;i<len;i++){
            IPOGTs.get(i).setHeadComboMaxima(pixelsStack, pnScratch, pnScratch1, ws);
        }
        return 1;
    }
    void computeRawPeaks(){
        int index,slice,i,iFE,j;
        String path=sOriginalImagePath.substring(4);//rwa ....tif
        String dir=FileAssist.getDirectory(sOriginalImagePath);
        String name=FileAssist.getFileName(sOriginalImagePath)+"."+FileAssist.getFileExtention(sOriginalImagePath);
        name=name.substring(4);
        ImagePlus impl=CommonMethods.importImage(dir+name);
        pixelRangeRaw=CommonMethods.getPixelRange(impl);
        

        int[][][] pixelsRaw;
        int w=impl.getWidth(),h=impl.getHeight(),sI=m_cStackIPOT.sliceI,sF=m_cStackIPOT.sliceF;
        int len=sF-sI+1;
        pixelsRaw=new int[len][h][w];
        for(slice=sI;slice<=sF;slice++){
            index=slice-sI;
            impl.setSlice(slice);
            CommonMethods.getPixelValue(impl, slice, pixelsRaw[index]);
        }
        m_cStackIPOT.pixelsRaw=pixelsRaw;
        IPOGaussianNode IPOG;
        ImageShape cIS1=new CircleImage(1),cIS3=new CircleImage(3),cISbkg=new CircleImage(25), cISRing=new Ring(3,4);
        cIS1.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cIS3.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cISRing.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cISbkg.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        
        for(slice=sI;slice<=sF;slice++){
            analyzeSlice(slice);
            len=m_cvIPONodes.size();
            index=slice-sI;
            for(i=0;i<len;i++){
                IPOG=m_cvIPONodes.get(i);
                cIS1.setCenter(IPOG.getCenter());
                cIS3.setCenter(IPOG.getCenter());
//                IPOG.peak1Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS1);
//                IPOG.peak3Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS3);
                IPOG.peak1Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS1)-ImageShapeHandler.getMean(pixelsRaw[index], cISRing);//13219
                IPOG.peak3Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS3)-ImageShapeHandler.getMean(pixelsRaw[index], cISRing);//13219
            }
        }
        
        IPOGTrackNode IPOGT;
        ArrayList<IPOGExtention> cvIPOGEs;
        IPOGExtention IPOGE;
        for(i=0;i<m_cStackIPOT.nNumTracks;i++){
            IPOGT=m_cStackIPOT.m_cvIPOGTracks.get(i);
            cvIPOGEs=IPOGT.cvIPOGExts;
            len=cvIPOGEs.size();
            for(j=0;j<len;j++){
                IPOGE=cvIPOGEs.get(j);
                index=IPOGE.sliceIndex-sI;
                cIS1.setCenter(IPOGE.center);
                cIS3.setCenter(IPOGE.center);
                cISbkg.setCenter(IPOGE.center);
//                IPOGE.peak1Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS1);
//                IPOGE.peak3Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS3);
                IPOGE.peak1Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS1)-ImageShapeHandler.getMean(pixelsRaw[index], cISRing);//13219;
                IPOGE.peak3Raw=ImageShapeHandler.getMean(pixelsRaw[index], cIS3)-ImageShapeHandler.getMean(pixelsRaw[index], cISRing);//13219;
                IPOGE.dBackground=ImageShapeHandler.getMean(pixelsCompenStack[index], cISbkg);
            }
        }
    }
    void extendIPOGTracks(){
        int i,slice,numTracks=m_cStackIPOT.nNumTracks,sliceI=m_cStackIPOT.sliceI,sliceF=m_cStackIPOT.sliceF,sliceFE,h=pixels.length,w=pixels[0].length,lenE=IPOGExtention.lenExtention;
        int nTrackIndex,x,y;
        IPOGTrackNode IPOGT;
        IPOGaussianNode IPOG;
        int[] pnPixelRanges=new int[2],stamps[]=pnScratch;
        CommonMethods.getPixelValueRange_Stack(implp, pnPixelRanges);
        LandscapeAnalyzerPixelSorting cLAZer=new LandscapeAnalyzerPixelSorting(implp);
        
        int[][] pnTrackStamp=new int[h][w];
        double[][] pdHeights=new double[h][w];
        
        ArrayList<IPOGExtention> cvIPOGEs;
        IPOGExtention IPOGE;
        Point center=null,pt;
        
        for(i=0;i<numTracks;i++){
            m_cStackIPOT.m_cvIPOGTracks.get(i).cvIPOGExts.clear();
        }
        for(slice=sliceI;slice<=sliceF;slice++){
            IJ.showStatus("Extending Tacks: Slice"+slice);
            CommonMethods.getPixelValue(implOriginal, slice, pixels);
            cLAZer.updateAndStampPixels(pixels, stamps);
            
            m_cStackIPOT.stampIPOs(slice, pnTrackStamp, pdHeights);
            
            if(slice==373){
                slice=slice;
            }
            
            for(i=0;i<numTracks;i++){
                IPOGT=m_cStackIPOT.getIPOGT(i);
                if(slice<IPOGT.firstSlice) continue;
                sliceFE=Math.min(IPOGT.lastSlice+lenE, sliceF);
                if(slice>sliceFE) continue;
                
                IPOGT.lastSliceE=sliceFE;
                cvIPOGEs=IPOGT.cvIPOGExts;
                if(slice==sliceI) cvIPOGEs.clear();
                
                if(slice<=IPOGT.lastSlice){
                    IPOG=IPOGT.getIPOG(slice);
                    if(IPOG!=null) {
                        center=IPOG.getCenter();
                        cvIPOGEs.add(new IPOGExtention(IPOG));
                        continue;
                    }                    
                }
                
                //IPOG==null;
                if(cvIPOGEs.size()==0){
                    i=i;
                    continue;
                }
//                center=cvIPOGEs.get(cvIPOGEs.size()-1).center;
                if(slice>IPOGT.lastSlice){
                    center=IPOGT.getIPOG(IPOGT.lastSlice).getCenter();
                }else{
                    center=cvIPOGEs.get(cvIPOGEs.size()-1).center;
                }
                x=center.x;
                y=center.y;
                nTrackIndex=pnTrackStamp[y][x];
                pt=LandscapeAnalyzerPixelSorting.findLocalMaximum(pixels, w, h, x, y); 
                nTrackIndex=pnTrackStamp[pt.y][pt.x];
                if(nTrackIndex>=0) pt.setLocation(center);
                cvIPOGEs.add(new IPOGExtention(slice,pt,pixels,pixelsCompenStack[slice-sliceI]));         
                IPOGT.TrackExtended(true);
            }
        }
    }
    public int TLG_TLH_Conversion(String path, boolean bTraining){
//        String path=FileAssist.getFilePath("input the original image", FileAssist.defaultDirectory, "a tif file", "tif", true);
        path=FileAssist.changeExt(path, "TLG");
        if(FileAssist.fileExists(path)){
            importStackIPOT_LevelInfo(path);
            m_cStackIPOT.checkTrackindexes();
        } else {
            path = FileAssist.changeExt(sOriginalImagePath, "TRK");
            importStackIPOT(path);
        }
        m_cStackIPOT.sImagePath=path;
        this.sOriginalImagePath=path;
        m_cStackIPOT.checkTrackindexes();
        rebuildStackIPOs();
        m_cStackIPOT.checkTrackindexes();
        return 1;
    }
    public int detectTransitions_Batch(String path, boolean bTraining, boolean silent){
//        String path=FileAssist.getFilePath("input the original image", FileAssist.defaultDirectory, "a tif file", "tif", true);
        setInteractive(false);
        importAndSetOriginalImage(path);
        if(implOriginal==null) return -1;
        pixelRange=CommonMethods.getPixelRange(implOriginal);

//        path = FileAssist.changeExt(sOriginalImagePath, "TRK");
//        importStackIPOT(path);
 
        path=FileAssist.changeExt(sOriginalImagePath, "TLH");
        if(FileAssist.fileExists(path)){
            importStackIPOT_LevelInfo(path);
            m_cStackIPOT.checkTrackindexes();
        } else {
            path = FileAssist.changeExt(sOriginalImagePath, "TRK");
            importStackIPOT(path);
        }
       
        m_cStackIPOT.checkTrackindexes();
        rebuildStackIPOs();
        m_cStackIPOT.checkTrackindexes();
        if(implOriginal.getNSlices()>1) ShowAssociatedImageStackRB.setSelected(true);
        ShowAssociatedImageRB.setSelected(false);
//        ShowAssociatedImageRBActionPerformed(null);
        showProcessedImage();
        buildAssociatedImageStack();
        AdjustFirstSliceRB.setSelected(false);
//        m_cViewer.SynchronizeSlice();
        m_cStackIPOT.checkTrackindexes();

//        m_cViewer.setMagnification(4);
//        implOriginal.getWindow().getCanvas().setSrcRect(new Rectangle(0,0,25,25));
//        m_cViewer.synchronizeDisplay();
//        m_cViewer.setContrastChoice(implInt, ImageComparisonViewer.LocalPixelRange);
//        m_cViewer.updateAssociatedImages();
//        implAssociated=m_cViewer.getAssociatedImage();

        ViewSelectedIPORB.setSelected(true);
        ClickToSelectRB.setSelected(true);
        SelectTrackRB.setSelected(true);
//        IPOGTrackNode IPOGT=m_cStackIPOT.m_cvIPOGTracks.get(0);//12921
//        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();//12921
//        IPOGs.add(IPOGT.m_cvIPOGs.get(0));//12921
//        updateSelectedIPOTable(IPOGs);//12921
        DisplayTrackImageRB.setSelected(false);
//        DisplayTrackImageRBActionPerformed(null);
//        if(implAssociated!=null) m_cViewer.setMagnification(implAssociated, 8);
//        if(implSeries!=null) m_cViewer.setMagnification(implSeries, 4);

        m_cStackIPOT.checkTrackindexes();
        extendIPOGTracks();
        computeRawPeaks();
        buildHeightCutoff();
        showHeightCutoff();
        TrackPlotOptionCB.setSelectedItem("Peak3 Raw");
        selectTracks_Default(bTraining);
        appendFittedTracks();
        if(silent)
            return detectTransitionsInSelectedTracks_Silent();
        else
            return detectTransitionsInSelectedTracks();
    }
    public int detectTransitionsInSelectedTracks(){
        ArrayList<IPOGTrackNode> cvIPOGTs=m_cStackIPOT.m_cvSelectedIPOGTracks;
        int i,len=cvIPOGTs.size(),nMaxRisingInterval=2;
        if(len<=0) return -1;
        IPOGTrackNode IPOGT;
        String plottingOption=(String)TrackPlotOptionCB.getSelectedItem();
        m_cSelectedIPOGT=m_cvSelectedIPOGTracks.get(0);
        if(m_cIPOGTTransitionAnalyzer==null)//to initialize m_cIPOGTTransitionAnalyzer
            plotTrack(m_cSelectedIPOGT,false,IPOGTLevelTransitionAnalyzer.getMaxRizingInterval(plottingOption),m_cSelectedIPOGT.firstSlice,m_cSelectedIPOGT.lastSlice);
        else
            m_cIPOGTTransitionAnalyzer.updatePixels(pixelsStack, pixelsCompenStack, m_cStackIPOT.sliceI, pixelRange);
        
        IPOGTrackPlotWindow IPOGTPW;
        
        for(i=0;i<len;i++){
            IPOGT=cvIPOGTs.get(i);
            m_cIPOGTTransitionAnalyzer.addToStatusArea("Fitting Track"+IPOGT.TrackIndex);
            IPOGTPW=detectTransitions(IPOGT,"Peak3 Raw",nMaxRisingInterval);
//            IPOGTPW.pw.close();
//            PlotWindowPlus.handler.closeAllPlotWindowPlus();
         }
        return -1;
    }
    public int detectTransitionsInSelectedTracks_Silent(){
        ArrayList<IPOGTrackNode> cvIPOGTs=m_cStackIPOT.m_cvSelectedIPOGTracks;
        int i,len=cvIPOGTs.size(),nMaxRisingInterval=2;
        if(len<=0) return -1;
        IPOGTrackNode IPOGT;
        String plottingOption=(String)TrackPlotOptionCB.getSelectedItem();
/*        m_cSelectedIPOGT=m_cvSelectedIPOGTracks.get(0);
        if(m_cIPOGTTransitionAnalyzer==null)//to initialize m_cIPOGTTransitionAnalyzer
            plotTrack(m_cSelectedIPOGT,false,IPOGTLevelTransitionAnalyzer.getMaxRizingInterval(plottingOption),m_cSelectedIPOGT.firstSlice,m_cSelectedIPOGT.lastSlice);
        else
            m_cIPOGTTransitionAnalyzer.updatePixels(pixelsStack, pixelsCompenStack, m_cStackIPOT.sliceI, pixelRange);
        
        IPOGTrackPlotWindow IPOGTPW;*/
        
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
        pnScratch1=CommonStatisticsMethods.getIntArray2(pnScratch1, pixelsCompenStack[0][0].length, pixelsCompenStack[0].length);
        for(i=0;i<len;i++){
            IPOGT=cvIPOGTs.get(i);
            IJ.showStatus("Fitting Track"+IPOGT.TrackIndex);
            IJ.showProgress(i+1, len);
            IPOGTLevelTransitionAnalyzer.fitTrack_BallRolling(IPOGT, pixelsStack, pixelsCompenStack, pnScratch, pnScratch1, plottingOption,IPOGTLevelTransitionAnalyzer.RefitIPOGs);
//            IPOGTLevelTransitionAnalyzer.fitTrack_BallRolling(IPOGT, pixelsStack, pixelsCompenStack, pnScratch, pnScratch1, plottingOption,0);
         }
        return -1;
    }
    public IPOGTrackPlotWindow detectTransitions(IPOGTrackNode IPOGT, String plotOption,int nMaxRisingInterval){
        if(m_cvIPOGTPWs==null) m_cvIPOGTPWs=new ArrayList();
        if(IPOGT.lastSliceE<IPOGT.lastSlice) extendIPOGTracks();
        int sliceI=IPOGT.firstSlice,sliceF=IPOGT.lastSliceE;
        IPOGTrackPlotWindow IPOGTPW=new IPOGTrackPlotWindow(IPOGT,plotOption,nMaxRisingInterval,sliceI,sliceF);
        m_cvIPOGTPWs.add(IPOGTPW);

        if(m_cIPOGTTransitionAnalyzer==null) {
            intRange ir=pixelRange;
            if(((String)TrackPlotOptionCB.getSelectedItem()).contains("Raw")) ir=pixelRangeRaw;
            m_cIPOGTTransitionAnalyzer=new IPOGTLevelTransitionAnalyzer(this,pixelsStack,pixelsCompenStack,m_cStackIPOT.sliceI,ir);
            m_cIPOGTTransitionAnalyzer.setImageID(implOriginal.getTitle());
            m_cIPOGTTransitionAnalyzer.addActionListener(this);
            IPOGTLevelTransitionAnalyzerForm form=new IPOGTLevelTransitionAnalyzerForm(m_cIPOGTTransitionAnalyzer);
            form.setVisible(true);
//            m_cIPOGTTransitionAnalyzer.setVisible(true);
        }else if(m_cIPOGTTransitionAnalyzer.getDisplayingForm()==null){
            IPOGTLevelTransitionAnalyzerForm form=new IPOGTLevelTransitionAnalyzerForm(m_cIPOGTTransitionAnalyzer);
            form.setVisible(true);
        }else if(!m_cIPOGTTransitionAnalyzer.getDisplayingForm().isVisible()){
            m_cIPOGTTransitionAnalyzer.getDisplayingForm().setVisible(true);
        }
        
        IPOGTPW.pw.getCanvas().addMouseMotionListener(this);
        IPOGTPW.pw.getCanvas().addMouseListener(this);
        m_cIPOGTTransitionAnalyzer.updateIPOGTPW(IPOGTPW, plotOption);
        String sFittingOption="Filted Amp";
        String sFilteringOption="Rolling Ball";
        CommonGuiMethods.selectString(m_cIPOGTTransitionAnalyzer.getFittingOptionCB(),sFittingOption);
        CommonGuiMethods.selectString(m_cIPOGTTransitionAnalyzer.getFilteringOptionCB(),sFilteringOption);
        m_cIPOGTTransitionAnalyzer.fitTrack(sFittingOption);
        m_cIPOGTTransitionAnalyzer.calLevelIPOs();
        return IPOGTPW;
    }
    public void closeAll(){
        CommonMethods.closeAllImages();
    }
    public static void analyzeIPOGTTransitionStatistics(String path, String sQuantityName){
        IPOGTLevelInfoAnalyzer analyzer=new IPOGTLevelInfoAnalyzer(path, sQuantityName);
    }
    void showSelection(){
        IPOGaussianNode IPOG;
        ArrayList<IPOGaussianNode> IPOGs;
        ArrayList<Point> points=new ArrayList();
        int i,len=m_cvSelectedIPOGTracks.size(),slice=m_cStackIPOT.sliceI;
//        if(implSelection==null){
            implSelection=CommonMethods.cloneImage(implOriginal,slice);
            implSelection=CommonMethods.convertImage(implSelection, ImagePlus.COLOR_RGB, true);
            implSelection.setTitle("Selected Tracks");
            implSelection.show();
//        }        
        int w=implSelection.getWidth(),h=implSelection.getHeight();
        for(i=0;i<len;i++){
            IPOG=m_cvSelectedIPOGTracks.get(i).getIPOG(slice);
            if(IPOG!=null) points.add(IPOG.getCenter());
        }
        ImageShape cIS=new Ring(8,9);
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        len=points.size();
        Point pt;
        for(i=0;i<len;i++){
            pt=points.get(i);
            cIS.setCenter(pt);
            ImageShapeHandler.drawShape(cIS, implSelection, Color.RED.getRGB());
        }
        implSelection.show();
    }
    public void removeAllTrackWindows(){
        IPOGTrackPlotWindow tpw;
        synchronized (this){
            while(m_cvIPOGTPWs.size()>0){                
                tpw=m_cvIPOGTPWs.get(0);
                tpw.clearTrackPlotProtections();
                m_cvIPOGTPWs.remove(0);
            }
        }
    }
    static public int getLastSlice(){
        return lastSlice;
    }
    static public void setLastSlice(int sliceF){
        lastSlice=sliceF;
    }
    public int exportAFRR(String pathLevelInfo, Formatter Shapefm, Formatter Contourfm, String sQuantityName){
//        String path=FileAssist.getFilePath("input the original image", FileAssist.defaultDirectory, "a tif file", "tif", true);
        pathLevelInfo=FileAssist.changeExt(pathLevelInfo, "TLH");
        if(!FileAssist.fileExists(pathLevelInfo)){
            IJ.error("File "+pathLevelInfo+" does not exist!");
        }
        
        importStackIPOT_LevelInfo(pathLevelInfo);
        m_cStackIPOT.checkTrackindexes();       
        rebuildStackIPOs();
        m_cStackIPOT.checkTrackindexes();
        ArrayList<IPOGTLevelInfoNode> cvLevelInfos=new ArrayList();
        m_cStackIPOT.getLevelInfoNodes(cvLevelInfos,sQuantityName);
        int i,len=cvLevelInfos.size(),j,len1;
        ArrayList<IPOGaussianNodeComplex> IPOGs;
        IPOGaussianNodeComplex IPOG;
        String line;
        String newline=PrintAssist.newline;
        for(i=0;i<len;i++){
            IPOGs=cvLevelInfos.get(i).getIPOs();
            len1=IPOGs.size();
            for(j=0;j<len1;j++){
                IPOG=IPOGs.get(j);
                if(IPOG.getIPOGCode()>=0){
                    line=IPOGaussianNodeHandler.getIPOGShapeAFRRData(IPOG)+newline;
                    if(line!=null) PrintAssist.printString(Shapefm, line);
                }
                if(IPOG.getContourCode()>=0){
                    line=IPOGaussianNodeHandler.getIPOGContourARFFData(IPOG,IPOGContourParameterNode.GaussianMean);
                    if(line!=null) PrintAssist.printString(Contourfm, line+newline);
                }
                if(IPOG.getRawContourCode()>=0){
                    line=IPOGaussianNodeHandler.getIPOGContourARFFData(IPOG,IPOGContourParameterNode.GaussianMeanRaw);
                    if(line!=null) PrintAssist.printString(Contourfm, line+newline);
                }
            }
        }
        return 1;
    }
    public static double getHeightPValue(int sliceIndex, double h){      
        if(ppdHeightCutoff==null) return -1.1;
        if(sliceIndex<0) sliceIndex=0;
        if(sliceIndex>=ppdHeightCutoff.length) sliceIndex=ppdHeightCutoff.length-1;
        int index=CommonStatisticsMethods.getLowerBoundIndex(ppdHeightCutoff[sliceIndex], h, -1);
        return pdHeightCutoffPs[index];
    }
}
