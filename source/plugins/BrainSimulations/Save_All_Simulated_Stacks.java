/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.OpenDialog;
import ij.io.RandomAccessStream;
import ij.plugin.PlugIn;
import BrainSimulations.DataClasses.*;
import ij.gui.*;
import ij.WindowManager;
import ij.io.FileSaver;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class Save_All_Simulated_Stacks implements PlugIn{
    
    public void run(String arg){
        saveAllSimulatedStacks();
    }
    void saveAllSimulatedStacks(){
        int ids[]=WindowManager.getIDList();
        int len=ids.length;
        int id;
        ArrayList <ImagePlus> impls;
        impls=new ArrayList <ImagePlus>();
        ImagePlus impl;
        for(int i=0;i<len;i++)
        {
            id=ids[i];
            impl=new ImagePlus();
            impl=(ImagePlus)WindowManager.getImage(id);
            if(impl.getTitle().startsWith("Simulated")){
                impls.add(impl);
            }
        }
        len=impls.size();
        for(int i=0;i<len;i++)
        {            
            impl=impls.get(i);
            FileSaver fs=new FileSaver(impl);
            String dir="C:\\Taihao\\Lab UCSF\\Imaging\\images\\simulated brain images\\";
            /*getFileInfo returns an instance of fileInfo with part of the 
            members of FileInfo class copied from the fileInfo of the 
            ImagePlus object. directory and fileName are not copied, so the 
            directory and fileName of the returned object is "" and "Untitled"*/
            
            String name=impl.getTitle();
            fs.saveAsTiffStack(dir+name);
            
        }
    }
}
