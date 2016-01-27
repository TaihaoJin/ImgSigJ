/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import utilities.CommonMethods;
/**
 *
 * @author Taihao
 */
public class ExtractSlices_ implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        int[]pixels1;
        int[]pixels2;
        ImagePlus implc=CommonMethods.cloneImage(impl);
        int nSliceNumber1,nSliceNumber2;
        int n1=92;
        int m1=253;
        int h=impl.getHeight();
        int w=impl.getWidth();
        int x,y;
        int ave1,ave2;
        int num;
        byte bps1[];
        byte bps2[];
        int r=0,g=0,b=0;
        int rgbPixels[]=new int[w*h];
        ImagePlus impld=CommonMethods.getRGBImage("Colocolized image", w, h, rgbPixels);
        for(y=0;y<h;y++){
            for(x=0;x<w;x++){
                num=y*w+x;
                ave1=0;
                ave2=0;
                for(int i=1;i<5;i++){
                    nSliceNumber1=n1+i;
                    nSliceNumber2=m1+i;
                    impl.setSlice(nSliceNumber1);
                    implc.setSlice(nSliceNumber2);
                    bps1=(byte[])impl.getProcessor().getPixels();
                    bps2=(byte[])implc.getProcessor().getPixels();
                    ave1+=0xff&bps1[num];
                    ave2+=0xff&bps2[num];
                }
                ave1/=5;
                ave2/=5;
                r=ave1;
                g=ave2;
                rgbPixels[num]=(r<<16)|(g<<8)|(b);
            }
        }
        impld.show();
    }
}
