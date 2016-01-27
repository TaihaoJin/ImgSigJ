/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import java.util.*;
import java.io.*;
import ij.IJ;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import BrainSimulations.DataClasses.AtlasVoxelNode;
import BrainSimulations.DataClasses.BrainStructureNameNode;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class Substructure {    
    BrainStructureNameNode StructureNode = new BrainStructureNameNode();
    int m_nPosInt,m_nNegInt;
    intRange m_xRange, m_yRange, m_zRange;
    float m_fScale;
    String m_sComments;
    float m_fVolume;
    int m_nNumVoxels;
    ArrayList <Region> regions;    
    boolean m_bScaleSet;
    boolean m_bVoxelsSorted;
    
    /* A brain substructure is composed of one or more regions. Each region is represented by a collection of voxels that are topologically
     connected. Such a structural unit is discribed by a collection of consecutive layers 
     with a unit depth (one layer of voxel). A given layer of a region may be composed of one or more 
     patches. Different patches of the same layer are not topologically connected. Each patch is considered to be composed of a collection of 
     consecutive stripes. Each stripe (unit hight and width) may be composed of more than one segments which
     are not topologically connected. In this program, each stripe is assumed to be paralle to x axis.
     There for the collection of x segments of a strip have the same y and z coordinates, but different ranges
     of x coordinages. The layers are assumed to be parallel to the xy plane. Therefore, all segments, stripes 
     and patches have the same z coordinates.*/
    class Segment{ 
        public intRange m_xRangeSegment;
        public int y0,z0;
        public int regionID,layerID,stripeID,segmentID,patchID;
        Segment(){
            y0=0;
            z0=0;
            regionID=0;
            layerID=0;
            stripeID=0;
            segmentID=0;
            patchID=0;
            m_xRangeSegment=new intRange();
        }
        
        Segment(int x, int y, int z){
            this();
            y0=y;
            z0=z;
            m_xRangeSegment=new intRange(x,x);
        }
        
        public void copySegment(Segment aSeg){
            aSeg.layerID=layerID;
            aSeg.m_xRangeSegment.resetRange();
            aSeg.m_xRangeSegment.mergeRanges(m_xRangeSegment);
            aSeg.patchID=patchID;
            aSeg.regionID=regionID;
            aSeg.segmentID=segmentID;
            aSeg.stripeID=stripeID;
            aSeg.y0=y0;
            aSeg.z0=z0;
         }
        
//        setIDS(int regionID, int layerID, int patchID, int stripeID, int )

        public Segment contains(int x, int y, int z)
        {
            if(z0!=z||y0!=y)
                return null;
            if(m_xRangeSegment.contains(x))
                return this;
            else 
                return null;
        }
        
        public Segment connected(int x, int y, int z)
        {
            if(z>=z0+1||z<=z0-1) return null;
            if(y>=y0+1||y<=y0-1) return null;
//            if(z!=z0) return null;
//            if(y!=y0) return null;
            if(m_xRangeSegment.overlapOrconnected(new intRange(x,x))) return this;
            return null;
        }
        
        public Segment connected(Segment aSeg)
        {
            if(aSeg.z0>=z0+1||aSeg.z0<=z0-1) return null;
            if(aSeg.y0>=y0+1||aSeg.y0<=y0-1) return null;
            if(m_xRangeSegment.overlapOrconnected(aSeg.m_xRangeSegment)) return this;
            return null;
        }
        
        public void expandSegment(Segment Seg2){
            if(m_xRangeSegment.getMin()>Seg2.m_xRangeSegment.getMin())m_xRangeSegment.setMin(Seg2.m_xRangeSegment.getMin());
            if(m_xRangeSegment.getMax()<Seg2.m_xRangeSegment.getMax())m_xRangeSegment.setMax(Seg2.m_xRangeSegment.getMax());
            adjustRangeX(this);
        }
        
        public void setLayerID(int ID){
            layerID=ID;
         }
        
        public void setRegionID(int ID){
            regionID=ID;
        }
        
        public void setPatchID(int ID){
            patchID=ID;
        }
        
        public void setStripeID(int ID){
            stripeID=ID;
        }
        
        public void setSegmentID(int ID){
            segmentID=ID;
        }
        
}
    
    //08822 need to implement merge methods for segment with a point or with a segment
    class Stripe{
        public int y0,z0;
        public int layerID,regionID,stripeID,patchID;
        public ArrayList <Segment> segments;
        public intRange m_xRangeStripe;
        Stripe(){
            y0=0;
            z0=0;
            layerID=0;
            regionID=0;
            stripeID=0;
            patchID=0;
            segments=new ArrayList <Segment>();
            m_xRangeStripe=new intRange();
        }
        
        public void copyStripe(Stripe aStripe){
            aStripe.layerID=layerID;
            aStripe.m_xRangeStripe.resetRange();
            aStripe.m_xRangeStripe.mergeRanges(m_xRangeStripe);
            aStripe.patchID=patchID;
            aStripe.regionID=regionID;                                                                                                                                                                                                                                                                                                                                                                                                                                                                
            aStripe.stripeID=stripeID;
            aStripe.y0=y0;
            aStripe.z0=z0;
            aStripe.segments.clear();
            int nSize=segments.size();
            for(int i=0;i<nSize;i++){
                Segment aSeg=new Segment();
                segments.get(i).copySegment(aSeg);
                aStripe.segments.add(aSeg);
            }
        }
        
        Stripe(int ID){
            this();
            stripeID=ID;
        }
        
        Stripe(int x, int y, int z){
            this();
            y0=y;
            z0=z;
            m_xRangeStripe.expandRanges(x, x);
            segments.add(new Segment(x,y,z));
        }
        
        public Segment contains(int x, int y, int z){
            if(z!=z0||y!=y0)
                return null;
            int nNumSeg=segments.size();
            Segment aSeg;
            for(int i=0;i<nNumSeg;i++){
                aSeg=segments.get(i).contains(x, y, z);
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
      
        
        public Segment connected(int x, int y, int z){
            int nNum=segments.size();
            Segment aSeg= new Segment();
            for(int i=0;i<nNum;i++){
                aSeg=segments.get(i).connected(x, y, z);
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
        
        public Segment connected(Segment aSeg0){
            int nNum=segments.size();
            Segment aSeg= new Segment();
            for(int i=0;i<nNum;i++){
                aSeg=segments.get(i).connected(aSeg0);
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
        
        public Segment getSegment(int segmentID){
            if(segmentID < 0||segmentID>=segments.size()) 
                return null;
            return segments.get(segmentID);                    
        }
        
        public void adjustSegID(){
            int nNum=segments.size();
            for(int i=0;i<nNum;i++){
                segments.get(i).segmentID=i;
            }
        }
        public void setRegionID(int ID){
           regionID=ID; 
           int nSize=segments.size();
           for(int i=0;i<nSize;i++){
               segments.get(i).regionID=ID;
           }               
        }
        
        public void setLayerID(int ID){
            layerID=ID;
            int nSize=segments.size();
            for(int i=0;i<nSize;i++){
                segments.get(i).setLayerID(ID);
            }
        }
        
        public void setPatchID(int ID){
            patchID=ID;
            int nSize=segments.size();
            for(int i=0;i<nSize;i++){
                segments.get(i).setPatchID(ID);
            }
        }
        
        public void setStripeID(int ID){
            stripeID=ID;
            int nSize=segments.size();
            for(int i=0;i<nSize;i++){
                segments.get(i).setStripeID(ID);
            }
        }
                
        public void expandStripe(Stripe aStripe){
            int min1=m_xRangeStripe.getMin();
            int min2=aStripe.m_xRangeStripe.getMin();
            int nSize=aStripe.segments.size();
            for(int i=0;i<nSize;i++){
                if(min1<min2) {
                    Segment aSeg=new Segment();
                    aStripe.segments.get(i).copySegment(aSeg);
                    segments.add(aSeg);
                }
                else {
                    Segment aSeg=new Segment();
                    aStripe.segments.get(nSize-1-i).copySegment(aSeg);
                    segments.add(0,aSeg);
                }
            }
            m_xRangeStripe.mergeRanges(aStripe.m_xRangeStripe);
            adjustSegID();
        }
    }
    
    class Patch{
        public int z0;
        public int regionID,layerID,patchID;
        public intRange m_xRangePatch;
        public intRange m_yRangePatch;
        public ArrayList <Stripe> stripes;
        
        Patch(){
            z0=0;
            m_xRangePatch=new intRange();
            m_yRangePatch=new intRange();
            stripes=new ArrayList <Stripe>();
        }
        
        Patch(int x, int y, int z){
            z0=z;
            m_xRangePatch=new intRange(x,x);
            m_yRangePatch=new intRange(y,y);
            stripes=new ArrayList <Stripe>();
            stripes.add(new Stripe(x,y,z));
        }
        
        public void copyPatch(Patch aPatch){
            aPatch.layerID=layerID;
            aPatch.m_xRangePatch.resetRange();
            aPatch.m_xRangePatch.mergeRanges(m_xRangePatch);
            aPatch.m_yRangePatch.resetRange();
            aPatch.m_yRangePatch.mergeRanges(m_yRangePatch);
            aPatch.patchID=patchID;
            aPatch.regionID=regionID;
            aPatch.z0=z0;
            aPatch.stripes.clear();
            int nSize=stripes.size();
            for(int i=0;i<nSize;i++){
                Stripe aStripe=new Stripe();
                stripes.get(i).copyStripe(aStripe);
                aPatch.stripes.add(aStripe);
            }
        }
        
        public Segment contains(int x,int y,int z){
            if(z0!=z)
                return null;
            if(!m_yRangePatch.contains(y)) return null;
            if(!m_xRangePatch.contains(x)) return null;
            
            return stripes.get(y-m_yRangePatch.getMin()).contains(x, y, z);
        }
        
        public Segment connected(int x, int y, int z){
            int nNumStripes=stripes.size();
            Segment aSeg= new Segment();
            for(int i=0;i<nNumStripes;i++){
                aSeg=stripes.get(i).connected(x, y, z);
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
        
        public Segment connected(Segment aSeg0){
            int nNum=stripes.size();
            Segment aSeg= new Segment();
            for(int i=0;i<nNum;i++){
                aSeg=stripes.get(i).connected(aSeg0);
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
        
        Segment getSegment(int stripeID,int segmentID){
            if(stripeID<0||stripeID>=stripes.size()) return null;
            return stripes.get(stripeID).getSegment(segmentID);
        }
        
        public void setRegionID(int ID){
           regionID=ID; 
           int nSize=stripes.size();
           for(int i=0;i<nSize;i++){
               stripes.get(i).setRegionID(ID);
           }               
        }
        
        public void setLayerID(int ID){
            layerID=ID;
            int nSize=stripes.size();
            for(int i=0;i<nSize;i++){
                stripes.get(i).setLayerID(ID);
            }
        }
        
        public void setPatchID(int ID){
            patchID=ID;
            int nSize=stripes.size();
            for(int i=0;i<nSize;i++){
                stripes.get(i).setPatchID(ID);
            }
        }
        
        
        public void expandPatch(Patch Patch2){//assuming the two patches are in the same layer.
            
            int min1=m_yRangePatch.getMin();
            int max1=m_yRangePatch.getMax();
            int min2=Patch2.m_yRangePatch.getMin();
            int max2=Patch2.m_yRangePatch.getMax();
            
            int nIndex=0, nIndex2;
            int nSize2=Patch2.stripes.size();
            for(int i=min1;i<=max1;i++){
                nIndex=i-min1;
                nIndex2=i-min2;
                if(nIndex2>=0&&nIndex2<nSize2){                    
                    stripes.get(nIndex).expandStripe(Patch2.stripes.get(nIndex2));
                }
            }
            
            for(int i=max1+1;i<=max2;i++){
                nIndex2=i-min2;
                Stripe aStripe=new Stripe();
                Patch2.stripes.get(nIndex2).copyStripe(aStripe);
                stripes.add(aStripe);
            }
            m_xRangePatch.mergeRanges(Patch2.m_xRangePatch);
            m_yRangePatch.mergeRanges(Patch2.m_yRangePatch);
            adjustStripeID();
        }
        
        public void adjustStripeID(){
            int nSize=stripes.size();
            for(int i=0;i<nSize;i++){
                stripes.get(i).setStripeID(i);
            }
        }
    }
    
    class Layer{
        public int regionID;
        public int layerID;
        public int z0;
        public intRange m_xRangeLayer;
        public intRange m_yRangeLayer;
        public ArrayList<Patch> patches;
        Layer(){
            z0=0;
            m_xRangeLayer=new intRange();
            m_yRangeLayer=new intRange();
            patches=new ArrayList<Patch>();
        }

        
        public void copyLayer(Layer aLayer){
            aLayer.layerID=layerID;
            aLayer.m_xRangeLayer.resetRange();
            aLayer.m_xRangeLayer.mergeRanges(m_xRangeLayer);
            aLayer.m_yRangeLayer.resetRange();
            aLayer.m_yRangeLayer.mergeRanges(m_yRangeLayer);
            aLayer.regionID=regionID;
            aLayer.z0=z0;
            aLayer.patches.clear();
            int nSize=patches.size();
            for(int i=0;i<nSize;i++){
                Patch aPatch=new Patch();
                patches.get(i).copyPatch(aPatch);
                aLayer.patches.add(aPatch);
            }
        }
        
        Layer(int x, int y, int z){
            z0=z;
            m_xRangeLayer=new intRange(x,x);
            m_yRangeLayer=new intRange(y,y);
            patches=new ArrayList<Patch>();
            patches.add(new Patch(x,y,z));
        }
        
        Layer(int layerID){
            this();
            setLayerID(layerID);
        }
        
        public Segment contains(int x, int y, int z)
        {
            if(z0!=z)
                return null;
            if(!m_xRangeLayer.contains(x)) return null;
            if(!m_yRangeLayer.contains(y)) return null;
            int nNum=patches.size();
            Segment aSeg;
            for(int i=0;i<nNum;i++){
                aSeg=patches.get(i).contains(x, y, z); 
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
        
        public Segment connected(int x, int y, int z){
            int nNumPatches=patches.size();
            Segment aSeg= new Segment();
            for(int i=0;i<nNumPatches;i++){
                aSeg=patches.get(i).connected(x, y, z);
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
        
        public Segment connected(Segment aSeg0){
            int nNumPatches=patches.size();
            Segment aSeg= new Segment();
            for(int i=0;i<nNumPatches;i++){
                aSeg=patches.get(i).connected(aSeg0);
                if(aSeg!=null) return aSeg;
            }
            return null;
        }
        
        Segment getSegment(int patchID, int stripeID,int segmentID){
            if(patchID<0||patchID>=patches.size()) return null;
            return patches.get(stripeID).getSegment(stripeID,segmentID);
        }
        
        public void setRegionID(int ID){
           regionID=ID; 
           int nSize=patches.size();
           for(int i=0;i<nSize;i++){
               patches.get(i).setRegionID(ID);
           }               
        }
        
        public void setLayerID(int ID){
            layerID=ID;
            int nSize=patches.size();
            for(int i=0;i<nSize;i++){
                patches.get(i).setLayerID(ID);
            }
        }

        public void expandLayer(Layer aLayer){
            int nSize=aLayer.patches.size();
            int nSize0=patches.size();
            for(int i=0;i<nSize;i++){
                Patch aPatch=new Patch();
                aLayer.patches.get(i).copyPatch(aPatch);
                patches.add(aPatch);
                patches.get(nSize0+i).setPatchID(nSize0+i);
            }
            m_xRangeLayer.mergeRanges(aLayer.m_xRangeLayer);
            m_yRangeLayer.mergeRanges(aLayer.m_yRangeLayer);            
        }
        
        public void adjustPatchID(){
            int nSize=patches.size();
            for(int i=0;i<nSize;i++){
                patches.get(i).setPatchID(i);
            }
        }
    }
    
    class Region{
        public intRange m_xRangeRegion;
        public intRange m_yRangeRegion;
        public intRange m_zRangeRegion;
        public ArrayList<Layer> layers;
        public int regionID;
        
        Region(){
            m_xRangeRegion=new intRange();
            m_yRangeRegion=new intRange();
            m_zRangeRegion=new intRange();
            layers=new ArrayList<Layer>();
            regionID=0;
        }
        
        Region(int x, int y, int z){
            regionID=0;
            m_xRangeRegion=new intRange(x,x);
            m_yRangeRegion=new intRange(y,y);
            m_zRangeRegion=new intRange(z,z);
            layers=new ArrayList<Layer>();
            layers.add(new Layer(x,y,z));
        }
        
        public Segment contains(int x, int y, int z){
            if(!m_zRangeRegion.contains(z))return null;            
            if(!m_yRangeRegion.contains(y))return null;            
            if(!m_xRangeRegion.contains(x))return null;            
            return layers.get(z-m_zRangeRegion.getMin()).contains(x, y, z);
        }
        
        Segment getSegment(int layerID, int patchID, int stripeID,int segmentID){
            if(layerID<0||layerID>=layers.size()) return null;
            return layers.get(layerID).getSegment(patchID,stripeID,segmentID);
        }
        
        public void setRegionID(int ID){
           regionID=ID; 
           int nSize=layers.size();
           for(int i=0;i<nSize;i++){
               layers.get(i).setRegionID(ID);
           }               
        }
        
        public Layer getLayer(int ID){
            if(ID<0||ID>layers.size()) return null;
            return layers.get(ID);
        }
        
        public void expandRegion(Region Region2){//Assuming Region2 is higher than this region, arranged in the method mergeRegions()
            int min1=m_zRangeRegion.getMin();
            int max1=m_zRangeRegion.getMax();
            int min2=Region2.m_zRangeRegion.getMin();
            int max2=Region2.m_zRangeRegion.getMax();
            int nSize2=Region2.layers.size();
            
            int nIndex=0,nIndex2=0;
            for(int i=min1;i<=max1;i++){
                nIndex=i-min1;
                nIndex2=i-min2;
                if(nIndex2>=0&&nIndex2<nSize2){
                    layers.get(nIndex).expandLayer(Region2.layers.get(nIndex2));
                }
                layers.get(nIndex).setLayerID(nIndex);
            }
            
            for(int i=max1+1;i<=max2;i++){
                nIndex2=i-min2;
                nIndex=i-min1;
                if(nIndex2>=0&&nIndex2<nSize2){
                    Layer aLayer=new Layer();
                    Region2.layers.get(nIndex2).copyLayer(aLayer);
                    layers.add(aLayer);
                }
                layers.get(nIndex).setLayerID(nIndex);
            }
            m_xRangeRegion.mergeRanges(Region2.m_xRangeRegion);
            m_yRangeRegion.mergeRanges(Region2.m_yRangeRegion);
            m_zRangeRegion.mergeRanges(Region2.m_zRangeRegion);
        }
    }
    
    public void adjustRangeX(Segment aSeg){
        m_xRange.mergeRanges(aSeg.m_xRangeSegment);
        getRegion(aSeg).m_xRangeRegion.mergeRanges(aSeg.m_xRangeSegment);
        getLayer(aSeg).m_xRangeLayer.mergeRanges(aSeg.m_xRangeSegment);
        getPatch(aSeg).m_xRangePatch.mergeRanges(aSeg.m_xRangeSegment);
        getStripe(aSeg).m_xRangeStripe.mergeRanges(aSeg.m_xRangeSegment);
    }
    
    public Region getRegion(Segment aSeg){
        return regions.get(aSeg.regionID);
    }
    
    public Layer getLayer(Segment aSeg){
        return regions.get(aSeg.regionID).layers.get(aSeg.layerID);
    }
    
    
    public void setScale(float fScale)
    {
        m_fScale=fScale;
    }
    public Substructure(){
        m_fScale=1.f;
        m_sComments="";
        m_nPosInt=2147483647;
        m_nNegInt=-2147483648;
        m_xRange=new intRange();
        m_yRange=new intRange();
        m_zRange=new intRange();
        regions=new ArrayList <Region>();
        m_bScaleSet=false;
        m_bVoxelsSorted=false;
    }
    public Segment contains(int x, int y, int z){
        if(!m_xRange.contains(x)) return null;
        if(!m_yRange.contains(y)) return null;
        if(!m_zRange.contains(z)) return null;
        Segment aSeg;
        int nNum=regions.size();
        for(int i=0;i<nNum;i++)
        {
            aSeg=regions.get(i).contains(x, y, z);
            if(aSeg!=null) return aSeg;
        }
        return null;
    }
    
    public Segment getSegment(int regionID, int layerID, int patchID, int stripeID, int segmentID)
    {
        if(regionID<0||regionID>=regions.size()) return null;
        return regions.get(regionID).getSegment(layerID,patchID,stripeID,segmentID);
    }
    
    public Segment createSegment(int x, int y, int z){
        
        int regionID=regions.size();
        Region aRegion=new Region(x,y,z);
        m_xRange.expandRanges(x, x);
        m_yRange.expandRanges(y, y);
        m_zRange.expandRanges(z, z);
        aRegion.setRegionID(regionID);
        regions.add(aRegion);
        return(getSegment(regionID,0,0,0,0));
    }
    public Stripe getStripe(Segment aSeg){
        return regions.get(aSeg.regionID).layers.get(aSeg.layerID).patches.get(aSeg.patchID).stripes.get(aSeg.stripeID);
    }
    
    public void removeSegment(Segment aSeg){
        Stripe aStripe=getStripe(aSeg);
        aStripe.segments.remove(aSeg.segmentID);
        aStripe.adjustSegID();
    }
  
    public void adjustRegionID(){
        int nNum=regions.size();
        for(int i=0;i<nNum;i++)
        {
            regions.get(i).setRegionID(i);
        }
    }
    
    public void mergeRegions(int regionID1, int regionID2){
        Region region1=regions.get(regionID1);
        Region region2=regions.get(regionID2);
        if(region1.m_zRangeRegion.getMin()<region2.m_zRangeRegion.getMin()){
            region1.expandRegion(region2);
            regions.remove(regionID2);
        }
        else{
            region2.expandRegion(region1);
            regions.remove(regionID1);
        }
        adjustRegionID();
    }
    
    public Patch getPatch(Segment aSeg){
        return regions.get(aSeg.regionID).layers.get(aSeg.layerID).patches.get(aSeg.patchID);
    }
    
    public void mergePatches(Segment Seg1, Segment Seg2){
        Patch patch1=getPatch(Seg1);        
        Patch patch2=getPatch(Seg2);
        if(patch1.m_yRangePatch.getMin()<=patch2.m_yRangePatch.getMin()){            
            patch1.expandPatch(patch2);
            regions.get(Seg2.regionID).layers.get(Seg2.layerID).patches.remove(Seg2.patchID);
        }else{
            patch2.expandPatch(patch1);
            regions.get(Seg1.regionID).layers.get(Seg1.layerID).patches.remove(Seg1.patchID);
        }
        regions.get(Seg2.regionID).layers.get(Seg2.layerID).adjustPatchID();
    }
                
    public void addVoxl(int x, int y, int z){
        Segment aSeg=contains(x, y, z);
        if(m_nNumVoxels==105){
            x=x;
        }
        int dx,dy,dz;
        boolean b=true;
        if(aSeg==null){
            aSeg=createSegment(x, y, z);
            for(int i=0;i<3;i++){
                for(int j=0;j<3;j++){
                    for(int k=0;k<3;k++){
                        dx=i-1;
                        dy=j-1;
                        dz=k-1;
                        if(!(dx==0&&dy==0&&dz==0)){
                            b=combineSegments(x,y,z,x+dx,y+dy,z+dz);
                        }
                    }
                }
            }
        }
        m_fVolume+=1.f;
        m_nNumVoxels++;
    }    
    public boolean combineSegments(int x1, int y1, int z1, int x2, int y2, int z2){
        Segment Seg1=contains(x1,y1,z1);
        Segment Seg2=contains(x2,y2,z2);
        
        if(Seg1==null||Seg2==null) return false;
        
        if(Seg1.regionID!=Seg2.regionID) {
            mergeRegions(Seg1.regionID,Seg2.regionID);
            Seg1=contains(x1,y1,z1);
            Seg2=contains(x2,y2,z2);//now Seg1 and Seg2 have the same regionID and layerID
        }
        
        if(Seg1==null||Seg2==null){
            Seg1=Seg1;
        }
        if(Seg1.patchID!=Seg2.patchID&&z1==z2) {
            mergePatches(Seg1,Seg2);
            Seg1=contains(x1,y1,z1);
            Seg2=contains(x2,y2,z2); //now Seg1 and Seg2 have the same regionID, layerID, and patchID
        }
        
        if(Seg1.segmentID!=Seg2.segmentID&&y1==y2&&z1==z2){
            Seg1.expandSegment(Seg2); //The two segments must be in the same stripe.
            removeSegment(Seg2);
            getStripe(Seg1).adjustSegID();
        }
        return true;
    }
    public void setSubstructureName(BrainStructureNameNode a){
        StructureNode.Abbreviation=a.Abbreviation;
        StructureNode.ParentStruct=a.ParentStruct;
        StructureNode.StructureId=a.StructureId;
        StructureNode.StructureName=a.StructureName;
        StructureNode.blue=a.blue;
        StructureNode.green=a.green;
        StructureNode.informaticsId=a.informaticsId;
        StructureNode.red=a.red;
    }
    
    public void SetComments(String sComments){
        m_sComments=sComments;
        ComputeScale();
    }
    
    public void ComputeScale(){
        int n=0;
        n=getLastInteger(m_sComments);
        if(n>0){
            m_fScale=(float) getLastInteger(m_sComments);
            m_bScaleSet=true;
        }
        else m_bScaleSet=false;
    }
    
    public int getLastInteger(String st){
        int nLength=st.length();
        if(nLength<=0)return -1;
        int nIndex=nLength-1;
        char ch=st.charAt(nIndex);
        while(!isNumber(ch)&&nIndex>0){
            nIndex--;
            ch=st.charAt(nIndex);
        }
        int factor=1;
        int nNum=0;
        while(isNumber(ch)){
            nNum+=factor*(ch-48);
            nIndex--;
            ch=st.charAt(nIndex);
            factor*=10;
        }
        return nNum;
    }
    
    boolean isNumber(char c){
        return (c>=48&&c<=57);
    }
    
    public int getPixel(float x, float y, float z){        
        int pixel=(255<<24)|(255<<16)|(255<<8)|255;
        if(contains(x,y,z)){
            pixel=getPixel();          
        }
        return pixel;
    }
    
    int getPixel(){
        int transparency=255;
        int pixel=0;
//      int pixel=(transparency<<24)|(StructureNode.red<<16)|(StructureNode.green<<8)|StructureNode.blue;
        pixel=(StructureNode.red<<16)|(StructureNode.green<<8)|StructureNode.blue;
        return  pixel;
    }
    
    public boolean contains(float xf, float yf, float zf){
        if(!m_bScaleSet) ComputeScale();
        if(m_bScaleSet){
            int x=(int) (xf/m_fScale);
            int y=(int) (yf/m_fScale);
            int z=(int) (zf/m_fScale);
            if(contains(x,y,z)!=null) return true;
        }
        return false;
    }
    
    public void setColor(int pixel){
        StructureNode.red=0xff & (pixel>>16);
        StructureNode.green=0xff & (pixel>>8);
        StructureNode.blue=0xff & pixel;
    }
    
    public intRange getXRange(){
        intRange ir=new intRange();
        ir.mergeRanges(m_xRange);
        return ir;
    }
    
    public intRange getYRange(){
        intRange ir=new intRange();
        ir.mergeRanges(m_yRange);
        return ir;
    }
    
    public intRange getZRange(){
        intRange ir=new intRange();
        ir.mergeRanges(m_zRange);
        return ir;
    }
    
    public float getScale(){
        if(!m_bScaleSet) ComputeScale();
        if(m_bScaleSet){
            return m_fScale;
        }else return -1.f;
    }    
    
    public void setVoxelsSorted(boolean bSorted){
        m_bVoxelsSorted=bSorted;
    }
}



