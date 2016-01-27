/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.Hashtable;
import ij.plugin.filter.GaussianBlur;
import ij.io.FileSaver;
import FluoObjects.IntensityPeakObject;
import java.awt.Frame;
import ImageAnalysis.ContourFollower;
import utilities.CustomDataTypes.intRange;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import ij.process.ByteProcessor;
import ij.process.ShortProcessor;
import ij.process.FloatProcessor;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import BrainSimulations.BrainSimulationGraphicalObject;
import ij.IJ;
import utilities.ArrayofArrays.IntArray;
import ij.io.Opener;
import ij.io.OpenDialog;
import java.awt.Color;
import java.awt.Font;
import ij.gui.HistogramWindow;
import java.awt.FontMetrics;
import ij.WindowManager;
import utilities.Geometry.Point2D;
import utilities.Geometry.ConvexPolygon;
import java.util.Stack;
import utilities.QuickFormatter;
import ij.process.ImageConverter;
import CommonDataClasses.MeanSem;
import FluoObjects.IPOAnalyzerForm;
import ij.gui.Roi;
import java.awt.Rectangle;
import java.util.Formatter;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.Point;
import utilities.ArrayofArrays.PointArray;
import utilities.ArrayofArrays.PointArray2;
import java.io.FileFilter;
import java.awt.FileDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import ImageAnalysis.LandscapeAnalyzer;
import ImageAnalysis.GeneralGraphicalObject;
import ImageAnalysis.RegionFinderAmp;
import ImageAnalysis.LandscapeAnalyzerGray8;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import utilities.statistics.Histogram;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.statistics.HistogramHandler;
import ImageAnalysis.ImageScanner;
import ImageAnalysis.PercentileFilter;
import ImageAnalysis.export_AutoCorrelation;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.statistics.MeanSem0;
import utilities.R250_521;
import java.util.Set;
import java.util.Iterator;
import utilities.CustomDataTypes.IntPair;
import ImageAnalysis.AnnotatedImagePlus;
import ImageAnalysis.RegionNode;
import ImageAnalysis.RegionBoundaryAnalyzer;
import ImageAnalysis.RegionComplexNode;
import utilities.Geometry.ImageShapes.*;
import utilities.CustomDataTypes.DoubleRange;
import javax.swing.SwingUtilities;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import FluoObjects.IPOGaussianNode;
import ImageAnalysis.TwoDFunction;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.awt.image.BufferedImage;
import ij.gui.Line;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import utilities.statistics.GaussianDistribution;
import utilities.Gui.Dialogs.OneTextFieldInputDialog;
import java.awt.Rectangle;
import javax.swing.*;
import utilities.CustomDataTypes.EllipseParNode;
import ij.process.EllipseFitter;
import java.text.NumberFormat;
import java.text.ParsePosition;
import utilities.CustomDataTypes.DoublePair;
import utilities.statistics.MeanSem1;
import utilities.io.PrintAssist;
/**
 *
 * @author Taihao
 */
public class CommonMethods {

	/** 8-bit grayscale (unsigned)*/
	public static final int GRAY8 = 0;
	
	/** 16-bit grayscale (unsigned) */
	public static final int GRAY16 = 1;
	
	/** 32-bit floating-point grayscale */
	public static final int GRAY32 = 2;
	
	/** 8-bit indexed color */
	public static final int COLOR_256 = 3;
	
	/** 32-bit RGB color */
	public static final int COLOR_RGB = 4;
        public static final double machEps=MachineEpsilonDouble();

    public static double[] uniformHist(int nDim, intRange range){
        double hist[]=new double[nDim];        
        int min=range.getMin();
        int max=range.getMax();
        if(min<0)min=0;
        if(max>=nDim)max=nDim-1;
        for(int i=0;i<nDim;i++){
            hist[i]=0.;
        }
        for(int i=min;i<=max;i++){
            hist[i]=1./(double)(max-min+1);
        }
        return hist;
    }
    
    public static double[] TriangleHist(int nDim, intRange range){
        double hist[]=new double[nDim];        
        int min=range.getMin();
        int max=range.getMax();
        if(min<0)min=0;
        if(max>=nDim)max=nDim-1;
        for(int i=0;i<nDim;i++){
            hist[i]=0.;
        }
        double c=0.5*(max+min);
        double sum,p,d;
        sum=0.;
        for(int i=min;i<=max;i++){
            d=c-i;
            if(Math.abs(d)<0.5) d=0.5;
            p=1./(d*d);
            hist[i]=p;
            sum+=p;
        }
        for(int i=min;i<=max;i++){
            hist[i]/=sum;
        }
        return hist;
    }
    
    public static int[] intTOrgb(int pixel){ 
        int[] rgb=new int[3];
        int r=0,g=1,b=2;
        rgb[r]=0xff & (pixel>>16);
        rgb[g]=0xff & (pixel>>8);
        rgb[b]=0xff & pixel;
        return rgb;
    }    
    public static void intTOrgb(int pixel, int[] rgb){ //
        int r=0,g=1,b=2;
        rgb[r]=0xff & (pixel>>16);
        rgb[g]=0xff & (pixel>>8);
        rgb[b]=0xff & pixel;
    }
    public static void rgbTOint(int[] pixels, int[] R, int G[], int B[]){
        int length=R.length;
        if(pixels==null||pixels.length!=length) pixels=new int[length];
        for(int i=0;i<length;i++){
            pixels[i]=(R[i]<<16)|(G[i]<<8)|B[i];
        }
    }
    public static int rgbTOint(int[] rgb){
        int pixel;
        int r=rgb[0];
        int g=rgb[1];
        int b=rgb[2];
        pixel=(r<<16)|(g<<8)|b;
        return pixel;
    }
    public static ImagePlus getRGBImage(String title, int width, int height, int[] pixels){
        ImageProcessor impr=new ColorProcessor(width, height, pixels);
        ImagePlus impl=new ImagePlus(title,impr);
        impl.show();
        return impl;
    } 
    public static void getRGB(ImagePlus impl0, byte[] R, byte[] G, byte[]B){
        if(impl0.getType()==COLOR_RGB){
            int h=impl0.getHeight();
            int w=impl0.getWidth();
            int pixels[]=(int[])impl0.getProcessor().getPixels();
            ColorProcessor cp=new ColorProcessor(w,h,pixels);
            cp.getRGB(R,G,B);            
        }
    }
    
    public static void getRGB(ImagePlus impl0, int[] iR, int[] iG, int[]iB){
        if(impl0.getType()==COLOR_RGB){
            int h=impl0.getHeight();
            int w=impl0.getWidth();
            byte[] R=new byte[h*w],B=new byte[h*w],G=new byte[h*w];
            int pixels[]=(int[])impl0.getProcessor().getPixels();
            ColorProcessor cp=new ColorProcessor(w,h,pixels);
            cp.getRGB(R,G,B);  
            for(int i=0;i<h*w;i++){
                iR[i]=0xff & R[i];
                iG[i]=0xff & G[i];
                iB[i]=0xff & B[i];
            }
        }
    }
    
    public static void showStatusAndProgress(String title, double percentage){
            IJ.showStatus(title);
            IJ.showProgress(percentage);
    }
    public static void CollectGargages(){
        Runtime rt=Runtime.getRuntime();  
        rt.gc();
    }
    public static long freeMemory(){        
        Runtime rt=Runtime.getRuntime();  
        return rt.freeMemory()/(1024*1024);
    }
    public static long totalMemory(){        
        Runtime rt=Runtime.getRuntime();  
        return rt.totalMemory()/(1024*1024);
    }
    public static long maxMemory(){        
        Runtime rt=Runtime.getRuntime();  
        return rt.maxMemory()/(1024*1024);
    }
    public static long currentTime(){
        return System.currentTimeMillis();
    }
    
    public static boolean containsContent(IntArray ia, int n){
        int size=ia.m_intArray.size();
        for(int i=0;i<size;i++){
            if(n==ia.m_intArray.get(i)) return true;
        }
        return false;
    }

    public static boolean containsContent(ArrayList<Integer> ia, int n){
        int size=ia.size();
        for(int i=0;i<size;i++){
            if(n==ia.get(i)) return true;
        }
        return false;
    }

    public static int IndexOf(ArrayList<Integer> ia, int n){
        int size=ia.size();
        for(int i=0;i<size;i++){
            if(n==ia.get(i)) return i;
        }
        return -1;
    }

    public static boolean containsContent(int[] pnA, int n){
        if(pnA==null) return false;
        int size=pnA.length;
        for(int i=0;i<size;i++){
            if(n==pnA[i]) return true;
        }
        return false;
    }

    public static boolean containsContent(ArrayList <Point2D> a, Point2D p){
        int size=a.size();
        for(int i=0;i<size;i++){
            if(p.equalContents(a.get(i))) return true;
        }
        return false;
    }

    public static boolean containsContent(ArrayList <Point> a, Point p){
        int size=a.size();
        Point pt;
        for(int i=0;i<size;i++){
            pt=a.get(i);
            if(p.x==pt.x&&p.y==pt.y) return true;
        }
        return false;
    }

    public static boolean containsContent(PointArray2 pa, Point p){
        if(findContent(pa, p)!=null) return true;
        return false;
    }

    public static ArrayList <Point> findContent(PointArray2 pa, Point p){
        int size=pa.m_pointArray2.size();
        for(int i=0;i<size;i++){
            if(containsContent(pa.m_pointArray2.get(i).m_pointArray,p)) return pa.m_pointArray2.get(i).m_pointArray;
        }
        return null;
    }

    public static PointArray findPoints(PointArray2 pa, Point p){
        int size=pa.m_pointArray2.size();
        for(int i=0;i<size;i++){
            if(containsContent(pa.m_pointArray2.get(i).m_pointArray,p)) pa.m_pointArray2.get(i);
        }
        return null;
    }
    public static ArrayList<Integer> findPoint(ArrayList<Point> pa, Point p){
        ArrayList<Integer> indexes=new ArrayList<Integer>();
        int size=pa.size();
        for(int i=0;i<size;i++){
            if(pa.get(i).equals(p))indexes.add(i);
        }
        return indexes;
    }

    public static void splitColors(ImagePlus RGB, ImagePlus [] rgb){
        int h=RGB.getHeight();
        int w=RGB.getWidth();
        byte[] R=new byte[h*w],B=new byte[h*w],G=new byte[h*w];
        int pixels[]=(int[])RGB.getProcessor().getPixels();
        ColorProcessor cp=new ColorProcessor(w,h,pixels);
        cp.getRGB(R,G,B);
        ImagePlus r,g,b;
        r=new ImagePlus("red color", new ByteProcessor(w, h, R, null));
        g=new ImagePlus("green color", new ByteProcessor(w, h, G, null));
        b=new ImagePlus("blue color", new ByteProcessor(w, h, B, null));
//        r.show();
//        g.show();
//        b.show();
        rgb[0]=r;
        rgb[1]=g;
        rgb[2]=b;
    }
    public static ImagePlus drawGraphicalObjects(ImagePlus impl, ArrayList <BrainSimulationGraphicalObject> gos, Color color){
        int width=impl.getWidth();
        int height=impl.getHeight();
        int pixels[]=(int[])impl.getProcessor().getPixels();
        int size=gos.size();    
        for(int i=0;i<size;i++){
            if(color!=null) gos.get(i).setPixel(color);
            gos.get(i).draw_XStripe(pixels, height, width);
        }
        ImagePlus impl0=getRGBImage("Recognized Graphical Objects", width, height, pixels);
        return impl0;
    }

    public static ImagePlus cloneImage(ImagePlus impl){
        return cloneImage("Cloned Image",impl,1,impl.getStackSize());
    }
    public static ImagePlus cloneImage(String title, ImagePlus impl){
        return cloneImage(title,impl,1,impl.getNSlices());
    }
    public static ImagePlus cloneImage(ImagePlus impl, int slice){
        return cloneImage("Cloned Image",impl,slice,slice);
    }
    public static void refreshImage(ImagePlus impl){
        impl.show();
        impl.getProcessor().createImage();
        impl.draw();
    }
    public static ImagePlus cloneImage(String title, ImagePlus impl,int nI, int nF){
        ImagePlus cip;
        int type=impl.getType();
        cip=new ImagePlus(title,copyProcessor(impl));
        int h=impl.getHeight(),w=impl.getWidth();
        ImageStack is=new ImageStack(w,h);
        int n=impl.getNSlices();
        int i;
        for(i=nI;i<=nF;i++){
            impl.setSlice(i);
            ImageProcessor ip=copyProcessor(impl);
            is.addSlice(i+"-th slice", ip);
        }
        cip=new ImagePlus(title,is);
        return cip;
    }
    public static ImagePlus copyToRGBImage(ImagePlus impl){
        int w=impl.getWidth(),h=impl.getHeight();
        int type=impl.getType();
        ImageStack is=new ImageStack(w,h);
        int numSlices=impl.getNSlices();
        int pn[]=new int[2];
        CommonMethods.getPixelValueRange_Stack(impl, pn);
        for(int i=1;i<=numSlices;i++){
            impl.setSlice(i);
            ColorProcessor ip=copyToRGBProcesser(impl.getProcessor(),type,(float)pn[0],(float)pn[1]);
            is.addSlice(i+"-th slice", ip);
        } 
        String title=impl.getTitle();
        title=getFileName(title)+"_rgbCopy"+getFileExtention(title);
        ImagePlus impl1=new ImagePlus(title,is);
        return impl1;
    }
    public static String getFileName(String path){
        String name;
        int position=path.lastIndexOf('\\');
        if(position>0){
            name=path.substring(position+1);
        }else{
            name=path;
        }
        position=name.lastIndexOf('.');
        if(position>0){
            name=name.substring(0,position);
        }
        return name;
    }
    public static String getDirectory(String path){
        String name;
        int position=path.lastIndexOf('\\');
        if(position>0){
            name=path.substring(0,position);
        }else{
            name=path;
        }
        return name+"\\";
    }
    public static String getFileExtention(String title){
        String ext;
        int position=title.lastIndexOf('.');
        if(position>0){
            ext=title.substring(position+1);
        }else{
            ext="";
        }
        return ext;
    }
    public static ColorProcessor copyToRGBProcesser(ImageProcessor ip, int type, float fn, float fx){
        int w=ip.getWidth(),h=ip.getHeight();
        ColorProcessor RGBip=new ColorProcessor(w,h);
        int[] rgbPixels=(int[])RGBip.getPixels();

        switch (type){
            case GRAY8:
                byte[] pixels=(byte[]) ip.getPixels();
                int len=pixels.length;
                int pixel;
                for(int i=0;i<len;i++){
                    pixel=0xff&pixels[i];
                    pixel=(int)(interpolation(fn,0,fx,255,pixel)+0.5);
                    rgbPixels[i]=(pixel<<16)|(pixel<<8)|pixel;
                }
                break;
            case GRAY16:
                short[] pixels1=(short[]) ip.getPixels();
                len=pixels1.length;
                for(int i=0;i<len;i++){
                    pixel=0xffff&pixels1[i];
                    pixel=(int)(interpolation(fn,0,fx,255,pixel)+0.5);
                    rgbPixels[i]=(pixel<<16)|(pixel<<8)|pixel;
                }
                break;
            case COLOR_256:
                byte[] pixels3=(byte[]) ip.getPixels();
                len=pixels3.length;
                for(int i=0;i<len;i++){
                    pixel=0xff&pixels3[i];
                    rgbPixels[i]=(pixel<<16)|(pixel<<8)|pixel;
                }
                break;
            case GRAY32:
                float[] pixels5=(float[]) ip.getPixels();
                len=pixels5.length;
                float ft;
                for(int i=0;i<len;i++){
                    ft=pixels5[i];
                    pixel=(int)(interpolation(fn,0,fx,255,ft)+0.5);
                    rgbPixels[i]=(pixel<<16)|(pixel<<8)|pixel;
                }

                break;
            case COLOR_RGB:
                int[] pixels7=(int[]) ip.getPixels();
                len=pixels7.length;

                for(int i=0;i<len;i++){
                    rgbPixels[i]=pixels7[i];
                }
                break;
            default:
                pixels7=(int[]) ip.getPixels();
                len=pixels7.length;

                for(int i=0;i<len;i++){
                    rgbPixels[i]=pixels7[i];
                }
                break;
        }
        return RGBip;
    }
    public static ImageProcessor copyProcessor(ImagePlus impl){
        int type=impl.getType();
        int h=impl.getHeight(),w=impl.getWidth();
        ImageProcessor impr;


        switch (type){
            case GRAY8:
                byte[] pixels0=(byte[]) impl.getProcessor().getPixels();
                int len=pixels0.length;
                byte[] pixels=new byte[len];

                for(int i=0;i<len;i++){
                    pixels[i]=pixels0[i];
                }
                impr=new ByteProcessor(w,h,pixels,null);
                break;
            case GRAY16:
                short[] pixels1=(short[]) impl.getProcessor().getPixels();
                len=pixels1.length;
                short[] pixels2=new short[len];

                for(int i=0;i<len;i++){
                    pixels2[i]=pixels1[i];
                }
                ColorModel cm=null;
                impr=new ShortProcessor(w,h,pixels2,cm);
                break;
            case COLOR_256:
                byte[] pixels3=(byte[]) impl.getProcessor().getPixels();
                len=pixels3.length;
                byte[] pixels4=new byte[len];

                for(int i=0;i<len;i++){
                    pixels4[i]=pixels3[i];
                }
                impr=new ByteProcessor(w,h,pixels4,null);
                break;
            case GRAY32:
                float[] pixels5=(float[]) impl.getProcessor().getPixels();
                len=pixels5.length;
                float[] pixels6=new float[len];

                for(int i=0;i<len;i++){
                    pixels6[i]=pixels5[i];
                }
                impr=new FloatProcessor(w,h,pixels6,null);
                break;
            case COLOR_RGB:
                int[] pixels7=(int[]) impl.getProcessor().getPixels();
                len=pixels7.length;
                int[] pixels8=new int[len];

                for(int i=0;i<len;i++){
                    pixels8[i]=pixels7[i];
                }
                impr=new ColorProcessor(w,h,pixels8);
                break;
            default:
                pixels7=(int[]) impl.getProcessor().getPixels();
                len=pixels7.length;
                pixels8=new int[len];

                for(int i=0;i<len;i++){
                    pixels8[i]=pixels7[i];
                }
                impr=new ColorProcessor(w,h,pixels8);
                break;
        }

//        ImageProcessor impr=impl.getProcessor().createProcessor(w,h);
//        impr.setPixels(pixels);
        return impr;
    }
  public static ImagePlus importImage(){
        OpenDialog od=new OpenDialog("Opening an image file","");
        String dir=od.getDirectory();
        String name=od.getFileName();
        Opener op=new Opener();
        ImagePlus impl=op.openImage(dir+name);
        return impl;
    }
    public static ImagePlus importImage(String path){
        if(!fileExists(path)) return importImage();
        Opener op=new Opener();
        ImagePlus impl0=op.openImage(path);
        return impl0;
    }
    public static boolean fileExists(String path){
        File file=new File(path);
        return (file.exists());
    }
    public static void SobelEdgeColorImage(ImagePlus impl){
        int h=impl.getHeight();
        int w=impl.getWidth();
        int[] R=new int[w*h];
        int[] G=new int[w*h];
        int[] B=new int[w*h];
        getRGB(impl, R, G, B);
        SobelFilter(R,w,h);
        SobelFilter(G,w,h);
        SobelFilter(B,w,h);
        int[] pixels=(int[])impl.getProcessor().getPixels();
        rgbTOint(pixels,R,G,B);
        impl.setTitle("SobelEdge");
//        impl.show();
    }
    public static void SobelFilter(int[] pixels, int w, int h){
        int i,j,i0,j0;
        int[] p1=new int[(h+2)*(w+2)];
        for(i=0;i<h+2;i++){
            i0=i-1;
            if(i0<0) i0=0;
            if(i0>=h) i0=h-1;
            for(j=0;j<w+2;j++){
                j0=j-1;
                if(j0<0)j0=0;
                if(j0>=w)j0=w-1;
                p1[i*w+j]=pixels[i0*w+j0];
            }
        }
        int a1,a2,a3,a4,a5,a6,a7,a8,a9;
        int X,Y;
        int max=0;
        for(i=1;i<=h;i++){
            i0=i-1;
            for(j=1;j<=w;j++){
                j0=j-1;
                a1=p1[(i-1)*w+j-1];
                a2=p1[(i-1)*w+j];
                a3=p1[(i-1)*w+j+1];
                a4=p1[i*w+j-1];
                a5=p1[i*w+j];
                a6=p1[i*w+j+1];
                a7=p1[(i+1)*w+j-1];
                a8=p1[(i+1)*w+j];
                a9=p1[(i+1)*w+j+1];
                X=2*(a6-a4)+a3+a9-a1-a7;
                Y=2*(a2-a8)+a1+a3-a7-a9;
                a5=(int)Math.sqrt(X*X+Y*Y);
                if(a5>max) max=a5;
                if(a5>255) a5=255;
                pixels[i0*w+j0]=a5;
            }
        }
/*        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                X=pixels[i*w+j];
                pixels[i*w+j]=(int)(255.*((double)X/255.)+0.5);
            }
        }*/
    }
    
    public static void pixelGradient_Gray8_Sobel(byte[] p, int w, int h, double[][] gX, double[][] gY){
        //the direction of gX and gY are pointing to right and up, respectively.
        int i,j,i0,j0,i1,j1;        
        int a1,a2,a3,a4,a5,a6,a7,a8,a9;
        int X,Y,o;
        int max=0;
        for(i=0;i<h;i++){
            i0=i-1;
            if(i0<0)i0=0;
            i1=i+1;
            if(i1>=h)i1=h-1;
            if(i==17){
                i=i;
            }
            for(j=0;j<w;j++){
                j0=j-1;
                if(j0<0)j0=0;
                j1=j+1;
                if(j1>=w)j1=w-1;
                a1=p[i0*w+j0];
                a2=p[i0*w+j];
                a3=p[i0*w+j1];
                a4=p[i*w+j0];
                a5=p[i*w+j];
                a6=p[i*w+j1];
                a7=p[i1*w+j0];
                a8=p[i1*w+j];
                a9=p[i1*w+j1];
                X=2*(a6-a4)+a3+a9-a1-a7;
                Y=2*(a2-a8)+a1+a3-a7-a9;
                gX[i][j]=X;
                gY[i][j]=Y;
//                gX[i][j]=a6-a5;
//                gY[i][j]=a2-a5;
            }
        }
    }
    public static void pixelGradient_Gray8_NarrowLine(byte[] p, int w, int h, double[][] gX, double[][] gY){
        //the direction of gX and gY are pointing to right and down, respectively.
        //positive gX and gY indicates the pixel value decreasing from left to right and from top to bottom.
        int i,j,o;        
        int max=0;
        int[][] p0=new int[h][w];
        int n;
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                n=p[o+j];
                p0[i][j]=0xff&n;
            }
        }
        QuickFormatter qf=new QuickFormatter("C:\\Taihao\\Personal\\Bible_Study\\ZPC\\Service Programs\\08o26\\NarrowLineFilter.txt");
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                calNarrowLineGradient(p0, w, h, gX, gY, i, j,qf);
            }
        }
        qf.m_fm.close();
    }
    public static void calNarrowLineGradient(int[][] p, int w, int h, double[][] gX, double[][] gY, int i0, int j0, QuickFormatter qf){
        double gdYX[][]=new double[8][2];
        double gdMax=0,gd=0.,dist=0.,num,gdMin=2550.;
        int maxgd=0,mingd=0;
        int i,j,k,ws,x,x0,x1,y,y0,y1;
        double sq2=Math.sqrt(2.);
        double sq5=Math.sqrt(5.);
        double gdMean=0.;
        //up direction, direction 1
        ws=5;
        gd=0.;        
        dist=2.;
        num=0;
        if(i0==242){
            i0=i0;
        }
        for(k=-ws;k<=ws;k++){            
            x=j0+k;
            y=i0;
            if(x<0||x>=w)continue;
            if(y<1||y>=h-1)continue;
            x1=x;
            x0=x;
            y1=y-1;
            y0=y+1;
            gd+=(double)p[y1][x1]-(double)p[y0][x0];
            num++;
        }
//        gd/=num*dist;
        gd/=num;
        gdYX[0][0]=-gd;//y1<y0
        gdYX[0][1]=0;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=0;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=0;
        }
        gdMean+=gd;
        
        //direction 2;
        ws=2;
        gd=0.;  
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+2*k;
            y=i0-k;
            if(x<0||x>=w-2)continue;
            if(y<2||y>=h-1)continue;
            x1=x;
            x0=x+1;
            y1=y-1;
            y0=y+1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y-2;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y-1;
            y0=y+1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y-2;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
//        gd/=num*dist;
        gd/=num;
        gdYX[1][0]=-gd*2./dist;
        gdYX[1][1]=-gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=1;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=1;
        }
        gdMean+=gd;
        
        
        //direction 3;
        ws=3;
        gd=0.; 
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0-k;
            if(x<1||x>w-2)continue;
            if(y<1||y>=h-1)continue;
            x1=x-1;
            x0=x+1;
            y1=y-1;
            y0=y+1;
            dist=2.*sq2;
//            gd+=((double)p[y1][x1]-(double)p[y0][x0])/dist;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y-1;
            y0=y;
            dist=sq2;
//            gd+=((double)p[y1][x1]-(double)p[y0][x0])/sq2;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            num+=2;
        }
        gd/=num;
        gdYX[2][0]=-gd/sq2;
        gdYX[2][1]=-gd/sq2;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=2;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=2;
        }
        gdMean+=gd;

        //direction 4;
        ws=2;
        gd=0.; 
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0-2*k;
            if(x<1||x>=w-2)continue;
            if(y<2||y>=h)continue;
            x1=x-1;
            x0=x+1;
            y1=y-1;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y-1;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x-1;
            x0=x+1;
            y1=y-2;
            y0=y-1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y-2;
            y0=y-1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
//        gd/=num*dist;
        gd/=num;
        gdYX[3][0]=-gd/dist;
        gdYX[3][1]=-2.*gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=3;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=3;
        }
        gdMean+=gd;
        
        
        //direction 5;
        ws=3;
        gd=0.;       
        num=0;
        for(k=-ws;k<=ws;k++){
            x=j0;
            y=i0-k;
            if(x<1||x>=w-1)continue;
            if(y<0||y>=h)continue;
            num++;
            x1=x-1;
            x0=x+1;
            y1=y;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
        }
        dist=2.;
//        gd/=num*dist;
        gd/=num;
        gdYX[4][0]=0;
        gdYX[4][1]=-gd;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=4;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=4;
        }
        gdMean+=gd;
        
        //direction 6;
        ws=2;
        gd=0.; 
        num=0;
        
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0+2*k;
            if(x<1||x>=w-2)continue;
            if(y<0||y>=h-2)continue;
            x1=x-1;
            x0=x+1;
            y1=y+1;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y+1;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x-1;
            x0=x+1;
            y1=y+2;
            y0=y+1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y+2;
            y0=y+1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
//        gd/=num*dist;
        gd/=num;
        gdYX[5][0]=gd/dist;
        gdYX[5][1]=-2.*gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=5;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=5;
        }
        gdMean+=gd;
        
        
        //direction 7;
        ws=3;
        gd=0.;   
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0+k;
            if(x<1||x>=w-1)continue;
            if(y<1||y>=h-1)continue;
            
            x1=x-1;
            x0=x+1;
            y1=y+1;
            y0=y-1;
            dist=2.*sq2;
//            gd+=((double)p[y1][x1]-(double)p[y0][x0])/dist;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y+1;
            y0=y;
            dist=sq2;
//            gd+=((double)p[y1][x1]-(double)p[y0][x0])/dist;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            num+=2;
        }
        gd/=num;
        gdYX[6][0]=gd/sq2;
        gdYX[6][1]=-gd/sq2;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=6;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=6;
        }
        gdMean+=gd;
        
        
        //direction 8;
        num=0;
        ws=2;
        gd=0.;        
        for(k=-ws;k<ws;k++){
            x=j0+2*k;
            y=i0+k;
            if(x<0||x>=w-2)continue;
            if(y<1||y>=h-2)continue;

            x1=x;
            x0=x+1;
            y1=y+1;
            y0=y-1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y+2;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y+1;
            y0=y-1;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y+2;
            y0=y;
            gd+=((double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
//        gd/=num*dist;
        gd/=num;
        gdYX[7][0]=2.*gd/dist;
        gdYX[7][1]=-gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=7;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=7;
        }        
        gdMean+=gd;
        gdMean/=8.;

        
//        gX[i0][j0]=gdYX[maxgd][1]*gdMax/gdMean;
//        gY[i0][j0]=gdYX[maxgd][0]*gdMax/gdMean;
        
        gX[i0][j0]=-gdYX[maxgd][1];//the pixel values dectease along the direction of the gradient.
        gY[i0][j0]=-gdYX[maxgd][0];
        if(gdMax>5){
            qf.m_fm.format("y, x:  %4d %4d  maxgd: %2d gdMax:  %6.2f  gdX: %6.2f  gdY: %6.2f gdMean:  %6.2f gdMin:  %6.2f%s", i0, j0, maxgd,gdMax,gdYX[maxgd][0],gdYX[maxgd][1],gdMean,gdMin,Constants.newline); 
        }
    }
    
    public static void calNarrowLineGradient_Orientation(int[][] p, int w, int h, double[][] gX, double[][] gY, int i0, int j0){
        double gdYX[][]=new double[8][2];
        double gdMax=0,gd=0.,dist=0.,num,gdMin=2550.;
        int maxgd=0,mingd=0;
        int i,j,k,ws,x,x0,x1,y,y0,y1;
        double sq2=Math.sqrt(2.);
        double sq5=Math.sqrt(5.);
        double gdMean=0.;
        double unit=2.;
        //up direction, direction 1
        ws=3;
        gd=0.;        
        dist=2.;
        num=0;
        for(k=-ws;k<=ws;k++){            
            x=j0+k;
            y=i0;
            if(x<0||x>=w)continue;
            if(y<1||y>=h-1)continue;
            x1=x;
            x0=x;
            y1=y-1;
            y0=y+1;
            gd+=Math.copySign(unit,p[y1][x1]-p[y][x]);
            gd+=Math.copySign(unit,p[y][x]-p[y0][x0]);
            num++;
        }
        gd/=num;
        gdYX[0][0]=gd;
        gdYX[0][1]=0;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=0;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=0;
        }
        gdMean+=gd;
        
        //direction 2;
        ws=1;
        gd=0.;  
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+2*k;
            y=i0-k;
            if(x<0||x>=w-2)continue;
            if(y<2||y>=h-1)continue;
            x1=x;
            x0=x+1;
            y1=y-1;
            y0=y+1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y-2;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y-1;
            y0=y+1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y-2;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
        gd/=num;
        gdYX[1][0]=gd*2./dist;
        gdYX[1][1]=-gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=1;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=1;
        }
        gdMean+=gd;
        
        
        //direction 3;
        ws=2;
        gd=0.; 
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0-k;
            if(x<1||x>w-2)continue;
            if(y<1||y>=h-1)continue;
            x1=x-1;
            x0=x+1;
            y1=y-1;
            y0=y+1;
            dist=2.*sq2;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y][x]);
            gd+=Math.copySign(unit,(double)p[y][x]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y-1;
            y0=y;
            dist=sq2;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            num+=2;
        }
        gd/=num;
        gdYX[2][0]=gd/sq2;
        gdYX[2][1]=-gd/sq2;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=2;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=2;
        }
        gdMean+=gd;

        //direction 4;
        ws=1;
        gd=0.; 
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0-2*k;
            if(x<1||x>=w-2)continue;
            if(y<2||y>=h)continue;
            x1=x-1;
            x0=x+1;
            y1=y-1;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y-1;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x-1;
            x0=x+1;
            y1=y-2;
            y0=y-1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y-2;
            y0=y-1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
        gd/=num;
        gdYX[3][0]=gd/dist;
        gdYX[3][1]=-2.*gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=3;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=3;
        }
        gdMean+=gd;
        
        
        //direction 5;
        ws=3;
        gd=0.;       
        num=0;
        for(k=-ws;k<=ws;k++){
            x=j0;
            y=i0-k;
            if(x<1||x>=w-1)continue;
            if(y<0||y>=h)continue;
            num++;
            x1=x-1;
            x0=x+1;
            y1=y;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y][x]);
            gd+=Math.copySign(unit,(double)p[y][x]-(double)p[y0][x0]);
        }
        dist=2.;
        gd/=num;
        gdYX[4][0]=0;
        gdYX[4][1]=-gd;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=4;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=4;
        }
        gdMean+=gd;
        
        //direction 6;
        ws=1;
        gd=0.; 
        num=0;
        
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0+2*k;
            if(x<1||x>=w-2)continue;
            if(y<0||y>=h-2)continue;
            x1=x-1;
            x0=x+1;
            y1=y+1;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y+1;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x-1;
            x0=x+1;
            y1=y+2;
            y0=y+1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+2;
            y1=y+2;
            y0=y+1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
        gd/=num;
        gdYX[5][0]=-gd/dist;
        gdYX[5][1]=-2.*gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=5;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=5;
        }
        gdMean+=gd;
        
        
        //direction 7;
        ws=2;
        gd=0.;   
        num=0;
        for(k=-ws;k<ws;k++){
            x=j0+k;
            y=i0+k;
            if(x<1||x>=w-1)continue;
            if(y<1||y>=h-1)continue;
            
            x1=x-1;
            x0=x+1;
            y1=y+1;
            y0=y-1;
            dist=2.*sq2;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y][x]);
            gd+=Math.copySign(unit,(double)p[y][x]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y+1;
            y0=y;
            dist=sq2;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            num+=2;
        }
        gd/=num;
        gdYX[6][0]=-gd/sq2;
        gdYX[6][1]=-gd/sq2;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=6;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=6;
        }
        gdMean+=gd;
        
        
        //direction 8;
        num=0;
        ws=1;
        gd=0.;        
        for(k=-ws;k<ws;k++){
            x=j0+2*k;
            y=i0+k;
            if(x<0||x>=w-2)continue;
            if(y<1||y>=h-2)continue;

            x1=x;
            x0=x+1;
            y1=y+1;
            y0=y-1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x;
            x0=x+1;
            y1=y+2;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y+1;
            y0=y-1;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            
            x1=x+1;
            x0=x+2;
            y1=y+2;
            y0=y;
            gd+=Math.copySign(unit,(double)p[y1][x1]-(double)p[y0][x0]);
            num+=4;
        }
        dist=sq5;
        gd/=num;
        gdYX[7][0]=-2.*gd/dist;
        gdYX[7][1]=-gd/dist;
        gd=Math.abs(gd);
        if(gd>gdMax){
            gdMax=gd;
            maxgd=7;
        }
        if(gd<gdMin){
            gdMin=gd;
            mingd=7;
        }        
        gdMean+=gd;
        gdMean/=8.;

        
//        gX[i0][j0]=gdYX[maxgd][1]*gdMax/gdMean;
//        gY[i0][j0]=gdYX[maxgd][0]*gdMax/gdMean;
        gX[i0][j0]=gdYX[maxgd][1]*gdMax;
        gY[i0][j0]=gdYX[maxgd][0]*gdMax;
    }
    
    
    public static Color randomColor(){
        int r,g,b;
        r=(int)(Math.random()*255+0.5);
        g=(int)(Math.random()*255+0.5);
        b=(int)(Math.random()*255+0.5);
        Color color=new Color(r,g,b);
        return color;
    }
    public static void labelGrphicalObjects(ImagePlus impl, ArrayList <BrainSimulationGraphicalObject> GOs){
        int i,j,x,y;
        Font font=new Font("Arial", Font.BOLD, 18);
        Color color=Color.pink;
//        impl.getProcessor().setFont(font);
        ArrayList <LabelNode> labels= LabelHandler.makeLabels(impl,GOs);
         LabelHandler.arrangeLabels(labels, impl.getWidth(), impl.getHeight());
        LabelNode label;
        int size=labels.size();        
        for(i=0;i<size;i++){
            label=labels.get(i);
            color=label.color;
            font=label.font;
            impl.getProcessor().setColor(color);
            y=label.corner[0];
            x=label.corner[1];
//            drawRectangle(impl,x,y, 20, 2,color);
            x+=label.shift[1]+label.minShift[1];
            y+=label.shift[0]+label.minShift[0];
//            impl.getProcessor().drawString(label.label,x,y);
        }
        size=GOs.size();
        ArrayList<Point2D> MECPolygon;
        BrainSimulationGraphicalObject go;
        int size1=GOs.size();
        Point2D p1, p2;
        for(i=1;i<size1;i++){
            go=GOs.get(i);
            if(go.isHiden()) continue;
            go.makeMECPolygon();
            if(go.getMECPolygon().size()<4) continue;
            go.makeMECRectangle();
//            go.drawMECPolygon(impl);
//            go.drawMECRactangle(impl);
           go.calCurvature(); 
           go.drawContour(impl,true);
           ArrayList <Color> colors=new ArrayList<Color>();
           colors.add(Color.RED);
           colors.add(Color.GREEN);
           colors.add(Color.BLUE);
           colors.add(Color.CYAN);
           colors.add(Color.ORANGE);
//           go.drawEnclosedObjects(impl, colors);
/*            
            MECPolygon=go.getMECPolygon();
//            color=go.getColor();
//            impl.getProcessor().setColor(color);
            size1=MECPolygon.size();
            impl.getProcessor().setColor(randomColor());
            for(j=0;j<size1;j++){
                p1=MECPolygon.get(j);
                p2=MECPolygon.get((j+1)%size1);
                impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
            }
            impl.getProcessor().setColor(randomColor());
            ConvexPolygon cpg=new ConvexPolygon(MECPolygon);
            ArrayList <Point2D> MAERectangle=cpg.getMAERectangle();
            for(j=0;j<4;j++){            
                p1=MAERectangle.get(j);
                p2=MAERectangle.get((j+1)%4);
                impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
            }
            */
        }
    }
    public static void labelGrphicalObjects1(ImagePlus impl, ArrayList <BrainSimulationGraphicalObject> GOs){
        int i,j,x,y;
        Font font=new Font("Arial", Font.BOLD, 18);
        Color color=Color.pink;
//        impl.getProcessor().setFont(font);
        ArrayList <LabelNode> labels=LabelHandler.makeLabels(impl,GOs);
        LabelHandler.arrangeLabels(labels, impl.getWidth(), impl.getHeight());
        LabelNode label;
        int size=labels.size();        
        for(i=0;i<size;i++){
            label=labels.get(i);
            color=label.color;
            font=label.font;
            impl.getProcessor().setColor(color);
            y=label.corner[0];
            x=label.corner[1];
            drawRectangle(impl,x,y, 20, 2,color);
            x+=label.shift[1]+label.minShift[1];
            y+=label.shift[0]+label.minShift[0];
            impl.getProcessor().drawString(label.label,x,y);
        }
        size=GOs.size();
        ArrayList<Point2D> MECPolygon;
        BrainSimulationGraphicalObject go;
        int size1=GOs.size();
        Point2D p1, p2;
        for(i=1;i<0;i++){
            go=GOs.get(i);
//            if(go.isHiden()) continue;
            go.makeMECPolygon();
            if(go.getMECPolygon().size()<4) continue;
            go.makeMECRectangle();
//            go.drawMECPolygon(impl);
 //           go.drawMECRactangle(impl);
           go.drawContour(impl,false);
           ArrayList <Color> colors=new ArrayList<Color>();
           colors.add(Color.RED);
           colors.add(Color.GREEN);
           colors.add(Color.BLUE);
           colors.add(Color.CYAN);
           colors.add(Color.ORANGE);
//           go.drawEnclosedObjects(impl, colors);
/*            
            MECPolygon=go.getMECPolygon();
//            color=go.getColor();
//            impl.getProcessor().setColor(color);
            size1=MECPolygon.size();
            impl.getProcessor().setColor(randomColor());
            for(j=0;j<size1;j++){
                p1=MECPolygon.get(j);
                p2=MECPolygon.get((j+1)%size1);
                impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
            }
            impl.getProcessor().setColor(randomColor());
            ConvexPolygon cpg=new ConvexPolygon(MECPolygon);
            ArrayList <Point2D> MAERectangle=cpg.getMAERectangle();
            for(j=0;j<4;j++){            
                p1=MAERectangle.get(j);
                p2=MAERectangle.get((j+1)%4);
                impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
            }
            */
        }
    }
    public static int complementaryRGB(int pixel){        
        int r=0,g=1,b=2;
        r=0xff & (pixel>>16);
        g=0xff & (pixel>>8);
        b=0xff & pixel;
        r=255-r;
        g=255-g;
        b=255-b;
        return (r<<16)|(g<<8)|b;
    }
    
    public static void drawRectangle(ImagePlus impl, int x, int y, int w, int h){
        drawRectangle (impl,x,y,w,h,null);
    }
    
    public static void drawRectangle(ImagePlus impl, int x, int y, int w, int h, Color color){
        int height=impl.getHeight(), width=impl.getWidth();
        int pixels[]=(int[]) impl.getProcessor().getPixels();
        int yMin=y,yMax=y+h,xMin=x,xMax=x+w;
        if(yMax>=height) y=height-1;
        if(xMax>=width) x=width-1;
        if(xMin<0) xMin=0;
        if(yMin<0) yMin=0;
        int offset=0;
        int pixel;
        for(y=yMin;y<=yMax;y++){
            offset=y*width;
            for(x=xMin;x<=xMax;x++){
                if(color==null) pixels[offset+x]=complementaryRGB(pixels[offset+x]);
                else pixels[offset+x]=color.getRGB();
            }
        }
    }


    public static ImagePlus newPlainRGBImage(String title,int w, int h, Color c){
        int pixels[]=new int[w*h];
        int pixel=c.getRGB();
        for(int i=0;i<w*h;i++){
            pixels[i]=pixel;
        }
        ColorProcessor cp=new ColorProcessor(w,h,pixels);
        ImagePlus impl=new ImagePlus(title, cp);
        return impl;
    }

    public static ImagePlus newGary8Image(String title,int w, int h){
        byte pixels[]=new byte[w*h];
        for(int i=0;i<w*h;i++){
            pixels[i]=0;
        }
        ByteProcessor cp=new ByteProcessor(w,h);
        ImagePlus impl=new ImagePlus(title, cp);
        return impl;
    }
    public static int randomRGB(){
        int rgb[]=new int[3];
        rgb[0]=(int)(Math.random()*255+0.5);
        rgb[1]=(int)(Math.random()*255+0.5);
        rgb[2]=(int)(Math.random()*255+0.5);
        int pixel=CommonMethods.rgbTOint(rgb);
        return pixel;
    }
    public static ArrayList <ImagePlus> getAllOpenImages(){        
        ArrayList <ImagePlus> impls;
        impls=new ArrayList <ImagePlus>();
        int ids[]=WindowManager.getIDList();
        if(ids==null) return impls;
        int len=ids.length;
        int id;
        ImagePlus impl;
        for(int i=0;i<len;i++)
        {
            id=ids[i];
            impl=(ImagePlus)WindowManager.getImage(id);
            impls.add(impl);
        }
        return impls;
    }
     public static void convertToGray8(ImagePlus impl){
        ImageConverter ic=new ImageConverter(impl);
        ic.convertToGray8();
     }
    
    public static double getDistanceToLine(Point P1, Point P2, Point P3){
        return getDistanceToLine(new Point2D(P1.x,P1.y),new Point2D(P2.x,P2.y),new Point2D(P3.x,P3.y));
    }
    
    public static double getDistanceToLine(DoublePair P1, DoublePair P2, DoublePair P3){
        return getDistanceToLine(new Point2D(P1.left,P1.right),new Point2D(P2.left,P2.right),new Point2D(P3.left,P3.right));
    }
    
    public static void reflect(DoublePair p, DoublePair p1){
        double dx=p1.left-p.left,dy=p1.right-p.right;
        p1.setValue(p.left-dx,p.right-dy);
    }
    public static double getDistance(DoublePair p1, DoublePair p2){
        return getDistance(p1.left,p1.right,p2.left,p2.right);
    }
    public static double getDistanceToLine(Point2D P1, Point2D P2, Point2D P3){
        //returns the distance from p3 to the line defined by p1 and p2. The dist will be positive if 
        //p3 is on the right side of the line when one is walking from p1 to p2, otherwise it will be negative.
        double dist;
        if(!P1.different(P3)||!P2.different(P3)||!P1.different(P2)) {
            return 0;
        }
        Point2D P0=getClosestPointOnLine(P1,P2,P3);
        double x0=P0.dx,y0=P0.dy,x3=P3.dx,y3=P3.dy;
        dist=Math.sqrt((x3-x0)*(x3-x0)+(y3-y0)*(y3-y0));
        double sign;
/*        if(P1.dy!=P2.dy) 
            sign=-(P2.dy-P1.dy)*(P3.x-x0);
        else
            sign=(P2.dx-P1.dx)*(P3.dy-y0);*/
        dist=Math.copySign(dist, (P2.dx-P1.dx)*(P3.dy-y0)-(P2.dy-P1.dy)*(P3.dx-x0));//13108
        return dist;
    }
    public static Point2D getClosestPointOnLine(Point2D P1, Point2D P2, Point2D P3){
        //returns point P0, the point on the line P1, P2 that is closest to P3
        double x1=P1.dx,x2=P2.dx,x3=P3.dx,y1=P1.dy,y2=P2.dy,y3=P3.dy;
        double k=(y2-y1)/(x2-x1), k1=-1./k;
        if(verticalLine(P1,P2)){
            return new Point2D(P1.dx,P3.dy);
        }
        if(horizontalLine(P1,P2)){
            return new Point2D(P3.dx,P1.dy);
        }
        double b=(y1*(x2-x1)-x1*(y2-y1))/(x2-x1),b1=y3+x3/k;
        double x0=(b1-b)/(k-k1);
        double y0=k*x0+b;
        Point2D p=new Point2D(x0,y0);
        return p;
    }
    public static double getDistanceToLine(double x1, double y1, double x2, double y2, double x3, double y3){
        //the distance from (x3,y3) to the line determined by the points (x1,y1) and (x2,y2)
        DoublePair dp=getClosestPointOnLine(x1,y1,x2,y2,x3,y3);
        return getDistance(x3, y3,dp.left,dp.right);
    }
    public static double getDistance(double x1, double y1, double x2, double y2){
        double dx=x1-x2,dy=y1-y2;
        return Math.sqrt(dx*dx+dy*dy);
    }
    public static DoublePair getClosestPointOnLine(double x1,double y1, double x2,double y2, double x3, double y3){
        //returns point P0, the point on the line P1 (x1,y1), P2 (x2,y2) that is closest to P3 (x3,y3)
        DoublePair dp;
        if(x1==x2){
            return new DoublePair(x1,y3);
        }
        if(y1==y2){
            return new DoublePair(x3,y1);
        }
        double k=(y2-y1)/(x2-x1), k1=-1./k;
        double b=(y1*(x2-x1)-x1*(y2-y1))/(x2-x1),b1=y3+x3/k;
        double x0=(b1-b)/(k-k1);
        double y0=k*x0+b;
        dp=new DoublePair(x0,y0);
        return dp;
    }
    public static Point2D getClosestPointOnLine(Point2D P1, double k, Point2D P3){
        //return the closest point on the line (going through point P1 with slope k) to the point P3.
        double x1=P1.x,x3=P3.x,y1=P1.y,y3=P3.y;
        double k1=-1./k;
        double b=y1-k*x1,b1=y3-k1*x3;
        double x0=(b1-b)/(k-k1);
        double y0=k*x0+b;
        Point2D p=new Point2D(x0,y0);
        return p;
    }
    public static boolean horizontalLine(Point2D P1, Point2D P2){
        return (P1.dy==P2.dy);
    }
    public static boolean verticalLine(Point2D P1, Point2D P2){
        return (P1.dx==P2.dx);
    }
    public static double getAngle(Point2D P1, Point2D P2, Point2D P3){
        //return the angle between P1-P2 and P3-P2.
        //The angle will be negative if P2 is on the left side of P3-P1 (walking
        //from P1 to P3).
        double s32=(P2.dx-P1.dx)*(P2.dx-P1.dx)+(P2.dy-P1.dy)*(P2.dy-P1.dy);
        double s22=(P3.dx-P1.dx)*(P3.dx-P1.dx)+(P3.dy-P1.dy)*(P3.dy-P1.dy);
        double s12=(P3.dx-P2.dx)*(P3.dx-P2.dx)+(P3.dy-P2.dy)*(P3.dy-P2.dy);
        double ca=-0.5*(s22-s12-s32)/Math.sqrt(s12*s32);
        ca=Math.acos(ca);

        Point2D P0=CommonMethods.getClosestPointOnLine(P1, P3, P2);
            double sign;
        if(P1.y!=P3.y)
            sign=Math.copySign(1.,-(P3.dy-P1.dy)*(P2.dx-P0.dx));
        else
            sign=Math.copySign(1.,(P3.dx-P1.dx)*(P2.dy-P0.dy));
        return -sign*Angles.degree(ca);
    }
    public static double getDistance(Point2D P1, Point2D P2){
        return Math.sqrt((P2.dx-P1.dx)*(P2.dx-P1.dx)+(P2.dy-P1.dy)*(P2.dy-P1.dy));
    }
    public static double getDist2(double x1, double y1, double x2, double y2){
        return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
    }
    public static void displayAsImage(String title, int w, int h, byte[][] pixels){
        int[] p=new int[w*h];
        int i,j,offset;
        for(i=0;i<h;i++){
            offset=i*w;
            for(j=0;j<w;j++){
                p[offset+j]=pixels[i][j];
            }
        }
        ImagePlus impl=getRGBImage(title,w,h,p);
        impl.show();
    }
    public static void displayAsImage(final String title, final int w, final int h, final int[][] pixels, final int type){
        java.awt.EventQueue.invokeLater(new Runnable(){
                public void run(){
                ImagePlus impl=CommonMethods.getBlankImage(type, w, h);
                impl.setTitle(title);
                CommonMethods.setPixels(impl, pixels,type==ImagePlus.COLOR_RGB);
                CommonGuiMethods.showTempImage(impl);
            }
        });
    }
    public static ImagePlus getCurrentImage(){
        return WindowManager.getCurrentImage();        
    }
    public static Color RBSpectrumColor(double c, double cMin, double cMid, double cMax){
        int pixel=0;
        int r,g=0,b=0;
        if(c<cMid){
            g=255;
            r=255-(int)(255.*(cMid-c)/(cMid-cMin));
            pixel=(r<<16)|(g<<8)|b;
        }else{            
            r=255;
            g=255-(int)(255.*(c-cMid)/(cMax-cMid));
            pixel=(r<<16)|(g<<8)|b;
        }
        return new Color(pixel);
    }
    public static ArrayList <Integer> getMinimumVertexPolygon(ArrayList <Point2D> v){
        ArrayList <Integer> v1=new ArrayList <Integer>();
        Stack <Integer> rs=new Stack <Integer>();
        Stack <Integer> sv=new Stack <Integer>();
        
        double h;
        int i,size=v.size();
        int p1=0,p2=size-1,mh1=size,mh2=-1;
        int maxH=size-1;
        rs.push(p2);
        p2=0;
        Point2D p;
        
        while(!rs.empty()){
            p1=p2;
            p2=rs.pop();
            maxH=getMaxOutliner(v,p1,p2);
            if(maxH>=0){
                if(maxH<mh1)mh1=maxH;
                if(maxH>mh2)mh2=maxH;
                rs.push(p2);
                rs.push(maxH);
                p2=maxH;
                mh2=maxH;
                p2=p1;
            }else{                
//                p=v.get(p1);
                v1.add(p1);
            }
        }        
        if(getDistanceToLine(v.get(0),v.get(mh2),v.get(size-1))>1) v1.add(size-1);
//        if(getDistanceToLine(v.get(mh1),v.get(mh2),v.get(0))>1) v1.add(v.get(0));
        return v1;
    }
    public static int getMaxOutliner(ArrayList <Point2D> v, int p1, int p2){
        Point2D P1=v.get(p1),P2=v.get(p2),P3=null;
        double hMax=-1.,h=0;
        int maxH=-1;
        for(int i=p1;i<p2;i++){
            P3=v.get(i);
            h=Math.abs(getDistanceToLine(P1,P2,P3));
            if(h>hMax){
                hMax=h;
                maxH=i;
            }
        }
        if(hMax<1) maxH=-1;
        return maxH;
    }
    
    static public void randomize(byte[] bytes){
        int N = bytes.length;
        int r,i;
        byte t;
        for(i=0;i<N;i++){
            r = i + (int) (Math.random() * (N-i));   // between i and N-1
            t=bytes[i];
            bytes[i]=bytes[r];
            bytes[r]=t;
        }
    }
    static public void randomize(int[] ints){
        int N = ints.length;
        int r,i;
        int t;
        for(i=0;i<N;i++){
            r = i + (int) (Math.random() * (N-i));   // between i and N-1
            t=ints[i];
            ints[i]=ints[r];
            ints[r]=t;
        }
    }
    static public void randomize(int[][] pixels){
        int h=pixels.length,w=pixels[0].length;
        int N = w*h;
        int r,i,i0,j0,it,jt;
        int t;
        for(i=0;i<N;i++){
            r = i + (int) (Math.random() * (N-i));   // between i and N-1
            i0=i/w;
            j0=i%w;
            it=r/w;
            jt=r%w;
            t=pixels[i0][j0];
            pixels[i0][j0]=pixels[it][jt];
            pixels[it][jt]=t;
        }
    }
    static public MeanSem getMeanSem (IntArray ir){
        MeanSem ms=new MeanSem();
        double sqs=0.,mean=0.,dev=0.,sem=0.;
        int n=ir.m_intArray.size();
        int i,j,p;
        for(i=0;i<n;i++){
            p=ir.m_intArray.get(i);
            mean+=p;
            sqs+=p*p;
        }
        mean/=n;
        dev=Math.sqrt((sqs-n*mean*mean)/n);
        if(n>1) sem=dev/Math.sqrt(n-1);
        ms.mean=mean;
        ms.sem=sem;
        ms.size=n;
        ms.dev=dev;
        return ms;
    }
    static public void collectROI(byte[]pixels, int w, int h, Roi ROI, IntArray ir){
        Rectangle br=ROI.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height;
        int x,y,i,j,o;

        for(i=0;i<rh;i++){
            y=yo+i;
            o=w*y;
            for(j=0;j<rw;j++){
                x=xo+j;
                if(ROI.contains(x, y)){
                        ir.m_intArray.add(0xff&pixels[o+x]);
                }
            }
        }
    }
    static public void collectROIs(byte[]pixels, int w, int h, ArrayList<Roi> ROIs, IntArray ir){
        int size=ROIs.size();
        Roi ROI;
        for(int rn=0;rn<size;rn++){
            ROI=ROIs.get(rn);
            Rectangle br=ROI.getBounds();
            int xo=br.x,yo=br.y,rw=br.width,rh=br.height;
            int x,y,i,j,o;
            for(i=0;i<rh;i++){
                y=yo+i;
                o=w*y;
                for(j=0;j<rw;j++){
                    x=xo+j;
                    if(ROI.contains(x, y)){
                            ir.m_intArray.add(0xff&pixels[o+x]);
                    }
                }
            }
        }
    }
    static public void collectROIs_Sorted(byte[]pixels, int w, int h, ArrayList<Roi> ROIs, IntArray ir){
        collectROIs(pixels, w, h, ROIs, ir);
        QuickSortInteger.quicksort(ir);
    }
    static public void collectROIs(int[][]pixels, int w, int h, ArrayList<Roi> ROIs, IntArray ir){
        int size=ROIs.size();
        Roi ROI;
        for(int rn=0;rn<size;rn++){
            ROI=ROIs.get(rn);
            Rectangle br=ROI.getBounds();
            int xo=br.x,yo=br.y,rw=br.width,rh=br.height;
            int x,y,i,j,o;
            for(i=0;i<rh;i++){
                y=yo+i;
                for(j=0;j<rw;j++){
                    x=xo+j;
                    if(ROI.contains(x, y)){
                            ir.m_intArray.add(pixels[y][x]);
                    }
                }
            }
        }
    }
    static public void collectROIs_Sorted(int[][]pixels, int w, int h, ArrayList<Roi> ROIs, IntArray ir){
        collectROIs(pixels, w, h, ROIs, ir);
        QuickSortInteger.quicksort(ir);
    }

    static public int LocalExtrema(double[] pData, ArrayList<Integer> lMinima, ArrayList<Integer> lMaxima)
    {
        double x0,x;
        short nS0,nS;
        int i,len=pData.length-1;
        if(pData.length<1) return -1;
        nS0=0;
        nS=0;
        x0=pData[0];
        lMinima.clear();
        lMaxima.clear();
        for (i=0;i<=len;i++)
        {
            if(i==len)x=x0-1;
            else x=pData[i];//inssure that there is at least a single local maximum, and the last local maximum appears after the last local minimum.
            if(i==0)x0=x-1.;//inssure that there is at least a single local maximum, and the first local maximum appears before the first local minimum.
            x=pData[i];
            if(x>x0)
                nS=1;
            else if (x<x0)
                nS=-1;
            else nS=0;

            if(nS0*nS<0)
            {
                if(nS<0)
                {
                    lMaxima.add(i-1);
                }
                else
                {
                    lMinima.add(i-1);

                }
            }
            if(nS!=0) nS0=nS;
            x0=x;
        }
        return 1;
    }
    static public int LocalExtrema(float[] pData, ArrayList<Integer> lMinima, ArrayList<Integer> lMaxima)
    {
        if(pData.length<1) return -1;
        double x0,x;
        short nS0,nS;
        int i,len=pData.length-1;
        nS0=0;
        nS=0;
        x0=pData[0];
        lMinima.clear();
        lMaxima.clear();
        for (i=0;i<=len;i++)
        {
            if(i==len)x=x0-1;
            else x=pData[i];//inssure that there is at least a single local maximum, and the last local maximum appears after the last local minimum.
            if(i==0)x0=x-1.;//inssure that there is at least a single local maximum, and the first local maximum appears before the first local minimum.
            x=pData[i];
            if(x>x0)
                nS=1;
            else if (x<x0)
                nS=-1;
            else nS=0;

            if(nS0*nS<0)
            {
                if(nS<0)
                {
                    lMaxima.add(i-1);
                }
                else
                {
                    lMinima.add(i-1);

                }
            }
            if(nS!=0) nS0=nS;
            x0=x;
        }
        return 1;
    }
    static public int LocalExtrema(double[] pData, int iI, int iF, ArrayList<Integer> lMinima, ArrayList<Integer> lMaxima)
    {
        if(pData.length<1) return -1;
        double x0,x;
        short nS0,nS;
        lMinima.clear();
        lMaxima.clear();
        int i;
        nS0=0;
        nS=0;
        x0=pData[iI];
        x=x0;
        for (i=iI;i<=iF;i++)
        {
            if(i==iF)x=x0-1;
            else x=pData[i];//inssure that there is at least a single local maximum, and the last local maximum appears after the last local minimum.
            if(i==iI)x0=x-1.;//inssure that there is at least a single local maximum, and the first local maximum appears before the first local minimum.
//            x=pData[i];
            if(x>x0)
                nS=1;
            else if (x<x0)
                nS=-1;
            else nS=0;

            if(nS0*nS<0)
            {
                if(nS<0)
                {
                    lMaxima.add(i-1);
                }
                else
                {
                    lMinima.add(i-1);

                }
            }
            if(nS!=0) nS0=nS;
            x0=x;
        }
        return 1;
    }

    static public int LocalExtrema(ArrayList<Double> data, ArrayList<Integer> lMinima, ArrayList<Integer> lMaxima)
    {
        if(data.size()<1) return -1;
        double x0,x;
        short nS0,nS;
        lMinima.clear();
        lMaxima.clear();
        int i;
        nS0=0;
        nS=0;
        x0=data.get(0);
        int len=data.size();
        for (i=0;i<=len;i++)
        {
            if(i==len)x=x0-1;
            else x=data.get(i);//inssure that there is at least a single local maximum, and the last local maximum appears after the last local minimum.
            if(i==0)x0=x-1.;//inssure that there is at least a single local maximum, and the first local maximum appears before the first local minimum.
            if(x>x0)
                nS=1;
            else if (x<x0)
                nS=-1;
            else nS=0;

            if(nS0*nS<0)
            {
                if(nS<0)
                {
                    lMaxima.add(i-1);
                }
                else
                {
                    lMinima.add(i-1);

                }
            }
            if(nS!=0) nS0=nS;
            x0=x;
        }
        return 1;
    }
    public static int LocalExtrema(ArrayList<Double> data, ArrayList<Double> lMinima, ArrayList<Double> lMaxima,int l)
    {
        if(data.size()<1) return -1;
        double x0,x;
        short nS0,nS;
        lMinima.clear();
        lMaxima.clear();
        int i;
        nS0=0;
        nS=0;
        x0=data.get(0);
        int len=data.size();
        for (i=0;i<=len;i++)
        {
            if(i==len)x=x0-1;
            else x=data.get(i);//inssure that there is at least a single local maximum, and the last local maximum appears after the last local minimum.
            if(i==0)x0=x-1.;//inssure that there is at least a single local maximum, and the first local maximum appears before the first local minimum.
            if(x>x0)
                nS=1;
            else if (x<x0)
                nS=-1;
            else nS=0;

            if(nS0*nS<0)
            {
                if(nS<0)
                {
                    lMaxima.add(data.get(i-1));
                }
                else
                {
                    lMinima.add(data.get(i-1));
                }
            }
            if(nS!=0) nS0=nS;
            x0=x;
        }
        return 1;
    }
    static public void collectROIs(int[][]pixels, int w, int h, ArrayList<Roi> ROIs, ArrayList<Integer> ir){
        int size=ROIs.size();
        Roi ROI;
        for(int rn=0;rn<size;rn++){
            ROI=ROIs.get(rn);
            Rectangle br=ROI.getBounds();
            int xo=br.x,yo=br.y,rw=br.width,rh=br.height;
            int x,y,i,j,o;
            for(i=0;i<rh;i++){
                y=yo+i;
                for(j=0;j<rw;j++){
                    x=xo+j;
                    if(ROI.contains(x, y)){
                            ir.add(pixels[y][x]);
                    }
                }
            }
        }
    }
    static public void collectROIs_Sorted(int[][]pixels, int w, int h, ArrayList<Roi> ROIs, ArrayList<Integer> ir){
        collectROIs(pixels, w, h, ROIs, ir);
        QuickSortInteger.quicksort(ir);
    }
    static public ArrayList<Double> runningWindowAverage(ArrayList <Double> data, int ws){//the returned array is 2*ws shorter than the original data.
        //Alternative could be filling the first ws and last ws elements with one of the actual running window average, but it could be problematic for
        //statistics.
        ArrayList<Double> rwa=new ArrayList<Double>();
        int len=2*ws+1;
        int i;
        double ave=0.,x0,x1;
        for(i=0;i<len;i++){
            ave+=data.get(i);
        }
        int size=data.size();
        rwa.add(ave/len);
        for(i=ws+1;i<size-ws;i++){
            ave+=data.get(i+ws);
            ave-=data.get(i-ws-1);
            rwa.add(ave/len);
        }
        return rwa;
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
    static public intRange positionRange(ArrayList<Integer> ascendingIr,int value){
        int size=ascendingIr.size();
        int vx=ascendingIr.get(size-1),vn=ascendingIr.get(0),v;

        if(value<vn)return new intRange(-1,-1);
        if(value>vx) return new intRange(size,size);
        if(value==vn)return new intRange(0,0);
        if(value==vx) return new intRange(size-1,size-1);

        int lo=0,hi=size-1,m;
        m=(lo+hi)/2;
        v=ascendingIr.get(m);
        while(hi>m&&lo<m){
            if(v==value) {
                return new intRange(m,m);
            }else if(v>value){
                hi=m;
            }else{
                lo=m;
            }
            m=(lo+hi)/2;
            v=ascendingIr.get(m);
        }
        return new intRange(lo,hi);
    }
    static public double getLinearIntoplation(double x1, double y1, double x2, double y2, double x){
        if(x1==x2) return y1;
        return y1+(y2-y1)*(x-x1)/(x2-x1);
    }
    static public double getMean(ArrayList <Double> data,int i0, int i1){
        double mean=0;
        for(int i=i0;i<=i1;i++){
            mean+=data.get(i);
        }
        return mean/(i1-i0+1);
    }
    static public ImagePlus meanFilteringGray8(ImagePlus impl, int radius){
        byte pixels[];
        int w=impl.getWidth();
        int h=impl.getHeight();
        int ns=impl.getStackSize();
        for(int i=0;i<ns;i++){
            impl.setSlice(i+1);
            pixels=(byte[])impl.getProcessor().getPixels();
            meanFiltering(w,h,pixels,radius);
        }
        return impl;
    }
    static public void meanFiltering(int w, int h, byte[] pixels, int radius){
        int len=h*w;
        double[][] lineMean=new double[h][w],blockMean=new double[h][w];
        double p,mean,p0,p1;
        int lSize=2*radius+1;
        int i,j,o;
        for(i=0;i<h;i++){
            o=i*w;
            p0=0xff&pixels[o];
            p1=0xff&pixels[o+w-1];
            mean=(radius+1)*p0;
            for(j=0;j<radius;j++){
                mean+=0xff&pixels[o+j];
            }
            for(j=0;j<=radius;j++){
                mean+=0xff&pixels[o+j+radius];
                mean-=p0;
                lineMean[i][j]=mean;
            }
            for(j=radius+1;j<w-radius;j++){
                mean+=0xff&pixels[o+j+radius];
                mean-=0xff&pixels[o+j-radius-1];
                lineMean[i][j]=mean;
            }
            for(j=w-radius;j<w;j++){
                mean+=p1;
                mean-=0xff&pixels[o+j-radius-1];
                lineMean[i][j]=mean;
            }
        }

        lSize*=lSize;
        for(j=0;j<w;j++){
            p0=lineMean[0][j];
            p1=lineMean[h-1][j];
            mean=(radius+1)*p0;
            for(i=0;i<radius;i++){
                mean+=lineMean[i][j];
            }
            for(i=0;i<=radius;i++){
                mean+=lineMean[i+radius][j];
                mean-=p0;
                blockMean[i][j]=mean/lSize;
            }
            for(i=radius+1;i<h-radius;i++){
                mean+=lineMean[i+radius][j];
                mean-=lineMean[i-radius-1][j];
                blockMean[i][j]=mean/lSize;
            }
            for(i=h-radius;i<h;i++){
                mean+=p1;
                mean-=lineMean[i-radius-1][j];
                blockMean[i][j]=mean/lSize;
            }
        }
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixels[o+j]=(byte)blockMean[i][j];
            }
        }
    }
    static public void meanFiltering(int w, int h, int[] pixels, int radius){
        int len=h*w;
        double[][] lineMean=new double[h][w],blockMean=new double[h][w];
        double p,mean,p0,p1;
        int lSize=2*radius+1;
        int i,j,o;
        for(i=0;i<h;i++){
            o=i*w;
            p0=pixels[o];
            p1=pixels[o+w-1];
            mean=(radius+1)*p0;
            for(j=0;j<radius;j++){
                mean+=pixels[o+j];
            }
            for(j=0;j<=radius;j++){
                mean+=pixels[o+j+radius];
                mean-=p0;
                lineMean[i][j]=mean;
            }
            for(j=radius+1;j<w-radius;j++){
                mean+=pixels[o+j+radius];
                mean-=pixels[o+j-radius-1];
                lineMean[i][j]=mean;
            }
            for(j=w-radius;j<w;j++){
                mean+=p1;
                mean-=pixels[o+j-radius-1];
                lineMean[i][j]=mean;
            }
        }

        lSize*=lSize;
        for(j=0;j<w;j++){
            p0=lineMean[0][j];
            p1=lineMean[h-1][j];
            mean=(radius+1)*p0;
            for(i=0;i<radius;i++){
                mean+=lineMean[i][j];
            }
            for(i=0;i<=radius;i++){
                mean+=lineMean[i+radius][j];
                mean-=p0;
                blockMean[i][j]=mean/lSize;
            }
            for(i=radius+1;i<h-radius;i++){
                mean+=lineMean[i+radius][j];
                mean-=lineMean[i-radius-1][j];
                blockMean[i][j]=mean/lSize;
            }
            for(i=h-radius;i<h;i++){
                mean+=p1;
                mean-=lineMean[i-radius-1][j];
                blockMean[i][j]=mean/lSize;
            }
        }
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixels[o+j]=(int)blockMean[i][j];
            }
        }
    }
    static public void meanFiltering(int w, int h, int [][] pixels, int radius){
        int len=h*w;
        double[][] lineMean=new double[h][w],blockMean=new double[h][w];
        double p,mean,p0,p1;
        int lSize=2*radius+1;
        int i,j;
        for(i=0;i<h;i++){
            p0=pixels[i][0];
            p1=pixels[i][w-1];
            mean=(radius+1)*p0;
            for(j=0;j<radius;j++){
                mean+=pixels[i][j];
            }
            for(j=0;j<=radius;j++){
                mean+=pixels[i][j+radius];
                mean-=p0;
                lineMean[i][j]=mean;
            }
            for(j=radius+1;j<w-radius;j++){
                mean+=pixels[i][j+radius];
                mean-=pixels[i][j-radius-1];
                lineMean[i][j]=mean;
            }
            for(j=w-radius;j<w;j++){
                mean+=p1;
                mean-=pixels[i][j-radius-1];
                lineMean[i][j]=mean;
            }
        }

        lSize*=lSize;
        for(j=0;j<w;j++){
            p0=lineMean[0][j];
            p1=lineMean[h-1][j];
            mean=(radius+1)*p0;
            for(i=0;i<radius;i++){
                mean+=lineMean[i][j];
            }
            for(i=0;i<=radius;i++){
                mean+=lineMean[i+radius][j];
                mean-=p0;
                pixels[i][j]=(int)mean/lSize;
            }
            for(i=radius+1;i<h-radius;i++){
                mean+=lineMean[i+radius][j];
                mean-=lineMean[i-radius-1][j];
                pixels[i][j]=(int)mean/lSize;
            }
            for(i=h-radius;i<h;i++){
                mean+=p1;
                mean-=lineMean[i-radius-1][j];
                pixels[i][j]=(int)mean/lSize;
            }
        }
    }
    static public void meanFiltering1(int w, int h, int [][] pixels, int radius){
        int len=h*w;
        double[][] lineMean=new double[h][w],blockMean=new double[h][w];
        double mean,p0,p1;
        int lSize=2*radius+1;
        int i,j;
        for(i=0;i<h;i++){
            mean=0;
            for(j=0;j<2*radius+1;j++){
                mean+=pixels[i][j];
            }
            lineMean[i][radius]=mean;
            for(j=radius+1;j<w-radius;j++){
                mean+=pixels[i][j+radius];
                mean-=pixels[i][j-radius-1];
                lineMean[i][j]=mean;
            }
            for(j=w-radius;j<w;j++){
                lineMean[i][j]=lineMean[i][w-radius-1];
            }
            for(j=0;j<radius;j++){
                lineMean[i][j]=lineMean[i][radius];
            }
        }

        lSize*=lSize;
        for(j=0;j<w;j++){
            mean=0;
            for(i=0;i<2*radius+1;i++){
                mean+=lineMean[i][j];
            }
            pixels[radius][j]=(int)(mean/lSize);
            for(i=radius+1;i<h-radius;i++){
                mean+=lineMean[i+radius][j];
                mean-=lineMean[i-radius-1][j];
                pixels[i][j]=(int)mean/lSize;
            }
            for(i=h-radius;i<h;i++){
                pixels[i][j]=(int)pixels[radius][j];
            }
            for(i=0;i<radius;i++){
                pixels[i][j]=(int)pixels[radius][j];
            }
        }
    }
    public static int nonEIPoint(int[][] pixels, int w, int h, int x, int y, int dx, int dy, int p0){
        int p=p0;
        while(p==p0){
            x+=dx;
            y+=dy;
            if(x>=0&&x<w&&y>=0&&y<h) p=pixels[y][x];
            else p=p0-1;
        }
        return p;
    }
    public static int circularAddition(int size, int position, int delta){
        int sum=(position+delta)%size;
        if(sum<0) sum+=(-sum/size+1)*size;
        return sum;
    }
    public static double getAverage(ArrayList<Integer> ir){
        int size=ir.size();
        double ave=0.;
        for(int i=0;i<size;i++){
            ave+=ir.get(i);
        }
        return ave/size;
    }
    public static void setPixels(ImagePlus impl, int[][][]pixels){
        int slice,N=pixels.length;
        for(slice=1;slice<=N;slice++){
            impl.setSlice(slice);
            setPixels(impl,pixels[slice-1]);
        }
    }
    public static void setPixels(ImagePlus impl,int[][] pixels){
        int X=0,Y=0,w=impl.getWidth(),h=impl.getHeight();
        setPixels(impl,pixels,X,Y,w,h,false);
    }
    public static void copyPixels(ImagePlus impl1, ImagePlus impl2, int[][] pixels){
        getPixelValue(impl1, impl1.getCurrentSlice(),pixels);
        setPixels(impl2,pixels);
    }
    public static void setPixels(ImagePlus impl,int[][] pixels, boolean rgbPixel){
        int X=0,Y=0,w=impl.getWidth(),h=impl.getHeight();
        setPixels(impl,pixels,X,Y,w,h,rgbPixel);
    }
/*
    public static void setPixels(ImagePlus impl,int[][] pixels, boolean fitToRange, boolean rgbPixel){
        if(fitToRange){
            int nRange[]=new int[2];
            CommonMethods.getPixelValueRange_Stack(impl, nRange);
            scaleData(pixels,nRange);
        }
        int X=0,Y=0,w=impl.getWidth(),h=impl.getHeight();
        setPixels(impl,pixels,X,Y,w,h,rgbPixel);
    }
*/
    public static void setPixels(ImagePlus impl,int[][] pixels, int x0, int y0, int width,  int height){
        int adjust=0;
        setPixels(impl,pixels,x0,y0,width,height,adjust,false);
    }

    public static void setPixels(ImagePlus impl,int[][] pixels, int x0, int y0, int width,  int height, boolean rgbPixel){
        int adjust=0;
        setPixels(impl,pixels,x0,y0,width,height,adjust,rgbPixel);
    }
    public static void setPixels(ImagePlus impl,int[][] pixels, int x0, int y0, int width,  int height, int adjust){
        setPixels(impl,pixels,x0,y0,width,height,adjust,false);
    }

    public static void setPixels(ImagePlus impl,int[][] pixels, int x0, int y0, int width,  int height, int adjust, boolean rgbPixel){
        int type=impl.getType();
        int h=impl.getHeight(),w=impl.getWidth();
        if(x0+width>w) {
            IJ.error("x0+width is larger than the width of the image, method getPixelValue in CommentMethods.java");
            x0=x0;
        }
        if(y0+height>h) IJ.error("y0+height is larger than the hehight of the image, method getPixelValue in CommentMethods.java");
        int x,y,o;
//        impl.setSlice(nCurrentSlice);
        if(adjust!=0){
            for(y=0;y<height;y++){
                for(x=0;x<width;x++){
                    pixels[y][x]+=adjust;
                }
            }
        }
        switch (type){
            case GRAY8:
                byte[] pixels0=(byte[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixels0[o+x0+x]=(byte)pixels[y][x];
                    }
                }
                break;
            case GRAY16:
                short[] pixels1=(short[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixels1[o+x0+x]=(short) pixels[y][x];
                    }
                }
                break;
            case COLOR_256:
                byte[] pixels3=(byte[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixels3[o+x0+x]=(byte) pixels[y][x];
                    }
                }
                break;
            case GRAY32:
                float[] pixels5=(float[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixels5[o+x0+x]=pixels[y][x];
                    }
                }
                break;
            case COLOR_RGB:
                int[][] pixelst=null;
                if(!rgbPixel){
                    pixelst=new int[h][w];
                    CommonStatisticsMethods.copyArray(pixels, pixelst);
                    grayToRGBPixels(pixelst);
                }else{
                    pixelst=pixels;
                }
                int pixel;
                int[] pixels7=(int[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixel=pixelst[y][x];
                        pixels7[o+x0+x]=pixel;
                    }
                }
                break;
            default:
                IJ.error("Unsurported image type for getPixelValue");
                break;
        }
    }

    public static void setPixel(ImagePlus impl,int pixel, int x0, int y0,boolean rgbPixel){
        int type=impl.getType();
        int h=impl.getHeight(),w=impl.getWidth();
        int x,y,o;
//        impl.setSlice(nCurrentSlice);
        switch (type){
            case GRAY8:
                byte[] pixels0=(byte[]) impl.getProcessor().getPixels();
                pixels0[y0*w+x0]=(byte)pixel;
                break;
            case GRAY16:
                short[] pixels1=(short[]) impl.getProcessor().getPixels();
                pixels1[y0*w+x0]=(short) pixel;
                break;
            case COLOR_256:
                byte[] pixels3=(byte[]) impl.getProcessor().getPixels();
                pixels3[y0*w+x0]=(byte) pixel;
                break;
            case GRAY32:
                float[] pixels5=(float[]) impl.getProcessor().getPixels();
                pixels5[y0*w+x0]=pixel;
                break;
            case COLOR_RGB:
                if(!rgbPixel){
                    pixel=grayToRGBPixel(pixel);
                }
                int[] pixels7=(int[]) impl.getProcessor().getPixels();
                pixels7[y0*w+x0]=pixel;
                break;
            default:
                IJ.error("Unsurported image type for getPixelValue");
                break;
        }
    }
    public static int grayToRGBPixel(int pixel){
        if(pixel>255) pixel=255;
        return (pixel<<16)|(pixel<<8)|pixel;
    }
    public static void grayToRGBPixels(int[][] pixels){
         int []range=new int[2];
         range[0]=0;
         range[1]=255;
         scaleData(pixels,range);
         int pixel,r,g,b,i,j,h=pixels.length,w=pixels[0].length;
         for(i=0;i<h;i++){
             for(j=0;j<w;j++){
                 pixel=pixels[i][j];
                 pixels[i][j]=(pixel<<16)|(pixel<<8)|pixel;
             }
         }
    }

    public static void getPixelValues(int [][] pixels0,int[][] pixels, int X, int width, int Y, int height){
       int x,y;
          for(y=0;y<height;y++){
              for(x=0;x<width;x++){
                   pixels[y][x]=pixels0[y+Y][x+X];
              }
          }
    }

    public static int getDigit(int a, int index){
        ArrayList <Integer> digits=new ArrayList<Integer>();
        int sign=1;
        if(a<0){
            sign=-1;
            a*=sign;
        }
        int r;
        while(a>=10){
            r=a%10;
            a/=10;
            digits.add(r);
        }
        digits.add(a);
        if(digits.size()<=index) return 0;
        return digits.get(index);
    }
    public static int setDigit(int a, int index, int c){
        int sign=1;
        if(a<0){
            sign=-1;
            a*=sign;
        }
        ArrayList <Integer> digits=new ArrayList<Integer>();
        int r;
        r=a%10;
        digits.add(r);
        while(a>9){
            a/=10;
            r=a%10;
            digits.add(r);
        }
        int size=digits.size();
        int i;
        if(index>=size){
            for(i=size;i<=index;i++){
                digits.add(0);
            }
            size=digits.size();
        }
        digits.set(index, c%10);
        int f=1;
        a=0;
        for(i=0;i<size;i++){
            a+=digits.get(i)*f;
            f*=10;
        }
        return a*sign;
    }
    public static double angleDegree(int dx, int dy){//returns angle in degree. The angle between x axis and line segment (0,0)->(dx,dy). Counter clockwise angle, the same as normal
        //coordinate system, but for the image convention, i.e., y axis pointing down.
        if(dx==0){
            if(dy>0)return -90.;
            else return 90;
        }else if(dy==0){
            if(dx>0) return 0;
            else return 180;
        }
        double r=Math.sqrt(dx*dx+dy*dy);
        double angle=Math.asin(Math.abs(dy)/r)*180./Math.PI;
        if(dx>0){
            if(dy>0) return -angle;
            else return angle;
        }else{//x<0
            if(dy>0) return -90-angle;
            else return angle+90;
        }
    }
    public static boolean withinROIs(int x, int y, ArrayList <Roi> ROIs){
        Roi ROI;
        int nSize=ROIs.size();
        for(int i=0;i<nSize;i++){
            ROI=ROIs.get(i);
            if(ROI.contains(x, y)) return true;
        }
        return false;
    }
    public static void getROIPoints(ArrayList <Roi> ROIs, ArrayList <IntArray> XPoints, ArrayList <IntArray> YPoints){
        Roi ROI;
        int nSize=ROIs.size();
        XPoints.clear();
        YPoints.clear();
        int x0,y0,w,h;
        for(int i=0;i<nSize;i++){
            ROI=ROIs.get(i);
            IntArray Xs=new IntArray();
            IntArray Ys=new IntArray();
            x0=ROI.getBoundingRect().x;
            y0=ROI.getBoundingRect().y;
            w=ROI.getBoundingRect().width;
            h=ROI.getBoundingRect().height;
            for(int x=x0;x<x0+w;x++){
                    for(int y=y0;y<y0+h;y++){
                        if(ROI.contains(x, y)){
                            Xs.m_intArray.add(x);
                            Ys.m_intArray.add(y);
                        }
                    }
              }
            XPoints.add(Xs);
            YPoints.add(Ys);
        }
    }
    public static void getMaxPixel(int [][] pixels, Roi ROI, IntArray XPoints, IntArray YPoints, int[]xy){
        int nx=0,pixel;
        int nSize=XPoints.m_intArray.size();
        int x,y,dx,dy;
        boolean lp=true;
        for(int i=0;i<nSize;i++){
            x=XPoints.m_intArray.get(i);
            y=YPoints.m_intArray.get(i);
            if(ROI.contains(x, y)){
                pixel=pixels[y][x];
                if(pixel>nx){
                    lp=true;
                    for(dx=-1;dx<=1;dx++){
                        for(dy=-1;dy<=1;dy++){
                            if(pixels[y][x]>pixel) lp=false;
                            if(!lp) break;
                        }
                        if(!lp) break;
                    }
                    if(lp){
                        nx=pixel;
                        xy[0]=x;
                        xy[1]=y;
                    }
                }
            }
        }
    }
    public static void labelWithCircle(ImagePlus impl, int x, int y, int len, Color c){
        impl.getProcessor().setColor(c);
        impl.getProcessor().setLineWidth(2);
        impl.getProcessor().drawOval(x, y, len, len);
    }
    public static double getAvePixel(int [][]pixels, int x0, int y0, int dx, int dy){
        double ave=0;
        int i,j,x,y;
        int num=0;
        int h=pixels.length;
        int w=pixels[0].length;
        for(i=-dy;i<=dy;i++){
            y=y0+i;
            if(y<0||y>=h) continue;
            for(j=-dx;j<=dx;j++){
                x=x0+j;
                if(x<0||x>=w) continue;
                ave+=pixels[y][x];
                num++;
            }
        }
        ave/=num;
        return ave;
    }

    public static void getPixelValue_stack (ImagePlus impl, int pixels[][]){//pixels [n][len]: each row stores pixel value of one slice. len=w*h
        int type=impl.getType();
        int h=impl.getHeight(),w=impl.getWidth();
        int nSlices=impl.getStackSize();
        int i,x,y,o,j;
        int len=w*h;
        for(i=0;i<nSlices;i++){
            impl.setSlice(i+1);
            switch (type){
                case GRAY8:
                    byte[] pixels0=(byte[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                            pixels[i][j]=0xff&pixels0[j];
                    }
                    break;
                case GRAY16:
                    short[] pixels1=(short[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                            pixels[i][j]=0xffff&pixels1[j];
                    }
                    break;
                case COLOR_256:
                    byte[] pixels3=(byte[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                            pixels[i][j]=0xff&pixels3[j];
                    }
                    break;
                case GRAY32:
                    float[] pixels5=(float[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                            pixels[i][j]=(int)pixels5[j];
                    }
                    break;
                case COLOR_RGB:
                    int[] pixels7=(int[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                            pixels[i][j]=getRGBPixelIntensity_ave(pixels7[j]);
                    }
                    break;
                default:
                    IJ.error("Unsurported image type for getPixelValue");
                    break;
            }
        }
    }
    public static void getPixelValue (ImagePlus impl, int nCurrentSlice, int pixels[][]){//pixels [w][h], the matrix stores the pixel value of the current slice
        int h=impl.getHeight(),w=impl.getWidth();
        int pixelRange[]=new int[2];
        getPixelValue(impl,nCurrentSlice,pixels,0,0,w,h);
    }

    public static void getPixelValue (ImagePlus impl, int nCurrentSlice, int pixels[][], int[] pixelRange){//pixels [w][h], the matrix stores the pixel value of the current slice
        int h=impl.getHeight(),w=impl.getWidth();
        getPixelValue(impl,nCurrentSlice,pixels,0,0,w,h,pixelRange);
    }

    public static void getPixelValue (ImagePlus impl, int nCurrentSlice, int pixels[][], int x0, int y0, int width, int height){//pixels [w][h], the matrix stores the pixel value of the current slice
        int[] pixelRange=new int[2];
        getPixelValue(impl,nCurrentSlice,pixels,x0,y0,width,height,pixelRange);
    }
    public static void getPixelValue (ImagePlus impl, int nCurrentSlice, int pixels[][], int x0, int y0, int width, int height, int[] pixelRange){//pixels [w][h], the matrix stores the pixel value of the current slice
        int type=impl.getType();
        int h=impl.getHeight(),w=impl.getWidth();
        if(width>w-x0) width=w-x0;
        if(x0+width>w) {
            IJ.error("x0+width is larger than the width of the image, method getPixelValue in CommentMethods.java");
            x0=x0;
        }
        if(height>h-y0) height=h-y0;
        if(y0+height>h) IJ.error("y0+width is larger than the ehight of the image, method getPixelValue in CommentMethods.java");
        int x,y,o;
        impl.setSlice(nCurrentSlice);
        int pn,px;
        int pixel;
        pn=Integer.MAX_VALUE;
        px=Integer.MIN_VALUE;
        switch (type){
            case GRAY8:
                byte[] pixels0=(byte[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixel=0xff&pixels0[o+x0+x];
                        pixels[y][x]=pixel;
                        if(pixel<pn) pn=pixel;
                        if(pixel>px) px=pixel;
                    }
                }
                break;
            case GRAY16:
                short[] pixels1=(short[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixel=0xffff&pixels1[o+x0+x];
                        pixels[y][x]=pixel;
                        if(x==97&&y==74){
                            x=x;
                        }
                        if(pixel<pn) pn=pixel;
                        if(pixel>px) px=pixel;
                    }
                }
                break;
            case COLOR_256:
                byte[] pixels3=(byte[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixel=0xff&pixels3[o+x0+x];
                        pixels[y][x]=pixel;
                        if(pixel<pn) pn=pixel;
                        if(pixel>px) px=pixel;
                    }
                }
                break;
            case GRAY32:
                float[] pixels5=(float[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
                        pixel=(int) pixels5[o+x0+x];
                        pixels[y][x]=pixel;
                        if(pixel<pn) pn=pixel;
                        if(pixel>px) px=pixel;
                    }
                }
                break;
            case COLOR_RGB:
                int[] pixels7=(int[]) impl.getProcessor().getPixels();
                for(y=0;y<height;y++){
                    o=(y0+y)*w;
                    for(x=0;x<width;x++){
//                        pixel=getRGBPixelIntensity_ave(pixels7[o+x0+x]);
                        pixel=pixels7[o+x0+x];//modified on 7/28/2010
                        pixels[y][x]=pixel;
                        if(pixel<pn) pn=pixel;
                        if(pixel>px) px=pixel;
                    }
                }
                break;
            default:
                IJ.error("Unsurported image type for getPixelValue");
                break;
        }
        pixelRange[0]=pn;
        pixelRange[1]=px;
    }
    public static int getRGBPixelIntensity_ave(int pixel){
        int r,g,b;
        r=0xff&(pixel>>16);
        g=0xff&(pixel>>8);
        b=0xff&pixel;
        return(r+g+b)/3;
    }
    public static void getPixelValueRange_Stack(ImagePlus impl, int[] nRange){
       int h=impl.getHeight(),w=impl.getWidth();
       int type=impl.getType();
       int nSlices=impl.getStackSize();
       int len=w*h;
       int pixels[][]=new int[nSlices][len];
       getPixelValue_stack(impl, pixels);
       int nMin=pixels[0][0],nMax=pixels[0][0];
       int i,j,pixel;
       for(i=0;i<nSlices;i++){
           for(j=0;j<len;j++){
               pixel=pixels[i][j];
               if(pixel>nMax){
                   nMax=pixel;
               }
               if(pixel<nMin){
                   nMin=pixel;
               }
           }
       }
       nRange[0]=nMin;
       nRange[1]=nMax;
    }

    public static float getMaxPixelValue(int type){
        switch (type){
            case ImagePlus.GRAY8:
                return Byte.MAX_VALUE-Byte.MIN_VALUE;
             case ImagePlus.GRAY16:
                return Short.MAX_VALUE-Short.MIN_VALUE;
            case ImagePlus.GRAY32:
                return Float.MAX_VALUE;
            case ImagePlus.COLOR_256:
                return Byte.MAX_VALUE-Byte.MIN_VALUE;
            case ImagePlus.COLOR_RGB:
                return Byte.MAX_VALUE-Byte.MIN_VALUE;
        }
        return 0;
    }

    public static void getDataRange(int[][] pnData, int[]nRange){
        int r=pnData.length;
        int c=pnData[0].length;
        int nMin=Integer.MAX_VALUE, nMax=Integer.MIN_VALUE;
        int i,j,p;
        for(i=0;i<r;i++){
            for(j=0;j<c;j++){
                p=pnData[i][j];
                if(p<nMin) nMin=p;
                if(p>nMax) nMax=p;
            }
        }
        nRange[0]=nMin;
        nRange[1]=nMax;
    }

    public static void getDataRange(ArrayList<Double> dvData, double[]dRange){
        int len=dvData.size();
        double dMin=Double.MAX_VALUE, dMax=Double.MIN_VALUE;
        int i;
        double dx;
        for(i=0;i<len;i++){
                dx=dvData.get(i);
                if(dx<dMin) dMin=dx;
                if(dx>dMax) dMax=dx;
        }
        dRange[0]=dMin;
        dRange[1]=dMax;
    }

    public static void scaleData(int[][] pnData, int[]nRange2){
        int nRange1[]=new int[2];
        getDataRange(pnData,nRange1);
        scaleData(pnData,nRange1,nRange2);
    }
    public static void scaleData(int[][] pnData, int[]nRange1, int[]nRange2){
        int r=pnData.length;
        int c=pnData[0].length;
        int nMin1=nRange1[0],nMin2=nRange2[0],nMax1=nRange1[1],nMax2=nRange2[1];
        int i,j,p;
        for(i=0;i<r;i++){
            for(j=0;j<c;j++){
                p=pnData[i][j];
                pnData[i][j]=(int)interpolation(nMin1,nMin2,nMax1,nMax2,p);
            }
        }
    }

    public static void adjustMinimum(int[][] pnData, int newMin){
        int r=pnData.length;
        int c=pnData[0].length;
        int nRange1[]=new int[2];
        getDataRange(pnData,nRange1);
        int nMin1=nRange1[0],nMax1=nRange1[1];
        int i,j,p;
        for(i=0;i<r;i++){
            for(j=0;j<c;j++){
                p=pnData[i][j];
                pnData[i][j]+=newMin-nMin1;
            }
        }
    }

    public static void getPixelValueRange_Stack(ImagePlus impl, double dPixelRange[]){
        int ns=impl.getStackSize();
        int w=impl.getWidth();
        int h=impl.getHeight();
        int len=w*h;
        int type=impl.getType();
        double dmin=impl.getProcessor().getPixel(0, 0);
        int i,j;
        double pixel;
        double dmax=dmin;
        switch (type){
            case ImagePlus.GRAY8:
                byte pixels1[];
                for(i=0;i<ns;i++){
                    impl.setSlice(i+1);
                    pixels1=(byte[])impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        pixel=0xff&pixels1[j];
                        if(pixel>dmax) dmax=pixel;
                        if(pixel<dmin) dmin=pixel;
                    }
                }
                break;
            case ImagePlus.GRAY16:
                short pixels2[];
                for(i=0;i<ns;i++){
                    impl.setSlice(i+1);
                    pixels2=(short[])impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        pixel=0xffff&pixels2[j];
                        if(pixel>dmax) dmax=pixel;
                        if(pixel<dmin) dmin=pixel;
                    }
                }
                break;
            case ImagePlus.GRAY32:
                float pixels3[]=(float[])impl.getProcessor().getPixels();
                for(i=0;i<ns;i++){
                    impl.setSlice(i+1);
                    pixels3=(float[])impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        pixel=pixels3[j];
                        if(pixel>dmax) dmax=pixel;
                        if(pixel<dmin) dmin=pixel;
                    }
                }
                break;
            case ImagePlus.COLOR_256:
                byte pixels4[]=(byte[])impl.getProcessor().getPixels();
                for(i=0;i<ns;i++){
                    impl.setSlice(i+1);
                    pixels4=(byte[])impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        pixel=0xff&pixels4[j];
                        if(pixel>dmax) dmax=pixel;
                        if(pixel<dmin) dmin=pixel;
                    }
                }
                break;
            case ImagePlus.COLOR_RGB:
                byte R[]=new byte[len];
                byte G[]=new byte[len];
                byte B[]=new byte[len];
                for(i=0;i<ns;i++){
                    impl.setSlice(i+1);
                    getRGB(impl,R,G,B);
                    for(j=0;j<len;j++){
                        pixel=0xff&R[j];
                        if(pixel>dmax) dmax=pixel;
                        if(pixel<dmin) dmin=pixel;
                        pixel=0xff&G[j];
                        if(pixel>dmax) dmax=pixel;
                        if(pixel<dmin) dmin=pixel;
                        pixel=0xff&B[j];
                        if(pixel>dmax) dmax=pixel;
                        if(pixel<dmin) dmin=pixel;
                    }
                }
                break;
            default:
                IJ.error("Unspported image type for getPixelValueRange");
                break;
        }
        dPixelRange[0]=dmin;
        dPixelRange[1]=dmax;
    }

    public static void scaleToFullRange (ImagePlus impl, double dRange[]){
       int h=impl.getHeight(),w=impl.getWidth();
       int type=impl.getType();
       int nSlices=impl.getStackSize();
       int len=w*h;
       int i,j,pixel;
       if(dRange[1]<dRange[0])getPixelValueRange_Stack(impl,dRange);
       double dMin=dRange[0];
       double dMax=dRange[1];
       double range=dMax-dMin;
       double dPixel;


       for(i=0;i<nSlices;i++){
            impl.setSlice(i);
            switch (type){
                case GRAY8:
                    byte[] pixels1=(byte[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        pixel=0xff&pixels1[j];
                        pixel=(int) interpolation(dMin,0,dMax,255,pixel);
                        pixels1[j]=(byte)(pixel);
                    }
                    break;
                case GRAY16:
                    short[] pixels2=(short[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        pixel=0xffff&pixels2[j];
                        pixel=(int) interpolation(dMin,0,dMax,32767,pixel);
                        pixels2[j]=(short)(pixel);
                    }
                    break;
                case COLOR_256:
                    byte[] pixels3=(byte[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        pixel=0xff&pixels3[j];
                        pixel=(int) interpolation(dMin,0,dMax,255,pixel);
                        pixels3[j]=(byte)(pixel);
                    }
                    break;
                case GRAY32:
                    float[] pixels4=(float[]) impl.getProcessor().getPixels();
                    for(j=0;j<len;j++){
                        dPixel=pixels4[j];
                        dPixel=interpolation(dMin,0,dMax,2147483647,dPixel);
                        pixels4[j]=(float)dPixel;
                    }
                    break;
                case COLOR_RGB:
                    int pixels5[]=(int[])impl.getProcessor().getPixels();
                    int r,g,b;
                    for(j=0;j<len;j++){
                        pixel=pixels5[j];
                        r=0xff&(pixel>>16);
                        g=0xff&(pixel>>8);
                        b=0xff&pixel;
                        r=(int)interpolation(dMin,0,dMax,255,r);
                        g=(int)interpolation(dMin,0,dMax,255,g);
                        b=(int)interpolation(dMin,0,dMax,255,b);
                        pixels5[j]=(r<<16)|(g<<8)|(b);
                    }
                default:
                    IJ.error("Unsurported image type for scaleToFullRange");
                    break;
            }
        }
    }

    public static void scaleToFullRange (ImagePlus impl){
       double dRange[]=new double[2];
       getPixelValueRange_Stack(impl,dRange);
       scaleToFullRange (impl, dRange);
    }

    public static double interpolation(double x1, double y1, double x2, double y2, double x){
        double k=(y2-y1)/(x2-x1);
        return y1+k*(x-x1);
    }

    public static ImagePlus convertImage (ImagePlus impl, int type, boolean bScaleToFullRange){
        int h=impl.getHeight(),w=impl.getWidth();
        int len=h*w;
        int nSlices=impl.getStackSize();
        ImagePlus implc;
        if(bScaleToFullRange) scaleToFullRange(impl);
        int pixels[][]=new int[nSlices][len];
        getPixelValue_stack(impl,pixels);
        ImageStack stk=new ImageStack(w,h);
        int range[]=new int[2];
        getPixelValueRange_Stack(impl,range);
        int nMin=range[0];
        int nMax=range[1];
        int nRange=nMax-nMin;
        int i,j,pixel,r,g,b;
        for(i=0;i<nSlices;i++){
            switch (type){
                case GRAY8:
                    ByteProcessor impr=new ByteProcessor(w,h);
                    byte[] pixels0=(byte[]) impr.getPixels();
                    for(j=0;j<len;j++){
                        pixel=pixels[i][j];
                        pixels0[j]=(byte)((255*pixel)/nMax);
//                        pixels0[j]=(byte)(pixel);//change made: 2/22/2010
                    }
//                    impr=new ByteProcessor(w,h,pixels,null);
                    stk.addSlice(i+"th slice", impr);
                    break;
                case GRAY32:
                    FloatProcessor impr1=new FloatProcessor(w,h);
                    float[] pixels1=(float[]) impr1.getPixels();
                    for(j=0;j<len;j++){
                        pixel=pixels[i][j];
                        pixels1[j]=(float)pixel;
//                        pixels0[j]=(byte)(pixel);//change made: 2/22/2010
                    }
//                    impr=new ByteProcessor(w,h,pixels,null);
                    stk.addSlice(i+"th slice", impr1);
                    break;
                case ImagePlus.COLOR_RGB:
                    ColorProcessor impr2=new ColorProcessor(w,h);
                    int[] pixels2=(int[]) impr2.getPixels();
                    
                    for(j=0;j<len;j++){
                        pixel=pixels[i][j];
                        r=(int)(CommonMethods.getLinearIntoplation(nMin, 0, nMax, 255, pixel)+0.5);
                        g=r;
                        b=r;
                        pixel=(r<<16)|(g<<8)|(b);
                        pixels2[j]=pixel;
                    }
                    stk.addSlice(i+"th slice", impr2);
                    break;
                default:
                    IJ.error("method convertImage to "+ getImageTypeName(type)+" is not currently supported");
                    return null;
                    
            }
        }
        implc=new ImagePlus(getImageTypeName(type),stk);
        return implc;
    }
    public static String getImageTypeName(int type){
        String name="";
        switch(type){
            case GRAY8:
                name="GRAY8";
                break;
            case GRAY16:
                name="GRAY16";
                break;
            case GRAY32:
                name="GRAY32";
                break;
            case COLOR_256:
                name="COLOR_256";
                break;
            case COLOR_RGB:
                name="COLOR_RGB";
                break;
            default:
                name="Un known type";
                break;
        }
        return name;
    }
    public static ImagePlus getMIPImage(ImagePlus impl){
        int w=impl.getWidth();
        int h=impl.getHeight();
        int pixelsc[][]=new int[h][w], pixels[][], nSlices=impl.getNSlices(),i,x,y,nn=Integer.MIN_VALUE,pixel,pixelc;
        for(y=0;y<h;y++){
            for(x=0;x<w;x++){
                pixelsc[y][x]=nn;
            }
        }

        for(i=0;i<nSlices;i++){
            impl.setSlice(i+1);
            pixels=CommonMethods.getPixelValues(impl);
            for(y=0;y<h;y++){
                for(x=0;x<w;x++){
                    pixel=pixels[y][x];
                    pixelc=pixelsc[y][x];
                    if(pixel>pixelc) pixelsc[y][x]=pixel;
                }
            }
        }
        int type=impl.getType();
        ImagePlus implc=CommonMethods.getBlankImage(type, w, h);
        CommonMethods.setPixels(implc, pixelsc);
        return implc;
    }
    public static ImagePlus getMIPImageGray16(ImagePlus impl){
        IJ.error("Method getMIPImageGray16 has not implemented");
        int type=impl.getType(),w=impl.getWidth(),h=impl.getHeight();
        return getBlankImage(type,w,h);
    }
    public static ImagePlus getMIPImageGray32(ImagePlus impl){
        IJ.error("Method getMIPImageGray32 has not implemented");
        int type=impl.getType(),w=impl.getWidth(),h=impl.getHeight();
        return getBlankImage(type,w,h);
    }
    public static ImagePlus getMIPImageCOLOR_256(ImagePlus impl){
        IJ.error("Method getMIPImageColor_256 has not implemented");
        int type=impl.getType(),w=impl.getWidth(),h=impl.getHeight();
        return getBlankImage(type,w,h);
    }
    public static ImagePlus getMIPImageCOLOR_RGB(ImagePlus impl){
        IJ.error("Method getMIPImageColor_RGB has not implemented");
        int type=impl.getType(),w=impl.getWidth(),h=impl.getHeight();
        return getBlankImage(type,w,h);    
    }
    
    public static ImagePlus getMIPImageGray8(ImagePlus impl){
//        int nCurrentSlice=impl.getCurrentSlice();
        int h=impl.getHeight(),w=impl.getWidth();
        ImagePlus implc=CommonMethods.getBlankImage(ImagePlus.GRAY8,w,h);
        ByteProcessor impr=new ByteProcessor(w,h);
        byte pixelsMIP[]=(byte[])impr.getPixels();
        implc.setTitle("MIP of "+impl.getTitle());
        int num=impl.getNSlices();
        byte[][]pixels=new byte[num][];
        byte[] pixels0=(byte[])implc.getProcessor().getPixels();
        int x,y,i,pixel,o,px;

        for(i=1;i<=num;i++){
            impl.setSlice(i);
            pixels[i-1]=(byte[])impl.getProcessor().getPixels();
        }

        for(y=0;y<h;y++){
            o=y*w;
            for(x=0;x<w;x++){
                pixel=0xff&pixels[0][o];
                px=pixel;
                for(i=1;i<=num;i++){
                    pixel=0xff&pixels[i-1][o];//the slice number is counting from 1
                    if(pixel>px) px=pixel;
                }
                pixels0[o]=(byte)px;
                pixelsMIP[o]=(byte)px;
                o++;
            }
        }
        impl.getStack().addSlice("MIP Slice", impr);
        return implc;
    }
    public static ImagePlus getBlankImage(int type, int w, int h){
        return getBlankImage(type,w,h,0);
    }
    public static ImagePlus getBlankImageStack(int type, int w, int h, int nSlices){
        ImagePlus impl;
        ImageStack is=new ImageStack(w,h);
        for(int i=0;i<nSlices;i++){
            impl=getBlankImage(type,w,h,0);
            is.addSlice("", impl.getProcessor());
        }
        impl=new ImagePlus("new empty image stack",is);
        return impl;
    }
    public static ImagePlus getBlankImage(int type, int w, int h, int basePixel){
        int len=w*h,i;
        switch (type){
            case ImagePlus.GRAY8:
                ByteProcessor bp=new ByteProcessor(w,h);
                byte[] bps=(byte[])bp.getPixels();
                for(i=0;i<len;i++){
                    bps[i]=(byte)basePixel;
                }
                return new ImagePlus("blank image",bp);
            case ImagePlus.GRAY16:
                ShortProcessor sp=new ShortProcessor(w,h);
                short[] sps=(short[])sp.getPixels();
                for(i=0;i<len;i++){
                    sps[i]=(short)basePixel;
                }
                return new ImagePlus("blank image",sp);
            case ImagePlus.GRAY32:
                FloatProcessor fp=new FloatProcessor(w,h);
                float[] fps=(float[])fp.getPixels();
                for(i=0;i<len;i++){
                    fps[i]=(float)basePixel;
                }
                return new ImagePlus("blank image",fp);
            case ImagePlus.COLOR_256:
                ByteProcessor cp=new ByteProcessor(w,h);
                byte[] cps=(byte[])cp.getPixels();
                for(i=0;i<len;i++){
                    cps[i]=(byte)basePixel;
                }
                return new ImagePlus("blank image",cp);
            case ImagePlus.COLOR_RGB:
                ColorProcessor rgbp=new ColorProcessor(w,h);
                int[] rgbps=(int[])rgbp.getPixels();
                for(i=0;i<len;i++){
                    rgbps[i]=basePixel;
                }
                return new ImagePlus("blank image",rgbp);
            default:
                break;
        }
        ByteProcessor bp=new ByteProcessor(w,h);
        return new ImagePlus("blank image",bp);
    }

    public static JFileChooser openFileDialogExtFilter(String sTitle,int type, String sFileType, String sExt, String sDir){//type=JFileChooser.OPEN_DIALOG or JFileChooser.SAVE_DIALOG
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                sFileType, sExt);
            chooser.setFileFilter(filter);
            chooser.setDialogTitle(sTitle);
            chooser.setDialogType(type);
            chooser.setCurrentDirectory(new File(sDir));
            int returnVal = chooser.showOpenDialog(null);
            return chooser;
    }

    public static void stampPixelsPixelSorting(ImagePlus impl, int[][] stamp){
        int h=impl.getHeight(),w=impl.getWidth(),i,j,pixel;
        int[][] pixels=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        LandscapeAnalyzerPixelSorting.stampPixels(pixels, stamp);
    }
    public static void stampPixels(ImagePlus impl, int[][] stamp){
        int h=impl.getHeight(),w=impl.getWidth(),i,j,pixel;
        int[][] pixels=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        LandscapeAnalyzer.stampPixels(w, h, pixels, stamp);
    }
    public static void stampPixels(ImagePlus impl){
//        ImagePlus impl=WindowManager.getCurrentImage();
        int h=impl.getHeight(),w=impl.getWidth(),i,j,pixel;
        int[][] pixels=new int[h][w], stamp=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        LandscapeAnalyzer.stampPixels(w, h, pixels, stamp);
        ImagePlus impl0=CommonMethods.newPlainRGBImage("stamp", w, h, Color.white);
        ImagePlus impl1=CommonMethods.newPlainRGBImage("groove networks", w, h, Color.white);
        int[] pixels0=(int[])impl0.getProcessor().getPixels();
        int[] pixels1=(int[])impl1.getProcessor().getPixels();
        int r,g,b,o;
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixel=pixels[i][j];
                pixels1[o+j]=(pixel<<16)|(pixel<<8)|pixel;
                switch (stamp[i][j]){
                    case LandscapeAnalyzer.regular:
                        r=pixel;
                        g=pixel;
                        b=pixel;
                        break;
                   case LandscapeAnalyzer.localMaximum:
                        r=0;
                        g=0;
                        b=pixel;
                        break;
                   case LandscapeAnalyzer.localMinimum:
                        r=pixel;
                        g=0;
                        b=0;
                        break;
                   case LandscapeAnalyzer.saddle1:
                        r=0;
                        g=pixel;
                        b=pixel;
                        break;
                   case LandscapeAnalyzer.saddle2:
                        r=0;
                        g=pixel;
                        b=pixel/2;
                        break;
                   case LandscapeAnalyzer.groove1:
                        r=pixel;
                        g=pixel;
                        b=0;
                        break;
                   case LandscapeAnalyzer.groove2:
                        r=pixel;
                        g=pixel/2;
                        b=0;
                        break;
                   case LandscapeAnalyzer.ridge1:
                        r=pixel;
                        g=pixel;
                        b=pixel;
                        break;
                   case LandscapeAnalyzer.ridge2:
                        r=pixel;
                        g=pixel;
                        b=pixel;
                        break;
                   default:
                        r=pixel;
                        g=pixel;
                        b=pixel;
                        break;
                }
                pixels0[o+j]=(r<<16)|(g<<8)|(b);
            }
        }
        impl0.show();
        LandscapeAnalyzer.markGrooveNetworks(pixels1, stamp, w, h);
        impl1.show();
    }
    public static void invertImage(ImagePlus impl){
        int w=impl.getWidth(),h=impl.getHeight();
        int i,j,nn,nx,slice0=impl.getCurrentSlice();
        int[][] pixels=new int[h][w],pixelsv=new int[h][w];;
        int slice,nSlices=impl.getNSlices();
        int[] pnRange=new int[2];
        intRange pir;
        for(slice=1;slice<=nSlices;slice++){
            impl.setSlice(slice);
            CommonMethods.getPixelValue(impl, slice, pixels,pnRange);
            nn=pnRange[0];
            nx=pnRange[1];
            for(i=0;i<h;i++){
                for(j=0;j<w;j++){
                    pixelsv[i][j]=nn+nx-pixels[i][j];
                }
            }
            CommonMethods.setPixels(impl, pixelsv);
        }
        if(impl.isVisible()){
            impl.setSlice(slice0);
            refreshImage(impl);
        }
    }
    public static void invertImage0(ImagePlus impl){
        int type=impl.getType();
        int w=impl.getWidth(),h=impl.getHeight();
        int len=w*h;
        int i,j;
        byte pixels[];
        switch (type){
            case ImagePlus.GRAY8:
                int num=impl.getStackSize();
                for(i=1;i<=num;i++){
                    impl.setSlice(i);
                    pixels=(byte[])impl.getProcessor().getPixels();
                    int pixel;
                    for(j=0;j<len;j++){
                        pixel=0xff&pixels[j];
                        pixels[j]=(byte) (255-pixel);
                    }
                }
                break;
            default:
                IJ.error("invertImage is not implemented for "+getImageTypeName(type));
        }
    }
    public static void saveImage(ImagePlus impl, String path){
            FileSaver fs=new FileSaver(impl);
            if(isStack(impl))
                fs.saveAsTiffStack(path);
            else
                fs.saveAsTiff(path);
    }
    public static String getClassName(String className0){
        String className;
        int index=className0.lastIndexOf('.');
        className=className0.substring(index+1);
        return className;
    }
    public static boolean isStack(ImagePlus impl){
        return(impl.getStackSize()>1);
    }
    public static void maskEIPoints(int[][]pixels, int[][]stamps, int w, int h, int x, int y, GeneralGraphicalObject ggo){//mask the stam value(landsacpe catogory) of the connected Equal Intensity points of (x,y).
        if(isEIPoint(pixels,w,h,x,y)){
            int pixel=pixels[y][x];
            ArrayList<Point> contour=ContourFollower.getContour_Out(pixels, w, h, new Point(x,y), pixel,pixel);
            ggo.setContour(pixels, contour, new intRange(pixel,pixel));
            ArrayList <Point> innerPoints=ggo.getInnerPoints();
            Point p;
            int nSize=innerPoints.size();
            int cat=stamps[y][x];
            for(int i=0;i<nSize;i++){
                p=innerPoints.get(i);
                stamps[p.y][p.x]=cat+LandscapeAnalyzer.nNumCats;
            }
            stamps[y][x]=cat;
        }
    }
    public static boolean isEIPoint(int[][] pixels,int w, int h, int x0, int y0){
        int dx,dy, x,y;
        for(dx=-1;dx<=1;dx++){
            x=x0+dx;
            if(x<0||x>=w) continue;
            for(dy=-1;dy<=1;dy++){
                if(dx==0&&dy==0) continue;
                y=y0+dy;
                if(y<0||y>=h)continue;
                if(pixels[y][x]==pixels[y0][x0]) return true;
            }
        }
        return false;
    }
    public static void closeAllImages(){
        ArrayList <ImagePlus> impls=CommonMethods.getAllOpenImages();
       int size=impls.size();
        for(int i=0;i<size;i++){
            impls.get(i).close();
        }
    }
    public static void hideAllImages(){
        ArrayList <ImagePlus> impls=CommonMethods.getAllOpenImages();
        int size=impls.size();
        for(int i=0;i<size;i++){
            impls.get(i).hide();
        }
    }
    public static void breakPoint(){
        int bp=0;//To make this as a break point to be called from script files for deburging
    }
    public static void getValueRange(int w, int h, int Ints[][], intRange xRange, intRange yRange,double[] dRange){
        int i,j;
        int xn=xRange.getMin(),xx=xRange.getMax();
        int yn=yRange.getMin(),yx=yRange.getMax();
        double dn=Ints[0][0],dx=Ints[0][0],dv;
         for(i=yn;i<=yx;i++){
            for(j=xn;j<xx;j++){
                dv=Ints[i][j];
                if(dv>dx) dx=dv;
                if(dv<dn) dn=dv;
            }
        }
        dRange[0]=dn;
        dRange[1]=dx;
    }


    public static ArrayList<Point> getSpecialLandscapePoints(ImagePlus impl, int type){//using pixelSorting
        ArrayList<Point> points=new ArrayList();
        int h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels=new int[h][w];
        getPixelValue(impl,impl.getCurrentSlice(),pixels);
        getSpecialLandscapePoints(pixels,w,h,type,points);
        return points;
    }

    public static ArrayList<Point> getSpecialLandscapePoints(int[][] pixels, int type){//using pixelSorting
        int h=pixels.length,w=pixels[0].length;
        ArrayList<Point> points=new ArrayList();
        getSpecialLandscapePoints(pixels,w,h,type,points);
        return points;
    }

    public static void getSpecialLandscapePoints(int pixels[][], int w, int h, int type, ArrayList<Point> points){//this method return all pixel points of the same landscape
                  //type as spicified by "type" in the argument list, and whose pixel value is within the range specified by "ia". In case a point is EI point, then
                  //the point closest to the centeroid of all EI points will be chosen.
        int stamp[][]=new int[h][w];
        boolean stampped=false;
        getSpecialLandscapePoints(pixels,stamp,stampped,w,h,type,points);
    }

    public static void getSpecialLandscapePoints(int pixels[][], int stamp[][], boolean stampped, int w, int h, int type, ArrayList<Point> points){//this method return all pixel points of the same landscape
                  //type as spicified by "type" in the argument list, and whose pixel value is within the range specified by "ia". In case a point is EI point, then
                  //the point closest to the centeroid of all EI points will be chosen.
        int iI=0,iF=pixels.length-1,jI=0,jF=pixels[0].length-1;
        getSpecialLandscapePoints(pixels,stamp,stampped,w,h,type,iI,iF,jI,jF,points);
    }
    public static void getSpecialLandscapePoints(int pixels[][], int stamp[][], boolean stampped, int w, int h, int type, int iI,int iF, int jI, int jF, ArrayList<Point> points){//this method return all pixel points of the same landscape
                  //type as spicified by "type" in the argument list, and whose pixel value is within the range specified by "ia". In case a point is EI point, then
                  //the point closest to the centeroid of all EI points will be chosen.
        int[] pixelRange=CommonMethods.getValueRange(pixels);
        LandscapeAnalyzerPixelSorting stampper=null;
        if(!stampped) stampper=new LandscapeAnalyzerPixelSorting(w,h,getValueRange(pixels));
        getSpecialLandscapePoints(pixels,stamp,stampper,stampped, w,h, type, iI,iF, jI,jF, points);
    }
    public static int[] getValueRange(int[][] pnValues){
        int nMin=Integer.MAX_VALUE, nMax=Integer.MIN_VALUE;
        int h=pnValues.length,w=pnValues[0].length;
        int r,c,it;
        for(r=0;r<h;r++){
            for(c=0;c<w;c++){
                it=pnValues[r][c];
                if(it>nMax) nMax=it;
                if(it<nMin) nMin=it;
            }
        }
        int[] pnRange=new int[2];
        pnRange[0]=nMin;
        pnRange[1]=nMax;
        return pnRange;
    }
    public static void clearExtremaFromEdges(int[][] stamp){
        int h=stamp.length,w=stamp[0].length;
        int i,j,type;
        for(i=0;i<h;i++){
            type=stamp[i][0];
            if(type==LandscapeAnalyzer.localMaximum||type==LandscapeAnalyzer.localMinimum) stamp[i][0]=-1;
            type=stamp[i][w-1];
            if(type==LandscapeAnalyzer.localMaximum||type==LandscapeAnalyzer.localMinimum) stamp[i][w-1]=-1;
        }
        for(i=0;i<w;i++){
            type=stamp[0][i];
            if(type==LandscapeAnalyzer.localMaximum||type==LandscapeAnalyzer.localMinimum) stamp[0][i]=-1;
            type=stamp[h-1][i];
            if(type==LandscapeAnalyzer.localMaximum||type==LandscapeAnalyzer.localMinimum) stamp[h-1][i]=-1;
        }
    }
    public static void swapElements(int[][] pnV,int i1, int j1, int i2, int j2){
        int nt=pnV[i1][j1];
        pnV[i1][j1]=pnV[i2][j2];
        pnV[i2][j2]=nt;
    }
    public static void swapElements(double[][] pdV,int i1, int j1, int i2, int j2){
        double dt=pdV[i1][j1];
        pdV[i1][j1]=pdV[i2][j2];
        pdV[i2][j2]=dt;
    }
    public static void getSpecialLandscapePoints(int pixels[][], int stamp[][], LandscapeAnalyzerPixelSorting stampper, boolean stampped, int w, int h, int type0, int iI, int iF, int jI, int jF, ArrayList<Point> points){//this method return all pixel points of the same landscape
                  //type as spicified by "type" in the argument list, and whose pixel value is within the range specified by "ia". In case a point is EI point, then
                  //the point closest to the centeroid of all EI points will be chosen.
        int i,j,pixel,type;
        if(!stampped) {
            stampper.updateAndStampPixels(pixels,stamp,iI, iF,jI,jF);
        }
//        moveExtremaFromEdges(pixels,stamp);//2/22/2011
        points.clear();
        for(i=iI;i<=iF;i++)
        {
            for(j=jI;j<=jF;j++)
            {
                type=LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[i][j]);
                switch(type0)
                {
                    case LandscapeAnalyzerPixelSorting.localMinimum:
                        if(type==type0||type==LandscapeAnalyzerPixelSorting.watershedLM){
                            points.add(new Point(j,i));
                        }
                        break;
                    case LandscapeAnalyzerPixelSorting.isolatedMinimum:
                        if(type==LandscapeAnalyzerPixelSorting.localMinimum){
                            if(LandscapeAnalyzerPixelSorting.isIsolatedMinimum(stamp, j, i))points.add(new Point(j,i));
                        }
                        break;
                    case LandscapeAnalyzerPixelSorting.watershed:
                        if(type==type0||type==LandscapeAnalyzerPixelSorting.watershedLM){
                            points.add(new Point(j,i));
                        }
                        break;
                    default:
                        if(type==type0){
                        points.add(new Point(j,i));
                    }
                    break;
                }
            }
        }
    }

    public static ArrayList<Point> getSpecialLandscapePoints(int pixels[][], LandscapeAnalyzerPixelSorting stampper, int type){
        ArrayList<Point> points=new ArrayList();
        int h=pixels.length,w=pixels[0].length;
        getSpecialLandscapePoints(pixels,new int[h][w], stampper, false, w,h,type, 0,h-1, 0,w-1,points);
        return points;
    }

    public static void getSpecialLandscapePoints(int pixels[][], int w, int h, int type, intRange ia ,ArrayList<Point> points){//this method return all pixel points of the same landscape
                  //type as spicified by "type" in the argument list, and whose pixel value is within the range specified by "ia". In case a point is EI point, then
                  //the point closest to the centeroid of all EI points will be chosen.
        int i,pixel,stamp[][]=new int[h][w];
        ArrayList<Point> pointst=new ArrayList();
        getSpecialLandscapePoints(pixels,stamp,false,w,h,type,0,h-1, 0,w-1,pointst);
        int len=points.size();
        Point pt;
        points.clear();
        for(i=0;i<len;i++){
            pt=points.get(i);
            if(ia.contains(pixels[pt.y][pt.x]))
                points.add(pt);
        }
    }

    public static int getValueOfPercentile(ArrayList <Integer> values, double percentile){
        int index=0;
        QuickSortInteger.quicksort(values);
        int len=values.size();
        index=(int) (len*percentile);
        return values.get(index);
    }
    public static Point closestPointToCenter_avoidImageEdges(ArrayList <Point> points,int w,int h){//it returns a border point if all EI points are on the image edges
        Point cp=getCentroid(points),p;
        int nSize=points.size(),cx=cp.x,cy=cp.y,index=0,x,y;
        double dist,dn=0,dx,dy;
        boolean first=true;
        for(int i=0;i<nSize;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            dist=(x-cx)*(x-cx)+(y-cy)*(y-cy);
            if(x==0||x==w-1) {
                index=-1;
                continue;
            }
            if(y==0||y==h-1) {
                index=-1;
                continue;
            }
            if(first){
                index=i;
                dn=dist;
                first=false;
            }
            if(dist<dn){
                dn=dist;
                index=i;
            }
        }
        if(index==-1){
            x=cp.x;
            y=cp.y;
            if(x==0) x=1;
            if(x==w-1) x--;
            if(y==0) y=1;
            if(y==h-1) y--;
            return (new Point(x,y));
        }
        return points.get(index);
    }
    public static Point closestPointToCenter(ArrayList <Point> points,int w,int h){
        Point cp=getCentroid(points),p;
        int nSize=points.size(),cx=cp.x,cy=cp.y,index=0,x,y;
        double dist,dn=Double.POSITIVE_INFINITY,dx,dy;
        boolean first=true;
        for(int i=0;i<nSize;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            dist=(x-cx)*(x-cx)+(y-cy)*(y-cy);
            if(dist<dn){
                dn=dist;
                index=i;
            }
        }
        return points.get(index);
    }
    public static Point getCentroid(ArrayList <Point> points){
        double cx=0,cy=0;
        int nSize=points.size();
        Point p;
        for(int i=0;i<nSize;i++){
            p=points.get(i);
            cx+=points.get(i).x;
            cy+=points.get(i).y;
        }
        p=new Point((int)(cx/nSize+0.5),(int) (cy/nSize+0.5));
        return p;
    }
    public static void getContours(int[][] pixels, int w, int h, ArrayList<Point> points, intRange ir ,ArrayList <PointArray> contours){
        int nSize=points.size();
        int i,pixel,index;
        Point p;
        for(i=0;i<nSize;i++){
            index=nSize-1-i;
            p=points.get(index);
            pixel=pixels[p.y][p.x];
            if(!ir.contains(pixel)){
                points.remove(index);
                continue;
            }
            ArrayList<Point> contour=ContourFollower.getContour_Out(pixels, w, h, p, ir.getMin(), ir.getMax());
            contours.add(new PointArray(contour));
        }
    }
    public static void getSpecialLandscapePoints_nonePixelSorting(int [][] pixels, int[][] stamp, int w, int h, int type, ArrayList<Point> trail, ArrayList <Point> points){
        int nSize=trail.size();
        int x,y;
        Point p;
        clearExtremaFromEdges(stamp);
        for(int i=0;i<nSize;i++){
            p=trail.get(i);
            x=p.x;
            y=p.y;
            if(stamp[y][x]==type){
                if(isEIPoint(pixels,w,h,x,y)){
                    GeneralGraphicalObject ggo=new GeneralGraphicalObject();
                    maskEIPoints(pixels,stamp,w,h,x,y,ggo);
                    points.add(closestPointToCenter(ggo.getInnerPoints(),w,h));
                }else{
                    points.add(new Point(x,y));
                }
            }
        }
    }
    public static void getExtremeValues(int pixels[][], ArrayList <Integer> xPoints, ArrayList <Integer> yPoints,Point[] extremePoints,int []nRange){
        int nSize=xPoints.size();
        int x,y,pixel,nn,nx,in=0,ix=0;
        nn=pixels[yPoints.get(0)][xPoints.get(0)];
        nx=nn;
        Point pn=new Point(xPoints.get(0),yPoints.get(0));
        Point px=new Point(xPoints.get(0),yPoints.get(0));
        for(int i=0;i<nSize;i++){
            x=xPoints.get(i);
            y=yPoints.get(i);
            pixel=pixels[y][x];
            if(pixel<nn){
                nn=pixel;
                in=i;
            }
            if(pixel>nx){
                nx=pixel;
                ix=i;
            }
        }
        extremePoints[0]=new Point(xPoints.get(in),yPoints.get(in));
        extremePoints[1]=new Point(xPoints.get(ix),yPoints.get(ix));
        nRange[0]=nn;
        nRange[1]=nx;
    }
    public static void getExtremeValues(int pixels[][], ArrayList <Point> points,int Indexes[],Point[] extremePoints,int []nRange){
        int nSize=points.size();
        int x,y,pixel,nn,nx,in=0,ix=0;
        Point p=points.get(0);
        nn=pixels[p.y][p.x];
        nx=nn;
        Point pn=new Point(p);
        Point px=new Point(p);
        for(int i=0;i<nSize;i++){
            p=points.get(i);
            pixel=pixels[p.y][p.x];
            if(pixel<nn){
                nn=pixel;
                in=i;
            }
            if(pixel>nx){
                nx=pixel;
                ix=i;
            }
        }
        extremePoints[0]=new Point(points.get(in));
        extremePoints[1]=new Point(points.get(ix));
        nRange[0]=nn;
        nRange[1]=nx;
        Indexes[0]=in;
        Indexes[1]=ix;
    }
    public static ArrayList <Integer> getPixelValues(int[][] pixels, ArrayList <Point> points){
         ArrayList <Integer> pvs=new ArrayList ();
         int len=points.size();
         Point p;
         for(int i=0;i<len;i++){
             p=points.get(i);
             pvs.add(pixels[p.y][p.x]);
         }
         return pvs;
    }
    public static void copyArrayList(ArrayList <Object> a1, ArrayList <Object> a2){
        int nSize=a1.size();
        for(int i=0;i<nSize;i++){
            a2.add(a1.get(i));
        }
    }
    public static void watershedImage(ImagePlus impl){
        int num=impl.getStackSize();
        int w=impl.getWidth();
        int h=impl.getHeight();
        int index;
        ImagePlus implw;
        ImageStack is=new ImageStack(w,h);
        for(index=1;index<=num;index++){
            impl.setSlice(index);
            ImagePlus implt=new ImagePlus("",impl.getProcessor());
            IJ.runPlugIn(implt, "Watershed_Algorithm", "");
            implw=WindowManager.getCurrentImage();
            is.addSlice("", implw.getProcessor());
            implw.clone();
        }
        implw=new ImagePlus("watershed Image",is);
        implw.show();
    }
    public static void getPixelHeights(int[][]pixels, int w, int h, int radius, ArrayList<Point> points, ArrayList <Double> pixelHeights){
        int len=points.size();
        int i,x0,y0;
        Point p;
        radius+=2;
        int ln=2*radius+1;
        for(i=0;i<len;i++){
            p=points.get(i);
            x0=p.x;
            y0=p.y;

            pixelHeights.add((double)getPixelHeight(pixels,w,h,x0,y0,radius));
        }
    }
    public static void getPixelHeights_ave(int[][]pixels, int w, int h, int radius, ArrayList<Point> points, ArrayList <Double> pixelHeights){
        int len=points.size();
        int i,x0,y0;
        Point p;
        for(i=0;i<len;i++){
            p=points.get(i);
            x0=p.x;
            y0=p.y;

            pixelHeights.add((double)getPixelHeight_ave(pixels,w,h,x0,y0,radius));
        }
    }
    public static int getPixelHeight(int[][]pixels, int w, int h, int x0, int y0,int radius){
        double ave=0;
        int n=0;
        int i,j,x,y,dx,dy;
        Point p;
        radius+=2;
        int ln=2*radius+1;
        intRange xRange=new intRange(78,80),yRange=new intRange(192,196);
        int []pv=new int[4*(ln-1)];
        double median;
            n=0;
            ave=0.;
            x=x0-radius;
            for(dy=-radius;dy<=radius;dy++){
                y=y0+dy;
                if(y<0||y>=h) continue;
                if(x<0||x>=w)continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }

            x=x0+radius;
            for(dy=-radius;dy<=radius;dy++){
                y=y0+dy;
                if(y<0||y>=h) continue;
                if(x<0||x>=w)continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }

            y=y0-radius;
            for(dx=-radius+1;dx<radius;dx++){
                x=x0+dx;
                if(y<0||y>=h) continue;
                if(x<0||x>=w)continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }

            y=y0+radius;
            for(dx=-radius+1;dx<radius;dx++){
                x=x0+dx;
                if(y<0||y>=h) continue;
                if(x<0||x>=w) continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }
            ave/=n;
            median=getMedian(pv,ave,n);
            return (int)(pixels[y0][x0]-median);
    }
    public static int getPixelHeight_ave(int[][]pixels, int w, int h, int x0, int y0,int radius){
        double ave=0;
        int n=0;
        int dr=2;
        int x,y,dx,dy;
        Point p;
        radius+=2;
        int ln=2*radius+1;
        intRange xRange=new intRange(78,80),yRange=new intRange(192,196);
        int []pv=new int[4*(ln-1)];

        int t=0;
        n=0;
        for(dy=-dr;dy<=dr;dy++){
            y=y0+dy;
            if(y<0||y>=h) continue;
            for(dx=-dr;dx<=dr;dx++){
                x=x0+dy;
                if(x<0||x>=w)continue;
                t+=pixels[y][x];
                n++;
            }
        }
        t/=n;

        double median;
            n=0;
            ave=0.;
            x=x0-radius;
            for(dy=-radius;dy<=radius;dy++){
                y=y0+dy;
                if(y<0||y>=h) continue;
                if(x<0||x>=w)continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }

            x=x0+radius;
            for(dy=-radius;dy<=radius;dy++){
                y=y0+dy;
                if(y<0||y>=h) continue;
                if(x<0||x>=w)continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }

            y=y0-radius;
            for(dx=-radius+1;dx<radius;dx++){
                x=x0+dx;
                if(y<0||y>=h) continue;
                if(x<0||x>=w)continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }

            y=y0+radius;
            for(dx=-radius+1;dx<radius;dx++){
                x=x0+dx;
                if(y<0||y>=h) continue;
                if(x<0||x>=w) continue;
                pv[n]=pixels[y][x];
                n++;
                ave+=pixels[y][x];
            }
            ave/=n;
            median=getMedian(pv,ave,n);
            return (int)(t-median);
    }
    public static double getMedian(int[]pv, double dv, int n){
        double md=0.;
        int nh=n/2;
        ArrayList <Integer> largers=new ArrayList();
        ArrayList <Integer> smallers=new ArrayList();
        ArrayList <Integer> longer;
        int i,j;
        for(i=0;i<n;i++){
            if(pv[i]>dv)
                largers.add(pv[i]);
            else
                smallers.add(pv[i]);
        }
        n=smallers.size();
        longer=smallers;
        double factor=1.;

        if(n<largers.size()){
            n=largers.size();
            longer=largers;
            factor=-1.;
        }
        int nd=n-nh;
        double nx,nt;
        int ix;
        nx=dv;
        for(i=0;i<nd;i++){
            nx=longer.get(i)*factor;
            ix=i;
            for(j=i+1;j<n;j++){
                nt=longer.get(j)*factor;
                if(nt>nx){
                    nx=nt;
                    ix=j;
                }
            }
            if(ix!=i){
                longer.set(ix, longer.get(i));
                longer.set(i, longer.get(ix));
            }
        }
        md=nx*factor;
        return md;
    }

    public static void markSpecialLandscapePoints(ImagePlus impl, ImagePlus implc, ArrayList <Integer> types,int pixel, ArrayList<IntArray> rgbIndexes){
        int w=impl.getWidth(),h=impl.getHeight();
        int[] pnRange=new int[2];
        getPixelValueRange_Stack(impl,pnRange);
        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(w,h,pnRange);
        markSpecialLandscapePoints(impl,implc,stampper,types,pixel,rgbIndexes,0,h-1,0,w-1);
    }

    public static void markSpecialLandscapePoints(ImagePlus impl, ImagePlus implc, LandscapeAnalyzerPixelSorting stampper, ArrayList <Integer> types,int pixel, ArrayList<IntArray> rgbIndexes,int iI, int iF, int jI, int jF){
        int w=impl.getWidth(),h=impl.getHeight();
        int[][] stamp=new int[h][w];
        int[][] pixels=getPixelValues(impl);
//        stampper.updateAndStampPixels(pixels,stamp);
        ArrayList <Point> points= new ArrayList();
        int i,j,it,type;
        int len=types.size();
//        stamp=stampper.getStamp();
        for(it=0;it<len;it++){
            points.clear();
            type=types.get(it);
            CommonMethods.getSpecialLandscapePoints(pixels, stamp, stampper, false, w, h, type, iI,iF,jI,jF, points);

            drawTrail(implc,points,pixel,rgbIndexes.get(it).m_intArray);
        }
    }

    public static void drawTrail(ImagePlus impl, ArrayList <Point> pa, int pixel0, ArrayList <Integer> rgbIndexes){
        if(impl.getType()!=ImagePlus.COLOR_RGB) IJ.error("only a COLOR_RGB image is supported by drawTrail");
        int rgb0[]=new int[3];
        int rgb[]=new int[3];
        rgb0[0]=0xff&(pixel0>>16);
        rgb0[1]=0xff&(pixel0>>8);
        rgb0[2]=0xff&(pixel0);
        int pixel,position,i,j,index;
        
        int[] pixels=(int[])impl.getProcessor().getPixels();
        int w=impl.getWidth();
        int h=impl.getHeight();
        int len=pa.size();

        int num=rgbIndexes.size();
        Point p;
        for(i=0;i<len;i++){
            p=pa.get(i);
            position=p.y*w+p.x;
            pixel=pixels[position];

            rgb[0]=0xff&(pixel>>16);
            rgb[1]=0xff&(pixel>>8);
            rgb[2]=0xff&(pixel);

            for(j=0;j<num;j++){
                index=rgbIndexes.get(j);
                rgb[index]=rgb0[index];
            }

            pixel=(rgb[0]<<16)|(rgb[1]<<8)|(rgb[2]);
            pixels[position]=pixel;
        }
    }
    public static void drawTrail(ImagePlus impl, ArrayList <Point> pa, int pixel){
        drawTrail(impl,pa,pixel,new Point(0,0));
    }
    public static void drawTrail(ImagePlus impl, ArrayList <Point> pa, int pixel, Point pDisplacement){
        int indexI=0,indexF=pa.size()-1;
        drawTrail(impl,pa,pixel,pDisplacement,indexI,indexF);
    }
    public static int drawTrail(ImagePlus impl, ArrayList <Point> pa, int pixel, Point pDisplacement,int indexI, int indexF){
        if(impl.getType()!=ImagePlus.COLOR_RGB) return setPixelValue(impl,pa,pixel);
        if(pa.size()==0) return -1;
        int dx=pDisplacement.x, dy=pDisplacement.y;
        int[] pixels=(int[])impl.getProcessor().getPixels();
        int w=impl.getWidth(),h=impl.getHeight();
        Point p;
        int len=pa.size();
        int i=indexI,i0=i;
        do{
            p=pa.get(i);
            if(!offBound(w,h,p.x+dx,p.y+dy)) {
                pixels[(p.y+dy)*w+p.x+dx]=pixel;
            }
            i0=i;
            i=CommonMethods.circularAddition(len, i, 1);
        } while(i0!=indexF);
        return 1;
    }
    public static int setPixelValue(ImagePlus impl, ArrayList <Point> pa, int pixel){
        Point p;
        int[][] pixels=getPixelValues(impl);
        int len=pa.size(),x,y,w=impl.getWidth(),h=impl.getHeight();
        for(int i=0;i<len;i++){
            p=pa.get(i);
            x=p.x;
            y=p.y;
            if(x<0||x>=w) continue;
            if(y<0||y>=h) continue;
            pixels[p.y][p.x]=pixel;
        }
        setPixels(impl,pixels);
        return 1;
    }
    public static void drawDot(ImagePlus impl, Point p, int pixel){
        int w=impl.getWidth();
        int h=impl.getHeight();
        intRange xRange=new intRange(0,w-1);
        intRange yRange=new intRange(0,h-1);
        if(impl.getType()!=ImagePlus.COLOR_RGB) IJ.error("only a COLOR_RGB image is supported by drawTrail");
        int[] pixels=(int[])impl.getProcessor().getPixels();
        if(xRange.contains(p.x)&&xRange.contains(p.y)){
            pixels[p.y*w+p.x]=pixel;
        }
    }
    public static boolean absolutePath(String path){
        int index=path.indexOf('"');
        if(index<=0) return false;
        int index1=path.indexOf('\\');
        if(index>index1) return false;
        return true;
    }
    public static void callRandomNumbers(int size){
        double dt;
        for(int i=0;i<size;i++){
            dt=Math.random();
        }
    }
    public static void randomizeImage(ImagePlus impl){
        int type=impl.getType(),num=impl.getStackSize();
//        callRandomNumbers(256*256*30);
        for(int i=0;i<num;i++){
            impl.setSlice(i+1);
            randomizeImage(impl.getProcessor(),type);
        }
    }
    public static void randomizeImage(ImageProcessor impr, int type){
        switch(type){
            case ImagePlus.GRAY8:
                randomizeByteProcessor(impr);
                break;
            case ImagePlus.GRAY16:
                randomizeShortProcessor(impr);
                break;
            case ImagePlus.GRAY32:
                randomizeFloatProcessor(impr);
                break;
            case(ImagePlus.COLOR_256):
                randomizeByteProcessor(impr);
                break;
            case ImagePlus.COLOR_RGB:
                randomizeColorProcessor(impr);
                break;
            default:
                IJ.error("Unsupported image type for randomizeImage");
                break;

        }
    }
    public static void randomizeByteProcessor(ImageProcessor impr){
        MT random=new MT();
        byte[] pixels=(byte[]) impr.getPixels();
        int N = pixels.length;
        int r,i;
        byte t;
        for(i=0;i<N;i++){
//            r = i + (int) (Math.random() * (N-i));   // between i and N-1
            r = i + CommonStatisticsMethods.getCircularIndex(random.random()%(N-i), N-i);   // between i and N-1 2/5/2013
            t=pixels[i];
            pixels[i]=pixels[r];
            pixels[r]=t;
        }
    }
    public static void randomizeShortProcessor(ImageProcessor impr){
        MT random=new MT();
        short[] pixels=(short[]) impr.getPixels();
        int N = pixels.length;
        int r,i;
        short t;
        for(i=0;i<N;i++){
//            r = i + (int) (Math.random() * (N-i));   // between i and N-1
            r = i + CommonStatisticsMethods.getCircularIndex(random.random()%(N-i), N-i);   // between i and N-1 2/5/2013
            t=pixels[i];
            pixels[i]=pixels[r];
            pixels[r]=t;
        }
    }
    public static void randomizeFloatProcessor(ImageProcessor impr){
        MT random=new MT();
        float[] pixels=(float[]) impr.getPixels();
        int N = pixels.length;
        int r,i;
        float t;
        for(i=0;i<N;i++){
//            r = i + (int) (Math.random() * (N-i));   // between i and N-1
//            r = i + random.random()%(N-i);   // between i and N-1 8/14/2010
            r = i + CommonStatisticsMethods.getCircularIndex(random.random()%(N-i), N-i);   // between i and N-1 2/5/2013
            t=pixels[i];
            pixels[i]=pixels[r];
            pixels[r]=t;
        }
    }
    public static void randomizeColorProcessor(ImageProcessor impr){
        MT random=new MT();
        int[] pixels=(int[]) impr.getPixels();
        int N = pixels.length;
        int r,i;
        int t;
        for(i=0;i<N;i++){
//            r = i + (int) (Math.random() * (N-i));   // between i and N-1
            r = i + CommonStatisticsMethods.getCircularIndex(random.random()%(N-i), N-i);   // between i and N-1 2/5/2013
            t=pixels[i];
            pixels[i]=pixels[r];
            pixels[r]=t;
        }
    }
    public static char readChar8(DataInputStream ds){
        byte b=0;
        try{
            b=ds.readByte();
        } catch (IOException e){
            IJ.error(e+" --- in readChar8 CommenMethods");
        }
        byte a=0;
        char c= (char)(0xff&b);
        return c;
    }
    public static void writeChar8(DataOutputStream ds, char c){
        byte b=(byte)(c&0xff);
        try{
            ds.writeByte(b);
        } catch (IOException e){
            IJ.error(e+" --- in writeChar8 CommenMethods");
        }
    }
    public static int getPixelPercentile(ImagePlus impl, double dPercentile, int radius){
        Histogram hist=new Histogram();
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        double dPixelRange[]=new double[2];
        CommonMethods.getPixelValueRange_Stack(impl, dPixelRange);
        double dPer=0.5;
        hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
        hist.setPercentile(dPer);
        int r=radius,col,rol,x,y;
        int ri=0,rf=h-1,ci=0,cf=w-1;
        rol=0;
        col=0;
        int yi,yf,xi,xf;
        yi=rol-r;
        yf=rol+r;
        xi=col-r;
        xf=col+r;
        if(yi<0) yi=0;
        if(yf>=h) yf=h-1;
        if(xi<0) xi=0;
        if(xf>=w) xf=w-1;
        for(y=yi;y<=yf;y++){
            for(x=xi;x<=xf;x++){
                hist.addData(pixels[y][x]);
            }
        }
        int sign=1;
        int pixels0[][]=new int[h][w];
        int xt;
        while(true) {
            pixels0[rol][col]=(int)hist.getPercentileValue();
            col+=sign;
            while(col>=0&&col<w){
                yi=rol-r;
                yf=rol+r;
                xi=col-sign*(r+1);
                xf=col+sign*r;
                if(yi<0) yi=0;
                if(yf>=h) yf=h-1;
                for(y=yi;y<=yf;y++){
                    if(xf<w&&xf>=0) hist.addData(pixels[y][xf]);
                    if(xi>=0&&xi<w) hist.removeData(pixels[y][xi]);
                }
                pixels0[rol][col]=(int)hist.getPercentileValue();
                col+=sign;
            }
            rol++;
            if(rol>=h) break;
            col-=sign;
            sign*=-1;

            yi=rol-r-1;
            yf=rol+r;
            xi=col-sign*(r+1);
            xf=col+sign*r;
            if(xi>xf){
                xt=xi;
                xi=xf;
                xf=xt;
            }
            if(xi<0) xi=0;
            if(xf>=w) xf=w-1;
            for(x=xi;x<=xf;x++){
                if(yf<h) hist.addData(pixels[yf][x]);
                if(yi>=0) hist.removeData(pixels[yi][x]);
            }
        }
        CommonMethods.setPixels(impl, pixels0);
        return 1;
    }
/*    public static int getSurroundingMean(ImagePlus impl, int[][] surroundingMean, int rin, int rou){//inner radius and outer radius, to be implemented 3/31/2010
        Histogram hist=new Histogram();
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        double dPixelRange[]=new double[2];
        CommonMethods.getPixelValueRange_Stack(impl, dPixelRange);
        double dPer=0.5;
        hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
        hist.setPercentile(dPer);
        int r=rou,col,rol,x,y;
        int ri=0,rf=h-1,ci=0,cf=w-1;
        rol=0;
        col=0;
        int yi,yf,xi,xf;
        yi=rol-r;
        yf=rol+r;
        xi=col-r;
        xf=col+r;
        if(yi<0) yi=0;
        if(yf>=h) yf=h-1;
        if(xi<0) xi=0;
        if(xf>=w) xf=w-1;
        for(y=yi;y<=yf;y++){
            for(x=xi;x<=xf;x++){
                hist.addData(pixels[y][x]);
            }
        }
        int sign=1;
        int pixels0[][]=new int[h][w];
        int xt;
        while(true) {
            pixels0[rol][col]=(int)hist.getPercentileValue();
            col+=sign;
            while(col>=0&&col<w){
                yi=rol-r;
                yf=rol+r;
                xi=col-sign*(r+1);
                xf=col+sign*r;
                if(yi<0) yi=0;
                if(yf>=h) yf=h-1;
                for(y=yi;y<=yf;y++){
                    if(xf<w&&xf>=0) hist.addData(pixels[y][xf]);
                    if(xi>=0&&xi<w) hist.removeData(pixels[y][xi]);
                }
                pixels0[rol][col]=(int)hist.getPercentileValue();
                col+=sign;
            }
            rol++;
            if(rol>=h) break;
            col-=sign;
            sign*=-1;

            yi=rol-r-1;
            yf=rol+r;
            xi=col-sign*(r+1);
            xf=col+sign*r;
            if(xi>xf){
                xt=xi;
                xi=xf;
                xf=xt;
            }
            if(xi<0) xi=0;
            if(xf>=w) xf=w-1;
            for(x=xi;x<=xf;x++){
                if(yf<h) hist.addData(pixels[yf][x]);
                if(yi>=0) hist.removeData(pixels[yi][x]);
            }
        }
        CommonMethods.setPixels(impl, pixels0);
        return 1;
    }*/
    public static void pixelSubtraction(ImagePlus impl, ImagePlus implb){

        int type=impl.getType();
        int h=impl.getHeight(),w=impl.getWidth();
        int len=w*h;
        int i;
        int pn=0;
        int pixels[]=new int[len];
//        impl.setSlice(nCurrentSlice);
        switch (type){
            case GRAY8:
                byte[] pixels0=(byte[]) impl.getProcessor().getPixels();
                byte[] pixels0b=(byte[]) implb.getProcessor().getPixels();
                int pixel;
                for(i=0;i<len;i++){
                    pixel=pixels0[i]-pixels0b[i];
                    if(pixel<pn) pn=pixel;
                    pixels[i]=pixel;
                }
                for(i=0;i<len;i++){
                    pixels0[i]=(byte)(pixels[i]-pn);
                }
                break;
            case GRAY16:
                short[] pixels1=(short[]) impl.getProcessor().getPixels();
                short[] pixels1b=(short[]) implb.getProcessor().getPixels();
                for(i=0;i<len;i++){
                    pixel=pixels1[i]-pixels1b[i];
                    if(pixel<pn) pn=pixel;
                    pixels[i]=pixel;
                }
                for(i=0;i<len;i++){
                    pixels1[i]=(short)(pixels[i]-pn);
                }
                break;
            case COLOR_256:
                pixels0=(byte[]) impl.getProcessor().getPixels();
                pixels0b=(byte[]) implb.getProcessor().getPixels();
                for(i=0;i<len;i++){
                    pixel=pixels0[i]-pixels0b[i];
                    if(pixel<pn) pn=pixel;
                    pixels[i]=(byte)pixel;
                }
                for(i=0;i<len;i++){
                    pixels0[i]=(byte)(pixels[i]-pn);
                }
                break;
            case GRAY32:
                float[] pixelsf=(float[]) impl.getProcessor().getPixels();
                float[] pixels5=(float[]) impl.getProcessor().getPixels();
                float[] pixels5b=(float[]) implb.getProcessor().getPixels();
                float pixelf,pnf=0;
                for(i=0;i<len;i++){
                    pixelf=pixels5[i]-pixels5b[i];
                    if(pixelf<pnf) pnf=pixelf;
                    pixels5[i]=pixelf;
                }
                for(i=0;i<len;i++){
                    pixels5[i]=(pixelsf[i]-pnf);
                }
                break;
            case COLOR_RGB:
                int r,g,b,rn=0,gn=0,bn=0;
                int[] pixels7=(int[]) impl.getProcessor().getPixels();
                int[] pixels7b=(int[]) implb.getProcessor().getPixels();
                len=pixels7.length;
                int rs[]=new int[len],gs[]=new int[len],bs[]=new int[len];
                int pixelb;
                for(i=0;i<len;i++){
                    pixel=pixels7[i];
                    pixelb=pixels7b[i];
                    r=0xff&(pixel>>16)-0xff&(pixelb>>16);
                    if(r<rn) rn=r;
                    g=0xff&(pixel>>8)-0xff&(pixelb>>8);
                    if(g<gn) gn=g;
                    b=0xff&(pixel)-0xff&(pixelb);
                    if(b<bn) bn=b;
                    rs[i]=r;
                    gs[i]=g;
                    bs[i]=b;
                }
                for(i=0;i<len;i++){
                    pixels7[i]=((rs[i]-rn)<<16)|((gs[i]-gn)<<8)|((bs[i]-bn));
                }
                break;
            default:
                IJ.error("Unsurported image type for getPixelValue");
                break;
        }
    }
    public static Histogram getPixelValueHistogram(ImagePlus impl){
        Histogram hist=new Histogram();
        int[] pixelRange=new int[2];
        int w=impl.getWidth(),h=impl.getHeight();
        int[][]pixels=new int[h][w];
        getPixelValue(impl,impl.getCurrentSlice(),pixels,pixelRange);
        hist.update(0,pixelRange[0]-10, pixelRange[1]+10, 1);
        int i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                hist.addData(pixels[i][j]);
            }
        }
        return hist;
    }
    public static double log10(double dx){
        return Math.log(dx)/Constants.log10;
    }
    public static double exp10(double dx){
        return Math.exp(dx*Constants.log10);
    }
    public static int getPixelPercentile_ImageShape(ImagePlus impl, double dPercentile, ImageShape shape){
        int colI=0, colF=impl.getWidth()-1,rowI=0, rowF=impl.getHeight()-1;
        return getPixelPercentile_ImageShape(impl,dPercentile,shape,colI,colF,rowI,rowF);
    }
    public static int getPixelPercentile_ImageShape(ImagePlus impl, double dPercentile, ImageShape shape, int colI, int colF, int rowI, int rowF){//each pixel value will be replaced by the percentile with the shape
        Histogram hist=new Histogram();
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        double dPixelRange[]=new double[2];
        CommonMethods.getPixelValueRange_Stack(impl, dPixelRange);
        hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
        hist.setPercentile(dPercentile);
        shape.setLocation(new Point(rowI,colI));
        ArrayList <Point> aInnerPoints=new ArrayList();
        shape.setCenter(new Point(rowI,colI));
        shape.getInnerPoints( aInnerPoints);
        ArrayList <Point> oldPoints=new ArrayList(), newPoints=new ArrayList();
        int len=aInnerPoints.size();
        Point p;
        int i,pixel;
        for(i=0;i<len;i++){
            p=aInnerPoints.get(i);
            pixel=pixels[p.y][p.x];
            hist.addData(pixel);
        }

        int sign=1,len1,len2;
        int pixels0[][]=new int[h][w];
        int dx,dy;

        p=new Point();
        Point p0=new Point(),pt;
        p.x=colI;
        p.y=rowI;

        p0.setLocation(p);
        while(p.y>=rowI&&p.y<=rowF){
            while(p.x>=colI&&p.x<=colF){
                dx=p.x-p0.x;
                dy=p.y-p0.y;
                shape.getInnerPointReplacement(dx, dy, newPoints, oldPoints);
                shape.setCenter(p);
                len1=newPoints.size();
                len2=oldPoints.size();

                for(i=0;i<len1;i++){
                    pt=newPoints.get(i);
                    pixel=pixels[pt.y][pt.x];
                    hist.addData(pixel);
                }

                for(i=0;i<len2;i++){
                    pt=oldPoints.get(i);
                    pixel=pixels[pt.y][pt.x];
                    hist.removeData(pixel);
                }

                pixels0[p.y][p.x]=(int) hist.getPercentileValue();
                p0.setLocation(p);
                p.translate(sign, 0);
            }
            p.translate(-sign, 1);
            sign*=-1;
        }

        int h1=rowF-rowI+1;
        int w1=colF-colI+1;
        CommonMethods.setPixels(impl, pixels0,colI,rowI,w1,h1);
        return 1;
    }
    public static int getPixelMean_ImageShape(ImagePlus impl, double dPercentile, ImageShape shape){
        int colI=0, colF=impl.getWidth()-1,rowI=0, rowF=impl.getHeight()-1;
        return getPixelMean_ImageShape(impl,dPercentile,shape,colI,colF,rowI,rowF);
    }
    public static int getPixelMean_ImageShape(ImagePlus impl, double dPercentile, ImageShape shape, int colI, int colF, int rowI, int rowF){
        Histogram hist=new Histogram();
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        double dPixelRange[]=new double[2];
        CommonMethods.getPixelValueRange_Stack(impl, dPixelRange);
        hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
        hist.setPercentile(dPercentile);
        shape.setLocation(new Point(rowI,colI));
        ArrayList <Point> aInnerPoints=new ArrayList();
        shape.setCenter(new Point(rowI,colI));
        shape.getInnerPoints( aInnerPoints);
        ArrayList <Point> oldPoints=new ArrayList(), newPoints=new ArrayList();
        int len=aInnerPoints.size();
        Point p;
        int i,pixel;
        for(i=0;i<len;i++){
            p=aInnerPoints.get(i);
            pixel=pixels[p.y][p.x];
            hist.addData(pixel);
        }

        int sign=1,len1,len2;
        int pixels0[][]=new int[h][w];
        int dx,dy;

        p=new Point();
        Point p0=new Point(),pt;
        p.x=colI;
        p.y=rowI;

        p0.setLocation(p);
        while(p.y>=rowI&&p.y<=rowF){
            while(p.x>=colI&&p.x<=colF){
                dx=p.x-p0.x;
                dy=p.y-p0.y;
                shape.getInnerPointReplacement(dx, dy, newPoints, oldPoints);
                shape.setCenter(p);
                len1=newPoints.size();
                len2=oldPoints.size();

                for(i=0;i<len1;i++){
                    pt=newPoints.get(i);
                    pixel=pixels[pt.y][pt.x];
                    hist.addData(pixel);
                }

                for(i=0;i<len2;i++){
                    pt=oldPoints.get(i);
                    pixel=pixels[pt.y][pt.x];
                    hist.removeData(pixel);
                }

                pixels0[p.y][p.x]=(int) hist.getMean();
                p0.setLocation(p);
                p.translate(sign, 0);
            }
            p.translate(-sign, 1);
            sign*=-1;
        }

        int h1=rowF-rowI+1;
        int w1=colF-colI+1;
        CommonMethods.setPixels(impl, pixels0,colI,rowI,w1,h1);
        return 1;
    }
    public static Histogram getDefaultHistogram(ImagePlus impl){
        int pixels0[][]=getPixelValues(impl);
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        double dRange[]=new double[2];
        getValueRange(w,h,pixels0, new intRange(0,w-1), new intRange(0,h-1), dRange);
        Histogram hist=new Histogram();
        hist.update(0, dRange[0], dRange[1], 1);
        return hist;
    }
    public static Histogram getDefaultHistogram(int[][] intValues){
        int w=intValues[0].length,h=intValues.length;
        double dRange[]=new double[2];
        getValueRange(w,h,intValues, new intRange(0,w-1), new intRange(0,h-1), dRange);
        Histogram hist=new Histogram();
        hist.update(0, dRange[0], dRange[1], 1);
        return hist;
    }
    public static int[][] getPixelValus_LocalReference(ImagePlus impl, ImageShape shape, int nRefType){
        int pixels0[][]=getPixelValues(impl);
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        ImageScanner imsc=new ImageScanner(new intRange(0,w-1), new intRange(0,h-1), new intRange(0,w-1), new intRange(0,h-1),shape);
        Point p;
        double dRange[]=new double[2];
        getValueRange(w,h,pixels0, new intRange(0,w-1), new intRange(0,h-1), dRange);
        Histogram hist=new Histogram();
        hist.update(0, dRange[0], dRange[1], 1);
        ArrayList<Point> newPoints=new ArrayList();
        ArrayList<Point> oldPoints=new ArrayList();

        PointArray paNew=new PointArray(), paOld=new PointArray();
        p=imsc.getPosition();
        double ref=0;

        while(true){
            p=imsc.getPosition();
            imsc.getPoints(paNew, paOld);
            newPoints=paNew.m_pointArray;
            oldPoints=paOld.m_pointArray;
            addValuesToHistogram(pixels0,newPoints,hist);
            removeValuesFromHistogram(pixels0,oldPoints,hist);
            if(nRefType==export_AutoCorrelation.localMeanRef)
                ref=hist.getMean();
            else if(nRefType==export_AutoCorrelation.localMedianRef)
                ref=hist.getPercentileValue();
            else
                ref=0;
            pixels[p.y][p.x]=pixels0[p.y][p.x]-(int)ref;
//            p0.setLocation(p);
            if(imsc.done()) break;
            imsc.move();
        }
        return pixels;
    }
    public static void getValueRange(ArrayList<Double> dvData, double[] pdRange){
        int len=dvData.size();
        double dn=Double.POSITIVE_INFINITY, dx=Double.NEGATIVE_INFINITY,dv;
        for(int i=0;i<len;i++){
            dv=dvData.get(i);
            if(dv<dn) dn=dv;
            if(dv>dx) dx=dv;
        }
        pdRange[0]=dn;
        pdRange[1]=dx;
    }
    public static int getMostProbablePixelValue(ImagePlus impl, int nWS){
        Histogram hist=getPixelValueHistogram(impl);
        hist.smoothHistogram(nWS);
        HistogramHandler hh=new HistogramHandler();
        hh.update(hist);
        int index=hh.getMainPeak();
        int pixel=(int)hist.getPosition(index);
        return pixel;
    }
    public static int getMostProbablePixelValue(int[][] pixels, int nWS){
        Histogram hist=CommonStatisticsMethods.getHistogram(pixels);
        hist.smoothHistogram(nWS);
        HistogramHandler hh=new HistogramHandler();
        hh.update(hist);
        int index=hh.getMainPeak();
        int pixel=(int)hist.getPosition(index);
        return pixel;
    }
    public static void getSmallNumbers(double pd[][],ArrayList<Double> doubles, double dv){
        int rows=pd.length, cols=pd[0].length;
        int i,j;
        double dx;
        for(i=0;i<rows;i++){
            for(j=0;j<cols;j++){
                dx=pd[i][j];
                if(dx<=dv) doubles.add(dx);
            }
        }
    }
    public static void getSmallNumbers(int[][] pd, ArrayList <Double> doubles, double dv){
        int rows=pd.length, cols=pd[0].length;
        int i,j;
        double dx;
        for(i=0;i<rows;i++){
            for(j=0;j<cols;j++){
                dx=pd[i][j];
                if(dx<=dv) doubles.add(dx);
            }
        }
    }
    public static void symmetrizeNumbers(ArrayList<Double> doubles, double center, double eps){
        int len=doubles.size();
        double dx;
        for(int i=0;i<len;i++){
            dx=doubles.get(i)-center;
            if(Math.abs(dx)<eps) continue;
            doubles.add(center-dx);
        }
    }
    public static int getRandomSamples(ArrayList<Double> dvData, ArrayList<Double> dvSamples, int nSize){
        int len=dvData.size();
        int i,n,index;
        double dt;
        if(len<=nSize){
            for(i=0;i<len;i++){
                dvSamples.add(dvData.get(i));
            }
            return -1;
        }
        for(i=0;i<nSize;i++){
            n=len-i;
            index=(int)(n*Math.random())+i;
            if(index>=len){//in case Math.random produce 0 or 1 by bug)
                continue;
            }
            dt=dvData.get(index);
            dvSamples.add(dt);
            dvData.set(index, dvData.get(i));
            dvData.set(i, dt);
        }
        return 1;
    }
    public static int getRandomSamples(ArrayList<Double> dvData, double[] pdSamples, int nSize){
        int len=dvData.size();
        int i,n,index;
        double dt;
        if(len<=nSize){
            for(i=0;i<len;i++){
                pdSamples[i]=dvData.get(i);
            }
            return -1;
        }
        for(i=0;i<nSize;i++){
            n=len-i;
            index=(int)(n*Math.random())+i;
            if(index>=len){//in case Math.random produce 0 or 1 by bug)
                continue;
            }
            dt=dvData.get(index);
            pdSamples[i]=dt;
            dvData.set(index, dvData.get(i));
            dvData.set(i, dt);
        }
        return 1;
    }
    public static ImagePlus getReducedImage(ImagePlus impl0, int nFactor){//sampling a pixel in every square of nFactor by nFactor
        int[][] pixels0=getPixelValues(impl0);
        int[][] pixels=CommonStatisticsMethods.dataReduction(pixels0, nFactor, nFactor);
        return newImage(pixels,impl0.getType());
    }
    public static int[][]getPixelValues(ImagePlus impl){
        int h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels=new int[h][w];
        getPixelValue(impl,impl.getCurrentSlice(),pixels);
        return pixels;
    }
    public static ImagePlus newImage(int[][] pixels, int nType){
        int h=pixels.length,w=pixels[0].length;
        ImagePlus impl=getBlankImage(nType,w,h);
        setPixels(impl,pixels);
        return impl;
    }
    public static int reduceDataSize_distr(ArrayList <Double> dvData0, ArrayList <Double> dvData, int len, double delta){
        int len0=dvData0.size();
        if(len0<=len) return -1;
        Histogram hist=new Histogram();
        double[] dRange=new double[2];
        getDataRange(dvData0,dRange);
        hist.update(0, dRange[0], dRange[1], delta);
        hist.addData(dvData0);
        hist.scale((double)len/(double)len0);

        int i;
        double dx;
        for(i=0;i<len0;i++){
            dx=dvData0.get(i);
            if(hist.removeData(dx)<0) continue;
            dvData.add(dx);
        }
        return 1;
    }
    public static void fillHistograms(int[][] pnIntValues, ArrayList<ImageShape> shapes, Point center, ArrayList<Histogram> hists){
        fillHistograms(pnIntValues, 1, shapes, center, hists);
    }
    public static void fillHistograms(int[][] pnIntValues, ArrayList<ImageShape> shapes, Point center, ArrayList<Histogram> hists, int[][]stamp, int nExcludingType){
        fillHistograms(pnIntValues, 1, shapes, center, hists, stamp, nExcludingType);
    }
    public static void fillHistograms(int[][] pnIntValues, int nRef, ArrayList<ImageShape> shapes, Point center, ArrayList<Histogram> hists){
        int len=shapes.size(),len1,i,j;
        ImageShape shape;
        ArrayList<Point> pts=new ArrayList();
        Histogram hist;
        Point pt;
        for(i=0;i<len;i++){
            shape=shapes.get(i);
            shape.setCenter(center);
            shape.getInnerPoints(pts);
            hist=hists.get(i);
            hist.clearHist();
            addValuesToHistogram(pnIntValues,nRef,pts,hist);
        }
    }
    public static void fillHistograms(int[][] pnIntValues, int nRef, ArrayList<ImageShape> shapes, Point center, ArrayList<Histogram> hists, int[][]stamp, int nExcludingType){
        int len=shapes.size(),len1,i,j;
        ImageShape shape;
        ArrayList<Point> pts=new ArrayList();
        Histogram hist;
        Point pt;
        for(i=0;i<len;i++){
            shape=shapes.get(i);
            shape.setCenter(center);
            shape.getInnerPoints(pts);
            hist=hists.get(i);
            hist.clearHist();
            addValuesToHistogram(pnIntValues,nRef,pts,hist,stamp,nExcludingType);
        }
    }
    public static void fillHistogram(int[][] pnIntValues, ImageShape shape, Point center, Histogram hist){
        int nRef=0;
        fillHistogram(pnIntValues,nRef,shape,center,hist);
    }
    public static void fillHistogram(int[][] pnIntValues, ImageShape shape, Point center, Histogram hist, int[][]stamp, int nExcludingType){
        int nRef=0;
        fillHistogram(pnIntValues,nRef,shape,center,hist,stamp,nExcludingType);
    }
    public static void fillHistogram(int[][] pnIntValues, int nRef, ImageShape shape, Point center, Histogram hist){
        ArrayList<Point> pts=new ArrayList();
        Point pt;
        shape.setCenter(center);
        shape.getInnerPoints(pts);
        hist.clearHist();
        addValuesToHistogram(pnIntValues,nRef,pts,hist);
    }
    public static void fillHistogram(int[][] pnIntValues, int nRef, ImageShape shape, Point center, Histogram hist, int[][]stamp, int nExcludingType){
        ArrayList<Point> pts=new ArrayList();
        Point pt;
        shape.setCenter(center);
        shape.getInnerPoints(pts);
        hist.clearHist();
        addValuesToHistogram(pnIntValues,nRef,pts,hist,stamp,nExcludingType);
    }
    public static void updateHistograms(int[][] pnIntValues, ArrayList<ImageShape> shapes, Point center0, Point center, ArrayList<Histogram> hists){
//        int rows=pnIntValues.length,cols=pnIntValues[0].length;
//        int c,r;
        int len=shapes.size(),len1,i,j,dx=center.x-center0.x,dy=center.y-center0.y;
        ImageShape shape;
        ArrayList<Point> newPoints=new ArrayList();
        ArrayList<Point> oldPoints=new ArrayList();
        Histogram hist;
        Point pt;
        for(i=0;i<len;i++){
            shape=shapes.get(i);
            shape.setCenter(center0);
            shape.getInnerPointReplacement(dx, dy, newPoints, oldPoints);
            hist=hists.get(i);
            addValuesToHistogram(pnIntValues,newPoints,hist);
            removeValuesFromHistogram(pnIntValues,oldPoints,hist);
        }
    }
    public static void addValuesToHistogram(int[][] pnIntValues, ArrayList<Point> pts, Histogram hist){
        int nRef=0;
        addValuesToHistogram(pnIntValues,nRef,pts,hist);
    }
    public static void addValuesToHistogram(int[][] pnIntValues, ArrayList<Point> pts, Histogram hist, int[][]stamp, int nExcludingType){
        int nRef=0;
        addValuesToHistogram(pnIntValues,nRef,pts,hist,stamp,nExcludingType);
    }
    public static void addValuesToHistogram(int[][] pnIntValues, int nRef, ArrayList<Point> pts, Histogram hist){
        int len=pts.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=pts.get(i);
            hist.addData(pnIntValues[pt.y][pt.x]-nRef);
        }
    }
    public static void addValuesToHistogram(int[][] pnIntValues, int nRef, ArrayList<Point> pts, Histogram hist, int[][]stamp, int nExcludingType){
        int len=pts.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=pts.get(i);
            if(stamp[pt.y][pt.x]==nExcludingType) continue;
            hist.addData(pnIntValues[pt.y][pt.x]-nRef);
        }
    }
    public static void removeValuesFromHistogram(int[][] pnIntValues, ArrayList<Point> pts, Histogram hist){
        int nRef=0;
        removeValuesFromHistogram(pnIntValues,nRef,pts,hist);
    }
    public static void removeValuesFromHistogram(int[][] pnIntValues, int nRef, ArrayList<Point> pts, Histogram hist){
        int len=pts.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=pts.get(i);
            hist.removeData(pnIntValues[pt.y][pt.x]-nRef);
        }
    }
    public static ImagePlus displayPixels(int[][] pixels, String title, int type){
        final ImagePlus impl;
        int h=pixels.length,w=pixels[0].length;
        int nMin=CommonStatisticsMethods.getMin(pixels);
        if(nMin<0){
            CommonStatisticsMethods.addToElements(pixels, -nMin);
            impl=CommonMethods.newImage(pixels, type);
            CommonStatisticsMethods.addToElements(pixels, nMin);
        }else{
            impl=CommonMethods.newImage(pixels, type);
        }
        title+="minPixel: "+PrintAssist.ToString(nMin);
        impl.setTitle(title);
        SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    impl.show();
                }
            });
        return impl;
    }

    public static void labelPointsInOrder(ImagePlus impl, String title, ArrayList <Point> points, int indexI, int indexF, ImageShape shape, int dx, int dy){
        ImagePlus implt=CommonMethods.copyToRGBImage(impl);
        int[] pixels=(int[])implt.getProcessor().getPixels();
        int index,pixel,r,g,b=0;
        Point p,pt=new Point();
        for(index=indexI;index<=indexF;index++){
            p=points.get(index);
            pt.setLocation(p);
            pt.translate(dx, dy);
            shape.setCenter(pt);
            r=(int)(CommonMethods.interpolation(indexI, 255, indexF, 0, index)+0.5);
            g=(int)(CommonMethods.interpolation(indexI, 0, indexF, 255, index)+0.5);
            pixel=0;
            pixel=(r<<16)|(g<<8)|(b);
            ImageShapeHandler.markShape(implt, shape, pixel);
        }
        implt.setTitle(title);
        implt.show();
    }
    public static void labelPointsInOrder(ImagePlus impl, String title, ArrayList <Point> points, int[] percentileIndexes, ImageShape shape, int nf, int dx, int dy){
//        ImagePlus implt=CommonMethods.copyToRGBImage(impl);
        ImagePlus implt=impl;
        int index,pixel,r,g,b=0,len=percentileIndexes.length,i,indexI,indexF;
        Point p,pt=new Point();
        indexF=points.size()-1;
        for(i=0;i<=nf;i++){
            indexI=percentileIndexes[i];
            r=(int)(CommonMethods.interpolation(0, 255, nf, 0, i)+0.5);
            g=(int)(CommonMethods.interpolation(0, 0, nf, 255, i)+0.5);
            b=i;
            pixel=0;
            pixel=(r<<16)|(g<<8)|(b);
            for(index=indexI;index<=indexF;index++){
                p=points.get(index);
                pt.setLocation(p);
                pt.translate(dx, dy);
                shape.setCenter(pt);
                ImageShapeHandler.markShape(implt, shape, pixel);
            }
            indexF=indexI-1;
        }
        implt.setTitle(title);
        implt.show();
    }
    public static void swapPoints(ArrayList<Point> points, int i, int j){
        Point pt=new Point(points.get(i));
        points.get(i).setLocation(points.get(j));
        points.get(j).setLocation(pt);
    }
    public static void swapDoubleElements(ArrayList<Double> dv, int i, int j){
        Double dt=new Double(dv.get(i));
        dv.set(i, dv.get(j));
        dv.set(j, dt);
    }
    public static int[][] getElementRanking(int[][] pnInts, int pnRange[]){
        int rows=pnInts.length,cols=pnInts[0].length;
        int len=rows*cols;
        double pdV[]=new double[len];
        int pnIndexes[]=new int[len];
        int r,c,index=0;

        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                pdV[index]=pnInts[r][c];
                pnIndexes[index]=index;
                index++;
            }
        }

        utilities.QuickSort.quicksort(pdV, pnIndexes);
        int nRank=0;
        int it, it0=(int)(pdV[0]+0.5);

        int[][] pnRank=new int[rows][cols];
        index=0;
        for(int i=0;i<len;i++){
            it=(int)(pdV[i]+0.5);
            if(it>it0) {
                nRank++;
                it0=it;
            }
            index=pnIndexes[i];
            r=index/cols;
            c=index-r*cols;
            pnRank[r][c]=nRank;
        }
        return pnRank;
    }
    public static void GaussianBlur(ImagePlus impl, float xSigma, float ySigma, float fAccuracy){
        GaussianBlur gb=new GaussianBlur();
        int num,i;
        num=impl.getStackSize();
        for(i=0;i<num;i++){
            impl.setSlice(i+1);
            gb.blurGaussian(impl.getProcessor(), xSigma, ySigma, fAccuracy);
        }
    }

    public static ImageProcessor newProcessor(int w, int h, int type){
        ImageProcessor impr;
        int len=w*h;

        switch (type){
            case GRAY8:
                byte[] pixels=new byte[len];
                impr=new ByteProcessor(w,h,pixels,null);
                break;
            case GRAY16:
                short[] pixels2=new short[len];
                ColorModel cm=null;
                impr=new ShortProcessor(w,h,pixels2,cm);
                break;
            case COLOR_256:
                byte[] pixels4=new byte[len];
                impr=new ByteProcessor(w,h,pixels4,null);
                break;
            case GRAY32:
                float[] pixels6=new float[len];
                impr=new FloatProcessor(w,h,pixels6,null);
                break;
            case COLOR_RGB:
                int[] pixels8=new int[len];
                impr=new ColorProcessor(w,h,pixels8);
                break;
            default:
                pixels8=new int[len];

                impr=new ColorProcessor(w,h,pixels8);
                break;
        }
        return impr;
    }

    public static ImagePlus getNewImage(int w, int h, int stackSize, int type){
        ImagePlus impl;
        ImageStack is=new ImageStack(w,h);
        ImageProcessor impr;
        for(int i=0;i<stackSize;i++){
            impr=newProcessor(w,h,type);
            is.addSlice("", impr);
        }
        impl=new ImagePlus("",is);
        return impl;
    }
    public static ImagePlus resizedImage_cropping(ImagePlus impl0, int w, int h){
        ImagePlus impl;
        int type=impl0.getType();
        int w0=impl0.getWidth(),h0=impl0.getHeight();
        int nSlices=impl0.getStackSize();
        int i,j;
        int[][]pixels0=new int[h0][w0];
        int [][]pixels;
        impl=getNewImage(w,h,nSlices,type);
        for(i=0;i<nSlices;i++){
            impl0.setSlice(i+1);
            impl.setSlice(i+1);
            CommonMethods.getPixelValue(impl0, i+1, pixels0);
            pixels=getCroppedIntArray(pixels0,w,h);
            CommonMethods.setPixels(impl, pixels);
        }
        return impl;
    }
    public static int[][] getCroppedIntArray(int[][]pixels0, int w, int h){
        int[][] pixels=new int[h][w];
        int h0=pixels0.length,w0=pixels0[0].length;
        int i,j,i0,j0,pixel;
        for(i=0;i<h;i++){
            i0=i%h0;
            for(j=0;j<w;j++){
                j0=j%w0;
                pixel=pixels0[i0][j0];
                pixels[i][j]=pixel;
            }
        }
        return pixels;
    }
    public static void sortArrays(ArrayList<Point> points, ArrayList<Double> dv1, ArrayList<Double> dv2, int indexI, int indexF){
        int len=indexF-indexI+1;
        int[] indexes=new int[len];
        double[] pd=new double[len];

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=dv2.get(i+indexI);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);
        rearrangePointArray(points,indexes,indexI,indexF);
        rearrangeDoubleArray(dv1,indexes,indexI,indexF);
        rearrangeDoubleArray(dv2,indexes,indexI,indexF);
    }

    public static void sortArrays(ArrayList<Point> points, ArrayList<Double>[] pdv, int indexI, int indexF, int sortingIndex){
        int len=indexF-indexI+1;
        int[] indexes=new int[len];
        double[] pd=new double[len];

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=pdv[sortingIndex].get(i+indexI);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);
        rearrangePointArray(points,indexes,indexI,indexF);
        len=pdv.length;
        for(i=0;i<len;i++){
            rearrangeDoubleArray(pdv[i],indexes,indexI,indexF);
        }
    }

    public static void sortArrays(ArrayList<Double> dv0, ArrayList<Double> dv1, int[] indexes, int sortingIndex){
        int len=indexes.length;
        double[] pd=new double[len];
        ArrayList<Double> dv=dv0;
        if(sortingIndex==1) dv=dv1;

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=dv.get(i);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);
    }
    public static void sortArrays(ArrayList<Double> dv0, int[] indexes, int[] pnRanking){
        int len=indexes.length;
        double[] pd=new double[len];
        ArrayList<Double> dv=dv0;

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=dv.get(i);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);

        int index,nRank;
        for(nRank=0;nRank<len;nRank++){
            index=indexes[nRank];
            pnRanking[index]=nRank;
        }
    }

    public static void sortArrays(ArrayList<Double> dv0, int[] indexes, int[] pnRanking, int indexI, int indexF){
        int len=indexes.length;
        double[] pd=new double[len];
        ArrayList<Double> dv=dv0;

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=dv.get(i);
            indexes[i]=i;
        }
        QuickSort.quicksort_external(pd, indexes, indexI, indexF);

        int index,nRank;
        for(nRank=indexI;nRank<=indexF;nRank++){
            index=indexes[nRank];
            pnRanking[index]=nRank-indexI;
        }
    }

    public static void sortArrays(ArrayList<MeanSem0[]> cvMS, ArrayList<Point> points, ArrayList<Double>[] pdv, int indexI, int indexF, int sortingIndex){
        int len=indexF-indexI+1;
        int[] indexes=new int[len];
        double[] pd=new double[len];

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=pdv[sortingIndex].get(i+indexI);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);
        rearrangePointArray(points,indexes,indexI,indexF);
        rearrangeMeansem0PointerArray(cvMS,indexes,indexI,indexF);
        len=pdv.length;
        for(i=0;i<len;i++){
            rearrangeDoubleArray(pdv[i],indexes,indexI,indexF);
        }
    }

    public static void sortArrays(ArrayList<MeanSem0[]> cvMS,ArrayList<Point> points, ArrayList<Double> dv1, ArrayList<Double> dv2, int indexI, int indexF){
        int len=indexF-indexI+1;
        int[] indexes=new int[len];
        double[] pd=new double[len];

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=dv2.get(i+indexI);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);
        rearrangePointArray(points,indexes,indexI,indexF);
        rearrangeDoubleArray(dv1,indexes,indexI,indexF);
        rearrangeDoubleArray(dv2,indexes,indexI,indexF);
        rearrangeMeansem0PointerArray(cvMS,indexes,indexI,indexF);
    }
    public static void sortArrays(ArrayList<MeanSem0[]> cvMS,ArrayList<Point> points, ArrayList<Double> dv1, ArrayList<Double> dv2, ArrayList<Double> dv3, int indexI, int indexF){
        int len=indexF-indexI+1;
        int[] indexes=new int[len];
        double[] pd=new double[len];

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=dv3.get(i+indexI);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);
        rearrangePointArray(points,indexes,indexI,indexF);
        rearrangeDoubleArray(dv1,indexes,indexI,indexF);
        rearrangeDoubleArray(dv2,indexes,indexI,indexF);
        rearrangeDoubleArray(dv3,indexes,indexI,indexF);
        rearrangeMeansem0PointerArray(cvMS,indexes,indexI,indexF);
    }
    public static void sortArrays(ArrayList<MeanSem0[]> cvMS, ArrayList<Double> dv2, int indexI, int indexF){
        int len=indexF-indexI+1;
        int[] indexes=new int[len];
        double[] pd=new double[len];

        int i,j;
        for(i=0;i<len;i++){
            pd[i]=dv2.get(i+indexI);
            indexes[i]=i;
        }
        QuickSort.quicksort(pd, indexes);
        rearrangeMeansem0PointerArray(cvMS,indexes,indexI,indexF);
    }
    public static void rearrangePointArray(ArrayList<Point> points, int[] indexes, int indexI, int indexF){//
        int len=indexF-indexI+1,index;
        Point[] pa=new Point[len];
        for(int i=0;i<len;i++){
            index=indexes[i];
            pa[i]=points.get(index+indexI);
        }
        for(int i=0;i<len;i++){
            points.set(i+indexI, pa[i]);
        }
    }
    public static void rearrangeMeansem0PointerArray(ArrayList<MeanSem0[]> cvMS, int[] indexes, int indexI, int indexF){//
        int len=indexF-indexI+1,index;
        int len0=cvMS.get(0).length;
        MeanSem0[][]msa=new MeanSem0[len][len0];
        for(int i=0;i<len;i++){
            index=indexes[i];
            msa[i]=cvMS.get(index+indexI);
        }
        for(int i=0;i<len;i++){
            cvMS.set(i+indexI, msa[i]);
        }
    }
    public static void swapMeansem0Pointers(ArrayList<MeanSem0[]> cvMS, int i, int j){
        MeanSem0[] pcMS=cvMS.get(i);
        cvMS.set(i, cvMS.get(j));
        cvMS.set(j, pcMS);
    }
    public static void rearrangeDoubleArray(ArrayList<Double> dv, int[] indexes, int indexI, int indexF){
        int len=indexF-indexI+1,index;
        double[] pd=new double[len];
        for(int i=0;i<len;i++){
            index=indexes[i];
            pd[i]=dv.get(index+indexI);
        }
        for(int i=0;i<len;i++){
            index=indexes[i];
            dv.set(i+indexI, pd[i]);
        }
    }
    public static ArrayList <Double> getAverageIntensityTrace(ImagePlus impl){
        ArrayList <Double> aves=new ArrayList();
        int num=impl.getNSlices();
        int h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels=new int[h][w];
        int i;
        MeanSem0 ms;
        double ave;
        for(i=1;i<=num;i++){
            impl.setSlice(i);
            CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
            ms=CommonStatisticsMethods.buildMeanSem(pixels);
            ave=ms.mean;
            aves.add(ave);
         }
        return aves;
    }
    public static int getIntesityPeakSliceIndex(ImagePlus impl){
        ArrayList <Double> aves=getAverageIntensityTrace(impl);
        int num=aves.size();
        int index=0;
        double dt, dMax=Double.MIN_VALUE;
        for(int i=0;i<num;i++){
            dt=aves.get(i);
            if(dt>dMax){
                index=i;
                dMax=dt;
            }
        }
        return index;
    }
    public static void setPixelAtSpecialLandsapePoints(int[][]pixels,ArrayList<Integer> types,int pixel){
        int num=types.size(),type;
        ArrayList <Point> specialPoints=new ArrayList();
        int len;
        Point pt;
        for(int id=0;id<num;id++){
            type=types.get(id);
            ArrayList<Point> points=getSpecialLandscapePoints(pixels,type);
            len=points.size();
            for(int i=0;i<len;i++){
                specialPoints.add(points.get(i));
            }
        }
        len=specialPoints.size();
        for(int i=0;i<len;i++){
           pt=specialPoints.get(i);
           pixels[pt.y][pt.x]=pixel;
       }
    }
    public static void setPixelAtSpecialLandsapePoints(int[][]pixels,int[][] stamp,int type, int pixel){
        int i,j,k,h=pixels.length,w=pixels[0].length;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                if(stamp[i][j]==type) pixels[i][j]=pixel;
            }
        }
    }
    public static double[] crossProduct (double[] v0, double[] v1)
    {
        double crossProduct[] = new double[3];

        crossProduct[0] = v0[1] * v1[2] - v0[2] * v1[1];
        crossProduct[1] = v0[2] * v1[0] - v0[0] * v1[2];
        crossProduct[2] = v0[0] * v1[1] - v0[1] * v1[0];

        return crossProduct;
    }
    public static double calAngle(double x1,double y1, double x2, double y2, double x3, double y3){//calculate the angle between vector P1P3 and P1P2. P1 (x1,x2), P2 (x2,y2), P3 (x3,y3)
        double dx3=x3-x1,dy3=y3-y1,dx2=x2-x1,dy2=y2-y1;
        double angle,a;
        
        double s32=CommonMethods.getDist2(x1, y1, x2, y2);   
        double s22=CommonMethods.getDist2(x1, y1, x3, y3);        
        double s12=CommonMethods.getDist2(x3, y3, x2, y2);        
        
        double sign=1;        
        if(((x3-x1)*(y2-y1)-(y3-y1)*(x2-x1))<0) sign=-1;
        
        a=(s22+s32-s12)/(2.*Math.sqrt(s22*s32));
        angle=sign*Math.acos(a);
        return angle;
    }
    public static boolean isOnEdge(int w, int h, Point p){
        int x=p.x,y=p.y;
        if(x==0||x==w-1) return true;
        if(y==0||y==h-1) return true;
        return false;
    }
    public static boolean isOnEdge(int w, int h, int x, int y){
        if(x==0||x==w-1) return true;
        if(y==0||y==h-1) return true;
        return false;
    }
    public static ImagePlus combineImagesX(ImagePlus impl1, ImagePlus impl2){
        int nSlices=impl1.getStackSize();
        if(nSlices!=impl2.getStackSize()){
            IJ.error("combineImagesX is applicable only to two same size image stacks");
        }
        int w1=impl1.getWidth(),w2=impl2.getWidth(),h1=impl1.getHeight(),h2=impl2.getHeight();
        int h=Math.max(h1, h2),w=w1+w2;
        ImagePlus impl=null;
        impl=CommonMethods.getBlankImageStack(impl1.getType(), w, h, nSlices);
        int i,x,y,xn;
        int[][] pixels=new int[h][w];
        int[][] pixels1=new int[h1][w1];
        int[][] pixels2=new int[h2][w2];
        boolean rgbPixels=true;
        for(i=0;i<nSlices;i++){
            impl1.setSlice(i+1);
            impl2.setSlice(i+1);
            CommonMethods.getPixelValue(impl1, i+1, pixels1);
            CommonMethods.getPixelValue(impl2, i+1, pixels2);
            for(y=0;y<h1;y++){
                for(x=0;x<w1;x++){
                    pixels[y][x]=pixels1[y][x];
                }
            }
            for(y=0;y<h2;y++){
                for(x=0;x<w2;x++){
                    xn=x+w1;
                    pixels[y][xn]=pixels2[y][x];
                }
            }
            impl.setSlice(i+1);
            CommonMethods.setPixels(impl, pixels,rgbPixels);
        }
        impl.setTitle("combined image by combineImagesX");
        return impl;
    }
    public static int getRGBPixel(int nV){
        int d=255*255;
        int r=nV/d;
        int g=(nV-r*d)/255;
        int b=nV-r*d-g*255;
        if(r>255) r=255;
        int pixel=(r<<16)|(g<<8)|b;
        return pixel;
    }
    public static boolean equal(ArrayList<Integer> ia1, ArrayList<Integer> ia2){
        int len1=ia1.size(),len2=ia2.size();
        if(len1!=len2) return false;
        int i,j;
        for(i=0;i<len1;i++){
            if(ia1.get(i)!=ia2.get(i)) return false;
        }
        return true;
    }
    public static Object retrieveVariable(Hashtable hTable, String key){
        Object Variable="";
        if(hTable.containsKey(key))
            Variable=hTable.get(key);
        else
            IJ.error("can not find the key word "+key+" in the command line "+ hTable.toString());
        return Variable;
    }
    public static void getWindowPositions(int nStart, int nEnd, int windowSize, int delta, ArrayList<Integer> positions){
        positions.clear();
        for(int position=nStart+windowSize;position<nEnd-windowSize;position+=delta){//delta is the step size of the sliding window
            positions.add(position);
        }
    }
    public static float MachineEpsilonFloat(){
       float machEps = 1.0f;

        do {
           machEps /= 2.0f;
        }
        while ((float)(1.0 + (machEps/2.0)) != 1.0);
       return machEps;
    }
    public static double MachineEpsilonDouble(){//http://en.wikipedia.org/wiki/Machine_epsilon
        /*
        IEEE 754 - 2008
         * single precision: 2^(-24)=5.96e-8
         * double precision: 2^(-53)=1.11e-16
         * quad(ruple) precision: 2^(-113)=9.63e-36
         */
       double machEps = 1.0f;

        do {
           machEps /= 2.0f;
        }
        while ((double)(1.0 + (machEps/2.0)) != 1.0);
       return machEps;
    }
    public static int getNumOfSmallerOrEqualElements(ArrayList<Integer> nvA, int nV){
        int len=nvA.size(),it;
        int id0=0,id1=len-1,id;
        it=nvA.get(id0);
        if(nV<it) return 0;
        if(nV==it) return 1;
        if(nV>=nvA.get(id1)) return len;
        id=(id0+id1)/2;
        while(id>id0){
            if(nV>=nvA.get(id))
                id0=id;
            else
                id1=id;
            id=(id0+id1)/2;
        }
        return id+1;
    }
    public static int getNumOfLargerOrEqualElements(ArrayList<Integer> nvA, int nV){
        int len=nvA.size(),it;
        int id0=0,id1=len-1,id;
        it=nvA.get(id0);
        if(nV>it) return 0;
        if(nV==it) return 1;
        if(nV<=nvA.get(id1)) return len;
        id=(id0+id1)/2;
        while(id>id0){
            if(nV<=nvA.get(id))
                id0=id;
            else
                id1=id;
            id=(id0+id1)/2;
        }
        return id+1;
    }
    public static int getNumOfSmallerOrEqualElements(double[] pdA, double dV){
        int len=pdA.length;
        double dt;
        int id0=0,id1=len-1,id;
        dt=pdA[id0];
        if(dV<dt) return 0;
        if(dV==dt) return 1;
        if(dV>=pdA[id1]) return len;
        id=(id0+id1)/2;
        while(id>id0){
            if(dV>=pdA[id])
                id0=id;
            else
                id1=id;
            id=(id0+id1)/2;
        }
        return id+1;
    }
    public static int getNumOfLargerOrEqualElements(double[] pdA, double dV){
        int len=pdA.length;
        double dt;
        int id0=0,id1=len-1,id;
        dt=pdA[id0];
        if(dV>dt) return 0;
        if(dV==dt) return 1;
        if(dV<=pdA[id1]) return len;
        id=(id0+id1)/2;
        while(id>id0){
            if(dV<=pdA[id])
                id0=id;
            else
                id1=id;
            id=(id0+id1)/2;
        }
        return id+1;
    }
    public static int getNumOfSmallerElements(ArrayList<Double> dvA, double dV, int sortingOrder){//dvA is a sorted arraylist. It is ascending array when sorting order is 1, and a descendint array
        //when sorting Order is -1.
        int len=dvA.size(),i;
        if(len==0) return 0;
        double dn,dx;
        if(sortingOrder>0){
            dn=dvA.get(0);
            dx=dvA.get(len-1);
        }else{
            dn=dvA.get(len-1);
            dx=dvA.get(0);
        }

        if(dV<=dn) return 0;
        if(dV>dx) return len;
        int num=0;
        if(dV==dx) {
            num=len-1;
            for(i=len-2;i>=0;i--){
                if(dvA.get(i)<dV) break;
                num--;
            }
            return num;
        }

        int[] indexes={0,len-1};
        int index0=0,index1=1;
        if(sortingOrder<0){
            index0=1;
            index1=0;
        }

        double dt;
        int id0=indexes[index0],id1=indexes[index1],id;
        dt=dvA.get(id0);
        if(dV<dt) return 0;
        id=(indexes[index0]+indexes[index1])/2;
/*        while(id!=indexes[index0]){//this block doesn't consider the case of degeneracy
            if((dV-dvA.get(id))<0)
                indexes[index1]=id;
            else 
                indexes[index0]=id;
            id=(indexes[index0]+indexes[index1])/2;
        }*/
        double delta;
        while(id!=indexes[index0]){
            delta=dV-dvA.get(id);
            if((delta)<=0)
                indexes[index1]=id;
            else 
                indexes[index0]=id;
            id=(indexes[index0]+indexes[index1])/2;
        }
        if(sortingOrder>0)
            num=indexes[0];
        else
            num=len-indexes[1]-1;

        for(id=indexes[0];id<=indexes[1];id++){
            if(dvA.get(id)<dV) num++;
        }
        return num;
    }
    public static void swapElements(double[] pdA,int i, int j){
        double d=pdA[i];
        pdA[i]=pdA[j];
        pdA[j]=d;
    }
    public static void subpixelWeightedCentroids(int[][] pixels, ArrayList<Point> points, ArrayList<double[]> centroids, int indexI, int indexF){
        Point pt;
        double shiftXY[]=new double[2],centroid[];
        int h=pixels.length,w=pixels[0].length,i;
        for(i=indexI;i<=indexF;i++){
            pt=points.get(i);
            subpixelWeightedCentroid(pixels,pt.x,pt.y,w,h,shiftXY);
            shiftXY[0]+=pt.x;
            shiftXY[1]+=pt.y;
            centroid=centroids.get(i);
            centroid[0]=shiftXY[0];
            centroid[1]=shiftXY[1];
        }
    }
    public static void subpixelWeightedCentroid(int[][] pixels, int x, int y, int w, int h, double[] shiftXY){//x,y is the position of the local maximum
        int z0,z1,z=pixels[y][x];
        if(x>0&&x<w-1){
            z0=pixels[y][x-1];
            z1=pixels[y][x+1];
            shiftXY[0]=subpixelWeightedCentroid_Shift(z0,z,z1);
        }else{
            shiftXY[0]=0;
        }

        if(y>0&&y<h-1){
            z0=pixels[y-1][x];
            z1=pixels[y+1][x];
            shiftXY[1]=subpixelWeightedCentroid_Shift(z0,z,z1);
        }else{
            shiftXY[1]=0;
        }
    }
    public static DoublePair subpixelWeightedCentroid(TwoDFunction fun, int x, int y){//x,y is the position of the local maximum
        double z0,z1;
        double z=fun.func(x, y);
        
        z0=fun.func(x-1, y);
        z1=fun.func(x+1, y);
        double shiftX=subpixelWeightedCentroid_Shift(z0,z,z1);
        if(Double.isInfinite(shiftX)||Double.isNaN(shiftX)) shiftX=0.;
        
        z0=fun.func(x, y-1);
        z1=fun.func(x, y+1);
        double shiftY=subpixelWeightedCentroid_Shift(z0,z,z1);
        if(Double.isInfinite(shiftY)||Double.isNaN(shiftY)) shiftY=0.;
        
        return new DoublePair(x+shiftX,y+shiftY);
    }
    public static void offEdges(int w, int h, Point p){
        if(p.x==0) p.x=1;
        if(p.x==w-1) p.x=w-2;
        if(p.y==0) p.y=1;
        if(p.y==h-1) p.y=h-2;

    }
    public static boolean offBound(int w, int h, int x, int y){
        return (x<0||x>=w||y<0||y>=h);
    }
    
    public static double subpixelWeightedCentroid_Shift(double z0, double z, double z1){
        double shift=0,temp;
        if(z0==z1) return 0;
        if(z0==z) return -0.5;
        if(z1==z) return 0.5;
        double dsign=1,delta0=z-z0,delta1=z-z1;
        if(z0>z1){
            dsign=-1;
            temp=delta1;
            delta1=delta0;
            delta0=temp;
        }
        shift=dsign*CommonMethods.interpolation(0, 0.5, delta0, 0, delta1);
        return shift;
    }
    public static int[] getDefaultRankingIndexes(int len){
        int[] pnIndexes=new int[len];
        for(int i=0;i<len;i++){
            pnIndexes[i]=i;
        }
        return pnIndexes;
    }
    public static void gradientFilter(ImagePlus impl, int[][] pixels, int[][] pixelsgd){
        getPixelValue(impl,impl.getCurrentSlice(),pixels);
        getGradientMap(pixels,pixelsgd);
        CommonMethods.setPixels(impl, pixelsgd);
    }
    public static void getGradientMap(int[][] pixels, int[][] pixelsgd){
        int i,j,w=pixels[0].length,h=pixels.length,pixel;
        double sqrt2=Math.sqrt(2);
        for(i=1;i<h-1;i++){
            for(j=1;j<w-1;j++){
                pixel=Math.abs(pixels[i][j-1]-pixels[i][j+1])+(int)(Math.abs(pixels[i-1][j-1]-pixels[i+1][j+1]))
                        +Math.abs(pixels[i-1][j]-pixels[i+1][j])+(int)(Math.abs(pixels[i-1][j+1]-pixels[i+1][j-1]));
                pixelsgd[i][j]=pixel/4;
            }
        }

        pixelsgd[0][0]=Math.abs(pixels[0][1]-pixels[1][0]);
        pixelsgd[h-1][w-1]=Math.abs(pixels[h-1][w-2]-pixels[h-2][w-1]);
        pixelsgd[0][w-1]=Math.abs(pixels[0][w-2]-pixels[1][w-1]);
        pixelsgd[h-1][0]=Math.abs(pixels[h-1][1]-pixels[h-2][0]);

        for(i=1;i<h-1;i++){
            j=0;
            pixelsgd[i][j]=Math.abs(pixels[i-1][j]-pixels[i+1][j]);
            j=w-1;
            pixelsgd[i][j]=Math.abs(pixels[i-1][j]-pixels[i+1][j]);
        }

        for(j=1;j<w-1;j++){
            i=0;
            pixelsgd[i][j]=Math.abs(pixels[i][j-1]-pixels[i][j+1]);
            i=h-1;
            pixelsgd[i][j]=Math.abs(pixels[i][j-1]-pixels[i][j+1]);
        }
    }
    public static void getGradientMap(int[][] pixels, int[][] pixelsgd,int iI0,int iF0, int jI0, int jF0){
        int i,j,w=pixels[0].length,h=pixels.length,pixel,in,ip,jn,jp;
        double sqrt2=Math.sqrt(2);
        int iI=Math.max(1, iI0);
        int jI=Math.max(1, jI0);
        int iF=Math.min(h-2, iF0);
        int jF=Math.min(w-2, jF0);

        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                pixel=Math.abs(pixels[i][j-1]-pixels[i][j+1])+(int)(Math.abs(pixels[i-1][j-1]-pixels[i+1][j+1]))
                        +Math.abs(pixels[i-1][j]-pixels[i+1][j])+(int)(Math.abs(pixels[i-1][j+1]-pixels[i+1][j-1]));
                pixelsgd[i][j]=pixel/4;
            }
        }
        if(iI0==0){
            if(jI0==0) pixelsgd[0][0]=Math.abs(pixels[0][1]-pixels[1][0]);
            for(j=jI;j<=jF;j++){
                pixelsgd[0][j]=Math.abs(pixels[0][j-1]-pixels[0][j+1]);
            }
            if(jF0==w-1) pixelsgd[0][w-1]=Math.abs(pixels[0][w-2]-pixels[1][w-1]);
        }

        if(iF0==h-1){
            if(jI0==0) pixelsgd[h-1][0]=Math.abs(pixels[h-1][1]-pixels[h-2][0]);
            for(j=jI;j<=jF;j++){
                pixelsgd[h-1][j]=Math.abs(pixels[h-1][j-1]-pixels[h-1][j+1]);
            }
            if(jF0==w-1) pixelsgd[h-1][w-1]=Math.abs(pixels[h-1][w-2]-pixels[h-2][w-1]);
        }

        if(jI0==0){
            for(i=iI;i<=iF;i++){
                pixelsgd[i][0]=Math.abs(pixels[i-1][0]-pixels[i+1][0]);
            }
        }

        if(jF0==w-1){
            for(i=iI;i<=iF;i++){
                pixelsgd[i][w-1]=Math.abs(pixels[i-1][w-1]-pixels[i+1][w-1]);
            }
        }
    }
    public static void excludeProximityOfSpecialLandscapePoints(int[][] stamp, int[][] exclusionStamp, int type0, int r, int exclusionMark, int iI, int iF, int jI, int jF){
        int i,j,index,type;
//        CommonStatisticsMethods.setElements(exclusionStamp, iI, iF, jI, jF, 0);//11812 this initialization should be done by callers.

        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                type=LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[i][j]);
                if(type==type0) {
                    CommonStatisticsMethods.setElements(exclusionStamp, i-r,i+r,j-r,j+r, exclusionMark);
                }
            }
        }
    }
    public static void buildComboStamp(LandscapeAnalyzerPixelSorting stamper, int[][] pixels, int[][] stamp, int[][] comboStamp, int[][] exclusionStamp, int[][] gradientMap, int[][] gdMapStamp, int exclusionMark, int iI, int iF, int jI, int jF){
        //passed 11812
        getGradientMap(pixels,gradientMap,iI,iF,jI,jF);
        stamper.updateAndStampPixels(gradientMap, gdMapStamp, iI, iF, jI, jF);
        CommonStatisticsMethods.setElements(comboStamp, iI, iF, jI, jF, 0);
        int i,j,type;
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                if(LandscapeAnalyzerPixelSorting.isLocalMinimum(gdMapStamp[i][j])){
                    if(exclusionStamp[i][j]!=exclusionMark) {
                        LandscapeAnalyzerPixelSorting.setLandscapeType(comboStamp, j, i, LandscapeAnalyzerPixelSorting.localMaximum, 0);
                    }
                }
            }
        }
    }
    public static void getRadialGradientMap(int[][] pixels, int[][] pixelsgd){
        ImageShape is1=new Ring(1,1),is2=new Ring(2,2);
        int[][] pixels1=CommonStatisticsMethods.getMean_ImageShape(pixels, is1);
        int[][] pixels2=CommonStatisticsMethods.getMean_ImageShape(pixels, is2);
        int w=pixels[0].length,h=pixels.length,i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pixelsgd[i][j]=pixels1[i][j]-pixels2[i][j];
            }
        }
    }
    public static void getSecondOrderGradientMap(int[][] pixels, int[][] pixelsgd){
        int i,j,w=pixels[0].length,h=pixels.length,pixel,dx,dy,x,y;
        double sqrt2=Math.sqrt(2);
        for(i=1;i<h-1;i++){
            for(j=1;j<w-1;j++){
                pixel=Math.abs(pixels[i][j-1]+pixels[i][j+1])+(int)(Math.abs(pixels[i-1][j-1]+pixels[i+1][j+1]))
                        +Math.abs(pixels[i-1][j]+pixels[i+1][j])+(int)(Math.abs(pixels[i-1][j+1]+pixels[i+1][j-1]));
                pixelsgd[i][j]=-(pixel-8*pixels[i][j])/8;
            }
        }

        pixelsgd[0][0]=-((pixels[0][1]+pixels[1][0])-2*pixels[0][0])/2;
        pixelsgd[h-1][w-1]=-((pixels[h-1][w-2]+pixels[h-2][w-1])-2*pixels[h-1][w-1])/2;
        pixelsgd[0][w-1]=-((pixels[0][w-2]+pixels[1][w-1])-2*pixels[0][w-1])/2;
        pixelsgd[h-1][0]=-((pixels[h-1][1]+pixels[h-2][0])-2*pixels[h-1][0])/2;

        for(i=1;i<h-1;i++){
            j=0;
            pixel=Math.abs(pixels[i-1][j]+pixels[i+1][j]);
            pixelsgd[i][j]=-(pixel-2*pixels[i][j])/2;
            j=w-1;
            pixel=Math.abs(pixels[i-1][j]+pixels[i+1][j]);
            pixelsgd[i][j]=-(pixel-2*pixels[i][j])/2;
        }

        for(j=1;j<w-1;j++){
            i=0;
            pixel=Math.abs(pixels[i][j-1]+pixels[i][j+1]);
            pixelsgd[i][j]=-(pixel-2*pixels[i][j])/2;
            i=h-1;
            pixel=Math.abs(pixels[i][j-1]+pixels[i][j+1]);
            pixelsgd[i][j]=-(pixel-2*pixels[i][j])/2;
        }
    }
    public static void getLocalMaximaAndCovervedMaxima(int[][]pixels,LandscapeAnalyzerPixelSorting stampper, int[][] stamp1, int[][] stamp2, int[][] pixelsgd, ArrayList<Point> localMaxima
            ,ArrayList<Point> coveredMaxima, ArrayList<Integer> vnMaximaIndexes){//index of the local maximum the covers the Gradient minima.
        localMaxima.clear();
        coveredMaxima.clear();
        ArrayList<Point> localMinima=new ArrayList(), waterSheds=new ArrayList();
        int h=pixels.length,w=pixels[0].length;
//            CommonMethods.getSpecialLandscapePoints(pixels, stamp, stampper, false, w, h, type, new intRange(0,w-1), new intRange(0,h-1), points);
        ArrayList<Point> gradientMinima=new ArrayList();
        getSpecialLandscapePoints(pixels, stamp1, false, w, h, LandscapeAnalyzerPixelSorting.localMaximum, localMaxima);
        getSpecialLandscapePoints(pixels, stamp1, false, w, h, LandscapeAnalyzerPixelSorting.localMinimum, localMinima);
        getSpecialLandscapePoints(pixels, stamp1, false, w, h, LandscapeAnalyzerPixelSorting.watershed, waterSheds);
        getGradientMap(pixels,pixelsgd);

//        meanFiltering(w,h,pixelsgd,1);
        getSpecialLandscapePoints(pixelsgd, stamp2, false, w, h, LandscapeAnalyzerPixelSorting.localMinimum, gradientMinima);
        int len1=localMaxima.size(),len2=gradientMinima.size(),i,j;
        Point p,pt;
        CommonStatisticsMethods.setElements(pixelsgd, -1);
        int x,y,type;
        ArrayList<Integer> types=new ArrayList();
        int nn=Integer.MIN_VALUE;
        CommonMethods.setPixelAtSpecialLandsapePoints(pixels, stamp1,LandscapeAnalyzer.watershed, nn);
        CommonMethods.setPixelAtSpecialLandsapePoints(pixels, stamp1,LandscapeAnalyzer.localMinimum, nn);

        int nNumMaxima=len1;
        len1=localMinima.size();
        for(i=0;i<len1;i++){
            p=localMinima.get(i);
            x=p.x;
            y=p.y;
            CommonStatisticsMethods.setElements(stamp1, Math.max(0, y-1), Math.min(h-1, y+1), Math.max(0, x-1), Math.min(w-1, x+1), i+1+nNumMaxima);//stamp1 is modified, so not to use normally
        }

        len1=waterSheds.size();
        for(i=0;i<len1;i++){
            p=waterSheds.get(i);
            x=p.x;
            y=p.y;
            CommonStatisticsMethods.setElements(stamp1, Math.max(0, y-1), Math.min(h-1, y+1), Math.max(0, x-1), Math.min(w-1, x+1), i+1+nNumMaxima);//stamp1 is modified, so not to use normally
        }

        for(i=0;i<nNumMaxima;i++){
            p=localMaxima.get(i);
            x=p.x;
            y=p.y;
            CommonStatisticsMethods.setElements(stamp1, Math.max(0, y-1), Math.min(h-1, y+1), Math.max(0, x-1), Math.min(w-1, x+1), i+1);//stamp1 is modified, so not to use normally
        }
        int index;
        for(j=0;j<len2;j++){
            p=gradientMinima.get(j);
            x=p.x;
            y=p.y;
            type=stamp1[y][x];
            if(type>0) continue;
//            if(type==-LandscapeAnalyzer.localMinimum) continue;
            if(x==0||x==w-1) continue;
            if(y==0||y==h-1) continue;
            coveredMaxima.add(p);
            pt=LandscapeAnalyzerPixelSorting.findLocalMaximum(pixels, w, h, x, y);
            index=stamp1[pt.y][pt.x]-1;
            if(index<=nNumMaxima){
                vnMaximaIndexes.add(index);
            }else{
                IJ.error("error in getLocalMaximaAndCovervedMaxima");
            }
        }
    }
    public static void showCoveredLocalMaxima(ImagePlus implc, int[][] pixels0, ArrayList<Point> points,int[] pnOverlappingIndexes, int indexI, int indexF){
        int i,j,pixel=(255<<8),w=implc.getWidth(),h=implc.getHeight(),lo,hi,len2;
        int[] pixels=(int[])implc.getProcessor().getPixels();
        Point p,pt;
        ArrayList <Point> contour;
        for(i=indexI;i<=indexF;i++){
            if(pnOverlappingIndexes[i]>=0){
                continue;
            }
            pixel=randomRGB();
            p=points.get(i);
            pixels[p.y*w+p.x]=pixel;
            pt=LandscapeAnalyzerPixelSorting.findLocalMaximum(pixels0, w, h, p.x, p.y);
            lo=pixels0[pt.y][pt.x];
            hi=lo;
            contour=ContourFollower.getContour_Out(pixels0, w, h, pt, lo, hi, true);
            ContourFollower.removeOffBoundPoints(w, h, contour);
            len2=contour.size();
            for(j=0;j<len2;j++){
                p=contour.get(j);
                pixels[p.y*w+p.x]=pixel;
            }
        }
    }
    public static void markPixel(int[][] pixels, int[][] stamp, int[][] scratch, int[][] scratchb, int w, int h, int x, int y, int pixel){
        if(isEIPoint(pixels, w, h, x, y)) {
            ArrayList<Point> region=new ArrayList(), border=new ArrayList();
            ImageAnalysis.RegionFinderEI.findRegion(pixels, scratch, scratchb, w, h, region, border, new Point(x,y), pixel);
            int len=region.size();
            Point p;
            for(int i=0;i<len;i++){
                p=region.get(i);
                stamp[p.y][p.x]=pixel;
            }
        }else{
            stamp[y][x]=pixel;
        }
    }
    public static ImagePlus getScaledImage(ImagePlus impl, int scale){//this is for enlarging for scale folds
        int w0=impl.getWidth(), h0=impl.getHeight();
        int[][] pixels0=CommonMethods.getPixelValues(impl);
        int w=w0*scale,h=h0*scale;
        ImagePlus implc=CommonMethods.getBlankImage(impl.getType(), w, h);
        int[][] pixels=new int[h][w];
        int i,j,k,l,pixel,x,y;
        for(i=0;i<h0;i++){
            for(j=0;j<w;j++){
                pixel=pixels0[i][j];
                for(k=0;k<scale;k++){
                    y=i*scale+k;
                    for(l=0;l<scale;l++){
                        x=j*scale+l;
                        pixels[y][x]=pixel;
                    }
                }
            }
        }
        CommonMethods.setPixels(implc, pixels);
        return implc;
    }
    public static void sortObjectArray(ArrayList<Object> ovA, double[] pdV){
        int len=pdV.length;
        int indexes[]=new int[len],i;
        for(i=0;i<len;i++){
            indexes[i]=i;
        }
        QuickSort.quicksort(pdV, indexes);
        sortObjectArray(ovA,indexes);
    }
    public static void sortObjectArray(ArrayList<Object> ovA, int[] indexes){
        ArrayList<Object> ovt=new ArrayList();
        int len=ovA.size(),i,index;
        for(i=0;i<len;i++){
            ovt.add(ovA.get(i));
        }
        for(i=0;i<len;i++){
            index=indexes[i];
            ovA.set(i,ovt.get(index));
        }
    }
    public static void swapObjectsElements(ArrayList<Object> ovA, int i, int j){
        Object ot=ovA.get(i);
        ovA.set(i, ovA.get(j));
        ovA.set(j, ot);
    }
    public static int[] sortArray(ArrayList<double[]> vpdArray, double dFactor){
        int len=vpdArray.size();
        int [] pnV=CommonMethods.getDefaultRankingIndexes(len);
        sortArray(vpdArray,pnV,dFactor,0,len-1);
        return pnV;
    }
    public static void sortArray(ArrayList<double[]> vpdArray, int[] pnIndexes, double dFactor,int indexI,int indexF){
        int len=vpdArray.size();
        int len1=vpdArray.get(0).length,i,j;
        double[] pdV=new double[len];
        double dv,dtf;
        for(i=indexI;i<=indexF;i++){
            dv=0;
            dtf=1;
            for(j=0;j<len1;j++){
                dv+=vpdArray.get(i)[j]*dtf;
                dtf*=dFactor;
            }
            pdV[i]=dv;
        }
        QuickSort.quicksort_external(pdV, pnIndexes, indexI, indexF);
    }
    public static ArrayList<Integer> getArray(Set<Integer> set){
        ArrayList<Integer> ia=new ArrayList();
        Iterator it=set.iterator();
        while(it.hasNext()){
            ia.add((Integer)it.next());
        }
        return ia;
    }
    public static ArrayList<IntPair> getCombinations(ArrayList<Integer> ia){
        int len=ia.size(),i,j;
        ArrayList<IntPair> ipa=new ArrayList();
        for(i=0;i<len;i++){
            for(j=i+1;j<len;j++){
                ipa.add(new IntPair(ia.get(i),ia.get(j)));
            }
        }
        return ipa;
    }
    public static boolean equivalentPairs(int r1, int r2, int r3, int r4){//r1!=r2&&r3!=r4
        if(r1==r3&&r2==r4) return true;
        if(r2==r3&&r1==r4) return true;
        return false;
    }
    public static boolean DirectContacting(Point p1, Point p2){
        return (Math.abs(p1.x-p2.x)+Math.abs(p1.y-p2.y)==1);
    }
    public static boolean DiagonallyContacting(Point p1, Point p2){
        return (Math.abs(p1.x-p2.x)==1&&Math.abs(p1.y-p2.y)==1);
    }
    public static boolean Overlapping(Point p1, Point p2){
        return(p1.x==p2.x&&p1.y==p2.y);
    }
    public static  ArrayList<Point> getEdgePath(Point p1, Point p2, int w, int h){
        //the path WILL NOT contain p1 and p2
        ArrayList<Point> path=new ArrayList();
        intRange xRange=new intRange(0,w-1);
//        intRange yRange=new intRange(0,h-1);
        int x1=p1.x,x2=p2.x,y1=p1.y,y2=p2.y,o;
        int edgeAxis1,edgeAxis2,delta;

        int[] pn1={p1.x,p1.y},pn2={p2.x,p2.y},pn=new int[2];
        int[] pnDim={w-1,h-1};
        if(xRange.isBorder(x1))
            edgeAxis1=0;
        else
            edgeAxis1=1;

        if(xRange.isBorder(x2))
            edgeAxis2=0;
        else
            edgeAxis2=1;

        int oAxis1=(edgeAxis1+1)%2,oAxis2=(edgeAxis2+1)%2;
        int o1=pn1[oAxis1],o2=pn2[oAxis2],e1=pn1[edgeAxis1],e2=pn2[edgeAxis2],e;

        if(edgeAxis1==edgeAxis2){
            if(e1!=e2){
//                IJ.error("current version of getEdgePath does not support two points on the oposite side of the the image");
                int[] pnM=new int[2];
                pnM[edgeAxis1]=pnDim[edgeAxis1]/2;
                if(o1+o2<=pnDim[oAxis1])
                    pnM[oAxis1]=0;
                else
                    pnM[oAxis1]=pnDim[oAxis1];

                Point pm=new Point(pnM[0],pnM[1]);
                appendPath(path,getEdgePath(p1,pm,w,h));
                path.add(pm);
                appendPath(path,getEdgePath(pm,p2,w,h));
                return path;
            }
            delta=1;
            if(o1>o2) delta=-1;
            o=o1-delta;
            while(o!=o2){
                o+=delta;
                pn[edgeAxis1]=pn1[edgeAxis1];
                pn[oAxis1]=o;
                path.add(new Point(pn[0],pn[1]));
            }
        }else{
            delta=1;
            if(o1>e2) delta=-1;
            o=o1-delta;
            while(o!=e2){
                o+=delta;
                pn[edgeAxis1]=e1;
                pn[oAxis1]=o;
                path.add(new Point(pn[0],pn[1]));
            }
            delta=1;
            if(e1>o2) delta=-1;
            e=e1;
            while(e!=o2){
                e+=delta;
                pn[edgeAxis1]=e;
                pn[oAxis1]=e2;
                path.add(new Point(pn[0],pn[1]));
            }
        }
        path.remove(path.size()-1);
        path.remove(0);
        return path;
    }
    public static void swapElements(ArrayList<Integer> ia, int i, int j){
        int nt=ia.get(i);
        ia.set(i, ia.get(j));
        ia.set(j, nt);
    }
    public static void translatePath(ArrayList<Point> path, Point pt){
        int i,len=path.size();
        for(i=0;i<len;i++){
            path.get(i).translate(pt.x, pt.y);
        }
    }
    public static void translatePath(ArrayList<Point> path,int iI, int iF, Point pt){
        int i,len=path.size();
        for(i=iI;i<iF;i++){
            path.get(i).translate(pt.x, pt.y);
        }
    }
    static public void buildConnectin(Point p1, Point p2, ArrayList<Point> path){

        //This method add all grid points that are necessary to connect the two points (ip1 and ip2) into the
        //array ipa, but without including ip1 or ip2. All the adjacent grid points must be either horizontally (having
        //the same y coordinates) or vertically (having the same x coordinates) connected.
        int x1=p1.x, x2=p2.x, y1=p1.y, y2=p2.y;
        path.clear();
        int x=x1,y;
        int delta;
        if(x1==x2){
            delta=1;
            if(y2<y1) delta=-1;
            y=y1+delta;
            while(delta*(y2-y)>=0){
                path.add(new Point(x1,y));
                y+=delta;
            }
        }else if(y1==y2){
            delta=1;
            if(x2<x1) delta=-1;
            x=x1+delta;
            while(delta*(x2-x)>=0){
                path.add(new Point(x,y1));
                x+=delta;
            }
        }else{
            double deltaX=x2-x1;
            double deltaY=y2-y1;
            double k=deltaY/deltaX;
            double xd,yd;
            int xSign=1, ySign=1;
            if(deltaX<0) xSign=-1;
            if(deltaY<0) ySign=-1;
            x=x1;
            int yi=y1,yf=y2;
            xd=x1;
            while(xSign*(x2-x)>0){
                xd=x+0.5*xSign;
                yd=y1+k*(xd-x1);
                yf=(int)yd;
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line only includes pixels that are traversed
                //(at lest two points are on the line) by the center line.
                y=yi+ySign;
                while(ySign*(yf-y)>=0){
                    path.add(new Point(x,y));
                    y+=ySign;
                }
                x+=xSign;
                yi=yf;/*
                if(ySign*(yd-(int)yd)<0.5||ySign*(yd-(int)yd)>0.5){
                    yi=yf;
                }
                else{//this is for the  case of (yd-(int)yd)==0.5
                    yi=yf+ySign;
                }*/
                y=yi;
                xd=x;
                yd=y1+k*(xd-x1);
                yf=(int)yd;
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line only includes pixels that are traversed
                while(ySign*(yf-y)>=0){
                    path.add(new Point(x,y));
                    y+=ySign;
                }
            }
        }
        int size=path.size();
        if(size>0){
            path.remove(size-1);
        }
    }
    static public void buildConnection(IntPair ip1, IntPair ip2, ArrayList <IntPair> ipa){
        //This method add all grid points that are necessary to connect the two points (ip1 and ip2) into the
        //array ipa, but without including ip1 or ip2. All the adjacent grid points must be either horizontally (having
        //the same y coordinates) or vertically (having the same x coordinates) connected.
        int x1=ip1.left, x2=ip2.left, y1=ip1.right, y2=ip2.right;
        int x=x1,y;
        int delta;
        if(x1==x2){
            delta=1;
            if(y2<y1) delta=-1;
            y=y1+delta;
            while(delta*(y2-y)>=0){
                ipa.add(new IntPair(x1,y));
                y+=delta;
            }
        }else if(y1==y2){
            delta=1;
            if(x2<x1) delta=-1;
            x=x1+delta;
            while(delta*(x2-x)>=0){
                ipa.add(new IntPair(x,y1));
                x+=delta;
            }
        }else{
            double deltaX=x2-x1;
            double deltaY=y2-y1;
            double k=deltaY/deltaX;
            double xd,yd;
            int xSign=1, ySign=1;
            if(deltaX<0) xSign=-1;
            if(deltaY<0) ySign=-1;
            x=x1;
            int yi=y1,yf=y2;
            xd=x1;
            while(xSign*(x2-x)>0){
                xd=x+0.5*xSign;
                yd=y1+k*(xd-x1);
                yf=(int)yd;
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line only includes pixels that are traversed
                //(at lest two points are on the line) by the center line.
                y=yi+ySign;
                while(ySign*(yf-y)>=0){
                    ipa.add(new IntPair(x,y));
                    y+=ySign;
                }
                x+=xSign;
                yi=yf;/*
                if(ySign*(yd-(int)yd)<0.5||ySign*(yd-(int)yd)>0.5){
                    yi=yf;
                }
                else{//this is for the  case of (yd-(int)yd)==0.5
                    yi=yf+ySign;
                }*/
                y=yi;
                xd=x;
                yd=y1+k*(xd-x1);
                yf=(int)yd;
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line only includes pixels that are traversed
                while(ySign*(yf-y)>=0){
                    ipa.add(new IntPair(x,y));
                    y+=ySign;
                }
            }
        }
        int size=ipa.size();
        if(size>0){
            ipa.remove(size-1);
        }
    }
    public static void appendPath(ArrayList<Point> path, ArrayList<Point> points){
        appendPath(path,points,0,points.size()-1);
    }
    public static void appendPath(ArrayList<Point> path, ArrayList<Point> points, int iI, int iF){
        int i;
        for(i=iI;i<=iF;i++){
            path.add(points.get(i));
        }
    }
    public static boolean ArraySize(int[][] pnV, int w, int h){
        if(pnV==null) return false;
        if(pnV.length!=h) return false;
        if(pnV[0].length!=w) return false;
        return true;
    }
    public static void insertPath(ArrayList<Point> points,ArrayList<Point> path,int position){
        //the path is inserted befor position
        int i,len=path.size();
        ArrayList<Point> pointst=new ArrayList();
        for(i=0;i<position;i++){
            pointst.add(points.get(i));
        }
        appendPath(pointst,path);
        appendPath(pointst,points,position,points.size()-1);
        points.clear();
        ContourFollower.copyArray(pointst, points);
    }
    public static void showFunctionValues_StandardFunction(ImagePlus impl, double[] pdPars, ArrayList<Point> points, String sFunctionType){
        ArrayList<Double> values=new ArrayList();
        int i,x,y,len=points.size();
        Point p;
        double pdX[]=new double[2];
        for(i=0;i<len;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            pdX[0]=x;
            pdX[1]=y;
            values.add(StandardFunctionValue(pdPars,pdX,sFunctionType));
        }
        showFunctionValues(impl,points,values);
    }
    public static void showFunctionValues(ImagePlus impl, ArrayList<Point> points, ArrayList<Double> values){
        if(impl.getType()!=ImagePlus.GRAY32){
            IJ.error("the image type for showFunctionValues must be GRAY32");
        }
        int w=impl.getWidth();
        float[] pixels=(float[])impl.getProcessor().getPixels();
        float pixel;
        int x,y,i,len=points.size();
        Point p;
        for(i=0;i<len;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            pixel=values.get(i).floatValue();
            pixels[y*w+x]=pixel;
        }
    }
    public static double StandardFunctionValue(double[] pdPars, double[] pdX, String sFunctionType){
        return StandardFunctionValue(pdPars, 0, pdPars.length, pdX, sFunctionType,true);
    }
    public static double StandardFunctionValue(double[] pdPars, int iI, int nPars, double[] pdX, String sFunctionType, boolean ConstantIncluded){
        double dv=0,B;
        int i;
        boolean validFunc=false;
        int nNumBasePars,nNumParsPerTerm,nTerms;
        int o;
        if(sFunctionType.contentEquals("exponential")){
            validFunc=true;
            nNumBasePars=1;
            nNumParsPerTerm=2;
            nTerms=nPars/nNumParsPerTerm;
            if(ConstantIncluded){
                dv=pdPars[iI];
                o=1;
            } else{
                dv=0;
                o=0;
            }
            for(i=0;i<nTerms;i++){
                dv+=pdPars[o+iI]*Math.exp(-pdX[0]/pdPars[iI+o+1]);
                o+=nNumParsPerTerm;
            }
        }
        if(sFunctionType.startsWith("polynomial")){
            validFunc=true;
            dv=0;
            o=1;
            if(ConstantIncluded){
                B=pdPars[iI];
                o=1;
            }else{
                B=0;
                o=0;
            }
            nTerms=nPars;
            for(i=0;i<nTerms;i++){
                dv+=pdPars[iI+i]*Math.pow(pdX[0], i+1);
            }
        }
        if(sFunctionType.contentEquals("gaussian")){
            validFunc=true;
            dv=0;
            double a,b,c,x=pdX[0],p;
            int nParsPerTerm=3;
            o=1;
            if(ConstantIncluded){
                B=pdPars[iI];
                o=1;
            }else{
                B=0;
                o=0;
            }
            nTerms=nPars/nParsPerTerm;
            dv=B;
            for(i=0;i<nTerms;i++){
                a=pdPars[iI+o];
                b=pdPars[iI+o+1];
                c=pdPars[iI+o+2];
                p=(x-b)*(x-b)/(2*c*c);
                dv+=a*Math.exp(-p);
                o+=nParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("FluoDelta")){
            validFunc=true;
            dv=0;
            double a,b,mu,sigma1,sigma2,x=pdX[0],p;
            int nParsPerTerm=5;
            o=1;
            if(ConstantIncluded){
                B=pdPars[iI];
                o=1;
            }else{
                B=0;
                o=0;
            }
            nTerms=nPars/nParsPerTerm;
            dv=B;
            for(i=0;i<nTerms;i++){
                a=pdPars[iI+o];
                b=pdPars[iI+o+1];
                mu=pdPars[iI+o+2];
                sigma1=pdPars[iI+o+3];
                sigma2=pdPars[iI+o+4];
                p=(x)*(x)/(2*sigma1*sigma1);
                dv+=a*Math.exp(-p);
                p=(x-mu)*(x-mu)/(2*sigma2*sigma2);
                dv+=b*Math.exp(-p);
                p=(x+mu)*(x+mu)/(2*sigma2*sigma2);
                dv+=b*Math.exp(-p);
                o+=nParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("gaussian_Hist")){
            validFunc=true;
            dv=0;
            double a,b,c,x0=pdX[1],x1=pdX[2],p0,p1;
            int nParsPerTerm=3;
            o=1;
            if(ConstantIncluded){
                B=pdPars[iI];
                o=1;
            }else{
                B=0;
                o=0;
            }
            nTerms=nPars/nParsPerTerm;
            dv=B;
            for(i=0;i<nTerms;i++){
                a=pdPars[iI+o];
                b=pdPars[iI+o+1];
                c=pdPars[iI+o+2];
                p0=(x0-b)*(x0-b)/(2*c*c);
                p1=(x1-b)*(x1-b)/(2*c*c);
                dv+=B*(x1-x0)+errorFunction_Non_Normalized(a,b,c,x1)-errorFunction_Non_Normalized(a,b,c,x0);
                o+=nParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("gaussian2D_GaussianPars")){//the parameters are sigma, theta ...
            validFunc=true;
            dv=0;
            double a,b,c,A,dx,dy,p,x=pdX[0],y=pdX[1];
            int nBasePars=1;
            int nParsPerTerm=6;
            double sigmax2,sigmay2,theta,ctheta,stheta;
            o=1;
            if(ConstantIncluded){
                B=pdPars[iI];
                o=1;
                nBasePars=1;
            }else{
                B=0;
                o=0;
                nBasePars=0;
            }
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            dv=B;
            for(i=0;i<nTerms;i++){
                A=pdPars[iI+o];
                sigmax2=pdPars[iI+o+1];
                sigmax2*=sigmax2;
                sigmay2=pdPars[iI+o+2];
                sigmay2*=sigmay2;
                theta=pdPars[iI+o+3];
                ctheta=Math.cos(theta);
                stheta=Math.sin(theta);

                a=0.5*ctheta*ctheta/sigmax2+0.5*stheta*stheta/sigmay2;
                b=0.5*stheta*ctheta*(1./sigmay2-1./sigmax2);
                c=0.5*stheta*stheta/sigmax2+0.5*ctheta*ctheta/sigmay2;

                dx=x-pdPars[iI+o+4];
                dy=y-pdPars[iI+o+5];
                p=dx*(a*dx+2*b*dy)+c*dy*dy;
                dv+=A*Math.exp(-p);
                o+=nParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            dv=0;
            double A,dx,dy,p,x=pdX[0],y=pdX[1];
            int nBasePars=1;
            int nParsPerTerm=4;
            double sigma2;
            o=1;
            if(ConstantIncluded){
                B=pdPars[iI];
                o=1;
                nBasePars=1;
            }else{
                B=0;
                o=0;
                nBasePars=0;
            }
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            dv=B;
            for(i=0;i<nTerms;i++){
                A=pdPars[iI+o];
                sigma2=pdPars[iI+o+1];
                dx=x-pdPars[iI+o+2];
                dy=y-pdPars[iI+o+3];
                p=0.5*(dx*dx+dy*dy)/sigma2;
                dv+=A*Math.exp(-p);
                o+=nParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("gaussian2D")){
            validFunc=true;
            dv=0;
            double a,b,c,A,dx,dy,p,x=pdX[0],y=pdX[1];
            int nBasePars=1;
            int nParsPerTerm=6;
            o=1;
            if(ConstantIncluded){
                B=pdPars[iI];
                o=1;
                nBasePars=1;
            }else{
                B=0;
                o=0;
                nBasePars=0;
            }
            dv=B;
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            for(i=0;i<nTerms;i++){
                A=pdPars[iI+o];
                a=pdPars[iI+o+1];
                b=pdPars[iI+o+2];
                c=pdPars[iI+o+3];
                dx=x-pdPars[iI+o+4];
                dy=y-pdPars[iI+o+5];
                o+=nParsPerTerm;
                p=dx*(a*dx+2*b*dy)+c*dy*dy;
                dv+=A*Math.exp(-p);
            }
        }
        if(!validFunc) IJ.error("undefined expression type: "+sFunctionType);
        return dv;
    }
    public static double errorFunction_Non_Normalized(double a, double b, double c, double x){
        //the gaussian function used here is f(x)=const+a*exp(-(x-b)*(x-b)/(2*c*c))
        double sign=1;
        if(x<b) sign=-1;
        double ef=a*c*Math.sqrt(Math.PI/2.);
        double z=Math.abs(x-b)/(Math.sqrt(2.)*c);
        ef*=GaussianDistribution.erf2(z);
        return 0.5+sign*ef;
    }
    public static double StandardFunctionVAndD(double[] pdPars, int iI, int nPars, double[] pdX, String sFunctionType, boolean ConstantIncluded,double[] pdGradient){
        double dv=0;
        int i;
        boolean validFunc=false;
        int nTerms;
        int o;
        if(sFunctionType.contentEquals("gaussian")){
            validFunc=true;
            dv=StandardFunctionValue(pdPars, iI, nPars, pdX,sFunctionType, ConstantIncluded);
            double a,b,c,x=pdX[0];
            int nParsPerTerm=3;
            o=1;
            if(ConstantIncluded){
                o=1;
            }else{
                o=0;
            }
            nTerms=nPars/nParsPerTerm;
            for(i=0;i<nTerms;i++){
                a=pdPars[iI+o];
                b=pdPars[iI+o+1];
                c=pdPars[iI+o+2];
                pdGradient[iI]=dv/a;
                pdGradient[iI+1]=dv*(x-b)/(c*c);
                pdGradient[iI+2]=dv*(x-b)*(x-b)/(c*c*c);
                o+=nParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            dv=StandardFunctionValue(pdPars, iI, nPars, pdX,sFunctionType, ConstantIncluded);;
            double A,x0,y0,p,x=pdX[0],y=pdX[1];
            int nBasePars=1;
            int nParsPerTerm=4;
            double sigma;
            o=1;
            if(ConstantIncluded){
                o=1;
                nBasePars=1;
            }else{
                o=0;
                nBasePars=0;
            }
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            for(i=0;i<nTerms;i++){
                A=pdPars[iI+o];
                sigma=pdPars[iI+o+1];
                x0=pdPars[iI+o+2];
                y0=pdPars[iI+o+3];

                pdGradient[iI]=dv/A;
                pdGradient[iI+1]=dv*((x-x0)*(x-x0)+(y-y0)*(y-y0))/(sigma*sigma*sigma);
                pdGradient[iI+2]=dv*(x-x0)/(2*sigma*sigma);
                pdGradient[iI+3]=dv*(y-y0)/(2*sigma*sigma);
                o+=nParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("gaussian2D")){
            validFunc=true;
            dv=StandardFunctionValue(pdPars, iI, nPars, pdX,sFunctionType, ConstantIncluded);;
            double a,b,c,A,dx,dy,p,x=pdX[0],y=pdX[1];
            int nBasePars=1;
            int nParsPerTerm=6;
            o=1;
            if(ConstantIncluded){
                o=1;
                nBasePars=1;
            }else{
                o=0;
                nBasePars=0;
            }
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            for(i=0;i<nTerms;i++){
                A=pdPars[iI+o];
                a=pdPars[iI+o+1];
                b=pdPars[iI+o+2];
                c=pdPars[iI+o+3];
                dx=x-pdPars[iI+o+4];
                dy=y-pdPars[iI+o+5];

                pdGradient[iI]=dv/A;
                pdGradient[iI+1]=-dv*dx*dx;
                pdGradient[iI+2]=-2*dv*dx*dy;
                pdGradient[iI+3]=-dv*dy*dy;
                pdGradient[iI+4]=2*(a*dx+b*dy)*dv;
                pdGradient[iI+5]=2*(b*dx+c*dy)*dv;
                o+=nParsPerTerm;
            }
        }
        if(!validFunc) IJ.error("undefined expression type: "+sFunctionType);
        return dv;
    }
    public static void StandardFunctionVarRanges(ArrayList<DoubleRange> varRanges,double[] pdPars, int iI, int nPars, String sFunctionType, boolean ConstantIncluded){

        int i;
        boolean validFunc=false;
        int nNumParsPerTerm,nTerms;
        int o;
        int len=varRanges.size();
        for(i=0;i<len;i++){
//            varRanges.get(i).resetRange();
        }
        DoubleRange dRange,dRange2;
        if(sFunctionType.contentEquals("exponential")){
            validFunc=true;
            nNumParsPerTerm=2;
            nTerms=nPars/nNumParsPerTerm;
            double a,tau;
            if(ConstantIncluded){
                o=1;
            } else{
                o=0;
            }
            dRange=varRanges.get(0);
            for(i=0;i<nTerms;i++){
                a=pdPars[o+iI];
                tau=pdPars[iI+o+1];
                if(tau>0){
                    dRange.expandRange(Double.NEGATIVE_INFINITY);
                    dRange.expandRange(5*tau);
                }else{
                    dRange.expandRange(Double.POSITIVE_INFINITY);
                    dRange.expandRange(5*tau);
                }
                o+=nNumParsPerTerm;
            }
        }
        if(sFunctionType.contentEquals("polynomial")){
            validFunc=true;
            dRange=varRanges.get(0);
            dRange.expandRange(Double.POSITIVE_INFINITY);
            dRange.expandRange(Double.NEGATIVE_INFINITY);
        }
        if(sFunctionType.contentEquals("gaussian")){
            validFunc=true;
            dRange=varRanges.get(0);
            double b,c;
            int nParsPerTerm=3;
            o=1;
            if(ConstantIncluded){
                o=1;
            }else{
                o=0;
            }
            nTerms=nPars/nParsPerTerm;
            for(i=0;i<nTerms;i++){
                b=pdPars[iI+o+1];
                c=pdPars[iI+o+2];
                o+=nParsPerTerm;
                dRange.expandRange(b-3*c);
                dRange.expandRange(b+3*c);
            }
        }
        if(sFunctionType.contentEquals("gaussian2D")){//the parameters are A,a,b,c,xc and yc
            validFunc=true;
            double[] pdNewPars=new double[6];
            double dx,dy;
            int nBasePars=1;
            int nParsPerTerm=6;
            double sigmax,sigmay,theta,ctheta,stheta;
            o=1;
            if(ConstantIncluded){
                o=1;
                nBasePars=1;
            }else{
                o=0;
                nBasePars=0;
            }
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            dRange=varRanges.get(0);
            dRange2=varRanges.get(1);

            for(i=0;i<nTerms;i++){
                ComposedFittingFunction.getGaussian2DParameters(pdPars, iI+o, pdNewPars, 0);
                sigmax=pdNewPars[1];
                sigmay=pdNewPars[2];
                theta=pdNewPars[3];
                ctheta=Math.cos(theta);
                stheta=Math.sin(theta);

                dx=pdPars[iI+o+4];
                dy=pdPars[iI+o+5];
                o+=nParsPerTerm;

                dRange.expandRange(dx-3*sigmax*ctheta);
                dRange.expandRange(dx-3*sigmay*stheta);
                dRange.expandRange(dx+3*sigmax*ctheta);
                dRange.expandRange(dx+3*sigmay*stheta);

                dRange2.expandRange(dy-3*sigmax*stheta);
                dRange2.expandRange(dy-3*sigmay*ctheta);
                dRange2.expandRange(dy+3*sigmax*stheta);
                dRange2.expandRange(dy+3*sigmay*ctheta);
            }
        }
        if(sFunctionType.contentEquals("gaussian2D_GaussianPars")){//the parameters are sigma, theta ...
            validFunc=true;
            double dx,dy;
            int nBasePars=1;
            int nParsPerTerm=6;
            double sigmax,sigmay,theta,ctheta,stheta;
            o=1;
            if(ConstantIncluded){
                o=1;
                nBasePars=1;
            }else{
                o=0;
                nBasePars=0;
            }
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            dRange=varRanges.get(0);
            dRange2=varRanges.get(1);

            for(i=0;i<nTerms;i++){
                sigmax=pdPars[iI+o+1];
                sigmay=pdPars[iI+o+2];
                theta=pdPars[iI+o+3];
                ctheta=Math.cos(theta);
                stheta=Math.sin(theta);

                dx=pdPars[iI+o+4];
                dy=pdPars[iI+o+5];
                o+=nParsPerTerm;

                dRange.expandRange(dx-3*sigmax*ctheta);
                dRange.expandRange(dx-3*sigmay*stheta);
                dRange.expandRange(dx+3*sigmax*ctheta);
                dRange.expandRange(dx+3*sigmay*stheta);

                dRange2.expandRange(dy-3*sigmax*stheta);
                dRange2.expandRange(dy-3*sigmay*ctheta);
                dRange2.expandRange(dy+3*sigmax*stheta);
                dRange2.expandRange(dy+3*sigmay*ctheta);
            }
        }
        if(sFunctionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            dRange=varRanges.get(0);
            dRange2=varRanges.get(1);
            double dx,dy;
            int nBasePars=1;
            int nParsPerTerm=4;
            double sigma;
            o=1;
            if(ConstantIncluded){
                o=1;
                nBasePars=1;
            }else{
                o=0;
                nBasePars=0;
            }
            nTerms=(nPars-nBasePars)/nParsPerTerm;
            for(i=0;i<nTerms;i++){
                sigma=pdPars[iI+o+1];

                dx=pdPars[iI+o+2];
                dy=pdPars[iI+o+3];
                o+=nParsPerTerm;
                dRange.expandRange(dx+3*sigma);
                dRange.expandRange(dx-3*sigma);
                dRange2.expandRange(dy+3*sigma);
                dRange2.expandRange(dy-3*sigma);
            }
        }
        if(!validFunc) IJ.error("undefined expression type: "+sFunctionType);
    }
    public static boolean isBlank(String[] line){
        int i,j,len=line.length;
        String st;
        for(i=0;i<len;i++){
            st=line[i];
            if(!isWhightString(st)) return false;
        }

        return true;
    }
    public static boolean isWhightString(String st){
        int i,len=st.length();
        for(i=0;i<len;i++){
            if(st.charAt(i)!=' ') return false;
        }
        return true;
    }
    public static void getRoiPoints(Roi ROI, ArrayList<Point> points){
        Rectangle br=ROI.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height;
        points.clear();
        int x,y,i,j,o;
        for(i=0;i<rh;i++){
            y=yo+i;
            for(j=0;j<rw;j++){
                x=xo+j;
                if(ROI.contains(x, y)){
                    points.add(new Point(x,y));
                }
            }
        }
    }

    public static void showLandscapeAndComplex(ImagePlus impl, boolean bShowRegions, boolean bHightLightGrooves, double pValue, boolean bShowComplex,int iI, int iF, int jI, int jF){//pValue is for building the complex
        //based on the region boundary hight of the randomized image
        final ImagePlus implc=CommonMethods.copyToRGBImage(impl);
        int h=impl.getHeight(),w=impl.getWidth();
        int pixel=0;
        ArrayList <Integer> types=new ArrayList();
        IntArray indexes=new IntArray();

        int type=LandscapeAnalyzerPixelSorting.localMaximum;

        ArrayList <IntArray> rgbIndexes=new ArrayList();
        indexes.m_intArray.add(0);
        indexes.m_intArray.add(1);
        types.add(LandscapeAnalyzerPixelSorting.localMaximum);
        rgbIndexes.add(indexes);

        indexes=new IntArray();
        types.add(LandscapeAnalyzerPixelSorting.localMinimum);
        indexes.m_intArray.add(1);
        indexes.m_intArray.add(2);
        rgbIndexes.add(indexes);

        indexes=new IntArray();
        types.add(LandscapeAnalyzerPixelSorting.watershed);
        indexes.m_intArray.add(2);
        rgbIndexes.add(indexes);

        int pixels[][]=new int[h][w],stamp[][],pixels0[][];


        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(impl);

        int i,j,slice,num=impl.getStackSize(),rgb[]=new int[3],region;

        RegionNode rNode;
        num=1;
        for(slice=0;slice<num;slice++){
            pixel=0;
            impl.setSlice(slice+1);
            pixels0=CommonMethods.getPixelValues(impl);
            implc.setSlice(slice+1);
            CommonMethods.markSpecialLandscapePoints(impl, implc, stampper, types, pixel, rgbIndexes,iI,iF,jI,jF);
            pixels=CommonMethods.getPixelValues(implc);
            stamp=stampper.getStamp();
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
 //                   implc.show();
                }
            });

            for(i=iI;i<=iF;i++){
                for(j=jI;j<=jF;j++){
                    pixel=pixels[i][j];
                    type=LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[i][j]);
                    region=LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[i][j]);
                    if(bShowRegions){
                        CommonMethods.intTOrgb(pixel, rgb);
                        rgb[2]=(region)%255;
                        pixel=(rgb[0]<<16)|(rgb[1]<<8)|(rgb[2]);
                        pixels[i][j]=pixel;
                    }
                    if(bHightLightGrooves&&LandscapeAnalyzerPixelSorting.isWatershed(stamp, j, i)){
                        CommonMethods.intTOrgb(pixel, rgb);
                        rgb[0]=255;
                        rgb[1]=255;
                        pixel=(rgb[0]<<16)|(rgb[1]<<8)|(rgb[2]);
                        pixels[i][j]=pixel;
                    }
                }
            }
            CommonMethods.setPixels(implc, pixels,true);

            implc.show();
            CommonMethods.refreshImage(implc);

            RegionBoundaryAnalyzer ba=buildRegionComplex(impl,pValue);

//            ba.removeOverheightBorderSegments(pixels0, 66);

            ArrayList<RegionComplexNode> cNodes=ba.getRegionComplexNodes();
            int len=cNodes.size();
            RegionComplexNode cNode;
            for(i=0;i<len;i++){
                cNode=cNodes.get(i);
                if(bShowComplex)ba.drawRegionComplex(implc, cNode, pixels0);
            }
        }
        CommonMethods.refreshImage(implc);
    }
    
    public static RegionBoundaryAnalyzer buildRegionComplex(ImagePlus impl, double pValue){
        return buildRegionComplex(impl,null,pValue);
    }
    
    public static RegionBoundaryAnalyzer buildRegionComplex(ImagePlus impl){
        RegionBoundaryAnalyzer cRBA=null;
        int[] pnRange=new int[2];
        int w=impl.getWidth(),h=impl.getHeight();
        int[][] stamp=new int[h][w];
        int[][] pixels=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels, pnRange);
        LandscapeAnalyzerPixelSorting la=new LandscapeAnalyzerPixelSorting(w,h,pnRange);

        la.updateAndStampPixels(pixels, stamp);
        cRBA=new RegionBoundaryAnalyzer(stamp);
        return cRBA;
    }
    
    public static RegionBoundaryAnalyzer buildRegionComplex(int[][] pixels){
        RegionBoundaryAnalyzer cRBA=null;
        int[] pnRange=new int[2];
        int w=pixels[0].length,h=pixels.length;
        int[][] stamp=new int[h][w];
        intRange ir=CommonStatisticsMethods.getRange(stamp);
        pnRange[0]=ir.getMin();
        pnRange[1]=ir.getMax();
        LandscapeAnalyzerPixelSorting la=new LandscapeAnalyzerPixelSorting(w,h,pnRange);

        la.updateAndStampPixels(pixels, stamp);
        cRBA=new RegionBoundaryAnalyzer(stamp);
        return cRBA;
    }
    public static RegionBoundaryAnalyzer buildRegionComplex(ImagePlus impl, ImagePlus implr, double pValue){
        RegionBoundaryAnalyzer cRBAr=null,cRBA=null;

        if(implr==null){
            if(impl.getWidth()<250||impl.getHeight()<250)
                implr=CommonMethods.resizedImage_cropping(impl, 250, 250);
            else
                implr=CommonMethods.cloneImage(impl);

            CommonMethods.randomizeImage(implr);
            implr.setTitle(impl.getTitle()+" -reference image");
        }

        cRBAr=CommonMethods.buildRegionComplex(implr);

        Histogram hist=cRBAr.getBorderSegmentHeightHistogram(CommonMethods.getPixelValues(impl));
        hist.setPercentile(1-pValue);

        double cutoff=hist.getPercentileValue();

        cRBA=CommonMethods.buildRegionComplex(impl);
        cRBA.removeOverheightBorderSegments(CommonMethods.getPixelValues(impl), cutoff);

        return cRBA;
    }
    public static ArrayList<Integer> getCommonElements(ArrayList<Integer> nv1, ArrayList<Integer> nv2){
        ArrayList<Integer> nv=new ArrayList();
        int len1=nv1.size(),len2=nv2.size(),i,j,n1,n2;
        for(i=0;i<len1;i++){
            n1=nv1.get(i);
            for(j=0;j<len2;j++){
                n2=nv2.get(j);
                if(n1==n2&&!containsContent(nv,n1)) nv.add(n1);//11829
            }
        }
        return nv;
    }
    public static ArrayList<Point> getComboMaxima(ImagePlus impl){
        int w=impl.getWidth(),h=impl.getHeight(),i,len;
        int[][] pixels=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
        return getComboMaxima(pixels);
    }
    public static ArrayList<Point> getComboMaximaInRegion(int[][] pixels,Point pt){
        ArrayList<Point> comboMaxima=getComboMaxima(pixels);
        int w=pixels[0].length,h=pixels.length;
        int[][] stamp=new int[h][w];
        LandscapeAnalyzerPixelSorting.stampPixels(pixels, stamp);
        int i,len=comboMaxima.size();
        Point p;
        int region=LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[pt.y][pt.x]);
        for(i=len-1;i>=0;i--){
            p=comboMaxima.get(i);
            if(LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[pt.y][pt.x])!=region) comboMaxima.remove(i);
        }
        return comboMaxima;
    }
    public static ArrayList<Point> getComboMaxima(int[][] pixels){
        ArrayList<Point> comboMaxima=new ArrayList();
        int w=pixels[0].length,h=pixels.length,i,len;
        int[][] pixelsgd=new int[h][w],pdExclusion=new int[h][w];
        int exclusion=-1;
        ArrayList<Point> watersheds=CommonMethods.getSpecialLandscapePoints(pixels,LandscapeAnalyzerPixelSorting.watershed);
        ArrayList<Point> localMinima=CommonMethods.getSpecialLandscapePoints(pixels,LandscapeAnalyzerPixelSorting.localMinimum);

        Point pt;
        len=localMinima.size();
        for(i=0;i<len;i++){
            pt=localMinima.get(i);
            CommonStatisticsMethods.setElements(pdExclusion, pt.y-1, pt.y+1, pt.x-1, pt.x+1, exclusion);
        }
        len=watersheds.size();
        for(i=0;i<len;i++){
            pt=watersheds.get(i);
            CommonStatisticsMethods.setElements(pdExclusion, pt.y-1, pt.y+1, pt.x-1, pt.x+1, exclusion);
        }

        CommonMethods.getGradientMap(pixels, pixelsgd);
        ArrayList<Point> gdMinima=CommonMethods.getSpecialLandscapePoints(pixelsgd, LandscapeAnalyzerPixelSorting.localMinimum);
        len=gdMinima.size();
        for(i=0;i<len;i++){
            pt=gdMinima.get(i);
            if(pdExclusion[pt.y][pt.x]!=exclusion) {
                comboMaxima.add(pt);
            }
        }
        return comboMaxima;
    }
    public static boolean containsImage(ArrayList<ImagePlus> Images, ImagePlus impl){
        int i,len=Images.size();
        for(i=0;i<len;i++){
             if(impl.equals(Images.get(i))) return true;
        }
        return false;
    }
    public static double DistToEdge(ArrayList<Point> points,int w,int h){
        double dist,dn=Double.POSITIVE_INFINITY;
        int i,len=points.size();
        Point pt;
        for(i=0;i<len;i++){
            pt=points.get(i);
            if(pt.x<dn) dn=pt.x;
            if(pt.y<dn) dn=pt.y;
            dist=w-1-pt.x;
            if(dist<dn) dn=dist;
            dist=h-1-pt.y;
            if(dist<dn) dn=dist;
        }
        return dn;
    }
    public static boolean containContent(ArrayList<IPOGaussianNode> IPOs, IPOGaussianNode IPO){
        int len=IPOs.size(),i;
        for(i=0;i<len;i++){
            if(IPOs.get(i)==IPO) return true;
        }
        return false;
    }
    public static void drawRois(BufferedImage img, ArrayList<Roi> rois, int mag,int font){
        int i,len=rois.size();
        for(i=0;i<len;i++){
            drawRoi(img,rois.get(i),mag,font);
        }
    }
    public static void drawRoi(BufferedImage img, Roi roi,int mag,int font){
        int[] pnXCoors, pnYCoors;
        Rectangle rect=roi.getBoundingRect();
        int i,len,x0=rect.x,y0=rect.y;
        if(roi instanceof PolygonRoi){
            PolygonRoi pr=(PolygonRoi)roi;
            pnXCoors=pr.getXCoordinates();
            pnYCoors=pr.getYCoordinates();
            len=pnXCoors.length;
            for(i=0;i<len;i++){
                pnXCoors[i]+=rect.x;
                pnYCoors[i]+=rect.y;
            }
            drawPolygon(img,pnXCoors, pnYCoors,roi.getColor(),mag,font);
        }
        if(roi instanceof Line){
            Line line=(Line)(roi);
            drawLine(img,line.x1,line.y1,line.x2,line.y2,roi.getColor(),mag,font);
        }
    }

    public static void drawPolygon(BufferedImage img, int[] pnXCoors, int[] pnYCoors, Color c, int mag,int font){
        int i,len=pnXCoors.length;

        int x0=pnXCoors[len-1],y0=pnYCoors[len-1],x,y;
        for(i=0;i<len;i++){
            x=pnXCoors[i];
            y=pnYCoors[i];
            drawLine(img,x0,y0,x,y,c,mag,font);
        }
    }

    public static void drawLine(BufferedImage img, int x1, int y1, int x2, int y2, Color c, int mag,int font){
       ArrayList<Point> points=new ArrayList();
       CommonMethods.buildConnectin(new Point(x1,y1), new Point(x2,y2), points);
       drawPoint(img,x1*mag, y1*mag, c,font);
       Point pt;
       int i,x0,y0,j,k;
       for(i=0;i<points.size();i++){
           pt=points.get(i);
           x0=pt.x*mag;
           y0=pt.y*mag;
           drawPoint(img,x0,y0,c,font);
       }
       drawPoint(img,x2*mag, y2*mag, c,font);
    }

    public static void drawPoint(BufferedImage img, int x, int y, Color c, int font){
        int i,j;
        for(i=0;i<font;i++){
            for(j=0;j<font;j++){
                img.setRGB(x+j, y+i, c.getRGB());
            }
        }
    }
    public static BufferedImage getBufferedImage(int type, int[][] pixels, int minBrightness, int mag){
        intRange ir=CommonStatisticsMethods.getRange(pixels);
        int h=pixels.length,w=pixels[0].length,len=w*h;
        BufferedImage im=new BufferedImage(w*mag,h*mag,type);
        int i,j,x,y,pixel,r,g,b;
        for(i=0;i<h*mag;i++){
            y=i/mag;
            for(j=0;j<w*mag;j++){
                x=j/mag;
                r=pixels[y][x];
                r=(int)(interpolation(ir.getMin(),minBrightness,ir.getMax(),255,r)+0.5);
                if(r<minBrightness) r=minBrightness;
                if(r>255) r=255;
                g=r;
                b=r;
                pixel=(r<<16)|(g<<8)|b;
                im.setRGB(j,i,pixel);
            }
        }
        return im;
    }
    public static ImageProcessor makeMask(Roi roi){
        Rectangle r=roi.getBounds();
        int l=r.x,t=r.y,h=r.height,w=r.width,i,j,o;
        ByteProcessor bp=new ByteProcessor(w,h);
        byte[] pixels=(byte[])bp.getPixels();
        for(i=t;i<t+h;i++){
            o=(i-t)*w;
            for(j=l;j<l+w;j++){
                if(roi.contains(j, i))
                    pixels[o+j-l]=(byte)255;
                else
                    pixels[o+j-l]=0;
            }
        }
        return bp;
    }
    public static Rectangle getBounds(ArrayList<Point> points){
        intRange irX=new intRange(),irY=new intRange();
        int i,len=points.size();
        Point pt;
        for(i=0;i<len;i++){
            pt=points.get(i);
            irX.expandRange(pt.x);
            irY.expandRange(pt.y);
        }
        Rectangle r=new Rectangle(irX.getMin(),irY.getMin(),irX.getRange(),irY.getRange());
        return r;
    }
    public static EllipseFitter fitEllipse(ArrayList<Point> contour){
        ImageShape cIS=ImageShapeHandler.buildImageShape(contour);
        ArrayList<Point> innerPoints=new ArrayList();
        cIS.getInnerPoints(innerPoints);
        Rectangle r=getBounds(contour);
        int w=r.width,h=r.height,l=r.x,t=r.y,i,len=innerPoints.size();
        byte[] pixels=new byte[w*h];
        Point pt;
        CommonStatisticsMethods.setElements(pixels,(byte)0);
        for(i=0;i<len;i++){
            pt=innerPoints.get(i);
            pixels[(pt.y-t)*w+(pt.x-l)]=1;
        }
        EllipseFitter ef=new EllipseFitter();
        ef.fit(r, pixels);
        return ef;
    }
    public static intRange getPixelRange(ImagePlus impl){
        int i,nSlices=impl.getNSlices();
        intRange pixelRange=new intRange(),ir;
        for(i=1;i<=nSlices;i++){
            ir=getPixelRange(impl,i);
            pixelRange.expandRange(ir);
        }
        return pixelRange;
    }
    public static intRange getPixelRange(ImagePlus impl, int slice){
        impl.setSlice(slice);
        int w=impl.getWidth(),h=impl.getHeight(),x,y;
        intRange ir=new intRange();
        for(y=0;y<h;y++){
            for(x=0;x<w;x++){
                ir.expandRange(impl.getProcessor().getPixel(x, y));
            }
        }
        return ir;
    }
    public static void displayEllipsePars(EllipseFitter ef){
        JTextArea ta=new JTextArea("");
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        String newLine="\n";
        
        String st="xc= "+PrintAssist.ToString(ef.xCenter, 3);
        ta.setText(st);
        st="    yc= "+PrintAssist.ToString(ef.yCenter, 3)+newLine;
        ta.append(st);
        
        st="major ="+PrintAssist.ToString(ef.major, 3);
        ta.append(st);
        
        st="    minor ="+PrintAssist.ToString(ef.minor, 3)+"    Ratio ="+PrintAssist.ToString(ef.major/ef.minor, 3)+ newLine;
        ta.append(st);
        st="angel= "+PrintAssist.ToString(ef.angle, 3)+newLine;
        ta.append(st);
        
        JViewport jvp=new JViewport();
        jvp.setView(ta);
        
        JScrollPane jsp=new JScrollPane();
        jsp.setViewport(jvp);
//        OneScrollPaneFrame.main("Ellipse Parameters", jsp);
        //Create and set up the window.         
        JFrame frame = new JFrame("TextDemo");         
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);           //Add contents to the window.         
        frame.add(jsp);           //Display the window.         
        frame.pack();         
        frame.setVisible(true);                 
    }
    public static boolean isNumeric(String str) 
    { 
        NumberFormat formatter = NumberFormat.getInstance(); 
        ParsePosition pos = new ParsePosition(0); 
        formatter.parse(str, pos); 
        return str.length() == pos.getIndex(); 
    } 
    public static boolean isRegularNumber(double dt){
        return (Double.isInfinite(dt)||Double.isNaN(dt));
    }
    public static DoublePair calCrossingPoint(double b1, double k1, double b2, double k2){
        //this method calculate the crossing point between two lines, y=k1x+b1 and y=k2x+b2
        double x,y;
        x=(b1-b2)/(k2-k1);
        y=k1*x+b1;
        return new DoublePair(x,y);
    }
    public static DoublePair getMidArcPoint(DoublePair p, DoublePair p1, DoublePair p2, double r){
        //returns a point on the line define by p and the midpoint between p1 and p2, and the distance 
        //between p and the point is r;
        DoublePair pm=new DoublePair(0.5*(p1.left+p2.left),0.5*(p1.right+p2.right));
        double dist=getDistance(p.left,p.right,pm.left,pm.right);
        double left=CommonMethods.getLinearIntoplation(0,p.left,dist,pm.left,r);
        double right=CommonMethods.getLinearIntoplation(0,p.right,dist,pm.right,r);
        return new DoublePair(left,right);
    }
    public static void rotate(DoublePair p1, DoublePair p2, double theta){
        //rotate point p2 by theta around p1
        DoublePair p2prime=new DoublePair(p2.left-p1.left,p2.right-p1.right);
        double ct=Math.cos(theta),st=Math.sin(theta);
        double x=p2prime.left*ct-p2prime.right*st,y=p2prime.left*st+p2prime.right*ct;
        p2.left=x+p1.left;
        p2.right=y+p1.right;
    }
    static public int calHeightCutoff(int[][] pixels,ImageShape cIS, ImageShape cBkg, double[] pdPVs, double[] pdPeakHeightCutoff, double[] pdGroovePixelCutoff){
        int h=pixels.length,w=pixels[0].length,i,len,len1=pdPVs.length;
        MeanSem1 ms=new MeanSem1();
        int[][] pixelst=CommonStatisticsMethods.copyArray(pixels);
        CommonMethods.randomize(pixelst);
        ImagePlus impl=CommonMethods.getBlankImage(ImagePlus.GRAY16, w, h);
        CommonMethods.setPixels(impl, pixelst);
        ArrayList<Point> points;
        float fx=1.f,fy=1.f,acc=0.0001f;
        CommonMethods.GaussianBlur(impl, fx, fy, acc);
        
        points=CommonMethods.getSpecialLandscapePoints(impl, LandscapeAnalyzerPixelSorting.localMaximum);
        len=points.size();
        Point p;
        double[] pdHs=new double[len];
        for(i=0;i<len;i++){
            p=points.get(i);
            cIS.setCenter(p);
            cBkg.setCenter(p);
            pdHs[i]=ImageShapeHandler.getMean(pixelst, cIS)-ImageShapeHandler.getMean(pixelst, cBkg);;
        }
        ms=CommonStatisticsMethods.buildMeanSem1(pdHs, 0, len-1, 1);
        for(i=0;i<len1;i++){
            pdPeakHeightCutoff[i]=GaussianDistribution.getZatP(1.-pdPVs[i], ms.mean, ms.getSD(), 0.0001*ms.mean);
        }
        if(pdGroovePixelCutoff==null) return 1;
        points.clear();
        points=CommonMethods.getSpecialLandscapePoints(impl, LandscapeAnalyzerPixelSorting.watershed);
        len=points.size();
        double[] pdt=new double[len];
        for(i=0;i<len;i++){
            p=points.get(i);
            pdt[i]=pixelst[p.y][p.x];
        }
        ms=CommonStatisticsMethods.buildMeanSem1(pdt, 0, len-1, 1);
        for(i=0;i<len1;i++){
            pdGroovePixelCutoff[i]=GaussianDistribution.getZatP(1.-pdPVs[i], ms.mean, ms.getSD(), 0.0001*ms.mean);
        }
        return 1;
    }
    static public void calRegionBorderCutoff(ImagePlus impl, double[] pdPVs, double[] pdBorderPixelCutoff, double[] pdBorderHeightCutoff){
        RegionBoundaryAnalyzer cRBAr=CommonMethods.buildRegionComplex(impl);
    }
    static public int showHeightSelection(ImagePlus impl,double p1,double p2){
        if(impl==null) return -1;
        int i,w=impl.getWidth(),h=impl.getHeight(),len1=IPOAnalyzerForm.pdHeightCutoffPs.length,j;
        int[][] pixels=new int[h][w];
        
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
        ImageShape cIS=new CircleImage(3),cBkg=new Ring(3,4),ring=new Ring(8,9),circle=new CircleImage(8);
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cBkg.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        ring.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        double[] pdCutoff=new double[len1];
        
        int index1=IPOAnalyzerForm.getHeightCutoffPIndex(p1),index2=IPOAnalyzerForm.getHeightCutoffPIndex(p2);
        calHeightCutoff(pixels,cIS,cBkg,IPOAnalyzerForm.pdHeightCutoffPs,pdCutoff,null);
        
        ArrayList<Point> points,SelectedPoints=new ArrayList();
        DoubleRange dr=new DoubleRange(pdCutoff[index2],pdCutoff[index1]);
        if(p1<IPOAnalyzerForm.pdHeightCutoffPs[IPOAnalyzerForm.pdHeightCutoffPs.length-1]*0.1) dr.setMax(Double.POSITIVE_INFINITY);
        ImagePlus implt=CommonMethods.cloneImage(impl);
        float fx=1f,fy=1f,acc=0.0001f;
        CommonMethods.GaussianBlur(implt, fx, fy, acc);
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
        points=CommonMethods.getSpecialLandscapePoints(implt, LandscapeAnalyzerPixelSorting.localMaximum);
        double height;
        Point p;
        IPOAnalyzerForm.showHeightCutoff();
//            public static double[] pdHeightCutoffPs={0.1,0.05,0.01,0.001,0.0001,0.00001,0.000001,0.0000001,0.000000001};

        ArrayList<Integer> indexes=new ArrayList();
        int index=0;
        for(i=0;i<points.size();i++){
            p=points.get(i);
            cIS.setCenter(p);
            cBkg.setCenter(p);
            height=ImageShapeHandler.getMean(pixels, cIS)-ImageShapeHandler.getMean(pixels, cBkg);
            if(!dr.contains(height)) continue;
            index=-1;
            for(j=0;j<len1;j++){
                if(pdCutoff[j]>height){
                    index=j-1;
                    indexes.add(index);
                    break;
                }
            }
            if(index<0) {
                indexes.add(len1);
            }
            SelectedPoints.add(p);
        }
            ImagePlus implSelection=CommonMethods.cloneImage(impl,impl.getCurrentSlice());
            implSelection=CommonMethods.convertImage(implSelection, ImagePlus.COLOR_RGB, true);
            implSelection.setTitle("Selected Objects");
            implSelection.show();
        Color c=Color.yellow;
        
        int[][] pnMark=new int[h][w];
        CommonStatisticsMethods.setElements(pnMark, -1);
        
        for(i=0;i<SelectedPoints.size();i++){
            c=Color.yellow;
            p=SelectedPoints.get(i);
            circle.setCenter(p);
            circle.getInnerPoints(points);
            for(j=0;j<points.size();j++){
                pnMark[points.get(j).y][points.get(j).x]=1;
            }            
        }
        
        for(i=0;i<SelectedPoints.size();i++){
            c=Color.BLACK;
            p=SelectedPoints.get(i);
            ring.setCenter(p);
            ring.getInnerPoints(points);
            index=indexes.get(i);
            if(index>=index2) c=Color.GREEN;
            if(index>=index2+3) c=Color.YELLOW;
            if(index>=index2+5) c=Color.RED;
//            if(index>=3) c=Color.MAGENTA;
            for(j=points.size()-1;j>=0;j--){
                if(pnMark[points.get(j).y][points.get(j).x]==1) points.remove(j);
            }
            CommonMethods.drawTrail(implSelection, points, c.getRGB());
        }
        implSelection.draw();
        return 1;
    }
    public static int drawPolygon(ImagePlus impl, Color c, ArrayList<Point> points){
        impl.setColor(c);
        int w=impl.getWidth(),h=impl.getHeight();
        ContourFollower.removeOffBoundPoints(w, h, points);
        if(points.isEmpty()) return -1;
        int i,len=points.size();
        Point p,p0=points.get(len-1);
        for(i=0;i<len;i++){
            p=points.get(i);
            impl.getProcessor().drawLine(p0.x, p0.y, p.x, p.y);
            p0=p;
        }
        return 1;
    }
    public static int displayPolygon(ArrayList<Point2D> points0){
        intRange xRange=new intRange(),yRange=new intRange();
        ArrayList<Point> points=new ArrayList();
        Point pt;
        Point2D pt2;
        int i,len=points0.size();
        for(i=0;i<len;i++){
            pt2=points0.get(i);
            pt=new Point(pt2.x,pt2.y);
            xRange.expandRange(pt.x);
            yRange.expandRange(pt.y);
        }
        int w=xRange.getRange(),h=yRange.getRange(),x0=xRange.getMin(),y0=yRange.getMin();
        ImagePlus impl=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w, h);
        for(i=0;i<len;i++){
            pt2=points0.get(i);
            pt=new Point(pt2.x-x0,pt2.y-y0);
            points.add(pt);
        }
        CommonMethods.drawPolygon(impl, Color.WHITE, points);
        CommonGuiMethods.showTempImage(impl);
        int[] pixels=(int[])impl.getProcessor().getPixels();
        pt=points.get(0);
        pixels[pt.y*w+pt.x]=Color.RED.getRGB();
        pt=points.get(len-1);
        pixels[pt.y*w+pt.x]=Color.BLUE.getRGB();
        impl.draw();
        return 1;
    }
    public static ArrayList<Integer> getDigits(int num, int base){
        ArrayList<Integer> digits=new ArrayList();
        int q=num/base,r=num-q*base;
        digits.add(r);
        while(q>0){
            num=q;
            q=num/base;
            r=num-q*base;
            digits.add(r);
        }
        return digits;
    }
    public static int getCoordinateRanges(ArrayList<Point> points, int iI, int iF, intRange xRange, intRange yRange){
        xRange.resetRange();
        yRange.resetRange();
        Point p;
        for(int i=0;i<points.size();i++){
            p=points.get(i);
            xRange.expandRange(p.x);
            yRange.expandRange(p.y);
        }
        return 1;
    }
}
    

