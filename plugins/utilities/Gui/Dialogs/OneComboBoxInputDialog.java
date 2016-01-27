/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.Gui.Dialogs;

import java.util.ArrayList;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JComboBox;

/**
 *
 * @author Taihao
 */
public class OneComboBoxInputDialog extends javax.swing.JDialog {

    /**
     * Creates new form OneComboBoxInputDialog
     */
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    int numBoxes;
    public int returnStatus;
    ArrayList<Component> m_cvAdditionalComponents;
    ArrayList<JLabel> cvLabels;
    ArrayList<JComboBox> cvBoxes;
    
    public OneComboBoxInputDialog(java.awt.Frame parent, String title, boolean modal) {
        super(parent, modal);
        setTitle(title);
        initComponents();
        additionalInit();
    }
    void additionalInit(){
        cvLabels=new ArrayList();
        cvBoxes=new ArrayList();
        cvLabels.add(label1);
        cvBoxes.add(box1);
        cvLabels.add(label2);
        cvBoxes.add(box2);
        cvLabels.add(label3);
        cvBoxes.add(box3);
        cvLabels.add(label4);
        cvBoxes.add(box4);
        cvLabels.add(label5);
        cvBoxes.add(box5);
        cvLabels.add(label6);
        cvBoxes.add(box6);
        numBoxes=6;
        m_cvAdditionalComponents=new ArrayList();
        OKBT.requestFocus();
    }
    public JLabel getLabel(int index){
        if(index<cvLabels.size()) return cvLabels.get(index);
        return null;
    }
    public JComboBox getBox(int index){
        if(index<cvBoxes.size()) return cvBoxes.get(index);
        return null;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        OKBT = new javax.swing.JButton();
        CancelBT = new javax.swing.JButton();
        box1 = new javax.swing.JComboBox();
        box2 = new javax.swing.JComboBox();
        box3 = new javax.swing.JComboBox();
        box4 = new javax.swing.JComboBox();
        box5 = new javax.swing.JComboBox();
        label1 = new javax.swing.JLabel();
        label2 = new javax.swing.JLabel();
        label3 = new javax.swing.JLabel();
        label4 = new javax.swing.JLabel();
        label5 = new javax.swing.JLabel();
        label6 = new javax.swing.JLabel();
        box6 = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);

        OKBT.setText("OK");
        OKBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKBTActionPerformed(evt);
            }
        });

        CancelBT.setText("Cancel");

        label1.setText("jLabel1");

        label2.setText("jLabel2");

        label3.setText("jLabel3");

        label4.setText("jLabel4");

        label5.setText("jLabel5");

        label6.setText("jLabel6");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(OKBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CancelBT)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(box6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                                .addComponent(box1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(box2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(box5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(box3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(box4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(67, 67, 67))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(box1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(box2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(box3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(box4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(box5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label6)
                    .addComponent(box6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OKBT)
                    .addComponent(CancelBT))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OKBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKBTActionPerformed
        // TODO add your handling code here:
                doClose(RET_OK);
    }//GEN-LAST:event_OKBTActionPerformed
    void doClose(int ret){
        returnStatus = ret;
        setVisible(false);
        dispose();
    }
    public int getReturnStatus(){
        return returnStatus;
    }
    /**
     * @param args the command line arguments
     */
    public static OneComboBoxInputDialog main(final String DialogTitle, final ArrayList<String> BoxLabels, final ArrayList<Object[]> items) {
                OneComboBoxInputDialog dialog = new OneComboBoxInputDialog(new javax.swing.JFrame(), DialogTitle, true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.initBoxes(BoxLabels, items);
                dialog.getContentPane().validate();
                dialog.getContentPane().repaint();
                dialog.pack();
                dialog.setVisible(true);
                return dialog;
    }
    public static OneComboBoxInputDialog main(final String DialogTitle, final ArrayList<String> BoxLabels, final ArrayList<Object[]> items,
                                              final ArrayList<Component> cvComps) {
                OneComboBoxInputDialog dialog = new OneComboBoxInputDialog(new javax.swing.JFrame(), DialogTitle, true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                
                dialog.initBoxes(BoxLabels, items);  
                Component cmp;
                int hspacing=30,w,h,x,y,num=dialog.numBoxes;
                
                for(int i=0;i<cvComps.size();i++){
                    dialog.addAdditionalComponent(cvComps.get(i));
                }     
                
                dialog.getContentPane().validate();
                dialog.getContentPane().repaint();
                dialog.pack();
                dialog.setVisible(true);
                return dialog;
    }
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OneComboBoxInputDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OneComboBoxInputDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OneComboBoxInputDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OneComboBoxInputDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                OneComboBoxInputDialog dialog = new OneComboBoxInputDialog(new javax.swing.JFrame(),"", true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    public void initBox(int index,String sLabel, Object[] poData){
        int i,len=poData.length;
        JLabel label=getLabel(index);
        JComboBox box=getBox(index);
        if(label!=null){
            label.setText(sLabel);
            for(i=0;i<len;i++){
                box.addItem(poData[i]);
            }
            box.addItem("New");
        }
    }
    public void hideBox(int index){
        int i;
        JLabel label=getLabel(index);
        JComboBox box=getBox(index);
        if(label!=null){
            label.setVisible(false);
            box.setVisible(false);
        }
    }
    public Object getSelectedItem(int index){
        Object Item=null;
        if(index<cvBoxes.size()) Item=getBox(index).getSelectedItem();
        return Item;
    }
    public int getSelectedIndex(int index){
        int selectedIndex=-1;
        if(index<cvBoxes.size()) selectedIndex=getBox(index).getSelectedIndex();
        return selectedIndex;
    }
    public void initBoxes(ArrayList<String> labels, ArrayList<Object[]> povItems){
                int i,len=labels.size();
                numBoxes=labels.size();
                for(i=0;i<len;i++){
                    initBox(i, labels.get(i), povItems.get(i));
                }
                for(i=len;i<cvLabels.size();i++){
                    hideBox(i);
                }
    }
    public void addAdditionalComponent(Component cmp0){
        int x,y,spacing=20,num=m_cvAdditionalComponents.size();
        Component cmp;
        if(num==0){
            cmp=getLabel(numBoxes-1);
        } else {
            cmp=m_cvAdditionalComponents.get(num-1);
        }
        getContentPane().add(cmp0);
        x=cmp.getBounds().x;
        y=cmp.getY()+cmp.getHeight()+spacing;
        cmp0.setVisible(true);
        cmp0.setBounds(x, y, cmp0.getPreferredSize().width, cmp0.getPreferredSize().height);
    }
    public int getSelectedItems(ArrayList<Object> items){
        items.clear();
        if(returnStatus==RET_CANCEL) return RET_CANCEL;
        for(int i=0;i<numBoxes;i++){
            items.add(getSelectedItem(i));
        }
        return RET_OK;
    }
    public int getSelectedIndexes(ArrayList<Integer> indexes){
        indexes.clear();
        if(returnStatus==RET_CANCEL) return RET_CANCEL;
        for(int i=0;i<numBoxes;i++){
            indexes.add(getSelectedIndex(i));
        }
        return RET_OK;
    }
    /**
    * @param args the command line arguments
    */
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelBT;
    private javax.swing.JButton OKBT;
    private javax.swing.JComboBox box1;
    private javax.swing.JComboBox box2;
    private javax.swing.JComboBox box3;
    private javax.swing.JComboBox box4;
    private javax.swing.JComboBox box5;
    private javax.swing.JComboBox box6;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JLabel label3;
    private javax.swing.JLabel label4;
    private javax.swing.JLabel label5;
    private javax.swing.JLabel label6;
    // End of variables declaration//GEN-END:variables
}
