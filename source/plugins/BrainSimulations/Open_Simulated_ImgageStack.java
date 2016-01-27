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
import ij.io.Opener;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class Open_Simulated_ImgageStack implements PlugIn{
    public void run(String Arg){
        ImagePlus impl=openSimulatedImageStack();
        impl.show();
    }
    
    public ImagePlus openSimulatedImageStack(){
        Opener opn=new Opener();
	OpenDialog od = new OpenDialog("Open", "");
        String directory = od.getDirectory();
        String name = od.getFileName();
        return opn.openImage(directory, name);
    }
}
