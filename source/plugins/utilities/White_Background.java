/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.WindowManager;
import ij.plugin.PlugIn;

/**
 *
 * @author Taihao
 */
public class White_Background implements PlugIn{
    public void run(String arg){        
        ImagePlus impl,impl0,impl1;
        impl=WindowManager.getCurrentImage();
        int w=impl.getWidth();
        int h=impl.getHeight();
        int pixels[]=(int[])impl.getProcessor().getPixels();
        int RGB[]=new int[3];
        int pixel, cutoff=300;
        for(int i=0;i<h*w;i++){
            pixel=pixels[i];
            CommonMethods.intTOrgb(pixel, RGB);
            if(RGB[0]+RGB[1]+RGB[2]>cutoff){
                RGB[0]=255;
                RGB[1]=255;
                RGB[2]=255;
                pixel=CommonMethods.rgbTOint(RGB);
                pixels[i]=pixel;
            }else{
                pixels[i]=0;
            }
        }
        impl.show();
    }
}
