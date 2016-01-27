/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.awt.Rectangle;

/**
 *
 * @author Taihao
 */
public class IntRectangle extends Rectangle{
    int angle; //Considering the tilting of the rectangle. y2=y1+width*sin(angle)
    //The rage of the angle is -45 to 45 degree. Rotation of more than 45 degree
    //is equivalent ot a smaller than 45 degree rotation and the exchange of 
    //of the height and the width.
    public double getArea(){
        return height*width;
    }
}

