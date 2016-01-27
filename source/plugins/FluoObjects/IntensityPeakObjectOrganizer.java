/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import ImageAnalysis.GraphicalObject;
import ImageAnalysis.GraphicalObjectProperty;
import ImageAnalysis.GraphicalObjectsHandler;
import CommonDataClasses.CommonDataSet;
import ij.ImagePlus;
import ij.gui.Roi;
import utilities.CommonMethods;
import utilities.statistics.MeanSem0;
import utilities.ArrayofArrays.IntArray;
import utilities.ArrayofArrays.IntArray2;
import java.awt.Rectangle;
import utilities.QuickSortInteger;
import ij.process.ByteProcessor;
import utilities.Import_ROIs;
import CommonDataClasses.CommonDataSet;
import ij.IJ;
import utilities.CustomDataTypes.intRange;
import java.awt.Color;
import ij.io.SaveDialog;
import java.util.Formatter;
import java.io.File;
import java.io.FileNotFoundException;
import utilities.QuickSort;
import utilities.ArrayofArrays.DoubleArray;
import ij.io.SaveDialog;
import utilities.QuickFormatter;
import utilities.io.PrintAssist;
import utilities.ArrayofArrays.PointArray;
import ImageAnalysis.GeneralGraphicalObject;
import java.awt.Point;
import ImageAnalysis.LandscapeAnalyzer;
import ImageAnalysis.ContourFollower;
import ScriptManager.Script_Runner;
import ImageAnalysis.PixelTrailHandler;
import utilities.ArrayofArrays.PixelTrailHandlerArray;
import utilities.io.PrintAssist;
import ij.ImageStack;
import ij.process.ColorProcessor;
import FluoObjects.MIPWatershedIPO;
import utilities.ArrayofArrays.IPOTrackArray;
import FluoObjects.IPObjectTracker;
import FluoObjects.IPOTBundleOrganizer;
import utilities.AbfHandler.Abf;
import utilities.io.FileAssist;
import ImageAnalysis.IPOPixelHeightsHandler;
import utilities.Geometry.ImageShapes.Ring;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Geometry.ImageShapes.Ring;
import utilities.CommonStatisticsMethods;
import ImageAnalysis.RegionBoundaryAnalyzer;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;

/**
                                    *
 *
 * @author Taihao
 */
public class IntensityPeakObjectOrganizer {
    static public final int RegularIPO=1, GaussianNodeIPO=2, GaussianNodeGroupIPO=3;
    ArrayList<Integer> anchors,generalIPOTs;
    String newline=System.getProperty("line.separator");
    ImagePlus impl,implr,implp,implpr,implCompensated;//impl: original image, implr: reference image, implp: processed image, implpr processed reference image
    int pixels[][], pixelsr[][];
    int pixelsp[][], pixelspr[][];
    int pixelsCompensated[][],pixelst[][];
    ArrayList <IntensityPeakObjectHandler> IPOHs;
    int trackingLength,radius;
    int searchingDist,firstHandledSlice;
    ArrayList<IPObjectTrack> IPOTracks;
    double[] backgroundPercentiles;
    int minPeakPercentileIndex;
    IPOTBundleOrganizer IPOTBO;
    ImageShape m_cSigShape,m_cSurShape;
    int m_numDisplayableTracks;

    ArrayList <PointArray> m_vcWshContourPeaks;
    ArrayList <ImageShape> m_cvIPORings;
    int m_nTrackExportMode;//0 for exporting pixelHeights, 1 for pixelHeights0
    int w,h;
    ArrayList<MeanSem0> cvIntensityMS, cvBackgroundMS, cvPixelHeights0MS, cvPixelHeightsMS, cvPixelHeightsCMS;
    ArrayList<Integer> nvSelectedParticles, nvBackgroundParticles;
    ArrayList<Double> dvCutoffs,dvMostProbablePixels;
    ArrayList<MeanSem0> cvRefMS;//MeanSem0 for the pixelHeights of the reference image used to computer the cutoff values.
    int m_nIPOType;
    int [][][]stampPS;


    public IntensityPeakObjectOrganizer(){
        backgroundPercentiles=new double[16];
        backgroundPercentiles[0]=0.0000000001;
        backgroundPercentiles[1]=0.00000001;
        backgroundPercentiles[2]=0.0000001;
        backgroundPercentiles[3]=0.00001;
        backgroundPercentiles[4]=0.0001;
        backgroundPercentiles[5]=0.001;
        backgroundPercentiles[6]=0.01;
        backgroundPercentiles[7]=0.05;
        backgroundPercentiles[8]=0.1;
        backgroundPercentiles[9]=0.2;
        backgroundPercentiles[10]=0.3;
        backgroundPercentiles[11]=0.4;
        backgroundPercentiles[12]=0.5;
        backgroundPercentiles[13]=0.6;
        backgroundPercentiles[14]=0.7;
        backgroundPercentiles[15]=1.;
        cvRefMS=new ArrayList();

        minPeakPercentileIndex=5;
        pixels=new int[1][1];
        impl=null;
        IPOHs=null;
        trackingLength=6;
        radius=3;
        searchingDist=5;
        m_nTrackExportMode=0;
        m_nIPOType=RegularIPO;
    }
    public void setTrackExportMode(int mode){
        m_nTrackExportMode=mode;
    }
    public void setImage(ImagePlus impl){
        this.impl=impl;
        w=impl.getWidth();
        h=impl.getHeight();
    }
    public void setImageR(ImagePlus impl){
        this.implr=impl;
        w=impl.getWidth();
        h=impl.getHeight();
    }
    public void setImageCompensated(ImagePlus impl){
        implCompensated=impl;
    }
    public void setImageP(ImagePlus impl){
        this.implp=impl;
        w=impl.getWidth();
        h=impl.getHeight();
    }
    public void setImagePR(ImagePlus impl){
        this.implpr=impl;
        w=impl.getWidth();
        h=impl.getHeight();
    }
    public void buildIPOHs_PrecomputedPixelHeights(String path,String pathp,String pathr, String pathrp, int nDetectionMode, int nBackgroundOption, int cutoffSmoothingWS,int nFirstFrame){//later version
        ArrayList<Integer> nvRadius=new ArrayList();
        ArrayList<Integer> nvNumLocalMaxima=new ArrayList();
        ArrayList<Point> cvvLocalMaxima=new ArrayList();
        ArrayList<Double> dvvPixelHeights=new ArrayList();
        ArrayList<Double> dvvPixelHeightsC=new ArrayList();
        ArrayList<Double> dvvPixelHeights0=new ArrayList();
        ArrayList<Double> dvvBackgroundPixels=new ArrayList();
        ArrayList<MeanSem0> dvvBackgroundPixelValues=new ArrayList();
        ArrayList<int[]> npvPercentileIndexes=new ArrayList();
        ArrayList<double[]> dpvPercentileCutoff=new ArrayList();
//        IPOPixelHeightsHandler.importStackPixelHeights(path, nvRadius, nvNumLocalMaxima, cvvLocalMaxima,
//                dvvPixelHeights, dvvPixelHeightsp0);
        IPOPixelHeightsHandler.detectIPOs_PixelHeights(path, pathp, pathr, nvRadius, nvNumLocalMaxima, cvvLocalMaxima,
                dvvPixelHeights, dvvPixelHeights0, dvvPixelHeightsC, dvvBackgroundPixelValues,
                dvvBackgroundPixels, cvRefMS, backgroundPercentiles, npvPercentileIndexes,dpvPercentileCutoff,
                nDetectionMode,nBackgroundOption,cutoffSmoothingWS,nFirstFrame,w,h);

        int nRRefI=nvRadius.get(0), nRRefO=nvRadius.get(1), nRMax=nvRadius.get(2);

        m_cvIPORings=new ArrayList();
        ArrayList<Double> dvRs=new ArrayList();
        IPOPixelHeightsHandler.constructIPORings(nRMax, nRRefI, nRRefO, m_cvIPORings, dvRs);
        m_cSigShape=new ImageShape();
        m_cSurShape=new ImageShape();
        IPOPixelHeightsHandler.constructSigShape(nRMax, nRRefI, nRRefO, m_cSigShape,m_cSurShape);
        ImageShapeHandler.setFrameRanges(m_cvIPORings, new intRange(0,w-1), new intRange(0,h-1));
        m_cSigShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        m_cSurShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

        int[] pnPercentileIndexes;
        int len,offset=0;
        if(impl!=null){
            int z=0,t=0,w=impl.getWidth(),h=impl.getHeight();
            trackingLength=3;
            intRange isr=new intRange(0,255);
            int n=impl.getNSlices();
            int i,j,k;
            if(IPOHs==null){
                IPOHs=new ArrayList <IntensityPeakObjectHandler>();
            }else{
                IPOHs.clear();
            }
            offset=0;
            pixelst=new int[h][w];
            for(i=0;i<n;i++){
                len=nvNumLocalMaxima.get(i);
                pnPercentileIndexes=npvPercentileIndexes.get(i);
                if(z==165){
                    z=z;
                }/*
//                offset=0;//10803
                for(j=0;j<len;j++){
                    localMaxima.add(cvvLocalMaxima.get(offset+j));
                    dvPixelHeights.add(dvvPixelHeights.get(offset+j));
//                    dvPixelHeights0.add(dvvPixelHeights0.get(offset+j));
                    dvPixelHeights0.add(dvvPixelHeights0.get(offset+j));
                    dvBackgroundPixelValues.add(dvvBackgroundPixelValues.get(offset+j));
                }
                for(j=0;j<len;j++){
                    cvvLocalMaxima.remove(j);
                    dvvPixelHeights.remove(j);
//                    dvPixelHeights0.add(dvvPixelHeights0.get(offset+j));
                    dvvPixelHeights0.remove(j);
                    dvvBackgroundPixelValues.remove(j);
                }*/
                impl.setSlice(i+1);
                implp.setSlice(i+1);
                implCompensated.setSlice(i+1);
                pixels=CommonMethods.getPixelValues(impl);
                pixelsp=CommonMethods.getPixelValues(implp);
                pixelsCompensated=CommonMethods.getPixelValues(implCompensated);
                z=i;
                t=i;
                IntensityPeakObjectHandler IPOH=new IntensityPeakObjectHandler(z,t,searchingDist,radius,pixels,pixelsp,pixelsCompensated,pixelst,w,h,
                        isr,trackingLength,backgroundPercentiles,cvvLocalMaxima,dvvPixelHeights,dvvPixelHeights0,
                        pnPercentileIndexes,minPeakPercentileIndex,m_cvIPORings,dpvPercentileCutoff.get(i),dvvBackgroundPixelValues,dvvBackgroundPixels,
                        cvRefMS.get(i),offset,len,m_cSigShape,m_cSurShape);
                IPOHs.add(IPOH);
                offset+=len;
            }
            cvvLocalMaxima.clear();
            dvvPixelHeights.clear();
            dvvPixelHeights0.clear();
            dvvBackgroundPixelValues.clear();
//                    dvPixelHeights0.add(dvvPixelHeights0.get(offset+j));
            Runtime.getRuntime().gc();
            setNeighboringIPOs(IPOHs);
        }else{
            IJ.showMessage("No image has been set for FluoObjectHandler to build IOPHs");
        }
        firstHandledSlice=IPOHs.get(0).z+1;
    }
    public void buildIPOHs_PrecomputedGaussianNode(String path){//later version
        m_nIPOType=GaussianNodeIPO;
        m_cSigShape=new CircleImage(3);
        m_cSurShape=new Ring(4,6);
        m_cSigShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        m_cSurShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
//        impl=CommonMethods.importImage(path);
        StackIPOGaussianNode cStackIPONode=IPOGaussianNodeHandler.importFittedStackIPOs(path);
        SliceIPOGaussianNode cSliceIPONode;
        ImagePlus implt;
        implp=impl;
        if(impl!=null&&cStackIPONode!=null){
            int z=0,t=0,w=impl.getWidth(),h=impl.getHeight();

            trackingLength=3;
            intRange isr=new intRange(0,255);
            int i,nSlices=cStackIPONode.nSlices;
            if(IPOHs==null){
                IPOHs=new ArrayList <IntensityPeakObjectHandler>();
            }else{
                IPOHs.clear();
            }
            pixelst=new int[h][w];
            int[][] pnScratch=new int[h][w];
            double[][] pdPixels=new double[h][w];

            for(i=0;i<nSlices;i++){
                cSliceIPONode=cStackIPONode.SliceIPOs.get(i);
                z=cSliceIPONode.slice;
                t=z;
                impl.setSlice(z);
                pixels=CommonMethods.getPixelValues(impl);
                pixelsCompensated=new int[h][w];
                IPOAnalyzerForm.getCompensatedPixels(cSliceIPONode.IPOs, pdPixels, pixels, pnScratch, pixelsCompensated);
//                CommonMethods.displayPixels(pixelsCompensated, "CompensatedImage", ImagePlus.GRAY16);//working as expected 11909
                IntensityPeakObjectHandler_GaussianNode IPOH=new IntensityPeakObjectHandler_GaussianNode(cSliceIPONode,searchingDist,radius,pixels,pixelsCompensated,pixelst,w,h,
                        trackingLength,m_cSigShape,m_cSurShape);
                IPOHs.add(IPOH);
            }
            Runtime.getRuntime().gc();
            setNeighboringIPOs(IPOHs);
        }else{
            IJ.showMessage("No image has been set for FluoObjectHandler to build IOPHs");
        }
        firstHandledSlice=IPOHs.get(0).z+1;
    }
    public void buildIPOHs_PrecomputedGaussianNodeGroup(String path){//later version
        m_nIPOType=GaussianNodeGroupIPO;
        m_cSigShape=new CircleImage(3);
        m_cSurShape=new Ring(4,6);
        m_cSigShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        m_cSurShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
//        impl=CommonMethods.importImage(path);
        StackIPOGaussianNode cStackIPONode=IPOGaussianNodeHandler.importFittedStackIPOs(path);
        SliceIPOGaussianNode cSliceIPONode;
        ImagePlus implt=implp;
        implp=impl;
        int[][] pnScratch=new int[h][w];
        if(impl!=null&&cStackIPONode!=null){
            int z=0,t=0,w=impl.getWidth(),h=impl.getHeight();

            trackingLength=3;
            intRange isr=new intRange(0,255);
            int i,nSlices=cStackIPONode.nSlices;
            if(IPOHs==null){
                IPOHs=new ArrayList <IntensityPeakObjectHandler>();
            }else{
                IPOHs.clear();
            }
            pixelst=new int[h][w];
            double[][] pdPixels=new double[h][w];

            for(i=0;i<nSlices;i++){
                cSliceIPONode=cStackIPONode.SliceIPOs.get(i);
                z=cSliceIPONode.slice-1;
                t=z;
                impl.setSlice(z);
                pixels=CommonMethods.getPixelValues(impl);
                pixelsCompensated=new int[h][w];
                IPOAnalyzerForm.getCompensatedPixels(cSliceIPONode.IPOs, pdPixels, pixels, pnScratch, pixelsCompensated);
//              CommonMethods.displayPixels(pixelsCompensated, "CompensatedImage", ImagePlus.GRAY16);//working as expected 11909
                IntensityPeakObjectHandler_GaussianNodeGroup IPOH=new IntensityPeakObjectHandler_GaussianNodeGroup(cSliceIPONode,searchingDist,radius,pixels,pixelsCompensated,pixelst,pnScratch,w,h,
                        trackingLength,m_cSigShape,m_cSurShape);
                IPOHs.add(IPOH);
            }
            Runtime.getRuntime().gc();
            setNeighboringIPOs(IPOHs);
        }else{
            IJ.showMessage("No image has been set for FluoObjectHandler to build IOPHs");
        }
        firstHandledSlice=IPOHs.get(0).z+1;
        implp=implt;
    }

    void setNeighboringIPOs(ArrayList <IntensityPeakObjectHandler> IPOHs){
        int size=IPOHs.size();
        int i,j;
        IntensityPeakObjectHandler IPOH,IPOHt;
        ArrayList <IntensityPeakObject> IPOs;
        IntensityPeakObject IPO,IPOt;
        int Ii,If,size1,index,num,k;
        ArrayList<Double> dr=new ArrayList<Double>();
        for(i=0;i<size;i++){
            Ii=i-1;
            if(Ii<0) Ii=0;
            If=i+1;
            if(If>size-1) If=size-1;
            IPOH=IPOHs.get(i);
            IPOs=IPOH.getIPOs();
            size1=IPOs.size();
            for(j=0;j<size1;j++){
                IPO=IPOH.getIPO(j);
                ArrayList<IntensityPeakObject> preIPOs=new ArrayList<IntensityPeakObject>();
                ArrayList<IntensityPeakObject> currentIPOs=new ArrayList<IntensityPeakObject>();
                ArrayList<IntensityPeakObject> postIPOs=new ArrayList<IntensityPeakObject>();

                if(i>Ii){
                    IPOHt=IPOHs.get(Ii);
                    IPOHt.getClosestIPOs(searchingDist, 5, IPO, preIPOs, dr);
                }
                IPO.setPreIPOs(preIPOs);
                IPOHt=IPOHs.get(i);
                IPOHt.getClosestIPOs(2*searchingDist, 5, IPO, currentIPOs, dr);
                num=currentIPOs.size();
                for(k=0;k<num;k++){
                    IPOt=currentIPOs.get(k);
                    if(IPOt.equals(IPO)) {
                        currentIPOs.remove(IPOt);
                        break;
                    }
                }
                IPO.setCurrentIPOs(currentIPOs);
                if(If>i){
                    IPOHt=IPOHs.get(If);
                    IPOHt.getClosestIPOs(searchingDist, 5, IPO, postIPOs, dr);
                }
                IPO.setPostIPOs(postIPOs);
            }
        }
    }

    void setNeighboringIPOs(IntensityPeakObjectHandler IPOH, IntensityPeakObject IPO, IntensityPeakObjectHandler IPOH1, IntensityPeakObject IPO1){//IPOH,IPO are before IPOH1,IPO1
        ArrayList<IntensityPeakObject> preIPOs=new ArrayList<IntensityPeakObject>();
        ArrayList<IntensityPeakObject> postIPOs=new ArrayList<IntensityPeakObject>();
        if(IPOH==null){
            if(IPOH1!=null&&IPO1!=null){
                IPO1.preIPO=null;
                IPO1.preIPOs=null;
            }
        }else if(IPOH1==null){
            if(IPOH!=null&&IPO!=null){
                IPO.postIPO=null;
                IPO.postIPOs=null;
            }
        }else{
            ArrayList<Double> dr=new ArrayList<Double>();
            IPOH.getClosestIPOs(searchingDist, 5, IPO1, preIPOs, dr);
            IPO1.setPreIPOs(preIPOs);
            IPOH1.getClosestIPOs(searchingDist, 5, IPO, postIPOs, dr);
            IPO.setPostIPOs(postIPOs);
            IPO1.preIPO=IPO;
            IPO.postIPO=IPO1;
        }
    }

    public void markIPOTracks(){
        markIPOTracks(IPOTracks,implp);
    }

    public void markIPOTracks(ArrayList<IPObjectTrack> tracks, ImagePlus impl){
        ImagePlus impl0=CommonMethods.copyToRGBImage(impl);
        int size=tracks.size();
        int i,j,k;
        IPObjectTrack track;
        Color c;
        for(i=0;i<size;i++){
            c=CommonMethods.randomColor();
            impl0.setColor(c);
            track=tracks.get(i);
            track.markTrack(impl0,c,minPeakPercentileIndex);
        }
        impl0.show();
    }

    public ImagePlus markIPOs(){
        ImagePlus impl0=CommonMethods.copyToRGBImage(implp);
        int size=IPOHs.size();
        int i,j,k;
        Color c;
        IntensityPeakObjectHandler IPOH;
        for(i=1;i<=size;i++){
            impl0.setSlice(i);
            IPOH=IPOHs.get(i-1);
            IPOH.markIPOs(impl0);
        }
        impl0.show();
        return impl0;
   }

   public void buildIPOTracks_IPOTracker(int firstSliceToTrack){
        int nCostMatrixChoice=IPObjectTracker.Distance;
        IPObjectTracker tracker=new IPObjectTracker(IPOHs,searchingDist,firstSliceToTrack,nCostMatrixChoice);
        if(nCostMatrixChoice==IPObjectTracker.ImageShapeOverlap){
            updateOvlpInfoToIPOGs();
        }
        IPOTracks=tracker.getIPOTracks();
//        ArrayList<Point> IPOTPairs=IntensityPeakObjectOrganizer.checkIPOTDuplicacy(IPOTracks);//for debugging purpose
   }
   public void setTrackExportMode(){
       int len=IPOTracks.size();
       for(int i=0;i<len;i++){
           IPOTracks.get(i).setTrackExportMode(m_nTrackExportMode);
       }
   }
   ArrayList<Integer> sortIPOTs(int key){
        ArrayList sortedOrder=new ArrayList<Integer>();
        int i,j;
        int size=IPOTracks.size();
        ArrayList<Double> sortingValues=new ArrayList<Double>();
        double d;
        for(i=0;i<size;i++){
            switch(key){
                case 0:
                    d=-IPOTracks.get(i).trackLength;
                    break;
                default:
                    d=-IPOTracks.get(i).trackLength;
                    break;
            }
            sortingValues.add(d);
            sortedOrder.add(i);
        }
        QuickSort.quicksort(sortingValues, sortedOrder);
        return sortedOrder;
    }

    IntensityPeakObjectHandler getIPOH(IntensityPeakObject IPO){
        int t0=IPOHs.get(0).t;
        int t=IPO.t;
        int index=t-t0;
        return IPOHs.get(index);
    }
    public void buildIPOTBundles(){
        int len=IPOHs.size();
        IPOTBO=new IPOTBundleOrganizer(IPOTracks,IPOHs.get(0).z,IPOHs.get(len-1).z);
    }
    public ImagePlus markIPOTs_Bundle(){
        ImagePlus impl0=CommonMethods.copyToRGBImage(implp);
//        ImagePlus impl1=CommonMethods.copyToRGBImage(impl);
//        impl0.close();
//        implp.close();
        Runtime.getRuntime().gc();
//        ImagePlus implc=CommonMethods.combineImagesX(impl0, impl1);
//        ImagePlus implc=CommonMethods.combineImagesX(impl0, impl1);
        if(m_nIPOType==RegularIPO)implp.close();
        Runtime.getRuntime().gc();
        impl0.setTitle("marked IPOTracks and the original image");
        IPOTBO.markIPOTBs(impl0, minPeakPercentileIndex);
        impl0.show();
        return impl0;
    }
    public void exportTrackToAbf(String path){
        int len=IPOTracks.size();
        IPObjectTrack IPOT;
        ArrayList <IPObjectTrack> IPOTs=new ArrayList();
        int type,i;
        ArrayList <IPOTrackArray> IPOTAs=new ArrayList();

        for(i=0;i<4;i++){
            IPOTAs.add(new IPOTrackArray());
        }

        int num=0;
        for(i=0;i<len;i++){
            IPOT=IPOTracks.get(i);
            type=IPOT.getTrackType();
            IPOTAs.get(type).m_vcIPOTs.add(IPOT);
        }

        int position=0;
        IPOT=IPOTracks.get(0);
        int numChannels=IPOT.getNumAbfChannels();

        int trackAbfLength=IPOT.getTrackAbfLength(numChannels)*(m_numDisplayableTracks);
        float[] pfData=new float[trackAbfLength];
        int index=0;
        for(type=0;type<4;type++){
            IPOTs=IPOTAs.get(type).m_vcIPOTs;
            len=IPOTs.size();
            for(i=0;i<len;i++){
                IPOT=IPOTs.get(i);

                if(IPOT.toDisplay()){
                    IPOT.setTrackIndex(index);
                    position=IPOT.exportTrackToAbf(pfData, index, position);
                    index++;
                    if(index>=m_numDisplayableTracks){
                        break;
                    }
                }else{
                    IPOT.setTrackIndex(index+1);
                }
            }
        }

        Abf cAbf=new Abf();
        cAbf.loadHeader(numChannels);
        IPObjectTrack.setADCChannelNamesAndUnits(cAbf.getADCChannelNames(), cAbf.getADCChannelUnits());
        cAbf.exportAsAbf(pfData, numChannels, trackAbfLength/numChannels, path);
    }
    public void exportTrackToAbf_GaussianNodeGroup(String path){
        int len=IPOTracks.size();
        IPObjectTrack IPOT;
        ArrayList <IPObjectTrack> IPOTs=new ArrayList();
        int type,i;
        ArrayList <IPOTrackArray> IPOTAs=new ArrayList();

        for(i=0;i<4;i++){
            IPOTAs.add(new IPOTrackArray());
        }

        int num=0;
        for(i=0;i<len;i++){
            IPOT=IPOTracks.get(i);
            type=IPOT.getTrackType();
            IPOTAs.get(type).m_vcIPOTs.add(IPOT);
        }

        int position=0;
        IPOT=IPOTracks.get(0);
        int numChannels=IPOT.getNumAbfChannels();

        int trackAbfLength=IPOT.getTrackAbfLength(numChannels)*(m_numDisplayableTracks);
        float[] pfData=new float[trackAbfLength];
        int index=0;
        for(type=0;type<4;type++){
            IPOTs=IPOTAs.get(type).m_vcIPOTs;
            len=IPOTs.size();
            for(i=0;i<len;i++){
                IPOT=IPOTs.get(i);

                if(IPOT.toDisplay()){
                    IPOT.setTrackIndex(index);
                    position=IPOT.exportTrackToAbf(pfData, index, position);
                    index++;
                    if(index>=m_numDisplayableTracks){
                        break;
                    }
                }else{
                    IPOT.setTrackIndex(index+1);
                }
            }
        }

        Abf cAbf=new Abf();
        cAbf.loadHeader(numChannels);
        IPObjectTrack.setADCChannelNamesAndUnits(cAbf.getADCChannelNames(), cAbf.getADCChannelUnits());
        cAbf.exportAsAbf(pfData, numChannels, trackAbfLength/numChannels, path);
    }
    public void exportIPOTBundlesToAbf(String path){
        IPOTBO.exportBundlesToAbf(path);
    }
    public static ArrayList<Point> checkIPOTDuplicacy(ArrayList<IPObjectTrack> IPOTs){
        ArrayList <Point> IPOTPairs=new ArrayList();
        Point IPOTPair=new Point();
        IPObjectTrack IPOT,IPOT1;
        int len=IPOTs.size();
        int i,j;
        for(i=0;i<len;i++){
            IPOT=IPOTs.get(i);
            for(j=i+1;j<len;j++){
                IPOT1=IPOTs.get(j);
                if(IPOT.commonHead(IPOT1)&&IPOT.commonEnd(IPOT1)){
                    IPOTPairs.add(new Point(i,j));
                }
            }
        }
        return IPOTPairs;
    }
    
    public void calImageStatistics_GaussianNode(){
        cvIntensityMS=new ArrayList();
        cvBackgroundMS=new ArrayList();
        cvPixelHeights0MS=new ArrayList();
        cvPixelHeightsMS=new ArrayList();
        cvPixelHeightsCMS=new ArrayList();
        nvSelectedParticles=new ArrayList();
        nvBackgroundParticles=new ArrayList();
        dvCutoffs=new ArrayList();
        dvMostProbablePixels=new ArrayList();
        IntensityPeakObject IPO;
        ArrayList<IntensityPeakObject> IPOs;
        ArrayList<Double> backgrounds=new ArrayList();
        ArrayList<Double> phs0=new ArrayList();
        ArrayList<Double> phs=new ArrayList();
        ArrayList<Double> phsC=new ArrayList();

        int nSlices=IPOHs.size(),nParticles;
        int i,j,slice;
        double mean, sum,sums2,dv;
        MeanSem0 ms;
        IntensityPeakObjectHandler_GaussianNode IPOH;
        int[][] pixels;


        for(i=0;i<nSlices;i++){
            IPOH=(IntensityPeakObjectHandler_GaussianNode)IPOHs.get(i);
            slice=IPOH.t;
            pixels=IPOH.getPixels();
            ms=CommonStatisticsMethods.buildMeanSem(pixels);
            cvIntensityMS.add(ms);
            IPOs=IPOH.IPOs;
            nParticles=IPOs.size();
            nvSelectedParticles.add(nParticles);
            nvBackgroundParticles.add(IPOH.backgroundIPOs.size());
            dvCutoffs.add(IPOH.TotalSignalCutoff);
            impl.setSlice(slice);
            dvMostProbablePixels.add((double)CommonMethods.getMostProbablePixelValue(impl, 2));

            backgrounds.clear();
            phs0.clear();
            phs.clear();
            phsC.clear();
            for(j=0;j<nParticles;j++){
                IPO=IPOs.get(j);
//                backgrounds.add((double)IPO.backgroundPixel);
                phs0.add(IPO.getPixelHeight0());
                phs.add(IPO.getPixelHeight());
//                phsC.add(IPO.pixelHeightC);
            }
            ms=CommonStatisticsMethods.buildMeanSem(IPOH.pixelsCompensated);
            cvBackgroundMS.add(ms);
            cvRefMS.add(ms);
            cvPixelHeights0MS.add(CommonStatisticsMethods.buildMeanSem(phs0, 0, nParticles-1, 1));
            cvPixelHeightsMS.add(CommonStatisticsMethods.buildMeanSem(phs, 0, nParticles-1, 1));
            cvPixelHeightsCMS.add(CommonStatisticsMethods.buildMeanSem(phs, 0, nParticles-1, 1));
//            cvPixelHeightsCMS.add(CommonStatisticsMethods.buildMeanSem(phsC, 0, nParticles-1, 1));
        }
    }

    public void calImageStatistics_GaussianNodeGroup(){
        cvIntensityMS=new ArrayList();
        cvBackgroundMS=new ArrayList();
        cvPixelHeights0MS=new ArrayList();
        cvPixelHeightsMS=new ArrayList();
        cvPixelHeightsCMS=new ArrayList();
        nvSelectedParticles=new ArrayList();
        nvBackgroundParticles=new ArrayList();
        dvCutoffs=new ArrayList();
        dvMostProbablePixels=new ArrayList();
        IntensityPeakObject IPO;
        ArrayList<IntensityPeakObject> IPOs;
        ArrayList<Double> backgrounds=new ArrayList();
        ArrayList<Double> phs0=new ArrayList();
        ArrayList<Double> phs=new ArrayList();
        ArrayList<Double> phsC=new ArrayList();

        int nSlices=IPOHs.size(),nParticles;
        int i,j,slice;
        double mean, sum,sums2,dv;
        MeanSem0 ms;
        IntensityPeakObjectHandler_GaussianNodeGroup IPOH;
        int[][] pixels;


        for(i=0;i<nSlices;i++){
            IPOH=(IntensityPeakObjectHandler_GaussianNodeGroup)IPOHs.get(i);
            slice=IPOH.t;
            pixels=IPOH.getPixels();
            ms=CommonStatisticsMethods.buildMeanSem(pixels);
            cvIntensityMS.add(ms);
            IPOs=IPOH.IPOs;
            nParticles=IPOs.size();
            nvSelectedParticles.add(nParticles);
            nvBackgroundParticles.add(IPOH.backgroundIPOs.size());
            dvCutoffs.add(IPOH.TotalSignalCutoff);
            impl.setSlice(slice);
            dvMostProbablePixels.add((double)CommonMethods.getMostProbablePixelValue(impl, 2));

            backgrounds.clear();
            phs0.clear();
            phs.clear();
            phsC.clear();
            for(j=0;j<nParticles;j++){
                IPO=IPOs.get(j);
//                backgrounds.add((double)IPO.backgroundPixel);
                phs0.add(IPO.getPixelHeight0());
                phs.add(IPO.getPixelHeight());
//                phsC.add(IPO.pixelHeightC);
            }
            ms=CommonStatisticsMethods.buildMeanSem(IPOH.pixelsCompensated);
            cvBackgroundMS.add(ms);
            cvRefMS.add(ms);
            cvPixelHeights0MS.add(CommonStatisticsMethods.buildMeanSem(phs0, 0, nParticles-1, 1));
            cvPixelHeightsMS.add(CommonStatisticsMethods.buildMeanSem(phs, 0, nParticles-1, 1));
            cvPixelHeightsCMS.add(CommonStatisticsMethods.buildMeanSem(phs, 0, nParticles-1, 1));
//            cvPixelHeightsCMS.add(CommonStatisticsMethods.buildMeanSem(phsC, 0, nParticles-1, 1));
        }
    }

    public void calImageStatistics(){
        cvIntensityMS=new ArrayList();
        cvBackgroundMS=new ArrayList();
        cvPixelHeights0MS=new ArrayList();
        cvPixelHeightsMS=new ArrayList();
        cvPixelHeightsCMS=new ArrayList();
        nvSelectedParticles=new ArrayList();
        nvBackgroundParticles=new ArrayList();
        dvCutoffs=new ArrayList();
        dvMostProbablePixels=new ArrayList();
        IntensityPeakObject IPO;
        ArrayList<IntensityPeakObject> IPOs;
        ArrayList<Double> backgrounds=new ArrayList();
        ArrayList<Double> phs0=new ArrayList();
        ArrayList<Double> phs=new ArrayList();
        ArrayList<Double> phsC=new ArrayList();

        int nSlices=IPOHs.size(),nParticles;
        int i,j;
        double mean, sum,sums2,dv;
        MeanSem0 ms;
        IntensityPeakObjectHandler IPOH;
        int[][] pixels;

        for(i=0;i<nSlices;i++){
            IPOH=IPOHs.get(i);
            pixels=IPOH.getPixels();
            ms=CommonStatisticsMethods.buildMeanSem(pixels);
            cvIntensityMS.add(ms);
            IPOs=IPOH.IPOs;
            nParticles=IPOs.size();
            nvSelectedParticles.add(nParticles);
            nvBackgroundParticles.add(IPOH.backgroundIPOs.size());
            dvCutoffs.add(IPOH.m_pdPercentileCutoff[IPOH.minPeakPercentileIndex]);
            impl.setSlice(i+1);
            dvMostProbablePixels.add((double)CommonMethods.getMostProbablePixelValue(impl, 2));

            backgrounds.clear();
            phs0.clear();
            phs.clear();
            phsC.clear();
            for(j=0;j<nParticles;j++){
                IPO=IPOs.get(j);
                backgrounds.add((double)IPO.backgroundPixel);
                phs0.add(IPO.getPixelHeight0());
                phs.add(IPO.getPixelHeight());
                phsC.add(IPO.pixelHeightC);
            }
            cvBackgroundMS.add(CommonStatisticsMethods.buildMeanSem(backgrounds, 0, nParticles-1, 1));
            cvPixelHeights0MS.add(CommonStatisticsMethods.buildMeanSem(phs0, 0, nParticles-1, 1));
            cvPixelHeightsMS.add(CommonStatisticsMethods.buildMeanSem(phs, 0, nParticles-1, 1));
            cvPixelHeightsCMS.add(CommonStatisticsMethods.buildMeanSem(phsC, 0, nParticles-1, 1));
        }
    }



    public void exportImageStatiticsToAbf(String path){
        int numChannels=IPOTracks.get(0).getNumAbfChannels();
        int trackAbfLength=IPOTracks.get(0).getTrackAbfLength(numChannels);
        float[] pfData=new float[trackAbfLength];


        Abf cAbf=new Abf();
        cAbf.loadHeader(numChannels);
        IPObjectTrack.setADCChannelNamesAndUnits(cAbf.getADCChannelNames(), cAbf.getADCChannelUnits());
        exportImageStatiticsToAbf(pfData,0,numChannels);
        setADCChannelNamesAndUnits_ImageStatistics(cAbf.getADCChannelNames(), cAbf.getADCChannelUnits());
        cAbf.exportAsAbf(pfData, numChannels, trackAbfLength/numChannels, FileAssist.getExtendedFileName(path, "_Image statitics"));
    }
    public static void setADCChannelNamesAndUnits_ImageStatistics(char[][] channelNames, char[][] units){
        int len=channelNames.length;
        String[] names=new String[len];
        String[] sUnits=new String[len];
        int i,j;
        for(i=0;i<len;i++){
            names[i]="";
            sUnits[i]="";
        }
        int used=0;
        names[0]="mean";
        used++;
        names[1]="MostPrbl";
        used++;
        names[2]="bkgrnd";
        used++;
        names[3]="Cutoff";
        used++;
        names[4]="#Particles";
        used++;
        names[5]="#bkgrnd";
        used++;
        names[6]="P.H.0";
        used++;
        names[7]="P.H.";
        used++;
        names[8]="P.H.C.";
        used++;
        names[9]="RefMean";
        used++;
        names[10]="RefSD";
        int lenc=channelNames[0].length,lenu=units[0].length,len1;
        for(i=0;i<len;i++){
            len1=names[i].length();
            for(j=0;j<lenc;j++){
                if(j<len1){
                    channelNames[i][j]=names[i].charAt(j);
                }else{
                    channelNames[i][j]=0;
                }
                if(j<lenu) units[i][j]=0;//not using units
            }
        }
    }
    int exportImageStatiticsToAbf(float[] pfData,int position0,int numChannels){
        int nSlices=IPOHs.size();

        int items,position=position0;
        int i,j;
        for(i=0;i<nSlices;i++){
            items=0;
            pfData[position]=(float)cvIntensityMS.get(i).mean;
            position++;
            items++;
            pfData[position]=dvMostProbablePixels.get(i).floatValue();
            position++;
            items++;
            pfData[position]=(float)cvBackgroundMS.get(i).mean;
            position++;
            items++;
            pfData[position]=(float)dvCutoffs.get(i).floatValue();
            position++;
            items++;
            pfData[position]=(float)nvSelectedParticles.get(i).floatValue();
            position++;
            items++;
            pfData[position]=(float)nvBackgroundParticles.get(i).floatValue();
            position++;
            items++;
            pfData[position]=(float)cvPixelHeights0MS.get(i).mean;
            position++;
            items++;
            pfData[position]=(float)cvPixelHeightsMS.get(i).mean;
            position++;
            items++;
            pfData[position]=(float)cvPixelHeightsCMS.get(i).mean;
            position++;
            items++;

            pfData[position]=(float)cvRefMS.get(i).mean;
            position++;
            items++;
            
            pfData[position]=(float)cvRefMS.get(i).getSD();
            position++;
            items++;

            for(j=items;j<numChannels;j++){
                pfData[position]=0;
                position++;
            }
        }
        return position;
    }
    public void setTrackDisplayability(boolean bDisplayAllTracks,int nMinLen,int nMaxTrackHeadLatency){
        int len=IPOTracks.size(),nStatus;
        m_numDisplayableTracks=0;
        for(int i=0;i<len;i++){
            nStatus=IPOTracks.get(i).setDisplayability(bDisplayAllTracks, nMinLen, nMaxTrackHeadLatency);
            if(nStatus==1) m_numDisplayableTracks++;
        }
    }
    public int getFirstHandledSlice(){
        return firstHandledSlice;
    }
    public ArrayList<IPObjectTrack> getIPOTracks(){
        return IPOTracks;
    }

    public void assignIPOShapes(){
        int i,len=IPOHs.size(),numIPOs,j;
        int slice;
        ArrayList<IntensityPeakObject> IPOs;
        IntensityPeakObject IPO;
        IntensityPeakObjectHandler IPOH;
        RegionBoundaryAnalyzer cRBA=null;
        stampPS=new int[len][h][w];
        ImageShape cIS;
        int rIndex;
        Point center;
        int stamp[][];
        implp.show();

        for(i=0;i<len;i++){
            IPOH=IPOHs.get(i);
            IPOs=IPOH.getIPOs();

            slice=IPOH.z+1;
            implp.setSlice(slice);

            pixelsp=CommonStatisticsMethods.getIntArray2(pixelsp, w, h);
            CommonMethods.getPixelValue(implp, implp.getCurrentSlice(), pixelsp);

            cRBA=CommonMethods.buildRegionComplex(implp);
            stamp=cRBA.getStamp();
            stampPS[i]=stamp;

            numIPOs=IPOs.size();
            for(j=0;j<numIPOs;j++){
                IPO=IPOs.get(j);
/*                if(IPO.cContourShape!=null){
                    IPO.cIS=IPO.cContourShape;
                    continue;
                }
 *
 */
                rIndex=LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[IPO.cy][IPO.cx]);
                cIS=cRBA.getRegionShape(rIndex);
                IPO.cIS=cIS;
            }
        }
    }
    public void updateOvlpInfoToIPOGs(){
        int i,len=IPOHs.size(),numIPOs,j;
        ArrayList<IntensityPeakObject> IPOs;
        IntensityPeakObject IPO;
        IntensityPeakObjectHandler IPOH;

        for(i=0;i<len;i++){
            IPOH=IPOHs.get(i);
            IPOs=IPOH.getIPOs();
            numIPOs=IPOs.size();
            for(j=0;j<numIPOs;j++){
                IPO=IPOs.get(j);
                IPO.updateOvlpForIPOGs();
            }
        }
    }
}
