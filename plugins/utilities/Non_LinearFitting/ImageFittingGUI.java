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

/**
 *
 * @author Taihao
 */
public class ImageFittingGUI extends javax.swing.JFrame implements MouseMotionListener,MouseListener,TableModelListener,ListSelectionListener, ActionListener, PixelGrabberReceiver{
    class MultipleButtonColumn extends ButtonColumn{
        int rows,cols;
        ArrayList<Integer> columns;
        TableCellEditor originalCellEditor[][];
        TableCellRenderer originalCellRenderer[][];
        Color[][] originalBackgroundColor;
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

    AnnotatedImagePlus implDrawing,implComponent;
    ImageShape m_cIS;
    int holderSizeX;
    int[] m_pnDrawingVarIndexes;
    DoubleRange[] m_cDrawingVarRanges;
    double[] m_pdPixelSizes;
    DoubleRange m_cSignalRange;
    ArrayList<ImagePlus> m_cvOldDrawingImages;
    boolean keepOldDrawingImage;
    boolean m_bPausedFitting;

    ImagePlus implO,implF,implDiff;
    int[][] pixelsO,pixelsF,pixelsDiff;

    Point m_cCursorImageCoordinates,m_cCursorOnScreenLocation;
    ImagePlus impl_Source;//the image that get the mouse cursor

//    PixelGrabberDlg m_cPixelGrabber;
    PixelGrabberForm m_cPixelGrabber;

    Thread m_cFittingThread;
    boolean m_bFittingTerminated;
    boolean m_bDisplayInOriginalImage=false;
    AnalysisMasterForm m_cMasterForm;
    ArrayList<ImagePlus> cvFittingImages;
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

    public ImageFittingGUI() {
        initComponents();
        createComponentColors();
        cvFittingImages=new ArrayList();
        setSN();
        m_bPausedFitting=false;
    }

    public ImageFittingGUI(FittingResultsNode fittingResults, int nModel){
        this();
//        this.m_cResultsNode=fittingResults;
        m_cCurrentModel=new FittingModelNode(fittingResults.m_cvModels.get(nModel));
        updateParTable();
        m_cFunc=new ComposedFittingFunction(m_cCurrentModel.svFunctionTypes);
        m_cvOldDrawingImages=new ArrayList();
        calSignalRange();
        setupDrawingImage();
    }
    public void updateFittingModel(FittingModelNode cModel){
        m_cCurrentModel=cModel;
        updateParTable();
        m_cFunc=new ComposedFittingFunction(m_cCurrentModel.svFunctionTypes);
        m_cvOldDrawingImages=new ArrayList();
        calSignalRange();
        setupDrawingImage();
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
        jLabel1 = new javax.swing.JLabel();
        DrawingParIndexes = new javax.swing.JTextField();
        keepOldDrawing = new javax.swing.JCheckBox();
        highlightPointsBT = new javax.swing.JCheckBox();
        showPeaksBT = new javax.swing.JButton();
        ComponentTF = new javax.swing.JTextField();
        FittingFunctionComponentChoiseCB = new javax.swing.JComboBox();
        UpdateTableBT = new javax.swing.JButton();
        OptimizeOneComponent = new javax.swing.JButton();
        DrawComponentsBT = new javax.swing.JButton();
        PixelGrabbingBT = new javax.swing.JButton();
        FixAllBT = new javax.swing.JButton();
        FreeAllBT = new javax.swing.JButton();
        TerminateFittingBT = new javax.swing.JButton();
        UpdateFittingResultsBT = new javax.swing.JButton();
        FitterStatusLabel = new javax.swing.JLabel();
        DisplayInOriginalImageRB = new javax.swing.JRadioButton();
        FittingMethodCB = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        FullModelFittingCB = new javax.swing.JCheckBox();
        CloseFittingImageBT = new javax.swing.JButton();

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

        jLabel1.setText("Drawing Var Indexes");

        DrawingParIndexes.setText("0,1");
        DrawingParIndexes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DrawingParIndexesActionPerformed(evt);
            }
        });

        keepOldDrawing.setText("keepOldDrawing");
        keepOldDrawing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepOldDrawingActionPerformed(evt);
            }
        });

        highlightPointsBT.setText("highlight points");
        highlightPointsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highlightPointsBTActionPerformed(evt);
            }
        });

        showPeaksBT.setText("show peaks");
        showPeaksBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPeaksBTActionPerformed(evt);
            }
        });

        ComponentTF.setText("1");
        ComponentTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComponentTFActionPerformed(evt);
            }
        });

        FittingFunctionComponentChoiseCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "gaussian2D_Circular", "gaussian2D_GaussianPars", "exponential", "polynomial 2", "gaussian", "gaussian2D" }));

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

        DrawComponentsBT.setText("Draw Components");
        DrawComponentsBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DrawComponentsBTActionPerformed(evt);
            }
        });

        PixelGrabbingBT.setText("Grab pixels");
        PixelGrabbingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PixelGrabbingBTActionPerformed(evt);
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

        DisplayInOriginalImageRB.setText("Display in Original Image");
        DisplayInOriginalImageRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisplayInOriginalImageRBActionPerformed(evt);
            }
        });

        FittingMethodCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "simplex", "conjugated gradient", "levenberg marguardt" }));

        jLabel2.setText("Fitting Method");

        FullModelFittingCB.setText("Full model");

        CloseFittingImageBT.setText("Close Fitting Images");
        CloseFittingImageBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseFittingImageBTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ComponentTF, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(FittingFunctionComponentChoiseCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(RemoveOneTermBT)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(FullModelFittingCB)
                                .addGap(12, 12, 12)
                                .addComponent(OptimizeOneComponent))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(DrawingParIndexes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(IterationNumberTF, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(OneIterationBT)
                                    .addComponent(IterationsBT)
                                    .addComponent(AddOneTermBT)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel1)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(keepOldDrawing))
                                            .addComponent(DrawFunctinValueBT)))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(FitDataBT)
                                    .addComponent(PauseFittingBT))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(TerminateFittingBT)
                                    .addComponent(UpdateFittingResultsBT)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(FitterStatusLabel)))
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(FixAllBT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(FreeAllBT))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ParTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(FittingMethodCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(DisplayInOriginalImageRB, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(44, 44, 44)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(FittingParameters)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(highlightPointsBT)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(UpdateTableBT)
                                                    .addComponent(showPeaksBT)
                                                    .addComponent(DrawComponentsBT)
                                                    .addComponent(PixelGrabbingBT)))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(48, 48, 48)
                                        .addComponent(CloseFittingImageBT)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FixAllBT)
                    .addComponent(FreeAllBT))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(FittingParameters)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(highlightPointsBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(showPeaksBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UpdateTableBT)
                                .addGap(35, 35, 35)
                                .addComponent(DrawComponentsBT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(PixelGrabbingBT)
                                .addGap(14, 14, 14)
                                .addComponent(DisplayInOriginalImageRB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(FittingMethodCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addGap(32, 32, 32)
                                .addComponent(CloseFittingImageBT)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(ParTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DrawFunctinValueBT)
                            .addComponent(DrawingParIndexes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addGap(8, 8, 8)
                        .addComponent(FittingFunctionComponentChoiseCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addComponent(RemoveOneTermBT)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(OptimizeOneComponent)
                                    .addComponent(FullModelFittingCB))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(FitDataBT)
                                    .addComponent(UpdateFittingResultsBT)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addComponent(ComponentTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TerminateFittingBT)
                            .addComponent(PauseFittingBT))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(FitterStatusLabel)
                        .addGap(29, 29, 29))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ParTablePaneVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_ParTablePaneVetoableChange
        int i=0;// TODO add your handling code here:
    }//GEN-LAST:event_ParTablePaneVetoableChange

    private void IterationNumberTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IterationNumberTFActionPerformed
        int i=0;// TODO add your handling code here:
    }//GEN-LAST:event_IterationNumberTFActionPerformed

    private void DrawingParIndexesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DrawingParIndexesActionPerformed
        String text=DrawingParIndexes.getText();
        StringTokenizer stk=new StringTokenizer(text,",");
        m_pnDrawingVarIndexes=new int[2];// TODO add your handling code here:
        m_pnDrawingVarIndexes[0]=Integer.parseInt(stk.nextToken());
        m_pnDrawingVarIndexes[1]=Integer.parseInt(stk.nextToken());
        setupDrawingImage();
    }//GEN-LAST:event_DrawingParIndexesActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        int i=0;// TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void DrawFunctinValueBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DrawFunctinValueBTActionPerformed
        drawFunctionValues();
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
    public void addFittingImage(ImagePlus impl){
        cvFittingImages.add(impl);
    }
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
        updateParTable();
        drawFunctionValues();
        ComponentTF.setText(""+(m_cCurrentModel.nComponents-1));
    }//GEN-LAST:event_AddOneTermBTActionPerformed

    private void RemoveOneTermBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveOneTermBTActionPerformed
        int nComponent=Integer.parseInt(ComponentTF.getText());
        m_cCurrentModel.removeOneComponent(nComponent);
        updateParTable();
        drawFunctionValues();
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

//        m_cFitter.doFit_Apache();
        drawFunctionValues();
        updateParTableCells();
        showComponentPeaks();
        if(FullModelFittingCB.isSelected()){
//            m_cCurrentModel=fullModelFitting(m_cCurrentModel);
//            IPOGaussianFitter.getFullModelFitting(m_cCurrentModel, 0.0001);
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_FitDataBTActionPerformed
    private void keepOldDrawingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepOldDrawingActionPerformed
        keepOldDrawingImage=keepOldDrawing.isSelected();
    }//GEN-LAST:event_keepOldDrawingActionPerformed

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_formMouseMoved

    private void highlightPointsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightPointsBTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_highlightPointsBTActionPerformed

    private void showPeaksBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPeaksBTActionPerformed
        showComponentPeaks();// TODO add your handling code here:
    }//GEN-LAST:event_showPeaksBTActionPerformed

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

    private void DrawComponentsBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DrawComponentsBTActionPerformed
        // TODO add your handling code here:
        int nComponent=Integer.parseInt(ComponentTF.getText());
        drawComponent(nComponent);
    }//GEN-LAST:event_DrawComponentsBTActionPerformed

    private void PixelGrabbingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PixelGrabbingBTActionPerformed
/*        PixelGrabberDlg.main(null);        // TODO add your handling code here:
        PixelGrabberDlg.makeForm();
        int choice=PixelGrabberDlg.lastGrabber.getChoice();*/
//        PixelGrabberDlg.main(null);
//        m_cPixelGrabber=new PixelGrabberDlg(this,true);
        m_cPixelGrabber=new PixelGrabberForm(this);
        m_cPixelGrabber.addReceiver(this);
        m_cPixelGrabber.setVisible(true);
        m_bDisplayInOriginalImage=true;
        DisplayInOriginalImageRB.setSelected(true);
    }//GEN-LAST:event_PixelGrabbingBTActionPerformed

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

    private void DisplayInOriginalImageRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisplayInOriginalImageRBActionPerformed
        // TODO add your handling code here:
        m_bDisplayInOriginalImage=DisplayInOriginalImageRB.isSelected();
    }//GEN-LAST:event_DisplayInOriginalImageRBActionPerformed

    private void CloseFittingImageBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseFittingImageBTActionPerformed
        // TODO add your handling code here:
        closeFittingImages();
    }//GEN-LAST:event_CloseFittingImageBTActionPerformed
    void updateFittingResults(){
        if(m_cFitter!=null){
            m_cFitter.updateFittingModel();
        }
        updateParTableCells();
        drawFunctionValues();
        showComponentPeaks();
        displayInOriginalImage();
    }
    public void grabPixels(PixelGrabberForm grabber){
        double pdX[][]=grabber.getPixelCoordinates();
        double pdY[]=grabber.getPixels();
            if(pdX!=null&&pdY!=null){
                m_cCurrentModel=new FittingModelNode(pdX,pdY);
                updateParTable();
                m_cFunc=new ComposedFittingFunction(m_cCurrentModel.svFunctionTypes);
                m_cvOldDrawingImages=new ArrayList();
                calSignalRange();
                setupDrawingImage();
            }
        if(m_bDisplayInOriginalImage){
            boolean newO=false;
            ImagePlus implt=implO;
            implO=grabber.getOriginalImage();
            int[][] pno=pixelsO;
            pixelsO=grabber.getOriginalPixels();
            if(implO!=implt){
                if(!sameDimensions(pno,pixelsO)){
                    int w=pixelsO.length,h=pixelsO[0].length;
                    pixelsDiff=new int[h][w];
                    pixelsF=new int[h][w];
                }
                implF=CommonMethods.cloneImage(implO);
                implF.setTitle("Fitted Image");
                implDiff=CommonMethods.cloneImage(implO);
                implDiff.setTitle("Diff Image");
                CommonMethods.getPixelValue(implO, implO.getCurrentSlice(), pixelsF);
                CommonMethods.getPixelValue(implO, implO.getCurrentSlice(), pixelsDiff);
            }
        }
    }
    int displayInOriginalImage(){
        if(implF==null) return -1;
        m_cCurrentModel.updateFittedData();
        double pdY[]=m_cCurrentModel.m_pdY,pdX[][]=m_cCurrentModel.m_pdX,pdYF[]=m_cCurrentModel.pdFittedY;
        int i,len=pdY.length,x,y;
        double yo,yf,ydiff,dcon=m_cCurrentModel.pdPars[0];
        for(i=0;i<len;i++){
            x=(int)(pdX[i][0]+0.5);
            y=(int)(pdX[i][1]+0.5);
            yo=pdY[i];
            yf=pdYF[i];
            ydiff=yo-yf+dcon;
            pixelsF[y][x]=(int)yf;
            pixelsDiff[y][x]=(int)ydiff;
        }
        
//        implF.show();
//        implDiff.show();
        
        CommonGuiMethods.showTempImage(implF);
        CommonGuiMethods.showTempImage(implDiff);
        
        CommonMethods.setPixels(implF, pixelsF);
        CommonMethods.setPixels(implDiff, pixelsDiff);
        CommonMethods.refreshImage(implF);
        CommonMethods.refreshImage(implDiff);
        return 1;
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
        if(highlightPointsBT.isSelected()){
            AnnotatedImagePlus impla=CommonGuiMethods.getAnnotatedSourceImage(eo, "FittingComponents");
            FittingResultAnalyzerForm.highlightCorrespondingDataPositions(impla, m_cCursorImageCoordinates,Color.green);
        }
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
    private javax.swing.JButton CloseFittingImageBT;
    private javax.swing.JTextField ComponentTF;
    private javax.swing.JRadioButton DisplayInOriginalImageRB;
    private javax.swing.JButton DrawComponentsBT;
    private javax.swing.JButton DrawFunctinValueBT;
    private javax.swing.JTextField DrawingParIndexes;
    private javax.swing.JButton FitDataBT;
    private javax.swing.JLabel FitterStatusLabel;
    private javax.swing.JComboBox FittingFunctionComponentChoiseCB;
    private javax.swing.JComboBox FittingMethodCB;
    private javax.swing.JLabel FittingParameters;
    private javax.swing.JButton FixAllBT;
    private javax.swing.JButton FreeAllBT;
    private javax.swing.JCheckBox FullModelFittingCB;
    private javax.swing.JTextField IterationNumberTF;
    private javax.swing.JButton IterationsBT;
    private javax.swing.JButton OneIterationBT;
    private javax.swing.JButton OptimizeOneComponent;
    private javax.swing.JScrollPane ParTablePane;
    private javax.swing.JButton PauseFittingBT;
    private javax.swing.JButton PixelGrabbingBT;
    private javax.swing.JButton RemoveOneTermBT;
    private javax.swing.JButton TerminateFittingBT;
    private javax.swing.JButton UpdateFittingResultsBT;
    private javax.swing.JButton UpdateTableBT;
    private javax.swing.JCheckBox highlightPointsBT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JCheckBox keepOldDrawing;
    private javax.swing.JButton showPeaksBT;
    // End of variables declaration//GEN-END:variables

    void calSignalRange(){
        int i;
        m_cSignalRange=new DoubleRange();
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=m_cCurrentModel.nDelta){
            m_cSignalRange.expandRange(m_cCurrentModel.m_pdY[i]);
        }
    }
    void setupDrawingImage(){
        buildDrawingImageShape();
        if(keepOldDrawingImage) {
            m_cvOldDrawingImages.add(implDrawing);
        }
        holderSizeX=m_cIS.getXrange().getRange()+1;
        int h=m_cIS.getYrange().getRange(),w=3*holderSizeX-1;
        m_pnVarIndexes=new int[h][holderSizeX-1];
        ImagePlus implt=CommonMethods.getBlankImage(ImagePlus.GRAY32, w, h);
        double[][] ppdOriginalPixels=new double[1][w*h];

        int i,j;
        double[] corner=new double[2];
        float[] pixels=(float[])implt.getProcessor().getPixels();
        int len=pixels.length;
        double midpoint=m_cSignalRange.getMidpoint();
        for(i=0;i<len;i++){
            pixels[i]=(float)midpoint;
            ppdOriginalPixels[0][i]=midpoint;
        }
        double Y,Y0,dx;
        int[] position=new int[2];
        int x,y;
        for(i=0;i<2;i++){
            corner[i]=m_cDrawingVarRanges[m_pnDrawingVarIndexes[i]].getMin();
        }

        ArrayList<Point> points=new ArrayList();
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=m_cCurrentModel.nDelta){
            Y0=m_cCurrentModel.m_pdY[i];
            for(j=0;j<2;j++){
                dx=m_cCurrentModel.m_pdX[i][m_pnDrawingVarIndexes[j]];
                x=(int)((dx-corner[j])/m_pdPixelSizes[j]+0.5);
                position[j]=x;
            }
            x=position[0];
            y=position[1];
            pixels[y*w+x]=(float)Y0;
            ppdOriginalPixels[0][y*w+x]=Y0;
            m_pnVarIndexes[y][x]=i;
        }


        ArrayList<ImageAnnotationNode> cvAnnotations=new ArrayList();
        ArrayList<ImageAnnotationNode>[] pcvFittingModelAnnotations=new ArrayList[1];
        implDrawing=new AnnotatedImagePlus(implt);
        addFittingImage(implDrawing);

        FittingModelAnnotationNode annotation=new FittingModelAnnotationNode(implDrawing,m_cIS,m_pdPixelSizes,corner,0,null,1);
        annotation.displayLocation=new Point(0,0);
        annotation.type="FittingModelAnnotationNode";
        annotation.updateLocation();
        cvAnnotations.add(annotation);
        pcvFittingModelAnnotations[0]=cvAnnotations;

        implDrawing.setAnnotations(pcvFittingModelAnnotations);
        implDrawing.setOriginalValues(ppdOriginalPixels);
        implDrawing.setDescription("FittingComponents");
 //       implDrawing.storeNote(m_cFittingResultNodes, "FittingResultNodes");

        implDrawing.show();
        CommonGuiMethods.setMagnification(implDrawing, 8);
        CommonGuiMethods.optimizeWindowSize(implDrawing);
        Rectangle rect=this.getBounds();
        implDrawing.getWindow().setLocation(rect.x+rect.width, rect.y+rect.height/3);
        implDrawing.getWindow().getCanvas().addMouseMotionListener(this);
        implDrawing.getWindow().getCanvas().addMouseListener(this);
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
    void calDrawingPixelSizes(){
        m_pdPixelSizes=new double[2];
        int maxImageLength=2000;
        int nDataPoints=(m_cCurrentModel.nF-m_cCurrentModel.nI)/m_cCurrentModel.nDelta+1;
        int i,varIndex;
        double delta;
        for(i=0;i<2;i++){
            varIndex=m_pnDrawingVarIndexes[i];
            double[] pdT=new double[nDataPoints];
            CommonStatisticsMethods.copyArray_Column(m_cCurrentModel.m_pdX,varIndex,m_cCurrentModel.nI,m_cCurrentModel.nF,m_cCurrentModel.nDelta,pdT,0,1);
            delta=Histogram.getOptimalBinSize(pdT, maxImageLength);
            m_pdPixelSizes[i]=delta;
        }
    }
    void buildDrawingImageShape(){
        m_pdPixelSizes=new double[2];
        calDrawingVarRanges();
        calDrawingPixelSizes();
        double dx,dxn,pdXn[]=new double[2];
        int i,j,varIndex,x;
        int[] position=new int[2];
        ArrayList<Point> points=new ArrayList();
        for(i=0;i<2;i++){
            pdXn[i]=m_cDrawingVarRanges[i].getMin();
        }
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=m_cCurrentModel.nDelta){
            for(j=0;j<2;j++){
                varIndex=m_pnDrawingVarIndexes[j];
                dx=m_cCurrentModel.m_pdX[i][varIndex];
                x=(int)((dx-pdXn[j])/m_pdPixelSizes[j]+0.5);
                position[j]=x;
            }
            points.add(new Point(position[0],position[1]));
        }
        m_cIS=ImageShapeHandler.buildImageShape_Scattered(points);
    }
    void calDrawingVarRanges(){
        int i,j,varIndex;
        m_cDrawingVarRanges=new DoubleRange[2];
        m_pnDrawingVarIndexes=new int[2];
        for(i=0;i<2;i++){
            m_pnDrawingVarIndexes[i]=i;
            m_cDrawingVarRanges[i]=new DoubleRange();
        }
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=m_cCurrentModel.nDelta){
            for(j=0;j<2;j++){
                m_cDrawingVarRanges[j].expandRange(m_cCurrentModel.m_pdX[i][m_pnDrawingVarIndexes[j]]);
            }
        }
    }
    void drawFunctionValues(){
        updateFittingFunction();
        boolean b=keepOldDrawingImage;
        if(b){
            m_cvOldDrawingImages.add(implDrawing);
            setupDrawingImage();
        }
        int holderSizeX=m_cIS.getXrange().getRange()+1;
        int i,j,w=implDrawing.getWidth();
        double[] corner=new double[2];
        float[] pixels=(float[])implDrawing.getProcessor().getPixels();
        double[] originalPixels=implDrawing.getOriginalValues(1);
        double Y,Y0,dx;
        int[] position=new int[2];
        int x,y;
        for(i=0;i<2;i++){
            corner[i]=m_cDrawingVarRanges[m_pnDrawingVarIndexes[i]].getMin();
        }

        DoubleRange SignalRangeFunc=new DoubleRange(), SignalRangeDelta=new DoubleRange();
        double delta;
        ArrayList<Point> points=new ArrayList();
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=
                m_cCurrentModel.nDelta){
            Y0=m_cCurrentModel.m_pdY[i];
            Y=m_cFunc.fun(m_cCurrentModel.pdPars, m_cCurrentModel.m_pdX[i]);
            delta=Y0-Y;
            for(j=0;j<2;j++){
                dx=m_cCurrentModel.m_pdX[i][m_pnDrawingVarIndexes[j]];
                x=(int)((dx-corner[j])/m_pdPixelSizes[j]+0.5);
                position[j]=x;
            }
            x=position[0];
            y=position[1];
            SignalRangeFunc.expandRange(Y);
            SignalRangeDelta.expandRange(delta);
            points.add(new Point(position[0],position[1]));
            pixels[y*w+x+holderSizeX]=(float)Y;
            pixels[y*w+x+2*holderSizeX]=(float)delta;
            originalPixels[y*w+x+holderSizeX]=Y;
            originalPixels[y*w+x+2*holderSizeX]=delta;
        }
        int len=points.size();
        Point pt;

        double signalMidpoint=m_cSignalRange.getMidpoint(),adjustY=signalMidpoint-SignalRangeFunc.getMidpoint(),adjustD=signalMidpoint-SignalRangeDelta.getMidpoint();
        for(i=0;i<len;i++){
            pt=points.get(i);
            x=pt.x;
            y=pt.y;
            pixels[y*w+x+holderSizeX]+=adjustY;
            pixels[y*w+x+2*holderSizeX]+=adjustD;
        }
        CommonMethods.refreshImage(implDrawing);
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
    void showComponentPeaks(){
//        updatePars();
        Point[] peaks=getComponentPeakPositions();
        Point peak;
        int num=peaks.length,i;
        for(i=0;i<num;i++){
            peak=peaks[i];
            if(peak==null) continue;
            FittingResultAnalyzerForm.highlightCorrespondingDataPositions(implDrawing, peak, m_cvComponentColors.get(i));
        }
    }
    Point[] getComponentPeakPositions(){
        updateFittingFunction();
        Point[] points=m_cIS.getInnerPoints();
        int len=points.length,i,j,index,numComponents=m_cFunc.getNumComponents();
        Point p,peak=null;
        double yx=Double.NEGATIVE_INFINITY;
        double Y;
        Point[] peaks=new Point[numComponents];
        double[] pdYx=new double[numComponents],pdv=new double[numComponents+1];//including the constant term
        for(i=0;i<numComponents;i++){
            pdYx[i]=Double.NEGATIVE_INFINITY;
        }
        for(i=0;i<len;i++){
            p=points[i];
            index=m_pnVarIndexes[p.y][p.x];
            m_cFunc.fun(m_cCurrentModel.pdPars, m_cCurrentModel.m_pdX[index],pdv);
            for(j=0;j<numComponents;j++){
                Y=Math.abs(pdv[j+1]);//the first element is a constant
                if(Y>pdYx[j]){
                    pdYx[j]=Y;
                    peaks[j]=p;
                }
            }
        }
        return peaks;
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
        drawFunctionValues();
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
    void drawComponent(int nComponent){        
        updateFittingFunction();
        boolean b=keepOldDrawingImage;

        if(implComponent==null){
//            m_cvOldDrawingImages.add(implDrawing);
            setupComponentImage();
        }
        if(implComponent.getProcessor()==null){//the image is closed
//            m_cvOldDrawingImages.add(implDrawing);
            setupComponentImage();
        }
        implComponent.show();
        implComponent.setTitle("Component"+nComponent);
        int holderSizeX=m_cIS.getXrange().getRange()+1;
        int i,j,w=implComponent.getWidth();
        double[] corner=new double[2];
        float[] pixels=(float[])implComponent.getProcessor().getPixels();
        double[] originalPixels=implComponent.getOriginalValues(1);
        double Y,Y0,dx;
        int[] position=new int[2];
        int x,y;
        for(i=0;i<2;i++){
            corner[i]=m_cDrawingVarRanges[m_pnDrawingVarIndexes[i]].getMin();
        }

        DoubleRange SignalRangeFunc=new DoubleRange(), SignalRangeDelta=new DoubleRange();
        double delta;
        ArrayList<Point> points=new ArrayList();
        double[] pdv=new double[m_cCurrentModel.nComponents+1];//the first term is the constant
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=
                m_cCurrentModel.nDelta){
            Y0=m_cCurrentModel.m_pdY[i];
            m_cFunc.fun(m_cCurrentModel.pdPars, m_cCurrentModel.m_pdX[i],pdv);
            Y=pdv[nComponent+1];
            delta=Y0-Y;
            for(j=0;j<2;j++){
                dx=m_cCurrentModel.m_pdX[i][m_pnDrawingVarIndexes[j]];
                x=(int)((dx-corner[j])/m_pdPixelSizes[j]+0.5);
                position[j]=x;
            }
            x=position[0];
            y=position[1];
            SignalRangeFunc.expandRange(Y);
            SignalRangeDelta.expandRange(delta);
            points.add(new Point(position[0],position[1]));
            pixels[y*w+x+holderSizeX]=(float)Y;
            pixels[y*w+x+2*holderSizeX]=(float)delta;
            originalPixels[y*w+x+holderSizeX]=Y;
            originalPixels[y*w+x+2*holderSizeX]=delta;
        }
        int len=points.size();
        Point pt;

        double signalMidpoint=m_cSignalRange.getMidpoint(),adjustY=signalMidpoint-SignalRangeFunc.getMidpoint(),adjustD=signalMidpoint-SignalRangeDelta.getMidpoint();
        for(i=0;i<len;i++){
            pt=points.get(i);
            x=pt.x;
            y=pt.y;
            pixels[y*w+x+holderSizeX]+=adjustY;
            pixels[y*w+x+2*holderSizeX]+=adjustD;
        }
        CommonMethods.refreshImage(implComponent);
    }
    public void valueChanged(ListSelectionEvent e) {

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if(lsm.equals(m_cParTable.getSelectionModel())){
            int row=m_cParTable.getSelectedRow();
            int nComponent=m_nvComponentIndexes.get(row);
            ComponentTF.setText(""+nComponent);
        }
/*
        int firstIndex = e.getFirstIndex();
        int lastIndex = e.getLastIndex();
        boolean isAdjusting = e.getValueIsAdjusting();
        if (lsm.isSelectionEmpty()) {
//            output.append(" <none>");
        } else {
            // Find out which indexes are selected.
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
//                    output.append(" " + i);
                }
            }
        }*/
    }
    void setupComponentImage(){
//        buildDrawingImageShape();//already done so
        if(keepOldDrawingImage) {
//            m_cvOldDrawingImages.add(implDrawing);//always reuse
        }
//        holderSizeX=m_cIS.getXrange().getRange()+1;
        int h=m_cIS.getYrange().getRange(),w=3*holderSizeX-1;
//        m_pnVarIndexes=new int[h][holderSizeX-1];
        ImagePlus implt=CommonMethods.getBlankImage(ImagePlus.GRAY32, w, h);
        double[][] ppdOriginalPixels=new double[1][w*h];

        int i,j;
        double[] corner=new double[2];
        float[] pixels=(float[])implt.getProcessor().getPixels();
        int len=pixels.length;
        double midpoint=m_cSignalRange.getMidpoint();
        for(i=0;i<len;i++){
            pixels[i]=(float)midpoint;
            ppdOriginalPixels[0][i]=midpoint;
        }
        double Y,Y0,dx;
        int[] position=new int[2];
        int x,y;
        for(i=0;i<2;i++){
            corner[i]=m_cDrawingVarRanges[m_pnDrawingVarIndexes[i]].getMin();
        }

        ArrayList<Point> points=new ArrayList();
        for(i=m_cCurrentModel.nI;i<=m_cCurrentModel.nF;i+=m_cCurrentModel.nDelta){
            Y0=m_cCurrentModel.m_pdY[i];
            for(j=0;j<2;j++){
                dx=m_cCurrentModel.m_pdX[i][m_pnDrawingVarIndexes[j]];
                x=(int)((dx-corner[j])/m_pdPixelSizes[j]+0.5);
                position[j]=x;
            }
            x=position[0];
            y=position[1];
            pixels[y*w+x]=(float)Y0;
            ppdOriginalPixels[0][y*w+x]=Y0;
            m_pnVarIndexes[y][x]=i;
        }


        ArrayList<ImageAnnotationNode> cvAnnotations=new ArrayList();
        ArrayList<ImageAnnotationNode>[] pcvFittingModelAnnotations=new ArrayList[1];
        implComponent=new AnnotatedImagePlus(implt);
        addFittingImage(implComponent);

        FittingModelAnnotationNode annotation=new FittingModelAnnotationNode(implComponent,m_cIS,m_pdPixelSizes,corner,0,null,1);
        annotation.displayLocation=new Point(0,0);
        annotation.type="FittingModelAnnotationNode";
        annotation.updateLocation();
        cvAnnotations.add(annotation);
        pcvFittingModelAnnotations[0]=cvAnnotations;

        implComponent.setAnnotations(pcvFittingModelAnnotations);
        implComponent.setOriginalValues(ppdOriginalPixels);
        implComponent.setDescription("FittingComponents");
 //       implDrawing.storeNote(m_cFittingResultNodes, "FittingResultNodes");

        implComponent.show();
        implComponent.getWindow().getCanvas().addMouseMotionListener(this);
        implComponent.getWindow().getCanvas().addMouseListener(this);
    }
    void showStatus(String status){
        FitterStatusLabel.setText(status);
    }
    public void closeFittingImages(){
        for(int i=0;i<cvFittingImages.size();i++){
            cvFittingImages.get(i).close();
        }
        cvFittingImages.clear();
    }
}
