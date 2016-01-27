/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import ij.plugin.PlugIn;
import ij.gui.Roi;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.Rectangle;
import ij.io.OpenDialog;
import java.io.File;
import java.io.FileNotFoundException;
import ij.IJ;
import ij.ImageStack;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import java.util.*;
import java.io.*;
/*
 *
 * @author Taihao
 */
public class Zhengji_Plot implements PlugIn{
    public void run(String arg){
        try{ZhengjiPlot();}
        catch(IOException e){
            IJ.error("");
        }
    }

    void ZhengjiPlot()throws IOException{
        String newline = System.getProperty("line.separator");
        ImagePlus impl=WindowManager.getCurrentImage();
        int W=0,H=0,Z=0,x,y,z;

        OpenDialog od=new OpenDialog("Export the pixels into a file","");
        String path=od.getDirectory()+od.getFileName();

//        byte pixels[];
        int pixels[];
        double values[];

        File file = new File(path);
        FileReader f=null;
//        FileReader f=new FileReader(file);
        try{f=new FileReader(file);}
        catch(FileNotFoundException e){
            IJ.error("");
        }
        BufferedReader br=new BufferedReader(f);
        int m1,m2,m3;
        br.readLine();
        String s=br.readLine();
        StringTokenizer st0=new StringTokenizer(s," ",false);
        Integer IT=new Integer(st0.nextToken());
        m3=IT.intValue();
        IT=new Integer(st0.nextToken());
        m2=IT.intValue();
        IT=new Integer(st0.nextToken());
        m1=IT.intValue();
        br.readLine();
        Double DT;

        Z=m1;
        H=m2;
        W=m3;
        int i,j,k;
        String sTemp;
        int oz,oy;
        int nResample=4;
        int w=1+(W-1)/nResample,h=1+(H-1)/nResample,n=1+(Z-1)/nResample;
//        pixels=new byte[len];
        int len=w*h*n;
        pixels=new int[len];
        values=new double[len];
        int scale=1;
        int x0,y0,z0;
        for(x=0;x<m3;x++){
            x0=x/nResample;
            for(y=0;y<m2;y++){
                y0=y/nResample;
                oy=y0*w;
                for(z=0;z<m1;z++){
                    z0=z/nResample;
                    oz=z0*w*h;
                    if(!st0.hasMoreTokens())
                    {
                        s=br.readLine();
                        st0=new StringTokenizer(s," ",false);
                    }
                    sTemp=st0.nextToken();
                    DT=new Double(sTemp);
                    if(x%nResample==0&&y%nResample==0&&z%nResample==0){
                        if(oz+oy+x0>w*h*n){
                            n=n;
                        }
                        values[oz+oy+x0]=DT;
                    }
                }
            }
        }
        Double Dscale=convertToPixels(values,pixels);
        ImageStack is=new ImageStack(w,h);
        int[] nPixels;
        for(z=0;z<n;z++) {
            ColorProcessor bp=new ColorProcessor(w,h);
            nPixels=(int[])bp.getPixels();
            oz=z*w*h;
            for(y=0;y<h;y++){
                oy=y*w;
                for(x=0;x<w;x++){
                    nPixels[oy+x]=pixels[oz+oy+x];
                }
            }
            is.addSlice("z="+z, bp);
        }
        impl=new ImagePlus("Charge Density scale= "+Dscale.toString(),is);
        impl.show();
    }
    double convertToPixels(double values[], byte pixels[]){
        int len=values.length;
        int i;
        double dn=99999999999.;
        double dp=-dn,dx=0.;
        double v=0.;
        double dn2=dn;
        for(i=0;i<len;i++){
            v=values[i];
            if(v>0){
                if(v>dp) dp=v;
            }else{
                if(v<dn) dn=v;
            }
        }
        dx=dp;
        if(-dn>dp) dx=-dn;
        for(i=0;i<len;i++){
            v=values[i];
            pixels[i]=(byte)((int)(245*(v-dn)/(dx-v))+10);
        }
        return dx/255;
    }
    double convertToPixels(double values[], int pixels[]){
        int len=values.length;
        int i;
        double dn=99999999999.;
        double dp=-dn,dx=0.;
        double v=0.;
        double dp2=dp;
        for(i=0;i<len;i++){
            v=values[i];
            if(v>0){
                if(v>dp){
                    dp=v;
                    dp2=dp;
                }
                if(v>dp2)dp2=v;
            }else{
                if(v<dn) dn=v;
            }
        }
        dx=dp;
        int n=0;
        if(-dn>dp) dx=-dn;
        int r=0,g=0,b=0;
        for(i=0;i<len;i++){
            r=0;
            b=0;
            v=values[i];
            if(v>0){
                b=(int)(255*v/dx);
                if(b>=200){
                    n++;
                }
            }else{
                r=(int)(-255*v/dx);
            }
            pixels[i]=(r<<16)|(g<<8)|b;
        }
        return dx/255;
    }
}
