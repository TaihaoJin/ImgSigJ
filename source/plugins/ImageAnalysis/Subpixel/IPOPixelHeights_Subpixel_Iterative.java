/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis.Subpixel;

/**
 *
 * @author Taihao
 */
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
import utilities.Geometry.ImageShapes.SubpixelAreaInRingsLUT;
import ImageAnalysis.IPOPixelHeightsHandler;
import ImageAnalysis.ContourFollower;
import ImageAnalysis.LandscapeAnalyzer;
import utilities.statistics.MeanSemFractional;
import utilities.Geometry.CocentricCircles_Rings;
import utilities.Geometry.ImageShapes.RectangleImage;
import utilities.statistics.MeanSem1;
import utilities.Categorizer.Categorizer;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.CustomDataTypes.IntPair;
import ImageAnalysis.CocentricRingKernel;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

/**
 *
 * @author Taihao
 */
public class IPOPixelHeights_Subpixel_Iterative{
//    ImagePlus impl;
    public static final int proximityLabel=-105;
    int m_nRMax,m_nRRefI, m_nRRefO,w,h;//m_nRMax the radius of the largest ring, m_nRRef is the readius of the the reference circle.
    int m_pnPixels[][], m_pnPixelsTemp[][], m_pnPixelsTemp1[][], m_pnPixelsTemp2[][], m_pnPixelsCompensated[][],m_pnPixelsProcessed[][],m_pnPixelsProcessed1[][],m_pnPixelsProcessedCompensated[][],m_pnPixelsProcessedCompensated1[][];//the pixel values using local reference: the mean (or median) of the reference circle.
//    CocentricCircles_Rings m_cCocentricRings; m_pnPixelsProcessed and m_pnPixelsProcessed1 (also compensated values of those) are used to store the processed image pixel values, and used for
//    computing the contour around each local maximum after modifying the pixel values of some special landscape points.
//    see the method, storeProcessedPixels().
    MeanSemFractional[][] m_pcRingMeanSems;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum i and ring r
    //MeanSemFractional is used for each ring to impletement subpixel resolution.
    MeanSem1[][] m_pcRingMeanSems_background;//MeanSems for the background rings of each local maximum. The radii of the inner and outer ring of the
    //two types of background rings are used. are m_nRRefI, m_nRRefO and m_nRefI, 4*m_nRefO, respectively.
    MeanSem1[][] m_pcRingMeanSems_background_Processed;//MeanSems for the background rings of the precessed image are stored in
    //this array for the computation of the contours of the original image.

    MeanSemFractional[][] m_pcRingMeanSems_Group;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
    MeanSemFractional[][] m_pcRingMeanSems_Normalized;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
    MeanSem1[][] m_pcRingMeanSems_Group_background;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
    MeanSem1[][] m_pcRingMeanSems_Normalized_background;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
    double [][] m_pnAdjustedPixels,m_pnAdjustedPixels_processed;//m_pnAdjustedPixels_processed stored to compute the contour of the satellites
    ImageShape m_cRefRing,backgroundRing;
    double m_pdPixelHeights[];//Acummulated pixel values using m_cRefRing as reference
    double m_pdPixelHeights_Contour[];//Acummulated pixel values using m_cRefRing as reference
    double m_pdPixelHeights0[];//the difference between the pixel value of the center and the mean of m_cRefRing
    double m_dDeltaPercentile;
    int[] m_pnAveRadius;//radius of a percentile group
    int[][]m_pnStamp;//stamp points of local maxima and the circle with the radius of the percentile group.
    int[][]m_pnRegionStamp,m_pnRegionStampo;//stamp points of local maxima and the circle with the radius of the percentile group.
    int[][]m_pnProximityMap;//all position within the m_pnAveRadius .
    ArrayList<Double> m_dvRsCocentric;//radius of m_cCocentricRings. the last element of m_cvRings is always the m_cRefRing, so for this one, m_dvRs is not a real radius
    int m_nPercentileDivision;//The number of the groups the local maxima will be grouped according to their pixel heights
    int m_nExcludingType;//the integer value being used to stamp local maxima and the circles.
    ArrayList<Point> m_cvLocalMaxima;
    ArrayList<Point> m_cvOriginalLocalMinima, m_cvOriginalWaterSheds;
    ArrayList<Point> m_cvLocalGDExtrema, m_cvSatelliteMaxima;// m_cvLocalGDExtrema is the local extrema of pixel gradient map, m_cvSatelliteMaxima is the local
    //maxima in the m_pnPixelsCompensated associated with m_cvLocalGDExtrema.
    int m_pnRankingIndexes[],m_pnIndexesPt[];
    int m_pnOverlappingIPOIndexProcessed[];//stored for later

    int m_nNumRings,m_nNumLocalMaxima,m_nNumSatellites,m_nTrueMaxima;
    int m_nMinDTB;//minimum distance (int index) to border for local maxima used for calculating m_pnCompensatedPixels;

    boolean m_bRankedPixelHeights0,m_bAllowRankingPixelHeights;
    int m_nR0; //m_nR0 is the index for the largest ring in the signal area
    int m_nNumOverlappingIOPs;//number of the satellites associated with the same local maxima in the compensated images
    //as original local maxima or other satellites.
    ImagePlus implC;
    double[] m_pdGroupingDelimiters;
    Categorizer m_cGrouper;
    SubpixelAreaInRingsLUT m_cSubpixelIS;
    ArrayList <double[]> IPOCenters;
    ArrayList <IntPair> m_cvOverlappedSatellites;
    int[] m_pnOverlappingIPOIndex;
    Point[] m_pcOriginalSatellites;
    int[] m_pnAdjusted;
    int[][] m_pnDistToBorder;
    int[] m_pnPracticalRadius, m_pnPracticalRadius_Group;
    boolean m_bPositionAdjustMode;//if true, than the iop center for the satellite maxima will be adjusted in each iteration of pixel statistics refinement.
    int[] m_pnLMToRegionIndexes,m_pnRegionToLMIndexes;//m_pnLMToRegionIndexes stores region indexes of local maxima,
    boolean m_bLMToRegionCoverstionUpdated;
    ImageShape[] m_pcProximityCircles;
    //m_pnRegionToLMIndexes stores local maximum indexes of regions.
    int[] m_pnNumAdjusted;
    LandscapeAnalyzerPixelSorting m_cStampper;


    public IPOPixelHeights_Subpixel_Iterative(int[][] pixels,LandscapeAnalyzerPixelSorting Stampper, ArrayList<Point> localMaxima, int numSatellites, SubpixelAreaInRingsLUT subpixelIS, int rRefI, int rRefO, int rMax){//nRefType 0 for median, 1 for mean and -1 for not using local reference
        m_cStampper=Stampper;
        h=pixels.length;
        w=pixels[0].length;
        m_nRMax=rMax;
        m_nRRefI=rRefI;
        m_nRRefO=rRefO;
        m_dDeltaPercentile=0.01;
        m_bRankedPixelHeights0=false;
        m_cSubpixelIS=subpixelIS;
        m_dvRsCocentric=subpixelIS.getCCRings().getRs();
        m_cSubpixelIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
//        m_cvLocalGDExtrema=localGDExtrema;
        init();
        updatePixels(pixels,localMaxima, IPOCenters,numSatellites,true);
        m_bAllowRankingPixelHeights=true;
    }
    void calIPOCenters(int[][] pixels, ArrayList<Point> localMaxima, ArrayList<double[]> IPOCenters, int indexI, int indexF){
        CommonMethods.subpixelWeightedCentroids(pixels, localMaxima, IPOCenters, indexI, indexF);
    }
    void init(){
        int i,r;
        m_cvOverlappedSatellites=new ArrayList();
        m_pcOriginalSatellites=new Point[1];
        m_nExcludingType=-1;
        m_pnPixelsCompensated=new int[h][w];
//        m_pnPixels=new int[h][w];
        m_pnPixelsTemp=new int[h][w];
        m_pnPixelsTemp1=new int[h][w];
        m_pnPixelsTemp2=new int[h][w];
        m_pnPixelsProcessed=new int[h][w];
        m_pnPixelsProcessed1=new int[h][w];
        m_pnPixelsProcessedCompensated=new int[h][w];
        m_pnPixelsProcessedCompensated1=new int[h][w];
        m_pnProximityMap=new int[h][w];
        m_nPercentileDivision=(int)(1./m_dDeltaPercentile);
        IPOCenters=new ArrayList();
        m_cvOriginalLocalMinima=new ArrayList();
        m_cvOriginalWaterSheds=new ArrayList();
        constructRings();
        m_nNumRings=m_cSubpixelIS.getNumRings();
        implC=CommonMethods.displayPixels(m_pnPixelsTemp, "contours", ImagePlus.COLOR_RGB);

        m_pcRingMeanSems_Group=new MeanSemFractional[m_nPercentileDivision][m_nNumRings];
        m_pcRingMeanSems_Group_background=new MeanSem1[m_nPercentileDivision][2];
        m_pcRingMeanSems_Normalized=new MeanSemFractional[m_nPercentileDivision][m_nNumRings];
        m_pcRingMeanSems_Normalized_background=new MeanSem1[m_nPercentileDivision][2];
        m_pdGroupingDelimiters=new double[m_nPercentileDivision-1];
        m_pnAveRadius=new int[m_nPercentileDivision];
        m_pnPracticalRadius_Group=new int[m_nPercentileDivision];
        for(i=0;i<m_nPercentileDivision;i++){
            for(r=0;r<m_nNumRings;r++){
                m_pcRingMeanSems_Group[i][r]=new MeanSemFractional();
                m_pcRingMeanSems_Normalized[i][r]=new MeanSemFractional();
            }
            m_pcRingMeanSems_Group_background[i][0]=new MeanSem1();
            m_pcRingMeanSems_Normalized_background[i][0]=new MeanSem1();
            m_pcRingMeanSems_Group_background[i][1]=new MeanSem1();
            m_pcRingMeanSems_Normalized_background[i][1]=new MeanSem1();
        }
        m_pnStamp=new int[h][w];
        m_pnRegionStamp=new int[h][w];
        m_pnRegionStampo=new int[h][w];
        int maxR=(int)m_cSubpixelIS.getMaxCCRingRadius()+1;
        m_pcProximityCircles=new ImageShape[maxR];

        double dMinDTB=5;
        ArrayList<Double> dvRs=m_cSubpixelIS.getCCRings().getRs();
        for(i=0;i<m_nNumRings;i++){
            m_nMinDTB=i;
            if(dvRs.get(i)>=dMinDTB) break;
        }
        for(i=0;i<maxR;i++){
            m_pcProximityCircles[i]=new CircleImage(i+1);
            m_pcProximityCircles[i].setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        }
        CommonStatisticsMethods.setElements(m_pnStamp,0);
        CommonStatisticsMethods.setElements(m_pnRegionStamp,0);
        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems_Group);
        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems_Normalized);
    }
    public MeanSemFractional[][] getPcRingMeanSems(){
        return m_pcRingMeanSems;
    }
    public MeanSemFractional[][] getPcRingMeanSems_Group(){
        return m_pcRingMeanSems_Group;
    }
    public MeanSemFractional[][] getPcRingMeanSems_Normalized(){
        return m_pcRingMeanSems_Normalized;
    }
    public MeanSem1[][] getPcRingMeanSems_background(){
        return m_pcRingMeanSems_background;
    }
    public MeanSem1[][] getPcRingMeanSems_Group_background(){
        return m_pcRingMeanSems_Group_background;
    }
    public MeanSem1[][] getPcRingMeanSems_Normalized_background(){
        return m_pcRingMeanSems_Normalized_background;
    }
    public int getnNumPercentileDivisions(){
        return m_nPercentileDivision;
    }
    public void updatePixels(int[][] pixels, ArrayList<Point> localMaxima, ArrayList<double[]> IPOCenters, int numSatellites, boolean bAdjustMode){
        //localMaxima contain local maxima and the selected local minima of gradient map, here called satellites
        m_bPositionAdjustMode=bAdjustMode;
        m_pnPixels=pixels;
        m_cvLocalMaxima=localMaxima;
        m_nNumLocalMaxima=localMaxima.size();
        m_nNumSatellites=numSatellites;
        m_nTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        int i,r;

        int len=0;
        if(m_pcRingMeanSems!=null) len=m_pcRingMeanSems.length;
        if(m_nNumLocalMaxima>len){
            m_pnPracticalRadius=new int[m_nNumLocalMaxima];
            m_pnRankingIndexes=new int[m_nNumLocalMaxima];
            m_pnIndexesPt=new int[m_nNumLocalMaxima];
            m_pcRingMeanSems=new MeanSemFractional[m_nNumLocalMaxima][m_nNumRings];
            m_pcRingMeanSems_background=new MeanSem1[m_nNumLocalMaxima][2];
            m_pcRingMeanSems_background_Processed=new MeanSem1[m_nNumLocalMaxima][2];
            m_pnAdjustedPixels=new double[m_nNumLocalMaxima][m_nNumRings];
            m_pnAdjustedPixels_processed=new double[m_nNumLocalMaxima][m_nNumRings];
            m_pdPixelHeights=new double[m_nNumLocalMaxima];
            m_pdPixelHeights0=new double[m_nNumLocalMaxima];
            m_pdPixelHeights_Contour=new double[m_nNumLocalMaxima];
            m_pnOverlappingIPOIndex=new int[m_nNumLocalMaxima];
            m_pnOverlappingIPOIndexProcessed=new int[m_nNumLocalMaxima];
            CommonStatisticsMethods.setElements(m_pnOverlappingIPOIndex, -1);
            m_pnDistToBorder=new int[m_nNumLocalMaxima][2];
            m_pnAdjusted=new int[m_nNumLocalMaxima];
            m_pnNumAdjusted=new int[20];
            m_pnLMToRegionIndexes=new int[m_nNumLocalMaxima];
            m_pnRegionToLMIndexes=new int[m_nNumLocalMaxima];

            for(i=0;i<m_nNumLocalMaxima;i++){
                for(r=0;r<m_nNumRings;r++){
                    m_pcRingMeanSems[i][r]=new MeanSemFractional();
                }
                for(r=0;r<2;r++){
                    m_pcRingMeanSems_background[i][r]=new MeanSem1();
                    m_pcRingMeanSems_background_Processed[i][r]=new MeanSem1();
                }
            }
        }
        for(i=0;i<m_nNumLocalMaxima;i++){
            m_pnAdjusted[i]=-1;
        }
        m_bLMToRegionCoverstionUpdated=false;

        for(i=0;i<20;i++){
            m_pnNumAdjusted[i]=0;
        }
        len=m_pcOriginalSatellites.length;
        if(m_nNumSatellites>len){
            m_pcOriginalSatellites=new Point[m_nNumSatellites];
        }

        int nTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        for(i=0;i<m_nNumSatellites;i++){
            m_pcOriginalSatellites[i]=new Point(m_cvLocalMaxima.get(nTrueMaxima+i));
        }

        if(m_bPositionAdjustMode){
            CommonMethods.getSpecialLandscapePoints(m_pnPixels, m_pnStamp, false, w, h, LandscapeAnalyzerPixelSorting.localMinimum, m_cvOriginalLocalMinima);
            CommonMethods.getSpecialLandscapePoints(m_pnPixels, m_pnStamp, true, w, h, LandscapeAnalyzerPixelSorting.watershed, m_cvOriginalWaterSheds);
            CommonStatisticsMethods.copyArray(m_pnStamp, m_pnRegionStamp);
            buildLMToRegionCoversion();
        }

        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems);
        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems_background);
        CommonStatisticsMethods.clearMeanSems(m_pcRingMeanSems_background_Processed);

        CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
        if(IPOCenters==null) IPOCenters=new ArrayList();
        if(IPOCenters.isEmpty()){
            for(i=0;i<m_nNumLocalMaxima;i++){
                IPOCenters.add(new double[2]);
            }
            calIPOCenters(pixels,m_cvLocalMaxima,IPOCenters,0,m_nNumLocalMaxima-m_nNumSatellites-1);
        }
        this.IPOCenters=IPOCenters;
    }
    void stampLandscape(){
        m_cStampper.updateAndStampPixels(m_pnPixels, m_pnRegionStamp);
    }
    public void noRanking(){
        m_bRankedPixelHeights0=true;
    }
    public int calRankingRingMeanSems(int indexI, int indexF){//checked on 3/3/2011
        if(m_bRankedPixelHeights0||!m_bAllowRankingPixelHeights) return -1;
        int len=indexF-indexI+1,i;
        double[] pdv=new double[len];
        int[] indexesPt=new int[len];
        for(i=indexI;i<=indexF;i++){
            indexesPt[i-indexI]=i;
            pdv[i-indexI]=m_pcRingMeanSems[i][0].mean;
        }

        utilities.QuickSort.quicksort(pdv, indexesPt);

        for(i=indexI;i<=indexF;i++){
            m_pnIndexesPt[i]=indexesPt[i-indexI];
        }

        //this is for debuggin, and need to remove after done
        double pdt[]=new double[len];
        for(i=indexI;i<=indexF;i++){
            pdt[i-indexI]=m_pcRingMeanSems[m_pnIndexesPt[i]][0].mean;
        }
        if(!Categorizer.verifySorting(pdt)){
            i=i;
        }

        int index;
        for(i=indexI;i<=indexF;i++){
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
        m_cRefRing=new Ring(m_nRRefI,m_nRRefO);
        backgroundRing=new Ring(m_nRRefI,4*m_nRRefO);
        m_cRefRing.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        backgroundRing.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
    }

    double getRefPixel(Point pt){
        m_cRefRing.setCenter(pt);
        return (double) ImageShapeHandler.getMean(m_pnPixelsCompensated, m_cRefRing);
    }

    public void calPixelStatistics_Initial(){
        int numTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        int i,c;
        double dRef;
        Point p;
        MeanSemFractional[] pcRingMeanSems=new MeanSemFractional[m_nNumRings];
        for(i=0;i<m_nNumRings;i++){
            pcRingMeanSems[i]=new MeanSemFractional();
        }
        MeanSemFractional fms=new MeanSemFractional();
        for(i=0;i<numTrueMaxima;i++){
            p=m_cvLocalMaxima.get(i);
            m_cSubpixelIS.setEnclosingRectangleCenter(p);
            m_cSubpixelIS.setCenter_Subpixel(IPOCenters.get(i)[0], IPOCenters.get(i)[1]);
            m_cRefRing.setCenter(p);
            CommonStatisticsMethods.clearMeanSems(pcRingMeanSems);
            dRef=getRefPixel(p);
            ImageShapeHandler.calMeanSemFraction_SubpixelCCRings(m_pnPixels,m_cSubpixelIS, m_cSubpixelIS.getEnclosingRectangle(), dRef, pcRingMeanSems);

            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems[i][c].update(pcRingMeanSems[c]);
            }
        }
    }
    
    public void calPixelStatistics(int[][] pixels, int indexI, int indexF, int iteration){
        int numTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        int i,c,iter;
        int y,y0=0;
        double dRef;
        Point p;
        MeanSemFractional[] pcRingMeanSems=new MeanSemFractional[m_nNumRings];
        for(i=0;i<m_nNumRings;i++){
            pcRingMeanSems[i]=new MeanSemFractional();
        }
        MeanSemFractional fms=new MeanSemFractional();
        for(i=indexI;i<=indexF;i++){
            iter=m_pnAdjusted[i];
            if(iter<iteration&&iter>=0) continue;
            p=m_cvLocalMaxima.get(i);
            if(p.x==49&&p.y==101){
                p=p;
            }
            if(p.x==54&&p.y==26){
                p=p;
            }
            if(p.x==60&&p.y==116){
                i=i;
            }
            if(p.x==58&&p.y==112){
                i=i;
            }
            m_cSubpixelIS.setEnclosingRectangleCenter(p);
            m_cSubpixelIS.setCenter_Subpixel(IPOCenters.get(i)[0], IPOCenters.get(i)[1]);
            m_cRefRing.setCenter(p);
            backgroundRing.setCenter(p);
            CommonStatisticsMethods.clearMeanSems(pcRingMeanSems);

            y=p.y;
//            if(y>y0) IJ.showStatus("calPixelStatistics "+PrintAssist.ToString(y)+"-th line of "+PrintAssist.ToString(h)+" lines!");
//            dRef=getRefPixel(p);
            ImageShapeHandler.calMeanSemFraction_SubpixelCCRings(pixels,m_pnRegionStamp,m_cSubpixelIS, m_cSubpixelIS.getEnclosingRectangle(),0, m_pnProximityMap,proximityLabel, pcRingMeanSems);
            m_pnDistToBorder[i][0]=m_cSubpixelIS.minDistToBorder;
            m_pnDistToBorder[i][1]=m_cSubpixelIS.maxDistToBorder;
            int dtb=m_pnDistToBorder[i][1];
            fms.clear();
            dRef=pcRingMeanSems[dtb].mean;
            for(c=0;c<m_nNumRings;c++){
                pcRingMeanSems[c].shiftMean(-dRef);
                m_pcRingMeanSems[i][c].update(pcRingMeanSems[c]);
                fms.mergeSems(pcRingMeanSems[c]);
            }

            m_pcRingMeanSems_background[i][0].update(ImageShapeHandler.getPixelMeanSem1(pixels, 0, m_cRefRing));
            m_pcRingMeanSems_background[i][1].update(ImageShapeHandler.getPixelMeanSem1(pixels, 0, backgroundRing));
        }

        calPixelHeights(indexI, indexF);
    }

    public int calAdjustedPixels(int indexI, int indexF){
//        CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
//      CommonMethods.displayPixels(m_pnPixelsCompensated, "before compensation", impl.getType());
//        Point pt;
        int pixel,r,c;
        Point pt;
        int dtb;//practical radius, max distToBorder

       for(r=indexI;r<indexF;r++){
           dtb=m_pnDistToBorder[r][1]+1;
           if(dtb<m_nNumRings) continue;
           if(m_pnAdjusted[r]>=0) continue;
           for(c=0;c<m_nNumRings;c++){
               m_pnAdjustedPixels[r][c]=0;
           }
           pt=m_cvLocalMaxima.get(r);
           for(c=0;c<dtb;c++){
               pixel=(int)m_pcRingMeanSems[r][c].mean;
               m_pnAdjustedPixels[r][c]=pixel;
           }
        }
        return 1;
    }

    public int calCompensatedPixels(int indexI, int indexF, int iteration){
        calAdjustedPixels(indexI, indexF);
        calCompensatedPixels(m_pnAdjustedPixels,indexI, indexF, iteration);
        return 1;
    }
    public int getNumRings(){
        return m_nNumRings;
    }
    public int calCompensatedPixels(double[][] adjustedPixels, int indexI, int indexF, int iteration){
        m_pnAdjustedPixels=adjustedPixels;
//        if(indexI==0) CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
//        CommonMethods.displayPixels(m_pnPixelsCompensated, "before compensation", impl.getType());
        Point pt;
        int r,dtb;
        double[] IPOCenter;
        int nNumAdjusted=0;
        for(r=indexI;r<indexF;r++){
            pt=m_cvLocalMaxima.get(r);
            if(!adjustable(r))continue;
            if(pt.x==65&&pt.y==115){
                pt=pt;
            }
           dtb=m_pnDistToBorder[r][1]+1;
           if(dtb<m_nNumRings) continue;
           if(m_pnAdjusted[r]>=0) continue;
           if(m_pnOverlappingIPOIndex[r]>=0)continue;
           m_pnAdjusted[r]=iteration;
           nNumAdjusted++;
           m_cSubpixelIS.setEnclosingRectangleCenter(pt);
           IPOCenter=IPOCenters.get(r);
           m_cSubpixelIS.setCenter_Subpixel(IPOCenter[0], IPOCenter[1]);
           ImageShapeHandler.addValue(m_pnPixelsCompensated, m_cSubpixelIS,m_pnAdjustedPixels[r],-1);
        }
        m_pnNumAdjusted[iteration]=nNumAdjusted;
        return 1;
    }
    boolean adjustable(int index){
        MeanSemFractional fms[]=m_pcRingMeanSems[index];
        for(int i=0;i<=m_nMinDTB;i++){
            if(fms[i].n<0.01) return false;
        }
        return true;
    }
    void calR0(){//m_nR0 is the index for the largest ring in the signal area
        int i,nSize=m_dvRsCocentric.size();
        double r;
        m_nR0=-1;

        for(i=0;i<nSize;i++){
            r=m_dvRsCocentric.get(i);
            if(r>=m_nRRefI) break;
            m_nR0=i;
        }
    }
    public void calPixelHeights(int indexI, int indexF){
        int nSize=m_dvRsCocentric.size();
        int i,c;
        Point p;
        double height=0;
        for(i=indexI;i<=indexF;i++){
            p=m_cvLocalMaxima.get(i);
            if(p.x==72&&p.y==79){
                p=p;
            }
            m_cRefRing.setCenter(p);
            backgroundRing.setCenter(p);
            height=0;
            for(c=0;c<nSize;c++){
                height+=m_pcRingMeanSems[i][c].getSum();
            }
            m_pdPixelHeights[i]=height;
            m_pdPixelHeights0[i]=m_pcRingMeanSems[i][0].mean;
        }
        m_bRankedPixelHeights0=false;
    }

    public void calPixelHeights0(){
        calPixelHeights0(0,m_nNumLocalMaxima-1);
    }
    public void calPixelHeights0(int indexI, int indexF){
        double dRef;
        Point pt;
        double[] IPOCenter;
        int nRMax=(int)m_cSubpixelIS.getCCRings().getMaxR()+1;
        ImageShape circle=new CircleImage(nRMax);
        circle.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        CocentricRingKernel ccKernel=new CocentricRingKernel(m_pnPixelsTemp,m_cSubpixelIS,CocentricRingKernel.gaussianWeight);
//        ccKernel.setRadius(3);
        Point p;
        for(int i=indexI;i<=indexF;i++){
            pt=m_cvLocalMaxima.get(i);
            circle.setCenter(pt);
            ImageShapeHandler.copyElements(m_pnPixelsCompensated, m_pnPixelsTemp, circle);

            m_cSubpixelIS.setEnclosingRectangleCenter(pt);
            IPOCenter=IPOCenters.get(i);
            m_cSubpixelIS.setCenter_Subpixel(IPOCenter[0], IPOCenter[1]);
            ImageShapeHandler.addValue(m_pnPixelsTemp, m_cSubpixelIS, m_pnAdjustedPixels[i],1);

            dRef=ImageShapeHandler.getMean(m_pnPixelsCompensated, m_cRefRing,m_pnStamp,m_nExcludingType);
            if(i==208){
                i=i;
            }
            m_pdPixelHeights0[i]=ccKernel.KernelMean(IPOCenter[0], IPOCenter[1], dRef);
        }
        m_bRankedPixelHeights0=false;
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
    public double[][] getAdjustedPixels(){
        return m_pnAdjustedPixels;
    }
    public ArrayList<double[]> getIPOCenters(){
        return IPOCenters;
    }
    public void calRefinedPixelHeights(){
        int nTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        int iter,numAdjusted;
        CommonStatisticsMethods.copyArray(m_pnPixels, m_pnPixelsCompensated);
        stampLandscape();
        CommonStatisticsMethods.copyArray(m_pnRegionStamp, m_pnRegionStampo);
        calPixelStatistics_Initial();
        calRankingRingMeanSems(0,nTrueMaxima-1);
        calGroupRingMeanSems();
        calAveRadius_Subpixel();
        markProximity();
        for(iter=0;iter<4;iter++){
            calPixelStatistics(m_pnPixelsCompensated,0,nTrueMaxima-1,iter);
            calCompensatedPixels(0,nTrueMaxima,iter);
            CommonMethods.displayPixels(m_pnPixelsCompensated, "CompensatedPixels"+iter, ImagePlus.GRAY16);
            adjustLocalMaxima();
        }
//        refinePixelStatitics();
        adjustSatellitePositions();
        calIPOCenters(m_pnPixelsCompensated,m_cvLocalMaxima,IPOCenters,nTrueMaxima,m_nNumLocalMaxima-1);//checked on 3/2/2011
        calPixelHeights0(0,m_nNumLocalMaxima-1);
    }

    void adjustLocalMaxima(){
        ArrayList<Point> localMinima=new ArrayList(), waterSheds=new ArrayList();
        CommonStatisticsMethods.copyArray(m_pnPixelsCompensated, m_pnPixelsTemp);

        int len1,i,j,nTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        Point p,pt;
        int x,y;
        int nn=Integer.MIN_VALUE;

        CommonMethods.getSpecialLandscapePoints(m_pnPixelsTemp, m_pnStamp, false, w, h, LandscapeAnalyzerPixelSorting.localMinimum, localMinima);
        CommonMethods.getSpecialLandscapePoints(m_pnPixelsTemp, m_pnStamp, true, w, h, LandscapeAnalyzerPixelSorting.watershed, waterSheds);

        CommonStatisticsMethods.copyArray(m_pnStamp, m_pnRegionStamp);

        len1=localMinima.size();
        for(i=0;i<len1;i++){
            p=localMinima.get(i);
            x=p.x;
            y=p.y;
           m_pnPixelsTemp[y][x]=nn;
        }

        len1=waterSheds.size();
        for(i=0;i<len1;i++){
            p=waterSheds.get(i);
            x=p.x;
            y=p.y;
            m_pnPixelsTemp[y][x]=nn;
        }

        len1=m_cvOriginalLocalMinima.size();
        for(i=0;i<len1;i++){
            p=m_cvOriginalLocalMinima.get(i);
            x=p.x;
            y=p.y;
           m_pnPixelsTemp[y][x]=nn;
        }

        len1=m_cvOriginalWaterSheds.size();
        for(i=0;i<len1;i++){
            p=m_cvOriginalWaterSheds.get(i);
            x=p.x;
            y=p.y;
            m_pnPixelsTemp[y][x]=nn;
        }

        int index;

        for(j=0;j<nTrueMaxima;j++){
            if(m_pnAdjusted[j]>=0) continue;

            p=m_cvLocalMaxima.get(j);
            x=p.x;
            y=p.y;
            pt=LandscapeAnalyzerPixelSorting.findLocalMaximum(m_pnPixelsTemp, w, h, x, y);
            p.setLocation(pt);
            calIPOCenters(m_pnPixelsCompensated,m_cvLocalMaxima,IPOCenters,j,j);
        }
    }
    void adjustSatellitePositions(){
        ArrayList<Point> localMinima=new ArrayList(), waterSheds=new ArrayList();
        CommonStatisticsMethods.copyArray(m_pnPixelsCompensated, m_pnPixelsTemp);

        int len1,i,j,nTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        Point p,pt;
        int x,y;
        int nn=Integer.MIN_VALUE;

        CommonMethods.getSpecialLandscapePoints(m_pnPixelsTemp, m_pnStamp, false, w, h, LandscapeAnalyzerPixelSorting.localMinimum, localMinima);
        CommonMethods.getSpecialLandscapePoints(m_pnPixelsTemp, m_pnStamp, true, w, h, LandscapeAnalyzerPixelSorting.watershed, waterSheds);

        len1=localMinima.size();
        for(i=0;i<len1;i++){
            p=localMinima.get(i);
            x=p.x;
            y=p.y;
           m_pnPixelsTemp[y][x]=nn;
        }

        len1=waterSheds.size();
        for(i=0;i<len1;i++){
            p=waterSheds.get(i);
            x=p.x;
            y=p.y;
            m_pnPixelsTemp[y][x]=nn;
        }

        len1=m_cvOriginalLocalMinima.size();
        for(i=0;i<len1;i++){
            p=m_cvOriginalLocalMinima.get(i);
            x=p.x;
            y=p.y;
           m_pnPixelsTemp[y][x]=nn;
        }

        len1=m_cvOriginalWaterSheds.size();
        for(i=0;i<len1;i++){
            p=m_cvOriginalWaterSheds.get(i);
            x=p.x;
            y=p.y;
            m_pnPixelsTemp[y][x]=nn;
        }

        CommonStatisticsMethods.setElements(m_pnStamp, -1);
        CommonStatisticsMethods.setElements(m_pnOverlappingIPOIndex, -1);
        len1=m_nNumLocalMaxima-m_nNumSatellites;
        for(i=0;i<len1;i++){
            p=m_cvLocalMaxima.get(i);
            x=p.x;
            y=p.y;
            CommonMethods.markPixel(m_pnPixelsTemp, m_pnStamp, m_pnPixelsTemp1, m_pnPixelsTemp2, w, h, x, y, i);
        }

        int index;
        m_cvOverlappedSatellites.clear();
        m_nNumOverlappingIOPs=0;
        for(j=len1;j<m_nNumLocalMaxima;j++){
            p=m_cvLocalMaxima.get(j);
            x=p.x;
            y=p.y;
            if(m_pnPixelsTemp[y][x]==nn) {
                p=m_pcOriginalSatellites[j-nTrueMaxima];
                x=p.x;
                y=p.y;
            }
            pt=LandscapeAnalyzerPixelSorting.findLocalMaximum(m_pnPixelsTemp, w, h, x, y);
            index=m_pnStamp[pt.y][pt.x];
            if(index>=0) {
                m_cvOverlappedSatellites.add(new IntPair(j,index));
                m_pnOverlappingIPOIndex[j]=index;
                m_nNumOverlappingIOPs++;
            }else{
                CommonMethods.markPixel(m_pnPixelsTemp,m_pnStamp, m_pnPixelsTemp1, m_pnPixelsTemp2, w, h, pt.x, pt.y, j);
            }
            p.setLocation(pt);
        }
    }
    public int[] getOverlappingIPOIndexes(){
        return m_pnOverlappingIPOIndex;
    }
    public int getNumOverlappedIOP(){
        return m_nNumOverlappingIOPs;
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
    public int getNumSatellites(){
        return m_nNumSatellites;
    }
    public double[] getGroupingDelimiters(){
        return m_pdGroupingDelimiters;
    }
    public int[][] getDistsToBorders(){
        return m_pnDistToBorder;
    }
    void getNeighboringLMIndexes(int index, ArrayList<Integer> indexes){
        Point po=m_cvLocalMaxima.get(index),p;
        int region0=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnRegionStamp[po.y][po.x]);
        m_cSubpixelIS.setCenter_Subpixel(po.x,po.y);
        ImageShape is=m_cSubpixelIS.getEnclosingRectangle();
        is.setCenter(po);
        ArrayList<Point> points=is.getPerimeterPoints();
        ContourFollower.removeOffBoundPoints(w, h, points);
        Set<Integer> indexSet=new TreeSet();
        int i,len=points.size(),region,lmIndex;
        for(i=0;i<len;i++){
            p=points.get(i);
            region=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnRegionStamp[p.y][p.x]);
            if(region<=0)//there should not be case for region==0
                continue;
            lmIndex=m_pnRegionToLMIndexes[region-1];
            if(lmIndex!=index){
                indexSet.add(lmIndex);
            }
        }
        len=indexSet.size();
        Iterator it=indexSet.iterator();
        while(it.hasNext()){
            lmIndex=(Integer)it.next();
            indexes.add(lmIndex);
        }
    }
    void buildLMToRegionCoversion(){//m_pnRegionStamp was updated by the method updatePixels
        int index,region;
        Point p;
        ArrayList<Integer> spareIndexes=new ArrayList();
        ArrayList<Point> sparePoints=new ArrayList();
        for(region=0;region<m_nTrueMaxima;region++){
            m_pnRegionToLMIndexes[region]=-1;
        }
        for(index=0;index<m_nTrueMaxima;index++){
            p=m_cvLocalMaxima.get(index);
            region=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnRegionStamp[p.y][p.x]);
            if(region<=0){
                region=region;
            }
            m_pnRegionToLMIndexes[region-1]=index;//region number starts from 1
            m_pnLMToRegionIndexes[index]=region;
        }
    }
    void calAveRadius_Subpixel(){
        int len=m_pcRingMeanSems_Group.length;
        double sd,dSur;
        MeanSem1 ms;
        int i,j;
        Point p;
        for(i=0;i<len;i++){
            p=m_cvLocalMaxima.get(i);
            dSur=m_pcRingMeanSems_Group[i][m_nNumRings-1].mean;
            for(j=0;j<m_nNumRings;j++){
                ms=m_pcRingMeanSems_Group[i][j];
                sd=ms.getSD();
//                if((ms.mean-dSur)<2*sd) break;
                if((ms.mean-dSur)<sd) break;
                m_pnAveRadius[i]=j;
            }
        }
    }
    void calGroupRingMeanSems(){
        int numTrueMaxima=m_nNumLocalMaxima-m_nNumSatellites;
        if(numTrueMaxima<m_nPercentileDivision) IJ.error("there less numbers of true local maxima than required number of groups");
        double dDelta=(double)numTrueMaxima/(double)m_nPercentileDivision;
        double dPosition=0;
        int rI,rF,i,c;
        rI=0;
        int index=0;
        for(i=0;i<m_nPercentileDivision;i++){
            dPosition+=dDelta;
            rF=Math.min(numTrueMaxima-1,(int)(dPosition+0.5));
            if(rF>=numTrueMaxima) rF=numTrueMaxima-1;
            for(c=0;c<m_nNumRings;c++){
                m_pcRingMeanSems_Group[i][c]=CommonStatisticsMethods.mergedMeanSem(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,c);
                m_pcRingMeanSems_Normalized[i][c]=CommonStatisticsMethods.mergedMeanSem_Normalized(m_pcRingMeanSems,m_pnIndexesPt,rI,rF,1,0,c);
             }
            for(c=0;c<2;c++){
                m_pcRingMeanSems_Group_background[i][c]=CommonStatisticsMethods.mergedMeanSem(m_pcRingMeanSems_background,m_pnIndexesPt,rI,rF,1,c);
                m_pcRingMeanSems_Normalized_background[i][c]=CommonStatisticsMethods.mergedMeanSem_Normalized(m_pcRingMeanSems_background,m_pnIndexesPt,rI,rF,1,0,c);
            }
            if(m_bAllowRankingPixelHeights){
                if(i<m_nPercentileDivision-1){
                    index=m_pnIndexesPt[rF+1];
                    m_pdGroupingDelimiters[i]=m_pcRingMeanSems[index][0].mean;
                }
            }
            rI=rF+1;
        }
        m_cGrouper=new Categorizer(m_pdGroupingDelimiters,true);
    }
    void markProximity(){
        CommonStatisticsMethods.setElements(m_pnProximityMap, 0);
        int i,j,index,r,region0,region,len;
        double dv;
        ArrayList<Double> dvRs=m_cSubpixelIS.getCCRings().getRs();
        ImageShape proximityCircle;
        Point po,p;
        ArrayList<Point> innerPoints=new ArrayList();
        for(i=0;i<m_nTrueMaxima;i++){
            if(m_pnAdjusted[i]>=0) continue;
            po=m_cvLocalMaxima.get(i);
            dv=m_pcRingMeanSems[i][0].mean;
            index=m_cGrouper.getCatIndex(dv);
            r=dvRs.get(m_pnAveRadius[index]).intValue()+1;
            proximityCircle=m_pcProximityCircles[r-1];
            proximityCircle.setCenter(po);
            proximityCircle.getInnerPoints(innerPoints);
            len=innerPoints.size();
            region0=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnRegionStamp[po.y][po.x]);
            for(j=0;j<len;j++){
                p=innerPoints.get(j);
                if(p.y==114&&p.x==59){
                    p=p;
                }
                region=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnRegionStamp[p.y][p.x]);
                if(region==region0) continue;
                m_pnProximityMap[p.y][p.x]=proximityLabel;
            }
        }
    }
}
