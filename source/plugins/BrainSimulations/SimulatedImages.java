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
import ij.io.FileInfo;
import BrainSimulations.DataClasses.AtlasVoxelNode;
import BrainSimulations.DataClasses.BrainStructureNameNode;
import utilities.CustomDataTypes.intRange;
import ij.ImagePlus;
import ij.CompositeImage;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import ij.ImageStack;
import utilities.QuickFormatter;
import utilities.QuickSort_Sortable;
import BrainSimulations.SubstructureEssential;
import BrainSimulations.DataClasses.FloatRange;
import BrainSimulations.SpacialObject;
import BrainSimulations.SpacialGrids;
import BrainSimulations.SphereSimulator;
import BrainSimulations.StructureFrame;
import BrainSimulations.SpacialObjectGenerator;
/**
 *
 * @author Taihao
 */
public class SimulatedImages {
//    ArrayList <SubstructureEssential> substructures;
    StructureFrame structureFrame;
    int m_nImageDim;    
//    intRange m_xRange;
//    intRange m_yRange;
//    intRange m_zRange;
    SpacialGrids m_SpacialGrids;
//    ArrayList <SpacialObject> m_SpacialObjects;
    ArrayList<ImageStack> m_imsks;
    ArrayList<ImagePlus> m_impls;
    ImageStack m_imsk;
    ImagePlus m_impl;
    int m_nSections;
    int m_sectionIndex;
//    float center[];
//    FloatRange m_fRanges[];
    
    public SimulatedImages(){
        m_nImageDim=1000;
//        m_xRange=new intRange();
//        m_yRange=new intRange();
//        m_zRange=new intRange();
        
//        m_SpacialObjects=new ArrayList <SpacialObject>();
        m_nSections=0;
    }
    
    public SimulatedImages(StructureFrame structureFrame0){
        this();
        structureFrame=structureFrame0;
        m_SpacialGrids=new SpacialGrids (structureFrame);
        addSpacialObjects();
    }
    
    public float getScale(){
        return structureFrame.getScale();
    }
 
    public float[][] rotationMatrix(float phi, float psi, float theta){
        float rmatrix[][]=new float [3][3];
        
        return rmatrix;
    }
    public int[] getPixels(float x0, float y0, float z0, float length, 
            float phi, float psi, float theta){
        int nDim=m_nImageDim;
        float x,y,z;
        float rmatrix[][]=new float [3][3];
        rmatrix=rotationMatrix(phi,psi,theta);
        int nNumPixels=nDim*nDim;
        int pixels[]=new int[nNumPixels];
        float fdelta=length/nDim;
        int index=0;
        for(int i=0;i<nDim;i++){
            if(i%100==0){
               i=i; 
            }
            for(int j=0;j<nDim;j++){
                if(j%100==0){
                   i=i; 
                }
                z=z0+i*fdelta;
                y=y0+j*fdelta;
                x=x0;
                pixels[index]=pixelAt(x,y,z);
                index++;
            }
        }
        return pixels;
    }
    
    public int[] getPixels(float x0[], float length, int OrientationIndex[]){
        int nDim=m_nImageDim;
        int nNumPixels=nDim*nDim;
        int pixels[]=new int[nNumPixels];
        float fdelta=length/nDim;
        int index=0;
        int o1=OrientationIndex[0];
        int o2=OrientationIndex[1];
        int o3=OrientationIndex[2];
        float x[]=new float[3];
        x[o3]=x0[o3];
        for(int i=0;i<nDim;i++){
            for(int j=0;j<nDim;j++){
                x[o1]=x0[o1]+(float)i*fdelta;
                x[o2]=x0[o2]+(float)j*fdelta;
                pixels[index]=pixelAt(x[0],x[1],x[2]);
                index++;
            }
        }
        return pixels;
    }
    
    public int[] getPixels(float x0[], float length1, float length2, 
            float fdelta, int OrientationIndex[]){
        int nDim=m_nImageDim;
        int index=0;
        int o1=OrientationIndex[0];
        int o2=OrientationIndex[1];
        int o3=OrientationIndex[2];
        int nDim1=(int)(length1/fdelta);
        int nDim2=(int)(length2/fdelta);
        int pixels[]=new int[nDim1*nDim2];
        float x[]=new float[3];
        x[o3]=x0[o3];
        for(int i=0;i<nDim2;i++){
            for(int j=0;j<nDim1;j++){
                x[o1]=x0[o1]+(float)j*fdelta;
                x[o2]=x0[o2]+(float)i*fdelta;
                pixels[index]=pixelAt(x[0],x[1],x[2]);
                index++;
            }
        }
        return pixels;
    }

    public int[] drawPixels(float x0[], float length1, float length2, 
            float fdelta, int OrientationIndex[]){
        int nDim=m_nImageDim;
        int index=0;
        int nDim1=(int)(length1/fdelta)+1;
        int nDim2=(int)(length2/fdelta)+1;
        int pixels[]=new int[nDim1*nDim2];
        ArrayList <SpacialObject> 
                objects=new ArrayList<SpacialObject>();
        ArrayList <SubstructureEssential> 
                substructures=new ArrayList<SubstructureEssential>();
        objects=m_SpacialGrids.getDrawingObjects(x0,
                OrientationIndex,nDim1,nDim2,fdelta);
        substructures=m_SpacialGrids.getDrawingSubstructures(x0,
                OrientationIndex,nDim1,nDim2,fdelta);
        int size=substructures.size();
        for(int i=0;i<size;i++){
            substructures.get(i).draw(pixels, x0, 
                    OrientationIndex, fdelta, nDim1, nDim2);
        }
        size=objects.size();
        for(int i=0;i<size;i++){
            objects.get(i).draw(pixels, x0, OrientationIndex, 
                    fdelta, nDim1, nDim2);
        }
        return pixels;
    }

    public int pixelAt(float x0, float y0, float z0){
//      int pixel=(255<<24)|(255<<16)|(255<<8)|255;
        int pixel=0;
        int pixel0=(255<<16)|(255<<8)|255;
   //     if(pixel>0) return pixel;
        pixel=m_SpacialGrids.getPixel(x0, y0, z0);
        if(pixel<0) pixel=pixel0;
        return pixel;
    }
    
   public void buildSection(float x0[], float length, int OrientationIndex[]){
        int pixels[]=getPixels(x0, length, OrientationIndex);
        int nDim=m_nImageDim;
        String s=""+m_nImageDim;
        int nSlice=1;
        int FILL_WHITE=4;
        m_imsk.addSlice(s,pixels);
   } 
   public void buildSection(float x0[], float length1, float length2, 
           float fScale, int OrientationIndex[]){
        int pixels[]=getPixels(x0, length1, length2, fScale, OrientationIndex);
        int nDim=m_nImageDim;
        String s=""+m_nImageDim;
        int nSlice=1;
        int FILL_WHITE=4;//
        m_imsk.addSlice(s,pixels);
    }      
   public void drawSection(float x0[], float length1, float length2, 
           float fScale, int OrientationIndex[]){
        int pixels[]=drawPixels(x0, length1, length2, fScale, OrientationIndex);
        int nDim=m_nImageDim;
        String s=""+m_nImageDim;
        int nSlice=1;
        int FILL_WHITE=4;//
        m_imsk.addSlice(s,pixels);
    }      
    
   
    public void makeSeriesSections(int sectionIndex){ 
    //sectionIndex: 1 for coronal, 2 for sagital and 3 for horizontal sections.
        float origion[]=new float[3];
        float expanding[]=new float[3];
        origion[0]=(float)(6275.0-25.);
        origion[1]=(float)(1950.0-25.);
        origion[2]=(float)(1950.0-25.);
        
        expanding[0]=(float)(9250.0+25.-origion[0]);
        expanding[1]=(float)(6425.0+25.-origion[1]);
        expanding[2]=(float)(5225.0+25.-origion[2]);
        
        m_sectionIndex=sectionIndex;
        int OrientationIndex[]=new int[3];
                
        OrientationIndex[0]=0;
        OrientationIndex[1]=1;
        OrientationIndex[2]=2;
        
        switch (sectionIndex){
            case 1:
                OrientationIndex[0]=1;
                OrientationIndex[1]=2;
                OrientationIndex[2]=0;
                break;
            case 3:
                OrientationIndex[0]=2;
                OrientationIndex[1]=0;
                OrientationIndex[2]=1;
                break;
            default:
                break;
        }
        int o1=OrientationIndex[0];
        int o2=OrientationIndex[1];
        int o3=OrientationIndex[2];
        intRange intRanges[]=new intRange[3];
        int sections;;
        float fScale=getScale();
        float length1=structureFrame.getRangeF(o1).getExpand();
        float length2=structureFrame.getRangeF(o2).getExpand();
        float length3=structureFrame.getRangeF(o3).getExpand();
        float x[]=new float[3];
        x[o3]=structureFrame.getRangeF(o3).getMin();
        float length=length2;
        if(length1>length) length=length1;
        x[o1]=structureFrame.getRangeF(o1).getMin()-0.0f*length;
        x[o2]=structureFrame.getRangeF(o2).getMin()-0.0f*length;
//        length*=1.2f;
//        length*=0.8;
        float x0=origion[o3];
        sections=40;
//        float dx=expanding[o3]/(float)(sections);
        float dx=25.f;
        sections=(int)(expanding[o3]/dx)+1;
        fScale=length/(m_nImageDim-1);
//        m_imsk=new ImageStack(m_nImageDim,m_nImageDim,null);
        int nDim1=(int)(length1/fScale)+1;
        int nDim2=(int)(length2/fScale)+1;
        m_imsk=new ImageStack(nDim1,nDim2,null);
        for(int i=0;i<sections;i++){
            x[o3]=x0+i*dx;
            drawSection(x, length1, length2, fScale, OrientationIndex);
            IJ.showStatus("drawing " + sectionTitle() + (i+1) + "/" + sections);
            IJ.showProgress((double)(i+1)/sections);
        }
        m_impl=new ImagePlus("Simulated "+sectionTitle(),m_imsk);
        x[o3]=x0;
        FileInfo fi=buildFileInfo(x,sectionIndex(OrientationIndex),sections,fScale,dx);
        m_impl.setFileInfo(fi);
        m_impl.show();
    }
    
    public void makeSeriesSections_EntireBrain(int sectionIndex){ 
    //sectionIndex: 1 for coronal, 2 for sagital and 3 for horizontal sections.
        m_sectionIndex=sectionIndex;
        int OrientationIndex[]=new int[3];
                
        OrientationIndex[0]=0;
        OrientationIndex[1]=1;
        OrientationIndex[2]=2;
        
        switch (sectionIndex){
            case 1:
                OrientationIndex[0]=1;
                OrientationIndex[1]=2;
                OrientationIndex[2]=0;
                break;
            case 3:
                OrientationIndex[0]=2;
                OrientationIndex[1]=0;
                OrientationIndex[2]=1;
                break;
            default:
                break;
        }
        int o1=OrientationIndex[0];
        int o2=OrientationIndex[1];
        int o3=OrientationIndex[2];
        intRange intRanges[]=new intRange[3];
        int sections;;
        float fScale=getScale();
        float length1=structureFrame.getRangeF(o1).getExpand();
        float length2=structureFrame.getRangeF(o2).getExpand();
        float length3=structureFrame.getRangeF(o3).getExpand();
        float x[]=new float[3];
        x[o3]=structureFrame.getRangeF(o3).getMin();
        float length=length2;
        if(length1>length) length=length1;
        x[o1]=structureFrame.getRangeF(o1).getMin()-0.0f*length;
        x[o2]=structureFrame.getRangeF(o2).getMin()-0.0f*length;
//        length*=1.2f;
//        length*=0.8;
        float x0=x[o3];
        sections=40;
        float dx=length3/(float)(sections);
        fScale=length/(m_nImageDim-1);
//        m_imsk=new ImageStack(m_nImageDim,m_nImageDim,null);
        int nDim1=(int)(length1/fScale)+1;
        int nDim2=(int)(length2/fScale)+1;
        m_imsk=new ImageStack(nDim1,nDim2,null);
        for(int i=0;i<sections;i++){
            x[o3]=x0+i*dx;
            drawSection(x, length1, length2, fScale, OrientationIndex);
            IJ.showStatus("drawing " + sectionTitle() + (i+1) + "/" + sections);
            IJ.showProgress((double)(i+1)/sections);
        }
        m_impl=new ImagePlus("Simulated "+sectionTitle(),m_imsk);
        x[o3]=x0;
        FileInfo fi=buildFileInfo(x,sectionIndex(OrientationIndex),sections,fScale,dx);
        m_impl.setFileInfo(fi);
        m_impl.show();
    }
    
    FileInfo buildFileInfo(float x0[],int oi, int sections,float delta, float dx){
        FileInfo fi=new FileInfo();
        fi.fileFormat=fi.TIFF;
        fi.fileType=fi.RGB;
        fi.fileName="section"+oi+".sbi";
        fi.directory="C:\\Taihao\\Lab UCSF\\Imaging\\images\\simulated brain images\\";
        fi.url="";
        fi.width=m_impl.getWidth();
        fi.height=m_impl.getHeight();
        fi.nImages=sections;
        fi.offset=0;
        fi.gapBetweenImages=0;
        fi.whiteIsZero=false;
        fi.intelByteOrder=false;
        fi.compression=0;
        fi.stripOffsets=null;
        fi.stripLengths=null;
	fi.lutSize=0;
	fi.reds=null;
	fi.greens=null;
	fi.blues=null;
	fi.pixels=null;	
	fi.debugInfo="";
        fi.sliceLabels=m_imsk.getSliceLabels();
	fi.info="now i am testing";
	fi.inputStream=null;	
	fi.pixelWidth=delta;
	fi.pixelHeight=delta;
	fi.pixelDepth=dx;
	fi.unit="micrometer";
	fi.calibrationFunction=0;
        fi.coefficients=new double[3];
        for(int i=0;i<3;i++)
        {
            fi.coefficients[i]=(double)x0[i];
        }
	fi.valueUnit="";
	fi.frameInterval=dx;
        String desc="";
        desc+=" SectionIndex: "+oi+" ";
        Float FC=new Float(0.f);
        desc+=" Coefficients of the origion of the first section(x,y,z): ";
        for(int i=0;i<3;i++){
            desc+=" "+FC.toString(x0[i])+" ";
        }
        desc+=" PixelWidth "+FC.toHexString(delta)+" micrometer, ";
        desc+=" PixelHeight "+FC.toHexString(delta)+" micrometer, ";
        desc+=" PixelDepth "+FC.toHexString(dx)+" micrometer. ";
	fi.description=desc;
	// Use <i>longOffset</i> instead of <i>offset</i> 
        //when offset>2147483647.
	fi.longOffset=0;
	// Extra metadata to be stored in the TIFF header
	fi.metaDataTypes=null; // must be < 0xffffff
	fi.metaData=null;
	fi.displayRanges=null;
	fi.channelLuts=null;
	fi.samplesPerPixel=1;
        
        return fi;
    }
    
    public int sectionIndex(int oi[]){
        switch (oi[2]){
            case 0:
                return 1;//coronal section;
            case 2:
                return 2;//sagital section;
            case 1: 
                return 3;//horizontal section;
            default:
                return 1;
        }    
     }
    
    String sectionTitle(){
        String title="Coronal Section";
        switch (m_sectionIndex){
            case 1:
                title="Coronal Section";
                break;
            case 2:
                title="Sagital Section";
                break;
            case 3: 
                title="Horizontal Section";
                break;
            default:
                title="Horizontal Section";
                break;    
        }
        return title;
    }
    void subtractPixels(int pixels1[],int pixels2[], int diff[]){
        int len=pixels1.length;
        for(int i=0;i<len;i++){
            diff[i]=pixels1[i]-pixels2[i];
        }
    }
   
    public void compareSeriesSections(int sectionIndex){ 
    //sectionIndex: 1 for coronal, 2 for sagital and 3 for horizontal sections.
        m_sectionIndex=sectionIndex;
        int OrientationIndex[]=new int[3];
                
        OrientationIndex[0]=0;
        OrientationIndex[1]=1;
        OrientationIndex[2]=2;
        
        switch (sectionIndex){
            case 1:
                OrientationIndex[0]=1;
                OrientationIndex[1]=2;
                OrientationIndex[2]=0;
                break;
            case 3:
                OrientationIndex[0]=2;
                OrientationIndex[1]=0;
                OrientationIndex[2]=1;
                break;
            default:
                break;
        }
        int o1=OrientationIndex[0];
        int o2=OrientationIndex[1];
        int o3=OrientationIndex[2];
        intRange intRanges[]=new intRange[3];
        int sections;;
        sections=40;
        float fScale=getScale();
        float length1=structureFrame.getRangeF(o1).getExpand();
        float length2=structureFrame.getRangeF(o2).getExpand();
        float length3=structureFrame.getRangeF(o3).getExpand();
        float x[]=new float[3];
        x[o3]=structureFrame.getRangeF(o3).getMin();
        float length=length2;
        if(length1>length) length=length1;
        x[o1]=structureFrame.getRangeF(o1).getMin()-0.0f*length;
        x[o2]=structureFrame.getRangeF(o2).getMin()-0.0f*length;
        float dx=length3/(float)(10*sections);
//        length*=1.2f;
//        length*=0.8;
        float x0=x[o3];
        fScale=length/m_nImageDim;
//        m_imsk=new ImageStack(m_nImageDim,m_nImageDim,null);
        int nDim1=(int)(length1/fScale);
        int nDim2=(int)(length2/fScale);
        m_imsk=new ImageStack(nDim1,nDim2,null);
        for(int i=0;i<sections;i++){
            x[o3]=x0+(300+i)*dx;
            buildSection(x, length1, length2, fScale, OrientationIndex);
        }
        ImageStack stackBuild=m_imsk;
        m_imsk=new ImageStack(nDim1,nDim2,null);
        for(int i=0;i<sections;i++){
            x[o3]=x0+i*dx;
            drawSection(x, length1, length2, fScale, OrientationIndex);
            IJ.showStatus("drawing sections " + (i+1) + "/" + sections);
            IJ.showProgress((double)(i+1)/sections);
        }
        ImageStack stackDrawn=m_imsk;
        compareStacks(stackBuild,stackDrawn,"BuiltSection","DrawnSection");
    }    
    
    void compareStacks(ImageStack stack1, ImageStack stack2, 
            String title1, String title2){
        int w=stack1.getWidth();
        int h=stack1.getHeight();
        int slices=stack1.getSize();
        ImageStack stackd=new ImageStack(w,h,null);
        int i;
        for(i=0;i<slices;i++){
            int pixels1[]=(int[])stack1.getPixels(i+1);
            //slice number of ImageStack counting from 1.
            int pixels2[]=(int[])stack2.getPixels(i+1);
            int diff[]=new int[w*h];
            subtractPixels(pixels1,pixels2,diff);
            stackd.addSlice("",diff);
        }
        ImagePlus impl1=new ImagePlus(title1,stack1);
        ImagePlus impl2=new ImagePlus(title2,stack2);
        ImagePlus impld=new ImagePlus("Difference",stackd);
        impl1.show();
        impl2.show();
        impld.show();
    }
    void addSpacialObjects(){
        SpacialObjectGenerator sog=new SpacialObjectGenerator(structureFrame);
        sog.setGrids(m_SpacialGrids);
        ArrayList <SpacialObject> spacialObjects=sog.getEllipsoids(100);
        m_SpacialGrids.addSpacialObjects(spacialObjects);
    }
    void saveImages(){
        RandomAccessFile raf;
    }            
}

