/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry.ImageShapes;
import java.awt.Point;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.IntRangeHandler;
import ij.IJ;
import ij.ImagePlus;
import utilities.CommonMethods;
import ij.gui.GenericDialog;
import utilities.ArrayofArrays.IntRangeArray;
import ij.WindowManager;
import ImageAnalysis.ContourFollower;
import java.awt.Color;
import utilities.CommonStatisticsMethods;
import utilities.Geometry.Point2D;
import utilities.Geometry.MinimalEnclosingCovexPolygon;
import utilities.Geometry.ConvexPolygon;
/**
 *
 * @author Taihao
 */
public class ImageShape {

    Point location;//the left top corner point of the boundary
    intRange m_xRange,m_yRange,m_xFrameRange,m_yFrameRange;//always use internal coordinates for m_xRange and m_yRange. The frame range is the range of the canvas
    int m_nArea,m_nPerimeter;

    ArrayList <IntRangeArray> m_xSegments=new ArrayList();
    ArrayList <IntRangeArray> m_ySegments=new ArrayList();
    ArrayList <intRange> m_xRanges,m_yRanges;//the range of x at given y
    String m_sDescription;
    public static final int CircleInt=0, RingInt=1, Circle_Dbl=2, Ring_Dbl=3, Complex=4;
    int m_nType;//Shape type, one of the above choices.
    boolean m_bConsistent;
    boolean m_bReflectiveBoundary;
    ArrayList<Point> m_cvPerimeterPoints;
    ArrayList<Point> m_cvOuterContour;
    ConvexPolygon m_cMECPolygon;
    double width,height;
    Point[] m_cMECRectangle;
    ArrayList<Double> m_dvDentDepths;
    
    public ImageShape(){
        location=new Point(0,0);
        m_xRange=new intRange();
        m_yRange=new intRange();
        m_xFrameRange=intRange.getMaxRange();
        m_yFrameRange=intRange.getMaxRange();
        m_xSegments=null;
        m_ySegments=null;
        m_nArea=0;
        m_nPerimeter=0;
        m_sDescription=new String();
        m_bConsistent=true;
        m_bReflectiveBoundary=false;
        m_cvPerimeterPoints=new ArrayList();
        m_cvOuterContour=new ArrayList();
        width=-1;
        height=-1;
    }
    void buildXsegments(ArrayList<IntRangeArray> xSegments){
        int len=xSegments.size(),lt;
        m_xSegments=new ArrayList();
        m_xRanges=new ArrayList();
        IntRangeArray ira;
        intRange ir,irt;
        ArrayList <intRange> xRanges;
        int i;
        m_nArea=0;
        for(int index=0;index<len;index++){
            xRanges=xSegments.get(index).m_intRangeArray;
            lt=xRanges.size();
            ira=new IntRangeArray();
            ir=new intRange();//ir is the range of x at this given y coordinate.
            for(i=0;i<lt;i++){
                irt=xRanges.get(i);
                ira.m_intRangeArray.add(new intRange(irt));
                ir.expandRange(irt);
                m_nArea+=irt.getRange();
            }
            m_xSegments.add(ira);
            m_xRanges.add(ir);
        }
    }
    void buildYsegments(ArrayList<IntRangeArray> ySegments){
        int len=ySegments.size(),lt;
        m_ySegments=new ArrayList();
        m_yRanges=new ArrayList();
        IntRangeArray ira;
        intRange ir,irt;
        ArrayList <intRange> yRanges;
        int i;
        int nArea=0;
        for(int index=0;index<len;index++){
            yRanges=ySegments.get(index).m_intRangeArray;
            lt=yRanges.size();
            ira=new IntRangeArray();
            ir=new intRange();
            for(i=0;i<lt;i++){
                irt=yRanges.get(i);
                ira.m_intRangeArray.add(new intRange(irt));
                ir.expandRange(irt);
                nArea+=irt.getRange();
            }
            m_ySegments.add(ira);
            m_yRanges.add(ir);
        }
        if(nArea!=m_nArea){
            m_bConsistent=false;
            IJ.error("inconsistent Area from x and y segments");
        }
    }
    public Point getLocation(){
        return location;
    }
    public ImageShape(boolean bDlg){
        this();
        if(bDlg){
            buildImageShapeDlg();
        }
    }
    public ImageShape(ImageShape is){
        this();
        buildImageShape(is);
    }
    public ImageShape(ArrayList<IntRangeArray> xSegments, ArrayList<IntRangeArray> ySegments){
        this();
        buildImageShape(xSegments,ySegments);
    }
    public void buildImageShape(ImageShape is){
        m_sDescription=is.getDescription();
        m_nType=is.getType();
        setLocation(is.getLocation());
        buildImageShape(is.getXsegments(),is.getYsegments());
        setFrameRanges(is.getXFrameRange(),is.getYFrameRange());
    }
    public String getDescription(){
        return m_sDescription;
    }
    public void buildImageShape(ArrayList<IntRangeArray> xSegments, ArrayList<IntRangeArray> ySegments){
        buildXsegments(xSegments);
        buildYsegments(ySegments);
        reAdjustCoordinates();
    }
    public ArrayList<IntRangeArray> getXsegments(){
        return m_xSegments;
    }
    public ArrayList<IntRangeArray> getYsegments(){
        return m_ySegments;
    }
    public intRange getXrange(){
        return m_xRange;
    }
    public intRange getYrange(){
        return m_yRange;
    }
    public intRange getXFrameRange(){
        return m_xFrameRange;
    }
    public intRange getYFrameRange(){
        return m_yFrameRange;
    }
    public void mergeShape(ImageShape is, int shiftX2, int shiftY2){//shiftX=is.location.x-location.x, shiftX=is.location.x-location.x
//        m_sDescription+=" merged with "+is.getDescription();
        m_nType=ImageShape.Complex;
        intRange xRange1=new intRange(m_xRange);
        intRange yRange1=new intRange(m_yRange);

        intRange xRange2=new intRange(is.getXrange());
        intRange yRange2=new intRange(is.getYrange());
        xRange2.shiftRange(shiftX2);
        yRange2.shiftRange(shiftY2);

        m_xRange.expandRange(xRange2);
        m_yRange.expandRange(yRange2);
        int shiftX1=-m_xRange.getMin();
        int shiftY1=-m_yRange.getMin();
        location.translate(-shiftX1, -shiftY1);

        m_xRange.shiftRange(shiftX1);
        m_yRange.shiftRange(shiftY1);
        xRange1.shiftRange(shiftX1);
        yRange1.shiftRange(shiftY1);
        xRange2.shiftRange(shiftX1);
        yRange2.shiftRange(shiftY1);
        shiftX2+=shiftX1;
        shiftY2+=shiftY1;
        m_xSegments=combinedIntRangeArrays(m_xSegments, is.getXsegments(), yRange1, yRange2, m_yRange, shiftX1, shiftX2);
        m_ySegments=combinedIntRangeArrays(m_ySegments, is.getYsegments(), xRange1, xRange2, m_xRange, shiftY1, shiftY2);
        reAdjustCoordinates();
    }

    ArrayList<IntRangeArray> combinedIntRangeArrays(ArrayList<IntRangeArray> iras1, ArrayList<IntRangeArray> iras2, intRange oRange1, intRange oRange2, intRange oRange, int hShift1, int hShift2){
        ArrayList <IntRangeArray> combinedRanges=new ArrayList();
        int o,h,id1,id2,id;
        int lh,lo=oRange.getRange(),oi=oRange.getMin(),oi1=oRange1.getMin(),oi2=oRange2.getMin();
        ArrayList<intRange> ira,irs;
        intRange ir;

        for(id=0;id<lo;id++){
            o=oi+id;
            irs=null;
            if(oRange1.contains(o)){
                irs=new ArrayList();
                id1=o-oi1;
                ira=iras1.get(id1).m_intRangeArray;
                lh=ira.size();
                for(h=0;h<lh;h++){
                    ir=ira.get(h);
                    ir.shiftRange(hShift1);
                    irs.add(ir);
                }
            }

            if(oRange2.contains(o)){
                if(irs==null) irs=new ArrayList();
                id2=o-oi2;
                ira=iras2.get(id2).m_intRangeArray;
                lh=ira.size();
                for(h=0;h<lh;h++){
                    ir=new intRange(ira.get(h));
                    ir.shiftRange(hShift2);
                    irs.add(ir);
                }
            }
            if(irs==null) irs=new ArrayList();//11829
            irs=combinedRanges(irs);
            combinedRanges.add(new IntRangeArray());
            combinedRanges.get(id).m_intRangeArray=irs;
        }
        return combinedRanges;
    }

    ArrayList<intRange> combinedRanges(ArrayList<intRange> irs0){
        ArrayList<intRange> irs=new ArrayList();
        int len=irs0.size();
        if(len==0) return irs;
        intRange ir=new intRange();
        int in,idn;
        idn=0;
        in=irs0.get(0).getMin();
        int i,j,it;
        for(i=0;i<len-1;i++){
            in=irs0.get(i).getMin();
            idn=i;
            for(j=i+1;j<len;j++){
                it=irs0.get(j).getMin();
                if(it<in){
                    in=it;
                    idn=j;
                }
            }
            if(idn>i){
                ir.reset(irs0.get(idn));
                irs0.get(idn).reset(irs0.get(i));
                irs0.get(i).reset(ir);
            }
        }
        it=0;
        intRange ir1;
        ir=irs0.get(0);
        irs.add(ir);
        it++;
        while(it<len){
            ir1=irs0.get(it);
            while(ir.overlapOrconnected(ir1)){
                ir.expandRange(ir1);
                it++;
                if(it<len){
                    ir1=irs0.get(it);
                }else{
                    break;
                }
            }
            if(it>=len) break;
            ir=ir1;
            irs.add(ir);
        }
        return irs;
    }

    public Point[] getInnerPoints(){//The coordinates of the points are in inner coordinate system,
        //therefore independent of the location of the ImageShape

        Point[] points=new Point[m_nArea];
        getInnerPoints(points);
        return points;
    }

    public Point[] getInnerPoints_ReflectiveBoundary(){//The coordinates of the points are in inner coordinate system,
        //therefore independent of the location of the ImageShape

        Point[] points=new Point[m_nArea];
        getInnerPoints_ReflectiveBoundary(points);
        return points;
    }

    public void getInnerPoints_ReflectiveBoundary(Point[] points){
        getInnerPoints(points);
        int len=points.length;
        int i;
        Point p;
        for(i=0;i<len;i++){
            p=points[i];
            reflectIntoFrame(p);
        }
    }

    public void reflectIntoFrame(Point p){
        p.setLocation(m_xFrameRange.mirrorIntoRange(p.x),m_yFrameRange.mirrorIntoRange(p.y));
    }
    
    public void getInnerPoints(Point[] points){//The coordinates of the points are in inner coordinate system,
        //therefore independent of the location of the ImageShape

        int len=m_yRange.getRange();
        int x,y,xi,xf,y0,segs,seg;
        int index=0;
        y0=m_yRange.getMin();
        intRange ir;
        ArrayList<intRange> irs;
        for(int i=0;i<len;i++){
            irs=m_xSegments.get(i).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                y=y0+i;
                xi=ir.getMin();
                xf=ir.getMax();
                for(x=xi;x<=xf;x++){
                    points[index]=new Point(x+location.x,y+location.y);
                    index++;
                }
            }
        }
    }

    public int getInnerPoints(ArrayList<Point> points){//returns the inner points within the frame
        points.clear();
        if(m_yRange.emptyRange()) return -1;
        int x,y,xi,xf,xo=location.x,yo=location.y;
        int index=0;
        int yi=yo+m_yRange.getMin(),yf=yo+m_yRange.getMax();
        int y0=yi;
        yi=m_yFrameRange.getClosestIntInRange(yi, -1);
        yf=m_yFrameRange.getClosestIntInRange(yf, 1);
        ArrayList<intRange> irs;
        intRange ir;
        int segs,seg;
        for(y=yi;y<=yf;y++){
            index=y-y0;
            if(m_xSegments.size()==0){
                y=y;
            }
            irs=m_xSegments.get(index).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                xi=ir.getMin()+xo;
                if(xi>m_xFrameRange.getMax()) break;
                xf=ir.getMax()+xo;
                if(xf<m_xFrameRange.getMin()) continue;
                xi=m_xFrameRange.getClosestIntInRange(xi, -1);
                xf=m_xFrameRange.getClosestIntInRange(xf, 1);
                for(x=xi;x<=xf;x++){
                    points.add(new Point(x,y));
                }
            }
        }
        return 1;
    }

    public int getInnerPointsY(ArrayList<Point> points){//returns the inner points within the frame
        points.clear();
        if(m_xRange.emptyRange()) return -1;
        int x,y,yi,yf,xo=location.x,yo=location.y;
        int index=0;
        int xi=xo+m_xRange.getMin(),xf=xo+m_xRange.getMax();
        int x0=xi;
        xi=m_xFrameRange.getClosestIntInRange(xi, -1);
        xf=m_xFrameRange.getClosestIntInRange(xf, 1);
        ArrayList<intRange> irs;
        intRange ir;
        int segs,seg;
        int len=m_ySegments.size();
        for(x=xi;x<=xf;x++){
            index=x-x0;
//        for(index=0;index<len;index++){
//            x=index+x0;
            irs=m_ySegments.get(index).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                yi=ir.getMin()+yo;
                if(yi>m_yFrameRange.getMax()) break;
                yf=ir.getMax()+yo;
                if(yf<m_yFrameRange.getMin()) continue;
                yi=m_yFrameRange.getClosestIntInRange(yi, -1);
                yf=m_yFrameRange.getClosestIntInRange(yf, 1);
                for(y=yi;y<=yf;y++){
                    points.add(new Point(x,y));
                }
            }
        }
        return 1;
    }

    public void getInnerPointReplacement(int dx, int dy, ArrayList <Point> newPoints, ArrayList<Point> oldPoints){
    //method getInnerPointReplacement computes the replacement of the inner points when the position of the shape change by dx,dy.
    //new points are the points now become part of the inner points, and oldPoints are the points that used to be part of the inner points at old position, but not at new position.

       if(dx==0){
             if(dy==1)
             {
                 getInnerPointReplacement_downward(newPoints,oldPoints);
             }else if(dy==-1){
                 getInnerPointReplacement_upward(newPoints,oldPoints);
             }
        }else if(dy==0){
            if(dx==1){
                 getInnerPointReplacement_rightward(newPoints,oldPoints);
            }else if(dx==-1){
                 getInnerPointReplacement_leftward(newPoints,oldPoints);
            }
       }
       if(oldPoints.size()==0&&newPoints.size()==0){
//            IJ.error("illegal move for the methods ImageShape.getInnerPointReplacement");//it is allowed to have a null move, dx==0 and dy==0
       }
    }
    void getInnerPointReplacement_upward(ArrayList <Point> newPoints, ArrayList<Point> oldPoints){
        oldPoints.clear();
        newPoints.clear();
        int xo=location.x,yo=location.y;
        int xi=xo+m_xRange.getMin(),xf=xo+m_xRange.getMax();
        int x0=xi;
        xi=m_xFrameRange.getClosestIntInRange(xi, -1);
        xf=m_xFrameRange.getClosestIntInRange(xf, 1);
        int x,y;
        int index;
        ArrayList <intRange> irs;
        intRange ir;
        int segs,seg;
        for(x=xi;x<=xf;x++){
            index=x-x0;
            irs=m_ySegments.get(index).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                y=ir.getMin()+yo-1;
                if(m_yFrameRange.contains(y)) newPoints.add(new Point(x,y));
                y=ir.getMax()+yo;
                if(m_yFrameRange.contains(y)) oldPoints.add(new Point(x,y));
            }
        }
    }
    void getInnerPointReplacement_downward(ArrayList <Point> newPoints, ArrayList<Point> oldPoints){
        oldPoints.clear();
        newPoints.clear();
        int xo=location.x,yo=location.y;
        int xi=xo+m_xRange.getMin(),xf=xo+m_xRange.getMax();
        int x0=xi;
        xi=m_xFrameRange.getClosestIntInRange(xi, -1);
        xf=m_xFrameRange.getClosestIntInRange(xf, 1);
        int x,y;
        int index;
        ArrayList <intRange> irs;
        intRange ir;
        int segs,seg;
        for(x=xi;x<=xf;x++){
            index=x-x0;
            irs=m_ySegments.get(index).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                y=ir.getMin()+yo;
                if(m_yFrameRange.contains(y)) oldPoints.add(new Point(x,y));
                y=ir.getMax()+yo+1;
                if(m_yFrameRange.contains(y)) newPoints.add(new Point(x,y));
            }
        }
    }
    void getInnerPointReplacement_leftward(ArrayList <Point> newPoints, ArrayList<Point> oldPoints){
        oldPoints.clear();
        newPoints.clear();
        int xo=location.x,yo=location.y;
        int yi=yo+m_yRange.getMin(),yf=yo+m_yRange.getMax();
        int y0=yi;
        yi=m_yFrameRange.getClosestIntInRange(yi, -1);
        yf=m_yFrameRange.getClosestIntInRange(yf, 1);
        int x,y;
        int index;
        ArrayList <intRange> irs;
        intRange ir;
        int segs,seg;
        for(y=yi;y<=yf;y++){
            index=y-y0;
            irs=m_xSegments.get(index).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                x=ir.getMin()+xo-1;
                if(m_xFrameRange.contains(x)) newPoints.add(new Point(x,y));
                x=ir.getMax()+xo;
                if(m_xFrameRange.contains(x)) oldPoints.add(new Point(x,y));
            }
        }
    }
    void getInnerPointReplacement_rightward(ArrayList <Point> newPoints, ArrayList<Point> oldPoints){
        oldPoints.clear();
        newPoints.clear();
        int xo=location.x,yo=location.y;
        int yi=yo+m_yRange.getMin(),yf=yo+m_yRange.getMax();
        int y0=yi;
        yi=m_yFrameRange.getClosestIntInRange(yi, -1);
        yf=m_yFrameRange.getClosestIntInRange(yf, 1);
        int x,y;
        int index;
        ArrayList <intRange> irs;
        intRange ir;
        int segs,seg;
        for(y=yi;y<=yf;y++){
            index=y-y0;
            irs=m_xSegments.get(index).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                x=ir.getMin()+xo;
                if(m_xFrameRange.contains(x)) oldPoints.add(new Point(x,y));
                x=ir.getMax()+xo+1;
                if(m_xFrameRange.contains(x)) newPoints.add(new Point(x,y));
            }
        }
    }
    public boolean exceedsFrame_CenterPosition(Point position){
        boolean b=false;
        intRange xRange=new intRange(m_xRange);
        intRange yRange=new intRange(m_yRange);
        xRange.recenter(position.x);
        yRange.recenter(position.y);
        if(!m_xFrameRange.enclosed(xRange))b=true;
        if(!m_yFrameRange.enclosed(yRange))b=true;
        return b;
    }
/*    public boolean exceedsFrame(){
        return exceedsFrame(location);
    }*/
    public void setLocation(Point p){
        location.setLocation(p);
    }
    public void setCenter(Point p){
        location.setLocation(p.x-m_xRange.getRange()/2,p.y-m_yRange.getRange()/2);
    }
    public void setCenter(int x, int y){
        location.setLocation(x-m_xRange.getRange()/2,y-m_yRange.getRange()/2);
    }
    public boolean outOfFrame(Point position){
        if(!m_xFrameRange.contains(position.x)) return true;
        if(!m_yFrameRange.contains(position.y)) return true;
        return false;
    }
    public Point getCenter(){
        return new Point(location.x+m_xRange.getRange()/2,location.y+m_yRange.getRange()/2);
    }
    public void markShape(ImagePlus impl, int dx, int dy){
        impl.setTitle(impl.getTitle()+" "+m_sDescription);
        int h=impl.getHeight(), w=impl.getWidth();
        setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        int pixels0[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels0);
        ArrayList<Point> innerPoints=new ArrayList();
        ArrayList<Point> newPoints=new ArrayList();
        ArrayList<Point> oldPoints=new ArrayList();
        getInnerPoints(innerPoints);
        getInnerPointReplacement(dx,dy,newPoints,oldPoints);
        int i;
        Point p;
        int len=innerPoints.size();
        for(i=0;i<len;i++){
            p=innerPoints.get(i);
            pixels0[p.y][p.x]=200;
        }

        len=newPoints.size();
        for(i=0;i<len;i++){
            p=newPoints.get(i);
            pixels0[p.y][p.x]=250;
        }

        len=oldPoints.size();
        for(i=0;i<len;i++){
            p=oldPoints.get(i);
            pixels0[p.y][p.x]=100;
        }

        CommonMethods.setPixels(impl, pixels0);
    }

    public void markShapeY(ImagePlus impl, int dx, int dy){
        impl.setTitle(impl.getTitle()+" "+m_sDescription);
        int h=impl.getHeight(), w=impl.getWidth();
        setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        int pixels0[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels0);
        ArrayList<Point> innerPoints=new ArrayList();
        ArrayList<Point> newPoints=new ArrayList();
        ArrayList<Point> oldPoints=new ArrayList();
        getInnerPointsY(innerPoints);
        getInnerPointReplacement(dx,dy,newPoints,oldPoints);
        int i;
        Point p;
        int len=innerPoints.size();
        for(i=0;i<len;i++){
            p=innerPoints.get(i);
            pixels0[p.y][p.x]=200;
        }

        len=newPoints.size();
        for(i=0;i<len;i++){
            p=newPoints.get(i);
            pixels0[p.y][p.x]=250;
        }

        len=oldPoints.size();
        for(i=0;i<len;i++){
            p=oldPoints.get(i);
            pixels0[p.y][p.x]=100;
        }

        CommonMethods.setPixels(impl, pixels0);
    }
    public boolean contains0(Point p){
        boolean b;
        int x=p.x-location.x,y=p.y-location.y;
        int yIndex=getYindex(y);
        if(yIndex<0) return false;
        if(!m_xRanges.get(yIndex).contains(x)) return false;
        if(getXSegIndex(yIndex,x)>=0) return true;
        return false;
    }
    public boolean contains(Point p){
        return contains(p.x,p.y);
    }
    public boolean contains(int x, int y){
        boolean b;
        x-=location.x;
        y-=location.y;
        int yIndex=getYindex(y);
        if(yIndex<0) return false;
        if(!m_xRanges.get(yIndex).contains(x)) return false;
        if(getXSegIndex(yIndex,x)>=0) return true;
        return false;
    }
    public int getYindex(int y){
        if(m_yRange.contains(y)){
            return y-m_yRange.getMin();
        }
        return -1;
    }
    public int getXSegIndex(int yIndex, int x){//the caller of this method need to insure yIndex is in correct
        //range
        ArrayList<intRange> irs;
        intRange ir;
        int segs,seg;
            irs=m_xSegments.get(yIndex).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                if(ir.contains(x)) return seg;
                if(ir.getMin()>x) return -seg;
            }
        return -segs;
    }
    public int getXindex(int x){
        if(m_yRange.contains(x)){
            return x-m_yRange.getMin();
        }
        return -1;
    }
    public int getYSegIndex(int xIndex, int y){//the caller of this method need to insure yIndex is in correct
        //range
        ArrayList<intRange> irs;
        intRange ir;
        int segs,seg;
            irs=m_ySegments.get(xIndex).m_intRangeArray;
            segs=irs.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                if(ir.contains(y)) return seg;
                if(ir.getMin()>y) return -seg;
            }
        return -segs;
    }
    public boolean connected(ImageShape shape1, int xShift, int yShift){
        intRange xRange1=shape1.m_xRange,yRange1=shape1.m_yRange;
        xRange1.shiftRange(xShift);
        yRange1.shiftRange(yShift);
        if(!xRange1.overlapOrconnected(m_xRange)) return false;
        intRange yRange=m_yRange.overlappedRange(yRange1);
        if(yRange.emptyRange()) return false;

        int y0=m_yRange.getMin();
        int y10=yRange1.getMin();
        int segs,seg,xi,xf;
        int segs1,seg1,xi1,xf1;

        ArrayList<IntRangeArray> xSegments1=shape1.getXsegments();

        int yi=yRange.getMin(),yf=yRange.getMax(),id,id1;
        ArrayList<intRange> irs,irs1;
        intRange xRange;
        intRange ir,ir1;
        for(int y=yi;y<=yf;y++){
            id=y-y0;
            id1=y-y10;
            irs=m_xSegments.get(id).m_intRangeArray;
            irs1=xSegments1.get(id1).m_intRangeArray;
            segs=irs.size();
            segs1=irs1.size();
            for(seg=0;seg<segs;seg++){
                ir=irs.get(seg);
                for(seg1=0;seg1<segs1;seg1++){
                    ir1=irs1.get(seg1);
                    ir1.shiftRange(xShift);
                    if(!ir.overlapOrconnected(ir1)) continue;
                    return true;
                }
            }
        }
        return false;
    }
    public void excludeShape(ImageShape is,int shiftX2, int shiftY2){
//        m_sDescription+=" excluded "+is.getDescription();
        m_nType=ImageShape.Complex;
        intRange xRange1=new intRange(m_xRange);
        intRange yRange1=new intRange(m_yRange);

        intRange xRange2=new intRange(is.getXrange());
        intRange yRange2=new intRange(is.getYrange());
        xRange2.shiftRange(shiftX2);
        yRange2.shiftRange(shiftY2);

        m_xSegments=excludedIntRangeArrays(m_xSegments, is.getXsegments(), yRange1, yRange2, shiftX2);
        m_ySegments=excludedIntRangeArrays(m_ySegments, is.getYsegments(), xRange1, xRange2, shiftY2);
        reAdjustCoordinates();
    }

    ArrayList<IntRangeArray> excludedIntRangeArrays(ArrayList<IntRangeArray> iras1, ArrayList<IntRangeArray> iras2, intRange oRange1, intRange oRange2, int hShift){
        ArrayList <IntRangeArray> excludedRanges=new ArrayList();
        int o,h,id1,id2,id,lt;
        int lh,lo,oi1=oRange1.getMin(),of1=oRange1.getMax(),oi2=oRange2.getMin();
        ArrayList<intRange> irs1,irs2,irst;
        intRange ir;
        IntRangeArray ira;

        for(o=oi1;o<=of1;o++){
            id1=o-oi1;
            irs1=iras1.get(id1).m_intRangeArray;
            if(oRange2.contains(o)){
                id2=o-oi2;
                irs2=new ArrayList();
                irst=iras2.get(id2).m_intRangeArray;
                lt=iras2.get(id2).m_intRangeArray.size();
                for(id=0;id<lt;id++){
                    ir=new intRange(irst.get(id));
                    ir.shiftRange(hShift);
                    irs2.add(ir);
                }
            }else{
                irs2=new ArrayList();
            }
            ira=new IntRangeArray();
            ira.m_intRangeArray=excludedRanges(irs1,irs2);
            excludedRanges.add(ira);
        }
        return excludedRanges;
    }

    ArrayList<intRange> excludedRanges0(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        ArrayList<intRange> irs=new ArrayList(),irst;
        int segs1=irs1.size(),seg1,segs2=irs2.size(),seg2,segs,seg;
        intRange ir1,ir2;
        for(seg1=0;seg1<segs1;seg1++){
            ir1=irs1.get(seg1);
            if(segs2==0){
                irs.add(ir1);
                continue;
            }
            for(seg2=0;seg2<segs2;seg2++){
                ir2=irs2.get(seg2);
                irst=ir1.excludedNonEmptyRanges(ir2);
                segs=irst.size();
                for(seg=0;seg<segs;seg++){
                    irs.add(irst.get(seg));
                }
            }
        }
        return irs;
    }

    ArrayList<intRange> excludedRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        ArrayList<intRange> irs1t,irst,irs=new ArrayList();
        int segs1=irs1.size(),seg1,segs2=irs2.size(),seg2,segs,seg;
        intRange ir1,ir2;

        for(seg=0;seg<segs1;seg++){
            irs.add(new intRange(irs1.get(seg)));
        }

        for(seg2=0;seg2<segs2;seg2++){

            ir2=irs2.get(seg2);
            if(segs2==0){
                continue;
            }
            irs1t=irs;
            segs1=irs1t.size();
            irs=new ArrayList();
            for(seg1=0;seg1<segs1;seg1++){
                ir1=irs1t.get(seg1);
                irst=ir1.excludedNonEmptyRanges(ir2);
                segs=irst.size();
                for(seg=0;seg<segs;seg++){
                    irs.add(irst.get(seg));
                }
            }
        }
        return irs;
    }

    public void eraseShape(){        
        m_xSegments.clear();
        m_ySegments.clear();
        m_nArea=0;
        m_yRange.resetRange();
        m_xRange.resetRange();
    }

    int reAdjustCoordinates(){//reset the coordinates so that the point (0,0) become the left top corner. Also the m_xRange and m_yRange are set accordingly.
        int xMin=Integer.MAX_VALUE,yMin=Integer.MAX_VALUE;
        int xMax=Integer.MIN_VALUE,yMax=Integer.MIN_VALUE;
        intRange ir;
        IntRangeArray ira;
        if(m_xSegments.size()==0||m_ySegments.size()==0){
            eraseShape();
            return -1;
        }
        ArrayList<IntRangeArray> xSegments=m_xSegments,ySegments=m_ySegments;
        m_xSegments=new ArrayList();
        m_ySegments=new ArrayList();
        m_xRanges=new ArrayList();
        m_yRanges=new ArrayList();
        m_nArea=0;
        int nAy=0;
        int i,j,x,y,lt,firstI=0,lastI=0,num=0;
        int len=xSegments.size();
        for(i=0;i<len;i++){
            ira=xSegments.get(i);
            lt=ira.m_intRangeArray.size();
            if(lt==0){
                if(num==0) continue;//skip empty segments only from the beginning
            }else{
                lastI=num;
            }
            num++;
            m_xSegments.add(ira);
            if(lt==0) continue;
            x=ira.m_intRangeArray.get(0).getMin();
            if(x<xMin) xMin=x;
        }

        for(i=num-1;i>lastI;i--){
            m_xSegments.remove(i);
        }

        num=m_xSegments.size();
        ArrayList<intRange> irs;
        for(i=0;i<num;i++){
            irs=m_xSegments.get(i).m_intRangeArray;
            lt=irs.size();
            if(lt==0){
                m_xRanges.add(new intRange());
                continue;
            }
            for(j=0;j<lt;j++){
                ir=irs.get(j);
                m_nArea+=ir.getRange();
                ir.shiftRange(-xMin);
            }

            x=irs.get(lt-1).getMax();
            if(x>xMax) xMax=x;
            m_xRanges.add(new intRange(irs.get(0).getMin(),x));
        }

        nAy=0;
        m_xRange.setRange(0,xMax);
        len=ySegments.size();
        num=0;
        for(i=0;i<len;i++){
            ira=ySegments.get(i);
            lt=ira.m_intRangeArray.size();
            if(lt==0){
                if(num==0) continue;//skip empty segments only from the beginning
            }else{
                lastI=num;
            }
            num++;
            m_ySegments.add(ira);
            if(lt==0)continue;
            y=ira.m_intRangeArray.get(0).getMin();
            if(y<yMin) yMin=y;
        }

        for(i=num-1;i>lastI;i--){
            m_ySegments.remove(i);
        }

        num=m_ySegments.size();
        for(i=0;i<num;i++){
            irs=m_ySegments.get(i).m_intRangeArray;
            lt=irs.size();
            if(lt==0){
                m_yRanges.add(new intRange());
                continue;
            }
            
            for(j=0;j<lt;j++){
                ir=irs.get(j);
                nAy+=ir.getRange();
                ir.shiftRange(-yMin);
            }

            y=irs.get(lt-1).getMax();
            if(y>yMax) yMax=y;
            m_yRanges.add(new intRange(irs.get(0).getMin(),y));
        }
        if(nAy!=m_nArea) {
            IJ.error("inconsistent area counting along x and y axis");
            m_bConsistent=false;
        }
        m_yRange.setRange(0,yMax);
        location.translate(xMin, yMin);
        return 1;
    }

    public void setFrameRanges(intRange xRange, intRange yRange){
        m_xFrameRange.reset(xRange);
        m_yFrameRange.reset(yRange);
    }

    void calArea(){
        int len=m_xSegments.size(),lt,i,j;
        m_nArea=0;
        IntRangeArray ira;
        for(i=0;i<len;i++){
            ira=m_xSegments.get(i);
            lt=ira.m_intRangeArray.size();
            for(j=0;j<lt;j++){
                m_nArea+=ira.m_intRangeArray.get(j).getRange();
            }
        }
    }
    public int getArea(){
        calArea();
        return m_nArea;
    }
    public void buildImageShapeDlg(){        GenericDialog gd=new GenericDialog("ImageShape Construction");
        String labe="choose ImageShape type";
        ArrayList <String> vcItems=new ArrayList();
        
        vcItems.add("Circle");
        vcItems.add("Ring");
        
        int len=vcItems.size();
        
        String[] items=new String[len];
        int i,j;
        for(i=0;i<len;i++){
            items[i]=vcItems.get(i);
        }
        
        String defaultItem="Circle";
        gd.addChoice(labe, items, defaultItem);
        
        gd.showDialog();
        
        if(gd.wasOKed()){
            buildImageShape(gd.getNextChoiceIndex());
        }
    }
    void buildImageShape(int choiceIndex){
        switch(choiceIndex){
            case 0:
                CircleImage circle=new CircleImage(true);
                buildImageShape(circle);
                break;
            case 1:
                Ring ring=new Ring(true);
                buildImageShape(ring);
                break;
            default:
                IJ.error("invalid choice of building ImageShape");
                break;
        }
    }
    boolean needAdjust(){
        boolean b=false;
        int len;
        if(m_xRange.getMin()!=0) b=true;
        if(m_xSegments.get(0).m_intRangeArray.size()==0) b=true;
        len=m_xSegments.size();
        if(m_xSegments.get(len-1).m_intRangeArray.size()==0) b=true;

        if(m_yRange.getMin()!=0) b=true;
        if(m_ySegments.get(0).m_intRangeArray.size()==0) b=true;
        len=m_ySegments.size();
        if(m_ySegments.get(len-1).m_intRangeArray.size()==0) b=true;
        return b;
    }
    public void setDescription(String sDescription){
        m_sDescription=sDescription;
    }
    public void mergeShape(ImageShape shape){
        m_nType=ImageShape.Complex;
        Point location2=shape.getLocation();
        int xShift=location2.x-location.x, yShift=location2.y-location.y;
        mergeShape(shape, xShift, yShift);
    }
    public void excludeShape(ImageShape shape){
        Point location2=shape.getLocation();
        int xShift=location2.x-location.x, yShift=location2.y-location.y;
        excludeShape(shape, xShift, yShift);
    }
    public ImageShape getOverlap(ImageShape shape2){//need to correct for the effacts of readusting coordinate each time. not usable right now.
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus impl1=CommonMethods.cloneImage(impl);
        impl1.setTitle("---shape2only---");
        ImageShape overlap=new ImageShape(shape2);
        ImageShape shape2only=new ImageShape(shape2);
        shape2only.excludeShape(this);
        shape2only.markShape(impl1, 0, 0);
        impl1.show();
        overlap.excludeShape(shape2only);
        ImagePlus impl2=CommonMethods.cloneImage(impl);
        impl2.setTitle("---shape2only---");
        overlap.markShape(impl2, 0, 0);
        impl2.show();
        return overlap;
    }

    public void overlapShape0(ImageShape shape2){//need to correct for the effacts of readusting coordinate each time. not usable right now.
        m_nType=ImageShape.Complex;
        ImageShape thisOnly=new ImageShape(this);
        thisOnly.excludeShape(shape2);
        excludeShape(thisOnly);
    }

    public void overlapShape(ImageShape shape2){//need to correct for the effacts of readusting coordinate each time. not usable right now.
        m_nType=ImageShape.Complex;
        Point location2=shape2.getLocation();
        int xShift=location2.x-location.x, yShift=location2.y-location.y;
        overlapShape(shape2, xShift, yShift);
    }

    public void overlapShape(ImageShape shape2, int shiftX2, int shiftY2){
        m_nType=ImageShape.Complex;
        intRange xRange1=new intRange(m_xRange);
        intRange yRange1=new intRange(m_yRange);

        intRange xRange2=new intRange(shape2.getXrange());
        intRange yRange2=new intRange(shape2.getYrange());
        xRange2.shiftRange(shiftX2);
        yRange2.shiftRange(shiftY2);

        m_xSegments=overlappingIntRangeArrays(m_xSegments, shape2.getXsegments(), yRange1, yRange2, shiftX2);
        m_ySegments=overlappingIntRangeArrays(m_ySegments, shape2.getYsegments(), xRange1, xRange2, shiftY2);
        reAdjustCoordinates();
    }

    ArrayList<IntRangeArray> overlappingIntRangeArrays(ArrayList<IntRangeArray> iras1, ArrayList<IntRangeArray> iras2, intRange oRange1, intRange oRange2, int hShift){
        ArrayList <IntRangeArray> overlappingRanges=new ArrayList();
        int o,h,id1,id2,id,lt;
        intRange commonRangeO=oRange1.overlappedRange(oRange2);
        int oi=commonRangeO.getMin(),of=commonRangeO.getMax();
        int lh,lo,oi1=oRange1.getMin(),oi2=oRange2.getMin();
        ArrayList<intRange> irs1,irs2,irst;
        intRange ir;
        IntRangeArray ira;

        for(o=oi;o<=of;o++){
            ira=new IntRangeArray();
            id1=o-oi1;
            irs1=iras1.get(id1).m_intRangeArray;

            id2=o-oi2;
            irs2=iras2.get(id2).m_intRangeArray;
             
            ira.m_intRangeArray=IntRangeHandler.getOverlappingRanges(irs1,irs2, hShift);
            overlappingRanges.add(ira);
        }
        return overlappingRanges;
    }

    public static ImageShape getOverlap(ArrayList<ImageShape> shapes){//need to correct for the effacts of readusting coordinate each time. not usable right now.
        int len=shapes.size();
        if(len==0) return null;
        ImageShape overlap=new ImageShape(shapes.get(0));
        for(int i=1;i<len;i++){
            overlap.overlapShape(shapes.get(i));
        }
        return overlap;
    }
    public static ImageShape getMergedShape(ArrayList<ImageShape> shapes){//need to correct for the effacts of readusting coordinate each time. not usable right now.
        int len=shapes.size();
        if(len==0) return null;
        ImageShape overlap=new ImageShape(shapes.get(0));
        for(int i=1;i<len;i++){
            overlap.mergeShape(shapes.get(i));
        }
        return overlap;
    }
    
    public void translate(int dx, int dy){
        location.translate(dx, dy);
    }
    
    public boolean overlapping(ImageShape shape2){
        m_nType=ImageShape.Complex;
        Point location2=shape2.getLocation();
        int xShift=location2.x-location.x, yShift=location2.y-location.y;

        intRange xRange2=shape2.getXrange(), yRange2=shape2.getYrange();

//        yRange2.shiftRange(yShift);
        if(!m_xRange.overlapped(xRange2,xShift)) return false;
        if(!m_yRange.overlapped(yRange2,yShift)) return false;

        ArrayList<IntRangeArray> xSegments=shape2.getXsegments();
        yRange2=yRange2.overlappedRange(yShift,m_yRange);
        int yi=yRange2.getMin(), yf=yRange2.getMax(),y,id2;
        for(y=yi;y<=yf;y++){
            id2=y-yShift;
            if(IntRangeHandler.overlapping(m_xSegments.get(y).m_intRangeArray, xSegments.get(id2).m_intRangeArray, xShift))return true;
        }
        return false;
    }
    public int getType(){
        return m_nType;
    }
    public boolean consistent(){
        return m_bConsistent;
    }
    public Point[] getConnerPoints(){//return leftTop, rightTop, leftBottom and rightBottom conners
        Point[] conners=new Point[4];
        int len=m_xSegments.size();
        int i,len1,xi,xf;
        for(i=0;i<4;i++){
            conners[i]=new Point(location);
        }
        intRange ir;
        ArrayList<intRange> ira;
        for(i=0;i<len;i++){
            ira=m_xSegments.get(i).m_intRangeArray;
            len1=ira.size();
            xi=ira.get(0).getMin();
            xf=ira.get(len1-1).getMax();
            if(xf>xi){
                conners[0].setLocation(location.x+xi,location.y+i);
                conners[1].setLocation(location.x+xf,location.y+i);
                break;
            }
        }
        for(i=len-1;i>=0;i--){
            ira=m_xSegments.get(i).m_intRangeArray;
            len1=ira.size();
            xi=ira.get(0).getMin();
            xf=ira.get(len1-1).getMax();
            if(xf>xi){
                conners[2].setLocation(location.x+xi,location.y+i);
                conners[3].setLocation(location.x+xf,location.y+i);
                break;
            }
        }
        return conners;
    }
    void buildOuterContour(){
        int w=m_xRange.getRange()+2,h=m_yRange.getRange()+2;
        int pixel=1,pixels[][]=new int[h][w],i,j;
        Point po=new Point(location);
        setLocation(new Point(1,1));
        Point firstPoint=getFirstPoint();
        ImageShapeHandler.addValue(pixels, this, pixel);
        m_cvOuterContour=ContourFollower.getContour(pixels, w, h,new Point(firstPoint.x,firstPoint.y-1), firstPoint, pixel, pixel);
        CommonStatisticsMethods.translatePoints(m_cvOuterContour, -1, -1);//to keep contour location invariant
        setLocation(po);
    }
    public Point getFirstPoint(){
        Point p=new Point(m_xSegments.get(0).m_intRangeArray.get(0).getMin(),m_yRange.getMin());
        p.translate(location.x, location.y);
        return p;
    }
    void buildPerimeter(){
        int w=m_xRange.getRange()+2,h=m_yRange.getRange()+2;
        int pixel=1,pixels[][]=new int[h][w],i,j;
        Point po=new Point(location);
        setLocation(new Point(1,1));
        Point firstPoint=getFirstPoint();
        CommonStatisticsMethods.setElements(pixels,getOuterContour(),pixel);
        m_cvPerimeterPoints=ContourFollower.getContour(pixels, w, h, firstPoint,new Point(firstPoint.x,firstPoint.y-1), pixel, pixel,true);
        CommonStatisticsMethods.translatePoints(m_cvPerimeterPoints,-1,-1);//to keep perimeter location invariant
        setLocation(po);
    }
    public ArrayList<Point> getOuterContour(){
        if(m_cvOuterContour.isEmpty())buildOuterContour();
        ArrayList <Point> contour=CommonStatisticsMethods.getCopy(m_cvOuterContour);
        CommonStatisticsMethods.translatePoints(contour, location.x, location.y);
        ContourFollower.removeOffBoundPoints(m_xFrameRange,m_yFrameRange, contour);
        return contour;
    }
    public ArrayList<Point> getPerimeterPoints(){//the callers need to remove points outside of the image dimesion by
        //calling ContourFollower.removeOffBoundPoints(w, h, perimeter);
        if(m_cvPerimeterPoints.isEmpty()) buildPerimeter();
        ArrayList <Point> perimeter=CommonStatisticsMethods.getCopy(m_cvPerimeterPoints);
        CommonStatisticsMethods.translatePoints(perimeter, location.x, location.y);
        return perimeter;
    }
    public Point getInnerCoordinates(Point pt){
        Point p=new Point(pt);
        p.translate(-location.x, -location.y);
        return p;
    }
    public boolean isOnEdge(Point pt){
        if(!contains(pt)) return false;
        int x=pt.x,y=pt.y,dx,dy;
        for(dy=-1;dy<=1;dy++){
            for(dx=-1;dx<=1;dx++){
                if(!contains(x+dx,y+dy)) return true;
            }
        }
        return false;
    }
    public boolean isOnEdge(int x, int y){
        if(!contains(x,y)) return false;
        int dx,dy;
        for(dy=-1;dy<=1;dy++){
            for(dx=-1;dx<=1;dx++){
                if(!contains(x+dx,y+dy)) return true;
            }
        }
        return false;
    }
    public double getWidth(){
        if(width<0) buildMECPolygon();
        return width;
    }
    public double getHeight(){
        if(height<0) buildMECPolygon();
        return height;
    }
    public void buildMECPolygon(){
        intRange yRange=new intRange(m_yRange);
        yRange.shiftRange(location.y);
        ArrayList<IntRangeArray> xSegments=new ArrayList();
        IntRangeArray ira0,ira=new IntRangeArray();
        int j,len1,x0=location.x,y0=location.y;
        intRange ir,ir0;
        for(int it=0;it<m_xSegments.size();it++){
            ira0=m_xSegments.get(it);
            ira=new IntRangeArray();
            len1=ira0.m_intRangeArray.size();
            for(j=0;j<len1;j++){
                ir=new intRange(ira0.m_intRangeArray.get(j));
                ir.shiftRange(x0);
                ira.m_intRangeArray.add(ir);
            }
            xSegments.add(ira);
        }
        m_cMECPolygon=new ConvexPolygon(MinimalEnclosingCovexPolygon.getMECPolygon(xSegments, yRange));
        ArrayList<Point2D> points=m_cMECPolygon.getMAERectangle();
        int i,len=points.size();
        m_cMECRectangle=new Point[len];
        
        for(i=0;i<len;i++){
            m_cMECRectangle[i]=new Point(points.get(i).x,points.get(i).y);
        } 
        width=m_cMECPolygon.getWidth();
        height=m_cMECPolygon.getHeight();
        calDentDepths();
    }
    public void calDentDepths(){
        if(m_cMECPolygon==null) buildMECPolygon();
        ArrayList<Point> vertices=m_cMECPolygon.getVertexPoints();
        m_dvDentDepths=new ArrayList();
        ArrayList<Point> piremeterPoints=getPerimeterPoints();
        int i,len=piremeterPoints.size(),p,p0,p1,len1;
        ArrayList<Integer> positions=new ArrayList();
        Point pt=new Point(),pt0,pt1;;
        for(i=0;i<len;i++){
            m_dvDentDepths.add(0.);
//            pt.setLocation(piremeterPoints.get(i).x-location.x,piremeterPoints.get(i).y-location.y);
            pt.setLocation(piremeterPoints.get(i));
            p=CommonStatisticsMethods.getPositionOfPoint(vertices, pt);
            if(p<0) continue;
            positions.add(i);
        }
        len1=positions.size();
        if(len1>0){
            p0=positions.get(len1-1);
            pt0=piremeterPoints.get(p0);
            double dist;
            for(i=0;i<positions.size();i++){
                p1=positions.get(i);            
                pt1=piremeterPoints.get(p1);
                p=CommonStatisticsMethods.getCircularIndex(p0+1, len);
                while(p!=p1){
                    pt=piremeterPoints.get(p);
                    dist=Math.abs(CommonMethods.getDistanceToLine(pt0, pt1, pt));
                    m_dvDentDepths.set(p, dist);
                    p=CommonStatisticsMethods.getCircularIndex(p+1, len);
                }
                p0=p1;
                pt0=pt1;
            }
        }
    }
    public ArrayList<Double> getDentDepths(){
        if(m_dvDentDepths==null) calDentDepths();
        return m_dvDentDepths;
    }
    public int drawPolygon(ImagePlus impl, Color c){
        if(m_cMECPolygon==null) buildMECPolygon();
        m_cMECPolygon.drawPolygon(impl, c);
        return 1;
    }
    public int drawMECRectangle(ImagePlus impl, Color c){
        if(m_cMECPolygon==null) buildMECPolygon();
        m_cMECPolygon.drawMECRectangle(impl, c);
        return 1;
    }
    public ArrayList<Point> getMECPolygonVertices(){
        if(m_cMECPolygon==null) buildMECPolygon();
        return m_cMECPolygon.getVertexPoints();
    }
    public ArrayList<Point> getMECRectangleVertices(){
        if(m_cMECPolygon==null) buildMECPolygon();
        return m_cMECPolygon.getMECRectanglePoints();
    }
}
