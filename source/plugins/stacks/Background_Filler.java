package stacks;

/*
 * @author Taihao
 */

import ij.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.*;
import ij.process.ByteProcessor;
import ij.io.Opener;
import ij.io.OpenDialog;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import utilities.CustomDataTypes.intRange;
import java.awt.geom.*;
import java.util.ArrayList;
//import utilities.Geometry.ImageTransform2D;

public class Background_Filler  implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */
    public void run(String arg) {
        ImagePlus impl=importImage("import the image stack to fill the background, gray8 only");//TODO: need to implement image stack type checking
        impl.show();
        fillBackground(impl);
    }

    void fillBackground(ImagePlus impl){
        int i,n=impl.getStackSize(),w=impl.getWidth(),h=impl.getHeight();
        byte[] pixels;
        for(i=1;i<=n;i++){
            impl.setSlice(i);
            pixels=(byte[])impl.getProcessor().getPixels();
            fillBackground(pixels,w,h);
        }
    }

    void fillBackground(byte[] pixels, int w, int h){
        int ws=5,ws1=1,x,y,dx,dy,pixel,pmax,mx,my,o;
        int low=20,entry=80,high=100;
        int noise=high-entry;
        int wx,wy;
        for(y=0;y<h-ws;y+=ws){
            for(x=0;x<w-ws;x+=ws){
                pmax=0;
                mx=x;
                my=y;
                for(dy=0;dy<ws;dy++){
                    o=w*(y+dy);
                    for(dx=0;dx<ws;dx++){
                        pixel=0xff&pixels[o+x+dx];
                        if(pixel>pmax){
                            pmax=pixel;
                            mx=x+dx;
                            my=y+dy;
                        }
                    }
                }
                if(pmax<entry){
                    for(wy=y;wy<y+ws-ws1;wy+=ws1){
                        for(wx=x;wx<x+ws-ws1;wx+=ws1){
                            pmax=0;
                            mx=x;
                            my=y;
                            for(dy=0;dy<ws1;dy++){
                                o=w*(wy+dy);
                                for(dx=0;dx<ws1;dx++){
                                    pixel=0xff&pixels[o+wx+dx];
                                    if(pixel>pmax){
                                        pmax=pixel;
                                        mx=wx+dx;
                                        my=wy+dy;
                                    }
                                }
                            }
                            if(pmax>low&&pmax<entry){
                                pixel=entry+(int)(noise*Math.random()+0.5);
                                pixels[my*w+mx]=(byte)pixel;
                            }
                        }
                    }
                }
            }
        }
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
