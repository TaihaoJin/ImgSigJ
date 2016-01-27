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

/**
 *
 * @author Taihao
 */
public class PixelAutoCorrelation {
    ImagePlus impl;
    int m_nRMax,m_nRRefI, m_nRRefO,w,h,m_nRefType;//m_nRMax the radius of the largest ring, m_nRRef is the readius of the the reference circle.
    int m_pnPixels[][], m_pnPixelsLR[][], m_pnPixelsTemp[][], m_pnPixelsCompensated[][];//the pixel values using local reference: the mean (or median) of the reference circle.
    ArrayList <ImageShape> m_cvRings;
    ArrayList <Histogram> m_cvHists;
    Histogram m_cAreaHist;
    double m_dDeltaPercentile;
    double[][]m_pdRingMean, m_pdRingMedian;
    double[] m_pdAreaMean, m_pdAreaSem, m_pdAreaMedian;//mead, median, and sem of pixels in the circle cocentered with m_cvRings to provide local reference of pixel values (let's call it
    //the reference circle.
    ImageScanner m_cIMSC;// The image scanner
    MeanSem0[] m_pcMeanSems_Mean;//MeanSems for the rings
    MeanSem0[] m_pcMeanSems_Median;
    MeanSem0[][] m_pcRingMeanSems_Mean;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the ringMean around i-th percentile points.
    MeanSem0[][] m_pcRingMeanSems_Median;
    MeanSem0 m_cPixelMeanSems;//MeanSem of the pixels of the image
    MeanSem0 m_cAreaMeanSems_Mean;//MeanSems of the reference circle
    MeanSem0 m_cAreaMeanSems_Median;
    ArrayList<Integer> pixelsOfPresetPoints;
    int[] m_pnPixels_trail;//when correlation is computed along a group of preset points, this array holds the pixel values of those points.
    MeanSem0 m_cPixelMeanSem_trail;//when correlation is computed along a group of preset points, this array holds the pixel values of those points.
    MeanSem0 m_cPixelMeanSems_trail[];//when correlation is computed along a group of preset points, this array holds the pixel values of those points.
//    ImageShape m_cRefCircle, m_cRefRing;
    ImageShape m_cRefRing;
    double m_pdPixelHeights[];//Acummulated pixel values using m_cRefRing as reference
    double m_pdPixelHeights0[];//the difference between the pixel value of the center and the mean of m_cRefRing
    boolean m_bPresetPoints;
    double[] m_pdAutoCorr_mean,m_pdAutoCorr_median;
    int[] m_pnAveRadius;
    int[][]m_pnStamp;
    ArrayList<Double> m_dvRs;
    int m_nPercentileDivision;//
    int m_nExcludingType;
    public PixelAutoCorrelation(ImagePlus impl, int rRefI, int rRefO, int rMax, int nRefType){//nRefType 0 for median, 1 for mean and -1 for not using local reference
        h=impl.getHeight();
        w=impl.getWidth();
        m_nRefType=nRefType;
        m_nRMax=rMax;
        m_nRRefI=rRefI;
        m_nRRefO=rRefO;
        this.impl=impl;
        m_cIMSC=new ImageScanner(new intRange(0,w-1), new intRange(0,h-1),new intRange(0,w-1), new intRange(0,h-1));
        m_bPresetPoints=false;
        m_dDeltaPercentile=0.01;
        init();
//        calPixelStatistics();
    }
    public void presetPoints(ArrayList<Point> points, ArrayList<Integer> pixels){
        m_cIMSC.presetPoints(points);
        pixelsOfPresetPoints=pixels;
        m_bPresetPoints=true;
    }
    void init(){
        m_nExcludingType=-1;
        m_pnPixelsCompensated=new int[h][w];
        m_pnPixels=new int[h][w];
        m_pnPixelsTemp=new int[h][w];

        constructRings();
        m_cvHists=new ArrayList();

        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), m_pnPixels);
        Point p=new Point(0,0);

        int pRange[]=new int[2];
        CommonMethods.getPixelValueRange_Stack(impl, pRange);
        int nNumRings=m_cvRings.size();

        m_pcMeanSems_Mean=new MeanSem0[nNumRings];
        m_pcMeanSems_Median=new MeanSem0[nNumRings];
        m_cPixelMeanSems=new MeanSem0();
        m_cAreaMeanSems_Mean=new MeanSem0();
        m_cAreaMeanSems_Median=new MeanSem0();

        Histogram hist;
        for(int r=0;r<nNumRings;r++){
            hist=new Histogram();
            hist.update(0, pRange[0], pRange[1], 1);
            m_cvHists.add(hist);
            m_pcMeanSems_Mean[r]=new MeanSem0();
            m_pcMeanSems_Median[r]=new MeanSem0();
        }

        m_pdAutoCorr_mean=new double[nNumRings];
        m_pdAutoCorr_median=new double[nNumRings];
        
        m_nPercentileDivision=(int)(1./m_dDeltaPercentile);
        m_pcRingMeanSems_Mean=new MeanSem0[m_nPercentileDivision][nNumRings];
        m_pcRingMeanSems_Median=new MeanSem0[m_nPercentileDivision][nNumRings];
        m_cPixelMeanSems_trail=new MeanSem0[m_nPercentileDivision];
        m_pnAveRadius=new int[m_nPercentileDivision];
        m_pnStamp=new int[h][w];
    }

    void constructRings(){
        m_dvRs=new ArrayList();
        m_cvRings=new ArrayList();
        m_cvRings.add(new CircleImage(0));
        m_dvRs.add(0.);
        double r=1.,dr=0.001;
        double ri=r-dr,ro=r+dr;
        m_cvRings.add(new Ring(ri,ro));
        m_dvRs.add(r);

        r=Math.sqrt(2);
        ri=r-dr;
        ro=r+dr;
        m_cvRings.add(new Ring(ri,ro));
        m_dvRs.add(r);

        int nr;
        for(nr=2;nr<=m_nRMax;nr++){
            m_cvRings.add(new Ring(nr-1,nr));
            m_dvRs.add((double)nr);
        }

        m_cRefRing=new Ring(m_nRRefI,m_nRRefO);
        m_cvRings.add(m_cRefRing);
        m_dvRs.add((double)m_nRMax+1);
        ImageShapeHandler.setFrameRanges(m_cvRings,new intRange(0,w-1), new intRange(0,h-1));
    }

    void calRefPixels(){
        int[] nRange=new int[2];
        CommonMethods.getDataRange(m_pnPixels, nRange);
        MeanSem0 ms=CommonStatisticsMethods.buildMeanSem(m_pnPixels);
        int nMin;
        switch(m_nRefType){
            case export_AutoCorrelation.originalRef:
                m_pnPixelsLR=new int[h][w];
                CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsLR);
                break;
            case export_AutoCorrelation.globalMeanRef:
                m_pnPixelsLR=new int[h][w];
                CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsLR);
                CommonStatisticsMethods.shiftArray(m_pnPixelsLR, -(int)(ms.mean+0.5));
                break;
            case export_AutoCorrelation.globalMedianRef:
                m_pnPixelsLR=new int[h][w];
                CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsLR);
                CommonStatisticsMethods.shiftArray(m_pnPixelsLR, -CommonStatisticsMethods.getQuantile(m_pnPixels, 0.5));
                break;
            case export_AutoCorrelation.localMeanRef:
                m_pnPixelsLR=CommonMethods.getPixelValus_LocalReference(impl, m_cRefRing, m_nRefType);
                break;
            case export_AutoCorrelation.localMedianRef:
                m_pnPixelsLR=CommonMethods.getPixelValus_LocalReference(impl, m_cRefRing, m_nRefType);
                break;
            default:
                m_pnPixelsLR=new int[h][w];
                CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsLR);
                break;
        }
        updateHistograms();
    }

    void updateHistograms(){
        Histogram hist;
        int len=m_cvHists.size();
        for(int r=0;r<len;r++){
            hist=CommonMethods.getDefaultHistogram(m_pnPixelsLR);
            m_cvHists.set(r, hist);
        }
    }

    int getRefPixel(Point pt){
        return m_pnPixels[pt.y][pt.x]-m_pnPixelsLR[pt.y][pt.x];
    }
    void calPixelStatistics(){
        calRefPixels();
        int len=m_cIMSC.m_nTotalSteps+1;
        int numRings=m_cvRings.size();

        m_pdRingMean=new double[len][numRings];
        m_pdRingMedian=new double[len][numRings];

        int i,c;
        Point p=m_cIMSC.getPosition();
        int index=0;
//        ImageShape shape;
        Histogram hist;
        double mean, median;
        int y,y0=0;
        int nRef;
        while (true){
            p=m_cIMSC.getPosition();
            y=p.y;
            if(y>y0) IJ.showStatus("calPixelStatistics "+PrintAssist.ToString(y)+"-th line of "+PrintAssist.ToString(h)+" lines!");
//            m_cRefRing.setCenter(p);
            nRef=getRefPixel(p);
            CommonMethods.fillHistograms(m_pnPixels, nRef ,m_cvRings, p, m_cvHists);
            for(i=0;i<=m_nRMax;i++){
                hist=m_cvHists.get(i);
                mean=hist.getMeanSem().mean;
                median=hist.getPercentileValue();
                m_pdRingMean[index][i]=mean;
                m_pdRingMedian[index][i]=median;
            }
            if(m_cIMSC.done()) break;
            m_cIMSC.move();
            index++;
        }
        m_cPixelMeanSems=CommonStatisticsMethods.buildMeanSem(m_pnPixelsLR);
//        m_cPixelMeanSem_trail=CommonStatisticsMethods.buildMeanSem(m_pnPixels_trail);
        for(c=0;c<=m_nRMax;c++){
            m_pcMeanSems_Median[c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMedian, c);
            m_pcMeanSems_Mean[c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMean, c);
        }
        if(m_bPresetPoints){
            len=pixelsOfPresetPoints.size();
            int num=len/m_nPercentileDivision;
            int rI,rF;
            for(i=0;i<m_nPercentileDivision;i++){
                rI=Math.max(0, (int)((i-.5)*num));
                rF=Math.min(len-1,(int)((i+.5)*num));
//                m_cPixelMeanSems_trail[i]=CommonStatisticsMethods.buildMeanSem(m_pnPixels_trail,rI,rF,1);
                for(c=0;c<numRings;c++){
                    m_pcRingMeanSems_Median[i][c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMedian,rI,rF,1,c);
                    m_pcRingMeanSems_Mean[i][c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMean,rI,rF,1,c);
                }
            }
        }
    }
    public void calAutoCorrelation(int nRefType){//computing the cross correlation coefficient between pixel values of points separated by the radius of rings
        m_nRefType=nRefType;
        int numRings=m_cvRings.size();
        calRefPixels();
        m_cPixelMeanSems=CommonStatisticsMethods.buildMeanSem(m_pnPixelsLR);
        int len;

        double mean=m_cPixelMeanSems.mean, sem2=m_cPixelMeanSems.sem2;
        double medianMean[]=new double[numRings-1];
        double medianSem2[]=new double[numRings-1];

        double crossProduct[]=new double[numRings-1];
        double numCrossPairs[]=new double[numRings-1];
        double crossProduct_median[]=new double[numRings-1];
        double numCrossPairs_median[]=new double[numRings-1];

        ArrayList<Double> medians[] =new ArrayList[numRings-1];
        int i,j,pixel0;

        for(i=0;i<numRings-1;i++){
            crossProduct[i]=0;
            numCrossPairs[i]=0;
            crossProduct_median[i]=0;
            numCrossPairs_median[i]=0;
            medianMean[i]=0;
            medianSem2[i]=0;
            medians[i]=new ArrayList();
        }

        int[][]pixels_LMC=new int[h][w];



        ImageShape shape;
        ArrayList<Point> points=new ArrayList();
        ArrayList<Point> scanningTrace=new ArrayList();

        double dp,median;
        Point p0,p;
        int index=0,y0=0,y;
        Histogram hist;
        int nRef,mean1;
        while(true){
            p0=new Point(m_cIMSC.getPosition());
            y=p0.y;
            scanningTrace.add(p0);
            pixel0=m_pnPixelsLR[p0.y][p0.x];
            nRef=getRefPixel(p0);
            dp=pixel0-mean;
            CommonMethods.fillHistograms(m_pnPixels, nRef, m_cvRings, p0, m_cvHists);
            for(i=0;i<numRings-1;i++){
                shape=m_cvRings.get(i);
                shape.setCenter(p0);
                shape.setFrameRanges(new intRange(p0.x, w-1), new intRange(p0.y, h-1));//this is to avoid double counting the pair of points with given distance
                shape.getInnerPoints(points);
                shape.setFrameRanges(new intRange(0, w-1), new intRange(0, h-1));
                len=points.size();
                for(j=0;j<len;j++){
                    p=points.get(j);
                    crossProduct[i]+=dp*(m_pnPixels[p.y][p.x]-nRef-mean);
                }
                numCrossPairs[i]+=len;

                numCrossPairs_median[i]+=1;

                hist=m_cvHists.get(i);
                median=hist.getPercentileValue();
                medianMean[i]+=median;
                medianSem2[i]+=median*median;
                medians[i].add(median);
                if(y>y0){
                    IJ.showStatus("computing autocorrelation: "+PrintAssist.ToString(y)+"-th line");
                    IJ.showProgress(y/h);
                    y0=y;
                }

            }
            if(m_cIMSC.done())break;
            m_cIMSC.move();
            index++;
        }
        double dAutoCorr_mean,meanMedian,cp;
        MeanSem0 ms=new MeanSem0();
        int num;
        for(i=0;i<numRings-1;i++){
            m_pdAutoCorr_mean[i]=crossProduct[i]/(Math.sqrt(sem2*sem2)*(numCrossPairs[i]));
            num=medians[i].size();
            medianMean[i]/=num;
            medianSem2[i]/=num;
            ms.updateMeanSquareSum(num, medianMean[i], medianSem2[i]);
            meanMedian=ms.mean;
            cp=0;
            for(j=0;j<num;j++){
                p=scanningTrace.get(j);
                cp+=(m_pnPixelsLR[p.y][p.x]-mean)*(medians[i].get(j)-meanMedian);
            }
            m_pdAutoCorr_median[i]=cp/(Math.sqrt(sem2*ms.sem2)*(num));
        }
    }
    public void calAutoCorrelation_RingMean(int nRefType){//crosscorrelation coefficients between pixel valuse and the means and medians of the pixels located on the cocentric rings with radius up to m_nRMax
        m_nRefType=nRefType;
        int numRings=m_cvRings.size();
        calRefPixels();
        int r,c;
        int len=m_cIMSC.m_nTotalSteps+1;
        double[] pixels0=new double[len];
        double[] meanr=new double[len];
        double[] medianr=new double[len];

        int index=0;

        Point p;
        index=0;
        while(true){
            p=m_cIMSC.getPosition();
            pixels0[index]=m_pnPixelsLR[p.y][p.x];
            index++;
            if(m_cIMSC.done())break;
            m_cIMSC.move();
        }

        ImageShape shape;
        Histogram hist=CommonMethods.getDefaultHistogram(m_pnPixelsLR);

        int y,y0=0;
        int nRef;
        for(c=0;c<numRings-1;c++){
            index=0;
            m_cIMSC.reset();
            y0=m_cIMSC.getPosition().y;
            shape=m_cvRings.get(c);
            while(true){
                p=m_cIMSC.getPosition();
                nRef=getRefPixel(p);
                y=p.y;
                if(y>y0){
                    IJ.showStatus("calAutoCorrelation_RingMean: r="+ PrintAssist.ToString(c+1)+"   y="+PrintAssist.ToString(y));
                    y0=y;
                }
                CommonMethods.fillHistogram(m_pnPixels,nRef, shape, p, hist);
                meanr[index]=hist.getMean();
                medianr[index]=hist.getPercentileValue();
                index++;
                if(m_cIMSC.done())break;
                m_cIMSC.move();
            }
            m_pdAutoCorr_mean[c]=CommonStatisticsMethods.crossCorrelationCoefficient(pixels0, meanr, 0);
            m_pdAutoCorr_median[c]=CommonStatisticsMethods.crossCorrelationCoefficient(pixels0, medianr, 0);
        }
    }
    public void exportRingMeanSem(String path){
        int r,c;
        int len=w*h;
        double[] pixels0=new double[len];
        double[] pixelsr=new double[len];
        int numRings=m_cvRings.size();

        MeanSem0 pcMeanSems_MeanNR[]=new MeanSem0[numRings];
        MeanSem0 pcMeanSems_MedianNR[]=new MeanSem0[numRings];


        len=m_cIMSC.getTotalSteps()+1;

        Point p;
        int pixel;
        int index=0;
        while(true){
            p=m_cIMSC.getPosition();
            if(m_bPresetPoints){
                pixel=pixelsOfPresetPoints.get(index);
            }
            pixel=m_pnPixelsLR[p.y][p.x];
            for(c=0;c<numRings;c++){
                if(pixel==0){
                    m_pdRingMedian[index][c]=pixel;
                    m_pdRingMean[index][c]=pixel;
                }else{
                    m_pdRingMedian[index][c]/=pixel;
                    m_pdRingMean[index][c]/=pixel;
                }
            }
            if(m_cIMSC.done())break;
            m_cIMSC.move();
            index++;
        }

        for(c=0;c<numRings;c++){
            pcMeanSems_MedianNR[c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMedian, c);
            pcMeanSems_MeanNR[c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMean, c);
        }

        Formatter fm=QuickFormatter.getFormatter(path);
        PrintAssist.printString(fm, "dist", 6);
        PrintAssist.printString(fm, "mean", 12);
        PrintAssist.printString(fm, "sem", 12);
        PrintAssist.printString(fm, "median", 12);
        PrintAssist.printString(fm, "sem", 12);
        PrintAssist.printString(fm, "meanNR", 12);
        PrintAssist.printString(fm, "semNR", 12);
        PrintAssist.printString(fm, "medianNR", 12);
        PrintAssist.printString(fm, "semNR", 12);
        PrintAssist.printString(fm, "num", 8);
        PrintAssist.endLine(fm);
/*            PrintAssist.printNumber(fm, 0, 6, 0);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.mean, 12, 3);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.sem, 12, 4);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.mean, 12, 3);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.sem, 12, 4);
            PrintAssist.printNumber(fm, 1, 12, 4);
            PrintAssist.printNumber(fm, 0, 12, 4);
            PrintAssist.printNumber(fm, 1, 12, 4);
            PrintAssist.printNumber(fm, 0, 12, 4);
            PrintAssist.printNumber(fm, len, 8, 0);
        PrintAssist.endLine(fm);*/
        for(c=0;c<numRings;c++){
            PrintAssist.printNumber(fm, c+1, 6, 0);
            PrintAssist.printNumber(fm, m_pcMeanSems_Mean[c].mean, 12, 3);
            PrintAssist.printNumber(fm, m_pcMeanSems_Mean[c].sem, 12, 4);
            PrintAssist.printNumber(fm, m_pcMeanSems_Median[c].mean, 12, 3);
            PrintAssist.printNumber(fm, m_pcMeanSems_Median[c].sem, 12, 4);
            PrintAssist.printNumber(fm, pcMeanSems_MeanNR[c].mean, 12, 4);
            PrintAssist.printNumber(fm, pcMeanSems_MeanNR[c].sem, 12, 4);
            PrintAssist.printNumber(fm, pcMeanSems_MedianNR[c].mean, 12, 4);
            PrintAssist.printNumber(fm, pcMeanSems_MedianNR[c].sem, 12, 4);
            PrintAssist.printNumber(fm, len, 8, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();
        path=FileAssist.getExtendedFileName(path, " -all percentile");
        
        fm=QuickFormatter.getFormatter(path);
        int i;

        for(i=0;i<m_nPercentileDivision;i++){
            PrintAssist.printString(fm, "dist"+PrintAssist.ToString(i), 8);
            PrintAssist.printString(fm, "mean"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "SD"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "median"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "SD"+PrintAssist.ToString(i), 12);
            /*
            PrintAssist.printString(fm, "meanNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "semNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "medianNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "semNR"+PrintAssist.ToString(i), 12);
             * 
             */
            PrintAssist.printString(fm, "num"+PrintAssist.ToString(i), 10);
        }
            PrintAssist.printString(fm, "dist", 8);
            PrintAssist.printString(fm, "meanAllPts", 12);
            PrintAssist.printString(fm, "SD_AllPts", 12);
            PrintAssist.printString(fm, "medianAPts", 12);
            PrintAssist.printString(fm, "SD_AllPts", 12);
            /*
            PrintAssist.printString(fm, "meanNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "semNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "medianNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "semNR"+PrintAssist.ToString(i), 12);
             *
             */
            PrintAssist.printString(fm, "numAllPts", 10);

        PrintAssist.endLine(fm);
        /*
        for(i=0;i<m_nPercentileDivision;i++){
            PrintAssist.printNumber(fm, 0, 8, 0);
            PrintAssist.printNumber(fm, m_cPixelMeanSems_trail[i].mean, 12, 3);
            PrintAssist.printNumber(fm, m_cPixelMeanSems_trail[i].getSD(), 12, 4);
            PrintAssist.printNumber(fm, m_cPixelMeanSems_trail[i].mean, 12, 3);
            PrintAssist.printNumber(fm, m_cPixelMeanSems_trail[i].getSD(), 12, 4);
            /*
            PrintAssist.printNumber(fm, 1, 12, 4);
            PrintAssist.printNumber(fm, 0, 12, 4);
            PrintAssist.printNumber(fm, 1, 12, 4);
            PrintAssist.printNumber(fm, 0, 12, 4);
             * 
             */
/*            PrintAssist.printNumber(fm, m_cPixelMeanSems_trail[i].n, 10, 0);
        }
            PrintAssist.printNumber(fm, 0, 8, 0);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.mean, 12, 3);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.getSD(), 12, 4);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.mean, 12, 3);
            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.getSD(), 12, 4);
            /*
            PrintAssist.printNumber(fm, 1, 12, 4);
            PrintAssist.printNumber(fm, 0, 12, 4);
            PrintAssist.printNumber(fm, 1, 12, 4);
            PrintAssist.printNumber(fm, 0, 12, 4);
             *
             */
//            PrintAssist.printNumber(fm, m_cPixelMeanSem_trail.n, 10, 0);
//        PrintAssist.endLine(fm);
        for(c=0;c<numRings;c++){
            for(i=0;i<m_nPercentileDivision;i++){
                PrintAssist.printNumber(fm, m_dvRs.get(c), 8, 2);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Mean[i][c].mean, 12, 3);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Mean[i][c].getSD(), 12, 4);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Median[i][c].mean, 12, 3);
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Median[i][c].getSD(), 12, 4);
                /*
                PrintAssist.printNumber(fm, pcMeanSems_MeanNR[c].mean, 12, 4);
                PrintAssist.printNumber(fm, pcMeanSems_MeanNR[c].sem, 12, 4);
                PrintAssist.printNumber(fm, pcMeanSems_MedianNR[c].mean, 12, 4);
                PrintAssist.printNumber(fm, pcMeanSems_MedianNR[c].sem, 12, 4);
                 */
                PrintAssist.printNumber(fm, m_pcRingMeanSems_Mean[i][c].n, 10, 0);
            }
                PrintAssist.printNumber(fm, m_dvRs.get(c), 8, 0);
                PrintAssist.printNumber(fm, m_pcMeanSems_Mean[c].mean, 12, 3);
                PrintAssist.printNumber(fm, m_pcMeanSems_Mean[c].getSD(), 12, 4);
                PrintAssist.printNumber(fm, m_pcMeanSems_Median[c].mean, 12, 3);
                PrintAssist.printNumber(fm, m_pcMeanSems_Median[c].getSD(), 12, 4);
                /*
                PrintAssist.printNumber(fm, pcMeanSems_MeanNR[c].mean, 12, 4);
                PrintAssist.printNumber(fm, pcMeanSems_MeanNR[c].sem, 12, 4);
                PrintAssist.printNumber(fm, pcMeanSems_MedianNR[c].mean, 12, 4);
                PrintAssist.printNumber(fm, pcMeanSems_MedianNR[c].sem, 12, 4);
                 */
                PrintAssist.printNumber(fm, m_pcMeanSems_Mean[c].n, 10, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();
        path=FileAssist.getExtendedFileName(path, "all percentile");
    }

    int calCompensatedPixels(){
        int numRings=m_cvRings.size();
        CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
        CommonMethods.displayPixels(m_pnPixelsCompensated, "before compensation", impl.getType());
        m_cIMSC.reset();
        Point pt;
        int index, step=0,pixel,i,r0;
        double dm,dsur;
        ImageShape shape;
        while(true){
            pt=m_cIMSC.getPosition();
            index=getPercentileIndex(step);
            dsur=m_pcRingMeanSems_Mean[index][m_nRMax].mean;
            r0=m_pnAveRadius[index];
//            m_pnPixelsCompensated[pt.y][pt.x]-=(int)(m_cPixelMeanSems_trail[index].mean-dsur);
            for(i=0;i<numRings;i++){
                pixel=(int)(m_pcRingMeanSems_Mean[index][i].mean-dsur);
                if(pixel<1) break;
                shape=m_cvRings.get(i);
                shape.setCenter(pt);
                ImageShapeHandler.addValue(m_pnPixelsCompensated, shape, -pixel);
            }
            if(m_cIMSC.done())break;
            m_cIMSC.move();
            step++;
        }
        CommonMethods.displayPixels(m_pnPixelsCompensated, "after compensation", impl.getType());
        return 1;
    }

    void stampIPOs(){
        calAveRadius();
        int i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                m_pnStamp[i][j]=0;
            }
        }
        int numRings=m_cvRings.size();
        m_cIMSC.reset();
        Point pt;
        int index, step=0,r0;
        ImageShape shape;
        while(true){
            pt=m_cIMSC.getPosition();
            index=getPercentileIndex(step);
            r0=m_pnAveRadius[index];
            for(i=0;i<=r0;i++){
                shape=m_cvRings.get(i);
                shape.setCenter(pt);
                ImageShapeHandler.setValue(m_pnStamp, shape, m_nExcludingType);
            }
            if(m_cIMSC.done())break;
            m_cIMSC.move();
            step++;
        }
    }

    void calAveRadius(){
        int len=m_pcRingMeanSems_Mean.length;
        double sd=CommonStatisticsMethods.buildMeanSem(m_pnPixels).getSD();
        int i,j,numRings=m_cvRings.size();
        for(i=0;i<len;i++){
            for(j=0;j<numRings;j++){
                if(m_pcRingMeanSems_Mean[i][j].mean<2*m_pcRingMeanSems_Mean[i][j].getSD()) break;
                m_pnAveRadius[i]=j;
            }
        }
    }

    int refinePixelStatitics(){
        stampIPOs();
        int[][] pixelsTemp=m_pnPixels;
        m_pnPixels=m_pnPixelsCompensated;

        m_cIMSC.reset();
        int len=m_cIMSC.m_nTotalSteps+1;
        int i,c,step=0;
        Point p=m_cIMSC.getPosition();
        int index=0;
        ImageShape shape;
        Histogram hist;
        double mean, median;
        int y,y0=0;
        int pixel,nRef;
        double dsur;
        int numRings=m_cvRings.size();
        while (true){
            p=m_cIMSC.getPosition();
            y=p.y;
            if(y>y0) IJ.showStatus("refinePixelStatistics "+PrintAssist.ToString(y)+"-th line of "+PrintAssist.ToString(h)+" lines!");
            index=getPercentileIndex(step);
            dsur=m_pcRingMeanSems_Mean[index][m_nRMax].mean;
            ImageShapeHandler.copyElements(m_pnPixelsCompensated, m_pnPixelsTemp, m_cvRings, p);
            for(i=0;i<numRings;i++){
                pixel=(int)(m_pcRingMeanSems_Mean[index][i].mean-dsur);
                if(pixel<1) break;
                shape=m_cvRings.get(i);
                shape.setCenter(p);
                ImageShapeHandler.addValue(m_pnPixelsTemp, shape, pixel);
            }
            nRef=(int)ImageShapeHandler.getMean(m_pnPixelsTemp, m_cRefRing,m_pnStamp,m_nExcludingType);
            CommonMethods.fillHistogram(m_pnPixelsTemp, nRef ,m_cvRings.get(numRings-1), p, m_cvHists.get(numRings-1),m_pnStamp,m_nExcludingType);
            CommonMethods.fillHistograms(m_pnPixelsTemp, nRef ,m_cvRings, p, m_cvHists);
            for(i=0;i<numRings;i++){
                hist=m_cvHists.get(i);
                mean=hist.getMeanSem().mean;
                median=hist.getPercentileValue();
                m_pdRingMean[step][i]=mean;
                m_pdRingMedian[step][i]=median;
            }
//            m_pnPixels_trail[step]=m_pnPixelsTemp[p.y][p.x]-nRef;
            if(m_cIMSC.done()) break;
            m_cIMSC.move();
            step++;
        }
        for(c=0;c<numRings;c++){
            m_pcMeanSems_Median[c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMedian, c);
            m_pcMeanSems_Mean[c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMean, c);
        }
        if(m_bPresetPoints){
            len=pixelsOfPresetPoints.size();
            int num=len/m_nPercentileDivision;
            int rI,rF;
            for(i=0;i<m_nPercentileDivision;i++){
                rI=Math.max(0, (int)((i-.5)*num));
                rF=Math.min(len-1,(int)((i+.5)*num));
                for(c=0;c<numRings;c++){
                    m_pcRingMeanSems_Median[i][c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMedian,rI,rF,1,c);
                    m_pcRingMeanSems_Mean[i][c]=CommonStatisticsMethods.buildMeanSem(m_pdRingMean,rI,rF,1,c);
                }
            }
        }
        m_pnPixels=pixelsTemp;
        return 1;
    }
    void calPixelHeights(){
        int len=m_cIMSC.getTotalSteps()+1;
        m_pdPixelHeights=new double[len];
        m_pdPixelHeights0=new double[len];
        int nSize=m_dvRs.size();
        int i,j,r0=0;
        double r;
        for(i=0;i<nSize;i++){
            r=m_dvRs.get(i);
            if(r>=m_nRRefI) break;
            r0++;
        }
        double height=0;
        int nArea;
        double dSur;
        for(i=0;i<len;i++){
            height=0;
            dSur=m_pdRingMean[i][nSize-1];
            for(j=0;j<=r0;j++){
                nArea=m_cvRings.get(j).getArea();
                height+=nArea*(m_pdRingMean[i][j]-dSur);
            }
            m_pdPixelHeights[i]=height;
            m_pdPixelHeights0[i]=m_pdRingMean[i][0]-dSur;
        }
    }
    public void exportPixelHeights(String path){
        Formatter fm=FileAssist.getFormatter(path);
        PrintAssist.printString(fm, "i", 8);
        PrintAssist.printString(fm, "X", 8);
        PrintAssist.printString(fm, "Y", 8);
        PrintAssist.printString(fm, "Height", 12);
        int nSize=m_dvRs.size();
        int i,r0=0;
        double r;
        for(i=0;i<nSize;i++){
            r=m_dvRs.get(i);
            if(r>m_nRRefI) break;
            PrintAssist.printString(fm, "r="+PrintAssist.ToString(r,3),10);
            r0++;
        }
        PrintAssist.printString(fm, "sur", 10);
        PrintAssist.endLine(fm);
        int len=m_cIMSC.getTotalSteps()+1;
        m_cIMSC.reset();
        Point p;
        int step=0;
        double height;
        int numRings=m_cvRings.size();
        while(true){
            p=m_cIMSC.getPosition();
            height=m_pdPixelHeights[step];
            PrintAssist.printNumber(fm, step, 8, 0);
            PrintAssist.printNumber(fm, p.x, 8, 0);
            PrintAssist.printNumber(fm, p.y, 8, 0);
            PrintAssist.printNumber(fm, height, 12, 1);
            for(i=0;i<r0;i++){
                PrintAssist.printNumber(fm, m_pdRingMean[step][i], 10, 2);
            }
            PrintAssist.printNumber(fm, m_pdRingMean[step][numRings-1], 10, 2);
            PrintAssist.endLine(fm);
            if(m_cIMSC.done()) break;
            m_cIMSC.move();
            step++;
        }
        fm.close();
    }
    int getPercentileIndex(int id){
        int len=m_cIMSC.getTotalSteps()+1;
        double delta=(double)len/(double)m_nPercentileDivision;
        int index=(int)(id/delta);
        return index;
    }
    public void exportAutoCorrelation(String path){
        Formatter fm=QuickFormatter.getFormatter(path);
        PrintAssist.printString(fm, "dist", 10);
        PrintAssist.printString(fm, "CorrMean", 10);
        PrintAssist.printString(fm, "CorrMedian", 12);
        PrintAssist.endLine(fm);
        for(int i=0;i<m_nRMax;i++){
            PrintAssist.printNumber(fm, i+1, 10, 1);
            PrintAssist.printNumber(fm,  m_pdAutoCorr_mean[i], 10, 4);
            PrintAssist.printNumber(fm, m_pdAutoCorr_median[i], 12, 4);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
    public void resetImageScanner(){
        m_cIMSC.reset();
    }
    public double[] getPixelHeights(){
        return m_pdPixelHeights0;
    }
}
