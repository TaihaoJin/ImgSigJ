/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.awt.Point;
import utilities.Geometry.ImageShapes.ImageShape;

/**
 *
 * @author Taihao
 */
public abstract class ImageAnnotationNode {
    public ImageShape cIS;
    public String type;
    private static int SN;
    public String title;
    public Object note;
    private int m_nSN;
    String name;
    public int w,h;
    public Point displayLocation;
    public double[] pixelSize, DisplayOffset;//x and y
    public abstract String getAnnotationAsString(int x, int y);
    public ImageAnnotationNode(){
        m_nSN=SN;
        SN++;
        pixelSize=new double[2];
        DisplayOffset=new double[2];
    }
    public void updateLocation(){
            cIS.setLocation(displayLocation);
        }
    public void setDisplayLocation(Point pt){
        displayLocation=new Point(pt);
    }
    public int getSN(){
        return m_nSN;
    }
    public void setNote(Object note){
        this.note=note;
    }
    public abstract boolean annotated(int x, int y);
    public boolean annotated(int x, int y, String type){
        if(!this.type.equalsIgnoreCase(type)&&!type.contentEquals("*")) return false;
        return (annotated(x,y));
    }
    public void setShape(ImageShape cIS){
        this.cIS=cIS;
        w=cIS.getXrange().getRange();
        h=cIS.getYrange().getRange();
    }
    public ImageShape getImageShape(){
        return cIS;
    }
    public int getShapeWidth(){
        return w;
    }
    public int getShapeHeight(){
        return h;
    }
    public void setPixelSize(double x, double y){
        pixelSize[0]=x;
        pixelSize[1]=y;
    }
    public void setDisplayOffset(double x, double y){
        DisplayOffset[0]=x;
        DisplayOffset[1]=y;
    }
}
