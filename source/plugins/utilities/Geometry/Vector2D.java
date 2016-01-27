/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;

/**
 *
 * @author Taihao
 */
public class Vector2D {
    public double dx,dy,length;
    public Vector2D(){
        dx=0.;
        dy=0.;
        length=0.f;
    }
    public Vector2D(double x, double y){
        dx=x;
        dy=y;
        length=Math.sqrt(dx*dx+dy*dy);
    }
    public void setComponents(double x, double y){
        dx=x;
        dy=y;
        length=Math.sqrt(dx*dx+dy*dy);
    }
    public double dotProduct(Vector2D v){
        return dx*v.dx+dy*v.dy;
    }
    public double getProjection(Vector2D v){
        return dotProduct(v)/length;
    }
    public void getDirection(int[] dxy){
        //this method computes a the direction of the vector, which is approximated to
        //the the closed ratio of integer dxy[0] and dxy[1]; the resolution is 1:3;
        double ax=Math.abs(dx), ay=Math.abs(dy);
        if(ax>ay){
            if(ay/ax<0.1){                
                dxy[0]=(int)Math.round(Math.copySign(1, dx));
                dxy[1]=0;
            }else{
                dxy[0]=(int)Math.round(Math.copySign(ax/ay, dx));
                dxy[1]=(int)Math.round(Math.copySign(1, dy));
            }
        }else{
            if(ax/ay<0.1){                
                dxy[0]=0;
                dxy[1]=(int)Math.round(Math.copySign(1, dy));
            }else{
                dxy[0]=(int)Math.round(Math.copySign(1, dx));
                dxy[1]=(int)Math.round(Math.copySign(ay/ax, dy));
            }
        }
    }
    public void extend(Vector2D v){
        double ex=getProjection(v); 
        dx+=Math.copySign(ex*dx/length, dx);
        dy+=Math.copySign(ex*dy/length, dy);
        length=Math.sqrt(dx*dx+dy*dy);        
    }
    public void extend(double ex){
        dx+=Math.copySign(ex*dx/length, dx);
        dy+=Math.copySign(ex*dy/length, dy);
        length=Math.sqrt(dx*dx+dy*dy);
    }
    public double getLength(){
        return length;
    }
}
