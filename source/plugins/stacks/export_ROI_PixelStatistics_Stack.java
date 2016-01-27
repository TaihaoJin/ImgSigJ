/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import utilities.CommonMethods;
import utilities.io.PrintAssist;
import utilities.ArrayofArrays.DoubleArray;
import java.util.ArrayList;
import utilities.QuickFormatter;
import java.util.Formatter;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import utilities.ArgumentDecoder;
import utilities.statistics.MeanSem0;
import utilities.CommonStatisticsMethods;
import utilities.io.FileAssist;
import ij.gui.Roi;

/**
 *
 * @author Taihao
 */
public class export_ROI_PixelStatistics_Stack implements ij.plugin.PlugIn{
    public void run(String arg){
        ArgumentDecoder argTable=new ArgumentDecoder(arg," ",true);
        ImagePlus impl=WindowManager.getCurrentImage();
        int num=impl.getNSlices();
        int h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels=new int[h][w];
        int i,j,k,len;
        ArrayList <DoubleArray> arrays =new ArrayList<DoubleArray>();
        ArrayList <String> titles=new ArrayList<String>();
        titles.add("FrameNumber");
        titles.add("AverageIntensity");
        DoubleArray fn=new DoubleArray();;
        DoubleArray trace=new DoubleArray();
        ArrayList <Integer> precisions=new ArrayList<Integer>();
        precisions.add(0);
        precisions.add(2);
        MeanSem0 ms;
        double ave;
        len=h*w;
        Roi roi=impl.getRoi();
        for(i=1;i<=num;i++){
            impl.setSlice(i);
            CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
            ms=CommonStatisticsMethods.buildMeanSem(pixels,roi);
            ave=ms.mean;
            fn.m_DoubleArray.add(new Double(i));
            trace.m_DoubleArray.add(new Double(ave));
        }
        arrays.add(fn);
        arrays.add(trace);

        String title=argTable.getString("title:");
        String path=argTable.getString("path:");
        path=impl.getTitle()+".txt";
        QuickFormatter fm=new QuickFormatter(path);
        PrintAssist pa=new PrintAssist();
        pa.printArrays(fm.getFormatter(), titles, precisions, arrays);
        fm.getFormatter().close();
    };
}
