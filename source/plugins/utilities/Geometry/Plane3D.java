/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;

/**
 *
 * @author Taihao
 */
public class Plane3D {
    //ax+bx+cz+d=0;
    double a, b, c, d=1;
    Plane mPlane;
    public Plane3D(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3){
        double dist12=dist(x1,y1,z1,x2,y2,z2),dist13=dist(x1,y1,z1,x3,y3,z3),dist23=dist(x2,y2,z2,x3,y3,z3);
        if(dist12==0||dist13==0||dist23==0){
            dist12=dist12;
        }
        double[][] v={{x1,y1,z1},{x2,y2,z2},{x3,y3,z3}},va={{1,y1,z1},{1,y2,z2},{1,y3,z3}},vb={{x1,1,z1},{x2,1,z2},{x3,1,z3}},vc={{x1,y1,1},{x2,y2,1},{x3,y3,1}};
        double D=calDeterminent3x3(v);
        D=D;
        a=-1.*calDeterminent3x3(va)/D;        
        b=-1.*calDeterminent3x3(vb)/D;        
        c=-1.*calDeterminent3x3(vc)/D;    
        mPlane=new Plane(new Vector3D(x1,y1,z1),new Vector3D(x2,y2,z2),new Vector3D(x3,y3,z3));
    }
    public static double calDeterminent3x3(double[][] v){
        double D=v[0][0]*v[1][1]*v[2][2]+v[1][0]*v[2][1]*v[0][2]+v[2][0]*v[0][1]*v[1][2]-v[2][0]*v[1][1]*v[0][2]-v[1][0]*v[0][1]*v[2][2]-v[0][0]*v[2][1]*v[1][2];
        return D;
    }
    public Point3D getProjection(double x, double y, double z){
        Vector3D V0=new Vector3D(x,y,z),Vt=mPlane.getNormal(),V1=new Vector3D(x+Vt.getX(),y+Vt.getY(),z+Vt.getZ());
        Line L=new Line(V0,V1);
        Vector3D V=mPlane.intersection(L);
        return new Point3D(V.getX(),V.getY(),V.getZ());
    }
    public double getZ(double x, double y){
        double z=-d-a*x-b*y;
        Line L=new Line(new Vector3D(x,y,0),new Vector3D(x,y,10));
        Vector3D V=mPlane.intersection(L);
        if(V==null)
            return z;
        z=V.getZ();
        return z;
    }
    public boolean IsVertical(){
        return a==0&&b==0;
    }
    static public double dist(double x1,double y1, double z1,double x2,double y2,double z2){
        double dx=x1-x2,dy=y1-y2,dz=z1-z2;
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }
    public Vector3D intersection(Line L){
        return mPlane.intersection(L);
    }
}
