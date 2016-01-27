/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Demo;
import ij.WindowManager;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class Scale_ToFullRange implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        scaleToFullRange(impl);
        impl.show();
    }
    void scaleToFullRange(ImagePlus impl){
        byte[] pixels=(byte[])impl.getProcessor().getPixels();
        int nMax=0, nMin=255;
        int len=pixels.length;
        int i,pixel;
        for(i=0;i<len;i++){
            pixel=pixels[i]&0xff;
            if(pixel<nMin) nMin=pixel;
            if(pixel>nMax) nMax=pixel;
        }
        for(i=0;i<len;i++){
            pixel=pixels[i]&0xff;
            pixels[i]=(byte)scaling(nMin,0,nMax,255,pixel);
        }
    }
    int scaling(double x1, double y1, double x2, double y2, double x){
        double k=(y2-y1)/(x2-x1);
        double y=y1+k*(x-x1);
        return (int) y;
    }
}
