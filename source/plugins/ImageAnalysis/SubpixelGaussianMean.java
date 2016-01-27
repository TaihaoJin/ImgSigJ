/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;
import FluoObjects.IPOGContourParameterNode;
import utilities.Geometry.ImageShapes.*;
import utilities.CustomDataTypes.DoubleRange;
import java.awt.Point;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.DoublePair;
/**
 *
 * @author Taihao
 */
public class SubpixelGaussianMean implements TwoDFunction{
    int w,h;
    int FunctionType;
    intRange xFRange, yFRange, xInnerRange, yInnerRange;
    CircleImage circle;
    ArrayList<Point> points;
    double R,sigma;
    int[][] pixels;
    Point p0;
    DoublePair Peak;//the heighest function value position within the range specified by xFRange and yFRange.
    DoubleRange cValueRange;
    Point Center;//this is the center of the tracked signal, may not be highest point (PeakPoint) or the geometric center (mid point of xRange and yRange). this is set by externally.
    //by IPOGTrackNode
    Point PixeledRegionCenter;
    double dCenterPeakValue;
    boolean bCentered;
   
    public SubpixelGaussianMean(int[][] pixels, double sigma, double R, intRange xFRange, intRange yFRange, int type){
        Peak=new DoublePair();
        points=new ArrayList();
        this.R=R;
        this.sigma=sigma;
        this.xFRange=new intRange(xFRange);
        this.yFRange=new intRange(yFRange);
        circle=new CircleImage(R);
        circle.setFrameRanges(xFRange, yFRange);
        this.pixels=CommonStatisticsMethods.copyArray(pixels);
        p0=new Point(0,0);
        circle.setCenter(p0);
        cValueRange=new DoubleRange();
        PixeledRegionCenter=new Point(xFRange.getMidpoint(),yFRange.getMidpoint());
        xInnerRange=new intRange(PixeledRegionCenter.x-2,PixeledRegionCenter.x+2);
        yInnerRange=new intRange(PixeledRegionCenter.y-2,PixeledRegionCenter.y+2);
        getValueRange(new DoublePair(0,0));
        FunctionType=type;
    } 
    boolean equal(Point p1, Point p2){
        return p1.x==p2.x&&p1.y==p2.y;
    }
    public double func(double x, double y){
        return getMean(x,y)-cValueRange.getMin();
    }
    double getMean(double x, double y){
        Point p=new Point((int)(x+0.5),(int)(y+0.5));
        if(!xFRange.contains(p.x)||!yFRange.contains(p.y)) return cValueRange.getMin()-50;
        p0.setLocation(p);
        circle.setCenter(p);
        circle.getInnerPoints(points);
        
        if(points.isEmpty()) return cValueRange.getMin()-50;
        double mean=0,dx,dy,z,w=0;
        
        for(int i=0;i<points.size();i++){
            p=points.get(i);
            dx=x-p.x;
            dy=y-p.y;
            z=Math.exp(-(dx*dx+dy*dy)/(2.*sigma*sigma));
            if(z>0.9){
                i=i;
            }
            w+=z;
            if(pixels[p.y][p.x]<1000){
                i=i;
            }
            mean+=z*pixels[p.y][p.x];
        }
        mean/=w;
        return mean;
    }
    public DoubleRange getValueRange(DoublePair SubpixelShift){//average function value of the points within a circle centered at Peak.
        DoubleRange dr=new DoubleRange();
        double dt,dMin=Double.POSITIVE_INFINITY;
        int x,y,xm=xFRange.getMidpoint(),ym=yFRange.getMidpoint();
        Peak.setValue(xm+SubpixelShift.left,ym+SubpixelShift.right);
        Point p=new Point();
        cValueRange.setRange(0, 0);
        dCenterPeakValue=Double.NEGATIVE_INFINITY;
        Ring ring=new Ring(6,8);
        for(y=yFRange.getMin();y<=yFRange.getMax();y++){
            for(x=xFRange.getMin();x<xFRange.getMax();x++){
                dt=func(x+SubpixelShift.left,y+SubpixelShift.right);
                if(dt>dr.getMax()){
                    Peak.setValue(x+SubpixelShift.left,y+SubpixelShift.right);
                    p.setLocation(x,y);
                }
                if(xInnerRange.contains(x)&&yInnerRange.contains(y)){
                    if(dt>dCenterPeakValue) dCenterPeakValue=dt;
                }
                if(dt<dMin) dMin=dt;
                dr.expandRange(dt);
            }
        }
        
        DoublePair dp=CommonMethods.subpixelWeightedCentroid(this, p.x, p.y);
        dt=func(dp.left,dp.right);
        if(dt>dr.getMax()){
            dr.setMax(dt);
            Peak.setValue(dp.left,dp.right);
        }
        
        if(xInnerRange.contains((int)(Peak.left+0.5))&&yInnerRange.contains((int)(Peak.right+0.5))) 
            bCentered=true;
        else
            bCentered=false;
        
        ring.setCenter(new Point((int)(Peak.left+0.5),(int)(Peak.right+0.5)));
        ring.setFrameRanges(xFRange, yFRange);
        ArrayList<Point> points=new ArrayList();
        ring.getInnerPoints(points);
        
        if(points.size()==0) 
            return new DoubleRange(dr);
        
        double  bkg=0;
        for(int i=0;i<points.size();i++){
            p=points.get(i);
            bkg+=func(p.x+SubpixelShift.left,p.y+SubpixelShift.right);
        }
        bkg/=points.size();
        cValueRange.setRange(bkg, dr.getMax());
        return dr;        
    }
    public DoublePair getPeak(){
        return Peak;
    }
    public DoubleRange getValueRange(){
        return cValueRange;
    }
    public void getFrameRanges(DoubleRange xFRange, DoubleRange yFRange){
        xFRange.setRange(this.xFRange.getMin(), this.xFRange.getMax());
        yFRange.setRange(this.yFRange.getMin(), this.yFRange.getMax());
    }
    public double getHeight(){
//        return getHeight(new DoublePair(xFRange.getMidpoint(),yFRange.getMidpoint()));
        return getHeight(new DoublePair(Center.x,Center.y));
//        return getHeight(Peak);
    }
    public double getHeight(DoublePair dp){
        CircleImage circle=new CircleImage(3);
        circle.setCenter(new Point((int)(dp.left+0.5),(int)(dp.right+0.5)));
        circle.setFrameRanges(xFRange, yFRange);
        ArrayList<Point> points=new ArrayList();
        circle.getInnerPoints(points);
        
        int x=(int) dp.left,y=(int) dp.right;
        DoublePair SubpixelShift=new DoublePair(dp.left-x,dp.right-y);
        
        double  h=0;
        Point p;
        for(int i=0;i<points.size();i++){
            p=points.get(i);
            h+=func(p.x+SubpixelShift.left,p.y+SubpixelShift.right);
        }
        h/=points.size();
        
        h=ImageShapeHandler.getMean(pixels, circle);
        
        Ring ring=new Ring(3,4);
        ring.setCenter(new Point((int)(dp.left+0.5),(int)(dp.right+0.5)));
        ring.setFrameRanges(xFRange, yFRange);
        ArrayList<Point> points1=new ArrayList();
        double bkg=ImageShapeHandler.getMean(pixels, ring);
        
        return h-bkg;
    }
    public int getTowDFunctionType(){
        return FunctionType;
    }
    public void setCenter(Point ct){
        Center=ct;
    }
    public int[][] getPixels(){
        int w=xFRange.getRange(),h=yFRange.getRange(),x0=xFRange.getMin(),y0=yFRange.getMin(),i,j;
        int[][] pixelst=new int[h][w];
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pixelst[i][j]=pixels[i+y0][j+x0];
            }
        }
        return pixelst;
    }
    public double getCenterPeakValue(){
        return dCenterPeakValue;
    }
    public boolean isCentered(){
        return bCentered;
    }
    public Point getSignalCenter(){
        return Center;
    }
}
