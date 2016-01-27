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

/**
 *
 * @author Taihao
 */
public class IntensityPeakObject_GaussianNode extends IntensityPeakObject{
    //an intensity peak object is defined on an image on xy plane, but with z and t coordinate.
    IPOGaussianNode IPOGNode;

    public IntensityPeakObject_GaussianNode(){
        super();
        IPOGNode=null;
    }
    public IntensityPeakObject_GaussianNode(IPOGaussianNode IPOG, int r, int[][]pixels0, int w, int h, int index, int trackingLength){
        this();
        pixels=pixels0;
        cx=IPOG.getXc();
        cy=IPOG.getYc();
        cz=IPOG.getZ();
        this.w=w;
        this.h=h;
        this.t=t;
        radius=r;
        int xi=cx-r,xf=cx+r,yi=cy-r,yf=cy+r;
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
        IPOGNode=IPOG;
        pixelHeight=IPOG.dTotalSignal;
    }
    public double getPixelHeight0(){
        return pixelHeight0;
    }
    public double dist(IntensityPeakObject_GaussianNode ipo){
        double dx=(IPOGNode.xc-ipo.IPOGNode.xc);
        double dy=(IPOGNode.yc-ipo.IPOGNode.yc);
        double dist=Math.sqrt(dx*dx+dy*dy);
        return dist;
    }
    public double getDistToNeighbor(){
       double dist=100;
       IntensityPeakObject_GaussianNode IPOG;
       if(currentIPOs==null) return dist;
       if(currentIPOs.size()>0){
            IPOG=(IntensityPeakObject_GaussianNode)currentIPOs.get(0);
            dist=dist(IPOG);
       }
       return dist;
    }
    public int getArea(){
        if(enclosedShape==null) return 0;
        return enclosedShape.getArea();
    }
    public double getAmpAt(double x,double y){
        double[]X={x,y};
        double dv=IPOGNode.getAmpAt(x, y);
        return dv;
    }
    public double[] getPeakPosition(){
        double[] peak=IPOGNode.getPeakPosition();
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
        IPOGNode.setOverlappingInfo(preOvlp, preRid, postOvlp, postRid);
    }
    public void setBundleTotalSignal(double signal){
        dTotalBundleSignal=signal;
        IPOGNode.dBundleTotalSignal=signal;
    }
    public void setTotalSignal(double signal){
        dTotalSignal=signal;
        IPOGNode.dTotalSignal=signal;
    }
    public void setArea(int area){
        this.area=area;
        IPOGNode.area=area;
    }
    public void setBackground(double bkg){
        IPOGNode.dBackground=bkg;
    }
}
