/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;
import java.awt.geom.Point2D.Double;
import utilities.CommonMethods;
import utilities.CommonGeometryMethods;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class Circle_Double {
    static final double machEps=CommonMethods.machEps;
    double cx,cy,r,area;
    double Eps=0.00000001;//practicalEps;
    public Circle_Double(double cx, double cy, double r){
        update(cx,cy,r);
    }
    public void update(double cx, double cy, double r){
        this.cx=cx;
        this.cy=cy;
        this.r=r;
        area=Math.PI*r*r;
    }
    public void setEps(double eps){
        Eps=eps;
    }
    public double getEps(){
        return Eps;
    }
    public double areaDisectedByLine(double x1, double y1, double x2, double y2){
        //if the line defined by (x1,y1) and (x2, y2) intersect the circle, this methods returns the area that is less (or equal) than half circle are.
        double ds=0.;
        double[] pdxy=new double[4];
        int num=getIntersectionPointsByLine(x1,y1,x2,y2,pdxy);
        if(num<2) return 0;
        ds=areaDisectedByLine_OnCirclePoints(pdxy[0],pdxy[1],pdxy[2],pdxy[2]);
        return ds;
    }
    public double areaDisectedByLinesegment(double x1, double y1, double x2, double y2){
        return areaDisectedByLinesegment(x1,y1,x2,y2,0);
    }
    public double areaDisectedByLinesegment(double x1, double y1, double x2, double y2, int sideness){
        //if the line segment defined by (x1,y1) and (x2, y2) intersect the circle.
        //this methods returns zero if the line segments has less than two intersection points with the circle.
        //this methods returns the area that is less (or equal) than half circle area when the sideness is 0.
        //this methods returns the area on the left side of the ray from (x1,y1) to (x2,y2) when
        //the sideness is -1, and returns the area in the right side of the ray when the
        //sideness is 1. (sideness is oposite for the images)

        //checked 2/15/2011
        double ds=0.;
        double[] pdxy=new double[4];
        int num=getIntersectionPointsByLine(x1,y1,x2,y2,pdxy);
        if(java.lang.Double.isNaN(pdxy[2])){//less than 2 intersection points with the line
//                IJ.error("wrong point positions for areaDisectedByLinesegment--1");
            return 0;
        }
        if(!CommonGeometryMethods.withinLineSegment(x1, y1, x2, y2, pdxy[0], pdxy[1])){
            if(!practicallyOverlap(x1,y1,pdxy[0],pdxy[1])) {
//                IJ.error("wrong point positions for areaDisectedByLinesegment--2");
                return 0;
            }
        }
        if(!CommonGeometryMethods.withinLineSegment(x1, y1, x2, y2, pdxy[2], pdxy[3])){
            if(!practicallyOverlap(x2,y2,pdxy[2],pdxy[3])) {
//                IJ.error("wrong point positions for areaDisectedByLinesegment--2");
                return 0;
            }
        }
        ds=areaDisectedByLine_OnCirclePoints(pdxy[0],pdxy[1],pdxy[2],pdxy[3]);
        if(sideness==0) return ds;
        if(sideness*CommonGeometryMethods.sideness(x1, y1, x2, y2, cx, cy)>0) ds=Math.PI*r*r-ds;//the sideness makes difference only when ds is greater than zero
        //The center of the circle is on the side indicated by "sideness", so the area should be
        //larger than half circle.
        return ds;
    }
    public double areaDisectedByLine_OnCirclePoints(double x1, double y1, double x2, double y2){
        //the points (x1,y1) and (x2, y2) are on the circle.
        //checked 11215
        double ds=0.;
        double angle=CommonGeometryMethods.getAngle(x1,y1,cx,cy,x2,y2);
        ds=0.5*r*r*angle;
        ds-=0.5*r*r*Math.sin(angle);
        return ds;
    }
    public double areaDisectedByTwoRays(double x1, double y1,  double x2, double y2, double x3, double y3){//checked on 2/15/2011
        //the point(x2,y2) must be in the circle, and the other points be on or outside of the circle.
        //the method returns the area of the cirle disected by the two rays:
        //(x2,y2) to (x1, y1) and (x2,y2) to (x3,y3).
        if(!inCircle(x2,y2))
            IJ.error("the point (x2,y2) must be in the circle for the method areaDisectedByTwoRays");
        double[] pdxy=new double[4];
        double x1t,x3t,y1t,y3t;
        int num1=getIntersectionPoint_Linesegment_OneInsiderOneOutsider(x2,y2,x1,y1,pdxy);
        if(num1==0){
            return 0;
        }
        x1t=pdxy[2];
        y1t=pdxy[3];
        
        int num2=getIntersectionPoint_Linesegment_OneInsiderOneOutsider(x2,y2,x3,y3,pdxy);
        if(num2==0){
            return 0;
        }
        x3t=pdxy[2];
        y3t=pdxy[3];
        double ds=0;
        if(num1!=-1&&num2!=-1) ds=CommonGeometryMethods.getTriangleArea(x1t, y1t, x2, y2, x3t, y3t);
        ds+=areaDisectedByLine_OnCirclePoints(x1t,y1t,x3t,y3t);
        return ds;
    }

    public double areaDisectedByTwoRays0(double x1, double y1,  double x2, double y2, double x3, double y3){//checked on 2/15/2011
        //the point(x2,y2) must be in the circle, and the other points be on or outside of the circle.
        //the method returns the area of the cirle disected by the two rays:
        //(x2,y2) to (x1, y1) and (x2,y2) to (x3,y3).
        if(!inCircle(x2,y2))
            IJ.error("the point (x2,y2) must be in the circle for the method areaDisectedByTwoRays");
        double[] pdxy1=new double[4],pdxy2=new double[4];
        double x1t,y1t,x3t,y3t;
        if(x1==4.5&&y1==1.5&&x2==3.5&&y2==2.5&&x3==3.5&&y1==1.5&&cx==3.1&&cy==2.5&&r==0.4){
            x1=x1;
        }
        int num1=getIntersectionPointsByLine(x2,y2,x1,y1,pdxy1);

        if(java.lang.Double.isNaN(pdxy1[0])){//the line has no intersection point with the circle
            IJ.error("wrong point positions for the method areaDisectedByTwoRays--1");
            return 0;
        }
        if(java.lang.Double.isNaN(pdxy1[2])){//the line has one intersection point with the circle
                if(!practicallyOverlap(x2,y2,pdxy1[0],pdxy1[1])){
                IJ.error("wrong point positions for the method areaDisectedByTwoRays--1");
                return 0;
            }else{
                pdxy1[2]=pdxy1[0];
                pdxy1[3]=pdxy1[1];
                num1=-1;
            }
        }else{
            if(!CommonGeometryMethods.withinLineSegment(x2, y2, x1, y1, pdxy1[2],pdxy1[3])) {
                if(!practicallyOverlap(x1,y1,pdxy1[2],pdxy1[3])){
//                        IJ.error("wrong point positions for the method areaDisectedByTwoRays--2");
                    pdxy1[2]=java.lang.Double.NaN;
                    pdxy1[3]=java.lang.Double.NaN;
                }else{
                    num1=1;
                }
            }else{
                num1=1;
            }
        }
        x1t=pdxy1[2];
        y1t=pdxy1[3];

        int num2=getIntersectionPointsByLine(x2,y2,x3,y3,pdxy2);
        if(java.lang.Double.isNaN(pdxy2[0])){//the line has no intersection point with the circle
            IJ.error("wrong point positions for the method areaDisectedByTwoRays--1");
            return 0;
        }
        if(java.lang.Double.isNaN(pdxy2[2])){//the line has one intersection point with the circle
                if(!practicallyOverlap(x2,y2,pdxy2[0],pdxy2[1])){
                IJ.error("wrong point positions for the method areaDisectedByTwoRays--1");
                return 0;
            }else{
                pdxy2[2]=pdxy2[0];
                pdxy2[3]=pdxy2[1];
                num2=-1;
            }
        }else{//num==2
            if(!CommonGeometryMethods.withinLineSegment(x2, y2, x3, y3, pdxy2[2],pdxy2[3])) {
                if(!practicallyOverlap(x3,y3,pdxy2[2],pdxy2[3])){
                    pdxy2[2]=java.lang.Double.NaN;
                    pdxy2[3]=java.lang.Double.NaN;
                }else{
                    num2=1;
                }
            }else{
                num2=1;
            }
        }
        x3t=pdxy2[2];
        y3t=pdxy2[3];
        if(java.lang.Double.isNaN(x1t)||java.lang.Double.isNaN(x3t)){
            if(java.lang.Double.isNaN(x1t)){
                if(num2==1){
                    return areaDisectedByLine_OnCirclePoints(pdxy2[0],pdxy2[1],pdxy2[2],pdxy2[3]);
                }
            }else{
                if(num1==1){
                    return areaDisectedByLine_OnCirclePoints(pdxy1[0],pdxy1[1],pdxy1[2],pdxy1[3]);
                }
            }
        }
        if(num1==-1){
            if(num2==1){
                return areaDisectedByLine_OnCirclePoints(pdxy2[0],pdxy2[1],pdxy2[2],pdxy2[3]);
            }else{
                return 0;
            }
        }
        if(num2==-1){
            if(num1==1){
                return areaDisectedByLine_OnCirclePoints(pdxy1[0],pdxy1[1],pdxy1[2],pdxy1[3]);
            }else{
                return 0;
            }
        }

        return areaDisectedByTwoRays_OnCirclePoints(x1t,y1t,x2,y2,x3t,y3t);
    }

    int getIntersectionPoint_Linesegment_OneInsiderOneOutsider(double x1, double y1, double x2, double y2, double[] pdxy){
        //(x1,y1) is inside circle and (x2,y2) is outside.
        if(x1==3.5&&y1==2.5&&x2==4.5&&y2==1.5&&cx==3.1&&cy==2.5&&r==0.4){
            x1=x1;
        }
        int num=getIntersectionPointsByLine(x1, y1, x2, y2, pdxy);
        if(java.lang.Double.isNaN(pdxy[0])){//the line has no intersection point with the circle
            IJ.error("getIntersectionPoint_Linesegment_OneInsiderOneOutside--1");
            return 0;
        }
        if(java.lang.Double.isNaN(pdxy[2])){//the line has one intersection point with the circle
                if(!practicallyOverlap(x1,y1,pdxy[0],pdxy[1])){
                IJ.error("getIntersectionPoint_Linesegment_OneInsiderOneOutside--2");
                return 0;
            }else{//(x1,y1) is practically on the circle and could have beed detected as insider due to
                    //numerical errors
                CommonMethods.swapElements(pdxy, 0, 2);
                CommonMethods.swapElements(pdxy, 1, 3);
                num=-1;
            }
        }else{//there two intersection points with the line
            if(!CommonGeometryMethods.withinLineSegment(x1, y1, x2, y2, pdxy[2],pdxy[3])) {
                if(practicallyOverlap(x2,y2,pdxy[2],pdxy[3])){//(x2,y2) has to be practically on the circle
                    if(!practicallyOnCircle(x2,y2)){//there should not be such a case unless something went wrong
                        IJ.error("getIntersectionPoint_Linesegment_OneInsiderOneOutside--3");
                        pdxy[2]=java.lang.Double.NaN;
                        pdxy[3]=java.lang.Double.NaN;
                        num=0;
                    }else{//(x2,y2) is practically on the circle and could have beed detected as insider due to
                    //numerical errors
                        num=-2;
                    }
                }else if(practicallyOverlap(x1,y1,pdxy[2],pdxy[3])){//(x1,y1) has to be practically on the circle
                    if(!practicallyOnCircle(x1,y1)){//there should not be such a case unless something went wrong
                        IJ.error("getIntersectionPoint_Linesegment_OneInsiderOneOutside--4");
                        pdxy[2]=java.lang.Double.NaN;
                        pdxy[3]=java.lang.Double.NaN;
                        num=0;
                    }else{//(x1,y1) is practically on the circle and could have beed detected as insider due to 
                    //numerical errors 
                    //and 
                        num=-1;
                    }
                }else{//there should not be such a case unless something went wrong
                        IJ.error("getIntersectionPoint_Linesegment_OneInsiderOneOutside--3");
                        pdxy[2]=java.lang.Double.NaN;
                        pdxy[3]=java.lang.Double.NaN;
                        num=0;
                }
            }else{//normal situation
                num=1;
            }
        }
        return num;
    }

    public double areaDisectedByTwoRays_OnCirclePoints(double x1, double y1,  double x2, double y2, double x3, double y3){
        //the point(x2,y2) must be in the circle, and the other two points are on the circle
        //the two rays are (x2,y2) to (x1, y1) and (x2,y2) to (x3,y3).

        //checked 11211
        double ds=0.;
        ds=areaDisectedByLine_OnCirclePoints(x1,y1, x3, y3);
        double angle=CommonGeometryMethods.getAngle(x1, y1, x2, y2, x3, y3);
        if(angle<0) return 0;//the point (x2,y2) is on the circle.
        ds+=0.5*Math.sqrt(((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))*((x3-x2)*(x3-x2)+(y3-y2)*(y3-y2)))*Math.sin(angle);
        return ds;
    }
    public double areaWithinSquare(double x, double y, double dLen){//checked 11211
        double ds=areaInTriangle(x,y+dLen,x,y,x+dLen,y);
        ds+=areaInTriangle(x,y+dLen,x+dLen,y+dLen,x+dLen,y);
        return ds;
    }
    public double areaInTriangle(double x1, double y1, double x2, double y2, double x3, double y3){
        double ds=0;
        double pdxy[]=new double[6],pdxy1[]=new double[4];
        int num=pointsInCircle(x1,y1,x2,y2,x3,y3,pdxy);
        double x1t,y1t,x2t,y2t;
        int i,o;

        switch (num){
            case 3:
                ds=CommonGeometryMethods.getTriangleArea(x1, y1, x2, y2, x3, y3);
                break;
            case 2:
                if(practicallyOnCircle(pdxy[4],pdxy[5])) return CommonGeometryMethods.getTriangleArea(x1, y1, x2, y2, x3, y3);
                ds=CommonGeometryMethods.getTriangleArea(pdxy[0],pdxy[1],pdxy[4],pdxy[5],pdxy[2],pdxy[3]);
                ds-=uncoveredAreaOfTriangle_OneOutsiderVertex(pdxy[0],pdxy[1],pdxy[4],pdxy[5],pdxy[2],pdxy[3]);
                break;
            case 1:
                ds=areaDisectedByTwoRays(pdxy[2],pdxy[3],pdxy[0],pdxy[1],pdxy[4],pdxy[5]);//this block can be replaced by finding the on circle points and computing the
                //triangle area
                ds-=areaDisectedByLinesegment(pdxy[2],pdxy[3],pdxy[4],pdxy[5]);
                break;
            case 0:
                ds=0;
                int sideness;
                double dst;
                if(CommonGeometryMethods.withinTriangle(x1, y1, x2, y2, x3, y3, cx, cy)){
                    sideness=CommonGeometryMethods.sideness(x1, y1, x2, y2, cx,cy);
                    ds=area;
                    dst=areaDisectedByLinesegment(x1,y1,x2,y2,-sideness);
                    ds-=dst;
                    dst=areaDisectedByLinesegment(x2,y2,x3,y3,-sideness);
                    ds-=dst;
                    dst=areaDisectedByLinesegment(x3,y3,x1,y1,-sideness);
                    ds-=dst;
                }else{
                    sideness=CommonGeometryMethods.sideness(x1, y1, x2, y2, x3, y3);
                    ds=areaDisectedByLinesegment(x1,y1,x2,y2,sideness);
                    dst=areaDisectedByLinesegment(x2,y2,x3,y3,sideness);
                     if(ds>0){
                        if(dst>0) {
                            dst=area-dst;
                            ds-=dst;
                        }
                    } else if (dst > 0)
                        ds=dst;

                    dst=areaDisectedByLinesegment(x3,y3,x1,y1,sideness);
                    if(ds>0){
                        if(dst>0) {
                            dst=area-dst;
                            ds-=dst;
                        }
                    }
                    else if(dst>0){
                        ds=dst;
                    }
                }
                break;
        }
        return ds;
    }
    public double uncoveredAreaOfTriangle_OneOutsiderVertex(double x1, double y1, double x2, double y2, double x3, double y3){//checked on 2/1/2011 and on 2/15/2011
        //the vertex (x2,y2) is outside of the circle, and the other two vertices are in the circle.
        if(inCircle(x2,y2))
            IJ.error("the point (x2,y2) must be out of the circle for the uncoveredAreaOfTriangle_OneOutsiderVertex");
        double[] pdxy=new double[4];
        double x1t,x3t,y1t,y3t;
        int num1=getIntersectionPoint_Linesegment_OneInsiderOneOutsider(x1,y1,x2,y2,pdxy);
        if(num1==0||num1==-2){
            return 0;
        }
        x1t=pdxy[2];
        y1t=pdxy[3];

        int num2=getIntersectionPoint_Linesegment_OneInsiderOneOutsider(x3,y3,x2,y2,pdxy);
        if(num2==0||num2==-2){
            return 0;
        }
        x3t=pdxy[2];
        y3t=pdxy[3];
        return uncoveredAreaOfTriangle_TwoOnCircleOneOutsiderVertices(x1t,y1t,x2,y2,x3t,y3t);
    }
    public double uncoveredAreaOfTriangle_TwoOnCircleOneOutsiderVertices(double x1, double y1, double x2, double y2, double x3, double y3){
        //point (x1,y1) and (x3,y3) are on the circle, and the point (x2, y2) is outside of the circle. 
        double ds=CommonGeometryMethods.getTriangleArea(x1, y1, x2, y2, x3, y3);
        ds-=areaDisectedByLine_OnCirclePoints(x1,y1,x3,y3);
        return ds;
    }
    public int getIntersectionPointsByLineSegment(double x1, double y1, double x2, double y2, double[] pdxy){//checked on 2/15/2011
        int num0=getIntersectionPointsByLine(x1,y1,x2,y2,pdxy),num=0;
        double x,y;
        int i,o;
        if(num0==0) return 0;
        if(num0==1||num0==-1){
            x=pdxy[0];
            y=pdxy[1];
            if(CommonGeometryMethods.withinLineSegment(x1, y1, x2, y2, x, y))
                return -1;
            else 
                return 0;
        }
        //num0==2
        for(i=0;i<num0;i++){
            o=2*i;
            x=pdxy[o];
            y=pdxy[o+1];
            if(CommonGeometryMethods.withinLineSegment(x1, y1, x2, y2, x, y)){
                if(i>num){
                    CommonMethods.swapElements(pdxy,2*num,o);
                    CommonMethods.swapElements(pdxy,2*num+1,o+1);
                }
                num++;
            }
        }
        return num;
    }
    public int getIntersectionPointsByLine(double x1, double y1, double x2, double y2, double[] pdxy){
        //pdxy[0]=x1, pdxy[1]=y1, pdxy[2]=x2, pdxy[3]=y2; (x1, y1) and (x2, y2) are the intersection points.
        //returning the number of intersection points. -1 indicating practical one

        //When there are two intersection points, the two points will be rearranged so that
        //the direction from the point (pdxy[0],pdxy[1]) to the point (pdxy[2],pdxy[3])
        //the same as the direction from the point (x1,y1) to (x2,y2).

        //checked 11211
        double dist;
        double x1t,y1t,x2t,y2t,dx,dy;

        int n,ndirection=2;
        if(x1==x2) ndirection=0;
        if(y1==y2) ndirection=1;

        for(int i=0;i<4;i++){
            pdxy[i]=java.lang.Double.NaN;
        }

        switch (ndirection){
            case 0://x1==x2
                dist=Math.abs(cx-x1);
                if(dist>r){
                    n=0;
                    break;
                }
                if(dist==r){
                    pdxy[0]=x1;
                    pdxy[1]=cy;
                    n=1;
                    break;
                }
                dy=Math.sqrt(r*r-dist*dist);
                if(y2>y1){
                    y1t=cy-dy;
                    y2t=cy+dy;
                }else{
                    y2t=cy-dy;
                    y1t=cy+dy;
                }
                pdxy[0]=x1;
                pdxy[1]=y1t;
                pdxy[2]=x1;
                pdxy[3]=y2t;
                n=2;
                break;
            case 1://y1==y2
                dist=Math.abs(cy-y1);
                if(dist>r){
                    n=0;
                    break;
                }
                if(dist==r){
                    pdxy[0]=cx;
                    pdxy[1]=y1;
                    n=1;
                    break;
                }
                dx=Math.sqrt(r*r-dist*dist);
                if(x2>x1){
                    x1t=cx-dx;
                    x2t=cx+dx;
                }else{
                    x2t=cx-dx;
                    x1t=cx+dx;
                }
                pdxy[0]=x1t;
                pdxy[1]=y1;
                pdxy[2]=x2t;
                pdxy[3]=y1;
                n=2;
                break;
            default:
                double k=(y2-y1)/(x2-x1);
                double A=1+k*k,B=2*(y1*k-k*cy-cx-k*k*x1);
                double C=cx*cx+y1*y1-2*y1*k*x1+2*k*x1*cy-2*cy*y1+cy*cy+k*k*x1*x1-r*r;
                double D=B*B-4*A*C;
                if(D<0) {
                    if(Math.abs(D)>Eps){
                        n=0;
                    }else{
                        x1t=0.5*B/A;
                        pdxy[0]=x1t;
                        pdxy[1]=y1+k*(x1t-x1);
                        n=-1;//practical one
                    }
                    break;
                }
                if(D==0) {
                    x1t=0.5*B/A;
                    pdxy[0]=x1t;
                    pdxy[1]=y1+k*(x1t-x1);
                    n=1;
                    break;
                }
                //D>0
                if(x1>x2){
                    x1t=(Math.sqrt(D)-B)/(2*A);
                    x2t=(-Math.sqrt(D)-B)/(2*A);
                }else{
                    x2t=(Math.sqrt(D)-B)/(2*A);
                    x1t=(-Math.sqrt(D)-B)/(2*A);
                }
                pdxy[0]=x1t;
                pdxy[2]=x2t;
                pdxy[1]=y1+k*(x1t-x1);
                pdxy[3]=y1+k*(x2t-x1);
                n=2;//checked on 1/31/11, correck
                break;
        }
        return n;
    }
    public boolean inOrOnCircle(double x1, double y1){
        return ((x1-cx)*(x1-cx)+(y1-cy)*(y1-cy)<=r*r);
    }
    public boolean inCircle(double x1, double y1){
        return ((x1-cx)*(x1-cx)+(y1-cy)*(y1-cy)<r*r);
    }
    public int pointsInCircle(double x1, double y1, double x2, double y2, double x3, double y3,double pdxy[]){
        //previously checked 11211
        int num=0,o;
        if(inCircle(x1,y1)){
            pdxy[0]=x1;
            pdxy[1]=y1;
            num++;
        }else{
            pdxy[4]=x1;
            pdxy[5]=y1;
        }
        if(inCircle(x2,y2)){
            o=2*num;
            pdxy[o]=x2;
            pdxy[o+1]=y2;
            num++;
        }else{
            o=2*(2+num);
            pdxy[o-2]=x2;
            pdxy[o-1]=y2;
        }
        if(inCircle(x3,y3)){
            o=2*num;
            pdxy[o]=x3;
            pdxy[o+1]=y3;
            num++;
        }else{
            o=2*(1+num);
            pdxy[o-2]=x3;
            pdxy[o-1]=y3;
        }
        return num;
    }
    public void setCenter(double x, double y){
        cx=x;
        cy=y;
    }
    public boolean practicallyOverlap(double x1, double y1, double x2, double y2){
        return CommonGeometryMethods.dist2(x1, y1, x2, y2)<Eps;
    }
    public boolean practicallyOnCircle(double x, double y){
        return Math.abs(CommonGeometryMethods.dist2(x, y, cx, cy)-r*r)<Eps;
    }
}
