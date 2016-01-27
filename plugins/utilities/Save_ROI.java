/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
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
public class Save_ROI implements PlugIn{
    public void run(String arg){
        saveROI();
    }

    void saveROI(){
        ImagePlus impl=WindowManager.getCurrentImage();
        Roi ROI=impl.getRoi();
        Rectangle br=ROI.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height,h=impl.getHeight(),w=impl.getWidth();
        byte[]pixels=(byte[])impl.getProcessor().getPixels();
        String title=impl.getTitle();
        int position=title.indexOf('.');
        if(position>0) title=title.substring(0,position-1);
        SaveDialog sd=new SaveDialog("Saving the region of interest",title+"_1",".Roi");
        String path=sd.getDirectory()+sd.getFileName();
        Formatter fm=QuickFormatter(path);
        int type=ROI.getType();

        switch (type){
            case Roi.OVAL:
                saveOvalROI(ROI, fm);
                break;
            case Roi.FREEROI:
                savePolygonROI(ROI,fm);
                break;
            case Roi.POLYGON:
                savePolygonROI(ROI,fm);
                break;
            case Roi.RECTANGLE:
                saveRectangleROI(ROI,fm);
                break;
        }        
        fm.close();
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
        fm.format("ROITYPE(Oval): %2d%s", ROI.getType(), newline);
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
    }
}
