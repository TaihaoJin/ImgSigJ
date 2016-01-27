/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ij.ImagePlus;
import java.util.ArrayList;
import ij.gui.Roi;

/**
 *
 * @author Taihao
 */
public class ImagePlusExt extends ImagePlus{
    ArrayList <Roi> ROIs;
    public ImagePlusExt(){
        ROIs=new ArrayList <Roi>();
    }
    public ImagePlusExt(ArrayList <Roi> ROIs){
        this();
        int size=ROIs.size();
        for(int i=0;i<size;i++){
            Roi ROI=(Roi)ROIs.get(i).clone();
            ROIs.add(ROI);
        }
    }
    public void clearROIs(){
        ROIs.clear();
    }
    public void addROI(Roi ROI){
        ROIs.add((Roi)ROI.clone());
    }
    public ArrayList <Roi> getROIs(){
        ArrayList <Roi> ROIs=new ArrayList <Roi>();
        int size=this.ROIs.size();
        for(int i=0;i<size;i++){
            ROIs.add((Roi)this.ROIs.get(i).clone());
        }
        return ROIs;
    }
}
