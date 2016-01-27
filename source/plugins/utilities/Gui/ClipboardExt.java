/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.Clipboard;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Image;

/**
 *
 * @author Taihao
 */
public class ClipboardExt extends Clipboard{
    Image img;
    public void setImage(Image img){
        this.img=img;
    }
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if(img==null) return super.getTransferData(flavor);
        return img;
    }    
}
