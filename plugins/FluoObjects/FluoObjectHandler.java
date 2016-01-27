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

/**
 *
 * @author Taihao
 */
public class FluoObjectHandler {
    ImagePlus impl;
    int[][] pixels;
    ArrayList<Roi>ROIs;
    ArrayList <GraphicalObject> GOList;
    ArrayList <GraphicalObjectProperty> GOPs;
    GraphicalObjectsHandler GOHandler;
    IntArray2 backgroundPixels;
    ArrayList <IntensityPeakObjectHandler> IPOHs;
    public FluoObjectHandler(){
        GOHandler=new GraphicalObjectsHandler();
        GOPs=new ArrayList <GraphicalObjectProperty>();
        backgroundPixels=new IntArray2();
        pixels=new int[1][1];
        impl=null;
        IPOHs=null;
    }
    public void setImage(ImagePlus impl){
        this.impl=impl;
    }

    public void collectBackgroundPixels(){
        ROIs=CommonDataSet.ROIOrganizer.importROIs(impl);
        Roi ROI;
        int n=impl.getNSlices();
        int i,j,w=impl.getWidth(),h=impl.getHeight();
        byte[] pixels;
        int numROIs=ROIs.size();
        backgroundPixels.m_IntArray2.clear();
        for(i=1;i<=n;i++){
            impl.setSlice(i);
            pixels=(byte[])impl.getProcessor().getPixels();
            IntArray ir=new IntArray();
            for(j=0;j<numROIs;j++){
                ROI=ROIs.get(j);
                collectROI(pixels,w,h,ROI,ir);
            }
            QuickSortInteger.quicksort(ir);
            backgroundPixels.m_IntArray2.add(ir);
        }
    }

    int backgroundPixel(int sliceIndex, double percentile){
        int index=(int) ((1.-percentile)*backgroundPixels.m_IntArray2.get(0).m_intArray.size());
        return backgroundPixels.m_IntArray2.get(sliceIndex-1).m_intArray.get(index);
    }

    public ArrayList <GraphicalObject> collectFluoParticles(ImagePlus impl, int sliceIndex){
        ArrayList <GraphicalObject> GOs;
        GraphicalObjectProperty gop=new GraphicalObjectProperty();
        int threshold=backgroundPixel(sliceIndex,0.0001);
        ArrayList<GraphicalObjectProperty> GOPs=new ArrayList<GraphicalObjectProperty>();
        gop.bConnectDiagonally=true;
        gop.name=new String("Fluorescent Object");
        gop.intensityRange.setRange(threshold, 255);
        GOPs.add(gop);
        GOs=GOHandler.findObjects(impl, GOPs);
        return GOs;
    }

    void collectROI(byte[]pixels, int w, int h, Roi ROI, IntArray ir){
        Rectangle br=ROI.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height;
        int x,y,i,j,o;

        for(i=0;i<rh;i++){
            y=yo+i;
            o=w*y;
            for(j=0;j<rw;j++){
                x=xo+j;
                if(ROI.contains(x, y)){
                        ir.m_intArray.add(0xff&pixels[o+x]);
                }
            }
        }
    }
}
