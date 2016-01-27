/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Process;
import ij.ImagePlus;
import ij.WindowManager;
import utilities.CommonMethods;
import ij.gui.GenericDialog;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class Image_Subtraction implements ij.plugin.PlugIn {
    ImagePlus impl,implb,implc;
    public void run(String arg){
        chooseImages();
        CommonMethods.pixelSubtraction(implc,implb);
        implc.setTitle('"'+impl.getTitle()+'"'+" subtracted by " + '"'+implb.getTitle()+'"');
        implc.show();
    }
    void chooseImages(){
        ArrayList<ImagePlus> images=CommonMethods.getAllOpenImages();
        int len=images.size();
        String label="choose minuend and subtrahend for image substraction";
        GenericDialog gd=new GenericDialog(label);
        ArrayList <String> vcItems=new ArrayList();

        String label1="choose minuend", label2="choose subtrahend";

        String defaultMinuend=WindowManager.getCurrentImage().getTitle(), defaultSubtrahend;

        int i, defaultMid=0, defaultSid;
        String sTitle;
        for(i=0;i<len;i++){
            sTitle=images.get(i).getTitle();
            vcItems.add(sTitle);
            if(sTitle.contentEquals(defaultMinuend)) defaultMid=i;
        }

        defaultSid=(defaultMid+1)%len;
        defaultSubtrahend=images.get(defaultSid).getTitle();

        String[] items=new String[len];
        for(i=0;i<len;i++){
            items[i]=vcItems.get(i);
        }

        gd.addChoice(label1, items, items[defaultMid]);
        gd.addChoice(label2, items, items[defaultSid]);
        gd.showDialog();
        int mId=gd.getNextChoiceIndex(), sId=gd.getNextChoiceIndex();
        impl=images.get(mId);
        implb=images.get(sId);
        implc=CommonMethods.cloneImage(impl);
    }
}
