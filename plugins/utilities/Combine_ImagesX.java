/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import ij.WindowManager;
import utilities.CommonMethods;
import ij.gui.GenericDialog;
import java.util.ArrayList;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class Combine_ImagesX implements ij.plugin.PlugIn{
    public void run(String arg){
        combineImages();
        chooseImages();
    }
    void combineImages(){

    }
    void chooseImages(){
        ArrayList<ImagePlus> imgs=CommonMethods.getAllOpenImages();
        int len=imgs.size();
        ArrayList <String> vcItems=new ArrayList();

        String defaultImg=WindowManager.getCurrentImage().getTitle();

        int i, id=0;
        String sTitle;
        for(i=0;i<len;i++){
            sTitle=imgs.get(i).getTitle();
            vcItems.add(sTitle);
            if(sTitle.contentEquals(defaultImg)) id=i;
        }

        String[] items=new String[len];
        for(i=0;i<len;i++){
            items[i]=vcItems.get(i);
        }

        String label="choose images to combine horizontaly";
        ArrayList<ImagePlus> images=new ArrayList();
        int refId;
        ImagePlus img;
        for(i=0;i<2;i++){
            refId=i;
            GenericDialog gd=new GenericDialog(label);
            gd.addChoice(PrintAssist.ToString_Order(i+1)+" image", items, items[i]);
            gd.showDialog();
            if(gd.wasOKed()){
                id=gd.getNextChoiceIndex();
                img=imgs.get(id);
                images.add(img);
            }
        }
        ImagePlus implc=CommonMethods.combineImagesX(images.get(0), images.get(1));
        implc.show();
    }
}
