/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

import ij.plugin.PlugIn;
import ij.gui.Roi;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.Rectangle;
import ij.io.OpenDialog;
import java.util.Formatter;
import java.io.File;
import java.io.FileNotFoundException;
import ij.IJ;
/*
 *
 * @author Taihao
 */
public class Export_ROI implements PlugIn{
    public void run(String arg){
        exportROI();
    }

    void exportROI(){
        String newline = System.getProperty("line.separator");
        ImagePlus impl=WindowManager.getCurrentImage();
        Roi ROI=impl.getRoi();
        Rectangle br=ROI.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height,h=impl.getHeight(),w=impl.getWidth();
//        byte[]pixels=(byte[])impl.getProcessor().getPixels();
        int pixels[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
        OpenDialog od=new OpenDialog("Export the pixels into a file","");
        String path=od.getDirectory()+od.getFileName();
        Formatter fm=QuickFormatter(path);
        int x,y,i,j,pixel,o;
        for(i=0;i<rh;i++){
            y=yo+i;
            o=w*y;
            for(j=0;j<rw;j++){
                x=xo+j;
                if(ROI.contains(x, y)){
                    pixel=pixels[y][x];
                    fm.format("%6d  %6d  %6d%s", x,y,pixel,newline);
                }
            }
        }
        fm.close();
    }
    Formatter QuickFormatter (String path){
        Formatter fm=null;
        File file=new File(path);
        try {
                fm= new Formatter(file);
        }
        catch(FileNotFoundException e){
            IJ.error("File Not Fount");
        }
        return fm;
    }
}
