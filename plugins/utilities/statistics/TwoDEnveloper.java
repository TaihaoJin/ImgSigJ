/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.Geometry.ImageShapes.*;
import utilities.CustomDataTypes.intRange;
import java.awt.Point;
import utilities.CustomDataTypes.DoubleRange;
import java.util.ArrayList;
import utilities.Geometry.Plane3D;
import utilities.Geometry.Point3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utilities.CommonGeometryMethods;
import ij.IJ;
import utilities.CommonMethods;


/**
 *
 * @author Taihao
 */
public class TwoDEnveloper {
    int[][] pnData;
    int[] pixelHist;
    double[][] pdEnvU,pdEnvL; 
    ImageShape cIS;
    public TwoDEnveloper(){        
    }
    public void buildEnvelopes(int[][] pnData, ImageShape cIS, double[][] pdEnvU, double[][] pdEnvL){
        this.pnData=pnData;
        this.pdEnvL=pdEnvL;
        this.pdEnvU=pdEnvU;
        this.cIS=cIS;
        int x,y,h=pnData.length,w=pnData[0].length;
        intRange xRange=cIS.getXrange(),yRange=cIS.getYrange();
        Point p=cIS.getCenter();
        int iI=p.y-yRange.getMin(),iF=h-1-yRange.getMax()+p.y,jI=p.x-xRange.getMin(),jF=w-1-xRange.getMax()+p.y;
        for(y=iI;y<=iF;y++){
            IJ.showStatus("y= "+y);
            for(x=jI;x<=jF;x++){
                if(y==172){
                    IJ.showStatus("x= "+x+",y= "+y);
                }
                findEnvPoints(x,y);
            }
        }
    }    
    public void findEnvPoints(int x0, int y0){
        ArrayList<Point> points=new ArrayList();
        cIS.setCenter(x0,y0);
        cIS.getInnerPoints(points);
        findEnvPoints(x0,y0,points,1);
        findEnvPoints(x0,y0,points,-1);
    }
    public void findEnvPoints(int x0, int y0, ArrayList<Point> points, int sign){
        double z=Double.NEGATIVE_INFINITY;
        double[][] pdEnv=pdEnvU;
        if(sign<0) pdEnv=pdEnvL;
        
        Point px1=new Point(),px2=new Point(),px3=new Point(),p;
        double z0=pnData[y0][x0],dist01,dist12,z1,z2;
        //finding the first point for upper and lower envelope
        
        if(x0==926&&y0==172){
            x0=x0;
        }
        
        px1=findFirstEnvPoint(x0,y0,points,sign);
        z=pnData[px1.y][px1.x];
        boolean settled=false;
        if(sign*(z-z0)<=0) {
            pdEnv[y0][x0]=z0;
        }else{
            findSecondEnvPoint_slope(points,x0,y0,px1,px2,sign);
            if(px2.x==x0&&px2.y==y0){
                pdEnv[y0][x0]=pnData[y0][x0];
            }else{
                while(CommonGeometryMethods.collinear(x0, y0, px1.x, px1.y, px2.x, px2.y)){
                    if(!CommonGeometryMethods.sameSide(x0, y0, px1.x, px1.y, px2.x, px2.y)){
                        dist01=Math.sqrt(CommonGeometryMethods.dist2(x0, y0, px1.x, px1.y));
                        dist12=Math.sqrt(CommonGeometryMethods.dist2(px1.x, px1.y,px2.x, px2.y));
                        z=CommonMethods.interpolation(0, pnData[px1.y][px1.x], dist12, pnData[px2.y][px2.x], dist01);
                        if(sign*(z-z0)<0) z=z0;
                        pdEnv[y0][x0]=z;
                        settled=true;
                        break;
                    }else{
                        if(CommonGeometryMethods.sameSide(px1.x, px1.y,x0,y0, px2.x, px2.y)) px1.setLocation(px2);
                        findSecondEnvPoint_slope(points,x0,y0,px1,px2,sign);
                        if(px2.x==x0&&px2.y==y0){
                            pdEnv[y0][x0]=z0;
                            settled=true;
                            break;
                        }
                    }
                }
                if(!settled){
                    findThirdEnvPoint(points,x0,y0,px1.x,px1.y,px2.x,px2.y,px3,sign);
                    if(px3.x==x0&&px3.y==y0){
                        pdEnv[y0][x0]=pnData[y0][x0];
                        settled=true;
                    } else {                   
                        while(!CommonGeometryMethods.insideTriangle(x0,y0,px1.x,px1.y,px2.x,px2.y,px3.x,px3.y)){
                            if(CommonGeometryMethods.sameSide(px3.x, px3.y, px1.x, px1.y, x0,y0,px2.x,px2.y))
                                px1.setLocation(px3);
                            else
                                px2.setLocation(px3);
                            findThirdEnvPoint(points,x0,y0,px1.x,px1.y,px2.x,px2.y,px3,sign);
                            if(px3.x==x0&&px3.y==y0){
                                pdEnv[y0][x0]=pnData[y0][x0];
                                settled=true;
                                break;
                            }                        
                        }
                    }
                    if(!settled){
                        Plane3D SU=new Plane3D(px1.x,px1.y,pnData[px1.y][px1.x],px2.x,px2.y,pnData[px2.y][px2.x],px3.x,px3.y,pnData[px3.y][px3.x]);
                        z=SU.getZ(x0, y0);
                        if(sign*(z-z0)<0) z=z0;
                        pdEnv[y0][x0]=SU.getZ(x0, y0);
                    }
                }
            }
        }
    }
    
    Point findFirstEnvPoint (int x0, int y0, ArrayList<Point> points, int sign){
        double z,zx=Double.NEGATIVE_INFINITY, z0=pnData[y0][x0];
        ArrayList<Integer> indexesX=new ArrayList();
        cIS.setCenter(x0,y0);
        cIS.getInnerPoints(points);
        Point p,px,p1,p2;
        int i,x,y,len=points.size(),index0;
        //finding the first point for upper and lower envelope
        if(x0==27&&y0==20){
            x0=x0;
        }
        for(i=len-1;i>=0;i--){
            p=points.get(i);
            x=p.x;
            y=p.y;
            if(x==x0&&y==y0){
                index0=i;
                continue;
            }
            z=sign*pnData[y][x];
            if(z>zx){
                indexesX.clear();
                indexesX.add(i);        
                zx=z;
            } else if(z==zx){
                indexesX.add(i);
            }
        }
        
        int nx=indexesX.size();
        ArrayList<Point> pts=new ArrayList();
        for(i=0;i<nx;i++){
            pts.add(points.get(indexesX.get(i)));
        }
        return findClosestPoint(x0,y0,pts);
    }
    
    Point findClosestPoint(int x0, int y0, ArrayList<Point> points){
        Point pt,px=points.get(0);
        double dx=CommonGeometryMethods.dist2(x0, y0, px.x, px.y),d2;
        for(int i=1;i<points.size();i++){
            pt=points.get(i);
            d2=CommonGeometryMethods.dist2(x0, y0, pt.x, pt.y);
            if(d2<dx){
                dx=d2;
                px=pt;
            }
        }
        return px;
    }
    
    void findSecondEnvPoint(ArrayList<Point> points, int x0, int y0,int x1, int y1,Point p2,double sign){
        int i,x,y,z,len=points.size();
        double z0=pnData[y0][x0],z1=pnData[y1][x1],zX=Double.NEGATIVE_INFINITY,small=0.00000000001,zt;
        Vector3D Pv=findPerpendicularPoint(x0,y0,x1,y1);
        Plane3D S1;   
        double xv=Pv.getX(),yv=Pv.getY();
        Line L0=new Line(new Vector3D(x0,y0,z0),new Vector3D(x0,y0,z0+10));
        Vector3D Vt,V0;
        Point3D Pt;
        Point p;
        for(i=0;i<len;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            if(x==25&&y==12){
                x=x;
            }
            if(Math.abs(CommonGeometryMethods.crossProduct(x1, y1, xv, yv, x1, y1, x, y))<small) 
                continue;
            if(!CommonGeometryMethods.sameSide(x1,y1,xv,yv,x0,y0,x,y)) continue;
            z=pnData[y][x];
            if(x==x1&&y==y1) continue;
//            if(L1.contains(new Vector3D(x,y,z)))
//                continue;          
            S1=new Plane3D(x1,y1,z1,xv,yv,z1,x,y,z); 
            
            Vt=S1.intersection(L0);
            if(Vt==null) {
                continue;
            }
            zt=Vt.getZ();
            if(zt*sign>zX){
                zX=sign*zt;
                p2.setLocation(x,y);
            }
        }
        if(pnData[y0][x0]*sign>zX) 
            p2.setLocation(x0,y0);
        else {
            if(CommonGeometryMethods.dotProduct(p2.x, p2.y, x0, y0, p2.x, p2.y, x1, y1)<0) 
                findSecondEnvPoint(points,x0,y0,p2.x,p2.y,p2,sign);
        }
    }
    void findSecondEnvPoint_slope(ArrayList<Point> points, int x0, int y0,Point p1,Point p2,double sign){
        int i,x,y,z,len=points.size(),x1=p1.x,y1=p1.y;
        double z0=pnData[y0][x0],z1=pnData[y1][x1],sn=Double.POSITIVE_INFINITY,small=0.00000000001,slope0;
        Vector3D Pv=findPerpendicularPoint(x0,y0,x1,y1);
        double xv=Pv.getX(),yv=Pv.getY(),dist, slope,dz;
        Point p;
        
        dz=z1-z0;
        dist=CommonMethods.getDistanceToLine(x1, y1, xv, yv, x0, y0);
        slope0=sign*dz/dist;
            
        for(i=0;i<len;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            if(x==25&&y==12){
                x=x;
            }
            if(Math.abs(CommonGeometryMethods.crossProduct(x1, y1, xv, yv, x1, y1, x, y))<small) 
                continue;
            if(!CommonGeometryMethods.sameSide(x1,y1,xv,yv,x0,y0,x,y)) continue;
            z=pnData[y][x];
            if(x==x1&&y==y1) continue;
//            if(L1.contains(new Vector3D(x,y,z)))
//                continue;  
            dz=z1-z;
            dist=CommonMethods.getDistanceToLine(x1, y1, xv, yv, x, y);
            slope=sign*dz/dist;
            if(slope<sn){
                sn=slope;
                p2.setLocation(x,y);
            }
        }
        if(sn>slope0) 
            p2.setLocation(x0,y0);
        else {
            if(CommonGeometryMethods.dotProduct(p2.x, p2.y, x0, y0, p2.x, p2.y, x1, y1)<0) {
                p1.setLocation(p2);
                findSecondEnvPoint_slope(points,x0,y0,p1,p2,sign);
            }
        }
    }
    void findThirdEnvPoint(ArrayList<Point> points, int x0, int y0,int x1, int y1,int x2,int y2, Point p3,double sign){
        int i,len=points.size(),x,y;
        double z,zX=Double.NEGATIVE_INFINITY;
        Plane3D S1;   
        Point3D Pt;
        Point p;
        Line L;
        for(i=0;i<len;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            if(x==x0&&y==y0) continue;
            if(x==x1&&y==y1) continue;
            if(x==x2&&y==y2) continue;
            if(x1==x2&&y1==y2) 
                continue;
            if(!CommonGeometryMethods.sameSide(x1,y1,x2,y2,x0,y0,x,y)||CommonGeometryMethods.collinear(x1,y1,x2,y2,x,y)) continue;
            S1=new Plane3D(x,y,pnData[y][x],x1,y1,pnData[y1][x1],x2,y2,pnData[y2][x2]);
            if(S1==null) continue;
            z=S1.getZ(x0, y0)*sign;
            if(z>zX){
                zX=z;
                p3.setLocation(p);
            }
        }
        if(pnData[y0][x0]*sign>zX) p3.setLocation(x0,y0);
    }
    /*
    void findThirdEnvPoint_slope(ArrayList<Point> points, int x0, int y0,int x1, int y1,int x2,int y2, Point p3,double sign){
        int i,len=points.size(),x,y;
        double z,sn=Double.POSITIVE_INFINITY,slope,dist,z0=pnData[y0][x0];
        Plane3D S1;   
        Point3D Pt;
        Point p;
        Line L;
        for(i=0;i<len;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            if(x==x0&&y==y0) continue;
            if(x==x1&&y==y1) continue;
            if(x==x2&&y==y2) continue;
            if(x1==x2&&y1==y2) 
                continue;
            if(!CommonGeometryMethods.sameSide(x1,y1,x2,y2,x0,y0,x,y)||CommonGeometryMethods.collinear(x1,y1,x2,y2,x,y)) continue;
            dz=z0-
        }
        if(pnData[y0][x0]*sign>zX) p3.setLocation(x0,y0);
    }*/
    Vector3D findPerpendicularPoint(int x0, int y0,int x1, int y1){//pick a point P located on a hirizontal line going through the point P1 (x1,y1, z1=pnData[y1][x1]) . 
        //and perpendicular to the line connecting P0 and P1. The distance between P and P1 is d=1;
        double k,b,A,B,C,d=1;
        double xt,yt,zt=pnData[y1][x1];
        if(y0==y1){
            xt=x1;
            yt=y1+1;
            return new Vector3D(xt,yt,zt);
        }else if(x0==x1) {
            xt=x1+1;
            yt=x1;
        }
        k=-(x1-x0)/(double)(y1-y0);
        b=-(x0*x1-x1*x1+y0*y1-y1*y1)/(double)(y1-y0);
        A=k*k+1;
        B=2*k*b-2*y1*k-2*x1;
        C=x1*x1+y1*y1-2*y1*b+b*b-1;
        xt=(Math.sqrt(B*B-4*A*C)-B)/(2*A);
        yt=k*xt+b;
        double delta=(x0-x1)*(x0-x1)+(y0-y1)*(y0-y1)+(y1-yt)*(y1-yt)+(x1-xt)*(x1-xt)-(x0-xt)*(x0-xt)-(y0-yt)*(y0-yt);
        return new Vector3D(xt,yt,zt);
    }
    
}
