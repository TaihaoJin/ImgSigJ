/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.Geometry.Circle_Double;

/**
 *
 * @author Taihao
 */
public class CocentricCircles_Rings {
    ArrayList<Double> m_dvRs;
    double cx,cy;//center
    ArrayList<Circle_Double> m_cvCircles;
    int numRings;
    public CocentricCircles_Rings(){
        setCenter(0,0);
        m_dvRs=new ArrayList();
        m_cvCircles=new ArrayList();
    }
    public ArrayList<Double> getRs(){
        return m_dvRs;
    }
    public CocentricCircles_Rings(double R, int numCircles){
        this();
        double delta=R/numCircles;
        ArrayList<Double> dvRs=new ArrayList();
        for(int i=1;i<=numCircles;i++){
            dvRs.add(i*delta);
        }
//        dvRs.remove(R);
        update(0,0,dvRs);
    }
    public void setCenter(double x, double y){
        cx=x;
        cy=y;
        for(int i=0;i<numRings;i++){
            m_cvCircles.get(i).setCenter(cx,cy);
        }
    }
    public CocentricCircles_Rings(double x, double y, ArrayList<Double> dvRs){
        this();
        update(x,y,dvRs);
    }
    public CocentricCircles_Rings(ArrayList<Double> dvRs){
        this();
        update(0,0,dvRs);
    }

    public void update(double x, double y, ArrayList<Double> dvRs){
        m_dvRs.clear();
        m_cvCircles.clear();
        cx=x;
        cy=y;
        int size=dvRs.size(),i;
        double r;
        for(i=0;i<size;i++){
            r=dvRs.get(i);
            m_dvRs.add(r);
            m_cvCircles.add(new Circle_Double(cx,cy,r));
        }
        numRings=dvRs.size();
    }
    public void areaInRings_Squre(double x, double y, double dLen, double [] pdAreas){
        double maxDist=Double.NEGATIVE_INFINITY;
        double dist2=CommonMethods.getDist2(cx,cy,x,y);
        if(dist2>maxDist) maxDist=dist2;
        dist2=CommonMethods.getDist2(cx,cy,x,y+dLen);
        if(dist2>maxDist) maxDist=dist2;
        dist2=CommonMethods.getDist2(cx,cy,x+dLen,y);
        if(dist2>maxDist) maxDist=dist2;
        dist2=CommonMethods.getDist2(cx,cy,x+dLen,y+dLen);
        if(dist2>maxDist) maxDist=dist2;
        maxDist=Math.sqrt(maxDist);
        int size=m_dvRs.size();
        int i;
        double r,dA,dA0=0;
        for(i=0;i<size;i++){
            r=m_dvRs.get(i);
            if(r>maxDist) {
                dA=dLen*dLen;
            }else{
                dA=m_cvCircles.get(i).areaWithinSquare(x, y, dLen);
            }
            pdAreas[i]=dA-dA0;
            dA0=dA;
        }
    }
    public boolean centeredInSquare(double x, double y, double dLen){
        if(cx<x||cx>x+dLen) return false;
        if(cy<y||cy>y+dLen) return false;
        return true;
    }
    public int getNumRings(){
        return numRings;
    }
    public double getMaxR(){
        return m_dvRs.get(numRings-1);
    }
}
