/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;


/**
 *
 * @author Taihao
 */
public class Ring_Double {
    Circle_Double inner, outer;
    double cx, cy;
    public Ring_Double(double cx, double cy,double ri, double ro){
        this.cx=cx;
        this.cy=cy;
        inner=new Circle_Double(cx,cy,ri);
        outer=new Circle_Double(cx,cy,ro);
    }
    public double areaInSquare(double x, double y, double dLen){
        return outer.areaWithinSquare(x, y, dLen)-inner.areaWithinSquare(x, y, dLen);
    }
}
