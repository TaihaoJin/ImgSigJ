/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class RegionComplexBoundaryNode {
    public ArrayList<RegionBorderSegmentNode> m_cvRBSNodes;
    RegionComplexBoundaryNode(ArrayList<RegionBorderSegmentNode> rbsNodes){
        m_cvRBSNodes=rbsNodes;
    }
}
