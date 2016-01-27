/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import ij.plugin.PlugIn;
import ij.ImagePlus;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class Image_Negative implements PlugIn{
    public void run(String arg){
        ImagePlus impl=CommonMethods.getCurrentImage();
        int[] pixels=(int[])impl.getProcessor().getPixels();
        int pixel,r,g,b,len=pixels.length,i;
        for(i=0;i<len;i++){
            pixel=pixels[i];
            r=0xff & (pixel>>16);
            g=0xff & (pixel>>8);
            b=0xff & pixel;
            pixels[i]=(r<<16)|(g<<8)|b;
        }
    }
}
