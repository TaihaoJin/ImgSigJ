/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import utilities.LabelNode;
import ij.ImagePlus;
import java.util.ArrayList;
import BrainSimulations.BrainSimulationGraphicalObject;
import utilities.CommonMethods;
import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Taihao
 */
public class LabelHandler {
    ArrayList <LabelNode> labels;
    public LabelHandler(){
        labels=new ArrayList<LabelNode>();
    }
    public static void arrangeLabels(ArrayList <LabelNode> labels0, int w, int h){
        int size=labels0.size();
        int i;
        sortLabels(labels0);
        avoidOverlapping(labels0);
        for(i=0;i<size;i++){
            intoImage(labels0.get(i), w, h);
        }
    }
    public static void intoImage(LabelNode label, int w, int h){
        if(label.stringWidth>w||label.stringHeight>h){
            label.overSized=true;
        }else{
            label.minShift[0]=Math.max(label.stringHeight, label.corner[0])-label.corner[0];
            label.minShift[1]=Math.min(w-label.stringWidth,label.corner[1])-label.corner[1];
        }
    }
    public static void sortLabels(ArrayList <LabelNode> labels0){
        QuickSortLabelNodes qs=new QuickSortLabelNodes();
        qs.quicksort(labels0);
    }
    public static void avoidOverlapping(ArrayList <LabelNode> labels0){
        int i, index, size=labels0.size();
        LabelNode a,b;
        int margin=10;
        for(i=1;i<size;i++){
            a=labels0.get(i);
            if(a.corner[1]==292){
                i=i;;
            }
            index=i-1;
            b=labels0.get(index);
            while(!a.overLapped(b)&&a.yOverlapped(b)&&index>0){
                index--;
                b=labels0.get(index);
            }
            if(a.overLapped(b)){
                a.shift[0]=b.corner[0]+b.shift[0]-a.corner[0]+margin+a.stringHeight;
            }
        }
    }
    public static ArrayList<LabelNode> makeLabels(ImagePlus impl, ArrayList <BrainSimulationGraphicalObject> GOs){
        ArrayList<LabelNode> labels0=new ArrayList<LabelNode>();
        int size=GOs.size();
        int anchor[]=new int[2];
        int i;
        int l=size/5;
        Font font=new Font("Arial", Font.BOLD, 24);
        Color color=CommonMethods.randomColor();
        BrainSimulationGraphicalObject Go;
        for(i=0;i<size;i++){
            Go=GOs.get(i);
            if(Go.isHiden()) continue;
            if(Go.getArea()<400) continue;
            color=CommonMethods.randomColor();
            LabelNode a=new LabelNode(impl, Go, font, color);
            labels0.add(a);
        }
        return labels0;
    }    
}
