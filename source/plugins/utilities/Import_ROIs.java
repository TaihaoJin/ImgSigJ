/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.WindowManager;
import CommonDataClasses.CommonDataSet;
import ij.gui.Roi;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class Import_ROIs implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        ArrayList<Roi> ROIs=CommonDataSet.ROIOrganizer.importROIs(impl);
        CommonDataSet.ROIOrganizer.loadROIs(impl,ROIs);
/*        try{ImportROIs();}
        catch (IOException e){
            IJ.error("IOException");
        }*/
    }
/*
    void ImportROIs() throws IOException {
//        ImagePlus impl=WindowManager.getCurrentImage();
//        Roi ROI=impl.getRoi();
        Roi ROI=null;
        OpenDialog sd=new OpenDialog("Import a group of region of interest","");
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
        int numROIs=Integer.valueOf(st);
        CommonDataSet.ROIs.clear();
        for(int i=0;i<numROIs;i++){
            Roi ROIt=ImportROI(br);
            CommonDataSet.ROIs.add(ROIt);
        }
        br.close();
        f.close();
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
    }*/
}
