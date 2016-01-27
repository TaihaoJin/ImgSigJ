/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.DataClasses;
import utilities.CommonMethods;
import utilities.Constants;
import BrainSimulations.DataClasses.BrainStructureNameNode;
import BrainSimulations.DataClasses.BrainStructureNameHistNode;
import ij.ImagePlus;
import ImageAnalysis.rgbHistogram;
import utilities.CustomDataTypes.intRange;
/**
 *
 * @author Taihao
 */
public class BrainSimulationGraphicalObjectProperty {
    public String StructureName;
    public String Abbreviation;
    public String ParentStruct;
    public int red;
    public int green;
    public int blue;
    public int informaticsId,StructureId;
    public double[] rHist, gHist, bHist;
    public intRange rRange,gRange,bRange;
    public boolean bShow;
    public boolean bConnectDiagonally;
    public boolean bRegisteringGenerally;//register the object to all graphical
    public String HistImageFileName;
        //grid points.
    public BrainSimulationGraphicalObjectProperty(){
        StructureName=null;
        Abbreviation=null;
        ParentStruct=null;
        red=0;
        green=0;
        blue=0;
        informaticsId=-1;
        StructureId=-1;//StructureId=-1 is for background, StructureId=-2 is for bright object.
        rRange=new intRange();
        gRange=new intRange();
        bRange=new intRange();
        rHist=null;
        gHist=null;
        bHist=null;
        bShow=true;
        bConnectDiagonally=true;
        bRegisteringGenerally=false;
        HistImageFileName=null;
    }

    public BrainSimulationGraphicalObjectProperty(BrainSimulationGraphicalObjectProperty gop){
        StructureName=gop.StructureName;
        Abbreviation=gop.Abbreviation;
        ParentStruct=gop.ParentStruct;
        red=gop.red;
        green=gop.green;
        blue=gop.blue;
        informaticsId=gop.informaticsId;
        StructureId=gop.StructureId;
        rRange=new intRange(gop.rRange);
        gRange=new intRange(gop.gRange);
        bRange=new intRange(gop.bRange);
        rHist=new double[256];
        gHist=new double[256];
        bHist=new double[256];
        for(int i=0;i<=255;i++){
            rHist[i]=gop.rHist[i];
            gHist[i]=gop.gHist[i];
            bHist[i]=gop.bHist[i];
        }
        bShow=gop.bShow;
        bConnectDiagonally=gop.bConnectDiagonally;
        bRegisteringGenerally=gop.bRegisteringGenerally;
        HistImageFileName=gop.HistImageFileName;
    }
    public void setStructureName(String name){
        StructureName=name;
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
    public void setAbbreviation(String name){
        Abbreviation=name;
    }
    public void setParentStruct(String name){
        ParentStruct=name;
    }
    public void setinformaticsId(int n){
        informaticsId=n;
    }
    public void setStructureId(int n){
        StructureId=n;
    }
    public void setRed(int r, int tol){
        red=r;
        int min=r-tol;
        if(min<0)min=0;
        int max=r+tol;
        if(max>255) r=255;
        rRange=new intRange(min,max);
        rHist=CommonMethods.uniformHist(256, rRange);
    }
    public void setGreen(int g, int tol){
        green=g;
        int min=g-tol;
        if(min<0)min=0;
        int max=g+tol;
        if(max>255) g=255;
        gRange=new intRange(min,max);
        gHist=CommonMethods.uniformHist(256, gRange);
    }
    public void setBlue(int b, int tol){
        blue=b;
        int min=b-tol;
        if(min<0)min=0;
        int max=b+tol;
        if(max>255) b=255;
        bRange=new intRange(min,max);
        bHist=CommonMethods.uniformHist(256, bRange);
    }

    public BrainSimulationGraphicalObjectProperty(BrainStructureNameNode aNode){
        StructureName=aNode.StructureName;
        Abbreviation=aNode.Abbreviation;
        ParentStruct=aNode.ParentStruct;
        red=aNode.red;
        green=aNode.green;
        blue=aNode.blue;
        informaticsId=aNode.informaticsId;
        StructureId=aNode.StructureId;
        int tol=5;
        int rMin=red-tol;
        if(rMin<0)rMin=0;
        int rMax=red+tol;
        if(rMax>=255) rMax=255;

        int gMin=green-tol;
        if(gMin<0)gMin=0;
        int gMax=green+tol;
        if(gMax>=255) gMax=255;

        int bMin=blue-tol;
        if(bMin<0)bMin=0;
        int bMax=blue+tol;
        if(bMax>=255) bMax=255;

        rRange=new intRange(rMin,rMax);
        gRange=new intRange(gMin,gMax);
        bRange=new intRange(bMin,bMax);
        rHist=CommonMethods.TriangleHist(256, rRange);
        gHist=CommonMethods.TriangleHist(256, gRange);
        bHist=CommonMethods.TriangleHist(256, bRange);
    }
    public BrainSimulationGraphicalObjectProperty(BrainStructureNameHistNode aNode){
        StructureName=aNode.StructureName;
        Abbreviation=aNode.Abbreviation;
        ParentStruct=aNode.ParentStruct;
        red=aNode.red;
        green=aNode.green;
        blue=aNode.blue;
        informaticsId=aNode.informaticsId;
        StructureId=aNode.StructureId;
        HistImageFileName=aNode.imageFileName;
        if(HistImageFileName.contentEquals("null")){
            int tol=5;
            int rMin=red-tol;
            if(rMin<0)rMin=0;
            int rMax=red+tol;
            if(rMax>=255) rMax=255;

            int gMin=green-tol;
            if(gMin<0)gMin=0;
            int gMax=green+tol;
            if(gMax>=255) gMax=255;

            int bMin=blue-tol;
            if(bMin<0)bMin=0;
            int bMax=blue+tol;
            if(bMax>=255) bMax=255;

            rRange=new intRange(rMin,rMax);
            gRange=new intRange(gMin,gMax);
            bRange=new intRange(bMin,bMax);
            rHist=CommonMethods.TriangleHist(256, rRange);
            gHist=CommonMethods.TriangleHist(256, gRange);
            bHist=CommonMethods.TriangleHist(256, bRange);
        }else{
            ImagePlus impl0=CommonMethods.importImage(HistImageFileName);
            rgbHistogram his=new rgbHistogram(impl0);
            red=his.getAverageRed();
            green=his.getAverageGreen();
            blue=his.getAverageBlue();
            rRange=his.getRRange();
            gRange=his.getGRange();
            bRange=his.getBRange();
            rHist=new double[256];
            gHist=new double[256];
            bHist=new double[256];
            his.getRHist(rHist);
            his.getGHist(gHist);
            his.getBHist(bHist);
        }
    }
    public void setAsBackground(){
        StructureName="background";
        Abbreviation="bkg";
        ParentStruct=null;
        StructureId=-1;
        red=200;
        green=200;
        blue=200;
        informaticsId=0;
        rRange=new intRange(0,255);
        gRange=new intRange(0,255);
        bRange=new intRange(0,255);
        rHist=defaultHist();
        gHist=defaultHist();
        bHist=defaultHist();
        bRegisteringGenerally=true;
        bShow=true;
    }

    public void setAsBrightObject(int brightness){
        StructureName="Bright Object";
        Abbreviation="brt";
        ParentStruct=null;
        StructureId=-2;
        red=255;
        green=255;
        blue=255;
        informaticsId=0;
        rRange=new intRange(brightness,255);
        gRange=new intRange(brightness,255);
        bRange=new intRange(brightness,255);
        rHist=defaultHist();
        gHist=defaultHist();
        bHist=defaultHist();
        bShow=true;
        bRegisteringGenerally=true;
    }

    double[] defaultHist(){
        return CommonMethods.uniformHist(256, new intRange(0,255));
    }

    public double MatchingProb_LogicalOr(int pixel){
        double prob=0.;
        int rgb[];
        rgb=CommonMethods.intTOrgb(pixel);
        int r=rgb[0],g=rgb[1],b=rgb[2];
        return MatchingProb_LogicalOr(r, g, b);
    }
    public double MatchingProb_Sum(int pixel){
        double prob=0.;
        int rgb[];
        rgb=CommonMethods.intTOrgb(pixel);
        int r=rgb[0],g=rgb[1],b=rgb[2];
        return MatchingProb_Sum(r, g, b);
    }

    public double MatchingProb_Sum(int r, int g, int b){
        double prob;
        int sum;
        switch (StructureId){
            case -1://backbround
                prob=1./Constants.largestFloat;
                break;
            case -2://prob of being a bright object
                sum=r+g+b;
                if(sum>255) sum=255;
                if(rRange.contains(sum))prob=1.;
                else prob=0.;
                break;
            default:
                if(!rRange.contains(r)) return 0.;
                if(!rRange.contains(g)) return 0.;
                if(!rRange.contains(b)) return 0.;
                prob=rHist[r]+gHist[g]+bHist[b];
                prob=prob/3.;
                break;
        }
        return prob;
    }
    public double MatchingProb_LogicalOr(int r, int g, int b){
        double prob;
        switch (StructureId){
            case -1://backbround
                prob=1./Constants.largestFloat;
                break;
            case -2://prob of being a bright object
                if(rRange.contains(r)||gRange.contains(g)||bRange.contains(b))prob=1.;
                else prob=0.;
                break;
            default:
                if(!rRange.contains(r)) return 0.;
                if(!rRange.contains(g)) return 0.;
                if(!rRange.contains(b)) return 0.;
                prob=rHist[r]+gHist[g]+bHist[b];
                prob=prob/3.;
                break;
        }
        return prob;
    }
}
