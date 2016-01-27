/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import FluoObjects.IntensityPeakObject;
import java.util.ArrayList;
import ij.ImagePlus;
import utilities.CustomDataTypes.intRange;
import ij.gui.Roi;
import utilities.ArrayofArrays.IntArray;
import utilities.ArrayofArrays.IntArray2;
import utilities.QuickSort;
import utilities.CommonMethods;
import java.awt.Color;
import java.awt.Point;
import ImageAnalysis.ContourFollower;
import utilities.ArrayofArrays.IntRangeArray;
import utilities.ArrayofArrays.IntRangeArray2;
import java.awt.Rectangle;
import utilities.QuickSortInteger;
import ImageAnalysis.LandscapeAnalyzer;
import ImageAnalysis.IPOPixelHeightsHandler;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.CommonStatisticsMethods;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.statistics.MeanSem0;
import utilities.Geometry.ImageShapes.RectangleImage;
import utilities.statistics.GaussianDistribution;


/**
 *
 * @author Taihao
 */
public class IntensityPeakObjectHandler {

    int z, t, r,w,h,trackingLength;
    int pixels[][],pixelsr[][],pixelStamp[][];
    int pixelsp[][],pixelspr[][];
    int pixelsCompensated[][];
    int pixelst[][],pnScratch[][];
    ArrayList <IntensityPeakObject> IPOs= new ArrayList<IntensityPeakObject>();
    ArrayList <IntensityPeakObject> backgroundIPOs= new ArrayList<IntensityPeakObject>();
    IntArray[][] ipoGrids, backgroundIPOGrids;
    public static final int gridSize=20;
    int minPeakIntensity;
    int minPeakPercentileIndex;//
    double backgroundPercentiles[];
    int backgroundIntensities[];
    int xGrid,yGrid;
    int refPixels[];
    int searchingDist;
    ArrayList<Point> m_cvLocalMaxima;
    ArrayList<Double> m_dvPixelHeights;
    ArrayList<Double> m_dvPixelHeights0;
    ArrayList<MeanSem0> m_dvBackgroundPixelValues;
    ArrayList<Double> m_dvBackgroundPixels;
    int[] m_pnPercentileIndexes;
    boolean m_bBasedOnPrecomputedPixelHeight;
    ArrayList<ImageShape> m_cvIPORings;
    double[] m_pdPercentileCutoff;
    int[] m_pnPixelRange=new int[2];
    String contourFilePath;
    int m_nStackOffset,m_nNumLocalMaxima;
    MeanSem0 m_cRefMS;
    ImageShape m_cSigShape,m_cSurShape;
    public IntensityPeakObjectHandler(){

    }
    public IntensityPeakObjectHandler(int z, int t, int r, int searchingDist, int pixels[][], int pixelsp[][], int pixelsCompensated[][],int[][] pixelst,
            int w, int h, intRange isr, int trackingLength, double []backgroundPercentile, ArrayList<Point> localMaxima,
            ArrayList<Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, int[] pnPercentileIndexes, 
            int minPeakPercentileIndex,ArrayList<ImageShape> cvRings, double[]pdPercentileCutoff, ArrayList<MeanSem0> dvBackgroundPixelValues,
            ArrayList<Double> backgroundPixels, MeanSem0 cRefMS, int nStackOffset,int nNumLocalMaxima,ImageShape cSigShape, ImageShape cSurShape){
        m_cRefMS=cRefMS;
        m_dvBackgroundPixels=backgroundPixels;
        m_nStackOffset=nStackOffset;
        m_nNumLocalMaxima=nNumLocalMaxima;
        m_cvLocalMaxima=localMaxima;
        m_dvPixelHeights=dvPixelHeights;
        m_dvPixelHeights0=dvPixelHeights0;
        m_pnPercentileIndexes=pnPercentileIndexes;
        m_pdPercentileCutoff=pdPercentileCutoff;
        m_dvBackgroundPixelValues=dvBackgroundPixelValues;
        m_cSigShape=cSigShape;
        m_cSurShape=cSurShape;
        this.z=z;
        this.t=t;
        this.r=r;
        this.w=w;
        this.h=h;
        this.searchingDist=searchingDist;
        this.pixels=pixels;
        this.pixelsp=pixelsp;
        this.pixelsCompensated=pixelsCompensated;
        this.pixelst=pixelst;
        this.trackingLength=trackingLength;
        this.minPeakPercentileIndex=minPeakPercentileIndex;
        this.backgroundPercentiles=backgroundPercentile;
        IPOs.clear();
        int i,j;
        this.pixels=new int[h][w];
//        this.pixelsr=new int[h][w];
//        this.pixelsp=new int[h][w];
 //       this.pixelspr=new int[h][w];
 //       this.pixelsr=pixelsr;
//        this.pixelsp=pixelsp;
 //       this.pixelspr=pixelspr;//a IPOH keeps (and uses later) only the pixel values of the original image.
        backgroundIntensities=new int[16];
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                this.pixels[i][j]=pixels[i][j];
            }
        }

        m_bBasedOnPrecomputedPixelHeight=true;
        m_cvIPORings=cvRings;
        buildIPObjects_PrecomputedPixelHeights();
        refineIPOSelection();
        initIPOGrids();
        registerIPOs();
        initBackgroundIPOGrids();
        registerBackgroundIPOs();
    }
    public void setContourFilePath(String path){
        contourFilePath=path;
    }
    public void getClosestIPOs(int dist, int nx, IntensityPeakObject IPO0,ArrayList<IntensityPeakObject> nIPOs0, ArrayList <Double> dr){
        int x0=IPO0.cx;
        int y0=IPO0.cy;
        getClosestIPOs(dist,nx,x0,y0,IPO0.getPixelHeight(),nIPOs0,dr);
    }
    public void getClosestBackgroundIPOs(int dist, int nx, int cx, int cy, double dPixelHeight, ArrayList<IntensityPeakObject> nIPOs0, ArrayList <Double> dr){

        IntArray[][] iat=ipoGrids;
        ArrayList<IntensityPeakObject> ipots=IPOs;

        ipoGrids=backgroundIPOGrids;
        IPOs=backgroundIPOs;
        getClosestIPOs(dist,nx,cx,cy,dPixelHeight,nIPOs0,dr);

        ipoGrids=iat;
        IPOs=ipots;
        
    }
    public void getClosestIPOs(int dist, int nx, int cx, int cy, double dPixelHeight, ArrayList<IntensityPeakObject> nIPOs0, ArrayList <Double> dr){
        //dist cutoff dist, nx: max number of IPOs to include
        nIPOs0.clear();
        dr.clear();
        ArrayList<IntensityPeakObject> nIPOs=new ArrayList<IntensityPeakObject>();
        int Xi, Yi, Xf, Yf,i,j,k,index;
        int x0=cx;
        int y0=cy;
        int xi=x0-dist,yi=y0-dist,xf=x0+dist,yf=y0+dist;
        if(xi<0) xi=0;
        if(xf>w-1) xf=w-1;
        if(yi<0) yi=0;
        if(yf>h-1) yf=h-1;
        Xi=xi/xGrid;
        Xf=xf/xGrid;
        Yi=yi/xGrid;
        Yf=yf/xGrid;
        ArrayList <Integer> ir;
        ArrayList <Integer> indexes=new ArrayList <Integer>();
        int size;
        int num=0;
        double dst;
        IntensityPeakObject IPO;
        ArrayList <Double> dh=new ArrayList();//differences in the pixel heights
        for(i=Yi;i<=Yf;i++){
            for(j=Xi;j<=Xf;j++){
                ir=ipoGrids[i][j].m_intArray;
                size=ir.size();
                for(k=0;k<size;k++){
                    index=ir.get(k);
                    if(!CommonMethods.containsContent(indexes, index)) indexes.add(index);
                }
            }
        }
        ir=indexes;
        indexes=new ArrayList();
        size=ir.size();
        for(k=0;k<size;k++){
            index=ir.get(k);
            IPO=IPOs.get(index);
            dst=IPO.dist(cx,cy);
            if(dst<=dist)
            {
                nIPOs.add(IPO);
                dr.add(dst);
                dh.add(Math.abs(dPixelHeight-IPO.getPixelHeight()));
                indexes.add(num);
                num++;
            }
        }
        QuickSort.quicksort(dr, indexes);
//        QuickSort.quicksort(dh, indexes);
        size=dr.size();
        if(nx>size)nx=size;
        for(i=0;i<nx;i++){
            index=indexes.get(i);
            nIPOs0.add(nIPOs.get(index));
        }
    }

    public void getClosestIPOs0(int dist, int nx, int cx, int cy, double dPixelHeight, ArrayList<IntensityPeakObject> nIPOs0, ArrayList <Double> dr){
        //dist cutoff dist, nx: max number of IPOs to include
        nIPOs0.clear();
        dr.clear();
        ArrayList<IntensityPeakObject> nIPOs=new ArrayList<IntensityPeakObject>();
        int Xi, Yi, Xf, Yf,i,j,k,index;
        int x0=cx;
        int y0=cy;
        int xi=x0-dist,yi=y0-dist,xf=x0+dist,yf=y0+dist;
        if(xi<0) xi=0;
        if(xf>w-1) xf=w-1;
        if(yi<0) yi=0;
        if(yf>h-1) yf=h-1;
        Xi=xi/xGrid;
        Xf=xf/xGrid;
        Yi=yi/xGrid;
        Yf=yf/xGrid;
        ArrayList <Integer> ir;
        ArrayList <Integer> indexes=new ArrayList <Integer>();
        int size;
        int num=0;
        double dst;
        IntensityPeakObject IPO;
        ArrayList <Double> dh=new ArrayList();//differences in the pixel heights
        for(i=Yi;i<=Yf;i++){
            for(j=Xi;j<=Xf;j++){
                ir=ipoGrids[i][j].m_intArray;
                size=ir.size();
                for(k=0;k<size;k++){
                    index=ir.get(k);
                    IPO=IPOs.get(index);
                    dst=IPO.dist(cx,cy);
                    if(dst<=dist)
                    {
                        nIPOs.add(IPO);
                        dr.add(dst);
                        dh.add(Math.abs(dPixelHeight-IPO.getPixelHeight()));
                        indexes.add(num);
                        num++;
                    }
                }
            }
        }
        QuickSort.quicksort(dr, indexes);
//        QuickSort.quicksort(dh, indexes);
        size=dr.size();
        if(nx>size)nx=size;
        for(i=0;i<nx;i++){
            index=indexes.get(i);
            nIPOs0.add(nIPOs.get(index));
        }
    }

    void initIPOGrids(){
        ipoGrids=new IntArray[gridSize][gridSize];
        int i,j;
        for(i=0;i<gridSize;i++){
            for(j=0;j<gridSize;j++){
                ipoGrids[i][j]=new IntArray();
            }
        }
        xGrid=w/gridSize;
        yGrid=h/gridSize;
        if(w%gridSize>0)xGrid++;
        if(h%gridSize>0)yGrid++;
    }
    void initBackgroundIPOGrids(){
        IntArray[][] iat=ipoGrids;
        ipoGrids=backgroundIPOGrids;
        initIPOGrids();
        backgroundIPOGrids=ipoGrids;
        ipoGrids=iat;
    }
    void resetIPOGrids(){
        int i,j;
        for(i=0;i<gridSize;i++){
            for(j=0;j<gridSize;j++){
                ipoGrids[i][j].m_intArray.clear();
            }
        }
        xGrid=w/gridSize;
        yGrid=h/gridSize;
        if(w%gridSize>0)xGrid++;
        if(h%gridSize>0)yGrid++;
    }
    void resetIPOBackgroundIPOGrids(){
        IntArray[][] iat=ipoGrids;
        ipoGrids=backgroundIPOGrids;
        resetIPOGrids();
        backgroundIPOGrids=ipoGrids;
        ipoGrids=iat;
    }
    void registerIPOs(){
        int i,x,y;
        int size=IPOs.size();
        IntensityPeakObject IPO;
        for(i=0;i<size;i++){
            IPO=IPOs.get(i);
            x=IPO.getCX()/xGrid;
            y=IPO.getCY()/yGrid;
            ipoGrids[y][x].m_intArray.add(i);
        }
    }
    void registerBackgroundIPOs(){
        IntArray[][] iat=ipoGrids;
        ArrayList<IntensityPeakObject> ipots=IPOs;
        ipoGrids=backgroundIPOGrids;
        IPOs=backgroundIPOs;
        registerIPOs();
        backgroundIPOGrids=ipoGrids;
        backgroundIPOs=IPOs;
        ipoGrids=iat;
        IPOs=ipots;
    }
    public int getXgrid(){
        return xGrid;
    }
    public int getYgrid(){
        return yGrid;
    }

    void setMinPeakIntensity(){
        ArrayList<Point> localMaxima=new ArrayList();
        ArrayList<Double> pixelHeights=new ArrayList();
        CommonMethods.getSpecialLandscapePoints(pixelspr, w, h, LandscapeAnalyzer.localMaximum, localMaxima);
//        CommonMethods.getPixelHeights_ave(pixelspr,w,h,r,localMaxima,pixelHeights);
        ArrayList<Integer> indexes=new ArrayList();
        int len=localMaxima.size();
        Point p;
        int [][]pixelst=pixelsp;
        pixelsp=pixelspr;
        double[] pdSigSur=new double[2];
        for(int it=0;it<len;it++){
            p=localMaxima.get(it);
            pixelHeights.add(getPixelHeight(p.x,p.y,pdSigSur));
            indexes.add(it);
        }
        pixelsp=pixelst;
        QuickSort.quicksort(pixelHeights,indexes);
        double per;
        int size=pixelHeights.size();
        int index=(int)((1-backgroundPercentiles[minPeakPercentileIndex])*size);
        minPeakIntensity =pixelHeights.get(index).intValue();
        for(int i=0;i<16;i++){
            per=backgroundPercentiles[i];
            index=(int)((1-per)*size);
            backgroundIntensities[i]=pixelHeights.get(index).intValue();
        }
    }

    double getPixelHeight(int x, int y, double[] pdSigSur){

        if(m_bBasedOnPrecomputedPixelHeight){
//            return IPOPixelHeightsHandler.getPixelHeight(pixels, m_cvIPORings, new Point(x,y));
            return IPOPixelHeightsHandler.getPixelHeight(pixels, pixelsCompensated,m_cSigShape, m_cSurShape,new Point(x,y),pdSigSur);
        }else{
            return CommonMethods.getPixelHeight_ave(pixelsp, w, h, x, y, r);
        }
    }

    double getPixelHeight0(int x, int y){
        if(m_bBasedOnPrecomputedPixelHeight){
            return IPOPixelHeightsHandler.getPixelHeight0(pixels, m_cvIPORings, new Point(x,y));
        }else{
            return CommonMethods.getPixelHeight_ave(pixelsp, w, h, x, y, r);
        }
    }
/*
    void buildIPObjects_IPOPixelHeights(){
        m_cvLocalMaxima=new ArrayList();
        m_dvPixelHeights=new ArrayList();
        m_dvPixelHeights0=new ArrayList();
        m_pnPercentileIndexes=new int[backgroundPercentiles.length];
        IPOPixelHeightsHandler.detectIPOs(pixels, pixelsp, pixelspr, m_cvLocalMaxima, m_dvPixelHeights, m_dvPixelHeights0, 5, 8, 8, backgroundPercentiles, m_pnPercentileIndexes);
        int x,y,i,j,pixel,len=m_cvLocalMaxima.size();
        Point p;
        int [][] pixelst=pixels;
        double pixelHeight;
        pixels=pixelsp;
        int nI,nF=len-1;
        for(i=0;i<=minPeakPercentileIndex;i++){
            nI=m_pnPercentileIndexes[i];
            for(j=nI;j<=nF;j++){
                p=m_cvLocalMaxima.get(i);
                x=p.x;
                y=p.y;
                IntensityPeakObject IPO=new IntensityPeakObject(x,y,z,r,t,pixels,w,h,IPOs.size(),trackingLength);
                IPO.setPixelHeight(m_dvPixelHeights.get(j));
                IPO.setPixelHeight0(m_dvPixelHeights0.get(j));
                IPO.percentileIndex=i;
                IPO.setPeakPixel(pixels[y][x]);
                IPOs.add(IPO);
            }
            nF=nI-1;
        }
    }*/
    void buildIPObjects_PrecomputedPixelHeights(){

        int x,y,i,j,len=m_nNumLocalMaxima;
        Point p;
        double pixelHeight,pv,cutoff=m_pdPercentileCutoff[minPeakPercentileIndex];
        int nI,nF=len-1;
        double mean=m_cRefMS.mean,sd=m_cRefMS.getSD();

        for(i=0;i<=minPeakPercentileIndex+0;i++){
//        for(i=0;i<len1;i++){
            nI=m_pnPercentileIndexes[i];
            for(j=nI;j<=nF;j++){
                p=m_cvLocalMaxima.get(j+m_nStackOffset);
                x=p.x;
                y=p.y;
                IntensityPeakObject IPO=new IntensityPeakObject(x,y,z,r,t,pixels,w,h,IPOs.size(),trackingLength);
                IPO.setPixelHeight(m_dvPixelHeights.get(j+m_nStackOffset));
                if(IPO.getPixelHeight()>=2*cutoff) IPO.bTrackOpening=true;
                IPO.peakPixel_ave=getPeakPixelAve(x,y);
                IPO.sur_quantile=getSurQuantile(x,y);
                IPO.setPixelHeight0(m_dvPixelHeights0.get(j+m_nStackOffset));
                IPO.setBackgroundPixel((int)(m_dvBackgroundPixels.get(j+m_nStackOffset)+0.5));
                IPO.percentileIndex=i;
                IPO.setPeakPixel(pixels[y][x]);
                IPO.basedOnPrecomputedPixelHeight();
                IPO.localMaximumIndex=j;
                pixelHeight=m_dvPixelHeights.get(j+m_nStackOffset);
                pv=GaussianDistribution.Phi(pixelHeight, mean, sd);
                pv=1.-pv;
                if(pv<1E-20) {
                    pv=1E-20;
                }
                pv=Math.log10(pv);
                IPO.pValue=pv;
                IPOs.add(IPO);
            }
            nF=nI-1;
        }/*
        for(j=0;j<=nF;j++){
            p=m_cvLocalMaxima.get(j+m_nStackOffset);
            x=p.x;
            y=p.y;
            IntensityPeakObject IPO=new IntensityPeakObject(x,y,z,r,t,pixels,w,h,IPOs.size(),trackingLength);
            IPO.setPixelHeight(m_dvPixelHeights.get(j+m_nStackOffset));
            IPO.setPixelHeight0(m_dvPixelHeights0.get(j+m_nStackOffset));
            IPO.percentileIndex=i;
            IPO.setPeakPixel(pixels[y][x]);
            IPO.basedOnPrecomputedPixelHeight();
            IPO.localMaximumIndex=j;
            backgroundIPOs.add(IPO);
        }*/
    }
    void buildIPObjects(){
        if(z==10){
            z=z;
        }
        setMinPeakIntensity();
        ArrayList <Point> localMaxima=new ArrayList();
        pixelStamp=new int[w][h];
        CommonMethods.getSpecialLandscapePoints(pixelsp, pixelStamp, false, w, h, LandscapeAnalyzer.localMaximum, localMaxima);
        ArrayList <Double> pixelHeights=new ArrayList();
//        CommonMethods.getPixelHeights_ave(pixelsp, w, h, r, localMaxima, pixelHeights);
        int x,y,i,j,pixel;
        int len=localMaxima.size();
        Point p;
        int nPer=0,perIndex=15;
        int [][] pixelst=pixels;
        pixels=pixelsp;
        double pixelHeight;
        double []pdSigSur=new double[2];
        for(i=0;i<len;i++){
            p=localMaxima.get(i);
            x=p.x;
            y=p.y;
//            pixel=pixelHeights.get(i).intValue();
            pixelHeight=getPixelHeight(x,y,pdSigSur);
            perIndex=15;
            for(j=0;j<=minPeakPercentileIndex;j++){
                if(pixelHeight>=backgroundIntensities[j]){
                    perIndex=j;
                    break;
                }
            }
            if(perIndex<=minPeakPercentileIndex){
                IntensityPeakObject IPO=new IntensityPeakObject(x,y,z,r,t,pixels,w,h,IPOs.size(),trackingLength);
                IPO.setPixelHeight(pixelHeight);
                IPO.percentileIndex=perIndex;
                IPO.setPeakPixel(pixels[y][x]);
                IPOs.add(IPO);
            }
        }
        pixels=pixelst;
//        setContours();
//        resolveMultiplePeaks();
    }
    void setContours(){
        int size=IPOs.size();
        int lo,hi=255;
        int i,j;
        IntensityPeakObject IPO;
        for(i=0;i<size;i++){
            IPO=IPOs.get(i);
            lo=90*pixelsp[IPO.cy][IPO.cx]/100;
            ArrayList<Point> contour=ContourFollower.getContour_Out(pixelsp, w, h, new Point(IPO.cx,IPO.cy), lo, hi);
            IPO.setContour(pixelsp,contour,new intRange(lo,hi));
        }
    }
    public ArrayList <IntensityPeakObject> getIPOs(){
        return IPOs;
    }
    public IntensityPeakObject getIPO(int index){
        return IPOs.get(index);
    }

    IntensityPeakObject mergeIPOs(ArrayList<IntensityPeakObject> IPOs, int index){
       IntensityPeakObject IPO;
       int x=0,y=0,size=IPOs.size();
       int i,in=0,px=-1,ix=0,p;
       for(i=0;i<size;i++){
           IPO=IPOs.get(i);
           x=IPO.cx;
           y=IPO.cy;
           if(x==100&&y==80&&z==30){
               x=x;
           }
           p=pixels[y][x];
           if(p>px){
               px=p;
               ix=i;
           }
       }
       IPO=IPOs.get(ix);
       return IPO;
    }

    void resolveMultiplePeaks(){
        int i,j,k,size=IPOs.size(),size1,size2,index;
        IntensityPeakObject IPO,IPO1,IPO2;
        index=0;
        size=IPOs.size();
        ArrayList<IntensityPeakObject> nIPOs=new  ArrayList<IntensityPeakObject>();
        for(i=0;i<size;i++){
            IPO=IPOs.get(i);
            for(j=i+1;j<size;j++){
                IPO1=IPOs.get(j);
                if(IPO.contains(new Point(IPO1.cx,IPO1.cy))){
                  IPO.IPOsWithinContour.add(IPO1);
                }
                if(IPO1.contains(new Point(IPO.cx,IPO.cy))){
                    IPO1.IPOsWithinContour.add(IPO);
                }
            }
        }

        ArrayList <IntensityPeakObject> IPOst=new ArrayList();
        IntensityPeakObject IPOt;
        for(i=0;i<size;i++){
            IPO=IPOs.get(i);
            size1=IPO.IPOsWithinContour.size();
            IPOst.add(IPO);;
            for(j=0;j<size1;j++){
                IPO1=IPO.IPOsWithinContour.get(j);
                size2=IPO1.IPOsWithinContour.size();
                for(k=0;k<size2;k++){
                    IPO2=IPO1.IPOsWithinContour.get(k);
                    if(IPO2.equals(IPO)){
                        if(IPO.dist(IPO2)>8) continue;
                        if(IPO.getPixelHeight()>IPO1.getPixelHeight()){
                            IPO1.connected=true;
                        }else{
                            IPO.connected=true;
                        }
//                        IPOst.add(IPO1);
                    }
                }
            }
//            IPOt=mergeIPOs();
        }
    }
    public void markIPOs(ImagePlus impl){
        Color c;
        int size=IPOs.size();
        for(int i=0;i<size;i++){
            c=CommonMethods.randomColor();
            impl.setColor(c);
//            IPOs.get(i).markIPO(impl);
            IPOs.get(i).markContour(impl);
        }
        size=backgroundIPOs.size();
        for(int i=0;i<size;i++){
            c=CommonMethods.randomColor();
            impl.setColor(c);
//            IPOs.get(i).markIPO(impl);
            backgroundIPOs.get(i).markContour(impl);
        }
    }
    public IntensityPeakObject pickIPO(intRange xRange, intRange yRange){
        int xi=xRange.getMin(),xf=xRange.getMax();
        if(xi<0)xi=0;
        if(xf>=w) xf=w-1;
        int yi=yRange.getMin(),yf=yRange.getMax();
        if(yi<0)yi=0;
        if(yf>=w) yf=w-1;
        int x,y;
        int p,px=-1,xx=0,yx=0;
        for(y=yi;y<=yf;y++){
            for(x=xi;x<=xf;x++){
                p=pixels[y][x];
                if(p>px){
                    yx=y;
                    xx=x;
                    px=p;
                }
            }
        }
        IntensityPeakObject IPO=new IntensityPeakObject(xx,yx,z,r,t,pixels,w,h,IPOs.size(),trackingLength);;
        return IPO;
    }
    public int getPercentileIndex(double pixelHeight){
        int index=15;
        for(int i=0;i<16;i++){
            if(pixelHeight>=backgroundIntensities[i]){
                index=i;
                break;
            }
        }
        return index;
    }
    public double getPixelHeightCutoff(){
        return m_pdPercentileCutoff[minPeakPercentileIndex];
    }
    void refineIPOSelection(){
        int len=IPOs.size();
        CommonStatisticsMethods.copyArray(pixelsp, pixelst);
        boolean selected[]=new boolean[len];
        IntensityPeakObject IPO;
        ArrayList <Integer> types=new ArrayList();
        types.add(LandscapeAnalyzer.watershed);
        types.add(LandscapeAnalyzer.localMinimum);
        intRange pixelRange=CommonStatisticsMethods.getRange(pixelsp);
        CommonMethods.setPixelAtSpecialLandsapePoints(pixelsp,types,0);
        double background;
        pixelRange=CommonStatisticsMethods.getRange(pixelsp);
        int pMax=pixelRange.getMax();
        ArrayList<Point> contour;
        int i,lo,index,nArea;
        Point pt;
        ImageShape shape;
        MeanSem0 ms,backgroundms;
        double pixelHeight, cutoff=m_pdPercentileCutoff[minPeakPercentileIndex],sd;
        intRange xFRange=new intRange(0,w-1),yFRange=new intRange(0,h-1);
        double dh,base;
        ImageShape rect=new RectangleImage(7,7);
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            if(IPO.cx==154&&IPO.cy==210&&IPO.cz==139){
                i=i;
            }
            index=IPO.localMaximumIndex;
            pt=m_cvLocalMaxima.get(index+m_nStackOffset);
            if(CommonMethods.isOnEdge(w, h, pt)){
                selected[i]=false;
                continue;
            }

            backgroundms=m_dvBackgroundPixelValues.get(index+m_nStackOffset);
            background=m_dvBackgroundPixels.get(index+m_nStackOffset);
            base=backgroundms.mean;
            dh=pixelsp[IPO.cy][IPO.cx]-base;
            sd=backgroundms.getSD();

            lo= (int)(base+0.15*dh);
//            lo= (int)(backgroundms.mean+2*backgroundms.getSD());
//            lo= (int)(backgroundms.mean+backgroundms.getSD());
            if(lo>=pixelsp[pt.y][pt.x]) {
                selected[i]=false;
                continue;
            }
            contour=ContourFollower.getContour_Out(pixelsp, w, h, pt,lo, pMax);
            shape=ImageShapeHandler.buildImageShape(contour);
            shape.setFrameRanges(xFRange, yFRange);
            ms=ImageShapeHandler.getPixelMeanSem(pixels, shape);
//            pixelHeight=ms.n*(ms.mean-backgroundms.mean);
            pixelHeight=ms.n*(ms.mean-background);//8/8/2010
//                IPO.setContour(contour);
//                IPO.setEnclosedShape(shape);
//                IPO.setPixelHeightC90p(pixelHeight);
//                IPO.setContourCutoff(lo);
//            if(pixelHeight>cutoff){
            if(IPO.percentileIndex<=minPeakPercentileIndex){
                selected[i]=true;
            }else{
                selected[i]=false;
            }

            lo= (int)(backgroundms.mean+2*backgroundms.getSD());
//            lo= (int)(backgroundms.mean+backgroundms.getSD());
            if(lo>pixelsp[pt.y][pt.x]) {
                selected[i]=false;
                continue;
            }
            contour=ContourFollower.getContour_Out(pixelsp, w, h, pt,lo, pMax);
            shape=ImageShapeHandler.buildImageShape(contour);
            shape.setFrameRanges(xFRange, yFRange);
            ms=ImageShapeHandler.getPixelMeanSem(pixels, shape);
            pixelHeight=ms.n*(ms.mean-background);
                IPO.setContour(contour);
                IPO.setEnclosedShape(shape);
                IPO.setPixelHeightC(pixelHeight);
                IPO.setContourCutoff(lo);
            rect.setCenter(pt);
            rect.setFrameRanges(xFRange, yFRange);
            ms=ImageShapeHandler.getPixelMeanSem(pixels, rect);
            pixelHeight=ms.n*(ms.mean-background);
//            IPO.setPixelHeight0(pixelHeight);
            int num=IPOs.size();
        }
        for(i=0;i<len;i++){
            index=len-1-i;
            if(!selected[index]){
                IPO=IPOs.get(index);
                backgroundIPOs.add(IPO);
                IPOs.remove(index);
            }
        }
        CommonStatisticsMethods.copyArray(pixelst, pixelsp);
    }
    public int[][] getPixels(){
        return pixels;
    }
    public double getPeakPixelAve(int x, int y){//this method picks one of that four squares (for pixels) containing the
        //point (x,y) that gives the highest average pixel value.
        double peakAve=Double.NEGATIVE_INFINITY;
        double ave=0,num;
        int[][]pixels=pixelsp;
        int h=pixels.length,w=pixels[0].length;
        int i,j,j0=Math.max(0, x-1),j1=Math.min(x+1, w-1),i0=Math.max(0, y-1),i1=Math.min(h-1, y+1);
        ave=0;
        num=0;
        for(i=i0;i<=y;i++){
            for(j=j0;j<=x;j++){
                num+=1;
                ave+=pixels[i][j];
            }
        }
        ave/=num;
        if(ave>peakAve) peakAve=ave;

        ave=0;
        num=0;
        for(i=i0;i<=y;i++){
            for(j=x;j<=j1;j++){
                num+=1;
                ave+=pixels[i][j];
            }
        }
        ave/=num;
        if(ave>peakAve) peakAve=ave;

        ave=0;
        num=0;
        for(i=y;i<=i1;i++){
            for(j=j0;j<=x;j++){
                num+=1;
                ave+=pixels[i][j];
            }
        }
        ave/=num;
        if(ave>peakAve) peakAve=ave;

        ave=0;
        num=0;
        for(i=y;i<=i1;i++){
            for(j=x;j<=j1;j++){
                num+=1;
                ave+=pixels[i][j];
            }
        }
        ave/=num;
        if(ave>peakAve) peakAve=ave;
        return peakAve;
    }
    public double getSurQuantile(int x, int y){
        m_cSurShape.setCenter(new Point(x,y));
        return ImageShapeHandler.getQuantile(pixelsp, m_cSurShape, 0.1);
    }
    public void buildIPOGContourShape(){

    }
}
