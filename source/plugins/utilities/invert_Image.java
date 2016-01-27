/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import utilities.CommonMethods;
import ij.plugin.PlugIn;
import ij.WindowManager;

/**
 *
 * @author Taihao
 */
public class invert_Image implements PlugIn {
    public void run(String arg){
        CommonMethods.invertImage(WindowManager.getCurrentImage());
        CommonMethods.watershedImage(WindowManager.getCurrentImage());
    }
}
