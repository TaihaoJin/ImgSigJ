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
import BrainSimulations.DataClasses.AtlasVoxelNode;
import BrainSimulations.DataClasses.BrainStructureNameNode;
import utilities.CustomDataTypes.intRange;
import ij.ImagePlus;
import ij.CompositeImage;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import utilities.QuickFormatter;
import utilities.QuickSort_Sortable;
/**
 *
 * @author Taihao
 */
public class AtlasAnnotation {   
    Runtime r = Runtime.getRuntime();
    long fm=0;
    intRange m_xRange;
    intRange m_yRange;
    intRange m_zRange;
    float m_fScale;
    boolean m_bScaleSet;
    boolean m_bRangeSet;
    boolean m_bVoxelsSorted;
    public static String newline = System.getProperty("line.separator");
    DataInputStream m_dis;
    DataOutputStream m_dos;
    int m_numSubstructures;
    AtlasAnnotation(){
        substructures = new ArrayList<SubstructureSorted>();
        m_nMaxID=0;
        m_xRange=new intRange();
        m_yRange=new intRange();
        m_zRange=new intRange();
        m_bRangeSet=false;
        m_bScaleSet=false;
        m_nImageDim=1000;
        m_bVoxelsSorted=false;
        m_nim=new NewImage();
    }
    
    ArrayList <SubstructureSorted> substructures;
    String m_sComment;
    String m_sBSHeading;
    String m_sAnnotationFileName;
    String m_sBrainStructureFileName;
    String m_sDim;
    int m_nxDim, m_nyDim, m_nzDim;
    int m_nMaxID;
    int m_nImageDim;
   
    
//    protected ArrayList <AtlasVoxelNode> m_vAtlasVoxels = new ArrayList <AtlasVoxelNode> ();
    protected ArrayList <AtlasVoxelNode> m_vAtlasVoxels = new ArrayList ();
    protected ArrayList <BrainStructureNameNode> m_vBrainStructureNameNodes=new ArrayList();
    
    public void ReadAtlasVoxels(String dir, String fileName)throws IOException{
        m_vAtlasVoxels.clear();
        File file = new File(dir, fileName);                
        m_sAnnotationFileName=fileName;        
        FileReader f=new FileReader(file);
        BufferedReader br=new BufferedReader(f);
        
        String s;
        s=br.readLine();
        m_sComment=s;
        s=br.readLine();
        m_sDim=s;
        StringTokenizer st0=new StringTokenizer(s,":,",false);
        String sTemp=st0.nextToken();
        
        sTemp=st0.nextToken();
        Integer intw=new Integer(sTemp);
        m_nxDim=intw.intValue();

        sTemp=st0.nextToken();
        m_nyDim=intw.valueOf(sTemp);

        sTemp=st0.nextToken();
        m_nzDim=intw.valueOf(sTemp);
        
        int n=0;
        int nNumVoxels=0;
        
//        QuickFormatter qfm=new QuickFormatter(dir+"test.txt");
//        Formatter fmtest=qfm.getFormatter();
        
        while((s=br.readLine())!=null)
        {
            nNumVoxels++;
            StringTokenizer st=new StringTokenizer(s,":,",false);
            while(st.hasMoreTokens())
            {
               AtlasVoxelNode av0=new AtlasVoxelNode();
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.x=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.y=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.z=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.id=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
//               substructures.get(av0.id).addVoxl(av0.x, av0.y, av0.z);
               m_vAtlasVoxels.add(av0); 
            }
        }
        br.close();
        f.close(); 
//        fmtest.close();

    }
    public void ReadAtlasVoxels ()throws IOException 
    {
        String title="Read a Atlas Annotation Voxel File";
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\";
//        String path="";//The path of the file to open. When specified, the OpenDialog will not open and use the path unchanged.
	String fileName = "";
        OpenDialog od = new OpenDialog(title , dir, fileName);
	fileName = od.getFileName();
        dir=od.getDirectory();
        File file = new File(dir, fileName);                
        od.setDefaultDirectory(dir);
        m_sAnnotationFileName=fileName;
        
        FileReader f=new FileReader(file);
        BufferedReader br=new BufferedReader(f);
        String s;
        s=br.readLine();
        m_sComment=s;
        s=br.readLine();
        StringTokenizer st0=new StringTokenizer(s,":,",false);
        String sTemp=st0.nextToken();
        
        sTemp=st0.nextToken();
        Integer intw=new Integer(sTemp);
        m_nxDim=intw.intValue();

        sTemp=st0.nextToken();
        m_nyDim=intw.valueOf(sTemp);

        sTemp=st0.nextToken();
        m_nzDim=intw.valueOf(sTemp);
        
        int n=0;
        int nNumVoxels=0;
        
        QuickFormatter qfm=new QuickFormatter(dir+"test.txt");
        Formatter fmtest=qfm.getFormatter();
        AtlasVoxelNode av0=new AtlasVoxelNode();
        while((s=br.readLine())!=null)
        {
            nNumVoxels++;
            StringTokenizer st=new StringTokenizer(s,":,",false);
            while(st.hasMoreTokens())
            {
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.x=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.y=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.z=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.id=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
//               m_vAtlasVoxels.add(av0); 
               if(av0.id==2){
                   nNumVoxels=nNumVoxels;
               }
               if(av0.id==0){
                   nNumVoxels=nNumVoxels;
               }
            }
        }
        br.close();
        f.close();  
        fmtest.close();
    }    

    public void ReadAtlasVoxels_build (String dir, String fileName)throws IOException 
    {
        File file = new File(dir, fileName);                
        m_sAnnotationFileName=fileName;
        
        FileReader f=new FileReader(file);
        BufferedReader br=new BufferedReader(f);
        String s;
        s=br.readLine();
        m_sComment=s;
        setComments();
        s=br.readLine();
        StringTokenizer st0=new StringTokenizer(s,":,",false);
        String sTemp=st0.nextToken();
        
        if(substructures.isEmpty()) buildSubstructures();
        
        sTemp=st0.nextToken();
        Integer intw=new Integer(sTemp);
        m_nxDim=intw.intValue();

        sTemp=st0.nextToken();
        m_nyDim=intw.valueOf(sTemp);

        sTemp=st0.nextToken();
        m_nzDim=intw.valueOf(sTemp);
        
        int n=0;
        int nNumVoxels=0;
            
//        QuickFormatter qfm=new QuickFormatter(dir+"test.txt");
 //       Formatter fmtest=qfm.getFormatter();
        int informaticID=0;
        
        while((s=br.readLine())!=null)
        {
            nNumVoxels++;
            StringTokenizer st=new StringTokenizer(s,":,",false);
            while(st.hasMoreTokens())
            {
               AtlasVoxelNode av0=new AtlasVoxelNode();
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.x=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.y=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.z=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               if(st.hasMoreTokens()){
                   sTemp=st.nextToken();
                   av0.id=intw.valueOf(sTemp);}
               else System.out.print(s+" no more tokens on the line");
               substructures.get(av0.id).addVoxl(av0.x, av0.y, av0.z);
               informaticID=av0.id;
            }
         }
        br.close();
        substructures.get(informaticID).closeSubstructure();
//        substructures.get(informaticID).contructTopology();
//        substructures.get(informaticID).buildRegions();
        substructures.get(informaticID).SetComments(m_sComment);
        substructures.get(informaticID).ComputeScale();
        m_fScale=substructures.get(informaticID).getScale();
        m_bScaleSet=true;
        
        QuickFormatter qfm=new QuickFormatter(dir+"Output_Essential\\"+fileName.substring(0,fileName.length()-4)+"_output.txt");
//        substructures.get(informaticID).writeSubstructure(qfm.getFormatter());
        substructures.get(informaticID).exportSubstructure_Essential(qfm);
        substructures.get(informaticID).exportSubstructure_Essential(m_dos);
        qfm.getFormatter().close();
        f.close(); 
//        fmtest.close();
    }    

    public void writeAtlasVoxels () throws FileNotFoundException{
        String name=m_sAnnotationFileName;
        String type="sva";
        String extension="sva";
                
   	SaveDialog sd = new SaveDialog("Save as "+type, name, extension);
	name = sd.getFileName();
	String directory = sd.getDirectory();
	String path = directory+name+"."+extension;
        File file=new File(path);
        
//        FileWriter f= new FileWriter(file);"
        Formatter fm= new Formatter(file);
        fm.format( "%s%s",m_sComment,newline);
        fm.flush();
        fm.format("Dimension%d,%d,%d%s", m_nxDim,m_nyDim,m_nzDim,newline);
        int nSize=m_vAtlasVoxels.size(); 
        fm.flush();
        for(int i=0;i<nSize;i++)
        {
            AtlasVoxelNode av0=new BrainSimulations.DataClasses.AtlasVoxelNode();
            av0=m_vAtlasVoxels.get(i);
            fm.format("%d,%d,%d,%d%s", av0.x,av0.y,av0.z,av0.id,newline);
            fm.flush();
        }
        
        fm.close();
    }
    
    public void setComments(){               
        int nID;
        for(int i=0;i<m_vBrainStructureNameNodes.size();i++){
            nID=m_vBrainStructureNameNodes.get(i).informaticsId;
            substructures.get(nID).SetComments(m_sComment);
            substructures.get(nID).ComputeScale();
            substructures.get(nID).setVoxelsSorted(m_bVoxelsSorted);
        }                
    }
    
    public void ReadBrainStructureNames() throws IOException {
        String title="Read a Brain Structure Name File";
//        String path="";
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\";
//        String path="";//The path of the file to open. When specified, the OpenDialog will not open and use the path unchanged.
	String fileName = "";
        OpenDialog od = new OpenDialog(title ,dir, fileName);
	fileName = od.getFileName();
        File file = new File(od.getDirectory(), fileName);                
        FileReader f = new FileReader(file);
        
        m_sBrainStructureFileName=fileName;
        
        BufferedReader br=new BufferedReader(f);
        String s;
        s=br.readLine();
        m_sBSHeading=s;
        Integer InTemp=new Integer("19");
        int nNumTokens=0;
        while((s=br.readLine())!=null)
        {
            StringTokenizer st0=new StringTokenizer(s,",",false);
            nNumTokens=st0.countTokens();
            BrainStructureNameNode av0=new BrainStructureNameNode();
            av0.StructureName=st0.nextToken();
            av0.Abbreviation=st0.nextToken();
            if(nNumTokens==8)
                av0.ParentStruct=st0.nextToken();
            else
                av0.ParentStruct="";
            av0.red=InTemp.valueOf(st0.nextToken());
            av0.green=InTemp.valueOf(st0.nextToken());
            av0.blue=InTemp.valueOf(st0.nextToken());
            av0.informaticsId=InTemp.valueOf(st0.nextToken());
            av0.StructureId=InTemp.valueOf(st0.nextToken());            
            m_vBrainStructureNameNodes.add(av0);
            if(av0.informaticsId>m_nMaxID) m_nMaxID=av0.informaticsId;
        }
    }
    
    public void buildAnnotation(){
        buildSubstructures();
        AtlasVoxelNode aVoxel;
        int nID;
        for(int i=0;i<m_vAtlasVoxels.size();i++){
            aVoxel=m_vAtlasVoxels.get(i);
            nID=aVoxel.id;
            substructures.get(nID).addVoxl(aVoxel.x, aVoxel.y, aVoxel.z);
        }        
    }
    
    public void buildAnnotation_Sorted() throws FileNotFoundException,IOException{
        m_bVoxelsSorted=true;
        buildSubstructures();
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\Separated\\Sorted\\25\\";
        File f=new File(dir);
        String[] list=f.list();
        int length=list.length;
        
        int num=0;
        m_numSubstructures=0;
        FileOutputStream fos=new FileOutputStream(dir+"EssentialSubstructures.dat");
        m_dos=new DataOutputStream(fos);
        for(int i=0;i<length;i++){
            num++;
            File f1=new File(dir+list[i]);
            if(f1.isDirectory()) continue;
            m_numSubstructures++;
        }
        m_dos.writeInt(m_numSubstructures);
        for(int i=0;i<length;i++){
            try{
                ReadAtlasVoxels_build(dir,list[i]);
            }catch (IOException e){
                System.out.println(e);
            }
            m_dos.flush();
            r.gc();
        }
        
        m_bVoxelsSorted=true;
        m_dos.close();
        fos.close();
    }
    
    public void buildSubstructures(){
        
        if(!substructures.isEmpty()) substructures.clear();
        for(int i=0;i<=m_nMaxID;i++){
            SubstructureSorted aNode=new SubstructureSorted();
            substructures.add(aNode);
        }
        
        int nID;
        for(int i=0;i<m_vBrainStructureNameNodes.size();i++){
            nID=m_vBrainStructureNameNodes.get(i).informaticsId;
            substructures.get(nID).setSubstructureName(m_vBrainStructureNameNodes.get(i));
//            substructures.get(nID).SetComments(m_sComment);
//            substructures.get(nID).ComputeScale();
//            substructures.get(nID).setVoxelsSorted(m_bVoxelsSorted);
        }                
    }

    public void writeBrainStructureNames()throws FileNotFoundException  {
        String name=m_sBrainStructureFileName;
        String type="csv";
        String extension="csv";
                
   	SaveDialog sd = new SaveDialog("Save as "+type, name, extension);
	name = sd.getFileName();
	String directory = sd.getDirectory();
	String path = directory+name+"."+extension;
        File file=new File(path);
        
        Formatter fm= new Formatter(file);
        fm.format( "%s%s",m_sBSHeading,newline);
        int nSize=m_vBrainStructureNameNodes.size();
        fm.flush();
        
        for(int i=0;i<nSize;i++)
        {
            fm.format("%s,",m_vBrainStructureNameNodes.get(i).StructureName);
            fm.format("%s,",m_vBrainStructureNameNodes.get(i).Abbreviation);
            fm.format("%s,",m_vBrainStructureNameNodes.get(i).ParentStruct);
            fm.format("%d,",m_vBrainStructureNameNodes.get(i).red);
            fm.format("%d,",m_vBrainStructureNameNodes.get(i).green);
            fm.format("%d,",m_vBrainStructureNameNodes.get(i).blue);
            fm.format("%d,",m_vBrainStructureNameNodes.get(i).informaticsId);
            fm.format("%d%s",m_vBrainStructureNameNodes.get(i).StructureId,newline);
       }
        fm.flush();
        fm.close();
    }

    
    public float[][] rotationMatrix(float phi, float psi, float theta){
        float rmatrix[][]=new float [3][3];
        
        return rmatrix;
    }
    public int[] getPixels(float x0, float y0, float z0, float length, float phi, float psi, float theta){
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
    
    public int pixelAt(float x0, float y0, float z0){
//      int pixel=(255<<24)|(255<<16)|(255<<8)|255;
        int pixel=0;
        pixel=(255<<16)|(255<<8)|255;
   //     if(pixel>0) return pixel;
        int nSize=substructures.size();
        for(int i=0;i<nSize;i++){
             if(substructures.get(i).contains(x0, y0, z0)){
                 return substructures.get(i).getPixel();
             }
        }
        return pixel;
    }
    public int pixelAt(int x0, int y0, int z0){
//      int pixel=(255<<24)|(255<<16)|(255<<8)|255;
        int pixel=0;
        pixel=(255<<16)|(255<<8)|255;
   //     if(pixel>0) return pixel;
        int nSize=substructures.size();
        for(int i=28;i<29;i++){
             if(substructures.get(i).contains(x0, y0, z0)!=null){
                 return substructures.get(i).getPixel();
             }
        }
        return pixel;
    }
    
        NewImage m_nim;
    public void displaySection(float x0, float y0, float z0, float length, float phi, float psi, float theta){
        int pixels[]=getPixels(x0, y0, z0, length, phi, psi, theta);
        int nDim=m_nImageDim;
        int nSlice=1;
        int FILL_WHITE=4;
   
        ImagePlus impl=m_nim.createRGBImage("test", nDim,nDim, nSlice, FILL_WHITE);
        ImageProcessor impr=impl.getProcessor();
        impr.setPixels(pixels);
        impl.show();
    }
    
    public void setRanges(){
        m_xRange.resetRange();
        m_yRange.resetRange();
        m_zRange.resetRange();
        int nSize=substructures.size();
        for(int i=0;i<nSize;i++){
             m_xRange.mergeRanges(substructures.get(i).getXRange()); 
             m_yRange.mergeRanges(substructures.get(i).getYRange()); 
             m_zRange.mergeRanges(substructures.get(i).getZRange()); 
        } 
        if(nSize>0) m_bRangeSet=true;
    }
    
    public void setScale(){
       int nSize=substructures.size();
       m_fScale=-1.f;
       int index=0;
       while(index<nSize&&m_fScale<0.f) {
           m_fScale=substructures.get(index).getScale();
           index++;
       }
    }
    
    public intRange getXRange(){
        if(!m_bRangeSet) setRanges();
        intRange ir=new intRange();
        ir.mergeRanges(m_xRange);
        return ir;
    }
    
    public intRange getYRange(){
        if(!m_bRangeSet) setRanges();
        intRange ir=new intRange();
        ir.mergeRanges(m_yRange);
        return ir;
    }
    
    public intRange getZRange(){
        if(!m_bRangeSet) setRanges();
        intRange ir=new intRange();
        ir.mergeRanges(m_zRange);
        return ir;
    }  
    
    public float getScale(){
        if(!m_bScaleSet) setScale();
        return m_fScale;
    }
    
    boolean withinRange(int x, int y, int z){
        if(m_xRange.contains(x)&&m_yRange.contains(y)&&m_zRange.contains(z))
            return true;
        else
            return false;
    }
    
    public void separateVoxels()throws IOException{
    
        String title="Read a Atlas Annotation Voxel File";
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\";
//        String path="";//The path of the file to open. When specified, the OpenDialog will not open and use the path unchanged.
	String fileName = "";
        OpenDialog od = new OpenDialog(title , dir, fileName);
	fileName = od.getFileName();
        dir=od.getDirectory();
        od.setDefaultDirectory(dir);
        String m_sAnnotationFileName=fileName;
        
        
        File file = new File(dir, fileName);                
        try{
            FileReader f=new FileReader(file);
            BufferedReader br=new BufferedReader(f);

            String s;
            s=br.readLine();
            String m_sComment=s;
            s=br.readLine();
            String sDim=new String(s);


            StringTokenizer st0=new StringTokenizer(s,":,",false);
            String sTemp=st0.nextToken();
            sTemp=st0.nextToken();
            Integer intw=new Integer(sTemp);
            int m_nxDim=intw.intValue();

            sTemp=st0.nextToken();
            int m_nyDim=intw.valueOf(sTemp);

            sTemp=st0.nextToken();
            int m_nzDim=intw.valueOf(sTemp);

            int n=0;
            int nNumVoxels=0;

            ArrayList <QuickFormatter> aqfm=new ArrayList <QuickFormatter>();
            int aqSize=aqfm.size();

            AtlasVoxelNode av0=new AtlasVoxelNode();
            StringTokenizer st;
            fm=r.freeMemory()/(1024*1024);
            while((s=br.readLine())!=null)
            {
                nNumVoxels++;
                st=new StringTokenizer(s,":,",false);
                while(st.hasMoreTokens())
                {
                   if(st.hasMoreTokens()){
                       sTemp=st.nextToken();
                       av0.x=intw.valueOf(sTemp);}
                   else System.out.print(s+" no more tokens on the line");
                   if(st.hasMoreTokens()){
                       sTemp=st.nextToken();
                       av0.y=intw.valueOf(sTemp);}
                   else System.out.print(s+" no more tokens on the line");
                   if(st.hasMoreTokens()){
                       sTemp=st.nextToken();
                       av0.z=intw.valueOf(sTemp);}
                   else System.out.print(s+" no more tokens on the line");
                   if(st.hasMoreTokens()){
                       sTemp=st.nextToken();
                       av0.id=intw.valueOf(sTemp);}
                   else System.out.print(s+" no more tokens on the line");
                   if(av0.id==0){
                       nNumVoxels=nNumVoxels;
                   }
                   if(av0.x==488&&av0.y==240&&av0.z==208){
                        nNumVoxels=nNumVoxels;
                   }
     //              substructures.get(av0.id).addVoxl(av0.x, av0.y, av0.z);
    //               m_vAtlasVoxels.add(av0); 
                   if(av0.id>aqSize) aqSize=fillFormatterArray(aqfm,av0.id);
                   if((aqfm.get(av0.id))==null){
                       int length=fileName.length();
                       aqfm.add(av0.id,new QuickFormatter(dir+fileName.substring(0,length-4)+"_"+av0.id+".txt"));
                       aqfm.remove(av0.id+1);
                        aqfm.get(av0.id).getFormatter().format("%s%s%s%s",m_sComment,newline,sDim,newline);
                   }
                   aqfm.get(av0.id).getFormatter().format("%d,%d,%d,%d%s", av0.x, av0.y, av0.z, av0.id,newline);
                   aqfm.get(av0.id).getFormatter().flush();
                }
                if((nNumVoxels%4000000)==0){
                    fm=r.freeMemory()/(1024*1024);
                    r.gc();
                    fm=r.freeMemory()/(1024*1024);
                }
            }
            closeFormatters(aqfm);
            br.close();
            f.close(); 
        }catch (FileNotFoundException e){
        System.out.println(dir+fileName+"  file not found");
    }

    }
    public int fillFormatterArray(ArrayList <QuickFormatter> aqfm, int index){
        int aqSize=aqfm.size();
        for(int i=aqSize;i<=index;i++){
            QuickFormatter a=null;
            aqfm.add(a);
        }
        return aqSize;
    }
    
    public void closeFormatters(ArrayList <QuickFormatter> aqfm){
        int nSize=aqfm.size();
        for(int i=0;i<nSize;i++){
            if(aqfm.get(i)!=null){
                aqfm.get(i).getFormatter().close();
            }
        }
    }
    
    public void SortVoxels(){
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\Separated\\";
        File f=new File(dir);
        String [] list=f.list();        
        String fileName;
        QuickSort_Sortable qs=new QuickSort_Sortable();
        int nSize=list.length;
//        int imin=0;
//        int nSize1=0;
 //       AtlasVoxelNode aNode;
        int fileLength=0;
        File f1;
        for(int i=0;i<nSize;i++){
            f1=new File(dir+list[i]);
            if(f1.isDirectory()) continue;
            try {
                    ReadAtlasVoxels(dir,list[i]);
            }catch (IOException e){
                System.out.println(e);
            }
    //            nSize1=m_vAtlasVoxels.size();
//            for(int j=0;j<nSize1;j++){
 //               imin=j;
  //              for(int k=j+1;k<nSize1;k++){
//                    if(m_vAtlasVoxels.get(k).smaller(m_vAtlasVoxels.get(imin))) imin=k;
//                }
//                if(imin!=j) {
 //                   aNode=m_vAtlasVoxels.get(j);
//                    m_vAtlasVoxels.set(j,m_vAtlasVoxels.get(imin));
  //                  m_vAtlasVoxels.set(imin,aNode);                    
//                }
 //           }
            qs.quicksort(m_vAtlasVoxels);
            fileLength=list[i].length();
            fileName=list[i].substring(0, fileLength-4)+"_sorted.txt";
            try{
                writeAtlasVoxel(dir+"Sorted\\",fileName);
            }catch (IOException e){
                System.out.println(e);
            }
        }        
    }
    
    public void writeAtlasVoxel(String dir, String fileName) throws FileNotFoundException{
        String name=m_sAnnotationFileName;
	String path = dir+fileName;
        File file=new File(path);
        
//        FileWriter f= new FileWriter(file);"
        Formatter fm= new Formatter(file);
        fm.format( "%s%s",m_sComment,newline);
        fm.format( "%s%s",m_sDim,newline);
        fm.flush();
        int nSize=m_vAtlasVoxels.size(); 
        fm.flush();
        for(int i=0;i<nSize;i++)
        {
            AtlasVoxelNode av0=new BrainSimulations.DataClasses.AtlasVoxelNode();
            av0=m_vAtlasVoxels.get(i);
            fm.format("%d,%d,%d,%d%s", av0.x,av0.y,av0.z,av0.id,newline);
            fm.flush();
        }        
        fm.close();
    }
    
    public void importSubstructures(){
        if(!substructures.isEmpty())substructures.clear();
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\Separated\\Sorted\\25\\Output\\test\\";
        File file=new File(dir);
        String list[]=file.list();
        long freeMem = r.freeMemory(); 
        for(int i=0;i<list.length;i++){
            File f=new File(dir+list[i]);
            if(f.isDirectory()) continue;
            freeMem=r.freeMemory();
            SubstructureSorted newSub=new SubstructureSorted();
            freeMem=r.freeMemory();
            try{newSub.importSubstructure(dir+list[i]);}
            catch(IOException e){
                System.out.println("Could not import substructures!");
            }
            freeMem=r.freeMemory();
            substructures.add(newSub);
            freeMem=r.freeMemory();
            r.gc();
            freeMem=r.freeMemory();

//            newSub.buildRegions();
//            QuickFormatter qfm=new QuickFormatter(dir+"testOut\\"+list[i].substring(0,list[i].length()-4)+"_Output.txt");
//            newSub.writeSubstructure(qfm.getFormatter());
//            qfm.getFormatter().close();
        }
    }
    ArrayList <SubstructureSorted> getSubstructureProperties(){
        if(substructures.isEmpty()){
            try {ReadBrainStructureNames();}
            catch (IOException e){
                System.out.println("Unable to read annotation");
            }
            buildSubstructures();
        }
        return substructures;
    }
}

