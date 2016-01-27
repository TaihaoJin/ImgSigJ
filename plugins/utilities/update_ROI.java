/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import CommonDataClasses.CommonDataSet;
import ij.plugin.PlugIn;
import ij.WindowManager;

/**
 *
 * @author Taihao
 */
public class update_ROI implements PlugIn{
    public void run(String arg){
        CommonDataSet.ROIOrganizer.updateROI(WindowManager.getCurrentImage());
    }
}
