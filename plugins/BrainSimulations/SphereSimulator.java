/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import BrainSimulations.DataClasses.*;
/**
 *
 * @author Taihao
 */
public class SphereSimulator extends SpacialObject{
    float radius,radius2;
    float center[];
    int pixel;
    public SphereSimulator(float x[],float r){
        radius=r;
        radius2=r*r;
        center=new float[3];
        m_fRanges=new FloatRange[3];
        for(int i=0;i<3;i++){
            center[i]=x[i];
        }
        int red=0,g=0,b=255;
        pixel=0;
        pixel=(red<<16)|(g<<8)|b;
        setRanges();
        setEnclosingBox();
    }
    public SphereSimulator(){        
        radius=0.f;
        for(int i=0;i<3;i++){
            center[i]=0.f;
        }
    }
    public int draw(int pixels[], float x0[], int of[], float delta, int w, int h){
        int successful=-1;
        int o1=of[0],o2=of[1],o3=of[2];
        if(!m_fRanges[o3].contains(x0[o3])) return -1;
        float fo1,o1Min,o1Max,fo2,o2Min,o2Max;
        int i,o3Int,o2Int0,o2Int;        
        int col,coli,colf,row,rowi,rowf,offset;
        int x[]=new int[3];
        float xf[]=new float[3];
        
        for(i=0;i<3;i++){
            xf[i]=x0[i];
        }
        float fH=(h-1)*delta,fW=(w-1)*delta;
        float f1=radius*radius-(center[o3]-x0[o3])*(center[o3]-x0[o3]);
        f1=(float)Math.sqrt(f1);
        FloatRange o1RangeF=new FloatRange((center[o1]-f1),(center[o1]+f1));
        FloatRange o2RangeF=new FloatRange((center[o2]-f1),(center[o2]+f1));
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
            efr=effectiveRange(xf,of);            
            o1Min=efr.getBiggerMin(o1RangeF1);
            o1Max=efr.getSmallerMax(o1RangeF1);
            coli=(int)((o1Min-x0[o1])/delta);
            colf=(int)((o1Max-x0[o1])/delta);
            offset=row*w;
            for(col=coli;col<=colf;col++){
                fo1=x0[o1]+col*delta;
                xf[o1]=fo1;
                if(contains(xf)){
                    try{pixels[offset+col]=getPixel();}
                    catch(ArrayIndexOutOfBoundsException e){
                        e=e;
                    }
                }
            }
       }
        successful=1;
        return successful;
    }
    FloatRange effectiveRange(float x[], int of[]){
        int o1=of[0],o2=of[1],o3=of[2];
        double dtemp=radius*radius-(center[o3]-x[o3])*(center[o3]-x[o3])-(center[o2]-x[o2])*(center[o2]-x[o2]);
        dtemp=(float)Math.sqrt(dtemp);
        float fMin=center[o1]-(float) dtemp,fMax=center[o1]+(float) dtemp;
        return new FloatRange(fMin,fMax);
    }
    
   public int  getPixel(float x, float y, float z){  
       int pixel=0;
       return pixel;
   }
   
   void setRanges(){
       for(int i=0;i<3;i++){
            m_fRanges[i]=new FloatRange(center[i]-radius,center[i]+radius);
       }
   }
   
   public FloatRange getRangeF(int oi){
       return new FloatRange(center[oi]-radius,center[oi]+radius);
   } 
   
   boolean contains(float x0[]){
       float dist=0.f;
       for(int i=0;i<3;i++){
           dist+=(center[i]-x0[i])*(center[i]-x0[i]);
       }
       return (dist<radius2);
   }
   public void setPixel(int pixel1){
       pixel=pixel1;
   }
   public int getPixel(){
       return pixel;
   }
   public boolean withinFrame(SpacialGrids sg){
       float c1[]=new float[3];
       for(int i=0;i<8;i++){
           for(int j=0;j<3;j++){
               c1[j]=enclosingBoxVertexes[i][j];
           }
           if(sg.contains(c1)<0) return false;
       }
       return true;
   }
   void setEnclosingBox(){
       float ranges[][]=new float[3][2];
       enclosingBoxVertexes=new float[8][3];
       int i,j,k;
       for(i=0;i<3;i++){
           ranges[i][0]=m_fRanges[i].getMin();
           ranges[i][1]=m_fRanges[i].getMax();
       }
       int index=0;
       for(i=0;i<2;i++){
           for(j=0;j<2;j++){
               for(k=0;k<2;k++){
                  enclosingBoxVertexes[index][0]=ranges[0][i];
                  enclosingBoxVertexes[index][1]=ranges[1][j];
                  enclosingBoxVertexes[index][2]=ranges[2][k];
                  index++;
               }
           }
       }
   }
   public float[][] getEnclosingBoxVertexes(){
       return enclosingBoxVertexes;
   }
}
