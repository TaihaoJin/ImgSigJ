/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

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
import java.awt.image.*;
import java.awt.*;
import ij.process.ImageProcessor;
import BrainSimulations.AtlasFeatureAnalysis;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class Compare_Images implements PlugIn{
    public void run(String Arg){
        ImagePlus impl1=openImage("Open the first image");
        impl1.show();
        ImagePlus impl2=openImage("Open the second image");
        impl2.show();
        ImagePlus impl3=ImageDifference(impl1, impl2);
        impl3.show();
    }
    
    public ImagePlus openImage(String OpenerTitle){
        AtlasFeatureAnalysis af=new AtlasFeatureAnalysis();
        Opener opn=new Opener();
	OpenDialog od = new OpenDialog(OpenerTitle, "");
        String directory = od.getDirectory();
        String name = od.getFileName();
        return opn.openImage(directory, name);
    }
    
    public static ImagePlus ImageDifference(ImagePlus impl1, ImagePlus impl2){   
        Image img1,img2;
        img1=impl1.getImage();
        int h=img1.getHeight(null);
        int w=img1.getWidth(null);
        int pixels1[]=new int[w*h];
        int i,j,p1,p2;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){       
                p1=impl1.getProcessor().getPixel(i,j);
                p2=impl2.getProcessor().getPixel(i,j);
                if(p1!=p2){
                    p1=p1;
                }
                pixels1[i*w+j]=p1-p2;                
            }
        }
        ImageProcessor ip=impl2.getProcessor();
        ip.setPixels(pixels1);
        ImagePlus impl=new ImagePlus("Comparison...",ip);
        return impl;
    }
}