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
public class ComplexRegionNode extends RegionNode{//this is the type of regions that contain inner regions
    ArrayList<RegionComplexNode> innerComplexes;
    ArrayList<RegionNode> innerRegions;
    public ArrayList<RegionComplexBoundaryNode> m_cvComplexBoundaries;//m_cvComplexBoundaries.get(0) is the outer boundary
    public ComplexRegionNode(){
        innerComplexes=null;
        innerRegions=null;
        m_cvComplexBoundaries=new ArrayList();
    }
}
