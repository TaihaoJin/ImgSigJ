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
import java.awt.Rectangle;

/**
 *
 * @author Taihao
 */
public class HighlightingRoiCollectionNode {
        String sID;
        ImagePlus impl;
        ArrayList<Roi> Rois;
        ArrayList<Color> colors;
        boolean visible;
        public HighlightingRoiCollectionNode(ImagePlus impl, String sID){
            this.impl=impl;
            this.sID=sID;
            Rois=new ArrayList();
            colors=new ArrayList();
            visible=true;
        }
        public void addRoi(Roi roi, Color c){
            Rois.add(roi);
            colors.add(c);
        }
        public void addRois(ArrayList<Roi> Rois, ArrayList<Color> colors){
            int len=Rois.size(),i;
            for(i=0;i<len;i++){
                addRoi(Rois.get(i),colors.get(i));
            }
        }
/*        public void removeRoi(Roi roi){
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
        }*/
        public void highlight(){
            int len=Rois.size(),i;
            Roi roi;
            for(i=0;i<len;i++){
                roi=Rois.get(i);
                roi.setColor(colors.get(i));
                roi.draw(impl.getWindow().getCanvas().getGraphics());
            }
        }
        public void highlight(ImagePlus impl){
            ImagePlus implt=impl;
            implt=this.impl;
            highlight();
            this.impl=implt;
        }
        public void clear(){
            Rois.clear();
        }
        public boolean matchingID(String sID){
            if(sID.endsWith("*")){
                int len=sID.length();
                sID=sID.substring(0,len-2);
                return this.sID.startsWith(sID);
            }
            return this.sID.contentEquals(sID);
        }
        public void extendID(String sExt){
            sID+=sExt;
        }
        void setVisible(boolean visible){
            this.visible=visible;
        }
        public boolean isVisible(){
            return visible;
        }
}
