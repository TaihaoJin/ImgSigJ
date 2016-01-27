package utilities.FormatConverters.CurrentImage;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.WindowManager;
import ij.plugin.PlugIn;
/**
 *
 * @author Taihao
 */
public class ConvertTo_Gray8 implements PlugIn{
    public void run(String arg){        
        ImagePlus impl;
        impl=WindowManager.getCurrentImage();
        impl.show();
        ImageConverter ic=new ImageConverter(impl);
        ic.convertToGray8();
        impl.show();
    }
}
