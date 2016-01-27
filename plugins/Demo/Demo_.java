/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Demo;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import SQL.CommonSQLMethods;
import java.sql.Connection;
import ij.plugin.PlugIn;
import utilities.*;
import BrainSimulations.BrainSimulationGraphicalObjectsHandler;
import ij.ImagePlus;
import BrainSimulations.BrainSimulationGraphicalObject;
import java.util.ArrayList;
import java.awt.Color;
import utilities.ArrayofArrays.IntArray;
import utilities.BinormialDistribution;
import ImageAnalysis.LineEdgeFinder;
import ij.WindowManager;
import utilities.TTestTable;
import ImageAnalysis.EdgeElementHandler_FRR;
import ImageAnalysis.GraphicalObject;
import ImageAnalysis.GraphicalObjectsHandler;
import FluoObjects.*;
import ImageAnalysis.LandscapeAnalyzer;
import utilities.io.PrintAssist;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Geometry.ImageShapes.RectangleImage;
import java.awt.Point;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.IntRangeHandler;
import ij.gui.GenericDialog;
import utilities.Geometry.ImageShapes.*;
import ImageAnalysis.PixelHeights;
import utilities.CommonStatisticsMethods;
import utilities.statistics.Histogram;
import utilities.statistics.MeanSem0;
import ImageAnalysis.IPOPixelHeightsHandler;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.statistics.GaussianDistribution;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import ij.IJ;
import ImageAnalysis.ContourFollower;
import ImageAnalysis.LineConnector;
import utilities.io.FileAssist;
import utilities.Non_LinearFitting.Fitting_Function;
import utilities.io.AsciiInputAssist;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.distribution.FDistributionImpl;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.MathException;
import utilities.Non_LinearFitting.LineEnveloper;
import utilities.Geometry.Circle_Double;
import utilities.Categorizer.Categorizer;
import ImageAnalysis.CocentricRingKernel;
import java.awt.*;
import utilities.Gui.ImageComparisonViewer;
import utilities.Gui.AnalysisMasterForm;
import utilities.MachineLearning.WekaDriver;
import utilities.statistics.OneDKMeans;
import R.RDriver;
import java.util.Enumeration;
import stacks.InternalSticher;
import utilities.CommonImageMethods;

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.RMainLoopCallbacks;
/**
 *
 * @author Taihao
 */
class fittingFunction extends Fitting_Function{
    String m_sExpressionType;
    public void setStartingTerms(int nTerm){
        
    }
    public double funValueAndGradient(double[]pars,double[]x,double[]pdGradient){return 0;}//not implemented
    public double funPartialDerivative(double[]pars,double[]x, int index){return 0;};
    public fittingFunction(String sType, int nNumPars, int nNumVars){
        m_nPars=nNumPars;
        m_nVars=nNumVars;
        m_sExpressionType=sType;
    }
    public void getDescriptions(ArrayList<String> svDescription,ArrayList<String> svExpression,ArrayList<String> svBaseParNames,ArrayList<String> svExpandedParNames,ArrayList<Integer> nvNumParameters){
        
    }
    public double fun(double[] pdPars, double[] pdX){
        double dv=0;
        int i;
        if(m_sExpressionType.contentEquals("exponetial_I_t")){
            int nTerms=m_nPars/2;
            dv=pdPars[2*nTerms];
            for(i=0;i<nTerms;i++){
                dv+=pdPars[i]*Math.exp(-pdX[0]/pdPars[nTerms+i]);
            }
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            dv=0;
            for(i=0;i<m_nPars;i++){
                dv+=pdPars[i]*Math.pow(pdX[0], i);
            }
        }
        return dv;
    }
    public double[] getExpandedModel(double[] pdPars0,double[] pdLocation){
        return getExpandedModel(pdPars0,pdLocation,0);
    }
    public double[] getExpandedModel(double[] pdPars0){
        return getExpandedModel(pdPars0,null);
    }
    public double[] getExpandedModel(double[] pdPars0,double[] pdLocation,double diff){
        int nPars0=pdPars0.length;
        double[] pdPars=null;
        int i;
        if(m_sExpressionType.contentEquals("exponetial_I_t")){
            m_nPars=nPars0+2;
            pdPars=new double[m_nPars];
            for(i=0;i<nPars0;i++){
                pdPars[i]=pdPars0[i];
            }
            pdPars[nPars0]=pdPars0[nPars0-2]/10;
            pdPars[nPars0+1]=pdPars0[nPars0-1]*10;
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            m_nPars=nPars0+1;
            pdPars=new double[m_nPars];
            for(i=0;i<nPars0;i++){
                pdPars[i]=pdPars0[i];
            }
            pdPars[nPars0]=pdPars0[nPars0-1]/10;
        }
        return pdPars;
    }
    public boolean expandable(){
        return true;
    }
}

public class Demo_ implements PlugIn{
    
    static{ 
        String st=Core.NATIVE_LIBRARY_NAME;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
    }
    
    public void run(String Arg)
    {
        System.setProperty("com.imsl.license.path", "D:\\Taihao\\MyProjects\\NetBeans Projects\\Java Projects\\VNI\\license\\license.dat");
//        tTestDemo();
        long startTime = System.currentTimeMillis();
//        drawConnection();
//        testRectangle();
//        EdgeElementHandlerDemo();
//        demo_IPOHOrganizer();
//        Demo_findFluoParticles();
//        demo_randomize();
//        demo_meanFilter();
//        testToString();
//        demo_stampPixels();
//        markShape4();
//        showPixelHeights();
//        ImageShapeHandler.displayMinimalRings(0, 10);
//        testGaussianDistribution();
//        testIPOPixelHeightsDetector();
//        testMeanSem0();
//            demo_stampPixels();
//            ImagePlus impl=WindowManager.getCurrentImage();

//            testNormalityOfLowPixels();
//        testFitting();
//        testDistributions();
//        testSmallers();
//        String defaultHeaderDir=FileAssist.getUserHome()+"\\"+"abf header files\\";
//        String defaultHeaderDir1=FileAssist.getUserDir()+"abf header files\\";
//        testCircle();
//        testCategorizer();
//        demoImageShapePerimiter();
//        ImageDisplayCommandsDemo();
//        demoCocentricRingKernel();
//        ImageComparisonViewerDemo();
//        ImageShapeMeanDemo();
//        AnalysisMasterForm.main(null);
//        WekaDriver.test();
//        WekaDriver.getIPOGClassifiers(null, null);
//          testOpenCV();
//          testR();
        
//        clusterTest();
//        CommonSQLMethods.showVersion(null);
        testSQL();
//        testInternalSticher();
//        EdgeElementHandlerDemo();
//        OneDFilter();
        long finishTime = System.currentTimeMillis();
        long elapsedTimeMillis = finishTime-startTime;
        double et=elapsedTimeMillis/1000.;
        double center[]=new double[2];
//        LineEnveloper.getWheelCenter(1671, 594, 1748, 601, false, center, 100, 100);
        et=et;
    }
    void testSmallers(){
        double[] pd1={1,2,3,4,5,6,7,8,9};
        double[] pd2={9,8,7,6,5,4,3,2,1,-6};
        int index=CommonMethods.getNumOfLargerOrEqualElements(pd1, 4.5)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(pd2, 4)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(pd2, .5)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(pd2, 9.5)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(pd2, 9)-1;

        index=CommonMethods.getNumOfSmallerOrEqualElements(pd1, 4.5)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(pd1, 4)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(pd1, .5)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(pd1, 9.5)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(pd1, 9)-1;

        ArrayList<Integer> nv1=new ArrayList(), nv2=new ArrayList();
        int i,len;
        len=10;
        for(i=0;i<len;i++){
            nv1.add(i);
            nv2.add(len-1-i);
        }

        index=CommonMethods.getNumOfLargerOrEqualElements(nv2, 6)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(nv2, 4)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(nv2, 0)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(nv2, 8)-1;
        index=CommonMethods.getNumOfLargerOrEqualElements(nv2, 9)-1;

        index=CommonMethods.getNumOfSmallerOrEqualElements(nv1, 3)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(nv1, 4)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(nv1, 0)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(nv1, 11)-1;
        index=CommonMethods.getNumOfSmallerOrEqualElements(nv1, 9)-1;
    }
    void testToString(){
        double d1,d2,d3;
        int i1,i2,i3;
        String sd1,sd2,sd3,si1,si2,si3;
        for(int i=0;i<100;i++){
            d1=10000*Math.random();
            d2=10000*Math.random();
            d3=d1-d2;
            i1=(int)d1;
            i2=(int)d2;
            i3=(int)d3;
            sd1=PrintAssist.ToString(d1, 2);
            sd2=PrintAssist.ToString(d1, 5);
            sd3=PrintAssist.ToString(d3,4);
            si3=PrintAssist.ToString(i3);
            si1=PrintAssist.ToString(i1);
        }
    }
    void demo_meanFilter(){
        ImagePlus impl=WindowManager.getCurrentImage();
        int h=impl.getHeight();
        int w=impl.getWidth();
        byte[] bp=(byte[])impl.getProcessor().getPixels();
        int pixels[][]=new int[h][w];
        int i,j,o;
        for(i=0;i<h;i++){
            o=w*i;
            for(j=0;j<w;j++){
                pixels[i][j]=0xff&bp[o+j];
            }
        }
        CommonMethods.meanFiltering(w,h,pixels,3);
        for(i=0;i<h;i++){
            o=w*i;
            for(j=0;j<w;j++){
                bp[o+j]=(byte)pixels[i][j];
            }
        }
    }
    void demo_stampPixels(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus implc=CommonMethods.copyToRGBImage(impl);
        int pixel=0;
        ArrayList <Integer> types=new ArrayList();
        IntArray indexes=new IntArray();
        int type=LandscapeAnalyzer.localMaximum;


        ArrayList <IntArray> rgbIndexes=new ArrayList();
        indexes.m_intArray.add(0);
        indexes.m_intArray.add(1);
        types.add(LandscapeAnalyzer.localMaximum);
        rgbIndexes.add(indexes);

        indexes=new IntArray();
        types.add(LandscapeAnalyzer.localMinimum);
        indexes.m_intArray.add(1);
        indexes.m_intArray.add(2);
        rgbIndexes.add(indexes);
  
 

        indexes=new IntArray();
        types.add(LandscapeAnalyzer.watershed);
        indexes.m_intArray.add(2);
        rgbIndexes.add(indexes);
        implc.show();

        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(impl);
        int i,num=impl.getStackSize();
        for(i=0;i<num;i++){
            impl.setSlice(i+1);
            implc.setSlice(i+1);
            CommonMethods.markSpecialLandscapePoints(impl, implc, stampper, types, pixel, rgbIndexes,0,impl.getHeight()-1,0,impl.getWidth()-1);
        } 
    }
    void demo_randomize(){
        ImagePlus impl=WindowManager.getCurrentImage();
        int h=impl.getHeight();
        int w=impl.getWidth();
        byte[] bp=(byte[])impl.getProcessor().getPixels();
        int pixels[][]=new int[h][w];
        int i,j,o;
        for(i=0;i<h;i++){
            o=w*i;
            for(j=0;j<w;j++){
                pixels[i][j]=0xff&bp[o+j];
            }
        }
        CommonMethods.randomize(pixels);
        for(i=0;i<h;i++){
            o=w*i;
            for(j=0;j<w;j++){
                bp[o+j]=(byte)pixels[i][j];
            }
        }
    }
    public static void demo_IPOHOrganizer(){
        ImagePlus impl=WindowManager.getCurrentImage();
        IntensityPeakObjectOrganizer IPOHOrganizer=new IntensityPeakObjectOrganizer();
        IPOHOrganizer.setImage(impl);
//        IPOHOrganizer.exportROITraces();
//        IPOHOrganizer.buildIPOHs();
//        IPOHOrganizer.refineTracks();
//        IPOHOrganizer.rebuildIPOT_ROIs();
        IPOHOrganizer.markIPOTracks();
//        IPOHOrganizer.exportTrackParameters();
        IPOHOrganizer.markIPOs();
    }
    void Demo_findFluoParticles(){
        ImagePlus impl=WindowManager.getCurrentImage();
        FluoObjectHandler foh=new FluoObjectHandler();
        int j,size;
        int n=impl.getNSlices();
        foh.setImage(impl);
        foh.collectBackgroundPixels();
        int sliceIndex=30;
        GraphicalObject GO;
        for(sliceIndex=1;sliceIndex<=n;sliceIndex++){
            impl.setSlice(sliceIndex);
            impl.getProcessor().setColor(Color.white);
            ArrayList <GraphicalObject> GOs=foh.collectFluoParticles(impl, sliceIndex);
            size=GOs.size();
            for(j=0;j<size;j++){
                GO=GOs.get(j);
    //            if(GO.getType().name==null){
    //                i=i;
    //            }
    //            if(GO.getType().name.equalsIgnoreCase("background"))continue;
                if(GO.getArea()>6){
                    j=j;
                }
                GO.markObject(impl);
            }
        }
    }
    void Demo_ThresholdAdjuster(){
        LocalThresholdAdjuster a=new LocalThresholdAdjuster();
    }

    void Demo_Binormial(){
        BinormialDistribution b= new BinormialDistribution();
        double a=b.prob(5, 2, 0.1);
    }
    void Demo_GOListBuilding(){
        
        BrainSimulationGraphicalObjectsHandler goh=new BrainSimulationGraphicalObjectsHandler();
        ImagePlus impl=CommonMethods.importImage();
        ImagePlus impl0=CommonMethods.cloneImage(impl);
        /*
        CommonMethods.SobelEdgeColorImage(impl0);
        impl0.show();
        ImagePlus impl1=CommonMethods.cloneImage(impl0);
        ImageConverter ic=new ImageConverter(impl1);
        ic.convertToGray8();
        impl1.show();*/

        
        
        int w=impl.getWidth();
        int h=impl.getHeight();
        Color c=Color.WHITE;
//        impl0=CommonMethods.drawGraphicalObjects(CommonMethods.newPlainRGBImage("Recognized Objects", w, h, c), edges);
//        impl0=CommonMethods.drawGraphicalObjects(CommonMethods.newPlainRGBImage("Recognized Objects", w, h, c), edges,null);
        impl.show();
//        CommonMethods.labelGrphicalObjects1(impl0, edges);   
        
    }
    void Demo_EnclosedObjects(){
        
        BrainSimulationGraphicalObjectsHandler goh=new BrainSimulationGraphicalObjectsHandler();
        double dMin=256;
        double dMax=-1;
        ImagePlus impl=CommonMethods.getCurrentImage();
        if(impl!=null){
            dMin=impl.getProcessor().getMinThreshold();
            dMax=impl.getProcessor().getMaxThreshold();
            impl.close();
        }
        impl=CommonMethods.importImage();
        ImagePlus impl0=CommonMethods.cloneImage(impl);
        if(dMax<0){
            dMin=150;
            dMax=150;
         }
        impl.show();
        ArrayList <BrainSimulationGraphicalObject> GOs=goh.findBrightObjects(impl0,dMin,dMax);
        goh.buildGOTopology(GOs);
        goh.hideGOType(GOs, -1);
//        goh.showGOType(GOs, 1003);
        
        int w=impl.getWidth();
        int h=impl.getHeight();
        Color c=Color.WHITE;
        impl0=CommonMethods.drawGraphicalObjects(CommonMethods.newPlainRGBImage("Recognized Objects", w, h, c), GOs, Color.WHITE);
        CommonMethods.labelGrphicalObjects(impl0, GOs);
        CommonMethods.labelGrphicalObjects(impl, GOs);
        impl.show();
    }    
    void tTestDemo(){
        int num=0;
        int df;
        double t,p;
        TTestTable table=new TTestTable();
        String filePath="C:\\Taihao\\Lab UCSF\\Imaging\\TTestTable.txt";
        table.OutputTTable(filePath);
        while(num<100){
            df=(int)(Math.random()*100)+1;
            t=5*Math.random();
            p=table.getPValue(df, t);
        }
    }
    void EdgeElementHandlerDemo(){
        ImagePlus impl=WindowManager.getCurrentImage();
        EdgeElementHandler_FRR edl=new EdgeElementHandler_FRR(impl);
    }
    void markShape(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus impl1=CommonMethods.cloneImage(impl);
        ImagePlus impl2=CommonMethods.cloneImage(impl);
        ImagePlus impl3=CommonMethods.cloneImage(impl);
        ImagePlus impl4=CommonMethods.cloneImage(impl);
        GenericDialog gd=new GenericDialog("input position and radius of a circle");
        gd.addNumericField("x", 70, 0);
        gd.addNumericField("y", 110, 0);
        gd.addNumericField("r", 50, 0);
        int x=70,y=110;
        int r=50;
//        gd.showDialog();
        if(gd.wasOKed()){
            x=(int) gd.getNextNumber();
            y=(int) gd.getNextNumber();
            r=(int) gd.getNextNumber();
        }
        int w=impl.getWidth(),h=impl.getHeight();
        Point p=new Point(x,y);
        int dx=0,dy=1;
        CircleImage circle=new CircleImage(p,r,new intRange(0,w-1), new intRange(0,h-1));
        ImageShape sp1=new ImageShape(circle);
        p=circle.getLocation();
//        p.translate(80, 0);
        circle.setLocation(p);
        ImageShape sp2=new ImageShape(circle);
        ImageShape sp3=new ImageShape(circle);
//        sp1.markShape(impl1, p, 1, 0);
        impl1.setTitle("impl1 dx=1, dy=0");
        impl2.setTitle("impl2 dx=-1, dy=0");
        sp2.mergeShape(sp1,60,30);
        sp2.mergeShape(sp3,-10,100);
        sp2.markShape(impl3, 0, 1);
        impl3.setTitle("impl3 dx=0, dy=1");
        sp2.mergeShape(sp1, -80, 40);
        sp2.markShape(impl4, 1, 0);
        impl4.setTitle("impl4 dx=1, dy=0");
        p.translate(-60, -30);
        sp2.markShape(impl1, 1, 0);
        sp2.excludeShape(sp1, -60, -30);
        sp2.markShape(impl2, 1, 0);
        impl1.show();
        impl2.show();
        impl3.show();
        impl4.show();
    }
    void markShape2(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus impl1=CommonMethods.cloneImage(impl);
        ImagePlus impl2=CommonMethods.cloneImage(impl);
        ImagePlus impl3=CommonMethods.cloneImage(impl);
        ImagePlus impl4=CommonMethods.cloneImage(impl);
        GenericDialog gd=new GenericDialog("input position and radius of a circle");
        gd.addNumericField("x", 70, 0);
        gd.addNumericField("y", 110, 0);
        gd.addNumericField("r", 50, 0);
        int x=30,y=110;
        int r=50;
//        gd.showDialog();
        if(gd.wasOKed()){
            x=(int) gd.getNextNumber();
            y=(int) gd.getNextNumber();
            r=(int) gd.getNextNumber();
        }
        int w=impl.getWidth(),h=impl.getHeight();
        Point p=new Point(x,y);
        int dx=0,dy=1;
        CircleImage circle=new CircleImage(p,r,new intRange(0,w-1), new intRange(0,h-1));
        CircleImage circle2=new CircleImage(p,r-20,new intRange(0,w-1), new intRange(0,h-1));
        ImageShape sp1=new ImageShape(circle);
        p=circle.getLocation();
//        p.translate(80, 0);
        circle.setLocation(p);
        p.setLocation(0,0);
        ImageShape sp2=new ImageShape(circle2);
        ImageShape sp3=new ImageShape(circle2);
//        sp1.markShape(impl1, p, 1, 0);
        impl1.setTitle("impl1 dx=0, dy=1");
        impl2.setTitle("impl2 dx=-1, dy=0");
        sp1.excludeShape(sp2,20,-15);
//        sp2.mergeShape(sp3,-14,0);
        sp1.markShape(impl1, 0, 1);
        sp1.excludeShape(sp2,-15,20);
        sp1.markShape(impl2, 0, 1);
//        impl3.setTitle("impl3 dx=0, dy=1");
//        sp2.mergeShape(sp1, -80, 40);
//        sp2.markShape(impl4, p, 1, 0);
//        impl4.setTitle("impl4 dx=1, dy=0");
//        p.translate(-60, -30);
//        sp2.markShape(impl1, p, 1, 0);
//        sp2.excludeShape(sp1, -60, -30);
//        sp2.markShape(impl2, p, 1, 0);
        impl1.show();
        impl2.show();
//        impl3.show();
//        impl4.show();
    }
    void markShape3(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus impl1=CommonMethods.cloneImage(impl);
        ImagePlus impl2=CommonMethods.cloneImage(impl);
        ImagePlus impl3=CommonMethods.cloneImage(impl);
        ImagePlus impl4=CommonMethods.cloneImage(impl);
        int x=80,y=110;
        int r=50;
        int w=impl.getWidth(),h=impl.getHeight();
        Point p=new Point(x,y);
        int dx=0,dy=1;
        int rI=10,rO=25;
        ImageShape ring=new ImageShape(true);

        rO=ring.getXrange().getRange()/2;
        rI=rO/2;

        p.setLocation(2*rO,2*rO);
        ring.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
//        impl2.setTitle("impl2 dx=-1, dy=0");
        p.setLocation(2*rO,2*rO);
        ring.markShape(impl1, 0, -1);
        p.setLocation(4*rO+rI,2*rO);
        ring.markShape(impl1, 0, 1);
        p.setLocation(2*rO,4*rO+rI);
        ring.markShape(impl1, -1, 0);
        p.setLocation(4*rO+rI,4*rO+rI);
        ring.markShape(impl1, 1, 0);
        p.setLocation(4*rO+rI,6*rO+2*rI);
        ring.markShape(impl1, 0, 0);

        p.setLocation(-rO,-rO);
        ring.markShape(impl1, 0, -1);
        p.setLocation(w-rO,-rO);
        ring.markShape(impl, 0, 1);
        p.setLocation(-rO,h-rO);
        ring.markShape(impl1, -1, 0);
        p.setLocation(w-rO,h-rO);
        int nArea=ring.getArea();
        impl1.setTitle("impl1 dx,dy: 0,-1; 0,1; -1,0; 1,0"+"  Area: "+PrintAssist.ToString(nArea));
        ring.markShape(impl1, 1, 0);

        impl1.show();
//        impl2.show();
//        impl3.show();
//        impl4.show();
    }

    void markShape4(){

        ArrayList<intRange> irs1=new ArrayList(), irs2=new ArrayList(),irs;
        irs1.add(new intRange(1,6));
        irs1.add(new intRange(9,11));
        irs1.add(new intRange(13,20));
        irs1.add(new intRange(22,45));
        irs1.add(new intRange(50,75));
        irs1.add(new intRange(83,96));

        irs2.add(new intRange(-21,-5));
        irs2.add(new intRange(-2,-1));
        irs2.add(new intRange(0,20));
        irs2.add(new intRange(21,23));
        irs2.add(new intRange(25,35));
        irs2.add(new intRange(47,49));
        irs2.add(new intRange(98,100));
        irs2.add(new intRange(101,130));

        irs=IntRangeHandler.getOverlappingRanges(irs1, irs2, 0);

        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus impl1;
        int w=impl.getWidth(),h=impl.getHeight();
        impl1=CommonMethods.getBlankImage(ImagePlus.GRAY8, w, h);
        ImagePlus impl2=CommonMethods.getBlankImage(impl.getType(), w, h);
        ImagePlus impl3=CommonMethods.getBlankImage(impl.getType(), w, h);
        int rI=10,rO=25;
        ImageShape ring1=new ImageShape(true);
        ImageShape ring2=new ImageShape(ring1);
        ImageShape ring3=new ImageShape(ring1);
        ImageShape ring4=new ImageShape(ring1);

        rO=ring1.getXrange().getRange()/2;
        ring2.translate(rO, 0);
        ring3.translate(0, rO);
        ring4.translate(rO, rO);
        ArrayList<ImageShape> shapes=new ArrayList();
        shapes.add(ring1);
        shapes.add(ring2);
        shapes.add(ring3);
        shapes.add(ring4);
        ImageShape merged=ImageShape.getMergedShape(shapes);
        merged.markShape(impl1, 0, 0);
        merged.translate(2*rO, 0);
        merged.markShapeY(impl1, 0, 0);
        impl1.show();

        ImageShape overlapped=ImageShape.getOverlap(shapes);
        overlapped.markShape(impl2, 0, 0);
        overlapped.translate(2*rO, 0);
        overlapped.markShapeY(impl2, 0, 0);


        impl2.show();
    }
    void showPixelHeights(){
        int nChoice=1;
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus implh=CommonMethods.cloneImage(impl);
        implh.setTitle("Pixel heights (choice "+PrintAssist.ToString(nChoice)+" ) of "+impl.getTitle());
        PixelHeights.getPixelHeights(impl, implh, nChoice);
        implh.show();
    }
    void testNormalityOfLowPixels(){
//        CommonStatisticsMethods.testNormalityOfLowPixels(WindowManager.getCurrentImage(),1);
//        disabled on 1/14/2014
    }
    void testMeanSem0(){
        double m,mss2,dv,sd1,sd2,sd3;
        int n1,n2;
        MeanSem0 ms1=new MeanSem0(),ms2=new MeanSem0(),ms3=new MeanSem0();
        double []data1={1.2,4.3,-0.5,7.6};
        double []data2={3.2,8.7,-6.8,9.56,8.97};
        double []data3={3.2,8.7,-6.8,9.56,8.97,1.2,4.3,-0.5,7.6};
        n1=data1.length;
        n2=data2.length;
        m=0;
        mss2=0;
        for(int i=0;i<n1;i++){
            dv=data1[i];
            m+=dv;
            mss2+=dv*dv;
        }
        m/=n1;
        mss2/=n1;
        ms1.updateMeanSquareSum(n1, m, mss2);
        sd1=ms1.getSD();

        m=0;
        mss2=0;
        for(int i=0;i<n2;i++){
            dv=data2[i];
            m+=dv;
            mss2+=dv*dv;
        }
        m/=n2;
        mss2/=n2;
        ms2.updateMeanSquareSum(n2, m, mss2);
        sd2=ms2.getSD();

        int n3=data3.length;
        m=0;
        mss2=0;
        for(int i=0;i<n3;i++){
            dv=data3[i];
            m+=dv;
            mss2+=dv*dv;
        }
        m/=n3;
        mss2/=n3;
        ms3.updateMeanSquareSum(n3, m, mss2);
        sd3=ms3.getSD();

        MeanSem0 ms=new MeanSem0(ms1);
        ms.mergeSems(ms2);
        double sd=ms.getSD();
    }
    void testIPOPixelHeightsDetector(){
        String path="D:\\Taihao\\Lab UCSF\\Imaging\\images\\Huanghe\\new dideos\\100204\\2\\02_A_1.1000xpGEMHE-16A-mEGFP-kv14cterm ax gaussian blur r1.phf";
        String pathr="D:\\Taihao\\Lab UCSF\\Imaging\\images\\Huanghe\\new dideos\\100204\\2\\02_A_1.1000xpGEMHE-16A-mEGFP-kv14cterm ax randomized gaussian blur r1.phf";
        ImagePlus impl=WindowManager.getCurrentImage();
        ArrayList<Point> cvLocalMaxima=new ArrayList();
        ArrayList <Double> dvPixelHeights=new ArrayList();
        ArrayList <Double> dvPixelHeightsC=new ArrayList();
        ArrayList <Double> dvPixelHeights0=new ArrayList();
        ArrayList <Integer> nvRadius=new ArrayList();
        ArrayList <int[]> pnvPercentileIndexes=new ArrayList();
        ArrayList <Integer> nvNumLocalMaxima=new ArrayList();

        double[] backgroundPercentiles=new double[16];
        backgroundPercentiles[0]=0.000001;
        backgroundPercentiles[1]=0.00001;
        backgroundPercentiles[2]=0.0001;
        backgroundPercentiles[3]=0.001;
        backgroundPercentiles[4]=0.01;
        backgroundPercentiles[5]=0.05;
        backgroundPercentiles[6]=0.1;
        backgroundPercentiles[7]=0.2;
        backgroundPercentiles[8]=0.3;
        backgroundPercentiles[9]=0.4;
        backgroundPercentiles[10]=0.5;
        backgroundPercentiles[11]=0.6;
        backgroundPercentiles[12]=0.7;
        backgroundPercentiles[13]=0.8;
        backgroundPercentiles[14]=0.9;
        backgroundPercentiles[15]=1.;
        IPOPixelHeightsHandler.detectIPOs(path, pathr, nvRadius, nvNumLocalMaxima, cvLocalMaxima, dvPixelHeights, dvPixelHeights0, dvPixelHeights, backgroundPercentiles, pnvPercentileIndexes);

        int[]percentileIndexes;
        ImageShape mark=new Ring(nvRadius.get(2),nvRadius.get(2));
        int nSlices=nvNumLocalMaxima.size(), slice;
        ArrayList<Point> pointsm=new ArrayList();
        int offset=0,len,i;
        Point pt;
        ImagePlus implc=CommonMethods.copyToRGBImage(impl);
        for(slice=0;slice<nSlices;slice++){
            impl.setSlice(slice+1);
            implc.setSlice(slice+1);
            percentileIndexes=pnvPercentileIndexes.get(slice);
            len=nvNumLocalMaxima.get(slice);
            pointsm.clear();
            for(i=0;i<len;i++){
                pt=cvLocalMaxima.get(offset+i);
                pointsm.add(pt);
            }
            CommonMethods.labelPointsInOrder(implc, "detected IPOs", pointsm, percentileIndexes, mark, 5, 0, 0);
            offset+=len;
        }
    }
    void testGaussianDistribution(){
        double mu=44.44958, sigma=94.23597, z=467, accuracy=0.1;
        double p=GaussianDistribution.Phi(z, mu, sigma);
        double z1=GaussianDistribution.getZatP(p, mu, sigma, p*accuracy);
        p=p;
    }
    void showRings(){
        int nRMax=4,nRRefI=5,nRRefO=8;
        ArrayList<ImageShape> cvRings=new ArrayList();
        ArrayList<Double> dvRs=new ArrayList();
        IPOPixelHeightsHandler.constructIPORings(nRMax, nRRefI, nRRefO, cvRings, dvRs);
        nRMax=nRMax;
    }
    void drawConnection(){
        Point p1=new Point(3,3), p2=new Point(8,27);
        ArrayList <Point> points=LineConnector.getConnection(p1, p2);
        ImagePlus impl =CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, 150, 150);
        int pixel=255<<16|0|0;
        CommonMethods.drawTrail(impl, points, pixel);

        p2=new Point(6,3);
        p1=new Point(11,27);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);
        impl.show();

        p1=new Point(30,30);
        p2=new Point(80,27);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);
        impl.show();

        p2=new Point(30,33);
        p1=new Point(80,30);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);
        impl.show();

        p1=new Point(20,20);
        p2=new Point(80,80);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);

        p1=new Point(130,130);
        p2=new Point(131,131);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);

        p1=new Point(122,120);
        p2=new Point(122,126);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);

        p2=new Point(125,120);
        p1=new Point(125,126);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);

        p1=new Point(120,120);
        p2=new Point(130,120);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);

        p2=new Point(120,125);
        p1=new Point(130,125);
        points=LineConnector.getConnection(p1, p2);
        pixel=0|255<<8|0;
        CommonMethods.drawTrail(impl, points, pixel);
        impl.show();
    }
    void testContourBuilding(){
        String path="D:\\Taihao\\Lab UCSF\\Imaging\\images\\Huanghe\\new dideos\\100414\\2\\after correcting shapes\\smaller size\\smaller stack\\blank image.Roi";
        File file = new File(path);
        FileReader f=null;
        try{f=new FileReader(file);}
        catch(FileNotFoundException e){
            IJ.error("");
        }
        BufferedReader br=new BufferedReader(f);
        String line="";
        try{
            line=br.readLine();
        }catch (IOException e){

        }
        StringTokenizer stk=new StringTokenizer(line," ",false);
        String st=stk.nextToken();
        st=stk.nextToken();
        int type=Integer.valueOf(st);

        try{
            line=br.readLine();
        }catch (IOException e){

        }
        stk=new StringTokenizer(line);
        stk.nextToken();
        int nPoints=Integer.valueOf(stk.nextToken());
        try{
            line=br.readLine();
        }catch (IOException e){

        }
        stk=new StringTokenizer(line);
        stk.nextToken();
        int x0=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int y0=Integer.valueOf(stk.nextToken());
        int x,y;
        String sTemp;

        ArrayList<Point> points=new ArrayList();

        for(int i=0;i<nPoints;i++){
            try{
                line=br.readLine();
            }catch (IOException e){

            }
            stk=new StringTokenizer(line);
            stk.nextToken();
            stk.nextToken();
            stk.nextToken();
            sTemp=stk.nextToken();
            x=Integer.valueOf(sTemp);
            stk.nextToken();
            sTemp=stk.nextToken();
            y=Integer.valueOf(sTemp);
            points.add(new Point(x,y));
        }
        ArrayList<Point> contour=ContourFollower.buildContour(points,1);
        int pixel=255<<16|0|0;
        ImagePlus impl =CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, 150, 150);
        impl.show();
        CommonMethods.drawTrail(impl, contour, pixel);
        ImageShape is=ImageShapeHandler.buildImageShape(contour);
        pixel=0|255<<8|0;
        ImageShapeHandler.drawShape(is, impl, pixel);
        impl.show();
        path=FileAssist.changeExt(path, "txt");
        path=FileAssist.getExtendedFileName(path, "_contour");
        ContourFollower.exportContour(path, contour);
    }
    void testRectangle(){
        RectangleImage rt=new RectangleImage(100, 150);
        ImagePlus impl=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, 500, 500);
        rt.setCenter(new Point(250,250));
        rt.markShape(impl, 0, 0);
        impl.show();
    }
    void testFitting(){
        String pathData="D:\\Taihao\\Electrophysiology Data\\TMEM16F\\Huanghe\\hy091310\\Data9.txt";
        double[][] pdData=AsciiInputAssist.readAsciiArray(pathData);//each row of this array is one column in the data file
        int rows=pdData.length,i,j;
        double pdX[][]=new double[rows][1],pdY[]=new double[rows];
        for(i=0;i<rows;i++){
            pdX[i][0]=pdData[i][0];
            pdY[i]=pdData[i][1];
        }
        double[] pdPars={1,1,1};
        fittingFunction func=new fittingFunction("polynomial",pdPars.length,1);
        Non_Linear_Fitter fitter=new Non_Linear_Fitter(pdX,pdY,pdPars,func,Non_Linear_Fitter.Least_Square,Non_Linear_Fitter.Simplex,0,pdY.length-1,1,null);
        double pdFittedPars[]=fitter.getFittedPars();
    }
    void testDistributions(){
        double df=15,df1=5,df2=10;
        ChiSquaredDistributionImpl chi_dist = new ChiSquaredDistributionImpl(df);
        FDistributionImpl f_dist=new FDistributionImpl(df1,df2);
        NormalDistributionImpl g_dist=new NormalDistributionImpl();
        double x=32.801,p=0,x1=0;
        double d=chi_dist.density(x);
        try{
            p=chi_dist.cumulativeProbability(x);
            x1=chi_dist.inverseCumulativeProbability(p);

            x=10.481;
            p=f_dist.cumulativeProbability(x);
            x1=f_dist.inverseCumulativeProbability(p);

            x=0.4;
            p=g_dist.cumulativeProbability(x);
            x1=g_dist.inverseCumulativeProbability(p);
        }
        catch (MathException e){

        }
    }
    void testCircle(){
        Circle_Double c=new Circle_Double(0.00000001,0.00000001,2);
        double ds=c.areaInTriangle(4, 0, 0, 0, 0, 4);
        ds=c.areaInTriangle(2, 0, 0, 0, 0, 2);
        ds=c.areaInTriangle(1, 0, 0, 0, 0, 1);
        ds=c.areaInTriangle(2.5, 0, 0, 0, 0, 2.5);
        ds=c.areaInTriangle(1, 0, 0, 0, 0, 4);
        ds=c.areaDisectedByLine(-4, 0, 4, 0);
        ds=c.areaDisectedByTwoRays(-4, 0, 0, 0, 4, 4);
        ds=c.areaDisectedByTwoRays(4, 0, 0, 0, 4, 4);
    }
    void testCategorizer(){
        int[][] indexes=new int[10][4];
        double[][] pdV=new double[10][4];
        double dv;
        int i,j;
        ArrayList<double[]> vpdDelimiters=new ArrayList();
        for(i=0;i<4;i++){
            double[]pdDelimiters=new double[150];
            for(j=0;j<150;j++){
                pdDelimiters[j]=j;
            }
            vpdDelimiters.add(pdDelimiters);
        }
        Categorizer cNode=new Categorizer(vpdDelimiters, true);
        for(i=0;i<10;i++){
            for(j=0;j<4;j++){
                dv=200*Math.random()-20;
                pdV[i][j]=dv;
            }
            cNode.getCatIndexes(pdV[i], indexes[i]);
        }
        dv=0;
    }
    void demoCocentricRingKernel(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ArrayList<Double> vdRs=new ArrayList();
//        vdRs.add(0.5);
        int i,j;
        for(i=1;i<12;i++){
            vdRs.add((double)i);
        }
        SubpixelAreaInRingsLUT subpixelIS=new SubpixelAreaInRingsLUT(vdRs,5);
        cocentricKernelFilter(impl,subpixelIS,3);
    }
    void cocentricKernelFilter(ImagePlus impl,SubpixelAreaInRingsLUT subpixelIS,double radius){

        int[][] pixels=CommonMethods.getPixelValues(impl);
        int w=impl.getWidth(),h=impl.getHeight();
        subpixelIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        ImagePlus implc=CommonMethods.getBlankImage(impl.getType(), w, h);
        CocentricRingKernel ccKernel=new CocentricRingKernel(pixels,subpixelIS,CocentricRingKernel.gaussianWeight);
        ccKernel.setRadius(3);
        int i,j;
        int[][] pixelsf=new int[h][w];
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                subpixelIS.setCenter_Subpixel(i, j);
                subpixelIS.setEnclosingRectangleCenter(new Point(i,j));
                pixelsf[i][j]=(int)ccKernel.KernelMean(j, i, 0);
            }
        }
        CommonMethods.setPixels(implc, pixelsf);
        implc.show();
    }
    void demoImageShapePerimiter(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus implc=CommonMethods.cloneImage(impl);
        ImagePlus implc2=CommonMethods.cloneImage(implc);
        int[][] pixels=CommonMethods.getPixelValues(impl);
        int w=impl.getWidth(),h=impl.getHeight();
        Point p=new Point(22,44);
        int pixel=pixels[p.y][p.x];
        ArrayList<Point> contour=ContourFollower.getContour_Out(pixels, w, h, p, pixel-1, Integer.MAX_VALUE);
        ImageShape is=ImageShapeHandler.buildImageShape(contour);
        Point po=is.getLocation();
        is.setLocation(po);
        ImagePlus implc3=CommonMethods.cloneImage(implc);
        CommonMethods.drawTrail(implc3, contour, pixel-200);
        implc3.show();
        ArrayList<Point> perimeter=is.getPerimeterPoints();
        ArrayList<Point> contour2=is.getOuterContour();
        CommonMethods.drawTrail(implc, contour, pixel-200);
        implc.setTitle("original contour");
        implc2.setTitle("reconstructed contour and perimeter");
        CommonMethods.drawTrail(implc2, contour2, pixel-200);
        CommonMethods.drawTrail(implc2, perimeter, pixel+200);
        implc.show();
        implc2.show();
    }
    void ImageDisplayCommandsDemo(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ArrayList<ImagePlus> impls=CommonMethods.getAllOpenImages();
        int len=impls.size(),i;
        for(i=len-1;i>=0;i--){
            if(impls.get(i)==impl) impls.remove(i);
        }
        displayTargetImages(impl,impls);
    }
    public void displayTargetImages(ImagePlus Implsr, ArrayList<ImagePlus> impls){
        ImagePlus impl=WindowManager.getCurrentImage();
        int len=impls.size();
        int i;
        int w=impl.getWindow().getWidth(),h=impl.getWindow().getHeight();
        double mag=impl.getWindow().getCanvas().getMagnification();
        Point p=impl.getWindow().getLocation();
        Rectangle srcRect=impl.getWindow().getCanvas().getSrcRect();
        int x=p.x,y=p.y;
        for(i=0;i<len;i++){
            impl=impls.get(i);
            impl.getWindow().setLocationAndSize(x+(i+1)*w, y, w, h,false,false);
            impl.getWindow().getCanvas().setMagnification(mag);
            impl.getWindow().getCanvas().setSrcRect(srcRect);
            impl.draw();
        }
    }
    public void ImageComparisonViewerDemo(){
        ImageComparisonViewer.main(null);
    }
    public void ImageShapeMeanDemo(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus implc=CommonMethods.cloneImage(impl);
        ImageShape cIS=new CircleImage(3);
        int[][] pixels=CommonMethods.getPixelValues(impl);
        int[][] pixelsm=CommonStatisticsMethods.getMean_ImageShape(pixels, cIS);
        CommonMethods.setPixels(implc, pixelsm);
        implc.show();
        ImagePlus impls=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, impl.getWidth(), impl.getHeight());
        cIS.setCenter(new Point(10,10));
        ImageShapeHandler.markShape(impls, cIS, Color.RED.getRGB());
        ImageShape is=new Ring(1,1);
        is.setCenter(new Point(20,20));
        ImageShapeHandler.markShape(impls, is, Color.RED.getRGB());
        is=new Ring(2,2);
        is.setCenter(new Point(30,30));
        ImageShapeHandler.markShape(impls, is, Color.RED.getRGB());
        is=new Ring(3,3);
        is.setCenter(new Point(40,40));
        ImageShapeHandler.markShape(impls, is, Color.RED.getRGB());
        impls.show();
    }
    void clusterTest(){
        int len=10;
        double dt;
        double[] pdData=new double[len];
        for(int i=0;i<len;i++){
            dt=Math.random();
            pdData[i]=(i%1)*1+dt;
        }
        pdData[len/2]=1.5;
//        pdData[len/3]=8.7;
//        pdData[len/4]=8.2;
//        pdData[len/5]=8.5;
//        pdData[len/6]=8.45;
        OneDKMeans cKM=new OneDKMeans(pdData,6);
        cKM=cKM;
        double[] pdmeans=cKM.getMeans();
    }
    void testR(){
        String st=System.getProperty("java.library.path");
        String args[]={""};
        Rengine re=RDriver.getRengine();
        double[] pdA={1,3,1,4,5,2},pdB={7,5,7,8,6,7};
        String A="A",B="B";
        re.assign("A", pdA);
        re.assign("B", pdB);
        REXP t=re.eval("t.test(A,B)");
        RList rl=t.asList();
        REXP rp=rl.at("p.value"),rn=rl.at("parameter"),rname=rl.at("data.name");
        String sname=rname.asString();
        int nn=rn.asInt();
        re.eval("boxplot(A,B)");
        double p=rp.asDouble();
        String sT=t.asString();
        String sT1=st;
    }
    void testSQL(){
        String sDBName="StrangeSTUDENTS",sTableName="REGISTRATION";
//        Connection conn=CommonSQLMethods.getDefaultSQLConnection();
        CommonSQLMethods.createDatabase(sDBName);
        CommonSQLMethods.createTable(sDBName,sTableName);
        CommonSQLMethods.dropTable(sDBName,sTableName);
        CommonSQLMethods.dropDatabase(sDBName);        
    }
    void testOpenCV(){
        System.out.println("Welcome to OpenCV " + Core.VERSION);
        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
        System.out.println("OpenCV Mat: " + m);
        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));
        System.out.println("OpenCV Mat data:\n" + m.dump());        
    }
    void testInternalSticher(){
        InternalSticher is=new InternalSticher(CommonMethods.getCurrentImage(),4,4);
//        InternalSticher is=new InternalSticher(CommonMethods.getCurrentImage(),4,4,"D:\\Taihao\\Lab UCSF\\Presentations\\lab meeting 09610\\09606\\Stitching Shift 1.txt");
    }
    void OneDFilter(){
        int type=ImagePlus.GRAY32;
        ImagePlus impl=CommonMethods.getCurrentImage();
        int ws=2;
        CommonImageMethods.PlaneFittingFilter_OptimalFitting(CommonImageMethods.copyImage(impl.getTitle()+" Optimal Plane Fitting ws"+ws,impl,type), ws);
//        CommonImageMethods.showEnvImages(impl, 2);
//        ImagePlus implXM=CommonImageMethods.copyImage(impl.getTitle()+"XMean ws="+ws,impl,type);
//       CommonImageMethods.PlaneFittingFilter_OptimalFitting(implXM, ws);
//        CommonImageMethods.XMean(implXM, ws);
//        CommonImageMethods.findRidges_X(CommonImageMethods.copyImage(implXM.getTitle()+"Ridges"+ws,implXM,type));
//        ImagePlus implYG=CommonImageMethods.copyImage(implXM.getTitle()+"YGradient ws="+ws,impl,type);
//        CommonImageMethods.findYGradient(implYG, ws);
//        CommonImageMethods.findRidges_X(CommonImageMethods.copyImage(implYG.getTitle()+"Ridges"+ws,implYG,type));
//        CommonImageMethods.linkHorizontalLines(implXM);
        
//        ImagePlus implr=CommonImageMethods.copyImage(impl.getTitle()+"reference"+ws,impl,type);
//        CommonMethods.randomizeImage(implr);
//        ImagePlus implXMr=CommonImageMethods.copyImage(impl.getTitle()+"XMean ws="+ws,implr,type);
//        CommonImageMethods.XMean(implXMr, ws);
//        CommonImageMethods.findRidges_X(CommonImageMethods.copyImage(implXMr.getTitle()+"Ridges"+ws,implXMr,type));
//        CommonImageMethods.linkHorizontalLines(implXMr);
//        CommonImageMethods.YMean(CommonImageMethods.copyImage(impl.getTitle()+"YMen ws=1",impl,type), 1);
//        CommonImageMethods.findXGradient(CommonImageMethods.copyImage(impl.getTitle()+"XGradient ws="+ws,impl,type), ws);
//        CommonImageMethods.findYGradient(CommonImageMethods.copyImage(impl.getTitle()+"YGradient ws="+ws,impl,type), ws);
//        CommonImageMethods.findYGradientExclusive(CommonImageMethods.copyImage(impl.getTitle()+"YGradientExclusive ws=3",impl,type), 3);
//        CommonImageMethods.findXLine(CommonImageMethods.copyImage(impl.getTitle()+"XLine ws=3",impl,type), 1,3);
//        CommonImageMethods.findYLine(CommonImageMethods.copyImage(impl.getTitle()+"YLine ws=3",impl,type), 1,3);
//        CommonImageMethods.findXLineExclusive(CommonImageMethods.copyImage(impl.getTitle()+"XLineExclusive ws=3",impl,type), 1,3);
    }
}
