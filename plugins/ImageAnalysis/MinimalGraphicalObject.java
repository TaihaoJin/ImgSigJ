/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import utilities.CustomDataTypes.intRange;
import java.util.ArrayList;
import utilities.CommonMethods;
import java.awt.Color;
import java.awt.Point;
import utilities.Geometry.ConvexPolygon;
import utilities.ArrayofArrays.IntRangeArray;
import utilities.ArrayofArrays.IntRangeArray2;
import utilities.Geometry.ConvexPolygon;
import utilities.Geometry.MinimalEnclosingCovexPolygon;
import utilities.Geometry.Point2D;
import utilities.ArrayofArrays.PointArray;
import utilities.ArrayofArrays.PointArray2;
import utilities.Geometry.ImageShapes.ImageShape;
/**
 *
 * @author Taihao
 */
public class MinimalGraphicalObject {
    protected int[][] pixels;
    protected PointArray2 innerContours =new PointArray2();
    protected PointArray2 innerObjectContours =new PointArray2();
    protected ArrayList <MinimalGraphicalObject> innerObjects=new ArrayList<MinimalGraphicalObject>();
    protected ArrayList <Point> contour=new ArrayList<Point>();
    protected intRange xRange=new intRange(),yRange=new intRange();
    protected int area, enclosedArea, perimeter, aveIntensity;
    protected IntRangeArray2 yStripes=new IntRangeArray2();//A Graphical Object is defined as a connected area in a image. The object can be discontinuous a long a given line.
                            //yStripes and xStripes is different ways to designate the positions of the pixeles of the object. yStripes store the ranges of x coordinates of the
                            //pixels of the objects at each y position, while xStripe store the ranges of y coordinates of the pixels of the objects at each x position.
    protected IntRangeArray2 yEnclosedStripes=new IntRangeArray2();
                            //yEnclosedStripes is the entire area enclosed by the contour, it cotains holes and inner objects.
    ArrayList <Point2D> MECPolygon;//Minimal enclosing ploygon.
    ArrayList <Point2D> MECRectangle;//Minimal enclosing rectangel.
    ImageShape signalShape;//used in later development (7/20/2010) instead of yStripes
    protected ImageShape enclosedShape;//used in later development (7/20/2010) instead of yEnclosedStripes. Is shape includes shapes of inner objects
    protected IntRangeArray2 xStripes=new IntRangeArray2();;
//    public abstract boolean matches(Point p);
    public intRange getXRange(){
        return xRange;
    }
    public intRange getYRange(){
        return yRange;
    }
    public IntRangeArray2 getYStripe(){
        return yStripes;
    }
    public IntRangeArray2 getXStripe(){
        return xStripes;
    }
    public ArrayList <Point> getContour(){
        return contour;
    }
    public int getArea(){
        return area;
    }
    public int getPerimeter(){
        return perimeter;
    }
    public int getAveIntensity(){
        return aveIntensity;
    }
    public void setContour(ArrayList<Point> contour){
        this.contour=contour;
    }
    public void setEnclosedShape(ImageShape shape){
        enclosedShape=shape;
    }
    public void setContour(int[][] pixels, ArrayList<Point> contour,intRange intensityRange){
        int size=contour.size();
        this.contour.clear();
        xRange.resetRange();
        yRange.resetRange();
        Point p;
        for(int i=0;i<size;i++){
            p=new Point(contour.get(i));
            this.contour.add(p);
            xRange.expandRange(p.x);
            yRange.expandRange(p.y);
        }
        perimeter=size;
        buildEnclosedObject(pixels, intensityRange);
        buildObject(pixels, intensityRange);
    }
    public void buildEnclosedObject(int[][]pixels, intRange intensityRange){//build the object enclosed by the contour, regardless of inner holes.
        int h=pixels.length,w=pixels[0].length;
        yEnclosedStripes.m_IntRangeArray2.clear();
        int yn=yRange.getMin(),yx=yRange.getMax();
        int size0=yx-yn+1,size=contour.size();
        Point p=new Point(),pn=new Point(),po=new Point();
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
        enclosedArea=0;
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
                if(!ContourFollower.isInside(contour, pts.get(j-1), new Point(xi,y), 1)){//outside of the object
/*                    if(xi>=w){
                        xi=xi;
                    }
                    if(intensityRange.contains(pixels[y][xi])){//neighboring objects
                        xi=xi;
                    }*/
                    continue;//I need to come back and check this block later. 2/19/2010, passed. xi could be equal to w when the contour passes the same point (w-1, y) more than once.
                }

                intRange ir=new intRange(xi,xf);
                enclosedArea+=ir.getMax()-ir.getMax()+1;
                irr.m_intRangeArray.add(ir);
            }
            yEnclosedStripes.m_IntRangeArray2.add(irr);
        }
        xRange=new intRange(xRange.getMin()+1,xRange.getMax()-1);
        yRange=new intRange(yRange.getMin()+1,yRange.getMax()-1);
    }
    public void buildObject(int[][]pixels, intRange intensityRange){//build the object enclosed by the contour, excluding the inner holes.
        int h=pixels.length,w=pixels[0].length;
        yStripes.m_IntRangeArray2.clear();
        int yn=yRange.getMin(),yx=yRange.getMax();//after the methods buildEnclosedObject was called, the yRange is the range of the y of the object enclosed by the contour.
        int size=yEnclosedStripes.m_IntRangeArray2.size(),size0;
        Point p=new Point(),pn=new Point(),po=new Point(),p0=new Point();
        int i,j,index;
        area=0;
        int x,xi,xf,y,pixel,nh;//nh: number of possible holes
        intRange seg;
        int size1;
        boolean WasInside=true;
        ArrayList<Point> innerContour=null;
        for(i=0;i<size;i++){
            y=yn+i;
            IntRangeArray irr=yEnclosedStripes.m_IntRangeArray2.get(i);
            size0=irr.m_intRangeArray.size();
            IntRangeArray irr1=new IntRangeArray();
            for(j=0;j<size0;j++){
                seg=irr.m_intRangeArray.get(j);
                xi=seg.getMin();
                xf=seg.getMax();
                intRange ir=new intRange();
                WasInside=true;
                for(x=xi;x<=xf;x++){
                    if(!intensityRange.contains(pixels[y][x])&&WasInside){//the point belong to a hole inside the enclosed object
                        irr1.m_intRangeArray.add(ir);
                        innerContour=CommonMethods.findContent(innerContours, new Point(x,y));
                        if(innerContour==null){//this is a new hole
                            innerContour=ContourFollower.getContour(pixels, w, h, new Point(x,y), new Point(x-1,y), intensityRange);
                            innerContours.m_pointArray2.add(new PointArray(innerContour));
                        }
                        p0.setLocation(x,y);
                        x++;
                        while(!CommonMethods.containsContent(innerContour, p0)||!intensityRange.contains(pixels[y][x])||!ContourFollower.isInside(innerContour, p0, new Point(x,y), -1)){//crossing to the opposite side of the contour
                            p0.setLocation(x,y);//need to check the above line later (10219)
                            x++;
                        }
                        x--;
                        ir=new intRange();
                        WasInside=false;
                    }else{
                        ir.expandRange(x);
                        WasInside=true;
                    }
                }
                irr1.m_intRangeArray.add(ir);
            }
            yStripes.m_IntRangeArray2.add(irr1);
        }
    }

    public boolean contains(Point p){
        if(!withinRange(p)) return false;
        int i,size;
        int index=p.y-yRange.getMin();
        size=yStripes.m_IntRangeArray2.get(index).m_intRangeArray.size();
        for(i=0;i<size;i++){
            if(yStripes.m_IntRangeArray2.get(index).m_intRangeArray.get(i).contains(p.x)) return true;
        }
        return false;
    }
    public boolean withinRange(Point p){
        return (xRange.contains(p.x)&&yRange.contains(p.y));
    }
    public void makeMECPolygon(){
        MinimalEnclosingCovexPolygon p=new MinimalEnclosingCovexPolygon();
        MECPolygon=p.getMECPolygon(yStripes, yRange);
    }
    public ArrayList <Point2D> getMECPolygon(){
        return MECPolygon;
    }
    public ArrayList <Point2D> getMECRectangle(){
        return MECRectangle;
    }
    public void makeMECRectangle(){
        ConvexPolygon p=new ConvexPolygon(MECPolygon);
        MECRectangle=p.getMAERectangle();
    }
    public double getCircularityRatio(){
        return (4.*Math.PI*area)/(perimeter*perimeter);
    }
    public ArrayList<Point> getInnerPoints(){
        ArrayList<Point> innerPoints=new ArrayList<Point>();
        IntRangeArray ira;
        intRange ir;
        int i,j,k,x,y,y0,len;
        int size=yStripes.m_IntRangeArray2.size();
        y0=yRange.getMin();
        for(i=0;i<size;i++){
            y=y0+i;
            ira=yStripes.m_IntRangeArray2.get(i);
            len=ira.m_intRangeArray.size();
            for(j=0;j<len;j++){
                ir=ira.m_intRangeArray.get(j);
                for(x=ir.getMin();x<=ir.getMax();x++){
                    innerPoints.add(new Point(x,y));
                }
            }
        }
        return innerPoints;
    }
    public PointArray2 getInnerContours(){
        return innerContours;
    }
}
