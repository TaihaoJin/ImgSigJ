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
public class Split_Colors implements PlugIn{
    public void run(String arg){        
        ImagePlus impl;
        impl=WindowManager.getCurrentImage();
//        impl.close();
        ImagePlus rgb[]=new ImagePlus[3];
        CommonMethods.splitColors(impl,rgb);
        ImagePlus r=rgb[0],g=rgb[1],b=rgb[2];
        r.show();
        g.show();
        b.show();
    }
}
