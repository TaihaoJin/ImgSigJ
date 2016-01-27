/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import utilities.CommonGuiMethods;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.QuickSort;
import utilities.statistics.Histogram;

/**
 *
 * @author Taihao
 */
public class ImageSegmentor {
    RegionBoundaryAnalyzer cRBA,cRBAr;
    ImagePlus impl,implp,implGD,implGDI,implpr,implGDr,implGDIr;
    RegionBoundaryAnalyzer m_cRBA;
    Histogram m_cRefSizeHist,m_cRefHeightHist,m_cRefSumHist,m_cRefBorderSegmentHRatioHist,m_cRefBorderSegmentHeightHist;
    int[][] pixels,pixelsp,pixelsr,pixelspr,stampProcessed,stampProcessedcp,pixelsgd2,stampgd2,pixelsF,pixelsD,exclusionStamp,comboStamp,comboStampcp,pnScratch,pnScratchp,pixelsCompen,pixelsCompenp,stampScratch,pnScratch1,pnScratch2,pnScratchr;
    int[] pnPixelRange;
    int w,h,wr,hr;
    double m_dRegionHCutoff,m_dRegionACutoff, m_dRegionSCutoff,m_dRegionHICutoff,m_dRegionMeanCutoff;//region height, area and regionSum cutoff, m_dRegionHICutoff is used for
    ArrayList<RegionNode> m_cvRegionNodes;
    ArrayList<RegionComplexNode> m_cvComplexNodes;
    ArrayList<RegionNode> m_cvRegionNodescp;
    ArrayList<RegionComplexNode> m_cvComplexNodescp;
    boolean m_pbBackgrounds[];
    public ImageSegmentor(ImagePlus impl){
        w=impl.getWidth();
        h=impl.getHeight();
        this.impl=impl;
        processImage();
        calGradientImages();
        calInvertedGradientImges();
        segmentImage();
    }
    void processImage(){
        float xSigma=1f,ySigma=1f,fAccuracy=0.001f;
        implp=CommonMethods.cloneImage(impl);   
        implp.setTitle("Processed Image");
        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);
        
        implpr=CommonMethods.cloneImage(impl);
        implpr.setTitle("Processed Randomized Image");
        CommonMethods.randomizeImage(implpr);
        CommonMethods.GaussianBlur(implpr, xSigma, ySigma, fAccuracy);
    }
    void calGradientImages(){
        int[][] pixels=new int[h][w],pixelsgd=new int[h][w];
        CommonMethods.getPixelValue(implp, implp.getCurrentSlice(), pixels);
        CommonMethods.getGradientMap(pixels, pixelsgd);
        implGD=CommonMethods.cloneImage(implp);
        CommonMethods.setPixels(implGD, pixelsgd);
        implGD.setTitle("Gradient Map of the Processed Image");
        
        pixels=new int[h][w];
        pixelsgd=new int[h][w];
        CommonMethods.getPixelValue(implpr, implpr.getCurrentSlice(), pixels);
        CommonMethods.getGradientMap(pixels, pixelsgd);
        implGDr=CommonMethods.cloneImage(impl);
        CommonMethods.setPixels(implGDr, pixelsgd);
        implGDr.setTitle("Gradient Map of the processed randomized image");
    }    
    void calInvertedGradientImges(){
        implGDI=CommonMethods.cloneImage(implGD);
        CommonMethods.invertImage(implGDI);
        implGDI.setTitle("Interted Gradient Map of the processed image");
        
        implGDIr=CommonMethods.cloneImage(implGDr);
        CommonMethods.invertImage(implGDIr);
        implGDIr.setTitle("Interted Gradient Map of the processed randomized image");
    }
    void segmentImage(){
        buildRegionComplexes();
        buildBackground();
        showSegmentation();
    }
    void buildRegionComplexes(){
        double pValue=0.2;

        pixelspr=CommonMethods.getPixelValues(implpr);
        cRBAr=CommonMethods.buildRegionComplex(implGDIr);
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

        m_cRBA=CommonMethods.buildRegionComplex(implGDI);
//        m_cRBA.removeOverheightBorderSegments(pixelsp, m_cRefBorderSegmentHeightHist.getPercentileValue(), false);
        m_cRBA.buildRegionPixelStatistics(pixelsp);
        m_cRBA.markSignificantRegions_Sum(m_dRegionSCutoff);
        m_cRBA.calBorderSegmentHeightsAndRatios(pixelsp);
//        m_cRBA.removeCloseToPeakBorderSegments(6.1*6.1);
        m_cvRegionNodes=m_cRBA.getRegionNodes();
        m_cvComplexNodes=m_cRBA.getComplexNodes();
        stampProcessed=m_cRBA.getStamp();
//        m_cRBA.calComplexBases(pixelsp);
        m_cRBA.assignRegionAndComplexBoundaryPoints();
    }
    void buildBackground(){
        ArrayList<RegionNode> cvRNodes=cRBAr.m_cvRegionNodes;
        int i,lenR=cvRNodes.size(),r1,r2;
        double[] pdMeans=new double[lenR];
        ArrayList<Double> dvMeans=new ArrayList();
        for(i=0;i<lenR;i++){
            pdMeans[i]=cvRNodes.get(i).meanSem.mean;
        }
        
        QuickSort.quicksort(pdMeans);
        double p=0.5;
        int index=(int)(lenR*(1-p)+0.5);
        double cutoff=pdMeans[index];
        
        lenR=m_cvRegionNodes.size();
        m_pbBackgrounds=new boolean[lenR];
        CommonStatisticsMethods.setElements(m_pbBackgrounds, false);
        
        for(i=0;i<lenR;i++){
            if(m_cvRegionNodes.get(i).meanSem.mean<cutoff) m_pbBackgrounds[i]=true;
        }
        
        int lenB=m_cRBA.m_nNumBorderSegments;
        ArrayList<RegionBorderSegmentNode> cvRBNodes=m_cRBA.m_cvBorderSegments;
        RegionBorderSegmentNode cRBSeg;
        for(i=0;i<lenB;i++){
            cRBSeg=cvRBNodes.get(i);
            r1=cRBSeg.region1;
            r2=cRBSeg.region2;
            if(m_pbBackgrounds[r1-1]&&m_pbBackgrounds[r2-1]) m_cRBA.removeBorderSegment(cRBSeg);
            if(!m_pbBackgrounds[r1-1]&&!m_pbBackgrounds[r2-1]) m_cRBA.removeBorderSegment(cRBSeg);
        }
        m_cRBA.completeComplexes();
    }
    void showSegmentation(){
        implp.show();
        implGD.show();
        implGDI.show();
        ImagePlus implt=CommonMethods.getBlankImage(ImagePlus.GRAY8,w, h);
        implt.setTitle("Complexes");
        ImagePlus implt1=CommonMethods.getBlankImage(ImagePlus.GRAY8,w, h);
        implt1.setTitle("Regions");
//        int i,len=m_cvComplexNodes.size();
        int i,len=m_cvRegionNodes.size();
        ArrayList<Point> contour;
        ArrayList<Point> boundary;
        RegionNode rNode;
        RegionComplexNode cNode;
        for(i=0;i<len;i++){
            rNode=m_cvRegionNodes.get(i);
            contour=rNode.m_cvBoundaryPoints;
            CommonMethods.drawTrail(implt1, contour, 255);
            if(rNode.complexIndex>=0) continue;
            CommonMethods.drawTrail(implt, contour, 255);
        }
        m_cvComplexNodes=m_cRBA.m_cvComplexNodes;
        len=m_cvComplexNodes.size();
        for(i=0;i<len;i++){
            cNode=m_cvComplexNodes.get(i);
            
            contour=m_cRBA.getRegionComplexOuterContour(cNode);
            boundary=cNode.m_cvBoundaryPoints;
            CommonMethods.drawTrail(implt, contour, 255);
//            CommonMethods.drawTrail(implt, boundary, 255);
        }
        implt.show();
        implt1.show();
    }
    void buildRegionComplexes0(){
        double pValue=0.2;

        pixelspr=CommonMethods.getPixelValues(implpr);
        cRBAr=CommonMethods.buildRegionComplex(implpr);
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
}
