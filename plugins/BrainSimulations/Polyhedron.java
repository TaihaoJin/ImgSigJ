/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;

import BrainSimulations.polyhedron.*;
import BrainSimulations.*;
import java.util.ArrayList;
import BrainSimulations.DataClasses.*;

/**
 *
 * @author Taihao
 */
public abstract class Polyhedron extends BrainSimulations.SpacialObject{
    int V, E, F; //number of Vertices, Edges and Faces.
    vertex vertices[];
    Polygon3D faces[];
    public Polyhedron (){        
    }
    public Polyhedron(int v, int e, int f){
        V=v;
        E=e;
        F=f;
        vertices=new vertex[V];
        for(int i=0;i<V;i++){
            vertices[i]=new vertex();
        }
        faces=new Polygon3D[F];
    }
    
    abstract void constructFaces();
    public Polygon3D constructPolygon(ArrayList<Integer> indexes){
        //this method return a Polygon3D constructed by vertices whose indexes
        //are stored in the arraylist indexes.
        int size=indexes.size();
        Polygon3D pg;
        int index;
        vertex vtx[]=new vertex[size];
        for(int i=0;i<size;i++){
            index=indexes.get(i);
            vtx[i]=new vertex(vertices[index]);
            vtx[i].setIndex(i);
        }
        return pg=new Polygon3D(vtx);
    }
    
    Polygon3D cross_sectionPolygon(double[]x0, int oi[]){
        //this method return the cross_section polygon of the polyhedron with
        //the plane defined by the point x0 and the axis oi[2].
        int o3=oi[2];
        if(!m_fRanges[o3].contains((float)x0[o3])) return null;
        ArrayList <edge> edges=new ArrayList <edge>();
        for(int i=0;i<F;i++){
            edge ce=faces[i].cross_sectionEdge(x0,oi);
            if(ce!=null) edges.add(ce);
        }
        if(edges.size()<3) return null;
        return new Polygon3D(edges);
    }
    public int draw(int pixels[], float x0[], int of[], float delta, int w, int h){
        //this method draw the cross-section polygon on the canvas plane defined by 
        //the opoint x0 and the axis of[2]. It returns 1 if the drawing was 
        //successful, otherwise returns 1.
        int o3=of[2];
        if(!m_fRanges[o3].contains(x0[o3])) return -1;
        double[] x1=new double[3];
        for(int i=0;i<3;i++){
            x1[i]=x0[i];
        }
        Polygon3D polygon=cross_sectionPolygon(x1, of);
        if(polygon!=null){
            polygon.setPixel(pixel);
            polygon.draw(pixels, x0, of, delta, w, h);
            return 1;
        }
        return -1;
    }
    void setRanges(){
        double x;
        int i,j;
        double fMin[]=new double[3];
        double fMax[]=new double[3];
        m_fRanges=new FloatRange[3];
        for(i=0;i<3;i++){
            m_fRanges[i]=new FloatRange();
            fMin[i]=m_fRanges[i].getMin();
            fMax[i]=m_fRanges[i].getMax();
        }
        for(i=0;i<V;i++){
            for(j=0;j<3;j++){
                x=vertices[i].getCoordinate(j);
                if(x<fMin[j])fMin[j]=x;
                if(x>fMax[j])fMax[j]=x;
            }
        }
        for(i=0;i<3;i++){
            m_fRanges[i].setMin((float)fMin[i]);
            m_fRanges[i].setMax((float)fMax[i]);
        }
    }
    public int getPixel(float x,float y, float z){
        double x0[]=new double[3];
        x0[0]=x;
        x0[1]=y;
        x0[2]=z;
        if(contains(x0)) return pixel;
        return -1;
    }
    boolean contains(double x0[]){
        int oi[]=new int[3];
        for(int i=0;i<2;i++){
            oi[i]=i;
        }
        Polygon3D pg=cross_sectionPolygon(x0, oi);
        if(pg==null) return false;
        return pg.contains(x0);
    }
    public void setPixel(int p){
        pixel=p;
    }
}
