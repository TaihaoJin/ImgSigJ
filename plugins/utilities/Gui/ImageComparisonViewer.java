/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImageComparisonViewer.java
 *
 * Created on Jul 20, 2011, 12:04:15 PM
 */

package utilities.Gui;
import ij.ImagePlus;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.Rectangle;
import ij.WindowManager;
import java.awt.Point;
import java.util.StringTokenizer;
import utilities.CommonMethods;
import utilities.CustomDataTypes.intRange;
import ij.plugin.frame.ContrastAdjuster;
import utilities.CommonStatisticsMethods;
import ij.gui.Roi;
import ij.gui.PointRoi;
import java.awt.Color;
import utilities.CommonGuiMethods;
import ij.ImageStack;
import utilities.Gui.AnalysisMasterForm;


/**
 *
 * @author Taihao
 */
public class ImageComparisonViewer extends javax.swing.JFrame implements MouseListener, MouseMotionListener, ComponentListener, ActionListener{
    public static final int StackPixelRange=1,SlicePixelRange=2,LocalPixelRange=3,SourcePixelRange=4;

    class ImagePacNode{
        public ImagePlus impl;
        boolean autoPixelRange;
        int PixelRangeChoice;
        int slice0;
        intRange pixelRange0,pixelRange;
        intRange slicePixelRange;
        intRange stackPixelRange;
        double mag,mag0;
        Rectangle srcRect, srcRect0;//used only for the source image pack
        boolean m_bSliceSeries;
        public ImagePacNode(ImagePlus impl){
            this(impl,false);
        }
        public ImagePacNode(ImagePlus impl, boolean bBuildStackPixelRange){
            this.impl=impl;
            if(impl==implSrc){
                autoPixelRange=m_bDisplayAutoAdjust;
                PixelRangeChoice=LocalPixelRange;
                srcRect=impl.getWindow().getCanvas().getSrcRect();
                srcRect0=new Rectangle(srcRect);
            }else{
                autoPixelRange=m_bDisplayAutoAdjust;
                PixelRangeChoice=SourcePixelRange;
            }
            pixelRange0=new intRange();
            slice0=impl.getCurrentSlice();
            slicePixelRange=new intRange();
            stackPixelRange=new intRange();
            buildSlicePixelRange();
            if(bBuildStackPixelRange) buildStackPixelRange();
            impl.show();
            mag=impl.getWindow().getCanvas().getMagnification();
        }
        public void buildStackPixelRange(){
            int nSlices=impl.getNSlices();
            int i,slice=impl.getCurrentSlice();
            for(i=0;i<nSlices;i++){
                impl.setSlice(i+1);
                buildSlicePixelRange();
                stackPixelRange.expandRange(slicePixelRange);
            }
            impl.setSlice(slice);
        }
        public void buildSlicePixelRange(){
            CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), m_pnPixelsT);
            slicePixelRange=CommonStatisticsMethods.getRange(m_pnPixelsT);
        }
        public boolean SliceChanged(){
            return slice0!=impl.getCurrentSlice();
        }
        public void updateSlice(){
            buildSlicePixelRange();
            slice0=impl.getCurrentSlice();
        }
        public boolean ViewChanged(){
           srcRect=impl.getWindow().getCanvas().getSrcRect();
           if(srcRect.x!=srcRect0.x) return true;
           if(srcRect.y!=srcRect0.y) return true;
           if(srcRect.width!=srcRect0.width) return true;
           if(srcRect.height!=srcRect0.height) return true;
           return false;
        }
        public boolean isSliceSeries(){
            return m_bSliceSeries;
        }
        public void updateView(){
           srcRect0.x=srcRect.x;
           srcRect0.y=srcRect.y;
           srcRect0.width=srcRect.width;
           srcRect0.height=srcRect.height;
        }
        public int adjustContrast(){
            if(impl.getType()==ImagePlus.COLOR_RGB) return -1;
            pixelRange=getDisplayPixelRange();
            if(!pixelRange.contenEquals(pixelRange0)){
                impl.getProcessor().setMinAndMax(pixelRange.getMin(), pixelRange.getMax());
                pixelRange0.reset(pixelRange);
                CommonMethods.refreshImage(impl);
            }
            return 1;
        }
        public intRange getDisplayPixelRange(){
            intRange pixelRange=null;
            switch (PixelRangeChoice){
                case SourcePixelRange:
                    if(isSourcePack()){
                        pixelRange=this.pixelRange;
                    }else{
                        pixelRange=implSrcPack.getDisplayPixelRange();
                    }
                    break;
                case LocalPixelRange:
                    pixelRange=getLocalPixelRange(impl);
                    break;
                case SlicePixelRange:
                    pixelRange=slicePixelRange;//slice need to be updated before calling this function.
                    break;
                case StackPixelRange:
                    if(stackPixelRange==null) buildStackPixelRange();
                    pixelRange=stackPixelRange;
                    break;
                default:
                    pixelRange=slicePixelRange;
                    break;
            }
            return pixelRange;
        }
        boolean isSourcePack(){
            return this==implSrcPack;
        }

    }

    ImagePlus implSrc;
    ImagePlus implAssociated;
    PointRoi m_cPointerRoi;

    ArrayList<ImagePacNode> implTargetPacks;
    ImagePacNode implSrcPack;
    int m_nNumImagesPerRow;
    int w,h,m_nZWs,m_nSlice0;
    int[][] m_pnPixels,m_pnPixelsT;

    boolean m_bDisplayAutoAdjust;
    intRange m_cDisplayPixelRange;
    ContrastAdjuster m_cContrastAdjuster;
    Rectangle m_cSrcRect;
    int xSpacing,ySpacing;
    boolean bHoldDisplaySynchronization;
    Roi m_cRoi;
    AssociatedImageDisplayer m_cAssociatedDisplayer;
    Point[] pcBlockCenters;
    intRange[] pcDisplayPixelRanges;
    int[] pnSliceIndexes;
    AnalysisMasterForm m_cMasterForm;
    public void setMasterForm(AnalysisMasterForm cMasterForm){
        m_cMasterForm=cMasterForm;
    }


    /** Creates new form ImageComparisonViewer */
    public ImageComparisonViewer() {
        initComponents();
        completeForm();
        m_cDisplayPixelRange=new intRange();
        xSpacing=0;
        ySpacing=0;
        bHoldDisplaySynchronization=false;
    }
    void completeForm(){
        implTargetPacks=new ArrayList();
        m_nNumImagesPerRow=Integer.parseInt(NumImagesPerRowTF.getText());
        m_bDisplayAutoAdjust=true;
        DisplayAutoAdjustCB.setSelected(m_bDisplayAutoAdjust);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        IncludeSourceImageBT = new javax.swing.JButton();
        IncludeTargetImageBT = new javax.swing.JButton();
        SynchronizeDisplayBT = new javax.swing.JButton();
        NumImagesPerRowTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        RemoveTargetImageBT = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        CenterAtTF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        GoToSliceTF = new javax.swing.JTextField();
        CloseAllTargetImagesBT = new javax.swing.JButton();
        DisplaySliceSeriesBT = new javax.swing.JButton();
        DisplayAutoAdjustCB = new javax.swing.JCheckBox();
        ContrastChoiceCB = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        UpdateOrdersBT = new javax.swing.JToggleButton();
        UpdateSpacingBT = new javax.swing.JButton();
        IncludeAllOpenImagesBT = new javax.swing.JButton();
        RemoveAllTargetImagesBT = new javax.swing.JButton();
        FirstSliceTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        LastSliceTF = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        IncrementTF = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        CloseAllImagesBT = new javax.swing.JButton();
        WindowWidthTF = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        SetWindowSizeBT = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        WindowHeightTF = new javax.swing.JTextField();
        SetMagnificationBT = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        MagnificationTF = new javax.swing.JTextField();
        MagnificationChoiceCB = new javax.swing.JComboBox();
        HighlightChoiceCB = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        HighlightBT = new javax.swing.JButton();
        HighlightCorrespondingPointsRB = new javax.swing.JRadioButton();
        UpdatePositionRB = new javax.swing.JRadioButton();
        BT1XBT = new javax.swing.JButton();
        BT10XBT = new javax.swing.JButton();
        BT16XBT = new javax.swing.JButton();
        SynchronizeSliceRB = new javax.swing.JRadioButton();
        DisplayAssociatedImagesRB = new javax.swing.JRadioButton();
        AssociatedImagePixelRatioTF = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        BringSrcImageFront = new javax.swing.JButton();
        FrontAssociatedImagesBT = new javax.swing.JButton();
        AdditionalMagnificationForAssociatedImageTF = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        IncludeSourceImageBT.setText("Includ Source Image");
        IncludeSourceImageBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IncludeSourceImageBTActionPerformed(evt);
            }
        });

        IncludeTargetImageBT.setText("Include Target Image");
        IncludeTargetImageBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IncludeTargetImageBTActionPerformed(evt);
            }
        });

        SynchronizeDisplayBT.setText("Sychronize Display");
        SynchronizeDisplayBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SynchronizeDisplayBTActionPerformed(evt);
            }
        });

        NumImagesPerRowTF.setText("3");
        NumImagesPerRowTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumImagesPerRowTFActionPerformed(evt);
            }
        });

        jLabel2.setText("Images Per Row");

        RemoveTargetImageBT.setText("Remove a Target Image");
        RemoveTargetImageBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveTargetImageBTActionPerformed(evt);
            }
        });

        jLabel3.setText("Center at");

        CenterAtTF.setText("1000,1000");
        CenterAtTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CenterAtTFActionPerformed(evt);
            }
        });

        jLabel4.setText("Go To Slice");

        GoToSliceTF.setText("1000");
        GoToSliceTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoToSliceTFActionPerformed(evt);
            }
        });

        CloseAllTargetImagesBT.setText("Close All Target Images");
        CloseAllTargetImagesBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseAllTargetImagesBTActionPerformed(evt);
            }
        });

        DisplaySliceSeriesBT.setText("Display Slice Series");
        DisplaySliceSeriesBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisplaySliceSeriesBTActionPerformed(evt);
            }
        });

        DisplayAutoAdjustCB.setText("Auto Adjust Display");
        DisplayAutoAdjustCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisplayAutoAdjustCBActionPerformed(evt);
            }
        });

        ContrastChoiceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Source", "View", "Slice", "Stack" }));
        ContrastChoiceCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ContrastChoiceCBActionPerformed(evt);
            }
        });

        jLabel5.setText("Contrast Choice");

        UpdateOrdersBT.setText("Update Orders");
        UpdateOrdersBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateOrdersBTActionPerformed(evt);
            }
        });

        UpdateSpacingBT.setText("Update Spacing");
        UpdateSpacingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSpacingBTActionPerformed(evt);
            }
        });

        IncludeAllOpenImagesBT.setText("Include All Open Images");
        IncludeAllOpenImagesBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IncludeAllOpenImagesBTActionPerformed(evt);
            }
        });

        RemoveAllTargetImagesBT.setText("Remove all Target Images");
        RemoveAllTargetImagesBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveAllTargetImagesBTActionPerformed(evt);
            }
        });

        FirstSliceTF.setText("-5");
        FirstSliceTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FirstSliceTFActionPerformed(evt);
            }
        });

        jLabel1.setText("First");

        LastSliceTF.setText("5");
        LastSliceTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LastSliceTFActionPerformed(evt);
            }
        });

        jLabel6.setText("Last");

        IncrementTF.setText("1");
        IncrementTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IncrementTFActionPerformed(evt);
            }
        });

        jLabel7.setText("Increment");

        CloseAllImagesBT.setText("Close All");
        CloseAllImagesBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseAllImagesBTActionPerformed(evt);
            }
        });

        WindowWidthTF.setText("256");

        jLabel8.setText("w");

        SetWindowSizeBT.setText("Set Window Size");
        SetWindowSizeBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetWindowSizeBTActionPerformed(evt);
            }
        });

        jLabel9.setText("h");

        WindowHeightTF.setText("256");

        SetMagnificationBT.setText("Set Magnification");
        SetMagnificationBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetMagnificationBTActionPerformed(evt);
            }
        });

        jLabel10.setText("Mag");

        MagnificationTF.setText("4.0");

        MagnificationChoiceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Roi", "Mag" }));

        HighlightChoiceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Source", "Local" }));

        jLabel11.setText("HighLight Choice");

        HighlightBT.setText("Highlight");
        HighlightBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HighlightBTActionPerformed(evt);
            }
        });

        HighlightCorrespondingPointsRB.setText("Hiligh Corresponding Points");
        HighlightCorrespondingPointsRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HighlightCorrespondingPointsRBActionPerformed(evt);
            }
        });

        UpdatePositionRB.setSelected(true);
        UpdatePositionRB.setText("update position");
        UpdatePositionRB.setVerifyInputWhenFocusTarget(false);

        BT1XBT.setText("1X");
        BT1XBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BT1XBTActionPerformed(evt);
            }
        });

        BT10XBT.setText("10X");
        BT10XBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BT10XBTActionPerformed(evt);
            }
        });

        BT16XBT.setText("16X");
        BT16XBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BT16XBTActionPerformed(evt);
            }
        });

        SynchronizeSliceRB.setText("Synchronize Slice");
        SynchronizeSliceRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SynchronizeSliceRBActionPerformed(evt);
            }
        });

        DisplayAssociatedImagesRB.setText("Dissplay ass imgs");
        DisplayAssociatedImagesRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisplayAssociatedImagesRBActionPerformed(evt);
            }
        });

        AssociatedImagePixelRatioTF.setText("1:2");

        jLabel12.setText("Pixel Ratio");

        BringSrcImageFront.setText("bring to front");
        BringSrcImageFront.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BringSrcImageFrontActionPerformed(evt);
            }
        });

        FrontAssociatedImagesBT.setText("to front");
        FrontAssociatedImagesBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrontAssociatedImagesBTActionPerformed(evt);
            }
        });

        AdditionalMagnificationForAssociatedImageTF.setText("2.0");
        AdditionalMagnificationForAssociatedImageTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AdditionalMagnificationForAssociatedImageTFActionPerformed(evt);
            }
        });

        jLabel13.setText("add mag");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(IncludeTargetImageBT)
                            .addComponent(IncludeAllOpenImagesBT)
                            .addComponent(RemoveTargetImageBT)
                            .addComponent(RemoveAllTargetImagesBT)
                            .addComponent(HighlightBT)
                            .addComponent(HighlightCorrespondingPointsRB)
                            .addComponent(SetWindowSizeBT)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(WindowWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(WindowHeightTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(UpdatePositionRB)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(SynchronizeDisplayBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SynchronizeSliceRB))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(NumImagesPerRowTF, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel2))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel4))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(GoToSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(CenterAtTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(DisplayAssociatedImagesRB)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel12)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(AssociatedImagePixelRatioTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel13)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(AdditionalMagnificationForAssociatedImageTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(FrontAssociatedImagesBT))))
                                .addGap(7, 7, 7)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(DisplaySliceSeriesBT)
                                    .addComponent(CloseAllTargetImagesBT)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel7)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(FirstSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel6)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(IncrementTF)
                                            .addComponent(LastSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(CloseAllImagesBT)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel5)
                                                .addComponent(jLabel11)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(BT1XBT)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(BT10XBT)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(HighlightChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(ContrastChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(BT16XBT)))
                                        .addComponent(DisplayAutoAdjustCB))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(57, 57, 57)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(11, 11, 11)
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(MagnificationTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(MagnificationChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(UpdateOrdersBT)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(SetMagnificationBT)
                                            .addComponent(UpdateSpacingBT)))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(124, 124, 124)
                        .addComponent(IncludeSourceImageBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BringSrcImageFront)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(IncludeSourceImageBT)
                    .addComponent(BringSrcImageFront))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(IncludeTargetImageBT)
                        .addGap(6, 6, 6)
                        .addComponent(IncludeAllOpenImagesBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(RemoveTargetImageBT)
                        .addGap(5, 5, 5)
                        .addComponent(RemoveAllTargetImagesBT)
                        .addGap(18, 18, 18)
                        .addComponent(HighlightBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(HighlightCorrespondingPointsRB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SynchronizeDisplayBT)
                            .addComponent(SynchronizeSliceRB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(NumImagesPerRowTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(GoToSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel13)
                                            .addComponent(AdditionalMagnificationForAssociatedImageTF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(CenterAtTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(FrontAssociatedImagesBT)))
                                    .addComponent(jLabel4)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(DisplayAssociatedImagesRB)
                                .addGap(2, 2, 2)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(AssociatedImagePixelRatioTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(UpdatePositionRB)
                        .addGap(6, 6, 6)
                        .addComponent(SetWindowSizeBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(WindowWidthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(WindowHeightTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(CloseAllTargetImagesBT)
                        .addGap(18, 18, 18)
                        .addComponent(DisplaySliceSeriesBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(FirstSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(LastSliceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(IncrementTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(CloseAllImagesBT)
                        .addGap(18, 18, 18)
                        .addComponent(DisplayAutoAdjustCB)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(ContrastChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(HighlightChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addGap(18, 18, 18)
                        .addComponent(UpdateOrdersBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(UpdateSpacingBT)
                        .addGap(18, 18, 18)
                        .addComponent(SetMagnificationBT)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(MagnificationTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MagnificationChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BT1XBT)
                    .addComponent(BT10XBT)
                    .addComponent(BT16XBT))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SynchronizeDisplayBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SynchronizeDisplayBTActionPerformed
        // TODO add your handling code here:
        bHoldDisplaySynchronization=false;
        synchronizeDisplay();

    }//GEN-LAST:event_SynchronizeDisplayBTActionPerformed

    private void NumImagesPerRowTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumImagesPerRowTFActionPerformed
        // TODO add your handling code here:
        m_nNumImagesPerRow=Integer.parseInt(NumImagesPerRowTF.getText());
        synchronizeDisplay();
    }//GEN-LAST:event_NumImagesPerRowTFActionPerformed

    private void IncludeSourceImageBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncludeSourceImageBTActionPerformed
        // TODO add your handling code here:
        pickSourceImage(WindowManager.getCurrentImage());
    }//GEN-LAST:event_IncludeSourceImageBTActionPerformed

    private void IncludeTargetImageBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncludeTargetImageBTActionPerformed
        // TODO add your handling code here:
        ImagePlus impl=WindowManager.getCurrentImage();
        pickTargetImage(impl);
    }//GEN-LAST:event_IncludeTargetImageBTActionPerformed
    public int pickTargetImage(ImagePlus impl){
        if(impl==implSrc) return -1;
        if(isTargetImage(impl)) return -1;
//        removeTargetImage(impl);
        if(impl.getWindow()==null) impl.show();
        impl.getWindow().getCanvas().addMouseListener(this);
        if(SynchronizeSliceRB.isSelected())
            if(!ConsistentStackSizes()) SynchronizeSliceRB.setSelected(false);
        implTargetPacks.add(new ImagePacNode(impl));
        synchronizeDisplay();
        return 1;
    }
    private void RemoveTargetImageBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveTargetImageBTActionPerformed
        // TODO add your handling code here:
        ImagePlus impl=WindowManager.getCurrentImage();
        removeTargetImage(impl);
    }//GEN-LAST:event_RemoveTargetImageBTActionPerformed

    public void removeTargetImage(ImagePlus impl0){
        int len=implTargetPacks.size(),i;
        ImagePlus impl=null;
        for(i=0;i<len;i++){
            if(impl0==implTargetPacks.get(i).impl) {
                impl=implTargetPacks.get(i).impl;
                implTargetPacks.remove(i);
            }
        }
        if(impl!=null){
            Point p;
            if(len>0){
                p=new Point(implTargetPacks.get(len-1).impl.getWindow().getLocation());
            }else{
                p=new Point(implSrc.getWindow().getLocation());
            }
            p.translate((int)(1.2*w), (int)(1.2*h));
            impl.getWindow().setLocation(p);
        }
    }
    private void CenterAtTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CenterAtTFActionPerformed
        ImagePlus impl=WindowManager.getCurrentImage();
        Point p=getXYCoordinatesFromString(CenterAtTF.getText(),",");
        setViewCenter(impl,p);
        highlightPoint(p);
    }//GEN-LAST:event_CenterAtTFActionPerformed
    public void setViewCenter1(ImagePlus impl, Point p){
        int x,y;
//        int magMax=32;//set by ImageJ
//        int cw=impl.getWindow().getCanvas().getWidth(),ch=impl.getWindow().getCanvas().getHeight();
        Point wlocation=impl.getWindow().getLocation();
        int ww=impl.getWindow().getWidth(),wh=impl.getWindow().getHeight();
        impl.getWindow().getCanvas().zoomOut(p.x, p.y);
        impl.getWindow().getCanvas().zoomIn(p.x, p.y);
        impl.getWindow().setLocationAndSize(wlocation.x, wlocation.y, ww, wh, false, false);
        if(implSrc!=null){
            if(implSrc==impl) synchronizeDisplay();
        }
    }
    public void setViewCenter(ImagePlus impl, Point p){
        int x,y;
//        int magMax=32;//set by ImageJ
//        int cw=impl.getWindow().getCanvas().getWidth(),ch=impl.getWindow().getCanvas().getHeight();
        int w=impl.getWidth(),h=impl.getHeight();
        Rectangle rect=impl.getWindow().getCanvas().getSrcRect();
        int rw=rect.width,rh=rect.height;

        int halfW=rect.width/2,halfH=rect.height/2;

        x=p.x-halfW;
        y=p.y-halfH;
        if(x>w-1-rw) x=w-1-rw;
        if(y>h-1-rh) y=h-1-rh;
        if(x<0) x=0;
        if(y<0) y=0;

        rect.x=x;
        rect.y=y;
//        rect.width=2*halfW+1;
//        rect.height=2*halfH+1;
        CommonMethods.refreshImage(impl);
        impl.getWindow().getCanvas().repaint();

//        impl.getWindow().getCanvas().setSrcRect(rect);
//        CommonMethods.refreshImage(impl);
        synchronizeDisplay();
//        impl.draw();
    }
    public void setViewCenter0(ImagePlus impl, Point p){
        int x,y;
        int magMax=32;//set by ImageJ
        int cw=impl.getWindow().getCanvas().getWidth(),ch=impl.getWindow().getCanvas().getHeight();

        Rectangle rect=impl.getWindow().getCanvas().getSrcRect();
        int halfW=Math.max(rect.width/2,cw/magMax),halfH=Math.max(rect.height/2,ch/magMax);
        halfW=Math.min(halfW, p.x);
        halfW=Math.min(halfW, w-p.x);
        halfH=Math.min(halfH, p.y);
        halfH=Math.min(halfH, h-p.y);

        x=p.x-halfW;
        y=p.y-halfH;
        rect.x=x;
        rect.y=y;
        rect.width=2*halfW+1;
        rect.height=2*halfH+1;

        double mag=0.5*Math.min(cw/halfW, ch/halfH);
        impl.getWindow().getCanvas().setSrcRect(rect);
        impl.getWindow().getCanvas().setMagnification(mag);
        CommonMethods.refreshImage(impl);
        synchronizeDisplay();
//        impl.draw();
    }
    Point getXYCoordinatesFromString(String st0, String delimiters){
        Point p=new Point();
        StringTokenizer stk=new StringTokenizer(st0,delimiters);
        p.x=Integer.parseInt(stk.nextToken());
        p.y=Integer.parseInt(stk.nextToken());
        return p;
    }
    private void GoToSliceTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoToSliceTFActionPerformed
        // TODO add your handling code here:
        ImagePlus impl=WindowManager.getCurrentImage();
        int slice=Integer.parseInt(GoToSliceTF.getText());
        impl.setSlice(slice);
        if(impl==implSrc) synchronizeDisplay();
    }//GEN-LAST:event_GoToSliceTFActionPerformed

    private void CloseAllTargetImagesBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseAllTargetImagesBTActionPerformed
        // TODO add your handling code here:
        int len=implTargetPacks.size(),i;
        for(i=len-1;i>=0;i--){
            implTargetPacks.get(i).impl.close();
            implTargetPacks.remove(i);
        }
    }//GEN-LAST:event_CloseAllTargetImagesBTActionPerformed

    private void DisplaySliceSeriesBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisplaySliceSeriesBTActionPerformed
        // TODO add your handling code here:
        displaySliceSeries();
    }//GEN-LAST:event_DisplaySliceSeriesBTActionPerformed
    void displaySliceSeries(){//make it independent of comparison
        boolean bt=bHoldDisplaySynchronization;
        bHoldDisplaySynchronization=true;
        pickSourceImage(WindowManager.getCurrentImage());
        implSrcPack.m_bSliceSeries=true;
        implTargetPacks.clear();
        updateSliceSeriesFrames();
        bHoldDisplaySynchronization=bt;
        synchronizeDisplay();
    }
    void updateSliceSeriesFrames(){
        boolean bt=bHoldDisplaySynchronization;
        bHoldDisplaySynchronization=true;
        int first=Integer.parseInt(FirstSliceTF.getText());
        int last=Integer.parseInt(LastSliceTF.getText());
        int delta=Integer.parseInt(IncrementTF.getText());
        ImagePlus impl=WindowManager.getCurrentImage();
        int zI, zF, slice,slice0,nSlices=impl.getNSlices();
        slice0=impl.getCurrentSlice();
        zI=Math.max(1, slice0+first);
        zF=Math.min(nSlices, slice0+last);
        ImagePlus implt;
        ImageStack is=implSrc.getImageStack();
//        implTargetPacks.clear();
        for(slice=zI;slice<=zF;slice+=delta){
            impl.setSlice(slice);
            implt=CommonMethods.cloneImage(impl, slice);
            implt.setTitle(is.getShortSliceLabel(slice));
            if(isSliceSeries()) pickTargetImage(implt);
            implt.show();
        }
        implSrc.setSlice(slice0);
        bHoldDisplaySynchronization=bt;
    }
    boolean isSliceSeries(){
        if(implSrcPack==null) return false;
        return implSrcPack.isSliceSeries();
    }
    private void DisplayAutoAdjustCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisplayAutoAdjustCBActionPerformed
        // TODO add your handling code here:
        m_bDisplayAutoAdjust=DisplayAutoAdjustCB.isSelected();
        if(m_bDisplayAutoAdjust&&m_cContrastAdjuster==null){
            m_cContrastAdjuster=new ContrastAdjuster();
        }
        synchronizeDisplay();
    }//GEN-LAST:event_DisplayAutoAdjustCBActionPerformed

    private void ContrastChoiceCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ContrastChoiceCBActionPerformed
        // TODO add your handling code here:
        ImagePlus impl=WindowManager.getCurrentImage();
        int choice=getContrastChoice();
        setContrastChoice(impl,choice);
    }//GEN-LAST:event_ContrastChoiceCBActionPerformed
    public void setContrastChoice(ImagePlus impl,int choice){
        ImagePacNode pac=getImagePack(impl);
        if(pac==null){
            pac=new ImagePacNode(impl,choice==StackPixelRange);
        }
        pac.PixelRangeChoice=choice;
        pac.adjustContrast();
    }
    private void UpdateOrdersBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateOrdersBTActionPerformed
        // TODO add your handling code here:
        updateTargetImageOrders();
        synchronizeDisplay();
    }//GEN-LAST:event_UpdateOrdersBTActionPerformed

    private void UpdateSpacingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateSpacingBTActionPerformed
        // TODO add your handling code here:
        updateSpacing();
        synchronizeDisplay();
    }//GEN-LAST:event_UpdateSpacingBTActionPerformed

    private void RemoveAllTargetImagesBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveAllTargetImagesBTActionPerformed
        // TODO add your handling code here:
        removeAllTargetImages();
    }//GEN-LAST:event_RemoveAllTargetImagesBTActionPerformed
    public void removeAllTargetImages(){
        int len=implTargetPacks.size();
        for(int i=len-1;i>=0;i--){
            implTargetPacks.remove(i);
        }
    }
    private void IncludeAllOpenImagesBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncludeAllOpenImagesBTActionPerformed
        // TODO add your handling code here:
        ArrayList<ImagePlus> images=CommonMethods.getAllOpenImages();
        int len=images.size();
        for(int i=0;i<len;i++){
            pickTargetImage(images.get(i));
        }
    }//GEN-LAST:event_IncludeAllOpenImagesBTActionPerformed

    private void CloseAllImagesBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseAllImagesBTActionPerformed
        // TODO add your handling code here:
        CommonMethods.closeAllImages();
    }//GEN-LAST:event_CloseAllImagesBTActionPerformed

    private void IncrementTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncrementTFActionPerformed
        // TODO add your handling code here:
        if(implSrcPack!=null){
            if(implSrcPack.isSliceSeries())updateSliceSeries();
        }
    }//GEN-LAST:event_IncrementTFActionPerformed

    private void FirstSliceTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FirstSliceTFActionPerformed
        // TODO add your handling code here:
        if(implSrcPack!=null){
            if(implSrcPack.isSliceSeries())updateSliceSeries();
        }
    }//GEN-LAST:event_FirstSliceTFActionPerformed

    private void LastSliceTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LastSliceTFActionPerformed
        // TODO add your handling code here:
        if(implSrcPack!=null){
            if(implSrcPack.isSliceSeries())updateSliceSeries();
        }
    }//GEN-LAST:event_LastSliceTFActionPerformed

    private void SetWindowSizeBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetWindowSizeBTActionPerformed
        // TODO add your handling code here:
        setWindowSize();
    }//GEN-LAST:event_SetWindowSizeBTActionPerformed

    private void SetMagnificationBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetMagnificationBTActionPerformed
        // TODO add your handling code here:
        setMagnification();
    }//GEN-LAST:event_SetMagnificationBTActionPerformed

    private void HighlightBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HighlightBTActionPerformed
        // TODO add your handling code here:
        highlight();
    }//GEN-LAST:event_HighlightBTActionPerformed

    private void HighlightCorrespondingPointsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HighlightCorrespondingPointsRBActionPerformed
        // TODO add your handling code here:
        if(!HighlightCorrespondingPointsRB.isSelected()) m_cPointerRoi=null;
    }//GEN-LAST:event_HighlightCorrespondingPointsRBActionPerformed

    private void BT1XBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BT1XBTActionPerformed
        // TODO add your handling code here:
        double mag=1;
        setMagnification(mag);
    }//GEN-LAST:event_BT1XBTActionPerformed

    private void BT10XBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BT10XBTActionPerformed
        // TODO add your handling code here:
        double mag=10;
        setMagnification(mag);
    }//GEN-LAST:event_BT10XBTActionPerformed

    private void BT16XBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BT16XBTActionPerformed
        // TODO add your handling code here:
        double mag=16;
        setMagnification(mag);
    }//GEN-LAST:event_BT16XBTActionPerformed

    private void SynchronizeSliceRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SynchronizeSliceRBActionPerformed
        // TODO add your handling code here:
        if(!ConsistentStackSizes()) SynchronizeSliceRB.setSelected(false);
    }//GEN-LAST:event_SynchronizeSliceRBActionPerformed
    public void SynchronizeSlice(){
        SynchronizeSliceRB.setSelected(true);
        SynchronizeSliceRBActionPerformed(null);
    }
    private void DisplayAssociatedImagesRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisplayAssociatedImagesRBActionPerformed
        // TODO add your handling code here:
        if(DisplayAssociatedImagesRB.isSelected())
            updateAssociatedImages();
        else
            closeAssociatedImages();
    }//GEN-LAST:event_DisplayAssociatedImagesRBActionPerformed

    private void BringSrcImageFrontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BringSrcImageFrontActionPerformed
        // TODO add your handling code here:
        implSrc.getWindow().toFront();
    }//GEN-LAST:event_BringSrcImageFrontActionPerformed

    private void FrontAssociatedImagesBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FrontAssociatedImagesBTActionPerformed
        // TODO add your handling code here:
        implAssociated.getWindow().toFront();
    }//GEN-LAST:event_FrontAssociatedImagesBTActionPerformed

    private void AdditionalMagnificationForAssociatedImageTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AdditionalMagnificationForAssociatedImageTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AdditionalMagnificationForAssociatedImageTFActionPerformed
    ArrayList<intRange> getPixelDisplayRanges(){
        ArrayList<intRange> cvRanges=new ArrayList();
        cvRanges.add(implSrcPack.pixelRange);
        int len=implTargetPacks.size();
        for(int i=0;i<len;i++){
            cvRanges.add(implTargetPacks.get(i).pixelRange);
        }
        return cvRanges;
    }
    public void updateAssociatedImages(){
        DisplayAssociatedImagesRB.setSelected(true);
        if(!AssociatedImageIsValid()) {
            closeAssociatedImages();
            createAssociatedImage();
        }
        ArrayList<ImagePlus> impls=getSourceAndTargetImages();
        ArrayList<intRange> cvPixelDisplayRanges=getPixelDisplayRanges();
        int i,len=impls.size(),nDisplayRadiusx,nDisplayRadiusy;
        pcBlockCenters=CommonStatisticsMethods.getPointArray(pcBlockCenters, len);
        pcDisplayPixelRanges=CommonStatisticsMethods.getIntRangeArray(pcDisplayPixelRanges, len);
        pnSliceIndexes=CommonStatisticsMethods.getIntArray(pnSliceIndexes, len);
        Rectangle rect;

        for(i=0;i<len;i++){
            pcBlockCenters[i]=new Point(m_cSrcRect.x+m_cSrcRect.width/2,m_cSrcRect.y+m_cSrcRect.height/2);
            pcDisplayPixelRanges[i]=cvPixelDisplayRanges.get(i);
            pnSliceIndexes[i]=impls.get(i).getCurrentSlice();
        }
        m_cAssociatedDisplayer.updateDisplayImage(pcBlockCenters, pnSliceIndexes, pcDisplayPixelRanges);
    }
    int closeAssociatedImages(){
        if(m_cAssociatedDisplayer==null) return -1;
        m_cAssociatedDisplayer.getDisplayImage().close();
        return 1;
    }
    int createAssociatedImage(){
        if(implSrc==null) return -1;
        ArrayList<ImagePlus> impls=new ArrayList();
        impls.add(implSrc);
        int i,len=implTargetPacks.size();  
        int nNumImages=len+1;
        int rows=nNumImages/m_nNumImagesPerRow;
        if(nNumImages>rows*m_nNumImagesPerRow) rows++;

        for(i=0;i<len;i++){
            impls.add(implTargetPacks.get(i).impl);
        }

        String sRatio=AssociatedImagePixelRatioTF.getText();
        StringTokenizer stk=new StringTokenizer(sRatio,":");
        int num=Integer.parseInt(stk.nextToken()),dem=Integer.parseInt(stk.nextToken());

        m_cAssociatedDisplayer=new AssociatedImageDisplayer("Associated Image of "+this.getTitle(),impls,rows,m_nNumImagesPerRow,num*(m_cSrcRect.width-1)/(2*dem),num*(m_cSrcRect.height-1)/(2*dem),4);
        implAssociated=m_cAssociatedDisplayer.implDisplay;
        implAssociated.getWindow().getCanvas().removeMouseListener(m_cAssociatedDisplayer);
        implAssociated.getWindow().getCanvas().removeMouseMotionListener(m_cAssociatedDisplayer);
//        implAssociated.getWindow().getCanvas().addMouseListener(implSrc.getWindow().getCanvas());
        implAssociated.getWindow().getCanvas().addMouseMotionListener(implSrc.getWindow().getCanvas());
        implAssociated.getWindow().getCanvas().addMouseListener(this);
        implAssociated.getWindow().getCanvas().addMouseMotionListener(this);
        implSrc.getWindow().getCanvas().addPaintListener(this);
//        implAssociated.getWindow().getCanvas().addMouseListener(m_cAssociatedDisplayer);
        implAssociated.getWindow().getCanvas().addMouseMotionListener(m_cAssociatedDisplayer);
        double mag=implSrc.getWindow().getCanvas().getMagnification();
        double addMag=Double.parseDouble(AdditionalMagnificationForAssociatedImageTF.getText());
        setMagnification(implAssociated,mag*addMag);
        CommonGuiMethods.optimizeWindowSize(implAssociated);

        return 1;
    }
    ArrayList<ImagePlus> getSourceAndTargetImages(){
        ArrayList<ImagePlus> impls=new ArrayList();
        impls.add(implSrc);
        int len=implTargetPacks.size();
        for(int i=0;i<len;i++){
            impls.add(implTargetPacks.get(i).impl);
        }
        return impls;
    }
    boolean AssociatedImageIsValid(){

        String sRatio=AssociatedImagePixelRatioTF.getText();
        StringTokenizer stk=new StringTokenizer(sRatio,":");
        int num=Integer.parseInt(stk.nextToken()),dem=Integer.parseInt(stk.nextToken());

        if(m_cAssociatedDisplayer==null) return false;
        if(!implAssociated.isVisible()) return false;
        if(num*(m_cSrcRect.width-1)/(2*dem)!=m_cAssociatedDisplayer.getDisplayRadiusx()) return false;
        if(num*(m_cSrcRect.height-1)/(2*dem)!=m_cAssociatedDisplayer.getDisplayRadiusy()) return false;
        ArrayList<ImagePlus> impls=m_cAssociatedDisplayer.getSrcImages();
        int len=impls.size(),lenTargets=implTargetPacks.size();
        int nImages=lenTargets+1;
        if(len!=nImages) return false;
        if(m_nNumImagesPerRow!=m_cAssociatedDisplayer.nImgCols) return false;
        int rows=nImages/m_nNumImagesPerRow;
        if(nImages>rows*m_nNumImagesPerRow) rows++;
        if(rows!=m_cAssociatedDisplayer.nImgRows) return false;
        int i,j;
        ImagePlus impl;

        boolean valid=false;
        for(j=0;j<len;j++){
            if(impls.get(j)==implSrc){
                valid=true;
                break;
            }
        }
        if(!valid) return false;
        for(i=0;i<lenTargets;i++){
            impl=implTargetPacks.get(i).impl;
            valid=false;
            for(j=0;j<len;j++){
                if(impls.get(j)==impl){
                    valid=true;
                    break;
                }
            }
            if(!valid) return false;
        }
        return true;
    }
    void highlight(){
       if(m_cAssociatedDisplayer!=null) m_cAssociatedDisplayer.showHighlightCollections();
       String choice=(String)HighlightChoiceCB.getSelectedItem();
        RoiHighlightNode highlighter,SrcHighlighter=RoiHighlighter.getHighlighter(implSrc);
        int i,len=implTargetPacks.size();
        RoiHighlighter.highLight();        
        ImagePlus impl;
        for(i=len-1;i>=0;i--){
            impl=implTargetPacks.get(i).impl;
            if(choice.contentEquals("Source"))
                highlighter=SrcHighlighter;
            else
                highlighter=RoiHighlighter.getHighlighter(impl);
            if(highlighter!=null) highlighter.highlight(impl);
        }
    }
    int setMagnification(){
        if(implSrc==null) return -1;
        boolean bRoi=false;
        Rectangle r=null;
        if(((String)MagnificationChoiceCB.getSelectedItem()).contentEquals("Roi")){
            Roi roi=implSrc.getRoi();
            if(roi!=null){
                if(!(roi instanceof PointRoi)){
                    r=roi.getBoundingRect();
                    implSrc.getWindow().getCanvas().setSrcRect(r);
                    bRoi=true;
                }
            }
        }

        double mag;
        if(bRoi){
            mag=Math.min(implSrc.getWidth()/((double)r.width), implSrc.getHeight()/((double)r.height));
        }else{
            mag=Double.parseDouble(MagnificationTF.getText());
        }
        setMagnification(mag);
        return 1;
    }
    public int setMagnification(double mag){

        Point wlocation=implSrc.getWindow().getLocation();

        if(implSrc==null) return -1;
        int ww=implSrc.getWindow().getWidth(),wh=implSrc.getWindow().getHeight();
        if(mag>32) mag=31.9;
        if(mag<0.3) mag=0.3;
        double oldMag=implSrc.getWindow().getCanvas().getMagnification();
        if(oldMag>mag){
            while(oldMag>mag){
                implSrc.getWindow().getCanvas().zoomOut(0, 0);
                oldMag=implSrc.getWindow().getCanvas().getMagnification();
            }
        }else{
            while(oldMag<mag){
                implSrc.getWindow().getCanvas().zoomIn(0, 0);
                oldMag=implSrc.getWindow().getCanvas().getMagnification();
            }
        }

        implSrc.getWindow().setLocationAndSize(wlocation.x, wlocation.y, ww, wh, false, false);

        synchronizeDisplay();
        return 1;
    }

    public int setMagnification(ImagePlus impl, double mag){

        Point wlocation=impl.getWindow().getLocation();

        if(impl==null) return -1;
        int ww=impl.getWindow().getWidth(),wh=impl.getWindow().getHeight();
        if(mag>32) mag=31.9;
        if(mag<0.3) mag=0.3;
        double oldMag=impl.getWindow().getCanvas().getMagnification();
        if(oldMag>mag){
            while(oldMag>mag){
                impl.getWindow().getCanvas().zoomOut(0, 0);
                oldMag=impl.getWindow().getCanvas().getMagnification();
            }
        }else{
            while(oldMag<mag){
                impl.getWindow().getCanvas().zoomIn(0, 0);
                oldMag=impl.getWindow().getCanvas().getMagnification();
            }
        }

        impl.getWindow().setLocationAndSize(wlocation.x, wlocation.y, ww, wh, false, false);
        return 1;
    }
    int setWindowSize(){
        if(implSrc==null) return -1;
        w=Integer.parseInt(WindowWidthTF.getText());
        h=Integer.parseInt(WindowHeightTF.getText());
        setWindowSize(w, h);
        return 1;
    }

    public int setWindowSize(int w, int h){
        if(implSrc==null) return -1;
        Point p=implSrc.getWindow().getLocation();
        implSrc.getWindow().setLocationAndSize(p.x, p.y, w, h, false, false);
        synchronizeDisplay();
        return 1;
    }

    void updateSliceSeries(){
        boolean bt=bHoldDisplaySynchronization;
        bHoldDisplaySynchronization=true;
        int first=Integer.parseInt(FirstSliceTF.getText());
        int last=Integer.parseInt(LastSliceTF.getText());
        int delta=Integer.parseInt(IncrementTF.getText());
        int zI,zF,slice0=implSrc.getCurrentSlice(),nSlices=implSrc.getNSlices(),i;
        zI=Math.max(1, slice0+first);
        zF=Math.min(nSlices, slice0+last);
        int num=(zF-zI)/delta+1,len=implTargetPacks.size();
        if(num>len){
            num-=len;
            for(i=0;i<num;i++){
                pickTargetImage(CommonMethods.cloneImage(implSrc,implSrc.getCurrentSlice()));
            }
        }else if(num<len){
            ImagePacNode pac;
            num=len-num;
            for(i=0;i<num;i++){
                pac=implTargetPacks.get(0);
                pac.impl.close();
                implTargetPacks.remove(0);
            }            
        }
        updateSliceSeriesImages();
        bHoldDisplaySynchronization=bt;
    }

    int updateSliceSeriesImages(){
        int first=Integer.parseInt(FirstSliceTF.getText());
        int last=Integer.parseInt(LastSliceTF.getText());
        int delta=Integer.parseInt(IncrementTF.getText());
        int zI,zF,slice=implSrc.getCurrentSlice(),nSlices=implSrc.getNSlices();
        zI=Math.max(1, slice+first);
        zF=Math.min(nSlices, slice+last);
        int len=(zF-zI)/delta+1;
        ImagePlus impl;
        if(len!=implTargetPacks.size()) return -1;//could be in the process of constructing the slice series
        int slice0=slice;
        int index;
        for(slice=zI;slice<=zF;slice+=delta){
            implSrc.setSlice(slice);
            CommonMethods.getPixelValue(implSrc, slice, m_pnPixelsT);
            index=(slice-zI)/delta;
            impl=implTargetPacks.get(index).impl;
            CommonMethods.setPixels(impl, m_pnPixelsT);
            impl.setTitle(""+(slice));
            CommonMethods.refreshImage(impl);
        }
        implSrc.setSlice(slice0);
        return 1;
    }
    
    void updateSpacing(){
        Point po=implSrc.getWindow().getLocation(),px=implTargetPacks.get(0).impl.getWindow().getLocation();
        int W=implSrc.getWindow().getWidth(),H=implSrc.getWindow().getHeight();
        xSpacing=px.x-(po.x+W);
        int len=implTargetPacks.size();
        if(len>m_nNumImagesPerRow-1) {
            Point py=implTargetPacks.get(m_nNumImagesPerRow-1).impl.getWindow().getLocation();
            ySpacing=py.y-(po.y+H);
        }
    }

    void updateTargetImageOrders(){
        int i,len=implTargetPacks.size();
        double[] pdPositions=new double[len];
        ArrayList<Object> ov=new ArrayList();
        Point p,po=implSrc.getWindow().getLocation();
        int W=implSrc.getWindow().getWidth(),H=implSrc.getWindow().getHeight();
        double factor=10000;
        ImagePacNode pac;
        for(i=0;i<len;i++){
            pac=implTargetPacks.get(i);
            p=pac.impl.getWindow().getLocation();
            pdPositions[i]=((int)((double)(p.y-po.y)/(double)W+0.5))*factor+p.x;
            ov.add(implTargetPacks.get(i));
        }
        CommonMethods.sortObjectArray(ov, pdPositions);

        implTargetPacks.clear();
        for(i=0;i<len;i++){
            implTargetPacks.add((ImagePacNode)ov.get(i));
        }
    }
    void updateContrastChoice(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePacNode pac=getImagePack(impl);
        int choice=getContrastChoice();
        if(pac==null){
            pac=new ImagePacNode(impl,choice==StackPixelRange);
        }
        pac.PixelRangeChoice=choice;
        pac.adjustContrast();
    }

    int getContrastChoice(){
        return getContrastChoice((String)ContrastChoiceCB.getSelectedItem());
    }
    public int getContrastChoice(String sChoice){
        if (sChoice.contentEquals("Stack")) return StackPixelRange;
        if (sChoice.contentEquals("Slice")) return SlicePixelRange;
        if (sChoice.contentEquals("View")) return LocalPixelRange;
        if (sChoice.contentEquals("Source")) return SourcePixelRange;
        return -1;//caller needs to no this is not a valid choice
    }
    boolean isTargetImage(ImagePlus impl){
        int len=implTargetPacks.size(),i;
        for(i=len-1;i>=0;i--){
            if(implTargetPacks.get(i).impl==impl) return true;
        }
        return false;
    }
    public void pickSourceImage(ImagePlus impl){
        boolean bNeedInitialize=false;
        if(implSrc==null){
            bNeedInitialize=true;
        }else{
            removeSourceImage(implSrc);
            if(w!=impl.getWidth()||h!=impl.getHeight())
                bNeedInitialize=true;
        }
        if(bNeedInitialize) {
            w=impl.getWidth();
            h=impl.getHeight();
            m_pnPixels=new int[h][w];
            m_pnPixelsT=new int[h][w];
        }
        removeTargetImage(impl);//will do nothing if impl is not one of the target images
        implSrc=impl;
        implSrc.getWindow().addComponentListener(this);
        implSrc.getWindow().getCanvas().addMouseListener(this);
        implSrc.getWindow().getCanvas().addMouseMotionListener(this);
        m_pnPixels=CommonMethods.getPixelValues(implSrc);
        implSrcPack=new ImagePacNode(implSrc);
        m_nSlice0=-1;
    }
    void removeSourceImage(ImagePlus impl){
        impl.getWindow().removeComponentListener(this);
        impl.getWindow().getCanvas().removeMouseListener(this);
        impl.getWindow().getCanvas().removeMouseMotionListener(this);
    }

    public int synchronizeDisplay(){
        implSrc.getWindow().setAlwaysOnTop(false);
        if(bHoldDisplaySynchronization) return -1;
        if(implSrcPack==null) return -1;
        boolean needAdjust=false;

        if(implSrcPack.SliceChanged()){
            implSrcPack.updateSlice();
            if(implSrcPack!=null){
                if(implSrcPack.isSliceSeries())updateSliceSeries();
            }
            needAdjust=true;
        }
        if(implSrcPack.ViewChanged()){
            implSrcPack.updateView();
            needAdjust=true;
        }

        implSrc.setRoi(m_cRoi);

        if(m_bDisplayAutoAdjust){
            implSrcPack.adjustContrast();
        }
        int i,len=implTargetPacks.size();
        int w=implSrc.getWindow().getWidth(),h=implSrc.getWindow().getHeight();
        double mag=implSrc.getWindow().getCanvas().getMagnification();
        Point p=implSrc.getWindow().getLocation();
        m_cSrcRect=implSrc.getWindow().getCanvas().getSrcRect();
        ImagePlus impl;
        ImagePacNode implPac;
        int x=p.x,y=p.y,r,c;
        int slice=implSrc.getCurrentSlice();
        for(i=len-1;i>=0;i--){
            r=(i+1)/m_nNumImagesPerRow;
            c=(i+1)%m_nNumImagesPerRow;
            implPac=implTargetPacks.get(i);
            impl=implPac.impl;
            if(SynchronizeSliceRB.isSelected()) impl.setSlice(slice);
            impl.setRoi(m_cRoi);
            if(!impl.isVisible())impl.show();
            impl.getWindow().setLocationAndSize(x+c*(w+xSpacing), y+r*(h+ySpacing), w, h,false,false);
            impl.getWindow().getCanvas().setSrcRect(m_cSrcRect);
            impl.getWindow().getCanvas().setMagnification(mag);
            impl.getWindow().getCanvas().setSize(implSrc.getWindow().getCanvas().getWidth(), implSrc.getWindow().getCanvas().getHeight());
            if(m_bDisplayAutoAdjust&&needAdjust&&impl.getType()!=ImagePlus.COLOR_RGB)implPac.adjustContrast();
            impl.draw();
            if(m_cPointerRoi!=null) CommonGuiMethods.highlightRoi(impl, m_cPointerRoi, Color.GREEN);
        }
        if(m_cPointerRoi!=null) CommonGuiMethods.highlightRoi(implSrc, m_cPointerRoi, Color.GREEN);
        if(DisplayAssociatedImagesRB.isSelected()) updateAssociatedImages();
//        highlight();
        return 1;
    }
    public boolean ConsistentStackSizes(){
        if(bHoldDisplaySynchronization) return false;
        if(implSrcPack==null) return false;

        int i,len=implTargetPacks.size();
        int slices=implSrc.getStackSize();
        for(i=len-1;i>=0;i--){
            if(implTargetPacks.get(i).impl.getStackSize()!=slices) return false;
        }
        return true;
    }
    ImagePacNode getImagePack(ImagePlus impl){
        ImagePacNode pac=null;
        if(impl==implSrc) return implSrcPack;
        int i,len=implTargetPacks.size();
        for(i=0;i<len;i++){
            pac=implTargetPacks.get(i);
            if(pac.impl==impl) return pac;
        }
        return null;
    }
    void adjustDisplayRange(ImagePlus impl, intRange displayRange){
        int min=m_cDisplayPixelRange.getMin(),max=m_cDisplayPixelRange.getMax();//need to finish it tomorrow
        m_cContrastAdjuster.setMinAndMax(impl, min, max);
    }
    intRange getDisplayRangeSrc(){
        intRange ir=new intRange();
        Rectangle rect=implSrc.getWindow().getCanvas().getSrcRect();
        int nSlice=implSrc.getCurrentSlice();
        if(nSlice!=m_nSlice0) CommonMethods.getPixelValue(implSrc, nSlice, m_pnPixels);
        int i,j,w=rect.width,h=rect.height,x0=rect.x,y0=rect.y,pixel;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pixel=m_pnPixels[y0+i][x0+j];
                ir.expandRange(pixel);
            }
        }
        return ir;
    }
    intRange getLocalPixelRange(ImagePlus impl){
        int[] pnRange=new int[2];
        if(m_pnPixelsT==null) m_pnPixelsT=new int[h][w];
        Rectangle rect=impl.getWindow().getCanvas().getSrcRect();
        int nSlice=impl.getCurrentSlice();
        int i,j,x0=rect.x,y0=rect.y;
        if(x0<0) x0=0;
        if(x0>=w) x0=w-1;
        if(y0<0) y0=0;
        if(y0>=w) y0=h-1;
        
        int rw=Math.min(rect.width,w-x0),rh=Math.min(rect.height,h-y0),pixel;

        CommonMethods.getPixelValue(impl, nSlice, m_pnPixelsT,x0,y0,rw,rh,pnRange);
        intRange ir=new intRange(pnRange[0],pnRange[1]);
        return ir;
    }
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ImageComparisonViewer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AdditionalMagnificationForAssociatedImageTF;
    private javax.swing.JTextField AssociatedImagePixelRatioTF;
    private javax.swing.JButton BT10XBT;
    private javax.swing.JButton BT16XBT;
    private javax.swing.JButton BT1XBT;
    private javax.swing.JButton BringSrcImageFront;
    private javax.swing.JTextField CenterAtTF;
    private javax.swing.JButton CloseAllImagesBT;
    private javax.swing.JButton CloseAllTargetImagesBT;
    private javax.swing.JComboBox ContrastChoiceCB;
    private javax.swing.JRadioButton DisplayAssociatedImagesRB;
    private javax.swing.JCheckBox DisplayAutoAdjustCB;
    private javax.swing.JButton DisplaySliceSeriesBT;
    private javax.swing.JTextField FirstSliceTF;
    private javax.swing.JButton FrontAssociatedImagesBT;
    private javax.swing.JTextField GoToSliceTF;
    private javax.swing.JButton HighlightBT;
    private javax.swing.JComboBox HighlightChoiceCB;
    private javax.swing.JRadioButton HighlightCorrespondingPointsRB;
    private javax.swing.JButton IncludeAllOpenImagesBT;
    private javax.swing.JButton IncludeSourceImageBT;
    private javax.swing.JButton IncludeTargetImageBT;
    private javax.swing.JTextField IncrementTF;
    private javax.swing.JTextField LastSliceTF;
    private javax.swing.JComboBox MagnificationChoiceCB;
    private javax.swing.JTextField MagnificationTF;
    private javax.swing.JTextField NumImagesPerRowTF;
    private javax.swing.JButton RemoveAllTargetImagesBT;
    private javax.swing.JButton RemoveTargetImageBT;
    private javax.swing.JButton SetMagnificationBT;
    private javax.swing.JButton SetWindowSizeBT;
    private javax.swing.JButton SynchronizeDisplayBT;
    private javax.swing.JRadioButton SynchronizeSliceRB;
    private javax.swing.JToggleButton UpdateOrdersBT;
    private javax.swing.JRadioButton UpdatePositionRB;
    private javax.swing.JButton UpdateSpacingBT;
    private javax.swing.JTextField WindowHeightTF;
    private javax.swing.JTextField WindowWidthTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
    public void mousePressed(MouseEvent e){}
    public void mouseClicked(MouseEvent eo){
        ImagePlus impl=getSourceImage(eo);

        if(impl!=null&&impl!=implAssociated) {
            m_cRoi=impl.getRoi();
            if(HighlightCorrespondingPointsRB.isSelected()){
                if(m_cRoi instanceof PointRoi) m_cRoi=null;
                Point pt=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
                m_cPointerRoi=new PointRoi(pt.x,pt.y);
                if(m_cRoi==null){
                    m_cRoi=m_cPointerRoi;
                }
            }
            if(UpdatePositionRB.isSelected()){
                Point pt=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
                CenterAtTF.setText(pt.x+","+pt.y);
            }
            synchronizeDisplay();
        }else if(impl!=null&&impl==implAssociated){
            Point pt=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
            pt=m_cAssociatedDisplayer.getOriginalCoordinates(pt.x, pt.y);

            if(HighlightCorrespondingPointsRB.isSelected()){
                if(m_cRoi instanceof PointRoi) m_cRoi=null;
                m_cPointerRoi=new PointRoi(pt.x,pt.y);
                if(m_cRoi==null){
                    m_cRoi=m_cPointerRoi;
                }
                m_cAssociatedDisplayer.highlightCorrespondingPoints(pt, Color.green);
            }
            if(UpdatePositionRB.isSelected()){
                CenterAtTF.setText(pt.x+","+pt.y);
            }
        }

    }

    public void highlightPoint(Point pt){
        m_cRoi=implSrc.getRoi();
        if(m_cRoi instanceof PointRoi) m_cRoi=null;
        m_cPointerRoi=new PointRoi(pt.x,pt.y);
        if(m_cRoi==null){
            m_cRoi=m_cPointerRoi;
        }
    }

    ImagePlus getSourceImage(MouseEvent eo){
        ImagePlus impl=null;
        if(eo.getSource()==implSrc.getWindow().getCanvas()) return implSrc;
        int i,len=implTargetPacks.size();
        for(i=0;i<len;i++){
            impl=implTargetPacks.get(i).impl;
            if(eo.getSource()==impl.getWindow().getCanvas()) return impl;
        }
        if(eo.getSource()==implAssociated.getWindow().getCanvas()) return implAssociated;
        return null;
    }
    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e){}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseDragged(MouseEvent e){
        synchronizeDisplay();
    }
    public void mouseMoved(MouseEvent e){};
    public void componentHidden(ComponentEvent ce){}
    public void componentMoved(ComponentEvent ce){synchronizeDisplay();}
    public void componentResized(ComponentEvent ce){synchronizeDisplay();}
    public void componentShown(ComponentEvent ce){}
    public ImagePlus getAssociatedImage(){
        return implAssociated;
    }
    public void actionPerformed(ActionEvent ae){
        ImagePlus impl=CommonGuiMethods.getSourceImage(ae);
        if(impl==implSrc){
            if(implSrcPack.SliceChanged()) {
                synchronizeDisplay();
                if(DisplayAssociatedImagesRB.isSelected()) updateAssociatedImages();
            }
        }
    }
    public AssociatedImageDisplayer getAssociatedDisplayer(){
        return m_cAssociatedDisplayer;
    }
}
