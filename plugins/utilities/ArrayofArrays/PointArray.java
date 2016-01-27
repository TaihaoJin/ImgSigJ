/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import java.util.ArrayList;
import java.awt.Point;

/**
 *
 * @author Taihao
 */
public class PointArray {
    public ArrayList<Point> m_pointArray;
    public PointArray(){
        m_pointArray=new ArrayList<Point>();
    }
    public PointArray(ArrayList<Point> pa){
        this();
        int size=pa.size();
        for(int i=0;i<size;i++){
            m_pointArray.add(new Point(pa.get(i)));
        }
    }
}
