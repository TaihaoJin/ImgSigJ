/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import java.util.*;
import java.io.*;
import ij.IJ;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import BrainSimulations.DataClasses.*;
import java.util.Formatter;
import utilities.QuickFormatter;
import utilities.ArrayofArrays.*;
import utilities.FormattedReader;
import BrainSimulations.SpacialObject;
import BrainSimulations.DataClasses.FloatRange;
import utilities.StringReader;


/**
 *
 * @author Taihao
 */
public class SubstructureEssential extends SpacialObject {    
    public static String newline = System.getProperty("line.separator");
    BrainStructureNameNode StructureNode = new BrainStructureNameNode();
    intRange m_xRange, m_yRange, m_zRange;
    intRange m_Ranges[];
    FloatRange m_fRanges[];
    float m_fScale;
    String m_sComments;
    float m_fVolume;
    int m_nNumVoxels;
    intRange m_xRangesLayerp[];
    intRange m_yRangesLayerp[];        
    intPointer2 layerIndexesip[];
    intPointer2 layerIndexesfp[];
    intPointer2 stripeIndexesip;
    intPointer2 stripeIndexesfp;
    intPointer segmentIndexesip;
    intPointer segmentIndexesfp;
    FloatRange m_xRangesLayerF[];
    FloatRange m_yRangesLayerF[];
    FloatRange m_xRangesStripeF[];
    FloatRange m_zRangesStripeF[];
    FloatRange m_zRangesSegmentF[];
    FloatRange m_yRangesSegmentF[];
    intRange m_zRangesStripe[];
    intRange m_xRangesStripe[];
    intRange m_zRangesSegment[];
    intRange m_yRangesSegment[];
    intRange m_ofRanges[][][];
    FloatRange m_ofRangesF[][][];
 
    int m_nNumSegments;
    intRange xRange;
    intRange yRange;
    
    public SubstructureEssential(){
        m_fScale=1.f;
        m_sComments="";
        m_xRange=new intRange();
        m_yRange=new intRange();
        m_zRange=new intRange();
        m_nNumSegments=0;
    }
    public BrainStructureNameNode getStructureNode(){
        return StructureNode;
    }
    public intRange getXRange(){
        return new intRange(m_xRange.getMin(),m_xRange.getMax());
    }
    public intRange getYRange(){
        return new intRange(m_yRange.getMin(),m_yRange.getMax());
    }
    public intRange getZRange(){
        return new intRange(m_zRange.getMin(),m_zRange.getMax());
    }
    
    public FloatRange getXRangeFloat(){
        return new FloatRange(m_xRange,m_fScale);
    }
    public FloatRange getYRangeFloat(){
        return new FloatRange(m_yRange,m_fScale);
    }
    public FloatRange getZRangeFloat(){
        return new FloatRange(m_zRange,m_fScale);
    }
    
    public int getPixel(float x, float y, float z){        
        int pixel=(255<<24)|(255<<16)|(255<<8)|255;
        if(contains(x,y,z)){
            pixel=getPixel();          
        }
        return pixel;
    }
    
    int getPixel(){
//        int transparency=255;
        int pixel=0;
//      int pixel=(transparency<<24)|(StructureNode.red<<16)|
//        (StructureNode.green<<8)|StructureNode.blue;
        pixel=(50<<24)|((StructureNode.red)<<16)|((StructureNode.green)<<8)|(StructureNode.blue);
        return  pixel;
    }
    
    public boolean contains(float xf, float yf, float zf){
            int x=(int) (xf/m_fScale);
            int y=(int) (yf/m_fScale);
            int z=(int) (zf/m_fScale);
            return contains(x,y,z);
    }
    
    public int contains(float xf[]){
        int x[]=new int[3];
        for(int i=0;i<3;i++){
            x[i]=(int)(xf[i]/m_fScale);
        }
        return contains(x);
    }
    
    public boolean contains(int x, int y, int z){
        int x0[]=new int[3];
        x0[0]=x;
        x0[1]=y;
        x0[2]=z;
        if(contains(x0)<0) return false;
        return true;
    }
    
    public int contains(int x0[]){
        int x=x0[0],y=x0[1],z=x0[2];
        int layer,stripe,seg,segs;
        layer=m_zRange.GetIndex(z);
        if(layer<0) return -1;
        stripeIndexesip=layerIndexesip[layer];
        if(stripeIndexesip.p2.length==0) return -1;
        stripe=m_yRangesLayerp[layer].GetIndex(y);        
        if(stripe<0) return -1;
        stripeIndexesfp=layerIndexesfp[layer];
        try{segmentIndexesip=stripeIndexesip.p2[stripe];}
        catch (ArrayIndexOutOfBoundsException e){
            e=e;
        }
        segmentIndexesfp=stripeIndexesfp.p2[stripe];
        segs=segmentIndexesip.p.length;
        for(seg=0;seg<segs;seg++){
            try{if(x<=segmentIndexesfp.p[seg]&&x>=segmentIndexesip.p[seg]) 
                return StructureNode.informaticsId;}
            catch(IndexOutOfBoundsException e){
                e=e;
            }
        }        
        return -1;
    }
    
    public void setColor(int pixel){
        StructureNode.red=0xff & (pixel>>16);
        StructureNode.green=0xff & (pixel>>8);
        StructureNode.blue=0xff & pixel;
    }
    
    
    public float getScale(){
        return m_fScale;
    }    
    
    
    class intPointer{
        public int p[];
        public intPointer(int size){
            p=new int[size];
        }
    }
    class intPointer2{
        public intPointer p2[];
        intPointer2(int size){
            p2=new intPointer[size];
        }
    }
    class intPointer3{
        public intPointer2 p3[];
        intPointer3(int size){
            p3=new intPointer2[size];
        }
    }
    
    public void importSubstructure(DataInputStream dis)throws IOException{
    
        int length=dis.readInt();
        StringReader sr =new StringReader();       
        StructureNode.StructureName=sr.readString(dis, length);
        length=dis.readInt();
        StructureNode.Abbreviation=sr.readString(dis, length);
        length=dis.readInt();
        StructureNode.ParentStruct=sr.readString(dis, length);        
        StructureNode.red=dis.readInt();
        StructureNode.green=dis.readInt();
        StructureNode.blue=dis.readInt();
        StructureNode.informaticsId=dis.readInt();
        StructureNode.StructureId=dis.readInt();
        length=dis.readInt();    
        m_sComments=sr.readString(dis, length);
        m_nNumVoxels=dis.readInt();
        m_fVolume=(float)m_nNumVoxels;
        m_fScale=dis.readFloat();
        m_xRange.setMin(dis.readInt());
        m_xRange.setMax(dis.readInt());
        m_yRange.setMin(dis.readInt());
        m_yRange.setMax(dis.readInt());
        m_zRange.setMin(dis.readInt());
        m_zRange.setMax(dis.readInt());
        int layers=dis.readInt();
        int layer;
                
        m_xRangesLayerp=new intRange[layers];
        m_yRangesLayerp=new intRange[layers];
        m_xRangesLayerF=new FloatRange[layers];
        m_yRangesLayerF=new FloatRange[layers];
        for(layer=0;layer<layers;layer++){
            xRange=new intRange();
            yRange=new intRange();
            xRange.setMin(dis.readInt());
            xRange.setMax(dis.readInt());
            yRange.setMin(dis.readInt());
            yRange.setMax(dis.readInt());
            m_xRangesLayerp[layer]=xRange;
            m_yRangesLayerp[layer]=yRange;
            m_xRangesLayerF[layer]=new FloatRange(xRange,m_fScale);
            m_yRangesLayerF[layer]=new FloatRange(yRange,m_fScale);
        }
        
        int segs;
        segs=dis.readInt();        
        m_yRangesSegment=new intRange[segs];
        m_zRangesSegment=new intRange[segs];
        m_yRangesSegmentF=new FloatRange[segs];
        m_zRangesSegmentF=new FloatRange[segs];
        for(int i=0;i<segs;i++){
            m_yRangesSegment[i]=new intRange();
            m_zRangesSegment[i]=new intRange();
            m_yRangesSegment[i].setMin(dis.readInt());
            m_yRangesSegment[i].setMax(dis.readInt());
            m_zRangesSegment[i].setMin(dis.readInt());
            m_zRangesSegment[i].setMax(dis.readInt());
            m_yRangesSegmentF[i]=new FloatRange(m_yRangesSegment[i],m_fScale);
            m_zRangesSegmentF[i]=new FloatRange(m_zRangesSegment[i],m_fScale);
        }
        
        int stripes=dis.readInt();
        m_xRangesStripe=new intRange[stripes];
        m_zRangesStripe=new intRange[stripes];
        m_xRangesStripeF=new FloatRange[stripes];
        m_zRangesStripeF=new FloatRange[stripes];
        for(int i=0;i<stripes;i++){
            m_xRangesStripe[i]=new intRange();
            m_zRangesStripe[i]=new intRange();
            m_xRangesStripe[i].setMin(dis.readInt());
            m_xRangesStripe[i].setMax(dis.readInt());
            m_zRangesStripe[i].setMin(dis.readInt());
            m_zRangesStripe[i].setMax(dis.readInt());
            m_xRangesStripeF[i]=new FloatRange(m_xRangesStripe[i],m_fScale);
            m_zRangesStripeF[i]=new FloatRange(m_zRangesStripe[i],m_fScale);
        }
        
        layers=dis.readInt();
        layerIndexesip=new intPointer2[layers];
        layerIndexesfp=new intPointer2[layers];
        
        int stripe,seg;
        
        for(layer=0;layer<layers;layer++){
        stripes=dis.readInt();
        stripeIndexesip=new intPointer2(stripes);
        stripeIndexesfp=new intPointer2(stripes);
        for(stripe=0;stripe<stripes;stripe++){
            segs=dis.readInt();                
            segmentIndexesip=new intPointer(segs);
            segmentIndexesfp=new intPointer(segs);
            if(segs>0){
                for(seg=0;seg<segs;seg++){
                    segmentIndexesip.p[seg]=dis.readInt();
                    segmentIndexesfp.p[seg]=dis.readInt();
                }
            }
            stripeIndexesip.p2[stripe]=segmentIndexesip;
            stripeIndexesfp.p2[stripe]=segmentIndexesfp;
        }
        layerIndexesip[layer]=stripeIndexesip;
        layerIndexesfp[layer]=stripeIndexesfp;
    }  
    setOFRanges();
  }
    
    public int draw(int pixels[], float x0[], int of[], float delta, 
            int w, int h){
        int o1=of[0],o2=of[1],o3=of[2];
        if(!m_fRanges[o3].contains(x0[o3])) return -1;
        float fo1,o1Min,o1Max,fo2,o2Min,o2Max;
        int i,o3Int,o2Int0,o2Int;        
        int col,coli,colf,row,rowi,rowf,offset;
        int x[]=new int[3];
        float xf[]=new float[3];
        for(i=0;i<3;i++){
            x[i]=(int)(x0[i]/m_fScale);
            xf[i]=x0[i];
        }
        
        o3Int=m_Ranges[o3].GetIndex(x[o3]);
        if(o3Int<0) return -1;
        float fH=(h-1)*delta,fW=(w-1)*delta;
        FloatRange o1RangeF=m_ofRangesF[o3][0][o3Int];
        FloatRange o2RangeF=m_ofRangesF[o3][1][o3Int];
        FloatRange o1RangeF1=new FloatRange(x0[o1],(x0[o1]+fW));
        FloatRange o2RangeF1=new FloatRange(x0[o2],(x0[o2]+fH));
        if(!o1RangeF.overlap(o1RangeF1))return -1;
        if(!o2RangeF.overlap(o2RangeF1))return -1;
        o1Min=o1RangeF.getBiggerMin(o1RangeF1);
        o1Max=o1RangeF.getSmallerMax(o1RangeF1);
        o2Min=o2RangeF.getBiggerMin(o2RangeF1);
        o2Max=o2RangeF.getSmallerMax(o2RangeF1);
        FloatRange efr=new FloatRange();
        intRange o1ir=new intRange();
        if(delta<m_fScale){
            o1ir=new intRange(o1Min,o1Max,m_fScale);
            xf[o1]=o1Min;
            xf[o2]=o2Min;
            efr=EffectiveRange(xf,o1,o1ir);
            o1Min=efr.getMin();
            o1Max=efr.getMax();
        }
        rowi=(int)((o2Min-x0[o2])/delta);
        rowf=(int)((o2Max-x0[o2])/delta);
        o2Int0=(int)(o2Min/m_fScale);
        for(row=rowi;row<=rowf;row++){
            fo2=x0[o2]+row*delta;
            if(delta<m_fScale){
                o2Int=(int)(fo2/m_fScale);
                if(o2Int>o2Int0){
                    xf[o1]=o1Min;
                    xf[o2]=fo2;
                    efr=EffectiveRange(xf,o1,o1ir);
                    o1Min=efr.getBiggerMin(o1RangeF1);
                    o1Max=efr.getSmallerMax(o1RangeF1);
                    o2Int0=o2Int;                            
                }
            }
            coli=(int)((o1Min-x0[o1])/delta);
            colf=(int)((o1Max-x0[o1])/delta);
            offset=row*w;
            for(col=coli;col<=colf;col++){
                fo1=x0[o1]+col*delta;
                xf[o1]=fo1;
                xf[o2]=fo2;
                if(contains(xf[0],xf[1],xf[2])){
                    try{pixels[offset+col]+=getPixel();}
                    catch(ArrayIndexOutOfBoundsException e){
                        e=e;
                    }
                }
            }
       }
       return 1;
    }
    
    intRange EffectiveRange(float fx,float fy,float fz, int of, intRange ir){        
        int nMin=ir.getMin()-1;
        int nMax=ir.getMax()+1;
        int i,x[];
        x=new int[3];
        x[0]=(int)(fx/m_fScale);
        x[1]=(int)(fy/m_fScale);
        x[2]=(int)(fz/m_fScale);
        for(i=nMin;i<=nMax;i++){
            x[of]=i;
            if(contains(x[0],x[1],x[2])){
                break;
            }
        }
        nMin=i;
        for(i=nMax;i>=nMin;i--){
            x[of]=i;
            if(contains(x[0],x[1],x[2])){
                break;
            }           
        }
        nMax=i;
        return new intRange(nMin,nMax);
    }
    
    FloatRange EffectiveRange(float x0[], int of, intRange ir){        
        int nMin=ir.getMin()-1;
        int nMax=ir.getMax()+1;
        int i,x[];
        x=new int[3];
        for(i=0;i<3;i++){
            x[i]=(int)(x0[i]/m_fScale);
        }
        for(i=nMin;i<=nMax;i++){
            x[of]=i;
            if(contains(x[0],x[1],x[2])){
                break;
            }
        }
        nMin=i;
        for(i=nMax;i>=nMin;i--){
            x[of]=i;
            if(contains(x[0],x[1],x[2])){
                break;
            }           
        }
        nMax=i;
        return new FloatRange(nMin,nMax,m_fScale);
//        return new FloatRange(ir,m_fScale);
    }
    
    void setOFRanges(){        
        m_Ranges=new intRange[3];
        m_fRanges=new FloatRange[3];
        m_Ranges[0]=m_xRange;
        m_Ranges[1]=m_yRange;
        m_Ranges[2]=m_zRange;
        
        for(int i=0;i<3;i++){
            m_fRanges[i]=new FloatRange(m_Ranges[i],m_fScale);
        }
        
        m_ofRanges=new intRange[3][2][];
        m_ofRanges[0][0]=m_yRangesSegment;
        m_ofRanges[0][1]=m_zRangesSegment;
        m_ofRanges[1][0]=m_zRangesStripe;
        m_ofRanges[1][1]=m_xRangesStripe;
        m_ofRanges[2][0]=m_xRangesLayerp;
        m_ofRanges[2][1]=m_yRangesLayerp;
        
        m_ofRangesF=new FloatRange[3][2][];
        m_ofRangesF[0][0]=m_yRangesSegmentF;
        m_ofRangesF[0][1]=m_zRangesSegmentF;
        m_ofRangesF[1][0]=m_zRangesStripeF;
        m_ofRangesF[1][1]=m_xRangesStripeF;
        m_ofRangesF[2][0]=m_xRangesLayerF;
        m_ofRangesF[2][1]=m_yRangesLayerF;
    }
    
   
   public FloatRange getRangeF(int oi){
       return m_fRanges[oi];
   } 
   void setRanges(){
       
   }
   void setEnclosingBox(){
       float ranges[][]=new float[3][2];
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
   public boolean withinFrame(SpacialGrids sg){
       return true;
   }
    public int getID(){
        return StructureNode.informaticsId;
    }
    void setPixel(int pixel){
        StructureNode.red=0xff&(pixel>>16);
        StructureNode.green=0xff&(pixel>>8);
        StructureNode.blue=0xff&(pixel);
    }
    public void reduceIntensity(float ratio){
        StructureNode.red*=ratio;
        StructureNode.green*=ratio;
        StructureNode.blue*=ratio;
    }
    public void adjustRGB(int dr, int dg, int db){        
        StructureNode.red+=dr;
        if(StructureNode.red>255)StructureNode.red=255;
        if(StructureNode.red<0)StructureNode.red=0;
        StructureNode.green+=dg;
        if(StructureNode.green>255)StructureNode.green=255;
        if(StructureNode.green<0)StructureNode.green=0;
        StructureNode.blue+=db;
        if(StructureNode.blue>255)StructureNode.blue=255;
        if(StructureNode.blue<0)StructureNode.blue=0;
    }        
}