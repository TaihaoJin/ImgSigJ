/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import CommonDataClasses.CommonDataSet;
import ij.plugin.PlugIn;
import ij.gui.Roi;
import ij.ImagePlus;
import java.awt.Color;
import ij.WindowManager;
import java.util.ArrayList;
import ij.process.ImageProcessor;
import java.awt.Polygon;

/**
 *
 * @author Taihao
 */
public class Mark_ROIs implements PlugIn{
    public void run(String arg){
        MarkROIs();
    }

    void MarkROIs(){
        ImagePlus impl=WindowManager.getCurrentImage();
        ImagePlus rgbImpl=CommonMethods.copyToRGBImage(impl);
        ArrayList <Roi> ROIs=CommonDataSet.ROIOrganizer.getROIs(impl);
        Roi ROI;
        Color c=Color.red;
        int numROIs=ROIs.size(),numSlices=rgbImpl.getNSlices();
        int i,j,nPoints,k,x0,x,y0,y;
        int[] xPoints,yPoints;
        ImageProcessor ip;
        Polygon plgn;
//        int deltax=265-383,deltay=230-121;
        int deltax=0,deltay=0;
        for(i=1;i<=numSlices;i++){
            rgbImpl.setSlice(i);
            ip=rgbImpl.getProcessor();
            ip.setColor(c);
            for(j=0;j<numROIs;j++){
                ROI=ROIs.get(j);
                plgn=ROI.getPolygon();
                nPoints=plgn.npoints;
                xPoints=plgn.xpoints;
                yPoints=plgn.ypoints;
                x0=xPoints[nPoints-1]+deltax;
                y0=yPoints[nPoints-1]+deltay;
                for(k=0;k<nPoints;k++){
                    x=xPoints[k]+deltax;
                    y=yPoints[k]+deltay;
                    ip.drawLine(x0, y0, x, y);
                    x0=x;
                    y0=y;
                }
            }
        }
        rgbImpl.show();
    }
}
