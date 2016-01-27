/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import java.io.File;
import ij.io.Opener;
import java.awt.image.*;
import java.awt.*;
import ij.io.FileSaver;
/**
 *
 * @author Taihao
 */
public class AtlasFeatureAnalysis {
    public AtlasFeatureAnalysis(){
    }
    public static void SplitAtlaFiles(String dir){
        dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Referenced Atlas\\Reorded\\";
        Opener opn=new Opener();        
        File f=new File(dir);
        String name;
        String[] list=f.list();
        int length=list.length;
        int i,j,k,w=0,h=0,w1=0,h1=0,w0=0,h0=0;
        ImagePlus impl;
        ImagePlus impl1;
        Image img;
        Canvas cvs=new Canvas();
        int pixels[]=null;
        ImageProcessor impr;
        int nl;
        for(i=0;i<length;i++){
            name=list[i];
            nl=name.length();
            impl=opn.openImage(dir, name);
            img=impl.getImage();
//            impl.show();
            w=img.getWidth(null);
            h=img.getHeight(null);
            w1=w/2;
            if(i==0||w!=w0||h!=h0){
                pixels=new int[w1*h];
                w0=w;
                h0=h;                
            }
            for(j=0;j<h;j++){
                for(k=0;k<w1;k++){
                    pixels[j*w1+k]=impl.getProcessor().getPixel(w1+k,j);
                }
            } 
            impr=new ColorProcessor(w1, h, pixels);    
            impl1=new ImagePlus(name+" (Right half)",impr);
            impl1.getProcessor().setPixels(pixels);
//            impl1.show();
            FileSaver fs=new FileSaver(impl1);
            fs.saveAsTiff(dir+name.substring(0,nl-4)+".tiff");
        }
    }
}

