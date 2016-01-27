/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.awt.Point;
import utilities.Geometry.ImageShapes.ImageShape;
import java.util.ArrayList;
import utilities.ArrayofArrays.PointArray;
import utilities.CustomDataTypes.intRange;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class ImageScanner {
    intRange m_xRange, m_yRange, m_xFRange, m_yFRange;
    Point position, positionI;
    int m_nSteps, m_nTotalSteps;
    int dxI;//initial moving; one and only one of dxI and dyI is none zero, is 1 or -1
    int xi,xf,yi,yf,dx,dy;
    ArrayList<ImageShape> shapes;
    ArrayList<PointArray> m_newPoints, m_oldPoints;
    ArrayList<Point> m_cvPresetPoints;
    int m_nNumShapes;
    boolean m_bPresetPoints;
    public ImageScanner(intRange xRange, intRange yRange, intRange xFRange, intRange yFRange){
        if(!xFRange.enclosed(xRange))IJ.error("scanning range (x) excceeds the frame range!");
        if(!yFRange.enclosed(yRange))IJ.error("scanning range (y) excceeds the frame range!");
        m_xRange=new intRange(xRange);
        m_yRange=new intRange(yRange);
        m_xFRange=new intRange(xFRange);
        m_yFRange=new intRange(yFRange);
        m_nSteps=0;
        xi=xRange.getMin();
        xf=xRange.getMax();
        yi=yRange.getMin();
        yf=yRange.getMax();
        dxI=1;
        position=new Point(xi,yi);
        positionI=new Point(xi,yi);
        m_nTotalSteps=m_xRange.getRange()*m_yRange.getRange()-1;
        shapes=new ArrayList<ImageShape>();
        m_bPresetPoints=false;
    }
/*    public ImageScanner(intRange xRange, intRange yRange, intRange xFRange, intRange yFRange, Point p0){//make the scanner always scan entire region, start from the left top corner
        this(xRange,yRange,xFRange,yFRange);
        position=new Point(p0);
        positionI=new Point(p0);
        this.dxI=dx;
    }*/
    public ImageScanner(intRange xRange, intRange yRange, intRange xFRange, intRange yFRange, ImageShape shape){
        this(xRange,yRange,xFRange,yFRange);
        shapes.add(shape);
        setInitialPoints();
    }
    public ImageScanner(intRange xRange, intRange yRange, intRange xFRange, intRange yFRange, ArrayList<ImageShape> shapes){
        this(xRange,yRange,xFRange,yFRange);
        int len=shapes.size();
        for(int i=0;i<len;i++){
            this.shapes.add(shapes.get(i));
        }
        setInitialPoints();
    }
    void setInitialPoints(){
        m_nNumShapes=shapes.size();
        ImageShape shape;
        m_newPoints=new ArrayList();
        m_oldPoints=new ArrayList();
        PointArray npa=new PointArray(), opa=new PointArray();
        for(int i=0;i<m_nNumShapes;i++){
            shape=shapes.get(i);
            npa=new PointArray();
            opa=new PointArray();
            shape.setCenter(position);
            shape.getInnerPoints(npa.m_pointArray);
            m_newPoints.add(npa);
            m_oldPoints.add(opa);
        }
    }
    void renewPointReplacement(){
        ImageShape shape;
        for(int i=0;i<m_nNumShapes;i++){
            shape=shapes.get(i);
            shape.setCenter(position);
            shape.getInnerPointReplacement(dx, dy, m_newPoints.get(i).m_pointArray, m_oldPoints.get(i).m_pointArray);
        }
    }
    public void move(){
        if(done()){
            IJ.error("the ImageScanner has finished it's scanning!");
        }else{
            if(m_bPresetPoints){
                m_nSteps++;
                position=m_cvPresetPoints.get(m_nSteps);
            }else{
                if(m_xRange.isBorder(position.x,dxI)){
                    dy=1;
                    dx=0;
                    dxI*=-1;
                }else{
                    dy=0;
                    dx=dxI;
                }
                renewPointReplacement();//have to do this before position translation
                position.translate(dx, dy);
                m_nSteps++;
            }
        }
    }
    public void getPoints(PointArray paNew, PointArray paOld){
        getPoints(paNew, paOld,0);
    }
    public void getPoints(PointArray paNew, PointArray paOld, int shapeId){
        paNew.m_pointArray=m_newPoints.get(shapeId).m_pointArray;
        paOld.m_pointArray=m_oldPoints.get(shapeId).m_pointArray;
    }
    public Point getPosition(){
        return position;
    }
    public boolean done(){
        if(m_nSteps>=m_nTotalSteps) return true;
        return false;
    }
    public void presetPoints(ArrayList<Point> points){
        m_bPresetPoints=true;
        m_cvPresetPoints=points;
        m_nTotalSteps=points.size()-1;
        m_nSteps=0;
        position=m_cvPresetPoints.get(0);
        positionI=new Point(position);
    }
    public void reset(){
        m_nSteps=0;
        if(m_bPresetPoints){
            position=m_cvPresetPoints.get(0);
        }else{
            position.setLocation(m_xRange.getMin(),m_yRange.getMin());
        }
        positionI.setLocation(position);
        dxI=1;
        setInitialPoints();
    }
    public int getTotalSteps(){
        return m_nTotalSteps;
    }
}
