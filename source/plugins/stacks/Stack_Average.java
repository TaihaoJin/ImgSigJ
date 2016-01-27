/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;

/**
 *
 * @author Taihao
 */
import ij.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import java.text.DecimalFormat;
import utilities.CommonMethods;

public class Stack_Average implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */
    public void run(String arg) {
        ImagePlus impl = WindowManager.getCurrentImage();

        if(impl==null) {
            IJ.noImage();
            return ;
        }
        averageStack(impl);
    }

    protected void averageStack(ImagePlus impl) {
        String sLabel = impl.getTitle();
        int w=impl.getWidth();
        int h=impl.getHeight();
        int len=w*h;
        ImagePlus impla=newGary8Image("stact average", w, h);
        int numStacks = impl.getStackSize();
        ImageStack stack = impl.getStack();
        int sz = stack.getSize();
        double ave[]=new double[h*w];
        int i,j;
        for(i=0;i<sz;i++){
            ave[i]=0.;
        }

        byte[] pixels;


        for(i=1;i<=sz;++i) {
            impl.setSlice(i);
//            ImageStack ims = new ImageStack(w,h,null);
//            ims.addSlice("",impl.getCurrentSlice());
//            ImagePlus  implt=new ImagePlus("",ims);
//            convertToGray8(implt);
            pixels=(byte[])impl.getProcessor().getPixels();
            for(j=0;j<len;j++){
                ave[j]+=0xff&((int)pixels[j]);
            }
        }
        pixels=(byte[])impla.getProcessor().getPixels();
        for(i=0;i<len;i++){
            pixels[i]=(byte)(ave[i]/sz);
        }
        impla.show();
    }
     public static void convertToGray8(ImagePlus impl){
        ImageConverter ic=new ImageConverter(impl);
        ic.convertToGray8();
     }
    public static ImagePlus newGary8Image(String title,int w, int h){
        byte pixels[]=new byte[w*h];
        for(int i=0;i<w*h;i++){
            pixels[i]=0;
        }
        ByteProcessor cp=new ByteProcessor(w,h);
        ImagePlus impl=new ImagePlus(title, cp);
        return impl;
    }
}
