/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import CommonDataClasses.CommonDataSet;
import ij.ImagePlus;
import ij.WindowManager;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class Store_ROI implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        CommonDataSet.ROIOrganizer.storeROI(impl);
    }
}
