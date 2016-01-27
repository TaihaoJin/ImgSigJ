/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.CustomDataTypes;

/**
 *
 * @author Taihao
 */
public class EllipseParNode {
    public double xc,yc,major,minor,angle;//angle is in degree
    public EllipseParNode(double xc, double yc, double major, double minor, double angle){
        this.xc=xc;
        this.yc=yc;
        this.major=major;
        this.minor=minor;
        this.angle=angle;
    }
}
