/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import utilities.CustomDataTypes.intRange;
import utilities.IntRectangle;
import utilities.ArrayofArrays.*;
import java.util.ArrayList;
import BrainSimulations.DataClasses.BrainSimulationGraphicalObjectProperty;
import utilities.CommonMethods;
import java.awt.Color;
import utilities.Geometry.Point2D;
import utilities.Geometry.ConvexPolygon;
import utilities.Geometry.MinimalEnclosingCovexPolygon;
import ij.ImagePlus;
import utilities.Constants;


/**
 *
 * @author Taihao
 */
public class BrainSimulationGraphicalObject {
    BrainSimulationGraphicalObjectProperty type;
    double rgbHistogram[][];
    public static final int r=0;
    public static final int g=1;
    public static final int x=0;
    public static final int y=2;
    double probType;//The probability of being the assigned GOP type, the over lap of the color histograms.
    int height, width,area;
    int pixel;//represents the color of the object
    intRange xRange, yRange;//The bounding rectangle of the object.
    IntRectangle enclosingRectangle; //minimum area, can be tilted.
    ArrayList <BrainSimulationGraphicalObject> enclosedObjects;//Topological consideration, an object can enclose other objects.
    IntRangeArray xRanges,yRanges;//xRanges is the range of x at each y (y belong to yRange). yRanges is the range of y at each x (x belong to xRange).
    IntRangeArray2 yStripes;//A Graphical Object is defined as a connected area in a image. The object can be discontinuous a long a given line.
                            //y stripe and x stripe is different ways to designate the positions of the pixeles of the object. yStripe store the ranges of x coordinates of the
                            //pixels of the objects at each y position, while xStripe store the ranges of y coordinates of the pixels of the objects at each x position.
    IntRangeArray2 xStripes;
    boolean bShow; //allow the object to be displayed.
    int anchor[];//A specified point (x,y) in the image that covered by the graphical object. Different graphical
    //objects can not have the same anchor point (or any points). The anchor point has the smallest y and x (the most left position of the object at the most top line overlapping the
    //the object.
    intRange rRange,gRange,bRange;//The range of the pixel values.
    ArrayList <Point2D> MECPolygon;//Minimal enclosing ploygon.
    ArrayList <Point2D> MECRectangle;//Minimal enclosing rectangel.
    ArrayList <Point2D> Contour;
    ArrayList <Integer> minContour;
    ArrayList <Double> Curvatures;//the curvature value along the contour.
    double[] rHist, gHist, bHist;
    ArrayList <BrainSimulationGraphicalObject> enclosingObjects;//objects that enclose the object.
    ArrayList <BrainSimulationGraphicalObject> neighboringObjects;//objects that contact with the object.
    BrainSimulationGraphicalObjectsHandler GOHandler;//the graphical object handler that owns this object.
    boolean bEnclosed;//This is an object that is enclosed by other objects.
    int perimeter;//the perimeter of the object in the unit of pixel.
    boolean croppedObject;//the object is contacting with the border of the image.
    double maxCurvature,minCurvature;//the largest and the smallest curvature along the contour.
    public BrainSimulationGraphicalObject(){
        type=null;
        rgbHistogram=null;
        xRange=new intRange();
        yRange=new intRange();
        enclosingRectangle=new IntRectangle();
        enclosedObjects=null;
        xStripes=new IntRangeArray2();
        yStripes=new IntRangeArray2();
        xRanges=new IntRangeArray();
        yRanges=new IntRangeArray();
        anchor=new int[2];
    }
    public boolean equals(BrainSimulationGraphicalObject go){
        return(anchor[0]==go.anchor[0]&&anchor[1]==go.anchor[1]);
    }
    public void setAnchor(int y, int x){
        anchor[0]=y;
        anchor[1]=x;
    }
    public boolean isHiden(){
        if(!bShow) return true;
        return false;
    }
    public void hide(){
        bShow=false;
    }
    public void show(){
        bShow=false;
    }
    public int[] getAnchor(){
        int[] a=new int[2];
        a[0]=anchor[0];
        a[1]=anchor[1];
        return a;
    }
    public BrainSimulationGraphicalObject(BrainSimulationGraphicalObjectProperty type0){
        this();
        type=type0;
        int rgb[]=new int[3];
        rgb[0]=type.red;
        rgb[1]=type.green;
        rgb[2]=type.blue;
        pixel=CommonMethods.rgbTOint(rgb);
        if(type0.showing()) bShow=true;
        else bShow=false;
    }
    boolean addXseg(intRange ir, int y){
        area+=(ir.getMax()-ir.getMin()+1);
        int size=yStripes.m_IntRangeArray2.size();
        if(yRange.emptyRange()) yRange.expandRange(y);
        int stripe=y-yRange.getMin();
        if(stripe<size){
            yStripes.m_IntRangeArray2.get(stripe).m_intRangeArray.add(new intRange(ir));
            xRanges.m_intRangeArray.get(stripe).mergeRanges(new intRange(ir));
            xRange.expandRange(ir);
            return true;
        }else if(stripe==size){
            yStripes.m_IntRangeArray2.add(new IntRangeArray());
            yStripes.m_IntRangeArray2.get(stripe).m_intRangeArray.add(new intRange(ir));
            xRanges.m_intRangeArray.add(new intRange(ir));
            yRange.expandRange(y);
            xRange.expandRange(ir);
            return true;
        }
        return false;
    }
    public void setRandomRGB(){
        int rgb[]=new int[3];
        rgb[0]=(int)(Math.random()*255+0.5);
        rgb[1]=(int)(Math.random()*255+0.5);
        rgb[2]=(int)(Math.random()*255+0.5);
        pixel=CommonMethods.rgbTOint(rgb);
 }
    public BrainSimulationGraphicalObjectProperty getType(){
        return new BrainSimulationGraphicalObjectProperty(type);
    }
    public void mergeGO(BrainSimulationGraphicalObject go){
        intRange newYrange=new intRange(yRange);
        newYrange.mergeRanges(go.yRange);
        intRange newXrange=new intRange(xRange);
        newXrange.mergeRanges(go.xRange);
        IntRangeArray newXranges=new IntRangeArray();
        IntRangeArray2 newYstripes=new IntRangeArray2();
        int stripe,stripe1,stripe2;
        int i,j,size;
        IntRangeArray ira1,ira2;
        for(int y=newYrange.getMin();y<newYrange.getMax();y++){
            stripe=y-newYrange.getMin();
            stripe1=yRange.GetIndex(y);
            stripe2=go.yRange.GetIndex(y);
            IntRangeArray ira=new IntRangeArray();
            intRange ir;
            if(stripe1>=0&&stripe2>=0){
                if(xRanges.m_intRangeArray.get(stripe1).getMin()<go.xRanges.m_intRangeArray.get(stripe2).getMin()){
                    ira1=yStripes.m_IntRangeArray2.get(stripe1);
                    ira2=go.yStripes.m_IntRangeArray2.get(stripe2);
                }else
                {
                    ira2=yStripes.m_IntRangeArray2.get(stripe1);
                    ira1=go.yStripes.m_IntRangeArray2.get(stripe2);
                }
                for(i=0;i<ira1.m_intRangeArray.size();i++){
                    ira.m_intRangeArray.add(ira1.m_intRangeArray.get(i));
                }
                for(i=0;i<ira2.m_intRangeArray.size();i++){
                    ira.m_intRangeArray.add(ira2.m_intRangeArray.get(i));
                }
            }else if(stripe1>=0){
                ira1=yStripes.m_IntRangeArray2.get(stripe1);
                for(i=0;i<ira1.m_intRangeArray.size();i++){
                    ira.m_intRangeArray.add(ira1.m_intRangeArray.get(i));
                }
            }else{
                if(stripe2==28){
                    stripe1=stripe1;
                }
                ira1=go.yStripes.m_IntRangeArray2.get(stripe2);
                for(i=0;i<ira1.m_intRangeArray.size();i++){
                    ira.m_intRangeArray.add(ira1.m_intRangeArray.get(i));
                }
            }
            ir=new intRange(ira.m_intRangeArray.get(0));
            size=ira.m_intRangeArray.size();
            ir.mergeRanges(ira.m_intRangeArray.get(size-1));
            newXranges.m_intRangeArray.add(ir);
            newXrange.mergeRanges(ir);
            newYstripes.m_IntRangeArray2.add(ira);
        }
        xRanges=newXranges;
        xRange=newXrange;
        yStripes=newYstripes;
    }
    public boolean draw(int[] pixels, int h, int w){
        if(!bShow) return false;
        int min=yRange.getMin();
        int max=yRange.getMax();
        int size,stripe,y,x,seg,offset;
        intRange ir;
        IntRangeArray ira;
        int xMin,xMax;
        for(y=min;y<=max;y++){
            if(y%100==0)CommonMethods.showStatusAndProgress("dawing y=" + y, 0);
            stripe=y-min;
            offset=y*w;
            ira=yStripes.m_IntRangeArray2.get(stripe);
            size=ira.m_intRangeArray.size();
            for(seg=0;seg<size;seg++){
                ir=ira.m_intRangeArray.get(seg);
                xMin=ir.getMin();
                xMax=ir.getMax();
                for(x=xMin;x<=xMax;x++){
                    pixels[offset+x]=pixel;
                }
            }
        }
        return true;
    }

    public boolean draw_XStripe(int[] pixels, int h, int w){
        if(!bShow) return false;
        int min=xRange.getMin();
        int max=xRange.getMax();
        int size,stripe,y,x,seg,offset;
        intRange ir;
        IntRangeArray ira;
        int yMin,yMax;
        for(x=min;x<=max;x++){
            stripe=x-min;
            ira=xStripes.m_IntRangeArray2.get(stripe);
            size=ira.m_intRangeArray.size();
            for(seg=0;seg<size;seg++){
                ir=ira.m_intRangeArray.get(seg);
                yMin=ir.getMin();
                yMax=ir.getMax();
                for(y=yMin;y<=yMax;y++){
                    offset=y*w;
                    pixels[offset+x]=pixel;
                }
            }
        }
        return true;
    }

    void renewHist(){
           rHist=new double[256];
           gHist=new double[256];
           bHist=new double[256];
           for(int i=0;i<=255;i++){
               rHist[i]=0.;
               gHist[i]=0.;
               bHist[i]=0.;
          }
    }

    public void clearHists(){
        try{
            for(int i=0;i<=255;i++){
                rHist[i]=0.;
                gHist[i]=0.;
                bHist[i]=0.;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            renewHist();
        }
        catch (NullPointerException e1){
            renewHist();
       }
    }
    public void resetRGBRanges(){
        try{
            rRange.resetRange();
            gRange.resetRange();
            bRange.resetRange();
        }catch(NullPointerException e){
            rRange=new intRange();
            gRange=new intRange();
            bRange=new intRange();
        }
    }
    public void buildHist(int pixels[][]){
        clearHists();
        resetRGBRanges();
        int min=yRange.getMin();
        int max=yRange.getMax();
        int size,stripe,y,x,seg;
        intRange ir;
        IntRangeArray ira;
        int xMin,xMax;
        int[] rgb= new int[3];
        for(y=min;y<=max;y++){
            stripe=y-min;
            ira=yStripes.m_IntRangeArray2.get(stripe);
            size=ira.m_intRangeArray.size();
            for(seg=0;seg<size;seg++){
                ir=ira.m_intRangeArray.get(seg);
                xMin=ir.getMin();
                xMax=ir.getMax();
                for(x=xMin;x<=xMax;x++){
                    pixel=pixels[y][x];
                    CommonMethods.intTOrgb(pixel,rgb);
                    rHist[rgb[0]]+=1.;
                    gHist[rgb[1]]+=1.;
                    bHist[rgb[2]]+=1.;
                    rRange.expandRange(rgb[0]);
                    gRange.expandRange(rgb[1]);
                    bRange.expandRange(rgb[2]);
                }
            }
        }
        for(int i=0;i<=255;i++){
             rHist[i]/=area;
             gHist[i]/=area;
             bHist[i]/=area;
             type.rHist[i]=rHist[i];
             type.gHist[i]=gHist[i];
             type.bHist[i]=bHist[i];
        }
    }

    public void setGOPType(BrainSimulationGraphicalObjectProperty gop){
        type=new BrainSimulationGraphicalObjectProperty(gop);
    }

    public void setProbType(double prob){
        probType=prob;
    }
    public double getProbType(){
        return probType;
    }
    public void getAnchor(int[] anchor){
        anchor[0]=this.anchor[0];
        anchor[1]=this.anchor[1];
    }
    public int  getArea(){
        return area;
    }
    public int getXMidpoint(){
        return xRange.getMidpoint();
    }
    public int getYMidpoint(){
        return yRange.getMidpoint();
    }
    public double overlapRGBProb(BrainSimulationGraphicalObjectProperty gop){
        double prob=1., prob1=0.;
        intRange nRange;

        nRange=this.rRange.overlappedRange(gop.rRange);
        prob1=0.;
        for(int i=nRange.getMin();i<=nRange.getMax();i++){
            prob1+=Math.sqrt(this.rHist[i]*gop.rHist[i]);
        }
        prob*=prob1;

        prob1=0.;
        nRange=this.gRange.overlappedRange(gop.gRange);
        for(int i=nRange.getMin();i<=nRange.getMax();i++){
            prob1+=Math.sqrt(this.gHist[i]*gop.gHist[i]);
        }
        prob*=prob1;
        prob1=0.;
        nRange=this.bRange.overlappedRange(gop.bRange);
        for(int i=nRange.getMin();i<=nRange.getMax();i++){
            prob1+=Math.sqrt(this.bHist[i]*gop.bHist[i]);
        }
        prob*=prob1;
        return prob;
    }
    public intRange getRRange(){
        return new intRange(rRange);
    }
    public intRange getGRange(){
        return new intRange(gRange);
    }
    public intRange getBRange(){
        return new intRange(bRange);
    }
    public void buildXStripes(){
        int xMin=xRange.getMin(),xMax=xRange.getMax();
        int yMin=yRange.getMin(),yMax=yRange.getMax();
        int x,y,stripe;
        boolean p=false,c=false;
        intRange seg=null;
        IntRangeArray ira=null;
        for(x=xMin;x<=xMax;x++){
            ira=new IntRangeArray();
            stripe=x-xMin;
            p=false;
            intRange yr=new intRange();
            for(y=yMin;y<=yMax+1;y++){
                c=contains(x,y);
                if(!p&&c){//first point of a segment
                    seg=new intRange(y,y);
                }
                if(p&&!c){//next point of the last point of a segment
                    seg.expandRange(y-1);
                    ira.m_intRangeArray.add(seg);
                    yr.expandRange(seg);
                }
                p=c;
            }
            xStripes.m_IntRangeArray2.add(ira);
            yRanges.m_intRangeArray.add(yr);
        }
    }
    public boolean contains(int x, int y){
        int stripe=yRange.GetIndex(y);
        if(stripe<0) return false;
        if(!xRanges.m_intRangeArray.get(stripe).contains(x)) return false;
        IntRangeArray ira=yStripes.m_IntRangeArray2.get(stripe);
        int size=ira.m_intRangeArray.size();
        int i,j;
        intRange ir;
        for(i=0;i<size;i++){
            ir=ira.m_intRangeArray.get(i);
            if(ir.getMin()>x) return false;
            if(ir.getMax()>=x) return true;
        }
        return false;
    }

    public intRange getXseg(int x, int y){//returns the segment containing (x,y) point
        int stripe=yRange.GetIndex(y);
        if(stripe<0) return null;
        if(!xRanges.m_intRangeArray.get(stripe).contains(x)) return null;
        IntRangeArray ira=yStripes.m_IntRangeArray2.get(stripe);
        int size=ira.m_intRangeArray.size();
        int i,j;
        intRange ir=null;
        for(i=0;i<size;i++){
            ir=ira.m_intRangeArray.get(i);
            if(ir.getMin()>x) return null;
            if(ir.getMax()>=x) return ir;
        }
        return null;
    }

    public intRange getYseg(int x, int y){//returns the segment containing (x,y) point
        int stripe=xRange.GetIndex(x);
        if(stripe<0) return null;
        if(!yRanges.m_intRangeArray.get(stripe).contains(y)) return null;
        IntRangeArray ira=xStripes.m_IntRangeArray2.get(stripe);
        int size=ira.m_intRangeArray.size();
        int i,j;
        intRange ir=null;
        for(i=0;i<size;i++){
            ir=ira.m_intRangeArray.get(i);
            if(ir.getMin()>y) return null;
            if(ir.getMax()>=y) return ir;
        }
        return null;
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
    public void drawMECRactangle(ImagePlus impl){
        Point2D p1,p2;
        impl.getProcessor().setColor(CommonMethods.randomColor());
        ConvexPolygon cpg=new ConvexPolygon(MECPolygon);
        ArrayList <Point2D> MAERectangle=cpg.getMAERectangle();
        for(int j=0;j<4;j++){
            p1=MAERectangle.get(j);
            p2=MAERectangle.get((j+1)%4);
            impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
        }
    }
    public void drawContour(ImagePlus impl, boolean displayCurvature){
        Point2D p1,p2;
        int size=Contour.size();
        impl.getProcessor().setLineWidth(4);
        for(int j=0;j<size;j++){
            p1=Contour.get(j);
            p2=Contour.get((j+1)%size);
            impl.getProcessor().setColor(Color.YELLOW);
            impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
        }

            int size1=Curvatures.size();
        if(displayCurvature&&size1>0){

            maxCurvature=Double.NEGATIVE_INFINITY;
            minCurvature=Constants.largestFloat;
            double c=0;
            for(int j=0;j<size1;j++){
                c=Curvatures.get(j);
                if(c<minCurvature){
                    minCurvature=c;
                }
                if(c>maxCurvature){
                    maxCurvature=c;
                }
            }
            if(minCurvature>0) minCurvature=-1;
            if(maxCurvature<0) maxCurvature=1;



            int i0,i,i1,di;
                impl.getProcessor().setColor(Color.BLACK);
            for(int j=0;j<size;j++){
                c=Curvatures.get(j);
                p1=Contour.get(j);
                p2=p1;
                impl.getProcessor().setColor(CommonMethods.RBSpectrumColor(c,-1,0,1));
                impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
            }
            /*
            int i0,i,i1,di;
                impl.getProcessor().setColor(Color.BLACK);
            for(int j=0;j<size1;j++){
                i=minContour.get(j);
                di=Math.min(3, circularDist(Contour.size(),i,minContour.get((j+1)%size1))/2);
                p1=Contour.get(i);
                p2=Contour.get((i+di)%size);
//                p2=p1;
                c=Curvatures.get(j);
                impl.getProcessor().setColor(CommonMethods.RBSpectrumColor(c,minCurvature,0,maxCurvature));
                impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);

                di=Math.min(3, circularDist(Contour.size(),minContour.get((j+1)%size1),i)/2);
                p1=Contour.get((i+size-di)%size);
//                p1=p2;
                p2=Contour.get(i);
                c=Curvatures.get(j);
                impl.getProcessor().setColor(CommonMethods.RBSpectrumColor(c,minCurvature,0,maxCurvature));
                impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
            }*/
        }
    }
    public int circularDist(int size, int I0, int I1){
        int dist=0;
        if(I1>I0) dist=I1-I0;
        if(I0>I1) dist=I0+size-I1;
        return dist;
    }
    public void drawMECPolygon(ImagePlus impl){
        Point2D p1,p2;
        int size=MECPolygon.size();
        impl.getProcessor().setColor(CommonMethods.randomColor());
        for(int j=0;j<size;j++){
            p1=MECPolygon.get(j);
            p2=MECPolygon.get((j+1)%size);
            impl.getProcessor().drawLine(p1.x,p1.y,p2.x,p2.y);
        }
    }
    public Color getColor(){
        return new Color(pixel);
    }
    public int getBorders(Point2D[][] borders,int x0, int y0, int i0, int j0, intRange xRange, intRange yRange){
        /*returns 8 points neighboring points located outside of the graphical object.
         The order of the five points are:
         00 for the point left up (smaller x,y) to the reference point.
         01 for the point up (same x, smaller y) to the reference point.
         02 for the point right up (larger x, smaller y) to the reference point.
         10 for the point left (smaller x, same y) to the reference point.
         12 for the point right (larger x, same y) to the reference point.
         20 for the point left lower (smaller x, larger y) to the reference point.
         21 for the point lower (same x, larger y) to the reference point.
         22 for the point right lower (larger x, larger y) to the reference point.
         11 for the closest point of all 8 points.
         The methods will find a point according to index value.*/
        intRange xr=this.getXseg(x0, y0), yr=this.getYseg(x0, y0);
        if(xr==null||yr==null){
            for(int i=0;i<3;i++){
                for(int j=0;j<0;j++){
                    borders[i][j]=null;
                }
            }
            return -1;
        }
        int x,y,x1,minDist,dist,yMin,yMax;
        Point2D p;
        int id=10*i0+j0;
        switch (id){
            case 10://i0=1, j0=0;
                x=xr.getMin()-1;
                if(x<xRange.getMin())
                    p=null;
                else
                    p=new Point2D(x,y0);
                borders[1][0]=p;
                break;
            case 12://i0=1, j0=2;
                x=xr.getMax()+1;
                if(x>xRange.getMax())
                    p=null;
                else
                    p=new Point2D(x,y0);
                borders[1][2]=p;
                break;
            case 1://i0=0, j0=1;
                y=yr.getMin()-1;
                if(y<yRange.getMin())
                    p=null;
                else
                    p=new Point2D(x0,y);
                borders[0][1]=p;
                break;
            case 21://i0=2,j0=1;
                y=yr.getMax()+1;
                if(y>yRange.getMax())
                    p=null;
                else
                    p=new Point2D(x0,y);
                borders[2][1]=p;
                break;
            default:
                minDist=Constants.largestInteger;
                x=xr.getMin()-1;
                if(x<xRange.getMin()){
                    p=null;}
                else{
                    p=new Point2D(x,y0);
                    minDist=x0-x;
                    borders[1][0]=p;
                    id=10;
                }
                x=xr.getMax()+1;
                if(x>xRange.getMax())
                    p=null;
                else{
                    p=new Point2D(x,y0);
                    borders[1][2]=p;
                    dist=x-x0;
                    if(dist<minDist){
                        minDist=dist;
                        id=12;
                    }
                }
                y=yr.getMin()-1;
                if(y<yRange.getMin())
                    p=null;
                else{
                    p=new Point2D(x0,y);
                    borders[0][1]=p;
                    dist=y0-y;
                    if(dist<minDist){
                        minDist=dist;
                        id=1;
                    }
                }
                y=yr.getMax()+1;
                if(y>yRange.getMax())
                    p=null;
                else{
                    p=new Point2D(x0,y);
                    borders[2][1]=p;
                    dist=y-y0;
                    if(dist<minDist){
                        minDist=dist;
                        id=20;
                    }
                }

                intRange ir;
                yMin=yr.getMin();
                yMax=yr.getMax();
                int minDist0=Constants.largestInteger;
                int minDist1=Constants.largestInteger;
                for(y=yMin;y<y0;y++){
                    ir=getXseg(x0,y);
                    x1=ir.getMin()-1;
                    if(x1>=xRange.getMin()){
                        dist=x0-x1+y0-y;
                        if(dist<minDist0){
                            borders[0][0]=new Point2D(x1,y);
                            minDist0=dist;
                            if(minDist0<minDist){
                                minDist=minDist0;
                                id=0;
                            }
                        }
                    }
                    x1=ir.getMax()+1;
                    if(x1<=xRange.getMax()){
                        dist=x1-x0+y0-y;
                        if(dist<minDist1){
                            borders[0][2]=new Point2D(x1,y);
                            minDist1=dist;
                            if(minDist0<minDist){
                                minDist=minDist0;
                                id=2;
                            }
                        }
                    }
                }

                minDist0=Constants.largestInteger;
                minDist1=Constants.largestInteger;
                for(y=y0+1;y<=yMax;y++){
                    ir=getXseg(x0,y);
                    x1=ir.getMin()-1;
                    if(x1>=xRange.getMin()){
                        dist=y-y0+x0-x;
                        if(dist<minDist0){
                            id=20;
                            borders[2][0]=new Point2D(x1,y);
                            minDist0=dist;
                            if(minDist0<minDist){
                                minDist=minDist0;
                                id=20;
                            }
                        }
                    }
                    x1=ir.getMax()+1;
                    if(x1<=xRange.getMax()){
                        dist=x1-x0+y-y0;
                        if(dist<minDist1){
                            id=22;
                            borders[2][2]=new Point2D(x1,y);
                            minDist1=dist;
                            if(minDist0<minDist){
                                minDist=minDist0;
                                id=22;
                            }
                        }
                    }
                }
                break;
        }
        i0=id/10;
        j0=id%10;
        borders[1][1]=borders[i0][j0];
        return 1;
    }
    public void buildEnclosedObjectList(int w0, int h0){
        Contour=new ArrayList <Point2D> ();
        minContour=new ArrayList <Integer> ();
        int moveType; //moveType 0 for backMove and 1 for turnRight
        if(enclosingObjects!=null) enclosingObjects.clear();
        else
            enclosingObjects=new ArrayList <BrainSimulationGraphicalObject> ();
        Point2D borders[][]=new Point2D[3][3];
        int i,j,x,y,x0,y0,xd,yd,xd0,yd0,size,size1,seg;
        int yMin=yRange.getMin(),yMax=yRange.getMax(),xMin=xRange.getMin(),xMax=xRange.getMax();
        int h=yMax-yMin+3, w=xMax-xMin+3;
        byte pixels[][]=new byte[h][w];
        for(y=0;y<h;y++){
            for(x=0;x<w;x++){
                pixels[y][x]=0;
            }
        }
        String title="object";
        size=yStripes.m_IntRangeArray2.size();
        IntRangeArray ira;
        intRange ir;
        BrainSimulationGraphicalObject go;
        for(int index=0;index<size;index++){
            ira=yStripes.m_IntRangeArray2.get(index);
            size1=ira.m_intRangeArray.size();
            for(seg=0;seg<size1;seg++){
                ir=ira.m_intRangeArray.get(seg);
                for(x=ir.getMin();x<=ir.getMax();x++){
                    pixels[index+1][x-xMin+1]=(byte)255;
                }
            }
        }
        ira=yStripes.m_IntRangeArray2.get(0);
        ir=ira.m_intRangeArray.get(0);
//        CommonMethods.displayAsImage(title,w, h, pixels);
        perimeter=0;
        int xo=ir.getMin()-1,yo=yMin-1;
        x=xo;
        y=yo;
        int move[]=new int[2];
        move[0]=0;
        move[1]=1;
        moveType=1;
        intRange xImageRange=new intRange(0,w0-1);
        intRange yImageRange=new intRange(0,h0-1);
        int moveSum=0;
        do{
            y+=move[0];
            x+=move[1];
            if((perimeter%100)==0){
                y=y;
            }
            if(pixels[y-yMin+1][x-xMin+1]==0){
//                pixels[y-yMin+1][x-xMin+1]=(byte)150;
                turnRight(move);

                go=GOHandler.getGOAt(x, y);
                if(go==null){
                    continue;
                }
                if(moveType==1) {
                    Contour.add(new Point2D(x,y));
                }
                if(moveSum==1){
                    minContour.add(Contour.size()-2);
                    moveSum=0;
                }
                moveType=1;
                moveSum+=moveType;
                if(GOHandler.performedEdgeFinding()&&go.type.StructureId==-2){
                    if(go.getBorders(borders, x, y, -1, -1, xImageRange, yImageRange)==-1) continue;
                    for(i=0;i<3;i++){
                        for(j=0;j<3;j++){
                            if(i!=1||j!=1){
                                if(borders[i][j]==null) continue;
                                x0=borders[i][j].x;
                                y0=borders[i][j].y;
                                if(x0==2048&&y0==772){
                                    i=i;
                                }
                                go=GOHandler.getGOAt(x0, y0);
                                if(go==null){
                                    continue;
                                }
                                if(!go.equals(this)){
                                    if(!enclosingObjects.contains(go)){
                                        enclosingObjects.add(go);
                                    }
                                }
                            }
                        }
                    }
                }
                else{
                    if(!go.equals(this)){
                        if(!enclosingObjects.contains(go)){
                            enclosingObjects.add(go);
                        }
                    }
                }
            }
            else{
                moveBack(move);
                moveType=-2;
                moveSum=-1;
                perimeter++;
            }
        }while(x!=xo||y!=yo);
        minContour.add(Contour.size()-1);

        size=enclosingObjects.size();
//        CommonMethods.displayAsImage(title,w, h, pixels);
        if(!this.isCropped(w0, h0)){
            if(size==1){
                go=enclosingObjects.get(0);
                if(go.enclosedObjects==null) go.enclosedObjects=new ArrayList<BrainSimulationGraphicalObject>();
                go.enclosedObjects.add(this);
                bEnclosed=true;
            }
        }
//        if(Contour.size()>4) minContour=CommonMethods.getMinimumVertexPolygon(Contour);
    }

    void moveBack(int[] move){
        move[0]*=-1;
        move[1]*=-1;
    }
    void turnRight(int[] move){
        int index=move[0]*10+move[1];
        switch(index){
            case 1:
                move[0]=1;
                move[1]=0;
                break;
            case 10:
                move[0]=0;
                move[1]=-1;
                break;
            case -1:
                move[0]=-1;
                move[1]=0;
                break;
            case -10:
                move[0]=0;
                move[1]=1;
                break;
        }
    }
    protected intRange getLowerIR(IntRangeArray ira, int x){
    //ira is an array of IntRange stored in ascending order. The methods returns
    //the largest IntRange objects whose nMax is smaller than x.
        int size=ira.m_intRangeArray.size();
        intRange ir=null, ir0=null;
        for(int i=0;i<size;i++){
            ir0=ira.m_intRangeArray.get(i);
            if(ir0.getMax()<x)
                ir=ir0;
            else
                break;
        }
        return ir;
    }
    protected intRange getHigherIR(IntRangeArray ira, int x){
    //ira is an array of IntRange stored in ascending order. The methods returns
    //the smallest IntRange objects whose nMin is larger than x.
        int size=ira.m_intRangeArray.size();
        intRange ir=null, ir0=null;
        for(int i=size-1;i>=0;i--){
            ir0=ira.m_intRangeArray.get(i);
            if(ir0.getMin()>x)
                ir=ir0;
            else
                break;
        }
        return ir;
    }
    public boolean isCropped(int w, int h){
        this.croppedObject=true;
        if(this.xRange.getMin()<=0) return this.croppedObject;
        if(this.xRange.getMin()>=w) return this.croppedObject;
        if(this.yRange.getMin()<=0) return this.croppedObject;
        if(this.yRange.getMin()>=h) return this.croppedObject;
        this.croppedObject=false;
        return false;
    }
    public void setPixel(Color color){
        pixel=(color.getRed()<<16)|(color.getGreen()<<8)|color.getBlue();
    }
    public boolean draw(ImagePlus impl, Color c){
        impl.getProcessor().setColor(c);
 //      if(!bShow) return false;
        int min=yRange.getMin();
        int max=yRange.getMax();
        int size,stripe,y,x,seg,offset;
        intRange ir;
        IntRangeArray ira;
        int xMin,xMax;
        for(y=min;y<=max;y++){
            stripe=y-min;
            ira=yStripes.m_IntRangeArray2.get(stripe);
            size=ira.m_intRangeArray.size();
            for(seg=0;seg<size;seg++){
                ir=ira.m_intRangeArray.get(seg);
                xMin=ir.getMin();
                xMax=ir.getMax();
                impl.getProcessor().drawLine(xMin,y,xMax,y);
            }
        }
        return true;
    }
    public int drawEnclosedObjects(ImagePlus impl, ArrayList <Color> colors){
        if(enclosedObjects==null)return -1;
        int size=enclosedObjects.size();
        int size1=colors.size();
        BrainSimulationGraphicalObject go;
        for(int i=0;i<size;i++){
            go=enclosedObjects.get(i);
            go.show();
            Color c=colors.get(i%size1);
            go.draw(impl,c);
        }
        return 1;
    }
    public ArrayList <Double> calCurvature(ArrayList <Integer> contour0){
        ArrayList <Double> c=new ArrayList<Double>();
        int i,i0,i1,size=contour0.size();
        if(size<5) return c;
        for(i=0;i<size;i++){
            i0=(i+size-1)%size;
            i1=((i+1)%size);
            c.add(CommonMethods.getAngle(Contour.get(contour0.get(i0)), Contour.get(contour0.get(i)), Contour.get(contour0.get(i1))));
        }
        /*
        double angle=0.;
        for(i=0;i<5;i++){
            i0=(i+size-2)%size;
            angle+=c.get(i0);
        }
        ArrayList <Double> c1=new ArrayList<Double>();
        c1.add(angle/5.);
        for(i=1;i<size;i++){
            angle+=c.get((i+2+size)%size);
            angle-=c.get((i+size-3)%size);
            c1.add(angle/5);
        }*/
        return c;

    }
    public void calCurvature0(){
        Curvatures=calCurvature(minContour);
    }
    public int calCurvature(){
        Curvatures=new ArrayList <Double> ();
        ArrayList <Double> c=new ArrayList<Double>();
        int i,i0,i1,size=Contour.size();
        if(size<33) return -1;
        double h;
        Point2D p1,p2,p3;
        double hMax=-1;
        for(i=0;i<size;i++){
            i0=(i+size-15)%size;
            i1=((i+15)%size);
            p1=Contour.get(i0);
            p2=Contour.get(i1);
            p3=Contour.get(i);
            h=CommonMethods.getDistanceToLine(p1, p2, p3);
            c.add(h);
            if(Math.abs(h)>hMax) hMax=Math.abs(h);
        }
        for(i=0;i<size;i++){
            h=c.get(i);
            Curvatures.add(h/hMax);
        }
        return 1;
    }
}
