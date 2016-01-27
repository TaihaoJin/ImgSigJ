/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImageFittingGUI.java
 *
 * Created on Jun 24, 2011, 10:33:59 PM
 */

package utilities.Non_LinearFitting;
import FluoObjects.IPOGaussianFitter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import utilities.CommonGuiMethods;
import utilities.Non_LinearFitting.GeneralTableModel;
import utilities.Non_LinearFitting.FittingResultsNode;
import java.util.ArrayList;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import ij.ImagePlus;
import ImageAnalysis.AnnotatedImagePlus;
import utilities.Non_LinearFitting.Fitting_Function;
import utilities.Non_LinearFitting.StandardFittingFunction;
import utilities.CommonMethods;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.CommonStatisticsMethods;
import utilities.statistics.Histogram;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import java.util.StringTokenizer;
import ImageAnalysis.ImageAnnotationNode;
import utilities.Non_LinearFitting.FittingModelAnnotationNode;
import java.util.EventObject;
import ij.IJ;
import utilities.Non_LinearFitting.ButtonColumn;
import utilities.Non_LinearFitting.FittingResultAnalyzerForm;
import utilities.Non_LinearFitting.MultipleButtonColumn;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.JLabel;
import javax.swing.event.*;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import utilities.io.PrintAssist;
import utilities.io.PixelGrabberDlg;
import utilities.Gui.PixelGrabberForm;
import utilities.Gui.PixelGrabberReceiver;
import utilities.Gui.AnalysisMasterForm;
import utilities.Non_LinearFitting.ButtonColumn;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.Non_LinearFitting.FittingModelAnnotationNode;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.Non_LinearFitting.FittingResultAnalyzerForm;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.Non_LinearFitting.GeneralTableModel;
import utilities.Non_LinearFitting.ImageFittingGUI;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import ij.gui.PlotWindow;
import utilities.Gui.PlotWindowPlus;
import utilities.statistics.MeanSem1;
import javax.swing.JViewport;
import utilities.statistics.GaussianDistribution;
import utilities.io.PrintAssist;
import ij.gui.Roi;
/**
 *
 * @author Taihao
 */
public class CurveFittingGUI extends javax.swing.JFrame implements MouseMotionListener,MouseListener,TableModelListener,ListSelectionListener, ActionListener{
    class MultipleButtonColumn extends ButtonColumn{
        int rows,cols;
        ArrayList<Integer> columns;
        TableCellEditor originalCellEditor[][];
        TableCellRenderer originalCellRenderer[][];
        Color[][] originalBackgroundColor;
        PlotWindowPlus m_cPlotWindow;
	public MultipleButtonColumn(JTable table, Action action, ArrayList<Integer> columns)
	{
                this.columns=new ArrayList();
                CommonStatisticsMethods.copyArray(columns, this.columns);
		this.table = table;
		this.action = action;
                cols=table.getColumnCount();
                rows=table.getRowCount();

		renderButton = new JButton();
		editButton = new JButton();
		editButton.setFocusPainted( false );
		editButton.addActionListener( this );
		originalBorder = editButton.getBorder();
		setFocusBorder( new LineBorder(Color.BLUE) );

		TableColumnModel columnModel = table.getColumnModel();
                int i,column,len=columns.size();
//                len=cols;
                for(i=0;i<len;i++){
                    column=columns.get(i);
//                    column=i;
                    columnModel.getColumn(column).setCellRenderer( this );
                    columnModel.getColumn(column).setCellEditor( this );
                }
		table.addMouseListener( this );
	}
	public void actionPerformed(ActionEvent e)
	{
		int row = table.convertRowIndexToModel( table.getEditingRow() );
		int column = table.convertRowIndexToModel( table.getEditingColumn() );
		fireEditingStopped();

		//  Invoke the Action

		ActionEvent event = new ActionEvent(
			table,
			ActionEvent.ACTION_PERFORMED,
			"" + row+" "+column);
		action.actionPerformed(event);
	}
	public Component getTableCellEditorComponent(
		JTable table, Object value, boolean isSelected, int row, int column)
	{
                if(!CommonMethods.containsContent(columns, column)) {
                    Component cell=originalCellEditor[row][column].getTableCellEditorComponent(table, value, isSelected, row, column);
                    return cell;
                }
		if (value == null)
		{
			editButton.setText( "" );
			editButton.setIcon( null );
		}
		else if (value instanceof Icon)
		{
			editButton.setText( "" );
			editButton.setIcon( (Icon)value );
		}
		else
		{
			editButton.setText( value.toString() );
			editButton.setIcon( null );
		}

		this.editorValue = value;
		return editButton;
	}
	public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
                if(isSelected)
		{
			renderButton.setForeground(table.getSelectionForeground());
	 		renderButton.setBackground(table.getSelectionBackground());
		}
		else
		{
			renderButton.setForeground(table.getForeground());
			renderButton.setBackground(UIManager.getColor("Button.background"));
		}

		if (hasFocus)
		{
			renderButton.setBorder( focusBorder );
		}
		else
		{
			renderButton.setBorder( originalBorder );
		}
		if (value == null)
		{
			renderButton.setText( "" );
			renderButton.setIcon( null );
		}
		else if (value instanceof Icon)
		{
			renderButton.setText( "" );
			renderButton.setIcon( (Icon)value );
		}
		else
		{
			renderButton.setText( value.toString() );
			renderButton.setIcon( null );
		}
                return renderButton;
        }
    }


    class ComponentBackgroundMaker extends JLabel implements TableCellRenderer {
        public ComponentBackgroundMaker(JTable table,int col){
            setOpaque(false);
             int rows=table.getRowCount(),i;
             table.getColumnModel().getColumn(col).setCellRenderer(this);
             setHorizontalAlignment(SwingConstants.RIGHT);
        }
	public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
            int component=m_nvComponentIndexes.get(row);
            setText(value.toString());
            if(component>-1&&column==2){
                setForeground(m_cvComponentColors.get(component));
            }else{
                setForeground(Color.white);
            }
            return this;
        }
    }


    ArrayList<Integer> buttonColumns;
    ArrayList<Integer> m_nvComponentIndexes;
    ArrayList<Color> m_cvComponentColors;
    ArrayList<Integer> m_nvParIndexes;
    int m_nNumComponentColors;
    int[][] m_pnVarIndexes;
    class ParTableModel extends GeneralTableModel{
        public ParTableModel(String[] columnHeads,Object[][] data){
            super(columnHeads,data);
        }
        public Class getColumnClass(int c) {
            JButton button=new JButton(" ");
            int i,len=buttonColumns.size();
            for(i=0;i<len;i++){
                if(c==buttonColumns.get(i)) return button.getClass();
            }
            return getValueAt(1, c).getClass();
        }
        public boolean isCellEditable(int row, int col) {
            if (col < 1) {
                return false;
            }
            if(col==5){
                return false;
            }
            return true;
        }
    }
    Action zeroParCell = new AbstractAction()
    {
        public void actionPerformed(ActionEvent e)
        {
            JTable table = (JTable)e.getSource();
            int modelRow = Integer.valueOf( e.getActionCommand() );
            ParTableModel tm=(ParTableModel)table.getModel();
            tm.setValueAt(0, modelRow, 2);
        }
    };
    Action zeroAndrestParCell = new AbstractAction()
    {
        public void actionPerformed(ActionEvent e)
        {
            JTable table = (JTable)e.getSource();
//            int modelRow = Integer.valueOf( e.getActionCommand() );
            int rowAndcolumn[]=new int[2];
            getRowAndColumn( e.getActionCommand(),rowAndcolumn);
            ParTableModel tm=(ParTableModel)table.getModel();
            int row=rowAndcolumn[0],column=rowAndcolumn[1];
            if(column==3){
                tm.setValueAt(0.0, row, 2);
            }else if(column==4){
                tm.setValueAt(tm.getValueAt(row, 5), row, 2);
            }
        }
        void getRowAndColumn(String st, int[] rowAndcolumn){
            StringTokenizer stk=new StringTokenizer(st);
            rowAndcolumn[0]=Integer.valueOf(stk.nextToken());
            rowAndcolumn[1]=Integer.valueOf(stk.nextToken());
        }
    };
    Action resetParCell = new AbstractAction()
    {
        public void actionPerformed(ActionEvent e)
        {
            JTable table = (JTable)e.getSource();
            int modelRow = Integer.valueOf( e.getActionCommand() );
            ParTableModel tm=(ParTableModel)table.getModel();
            tm.setValueAt(tm.getValueAt(modelRow, 5), modelRow, 2);
        }
    };
    class resetTableButton extends JButton{
        public String toString(){
            return "R";
        }
    }
    class zeroTableButton extends JButton{
        public String toString(){
            return "Z";
        }
    }


    static int nSN=1;
    static int nLock;
    int m_nSN;

    Non_Linear_Fitter m_cFitter;
    FittingModelNode m_cCurrentModel;

    ComposedFittingFunction m_cFunc;
    FittingResultsNode m_cResultsNode;
    JTable m_cParTable;

    DoubleRange m_cSignalRange;
    boolean m_bPausedFitting;

    Point m_cCursorImageCoordinates,m_cCursorOnScreenLocation;
    ImagePlus impl_Source;//the image that get the mouse cursor
    PlotWindowPlus m_cPlotWindow;

    Thread m_cFittingThread;
    boolean m_bFittingTerminated;
    AnalysisMasterForm m_cMasterForm;
    int m_nNumPointsInCurve;

    public void setMasterForm(AnalysisMasterForm cMasterForm){
        m_cMasterForm=cMasterForm;
    }

    /** Creates new form ImageFittingGUI */
    protected static FittingResultsNode initialFittingResultNode;//not currently in use

    /**
     * Get the value of initialFittingResultNode
     *
     * @return the value of initialFittingResultNode
     */
    public static FittingResultsNode getInitialFittingResultNode() {
        return initialFittingResultNode;
    }

    /**
     * Set the value of initialFittingResultNode
     *
     * @param initialFittingResultNode new value of initialFittingResultNode
     */
    public static void setInitialFittingResultNode(FittingResultsNode initialFittingResultNode) {
        ImageFittingGUI.initialFittingResultNode = initialFittingResultNode;
    }

    public CurveFittingGUI() {
        initComponents();
        createComponentColors();
        setSN();
        m_nNumPointsInCurve=500;
        m_bPausedFitting=false;
    }

    public CurveFittingGUI(FittingResultsNode fittingResults, int nModel){
        this();
//        this.m_cResultsNode=fittingResults;
        m_cCurrentModel=new FittingModelNode(fittingResults.m_cvModels.get(nModel));
        updateParTable();
        m_cFunc=new ComposedFittingFunction(m_cCurrentModel.svFunctionTypes);
        calSignalRange();
        drawFunctionValue();
    }
    void setSN(){
        m_nSN=nSN;
        nSN++;
    }

    public int getSN(){
        return m_nSN;
    }

    public boolean initParLocked(){
        return (nLock!=0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        ParTablePane = new javax.swing.JScrollPane();
        FittingParameters = new javax.swing.JLabel();
        DrawFunctinValueBT = new javax.swing.JButton();
        OneIterationBT = new javax.swing.JButton();
        IterationsBT = new javax.swing.JButton();
        IterationNumberTF = new javax.swing.JTextField();
        AddOneTermBT = new javax.swing.JButton();
        RemoveOneTermBT = new javax.swing.JButton();
        PauseFittingBT = new javax.swing.JButton();
        FitDataBT = new javax.swing.JButton();
        keepOldDrawing = new javax.swing.JCheckBox();
        ComponentTF = new javax.swing.JTextField();
        FittingFunctionComponentChoiseCB = new javax.swing.JComboBox();
        UpdateTableBT = new javax.swing.JButton();
        OptimizeOneComponent = new javax.swing.JButton();
        CurvePlotSettingBT = new javax.swing.JButton();
        FixAllBT = new javax.swing.JButton();
        FreeAllBT = new javax.swing.JButton();
        TerminateFittingBT = new javax.swing.JButton();
        UpdateFittingResultsBT = new javax.swing.JButton();
        FitterStatusLabel = new javax.swing.JLabel();
        FittingMethodCB = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        FullModelFittingCB = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        SmoothCurveBT = new javax.swing.JButton();
        SmoothingWSLB = new javax.swing.JLabel();
        SmoothingWSTF = new javax.swing.JTextField();
        InfoTableSP = new javax.swing.JScrollPane();
        InfoTableLB = new javax.swing.JLabel();
        ShowMeanSemBT = new javax.swing.JButton();
        ExcludeOutliarsCB = new javax.swing.JCheckBox();
        CloseAllBT = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        OutliarPTF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        RegressionOrderTF = new javax.swing.JTextField();
        OutliarExclusionRegression = new javax.swing.JCheckBox();
        showDevP = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        DevPWS = new javax.swing.JTextField();
        SelectCurveBT = new javax.swing.JButton();

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("FittingGuiFrame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        ParTablePane.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                ParTablePaneVetoableChange(evt);
            }
        });

        FittingParameters.setText("Fitting Parameters");

        DrawFunctinValueBT.setText("Draw Function Values");
        DrawFunctinValueBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DrawFunctinValueBTActionPerformed(evt);
            }
        });

        OneIterationBT.setText("One Iteration");
        OneIterationBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OneIterationBTActionPerformed(evt);
            }
        });

        IterationsBT.setText("Iterations");
        IterationsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IterationsBTActionPerformed(evt);
            }
        });

        IterationNumberTF.setText("10");
        IterationNumberTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IterationNumberTFActionPerformed(evt);
            }
        });

        AddOneTermBT.setText("Add one Term");
        AddOneTermBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddOneTermBTActionPerformed(evt);
            }
        });

        RemoveOneTermBT.setText("Reduce One Component");
        RemoveOneTermBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveOneTermBTActionPerformed(evt);
            }
        });

        PauseFittingBT.setText("Pause Fitting");
        PauseFittingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PauseFittingBTActionPerformed(evt);
            }
        });

        FitDataBT.setText("Fit Data");
        FitDataBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FitDataBTActionPerformed(evt);
            }
        });

        keepOldDrawing.setText("Display Fitting in New Plot");
        keepOldDrawing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepOldDrawingActionPerformed(evt);
            }
        });

        ComponentTF.setText("1");
        ComponentTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComponentTFActionPerformed(evt);
            }
        });

        FittingFunctionComponentChoiseCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "gaussian2D_Circular", "gaussian2D_GaussianPars", "exponential", "polynomial 2", "gaussian", "gaussian_Hist", "gaussian2D", "FluoDelta" }));

        UpdateTableBT.setText("Update Table");
        UpdateTableBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateTableBTActionPerformed(evt);
            }
        });

        OptimizeOneComponent.setText("Optimize One Component");
        OptimizeOneComponent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptimizeOneComponentActionPerformed(evt);
            }
        });

        CurvePlotSettingBT.setText("Select Graph");
        CurvePlotSettingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CurvePlotSettingBTActionPerformed(evt);
            }
        });

        FixAllBT.setText("fix all");
        FixAllBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FixAllBTActionPerformed(evt);
            }
        });

        FreeAllBT.setText("free all");
        FreeAllBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FreeAllBTActionPerformed(evt);
            }
        });

        TerminateFittingBT.setText("terminate fitting");
        TerminateFittingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TerminateFittingBTActionPerformed(evt);
            }
        });

        UpdateFittingResultsBT.setText("Update Fitting Results");
        UpdateFittingResultsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateFittingResultsBTActionPerformed(evt);
            }
        });

        FitterStatusLabel.setText("Fitter not started");

        FittingMethodCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "simplex", "conjugated gradient", "levenberg marguardt" }));

        jLabel2.setText("Fitting Method");

        FullModelFittingCB.setText("Full model");

        jButton1.setText("Build Histogram");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        SmoothCurveBT.setText("Smooth Curve");
        SmoothCurveBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SmoothCurveBTActionPerformed(evt);
            }
        });

        SmoothingWSLB.setText("Smoothing WS");

        SmoothingWSTF.setText("2");

        InfoTableLB.setText("Info Table");

        ShowMeanSemBT.setText("Show Mean Sem");
        ShowMeanSemBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowMeanSemBTActionPerformed(evt);
            }
        });

        ExcludeOutliarsCB.setText("Exclude Outliars");

        CloseAllBT.setText("Close");
        CloseAllBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseAllBTActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PlotWindowPlus" }));

        jLabel1.setText("p <");

        OutliarPTF.setText("0.01");

        jLabel3.setText("Regression Order");

        RegressionOrderTF.setText("1");

        OutliarExclusionRegression.setText("Outliar Exclusion");

        showDevP.setText("Show DevP");
        showDevP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDevPActionPerformed(evt);
            }
        });

        jLabel4.setText("WS/2");

        DevPWS.setText("1");

        SelectCurveBT.setText("Select Curve");
        SelectCurveBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectCurveBTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(OneIterationBT)
                                .addComponent(IterationsBT)
                                .addComponent(AddOneTermBT)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(10, 10, 10)
                                    .addComponent(keepOldDrawing))
                                .addComponent(DrawFunctinValueBT)
                                .addComponent(FittingFunctionComponentChoiseCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(IterationNumberTF, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(RemoveOneTermBT)
                                    .addGap(9, 9, 9)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(FitDataBT)
                                        .addComponent(PauseFittingBT))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(TerminateFittingBT)
                                        .addComponent(UpdateFittingResultsBT)))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(ComponentTF, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(FullModelFittingCB))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(OptimizeOneComponent)
                                    .addGap(3, 3, 3)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(FitterStatusLabel)))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(InfoTableLB)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(FixAllBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(FreeAllBT))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(ParTablePane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                        .addComponent(InfoTableSP, javax.swing.GroupLayout.Alignment.LEADING)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(UpdateTableBT)
                            .addComponent(jLabel2)
                            .addComponent(FittingMethodCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1)
                            .addComponent(SmoothCurveBT)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(SmoothingWSLB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SmoothingWSTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(ShowMeanSemBT)
                            .addComponent(ExcludeOutliarsCB)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(CloseAllBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(showDevP)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(DevPWS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(CurvePlotSettingBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(SelectCurveBT))
                            .addComponent(FittingParameters)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(OutliarPTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RegressionOrderTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(OutliarExclusionRegression))))
                .addGap(41, 41, 41))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(FixAllBT)
                                    .addComponent(FreeAllBT)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(104, 104, 104)
                                        .addComponent(jLabel2))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(CurvePlotSettingBT)
                                            .addComponent(SelectCurveBT))
                                        .addGap(11, 11, 11)
                                        .addComponent(UpdateTableBT)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(ParTablePane)
                                    .addGap(33, 33, 33))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(ShowMeanSemBT)
                                        .addComponent(FittingMethodCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(InfoTableLB)
                                        .addComponent(ExcludeOutliarsCB))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(SmoothCurveBT)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(SmoothingWSLB)
                                        .addComponent(SmoothingWSTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(DrawFunctinValueBT)
                                .addGap(7, 7, 7)
                                .addComponent(keepOldDrawing)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(OneIterationBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(IterationsBT)
                                    .addComponent(IterationNumberTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(AddOneTermBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(FittingFunctionComponentChoiseCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(RemoveOneTermBT)
                                .addGap(38, 38, 38)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ComponentTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(OptimizeOneComponent)
                                    .addComponent(FullModelFittingCB))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(FitDataBT)
                                    .addComponent(UpdateFittingResultsBT))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(PauseFittingBT)
                                    .addComponent(TerminateFittingBT))
                                .addGap(63, 63, 63)
                                .addComponent(FitterStatusLabel))
                            .addComponent(InfoTableSP, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(OutliarPTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(RegressionOrderTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(OutliarExclusionRegression)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(CloseAllBT)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(showDevP)
                                    .addComponent(jLabel4)
                                    .addComponent(DevPWS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(FittingParameters)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ParTablePaneVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_ParTablePaneVetoableChange
        int i=0;// TODO add your handling code here:
    }//GEN-LAST:event_ParTablePaneVetoableChange

    private void IterationNumberTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IterationNumberTFActionPerformed
        int i=0;// TODO add your handling code here:
    }//GEN-LAST:event_IterationNumberTFActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        int i=0;// TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void DrawFunctinValueBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DrawFunctinValueBTActionPerformed
        drawFunctionValue();
    }//GEN-LAST:event_DrawFunctinValueBTActionPerformed
    double[] retrieveParsFromTable(JTable table, int col){
        double[] pdPars;
        ArrayList<String> parNames=new ArrayList();
        ArrayList<Double> parValues=new ArrayList();
        getParsFromParTable(parNames,parValues);
        int len=parNames.size();
        pdPars=new double[len];
        for(int i=0;i<len;i++){
            pdPars[i]=parValues.get(i);
        }
        return pdPars;
    }
    
    private void OneIterationBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OneIterationBTActionPerformed
        fitData(1);
    }//GEN-LAST:event_OneIterationBTActionPerformed

    private void IterationsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IterationsBTActionPerformed
        int iterations=Integer.parseInt(IterationNumberTF.getText());
        fitData(iterations);
    }//GEN-LAST:event_IterationsBTActionPerformed
    void fitData(int iterations){
        boolean newFitting=false;

        if(m_cFitter==null)
            newFitting=true;
        else if(m_bFittingTerminated)
            newFitting=true;


        if(newFitting){
            updateFitter();
            m_cFitter.setPausingIterations(iterations);
            FitDataBTActionPerformed(null);
        }else{
            m_cFitter.setPausingIterations(iterations);
            PauseFittingBTActionPerformed(null);
        }
    }
    private void AddOneTermBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddOneTermBTActionPerformed
        int i=0;
        String sType=(String)FittingFunctionComponentChoiseCB.getSelectedItem();
        if(sType.contentEquals("gaussian2D_Circular")||sType.contentEquals("gaussian2D_GaussianPars")){
            IPOGaussianExpander expander=new IPOGaussianExpander();
            if(m_cCurrentModel.svFunctionTypes.size()==0)
                m_cCurrentModel=expander.buildExpandedModel(m_cCurrentModel, sType,1, false, true);
            else
                m_cCurrentModel=expander.buildExpandedModel(m_cCurrentModel, sType,1, false);
        }else{
            m_cCurrentModel.addOneComponent(sType);
        }
        int nWS=m_cPlotWindow.getSmoothingWS();
        m_cCurrentModel.makeHistFittingReady(nWS);
        updateParTable();
        drawFunctionValue();
        if(m_cFittingThread!=null){
            m_cFittingThread.interrupt();
        }
        ComponentTF.setText(""+(m_cCurrentModel.nComponents-1));
    }//GEN-LAST:event_AddOneTermBTActionPerformed

    private void RemoveOneTermBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveOneTermBTActionPerformed
        int nComponent=Integer.parseInt(ComponentTF.getText());
        m_cCurrentModel.removeOneComponent(nComponent);
        updateParTable();
        drawFunctionValue();
        m_cFittingThread.interrupt();
        ComponentTF.setText(""+(m_cCurrentModel.nComponents-1));
    }//GEN-LAST:event_RemoveOneTermBTActionPerformed

    private void PauseFittingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PauseFittingBTActionPerformed
        if(m_cFitter!=null){
            synchronized (m_cFitter) {
                if(m_cFitter.isPausedFitting()){
                    m_cFitter.resumeFitting();
                    m_cFitter.notifyAll();
                } else {
                    m_cFitter.pauseFitting();
                    PauseFittingBT.setLabel("resume");
                }
            }
        }
        ComponentTF.setText(""+(m_cCurrentModel.nComponents-1));
    }//GEN-LAST:event_PauseFittingBTActionPerformed

    private void FitDataBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FitDataBTActionPerformed
        if(FullModelFittingCB.isSelected()){
            FittingResultsNode aResultsNode=Non_Linear_FitterHandler.getFullModelFitting(m_cCurrentModel, 0.001);
            int nModels=aResultsNode.nModels;
            m_cCurrentModel=aResultsNode.m_cvModels.get(nModels-1);
        }
        else
        {
            boolean newFitting=false;
            if(m_cFittingThread==null)
                newFitting=true;
            else if(m_bFittingTerminated)
                newFitting=true;
            else if(!m_cFittingThread.isAlive())
                newFitting=true;

            if(newFitting){
                updateFitter();
                m_cFittingThread=new Thread(new Runnable() {
                    public void run() {
                        m_bFittingTerminated=false;
                        String method=(String)FittingMethodCB.getSelectedItem();
                        if(method.contentEquals("simplex"))m_cFitter.doFit_Apache();
                        if(method.contentEquals("conjugated gradient"))m_cFitter.doFit_ConjugateGradient();
                        if(method.contentEquals("levenberg marguardt"))m_cFitter.doFit_LevenbergMarquardt();
                        updateFittingResults();
                        m_bFittingTerminated=true;
                    }
                });
                m_cFittingThread.start();
            }else{
                m_cFitter.setMaxPausingIteration();
                PauseFittingBTActionPerformed(null);
            }
        }
        updateParTableCells();
        drawFunctionValue();
    }//GEN-LAST:event_FitDataBTActionPerformed
    private void keepOldDrawingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepOldDrawingActionPerformed
    }//GEN-LAST:event_keepOldDrawingActionPerformed

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_formMouseMoved

    private void ComponentTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComponentTFActionPerformed
        int nComponent=Integer.parseInt(ComponentTF.getText()),nComponentMax=m_cCurrentModel.nComponents-1;
        if(nComponent>nComponentMax)
            ComponentTF.setText(""+nComponentMax);
    }//GEN-LAST:event_ComponentTFActionPerformed

    private void UpdateTableBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateTableBTActionPerformed
        updateParTable();
        // TODO add your handling code here:
    }//GEN-LAST:event_UpdateTableBTActionPerformed

    private void OptimizeOneComponentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OptimizeOneComponentActionPerformed
        // TODO add your handling code here:
        int nComponent=Integer.parseInt(ComponentTF.getText());
        m_cCurrentModel.OptimizeOneComponentPars(nComponent);
        updateFittingResults();
    }//GEN-LAST:event_OptimizeOneComponentActionPerformed

    private void CurvePlotSettingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CurvePlotSettingBTActionPerformed
        Window wdo=null;
        if(m_cPlotWindow!=null){
            wdo=m_cPlotWindow.getImagePlus().getWindow();
        }
        ImagePlus impl=CommonMethods.getCurrentImage();
        Window wd=impl.getWindow();
        if(wd instanceof PlotWindow||wd instanceof PlotWindowPlus)
            updateCurveData((PlotWindow) wd);
        else
            IJ.error("the current image is not in a PlotWindow");
        if(wdo!=null) wdo.dispose();
    }//GEN-LAST:event_CurvePlotSettingBTActionPerformed

    private void FixAllBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FixAllBTActionPerformed
        m_cCurrentModel.fixAllPars();
        updateParTableCells();
    }//GEN-LAST:event_FixAllBTActionPerformed

    private void FreeAllBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FreeAllBTActionPerformed
        m_cCurrentModel.freeAllPars();
        updateParTableCells();
        // TODO add your handling code here:
    }//GEN-LAST:event_FreeAllBTActionPerformed

    private void TerminateFittingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TerminateFittingBTActionPerformed
        // TODO add your handling code here:
        if(m_cFitter!=null){
            m_cFitter.terminateIterations();
        }
    }//GEN-LAST:event_TerminateFittingBTActionPerformed

    private void UpdateFittingResultsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateFittingResultsBTActionPerformed
        // TODO add your handling code here:
        updateFittingResults();
    }//GEN-LAST:event_UpdateFittingResultsBTActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        ImagePlus impl=CommonMethods.getCurrentImage();
        Window wd=impl.getWindow();
        if(wd instanceof PlotWindow)
            buildHistogram((PlotWindow) wd);
        else
            IJ.error("the current image is not in a PlotWindow");

    }//GEN-LAST:event_jButton1ActionPerformed

    private void SmoothCurveBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SmoothCurveBTActionPerformed
        // TODO add your handling code here:
        smoothCurrentCurve();
    }//GEN-LAST:event_SmoothCurveBTActionPerformed

    private void ShowMeanSemBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowMeanSemBTActionPerformed
        // TODO add your handling code here:
        showCurveMeanSem();
    }//GEN-LAST:event_ShowMeanSemBTActionPerformed

    private void CloseAllBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseAllBTActionPerformed
        // TODO add your handling code here:
        closeAllPlotWindowPlus();
    }//GEN-LAST:event_CloseAllBTActionPerformed

    private void showDevPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showDevPActionPerformed
        // TODO add your handling code here:
        showDevP();
    }//GEN-LAST:event_showDevPActionPerformed

    private void SelectCurveBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectCurveBTActionPerformed
        // TODO add your handling code here:
        Roi roi=m_cPlotWindow.getImagePlus().getRoi();
        m_cPlotWindow.selectActiveCurve(roi);
    }//GEN-LAST:event_SelectCurveBTActionPerformed
    void updateFittingResults(){
        if(m_cFitter!=null){
            m_cFitter.updateFittingModel();
        }
        updateParTableCells();
        drawFunctionValue();
    }
    public static boolean sameDimensions(int[][] pn1, int pn2[][]){
        if(pn1==null||pn2==null) return false;
        if(pn1.length!=pn2.length||pn1[0].length!=pn2[0].length) return false;
        return true;
    }
    public void actionPerformed( ActionEvent ae){
        if(ae.getSource() instanceof Non_Linear_Fitter){
            if(ae.getActionCommand().contentEquals("Paused")){
                updateFittingResults();
                PauseFittingBT.setLabel("resume");
                showStatus("Fitter Status: Paused");
            }else if(ae.getActionCommand().contentEquals("Running")){
                PauseFittingBT.setLabel("pause");
                showStatus("Fitter Status: running");
            }else if(ae.getActionCommand().contentEquals("Finished")){
                updateFittingResults();
                showStatus("Fitter Status: Finished");
            }else if(ae.getActionCommand().contentEquals("Terminated")){
                updateFittingResults();
                showStatus("Fitter Status: Terminated");
            }else if(ae.getActionCommand().startsWith("Iterations")){
                updateFittingResults();
                showStatus(ae.getActionCommand());
            }
        }
    }
    void FitterPaused(){

    }
    public void mousePressed(MouseEvent e){}
    public void mouseClicked(MouseEvent eo){
        recordLocation(eo);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e){}

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e){}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e){
        impl_Source=getSourceImage(e);
        recordLocation(e);
        String stemp=impl_Source.getTitle();
        ImagePlus impl=getSourceImage(e);
        if(impl.getType()==AnnotatedImagePlus.Annotated){
            AnnotatedImagePlus impla=(AnnotatedImagePlus) impl;
            if(impla.getDescription().equalsIgnoreCase("FittingComponents")){
                int slice=impla.getCurrentSlice();
                stemp=impla.getAnnotaionsAsString(m_cCursorImageCoordinates.x, m_cCursorImageCoordinates.y, slice, "*");
            }
        }
        IJ.showStatus(stemp);
    }

    /**
    * @param args the command line arguments
    */
    static class Runner implements Runnable{
        FittingResultsNode resultNode;
        int nModel;
        public Runner(FittingResultsNode resultNode, int nModel){
            this.resultNode=resultNode;
            this.nModel=nModel;
//            run();
        }
        public void run(){
            ImageFittingGUI gui=new ImageFittingGUI(resultNode,nModel);
            gui.setVisible(true);
        }
    }
    public static void main(FittingResultsNode resultNodet, int nModel) {
        java.awt.EventQueue.invokeLater(new Runner(resultNodet, nModel));
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ImageFittingGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddOneTermBT;
    private javax.swing.JButton CloseAllBT;
    private javax.swing.JTextField ComponentTF;
    private javax.swing.JButton CurvePlotSettingBT;
    private javax.swing.JTextField DevPWS;
    private javax.swing.JButton DrawFunctinValueBT;
    private javax.swing.JCheckBox ExcludeOutliarsCB;
    private javax.swing.JButton FitDataBT;
    private javax.swing.JLabel FitterStatusLabel;
    private javax.swing.JComboBox FittingFunctionComponentChoiseCB;
    private javax.swing.JComboBox FittingMethodCB;
    private javax.swing.JLabel FittingParameters;
    private javax.swing.JButton FixAllBT;
    private javax.swing.JButton FreeAllBT;
    private javax.swing.JCheckBox FullModelFittingCB;
    private javax.swing.JLabel InfoTableLB;
    private javax.swing.JScrollPane InfoTableSP;
    private javax.swing.JTextField IterationNumberTF;
    private javax.swing.JButton IterationsBT;
    private javax.swing.JButton OneIterationBT;
    private javax.swing.JButton OptimizeOneComponent;
    private javax.swing.JCheckBox OutliarExclusionRegression;
    private javax.swing.JTextField OutliarPTF;
    private javax.swing.JScrollPane ParTablePane;
    private javax.swing.JButton PauseFittingBT;
    private javax.swing.JTextField RegressionOrderTF;
    private javax.swing.JButton RemoveOneTermBT;
    private javax.swing.JButton SelectCurveBT;
    private javax.swing.JButton ShowMeanSemBT;
    private javax.swing.JButton SmoothCurveBT;
    private javax.swing.JLabel SmoothingWSLB;
    private javax.swing.JTextField SmoothingWSTF;
    private javax.swing.JButton TerminateFittingBT;
    private javax.swing.JButton UpdateFittingResultsBT;
    private javax.swing.JButton UpdateTableBT;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JCheckBox keepOldDrawing;
    private javax.swing.JButton showDevP;
    // End of variables declaration//GEN-END:variables

    void calSignalRange(){
        int i;
        m_cSignalRange=new DoubleRange();
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=m_cCurrentModel.nDelta){
            m_cSignalRange.expandRange(m_cCurrentModel.m_pdY[i]);
        }
    }
   void createComponentColors(){
        m_cvComponentColors=new ArrayList();
        m_cvComponentColors.add(Color.red);
        m_cvComponentColors.add(Color.blue);
//        m_cvComponentColors.add(Color.yellow);
        m_cvComponentColors.add(Color.green);
        m_cvComponentColors.add(Color.pink);
        m_cvComponentColors.add(Color.cyan);
        m_cvComponentColors.add(Color.orange);
        m_cvComponentColors.add(Color.magenta);
        m_nNumComponentColors=m_cvComponentColors.size();
    }
    void addComponentColors(int nNumComponents){
        if(nNumComponents>m_nNumComponentColors){
            for(int i=m_nNumComponentColors;i<nNumComponents;i++){
                m_cvComponentColors.add(CommonMethods.randomColor());
            }
            m_nNumComponentColors=m_cvComponentColors.size();
        }
    }
    Color getComponentColor(int component){
        if(component<m_nNumComponentColors){
            return m_cvComponentColors.get(component);
        }
        return CommonMethods.randomColor();
    }
    void updateParTable(){
        createComponentColors();
        m_nvComponentIndexes=new ArrayList();
        String[] columnHead={"Par Names", "Fixed", "Values","Zero","Reset","Initial"};
        ArrayList<String> ParameterNames=new ArrayList();
        ArrayList<String> ResultItemNames=new ArrayList();
        ArrayList<Double> ParameterValues=new ArrayList();
        ArrayList<Double> ResultItemValues=new ArrayList();
        ArrayList<Integer> nvNumPars=new ArrayList();
        String[] blankLine={"   ","   ","   ","    ","    ","    "};
        m_cCurrentModel.retrieveFittingResults(ParameterNames, ParameterValues, ResultItemNames, ResultItemValues,nvNumPars);
        int i,rows=ParameterNames.size();
        Object[] row=new Object[3];
        Boolean b=false;
        ArrayList<Object[]> lines=new ArrayList();
        int nNumPars;
        int nComponents=nvNumPars.size(),j;
        m_nvParIndexes=new ArrayList();
        int[] pnFixedIndexes=m_cCurrentModel.getFixedParIndexes();
        int nPars=0;
        for(i=0;i<nComponents;i++){
            if(i==0){
                row=new Object[6];
                row[0]=ParameterNames.get(0);
                row[1]=new Boolean(pnFixedIndexes[nPars]==1);
                row[2]=new Double(ParameterValues.get(0));
                row[3]=new zeroTableButton();
                row[4]=new resetTableButton();
                row[5]=new Double(ParameterValues.get(0));
                lines.add(row);
                m_nvParIndexes.add(nPars);
                nPars++;
                m_nvComponentIndexes.add(i);
            }
            nNumPars=nvNumPars.get(i);
            for(j=0;j<nNumPars;j++){
                row=new Object[6];
                row[0]=ParameterNames.get(nPars);
                row[1]=new Boolean(pnFixedIndexes[nPars]==1);
                row[2]=new Double(ParameterValues.get(nPars));
                row[3]=new zeroTableButton();
                row[4]=new resetTableButton();
                row[5]=new Double(ParameterValues.get(nPars));
                lines.add(row);
                m_nvParIndexes.add(nPars);
                nPars++;
                m_nvComponentIndexes.add(i);
            }
            if(i==nComponents-1) break;
            row=new Object[6];
            row[0]="   ";
            row[1]="   ";
            row[2]="   ";
            row[3]="   ";
            row[4]="   ";
            row[5]="   ";
            m_nvComponentIndexes.add(-1);
            m_nvParIndexes.add(-1);
            lines.add(row);
        }
        addComponentColors(nComponents);
        rows=lines.size();
        Object[][] poData=new Object[rows][];

        for(i=0;i<rows;i++){
            row=lines.get(i);
            poData[i]=row;
        }

        JViewport viewport=new JViewport();
        m_cParTable=new JTable(new ParTableModel(columnHead,poData));
        CommonGuiMethods.setTableCellAlignmentH(m_cParTable, JLabel.RIGHT);
        m_cParTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        viewport.setView(m_cParTable);
        ParTablePane.setViewport(viewport);
        buttonColumns=new ArrayList();
        buttonColumns.add(3);
        buttonColumns.add(4);
        ButtonColumn button=new MultipleButtonColumn(m_cParTable, zeroAndrestParCell,buttonColumns);
        ComponentBackgroundMaker cc=new ComponentBackgroundMaker(m_cParTable,2);
        m_cParTable.getModel().addTableModelListener(this);
        m_cParTable.getSelectionModel().addListSelectionListener(this);
//        ButtonColumn buttonZ=new ButtonColumn(m_cParTable, zeroParCell,4);
    }
    void getParsFromParTable(ArrayList<String> svParNames, ArrayList<Double> dvParValues){
        int lines=m_cParTable.getRowCount(),i;
        String name;
        for(i=0;i<lines;i++){
            name=(String)m_cParTable.getValueAt(i, 0);
            if(CommonMethods.isWhightString(name)) continue;
            svParNames.add(name);
            dvParValues.add((Double)m_cParTable.getValueAt(i, 2));
        }
    }
    void updateFittingFunction(){
        m_cFunc=new ComposedFittingFunction(m_cCurrentModel.svFunctionTypes);
    }
    public ImagePlus getSourceImage(EventObject eo){
        ArrayList<ImagePlus> images=CommonMethods.getAllOpenImages();
        ImagePlus impl;
        int len=images.size();
        for(int i=0;i<len;i++){
            impl=images.get(i);
            if(impl.getWindow().getCanvas().equals(eo.getSource())) return impl;
        }
        return null;
    }

    void recordLocation(MouseEvent me){
        impl_Source=getSourceImage(me);
        m_cCursorOnScreenLocation=me.getLocationOnScreen();
        m_cCursorImageCoordinates=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl_Source);
    }
    public void tableChanged(TableModelEvent te){
        Object o=te.getSource();
        if(o==m_cParTable){
            o=o;
        }
        int col=te.getColumn();
        int fr=te.getFirstRow(),lr=te.getLastRow(),nParIndex;
        if(col==2){
            for(int i=fr;i<=lr;i++){
                nParIndex=m_nvParIndexes.get(i);
                if(nParIndex>=0) m_cCurrentModel.pdPars[nParIndex]=(Double)m_cParTable.getValueAt(i, col);
            }
        }
    }
    void updateFixedParIndexes(){
        int rows=m_cParTable.getModel().getRowCount();
        ArrayList <Integer> nvFixedIndexes=new ArrayList();
        for(int r=0;r<rows;r++){
            if(m_nvParIndexes.get(r)<0) continue;
            if((Boolean)m_cParTable.getValueAt(r, 1)==true) nvFixedIndexes.add(m_nvParIndexes.get(r));
        }
        int len=nvFixedIndexes.size();
        m_cCurrentModel.pnFixedParIndexes=new int[len];
        for(int r=0;r<len;r++){
            m_cCurrentModel.pnFixedParIndexes[r]=nvFixedIndexes.get(r);
        }
    }
    int updateFitter(){
        if(m_cFitter!=null){
            if(m_cFitter.isPausedFitting()) return -1;
        }
        updateFixedParIndexes();
        m_cCurrentModel.toGaussian2D();
        updateFittingFunction();
        m_cFitter=new Non_Linear_Fitter(m_cCurrentModel.m_pdX,m_cCurrentModel.m_pdY,m_cCurrentModel.pdPars,m_cFunc,m_cCurrentModel.MinimizationOption,
                m_cCurrentModel.MinimizationMethod,m_cCurrentModel.nI,m_cCurrentModel.nF,m_cCurrentModel.nDelta,m_cCurrentModel.pnFixedParIndexes,m_cCurrentModel);
        m_cFitter.addFitterListener(this);
        return 1;
    }
    void updateParTableCells(){
        int rows=m_cParTable.getRowCount(),index,r;
        int nPrecision;
        double dv;
        int[] pnFixedIndexes=m_cCurrentModel.getFixedParIndexes();
        int transform=m_cCurrentModel.toGaussian2D_GaussianPars();
        ArrayList<String> svParNames=m_cCurrentModel.svParNames;
        int parIndex;
        for(r=0;r<rows;r++){
            parIndex=m_nvParIndexes.get(r);
            if(parIndex<0) continue;
//            index=m_nvParIndexes.get(parIndex);
//            if(index<0) continue;
            dv=m_cCurrentModel.pdPars[parIndex];
            nPrecision=PrintAssist.getPrintingPrecisionF(dv, 0.01);
            m_cParTable.getModel().setValueAt(svParNames.get(parIndex), r, 0);
            m_cParTable.getModel().setValueAt((Double)dv, r, 2);
            m_cParTable.getModel().setValueAt(new Boolean (pnFixedIndexes[parIndex]==1), r, 1);
        }
        if(transform==1) m_cCurrentModel.toGaussian2D();
    }
    public void valueChanged(ListSelectionEvent e) {

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if(lsm.equals(m_cParTable.getSelectionModel())){
            int row=m_cParTable.getSelectedRow();
            int nComponent=m_nvComponentIndexes.get(row);
            ComponentTF.setText(""+nComponent);
        }
    }
    void showStatus(String status){
        FitterStatusLabel.setText(status);
    }
    void buildHistogram(PlotWindow pw){
        float[] pfX=pw.getXValues(),pfY=pw.getYValues();
        int nData=pfX.length,i;
        double[] pdX;
        double[] pdY=CommonStatisticsMethods.copyArray(pfY);
        Histogram hist=new Histogram(pdY);
        int nDim=hist.getDim();
        pdX=new double[nDim];
        pdY=new double[nDim];
        for(i=0;i<nDim;i++){
            pdX[i]=hist.getPosition(i);
            pdY[i]=hist.getCounts(i);
        }
        String PlotTitle="Histogram of "+pw.getTitle();
        String XTitle="Bin Center";
        String YTitle="Counts";
        PlotWindowPlus pwt=new PlotWindowPlus(pdX,pdY,PlotTitle,XTitle,YTitle,1,PlotWindowPlus.StepPlot,Color.black);
    }
    void updateCurveData(PlotWindow pw){
        float[] pfX=pw.getXValues(),pfY=pw.getYValues();
        int nData=pfX.length;
        String sPlotTitle=pw.getTitle();

        int shape=PlotWindow.LINE,lw=1;
        Color c=Color.BLACK;
        if(pw instanceof PlotWindowPlus){
            PlotWindowPlus pwp=(PlotWindowPlus)pw;
            shape=pwp.getPlotShape(0);
            lw=pwp.getPlotLineWidth(0);
            c=pwp.getPlotColor(0);
        }
        
        m_cPlotWindow=new PlotWindowPlus(CommonStatisticsMethods.copyArray(pfX),CommonStatisticsMethods.copyArray(pfY),"Fitting "+sPlotTitle,"X","Y",lw,shape,c);
        double pdX[][]=new double[nData][1],pdY[]=new double[nData];
        for(int i=0;i<nData;i++){
            pdX[i][0]=pfX[i];
            pdY[i]=pfY[i];
        }
        m_cCurrentModel=new FittingModelNode(pdX,pdY);
    }
    void drawFunctionValue(){
        int nWS=m_cPlotWindow.getSmoothingWS();
        int nComponents=m_cCurrentModel.nComponents,i,j;
        int nCurves=0;
        if(nComponents>0) nCurves=nComponents+1;
        DoubleRange cVR=m_cCurrentModel.getVarRange(0);
        double xI=cVR.getMin(),xF=cVR.getMax(),delta=(xF-xI)/m_nNumPointsInCurve,dY,dX;
        int nData=m_cCurrentModel.m_pdX.length;
        double pdXt[][]=new double[m_nNumPointsInCurve][1],pdX[]=new double[m_nNumPointsInCurve],pdY[]=new double[nComponents+1];

        ArrayList<double[]> pdvY=new ArrayList();
        for(i=0;i<nCurves;i++){
            pdvY.add(new double[m_nNumPointsInCurve]);
        }
        
        ComposedFittingFunction cFun=new ComposedFittingFunction(m_cCurrentModel.svFunctionTypes);
        double[] pdPars=m_cCurrentModel.pdPars;

        if(nCurves>0){
            for(i=0;i<m_nNumPointsInCurve;i++){
                dX=xI+i*delta;
                pdXt[i][0]=dX;
                pdX[i]=dX;
            }
            pdXt=m_cCurrentModel.getHistFittingReady(m_cCurrentModel.m_pdX,pdXt,m_cCurrentModel.svFunctionTypes,nWS);
            for(i=0;i<m_nNumPointsInCurve;i++){
                dY=cFun.fun(pdPars, pdXt[i], pdY);
                pdvY.get(0)[i]=dY;
                for(j=0;j<nComponents;j++){
                    pdvY.get(j+1)[i]=pdY[j+1];
                }
            }
        }
//        if(nComponents==1) pdvY.remove(0);
        int lw=2;
 //       clearFunctionValueCurves();
        m_cPlotWindow.removePlotGroup("Fitting Curve");
        Color c=Color.BLACK;
        for(i=0;i<pdvY.size();i++){
            if(i>0){
                lw=1;
                c=getComponentColor(i-1);
            }
            m_cPlotWindow.addPlot("Fitting Curve"+i, pdX, pdvY.get(i), lw, PlotWindow.LINE,c);
        }
    }
/*    void clearFunctionValueCurves(){
        int i,num=m_cPlotWindow.getNumCurves();
        for(i=1;i<num;i++){
            m_cPlotWindow.removeCurve(1);
        }
        m_cPlotWindow.refreshPlot();
    }*/
    int smoothCurrentCurve(){
        if(m_cPlotWindow==null) return -1;
        double[] pdY=m_cPlotWindow.getYValues_DP();
        int nWS=Integer.parseInt((String)SmoothingWSTF.getText());
        pdY=CommonStatisticsMethods.getRunningWindowAverage(pdY, 0, pdY.length-1, nWS, false);
        m_cPlotWindow.replaceYValues(pdY);
        m_cPlotWindow.setSmoothingWS(nWS);
        m_cPlotWindow.refreshPlot();
        return 1;
    }
    int showCurveMeanSem(){
        if(m_cPlotWindow==null) return -1;
        double[] pdY=m_cPlotWindow.getYValues_DP();
        MeanSem1 ms=new MeanSem1();
        if(ExcludeOutliarsCB.isSelected()){
            double pValue=Double.parseDouble((String)OutliarPTF.getText());
            ArrayList<Integer> indexes=new ArrayList();
            CommonStatisticsMethods.findOutliars(pdY,pValue,ms,indexes);
        }else{
            ms=CommonStatisticsMethods.buildMeanSem1(pdY, 0, pdY.length-1, 1);
        }
        String[][] psData=ms.getMeanSemAsStringArray();
        double dMin=ms.min,dMax=ms.max,mean=ms.mean,sd=ms.getSD();
        double dx=Math.max(mean-dMin, dMax-mean);
        double dp=1-GaussianDistribution.Phi(dx,mean,sd);
        String p=PrintAssist.ToStringScientific(dp, 3);

        int len=psData[0].length,i;
        String[][] psT=new String[2][len+1];
        for(i=0;i<len;i++){
            psT[0][i]=psData[0][i];
            psT[1][i]=psData[1][i];
        }
        psT[0][len]="maxP";
        psT[1][len]=p;
        JViewport jvp=CommonGuiMethods.buildCommonTableView(psT[0], psT, 1, 1);
        InfoTableSP.setViewport(jvp);
        return 1;
    }
    void closeAllPlotWindowPlus(){
        ArrayList <ImagePlus> ims=CommonMethods.getAllOpenImages();
        ImagePlus impl;
        int i,len=ims.size();
        Window wd;
        for(i=0;i<len;i++){
            impl=ims.get(i);
            wd=ims.get(i).getWindow();
            if(wd instanceof PlotWindowPlus) {
                wd=impl.getWindow();
                wd.dispose();
            }
        }
    }
    int showDevP(){
        int ws=getDevPWS();
        ImagePlus impl=CommonMethods.getCurrentImage();
        Window wd=impl.getWindow();
        double[] pdX=null, pdY=null;
        if(wd instanceof PlotWindowPlus || wd instanceof PlotWindow){
            PlotWindow pw=(PlotWindow)wd;
            float[] pfX=pw.getXValues(), pfY=pw.getYValues();
            pdX=CommonStatisticsMethods.copyArray(pfX);
            pdY=CommonStatisticsMethods.copyArray(pfY);
        };
        pdY=CommonStatisticsMethods.getRWLogDevP(pdY,ws,0);
        PlotWindowPlus pwp=new PlotWindowPlus(pdX,pdY,"Log p WS="+ws,"X","Log10(p)",2,PlotWindow.LINE,Color.BLACK);
        int i,len=pdX.length;
        double[] pdY05=new double[len],pdY01=new double[len],pdY001=new double[len],pdY0001=new double[len];
        CommonStatisticsMethods.setElements(pdY05, Math.log10(0.05));
        CommonStatisticsMethods.setElements(pdY01, Math.log10(0.01));
        CommonStatisticsMethods.setElements(pdY001, Math.log10(0.001));
        CommonStatisticsMethods.setElements(pdY0001, Math.log10(0.0001));
        pwp.addPlot("", pdX, pdY05, 1, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY01, 1, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY001, 1, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY0001, 1, PlotWindow.LINE, Color.BLACK);
        return 1;
    }
    public int getDevPWS(){
        return Integer.parseInt(DevPWS.getText());
    }
}
