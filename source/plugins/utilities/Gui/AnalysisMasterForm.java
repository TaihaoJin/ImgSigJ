/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AnalysisMasterForm.java
 *
 * Created on Sep 22, 2011, 4:51:07 PM
 */

package utilities.Gui;
import utilities.Gui.ImageComparisonViewer;
import FluoObjects.IPOAnalyzerForm;
import ImageAnalysis.Common_AnalysisForm;
//import utilities.Non_LinearFitting.ImageFittingGUI;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Rectangle;
import utilities.Gui.DragMoverListener;
import java.awt.Point;
import utilities.Gui.OneSchrollPaneFrame;
import java.util.ArrayList;
import utilities.io.PrintAssist;
import ij.ImagePlus;
import utilities.CommonGuiMethods;
/**
 *
 * @author Taihao
 */
public class AnalysisMasterForm extends javax.swing.JFrame implements MouseMotionListener,MouseListener
 {
    public static OneSchrollPaneFrame cMessageFrame;
    public static ArrayList<ImagePlus> cvTempImages;
    ImageComparisonViewer m_cComparisonViewer;
    Common_AnalysisForm m_cCommonAnalysisForm;
    IPOAnalyzerForm m_cIPOAnalyzer;
    MasterFittingGUI m_cFitter;
    Point MouseStart, move;
    Rectangle IPOAnalysisPaneSize;
    static public JTextArea cMessageArea;

    /** Creates new form AnalysisMasterForm */
    public AnalysisMasterForm() {
        initComponents();
        completeForm();
        CommonGuiMethods.makeClipboardCopiable(this);
    }
    void completeForm(){
        if(cvTempImages==null) cvTempImages=new ArrayList();
        m_cComparisonViewer=new ImageComparisonViewer();
        m_cCommonAnalysisForm=new Common_AnalysisForm();
        m_cIPOAnalyzer=new IPOAnalyzerForm();
        m_cFitter=new MasterFittingGUI();

        m_cFitter.setMasterForm(this);
        m_cCommonAnalysisForm.setMasterForm(this);
        m_cIPOAnalyzer.setMasterForm(this);
        m_cComparisonViewer.setMasterForm(this);
        
        JViewport commonAnalysisPort=new JViewport();
        commonAnalysisPort.setView(m_cCommonAnalysisForm.getContentPane());
        CommonAnalysisPane.setViewport(commonAnalysisPort);

        JViewport ComparisonPort=new JViewport();
        ComparisonPort.setView(m_cComparisonViewer.getContentPane());
        ComparisonPane.setViewport(ComparisonPort);
        
        JViewport FittingPort=new JViewport();
        FittingPort.setView(m_cFitter.getContentPane());
        FittingPane.setViewport(FittingPort);
        
        JViewport IPOPort=new JViewport();
        IPOPort.setView(m_cIPOAnalyzer.getContentPane());
        IPOAnalysisPaneSize=m_cIPOAnalyzer.getContentPane().getBounds();
        IPOAnalysisPane.setViewport(IPOPort);
        IPOAnalysisPane.addMouseMotionListener(this);
        IPOAnalysisPane.addMouseListener(this);

        setAlwaysOnTop(false);
//        JComponent comp;
//        DragMoverListener IPOMover=new DragMoverListener(IPOPort);
    }

    public ImageComparisonViewer getComparisonViewer(){
        return m_cComparisonViewer;
    }
    public Common_AnalysisForm getCommonAnalysisForm(){
        return m_cCommonAnalysisForm;
    }
    public IPOAnalyzerForm getIPOAnalyzer(){
        return m_cIPOAnalyzer;
    }
    public MasterFittingGUI getFittingGUI(){
        return m_cFitter;
    }
    public static int showMessageFrame(){
        if(cMessageFrame==null) cMessageFrame=new OneSchrollPaneFrame();
        cMessageArea=new JTextArea();
        JViewport jvp=new JViewport();
        jvp.setView(cMessageArea);
        cMessageFrame.getPane().setViewport(jvp);        
        return 1;
    }
    public static void appendText(String text){
        if(cMessageFrame==null) showMessageFrame();
        if(!cMessageFrame.isVisible()) cMessageFrame.setVisible(true);
        cMessageArea.append(text);
        
    }

    public static void setText(String text){
        if(cMessageFrame==null) showMessageFrame();
        if(!cMessageFrame.isVisible()) cMessageFrame.setVisible(true);
        cMessageArea.setText(text);
        
    }

    public static int appendStrings(ArrayList<String> strings){
        if(strings==null) return -1;
        int i,len=strings.size();
        for(i=0;i<len;i++){
            appendText(strings.get(i));           
        }
        return 1;
    }
    public static void newLine(){
        appendText(PrintAssist.newline);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        MasterPane = new javax.swing.JTabbedPane();
        CommonAnalysisPane = new javax.swing.JScrollPane();
        ComparisonPane = new javax.swing.JScrollPane();
        IPOAnalysisPane = new javax.swing.JScrollPane();
        FittingPane = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Analysis");
        setAlwaysOnTop(true);

        MasterPane.addTab("Common Analysis", CommonAnalysisPane);
        MasterPane.addTab("Comparison Viewer", ComparisonPane);
        MasterPane.addTab("IPO Analyzer", IPOAnalysisPane);
        MasterPane.addTab("Fitting Gui", FittingPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MasterPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MasterPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if(GeneralManager.MasterForm==null){
                    GeneralManager.MasterForm=new AnalysisMasterForm();
                    GeneralManager.MasterForm.setVisible(true);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane CommonAnalysisPane;
    private javax.swing.JScrollPane ComparisonPane;
    private javax.swing.JScrollPane FittingPane;
    private javax.swing.JScrollPane IPOAnalysisPane;
    private javax.swing.JTabbedPane MasterPane;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    // End of variables declaration//GEN-END:variables
    public void mouseMoved(MouseEvent e){

    }
    public void mouseDragged(MouseEvent e){
        if(e.getSource()==IPOAnalysisPane){
            int dx,dy,newX,newY;
            JViewport vp=IPOAnalysisPane.getViewport();
            Rectangle portSize=vp.getBounds(),viewSize=vp.getVisibleRect();
            Point pt=e.getLocationOnScreen();
            dx=MouseStart.x-pt.x;
            dy=MouseStart.y-pt.y;
            
            Point viewPoint=IPOAnalysisPane.getViewport().getViewPosition();
            viewPoint.translate(dx, dy);
            newX=viewPoint.x;
            newY=viewPoint.y;

            if(newX<0) newX=0;
            if(newY<0) newY=0;
            if(newX>IPOAnalysisPaneSize.width-viewSize.width) newX=IPOAnalysisPaneSize.width-viewSize.width;
            if(newY>IPOAnalysisPaneSize.height-viewSize.height) newY=IPOAnalysisPaneSize.height-viewSize.height;
            viewPoint.setLocation(newX, newY);
            vp.setViewPosition(viewPoint);
//            IPOAnalysisPane.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            MouseStart.setLocation(pt);
        }
    }
    public void mouseClicked(MouseEvent e){

    }
    public void mousePressed(MouseEvent e){
        MouseStart=e.getLocationOnScreen();
    }
    public void mouseReleased(MouseEvent e){

    }
    public void mouseEntered(MouseEvent e){

    }
    public void mouseExited(MouseEvent e){

    }
    public static void addTempImage(ImagePlus impl){
        if(cvTempImages==null) cvTempImages=new ArrayList();
        cvTempImages.add(impl);
    }
    public static void closeAllTempImages(){
        ImagePlus impl;
        for(int i=0;i<cvTempImages.size();i++){
            impl=cvTempImages.get(i);
            if(impl==null) continue;
            if(impl.getWindow().isClosed()) continue;
            impl.close();
        }
    }
}
