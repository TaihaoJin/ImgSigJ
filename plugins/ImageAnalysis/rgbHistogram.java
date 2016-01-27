/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ij.ImagePlus;
import utilities.CustomDataTypes.intRange;
import ij.process.ColorProcessor;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class rgbHistogram {
    intRange rRange, gRange, bRange;
    double[] rHist, gHist, bHist;
    int red,green,blue;
    public rgbHistogram(ImagePlus impl0){
        if(impl0.getType()==CommonMethods.COLOR_RGB){
            rHist=new double[256];
            gHist=new double[256];
            bHist=new double[256];
            int h=impl0.getHeight();
            int w=impl0.getWidth();
            int n=w*h;
            byte[] R=new byte[n],B=new byte[n],G=new byte[n];
            int pixels[]=(int[])impl0.getProcessor().getPixels();
            ColorProcessor cp=new ColorProcessor(w,h,pixels);
            cp.getRGB(R,G,B);  
            for(int i=0;i<256;i++){
                rHist[i]=0.;
                gHist[i]=0.;
                bHist[i]=0.;
            }
            for(int i=0;i<n;i++){
                rHist[0xff & R[i]]+=1.;
                gHist[0xff & G[i]]+=1.;
                bHist[0xff & B[i]]+=1.;
            }
            rRange=new intRange();
            gRange=new intRange();
            bRange=new intRange();
            red=0;
            green=0;
            blue=0;
            for(int i=0;i<256;i++){
                if(rHist[i]>0.1) rRange.expandRange(i);
                if(gHist[i]>0.1) gRange.expandRange(i);
                if(bHist[i]>0.1) bRange.expandRange(i);
                red+=i*rHist[i];
                green+=i*gHist[i];
                blue+=i*bHist[i];
                rHist[i]/=n;
                gHist[i]/=n;
                bHist[i]/=n;
            }
            red/=n;
            green/=n;
            blue/=n;            
        }
    }
    public intRange getRRange(){
        return rRange;
    }
    public intRange getGRange(){
        return gRange;
    }
    public intRange getBRange(){
        return bRange;
    }
    public double[] getRHist(){
        return rHist;
    }
    public double[] getGHist(){
        return gHist;
    }
    public double[] getBHist(){
        return bHist;
    }
    public void getRHist(double his[]){
        for(int i=0;i<256;i++){
            his[i]=rHist[i];
        }
    }
    public void getGHist(double his[]){
        for(int i=0;i<256;i++){
            his[i]=gHist[i];
        }
    }
    public void getBHist(double his[]){
        for(int i=0;i<256;i++){
            his[i]=bHist[i];
        }
    }
    public int getAverageRed(){
        return red;
    }
    public int getAverageGreen(){
        return green;
    }
    public int getAverageBlue(){
        return blue;
    }
}
