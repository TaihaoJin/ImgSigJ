/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import utilities.CommonMethods;
import utilities.statistics.Histogram;
import ij.WindowManager;
import utilities.statistics.HistogramHandler;
import utilities.io.FileAssist;
import java.util.Formatter;
import utilities.CommonGuiMethods;
import ij.gui.GenericDialog;

/**
 *
 * @author Taihao
 */
public class exportPixelValueHistogram_ implements PlugIn{
    public void run(String args){
        exportPixelValueHistogram();
    }
    void exportPixelValueHistogram(){
        ImagePlus impl=WindowManager.getCurrentImage();
        int pixelRange[]=new int[2];
        Histogram hist=CommonMethods.getPixelValueHistogram(impl);
        hist.setTitles("pixelValue", "counts");
        String path=FileAssist.getFilePath("choose file to export the pixel value histogram", "","text file", "txt", false);
        Formatter fm=FileAssist.getFormatter(path);
        int ws=5;
        GenericDialog gd=new GenericDialog("Histogram Smoothing Option");
        gd.addNumericField("window size for smoothing", 2, 0);
        gd.addNumericField("number of iterations for smoothing", 1, 0);
        gd.showDialog();
        ws=(int) (gd.getNextNumber()+0.5);
        hist.setTitles("PixelValue", "Counts");
        HistogramHandler.exportHistogram(fm, hist, ws);
        fm.close();
    }
}
