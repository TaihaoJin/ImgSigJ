/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.polyhedron;
import java.util.ArrayList;
import BrainSimulations.DataClasses.*;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class Polygon3D {
    FloatRange m_fRanges[];
    int V; //number of vertices
    int pixel=0;
    public vertex vertices[];
    public edge edges[];
    float eps=0.0000001f;
    public Polygon3D (){
        V=0;
    }
    public Polygon3D (int n){
        V=n;
        vertices=new vertex[V];
        edges=new edge[V];
    }
    public Polygon3D(vertex vtx[]){//assuming the vertices are already in correct order.
        this(vtx.length);
        for(int i=0;i<V;i++){
            vertices[i]=new vertex(vtx[i]);
            edges[i]=new edge(i,vtx[i],vtx[(i+1)%V]);
        }
        setRanges();
    }
    public Polygon3D(ArrayList <edge> edges0){
        this();
        ArrayList <edge> egs=circularEdges(edges0);
        if(egs!=null){
            V=egs.size();
            vertices=new vertex[V];
            edges=new edge[V];
            edge eg;
            for(int i=0;i<V;i++){
                eg=egs.get(i);
                edges[i]=new edge(eg);
                vertices[i]=new vertex(eg.v1);
            }
            setRanges();
        }
    }
    ArrayList <edge> circularEdges(ArrayList <edge> edges){
        ArrayList <edge> egs=new ArrayList <edge>();
        int size=edges.size();
        int size1;
        edge eg,eg0;
        for(int i=0;i<size;i++){
            ArrayList <edge> egs1=copyEdges(edges);
            egs.clear();
            eg=edges.get(i);
            egs1.remove(i);
            egs.add(eg);
            edge eg2=extractNextEdge(eg,egs1);
            while(eg2!=null){
                egs.add(eg2);
                eg2=extractNextEdge(eg2,egs1);
            }
            size1=egs.size();
            if(size1<3) continue;
            eg2=egs.get(size1-1);
            if(eg2.nextEdge(eg)) return egs;
        }
        return null;
    }    
    edge extractNextEdge(edge eg1, ArrayList <edge> edges){
        int size=edges.size();
        for(int i=0;i<size;i++){
            if(eg1.nextEdge(edges.get(i))){
                edge eg=new edge(edges.get(i));
                edges.remove(i);
                return eg;
            }
        }
        return null;
    }    
    ArrayList <edge> copyEdges(ArrayList <edge> edges){
        ArrayList <edge> egs=new ArrayList <edge>();
        int size=edges.size();
        for(int i=0;i<size;i++){
            edge eg=new edge(edges.get(i));
            egs.add(eg);
        }
        return egs;
    }
    public edge cross_sectionEdge(double[]x0, int oi[]){
        edge eg=null;
        for(int i=0;i<V;i++){
            CrossPoint cp=edges[i].crossPoint(x0,oi);
            if(cp!=null){
                if(eg==null) eg=new edge();
                if(cp.index==0) eg.v1=new vertex(cp);
                else eg.v2=new vertex(cp);
            }
        }
        return eg;
    }
    boolean onPlane(float x0[], int index){
        if((m_fRanges[index].getMax()-m_fRanges[index].getMin())>eps) return false;
        if(Math.abs(m_fRanges[index].getMax()-x0[index])>eps) return false;
        return true;
    }
    public int draw(int pixels[], float x0[], int of[], float delta, int w, int h){
        if(!onPlane(x0,of[2])) return -1;
        //x0 is the coordinates of the top left corner of the image section, of is the 
        //orientation factor array. delta is the pixel width (and the same as pixel height).
        //w and h are the width and the height (number of pixels) of the image, respectively.
        int successful=-1;
        int o1=of[0],o2=of[1],o3=of[2];
        float fo1,o1Min,o1Max,fo2,o2Min,o2Max;
        int i,o3Int,o2Int0,o2Int;        
        int col,coli,colf,row,rowi,rowf,offset;
        int x[]=new int[3];
        double xf[]=new double[3];
        
        for(i=0;i<3;i++){
            xf[i]=x0[i];
        }
        
        float fH=(h-1)*delta,fW=(w-1)*delta;
//        float f1=radius*radius-(center[o3]-x0[o3])*(center[o3]-x0[o3]);
//        f1=(float)Math.sqrt(f1);
        FloatRange o1RangeF=m_fRanges[o1];
        FloatRange o2RangeF=m_fRanges[o2];
        FloatRange o1RangeF1=new FloatRange(x0[o1],(x0[o1]+fW));
        FloatRange o2RangeF1=new FloatRange(x0[o2],(x0[o2]+fH));
        if(!o1RangeF.overlap(o1RangeF1))return -1;
        if(!o2RangeF.overlap(o2RangeF1))return -1;
        o1Min=o1RangeF.getBiggerMin(o1RangeF1);
        o1Max=o1RangeF.getSmallerMax(o1RangeF1);
        o2Min=o2RangeF.getBiggerMin(o2RangeF1);
        o2Max=o2RangeF.getSmallerMax(o2RangeF1);
        rowi=(int)((o2Min-x0[o2])/delta);
        rowf=(int)((o2Max-x0[o2])/delta);
        FloatRange efr;
        for(row=rowi;row<=rowf;row++){
            fo2=x0[o2]+row*delta;
            xf[o2]=fo2;
            efr=effectiveRange(xf,o2,o1);            
            o1Min=efr.getBiggerMin(o1RangeF1);
            o1Max=efr.getSmallerMax(o1RangeF1);
            coli=(int)((o1Min-x0[o1])/delta);
            colf=(int)((o1Max-x0[o1])/delta);
            offset=row*w;
            for(col=coli;col<=colf;col++){
                fo1=x0[o1]+col*delta;
                xf[o1]=fo1;
                pixels[offset+col]=pixel;
            }
       }
        successful=1;
        return successful;
    }
    
    public void setPixel(int p){
        pixel=p;
    }
    
    FloatRange effectiveRange(double xf[],int index0, int index1){
        //calling this method assured that the polygon is on the plane of x_index0=xf[index2]
        //index2 is one of 0,1,2 other than index0 and index1;
        FloatRange fr=new FloatRange();
        double yMin=fr.getMin();
        double yMax=fr.getMax();
        double x=xf[index0],x1,x2,y1,y2,y;
        edge eg;
        for(int i=0;i<V;i++){
            eg=edges[i];
            x1=eg.v1.getCoordinate(index0);
            x2=eg.v2.getCoordinate(index0);
            if((x1-x)*(x2-x)>0) continue;
            y1=eg.v1.getCoordinate(index1);
            y2=eg.v2.getCoordinate(index1);
            y=y1+(y2-y1)*(x-x1)/(x2-x1);
            if(yMin>y)yMin=y;
            if(yMax<y)yMax=y;
        }
        fr.setMin((float)yMin);
        fr.setMax((float)yMax);
        return fr;
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
    public boolean contains(double[] x0){
        /* this method is called under the assumption that the point x0 is on 
         * the plane of the polygon
        */
        ArrayList <Integer> ia=nonNormalAxis();
        int size=ia.size();
        if(size<2) IJ.error("a polygon3D with zero area (polygon3D line 212");
        int o3=ia.get(0);
        int oi[]=new int[3];
        oi[2]=o3;
        oi[0]=(o3+1)%3;
        oi[1]=(o3+2)%3;
        edge ed=cross_sectionEdge(x0, oi);
        o3=ia.get(1);
        return ((ed.v1.getCoordinate(o3)-x0[o3])*(ed.v2.getCoordinate(o3)-x0[o3])<=0.);
    }
    public ArrayList <Integer> nonNormalAxis(){
        ArrayList <Integer> ia =new ArrayList <Integer>();
        for(int i=0;i<3;i++){
            if(m_fRanges[i].getExpand()>eps){
                ia.add(i);
            }
        }
        return ia;
    }
}
