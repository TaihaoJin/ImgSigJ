/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FluoObjects;
import ij.ImagePlus;
import java.awt.Point;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.CustomDataTypes.intRange;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.statistics.MeanSem1;

/**
 *
 * @author Taihao
 */
public class IPOGExtention extends IPOGaussianNode{
    public static final int lenExtention=5;
    public Point center;
    public Point getCenter(){
        return center;
    }
    public IPOGExtention(){
        super();
    }
    public IPOGExtention(int slice,Point pt,int[][] pixels, int[][] pixelsCompensated){
        super();
        sliceIndex=slice;
        center=new Point(pt);
        calParameters(pixels,pixelsCompensated);
    }
    public IPOGExtention(IPOGaussianNode IPOG){
        super();
        copy(IPOG);
        center=super.getCenter();
    }
    public void calParameters(int[][] pixels, int[][] pixelsCompensated){
        int num,j,R=4,h=pixels.length,w=pixels[0].length;
        int[][] pixelst=new int[h][w];
        double sig,bkg,dTotalSignal;
        ImageShape cISbkg=new CircleImage(25),circle1=new CircleImage(1),circle3=new CircleImage(3);
        ImageShape cISsig,cIS=new CircleImage(R);

        cISbkg.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle1.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle3.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

//        ImageShapeHandler.filtPixels_Mean(pixelsCompensated, pixelst, circle1);

        Point pt;
        int area;
        double pixel,peak1,peak3;
        
        circle1.setCenter(center);
        circle3.setCenter(center);

        cISsig=new CircleImage(R);
        cISsig.setCenter(center);
        cISsig.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        ArrayList<Point> points=new ArrayList();

        bkg=ImageShapeHandler.getMean(pixelsCompensated, cISbkg);

        Amp=ImageShapeHandler.getMean(pixels, circle1)-bkg;

        circle3.getInnerPoints(points);
        num=points.size();
        sig=0;
        area=0;
        for(j=0;j<num;j++){
            pt=points.get(j);
            pixel=pixels[pt.y][pt.x];
            sig+=pixel;
            area++;
        }

        dTotalSignal=sig-bkg*area;

        setTotalSignal(dTotalSignal);
        setBackground((int)(bkg+0.5));
        setArea(area);
        peak1=ImageShapeHandler.getMean(pixels, circle1);
        peak3=ImageShapeHandler.getMean(pixels, circle3);
        setPeak1(peak1);
        setPeak3(peak3);
     }
}
