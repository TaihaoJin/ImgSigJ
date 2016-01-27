/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry.ImageShapes;
import utilities.Geometry.ImageShapes.CircleImage;
import java.awt.Point;
import utilities.CustomDataTypes.intRange;
import utilities.ArrayofArrays.IntRangeArray;
import ij.gui.GenericDialog;
import utilities.io.PrintAssist;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class Ring extends ImageShape{
    int radiusI;
    int radiusO;
    public Ring(boolean bDlg){
        GenericDialog gd=new GenericDialog("input inner and outer radius of the ring");
        gd.addNumericField("inner radius", 10, 0);
        gd.addNumericField("outer radius", 25, 0);
        gd.showDialog();
        int rI=(int) (gd.getNextNumber()+0.5);
        int rO=(int) (gd.getNextNumber()+0.5);
        buildRing(rI,rO);
    }
    public Ring(int radiusI, int radiusO){
        setDescription("Ring: rI="+PrintAssist.ToString(radiusI)+" rO="+PrintAssist.ToString(radiusO));
        buildRing(radiusI, radiusO);
    }
    public Ring(double radiusI, double radiusO){//defined as CircleImage(raiusO) excluding CirgleImage(radiusI-1)//modified on 6/7/2010
//        setDescription("Ring: rI="+PrintAssist.ToString(radiusI)+" rO="+PrintAssist.ToString(radiusO));
        buildRing(radiusI, radiusO);
    }
    public Ring(int radiusI, int radiusO, intRange xFRange, intRange yFRange){
        this(radiusI,radiusO);
        m_xFrameRange.reset(xFRange);
        m_yFrameRange.reset(yFRange);
    }
    void buildRing(int radiusI, int radiusO){
        m_nType=ImageShape.RingInt;
        this.radiusI=radiusI;
        this.radiusO=radiusO;
        CircleImage outer=new CircleImage(radiusO);
        super.buildImageShape(outer);
        location=new Point(0,0);
        ImageShape inner=new ImageShape(new CircleImage(radiusI-1));
        inner.setCenter(new Point(radiusO,radiusO));
        excludeShape(inner);
    }
    void buildRing(double radiusI, double radiusO){
        m_nType=ImageShape.Ring_Dbl;
        this.radiusI=(int)radiusI;
        this.radiusO=(int)radiusO;
        CircleImage outer=new CircleImage(radiusO);
        outer.setCenter(new Point(0,0));
        super.buildImageShape(outer);
        Point pt=ImageShapeHandler.closestInsidePointEX(radiusI);
        if(pt!=null){
            double rI=Math.sqrt((pt.x*pt.x+pt.y*pt.y));
            ImageShape inner=new ImageShape(new CircleImage(rI));
            inner.setCenter(new Point(getCenter()));
            excludeShape(inner);
        }
    }
}
