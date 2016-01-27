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

public class Stack_ChannelSplitter implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */
    public void run(String arg) {
        ImagePlus impl = WindowManager.getCurrentImage();
        if(impl==null) {
            IJ.noImage();
            return ;
        }
        splitChannels(impl);
    }

    protected void splitChannels(ImagePlus impl) {
        String sLabel = impl.getTitle();
        int w=impl.getWidth();
        int h=impl.getHeight();
        int len=w*h;
        int sz = impl.getStackSize();
        int i,j,k;
        byte[] rp,gp,bp;
        ImageStack rs=new ImageStack(w,h),gs=new ImageStack(w,h),bs=new ImageStack(w,h);
        for(i=1;i<=sz;i++){
            impl.setSlice(i);
            ByteProcessor rps=new ByteProcessor(w,h),gps=new ByteProcessor(w,h),bps=new ByteProcessor(w,h);
            rp=(byte[])rps.getPixels();
            gp=(byte[])gps.getPixels();
            bp=(byte[])bps.getPixels();
            CommonMethods.getRGB(impl, rp, gp, bp);
            rs.addSlice("", rps);
            gs.addSlice("", gps);
            bs.addSlice("", bps);
        }
        ImagePlus rmpl=new ImagePlus("",rs),gmpl=new ImagePlus("",gs),bmpl=new ImagePlus("",bs);
        rmpl.show();
        gmpl.show();
        bmpl.show();
    }
}
