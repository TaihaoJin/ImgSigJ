/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import utilities.CommonMethods;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.statistics.Histogram;
import utilities.statistics.HistogramHandler;
import ImageAnalysis.RegionBoundaryAnalyzer;
import utilities.io.FileAssist;
import java.util.Formatter;

/**
 *
 * @author Taihao
 */
public class export_BorderSegmentHeightHistogram implements PlugIn{
    public void run(String arg){
        exportHist();
    };
    void exportHist(){
        ImagePlus impl=WindowManager.getCurrentImage();
        int h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels=CommonMethods.getPixelValues(impl),stamp=new int[h][w];
        LandscapeAnalyzerPixelSorting stampper=new LandscapeAnalyzerPixelSorting(impl);
        stampper.updateAndStampPixels(pixels, stamp);
        RegionBoundaryAnalyzer rbAnalyzer=new RegionBoundaryAnalyzer(stamp);
        Histogram hist=rbAnalyzer.getBorderSegmentHeightHistogram(pixels);
        hist.setTitles("SegmentHeight", "Counts");
        String userHome=FileAssist.getUserHome();
        String path=FileAssist.getFilePath("choose file for bordre segment height histogram", "", "text file", "txt", false);
        Formatter fm=FileAssist.getFormatter(path);
        HistogramHandler.exportHistogram(fm, hist);
        fm.close();
    }
}
