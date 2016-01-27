/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import java.awt.Point;
import ImageAnalysis.RegionBorderNode;
import ImageAnalysis.RegionBorderSegmentNode;
import utilities.CommonStatisticsMethods;
import utilities.CommonMethods;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import ImageAnalysis.NeighboringPositionTraveler;
import utilities.CustomDataTypes.IntPair;
import ij.ImagePlus;
import ij.IJ;
import utilities.statistics.Histogram;
import utilities.statistics.HistogramHandler;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.CustomDataTypes.intRange;
import utilities.io.PrintAssist;
import java.util.Formatter;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.MeanSem1;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import ImageAnalysis.ComplexRegionNode;
import utilities.CustomDataTypes.DoublePair;

/**
 *
 * @author Taihao
 */
public class RegionBoundaryAnalyzer {
    static final int[] nextDirect={1,0,0,1};
    static public final int left=1,right=2;
    ArrayList<RegionBorderSegmentNode> m_cvBorderSegments;
    int h,w,m_nNumRegions,m_nNumBorderNodes,m_nNumBorderSegments;
    int[] m_pnSegmentIndexes;//store the indexes to retrieve the boundary segment from m_cvBorderSegments
    int[][] m_pnStamp;
    int[][] m_pnNodeIndexes,m_pnScratch;
    ArrayList<RegionBorderNode> m_cvBorderNodes;
    ArrayList<RegionNode> m_cvRegionNodes;
    ArrayList<RegionNode> m_cvComplexRegionNodes;
    ArrayList<RegionNode> m_cvSignificanRegionNodes;
    ArrayList<RegionComplexNode> m_cvComplexNodes;
    Histogram m_cRegionSizeHist,m_cRegionSumHist,m_cRegionHeightHist,m_cBoundarySegmentHeightHist,m_cBorderSegmentHRatioHist;
    public int[][] pixels;
    
    public RegionBoundaryAnalyzer(int[][] stamp){
        m_pnStamp=stamp;
        h=stamp.length;
        w=stamp[0].length;
//        implc=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w, h);//for debuging
//        implc.show();
        m_cvBorderNodes=new ArrayList();
        m_cvBorderSegments=new ArrayList();
        m_cvRegionNodes=new ArrayList();
        m_pnNodeIndexes=new int[h][w];
        buildRegionBorderNodes();
        buildBorderSegments();

        m_nNumRegions=m_cvRegionNodes.size();
        m_nNumBorderNodes=m_cvBorderNodes.size();
        m_nNumBorderSegments=m_cvBorderSegments.size();

        registerBorderSegments();
        sortRegionBorderSegmentsNew();
        m_cvComplexNodes=new ArrayList();
    }
    public void setPixel(int[][] pixels){
        this.pixels=pixels;
    }
    void buildRegionBorderNodes(){
        int i,j,type,region,regionhx=0,index,k;
        CommonStatisticsMethods.setElements(m_pnNodeIndexes, -1);
        RegionNode aRegionNode;

        index=0;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                type=LandscapeAnalyzerPixelSorting.getLandscapeType(m_pnStamp[i][j]);
                region=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[i][j]);
                if(region>regionhx) {
                    for(k=regionhx;k<region;k++){
                        m_cvRegionNodes.add(null);
                    }
                    regionhx=region;
                }
                if(type==LandscapeAnalyzerPixelSorting.watershed||type==LandscapeAnalyzerPixelSorting.watershedLM){
                    m_cvBorderNodes.add(buildBorderNode(j,i,index));
                    m_pnNodeIndexes[i][j]=index;
                    index++;
                }else{//a regular point or a local maxima
                    aRegionNode=m_cvRegionNodes.get(region-1);
                    if(aRegionNode==null){
                        aRegionNode=new RegionNode();
                        aRegionNode.regionIndex=region;
                        m_cvRegionNodes.set(region-1, aRegionNode);
                    }
                    aRegionNode.area++;
                    if(type==LandscapeAnalyzerPixelSorting.localMaximum){
                        aRegionNode.peakLocation=new Point(j,i);
                    }
                }
            }
        }
        m_nNumRegions=regionhx;
        m_nNumBorderNodes=m_cvBorderNodes.size();

        for(index=0;index<m_nNumBorderNodes;index++){
            completeBorderNode(m_pnNodeIndexes,w,h,index);
        }
    }
    boolean onEdge(RegionBorderNode aNode){
        return(aNode.neighboringNodes.size()==1);
    }
    boolean isSegmentTerminal(RegionBorderNode aNode){
        return(aNode.neighboringNodes.size()!=2);//size 1: on edge; size 3 or 4 intersection points of segments
    }
    int completeBorderNode(int[][] pnNodeIndexes, int w, int h, int indexo){
        RegionBorderNode aNode=m_cvBorderNodes.get(indexo),bNode;
        Point po=aNode.location,p;
        int xo=aNode.location.x,yo=aNode.location.y,x,y,index,i;
        for(i=0;i<2;i++){//only visit the two next direct,
            //right side point and lower side point (1,0) and (0,1)
            x=xo+nextDirect[i*2];
            if(x<0||x>=w) continue;
            y=yo+nextDirect[i*2+1];
            if(y<0||y>=h) continue;
            index=pnNodeIndexes[y][x];
            if(index>=0){
                bNode=m_cvBorderNodes.get(index);
                aNode.neighboringNodes.add(bNode);
                bNode.neighboringNodes.add(aNode);
            }
        }
        //found all neighboring boundary nodes.
        int num=aNode.neighboringNodes.size();
        int step=0;
        if(num==1){
            aNode.neighboringRegions=LandscapeAnalyzerPixelSorting.getNeighboringRegions(m_pnStamp, xo, yo);
        }else{
            for(i=0;i<num;i++){
                step=0;
                bNode=aNode.neighboringNodes.get(i);
                p=bNode.location;
                p=NeighboringPositionTraveler.nextPosition(p.x-po.x, p.y-po.y, 1);
                p.translate(po.x, po.y);
                while(pnNodeIndexes[p.y][p.x]>=0){
                    p=NeighboringPositionTraveler.nextPosition(p.x-po.x, p.y-po.y, 1);
                    p.translate(po.x, po.y);
                    step++;
                }
                if(step>=2){
                    continue;
                }
                aNode.neighboringRegions.add(LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[p.y][p.x]));
            }
        }
        int len=aNode.neighboringRegions.size();
        int rn=Integer.MAX_VALUE;
        for(i=0;i<len;i++){
            index=aNode.neighboringRegions.get(i);
            if(index<rn) rn=index;
        }
        aNode.regionIndex=rn;//the region with lower regin index has higher peak pixel value
        return 1;
    }

    RegionBorderNode buildBorderNode(int x, int y, int nodeIndex){
        RegionBorderNode aNode=new RegionBorderNode();
        aNode.location=new Point(x,y);
        aNode.nodeIndex=nodeIndex;
        return aNode;
    }
    void buildBorderSegments(){
        IntPair regionPair;
        ArrayList<IntPair> regionPairs;
        RegionBorderNode aNode,bNode;
        RegionBorderSegmentNode aSegNode;
        int len=m_cvBorderNodes.size();
        int i,j,index,nbhrs,region1,region2;
        ArrayList<Integer> CommonRegions;
        for(i=0;i<len;i++){
            aNode=m_cvBorderNodes.get(i);
            Point p=aNode.location;
            if(!isSegmentTerminal(aNode)) continue;
            nbhrs=aNode.neighboringNodes.size();
            for(j=0;j<nbhrs;j++){
                bNode=aNode.neighboringNodes.get(j);
                CommonRegions=CommonMethods.getCommonElements(aNode.neighboringRegions, bNode.neighboringRegions);
                if(CommonRegions.size()!=2) continue;
                region1=CommonRegions.get(0);
                region2=CommonRegions.get(1);

                if(BorderSegmentExists(aNode,bNode)) continue;
                aSegNode=buildBorderSegment(aNode,bNode,region1,region2);
                adjustRegionIndexes(aSegNode);
                m_cvBorderSegments.add(aSegNode);
            }
        }
        buildCircularBorderSegments();
    }
    void buildCircularBorderSegments(){
        int i,len=m_cvBorderNodes.size();
        RegionBorderNode rbNode;
        for(i=0;i<len;i++){
            rbNode=m_cvBorderNodes.get(i);
            if(rbNode.segmentIndexes.isEmpty()) buildCircularBorderSegment(rbNode);
        }
    }

    int buildCircularBorderSegment(RegionBorderNode aNode){
        if(aNode.neighboringRegions.size()!=2){
            IJ.error("aNode is not a node in a circular border segment");
            return 1;
        }

        int region1=aNode.neighboringRegions.get(0);
        int region2=aNode.neighboringRegions.get(1);
        RegionBorderNode bNode=aNode.neighboringNodes.get(0);

        if(bNode.neighboringRegions.size()!=2||bNode.segmentIndexes.size()!=0){
            IJ.error("bNode is not a node in a circular border segment");
            return 1;
        }

        ArrayList<Integer> CommonRegions=CommonMethods.getCommonElements(aNode.neighboringRegions, bNode.neighboringRegions);
        if(CommonRegions.size()!=2||bNode.segmentIndexes.size()!=0){
            IJ.error("aNode and bNode are not a node in a circular border segment");
            return 1;
        }

        RegionBorderNode aNode0;
        int segIndex=m_cvBorderSegments.size();
        aNode.segmentIndexes.add(segIndex);
        RegionBorderSegmentNode aSegNode=new RegionBorderSegmentNode();
        aSegNode.region1=region1;
        aSegNode.region2=region2;
        ArrayList<RegionBorderNode> rbnodes=aSegNode.BoundaryNodes;
        rbnodes.add(aNode);
        aSegNode.segIndex=segIndex;

        bNode.segmentIndexes.add(segIndex);
        rbnodes.add(bNode);
        RegionBorderNode aNodeo=aNode;
        while(bNode!=aNodeo){
            aNode0=aNode;
            aNode=bNode;
            bNode=nexBorderNode(aNode0,aNode,region1,region2);
            bNode.segmentIndexes.add(segIndex);
            rbnodes.add(bNode);
        }
        m_cvBorderSegments.add(aSegNode);
        return 1;
    }
    boolean BorderSegmentExists(RegionBorderNode aNode, RegionBorderNode bNode){
        ArrayList<Integer> segIndexes=aNode.segmentIndexes;
        int len=segIndexes.size(),i,index,j,len1;
        RegionBorderSegmentNode segNode;
        ArrayList<RegionBorderNode> rbNodes;
        for(i=0;i<len;i++){
            index=segIndexes.get(i);
            segNode=m_cvBorderSegments.get(index);
            rbNodes=segNode.BoundaryNodes;
            len1=rbNodes.size();
            if(len1<2) continue;
            if(rbNodes.get(0)==aNode&&rbNodes.get(1)==bNode) return true;
            if(rbNodes.get(len1-1)==aNode&&rbNodes.get(len1-2)==bNode) return true;
        }
        return false;
    }
    boolean BorderSegmentExists(RegionBorderNode aNode, int region1, int region2){//this method has exception in some cases. Retiring.
        ArrayList<Integer> segIndexes=aNode.segmentIndexes;
        int len=segIndexes.size(),i,index;
        for(i=0;i<len;i++){
            index=segIndexes.get(i);
            if(PartitioningBorderSegment(m_cvBorderSegments.get(index),region1,region2)) return true;
        }
        return false;
    }
    boolean PartitioningBorderSegment(RegionBorderSegmentNode aNode, int region1, int region2){
         int r1=aNode.region1,r2=aNode.region2;
        return CommonMethods.equivalentPairs(r1, r2, region1,region2);
    }
    int adjustRegionIndexes(RegionBorderSegmentNode segNode){//make region1 in the left side of the boundary
        if(segNode.BoundaryNodes.size()==1) return -1;
        if(regionSideness(segNode,segNode.region1,segNode.region2)!=left){
            int it=segNode.region1;
            segNode.region1=segNode.region2;
            segNode.region2=it;
        }
        return 1;
    }
    RegionBorderSegmentNode SingleNodeBorderSegment(RegionBorderNode aNode, int region1, int region2){
        int segIndex=m_cvBorderSegments.size();
        aNode.segmentIndexes.add(segIndex);
        RegionBorderSegmentNode aSegNode=new RegionBorderSegmentNode();
        aSegNode.region1=region1;
        aSegNode.region2=region2;
        aSegNode.BoundaryNodes.add(aNode);
        aSegNode.segIndex=segIndex;
        return aSegNode;
    }
    RegionBorderSegmentNode buildBorderSegment(RegionBorderNode aNode, RegionBorderNode bNode,int region1, int region2){
        RegionBorderNode aNode0;
        int segIndex=m_cvBorderSegments.size();
        aNode.segmentIndexes.add(segIndex);
        RegionBorderSegmentNode aSegNode=new RegionBorderSegmentNode();
        aSegNode.region1=region1;
        aSegNode.region2=region2;
        ArrayList<RegionBorderNode> rbnodes=aSegNode.BoundaryNodes;
        rbnodes.add(aNode);
        aSegNode.segIndex=segIndex;

        bNode.segmentIndexes.add(segIndex);
        rbnodes.add(bNode);
        while(!isSegmentTerminal(bNode)){
            aNode0=aNode;
            aNode=bNode;
            bNode=nexBorderNode(aNode0,aNode,region1,region2);
            bNode.segmentIndexes.add(segIndex);
            rbnodes.add(bNode);
        }
        return aSegNode;
    }
    RegionBorderNode nexBorderNode(RegionBorderNode aNode,int region1,int region2){//aNode is a terminal node
        RegionBorderNode bNode=null;
        ArrayList<RegionBorderNode> neighbors=aNode.neighboringNodes;
        int len=neighbors.size();
        int i;
        for(i=0;i<len;i++){
            bNode=neighbors.get(i);
            if(PartitioningBorderNode(bNode,region1,region2)) {
                return bNode;
            }        
        }
        return null;
    }
    RegionBorderNode nexBorderNode(RegionBorderNode previousNode,RegionBorderNode currentNode,int region1,int region2){//aNode is a terminal node
        RegionBorderNode bNode=null;
        ArrayList<RegionBorderNode> neighbors=currentNode.neighboringNodes;
        int len=neighbors.size();
        int i;
        for(i=0;i<len;i++){
            bNode=neighbors.get(i);
            if(!PartitioningBorderNode(bNode,region1,region2)) continue;
            if(bNode!=previousNode) return bNode;
        }
        return null;
    }
    boolean PartitioningBorderNode(RegionBorderNode aNode, int region1, int region2){
        /*
        if(!CommonMethods.containsContent(aNode.neighboringRegions, region1)) return false;
        if(!CommonMethods.containsContent(aNode.neighboringRegions, region2)) return false;
         *
         */
        ArrayList<Integer> rs=new ArrayList();
        rs.add(region1);
        rs.add(region2);
        ArrayList<Integer> nbrs=aNode.neighboringRegions;
        int len=nbrs.size(),lent,i,j,region,regiont;
        for(i=0;i<len;i++){
            region=nbrs.get(i);
            lent=rs.size();
            for(j=0;j<lent;j++){
                regiont=rs.get(j);
                if(regiont==region){
                    rs.remove(j);
                    break;
                }
            }
        }
        return(rs.size()==0);
    }
    void registerBorderSegments(){
        int len=m_cvBorderSegments.size(),i,j,region1,region2,sign;
        RegionNode rNode1, rNode2;
        RegionBorderSegmentNode rbsNode;
        for(i=0;i<len;i++){
            sign=1;
            rbsNode=m_cvBorderSegments.get(i);
            region1=rbsNode.region1;
            region2=rbsNode.region2;
            if(i==len-1){
                i=i;
            }
            rNode1=m_cvRegionNodes.get(region1-1);
            rNode2=m_cvRegionNodes.get(region2-1);
            rNode1.boundarySegments.add(rbsNode);
            rNode2.boundarySegments.add(rbsNode);
         }
    }

    int regionSideness(RegionBorderSegmentNode segNode, int region1, int region2){//sideness of region1
        Point p0=segNode.BoundaryNodes.get(0).location;
        Point p1=segNode.BoundaryNodes.get(1).location;
        Point rp=getRightSideRegionPoint(p0,p1);
        int regionIndex,sideness;
        while(true){
            regionIndex=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[rp.y][rp.x]);
            if(regionIndex==region1){
                sideness=right;
                break;
            }
            else if(regionIndex==region2){
                sideness=left;
                break;
            }
            rp=getRightSideRegionPoint(p0,rp);
        }
        return sideness;
    }

    int getRightSideRegionIndex(Point p0, Point p1){
        Point rp=getRightSideRegionPoint(p0,p1);
        int regionIndex=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[rp.y][rp.x]);
        return regionIndex;
    }
    Point getRightSideRegionPoint(Point p0, Point p1){
        int dx=p1.x-p0.x,dy=p1.y-p0.y;
        Point rp=NeighboringPositionTraveler.nextPosition(dx, dy, 1);//right side point of the boundary
        rp.translate(p0.x, p0.y);
        while(m_pnNodeIndexes[rp.y][rp.x]>=0){
            rp=NeighboringPositionTraveler.nextPosition(rp.x-p0.x, rp.y-p0.y, 1);
            rp.translate(p0.x, p0.y);
        }
        return rp;
    }
    public static int pixelOfNode(RegionBorderNode aNode, int index){
        ArrayList<Integer> nbhrs=aNode.neighboringRegions;
        int len=nbhrs.size(),region,pixel=0,r=0,g=0,b=0;
        if(len>=1) r=nbhrs.get(0)%255;
        if(len>=2) g=nbhrs.get(1)%255;
        if(len>=3) b=nbhrs.get(2)%255;
        if(len>=4) g=((nbhrs.get(1)+nbhrs.get(3))/2);
        b=(index+b)%255;
        pixel=(r<<16)|(g<<8)|b;
        return pixel;
    }
    public void drawRegionBoundaries(ImagePlus impl, Point[] points){
        int i,index,len=points.length;
        Point pt;
        for(i=0;i<len;i++){
            pt=points[i];
            index=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[pt.y][pt.x])-1;
            drawRegionBoundary(impl,m_cvRegionNodes.get(index));
        }
    }
    public void drawRegionBoundaries_Random(ImagePlus impl, int n){
        int i,index,len=m_cvBorderSegments.size();
        for(i=0;i<n;i++){
            index=(int)(Math.random()*n);
            drawRegionBoundary(impl,m_cvRegionNodes.get(i));
        }
    }
    public void drawRegionBoundary(ImagePlus impl, RegionNode rNode){
        ArrayList <RegionBorderNode> rbNodes=getRegionBoundary(rNode);
        drawBoundary(impl,rbNodes);
    }
    /*
    public void drawRegionComplexBoundary(ImagePlus impl, RegionComplexNode cNode){
        ArrayList <RegionBorderNode> rbNodes=getRegionComplexBoundary(cNode);
        drawBoundary(impl,rbNodes);
    }
     * 
     */
    public void drawBoundary(ImagePlus impl, ArrayList <RegionBorderNode> rbNodes){
        int[] pixels=(int[])impl.getProcessor().getPixels();
        RegionBorderNode rbNode;
        int len=rbNodes.size(),pixel;
        int i;
        Point p;
        for(i=0;i<len;i++){
            rbNode=rbNodes.get(i);
            p=rbNode.location;
            pixel=pixelOfNode(rbNode,i);
            pixels[w*p.y+p.x]=pixel;
        }
    }
    ArrayList<RegionBorderNode> getRegionBoundary(RegionNode rNode){
        if(rNode==null||rNode.regionIndex<0) return null;
        return getRegionBoundary(rNode.boundarySegments, rNode.regionIndex);
    }
    ArrayList<RegionBorderNode> getRegionBoundary(ArrayList<RegionBorderSegmentNode> rbsNodes,int region){
        ArrayList<RegionBorderNode> cvBoundary=new ArrayList();

        RegionBorderSegmentNode rbsNode=rbsNodes.get(0);
        RegionBorderNode rbNode=getFirstBorderNode(rbsNode,region);
        Point pt=rbNode.location;
        cvBoundary.add(rbNode);
        int len=rbsNodes.size(),i;
        for(i=0;i<len;i++){
            rbsNode=rbsNodes.get(i);
            getConnectingBorderNodes(rbsNode,pt,region,cvBoundary);
            pt=getLastPoint(rbsNode,region);
        }
        if(region>=m_nNumRegions+1)
            patchRegionComplexBoundary(cvBoundary,region);
        return cvBoundary;
    }
    int patchRegionComplexBoundary(ArrayList<RegionBorderNode> rbNodes, int region){
        int len=rbNodes.size();
        RegionBorderNode rbNode0=rbNodes.get(len-1),rbNode1=rbNodes.get(0),rbNode;
        Point p0=rbNode0.location,p1=rbNode1.location;
        if(!CommonMethods.DiagonallyContacting(p0, p1)) return -1;
        int cIndex=region-m_nNumRegions-1;
        RegionComplexNode cNode=m_cvComplexNodes.get(cIndex);
        rbNode=getConnectionRBNode(cNode.m_cvInnerBorderSegments,p0,p1);
        if(rbNode==null){
            rbNode=getConnectionRBNode(cNode.m_cvIntraComplexBorderSegments,p0,p1);
        }
        if(rbNode!=null){
            rbNodes.add(rbNode);
        }else{
            IJ.error("Could not find connection region border node, getConnectionRBNode");
        }
        return 1;
    }
    int getSegmentDirection(RegionBorderSegmentNode rbsNode, int regionId){
        int id1,id2;
        if(regionId>m_cvRegionNodes.size()){
            id1=rbsNode.complex1;
            id2=rbsNode.complex2;
        }else{
            id1=rbsNode.region1;
            id2=rbsNode.region2;
        }
        if(id1==regionId) return -1;
        if(id2==regionId) return 1;
        IJ.error("inconsistent regionId for getSegmentDirection");
        return 1;
    }
    public void getConnectingBorderNodes(RegionBorderSegmentNode rbsNode, Point p0, int region, ArrayList<RegionBorderNode> rbNodes0){
        Point pt=getFirstPoint(rbsNode,region);
        RegionBorderNode rbNode;
        ArrayList<RegionBorderNode> rbNodes=rbsNode.BoundaryNodes;
        int delta=1,shift=0;
/*        if(pt==rbNodes.get(0).location)//
            delta=1;
        else
            delta=-1;
*/
        delta=getSegmentDirection(rbsNode,region);
        if(CommonMethods.DirectContacting(p0, pt))
            shift=0;
        else if(CommonMethods.Overlapping(p0, pt))
            shift=1;
        else if(ConnectingByImageEdge(p0,pt,region))
            shift=0;
        else if(CommonMethods.DiagonallyContacting(p0, pt)){//complexBoundary
                shift=0;
                int cIndex=region-m_nNumRegions-1;
                RegionComplexNode cNode=m_cvComplexNodes.get(cIndex);
                rbNode=getConnectionRBNode(cNode.m_cvInnerBorderSegments,p0,pt);
                if(rbNode==null){
                    rbNode=getConnectionRBNode(cNode.m_cvIntraComplexBorderSegments,p0,pt);
                }
                if(rbNode!=null){
                    rbNodes0.add(rbNode);
                }else{
                    IJ.error("Could not find connection region border node, getConnectionRBNode");
                }
        }else
            IJ.error("the boundary segment is not connecting with the point in the method getConnectingBoundaryNodes");

        int len=rbNodes.size(),i;
        int index=shift;
        if(delta==-1) index=len-1-shift;

        for(i=0;i<len-shift;i++){
            rbNodes0.add(rbNodes.get(index));
            index+=delta;
        }
    }
    RegionBorderNode getConnectionRBNode(ArrayList<RegionBorderSegmentNode> rbsNodes,Point p0, Point p1){
        //this method finds and return a RegionBorderNode whose location directly contracts with both p0
        //and p1. it searches the node among the first and the last node of rbsNode in rbsNodes.
        int i,len=rbsNodes.size(),len1;
        RegionBorderSegmentNode rbsNode;
        RegionBorderNode rbNode;
        ArrayList<RegionBorderNode> rbNodes;
        Point pt;
        for(i=0;i<len;i++){
            rbsNode=rbsNodes.get(i);
            rbNodes=rbsNode.BoundaryNodes;
            len1=rbNodes.size();
            rbNode=rbNodes.get(0);
            pt=rbNode.location;
            if(CommonMethods.DirectContacting(pt, p0)&&CommonMethods.DirectContacting(pt, p1)) return rbNode;
            rbNode=rbNodes.get(len1-1);
            pt=rbNode.location;
            if(CommonMethods.DirectContacting(pt, p0)&&CommonMethods.DirectContacting(pt, p1)) return rbNode;
        }
        
        return null;
    }
    int sortBorderSegmentsNew(ArrayList<RegionBorderSegmentNode> rbsNodes, int region, Point pf, int iI){//sort connected boundary segments
        //starting from iI and return the position of last segment that can find the following connected
        //boundary segment.
        int i,len=rbsNodes.size(),iF=iI-1;
        int index=iI;
        ArrayList<Integer> indexes;
        RegionBorderSegmentNode rbsNode0=null;

        Point pi;

        for(i=iI;i<len;i++){
            if(pf.x==116&&pf.y==66){
                pf=pf;
            }
            if(i==10){
                i=i;
            }
            index=-1;
            rbsNode0=rbsNodes.get(i-1);
            if(onEdge(getLastNode(rbsNode0,region))){
                indexes=getImageEdgeBorderSegments(rbsNodes,i,region,1);
                index=getEdgeConnectedSegments(rbsNodes,region,indexes,0,pf);
                if(index>=0) index=indexes.get(index);
            }

            if(rbsNode0.BoundaryNodes.size()>1){
                pi=getSecondLastPoint(rbsNode0,region);
            }else{
                pi=getANeighboringPointInRegion(pf,region);
            }

            if(index<0){
                indexes=getOverlappingBorderSegment(rbsNodes,pf,region,i);
                if(indexes.isEmpty())
                    index=-1;
                else {
                    index=getValidOverlappingConnection(rbsNodes,indexes,pi,pf,region);
                }
            }

            if(index<0){
                indexes=getConnectingBorderSegment(rbsNodes,pf,region,i);
                if(indexes.isEmpty())
                    index=-1;
                else {
                    index=getValidDirectConnection(rbsNodes,indexes,pi,pf,region);
                }
            }
            if(index<0&&region>=m_nNumRegions+1){//sorting the boundary of a region complex
                //the maximum of the regionIndex is m_nNumRegions
                indexes=getDiagonallyConnectingBorderSegment(rbsNodes,pf,region,i);
                if(indexes.isEmpty())
                    index=-1;
                else {
                    index=getValidDiagonalConnection(rbsNodes,indexes,pi,pf,region);
                }
            }
            if(index<0) {//there now contacting connections
                return iF;
            }
            swapElements(rbsNodes,i,index);
            pf=getLastPoint(rbsNodes.get(i),region);
            iF=i;
        }
        return iF;
    }
    Point getANeighboringPointInRegion(Point po,int region){
        Point pt=new Point(-1,0);
        int regiont=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[po.y+pt.y][po.x+pt.x]);
        if(regiont==region&&m_pnNodeIndexes[po.y+pt.y][po.x+pt.x]<0) {
            pt.translate(po.x, po.y);
            return pt;
        }
        pt=NeighboringPositionTraveler.nextPosition(pt.x, pt.y, 1);
        while(pt.x!=-1||pt.y!=0){
            if(m_pnNodeIndexes[po.y+pt.y][po.x+pt.x]>=0) {
                pt=NeighboringPositionTraveler.nextPosition(pt.x, pt.y, 1);
                continue;
            }
            regiont=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[po.y+pt.y][po.x+pt.x]);
            if(region>=m_nNumRegions+1) {
                regiont=m_cvRegionNodes.get(regiont-1).complexIndex+m_nNumRegions+1;
            }
            if(regiont==region) {
                pt.translate(po.x, po.y);
                return pt;
            }
            pt=NeighboringPositionTraveler.nextPosition(pt.x, pt.y, 1);
        }
        return null;
    }/*
    int sortBorderSegments0(ArrayList<RegionBorderSegmentNode> rbsNodes, int region, Point pf, int iI){//sort connected boundary segments
        //starting from iI and return the position of last segment that can find the following connected
        //boundary segment.
        int i,j,it,lenn,len1,len2,len=rbsNodes.size(),iF=iI-1;
        int index=iI;
        ArrayList<Integer> indexes;
        for(i=iI;i<len;i++){
            if(i==27){
                i=i;
            }
            indexes=getOverlappingBorderSegment(rbsNodes,pf,region,i);
            if(indexes.size()==0){
                indexes=getConnectingBorderSegment(rbsNodes,pf,region,i);
            }
            if(indexes.size()==0&&region>=m_nNumRegions+1){
                indexes=getDiagonallyConnectingBorderSegment(rbsNodes,pf,region,i);
            }
            len1=indexes.size();
            if(len1==0){
                return iF;
            }else if(len1==1){
                index=indexes.get(0);
            }else{
                lenn=Integer.MAX_VALUE;
                for(j=0;j<len1;j++){
                    it=indexes.get(j);
                    len2=rbsNodes.get(it).BoundaryNodes.size();
                    if(len2<lenn) {
                        lenn=len2;
                        index=it;
                    }
                }
            }
            swapElements(rbsNodes,i,index);
            pf=getLastPoint(rbsNodes.get(i),region);
            iF=i;
        }
        return iF;
    }*/
    int getValidOverlappingConnection(ArrayList<RegionBorderSegmentNode> rbsNodes,ArrayList<Integer> indexes,Point pi, Point pf, int region){//deed to check
        //The first point of all rbsNode pointed by indexes overlap with pf.
        int index=-1,index0=-1;
        int len=indexes.size(),i;
        RegionBorderSegmentNode rbsNode;
        ArrayList<Point> path;
        Point p1=new Point(pf);
        for(i=0;i<len;i++){
            index=indexes.get(i);
            rbsNode=rbsNodes.get(index);
            if(rbsNode.BoundaryNodes.size()==1) return index;
            p1=getSecondPoint(rbsNode,region);
            path=NeighboringPositionTraveler.getPath(pi.x-pf.x,pi.y-pf.y,p1.x-pf.x,p1.y-pf.y,-1);
            CommonMethods.translatePath(path, pf);
            if(isOnlyRegionOnPath(path,region)) {
                index0=index;
            }
        }
        return index0;
    }
    boolean isValidOverlappingConnection(RegionBorderSegmentNode rbsNode1, RegionBorderSegmentNode rbsNode2, int region){
        if(rbsNode2.BoundaryNodes.size()==1) return true;
        Point pf=getLastPoint(rbsNode1,region);
        Point p1,p2;
        if(rbsNode1.BoundaryNodes.size()==1){
            p1=getANeighboringPointInRegion(pf,region);
        }else{
            p1=getSecondLastPoint(rbsNode1,region);
        }
        p2=getSecondPoint(rbsNode2,region);
        return isValidConnection(p1,pf,p2,region);
    }
    boolean isValidConnection(Point p1, Point pf, Point p2, int region){
        ArrayList<Point> path1=NeighboringPositionTraveler.getPath(p1.x-pf.x,p1.y-pf.y,p2.x-pf.x,p2.y-pf.y,-1);
        CommonMethods.translatePath(path1, pf);
        if(isOnlyRegionOnPath(path1,region)) {
            return true;
        }
        ArrayList<Point> path2=NeighboringPositionTraveler.getPath(p1.x-pf.x,p1.y-pf.y,p2.x-pf.x,p2.y-pf.y,1);
        CommonMethods.translatePath(path2, pf);
        if(isOnlyRegionOnPath(path2,region)) {
            return true;
        }
        return false;
    }
    boolean isValidDirectOrDiagonallyContactingConnection(RegionBorderSegmentNode rbsNode1, RegionBorderSegmentNode rbsNode2, int region){
        if(rbsNode2.BoundaryNodes.size()==1) return true;
        Point pf=getLastPoint(rbsNode1,region);
        Point p1,p2;
        if(rbsNode1.BoundaryNodes.size()==1){
            p1=getANeighboringPointInRegion(pf,region);
        }else{
            p1=getSecondLastPoint(rbsNode1,region);
        }
        p2=getFirstPoint(rbsNode2,region);

        return isValidConnection(p1,pf,p2,region);
    }
    int getValidDirectConnection(ArrayList<RegionBorderSegmentNode> rbsNodes,ArrayList<Integer> indexes,Point pi, Point pf, int region){//deed to check
        //The first point of all rbsNode pointed by indexes directly contacting with pf.
        int index=0;
        int len=indexes.size(),i;
        RegionBorderSegmentNode rbsNode;
        ArrayList<Point> path;
        Point p1=new Point(pf);
        for(i=0;i<len;i++){
            index=indexes.get(i);
            rbsNode=rbsNodes.get(index);
            p1=getFirstPoint(rbsNode,region);
            path=NeighboringPositionTraveler.getPath(pi.x-pf.x,pi.y-pf.y,p1.x-pf.x,p1.y-pf.y,-1);
            CommonMethods.translatePath(path, pf);
            if(isOnlyRegionOnPath(path,region)) return index;
        }
        return -1;
    }
    int getValidDiagonalConnection(ArrayList<RegionBorderSegmentNode> rbsNodes,ArrayList<Integer> indexes,Point pi, Point pf, int region){//deed to check
        //The first point of all rbsNode pointed by indexes directly contacting with pf.
        int index=0;
        int len=indexes.size(),i;
        RegionBorderSegmentNode rbsNode;
        ArrayList<Point> path;
        Point p1=new Point(pf);
        for(i=0;i<len;i++){
            index=indexes.get(i);
            rbsNode=rbsNodes.get(index);
            p1=getFirstPoint(rbsNode,region);
            if(!isDiagonalRegionBorderNodePair(pf,p1)) continue;
            path=NeighboringPositionTraveler.getPath(pi.x-pf.x,pi.y-pf.y,p1.x-pf.x,p1.y-pf.y,-1);
            CommonMethods.translatePath(path, pf);
            if(isOnlyRegionOnPath(path,region)) return index;
        }
        return -1;
    }
    boolean isDiagonalRegionBorderNodePair(Point p1, Point p2){//return trues if the two points are diagonally
        //contacting and they are part of a four region border node complex.
        if(!CommonMethods.DiagonallyContacting(p1, p2)) return false;
        if(m_pnNodeIndexes[p1.y][p2.x]>=0&&m_pnNodeIndexes[p2.y][p1.x]>=0) return true;
        return false;
    }
    boolean isOnlyRegionOnPath(ArrayList<Point> path, int region){
        int i,len=path.size(),regiont;
        RegionNode rNode;
        Point pt;
        for(i=0;i<len;i++){
            pt=path.get(i);
            if(m_pnNodeIndexes[pt.y][pt.x]>=0) continue;
            regiont=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[pt.y][pt.x]);
            rNode=m_cvRegionNodes.get(regiont-1);
            if(region>=m_nNumRegions+1)
                regiont=rNode.complexIndex+m_nNumRegions+1;
            else
                regiont=rNode.regionIndex;
            if(regiont!=region) return false;
        }
        return true;
    }
    ArrayList<Integer> sortBorderSegmentsNew(ArrayList<RegionBorderSegmentNode> rbsNodes, int region){
        ArrayList<Integer> nvSegmentEnds=new ArrayList();//For region complexes with inner complexes, the boundaries
        //are not connected. nvSegmentEnds mark the position of the rbsNodes that could not find other segments to
        //connect with.
        Point pf;
        int len=rbsNodes.size();
        int index;
        ArrayList<Integer> indexes;
        RegionBorderSegmentNode rbsNode;
        indexes=getImageEdgeBorderSegments(rbsNodes,region,1);
        if(indexes.size()>0 ){
            index=indexes.get(0);
        }else{
            index=0;
            if(rbsNodes.isEmpty()) return nvSegmentEnds;
            rbsNode=rbsNodes.get(index);
            if(rbsNode.BoundaryNodes.size()==1) index=1;
        }

        if(index!=0) swapElements(rbsNodes,0,index);
        pf=getLastPoint(rbsNodes.get(0),region);

        int iF=sortBorderSegmentsNew(rbsNodes,region,pf,1);
        
        if(iF==len-1) return nvSegmentEnds;//All rbsNodes are connected
/*
        if(region<=m_nNumRegions) {//boundary of a simple region should have single connectivity. There are exception, so this block is commented out 11829
            IJ.error("Unable to sort all boundary segments, sortBoundarySegments");
            return nvSegmentEnds;
        }*/
        //They are boundary of region complex, so could be disconnected
        int iI=iF+1;
        while(iF<len-1){
            nvSegmentEnds.add(iF);
            iI=iF+1;
            pf=getLastPoint(rbsNodes.get(iI),region);
            if(iI==len-1) {
                iF=iI;
                break;
            }
            if(iI>len-1){
                IJ.error("unexpected case, sortBorderSegmentsNew");
            }
            iF=sortBorderSegmentsNew(rbsNodes,region,pf,iI+1);
        }
        if(!ConnectedRBSegments(rbsNodes.get(iF),rbsNodes.get(0),region)) nvSegmentEnds.add(iF);
        return nvSegmentEnds;
    }
    boolean ConnectedRBSegments(RegionBorderSegmentNode rbsNode1, RegionBorderSegmentNode rbsNode2, int region){
        Point p1=getLastPoint(rbsNode1,region),p2=getFirstPoint(rbsNode2,region);
        if(CommonMethods.Overlapping(p1,p2)){
            if(isValidOverlappingConnection(rbsNode1,rbsNode2,region)) return true;
        }
        if(CommonMethods.DirectContacting(p1, p2)||CommonMethods.DiagonallyContacting(p1, p2)){
            if(isValidDirectOrDiagonallyContactingConnection(rbsNode1,rbsNode2,region)){
                return true;
            }
        }
        return false;
    }
    boolean ConnectingByImageEdge(Point p1, Point p2, int region){
        if(!CommonMethods.isOnEdge(w, h, p1)||!CommonMethods.isOnEdge(w, h, p2))return false;
        ArrayList<Point> path=CommonMethods.getEdgePath(p1, p2, w, h);//the edgepath does not include p1 and p2
        return isOnlyRegionOnPath(path,region);
    }
    int getEdgeConnectedSegments(ArrayList<RegionBorderSegmentNode> rbsNodes, int region, ArrayList<Integer> EdgeSegmentIndexes, int iI, Point pf){
        int len=EdgeSegmentIndexes.size(),i,index;
        for(i=iI;i<len;i++){
            index=EdgeSegmentIndexes.get(i);
            if(ConnectingByImageEdge(pf,getFirstPoint(rbsNodes.get(index),region),region)) return i;
        }
        return -1;
    }
    ArrayList<Integer> getImageEdgeBorderSegments(ArrayList<RegionBorderSegmentNode> rbsNodes, int region, int direction){
        //returns the indexes of the bordersegments starting from the image edges
        return getImageEdgeBorderSegments(rbsNodes,0,region,direction);
    }
    ArrayList<Integer> getImageEdgeBorderSegments(ArrayList<RegionBorderSegmentNode> rbsNodes, int iI, int region, int direction){
        //returns the indexes of the bordersegments starting from the image edges
        int len=rbsNodes.size(),i;
        ArrayList<Integer> indexes=new ArrayList();
        Point pt;
        for(i=iI;i<len;i++){
            if(direction==1){
                pt=getFirstPoint(rbsNodes.get(i),region);
            }else{
                pt=getLastPoint(rbsNodes.get(i),region);
            }
            if(pt.x<=0||pt.x>=w-1){
                indexes.add(i);
            }
            if(pt.y<=0||pt.y>=h-1) {
                indexes.add(i);
            }
        }
        return indexes;
    }
    public static void swapElements(ArrayList<RegionBorderSegmentNode> rbsNodes, int i, int j){
        RegionBorderSegmentNode aNode=rbsNodes.get(i);
        rbsNodes.set(i, rbsNodes.get(j));
        rbsNodes.set(j, aNode);
    }
    public static void swapComplexElements(ArrayList<RegionComplexNode> cNodes, int i, int j){
        RegionComplexNode cNode=cNodes.get(i);
        cNodes.set(i, cNodes.get(j));
        cNodes.set(j, cNode);
    }
    public ArrayList<Integer> getConnectingBorderSegment(ArrayList<RegionBorderSegmentNode> rbsNodes,Point p,int region, int iI){
        int i,len=rbsNodes.size();
        ArrayList<Integer> indexes=new ArrayList();
        for(i=iI;i<len;i++){
            if(ConnectingBorderSegment(p,rbsNodes.get(i),region)) indexes.add(i);
        }
        return indexes;
    }
    public ArrayList<Integer> getDiagonallyConnectingBorderSegment(ArrayList<RegionBorderSegmentNode> rbsNodes,Point p,int region, int iI){
        int i,len=rbsNodes.size();
        ArrayList<Integer> indexes=new ArrayList();
        for(i=iI;i<len;i++){
            if(DiagonallyConnectingBorderSegment(p,rbsNodes.get(i),region)) indexes.add(i);
        }
        return indexes;
    }
    public ArrayList<Integer> getOverlappingBorderSegment(ArrayList<RegionBorderSegmentNode> rbsNodes,Point p,int region, int iI){
        int i,len=rbsNodes.size();
        ArrayList<Integer> indexes=new ArrayList();
        for(i=iI;i<len;i++){
            if(OverlappingBorderSegment(p,rbsNodes.get(i),region)) indexes.add(i);
        }
        return indexes;
    }
    public boolean ConnectingBorderSegment(Point p, RegionBorderSegmentNode rbsNode, int region){
        Point pt=getFirstPoint(rbsNode,region);
        return(CommonMethods.DirectContacting(p, pt));
    }
    public boolean DiagonallyConnectingBorderSegment(Point p, RegionBorderSegmentNode rbsNode, int region){
        Point pt=getFirstPoint(rbsNode,region);
        return(CommonMethods.DiagonallyContacting(p, pt));
    }
    public boolean OverlappingBorderSegment(Point p, RegionBorderSegmentNode rbsNode, int region){
        Point pt=getFirstPoint(rbsNode,region);
        return(CommonMethods.Overlapping(p, pt));
    }
    public Point getFirstPoint(RegionBorderSegmentNode rbsNode, int region){
        return getFirstBorderNode(rbsNode,region).location;
    }
    public RegionBorderNode getFirstBorderNode(RegionBorderSegmentNode rbsNode, int region){
        if(getRegionSideness(rbsNode,region)==right){
            return rbsNode.BoundaryNodes.get(0);
        }else{
            return rbsNode.BoundaryNodes.get(rbsNode.BoundaryNodes.size()-1);
        }
    }
    public Point getSecondPoint(RegionBorderSegmentNode rbsNode, int region){
        return getSecondBorderNode(rbsNode,region).location;
    }
    public RegionBorderNode getSecondBorderNode(RegionBorderSegmentNode rbsNode, int region){
        if(getRegionSideness(rbsNode,region)==right){
            return rbsNode.BoundaryNodes.get(1);
        }else{
            return rbsNode.BoundaryNodes.get(rbsNode.BoundaryNodes.size()-2);
        }
    }
    public Point getLastPoint(RegionBorderSegmentNode rbsNode, int region){
        return getLastNode(rbsNode,region).location;
    }
    public RegionBorderNode getLastNode(RegionBorderSegmentNode rbsNode, int region){
        if(getRegionSideness(rbsNode,region)==left){
            return rbsNode.BoundaryNodes.get(0);
        }else{
            return rbsNode.BoundaryNodes.get(rbsNode.BoundaryNodes.size()-1);
        }
    }
    public Point getSecondLastPoint(RegionBorderSegmentNode rbsNode, int region){
        return getSecondLastNode(rbsNode,region).location;
    }
    public RegionBorderNode getSecondLastNode(RegionBorderSegmentNode rbsNode, int region){
        if(getRegionSideness(rbsNode,region)==left){
            return rbsNode.BoundaryNodes.get(1);
        }else{
            return rbsNode.BoundaryNodes.get(rbsNode.BoundaryNodes.size()-2);
        }
    }
    public int getRegionSideness(RegionBorderSegmentNode rbsNode, int region){
        if(region<m_nNumRegions+1){//m_nNumRegions is the largest region index
            if(rbsNode.region1==region) return left;
            if(rbsNode.region2==region) return right;
        }else{//region==complex index+m_nNumRegions
            if(rbsNode.complex1==region) return left;
            if(rbsNode.complex2==region) return right;
        }
        IJ.error("unable to ditermine the region sideness, getRegionSideness");
        return -1;
    }
    public static void getRBNodes(ArrayList<RegionBorderNode> rbNodes0,int iI, int iF, int delta, ArrayList<RegionBorderNode> rbNodes){
        for(int i=iI;i<=iF;i+=delta){
            rbNodes.add(rbNodes0.get(i));
        }
    }
    void sortRegionBorderSegmentsNew(){
        int i,len=m_cvRegionNodes.size();
        RegionNode rNode;
        ArrayList<Integer> nvSegmentEnds;
        for(i=0;i<len;i++){
            if(i==366){
                i=i;
            }
            rNode=m_cvRegionNodes.get(i);/*will delete the commented out part after making sure the code is correct
            if(rNode.boundarySegments.size()>0){
                sortBorderSegmentsNew(rNode.boundarySegments,rNode.regionIndex);
            }else{
                i=i;
            }*/
            nvSegmentEnds=sortBorderSegmentsNew(rNode.boundarySegments,rNode.regionIndex);
            if(nvSegmentEnds.isEmpty()){
                rNode.m_cvRegionComplexBoundaries.add(new RegionComplexBoundaryNode(rNode.boundarySegments));
            }else{
                rNode.m_cvRegionComplexBoundaries=buildRegionComplexBoundaries(rNode.boundarySegments,nvSegmentEnds);
                rNode.boundarySegments=rNode.m_cvRegionComplexBoundaries.get(0).m_cvRBSNodes;//only keep the outer boundary
            }
        }
    }
    public Histogram getBorderSegmentHeightHistogram(int[][] pixels){
        int i,len=m_cvBorderSegments.size();
        ArrayList<Double> heights=new ArrayList();
        for(i=0;i<len;i++){
            heights.add(getBorderSegmentHeight(m_cvBorderSegments.get(i),pixels));
        }
        Histogram hist=new Histogram(heights);
        return hist;
    }
    public double getBorderSegmentHeight(RegionBorderSegmentNode rbsNode, int[][] pixels){
        ArrayList<RegionBorderNode> rbNodes=rbsNode.BoundaryNodes;
        int i,pixel,pn=Integer.MAX_VALUE,px=Integer.MIN_VALUE,len=rbNodes.size();
        Point pt;
        for(i=0;i<len;i++){
            pt=rbNodes.get(i).location;
            pixel=pixels[pt.y][pt.x];
            if(pixel>px) px=pixel;
            if(pixel<pn) pn=pixel;
        }
        double height=px-pn;
        return height;
    }
    public double getBorderSegmentHeight_FromRegionBases(RegionBorderSegmentNode rbsNode, int[][] pixels){
        ArrayList<RegionBorderNode> rbNodes=rbsNode.BoundaryNodes;
        int i,pixel,px=Integer.MIN_VALUE,len=rbNodes.size();
        Point pt;
        int region1=rbsNode.region1,region2=rbsNode.region2;

        for(i=0;i<len;i++){
            pt=rbNodes.get(i).location;
            pixel=pixels[pt.y][pt.x];
            if(pixel>px) px=pixel;
        }
        double height=px-0.5*(m_cvRegionNodes.get(region1-1).base+m_cvRegionNodes.get(region2-1).base);
        return height;
    }
    public void removeOverheightBorderSegments(int[][] pixels, double cutoff){
        removeOverheightBorderSegments(pixels,cutoff,true);
    }
    public void removeOverheightBorderSegments(int[][] pixels, double cutoff, boolean completeComplexes){
        //bordersegments whose heights are higher than cutoff will be removed, and the regions partitioned by the removed border segments will be
        //merged into complexes.
        int i,len=m_cvBorderSegments.size();
        RegionBorderSegmentNode rbsNode;
        double height;
        for(i=0;i<len;i++){
            rbsNode=m_cvBorderSegments.get(i);
            height=getBorderSegmentHeight(rbsNode,pixels);
            if(height>cutoff){
                removeBorderSegment(rbsNode);
            }
        }
        if(completeComplexes) completeComplexes();
    }
    void completeComplexes(){
        int i,len=m_cvComplexNodes.size();
        RegionComplexNode cNode;
        int cIndex=0;
        for(i=0;i<len;i++){
            cNode=m_cvComplexNodes.get(i);
            if(cNode.complexIndex==-1){
                continue;
            }
            completeComplexNode(cNode,cIndex);
            swapComplexElements(m_cvComplexNodes,cIndex,i);
            cIndex++;
        }
        int len1=len-cIndex;
        int index;
        for(i=0;i<len1;i++){
            index=len-1-i;
            m_cvComplexNodes.remove(index);
        }
        sortComplexBoundaryNodes();
    }
    void completeComplexNode(RegionComplexNode cNode, int cIndex){
        cNode.complexIndex=cIndex;
        ArrayList<RegionNode> rNodes=cNode.m_cvEnclosedRegions;
        RegionNode rNode;
        ArrayList<RegionBorderSegmentNode> rbsNodes;
        RegionBorderSegmentNode rbsNode;
        int len=rNodes.size(),i,j,len1;
        int areaR=0,areaB=0;
        for(i=0;i<len;i++){
            rNode=rNodes.get(i);
            rNode.complexIndex=cIndex;
        }
        for(i=0;i<len;i++){
            rNode=rNodes.get(i);
            areaR+=rNode.area;
            rbsNodes=rNode.boundarySegments;
            len1=rbsNodes.size();
            for(j=0;j<len1;j++){
                rbsNode=rbsNodes.get(j);
                if(isRemoved(rbsNode)){
                    if(getRegionSideness(rbsNode,rNode.regionIndex)==left)continue;
                    rbsNode.complexIndex=cIndex;
                    cNode.m_cvInnerBorderSegments.add(rbsNode);
                    areaB+=removeRegionBorderNodes(rbsNode);
                }else {
                    assignComplexIndex(rbsNode,cIndex);
                    if(isIntraComplexBorderSegment(rbsNode)){
                        if(getRegionSideness(rbsNode,rNode.regionIndex)==left)continue;
                        rbsNode.complexIndex=cIndex;
                        cNode.m_cvIntraComplexBorderSegments.add(rbsNode);
                        areaB+=removeRegionBorderNodes(rbsNode);
                    } else
                        cNode.m_cvBorderSegments.add(rbsNode);
                }
            }
        }
        cNode.area=areaR+areaB;
    }
    int assignComplexIndex(RegionBorderSegmentNode rbsNode, int cIndex){
        int region1=rbsNode.region1,region2=rbsNode.region2;
        boolean assigned=false;
        if(m_cvRegionNodes.get(region1-1).complexIndex==cIndex){
            rbsNode.complex1=cIndex+m_nNumRegions+1;
            assigned=true;
        }
        if(m_cvRegionNodes.get(region2-1).complexIndex==cIndex){
            rbsNode.complex2=cIndex+m_nNumRegions+1;
            assigned=true;
        }
        if(assigned) return 1;
        IJ.error("error in assigning complex index, assignComplexIndex");
        return -1;
    }
    int removeRegionBorderNodes(RegionBorderSegmentNode rbsNode){
        ArrayList<RegionBorderNode> rbNodes=rbsNode.BoundaryNodes;
        RegionBorderNode rbNode;
        int len=rbNodes.size(),i,cIndex=rbsNode.complexIndex;
        int areaB=len-2;
        if(areaB<0) areaB=0;//for single-node segments
        rbNode=rbNodes.get(0);
        if(isUnlinked(rbNode)){
            rbNode.complexIndex=cIndex;
            areaB+=1;
        }
        if(len==1) return areaB;
        for(i=1;i<len-1;i++){
            rbNode=rbNodes.get(i);
            rbNode.complexIndex=cIndex;
        }
        rbNode=rbNodes.get(len-1);
        if(isUnlinked(rbNode)){
            rbNode.complexIndex=cIndex;
            areaB+=1;
        }
        return areaB;
    }
    boolean isRemoved(RegionBorderSegmentNode rbsNode){
        return (rbsNode.complexIndex>=0);
    }
    boolean isRemovable(RegionBorderSegmentNode rbsNode){
        if(rbsNode.complexIndex>=0) return true;
        ArrayList<RegionBorderNode> rbNodes=rbsNode.BoundaryNodes;
        if(isUnlinked(getFirstNode(rbsNode))) return true;
        if(isUnlinked(getLastNode(rbsNode))) return true;
        return false;
    }
    boolean isUnlinked(RegionBorderNode rbNode){
        ArrayList<Integer> rIndexes=rbNode.neighboringRegions;
        int len=rIndexes.size(),i,index,cIndex,cIndex0=0;
        RegionNode rNode;
        for(i=0;i<len;i++){
            index=rIndexes.get(i)-1;
            rNode=m_cvRegionNodes.get(index);
            cIndex=rNode.complexIndex;
            if(cIndex<0) return false;
            if(i==0){
                cIndex0=cIndex;
            }else{
                if(cIndex!=cIndex0) return false;
            }
        }
        return true;
    }
    RegionBorderNode getFirstNode(RegionBorderSegmentNode rbsNode){
        return rbsNode.BoundaryNodes.get(0);
    }
    RegionBorderNode getLastNode(RegionBorderSegmentNode rbsNode){
        ArrayList<RegionBorderNode> rbNodes=rbsNode.BoundaryNodes;
        int len=rbNodes.size();
        return rbsNode.BoundaryNodes.get(len-1);
    }
    int removeBorderSegment(RegionBorderSegmentNode rbsNode){
        if(rbsNode.complexIndex>=0) return -1;
        int region1=rbsNode.region1,region2=rbsNode.region2;
        RegionNode rNode1=m_cvRegionNodes.get(region1-1);
        RegionNode rNode2=m_cvRegionNodes.get(region2-1);
        if(rbsNode.segIndex==7137||rbsNode.segIndex==7144){
            rbsNode=rbsNode;
        }
        int i,it,complexIndex=m_cvComplexNodes.size(),complex1=rNode1.complexIndex,complex2=rNode2.complexIndex;
        if(complex1>=0&&complex2>=0){
            mergeComplexes(complex1,complex2,rbsNode);
        }else if(complex1>=0&&complex2<0){
            mergeRegionToComplex(complex1,rNode2,rbsNode);
        }else if(complex1<0&&complex2>=0){
            mergeRegionToComplex(complex2,rNode1,rbsNode);
        }else{
            mergeRegions(rNode1,rNode2,rbsNode);
        }

        return 1;
    }
    int mergeRegions(RegionNode rNode1, RegionNode rNode2, RegionBorderSegmentNode rbsNode){
        RegionComplexNode cNode=new RegionComplexNode();
        int cIndex=m_cvComplexNodes.size();
        rbsNode.complexIndex=cIndex;
        rNode1.complexIndex=cIndex;
        rNode2.complexIndex=cIndex;
        cNode.complexIndex=cIndex;
        cNode.m_cvEnclosedRegions.add(rNode1);
        cNode.m_cvEnclosedRegions.add(rNode2);
        m_cvComplexNodes.add(cNode);
        return cIndex;
    }
    int mergeRegionToComplex(int cIndex,RegionNode rNode,RegionBorderSegmentNode rbsNode){
          RegionComplexNode cNode=m_cvComplexNodes.get(cIndex);
          rbsNode.complexIndex=cIndex;
          rNode.complexIndex=cIndex;
          cNode.m_cvEnclosedRegions.add(rNode);
          return cIndex;
    }
    int mergeComplexes(int cIndex1, int cIndex2,RegionBorderSegmentNode rbsNode){
        int complexIndex=Math.min(cIndex1, cIndex2);
        rbsNode.complexIndex=complexIndex;
        if(cIndex1==cIndex2) return complexIndex;//happen to the 
        RegionComplexNode cNode=new RegionComplexNode();
        cNode.complexIndex=complexIndex;
        int i,len;

        RegionNode rNode;
        RegionComplexNode cNode1=m_cvComplexNodes.get(cIndex1),cNode2=m_cvComplexNodes.get(cIndex2);
        len=cNode1.m_cvEnclosedRegions.size();
        for(i=0;i<len;i++){
            rNode=cNode1.m_cvEnclosedRegions.get(i);
            rNode.complexIndex=complexIndex;
            cNode.m_cvEnclosedRegions.add(rNode);
        }
        len=cNode2.m_cvEnclosedRegions.size();
        for(i=0;i<len;i++){
            rNode=cNode2.m_cvEnclosedRegions.get(i);
            rNode.complexIndex=complexIndex;
            cNode.m_cvEnclosedRegions.add(rNode);
        }
        m_cvComplexNodes.set(complexIndex,cNode);
        m_cvComplexNodes.get(Math.max(cIndex1, cIndex2)).complexIndex=-1;
        return complexIndex;
    }
    void sortComplexBoundaryNodes(){
        int i,len=m_cvComplexNodes.size(),j,len1,lent,lentx,kx,k;
        RegionComplexBoundaryNode rcbNode;
        RegionComplexNode cNode;
        for(i=0;i<len;i++){
            cNode=m_cvComplexNodes.get(i);
            sortComplexBoundaryNodes(cNode);
            len1=cNode.m_cvComplexBoundaries.size();
            if(len1==1) continue;
            for(j=0;j<len1;j++){
                lentx=cNode.m_cvComplexBoundaries.get(j).m_cvRBSNodes.size();;
                kx=j;
                for(k=j+1;k<len1;k++){
                    rcbNode=cNode.m_cvComplexBoundaries.get(k);
                    lent=rcbNode.m_cvRBSNodes.size();
                    if(lent>lentx){
                        lentx=lent;
                        kx=k;
                    }
                }
                if(kx>j) swapRCBElements(cNode.m_cvComplexBoundaries,j,kx);
            }
        }
    }
    void swapRCBElements(ArrayList<RegionComplexBoundaryNode> rcbNodes,int i1, int i2){
        RegionComplexBoundaryNode rcbNode=rcbNodes.get(i1);
        rcbNodes.set(i1, rcbNodes.get(i2));
        rcbNodes.set(i2, rcbNode);
    }
    void sortComplexBoundaryNodes(RegionComplexNode cNode){
        ArrayList<Integer> nvSegmentEnds=sortBorderSegmentsNew(cNode.m_cvBorderSegments,cNode.complexIndex+m_nNumRegions+1);

        if(nvSegmentEnds.isEmpty()){
            cNode.m_cvComplexBoundaries.add(new RegionComplexBoundaryNode(cNode.m_cvBorderSegments));
        }else{//the following block could be now rewriten as cNode.m_cvBorderSegments=buildRegionComplexBoundaries(cNode.m_cvBorderSegments,nvSegmentEnds)
            //I am leaving it as it is. 
            ArrayList<RegionBorderSegmentNode> rbsNodes=cNode.m_cvBorderSegments;
            int len=nvSegmentEnds.size(),i,index,len0=rbsNodes.size();
            int iI=nvSegmentEnds.get(len-1),iF;
            for(i=0;i<len;i++){
                ArrayList<RegionBorderSegmentNode> rbsNodest=new ArrayList();
                iF=nvSegmentEnds.get(i);
                index=CommonMethods.circularAddition(len0, iI, 1);
                rbsNodest.add(rbsNodes.get(index));
                while(index!=iF){
                    index=CommonMethods.circularAddition(len0, index, 1);
                    rbsNodest.add(rbsNodes.get(index));
                }
                cNode.m_cvComplexBoundaries.add(new RegionComplexBoundaryNode(rbsNodest));
                iI=iF;
            }
        }
    }
    ArrayList<RegionComplexBoundaryNode> buildRegionComplexBoundaries(ArrayList<RegionBorderSegmentNode> rbsNodes,ArrayList<Integer> nvSegmentEnds){
        ArrayList<RegionComplexBoundaryNode> rcbNodes=new ArrayList();
        int len=nvSegmentEnds.size(),i,index,len0=rbsNodes.size();
        int iI=nvSegmentEnds.get(len-1),iF;
        int ix=0,nx=0,size;
        for(i=0;i<len;i++){
            ArrayList<RegionBorderSegmentNode> rbsNodest=new ArrayList();
            iF=nvSegmentEnds.get(i);
            index=CommonMethods.circularAddition(len0, iI, 1);
            rbsNodest.add(rbsNodes.get(index));
            while(index!=iF){
                index=CommonMethods.circularAddition(len0, index, 1);
                rbsNodest.add(rbsNodes.get(index));
            }
            rcbNodes.add(new RegionComplexBoundaryNode(rbsNodest));
            size=rbsNodest.size();
            if(nx<size){
                nx=size;
                ix=i;
            }
            iI=iF;
        }
        if(ix!=0){
            RegionComplexBoundaryNode rcbNode=rcbNodes.get(0);
            rcbNodes.set(0, rcbNodes.get(ix));
            rcbNodes.set(ix, rcbNode);
        }
        return rcbNodes;
    }
    public void drawRegionComplex(ImagePlus impl, RegionComplexNode cNode,int[][]pixels){
        drawRemovedBorderSegments(impl,cNode,pixels);
        drawRegionComplexBoundary(impl,cNode,pixels);
        drawComplexInnerPoints(impl,cNode);
    }
    ArrayList<RegionBorderNode> getRegionComplexBoundary(RegionComplexNode cNode,int index){
        return getRegionBoundary(cNode.m_cvComplexBoundaries.get(index).m_cvRBSNodes, cNode.complexIndex+m_nNumRegions+1);
    }
    public void drawRegionComplexBoundary(ImagePlus impl, RegionComplexNode cNode,int[][] pixels){
        ArrayList<RegionBorderNode> rbNodes;
        RegionBorderNode rbNode;
        int h=impl.getHeight(),w=impl.getWidth();
        int i,cIndex=cNode.complexIndex,len,pixel,pixelst[]=(int[])impl.getProcessor().getPixels();
        int lent, j;
        len=cNode.m_cvComplexBoundaries.size();
        for(i=0;i<len;i++){
            rbNodes=getRegionComplexBoundary(cNode,i);
            lent=rbNodes.size();
            for(j=0;j<lent;j++){
                rbNode=rbNodes.get(j);
                Point pt=rbNode.location;
                pixel=getPixelOfComplexBoundaryNode(pt,pixels,cIndex,j);
                pixelst[w*pt.y+pt.x]=pixel;
            }
        }
    }
    public int getPixelOfComplexBoundaryNode(Point pt, int[][]pixels, int cIndex, int index){
        int r=cIndex%255,g=pixels[pt.y][pt.x]%255,b=index%255;
        return (r<<16)|(g<<8)|b;
    }
    public void drawRemovedBorderSegments(ImagePlus impl, RegionComplexNode cNode, int[][] pixels){
        ArrayList<RegionBorderSegmentNode> rbsNodes=cNode.m_cvInnerBorderSegments;
        ArrayList<RegionBorderNode> rbNodes;
        RegionBorderSegmentNode rbsNode;
        RegionBorderNode rbNode;
        int i,j,len=rbsNodes.size(),len1,pixel;
        int h=impl.getHeight(),w=impl.getWidth();
        Point pt;
        int[] pixelst=(int[])impl.getProcessor().getPixels();

        for(i=0;i<len;i++){
            rbsNode=rbsNodes.get(i);
            rbNodes=rbsNode.BoundaryNodes;
            len1=rbNodes.size();
            for(j=0;j<len1;j++){
                rbNode=rbNodes.get(j);
                pt=rbNode.location;
                pixel=pixelOfRemovedBorderNode(pt, pixels);
                pixelst[pt.y*w+pt.x]=pixel;
            }
        }
    }
    public int pixelOfRemovedBorderNode(Point pt,int[][] pixels){
        int pixel=pixels[pt.y][pt.x]%255;
        pixel=(pixel<<16)|(pixel<<8);
        return pixel;
    }
    public ArrayList<RegionComplexNode> getRegionComplexNodes(){
        return m_cvComplexNodes;
    }
    boolean isIntraComplexBorderSegment(RegionBorderSegmentNode rbsNode){
        int cIndex1=rbsNode.complex1,cIndex2=rbsNode.complex2;
        if(cIndex1!=cIndex2) return false;
        if(cIndex1<0) return false;
        return true;
    }
    public ArrayList<Point>[] getRegionComplexBoundaries(RegionComplexNode cNode){
        ArrayList<RegionBorderNode> rbNodes;
        int i,len;
        len=cNode.m_cvComplexBoundaries.size();
        ArrayList<Point>[] boundaries=new ArrayList[len];
        ArrayList<Point>Boundary;
        for(i=0;i<len;i++){            
            rbNodes=getRegionComplexBoundary(cNode,i);
            boundaries[i]=getContour(rbNodes);
        }
        return boundaries;
    }
    public ArrayList<Point> getRegionComplexOuterContour(RegionComplexNode cNode){
        ArrayList<RegionBorderNode> rbNodes=getRegionComplexBoundary(cNode,0);//A region complex could be disconineous. the outer boundary is stored at the first position.
        return getContour(rbNodes);
    }
    public ArrayList <Point> getContour(ArrayList<RegionBorderNode> rbNodes){//a valid contour
        //segment contain no consecutive overlapping points, and all neighboring points should be directly contacting
        int len=rbNodes.size();
        ArrayList<Point> contour=getContourSegment(rbNodes,0,len-1);
        if(CommonMethods.Overlapping(contour.get(0), contour.get(len-1))){
            contour.remove(len-1);
        }
//        if(ContourFollower.getNonDirectContactingPosition(contour)>=0)//to allow disconnected (by the image edges) boundaries
//            IJ.error("Neighboring points are not directly contacting");
        return contour;
    }
    public ArrayList <Point> getContourSegment(ArrayList<RegionBorderNode> rbNodes, int iI, int iF){//a valid contour
        //segment contain no consecutive overlapping points, and all neighboring points should be directly contacting
        ArrayList <Point> seg=new ArrayList();
        RegionBorderNode rbNode;
        int i, j;
        Point p0=rbNodes.get(iI).location,pt;
        seg.add(p0);
        for(i=iI+1;i<=iF;i++){
            rbNode=rbNodes.get(i);
            pt=rbNode.location;
            if(pt.x==p0.x&&pt.y==p0.y) continue;
            seg.add(pt);
            p0=pt;
        }
        return seg;
    }
    public ImageShape[] getRegionComplexShapes(RegionComplexNode cNode){
        ArrayList<Point>[] boundaries=getRegionComplexBoundaries(cNode);
        int i,len=boundaries.length;
        ImageShape[] shapes=new ImageShape[len];
        ArrayList<Point> boundary;
        boundary=ContourFollower.OffBoundConnectedContour(boundaries[0], w, h);
        shapes[0]=ImageShapeHandler.buildImageShape(boundary);
        for(i=1;i<len;i++){
            boundary=boundaries[i];
            boundary=ContourFollower.getEnlargedContour(boundary, 0);
            shapes[i]=ImageShapeHandler.buildImageShape(boundary);
        }
        return shapes;
    }

    public ArrayList<Point> getOuterContour(RegionComplexNode cNode){
           ArrayList<Point>[] boundaries=getRegionComplexBoundaries(cNode);
           ArrayList<Point> boundary;
           boundary=ContourFollower.OffBoundConnectedContour(boundaries[0], w, h);
           ContourFollower.removeOffBoundPoints(w, h, boundary);
           return boundary;
    }
    public ImageShape getRegionComplexShape(RegionComplexNode cNode){
        ImageShape[] shapes=getRegionComplexShapes(cNode);
        ImageShape outerShape=new ImageShape(shapes[0]);
        int len=shapes.length,i;
        for(i=1;i<len;i++){
            outerShape.excludeShape(shapes[i]);
        }
        outerShape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        return outerShape;
    }
    public ImageShape getRegionComplexOuterShape(RegionComplexNode cNode){
        ImageShape[] shapes=getRegionComplexShapes(cNode);
        ImageShape outerShape=new ImageShape(shapes[0]);
        return outerShape;
    }
    public void getComplexInnerPoints(RegionComplexNode cNode, ArrayList<Point> points){
        getRegionComplexShape(cNode).getInnerPoints(points);
    }
    public void drawComplexInnerPoints(ImagePlus impl, RegionComplexNode cNode){
        int[] rgb=new int[3],pixels=(int[])impl.getProcessor().getPixels();
        ArrayList <Point> points=new ArrayList();
        getComplexInnerPoints(cNode, points);
        int pixel,x,y;
        int i,len=points.size(),cIndex=cNode.complexIndex;
        Point p;
        for(i=0;i<len;i++){
            p=points.get(i);
            x=p.x;
            y=p.y;
            pixel=pixels[w*y+x];
            CommonMethods.intTOrgb(pixel, rgb);
            rgb[1]=i%255;
            rgb[2]=cIndex%255;
            pixel=CommonMethods.rgbTOint(rgb);
            pixels[w*y+x]=pixel;
        }
    }
    public FittingResultsNode fitComplex_Gaussian2D(ImagePlus implDisplay, int[][] pixels,RegionComplexNode cNode){
        ImageShape cIS=getRegionComplexShape(cNode);
        ArrayList<RegionNode> regions=cNode.m_cvEnclosedRegions;
        int i,len=regions.size();
        ArrayList<Point> regionPeakLocations=new ArrayList();
        for(i=0;i<len;i++){
            regionPeakLocations.add(regions.get(i).peakLocation);
        }
        FittingResultsNode resultsNode=ImageShapeHandler.fitPixels_Gaussian2D(implDisplay,pixels, cIS, regionPeakLocations);
        return resultsNode;
    }
    public FittingResultsNode fitRegion_Gaussian2D(ImagePlus implDisplay, int[][] pixels,RegionNode rNode){
        ImageShape cIS=getRegionShape(rNode);
        int i;
        ArrayList<Point> regionPeakLocations=new ArrayList();
        regionPeakLocations.add(rNode.peakLocation);
        FittingResultsNode resultsNode=ImageShapeHandler.fitPixels_Gaussian2D(implDisplay,pixels, cIS, regionPeakLocations);
        return resultsNode;
    }
    public void showComplexFitting_StandardFunction(ImagePlus impl,RegionComplexNode cNode, double[] pdFittedPars, String sFunctionType){
        ImageShape cIS=getRegionComplexShape(cNode);
        ArrayList<Point> points=new ArrayList();
        cIS.getInnerPoints(points);
        ArrayList<Point> contour=cIS.getOuterContour();
        ContourFollower.removeOffBoundPoints(w, h, contour);
        int type=impl.getType();
        if(type!=ImagePlus.GRAY32){
            IJ.error("the image type for showFunctionValues must be GRAY32");
        }
        CommonMethods.showFunctionValues_StandardFunction(impl, pdFittedPars, points, sFunctionType);
        CommonMethods.showFunctionValues_StandardFunction(impl, pdFittedPars, contour, sFunctionType);
    }
    public void showRegionFitting_StandardFunction(ImagePlus impl,RegionNode rNode, double[] pdFittedPars, String sFunctionType){
        ImageShape cIS=getRegionShape(rNode);
        ArrayList<Point> points=new ArrayList();
        cIS.getInnerPoints(points);
        ArrayList<Point> contour=cIS.getOuterContour();
        ContourFollower.removeOffBoundPoints(w, h, contour);
        int type=impl.getType();
        if(type!=ImagePlus.GRAY32){
            IJ.error("the image type for showFunctionValues must be GRAY32");
        }
        CommonMethods.showFunctionValues_StandardFunction(impl, pdFittedPars, points, sFunctionType);
        CommonMethods.showFunctionValues_StandardFunction(impl, pdFittedPars, contour, sFunctionType);
    }
    public void getRegionContour(RegionNode rNode, ArrayList<Point> contour){
        contour.clear();
        ArrayList<Point> contour1=getContour(getRegionBoundary(rNode));
        if(contour1!=null){
            contour1=ContourFollower.OffBoundConnectedContour(contour1, w, h);
            CommonStatisticsMethods.copyPointArray(contour1, contour);
        }
    }
    public ImageShape getRegionShape(int rIndex){
        return getRegionShape(m_cvRegionNodes.get(rIndex-1));
    }
    public RegionNode getRegionNode(Point p0){
        int x=p0.x,y=p0.y;
        if(x<0||x>=w) return null;
        if(y<0||y>=h) return null;
        
        int rIndex=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[y][x]);
        return m_cvRegionNodes.get(rIndex-1);
    }
    public ImageShape getRegionShape(RegionNode rNode){
        if(rNode.cIS!=null) return rNode.cIS;
        ArrayList<Point> contour=new ArrayList();
        getRegionContour(rNode,contour);
        ImageShape cIS=ImageShapeHandler.buildImageShape(contour);
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        rNode.cIS=cIS;
        return cIS;
    }
    public ArrayList<Point> getContour(RegionNode rNode){
        ArrayList<Point> contour=new ArrayList();
        getRegionContour(rNode,contour);
        return contour;
    }
    public ArrayList<RegionNode> getRegionNodes(){
        return m_cvRegionNodes;
    }
    public ArrayList<RegionComplexNode> getComplexNodes(){
        return m_cvComplexNodes;
    }
    public void buildRegionHistograms(int[][] pixels){
        m_cBoundarySegmentHeightHist=getBorderSegmentHeightHistogram(pixels);
        ArrayList<Double> dvSumHeights=new ArrayList();
        ArrayList<Double> dvRegionSizes=new ArrayList();
        ArrayList<Double> dvRegionHeights=new ArrayList();

        int i,len=m_cvRegionNodes.size(),j,len1,pixel,pixel1,sum;
        Point peak,pt;
        ArrayList<Point> contour;
        RegionNode aRegion;
        aRegion=m_cvRegionNodes.get(0);
        if(aRegion.meanSem==null) buildRegionPixelStatistics(pixels);
        for(i=0;i<len;i++){
            aRegion=m_cvRegionNodes.get(i);
            dvRegionHeights.add(aRegion.height);
            dvRegionSizes.add((double)aRegion.area);
            dvSumHeights.add(aRegion.sum);
        }
        m_cRegionSizeHist=new Histogram(dvRegionSizes);
        m_cRegionHeightHist=new Histogram(dvRegionHeights);
        m_cRegionSumHist=new Histogram(dvSumHeights);
    }
    public Histogram getBorderSegmentHeightHistogram(){
        return m_cBoundarySegmentHeightHist;
    }
    public Histogram getRegionAreaHistogram(){
        return m_cRegionSizeHist;
    }
    public Histogram getRegionHeightHistogram(){
        return m_cRegionHeightHist;
    }

    public ArrayList<Point> getBoundaryPoints(RegionNode aNode){
        ArrayList<Point> points=aNode.m_cvBoundaryPoints;
        if(points.isEmpty()) assignRegionAndComplexBoundaryPoints();
        return points;
    }
    public ArrayList<Point> getBoundaryPoints(RegionComplexNode cNode){
        ArrayList<Point> points=cNode.m_cvBoundaryPoints;
        if(points.isEmpty()) assignRegionAndComplexBoundaryPoints();
        return points;
    }
    public void assignRegionAndComplexBoundaryPoints(){//border points are assigned to the nearest (peak location) regions.
        int i,j,w=m_pnStamp[0].length,h=m_pnStamp.length,nodeIndex,rIndex,cIndex,nbrs,k,kn;
        double dist2,dn;
        RegionBorderNode bNode;
        ArrayList<Integer> nvNbrs;
        RegionNode rNode;
        Point p,pt;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                nodeIndex=m_pnNodeIndexes[i][j];
                if(nodeIndex<0) continue;
                bNode=m_cvBorderNodes.get(nodeIndex);
//                rIndex=bNode.regionIndex-1;//region index starts from 1
                p=new Point(j,i);


                nvNbrs=bNode.neighboringRegions;
                nbrs=nvNbrs.size();

                dn=Double.POSITIVE_INFINITY;
                kn=0;
                for(k=0;k<nbrs;k++){
                    rIndex=nvNbrs.get(k)-1;//region index starts from 1
                    rNode=m_cvRegionNodes.get(rIndex);
                    pt=rNode.peakLocation;
                    dist2=CommonMethods.getDist2(p.x, p.y, pt.x, pt.y);
                    if(dist2<dn){
                        kn=k;
                        dn=dist2;
                    }
                }

                rIndex=nvNbrs.get(kn)-1;
                if(rIndex==0){
                    rIndex=rIndex;
                }
                rNode=m_cvRegionNodes.get(rIndex);
//                rNode=m_cvRegionNodes.get(rIndex-1);//13205
                rNode.m_cvBoundaryPoints.add(p);

                cIndex=bNode.complexIndex;
                if(cIndex>=0) continue;//blongs to inner border segments

                cIndex=rNode.complexIndex;
                if(cIndex<0) continue;//not in any complexes
//                cIndex--;//complex index starts from 1
                m_cvComplexNodes.get(cIndex).m_cvBoundaryPoints.add(p);
            }
        }
    }

    public void buildRegionPixelStatistics(int[][] pixels){
        int i,j,index,region,pixel;
        int nNumRegions=m_cvRegionNodes.size();
        double[]pdSum=new double[nNumRegions],pdSS=new double[nNumRegions];
        int[]pnNum=new int[nNumRegions];
        intRange pcRanges[]=new intRange[nNumRegions];

        for(i=0;i<nNumRegions;i++){
            pdSum[i]=0;
            pdSS[i]=0;
            pcRanges[i]=new intRange();
            pnNum[i]=0;
        }

        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                index=m_pnStamp[i][j];
                region=LandscapeAnalyzerPixelSorting.getRegionIndex(index)-1;
                pixel=pixels[i][j];
                pdSum[region]+=pixel;
                pdSS[region]+=pixel*pixel;
                pnNum[region]+=1;
                pcRanges[region].expandRange(pixel);
            }
        }

        RegionNode rNode;
        MeanSem1 ms;
        double dNum;
        intRange ir;
        Point pt;
//        double base=CommonMethods.getMostProbablePixelValue(pixels, 5);//not good way, 11806
        ArrayList<Point> contour;

        Histogram hist;
        ArrayList<Double> dvPixels=new ArrayList();
        for(i=0;i<nNumRegions;i++){
            rNode=m_cvRegionNodes.get(i);
            if(rNode.fitted) continue;
            ms=new MeanSem1();
            dNum=pnNum[i];
            ir=pcRanges[i];
            ms.updateMeanSquareSum(pnNum[i], pdSum[i]/dNum, pdSS[i]/dNum,ir.getMax(), ir.getMin());
            rNode.meanSem=ms;
            pt=rNode.peakLocation;
            pixel=pixels[pt.y][pt.x];
            rNode.peak=pixel;
            contour=getContour(rNode);
            ContourFollower.removeOffBoundPoints(w, h, contour);
            rNode.base=CommonStatisticsMethods.getTrailPercentileValue(pixels,contour,0.5);
//            rNode.base=CommonStatisticsMethods.getTrailMean(pixels,contour);
            rNode.height=pixel-rNode.base;
            rNode.sum=(ms.mean-rNode.base)*dNum;
        }
    }
    public void buildRegionRanges(){
        int i,j,index,region,pixel;
        RegionNode rNode;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                index=m_pnStamp[i][j];
                region=LandscapeAnalyzerPixelSorting.getRegionIndex(index)-1;
                rNode=m_cvRegionNodes.get(region);
                rNode.cXRange.expandRange(j);
                rNode.cYRange.expandRange(i);
            }
        }
    }
    public void buildComplexRanges(){
        int i,j,index,cIndex,pixel,len,len1;
        RegionComplexNode cNode;
        RegionNode rNode;
        len=m_cvComplexNodes.size();
        for(i=0;i<len;i++){
            cNode=m_cvComplexNodes.get(i);
            len1=cNode.m_cvEnclosedRegions.size();
            for(j=0;j<len1;j++){
                rNode=cNode.m_cvEnclosedRegions.get(j);
                cNode.cXRange.expandRange(rNode.cXRange);
                cNode.cYRange.expandRange(rNode.cYRange);
            }
        }
    }
    public void calRegionPixelStatistics(RegionNode rNode,int[][] pixels){
        int i,j,index,region,pixel;
        double dSum=0,dSS=0;
        int nNum=0,rIndex=rNode.regionIndex;
        intRange cPixelRange=new intRange();

        if(m_cvRegionNodes.get(0).cXRange.emptyRange()) buildRegionRanges();

        for(i=rNode.cYRange.getMin();i<=rNode.cYRange.getMax();i++){
            for(j=rNode.cXRange.getMin();j<=rNode.cXRange.getMax();j++){
                index=m_pnStamp[i][j];
                if(i==79&&j==544){
                    i=i;
                }
                region=LandscapeAnalyzerPixelSorting.getRegionIndex(index);
                if(region!=rIndex) continue;
                pixel=pixels[i][j];
                dSum+=pixel;
                dSS+=pixel*pixel;
                nNum++;
                cPixelRange.expandRange(pixel);
            }
        }

        MeanSem1 ms;
        Point pt;

        ArrayList<Point> contour;

        ms=new MeanSem1();
        ms.updateMeanSquareSum(nNum, dSum/nNum, dSS/nNum,cPixelRange.getMax(), cPixelRange.getMin());
        rNode.meanSem=ms;

        pt=rNode.peakLocation;
        pixel=pixels[pt.y][pt.x];
        rNode.peak=pixel;
        contour=getContour(rNode);
        ContourFollower.removeOffBoundPoints(w, h, contour);
        rNode.base=CommonStatisticsMethods.getTrailPercentileValue(pixels,contour,0.5);
//            rNode.base=CommonStatisticsMethods.getTrailMean(pixels,contour);
        rNode.height=pixel-rNode.base;
        rNode.sum=(ms.mean-rNode.base)*nNum;
    }
    public void calRegionComplexPixelStatistics(RegionComplexNode cNode,int[][] pixels){
        int i,j,index,pixel,complex;
        double dSum=0,dSS=0;
        int nNum=0,cIndex=cNode.complexIndex;
        intRange cPixelRange=new intRange();

        if(m_cvComplexNodes.get(0).cXRange.emptyRange()) buildComplexRanges();

        ArrayList<Integer> cvRegionIndexes=new ArrayList();
        int len=cNode.m_cvEnclosedRegions.size();
        double peak=Double.NEGATIVE_INFINITY;
        Point peakLocation;
        RegionNode rNode;
        for(i=0;i<len;i++){
            rNode=cNode.m_cvEnclosedRegions.get(i);
            cvRegionIndexes.add(rNode.regionIndex);
            if(rNode.peak>peak){
                peak=rNode.peak;
                peakLocation=rNode.peakLocation;
            }
        }

        for(i=cNode.cYRange.getMin();i<=cNode.cYRange.getMax();i++){
            for(j=cNode.cXRange.getMin();j<=cNode.cXRange.getMax();j++){
                index=m_pnStamp[i][j];
                if(!CommonMethods.containsContent(cvRegionIndexes, index)) continue;
                pixel=pixels[i][j];
                dSum+=pixel;
                dSS+=pixel*pixel;
                nNum++;
                cPixelRange.expandRange(pixel);
            }
        }

        MeanSem1 ms;
        double dNum;
        intRange ir;
        Point pt;

        ArrayList<Point> contour;

        ms=new MeanSem1();
        ms.updateMeanSquareSum(nNum, dSum/nNum, dSS/nNum,cPixelRange.getMax(), cPixelRange.getMin());
        cNode.meanSem=ms;

        contour=getOuterContour(cNode);
        ContourFollower.removeOffBoundPoints(w, h, contour);
        cNode.base=CommonStatisticsMethods.getTrailPercentileValue(pixels,contour,0.5);
//            rNode.base=CommonStatisticsMethods.getTrailMean(pixels,contour);
        cNode.height=cNode.peak-cNode.base;
        cNode.sum=(ms.mean-cNode.base)*nNum;
    }
    public void calComplexBases(int[][] pixels){
        int i;
        ArrayList<Point> contour;
        int numComplexes=m_cvComplexNodes.size();
        RegionComplexNode cNode;
        for(i=0;i<numComplexes;i++){
            cNode=m_cvComplexNodes.get(i);
            if(cNode.fitted) continue;
            contour=getRegionComplexOuterContour(cNode);
            ContourFollower.removeOffBoundPoints(w, h, contour);
            cNode.base=CommonStatisticsMethods.getTrailPercentileValue(pixels,contour,0.5);
        }
    }
    public int[][] getStamp(){
        return m_pnStamp;
    }
    public Histogram getRegionSumHist(){
        return m_cRegionSumHist;
    }
    public void markSignificantRegions(double hCutoff, double sumCutoff){
        int len=m_cvRegionNodes.size(),i;
        RegionNode aNode;
        m_cvSignificanRegionNodes=new ArrayList();
        for(i=0;i<len;i++){
            aNode=m_cvRegionNodes.get(i);
            if(aNode.peakLocation.x==110&&aNode.peakLocation.y==55){
                i=i;
            }
            if(aNode.height>hCutoff||aNode.sum>sumCutoff) {
                aNode.significant=true;
                m_cvSignificanRegionNodes.add(aNode);
            }
        }
    }
    public void markSignificantRegions(double hCutoff){
        int len=m_cvRegionNodes.size(),i;
        RegionNode aNode;
        m_cvSignificanRegionNodes=new ArrayList();
        for(i=0;i<len;i++){
            aNode=m_cvRegionNodes.get(i);
            if(aNode.peakLocation.x==110&&aNode.peakLocation.y==55){
                i=i;
            }
            if(aNode.height>hCutoff) {
                aNode.significant=true;
                m_cvSignificanRegionNodes.add(aNode);
            }
        }
    }
    public void markSignificantRegions_Size(double sCutoff){
        int len=m_cvRegionNodes.size(),i;
        RegionNode aNode;
        m_cvSignificanRegionNodes=new ArrayList();
        for(i=0;i<len;i++){
            aNode=m_cvRegionNodes.get(i);
            if(aNode.peakLocation.x==110&&aNode.peakLocation.y==55){
                i=i;
            }
            if(aNode.area>sCutoff) {
                aNode.significant=true;
                m_cvSignificanRegionNodes.add(aNode);
            }
        }
    }
    public void markSignificantRegions_Sum(double sCutoff){
        int len=m_cvRegionNodes.size(),i;
        RegionNode aNode;
        Point peak;
        m_cvSignificanRegionNodes=new ArrayList();
        for(i=0;i<len;i++){
            aNode=m_cvRegionNodes.get(i);
            peak=aNode.peakLocation;
            if(CommonMethods.getDist2(peak.x, peak.y, 219, 219)<16){
                i=i;
            }
            if(aNode.sum>sCutoff) {
                aNode.significant=true;
                m_cvSignificanRegionNodes.add(aNode);
            }
        }
    }
    public void calBorderSegmentHeightsAndRatios(int[][] pixels){//the buildRegionPixelStatistics should be called first
        int len=m_cvBorderSegments.size(),i;
        RegionBorderSegmentNode rbNode;
        double base,h,r1,r2;
        int region1,region2;
        RegionNode rNode1,rNode2;
        ArrayList<Double> dvRatios=new ArrayList();
        for(i=0;i<len;i++){
            rbNode=m_cvBorderSegments.get(i);
            region1=rbNode.region1;
            region2=rbNode.region2;
            rNode1=m_cvRegionNodes.get(region1-1);
            rNode2=m_cvRegionNodes.get(region2-1);
            h=getBorderSegmentHeight_FromRegionBases(rbNode,pixels);
            rbNode.height=h;
            r1=h/(rNode1.height);
            rbNode.hRatios[0]=r1;//the region height is already in relative to the base
            r2=h/(rNode2.height);
            rbNode.hRatios[1]=r2;//the region height is already in relative to the base
            dvRatios.add(r1);
            dvRatios.add(r2);
        }
        m_cBorderSegmentHRatioHist=new Histogram(dvRatios);
    }
    public Histogram getBorderSegmentRatioHistogram(){
        return m_cBorderSegmentHRatioHist;
    }
    public void removeHighHeightRatioBorderSegments(double rCutoff){
        int i,len=m_cvBorderSegments.size();
        RegionBorderSegmentNode rbsNode;
        double[] pdRatios;
        int region1,region2;
        for(i=0;i<len;i++){
            rbsNode=m_cvBorderSegments.get(i);
            region1=rbsNode.region1;
            region2=rbsNode.region2;
            if(m_cvRegionNodes.get(region1-1).peakLocation.x==93&&m_cvRegionNodes.get(region1-1).peakLocation.y==202){
                i=i;
            }
            if(m_cvRegionNodes.get(region2-1).peakLocation.x==93&&m_cvRegionNodes.get(region2-1).peakLocation.y==202){
                i=i;
            }
            if(!m_cvRegionNodes.get(region1-1).significant&&!m_cvRegionNodes.get(region2-1).significant)continue;
//            if(!m_cvRegionNodes.get(region2-1).significant)continue;
            pdRatios=rbsNode.hRatios;
            if(pdRatios[0]>rCutoff&&m_cvRegionNodes.get(region1-1).significant||pdRatios[1]>rCutoff&&m_cvRegionNodes.get(region2-1).significant) {
                removeBorderSegment(rbsNode);
            }else{
                i=i;
            }
        }
        completeComplexes();
    }
    public void removeCloseToPeakBorderSegments(double d2Cutoff1, double d2Cutoff2){
        int i,len=m_cvBorderSegments.size();
        RegionBorderSegmentNode rbsNode;
        ArrayList<RegionBorderNode> cvBNodes;
        int region1,region2;
        Point p1,p2,p,pn1,pn2;
        int len1,j;
        double dn1,dn2,dist2;
        boolean remove=false;

        for(i=0;i<len;i++){
            remove=false;
            rbsNode=m_cvBorderSegments.get(i);
            region1=rbsNode.region1;
            region2=rbsNode.region2;

            dn1=Double.POSITIVE_INFINITY;
            dn2=Double.POSITIVE_INFINITY;

            p1=m_cvRegionNodes.get(region1-1).peakLocation;
            p2=m_cvRegionNodes.get(region2-1).peakLocation;
            cvBNodes=rbsNode.BoundaryNodes;
            len1=cvBNodes.size();
            for(j=0;j<len1;j++){
                p=cvBNodes.get(j).location;
                dist2=CommonMethods.getDist2(p.x, p.y, p1.x, p1.y);
                if(dist2<dn1) dn1=dist2;
                dist2=CommonMethods.getDist2(p.x, p.y, p2.x, p2.y);
                if(dist2<dn2) dn2=dist2;
            }

            if(dn1<=d2Cutoff1&&m_cvRegionNodes.get(region1-1).significant||dn2<=d2Cutoff1&&m_cvRegionNodes.get(region2-1).significant) {
                remove=true;
            }else if(dn1<d2Cutoff2&&m_cvRegionNodes.get(region1-1).significant&&dn2<d2Cutoff2&&m_cvRegionNodes.get(region2-1).significant) {
                remove=true;
            }
            if(remove){
                removeBorderSegment(rbsNode);
            }
        }
        completeComplexes();
    }
    public void removeCloseToPeakBorderSegments(double d2Cutoff){
        int i,len=m_cvBorderSegments.size();
        RegionBorderSegmentNode rbsNode;
        int region1,region2;
        Point p1,p2;
        boolean remove=false;

        RegionNode rNode1,rNode2;
        for(i=0;i<len;i++){
            remove=false;
            rbsNode=m_cvBorderSegments.get(i);
            rNode1=m_cvRegionNodes.get(rbsNode.region1-1);
            rNode2=m_cvRegionNodes.get(rbsNode.region2-1);

            if(!rNode1.significant||!rNode2.significant) continue;

            p1=rNode1.peakLocation;
            p2=rNode2.peakLocation;
            if(CommonMethods.getDist2(p1.x, p1.y, p2.x, p2.y)<d2Cutoff) remove=true;
            if(remove){
                removeBorderSegment(rbsNode);
            }
        }
        completeComplexes();
    }
    public static boolean containsRegion(RegionComplexNode cNode, RegionNode rNode){
        int i,len=cNode.m_cvEnclosedRegions.size();
        for(i=0;i<len;i++){
            if(cNode.m_cvEnclosedRegions.get(i)==rNode) return true;
        }
        return false;
    }
    public Point getRegionPeak(Point pt){
        RegionNode rNode=getRegionNode(pt);
        if(rNode!=null) return rNode.peakLocation;
        return null;
    }
    public void pickBackgroundRegions_BackgroundDistribution(int[][] pixels, double mean, double sd, double pValue){
        calRegionPixelStatistics(pixels);
        mergeSimilarRegions(0.1);
        completeComplexes();
        calRegionComplexPixelStatistics(pixels);
        
        int i,j,len=m_cvRegionNodes.size();
        RegionNode r;
        for(i=0;i<len;i++){
            r=m_cvRegionNodes.get(i);
            if(r.complexIndex>0) continue;
            if(r.getT(mean, sd)>pValue) 
                r.background=true;
            else
                r.background=false;
        }
        RegionComplexNode c;
        for(i=0;i<m_cvComplexNodes.size();i++){
            c=m_cvComplexNodes.get(i);
            if(c.getTTest(mean,sd)>pValue)
                c.markRegionsAsBackground(true);
            else
                c.markRegionsAsBackground(false);
        }
    }   
    public void calRegionPixelStatistics(int[][] pixels){
        int i,j,len=m_cvRegionNodes.size();
        RegionNode r;
        for(i=0;i<len;i++){
            r=m_cvRegionNodes.get(i);
            calRegionPixelStatistics(r,pixels);
        }
    }
    public void calRegionComplexPixelStatistics(int[][] pixels){
        for(int i=0;i<m_cvComplexNodes.size();i++){
            calRegionComplexPixelStatistics(m_cvComplexNodes.get(i),pixels);
        }
    }
    public boolean[][] getBackground(){
        boolean[][] pbBackground=new boolean[h][w];
        int i,j,rIndex;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                rIndex=LandscapeAnalyzerPixelSorting.getRegionIndex(m_pnStamp[i][j]);
                pbBackground[i][j]=m_cvRegionNodes.get(rIndex-1).background;
            }
        }
        return pbBackground;
    }
    public void mergeSimilarRegions(double p){
        RegionNode r1,r2;
        ArrayList<RegionBorderSegmentNode> sbns;
        RegionBorderSegmentNode rbs;
        int i,j,len=m_cvBorderSegments.size();
        for(i=0;i<len;i++){
            rbs=m_cvBorderSegments.get(i);
            r1=m_cvRegionNodes.get(rbs.region1-1);
            r2=m_cvRegionNodes.get(rbs.region2-1);
            if(r1.getT(r2)>p) removeBorderSegment(rbs);
        }
        completeComplexes();
    }
    public ArrayList<RegionNode> getNeighboringRegions(RegionNode rNode){
        int i,len=rNode.boundarySegments.size(),regionID=rNode.regionIndex,nr;
        ArrayList<RegionNode> rNodes=new ArrayList();
        RegionBorderSegmentNode rbs;
        for(i=0;i<len;i++){
            rbs=rNode.boundarySegments.get(i);
            if(rbs.region1==regionID)
                nr=rbs.region2;
            else
                nr=rbs.region1;
            rNodes.add(m_cvRegionNodes.get(nr-1));
        }
        return rNodes;
    }
}
