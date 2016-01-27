/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.FormatConverters;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.WindowManager;
import ij.plugin.PlugIn;

/**
 *
 * @author Taihao
 */
public class ConvertCurrentImageTo_Gray8 {
    public void run(String arg){        
        ImagePlus impl;
        impl=WindowManager.getCurrentImage();
        ImageConverter ic=new ImageConverter(impl);
        ic.convertToGray8();
        impl.show();
    }
}
