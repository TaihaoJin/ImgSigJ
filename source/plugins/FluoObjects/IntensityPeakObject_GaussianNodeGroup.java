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
import FluoObjects.IPOGaussianNode;
import utilities.CustomDataTypes.intRange;
import utilities.CommonStatisticsMethods;
import utilities.CommonMethods;
import ImageAnalysis.ContourFollower;
import utilities.Geometry.ImageShapes.*;

/**
 *
 * @author Taihao
 */
public class IntensityPeakObject_GaussianNodeGroup extends IntensityPeakObject{
    //an intensity peak object is defined on an image on xy plane, but with z and t coordinate.
//    ArrayList<IPOGaussianNode> IPOGs;
    IPOGaussianNodeComplex IPOGNode;
    public double base;

    public IntensityPeakObject_GaussianNodeGroup(){
        super();
//        IPOGs=new ArrayList();
//        IPOGNode=null;
    }
    public IntensityPeakObject_GaussianNodeGroup(ArrayList<IPOGaussianNode> IPOGs, int r, int[][]pixels0, int w, int h, int index, int trackingLength){
        this();
        updateGaussianNodeGroup(IPOGs,r,pixels0,w,h,index,trackingLength);
    }
    public void updateGaussianNodeGroup(ArrayList<IPOGaussianNode> IPOGGs, int r, int[][]pixels0, int w, int h, int index, int trackingLength){
        IPOGNode=new IPOGaussianNodeComplex(IPOGGs);
        int i,j,ix;
        ix=0;
        IPOGaussianNode IPOG;

        pixels=pixels0;
        cx=IPOGNode.xcr;
        cy=IPOGNode.ycr;
        cz=IPOGNode.getZ();
        this.w=w;
        this.h=h;
        this.t=t;
        radius=r;
        int xi=cx-r,xf=cx+r,yi=cy-r,yf=cy+r;
        if(xi<0) xi=0;
        if(yi<0) yi=0;
        if(xf>w-1) xf=w-1;
        if(yf>h-1) yf=h-1;
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
        pixelHeight=0;

        pixelHeight=IPOGNode.dTotalSignal;//old definition of pixelHeigth.

        this.trackingLength=trackingLength;
        preIPO=null;
        postIPO=null;
        preIPOs=null;
        postIPOs=null;
        IPOsWithinContour=new ArrayList<IntensityPeakObject>();
    }
    public double getPixelHeight0(){
        return pixelHeight0;
    }
    public void setBackgroundPixel(){
        backgroundPixel=0;
        int i,len=IPOGNode.IPOGs.size();
        for(i=0;i<len;i++){
            backgroundPixel+=IPOGNode.IPOGs.get(i).cnst;
        }
        backgroundPixel/=(double)len;
    }
    public double dist(IntensityPeakObject_GaussianNodeGroup ipo){
        double dx=(IPOGNode.xcr-ipo.IPOGNode.xcr);
        double dy=(IPOGNode.ycr-ipo.IPOGNode.ycr);
        double dist=Math.sqrt(dx*dx+dy*dy);
        return dist;
    }
    public double getDistToNeighbor(){
       double dist=100;
       IntensityPeakObject_GaussianNodeGroup IPOG;
       if(currentIPOs==null) return dist;
       if(currentIPOs.size()>0){
            IPOG=(IntensityPeakObject_GaussianNodeGroup)currentIPOs.get(0);
            dist=dist(IPOG);
       }
       return dist;
    }
    public int getArea(){
        return area;
    }
    public void setArea(int area){
        this.area=area;
        IPOGNode.setArea(area);
    }
    public double getAmpAt(double x,double y){
        double dv=0;
        int i,len=IPOGNode.IPOGs.size();
        for(i=0;i<len;i++){
            dv+=IPOGNode.IPOGs.get(i).getAmpAt(x, y);
        }
        return dv;
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
        return IPOGNode;
    }
    public void updateOvlpForIPOGs(){
        int i,len=IPOGNode.IPOGs.size();
        for(i=0;i<len;i++){
            IPOGNode.IPOGs.get(i).setOverlappingInfo(preOvlp, preRid, postOvlp, postRid);
            if(cContourShape==null) IPOGNode.setArea(getRegionArea());
        }
    }
    public void buildIPOGContourShapes(int[][] pixelsCompensated, int[][] pnScratch, int cutoff){
        int h=pixelsCompensated.length,w=pixelsCompensated[0].length;
        intRange xRange3=new intRange(), yRange3=new intRange();
        IPOGaussianNodeHandler.getRanges3(IPOGNode.IPOGs, xRange, yRange);
        xRange.setCommonRange(new intRange(0,w-1));
        yRange.setCommonRange(new intRange(0,h-1));
        CommonStatisticsMethods.copyArray(pixelsCompensated, pnScratch, yRange.getMin(),yRange.getMax(),xRange.getMin(),xRange.getMax());
        IPOGaussianNodeHandler.getSuperImposition(pnScratch, IPOGNode.IPOGs);
        if(pnScratch[cy][cx]<cutoff){
            cContourShape=new CircleImage(3);
            cContourShape.setCenter(new Point(cx,cy));
            cContourShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        }else{
            ArrayList<Point> contour=ContourFollower.getContour_Out(pnScratch, w, h, new Point(cx,cy), cutoff, Integer.MAX_VALUE,true);
            cContourShape=ImageShapeHandler.buildImageShape(contour);
        }
    }
    public void setBundleTotalSignal(double signal){
        dTotalBundleSignal=signal;
        IPOGNode.setBundleTotalSignal(signal);
    }
    public void setTotalSignal(double signal){
        dTotalSignal=signal;
        IPOGNode.setTotalSignal(signal);
    }
    public void setBackground(double bkg){
        IPOGNode.setBackground(bkg);
    }
    public void setPixelHeight(double height){
        pixelHeight=height;
    }
    public void setPeak1(double pixel){
        peak1=pixel;
        IPOGNode.setPeak1(pixel);
    }
    public void setPeak3(double pixel){
        peak3=pixel;
        IPOGNode.setPeak3(pixel);
    }
}
