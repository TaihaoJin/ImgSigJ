/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import CommonDataClasses.CommonDataSet;
import ij.plugin.PlugIn;
import ij.gui.Roi;
import ij.gui.PolygonRoi;
import ij.gui.OvalRoi;
import ij.gui.FreehandRoi;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.Rectangle;
import ij.io.SaveDialog;
import java.util.Formatter;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.*;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class Save_ROIs implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        CommonDataSet.ROIOrganizer.saveROIs(impl);
    }
/*
    void saveROIs(){
        ImagePlus impl=WindowManager.getCurrentImage();
        Roi ROI;

        String title=impl.getTitle();
        String newline = System.getProperty("line.separator");
        int position=title.indexOf('.');
        if(position>0) title=title.substring(0,position-1);
        SaveDialog sd=new SaveDialog("Saving the region of interest",title+"_1",".Ris");
        String path=sd.getDirectory()+sd.getFileName();
        Formatter fm=QuickFormatter(path);
        int size=CommonDataSet.ROIs.size();
        fm.format("Num_ROIS:  %3d%s", size,newline);
        for(int i=0;i<size;i++){
            saveROI(CommonDataSet.ROIs.get(i),fm);
        }
        fm.close();
    }

    void saveROI(Roi ROI, Formatter fm){
        Rectangle br=ROI.getBounds();
        int type=ROI.getType();

        switch (type){
            case Roi.OVAL:
                saveOvalROI(ROI, fm);
                break;
            case Roi.FREEROI:
                saveFreehandROI(ROI,fm);
                break;
            case Roi.POLYGON:
                savePolygonROI(ROI,fm);
                break;
            case Roi.RECTANGLE:
                saveRectangleROI(ROI,fm);
                break;
        }
    }

    void saveOvalROI(Roi ROI, Formatter fm){
        String newline = System.getProperty("line.separator");
        Rectangle bounds=ROI.getBounds();
        int x=bounds.x,y=bounds.y;
        fm.format("ROITYPE(Oval): %2d%s", ROI.getType(), newline);
        fm.format("x:  %6d  y:  %6d  width:  %6d  height:  %6d%s",x,y,bounds.width,bounds.height,newline);
    }

    void saveRectangleROI(Roi ROI, Formatter fm){
        String newline = System.getProperty("line.separator");
        Rectangle bounds=ROI.getBounds();
        int x=bounds.x,y=bounds.y;
        fm.format("ROITYPE(Rectangle): %2d%s", ROI.getType(), newline);
        fm.format("x:  %6d  y:  %6d  width:  %6d  height:  %6d%s",x,y,bounds.width,bounds.height,newline);
    }

    void savePolygonROI(Roi ROI, Formatter fm){
        String newline = System.getProperty("line.separator");
        Polygon plgn=ROI.getPolygon();
        int nPoints=plgn.npoints;
        int[] xPoints=plgn.xpoints, yPoints=plgn.ypoints;
        Rectangle bounds=ROI.getBounds();
        int x=bounds.x,y=bounds.y;
        fm.format("ROITYPE(Polygon): %2d  %s", ROI.getType(),newline);
        fm.format("nPoints:  %6d%s",nPoints,newline);
        fm.format("originX:  %6d  originY  %6d%s",x,y,newline);
        for(int i=0;i<nPoints;i++){
            fm.format("vertex:  %6d  xPoint:  %6d  yPoint:  %6d%s",i,xPoints[i],yPoints[i],newline);
        }
    }

    void saveFreehandROI(Roi ROI, Formatter fm){
        String newline = System.getProperty("line.separator");
        Polygon plgn=ROI.getPolygon();
        int nPoints=plgn.npoints;
        int[] xPoints=plgn.xpoints, yPoints=plgn.ypoints;
        Rectangle bounds=ROI.getBounds();
        int x=bounds.x,y=bounds.y;
        fm.format("ROITYPE(Freehand): %2d  %s", ROI.getType(),newline);
        fm.format("nPoints:  %6d%s",nPoints,newline);
        fm.format("originX:  %6d  originY  %6d%s",x,y,newline);
        for(int i=0;i<nPoints;i++){
            fm.format("vertex:  %6d  xPoint:  %6d  yPoint:  %6d%s",i,xPoints[i],yPoints[i],newline);
        }
    }

    Formatter QuickFormatter (String path){
        Formatter fm=null;
        File file=new File(path);
        try {
                fm= new Formatter(file);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Fount");
        }
        return fm;
    }*/
}
