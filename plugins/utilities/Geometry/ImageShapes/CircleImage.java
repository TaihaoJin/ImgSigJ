/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry.ImageShapes;
import java.awt.Point;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
import ij.IJ;
import ij.ImagePlus;
import utilities.CommonMethods;
import ij.gui.GenericDialog;
import utilities.ArrayofArrays.IntRangeArray;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class CircleImage extends ImageShape{
    //first point for a line and triable; left top corner for rectangle; center for circle and ellipsoid
//    public Point[] m_pcInnerPointsWithinFrame,m_pcPerimeterPointsWithinFrame
    int radius;
    double dRadius;

    public CircleImage(int radius){
        buildCircle(radius);
    }
    public CircleImage(double radius){
        buildCircle(radius);
    }
    public CircleImage(boolean bDlg){
        buildCircleDlg();
    }
    void buildCircleDlg(){        
        GenericDialog gd=new GenericDialog("input the radius of the circle");
        gd.addNumericField("radius", 10, 0);
        gd.showDialog();
        int r=(int) (gd.getNextNumber()+0.5);
        buildCircle(r);
    }
    void buildCircle(int radius){
        m_nType=ImageShape.Circle_Dbl;
        setDescription("Circle: r="+PrintAssist.ToString(radius));
        this.radius=radius;
        buildCircle(radius+0.5);
    }
    void buildCircle(double radius){
//        setDescription("Circle: r="+PrintAssist.ToString(radius));
        m_nType=ImageShape.CircleInt;
        this.radius=(int)radius;
        int w=(int) (2*radius)+1;
        int h=w;
        m_xRange=new intRange(0,w-1);
        m_yRange=new intRange(0,h-1);
        m_xFrameRange=intRange.getMaxRange();
        m_yFrameRange=intRange.getMaxRange();
        setLocation(new Point(0,0));
        buildXsegments(radius);
        buildYsegments(radius);
        reAdjustCoordinates();
    }
    public CircleImage(Point center, int radius){
        buildCircle(radius);
        setCenter(center);
    }
    public CircleImage(int radius,intRange xFRange, intRange yFRange){
        buildCircle(radius);
        m_xFrameRange=new intRange(xFRange);
        m_yFrameRange=new intRange(yFRange);
    }
    public CircleImage(Point center, int radius,intRange xFRange, intRange yFRange){
        this(center,radius);
        m_xFrameRange=new intRange(xFRange);
        m_yFrameRange=new intRange(yFRange);
    }
    void buildXsegments(double r){
        int y;
        m_xSegments=new ArrayList();
        double r2=r*r;
        int yi=-(int)r,yf=(int)r;
        m_nArea=0;
        intRange ir;
        IntRangeArray ira;
        int dx,dy;
        for(y=yi;y<=yf;y++){
            dx=(int) Math.sqrt(r2-y*y);
            ira=new IntRangeArray();
            ir=new intRange(-dx,dx);
            ira.m_intRangeArray.add(ir);
            m_nArea+=ir.getRange();
            m_xSegments.add(ira);
        }
    }
    void buildYsegments(double r){
        int x;
        m_ySegments=new ArrayList();
        double r2=r*r;
        int xi=-(int)r,xf=(int)r;
        m_nArea=0;
        intRange ir;
        IntRangeArray ira;
        int dx,dy;
        for(x=xi;x<=xf;x++){
            dy=(int) Math.sqrt(r2-x*x);
            ira=new IntRangeArray();
            ir=new intRange(-dy,dy);
            ira.m_intRangeArray.add(ir);
            m_nArea+=ir.getRange();
            m_ySegments.add(ira);
        }
    }
}
