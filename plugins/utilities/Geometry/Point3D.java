/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;
import java.awt.Point;

/**
 *
 * @author Taihao
 */
public class Point3D {
    public double x,y,z;
    public Point3D(){
        x=0;
        y=0;
        z=0;
    }
    public Point3D(double x, double y, double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public Point3D(Point p){
        x=p.x;
        y=p.y;
        z=0;
    }
    public void setLocation(Point p){
        x=p.x;
        y=p.y;
        z=0;
    }
    public void setLocation(double x, double y, double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public void setLocation(Point3D p){
        this.x=p.x;
        this.y=p.y;
        this.z=p.z;
    }
    public Point getXYPoint(){
        return new Point((int)(x+0.5),(int)(y+0.5));
    }
    public void setZ(int z){
        this.z=z;
    }
}
