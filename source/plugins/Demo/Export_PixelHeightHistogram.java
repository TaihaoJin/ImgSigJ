/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Demo;
import ij.WindowManager;
import utilities.CommonMethods;
import ij.ImagePlus;
import java.util.ArrayList;
import java.awt.Point;
import ImageAnalysis.LandscapeAnalyzer;
import utilities.statistics.Histogram;
import java.util.Formatter;
import utilities.io.FileAssist;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class Export_PixelHeightHistogram implements ij.plugin.PlugIn {
    public void run (String args){
        Histogram PixelHeightHist=new Histogram();
        Histogram PeakValueHist=new Histogram();
        Histogram SurroundingMeanHist=new Histogram();
        ArrayList<Double> PeakValues=new ArrayList();
        ArrayList<Double> SurroundingMeans=new ArrayList();
        ArrayList<Double> PixelHeights=new ArrayList();
        ImagePlus impl=WindowManager.getCurrentImage();
        int h=impl.getHeight();
        int w=impl.getWidth();

        int Ri=25, Ro=4;
        int MeanRi[][]=new int[h][w];
        int MeanRo[][]=new int[h][w];


        int[][] pixels=new int[h][w];
        CommonMethods.getPixelValue(impl, 1, pixels);

        int x,y,i,j,pixel;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                MeanRi[i][j]=pixels[i][j];
                MeanRo[i][j]=pixels[i][j];
            }
        }

        CommonMethods.meanFiltering(w, h, MeanRi, Ri);
        CommonMethods.meanFiltering(w, h, pixels, Ro);

        int Ao=(2*Ro+1)*(2*Ro+1);
        int Ai=(2*Ri+1)*(2*Ri+1);
        int dA=Ao-Ai;
        
        ArrayList <Point> localMaxima=new ArrayList();
        int[][] pixelStamp=new int[w][h];
        CommonMethods.getSpecialLandscapePoints(pixels, pixelStamp, false, w, h, LandscapeAnalyzer.localMaximum, localMaxima);
        ArrayList <Double> pixelHeights=new ArrayList();
//        CommonMethods.getPixelHeights_ave(pixelsp, w, h, r, localMaxima, pixelHeights);
        int len=localMaxima.size();


        Point p;
        int nPer=0,perIndex=15;
        int [][] pixelst=pixels;
        double pixelHeight;
        String path=FileAssist.getFilePath("export pixel heights of local maxima", "", "text file", "txt", false);
        Formatter fm=CommonMethods.QuickFormatter(path);
        PrintAssist.printString(fm, "x", 8);
        PrintAssist.printString(fm, "y", 8);
        PrintAssist.printString(fm, "peak", 12);
        PrintAssist.printString(fm, "sum", 12);
        PrintAssist.printString(fm, "surMean", 12);
        PrintAssist.printString(fm, "peakDiff", 12);
        PrintAssist.printString(fm, "sumDiff", 12);
        double peak,sum,surMean,peakDiff,sumDiff;
        for(i=0;i<len;i++){
            p=localMaxima.get(i);
            x=p.x;
            y=p.y;
            PrintAssist.printNumber(fm, x, 8, 0);
            PrintAssist.printNumber(fm, y, 8, 0);
            peak=pixels[y][x];
            PrintAssist.printNumber(fm, peak, 12, 0);
            sum=MeanRi[y][x]*Ai;
            PrintAssist.printNumber(fm, sum, 12, 0);
            surMean=MeanRi[y][x]*Ao-sum;
            surMean/=(Ao-Ai);
            PrintAssist.printNumber(fm, surMean, 12, 0);
            peakDiff=peak-surMean;
            PrintAssist.printNumber(fm, peakDiff, 12, 0);
            sumDiff=sum-surMean*Ai;
            PrintAssist.printNumber(fm, sumDiff, 12, 0);
        }
    }
}
