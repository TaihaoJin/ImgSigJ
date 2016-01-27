/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ij.plugin.filter.RankFilters;
import utilities.Geometry.ImageShapes.SubpixelAreaInRingsLUT;
import ImageAnalysis.IPOPixelHeights;
import ImageAnalysis.Subpixel.IPOPixelHeights_Subpixel;
import ImageAnalysis.Subpixel.IPOPixelHeights_Subpixel_Iterative;
import java.util.Formatter;
import utilities.QuickFormatter;
import utilities.io.FileAssist;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import utilities.CommonGuiMethods;
import java.util.ArrayList;
import utilities.CommonMethods;
import java.awt.Point;
import ImageAnalysis.LandscapeAnalyzer;
import ImageAnalysis.PixelHeights;
import utilities.Geometry.ImageShapes.*;
import utilities.CustomDataTypes.intRange;
import utilities.CommonStatisticsMethods;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import ij.IJ;
import java.io.DataInputStream;
import java.io.FileInputStream;
import utilities.io.PrintAssist;
import java.io.BufferedInputStream;
import utilities.io.ByteConverter;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.statistics.MeanSem0;
import utilities.QuickSort;
import utilities.statistics.GaussianDistribution;
import utilities.Geometry.CocentricCircles_Rings;
import utilities.statistics.MeanSem1;
import utilities.statistics.MeanSemFractional;
import utilities.statistics.MeanSemFractional0;
import utilities.Categorizer.Categorizer;
/**
 *
 * @author Taihao
 */
public class IPOPixelHeightsHandler {
//    static final int nRRefI=5, nRRefO=8, nRMax=8;
/*    public static int detectIPOs(int[][] pixels, int[][] pixelsm, int[][]pixelsmr, ArrayList<Point> positions, ArrayList<Double> pixelHeights, ArrayList<Double> pixelHeights0, int nRRefI, int nRRefO, int nRMax, double[] backgroundPercentiles, int[] percentileIndexes){
        int nIPOs;
        IPOPixelHeights c=new IPOPixelHeights(pixelsm,CommonMethods.getSpecialLandscapePoints(pixelsm, LandscapeAnalyzer.localMaximum), nRRefI, nRRefO, nRMax);
        int lenm, lenmr;
        c.calRefinedPixelHeights();

        c.getLocalMaxima(positions);
        lenm=positions.size();
        double[] pixelHeights0m=new double[lenm];
        c.getPixelHeights0(pixelHeights0m);

        c.updatePixels(pixels);
        c.calCompensatedPixels();
        c.refinePixelStatitics();
        c.getPixelHeights0(pixelHeights0);
        c.getPixelHeights(pixelHeights);

        IPOPixelHeights cr=new IPOPixelHeights(pixelsmr,CommonMethods.getSpecialLandscapePoints(pixelsmr, LandscapeAnalyzer.localMaximum), nRRefI, nRRefO, nRMax);
        cr.calRefinedPixelHeights();

        lenmr=cr.getNumLocalMaxima();
        double[] pixelHeights0mr=new double[lenmr];
        cr.getPixelHeights0(pixelHeights0mr);

        double dp,hc;
        int len=backgroundPercentiles.length,i,index,nf=pixelHeights0m.length-1,j;
        for(i=0;i<len;i++){
            dp=backgroundPercentiles[i];
            index=(int)((1-dp)*lenmr);
            hc=pixelHeights0mr[index];
            for(j=nf-1;j>=0;j--){
                if(pixelHeights0m[j]<hc) {
                    percentileIndexes[i]=j+1;
                    nf=j+1;
                    break;
                }
            }
        }

        nIPOs=0;
        return nIPOs;
    }*/
    public static void detectIPOs(String path, String pathr, ArrayList<Integer> nvRadius, ArrayList<Integer> nvNumLocalMaxima, ArrayList<Point> cvLocalMaxima,
            ArrayList<Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC, double[] backgroundPercentiles,
            ArrayList<int[]> npvPercentileIndexes){//based on the ranking of the reference IPOs.
        ArrayList<Integer> nvNumLocalMaximar=new ArrayList();
        ArrayList<Point> cvLocalMaximar=new ArrayList();
        ArrayList<Double> dvPixelHeightsr=new ArrayList();
        ArrayList<Double> dvPixelHeightsCr=new ArrayList();
        ArrayList<Double> dvPixelHeights0r=new ArrayList();
        npvPercentileIndexes.clear();

        IPOPixelHeightsHandler.importStackPixelHeights(path, nvRadius, nvNumLocalMaxima, cvLocalMaxima, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC);
        IPOPixelHeightsHandler.importStackPixelHeights(pathr, nvRadius, nvNumLocalMaximar, cvLocalMaximar, dvPixelHeightsr, dvPixelHeights0r, dvPixelHeightsCr);

        double dp,hc,hcr;
        int len=backgroundPercentiles.length,i,indexr,nf,j;
        int nSlices=nvNumLocalMaxima.size(),slice;
        int offset=0,offsetr=0;
        int lenm,lenmr;
        for(slice=0;slice<nSlices;slice++){
            lenm=nvNumLocalMaxima.get(slice);
            lenmr=nvNumLocalMaximar.get(slice);
            nf=offset+lenm-1;
            int[] percentileIndexes=new int[len];
            for(i=0;i<len;i++){
                dp=backgroundPercentiles[i];
                indexr=(int)((1-dp)*lenmr);
                hcr=dvPixelHeights0r.get(offsetr+indexr);
                for(j=nf-1;j>=offset;j--){
                    hc=dvPixelHeights0.get(j);
                    if(hc<hcr) {
                        percentileIndexes[i]=j+1-offset;
                        nf=j+1;
                        break;
                    }
                }
            }
            offset+=lenm;
            offsetr+=lenmr;
            npvPercentileIndexes.add(percentileIndexes);
       }
    }

    public static void detectIPOs_PixelHeights(String path, String pathp, String pathr, ArrayList<Integer> nvRadius, ArrayList<Integer> nvNumLocalMaxima, ArrayList<Point> cvLocalMaxima,
            ArrayList<Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC, ArrayList<MeanSem0> dvBackgroundPixelValues,
            ArrayList<Double> dvBackgroundPixels, ArrayList<MeanSem0> cvRefMS, double[] backgroundPercentiles,
            ArrayList<int[]> npvPercentileIndexes, ArrayList<double[]> dpvPercentileCutoff,
            int detectionMode, int nBackgroundOption, int cutoffSmoothingWS, int nFirstFrame, int w, int h){//detectionMode 0 for pixelHeight0, 1 for pixelHeight, 2 for pixelHeightC
        ArrayList<Integer> nvNumLocalMaximar=new ArrayList();
        ArrayList<Point> cvLocalMaximar=new ArrayList();
        ArrayList<Double> dvPixelHeightsr=new ArrayList();
        ArrayList<Double> dvPixelHeightsCr=new ArrayList();
        ArrayList<Double> dvPixelHeights0r=new ArrayList();
        ArrayList<Double> pdvPixelHeights[]=new ArrayList[3];
        ArrayList<Double> pdvPixelHeightsr[]=new ArrayList[3];
        npvPercentileIndexes.clear();

        IPOPixelHeightsHandler.importStackPixelHeights(path, nvRadius, nvNumLocalMaxima, cvLocalMaxima, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC);
        IPOPixelHeightsHandler.importStackPixelHeights(pathr, nvRadius, nvNumLocalMaximar, cvLocalMaximar, dvPixelHeightsr, dvPixelHeights0r, dvPixelHeightsCr);

        IPOPixelHeightsHandler.importBackgroundPixels(pathp, dvBackgroundPixelValues, nBackgroundOption);

        if(nBackgroundOption!=0){//adjusting the pixel heights
            ArrayList<MeanSem0> background0=new ArrayList(), background1=new ArrayList();
            MeanSem0 ms0,ms1;
            int[] pnRadii=new int[3];
            importRingRadius(path,pnRadii);
            ImageShape shape=new CircleImage(pnRadii[0]);
            shape.setFrameRanges(new intRange(0,w-1),new intRange(0,h-1));
            int num=cvLocalMaxima.size(),numr=cvLocalMaximar.size();
            ArrayList <Point> innerPoints=new ArrayList();
            int n;

            double ph;
            Point pt;

            importBackgroundPixels(path,background0,0);
            importBackgroundPixels(path,background1,nBackgroundOption);
            for(int i=0;i<num;i++){
                pt=cvLocalMaxima.get(i);
                shape.setCenter(pt);
                shape.getInnerPoints(innerPoints);
                n=innerPoints.size();

                ph=dvPixelHeights.get(i);
                ms0=background0.get(i);
                ms1=background1.get(i);
                ph+=ms0.mean*n;
                ph-=ms1.mean*n;
                dvBackgroundPixels.add(ms1.mean);

                dvPixelHeights.set(i, ph);
/*
                ph=dvPixelHeightsC.get(i);
                ms0=dvBackgroundPixelValues.get(i);
                ms1=background1.get(i);
                ph+=ms0.mean*n;
                ph-=ms1.mean*n;

                dvPixelHeightsC.set(i, ph);*/
            }

            importBackgroundPixels(pathr,background0,0);
            importBackgroundPixels(pathr,background1,nBackgroundOption);
            for(int i=0;i<numr;i++){
                pt=cvLocalMaximar.get(i);
                shape.setCenter(pt);
                shape.getInnerPoints(innerPoints);
                n=innerPoints.size();

                ph=dvPixelHeightsr.get(i);
                ms0=background0.get(i);
                ms1=background1.get(i);
                ph+=ms0.mean*n;
                ph-=ms1.mean*n;
                dvPixelHeightsr.set(i, ph);
            }
        }else{
            ArrayList<MeanSem0> background0=new ArrayList();
            importBackgroundPixels(path,background0,0);
            int num=cvLocalMaxima.size();
            for(int i=0;i<num;i++){
                dvBackgroundPixels.add(background0.get(i).mean);
            }
        }

        pdvPixelHeights[0]=dvPixelHeights0;
        pdvPixelHeights[1]=dvPixelHeights;
        pdvPixelHeights[2]=dvPixelHeightsC;

        pdvPixelHeightsr[0]=dvPixelHeights0r;
        pdvPixelHeightsr[1]=dvPixelHeightsr;
        pdvPixelHeightsr[2]=dvPixelHeightsCr;

        double dp,hc,hcr;
        MeanSem0 ms;
        GaussianDistribution gd;
        int len=backgroundPercentiles.length,i,indexr,nf,j;
        int nSlices=nvNumLocalMaxima.size(),slice;
        int offset=0,offsetr=0;
        int lenm,lenmr,nfr;
        double mean, sd;
        int nf0;
        cvRefMS.clear();
        for(slice=0;slice<nSlices;slice++){
            lenmr=nvNumLocalMaximar.get(slice);
            nfr=offsetr+lenmr-1;
//            ms=CommonStatisticsMethods.buildMeanSem(dvPixelHeightsr,offsetr,nfr,1);10801
            ms=CommonStatisticsMethods.buildMeanSem(pdvPixelHeightsr[detectionMode],offsetr,nfr,1);
            cvRefMS.add(ms);
            offsetr+=lenmr;
        }
        CommonStatisticsMethods.smoothMeanSemArray(cvRefMS, cutoffSmoothingWS, nFirstFrame, nSlices-1);
        offset=0;
        offsetr=0;
        for(slice=0;slice<nSlices;slice++){
            lenm=nvNumLocalMaxima.get(slice);
            lenmr=nvNumLocalMaximar.get(slice);
            nf0=offset+lenm-1;
            nf=nf0;
            nfr=offsetr+lenmr-1;
            ms=cvRefMS.get(slice);
            mean=ms.mean;
            sd=ms.getSD();
//            CommonMethods.sortArrays(cvLocalMaxima, dvPixelHeights0, dvPixelHeights, offset, nf);10801
            CommonMethods.sortArrays(cvLocalMaxima, pdvPixelHeights, offset, nf, detectionMode);
            int[] percentileIndexes=new int[len];
            double[] percentileCutoff=new double[len];
            for(i=0;i<len;i++){
                dp=backgroundPercentiles[i];
//                indexr=(int)((1-dp)*lenmr);
//                hcr=dvPixelHeights0r.get(offsetr+indexr);
                hcr=GaussianDistribution.getZatP(1.-dp, mean, sd, 0.01);
                percentileCutoff[i]=hcr;
                for(j=nf;j>=offset;j--){
                    hc=dvPixelHeights.get(j);
                    if(hc<hcr) {
                        percentileIndexes[i]=j+1-offset;
                        nf=j+1;
                        if(nf>nf0) nf=nf0;
                        break;
                    }
                }
            }
            offset+=lenm;
            offsetr+=lenmr;
            npvPercentileIndexes.add(percentileIndexes);
            dpvPercentileCutoff.add(percentileCutoff);
       }
    }

    public static void importRingMeansems(String path, int nFrameNumber, ArrayList <Point> localMaxima, ArrayList<MeanSem0[]> cvRingMeansems, ArrayList<MeanSem0[]> cvRingMeansems_Group, ArrayList<MeanSem0[]> cvRingMeansems_Normalized){
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
            readRingMeansems(ds,nFrameNumber,localMaxima, cvRingMeansems,cvRingMeansems_Group,cvRingMeansems_Normalized);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }
    }
    public static void importRingMeansems_Subpixel(String path, int nFrameNumber, ArrayList <double[]> IPOCenters, ArrayList <int[]> DistsToBorders,
            ArrayList<MeanSemFractional0[]> cvRingMeansems, ArrayList<MeanSemFractional0[]> cvRingMeansems_Group, ArrayList<MeanSemFractional0[]> cvRingMeansems_Normalized,
            ArrayList<MeanSemFractional0[]> cvRingMeansems_background, ArrayList<MeanSemFractional0[]> cvRingMeansems_Group_background, 
            ArrayList<MeanSemFractional0[]> cvRingMeansems_Normalized_background,
            ArrayList<Integer> nvOverlappingSatelliteIndexes,ArrayList<Integer> nvSatelliteLMIndexes,
            ArrayList<Double> vdGroupingDelimiters){
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
            readRingMeansems_Subpixel(ds,nFrameNumber,IPOCenters, DistsToBorders,cvRingMeansems,cvRingMeansems_Group,cvRingMeansems_Normalized,
                    cvRingMeansems_background,cvRingMeansems_Group_background,cvRingMeansems_Normalized_background,
                    nvOverlappingSatelliteIndexes,nvSatelliteLMIndexes,vdGroupingDelimiters);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }
    }
    public static void importBackgroundPixels(String path, ArrayList<MeanSem0> dvBackgroundPixels, int nBackgroundOption){
        path=FileAssist.changeExt(path, "phi");
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
            readBackgroundPixels(ds,dvBackgroundPixels,nBackgroundOption);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }
    }
    public static void importRings(String path, ArrayList <ImageShape> cvRings, ArrayList<Double> dvRs){
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
         }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }

        int i,j,len,nRRefI,nRRefO,nRMax,nNumRings;

        int intLen=4, floatLen=4;
        byte[] bt1 =new byte[20];
        byte[] bt2 =new byte[4];

        try{
            ds.read(bt1, 0, 20);
        }catch (IOException e){

        }

        int[] ia1=new int[5];
        ByteConverter.getIntArray(bt1, 0, 20, ia1, 0, 5);
        nRRefI=ia1[0];
        nRRefO=ia1[1];
        nRMax=ia1[2];
        constructIPORings(nRMax,nRRefI,nRRefO,cvRings,dvRs);
    }

    public static void importRingRadius(String path, int[] pnRadii){
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
         }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }

        int i,j,len,nRRefI,nRRefO,nRMax,nNumRings;

        int intLen=4, floatLen=4;
        byte[] bt1 =new byte[20];
        byte[] bt2 =new byte[4];

        try{
            ds.read(bt1, 0, 20);
        }catch (IOException e){

        }

        int[] ia1=new int[5];
        ByteConverter.getIntArray(bt1, 0, 20, ia1, 0, 5);
        nRRefI=ia1[0];
        nRRefO=ia1[1];
        nRMax=ia1[2];
        pnRadii[0]=ia1[0];
        pnRadii[1]=ia1[1];
        pnRadii[2]=ia1[2];
    }

    public static void readRingMeansems(BufferedInputStream ds, int nFrameNumber, ArrayList <Point> localMaxima, ArrayList<MeanSem0[]> cvRingMeansems, ArrayList<MeanSem0[]> cvRingMeansems_Group, ArrayList<MeanSem0[]> cvRingMeansems_Normalized)throws IOException{
        int i,j,k,len,nNumRings;

        byte[] bt1 =new byte[24];
        byte[] bt2 =new byte[4];
        ds.read(bt1, 0, 24);

        int[] ia1=new int[6];
        ByteConverter.getIntArray(bt1, 0, 24, ia1, 0, 6);
        nNumRings=ia1[3];
        int nNumGroups=ia1[4];

        Point pt;
        localMaxima.clear();
        cvRingMeansems.clear();
        cvRingMeansems_Group.clear();
        cvRingMeansems_Normalized.clear();

        byte[] bt3=null;
        int offset=0;
        int pnXYs[]=null;
        float pfRingMeansems[]=null;
        int index;
        int fLeng,intLeng,bfLeng,fLeng0=0,intLeng0=0,bfLeng0=0;

        long lBytesToSkipForAdjustedPixels=0;
        byte[] pbBytesForAdjustedPixels=new byte[1];

        for(i=0;i<nFrameNumber;i++){
            if(i%10==1) IJ.showStatus("reading pixel heights i="+PrintAssist.ToString(i));
            ds.read(bt2);
            len=ByteConverter.toInteger(bt2, 0, 4);

            bfLeng=len*(2*4+3*4*nNumRings)+2*nNumGroups*nNumRings*3*4;
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);
        }

        int n;
        double mean,sd;
        for(i=nFrameNumber;i<=nFrameNumber;i++){
            ds.read(bt2);
            len=ByteConverter.toInteger(bt2, 0, 4);

            bfLeng=len*(2*4+3*4*nNumRings)+2*nNumGroups*nNumRings*3*4;
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);

            intLeng=2*len;
            if(intLeng>intLeng0){
                pnXYs=new int[intLeng];
                intLeng0=intLeng;
            }

            fLeng=3*len*nNumRings+2*nNumGroups*nNumRings*3;
            if(fLeng>fLeng0){
                pfRingMeansems=new float[fLeng];
                fLeng0=fLeng;
            }

            offset=0;
            ByteConverter.getIntArray(bt3, offset, 4*intLeng, pnXYs, 0, intLeng);
            offset+=4*intLeng;
            ByteConverter.getFloatArray(bt3, offset, 4*fLeng, pfRingMeansems, 0, fLeng);

            index=0;
            for(j=0;j<len;j++){
                pt=new Point();
                pt.x=pnXYs[index];
                index++;
                pt.y=pnXYs[index];
                index++;
                localMaxima.add(pt);
            }
            index=0;
            for(j=0;j<len;j++){
                MeanSem0[] pcMS=new MeanSem0[nNumRings];
                for(k=0;k<nNumRings;k++){
                    MeanSem0 ms=new MeanSem0();
                    n=(int)(pfRingMeansems[index]+.5);
                    index++;
                    mean=pfRingMeansems[index];
                    index++;
                    sd=pfRingMeansems[index];
                    index++;
                    ms.updateMeanSem2(n, mean, sd*sd);
                    pcMS[k]=ms;
                }
                cvRingMeansems.add(pcMS);
            }
            for(j=0;j<nNumGroups;j++){
                MeanSem0[] pcMS=new MeanSem0[nNumRings];
                for(k=0;k<nNumRings;k++){
                    MeanSem0 ms=new MeanSem0();
                    n=(int)(pfRingMeansems[index]+.5);
                    index++;
                    mean=pfRingMeansems[index];
                    index++;
                    sd=pfRingMeansems[index];
                    index++;
                    ms.updateMeanSem2(n, mean, sd*sd);
                    pcMS[k]=ms;
                }
                cvRingMeansems_Group.add(pcMS);
            }
            for(j=0;j<nNumGroups;j++){
                MeanSem0[] pcMS=new MeanSem0[nNumRings];
                for(k=0;k<nNumRings;k++){
                    MeanSem0 ms=new MeanSem0();
                    n=(int)(pfRingMeansems[index]+.5);
                    index++;
                    mean=pfRingMeansems[index];
                    index++;
                    sd=pfRingMeansems[index];
                    index++;
                    ms.updateMeanSem2(n, mean, sd*sd);
                    pcMS[k]=ms;
                }
                cvRingMeansems_Normalized.add(pcMS);
            }
        }
    }
    public static void readRingMeansems_Subpixel(BufferedInputStream ds, int nFrameNumber, ArrayList <double[]> localMaxima, ArrayList<int[]> DistsToBorders,
            ArrayList<MeanSemFractional0[]> cvRingMeansems, ArrayList<MeanSemFractional0[]> cvRingMeansems_Group, ArrayList<MeanSemFractional0[]> cvRingMeansems_Normalized,
            ArrayList<MeanSemFractional0[]> cvRingMeansems_background, ArrayList<MeanSemFractional0[]> cvRingMeansems_Group_background,
            ArrayList<MeanSemFractional0[]> cvRingMeansems_Normalized_background,
            ArrayList<Integer> nvOverlappingSatelliteIndexes,ArrayList<Integer> nvSatelliteLMIndexes,
            ArrayList<Double> vdGroupingDelimiters)throws IOException{
        localMaxima.clear();
        cvRingMeansems.clear();
        cvRingMeansems_Group.clear();
        cvRingMeansems_Normalized.clear();
        cvRingMeansems_background.clear();
        cvRingMeansems_Group_background.clear();
        cvRingMeansems_Normalized_background.clear();
        nvOverlappingSatelliteIndexes.clear();
        nvSatelliteLMIndexes.clear();
        vdGroupingDelimiters.clear();
        DistsToBorders.clear();
        int i,j,k,len,nNumRings;
        int[] pnPars=new int[5];
        ArrayList<Double> dvRs=new ArrayList();
        readPixelHeightsGeometricalParameters(ds,pnPars,dvRs);
        nNumRings=pnPars[4];

        byte[] bt1 =new byte[24];
        ds.read(bt1, 0, 8);

        int[] ia1=new int[2];
        ByteConverter.getIntArray(bt1, 0, 8, ia1, 0, 2);
        int nNumFrames=ia1[0];
        int nNumGroups=ia1[1];

        Point pt;
        localMaxima.clear();
        cvRingMeansems.clear();
        cvRingMeansems_Group.clear();
        cvRingMeansems_Normalized.clear();

        byte[] bt3=null;
        int offset=0;
        int pnXYs[]=null;
        float pfRingMeansems[]=null;
        int index;
        int fLeng,intLeng,bfLeng,fLeng0=0,intLeng0=0,bfLeng0=0,nNumSatellites;
        int pnTemp[]=null;

        long lBytesToSkipForAdjustedPixels=0;
        byte[] pbBytesForAdjustedPixels=new byte[1];

        for(i=0;i<nFrameNumber;i++){
            if(i%10==1) IJ.showStatus("reading pixel heights i="+PrintAssist.ToString(i));
            ds.read(bt1,0,4);
            len=ByteConverter.toInteger(bt1, 0, 4);
            ds.read(bt1,0,4);
            nNumSatellites=ByteConverter.toInteger(bt1, 0, 4);
            bfLeng=len*(2*4+3*4*(nNumRings+2))+2*nNumGroups*(nNumRings+2)*3*4;
            bfLeng+=len*2*4;//for distsToBorders
            bfLeng+=len*4+nNumSatellites*4;
            bfLeng+=(nNumGroups-1)*4;

            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);
        }

        double n,mean,sd;
        MeanSemFractional0[] ringMeanSems,ringMeanSems0;
        float pfTemp[]=null;
        double[] IPOCenter;
        int[] distsToBorder;
        for(i=nFrameNumber;i<=nFrameNumber;i++){
            ds.read(bt1,0,4);
            len=ByteConverter.toInteger(bt1, 0, 4);

            ds.read(bt1,0,4);
            nNumSatellites=ByteConverter.toInteger(bt1, 0, 4);
            bfLeng=len*(2*4+3*4*(nNumRings+2))+2*nNumGroups*(nNumRings+2)*3*4;
            bfLeng+=len*4+nNumSatellites*4;
            bfLeng+=len*2*4;//for distsToBorders
            bfLeng+=(nNumGroups-1)*4;
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);

            intLeng=len+nNumSatellites;
            intLeng+=2*len;
            if(intLeng>intLeng0){
                pnTemp=new int[intLeng];
                intLeng0=intLeng;
            }

            ByteConverter.getIntArray(bt3, 0, 4*intLeng, pnTemp, 0, intLeng);

            index=0;
            for(j=0;j<len;j++){
                nvOverlappingSatelliteIndexes.add(pnTemp[index]);
                index++;
            }

            for(j=0;j<nNumSatellites;j++){
                nvSatelliteLMIndexes.add(pnTemp[index]);
                index++;
            }

            for(j=0;j<len;j++){
                distsToBorder=new int[2];
                distsToBorder[0]=pnTemp[index];
                index++;
                distsToBorder[1]=pnTemp[index];
                index++;
                DistsToBorders.add(distsToBorder);
            }

//            fLeng=2*len+2*len+3*len*(nNumRings+2)+2*nNumGroups*(nNumRings+2)*3;
            fLeng=2*len+3*len*(nNumRings+2)+2*nNumGroups*(nNumRings+2)*3;
            fLeng+=nNumGroups-1;
            if(fLeng>fLeng0){
                pfTemp=new float[fLeng];
                fLeng0=fLeng;
            }

            ByteConverter.getFloatArray(bt3, intLeng*4, bfLeng-4*intLeng, pfTemp, 0, fLeng);

            index=0;
            for(j=0;j<nNumGroups-1;j++){
                vdGroupingDelimiters.add((double)pfTemp[index]);
                index++;
            }

            for(j=0;j<len;j++){
                IPOCenter=new double[2];
                IPOCenter[0]=pfTemp[index];
                index++;
                IPOCenter[1]=pfTemp[index];
                index++;
                localMaxima.add(IPOCenter);
            }


            for(j=0;j<len;j++){
                MeanSemFractional0[] pcMS=new MeanSemFractional0[nNumRings];
                for(k=0;k<nNumRings;k++){
                    MeanSem0 ms=new MeanSem0();
                    n=pfTemp[index];
                    index++;
                    mean=pfTemp[index];
                    index++;
                    sd=pfTemp[index];
                    index++;
                    pcMS[k]=new MeanSemFractional0(n,mean,sd);
                }
                cvRingMeansems.add(pcMS);
            }

            for(j=0;j<len;j++){
                MeanSemFractional0[] pcMS=new MeanSemFractional0[2];
                for(k=0;k<2;k++){
                    MeanSem0 ms=new MeanSem0();
                    n=pfTemp[index];
                    index++;
                    mean=pfTemp[index];
                    index++;
                    sd=pfTemp[index];
                    index++;
                    pcMS[k]=new MeanSemFractional0(n,mean,sd);
                }
                cvRingMeansems_background.add(pcMS);
            }

            for(j=0;j<nNumGroups;j++){
                MeanSemFractional0[] pcMS=new MeanSemFractional0[nNumRings];
                for(k=0;k<nNumRings;k++){
                    n=pfTemp[index];
                    index++;
                    mean=pfTemp[index];
                    index++;
                    sd=pfTemp[index];
                    index++;
                    pcMS[k]=new MeanSemFractional0(n,mean,sd);
                }
                cvRingMeansems_Group.add(pcMS);
            }

            for(j=0;j<nNumGroups;j++){
                MeanSemFractional0[] pcMS=new MeanSemFractional0[2];
                for(k=0;k<2;k++){
                    n=pfTemp[index];
                    index++;
                    mean=pfTemp[index];
                    index++;
                    sd=pfTemp[index];
                    index++;
                    pcMS[k]=new MeanSemFractional0(n,mean,sd);
                }
                cvRingMeansems_Group_background.add(pcMS);
            }

            for(j=0;j<nNumGroups;j++){
                MeanSemFractional0[] pcMS=new MeanSemFractional0[nNumRings];
                for(k=0;k<nNumRings;k++){
                    n=pfTemp[index];
                    index++;
                    mean=pfTemp[index];
                    index++;
                    sd=pfTemp[index];
                    index++;
                    pcMS[k]=new MeanSemFractional0(n,mean,sd);
                }
                cvRingMeansems_Normalized.add(pcMS);
            }

            for(j=0;j<nNumGroups;j++){
                MeanSemFractional0[] pcMS=new MeanSemFractional0[2];
                for(k=0;k<2;k++){
                    n=pfTemp[index];
                    index++;
                    mean=pfTemp[index];
                    index++;
                    sd=pfTemp[index];
                    index++;
                    pcMS[k]=new MeanSemFractional0(n,mean,sd);
                }
                cvRingMeansems_Normalized_background.add(pcMS);
            }
        }
    }

    public static void readBackgroundPixels(BufferedInputStream ds, ArrayList<MeanSem0> dvBackgroundPixels, int nBackgroundOption)throws IOException{
        int i,j,len,nNumRings;

        byte[] bt1 =new byte[24];
        byte[] bt2 =new byte[4];
        ds.read(bt1, 0, 24);

        int[] ia1=new int[6];
        ByteConverter.getIntArray(bt1, 0, 24, ia1, 0, 6);
        nNumRings=ia1[3];
        int nNumGroups=ia1[4];

        Point pt;
        byte[] bt3=null;
        int offset=0;
        int pnXYs[]=null;
        float pfRingMeansems[]=null;
        int index;
        int fLeng,intLeng,bfLeng,fLeng0=0,bfLeng0=0;
        int numSlices=ia1[5];
        dvBackgroundPixels.clear();

        for(i=0;i<numSlices;i++){
            ds.read(bt2);
            len=ByteConverter.toInteger(bt2, 0, 4);

            bfLeng=len*(2*4+3*4*nNumRings)+2*nNumGroups*nNumRings*3*4;
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);

            intLeng=2*len;
            fLeng=3*len*nNumRings+2*nNumGroups*nNumRings*3;
            if(fLeng>fLeng0){
                pfRingMeansems=new float[fLeng];
                fLeng0=fLeng;
            }

            offset=0;
//            ByteConverter.getNewIntArray(bt3, offset, 4*intLeng, pnXYs, 0, intLeng);
            offset+=4*intLeng;
            ByteConverter.getFloatArray(bt3, offset, 4*fLeng, pfRingMeansems, 0, fLeng);
            index=0;
            double mean,sd,n;
            int nDelta=3;
            MeanSem0 ms0,ms1, msTotal=new MeanSem0();
            for(j=0;j<len;j++){
                index+=nDelta*(nNumRings-2);
                n=pfRingMeansems[index];
                index++;
                mean=pfRingMeansems[index];
                index++;
                sd=pfRingMeansems[index];
                ms0=new MeanSem0();
                ms0.updateMeanSem2((int)n, mean, sd*sd);
                index++;
                n=pfRingMeansems[index];
                index++;
                mean=pfRingMeansems[index];
                index++;
                sd=pfRingMeansems[index];
                ms1=new MeanSem0();
                ms1.updateMeanSem2((int)n, mean, sd*sd);                
                index++;

                if(nBackgroundOption==0)
                    dvBackgroundPixels.add(ms0);
                else if(nBackgroundOption==1){
                    dvBackgroundPixels.add(ms1);
                }else if(nBackgroundOption==2){
                    msTotal.mergeSems(ms1);
                }else{
                    IJ.error("invalid backgroundOption to read backgroundPixels");
                }
            }
            if(nBackgroundOption==2){
                for(j=0;j<len;j++){
                    dvBackgroundPixels.add(msTotal);
                }
            }
        }
    }

    public static void exportStackPixelHeights(ImagePlus impl, ImagePlus implm, ImagePlus implCompensated,
            ImagePlus implpCompensated, String title, String pathp){
        String path;
        String pathp1;
        String path1;
        impl.setTitle("impl");
        implm.setTitle("implm");
        if(!FileAssist.containsFileNameExtension(title)){
            path=FileAssist.getFilePath(title, "", "pixel height file", "phf", false);
            pathp=FileAssist.getExtendedFileName(path, " processed image");
        }else{
            path=title;
        }
        path1=FileAssist.changeExt(path, "phi");
        pathp1=FileAssist.changeExt(pathp, "phi");
        FileOutputStream fs=null;
        FileOutputStream fs1=null;
        FileOutputStream fsp=null;
        FileOutputStream fsp1=null;

        File f=new File(path);
        DataOutputStream ds=null;

        File f1=new File(path1);
        DataOutputStream ds1=null;

        File fp=new File(pathp);
        DataOutputStream dsp=null;

        File fp1=new File(pathp1);
        DataOutputStream dsp1=null;

        try{
            fs=new FileOutputStream(f);
            ds=new DataOutputStream(fs);

            fs1=new FileOutputStream(f1);
            ds1=new DataOutputStream(fs1);

            fsp=new FileOutputStream(fp);
            dsp=new DataOutputStream(fsp);

            fsp1=new FileOutputStream(fp1);
            dsp1=new DataOutputStream(fsp1);
        }

        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }

        int w=impl.getWidth(),h=impl.getHeight();
        int[][] pixels=new int[h][w];
        int[][] pixelsm=new int[h][w];
        int nSlices=impl.getNSlices();
        IPOPixelHeights c=null;

        int i,j,nRRefI=5, nRRefO=8, nRMax=8;
        ArrayList<ImageShape> cvRings=new ArrayList();
        ArrayList<Double> dvRs=new ArrayList();
        constructIPORings(nRMax,nRRefI,nRRefO,cvRings,dvRs);
        int nNumRings=cvRings.size();
        int nNumGroups=100;
        Point pt;
        float fv;
        try{
            ds.writeInt(nRRefI);
            ds.writeInt(nRRefO);
            ds.writeInt(nRMax);
            ds.writeInt(nNumRings);
            ds.writeInt(nSlices);

            ds1.writeInt(nRRefI);
            ds1.writeInt(nRRefO);
            ds1.writeInt(nRMax);
            ds1.writeInt(nNumRings);
            ds1.writeInt(nNumGroups);
            ds1.writeInt(nSlices);

            dsp.writeInt(nRRefI);
            dsp.writeInt(nRRefO);
            dsp.writeInt(nRMax);
            dsp.writeInt(nNumRings);
            dsp.writeInt(nSlices);

            dsp1.writeInt(nRRefI);
            dsp1.writeInt(nRRefO);
            dsp1.writeInt(nRMax);
            dsp1.writeInt(nNumRings);
            dsp1.writeInt(nNumGroups);
            dsp1.writeInt(nSlices);
        }catch(IOException e){
            IJ.error("IOException");
        }
        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(impl);
        for(i=0;i<nSlices;i++){
            impl.setSlice(i+1);
            implCompensated.setSlice(i+1);
            implpCompensated.setSlice(i+1);

            CommonMethods.getPixelValue(impl, i+1, pixels);
            CommonMethods.getPixelValue(implm, i+1, pixelsm);
            if(i==0){
                c=new IPOPixelHeights(pixelsm,CommonMethods.getSpecialLandscapePoints(pixelsm, stampper, LandscapeAnalyzer.localMaximum), nRRefI, nRRefO, nRMax);
            }else{
                c.updatePixels(pixelsm,CommonMethods.getSpecialLandscapePoints(pixelsm, stampper, LandscapeAnalyzer.localMaximum));
            }
            c.calRefinedPixelHeights();
            CommonMethods.setPixels(implpCompensated, c.getCompensatedPixels());
//            c.noRanking();
            c.storeProcessedPixels();
            c.calPixelHeights_Contour();
            exportPixelHeights(dsp,c);
            exportPixelHeightsInt(dsp1,c);

            c.updatePixels(pixels);
            c.calCompensatedPixels(c.getAdjustedPixels());
            CommonMethods.setPixels(implCompensated, c.getCompensatedPixels());
            c.refinePixelStatitics_noSorting();
            c.calPixelHeights_Contour();
            c.noRanking();
            exportPixelHeights(ds,c);
            exportPixelHeightsInt(ds1,c);
        }
        try{
            ds.close();
            fs.close();
            dsp.close();
            fsp.close();
            ds1.close();
            fs1.close();
            dsp1.close();
            fsp1.close();
        }catch(IOException e){
            IJ.error("IOException");
        }
     }
    public static void exportPixelHeightsGeometricalParameters(DataOutputStream ds, int nRRefI, int nRRefO, int nRMax, int nSubpixelResolution, ArrayList<Double> dvRs){
        int numRings=dvRs.size(),i;
        try{
            ds.writeInt(nRRefI);
            ds.writeInt(nRRefO);
            ds.writeInt(nRMax);
            ds.writeInt(nSubpixelResolution);
            ds.writeInt(numRings);
            for(i=0;i<numRings;i++){
                ds.writeFloat(dvRs.get(i).floatValue());
            }
        }
        catch(IOException e){
            IJ.error("IOEception in exportPixelHeightsGeometricalParameters");
        }
    }
    public static void exportStackPixelHeights_Subpixel(ImagePlus impl, ImagePlus implm, ImagePlus implCompensated,
            ImagePlus implpCompensated, String title, String pathp){
        String path;
        String pathp1;
        String path1;
        impl.setTitle("impl");
        implm.setTitle("implm");
        if(!FileAssist.containsFileNameExtension(title)){
            path=FileAssist.getFilePath(title, "", "pixel height file", "phf", false);
            pathp=FileAssist.getExtendedFileName(path, " processed image");
        }else{
            path=title;
        }
        path1=FileAssist.changeExt(path, "phi");
        pathp1=FileAssist.changeExt(pathp, "phi");
        FileOutputStream fs=null;
        FileOutputStream fs1=null;
        FileOutputStream fsp=null;
        FileOutputStream fsp1=null;

        implCompensated.show();
        implpCompensated.show();

        File f=new File(path);
        DataOutputStream ds=null;

        File f1=new File(path1);
        DataOutputStream ds1=null;

        File fp=new File(pathp);
        DataOutputStream dsp=null;

        File fp1=new File(pathp1);
        DataOutputStream dsp1=null;

        try{
            fs=new FileOutputStream(f);
            ds=new DataOutputStream(fs);

            fs1=new FileOutputStream(f1);
            ds1=new DataOutputStream(fs1);

            fsp=new FileOutputStream(fp);
            dsp=new DataOutputStream(fsp);

            fsp1=new FileOutputStream(fp1);
            dsp1=new DataOutputStream(fsp1);
        }

        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }

        int w=impl.getWidth(),h=impl.getHeight();
        int[][] pixels=new int[h][w];
        int[][] pixelsm=new int[h][w];
        int nSlices=impl.getNSlices();
        IPOPixelHeights_Subpixel_Iterative c=null;

        int i,j,nRRefI=6, nRRefO=8, nRMax=4*nRRefO,nSubpixelResolution=5;
        CocentricCircles_Rings ccring=new CocentricCircles_Rings(6,30);
        SubpixelAreaInRingsLUT subpixelIS=new SubpixelAreaInRingsLUT(ccring,nSubpixelResolution);
        int nNumGroups=100;
        try{
            exportPixelHeightsGeometricalParameters(ds,nRRefI,nRRefO,nRMax,nSubpixelResolution,subpixelIS.getCCRings().getRs());
            ds.writeInt(nSlices);
            ds.writeInt(nNumGroups);

            exportPixelHeightsGeometricalParameters(ds1,nRRefI,nRRefO,nRMax,nSubpixelResolution,subpixelIS.getCCRings().getRs());
            ds1.writeInt(nSlices);
            ds1.writeInt(nNumGroups);

            exportPixelHeightsGeometricalParameters(dsp,nRRefI,nRRefO,nRMax,nSubpixelResolution,subpixelIS.getCCRings().getRs());
            dsp.writeInt(nSlices);
            dsp.writeInt(nNumGroups);

            exportPixelHeightsGeometricalParameters(dsp1,nRRefI,nRRefO,nRMax,nSubpixelResolution,subpixelIS.getCCRings().getRs());
            dsp1.writeInt(nSlices);
            dsp1.writeInt(nNumGroups);
        }catch(IOException e){
            IJ.error("IOException");
        }

        int[][] pixelst=new int[h][w], pixelsgd=new int[h][w],stamp1=new int[h][w],stamp2=new int[h][w];
        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(implm);
        ArrayList<Point> localMaxima,localGDExtrema;

        ImageAnalysis.show_Landscape actemp=new ImageAnalysis.show_Landscape();
        ImagePlus implc=actemp.demo_stampPixels(implm);

        ArrayList<Integer> vnSatellitesMaximaIndexes=new ArrayList();//index of the local maximum the covers the Gradient minima.
        int numSatellites;
        int nTruemaxima,nLocalMaxima;

        for(i=0;i<nSlices;i++){
            impl.setSlice(i+1);
            implm.setSlice(i+1);
            implCompensated.setSlice(i+1);
            implpCompensated.setSlice(i+1);
            localMaxima=new ArrayList();
            localGDExtrema=new ArrayList();

            CommonMethods.getPixelValue(impl, i+1, pixels);
            CommonMethods.getPixelValue(implm, i+1, pixelsm);
            CommonMethods.getPixelValue(implm,i+1, pixelst);

            CommonMethods.getLocalMaximaAndCovervedMaxima(pixelst, stampper, stamp1, stamp2, pixelsgd, localMaxima, localGDExtrema,vnSatellitesMaximaIndexes);
            numSatellites=localGDExtrema.size();

            for(j=0;j<numSatellites;j++){
                localMaxima.add(localGDExtrema.get(j));
            }

            nLocalMaxima=localMaxima.size();
            nTruemaxima=nLocalMaxima-numSatellites;

            numSatellites=localGDExtrema.size();
            implc.setSlice(i+1);
//            CommonMethods.showCoveredLocalMaxima(implc,pixelst,localGDExtrema);

//            IJ.runPlugIn(implt,ij.plugin.filter.RankFilters, "varience");
            ArrayList<double[]> IPOCenters;
            if(i==0){
                c=new IPOPixelHeights_Subpixel_Iterative(pixelsm,stampper,localMaxima,numSatellites,subpixelIS,nRRefI, nRRefO, nRMax);
            }else{
                c.updatePixels(pixelsm,localMaxima,null,numSatellites,true);//turn it on later
            }
            c.calRefinedPixelHeights();
            CommonMethods.setPixels(implpCompensated, c.getCompensatedPixels());
//            c.noRanking();
//            c.storeProcessedPixels();
//            c.calPixelHeights_Contour();
//            exportPixelHeights_Subpixel(dsp,c,vnSatellitesMaximaIndexes);
//            exportPixelHeightsInt_Subpixel(dsp1,c,vnSatellitesMaximaIndexes);

            CommonMethods.showCoveredLocalMaxima(implc,pixelst,c.getLocalMaxima(),c.getOverlappingIPOIndexes(),nTruemaxima,nLocalMaxima-1);

            c.updatePixels(pixels,c.getLocalMaxima(),c.getIPOCenters(),numSatellites,false);//turn it on later
//            c.calCompensatedPixels(c.getAdjustedPixels(),0,localMaxima.size()-1);
//            CommonMethods.setPixels(implCompensated, c.getCompensatedPixels());
//            c.refinePixelStatitics_noSorting();
//            c.calPixelHeights_Contour();
//            c.calPixelHeights0();
//            c.noRanking();
//            exportPixelHeights_Subpixel(ds,c,vnSatellitesMaximaIndexes);
//            exportPixelHeightsInt_Subpixel(ds1,c,vnSatellitesMaximaIndexes);
        }
        try{
            ds.close();
            fs.close();
            dsp.close();
            fsp.close();
            ds1.close();
            fs1.close();
            dsp1.close();
            fsp1.close();
        }catch(IOException e){
            IJ.error("IOException");
        }
     }

    static void exportPixelHeightsInt(DataOutputStream ds, IPOPixelHeights c){
        MeanSem0[][] pcRingMeanSems;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum i and ring r
        MeanSem0[][] pcRingMeanSems_Group;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
        MeanSem0[][] pcRingMeanSems_Normalized;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
        pcRingMeanSems=c.getPcRingMeanSems();
        pcRingMeanSems_Group=c.getPcRingMeanSems_Group();
        pcRingMeanSems_Normalized=c.getPcRingMeanSems_Normalized();
        int nNumGroups=c.getnNumPercentileDivisions();
        int nNumLocalMaxima=c.getNumLocalMaxima();
        int nNumRings=c.getNumRings();
        try{
            ds.writeInt(nNumLocalMaxima);
        }catch(IOException e){
            IJ.error("IOException");
        }
        int[] pnIndexesPt=c.getPtRankingIndexes();
        int i,j,index;
        ArrayList<Point> localMaxima=c.getLocalMaxima();
        Point pt;
        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            pt=localMaxima.get(index);
            try{
                ds.writeInt(pt.x);
                ds.writeInt(pt.y);
            }catch(IOException e){
                IJ.error("IOException");
            }
        }

        MeanSem0 ms;

        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            for(j=0;j<nNumRings;j++){
                ms=pcRingMeanSems[index][j];
                try{
                    ds.writeFloat(ms.n);
                    ds.writeFloat((float)ms.mean);
                    ds.writeFloat((float)ms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }
        for(i=0;i<nNumGroups;i++){
            for(j=0;j<nNumRings;j++){
                ms=pcRingMeanSems_Group[i][j];
                try{
                    ds.writeFloat(ms.n);
                    ds.writeFloat((float)ms.mean);
                    ds.writeFloat((float)ms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }
        for(i=0;i<nNumGroups;i++){
            for(j=0;j<nNumRings;j++){
                ms=pcRingMeanSems_Normalized[i][j];
                try{
                    ds.writeFloat(ms.n);
                    ds.writeFloat((float)ms.mean);
                    ds.writeFloat((float)ms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }
    }
    static void exportPixelHeightsInt_Subpixel(DataOutputStream ds, IPOPixelHeights_Subpixel c ,ArrayList <Integer> vnSatellitesMaximaIndexes){
        MeanSemFractional[][] pcRingMeanSems;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum i and ring r
        MeanSemFractional[][] pcRingMeanSems_Group;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
        MeanSemFractional[][] pcRingMeanSems_Normalized;//MeanSems for the rings, m_pcRingMeanSems_Mean[i][r] is the meanSem of the pixels in local maximum group i (grouping by percentile)  and ring r
        MeanSem1[][] pcRingMeanSems_background;
        MeanSem1[][] pcRingMeanSems_Group_background;
        MeanSem1[][] pcRingMeanSems_Normalized_background;
        pcRingMeanSems=c.getPcRingMeanSems();
        pcRingMeanSems_Group=c.getPcRingMeanSems_Group();
        pcRingMeanSems_Normalized=c.getPcRingMeanSems_Normalized();

        pcRingMeanSems_background=c.getPcRingMeanSems_background();
        pcRingMeanSems_Group_background=c.getPcRingMeanSems_Group_background();
        pcRingMeanSems_Normalized_background=c.getPcRingMeanSems_Normalized_background();
        int nNumGroups=c.getnNumPercentileDivisions();
        ArrayList<double[]> IPOCenters=c.getIPOCenters();
        int nNumLocalMaxima=IPOCenters.size();
        int nNumRings=c.getNumRings();
        int[] pnOverlappingSatelliteIndexes=c.getOverlappingIPOIndexes();
        int[] pnRanking=c.getRanking();
        int nNumSatellites=c.getNumSatellites();
        try{
            ds.writeInt(nNumLocalMaxima);
            ds.writeInt(nNumSatellites);
        }catch(IOException e){
            IJ.error("IOException");
        }

        int[] pnIndexesPt=c.getPtRankingIndexes();
//        int[] pnIndexesPt=CommonMethods.getDefaultRankingIndexes(nNumLocalMaxima);3/5/2011
        double[]IPOCenter;
        int i,j,index,len1;
        double[] pdDelimiters=c.getGroupingDelimiters();
        int nTrueMaxima=nNumLocalMaxima-nNumSatellites;
        int overlappingIndex;

        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            try{
                overlappingIndex=pnOverlappingSatelliteIndexes[index];
                if(overlappingIndex>=0) overlappingIndex=pnRanking[overlappingIndex];
                ds.writeInt(overlappingIndex);
            }catch(IOException e){
                IJ.error("IOException");
            }
        }

        for(j=0;j<nNumSatellites;j++){
            try{
                index=pnIndexesPt[j+nTrueMaxima]-nTrueMaxima;
                ds.writeInt(pnRanking[vnSatellitesMaximaIndexes.get(index)]);
            }catch(IOException e){
                IJ.error("IOException");
            }
        }

        int[][] pdDistToPorder=c.getDistsToBorders();
        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            try{
                ds.writeInt(pdDistToPorder[index][0]);
                ds.writeInt(pdDistToPorder[index][1]);
            }catch(IOException e){
                IJ.error("IOException");
            }
        }

        len1=pdDelimiters.length;
        for(j=0;j<len1;j++){
            try{
                ds.writeFloat((float)pdDelimiters[j]);
            }catch(IOException e){
                IJ.error("IOException");
            }
        }

        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            IPOCenter=IPOCenters.get(index);
            try{
                ds.writeFloat((float)IPOCenter[0]);
                ds.writeFloat((float)IPOCenter[1]);
            }catch(IOException e){
                IJ.error("IOException");
            }
        }

        MeanSem1 ms;
        MeanSemFractional fms;

        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            for(j=0;j<nNumRings;j++){
                fms=pcRingMeanSems[index][j];
                try{
                    ds.writeFloat((float)fms.n);
                    ds.writeFloat((float)fms.mean);
                    ds.writeFloat((float)fms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }

        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            for(j=0;j<2;j++){
                ms=pcRingMeanSems_background[index][j];
                try{
                    ds.writeFloat(ms.n);
                    ds.writeFloat((float)ms.mean);
                    ds.writeFloat((float)ms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }

        for(i=0;i<nNumGroups;i++){
            for(j=0;j<nNumRings;j++){
                fms=pcRingMeanSems_Group[i][j];
                try{
                    ds.writeFloat((float)fms.n);
                    ds.writeFloat((float)fms.mean);
                    ds.writeFloat((float)fms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }

        for(i=0;i<nNumGroups;i++){
            for(j=0;j<2;j++){
                ms=pcRingMeanSems_Group_background[i][j];
                try{
                    ds.writeFloat(ms.n);
                    ds.writeFloat((float)ms.mean);
                    ds.writeFloat((float)ms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }

        for(i=0;i<nNumGroups;i++){
            for(j=0;j<nNumRings;j++){
                fms=pcRingMeanSems_Normalized[i][j];
                try{
                    ds.writeFloat((float)fms.n);
                    ds.writeFloat((float)fms.mean);
                    ds.writeFloat((float)fms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }

        for(i=0;i<nNumGroups;i++){
            for(j=0;j<2;j++){
                ms=pcRingMeanSems_Normalized_background[i][j];
                try{
                    ds.writeFloat(ms.n);
                    ds.writeFloat((float)ms.mean);
                    ds.writeFloat((float)ms.getSD());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
        }
    }
    static void exportPixelHeights(DataOutputStream ds, IPOPixelHeights c){
        int len;
        ArrayList<Double> pixelHeights=new ArrayList(), pixelHeights0=new ArrayList(),pixelHeightsC=new ArrayList();
        ArrayList<Point> localMaxima=new ArrayList();
        int[] pnIndexesPt=c.getPtRankingIndexes();
        int[][] adjustedPixels=c.getAdjustedPixels();
        int i,j,index;
        Point pt;
            c.getPixelHeights(pixelHeights);
            c.getPixelHeightsC(pixelHeightsC);
            c.getPixelHeights0(pixelHeights0);
            c.getLocalMaxima(localMaxima);
            len=pixelHeights.size();
            try{
                ds.writeInt(len);
            }catch(IOException e){
                IJ.error("IOException");
            }
            for(j=0;j<len;j++){
                pt=localMaxima.get(j);
                try{
                    ds.writeInt(pt.x);
                    ds.writeInt(pt.y);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<len;j++){
                try{
                    ds.writeFloat(pixelHeights.get(j).floatValue());
                 }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<len;j++){
                try{
                    ds.writeFloat(pixelHeights0.get(j).floatValue());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<len;j++){
                try{
                    ds.writeFloat(pixelHeightsC.get(j).floatValue());
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            int len1=adjustedPixels[0].length;
            for(i=0;i<len;i++){
                index=pnIndexesPt[i];
                for(j=0;j<len1;j++){
                    try{
                        ds.writeFloat((float)adjustedPixels[index][j]);
                    }catch(IOException e){
                        IJ.error("IOException");
                    }
                }
            }
    }
    static void exportPixelHeights_Subpixel(DataOutputStream ds, IPOPixelHeights_Subpixel c,ArrayList <Integer> vnSatellitesMaximaIndexes){
        int len,len1;
        double[] pixelHeights=c.getPixelHeights(), pixelHeights0=c.getPixelHeights0(),pixelHeightsC=c.getPixelHeightsC();
        ArrayList<Point> localMaxima=c.getLocalMaxima();
        ArrayList<double[]> IPOCenters=c.getIPOCenters();
        int[] pnIndexesPt=c.getPtRankingIndexes();
        int[] pnRanking=c.getRanking();
        double[][] adjustedPixels=c.getAdjustedPixels();
        int[] pnOverlappingSatelliteIndexes=c.getOverlappingIPOIndexes();
        int nNumSatellites=c.getNumSatellites();
        int i,j,index,overlappingIndex;
        double pdDelimiters[]=c.getGroupingDelimiters();
        double[] IPOCenter;
        Point pt;
            len=localMaxima.size();
            int nTrueMaxima=len-nNumSatellites;
            try{
                ds.writeInt(len);
                ds.writeInt(nNumSatellites);
            }catch(IOException e){
                IJ.error("IOException");
            }

            for(j=0;j<len;j++){
                index=pnIndexesPt[j];
                try{
                    overlappingIndex=pnOverlappingSatelliteIndexes[index];
                    if(overlappingIndex>=0) overlappingIndex=pnRanking[overlappingIndex];
                    ds.writeInt(overlappingIndex);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<nNumSatellites;j++){
                index=pnIndexesPt[j+nTrueMaxima]-nTrueMaxima;
                try{
                    if(index<0){
                        index=index;
                    }
                    ds.writeInt(pnRanking[vnSatellitesMaximaIndexes.get(index)]);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
           int [][] pdDistToPorder=c.getDistsToBorders();
            for(j=0;j<len;j++){
                index=pnIndexesPt[j];
                try{
                    ds.writeInt(pdDistToPorder[index][0]);
                    ds.writeInt(pdDistToPorder[index][1]);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            len1=pdDelimiters.length;
            for(j=0;j<len1;j++){
                try{
                    ds.writeFloat((float)pdDelimiters[j]);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<len;j++){
                index=pnIndexesPt[j];
                IPOCenter=IPOCenters.get(index);
                try{
                    ds.writeFloat((float)IPOCenter[0]);
                    ds.writeFloat((float)IPOCenter[1]);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<len;j++){
                index=pnIndexesPt[j];
                try{
                    ds.writeFloat((float)pixelHeights[index]);
                 }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<len;j++){
                index=pnIndexesPt[j];
                try{
                    ds.writeFloat((float)pixelHeights0[index]);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
            for(j=0;j<len;j++){
                index=pnIndexesPt[j];
                try{
                    ds.writeFloat((float)pixelHeightsC[index]);
                }catch(IOException e){
                    IJ.error("IOException");
                }
            }
           len1=adjustedPixels[0].length;
            for(i=0;i<len;i++){
                index=pnIndexesPt[i];
                for(j=0;j<len1;j++){
                    try{
                        ds.writeFloat((float)adjustedPixels[index][j]);
                    }catch(IOException e){
                        IJ.error("IOException");
                    }
                }
            }
    }
    public static void importStackPixelHeights(String path, ArrayList<Integer> radius, ArrayList<Integer> nvNumLocalMaxima, ArrayList<Point> cvLocalMaxima, ArrayList< Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC){
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
            readPixelHeights(ds,radius,nvNumLocalMaxima,cvLocalMaxima, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }
    }
    public static void readPixelHeights (BufferedInputStream ds, ArrayList<Integer> radius, ArrayList<Integer> nvNumLocalMaxima, ArrayList<Point> cvLocalMaxima, ArrayList< Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC)throws IOException {
        int i,j,len,nRRefI,nRRefO,nRMax,nNumRings;

        byte[] bt1 =new byte[20];
        byte[] bt2 =new byte[4];
        ds.read(bt1, 0, 20);

        int[] ia1=new int[5];
        ByteConverter.getIntArray(bt1, 0, 20, ia1, 0, 5);
        nRRefI=ia1[0];
        nRRefO=ia1[1];
        nRMax=ia1[2];
        nNumRings=ia1[3];
        int nSlices=ia1[4];

        radius.clear();
        radius.add(nRRefI);
        radius.add(nRRefO);
        radius.add(nRMax);

        Point pt;
        double h,h0;
        nvNumLocalMaxima.clear();
        cvLocalMaxima.clear();
        dvPixelHeights.clear();
        dvPixelHeights0.clear();

        byte[] bt3=null;
        int offset=0;
        int pnXYs[]=null;
        float pfPHs[]=null;
        int index;
        int fLeng,intLeng,bfLeng,fLeng0=0,intLeng0=0,bfLeng0=0;

        long lBytesToSkipForAdjustedPixels=0;
        byte[] pbBytesForAdjustedPixels=new byte[1];

        for(i=0;i<nSlices;i++){
            if(i%10==1) IJ.showStatus("reading pixel heights i="+PrintAssist.ToString(i));
            ds.read(bt2);
            len=ByteConverter.toInteger(bt2, 0, 4);
            nvNumLocalMaxima.add(len);

            bfLeng=len*20;
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);
            intLeng=2*len;
            if(intLeng>intLeng0){
                pnXYs=new int[intLeng];
                intLeng0=intLeng;
            }

            fLeng=3*len;
            if(fLeng>fLeng0){
                pfPHs=new float[fLeng];
                fLeng0=fLeng;
            }

            offset=0;
            ByteConverter.getIntArray(bt3, offset, 4*intLeng, pnXYs, 0, intLeng);
            offset+=4*intLeng;
            ByteConverter.getFloatArray(bt3, offset, 4*fLeng, pfPHs, 0, fLeng);

            index=0;
            for(j=0;j<len;j++){
                pt=new Point();
                pt.x=pnXYs[index];
                index++;
                pt.y=pnXYs[index];
                index++;
                cvLocalMaxima.add(pt);
            }
            index=0;
            for(j=0;j<len;j++){
                dvPixelHeights.add((double)pfPHs[index]);
                index++;
            }
            for(j=0;j<len;j++){
                dvPixelHeights0.add((double)pfPHs[index]);
                index++;
            }
            for(j=0;j<len;j++){
                dvPixelHeightsC.add((double)pfPHs[index]);
                index++;
            }
            lBytesToSkipForAdjustedPixels=4*len*nNumRings;
            if(pbBytesForAdjustedPixels.length<lBytesToSkipForAdjustedPixels){
                pbBytesForAdjustedPixels=new byte[(int)lBytesToSkipForAdjustedPixels];
            }
//            for(j=0;j<lBytesToSkipForAdjustedPixels;j++){
            ds.read(pbBytesForAdjustedPixels,0,(int)lBytesToSkipForAdjustedPixels);
 //           }
//            ds.skip(lBytesToSkipForAdjustedPixels);

        }
    }
    public static void importStackPixelHeights(String path, int nNumFrame, ArrayList<Point> cvLocalMaxima, ArrayList< Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC){
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
            readPixelHeights(ds,nNumFrame,cvLocalMaxima, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }
    }

    public static void importStackPixelHeights_Subpixel(String path, int nNumFrame, ArrayList<Double> dvRs, ArrayList<double[]> IPOCenters, ArrayList<int[]> DistsToBorders,
            ArrayList< Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC,
            ArrayList<Integer> nvOverlappingSatelliteIndexes,ArrayList<Integer> nvSatelliteLMIndexes,
            ArrayList<Double> vdGroupingDelimiters){
        File f=new File(path);
        BufferedInputStream ds=null;
        try{
            FileInputStream fs=new FileInputStream(f);
            ds=new BufferedInputStream(fs);
            readPixelHeights_Subpixel(ds,nNumFrame,IPOCenters,DistsToBorders, dvRs, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC,nvOverlappingSatelliteIndexes,nvSatelliteLMIndexes,
                    vdGroupingDelimiters);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }
        catch(IOException e){
            IJ.error("IOException");
        }
    }
    public static void exportPixelHeights(String path, ArrayList <Point> localMaxima, ArrayList<Double> pixelHeights, ArrayList<Double> pixelHeights0){
        Formatter fm=FileAssist.getFormatter(path);
        PrintAssist.printString(fm, "Rank", 8);
        PrintAssist.printString(fm, "X", 8);
        PrintAssist.printString(fm, "Y", 8);
        PrintAssist.printString(fm, "Height0", 12);
        PrintAssist.printString(fm, "Height", 12);
        int i,nNumLocalMaxima=localMaxima.size();
        double r;
        PrintAssist.endLine(fm);
        Point p;
        double height,height0;
        for(i=0;i<nNumLocalMaxima;i++){
            p=localMaxima.get(i);
            height=pixelHeights.get(i);
            height0=pixelHeights0.get(i);
            PrintAssist.printNumber(fm, i, 8, 0);
            PrintAssist.printNumber(fm, p.x, 8, 0);
            PrintAssist.printNumber(fm, p.y, 8, 0);
            PrintAssist.printNumber(fm, height0, 12, 1);
            PrintAssist.printNumber(fm, height, 12, 1);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
    public static void readPixelHeights (BufferedInputStream ds,int nFrameNumber, ArrayList<Point> cvLocalMaxima, ArrayList< Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC)throws IOException {
        int i,j,len,nNumRings;

        byte[] bt1 =new byte[20];
        byte[] bt2 =new byte[4];
        ds.read(bt1, 0, 20);

        int[] ia1=new int[5];
        ByteConverter.getIntArray(bt1, 0, 20, ia1, 0, 5);
        nNumRings=ia1[3];


        Point pt;
        cvLocalMaxima.clear();
        dvPixelHeights.clear();
        dvPixelHeights0.clear();

        byte[] bt3=null;
        int offset=0;
        int pnXYs[]=null;
        float pfPHs[]=null;
        int index;
        int fLeng,intLeng,bfLeng,fLeng0=0,intLeng0=0,bfLeng0=0;

        long lBytesToSkipForAdjustedPixels=0;
        byte[] pbBytesForAdjustedPixels=new byte[1];

        for(i=0;i<nFrameNumber;i++){
            if(i%10==1) IJ.showStatus("reading pixel heights i="+PrintAssist.ToString(i));
            ds.read(bt2);
            len=ByteConverter.toInteger(bt2, 0, 4);

            bfLeng=len*20;
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);

            lBytesToSkipForAdjustedPixels=4*len*nNumRings;
            if(pbBytesForAdjustedPixels.length<lBytesToSkipForAdjustedPixels){
                pbBytesForAdjustedPixels=new byte[(int)lBytesToSkipForAdjustedPixels];
            }
            ds.read(pbBytesForAdjustedPixels,0,(int)lBytesToSkipForAdjustedPixels);
        }

        for(i=nFrameNumber;i<=nFrameNumber;i++){
            ds.read(bt2);
            len=ByteConverter.toInteger(bt2, 0, 4);

            bfLeng=len*20;
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);
            intLeng=2*len;
            if(intLeng>intLeng0){
                pnXYs=new int[intLeng];
                intLeng0=intLeng;
            }
            fLeng=3*len;
            if(fLeng>fLeng0){
                pfPHs=new float[fLeng];
                fLeng0=fLeng;
            }

            offset=0;
            ByteConverter.getIntArray(bt3, offset, 4*intLeng, pnXYs, 0, intLeng);
            offset+=4*intLeng;
            ByteConverter.getFloatArray(bt3, offset, 4*fLeng, pfPHs, 0, fLeng);

            index=0;
            for(j=0;j<len;j++){
                pt=new Point();
                pt.x=pnXYs[index];
                index++;
                pt.y=pnXYs[index];
                index++;
                cvLocalMaxima.add(pt);
            }
            index=0;
            for(j=0;j<len;j++){
                dvPixelHeights.add((double)pfPHs[index]);
                index++;
            }
            for(j=0;j<len;j++){
                dvPixelHeights0.add((double)pfPHs[index]);
                index++;
            }
            for(j=0;j<len;j++){
                dvPixelHeightsC.add((double)pfPHs[index]);
                index++;
            }
        }
    }
    public static void readPixelHeightsGeometricalParameters(BufferedInputStream ds, int[] pnPars, ArrayList<Double> dvRs)throws IOException{
        int numRings,i;
        byte[] bt1 =new byte[20];
        ds.read(bt1, 0, 20);
        ByteConverter.getIntArray(bt1, 0, 20, pnPars, 0, 5);

        numRings=pnPars[4];
        int lenb=4*numRings;

        byte bt2[]=new byte[lenb];
        float[] fa=new float[numRings];
        ds.read(bt2,0,lenb);
        ByteConverter.getFloatArray(bt2, 0, lenb, fa, 0, numRings);
        dvRs.clear();
        for(i=0;i<numRings;i++){
            dvRs.add((double)fa[i]);
        }
    }
    public static void readPixelHeights_Subpixel (BufferedInputStream ds,
            int nFrameNumber, ArrayList<double[]> IPOCenters, ArrayList<int[]> DistsToBorders,ArrayList<Double> dvRs,
            ArrayList< Double> dvPixelHeights, ArrayList<Double> dvPixelHeights0, ArrayList<Double> dvPixelHeightsC,
            ArrayList<Integer> nvOverlappingSatelliteIndexes,ArrayList<Integer> nvSatelliteLMIndexes,
            ArrayList<Double> vdGroupingDelimiters)throws IOException {
        dvRs.clear();
        dvPixelHeights.clear();
        dvPixelHeights0.clear();
        vdGroupingDelimiters.clear();
        nvOverlappingSatelliteIndexes.clear();
        nvSatelliteLMIndexes.clear();
        int i,j,len,nNumRings,nNumSatellites;
        int[] pnPars=new int[5];
        readPixelHeightsGeometricalParameters(ds,pnPars,dvRs);
        nNumRings=pnPars[4];

        byte[] bt1 =new byte[20];
        ds.read(bt1, 0, 4);
        int nNumFrame=ByteConverter.toInteger(bt1, 0, 4);
        ds.read(bt1, 0, 4);
        int nNumGroups=ByteConverter.toInteger(bt1, 0, 4);

        Point pt;
        IPOCenters.clear();
        dvPixelHeights.clear();
        dvPixelHeights0.clear();
        nvOverlappingSatelliteIndexes.clear();
        nvSatelliteLMIndexes.clear();

        byte[] bt3=null;
        int offset=0;
        float pfTemp[]=null;
        int index,pnTemp[]=null,nLeng;
        int fLeng,bfLeng,fLeng0=0,nLeng0=0,bfLeng0=0;

        long lBytesToSkipForAdjustedPixels=0;
        byte[] pbBytesForAdjustedPixels=new byte[1];
        int[] distsToBorder;

        for(i=0;i<nFrameNumber;i++){
            if(i%10==1) IJ.showStatus("reading pixel heights i="+PrintAssist.ToString(i));
            ds.read(bt1,0,4);
            len=ByteConverter.toInteger(bt1, 0, 4);
            ds.read(bt1,0,4);
            nNumSatellites=ByteConverter.toInteger(bt1, 0, 4);

            bfLeng=len*4*5;
            bfLeng+=len*4+nNumSatellites*4;//beytes for overlapping satellite indexes (nvOverlappingSatelliteIndexes)
            //and the local maxima index for each satellite (nvSatelliteLMIndexes).
            bfLeng+=2*len*4;
            bfLeng+=4*(nNumGroups-1);
            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }

            ds.read(bt3, 0, bfLeng);

            lBytesToSkipForAdjustedPixels=4*len*nNumRings;
            if(pbBytesForAdjustedPixels.length<lBytesToSkipForAdjustedPixels){
                pbBytesForAdjustedPixels=new byte[(int)lBytesToSkipForAdjustedPixels];
            }
            ds.read(pbBytesForAdjustedPixels,0,(int)lBytesToSkipForAdjustedPixels);
        }

        double[] IPOCenter;
        for(i=nFrameNumber;i<=nFrameNumber;i++){
            ds.read(bt1,0,4);
            len=ByteConverter.toInteger(bt1, 0, 4);
            ds.read(bt1,0,4);
            nNumSatellites=ByteConverter.toInteger(bt1, 0, 4);

            bfLeng=len*4*5;
            bfLeng+=len*4+nNumSatellites*4;
            bfLeng+=2*len*4;
            bfLeng+=4*(nNumGroups-1);

            if(bfLeng>bfLeng0){
                bt3=new byte[bfLeng];
                bfLeng0=bfLeng;
            }
            ds.read(bt3, 0, bfLeng);

            nLeng=len+nNumSatellites;
            nLeng+=2*len;
            if(nLeng>nLeng0){
                pnTemp=new int[nLeng];
                nLeng0=nLeng;
            }
            ByteConverter.getIntArray(bt3, 0, nLeng*4, pnTemp, 0, nLeng);

            index=0;
            for(j=0;j<len;j++){
                nvOverlappingSatelliteIndexes.add(pnTemp[index]);
                index++;
            }
            for(j=0;j<nNumSatellites;j++){
                nvSatelliteLMIndexes.add(pnTemp[index]);
                index++;
            }

            for(j=0;j<len;j++){
                distsToBorder=new int[2];
                distsToBorder[0]=pnTemp[index];
                index++;
                distsToBorder[1]=pnTemp[index];
                index++;
                DistsToBorders.add(distsToBorder);
            }

            fLeng=5*len;
            fLeng+=nNumGroups-1;

            if(fLeng>fLeng0){
                pfTemp=new float[fLeng];
                fLeng0=fLeng;
            }

            ByteConverter.getFloatArray(bt3, nLeng*4, 4*fLeng, pfTemp, 0, fLeng);

            index=0;
            for(j=0;j<nNumGroups-1;j++){
                vdGroupingDelimiters.add((double)pfTemp[index]);
                index++;
            }
            for(j=0;j<len;j++){
                IPOCenter=new double[2];
                IPOCenter[0]=pfTemp[index];
                index++;
                IPOCenter[1]=pfTemp[index];
                index++;
                IPOCenters.add(IPOCenter);
            }
            for(j=0;j<len;j++){
                dvPixelHeights.add((double)pfTemp[index]);
                index++;
            }
            for(j=0;j<len;j++){
                dvPixelHeights0.add((double)pfTemp[index]);
                index++;
            }
            for(j=0;j<len;j++){
                dvPixelHeightsC.add((double)pfTemp[index]);
                index++;
            }
        }
    }
    public static void constructIPORings(int nRMax, int nRRefI, int nRRefO, ArrayList<ImageShape> cvRings, ArrayList<Double> dvRs){
        cvRings.clear();
        dvRs.clear();
        cvRings.add(new Ring(0.,0.));
        dvRs.add(0.);
        double r=1.,dr=0.001;
        double ri=r-dr,ro=r+dr;
        cvRings.add(new Ring(ri,ro));
        dvRs.add(r);

        r=Math.sqrt(2);
        ri=r-dr;
        ro=r+dr;
        cvRings.add(new Ring(ri,ro));
        dvRs.add(r);

        r=2;
        ri=r-dr;
        ro=r+dr;
        cvRings.add(new Ring(ri,ro));
        dvRs.add(r);

        r=Math.sqrt(5);
        ri=r-dr;
        ro=r+dr;
        cvRings.add(new Ring(ri,ro));
        dvRs.add(r);

        int nr;
        for(nr=3;nr<=nRMax;nr++){
            cvRings.add(new Ring(nr,nr));
            dvRs.add((double)nr);
        }

        Ring cRefRing=new Ring(nRRefI,nRRefO);
        cvRings.add(cRefRing);
        dvRs.add((double)nRMax+1);
        Ring backgroundRing=new Ring(nRRefI,4*nRRefO);
        cvRings.add(backgroundRing);
        dvRs.add((double)4*nRRefO);
    }
    public static void constructSigShape(int nRMax, int nRRefI, int nRRefO, ImageShape cSigShape, ImageShape cSurShape){
        cSigShape.buildImageShape(new CircleImage(nRRefI-1));
        cSurShape.buildImageShape(new Ring(nRRefI,nRRefO*4));
    }
    public static double getPixelHeight(int[][]pixels,ArrayList<ImageShape> cvRings,Point pt){
        double ph=0.;
        int len=cvRings.size();
        ImageShape cRef=cvRings.get(len-1),shape;
        int num=0,i,n;
        MeanSem0 ms;
        cRef.setCenter(pt);
        ms=ImageShapeHandler.getPixelMeanSem(pixels, cRef);
        double meanRef=ms.mean;
        for(i=0;i<len-1;i++){
            shape=cvRings.get(i);
            shape.setCenter(pt);
            ms=ImageShapeHandler.getPixelMeanSem(pixels, shape);
            n=ms.n;
            num+=n;
            ph+=n*(ms.mean-meanRef);
        }
        return ph;
    }
    public static double getPixelHeight(int[][]pixels,int [][] pixelsSur, ImageShape cSigShape,ImageShape cSurShape,Point pt, double[] pdSigSur){
        double ph=0.,sig,sur;
        int n;
        MeanSem0 ms;
        cSigShape.setCenter(pt);
        cSurShape.setCenter(pt);
        ms=ImageShapeHandler.getPixelMeanSem(pixels, cSigShape);
        n=ms.n;
        sig=ms.mean;
        ms=ImageShapeHandler.getPixelMeanSem(pixelsSur, cSurShape);
        sur=ms.mean;
        ph=(sig-sur)*n;
        pdSigSur[0]=sig;
        pdSigSur[1]=sur;
        return ph;
    }
    public static double getPixelHeight0(int[][]pixels,ArrayList<ImageShape> cvRings,Point pt){
        double ph0=0.;
        int len=cvRings.size();
        ImageShape cRef=cvRings.get(len-1);
        int n;
        MeanSem0 ms;
        cRef.setCenter(pt);
        ms=ImageShapeHandler.getPixelMeanSem(pixels, cRef);
        ph0=pixels[pt.y][pt.x]-ms.mean;
        return ph0;
    }
    public static void exportPixelHeights(String path,ArrayList<Point> localMaxima, ArrayList<Double>[] pixelHeights, ArrayList<MeanSem0[]> cvRingMeansems, ArrayList<ImageShape> cvRings, ArrayList<Double> dvRs, MeanSem0[] MSs, int detectionMode){
        int modes=MSs.length;
        int i,c,r0=0,nNumRings=dvRs.size(),nNumLocalMaxima=localMaxima.size();
        Formatter fm=FileAssist.getFormatter(path);
        PrintAssist.printString(fm, "Rank", 8);
        PrintAssist.printString(fm, "X", 8);
        PrintAssist.printString(fm, "Y", 8);

        for(i=0;i<modes;i++){
            PrintAssist.printString(fm, "Height"+i, 12);
            PrintAssist.printString(fm, "pValue"+i, 12);
        }

        double r;
        for(i=0;i<nNumRings-1;i++){
            r=dvRs.get(i);
            PrintAssist.printString(fm, "r="+PrintAssist.ToString(r,3),10);
            r0++;
        }
        PrintAssist.printString(fm, "sur0", 10);
        PrintAssist.printString(fm, "sd0", 10);
        PrintAssist.printString(fm, "num0", 10);
        PrintAssist.printString(fm, "sur1", 10);
        PrintAssist.printString(fm, "sd1", 10);
        PrintAssist.printString(fm, "num1", 10);
        PrintAssist.endLine(fm);
        Point p;
        double height, mu[]=new double[3], sd[]=new double[3], pv;
        int j;

        for(j=0;j<modes;j++){
            height=pixelHeights[j].get(i);
            mu[j]=MSs[j].mean;
            sd[j]=MSs[j].getSD();
        }
//        calRanking();
        for(i=0;i<nNumLocalMaxima;i++){
            p=localMaxima.get(i);
            PrintAssist.printNumber(fm, i, 8, 0);
            PrintAssist.printNumber(fm, p.x, 8, 0);
            PrintAssist.printNumber(fm, p.y, 8, 0);
            for(j=0;j<modes;j++){
                height=pixelHeights[j].get(i);
                pv=1.-GaussianDistribution.Phi(height,mu[j],sd[j]);
                PrintAssist.printNumber(fm, height, 12, 1);
                PrintAssist.printNumber(fm, pv, 12, 8);
            }
            for(c=0;c<r0;c++){
                PrintAssist.printNumber(fm, cvRingMeansems.get(i)[c].mean, 10, 2);
            }
            PrintAssist.printNumber(fm, cvRingMeansems.get(i)[nNumRings-2].mean, 10, 2);
            PrintAssist.printNumber(fm, cvRingMeansems.get(i)[nNumRings-2].getSD(), 10, 2);
            PrintAssist.printNumber(fm, cvRingMeansems.get(i)[nNumRings-2].n, 10, 2);
            PrintAssist.printNumber(fm, cvRingMeansems.get(i)[nNumRings-1].mean, 10, 2);
            PrintAssist.printNumber(fm, cvRingMeansems.get(i)[nNumRings-1].getSD(), 10, 2);
            PrintAssist.printNumber(fm, cvRingMeansems.get(i)[nNumRings-1].n, 10, 2);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
    public static int[] getNumSatellites(ArrayList<Integer> nvOverlappingSatelliteIndexesr,ArrayList<Integer> nvSatelliteLMIndexes){
        int len=nvOverlappingSatelliteIndexesr.size(),len1=nvSatelliteLMIndexes.size();
        int[] pnNumSatellites=new int[len];
        int i,index;
        for(i=0;i<len;i++){
            pnNumSatellites[i]=0;
        }
        for(i=0;i<len1;i++){
            index=nvSatelliteLMIndexes.get(i);
            if(index<0) continue;
            pnNumSatellites[index]+=1;
        }
        return pnNumSatellites;
    }
    public static int[] getNumEffectiveSatellites(ArrayList<Integer> nvOverlappingSatelliteIndexesr,ArrayList<Integer> nvSatelliteLMIndexes){
        int len=nvOverlappingSatelliteIndexesr.size(),len1=nvSatelliteLMIndexes.size();
        int[] pnNumSatellites=new int[len];
        int i,index,nTrueMaxima=len-len1;
        for(i=0;i<len;i++){
            pnNumSatellites[i]=0;
        }
        for(i=0;i<len1;i++){
            if(nvOverlappingSatelliteIndexesr.get(i+nTrueMaxima)>=0) continue;
            index=nvSatelliteLMIndexes.get(i);
            if(index<0) continue;
            pnNumSatellites[index]+=1;
        }
        return pnNumSatellites;
    }
    public static void exportPixelHeights_Subpixel(String path,ArrayList<double[]> IPOCenters,ArrayList<int[]> DistsToBorders, ArrayList<Double>[] pixelHeights,
            ArrayList<MeanSemFractional0[]> cvRingMeansems, ArrayList<MeanSemFractional0[]> cvRingMeansems_background,
            ArrayList<Double> dvRs, MeanSem0[] MSs, int numGroups, int detectionMode, ArrayList <Double> vdGroupingDelimiters
            ,ArrayList<Integer> nvOverlappingSatelliteIndexesr,ArrayList<Integer> nvSatelliteLMIndexes){

        int len=pixelHeights[0].size();
        int nNumDelimiters=vdGroupingDelimiters.size();
        int[] pnIndexes=new int[len],pnRanking=new int[len];
        int nNumTrueMaxima=len-nvSatelliteLMIndexes.size();
//        CommonMethods.sortArrays(pixelHeights[0], pnIndexes, pnRanking,0,nNumTrueMaxima-1);
//        CommonMethods.sortArrays(pixelHeights[0], pnIndexes, pnRanking,nNumTrueMaxima,len-1);

        int[] pnNumSatellites=getNumSatellites(nvOverlappingSatelliteIndexesr,nvSatelliteLMIndexes);
        int[] pnNumEffectiveSatellites=getNumEffectiveSatellites(nvOverlappingSatelliteIndexesr,nvSatelliteLMIndexes);

        int modes=MSs.length;
        int i,c,r0=0,nNumRings=dvRs.size(),nNumLocalMaxima=IPOCenters.size();

        double[] pdGroupingDelimiters=new double[nNumDelimiters];
        for(i=0;i<nNumDelimiters;i++){
            pdGroupingDelimiters[i]=vdGroupingDelimiters.get(i);
        }
        Categorizer cGrouper=new Categorizer(pdGroupingDelimiters,true);

        Formatter fm=FileAssist.getFormatter(path);
        PrintAssist.printString(fm, "Index", 8);
        PrintAssist.printString(fm, "Group", 8);
        PrintAssist.printString(fm, "X", 8);
        PrintAssist.printString(fm, "Y", 8);
        PrintAssist.printString(fm, "toBorder0", 10);
        PrintAssist.printString(fm, "toBorder0", 10);

        for(i=0;i<modes;i++){
            PrintAssist.printString(fm, "Height"+i, 12);
            PrintAssist.printString(fm, "pValue"+i, 12);
        }

        PrintAssist.printString(fm, "LMIndex", 10);
        PrintAssist.printString(fm, "OvlpIdx", 10);
        PrintAssist.printString(fm, "Satell", 8);
        PrintAssist.printString(fm, "EffSat", 8);

        double r;
        for(i=0;i<nNumRings;i++){
            r=dvRs.get(i);
            PrintAssist.printString(fm, "r="+PrintAssist.ToString(r,3),10);
            r0++;
        }
        PrintAssist.printString(fm, "sur0", 10);
        PrintAssist.printString(fm, "sd0", 10);
        PrintAssist.printString(fm, "num0", 10);
        PrintAssist.printString(fm, "sur1", 10);
        PrintAssist.printString(fm, "sd1", 10);
        PrintAssist.printString(fm, "num1", 10);
        PrintAssist.endLine(fm);
        Point p;
        double height, mu[]=new double[3], sd[]=new double[3], pv;
        int j;

        for(j=0;j<modes;j++){
            height=pixelHeights[j].get(i);
            mu[j]=MSs[j].mean;
            sd[j]=MSs[j].getSD();
        }

//        calRanking();
        double[] IPOCenter;
        int groupIndex,nRank,LMIndex,index;

        int pnIndexesPt[]=CommonMethods.getDefaultRankingIndexes(nNumLocalMaxima);
        CommonMethods.sortArray(IPOCenters,pnIndexesPt,100000.,0,nNumTrueMaxima-1);
        CommonMethods.sortArray(IPOCenters,pnIndexesPt,100000.,nNumTrueMaxima,nNumLocalMaxima-1);
/*
        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            pnRanking[index]=i;
        }
*/
        int[] distsToBorder;
        for(i=0;i<nNumLocalMaxima;i++){
            index=pnIndexesPt[i];
            nRank=pnRanking[index];
            groupIndex=cGrouper.getCatIndex(pixelHeights[0].get(index));
            IPOCenter=IPOCenters.get(index);
            distsToBorder=DistsToBorders.get(index);
            PrintAssist.printNumber(fm, index, 8, 0);
            PrintAssist.printNumber(fm, groupIndex, 8, 0);
            PrintAssist.printNumber(fm, IPOCenter[0], 8, 2);
            PrintAssist.printNumber(fm, IPOCenter[1], 8, 2);
            PrintAssist.printNumber(fm, distsToBorder[0], 10, 0);
            PrintAssist.printNumber(fm, distsToBorder[1], 10, 0);
            for(j=0;j<modes;j++){
                height=pixelHeights[j].get(index);
                pv=1.-GaussianDistribution.Phi(height,mu[j],sd[j]);
                PrintAssist.printNumber(fm, height, 12, 1);
                PrintAssist.printNumber(fm, pv, 12, 8);
            }
            
            if(i<nNumTrueMaxima){
                LMIndex=-1;
            }else{
                LMIndex=nvSatelliteLMIndexes.get(index-nNumTrueMaxima);
            }
            PrintAssist.printNumber(fm, LMIndex, 10, 0);
            PrintAssist.printNumber(fm, nvOverlappingSatelliteIndexesr.get(index), 10, 0);
            PrintAssist.printNumber(fm, pnNumSatellites[index], 8, 0);
            PrintAssist.printNumber(fm, pnNumEffectiveSatellites[index], 8, 0);

            for(c=0;c<nNumRings;c++){
                PrintAssist.printNumber(fm, cvRingMeansems.get(index)[c].mean, 10, 2);
            }
            PrintAssist.printNumber(fm,cvRingMeansems_background.get(index)[0].mean, 10, 2);
            PrintAssist.printNumber(fm,cvRingMeansems_background.get(index)[0].sd, 10, 2);
            PrintAssist.printNumber(fm,cvRingMeansems_background.get(index)[0].n, 10, 2);
            PrintAssist.printNumber(fm,cvRingMeansems_background.get(index)[1].mean, 10, 2);
            PrintAssist.printNumber(fm,cvRingMeansems_background.get(index)[1].sd, 10, 2);
            PrintAssist.printNumber(fm,cvRingMeansems_background.get(index)[1].n, 10, 2);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }

    public static void exportRingMeanSem(String path,ArrayList<MeanSem0[]> cvRingMeansems_Group, ArrayList<MeanSem0[]> cvRingMeansems_Normalized, ArrayList<ImageShape> cvRings, ArrayList<Double> dvRs){
        int i,r,c,nNumRings=cvRings.size();
        MeanSem0 MeanSem, MeanSemNR;
        int nNumGroups=cvRingMeansems_Group.size();
        int rI,rF;
        path=FileAssist.getExtendedFileName(path, " -all percentile");
        Formatter fm=QuickFormatter.getFormatter(path);

        for(i=0;i<nNumGroups;i++){
            PrintAssist.printString(fm, "dist"+PrintAssist.ToString(i), 8);
            PrintAssist.printString(fm, "mean"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "SD"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "meanNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "semNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "num"+PrintAssist.ToString(i), 10);
        }
        PrintAssist.printString(fm, "numPixels", 15);
        PrintAssist.endLine(fm);
        for(c=0;c<nNumRings;c++){
            for(i=0;i<nNumGroups;i++){
                PrintAssist.printNumber(fm, dvRs.get(c), 8, 2);
                PrintAssist.printNumber(fm, cvRingMeansems_Group.get(i)[c].mean, 12, 3);
                PrintAssist.printNumber(fm, cvRingMeansems_Group.get(i)[c].getSD(), 12, 4);
                PrintAssist.printNumber(fm, cvRingMeansems_Normalized.get(i)[c].mean, 12, 4);
                PrintAssist.printNumber(fm, cvRingMeansems_Normalized.get(i)[c].getSD(), 12, 4);
                PrintAssist.printNumber(fm, cvRingMeansems_Group.get(i)[c].n, 10, 0);
            }
            PrintAssist.printNumber(fm, cvRings.get(c).getArea(), 15, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();
     }
    public static void exportRingMeanSem_Subpixel(String path,ArrayList<MeanSemFractional0[]> cvRingMeansems_Group, 
            ArrayList<MeanSemFractional0[]> cvRingMeansems_Normalized,ArrayList<Double> dvRs,ArrayList <Double> vdGroupingDelimiters){
        int i,r,c,nNumRings=dvRs.size();
        MeanSem0 MeanSem, MeanSemNR;
        int nNumGroups=cvRingMeansems_Group.size();
        int rI,rF;
        path=FileAssist.getExtendedFileName(path, " -all percentile");
        Formatter fm=QuickFormatter.getFormatter(path);
        int nDelimiters=vdGroupingDelimiters.size();


        for(i=0;i<nNumGroups;i++){
            PrintAssist.printString(fm, "dist"+PrintAssist.ToString(i), 8);
            PrintAssist.printString(fm, "mean"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "SD"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "meanNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "semNR"+PrintAssist.ToString(i), 12);
            PrintAssist.printString(fm, "num"+PrintAssist.ToString(i), 10);
        }
//        PrintAssist.printString(fm, "numPixels", 15);
        PrintAssist.endLine(fm);
        double delimiter;
        for(c=0;c<nNumRings;c++){
            for(i=0;i<nNumGroups;i++){
                PrintAssist.printNumber(fm, dvRs.get(c), 8, 2);
                PrintAssist.printNumber(fm, cvRingMeansems_Group.get(i)[c].mean, 12, 3);
                PrintAssist.printNumber(fm, cvRingMeansems_Group.get(i)[c].sd, 12, 4);
                PrintAssist.printNumber(fm, cvRingMeansems_Normalized.get(i)[c].mean, 12, 4);
                PrintAssist.printNumber(fm, cvRingMeansems_Normalized.get(i)[c].sd, 12, 4);
                PrintAssist.printNumber(fm, cvRingMeansems_Group.get(i)[c].n, 10, 0);
            }
//            PrintAssist.printNumber(fm, cvRings.get(c).getArea(), 15, 0);
            PrintAssist.endLine(fm);
        }
        delimiter=0;
        for(i=0;i<nNumGroups;i++){
            if(i<nNumGroups-1) delimiter=vdGroupingDelimiters.get(i);
            PrintAssist.printString(fm, "GrpDlmters", 12);
            PrintAssist.printNumber(fm, delimiter, 12, 3);
            PrintAssist.printNumber(fm, delimiter, 12, 4);
            PrintAssist.printNumber(fm, delimiter, 12, 4);
            PrintAssist.printNumber(fm, delimiter, 12, 4);
            PrintAssist.printNumber(fm, delimiter, 10, 0);
        }
        fm.close();
     }
}
