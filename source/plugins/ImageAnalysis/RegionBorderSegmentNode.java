/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import ImageAnalysis.RegionBorderNode;

/**
 *
 * @author Taihao
 */
public class RegionBorderSegmentNode {
    public ArrayList<RegionBorderNode> BoundaryNodes;
    public int region1, region2;
    public int complex1, complex2;
    public double[] hRatios;//the ratios of the segment height and the heights of the region it partitions.
    int segIndex,complexIndex;
    double height;
    public RegionBorderSegmentNode(){
        BoundaryNodes=new ArrayList();
        complexIndex=-1;
        complex1=-1;
        complex2=-1;
        hRatios=new double[2];
        height=0;
    }
}
