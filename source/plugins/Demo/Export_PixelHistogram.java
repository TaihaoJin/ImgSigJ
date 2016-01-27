/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Demo;
import ij.WindowManager;
import utilities.CommonMethods;
import ij.ImagePlus;
import ImageAnalysis.PercentileFilter;
import utilities.Geometry.ImageShapes.ImageShape;
import ImageAnalysis.PixelHeights;
import utilities.statistics.MeanSem0;
import java.util.ArrayList;
import ij.gui.GenericDialog;
import utilities.CommonGuiMethods;
import java.awt.Point;
import utilities.io.FileAssist;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class Export_PixelHistogram implements ij.plugin.PlugIn{
    public void run(String args){
        ImagePlus impl,implm,implr,implrm;
        ArrayList<MeanSem0> sigMS=new ArrayList();
        ArrayList<MeanSem0> surMS=new ArrayList();
        ArrayList<MeanSem0> sigMSr=new ArrayList();
        ArrayList<MeanSem0> surMSr=new ArrayList();
        ArrayList<Point> localMaxima=new ArrayList();
        ArrayList<Point> localMaximar=new ArrayList();
        ArrayList<Integer> sigNeighbors=new ArrayList();
        ArrayList<Integer> surNeighbors=new ArrayList();
        ArrayList<Integer> sigrNeighbors=new ArrayList();
        ArrayList<Integer> surrNeighbors=new ArrayList();
        ArrayList<Integer> sigUncoveredN=new ArrayList();
        ArrayList <Integer> surUncoveredN=new ArrayList();
        ArrayList<Integer> sigrUncoveredN=new ArrayList();
        ArrayList <Integer> surrUncoveredN=new ArrayList();

        GenericDialog gd=new GenericDialog("Choose images");
        ArrayList<ImagePlus> imgs=CommonMethods.getAllOpenImages();
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "original image");
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "mask image");
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "randomized image");
        CommonGuiMethods.addImageSelectionChoices(gd, imgs, "mask of randomized image");
        gd.showDialog();
        int index=gd.getNextChoiceIndex();
        impl=imgs.get(index);
        index=gd.getNextChoiceIndex();
        implm=imgs.get(index);
        index=gd.getNextChoiceIndex();
        implr=imgs.get(index);
        index=gd.getNextChoiceIndex();
        implrm=imgs.get(index);
        PixelHeights.getPixelHeights_localMaxima1(impl, implm, localMaxima, sigMS, surMS,sigNeighbors,surNeighbors,sigUncoveredN,surUncoveredN);
        PixelHeights.getPixelHeights_localMaxima1(implr, implrm, localMaximar, sigMSr, surMSr,sigrNeighbors,surrNeighbors,sigrUncoveredN,surrUncoveredN);

        String path=FileAssist.getFilePath("export pixel height", FileAssist.defaultDirectory, "text file", "txt", false);
        java.util.Formatter fm=FileAssist.getFormatter(path);

        int len1=sigMS.size(),len2=sigMSr.size();
        double ph[]=new double[len1], phr[]=new double[len2];
        int indexes1[]=new int[len1], indexes2[]=new int[len2];
        int i,j;
        int nx=Math.max(len1, len2);
        for(i=0;i<nx;i++){
            if(i<len1){
                ph[i]=sigMS.get(i).mean-surMS.get(i).mean;
                indexes1[i]=i;
            }
            if(i<len2){
                phr[i]=sigMSr.get(i).mean-surMSr.get(i).mean;
                indexes2[i]=i;
            }
        }

        utilities.QuickSort.quicksort(ph, indexes1);
        utilities.QuickSort.quicksort(phr, indexes2);

        PrintAssist.printString(fm, "x", 5);
        PrintAssist.printString(fm, "y", 5);
        PrintAssist.printString(fm, "sigr", 10);
        PrintAssist.printString(fm, "sigrN", 7);
        PrintAssist.printString(fm, "surr", 10);
        PrintAssist.printString(fm, "surrN", 7);
        PrintAssist.printString(fm, "sigNbrs", 9);
        PrintAssist.printString(fm, "surNbrs", 9);
        PrintAssist.printString(fm, "sigrUCA", 9);
        PrintAssist.printString(fm, "surrUCA", 9);
        PrintAssist.printString(fm, "pHeight", 9);

        PrintAssist.printString(fm, "x", 5);
        PrintAssist.printString(fm, "y", 5);
        PrintAssist.printString(fm, "sig", 10);
        PrintAssist.printString(fm, "sigN", 7);
        PrintAssist.printString(fm, "sur", 10);
        PrintAssist.printString(fm, "surN", 7);
        PrintAssist.printString(fm, "sigNbrs", 9);
        PrintAssist.printString(fm, "surNbrs", 9);
        PrintAssist.printString(fm, "sigUCA", 9);
        PrintAssist.printString(fm, "surUCA", 9);
        PrintAssist.printString(fm, "pHeight", 9);
        PrintAssist.printString(fm, PrintAssist.newline, 1);
        for(i=0;i<nx;i++){
            if(i<len2){
                index=indexes2[i];
                PrintAssist.printNumber(fm, localMaximar.get(index).x, 5, 0);
                PrintAssist.printNumber(fm, localMaximar.get(index).y, 5, 0);
                PrintAssist.printNumber(fm, sigMSr.get(index).mean, 10, 1);
                PrintAssist.printNumber(fm, sigMSr.get(index).n, 7, 1);
                PrintAssist.printNumber(fm, surMSr.get(index).mean, 10, 1);
                PrintAssist.printNumber(fm, surMSr.get(index).n, 7, 1);
                PrintAssist.printNumber(fm, sigrNeighbors.get(index), 9, 0);
                PrintAssist.printNumber(fm, surrNeighbors.get(index), 9, 0);
                PrintAssist.printNumber(fm, sigrUncoveredN.get(index), 9, 0);
                PrintAssist.printNumber(fm, surrUncoveredN.get(index), 9, 0);
                PrintAssist.printNumber(fm, phr[i], 9, 0);
            }else{
                PrintAssist.printNumber(fm, -1, 5, 0);
                PrintAssist.printNumber(fm, -1, 5, 0);
                PrintAssist.printNumber(fm, -1, 10, 1);
                PrintAssist.printNumber(fm, -1, 7, 1);
                PrintAssist.printNumber(fm, -1, 10, 1);
                PrintAssist.printNumber(fm, -1, 7, 1);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
            }
            if(i<len1){
                index=indexes1[i];
                PrintAssist.printNumber(fm, localMaxima.get(index).x, 5, 0);
                PrintAssist.printNumber(fm, localMaxima.get(index).y, 5, 0);
                PrintAssist.printNumber(fm, sigMS.get(index).mean, 10, 1);
                PrintAssist.printNumber(fm, sigMS.get(index).n, 7, 1);
                PrintAssist.printNumber(fm, surMS.get(index).mean, 10, 1);
                PrintAssist.printNumber(fm, surMS.get(index).n, 7, 1);
                PrintAssist.printNumber(fm, sigNeighbors.get(index), 9, 0);
                PrintAssist.printNumber(fm, surNeighbors.get(index), 9, 0);
                PrintAssist.printNumber(fm, sigUncoveredN.get(index), 9, 0);
                PrintAssist.printNumber(fm, surUncoveredN.get(index), 9, 0);
                PrintAssist.printNumber(fm, ph[i], 9, 0);
            }else{
                PrintAssist.printNumber(fm, -1, 5, 0);
                PrintAssist.printNumber(fm, -1, 5, 0);
                PrintAssist.printNumber(fm, -1, 10, 1);
                PrintAssist.printNumber(fm, -1, 7, 1);
                PrintAssist.printNumber(fm, -1, 10, 1);
                PrintAssist.printNumber(fm, -1, 7, 1);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
                PrintAssist.printNumber(fm, -1, 9, 0);
            }
            PrintAssist.printString(fm, PrintAssist.newline, 1);
        }
        fm.close();
    }
}
