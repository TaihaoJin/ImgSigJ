/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PixelGrabberForm.java
 *
 * Created on Jul 18, 2011, 11:19:31 AM
 */

package utilities.Gui;
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
import java.awt.event.ActionListener;

/**
 *
 * @author Taihao
 */
public class PixelGrabberForm extends javax.swing.JFrame{
    RegionBoundaryAnalyzer m_cRBA;
    ImagePlus impl;
    boolean validRBA;
    double[][] m_pdX;
    double[] m_pdY;
    int[][] pixels,stamp;
    int w,h;
    ImageShape m_cIS;
    ArrayList<RegionNode> m_cvRegionNodes;
    ArrayList<RegionComplexNode> m_cvComplexNodes;
    ArrayList<ImageShape> m_cvRegionShapes;
    ArrayList<ImageShape> m_cvComplexShapes;
    ArrayList<PixelGrabberReceiver> m_cvReceivers;
    Roi m_cRoi;
    String choice0;

    /** Creates new form PixelGrabberForm */
    public PixelGrabberForm() {
        initComponents();
        choice0="ini";
        GrabPixelsBT.setActionCommand("PixelsGrabbed");
        m_cvReceivers=new ArrayList();
    }

    public PixelGrabberForm(ActionListener parent) {
        this();
//        GrabPixelsBT.addActionListener(parent);
    }
    public void addReceiver(PixelGrabberReceiver receiver){
        m_cvReceivers.add(receiver);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        PixelSourceCB = new javax.swing.JComboBox();
        SetCurrentImageBT = new javax.swing.JButton();
        ROIChoiceCB = new javax.swing.JComboBox();
        MakeRegionBT = new javax.swing.JButton();
        FinishBT = new javax.swing.JButton();
        GrabPixelsBT = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        ComplexThresholdTF = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Pixel Source");

        PixelSourceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ROI", "Entire Image" }));

        SetCurrentImageBT.setText("Set Current Image");
        SetCurrentImageBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetCurrentImageBTActionPerformed(evt);
            }
        });

        ROIChoiceCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ROI", "Landscape Region", "Landscape Complex" }));

        MakeRegionBT.setText("Make ROI");
        MakeRegionBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MakeRegionBTActionPerformed(evt);
            }
        });

        FinishBT.setText("finish");
        FinishBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FinishBTActionPerformed(evt);
            }
        });

        GrabPixelsBT.setText("Grab Pixels");
        GrabPixelsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GrabPixelsBTActionPerformed(evt);
            }
        });

        jLabel2.setText("Complex Threshold");

        ComplexThresholdTF.setText("0.01");

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ComplexThresholdTF, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PixelSourceCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(20, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FinishBT)
                            .addComponent(GrabPixelsBT))
                        .addGap(38, 38, 38))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(MakeRegionBT))
                    .addComponent(SetCurrentImageBT))
                .addContainerGap(209, Short.MAX_VALUE))
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
                .addGap(40, 40, 40)
                .addComponent(SetCurrentImageBT)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MakeRegionBT)
                    .addComponent(FinishBT))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SetCurrentImageBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetCurrentImageBTActionPerformed
        impl=CommonMethods.getCurrentImage();// TODO add your handling code here:
        if(pixels==null){
            pixels=CommonMethods.getPixelValues(impl);
            h=pixels.length;
            w=pixels[0].length;
        }else{
            if(pixels.length!=h||pixels[0].length!=w)
                pixels=CommonMethods.getPixelValues(impl);
            else
                CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
        }
        validRBA=false;
        m_cRoi=null;
        m_cIS=null;
}//GEN-LAST:event_SetCurrentImageBTActionPerformed

    private void MakeRegionBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MakeRegionBTActionPerformed
        // TODO add your handling code here:
        makeRoi();
}//GEN-LAST:event_MakeRegionBTActionPerformed

    private void FinishBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FinishBTActionPerformed
        //        SwingUtilities.getWindowAncestor(this).dispose();
        this.dispose();
}//GEN-LAST:event_FinishBTActionPerformed

    private void GrabPixelsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GrabPixelsBTActionPerformed
        grabPixels();
        int len=m_cvReceivers.size(),i;
        for(i=0;i<len;i++){
            m_cvReceivers.get(i).grabPixels(this);
        }
//        notifyAll();
    }//GEN-LAST:event_GrabPixelsBTActionPerformed
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
        if(validChoice){
            if(!validRBA){
                analyzeLandScape();
                validRBA=true;
            }
            ImageShape cIS;
            PointRoi pr=(PointRoi)aRoi;
            int[] pnX=pr.getXCoordinates(),pnY=pr.getYCoordinates();
            Point po=aRoi.getBounds().getLocation();
            int x=po.x,y=po.y;

            if(choice.contentEquals(choice0)||choice0.contentEquals("Landscape Complext")){
                if(m_cIS!=null){
                    if(m_cIS.contains(po)) {
                        choice="bye pass";
                    }else{
                        m_cRoi=null;
                        choice0=choice;
                    }
                }
            }else{
                choice0=choice;
            }
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
            }else if(choice.contentEquals("Landscape Complex")){
                len=m_cvComplexShapes.size();
                for(i=0;i<len;i++){
                    cIS=m_cvComplexShapes.get(i);
                    if(cIS.contains(x,y)){
                        m_cIS=cIS;
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
    int grabPixels(){
        String choice=(String)PixelSourceCB.getSelectedItem();
        int i,j,len,o,index;
        if(choice.contentEquals("Entire Image")){
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
        if(choice.contentEquals("Landscape Region")||choice.contentEquals("Landscape Complex")){
            makeRoi();
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

    public ImagePlus getOriginalImage(){
        return impl;
    }
    
    public int[][] getOriginalPixels(){
        return pixels;
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
                final PixelGrabberForm form = new PixelGrabberForm();
                form.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
 //                       SwingUtilities.getWindowAncestor(dialog).dispose();
                        form.dispose();
                    }
                });
                form.setVisible(true);
            }
        });
    }
    /**
    * @param args the command line arguments
    */

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

}
