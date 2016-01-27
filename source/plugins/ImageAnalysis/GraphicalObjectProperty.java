/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import utilities.CommonMethods;
import utilities.Constants;
//import ij.ImagePlus;
import ImageAnalysis.rgbHistogram;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class GraphicalObjectProperty {
    public int red;
    public int green;
    public int blue;
    public intRange rRange,gRange,bRange;
    public intRange intensityRange;
    public boolean bShow;
    public boolean bConnectDiagonally;
    public String name;
    public GraphicalObjectProperty(){
        red=0;
        green=0;
        blue=0;
        rRange=new intRange();
        gRange=new intRange();
        bRange=new intRange();
        intensityRange=new intRange();
        bShow=true;
        bConnectDiagonally=true;
    }
    public GraphicalObjectProperty(GraphicalObjectProperty gop){
        red=gop.red;
        green=gop.green;
        blue=gop.blue;
        rRange=new intRange(gop.rRange);
        gRange=new intRange(gop.gRange);
        bRange=new intRange(gop.bRange);
        bShow=gop.bShow;
        bConnectDiagonally=gop.bConnectDiagonally;
    }
    public void connectDiagonally(boolean b){
        bConnectDiagonally=b;
    }
    public boolean showing(){
        return bShow;
    }
    public void show(){
        bShow=true;
    }
    public void hide(){
        bShow=false;
    }
    public void setRed(int r, int tol){
        red=r;
        int min=r-tol;
        if(min<0)min=0;
        int max=r+tol;
        if(max>255) r=255;
        rRange=new intRange(min,max);
    }
    public void setGreen(int g, int tol){
        green=g;
        int min=g-tol;
        if(min<0)min=0;
        int max=g+tol;
        if(max>255) g=255;
        gRange=new intRange(min,max);
    }
    public void setBlue(int b, int tol){
        blue=b;
        int min=b-tol;
        if(min<0)min=0;
        int max=b+tol;
        if(max>255) b=255;
        bRange=new intRange(min,max);
    }
    double[] defaultHist(){
        return CommonMethods.uniformHist(256, new intRange(0,255));
    }

    public double MatchingProb_LogicalAnd(int pixel){
        double prob=0.;
        int rgb[];
        rgb=CommonMethods.intTOrgb(pixel);
        int r=rgb[0],g=rgb[1],b=rgb[2];
        return MatchingProb_LogicalAnd(r, g, b);
    }
    public double MatchingProbGRAY(int pixel){
        double prob=0.;
        if(intensityRange.contains(0xff&pixel)) prob=1.;
        return prob;
    }
    public double MatchingProb_Sum(int pixel){
        double prob=0.;
        int rgb[];
        rgb=CommonMethods.intTOrgb(pixel);
        int r=rgb[0],g=rgb[1],b=rgb[2];
        return MatchingProb_Sum(r, g, b);
    }
    public boolean MatchingRGB(int pixel){
        int rgb[];
        rgb=CommonMethods.intTOrgb(pixel);
        int r=rgb[0],g=rgb[1],b=rgb[2];
        if(rRange.contains(r)&&gRange.contains(g)&&bRange.contains(b)) return true;
        return false;
    }
    public void setAsBackground(){
        name=new String("background");
        red=0;
        green=0;
        blue=0;
        rRange=new intRange(0,255);
        gRange=new intRange(0,255);
        bRange=new intRange(0,255);
        bShow=true;
        intensityRange=new intRange(0,255);
        bConnectDiagonally=false;
    }
    public boolean MatchingGRAY(int pixel){
        if(intensityRange.contains(0xff&pixel)) return true;
        return false;
    }
    public double MatchingProb_Sum(int r, int g, int b){
        double prob=0.;
        if(rRange.contains(r)) prob+=1.;
        if(gRange.contains(g)) prob+=1.;
        if(bRange.contains(b)) prob+=1.;
        prob=prob/3.;
        return prob;
    }
    public double MatchingProb_LogicalAnd(int r, int g, int b){
        double prob;
        if(!rRange.contains(r)) return 0.;
        if(!rRange.contains(g)) return 0.;
        if(!rRange.contains(b)) return 0.;
        return 1.;
    }
}
