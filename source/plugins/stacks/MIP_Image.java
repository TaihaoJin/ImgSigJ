/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class MIP_Image implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        int nCurrentSlice=impl.getCurrentSlice();
        ImagePlus implc=CommonMethods.getMIPImage(impl);
        implc.show();
    };
}
