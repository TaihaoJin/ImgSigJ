/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;
import ij.ImagePlus;
import ij.WindowManager;
import utilities.CommonGuiMethods;
import utilities.io.PrintAssist;
import utilities.io.FileAssist;
import utilities.CustomDataTypes.*;
import utilities.CommonMethods;
import ImageAnalysis.ImageScanner;
import utilities.statistics.Histogram;
import utilities.Geometry.ImageShapes.*;
import java.util.ArrayList;
import java.awt.Point;
import utilities.statistics.MeanSem0;
import utilities.CommonStatisticsMethods;
import java.util.Formatter;
import utilities.QuickFormatter;
import ij.IJ;
import java.awt.Color;

/**
 *
 * @author Taihao
 */
public class IPOPixelHeights{
//    ImagePlus impl;
    int m_nRMax,m_nRRefI, m_nRRefO,w,h;//m_nRMax the radius of the largest ring, m_nRRef is the readius of the the reference circle.
    int m_pnPixels[][], m_pnPixelsTemp[][], m_pnPixelsCompensated[][],m_pnPixelsProcessed[][],m_pnPixelsProcessed1[][];//the pixel values using local reference: the mean (or median) of the reference circle.
    ArrayList <ImageShape> m_cvRings;
    ArrayList <MeanSem0> m_cvRingMeanSems;
    MeanSem0[][] m_pcRingMeanSems;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum i and ring r
    //Added one extra collumn for each row to store the mean background over large area. 7/30/2010
    MeanSem0[][] m_pcRingMeanSems_Processed;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum i and ring r
    //Added one extra collumn for each row to store the mean background over large area. 7/30/2010
    MeanSem0[][] m_pcRingMeanSems_Group;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
    MeanSem0[][] m_pcRingMeanSems_Normalized;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
    int[][] m_pnAdjustedPixels;
    ImageShape m_cRefRing,backgroundRing;
    double m_pdPixelHeights[];//Acummulated pixel values using m_cRefRing as reference
    double m_pdPixelHeights_Contour[];//Acummulated pixel values using m_cRefRing as reference
    double m_pdPixelHeights0[];//the difference between the pixel value of the center and the mean of m_cRefRing
    double m_dDeltaPercentile;
    int[] m_pnAveRadius;//radius of a percentile group
    int[][]m_pnStamp;//stamp points of local maxima and the circle with the radius of the percentile group.
    ArrayList<Double> m_dvRs;//radius of m_cvRings. the last element of m_cvRings is always the m_cRefRing, so for this one, m_dvRs is not a real radius
    int m_nPercentileDivision;//The number of the groups the local maxima will be grouped according to their pixel heights
    int m_nExcludingType;//the integer value being used to stamp local maxima and the circles.
    ArrayList<Point> m_cvLocalMaxima;
    int m_pnRankingIndexes[],m_pnIndexesPt[];
    int m_nNumRings,m_nNumLocalMaxima;
    boolean m_bRankedPixelHeights0,m_bAllowRankingPixelHeights;
    int m_nR0; //m_nR0 is the index for the largest ring in the signal area
    ImagePlus implC;

    public IPOPixelHeights(int[][] pixels, ArrayList<Point> localMaxima, int rRefI, int rRefO, int rMax){//nRefType 0 for median, 1 for mean and -1 for not using local reference
        h=pixels.length;
        w=pixels[0].length;
        m_nRMax=rMax;
        m_nRRefI=rRefI;
        m_nRRefO=rRefO;
        m_dDeltaPercentile=0.01;
        m_bRankedPixelHeights0=false;
        init();
        updatePixels(pixels,localMaxima);
        m_bAllowRankingPixelHeights=true;
    }

    void init(){
        int i,r;
        m_nExcludingType=-1;
        m_pnPixelsCompensated=new int[h][w];
//        m_pnPixels=new int[h][w];
        m_pnPixelsTemp=new int[h][w];
        m_pnPixelsProcessed=new int[h][w];
        m_pnPixelsProcessed1=new int[h][w];
        implC=CommonMethods.displayPixels(m_pnPixelsTemp, "contours", ImagePlus.COLOR_RGB);

        constructRings();
        m_nNumRings=m_cvRings.size();
        m_cvRingMeanSems=new ArrayList();

        Point p=new Point(0,0);

        m_nPercentileDivision=(int)(1./m_dDeltaPercentile);
        m_pcRingMeanSems_Group=new MeanSem0[m_nPercentileDivision][m_nNumRings];
        m_pcRingMeanSems_Normalized=new MeanSem0[m_nPercentileDivision][m_nNumRings];
        m_pnAveRadius=new int[m_nPercentileDivision];
        for(i=0;i<m_nPercentileDivision;i++){
            for(r=0;r<m_nNumRings;r++){
                m_pcRingMeanSems_Group[i][r]=new MeanSem0();
                m_pcRingMeanSems_Normalized[i][r]=new MeanSem0();
            }
        }


        m_pnStamp=new int[h][w];
        CommonStatisticsMethods.setElements(m_pnStamp,0);
        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems_Group);
        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems_Normalized);
    }
    public void updatePixels(int[][] pixels){
        m_pnPixels=pixels;
    }
    public MeanSem0[][] getPcRingMeanSems(){
        return m_pcRingMeanSems;
    }
    public MeanSem0[][] getPcRingMeanSems_Group(){
        return m_pcRingMeanSems_Group;
    }
    public MeanSem0[][] getPcRingMeanSems_Normalized(){
        return m_pcRingMeanSems_Normalized;
    }
    public int getnNumPercentileDivisions(){
        return m_nPercentileDivision;
    }
    public void updatePixels(int[][] pixels, ArrayList<Point> localMaxima){
        m_pnPixels=pixels;
        m_cvLocalMaxima=localMaxima;
        m_nNumLocalMaxima=m_cvLocalMaxima.size();
        int i,r;

        int len=0;
        if(m_pcRingMeanSems!=null) len=m_pcRingMeanSems.length;
        if(m_nNumLocalMaxima>len){
            m_pnRankingIndexes=new int[m_nNumLocalMaxima];
            m_pnIndexesPt=new int[m_nNumLocalMaxima];
            m_pcRingMeanSems=new MeanSem0[m_nNumLocalMaxima][m_nNumRings];
            m_pcRingMeanSems_Processed=new MeanSem0[m_nNumLocalMaxima][m_nNumRings];
            m_pnAdjustedPixels=new int[m_nNumLocalMaxima][m_nNumRings];
            m_pdPixelHeights=new double[m_nNumLocalMaxima];
            m_pdPixelHeights0=new double[m_nNumLocalMaxima];
            m_pdPixelHeights_Contour=new double[m_nNumLocalMaxima];
        }

        for(i=0;i<m_nNumLocalMaxima;i++){
            m_pnRankingIndexes[i]=i;
        }

        for(i=0;i<m_nNumLocalMaxima;i++){
            for(r=0;r<m_nNumRings;r++){
                m_pcRingMeanSems[i][r]=new MeanSem0();
                m_pcRingMeanSems_Processed[i][r]=new MeanSem0();
            }
        }

        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems);
        CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
    }
    public void noRanking(){
        m_bRankedPixelHeights0=true;
    }
    public int calRanking(){
        if(m_bRankedPixelHeights0||!m_bAllowRankingPixelHeights) return -1;
        int len=m_pnIndexesPt.length,i;
        double[] pdv=new double[m_nNumLocalMaxima];
        for(i=0;i<m_nNumLocalMaxima;i++){
            m_pnIndexesPt[i]=i;
            pdv[i]=m_pdPixelHeights0[i];
        }
        for(i=m_nNumLocalMaxima;i<len;i++){
            m_pnIndexesPt[i]=i;
            m_pdPixelHeights0[i]=Double.MAX_VALUE-20*Math.random();
        }
        utilities.QuickSort.quicksort(pdv, m_pnIndexesPt);
        int index;
        for(i=0;i<m_nNumLocalMaxima;i++){
            index=m_pnIndexesPt[i];
            m_pnRankingIndexes[index]=i;
        }
        m_bRankedPixelHeights0=true;
        return 1;
    }

    public int[] getPtRankingIndexes(){//m_pnIndexesPt[i] stores the index of a local maximum whose ranking of the pixelHeight0 is the i-th in ascending order.
        return m_pnIndexesPt;
    }

    public int[] getRanking(){//getRankingIndexes[i] stores the ranking of pixelHeight0 of the i-th local maximum in ascending order.
        return m_pnRankingIndexes;
    }
    void constructRings(){
        m_cvRings=new ArrayList();
        m_dvRs=new ArrayList();
        IPOPixelHeightsHandler.constructIPORings(m_nRMax, m_nRRefI, m_nRRefO, m_cvRings, m_dvRs);
        int len=m_cvRings.size();
        m_cRefRing=m_cvRings.get(len-2);
        backgroundRing=m_cvRings.get(len-1);
        ImageShapeHandler.setFrameRanges(m_cvRings,new intRange(0,w-1), new intRange(0,h-1));
    }

    double getRefPixel(Point pt){
        m_cRefRing.setCenter(pt);
        return (double) ImageShapeHandler.getMean(m_pnPixelsCompensated, m_cRefRing);
    }

    public void calPixelStatistics(){
        int i,c;
        int y,y0=0;
        double dRef;
        Point p;
        for(i=0;i<m_nNumLocalMaxima;i++){
            p=m_cvLocalMaxima.get(i);
            y=p.y;
//            if(y>y0) IJ.showStatus("calPixelStatistics "+PrintAssist.ToString(y)+"-th line of "+PrintAssist.ToString(h)+" lines!");
            dRef=getRefPixel(p);
            ImageShapeHandler.calMeanSems(m_pnPixels, dRef ,m_cvRings, p, m_cvRingMeanSems,0,m_nNumRings-3);
//            m_cvRingMeanSems.set(m_nNumRings-1, ImageShapeHandler.getPixelMeanSem(m_pnPixels, m_cRefRing));
            m_cvRingMeanSems.add(ImageShapeHandler.getPixelMeanSem(m_pnPixels, m_cvRings.get(m_nNumRings-2)));
            m_cvRingMeanSems.add(ImageShapeHandler.getPixelMeanSem(m_pnPixels, m_cvRings.get(m_nNumRings-1)));
            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems[i][c].update(m_cvRingMeanSems.get(c));
            }
        }
        calPixelHeights();
        calRanking();
        double dDelta=(double)m_nNumLocalMaxima/(double)m_nPercentileDivision;
        double dPosition=0;
        int rI,rF;
        rI=0;
        for(i=0;i<m_nPercentileDivision;i++){
            dPosition+=dDelta;
            rF=Math.min(m_nNumLocalMaxima-1,(int)(dPosition+0.5));
            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems_Group[i][c]=CommonStatisticsMethods.mergedMeanSem(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,c);
                m_pcRingMeanSems_Normalized[i][c]=CommonStatisticsMethods.mergedMeanSem_Normalized(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,0,c);
             }
            rI=rF+1;
        }
    }

    public void exportRingMeanSem(String path){
        int i,r,c;
        MeanSem0 MeanSem, MeanSemNR;
        int num=m_nNumLocalMaxima/m_nPercentileDivision;
        int rI,rF;
        path=FileAssist.getExtendedFileName(path, " -all percentile");
        Formatter fm=QuickFormatter.getFormatter(path);

        for(i=0;i<m_nPercentileDivision;i++){
            PrintAssist.printString(fm, "dist"+PrintAssist.ToString(i), 8);
            PrintAssist.printString(fm, "mean"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "SD"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "meanNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "semNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "num"+PrintAssist.ToString(i), 10);
        }

        PrintAssist.printString(fm, "dist", 8);
        PrintAssist.printString(fm, "meanAllPts", 12);
        PrintAssist.printString(fm, "SD_AllPts", 12);
        PrintAssist.printString(fm, "numAllPts", 10);

        PrintAssist.endLine(fm);
        for(c=0;c<m_nNumRings;c++){
            for(i=0;i<m_nPercentileDivision;i++){
                PrintAssist.printNumber(fm, m_dvRs.get(c), 8, 2);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Group[i][c].mean, 12, 3);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Group[i][c].getSD(), 12, 4);

//                rI=Math.max(0, (int)((i-.5)*num));
//                rF=Math.min(m_nNumLocalMaxima-1,(int)((i+.5)*num));
//                MeanSemNR=CommonStatisticsMethods.mergedMeanSem_Normalized(m_pcRingMeanSems, m_pnRankingIndexes, rI, rF, 1, 0, c);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Normalized[i][c].mean, 12, 4);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Normalized[i][c].getSD(), 12, 4);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Group[i][c].n, 10, 0);
            }

            rI=0;
            rF=m_nPercentileDivision-1;
            MeanSemNR=CommonStatisticsMethods.mergedMeanSem_Normalized(m_pcRingMeanSems, m_pnRankingIndexes, rI, rF, 1, 0, c);
            MeanSem=CommonStatisticsMethods.mergedMeanSem(m_pcRingMeanSems, rI, rF, 1, c);
            PrintAssist.printNumber(fm, m_dvRs.get(c), 8, 2);
            PrintAssist.printNumber(fm, MeanSem.mean, 12, 3);
            PrintAssist.printNumber(fm, MeanSem.getSD(), 12, 4);
            PrintAssist.printNumber(fm, MeanSemNR.mean, 12, 4);
            PrintAssist.printNumber(fm, MeanSemNR.getSD(), 12, 4);
            PrintAssist.printNumber(fm, MeanSemNR.n, 10, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();
        path=FileAssist.getExtendedFileName(path, "all percentile");
    }

    public int calAdjustedPixels(){
        CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
//        CommonMethods.displayPixels(m_pnPixelsCompensated, "before compensation", impl.getType());
        Point pt;
        int index,nRank,pixel,r,c;
        ImageShape shape;
        double peak,ratio;
        boolean adjust=true;
        for(r=0;r<m_nNumLocalMaxima;r++){
            adjust=true;
            pt=m_cvLocalMaxima.get(r);
            peak=m_pcRingMeanSems[r][0].mean;
            nRank=m_pnRankingIndexes[r];
            index=getPercentileIndex(nRank);
            for(c=0;c<m_nNumRings;c++){
                m_pnAdjustedPixels[r][c]=0;
                ratio=m_pcRingMeanSems_Normalized[index][c].mean;
                pixel=(int)(peak*ratio);
                if(pixel<1) adjust=false;
                if(!adjust) continue;
                m_pnAdjustedPixels[r][c]=pixel;
            }
        }
        return 1;
    }
    public int calCompensatedPixels(){
        calAdjustedPixels();
        calCompensatedPixels(m_pnAdjustedPixels);
        return 1;
    }
    public int getNumRings(){
        return m_cvRings.size();
    }
    public int calCompensatedPixels(int[][] adjustedPixels){
        m_pnAdjustedPixels=adjustedPixels;
        CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
//        CommonMethods.displayPixels(m_pnPixelsCompensated, "before compensation", impl.getType());
        Point pt;
        int index,nRank,pixel,r,c;
        double dsur;
        ImageShape shape;
        double peak,ratio;
        boolean adjust=true;
        for(r=0;r<m_nNumLocalMaxima;r++){
            pt=m_cvLocalMaxima.get(r);
            for(c=0;c<m_nNumRings;c++){
                pixel=m_pnAdjustedPixels[r][c];
                if(pixel<1) break;
                shape=m_cvRings.get(c);
                shape.setCenter(pt);
                ImageShapeHandler.addValue(m_pnPixelsCompensated, shape, -pixel);
            }
        }
        return 1;
    }
    void stampIPOs(){
        calAveRadius();
        int i,r;
        for(i=0;i<h;i++){
            for(r=0;r<w;r++){
                m_pnStamp[i][r]=0;
            }
        }
        int numRings=m_cvRings.size();
        Point pt;
        int index,nRank, step=0,r0;
        ImageShape shape;
        for(i=0;i<m_nNumLocalMaxima;i++){
            pt=m_cvLocalMaxima.get(i);
            nRank=m_pnRankingIndexes[i];
            index=getPercentileIndex(nRank);
            r0=m_pnAveRadius[index];
            for(r=0;r<=r0;r++){
                shape=m_cvRings.get(r);
                shape.setCenter(pt);
                ImageShapeHandler.setValue(m_pnStamp, shape, m_nExcludingType);
            }
        }
    }

    void calAveRadius(){
        int len=m_pcRingMeanSems_Group.length;
        double sd,dSur;
        MeanSem0 ms;
        int i,j;
        for(i=0;i<len;i++){
            dSur=m_pcRingMeanSems_Group[i][m_nNumRings-1].mean;
            for(j=0;j<m_nNumRings;j++){
                ms=m_pcRingMeanSems_Group[i][j];
                sd=ms.getSD();
//                if((ms.mean-dSur)<2*sd) break;
                if((ms.mean-dSur)<2*sd) break;
                m_pnAveRadius[i]=j;
            }
        }
    }

    public int refinePixelStatitics(){
        stampIPOs();
        refinePixelStatitics(m_pnStamp);
        return 1;
    }
    public int refinePixelStatitics_noSorting(){
//        stampIPOs();
        m_bAllowRankingPixelHeights=false;
        refinePixelStatitics(m_pnStamp);
        m_bAllowRankingPixelHeights=true;
//        refinePixelStatitics_noSorting(m_pnStamp);
        return 1;
    }

    public int refinePixelStatitics(int[][] stamp){
        m_pnStamp=stamp;
        int i,c;
        Point p;
        ImageShape shape;
        int y,y0=0;
        int pixel;
        double dsur,dRef;
        for(i=0;i<m_nNumLocalMaxima;i++){
            p=m_cvLocalMaxima.get(i);
            y=p.y;
//            if(y>y0) IJ.showStatus("refinePixelStatistics "+PrintAssist.ToString(y)+"-th line of "+PrintAssist.ToString(h)+" lines!");
            ImageShapeHandler.copyElements(m_pnPixelsCompensated, m_pnPixelsTemp, m_cvRings, p);

            for(c=0;c<m_nNumRings;c++){
                pixel=m_pnAdjustedPixels[i][c];
                if(pixel<1) break;
                shape=m_cvRings.get(c);
                shape.setCenter(p);
                ImageShapeHandler.addValue(m_pnPixelsTemp, shape, pixel);
            }
            dRef=ImageShapeHandler.getMean(m_pnPixelsTemp, m_cRefRing,m_pnStamp,m_nExcludingType);
            ImageShapeHandler.calMeanSems(m_pnPixelsTemp, dRef ,m_cvRings, p, m_cvRingMeanSems,0,m_nNumRings-3);
            m_cvRingMeanSems.add(new MeanSem0());
            m_cvRingMeanSems.add(new MeanSem0());
            ImageShapeHandler.calMeanSem(m_pnPixelsTemp, 0 ,m_cvRings.get(m_nNumRings-2), p, m_cvRingMeanSems.get(m_nNumRings-2),m_pnStamp,m_nExcludingType);
            ImageShapeHandler.calMeanSem(m_pnPixelsTemp, 0 ,m_cvRings.get(m_nNumRings-1), p, m_cvRingMeanSems.get(m_nNumRings-1),m_pnStamp,m_nExcludingType);
            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems[i][c].update(m_cvRingMeanSems.get(c));
            }
        }
        calPixelHeights();
        calRanking();

        double dDelta=(double)m_nNumLocalMaxima/(double)m_nPercentileDivision;
        double dPosition=0;
        int rI,rF;
        rI=0;

        for(i=0;i<m_nPercentileDivision;i++){
            dPosition+=dDelta;
            rF=Math.min(m_nNumLocalMaxima-1,(int)(dPosition+0.5));
            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems_Group[i][c]=CommonStatisticsMethods.mergedMeanSem(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,c);
                m_pcRingMeanSems_Normalized[i][c]=CommonStatisticsMethods.mergedMeanSem_Normalized(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,0,c);
            }
            rI=rF+1;
        }

        return 1;
    }
     public int refinePixelStatitics_noSorting(int[][] stamp){
        m_pnStamp=stamp;
        int i,c;
        Point p;
        ImageShape shape;
        int y,y0=0;
        int pixel;
        double dsur,dRef;
        for(i=0;i<m_nNumLocalMaxima;i++){
            p=m_cvLocalMaxima.get(i);
            y=p.y;
//            if(y>y0) IJ.showStatus("refinePixelStatistics "+PrintAssist.ToString(y)+"-th line of "+PrintAssist.ToString(h)+" lines!");
            ImageShapeHandler.copyElements(m_pnPixelsCompensated, m_pnPixelsTemp, m_cvRings, p);

            for(c=0;c<m_nNumRings;c++){
                pixel=m_pnAdjustedPixels[i][c];
                if(pixel<1) break;
                shape=m_cvRings.get(c);
                shape.setCenter(p);
                ImageShapeHandler.addValue(m_pnPixelsTemp, shape, pixel);
            }
            dRef=ImageShapeHandler.getMean(m_pnPixelsTemp, m_cRefRing,m_pnStamp,m_nExcludingType);
            ImageShapeHandler.calMeanSems(m_pnPixelsTemp, dRef ,m_cvRings, p, m_cvRingMeanSems);
            ImageShapeHandler.calMeanSem(m_pnPixelsTemp, 0 ,m_cvRings.get(m_nNumRings-1), p, m_cvRingMeanSems.get(m_nNumRings-1),m_pnStamp,m_nExcludingType);
            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems[i][c].update(m_cvRingMeanSems.get(c));
            }
        }
        calPixelHeights();
//        calRanking();
        double dDelta=(double)m_nNumLocalMaxima/(double)m_nPercentileDivision;
        double dPosition=0;
        int rI,rF;
        rI=0;
        for(i=0;i<m_nPercentileDivision;i++){
            dPosition+=dDelta;
            rF=Math.min(m_nNumLocalMaxima-1,(int)(dPosition+0.5));
            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems_Group[i][c]=CommonStatisticsMethods.mergedMeanSem(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,c);
                m_pcRingMeanSems_Normalized[i][c]=CommonStatisticsMethods.mergedMeanSem_Normalized(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,0,c);
            }
            rI=rF+1;
        }
        return 1;
    }
    void calR0(){//m_nR0 is the index for the largest ring in the signal area
        int i,nSize=m_dvRs.size();
        double r;
        m_nR0=-1;

        for(i=0;i<nSize;i++){
            r=m_dvRs.get(i);
            if(r>=m_nRRefI) break;
            m_nR0=i;
        }
    }
    public void calPixelHeights(){
        int nSize=m_dvRs.size();
        int i,c,r0=0;
        double r;

        for(i=0;i<nSize;i++){
            r=m_dvRs.get(i);
            if(r>=m_nRRefI) break;
            r0++;
        }

        double height=0;
        int nArea;
        for(i=0;i<m_nNumLocalMaxima;i++){
            height=0;
            for(c=0;c<=r0;c++){
                nArea=m_cvRings.get(c).getArea();
                height+=nArea*(m_pcRingMeanSems[i][c].mean);
            }
            m_pdPixelHeights[i]=height;
            m_pdPixelHeights0[i]=m_pcRingMeanSems[i][0].mean;

        }
        m_bRankedPixelHeights0=false;
    }
    public void calPixelHeights_Contour(){
        double heightc=0;
        int hi=Integer.MAX_VALUE,lo;//height_Contour;
        int i,nArea;
        ArrayList <Point> contour;

        MeanSem0 ms;
        ImageShape shape;
        int centerPixel;
        Point pt;

        int[][] pixels;
        Color c;

        for(i=0;i<m_nNumLocalMaxima;i++){
            pt=m_cvLocalMaxima.get(i);
            centerPixel=m_pnPixelsProcessed[pt.y][pt.x];
            hi=Integer.MAX_VALUE;
            ms=m_pcRingMeanSems_Processed[i][m_nNumRings-1];
            lo=(int)m_pcRingMeanSems_Processed[i][m_nNumRings-1].mean+2*(int)ms.getSD();
            pixels=m_pnPixelsProcessed;
            if(centerPixel<lo){
                hi=lo;
                lo=Integer.MIN_VALUE;
                pixels=m_pnPixelsProcessed1;
            }
            contour=ContourFollower.getContour_Out(pixels, w, h, m_cvLocalMaxima.get(i), lo, hi);

            if(contour.size()>0) {
                c=CommonMethods.randomColor();
                ContourFollower.markContour(implC, contour, c);
            }

            shape=ImageShapeHandler.buildImageShape(contour);
            ms=ImageShapeHandler.getPixelMeanSem(m_pnPixels, shape);
            nArea=ms.n;
            heightc=nArea*(ms.mean-lo);

            m_pdPixelHeights_Contour[i]=heightc;
        }
        m_bRankedPixelHeights0=false;
    }

    public void calPixelHeights0(){
        double dSur,dRef;
        Point pt;
        for(int i=0;i<m_nNumLocalMaxima;i++){
            pt=m_cvLocalMaxima.get(i);
            m_cRefRing.setCenter(pt);
            dSur=ImageShapeHandler.getMean(m_pnPixelsCompensated, m_cRefRing,m_pnStamp,m_nExcludingType);
            dRef=m_pcRingMeanSems[i][m_nNumRings-1].mean;
            m_pdPixelHeights0[i]=m_pcRingMeanSems[i][0].mean-dRef+m_pnPixelsCompensated[pt.y][pt.x]-dSur;
        }
        m_bRankedPixelHeights0=false;
    }
    public void exportPixelHeights(String path){
        Formatter fm=FileAssist.getFormatter(path);
        PrintAssist.printString(fm, "Rank", 8);
        PrintAssist.printString(fm, "Index", 8);
        PrintAssist.printString(fm, "X", 8);
        PrintAssist.printString(fm, "Y", 8);
        PrintAssist.printString(fm, "Height", 12);
        int i,index,c,r0=0;
        double r;
        for(i=0;i<m_nNumRings;i++){
            r=m_dvRs.get(i);
            if(r>m_nRRefI) break;
            PrintAssist.printString(fm, "r="+PrintAssist.ToString(r,3),10);
            r0++;
        }
        PrintAssist.printString(fm, "sur", 10);
        PrintAssist.endLine(fm);
        Point p;
        double height;
        calRanking();
        for(i=0;i<m_nNumLocalMaxima;i++){
            index=m_pnIndexesPt[i];
            p=m_cvLocalMaxima.get(index);
            height=m_pdPixelHeights[index];
            PrintAssist.printNumber(fm, i, 8, 0);
            PrintAssist.printNumber(fm, index, 8, 0);
            PrintAssist.printNumber(fm, p.x, 8, 0);
            PrintAssist.printNumber(fm, p.y, 8, 0);
            PrintAssist.printNumber(fm, height, 12, 1);
            for(c=0;c<r0;c++){
                PrintAssist.printNumber(fm, m_pcRingMeanSems[index][c].mean, 10, 2);
            }
            PrintAssist.printNumber(fm, m_pcRingMeanSems[index][m_nNumRings-1].mean, 10, 2);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
    int getPercentileIndex(int id){
        double delta=(double)m_nNumLocalMaxima/(double)m_nPercentileDivision;
        int index=(int)(id/delta);
        return index;
    }
    public double[] getPixelHeights0(){
        return m_pdPixelHeights0;
    }
    public double[] getPixelHeights(){
        return m_pdPixelHeights;
    }
    public double[] getPixelHeightsC(){
        return m_pdPixelHeights_Contour;
    }
    public ArrayList<Point> getLocalMaxima(){
        return m_cvLocalMaxima;
    }
    public int[][] getStamp(){
        return m_pnStamp;
    }
    public int[][] getAdjustedPixels(){
        return m_pnAdjustedPixels;
    }
    public void calRefinedPixelHeights(){
        calPixelStatistics();
        calCompensatedPixels();
        refinePixelStatitics();
        calCompensatedPixels();
        refinePixelStatitics();
    }
    public int getNumLocalMaxima(){
        return m_nNumLocalMaxima;
    }
    public int[][] getCompensatedPixels(){
        return m_pnPixelsCompensated;
    }
    public void getPixelHeights(ArrayList<Double> pixelHeights){
        int i,index;
        for(i=0;i<m_nNumLocalMaxima;i++){
            index=m_pnIndexesPt[i];
            pixelHeights.add(m_pdPixelHeights[index]);
        }
    }
    public void getPixelHeights0(ArrayList<Double> pixelHeights0){
        int i,index;
        for(i=0;i<m_nNumLocalMaxima;i++){
            index=m_pnIndexesPt[i];
            pixelHeights0.add(m_pdPixelHeights0[index]);
        }
    }
    public void getPixelHeightsC(ArrayList<Double> pixelHeightsC){
        int i,index;
        for(i=0;i<m_nNumLocalMaxima;i++){
            index=m_pnIndexesPt[i];
            pixelHeightsC.add(m_pdPixelHeights_Contour[index]);
        }
    }
    public void getLocalMaxima(ArrayList<Point> localMaxima){
        int i,index;
        for(i=0;i<m_nNumLocalMaxima;i++){
            index=m_pnIndexesPt[i];
            localMaxima.add(m_cvLocalMaxima.get(index));
        }
    }
    public void storeProcessedPixels(){
        CommonStatisticsMethods.copyArray(m_pnPixels,m_pnPixelsProcessed);
        CommonStatisticsMethods.copyArray(m_pnPixels,m_pnPixelsProcessed1);

        ArrayList<Integer> types=new ArrayList();
        types.add(LandscapeAnalyzer.watershed);
        types.add(LandscapeAnalyzer.localMinimum);
        int nn=Integer.MIN_VALUE,nx=Integer.MAX_VALUE;
        CommonMethods.setPixelAtSpecialLandsapePoints(m_pnPixelsProcessed, types, nn);
        CommonMethods.setPixelAtSpecialLandsapePoints(m_pnPixelsProcessed1, types, nx);
        int i,j;
        for(i=0;i<m_nNumLocalMaxima;i++){
            for(j=0;j<m_nNumRings;j++){
                m_pcRingMeanSems_Processed[i][j].update(m_pcRingMeanSems[i][j]);
            }
        }
        CommonMethods.setPixels(implC, m_pnPixels);
        implC.show();
    }
}
