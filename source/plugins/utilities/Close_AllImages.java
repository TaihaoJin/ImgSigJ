/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.WindowManager;
import ij.plugin.PlugIn;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class Close_AllImages implements PlugIn{
    public void run(String arg){
        ArrayList <ImagePlus> impls=CommonMethods.getAllOpenImages();
        int size=impls.size();
        for(int i=0;i<size;i++){
            impls.get(i).close();
        }
    }
}
