/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Demo;
import ImageAnalysis.IPOPixelHeights;
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
import utilities.CommonStatisticsMethods;
import ImageAnalysis.IPOPixelHeightsHandler;
/**
 *
 * @author Taihao
 */
public class Export_PixelHeightsIPO implements PlugIn{
    int m_nRMax,m_nRRefI,m_nRRefO,h,w;
    ImagePlus impl,implm, implmr;
    String path;
    public void run(String arg){
//        getParameters();
//        calPixelheights();
//        detectIPOs();
//        exportStackPixelHeights();
    }/*
    void getParameters(){
        path=FileAssist.getFilePath("export auto correlation", "", "text file", "txt", false);
        m_nRMax=8;
        m_nRRefI=5;
        m_nRRefO=8;

        GenericDialog gd=new GenericDialog("Choose images and parameters");
        ArrayList<ImagePlus> imgs=CommonMethods.getAllOpenImages();
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "original image");
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "mask image");
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "reference mask image");

        gd.addNumericField("maximum radius", m_nRMax, 0);
        gd.addNumericField("reference ring inner radius", m_nRRefI, 0);
        gd.addNumericField("reference ring outer radius", m_nRRefO, 0);
        gd.showDialog();


        int index=gd.getNextChoiceIndex();
        impl=imgs.get(index);
        index=gd.getNextChoiceIndex();
        implm=imgs.get(index);
        index=gd.getNextChoiceIndex();
        implmr=imgs.get(index);
        m_nRMax=(int)(gd.getNextNumber()+0.5);
        m_nRRefI=(int)(gd.getNextNumber()+0.5);
        m_nRRefO=(int)(gd.getNextNumber()+0.5);
        h=impl.getHeight();
        w=impl.getWidth();
    }
    void calPixelheights(){
        IPOPixelHeights c=new IPOPixelHeights(CommonMethods.getPixelValues(implm),CommonMethods.getSpecialLandscapePoints(implm, LandscapeAnalyzer.localMaximum),m_nRRefI,m_nRRefO,m_nRMax);
        int len;
//        c.calRefinedPixelHeights();
//        c.calPixelHeights0();
//        c.calRanking();
        c.calPixelStatistics();
        path=FileAssist.getExtendedFileName(path, "_1");
        c.exportRingMeanSem(path);
        path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
//        c.calPixelHeights();
        c.exportPixelHeights(path);

//        c.calRanking();
        c.calCompensatedPixels();
        c.refinePixelStatitics();
        path=FileAssist.getExtendedFileName(path, "_2");
        c.exportRingMeanSem(path);
        path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
//        c.calPixelHeights();
        c.exportPixelHeights(path);

//        c.calRanking();
        c.calCompensatedPixels();
        c.refinePixelStatitics();
        path=FileAssist.getExtendedFileName(path, "_3");
        c.exportRingMeanSem(path);
        path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
//        c.calPixelHeights();
        c.exportPixelHeights(path);

//        c.calRanking();
        c.calCompensatedPixels();
        c.refinePixelStatitics();
        path=FileAssist.getExtendedFileName(path, "_4");
        c.exportRingMeanSem(path);
        path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
//        c.calPixelHeights();
        c.exportPixelHeights(path);
        double[] pixelHeights1=c.getPixelHeights();
        len=pixelHeights1.length;
        ImageShape mark=new Ring(7,7);

        ArrayList points=new ArrayList();
//        c.getLocalMaxima(points);//need to fix it later on 8/2/2010
        int ni=len-150,nf=len-1;
        CommonMethods.labelPointsInOrder(impl, "detected IPOs", points, ni, nf, mark, 0, 0);

        int[][] pixels=CommonMethods.getPixelValues(impl);
        c.updatePixels(pixels);
        c.calCompensatedPixels();
        c.refinePixelStatitics();
        path=FileAssist.getExtendedFileName(path, "_5");
        c.exportRingMeanSem(path);
        path=FileAssist.getExtendedFileName(path, "_PixelHeigths");
//        c.calPixelHeights();
        c.exportPixelHeights(path);
    }
    void detectIPOs(){
        IPOPixelHeights c=new IPOPixelHeights(CommonMethods.getPixelValues(implm),CommonMethods.getSpecialLandscapePoints(implm, LandscapeAnalyzer.localMaximum),m_nRRefI,m_nRRefO,m_nRMax);
        int lenm, lenmr;
        c.calRefinedPixelHeights();
        CommonMethods.displayPixels(c.getCompensatedPixels(), "mask image after compensation", ImagePlus.GRAY16);
        String name=FileAssist.getExtendedFileName(path, "_RingMeanSem_maskImage");
        c.exportRingMeanSem(name);
        name=FileAssist.getExtendedFileName(path, "_PixelHeigths_maskImage");
        c.exportPixelHeights(name);

        ArrayList pointsm=new ArrayList();
        c.getLocalMaxima(pointsm);
        lenm=pointsm.size();
        double[] pixelHeights0m=new double[lenm];
        c.getPixelHeights0(pixelHeights0m);

        int[][] pixels=CommonMethods.getPixelValues(impl);
        c.updatePixels(pixels);
        c.calCompensatedPixels();
        CommonMethods.displayPixels(c.getCompensatedPixels(), "original image after compensation", ImagePlus.GRAY16);
        c.refinePixelStatitics();
        name=FileAssist.getExtendedFileName(path, "_RingMeanSem_originalImage");
        c.exportRingMeanSem(name);
        name=FileAssist.getExtendedFileName(path, "_PixelHeigths_originalImage");
        c.exportPixelHeights(name);

        IPOPixelHeights cr=new IPOPixelHeights(CommonMethods.getPixelValues(implmr),CommonMethods.getSpecialLandscapePoints(implmr, LandscapeAnalyzer.localMaximum),m_nRRefI,m_nRRefO,m_nRMax);
        cr.calRefinedPixelHeights();
        name=FileAssist.getExtendedFileName(path, "_RingMeanSem_referenceImage");
        cr.exportRingMeanSem(name);
        name=FileAssist.getExtendedFileName(path, "_PixelHeigths_referenceImage");
        cr.exportPixelHeights(name);

        lenmr=cr.getNumLocalMaxima();
        double[] pixelHeights0mr=new double[lenmr];
        cr.getPixelHeights0(pixelHeights0mr);

        double dP=0.01;
        int index=(int)((1-dP)*lenmr);
        double hc=pixelHeights0mr[index];

        int ni=0,nf=lenm-1;
        for(int i=nf;i>=0;i--){
            if(pixelHeights0m[i]<hc) break;
            ni=i;
        }
        ImageShape mark=new Ring(7,7);
//        c.getLocalMaxima(pointsm);
        CommonMethods.labelPointsInOrder(impl, "detected IPOs", pointsm, ni, nf, mark, 0, 0);

        double[] backgroundPercentiles=new double[16];
        backgroundPercentiles=new double[16];
        backgroundPercentiles[0]=0.0000000001;
        backgroundPercentiles[1]=0.00000001;
        backgroundPercentiles[2]=0.0000001;
        backgroundPercentiles[3]=0.00001;
        backgroundPercentiles[4]=0.0001;
        backgroundPercentiles[5]=0.001;
        backgroundPercentiles[6]=0.01;
        backgroundPercentiles[7]=0.05;
        backgroundPercentiles[8]=0.1;
        backgroundPercentiles[9]=0.2;
        backgroundPercentiles[10]=0.3;
        backgroundPercentiles[11]=0.4;
        backgroundPercentiles[12]=0.5;
        backgroundPercentiles[13]=0.6;
        backgroundPercentiles[14]=0.7;
        backgroundPercentiles[15]=1.;

        ArrayList<Double> pixelHeights0=new ArrayList(), pixelHeights=new ArrayList();
        int[] percentileIndexes=new int[16];
        int[][] pixelsm=CommonMethods.getPixelValues(implm), pixelsmr=CommonMethods.getPixelValues(implmr);
        IPOPixelHeightsHandler.detectIPOs(pixels, pixelsm, pixelsmr, pointsm, pixelHeights, pixelHeights0, m_nRRefI, m_nRRefO, m_nRMax, backgroundPercentiles, percentileIndexes);
        CommonMethods.labelPointsInOrder(impl, "detected IPOs", pointsm, percentileIndexes, mark, 5, 0, 0);
    }
    void exportStackPixelHeights(){
        ArrayList<Integer> nvRadius=new ArrayList();
        ImagePlus implc=CommonMethods.cloneImage(impl);
        ImagePlus implmc=CommonMethods.cloneImage(implm);
        implc.setTitle("compensated pixels for the original image");
        implmc.setTitle("compensated pixels for the processed image");
        ImagePlus implcr=CommonMethods.cloneImage(impl);
        implcr.setTitle("compensated pixels for the processed reference image");
        IPOPixelHeightsHandler.exportStackPixelHeights(impl, implm, implc, implmc,"export pixel heights for the original image","");
//        IPOPixelHeightsHandler.exportStackPixelHeights(implm, implc, "export pixel heights for the processed image");
        implc.show();
        IPOPixelHeightsHandler.exportStackPixelHeights(implmr, implcr, "export pixel heights for the processed reference image");
        implcr.show();
    }*/
}
