/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PixelGrabberDlg.java
 *
 * Created on Jul 16, 2011, 1:24:33 PM
 */

package utilities.io;
import java.awt.*;
import javax.swing.*;
import utilities.Non_LinearFitting.ImageFittingGUI;
import ImageAnalysis.RegionBoundaryAnalyzer;
import ij.ImagePlus;
import utilities.CommonMethods;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.CustomDataTypes.intRange;
import utilities.CommonStatisticsMethods;
import utilities.statistics.Histogram;
import java.util.ArrayList;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import ImageAnalysis.RegionNode;
import ImageAnalysis.RegionComplexNode;
import ij.gui.Roi;
import ij.gui.PointRoi;
import utilities.CommonGuiMethods;
/**
 *
 * @author Taihao
 */
public class PixelGrabberDlg extends javax.swing.JDialog {
    RegionBoundaryAnalyzer m_cRBA;
    ImagePlus impl;
    boolean validRBA;
    double[][] m_pdX;
    double[] m_pdY;
    int[][] pixels,stamp;
    ImageShape m_cIS;
    ArrayList<RegionNode> m_cvRegionNodes;
    ArrayList<RegionComplexNode> m_cvComplexNodes;
    ArrayList<ImageShape> m_cvRegionShapes;
    ArrayList<ImageShape> m_cvComplexShapes;
    Roi m_cRoi;
    /** Creates new form PixelGrabberDlg */
    public PixelGrabberDlg(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        GrabPixelsBT.addActionListener((ImageFittingGUI)parent);
        GrabPixelsBT.setActionCommand("PixelsGrabbed");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PixelSourceCB = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        GrabPixelsBT = new javax.swing.JButton();
        FinishBT = new javax.swing.JButton();
        MakeRegionBT = new javax.swing.JButton();
        ROIChoiceCB = new javax.swing.JComboBox();
        ComplexThresholdTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        SetCurrentImageBT = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pixel Grabber");
        setModalExclusionType(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        PixelSourceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Entire Image", "ROI" }));

        jLabel1.setText("Pixel Source");

        GrabPixelsBT.setText("Grab Pixels");
        GrabPixelsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GrabPixelsBTActionPerformed(evt);
            }
        });

        FinishBT.setText("finish");
        FinishBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FinishBTActionPerformed(evt);
            }
        });

        MakeRegionBT.setText("Make ROI");
        MakeRegionBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MakeRegionBTActionPerformed(evt);
            }
        });

        ROIChoiceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Landscape Region", "Landscape Complex", "ROI" }));

        ComplexThresholdTF.setText("0.01");

        jLabel2.setText("Complex Threshold");

        SetCurrentImageBT.setText("Set Current Image");
        SetCurrentImageBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetCurrentImageBTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(ROIChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(ComplexThresholdTF, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PixelSourceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(20, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(GrabPixelsBT)
                        .addGap(38, 38, 38))))
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(MakeRegionBT)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 157, Short.MAX_VALUE)
                .addComponent(FinishBT)
                .addGap(50, 50, 50))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(SetCurrentImageBT)
                .addContainerGap(242, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PixelSourceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(ROIChoiceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(ComplexThresholdTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(GrabPixelsBT))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(SetCurrentImageBT)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MakeRegionBT)
                    .addComponent(FinishBT))
                .addGap(24, 24, 24))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void FinishBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FinishBTActionPerformed
//        SwingUtilities.getWindowAncestor(this).dispose();
        this.dispose();
    }//GEN-LAST:event_FinishBTActionPerformed

    private void MakeRegionBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MakeRegionBTActionPerformed
        // TODO add your handling code here:
        makeRoi();
    }//GEN-LAST:event_MakeRegionBTActionPerformed
    void makeRoi(){
        String choice=(String) ROIChoiceCB.getSelectedItem();
        boolean validChoice=true;
        if(!choice.contentEquals("Landscape Region")&&!choice.contentEquals("Landscape Complex")) validChoice=false;
        Roi aRoi=impl.getRoi();
        if(aRoi!=null){
            if(!(aRoi instanceof PointRoi)) validChoice=false;
        }else{
            validChoice=false;
        }
        m_cRoi=null;
        if(validChoice){
            if(!validRBA){
                analyzeLandScape();
            }
            ImageShape cIS;
            PointRoi pr=(PointRoi)aRoi;
            int[] pnX=pr.getXCoordinates(),pnY=pr.getYCoordinates();
            Point po=aRoi.getBounds().getLocation();
            int x=po.x,y=po.y;

            int len,i;
            if(choice.contentEquals("Landscape Region")){
                len=m_cvRegionShapes.size();
                for(i=0;i<len;i++){
                    cIS=m_cvRegionShapes.get(i);
                    if(cIS.contains(x,y)){
                        m_cIS=cIS;
                        m_cRoi=ImageShapeHandler.getRoi(cIS);
                        break;
                    }
                }
            }else{
                len=m_cvComplexShapes.size();
                for(i=0;i<len;i++){
                    cIS=m_cvComplexShapes.get(i);
                    if(cIS.contains(x,y)){
                        m_cRoi=ImageShapeHandler.getRoi(cIS);
                        break;
                    }
                }
            }
        }
        if(m_cRoi!=null){
            CommonGuiMethods.highlightRoi(impl, m_cRoi, Color.red);
        }
    }
    private void SetCurrentImageBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetCurrentImageBTActionPerformed
        impl=CommonMethods.getCurrentImage();// TODO add your handling code here:
        validRBA=false;
        m_cRoi=null;
        m_cIS=null;
    }//GEN-LAST:event_SetCurrentImageBTActionPerformed

    private void GrabPixelsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GrabPixelsBTActionPerformed
        grabPixels();
        notifyAll();
        
    }//GEN-LAST:event_GrabPixelsBTActionPerformed
    int grabPixels(){
        String choice=(String)PixelSourceCB.getSelectedItem();
        int i,j,len,o,index;
        if(choice.contentEquals("EntireImage")){
            int w=impl.getWidth(),h=impl.getHeight();
            len=w*h;
            m_pdX=new double[len][2];
            m_pdY=new double[len];
            pixels=CommonMethods.getPixelValues(impl);
            for(i=0;i<h;i++){
                o=i*w;
                for(j=0;j<w;j++){
                    index=o+j;
                    m_pdX[index][0]=j;
                    m_pdX[index][1]=i;
                    m_pdY[index]=pixels[i][j];
                }
            }
            return 1;
        }
        //pixel source choice is a ROI
        choice=(String)ROIChoiceCB.getSelectedItem();
        if(!choice.contentEquals("Landscape Region")&&!choice.contentEquals("Landscape Complex")){
            if(!validRBA){
                makeRoi();
            }
        }else{//roi choice is a pre made ROI in impl
            m_cRoi=impl.getRoi();
            m_cIS=ImageShapeHandler.buildImageShape(m_cRoi);
        }
        ArrayList<Point> points=new ArrayList();
        m_cIS.getInnerPoints(points);
        len=points.size();
        m_pdX=new double[len][2];
        m_pdY=new double[len];
        Point p;
        for(i=0;i<len;i++){
            p=points.get(i);
            m_pdX[i][0]=p.x;
            m_pdX[i][1]=p.y;
            m_pdY[i]=pixels[p.y][p.x];
        }
        return 1;
    }
    void analyzeLandScape(){
        pixels=CommonMethods.getPixelValues(impl);
        int w=pixels[0].length,h=pixels.length;
        stamp=new int[h][w];
        intRange ir=CommonStatisticsMethods.getRange(pixels);
        int[] pixelRange={ir.getMin(),ir.getMax()};
        int[][] pixelsr=CommonStatisticsMethods.copyArray(pixels);
        CommonMethods.randomize(pixelsr);
        LandscapeAnalyzerPixelSorting la=new LandscapeAnalyzerPixelSorting(w,h,pixelRange);
        la.updateAndStampPixels(pixelsr, stamp);
        m_cRBA=new RegionBoundaryAnalyzer(stamp);
        Histogram hist=m_cRBA.getBorderSegmentHeightHistogram(pixelsr);
        double cutoff=Double.parseDouble(ComplexThresholdTF.getText());
        hist.setPercentile(1-cutoff);
        cutoff=hist.getPercentileValue();

        la.updateAndStampPixels(pixels, stamp);
        m_cRBA=new RegionBoundaryAnalyzer(stamp);
        m_cRBA.removeOverheightBorderSegments(pixels, cutoff);
        m_cvRegionNodes=m_cRBA.getRegionNodes();
        m_cvComplexNodes=m_cRBA.getComplexNodes();

        int len=m_cvRegionNodes.size(),i;
        m_cvRegionShapes=new ArrayList();
        for(i=0;i<len;i++){
            m_cvRegionShapes.add(m_cRBA.getRegionShape(m_cvRegionNodes.get(i)));
        }

        len=m_cvComplexNodes.size();
        m_cvComplexShapes=new ArrayList();
        for(i=0;i<len;i++){
            m_cvComplexShapes.add(m_cRBA.getRegionComplexShape(m_cvComplexNodes.get(i)));
        }
        validRBA=true;
    }
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                final PixelGrabberDlg dialog = new PixelGrabberDlg(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
 //                       SwingUtilities.getWindowAncestor(dialog).dispose();
                        dialog.dispose();
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ComplexThresholdTF;
    private javax.swing.JButton FinishBT;
    private javax.swing.JButton GrabPixelsBT;
    private javax.swing.JButton MakeRegionBT;
    private javax.swing.JComboBox PixelSourceCB;
    private javax.swing.JComboBox ROIChoiceCB;
    private javax.swing.JButton SetCurrentImageBT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
    public double[][] getPixelCoordinates(){
        return m_pdX;
    }
    public double [] getPixels(){
        return m_pdY;
    }
    public void fireActinPerformed(){

    }
}