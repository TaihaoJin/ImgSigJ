/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.WindowManager;
import ij.plugin.PlugIn;

/**
 *
 * @author Taihao
 */
public class Clone_Image implements PlugIn{

    public void run(String arg){        
        ImagePlus impl,impl0,impl1;
        impl=WindowManager.getCurrentImage();
        impl.show();
        impl0=CommonMethods.cloneImage(impl);
        impl0.show();
        impl1=CommonMethods.cloneImage(impl);
        impl1.show();
    }
}
