/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import ij.gui.Roi;
import java.util.ArrayList;
import ij.ImagePlus;
import java.awt.Color;
import utilities.CommonMethods;
import utilities.CommonGuiMethods;
import java.awt.Color;
import java.awt.Point;
import ij.gui.PointRoi;

/**
 *
 * @author Taihao
 */
public class RoiHighlighter {
    public static ArrayList<RoiHighlightNode> StoredHighlighters=new ArrayList();
    public static RoiHighlightNode highlighter;
    public static Thread HighlightingThread;
    public static void addRoi(ImagePlus impl,Roi roi){
        if(highlighter==null){
            highlighter=new RoiHighlightNode(impl);
        }
        if(highlighter.impl!=impl){
            highlighter=new RoiHighlightNode(impl);
        }
        highlighter.addRoi(roi);
    }
    public static void removeRoi(ImagePlus impl,Roi roi){
        if(highlighter!=null){
            highlighter.removeRoi(roi);
        }
    }
    public static void addRoi(ImagePlus impl,ArrayList<Roi> rois){
        if(highlighter==null){
            highlighter=new RoiHighlightNode(impl);
        }
        if(highlighter.impl!=impl){
            highlighter=new RoiHighlightNode(impl);
        }
        highlighter.addRois(rois);
    }
    public static void removeRoi(ImagePlus impl,ArrayList<Roi> rois){
        if(highlighter!=null){
            highlighter.removeRois(rois);
        }
    }
    public static RoiHighlightNode getStoredHighlighter(ImagePlus impl){
        int i,len=StoredHighlighters.size();
        for(i=0;i<len;i++){
            if(StoredHighlighters.get(i).impl==impl) return StoredHighlighters.get(i);
        }
        return null;
    }
    public static void clearCurrentHighlighter(){
        if(highlighter!=null)highlighter.clear();
    }
    public static RoiHighlightNode getHighlighter(ImagePlus impl){
        int i,len=StoredHighlighters.size();
        RoiHighlightNode hl=getStoredHighlighter(impl);
        if(highlighter!=null){
            if(highlighter.impl==impl){
                if(hl!=null){
                    hl.addRois(highlighter.Rois);
                }else{
                    hl=highlighter;
                }
            }
        }
        return hl;
    }
    public static void highLight(){
        int i,len;
        highlighter.highlight();
        len=StoredHighlighters.size();
        for(i=0;i<len;i++){
            StoredHighlighters.get(i).highlight();
        }
    }
    public static void clearHighlighter(){
        highlighter.clear();
        StoredHighlighters.clear();
    }
    public static void highlightPoints(ImagePlus impl, ArrayList<Point> points, Color c){
        int len=points.size();
        ArrayList<Roi> Rois=new ArrayList();
        Roi roi;
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            roi=new PointRoi(p.x,p.y);
            roi.setColor(c);
            Rois.add(roi);
        }
        RoiHighlighter.clearCurrentHighlighter();
        RoiHighlighter.addRoi(impl, Rois);
        RoiHighlighter.highLight();
    }
}
