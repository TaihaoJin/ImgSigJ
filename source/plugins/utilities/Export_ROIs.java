/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

import ij.plugin.PlugIn;
import ij.gui.Roi;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.Rectangle;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import java.util.Formatter;
import java.io.File;
import java.io.FileNotFoundException;
import ij.IJ;
import utilities.ArrayofArrays.IntArray;
import utilities.ArrayofArrays.IntArray2;
import utilities.ArrayofArrays.IntArray3;
import ij.gui.Roi;
import java.util.ArrayList;
import CommonDataClasses.CommonDataSet;
import CommonDataClasses.MeanSem;
import utilities.statistics.MeanSem0;

/*
 *
 * @author Taihao
 */
public class Export_ROIs implements PlugIn{
    IntArray3 PixelsROIs=new IntArray3();
    ImagePlus impl;
    ArrayList <Roi> ROIs;
    String sROIFileName;
    public void run(String arg){
        impl=WindowManager.getCurrentImage();
        ROIs=CommonDataSet.ROIOrganizer.importROIs(impl);
        sROIFileName=CommonDataSet.ROIOrganizer.getROIFileName(impl);
        collectPixels();
        exportROIs();
    }
    void collectPixels(){
        int size=ROIs.size();
        Roi ROI;
        for(int i=0;i<size;i++){
            ROI=ROIs.get(i);
            impl.setRoi(ROI);
            PixelsROIs.m_IntArray3.add(collectROI(impl));
        }
    }
    IntArray2 collectROI(ImagePlus impl){
        IntArray2 PixelsROI=new IntArray2();
        int numSlices=impl.getNSlices();
        Roi ROI=impl.getRoi();
        Rectangle br=ROI.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height,h=impl.getHeight(),w=impl.getWidth();
        byte[] pixels;
        int x,y,i,j,pixel,o,k;

        for(i=1;i<=numSlices;i++){
            impl.setSlice(i);
            pixels=(byte[])impl.getProcessor().getPixels();
            IntArray ir=new IntArray();
            for(j=0;j<rh;j++){
                y=yo+j;
                o=w*y;
                for(k=0;k<rw;k++){
                    x=xo+k;
                    if(ROI.contains(x, y)){
                        ir.m_intArray.add(0xff&pixels[o+x]);
                    }
                }
            }
            PixelsROI.m_IntArray2.add(ir);
        }        
        return PixelsROI;
    }
    void exportROIs(){
        String newline = System.getProperty("line.separator");
        int numSlices=impl.getNSlices();
        SaveDialog sd=new SaveDialog("Export the pixels into a file",sROIFileName,".txt");
        String dir=sd.getDirectory(),name=sd.getFileName();
        Formatter fm=QuickFormatter(dir+name);
        int i,j,k,pixel;

        int size=ROIs.size();
        int roiSize;
        for(i=0;i<size;i++){
            roiSize=PixelsROIs.m_IntArray3.get(i).m_IntArray2.get(0).m_intArray.size();
            for(j=0;j<roiSize;j++){
                for(k=0;k<numSlices;k++){
                    pixel=PixelsROIs.m_IntArray3.get(i).m_IntArray2.get(k).m_intArray.get(j);
                    fm.format("%6d", pixel);
                }
                fm.format("%s", newline);
            }
        }
        fm.close();
        
        name=CommonMethods.getFileName(name)+"_MeanSems"+CommonMethods.getFileExtention(name);
        fm=QuickFormatter(dir+name);
        IntArray ir;
        MeanSem ms;
        int digits=0;
        for(i=0;i<size;i++){
            digits=getDigits(i);
            switch (digits){
                case 1:
                    fm.format("   mean%1d    sem%1d", i,i);
                    break;
                case 2:
                    fm.format("  mean%2d   sem%2d", i,i);
                    break;
                case 3:
                    fm.format(" mean%3d  sem%3d", i,i);
                    break;
                default:
                    break;
            }
        }
        fm.format("    mean     sem");
        fm.format("%s", newline);
        Rectangle br;
        MeanSem0 ms0=new MeanSem0();
        for(i=0;i<numSlices;i++){
            for(j=0;j<size;j++){
                if(i==0){
                    br=ROIs.get(j).getBounds();
                }
                ir=PixelsROIs.m_IntArray3.get(j).m_IntArray2.get(i);
                ms=CommonMethods.getMeanSem(ir);
                if(j==0){
                    ms0.update(ms.size, ms.mean, ms.sem);
                }else{
                    ms0.mergeSems(new MeanSem0(ms.size, ms.mean, ms.sem*ms.sem*ms.size));
                }
                fm.format("  %6.2f  %6.3f",ms.mean,ms.sem);
            }
            fm.format("  %6.2f  %6.3f",ms0.mean,ms0.sem);
            fm.format("%s", newline);
        }
        fm.close();
        name=CommonMethods.getFileName(name)+".brd";
        fm=QuickFormatter(dir+name);
        fm.format("bounds of ROIs ((x,y) for top_left, top_right, bottom_left and bottom_right) and the number of pixels in ROI%s",newline);
        for(j=0;j<size;j++){
            roiSize=PixelsROIs.m_IntArray3.get(j).m_IntArray2.get(0).m_intArray.size();            br=ROIs.get(j).getBounds();
            fm.format("%3d  (%4d,%4d) (%4d,%4d) (%4d,%4d) (%4d,%4d)  N=%8d%s",j,br.x,br.y,br.x+br.width,br.y,br.x,br.y+br.height,br.x+br.width,br.y+br.height,roiSize,newline);
        }
        fm.format("%s", newline);
        fm.close();
    }
    int getDigits(int it){
        int digs=1;
        int q=it/10;
        while(q!=0){
            digs++;
            q/=10;
        }
        return digs;
    }
    Formatter QuickFormatter (String path){
        Formatter fm=null;
        File file=new File(path);
        try {
                fm= new Formatter(file);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Fount");
        }
        return fm;
    }
}
