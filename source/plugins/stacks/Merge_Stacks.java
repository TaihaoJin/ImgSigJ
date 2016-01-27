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
import ij.process.ByteProcessor;
import ij.io.Opener;
import ij.io.OpenDialog;

public class Merge_Stacks implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */
    public void run(String arg) {
        ImagePlus impl1=importImage("import the first image stack");
        ImagePlus impl2=importImage("import the second image stack");
//        int x1=255,x2=1602,y1=1627,y2=1869,i1=5,i2=9;
//        int x1=245,x2=1909,y1=449,y2=600,i1=2,i2=6;
        int x1=0,x2=0,y1=0,y2=0,i1=43,i2=0;
        int dN=i2-i1,dX=x2-x1,dY=y2-y1;
        int[]xCorrection=new int[1];
        xCorrection[0]=0;
        /*
        int[]xCorrection=new int[22];
        for(int i=0;i<22;i++){
            xCorrection[i]=0;
        }
        xCorrection[0]=1864-1700;
        xCorrection[1]=1847-1700;
        xCorrection[2]=1830-1700;
        xCorrection[3]=1809-1700;
        xCorrection[4]=1797-1700;
        xCorrection[5]=1783-1700;
        xCorrection[6]=1774-1700;
        xCorrection[7]=1764-1700;
        xCorrection[8]=1755-1700;
        xCorrection[9]=1696-1649;
        xCorrection[10]=1857-1812;
        xCorrection[11]=1857-1813;
        xCorrection[12]=1848-1813;
        xCorrection[13]=1865-1829;
        xCorrection[14]=1859-1822;
        xCorrection[15]=2064-2042;
        xCorrection[16]=2063-2045;
        xCorrection[17]=2060-2046;
        xCorrection[18]=2054-2040;
        xCorrection[19]=2050-2041;
        xCorrection[20]=2049-2043;
        xCorrection[21]=2049-2045;
        */
        mergeStacks(impl1,impl2,dN,dX,dY,xCorrection);
    }

    protected void mergeStacks(ImagePlus impl1,ImagePlus impl2,int dN,int dX,int dY,int[] xCorrection) {
        int w1=impl1.getWidth();
        int h1=impl1.getHeight();
        int w2=impl1.getWidth();
        int h2=impl1.getHeight();
        int len1=w1*h1;
        int len2=w1*h1;
//        int H=h+Math.abs(dY);
//        int W=w+Math.abs(dX);
        int H=Math.max(h1, h2);
        int W=Math.max(w1, w2);
        int n1 = impl1.getStackSize(), n2=impl2.getStackSize();
        int shiftX=Math.max(0, -dX),shiftY=Math.max(0, -dY),shiftN=Math.max(0, -dN);
        int j,k,x,y,o;
        ImageStack is=new ImageStack(W,H);
//        int len1=xCorrection.length;

        byte[] pixels1,pixels2,pixels;

        int index1=0,index2=0,i=0;;
        int pixel0,pixel;
        while(index1<=n1||index2<=n2) {
            index1=i-shiftN-dN;
            index2=i-shiftN;
            ByteProcessor bp=null;
            pixels=null;
            if(index1>0&&index1<=n1){
                bp=new ByteProcessor(W,H);
                pixels=(byte[])bp.getPixels();
                for(y=0;y<H;y++){
                    o=y*W;
                    for(x=0;x<w1;x++){
                        pixels[w1+x]=0;
                    }
                }
                impl1.setSlice(index1);
                pixels1=(byte[])impl1.getProcessor().getPixels();
                for(j=0;j<h1;j++){
                    o=j*w1;
                    for(k=0;k<w1;k++){
                        y=j+shiftY+dY;
                        x=k+shiftX+dX;
//                        if(index1<=len1)x-=xCorrection[index1-1];
                        pixels[y*W+x]=pixels1[o+k];
                    }
                }
            }
            if(index2>0&&index2<=n2){
                if(bp==null){
                    bp=new ByteProcessor(W,H);
                    pixels=(byte[])bp.getPixels();
                    for(y=0;y<H;y++){
                        o=y*W;
                        for(x=0;x<w2;x++){
                            pixels[w2+x]=0;
                        }
                    }
                }
                pixels=(byte[])bp.getPixels();
                impl2.setSlice(index2);
                pixels2=(byte[])impl2.getProcessor().getPixels();
                for(j=0;j<h2;j++){
                    o=j*w2;
                    for(k=0;k<w2;k++){
                        y=j+shiftY;
                        x=k+shiftX;
                        pixel0=0xff&pixels[y*W+x];
                        pixel=0xff&pixels2[o+k];
                        if(pixel>pixel0) pixels[y*W+x]=(byte)pixel;
                    }
                }
            }
            if(bp!=null) is.addSlice("", bp);
            i++;
        }
        ImagePlus impl=new ImagePlus("merged stack",is);
        impl.show();
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
    public static ImagePlus importImage(String title){
        OpenDialog od=new OpenDialog(title,"");
        String dir=od.getDirectory();
        String name=od.getFileName();
        Opener op=new Opener();
        ImagePlus impl=op.openImage(dir+name);
        return impl;
    }
}
