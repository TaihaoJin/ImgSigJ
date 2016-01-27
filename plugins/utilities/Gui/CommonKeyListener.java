/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import utilities.CommonImageMethods;

/**
 *
 * @author Taihao
 */
public class CommonKeyListener implements KeyListener, Transferable{
    Frame frame;
    BufferedImage img;
    static java.awt.datatransfer.Clipboard clipboard;
    public void keyPressed(KeyEvent ke){
        char c=ke.getKeyChar();
        int modifier=ke.getModifiers();
        String sModifier=KeyEvent.getKeyModifiersText(modifier);
        if(c=='c'||c=='C'){
            if(sModifier.contentEquals("Cntr")){
//            if(true){
                Object o=ke.getSource();
                if(o instanceof Frame){
                    frame=(Frame) ke.getSource();
                    img=CommonImageMethods.getScreenshort(frame);
                }
            }
        }
    }
    public void keyReleased(KeyEvent ke){
        frame=(Frame)ke.getSource();
    }
    public void keyTyped(KeyEvent ke){
        char c=ke.getKeyChar();
        int modifier=ke.getModifiers();
        String sModifier=KeyEvent.getKeyModifiersText(modifier);
        if(c=='c'||c=='C'){
            if(sModifier.contentEquals("Shift")){
//            if(true){
                Object o=ke.getSource();
                if(o instanceof Frame){
                    frame=(Frame) ke.getSource();
                    img=CommonImageMethods.getScreenshort(frame);
                }
            }
        }
    }
    public DataFlavor[] getTransferDataFlavors() {
	return new DataFlavor[] { DataFlavor.imageFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return DataFlavor.imageFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        return img;
    }
}
