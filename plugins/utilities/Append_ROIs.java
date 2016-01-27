/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.WindowManager;
import CommonDataClasses.CommonDataSet;

/**
 *
 * @author Taihao
 */
public class Append_ROIs implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        CommonDataSet.ROIOrganizer.appendROIs(impl, CommonDataSet.ROIOrganizer.importROIs(impl));
/*        try{ImportROIs();}
        catch (IOException e){
            IJ.error("IOException");
        }*/
    }
}