/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import ij.WindowManager;
import utilities.CommonMethods;
import ij.gui.GenericDialog;
import java.util.ArrayList;
import utilities.io.PrintAssist;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.*;
import java.util.Formatter;
import ij.IJ;
import java.awt.Point;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import ij.gui.Roi;
import ij.gui.PointRoi;
import java.util.EventObject;
import ImageAnalysis.AnnotatedImagePlus;
import ij.gui.ImageCanvas;
import utilities.Gui.RoiHighlighter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.Dimension;
import java.awt.Graphics;
import utilities.Geometry.ImageShapes.*;
import ij.gui.PlotWindow;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.DoubleRange;
import java.util.StringTokenizer;
import java.awt.Container;
import ij.gui.Line;
import ij.gui.ImageWindow;
import utilities.Gui.PlotWindowPlus;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import utilities.Gui.PlotWindowPlus;
import utilities.statistics.LineSegmentRegressionEvaluater;
import utilities.Gui.Dialogs.OneTextFieldInputDialog;
import utilities.Gui.Dialogs.OneComboBoxInputDialog;
import java.awt.Component;
import utilities.Gui.AnalysisMasterForm;
import utilities.Gui.ComponentKeyboardAction;
import utilities.Gui.OneSchrollPaneFrame;
import utilities.statistics.MeanSem1;

/**
 *
 * @author Taihao
 */
public class CommonGuiMethods {
    public static void addImageSelectionChoices(GenericDialog gd, ArrayList <ImagePlus> imgs, String label){
        int len=imgs.size();
        ArrayList <String> vcItems=new ArrayList();

        String defaultImg=WindowManager.getCurrentImage().getTitle();

        int i, id=0;
        String sTitle;
        for(i=0;i<len;i++){
            sTitle=imgs.get(i).getTitle();
            vcItems.add(sTitle);
            if(sTitle.contentEquals(defaultImg)) id=i;
        }

        String[] items=new String[len];
        for(i=0;i<len;i++){
            items[i]=vcItems.get(i);
        }
        gd.addChoice(label, items, items[id]);
    }
    static public Formatter QuickFormatter (String path){
        Formatter fm=null;
        File file=new File(path);
        try {
                fm= new Formatter(file);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Fount");
        }
        return fm;
    }
    public static JFileChooser openFileDialogExtFilter(String sTitle,int type, String sFileType, String sExt, String sDir){//type=JFileChooser.OPEN_DIALOG or JFileChooser.SAVE_DIALOG
        StringTokenizer stk=new StringTokenizer(sExt," ,");
        int len=stk.countTokens();
        String[] psExt=new String[len];
        for(int i=0;i<len;i++){
            psExt[i]=stk.nextToken();
        }
        
        JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                sFileType, psExt);
            chooser.setFileFilter(filter);
            chooser.addChoosableFileFilter(filter);
            chooser.setDialogTitle(sTitle);
            chooser.setDialogType(type);
            chooser.setCurrentDirectory(new File(sDir));
            if(type==JFileChooser.OPEN_DIALOG){
                chooser.showOpenDialog(null);
            }else{
                chooser.showSaveDialog(null);
            }
            return chooser;
    }
    public static JFileChooser openFileDialogExtFilter(String sTitle,int type, ArrayList<String> sFileTypes, ArrayList<String> sExts, String sDir){//type=JFileChooser.OPEN_DIALOG or JFileChooser.SAVE_DIALOG
            JFileChooser chooser = new JFileChooser();
            int i,len=sFileTypes.size();
            FileNameExtensionFilter filter;
            for(i=0;i<len;i++){
                filter = new FileNameExtensionFilter(sFileTypes.get(i), sExts.get(i));
                if(i==0)
                    chooser.setFileFilter(filter);
                else
                    chooser.addChoosableFileFilter(filter);
            }
            chooser.setDialogTitle(sTitle);
            chooser.setDialogType(type);
            chooser.setCurrentDirectory(new File(sDir));
            if(type==JFileChooser.OPEN_DIALOG){
                chooser.showOpenDialog(null);
            }else{
                chooser.showSaveDialog(null);
            }
            return chooser;
    }
    public static int getColorIntoplation(int[]rgb1, double dv1, int[]rgb2, double dv2, double dv){
        int r=(int) (CommonMethods.interpolation(dv1, rgb1[0], dv2, rgb2[0], dv)+0.5);
        int g=(int) (CommonMethods.interpolation(dv1, rgb1[1], dv2, rgb2[1], dv)+0.5);
        int b=(int) (CommonMethods.interpolation(dv1, rgb1[2], dv2, rgb2[2], dv)+0.5);
        return (r<<16)|(g<<8)|b;
    }
    public static int getColorIntoplationWR(int[]rgb1, double dv1, int[]rgb2, double dv2, double dv, int rIndex){
        int[] rgb={(int) (CommonMethods.interpolation(dv1, rgb1[0], dv2, rgb2[0], dv)+0.5),(int) (CommonMethods.interpolation(dv1, rgb1[1], dv2, rgb2[1], dv)+0.5),(int) (CommonMethods.interpolation(dv1, rgb1[2], dv2, rgb2[2], dv)+0.5)};
        if(rIndex>=0&&rIndex<3) rgb[rIndex]=(int)(255*Math.random());
        return (rgb[0]<<16)|(rgb[1]<<8)|rgb[2];
    }
    public static Point getCursorLocation_ImageCoordinates(ImagePlus impl){
        if(impl==null) {
            return null;
        }
        ImageCanvas c=impl.getCanvas();
        if(c==null) return null;
        return c.getCursorLoc();
    }
    public static Point getImageLocationOnScreen(ImagePlus impl){
        ImageCanvas c=impl.getCanvas();
        if(c==null) return null;
        return impl.getCanvas().getLocationOnScreen();
    }
    public static void setTableCellAlignmentH(JTable table, int option){
        DefaultTableCellRenderer renderer=new DefaultTableCellRenderer();

        int rows=table.getRowCount(),cols=table.getColumnCount();
        renderer.setHorizontalAlignment(option);
        for(int i=0;i<cols;i++){
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
    public static void highlightPoints(ImagePlus impl, ArrayList<Point> points, Color c){
        int len=points.size();
        ArrayList<Roi> Rois=new ArrayList();
        Roi roi;
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            roi=new PointRoi(p.x,p.y);
            roi.setColor(c);
            Rois.add(roi);
        }
        RoiHighlighter.clearCurrentHighlighter();
        RoiHighlighter.addRoi(impl, Rois);
        RoiHighlighter.highLight();
    }
    public static void highlightPoint(ImagePlus impl, Point p, Color c){
        PointRoi pRoi=new PointRoi(p.x,p.y);
        pRoi.setImage(impl);
        pRoi.setColor(c);
        Graphics g=impl.getCanvas().getGraphics();
        if(g!=null)
            pRoi.draw(g);
        else
            p=p;
    }
    public static void highlightRoi(ImagePlus impl, Roi aRoi, Color c){
        aRoi.setImage(impl);
        aRoi.setColor(c);
        aRoi.draw(impl.getCanvas().getGraphics());
    }
    public static ImagePlus getSourceImage(EventObject eo){
        ArrayList<ImagePlus> images=CommonMethods.getAllOpenImages();
        ImagePlus implt=WindowManager.getCurrentImage();
        ImagePlus impl;
        int len=images.size();
        for(int i=0;i<len;i++){
            impl=images.get(i);
            if(impl.getWindow().getCanvas().equals(eo.getSource())) return impl;
        }
        return null;
    }

    public static AnnotatedImagePlus getAnnotatedSourceImage(EventObject eo,String sType0){
        ImagePlus impl=getSourceImage(eo);
        if(impl.getType()==AnnotatedImagePlus.Annotated){
            AnnotatedImagePlus impla=(AnnotatedImagePlus)impl;
            if(sType0.contentEquals("*")) return impla;
            if(impla.getDescription().equalsIgnoreCase(sType0)) return impla;
        }
        return null;
    }
    public static int setMagnification(ImagePlus impl, double mag){

        Point wlocation=impl.getWindow().getLocation();

        int ww=impl.getWindow().getWidth(),wh=impl.getWindow().getHeight();
        if(mag>32) mag=31.9;
        if(mag<0.3) mag=0.3;
        double oldMag=impl.getWindow().getCanvas().getMagnification();
        Rectangle rect=impl.getWindow().getCanvas().getSrcRect();
        int x0=rect.x+rect.width/2,y0=rect.y+rect.height/2;
        if(oldMag>mag){
            while(oldMag>mag){
                impl.getWindow().getCanvas().zoomOut(x0,y0);
                oldMag=impl.getWindow().getCanvas().getMagnification();
            }
        }else{
            while(oldMag<mag){
                impl.getWindow().getCanvas().zoomIn(x0,y0);
                oldMag=impl.getWindow().getCanvas().getMagnification();
            }
        }

        impl.getWindow().setLocationAndSize(wlocation.x, wlocation.y, ww, wh, false, false);
        return 1;
    }
    public static JTable autoResizeColWidth(JTable table, DefaultTableModel model) {
        //http://www.pikopong.com/blog/2008/08/13/auto-resize-jtable-column-width/ //11829
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        table.setModel(model);

        int margin = 5;

        for (int i = 0; i < table.getColumnCount(); i++) {
            int                     vColIndex = i;
            DefaultTableColumnModel colModel  = (DefaultTableColumnModel) table.getColumnModel();
            TableColumn             col       = colModel.getColumn(vColIndex);
            int                     width     = 0;

            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();

            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }

            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

            width = comp.getPreferredSize().width;

            // Get maximum width of column data
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, vColIndex);
                comp     = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
                        r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }

            // Add margin
            width += 2 * margin;

            // Set the width
            col.setPreferredWidth(width);
        }

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
            SwingConstants.LEFT);

        // table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }
    public static void addMouseListener(ImagePlus impl, MouseListener ml){
        MouseListener[] mls=impl.getWindow().getCanvas().getMouseListeners();
        int len=mls.length,i;
        boolean added=false;
        for(i=0;i<len;i++){
            if(ml==mls[i]){
                added=true;
                break;
            }
        }
        if(!added) impl.getWindow().getCanvas().addMouseListener(ml);
    }
    public static void optimizeWindowSize(ImagePlus impl){//this method adjust the window size of the image window to show the
        //entire image without change the magnification.
        impl.show();
        Dimension dimExtra=impl.getWindow().getExtraSize();
        int w=impl.getWidth(),h=impl.getHeight();
        double mag=impl.getWindow().getCanvas().getMagnification();
        Rectangle rectMax=impl.getWindow().getMaximumBounds();
        int wx=rectMax.width,hx=rectMax.height;
        int width=(int)(w*mag)+dimExtra.width,height=(int)(h*mag)+dimExtra.height;
        if(width>wx) width=wx;
        if(height>hx) height=hx;
        Point location=impl.getWindow().getLocationOnScreen();
        impl.getWindow().setLocationAndSize(location.x, location.y, width, height, false, false);
    }
    public static void drawRoiMeanTrace(ImagePlus impl,Roi roi){
        drawRoiMeanTrace(impl,roi,1,impl.getNSlices());
    }
    public static void drawRoiMeanTrace(ImagePlus impl, Roi roi, int sliceI, int sliceF){
        drawRoiMeanTrace(impl,roi,sliceI,sliceF,0,0);
    }
    public static void drawRoiMeanTrace(ImagePlus impl, Roi roi, int sliceI, int sliceF, int shiftX, int shiftY){
        int slice;
        ImageShape cIS=ImageShapeHandler.buildImageShape(roi);
        Point center=cIS.getCenter();
        center.translate(shiftX, shiftY);
        cIS.setCenter(center);

        double[] pdX=new double[sliceF-sliceI+1],pdY=new double[sliceF-sliceI+1];
        int w=impl.getWidth(),h=impl.getHeight();
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        int pixels[][]=new int[h][w];
        for(slice=sliceI;slice<=sliceF;slice++){
            impl.setSlice(slice);
            CommonMethods.getPixelValue(impl, slice, pixels);
            pdX[slice-sliceI]=slice;
            pdY[slice-sliceI]=ImageShapeHandler.getMean(pixels, cIS);
        }
        PlotWindow pw=new PlotWindow("Roi mean trace of "+impl.getTitle(),"slice","Roi Mean",pdX,pdY);
        pw.draw();
    }
    public static int getCoordinateRanges(PlotWindow pw, Roi roi,DoubleRange xRange,DoubleRange yRange){
        Rectangle rect=roi.getBoundingRect();
        Rectangle frame=pw.getPlotFrame();
        intRange cXRR=new intRange(rect.x,rect.x+rect.width-1),cYRR=new intRange(rect.y,rect.y+rect.height-1);
        intRange cXRF=new intRange(frame.x,frame.x+frame.width-1),cYRF=new intRange(frame.y,frame.y+frame.height-1);

        if(!cXRR.overlapped(cXRF)) return -1;
        cXRR.setCommonRange(cXRF);
        
        if(!cYRR.overlapped(cYRF)) return -1;
        cYRR.setCommonRange(cYRF);

        double[] coor1=new double[2],coor2=new double[2];
        getCoordinate(pw,cXRR.getMin(),cYRR.getMax(),coor1);
        getCoordinate(pw,cXRR.getMax(),cYRR.getMin(),coor2);
        xRange.setRange(coor1[0], coor2[0]);
        yRange.setRange(coor1[1], coor2[1]);
        return 1;
    }
    public static int getCoordinate(PlotWindow pw, int x, int y, double[] coor){//x,y is the coordinates on the image, coor stores the data value
        if(pw==null) return -1;
        pw.getCoordinate(x, y, coor);
        return 1;
    }
    public static Line getLine(PlotWindow pw,double dx0, double dy0, double dx1, double dy1){
        double[] coor0=new double[2],coor1=new double[2];
        Point p0=pw.getPixelCoordinates(dx0, dy0);
        Point p1=pw.getPixelCoordinates(dx1, dy1);
        return new Line(p0.x,p0.y,p1.x,p1.y,pw.getImagePlus());
    }
    public static JViewport getJViewport(ImageWindow pw){
        JFrame jf=new JFrame();
        JViewport jvp=new JViewport();
        Component[] components=pw.getComponents();
//        Container container=new Container();
//        container.setBounds(pw.getBounds());
//        jvp.setBounds(pw.getBounds());
        jf.setBounds(pw.getCanvas().getBounds());
        int len=components.length;
        for(int i=0;i<len;i++){
//            container.add(components[i]);
//            jvp.add(components[i]);
//            jf.add(components[i]);
        }
        jf.add(pw.getCanvas());
//        container.add(pw.getCanvas());
//        jvp.add(pw.getCanvas());
        jvp.setView(jf.getContentPane());
//        jvp.setView(pw.getCanvas());
        return jvp;
    }
    static public PlotWindow displayCurve(String title, String xLabel, String yLabel, ArrayList<Double> dvX, ArrayList<Double> dvY){
        int i,len=dvX.size();
        double[] pdX=new double[len],pdY=new double[len];
        for(i=0;i<len;i++){
            pdX[i]=dvX.get(i);
            pdY[i]=dvY.get(i);
        }
        PlotWindow pw=new PlotWindow(title, xLabel,yLabel,pdX,pdY);
        pw.draw();
        return pw;
    }
    static public void alignComponents(Component source, Component target, String option1, int margin2){//
        Rectangle rect1=source.getBounds(),rect2=target.getBounds();
        int x,y;
        if(option1.contentEquals("Left")) {
            x=rect1.x;
            y=rect1.y+rect1.height+margin2;
        }
        if(option1.contentEquals("Right")) {
            x=rect1.x+rect1.width-rect2.width;
            y=rect1.y+rect1.height+margin2;
        }
        if(option1.contentEquals("Top")) {
            y=rect1.y;
            x=rect1.x+rect1.width+margin2;
        }
        if(option1.contentEquals("Bottom")) {
            y=rect1.y+rect1.height-rect2.height;
            x=rect1.x+rect1.width+margin2;
        }
    }
    static public PlotWindow showPlot(String title, String xLabel, String yLabel, ArrayList<Double> dvX, ArrayList<Double> dvY){
        int i,len=Math.min(dvX.size(),dvY.size());
        double[] pfX=new double[len],pfY=new double[len];
        for(i=0;i<len;i++){
            pfX[i]=dvX.get(i).floatValue();
            pfY[i]=dvY.get(i).floatValue();
        }
        PlotWindowPlus pw=new PlotWindowPlus(pfX,pfY,title, xLabel,yLabel,1,PlotWindow.LINE,Color.BLACK);
        return pw;
    }
    /*
    static public JTable buildCommonTable(String[] columnHead, String[][] psData,int rowI, int rowF){
        int rows=rowF-rowI+1,i;
        Object[][] poData=new Object[rows][];

        for(i=rowI;i<=rowF;i++){
            poData[i-rowI]=psData[i];
        }

        JTable cTable=new JTable(poData,columnHead);
        CommonGuiMethods.setTableCellAlignmentH(cTable, JLabel.RIGHT);
        cTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        cTable=CommonGuiMethods.autoResizeColWidth(cTable, new javax.swing.table.DefaultTableModel());
        return cTable;
    }*/
    static public JTable buildCommonTable(String[] columnHead, Object[][] psData,int rowI, int rowF){
        int rows=rowF-rowI+1,i;
        Object[][] poData=new Object[rows][];

        for(i=rowI;i<=rowF;i++){
            poData[i-rowI]=psData[i];
        }

        JTable cTable=new JTable(poData,columnHead);
        CommonGuiMethods.setTableCellAlignmentH(cTable, JLabel.RIGHT);
        cTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        cTable=CommonGuiMethods.autoResizeColWidth(cTable, new javax.swing.table.DefaultTableModel());
        return cTable;
    }
    static public JViewport buildCommonTableView(String[] columnHead, String[][] psData,int rowI, int rowF){
        JViewport cTableViewport=new JViewport();
        cTableViewport.setView(buildCommonTable(columnHead,psData,rowI,rowF));
        return cTableViewport;
    }
    static public JViewport buildCommonTableView(String[] columnHead, Object[][] psData,int rowI, int rowF){
        JViewport cTableViewport=new JViewport();
        cTableViewport.setView(buildCommonTable(columnHead,psData,rowI,rowF));
        return cTableViewport;
    }
    static public PlotWindowPlus addPlot(PlotWindowPlus pw,String title,double[] pdX,double[] pdY,int lw,int shape,Color c,boolean show){
        if(pw!=null){
            pw.addPlot(title, pdX, pdY, lw, shape, c, show);
            return pw;
        }
        return displayNewPlotWindowPlus(title,pdX,pdY,lw,shape,c);
    }
    static public PlotWindowPlus displayNewPlotWindowPlus(String title,double[] pdX,double[] pdY,int lw,int shape,Color c){
        return new PlotWindowPlus(pdX,pdY,title,title+"_X",title+"_Y",lw,shape,c);
    }
    static public void displayLineSegment(PlotWindowPlus pw, PolynomialLineFittingSegmentNode seg, String title, int lw, int shape, Color c){
        ArrayList<double[]> pdvXY=new ArrayList();
        LineSegmentRegressionEvaluater.getPredictedValues(seg.getRegressionNode(), pdvXY);
        double[] pdX=pdvXY.get(0),pdY=pdvXY.get(1);        
        if(pdX.length>1) 
            if(pdX.length==pdY.length) pw.addPlot(title,pdX , pdY, lw, shape,c);
        else if(pdX.length==1){
            double x=pdX[0],y=pdY[0];
            pdX=new double[2];
            pdY=new double[2];
            pdX[0]=x;
            pdY[0]=y;
            pdX[1]=x;
            pdY[1]=y;
            pw.addPlot(title,pdvXY.get(0) , pdvXY.get(1), lw, shape,c);
        }
    }
    static public Color getDefaultColor(int index){
        switch (index){
            case 0:
                return Color.BLACK;
             case 1: 
                return Color.RED;
            case 2: 
                return Color.BLUE;
            case 3:
                return Color.CYAN;
            case 4:
                return Color.MAGENTA;
            case 5:
                return Color.GREEN;
            case 6:
                return Color.ORANGE;
            default:
                return CommonMethods.randomColor();
        }
    }
    static public Color getColorSelection(String option){
        if(option.contentEquals("Black")) return Color.black;
        if(option.contentEquals("Red")) return Color.RED;
        if(option.contentEquals("Blue")) return Color.blue;
        if(option.contentEquals("Cyan")) return Color.CYAN;
        if(option.contentEquals("Magenta")) return Color.MAGENTA;
        if(option.contentEquals("Yellow")) return Color.yellow;
        if(option.contentEquals("Red")) return Color.RED;
        if(option.contentEquals("Green")) return Color.GREEN;
        if(option.contentEquals("Orange")) return Color.ORANGE;
        if(option.contentEquals("Pink")) return Color.PINK;
        return CommonMethods.randomColor();
    }
    static public void addColorOptions(JComboBox box){
        box.addItem("Black");
        box.addItem("Red");
        box.addItem("Blue");
        box.addItem("Cyan");
        box.addItem("Magenta");
        box.addItem("Yellow");
        box.addItem("Red");
        box.addItem("Green");
        box.addItem("Orange");
        box.addItem("Pink");        
    }
    public static void showPValuesLog10(String title, double[] pdX, double[]pdY){
        int len=pdX.length;
        PlotWindowPlus pwp=new PlotWindowPlus(pdX,pdY,title,title+"_X","Log(p)",2,PlotWindow.LINE,Color.RED);
        double[] pdY05=new double[len],pdY01=new double[len],pdY001=new double[len],pdY0001=new double[len];
        double[] pdY00001=new double[len],pdY000001=new double[len];
        CommonStatisticsMethods.setElements(pdY05, Math.log10(0.05));
        CommonStatisticsMethods.setElements(pdY01, Math.log10(0.01));
        CommonStatisticsMethods.setElements(pdY001, Math.log10(0.001));
        CommonStatisticsMethods.setElements(pdY0001, Math.log10(0.0001));
        CommonStatisticsMethods.setElements(pdY00001, Math.log10(0.00001));
        CommonStatisticsMethods.setElements(pdY000001, Math.log10(0.000001));
        DoubleRange dr=CommonStatisticsMethods.getRange(pdY);
        pwp.addPlot("", pdX, pdY05, 1, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY01, 1, PlotWindow.LINE, Color.BLACK);
        if(dr.getMin()<-3)pwp.addPlot("", pdX, pdY001, 1, PlotWindow.LINE, Color.BLACK);
        if(dr.getMin()<-4)pwp.addPlot("", pdX, pdY0001, 1, PlotWindow.LINE, Color.BLACK);
        if(dr.getMin()<-5)pwp.addPlot("", pdX, pdY00001, 1, PlotWindow.LINE, Color.BLACK);
        if(dr.getMin()<-6)pwp.addPlot("", pdX, pdY000001, 1, PlotWindow.LINE, Color.BLACK);
    }
    public static PlotWindowPlus getCurrentPlotWindowPlus(){
        ImagePlus impl=CommonMethods.getCurrentImage();
        if(impl.getWindow() instanceof PlotWindowPlus) return (PlotWindowPlus) impl.getWindow();
        return null;
    }
    public static ArrayList<String>  inputTexts(String DialogTitle, ArrayList<String> labels, ArrayList<String> texts){
        OneTextFieldInputDialog dialog=OneTextFieldInputDialog.main(DialogTitle, labels,texts);
        return dialog.getTexts();
    }
    public static ArrayList<String>  getLoginInfo(String DialogTitle, ArrayList<String> labels, ArrayList<String> texts){
        OneTextFieldInputDialog dialog=OneTextFieldInputDialog.main(DialogTitle, labels,texts,true);
        ArrayList<String> svTexts=dialog.getTexts();
        svTexts.add(dialog.getPassword());
        return svTexts;
    }
    public static String getOneTextInput(String title, String name, String init){
        ArrayList<String> labels=new ArrayList(), inits=new ArrayList();
        labels.add(name);
        inits.add(init);
        ArrayList<String> values=inputTexts(title,labels,inits);
        return values.get(0);
    }
    public static int getOneInteger(String title, String varName, int nV){
        ArrayList<String> names=new ArrayList(), inits=new ArrayList();
        names.add(varName);
        inits.add(PrintAssist.ToString(nV));
        ArrayList<String> inputs=CommonGuiMethods.inputTexts(title, names, inits);
        return Integer.parseInt(inputs.get(0));
    }
    public static Object getOneComboBoxSelection(String title, String name, Object[] items){
        ArrayList<String> names=new ArrayList();
        ArrayList<Object[]> povItems=new ArrayList();
        names.add(name);
        povItems.add(items);
        ArrayList<Object> ovItems=getComboBoxSelections(title,names,povItems);
        return ovItems.get(0);
    }
    public static Object getOneComboBoxSelection(String title, String name, Object[] items,ArrayList<Component> cvComps){
        ArrayList<String> names=new ArrayList();
        ArrayList<Object[]> povItems=new ArrayList();
        names.add(name);
        povItems.add(items);
        ArrayList<Object> ovItems=getComboBoxSelections(title,names,povItems,cvComps);
        return ovItems.get(0);
    }
    public static ArrayList<Object> getComboBoxSelections(String DialogTitel, ArrayList<String> BoxNames, ArrayList<Object[]> povItems){
        OneComboBoxInputDialog dlg=OneComboBoxInputDialog.main(DialogTitel,BoxNames,povItems);
        ArrayList<Object> pvSelectedItems=new ArrayList();
        dlg.getSelectedItems(pvSelectedItems);
        return pvSelectedItems;
    }
    public static ArrayList<Integer> getComboBoxSelectionIndexes(String DialogTitel, ArrayList<String> BoxNames, ArrayList<Object[]> povItems){
        OneComboBoxInputDialog dlg=OneComboBoxInputDialog.main(DialogTitel,BoxNames,povItems);
        ArrayList<Integer> SelectionIndexes=new ArrayList();
        dlg.getSelectedIndexes(SelectionIndexes);
        return SelectionIndexes;
    }
    public static ArrayList<Object> getComboBoxSelections(String DialogTitel, ArrayList<String> BoxNames, ArrayList<Object[]> povItems,ArrayList<Component> cvComps){
        OneComboBoxInputDialog dlg=OneComboBoxInputDialog.main(DialogTitel,BoxNames,povItems,cvComps);
        ArrayList<Object> pvSelectedItems=new ArrayList();
        dlg.getSelectedItems(pvSelectedItems);
        return pvSelectedItems;
    }
    public static int resetItems(JComboBox box, ArrayList<Integer> nv){
        int i,len=nv.size();
        synchronized (box){
            ArrayList<ActionListener> cvAls=storeActionListeners(box);
            Object[] poItems=new Object[len];
            for(i=0;i<len;i++){
                poItems[i]=nv.get(i).toString();
            }
            resetItems(box,poItems);
            addActionListeners(box,cvAls);
        }
        return 1;
    }
    public static int resetItemsStrings(ActionListener al, JComboBox box, ArrayList<String> sv){
        if(sv==null) return -1;
        int i,len=sv.size();
        synchronized (box){
            Object[] poItems=new Object[len];
            for(i=0;i<len;i++){
                poItems[i]=sv.get(i);
            }
            resetItems(box,poItems);
        }
        return 1;
    }
    public static int resetItems(JComboBox box, Object[] poItems){
        synchronized (box){
            ArrayList<ActionListener> cvAls=storeActionListeners(box);
            clearBoxItems(box);
            int i,len=poItems.length;
            for(i=0;i<len;i++){
                box.addItem(poItems[i]);
            }
            addActionListeners(box,cvAls);
        }
        return 1;
    }
    public static int clearBoxItems(JComboBox box){        
        synchronized (box){
            ArrayList<ActionListener> cvAls=storeActionListeners(box);
            box.removeAllItems();
            addActionListeners(box,cvAls);
        }        
        return 1;
    }
    public static int setSelectedItem(ActionListener al, JComboBox box, String item){
        int i,len=box.getItemCount(),status =-1;
        synchronized (box){
            ArrayList<ActionListener> cvAls=storeActionListeners(box);
            for(i=0;i<len;i++){
                if(item.contentEquals((String)box.getItemAt(i))){
                    box.setSelectedIndex(i);
                    status=1;
                    break;
                }
            }
            addActionListeners(box,cvAls);
        }
        return status;
    }
    public static  ArrayList<ActionListener> storeActionListeners(JComboBox box){
        ActionListener[] als=(ActionListener[])box.getListeners(ActionListener.class);
        int i,len=als.length;
        ArrayList<ActionListener> cvAls=new ArrayList();
        ActionListener al;
        for(i=0;i<len;i++){
            al=als[i];
            cvAls.add(al);
            box.removeActionListener(al);
        }
        return cvAls;
    }
    public static void addActionListeners(JComboBox box, ArrayList<ActionListener> cvAls){
        for(int i=0;i<cvAls.size();i++){
            box.addActionListener(cvAls.get(i));
        }
    }
    public static int displayTable(String description,ArrayList<String[]> svData){
        int i,j,len=svData.size();
        if(len==0) return -1;
        int len1=svData.get(0).length;
        String[][] psData=new String[len][len1];
        for(i=0;i<len;i++){
            for(j=0;j<len1;j++){
                psData[i][j]=svData.get(i)[j];
            }
        }
        JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 1, len-1);
        OneSchrollPaneFrame fr=new OneSchrollPaneFrame();
        fr.display(description,jvp);
        fr.setVisible(true);
        return 1;
    }
    public static int displayTable(String discription, String[][] psData){        
        JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 1, psData.length-1);
        OneSchrollPaneFrame fr=new OneSchrollPaneFrame();
        fr.display(discription,jvp);
        fr.setVisible(true);
        return 1;
    }
    public static int displayTable(String discription, String[] psHead, Object[][] poData){        
        JViewport jvp=CommonGuiMethods.buildCommonTableView(psHead, poData, 0, poData.length-1);
        OneSchrollPaneFrame fr=new OneSchrollPaneFrame();
        fr.display(discription,jvp);
        fr.setVisible(true);
        return 1;
    }
    public static int displayTable(JScrollPane jsp, String description,String[][] psData){
        JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 1, psData.length-1);
        jsp.setViewport(jvp);
        return 1;
    }
    public static int appendText(OneSchrollPaneFrame fr, String text){
        
        return 1;
    }
    public static int selectString(JComboBox cb, String key){
        synchronized (cb){
            int num=cb.getItemCount(),index;
            String st;
            for(index=0;index<num;index++){
                st=(String)cb.getItemAt(index);
                if(st.contentEquals(key)){
                    cb.setSelectedIndex(index);
                    return 1;
                }
            }
            return -1;
        }
    }
    public static void showTempImage(ImagePlus impl){
        AnalysisMasterForm.addTempImage(impl);
        impl.show();
    }
    static public void showMessage(String message){
         JOptionPane jop=new JOptionPane(message);
         jop.setVisible(true);
    }
    static public void updateItem(JComboBox cb, String[] options){
        int i,items=options.length;
        Object[] poItems=new Object[items];
        for(i=0;i<items;i++){
            poItems[i]=options[i];
        }
        CommonGuiMethods.resetItems(cb, poItems);
    }
    static public void updateItem(JComboBox cb, ArrayList<String> options){
        int i,items=options.size();
        Object[] poItems=new Object[items];
        for(i=0;i<items;i++){
            poItems[i]=options.get(i);
        }
        CommonGuiMethods.resetItems(cb, poItems);
    }
    static public void updateTextFields(JTextField tf1, JTextField tf2, DoubleRange dr, int precision){
        tf1.setText(PrintAssist.ToString(dr.getMin(), precision));
        tf2.setText(PrintAssist.ToString(dr.getMax(), precision));
        tf1.setSize(tf1.getPreferredSize());
        tf2.setSize(tf2.getPreferredSize());
    }
    static public PlotWindowPlus getCombinedPlotWindows(ArrayList<PlotWindowPlus> pws){        
        int i,j,len=pws.size(),len1;
        if(len==0) return null;
        PlotWindowPlus pw=pws.get(0).copy();
        for(i=1;i<len;i++){
            pw.appendPlotWindow(pws.get(i));
        }
        pw.refreshPlot();
        return pw;
    }
    public static Component getTopLevelAncestor(Component c) {
        Component cp=c.getParent();
      while (cp != null) {
          c=cp;
//        if (c instanceof Window || c instanceof Applet)break;
            cp = c.getParent();
      }
      return c;
    }
    public static int makeClipboardCopiable(JFrame frame){
        if(frame==null) return -1;
        
        JPanel jp=(JPanel) frame.getContentPane();
        ComponentKeyboardAction scopy=new ComponentKeyboardAction("Cntr+Shift+C");
        KeyStroke stroke = KeyStroke.getKeyStroke(67,java.awt.event.InputEvent.CTRL_DOWN_MASK|java.awt.event.InputEvent.SHIFT_DOWN_MASK);
//        stroke = KeyStroke.getKeyStroke(67,java.awt.event.InputEvent.CTRL_DOWN_MASK);
//        InputMap IMap=SummaryOptionCB.getInputMap(JComponent.WHEN_FOCUSED);
//        ActionMap AMap=SummaryOptionCB.getActionMap();
        String actionName="scopy";
//        IMap.put(stroke,actionName);
        jp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke,actionName);
        jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke,actionName);
        jp.getActionMap().put(actionName, scopy);
        return 1;
    }
    public static int displaySummaryGraph(String WindowTitle, String GraphTitle, String yAxisTitle, ArrayList<String> svBarTitles, ArrayList<MeanSem1> cvMeanSems, boolean upward){
        int i,j,len=cvMeanSems.size(),len1,shape=0,r;
        MeanSem1 ms;
        PlotWindowPlus pw=null;
        DoubleRange drX=new DoubleRange();
        ArrayList<Double> dvXs=new ArrayList(),dvMeans=new ArrayList(),dvSems=new ArrayList();
        double x;
        for(i=0;i<len;i++){
            drX.expandRange(i);
            ms=cvMeanSems.get(i);
            dvXs.add((double)(i));
            dvMeans.add(ms.mean);
            dvSems.add(ms.sem);            
        }        
        ArrayList<Color> cvColors=new ArrayList();
        cvColors.add(CommonGuiMethods.getDefaultColor(0));
        ArrayList<Double> dvPVs=new ArrayList();
        double [] pdX={drX.getMin(),drX.getMax()}, pdY={drX.getMin(),drX.getMax()};
        
        pw=new PlotWindowPlus(pdX,pdY,WindowTitle," ", yAxisTitle,1,shape,Color.BLACK);
        
        ArrayList<Color> lColors=new ArrayList();
        Color c;
        int lw=2,barL=10,width=15;
        for(i=0;i<len;i++){
            c=CommonGuiMethods.getDefaultColor(i);
            lColors.add(c);
            pw.addBarGraphNode(dvXs.get(i), dvMeans.get(i), dvSems.get(i), c, lw, barL, width, true);
        }
        pw.setGraphTitle(GraphTitle);
        pw.removeCurve(0);
        pw.displayXAxisLabel(false);
        pw.setLegends(svBarTitles, lColors);
        pw.setLimit(drX.getMin()-0.5,drX.getMax()+0.5);
        pw.OptimizeBinWidth();
        return 1;
    }
}
