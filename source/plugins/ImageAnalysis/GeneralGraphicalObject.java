/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ImageAnalysis.MinimalGraphicalObject;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class GeneralGraphicalObject extends MinimalGraphicalObject{
    public boolean matches(Point p){
        return contains(p);
    }
}
