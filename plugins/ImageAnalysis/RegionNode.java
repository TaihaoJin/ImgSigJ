/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import java.awt.Point;
import utilities.statistics.MeanSem1;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.HypothesisTester;
import utilities.CustomDataTypes.DoublePair;
import utilities.statistics.GaussianDistribution;

/**
 *
 * @author Taihao
 */
public class RegionNode {
    public boolean background;
    public Point peakLocation;
    public MeanSem1 meanSem;
    public int regionIndex;
    public int complexIndex;
    public int area;
    public double peak;
    public double height;
    public double base;
    public double sum;
    public intRange cXRange,cYRange;
    public boolean significant,fitted;
    public FittingModelNode fittingModel,previousModel;
    public ArrayList<RegionBorderSegmentNode> boundarySegments;
    public ArrayList<Point> m_cvInnerPoints;
    public ArrayList<Point> m_cvBoundaryPoints;//the points on the complex boundary where the region indexes are the same of this region.
    //the region indexes of the boundary points are assigned as the complex index of the one (among the regions partitioned by the boundary) with higher main peak pixel value.
    public ArrayList<RegionNode> neighbors;
    public ImageShape cIS;
    public RegionNode outerRegion;//not implemented yet 11831
    public RegionComplexNode outerComplex;//not implemented yet 11831
    public ArrayList<RegionNode> innerRegions;//not implemented yet 11831
    public ArrayList<RegionComplexBoundaryNode> m_cvRegionComplexBoundaries;//m_cvComplexBoundaries.get(0) is the outer boundary
    public RegionNode(){
        cIS=null;
        meanSem=null;
        peakLocation=new Point();
        boundarySegments=new ArrayList();
        neighbors=new ArrayList();
        m_cvBoundaryPoints=new ArrayList();
        complexIndex=-1;
        peak=-1;
        height=-1;
        significant=false;
        base=Double.POSITIVE_INFINITY;
        fitted=false;
        fittingModel=null;
        cXRange=new intRange();
        cYRange=new intRange();
        previousModel=null;
        outerRegion=null;
        outerComplex=null;
        m_cvRegionComplexBoundaries=new ArrayList();
        m_cvInnerPoints=new ArrayList();
        background=false;
    }
    public int getSize(){
        return (int)meanSem.getN();
    }
    public double getT(RegionNode rNode){
        MeanSem1 ms1=rNode.meanSem;
        if(getSize()>=3){
            if(rNode.getSize()>=3){
                return HypothesisTester.tTest(meanSem, ms1);
            }else
                return HypothesisTester.tTest(ms1.mean, meanSem);
        }else{
            if(rNode.getSize()>=3){
                return HypothesisTester.tTest(meanSem.mean, ms1);
            }else
                return 1.1;
        }
    }
    public double getT(double mean, double sd){
        int n=getSize();
            if(peakLocation.y==79&&peakLocation.x==209){
                n=n;
            }
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
}
