/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.OpenDialog;
import ij.io.RandomAccessStream;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import BrainSimulations.DataClasses.*;
import java.util.Formatter;
import utilities.Constants;
import ij.io.SaveDialog;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.ArrayList;
import BrainSimulations.SubstructureEssential;
import BrainSimulations.StructureFrame;
import ij.gui.*;


/**
 *
 * @author Taihao
 */
public class Brain_Image_Simulation implements PlugIn{
    static boolean m_frameBuilt=false;
    Runtime runTime=Runtime.getRuntime();
    long freeMem;
    AtlasAnnotation m_annotation;
    ArrayList <AtlasAnnotation> annotations=new ArrayList <AtlasAnnotation>();
    ArrayList <SubstructureEssential> substructures;
    SimulatedImages m_simulatedImages;
    int m_nNumSubstructures;
    StructureFrame structureFrame;
    
    public void run(String arg){
        long start1 = System.currentTimeMillis();
        simulate("Mouse");
        annotations.add(new AtlasAnnotation());
//        try{annotations.get(0).separateVoxels();}
//        catch (IOException e){
//            e=e;
//        }
        freeMem=runTime.freeMemory()/(1024*1024);
        if(!m_frameBuilt){
            try{buildSubstructures();}
            catch (FileNotFoundException e1){} catch(IOException e2){};
            structureFrame=new StructureFrame(substructures);
            m_frameBuilt=true;
        }

        OutputSubstructureNameNodes(substructures);
//        try {
//            annotation.writeAtlasVoxels();
//        }
//        catch (FileNotFoundException e)
//        {
            
//        }
//        try {annotations.get(0).ReadBrainStructureNames();}
//        catch (IOException e){
 //           System.out.println("Unable to read annotation");
 //      }
        
 //       try {
//            m_annotation.ReadAtlasVoxels_build();
 //       }
//        catch (IOException e){
//            System.out.println("Unable to read annotation");
 //       }
        
//            annotations.get(0).SortVoxels();
//         displayAnnotation();
 //       try {
//            annotation.writeBrainStructureNames();
//        }
//        catch (FileNotFoundException e)
 //       {
            
//        }
//           try{annotations.get(0).buildAnnotation_Sorted();}
//           catch (FileNotFoundException e){}catch(IOException e1){};
           
        
//            annotations.get(0).importSubstructures();
//        overrideColor();
        long start2 = System.currentTimeMillis();
        freeMem=runTime.freeMemory()/(1024*1024);
        m_simulatedImages=new SimulatedImages(structureFrame);
        int sectionIndex=2;//1 for coronal, 2 for sagital and 3 for horizontal sections.
        for(sectionIndex=1;sectionIndex<=3;sectionIndex++){
            m_simulatedImages.makeSeriesSections_EntireBrain(sectionIndex);
            freeMem=runTime.freeMemory()/(1024*1024);
        }
        
//        for(sectionIndex=1;sectionIndex<=3;sectionIndex++){
//            m_simulatedImages.compareSeriesSections(sectionIndex);
 //           freeMem=runTime.freeMemory()/(1024*1024);
  //      }
        
        long finishTime = System.currentTimeMillis();
        long elapsedTimeMillis1 = finishTime-start1;
        long elapsedTimeMillis2 = finishTime-start2;
        elapsedTimeMillis1=elapsedTimeMillis1;
    }
    
    void overrideColor(){
        AtlasAnnotation ata=new AtlasAnnotation();
        ArrayList <SubstructureSorted> ss=ata.getSubstructureProperties();
        int size=substructures.size();
        int nID;
        for(int i=0;i<size;i++){
            nID=substructures.get(i).getID();
            substructures.get(i).setPixel(ss.get(nID).getPixel());
        }
    }
    
    void buildSubstructures() throws FileNotFoundException, IOException{
        substructures=new ArrayList <SubstructureEssential>();
        if(!substructures.isEmpty())substructures.clear();
        String path="D:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\Separated\\Sorted\\25\\EssentialSubstructures.dat";
        FileInputStream fis=new FileInputStream(path);
        BufferedInputStream bis=new BufferedInputStream(fis);
        DataInputStream dis=new DataInputStream(bis);
        m_nNumSubstructures=dis.readInt();
        int length=0;
        Runtime r = Runtime.getRuntime();
        long freeMem = r.freeMemory(); 
        long start = System.currentTimeMillis();
        int nid;
        float ratio=0.2f;
        for(int i=0;i<m_nNumSubstructures;i++){
            IJ.showStatus("Importing Substructures " + (i+1) + "/" + m_nNumSubstructures);
            IJ.showProgress((double)(i+1)/m_nNumSubstructures);
            freeMem=r.freeMemory();
            SubstructureEssential newSub=new SubstructureEssential();
            try{newSub.importSubstructure(dis);}
            catch(IOException e){
                System.out.println("Could not import substructures!");
            }
            nid=newSub.StructureNode.informaticsId;
            ratio=0.2f;
            if(nid==26||nid==28) ratio=0.8f;
            if(nid==28) newSub.adjustRGB(-40, 0, +40);
//            newSub.reduceIntensity(ratio);
            substructures.add(newSub);
            freeMem=r.freeMemory();
            r.gc();
            freeMem=r.freeMemory();

//            newSub.buildRegions();
//            QuickFormatter qfm=new QuickFormatter(dir+"testOut\\"+list[i].substring(0,list[i].length()-4)+"_Output.txt");
//            newSub.writeSubstructure(qfm.getFormatter());
//            qfm.getFormatter().close();
        }
        long elapsedTimeMillis = System.currentTimeMillis()-start;
        elapsedTimeMillis=elapsedTimeMillis;
    }
    
    public void OutputSubstructureNameNodes(ArrayList <SubstructureEssential> substructures0){
        String m_sBSHeading="StructureName,Abbreviation,ParentStruct,red,green,blue,informaticsId,StructureId";
        String name="";
        String type="csv";
        String extension="csv";
                
   	SaveDialog sd = new SaveDialog("Save as "+type, name, extension);
	name = sd.getFileName();
	String directory = sd.getDirectory();
	String path = directory+name+"."+extension;
        File file=new File(path);
        
        try{Formatter fm= new Formatter(file);
            String newline=Constants.newline;
            fm.format( "%s%s",m_sBSHeading,newline);
            int nSize=substructures.size();
            fm.flush();

            for(int i=0;i<nSize;i++)
            {
                fm.format("%s,",substructures0.get(i).getStructureNode().StructureName);
                fm.format("%s,",substructures0.get(i).getStructureNode().Abbreviation);
                fm.format("%s,",substructures0.get(i).getStructureNode().ParentStruct);
                fm.format("%d,",substructures0.get(i).getStructureNode().red);
                fm.format("%d,",substructures0.get(i).getStructureNode().green);
                fm.format("%d,",substructures0.get(i).getStructureNode().blue);
                fm.format("%d,",substructures0.get(i).getStructureNode().informaticsId);
                fm.format("%d%s",substructures0.get(i).getStructureNode().StructureId,newline);
           }
            fm.flush();
            fm.close();
        }
        catch(FileNotFoundException e){
            e=e;
        }
}
    
    public void simulate(String arg){
//        boolean bTest=showDialog ();
    }
boolean showDialog() {
                BrainStructureNameNode aNode=new BrainStructureNameNode();
		GenericDialog gd = new GenericDialog("Reduce");
        	gd.setInsets(10, 20, 5);
                aNode.StructureName="test";
		gd.addMessage("Simulating a Mouse Brain"+aNode.StructureName);
		gd.setInsets(0, 35, 0);
		gd.addCheckbox(" channels", true);
		gd.setInsets(0, 35, 0);
		gd.addCheckbox(" slices", true);
		gd.setInsets(5, 20, 0);
		gd.addMessage("      ");
//		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
                boolean channels2 = gd.getNextBoolean();
		boolean slices2 = gd.getNextBoolean();
                return true;
	}       
}



