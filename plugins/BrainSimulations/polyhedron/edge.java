/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.polyhedron;
import utilities.Constants;

/**
 *
 * @author Taihao
 */
public class edge {
    public vertex v1, v2;//when a edge belongs to a polygon, the polygon is on 
                         //the left hand side when one walking from v1 to v2.
    public int index;
    public edge(){}
    public edge (int i, vertex v10, vertex v20){
        index=i;
        v1=new vertex(v10);
        v2=new vertex(v20);
    }
    public edge(edge eg1){
        index=eg1.index;
        v1=new vertex(eg1.v1);
        v2=new vertex(eg1.v2);
    }
    boolean theSame(edge e1){
        return (e1.v1.index==v1.index&&e1.v2.index==v2.index);
    }
    boolean nextEdge(edge eg){
        return v2.overlap(eg.v1);
    }
    public CrossPoint crossPoint(double[] x0,int[] oi){
        //this methods return the point and direction where the edge crosses a 
        //plane defined by the point x0 and the axis oi[2].
        CrossPoint cp=null;
        int o3=oi[2];
        double y=x0[o3];
        double y1=v1.getCoordinate(o3);
        double y2=v2.getCoordinate(o3);
        if((y1-y)*(y2-y)>0) return cp;
        cp=new CrossPoint();
        if(y2>y1) cp.index=1;
        else index =0;
        cp.setCoordinate(y, o3);
        double x,x1,x2,dx,dy;
        for(int i=0;i<2;i++){
            dy=y2-y1;
            x1=v1.getCoordinate(oi[i]);
            x2=v2.getCoordinate(oi[i]);
            dx=x2-x1;
            if(Math.abs(dx)<Constants.epsF) { 
                x=0.5*(x1+x2);
                cp.setCoordinate(x,oi[i]);
                continue;
            }
            x=x1+dx*(y-y1)/dy;
            cp.setCoordinate(x,oi[i]);
        }
        return cp;
    } 
}
