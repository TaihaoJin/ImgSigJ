/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import java.util.ArrayList;
import BrainSimulations.SpacialObject;
import utilities.ArrayofArrays.SpacialObjectArray;
import utilities.ArrayofArrays.IntArray;
import BrainSimulations.DataClasses.FloatRange;
import BrainSimulations.StructureFrame;
import BrainSimulations.SubstructureEssential;

/**
 *
 * @author Taihao
 */
public class SpacialGrids {
    IntArray objectIndexes[][][];
    IntArray substructureIndexes[][][];
    ArrayList <SpacialObject> spacialObjects;
    int nDim;
    float m_fScales[];
    float m_fMin[];
    FloatRange m_fRanges[];
    StructureFrame structureFrame;
    boolean bSubstructuresSet;
    
    public SpacialGrids(){
        nDim=20;
        bSubstructuresSet=false;
        m_fRanges=new FloatRange[3];
        for(int i=0;i<3;i++){
            m_fRanges[i]=new FloatRange();
        }
        m_fScales=new float[3];
        m_fMin=new float[3];
        objectIndexes=new IntArray [nDim][nDim][nDim];
        substructureIndexes=new IntArray [nDim][nDim][nDim];
        spacialObjects=new ArrayList <SpacialObject>();
        for(int i=0;i<nDim;i++){
            for(int j=0;j<nDim;j++){
                 for(int k=0;k<nDim;k++){
                    objectIndexes[i][j][k]=new IntArray();
                    substructureIndexes[i][j][k]=new IntArray();
                }
           }            
        }
    }
    public SpacialGrids(StructureFrame structureFrame0){
        this();
        structureFrame=structureFrame0;
        setScalesAndBoundary();
        registerSubstructures();
        bSubstructuresSet=true;
    }
    void setScalesAndBoundary(){
        int i0;
        for(int i=0;i<3;i++){
            m_fRanges[i].resetRange();
            m_fScales[i]=-1.f;
        }
        i0=0;
        
        for(int i=0;i<3;i++){
            m_fRanges[i]=structureFrame.getRangeF(i);
        }
        
        for(int i=0;i<3;i++){
            m_fScales[i]=m_fRanges[i].getExpand()/((float)nDim);
            m_fMin[i]=m_fRanges[i].getMin();
        }
    }
    public void registerSubstructures(){
        ArrayList <SubstructureEssential> 
                substructures=structureFrame.getSubstructures();
        int nSize=substructures.size();
        FloatRange fRanges[]=new FloatRange[3];
        int fMin[]=new int[3], fMax[]=new int[3];
        int i,j,k,l;
        for(i=0;i<nSize;i++){
            for(j=0;j<3;j++){
                fRanges[j]=substructures.get(i).getRangeF(j);
                fMin[j]=(int)((fRanges[j].getMin()-m_fMin[j])/m_fScales[j]);
                fMax[j]=(int)((fRanges[j].getMax()-m_fMin[j])/m_fScales[j]);
                if(fMin[j]<0)fMin[j]=0;
                if(fMax[j]>=nDim) fMax[j]=nDim-1;
            }
            for(j=fMin[0];j<=fMax[0];j++){
                for(k=fMin[1];k<=fMax[1];k++){
                    for(l=fMin[2];l<=fMax[2];l++){
                        try{substructureIndexes[j][k][l].m_intArray.add(i);}
                        catch (ArrayIndexOutOfBoundsException e){
                            e=e;
                        }
                    }
               }
           }                
        }
    }
    public void registerObjects(int index1, int index2){
        int nSize=spacialObjects.size();
        FloatRange fRanges[]=new FloatRange[3];
        int fMin[]=new int[3], fMax[]=new int[3];
        int i,j,k,l;
        for(i=index1;i<=index2;i++){
            for(j=0;j<3;j++){
                fRanges[j]=spacialObjects.get(i).getRangeF(j);
                fMin[j]=(int)((fRanges[j].getMin()-m_fMin[j])/m_fScales[j]);
                fMax[j]=(int)((fRanges[j].getMax()-m_fMin[j])/m_fScales[j]);
                if(fMin[j]<0)fMin[j]=0;
                if(fMax[j]>=nDim) fMax[j]=nDim-1;
            }
            for(j=fMin[0];j<=fMax[0];j++){
                for(k=fMin[1];k<=fMax[1];k++){
                    for(l=fMin[2];l<=fMax[2];l++){
                        try{objectIndexes[j][k][l].m_intArray.add(i);}
                        catch (ArrayIndexOutOfBoundsException e){
                            e=e;
                        }
                    }
               }
           }                
        }
    }
    int getPixel(float xf,float yf,float zf){
        int pixel=-1;
        ArrayList <SubstructureEssential> 
                substructures=structureFrame.getSubstructures();
        if(!m_fRanges[0].contains(xf)) return pixel;
        if(!m_fRanges[1].contains(yf)) return pixel;
        if(!m_fRanges[2].contains(zf)) return pixel;
        int x=(int)((xf-m_fMin[0])/m_fScales[0]);
        int y=(int)((yf-m_fMin[1])/m_fScales[1]);
        int z=(int)((zf-m_fMin[2])/m_fScales[2]);
        int nSize=substructureIndexes[x][y][z].m_intArray.size();
        int i,index;
        SpacialObject so;
        for(i=0;i<nSize;i++){
            index=substructureIndexes[x][y][z].m_intArray.get(i);
            so=substructures.get(index);
            pixel=so.getPixel(xf, yf, zf);
            if(pixel>0) return pixel;
        }
        return pixel;
    }
    public ArrayList <SpacialObject> getDrawingObjects(float x0[],int of[],
            int w,int h,float delta){
        ArrayList <SpacialObject> objects=new ArrayList <SpacialObject>();
        int x1[]=new int[3];
        int x2[]=new int[3];
        int i,j,k;
        for(i=0;i<3;i++){
            x1[i]=(int)((x0[i]-m_fMin[i])/m_fScales[i]);
        }
        ArrayList<Integer> ial;
        int size;
        int o1=of[0],o2=of[1],o3=of[2];
        if(x1[o3]<0||x1[o3]>=nDim) return objects;
        
        int xF=(int)((x0[o1]-m_fMin[o1]+w*delta)/m_fScales[o1]);
        if(xF<0) return objects;
        if(xF>=nDim) xF=nDim-1;
        int yF=(int)((x0[o2]-m_fMin[o2]+h*delta)/m_fScales[o2]);
        if(yF<0) return objects;
        if(yF>=nDim)yF=nDim-1;
        x2[o3]=x1[o3];
        int index;
        if(x1[o1]<0)x1[01]=0;
        if(x1[o2]<0)x1[02]=0;
        SpacialObject so;
        for(i=x1[o1];i<=xF;i++){
            x2[o1]=i;
            for(j=x1[o2];j<=yF;j++){
                x2[o2]=j;
                ial=objectIndexes[x2[0]][x2[1]][x2[2]].m_intArray;
                size=ial.size();
                for(k=0;k<size;k++){
                    index=ial.get(k);
                    so=spacialObjects.get(index);
                    if(!objects.contains(so)){
                        objects.add(so);
                    }
                }
            }            
        }
        return objects;
    }
    
    public ArrayList <SubstructureEssential> getDrawingSubstructures(
            float x0[],int of[],int w,int h,float delta){
        ArrayList <SubstructureEssential> 
                objects=new ArrayList <SubstructureEssential>();
        ArrayList <SubstructureEssential> 
                substructures=structureFrame.getSubstructures();
        int x1[]=new int[3];
        int x2[]=new int[3];
        int i,j,k;
        for(i=0;i<3;i++){
            x1[i]=(int)((x0[i]-m_fMin[i])/m_fScales[i]);
        }
        ArrayList<Integer> ial;
        int size;
        int o1=of[0],o2=of[1],o3=of[2];
        if(x1[o3]<0||x1[o3]>=nDim) return objects;
        
        int xF=(int)((x0[o1]-m_fMin[o1]+w*delta)/m_fScales[o1]);
        if(xF<0) return objects;
        if(xF>=nDim) xF=nDim-1;
        int yF=(int)((x0[o2]-m_fMin[o2]+h*delta)/m_fScales[o2]);
        if(yF<0) return objects;
        if(yF>=nDim)yF=nDim-1;
        x2[o3]=x1[o3];
        int index;
        if(x1[o1]<0)x1[01]=0;
        if(x1[o2]<0)x1[02]=0;
        SubstructureEssential so;
        for(i=x1[o1];i<=xF;i++){
            x2[o1]=i;
            for(j=x1[o2];j<=yF;j++){
                x2[o2]=j;
                ial=substructureIndexes[x2[0]][x2[1]][x2[2]].m_intArray;
                size=ial.size();
                for(k=0;k<size;k++){
                    index=ial.get(k);
                    so=substructures.get(index);
                    if(!objects.contains(so)){
                        objects.add(so);
                    }
                }
            }            
        }
        return objects;
    }
    public void addSpacialObjects(ArrayList<SpacialObject> objects){
        int index1=spacialObjects.size()-1;
        int offset=0;
        if(index1<0) {
            offset=index1;
            index1=0;
        }
        int size=objects.size();
        for(int i=0;i<size;i++){
            spacialObjects.add(objects.get(i));
        }
        registerObjects(index1,index1+size+offset);
    }
    public void addSpacialObject(SpacialObject sobject){
        int index=spacialObjects.size();
        spacialObjects.add(sobject);
        registerObjects(index,index);
    }
    public int contains(float x0[]){
        ArrayList <SubstructureEssential> ss=structureFrame.getSubstructures();
        int x[]=new int[3];
        for(int i=0;i<3;i++){
            x[i]=(int)((x0[i]-m_fMin[i])/m_fScales[i]);
            if(x[i]<0||x[i]>=nDim) return -1;
        }
        int id=-1;
        IntArray ial=substructureIndexes[x[0]][x[1]][x[2]];
        int size=ial.m_intArray.size();
        for(int i=0;i<size;i++){
            id=ss.get(ial.m_intArray.get(i)).contains(x0);
            if(id>0) return id;
        }
        return -1;
    }
}