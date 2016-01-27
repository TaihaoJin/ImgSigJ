/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.WindowManager;
import ij.ImagePlus;
import utilities.CommonMethods;
import ij.process.ImageConverter;
import ij.plugin.PlugIn;/**
 *
 * @author Taihao
 */
public class Randomize_Image implements PlugIn{
    public void run(String arg){
//        ImagePlus impl=CommonMethods.cloneImage("Randomized image", WindowManager.getCurrentImage());
        ImagePlus impl=WindowManager.getCurrentImage();
//        impl.show();
        CommonMethods.randomizeImage(impl);
    }
}
