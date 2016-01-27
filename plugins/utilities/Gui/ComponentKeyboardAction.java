/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.Component;
import utilities.CommonImageMethods;
import utilities.Gui.ClipboardExt;
import java.awt.Image;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import utilities.CommonGuiMethods;

/**
 *
 * @author Taihao
 */
public class ComponentKeyboardAction extends AbstractAction{
    public static ClipboardExt clipboard=new ClipboardExt();
    Component source;
    String keyString;
    public ComponentKeyboardAction(){
        keyString="Cntr+C";
    }
    public ComponentKeyboardAction(String keyString){
        this.keyString=keyString;
    }
    public void actionPerformed(ActionEvent e){
        Object o=e.getSource();
        if(o instanceof Component){
            source=(Component) e.getSource();
            performAction();
        }
    }
    void performAction(){
        if(keyString.contentEquals("Cntr+Shift+C")){
           if(source!=null){
               Component cmp=CommonGuiMethods.getTopLevelAncestor(source);
                Image img=CommonImageMethods.getScreenshort(cmp);
                clipboard.setImage(img);
                clipboard.run("scopy");
            }
        }
    }
}
