/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;

/**
 *
 * @author Taihao
 */
public class Point2D {
    public int x,y;
    public double dx,dy;
    public Point2D(){
        this.x=0;
        this.y=0;
        this.dx=x;
        this.dy=y;
    }
    public Point2D(int x, int y){
        this.x=x;
        this.y=y;
        this.dx=x;
        this.dy=y;
    }
    public Point2D(double dx, double dy){
        this.dx=dx;
        this.dy=dy;
        this.x=(int)dx;
        this.y=(int)dy;
    }
    public void update(int x, int y){
        this.x=x;
        this.y=y;
        this.dx=x;
        this.dy=y;
    }
    public void update(double dx, double dy){
        this.dx=dx;
        this.dy=dy;
        this.x=(int)dx;
        this.y=(int)dy;
    }
    public boolean equalContents(Point2D p){
        return (p.dx==this.dx&&p.dy==this.dy);
    }
    public boolean different(Point2D p){
        return (!equalContents(p));
    }
}
