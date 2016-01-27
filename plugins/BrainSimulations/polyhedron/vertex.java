/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.polyhedron;

/**
 *
 * @author Taihao
 */
public class vertex {
    public int index;
    public double x,y,z;
    public vertex(){
    }
    public vertex(int i, double x0, double y0, double z0){
        index=i;
        x=x0;
        y=y0;
        z=z0;
    }
    public vertex(vertex v0){
        index=v0.index;
        x=v0.x;
        y=v0.y;
        z=v0.z;
    }
    
    public vertex(CrossPoint v0){
        this(v0.v);
    }
    
    public void setCoordinate(double x0, int index){
        switch (index){
            case 0:
                x=x0;
                break;
            case 1:
                y=x0;
                break;
            case 2:
                z=x0;
                break;
            default:
                break;
        }
    }
    public double getCoordinate(int index){
        double x0=0;
        switch (index){
            case 0:
                x0=x;
                break;
            case 1:
                x0=y;
                break;
            case 2:
                x0=z;
                break;
            default:
                break;
        }
        return x0;
    }
    public boolean overlap(vertex v1){
        double eps=0.0000001;
        double d=0.;
        for(int i=0;i<3;i++){
            d+=(v1.getCoordinate(i)-getCoordinate(i))*(v1.getCoordinate(i)-getCoordinate(i));
        }
        return (Math.sqrt(d)<eps);
    }
    public void setIndex(int i){
        index=i;
    }
}
