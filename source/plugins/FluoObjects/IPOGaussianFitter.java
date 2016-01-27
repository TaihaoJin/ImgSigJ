/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import utilities.CommonMethods;
import utilities.ArrayofArrays.IntArray;
import java.util.ArrayList;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.Point;
import utilities.Geometry.ImageShapes.*;
import utilities.Non_LinearFitting.StandardFittingFunction;
import utilities.Non_LinearFitting.FittingResultsNode;
import java.util.Formatter;
import utilities.io.PrintAssist;
import utilities.io.FileAssist;
import ij.IJ;
import utilities.io.PrintAssist;
import java.awt.event.*;
import java.awt.*;
import ImageAnalysis.RegionBoundaryAnalyzer;
import ImageAnalysis.RegionNode;
import ImageAnalysis.RegionComplexNode;
import ij.gui.Roi;
import utilities.Gui.ImageComparisonViewer;
import utilities.Non_LinearFitting.ImageFittingGUI;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.CommonStatisticsMethods;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.Histogram;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import utilities.io.FileAssist;
import utilities.Non_LinearFitting.FittingResultsNode;
import java.util.Formatter;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.Non_LinearFitting.FittingModelHandler;
import ImageAnalysis.ContourFollower;
import utilities.Non_LinearFitting.Fitting_Function;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.CustomDataTypes.DoubleRange;
import FluoObjects.IPOGaussianNodeHandler;
import FluoObjects.IPOGaussianNode;
import FluoObjects.IPOGaussianNodeCluster;
import utilities.Non_LinearFitting.FittingComparison;


/**
 *
 * @author Taihao
 */
public class IPOGaussianFitter implements ActionListener{
    public static final int exclusion=-11;

    ImagePlus impl,implp,implr,implpr,implF,implD,implF0,implD0,implCompen,resultsImage,implInt,implResults,implComplex,implRegion,implCompen0,implInt0,implComposed;
    FittingModelNode m_cCurrentModel;
    Histogram m_cRefSizeHist,m_cRefHeightHist,m_cRefSumHist,m_cRefBorderSegmentHRatioHist,m_cRefBorderSegmentHeightHist;
    int[][] pixels,pixelsp,pixelsr,pixelspr,stampProcessed,stampProcessedcp,pixelsgd2,stampgd2,pixelsF,pixelsD,exclusionStamp,comboStamp,comboStampcp,pnScratch,pnScratchp,pixelsCompen,pixelsCompenp,stampScratch,pnScratch1,pnScratch2,pnScratchr;
    int[] pnPixelRange;
    int w,h,wr,hr;
    long startTime,endTime;
    ArrayList<RegionNode> m_cvRegionNodes,m_cvCompenRegionNodes,m_cvRefittingRegionNodes;
    ArrayList<RegionComplexNode> m_cvComplexNodes,m_cvRefittingComplexNodes;
    ArrayList<RegionNode> m_cvRegionNodescp;
    ArrayList<RegionComplexNode> m_cvComplexNodescp;
    ImageComparisonViewer m_cViewer;
    ImageFittingGUI m_cFittingGUI;
    RegionBoundaryAnalyzer m_cRBA,m_cRBAcp;
    ArrayList<FittingModelNode> m_cvFittedModels;
    ArrayList<FittingResultsNode> m_cvFittingResultNodes, m_cvRefinedFittingResultNodes,m_cvInvalidFittingResultNodes;
    Non_Linear_Fitter m_cFitter;
    protected ArrayList<ActionListener> m_cvListeners;
    double m_dRegionHCutoff,m_dRegionACutoff, m_dRegionSCutoff,m_dRegionHICutoff;//region height, area and regionSum cutoff, m_dRegionHICutoff is used for
    //initialization of the fitting model components.
    String path,dir,fileName;
    float xSigma=1f,ySigma=1f,fAccuracy=0.001f;
    LandscapeAnalyzerPixelSorting stamperScratch;
    ArrayList<IPOGaussianNode> m_cvGaussianNodes;
    ArrayList<IPOGaussianNodeCluster> m_cvGaussianClusters;

    ImagePlus implShapes;
    boolean silent,saveImages,exportTxtFormat;
    String ThreadName;
    public IPOGaussianFitter(boolean silent,boolean saveImages, boolean exportTxtFormat){
        this.silent=silent;
        this.saveImages=saveImages;
        this.exportTxtFormat=exportTxtFormat;
        m_cvListeners=new ArrayList();
        ThreadName=null;
    }
    public void setThreadName(String threadName){
        this.ThreadName=threadName;
    }
    public IPOGaussianFitter(boolean silent,boolean saveImages){
        this(silent,saveImages,true);
    }
    public IPOGaussianFitter(boolean silent){
        this(silent,true);
    }
    public void fitImage(ImagePlus impl0){
        if(saveImages||exportTxtFormat){
            path=FileAssist.getFilePath("Choosing file path to save the fitted models",FileAssist.defaultDirectory, "Text file", "txt", false);
            dir=FileAssist.getDirectory(path);
            fileName=FileAssist.getFileName(path);
        }
        startTime=System.currentTimeMillis();
        impl=impl0;
        String imageTitle=impl.getTitle();
        int len=imageTitle.length();
        imageTitle=imageTitle.substring(0, len-4);
        pnPixelRange=new int[2];

        implp=CommonMethods.cloneImage(impl);
        implp.setTitle(imageTitle+" -processed image");
        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);
        if(!silent)impl.show();
        if(!silent)implp.show();

        w=impl.getWidth();
        h=impl.getHeight();
        implComplex=CommonMethods.getBlankImage(ImagePlus.GRAY16, w, h);

        exclusionStamp=new int[h][w];
        if(w<250)
            implr=CommonMethods.resizedImage_cropping(impl, 250, 250);
        else
            implr=CommonMethods.cloneImage(impl);

        wr=implr.getWidth();
        hr=implr.getHeight();
        CommonMethods.randomizeImage(implr);
        implr.setTitle(imageTitle+" -reference image");

        implpr=CommonMethods.cloneImage(implr);
        implpr.setTitle(imageTitle+" -processed reference image");
        CommonMethods.GaussianBlur(implpr, xSigma, ySigma, fAccuracy);
        implF=CommonMethods.cloneImage(impl);
        implD=CommonMethods.cloneImage(impl);
        implF.setTitle("Fitting Results of "+impl.getTitle());
        implD.setTitle("Residual Values of "+impl.getTitle());
        if(!silent)implF.show();
        if(!silent)implD.show();
        pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
        pixelsF=CommonStatisticsMethods.getIntArray2(pixelsF, w, h);
        pixelsD=CommonStatisticsMethods.getIntArray2(pixelsD, w, h);
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels,pnPixelRange);
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixelsF);
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixelsD);

        implCompen=CommonMethods.cloneImage(impl);
        implCompen.setTitle("Compensated Image");
        implInt=CommonMethods.cloneImage(impl);
        implInt.setTitle("Integrated Image");

        if(!silent)SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                m_cViewer=new ImageComparisonViewer();
                m_cViewer.setVisible(true);
                m_cViewer=new ImageComparisonViewer();
                m_cViewer.pickSourceImage(impl);
                m_cViewer.pickTargetImage(implp);
                m_cViewer.pickTargetImage(implF);
                m_cViewer.pickTargetImage(implD);
            }
        });
        fitPixels();
        calCompensatedImage_IPOGaussian();
        refitRegionAndComplexes();
        fixInvalidFitting();
        removeRefittedResults();
        refineCompensatedImage_IPOGaussian();
        endTime=System.currentTimeMillis();
        fireActionEvent(new ActionEvent(this,0,"Status: Finished IPOGausian Fitting. Elapsed Time (s): "+((endTime-startTime)/1000)));
        if(exportTxtFormat)exportFittedModels();
        if(saveImages)saveResultImage();
    }
    public void updateAndFitImage(ImagePlus impl0){
        if(saveImages||exportTxtFormat){
            path=FileAssist.getFilePath("Choosing file path to save the fitted models",FileAssist.defaultDirectory, "Text file", "txt", false);
            dir=FileAssist.getDirectory(path);
            fileName=FileAssist.getFileName(path);
        }
        startTime=System.currentTimeMillis();
        impl=impl0;
        String imageTitle=impl.getTitle();
        int len=imageTitle.length();
        imageTitle=imageTitle.substring(0, len-4);
        pnPixelRange=CommonStatisticsMethods.getIntArray(pnPixelRange, 2);

        pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels,pnPixelRange);
        CommonMethods.setPixels(implp, pixels);
        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);
        if(!silent)CommonMethods.refreshImage(impl);
        if(!silent)CommonMethods.refreshImage(implp);

        w=impl.getWidth();
        h=impl.getHeight();
        wr=implr.getWidth();
        hr=implr.getHeight();

        CommonStatisticsMethods.setElements(pnScratch, 0);
        CommonMethods.setPixels(implComplex, pnScratch);

        exclusionStamp=CommonStatisticsMethods.getIntArray2(exclusionStamp, w, h);
        CommonStatisticsMethods.setElements(exclusionStamp, 0);

        pixelsr=CommonStatisticsMethods.getIntArray2(pixelsr, wr, hr);
        CommonStatisticsMethods.copyArray_differentSizes(pixels, pixelsr);
        CommonMethods.setPixels(implr, pixelsr);
        CommonMethods.randomizeImage(implr);

        pnScratchr=CommonStatisticsMethods.getIntArray2(pnScratchr, wr, hr);
        CommonMethods.getPixelValue(implr, implr.getCurrentSlice(), pnScratchr);
        CommonMethods.setPixels(implpr, pnScratchr);
        CommonMethods.GaussianBlur(implpr, xSigma, ySigma, fAccuracy);

//        implF=CommonMethods.cloneImage(impl);
//        implD=CommonMethods.cloneImage(impl);
//        implF.setTitle("Fitting Results of "+impl.getTitle());
//        implD.setTitle("Residual Values of "+impl.getTitle());
//        if(!silent)implF.show();
//        if(!silent)implD.show();
//        pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
        pixelsF=CommonStatisticsMethods.getIntArray2(pixelsF, w, h);
        pixelsD=CommonStatisticsMethods.getIntArray2(pixelsD, w, h);
//        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels,pnPixelRange);
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixelsF);
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixelsD);

        CommonMethods.setPixels(implCompen, pixels);
        CommonMethods.setPixels(implInt, pixels);
//        implCompen=CommonMethods.cloneImage(impl);
//        implCompen.setTitle("Compensated Image");
//        implInt=CommonMethods.cloneImage(impl);
//        implInt.setTitle("Integrated Image");

        if(!silent)SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                m_cViewer=new ImageComparisonViewer();
                m_cViewer.setVisible(true);
                m_cViewer=new ImageComparisonViewer();
                m_cViewer.pickSourceImage(impl);
                m_cViewer.pickTargetImage(implp);
                m_cViewer.pickTargetImage(implF);
                m_cViewer.pickTargetImage(implD);
            }
        });
        fitPixels();
        calCompensatedImage_IPOGaussian();
        refitRegionAndComplexes();
//        refitRegionAndComplexes_GaussianNode();//need to look into it more later
        fixInvalidFitting();
        removeRefittedResults();
        refineCompensatedImage_IPOGaussian();
        endTime=System.currentTimeMillis();
        fireActionEvent(new ActionEvent(this,0,"Status: Finished IPOGausian Fitting. Elapsed Time (s): "+((endTime-startTime)/1000)));
        if(exportTxtFormat)exportFittedModels();
        if(saveImages)saveResultImage();
    }
    void exportFittedModels(){
        Formatter fm=FileAssist.getFormatter(path);
        FittingResultsNode aNode;
        int len=m_cvFittingResultNodes.size(),i;
        for(i=0;i<len;i++){
            aNode=m_cvFittingResultNodes.get(i);
            aNode.ExportFittingResults_MultipleLines(fm);
            fm.flush();
        }
        len=m_cvRefinedFittingResultNodes.size();
        for(i=0;i<len;i++){
            aNode=m_cvRefinedFittingResultNodes.get(i);
            aNode.ExportFittingResults_MultipleLines(fm);
            fm.flush();
        }
        fm.close();
        path=FileAssist.getExtendedFileName(path, "_invalid");
        fm=FileAssist.getFormatter(path);
        len=m_cvInvalidFittingResultNodes.size();
        for(i=0;i<len;i++){
            aNode=m_cvInvalidFittingResultNodes.get(i);
            aNode.ExportFittingResults_MultipleLines(fm);
            fm.flush();
        }
        fm.close();
    }
    void fitPixels(){
        int mainPeak=CommonMethods.getMostProbablePixelValue(impl, 5);
        buildRegionComplexes();
        boolean bypass=false;
        stampGradientMap2();
        m_cvFittedModels=new ArrayList();
        m_cvFittingResultNodes=new ArrayList();
        m_cvInvalidFittingResultNodes=new ArrayList();
        int len=m_cvRegionNodes.size(),i;
        ImageShape cIS;
        Point viewCenter;
        if(!silent)m_cViewer.setMagnification(6);
        if(!silent)m_cViewer.setWindowSize(400, 400);

        double dRef;
        RegionNode aNode;
        Point peak;
        int[][] pixelsRegion=new int[h][w];
        ArrayList<Point>mainPeaks;

        ArrayList<Point> contour;
        ArrayList<Point> peaks=new ArrayList();
        FittingResultsNode aResultsNode;
        int maxRegions=10000;
        for(i=0;i<len;i++){
            aNode=m_cvRegionNodes.get(i);
            if(aNode.fitted) continue;
            cIS=m_cRBA.getRegionShape(aNode);
            aNode.cIS=cIS;
            if(!aNode.significant) continue;
            contour=m_cRBA.getContour(aNode);
            ContourFollower.removeOffBoundPoints(w, h, contour);
            CommonStatisticsMethods.setElements(pixelsRegion, contour, aNode.regionIndex+maxRegions);
            ImageShapeHandler.addValue(pixelsRegion, cIS, aNode.regionIndex);

            if(aNode.complexIndex>=0) continue;
            peak=aNode.peakLocation;
            CommonStatisticsMethods.setElements(exclusionStamp, peak.y-1, peak.y+1, peak.x-1, peak.x+1, exclusion);
            peaks.clear();
            peaks.add(peak);
            
            viewCenter=cIS.getCenter();
            if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
            ActionEvent ae=new ActionEvent(this,0,"Status: fitting region "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+")");
            if(!silent)fireActionEvent(ae);

            final FittingModelNode aModel;
            aModel=IPOGaussianExpander.getInitialIPOGaussianFittingModel(pixels,pixelsp, comboStamp,exclusionStamp,exclusion, cIS, aNode.m_cvBoundaryPoints,"gaussian2D_GaussianPars",m_dRegionHICutoff,aNode.base,peaks);
            m_cCurrentModel=aModel;
            if(aModel.mainPeaks==null)aModel.mainPeaks=new ArrayList();
            aModel.mainPeaks.add(peak);
            aModel.m_cIS=cIS;

            if(aModel.getNumFreePars()<=0) continue;
            if(bypass)
                aResultsNode=new FittingResultsNode(aModel);
            else
                aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);
            if(invalidFitting(aResultsNode,true)) {
                m_cvInvalidFittingResultNodes.add(aResultsNode);
                continue;
            }
            m_cvFittingResultNodes.add(aResultsNode);

            aNode.fitted=true;
            aNode.fittingModel=aModel;

            if(!silent)SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    updateImages(aModel);
                }
            });
        }
        RegionComplexNode cNode;
//        Point p1=new Point(159,163),p2=new Point(153,164);
        len=m_cvComplexNodes.size();
        int regions,j;
        int[][] pixelsComplex=new int[h][w];

        int maxComplexes=Math.max(500, len);
        for(i=0;i<len;i++){
            peaks.clear();
            mainPeaks=new ArrayList();
            cNode=m_cvComplexNodes.get(i);
            if(cNode.fitted) continue;
            regions=cNode.m_cvEnclosedRegions.size();
            for(j=0;j<regions;j++){
                peak=cNode.m_cvEnclosedRegions.get(j).peakLocation;
                CommonStatisticsMethods.setElements(exclusionStamp, peak.y-1, peak.y+1, peak.x-1, peak.x+1, exclusion);
                peaks.add(peak);
                mainPeaks.add(peak);
            }
            cIS=m_cRBA.getRegionComplexShape(cNode);
            cNode.cIS=cIS;
            contour=m_cRBA.getOuterContour(cNode);
            CommonStatisticsMethods.setElements(pixelsComplex, contour, cNode.complexIndex+maxComplexes);
            ImageShapeHandler.addValue(pixelsComplex, cIS, cNode.complexIndex);
            viewCenter=cIS.getCenter();
            if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
            dRef=cNode.base;
            final FittingModelNode aModel;

            aModel=IPOGaussianExpander.getInitialIPOGaussianFittingModel(pixels,pixelsp, comboStamp, exclusionStamp,exclusion, cIS, cNode.m_cvBoundaryPoints,"gaussian2D_GaussianPars",m_dRegionHICutoff,dRef,peaks);
            m_cCurrentModel=aModel;
            aModel.mainPeaks=mainPeaks;
            aModel.m_cIS=cIS;
            ActionEvent ae=new ActionEvent(this,0,"Status: fitting complex "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+") "+cNode.m_cvEnclosedRegions.size()+" regions "
                    +aModel.nComponents+ " components");
            if(!silent)fireActionEvent(ae);

            if(bypass)
                aResultsNode=new FittingResultsNode(aModel);
            else
                aResultsNode=m_cFitter.getFittedModel(aModel, aModel, 0.1,this);
            if(invalidFitting(aResultsNode,true)){
                m_cvInvalidFittingResultNodes.add(aResultsNode);
                continue;
            }
            assignRegionIndexes(aResultsNode.m_cvModels.get(0));
            m_cvFittingResultNodes.add(aResultsNode);
            cNode.fitted=true;
            cNode.fittingModel=aModel;
            if(!silent)SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    updateImages(aModel);
                }
            });
        }
        implRegion=CommonMethods.cloneImage(implComplex);
        CommonMethods.setPixels(implComplex, pixelsComplex);
        CommonMethods.setPixels(implRegion, pixelsRegion);
        if(!silent)implRegion.show();
        if(!silent)implComplex.show();
        len=m_cvRegionNodes.size();
    }
    void fixInvalidFitting(){
        int mainPeak=CommonMethods.getMostProbablePixelValue(impl, 5);
        ArrayList<FittingModelNode> cvModels=new ArrayList();
        int len=m_cvInvalidFittingResultNodes.size(),i;
        for(i=0;i<len;i++){
            cvModels.add(m_cvInvalidFittingResultNodes.get(i).m_cvModels.get(0));
        }
        m_cvInvalidFittingResultNodes.clear();
        ImageShape cIS;
        int rIndex,cIndex;

        FittingResultsNode aResultsNode;
        Point viewCenter;
        boolean invalidConstant;
        for(i=0;i<len;i++){
            final FittingModelNode aModel;
            aModel=cvModels.get(i);
            cIS=aModel.m_cIS;
            viewCenter=cIS.getCenter();
            if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
            ActionEvent ae=new ActionEvent(this,0,"Status: fixing "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+")");
            if(!silent)fireActionEvent(ae);
            aModel.deleteInvalidComponents();
            m_cCurrentModel=aModel;

            invalidConstant=invalidConstant(aModel);
            if(invalidFittingModel(aModel,true)) IPOGaussianExpander.reduceToBasicIPOGaussianFittingModel(pixelsCompen, aModel);
            IPOGaussianExpander.freezePeakLocations(aModel,invalidConstant);
            aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);
            if(CommonMethods.DistToEdge(aModel.mainPeaks,w,h) >2){
                IPOGaussianExpander.freePeakLocations(aModel,invalidConstant);
                aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);
            }

            if(invalidFitting(aResultsNode,true)) {
                IPOGaussianExpander.mergeCrowedPeaks(aModel.mainPeaks, 3.5);
                IPOGaussianExpander.reduceToBasicIPOGaussianFittingModel(pixelsCompen, aModel);
                IPOGaussianExpander.freezePeakLocations(aModel,invalidConstant);
                aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);
                if(CommonMethods.DistToEdge(aModel.mainPeaks,w,h) >3){
                    IPOGaussianExpander.freePeakLocations(aModel,invalidConstant);
                    aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);
                }
                if(invalidFitting(aResultsNode,false)) {
                    m_cvInvalidFittingResultNodes.add(aResultsNode);
                    aModel.invalid=true;
                    continue;
                }
            }
            aModel.invalid=false;
            assignRegionIndexes(aResultsNode.m_cvModels.get(0));
            m_cvRefinedFittingResultNodes.add(aResultsNode);

            if(!silent)SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    updateImages(aModel);
                }
            });
        }
        if(!silent)SwingUtilities.invokeLater(new Runnable(){
            public void run(){undoInvalidImageUpdates();}
        });
    }

    void undoInvalidImageUpdates(){
        int i,len=m_cvInvalidFittingResultNodes.size();
        for(i=0;i<len;i++){
            undoImagesUpdate(m_cvInvalidFittingResultNodes.get(i).m_cvModels.get(0));
        }
    }
    void updateImages(FittingModelNode aModel){
        int len=aModel.m_pdX.length;
        double[] pdY=aModel.m_pdY,pdFittedY=aModel.pdFittedY;
        double[][] pdX=aModel.m_pdX;
        aModel.updateFittedData();
        int pixelConst=(int)(aModel.pdPars[0]+0.5);
        int i,x,y,pixel,pixelF;
        for(i=0;i<len;i++){
            x=(int)(pdX[i][0]+0.5);
            y=(int)(pdX[i][1]+0.5);
            pixel=(int)(pdY[i]+0.5);
            pixelF=(int)(pdFittedY[i]+0.5);
            pixelsF[y][x]=pixelF;
            pixelsD[y][x]=pixel-pixelF+pixelConst;
            if(pixelsD[y][x]<0||pixelsD[y][x]>3000) {
                i=i;
            }
        }
        CommonMethods.setPixels(implF, pixelsF);
        CommonMethods.setPixels(implD, pixelsD);
        CommonMethods.refreshImage(implF);
        CommonMethods.refreshImage(implD);
        m_cViewer.synchronizeDisplay();
    }
    void undoImagesUpdate(FittingModelNode aModel){
        int len=aModel.m_pdX.length;
        double[] pdY=aModel.m_pdY,pdFittedY=aModel.pdFittedY;
        double[][] pdX=aModel.m_pdX;
        aModel.updateFittedData();
        int pixelConst=(int)(aModel.pdPars[0]+0.5);
        int i,x,y,pixel,pixelF;
        for(i=0;i<len;i++){
            x=(int)(pdX[i][0]+0.5);
            y=(int)(pdX[i][1]+0.5);
            pixel=(int)(pdY[i]+0.5);
            pixelF=(int)(pdFittedY[i]+0.5);
            pixelsF[y][x]=pixels[y][x];
            pixelsD[y][x]=pixels[y][x];
            if(pixelsD[y][x]<0||pixelsD[y][x]>3000) {
                i=i;
            }
        }
        CommonMethods.setPixels(implF, pixelsF);
        CommonMethods.setPixels(implD, pixelsD);
        CommonMethods.refreshImage(implF);
        CommonMethods.refreshImage(implD);
        m_cViewer.synchronizeDisplay();
    }
    
    void stampGradientMap2(){
        pixelsp=CommonStatisticsMethods.getIntArray2(pixelsp, w, h);
        CommonMethods.getPixelValue(implp, implp.getCurrentSlice(), pixelsp);
        stampgd2=CommonStatisticsMethods.getIntArray2(stampgd2, w, h);
        pixelsgd2=CommonStatisticsMethods.getIntArray2(pixelsgd2, w, h);

        CommonMethods.getSecondOrderGradientMap(pixelsp, pixelsgd2);
        intRange ir=CommonStatisticsMethods.getRange(pixelsgd2);
        int[] pnRange={ir.getMin(),ir.getMax()};
        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(w,h,pnRange);
        stampper.updateAndStampPixels(pixelsgd2, stampgd2);
    }

    void buildRegionComplexes(){
        double pValue=0.2;

        pixelspr=CommonMethods.getPixelValues(implpr);
        RegionBoundaryAnalyzer cRBAr=CommonMethods.buildRegionComplex(implpr);
        cRBAr.buildRegionPixelStatistics(pixelspr);
        cRBAr.buildRegionHistograms(pixelspr);
        cRBAr.calBorderSegmentHeightsAndRatios(pixelspr);
        m_cRefSizeHist=cRBAr.getRegionAreaHistogram();
        m_cRefHeightHist=cRBAr.getRegionHeightHistogram();
        m_cRefSumHist=cRBAr.getRegionSumHist();
        m_cRefBorderSegmentHRatioHist=cRBAr.getBorderSegmentRatioHistogram();
        m_cRefBorderSegmentHeightHist=cRBAr.getBorderSegmentHeightHistogram(pixelspr);

        double percentile=1.-pValue;
        m_cRefSizeHist.setPercentile(percentile);
        m_cRefHeightHist.setPercentile(percentile);
        m_cRefSumHist.setPercentile(percentile);
        m_cRefBorderSegmentHRatioHist.setPercentile(percentile);
        m_cRefBorderSegmentHeightHist.setPercentile(percentile);


        m_dRegionHCutoff=m_cRefHeightHist.getPercentileValue();
        m_cRefHeightHist.setPercentile(0.7);
        m_dRegionHICutoff=m_cRefHeightHist.getPercentileValue();
        m_dRegionACutoff=m_cRefSizeHist.getPercentileValue();
        m_dRegionSCutoff=m_cRefSumHist.getPercentileValue();
        double rCutoff=m_cRefBorderSegmentHRatioHist.getPercentileValue();

        pixelsp=CommonStatisticsMethods.getIntArray2(pixelsp, w, h);
        CommonMethods.getPixelValue(implp, implp.getCurrentSlice(), pixelsp);

        m_cRBA=CommonMethods.buildRegionComplex(implp);
//        m_cRBA.removeOverheightBorderSegments(pixelsp, m_cRefBorderSegmentHeightHist.getPercentileValue(), false);
        m_cRBA.buildRegionPixelStatistics(pixelsp);
        m_cRBA.markSignificantRegions_Sum(m_dRegionSCutoff);
        m_cRBA.calBorderSegmentHeightsAndRatios(pixelsp);
        m_cRBA.removeCloseToPeakBorderSegments(6.1*6.1);
        m_cvRegionNodes=m_cRBA.getRegionNodes();
        m_cvComplexNodes=m_cRBA.getComplexNodes();
        stampProcessed=m_cRBA.getStamp();
        m_cRBA.calComplexBases(pixelsp);
        m_cRBA.assignRegionAndComplexBoundaryPoints();

        ArrayList<Point> comboMaxima=CommonMethods.getComboMaxima(implp);
        comboStamp=CommonStatisticsMethods.getIntArray2(comboStamp, w, h);
        int len=comboMaxima.size();
        Point pt;
        for(int i=0;i<len;i++){
            pt=comboMaxima.get(i);
            LandscapeAnalyzerPixelSorting.setLandscapeType(comboStamp, pt.x, pt.y, LandscapeAnalyzerPixelSorting.localMaximum, 0);
        }
    }

    void buildRegionComplexesOfCompensatedImage(){
        ImagePlus implcp=CommonMethods.cloneImage(implCompen);
        CommonMethods.GaussianBlur(implcp, xSigma, ySigma, fAccuracy);
        int[][] pixelscp=CommonMethods.getPixelValues(implcp);

        m_cRBAcp=CommonMethods.buildRegionComplex(implcp);
        m_cRBAcp.buildRegionPixelStatistics(pixelscp);
        m_cRBAcp.markSignificantRegions_Sum(m_dRegionSCutoff);
        m_cvRegionNodescp=m_cRBAcp.getRegionNodes();
        stampProcessedcp=m_cRBAcp.getStamp();
    }

    void prepareRefittingRegionsAndComplexes(){
        RegionNode rNode,rNodeCp;
        RegionComplexNode cNode;
        Point peak;
        FittingModelNode aModel;

        if(saveImages){
            implCompen0=CommonMethods.cloneImage(implCompen);
            implCompen0.setTitle("Compensated Image0");
            implInt0=CommonMethods.cloneImage(implInt);
            implInt0.setTitle("Integrated Image0");
            if(!silent)implCompen0.show();
            if(!silent)implInt0.show();
            implF0=CommonMethods.cloneImage(implF);
            implF0.setTitle("Fitted Image0");
            implD0=CommonMethods.cloneImage(implD);
            implD0.setTitle("Residual Image0");
            if(!silent)implF0.show();
            if(!silent)implD0.show();
        }
        int len=m_cvRegionNodescp.size(),i,rIndex,cIndex;
        for(i=0;i<len;i++){
            rNodeCp=m_cvRegionNodescp.get(i);
            if(!rNodeCp.significant) continue;

            peak=rNodeCp.peakLocation;
            if(CommonMethods.getDist2(peak.x, peak.y, 135, 43)<81){
                i=i;
            }
            rIndex=LandscapeAnalyzerPixelSorting.getRegionIndex(stampProcessed[peak.y][peak.x])-1;
            rNode=m_cvRegionNodes.get(rIndex);
            cIndex=rNode.complexIndex;//cIndex does start from 0
            if(cIndex>=0) {//it's within a complex
                cNode=m_cvComplexNodes.get(cIndex);
                if(!RegionBoundaryAnalyzer.containsRegion(cNode, rNode)) IJ.error("inconsistency in method: prepareRefittingRegionsAndComplexes");
                if(cNode.fittingModel!=null){
                    aModel=cNode.fittingModel;
                    if(cNode.previousModel==null)cNode.previousModel=new FittingModelNode(aModel);
                    IPOGaussianExpander.addOneGaussianComponent(aModel, pixelsCompen, peak, "gaussian2D_GaussianPars");
                    cNode.fitted=false;
                }else{//the inifitial fitting was invalid. will be refitted in new background
                    cNode.fittingModel=null;
                }
                cNode.fitted=false;
            }else{
                if(rNode.significant){
                    if(rNode.fittingModel!=null){
                        aModel=rNode.fittingModel;

                        if(rNode.previousModel==null) rNode.previousModel=new FittingModelNode(aModel);
                        IPOGaussianExpander.addOneGaussianComponent(aModel, pixelsCompen, peak, "gaussian2D_GaussianPars");
                    }
                }else{
                    rNode.significant=true;
                }
                rNode.fitted=false;
            }
        }
    }
    void refineRegionPixelStatistics(){
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, w, h);
        int len=m_cvRegionNodes.size(),i;
        RegionNode rNode;
        RegionComplexNode cNode;
        for(i=0;i<len;i++){
            rNode=m_cvRegionNodes.get(i);
            restoreCompensatedPixels(rNode,pnScratch);
            m_cRBA.calRegionPixelStatistics(rNode, pixels);
        }
        int rIndex,len1,j;
        for(i=0;i<len;i++){
            cNode=m_cvComplexNodes.get(i);
            restoreCompensatedPixels(cNode,pnScratch);
            m_cRBA.calRegionComplexPixelStatistics(cNode, pixels);//take care of this function tomorrow
        }
    }
    void restoreCompensatedPixels(RegionNode rNode, int[][] pixels){
        int i,j,len,x,y;
        if(rNode.cXRange.emptyRange()){
            m_cRBA.buildRegionRanges();
        }
        int iI=rNode.cYRange.getMin()-1;
        if(iI<0) iI=0;
        int iF=rNode.cYRange.getMax()+1;
        if(iF>=h) iF=h-1;

        int jI=rNode.cXRange.getMin()-1;
        if(jI<0) jI=0;
        int jF=rNode.cXRange.getMax()+1;
        if(jF>=w) jF=w-1;

        CommonStatisticsMethods.copyArray(pixelsCompen, pixels,iI,iF,jI,jF);

        Point p;

        if(rNode.fittingModel!=null){
            FittingModelNode aModel=rNode.fittingModel;
            if(rNode.previousModel!=null) aModel=rNode.previousModel;
            double[] pdX=new double[2],pdPars=aModel.pdPars;
            double cnst=pdPars[0];
            Fitting_Function func=new ComposedFittingFunction(aModel.svFunctionTypes);
            for(i=iI;i<=iF;i++){
                for(j=jI;j<=jF;j++){
                    pdX[0]=j;
                    pdX[1]=i;
                    pixels[i][j]+=func.fun(pdPars, pdX)-cnst;
                }
            }
        }
    }
    void restoreCompensatedPixels(RegionComplexNode cNode, int[][] pixels){
        int i,j,len,x,y;
        if(cNode.cXRange.emptyRange()){
            m_cRBA.buildComplexRanges();
        }
        int iI=cNode.cYRange.getMin()-1;
        if(iI<0) iI=0;
        int iF=cNode.cYRange.getMax()+1;
        if(iF>=h) iF=h-1;

        int jI=cNode.cXRange.getMin()-1;
        if(jI<0) jI=0;
        int jF=cNode.cXRange.getMax()+1;
        if(jF>=w) jF=w-1;

        CommonStatisticsMethods.copyArray(pixelsCompen, pixels,iI,iF,jI,jF);

        Point p;

        if(cNode.fittingModel!=null){
            FittingModelNode aModel=cNode.fittingModel;
            if(cNode.previousModel!=null) aModel=cNode.previousModel;
            double[] pdX=new double[2],pdPars=aModel.pdPars;
            double cnst=pdPars[0];
            Fitting_Function func=new ComposedFittingFunction(aModel.svFunctionTypes);
            for(i=iI;i<=iF;i++){
                for(j=jI;j<=jF;j++){
                    pdX[0]=j;
                    pdX[1]=i;
                    pixels[i][j]+=func.fun(pdPars, pdX)-cnst;
                }
            }
        }
    }
    void refitRegionAndComplexes(){
        buildRegionComplexesOfCompensatedImage();
        prepareRefittingRegionsAndComplexes();
        m_cvRefinedFittingResultNodes=new ArrayList();
        m_cvInvalidFittingResultNodes.clear();
        stampScratch=CommonStatisticsMethods.getIntArray2(stampScratch, w, h);
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, w, h);
        pnScratchp=CommonStatisticsMethods.getIntArray2(pnScratchp, w, h);
        pnScratch1=CommonStatisticsMethods.getIntArray2(pnScratch1, w, h);
        pnScratch2=CommonStatisticsMethods.getIntArray2(pnScratch2, w, h);
        int len=m_cvRegionNodes.size(),i;
        ImageShape cIS;
        Point viewCenter;

        RegionNode aNode;
        Point peak;

        ArrayList<Point> peaks=new ArrayList();
        ArrayList<Point>mainPeaks;
        FittingResultsNode aResultsNode;

        int iI,iF,jI,jF;
        for(i=0;i<len;i++){
            final FittingModelNode aModel;
            aNode=m_cvRegionNodes.get(i);
            aNode.fitted=false;
            if(!aNode.significant) continue;
            if(aNode.complexIndex>=0) continue;
            cIS=aNode.cIS;

            restoreCompensatedPixels(aNode,pnScratch);
            iI=aNode.cYRange.getMin()-1;
            if(iI<0) iI=0;
            iF=aNode.cYRange.getMax()+1;
            if(iF>=h) iF=h-1;

            jI=aNode.cXRange.getMin()-1;
            if(jI<0) jI=0;
            jF=aNode.cXRange.getMax()+1;
            if(jF>=w) jF=w-1;

            CommonStatisticsMethods.copyArray(pnScratch, pnScratchp, iI, iF, jI, jF);
            CommonStatisticsMethods.meanFiltering(pnScratchp, iI, iF, jI, jF, 1);

            if(aNode.fittingModel==null){
                peak=aNode.peakLocation;

                peaks.clear();
                peaks.add(peak);

                viewCenter=cIS.getCenter();
                if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
                ActionEvent ae=new ActionEvent(this,0,"Status: fitting region "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+")");
                if(!silent)fireActionEvent(ae);

                m_cRBA.calRegionPixelStatistics(aNode, pnScratch);


                if(stamperScratch==null) stamperScratch=new LandscapeAnalyzerPixelSorting(w,h,pnPixelRange);
                stamperScratch.updateAndStampPixels(pnScratchp, stampScratch, iI, iF, jI, jF);
                CommonStatisticsMethods.setElements(exclusionStamp, iI, iF, jI, jF, 0);
                CommonStatisticsMethods.setElements(exclusionStamp, peak.y-1, peak.y+1, peak.x-1, peak.x+1, exclusion);
                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.localMinimum, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.watershed, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.buildComboStamp(stamperScratch, pnScratchp, stampScratch, comboStamp, exclusionStamp, pnScratch1, pnScratch2, exclusion, iI, iF, jI, jF);
                aModel=IPOGaussianExpander.getInitialIPOGaussianFittingModel(pnScratch,pnScratchp, comboStamp,exclusionStamp,exclusion, cIS, aNode.m_cvBoundaryPoints,"gaussian2D_GaussianPars",m_dRegionHICutoff,aNode.base,peaks);
                m_cCurrentModel=aModel;
                if(aModel.mainPeaks==null)aModel.mainPeaks=new ArrayList();
                aModel.mainPeaks.add(peak);
                aModel.m_cIS=cIS;
                aModel.rIndex=aNode.regionIndex;
                aModel.cIndex=aNode.complexIndex;
            }else{
                aModel=aNode.fittingModel;
                replacePixels(aModel,pnScratch,pnScratchp);
            }

            if(aModel.getNumFreePars()<=0) continue;

            aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);
            aNode.fittingModel=aModel;

            if(invalidFitting(aResultsNode,true)) {
                m_cvInvalidFittingResultNodes.add(aResultsNode);
                aModel.invalid=true;
                continue;
            }
            m_cvRefinedFittingResultNodes.add(aResultsNode);
            assignRegionIndexes(aResultsNode.m_cvModels.get(0));

            aNode.fitted=true;
            aModel.invalid=false;
            if(!silent) SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    updateImages(aModel);
                }
            });
        }

        RegionComplexNode cNode;
        Point p1=new Point(159,163),p2=new Point(153,164);
        len=m_cvComplexNodes.size();
        int regions,j;
        int[][] pixelsComplex=new int[h][w];

        int maxComplexes=Math.max(500, len);
        for(i=0;i<len;i++){
            final FittingModelNode aModel;

            cNode=m_cvComplexNodes.get(i);
            cNode.fitted=false;
//            if(cNode.fitted) continue;
            cIS=cNode.cIS;

            restoreCompensatedPixels(cNode,pnScratch);
            iI=cNode.cYRange.getMin()-1;
            if(iI<0) iI=0;
            iF=cNode.cYRange.getMax()+1;
            if(iF>=h) iF=h-1;

            jI=cNode.cXRange.getMin()-1;
            if(jI<0) jI=0;
            jF=cNode.cXRange.getMax()+1;
            if(jF>=w) jF=w-1;

            CommonStatisticsMethods.copyArray(pnScratch, pnScratchp, iI, iF, jI, jF);
            CommonStatisticsMethods.meanFiltering(pnScratchp, iI, iF, jI, jF, 1);

            if(cNode.fittingModel==null){
                peaks.clear();
                mainPeaks=new ArrayList();

                m_cRBA.calComplexBases(pnScratch);

                if(stamperScratch==null) stamperScratch=new LandscapeAnalyzerPixelSorting(w,h,pnPixelRange);
                stamperScratch.updateAndStampPixels(pnScratchp, stampScratch, iI, iF, jI, jF);
                CommonStatisticsMethods.setElements(exclusionStamp, iI, iF, jI, jF, 0);

                regions=cNode.m_cvEnclosedRegions.size();
                for(j=0;j<regions;j++){
                    aNode=cNode.m_cvEnclosedRegions.get(j);
                    peak=aNode.peakLocation;
                    CommonStatisticsMethods.setElements(exclusionStamp, peak.y-1, peak.y+1, peak.x-1, peak.x+1, exclusion);
                    peaks.add(peak);
                    mainPeaks.add(peak);
                }

                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.localMinimum, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.watershed, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.buildComboStamp(stamperScratch, pnScratchp, stampScratch, comboStamp, exclusionStamp, pnScratch1, pnScratch2, exclusion, iI, iF, jI, jF);
                aModel=IPOGaussianExpander.getInitialIPOGaussianFittingModel(pnScratch,pnScratchp, comboStamp,exclusionStamp,exclusion, cIS, cNode.m_cvBoundaryPoints,"gaussian2D_GaussianPars",m_dRegionHICutoff,cNode.base,peaks);
                aModel.mainPeaks=mainPeaks;
            }else{
                aModel=cNode.fittingModel;
                replacePixels(aModel,pnScratch,pnScratchp);
            }
            viewCenter=cIS.getCenter();
            if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
            ActionEvent ae=new ActionEvent(this,0,"Status: fitting complex "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+")");
            if(!silent)fireActionEvent(ae);
            m_cCurrentModel=aModel;
            aModel.m_cIS=cIS;
            aModel.cIndex=cNode.complexIndex;

            if(aModel.getNumFreePars()<=0) continue;

            aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);
            cNode.fittingModel=aModel;

            if(invalidFitting(aResultsNode,true)) {
                m_cvInvalidFittingResultNodes.add(aResultsNode);
                aModel.invalid=true;
                continue;
            }
            assignRegionIndexes(aResultsNode.m_cvModels.get(0));
            m_cvRefinedFittingResultNodes.add(aResultsNode);

            cNode.fitted=true;
            aModel.invalid=false;
            if(!silent)SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    updateImages(aModel);
                }
            });
        }
    }
    public ArrayList<RegionNode> getFittedRegionNodes(){
        return m_cvRegionNodes;
    }
    public ArrayList<RegionComplexNode> getFittedComplexNodes(){
        return m_cvComplexNodes;
    }
    public int getWidth(){
        return w;
    }
    public int getHeigh(){
        return h;
    }
    void refitRegionAndComplexes_GaussianNode(){//need to look into in more detail. Not using in this version
        buildRegionComplexesOfCompensatedImage();
        prepareRefittingRegionsAndComplexes();
        m_cvGaussianNodes=new ArrayList();
        m_cvGaussianClusters=new ArrayList();
        m_cvRefinedFittingResultNodes=new ArrayList();
        m_cvInvalidFittingResultNodes.clear();
        stampScratch=CommonStatisticsMethods.getIntArray2(stampScratch, w, h);
        pnScratch=CommonStatisticsMethods.getIntArray2(pnScratch, w, h);
        pnScratchp=CommonStatisticsMethods.getIntArray2(pnScratchp, w, h);
        pnScratch1=CommonStatisticsMethods.getIntArray2(pnScratch1, w, h);
        pnScratch2=CommonStatisticsMethods.getIntArray2(pnScratch2, w, h);
        int len=m_cvRegionNodes.size(),i;
        ImageShape cIS;
        Point viewCenter;
        ArrayList<Boolean> bvFitted0,bvFitted;

        RegionNode aNode;
        Point peak;

        ArrayList<Point> peaks=new ArrayList();
        ArrayList<Point>mainPeaks;
        FittingResultsNode aResultsNode;

        int iI,iF,jI,jF,j,nComponents0,nComponents;
        for(i=0;i<len;i++){
            final FittingModelNode aModel;
            aNode=m_cvRegionNodes.get(i);
            aNode.fitted=false;
            if(!aNode.significant) continue;
            if(aNode.complexIndex>=0) continue;
            cIS=aNode.cIS;

            restoreCompensatedPixels(aNode,pnScratch);
            iI=aNode.cYRange.getMin()-1;
            if(iI<0) iI=0;
            iF=aNode.cYRange.getMax()+1;
            if(iF>=h) iF=h-1;

            jI=aNode.cXRange.getMin()-1;
            if(jI<0) jI=0;
            jF=aNode.cXRange.getMax()+1;
            if(jF>=w) jF=w-1;

            CommonStatisticsMethods.copyArray(pnScratch, pnScratchp, iI, iF, jI, jF);
            CommonStatisticsMethods.meanFiltering(pnScratchp, iI, iF, jI, jF, 1);

            if(aNode.fittingModel==null){
                peak=aNode.peakLocation;

                peaks.clear();
                peaks.add(peak);

                viewCenter=cIS.getCenter();
                if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
                ActionEvent ae=new ActionEvent(this,0,"Status: fitting region "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+")");
                if(!silent)fireActionEvent(ae);

                m_cRBA.calRegionPixelStatistics(aNode, pnScratch);

                if(stamperScratch==null) stamperScratch=new LandscapeAnalyzerPixelSorting(w,h,pnPixelRange);
                stamperScratch.updateAndStampPixels(pnScratchp, stampScratch, iI, iF, jI, jF);
                CommonStatisticsMethods.setElements(exclusionStamp, iI, iF, jI, jF, 0);
                CommonStatisticsMethods.setElements(exclusionStamp, peak.y-1, peak.y+1, peak.x-1, peak.x+1, exclusion);
                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.localMinimum, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.watershed, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.buildComboStamp(stamperScratch, pnScratchp, stampScratch, comboStamp, exclusionStamp, pnScratch1, pnScratch2, exclusion, iI, iF, jI, jF);
                aModel=IPOGaussianExpander.getInitialIPOGaussianFittingModel(pnScratch,pnScratchp, comboStamp,exclusionStamp,exclusion, cIS, aNode.m_cvBoundaryPoints,"gaussian2D_GaussianPars",m_dRegionHICutoff,aNode.base,peaks);
                m_cCurrentModel=aModel;
                if(aModel.mainPeaks==null)aModel.mainPeaks=new ArrayList();
                aModel.mainPeaks.add(peak);
                aModel.m_cIS=cIS;
                bvFitted=new ArrayList();
                nComponents=aModel.nComponents;
                aModel.bvFitted=new ArrayList();
                for(j=0;j<nComponents;j++){
                    aModel.bvFitted.add(false);
                }
            }else{
                aModel=aNode.fittingModel;
                nComponents=aModel.nComponents;
                nComponents0=0;
                aModel.bvFitted=new ArrayList();
                if(aNode.previousModel!=null) {
                    nComponents0=aNode.previousModel.nComponents;
                    for(j=0;j<nComponents0;j++){
                        aModel.bvFitted.add(true);
                    }
                }
                for(j=nComponents0;j<nComponents;j++){
                    aModel.bvFitted.add(false);
                }
            }

            if(aModel.getNumFreePars()<=0) {
                continue;
            }

//            aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);//11829
            aNode.fittingModel=aModel;
/*
            if(invalidFitting(aResultsNode,true)) {
                m_cvInvalidFittingResultNodes.add(aResultsNode);
                continue;
            }
            m_cvRefinedFittingResultNodes.add(aResultsNode);
            assignRegionIndexes(aResultsNode.m_cvModels.get(0));*///11829

        }

        RegionComplexNode cNode;
        Point p1=new Point(159,163),p2=new Point(153,164);
        len=m_cvComplexNodes.size();
        int regions;
        int[][] pixelsComplex=new int[h][w];

        int maxComplexes=Math.max(500, len);
        for(i=0;i<len;i++){
            final FittingModelNode aModel;

            cNode=m_cvComplexNodes.get(i);
//            if(cNode.fitted) continue;
            cIS=cNode.cIS;

            restoreCompensatedPixels(cNode,pnScratch);
            iI=cNode.cYRange.getMin()-1;
            if(iI<0) iI=0;
            iF=cNode.cYRange.getMax()+1;
            if(iF>=h) iF=h-1;

            jI=cNode.cXRange.getMin()-1;
            if(jI<0) jI=0;
            jF=cNode.cXRange.getMax()+1;
            if(jF>=w) jF=w-1;

            CommonStatisticsMethods.copyArray(pnScratch, pnScratchp, iI, iF, jI, jF);
            CommonStatisticsMethods.meanFiltering(pnScratchp, iI, iF, jI, jF, 1);

            if(cNode.fittingModel==null){
                peaks.clear();
                mainPeaks=new ArrayList();

                m_cRBA.calComplexBases(pnScratch);

                if(stamperScratch==null) stamperScratch=new LandscapeAnalyzerPixelSorting(w,h,pnPixelRange);
                stamperScratch.updateAndStampPixels(pnScratchp, stampScratch, iI, iF, jI, jF);
                CommonStatisticsMethods.setElements(exclusionStamp, iI, iF, jI, jF, 0);

                regions=cNode.m_cvEnclosedRegions.size();
                for(j=0;j<regions;j++){
                    aNode=cNode.m_cvEnclosedRegions.get(j);
                    peak=aNode.peakLocation;
                    CommonStatisticsMethods.setElements(exclusionStamp, peak.y-1, peak.y+1, peak.x-1, peak.x+1, exclusion);
                    peaks.add(peak);
                    mainPeaks.add(peak);
                }

                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.localMinimum, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.excludeProximityOfSpecialLandscapePoints(stampScratch, exclusionStamp, LandscapeAnalyzerPixelSorting.watershed, 1, exclusion, iI, iF, jI, jF);
                CommonMethods.buildComboStamp(stamperScratch, pnScratchp, stampScratch, comboStamp, exclusionStamp, pnScratch1, pnScratch2, exclusion, iI, iF, jI, jF);
                aModel=IPOGaussianExpander.getInitialIPOGaussianFittingModel(pnScratch,pnScratchp, comboStamp,exclusionStamp,exclusion, cIS, cNode.m_cvBoundaryPoints,"gaussian2D_GaussianPars",m_dRegionHICutoff,cNode.base,peaks);
                aModel.mainPeaks=mainPeaks;
                nComponents=aModel.nComponents;
                aModel.bvFitted=new ArrayList();
                for(j=0;j<nComponents;j++){
                    aModel.bvFitted.add(false);
                }
            }else{
                aModel=cNode.fittingModel;
                nComponents=aModel.nComponents;
                nComponents0=0;
                aModel.bvFitted=new ArrayList();
                if(cNode.previousModel!=null) {
                    nComponents0=cNode.previousModel.nComponents;
                    for(j=0;j<nComponents0;j++){
                        aModel.bvFitted.add(true);
                    }
                }
                for(j=nComponents0;j<nComponents;j++){
                    aModel.bvFitted.add(false);
                }
//                replacePixels(aModel,pnScratch,pnScratchp);//11829
            }
            viewCenter=cIS.getCenter();
            if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
            ActionEvent ae=new ActionEvent(this,0,"Status: fitting complex "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+")");
            if(!silent)fireActionEvent(ae);
            m_cCurrentModel=aModel;
            aModel.m_cIS=cIS;
            assignRegionIndexes(aModel);

            if(aModel.getNumFreePars()<=0) continue;

//            aResultsNode = m_cFitter.getFittedModel(aModel, aModel, 0.1, this);//11829
            cNode.fittingModel=aModel;

/*            if(invalidFitting(aResultsNode,true)) {
                m_cvInvalidFittingResultNodes.add(aResultsNode);
                continue;
            }
            assignRegionIndexes(aResultsNode.m_cvModels.get(0));
            m_cvRefinedFittingResultNodes.add(aResultsNode);
            cNode.fitted=true;
            *///11829

        }

        IPOGaussianNodeHandler handler=new IPOGaussianNodeHandler(m_cvRegionNodes,m_cvComplexNodes,0.2,w,h);
        ArrayList<IPOGaussianNode> IPOs=handler.m_cvIPOGaussianNodes;
        ArrayList<IPOGaussianNodeCluster> clusters=handler.m_cvIPOGaussianClusters;

        IPOGaussianNode IPO;
        ArrayList<FittingModelNode> trialModels=new ArrayList();
        len=IPOs.size();
        FittingModelNode aModel;
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            if(!IPO.cvIndexesInCluster.isEmpty()) continue;
            aModel=buildFittingModel(IPO,pixelsCompen);
            trialModels.add(aModel);
        }

        len=clusters.size();
        for(i=0;i<len;i++){
            aModel=buildFittingModel(clusters.get(i),pixelsCompen);
            trialModels.add(aModel);
        }

        len=trialModels.size();
        for(i=0;i<len;i++){
            final FittingModelNode aModelt;
            aModelt=trialModels.get(i);
            viewCenter=aModelt.m_cIS.getCenter();
            if(!silent)m_cViewer.setViewCenter(impl, viewCenter);
            ActionEvent ae=new ActionEvent(this,0,"Status: fitting complex "+(i+1)+ " of "+len+" ("+viewCenter.x+", "+viewCenter.y+")");
            if(!silent)fireActionEvent(ae);
            m_cCurrentModel=aModelt;

            aResultsNode = m_cFitter.getFittedModel(aModelt, aModelt, 0.1, this);

            if(invalidFitting(aResultsNode,true)) {
                m_cvInvalidFittingResultNodes.add(aResultsNode);
                continue;
            }
            m_cvRefinedFittingResultNodes.add(aResultsNode);

            if(!silent)SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    updateImages(aModelt);
                }
            });

        }
    }
    FittingModelNode buildFittingModel(IPOGaussianNode IPO, int[][] pixels0){
        double[] pdY, pdX[],X=new double[2];
        ImageShape cIS=IPOGaussianNodeHandler.buildIPOShape(IPO, IPO.Amp*0.011);
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        ArrayList<Point> points=new ArrayList();
        cIS.getInnerPoints(points);
        int i,j,len=points.size();
        Point pt;
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction func=new ComposedFittingFunction(svT);
        double y0;

        pdX=new double[len][2];
        pdY=new double[len];
        int x,y;
        for(i=0;i<len;i++){
            pt=points.get(i);
            x=pt.x;
            y=pt.y;
            pdX[i][0]=x;
            pdX[i][1]=y;
            pdY[i]=pixels0[y][x];
            if(IPO.converged){
                y0=func.fun(IPO.pdPars, pdX[i]);
                pdY[i]+=y0;
            }
        }
        FittingModelNode aModel=new FittingModelNode(pdX,pdY);
        aModel.setFunctionTypes(svT);
        aModel.pdPars=IPO.pdPars;
        aModel.m_cIS=cIS;
        return aModel;
    }
    
    FittingModelNode buildFittingModel(IPOGaussianNodeCluster cluster, int[][] pixels0){
        //this method so far is specifically for "gaussian2D_GaussianPars"
        double[] pdY, pdX[],X=new double[2];

        ArrayList<IPOGaussianNode> IPOs=cluster.m_cvIPONodes;
        ImageShape cIS, cISt;
        ArrayList<ImageShape> cvShapes=new ArrayList();
//        ArrayList<ComposedFittingFunction> funcs=new ArrayList();
        int i,j,len,nNumIPOs=IPOs.size();
        IPOGaussianNode IPO=IPOs.get(0);
        cIS=cIS=IPOGaussianNodeHandler.buildIPOShape(IPO, 0.011*IPO.Amp);
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction func=new ComposedFittingFunction(svT);
        double cnst=IPO.cnst;
        for(i=1;i<nNumIPOs;i++){
            IPO=IPOs.get(i);
            if(IPO.IPOIndex==8){
                i=i;
            }
            cnst+=IPO.cnst;
            cISt=IPOGaussianNodeHandler.buildIPOShape(IPO, 0.011*IPO.Amp);
            cIS.mergeShape(cISt);
            svT.add("gaussian2D_GaussianPars");
        }
        cnst/=nNumIPOs;

        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

        ArrayList<Point> points=new ArrayList();
        cIS.getInnerPoints(points);
        len=points.size();
        Point pt;
        double y0;

        pdX=new double[len][2];
        pdY=new double[len];
        int x,y;
        for(i=0;i<len;i++){
            pt=points.get(i);
            x=pt.x;
            y=pt.y;
            pdX[i][0]=x;
            pdX[i][1]=y;
            pdY[i]=pixels0[y][x];
            for(j=0;j<nNumIPOs;j++){
                IPO=IPOs.get(j);
                if(IPO.converged){
                    y0=func.fun(IPO.pdPars, pdX[i]);
                    pdY[i]+=y0;
                }
            }
        }
        FittingModelNode aModel=new FittingModelNode(pdX,pdY);
        aModel.rIndexes=new ArrayList();
        aModel.setFunctionTypes(svT);
        func=new ComposedFittingFunction(svT);

        double[] pdPars=new double[nNumIPOs*6+1];
        int nPars=1,num=6;
        for(i=0;i<nNumIPOs;i++){
            for(j=0;j<num;j++){
                pdPars[nPars+j]=IPO.pdPars[j+1];
            }
            nPars+=num;
            aModel.rIndexes.add(IPOs.get(i).IPOIndex);
        }
        aModel.m_cIS=cIS;
        aModel.pdPars=pdPars;
        return aModel;
    }
    void replacePixels(FittingModelNode aModel, int[][]pixels, int[][] pixelsp){
        int h=pixels.length,w=pixels[0].length;
        double pdX[][]=aModel.m_pdX, pdY[]=aModel.m_pdY;
        int i,len=pdX.length,x,y;
        double dn=Double.POSITIVE_INFINITY,pixelp;
        for(i=0;i<len;i++){
            x=(int)(pdX[i][0]+0.5);
            y=(int)(pdX[i][1]+0.5);
            pdY[i]=pixels[y][x];

            pixelp=pixelsp[y][x];
            if(pixelp<dn) dn=pixelp;
        }
        double pdPars[]=aModel.pdPars;
        ArrayList<String> svParNames=aModel.svParNames;
        len=pdPars.length;
        pdPars[0]=dn;
        ArrayList<Integer> nvNumPars=aModel.nvNumParameters;
        int terms=nvNumPars.size();
        int num=1,index;
        for(i=0;i<terms;i++){
            num+=nvNumPars.get(i);
            x=(int)(pdPars[num-2]+0.5);
            if(x<0||x>=w) continue;
            y=(int)(pdPars[num-1]+0.5);
            if(y<0||y>=h) continue;
            pdPars[num-nvNumPars.get(i)]=pixelsp[y][x]-dn;
        }
    }
    void removeRefittedResults(){
        int i,len=m_cvRefinedFittingResultNodes.size(),len1,j;
        ImageShape cIS;
        ArrayList<FittingResultsNode> cvRefittedResults=new ArrayList();
        FittingResultsNode aResult;
        for(i=0;i<len;i++){
            cIS=m_cvRefinedFittingResultNodes.get(i).m_cvModels.get(0).m_cIS;
            len1=m_cvFittingResultNodes.size();
            for(j=0;j<len1;j++){
                aResult=m_cvFittingResultNodes.get(j);
                if(cIS.equals(aResult.m_cvModels.get(0).m_cIS)){
                    cvRefittedResults.add(aResult);
                    m_cvFittingResultNodes.remove(j);
                    break;
                }
            }
        }
        undoImageCompensation(cvRefittedResults);
    }
    void markRefittingRegionAndComplexes(){//not using in this version
        int len=m_cvRegionNodescp.size(),i;
        RegionNode rNode,rNodeo;
        Point pt;
        int rIndex,cIndex;
        for(i=0;i<len;i++){
            rNode=m_cvRegionNodescp.get(i);
            if(!rNode.significant) continue;
            if(!rNode.fitted) continue;
            pt=rNode.peakLocation;
            rIndex=stampProcessed[pt.y][pt.x];
            rIndex=LandscapeAnalyzerPixelSorting.getRegionIndex(rIndex)-1;//region index starts from 1
            rNodeo=m_cvRegionNodes.get(rIndex);

            cIndex=rNodeo.complexIndex-1;
            if(cIndex<0)
                m_cvComplexNodes.get(cIndex).fitted=false;
            else
                rNodeo.fitted=false;
        }
    }
    public void addFitterListener(ActionListener al){
        m_cvListeners.add(al);
    }
    void fireActionEvent(ActionEvent ae0){
        ActionEvent ae=ae0;
        if(ThreadName!=null){
            String newCommand=ThreadName+": "+ae0.getActionCommand();
            ae=new ActionEvent(this,0,newCommand);
        }
        int len=m_cvListeners.size(),i;
        for(i=0;i<len;i++){
            m_cvListeners.get(i).actionPerformed(ae);
        }
    }
    public void actionPerformed(ActionEvent ae){
        String st=ae.getActionCommand();
        if(st.startsWith("Iterations")){
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    updateImages(m_cCurrentModel);
                }
            });
        }
    }
    void calCompensatedImage_IPOGaussian(){
        int len=m_cvFittingResultNodes.size(),i,j,len1;
        if(!silent)implCompen.show();
        if(!silent)implInt.show();
        FittingResultsNode fNode;
        FittingModelNode fmNode;
        int[][] pixels=CommonMethods.getPixelValues(implCompen);
        int[][] pixelsInt=new int[h][w];
        double[][] pdV=new double[h][w];
        CommonStatisticsMethods.setElements(pdV, 0);
        for(i=0;i<len;i++){
            fNode=m_cvFittingResultNodes.get(i);
            len1=fNode.nModels;
            for(j=0;j<len1;j++){
                fmNode=fNode.m_cvModels.get(j);
                FittingModelHandler.addFittedValues(fmNode, pdV);
            }
        }
        int pixel;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pixel=(int)pdV[i][j];
                pixels[i][j]-=pixel;
                pixelsInt[i][j]=pixel;
            }
        }
        pixelsCompen=pixels;
        CommonMethods.setPixels(implCompen, pixels);
        CommonMethods.setPixels(implInt, pixelsInt);
        if(!silent)implCompen.show();
    }
    void refineCompensatedImage_IPOGaussian(){
        int len=m_cvRefinedFittingResultNodes.size(),i,j,len1;
        FittingResultsNode fNode;
        FittingModelNode fmNode;
        int[][] pixels=CommonMethods.getPixelValues(implCompen);
        int[][] pixelsInt=new int[h][w];
        implComposed=CommonMethods.cloneImage(impl);
        int[][] pixelsComposed=CommonMethods.getPixelValues(implComposed);
        double[][] pdV=new double[h][w];
        CommonStatisticsMethods.setElements(pdV, 0);
        for(i=0;i<len;i++){
            fNode=m_cvRefinedFittingResultNodes.get(i);
            len1=fNode.nModels;
            for(j=0;j<len1;j++){
                fmNode=fNode.m_cvModels.get(j);
                if(fmNode.m_cIS.contains(3,143)){
                    i=i;
                }
                FittingModelHandler.addFittedValues(fmNode, pdV);
            }
        }
        int pixel;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pixel=(int)pdV[i][j];
                pixels[i][j]-=pixel;
                pixelsInt[i][j]+=pixel;
            }
        }

        FittingModelNode aModel;
        double cnst,pdX[][];
        int x,y;
        for(i=0;i<len;i++){
            fNode=m_cvRefinedFittingResultNodes.get(i);
            aModel=fNode.m_cvModels.get(0);
            cnst=aModel.pdPars[0];
            pdX=aModel.m_pdX;
            len1=pdX.length;
            for(j=0;j<len1;j++){
                x=(int)(pdX[j][0]+0.5);
                y=(int)(pdX[j][1]+0.5);
                pixelsComposed[y][x]=(int)(cnst+pdV[y][x]);
            }
        }

        CommonMethods.setPixels(implCompen, pixels);
        CommonMethods.setPixels(implInt, pixelsInt);
        CommonMethods.setPixels(implComposed, pixelsComposed);
        if(!silent)implCompen.show();
        if(!silent)implComposed.show();
    }
    void undoInvalidImageCompensation(){
         undoImageCompensation(m_cvInvalidFittingResultNodes);
    }
    void undoImageCompensation(ArrayList<FittingResultsNode> cvFittingResults){
        int len=cvFittingResults.size(),i,j,len1;
        FittingResultsNode fNode;
        FittingModelNode fmNode;
        int[][] pixels=CommonMethods.getPixelValues(implCompen);
        int[][] pixelsInt=CommonMethods.getPixelValues(implInt);;
        double[][] pdV=new double[h][w];
        CommonStatisticsMethods.setElements(pdV, 0);

        for(i=0;i<len;i++){
            fNode=cvFittingResults.get(i);
            len1=fNode.nModels;
            for(j=0;j<len1;j++){
                fmNode=fNode.m_cvModels.get(j);
                FittingModelHandler.addFittedValues(fmNode, pdV);
            }
        }

        int pixel;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pixel=(int)pdV[i][j];
                pixels[i][j]+=pixel;
                pixelsInt[i][j]-=pixel;
            }
        }
        CommonMethods.setPixels(implCompen, pixels);
        CommonMethods.setPixels(implInt, pixelsInt);
        if(!silent)CommonMethods.refreshImage(implCompen);
        if(!silent)CommonMethods.refreshImage(implInt);
    }
    void saveResultImage(){
//        CommonMethods.closeAllImages();
        implResults=CommonMethods.getBlankImageStack(ImagePlus.GRAY32, w, h, 13);
        implResults.setTitle("Results Image");
        implResults.getStack().setSliceLabel("Original Image", 1);
        implResults.getStack().setSliceLabel("Precessed Image", 2);
        implResults.getStack().setSliceLabel("Fitted Image", 3);
        implResults.getStack().setSliceLabel("Composed Image", 4);
        implResults.getStack().setSliceLabel("Residual Image", 5);
        implResults.getStack().setSliceLabel("Compensated Image",6);
        implResults.getStack().setSliceLabel("Fitted Image0", 7);
        implResults.getStack().setSliceLabel("Residual Image0", 8);
        implResults.getStack().setSliceLabel("Compensated0 Image0", 9);
        implResults.getStack().setSliceLabel("Itegrated Image", 10);
        implResults.getStack().setSliceLabel("Itegrated Image0", 11);
        implResults.getStack().setSliceLabel("Region Boundary Image", 12);
        implResults.getStack().setSliceLabel("Complex Boundary Image", 13);

        implResults.setSlice(1);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(2);
        CommonMethods.getPixelValue(implp, implp.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(3);
        CommonMethods.getPixelValue(implF, implF.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(4);
        CommonMethods.getPixelValue(implComposed, implComposed.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(5);
        CommonMethods.getPixelValue(implD, implD.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(6);
        CommonMethods.getPixelValue(implCompen, implCompen.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);
        if(!silent)implResults.show();

        implResults.setSlice(7);
        CommonMethods.getPixelValue(implF0, implF0.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(8);
        CommonMethods.getPixelValue(implD0, implD0.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(9);
        CommonMethods.getPixelValue(implCompen0, implCompen0.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);
        if(!silent)implResults.show();

        implResults.setSlice(10);
        CommonMethods.getPixelValue(implInt, implInt.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(11);
        CommonMethods.getPixelValue(implInt0, implInt0.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(12);
        CommonMethods.getPixelValue(implRegion, implRegion.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);

        implResults.setSlice(13);
        CommonMethods.getPixelValue(implComplex, implComplex.getCurrentSlice(), pixels);
        CommonMethods.setPixels(implResults, pixels);
        if(!silent)implResults.show();
        FileAssist.changeExt(fileName, "tif");
        CommonMethods.saveImage(implResults, dir+fileName);
    }
    boolean invalidFitting(FittingResultsNode aNode, boolean IncludingShape){
        int len=aNode.nModels,i;
        boolean invalid=false;
        for(i=0;i<len;i++){
            if(invalidFittingModel(aNode.m_cvModels.get(i),IncludingShape)) invalid=true;
        }
        return invalid;
    }
    boolean invalidConstant(FittingModelNode aModel){
        double dconst=aModel.pdPars[0];
        return (dconst<pnPixelRange[0]||dconst>pnPixelRange[1]);
    }
    boolean invalidFittingModel(FittingModelNode aModel,boolean includingShape){
        if(!CommonStatisticsMethods.regularDoubleArray(aModel.pdPars)) return false;
        
        double dconst=aModel.pdPars[0];
        boolean invalidComponent=false;
        aModel.nvInvalidComponents.clear();
        boolean invalid=false;
        if(dconst<pnPixelRange[0]||dconst>pnPixelRange[1]) invalid=true;
        
        if(aModel.nComponents<=0) return true;
        int numComponents=aModel.nComponents;
        int nPars=1,j,num,x,y;
        int transform=aModel.toGaussian2D_GaussianPars();

        double dPar;
        for(int i=0;i<numComponents;i++){
            invalidComponent=false;
            num=aModel.nvNumParameters.get(i);
            for(j=0;j<num;j++){
                dPar=aModel.pdPars[nPars];
                if(aModel.svParNames.get(nPars).contains("sigma")&&includingShape){
                    if(Math.abs(dPar)>5||Math.abs(dPar)<0.5) invalidComponent=true;
                }
                if(aModel.svParNames.get(nPars).startsWith("A")){
                    if(dPar<0) invalidComponent=true;;
                    if(dPar>1.5*(pnPixelRange[1]-pnPixelRange[0])) invalidComponent=true;;
                }
                if(Double.isNaN(dPar)||Double.isInfinite(dPar)) invalid=true;
                nPars++;
            }

            if(!aModel.cvVarRanges.get(0).contains(aModel.pdPars[nPars-2])||!aModel.cvVarRanges.get(1).contains(aModel.pdPars[nPars-1])){
                invalidComponent=true;
            }
            if(invalidComponent) {
                aModel.nvInvalidComponents.add(i);
                invalid=true;
            }
        }
        if(transform==1) aModel.toGaussian2D();
        return invalid;
    }
    void assignRegionIndexes(FittingModelNode aModel){
        ArrayList<Point> peaks=aModel.getPeakPositions();
        ArrayList<Integer> rids=new ArrayList();
        int len=peaks.size(),i,j;
        Point p;
        for(i=0;i<len;i++){
            p=peaks.get(i);
            rids.add(LandscapeAnalyzerPixelSorting.getRegionIndex(stampProcessed[p.y][p.x]));
        }
        aModel.setRegionIndexes(rids);
    }
    public double getTogalSignalCutoff(){
        return m_dRegionSCutoff;
    }
    public double getPeakHeightCutoff(){
        return m_dRegionHCutoff;
    }
    public double getRegionAreaCutoff(){
        return m_dRegionACutoff;
    }
    public Point getRegionPeak(int x, int y){
        int rId=LandscapeAnalyzerPixelSorting.getRegionIndex(stampProcessed[y][x]);
        return m_cvRegionNodes.get(rId-1).peakLocation;
    }
    public void loadCompemsatedImage(ImagePlus impl){
        CommonMethods.setPixels(impl, pixelsCompen);
    }
    public void loadProcessedImage(ImagePlus impl){
        CommonMethods.setPixels(impl, pixelsp);
    }
    static public int getFullIterations(FittingModelNode aModel,double accuracy){
        double par0=aModel.pdPars[1],par;
        Non_Linear_Fitter.getFittedModel_Simplex(aModel, aModel, 0.1, null);
        par=aModel.pdPars[1];
        double acc=Math.abs(par-par0)/Math.abs(par+par0);
        int iter=0,numIter=aModel.nIterations;
        par0=par;
        while(acc>accuracy&&iter<5){
            if(!CommonStatisticsMethods.regularDoubleArray(aModel.pdPars)){
                return -1;
            }        
            iter++;
            Non_Linear_Fitter.getFittedModel_Simplex(aModel, aModel, 0.1, null);
            numIter=aModel.nIterations;
            par=aModel.pdPars[1];
            acc=Math.abs(par-par0)/Math.abs(par+par0);
            par0=par;
        }
        return 1;
    }
    public static boolean invalidIPOG(IPOGaussianNodeComplex IPOG, intRange xRange, intRange yRange){
        if(IPOG==null) return true;
        return invalidIPOGs(IPOG.IPOGs,null,xRange,yRange);
    }
    public static boolean invalidIPOGs(ArrayList<IPOGaussianNode> IPOGs0, ArrayList<Integer> nvIndexes,intRange xRange, intRange yRange){
        ArrayList<IPOGaussianNode> IPOGs=IPOGaussianNodeHandler.getSimpleIPOGs(IPOGs0);
        int i,len=IPOGs.size();
        if(nvIndexes!=null) nvIndexes.clear();
        IPOGaussianNode IPOG;
        DoubleRange sigmaRange=new DoubleRange(0.2,10);
        Point center;
        boolean invalid=false;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            if(!CommonStatisticsMethods.regularDoubleArray(IPOG.pdPars)){
                if(nvIndexes!=null) nvIndexes.add(i);
                invalid=true;
                continue;
            }
            if(IPOG.Amp<0){
                if(nvIndexes!=null) nvIndexes.add(i);
                invalid=true;
                continue;
            }
            if(!sigmaRange.contains(IPOG.sigmax)||!sigmaRange.contains(IPOG.sigmay)){
                if(nvIndexes!=null) nvIndexes.add(i);
                invalid=true;
                continue;
            }
            if(xRange==null||yRange==null) continue;
            if(xRange.emptyRange()||yRange.emptyRange()) continue;
            center=IPOG.getCenter();
            if(!xRange.contains(center.x)||!yRange.contains(center.y)){
                if(nvIndexes!=null) nvIndexes.add(i);
                invalid=true;
                continue;
            }
        }
        return invalid;
    }
    
    public static boolean invalidIPOGModel(FittingModelNode aModel, intRange xRange, intRange yRange){
        if(aModel==null) return true;
        if(!CommonStatisticsMethods.regularDoubleArray(aModel.pdPars)) return true;
        
        ArrayList<IPOGaussianNode> cvIPOGs=new ArrayList();
        ArrayList<Integer> nvInvalidComponents=new ArrayList();
        IPOGaussianNodeHandler.buildIPOGaussianNode(cvIPOGs, aModel, -1, -1);
        return invalidIPOGs(cvIPOGs,nvInvalidComponents,xRange,yRange);
    }
    
    public static boolean invalidIPOGModel(IPOGaussianNodeComplex IPOG, intRange xRange, intRange yRange){
        if(IPOG==null) return true;
        
        ArrayList<IPOGaussianNode> cvIPOGs=IPOG.IPOGs;
        ArrayList<Integer> nvInvalidComponents=new ArrayList();
        return invalidIPOGs(cvIPOGs,nvInvalidComponents,xRange,yRange);
    }
    
    static public FittingResultsNode getFullModelFitting(FittingModelNode aModel,double pThreshold){
        int nMaxIterations=6;
        return getFullModelFitting(aModel,pThreshold,nMaxIterations);
    }
    
    static public FittingResultsNode getFullModelFitting(FittingModelNode aModel,double pThreshold, int nMaxIterations){
        IPOGaussianExpander expander=new IPOGaussianExpander();
        double accuracy=0.00001;
        
        if(aModel==null) return null;
        calIPOGMeritPValue(aModel); 
        FittingResultsNode aResultsNode=Non_Linear_Fitter.getFittedModel_Simplex(aModel, aModel, 0.1, null);
        if(invalidIPOGModel(aModel,null,null)) {
            if(aModel.nComponents==1)
                return null;
            else {
                aModel=IPOGaussianExpander.getDummyIPOGModel();
                aResultsNode=Non_Linear_Fitter.getFittedModel_Simplex(aModel, aModel, 0.1, null);
                if(invalidIPOGModel(aModel,null,null)) return null;
            }
        }
        
        if(getFullIterations(aModel,accuracy)<0) return null;        
        calIPOGMeritPValue(aModel); 
        FittingModelNode bModel=expander.buildExpandedModel(aModel, "gaussian2D_GaussianPars", 1, false, false,1);
        calIPOGMeritPValue(bModel); 
        if(!CommonStatisticsMethods.regularDoubleArray(bModel.pdPars)){
            return null;
        }        
        
        Non_Linear_Fitter.getFittedModel(bModel, bModel, 0.1, null);
        if(getFullIterations(bModel,accuracy)<0) return aResultsNode;
        
        double pValue=FittingComparison.getFittingComparison(aModel, bModel);
        int nIter=0;
        while(pValue<pThreshold&&!invalidIPOGModel(bModel,null,null)){
            nIter++;
            if(nIter>nMaxIterations) break;
            calIPOGMeritPValue(bModel); 
            aResultsNode.m_cvModels.add(bModel);
            aResultsNode.m_dvPValues.add(pValue);
            aModel=bModel;
            bModel=expander.buildExpandedModel(aModel, "gaussian2D_GaussianPars", 1, false, false,1);
            Non_Linear_Fitter.getFittedModel_Simplex(bModel, bModel, 0.1, null);
            if(getFullIterations(bModel,accuracy)<0) break;
            pValue=FittingComparison.getFittingComparison(aModel, bModel);
        }
        if(invalidIPOGModel(bModel,null,null)){
            bModel=aModel;
            pValue=1;
        }
        calIPOGMeritPValue(bModel); 
        aResultsNode.m_cvModels.add(bModel);
        aResultsNode.m_dvPValues.add(pValue);
        return aResultsNode;
    }
    static int calIPOGMeritPValue(FittingModelNode aModel){
        //assumtion: this a IPOG model
        if(aModel==null) return -1;
        FittingModelNode bModel=new FittingModelNode(aModel);
        bModel.fixAllPars();
        ArrayList<Integer> nvFreePars=new ArrayList();
        nvFreePars.add(0);
        bModel.unfixParameters(nvFreePars);
        double[] pdPars=bModel.pdPars;
        int i,len=pdPars.length,terms=(len-1)/6;
        for(i=0;i<terms;i++){
            pdPars[1+6*i]=0;
        }
        Non_Linear_Fitter.getFittedModel_Simplex(bModel, bModel, 0.1, null);
        aModel.dMeritPV=FittingComparison.getFittingComparison(bModel, aModel);
        return 1;
    }
}
