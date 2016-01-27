/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.awt.Point;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
import utilities.CommonMethods;
import ij.IJ;
import utilities.DecisionMakers.IsInside;
import utilities.ArrayofArrays.PointArray;
import java.util.Formatter;
import utilities.QuickFormatter;
import utilities.io.PrintAssist;
import ij.ImagePlus;
import utilities.CommonStatisticsMethods;
import java.awt.Color;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.DoublePair;
import utilities.Geometry.Point2D;
import utilities.statistics.PolynomialRegression;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
/**
 *
 * @author Taihao
 */
public class ContourFollower {
        //this contour follower follows the contour by keeping the object on the right hand side.
    int[][] pixels;
    int w,h;
    public ContourFollower(int[][] pixels){
        this.pixels=pixels;
        h=pixels.length;
        if(h>0) w=pixels[0].length;
    }
    static public final int xnxn=1, xnxp=2, xnyn=3, xnyp=4;
    static public final int xpxn=5, xpxp=6, xpyn=7, xpyp=8;
    static public final int ynxn=9, ynxp=10, ynyn=11, ynyp=12;
    static public final int ypxn=13, ypxp=14, ypyn=15, ypyp=16;
    static public final int contourDirection[]={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    static public final double SubpixelContourPrecision=0.00000000001;
    static private int[][] moves;
    //these parameters describe the direction of a contour at a given contour point. The contour direction at a given contour point is
    //determined by the given contour point p, the contour point before it (p0) and the contour point after it (p1). It is specified by
    //the directions of the two vectors (P-P0) and (P1-P). xp and xn stand for the direction of a vector along the positive and negative
    //directions of x axis, respectively. Similar for yp and yn.
    static public final int frontLeft=0,front=1,frontRight=2,right=3,rearRight=4,rear=5,rearLeft=6,left=7;
    //these parameters describe the relative position of a point in relation to with its directly contacting contour point.
    static boolean bCountImageEdges=false;
    public static boolean innerContour(ArrayList<Point> contour){
        //this contour follower follows the contour by keeping the object on the right hand side.
        boolean inner=true;
        int size=contour.size();
        ArrayList <Integer> angles= new ArrayList <Integer>(),angles0= new ArrayList <Integer>(),angles1= new ArrayList <Integer>(), asums=new ArrayList <Integer>();
        int i,angle,asum=0,i0,i1,a0,a1;
        Point p0,p,p1;
        for(i=0;i<size;i++){
            i0=circularAddition(size,i,-1);
            i1=circularAddition(size,i,1);
            p=contour.get(i);
            p0=contour.get(i0);
            p1=contour.get(i1);
            a0=angle(p.x-p0.x,p.y-p0.y);
            a1=angle(p1.x-p.x,p1.y-p.y);
            angle=dAngle(a0,a1);
            angles.add(angle);
            asum+=angle;
            asums.add(asum);
            angles0.add(a0);
            angles1.add(a1);
        }
        if(asum==-4) inner=false;
        return inner;
    }
    static int circularAddition(int size, int position, int delta){
        int sum=(position+delta)%size;
        if(sum<0) sum+=(-sum/size+1)*size;
        return sum;
    }
    static void setEdgeCountingMode(boolean bMode){
        bCountImageEdges=bMode;
    }
    public static ArrayList<Point> getContour_Out(int[][] pixels, int w, int h, Point p0o, int lo, int hi){//returns the out contour
        ArrayList<Point> contour;
        contour=getContour_Out(pixels,w,h,p0o,lo,hi,false);
//        removeOffBoundPoints(w,h,contour);2/26/2011
        return contour;
    }
    public static ArrayList<Point> getContour_Out(int[][] pixels, int w, int h, Point p0o, int lo, int hi, boolean bEdgeMode){//returns the out contour
        boolean bt=bCountImageEdges;
        setEdgeCountingMode(bEdgeMode);
        Point p0=new Point(p0o);
        if(!isInside(pixels, w, h, p0,lo,hi)){
            IJ.error("getContour needs to input a point contained inside the object");
            return new ArrayList<Point>();
        }
        ArrayList<Point> contour;
        Point po=new Point(p0);
        stepOut(pixels, w, h, p0,0,1,lo,hi);
        Point p=new Point(p0);
        Point p1=new Point(p0);
        Point pt=new Point(p0);
        Point po0=new Point(),po1=new Point();
        p1.translate(0, -1);
        int rightTurns=0,moveBacks=0;
        while(true){
            contour=getContour(pixels,w,h,p0,p1,lo,hi,bEdgeMode);
            if(!innerContour(contour))break;
            //followed an iner contour, need to cross the object to the outer area
            stepInto(pixels, w, h, p0,0,1,lo,hi);
            stepOut(pixels, w, h, p0,0,1,lo,hi);
            contour.clear();
            p.setLocation(p0);
            p1.setLocation(p0);
            p1.translate(0, -1);
        }
        setEdgeCountingMode(bt);
        return contour;
    }
    public static ArrayList<Point> getContour_Out(TwoDFunction fun,double x0, double y0,double dx, double dy, Point p0o, DoubleRange dr){//returns the out contour
        Point p0=new Point(p0o);
//        if(!isInside(pixels, w, h, p0,lo,hi)){
        if(!isInside(fun,x0,y0,dx,dy,p0,dr)){
            IJ.error("getContour needs to input a point contained inside the object");
            return new ArrayList<Point>();
        }
        ArrayList<Point> contour;
        Point po=new Point(p0);
//        stepOut(pixels, w, h, p0,0,1,lo,hi);
        stepOut(fun,x0,y0,dx,dy,p0,0,1,dr);
        Point p=new Point(p0);
        Point p1=new Point(p0);
        Point pt=new Point(p0);
        Point po0=new Point(),po1=new Point();
        p1.translate(0, -1);
        int rightTurns=0,moveBacks=0;
        while(true){
            contour=getContour(fun,x0,y0,dx,dy,p0,p1,dr);
            if(!innerContour(contour))break;
            //followed an iner contour, need to cross the object to the outer area
            stepInto(fun,x0,y0,dx,dy,p0,0,1,dr);
            stepOut(fun,x0,y0,dx,dy,p0,0,1,dr);
            contour.clear();
            p.setLocation(p0);
            p1.setLocation(p0);
            p1.translate(0, -1);
        }
        return contour;
    }
    public static int getContour_Out(TwoDFunction fun,double x0, double y0,double dx, double dy, Point p0o, DoubleRange dr,ArrayList<Point> Contour, ArrayList<DoublePair> Contour_Subpixel){//returns the out contour
        Point p0=new Point(p0o);
//        if(!isInside(pixels, w, h, p0,lo,hi)){
        if(!isInside(fun,x0,y0,dx,dy,p0,dr)){
 //           IJ.error("getContour needs to input a point contained inside the object");
            return -1;
        }
        Contour_Subpixel.clear();
        Contour.clear();
        ArrayList<Point> contour;
        Point po=new Point(p0);
//        stepOut(pixels, w, h, p0,0,1,lo,hi);
        stepOut(fun,x0,y0,dx,dy,p0,0,1,dr);
        Point p=new Point(p0);
        Point p1=new Point(p0);
        Point pt=new Point(p0);
        Point po0=new Point(),po1=new Point();
        p1.translate(0, -1);
        int rightTurns=0,moveBacks=0,i,len;
        while(true){
            contour=getContour(fun,x0,y0,dx,dy,p0,p1,dr);
            if(contour==null){//this patch is for temporally to get the breaking point going.//13130
                CircleImage circle=new CircleImage(20);
                DoublePair dp=fun.getPeak();
                circle.setCenter(new Point((int)dp.left,(int)dp.right));
                contour=circle.getOuterContour();
                break;
            }
            if(!innerContour(contour))break;
            //followed an iner contour, need to cross the object to the outer area
            stepInto(fun,x0,y0,dx,dy,p0,0,1,dr);
            stepOut(fun,x0,y0,dx,dy,p0,0,1,dr);
            contour.clear();
            p.setLocation(p0);
            p1.setLocation(p0);
            p1.translate(0, -1);
        }
        for(i=0;i<contour.size();i++){
            Contour.add(contour.get(i));
        }
        getSubpixelContour(fun,x0,y0,dx,dy,dr,Contour,Contour_Subpixel);
        return 1;
    }

    public static ArrayList<Point> getContour(int[][] pixels, int w, int h, Point p0, Point p1, intRange ir){
        return getContour(pixels,w,h,p0, p1,ir.getMin(),ir.getMax());
    }
/* need to make this method work well
    public static Point oppositeSide_X(ArrayList<Point> contour, Point p0, int direction){
        int size=contour.size();
        int x0=p0.x,y0=p0.y,x=x0-1;
        int i;
        int xn=0;
        boolean first=true;
        Point p=null,pt=null,p1=new Point();
        for(i=0;i<size;i++){
            p=contour.get(i);
            x=p.x;
            if(p.y==y0&&direction*(x-x0)>=0) {
                p1.setLocation(x+direction, y0);
                if(sideness(contour,i,circularAddition(size,i,1),p1)==left||sideness(contour,i,circularAddition(size,i,-1),p1)==left){
                    if(first){
                        pt=p;
                        xn=x;
                    }else{
                        if(direction*(xn-x)>0) {
                            pt=p;
                            xn=x;
                        }
                    }
                }
            }
        }
        return pt;
    }
*/
    public static ArrayList<Point> getContour(int[][] pixels, int w, int h, Point p0, Point p1, int lo, int hi){//contour can contain offBound points if bCountImageEdges is set to true
        return getContour(pixels,w,h,p0,p1,lo,hi,false);
    }
    public static ArrayList<Point> getContour(int[][] pixels, int w, int h, Point p0, Point p1, int lo, int hi,boolean bCountEdge){//contour can contain offBound points if 
        //bCountImageEdges is set to true. Otherwise, the positions on the image edge are considered as outside regardless of the actual pixel values
        //p0 and p1 should be directly contacting and should be outside and inside the object, respectively
        //p0 will be the last point of the array contour.
        boolean bTemp=bCountImageEdges;
        bCountImageEdges=bCountEdge;
        if(!isInside(pixels, w, h, p1,lo,hi)||isInside(pixels, w, h, p0,lo,hi)||(Math.abs(p1.x-p0.x)+Math.abs(p1.y-p0.y))!=1){
            IJ.error("For the methods getContour: the input point p1 should be inside and p0 should be outside of the object, and they should be direct contacting neighbors!");
            return null;
        }
        ArrayList<Point> contour=new ArrayList<Point>();
        Point p=new Point(p0);
        Point pt=new Point(p0);
        Point po0=new Point(),po1=new Point();
        int rightTurns=0,moveBacks=0;
        int moveType=-1;
        int cPoints;//the number of contour points
        int steps;
        Point cp=new Point(), cp0=new Point();

        cPoints=0;
        steps=0;
        rightTurns=0;
        moveBacks=0;
        while(!p1.equals(po1)||!p.equals(po0)){
            steps++;
            if(isInside(pixels, w, h, p1,lo,hi)){
                if(moveBacks==0){
                    po1.setLocation(p1);
                    po0.setLocation(p);
                }
                pt.setLocation(p1);//moving back;
                p1.setLocation(p);
                p.setLocation(pt);
                moveType=0;
                moveBacks++;
            }else{
                if(moveType==1){
                    contour.add(new Point(p1));
                    cp0.setLocation(cp);
                    cp.setLocation(p1);
                    cPoints++;
                }
                pt.setLocation(p1);//turning right
                p1.translate(p.y-p1.y, p1.x-p.x);
                p.setLocation(pt);
                moveType=1;
                rightTurns++;
            }
        }
        if(contour.size()==0) contour.add(p0);//one point contour, an inner contour in the case of a single pixel whole.
        bCountImageEdges=bTemp;
        return contour;
    }
    public static boolean isInside(TwoDFunction fun,double x0, double y0,double dx, double dy, Point p, DoubleRange dr){
        double x=x0+dx*p.x,y=y0+dy*p.y;
        double z=fun.func(x, y);
        return dr.contains(z);        
    }
    public static ArrayList<Point> getContour(TwoDFunction fun,double x0, double y0,double dx, double dy, Point p0, Point p1, DoubleRange dr){
        boolean bTemp=bCountImageEdges;
        if(!isInside(fun,x0,y0,dx,dy,p1,dr)||isInside(fun,x0,y0,dx,dy,p0,dr)||(Math.abs(p1.x-p0.x)+Math.abs(p1.y-p0.y))!=1){
//            IJ.error("For the methods getContour: the input point p1 should be inside and p0 should be outside of the object, and they should be direct contacting neighbors!");
            return null;
        }
        ArrayList<Point> contour=new ArrayList<Point>();
        Point p=new Point(p0);
        Point pt=new Point(p0);
        Point po0=new Point(),po1=new Point();
        int rightTurns=0,moveBacks=0;
        int moveType=-1;
        int cPoints;//the number of contour points
        int steps;
        Point cp=new Point(), cp0=new Point();

        cPoints=0;
        steps=0;
        rightTurns=0;
        moveBacks=0;
        while(!p1.equals(po1)||!p.equals(po0)){
            steps++;
            if(isInside(fun,x0,y0,dx,dy,p1,dr)){
                if(moveBacks==0){
                    po1.setLocation(p1);
                    po0.setLocation(p);
                }
                pt.setLocation(p1);//moving back;
                p1.setLocation(p);
                p.setLocation(pt);
                moveType=0;
                moveBacks++;
            }else{
                if(moveType==1){
                    contour.add(new Point(p1));
                    cp0.setLocation(cp);
                    cp.setLocation(p1);
                    cPoints++;
                }
                pt.setLocation(p1);//turning right
                p1.translate(p.y-p1.y, p1.x-p.x);
                p.setLocation(pt);
                moveType=1;
                rightTurns++;
            }
        }
        if(contour.size()==0) contour.add(p0);//one point contour, an inner contour in the case of a single pixel whole.
        bCountImageEdges=bTemp;
        return contour;
    }
    public static void removeOffBoundPoints(int w, int h, ArrayList<Point> points){
        int len=points.size();
        Point p;
        for(int i=len-1;i>=0;i--){
            p=points.get(i);
            if(offBound(w,h,p.x,p.y))points.remove(i);
        }
    }
    public static void removeOffBoundPoints(intRange xRange, intRange yRange, ArrayList<Point> points){
        int len=points.size();
        Point p;
        for(int i=len-1;i>=0;i--){
            p=points.get(i);
            if(!xRange.contains(p.x)||!yRange.contains(p.y))points.remove(i);
        }
    }
    static int angle(int dx, int dy){//
        int a=0;
        if(dx==0){
            if(dy==1){
                a=3;
            }else{//dy==-1
                a=1;
            }
        }else{//dy==0
            if(dx==1){
                a=0;
            }else{//dx==-1
                a=2;
            }
        }
        return a;
    }
    static int dAngle(int a1, int a2){
        int a=a2-a1;
        switch (a){
            case 3:
                a=-1;
                break;
            case -3:
                a=1;
                break;
            case 2:
                a=2;
                break;
            case -2:
                a=2;
                break;
            default:
                break;
        }
        return a;
    }
    static void stepOut(int[][] pixels, int w, int h, Point p, int dx,int dy, int lo, int hi){
        while(isInside(pixels, w,h,p,lo,hi)){
            p.x+=dx;
            p.y+=dy;
        }
    }
    static void stepInto(int[][] pixels, int w, int h, Point p, int dx,int dy, int lo, int hi){
        while(!isInside(pixels, w, h, p,lo,hi)){
            p.x+=dx;
            p.y+=dy;
            if(p.x<0||p.x>=w||p.y<0||p.y>=h) break;
        }
    }

    static void stepOut(TwoDFunction fun,double x0, double y0,double dx, double dy, Point p, int nDx, int nDy, DoubleRange dr){
        int iter=0,maxIter=20000;
        while(isInside(fun,x0,y0,dx,dy,p,dr)){
            iter++;
            if(iter>maxIter) break;
            p.x+=nDx;
            p.y+=nDy;
        }
        if(iter>maxIter){
            iter=iter;
        }
    }
    static void stepInto(TwoDFunction fun,double x0, double y0,double dx, double dy, Point p, int nDx, int nDy, DoubleRange dr){
        while(!isInside(fun,x0,y0,dx,dy,p,dr)){
            p.x+=nDx;
            p.y+=nDy;
        }
    }

    static boolean isInside(int[][] pixels, int w, int h, int x, int y, int lo, int hi){
        if(!bCountImageEdges){
            if(onEdges(w,h,x,y)) return false;
        }
        if(offBound(w,h,x,y))return false;
        int pixel=pixels[y][x];
        return (pixel>=lo&&pixel<=hi);
    }

    static public boolean onEdges(int w,int h, int x, int y){
        return (x==0||x==w-1||y==0||y==h-1);
    }
    static public boolean offBound(int w, int h, int x, int y){
        return (x<0||x>=w||y<0||y>=h);
    }

    static boolean isInside(int[][] pixels, int w, int h, Point p, int lo, int hi){
        return isInside(pixels,w,h,p.x,p.y,lo,hi);
    }
    static public Point moveBack(Point p0, Point p){
        return p0;
    }
    static public Point moveForward(Point p0, Point p){
        int dx=p.x-p0.x;
        int dy=p.y-p0.y;
        return new Point(p.x+dx,p.y+dy);
    }
    static public Point turnLeft(Point p0, Point p){
        int dx0=p.x-p0.x;
        int dy0=p.y-p0.y;
        int dx=dy0,dy=-dx0;
        return new Point(p.x+dx,p.y+dy);
    }
    static int leftTurnX(int dx0, int dy0){
        return dy0;
    }
    static int leftTurnY(int dx0, int dy0){
        return -dx0;
    }
    static public Point turnRight(Point p0, Point p){
        int dx0=p.x-p0.x;
        int dy0=p.y-p0.y;
        int dx=-dy0,dy=dx0;
        return new Point(p.x+dx,p.y+dy);
    }
    static int rightTurnX(int dx0, int dy0){
        return -dy0;
    }
    static int rightTurnY(int dx0, int dy0){
        return dx0;
    }
    public static boolean isInside(ArrayList<Point> contour, Point pc, Point p, int outer){//this methods assumes pc (part of the contour) and p are directly contacting neighbors.
        //parameter outer indicates whether the contour is a inner contour (outer=-1) or a outer contour(outer=1).
        //this method detect whether a point directly contacting with a contour point is in our outside of the object enclosed by the contour.
        if(outer==0) {
            if(innerContour(contour)) outer=-1;
            else outer=1;
        }

        int minSize=4;
        if(contour.size()<=minSize){
            if(outer==1) return false;
            else return true;
        }
        ArrayList<Integer> indexes=CommonMethods.findPoint(contour, pc);
        int sideness;
        int i, size=indexes.size(),index,indexp,indexn,index0,index1;
        for(i=0;i<size;i++){
            index=indexes.get(i);
            indexp=CommonMethods.circularAddition(contour.size(), index, 1);//higher index
            indexn=CommonMethods.circularAddition(contour.size(), index, -1);//lower index
            index0=index;
            index1=indexp;
            if(!contacting(p,contour.get(index1))){
                if(contacting(p,contour.get(indexn))){
                    index0=indexn;
                    index1=index;
                }
                else return true;//the tip turning point of a folded contour
                //modified on (7/21/2010)
            }
            sideness=sideness(contour,index0,index1,p);
            if(isRightside(sideness)) {
                return true;
            }else{
                if(index0==index){//have not tried the other posibility of the tip turning point of a folded contour
                    index0=indexn;
                    index1=index;
                }
                sideness=sideness(contour,index0,index1,p);
                if(isRightside(sideness)) {
                    return true;
                }
            }
        }
        return false;
    }
    static boolean isEnclosedSide(int sideness,int outer){
        if(outer==1)
            return isRightside(sideness);
        else
            return isLeftside(sideness);
    }
    public static boolean contacting(Point p1, Point p2){
        if(Math.abs(p1.x-p2.x)<2&&Math.abs(p1.y-p2.y)<2) return true;
        return false;
    }
    static public int sideness(ArrayList<Point> contour, int index, int index1, Point p){//TODO: need to be implemented for the case of direct contacting point
        Point pc=contour.get(index), pf=contour.get(index1);//pc is the point directly contacting with p
        int dx=p.x-pc.x,dy=p.y-pc.y,dx1=pf.x-pc.x,dy1=pf.y-pc.y;
        double crossProduct=dx1*dy-dy1*dx, dotProduct=dx1*dx+dy*dy1;
        if(crossProduct==0&&dotProduct>0) return front;
        if(crossProduct>0&&dotProduct==0) return right;
        if(crossProduct<0&&dotProduct==0) return left;
        if(crossProduct==0&&dotProduct<0) return rear;
        if(crossProduct<0&&dotProduct>0) return frontLeft;
        if(crossProduct<0&&dotProduct<0) return rearLeft;
        if(crossProduct>0&&dotProduct<0) return rearRight;
        if(crossProduct>0&&dotProduct>0) return frontRight;
        return -1;//this statement shouldn't be reached.
    }
    public static boolean higherIndex(int size, int index1, int index2){
        if(index2>index1)
            return true;
        else if(index1==(size-1))
            return true;
        return false;
    }
    public static boolean isRightside(int sideness){//7/21/2010
        if(sideness==right) return true;
        if(sideness==frontRight) return true;
        if(sideness==rearRight) return true;
        return false;
    }
    public static boolean isLeftside(int sideness){
        if(sideness==left) return true;
        if(sideness==frontLeft) return true;
        if(sideness==rearLeft) return true;
        return false;
    }
    static public void getDirection(ArrayList<Point> contour, int index, Point v0, Point v1){
        int size=contour.size();
        int index0=CommonMethods.circularAddition(size, index, -1);
        int index1=CommonMethods.circularAddition(size, index, 1);
        Point p0=contour.get(index0),p1=contour.get(index),p=contour.get(index);
        v0.setLocation(p.x-p0.x,p.y-p0.y);
        v1.setLocation(p1.x-p.x,p1.y-p.y);
    }
    static public int getDirection(Point v0, Point v1){
        int direction=0;
        return direction;
    }
    static public void unfoldOuterContour(ArrayList<Point> contour, ArrayList<PointArray> contours){//contour is a possibly folded outer contour. contours store the unfolded
        //contour as the first element, and the rest are quasi-inner contour
        //will be implemented later.
    }
    static public Point getLeftsidePoint(ArrayList<Point> contour, int index, int delta){//on left side  of p=contour.get(index) when looking form  p0=contour.get(index+delta), delta=1 or -1, to p
        int nSize=contour.size();
        int index0=ContourFollower.circularAddition(nSize, index, delta);
        Point p=contour.get(index),p0=contour.get(index0);
        int x,y;
        if(p0.x==p.x){
            x=p.x+(p.y-p0.y);//p.y-p.y==1 or -1 because they are directly contacting.
            y=p.y;
        }else{//must be p.y==p0.y becuase they are directly contacting
            x=p.x;
            y=p.y-(p.x-p0.x);
        }
        return new Point(x,y);
    }
    static public int getSubpixelContour(TwoDFunction fun, double x0, double y0, double dx, double dy, DoubleRange dr, ArrayList<Point> contour,ArrayList<DoublePair> SubPixelContour){
        int i=0,len=contour.size(),moveIndex;        
        Point p=contour.get(i),pin=new Point();
        int[] move;
        DoublePair dp;
        for(i=0;i<len;i++){
            p=contour.get(i);
            SubPixelContour.add(new DoublePair(p.x*dx+x0,p.y*dy+y0));
        }
        if(true) return 1;
        for(i=0;i<len;i++){
            if(i==10){
                i=i;
            }
            p=contour.get(i);   
            moveIndex=0;
            move=getMove(moveIndex);
            pin.setLocation(p.x+move[0],p.y+move[1]);
            while(!isInside(fun,x0,y0,dx,dy,pin,dr)){
                moveIndex++;
                if(moveIndex>7) {
                   // IJ.error("Error in getSubpixelContour");
                    break;
                }
                move=getMove(moveIndex);
                pin.setLocation(p.x+move[0],p.y+move[1]);
            }
            dp=getSubpixelContourPoint(fun,x0,y0,dx,dy,dr,p,pin,SubpixelContourPrecision);
            SubPixelContour.add(dp);
        }
        return 1;
    }/*this is a functional code. I am making this change to do fast, and coarse computing. 1/29/2013
    static public int getSubpixelContour(TwoDFunction fun, double x0, double y0, double dx, double dy, DoubleRange dr, ArrayList<Point> contour,ArrayList<DoublePair> SubPixelContour){
        int i=0,len=contour.size(),moveIndex;        
        Point p=contour.get(i),pin=new Point();
        int[] move;
        DoublePair dp;
        for(i=0;i<len;i++){
            if(i==10){
                i=i;
            }
            p=contour.get(i);   
            moveIndex=0;
            move=getMove(moveIndex);
            pin.setLocation(p.x+move[0],p.y+move[1]);
            while(!isInside(fun,x0,y0,dx,dy,pin,dr)){
                moveIndex++;
                if(moveIndex>7) {
                   // IJ.error("Error in getSubpixelContour");
                    break;
                }
                move=getMove(moveIndex);
                pin.setLocation(p.x+move[0],p.y+move[1]);
            }
            dp=getSubpixelContourPoint(fun,x0,y0,dx,dy,dr,p,pin,SubpixelContourPrecision);
            SubPixelContour.add(dp);
        }
        return 1;
    }*/
    static public DoublePair getSubpixelContourPoint(TwoDFunction fun, double x0, double y0, double dx, double dy, DoubleRange dr,Point p, Point pin, double Precision){//p and pin are directly contacting points, 
        //p is a point in the contour and pin is in the region.
        int index=0;
        double delta=dx;
        double[] dp={x0+p.x*dx,y0+p.y*dy},dpin={x0+pin.x*dx,y0+pin.y*dy},dpt={x0+pin.x*dx,y0+pin.y*dy};
        double amp;
        delta=Math.abs(dp[0]-dpin[0])+Math.abs(dp[1]-dpin[1]);        
        while(delta>Precision){
            dpt[0]=0.5*(dp[0]+dpin[0]);
            dpt[1]=0.5*(dp[1]+dpin[1]);
            amp=fun.func(dpt[0], dpt[1]);
            if(dr.contains(amp)){
                dpin[0]=dpt[0];
                dpin[1]=dpt[1];
            }else{
                dp[0]=dpt[0];
                dp[1]=dpt[1];
            }
            delta=Math.abs(dp[0]-dpin[0])+Math.abs(dp[1]-dpin[1]);        
        }
        return new DoublePair(dp[0],dp[1]);
    }
    static public ArrayList<Point> buildContour(ArrayList<Point> points, int outerness){
        ArrayList<Point> contour=buildContour(points);
        if(getOuterness(contour)!=outerness) reverseContourOuterness(contour);
        return contour;
    }
    public static int getOuterness(ArrayList<Point> contour){
        if(innerContour(contour))
            return -1;
        else
            return 1;
    }
    static public ArrayList<Point> buildContour(ArrayList<Point> points){
        int len=points.size(),i,j,len1;
        Point p0=points.get(0),p;
        ArrayList<Point> contour=new ArrayList(),points1;
        contour.add(p0);
        for(i=1;i<=len;i++){
            if(i<len)
                p=points.get(i);
            else
                p=points.get(0);
            points1=LineConnector.getConnection(p0, p);
            len1=points1.size();
            for(j=0;j<len1;j++){
                contour.add(points1.get(j));
            }
            contour.add(p);
            p0=p;
        }
        len=contour.size();
        contour.remove(len-1);
        return contour;
    }
    static public void reverseContourOuterness(ArrayList<Point> points){//outer: 1 for outer contour and -1 for inner contour
        int len=points.size(),i,index,len1;
        int half=len/2;

        for(i=0;i<half;i++){
            index=len-1-i;
            CommonMethods.swapPoints(points, i, index);
        }
    }
    static public void exportContour(String path, ArrayList<Point> contour){
        Formatter fm=QuickFormatter.getFormatter(path);
        int len=contour.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=contour.get(i);
            PrintAssist.printString(fm, "Point"+i+":", 12);
            PrintAssist.printString(fm, pt.x+",", 6);
            PrintAssist.printString(fm, pt.y+",", 6);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
    static public void markContour(ImagePlus impl, ArrayList<Point> contour, Color c, Point pDisp){
        CommonMethods.drawTrail(impl, contour, c.getRGB(),pDisp);
    }
    static public void markContour(ImagePlus impl, ArrayList<Point> contour, Color c){
//        ArrayList<Point> points=CommonStatisticsMethods.copyArray(contour);
//        removeOffBoundPoints(impl.getWidth(),impl.getHeight(),points);
        CommonMethods.drawTrail(impl, contour, c.getRGB(),new Point(0,0));
    }
    static public void markContourX(ImagePlus impl, ArrayList<Point> contour, Color cb, Color ct, Color ci,intRange xRange){
        int len=contour.size();
        int i,lt=0,lb=0,rt=0,rb=0,lYMin=Integer.MAX_VALUE,lYMax=Integer.MIN_VALUE,rYMin=Integer.MAX_VALUE,rYMax=Integer.MIN_VALUE,x0=xRange.getMin(),dx=xRange.getRange()/3,x,y;
        int xl=x0+dx,xr=x0+2*dx;
        Point pt,pt0=new Point(0,0);
        for(i=0;i<len;i++){
            pt=contour.get(i);
            x=pt.x;
            y=pt.y;
            if(x==xl){
                if(y<lYMin){
                    lYMin=y;
                    lt=i;
                }
                if(y>lYMax){
                    lYMax=y;
                    lb=i;
                }
            }else if(x==xr){
                if(y<rYMin){
                    rYMin=y;
                    rt=i;
                }
                if(y>rYMax){
                    rYMax=y;
                    rb=i;
                }
            }
        }

        CommonMethods.drawTrail(impl, contour, cb.getRGB(),pt0,lt,rt);
        if(len>0)CommonMethods.drawTrail(impl, contour, ct.getRGB(),pt0,CommonMethods.circularAddition(len, lb, 1),CommonMethods.circularAddition(len, lt, -1));
        if(len>0)CommonMethods.drawTrail(impl, contour, ct.getRGB(),pt0,CommonMethods.circularAddition(len, rt, 1),CommonMethods.circularAddition(len, rb, -1));
        CommonMethods.drawTrail(impl, contour, ci.getRGB(),pt0,rb,lb);
    }
    public static void getXYRanges(ArrayList<Point> points, intRange xRange, intRange yRange){
        xRange.resetRange();
        yRange.resetRange();
        int xMin=Integer.MAX_VALUE;
        int yMin=Integer.MAX_VALUE;
        int xMax=Integer.MIN_VALUE;
        int yMax=Integer.MIN_VALUE;
        Point pt;
        int i, len=points.size(),x,y;
        for(i=0;i<len;i++){
            pt=points.get(i);
            x=pt.x;
            y=pt.y;
            if(x>xMax) xMax=x;
            if(x<xMin) xMin=x;
            if(y>yMax) yMax=y;
            if(y<yMin) yMin=y;
        }
        xRange.setRange(xMin, xMax);
        yRange.setRange(yMin, yMax);
    }
    public static Point getRightBottomInnerHandlePoint(ArrayList<Point> contour){//assuming to be a outer contour
        Point ti,pt;
        int yMax=Integer.MIN_VALUE;
        int x,y,i,len=contour.size(),ix=1;
        for(i=0;i<len;i++){
            pt=contour.get(i);
            y=pt.y;
            if(y>yMax){
                yMax=y;
                ix=i;
            }
        }
        int ix0,ix1=CommonMethods.circularAddition(len, ix, -1);
        while(contour.get(ix1).y==yMax){
            ix=ix1;
            ix1=CommonMethods.circularAddition(len, ix, -1);
        }
        pt=contour.get(ix);
        ti=new Point(pt.x-1,pt.y-1);
        return ti;
    }
    public static Point getLeftBottomInnerHandlePoint(ArrayList<Point> contour){//assuming to be a outer contour
        Point ti,pt;
        int yMax=Integer.MIN_VALUE;
        int x,y,i,len=contour.size(),ix=1;
        for(i=0;i<len;i++){
            pt=contour.get(i);
            y=pt.y;
            if(y>yMax){
                yMax=y;
                ix=i;
            }
        }
        int ix0,ix1=CommonMethods.circularAddition(len, ix, 1);
        while(contour.get(ix1).y==yMax){
            ix=ix1;
            ix1=CommonMethods.circularAddition(len, ix, 1);
        }
        pt=contour.get(ix);
        ti=new Point(pt.x+1,pt.y-1);
        return ti;
    }
    public static Point getLeftTopInnerHandlePoint(ArrayList<Point> contour){//assuming to be a outer contour
        Point ti,pt;
        int yMin=Integer.MAX_VALUE;
        int y,i,len=contour.size(),ix=1;
        for(i=0;i<len;i++){
            pt=contour.get(i);
            y=pt.y;
            if(y<yMin){
                yMin=y;
                ix=i;
            }
        }
        int ix0,ix1=CommonMethods.circularAddition(len, ix, -1);
        while(contour.get(ix1).y==yMin){
            ix=ix1;
            ix1=CommonMethods.circularAddition(len, ix, -1);
        }
        pt=contour.get(ix);
        ti=new Point(pt.x+1,pt.y+1);
        return ti;
    }
    public static Point getRightTopInnerHandlePoint(ArrayList<Point> contour){//assuming to be a outer contour
        Point ti,pt;
        int yMin=Integer.MAX_VALUE;
        int x,y,i,len=contour.size(),ix=1;
        for(i=0;i<len;i++){
            pt=contour.get(i);
            y=pt.y;
            if(y<yMin){
                yMin=y;
                ix=i;
            }
        }
        int ix0,ix1=CommonMethods.circularAddition(len, ix, 1);
        while(contour.get(ix1).y==yMin){
            ix=ix1;
            ix1=CommonMethods.circularAddition(len, ix, 1);
        }
        pt=contour.get(ix);
        ti=new Point(pt.x-1,pt.y+1);
        return ti;
    }
    public static ArrayList<Point> getEnlargedContour(ArrayList<Point> contour, int outer){
        //parameter outer indicates whether the contour is a inner contour (outer=-1) or a outer contour(outer=1).
        if(getNonDirectContactingPosition(contour)>=0){
            IJ.error("invalid contour, getEnlargedContour");
            return null;
        }
        if(outer==0) {
            if(innerContour(contour)) outer=-1;
            else outer=1;
        }
        if(outer<0) contour=getInvertedContour(contour);

        Point pe0,pe;//the points on the enlarged contour
        Point p;//the points on the contour
        ArrayList<Point> path;
        int len=contour.size();
        int iI=0,iF=len-1;
        while(!isRightTurningPoint(contour,iI)||!isStraightPoint(contour,iF)){
            iI=CommonMethods.circularAddition(len, iI, 1);
            iF=CommonMethods.circularAddition(len, iI, -1);
        }
        int i;
        
        ArrayList<Point> lcontour=getWrappingSegment(contour,iI,iF,1);
        len=lcontour.size();
        pe0=lcontour.get(len-1);
        pe=lcontour.get(0);
        p=contour.get(iI);
        path=NeighboringPositionTraveler.getPath(pe0.x-p.x, pe0.y-p.y,pe.x-p.x, pe.y-p.y, 1);
        CommonMethods.translatePath(path, p);
        CommonMethods.appendPath(lcontour, path);
        return lcontour;
    }
    public static boolean isTurningPoint(ArrayList<Point> seg, int index){
        int len=seg.size();
        int i0=CommonMethods.circularAddition(len, index, -1);
        int i1=CommonMethods.circularAddition(len, index, 1);
        Point p=seg.get(index),p0=seg.get(i0),p1=seg.get(i1);
        if(p.x==p0.x&&p.x==p1.x) return false;
        if(p.y==p0.y&&p.y==p1.y) return false;
        return true;
    }
    public static boolean isStraightPoint(ArrayList<Point> seg, int index){
        return (!isTurningPoint(seg,index));
    }
    public static boolean isRightTurningPoint(ArrayList<Point> seg, int index){
        int len=seg.size();
        int i0=CommonMethods.circularAddition(len, index, -1);
        int i1=CommonMethods.circularAddition(len, index, 1);
        Point p=seg.get(index),p0=seg.get(i0),p1=seg.get(i1);
        Point pr=turnRight(p0,p);
        return (pr.x==p1.x&&pr.y==p1.y);
    }
    public static boolean isLeftTurningPoint(ArrayList<Point> seg, int index){
        int len=seg.size();
        int i0=CommonMethods.circularAddition(len, index, -1);
        int i1=CommonMethods.circularAddition(len, index, 1);
        Point p=seg.get(index),p0=seg.get(i0),p1=seg.get(i1);
        Point pr=turnLeft(p0,p);
        return (pr.x==p1.x&&pr.y==p1.y);
    }
    public static ArrayList<Point> getEnlargedContour0(ArrayList<Point> contour, int outer){
        //parameter outer indicates whether the contour is a inner contour (outer=-1) or a outer contour(outer=1).
        if(getNonDirectContactingPosition(contour)>=0){
            IJ.error("invalid contour, getEnlargedContour");
            return null;
        }
        if(outer==0) {
            if(innerContour(contour)) outer=-1;
            else outer=1;
        }
        if(outer<0) contour=getInvertedContour(contour);
        Point pe0,pe;//the points on the enlarged contour
        Point p;//the points on the contour
        ArrayList<Point> path;
        int len=contour.size();
        ArrayList<Point> lcontour=getWrappingSegment(contour,0,len-1,1);
        len=lcontour.size();
        int i;
        p=contour.get(0);
        pe0=lcontour.get(len-1);
        pe=lcontour.get(0);
        path=NeighboringPositionTraveler.getPath(pe0.x-p.x, pe0.y-p.y,pe.x-p.x, pe.y-p.y, 1);
        CommonMethods.translatePath(path, p);
        CommonMethods.appendPath(lcontour, path);
        return lcontour;
    }
    public static ArrayList<Point> getWrappingSegment(ArrayList<Point> segment, int iI0, int iF, int sideness){//changed on 14908, there is some problems.
        //this method returns a line (direct contacting connection) segment that wraps the "segment" from
        //point iI to iF. the wrapping is on the left side if sideness is 1, on the right side if sideness is -1.
        intRange xRange=new intRange(), yRange=new intRange();
        int len=segment.size(),iI=iI0;
        Point pe0;//the points on the enlarged contour
        Point p0,p,pt;//the points on the contour
        
        
        ArrayList<Point> path0,path=new ArrayList();
        ArrayList<Point> lcontour=new ArrayList();
        CommonMethods.getCoordinateRanges(segment, CommonMethods.circularAddition(len, iI, -1), CommonMethods.circularAddition(len, iF, 1), xRange, yRange);
        Point conner=new Point(xRange.getMin()-1,yRange.getMin()-1);
        CommonMethods.translatePath(segment, new Point(-conner.x,-conner.y));
        
        int w0=xRange.getRange()+2,h0=yRange.getRange()+2;
        boolean[][] stamp=new boolean[h0][w0];
        CommonStatisticsMethods.setElements(stamp, false);
        int i,j,next,dx,dy;
        for(i=iI;i<=iF;i++){
            p=segment.get(i);
            stamp[p.y][p.x]=true;
        }
        
        p0=segment.get(iI);
        next=CommonMethods.circularAddition(len, iI, 1);
        p=segment.get(next);
        dx=p0.x-p.x;
        dy=p0.y-p.y;
        if(Math.abs(dx)>1||Math.abs(dy)>1){//14908
            dx=dy;
            CommonMethods.buildConnectin(p0, p, path);
            for(i=0;i<path.size();i++){
                segment.add(iI+i+1, path.get(i));
            }
        }
        pe0=NeighboringPositionTraveler.nextPosition(dx, dy,1);
        pe0.translate(p.x, p.y);
        if(pe0.x>=w0||pe0.y>=h0){
            pe0=pe0;
        }
        while(stamp[pe0.y][pe0.x]){
            iI=CommonMethods.circularAddition(len, iI, 1);
            p0=segment.get(iI);
            next=CommonMethods.circularAddition(len, iI, 1);
            p=segment.get(next);
            pe0=NeighboringPositionTraveler.nextPosition(p0.x-p.x, p0.y-p.y,1);
            pe0.translate(p.x, p.y);
        }
        lcontour.add(pe0);
        i=iI;
        int lent;
        while(i!=iF){
            if(i==54){
                iI=iI;
            }
            i=CommonMethods.circularAddition(len, i, 1);
            p=segment.get(i);
            if(p.x==pe0.x&&p.y==pe0.y){
                if(i==54||i==51){
                    ImagePlus impl=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, 658,496);
                    CommonMethods.drawTrail(impl, segment, Color.red.getRGB());
                    CommonMethods.drawTrail(impl, lcontour, Color.blue.getRGB());
                    impl.show();
                }
                lent=lcontour.size();
                lcontour.remove(lent-1);
                pe0=lcontour.get(lent-2);
            }
            path0=NeighboringPositionTraveler.getPath(pe0.x-p0.x, pe0.y-p0.y,p.x-p0.x, p.y-p0.y, sideness);
            CommonMethods.translatePath(path0, p0);
            
            path.clear();
            for(j=0;j<path0.size();j++){
                pt=path0.get(j);
                if(stamp[pt.y][pt.x]) //pt is part if segment
                    break;
            }
            CommonMethods.appendPath(lcontour, path);
            p0=p;
            pe0=lcontour.get(lcontour.size()-1);
        }
        
        CommonMethods.translatePath(lcontour, conner);
        CommonMethods.translatePath(segment, conner);
        return lcontour;
    }
    public static ArrayList<Point> getInvertedContour(ArrayList<Point> contour){
        int len=contour.size();
        int i;
        ArrayList<Point> points=new ArrayList();
        for(i=0;i<len;i++){
            points.add(contour.get(len-1-i));
        }
        return points;
    }
    public static void getImageEdgePositions(ArrayList<Point> contour, ArrayList<Integer> edgePositions, int w, int h){
        //this methods checks a contour disconnected at the image edges and store the indexes of the points on the
        //contour that a located on image edges. On each pair of edge points, only the indexe of the second point
        //is tored in edgePositions.
        edgePositions.clear();
        int i,len=contour.size(),i0,i1,position0,iF=len-1;
        Point p; 
        position0=-1;
        p=contour.get(len-1);
        if(CommonMethods.isOnEdge(w, h, p)) position0=len-1;
        for(i=0;i<len;i++){
            p=contour.get(i);
            if(CommonMethods.isOnEdge(w, h, p)){
                if(position0>=0){//the previous point is on edge
                    edgePositions.add(i);
                }
                position0=i;
            }else{
//                if(position>=0) IJ.error("there are must be two consecutive on-edge points on the contour getImageEdgePositions");
                //commenting out the above restriction is to allow a single edge point or continuous edge points on a contour
                position0=-1;
            }
        }
    }

    public static ArrayList<Point> OffBoundConnectedContour(ArrayList<Point> contouro, int w, int h){
        //contour should be disconnected only at image edges
        //this methods links a contour disconnected at image edges to make a contour that also follow the
        //points on the image on the edge (counting them as inside points)
        ArrayList<Point> points=new ArrayList(),contour=new ArrayList(),edgePath;
        copyArray(contouro, contour);
        copyArray(contouro, points);
        ArrayList<Integer> edgePositions=new ArrayList();
        ArrayList<Integer> edgePositions1=new ArrayList();//new positions after inserting the edge paths
        ArrayList<Integer> edgePathLengths=new ArrayList();
        getImageEdgePositions(contour,edgePositions,w,h);
        int i,len=points.size(),position,previous,index;
        int len1=edgePositions.size();
        Point p,p1,p2;
        
        boolean bInverted=false;

        int growth=0;
        for(i=0;i<len1;i++){
            position=edgePositions.get(i);
            previous=CommonMethods.circularAddition(len, position, -1);
            p1=contouro.get(previous);
            p2=contouro.get(position);
            edgePath=CommonMethods.getEdgePath(p1, p2, w, h);
            edgePathLengths.add(edgePath.size());
            CommonMethods.insertPath(contour,edgePath,position+growth);
            growth+=edgePathLengths.get(i);
            edgePositions1.add(position+growth);
        }
        if(innerContour(contour)) {
            bInverted=true;
            contour=getInvertedContour(contour);
            CommonStatisticsMethods.invertArray(edgePositions);
            CommonStatisticsMethods.invertArray(edgePositions1);
            CommonStatisticsMethods.invertArray(edgePathLengths);
        }

        ArrayList<Point> seg;
        len=contour.size();
        for(i=0;i<len1;i++){
            index=len1-1-i;
            position=edgePositions1.get(index);
            previous=CommonMethods.circularAddition(len, position, -(edgePathLengths.get(index)+1));
            if(getNonDirectContactingPosition(contour,previous,position)>=0){
                position=position;
            }
            seg=getWrappingSegment(contour,previous,position,1);
            position=edgePositions.get(index);
            CommonMethods.insertPath(points, seg, position);
        }
        return points;
    }
    public static void copyArray(ArrayList<Point> points0,ArrayList<Point> points){
        int len=points0.size(),i;
        points.clear();
        for(i=0;i<len;i++){
            points.add(points0.get(i));
        }
    }
    public static boolean validContour(ArrayList <Point> points){
         return (getNonDirectContactingPosition(points)<0);
    }
    public static boolean validContourSegment(ArrayList <Point> points, int iI,int iF){
        return (getNonDirectContactingPosition(points,iI,iF)<0);
    }
    public static int getNonDirectContactingPosition(ArrayList <Point> points){
        int len=points.size();
        int position=getNonDirectContactingPosition(points,0,len-1);
        if(position>=0) return position;
        position=getNonDirectContactingPosition(points,len-1,0);
        return position;
    }
    public static int getNonDirectContactingPosition(ArrayList <Point> points, int iI,int iF){
        int index,len=points.size();
        index=iI;
        Point p0=points.get(iI),p;
        while(index!=iF){
            index=CommonMethods.circularAddition(len, index, 1);
            p=points.get(index);
            if(!CommonMethods.DirectContacting(p0, p)) return index;
            p0=p;
        }
        return -1;
    }
    static public ArrayList<Double> calCurvatures(ArrayList<Point> Contour,int ws){
        ArrayList<Double> Curvatures=new ArrayList <Double> ();
        int i,i0,i1,size=Contour.size();
        if(size<3) return Curvatures;
        double h;
        Point p1,p2,p3;
        double hMax=-1;
        for(i=0;i<size;i++){
            i0=(i+size-ws)%size;
            i1=((i+ws)%size);
            p1=Contour.get(i0);
            p2=Contour.get(i1);
            p3=Contour.get(i);
            if(p3.x==81+840&&p3.y==78+1203){
                i=i;
            }
            h=CommonMethods.getDistanceToLine(p1, p2, p3)/CommonMethods.getDistance(p1.x, p1.y, p2.x, p2.y);
            Curvatures.add(h);
            if(Math.abs(h)>hMax) hMax=Math.abs(h);
        }
        return Curvatures;
    }
    static public int getLeftTopPosition(ArrayList<Point> contour){
        Point pLT=contour.get(0),p;
        int i,lt=0,len=contour.size();
        for(i=1;i<len;i++){
            p=contour.get(i);
            if(p.x>pLT.x) continue;
            if(p.x<pLT.x||p.y<pLT.y){
                pLT=p;
                lt=i;
            }
        }
        return lt;
    }
    static public ArrayList<Double> calCurvatures_Subpixel(ArrayList<DoublePair> Contour,int ws){
        ArrayList<Double> Curvatures=new ArrayList <Double> ();
        int i,i0,i1,size=Contour.size();
        if(size<3) return Curvatures;
        double h;
        DoublePair p1,p2,p3;
        double hMax=-1;
        for(i=0;i<size;i++){
            i0=(i+size-ws)%size;
            i1=((i+ws)%size);
            p1=Contour.get(i0);
            p2=Contour.get(i1);
            p3=Contour.get(i);
            if(i==154){
                i=i;
            }
//            h=CommonMethods.getDistanceToLine(new Point2D(p1.left,p1.right), new Point2D(p2.left,p2.right),new Point2D(p3.left,p3.right))/CommonMethods.getDistance(p1.left, p1.right, p2.left, p2.right);
            h=CommonMethods.getDistanceToLine(new Point2D(p1.left,p1.right), new Point2D(p2.left,p2.right),new Point2D(p3.left,p3.right));
            Curvatures.add(h);
            if(Math.abs(h)>hMax) hMax=Math.abs(h);
        }
        return Curvatures;
    }
    static public DoublePair getOneSidePoint(TwoDFunction func, DoublePair p, double ds, DoubleRange dr,boolean inside){
        //p is a point on the contour. One of neighboring points should be an inside point.
        double x0=p.left,y0=p.right,x,y;
        int numRotations=8;
        double theta=2*Math.PI/numRotations,theta0=theta,shift=0;
        DoublePair p2=new DoublePair(p.left+ds,p.right),po=new DoublePair(p2);
        double thetaMin=0.01*Math.PI;
        while(!findOneSidePosition(func,p,p2,theta,dr,inside)){
            shift+=2*theta;
            if(shift>=theta0){
                theta/=2;
                if(theta<thetaMin) return null;
                shift=theta;
                p2.setValue(po);
            }
            CommonMethods.rotate(po, p2, theta);
        }
        return p2;
    }
    static public boolean findOneSidePosition(TwoDFunction func, DoublePair p, DoublePair p2, double theta, DoubleRange dr,boolean inside){
        //rotate point p2 around p until the func value at p2 is within or outside of the range dr (depends on inside is ture or false).
        //returns -1 if no position found after a rotate 2 pi.
        double dMax=Math.PI,sum=0;
        while(dr.contains(func.func(p2.left, p2.right))!=inside){
            sum+=theta;
            if(sum>=dMax) return false;
            CommonMethods.rotate(p, p2, theta);
        }
        return true;
    }
    static public double calCurvature(TwoDFunction func, ArrayList<DoublePair> Contour, int index, double r, DoubleRange dr){
        double cur=0;
        if(index==47){
            index=index;
        }
        int size=Contour.size(),index0=CommonStatisticsMethods.getCircularIndex(index-5, size),index1=CommonStatisticsMethods.getCircularIndex(index+5, size);
        DoublePair p=Contour.get(index),pl=Contour.get(index0),pr=Contour.get(index1),p2=getOneSidePoint(func,p,r,dr,false);
        if(p2==null){
            return 0;
        }
        return CommonMethods.getDistanceToLine(pr, pl, p);
    }    
    /*this is a functional code. I am making this change to do fast, and coarse computing. 1/29/2013
    static public double calCurvature(TwoDFunction func, ArrayList<DoublePair> Contour, int index, double r, DoubleRange dr){
        double cur=0;
        if(index==47){
            index=index;
        }
        int size=Contour.size(),index0=CommonStatisticsMethods.getCircularIndex(index-1, size),index1=CommonStatisticsMethods.getCircularIndex(index+1, size);
        DoublePair p=Contour.get(index),pl=Contour.get(index0),pr=Contour.get(index1),p2=getOneSidePoint(func,p,r,dr,false);
        if(p2==null){
            return 0;
        }
        DoublePair p1=new DoublePair(p2.left,p2.right),p0=new DoublePair(p2);
        double theta=0.01*Math.PI;
        double sum=0;
        CommonMethods.rotate(p, p1, theta);
        while(!dr.contains(func.func(p1.left,p1.right))){
            sum+=theta;
            p2.setValue(p1);
            CommonMethods.rotate(p, p1, theta);
            if(Math.abs(sum)>Math.PI) break;
        }
        DoublePair pc1=getContourPoint(func,p,p1,p2,r,dr);
        
        theta*=-1;
        p1.setValue(p0);
        p2.setValue(p0);
        sum=0;
        CommonMethods.rotate(p, p1, theta);
        while(!dr.contains(func.func(p1.left,p1.right))){
            sum+=theta;
            p2.setValue(p1);
            CommonMethods.rotate(p, p1, theta);
            if(Math.abs(sum)>Math.PI) break;
        }
        DoublePair pc2=getContourPoint(func,p,p1,p2,r,dr);
        
        double dist=CommonMethods.getDistanceToLine(pc1, pc2, p);
        CommonMethods.reflect(p, pc2);
        cur=CommonMethods.getDistance(pc1, pc2)/r;
        cur=Math.copySign(cur, dist);
        return cur;
    }*/
    static public DoublePair getContourPoint(TwoDFunction func, DoublePair p, DoublePair p1, DoublePair p2, double r, DoubleRange dr){
        //p1 and p2 are inside and outside points, respectively. the distance from the two points to the point p are the same, and equal to r.
        DoublePair pm;
        int nMax=30,num=0;;
        while(Math.abs(p1.left-p2.left)+Math.abs(p1.right-p2.right)>SubpixelContourPrecision){
            pm=CommonMethods.getMidArcPoint(p, p1, p2, r);
            if(dr.contains(func.func(pm.left, pm.right))){
                p1.left=pm.left;
                p1.right=pm.right;
            }else{
                p2.left=pm.left;
                p2.right=pm.right;
            }
            num++;
            if(num>nMax) break;
        }
        return new DoublePair(p2.left,p2.right);
    }
    static public ArrayList<Double> calCurvatures_Subpixel(TwoDFunction func, ArrayList<DoublePair> Contour, double r, DoubleRange dr){
        ArrayList<Double> Curvatures=new ArrayList <Double> ();
        int i,i0,i1,size=Contour.size(),ws=1;
        if(size<3) return Curvatures;
        DoublePair p1,p2,p3;
        for(i=0;i<size;i++){
            Curvatures.add(calCurvature(func,Contour,i,r,dr));
        }
        return Curvatures;
    }
    
    public static int[] getMove(int index){
        if(moves==null) initMoves();
        return moves[index];
    }
    static private void initMoves(){
        moves=new int[8][2];
        moves[0][0]=1;
        moves[0][1]=0;
        moves[1][0]=1;
        moves[1][1]=1;
        moves[2][0]=0;
        moves[2][1]=1;
        moves[3][0]=-1;
        moves[3][1]=1;
        moves[4][0]=-1;
        moves[4][1]=0;
        moves[5][0]=-1;
        moves[5][1]=-1;
        moves[6][0]=0;
        moves[6][1]=-1;
        moves[7][0]=1;
        moves[7][1]=-1;
    }
}
