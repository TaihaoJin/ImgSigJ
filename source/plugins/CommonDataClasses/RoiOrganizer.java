/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CommonDataClasses;
import CommonDataClasses.RoiHandler;
import java.util.ArrayList;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.IJ;
import java.io.IOException;

/**
 *
 * @author Taihao
 */
public class RoiOrganizer {
        ArrayList<RoiHandler> RHs;
    public RoiOrganizer(){
        RHs=new ArrayList<RoiHandler>();
    }
    RoiHandler getMatchingRH(ImagePlus impl){
        RoiHandler rh=null;
        int size=RHs.size();
        for(int i=0;i<size;i++){
            rh=RHs.get(i);
            if(rh.matchingImage(impl)){
                break;
            }else{
                rh=null;
            }
        }
        return rh;
    }

    public void storeROI(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        if(rh==null){
            rh=new RoiHandler(impl.getID());
            RHs.add(rh);
        }
        rh.storeROI(impl);
    }

    public void saveROIs(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        if(rh!=null){
            rh.saveROIs(impl);
        }else{
            IJ.showMessage("There is no stored ROI associated with this image. Run Store ROI to store a ROI.");
        }
    }

    public void applyROI(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        if(rh!=null){
            int nextIndex=rh.nextIndex();
            if(nextIndex>=0)
                impl.setRoi(rh.getROI(nextIndex));
            else
                IJ.showMessage("There is no stored ROI associated with this image. Run Store ROI to store a ROI.");
        }else{
            IJ.showMessage("There is no stored ROI associated with this image. Run Store ROI to store a ROI.");
        }
    }

    public void updateROI(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        if(rh!=null){
            int currentIndex=rh.getCurrentIndex();
            if(currentIndex>=0)
                rh.updateROI(currentIndex, impl.getRoi());
            else
                IJ.showMessage("The ROI of current image is not applied from a stored ROI");
        }else{
            IJ.showMessage("There is no stored ROI associated with this image. Run Store ROI to store a ROI.");
        }
    }

    public ArrayList<Roi> importROIs(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        ArrayList<Roi> ROIs;
        if(rh==null){
            rh=new RoiHandler(impl.getID());
            RHs.add(rh);
        }

        try{ROIs=rh.ImportROIs(impl);
            rh.loadROIs(impl,ROIs);
        }
        catch (IOException e){
            IJ.showMessage("Problems with reading the input file of stored ROIs");
            ROIs=null;
        }
        return ROIs;
    }

    public void appendROIs(ImagePlus impl, ArrayList <Roi> ROIs){
        RoiHandler rh=getMatchingRH(impl);
        if(rh==null){
            rh=new RoiHandler(impl.getID());
            RHs.add(rh);
        }
        rh.appendROIs(impl, ROIs);
    }

    public void loadROIs(ImagePlus impl, ArrayList <Roi> ROIs){
        RoiHandler rh=getMatchingRH(impl);
        if(rh==null){
            rh=new RoiHandler(impl.getID());
            RHs.add(rh);
        }
        rh.loadROIs(impl, ROIs);
    }

    public ArrayList<Roi> getROIs(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        ArrayList<Roi> ROIs=null;
        if(rh!=null){
            ROIs=rh.getROIs(impl);
        }else{
            IJ.showMessage("There is no stored ROI associated with this image. Run Store ROI to store a ROI.");
        }
        return ROIs;
    }
    public void clearRIOs(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        if(rh!=null){
            rh.clearROIs(impl);
        }else{
            IJ.showMessage("There is no stored ROI associated with this image. Run Store ROI to store a ROI.");
        }
    }
    public String getROIFileName(ImagePlus impl){
        RoiHandler rh=getMatchingRH(impl);
        ArrayList<Roi> ROIs=null;
        if(rh!=null){
            ROIs=rh.getROIs(impl);
        }else{
            IJ.showMessage("There is no stored ROI associated with this image. Run Store ROI to store a ROI.");
        }
        return rh.getROIFileName();
    }
}
