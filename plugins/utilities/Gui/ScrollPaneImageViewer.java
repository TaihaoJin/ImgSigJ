/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import ij.ImagePlus;
import FluoObjects.IPOGTrackNode;
import javax.swing.JViewport;
import javax.swing.*;
import utilities.CommonGuiMethods;
import utilities.statistics.LevelTransitionDetector_Downward;
import java.util.ArrayList;
import utilities.Gui.HighlightingRoiCollectionContainer;
import utilities.Gui.HighlightingRoiCollectionNode;
import ij.gui.Roi;
import ij.gui.Line;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import ij.gui.PlotWindow;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.StringTokenizer;
import utilities.io.PrintAssist;
import org.apache.commons.math.stat.regression.SimpleRegression;
import ij.gui.Roi;
import ij.gui.PointRoi;
import java.awt.event.ActionEvent;
import utilities.CommonMethods;
import utilities.CommonGuiMethods;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.intRange;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.Gui.ScrollablePicture;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import ij.gui.Roi;
import ij.gui.Line;

/**
 *
 * @author Taihao
 */
public class ScrollPaneImageViewer {
    JScrollPane m_cJSP;
    JViewport m_cJVP;
    int m_nMag,minBrightness;
    int[][] pixels;
    BufferedImage img;
    public ScrollPaneImageViewer(JScrollPane jsp){
        m_cJSP=jsp;
        minBrightness=50;
    }
    public void updateImage(int[][] pixels,int nMag){
        updateImage(pixels,nMag,null,1);
    }
    public void updateImage(int[][] pixels,int nMag,ArrayList<Roi> rois, int font){
        m_nMag=nMag;
        this.pixels=pixels;
        img=CommonMethods.getBufferedImage(BufferedImage.TYPE_INT_RGB, pixels, minBrightness,m_nMag);
        if(rois!=null)CommonMethods.drawRois(img, rois, nMag,font);
        ImageIcon ic=new ImageIcon();
        ic.setImage(img);
        ScrollablePicture sp=new ScrollablePicture(ic,5);
        JViewport IPOImage=new JViewport();
        IPOImage.setView(sp);
        m_cJSP.setViewport(IPOImage);
    }
}
