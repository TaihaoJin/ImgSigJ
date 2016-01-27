/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * IPOGTrackSelectionChoiceForm.java
 *
 * Created on Oct 6, 2011, 6:32:16 PM
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.CommonGuiMethods;
import utilities.io.PrintAssist;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class IPOGTrackSelectionChoiceForm extends javax.swing.JFrame {

    /** Creates new form IPOGTrackSelectionChoiceForm */
    StackIPOGTracksNode m_cStackIPOT;
    ArrayList<IPOGTrackNode> m_cvSelectedIPOGTracks;
    public IPOGTrackSelectionChoiceForm() {
        initComponents();
        m_cvSelectedIPOGTracks=new ArrayList();
        CommonGuiMethods.makeClipboardCopiable(this);
    }
    public int updateIPOGTSelectionChoice(StackIPOGTracksNode cStackIPOT, String plottingOption){
        m_cStackIPOT=cStackIPOT;
        m_cStackIPOT.calParRanges();
        Height1TF.setText(PrintAssist.ToString(m_cStackIPOT.cHeightRange.getMin(), 1));
        Height2TF.setText(PrintAssist.ToString(m_cStackIPOT.cHeightRange.getMax(), 1));
        TotalSignal1TF.setText(PrintAssist.ToString(m_cStackIPOT.cTotalSignalRange.getMin(), 1));
        TotalSignal2TF.setText(PrintAssist.ToString(m_cStackIPOT.cTotalSignalRange.getMax(), 1));
        hwRatioITF.setText(PrintAssist.ToString(m_cStackIPOT.cHWRatioRange.getMin(), 1));
        hwRatioFTF.setText(PrintAssist.ToString(m_cStackIPOT.cHWRatioRange.getMax(), 1));
        ClusterSize1TF.setText(PrintAssist.ToString(m_cStackIPOT.cClusterSizeRange.getMin(), 0));
        ClusterSize2TF.setText(PrintAssist.ToString(m_cStackIPOT.cClusterSizeRange.getMax(), 0));
        FirstTrackIdTF.setText(PrintAssist.ToString(m_cStackIPOT.cTrackIndexRange.getMin(), 0));
        LastTrackIdTF.setText(PrintAssist.ToString(m_cStackIPOT.cTrackIndexRange.getMax(), 0));
        FirstSliceIndexITF.setText(PrintAssist.ToString(m_cStackIPOT.cFirstSliceIndexRange.getMin(), 0));
        FirstSliceIndexFTF.setText(PrintAssist.ToString(m_cStackIPOT.cFirstSliceIndexRange.getMax(), 0));
        AreaITF.setText(PrintAssist.ToString(m_cStackIPOT.cIPOAreaRange.getMin(), 0));
        AreaFTF.setText(PrintAssist.ToString(m_cStackIPOT.cIPOAreaRange.getMax(), 0));
        BundleSizeITF.setText(PrintAssist.ToString(m_cStackIPOT.cBundleSizeRange.getMin(), 0));
        BundleSizeFTF.setText(PrintAssist.ToString(m_cStackIPOT.cBundleSizeRange.getMax(), 0));
        BundleIndexITF.setText(PrintAssist.ToString(m_cStackIPOT.cBundleIndexRange.getMin(), 0));
        BundleIndexFTF.setText(PrintAssist.ToString(m_cStackIPOT.cBundleIndexRange.getMax(), 0));
        TLengthITF.setText(PrintAssist.ToString(m_cStackIPOT.cTrackLengthRange.getMin(), 0));
        TLengthFTF.setText(PrintAssist.ToString(m_cStackIPOT.cTrackLengthRange.getMax(), 0));
        Complexity1TF.setText(PrintAssist.ToString(m_cStackIPOT.cComplexityRange.getMin(), 0));
        Complexity2TF.setText(PrintAssist.ToString(m_cStackIPOT.cComplexityRange.getMax(), 0));
        HeadValue1TF.setText(PrintAssist.ToString(m_cStackIPOT.cHeadValueRange.getMin(), 1));
        HeadValue2TF.setText(PrintAssist.ToString(m_cStackIPOT.cHeadValueRange.getMax(), 1));
        return 1;
    }
    int selectTracks(String plottingOption){
        if(m_cStackIPOT==null) return -1;
        m_cStackIPOT.calParRanges();
        m_cvSelectedIPOGTracks.clear();
        int i,len=m_cStackIPOT.m_cvIPOGTracks.size();
        IPOGTrackNode IPOGT;
        DoubleRange HR=new DoubleRange(Double.parseDouble(Height1TF.getText()),Double.parseDouble(Height2TF.getText()));
        DoubleRange TR=new DoubleRange(Double.parseDouble(TotalSignal1TF.getText()),Double.parseDouble(TotalSignal2TF.getText()));
        intRange CR=new intRange(Integer.parseInt(ClusterSize1TF.getText()),Integer.parseInt(ClusterSize2TF.getText()));
        intRange FR=new intRange(Integer.parseInt(FirstTrackIdTF.getText()),Integer.parseInt(LastTrackIdTF.getText()));
        intRange BR=new intRange(Integer.parseInt(BundleSizeITF.getText()),Integer.parseInt(BundleSizeFTF.getText()));
        intRange LR=new intRange(Integer.parseInt(TLengthITF.getText()),Integer.parseInt(TLengthFTF.getText()));
        intRange AR=new intRange(Integer.parseInt(AreaITF.getText()),Integer.parseInt(AreaFTF.getText()));
        DoubleRange RR=new DoubleRange(Double.parseDouble(hwRatioITF.getText()),Double.parseDouble(hwRatioFTF.getText()));
        intRange BIR=new intRange(Integer.parseInt(BundleIndexITF.getText()),Integer.parseInt(BundleIndexFTF.getText()));
        DoubleRange TDR=new DoubleRange(Double.parseDouble(TotalDriftITF.getText()),Double.parseDouble(TotalDriftFTF.getText()));
        DoubleRange SDR=new DoubleRange(Double.parseDouble(StepDriftITF.getText()),Double.parseDouble(StepDriftFTF.getText()));
        DoubleRange HVR=new DoubleRange(Double.parseDouble(HeadValue1TF.getText()),Double.parseDouble(HeadValue2TF.getText()));
        intRange FSR=new intRange(Integer.parseInt(FirstSliceIndexITF.getText()),Integer.parseInt(FirstSliceIndexFTF.getText()));
        intRange CXR=new intRange(Integer.parseInt(Complexity1TF.getText()),Integer.parseInt(Complexity2TF.getText()));
        int firstSlice=m_cStackIPOT.sliceI;
        for(i=0;i<len;i++){
            IPOGT=m_cStackIPOT.m_cvIPOGTracks.get(i);
            if(HeightRB.isSelected()&&!HR.contains(IPOGT.getValue(firstSlice,plottingOption))) continue;
            if(FirstSliceIndexRB.isSelected()&&!FSR.contains(IPOGT.firstSlice)) continue;
            if(TotalSignalRB.isSelected()&&!TR.contains(IPOGT.cTotalSignalRange.getMax())) continue;
            if(ClusterSizeRB.isSelected()&&!CR.contains(IPOGT.cClusterSizeRange)) continue;
            if(TrackIdRB.isSelected()&&!FR.contains(IPOGT.m_cvIPOGs.get(0).TrackIndex)) continue;
            if(BundleSizeRB.isSelected()&&!BR.contains(IPOGT.BundleSize)) continue;
            if(TLengthRB.isSelected()&&!LR.contains(IPOGT.lastSlice-IPOGT.firstSlice+1)) continue;
            if(AreaRB.isSelected()&&!AR.contains(IPOGT.cAreaRange.getMax())) continue;
            if(HeightWidthRatioRB.isSelected()&&!RR.contains(IPOGT.cHeightWidthRatioRange)) continue;
            if(BundleIndexRB.isSelected()&&!BIR.contains(IPOGT.BundleIndex)) continue;
            if(TotalDriftRB.isSelected()&&!TDR.contains(IPOGT.cXCRange.getRange()+IPOGT.cYCRange.getRange())) continue;
            if(StepDriftRB.isSelected()&&!SDR.contains(IPOGT.cDriftRange.getMax())) continue;
            if(ComplexityRB.isSelected()&&!CXR.contains(IPOGT.m_nComplexity)) continue;
            if(HeadValueRB.isSelected()&&!HVR.contains(IPOGT.getHeadValue(plottingOption))) continue;
            m_cvSelectedIPOGTracks.add(IPOGT);
        }
        return 1;
    }
    ArrayList<IPOGTrackNode> getSelectedTracks(){
        return m_cvSelectedIPOGTracks;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel13 = new javax.swing.JLabel();
        LastTrackIdTF = new javax.swing.JTextField();
        TrackIdRB = new javax.swing.JRadioButton();
        FirstTrackIdTF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        TotalSignal1TF = new javax.swing.JTextField();
        ClusterSizeRB = new javax.swing.JRadioButton();
        ClusterSize1TF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        TotalSignalRB = new javax.swing.JRadioButton();
        Height2TF = new javax.swing.JTextField();
        Height1TF = new javax.swing.JTextField();
        HeightRB = new javax.swing.JRadioButton();
        ClusterSize2TF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        TotalSignal2TF = new javax.swing.JTextField();
        BundleSizeRB = new javax.swing.JRadioButton();
        BundleSizeITF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        BundleSizeFTF = new javax.swing.JTextField();
        AreaRB = new javax.swing.JRadioButton();
        AreaITF = new javax.swing.JTextField();
        AreaFTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        HeightWidthRatioRB = new javax.swing.JRadioButton();
        hwRatioITF = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        hwRatioFTF = new javax.swing.JTextField();
        TLengthRB = new javax.swing.JRadioButton();
        TLengthITF = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        TLengthFTF = new javax.swing.JTextField();
        FirstSliceIndexRB = new javax.swing.JRadioButton();
        FirstSliceIndexITF = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        FirstSliceIndexFTF = new javax.swing.JTextField();
        BundleIndexRB = new javax.swing.JRadioButton();
        BundleIndexITF = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        BundleIndexFTF = new javax.swing.JTextField();
        TotalDriftRB = new javax.swing.JRadioButton();
        TotalDriftITF = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        TotalDriftFTF = new javax.swing.JTextField();
        StepDriftRB = new javax.swing.JRadioButton();
        StepDriftITF = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        StepDriftFTF = new javax.swing.JTextField();
        ComplexityRB = new javax.swing.JRadioButton();
        Complexity1TF = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        Complexity2TF = new javax.swing.JTextField();
        HeadValueRB = new javax.swing.JRadioButton();
        HeadValue1TF = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        HeadValue2TF = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel13.setText("to");

        LastTrackIdTF.setText("300");

        TrackIdRB.setText("TrackId");

        FirstTrackIdTF.setText("0");

        jLabel4.setText("to");

        TotalSignal1TF.setText("800");

        ClusterSizeRB.setText("cluster size");

        ClusterSize1TF.setText("0");
        ClusterSize1TF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClusterSize1TFActionPerformed(evt);
            }
        });

        jLabel2.setText("to");

        TotalSignalRB.setText("total signal");

        Height2TF.setText("999999");

        Height1TF.setText("100");

        HeightRB.setText("height");

        ClusterSize2TF.setText("10000");

        jLabel3.setText("to");

        TotalSignal2TF.setText("99999");
        TotalSignal2TF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TotalSignal2TFActionPerformed(evt);
            }
        });

        BundleSizeRB.setText("Bundle Size");

        BundleSizeITF.setText("0");

        jLabel1.setText("to");

        BundleSizeFTF.setText("10");

        AreaRB.setText("Area");

        AreaITF.setText("100");

        AreaFTF.setText("500");

        jLabel5.setText("to");

        HeightWidthRatioRB.setText("h/w Ratio");

        hwRatioITF.setText("0.5");

        jLabel6.setText("to");

        hwRatioFTF.setText("2");

        TLengthRB.setText("Track Length");

        TLengthITF.setText("20");

        jLabel7.setText("to");

        TLengthFTF.setText("500");

        FirstSliceIndexRB.setText("First Slice");

        FirstSliceIndexITF.setText("10");

        jLabel8.setText("to");

        FirstSliceIndexFTF.setText("500");

        BundleIndexRB.setText("BundleId");

        BundleIndexITF.setText("10 ");

        jLabel9.setText("to");

        BundleIndexFTF.setText("1000");

        TotalDriftRB.setText("Total Drift");

        TotalDriftITF.setText("20");

        jLabel10.setText("to");

        TotalDriftFTF.setText("30");

        StepDriftRB.setText("Step Drift");

        StepDriftITF.setText("0");

        jLabel11.setText("to");

        StepDriftFTF.setText("10");

        ComplexityRB.setText("Complexity");

        Complexity1TF.setText("5");

        jLabel12.setText("to");

        Complexity2TF.setText("15");

        HeadValueRB.setText("Head Value");

        HeadValue1TF.setText("10000");
        HeadValue1TF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HeadValue1TFActionPerformed(evt);
            }
        });

        jLabel14.setText("to");

        HeadValue2TF.setText("10000");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(StepDriftRB)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ComplexityRB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(HeightWidthRatioRB)
                                    .addComponent(AreaRB)
                                    .addComponent(BundleIndexRB)
                                    .addComponent(TotalDriftRB))
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(BundleIndexITF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                                        .addComponent(TotalDriftITF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                                        .addComponent(StepDriftITF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                                        .addComponent(Complexity1TF, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(hwRatioITF)
                                        .addComponent(AreaITF, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)))))
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(hwRatioFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                                    .addComponent(AreaFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(StepDriftFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TotalDriftFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BundleIndexFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Complexity2TF, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TotalSignalRB)
                            .addComponent(HeightRB)
                            .addComponent(TrackIdRB)
                            .addComponent(ClusterSizeRB)
                            .addComponent(BundleSizeRB)
                            .addComponent(TLengthRB)
                            .addComponent(FirstSliceIndexRB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(FirstSliceIndexITF)
                            .addComponent(TLengthITF)
                            .addComponent(BundleSizeITF)
                            .addComponent(FirstTrackIdTF, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(ClusterSize1TF, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(TotalSignal1TF, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(Height1TF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel4)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel13)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel2)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(ClusterSize2TF, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(LastTrackIdTF, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(TotalSignal2TF, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(Height2TF, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(BundleSizeFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(FirstSliceIndexFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(TLengthFTF, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(HeadValueRB)
                        .addGap(14, 14, 14)
                        .addComponent(HeadValue1TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(HeadValue2TF, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(HeadValueRB)
                    .addComponent(HeadValue1TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(HeadValue2TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Height2TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(TotalSignal2TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(LastTrackIdTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(176, 176, 176)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(FirstSliceIndexFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(Height1TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(TotalSignal1TF, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(ClusterSize1TF, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ClusterSizeRB)
                                    .addComponent(jLabel4)
                                    .addComponent(ClusterSize2TF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(FirstTrackIdTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(TotalSignalRB))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(HeightRB)
                                .addGap(62, 62, 62)
                                .addComponent(TrackIdRB)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(BundleSizeRB)
                            .addComponent(BundleSizeITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(BundleSizeFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TLengthRB)
                            .addComponent(TLengthITF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(TLengthFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(FirstSliceIndexRB)
                            .addComponent(FirstSliceIndexITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AreaRB)
                    .addComponent(AreaITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(AreaFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(HeightWidthRatioRB)
                    .addComponent(hwRatioITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(hwRatioFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BundleIndexRB)
                    .addComponent(BundleIndexITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(BundleIndexFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TotalDriftRB)
                    .addComponent(TotalDriftITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(TotalDriftFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(StepDriftRB)
                    .addComponent(StepDriftITF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(StepDriftFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ComplexityRB)
                    .addComponent(Complexity1TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(Complexity2TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ClusterSize1TFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClusterSize1TFActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_ClusterSize1TFActionPerformed

    private void TotalSignal2TFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TotalSignal2TFActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_TotalSignal2TFActionPerformed

    private void HeadValue1TFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HeadValue1TFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_HeadValue1TFActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IPOGTrackSelectionChoiceForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AreaFTF;
    private javax.swing.JTextField AreaITF;
    private javax.swing.JRadioButton AreaRB;
    private javax.swing.JTextField BundleIndexFTF;
    private javax.swing.JTextField BundleIndexITF;
    private javax.swing.JRadioButton BundleIndexRB;
    private javax.swing.JTextField BundleSizeFTF;
    private javax.swing.JTextField BundleSizeITF;
    private javax.swing.JRadioButton BundleSizeRB;
    private javax.swing.JTextField ClusterSize1TF;
    private javax.swing.JTextField ClusterSize2TF;
    private javax.swing.JRadioButton ClusterSizeRB;
    private javax.swing.JTextField Complexity1TF;
    private javax.swing.JTextField Complexity2TF;
    private javax.swing.JRadioButton ComplexityRB;
    private javax.swing.JTextField FirstSliceIndexFTF;
    private javax.swing.JTextField FirstSliceIndexITF;
    private javax.swing.JRadioButton FirstSliceIndexRB;
    private javax.swing.JTextField FirstTrackIdTF;
    private javax.swing.JTextField HeadValue1TF;
    private javax.swing.JTextField HeadValue2TF;
    private javax.swing.JRadioButton HeadValueRB;
    private javax.swing.JTextField Height1TF;
    private javax.swing.JTextField Height2TF;
    private javax.swing.JRadioButton HeightRB;
    private javax.swing.JRadioButton HeightWidthRatioRB;
    private javax.swing.JTextField LastTrackIdTF;
    private javax.swing.JTextField StepDriftFTF;
    private javax.swing.JTextField StepDriftITF;
    private javax.swing.JRadioButton StepDriftRB;
    private javax.swing.JTextField TLengthFTF;
    private javax.swing.JTextField TLengthITF;
    private javax.swing.JRadioButton TLengthRB;
    private javax.swing.JTextField TotalDriftFTF;
    private javax.swing.JTextField TotalDriftITF;
    private javax.swing.JRadioButton TotalDriftRB;
    private javax.swing.JTextField TotalSignal1TF;
    private javax.swing.JTextField TotalSignal2TF;
    private javax.swing.JRadioButton TotalSignalRB;
    private javax.swing.JRadioButton TrackIdRB;
    private javax.swing.JTextField hwRatioFTF;
    private javax.swing.JTextField hwRatioITF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables

}
