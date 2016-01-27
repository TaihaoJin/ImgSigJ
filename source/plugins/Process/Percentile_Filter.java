/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Process;
import ij.ImagePlus;
import ij.WindowManager;
import ImageAnalysis.PercentileFilter;
import utilities.CommonMethods;
import utilities.Geometry.ImageShapes.CircleImage;
import java.awt.Point;
import ij.gui.GenericDialog;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class Percentile_Filter implements ij.plugin.PlugIn{
    public void run(String args){
        ImagePlus impl=WindowManager.getCurrentImage();
        double dPer=0.02;
        int radius=25;
        GenericDialog gd=new GenericDialog("parameters for the Percentile_Filter");
        ImagePlus implc=CommonMethods.cloneImage(impl);
        PercentileFilter pf=new PercentileFilter(implc,true);
        implc.show();
        ImagePlus implb=pf.getBackgroundImage();
        implb.show();
    }
}
