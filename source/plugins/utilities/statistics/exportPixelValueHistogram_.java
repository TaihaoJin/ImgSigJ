/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import utilities.CommonMethods;
import utilities.statistics.Histogram;
import ij.WindowManager;
import utilities.statistics.HistogramHandler;
import utilities.io.FileAssist;
import java.util.Formatter;

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
        Histogram hist=CommonMethods.getPixelValueHistogram(impl);
        hist.setTitles("pixelValue", "counts");
        String path=FileAssist.getFilePath("choose file to export the pixel value histogram","","","txt", false);
        Formatter fm=FileAssist.getFormatter(path);
        HistogramHandler.exportHistogram(fm, hist);
    }
}
