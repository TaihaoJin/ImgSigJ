package stacks;

import ij.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import java.text.DecimalFormat;
import utilities.CommonMethods;
import ij.process.ByteProcessor;
import stacks.Stack_RunningWindowAverage;
import ij.gui.GenericDialog;
public class Stack_RunningWindowAverage implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */
    public void run(String arg) {
        ImagePlus impl = WindowManager.getCurrentImage();
        if(impl==null) {
            IJ.noImage();
            return ;
        }
        RunningWindowAverage(impl);
    }

    protected void RunningWindowAverage(ImagePlus impl) {
        int ws=1;
        GenericDialog gd=new GenericDialog("Choose images and parameters");
        gd.addNumericField("maximum radius", ws, 0);
        gd.showDialog();
        ws=(int)(gd.getNextNumber()+0.5);
        RunningWindowAverage(impl,ws);
    }
    public static void RunningWindowAverage(ImagePlus impl, int ws) {
        String sLabel = impl.getTitle();
        int w=impl.getWidth();
        int h=impl.getHeight();
        int len=2*ws+1;
        int sz = impl.getStackSize();
        int i,j,k;

        ImagePlus implAve=CommonMethods.cloneImage(impl);
        implAve.setTitle("running window average of "+impl.getTitle()+" ws:"+ws);

        ImageStack is=new ImageStack(w,h);
        for(i=0;i<ws;i++){
            is.addSlice(i+"-th slice", new ByteProcessor(w,h));
        }

        int[][] pixels=new int[h][w];
        int[][] ave=new int[h][w];
        int ji,jf,l;
        for(i=0;i<sz;i++){
            implAve.setSlice(i+1);
            for(j=0;j<h;j++){
                for(k=0;k<w;k++){
                    ave[j][k]=0;
                }
            }
            ji=Math.max(0, i-ws);
            jf=Math.min(sz-1, i+ws);
            len=jf-ji+1;
            for(j=ji;j<=jf;j++) {
                impl.setSlice(j);
                CommonMethods.getPixelValue(impl, j+1, pixels);
                for(l=0;l<h;l++){
                    for(k=0;k<w;k++){
                        ave[l][k]+=pixels[l][k];
                    }
                }
            }
            for(j=0;j<h;j++){
                for(k=0;k<w;k++){
                    ave[j][k]/=len;
                }
            }
            CommonMethods.setPixels(implAve, ave);
        }
        implAve.show();
    }
}
