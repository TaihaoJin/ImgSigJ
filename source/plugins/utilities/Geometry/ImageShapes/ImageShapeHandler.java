/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry.ImageShapes;
import java.awt.Point;
import java.util.ArrayList;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.intRange;
import ij.ImagePlus;
import ij.IJ;
import utilities.statistics.MeanSem0;
import ij.WindowManager;
import utilities.CommonMethods;
import utilities.CommonGuiMethods;
import utilities.ArrayofArrays.IntRangeArray;
import utilities.ArrayofArrays.IntRangeArray2;
import utilities.ArrayofArrays.PointArray;
import utilities.ArrayofArrays.PointArray2;
import ImageAnalysis.ContourFollower;
import java.awt.Color;
import utilities.statistics.MeanSemFractional;
import utilities.statistics.MeanSem1;
import utilities.Non_LinearFitting.StandardFittingFunction;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import utilities.Non_LinearFitting.Constrains.*;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.QuickSort;
import utilities.QuickSortInteger;
import utilities.statistics.Histogram;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.Non_LinearFitting.FittingModelExpander;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import ij.gui.Roi;
import ij.gui.PolygonRoi;
import ImageAnalysis.ImageScanner;

/**
 *
 * @author Taihao
 */
public class ImageShapeHandler {
    public static ArrayList<Point> getOverlappingCenterPositions(ImageShape is1, ImageShape is2, ArrayList<Point> points2){//return the points amont points2 center at which is2 is overlapping with is1
        ArrayList<Point> points=new ArrayList();
        int len=points.size();
        Point p;
        for(int i=0;i<len;i++){
            p=points2.get(i);
            is2.setCenter(p);
            if(is1.overlapping(is2)) points.add(p);
        }
        return points;
    }
    public static ArrayList<Integer> getOverlappingCenterIndexes(ImageShape is1, ImageShape is2, ArrayList<Point> points2, ArrayList<Integer> indexes2){//return the points amont points2 center at which is2 is overlapping with is1
        ArrayList<Integer> indexes=new ArrayList();
        int len=indexes2.size();
        Point p;
        int index;
        for(int i=0;i<len;i++){
            index=indexes2.get(i);
            p=points2.get(index);
            is2.setCenter(p);
            if(is1.overlapping(is2)) indexes.add(index);
        }
        return indexes;
    }
    public static double getMean(int pnValues[][], ImageShape shape){
        double m=0;
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            m+=pnValues[pt.y][pt.x];
        }
        m/=len;
        return m;
    }
    public static double getMean(int pnValues[][], ImageShape shape, ImageShape Frame){
        double m=0;
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            if(!Frame.contains(pt)) continue;
            m+=pnValues[pt.y][pt.x];
        }
        m/=len;
        return m;
    }
    public static double getContourMean(int pnValues[][], ImageShape shape){
        double m=0;
        ArrayList<Point> points=shape.getOuterContour();
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            m+=pnValues[pt.y][pt.x];
        }
        m/=len;
        return m;
    }
    
    public static intRange getRange(int pnValues[][], ImageShape shape){
        double m=0;
        intRange ir=new intRange();
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            ir.expandRange(pnValues[pt.y][pt.x]);
        }
        return ir;
    }

    public static double getMean(int pnValues[][], ImageShape shape, int[][] pnStamp, int nExcludingType){
        double m=0;
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        double num=0;
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            if(pnStamp[pt.y][pt.x]==nExcludingType) continue;
            m+=pnValues[pt.y][pt.x];
            num++;
        }
        m/=num;
        return m;
    }
    public static int getQuantile(int pnValues[][], ImageShape shape, double q){
        int nQ;
        ArrayList<Point> points=new ArrayList();

        shape.getInnerPoints(points);
        int len=points.size();

        int[] pnVs=new int[len];
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            pnVs[i]=pnValues[pt.y][pt.x];
        }
        nQ=CommonStatisticsMethods.getQuantile(pnVs, q);
        return nQ;
    }
    public static void setValue(int[][] pnValues, ImageShape shape, int it){
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            pnValues[pt.y][pt.x]=it;
        }
    }
    public static void addValue(int[][] pnValues, ImageShape shape, int it){
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            pnValues[pt.y][pt.x]+=it;
        }
    }

    public static void addValue(int[][] pnValues, SubpixelAreaInRingsLUT subpixelIS, double[] values, double dSign){
        ArrayList<Point> points=new ArrayList();
        subpixelIS.getEnclosingRectangle().getInnerPoints(points);
        int len=points.size(),len1;
        Point pt;
        int i,j;
        double[] areaInRings;
        intRange ir;
        double value,area;
        for(i=0;i<len;i++){
            pt=points.get(i);
            areaInRings=subpixelIS.getAreaInRings(pt.x, pt.y);
            ir=subpixelIS.getRingRanges(pt.x, pt.y);
            if(ir==null){
                i=i;
            }
            value=0.;
            for(j=ir.getMin();j<=ir.getMax();j++){
                area=areaInRings[j];
                value+=values[j]*area;
            }
            pnValues[pt.y][pt.x]+=(int) (dSign*value);
        }
    }

    public static void addValue(int[][] pnValues, SubpixelAreaInRingsLUT subpixelIS, int[][] pnExcludingMap, int excludingLabel, double[] values, double dSign){
        ArrayList<Point> points=new ArrayList();
        subpixelIS.getEnclosingRectangle().getInnerPoints(points);
        int len=points.size(),len1;
        Point pt;
        int i,j;
        double[] areaInRings;
        intRange ir;
        double value,area;
        for(i=0;i<len;i++){
            pt=points.get(i);
            if(pnExcludingMap[pt.y][pt.x]==excludingLabel) continue;
            areaInRings=subpixelIS.getAreaInRings(pt.x, pt.y);
            ir=subpixelIS.getRingRanges(pt.x, pt.y);
            if(ir==null){
                i=i;
            }
            value=0.;
            for(j=ir.getMin();j<=ir.getMax();j++){
                area=areaInRings[j];
                value+=values[j]*area;
            }
            pnValues[pt.y][pt.x]+=(int) (dSign*value);
        }
    }

    public static void scaleElements(int[][] pnValues, ImageShape shape, int it){
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            pnValues[pt.y][pt.x]*=it;
        }
    }

    public static void copyElements(int[][] pnValues1, int[][] pnValues2, ImageShape shape){
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=points.get(i);
            pnValues2[pt.y][pt.x]=pnValues1[pt.y][pt.x];
        }
    }
    public static void copyElements(int[][] pnValues1, int[][] pnValues2, ArrayList<ImageShape> shapes, Point pt){
        int len=shapes.size();
        ImageShape shape;
        for(int i=0;i<len;i++){
            shape=shapes.get(i);
            shape.setCenter(pt);
            copyElements(pnValues1,pnValues2,shape);
        }
    }
    public static double getLeastRadiusDiff(double dRi, double dRo, ArrayList<Double> dvRs){
        double dMinDR=dRo;
        dvRs.clear();
        int x,y;
        int nXMax=(int)dRo,nXMin=(int)dRi;
        double r,dr,r0,r1,ri,rf;
        r0=0;
        for(x=nXMin;x<=nXMax;x++){
            for(y=0;y<=x;y++){
                r=Math.sqrt(x*x+y*y);
                if(r<dRi||r>dRo) continue;
                dvRs.add(r);
            }
        }
        utilities.QuickSort.quicksort(dvRs);
        int len=dvRs.size();

        int i;
        r0=dvRs.get(len-1);
        for(i=len-2;i>=0;i--){
            r=dvRs.get(i);
            if(r==r0){
                dvRs.remove(i+1);
            }else{
                dr=r0-r;
                if(dr<dMinDR) dMinDR=dr;
            }
            r0=r;
        }
        return dMinDR;
    }
    public static ArrayList<ImageShape> getMinimalRings(double dRi, double dRo, ArrayList<Double> dvRs){
        ArrayList<ImageShape> rings=new ArrayList();
        dvRs.clear();
        double r,ri,ro,dMinDR=getLeastRadiusDiff(dRi,dRo,dvRs);
        int i,len=dvRs.size();
        for(i=0;i<len;i++){//a ring with the first element will be an empty shape
            r=dvRs.get(i);
            ri=r-0.5*dMinDR;
            ro=r+0.5*dMinDR;
            rings.add(new Ring(ri,ro));
        }
        return rings;
    }
    public static void setFrameRanges(ArrayList<ImageShape> shapes, intRange xRange, intRange yRange){
        int len=shapes.size();
        for(int i=0;i<len;i++){
            shapes.get(i).setFrameRanges(xRange, yRange);
        }
    }
    public static void setImageCenter(ArrayList<ImageShape> shapes, Point pt){
        int len=shapes.size();
        for(int i=0;i<len;i++){
            shapes.get(i).setCenter(pt);
        }
    }
    public static void markShape(ImagePlus impl, ImageShape shape, Color c){
        markShape(impl,shape,c.getRGB());
    }

    public static void markShape(ImagePlus impl, ImageShape shape, int pixel){
        if(impl.getType()!=ImagePlus.COLOR_RGB) IJ.error("only a COLOR_RGB image is supported by drawTrail");
        int[] pixels=(int[])impl.getProcessor().getPixels();
        int w=impl.getWidth();
        int h=impl.getHeight();
        shape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            pixels[p.y*w+p.x]=pixel;
        }
    }

    public static MeanSem0 getPixelMeanSem(int[][]pixels,ImageShape shape){
        return getPixelMeanSem(pixels,0,shape);
    }

    public static MeanSem0 getPixelMeanSem(int[][]pixels, double dRef, ImageShape shape){
        MeanSem0 pixelMS=new MeanSem0();
        double pixel,sum=0,sum2=0;
        ArrayList<Point> points=new ArrayList();

        shape.getInnerPoints(points);

        int len=points.size();
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            pixel=pixels[p.y][p.x]-dRef;
            sum+=pixel;
            sum2+=pixel*pixel;
        }
        double m;
        if(len==0){
            m=0;
            sum2=0;
        }
        else{
            m=sum/len;
            sum2/=len;
        }
        pixelMS.updateMeanSquareSum(len, m, sum2);
        return pixelMS;
    }

    public static MeanSem1 getPixelMeanSem1(int[][]pixels, ImageShape shape){
        return getPixelMeanSem1(pixels,0,shape);
    }

    public static MeanSem1 getPixelMeanSem1(int[][]pixels, double dRef, ImageShape shape){
        MeanSem1 pixelMS=new MeanSem1();
        double pixel,sum=0,sum2=0,dMin=Double.POSITIVE_INFINITY,dMax=Double.NEGATIVE_INFINITY;
        ArrayList<Point> points=new ArrayList();

        shape.getInnerPoints(points);

        int len=points.size();
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            pixel=pixels[p.y][p.x]-dRef;
            if(pixel<dMin) dMin=pixel;
            if(pixel>dMax) dMax=pixel;
            sum+=pixel;
            sum2+=pixel*pixel;
        }
        double m;
        if(len==0){
            m=0;
            sum2=0;
        }
        else{
            m=sum/len;
            sum2/=len;
        }
        pixelMS.updateMeanSquareSum(len, m, sum2,dMax,dMin);
        return pixelMS;
    }
    public static MeanSem1 getContourMeanSem1(int[][]pixels, double dRef, ImageShape shape){
        MeanSem1 pixelMS=new MeanSem1();
        double pixel,sum=0,sum2=0,dMin=Double.POSITIVE_INFINITY,dMax=Double.NEGATIVE_INFINITY;
        ArrayList<Point> points=shape.getOuterContour();


        int len=points.size();
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            pixel=pixels[p.y][p.x]-dRef;
            if(pixel<dMin) dMin=pixel;
            if(pixel>dMax) dMax=pixel;
            sum+=pixel;
            sum2+=pixel*pixel;
        }
        double m;
        if(len==0){
            m=0;
            sum2=0;
        }
        else{
            m=sum/len;
            sum2/=len;
        }
        pixelMS.updateMeanSquareSum(len, m, sum2,dMax,dMin);
        return pixelMS;
    }

    public static MeanSem0 getPixelMeanSem(int[][]pixels, double dRef, ImageShape shape, int[][]pnStamp, int nExcludingType){
        MeanSem0 pixelMS=new MeanSem0();
        double pixel,sum=0,sum2=0;
        ArrayList<Point> points=new ArrayList();

        shape.getInnerPoints(points);

        int len=points.size(), num=0;
        Point p;
        int y,x;
        for(int i=0;i<len;i++){
            p=points.get(i);
            y=p.y;
            x=p.x;
            if(pnStamp[y][x]==nExcludingType) continue;
            pixel=pixels[y][x]-dRef;
            sum+=pixel;
            sum2+=pixel*pixel;
            num++;
        }
        double m;
        if(num==0){
            m=0;
            sum2=0;
        }
        else
        {
            m=sum/num;
            sum2/=num;
        }
        pixelMS.updateMeanSquareSum(num, m, sum2);
        return pixelMS;
    }

    public static MeanSem1 getPixelMeanSem1(int[][]pixels, double dRef, ImageShape shape, int[][]pnStamp, int nExcludingType){
        MeanSem1 pixelMS=new MeanSem1();
        double pixel,sum=0,sum2=0;
        ArrayList<Point> points=new ArrayList();
        double dMin=Double.POSITIVE_INFINITY,dMax=Double.NEGATIVE_INFINITY;

        shape.getInnerPoints(points);

        int len=points.size(), num=0;
        Point p;
        int y,x;
        for(int i=0;i<len;i++){
            p=points.get(i);
            y=p.y;
            x=p.x;
            if(pnStamp[y][x]==nExcludingType) continue;
            pixel=pixels[y][x]-dRef;
            if(pixel<dMin) dMin=pixel;
            if(pixel>dMax) dMax=pixel;
            sum+=pixel;
            sum2+=pixel*pixel;
            num++;
        }
        double m;
        if(num==0){
            m=0;
            sum2=0;
        }
        else
        {
            m=sum/num;
            sum2/=num;
        }
        pixelMS.updateMeanSquareSum(num, m, sum2, dMax, dMin);
        return pixelMS;
    }

    public static void calMeanSems(int[][] pixels, double dRef ,ArrayList<ImageShape> shapes, Point pt, ArrayList<MeanSem0> meanSems){
        int indexI=0;
        int indexF=shapes.size()-1;
        calMeanSems(pixels,dRef,shapes,pt,meanSems,indexI,indexF);
    }
    public static void calMeanSems(int[][] pixels, double dRef ,ArrayList<ImageShape> shapes, Point pt, ArrayList<MeanSem0> meanSems, int indexI, int indexF){
        meanSems.clear();
        ImageShape shape;
        for(int i=indexI;i<=indexF;i++){
            shape=shapes.get(i);
            shape.setCenter(pt);
            meanSems.add(getPixelMeanSem(pixels,dRef,shape));
        }
    }
    public static void calMeanSem(int[][] pixels, double dRef ,ImageShape shape, Point pt, MeanSem0 meanSem, int[][] stamp, int nExcludingType){
        shape.setCenter(pt);
        meanSem.update(getPixelMeanSem(pixels,dRef,shape,stamp,nExcludingType));
    }
    public static void calMeanSem(int[][] pixels, double dRef ,ImageShape shape, Point pt, MeanSem1 meanSem, int[][] stamp, int nExcludingType){
        shape.setCenter(pt);
        meanSem.update(getPixelMeanSem1(pixels,dRef,shape,stamp,nExcludingType));
    }
    public static void displayMinimalRings(int nRi, int nRo){
        ArrayList<Double> dvRs=new ArrayList();
        ArrayList<ImageShape> rings=getMinimalRings(nRi,nRo,dvRs);
        ImagePlus impl=WindowManager.getCurrentImage();
        int w=impl.getWidth(),h= impl.getHeight();
        ImagePlus implc=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w,h);

        double r=nRo+5.5;
        Point p1=closestInsidePoint(r);
        double r1=Math.sqrt(p1.x*p1.x+p1.y*p1.y);
        Point p0=closestInsidePointEX(p1);
        double r0=Math.sqrt(p0.x*p0.x+p0.y*p0.y);
        Point p2=closestOutsidePoint(r);
        double r2=Math.sqrt(p2.x*p2.x+p2.y*p2.y);
        Point p3=closestOutsidePointEX(p2);
        double r3=Math.sqrt(p3.x*p3.x+p3.y*p3.y);
        rings.add(new Ring(r1-0.5*(r1-r0),r1+0.5*(r2-r1)));
        rings.add(new Ring(r2-0.5*(r2-r1),r2+0.5*(r3-r2)));

        setFrameRanges(rings,new intRange(0,w-1), new intRange(0,h-1));
        Point pt=new Point(0,0);
        setImageCenter(rings,pt);

        int i,len=rings.size();
        int lent=Math.min(len-1, 255);
        int rgb1[]={255,0,0}, rgb2[]={0,len,0}, pixel;
        ArrayList <Point> points=new ArrayList();
        ImageShape ring;
        for(i=0;i<len;i++){
            ring=rings.get(i);
            ring.getInnerPoints(points);
            pixel=CommonGuiMethods.getColorIntoplationWR(rgb1, 0, rgb2, len-1, i,2);
            CommonMethods.drawTrail(implc, points, pixel);
        }
        implc.show();
    }

    public static void displayShapes(ArrayList<ImageShape> shapes, Point center){
        ImagePlus impl=WindowManager.getCurrentImage();
        int w=impl.getWidth(),h= impl.getHeight();
        ImagePlus implc=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w,h);

        setFrameRanges(shapes,new intRange(0,w-1), new intRange(0,h-1));
        setImageCenter(shapes,center);

        int i,len=shapes.size();
        int lent=Math.min(len-1, 255);
        int rgb1[]={255,0,0}, rgb2[]={0,len-1,0}, pixel;
        ArrayList <Point> points=new ArrayList();
        ImageShape ring;
        for(i=0;i<len;i++){
            ring=shapes.get(i);
            ring.getInnerPoints(points);
            pixel=CommonGuiMethods.getColorIntoplationWR(rgb1, 0, rgb2, len-1, i,2);
            CommonMethods.drawTrail(implc, points, pixel);
        }
        implc.show();
    }
    public static void drawShape(ImageShape shape, ImagePlus implc, int pixel){
        int w=implc.getWidth(),h= implc.getHeight();

        shape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        ArrayList <Point> points=new ArrayList();
        shape.getInnerPoints(points);
        CommonMethods.drawTrail(implc, points, pixel);
        implc.show();
    }
    public static Point closestInsidePoint(double r){//find the closest point to the circle with radius r and centered at (0,0)
        double r2=r*r;
        double dist,dn=r2;
        Point pt=new Point();
        int rx=(int)r,rn=(int)(r/Math.sqrt(2.));
        int x,y;

        for(x=rn;x<=rx;x++){
            y=(int)(Math.sqrt(r*r-x*x));
            dist=r2-(x*x+y*y);
            if(dist<dn){
                dn=dist;
                pt.setLocation(x,y);
            }
        }
        return pt;
    }
    public static Point closestInsidePointEX(double r){//find the closest point to the circle with radius r and centered at (0,0), excluding the points whose are on the circle
        if(r==0.) return null;
        double r2=r*r;
        double dist,dn=r2;
        Point pt=new Point();
        int rx=(int)r,rn=(int)(r/Math.sqrt(2.));
        int x,y;

        for(x=rn;x<=rx;x++){
            y=(int)(Math.sqrt(r*r-x*x));
            dist=r2-(x*x+y*y);
            if(dist==0.) continue;
            if(dist<dn){
                dn=dist;
                pt.setLocation(x,y);
            }
        }
        return pt;
    }
    public static Point closestInsidePointEX(Point p0){//find the closest point to the circle centered at (0,0) and going through the point p0, excluding the points whose are on the circle
        int r2=p0.x*p0.x+p0.y*p0.y;
        int dist2,dn=r2;
        Point pt=new Point();
        int rx=(int)Math.sqrt(r2);
        int rn=(int)(rx/Math.sqrt(2.));
        int x,y;

        for(x=rn;x<=rx;x++){
            y=(int)(Math.sqrt(r2-x*x));
            dist2=r2-(x*x+y*y);
            if(dist2==0) continue;
            if(dist2<dn){
                dn=dist2;
                pt.setLocation(x,y);
            }
        }
        return pt;
    }
    public static Point closestOutsidePoint(double r){//find the closest point to the circle with radius r and centered at (0,0)
        double dist,dn=Double.MAX_VALUE,r2=r*r;
        Point pt=new Point();
        int rx=(int)r,rn=(int)(r/Math.sqrt(2.));
        int x,y;

        for(x=rn;x<=rx;x++){
            y=(int)(Math.sqrt(r*r-x*x))+1;
            dist=(x*x+y*y)-r2;
            if(dist<dn){
                dn=dist;
                pt.setLocation(x,y);
            }
        }
        x=(int)r+1;
        dist=x*x-r2;
        if(dist<dn){
            dn=dist;
            pt.setLocation(x,0);
        }
        return pt;
    }
    public static Point closestOutsidePointEX(Point p0){//find the closest point to the circle centered at (0,0) and going through the point p0, excluding the points whose are on the circle
        int dist2,dn=Integer.MAX_VALUE,r2=p0.x*p0.x+p0.y*p0.y;
        Point pt=new Point();
        double r=Math.sqrt(r2);
        int rx=(int)r,rn=(int)(r/Math.sqrt(2.));
        int x,y;

        for(x=rn;x<=rx;x++){
            y=(int)(Math.sqrt(r2-x*x))+1;
            dist2=(x*x+y*y)-r2;
            if(dist2==0) continue;
            if(dist2<dn){
                dn=dist2;
                pt.setLocation(x,y);
            }
        }
        x=(int)r+1;
        dist2=(int)((x-r)*(x-r));
        if(dist2<dn&&dist2>0){
            dn=dist2;
            pt.setLocation(x,0);
        }
        return pt;
    }
    public static ArrayList<IntRangeArray> buildEnclosedXSegments(ArrayList<Point> contour, intRange xRange, intRange yRange){//build the object enclosed by the contour, regardless of inner holes.
        IntRangeArray2 yEnclosedStripes=new IntRangeArray2();
        int yn=yRange.getMin(),yx=yRange.getMax();
        int size0=yx-yn+1,size=contour.size();
        Point p=new Point(),pn=new Point(),po=new Point();
        int outer=1;
        if(ContourFollower.innerContour(contour))
            outer=-1;
        else
            outer=1;

        PointArray[] cPoints=new PointArray[size0];
        ArrayList<Point> pts;
        int i,j,k,kn,index;
        for(i=0;i<size0;i++){
            cPoints[i]=new PointArray();
        }
        for(i=0;i<size;i++){
            p=new Point(contour.get(i));
            index=p.y-yn;
            cPoints[index].m_pointArray.add(p);//distribute the contour points into different arraylist according to the y coordinates.
        }
        int enclosedArea=0;
        int x,xi,xf,y,pixel,nh;//nh: number of possible holes
        for(i=1;i<size0-1;i++){
            y=yn+i;
            pts=cPoints[i].m_pointArray;
            size=pts.size();
            for(j=0;j<size;j++){
                kn=j;
                pn.setLocation(pts.get(j));
                for(k=j+1;k<size;k++){
                    p=pts.get(k);
                    if(p.x<pn.x){
                        kn=k;
                        pn.setLocation(p);
                    }
                }
                pts.get(kn).setLocation(pts.get(j));
                pts.get(j).setLocation(pn);
            }//contour points with the same y coordinate are sorted (sorted) according to the x values
            IntRangeArray irr=new IntRangeArray();

            for(j=1;j<size;j++){
                xi=pts.get(j-1).x+1;
                xf=pts.get(j).x-1;
                if(xf<xi) continue;
                if(y==78&&xi==121){
                    xi=xi;
                }
                if(!ContourFollower.isInside(contour, pts.get(j-1), new Point(xi,y), outer)){//outside of the object
/*                    if(xi>=w){
                        xi=xi;
                    }
                    if(intensityRange.contains(pixels[y][xi])){//neighboring objects
                        xi=xi;
                    }*/
                    continue;//I need to come back and check this block later. 2/19/2010, passed. xi could be equal to w when the contour passes the same point (w-1, y) more than once.
                }

                intRange ir=new intRange(xi,xf);
                enclosedArea+=ir.getMax()-ir.getMin()+1;
                irr.m_intRangeArray.add(ir);
            }
            yEnclosedStripes.m_IntRangeArray2.add(irr);
        }
        xRange=new intRange(xRange.getMin()+1,xRange.getMax()-1);
        yRange=new intRange(yRange.getMin()+1,yRange.getMax()-1);
        return yEnclosedStripes.m_IntRangeArray2;
    }
    public static ArrayList<IntRangeArray> buildEnclosedYSegments(ArrayList<Point> contour, intRange xRange, intRange yRange){//build the object enclosed by the contour, regardless of inner holes.
        int outer=1;
        if(ContourFollower.innerContour(contour))
            outer=-1;
        else
            outer=1;

        IntRangeArray2 xEnclosedStripes=new IntRangeArray2();
        int xn=xRange.getMin(),xx=xRange.getMax();
        int size0=xx-xn+1,size=contour.size();
        Point p=new Point(),pn=new Point(),po=new Point();

        PointArray[] cPoints=new PointArray[size0];
        ArrayList<Point> pts;
        int i,j,k,kn,index;
        for(i=0;i<size0;i++){
            cPoints[i]=new PointArray();
        }
        for(i=0;i<size;i++){
            p=new Point(contour.get(i));
            index=p.x-xn;
            cPoints[index].m_pointArray.add(p);//distribute the contour points into different arraylist according to the x coordinates.
        }
        int enclosedArea=0;
        int x,yi,yf,y,pixel,nh;//nh: number of possible holes
        for(i=1;i<size0-1;i++){
            x=xn+i;
            pts=cPoints[i].m_pointArray;
            size=pts.size();
            for(j=0;j<size;j++){
                kn=j;
                pn.setLocation(pts.get(j));
                for(k=j+1;k<size;k++){
                    p=pts.get(k);
                    if(p.y<pn.y){
                        kn=k;
                        pn.setLocation(p);
                    }
                }
                pts.get(kn).setLocation(pts.get(j));
                pts.get(j).setLocation(pn);
            }//contour points with the same y coordinate are sorted (sorted) according to the y values
            IntRangeArray irr=new IntRangeArray();
            for(j=1;j<size;j++){
                yi=pts.get(j-1).y+1;
                yf=pts.get(j).y-1;
                if(yf<yi) continue;
                if(!ContourFollower.isInside(contour, pts.get(j-1), new Point(x,yi), outer)){//outside of the object
/*                    if(xi>=w){
                        xi=xi;
                    }
                    if(intensityRange.contains(pixels[y][xi])){//neighboring objects
                        xi=xi;
                    }*/
                    continue;//I need to come back and check this block later. 2/19/2010, passed. xi could be equal to w when the contour passes the same point (w-1, y) more than once.
                }

                intRange ir=new intRange(yi,yf);
                enclosedArea+=ir.getMax()-ir.getMin()+1;
                irr.m_intRangeArray.add(ir);
            }
            xEnclosedStripes.m_IntRangeArray2.add(irr);
        }
        xRange=new intRange(xRange.getMin()+1,xRange.getMax()-1);
        yRange=new intRange(yRange.getMin()+1,yRange.getMax()-1);
        return xEnclosedStripes.m_IntRangeArray2;
    }
    public static ImageShape buildImageShape(ArrayList<Point> contour){//build the object enclosed by the contour, regardless of inner holes.
        return buildImageShape(contour, null);
    }
    public static ImageShape buildImageShape(ArrayList<Point> contour, ImagePlus impl){//build the object enclosed by the contour, regardless of inner holes.
        intRange xRange=new intRange(), yRange=new intRange();
        ContourFollower.getXYRanges(contour, xRange, yRange);
        ArrayList<IntRangeArray> xSegments=buildEnclosedXSegments(contour,xRange,yRange);
        ArrayList<IntRangeArray> ySegments=buildEnclosedYSegments(contour,xRange,yRange);
        ImageShape is=new ImageShape(xSegments,ySegments);
        int currentSlice;
        if(impl==null)
            currentSlice=WindowManager.getCurrentImage().getCurrentSlice();
        else
            currentSlice=impl.getCurrentSlice();

        if(!is.consistent()){
            if(impl==null){
                impl=WindowManager.getCurrentImage();
                if(impl.getType()!=ImagePlus.COLOR_RGB){
                    impl=CommonMethods.copyToRGBImage(impl);
                    impl.setSlice(currentSlice);
                    impl.setTitle("for checking the inconcistency");
                }
            }
//            ContourFollower.markContour(impl, contour, Color.red);
            impl.show();
            is.markShape(impl, 0, 0);
            int w=impl.getWidth(),h=impl.getHeight();
            Point p0,p;
            p0=is.getLocation();
            p=new Point(p0);
            int dx=xRange.getRange()+1,dy=yRange.getRange()+1;
            if(p0.x+dx>=w||p0.y+dy>=h){
                    dx=0;
                    dy=0;
                    impl=CommonMethods.copyToRGBImage(impl);
                    impl.setSlice(currentSlice);
                    impl.setTitle("for checking the inconcistency Y segments");
                    impl.show();
            }else{
                p.translate(dx, dy);
                is.setLocation(p);
            }
//            ContourFollower.markContour(impl, contour, Color.red,new Point(dx,dy));
            is.markShapeY(impl, 0, 0);
            is.setLocation(p0);
        }
        return is;
    }
//    public static void calMeanSemFraction_SubpixelCCRings(int[][] pixels,SubpixelAreaInRingsLUT subpixelIS, ImageShape cIS, double dRefPixel, MeanSemFractional[] pcMeanSems){
    public static void calMeanSemFraction_SubpixelCCRings(int[][] pixels,SubpixelAreaInRingsLUT subpixelIS, ImageShape cIS, double dRefPixel, MeanSem1[] pcMeanSems){//11120
        //this method calculates the MeanSemFractional for each ring of the cocentric ring in subpixelIS contributed by
        //the pixels within the ImageShape cIS.
        //this method assumes the center of cIS and the interger parts of the coordinates of the center (without counting the subpixel displacement [-0.5, 0.5]
        //) of the cocentric rings of subpixelIS are the same.
        //the subpixel displacement (-0.5 to 0.5) should have been set to subpixelIS.
        Point po=cIS.getCenter(),p,pt;
        int x,y;
        ArrayList <Point> innerPoints=new ArrayList();
        cIS.getInnerPoints(innerPoints);
        int len=innerPoints.size(),i,j,pixel;
        intRange ringRange;
        double[] areaInRings;
        for(i=0;i<len;i++){
            p=innerPoints.get(i);
            x=p.x;
            y=p.y;
            areaInRings=subpixelIS.getAreaInRings(x, y);

            ringRange=subpixelIS.getRingRanges(x,y);
            pixel=pixels[y][x];
            for(j=ringRange.getMin();j<=ringRange.getMax();j++){
                pcMeanSems[j].addData(areaInRings[j], pixel-dRefPixel);
            }
        }
    }
    public static void calMeanSemFraction_SubpixelCCRings(int[][] pixels,int[][] regionStamp, SubpixelAreaInRingsLUT subpixelIS, ImageShape cIS, double dRefPixel,
            int[][] pnExcludingMap, int nExcludingLabel,MeanSem1[] pcMeanSems){//11120
        //this method calculates the MeanSemFractional for each ring of the cocentric ring in subpixelIS contributed by
        //the pixels within the ImageShape cIS.
        //this method assumes the center of cIS and the interger parts of the coordinates of the center (without counting the subpixel displacement [-0.5, 0.5]
        //) of the cocentric rings of subpixelIS are the same.
        //the subpixel displacement (-0.5 to 0.5) should have been set to subpixelIS.
        subpixelIS.minDistToBorder=Integer.MAX_VALUE;
        subpixelIS.maxDistToBorder=Integer.MIN_VALUE;
        Point po=cIS.getCenter(),p,pt;
        int x,y;
        ArrayList <Point> innerPoints=new ArrayList();
        cIS.getInnerPoints(innerPoints);
        int len=innerPoints.size(),i,j,pixel,region,region0=ImageAnalysis.LandscapeAnalyzerPixelSorting.getRegionIndex(regionStamp[po.y][po.x]);;
        intRange ringRange;
        double[] areaInRings;
        int dist;
        for(i=0;i<len;i++){
            p=innerPoints.get(i);
            x=p.x;
            y=p.y;
            if(pnExcludingMap[y][x]==nExcludingLabel) {
                continue;
            }
            areaInRings=subpixelIS.getAreaInRings(x, y);

            ringRange=subpixelIS.getRingRanges(x,y);
            pixel=pixels[y][x];
            region=ImageAnalysis.LandscapeAnalyzerPixelSorting.getRegionIndex(regionStamp[y][x]);
            if(ringRange.emptyRange()) continue;
            if(region==region0){
                dist=ringRange.getMax();
                if(dist>subpixelIS.maxDistToBorder)
                    subpixelIS.maxDistToBorder=dist;
                for(j=ringRange.getMin();j<=ringRange.getMax();j++){
                    pcMeanSems[j].addData(areaInRings[j], pixel-dRefPixel);
                }
            }else{
                dist=ringRange.getMin();
                if(dist<subpixelIS.minDistToBorder)
                    subpixelIS.minDistToBorder=dist;
            }
        }
    }
    public static void calMeanSemFraction_SubpixelCCRings(int[][] pixels,int[][] regionStamp, SubpixelAreaInRingsLUT subpixelIS, ImageShape cIS, double dRefPixel,
            MeanSem1[] pcMeanSems){//11120
        //this method calculates the MeanSemFractional for each ring of the cocentric ring in subpixelIS contributed by
        //the pixels within the ImageShape cIS.
        //this method assumes the center of cIS and the interger parts of the coordinates of the center (without counting the subpixel displacement [-0.5, 0.5]
        //) of the cocentric rings of subpixelIS are the same.
        //the subpixel displacement (-0.5 to 0.5) should have been set to subpixelIS.
        subpixelIS.minDistToBorder=Integer.MAX_VALUE;
        subpixelIS.maxDistToBorder=Integer.MIN_VALUE;
        Point po=cIS.getCenter(),p,pt;
        int x,y;
        ArrayList <Point> innerPoints=new ArrayList();
        cIS.getInnerPoints(innerPoints);
        int len=innerPoints.size(),i,j,pixel,region,region0=ImageAnalysis.LandscapeAnalyzerPixelSorting.getRegionIndex(regionStamp[po.y][po.x]);;
        intRange ringRange;
        double[] areaInRings;
        int dist;
        for(i=0;i<len;i++){
            p=innerPoints.get(i);
            x=p.x;
            y=p.y;
            areaInRings=subpixelIS.getAreaInRings(x, y);

            ringRange=subpixelIS.getRingRanges(x,y);
            pixel=pixels[y][x];
            region=ImageAnalysis.LandscapeAnalyzerPixelSorting.getRegionIndex(regionStamp[y][x]);
            if(ringRange.emptyRange()) continue;
            if(region==region0){
                dist=ringRange.getMax();
                if(dist>subpixelIS.maxDistToBorder)
                    subpixelIS.maxDistToBorder=dist;
                for(j=ringRange.getMin();j<=ringRange.getMax();j++){
                    pcMeanSems[j].addData(areaInRings[j], pixel-dRefPixel);
                }
            }else{
                dist=ringRange.getMin();
                if(dist<subpixelIS.minDistToBorder)
                    subpixelIS.minDistToBorder=dist;
            }
        }
    }
    public static double getWeightedMean_SubpixelCCRings(int[][] pixels,SubpixelAreaInRingsLUT subpixelIS, ImageShape cIS, double pdWeights[], double dRefPixel){//11308
        //this method calculates the weighted mean of all pixels within the shape cIS. the length of pdWeights need to match with the number of rings in subpixelIS
        //pdWeights is assumed to be normalized already
        //this method assumes the center of cIS and the interger parts of the coordinates of the center (without counting the subpixel displacement [-0.5, 0.5]
        //) of the cocentric rings of subpixelIS are the same.
        //the subpixel displacement (-0.5 to 0.5) should have been set to subpixelIS.
        Point p;
        int x,y;
        Point[] innerPoints=cIS.getInnerPoints();
        int len=innerPoints.length,i,j,pixel;
        intRange ringRange;
        double[] areaInRings;
        double mean=0;
        for(i=0;i<len;i++){
            p=innerPoints[i];
            areaInRings=subpixelIS.getAreaInRings(p.x, p.y);
            ringRange=subpixelIS.getRingRanges(p.x,p.y);
            cIS.reflectIntoFrame(p);
            pixel=pixels[p.y][p.x];
            for(j=ringRange.getMin();j<=ringRange.getMax();j++){
                mean+=areaInRings[j]*pdWeights[j]*(pixel-dRefPixel);
            }
        }
        return mean;
    }
    public static FittingResultsNode fitPixels_Gaussian2D(ImagePlus implDisplay, int[][] pixels, ImageShape cIS,ArrayList<Point> locations){
        Non_Linear_Fitter fitter=new Non_Linear_Fitter();
        fitter.setFunctionImage(implDisplay);
        ArrayList<Point> innerPoints=new ArrayList();
        cIS.getInnerPoints(innerPoints);
        ArrayList<Point> contour=cIS.getOuterContour();
        ContourFollower.removeOffBoundPoints(cIS.getXFrameRange().getMax()+1, cIS.getYFrameRange().getMax()+1, contour);
        CommonMethods.appendPath(innerPoints, contour);

        int nNumPoints=innerPoints.size();
        int nNumLocations=locations.size();
        Point p;

//        ArrayList<ConstraintFunction> ConstraintFunctions=new ArrayList();
//        ArrayList<ConstraintViolationChecker> ViolationCheckers=new ArrayList();
        ArrayList<ConstraintNode> cvConstraintNodes=new ArrayList();
        ArrayList<ConstraintNode> cvConstraintNodes1=new ArrayList();


        double[] pdConstraintFuncPars=new double[2];
        pdConstraintFuncPars[0]=2000000;
        pdConstraintFuncPars[1]=1;
        ArrayList<Integer> ConstrainedIndexes=new ArrayList();

        double[] pdOrigin=new double[2];
        p=cIS.getCenter();
        pdOrigin[0]=p.x;
        pdOrigin[1]=p.y;
        ConstrainedIndexes.add(4);
        ConstrainedIndexes.add(5);
        cvConstraintNodes.add(new ConstraintNode(new DistantConstraintFunction("exponential",ConstrainedIndexes,pdOrigin,pdConstraintFuncPars),new ConstraintLocationChecker_ImageShape(ConstrainedIndexes,cIS)));
/*
        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(0);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,0,Double.MAX_VALUE,pdConstraintFuncPars));

        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(1);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,1,3,pdConstraintFuncPars));

        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(2);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,1,3,pdConstraintFuncPars));

        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(3);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,-0.5*Math.PI,0.5*Math.PI,pdConstraintFuncPars));
*/
        ConstraintExpander cExpander0=new ConstraintExpander(cvConstraintNodes);

        double [][]pdX=new double[nNumPoints][2];
        double []pdData=new double[nNumPoints];
        int i,j,x,y;
        for(i=0;i<nNumPoints;i++){
            p=innerPoints.get(i);
            x=p.x;
            y=p.y;
            pdData[i]=pixels[y][x];
            pdX[i][0]=x;
            pdX[i][1]=y;
        }
        double base=CommonStatisticsMethods.getMean(pixels, contour);
        int nParsPerTerm=6;

        double[] pdPars=new double[nParsPerTerm*nNumLocations+1];
        int pixel,o;

        cvConstraintNodes.clear();//to hold the constraints for the location of the ellipsical
        //gaussian functions within the ImageShape, and set a distance constraint from the "localtions".
        ArrayList<ConstraintNode> cvConstraintNodest;
        DoubleRange cDistRange=new DoubleRange(0,2);
        ArrayList<String> functionTypes=new ArrayList();

        pdPars[0]=base;
        o=1;
        int len;
        for(i=0;i<nNumLocations;i++){
            p=locations.get(i);
            pixel=pixels[p.y][p.x];
            pdPars[o]=pixel-base;
            pdPars[o+1]=2.2;
            pdPars[o+2]=1.7;
            pdPars[o+3]=0.75;
            pdPars[o+4]=p.x;
            pdPars[o+5]=p.y;
            cvConstraintNodest=cExpander0.getConstraintNodes(o);
            len=cvConstraintNodest.size();
            for(j=0;j<len;j++){
                cvConstraintNodes.add(cvConstraintNodest.get(j));
            }

            ConstrainedIndexes=new ArrayList();
            pdOrigin=new double[2];
            p=locations.get(i);
            pdOrigin[0]=p.x;
            pdOrigin[1]=p.y;
            ConstrainedIndexes.add(o+4);
            ConstrainedIndexes.add(o+5);
            cvConstraintNodes.add(new ConstraintNode(new DistantConstraintFunction("exponential",ConstrainedIndexes,pdOrigin,pdConstraintFuncPars),new ConstraintLocationChecker_Distance(ConstrainedIndexes,pdOrigin,cDistRange)));

            o+=nParsPerTerm;
            functionTypes.add("gaussian2D_GaussianPars");
        }

        ConstrainedIndexes=new ArrayList();
        pdOrigin=new double[2];
        p=cIS.getCenter();
        pdOrigin[0]=p.x;
        pdOrigin[1]=p.y;
        ConstrainedIndexes.add(2);
        ConstrainedIndexes.add(3);
        cvConstraintNodes1.add(new ConstraintNode(new DistantConstraintFunction("exponential",ConstrainedIndexes,pdOrigin,pdConstraintFuncPars),new ConstraintLocationChecker_ImageShape(ConstrainedIndexes,cIS)));

//        StandardFittingFunction func=new StandardFittingFunction("gaussian2D_IPO",nNumLocations*nParsPerTerm+1,2);11626
        ComposedFittingFunction func=new ComposedFittingFunction(functionTypes);
        ConstraintExpander cExpander=new ConstraintExpander(cvConstraintNodes1);
        fitter.setConstraints(cvConstraintNodes);

        //for debugging
        double[] transfPars=StandardFittingFunction.getTransformedGaussian2DParameters(pdPars);
        double[] gaussPars=StandardFittingFunction.getGaussian2DParameters(transfPars);

  //      fitter.update(pdX, pdData, StandardFittingFunction.getTransformedGaussian2DParameters(pdPars), func, Non_Linear_Fitter.Least_Square, Non_Linear_Fitter.Simplex, 0, nNumPoints-1, 1, null);
        fitter.update(pdX, pdData, pdPars, func, Non_Linear_Fitter.Least_Square, Non_Linear_Fitter.Simplex, 0, nNumPoints-1, 1, null);
        fitter.setAsExpandableModel(55);
        fitter.setModelExpander(new IPOGaussianExpander());
        fitter.fitData_Apache();
//        double[] pdFittedPars=StandardFittingFunction.getGaussian2DParameters(fitter.getFittedPars());
        FittingResultsNode resultsNode=fitter.getFittingReults();
        return resultsNode;
    }
    public static FittingResultsNode fitPixels_Gaussian2Do(ImagePlus implDisplay, int[][] pixels, ImageShape cIS,ArrayList<Point> locations){
        Non_Linear_Fitter fitter=new Non_Linear_Fitter();
        fitter.setFunctionImage(implDisplay);
        ArrayList<Point> innerPoints=new ArrayList();
        cIS.getInnerPoints(innerPoints);
        ArrayList<Point> contour=cIS.getOuterContour();
        ContourFollower.removeOffBoundPoints(cIS.getXFrameRange().getMax()+1, cIS.getYFrameRange().getMax()+1, contour);
        CommonMethods.appendPath(innerPoints, contour);

        int nNumPoints=innerPoints.size();
        int nNumLocations=locations.size();
        Point p;

//        ArrayList<ConstraintFunction> ConstraintFunctions=new ArrayList();
//        ArrayList<ConstraintViolationChecker> ViolationCheckers=new ArrayList();
        ArrayList<ConstraintNode> cvConstraintNodes=new ArrayList();
        ArrayList<ConstraintNode> cvConstraintNodes1=new ArrayList();


        double[] pdConstraintFuncPars=new double[2];
        pdConstraintFuncPars[0]=2000000;
        pdConstraintFuncPars[1]=1;
        ArrayList<Integer> ConstrainedIndexes=new ArrayList();

        double[] pdOrigin=new double[2];
        p=cIS.getCenter();
        pdOrigin[0]=p.x;
        pdOrigin[1]=p.y;
        ConstrainedIndexes.add(4);
        ConstrainedIndexes.add(5);
        cvConstraintNodes.add(new ConstraintNode(new DistantConstraintFunction("exponential",ConstrainedIndexes,pdOrigin,pdConstraintFuncPars),new ConstraintLocationChecker_ImageShape(ConstrainedIndexes,cIS)));
/*
        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(0);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,0,Double.MAX_VALUE,pdConstraintFuncPars));

        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(1);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,1,3,pdConstraintFuncPars));

        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(2);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,1,3,pdConstraintFuncPars));

        ConstrainedIndexes=new ArrayList();
        ConstrainedIndexes.add(3);
        ViolationCheckers.add(new ConstraintViolationChecker_default(ConstrainedIndexes));
        ConstraintFunctions.add(new ValueRangeConstraintFunction("exponential",ConstrainedIndexes,-0.5*Math.PI,0.5*Math.PI,pdConstraintFuncPars));
*/
        ConstraintExpander cExpander0=new ConstraintExpander(cvConstraintNodes);

        double [][]pdX=new double[nNumPoints][2];
        double []pdData=new double[nNumPoints];
        int i,j,x,y;
        for(i=0;i<nNumPoints;i++){
            p=innerPoints.get(i);
            x=p.x;
            y=p.y;
            pdData[i]=pixels[y][x];
            pdX[i][0]=x;
            pdX[i][1]=y;
        }
        double base=CommonStatisticsMethods.getMean(pixels, contour);
        int nParsPerTerm=6;
        double[] pdPars=new double[nParsPerTerm*nNumLocations+1];
        int pixel,o;

        cvConstraintNodes.clear();//to hold the constraints for the location of the ellipsical
        //gaussian functions within the ImageShape, and set a distance constraint from the "localtions".
        ArrayList<ConstraintNode> cvConstraintNodest;
        DoubleRange cDistRange=new DoubleRange(0,2);

        pdPars[0]=base;
        o=1;
        int len;
        for(i=0;i<nNumLocations;i++){
            p=locations.get(i);
            pixel=pixels[p.y][p.x];
            pdPars[o]=pixel-base;
            pdPars[o+1]=2.2;
            pdPars[o+2]=1.7;
            pdPars[o+3]=0.75;
            pdPars[o+4]=p.x;
            pdPars[o+5]=p.y;
            cvConstraintNodest=cExpander0.getConstraintNodes(o);
            len=cvConstraintNodest.size();
            for(j=0;j<len;j++){
                cvConstraintNodes.add(cvConstraintNodest.get(j));
            }

            ConstrainedIndexes=new ArrayList();
            pdOrigin=new double[2];
            p=locations.get(i);
            pdOrigin[0]=p.x;
            pdOrigin[1]=p.y;
            ConstrainedIndexes.add(o+4);
            ConstrainedIndexes.add(o+5);
            cvConstraintNodes.add(new ConstraintNode(new DistantConstraintFunction("exponential",ConstrainedIndexes,pdOrigin,pdConstraintFuncPars),new ConstraintLocationChecker_Distance(ConstrainedIndexes,pdOrigin,cDistRange)));

            o+=nParsPerTerm;
        }

        ConstrainedIndexes=new ArrayList();
        pdOrigin=new double[2];
        p=cIS.getCenter();
        pdOrigin[0]=p.x;
        pdOrigin[1]=p.y;
        ConstrainedIndexes.add(2);
        ConstrainedIndexes.add(3);
        cvConstraintNodes1.add(new ConstraintNode(new DistantConstraintFunction("exponential",ConstrainedIndexes,pdOrigin,pdConstraintFuncPars),new ConstraintLocationChecker_ImageShape(ConstrainedIndexes,cIS)));

//        StandardFittingFunction func=new StandardFittingFunction("gaussian2D_IPO",nNumLocations*nParsPerTerm+1,2);11626
        StandardFittingFunction func=new StandardFittingFunction("gaussian2D_IPO");
        func.setNumStartingTerms(nNumLocations);
        ConstraintExpander cExpander=new ConstraintExpander(cvConstraintNodes1);
        fitter.setConstraints(cvConstraintNodes);

        //for debugging
        double[] transfPars=StandardFittingFunction.getTransformedGaussian2DParameters(pdPars);
        double[] gaussPars=StandardFittingFunction.getGaussian2DParameters(transfPars);

  //      fitter.update(pdX, pdData, StandardFittingFunction.getTransformedGaussian2DParameters(pdPars), func, Non_Linear_Fitter.Least_Square, Non_Linear_Fitter.Simplex, 0, nNumPoints-1, 1, null);
        fitter.update(pdX, pdData, pdPars, func, Non_Linear_Fitter.Least_Square, Non_Linear_Fitter.Simplex, 0, nNumPoints-1, 1, null);
        fitter.setAsExpandableModel(55);
        fitter.fitData_Apache();
//        double[] pdFittedPars=StandardFittingFunction.getGaussian2DParameters(fitter.getFittedPars());
        FittingResultsNode resultsNode=fitter.getFittingReults();
        return resultsNode;
    }
    static public ImageShape buildImageShape_Scattered(ArrayList<Point> points){
        ArrayList<IntRangeArray> xSegments=buildXSegments(points);
        ArrayList<IntRangeArray> ySegments=buildYSegments(points);
        return new ImageShape(xSegments,ySegments);
    }
    static public ArrayList<IntRangeArray> buildXSegments(ArrayList<Point> points0){//need to make copy of the points first)
        ArrayList<Point> points=new ArrayList();
        CommonStatisticsMethods.copyPointArray(points0, points);
        ArrayList<IntRangeArray> xSegments=new ArrayList();
        int len=points.size(),i,j,index;
        double pdt[]=new double[len];
        ArrayList<Object> ovA=new ArrayList();
        for(i=0;i<len;i++){
            pdt[i]=points.get(i).y;
            ovA.add(points.get(i));
        }
        CommonMethods.sortObjectArray(ovA, pdt);
        for(i=0;i<len;i++){
            points.set(i, (Point)ovA.get(i));
        }
        int yn=points.get(0).y,y,yx=points.get(len-1).y,position,yt;
        position=0;
        Point pt=points.get(0);
        yt=pt.y;
        ArrayList<Integer> nvXs;
//        ArrayList<IntRangeArray> ySegments=new ArrayList();
        for(y=yn;y<=yx;y++){
            nvXs=new ArrayList();
            pt=points.get(position);
            yt=pt.y;
            while(yt==y){
                nvXs.add(pt.x);
                position++;
                if(position>=len) break;
                pt=points.get(position);
                yt=pt.y;
            }
            if(!nvXs.isEmpty())xSegments.add(getRanges(nvXs));
        }
        return xSegments;
    }
    static public ArrayList<IntRangeArray> buildYSegments(ArrayList<Point> points0){
        ArrayList<Point> points=new ArrayList();
        CommonStatisticsMethods.copyPointArray(points0, points);
        ArrayList<IntRangeArray> ySegments=new ArrayList();
        int len=points.size(),i,j,index;
        double pdt[]=new double[len];
        ArrayList<Object> ovA=new ArrayList();
        for(i=0;i<len;i++){
            pdt[i]=points.get(i).x;
            ovA.add(points.get(i));
        }
        CommonMethods.sortObjectArray(ovA, pdt);
        for(i=0;i<len;i++){
            points.set(i, (Point)ovA.get(i));
        }
        int xn=points.get(0).x,x,xx=points.get(len-1).x,position,xt;
        position=0;
        Point pt=points.get(0);
        xt=pt.x;
        ArrayList<Integer> nvYs;
//        ArrayList<IntRangeArray> ySegments=new ArrayList();
        for(x=xn;x<=xx;x++){
            nvYs=new ArrayList();
            pt=points.get(position);
            xt=pt.x;
            while(xt==x){
                nvYs.add(pt.y);
                position++;
                if(position>=len) break;
                pt=points.get(position);
                xt=pt.x;
            }
            if(!nvYs.isEmpty())ySegments.add(getRanges(nvYs));
        }
        return ySegments;
    }
    public static IntRangeArray getRanges(ArrayList<Integer> nvXs){
        QuickSortInteger.quicksort(nvXs);
        IntRangeArray ira=new IntRangeArray();
        int len=nvXs.size(),i,n0,n,nr;
        intRange ir=new intRange();
        n0=nvXs.get(0);
        nr=n0+1;
        for(i=1;i<len;i++) {
            n=nvXs.get(i);
            if(n!=nr){
                ira.m_intRangeArray.add(new intRange(n0,nr-1));
                n0=n;
            }
            nr=n+1;
        }
        ira.m_intRangeArray.add(new intRange(n0,nr-1));
        return ira;
    }
    public static ImageShape buildDrawingImageShape(double[][] pdX, int[] pnDrawingVarIndexes, int maxImageLength, int nI, int nF, int nDelta){//checked on 7/7/11
        double[] pdPixelSizes=calPatchPixelSize(pdX,pnDrawingVarIndexes,maxImageLength);
        DoubleRange[] pcDrawingVarRanges=calDrawingVarRanges(pdX,pnDrawingVarIndexes);
        double dx,dxn,pdXn[]=new double[2];
        int i,j,varIndex,x,len=pdX.length;
        int[] position=new int[2];
        ArrayList<Point> points=new ArrayList();
        for(i=0;i<2;i++){
            pdXn[i]=pcDrawingVarRanges[i].getMin();
        }
        Point po=new Point((int)((pdXn[pnDrawingVarIndexes[0]])/pdPixelSizes[pnDrawingVarIndexes[0]]+0.5),(int)((pdXn[pnDrawingVarIndexes[1]])/pdPixelSizes[pnDrawingVarIndexes[1]]+0.5));
        for(i=nI;i<=nF;i+=nDelta){
            for(j=0;j<2;j++){
                varIndex=pnDrawingVarIndexes[j];
                dx=pdX[i][varIndex];
                x=(int)((dx-pdXn[j])/pdPixelSizes[j]+0.5);
                position[j]=x;
            }
            points.add(new Point(position[0],position[1]));
        }
        ImageShape cIS=ImageShapeHandler.buildImageShape_Scattered(points);
        cIS.setLocation(po);
        return cIS;
    }
    public static DoubleRange[] calDrawingVarRanges(double[][] pdX, int[] pnDrawingVarIndexes){
        int i,j,varIndex,len=pdX.length;
        DoubleRange[] pcDrawingVarRanges=new DoubleRange[2];
        for(i=0;i<2;i++){
            pcDrawingVarRanges[i]=new DoubleRange();
        }
        for(i=0;i<len;i++){
            for(j=0;j<2;j++){
                 pcDrawingVarRanges[j].expandRange(pdX[i][pnDrawingVarIndexes[j]]);
            }
        }
        return pcDrawingVarRanges;
    }
    public static double[] calPatchPixelSize(double[][] pdX, int[] pnDrawingVarIndexes, int maxImageLength){
        double[] pixelSize=new double[2];
        int nDataPoints=pdX.length;
        int i,varIndex;
        double delta;
        for(i=0;i<2;i++){
            varIndex=pnDrawingVarIndexes[i];
            double[] pdT=new double[nDataPoints];
            CommonStatisticsMethods.copyArray_Column(pdX,varIndex,0,nDataPoints-1,1,pdT,0,1);
            delta=Histogram.getOptimalBinSize(pdT, maxImageLength);
            pixelSize[i]=delta;
        }
        return pixelSize;
    }
    public static Roi getRoi(ImageShape cIS){
        Roi aRoi=null;
        ArrayList <Point> perimeter=cIS.getPerimeterPoints();
        int i,len=perimeter.size();
        int[] xs=new int[len], ys=new int[len];
        for(i=0;i<len;i++){
            xs[i]=perimeter.get(i).x;
            ys[i]=perimeter.get(i).y;
        }
        aRoi=new PolygonRoi(xs,ys,len,Roi.POLYGON);
        return aRoi;
    }
    public static ImageShape buildImageShape(Roi aRoi){
        ArrayList<Point> points=new ArrayList();
        CommonMethods.getRoiPoints(aRoi, points);
        ImageShape cIS=ImageShapeHandler.buildImageShape_Scattered(points);
        return cIS;
    }
    public static void filtPixels_Mean(int[][] pixels, int[][] pixels1, ImageShape shape){
        filtPixels(pixels,pixels1,shape,"Mean",0.5);
    }
    public static void filtPixels_Percentile(int[][] pixels, int[][] pixels1, ImageShape shape,double percentile){
        filtPixels(pixels,pixels1,shape,"Percentile",percentile);
    }
    public static void filtPixels(int[][] pixels, int[][] pixels1, ImageShape shape, String sType, double percentile){
        int w=pixels[0].length,h=pixels.length;
        ImageScanner imsc=new ImageScanner(new intRange(0,w-1), new intRange(0,h-1), new intRange(0,w-1), new intRange(0,h-1),shape);
        Point p;
        intRange pixelRange=CommonStatisticsMethods.getRange(pixels);
        Histogram hist=new Histogram();
        hist.update(0, pixelRange.getMin(), pixelRange.getMax(), 1);
        ArrayList<Point> newPoints=new ArrayList();
        ArrayList<Point> oldPoints=new ArrayList();

        PointArray paNew=new PointArray(), paOld=new PointArray();
        p=imsc.getPosition();
        double value=0;

        while(true){
            p=imsc.getPosition();
            imsc.getPoints(paNew, paOld);
            newPoints=paNew.m_pointArray;
            oldPoints=paOld.m_pointArray;
            CommonMethods.addValuesToHistogram(pixels,newPoints,hist);
            CommonMethods.removeValuesFromHistogram(pixels,oldPoints,hist);
            if(sType.contentEquals("Mean"))
                value=hist.getMean();
            else if(sType.contentEquals("Percentile"))
                value=hist.getPercentileValue();
            else
                value=0;
            pixels1[p.y][p.x]=(int)(value+0.5);
            if(imsc.done()) break;
            imsc.move();
        }
    }
    public static int getPixels(int[][] pixels,ImageShape cIS,double[][] pdX, double[] pdY){
        getPixels(pixels,cIS,pdX,pdY,1);
        return 1;
    }
    public static int getPixels(int[][] pixels,ImageShape cIS,double[][] pdX, double[] pdY, double scale){
        getPixels(pixels,cIS,pdX,pdY,scale,null);
        return 1;
    }
    public static int getPixels(int[][] pixels,ImageShape cIS,double[][] pdX, double[] pdY, double scale, intRange pixelRange){
        ArrayList<Point> points=new ArrayList();
        if(pixelRange!=null) pixelRange.resetRange();
        int i,len,h=pixels.length,w=pixels[0].length;
        intRange xFRange=cIS.getXFrameRange(),yFRange=cIS.getYFrameRange();
        xFRange.setMin(Math.max(0, xFRange.getMin()));
        yFRange.setMin(Math.max(0, yFRange.getMin()));
        xFRange.setMax(Math.min(w-1, xFRange.getMax()));
        yFRange.setMin(Math.min(h-1, yFRange.getMin()));
        cIS.setFrameRanges(xFRange, yFRange);
        cIS.getInnerPoints(points);
        len=points.size();
        if(pdY.length!=len){
            IJ.error("error in getPixels of ImageShapeHandler");
            return -1;
        }
        Point pt;
        int pixel;
        for(i=0;i<len;i++){
            pt=points.get(i);
            if(pt.x<0||pt.x>=w){
                continue;
            }
            if(pt.y<0||pt.y>=h){
                continue;
            }
            pdX[i][0]=pt.x;
            pdX[i][1]=pt.y;
            pixel=(int)(pixels[pt.y][pt.x]*scale+0.5);
            pdY[i]=pixels[pt.y][pt.x]*scale;
            if(pixelRange!=null) pixelRange.expandRange(pixel);
        }
        return 1;
    }
}
