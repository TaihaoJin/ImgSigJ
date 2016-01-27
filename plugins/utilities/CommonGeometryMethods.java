/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

import ij.IJ;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfInt;

/**
 *
 * @author Taihao
 */
public class CommonGeometryMethods {

    public static double crossProduct(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
        double dx1=x2-x1,dy1=y2-y1,dx2=x4-x3,dy2=y4-y3;
        return dx1*dy2-dy1*dx2;
    }
    public static double dotProduct(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
        double dx1=x2-x1,dy1=y2-y1,dx2=x4-x3,dy2=y4-y3;
        return dx1*dx2+dy1*dy2;
    }
    public static boolean collinear(double x1, double y1, double x2, double y2, double x3, double y3){
        return crossProduct(x1,y1,x2,y2,x1,y1,x3,y3)==0;
    }
    public static boolean rightSide(double x1, double y1, double x2, double y2, double x3, double y3){//return true if the point(x3,y3) is on the right side of the
        //ray from (x1,y1) to (x2,y2
        return crossProduct(x1,y1,x3,y3,x1,y1,x2,y2)>0;
    }
    public static boolean leftSide(double x1, double y1, double x2, double y2, double x3, double y3){//return true if the point(x3,y3) is on the left side of the
        //ray from (x1,y1) to (x2,y2
        return crossProduct(x1,y1,x3,y3,x1,y1,x2,y2)<0;
    }
    public static int sideness(double x1, double y1, double x2, double y2, double x3, double y3){//
        double cp=crossProduct(x1,y1,x3,y3,x1,y1,x2,y2);
        if(cp<0) return -1;//left, but oposite for images
        if(cp>0) return 1;//right
        return 0;//collinear
    }
    public static double dist2(double x1, double y1, double x2, double y2){
        double dx=x2-x1,dy=y2-y1;
        return dx*dx+dy*dy;
    }
    public static double getAngle(double x1, double y1, double x2, double y2, double x3, double y3){
        //xi, yi are the x and y coordinates of point pi (i=1, 2, 3), respectively.
        //return the angle between P1-P2 and P3-P2.
        double s32=(x2-x1)*(x2-x1)+(y2-y1)*(y2-y1);
        double s22=(x3-x1)*(x3-x1)+(y3-y1)*(y3-y1);
        double s12=(x3-x2)*(x3-x2)+(y3-y2)*(y3-y2);
        if(Math.abs(s12*s32)<CommonMethods.machEps) {
//            IJ.error("can not determine the angle if a vector has zero length");
            return -4*Math.PI;
        }//making it out of domain, so that the caller can realize
        double ca=-0.5*(s22-s12-s32)/Math.sqrt(s12*s32);
        if(Math.abs(ca)>1){//could be caused by numerical error when the analytical value should be 1
            if(ca>0) return 0;
            //ca<0
            return Math.PI;
        }
        ca=Math.acos(ca);
        return ca;
    }
    public static int getClosestPointOnLine(double x1, double y1, double x2, double y2, double x0, double y0, double[] pdxy){
        //pdxy will store the x and y coordinates of the closest point on the line formed by (x1, y1) and (x2, y2) to the point (x0,y0).
        if(x1==x2){
            pdxy[0]=x1;
            pdxy[1]=y0;
            return 1;
        }
        if(y1==y2){
            pdxy[0]=x0;
            pdxy[1]=y1;
            return 1;
        }
        double k=(y2-y1)/(x2-x1);
        double x=(k*k*x1-k*y1+x0+k*y0)/(k*k+1);
        pdxy[0]=x;
        pdxy[1]=k*(x-x1)+y1;
        return 1;
    }
    public static double getTriangleArea(double x1, double y1, double x2, double y2, double x3, double y3){
        double[] pdxy=new double[2];
        getClosestPointOnLine(x1,y1,x2,y2,x3,y3,pdxy);
        return 0.5*Math.sqrt(((x3-pdxy[0])*(x3-pdxy[0])+(y3-pdxy[1])*(y3-pdxy[1]))*((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)));
    }
    public static boolean withinLineSegment(double x1, double y1, double x2, double y2, double x, double y){
        //the point (x,y) is coline with (x1,y1) and (x2,y2).
        if(Math.abs(y2-y1)>Math.abs(x2-x1)){
            return ((y1-y)*(y-y2)>=0);
        }
        return (x1-x)*(x-x2)>=0;
    }
    public static boolean withinTriangle(double x1, double y1, double x2, double y2, double x3, double y3, double x, double y){
        //return true if the point (x,y) is in the triable (x1,y1),(x2,y2),(x3,y3).
        int sideness;
        sideness=sideness(x1,y1,x2,y2,x,y);
        sideness+=sideness(x2,y2,x3,y3,x,y);
        sideness+=sideness(x3,y3,x1,y1,x,y);
        return Math.abs(sideness)==3;//they are all 1 or all -1
    }
    public static int getCircleCenter(double xl, double yl, double xr, double yr, double r, double[] centers){
        //two center positions, centers={xc1,yc1,xc2,yc2}
        //the point (xc1,yc1) is at the upper side of the line from (xl,yl) to (xr,yr) (or on the left hand side).
        double dx=xr-xl,dy=yr-yl,xm=0.5*(xl+xr),ym=0.5*(yl+yr);
        double xc1=Double.NaN,yc1=Double.NaN,xc2=Double.NaN,yc2=Double.NaN;
        double r2=r*r,dist2=dx*dx+dy*dy;
        double deltaX,deltaY,d,d1,d2;
        if(dist2>r2||dist2==0) return -1;//wrong condition
        if(dist2==r2) {
            centers[0]=xm;
            centers[1]=ym;
            centers[2]=xm;
            centers[3]=ym;
            return 1;
        }
        if(dx==0) {
            yc1=ym;
            yc2=ym;
            deltaX=Math.sqrt(r2-0.25*dist2);
            xc1=xm-deltaX;
            xc2=xm+deltaX;
        } else if(dy==0) {
            xc1=xm;
            xc2=xm;
            deltaY=Math.sqrt(r2-0.25*dist2);
            yc1=ym+deltaY;
            yc2=ym-deltaY;
        } else {
            d=Math.sqrt(dist2);
            d1=d/2;
            d2=Math.sqrt(r2-d1*d1);
            double cosTheta=dx/d,sinTheta=dy/d;
            xc1=xm-d2*sinTheta;
            xc2=xm+d2*sinTheta;
            yc1=ym+d2*cosTheta;
            yc2=ym-d2*cosTheta;
        }
        centers[0]=xc1;
        centers[1]=yc1;
        centers[2]=xc2;
        centers[3]=yc2;
        return 1;
    }
    public static boolean sameSide(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){//return true if points (x3,y3) and (x4,y4)
        //are on the same side of line determined by the points (x1, y1) and (x2, y2)
        double cp3=CommonGeometryMethods.crossProduct(x1, y1, x2, y2, x1, y1, x3, y3), cp4=CommonGeometryMethods.crossProduct(x1, y1, x2, y2, x1, y1, x4, y4);
        if(cp3*cp4<0) return false;
        return true;
    }
    public static boolean sameSide(double x0, double y0, double x1, double y1, double x2, double y2){//assumption: three points are collinear.
        //return true if the points (x1,y1) and (x2,y2) are on the same side of the point (x0,y0).
        double dx1=x1-x0,dy1=y1-y0,dx2=x2-x0,dy2=y2-y0;       
        return dx1*dx2>=0&&dy1*dy2>=0;
    }
    public static boolean insideTriangle(double x0,double y0, double x1, double y1, double x2, double y2, double x3, double y3){//return true if (x0,y0) is 
        //located inside of the rianble (x1,y1), (x2,y2), (x3,y3)
        ArrayList<Point> points=new ArrayList();
        points.add(new Point(x1,y1));
        points.add(new Point(x2,y2));
        points.add(new Point(x3,y3));
        points.add(new Point(x0,y0));
        MatOfPoint Pts=new MatOfPoint();
        MatOfInt hull=new MatOfInt();
        Pts.fromList(points);
        Imgproc.convexHull(Pts, hull,true);
        int h=hull.height(),w=hull.width(),i;
        int[] indexes=hull.toArray();
        for(i=0;i<h;i++){
            if(indexes[i]==3) return false;
        }
        return true;
    }
}
