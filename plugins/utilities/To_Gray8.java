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
public class To_Gray8 implements PlugIn{
    public void run(String arg){        
        ImagePlus impl;
        impl=WindowManager.getCurrentImage();
        impl.show();
        ImageConverter ic=new ImageConverter(impl);
        ic.convertToGray8();
        impl.show();
    }
}
