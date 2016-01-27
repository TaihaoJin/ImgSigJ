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
public class RectangleImage extends ImageShape{
    int width;
    int height;
    public RectangleImage(int width, int height){
        buildRectangleImage(width, height);
    }
    void buildRectangleImage(int w, int h){
        m_nType=ImageShape.RingInt;
        this.width=w;
        this.height=h;
        int i,j;
        ArrayList<IntRangeArray> xSegments=new ArrayList();
        ArrayList<IntRangeArray> ySegments=new ArrayList();
        for(i=0;i<h;i++){
            IntRangeArray ira=new IntRangeArray();
            ira.m_intRangeArray.add(new intRange(0,width-1));
            xSegments.add(ira);
        }
        for(j=0;j<w;j++){
            IntRangeArray ira=new IntRangeArray();
            ira.m_intRangeArray.add(new intRange(0,height-1));
            ySegments.add(ira);
        }
        super.buildImageShape(xSegments, ySegments);
    }
}
