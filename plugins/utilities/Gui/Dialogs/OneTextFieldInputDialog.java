/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OneTextFieldInputDialog.java
 *
 * Created on May 9, 2012, 11:54:18 AM
 */

package utilities.Gui.Dialogs;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class OneTextFieldInputDialog extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    int numTexts;
    boolean password=false;

    /** Creates new form OneTextFieldInputDialog */
    public OneTextFieldInputDialog(java.awt.Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        initComponents();
        getRootPane().setDefaultButton(okButton);
        PasswordField.requestFocus();
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    public void PasswordInput(boolean Password){
        password=Password;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        label1 = new javax.swing.JLabel();
        text1 = new javax.swing.JTextField();
        label2 = new javax.swing.JLabel();
        text2 = new javax.swing.JTextField();
        label3 = new javax.swing.JLabel();
        text3 = new javax.swing.JTextField();
        label4 = new javax.swing.JLabel();
        text4 = new javax.swing.JTextField();
        label5 = new javax.swing.JLabel();
        text5 = new javax.swing.JTextField();
        PasswordLabel = new javax.swing.JLabel();
        PasswordField = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        label1.setText("label1");

        text1.setText("text1");

        label2.setText("label2");

        text2.setText("text2");

        label3.setText("label3");

        text3.setText("text3");

        label4.setText("label4");

        text4.setText("text4");

        label5.setText("labe5");

        text5.setText("text5");

        PasswordLabel.setText("Password");

        PasswordField.setText("jPasswordField1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label1)
                        .addGap(18, 18, 18)
                        .addComponent(text1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label2)
                        .addGap(18, 18, 18)
                        .addComponent(text2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label3)
                        .addGap(18, 18, 18)
                        .addComponent(text3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label4)
                        .addGap(18, 18, 18)
                        .addComponent(text4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label5)
                        .addGap(18, 18, 18)
                        .addComponent(text5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(PasswordLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(99, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label1)
                    .addComponent(text1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(label2)
                    .addComponent(text2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label3)
                    .addComponent(text3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label4)
                    .addComponent(text4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label5)
                    .addComponent(text5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PasswordLabel)
                    .addComponent(PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    public String getInputString(){
        return text1.getText();
    }
    public void initLabel(int index,String label, String text){
        switch (index){
            case 0:
                label1.setText(label);
                text1.setText(text);
                break;
            case 1:
                label2.setText(label);
                text2.setText(text);
                break;
            case 2:
                label3.setText(label);
                text3.setText(text);
                break;
            case 3:
                label4.setText(label);
                text4.setText(text);
                break;
            case 4:
                label5.setText(label);
                text5.setText(text);
                break;
        }
    }
    public void hideLabel(int index){
        switch (index){
            case 0:
                label1.setVisible(false);
                text1.setVisible(false);
                break;
            case 1:
                label2.setVisible(false);
                text2.setVisible(false);
                break;
            case 2:
                label3.setVisible(false);
                text3.setVisible(false);
                break;
            case 3:
                label4.setVisible(false);
                text4.setVisible(false);
                break;
            case 4:
                label5.setVisible(false);
                text5.setVisible(false);
                break;
        }
    }
    public String getText(int index){
        String text;
        switch (index){
            case 0:
                text=text1.getText();
                break;
            case 1:
                text=text2.getText();
                break;
            case 2:
                text=text3.getText();
                break;
            case 3:
                text=text4.getText();
                break;
            case 4:
                text=text5.getText();
                break;
            default:
                text="";
        }
        return text;
    }
    public ArrayList<String> getTexts(){
        ArrayList<String> texts=new ArrayList();
        for(int i=0;i<numTexts;i++){
            texts.add(getText(i));
        }
        return texts;
    }
    public String getPassword(){
        char pwcs[]=PasswordField.getPassword();
        String pw=new String(pwcs);
        return pw;
    }
    public void initLabels(ArrayList<String> labels, ArrayList<String> texts){
                int i,len=labels.size(),numFields=5;
                for(i=0;i<len;i++){
                    initLabel(i, labels.get(i), texts.get(i));
                }
                for(i=len;i<numFields;i++){
                    hideLabel(i);
                }
                if(!password) {
                    PasswordLabel.setVisible(false);
                    PasswordField.setVisible(false);
                }
                PasswordField.requestFocus();
                setVisible(true);                                
                numTexts=labels.size();
    }
    /**
    * @param args the command line arguments
    */
    public static OneTextFieldInputDialog main(final String title, final ArrayList<String> labels, final ArrayList<String> texts) {
                OneTextFieldInputDialog dialog = new OneTextFieldInputDialog(new javax.swing.JFrame(), title, true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.initLabels(labels, texts);
                return dialog;
    }
    public static OneTextFieldInputDialog main(final String title, final ArrayList<String> labels, final ArrayList<String> texts, boolean Password) {
                OneTextFieldInputDialog dialog = new OneTextFieldInputDialog(new javax.swing.JFrame(), title, true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.PasswordInput(Password);
                dialog.initLabels(labels, texts);                
                return dialog;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField PasswordField;
    private javax.swing.JLabel PasswordLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JLabel label3;
    private javax.swing.JLabel label4;
    private javax.swing.JLabel label5;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField text1;
    private javax.swing.JTextField text2;
    private javax.swing.JTextField text3;
    private javax.swing.JTextField text4;
    private javax.swing.JTextField text5;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;
}