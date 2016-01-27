/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.WindowManager;

/**
 *
 * @author Taihao
 */
public class Delete_Current_Slice implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        int num=impl.getStackSize();
        int n=impl.getCurrentSlice();
        int n1=impl.getSlice();
        impl.hide();
        if(n<impl.getStackSize()) //the method deletSlice(n) does not work for the last slice (shouldn't have been this way)
            impl.getStack().deleteSlice(n);
        else
            impl.getStack().deleteLastSlice();
        impl.show();
    }
}
