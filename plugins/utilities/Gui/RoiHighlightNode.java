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

/**
 *
 * @author Taihao
 */
public class RoiHighlightNode {
        ImagePlus impl;
        ArrayList<Roi> Rois;
        public RoiHighlightNode(ImagePlus impl){
            this.impl=impl;
            Rois=new ArrayList();
        }
        public void addRoi(Roi roi){
            Rois.add(roi);
        }
        public void addRois(ArrayList<Roi> Rois){
            int len=Rois.size(),i;
            for(i=0;i<len;i++){
                addRoi(Rois.get(i));
            }
        }
        public void removeRoi(Roi roi){
            int len=Rois.size(),i;
            for(i=0;i<len;i++){
                if(Rois.get(i)==roi) Rois.remove(i);
            }
        }
        public void removeRois(ArrayList<Roi> Rois){
            int len=Rois.size(),i;
            for(i=0;i<len;i++){
                removeRoi(Rois.get(i));
            }
        }
        public void highlight(){
            int len=Rois.size(),i;
            for(i=0;i<len;i++){
                CommonGuiMethods.highlightRoi(impl, Rois.get(i), Roi.getColor());
            }
        }
        public void highlight(ImagePlus impl){
            int len=Rois.size(),i;
            for(i=0;i<len;i++){
                CommonGuiMethods.highlightRoi(impl, Rois.get(i), Roi.getColor());
            }
        }
        public void clear(){
            Rois.clear();
        }
}
