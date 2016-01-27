/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import ij.ImagePlus;
import ImageAnalysis.MinimalGraphicalObject;
import java.awt.Point;
import utilities.CommonMethods;
import java.awt.Color;
import ImageAnalysis.ContourFollower;
import utilities.Geometry.ImageShapes.ImageShape;

/**
 *
 * @author Taihao
 */
public class IntensityPeakObject extends MinimalGraphicalObject{
    //an intensity peak object is defined on an image on xy plane, but with z and t coordinate.
    int cx,cy, cz, t;//the coordinates of the intenisty peak
    int w, h;
    int radius;
    int peakPixel,backgroundPixel;
    double Amp;
    double ave,pValue,dTotalSignal,dTotalBundleSignal,peak1,peak3;//peak1 and peak 3 are average pixel values of circles with radius 1 and 3 centered that the peak position
    double peakPixel_ave, sur_quantile;//peakPixel_ave is conceptually similar to pixelHeight0. Numerically it is
    //the highest value of the average intensity of the four squares (each containing four pixels) containing the peak point (cx,cy).
    int size;//number of pixels surrounding the intensity peak that has been taken into the calculation of ave.
    boolean background;
    boolean bTrackOpening;
    int trackingLength;
    boolean m_bBasedOnPrecomputedPixelHeight;
    boolean connected;// there exist one other intensity peak within the contour and this peak is also within the contour of the other peak.

    double pixelHeight,pixelHeight0,pixelHeightC,pixelHeightC90p,contourCutoff;
    //pixelHeight is the total signal (subtracted by backgound) over the area of the IPO.
    //pixelHeight0 is the pixel value at (cx,cy) (subtracted by backgound).
    //see the method refineIPOSelection() in class IntensityPeakObjectHander for
    //pixelHeightC,pixelHeightC90p and contourCutoff.
    int percentileIndex;
    ImageShape cIS,cContourShape;

    ArrayList<IntensityPeakObject> preIPOs;//IPOs in the preceding image
    ArrayList<IntensityPeakObject> IPOsWithinContour;
    ArrayList<IntensityPeakObject> postIPOs;//IPOs in the following image
    ArrayList<IntensityPeakObject> currentIPOs;//IPOs in the same image
    IntensityPeakObject preIPO;
    IntensityPeakObject postIPO;

//    int index;//the index of the list of IP objects in the same image.
    int TrackIndex;
    int indexTemp;//index temporary used to identify the position of the object in a arraylist.
    int localMaximumIndex;//the index to identify the intensity peak among local maxima of the image.
    int m_nBundleSize,m_nBundleIndex;
    double preOvlp,postOvlp;
    int preRid,postRid;
    int rIndex;
    IntensityPeakObject preOvlpIPO,postOvlpIPO;
    public IntensityPeakObject(){
        cx=0;
        cy=0;
        cz=0;
        this.t=0;
        radius=0;
        preIPO=null;
        postIPO=null;
        preIPOs=null;
        postIPOs=null;
        connected=false;
        TrackIndex=-1;
        bTrackOpening=false;
        m_bBasedOnPrecomputedPixelHeight=false;
        preOvlp=-1;
        postOvlp=-1;
        preRid=-1;
        postRid=-1;
        rIndex=-1;
        preOvlpIPO=null;
        postOvlpIPO=null;
    }
    public IntensityPeakObject(int x, int y, int z, int r, int t, int[][]pixels0, int w, int h, int index, int trackingLength){
        this();
        pixels=pixels0;
        cx=x;
        cy=y;
        cz=z;
        this.w=w;
        this.h=h;
        this.t=t;
        radius=r;
        int xi=x-r,xf=x+r,yi=y-r,yf=y+r;
        if(xi<0) xi=0;
        if(yi<0) yi=0;
        if(xf>w-1) xf=w-1;
        if(yf>h-1) yf=h-1;
        int i,j;
        ave=0.;
        size=0;
        for(i=yi;i<=yf;i++){
            for(j=xi;j<=xf;j++){
                size++;
                ave+=pixels[i][j];
            }
        }
        ave/=size;
        background=false;
//        this.index=index;
        this.trackingLength=trackingLength;
        preIPO=null;
        postIPO=null;
        preIPOs=null;
        postIPOs=null;
        IPOsWithinContour=new ArrayList<IntensityPeakObject>();
    }
    boolean equals(IntensityPeakObject IPO){
        return(cx==IPO.cx&&cy==IPO.cy&&cz==IPO.cz&&t==IPO.t);
    }
    public void setPreIPOs(ArrayList<IntensityPeakObject> preIPOs){
        this.preIPOs=preIPOs;
    }
    public void setCurrentIPOs(ArrayList<IntensityPeakObject> currentIPOs){
        this.currentIPOs=currentIPOs;
    }
    public void setPostIPOs(ArrayList<IntensityPeakObject> postIPOs){
        this.postIPOs=postIPOs;
    }
    public int getCX(){
        return cx;
    }
    public int getCY(){
        return cy;
    }
    public int getCZ(){
        return cz;
    }
    public int getT(){
        return t;
    }
    public int getRadius(){
        return radius;
    }
    public int getSize(){
        return size;
    }
    public double getPixelHeight(){
        return pixelHeight;
    }
    public double getPixelHeight0(){
        return pixelHeight0;
    }
    public double dist(IntensityPeakObject ipo){
        double dx=(cx-ipo.cx);
        double dy=(cy-ipo.cy);
        double dist=Math.sqrt(dx*dx+dy*dy);
        return dist;
    }
    public double dist(int x, int y){
        double dx=(cx-x);
        double dy=(cy-y);
        double dist=Math.sqrt(dx*dx+dy*dy);
        return dist;
    }
    public void setPixelHeight(double dh){
        pixelHeight=dh;
    }
    public void setPixelHeight0(double dh){
        pixelHeight0=dh;
    }
    public void markIPO(ImagePlus impl){
        int w=impl.getWidth();
        int h=impl.getHeight();
        int xi=cx-radius;
        if(xi<0)xi=0;
        int xf=cx+radius;
        if(xf>w-1) xf=w-1;

        int yi=cy-radius;
        if(yi<0)yi=0;
        int yf=cy+radius;
        if(yf>h-1) yf=h-1;
//        Color c=Color.BLACK;
        impl.getProcessor().drawLine(xi, yi, xf, yi);
        impl.getProcessor().drawLine(xf, yi, xf, yf);
        impl.getProcessor().drawLine(xf, yf, xi, yf);
        impl.getProcessor().drawLine(xi, yf, xi, yi);
    }
    public void setPixelHeightC(double pixelHeightC){
        this.pixelHeightC=pixelHeightC;
    }
    public void setPixelHeightC90p(double pixelHeightC){
        this.pixelHeightC90p=pixelHeightC;
    }
    public void setContourCutoff(double cutoff){
        contourCutoff=cutoff;
    }
    public int markContour(ImagePlus impl){
        int size=contour.size();
        if(size==0) return -1;
        Point p0=contour.get(size-1),p;
        for(int i=0;i<size;i++){
            p=contour.get(i);
            impl.getProcessor().drawDot(p.x, p.y);
        }
        Point[] conners=enclosedShape.getConnerPoints();
        Point rb=conners[3],lb=conners[2],lt=conners[0],rt=conners[1];
        int b=255-percentileIndex*20;
        if(b<0) b=0;
        int pixel=(0<<16)|(0<<8)|(b);
        CommonMethods.drawDot(impl, rb, pixel);
        pixel=(int)contourCutoff;
        CommonMethods.drawDot(impl, lb, pixel);
        pixel=enclosedShape.getArea();
        CommonMethods.drawDot(impl, lt, pixel);
        pixel=(int)pixelHeightC;
        CommonMethods.drawDot(impl, rt, pixel);
        return 1;
    }
    public boolean matches(Point p){
        return contains(p);
    }
    public void setPeakPixel(int pixel){
        peakPixel=pixel;
    }
    public int getPixel(int x, int y){
        return pixels[y][x];
    }
    public int getPixelHeight(int x, int y){
        return CommonMethods.getPixelHeight(pixels, w, h, x, y, radius);
    }
    public void basedOnPrecomputedPixelHeight(){
        m_bBasedOnPrecomputedPixelHeight=true;
    }
    public int getTrackIndex(){
        return TrackIndex;
    }
    public void setTrackIndex(int index){
        TrackIndex=index;
    }
    public ImageShape getEnclosingShape(){
        return super.enclosedShape;
    }
    public void setBundleSize(int size){
        m_nBundleSize=size;
    }
    public int getBundleSize(){
        return m_nBundleSize;
    }
    public void setBackgroundPixel(int pixel){
        backgroundPixel=pixel;
    }
    public int getBackgroundPixel(){
        return backgroundPixel;
    }
    public double getDistToNeighbor(){
       double dist=100;
       IntensityPeakObject IPO;
       if(currentIPOs==null) return dist;
       if(currentIPOs.size()>0){
            IPO=currentIPOs.get(0);
            dist=dist(IPO);
       }
       return dist;
    }
    public int getArea(){
        if(enclosedShape==null) return 0;
        return enclosedShape.getArea();
    }
    public double getAmpAt(double x, double y){//this function is to overide in IPOGaussianNode. For this class, it will not be used.
        return peakPixel_ave;
    }
    public double[] getPeakPosition(){
        double[] peak={cx,cy};
        return peak;
    }
    public double getSignal(String name){
        if(name.contentEquals("Amp")) return peakPixel_ave;
        if(name.contentEquals("Sum")) return pixelHeight;
        return -1;
    }
    public IPOGaussianNode getIPOG(){
        return null;
    }
    public int getRegionArea(){
        if(cIS==null) return 0;
        return cIS.getArea();
    }
    public void updateOvlpForIPOGs(){
        //do nothing
    }
    public ImageShape getContourShape(){
        return cContourShape;
    }
    public void setBundleTotalSignal(double signal){
        dTotalBundleSignal=signal;
    }
    public void setTotalSignal(double signal){
        dTotalSignal=signal;
    }
    public void setArea(int area){
        this.area=area;
    }
    public void setBackground(double bkg){
//        dBackground=bkg;
    }
    public void setPeak1(double pixel){
        peak1=pixel;
    }
    public void setPeak3(double pixel){
        peak3=pixel;
    }
}
