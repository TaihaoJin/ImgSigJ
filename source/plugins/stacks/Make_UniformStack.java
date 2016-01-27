/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;
import ij.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.*;
/**
 *
 * @author Taihao
 */
public class Make_UniformStack implements ij.plugin.PlugIn{
    public void run(String args){
        makeUniformStack();
    }
    void makeUniformStack(){
        ImagePlus impl=WindowManager.getCurrentImage();
        int w=impl.getWidth(),h=impl.getHeight(),len=impl.getStackSize();
        ImageStack is=new ImageStack(w,h);
        ImageProcessor impr=impl.getProcessor();
        int i;
        for(i=0;i<len;i++){
            is.addSlice("", impr);
        }
        ImagePlus implc=new ImagePlus("uniform image stack", is);
        implc.show();
    }
}
