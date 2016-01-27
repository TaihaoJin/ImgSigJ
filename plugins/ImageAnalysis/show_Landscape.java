/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
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
/**
 *
 * @author Taihao
 */
public class show_Landscape implements ij.plugin.PlugIn, MouseListener{
    ImagePlus m_cImpl;
    String statusString;
    public void run(String arg){
        demo_stampPixels();
    }
    void demo_stampPixels(){
        demo_stampPixels(WindowManager.getCurrentImage(),true,true);
    }
    public ImagePlus demo_stampPixels(ImagePlus impl){
        return demo_stampPixels(impl,false,true);
    }
    public ImagePlus demo_stampPixels(ImagePlus impl,boolean bHightLightGrooves,boolean bShowRegions){
        m_cImpl=impl;
        impl.getCanvas().addMouseListener(this);
        ImageShape is=new RectangleImage(2,3);
        is.setLocation(new Point(2,2));
        ArrayList<Point> contour=is.getOuterContour();

        ArrayList<Point> lcontour=ContourFollower.getEnlargedContour(contour, 0);
        ImagePlus implc=CommonMethods.copyToRGBImage(impl);
        int h=impl.getHeight(),w=impl.getWidth();
        int pixel=0;
        ArrayList <Integer> types=new ArrayList();
        IntArray indexes=new IntArray();

        int type=LandscapeAnalyzerPixelSorting.localMaximum;

        ArrayList <IntArray> rgbIndexes=new ArrayList();
        indexes.m_intArray.add(0);
        indexes.m_intArray.add(1);
        types.add(LandscapeAnalyzerPixelSorting.localMaximum);
        rgbIndexes.add(indexes);

        indexes=new IntArray();
        types.add(LandscapeAnalyzerPixelSorting.localMinimum);
        indexes.m_intArray.add(1);
        indexes.m_intArray.add(2);
        rgbIndexes.add(indexes);

        indexes=new IntArray();
        types.add(LandscapeAnalyzerPixelSorting.watershed);
        indexes.m_intArray.add(2);
        rgbIndexes.add(indexes);
        implc.show();

        int pixels[][]=new int[h][w],stamp[][],pixels0[][];


        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(impl);

        int i,j,slice,num=impl.getStackSize(),rgb[]=new int[3],region;

        ImagePlus implFuc=CommonMethods.cloneImage(impl);
        ImagePlus implFucr=CommonMethods.cloneImage(impl);
        implFucr=CommonMethods.convertImage(implFucr, ImagePlus.GRAY32, false);
        implFucr.setTitle("fitted regions");
        implFuc=CommonMethods.convertImage(implFuc, ImagePlus.GRAY32, false);
        implFuc.setTitle("fitted complexes");
        implFuc.show();
        implFucr.show();
        RegionNode rNode;
        String dir=FileAssist.defaultDirectory;

        num=1;
        for(slice=0;slice<num;slice++){
            pixel=0;
            impl.setSlice(slice+1);
            pixels0=CommonMethods.getPixelValues(impl);
            implc.setSlice(slice+1);
            implFuc.setSlice(slice+1);
            CommonMethods.markSpecialLandscapePoints(impl, implc, stampper, types, pixel, rgbIndexes,0,impl.getHeight()-1,0,impl.getWidth()-1);
            pixels=CommonMethods.getPixelValues(implc);
            stamp=stampper.getStamp();

            for(i=0;i<h;i++){
                for(j=0;j<w;j++){
                    pixel=pixels[i][j];
                    type=LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[i][j]);
                    if(type==LandscapeAnalyzerPixelSorting.localMinimum){
                        j=j;
                    }
                    region=LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[i][j]);
//                    if(type==LandscapeAnalyzerPixelSorting.regular&&bShowRegions){
                    if(bShowRegions){
                        CommonMethods.intTOrgb(pixel, rgb);
                        rgb[2]=(region)%255;
                        pixel=(rgb[0]<<16)|(rgb[1]<<8)|(rgb[2]);
                        pixels[i][j]=pixel;
                    }
                    if(bHightLightGrooves&&LandscapeAnalyzerPixelSorting.isWatershed(stamp, j, i)){
                        CommonMethods.intTOrgb(pixel, rgb);
                        rgb[0]=255;
                        rgb[1]=255;
                        pixel=(rgb[0]<<16)|(rgb[1]<<8)|(rgb[2]);
                        pixels[i][j]=pixel;
                    }
                }
            }
            CommonMethods.setPixels(implc, pixels,true);
            implc.show();
            RegionBoundaryAnalyzer ba=new RegionBoundaryAnalyzer(stamp);
//            ba.drawRegionBoundaries_Random(implc, 30);
//            ba.drawRegionBoundaries(implc, points);
            ba.removeOverheightBorderSegments(pixels0, 66);

            ArrayList<RegionComplexNode> cNodes=ba.getRegionComplexNodes();
            int len=cNodes.size();
            RegionComplexNode cNode;
            for(i=0;i<len;i++){
                cNode=cNodes.get(i);
                ba.drawRegionComplex(implc, cNode, pixels0);
            }
            double pdPars[],pdParst[];
            len=ba.m_cvRegionNodes.size();
            Point p;
            FittingResultsNode resultsNode;

            String fittingFileNamer=dir+impl.getTitle()+" slice"+slice+" region fitting test.txt";
            Formatter fmr=PrintAssist.getQuickFormatter(fittingFileNamer);
            
//            for(i=0;i<0;i++){
            for(i=0;i<ba.m_cvRegionNodes.size();i++){
                statusString="Fitting the "+PrintAssist.ToString_Order(i+1)+" region of "+len+".";
                IJ.showStatus(statusString);
                rNode=ba.m_cvRegionNodes.get(i);
                p=rNode.peakLocation;
                if(p.x==122&&p.y==39){
                    p=p;
                }
                if(rNode.area<25) continue;
                resultsNode=ba.fitRegion_Gaussian2D(implFucr,pixels0, rNode);
                PrintAssist.printString(fmr, "region"+i+" peak ("+p.x+"," +p.y+"): "+resultsNode.nModels+" components  ");
//                resultsNode.ExportFittingResults_SingleLine(fmr);
                PrintAssist.endLine(fmr);
                resultsNode.ExportFittingResults_MultipleLines(fmr);
                PrintAssist.endLine(fmr);
                fmr.flush();
            }
            fmr.close();
            boolean fittingComplexes=true;
            if(!fittingComplexes) continue;
            String fittingFileNamec=dir+impl.getTitle()+" slice"+slice+" complex fitting test.txt";
            Formatter fmc=PrintAssist.getQuickFormatter(fittingFileNamec);
            len=cNodes.size();
//            for(i=0;i<len;i++){
            for(i=0;i<len;i++){
                statusString="Fitting the "+PrintAssist.ToString_Order(i+1)+" complex of "+len+".";
                IJ.showStatus(statusString);
                cNode=cNodes.get(i);
                resultsNode=ba.fitComplex_Gaussian2D(implFuc,pixels0, cNode);
                PrintAssist.printString(fmc, "complex"+i+"  "+resultsNode.nModels+" components  ");
//                resultsNode.ExportFittingResults_SingleLine(fmc);
                PrintAssist.endLine(fmc);
                resultsNode.ExportFittingResults_MultipleLines(fmc);
                PrintAssist.endLine(fmc);
                fmc.flush();
            }
            fmc.close();
            statusString="Finished fitting the "+PrintAssist.ToString_Order(slice+1)+" slice of "+num+".";
        }
        return implc;
    }
    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent e){}

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e){}

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e){}

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e){}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){
        IJ.showStatus(statusString);
    }
}
