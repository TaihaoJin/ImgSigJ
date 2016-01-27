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
public class SliceIPOGaussianNode {
    public int slice,nNumIPOs;
    public double RegionHCutoff,TotalSignalCutoff;
    ArrayList<IPOGaussianNode> IPOs;
    public SliceIPOGaussianNode(){
        RegionHCutoff=-1;
        TotalSignalCutoff=-1;
        IPOs=new ArrayList();
    }
    public SliceIPOGaussianNode(ArrayList<IPOGaussianNode> IPOs,int slice){
        this.IPOs=IPOs;
        this.slice=slice;
        nNumIPOs=IPOs.size();
        int i,len=IPOs.size();
        for(i=0;i<len;i++){
            IPOs.get(i).sliceIndex=slice;
        }
    }
}
