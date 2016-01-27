/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class RegionBorderNode {
    int nodeIndex,complexIndex,regionIndex;
    public Point location;
    ArrayList<RegionBorderNode> neighboringNodes;
    ArrayList<Integer> neighboringRegions;
    ArrayList<Integer> segmentIndexes;

    public RegionBorderNode(){
        neighboringNodes=new ArrayList();
        neighboringRegions=new ArrayList();
        segmentIndexes=new ArrayList();
        complexIndex=-1;
        regionIndex=-1;
    }
}
