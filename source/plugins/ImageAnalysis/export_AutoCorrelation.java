/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ImageAnalysis.PixelAutoCorrelation;
import java.util.Formatter;
import utilities.QuickFormatter;
import utilities.io.FileAssist;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import utilities.CommonGuiMethods;
import java.util.ArrayList;
import utilities.CommonMethods;
import java.awt.Point;
import ImageAnalysis.LandscapeAnalyzer;
import ImageAnalysis.PixelHeights;
import utilities.Geometry.ImageShapes.*;
import utilities.CustomDataTypes.intRange;
/**
 *
 * @author Taihao
 */
public class export_AutoCorrelation implements PlugIn{
    public static final int originalRef=0;
    public static final int localMeanRef=1;
    public static final int localMedianRef=2;
    public static final int globalMeanRef=3;
    public static final int globalMedianRef=4;
    int m_nRMax,m_nRRefI,m_nRRefO,m_nRefType,h,w;
    ImagePlus impl,implm;
    boolean m_bBasedOnLocalMaxima;
    String path;
    double m_dQi,m_dQf;
    public void run(String arg){
        getParameters();
        calPixelheights();
    }
    void getParameters(){
        path=FileAssist.getFilePath("export auto correlation", "", "text file", "txt", false);
        m_nRMax=9;
        m_nRRefI=5;
        m_nRRefO=9;

        GenericDialog gd=new GenericDialog("Choose images and parameters");
        ArrayList<ImagePlus> imgs=CommonMethods.getAllOpenImages();
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "original image");
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "mask image");

        int len=5;
        int[] nRefTypes=new int[len];
        String[] sRefTypes=new String[len];
        nRefTypes[0]=originalRef;
        nRefTypes[1]=localMeanRef;
        nRefTypes[2]=localMedianRef;
        nRefTypes[3]=globalMeanRef;
        nRefTypes[4]=globalMedianRef;

        sRefTypes[0]="originalRef";
        sRefTypes[1]="localMeanRef";
        sRefTypes[2]="localMedianRef";
        sRefTypes[3]="globalMeanRef";
        sRefTypes[4]="globalMedianRef";
        gd.addChoice("reference pixel value", sRefTypes, "localMeanRef");

        gd.addNumericField("maximum radius", m_nRMax, 0);
        gd.addNumericField("reference ring inner radius", m_nRRefI, 0);
        gd.addNumericField("reference ring outer radius", m_nRRefO, 0);
        gd.addCheckbox("including local maxima of the mask image only", true);
        gd.addNumericField("lower end quantile", 0, 1);
        gd.addNumericField("higher end quantile", 1, 1);

        gd.showDialog();


        int index=gd.getNextChoiceIndex();
        impl=imgs.get(index);
        index=gd.getNextChoiceIndex();
        implm=imgs.get(index);
        index=gd.getNextChoiceIndex();
        m_nRefType=nRefTypes[index];
        m_nRMax=(int)(gd.getNextNumber()+0.5);
        m_nRRefI=(int)(gd.getNextNumber()+0.5);
        m_nRRefO=(int)(gd.getNextNumber()+0.5);
        m_dQi=gd.getNextNumber();
        m_dQf=gd.getNextNumber();
        m_bBasedOnLocalMaxima=gd.getNextBoolean();

        h=impl.getHeight();
        w=impl.getWidth();
    }
    void calPixelheights(){
        PixelAutoCorrelation c=new PixelAutoCorrelation(impl,m_nRRefI,m_nRRefO,m_nRMax,m_nRefType);
        int len;
        int index;
        int indexes[]=new int[1];
        if(m_bBasedOnLocalMaxima){
            ArrayList<Point> points=CommonMethods.getSpecialLandscapePoints(implm, LandscapeAnalyzer.localMaximum);
            ArrayList<Integer> pixels=new ArrayList();
            ArrayList<Double> pixelHeights=new ArrayList();
            ImageShape sigShape=new CircleImage(0), surShape=new Ring(m_nRRefI,m_nRRefO);
            sigShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
            surShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
            PixelHeights.getPixelHeigths(implm, sigShape, surShape, points, pixelHeights, localMedianRef, 0.5);

//            int[][] pixels=new int[h][w];
//            CommonMethods.getPixelValue(implm, 1, pixels);

            len=points.size();
            Point p;
            double[] dvs=new double[len];
            indexes=new int[len];
            int i;
            for(i=0;i<len;i++){
                p=points.get(i);
                dvs[i]=pixelHeights.get(i);
                indexes[i]=i;
            }
            utilities.QuickSort.quicksort(dvs, indexes);
            int ni=(int)(m_dQi*len), nf=(int)(m_dQf*len);
            if(nf>=len) nf=len-1;
            ArrayList<Point> points1=new ArrayList();
            for(i=ni;i<=nf;i++){
                index=indexes[i];
                p=points.get(index);
                points1.add(p);
                pixels.add((int)(implm.getProcessor().getPixelValue(p.x, p.y)+0.5));
            }
            c.presetPoints(points1,pixels);

            c.calPixelStatistics();
            c.calAutoCorrelation_RingMean(m_nRefType);
            c.exportAutoCorrelation(path);
            path=FileAssist.getExtendedFileName(path, "_RingMeanSem");
            c.exportRingMeanSem(path);
            path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
            c.calPixelHeights();
            c.exportPixelHeights(path);

            c.calCompensatedPixels();
            c.refinePixelStatitics();
            path=FileAssist.getExtendedFileName(path, "_refined");
            c.exportRingMeanSem(path);
            path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
            c.calPixelHeights();
            c.exportPixelHeights(path);

            double pixelHeights1[]=c.getPixelHeights();
            len=pixelHeights1.length;
            utilities.QuickSort.quicksort(pixelHeights1, indexes);
            ni=(int)(m_dQi*len);
            nf=(int)(m_dQf*len);
            if(nf>=len) nf=len-1;
            points1.clear();
            pixels.clear();
            for(i=ni;i<=nf;i++){
                index=indexes[i];
                p=points.get(index);
                points1.add(p);
                pixels.add((int)(implm.getProcessor().getPixelValue(p.x, p.y)+0.5));
            }
            c.presetPoints(points1,pixels);

            c.calPixelStatistics();
            c.calAutoCorrelation_RingMean(m_nRefType);
            c.exportAutoCorrelation(path);
            path=FileAssist.getExtendedFileName(path, "_RingMeanSem");
            c.exportRingMeanSem(path);
            path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
            c.calPixelHeights();
            c.exportPixelHeights(path);

            c.calCompensatedPixels();
            c.refinePixelStatitics();
            path=FileAssist.getExtendedFileName(path, "_refined");
            c.exportRingMeanSem(path);
            path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
            c.calPixelHeights();
            c.exportPixelHeights(path);

            pixelHeights1=c.getPixelHeights();
            len=pixelHeights1.length;
            utilities.QuickSort.quicksort(pixelHeights1, indexes);
            points1.clear();
            pixels.clear();
            for(i=ni;i<=nf;i++){
                index=indexes[i];
                p=points.get(index);
                points1.add(p);
                pixels.add((int)(implm.getProcessor().getPixelValue(p.x, p.y)+0.5));
            }
            ImageShape mark=new Ring(6,7);
            ni=len-150;
            nf=len-1;
            CommonMethods.labelPointsInOrder(impl, "detected IPOs", points1, ni, nf, mark, 0, 0);
        }
    }
}
