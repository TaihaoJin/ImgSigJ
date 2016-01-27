/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;
import utilities.Geometry.Point2D;
import utilities.ArrayofArrays.IntRangeArray2;
import utilities.CustomDataTypes.intRange;
import java.util.ArrayList;
import utilities.ArrayofArrays.IntRangeArray;
import utilities.Constants;
import java.util.Stack;
import utilities.ArrayofArrays.IntStack;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class MinimalEnclosingCovexPolygon {    
    public static ArrayList <Point2D> getMECPolygon(ArrayList<IntRangeArray> xSegments, intRange yRange){
        return getMECPolygon(new IntRangeArray2(xSegments),yRange);
    }
    public static ArrayList <Point2D> getMECPolygon(IntRangeArray2 yStripes, intRange yRange){
        ArrayList <Point2D> MECPolygon=new ArrayList <Point2D>();
        int size=yStripes.m_IntRangeArray2.size();
        if(size<=1) return MECPolygon;//return an empty polygon because the input structure has zero area.
        Point2D[] vertices=new Point2D[2*size];
        //constructing enclosing polygon (might not be convex)
        int yMin=yRange.getMin(), yMax=yRange.getMax();
        int i,y,stripe,size1,size2,size3,size4;
        IntRangeArray ira;
        for(y=yMin;y<=yMax;y++){
            stripe=y-yMin;
            ira=yStripes.m_IntRangeArray2.get(stripe);
            size1=ira.m_intRangeArray.size();
            vertices[stripe]=new Point2D(ira.m_intRangeArray.get(0).getMin(),y);
            vertices[2*size-1-stripe]=new Point2D(ira.m_intRangeArray.get(size1-1).getMax(),y);
        }
        ArrayList <Point2D> fPoints1=new ArrayList <Point2D> ();
        ArrayList <Point2D> rPoints1=new ArrayList <Point2D> ();
        ArrayList <Point2D> fPoints2=new ArrayList <Point2D> ();
        ArrayList <Point2D> rPoints2=new ArrayList <Point2D> ();
        int p1=0,p2=size-1,p3=size,p4=2*size-1;
//        disect(vertices, fPoints1, 0, size-1);
//        disect(vertices, fPoints2, size, 2*size-1);
        findConvexVertices(vertices,fPoints1,0,size-1);
        findConvexVertices(vertices,fPoints2,size,2*size-1);
        size1=fPoints1.size();
        MECPolygon.add(vertices[p1]);
        for(i=0;i<size1;i++){
            MECPolygon.add(fPoints1.get(i));
        }
        if(vertices[p1].different(vertices[p2])) MECPolygon.add(vertices[p2]);
        if(vertices[p2].different(vertices[p3])) MECPolygon.add(vertices[p3]);
        size3=fPoints2.size();
        for(i=0;i<size3;i++){
            MECPolygon.add(fPoints2.get(i));
        }
        if(vertices[p1].different(vertices[p4]))MECPolygon.add(vertices[p4]);
//        CommonMethods.displayPolygon(MECPolygon);
        return MECPolygon;        
    }
    public static int findConvexVertices(Point2D[] vertices, ArrayList <Point2D> CVs, int first, int last){//CVs
        int p,p0=first,px;
        double aMax=Double.NEGATIVE_INFINITY,angle;
        ArrayList<Double> dvAs=new ArrayList();
        px=getLargestAnglePosition(vertices,p0,last,dvAs);
       
        while(px>p0){
            aMax=dvAs.get(px-p0);
            if(aMax<0) break;
//            for(p=p0+1;p<last;p++){
//                if(dvAs.get(p-p0)<aMax) continue;
//                CVs.add(vertices[p]);
//            }
            CVs.add(vertices[px]);
            p0=px;
            px=getLargestAnglePosition(vertices,p0,last,dvAs);
        }
        return 1;
    }
    public static int getLargestAnglePosition(Point2D[] pts, int first, int last,ArrayList<Double> dvAngles){
        Point2D P1=pts[first],P2=pts[last],P3;
        dvAngles.clear();
        int px=first,p;
        double angle, ax=Double.NEGATIVE_INFINITY,x1=P1.dx,y1=P1.dy,x2=P2.dx,y2=P2.dy;
        dvAngles.add(0.);
        for(p=first+1;p<last;p++){
            P3=pts[p];
            angle=-CommonMethods.calAngle(x1, y1, x2, y2, P3.dx, P3.dy);//the method is defined as in normal geomettry. the image coordinate is lefthanded
            if(angle>=ax){//the equality condition is to get the position closest to last
                ax=angle;
                px=p;
            }
            dvAngles.add(angle);
        }
        return px;
    }
    public static int disect(Point2D[] vertices, ArrayList <Point2D> fPoints, int first, int last){        
        double a1=Double.NEGATIVE_INFINITY, a2=Double.NEGATIVE_INFINITY, h=Double.NEGATIVE_INFINITY;
        int p1=first,p2=last;
        int i,maxH=p1,maxA1=p1,maxA2=p1;
        double hMax=a1,a1Max=a1,a2Max=a1;
        Point2D P1=vertices[p1],P2=vertices[p2];
        double ha12[]=new double[3];
        Stack <IntStack> rStack=new Stack<IntStack>();
        IntStack rPoints =new IntStack();
        rPoints.m_intStack.push(last);
        rStack.push(rPoints);
        while(!rStack.empty()){
            a1Max=Double.NEGATIVE_INFINITY;
            a2Max=Double.NEGATIVE_INFINITY;
            hMax=Double.NEGATIVE_INFINITY;
            maxH=p1;
            maxA1=p1;
            maxA2=p1;
            rPoints=rStack.pop();
            p2=rPoints.m_intStack.pop();
            P1=vertices[p1];
            P2=vertices[p2];
            for(i=p1;i<=p2;i++){
                heightANDangles(P1,P2,vertices[i],ha12);
                h=ha12[0];
                a1=ha12[1];
                a2=ha12[2];
                if(h<0) {
                    h=h;
                }
                if(h>=hMax) {
                    hMax=h;
                    maxH=i;
                }
                if(a1>=a1Max){
                    a1Max=a1;
                    maxA1=i;
                }
                if(a2>a2Max){
                    a2Max=a2;
                    maxA2=i;
                }
            }
            if(hMax>0){
                if(maxA1==maxA2){
                    if(p1!=first) fPoints.add(vertices[p1]);
                    fPoints.add(vertices[maxH]);
                    while(!rPoints.m_intStack.isEmpty()){
//                        if(p2!=last) fPoints.add(vertices[p2]);
                        fPoints.add(vertices[p2]);
                        p2=rPoints.m_intStack.pop();
                    }
                    p1=p2;                    
                }else{
                    rPoints.m_intStack.push(p2);
                    rPoints.m_intStack.push(maxA2);
                    if(maxA2>maxH){
                        rStack.push(rPoints);
                        
                    }
                    if(maxH>maxA1){
                        if(maxH==maxA2){//rpoints was not previously 
                            rStack.push(rPoints);
                        }else{
                            rPoints=new IntStack(maxH);
                            rStack.push(rPoints);
                        }
                    }
                    if(p1!=first) fPoints.add(vertices[p1]);
                    p1=maxA1;
                }
            }else{
                if(p1!=first) fPoints.add(vertices[p1]);
                while(!rPoints.m_intStack.isEmpty()){
                    if(p2!=last) fPoints.add(vertices[p2]);
                    p2=rPoints.m_intStack.pop();
                }
                p1=p2;
            }
        }
        return 1;
    }
     public static int disect_old(Point2D[] vertices, ArrayList <Point2D> fPoints, int first, int last){        
        double a1=Double.NEGATIVE_INFINITY, a2=Double.NEGATIVE_INFINITY, h=Double.NEGATIVE_INFINITY;
        int p1=first,p2=last;
        int i,maxH=p1,maxA1=p1,maxA2=p1;
        double hMax=a1,a1Max=a1,a2Max=a1;
        Point2D P1=vertices[p1],P2=vertices[p2];
        double ha12[]=new double[3];
        Stack <IntStack> rStack=new Stack<IntStack>();
        IntStack rPoints =new IntStack();
        rPoints.m_intStack.push(last);
        rStack.push(rPoints);
        while(!rStack.empty()){
            a1Max=Double.NEGATIVE_INFINITY;
            a2Max=Double.NEGATIVE_INFINITY;
            hMax=Double.NEGATIVE_INFINITY;
            maxH=p1;
            maxA1=p1;
            maxA2=p1;
            rPoints=rStack.pop();
            p2=rPoints.m_intStack.pop();
            P1=vertices[p1];
            P2=vertices[p2];
            for(i=p1;i<=p2;i++){
                heightANDangles(P1,P2,vertices[i],ha12);
                h=ha12[0];
                a1=ha12[1];
                a2=ha12[2];
                if(h>=hMax) {
                    hMax=h;
                    maxH=i;
                }
                if(a1>=a1Max){
                    a1Max=a1;
                    maxA1=i;
                }
                if(a2>a2Max){
                    a2Max=a2;
                    maxA2=i;
                }
            }
            if(hMax>0){
                rPoints.m_intStack.push(p2);
                rPoints.m_intStack.push(maxA2);
                if(maxA2>maxH){
                    rStack.push(rPoints);
                }
                if(maxH>maxA1){
                    if(maxH==maxA2){//rpoints was not previously 
                        rStack.push(rPoints);
                    }
                    rPoints=new IntStack(maxH);
                    rStack.push(rPoints);
                }
                if(p1!=first) fPoints.add(vertices[p1]);
                p1=maxA1;
                if(maxA1==maxA2){
                    p2=rPoints.m_intStack.pop();
                    while(!rPoints.m_intStack.isEmpty()){
                        if(p2!=last) fPoints.add(vertices[p2]);
                        p2=rPoints.m_intStack.pop();
                    }
                    p1=p2;
                }
            }else{
                if(p1!=first) fPoints.add(vertices[p1]);
                while(!rPoints.m_intStack.isEmpty()){
                    if(p2!=last) fPoints.add(vertices[p2]);
                    p2=rPoints.m_intStack.pop();
                }
                p1=p2;
            }
        }
        return 1;
    }
    public static int heightANDangles(Point2D P1,Point2D P2,Point2D P3,double[] ha12){
        double a1=0., a2=0., h2=0.;
        if(!P1.different(P3)||!P2.different(P3)||!P1.different(P2)) {
            ha12[0]=Double.NEGATIVE_INFINITY;
            ha12[1]=Double.NEGATIVE_INFINITY;
            ha12[2]=Double.NEGATIVE_INFINITY;
            return 0;
        }
        double s32=(P2.x-P1.x)*(P2.x-P1.x)+(P2.y-P1.y)*(P2.y-P1.y);        
        double s22=(P3.x-P1.x)*(P3.x-P1.x)+(P3.y-P1.y)*(P3.y-P1.y);        
        double s12=(P3.x-P2.x)*(P3.x-P2.x)+(P3.y-P2.y)*(P3.y-P2.y);        
        double x1=P1.x,x2=P2.x,x3=P3.x,y1=P1.y,y2=P2.y,y3=P3.y;
        
        Point2D p0=CommonMethods.getClosestPointOnLine(P1, P2, P3);
        
        double x0=p0.dx;
        double y0=p0.dy;
        
        double d1=Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));
        double d2=Math.sqrt((x0-x2)*(x0-x2)+(y0-y2)*(y0-y2));
        h2=(x3-x0)*(x3-x0)+(y3-y0)*(y3-y0);
        double sign=1;
/*        if(P1.y!=P2.y) 
            sign=-(P2.y-P1.y)*(P3.x-x0);
        else
            sign=(P2.x-P1.x)*(P3.y-y0);*/
        
        if(((P3.dx-P1.dx)*(P2.dy-P1.dy)-(P3.dy-P1.dy)*(P2.dx-P1.dx))<0) sign=-1;
//        h2=Math.copySign(h2, sign);
        h2*=sign/Math.abs(sign);
//        a1=-((x2-x1)*(x3-x1)+(y2-y1)*(y3-y1))/(2.*Math.sqrt(s22*s32));
//        a2=-((x1-x2)*(x3-x2)+(y1-y2)*(y3-y2))/(2.*Math.sqrt(s12*s32));        
//        a1=(s12-s22-s32)/(2.*Math.sqrt(s22*s32));
//        a2=(s22-s12-s32)/(2.*Math.sqrt(s12*s32));
        a1=2+(s12-s22-s32)/(2.*Math.sqrt(s22*s32));
        a2=2+(s22-s12-s32)/(2.*Math.sqrt(s12*s32));
        a1*=sign/Math.abs(sign);
        a2*=sign/Math.abs(sign);
        ha12[0]=h2;
        ha12[1]=a1;
        ha12[2]=a2;
        return 1;
    }
    /*
    public static int heightANDangles0(Point2D P1,Point2D P2,Point2D P3,double[] ha12){
        double a1=0., a2=0., h2=0.;
        if(!P1.different(P3)||!P2.different(P3)||!P1.different(P2)) {
            ha12[0]=Constants.largestNegativeFloat;
            ha12[1]=Constants.largestNegativeFloat;
            ha12[2]=Constants.largestNegativeFloat;
            return 0;
        }
        double s32=(P2.x-P1.x)*(P2.x-P1.x)+(P2.y-P1.y)*(P2.y-P1.y);        
        double s22=(P3.x-P1.x)*(P3.x-P1.x)+(P3.y-P1.y)*(P3.y-P1.y);        
        double s12=(P3.x-P2.x)*(P3.x-P2.x)+(P3.y-P2.y)*(P3.y-P2.y);        
        double x1=P1.x,x2=P2.x,x3=P3.x,y1=P1.y,y2=P2.y,y3=P3.y;
        
        double k=(y2-y1)/(x2-x1), k1=-1./k;
        double b=(y1*(x2-x1)-x1*(y2-y1))/(x2-x1),b1=y3+x3/k;
        
        double x0=(b1-b)/(k-k1);
        double y0=k*x0+b;
        
        double d1=Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));
        double d2=Math.sqrt((x0-x2)*(x0-x2)+(y0-y2)*(y0-y2));
        h2=(x3-x0)*(x3-x0)+(y3-y0)*(y3-y0);
        double sign;
        if(P1.y!=P2.y) 
            sign=-(P2.y-P1.y)*(P3.x-x0);
        else
            sign=(P2.x-P1.x)*(P3.y-y0);
//        h2=Math.copySign(h2, sign);
        h2*=sign/Math.abs(sign);
//        a1=-((x2-x1)*(x3-x1)+(y2-y1)*(y3-y1))/(2.*Math.sqrt(s22*s32));
//        a2=-((x1-x2)*(x3-x2)+(y1-y2)*(y3-y2))/(2.*Math.sqrt(s12*s32));        
//        a1=(s12-s22-s32)/(2.*Math.sqrt(s22*s32));
//        a2=(s22-s12-s32)/(2.*Math.sqrt(s12*s32));
        a1=2+(s12-s22-s32)/(2.*Math.sqrt(s22*s32));
        a2=2+(s22-s12-s32)/(2.*Math.sqrt(s12*s32));
        a1*=sign/Math.abs(sign);
        a2*=sign/Math.abs(sign);
        ha12[0]=h2;
        ha12[1]=a1;
        ha12[2]=a2;
        return 1;
    }*/
}
