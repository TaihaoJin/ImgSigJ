/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class StackIPOGaussianNode {
    int nSlices,sliceI,sliceF;
    ArrayList<SliceIPOGaussianNode> SliceIPOs;
    public StackIPOGaussianNode(){
        SliceIPOs=new ArrayList();
    }
    public void setSliceIPOs(ArrayList<SliceIPOGaussianNode> SliceIPOs){
        this.SliceIPOs=SliceIPOs;
        nSlices=SliceIPOs.size();
        sliceI=SliceIPOs.get(0).slice;
        sliceF=SliceIPOs.get(nSlices-1).slice;
    }
    ArrayList<IPOGaussianNode> getIPOGs(int slice){
        if(slice<sliceI||slice>sliceF) return null;
        return SliceIPOs.get(slice-sliceI).IPOs;
    }
    public int getFirstSlice(){
        return sliceI;
    }
    public int getLastSlice(){
        return sliceF;
    }
}
