/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CommonDataClasses;
import CommonDataClasses.CommonDataSet;
import ij.plugin.PlugIn;
import ij.gui.Roi;
import ij.ImagePlus;
import java.awt.Color;
import ij.WindowManager;
import java.util.ArrayList;
import ij.process.ImageProcessor;
import java.awt.Polygon;
import utilities.CommonMethods;
import ij.io.OpenDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.FreehandRoi;

import ij.WindowManager;
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
public class RoiHandler {
    protected int ID;
    protected int currentIndex;
    ArrayList <Roi> ROIs=new ArrayList <Roi>();
    String sROIFileName="";
    String sDefaultROIDir="";
    String sDefaultROIFileName="";
    public RoiHandler(int ID){
        this.ID=ID;
        currentIndex=-1;
    }
    public int getCurrentIndex(){
        return currentIndex;
    }
    public int nextIndex(){
        int size=ROIs.size();
        if(size==0) return -1;
        int nextIndex=(currentIndex+1)%size;
        currentIndex=nextIndex;
        return nextIndex;
    }
    public int getID(){
        return ID;
    }
    public Roi getROI(int index){
        return (Roi)ROIs.get(index).clone();
    }
    public void updateROI(int index, Roi ROI){
        ROIs.set(index,(Roi)ROI.clone());
    }
    public boolean matchingImage(ImagePlus impl){
        return ID==impl.getID();
    }
    public ArrayList <Roi> getROIs(ImagePlus impl){
        ArrayList <Roi> ROIs=null;
        if(matchingImage(impl)){
            ROIs=new ArrayList<Roi>();
            int size=this.ROIs.size();
            for(int i=0;i<size;i++){
                ROIs.add((Roi)(this.ROIs.get(i).clone()));
            }
        }
        return ROIs;
    }
    public void storeROI(ImagePlus impl){
        if(matchingImage(impl)) ROIs.add((Roi)(impl.getRoi().clone()));
    }
    public void clearROIs(ImagePlus impl){
        if(matchingImage(impl)) ROIs.clear();
    }

    public void appendROIs(ImagePlus impl, ArrayList <Roi> ROIs){
        if(matchingImage(impl)){
            int size=ROIs.size();
            for(int i=0;i<size;i++){
                this.ROIs.add((Roi)(ROIs.get(i).clone()));
            }
        }
    }
    public void loadROIs(ImagePlus impl, ArrayList <Roi> ROIs){
        this.ROIs.clear();
        if(matchingImage(impl)){
            int size=ROIs.size();
            for(int i=0;i<size;i++){
                this.ROIs.add((Roi)(ROIs.get(i).clone()));
            }
        }
    }
    int MarkROIs(ImagePlus impl){
        if(!matchingImage(impl)) return 0;
        ImagePlus rgbImpl=CommonMethods.copyToRGBImage(impl);
        ArrayList<Roi> ROIs=CommonDataSet.ROIOrganizer.getROIs(impl);
        Roi ROI;
        Color c=Color.red;
        int numROIs=ROIs.size(),numSlices=rgbImpl.getNSlices();
        int i,j,nPoints,k,x0,x,y0,y;
        int[] xPoints,yPoints;
        ImageProcessor ip;
        Polygon plgn;
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
                x0=xPoints[nPoints-1];
                y0=yPoints[nPoints-1];
                for(k=0;k<nPoints;k++){
                    x=xPoints[k];
                    y=yPoints[k];
                    ip.drawLine(x0, y0, x, y);
                    x0=x;
                    y0=y;
                }
            }
        }
        rgbImpl.show();
        return 1;
    }
    public ArrayList <Roi> ImportROIs(ImagePlus impl) throws IOException {
//        ImagePlus impl=WindowManager.getCurrentImage();
//        Roi ROI=impl.getRoi();
        ArrayList <Roi> ROIs=new ArrayList <Roi>();
        Roi ROI=null;
        OpenDialog sd=new OpenDialog("Import a group of region of interest",sDefaultROIDir,sDefaultROIFileName);
        String path=sd.getDirectory()+sd.getFileName();
        sROIFileName=path;
        File file = new File(path);
        FileReader f=null;
        try{f=new FileReader(file);}
        catch(FileNotFoundException e){
            IJ.error("");
        }
        BufferedReader br=new BufferedReader(f);

        String line=br.readLine();
        StringTokenizer stk=new StringTokenizer(line," ",false);
        String st=stk.nextToken();
        st=stk.nextToken();
        int numROIs=Integer.valueOf(st);
        clearROIs(impl);
        for(int i=0;i<numROIs;i++){
            Roi ROIt=ImportROI(br);
            ROIs.add(ROIt);
        }
        br.close();
        f.close();
        return ROIs;
    }

    Roi ImportROI(BufferedReader br) throws IOException {
        Roi ROI=null;
        String line=br.readLine();
        StringTokenizer stk=new StringTokenizer(line," ",false);
        String st=stk.nextToken();
        st=stk.nextToken();
        int type=Integer.valueOf(st);

        switch (type){
            case Roi.OVAL:
                ROI=importOvalROI(br);
                break;
            case Roi.FREEROI:
                ROI=importPolygonROI(br);
                break;
            case Roi.POLYGON:
                ROI=importPolygonROI(br);
                break;
            case Roi.RECTANGLE:
                ROI=importRectangleROI(br);
                break;
        }
        return ROI;
    }

    Roi importOvalROI (BufferedReader br)throws IOException {
        String line=br.readLine();
        StringTokenizer stk=new StringTokenizer(line);
        stk.nextToken();
        int x=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int y=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int width=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int height=Integer.valueOf(stk.nextToken());
        OvalRoi ROI=new OvalRoi(x,y,width,height);
        return ROI;
    }

    Roi importRectangleROI(BufferedReader br)throws IOException {
        String line=br.readLine();
        StringTokenizer stk=new StringTokenizer(line);
        stk.nextToken();
        int x=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int y=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int width=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int height=Integer.valueOf(stk.nextToken());
        Roi ROI=new Roi(x,y,width,height);
        return ROI;
    }

    Roi importPolygonROI(BufferedReader br)throws IOException {
        String line=br.readLine();
        StringTokenizer stk=new StringTokenizer(line);
        stk.nextToken();
        int nPoints=Integer.valueOf(stk.nextToken());
        line=br.readLine();
        stk=new StringTokenizer(line);
        stk.nextToken();
        int x=Integer.valueOf(stk.nextToken());
        stk.nextToken();
        int y=Integer.valueOf(stk.nextToken());
        int p;
        String sTemp;
        int[] xPoints=new int[nPoints], yPoints=new int[nPoints];
        for(int i=0;i<nPoints;i++){
            line=br.readLine();
            stk=new StringTokenizer(line);
            stk.nextToken();
            stk.nextToken();
            stk.nextToken();
            sTemp=stk.nextToken();
            p=Integer.valueOf(sTemp);
            xPoints[i]=p;
            stk.nextToken();
            sTemp=stk.nextToken();
            p=Integer.valueOf(sTemp);
            yPoints[i]=p;
        }
        PolygonRoi plgnROI=new PolygonRoi(xPoints,yPoints,nPoints,Roi.POLYGON);
        return plgnROI;
    }

    void saveROIs(ImagePlus impl){
        Roi ROI;

        String title=impl.getTitle();
        String newline = System.getProperty("line.separator");
        int position=title.indexOf('.');
        if(position>0) title=title.substring(0,position-1);
        sROIFileName=title+"_1";
        SaveDialog sd=new SaveDialog("Saving the region of interest",sROIFileName,".Ris");
        String path=sd.getDirectory()+sd.getFileName();
        sDefaultROIDir=sd.getDirectory();
        sDefaultROIFileName=sd.getFileName();
        Formatter fm=QuickFormatter(path);
        int size=ROIs.size();
        fm.format("Num_ROIS:  %3d%s", size,newline);
        for(int i=0;i<size;i++){
            saveROI(ROIs.get(i),fm);
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
    }
    public String getROIFileName(){
        return sROIFileName;
    }
}
