/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Demo;
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
 
//package com.asprise.util.ocr.demo;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//import com.asprise.util.ocr.OCR;
public class OCRDemo {
    public OCRDemo(){
    }
    public String getString(){
        String OpenerTitle="Open an image for OCR demo";
        Opener opn=new Opener();
        OpenDialog od = new OpenDialog(OpenerTitle, "");
        String directory = od.getDirectory();
        String name = od.getFileName();
        ImagePlus impl=opn.openImage(directory, name);
        Image img=impl.getImage();
        impl.show();
        int h=img.getHeight(null);
        int w=img.getWidth(null);
        ColorModel cm=impl.getProcessor().getColorModel();
        BufferedImage bimg=new BufferedImage(w,h,4);
        int i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                bimg.setRGB(j, i, impl.getProcessor().getPixel(j,i));
            }
        }
//        String s = new OCR().recognizeEverything(bimg);
        String s = null;
        return s;
    }
}
