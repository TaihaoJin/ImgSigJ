/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Demo;
import ij.ImagePlus;
import ij.CompositeImage;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import ij.ImageStack;

/**
 *
 * @author Taihao
 */
public class CreatingImagesDemo {
    public CreatingImagesDemo(){
        int nDim=1000;
        int nSlice=1;
        int FILL_WHITE=4;
        NewImage m_nim=new NewImage();
        ImagePlus impl=NewImage.createRGBImage("test", nDim,nDim, nSlice, FILL_WHITE);
//        ImageProcessor impr=impl.getProcessor();
//        impr.setPixels(pixels);
        impl.show();
        
        ImageStack is=new ImageStack(nDim,nDim,null);
        

    }
}
