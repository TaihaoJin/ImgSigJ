/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

/**
 *
 * @author Taihao
 */
public class Angles {
    Angles(){        
    }
    public static double radian(double angle){
        return (angle/180)*Math.PI;
    }
    
    public static double degree(double angle){
        return (angle/Math.PI)*180;
    }
}
