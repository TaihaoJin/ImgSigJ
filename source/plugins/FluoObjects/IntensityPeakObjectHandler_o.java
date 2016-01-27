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

/**
 *
 * @author Taihao
 */
public class IntensityPeakObjectHandler_o {

    class IntPair {
            public int x;
            public int y;
            public IntPair(){
                this.x=0;
                this.y=0;
            }
            public IntPair(int x, int y){
                this();
                this.x=x;
                this.y=y;
            }
            public void setXY(int x,int y){
                this.x=x;
                this.y=y;
            }
            public void setXY(IntPair ip){
                this.x=ip.x;
                this.y=ip.y;
            }
            public IntPair(IntPair ip){
                this(ip.x,ip.y);
            }
            public boolean equals(IntPair ip){
                if(ip.x==x&&ip.y==y) return true;
                return false;
            }
    }
    int z, t, r,w,h,trackingLength;
    intRange intensityRange;
    int pixels[][],pixelsr[][],pixelStamp[][];
    int pixelsp[][],pixelspr[][];
    ArrayList <IntensityPeakObject> IPOs= new ArrayList<IntensityPeakObject>();
    ArrayList <Integer> backgroundIPOs;
    ArrayList <Integer> generalIPOs;
    ArrayList <Roi> ROIs;
    IntArray[][] ipoGrids;
    public static final int gridSize=20;
    int xGrid,yGrid;
    int minPeakIntensity;
    double minPeakPercentile;//
    double pCutoff;
    double intensityCutoff;//The cutoff will be decided based on the minPeakPercentile among all pixels contained in the background ROIs.
    double peakSeparationCutoff;
    ContourFollower cfollower;
    IntensityPeakObject commonIPO;
    double backgroundPercentiles[];
    int backgroundIntensities[];
    int refPixels[];
    boolean m_bBasedOnROIs;
//    public IntensityPeakObjectHandler(int z, int t, int r, int pixels[][], int refPixels[], int w, int h, intRange isr, ArrayList <Roi> ROIs, double pCutoff,int trackingLength, double PSCutoff, double minPeakPercentile, boolean bBasedOnROIs,ArrayList <IntArray> XROIPoints, ArrayList<IntArray> YROIPoints){
    public IntensityPeakObjectHandler_o(int z, int t, int r, int pixels[][], int pixelsr[][], int[][] pixelsp, int[][] pixelspr, int w, int h, intRange isr, ArrayList <Roi> ROIs, double pCutoff,int trackingLength, double PSCutoff, double minPeakPercentile, boolean bBasedOnROIs,ArrayList <IntArray> XROIPoints, ArrayList<IntArray> YROIPoints){
        this.z=z;
        this.t=t;
        this.r=r;
        this.w=w;
        this.h=h;
        this.pixels=pixels;
        this.pCutoff=pCutoff;
        this.trackingLength=trackingLength;
        this.peakSeparationCutoff=PSCutoff;
        this.minPeakPercentile=minPeakPercentile;
        backgroundIPOs=new ArrayList <Integer>();
        generalIPOs=new ArrayList <Integer>();
        intensityRange=new intRange(isr);
        cfollower=new ContourFollower(pixels);
        commonIPO=new IntensityPeakObject();
        IPOs.clear();
        this.ROIs=ROIs;
        m_bBasedOnROIs=bBasedOnROIs;
        int i,j;
        this.pixels=new int[h][w];
//        this.pixelsr=new int[h][w];
//        this.pixelsp=new int[h][w];
//        this.pixelspr=new int[h][w];
        this.pixelsr=pixelsr;
        this.pixelsp=pixelsp;
        this.pixelspr=pixelspr;//a IPOH keeps (and uses later) only the pixel values of the original image.
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                this.pixels[i][j]=pixels[i][j];
//                this.pixelsr[i][j]=pixelsr[i][j];
//                this.pixelsp[i][j]=pixels[i][j];
//                this.pixelspr[i][j]=pixelsr[i][j];
            }
        }

        if(bBasedOnROIs)buildIPObjects_withinROIs(ROIs,XROIPoints, YROIPoints);
        buildIPObjects();
        separateBackground();
        initIPOGrids();
        registerIPOs();
    }
    public void getClosestIPOs(int dist, int nx, IntensityPeakObject IPO0,ArrayList<IntensityPeakObject> nIPOs0, ArrayList <Double> dr){
        int x0=IPO0.cx;
        int y0=IPO0.cy;
        getClosestIPOs(dist,nx,x0,y0,IPO0.getPixelHeight(),nIPOs0,dr);
    }
    public void getClosestIPOs(int dist, int nx, int cx, int cy, double dPixelHeight, ArrayList<IntensityPeakObject> nIPOs0, ArrayList <Double> dr){
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

    public void getClosestGeneralIPOs(int dist, int nx, IntensityPeakObject IPO0,ArrayList<IntensityPeakObject> nIPOs0, ArrayList <Double> dr0){
        ArrayList<IntensityPeakObject> nIPOs=new ArrayList<IntensityPeakObject>();
        ArrayList<Double> dr=new ArrayList<Double>();
        int x0=IPO0.cx;
        int y0=IPO0.cy;
        getClosestIPOs(dist,nx,x0,y0,IPO0.getPixelHeight(),nIPOs,dr);
        int size=nIPOs.size();
        IntensityPeakObject IPO;
        for(int i=0;i<size;i++){
            IPO=nIPOs.get(i);
            if(IPO.background) continue;
            nIPOs0.add(IPO);
            dr0.add(dr.get(i));
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
        CommonMethods.getPixelHeights_ave(pixelspr,w,h,r,localMaxima,pixelHeights);
        ArrayList<Integer> indexes=new ArrayList();
        int len=localMaxima.size();
        for(int it=0;it<len;it++){
            indexes.add(it);
        }
        QuickSort.quicksort(pixelHeights,indexes);
        backgroundPercentiles=new double[16];
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
        backgroundIntensities=new int[16];
        double per;
        int size=pixelHeights.size();
        int index=(int)((1-minPeakPercentile)*size);
        minPeakIntensity =pixelHeights.get(index).intValue();
        for(int i=0;i<16;i++){
            per=backgroundPercentiles[i];
            index=(int)((1-per)*size);
            backgroundIntensities[i]=pixelHeights.get(index).intValue();
        }
    }
    void setMinPeakIntensity10(){
        ArrayList<Integer> backgroundPixels=new ArrayList<Integer>();
//        CommonMethods.collectROIs_Sorted(pixels, w, h, ROIs, backgroundPixels);
        int i,j,o;
        int[] pixelsc=new int[w*h];
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixelsc[o+j]=pixels[i][j];
            }
        }
        CommonMethods.randomize(pixelsc);
        CommonMethods.meanFiltering(w, h, pixelsc, 3);
        QuickSortInteger.quicksort(pixelsc);
        int size=w*h;
        backgroundPercentiles=new double[16];
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
        backgroundIntensities=new int[16];
        double per;
        int index=(int)((1-minPeakPercentile)*size);
        minPeakIntensity=pixelsc[index];
//        minPeakIntensity+=10;
        for(i=0;i<16;i++){
            per=backgroundPercentiles[i];
            index=(int)((1-per)*size);
            backgroundIntensities[i]=pixelsc[index];
        }
    }
    void setMinPeakIntensity0(){
        ArrayList<Integer> backgroundPixels=new ArrayList<Integer>();
        CommonMethods.collectROIs_Sorted(pixels, w, h, ROIs, backgroundPixels);
        int size=backgroundPixels.size();
        backgroundPercentiles=new double[16];
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
        backgroundIntensities=new int[16];
        double per;
        int index=(int)((1-minPeakPercentile)*size);
        minPeakIntensity=backgroundPixels.get(index);
//        minPeakIntensity+=10;
        for(int i=0;i<16;i++){
            per=backgroundPercentiles[i];
            index=(int)((1-per)*size);
            backgroundIntensities[i]=backgroundPixels.get(index);
        }
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
        CommonMethods.getPixelHeights_ave(pixelsp, w, h, r, localMaxima, pixelHeights);
        int x,y,i,j,pixel;
        int len=localMaxima.size();
        Point p;
        for(i=0;i<len;i++){
            p=localMaxima.get(i);
            x=p.x;
            y=p.y;
            pixel=pixelHeights.get(i).intValue();
            if(pixel>minPeakIntensity){
                IntensityPeakObject IPO=new IntensityPeakObject(x,y,z,r,t,pixels,w,h,IPOs.size(),trackingLength);
                IPO.setPixelHeight(pixel);
                IPO.setPeakPixel(pixels[y][x]);
                IPOs.add(IPO);
            }
        }
//        setContours();
//        resolveMultiplePeaks();
    }
    void buildIPObjects0(){
        setMinPeakIntensity();
        boolean lp=true;
        int x,y,i,j,p,p1;
        int[][] stamp=new int[h][w];
        LandscapeAnalyzer.stampPixels(w, h, pixels, stamp);
        for(y=1;y<h-1;y++){
            for(x=1;x<w-1;x++){
                p=pixels[y][x];
                if(stamp[y][x]==LandscapeAnalyzer.localMaximum&&intensityRange.contains(p)&&p>minPeakIntensity){
                    if(m_bBasedOnROIs){
                        if(!CommonMethods.withinROIs(x, y, ROIs)) continue;
                    }
                    IntensityPeakObject IPO=new IntensityPeakObject(x,y,z,r,t,pixels,w,h,IPOs.size(),trackingLength);
                    IPOs.add(IPO);
                }
            }
        }
//        setContours();
//        resolveMultiplePeaks();
    }
    void buildIPObjects_withinROIs(ArrayList <Roi> ROIs,ArrayList<IntArray>XPoints,ArrayList<IntArray> YPoints){
        //XPoints and YPoints are the positions of all pixels in ROI
        setMinPeakIntensity();
        boolean lp=true;
        int x,y,i,j,p,p1;
        int xi=r,xf=w-r-1;
        int yi=r,yf=h-r-1;
        int xt,yt;
        int size=0;
        int nSize=ROIs.size();
        Roi ROI;
        Rectangle border;
        Integer X=new Integer(0);
        Integer Y=new Integer(0);
        int []xy=new int[2];
        for(i=0;i<nSize;i++){
             ROI=ROIs.get(i);
/*            border=ROI.getBoundingRect();
           X=border.x;
            Y=border.y;
            height=border.height;
            width=border.width;
            int[][] stamp=new int[height][width];
            int [][] pixels0=new int[height][width];
            CommonMethods.setPixels(pixels,pixels0, X, width, Y, height);
            LandscapeAnalyzer.stampPixels(width, height, pixels0, stamp);
            for(x=X;x<X+width;x++){
                for(y=Y;y<Y+height;y++){
                    if(stamp[y-Y][x-X]==LandscapeAnalyzer.localMaximum&&ROI.contains(x, y)){
                        IntensityPeakObject IPO=new IntensityPeakObject(x,y,z,r,t,pixels,w,h,IPOs.size(),trackingLength);
                        IPOs.add(IPO);
                    }
                }
            }
        }*/
//        setContours();
//        resolveMultiplePeaks();
            CommonMethods.getMaxPixel(pixels, ROI,XPoints.get(i),YPoints.get(i), xy);
            IntensityPeakObject IPO=new IntensityPeakObject(xy[0],xy[1],z,r,t,pixels,w,h,IPOs.size(),trackingLength);
        }
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
    Point closestPointToCenter(ArrayList<Point> PTs){
       Point cp,pt;
       int x=0,y=0,size=PTs.size(),dx,dy;
       int i,in=0,px=-1,ix=0;
       for(i=0;i<size;i++){
           pt=PTs.get(i);
           x+=pt.x;
           y+=pt.y;
       }
       x/=size;
       y/=size;
       double dn=999999.,dist;
       for(i=0;i<size;i++){
           pt=PTs.get(i);
           dx=x-pt.x;
           dy=y-pt.y;
           dist=Math.sqrt(dx*dx+dy*dy);
           if(dist<dn){
               dn=dist;
               in=i;
           }
       }
       cp=PTs.get(in);
       return cp;
    }
    int separateBackground(){
        ArrayList <IntensityPeakObject> nIPOs=new ArrayList <IntensityPeakObject>();
        IntensityPeakObject IPO;
        backgroundIPOs.clear();
        generalIPOs.clear();
        int size=IPOs.size();
        int i,index;
        for(i=0;i<size;i++){
            IPO=IPOs.get(i);
            if(m_bBasedOnROIs){
                if(background(IPO)){
                    backgroundIPOs.add(i);
                    IPO.background=true;
                }else{
                    generalIPOs.add(i);
                    IPO.background=false;
                }
            }else{
                generalIPOs.add(i);
                IPO.background=false;
            }
        }
        size=backgroundIPOs.size();
        if(size>1){
            ArrayList<Double> aves=new ArrayList<Double>();
            for(i=0;i<size;i++){
                index=backgroundIPOs.get(i);
                aves.add(IPOs.get(index).ave);
            }

            QuickSort.quicksort(aves, backgroundIPOs);
            index=(int)((1.-pCutoff)*(size-1));
            intensityCutoff=aves.get(index);
            ArrayList<Integer> indexes=new ArrayList<Integer> ();

            for(i=0;i<size;i++){
                index=backgroundIPOs.get(i);
                IPO=IPOs.get(index);
//                IPO.index=i;
                nIPOs.add(IPO);
                indexes.add(i);
            }
            backgroundIPOs.clear();
            ArrayList<Integer> indexes2=backgroundIPOs;
            backgroundIPOs=indexes;
            int num=size;
            size=generalIPOs.size();
            for(i=0;i<size;i++){
                index=generalIPOs.get(i);
                IPO=IPOs.get(index);
                if(IPO.ave>=intensityCutoff){
//                    IPO.index=num;
                    nIPOs.add(IPO);
                    indexes2.add(num);
                    num++;
                }
            }
            generalIPOs.clear();
            generalIPOs=indexes2;
            IPOs.clear();
            IPOs=nIPOs;
        }else{
            intensityCutoff=0;
        }
        return 0;
    }
    boolean background(IntensityPeakObject IPO){
        int size=ROIs.size();
        for(int i=0;i<size;i++){
            if(ROIs.get(i).contains(IPO.getCX(), IPO.getCY())) return true;
        }
        return false;
    }
    public ArrayList <IntensityPeakObject> getIPOs(){
        return IPOs;
    }
    public IntensityPeakObject getIPO(int index){
        return IPOs.get(index);
    }
    public ArrayList <Integer> getBackgroundIPOs(){
        return backgroundIPOs;
    }
    public ArrayList <Integer> getGeneralIPOs(){
        return generalIPOs;
    }
    public double getCutoff(){
        return intensityCutoff;
    }

    int profileMinimum(int x1,int y1,int x2, int y2){
        ArrayList <Integer> profile=lineProfile(x1,y1,x2,y2);
        int size=profile.size();
        int pn=256,p;
        for(int i=0;i<size;i++){
            p=profile.get(i);
            if(p<pn) pn=p;
        }
        return pn;
    }

    ArrayList<Integer> lineProfile(int x1,int y1,int x2, int y2){
        ArrayList<Integer> profile=new ArrayList<Integer>();
        ArrayList <IntPair> ipa=new ArrayList<IntPair>();
        buildConnection(new IntPair(x1,y1),new IntPair(x2,y2),ipa);
        profile.add(pixels[y1][x1]);
        int size=ipa.size();
        IntPair ip;
        for(int i=0;i<size;i++){
            ip=ipa.get(i);
            profile.add(pixels[ip.y][ip.x]);
        }
        profile.add(pixels[y2][x2]);
        return profile;
    }
    void buildConnection(IntPair ip1, IntPair ip2, ArrayList <IntPair> ipa){
        int x1=ip1.x,x2=ip2.x,y1=ip1.y, y2=ip2.y;
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
                yi=yf;
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
    boolean separatePeaks(int x1, int y1, int x2, int y2){
        //this method chects the pixels values along the pixel line between (x1,y1) and (x2,y2).
        //If the ratio between the lowest and the highest values larger then the threshold, the the method will return ture.
        if(Math.abs(x1-x2)>15||Math.abs(y1-y2)>15) return true;
        int px=pixels[y1][x1];
        int pn=pixels[y2][x2];
        int pmin=256,p;
        int it;
        if(pn>px){
            pn=px;
            px=pixels[y2][x2];
            it=x1;
            x1=x2;
            x2=it;
            it=y1;
            y1=y2;
            y2=it;
        }
        if((double)pn/px<0.8) return true;
        int cutoff=(int) (peakSeparationCutoff*Math.min(pixels[y1][x1], pixels[y2][x2]));
        int x=x1,y;
        int delta;
        if(x1==x2){
            delta=1;
            if(y2<y1) delta=-1;
            y=y1+delta;
            while(delta*(y2-y)>=0){
//                ipa.add(new IntPair(x1,y));
                p=pixels[y][x1];
                if(p<cutoff) return true;
                if(((double)pmin/(double)Math.min(px, p))<peakSeparationCutoff) return true;
                if(p<pmin)pmin=p;
                y+=delta;
            }
        }else if(y1==y2){
            delta=1;
            if(x2<x1) delta=-1;
            x=x1+delta;
            while(delta*(x2-x)>=0){
//                ipa.add(new IntPair(x,y1));
                p=pixels[y1][x];
                if(p<cutoff) return true;
                if(((double)pmin/(double)Math.min(px, p))<peakSeparationCutoff) return true;
                if(p<pmin)pmin=p;
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
//                    ipa.add(new IntPair(x,y));
                    p=pixels[y][x];
                    if(p<cutoff) return true;
                    if(((double)pmin/(double)Math.min(px, p))<peakSeparationCutoff) return true;
                    if(p<pmin)pmin=p;
                    y+=ySign;
                }
                x+=xSign;
                yi=yf;
                y=yi;
                xd=x;
                yd=y1+k*(xd-x1);
                yf=(int)yd;
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line only includes pixels that are traversed
                while(ySign*(yf-y)>=0){
//                    ipa.add(new IntPair(x,y));
                    p=pixels[y][x];
                    if(p<cutoff) return true;
                    if(((double)pmin/(double)Math.min(px, p))<peakSeparationCutoff) return true;
                    if(p<pmin)pmin=p;
                    y+=ySign;
                }
            }
        }
        return false;
    }
    boolean separatePeaks0(int x1,int y1,int x2,int y2){
        double ratio=(double)profileMinimum(x1,y1,x2,y2)/Math.min(pixels[y1][x1],pixels[y2][x2]);
        return(ratio<peakSeparationCutoff);
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
    IntensityPeakObject mergeIPOso(ArrayList<IntensityPeakObject> IPOs, int index){
       IntensityPeakObject IPO;
       int x=0,y=0,size=IPOs.size();
       int i,in=0,px=-1,ix=0;
       for(i=0;i<size;i++){
           IPO=IPOs.get(i);
           x+=IPO.cx;
           y+=IPO.cy;
       }
       x/=size;
       y/=size;
       double dn=999999.,dist;
       for(i=0;i<size;i++){
           IPO=IPOs.get(i);
//           IPO.index=index;
           dist=IPO.dist(x,y);
           if(dist<dn){
               dn=dist;
               in=i;
           }
       }
       IPO=IPOs.get(in);
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
        /*
        for(i=0;i<size;i++){
            index=size-1-i;
            IPO=IPOs.get(index);
            if(IPO.connected) IPOs.remove(index);
        }*/
    }
    /*
    void resolveMultiplePeaks1(){
        if(z==30){
            z=z;
        }
        int i,j,size=IPOs.size(),size1,index;
        IntensityPeakObject IPO,IPO1;
        index=0;
        size=IPOs.size();
        ArrayList<IntensityPeakObject> nIPOs=new  ArrayList<IntensityPeakObject>();
        IntArray2 IPOIndexes=new IntArray2();
        for(i=0;i<size;i++){
            IPO=IPOs.get(i);
            index=IPO.index;
            size1=IPOIndexes.m_IntArray2.size();
            if(index>=size1){
                index=size1;
                IPO.index=index;
                IntArray ir=new IntArray();
                ir.m_intArray.add(i);
                IPOIndexes.m_IntArray2.add(ir);
            }
            for(j=i+1;j<size;j++){
                IPO1=IPOs.get(j);
                if(IPO1.index<j) continue;
                if(!separatePeaks(IPO.cx,IPO.cy,IPO1.cx,IPO1.cy)){
                    IPO1.index=index;
                    IPOIndexes.m_IntArray2.get(index).m_intArray.add(j);
                }
            }
        }
        size=IPOIndexes.m_IntArray2.size();
        ArrayList<IntensityPeakObject> IPOst=new  ArrayList<IntensityPeakObject>();
        IntArray ir;
        for(i=0;i<size;i++){
            IPOst.clear();
            ir=IPOIndexes.m_IntArray2.get(i);
            size1=ir.m_intArray.size();
            for(j=0;j<size1;j++){
                index=ir.m_intArray.get(j);
                IPO=IPOs.get(index);
                IPOst.add(IPO);
            }
            IPO=mergeIPOs(IPOst,i);
            nIPOs.add(IPO);
        }
        IPOs.clear();
        IPOs=nIPOs;
    }*/
    /*
    void resolveMultiplePeaks0(){
        if(z==30){
            z=z;
        }
        int i,j,k,size=IPOs.size(),size1,size2,jI,index;
        ArrayList<IntensityPeakObject> nIPOs=new  ArrayList<IntensityPeakObject>();
        ArrayList<IntensityPeakObject> IPOst=new  ArrayList<IntensityPeakObject>();
        ArrayList<IntensityPeakObject> IPOs0=new  ArrayList<IntensityPeakObject>();
        ArrayList<IntensityPeakObject> IPOs1=new  ArrayList<IntensityPeakObject>();
        IntensityPeakObject IPO,IPO1;
        int newIPOs=0;
        IPOs1=IPOs;
        index=0;
        int newIPOs1=0;
        size=IPOs1.size();
        while(size>1){
            IPOs0.clear();
            IPOs0=IPOs1;
            IPOs1=new ArrayList<IntensityPeakObject>();

            IPO=IPOs0.get(0);
            IPOst.clear();
            IPOst.add(IPO);
            newIPOs=0;
            for(j=1;j<size;j++){
                IPO1=IPOs0.get(j);
                if(!separatePeaks(IPO.cx,IPO.cy,IPO1.cx,IPO1.cy)){
                    IPOst.add(IPO1);
                    newIPOs++;
                }else{
                    IPOs1.add(IPO1);
                }
            }
            while(newIPOs>0){
                size1=IPOst.size();
                jI=size1-newIPOs;
                newIPOs=0;
                for(j=jI;j<size1;j++){
                    IPOs0.clear();
                    IPOs0=IPOs1;
                    IPOs1=new ArrayList<IntensityPeakObject>();

                    IPO=IPOst.get(j);
                    newIPOs1=0;
                    size2=IPOs0.size();
                    for(k=0;k<size2;k++){
                        IPO1=IPOs0.get(k);
                        if(!separatePeaks(IPO.cx,IPO.cy,IPO1.cx,IPO1.cy)){
                            IPOst.add(IPO1);
                            newIPOs++;
                            newIPOs1++;
                        }else{
                            IPOs1.add(IPO1);
                        }
                    }
                }
//                IPOs0.clear();
//                IPOs0=IPOs1;
//                IPOs1=new ArrayList<IntensityPeakObject>();
            }
            IPO=mergeIPOs(IPOst,index);
            nIPOs.add(IPO);
            index++;
            size=IPOs1.size();
        }
        IPOs.clear();
        IPOs=nIPOs;
    }
     * */
    public void markIPOs(ImagePlus impl){
        Color c;
        int size=IPOs.size();
        for(int i=0;i<size;i++){
            c=CommonMethods.randomColor();
            impl.setColor(c);
            IPOs.get(i).markIPO(impl);
            IPOs.get(i).markContour(impl);
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
    public IntensityPeakObject pickIPO(Roi ROI){
        Rectangle br=ROI.getBounds();
        int xi=Math.max(0,br.x),xf=Math.min(w-1,xi+br.width);
        int yi=Math.max(0,br.y),yf=Math.min(h-1,yi+br.height);
        int x,y;
        int p,px=-1,xx=0,yx=0;
        for(y=yi;y<=yf;y++){
            for(x=xi;x<=xf;x++){
                if(!ROI.contains(x, y))continue;
                p=pixels[y][x];
                if(p>px&&isLocalMaximum(x,y)){
                    yx=y;
                    xx=x;
                    px=p;
                }
            }
        }
        IntensityPeakObject IPO=new IntensityPeakObject(xx,yx,z,r,t,pixels,w,h,IPOs.size(),trackingLength);;
        return IPO;
    }
    public boolean isLocalMaximum(int x, int y){
        int xi=x-1,xf=x+1,yi=y-1,yf=y+1,i,j;
        if(xi<0)xi=0;
        if(yi<0)yi=0;
        if(xf>=w-1)xf=w-1;
        if(yf>=h-1)yf=h-1;
        int p=pixels[y][x];
        for(i=yi;i<=yf;i++){
            for(j=xi;j<=xf;j++){
                if(pixels[i][j]>p)return false;
            }
        }
        return true;
    }
    public ArrayList<IntensityPeakObject> getIPOsInROI(Roi ROI){
        Rectangle br=ROI.getBounds();
        int cx=br.x+br.width/2,cy=br.y+br.height/2;
        int dist=Math.max(br.width, br.height);
        int nx=500;
        ArrayList<IntensityPeakObject> IPOst=new ArrayList<IntensityPeakObject>();
        ArrayList<IntensityPeakObject> IPOs=new ArrayList<IntensityPeakObject>();
        ArrayList<Double> dr=new ArrayList<Double>();
        getClosestIPOs(dist,nx,cx,cy,255,IPOst,dr);//255 is the place for the pixel height, edited on 2/27/2010
        int size=IPOst.size();
        int i;
        IntensityPeakObject IPO;
        for(i=0;i<size;i++){
            IPO=IPOst.get(i);
            if(ROI.contains(IPO.cx, IPO.cy)){
                IPOs.add(IPO);
            }
        }
        return IPOs;
    }
}
