/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import java.awt.Point;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.HypothesisTester;
import utilities.statistics.MeanSem1;


/**
 *
 * @author Taihao
 */
public class RegionComplexNode {
    public int complexIndex;
    public ArrayList<RegionNode> m_cvEnclosedRegions;
    public ArrayList<RegionBorderSegmentNode> m_cvInnerBorderSegments;//these are the RegionBorderSegmentNodes
    //meet the removal criteria. The complex comformed due to the removal of these border segments.
    public ArrayList<RegionBorderSegmentNode> m_cvBorderSegments;
    public ArrayList<RegionBorderSegmentNode> m_cvIntraComplexBorderSegments;//these are the RegionBorderSegmentNodes
    //that do not meet the removal criteria. The complex comformed due to the removal of m_cvInnerBorderSegments. These border
    //segments have been removed because they are not partitioning complexes due to the removal of their connecting border segmengs
    public ArrayList<RegionComplexBoundaryNode> m_cvComplexBoundaries;//m_cvComplexBoundaries.get(0) is the outer boundary
    public ArrayList<Point> m_cvBoundaryPoints;//the points on the complex boundary where the complex indexes are the same of this complex.
    //the complex indexes of the boundary points are assigned as the complex index of the one (among the complexes partitioned by the boundary) with higher main peak pixel value.
    public double base;
    public boolean fitted;
    public ImageShape cIS;
    public FittingModelNode fittingModel,previousModel;
    public MeanSem1 meanSem;
    int area;
    public intRange cXRange,cYRange;
    public double height,peak,sum;
    public Point peakLocation;
    public ArrayList<RegionNode> cvInnerRegions;
    public RegionNode outerRegion;//a region (if exist) that completely encloses this complex
    public RegionComplexNode outerComplex;
    public RegionComplexNode(){
        m_cvEnclosedRegions=new ArrayList();
        m_cvInnerBorderSegments=new ArrayList();
        m_cvBorderSegments=new ArrayList();
        m_cvIntraComplexBorderSegments=new ArrayList();
        m_cvComplexBoundaries=new ArrayList();
        m_cvBoundaryPoints=new ArrayList();
        base=-1;
        area=0;
        fitted=false;
        fittingModel=null;
        cXRange=new intRange();
        cYRange=new intRange();
        meanSem=null;
        previousModel=null;
        cvInnerRegions=null;
        outerComplex=null;
        outerRegion=null;
    }
    public double getTTest(double mean, double sd){
        int n=meanSem.n;
        if(n>=3){
//        if(false){
                return HypothesisTester.tTest(mean,meanSem);
        }else{
            double p=GaussianDistribution.Phi(meanSem.getMean(), mean, sd/Math.sqrt(n));
//            double p=GaussianDistribution.Phi(meanSem.getMean(), mean, sd);
            p=1.-p;
            return p;
        }        
    }
    public void markRegionsAsBackground(boolean background){
        for(int i=0;i<m_cvEnclosedRegions.size();i++){
            m_cvEnclosedRegions.get(i).background=background;
        }
    }
}
