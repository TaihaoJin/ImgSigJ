/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import BrainSimulations.DataClasses.*;
import java.util.ArrayList;
import utilities.*;
import BrainSimulations.Polyhedron;
import BrainSimulations.BoxSimulator;

/**
 *
 * @author Taihao
 */
public class EllipsoidSimulator extends SpacialObject{
    float radius[],radius2[],rotationAngles[],maxRadius;
    float center[];
    double rm[][];
    int pixel;
    double Q[][];
    double m_dScale;
    public static final int White = (255<<16)|(255<<8)|255;
    public static final int Red = (255<<16)|(0<<8)|0;
    public static final int Blue = (0<<16)|(0<<8)|255;
    public static final int Green = (0<<16)|(255<<8)|0;
    public static final double eps=0.00000000001;
    float enclosingBoxVertexes0[][];
    
    public EllipsoidSimulator(){  
        maxRadius=0f;
        m_fRanges=new FloatRange[3];
        radius=new float[3];
        radius2=new float[3];
        rotationAngles=new float[3];
        center =new float[3];
        rm=new double[3][3];
        enclosingBoxVertexes=new float[8][3];
        enclosingBoxVertexes0=new float[8][3];
        int red=0,g=0,b=255;
        pixel=0;
        pixel=(red<<16)|(g<<8)|b;
        Q=new double[3][3];
    }
    public EllipsoidSimulator(float x[],float r[], float angle[]){
        this();
        m_dScale=1.;
        for(int i=0;i<3;i++){
            m_dScale*=r[i];
        }
//        m_dScale=Math.cbrt(m_dScale);        
        m_dScale=1.;        
        for(int i=0;i<3;i++){
            if(r[i]>maxRadius)maxRadius=r[i];
            radius[i]=r[i];
            radius2[i]=r[i]*r[i];
            center[i]=x[i];
            rotationAngles[i]=angle[i];
        }
        init();
    }
    void init(){
        calRotationalMatrix();
        calEquationCoefficientMtrices();
        setRanges();
        setEnclosingBox();
        setEnclosingBox0();
    }
    void calEquationCoefficientMtrices(){
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                Q[i][j]+=0.;
                for(int k=0;k<3;k++){                
                    Q[i][j]+=m_dScale*m_dScale*(rm[k][i]*rm[k][j])/(radius[k]*radius[k]);
                }
            }
        }
    }
    void calRotationalMatrix(){
        double a=Angles.radian(rotationAngles[0]),b=Angles.radian(rotationAngles[1]),g=Angles.radian(rotationAngles[2]);
        rm[0][0]=Math.cos(g)*Math.cos(b);
        rm[0][1]=Math.cos(g)*Math.sin(b)*Math.sin(a)+Math.sin(g)*Math.cos(a);
        rm[0][2]=Math.sin(g)*Math.sin(a)-Math.cos(g)*Math.sin(b)*Math.cos(a);
        rm[1][0]=-Math.sin(g)*Math.cos(b);
        rm[1][1]=Math.cos(g)*Math.cos(a)-Math.sin(g)*Math.sin(b)*Math.sin(a);
        rm[1][2]=Math.sin(g)*Math.sin(b)*Math.cos(a)+Math.cos(g)*Math.sin(a);
        rm[2][0]=Math.sin(b);
        rm[2][1]=-Math.cos(b)*Math.sin(a);
        rm[2][2]=Math.cos(b)*Math.cos(a);
    }
    
    boolean getCoordinates(float x0[], float x1[], int index){//index indicates the unknown coordinate.
        //x1 store the two value for x0[index], x1[0]<x1[1]
        boolean succesful=true;
        int oi[]=new int[3];
        getOrientationIndexes(index, oi);
        int o1=oi[0], o2=oi[1], o3=oi[2];
        double A=Q[o1][o1];
        double x2=(x0[o2]-center[o2]);
        double x3=(x0[o3]-center[o3]);
        double B=2.*(Q[o1][o2]*x2+Q[o1][o3]*x3);
        double C=Q[o2][o2]*x2*x2+Q[o3][o3]*x3*x3+2.*Q[o2][o3]*x2*x3-1;
        double d1=B*B-4.*A*C;
        if(Math.abs(d1/(B*B))<eps) d1=0.;            
        if((d1)<0.) return false;
        x1[0]=(float)((-B-Math.sqrt(d1))/(2.*A))+center[index];
        x1[1]=(float)((-B+Math.sqrt(d1))/(2.*A))+center[index];
        return succesful;
    }
    
    void getOrientationIndexes(int index, int oi[]){
        
        switch (index){
            case 0://calculate x, based on know y, and z;
                oi[0]=0;
                oi[1]=1;
                oi[2]=2;
                break;
            case 1:
                oi[0]=1;
                oi[1]=2;
                oi[2]=0;
                break;
            case 2:
                oi[0]=2;
                oi[1]=0;
                oi[2]=1;
                break;
            default:
                oi[0]=0;
                oi[1]=1;
                oi[2]=2;
                break;
        }
    }
    
    FloatRange calExtrema(int index){
        int oi[]=new int[3];
        getOrientationIndexes(index, oi);
        int o1=oi[0], o2=oi[1], o3=oi[2];
        
        ArrayList <Float> x1= new ArrayList<Float>();
        ArrayList <Float> x2= new ArrayList<Float>();
        ArrayList <Float> x3= new ArrayList<Float>();
        ArrayList <Float> x21= new ArrayList<Float>();
        ArrayList <Float> x30= new ArrayList<Float>();
        ArrayList <Float> x31= new ArrayList<Float>();
        
        double A2=Q[o1][o1]*Q[o2][o2]*Q[o2][o2]-Q[o1][o2]*Q[o1][o2]*Q[o2][o2];
        double B2=2.*(Q[o1][o1]*Q[o2][o2]*Q[o2][o3]-Q[o1][o2]*Q[o1][o3]*Q[o2][o2]);
        double C2=Q[o1][o1]*Q[o2][o3]*Q[o2][o3]+Q[o1][o2]*Q[o1][o2]*Q[o3][o3]-2.*Q[o1][o2]*Q[o1][o3]*Q[o2][o3];
        double D2=-Q[o1][o2]*Q[o1][o2];
        
        double A3=Q[o1][o1]*Q[o2][o3]*Q[o2][o3]+Q[o1][o3]*Q[o1][o3]*Q[o2][o2]-2.*Q[o1][o2]*Q[o1][o3]*Q[o2][o3];
        double B3=2.*(Q[o1][o1]*Q[o3][o3]*Q[o2][o3]-Q[o1][o2]*Q[o1][o3]*Q[o3][o3]);
        double C3=Q[o1][o1]*Q[o3][o3]*Q[o3][o3]-Q[o1][o3]*Q[o1][o3]*Q[o3][o3];
        double D3=-Q[o1][o3]*Q[o1][o3];
        
        double d1=A3*B2*B2/(2.*A2*A2)-A3*C2/A2-B3*B2/(2.*A2)+C3;
        double d2=0.5*B3/A2-0.5*B2*A3/(A2*A2);
        double d3=D3-A3*D2/A2;
        double A4=d1*d1+d2*d2*(4.*A2*C2-B2*B2);
        double B4=2.*d1*d3+4.*A2*d2*d2*D2;
        double C4=d3*d3;
        d1=B4*B4-4.*A4*C4;
        if(Math.abs(d1/(B4*B4))<eps) d1=0.;
        d1=Math.sqrt(d1);
//        float x31=(float)((-B4+d1)/(2.*A4))+center[o3];
//        float x32=(float)((-B4-d1)/(2.*A4))+center[o3];
        double x3square[]=new double [2];
        x3square[0]=(-B4+d1)/(2.*A4);
        x3square[1]=(-B4-d1)/(2.*A4);
        Float ftemp;
        
        if(x3square[0]>=0){
            ftemp=(float)(Math.sqrt(x3square[0]))+center[o3];
            x30.add(ftemp);
            ftemp=-(float)(Math.sqrt(x3square[0]))+center[o3];
            x30.add(ftemp);
        }
        if(x3square[1]>=0){
            ftemp=(float)(Math.sqrt(x3square[1]))+center[o3];
            x30.add(ftemp);
            ftemp=-(float)(Math.sqrt(x3square[1]))+center[o3];
            x30.add(ftemp);
        }
      
        int size=x30.size();
        float f;
        double b2t,c2t;
        
        for(int i=0;i<size;i++){ 
            f=x30.get(i)-center[o3];
            b2t=B2*f;
            c2t=C2*f*f+D2;
            d2=b2t*b2t-4.*A2*c2t;
            if(Math.abs(d2/(b2t*b2t))<eps) d2=0.;            
            if(d2<0) continue;
            d1=Math.sqrt(d2);
            ftemp=(float)((-b2t+d1)/(2.*A2))+center[o2];
            x21.add(ftemp);
            x31.add(f+center[o3]);
            ftemp=(float)((-b2t-d1)/(2.*A2))+center[o2];
            x21.add(ftemp);
            x31.add(f+center[o3]);
        }

        size=x31.size();
        float x0[]=new float[3];
        float ex1[]=new float[2];
        float fMin=center[index];
        float fMax=center[index];
        for(int i=0;i<3;i++){
            fMin+=radius[i];
            fMax-=radius[i];
        }
        
        float f11,f12;
        
        for(int i=0;i<size;i++){
            f=x21.get(i);
            ftemp=x31.get(i);
            x0[o2]=f;
            x0[o3]=ftemp;
            if((!getCoordinates(x0, ex1, index)))continue;
            f11=ex1[0];
            f12=ex1[1];
            x1.add(f11);
            x2.add(f);
            x3.add(ftemp);
            x1.add(f12);      
            x2.add(f);
            x3.add(ftemp);
            if(f11<fMin)fMin=f11;
            if(f12<fMin)fMin=f12;
            if(f11>fMax)fMax=f11;
            if(f12>fMax)fMax=f12;
        }
//        fMin=center[index]-maxRadius;
//        fMax=center[index]+maxRadius;
        return new FloatRange((float)(fMin*m_dScale), (float)(fMax*m_dScale));
    }
    
    
    FloatRange effectiveRange1(float x[], int index0, int index1){
        //This method returns the range of x[index0], using the coordinate x[index1] as a known input value.
        //The partial derivative of x[index0] over x[index2] is zero at the x[index1] where x[index0] reaches extrema. 
        // index2 is one of 0,1,2 not specified by index0 or index1
        int oi[]=new int[3];        
        getOrientationIndexes(index0, oi);
        int o1=oi[0], o2=oi[1], o3=oi[2];
        double A2=0.,B2=0.,C2=0.,D2=0.;
        int index2=0;
        
        if((index1-index0)==2||(index1-index0)==-1)
        {
            index2=index0+1;
            if(index2>2) index2-=3;
            A2=Q[o1][o2]*Q[o1][o2]-Q[o1][o1]*Q[o2][o2];
            B2=2.*(Q[o2][o1]*Q[o2][o3]-Q[o1][o3]*Q[o2][o2]);
            B2*=x[o3]-center[o3];
            C2=Q[o2][o3]*Q[o2][o3]-Q[o2][o2]*Q[o3][o3];
            C2*=(x[o3]-center[o3])*(x[o3]-center[o3]);
            D2=Q[o2][o2];
            C2+=D2;
        }else if((index1-index0)==1||(index1-index0)==-2){ 
            index2=index0+2;
            if(index2>2) index2-=3;
            A2=Q[o3][o1]*Q[o3][o1]-Q[o1][o1]*Q[o3][o3];
            B2=2.*(Q[o3][o1]*Q[o2][o3]-Q[o1][o2]*Q[o3][o3]);
            B2*=(x[o2]-center[o2]);
            C2=Q[o2][o3]*Q[o2][o3]-Q[o2][o2]*Q[o3][o3];
            D2=Q[o3][o3];
            C2*=(x[o2]-center[o2])*(x[o2]-center[o2]);
            C2+=D2;
        }
        float fMin=center[index0]+maxRadius;
        float fMax=center[index0]-maxRadius;
        double d1=0.,x1=0.,x2=0.;
        d1=B2*B2-4.*A2*C2;
        if(Math.abs(d1/(B2*B2))<eps) d1=0.;            
        if((d1)>=0.){
            d1=Math.sqrt(d1);
            x1=center[index0]-d1/(2.*A2)-B2/(2.*A2);
            x2=center[index0]+d1/(2.*A2)-B2/(2.*A2);
            if(x1<fMin)fMin=(float)x1;
            if(x2<fMin)fMin=(float)x2;
            if(x1>fMax)fMax=(float)x1;
            if(x2>fMax)fMax=(float)x2;
        }
//        fMin=center[index0]-maxRadius;
//        fMax=center[index0]+maxRadius;
        return new FloatRange(fMin, fMax);
    }
    FloatRange effectiveRange1_PartialDerivative(float x[], int index0, int index1){
        //This method returns the range of x[index0], using the coordinate x[index1] as a known input value.
        //The partial derivative of x[index0] over x[index2] is zero at the x[index1] where x[index0] reaches extrema. 
        // index2 is one of 0,1,2 not specified by index0 or index1
        int oi[]=new int[3];        
        getOrientationIndexes(index0, oi);
        int o1=oi[0], o2=oi[1], o3=oi[2];
        double A2=0.,B2=0.,C2=0.,D2=0.;
        int index2=0;
        
        if((index1-index0)==2||(index1-index0)==-1)
        {
            index2=index0+1;
            if(index2>2) index2-=3;
            A2=Q[o1][o1]*Q[o2][o2]*Q[o2][o2]-Q[o1][o2]*Q[o1][o2]*Q[o2][o2];
            B2=2.*(Q[o1][o1]*Q[o2][o2]*Q[o2][o3]-Q[o1][o2]*Q[o1][o3]*Q[o2][o2]);
            B2*=x[o3]-center[o3];
            C2=Q[o1][o1]*Q[o2][o3]*Q[o2][o3]+Q[o1][o2]*Q[o1][o2]*Q[o3][o3]-2.*Q[o1][o2]*Q[o1][o3]*Q[o2][o3];
            C2*=(x[o3]-center[o3])*(x[o3]-center[o3]);
            D2=-Q[o1][o2]*Q[o1][o2];
            C2+=D2;
        }else if((index1-index0)==1||(index1-index0)==-2){ 
            index2=index0+2;
            if(index2>2) index2-=3;
            C2=Q[o1][o1]*Q[o2][o3]*Q[o2][o3]+Q[o1][o3]*Q[o1][o3]*Q[o2][o2]-2.*Q[o1][o2]*Q[o1][o3]*Q[o2][o3];
            B2=2.*(Q[o1][o1]*Q[o3][o3]*Q[o2][o3]-Q[o1][o2]*Q[o1][o3]*Q[o3][o3]);
            B2*=(x[o2]-center[o2]);
            A2=Q[o1][o1]*Q[o3][o3]*Q[o3][o3]-Q[o1][o3]*Q[o1][o3]*Q[o3][o3];
            D2=-Q[o1][o3]*Q[o1][o3];
            C2*=(x[o2]-center[o2])*(x[o2]-center[o2]);
            C2+=D2;
        }
        double d1=Math.sqrt(B2*B2-4.*A2*C2);
        double x1=center[index0]-d1/(2.*A2)-B2/(2.*A2);
        double x2=center[index0]+d1/(2.*A2)-B2/(2.*A2);
        float x0[]=new float[3];
        for(int i=0;i<3;i++){
            x0[i]=x[i];
        }
        float fMin=center[index0]+maxRadius;
        float fMax=center[index0]-maxRadius;
        x0[index2]=(float)x1;
        float ex1[]=new float[2];
        float ex2[]=new float[2];
        if(getCoordinates(x0, ex1, index0)){
            if(ex1[0]<fMin) fMin=ex1[0];
            if(ex1[1]>fMax) fMax=ex1[1];
        }
        x0[index2]=(float)x2;
        if(getCoordinates(x0, ex2, index0)){
            if(ex2[0]<fMin) fMin=ex2[0];
            if(ex2[1]>fMax) fMax=ex2[1];
        }
        fMin=center[index0]-maxRadius;
        fMax=center[index0]+maxRadius;
        return new FloatRange(fMin, fMax);
    }

    FloatRange effectiveRange2(float x[], int index0){
        //This method returns the range of x[index0], minimum and the maximum of x[index0] on 
        //the surface of the spacial object, using the coordinates x[index1] and x[index2] as the known
        //input values. index1 and index2 are the two of 0,1,2 that are not specified by index0.
        float fMin=center[index0]+maxRadius;
        float fMax=center[index0]-maxRadius;
        float ex[]=new float[2];
        if(getCoordinates(x,ex,index0)){            
            if(ex[0]<fMin) fMin=ex[0];
            if(ex[1]>fMax) fMax=ex[1];
            if(ex[1]<fMin) fMin=ex[1];
            if(ex[0]>fMax) fMax=ex[0];
        }
//        return m_fRanges[index0];
//        if(fMin>fMax){
//            float f=fMax;
//            fMax=fMin;
//            fMin=f;
//        }
        return new FloatRange(fMin,fMax);
    }
    
    public int draw(int pixels[], float x0[], int of[], float delta, int w, int h){
        //x0 is the coordinates of the top left corner of the image section, of is the 
        //orientation factor array. delta is the pixel width (and the same as pixel height).
        //w and h are the width and the height (number of pixels) of the image, respectively.
//        drawBox(pixels, x0, of, delta, w, h);
//        drawBox0(pixels, x0, of, delta, w, h);
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
//        float f1=radius*radius-(center[o3]-x0[o3])*(center[o3]-x0[o3]);
//        f1=(float)Math.sqrt(f1);
        FloatRange o1RangeF=effectiveRange1(x0,o1,o3);
        FloatRange o2RangeF=effectiveRange1(x0,o2,o3);;
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
            efr=effectiveRange2(xf,o1);            
            o1Min=efr.getBiggerMin(o1RangeF1);
            o1Max=efr.getSmallerMax(o1RangeF1);
            coli=(int)((o1Min-x0[o1])/delta);
            colf=(int)((o1Max-x0[o1])/delta);
            offset=row*w;
            for(col=coli;col<=colf;col++){
                fo1=x0[o1]+col*delta;
                xf[o1]=fo1;
                pixels[offset+col]=getPixel();

 //               if(contains(xf)){
//                    try{pixels[offset+col]=getPixel();}
//                    catch(ArrayIndexOutOfBoundsException e){
//                        e=e;
//                    }
//                }
            }
       }
        successful=1;
        return successful;
    }
    
    public void drawBox(int pixels[], float x0[], int of[], float delta, int w, int h){
        BoxSimulator box=new BoxSimulator(enclosingBoxVertexes);
        box.setPixel(Red);
        box.draw(pixels, x0, of, delta, w, h);
    }

    public void drawBox_original(int pixels[], float x0[], int of[], float delta, int w, int h){
        
        int o1=of[0],o2=of[1],o3=of[2];
        int col,coli,colf,rowi,rowf,row,offset;
        rowi=(int)((m_fRanges[o2].getMin()-x0[o2])/delta);
        rowf=(int)((m_fRanges[o2].getMax()-x0[o2])/delta);
        if(rowi<0) rowi=0;
        if(rowf>h) rowf=h;
        for(row=rowi;row<rowf;row++){
            coli=(int)((m_fRanges[o1].getMin()-x0[o1])/delta);
            colf=(int)((m_fRanges[o1].getMax()-x0[o1])/delta);
            if(coli<0) coli=0;
            if(colf>w) colf=w;
            offset=row*w;
            for(col=coli;col<colf;col++){
                pixels[offset+col]=White;
            }
       }
    }    
    
    public void drawBox0(int pixels[], float x0[], int of[], float delta, int w, int h){        
        BoxSimulator box=new BoxSimulator(enclosingBoxVertexes0);
        box.setPixel(Green);
        box.draw(pixels, x0, of, delta, w, h);
    }
    
   public int  getPixel(float x, float y, float z){  
       int pixel=0;
       return pixel;
   }
   
   void setRanges(){
       for(int i=0;i<3;i++){
            m_fRanges[i]=calExtrema(i);
       }
   }
      
   boolean contains(float x0[]){
       float x[]=new float[3];
       float ft=0.f;
       for(int i=0;i<3;i++){
           ft=x0[i];
           if(!m_fRanges[i].contains(ft)) return false;
           x[i]=ft;
       }
       float ex[]=new float[2];
       if(getCoordinates(x, ex, 0)){  
           return (x0[0]>=ex[0])&&x0[0]<=ex[1];
       }else return false;
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
   
   void setEnclosingBox0(){
       float ranges[][]=new float[3][2];
       float enclosingBoxVertexes1[][]=new float[8][3];
       int i,j,k;
       for(i=0;i<3;i++){
           ranges[i][0]=-radius[i];
           ranges[i][1]=radius[i];
       }
       
       int index=0;
       for(i=0;i<2;i++){
           for(j=0;j<2;j++){
               for(k=0;k<2;k++){
                  enclosingBoxVertexes0[index][0]=ranges[0][i];
                  enclosingBoxVertexes0[index][1]=ranges[1][j];
                  enclosingBoxVertexes0[index][2]=ranges[2][k];
                  index++;
               }
           }
       }
       
       for(i=0;i<8;i++){
                   Rotations.rotationZ(enclosingBoxVertexes0[i],-rotationAngles[2]);
                   Rotations.rotationY(enclosingBoxVertexes0[i],-rotationAngles[1]);
                   Rotations.rotationX(enclosingBoxVertexes0[i],-rotationAngles[0]);
       }
       
       for(i=0;i<8;i++){
           for(j=0;j<3;j++){
               enclosingBoxVertexes0[i][j]+=center[j];
           }
       }              
   }
}
