/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.WindowManager;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import utilities.CommonMethods;
/**
; * @author Taihao
 */
public class Extract_Slices implements PlugIn{
    public void run(String arg){
        extractSlices();
    }
    void extractSlices(){
        ImagePlus impl=WindowManager.getCurrentImage();
        int num=impl.getNSlices();
        GenericDialog gd=new GenericDialog("Slice Extraction Options");
        gd.addNumericField("first slice", 1, 0);
        gd.addNumericField("last slice", num, 0);
        gd.addNumericField("increment", 1, 0);
        gd.showDialog();
        int ni=(int)(gd.getNextNumber()+0.5);
        int nf=(int)(gd.getNextNumber()+0.5);
        int dn=(int)(gd.getNextNumber()+0.5);

        int w=impl.getWidth(), h=impl.getHeight();
        ImageStack is=new ImageStack(w,h);
        for(int i=ni;i<=nf;i++){
            impl.setSlice(i);
            ImageProcessor ip=CommonMethods.copyProcessor(impl);
            is.addSlice(null, ip);
        }
        ImagePlus implt=new ImagePlus("extracted slices",is);
        implt.show();
    }
}
