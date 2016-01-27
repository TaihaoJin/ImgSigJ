/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;
import ImageAnalysis.ContourFollower;
import utilities.Geometry.Point2D;
import java.util.ArrayList;
import utilities.Constants;
import utilities.CommonMethods;
import java.awt.Point;
import ij.ImagePlus;
import java.awt.Color;
/**
 *
 * @author Taihao
 */
public class ConvexPolygon {
    int sides;
    Point2D[] vertices;
    Point2D centroid;
    double area;
    double perimeter;
    double angles[];
    double height, width;
    Point2D[] MAERectangle;
    public ConvexPolygon(ArrayList <Point2D> vPoints){
        int size=vPoints.size();
        this.sides=size;
        vertices=new Point2D[size];
        angles=new double[sides];
        MAERectangle=new Point2D[4];
        for(int i=0;i<size;i++){
            vertices[i]=vPoints.get(i);
        }
        computeCentroid();
        computeAngles();
        computeMAERectangle();
    }
    public void computeMAERectangle(){
        double aMin=Constants.largestFloat;
        int lrt[]=new int[3], lrtM[]=new int[3],iMin=0;
        double[] wh=new double[2], whM=new double[2];
        double a;
        int i,j;
        for(i=0;i<sides;i++){
            getHeightAndWidth(i,wh,lrt);
            a=wh[0]*wh[1];
            if(a<aMin) {
                aMin=a;
                iMin=i;
                for(j=0;j<3;j++){
                    if(j<2){
                        whM[j]=wh[j];
                    }
                    lrtM[j]=lrt[j];
                }
            }
        }
        Point2D p1=null,p2=null;
        try{p1=vertices[iMin];p2=vertices[(iMin+1)%sides];}
        catch(ArrayIndexOutOfBoundsException e){
            e=e;
        }
        MAERectangle[0]=CommonMethods.getClosestPointOnLine(p1, p2, vertices[lrtM[0]]);
        MAERectangle[1]=CommonMethods.getClosestPointOnLine(p1, p2, vertices[lrtM[1]]);
        if(CommonMethods.horizontalLine(p1,p2)){            
            MAERectangle[2]=new Point2D(vertices[lrtM[1]].dx,vertices[lrtM[2]].y);
            MAERectangle[3]=new Point2D(vertices[lrtM[0]].dx,vertices[lrtM[2]].y);
        }else if(CommonMethods.verticalLine(p1,p2)){            
            MAERectangle[2]=new Point2D(vertices[lrtM[2]].dx,vertices[lrtM[1]].y);
            MAERectangle[3]=new Point2D(vertices[lrtM[2]].dx,vertices[lrtM[0]].y);        
        }else{                        
            double k=(p2.dy-p1.dy)/(p2.dx-p1.dx);
            MAERectangle[2]=CommonMethods.getClosestPointOnLine(vertices[lrtM[2]], k, vertices[lrtM[1]]);
            MAERectangle[3]=CommonMethods.getClosestPointOnLine(vertices[lrtM[2]], k, vertices[lrtM[0]]);        
        }
        width=CommonMethods.getDistance(MAERectangle[0],MAERectangle[1]);
        height=CommonMethods.getDistance(MAERectangle[0],MAERectangle[3]);
    }
    public ArrayList <Point2D> getMAERectangle(){
        ArrayList <Point2D> ps=new ArrayList <Point2D>(4);
        for(int i=0;i<4;i++){
            ps.add(MAERectangle[i]);
        }
        return ps;
    }
    public void computeCentroid(){
        double x=0,y=0;
        for(int i=0;i<sides;i++){
            x+=vertices[i].dx;
            y+=vertices[i].dy;
        }
       centroid=new Point2D(x,y);
    }
    public void computeAngles(){
        int i0,i,i1;
        double angle;
        for(i=0;i<sides;i++){
            i0=i-1;
            if(i==0) i0=sides-1;
            i1=i+1;
            if(i1==sides) i1=0;
            angle=CommonMethods.getAngle(vertices[i0], vertices[i], vertices[i1]);
            angles[i]=Math.abs(angle);//it's a convex polygon, the angle will be positive regardless of sign convention
        }
    }
    public void getHeightAndWidth(int index, double[] wh, int lrt[]){
        //computes the height and width of the enclosing rectangle one of whose sides 
        //align with the side formed by the vertices[index] and vertices[(i+1)%sides]
        Point2D p1=vertices[index],p2=vertices[(index+1)%sides];
        int left=0,right=0,top=0,i;
        double angle=0.,angle0=0.;
        for(i=0;i<sides;i++){
            angle+=(180-angles[(index+1+i)%sides]);
            if(angle0<90.&&angle>=90) right=(index+1+i)%sides;
            if(angle0<180.&&angle>=180) top=(index+1+i)%sides;
            if(angle0<270.&&angle>=270) left=(index+1+i)%sides;
            angle0=angle;
        }
        Point2D p=CommonMethods.getClosestPointOnLine(p1, p2, vertices[top]);
        wh[1]=CommonMethods.getDistance(vertices[top], p);
        p=CommonMethods.getClosestPointOnLine(p1, p2, vertices[left]);
        Point2D pl=CommonMethods.getClosestPointOnLine(p1, p2, vertices[right]);
        wh[0]=CommonMethods.getDistance(p, pl);
        lrt[0]=left;
        lrt[1]=right;
        lrt[2]=top;
    }
    public int getSides(){
        return sides;
    }
    public double getWidth(){
        return width;       
    }
    public double getHeight(){
        return height;
    }
    public ArrayList<Point> getMECRectanglePoints(){
        ArrayList<Point> points=new ArrayList();
        Point2D p;
        for(int i=0;i<4;i++){
            p=MAERectangle[i];
            points.add(new Point(p.x,p.y));
        }
        return points;
    }
    public ArrayList<Point> getVertexPoints(){
        int i,len=vertices.length;
        ArrayList<Point> points=new ArrayList();
        Point2D pt;
        for(i=0;i<len;i++){
            pt=vertices[i];
            points.add(new Point(pt.x,pt.y));
        }
        return points;
    }
    public int drawPolygon(ImagePlus impl, Color c){
        ArrayList<Point> points=getVertexPoints();
        CommonMethods.drawPolygon(impl,c,points);
        return 1;
    }
    public int drawMECRectangle(ImagePlus impl, Color c){
        ArrayList<Point> points=getMECRectanglePoints();
        CommonMethods.drawPolygon(impl,c,points);
        return 1;        
    }
}
