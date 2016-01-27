/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import utilities.Geometry.ImageShapes.SubpixelAreaInRingsLUT;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Geometry.CocentricCircles_Rings;
import utilities.Geometry.ImageShapes.ImageShape;
import java.awt.Point;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
import utilities.Geometry.ImageShapes.ImageShapeHandler;

/**
 *
 * @author Taihao
 */
public class CocentricRingKernel {
    public static final int uniformWeight=0, gaussianWeight=1;
    CocentricCircles_Rings ring;
    SubpixelAreaInRingsLUT subpixelIS;
    int[][] pixels;
    double[] weights;
    int w,h,weightingOption;
    final double PI=Math.PI;
    double area,radius;
    ArrayList<Double> dvRs;
    Point[] innerPoints;
    public CocentricRingKernel(int[][] pixels, SubpixelAreaInRingsLUT subpixelIS, int weightingOption){
        this.subpixelIS=subpixelIS;
        this.pixels=pixels;
        w=pixels[0].length;
        h=pixels.length;
        subpixelIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        this.weightingOption=weightingOption;
        double R=subpixelIS.getMaxCCRingRadius();
        ring=subpixelIS.getCCRings();
        dvRs=ring.getRs();
        radius=1;
        calWeights();
    }
    public void updatePixels(int pixels[][]){
        this.pixels=pixels;
    }
    void calWeights(){
        double pi=Math.PI;
        double w,wA=0,a,r0,r;
        r0=0;
        int num=dvRs.size();
        int i,j;
        area=0;
        weights=new double[num];
        for(i=0;i<num;i++){
            r=dvRs.get(i);
            a=r*r*pi;
            a-=area;
            area+=a;
            w=weight(r);
            wA+=a*w;
            weights[i]=w;
        }
        for(i=0;i<num;i++){
            weights[i]/=wA;
        }
    }
    public  void setRadius(double r){
        radius=r;
        calWeights();
    }
    double weight(double r){
        double w;
        switch(weightingOption){
            case uniformWeight:
                w=1;
                break;
            case gaussianWeight:
                w=Math.exp((-r*r)/(2*radius*radius))/(radius*Math.sqrt(2*PI));
                break;
            default:
                w=1;
                break;
        }
        return w;
    }
    public double KernelMean(double x, double y, double dRef){
        subpixelIS.setCenter_Subpixel(x, y);
        subpixelIS.setEnclosingRectangleCenter(new Point((int)(x+0.5),(int)(y+0.5)));
        return ImageShapeHandler.getWeightedMean_SubpixelCCRings(pixels, subpixelIS, subpixelIS.getEnclosingRectangle(), weights, dRef);
    }
}
