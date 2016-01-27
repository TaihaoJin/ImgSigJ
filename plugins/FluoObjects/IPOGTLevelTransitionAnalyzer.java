/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * IPOGTLevelTransitionAnalyzer.java
 *
 * Created on Oct 19, 2011, 7:50:28 AM
 */

package FluoObjects;
import ImageAnalysis.SubpixelGaussianMean;
import ImageAnalysis.TwoDFunction;
import ij.ImagePlus;
import ij.WindowManager;
import javax.swing.*;
import utilities.statistics.LevelTransitionDetector_LevelComparison;
import java.util.ArrayList;
import utilities.Gui.HighlightingRoiCollectionContainer;
import utilities.Gui.HighlightingRoiCollectionNode;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.Point;
import ij.gui.PlotWindow;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.StringTokenizer;
import utilities.io.PrintAssist;
import ij.gui.PointRoi;
import java.awt.event.ActionEvent;
import utilities.CommonMethods;
import utilities.CommonGuiMethods;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.intRange;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.Gui.ScrollablePicture;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import utilities.Gui.ScrollPaneImageViewer;
import ij.gui.Roi;
import ij.gui.Line;
import javax.swing.JPanel;
import javax.swing.event.*;
import utilities.statistics.Histogram;
import utilities.Non_LinearFitting.LineEnveloper;
import utilities.Gui.TableFrame;
import utilities.statistics.RunningWindowRegressionLiner;
import utilities.Non_LinearFitting.StraightlineFitter;
import org.apache.commons.math.stat.regression.SimpleRegression;
import ij.gui.Plot;
import utilities.CustomDataTypes.DoubleRange;
import utilities.statistics.PolynomialRegression;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import utilities.statistics.LineFitter_AdaptivePolynomial;
import utilities.statistics.LineSegmentRegressionEvaluater;
import utilities.statistics.SignalTransitionDetector;
import utilities.Gui.PlotWindowPlus;
import java.awt.Window;
import utilities.Gui.*;
import utilities.statistics.*;
import javax.swing.JTextArea;
import utilities.CustomDataTypes.DoublePair;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class IPOGTLevelTransitionAnalyzer extends javax.swing.JFrame implements HighlightingRoiCollectionContainer, ActionListener, MouseListener, MouseMotionListener,TableModelListener{
    public static int nBackgroundJobID=0;
    public static final int RefitCompleteIPOGs=1,RefitIPOGs=2;
    ImagePlus implTrackPlot;
    IPOGTLevelTransitionAnalyzerForm m_cDisplayingForm;
    IPOGTrackNode m_cIPOGT;
    IPOGTrackPlotWindow m_cIPOGTPW;
    JViewport m_cTrackPlotViewport;
    public static JTextArea m_cStatusTextArea;
//    LevelTransitionDetector_LevelComparison m_cLevelDetector;
    int m_nMaxLevels,m_nMaxRisingInterval;
//    ArrayList<Integer> m_nvTransitions;
    /** Creates new form IPOGTLevelTransitionAnalyzer */
//    ArrayList<IPOGTLevelNode> m_cvLevelNodes_M;
    ArrayList<HighlightingRoiCollectionNode> m_cvHLCollections;
    HighlightingRoiCollectionNode m_cTransitionHilights;
    String m_sPlottingOption;
    ArrayList<ActionListener> m_cvActionListeners;
    int[][][] pixelsStack,pixelsCompenStack;
    IPOAnalyzerForm m_cIPOAnalyzer;

    int[][] pixelsIPO,pixelsScratch,pnScratch,pixelsTrack;
    ScrollPaneImageViewer m_cTrackViewer;
    ImagePlus implIPO;
    JTable m_cLevelInfoTable_A,m_cLevelInfoTable_M;
    JTable m_cLevelIPOGTable;
    int wIPO,hIPO;
    int nStackSize,w,h,firstSlice,lastSlice;
    JViewport m_cLevelIPOViewport,m_cTrackIPOViewport,m_cTrackInfoViewport,m_cTrackIPOViewportDetailed,m_cLevelInfoViewport_M,m_cLevelInfoViewport_A,m_cCurveEvaluationViewport,m_cStatusViewPort;
    ArrayList<IPOGaussianNode> m_cvLevelIPOGs;
    double[] m_pdStackMinima;
    
    int[] m_pnStackMinIndex;
    TableFrame m_cTableFrame;
    ArrayList <Double> m_dvX=new ArrayList(), m_dvY=new ArrayList();
    String[][] m_psModelEvaluationTableData;
    int[] m_pnRisingIntervals;
//    PiecewisePolynomialLineFitter_ProgressiveSegmenting m_cTrackFitter;
    PolynomialLineFitter m_cTrackFitter;
    String m_sImageID;
    PolynomialRegression m_cPr;
    boolean bInitializing;
    boolean[] pbSelection;
    double[] pdX,pdY;
    intRange pixelRange;
    boolean bSmoothCriteria;

    public IPOGTLevelTransitionAnalyzer(IPOAnalyzerForm cIPOAnalyzer,int[][][] pixelsStack, int[][][] pixelsCompenStack,int firstSlice, intRange pixelRange) {
        if(m_cStatusTextArea==null) m_cStatusTextArea=new JTextArea();
        h=pixelsStack[0].length;
        w=pixelsStack[0][0].length;
        this.pixelRange=pixelRange;
        m_cIPOAnalyzer=cIPOAnalyzer;
        this.pixelsStack=pixelsStack;
        this.pixelsCompenStack=pixelsCompenStack;
        this.firstSlice=firstSlice;
        lastSlice=firstSlice+pixelsStack.length-1;
        m_psModelEvaluationTableData=null;
        initComponents();
        additionalInit();
    }
    public void updatePixels(int[][][] pixelsStack, int[][][] pixelsCompenStack,int firstSlice, intRange pixelRange){
        h=pixelsStack[0].length;
        w=pixelsStack[0][0].length;
        this.pixelRange=pixelRange;
        this.pixelsStack=pixelsStack;
        this.pixelsCompenStack=pixelsCompenStack;
        this.firstSlice=firstSlice;
        lastSlice=firstSlice+pixelsStack.length-1;
        m_psModelEvaluationTableData=null;
        DisplayOnlyCB.setSelected(true);
        
        w=pixelsStack[0][0].length;
        h=pixelsStack[0].length;
        m_nMaxRisingInterval=getMaxRizingInterval((String)PlottingOptionCB.getSelectedItem());
        m_cvActionListeners.clear();
        wIPO=21;
        hIPO=21;
        int wt=2*wIPO+1,ht=(m_nMaxLevels+1)*(hIPO+1)-1;
        implIPO=CommonMethods.getBlankImage(ImagePlus.GRAY16, wt, ht);
        implIPO.show();
        implIPO.hide();
        int nStackSize=pixelsStack.length,w=pixelsStack[0][0].length,h=pixelsStack[0].length;
        pixelsScratch=new int[h][w];
        pnScratch=new int[h][w];
        pixelsIPO=new int[ht][wt];
    }
    void additionalInit(){
        bInitializing=true;
        w=pixelsStack[0][0].length;
        h=pixelsStack[0].length;
        m_cTrackPlotViewport=new JViewport();
        m_nMaxLevels=8;
        m_nMaxRisingInterval=getMaxRizingInterval((String)PlottingOptionCB.getSelectedItem());
        m_cvActionListeners=new ArrayList();
        wIPO=21;
        hIPO=21;
        int wt=2*wIPO+1,ht=(m_nMaxLevels+1)*(hIPO+1)-1;
        implIPO=CommonMethods.getBlankImage(ImagePlus.GRAY16, wt, ht);
        implIPO.show();
        implIPO.hide();
        int nStackSize=pixelsStack.length,w=pixelsStack[0][0].length,h=pixelsStack[0].length;
        pixelsScratch=new int[h][w];
        pnScratch=new int[h][w];
        pixelsIPO=new int[ht][wt];
        AveImageSP.addMouseListener(this);
        AveImageSP.addMouseMotionListener(this);
        PlotImageSP.addMouseListener(this);
        PlotImageSP.addMouseMotionListener(this);
        m_cTrackViewer=new ScrollPaneImageViewer(TrackImageSP);
        m_cDisplayingForm=null;
        m_sImageID="";
        
        
        String pst[]=IPOGTLevelInfoNode.psTrackEvaluationElements;
        CommonGuiMethods.resetItems(EvaluationElementsCB, pst);
        pst=IPOGTLevelInfoNode.psTransitionDetectionConfidence;
        CommonGuiMethods.resetItems(ConfidenceLevelCB, pst);
        pst=IPOGTLevelInfoNode.psInfoNodeStatus;
        CommonGuiMethods.resetItems(InfoNodeStatusCB, pst);
        SmoothingCB.setSelected(false);
        
        DisplayOnlyCB.setSelected(true);
        bInitializing=false;
    }
    public void setDisplayingForm(IPOGTLevelTransitionAnalyzerForm cDisplayingForm){
        m_cDisplayingForm=cDisplayingForm;
    }
    public IPOGTLevelTransitionAnalyzerForm getDisplayingForm(){
        return m_cDisplayingForm;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PlotImageSP = new javax.swing.JScrollPane();
        CreateTransitionRB = new javax.swing.JRadioButton();
        RemoveTransitionRB = new javax.swing.JRadioButton();
        ShowTransitions = new javax.swing.JRadioButton();
        SliceLB = new javax.swing.JLabel();
        CursorLB = new javax.swing.JLabel();
        Value = new javax.swing.JLabel();
        RegressionLB = new javax.swing.JLabel();
        LevelLB = new javax.swing.JLabel();
        NextTrack = new javax.swing.JButton();
        PreviousTrackBT = new javax.swing.JButton();
        IPOGTableSP = new javax.swing.JScrollPane();
        TrackImageSP = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();
        AveImageSP = new javax.swing.JScrollPane();
        CenterAtCurrentIPORB = new javax.swing.JCheckBox();
        IPOTableViewSelectionCB = new javax.swing.JComboBox();
        HighlightIPOGCenterCB = new javax.swing.JCheckBox();
        FontTF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        EvaluationElementsCB = new javax.swing.JComboBox();
        DeltaLB = new javax.swing.JLabel();
        PlottingOptionCB = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        PlottingReferenceOptionCB = new javax.swing.JComboBox();
        PlotBT = new javax.swing.JButton();
        StackMinLB = new javax.swing.JLabel();
        AutoDetectBT = new javax.swing.JButton();
        OutputValuesAsTableBT = new javax.swing.JButton();
        ShowTransitionsCB = new javax.swing.JCheckBox();
        AdjustLevelCB = new javax.swing.JCheckBox();
        OutputOptionCB = new javax.swing.JComboBox();
        ShowSegmentRB = new javax.swing.JRadioButton();
        SegmentOptionCB = new javax.swing.JComboBox();
        AddPlotRB = new javax.swing.JRadioButton();
        DisplayOptionsCB = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        ShowRegressionBT = new javax.swing.JButton();
        ClearRegressionBT = new javax.swing.JButton();
        RegressionOrderCB = new javax.swing.JComboBox();
        RegressionModesCB = new javax.swing.JComboBox();
        ShowPlotDataBT = new javax.swing.JButton();
        MarkExcessiveDeltaCB = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        PVDeltaTF = new javax.swing.JTextField();
        ShowLevelCB = new javax.swing.JCheckBox();
        AppendTableCB = new javax.swing.JCheckBox();
        detectTransitionsBT = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        TransitionPValueTF = new javax.swing.JTextField();
        FitTrackBT = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        PVChiSQTF = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        PVTiltingTF = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        PVPWDevTF = new javax.swing.JTextField();
        ZoomInBT = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        DataSelectionOptionCB = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        OutliarRatioTF = new javax.swing.JTextField();
        DataSelectionColorCB = new javax.swing.JComboBox();
        ShowDataSelectionCB = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        SelectRoiCB = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        OveridePlotParsCB = new javax.swing.JCheckBox();
        LineWidthTF = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        PlotShapeTF = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        PlotColorCB = new javax.swing.JComboBox();
        Reset = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        PSidenessTF = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        BreakStatusCB = new javax.swing.JComboBox();
        UpdateCursorRB = new javax.swing.JRadioButton();
        FreezeCursorCB = new javax.swing.JCheckBox();
        OutliarTypeCB = new javax.swing.JComboBox();
        FilterBT = new javax.swing.JButton();
        FilterOptionCB = new javax.swing.JComboBox();
        DisplayOnlyCB = new javax.swing.JCheckBox();
        ActiveCurveHandlingOptionCB = new javax.swing.JComboBox();
        FittingOptionCB = new javax.swing.JComboBox();
        ConfidenceLevelCB = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        EndLevelTF = new javax.swing.JTextField();
        InfoNodeStatusCB = new javax.swing.JComboBox();
        ClearEvaluationsBT = new javax.swing.JButton();
        CloseAllPlotsBT = new javax.swing.JButton();
        CloseTempFramesBT = new javax.swing.JButton();
        ClearMessageAreaCB = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        PVTerminalsTF = new javax.swing.JTextField();
        BatchFittingCB = new javax.swing.JCheckBox();
        AutomationLabel = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        SDChoiceCB = new javax.swing.JComboBox();
        PlotHandlerBT = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        TransitionDisplayOptionCB = new javax.swing.JComboBox();
        SmoothingCB = new javax.swing.JCheckBox();
        IPOGsFittingOptionCB = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Level Transition Analyzer");

        CreateTransitionRB.setText("create transition");
        CreateTransitionRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateTransitionRBActionPerformed(evt);
            }
        });

        RemoveTransitionRB.setText("remove transition");
        RemoveTransitionRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveTransitionRBActionPerformed(evt);
            }
        });

        ShowTransitions.setText("Show Transitions");
        ShowTransitions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowTransitionsActionPerformed(evt);
            }
        });

        SliceLB.setText("Slice");

        CursorLB.setText("Cursor");

        Value.setText("Value");

        RegressionLB.setText("Regression");

        LevelLB.setText("Level");

        NextTrack.setText("Next");
        NextTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextTrackActionPerformed(evt);
            }
        });

        PreviousTrackBT.setText("Previous");
        PreviousTrackBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreviousTrackBTActionPerformed(evt);
            }
        });

        jLabel2.setText("Track Image");

        CenterAtCurrentIPORB.setText("center at current IPO");
        CenterAtCurrentIPORB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CenterAtCurrentIPORBActionPerformed(evt);
            }
        });

        IPOTableViewSelectionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Level Info_M", "Level Info_A", "Track Info", "Track IPOGs", "Track IPOGs Detailed", "Level IPOGs", "Curve Evaluations", "Status" }));
        IPOTableViewSelectionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IPOTableViewSelectionCBActionPerformed(evt);
            }
        });

        HighlightIPOGCenterCB.setText("highlight IPOG Centers");
        HighlightIPOGCenterCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HighlightIPOGCenterCBActionPerformed(evt);
            }
        });

        FontTF.setText("2");

        jLabel3.setText("font");

        EvaluationElementsCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Not Analyzed", "Over drifting", "Heterogeneous delta", "upper transitions", "Overlapping particles", "No last transition", "Close to edges", "Too noisy" }));
        EvaluationElementsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EvaluationElementsCBActionPerformed(evt);
            }
        });

        DeltaLB.setText("Delta");

        PlottingOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Current Data", "Peak1", "Peak3", "Peak1 Raw", "Peak3 Raw", "Amp", "SignalCal", "Background", "Height", "Signal", "None", "Area", "BundleTotal", "Drift" }));
        PlottingOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlottingOptionCBActionPerformed(evt);
            }
        });

        jLabel4.setText("Reference");

        PlottingReferenceOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1000", "background", "delta", "regression" }));

        PlotBT.setText("Plot");
        PlotBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlotBTActionPerformed(evt);
            }
        });

        StackMinLB.setText("StackMin");

        AutoDetectBT.setText("Auto Detect");
        AutoDetectBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AutoDetectBTActionPerformed(evt);
            }
        });

        OutputValuesAsTableBT.setText("Output");
        OutputValuesAsTableBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OutputValuesAsTableBTActionPerformed(evt);
            }
        });

        ShowTransitionsCB.setText("show transitions");
        ShowTransitionsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowTransitionsCBActionPerformed(evt);
            }
        });

        AdjustLevelCB.setText("Adjust Level");
        AdjustLevelCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AdjustLevelCBActionPerformed(evt);
            }
        });

        OutputOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Env Lines", "SR Lines", "Linear Seg Lengths", "RWA", "Median Reflection" }));

        ShowSegmentRB.setText("Show Seg");

        SegmentOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Optimal", "Starting", "Ending", "Long", "SmoothSeg", "Smooth", "Clear" }));
        SegmentOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SegmentOptionCBActionPerformed(evt);
            }
        });

        AddPlotRB.setText("display");
        AddPlotRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddPlotRBActionPerformed(evt);
            }
        });

        DisplayOptionsCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Optimal LS", "Optimal Segs", "Merged Segs", "Starting LS", "Ending LS", "Long LS", "DeltaO", "DeltaLR", "Transitions", "Sig of Trans", "Breaks", "Level Estimation Lines" }));
        DisplayOptionsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisplayOptionsCBActionPerformed(evt);
            }
        });

        jButton1.setText("Zoom Out");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel5.setText("Order");

        jLabel6.setText("Modes");

        ShowRegressionBT.setText("Show Regression");
        ShowRegressionBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowRegressionBTActionPerformed(evt);
            }
        });

        ClearRegressionBT.setText("Clear Regression");
        ClearRegressionBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearRegressionBTActionPerformed(evt);
            }
        });

        RegressionOrderCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "-1" }));
        RegressionOrderCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RegressionOrderCBActionPerformed(evt);
            }
        });

        RegressionModesCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "central", "-1" }));

        ShowPlotDataBT.setText("Show Plot Data");
        ShowPlotDataBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowPlotDataBTActionPerformed(evt);
            }
        });

        MarkExcessiveDeltaCB.setText("Mark Outliars");
        MarkExcessiveDeltaCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MarkExcessiveDeltaCBActionPerformed(evt);
            }
        });

        jLabel7.setText("p Delta");

        PVDeltaTF.setText("0.01");

        ShowLevelCB.setText("Show Level");
        ShowLevelCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowLevelCBActionPerformed(evt);
            }
        });

        AppendTableCB.setText("Append Table");
        AppendTableCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AppendTableCBActionPerformed(evt);
            }
        });

        detectTransitionsBT.setText("Detect Transitions");
        detectTransitionsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectTransitionsBTActionPerformed(evt);
            }
        });

        jLabel8.setText("p Value");

        TransitionPValueTF.setText("0.01");

        FitTrackBT.setText("Fit Track");
        FitTrackBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FitTrackBTActionPerformed(evt);
            }
        });

        jLabel10.setText("p ChiSQ");

        PVChiSQTF.setText("0.05");

        jLabel9.setText("p Tilting");

        PVTiltingTF.setText("0.05");

        jLabel11.setText("p Point Dev");

        PVPWDevTF.setText("0.01");

        ZoomInBT.setText("Zoom In");
        ZoomInBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZoomInBTActionPerformed(evt);
            }
        });

        jLabel12.setText("Data Option");

        DataSelectionOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All Points", "Local Min", "Local Max", "Exclude All", "Processing Selection" }));
        DataSelectionOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DataSelectionOptionCBActionPerformed(evt);
            }
        });

        jLabel13.setText("Outliar Ratio");

        OutliarRatioTF.setText("0.0");
        OutliarRatioTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OutliarRatioTFActionPerformed(evt);
            }
        });

        DataSelectionColorCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Black", "Red", "Yellow", "Blue", "Green", "Magenta", "Orange", "Pink", "Cyane" }));
        DataSelectionColorCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DataSelectionColorCBActionPerformed(evt);
            }
        });

        ShowDataSelectionCB.setText("Show Selection");
        ShowDataSelectionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowDataSelectionCBActionPerformed(evt);
            }
        });

        jLabel14.setText("Select Roi");

        SelectRoiCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Include", "Exclude", "Current Roi Only", "Exclude Outside" }));
        SelectRoiCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectRoiCBActionPerformed(evt);
            }
        });

        jLabel15.setText("Line Width");

        OveridePlotParsCB.setText("Overide Plot Pars");

        LineWidthTF.setText("1");

        jLabel16.setText("Shape");

        PlotShapeTF.setText("2");

        jLabel17.setText("Color");

        PlotColorCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Black", "Red", "Yellow", "Blue", "Green" }));

        Reset.setText("Reset");
        Reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResetActionPerformed(evt);
            }
        });

        jLabel18.setText("p Sideness");

        PSidenessTF.setText("0.05");

        jLabel19.setText("Confirm Breaks");

        BreakStatusCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Break", "Merge", "Signal Jump", "Ambiguous" }));
        BreakStatusCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BreakStatusCBActionPerformed(evt);
            }
        });

        UpdateCursorRB.setText("Cursor");

        FreezeCursorCB.setText("Freeze");
        FreezeCursorCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FreezeCursorCBActionPerformed(evt);
            }
        });

        OutliarTypeCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Excessive Delta", "Spike", "Spike1", "Spike2", "Spike3", "Spike4" }));

        FilterBT.setText("Filter");
        FilterBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterBTActionPerformed(evt);
            }
        });

        FilterOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rolling Ball", "Spike Detection", "Edge Protection", "Rolling Ball WeightedCenter", "Median RW", "Mean RW", "Median FRDelta", "Ranking Based Edge Protection" }));
        FilterOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterOptionCBActionPerformed(evt);
            }
        });

        DisplayOnlyCB.setText("Display Only");
        DisplayOnlyCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisplayOnlyCBActionPerformed(evt);
            }
        });

        ActiveCurveHandlingOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "remove" }));
        ActiveCurveHandlingOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ActiveCurveHandlingOptionCBActionPerformed(evt);
            }
        });

        FittingOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Filted Amp", "Progressive Segmentation", "Adptive Polynomial" }));

        ConfidenceLevelCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Very Confident", "Confident", "Acceptable" }));
        ConfidenceLevelCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConfidenceLevelCBActionPerformed(evt);
            }
        });

        jLabel20.setText("End Level");

        EndLevelTF.setText("1");
        EndLevelTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EndLevelTFActionPerformed(evt);
            }
        });

        InfoNodeStatusCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Not Analyzed", "Confirm Auto", "Excluded", "Verified", " " }));
        InfoNodeStatusCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InfoNodeStatusCBActionPerformed(evt);
            }
        });

        ClearEvaluationsBT.setText("Clear Evaluations");
        ClearEvaluationsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearEvaluationsBTActionPerformed(evt);
            }
        });

        CloseAllPlotsBT.setText("Close all Plots");
        CloseAllPlotsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseAllPlotsBTActionPerformed(evt);
            }
        });

        CloseTempFramesBT.setText("close temp frames");
        CloseTempFramesBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseTempFramesBTActionPerformed(evt);
            }
        });

        ClearMessageAreaCB.setText("Clear Message Area");
        ClearMessageAreaCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearMessageAreaCBActionPerformed(evt);
            }
        });

        jLabel1.setText("p Terminals");

        PVTerminalsTF.setText("0.05");
        PVTerminalsTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PVTerminalsTFActionPerformed(evt);
            }
        });

        BatchFittingCB.setText("Batch");
        BatchFittingCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BatchFittingCBActionPerformed(evt);
            }
        });

        AutomationLabel.setText("Automatic");

        jLabel21.setText("SD");

        SDChoiceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Signal", "Active Curve", "Local", "Input Number" }));
        SDChoiceCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SDChoiceCBActionPerformed(evt);
            }
        });

        PlotHandlerBT.setText("Plot Handler");
        PlotHandlerBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlotHandlerBTActionPerformed(evt);
            }
        });

        jButton2.setText("Text Area");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        TransitionDisplayOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automated Detection", "Manual Detection" }));
        TransitionDisplayOptionCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TransitionDisplayOptionCBActionPerformed(evt);
            }
        });

        SmoothingCB.setText("Smoothing");
        SmoothingCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SmoothingCBActionPerformed(evt);
            }
        });

        IPOGsFittingOptionCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Fit IPOGs", "Refit IPOGs", "No Fitting" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ZoomInBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(FreezeCursorCB)
                                    .addComponent(Reset)))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(AddPlotRB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(DisplayOptionsCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(ShowSegmentRB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(SegmentOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(ShowPlotDataBT)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(8, 8, 8)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(OveridePlotParsCB)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel15)
                                                    .addComponent(jLabel16)
                                                    .addComponent(jLabel17))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(PlotColorCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(PlotShapeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(LineWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(TransitionPValueTF, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(detectTransitionsBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(FittingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(FitTrackBT)
                                        .addGap(18, 18, 18)
                                        .addComponent(BatchFittingCB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(BreakStatusCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(OutputValuesAsTableBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(OutputOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(ClearMessageAreaCB)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(EvaluationElementsCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(ClearEvaluationsBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(AutomationLabel)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ConfidenceLevelCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(PlotHandlerBT)
                                        .addComponent(InfoNodeStatusCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jButton2)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(PreviousTrackBT)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(AutoDetectBT))
                                            .addComponent(CreateTransitionRB)
                                            .addComponent(RemoveTransitionRB)
                                            .addComponent(ShowTransitions)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(Value)
                                                    .addComponent(SliceLB)
                                                    .addComponent(DeltaLB)
                                                    .addComponent(CursorLB)
                                                    .addComponent(RegressionLB)
                                                    .addComponent(StackMinLB)
                                                    .addComponent(LevelLB)))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(AdjustLevelCB)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(ShowLevelCB)))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(CloseTempFramesBT)
                                            .addComponent(CloseAllPlotsBT)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(NextTrack)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ShowTransitionsCB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(TransitionDisplayOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(IPOGsFittingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(55, 55, 55)
                                        .addComponent(ActiveCurveHandlingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(38, 38, 38)
                                        .addComponent(SmoothingCB)))
                                .addGap(0, 31, Short.MAX_VALUE)))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(FilterBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(FilterOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(28, 28, 28)
                                        .addComponent(ShowRegressionBT)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(ShowDataSelectionCB)
                                                .addGap(18, 18, 18)
                                                .addComponent(DataSelectionColorCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(RegressionOrderCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(RegressionModesCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(ClearRegressionBT)
                                                .addGap(22, 22, 22)
                                                .addComponent(MarkExcessiveDeltaCB)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(OutliarTypeCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(22, 22, 22)
                                        .addComponent(DisplayOnlyCB)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(DataSelectionOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(OutliarRatioTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PVPWDevTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(SelectRoiCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(TrackImageSP)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(IPOGTableSP)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(AppendTableCB)
                                        .addGap(34, 34, 34)
                                        .addComponent(jLabel7)
                                        .addGap(12, 12, 12)
                                        .addComponent(PVDeltaTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PVChiSQTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(PVTiltingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PVTerminalsTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel18)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PSidenessTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel21)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(SDChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(IPOTableViewSelectionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel20)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(EndLevelTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(PlotImageSP, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(AveImageSP, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(CenterAtCurrentIPORB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(HighlightIPOGCenterCB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(FontTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(UpdateCursorRB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PlotBT)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PlottingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(29, 29, 29)
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PlottingReferenceOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(ZoomInBT)
                            .addComponent(Reset))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CreateTransitionRB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(RemoveTransitionRB)
                            .addComponent(PlotHandlerBT))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ShowTransitions)
                            .addComponent(jButton2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SliceLB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Value)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DeltaLB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CursorLB)
                            .addComponent(FreezeCursorCB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RegressionLB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(StackMinLB)
                        .addGap(7, 7, 7)
                        .addComponent(LevelLB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(InfoNodeStatusCB, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ClearEvaluationsBT)
                            .addComponent(AutomationLabel)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(PlotImageSP, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(AveImageSP, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CenterAtCurrentIPORB)
                    .addComponent(HighlightIPOGCenterCB)
                    .addComponent(jLabel3)
                    .addComponent(FontTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PlottingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PlotBT)
                    .addComponent(PlottingReferenceOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2)
                    .addComponent(UpdateCursorRB)
                    .addComponent(ConfidenceLevelCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(EvaluationElementsCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(TrackImageSP, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6)
                                    .addComponent(RegressionOrderCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(RegressionModesCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ShowRegressionBT)
                                    .addComponent(ClearRegressionBT)
                                    .addComponent(OutliarTypeCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(MarkExcessiveDeltaCB))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(AppendTableCB)
                                    .addComponent(DataSelectionColorCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ShowDataSelectionCB)
                                    .addComponent(jLabel18)
                                    .addComponent(PVTiltingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9)
                                    .addComponent(PVChiSQTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel7)
                                    .addComponent(PVDeltaTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1)
                                    .addComponent(PVTerminalsTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(PSidenessTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel21)
                                    .addComponent(SDChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(FilterBT)
                                .addComponent(FilterOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel14)
                                .addComponent(SelectRoiCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(OutliarRatioTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(PVPWDevTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel11))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(DisplayOnlyCB)
                                .addComponent(jLabel12)
                                .addComponent(DataSelectionOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel13)
                                .addComponent(IPOTableViewSelectionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel20)
                                .addComponent(EndLevelTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(IPOGTableSP, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(NextTrack)
                            .addComponent(ShowTransitionsCB)
                            .addComponent(TransitionDisplayOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(PreviousTrackBT)
                            .addComponent(AutoDetectBT)
                            .addComponent(CloseAllPlotsBT))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AdjustLevelCB)
                            .addComponent(ShowLevelCB)
                            .addComponent(CloseTempFramesBT))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ActiveCurveHandlingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SmoothingCB)
                            .addComponent(IPOGsFittingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(FitTrackBT)
                            .addComponent(jLabel19)
                            .addComponent(BreakStatusCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BatchFittingCB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(OutputValuesAsTableBT)
                            .addComponent(OutputOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ClearMessageAreaCB))
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(detectTransitionsBT)
                            .addComponent(FittingOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(TransitionPValueTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ShowSegmentRB)
                            .addComponent(SegmentOptionCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AddPlotRB)
                            .addComponent(DisplayOptionsCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(ShowPlotDataBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(OveridePlotParsCB)
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LineWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(PlotShapeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(PlotColorCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(40, 40, 40))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ShowTransitionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowTransitionsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ShowTransitionsActionPerformed

    private void CreateTransitionRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateTransitionRBActionPerformed
        // TODO add your handling code here:
        if(CreateTransitionRB.isSelected())RemoveTransitionRB.setSelected(false);
    }//GEN-LAST:event_CreateTransitionRBActionPerformed

    private void RemoveTransitionRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveTransitionRBActionPerformed
        // TODO add your handling code here:
        if(RemoveTransitionRB.isSelected()) CreateTransitionRB.setSelected(false);
    }//GEN-LAST:event_RemoveTransitionRBActionPerformed

    private void NextTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextTrackActionPerformed
        // TODO add your handling code here:
        fireActionEvent(new ActionEvent(this,0,"Next Track"));
    }//GEN-LAST:event_NextTrackActionPerformed

    private void PreviousTrackBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PreviousTrackBTActionPerformed
        // TODO add your handling code here:
        fireActionEvent(new ActionEvent(this,0,"Previous Track"));
    }//GEN-LAST:event_PreviousTrackBTActionPerformed

    private void IPOTableViewSelectionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IPOTableViewSelectionCBActionPerformed
        // TODO add your handling code here:
        updateIPOGTableView((String)IPOTableViewSelectionCB.getSelectedItem());
    }//GEN-LAST:event_IPOTableViewSelectionCBActionPerformed

    private void EvaluationElementsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EvaluationElementsCBActionPerformed
        // TODO add your handling code here:
        if(m_cIPOGTPW!=null&&!bInitializing){
            if(m_cIPOGTPW.IPOGT!=null){
                if(m_cIPOGTPW.IPOGT.m_cLevelInfo==null)m_cIPOGTPW.IPOGT.setLevelInfo(new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_sPlottingOption,pbSelection,m_nMaxRisingInterval)); 
                if(m_cIPOGTPW.IPOGT.m_cLevelInfo!=null){
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.addEvaluation_Manual((String)EvaluationElementsCB.getSelectedItem());
                    if(m_cTrackFitter!=null) {
                        if(m_cTrackFitter instanceof PiecewisePolynomialLineFitter_ProgressiveSegmenting){
                            PiecewisePolynomialLineFitter_ProgressiveSegmenting cTrackFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) m_cTrackFitter;
                            cTrackFitter.setVerification((String)EvaluationElementsCB.getSelectedItem());
                        }
                    }
                    updateIPOGTableView("Curve Evaluations");
                }
                if(m_cIPOGT.m_cLevelInfo!=null) displayBreakInfo();
            }
        }
    }//GEN-LAST:event_EvaluationElementsCBActionPerformed

    private void PlotBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlotBTActionPerformed
        // TODO add your handling code here:
        plotTrack();
    }//GEN-LAST:event_PlotBTActionPerformed

    private void AutoDetectBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AutoDetectBTActionPerformed
        // TODO add your handling code here:
        IPOGTrackPlotWindow IPOGTPW=new IPOGTrackPlotWindow(m_cIPOGT,(String)PlottingOptionCB.getSelectedItem());
        m_cIPOGT.m_cLevelInfo=null;
        updateIPOGTPW(IPOGTPW,(String)PlottingOptionCB.getSelectedItem());
    }//GEN-LAST:event_AutoDetectBTActionPerformed

    private void OutputValuesAsTableBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OutputValuesAsTableBTActionPerformed
        // TODO add your handling code here:
//        adjustLevel();
//        exportEnvelopes();
//        exportSRLines();
        outputValuesAsTable();
    }//GEN-LAST:event_OutputValuesAsTableBTActionPerformed

    private void SegmentOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SegmentOptionCBActionPerformed
        // TODO add your handling code here:
        if(((String)SegmentOptionCB.getSelectedItem()).contentEquals("Clear")) showIPOGTFitting(new Point(0,0));
        if(((String)SegmentOptionCB.getSelectedItem()).contentEquals("Smooth")) showSmoothRegions();
    }//GEN-LAST:event_SegmentOptionCBActionPerformed

    private void DisplayOptionsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisplayOptionsCBActionPerformed
        // TODO add your handling code here:
        String option=(String) DisplayOptionsCB.getSelectedItem();
        if(option.contentEquals("Breaks"))
                displayBreakInfo();
        else if(option.contentEquals("Level Estimation Lines"))
            displayLevelEstimationLines();
        else {
            if(AddPlotRB.isSelected()) displayPlot(option);
            PolynomialLineFitter cTrackFitter0=m_cIPOGTPW.getTrackFitter();
            if(cTrackFitter0 instanceof PiecewisePolynomialLineFitter_ProgressiveSegmenting){
                PiecewisePolynomialLineFitter_ProgressiveSegmenting cTrackFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) cTrackFitter0;
                highlightSelection();
                if(cTrackFitter!=null){
                    String[][] psData=cTrackFitter.getSplittedSegmentsAsStringArray();
                    JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 0, psData.length-1);
                    IPOGTableSP.setViewport(jvp);
                }
            }
        }
    }//GEN-LAST:event_DisplayOptionsCBActionPerformed
    void displayLevelEstimationLines(){
        PolynomialLineFitter cTrackFitter0=m_cIPOGTPW.getTrackFitter();
        if(cTrackFitter0!=null){
            if(cTrackFitter0 instanceof PiecewisePolynomialLineFitter_ProgressiveSegmenting){
                PiecewisePolynomialLineFitter_ProgressiveSegmenting cTrackFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) cTrackFitter0;
                ArrayList<double[]> lines=cTrackFitter.getLevelEstimationLines();
                int i,len=lines.size()/4,lw=2,shape=PlotWindow.LINE,line=0;
                Color r=Color.RED,b=Color.BLUE;
                String title="Seg";
                for(i=0;i<len;i++){
                    m_cIPOGTPW.pw.addPlot(title+i+"L", lines.get(line), lines.get(line+1), lw, shape,b,false);
                    line+=2;
                    m_cIPOGTPW.pw.addPlot(title+i+"R", lines.get(line), lines.get(line+1), lw, shape,r,false);
                    line+=2;
                }
                m_cIPOGTPW.pw.refreshPlot();
            }
        }
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        zoomOut();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void ShowRegressionBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowRegressionBTActionPerformed
        showRegression();
        highlightSelection();
    }//GEN-LAST:event_ShowRegressionBTActionPerformed

    private void ClearRegressionBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearRegressionBTActionPerformed
        // TODO add your handling code here:
        clearRegressions();
    }//GEN-LAST:event_ClearRegressionBTActionPerformed

    private void RegressionOrderCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegressionOrderCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RegressionOrderCBActionPerformed

    private void ShowPlotDataBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowPlotDataBTActionPerformed
        showPlotData();
    }//GEN-LAST:event_ShowPlotDataBTActionPerformed
    void showPlotData(){
        ImagePlus impl=CommonMethods.getCurrentImage();
        Window wd=impl.getWindow();
        if(wd instanceof PlotWindowPlus){
            PlotWindowPlus wdt=(PlotWindowPlus) wd;
            wdt.showPlotData();
        }else{
            m_cIPOGTPW.showPlotData();
        }
    }
    private void MarkExcessiveDeltaCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MarkExcessiveDeltaCBActionPerformed
        String sType=(String)OutliarTypeCB.getSelectedItem();
        double pValue=Double.parseDouble(PVDeltaTF.getText());
        if(MarkExcessiveDeltaCB.isSelected()){
            if(sType.contentEquals("Excessive Delta")){
                m_cIPOGTPW.markExcessiveDeltaPoints(pValue);
                m_cIPOGTPW.highlightExcessiveDelta(CommonGuiMethods.getColorSelection((String)DataSelectionColorCB.getSelectedItem()));
            }else if(sType.startsWith("Spike")){
                int multiplicity=2;
                if(sType.contains("1")) multiplicity=1;
                if(sType.contains("2")) multiplicity=2;
                m_cIPOGTPW.markSpikes(pValue,multiplicity);
                m_cIPOGTPW.highlightSpikes(CommonGuiMethods.getColorSelection((String)DataSelectionColorCB.getSelectedItem()));
            }
        } else{
            m_cIPOGTPW.includeExcessiveDeltaPoints();
            m_cIPOGTPW.removeExcessiveDeltaHighlights();
            m_cIPOGTPW.removeSpikeHighlights();
        }
    }//GEN-LAST:event_MarkExcessiveDeltaCBActionPerformed

    private void ShowLevelCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowLevelCBActionPerformed
        // TODO add your handling code here:
        if(ShowLevelCB.isSelected()) 
            updateViews();
        else
        {
//            m_cIPOGTPW.pw.removePlotGroup("FittingLine");
//            m_cIPOGTPW.pw.removePlotGroup("Transition");
//            m_cIPOGTPW.pw.removePlotGroup("Optimal");
//            m_cIPOGTPW.pw.removePlotGroup("Merge");
              m_cIPOGTPW.pw.removePlotGroup("Seg");
//            m_cIPOGTPW.pw.removePlotGroup("SmoothRegion");
//            m_cIPOGTPW.pw.removePlotGroup("SmoothSeg");
        }
    }//GEN-LAST:event_ShowLevelCBActionPerformed

    private void AppendTableCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AppendTableCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AppendTableCBActionPerformed

    private void AddPlotRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddPlotRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AddPlotRBActionPerformed

    private void detectTransitionsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detectTransitionsBTActionPerformed
        // TODO add your handling code here:
        int order=Integer.parseInt((String)RegressionOrderCB.getSelectedItem());
//        m_cIPOGTPW.pw.removePlotGroup("Optimal");
        double dPDelta=Double.parseDouble(PVDeltaTF.getText()),dPTransition=Double.parseDouble(TransitionPValueTF.getText());
        m_cIPOGTPW.detectTrackSignalTransitions((String)FittingOptionCB.getSelectedItem(),(String)DataSelectionOptionCB.getSelectedItem(),order, dPDelta, dPTransition,Double.parseDouble(OutliarRatioTF.getText()));
    }//GEN-LAST:event_detectTransitionsBTActionPerformed

    private void FitTrackBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FitTrackBTActionPerformed
        // TODO add your handling code here:
        String fittingOption=(String) FittingOptionCB.getSelectedItem();
        clearMessageArea();
        fitTrack(fittingOption);
    }//GEN-LAST:event_FitTrackBTActionPerformed
    static public LineFeatureExtracter2 detectTransitions(IPOGTLevelInfoNode aInfoNode){
        double[] pdX=aInfoNode.pdX,pdY=aInfoNode.pdY;
        
        int nRisingInterval=aInfoNode.nMaxRisingInterval,nOrder=1;
        double dPChiSQ=aInfoNode.getdPChiSQ(),dPPWDev=aInfoNode.getdPPWDev(),dPTilting=aInfoNode.getdPTilting(),dPSideness=aInfoNode.getdPSideness(),dPTerminals=aInfoNode.getdPTerminals();
        
        if(aInfoNode==null) return null;
        LineFeatureExtracter2 cFeatureExtracter=new LineFeatureExtracter2("",pdX,pdY,nRisingInterval,dPChiSQ,dPPWDev,dPTilting,dPSideness,dPTerminals,nOrder);
        boolean[] pbSelection=cFeatureExtracter.getSelection();
        aInfoNode.setSelction(pbSelection);
        ArrayList<double[]> pdvFiltedY=null;
        ArrayList <LineSegmentNode> segs=null;
        if(cFeatureExtracter.validClusteringExtention())
             segs=cFeatureExtracter.getSignalLevelSegmentsE();
        else
             segs=cFeatureExtracter.getSignalLevelSegments();
                
        boolean consistency=true;
        aInfoNode.markSignificantTransitions(cFeatureExtracter.getSignificantTransitions());
        if(cFeatureExtracter.validClusteringExtention()&&!CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPoints(), cFeatureExtracter.getDetectedTransitionPointsE())) consistency=false;
        aInfoNode.setConsistency(consistency);
        aInfoNode.setSignal(cFeatureExtracter.getSignal());
        if(aInfoNode.pdSignal==null) {
            int i=0;
        }
        aInfoNode.setEnvTrail(cFeatureExtracter.getEnvTrail());
        aInfoNode.setSigDiff(cFeatureExtracter.getSigDiff());
        if(CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPoints(), cFeatureExtracter.getHighClusterPoints()))
            aInfoNode.allConfirmed(true);
        else
            aInfoNode.allConfirmed(false);
                
        if(CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPointsE(), cFeatureExtracter.getHighClusterPointsE()))
            aInfoNode.allConfirmedE(true);
        else
            aInfoNode.allConfirmedE(false);
        aInfoNode.setMeanDiffMeanSems(cFeatureExtracter.getMeanDiffMeanSems());
        aInfoNode.buildLevelInfoNodes(segs);
//        if(aInfoNode.StoredIPOGs()) 
//            aInfoNode.restoreIPOGs();
//        aInfoNode.evaluateSignal();
        return cFeatureExtracter;
    }
    int fitTrack_BallRolling(){
        double dPChiSQ=Double.parseDouble(PVChiSQTF.getText());
        double dPTilting=Double.parseDouble(PVTiltingTF.getText());
        double dPSideness=Double.parseDouble(PSidenessTF.getText());
        double dPPWDev=Double.parseDouble(PVPWDevTF.getText());
        double dOutliarRatio=Double.parseDouble(OutliarRatioTF.getText());
        double dPTerminals=Double.parseDouble(PVTerminalsTF.getText());
        
        String sOrder=(String) RegressionOrderCB.getSelectedItem();
        int nOrder=Integer.parseInt(sOrder);
        LineFeatureExtracter2 cFeatureExtracter=null;
        ArrayList <LineSegmentNode> segs=null;
            
        m_cIPOGTPW.IPOGT.activateLevelInfo(m_cIPOGTPW.plotOption);
        IPOGTLevelInfoNode cLevelInfoNode=m_cIPOGTPW.IPOGT.m_cLevelInfo;
        if(cLevelInfoNode==null){
            cLevelInfoNode=new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_cIPOGTPW.plotOption,null,m_nMaxRisingInterval);
            m_cIPOGTPW.IPOGT.setLevelInfo(cLevelInfoNode);         
        }else if(cLevelInfoNode.versionNumber!=IPOGTLevelInfoNode.getCurrentVersionNumber()){
            IPOGTLevelInfoNode aNode=cLevelInfoNode;
            cLevelInfoNode=new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_cIPOGTPW.plotOption,null,m_nMaxRisingInterval);
            cLevelInfoNode.copyManualLevelInfo(aNode);
            m_cIPOGTPW.IPOGT.setLevelInfo(cLevelInfoNode);         
        }
           
        cLevelInfoNode.pdX=pdX;
        cLevelInfoNode.pdY=pdY;
        cLevelInfoNode.dPChiSQ=dPChiSQ;
        cLevelInfoNode.dPPWDev=dPPWDev;
        cLevelInfoNode.dPSideness=dPSideness;
        cLevelInfoNode.dPTerminals=dPTerminals;
        cLevelInfoNode.dPTilting=dPTilting;
        cLevelInfoNode.dOutliarRatio=dOutliarRatio;
        int IPOGsFittingOption=getFittingOption();
        fitTrack_BallRolling(m_cIPOGTPW.IPOGT,pixelsStack,pixelsCompenStack,pixelsScratch,pnScratch,m_cIPOGTPW.plotOption,IPOGsFittingOption);
/*        
            cFeatureExtracter=detectTransitions(cLevelInfoNode);
            
            pbSelection=cFeatureExtracter.getSelection();
            cLevelInfoNode.setSelction(pbSelection);
            m_cIPOGTPW.pw.setFeatureExtracter(cFeatureExtracter);
                
            m_cTrackFitter=cFeatureExtracter.getSignalFitter();
            m_cIPOGTPW.setTrackFiter(m_cTrackFitter);
            detectTransitionsBTActionPerformed(null);

            if(cLevelInfoNode.StoredIPOGs()) 
                cLevelInfoNode.restoreIPOGs();
            calLevelIPOs();*/
            showTransitions();            
        return 1;
    }
    public static int fitTrack_BallRolling(IPOGTrackNode IPOGT,int[][][] pixelsStack,int[][][] pixelsCompenStack,int[][]pixelsScratch,int[][] pnScratch,String sQuantityName,int IPOGRefittingOption){
        LineFeatureExtracter2 cFeatureExtracter=null;
        ArrayList <LineSegmentNode> segs=null;
            
        IPOGT.activateLevelInfo(sQuantityName);
        IPOGTLevelInfoNode cLevelInfoNode=IPOGT.m_cLevelInfo;
        if(cLevelInfoNode==null){
            cLevelInfoNode=new IPOGTLevelInfoNode(IPOGT,sQuantityName,null,getMaxRizingInterval(sQuantityName));
            IPOGT.setLevelInfo(cLevelInfoNode);         
        }else if(cLevelInfoNode.versionNumber!=IPOGTLevelInfoNode.getCurrentVersionNumber()){
            IPOGTLevelInfoNode aNode=cLevelInfoNode;
            cLevelInfoNode=new IPOGTLevelInfoNode(IPOGT,sQuantityName,null,getMaxRizingInterval(sQuantityName));
            cLevelInfoNode.copyManualLevelInfo(aNode);
            IPOGT.setLevelInfo(cLevelInfoNode);         
        }
        cFeatureExtracter=detectTransitions(cLevelInfoNode);
            
        boolean[] pbSelection=cFeatureExtracter.getSelection();
        cLevelInfoNode.setSelction(pbSelection);

        if(!cLevelInfoNode.StoredIPOGs()) IPOGRefittingOption=IPOGTLevelTransitionAnalyzer.RefitCompleteIPOGs;
        calLevelIPOs(IPOGT,pixelsStack,pixelsCompenStack,pixelsScratch,pnScratch,IPOGRefittingOption);       
        cLevelInfoNode.restoreIPOGs();
        return 1;
    }
    int fitTrack(String fittingOption){
            double dPChiSQ=Double.parseDouble(PVChiSQTF.getText());
            double dPTilting=Double.parseDouble(PVTiltingTF.getText());
            double dPSideness=Double.parseDouble(PSidenessTF.getText());
            double dPPWDev=Double.parseDouble(PVPWDevTF.getText());
            double dOutliarRatio=Double.parseDouble(OutliarRatioTF.getText());
            double dPTerminals=Double.parseDouble(PVTerminalsTF.getText());
            String sOrder=(String) RegressionOrderCB.getSelectedItem();
            int nOrder=Integer.parseInt(sOrder);
            LineFeatureExtracter2 cFeatureExtracter=null;
        
        if(fittingOption.contentEquals("Filted Amp")){            
//            m_cTrackFitter=m_cIPOGTPW.fitTrackSignal((String)DataSelectionOptionCB.getSelectedItem(),Integer.parseInt((String)RegressionOrderCB.getSelectedItem()), Double.parseDouble(PVDeltaTF.getText()), Double.parseDouble(PVChiSQTF.getText()), Double.parseDouble(PVTiltingTF.getText()), Double.parseDouble(PSidenessTF.getText()), Double.parseDouble(PVPWDevTF.getText()), Double.parseDouble(OutliarRatioTF.getText()),false);
            ArrayList<double[]> pdvFiltedY=null;
            ArrayList <LineSegmentNode> segs=null;
            String sType=(String)OutliarTypeCB.getSelectedItem();
            String sFilterType=(String)FilterOptionCB.getSelectedItem();
            if(sFilterType.startsWith("Ranking Based")) 
                pdvFiltedY=m_cIPOGTPW.filtTrack_RankingBasedEdgeProtection(Color.red, false);
            else if(sFilterType.startsWith("Edge")){
                double pValue=Double.parseDouble(PVDeltaTF.getText());
                int nMaxMultiplicity=2;
                pdvFiltedY=m_cIPOGTPW.filtTrackData(pValue, -1*nMaxMultiplicity,Color.BLACK,false);  
            }else if(sFilterType.startsWith("Rolling Ball")){
                fitTrack_BallRolling();
                return 1;
//                m_nMaxRisingInterval=4;//11n20
/*                cFeatureExtracter=new LineFeatureExtracter2(this.m_sImageID+" Track"+m_cIPOGTPW.IPOGT.TrackIndex,m_cIPOGTPW.pdX,m_cIPOGTPW.pdY,m_nMaxRisingInterval,dPChiSQ,dPPWDev,dPTilting,dPSideness,dPTerminals,nOrder);
                pbSelection=cFeatureExtracter.getSelection();
//                SignalTransitionDetector_FeatureBased cST=new SignalTransitionDetector_FeatureBased(cFeatureExtracter);
                if(cFeatureExtracter.validClusteringExtention())
                    segs=cFeatureExtracter.getSignalLevelSegmentsE();
                else
                    segs=cFeatureExtracter.getSignalLevelSegments();
                
                m_cIPOGTPW.pw.setFeatureExtracter(cFeatureExtracter);
                
                m_cTrackFitter=cFeatureExtracter.getSignalFitter();
                m_cIPOGTPW.setTrackFiter(m_cTrackFitter);
                detectTransitionsBTActionPerformed(null);
/*                double dRx=10;
                int nRanking=0;
                
                
                ProbingBall pb=new ProbingBall(m_cIPOGTPW.getDataX(),m_cIPOGTPW.getDataY(),dRx,-1,nRanking);  
                
                pdvFiltedY=new ArrayList();
                pdvFiltedY.add(pb.getProbeTrail(ProbingBall.Downward));
                
                dRx=20;
//                pdvFiltedY=new ArrayList();
                pdvFiltedY.add(pb.getProbeTrail(ProbingBall.Downward));*/
            }else if(sFilterType.startsWith("Rolling Ball WeightedCenter")){
                double dRx=10;
                int nRanking=0;
                double[] pdTrailD,pdTrailU,pdTrailC;
                
                ProbingBall pb=new ProbingBall(m_cIPOGTPW.getDataX(),m_cIPOGTPW.getDataY(),dRx,-1,nRanking);  
                pdTrailU=pb.getProbeTrail(ProbingBall.Downward);
                pdTrailD=pb.getProbeTrail(ProbingBall.Upward);
                pdTrailC=CommonStatisticsMethods.getWeightedCenterLine(pdTrailU, pdTrailD);
                
                pdvFiltedY=new ArrayList();
                pdvFiltedY.add(pdTrailC);
                
                dRx=20;
                pb=new ProbingBall(m_cIPOGTPW.getDataX(),m_cIPOGTPW.getDataY(),dRx,-1,nRanking);  
                pdTrailU=pb.getProbeTrail(ProbingBall.Downward);
                pdTrailD=pb.getProbeTrail(ProbingBall.Upward);
                pdTrailC=CommonStatisticsMethods.getWeightedCenterLine(pdTrailU, pdTrailD);
                pdvFiltedY.add(pb.getProbeTrail(ProbingBall.Downward));
            }
            LineSegmenter_FiltedAmpBased filter;
            if(segs==null) {
                filter=new LineSegmenter_FiltedAmpBased(m_cIPOGTPW.pdX,m_cIPOGTPW.pdY,pdvFiltedY,m_cIPOGTPW.nMaxRisingInterval);
                segs=filter.getSegments();
            }

            int i,len=segs.size();
            double x0=0,y0=0;
            m_cIPOGTPW.IPOGT.activateLevelInfo(m_cIPOGTPW.plotOption);
            IPOGTLevelInfoNode cLevelInfoNode=m_cIPOGTPW.IPOGT.m_cLevelInfo;
            if(cLevelInfoNode==null){
                cLevelInfoNode=new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_cIPOGTPW.plotOption,pbSelection,m_nMaxRisingInterval);
                m_cIPOGTPW.IPOGT.setLevelInfo(cLevelInfoNode);         
            }else if(cLevelInfoNode.versionNumber!=IPOGTLevelInfoNode.getCurrentVersionNumber()){
                IPOGTLevelInfoNode aNode=cLevelInfoNode;
                cLevelInfoNode=new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_cIPOGTPW.plotOption,pbSelection,m_nMaxRisingInterval);
                cLevelInfoNode.copyManualLevelInfo(aNode);
                m_cIPOGTPW.IPOGT.setLevelInfo(cLevelInfoNode);         
            }
/*            if(sFilterType.startsWith("Rolling Ball")){
                boolean consistency=true;
                if(cFeatureExtracter.validClusteringExtention()&&!CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPoints(), cFeatureExtracter.getDetectedTransitionPointsE())) consistency=false;
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setConsistency(consistency);
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setSignal(cFeatureExtracter.getSignal());
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setEnvTrail(cFeatureExtracter.getEnvTrail());
//                cFeatureExtracter.calSigDiff();
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setSigDiff(cFeatureExtracter.getSigDiff());
                if(CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPoints(), cFeatureExtracter.getHighClusterPoints()))
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmed(true);
                else
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmed(false);
                
                if(CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPointsE(), cFeatureExtracter.getHighClusterPointsE()))
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmedE(true);
                else
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmedE(false);
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setMeanDiffMeanSems(cFeatureExtracter.getMeanDiffMeanSems());
            }    */
            cLevelInfoNode.buildLevelInfoNodes(segs);
            if(cLevelInfoNode.StoredIPOGs()) 
//                cLevelInfoNode.restoreIPOGs();
            calLevelIPOs();
            cLevelInfoNode.restoreIPOGs();
            showTransitions();
            
        }else if(fittingOption.contentEquals("Adptive Polynomial")){
            m_cTrackFitter=m_cIPOGTPW.fitTrackSignal(fittingOption,(String)DataSelectionOptionCB.getSelectedItem(),Integer.parseInt((String)RegressionOrderCB.getSelectedItem()), Double.parseDouble(PVDeltaTF.getText()), 
                    dPChiSQ, dPTilting, dPSideness, dPPWDev, dPTerminals, dOutliarRatio,true);
            detectTransitionsBTActionPerformed(null);
            displayPlot((String) DisplayOptionsCB.getSelectedItem());
        } else if(fittingOption.contentEquals("Progressive Segmentation")){
            m_cTrackFitter=m_cIPOGTPW.fitTrackSignal(fittingOption,(String)DataSelectionOptionCB.getSelectedItem(),Integer.parseInt((String)RegressionOrderCB.getSelectedItem()), Double.parseDouble(PVDeltaTF.getText()), Double.parseDouble(PVChiSQTF.getText()), Double.parseDouble(PVTiltingTF.getText()), Double.parseDouble(PSidenessTF.getText()), Double.parseDouble(PVPWDevTF.getText()), dPTerminals, Double.parseDouble(OutliarRatioTF.getText()),true);
            detectTransitionsBTActionPerformed(null);
            displayPlot((String) DisplayOptionsCB.getSelectedItem());
            PiecewisePolynomialLineFitter_ProgressiveSegmenting cTrackFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) m_cTrackFitter;
            if(cTrackFitter.getFittedSegments().get(0) instanceof LineFittingSegmentGroup){
                displayBreakInfo();
            }else{
                String[][] psData=cTrackFitter.getSignalJumpAsStringArray();
        //        String[][] psData=cTrackFitter.getSplittedSegmentsAsStringArray();
                JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 0, psData.length-1);
                IPOGTableSP.setViewport(jvp);
            }
        }

//        LineSegmentRegressionEvaluater.markExcludedPoints(m_cTrackFitter.getFittedSegments());        
        return 1;
    }
    int getFittingOption(){
        int IPOGsFittingOption=0;
        String st=(String) IPOGsFittingOptionCB.getSelectedItem();
        if(st.contentEquals("No Fitting")) IPOGsFittingOption=0;
        if(st.contentEquals("Refit IPOGs")) IPOGsFittingOption=IPOGTLevelTransitionAnalyzer.RefitCompleteIPOGs;
        if(st.contentEquals("Fit IPOGs")) IPOGsFittingOption=IPOGTLevelTransitionAnalyzer.RefitIPOGs;
        return IPOGsFittingOption;
    }
    int fitTrack0(String fittingOption){
            double dPChiSQ=Double.parseDouble(PVChiSQTF.getText());
            double dPTilting=Double.parseDouble(PVTiltingTF.getText());
            double dPSideness=Double.parseDouble(PSidenessTF.getText());
            double dPPWDev=Double.parseDouble(PVPWDevTF.getText());
            double dOutliarRatio=Double.parseDouble(OutliarRatioTF.getText());
            double dPTerminals=Double.parseDouble(PVTerminalsTF.getText());
            String sOrder=(String) RegressionOrderCB.getSelectedItem();
            int nOrder=Integer.parseInt(sOrder);
            LineFeatureExtracter2 cFeatureExtracter=null;
        
        if(fittingOption.contentEquals("Filted Amp")){            
//            m_cTrackFitter=m_cIPOGTPW.fitTrackSignal((String)DataSelectionOptionCB.getSelectedItem(),Integer.parseInt((String)RegressionOrderCB.getSelectedItem()), Double.parseDouble(PVDeltaTF.getText()), Double.parseDouble(PVChiSQTF.getText()), Double.parseDouble(PVTiltingTF.getText()), Double.parseDouble(PSidenessTF.getText()), Double.parseDouble(PVPWDevTF.getText()), Double.parseDouble(OutliarRatioTF.getText()),false);
            ArrayList<double[]> pdvFiltedY=null;
            ArrayList <LineSegmentNode> segs=null;
            String sType=(String)OutliarTypeCB.getSelectedItem();
            String sFilterType=(String)FilterOptionCB.getSelectedItem();
            if(sFilterType.startsWith("Ranking Based")) 
                pdvFiltedY=m_cIPOGTPW.filtTrack_RankingBasedEdgeProtection(Color.red, false);
            else if(sFilterType.startsWith("Edge")){
                double pValue=Double.parseDouble(PVDeltaTF.getText());
                int nMaxMultiplicity=2;
                pdvFiltedY=m_cIPOGTPW.filtTrackData(pValue, -1*nMaxMultiplicity,Color.BLACK,false);  
            }else if(sFilterType.startsWith("Rolling Ball")){
//                m_nMaxRisingInterval=4;//11n20
                cFeatureExtracter=new LineFeatureExtracter2(this.m_sImageID+" Track"+m_cIPOGTPW.IPOGT.TrackIndex,m_cIPOGTPW.pdX,m_cIPOGTPW.pdY,m_nMaxRisingInterval,dPChiSQ,dPPWDev,dPTilting,dPSideness,dPTerminals,nOrder);
                pbSelection=cFeatureExtracter.getSelection();
//                SignalTransitionDetector_FeatureBased cST=new SignalTransitionDetector_FeatureBased(cFeatureExtracter);
                if(cFeatureExtracter.validClusteringExtention())
                    segs=cFeatureExtracter.getSignalLevelSegmentsE();
                else
                    segs=cFeatureExtracter.getSignalLevelSegments();
                
                m_cIPOGTPW.pw.setFeatureExtracter(cFeatureExtracter);
                
                m_cTrackFitter=cFeatureExtracter.getSignalFitter();
                m_cIPOGTPW.setTrackFiter(m_cTrackFitter);
                detectTransitionsBTActionPerformed(null);
/*                double dRx=10;
                int nRanking=0;
                
                
                ProbingBall pb=new ProbingBall(m_cIPOGTPW.getDataX(),m_cIPOGTPW.getDataY(),dRx,-1,nRanking);  
                
                pdvFiltedY=new ArrayList();
                pdvFiltedY.add(pb.getProbeTrail(ProbingBall.Downward));
                
                dRx=20;
//                pdvFiltedY=new ArrayList();
                pdvFiltedY.add(pb.getProbeTrail(ProbingBall.Downward));*/
            }else if(sFilterType.startsWith("Rolling Ball WeightedCenter")){
                double dRx=10;
                int nRanking=0;
                double[] pdTrailD,pdTrailU,pdTrailC;
                
                ProbingBall pb=new ProbingBall(m_cIPOGTPW.getDataX(),m_cIPOGTPW.getDataY(),dRx,-1,nRanking);  
                pdTrailU=pb.getProbeTrail(ProbingBall.Downward);
                pdTrailD=pb.getProbeTrail(ProbingBall.Upward);
                pdTrailC=CommonStatisticsMethods.getWeightedCenterLine(pdTrailU, pdTrailD);
                
                pdvFiltedY=new ArrayList();
                pdvFiltedY.add(pdTrailC);
                
                dRx=20;
                pb=new ProbingBall(m_cIPOGTPW.getDataX(),m_cIPOGTPW.getDataY(),dRx,-1,nRanking);  
                pdTrailU=pb.getProbeTrail(ProbingBall.Downward);
                pdTrailD=pb.getProbeTrail(ProbingBall.Upward);
                pdTrailC=CommonStatisticsMethods.getWeightedCenterLine(pdTrailU, pdTrailD);
                pdvFiltedY.add(pb.getProbeTrail(ProbingBall.Downward));
            }
            LineSegmenter_FiltedAmpBased filter;
            if(segs==null) {
                filter=new LineSegmenter_FiltedAmpBased(m_cIPOGTPW.pdX,m_cIPOGTPW.pdY,pdvFiltedY,m_cIPOGTPW.nMaxRisingInterval);
                segs=filter.getSegments();
            }

            int i,len=segs.size();
            double x0=0,y0=0;
            m_cIPOGTPW.IPOGT.activateLevelInfo(m_cIPOGTPW.plotOption);
            IPOGTLevelInfoNode cLevelInfoNode=m_cIPOGTPW.IPOGT.m_cLevelInfo;
            if(cLevelInfoNode==null){
                cLevelInfoNode=new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_cIPOGTPW.plotOption,pbSelection,m_nMaxRisingInterval);
                m_cIPOGTPW.IPOGT.setLevelInfo(cLevelInfoNode);         
            }else if(cLevelInfoNode.versionNumber!=IPOGTLevelInfoNode.getCurrentVersionNumber()){
                IPOGTLevelInfoNode aNode=cLevelInfoNode;
                cLevelInfoNode=new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_cIPOGTPW.plotOption,pbSelection,m_nMaxRisingInterval);
                cLevelInfoNode.copyManualLevelInfo(aNode);
                m_cIPOGTPW.IPOGT.setLevelInfo(cLevelInfoNode);         
            }
            if(sFilterType.startsWith("Rolling Ball")){
                boolean consistency=true;
                if(cFeatureExtracter.validClusteringExtention()&&!CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPoints(), cFeatureExtracter.getDetectedTransitionPointsE())) consistency=false;
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setConsistency(consistency);
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setSignal(cFeatureExtracter.getSignal());
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setEnvTrail(cFeatureExtracter.getEnvTrail());
//                cFeatureExtracter.calSigDiff();
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setSigDiff(cFeatureExtracter.getSigDiff());
                if(CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPoints(), cFeatureExtracter.getHighClusterPoints()))
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmed(true);
                else
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmed(false);
                
                if(CommonStatisticsMethods.equalContents(cFeatureExtracter.getDetectedTransitionPointsE(), cFeatureExtracter.getHighClusterPointsE()))
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmedE(true);
                else
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.allConfirmedE(false);
                m_cIPOGTPW.IPOGT.m_cLevelInfo.setMeanDiffMeanSems(cFeatureExtracter.getMeanDiffMeanSems());
            }    
            cLevelInfoNode.buildLevelInfoNodes(segs);
            if(cLevelInfoNode.StoredIPOGs()) 
//                cLevelInfoNode.restoreIPOGs();
            calLevelIPOs();
            cLevelInfoNode.restoreIPOGs();
            showTransitions();
            
        }else if(fittingOption.contentEquals("Adptive Polynomial")){
            m_cTrackFitter=m_cIPOGTPW.fitTrackSignal(fittingOption,(String)DataSelectionOptionCB.getSelectedItem(),Integer.parseInt((String)RegressionOrderCB.getSelectedItem()), Double.parseDouble(PVDeltaTF.getText()), 
                    dPChiSQ, dPTilting, dPSideness, dPPWDev, dPTerminals, dOutliarRatio,true);
            detectTransitionsBTActionPerformed(null);
            displayPlot((String) DisplayOptionsCB.getSelectedItem());
        } else if(fittingOption.contentEquals("Progressive Segmentation")){
            m_cTrackFitter=m_cIPOGTPW.fitTrackSignal(fittingOption,(String)DataSelectionOptionCB.getSelectedItem(),Integer.parseInt((String)RegressionOrderCB.getSelectedItem()), Double.parseDouble(PVDeltaTF.getText()), Double.parseDouble(PVChiSQTF.getText()), Double.parseDouble(PVTiltingTF.getText()), Double.parseDouble(PSidenessTF.getText()), Double.parseDouble(PVPWDevTF.getText()), dPTerminals, Double.parseDouble(OutliarRatioTF.getText()),true);
            detectTransitionsBTActionPerformed(null);
            displayPlot((String) DisplayOptionsCB.getSelectedItem());
            PiecewisePolynomialLineFitter_ProgressiveSegmenting cTrackFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) m_cTrackFitter;
            if(cTrackFitter.getFittedSegments().get(0) instanceof LineFittingSegmentGroup){
                displayBreakInfo();
            }else{
                String[][] psData=cTrackFitter.getSignalJumpAsStringArray();
        //        String[][] psData=cTrackFitter.getSplittedSegmentsAsStringArray();
                JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 0, psData.length-1);
                IPOGTableSP.setViewport(jvp);
            }
        }

//        LineSegmentRegressionEvaluater.markExcludedPoints(m_cTrackFitter.getFittedSegments());        
        return 1;
    }
    int displayBreakInfo(){
        if(m_cTrackFitter==null) return -1;
        if(m_cTrackFitter instanceof PiecewisePolynomialLineFitter_ProgressiveSegmenting) return -1;
        
        PiecewisePolynomialLineFitter_ProgressiveSegmenting cTrackFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) m_cTrackFitter;
        if(!cTrackFitter.isMerged()) return -1;
        String[][] psData=cTrackFitter.getMergingSegmentsAsStraingArray();
//        String[][] psData=cTrackFitter.getSplittedSegmentsAsStringArray();
        JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 0, psData.length-1);
        IPOGTableSP.setViewport(jvp);
        return 1;
    }
    private void ZoomInBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZoomInBTActionPerformed
        // TODO add your handling code here:
        zoomIn();
    }//GEN-LAST:event_ZoomInBTActionPerformed

    private void OutliarRatioTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OutliarRatioTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_OutliarRatioTFActionPerformed

    private void DataSelectionColorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DataSelectionColorCBActionPerformed
        // TODO add your handling code here:
        highlightSelection();
    }//GEN-LAST:event_DataSelectionColorCBActionPerformed

    private void DataSelectionOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DataSelectionOptionCBActionPerformed
        // TODO add your handling code here:
        boolean pbSelected[]=m_cIPOGTPW.getDataSelection();
        String option=(String)DataSelectionOptionCB.getSelectedItem();
        if(option.contentEquals("All Points")) CommonStatisticsMethods.setElements(pbSelected, true);
        ArrayList<Integer> nvT;
        int i;
        if(option.contentEquals("Exclude All")){
            CommonStatisticsMethods.setElements(pbSelected, false);
        }
        if(option.contentEquals("Local Min")){
            CommonStatisticsMethods.setElements(pbSelected, false);
            nvT=m_cIPOGTPW.getLocalMinima();
            for(i=0;i<nvT.size();i++){
                pbSelected[nvT.get(i)]=true;
            }
        }
        if(option.contentEquals("Local Max")){
            CommonStatisticsMethods.setElements(pbSelected, false);
            nvT=m_cIPOGTPW.getLocalMaxima();
            for(i=0;i<nvT.size();i++){
                pbSelected[nvT.get(i)]=true;
            }
        }
        if(option.contentEquals("Processing Selection")){
            if(pbSelection==null){
                pbSelection=new boolean[pbSelected.length];
                CommonStatisticsMethods.setElements(pbSelection, true);
            }
            CommonStatisticsMethods.copyArray(this.pbSelection, pbSelected);
        }
        m_cIPOGTPW.calSelectedSD();
        highlightSelection();
    }//GEN-LAST:event_DataSelectionOptionCBActionPerformed

    private void SelectRoiCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectRoiCBActionPerformed
        // TODO add your handling code here:
        selectDataInRoi();
        highlightSelection();
    }//GEN-LAST:event_SelectRoiCBActionPerformed

    private void ShowDataSelectionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowDataSelectionCBActionPerformed
        // TODO add your handling code here:
        highlightSelection();
    }//GEN-LAST:event_ShowDataSelectionCBActionPerformed

    private void ResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResetActionPerformed
        // TODO add your handling code here:
        ImagePlus impl=CommonMethods.getCurrentImage();
        Window wd=impl.getWindow();
        if(wd instanceof PlotWindowPlus){
            PlotWindowPlus pw=((PlotWindowPlus)wd);
            pw.resetView();
        }
    }//GEN-LAST:event_ResetActionPerformed

    private void BreakStatusCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BreakStatusCBActionPerformed
        // TODO add your handling code here:
        Roi roi=m_cIPOGTPW.pw.getImagePlus().getRoi();
        DoubleRange xRange=new DoubleRange(),yRange=new DoubleRange();
        String status=(String) BreakStatusCB.getSelectedItem();
        CommonGuiMethods.getCoordinateRanges(m_cIPOGTPW.pw, roi, xRange, yRange);
        if(m_cTrackFitter!=null){
            if(m_cTrackFitter instanceof PiecewisePolynomialLineFitter_ProgressiveSegmenting){
            PiecewisePolynomialLineFitter_ProgressiveSegmenting cTrackFitter=(PiecewisePolynomialLineFitter_ProgressiveSegmenting) m_cTrackFitter;
                if(cTrackFitter.isMerged()) cTrackFitter.setBreakStatus(xRange,status);
            }
        }
        displayBreakInfo();
    }//GEN-LAST:event_BreakStatusCBActionPerformed

    private void CenterAtCurrentIPORBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CenterAtCurrentIPORBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CenterAtCurrentIPORBActionPerformed

    private void HighlightIPOGCenterCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HighlightIPOGCenterCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_HighlightIPOGCenterCBActionPerformed

    private void FreezeCursorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FreezeCursorCBActionPerformed
        // TODO add your handling code here:
        m_cIPOGTPW.pw.freezeCursor(FreezeCursorCB.isSelected());
    }//GEN-LAST:event_FreezeCursorCBActionPerformed

    private void FilterBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterBTActionPerformed
        // TODO add your handling code here:
        String sType=(String)OutliarTypeCB.getSelectedItem();
        Color c=CommonGuiMethods.getColorSelection((String)DataSelectionColorCB.getSelectedItem());
        double pValue=Double.parseDouble(PVDeltaTF.getText());
        int nMaxMultiplicity=2;
        if (sType.startsWith("Spike")){
            int it=sType.indexOf("e");
            nMaxMultiplicity=Integer.parseInt(sType.substring(it+1));
        }
        String sFilterType=(String)FilterOptionCB.getSelectedItem();
        if(sFilterType.startsWith("Spike")) 
            m_cIPOGTPW.filtTrackData(pValue, nMaxMultiplicity,c,true);
        else if(sFilterType.startsWith("Edge"))
            m_cIPOGTPW.filtTrackData(pValue, -1*nMaxMultiplicity,c,true);
        else if(sFilterType.startsWith("Rolling Ball"))
            m_cIPOGTPW.filtTrackData_RollingBall(c);
        else if(sFilterType.startsWith("Median RW"))
            m_cIPOGTPW.filtTrackData_Median(c);            
        else if(sFilterType.startsWith("Mean RW"))
            m_cIPOGTPW.filtTrackData_Mean(c);            
        else if(sFilterType.contentEquals("Median FRDelta"))
            m_cIPOGTPW.showAntiMedianFilter(0);
        else if(sFilterType.startsWith("Ranking Extrema"))
            m_cIPOGTPW.markRankingExtrema(c);
        else if(sFilterType.startsWith("Ranking Based"))
            m_cIPOGTPW.filtTrack_RankingBasedEdgeProtection(c,true);

        if(!DisplayOnlyCB.isSelected()){
            m_cIPOGTPW.updateFiltedData();
        }
//        ArrayList<double[]> pdvXY=new ArrayList();
//        m_cIPOGTPW.getFiltedTrackData(pdvXY);
//        m_cIPOGTPW.pw.addPlot("Filted", pdvXY.get(0), pdvXY.get(1), 1, PlotWindow.LINE,Color.red,true);

    }//GEN-LAST:event_FilterBTActionPerformed

    private void DisplayOnlyCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisplayOnlyCBActionPerformed
        // TODO add your handling code here:
        if(!DisplayOnlyCB.isSelected())
            m_cIPOGTPW.updateFiltedData();
    }//GEN-LAST:event_DisplayOnlyCBActionPerformed

    private void ActiveCurveHandlingOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ActiveCurveHandlingOptionCBActionPerformed
        // TODO add your handling code here:
        String option=(String)(ActiveCurveHandlingOptionCB.getSelectedItem());
        m_cIPOGTPW.pw.handleActiveCurve(option);
    }//GEN-LAST:event_ActiveCurveHandlingOptionCBActionPerformed

    private void FilterOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterOptionCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FilterOptionCBActionPerformed

    private void ShowTransitionsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowTransitionsCBActionPerformed
        // TODO add your handling code here:
        if(ShowTransitionsCB.isSelected()) displayTransitions();
    }//GEN-LAST:event_ShowTransitionsCBActionPerformed

    private void ConfidenceLevelCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConfidenceLevelCBActionPerformed
        // TODO add your handling code here:
        if(m_cIPOGTPW!=null&&!bInitializing){
            if(m_cIPOGT!=null){
                if(m_cIPOGTPW.IPOGT.m_cLevelInfo==null)m_cIPOGTPW.IPOGT.setLevelInfo(new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_sPlottingOption,pbSelection,m_nMaxRisingInterval)); ; 
                if(m_cIPOGT.m_cLevelInfo!=null){
                    m_cIPOGT.m_cLevelInfo.setConfidenceLevel((String)ConfidenceLevelCB.getSelectedItem())                        ;
                }
            }     
        }
    }//GEN-LAST:event_ConfidenceLevelCBActionPerformed

    private void EndLevelTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EndLevelTFActionPerformed
        // TODO add your handling code here:
        m_cIPOGTPW.IPOGT.m_cLevelInfo.setEndLevelM((Integer.parseInt(EndLevelTF.getText())));
    }//GEN-LAST:event_EndLevelTFActionPerformed

    private void InfoNodeStatusCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InfoNodeStatusCBActionPerformed
        // TODO add your handling code here:
        if(m_cIPOGTPW!=null&&!bInitializing){
            if(m_cIPOGTPW.IPOGT.m_cLevelInfo==null)
                m_cIPOGTPW.IPOGT.setLevelInfo(new IPOGTLevelInfoNode(m_cIPOGTPW.IPOGT,m_sPlottingOption,pbSelection,m_nMaxRisingInterval));    
            String item=(String)InfoNodeStatusCB.getSelectedItem();
            m_cIPOGTPW.IPOGT.m_cLevelInfo.setInfoStatus(item);
            if(item.contentEquals("Confirm Auto")) {
                ShowTransitionsCB.setText("Automated");
                updateViews();
            }else{
                ShowTransitionsCB.setText("Manual");
            }
        }
    }//GEN-LAST:event_InfoNodeStatusCBActionPerformed

    private void ClearEvaluationsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearEvaluationsBTActionPerformed
        // TODO add your handling code here:
        if(m_cIPOGTPW.IPOGT.m_cLevelInfo!=null)m_cIPOGTPW.IPOGT.m_cLevelInfo.resetEvaluations();
        updateIPOGTableView("Curve Evaluations");
    }//GEN-LAST:event_ClearEvaluationsBTActionPerformed

    private void CloseAllPlotsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseAllPlotsBTActionPerformed
        // TODO add your handling code here:
        PlotWindowPlus.handler.closeAllPlotWindowPlus();
    }//GEN-LAST:event_CloseAllPlotsBTActionPerformed

    private void CloseTempFramesBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseTempFramesBTActionPerformed
        // TODO add your handling code here:
        if(OneSchrollPaneFrame.cvFrames==null) OneSchrollPaneFrame.cvFrames=new ArrayList();
        int i,len=OneSchrollPaneFrame.cvFrames.size();
        JFrame fr;
        for(i=0;i<len;i++){
            fr=OneSchrollPaneFrame.cvFrames.get(i);
            if(fr==null) continue;
            if(fr.isVisible()) fr.dispose();
        }
        MasterFittingGUI.closeAllTempWindows();
    }//GEN-LAST:event_CloseTempFramesBTActionPerformed

    private void ClearMessageAreaCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearMessageAreaCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ClearMessageAreaCBActionPerformed
    public void clearMessageArea(boolean clear){
        ClearMessageAreaCB.setSelected(clear);
    }
    private void BatchFittingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BatchFittingCBActionPerformed
        // TODO add your handling code here:
        boolean option=!BatchFittingCB.isSelected();
        if(!option) clearMessageArea(false);
        IPOAnalyzerForm.setInteractive(!option);
    }//GEN-LAST:event_BatchFittingCBActionPerformed

    private void PVTerminalsTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PVTerminalsTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PVTerminalsTFActionPerformed

    private void SDChoiceCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SDChoiceCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SDChoiceCBActionPerformed

    private void PlotHandlerBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlotHandlerBTActionPerformed
        // TODO add your handling code here:
        PlotWindowPlus.handler.toFront();
    }//GEN-LAST:event_PlotHandlerBTActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        AnalysisMasterForm.cMessageFrame.toFront();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void AdjustLevelCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AdjustLevelCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AdjustLevelCBActionPerformed

    private void TransitionDisplayOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TransitionDisplayOptionCBActionPerformed
        // TODO add your handling code here:
        displayTransitions();
    }//GEN-LAST:event_TransitionDisplayOptionCBActionPerformed

    private void PlottingOptionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlottingOptionCBActionPerformed
        // TODO add your handling code here:
        plotTrack();
    }//GEN-LAST:event_PlottingOptionCBActionPerformed

    private void SmoothingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SmoothingCBActionPerformed
        // TODO add your handling code here:
        bSmoothCriteria=SmoothingCB.isSelected();
    }//GEN-LAST:event_SmoothingCBActionPerformed
    void clearMessageArea(){
        if(ClearMessageAreaCB.isSelected())AnalysisMasterForm.setText(null);
    }
    void highlightSelection(){
        if(ShowDataSelectionCB.isSelected()) 
            m_cIPOGTPW.pw.highlightDataSelection(m_cIPOGTPW.pdX,m_cIPOGTPW.getDataSelection(), CommonGuiMethods.getColorSelection((String)DataSelectionColorCB.getSelectedItem()));
        else
            m_cIPOGTPW.pw.removeSelectionHighlights();
    }
    int plotTrack(){
        String plottingOption=(String)(PlottingOptionCB.getSelectedItem()),refOption=(String)(PlottingReferenceOptionCB.getSelectedItem());
        int sI=m_cIPOGT.firstSlice,sF=m_cIPOGT.lastSlice,slice,sr;

        if(m_cIPOGTPW!=null){
            DoubleRange xRange=new DoubleRange(),yRange=new DoubleRange();
            int status=m_cIPOGTPW.pw.getROIRange(xRange, yRange);
            if(status==1){
                sI=Math.max((int)(xRange.getMin()+0.5),sI);
                sF=Math.min((int)(xRange.getMax()+0.5),sF);

            }
        }

        ArrayList<Double> dvX=new ArrayList(), dvY=new ArrayList();
        IPOGaussianNode IPOG, IPOG1;
        double x,y;
        String refOption0=refOption;

        if(refOption0.contentEquals("regression")) refOption=IPOGaussianNode.getDefaultRefID("plottingOption");

        String sID=null;
        if(!plottingOption.contentEquals("Current Data"))
            m_nMaxRisingInterval=getMaxRizingInterval(plottingOption);
        for(slice=sI;slice<=sF;slice++){
            IPOG=m_cIPOGT.getIPOG(slice);
            if(IPOG==null) continue;
            if(sID==null) sID=IPOG.getValueLable(plottingOption);
            x=IPOG.sliceIndex;
            y=IPOG.getValue(plottingOption,refOption);
            dvX.add(x);
            dvY.add(y);
        }
        if(refOption.contentEquals("delta")){
            int nExt=Math.max(m_nMaxRisingInterval, lastSlice-sF);
            for(slice=sF+1;slice<=sF+nExt;slice++){
                IPOG=m_cIPOGT.getIPOG(slice);
                if(IPOG==null) continue;
                y=IPOG.getValue(plottingOption,refOption);
                dvY.add(y);
            }
            int[] pnIntervals=CommonStatisticsMethods.calRisingIntervals(dvY, m_nMaxRisingInterval);
                if(pnIntervals==null){
                    dvX.clear();
                    dvY.clear();
                    return -1;
                }
            int len=dvY.size(),len1=pnIntervals.length,i;
            for(i=0;i<len1;i++){
                y=dvY.get(i)-dvY.get(i+pnIntervals[i]);
                dvY.set(i, y);
            }
            for(i=len1;i<len;i++){
                dvY.remove(len1);
            }
        }

        int len=dvX.size(),i;

        if(refOption0.contentEquals("regression")){
            int nModes=Integer.parseInt((String)RegressionOrderCB.getSelectedItem());
            PolynomialRegression sr1=CommonStatisticsMethods.getPolynomialRegression(dvX, dvY, getRegressionOrder(),nModes,Double.parseDouble(OutliarRatioTF.getText()));
            for(i=0;i<len;i++){
                dvY.set(i, dvY.get(i)-sr1.predict(dvX.get(i)));
            }
        }

        if(plottingOption.contentEquals("Current Data")&&m_cIPOGTPW.pw.isActiveCurveSelected()){
            dvX.clear();
            dvY.clear();
            PlotWindowPlus pw=m_cIPOGTPW.pw;
            int index=pw.getActiveCurveIndex();
            double[] pdX=pw.getXValues(index),pdY=pw.getYValues(index);
            len=pdX.length;

            for(i=0;i<len;i++){
                x=pdX[i];
                if(x<sI||x>sF) continue;
                dvX.add(x);
                dvY.add(pdY[i]);
            }

            if(refOption0.contentEquals("regression")){
                int nModes=Integer.parseInt((String)RegressionOrderCB.getSelectedItem());
                for(i=0;i<len;i++){
                    dvY.set(i, dvY.get(i)-m_cPr.predict(dvX.get(i)));
                }
            }
            if(refOption.contentEquals("delta")){
                int[] pnIntervals=CommonStatisticsMethods.calRisingIntervals(dvY, m_nMaxRisingInterval);
                if(pnIntervals==null){
                    dvX.clear();
                    dvY.clear();
                    return -1;
                }
                len=dvY.size();
                int len1=pnIntervals.length;
                for(i=0;i<len1;i++){
                    y=dvY.get(i)-dvY.get(i+pnIntervals[i]);
                    dvY.set(i, y);
                }
                for(i=len1;i<len;i++){
                    dvY.remove(len1);
                }
            }
        }

        CommonGuiMethods.showPlot(plottingOption+" of Track"+m_cIPOGT.TrackIndex, sID, "Slice", dvX, dvY);
        return 1;
    }
    public static int getMaxRizingInterval(String plottingOption){
        if(plottingOption.contentEquals("Peak1 Raw")||plottingOption.contentEquals("Peak3 Raw")) return 2;//12n28
        return 4;
    }
    /**
    * @param args the command line arguments
    */
    public static IPOGTLevelTransitionAnalyzer main(String args[]) {
        final IPOGTLevelTransitionAnalyzer analyzer=new IPOGTLevelTransitionAnalyzer(null,null,null,-1,null);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                analyzer.setVisible(true);
            }
        });
        return analyzer;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox ActiveCurveHandlingOptionCB;
    private javax.swing.JRadioButton AddPlotRB;
    private javax.swing.JCheckBox AdjustLevelCB;
    private javax.swing.JCheckBox AppendTableCB;
    private javax.swing.JButton AutoDetectBT;
    private javax.swing.JLabel AutomationLabel;
    private javax.swing.JScrollPane AveImageSP;
    private javax.swing.JCheckBox BatchFittingCB;
    private javax.swing.JComboBox BreakStatusCB;
    private javax.swing.JCheckBox CenterAtCurrentIPORB;
    private javax.swing.JButton ClearEvaluationsBT;
    private javax.swing.JCheckBox ClearMessageAreaCB;
    private javax.swing.JButton ClearRegressionBT;
    private javax.swing.JButton CloseAllPlotsBT;
    private javax.swing.JButton CloseTempFramesBT;
    private javax.swing.JComboBox ConfidenceLevelCB;
    private javax.swing.JRadioButton CreateTransitionRB;
    private javax.swing.JLabel CursorLB;
    private javax.swing.JComboBox DataSelectionColorCB;
    private javax.swing.JComboBox DataSelectionOptionCB;
    private javax.swing.JLabel DeltaLB;
    private javax.swing.JCheckBox DisplayOnlyCB;
    private javax.swing.JComboBox DisplayOptionsCB;
    private javax.swing.JTextField EndLevelTF;
    private javax.swing.JComboBox EvaluationElementsCB;
    private javax.swing.JButton FilterBT;
    private javax.swing.JComboBox FilterOptionCB;
    private javax.swing.JButton FitTrackBT;
    private javax.swing.JComboBox FittingOptionCB;
    private javax.swing.JTextField FontTF;
    private javax.swing.JCheckBox FreezeCursorCB;
    private javax.swing.JCheckBox HighlightIPOGCenterCB;
    private javax.swing.JScrollPane IPOGTableSP;
    private javax.swing.JComboBox IPOGsFittingOptionCB;
    private javax.swing.JComboBox IPOTableViewSelectionCB;
    private javax.swing.JComboBox InfoNodeStatusCB;
    private javax.swing.JLabel LevelLB;
    private javax.swing.JTextField LineWidthTF;
    private javax.swing.JCheckBox MarkExcessiveDeltaCB;
    private javax.swing.JButton NextTrack;
    private javax.swing.JTextField OutliarRatioTF;
    private javax.swing.JComboBox OutliarTypeCB;
    private javax.swing.JComboBox OutputOptionCB;
    private javax.swing.JButton OutputValuesAsTableBT;
    private javax.swing.JCheckBox OveridePlotParsCB;
    private javax.swing.JTextField PSidenessTF;
    private javax.swing.JTextField PVChiSQTF;
    private javax.swing.JTextField PVDeltaTF;
    private javax.swing.JTextField PVPWDevTF;
    private javax.swing.JTextField PVTerminalsTF;
    private javax.swing.JTextField PVTiltingTF;
    private javax.swing.JButton PlotBT;
    private javax.swing.JComboBox PlotColorCB;
    private javax.swing.JButton PlotHandlerBT;
    private javax.swing.JScrollPane PlotImageSP;
    private javax.swing.JTextField PlotShapeTF;
    private javax.swing.JComboBox PlottingOptionCB;
    private javax.swing.JComboBox PlottingReferenceOptionCB;
    private javax.swing.JButton PreviousTrackBT;
    private javax.swing.JLabel RegressionLB;
    private javax.swing.JComboBox RegressionModesCB;
    private javax.swing.JComboBox RegressionOrderCB;
    private javax.swing.JRadioButton RemoveTransitionRB;
    private javax.swing.JButton Reset;
    private javax.swing.JComboBox SDChoiceCB;
    private javax.swing.JComboBox SegmentOptionCB;
    private javax.swing.JComboBox SelectRoiCB;
    private javax.swing.JCheckBox ShowDataSelectionCB;
    private javax.swing.JCheckBox ShowLevelCB;
    private javax.swing.JButton ShowPlotDataBT;
    private javax.swing.JButton ShowRegressionBT;
    private javax.swing.JRadioButton ShowSegmentRB;
    private javax.swing.JRadioButton ShowTransitions;
    private javax.swing.JCheckBox ShowTransitionsCB;
    private javax.swing.JLabel SliceLB;
    private javax.swing.JCheckBox SmoothingCB;
    private javax.swing.JLabel StackMinLB;
    private javax.swing.JScrollPane TrackImageSP;
    private javax.swing.JComboBox TransitionDisplayOptionCB;
    private javax.swing.JTextField TransitionPValueTF;
    private javax.swing.JRadioButton UpdateCursorRB;
    private javax.swing.JLabel Value;
    private javax.swing.JButton ZoomInBT;
    private javax.swing.JButton detectTransitionsBT;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
    public void updateIPOGTPW(IPOGTrackPlotWindow IPOGTPW, String plottingOption){
        bInitializing=true;
        bSmoothCriteria=SmoothingCB.isSelected();
        m_pdStackMinima=m_cIPOAnalyzer.getStackMinima(plottingOption);
        m_pnStackMinIndex=m_cIPOAnalyzer.getStackMinIndexes(plottingOption);
        if(m_cvHLCollections==null)
            m_cvHLCollections=new ArrayList();
        else
            m_cvHLCollections.clear();
        if(!IPOAnalyzerForm.isInteractive()) clearMessageArea(false);

        if(IPOAnalyzerForm.isInteractive()&&m_cIPOGTPW!=null) m_cIPOGTPW.pw.close();
        pdX=IPOGTPW.pdX;
        pdY=IPOGTPW.pdY;
        CenterAtCurrentIPORB.setSelected(false);
        m_cIPOGTPW=IPOGTPW;
        m_cIPOGT=IPOGTPW.IPOGT;
        String title=" Level Transitions in Track"+m_cIPOGT.TrackIndex+" of video "+m_sImageID;
        m_cIPOGTPW.setImageID(m_sImageID);
        this.setTitle(title);
        fireActionEvent(new ActionEvent(this,0,"parentTitle "+title));
        m_cIPOGTPW.pw.getCanvas().addMouseListener(this);
        m_cIPOGTPW.pw.getCanvas().addMouseMotionListener(this);
        m_cIPOGTPW.IPOGT.w=w;
        m_cIPOGTPW.IPOGT.h=h;
        

        getTrackData(m_dvX,m_dvY);
        m_nMaxRisingInterval=getMaxRizingInterval(plottingOption);
        m_cIPOGTPW.setRisingInterval(m_nMaxRisingInterval);
        calRisingIntervals();
        m_cIPOGT.setRisingInterval(m_nMaxRisingInterval);

        m_sPlottingOption=plottingOption;
//        m_cTrackPlotViewport=CommonGuiMethods.getJViewport(IPOGTPW.pw);

        JPanel PlotImagePane=new JPanel();
        PlotImagePane.add(m_cIPOGTPW.pw.getCanvas());
        m_cTrackPlotViewport.setView(PlotImagePane);

        PlotImageSP.setBounds(m_cIPOGTPW.pw.getCanvas().getBounds());
        PlotImageSP.setViewportView(m_cTrackPlotViewport);
        implTrackPlot=IPOGTPW.pw.getImagePlus();
        implTrackPlot.getWindow().getCanvas().addPaintListener(this);
        m_cIPOGT.activateLevelInfo(m_cIPOGTPW.plotOption);
        ArrayList<Double> dvX=new ArrayList(), dvY=new ArrayList();
        getTrackData(dvX,dvY);
//        ShowLevelCB.setSelected(false);
        IPOGTPW.pw.toBack();
        if(m_cIPOGTPW.IPOGT.m_cLevelInfo!=null){
            updateViews();
            updateIPOGTableView("Curve Evaluations");
        }
        initComboBoxSelections();
        bInitializing=false;
        DisplayOnlyCB.setSelected(true);
    }
    void initComboBoxSelections(){
        if(m_cIPOGTPW.IPOGT.m_cLevelInfo!=null){
            if(m_cIPOGTPW.IPOGT.m_cLevelInfo.svCurveEvaluations_Manual.size()>0) 
                CommonGuiMethods.setSelectedItem(this,EvaluationElementsCB, m_cIPOGTPW.IPOGT.m_cLevelInfo.svCurveEvaluations_Manual.get(0));
            else
                CommonGuiMethods.setSelectedItem(this,EvaluationElementsCB, IPOGTLevelInfoNode.psTrackEvaluationElements[0]);
            
            CommonGuiMethods.setSelectedItem(this,InfoNodeStatusCB, m_cIPOGTPW.IPOGT.m_cLevelInfo.sInfoNodeStatus_Manual);
            CommonGuiMethods.setSelectedItem(this,ConfidenceLevelCB, m_cIPOGTPW.IPOGT.m_cLevelInfo.sTransitionConfidenceM);
        }else{
            EvaluationElementsCB.setSelectedItem(IPOGTLevelInfoNode.psTrackEvaluationElements[0]);
            InfoNodeStatusCB.setSelectedItem(IPOGTLevelInfoNode.psInfoNodeStatus[0]);;
            ConfidenceLevelCB.setSelectedItem(IPOGTLevelInfoNode.psTransitionDetectionConfidence[0]);;
        }
    }
    void showTransitions(){
        m_cIPOGT.activateLevelInfo(m_cIPOGTPW.plotOption);
        updateViews();
    }
    int updateViews(){
        if(m_cIPOGT.m_cLevelInfo==null) return -1;;
        m_cLevelIPOGTable=buildCommonTable(m_cIPOGT.m_cLevelInfo.getIPOs());
        m_cIPOGT.m_cLevelInfo.evaluateSignal();
        m_cLevelIPOGTable.getModel().addTableModelListener(this);
//        if(m_cIPOGTPW.IPOGT.m_cLevelInfo.LevelIPOGsAreComputed())m_cLevelIPOViewport=buildCommonTableView(m_cLevelIPOGTable);
//        if(m_cIPOGTPW.IPOGT.m_cLevelInfo.LevelIPOGsAreComputed())m_cTrackIPOViewport=buildCommonTableView(m_cIPOGT.m_cvIPOGs,false);
        m_cLevelIPOViewport=buildCommonTableView(m_cLevelIPOGTable);
        m_cTrackIPOViewport=buildCommonTableView(m_cIPOGT.m_cvIPOGs,false);
        m_cTrackInfoViewport=buildTrackInfoTableView();
        m_cTrackIPOViewportDetailed=buildCommonTableView(IPOGaussianNodeHandler.getSimpleIPOGs(m_cIPOGT.m_cvIPOGs),true);
        m_cLevelInfoViewport_M=buildLevelInfoViewport_M();
        m_cLevelInfoViewport_A=buildLevelInfoViewport_A();
        m_cCurveEvaluationViewport=buildCurveEvaluationView();
        updateIPOGTableView((String)IPOTableViewSelectionCB.getSelectedItem());
        displayTransitions();
        return 1;
    }
    JComboBox getFittingOptionCB(){
        return FittingOptionCB;
    }
    JComboBox getFilteringOptionCB(){
        return FilterOptionCB;
    }    
    int calLevelIPOs_Background(){
        final IPOGTrackNode IPOGT=m_cIPOGTPW.IPOGT;
        if(IPOGT.m_cLevelInfo==null) return -1;
        if(!m_cIPOGT.m_cLevelInfo.ComputeShape()) return -1;
        final int[][][] pixelsCompenStack=this.pixelsCompenStack;
        final int[][] pixelsScratch=new int[h][w],pnScratch=new int[h][w];
        Thread t=new Thread(new Runnable() {            
                public void run() {
                    IPOGTLevelTransitionAnalyzer.nBackgroundJobID++;
                    int ID=IPOGTLevelTransitionAnalyzer.nBackgroundJobID;
                    String st="Background Job Submitted: ID="+ID+"  (Fitting Level IPO for Track"+IPOGT.TrackIndex+"  Time: "+System.currentTimeMillis();
                    addToStatusArea(st);
                    
                    calLevelIPOs(IPOGT,pixelsStack,pixelsCompenStack, pixelsScratch, pnScratch,getFittingOption());
                    m_cIPOGTPW.IPOGT.m_cLevelInfo.evaluateSignal();
                    
                    st="Background Job Finished: ID="+ID+"  (Fitting Level IPO for Track"+IPOGT.TrackIndex+"  Time: "+System.currentTimeMillis();
                    addToStatusArea(st);
                    if(IPOGT==m_cIPOGTPW.IPOGT) updateViews();
                }
        });
        t.start();  
        return 1;
    }
    public static int buildContours(IPOGTLevelInfoNode aInfo,int[][][]pixelsStack,int[][][] pixelsCompen,int[][]pnScratch,int[][] pnScratcht){
        ArrayList<IPOGaussianNodeComplex> cvIPOGs=aInfo.getIPOs();
        IPOGTrackNode IPOGT=aInfo.cIPOGT;
        int sI,sF,i,len=cvIPOGs.size();
        
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode pNode;
        IPOGT.m_cLevelInfo.cvStoredContourParNodes.clear();
        boolean show=false;
        for(i=0;i<len;i++){
            IJ.showStatus("Building IPOG Contour "+i);
            IPOG=cvIPOGs.get(i);
            IPOG.cvContourParNodes.clear();
            sI=IPOG.sliceI;
            sF=IPOG.sliceF;
            
            IPOG.buildContour_Percentage(IPOG,IPOGContourParameterNode.IPOG,show);
            IPOGTrackNode.buildAveIPOGaussianMeanContour(IPOG, IPOGT, pixelsCompen, pnScratch, pnScratcht, sI, sF, show);
            IPOGTrackNode.buildAveIPOsRawPixelContour(IPOG, IPOGT, pixelsStack, pnScratch, pnScratcht, sI, sF, show);
//            m_cIPOGTPW.IPOGT.m_cLevelInfo.storeContourParNode(IPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw));
            aInfo.storeContourParNodes(IPOG.cvContourParNodes);
        }
        return 1;
    }
    int calLevelIPOs(){
        IPOGTrackNode IPOGT=m_cIPOGTPW.IPOGT;
        if(IPOGT.m_cLevelInfo==null) return -1;
//        if(!m_cIPOGT.m_cLevelInfo.LevelIPOGsAreComputed()){;
//            int[][][] pixelsCompenStack=this.pixelsCompenStack;
            int[][] pixelsScratch=new int[h][w],pnScratch=new int[h][w];        
            calLevelIPOs(IPOGT,pixelsStack,pixelsCompenStack, pixelsScratch, pnScratch,getFittingOption());
            m_cIPOGTPW.IPOGT.m_cLevelInfo.evaluateSignal();        
//        }
        if(IPOGT==m_cIPOGTPW.IPOGT) updateViews();     
        return 1;
    }
    public static int calLevelIPOs(IPOGTrackNode IPOGT,int[][][]pixelsStack,int[][][]pixelsCompenStack,int[][] pixelsScratch, int[][] pnScratch, int RefittingOption){
        switch(RefittingOption){
            case IPOGTLevelTransitionAnalyzer.RefitCompleteIPOGs:
                IPOGT.m_cLevelInfo.clearIPOGs();
                break;
            case IPOGTLevelTransitionAnalyzer.RefitIPOGs:
                break;
            default:
                return -1;                
        }
        IPOGTLevelInfoNode aInfo=IPOGT.m_cLevelInfo;
        if(aInfo==null) return -1;
        int w=IPOGT.w, h=IPOGT.h;
        calLevelIPOs(IPOGT,pixelsCompenStack, pixelsScratch, pnScratch,aInfo.sQuantityName);
        buildContours(IPOGT.m_cLevelInfo,pixelsStack,pixelsCompenStack,pixelsScratch,pnScratch);
//        aInfo.evaluateSignal();        
        return 1;
    }
    public void getTrackData(ArrayList<Double> dvX, ArrayList<Double> dvY){
        dvX.clear();
        dvY.clear();
        String option=m_cIPOGTPW.plotOption;
        int len=m_cIPOGT.m_cvIPOGs.size(),i;
        IPOGaussianNode IPOG;
        int sI=m_cIPOGT.firstSlice,sF=m_cIPOGT.lastSlice,slice;
        for(slice=sI;slice<=sF;slice++){
            IPOG=m_cIPOGT.getIPOG(slice);
            if(IPOG==null) continue;
            dvX.add((double)slice);
            dvY.add(IPOG.getValue(option));
        }
    }
    public static int calLevelIPOs(IPOGTrackNode IPOGT,int[][][] pixelsCompenStack, int[][] pixelsScratch, int[][] pnScratch, String QuantityName){
        IPOGTLevelInfoNode cLevelInfoNode; 
        IPOGT.activateLevelInfo(QuantityName);
        if(IPOGT.m_cLevelInfo==null) return -1;
//        if(!IPOGT.m_cLevelInfo.ComputeShape()) return -1;
        cLevelInfoNode=IPOGT.m_cLevelInfo;
        
        IPOGTLevelNode lNode;
        int nTrackHeadLen=IPOGT.m_cLevelInfo.getTrackHeadLength();
        int sI=IPOGT.firstSlice,sF=Math.min(sI+nTrackHeadLen, IPOGT.lastSlice);
        IPOGaussianNodeComplex IPOGt=IPOGT.m_cLevelInfo.retrieveStoredIPOG(sI, sF);        
        if(IPOGt==null){
            IPOGt=IPOGTrackNode.buildAveIPOG_FullModel(IPOGT,pixelsCompenStack, pixelsScratch, pnScratch,sI,sF,new intRange(),new intRange(), false, false, IPOGContourParameterNode.IPOG);
            if(IPOGt!=null) IPOGt.setLevel(100);            
        }
        if(IPOGt==null) IPOGt=IPOGaussianExpander.getDummyIPOG();
//        IPOGTrackNode.buildAveIPOsRawPixelContour(IPOGt, IPOGT, pixelsStack, pnScratch, pnScratch, sI, sF, false);
        IPOGT.m_cLevelInfo.storeIPOG(IPOGt);
        cLevelInfoNode.m_cTrackHeadIPOG=IPOGt;
        
        sI=IPOGT.lastSlice+getMaxRizingInterval(QuantityName);
        sF=IPOGT.lastSliceE;
        if(sI>=sF) 
            sI=sF-5;
        IPOGt=IPOGT.m_cLevelInfo.retrieveStoredIPOG(sI, sF);        
        if(IPOGt==null){
            IPOGt=IPOGTrackNode.buildAveIPOG_FullModel(IPOGT,pixelsCompenStack, pixelsScratch, pnScratch,sI,sF,new intRange(),new intRange(), false, false, IPOGContourParameterNode.IPOG);
            if(IPOGt!=null) IPOGt.setLevel(-100);            
        }
        if(IPOGt==null) IPOGt=IPOGaussianExpander.getDummyIPOG();
//        IPOGTrackNode.buildAveIPOsRawPixelContour(IPOGt, IPOGT, pixelsStack, pnScratch, pnScratch, sI, sF, false);
        IPOGT.m_cLevelInfo.storeIPOG(IPOGt);
        
        int i,len;
        ArrayList<IPOGTLevelNode> cvLevelNodes=IPOGT.m_cLevelInfo.m_cvLevelNodes_A;
        len=cvLevelNodes.size();
        for(i=0;i<len;i++){
            lNode=cvLevelNodes.get(i);
            buildLevelIPOG(lNode,pixelsCompenStack,pixelsScratch,pnScratch);
        }
        return 1;
    }
    
    public static int buildLevelIPOG(IPOGTLevelNode lNode,int[][][]pixelsCompenStack,int[][]pixelsScratch,int[][] pnScratch){        
        IPOGTLevelInfoNode cInfoNode=lNode.cIPOGT.m_cLevelInfo;
        int sI=lNode.sliceI,sF,nWs=cInfoNode.getMaxSegLength();
        sF=Math.min(sI+nWs, lNode.sliceF);
        int lastSlice=lNode.cIPOGT.lastSlice;
        int p=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(lNode.pdX, lastSlice, 1);
        p=CommonStatisticsMethods.getFirstExtremumIndex(lNode.pdY, p, 1, -1);
        lastSlice=(int)(lNode.pdX[p]+0.5);
        sF=Math.min(sF, lastSlice);
        sF=Math.max(sI+2, sF);
        sF=Math.min(sF, lNode.sliceF);
        
        
        IPOGaussianNodeComplex IPOGt=cInfoNode.retrieveStoredIPOG(sI, sF);
        if(sI==104&&sF==107){
            sI=sI;
        }
        if(IPOGt==null) IPOGt=IPOGTrackNode.buildAveIPOG_FullModel(lNode.cIPOGT,pixelsCompenStack,pixelsScratch,pnScratch,sI,sF,new intRange(),new intRange(), false, false, IPOGContourParameterNode.IPOG);
//        IPOGTrackNode.buildAveIPOsRawPixelContour(IPOGt, lNode.cIPOGT, pixelsStack, pnScratch, pnScratch, sI, sF, false);
        cInfoNode.storeIPOG(IPOGt);
        if(IPOGt==null) {
            IPOGt=IPOGaussianExpander.getDummyIPOG();
            IPOGt.setSliceRange(-1, -1);
        }
        lNode.m_cIPOG=IPOGt;
        IPOGt.setLevel(lNode.nLevel);
        
        if(lNode.sliceF-lNode.sliceI>nWs){
            sF=lNode.sliceF;
            sI=Math.max(sF-nWs, lNode.sliceI);
            sF=Math.min(sF, lastSlice);
            sF=Math.max(sI+2, sF);
            sF=Math.min(sF, lNode.sliceF);

            IPOGt=cInfoNode.retrieveStoredIPOG(sI, sF);
            if(IPOGt==null) IPOGt=IPOGTrackNode.buildAveIPOG_FullModel(lNode.cIPOGT,pixelsCompenStack,pixelsScratch,pnScratch,sI,sF,new intRange(),new intRange(), false, false, IPOGContourParameterNode.IPOG);
    //        IPOGTrackNode.buildAveIPOsRawPixelContour(IPOGt, lNode.cIPOGT, pixelsStack, pnScratch, pnScratch, sI, sF, false);
            cInfoNode.storeIPOG(IPOGt);
            if(IPOGt==null) {
                IPOGt=IPOGaussianExpander.getDummyIPOG();
                IPOGt.setSliceRange(-1, -1);
            }
            lNode.m_cIPOGR=IPOGt;
            IPOGt.setLevel(lNode.nLevel);
        }else{
            lNode.m_cIPOGR=IPOGt;
        }
        return 1;
    }
        
    int displayTransitions(){
        if(!IPOAnalyzerForm.isInteractive()) return -1;
            m_cIPOGTPW.pw.removePlotGroup("FittingLine");
            m_cIPOGTPW.pw.removePlotGroup("Transition");
            m_cIPOGTPW.pw.removePlotGroup("Optimal");
            m_cIPOGTPW.pw.removePlotGroup("Merge");
            m_cIPOGTPW.pw.removePlotGroup("Seg");
        
//        if(m_cIPOGT.m_cLevelInfo==null) fitTrack("Filted Amp");
        if(m_cIPOGT.m_cLevelInfo==null) {
            showAveIPGImage();
            updateIPOGTableView("Curve Evaluations");
            return -1;
        }
        ArrayList<IPOGTLevelNode> cvLevelNodes=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_A;
        if(((String)TransitionDisplayOptionCB.getSelectedItem()).contentEquals("Manual Detection")) 
            cvLevelNodes=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M;
        
        if(cvLevelNodes==null) return -1;
        
        if(cvLevelNodes.size()==0) return -1;
        IPOGTLevelNode lNode0,lNode;
        int sI=m_cIPOGT.firstSlice,sF=Math.min(m_cIPOGT.lastSlice,sI+10);
        int i,len=cvLevelNodes.size();
        lNode0=cvLevelNodes.get(0);

        int ls=IPOGTLevelNode.nRulerSize;
        double dXR=Math.min(lNode0.sliceI+ls,lNode0.sliceF),dXL=Math.max(lNode0.sliceI, lNode0.sliceF-ls);

        intRange xRange=new intRange(),yRange=new intRange();
        CommonStatisticsMethods.setElements(pixelsIPO, 1000);

        m_cIPOGT.buildAveIPO(pixelsCompenStack, pixelsScratch, pnScratch, sI, sF, xRange,yRange);
        Point lt=new Point(0,len*(hIPO+1));
        loadIPOPixels(lt,xRange,yRange);

        m_cIPOGT.buildAveIPO_Raw(pixelsStack, pixelsScratch, pnScratch, sI, sF, xRange,yRange);
        
        lt.setLocation(wIPO+1,len*(wIPO+1));
        loadIPOPixels(lt,xRange,yRange);

        double x0=0,y0=0;
        for(i=0;i<len;i++){
            lNode=cvLevelNodes.get(i);
            if(i==0){
                x0=lNode.sliceI;
                y0=lNode.dL;
            }
            sI=lNode.sliceI;
            sF=lNode.sliceF;
            
            dXR=Math.min(sI+ls,sF);
            dXL=Math.max(sI, sF-ls);

            m_cIPOGT.buildAveIPO(pixelsCompenStack, pixelsScratch, pnScratch, sI, sF, xRange,yRange);
            lt.setLocation(0,i*(hIPO+1));
            loadIPOPixels(lt,xRange,yRange);

            m_cIPOGT.buildAveIPO_Raw(pixelsStack, pixelsScratch, pnScratch, sI, sF, xRange,yRange);
            lt.setLocation(wIPO+1,i*(hIPO+1));
            loadIPOPixels(lt,xRange,yRange);
            lNode0=lNode;

            lNode.seg.display(m_cIPOGTPW.pw, x0, y0);
            x0=lNode.sliceF;
            y0=lNode.dR;
        }
        updateIPOGTableView((String)IPOTableViewSelectionCB.getSelectedItem());
        showAveIPGImage();
        return 1;
    }
    void showAveIPGImage(){
        int mag=4,minBrightness=50;
        Image img=CommonMethods.getBufferedImage(BufferedImage.TYPE_INT_RGB, pixelsIPO, minBrightness,mag);
        int font=Integer.parseInt(FontTF.getText());
        ImageIcon ic=new ImageIcon();
        ic.setImage(img);
        ScrollablePicture sp=new ScrollablePicture(ic,5);
        JViewport IPOImage=new JViewport();
        IPOImage.setView(sp);
        AveImageSP.setViewport(IPOImage);
    }
    void loadIPOPixels(Point lt,intRange xRange, intRange yRange){
        int dx=lt.x-xRange.getMin(),dy=lt.y-yRange.getMin();
        CommonStatisticsMethods.copyArray(pixelsScratch, pixelsIPO, yRange.getMin(),yRange.getMax(),xRange.getMin(),xRange.getMax(),dy,dx);
    }
    void AppendIPOGs(ArrayList<IPOGaussianNode> IPOGs, ArrayList<IPOGaussianNode> IPOGst){
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGst.add(IPOGs.get(i));
        }
    }
    public void addCollection(HighlightingRoiCollectionNode aCollection){

    };
    public HighlightingRoiCollectionNode getCollection(String sID){
        int i,len=m_cvHLCollections.size();
        HighlightingRoiCollectionNode hl;
        for(i=0;i<len;i++){
            hl=m_cvHLCollections.get(i);
            if(hl.matchingID(sID)) return hl;
        }
        return null;
    }
    public void removeCollection(String sID){
        int i,len=m_cvHLCollections.size();
        HighlightingRoiCollectionNode hl;
        for(i=len-1;i>=0;i--){
            hl=m_cvHLCollections.get(i);
            if(hl.matchingID(sID)) m_cvHLCollections.remove(i);
        }
    }
    public void showHighlightCollections(){
        if(m_cvHLCollections!=null) { 
            int i,len=m_cvHLCollections.size();
            HighlightingRoiCollectionNode hl;
            for(i=0;i<len;i++){
                hl=m_cvHLCollections.get(i);
                if(hl.isVisible())hl.highlight();
            }
        };
    }

    public void setHighlight(boolean highlight){
    }
    public void actionPerformed(ActionEvent ae){
        showHighlightCollections();
    }
    public void mouseReleased(MouseEvent e){}

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e){}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){
    }
    public void mouseDragged(MouseEvent e){}

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e){
        applyMousemove(e);
    }
    int applyMousemove(MouseEvent e){
        Point cursor;
        if(m_cIPOGTPW.pw.getImagePlus().getCanvas()==e.getSource()){
            IPOGTLevelNode lNode=null;
            cursor=CommonGuiMethods.getCursorLocation_ImageCoordinates(m_cIPOGTPW.pw.getImagePlus());
            String plotValue=m_cIPOGTPW.pw.getPoingCoordinate(cursor.x, cursor.y);
            StringTokenizer stk=new StringTokenizer(plotValue,"X,Y= ");
            if(stk.countTokens()!=2) return -1;
            double x=Double.parseDouble(stk.nextToken()),y=Double.parseDouble(stk.nextToken());
            int slice=(int)(x+0.5);
            if(slice<firstSlice) slice=firstSlice;
            if(slice>lastSlice) slice=lastSlice;
            if(m_cIPOGT.m_cLevelInfo!=null)lNode=IPOGTLevelInfoNode.getLevelNode(m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M, slice);
//            int index=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, slice, 1);
            int index=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, slice, 1);
            SliceLB.setText("Index: "+index+"    Slice: "+slice);
            CursorLB.setText("Coursor: X="+PrintAssist.ToString(x,1)+" Y="+PrintAssist.ToString(y,1));
            String sID=m_cIPOGT.m_cvIPOGs.get(0).getValueLable(m_sPlottingOption);
            IPOGaussianNode IPOG=m_cIPOGT.getIPOG(slice),IPOG1=null;
            if(index>0&&index<m_pnRisingIntervals.length) IPOG1=m_cIPOGT.getIPOG(slice+m_pnRisingIntervals[index]);
            updateTrackImage(slice);
            if(IPOG!=null){
                Value.setText(m_sPlottingOption+": Y="+PrintAssist.ToString(IPOG.getValue(sID),1));
                
            } else
                Value.setText(m_sPlottingOption+": A Gap");

            if(IPOG!=null&&IPOG1!=null){
                DeltaLB.setText("Delta: "+PrintAssist.ToString((IPOG.getValue(sID)-IPOG1.getValue(sID)),1));
            }
            RegressionLB.setText("Regression: Y="+PrintAssist.ToString(getRegressionValue(x),1));
            StackMinLB.setText("StackMin: (Track:"+m_pnStackMinIndex[slice-firstSlice]+")Y="+PrintAssist.ToString(m_pdStackMinima[slice-firstSlice],1));
            if(lNode!=null)
                LevelLB.setText("Level: "+lNode.nLevel);
            else
                LevelLB.setText("Level: ND");
        }
        return 1;
    }
    public int updateTrackImage(int slice){
        if(slice<m_cIPOGT.firstSlice||slice>m_cIPOGT.lastSliceE) return -1;
        int stackSize=pixelsStack.length;
        int font=Integer.parseInt(FontTF.getText());
        int rows=2,cols=12,r=9,mag=4;
        int slices=rows*cols;
        int nPs=5;
        int len=2*r+1;
        int ht=rows*(len+1)-1,wt=cols*(len+1)-1;
        w=pixelsStack[0][0].length;
        h=pixelsStack[0].length;
        pixelsTrack=CommonStatisticsMethods.getIntArray2(pixelsTrack, wt, ht);
        m_cTrackViewer=new ScrollPaneImageViewer(TrackImageSP);
        CommonStatisticsMethods.setElements(pixelsTrack, 1000);
        int sI=slice-nPs,sF=slice+(slices-nPs-1);
        slice=sI;
        
        IPOGaussianNode IPOG=m_cIPOGT.getIPOG(slice);
        if(IPOG==null) IPOG=m_cIPOGT.getIPOGE(slices);
        if(IPOG==null){
            return -1;
        }
        Point pt=IPOG.getCenter();
        int xI=Math.max(0, pt.x-r),xF=Math.min(w-1, pt.x+r);
        int yI=Math.max(0, pt.y-r),yF=Math.min(w-1, pt.y+r);
        int[][] pixels;
        int x,y,dx,dy,row,col;
        ArrayList<Roi> rois=new ArrayList();
        PointRoi pr;
        for(slice=sI;slice<=sF;slice++){
            if(slice<firstSlice||slice>=stackSize) continue;
            IPOG=m_cIPOGT.getIPOG(slice);
            if(IPOG==null) IPOG=m_cIPOGT.getIPOGE(slices);
            if(IPOG!=null&&CenterAtCurrentIPORB.isSelected())
                pt=IPOG.getCenter();
            xI=Math.max(0, pt.x-r);
            xF=Math.min(w-1, pt.x+r);
            yI=Math.max(0, pt.y-r);
            yF=Math.min(w-1, pt.y+r);
            pixels=pixelsStack[slice-firstSlice];
            row=(slice-sI)/cols;
            col=(slice-sI)%cols;
            dx=r-pt.x+(len+1)*col;
            dy=r-pt.y+(len+1)*row;
            if(IPOG!=null&&HighlightIPOGCenterCB.isSelected()){
                pr=new PointRoi(IPOG.getXc()+dx,IPOG.getYc()+dy);
                pr.setColor(Color.RED);

                rois.add(pr);
            }
            for(y=yI;y<=yF;y++){
                for(x=xI;x<=xF;x++){
                    pixelsTrack[y+dy][x+dx]=pixels[y][x];
                }
            }
        }
        int xl1=(nPs%cols)*(len+1),xl2=xl1,yl1=(nPs/cols)*(len+1),yl2=yl1+len;
        Line line=new Line(xl1,yl1,xl2,yl2);
        Line.setColor(Color.GREEN);
        rois.add(line);
        m_cTrackViewer.updateImage(pixelsTrack, 4,rois,font);
        return 1;
    }
/*    void updateTrackImageo(int slice){
        int stackSize=pixelsStack.length;
        int font=Integer.parseInt(FontTF.getText());
        int rows=2,cols=12,r=9,mag=4;
        int slices=rows*cols;
        int nPs=5;
        int len=2*r+1;
        int ht=rows*(len+1)-1,wt=cols*(len+1)-1;
        w=pixelsStack[0][0].length;
        h=pixelsStack[0].length;
        pixelsTrack=CommonStatisticsMethods.getIntArray2(pixelsTrack, wt, ht);
        m_cTrackViewer=new ScrollPaneImageViewer(TrackImageSP);
        CommonStatisticsMethods.setElements(pixelsTrack, 1000);
        int sI=slice-nPs,sF=slice+(slices-nPs-1);
        slice=sI;
        IPOGaussianNode IPOG=m_cIPOGT.getIPOG(slice);
        while(IPOG==null){
            slice++;
            IPOG=m_cIPOGT.getIPOG(slice);
        }
        Point pt=IPOG.getCenter();
        int xI=Math.max(0, pt.x-r),xF=Math.min(w-1, pt.x+r);
        int yI=Math.max(0, pt.y-r),yF=Math.min(w-1, pt.y+r);
        int[][] pixels;
        int x,y,dx,dy,row,col;
        ArrayList<Roi> rois=new ArrayList();
        PointRoi pr;
        for(slice=sI;slice<=sF;slice++){
            if(slice<firstSlice||slice>=stackSize) continue;
            IPOG=m_cIPOGT.getIPOG(slice);
            if(IPOG!=null&&CenterAtCurrentIPORB.isSelected())
                pt=IPOG.getCenter();
            xI=Math.max(0, pt.x-r);
            xF=Math.min(w-1, pt.x+r);
            yI=Math.max(0, pt.y-r);
            yF=Math.min(w-1, pt.y+r);
            pixels=pixelsStack[slice-firstSlice];
            row=(slice-sI)/cols;
            col=(slice-sI)%cols;
            dx=r-pt.x+(len+1)*col;
            dy=r-pt.y+(len+1)*row;
            if(IPOG!=null&&HighlightIPOGCenterCB.isSelected()){
                pr=new PointRoi(IPOG.getXc()+dx,IPOG.getYc()+dy);
                pr.setColor(Color.RED);

                rois.add(pr);
            }
            for(y=yI;y<=yF;y++){
                for(x=xI;x<=xF;x++){
                    pixelsTrack[y+dy][x+dx]=pixels[y][x];
                }
            }
        }
        int xl1=(nPs%cols)*(len+1),xl2=xl1,yl1=(nPs/cols)*(len+1),yl2=yl1+len;
        Line line=new Line(xl1,yl1,xl2,yl2);
        Line.setColor(Color.GREEN);
        rois.add(line);
        m_cTrackViewer.updateImage(pixelsTrack, 4,rois,font);
    }*/
    double getRegressionValue(double x){
        int index=0;
        return 0;//need to fix it later, more likely this will become obsolete
        /*IPOGTLevelNode lNode=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M.get(0);
        int len=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M.size();
        while(lNode.dXEnd<x){
            index++;
            if(index>=len) break;
            lNode=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M.get(index);
        }
        return lNode.cSr.predict(x);*/
    }
    public void mousePressed(MouseEvent e){
    }
    public void mouseClicked(MouseEvent e){
        applyClick(e);
    }
    int applyClick(MouseEvent e){
        ImagePlus impl=CommonGuiMethods.getSourceImage(e);
        boolean needUpdate=false;
        if(impl==null) {
            impl=m_cIPOGTPW.pw.getImagePlus();
            if(impl.getWindow().getCanvas()!=e.getSource())return -1;
        }
        Point cursor;
        if(impl==m_cIPOGTPW.pw.getImagePlus()){
            IPOGTLevelNode lNode;
            cursor=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
            String plotValue=m_cIPOGTPW.pw.getPoingCoordinate(cursor.x, cursor.y);
            StringTokenizer stk=new StringTokenizer(plotValue,"X,Y= ");
            if(stk.countTokens()!=2) return -1;
            double x=Double.parseDouble(stk.nextToken()),y=Double.parseDouble(stk.nextToken());
            int slice=(int)(x+0.5);

            Roi roi=m_cIPOGTPW.pw.getImagePlus().getRoi();
            if(CreateTransitionRB.isSelected()) {
                createLevelTransition(slice);
                needUpdate=true;
            }
            if(RemoveTransitionRB.isSelected()) {
                if(roi==null){
                    removeLevelTransition(slice);
                    needUpdate=true;
                }else{
                    DoubleRange xRange=new DoubleRange(), yRange=new DoubleRange();
                    CommonGuiMethods.getCoordinateRanges(m_cIPOGTPW.pw, roi, xRange, yRange);
                    ArrayList<Integer> nvSlices=m_cIPOGTPW.IPOGT.m_cLevelInfo.getTransitionSlices(xRange);
                    int i,len=nvSlices.size();
                    for(i=0;i<len;i++){
                        removeLevelTransition(nvSlices.get(i));
                    }
                    needUpdate=true;
                }
            }
            if(needUpdate) displayTransitions();
            if(AdjustLevelCB.isSelected()){
                adjustLevel(cursor);
            }
            if(ShowSegmentRB.isSelected()&&!e.isControlDown()) 
                showIPOGTFitting(cursor);
        }
        return 1;
    }
    int removeLevelTransition(int slice){
        IPOGTLevelNode lNode=m_cIPOGT.m_cLevelInfo.removeLevelTransition(slice);
        if(lNode==null) return -1;
        buildLevelIPOG(lNode,pixelsCompenStack,pixelsScratch,pnScratch);
        RemoveTransitionRB.setSelected(false);
        if(ShowLevelCB.isSelected())
            updateViews();
        else
            displayTransitions();
        return 1;
    }
    int createLevelTransition(int slice){
        ArrayList<IPOGTLevelNode> lNodes=m_cIPOGT.m_cLevelInfo.createLevelTransition(slice);
        if(lNodes==null) return -1;
        CreateTransitionRB.setSelected(false);
        for(int i=0;i<lNodes.size();i++){
            buildLevelIPOG(lNodes.get(i),pixelsCompenStack,pixelsScratch,pnScratch);
        }
        if(ShowLevelCB.isSelected())
            updateViews();
        else
            displayTransitions();
        return 1;
    }
    void fireActionEvent(ActionEvent ae){
        int i,len=m_cvActionListeners.size();
        for(i=0;i<len;i++){
            m_cvActionListeners.get(i).actionPerformed(ae);
        }
    }
    public void addActionListener(ActionListener al){
        m_cvActionListeners.add(al);
    }
    public static JTable buildCommonTable(ArrayList<IPOGaussianNode> cvIPOGs, boolean simpleIPOGs){
        if(cvIPOGs==null) return null;
        String[][] ppsPars=IPOGaussianNodeHandler.getIPOsAsStrings(cvIPOGs,simpleIPOGs);
        String[] columnHead=ppsPars[0];
        return buildCommonTable(columnHead,ppsPars,0,ppsPars.length-1);
    }
    public static JTable buildCommonTable(ArrayList<IPOGaussianNodeComplex> cvIPOGs){
        if(cvIPOGs==null) return null;
        String[][] ppsPars=IPOGaussianNodeHandler.getIPOsAsStringsComplex(cvIPOGs);
        String[] columnHead=ppsPars[0];
        return buildCommonTable(columnHead,ppsPars,0,ppsPars.length-1);
    }
    public static JViewport buildCommonTableView(ArrayList<IPOGaussianNode> cvIPOGs, boolean simpleIPOGs){
        if(cvIPOGs==null) return null;
        String[][] ppsPars;
        if(simpleIPOGs)
            ppsPars=IPOGaussianNodeHandler.getIPOsAsStrings(cvIPOGs,simpleIPOGs);
        else{
            ArrayList<IPOGaussianNodeComplex> cvIPOGst=new ArrayList();
            for(int i=0;i<cvIPOGs.size();i++){
                cvIPOGst.add((IPOGaussianNodeComplex)cvIPOGs.get(i));
            }
            ppsPars=IPOGaussianNodeHandler.getIPOsAsStringsComplex(cvIPOGst);
        }
        String[] columnHead=ppsPars[0];
        return buildCommonTableView(columnHead,ppsPars,1,ppsPars.length-1);
    }

    public int displayIPOTable(ArrayList<IPOGaussianNode> cvIPOGs, boolean simpleIPOGs){
        if(cvIPOGs==null) return -1;
        ArrayList<String> parNames=new ArrayList(), parValues=new ArrayList();
        String[][] ppsPars;
        if(simpleIPOGs)
            ppsPars=IPOGaussianNodeHandler.getIPOsAsStrings(cvIPOGs,simpleIPOGs);
        else{
            ArrayList<IPOGaussianNodeComplex> cvIPOGst=new ArrayList();
            for(int i=0;i<cvIPOGs.size();i++){
                cvIPOGst.add((IPOGaussianNodeComplex)cvIPOGs.get(i));
            }
            ppsPars=IPOGaussianNodeHandler.getIPOsAsStringsComplex(cvIPOGst);
        }
        String[] columnHead=ppsPars[0];
        int i,j,rows=ppsPars.length-1;
        Object[][] poData=new Object[rows][];

        for(i=0;i<rows;i++){
            poData[i]=ppsPars[i+1];
        }

        JViewport cTable1Viewport=new JViewport();
        JTable cIPOGTable=new JTable(poData,columnHead);
//        ComponentBackgroundMaker cc;
//        cIPOGTable.getModel().addTableModelListener(this);
        CommonGuiMethods.setTableCellAlignmentH(cIPOGTable, JLabel.RIGHT);
        cIPOGTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        cIPOGTable=CommonGuiMethods.autoResizeColWidth(cIPOGTable, new javax.swing.table.DefaultTableModel());
//        if(cIPOGColors!=null){
//            cc=new ComponentBackgroundMaker(m_cSelectedIPOTable,1,m_cvSelectedIPOColors);
//        }

        cTable1Viewport.setView(cIPOGTable);
        IPOGTableSP.setViewport(cTable1Viewport);
        return 1;
    }
    public static JTable buildCommonTable(String[] columnHead, String[][] psData,int rowI, int rowF){
        int rows=rowF-rowI+1,i;
        Object[][] poData=new Object[rows][];

        for(i=rowI;i<=rowF;i++){
            poData[i-rowI]=psData[i];
        }

        JTable cTable=new JTable(poData,columnHead);
        CommonGuiMethods.setTableCellAlignmentH(cTable, JLabel.RIGHT);
        cTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        cTable=CommonGuiMethods.autoResizeColWidth(cTable, new javax.swing.table.DefaultTableModel());
        return cTable;
    }
    
    public static JViewport buildCommonTableView(String[] columnHead, String[][] psData,int rowI, int rowF){
        return buildCommonTableView(buildCommonTable(columnHead,psData,rowI,rowF));
    }
    
    public static JViewport buildCommonTableView(JTable table){
        JViewport cTableViewport=new JViewport();
        cTableViewport.setView(table);
        return cTableViewport;
    }

    
    JViewport buildTrackInfoTableView(){
        ArrayList<String> names=new ArrayList(), svValues=new ArrayList();
        ArrayList<Double> values=new ArrayList();
        m_cIPOGT.getTrackInfo(names, values, svValues);
        int len=names.size(),i;
        String[] psNames=new String[len];
        String[][] psPars=new String[1][len];
        for(i=0;i<len;i++){
            psNames[i]=names.get(i);
            psPars[0][i]=svValues.get(i);
        }
        return buildCommonTableView(psNames,psPars,0,0);
    }
    void updateIPOGTableView(String selection){
        if(selection.contentEquals("Status")) showStatus();
        if(selection.contentEquals("Track Info")) IPOGTableSP.setViewport(m_cTrackInfoViewport);
        if(selection.contentEquals("Track IPOGs")) IPOGTableSP.setViewport(m_cTrackIPOViewport);
        if(selection.contentEquals("Level IPOGs")) IPOGTableSP.setViewport(m_cLevelIPOViewport);
        if(selection.contentEquals("Track IPOGs Detailed")) IPOGTableSP.setViewport(m_cTrackIPOViewportDetailed);
        if(selection.contentEquals("Level Info_M")) IPOGTableSP.setViewport(m_cLevelInfoViewport_M);
        if(selection.contentEquals("Level Info_A")) IPOGTableSP.setViewport(m_cLevelInfoViewport_A);
        if(selection.contentEquals("Curve Evaluations")) {
            m_cCurveEvaluationViewport=buildCurveEvaluationView();
            IPOGTableSP.setViewport(m_cCurveEvaluationViewport);
        }
    }
/*    public void calLevelInfo(){//need to fix it anyway
        ArrayList<Double> dvStepSizes=new ArrayList();
        int i,len=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M.size(),j,nDelta=m_nMaxRisingInterval;

        IPOGTLevelNode lNode0=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M.get(0),lNode;
        IPOGTLevelInfoNode.calLevelInfo(m_cIPOGT,lNode0,m_sPlottingOption);

        IPOGaussianNode IPOG0, IPOG;
        for(j=lNode0.indexI;j<lNode0.indexF;j++){
            IPOG=m_cIPOGT.getIPOG(j);
            IPOG0=m_cIPOGT.getIPOG(j+nDelta);
            if(IPOG==null||IPOG0==null) continue;
            dvStepSizes.add(Math.abs(IPOG.getValue(m_sPlottingOption)-IPOG0.getValue(m_sPlottingOption)));
        }
        lNode0.cStepSizeHist=new Histogram(dvStepSizes);

        for(i=1;i<len;i++){
            dvStepSizes=new ArrayList();
            lNode=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M.get(i);
            IPOGTLevelInfoNode.calLevelInfo(m_cIPOGT,lNode,m_sPlottingOption);
            lNode0.delta=lNode0.dR-lNode.dL;
            lNode0.dRatio=lNode0.dR/lNode0.delta;
            lNode0=lNode;
            for(j=lNode.indexI;j<lNode.indexF;j++){
                IPOG=m_cIPOGT.getIPOG(j);
                IPOG0=m_cIPOGT.getIPOG(j+nDelta);
                if(IPOG==null||IPOG0==null) continue;
                dvStepSizes.add(Math.abs(IPOG.getValue(m_sPlottingOption)-IPOG0.getValue(m_sPlottingOption)));
            }
            lNode.cStepSizeHist=new Histogram(dvStepSizes);
        }
        lNode=m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M.get(len-1);
        lNode0.delta=lNode0.dR;
        lNode0.dRatio=1;
//        m_cIPOGT.setLevelComplexity(pixelsStack,pixelsScratch,pnScratch);
    }*/
    JViewport buildLevelInfoViewport_M(){
        String psData[][]=IPOGTLevelInfoNode.getLevelInfoAsStringArray(m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_M);
        m_cLevelInfoTable_M=buildCommonTable(psData[0],psData,1,psData.length-1);
        m_cLevelInfoTable_M.getModel().addTableModelListener(this);
        JViewport jvp=new JViewport();
        jvp.setView(m_cLevelInfoTable_M);
        return jvp;
    }
    JViewport buildLevelInfoViewport_A(){
        String psData[][]=IPOGTLevelInfoNode.getLevelInfoAsStringArray(m_cIPOGT.m_cLevelInfo.m_cvLevelNodes_A);
        m_cLevelInfoTable_A=buildCommonTable(psData[0],psData,1,psData.length-1);
        m_cLevelInfoTable_A.getModel().addTableModelListener(this);
        JViewport jvp=new JViewport();
        jvp.setView(m_cLevelInfoTable_A);
        return jvp;
    }
    JViewport buildCurveEvaluationView(){
        String psData[][];
        int i,lena,lenm,len;
        if(m_cIPOGTPW.IPOGT.m_cLevelInfo!=null){
            ArrayList<String> sva=m_cIPOGTPW.IPOGT.m_cLevelInfo.svCurveEvaluations_Automated;
            ArrayList<String> svm=m_cIPOGTPW.IPOGT.m_cLevelInfo.svCurveEvaluations_Manual;
            lena=sva.size();
            lenm=svm.size();
            len=Math.max(lena, lenm);
            psData=new String[len+1][2];
            psData[0][0]="Automatic Evaluations: ";
            psData[0][1]="Manual Evaluations: ";
            if(m_cIPOGTPW.IPOGT.m_cLevelInfo.verifiable(sva))
                psData[0][0]+="Verified";
            else
                psData[0][0]+="Excluded";
            
            psData[0][1]+=m_cIPOGTPW.IPOGT.m_cLevelInfo.sInfoNodeStatus_Manual;
                        
            for(i=0;i<len;i++){
                if(i<lena) 
                    psData[i+1][0]=sva.get(i);
                else
                    psData[i+1][0]="";
                if(i<lenm) 
                    psData[i+1][1]=svm.get(i);
                else
                    psData[i+1][1]="";
            }
        }else{
            psData=new String[2][1];
            psData[0][0]="Cureve Evaluations";
            psData[1][0]="Note Evaluated";
            len=1;
        }
        JViewport jvp=new JViewport();
        jvp.setView(CommonGuiMethods.buildCommonTable(psData[0], psData, 1, len));
        return jvp;
    }
    void confirmAllIPOGShapes(){
        
    }
    int getIPOGIndex(int row){
        int index=0,iI,iI0=-1,iF,iF0=-1,i;
        String sI,sF,st;
        for(i=1;i<=row;i++){
            sI=((String)m_cLevelIPOGTable.getValueAt(i, 6)).trim();
            sF=((String)m_cLevelIPOGTable.getValueAt(i, 7)).trim();
            iI=Integer.parseInt(sI);
            iF=Integer.parseInt(sF);
            if(iI!=iI0||iF!=iF0){
                iI0=iI;
                iF0=iF;
                index++;
            }
        }
        return index-1;
    }
    int handleTableChange(TableModelEvent te){
        Object o=te.getSource();
        int col=te.getColumn(),index;
        
        String CellValue;

        IPOGaussianNode IPO=null;
        String sI,sF;

        int fr=te.getFirstRow();

        if(o==m_cLevelInfoTable_M.getModel()){
            CellValue=(String)m_cLevelInfoTable_M.getValueAt(fr,col);
            m_cIPOGT.m_cLevelInfo.update(fr, col, CellValue);
        }
        if(o==m_cLevelIPOGTable.getModel()){
            if(fr<1) return -1;//the first row is a description line
            String title=(String)m_cLevelIPOGTable.getValueAt(0, col);
            if(title.contentEquals("IPOG")){
                index=getIPOGIndex(fr);
                m_cIPOGT.m_cLevelInfo.updateLevelIPOGShape(index);
                return 1;
            }
            if(title.contentEquals("Contour")){
                index=getIPOGIndex(fr);
                m_cIPOGT.m_cLevelInfo.updateLevelIPOGContour(index);
                return 1;
            }
            if(title.contentEquals("RawContour")){
                index=getIPOGIndex(fr);
                m_cIPOGT.m_cLevelInfo.updateLevelIPOGShapeRaw(index);
                return 1;
            }
            if(title.contentEquals("ConfirmIPOG")){
                ArrayList<String> shapes=new ArrayList();
                int i,len=m_cLevelIPOGTable.getRowCount(),iI=-1,iF=-1,iI0=-1,iF0=-1;
                String st;
                for(i=1;i<len;i++){
                    sI=((String)m_cLevelIPOGTable.getValueAt(i, 6)).trim();
                    sF=((String)m_cLevelIPOGTable.getValueAt(i, 7)).trim();
                    iI=Integer.parseInt(sI);
                    iF=Integer.parseInt(sF);
                    if(iI!=iI0||iF!=iF0){
                        st=((String)m_cLevelIPOGTable.getValueAt(i, col-1)).trim();
                        shapes.add(st);
                        iI0=iI;
                        iF0=iF;
                    }
                }
                m_cIPOGT.m_cLevelInfo.updateLevelIPOGShapeConfirmation(shapes);
                return 1;
            }
            if(title.contentEquals("ConfirmContour")){
                ArrayList<String> shapes=new ArrayList();
                int i,len=m_cLevelIPOGTable.getRowCount(),iI=-1,iF=-1,iI0=-1,iF0=-1;
                String st;
                for(i=1;i<len;i++){
                    sI=((String)m_cLevelIPOGTable.getValueAt(i, 6)).trim();
                    sF=((String)m_cLevelIPOGTable.getValueAt(i, 7)).trim();
                    iI=Integer.parseInt(sI);
                    iF=Integer.parseInt(sF);
                    if(iI!=iI0||iF!=iF0){
                        st=((String)m_cLevelIPOGTable.getValueAt(i, col-1)).trim();
                        shapes.add(st);
                        iI0=iI;
                        iF0=iF;
                    }
                }
                m_cIPOGT.m_cLevelInfo.updateLevelIPOGContourConfirmation(shapes);
                return 1;
            }
            if(title.contentEquals("ConfirmRawShape")){
                ArrayList<String> shapes=new ArrayList();
                int i,len=m_cLevelIPOGTable.getRowCount(),iI=-1,iF=-1,iI0=-1,iF0=-1;
                String st;
                for(i=1;i<len;i++){
                    sI=((String)m_cLevelIPOGTable.getValueAt(i, 6)).trim();
                    sF=((String)m_cLevelIPOGTable.getValueAt(i, 7)).trim();
                    iI=Integer.parseInt(sI);
                    iF=Integer.parseInt(sF);
                    if(iI!=iI0||iF!=iF0){
                        st=((String)m_cLevelIPOGTable.getValueAt(i, col-1)).trim();
                        shapes.add(st);
                        iI0=iI;
                        iF0=iF;
                    }
                }
                m_cIPOGT.m_cLevelInfo.updateLevelIPOGShapeConfirmationRaw(shapes);
                return 1;
            }
            if(title.contentEquals("sliceI")){
                sI=(String)m_cLevelIPOGTable.getValueAt(fr, 6);
                sI=sI.trim();
                updateTrackImage(Integer.parseInt(sI));
                m_cIPOGTPW.pw.resetCursor(Double.parseDouble(sI));
                return 1;
            }
            if(title.contentEquals("row")){
                sI=(String)m_cLevelIPOGTable.getValueAt(fr, 6);
                sF=(String)m_cLevelIPOGTable.getValueAt(fr, 7);
                sI=sI.trim();
                sF=sF.trim();
                int sliceI=Integer.parseInt(sI),sliceF=Integer.parseInt(sF);
                m_cIPOAnalyzer.showTrackAveIPO(m_cIPOGT, sliceI, sliceF);
            }
        }
        return 1;
    }
    public void tableChanged(TableModelEvent te){
        handleTableChange(te);
        updateViews();
    }
    ArrayList<Point> pointsForLevelAdjust;
    int adjustLevel(Point cursor){
        if(pointsForLevelAdjust==null) pointsForLevelAdjust=new ArrayList();
        if(pointsForLevelAdjust.isEmpty()) {
            pointsForLevelAdjust.add(cursor);
            return 1;
        }
        pointsForLevelAdjust.add(cursor);
        AdjustLevelCB.setSelected(false);

        double x1,y1,x2,y2;
        Point p1=pointsForLevelAdjust.get(0),p2=pointsForLevelAdjust.get(1);
        pointsForLevelAdjust.clear();
        String plotValue=m_cIPOGTPW.pw.getPoingCoordinate(p1.x, p1.y);
        StringTokenizer stk=new StringTokenizer(plotValue,"X,Y= ");
        if(stk.countTokens()!=2) return -1;
        x1=Double.parseDouble(stk.nextToken());
        y1=Double.parseDouble(stk.nextToken());

        plotValue=m_cIPOGTPW.pw.getPoingCoordinate(p2.x, p2.y);
        stk=new StringTokenizer(plotValue,"X,Y= ");
        if(stk.countTokens()!=2) return -1;
        x2=Double.parseDouble(stk.nextToken());
        y2=Double.parseDouble(stk.nextToken());
        m_cIPOGT.m_cLevelInfo.adjustLevel(x1,y1,x2,y2);
        m_cLevelInfoViewport_M=buildLevelInfoViewport_M();
        m_cLevelInfoViewport_A=buildLevelInfoViewport_A();
        updateIPOGTableView((String)IPOTableViewSelectionCB.getSelectedItem());
        return 1;
    }
    void exportEnvelopes(){
        ArrayList <Double> dvX=new ArrayList(), dvY=new ArrayList();
        getTrackData(dvX,dvY);
        int len=dvX.size(),i,j;
        int size=(int)(dvX.get(len-1)-dvX.get(0)+1.5);
        double[] pdX=new double[size],pdY=new double[size];
        int x0,x,index=0;
        double y0,y,ym;
        x0=(int)(dvX.get(0)+0.5);
        y0=dvY.get(0);
        pdX[0]=x0;
        pdY[0]=y0;
        for(i=1;i<len;i++){
            x=(int)(dvX.get(i)+0.5);
            y=dvY.get(i);
            ym=0.5*(y0+y);
            for(j=x0+1;j<x;j++){
                index++;
                pdX[index]=pdX[index-1]+1;
                pdY[index]=ym;
            }
            index++;
            pdX[index]=x;
            pdY[index]=y;
            x0=x;
            y0=y;
        }

        double[] pdHiLine=new double[size];
        double[] pdLoLine=new double[size];

        int nRanking=0,nWS=20;
        LineEnveloper lenv=new LineEnveloper(pdY,nWS,nRanking,pdLoLine,pdHiLine);

        String[] ColumnHead={"Slice","LoLine","Y","HiLine"};
        String[][] psData=new String[size][4];
        for(i=0;i<size;i++){
            psData[i][0]=PrintAssist.ToString(pdX[i],0);
            psData[i][1]=PrintAssist.ToString(pdLoLine[i],1);
            psData[i][2]=PrintAssist.ToString(pdY[i],2);
            psData[i][3]=PrintAssist.ToString(pdHiLine[i],3);
        }

        if(m_cTableFrame==null)
            m_cTableFrame=new TableFrame("Regression Lines",ColumnHead,psData,0,len-1);
        else
            m_cTableFrame.update("Evn Line RW", ColumnHead, psData, 0, psData.length-1);
    }
    void exportSRLines(){//need to fix it to reflect the position dependent rising nMaxRisingInterval 12316
        ArrayList <Double> dvX=new ArrayList(), dvY=new ArrayList();
        getTrackData(dvX,dvY);
        dvX.remove(0);
        dvY.remove(0);
        int nWS=10;
        RunningWindowRegressionLiner RWSRL=new RunningWindowRegressionLiner(dvX,dvY,nWS,0);
        RunningWindowRegressionLiner RWSRR=new RunningWindowRegressionLiner(dvX,dvY,0,nWS);

        int len=dvX.size(),i,j;
        String[] ColumnHead={"Slice","Left","Y","Right","Delta"};
        String[][] psData=new String[len][5];
        for(i=0;i<len;i++){
            psData[i][0]=PrintAssist.ToString(dvX.get(i),0);
            psData[i][1]=PrintAssist.ToString(RWSRL.predict(i),1);
            psData[i][2]=PrintAssist.ToString(dvY.get(i),1);
            psData[i][3]=PrintAssist.ToString(RWSRR.predict(i),1);
            psData[i][4]=PrintAssist.ToString(RWSRL.predict(i)-RWSRR.predict(Math.min(len-1,i+m_nMaxRisingInterval),dvX.get(Math.min(len-1,i+m_nMaxRisingInterval))),2);
        }

        if(m_cTableFrame==null)
            m_cTableFrame=new TableFrame("Regression Lines",ColumnHead,psData,0,len-1);
        else
            m_cTableFrame.update("Evn Line RW", ColumnHead, psData, 0, psData.length-1);
    }
    void outputValuesAsTable(){
        String option=(String)(OutputOptionCB.getSelectedItem());
        if(option.contentEquals("Env Lines")){
            exportEnvelopes();
        }
        if(option.contentEquals("SR Lines")){
            exportSRLines();
        }
        if(option.contentEquals("Linear Seg Lengths")){
            exportLinearSegLength();
        }
        if(option.contentEquals("RWA")){
            exportRWA();
        }
        if(option.contentEquals("Median Reflection")){
            exportMedianReflection();
        }
    }
    StraightlineFitter m_cStraitLineFitter;
    void exportLinearSegLength(){
        int order=Integer.parseInt((String)RegressionOrderCB.getSelectedItem());
        double pValue=Double.parseDouble(PVDeltaTF.getText());
        m_cIPOGTPW.exportLinearSegLength(m_cTableFrame, order, pValue);
    }
    void exportRWA(){//need to fix it to reflect the position dependent rising nMaxRisingInterval 12316
        ArrayList <Double> dvX=new ArrayList(), dvY=new ArrayList();
        getTrackData(dvX,dvY);
        dvX.remove(0);
        dvY.remove(0);
        int nWS=10;
        double pdRWAL[]=CommonStatisticsMethods.getRunningWindowAverage(dvX, dvY, 0, dvX.size()-1, nWS, 0);
        double pdRWAR[]=CommonStatisticsMethods.getRunningWindowAverage(dvX, dvY, 0, dvX.size()-1, 0, nWS);

        int len=dvX.size(),i,j;
        String[] ColumnHead={"Slice","Left","Y","Right","Delta"};
        String[][] psData=new String[len][5];
        for(i=0;i<len;i++){
            psData[i][0]=PrintAssist.ToString(dvX.get(i),0);
            psData[i][1]=PrintAssist.ToString(pdRWAL[i],1);
            psData[i][2]=PrintAssist.ToString(dvY.get(i),1);
            psData[i][3]=PrintAssist.ToString(pdRWAR[i],1);
            psData[i][4]=PrintAssist.ToString(pdRWAL[i]-pdRWAR[Math.min(len-1,i+m_nMaxRisingInterval)],2);
        }

        if(m_cTableFrame==null)
            m_cTableFrame=new TableFrame("Regression Lines",ColumnHead,psData,0,len-1);
        else
            m_cTableFrame.update("Evn Line RW", ColumnHead, psData, 0, psData.length-1);
    }

    void exportMedianReflection(){//need to fix it to reflect the position dependent rising nMaxRisingInterval 12316
        ArrayList <Double> dvX=new ArrayList(), dvY=new ArrayList();
        getTrackData(dvX,dvY);
        dvX.remove(0);
        dvY.remove(0);
        int len=dvX.size(),i,j;
        double pdDelta[]=new double[len-m_nMaxRisingInterval];
        double pdDeltaMR[]=new double[len-m_nMaxRisingInterval];
        for(i=0;i<len-m_nMaxRisingInterval;i++){
            pdDelta[i]=dvY.get(i)-dvY.get(i+m_nMaxRisingInterval);
            pdDeltaMR[i]=dvY.get(i)-dvY.get(i+m_nMaxRisingInterval);
        }

        CommonStatisticsMethods.makeMedianReflection(pdDeltaMR);
        String[] ColumnHead={"Slice","Delta","Median Reflection"};
        String[][] psData=new String[len-m_nMaxRisingInterval][ColumnHead.length];
        for(i=0;i<len-m_nMaxRisingInterval;i++){
            psData[i][0]=PrintAssist.ToString(dvX.get(i),0);
            psData[i][1]=PrintAssist.ToString(pdDelta[i],1);
            psData[i][2]=PrintAssist.ToString(pdDeltaMR[i],1);
        }

        if(m_cTableFrame==null)
            m_cTableFrame=new TableFrame("Median Reflection",ColumnHead,psData,0,len-1-m_nMaxRisingInterval);
        else
            m_cTableFrame.update("Median Reflection", ColumnHead, psData, 0, psData.length-1-m_nMaxRisingInterval);
    }
    int showSmoothRegions(){
        int position=0,len=pdX.length;
        intRange ir;
        while(position<len){
            ir=showFirstSmoothRegion(position);
            if(ir==null) {
                position++;
                continue;
            }
            position=ir.getMax()+1;
        }
        return 1;
    }
    intRange showFirstSmoothRegion(int position){
        intRange ir=showSmoothRegion(position);
        int len=pdX.length;
        while(ir==null){
            position++;
            if(position>=len) break;
            ir=showSmoothRegion(position);
        }
        return ir;
    }
    intRange showSmoothRegion(int index){
        if(m_cTrackFitter==null) return null;
        String sc=(String)PlotColorCB.getSelectedItem();
        Color c=CommonGuiMethods.getColorSelection(sc);
        intRange ir=m_cTrackFitter.getSmoothRange(index);
        if(ir.emptyRange()) return null;
        int len=ir.getRange(),i,in=ir.getMin();
        double [] pdXT=new double[len],pdYT=new double[len];
        for(i=ir.getMin();i<=ir.getMax();i++){
            pdXT[i-in]=pdX[i];
            pdYT[i-in]=pdY[i];
        }
        m_cIPOGTPW.pw.addPlot("SmoothRegion"+index, pdXT, pdYT, 2, 2,c,true);
        return ir;
    }
    int showIPOGTFitting(Point cursor){
        String Option=(String)SegmentOptionCB.getSelectedItem();
        if(Option.contentEquals("Clear")) {
            m_cIPOGTPW.pw.removePlotGroup("FittingLine");
            m_cIPOGTPW.pw.removePlotGroup("Transition");
            m_cIPOGTPW.pw.removePlotGroup("Optimal");
            m_cIPOGTPW.pw.removePlotGroup("Merge");
            m_cIPOGTPW.pw.removePlotGroup("Seg");
            m_cIPOGTPW.pw.removePlotGroup("SmoothRegion");
            m_cIPOGTPW.pw.removePlotGroup("SmoothSeg");
            return 1;
        }
        LineFeatureExtracter2 cFE;

        String plotValue=m_cIPOGTPW.pw.getPoingCoordinate(cursor.x, cursor.y);
        StringTokenizer stk=new StringTokenizer(plotValue,"X,Y= ");
        double x=Double.parseDouble(stk.nextToken());
        double y=Double.parseDouble(stk.nextToken());
        int index=m_cIPOGTPW.getXIndex(x),nRisingInterval;

        if(index<0){
            return -1;
        }
        String title1=null,title2=null;
        Color c1=null,c2=null;
        int lw=2;
        int shape=PlotWindow.LINE;

        DoubleRange dR1,dR2;
        PolynomialLineFittingSegmentNode sr1=null,sr2=null;
        PolynomialLineFittingSegmentNode[] pcSegments;
        PolynomialLineFitter cTrackFitter=m_cIPOGTPW.getTrackFitter();
        
        String sX=PrintAssist.ToString(x, 0);
        int[] pnRisingIntervals=null;
        int position=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(m_cIPOGTPW.pdX, x, 1);
        if(cTrackFitter!=null) pnRisingIntervals=cTrackFitter.getRisingIntervals();
        if(Option.contentEquals("Optimal")){
            if(cTrackFitter==null) return -1;
            title1="FittingLine"+"_Optimal";
            pcSegments=cTrackFitter.getOptimalRegressions();
            c1=Color.BLACK;
            sr1=pcSegments[index];
            sr2=null;
        }else if(Option.contentEquals("Starting")){
            c1=Color.BLUE;
            c2=Color.RED;
            title1="FittingLine"+"_Starting";
            if(cTrackFitter==null){
                PolynomialRegression cPr=CommonStatisticsMethods.getOptimalStartingRegression(m_cIPOGTPW.pdX, m_cIPOGTPW.pdY,m_cIPOGTPW.IPOGT.m_cLevelInfo.pbSelected, position, 5, 30, -1);
                m_cIPOGTPW.pw.displayRegression(title1+sX,cPr,cPr.getXFittingRange(),lw,shape,c1);
                return 1;
            }
            pcSegments=cTrackFitter.getStartingRegressions();
            sr1=pcSegments[index];
            pcSegments=cTrackFitter.getEndingRegressions();
            nRisingInterval=CommonStatisticsMethods.getRisingInterval(pnRisingIntervals, index,-1);
            if(index>pnRisingIntervals[index]){
                index+=nRisingInterval;
                sr2=pcSegments[index];
            }
            title2="FittingLine"+"_Ending";
        }else if(Option.contentEquals("Ending")){
            title1="FittingLine"+"_Ending";
            title2="FittingLine"+"_Starting";
            if(cTrackFitter==null){
                PolynomialRegression cPr=CommonStatisticsMethods.getOptimalEndingRegression(m_cIPOGTPW.pdX, m_cIPOGTPW.pdY,m_cIPOGTPW.IPOGT.m_cLevelInfo.pbSelected, position, 5, 30, -1);
                m_cIPOGTPW.pw.displayRegression(title1+sX,cPr,cPr.getXFittingRange(),lw,shape,c1);
                return 1;
            }
            c1=Color.RED;
            c2=Color.BLUE;
            pcSegments=cTrackFitter.getEndingRegressions();
            sr1=pcSegments[index];
            pcSegments=cTrackFitter.getStartingRegressions();
            if(index<pcSegments.length-m_nMaxRisingInterval){
                index+=pnRisingIntervals[index];
                sr2=pcSegments[index];
            }
        }else if(Option.contentEquals("Long")){
            if(cTrackFitter==null) return -1;
            c1=Color.blue;
            title1="FittingLine"+"_Long";
            pcSegments=cTrackFitter.getLongRegressions();
            sr1=pcSegments[index];
        }else if (Option.contentEquals("Smooth")){
            showSmoothRegion(index);
        } else if (Option.contentEquals("SmoothSeg")){
            title1="SmoothSeg";
            cFE=m_cIPOGTPW.pw.getFeatureExtracter();
            if(cFE==null) {
                sr1=null;
                sr2=null;
            }else{
                c1=Color.ORANGE;
                pcSegments=cFE.getSmoothSegments();
                if(index<pcSegments.length)sr1=pcSegments[index];
                if(sr1!=null){
                    if(sr1.getStart(0, 1)==50&&sr1.getStart(0, -1)==62){
                        sr1=sr1;
                    }
                    if(!sr1.isSmooth(position,(double)m_nMaxRisingInterval))
                        c1=Color.MAGENTA;
                }
            }
        }

        
        if(sr1!=null){
            dR1=sr1.getFittingRange();
            m_cIPOGTPW.pw.displayRegression(title1+sX,sr1.getRegressionNode(),dR1,lw,shape,c1);
            sr1.showDeviation();
            sr1.getRegressionNode().showRWASignificance();
//            if(sr1.getRegressionNode().getModel()!=null)displayModelEvaluation(sr1.getRegressionNode().getModel(),title1+sX);
            displaySegmentPars(sr1);
        }
        if(sr2!=null){
            AppendTableCB.setSelected(true);
            dR2=sr2.getFittingRange();
            m_cIPOGTPW.pw.displayRegression(title2+sX,sr2.getRegressionNode(),dR2,lw,shape,c2);
//            if(sr1.getRegressionNode().getModel()!=null)displayModelEvaluation(sr2.getRegressionNode().getModel(),title2+sX);
            AppendTableCB.setSelected(true);
            displaySegmentPars(sr2);
            sr2.getRegressionNode().showRWASignificance();
            sr2.showDeviation();
        }
        return 1;
    }
    HighlightingRoiCollectionNode m_cLineSegmentHightlights;
    public int displayPlot(String option){
        SignalTransitionDetector cDetector=m_cIPOGTPW.getSignalTransitionDetector();
        if(cDetector==null) return -1;
        PlotWindowPlus pw=null;
        if(option.contentEquals("Optimal LS")){
            option="OptimalRegressionLine";
            pw=m_cIPOGTPW.pw;
        } else if(option.contentEquals("Optimal Segs")){
            option="OptimalSegments";
            pw=m_cIPOGTPW.pw;
        } else if(option.contentEquals("Merged Segs")){
            option="MergedSegments";
            pw=m_cIPOGTPW.pw;
        }else if(option.contentEquals("Starting LS")){
            option="OptimalStartingRegressionLine";
            pw=m_cIPOGTPW.pw;
        }else if(option.contentEquals("Ending LS")) {
            option="OptimalEndingRegressionLine";
            pw=m_cIPOGTPW.pw;
        }else if(option.contentEquals("Long LS")) {
            option="OptimalLongRegressionLine";
            pw=m_cIPOGTPW.pw;
        }else if(option.contentEquals("DeltaO")){
            option="OptimalRegressionLineDelta";
        }else if(option.contentEquals("DeltaLR")){
            option="OptimalRegressionLineDeltaLR";
        }else if(option.contentEquals("Transitions")){
            cDetector.displayTransitions(m_cIPOGTPW.pw, Double.parseDouble(TransitionPValueTF.getText()));
            return 1;
        }else if(option.contentEquals("Sig of Trans")){
            cDetector.displayDifferenceSignificance(pw);
            return 1;
        }
        if(OveridePlotParsCB.isSelected())
            cDetector.drawOptimalRegressionLine(pw, option,Integer.parseInt(LineWidthTF.getText()),Integer.parseInt(PlotShapeTF.getText()),CommonGuiMethods.getColorSelection((String)PlotColorCB.getSelectedItem()));
        else
            cDetector.drawOptimalRegressionLine(pw, option);
        return 1;
    }
    int zoomOut(){
        ImagePlus impl=CommonMethods.getCurrentImage();
        Window wd=impl.getWindow();
        if(!(wd instanceof PlotWindowPlus)) return -1;
        PlotWindowPlus pwp=(PlotWindowPlus) wd;
        pwp.zoomOut();
        return 1;
    }
    int zoomIn(){
        ImagePlus impl=CommonMethods.getCurrentImage();
        Window wd=impl.getWindow();
        if(!(wd instanceof PlotWindowPlus)) return -1;
        PlotWindowPlus pwp=(PlotWindowPlus) wd;
        pwp.zoomIn();
        return 1;
    }
    public int getRegressionOrder(){
        return Integer.parseInt((String)RegressionOrderCB.getSelectedItem());
    }
    public int getRegressionModes(){
        int nModes=1;
        String st=(String)RegressionModesCB.getSelectedItem();
        if(st.contentEquals("central"))
            nModes=FittingModelNode.CentralMode;
        else               
            nModes=Integer.parseInt(st);

        return nModes;
    }
    void showRegression(){
        PolynomialLineFittingSegmentNode seg=m_cIPOGTPW.showRegression(getRegressionOrder(), getRegressionModes(),Double.parseDouble(PVChiSQTF.getText()),Double.parseDouble(PVTiltingTF.getText()),Double.parseDouble(PSidenessTF.getText()),Double.parseDouble(PVPWDevTF.getText()),3,0,Double.parseDouble(PVTerminalsTF.getText()),Double.parseDouble(OutliarRatioTF.getText()),(String)SDChoiceCB.getSelectedItem());
        displaySegmentPars(seg);
    }
    void displaySegmentPars(PolynomialLineFittingSegmentNode seg){
        String title="Regression";
        m_cPr=seg.getRegressionNode();
        int wsTilting=3,wsPWDev=0;
        String[][] psData=LineSegmentRegressionEvaluater.getRegressionResultsAsStringArray(title, seg);
        if(m_psModelEvaluationTableData==null||!AppendTableCB.isSelected())
            m_psModelEvaluationTableData=psData;
        else
            m_psModelEvaluationTableData=CommonStatisticsMethods.addRow(m_psModelEvaluationTableData,psData,1);
        JViewport jvp=CommonGuiMethods.buildCommonTableView(m_psModelEvaluationTableData[0], m_psModelEvaluationTableData, 0, m_psModelEvaluationTableData.length-1);
        IPOGTableSP.setViewport(jvp);
        
    }
    void displayModelEvaluation(FittingModelNode cModel, String title){
        String psData[][]=cModel.getFittingEvaluationAsStringArray(title);
        if(m_psModelEvaluationTableData==null||!AppendTableCB.isSelected())
            m_psModelEvaluationTableData=psData;
        else
            m_psModelEvaluationTableData=CommonStatisticsMethods.addRow(m_psModelEvaluationTableData,psData,1);
        JViewport jvp=CommonGuiMethods.buildCommonTableView(m_psModelEvaluationTableData[0], m_psModelEvaluationTableData, 0, m_psModelEvaluationTableData.length-1);
        IPOGTableSP.setViewport(jvp);
    }
    void clearRegressions(){
        m_cIPOGTPW.clearRegressions();
    }
    void calRisingIntervals(){
        m_pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(m_dvY, m_nMaxRisingInterval);
    }
    public int selectDataInRoi(){
        double y, pdY[]=m_cIPOGTPW.getDataY();
        String option=(String) SelectRoiCB.getSelectedItem();
        boolean[] pbSelected=m_cIPOGTPW.getDataSelection();
        Roi roi=m_cIPOGTPW.pw.getImagePlus().getRoi();
        if(roi==null) return -1;
        DoubleRange drX=new DoubleRange(),drY=new DoubleRange();
        CommonGuiMethods.getCoordinateRanges(m_cIPOGTPW.pw, roi, drX, drY);
        int iI=m_cIPOGTPW.getXIndex(drX.getMin()),iF=m_cIPOGTPW.getXIndex(drX.getMax()),i;
        if(option.contentEquals("Current Roi Only")){
            CommonStatisticsMethods.setElements(pbSelected,false);
            for(i=iI;i<=iF;i++){
                y=pdY[i];
                if(!drY.contains(y)) continue;
                pbSelected[i]=true;
            }
        }
        if(option.contentEquals("Include")){
            for(i=iI;i<=iF;i++){
                y=pdY[i];
                if(!drY.contains(y)) continue;
                pbSelected[i]=true;
            }
        }
        if(option.contentEquals("Exclude")){
            for(i=iI;i<=iF;i++){
                y=pdY[i];
                if(!drY.contains(y)) continue;
                pbSelected[i]=false;
            }
        }
        if(option.contentEquals("Exclude Outside")){
            for(i=0;i<pbSelected.length;i++){
                if(i>=iI&&i<=iF) continue;
                pbSelected[i]=false;
            }
        }
        return 1;
    }
    public void setImageID(String ImageID){
        m_sImageID=ImageID;
    }
    int addToStatusArea(String message){
        if(message==null) return -1;
        m_cStatusTextArea.append(message+PrintAssist.newline);
        return 1;
    }
    int showStatus(){
        if(m_cStatusViewPort==null)m_cStatusViewPort=new JViewport();
        m_cStatusViewPort.setView(m_cStatusTextArea);
        IPOGTableSP.setViewport(m_cStatusViewPort);
        return 1;
    }
}
