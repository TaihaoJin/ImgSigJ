/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.plugin.PlugIn;
import ij.gui.Roi;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.Rectangle;
import ij.io.OpenDialog;
import java.util.Formatter;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.*;
import ij.IJ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.FreehandRoi;

/**
 *
 * @author Taihao
 */
public class Import_ROI implements PlugIn{
    public void run(String arg){
        try{ImportROI();}
        catch (IOException e){
            IJ.error("IOException");
        }
    }

    void ImportROI() throws IOException {
        ImagePlus impl=WindowManager.getCurrentImage();
        Roi ROI=impl.getRoi();
        OpenDialog sd=new OpenDialog("Import a region of interest","");
        String path=sd.getDirectory()+sd.getFileName();
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
        impl.setRoi(ROI);
        br.close();
        f.close();
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
}
