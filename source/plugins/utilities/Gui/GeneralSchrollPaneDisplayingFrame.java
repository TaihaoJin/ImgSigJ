/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GeneralSchrollPaneDisplayingFrame.java
 *
 * Created on Mar 7, 2012, 10:38:17 AM
 */

package utilities.Gui;
import javax.swing.JViewport;

/**
 *
 * @author Taihao
 */
public class GeneralSchrollPaneDisplayingFrame extends javax.swing.JFrame {

    /** Creates new form GeneralSchrollPaneDisplayingFrame */
    public GeneralSchrollPaneDisplayingFrame() {
        initComponents();
    }
    public GeneralSchrollPaneDisplayingFrame(JViewport jvp) {
        this();
        DisplayingSP.setViewport(jvp);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        DisplayingSP = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Schroll Pane Displayer");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(DisplayingSP, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(DisplayingSP, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GeneralSchrollPaneDisplayingFrame().setVisible(true);
            }
        });
    }
    public static void displayNewFrame(final JViewport jvp) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GeneralSchrollPaneDisplayingFrame(jvp).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane DisplayingSP;
    // End of variables declaration//GEN-END:variables

}
