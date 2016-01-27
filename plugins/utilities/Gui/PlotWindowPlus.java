
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import ij.gui.PlotWindow;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.StringTokenizer;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.awt.Color;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.PointRoi;
import utilities.CommonGuiMethods;
import java.awt.Point;
import utilities.Gui.PlotWindowTuner;
import ij.gui.Plot;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CustomDataTypes.DoubleRange;
import java.awt.Rectangle;
import utilities.Gui.GeneralSchrollPaneDisplayingFrame;
import javax.swing.JViewport;
import utilities.io.PrintAssist;
import utilities.CustomDataTypes.intRange;
import java.awt.Point;
import ij.WindowManager;
import ij.gui.Toolbar;
import ij.IJ;
import java.awt.*;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import ij.ImagePlus;
import javax.swing.JLabel;
import utilities.statistics.LineFeatureExtracter2;
import utilities.CustomDataTypes.DoublePair;
import ij.gui.PolygonRoi;
import utilities.statistics.GaussianDistribution;
import java.awt.FontMetrics;
import java.awt.Font;
import ij.gui.TextRoi;

/**
 *
 * @author Taihao
 */
public class PlotWindowPlus extends PlotWindow implements MouseListener, MouseMotionListener, WindowListener, ComponentListener{
    class cursor{        
        cursor primary,secondary;
        Color c;
        String label;
        int curveIndex;
        double x,y;
        ArrayList<Integer> curveIndexes;
        public cursor(cursor primary, String label,Color c){
            this.primary=primary;
            if(primary==null){
                curveIndexes=new ArrayList();
            }
            secondary=null;
            x=0.5*(m_dXI+m_dXF);
            y=getActiveCurveValue(x);
            this.label=label;
            this.c=c;
            double pdX[]={dXCursor,dXCursor},pdY[]={m_dYI-marginY,m_dYF+marginY};
            curveIndex=pdvPlotDataX.size();
            if(primary==null) curveIndexes.add(curveIndex);//only the primary cursor keeps tracking of the cursor curveIndexes
            addPlot(label,pdX,pdY,1,PlotWindow.LINE,c,false);
        }
        void update(){
            x=dXCursor;
            y=getActiveCurveValue(x);
        }
        public int draw(boolean displayStatus){
            drawCursorLine(dXCursor);
            if(secondary!=null)secondary.drawCursorLine(secondary.x);
            displayCursorCoordinates();
            if(!displayStatus) return 1;
            if(secondary==null) 
                displayStatus();
            else {
                double x=secondary.x;
                if(x>=m_dXI&&x<=m_dXF){
                    secondary.displayStatus();
                }else{
                    displayStatus();
                }
            }
            return 1;
        }
        public void drawCursorLine(double x){
            double[] pdX=pdvPlotDataX.get(curveIndex),pdY=pdvPlotDataY.get(curveIndex);
            pdX[0]=x;
            pdX[1]=x;
            pdY[0]=m_dYI;
            pdY[1]=m_dYF;
            float[] pfX=pfvPlotDataX.get(curveIndex),pfY=pfvPlotDataY.get(curveIndex);
            pfX[0]=(float)x;
            pfX[1]=(float)x;
            pfY[0]=(float)m_dYI;
            pfY[1]=(float)m_dYF;
        }
        int displayStatus(){
            String status="";
            int activeIndex=getActiveCurveIndex();
            if(activeIndex<0) activeIndex=0;
            if(primary!=null){//this is the secondary
                status+="Index0= "+getXIndex_Roundoff(activeIndex,x)+"  Cursors: X0= "+getXString(x)+"   Y0= "+getYString(y);
//                status+="  X= "+getXString(dXCursor)+"   Y= "+getYString(getActiveCurveValue(dXCursor));
                status+="  dX= "+getXString(dXCursor-x)+"  dY= "+getYString(getActiveCurveValue(dXCursor)-y);
            }else{
                if(activeIndex>cvDisplayRangesX.size()){
                    activeIndex=activeIndex;
                    return -1;
                }
//                status+="Index= "+getXIndex_Roundoff(activeIndex,dXCursor)+"  Cursor X= "+getXString(dXCursor)+"   Y= "+getYString(getActiveCurveValue(dXCursor));                
                status+="Index= "+getXIndex_Roundoff(activeIndex,dXCursor)+"  Cursor X= "+getXString(dXCursor)+"   Y= "+PrintAssist.ToStringScientific(getActiveCurveValue(dXCursor),3);                
            }
            handler.setCursorStatus(status);
            return 1;
        }
        void adjustCurveIndex(int index_removedCurve){
            if(isPrimary()){
                if(index_removedCurve<curveIndex) {
                    curveIndex--;
                    curveIndexes.set(0, curveIndex);
                }
                if(secondary!=null) {
                    secondary.adjustCurveIndex(index_removedCurve);
                    curveIndexes.set(1, secondary.curveIndex);
                }
            }else{
                if(index_removedCurve<curveIndex) curveIndex--;
            }
        }
        boolean isPrimary(){
            return primary==null;
        }
        boolean isSecondary(){
            return primary!=null;
        }
        public void record(){
            if(secondary==null){ 
                secondary=new cursor(this,"secondary",Color.BLUE);
                curveIndexes.add(secondary.curveIndex);
            } else {
                secondary.x=dXCursor;
                secondary.y=getActiveCurveValue(dXCursor);
            }   
            refreshPlot();
        }
        public ArrayList<Integer> getCursorCurveIndexes(){
            return curveIndexes;
        }
    }
    class YErrorbars{
        double[] pdX,pdY,pdErrors;
        int lw,barL;
        DoubleRange xRange,yRange;
        Color c;
        public YErrorbars(double[] pdX, double[] pdY, double[] pdErrors, Color c){
            lw=1;
            barL=10;
            this.pdX=pdX;
            this.pdY=pdY;
            this.pdErrors=pdErrors;
            calRanges();
        }
        public YErrorbars(double[] pdX, double[] pdY, double[] pdErrors, Color c, int barL, int lw){
            this.lw=lw;
            this.barL=barL;
            this.pdX=pdX;
            this.pdY=pdY;
            this.pdErrors=pdErrors;
            this.c=c;
            calRanges();
        }
        public DoubleRange getXRange(){
            return xRange;
        }
        public DoubleRange getYRange(){
            return yRange;
        }
        
        public DoubleRange getYRange(DoubleRange xRange){
            DoubleRange yRange=new DoubleRange();
            double dy=getYLength(lw);
            for(int i=0;i<pdY.length;i++){
                if(!xRange.contains(pdX[i])) continue;
                yRange.expandRange(pdY[i]-pdErrors[i]-dy);
                yRange.expandRange(pdY[i]+pdErrors[i]+dy);
            }            
            return yRange;
        }
        
        public DoubleRange getXRange(DoubleRange xRange0){
            DoubleRange xRange=new DoubleRange();            
            double x,dx=getXLength(barL);
            for(int i=0;i<pdY.length;i++){
                x=pdX[i];
                if(!xRange0.contains(x)) continue;
                xRange.expandRange(x-dx);
                xRange.expandRange(x+dx);
            }            
            return xRange;
        }
        void calRanges(){
            xRange=new DoubleRange();
            yRange=new DoubleRange();
            xRange.expandRange(pdX);
            xRange.expandRange(xRange.getMin()-getXLength(barL));
            xRange.expandRange(xRange.getMax()+getXLength(barL));
            double dy=getYLength(lw);
            for(int i=0;i<pdY.length;i++){
                yRange.expandRange(pdY[i]-pdErrors[i]-dy);
                yRange.expandRange(pdY[i]+pdErrors[i]+dy);
            }            
        }
        public int draw(){
            int i,len=pdX.length;
            ImagePlus impl=getImagePlus();
            ImageProcessor ip=impl.getProcessor();
            impl.getProcessor().setColor(c);
            Point pp,pn;
            double dx,dy,error;
            for(i=0;i<len;i++){
                dx=pdX[i];
                dy=pdY[i];
                error=pdErrors[i];
                pn=getPixelCoordinates(dx,dy-error);
                pp=getPixelCoordinates(dx,dy+error);
                ip.drawLine(pn.x, pn.y, pp.x, pp.y);
                ip.drawLine(pn.x-barL, pn.y, pn.x+barL, pn.y);
                ip.drawLine(pp.x-barL, pp.y, pp.x+barL, pp.y);
            }
            return 1;
        }
        public void setColor(Color c){
            this.c=c;
        }
    }
    class BarGraphNode{
        double x,y,yError;
        Color c;
        DoubleRange xRange,yRange;
        boolean Upward;
        int lw,barL, width;        
        public BarGraphNode(double x,double y,double yError, Color c){
            lw=1;
            barL=10;
            width=15;
            this.x=x;
            this.y=y;
            this.yError=yError;
            this.c=c;
            Upward=true;
            calRanges();
        }
        void calRanges(){
            double dy0=getYLength(lw),dx0=getXLength(barL);
            xRange=new DoubleRange(x-dx0,x+dx0);
            yRange=new DoubleRange();
            if(Upward)
                yRange.setRange(y, y+yError+dy0);
            else
                yRange.setRange(y-yError-dy0, y);
            
        }
        public BarGraphNode(double x,double y,double yError, Color c,int lw, int barL, int width, boolean Upward){
            this(x,y,yError,c);
            this.lw=lw;
            this.barL=barL;
            calRanges();
        }
        public int draw(){
            drawBox();
            drawErrorBar();
            return 1;
        }
        void drawBox(){
            Point p0=getPixelCoordinates(x,y),pn=getPixelCoordinates(x,y);
            if(Upward)
                pn.y=plotFrame.y+plotFrame.height+ExtraTopMargin;
            else
                pn.y=plotFrame.y+ExtraTopMargin;
            
            Point LeftTop=new Point(p0.x-width/2,p0.y),RightTop=new Point(p0.x+width/2,p0.y),LeftBottom=new Point(p0.x-width/2,pn.y),RightBottom=new Point(p0.x+width/2,pn.y);
            ImageProcessor ip=getImagePlus().getProcessor();
            Color c0=ip.getDrawingColor();
            int lw0=ip.getLineWidth();
            ip.setColor(c);
            ip.setLineWidth(lw);
            ip.drawLine(LeftBottom.x, LeftBottom.y, LeftTop.x, LeftTop.y);
            ip.drawLine(LeftTop.x, LeftTop.y, RightTop.x, RightTop.y);
            ip.drawLine(RightTop.x, RightTop.y, RightBottom.x, RightBottom.y);
            ip.setColor(c0);
            ip.setLineWidth(lw0);
        }
        void drawErrorBar(){
            double y1=y+yError;
            if(!Upward) y1=y-yError;
            Point p0=getPixelCoordinates(x,y),pp=getPixelCoordinates(x,y1);
            Point Left=new Point(p0.x-barL/2,pp.y),Right=new Point(p0.x+barL/2,pp.y);
            ImageProcessor ip=getImagePlus().getProcessor();
            Color c0=ip.getDrawingColor();
            int lw0=ip.getLineWidth();
            ip.setColor(c);
            ip.setLineWidth(lw);
            ip.drawLine(p0.x,p0.y,pp.x,pp.y);
            ip.drawLine(Left.x,Left.y,Right.x,Right.y);
            ip.setColor(c0);
            ip.setLineWidth(lw0);
        }
        public double getX(){
            return x;
        }
        public DoubleRange getYRange(){
            return yRange;
        }
        public DoubleRange getXRange(){
            return xRange;
        }
    }
    class SummaryNode{
        //draw a box centered point corresponding (x, mean). The lower and upper edges of the box indicate mean-sd and mean+sd, respectively.
        //It also draw lines segements indicating the posisions corresponding the p Values specified in dvPs. 
        double x,mean,sd;
        ArrayList<Double> dvPValues;
        ArrayList<Double> dvDeltaY;
        ArrayList<Color> cvColors;
        Color c;
        DoubleRange xRange,yRange;
        int n,lw,barL;        
        public SummaryNode(double x,double mean,double sd,int n,Color c, ArrayList<Double> dvPs,ArrayList<Color> colors){
            lw=1;
            barL=10;
            this.x=x;
            this.mean=mean;
            this.sd=sd;
            this.n=n;
            dvPValues=new ArrayList();
            cvColors=new ArrayList();
            for(int i=0;i<dvPs.size();i++){
                dvPValues.add(dvPs.get(i));
            }
            for(int i=0;i<colors.size();i++){
                cvColors.add(colors.get(i));
            }
            this.c=c;
            calRanges();
        }
        void calRanges(){
            double y,dy,dy0=getYLength(lw),dx0=getXLength(barL);
            xRange=new DoubleRange(x-dx0,x+dx0);
            yRange=new DoubleRange();
            dvDeltaY=new ArrayList();
            for(int i=0;i<dvPValues.size();i++){
                y=GaussianDistribution.getZatP(dvPValues.get(i), mean, sd, 0.0001*sd);
                dy=Math.abs(y-mean);
                dvDeltaY.add(dy);
                yRange.expandRange(mean-dy-dy0);
                yRange.expandRange(mean+dy+dy0);
            }
        }
        public SummaryNode(double x,double mean,double sd,int n,Color c,ArrayList<Double> dvPs,ArrayList<Color> colors, int barL, int lw){
            this(x,mean,sd,n,c,dvPs,colors);
            this.lw=lw;
            this.barL=barL;
            calRanges();
        }
        public int draw(){
            int i,xl,xr,yn,yp,xc;
            ImagePlus impl=getImagePlus();
            ImageProcessor ip=impl.getProcessor();
            Point pc,pp,pn,pnb,ppb;
            double delta;
            ip.setLineWidth(lw);
            
            //drawing the center line
            pc=getPixelCoordinates(x,mean);
            xc=pc.x;
            xl=xc-barL;
            xr=xc+barL;
            impl.setColor(c);
            ip.drawLine(xl, pc.y, xr, pc.y);
            
            //drawing SD box
            pnb=getPixelCoordinates(x,mean-sd);
            ppb=getPixelCoordinates(x,mean+sd);
            yn=pnb.y;
            yp=ppb.y;
            ip.drawLine(xl,yn,xl,yp);
            ip.drawLine(xl,yp,xr,yp);
            ip.drawLine(xr,yp,xr,yn);
            ip.drawLine(xr,yn,xl,yn);

            //drawing outliar indicaters
            intRange ir=new intRange();
            for(i=0;i<dvDeltaY.size();i++){
                delta=dvDeltaY.get(i);
                pn=getPixelCoordinates(x,mean-delta);
                pp=getPixelCoordinates(x,mean+delta);   
                yn=pn.y;
                yp=pp.y;
                ip.setColor(cvColors.get(i));
                ip.drawLine(xl,yp,xr,yp);
                ip.drawLine(xr,yn,xl,yn);
                ir.expandRange(yn);
                ir.expandRange(yp);
            }
            //drawing vertical lines
            yp=ir.getMax();
            yn=ppb.y;
            ip.setColor(c);
            ip.drawLine(xc, yn, xc, yp);
            yn=ir.getMin();
            yp=pnb.y;
            ip.drawLine(xc, yn, xc, yp);
            return 1;
        }
        public double getX(){
            return x;
        }
        public DoubleRange getYRange(){
            return yRange;
        }
        public DoubleRange getXRange(){
            return xRange;
        }
    }
    public static final int StepPlot=101,MaxPlotListSize=5;
    public static PlotHandlerGui handler;
    ArrayList<String> svPlotTitles;
    ArrayList<double[]> pdvPlotDataX;
    ArrayList<double[]> pdvPlotDataY;
    ArrayList<YErrorbars> cvErrorbars;
    ArrayList<SummaryNode> cvSummaryNodes;
    ArrayList<BarGraphNode> cvBarGraphNodes;
    ArrayList<float[]>pfvPlotDataX;
    ArrayList<float[]>pfvPlotDataY;
    ArrayList<Integer> nvPlotLineWidths;
    ArrayList<Integer> nvPlotShapes;
    ArrayList<DoubleRange> cvDisplayRangesX;
    ArrayList<DoubleRange> cvDisplayRangesY;
    ArrayList<TextRoi> cvTextRois;
//    public PlotWindow pw;
    public String plotOption;
    double[] pdX,pdY;
    double m_dXI, m_dXF,m_dYI,m_dYF;
    public ArrayList<Color> m_cvDefaultPlotColors;
    public ArrayList<Color> m_cvPlotColors;
    DoubleRange cDataRangeX,cDataRangeY,cDisplayRangeX,cDisplayRangeY;
    String xLabel,yLabel;
    int m_nSmoothingWS;
    int PlotHeight,PlotWidth;
    Point MouseStart;
    double magX,magY,dXCursor;
    double m_dMarginRatioX,m_dMarginRatioY,marginX,marginY;
    int nImageWidth,nImageHeight;
    boolean bFreezeCursor;
    int m_nActiveCurveIndex;
    private Button link;
    private Button list, save, copy;
    private Label coordinates;
    ArrayList<PlotWindowPlus> m_cvLinkedPlotWindows;
    boolean bSynchScales,bSynchCursor;
    public static ArrayDeque <PlotWindowPlus> PlotList=new ArrayDeque();
    double xAccuracy, yAccuracy;
    ArrayList<ActionListener> m_cvActionListeners;
    String m_sCoors;
    ImagePlus impl;
    Rectangle plotFrame;
    boolean autoYScale;
    Point mousePoint;
    double[] pdMouseCoors;
    cursor m_cCursor;
    LineFeatureExtracter2 cFeatureExtracter;
    boolean bProtected;
    String sGraphTitle,sXAxisTitle,sYAxisTitle;
    boolean bClickToSelectCurve;
    String m_sPlotWindowTitle;
    String m_sPlotType;
    Font FontGraphTitle,FontLegend;
    Point GraphTitlePosition;
    Color cGraphTitle,cXTitle,cYTitle;
    
    ArrayList<String> svLegends;
    ArrayList<Color> cvLegendColors;
    ArrayList<Point> cvLegendPositions;
    
    boolean displayXAxisLabel;

    public PlotWindowPlus(){
        bProtected=false;
    }

    public void init(){
        addWindowListener(this);
        bProtected=false;
        m_cvActionListeners=new ArrayList();
        if(handler==null) {
            handler=new PlotHandlerGui();
            handler.setVisible(true);
        }
        handler.addPlotWindow(this);
        handler.setAsCurrentPlotWindow(this);
        addActionListener(handler);
        xAccuracy=Double.POSITIVE_INFINITY;
        yAccuracy=Double.POSITIVE_INFINITY;
        svPlotTitles=new ArrayList();
        pdvPlotDataX=new ArrayList();
        pdvPlotDataY=new ArrayList();
        pfvPlotDataX=new ArrayList();
        pfvPlotDataY=new ArrayList();
        nvPlotLineWidths=new ArrayList();
        nvPlotShapes=new ArrayList();
        m_cvPlotColors=new ArrayList();
        cDataRangeX=new DoubleRange();
        cDataRangeY=new DoubleRange();
        cvErrorbars=new ArrayList();
        cvTextRois=new ArrayList();
        m_cvLinkedPlotWindows=new ArrayList();
        m_cvLinkedPlotWindows.add(this);

        m_cvDefaultPlotColors=new ArrayList();

        m_cvDefaultPlotColors.add(Color.red);
        m_cvDefaultPlotColors.add(Color.blue);
        m_cvDefaultPlotColors.add(Color.green);
        m_cvDefaultPlotColors.add(Color.magenta);
        m_cvDefaultPlotColors.add(Color.orange);
        m_cvDefaultPlotColors.add(Color.pink);
        cvDisplayRangesX=new ArrayList();
        cvDisplayRangesY=new ArrayList();
        cDisplayRangeX=new DoubleRange();
        cDisplayRangeY=new DoubleRange();
        cvSummaryNodes=new ArrayList();
        cvBarGraphNodes=new ArrayList();
        m_dMarginRatioX=0.01;
        m_dMarginRatioY=0.02;
        bFreezeCursor=false;
        m_nActiveCurveIndex=-1;
        bSynchScales=true;
        bSynchCursor=true;
        mousePoint=null;
        pdMouseCoors=new double[2];
        getImagePlus().getWindow().addComponentListener(this);
        sGraphTitle=" ";
        sGraphTitle=null;
        FontGraphTitle=new Font("default",Font.BOLD,16);
        FontLegend=new Font("default",Font.BOLD,14);
        cGraphTitle=Color.BLACK;
        cXTitle=Color.BLACK;
        cYTitle=Color.BLACK;
        displayXAxisLabel=true;
    }
	public void draw() {
		Panel buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		list = new Button(" List ");
		list.addActionListener(this);
		buttons.add(list);
		save = new Button("Save...");
		save.addActionListener(this);
		buttons.add(save);
		copy = new Button("Copy...");
		copy.addActionListener(this);
		buttons.add(copy);
                link=new Button("Link..");
                link.addActionListener(this);
                buttons.add(link);
		coordinates = new Label("X=123, Y=123");
		coordinates.setFont(new Font("Monospaced", Font.PLAIN, 12));
		buttons.add(coordinates);
                super.setButtons(list, save, copy, coordinates);
		add(buttons);
		drawPlot();
		pack();
//		coordinates.setText("");
                plotFrame=getPlotFrame();
		ImageProcessor ip = getPlot().getProcessor();
		if ((ip instanceof ColorProcessor) && (imp.getProcessor() instanceof ByteProcessor))
			imp.setProcessor(null, ip);
		else
			imp.updateAndDraw();
		if (listValues)
			showPlotData();
	}

	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==link)
			linkPlotWindowPlus();
		else if (b==list)
			showPlotData();
		else
			super.actionPerformed(e);
	}
        public String getCoordinate(){
            return coordinates.getText();
        }
        public PlotWindowPlus getPreviousPlotWindowPlus(){
            if(PlotList.isEmpty()) return null;
            PlotWindowPlus pw=PlotList.getLast();
            if(pw==this){
                PlotList.pollLast();
                if(PlotList.isEmpty())
                    pw=null;
                else
                    pw=PlotList.getLast();
            }
            addToPlotList(this);
            return pw;
        }
        void linkPlotWindowPlus(){
            PlotWindowPlus pw=getPreviousPlotWindowPlus();
            if(pw!=null) {
                ArrayList<PlotWindowPlus> pws=pw.getLinkedPlotWindowPlus();
                int i,len=pws.size();
                for(i=0;i<len;i++){
                    pw=pws.get(i);
                    if(!m_cvLinkedPlotWindows.contains(pw)) m_cvLinkedPlotWindows.add(pw);
                }
                len=m_cvLinkedPlotWindows.size();
                for(i=0;i<len;i++){
                    pw=m_cvLinkedPlotWindows.get(i);
                    pw.setLinkedPlotWindowPlus(m_cvLinkedPlotWindows);
                }
            }
        }
        void setLinkedPlotWindowPlus(ArrayList<PlotWindowPlus> pws){
            m_cvLinkedPlotWindows=pws;
        }
        ArrayList<PlotWindowPlus> getLinkedPlotWindowPlus(){
            return m_cvLinkedPlotWindows;
        }
        void synchScales(boolean synch){
            bSynchScales=synch;
        }

        void synchCursor(boolean synch){
            bSynchCursor=synch;
        }

    public PlotWindowPlus(ArrayList<Double> dvY, String plotTitle, String xTitle, String yTitle, int lw, int shape, Color c){
        this(CommonStatisticsMethods.copyToDoubleArray(getDefaultXAsDoubleArrayList(dvY.size())),CommonStatisticsMethods.copyToDoubleArray(dvY),plotTitle,xTitle,yTitle,lw,shape,c);
    }
    public PlotWindowPlus(ArrayList<Double> dvX, ArrayList<Double> dvY, String plotTitle, String xTitle, String yTitle, int lw, int shape, Color c){
        this(CommonStatisticsMethods.copyToDoubleArray(dvX),CommonStatisticsMethods.copyToDoubleArray(dvY),plotTitle,xTitle,yTitle,lw,shape,c);
    }
    public static ArrayList<Double> getDefaultXAsDoubleArrayList(int len){
        ArrayList<Double> dv=new ArrayList();
        for(int i=0;i<len;i++){
            dv.add((double)i+1.);
        }
        return dv;
    }

    public PlotWindowPlus(double[] pdX, double[] pdY, String PlotWindowTitle, String plotTitle, String xTitle, String yTitle, int lw, int shape, Color c){
        super(PlotWindowTitle,xTitle,yTitle,pdX,pdY);
        m_sPlotWindowTitle=PlotWindowTitle;
        draw();
        init();
        initPlot(pdX,pdY,plotTitle,xTitle,yTitle,lw,shape,c);
    }
    public String getPlotWindowTitle(){
        return m_sPlotWindowTitle;
    }
    public PlotWindowPlus(double[] pdX, double[] pdY, String plotTitle, String xTitle, String yTitle, int lw, int shape, Color c){
        this(pdX,pdY,new String(plotTitle),plotTitle,xTitle,yTitle,lw,shape, c);
    }

    void initPlot(double[] pdX, double[] pdY, String plotTitle, String xTitle, String yTitle, int lineWidth, int shape, Color c){
        autoYScale=true;
        this.pdX=pdX;
        this.pdY=pdY;
//        pw=new PlotWindow(plotTitle,xTitle,yTitle,pdX,pdY);
//        calPlotAccuracy(pdX,pdY);
        ImagePlus impl=getImagePlus();
        getCanvas().addMouseListener(this);
        getCanvas().addMouseMotionListener(this);
        nImageWidth=impl.getWidth();
        nImageHeight=impl.getHeight();
        PlotHeight=super.getImagePlus().getHeight();
        PlotWidth=super.getImagePlus().getWidth();
        xLabel=xTitle;
        yLabel=yTitle;
        ArrayList<Double> dvX=new ArrayList(),dvY=new ArrayList();
        pdvPlotDataX.add(pdX);
        pdvPlotDataY.add(pdY);
        pfvPlotDataX.add(CommonStatisticsMethods.copyToFloatArray(pdX));
        pfvPlotDataY.add(CommonStatisticsMethods.copyToFloatArray(pdY));
        svPlotTitles.add(plotTitle);
        nvPlotLineWidths.add(lineWidth);
        nvPlotShapes.add(shape);
        m_cvPlotColors.add(c);
        cvDisplayRangesX.add(CommonStatisticsMethods.getRange(pdX));
        cvDisplayRangesY.add(CommonStatisticsMethods.getRange(pdY));
        DoubleRange drX=CommonStatisticsMethods.getRange(pdX);
        DoubleRange drY=CommonStatisticsMethods.getRange(pdY);
        setLimits(drX.getMin(),drX.getMax(),drY.getMin(),drY.getMax());
        calDataRange();
    }
    void calMagnification(){
        magX=nImageWidth/(m_dXF-m_dXI);
        magY=nImageHeight/(m_dYF-m_dYI);
    }
    public void addPlot(String title, double[] pdX, double[] pdY, int lw, int shape){
        addPlot(title,pdX,pdY,lw,shape,getNewPlotColor(),true);
    }
    public void addPlot(String title, double[] pdX, double[] pdY, int lw, int shape,Color c){
        addPlot(title,pdX,pdY,lw,shape,c,true);
    }
    public void addPlot(String title, ArrayList<Double> dvX, ArrayList<Double> dvY, int lw, int shape, Color c, boolean bDraw){
        int len=dvX.size();
        double[] pdX=new double[len],pdY=new double[len];
        for(int i=0;i<len;i++){
            pdX[i]=dvX.get(i);
            pdY[i]=dvY.get(i);
        }
        addPlot(title, pdX, pdY,lw,shape,c, bDraw);
    }
    public void addPlot(String title, ArrayList<Double> dvY, int lw, int shape, Color c, boolean bDraw){
        int len=dvY.size();
        double[] pdX=new double[len],pdY=new double[len];
        for(int i=0;i<len;i++){
            pdX[i]=i+1;
            pdY[i]=dvY.get(i);
        }
        addPlot(title, pdX, pdY,lw,shape,c, bDraw);
    }
    public int addPlot(String title, double[] pdX, double[] pdY, int lw, int shape,Color c,boolean bDraw){
        if(pdX.length==0) return -1;
        if(pdX.length!=pdY.length){
            int len=Math.min(pdX.length, pdY.length);
            pdX=CommonStatisticsMethods.getTrimmedDoubleArray(pdX, 0, len-1, 1);
            pdY=CommonStatisticsMethods.getTrimmedDoubleArray(pdY, 0, len-1, 1);
        }
        pdvPlotDataX.add(pdX);
        pdvPlotDataY.add(pdY);
        pfvPlotDataX.add(CommonStatisticsMethods.copyToFloatArray(pdX));
        pfvPlotDataY.add(CommonStatisticsMethods.copyToFloatArray(pdY));
        svPlotTitles.add(title);
        nvPlotLineWidths.add(lw);
        nvPlotShapes.add(shape);
        m_cvPlotColors.add(c);
        cvDisplayRangesX.add(new DoubleRange(pdX[0],pdX[pdX.length-1]));
        cvDisplayRangesY.add(CommonStatisticsMethods.getRange(pdY));
        displayCurveList();
        if(!bDraw) return -1;
        calDisplayRange();
        calDataRange();
        refreshPlot();
        return 1;
    }

    public Color getNewPlotColor(){
        int plots=pdvPlotDataX.size(),colors=m_cvDefaultPlotColors.size();
        if(plots<colors) return m_cvDefaultPlotColors.get(plots);
        return CommonMethods.randomColor();
    }
    public void addMouseListener(MouseListener ml){
        getCanvas().addMouseListener(ml);
    }
    public String getCoordinates(){
        return getCoordinate();
    }
    Point getCursorLocation(){
        return getMousePosition();
    }
    public int highlightCurrentDataPoint(int x, int y, Color c){
        ImagePlus im=getImagePlus();
        CommonGuiMethods.highlightPoint(im, new Point(x,y), c);
        return 1;
    }
    public int highlightDataSelection(double[] pdX0, boolean pbSelected[], Color c){
        removeSelectionHighlights();
        ArrayList<intRange> cvSelectedRanges=CommonStatisticsMethods.getSelectedRanges(pbSelected);
        int lw=2,shape=PlotWindow.LINE;
        int i,len=cvSelectedRanges.size();
        intRange ir;
        double[] pdXT,pdYT;
        double xt,yt;
        int index=getActiveCurveIndex();
        double[] pdX,pdY;
        if(index>=0){
            pdX=getXValues(index);
            pdY=getYValues(index);
        }else{
            pdX=this.pdX;
            pdY=this.pdY;
        }
        intRange irt;
        for(i=0;i<len;i++){
            irt=cvSelectedRanges.get(i);
            ir=CommonStatisticsMethods.getXIndexRange(pdX,new DoubleRange(pdX0[irt.getMin()],pdX0[irt.getMax()]));
            if(ir.getRange()>=2){
                pdXT=CommonStatisticsMethods.copyArrayToArray(pdX,ir.getMin(),ir.getMax(),1);
                pdYT=CommonStatisticsMethods.copyArrayToArray(pdY,ir.getMin(),ir.getMax(),1);
            }else{
                pdXT=new double[2];
                pdYT=new double[2];
                xt=pdX[ir.getMin()];
                yt=pdY[ir.getMin()];
                pdXT[0]=xt;
                pdXT[1]=xt;
                pdYT[0]=yt;
                pdYT[1]=yt;
            }
            addPlot("SelectedRegion"+i,pdXT,pdYT,lw,shape,c,false);
        }
        refreshPlot();
        return 1;
    }
    public void removeSelectionHighlights(){
        removePlotGroup("SelectedRegion");
    }
    public int setLimit(double xI, double xF){
        if(!autoYScale) {
            setLimits(xI,xF,m_dYI,m_dYF);
            return -1;
        }
        double yI=Double.POSITIVE_INFINITY,yF=Double.NEGATIVE_INFINITY,xt,yt;
        int i,j,len=pdvPlotDataX.size();
        for(i=0;i<len;i++){
            for(j=0;j<pdvPlotDataX.get(i).length;j++){
                xt=pdvPlotDataX.get(i)[j];
                if(xt<xI||xt>xF) continue;
                yt=pdvPlotDataY.get(i)[j];
                if(yt>yF) yF=yt;
                if(yt<yI) yI=yt;
            }
        }
        
        len=cvErrorbars.size();
        DoubleRange drx0=new DoubleRange(xI,xF),drx=new DoubleRange(xI,xF),dry=new DoubleRange(yI,yF);
        for(i=0;i<len;i++){
            drx.expandRange(cvErrorbars.get(i).getXRange(drx0));
            dry.expandRange(cvErrorbars.get(i).getYRange(drx0));
        }
        xI=drx.getMin();
        xF=drx.getMax();
        yI=dry.getMin();
        yF=dry.getMax();
        setLimits(xI,xF,yI,yF);
        return 1;
    }
    public DoubleRange getYRange(double xI, double xF){
        DoubleRange dR=new DoubleRange(),dRX=new DoubleRange(xI,xF);
        double yI=Double.POSITIVE_INFINITY,yF=Double.NEGATIVE_INFINITY,xt,yt;
        int i,j,len=pdvPlotDataX.size(),len1;
        if(m_cCursor==null) {
            m_cCursor=new cursor(null,"cursor",Color.RED);
        }
        ArrayList<Integer> cvCursorCurveIndexes=m_cCursor.getCursorCurveIndexes();
        for(i=0;i<len;i++){
            if(CommonMethods.containsContent(cvCursorCurveIndexes, i)) continue;
            for(j=0;j<pdvPlotDataX.get(i).length;j++){
                xt=pdvPlotDataX.get(i)[j];
                if(xt<xI||xt>xF) continue;
                yt=pdvPlotDataY.get(i)[j];
                dR.expandRange(yt);
            }
        }
        len=cvErrorbars.size();
        YErrorbars e;
        for(i=0;i<len;i++){
            e=cvErrorbars.get(i);
            dR.expandRange(e.getYRange(dRX));
        }
        
        SummaryNode sNode;
        double x;
        for(i=0;i<cvSummaryNodes.size();i++){
            sNode=cvSummaryNodes.get(i);
            x=sNode.getX();
            if(x<xI||x>xF) continue;
            dR.expandRange(sNode.getYRange());
        }
        
        BarGraphNode bNode;
        for(i=0;i<cvBarGraphNodes.size();i++){
            bNode=cvBarGraphNodes.get(i);
            x=bNode.getX();
            if(x<xI||x>xF) continue;
            dR.expandRange(bNode.getYRange());
        }
        return dR;
    }
    void updateDisplayRanges(double xI,double xF, double yI, double yF){
        int i,len=cvDisplayRangesX.size();
        for(i=0;i<len;i++){
            cvDisplayRangesX.get(i).setRange(xI,xF);
            cvDisplayRangesY.get(i).setRange(yI,yF);
        }
    }
    public void refreshPlot(){
        int shape;
        int i,j,len=pdvPlotDataX.size();
        if(pdvPlotDataX.size()!=m_cvPlotColors.size()){
            len=len;
        }
        float[] pfX,pfY;
        if(m_cCursor==null) {
            m_cCursor=new cursor(null,"cursor",Color.RED);
        }
        m_cCursor.draw(handler.isCurrent(this));
        for(i=0;i<len;i++){
            setColor(m_cvPlotColors.get(i));
            setLineWidth(nvPlotLineWidths.get(i));
            shape=nvPlotShapes.get(i);
            pfX=pfvPlotDataX.get(i);
            if(pfX.length<2) continue;
            pfY=pfvPlotDataY.get(i);

            if(shape==this.StepPlot){
                ArrayList<double[]> pdvXY=new ArrayList();
                pdvXY.add(pdX);
                pdvXY.add(pdY);
                converToStepPlotData(pdvXY);
                pdX=pdvXY.get(0);
                pdY=pdvXY.get(1);
                shape=LINE;
            }
            if(i==0){
                float pfXT[]=new float[2], pfYT[]=new float[2];
                float fdx=(float)(0.001*(m_dXF-m_dXI)),fdy=(float) (0.001*(m_dYF-m_dYI));
                if(fdx<0) 
                    fdx=-fdx;
                else if(fdx==0.)
                    fdx=0.0001f;
                
                if(fdy<0) 
                    fdy=-fdy;
                else if(fdy==0.)
                    fdy=0.0001f;
                
                pfXT[0]=(float)(m_dXI-fdx);
                pfYT[0]=(float)(m_dYI-fdy);
                pfXT[1]=(float)(m_dXI+fdx);
                pfYT[1]=(float)(m_dYI+fdy);
                m_dXF=Math.max(m_dXF, m_dXI+0.00001);
                m_dYF=Math.max(m_dYF, m_dYI+0.00001);
                
                updatePlot(svPlotTitles.get(0),xLabel,yLabel,pfXT,pfYT);
                
                getImagePlus().setWindow(this);
                if(m_dXF<=m_dXI||m_dYF+marginY<=m_dYI-marginY){
                    i=i;
                }
                if(autoYScale)
                    super.setLimits(m_dXI, m_dXF, m_dYI-marginY,m_dYF+marginY);
                else
                    super.setLimits(m_dXI, m_dXF, m_dYI,m_dYF);
                drawPlot();
                setColor(m_cvPlotColors.get(i));
                setLineWidth(nvPlotLineWidths.get(i));
                super.displayXAxisLabel(displayXAxisLabel);
            }
            addPoints(pfX, pfY, shape);
            drawPlot();
        }
        for(i=0;i<cvErrorbars.size();i++){
            cvErrorbars.get(i).draw();
        }
        for(i=0;i<cvBarGraphNodes.size();i++){
            cvBarGraphNodes.get(i).draw();
        }
        for(i=0;i<cvSummaryNodes.size();i++){
            cvSummaryNodes.get(i).draw();
        }
        drawGraphTitle();
        drawLegends();
        impl=getImagePlus();
    }

    public void setLimits(double xI, double xF, double yI, double yF){//redraw the plot
        if(autoYScale){
            DoubleRange yRange=getYRange(xI,xF);
            yI=yRange.getMin();
            yF=yRange.getMax();
        }
        if(xF>=xI&&yF>=yI){
            updateDisplayRanges(xI,xF,yI,yF);
            int w=plotFrame.width;        
            m_dXI=xI;
            m_dXF=xF;
            m_dYI=yI;
            m_dYF=yF;
            int i,len=pdvPlotDataX.size();
            ArrayList<float[]> pfv=new ArrayList();
            float pfX[];
            int status=1;
            for(i=0;i<len;i++){
                CommonStatisticsMethods.copyDoubleInRangeToFloatArray(pdvPlotDataX.get(i),pdvPlotDataY.get(i),m_dXI,m_dXF,pfv);
                if(pfv.size()==0) continue;
                if(pfv.get(0).length>2*w){
                    CommonStatisticsMethods.reduceDataSize_ExtremaBased(pfv.get(0), pfv.get(1), 3*w, pfv);
                }
                pfX=pfv.get(0);
                if(pfX.length<2) continue;//status=-1;
                pfvPlotDataX.set(i, pfv.get(0));
                pfvPlotDataY.set(i, pfv.get(1));
            }
            if(status!=-1){
                fireActionevent(new ActionEvent(this,0,"PlotRescaled"));
                calMagnification();
                if(bSynchScales) synchronizeScales(xI,xF,yI,yF);
                marginX=0.05*(xF-xI);
                marginY=0.05*(yF-yI);
            }
            refreshPlot();
        }
    }
    void synchronizeScales(double xI, double xF, double yI, double yF){
        int i,len=m_cvLinkedPlotWindows.size();
        PlotWindowPlus pw;
        for(i=0;i<len;i++){
            pw=m_cvLinkedPlotWindows.get(i);
            if(pw==this) continue;
            pw.synchScales(false);
            pw.synchCursor(false);
            pw.setLimit(xI, xF);
            pw.synchScales(true);
            pw.synchCursor(true);
        }
    }
    void synchronizeCursors(double x){
        int i,len=m_cvLinkedPlotWindows.size();
        PlotWindowPlus pw;
        for(i=0;i<len;i++){
            pw=m_cvLinkedPlotWindows.get(i);
            if(pw==this) continue;
            pw.synchCursor(false);
//            pw.toFront();
            pw.resetCursor(x);
            pw.synchCursor(true);
        }
    }
    void converToStepPlotData(ArrayList<double[]> pdvXY){
        double[] pdX0=pdvXY.get(0),pdY0=pdvXY.get(1);
        int len0=pdX0.length,len=3*len0+2,i;
        double[] pdX=new double[len],pdY=new double[len];
        double x0,x,y0,y,xm;
        int position=0;
        calDataRange();
        double delta;
        double yI=cDataRangeY.getMin();

        x0=pdX0[0];
        y0=pdY0[0];
        x=x0;
        y=y0;
        delta=0;
        for(i=1;i<len0;i++){
            x=pdX0[i];
            y=pdY0[i];
            delta=x-x0;
            xm=(x0+x)/2;
            if(i==1){
                pdX[position]=x0-delta/2;
                pdY[position]=yI;
                position++;
                pdX[position]=x0-delta/2;
                pdY[position]=y0;
                position++;
            }
            pdX[position]=xm;
            pdY[position]=y0;
            position++;
            pdX[position]=xm;
            pdY[position]=y;
            position++;
            x0=x;
            y0=y;
        }
        pdX[position]=x+delta/2;
        pdY[position]=y;
        position++;
        pdX[position]=x+delta/2;
        pdY[position]=yI;
        pdvXY.clear();
        pdvXY.add(pdX);
        pdvXY.add(pdY);
    }
    void calDataRange(){
        cDataRangeX.resetRange();
        cDataRangeY.resetRange();
        ArrayList<Integer> cvCursorCurveIndexes=m_cCursor.getCursorCurveIndexes();
        int i,num=pdvPlotDataX.size();
        for(i=0;i<num;i++){
            if(CommonMethods.containsContent(cvCursorCurveIndexes, i)) continue;
            cDataRangeX.expandRange(CommonStatisticsMethods.getRange(pdvPlotDataX.get(i)));
            cDataRangeY.expandRange(CommonStatisticsMethods.getRange(pdvPlotDataY.get(i)));
        }
        num=cvErrorbars.size();
        YErrorbars e;
        double[] pdY,pdE;
        int j,len;
        for(i=0;i<num;i++){
            e=cvErrorbars.get(i);
            cDataRangeX.expandRange(e.getXRange());
            cDataRangeY.expandRange(e.getYRange());
        }
        
        SummaryNode sNode;
        for(i=0;i<cvSummaryNodes.size();i++){
            sNode=cvSummaryNodes.get(i);
            cDataRangeX.expandRange(sNode.getXRange());
            cDataRangeY.expandRange(sNode.getYRange());
        }
        
        BarGraphNode bNode;
        for(i=0;i<cvBarGraphNodes.size();i++){
            bNode=cvBarGraphNodes.get(i);
            cDataRangeX.expandRange(bNode.getXRange());
            cDataRangeY.expandRange(bNode.getYRange());
        }
    }
    void calDisplayRange(){
        cDisplayRangeX.resetRange();
        cDisplayRangeY.resetRange();
        int i,j,len,num=pdvPlotDataX.size();
        for(i=0;i<num;i++){
            cDisplayRangeX.expandRange(cvDisplayRangesX.get(i));
            cDisplayRangeY.expandRange(cvDisplayRangesY.get(i));
        }
        m_dXI=cDisplayRangeX.getMin();
        m_dXF=cDisplayRangeX.getMax();
        m_dYI=cDisplayRangeY.getMin();
        m_dYF=cDisplayRangeY.getMax();
        num=cvErrorbars.size();
        YErrorbars e;
        double[] pdY,pdE,pdX;
        for(i=0;i<num;i++){
            e=cvErrorbars.get(i);
//            cDisplayRangeX.expandRange(e.getXRange());
            cDisplayRangeY.expandRange(e.getYRange(cDisplayRangeX));
        }
        SummaryNode sNode;
        double x;
        for(i=0;i<cvSummaryNodes.size();i++){
            sNode=cvSummaryNodes.get(i);
            x=sNode.getX();
            if(!cDisplayRangeX.contains(x)) continue;
            cDisplayRangeY.expandRange(sNode.getYRange());
        }
        
        BarGraphNode bNode;
        for(i=0;i<cvBarGraphNodes.size();i++){
            bNode=cvBarGraphNodes.get(i);
            x=bNode.getX();
            if(!cDisplayRangeX.contains(x)) continue;
            cDisplayRangeY.expandRange(bNode.getYRange());
        }
    }
    public void getDataRanges(DoubleRange xRange,DoubleRange yRange){
        xRange.resetRange();
        yRange.resetRange();
        xRange.expandRange(cDataRangeX);
        yRange.expandRange(cDataRangeY);
    }
    public void zoomOut(){
        int factor=2;
        double xRange=cDataRangeX.getRange(),yRange=cDataRangeY.getRange();
        double len0X=m_dXF-m_dXI,lenX=factor*len0X;
        double sMX=(m_dXF+m_dXI)/2;
        double xI=sMX-lenX/2,xF=sMX+lenX/2;
        double len0Y=m_dYF-m_dYI,lenY=factor*len0Y;
        double sMY=(m_dYF+m_dYI)/2;
        double yI=sMY-lenY/2,yF=sMY+lenY/2;
        double xMin=cDataRangeX.getMin()-xRange*m_dMarginRatioX;
        double xMax=cDataRangeX.getMax()+xRange*m_dMarginRatioX;
        double yMin=cDataRangeY.getMin()-yRange*m_dMarginRatioY;
        double yMax=cDataRangeY.getMax()+xRange*m_dMarginRatioY;
        if(xI<xMin) xI=xMin;
        if(xF>xMax) xF=xMax;
        if(yI<yMin) yI=yMin;
        if(yF>yMax) yF=yMax;
        setLimits(xI,xF,yI,yF);
    }
    public void zoomIn_Default(){
        double factor=0.5;
        double len0=m_dXF-m_dXI,len=factor*len0;
        double sM=(m_dXF+m_dXI)/2;
        double sI=sM-len/2,sF=sM+len/2;
        setLimit(sI,sF);
    }
    public int zoomIn(){
        ImagePlus impl=getImagePlus();
        Roi roi=impl.getRoi();
        if(roi==null) {
            zoomIn_Default();
            return 1;
        }
        DoubleRange xRange=new DoubleRange(),yRange=new DoubleRange();
        CommonGuiMethods.getCoordinateRanges(this,roi,xRange,yRange);
        setLimits(xRange.getMin(),xRange.getMax(),yRange.getMin(),yRange.getMax());
        return 1;
    }
    public void clearCurves(){
        int num=pdvPlotDataX.size(),i;
        for(i=0;i<num;i++){
            removeCurve(num-1-i);
        }
    }
    public int removeCurve(int index){
        pdvPlotDataX.remove(index);
        pdvPlotDataY.remove(index);
        pfvPlotDataX.remove(index);
        pfvPlotDataY.remove(index);
        svPlotTitles.remove(index);
        nvPlotShapes.remove(index);
        nvPlotLineWidths.remove(index);
        m_cvPlotColors.remove(index);
        cvDisplayRangesX.remove(index);
        cvDisplayRangesY.remove(index);
        m_cCursor.adjustCurveIndex(index);
        if(index==m_nActiveCurveIndex) m_nActiveCurveIndex=-1;
        if(index<m_nActiveCurveIndex) m_nActiveCurveIndex--;
        displayCurveList();
        return 1;
    }
    public int removeActiveCurve(){
        int index=getActiveCurveIndex();
        if(index<0) return -1;
        removeCurve(index);
        Roi roi=null;
        selectActiveCurve(roi);
        return 1;
    }
    public float[] getXValues(){
        int it=0;
        if(m_nActiveCurveIndex>=0&&m_nActiveCurveIndex<pdvPlotDataX.size()) it=m_nActiveCurveIndex;
        return CommonStatisticsMethods.copyToFloatArray(pdvPlotDataX.get(it));
    }
    public float[] getYValues(){
        int it=0;
        if(m_nActiveCurveIndex>=0&&m_nActiveCurveIndex<pdvPlotDataX.size()) it=m_nActiveCurveIndex;
        return CommonStatisticsMethods.copyToFloatArray(pdvPlotDataY.get(it));
    }
    public double[] getYValues_DP(){
        return pdvPlotDataY.get(0);
    }
    public int getPlotShape(int index){
        return nvPlotShapes.get(index);
    }
    public Color getPlotColor(int index){
        return m_cvPlotColors.get(index);
    }
    public int getPlotLineWidth(int index){
        return nvPlotLineWidths.get(index);
    }
    public String getPlotTitile(int index){
        return svPlotTitles.get(index);
    }
    public int getNumCurves(){
        return pdvPlotDataX.size();
    }
    public void replaceYValues(double[] pdY){
        pdvPlotDataY.set(0, pdY);
    }
    public void setSmoothingWS(int ws){
        m_nSmoothingWS=ws;
    }
    public int getSmoothingWS(){
        return m_nSmoothingWS;
    }
    public int getROIRange(DoubleRange xRange, DoubleRange yRange){
        ImagePlus impl=getImagePlus();
        Roi roi=impl.getRoi();
        int status;
        if(roi!=null){
            status=CommonGuiMethods.getCoordinateRanges(this,roi,xRange,yRange);
            if(status==-1) return -1;
        }else{
            return -1;
        }
        return 1;
    }
    public int getSelectedRanges(DoubleRange xRange,DoubleRange yRange){
        ImagePlus impl=this.getImagePlus();
        Roi roi=impl.getRoi();
        if(roi==null){
            xRange.setRange(m_dXI, m_dXF);
            yRange.setRange(m_dYI, m_dYF);
            return 1;
        }
        int status=CommonGuiMethods.getCoordinateRanges(this,roi,xRange,yRange);
        return 1;
    }
    public void getDisplayingRange(DoubleRange xRange, DoubleRange yRange){
        xRange.setRange(m_dXI, m_dXF);
        yRange.setRange(m_dYI, m_dYF);
    }
    public int getNumDataPoints(){
        int num=getNumCurves(),i,lMax,len;
        lMax=0;
        for(i=0;i<num;i++){
            len=pdvPlotDataX.get(i).length;
            if(len>lMax) lMax=len;
        }
        return lMax;
    }
    public String[][] getPlotDataAsStringArray(){
        int num=getNumCurves(),lMax=getNumDataPoints(),i,j;
        String psData[][]=new String[lMax+1][2*num+1],st,sX,sY;
        psData[0][0]="row";
        int position=1;
        for(i=0;i<num;i++){
            sX=svPlotTitles.get(i)+"X";
            sY=svPlotTitles.get(i)+"Y";
            psData[0][position]=sX;
            position++;
            psData[0][position]=sY;
            position++;
        }
        int line,precision=5;
        double[] pdX,pdY;
        for(i=0;i<lMax;i++){
            line=i+1;
            psData[line][0]=PrintAssist.ToString(i);
            position=1;
            for(j=0;j<num;j++){
                pdX=pdvPlotDataX.get(j);
                if(i<pdX.length){
                    sX=PrintAssist.ToStringScientific(pdX[i], precision);
                    sY=PrintAssist.ToStringScientific(pdvPlotDataY.get(j)[i],precision);
                }else
                {
                    sX="";
                    sY="";
                }
                psData[line][position]=sX;
                position++;
                psData[line][position]=sY;
                position++;
            }
        }
        return psData;
    }
    public void showPlotData(){
        String[][] psData=getPlotDataAsStringArray();
        JViewport jvp=CommonGuiMethods.buildCommonTableView(psData[0], psData, 0, psData.length-1);
        GeneralSchrollPaneDisplayingFrame.displayNewFrame(jvp);
    }
    public void removePlotGroup(String sGroupName){
        int i,len=pdvPlotDataX.size();
        for(i=len-1;i>=0;i--){
            if(svPlotTitles.get(i).startsWith(sGroupName)) removeCurve(i);
        }
        if(pdvPlotDataX.size()!=m_cvPlotColors.size()){
            len=len;
        }
        refreshPlot();
    }
    public void drawPlotGroup(String sGroupName,double[] pdX, double[] pdY, ArrayList<intRange> ira,int lw, int shape, Color c){
        if(c==null) c=getNewPlotColor();
        int i,len=ira.size();
        for(i=0;i<len;i++){
            addPlot(sGroupName+i,CommonStatisticsMethods.getDoubleArray(pdX, ira.get(i)),CommonStatisticsMethods.getDoubleArray(pdY, ira.get(i)),lw,shape,c,false);
        }
        refreshPlot();
    }
    
    public void getDisplayRange(DoubleRange xRange, DoubleRange yRange){
        xRange.setRange(m_dXI, m_dXF);
        yRange.setRange(m_dYI, m_dYF);
    }
    public void displayRegression(String title, SimpleRegression sr, DoubleRange dRange, int lw, int shape, Color c){
        double xI=dRange.getMin(),xF=dRange.getMax(),delta=dRange.getRange()/(PlotWidth-1),x;
        double[] pdX=new double[PlotWidth],pdY=new double[PlotWidth];
        if(c==null) c=getNewPlotColor();
        for(int i=0;i<PlotWidth;i++){
            x=xI+i*delta;
            pdX[i]=x;
            pdY[i]=sr.predict(x);
        }
        addPlot(title,pdX,pdY,lw,shape,c,true);
    }
    double getPlotDensity(){
        return (m_dXF-m_dXI)/PlotWidth;
    }
    public void mouseEntered(MouseEvent e){
        handler.setAsCurrentPlotWindow(this);
        addToPlotList(this);
    }
    public static void addToPlotList(PlotWindowPlus pw){
        if(PlotList.isEmpty())
            PlotList.add(pw);
        else if(PlotList.getLast() != pw) PlotList.add(pw);
        if(PlotList.size()>MaxPlotListSize) PlotList.poll();
    }
    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){mousePoint=null;}
    public void mouseClicked(MouseEvent e){
        WindowManager.setTempCurrentImage(getImagePlus());
        if(IJ.shiftKeyDown()){
            zoomOut();
        }else if(IJ.spaceBarDown()){
            ImagePlus impl=getImagePlus();
            Roi roi=impl.getRoi();
            Point pt=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
            if(roi==null){
                recordCursor();
                synchRecordingCursor();
                handler.toFront();
            } else {
                if(roi instanceof TextRoi){
                    cvTextRois.add((TextRoi)roi);
                }else{
                    if(!roi.contains(pt.x, pt.y)){
                        recordCursor();
                        synchRecordingCursor();
                        handler.toFront();
                    }
                }
            }                
        }else{
            setLimits(m_dXI,m_dXF,m_dYI,m_dYF);
            if(e.getClickCount()==2){
                
            }
        }
        if(handler.pickingXRange()){//set the roi that cover the chosen range of x
            pickXRange();
        }
    }
    public double getXScale(){
        double[] scales=new double[2];
        getScales(scales);
        return scales[0];
    }
    public double getYScale(){
        double[] scales=new double[2];
        getScales(scales);
        return scales[1];
    }
    public double getYLength(int len){
        return len/getYScale();
    }
    public double getXLength(int len){
        return len/getXScale();
    }
    public int getYImageLength(double len){
        return (int) (len*getYScale()+0.5);
    }
    public int getXImageLength(int len){
        return (int) (len*getXScale()+0.5);
    }
    public void mousePressed(MouseEvent e){
        MouseStart=e.getLocationOnScreen();
    }
    public int pickXRange(){
            DoublePair dp=handler.getRegionPickingRange();
            handler.unselectPickRegionRB();            
            
//            double l=dXCursor-dp.left,r=dXCursor+dp.right,t=m_dYI,b=m_dYF;
            double l=dp.left,r=dp.right,t=m_dYI-marginY,b=m_dYF+marginY;
            Point lt=getPixelCoordinates(l,t),rb=getPixelCoordinates(r,b);
            int[] xPoints={lt.x,rb.x,rb.x,lt.x},yPoints={lt.y,lt.y,rb.y,rb.y};

            Roi roi=new PolygonRoi(xPoints,yPoints,4,Roi.POLYGON);
            getImagePlus().setRoi(roi);
        return 1;
    }
    public void mouseReleased(MouseEvent e){
        Point pt=e.getLocationOnScreen();
        if (Toolbar.getToolId()==Toolbar.HAND || IJ.spaceBarDown()){
            double dx=(MouseStart.x-pt.x)/magX, dy=(MouseStart.y-pt.y)/magY;
            setLimits(m_dXI+dx,m_dXF+dx,m_dYI-dy,m_dYF-dy);
        }else if(IJ.shiftKeyDown()){
            ImagePlus impl=getImagePlus();
            if(impl.getRoi()!=null)zoomIn();
        }
        MouseStart.setLocation(pt);
    }
    public void mouseDragged(MouseEvent e){
/*        Point pt=e.getLocationOnScreen();
        double dx=(MouseStart.x-pt.x)*magX, dy=(MouseStart.y-pt.y)*magY;
        setLimits(m_dXI+dx,m_dXF+dx,m_dYI+dy,m_dYF+dy);
        MouseStart.setLocation(pt);*/
    }
    public void mouseMoved(MouseEvent e){
        mousePoint=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
        if(!bFreezeCursor) 
            updateCursor(e);
    }
    int updateMouseCoors(){
        if(mousePoint==null) return -1;
        getCoordinate(mousePoint.x, mousePoint.y, pdMouseCoors);
        return 1;
    }
    void displayCoordinates(int x, int y){
        double[] coors=new double[2];
        int status=getCoordinate(x, y, coors);
        if(status==1){
            int index=getActiveCurveIndex();
            if(index<0) index=0;            
            m_sCoors="X= "+getXString(coors[0])+"  Y-Cursor= "+getYString(coors[1])+"  Y-Curve="+getYString(getCurveValue(index,coors[0]));
        }
        if(m_sCoors!=null) 
            coordinates.setText(m_sCoors);
        
    }
    public int updateCursor(MouseEvent e){
        mousePoint=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
        double[] pdXY=new double[2];
        if(mousePoint==null) return -1;
        CommonGuiMethods.getCoordinate(this, mousePoint.x, mousePoint.y, pdXY);
        double x=pdXY[0];
        if(Double.isNaN(x)) return -1;
        if(x>=m_dXI&&x<=m_dXF&&!e.isAltDown()){
            resetCursor(x);
        }
        return 1;
    }
    public void resetCursor(double x){
        dXCursor=x;
        refreshPlot();
        if(bSynchCursor) synchronizeCursors(x);
    }
    public void resetView(){
        setLimits(cDataRangeX.getMin(),cDataRangeX.getMax(),cDataRangeY.getMin(),cDataRangeY.getMax());
    }
    public void freezeCursor(boolean freeze){
        bFreezeCursor=freeze;
    }
    public void selectActiveCurve(MouseEvent me){
        Point p=CommonGuiMethods.getCursorLocation_ImageCoordinates(getImagePlus());
        PointRoi Roi=new PointRoi(p.x,p.y);
        
        selectActiveCurve(Roi);
        hightlightActiveCurve_Force();
    }
    public void selectActiveCurve(Roi roi){
        if(roi==null){
            m_nActiveCurveIndex=-1;
            hightlightActiveCurve();
        }else{
            DoubleRange xRange=new DoubleRange(), yRange=new DoubleRange();
            CommonGuiMethods.getCoordinateRanges(this, roi, xRange, yRange);
            double x=xRange.getMidpoint(),dn=yRange.getRange(),diff,y=yRange.getMidpoint();
            if(roi instanceof PointRoi){
                yRange.setRange(m_dYI, m_dYF);
                dn=m_dYF-m_dYI;
            }
            ArrayList <Integer> nvIndexes=getCurveIndexesInRange(x,yRange);
            int i,len=nvIndexes.size(),index=-1;
            if(len>0) m_nActiveCurveIndex=nvIndexes.get(0);
            for(i=0;i<len;i++){
                diff=Math.abs(y-getCurveValue(nvIndexes.get(i),x));
                if(diff<dn){
                    dn=diff;
                    index=nvIndexes.get(i);
                }
            }
            selectActiveCurve(index);
        }
    }
    public void selectActiveCurve(int index){
        m_nActiveCurveIndex=index;
        if(handler.getCurrentPlotWindow()!=this)
            handler.setAsCurrentPlotWindow(this);
        if(index>=0) handler.updateCurveListCBSelection(svPlotTitles.get(m_nActiveCurveIndex));
        hightlightActiveCurve();
    }
    public int selectActiveCurve(String name){
        int index=getCurveIndex(name);
        selectActiveCurve(index);
        return 1;
    }
    public int getCurveIndex(String name){
        int i,len=svPlotTitles.size();
        for(i=0;i<len;i++){
            if(svPlotTitles.get(i).contentEquals(name)) return i;
        }
        return -1;
    }
    public void setCurveColor(int index, Color c){
        if(index>=0&&index<m_cvPlotColors.size())m_cvPlotColors.set(index, c);
    }
    public void setLineWidth(int index, int lw){
        if(index>=0&&index<nvPlotLineWidths.size())nvPlotLineWidths.set(index, lw);
    }
    public ArrayList<Integer> getCurveIndexesInRange(double x, DoubleRange yRange){
        int i,len=pdvPlotDataY.size();
        ArrayList<Integer> indexes=new ArrayList();
        for(i=0;i<len;i++){
            if(!cvDisplayRangesX.get(i).contains(x)) continue;
            if(yRange.contains(getCurveValue(i,x))) indexes.add(i);
        }
        return indexes;
    }
    public double getCurveValue(int index, double x){
        if(!cvDisplayRangesX.get(index).contains(x)) return Double.NaN;
        double[] pdX=pdvPlotDataX.get(index),pdY=pdvPlotDataY.get(index);
        ArrayList<Integer> indexes=CommonStatisticsMethods.getCrossingPositions(pdX, x);
        if(indexes.isEmpty()){
            if(x==pdX[0]) return pdY[0];
            if(x==pdX[pdX.length-1]) return pdY[pdY.length-1];
            return Double.NaN;
        }
        int it=indexes.get(0);
        if(it<0||it>=pdX.length) return Double.NaN;
        if(it==pdX.length-1) return pdY[it];
        if(pdX[it+1]-x<x-pdX[it]) it++;
        return pdY[it];
//        return CommonMethods.getLinearIntoplation(pdX[it], pdY[it], pdX[it+1], pdY[it+1], x);
    }
    int getXIndex_Roundoff(int curveIndex, double x){
        if(curveIndex>=cvDisplayRangesX.size()) return -1;
        if(!cvDisplayRangesX.get(curveIndex).contains(x)) return -1;
        double[] pdX=pdvPlotDataX.get(curveIndex),pdY=pdvPlotDataY.get(curveIndex);
        ArrayList<Integer> indexes=CommonStatisticsMethods.getCrossingPositions(pdX, x);
        if(indexes.isEmpty()){
            if(x==pdX[0]) return 0;
            if(x==pdX[pdX.length-1]) return pdY.length-1;
            return -1;
        }
        int index=indexes.get(0);
        if(Math.abs(pdX[index]-x)>Math.abs(pdX[index+1]-x)) index++;
        return index;
    }
    public double getActiveCurveValue(double x){
        int it=m_nActiveCurveIndex;
        if(it<0||it>=pdvPlotDataX.size()) it=0;
        return getCurveValue(it,x);
    }
    public int getActiveCurveIndex(){
        return m_nActiveCurveIndex;
    }
    public void hightlightActiveCurve(){
        this.removePlotGroup("ActiveCurve");
        if(m_nActiveCurveIndex>=0&&m_nActiveCurveIndex<pdvPlotDataX.size()){
            if(handler.HighlightCurve())
                this.addPlot("ActiveCurve", pdvPlotDataX.get(m_nActiveCurveIndex), pdvPlotDataY.get(m_nActiveCurveIndex), nvPlotLineWidths.get(m_nActiveCurveIndex)+1, nvPlotShapes.get(m_nActiveCurveIndex), Color.ORANGE, true);
        }
    }
    public void hightlightActiveCurve_Force(){
        this.removePlotGroup("ActiveCurve");
        if(m_nActiveCurveIndex>=0&&m_nActiveCurveIndex<pdvPlotDataX.size()){
                this.addPlot("ActiveCurve", pdvPlotDataX.get(m_nActiveCurveIndex), pdvPlotDataY.get(m_nActiveCurveIndex), nvPlotLineWidths.get(m_nActiveCurveIndex)+1, nvPlotShapes.get(m_nActiveCurveIndex), Color.ORANGE, true);
        }
    }
    public double[] getXValues(int index){
        return pdvPlotDataX.get(index);
    }
    public double[] getYValues(int index){
        return pdvPlotDataY.get(index);
    }
    public boolean isActiveCurveSelected(){
        return m_nActiveCurveIndex>=0&&m_nActiveCurveIndex<pdvPlotDataX.size();
    }
    public void windowClosed(WindowEvent we){
        super.windowClosed(we);
        Window w=we.getWindow();
        if(w instanceof PlotWindowPlus) {
            PlotWindowPlus pw=(PlotWindowPlus) w;
            removeFromPlotList(pw);
            handler.removePlotWindow(pw);
        }
    }
    public static void removeFromPlotList(PlotWindowPlus pw){
        boolean removed=PlotList.removeFirstOccurrence(pw);
        while(removed){
            removed=PlotList.removeFirstOccurrence(pw);
        }
    }
    public void markPoints(String title, double[] pdX, double[] pdY, ArrayList<Integer> points, int lw, int shape, Color c){
        ArrayList<Double> dvX=new ArrayList(), dvY=new ArrayList();
        int i, len=points.size();
        for(i=0;i<len;i++){
            dvX.add(pdX[points.get(i)]);
            dvY.add(pdY[points.get(i)]);
        }
        addPlot(title, dvX, dvY, lw, shape, c, true);
    }
    public void handleActiveCurve(String option){
        if(option.contentEquals("remove")){
            removeActiveCurve();
        }
    }
    public void componentResized(ComponentEvent e){
        fitPlotSize();
    }
    public void componentHidden(ComponentEvent e){
        
    }
    public void componentShown(ComponentEvent e){
        
    }
    public void componentMoved(ComponentEvent e){
        
    }
    int fitPlotSize(){
        Window win=getImagePlus().getWindow();
        if(win==null) return -1;
        if(!win.isVisible()) return -1;
        int h=win.getHeight(),w=win.getWidth();
        Plot plot=getPlot();
        if(w<=0||h<=0){
            w=w;
            return -1;
        }
        if(plot!=null) getPlot().ReSize(w, h);
        return 1;
    }
    public int selectActiveCurve(){
        Roi roi=getImagePlus().getRoi();
        if(roi==null) return -1;
        selectActiveCurve(roi);
        return 1;
    }
    void calPlotAccuracy(){
        xAccuracy=(m_dXF-m_dXI)/plotFrame.width;
        yAccuracy=(m_dYF-m_dYI)/plotFrame.height;
    }
    public void addActionListener(ActionListener al){
        m_cvActionListeners.add(al);
    }
    void fireActionevent(ActionEvent ae){
        int i,len=m_cvActionListeners.size();
        for(i=0;i<len;i++){
            m_cvActionListeners.get(i).actionPerformed(ae);
        }
    }
    public String getXString(double x){
        int digits=PrintAssist.getDigits((m_dXF-m_dXI)/plotFrame.width),precision;
        if(digits>=0) 
            precision=0;
        else
            precision=-digits+1;
        return PrintAssist.ToString(x, precision);
    }
    public String getYString(double y){
        int digits=PrintAssist.getDigits((m_dYF-m_dYI)/plotFrame.height),precision;
        if(digits>=0) 
            precision=0;
        else
            precision=-digits+1;
        return PrintAssist.ToString(y, precision);
    }
    public void autoYScale(boolean auto){
        autoYScale=auto;
    }
    int displayCursorCoordinates(){
        if(dXCursor<m_dXI||dXCursor>m_dXF) return -1;
        m_sCoors="X= "+getXString(dXCursor);
        if(mousePoint!=null){
            updateMouseCoors();
            m_sCoors+="  Y-Cursor= "+getYString(pdMouseCoors[1]);
        }
        m_sCoors+="  Y-Curve="+getYString(getActiveCurveValue(dXCursor));
        if(m_sCoors!=null) coordinates.setText(m_sCoors);
        return 1;
    }
    public void recordCursor(){
        m_cCursor.record();
    }
    void synchRecordingCursor(){
        int i,len=m_cvLinkedPlotWindows.size();
        PlotWindowPlus pw;
        for(i=0;i<len;i++){
            pw=m_cvLinkedPlotWindows.get(i);
            if(pw==this) continue;
            pw.recordCursor();
        }
    }
    public void setFeatureExtracter(LineFeatureExtracter2 cFE){
        cFeatureExtracter=cFE;
    }
    public LineFeatureExtracter2 getFeatureExtracter(){
        return cFeatureExtracter;
    }
    public void displayCurveList(){
        handler.displayCurveList(svPlotTitles, m_nActiveCurveIndex);
    }
    public ArrayList<double[]> getActiveCurve(){
        int index=getActiveCurveIndex();
        if(index<0) index=0;
        ArrayList<double[]> line=new ArrayList();
        line.add(getXValues(index));
        line.add(getYValues(index));
        return line;
    }
    public double getCursorX(){
        return dXCursor;
    }
    public void setProtection(boolean protect){
        bProtected=protect;
    }
    public boolean isProtected(){
        return bProtected;
    }
    public void addYErrorbars(double[] pdX, double[] pdY, double[] pdYErrorbars,Color c, int barL, int lw){
        cvErrorbars.add(new YErrorbars(pdX,pdY,pdYErrorbars,c,barL,lw));    
        calDataRange();
        calDisplayRange();
        resetView();
    }
    public void addSummaryNode(double x, double mean, double sd, int n, Color c, ArrayList<Double> dvPs, ArrayList<Color> colors, int barL, int lw){
        cvSummaryNodes.add(new SummaryNode(x,mean,sd,n,c,dvPs,colors,barL,lw));
        calDataRange();
        calDisplayRange();
        resetView();
    }
    public void addBarGraphNode(double x, double y, double yError, Color c, int lw, int barL, int width, boolean Upward){
        cvBarGraphNodes.add(new BarGraphNode(x,y,yError, c, lw, barL, width, Upward));
        calDataRange();
        calDisplayRange();
        resetView();
    }
    public String getActiveCurveTitle(){
        return svPlotTitles.get(m_nActiveCurveIndex);
    }
    //addPlot(String title, double[] pdX, double[] pdY, int lw, int shape,Color c,boolean bDraw)
    public int appendPlotWindow(PlotWindowPlus pw){
        ArrayList<Integer> cvCursorCurveIndexes=pw.m_cCursor.getCursorCurveIndexes();
        int i,num=pw.pdvPlotDataX.size();
        String plotTitle;
        for(i=0;i<num;i++){
            if(CommonMethods.containsContent(cvCursorCurveIndexes, i)) continue;
            plotTitle=pw.svPlotTitles.get(i);
            if(existingPlotTitle(plotTitle)) plotTitle=getUniquePlotTitle(plotTitle);
            addPlot(plotTitle,pw.pdvPlotDataX.get(i),pw.pdvPlotDataY.get(i),pw.nvPlotLineWidths.get(i),pw.nvPlotShapes.get(i),pw. m_cvPlotColors.get(i),false);
        }
        num=pw.cvErrorbars.size();
        YErrorbars e;
        double[] pdX,pdY,pdE;
        int j,len;
        for(i=0;i<num;i++){
            e=pw.cvErrorbars.get(i);
            pdX=e.pdX;
            pdY=e.pdY;
            pdE=e.pdErrors;
            addYErrorbars(pdX,pdY,pdE,e.c,e.barL,e.lw);
        }
        
        SummaryNode sNode;
        for(i=0;i<cvSummaryNodes.size();i++){
            sNode=cvSummaryNodes.get(i);
            addSummaryNode(sNode.getX(),sNode.mean,sNode.sd,sNode.n,sNode.c, sNode.dvPValues,sNode.cvColors,sNode.lw,sNode.barL);
        }
        
        BarGraphNode bNode;
        for(i=0;i<cvBarGraphNodes.size();i++){
            bNode=cvBarGraphNodes.get(i);
            addBarGraphNode(bNode.x,bNode.y,bNode.yError,bNode.c,bNode.lw,bNode.barL,bNode.width,bNode.Upward);
        }
        
        pw.calDataRange();
        pw.calDisplayRange();
        pw.refreshPlot();
        return 1;
    }
    public void setXLabel(String label){
        xLabel=label;
    }
    public void setYLabel(String label){
        yLabel=label;
    }
    public String getXLabel(){
        return xLabel;
    }
    public String getYLabel(){
        return yLabel;
    }
    public PlotWindowPlus copy(){
        PlotWindowPlus pw=null;
        ArrayList<Integer> cvCursorCurveIndexes=m_cCursor.getCursorCurveIndexes();
        int i,num=pdvPlotDataX.size();
        boolean first=true;
        String plotTitle;
        
        for(i=0;i<num;i++){
            if(CommonMethods.containsContent(cvCursorCurveIndexes, i)) continue;
            plotTitle=svPlotTitles.get(i);
            if(first) {
                pw=new PlotWindowPlus(pdvPlotDataX.get(i),pdvPlotDataY.get(i),m_sPlotWindowTitle,plotTitle,xLabel,yLabel,nvPlotLineWidths.get(i),nvPlotShapes.get(i),m_cvPlotColors.get(i));
                first=false;
                continue;
            }
            if(existingPlotTitle(plotTitle)) plotTitle=getUniquePlotTitle(plotTitle);
            pw.addPlot(plotTitle,pdvPlotDataX.get(i),pdvPlotDataY.get(i),nvPlotLineWidths.get(i),nvPlotShapes.get(i),m_cvPlotColors.get(i),false);
        }
        num=cvErrorbars.size();
        YErrorbars e;
        double[] pdX,pdY,pdE;
        int j,len;
        for(i=0;i<num;i++){
            e=cvErrorbars.get(i);
            pdX=e.pdX;
            pdY=e.pdY;
            pdE=e.pdErrors;
            pw.addYErrorbars(pdX,pdY,pdE,e.c,e.barL,e.lw);
        }
        
        SummaryNode sNode;
        for(i=0;i<cvSummaryNodes.size();i++){
            sNode=cvSummaryNodes.get(i);
            addSummaryNode(sNode.getX(),sNode.mean,sNode.sd,sNode.n,sNode.c,sNode.dvPValues,sNode.cvColors,sNode.lw,sNode.barL);
        }
        
        BarGraphNode bNode;
        for(i=0;i<cvBarGraphNodes.size();i++){
            bNode=cvBarGraphNodes.get(i);
            addBarGraphNode(bNode.x,bNode.y,bNode.yError,bNode.c,bNode.lw,bNode.barL,bNode.width,bNode.Upward);
        }
        
        pw.calDataRange();
        pw.calDisplayRange();
        pw.refreshPlot();
        return pw;
    }
    public String getUniquePlotTitle(String plotTitle){
        while(existingPlotTitle(plotTitle)){
            plotTitle+=""+1;
        }
        return plotTitle;
    }
    public boolean existingPlotTitle(String title){
        for(int i=0;i<svPlotTitles.size();i++){
            if(svPlotTitles.get(i).contentEquals(title)) return true;
        }
        return false;
    }
    public String getPlotType(){
        return m_sPlotType;
    }
    public void setPlotType(String type){
        m_sPlotType=type;
    }
    public void setUniformColor(Color c){
        int i,len=m_cvPlotColors.size();
        for(i=0; i<len;i++){
            m_cvPlotColors.set(i, c);
        }
        len=cvErrorbars.size();
        for(i=0;i<len;i++){
            cvErrorbars.get(i).setColor(c);
        }
    }
    public void setLegends(ArrayList<String> legends, ArrayList<Color> colors){
        svLegends=legends;
        cvLegendColors=colors;
        
        ImagePlus impl=getImagePlus();
        ImageProcessor ip=impl.getProcessor();
        
        Font font=ip.getFontMetrics().getFont();
        ip.setFont(FontLegend);
        
        int len=legends.size(),i,j,p,h,w,h0=0,w0=0,lw=5,divider=20,indent=20;
        FontMetrics fm=ip.getFontMetrics();
        h0=fm.getHeight()+lw;
        String legend;
        for(i=0;i<len;i++){
            legend=legends.get(i);
            w0=Math.max(w0, ip.getStringWidth(legend));
        }
        w0+=2*lw;
        h=impl.getHeight();
        w=impl.getWidth();
        int cols=(w-indent)/w0,rows=len/cols;
        if(cols*rows<len) rows++;
        
        h=h+divider+rows*h0+2*lw;
        h0=impl.getHeight()+divider;
        
        setExtraBottomMargin(h-h0);
        
        p=0;
        if(cvLegendPositions==null) cvLegendPositions=new ArrayList();
        
        cvLegendPositions.clear();
        for(i=0;i<rows;i++){
            for(j=0;j<cols;j++){
                if(p>=len) break;
                cvLegendPositions.add(new Point(indent+j*w0,(i)*(fm.getHeight()+lw)+h0));
                p++;
            }
        }
        ip.setFont(font);
   }
   public int drawLegends(){
       if(svLegends==null) return -1;
       if(svLegends.isEmpty()) return -1;
       ImageProcessor im=getImagePlus().getProcessor();
       Font f=im.getFontMetrics().getFont();
       im.setFont(FontLegend);
       Point p;
       Color c,c0=im.getDrawingColor();
       for(int i=0;i<svLegends.size();i++){
           p=cvLegendPositions.get(i);
           c=cvLegendColors.get(i);
           im.setColor(c);
           im.drawString(svLegends.get(i), p.x, p.y);
       }
       im.setFont(f);
       im.setColor(c0);
       return 1;
   } 
   public ImagePlus getLegendedImagePlus(ArrayList<String> legends, ArrayList<Color> colors){
        ImagePlus impl=getImagePlus();
        ImageProcessor ip=impl.getProcessor();
        Font font=new Font("default",Font.BOLD,12);
        ip.setFont(font);
        int len=legends.size(),i,j,p,h,w,h0=0,w0=0,lw=5,divider=20,indent=20;
        FontMetrics fm=ip.getFontMetrics();
        h0=fm.getHeight()+lw;
        String legend;
        for(i=0;i<len;i++){
            legend=legends.get(i);
            w0=Math.max(w0, ip.getStringWidth(legend));
        }
        w0+=2*lw;
        h=impl.getHeight();
        w=impl.getWidth();
        int cols=(w-indent)/w0,rows=len/cols;
        if(cols*rows<len) rows++;
        
        h=h+divider+rows*h0+2*lw;
        h0=impl.getHeight()+divider;
        
        ImagePlus implc=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w, h);
        implc.setTitle(impl.getTitle());
        int pixels[][]=new int[h][w],pixels0[][]=CommonMethods.getPixelValues(impl);
        CommonStatisticsMethods.setElements(pixels, Color.WHITE.getRGB());
        for(i=0;i<impl.getHeight();i++){
            for(j=0;j<w;j++){
                pixels[i][j]=pixels0[i][j];
            }
        }
        
        CommonMethods.setPixels(implc, pixels, true);
        ip=implc.getProcessor();
        ip.setFont(font);
        p=0;
        for(i=0;i<rows;i++){
            for(j=0;j<cols;j++){
                if(p>=len) break;
                ip.setColor(colors.get(p));
                ip.drawString(legends.get(p),indent+j*w0,(i+1)*(fm.getHeight()+lw)+h0);
                p++;
            }
        }
        implc.show();
        return implc;
    }
    public void setGraphTitle(String title){
        sGraphTitle=title;
        calGraphTitlePosition();
        refreshPlot();
    }
    public void setXAxisTitle(String title){
        sXAxisTitle=title;
        setXLabel(title);
        refreshPlot();
    }
    public void setYAxisTitle(String title){
        sYAxisTitle=title;
        setYLabel(title);
        refreshPlot();
    }
    public void displayXAxisLabel(boolean show){
        displayXAxisLabel=show;
    }
    public int drawGraphTitle(){
        if(sGraphTitle==null) return -1;
        if(GraphTitlePosition==null) calGraphTitlePosition();
        ImagePlus impl=getImagePlus();
        ImageProcessor ip=impl.getProcessor();
        Font f=impl.getProcessor().getFontMetrics().getFont();
        Color c=ip.getDrawingColor();
        ip.setColor(cGraphTitle);
        impl.getProcessor().setFont(FontGraphTitle);
        getImagePlus().getProcessor().drawString(sGraphTitle, GraphTitlePosition.x, GraphTitlePosition.y);
        impl.getProcessor().setFont(f);
        impl.show();
        impl.draw();
        ip.setColor(c);
        return 1;
    }
    public int calGraphTitlePosition(){
        if(sGraphTitle==null) return -1;
        ImagePlus impl=getImagePlus();
        ImageProcessor ip=impl.getProcessor();
        Font ft=ip.getFontMetrics().getFont();
        ip.setFont(FontGraphTitle);
        FontMetrics fm=ip.getFontMetrics();
        int lw=6,h0=fm.getHeight()+2*lw,width=impl.getWidth(), tlength=ip.getStringWidth(sGraphTitle);

        ip.setFont(ft);
        setExtraTopMargin(h0);
        if(tlength>width)
            GraphTitlePosition=new Point(0,h0-lw+4);
        else
            GraphTitlePosition=new Point((width-tlength)/2,h0-lw+4);
/*    int ExtraTMargin0=0,ExtraBMargin0=0;
    int ExtraTMargin=0,ExtraBMargin=0;*/
        
        return 1;
    }
    public void OptimizeBinWidth(){
        int i,len=cvBarGraphNodes.size();
        BarGraphNode bnode0=cvBarGraphNodes.get(0),bnode;
        DoubleRange dr=new DoubleRange();
        double x0=bnode0.x;
        for(i=1;i<len;i++){
            bnode=cvBarGraphNodes.get(i);
            dr.expandRange(bnode.x-bnode0.x);
            bnode0=bnode;
        }
        double dm=dr.getMin();   
        if(!dr.isValid()) dm=(m_dXF-m_dXI)/2;
        Point p0=getPixelCoordinates(x0,m_dYI),p=getPixelCoordinates(x0+dm,m_dYI);
        int width=((p.x-p0.x)*2)/3;
        if(width==0) width=1;
        int lbar=Math.max((width*2)/3,1);
        for(i=0;i<len;i++){
            bnode=cvBarGraphNodes.get(i);
            bnode.width=width;
            bnode.barL=lbar;
        }
        refreshPlot();
    }
}
