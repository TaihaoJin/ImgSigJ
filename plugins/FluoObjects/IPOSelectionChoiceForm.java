/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * IPOSelectionChoiceForm.java
 *
 * Created on Oct 6, 2011, 6:26:01 PM
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.io.PrintAssist;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.intRange;
/**
 *
 * @author Taihao
 */
public class IPOSelectionChoiceForm extends javax.swing.JFrame {

    /** Creates new form IPOSelectionChoiceForm */
    ArrayList<IPOGaussianNode> m_cvIPOGs;
    ArrayList<IPOGaussianNode> m_cvSelectedIPOGs;
    DoubleRange HeightRange=new DoubleRange();
    DoubleRange ClusterSizeRange=new DoubleRange();
    DoubleRange TotalSignalRange=new DoubleRange();
    intRange IPOIndexRange=new intRange();

    public IPOSelectionChoiceForm() {
        initComponents();
        HeightRange=new DoubleRange();
        ClusterSizeRange=new DoubleRange();
        TotalSignalRange=new DoubleRange();
        IPOIndexRange=new intRange();
        m_cvSelectedIPOGs=new ArrayList();
    }
    public void updateIPOSelectionChoices(ArrayList<IPOGaussianNode> IPOGs){
        calParRange(IPOGs);
        updteIPOSelectionChoices();
    }
    public void updateIPOSelectionChoices(StackIPOGaussianNode cStackIPO){
        calParRange(cStackIPO);
        updteIPOSelectionChoices();
    }
    public void updteIPOSelectionChoices(){
        Height1TF.setText(PrintAssist.ToString(HeightRange.getMin(), 1));
        Height2TF.setText(PrintAssist.ToString(HeightRange.getMax(), 1));
        TotalSignal1TF.setText(PrintAssist.ToString(TotalSignalRange.getMin(), 1));
        TotalSignal2TF.setText(PrintAssist.ToString(TotalSignalRange.getMax(), 1));
        ClusterSize1TF.setText(PrintAssist.ToString(ClusterSizeRange.getMin(), 0));
        ClusterSize2TF.setText(PrintAssist.ToString(ClusterSizeRange.getMax(), 0));
        FirstIdTF.setText(PrintAssist.ToString(IPOIndexRange.getMin(), 0));
        LastIdTF.setText(PrintAssist.ToString(IPOIndexRange.getMax(), 0));
    }
    public void clearRanges(){
        HeightRange.resetRange();
        ClusterSizeRange.resetRange();
        TotalSignalRange.resetRange();
    }
    public int calParRange(ArrayList<IPOGaussianNode> IPOGs){
        if(IPOGs==null) return -1;
        int len=IPOGs.size(),i;
        IPOIndexRange.setRange(0, len-1);
        IPOGaussianNode IPOG;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            HeightRange.expandRange(IPOG.Amp);
            ClusterSizeRange.expandRange(IPOG.getClusterSize());
            TotalSignalRange.expandRange(IPOG.dTotalSignal);
        }
        return 1;
    }
    public int calParRange(StackIPOGaussianNode cStackIPO){
        clearRanges();
        if(cStackIPO==null) return -1;
        int sliceI=cStackIPO.sliceI,sliceF=cStackIPO.sliceF,slice;
        for(slice=sliceI;slice<=sliceF;slice++){
            calParRange(cStackIPO.getIPOGs(slice));
        }
        return 1;
    }
    public void updateSelectionCriteria(){
        HeightRange.setRange(Double.parseDouble(Height1TF.getText()),Double.parseDouble(Height2TF.getText()));
        ClusterSizeRange.setRange(Integer.parseInt(ClusterSize1TF.getText()),Integer.parseInt(ClusterSize2TF.getText()));
        TotalSignalRange.setRange(Double.parseDouble(TotalSignal1TF.getText()),Double.parseDouble(TotalSignal2TF.getText()));
        IPOIndexRange.setRange(Integer.parseInt(FirstIdTF.getText()),Integer.parseInt(LastIdTF.getText()));
    }
    public void selectIPOGs(){
        int i,len=m_cvIPOGs.size();
        boolean hc=HeightRB.isSelected(),cc=ClusterSizeRB.isSelected(),tc=TotalSignalRB.isSelected(),ic=IPOIdRB.isSelected();
        IPOGaussianNode IPO;
        m_cvSelectedIPOGs.clear();
        for(i=0;i<len;i++){
            IPO=m_cvIPOGs.get(i);
            if(hc){
                if(!HeightRange.contains(IPO.Amp)) continue;
            }
            if(cc){
                if(!ClusterSizeRange.contains(IPO.cvIndexesInCluster.size())) continue;
            }
            if(tc){
                if(!TotalSignalRange.contains(IPO.dTotalSignal)) continue;
            }
            if(ic){
                if(!IPOIndexRange.contains(IPO.IPOIndex)) continue;
            }
            m_cvSelectedIPOGs.add(IPO);
        }
    }

    public void selectIPOGs(ArrayList<IPOGaussianNode> IPOGs){
        m_cvIPOGs=IPOGs;
        selectIPOGs();
    }

    public int selectIPOs(StackIPOGaussianNode cStackIPO){
        int sliceI=cStackIPO.sliceI,sliceF=cStackIPO.sliceF,slice,i,len;
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        for(slice=sliceI;slice<=sliceF;slice++){
            selectIPOGs(cStackIPO.getIPOGs(slice));
            len=m_cvSelectedIPOGs.size();
            for(i=0;i<len;i++){
                IPOGs.add(m_cvSelectedIPOGs.get(i));
            }
        }
        m_cvSelectedIPOGs=IPOGs;
        return 1;
    }

    public ArrayList<IPOGaussianNode> getSelectedIPOGs(){
        return m_cvSelectedIPOGs;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Height1TF = new javax.swing.JTextField();
        HeightRB = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        Height2TF = new javax.swing.JTextField();
        TotalSignalRB = new javax.swing.JRadioButton();
        TotalSignal1TF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        ClusterSize1TF = new javax.swing.JTextField();
        ClusterSizeRB = new javax.swing.JRadioButton();
        ClusterSize2TF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        TotalSignal2TF = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        IPOIdRB = new javax.swing.JRadioButton();
        LastIdTF = new javax.swing.JTextField();
        FirstIdTF = new javax.swing.JTextField();
        EntireStackCB = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Height1TF.setText("100");

        HeightRB.setText("height");

        jLabel2.setText("to");

        Height2TF.setText("999999");

        TotalSignalRB.setText("total signal");

        TotalSignal1TF.setText("800");

        jLabel4.setText("to");

        ClusterSize1TF.setText("0");
        ClusterSize1TF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClusterSize1TFActionPerformed(evt);
            }
        });

        ClusterSizeRB.setText("cluster size");

        ClusterSize2TF.setText("10000");

        jLabel3.setText("to");

        TotalSignal2TF.setText("99999");
        TotalSignal2TF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TotalSignal2TFActionPerformed(evt);
            }
        });

        jLabel13.setText("t0");

        IPOIdRB.setText("IPO Id");

        LastIdTF.setText("300");

        FirstIdTF.setText("0");

        EntireStackCB.setText("entire stack");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TotalSignalRB)
                            .addComponent(HeightRB)
                            .addComponent(IPOIdRB)
                            .addComponent(ClusterSizeRB))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Height1TF, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                            .addComponent(ClusterSize1TF)
                            .addComponent(FirstIdTF, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                            .addComponent(TotalSignal1TF))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(TotalSignal2TF)
                                .addComponent(Height2TF)
                                .addComponent(ClusterSize2TF, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(LastIdTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(EntireStackCB))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(Height2TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Height1TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TotalSignal1TF, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(TotalSignal2TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ClusterSize1TF, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ClusterSize2TF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(FirstIdTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(LastIdTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(TotalSignalRB))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(HeightRB)
                        .addGap(39, 39, 39)
                        .addComponent(ClusterSizeRB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(IPOIdRB)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(EntireStackCB))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ClusterSize1TFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClusterSize1TFActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_ClusterSize1TFActionPerformed

    private void TotalSignal2TFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TotalSignal2TFActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_TotalSignal2TFActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IPOSelectionChoiceForm().setVisible(true);
            }
        });
    }
    public boolean selectEntireStack(){
        return EntireStackCB.isSelected();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ClusterSize1TF;
    private javax.swing.JTextField ClusterSize2TF;
    private javax.swing.JRadioButton ClusterSizeRB;
    private javax.swing.JCheckBox EntireStackCB;
    private javax.swing.JTextField FirstIdTF;
    private javax.swing.JTextField Height1TF;
    private javax.swing.JTextField Height2TF;
    private javax.swing.JRadioButton HeightRB;
    private javax.swing.JRadioButton IPOIdRB;
    private javax.swing.JTextField LastIdTF;
    private javax.swing.JTextField TotalSignal1TF;
    private javax.swing.JTextField TotalSignal2TF;
    private javax.swing.JRadioButton TotalSignalRB;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables

}
