/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Process;
import ij.ImagePlus;
import ij.WindowManager;
import utilities.CommonMethods;
import java.awt.Point;
import ImageAnalysis.LandscapeAnalyzer;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class sharpPeak_smoother implements ij.plugin.PlugIn{
    public void run(String arg){
        smoothPeakSpots();
    }
    void smoothPeakSpots(){
        ImagePlus impl=WindowManager.getCurrentImage();
        smoothPeakSpots(impl);

    }
    public static void smoothPeakSpots(ImagePlus impl){
        int[][] pixels;
        int num=impl.getStackSize();
        int i,j;
        for(i=1;i<=num;i++){
            impl.setSlice(i);
            pixels=CommonMethods.getPixelValues(impl);
            smoothPeakSpots(pixels);
            CommonMethods.setPixels(impl, pixels);
        }
    }
    public static void smoothPeakSpots(int[][] pixels){
        ArrayList<Point> localMaxima=CommonMethods.getSpecialLandscapePoints(pixels, LandscapeAnalyzer.localMaximum);
        int len=localMaxima.size(),i,x,y,pixel,nx,dvx;
        Point pt;
        int[] pvd=new int[2],pIV=new int[8];
        for(i=0;i<len;i++){
            pt=localMaxima.get(i);
            x=pt.x;
            y=pt.y;
            if(x==53&&y==60){
                x=x;
            }

            pIV[0]=pixels[y-1][x-1];
            pIV[1]=pixels[y-1][x];
            pIV[2]=pixels[y-1][x+1];
            pIV[3]=pixels[y][x-1];
            pIV[4]=pixels[y][x+1];
            pIV[5]=pixels[y+1][x-1];
            pIV[6]=pixels[y+1][x];
            pIV[7]=pixels[y+1][x+1];
            
//            getMaxValueAndDiff(pIV,8,pvd);
            getMaxValueMeanDiff(pIV,8,pvd);

            pixel=pixels[y][x];
            nx=pvd[0];
            dvx=pvd[1];
            if(pixel-nx>dvx) pixel=nx+dvx;
            pixels[y][x]=pixel;
        }
    }
    public static void getMaxValueAndDiff(int[] pIV, int len, int[] pvd){
        int i,j;
        int nx=pIV[0],ndx=Math.abs(pIV[0]-pIV[1]),nv,nd;
        for(i=0;i<len;i++){
            nv=pIV[i];
            if(nv>nx)nx=nv;
        }
        for(i=0;i<len;i++){
            for(j=i+1;j<len;j++){
                nd=pIV[i]-pIV[j];
                if(nd<0) nd=-nd;
                if(nd>ndx) ndx=nd;
            }
        }
        pvd[0]=nx;
        pvd[1]=ndx;
    }
    public static void getMaxValueMinDiff(int[] pIV, int len, int[] pvd){
        int i,j;
        int nx=pIV[0],ndn=Math.abs(pIV[0]-pIV[1]),nv,nd;
        for(i=0;i<len;i++){
            nv=pIV[i];
            if(nv>nx)nx=nv;
        }
        for(i=0;i<len;i++){
            for(j=i+1;j<len;j++){
                nd=pIV[i]-pIV[j];
                if(nd<0) nd=-nd;
                if(nd<ndn) ndn=nd;
            }
        }
        pvd[0]=nx;
        pvd[1]=ndn;
    }
    public static void getMaxValueMeanDiff(int[] pIV, int len, int[] pvd){
        int i,j;
        int nx=pIV[0],mean=0,nv,nd,num=0;
        for(i=0;i<len;i++){
            nv=pIV[i];
            if(nv>nx)nx=nv;
        }
        for(i=0;i<len;i++){
            for(j=i+1;j<len;j++){
                nd=pIV[i]-pIV[j];
                if(nd<0) nd=-nd;
                mean+=nd;
                num++;
            }
        }
        mean/=num;
        pvd[0]=nx;
        pvd[1]=mean;
    }
}
