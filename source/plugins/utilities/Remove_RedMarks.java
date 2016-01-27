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
import java.util.Formatter;
import java.io.File;
import java.io.FileNotFoundException;
import ij.IJ;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class Remove_RedMarks implements PlugIn{
    public void run(String arg){
        removeRedMarks();
    }

    void removeRedMarks(){
        ImagePlus impl=WindowManager.getCurrentImage();
        int numSlices=impl.getNSlices();
        int h=impl.getHeight(),w=impl.getWidth(),n=impl.getNSlices();
        int[] pixels;
        int i,j,pixel,r,g,b,len;
        ArrayList <Integer> background=new ArrayList <Integer>();
        Roi ROI=impl.getRoi();
        Rectangle br=ROI.getBounds();
        int sizeb=br.height*br.width;
        int index=0;

        for(i=1;i<=n;i++){
            impl.setSlice(i);
            pixels=(int[])impl.getProcessor().getPixels();
            len=pixels.length;
            if(ROI!=null){
                background.clear();
                getBackground(pixels,ROI,background,impl);
                sizeb=background.size();
            }
            for(j=0;j<len;j++){
                pixel=pixels[j];
                r=0xff & (pixel>>16);
                g=0xff & (pixel>>8);
                b=0xff & pixel;
//                if(r>0.75*(g+b)||g>0.75*(r+b)||b>0.75*(g+r)){
                if(r>0.5*(g+b)+15||g>0.5*(r+b)+15||b>0.5*(g+r)+15){
                    if(ROI!=null){
                        index=(int)(Math.random()*sizeb);
                        pixel=background.get(index);
                        pixels[j]=pixel;
                    }else{
                        r=(g+b)/2;
                        pixel=(r<<16)|(g<<8)|b;
                        pixels[j]=pixel;
                    }
                }
            }
        }
    }
    void getBackground(int[] pixels, Roi ROI, ArrayList <Integer> background, ImagePlus impl){
        Rectangle br=ROI.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height,h=impl.getHeight(),w=impl.getWidth();
        int x,y,i,j,pixel,o;
        for(i=0;i<rh;i++){
            y=yo+i;
            o=w*y;
            for(j=0;j<rw;j++){
                x=xo+j;
                if(ROI.contains(x, y)){
                    pixel=pixels[o+x];
                    background.add(pixel);
                }
            }
        }
    }
}
