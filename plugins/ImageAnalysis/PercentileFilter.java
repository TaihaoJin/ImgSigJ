/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;
import ij.ImagePlus;
import utilities.CommonMethods;
import utilities.io.PrintAssist;
import utilities.Geometry.ImageShapes.ImageShape;
import ij.gui.GenericDialog;
import java.util.ArrayList;
import ij.WindowManager;
import utilities.CustomDataTypes.intRange;
/**
 *
 * @author Taihao
 */
public class PercentileFilter {
    public static final int percentileFilter=0,meanFilter=1;
    ImagePlus impl,implb;
    ImageShape m_shape;
    double m_dPercentile;
    int m_nType;
    public PercentileFilter(ImagePlus impl, boolean parDlg){
        if(parDlg){
            m_shape=new ImageShape(true);
            int h=impl.getHeight(),w=impl.getWidth();
            m_shape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
            GenericDialog gd=new GenericDialog("Filtering options");
            String labe="choose filter type";
            ArrayList <String> vcItems=new ArrayList();

            vcItems.add("Percentile");
            vcItems.add("Mean");

            int len=vcItems.size();

            String[] items=new String[len];
            int i,j;
            for(i=0;i<len;i++){
                items[i]=vcItems.get(i);
            }

            String defaultItem="Percentile";
            gd.addChoice(labe, items, defaultItem);
            gd.addNumericField("percentile", 0.02, 2);
            gd.showDialog();
            m_dPercentile=gd.getNextNumber();
            m_nType=gd.getNextChoiceIndex();
            makePercentileFilter(impl,m_dPercentile,m_shape,m_nType);
        }
    }
    public PercentileFilter(ImagePlus impl, double dPer, ImageShape shape, int type){
        makePercentileFilter(impl,dPer,shape,type);
    }
    public void makePercentileFilter(ImagePlus impl, double dPer, ImageShape shape, int type){
        m_shape=shape;
        this.m_dPercentile=dPer;
        this.impl=impl;
        implb=CommonMethods.cloneImage(getTitle()+" "+shape.getDescription(), impl);
        int num=impl.getStackSize();
        for(int i=0;i<num;i++){
            impl.setSlice(i+1);
            implb.setSlice(i+1);
            switch(m_nType){
                case 0:
                    CommonMethods.getPixelPercentile_ImageShape(implb, dPer, shape);
                    break;
                case 1:
                    CommonMethods.getPixelMean_ImageShape(implb, dPer, shape);
                    break;
            }
            
            CommonMethods.pixelSubtraction(impl,implb);
        }
    }
    public double getPercentile(){
        return m_dPercentile;
    }
    public ImagePlus getFiltedImage(){
        return impl;
    }
    public ImagePlus getBackgroundImage(){
        return implb;
    }
    public String getTitle(){
        switch (m_nType){
            case percentileFilter:
                return "percentile: "+PrintAssist.ToString(m_dPercentile*100, 0);
            case meanFilter:
                return "Mean Filter";
            default:
                return "Mean Filter";
        }
    }
}
