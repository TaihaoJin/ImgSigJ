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
import BrainSimulations.DataClasses.*;
import java.util.Formatter;
import utilities.QuickFormatter;
import utilities.ArrayofArrays.*;
import utilities.FormattedReader;
import utilities.CustomDataTypes.intRange;



/**
 *
 * @author Taihao
 */
public class SubstructureSorted {    
    public static String newline = System.getProperty("line.separator");
    BrainStructureNameNode StructureNode = new BrainStructureNameNode();
    int m_nPosInt,m_nNegInt;
    intRange m_xRange, m_yRange, m_zRange;
    float m_fScale;
    String m_sComments;
    float m_fVolume;
    int m_nNumVoxels;
//    ArrayList <Region> regions;    
    boolean m_bScaleSet;
    boolean m_bVoxelsSorted;
    ArrayList <Segment> segments;
    IntArray segmentIndexes;
    IntArray2 stripeIndexes;
    IntArray3 layerIndexes;
    intRange m_zRangesStripe[];
    intRange m_xRangesStripe[];
    intRange m_zRangesSegment[];
    intRange m_yRangesSegment[];
 
    int m_ncSegment;
    int m_ncLayer;
    int m_ncStripe;
    int m_npSegment;
    int m_npLayer;
    int m_npStripe;            
    int m_nNumSegments;
    ArrayList <intRange> m_xRangesLayer;
    ArrayList <intRange> m_yRangesLayer;
    intRange xRange;
    intRange yRange;
    Segment currentSegment;
    ArrayList<region> m_regions;
        
    /* A brain substructure is composed of one or more regions. Each region is represented by a collection of voxels that are topologically
     connected. Such a structural unit is discribed by a collection of consecutive layers 
     with a unit depth (one layer of voxel). A given layer of a region may be composed of one or more 
     patches. Different patches of the same layer are not topologically connected. Each patch is considered to be composed of a collection of 
     consecutive stripes. Each stripe (unit hight and width) may be composed of more than one segments which
     are not topologically connected. In this program, each stripe is assumed to be paralle to x axis.
     There for the collection of x segments of a strip have the same y and z coordinates, but different ranges
     of x coordinages. The layers are assumed to be parallel to the xy plane. Therefore, all segments, stripes 
     and patches have the same z coordinates.*/
    public SubstructureSorted(){
        m_fScale=1.f;
        m_sComments="";
        m_nPosInt=2147483647/4;
        m_nNegInt=-2147483648/4;
        m_xRange=new intRange();
        m_yRange=new intRange();
        m_zRange=new intRange();
        segments=new ArrayList <Segment>();
        m_bScaleSet=false;
        m_bVoxelsSorted=false;
        m_xRangesLayer=new ArrayList <intRange>();
        m_yRangesLayer=new ArrayList <intRange>();
        
        m_nNumSegments=0;
    }
    class Segment{ 
        public intRange m_xRangeSegment;
        public int y0,z0;
        public int layerID,stripeID,segmentID;
        IDNode regionID;
        IDNode patchID;
        Segment(){
            y0=0;
            z0=0;
            layerID=0;
            stripeID=0;
            segmentID=0;
            m_xRangeSegment=new intRange();
            patchID=new IDNode();
            regionID=new IDNode();
        }
        
        public int getRegionID(){
            if(regionID!=null){
                return regionID.getID();
            }
            return -1;
        }
        
        Segment(int x, int y, int z){
            this();
            y0=y;
            z0=z;
            m_xRangeSegment=new intRange(x,x);
        }
        
        public void expandSegment(int x1, int x2){
            m_xRangeSegment.expandRanges(x1, x2);
        }
        
        public intRange getRange(){
            return new intRange(m_xRangeSegment.getMin(),m_xRangeSegment.getMax());
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
        
        public Segment getSegment(int segmentID){
            return this;
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
        
        
        public void expandSegment(Segment Seg2){
            if(m_xRangeSegment.getMin()>Seg2.m_xRangeSegment.getMin())m_xRangeSegment.setMin(Seg2.m_xRangeSegment.getMin());
            if(m_xRangeSegment.getMax()<Seg2.m_xRangeSegment.getMax())m_xRangeSegment.setMax(Seg2.m_xRangeSegment.getMax());
            adjustRangeX(this);
        }
        
        public void setLayerID(int ID){
            layerID=ID;
         }
        
        public void setRegionID(IDNode ID){
            regionID=ID;
        }
        
        public void setPatchID(IDNode ID){
            patchID=ID;
        }
        
        public void setStripeID(int ID){
            stripeID=ID;
        }
        
        public void setSegmentID(int ID){
            segmentID=ID;
        }
        
}
        
    public void adjustRangeX(Segment aSeg){
        m_xRange.mergeRanges(aSeg.m_xRangeSegment);
    }
        
    
    public void setScale(float fScale)
    {
        m_fScale=fScale;
    }
    
    public Segment contains(int x, int y, int z){
        if(!m_xRange.contains(x)) return null;
//        if(!m_yRange.contains(y)) return null;
        if(!m_zRange.contains(z)) return null;
        int layerIndex=z-m_zRange.getMin();
        if(!m_yRangesLayer.get(layerIndex).contains(y)) return null;
        int stripeIndex=y-m_yRangesLayer.get(layerIndex).getMin();
        stripeIndexes=layerIndexes.m_IntArray3.get(layerIndex);
        if(stripeIndexes.m_IntArray2.size()<=0) return null;
        segmentIndexes=stripeIndexes.m_IntArray2.get(stripeIndex);
        int nSize=segmentIndexes.m_intArray.size();
        int index;
        Segment aSeg=null;
        for(int i=0;i<nSize;i++){
            index=segmentIndexes.m_intArray.get(i);
            aSeg=segments.get(index).contains(x, y, z);
            if(aSeg!=null){
                return aSeg;
            }
        }
        return null;
    }
             
    public void addVoxl(int x, int y, int z){
        if(m_nNumVoxels==81){
            m_nNumVoxels=m_nNumVoxels;
        }
        if(m_nNumSegments==0) {
            FirstSegment(x,y,z);
        }else{
             if(currentLayer(x,y,z)){
                if(currentStripe(x,y,z)){
                    if(currentSegment.getRange().getMax()==(x-1)){
                        currentSegment.expandSegment(x,x);
                    }else{
                        closeSegment();
                        newSegment(x,y,z);
                    }
                }else{
                    closeStripe();
                    newStripe(x,y,z);
                }
            }else{
                closeLayer(); 
                newLayer(x,y,z);
            }
        }  
        m_nNumVoxels++;
        m_fVolume+=1.f;
        m_bScaleSet=false;
    }
    
    public boolean scaleSet() {
        return m_bScaleSet;
    }
    
    boolean currentStripe(int x, int y, int z){
        m_ncStripe=y-yRange.getMin();
        if(m_ncStripe==m_npStripe) return true;
        return false;
    }
    
    boolean currentLayer(int x, int y, int z){
        m_ncLayer=z-m_zRange.getMin();
        if(m_ncLayer==m_npLayer) return true;
        return false;
    }
    
    void FirstSegment(int x, int y, int z){
        m_xRange.setMin(x);
        m_yRange.setMin(y);
        m_zRange.setMin(z);
        m_nNumSegments=0;
        m_ncLayer=0;
        m_ncStripe=0;
        m_npLayer=0;
        m_npStripe=0;
        xRange=new intRange();
        yRange=new intRange();
        xRange.setMin(x);
        yRange.setMin(y);
        segmentIndexes=new IntArray();
        stripeIndexes=new IntArray2();
        layerIndexes=new IntArray3();
        newSegment(x,y,z);
    }
    
    void closeSegment(){
        segments.add(currentSegment);
        segmentIndexes.m_intArray.add(m_ncSegment);
        m_ncSegment++;
    }
    
    void newSegment(int x, int y, int z){
        currentSegment=new Segment(x,y,z);
        currentSegment.setLayerID(m_ncLayer);
        currentSegment.setSegmentID(m_ncSegment);
        currentSegment.setStripeID(m_ncStripe);
        m_nNumSegments++;
     }
    
    void closeStripe(){
        closeSegment();
        stripeIndexes.m_IntArray2.add(segmentIndexes);
        xRange.expandRange(currentSegment.getRange());
    }
    
    void newStripe(int x, int y, int z){
        segmentIndexes=new IntArray();
        m_ncStripe=y-yRange.getMin();
        for(int i=m_npStripe+1;i<m_ncStripe;i++){
            stripeIndexes.m_IntArray2.add(segmentIndexes);
            segmentIndexes=new IntArray();
        }
        m_npStripe=m_ncStripe;
        xRange.expandRange(new intRange(x,x));
        newSegment(x,y,z);
    }
    
    void closeLayer(){
        closeStripe();
        layerIndexes.m_IntArray3.add(stripeIndexes);
        m_xRange.expandRange(xRange);
        m_xRangesLayer.add(xRange);
        int y=m_ncStripe+yRange.getMin();
        yRange.expandRanges(y,y);
        m_yRangesLayer.add(yRange);
        m_yRange.expandRange(yRange);
        m_zRange.setMax(currentSegment.z0);
    }
    
    public void closeSubstructure(){
        closeLayer();
        m_ncSegment--;
    }
    
    void newLayer(int x, int y, int z){
        stripeIndexes=new IntArray2();
        xRange=new intRange();
        yRange=new intRange();
        for(int i=m_npLayer+1; i<m_ncLayer; i++){
            layerIndexes.m_IntArray3.add(stripeIndexes);
            m_xRangesLayer.add(xRange);
            m_yRangesLayer.add(yRange);
            stripeIndexes=new IntArray2();
            xRange=new intRange();
            yRange=new intRange();
        }
        xRange=new intRange();
        yRange=new intRange();
        xRange.setMin(x);
        yRange.setMin(y);
        m_npLayer=m_ncLayer;
        newStripe(x,y,z);
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
        while(isNumber(ch)&&nIndex>0){
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
    
    class Patch{
        int z0;
        intRange m_xRangePatch;
        intRange m_yRangePatch;
        IntArray2 segmentIndexes_Patch;
        public Patch(){
            m_xRangePatch=new intRange();
            m_yRangePatch=new intRange();
            segmentIndexes_Patch=new IntArray2();
        }
        public void addSegment(int index){
            int stripeIndex=0;
        }
    }
    
    
   
    
    
    IntArray segmentIndexesP;
    IntArray2 stripeIndexesP;
    IntArray3 patchIndexes;
    IntArray4 regionIndexes;
    IDCollectionNode patchNodes;//The data member of this object contain all independent reference of patchID of all segments in the same patch;
    IDCollectionNodeArray LayerNodes;//The data member of this object contains a list of all patcheNodes in the same layer
    IDCollectionNode regionNodes;//
    ArrayList <Integer> patchNumbers=new ArrayList<Integer>();
    ArrayList <Integer> regionNumbers=new ArrayList<Integer>();
    
    public void contructTopology(){
        int yMin,yMax,zMin,zMax,nSize;
        int currentPatch=0;
        int currentRegion=0;
        zMin=m_zRange.getMin();
        zMax=m_zRange.getMax();
        Segment aSeg;
        for(int layer=0;layer<=zMax-zMin;layer++){
            patchNumbers.clear();
            currentPatch=0;
            yMin=m_yRangesLayer.get(layer).getMin();
            yMax=m_yRangesLayer.get(layer).getMax();
            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
            for(int stripe=0;stripe<=yMax-yMin;stripe++)
            {
                if(stripeIndexes.m_IntArray2.size()==0) continue;
                try{segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);}
                catch (IndexOutOfBoundsException e){
                    e=e;
                }
                nSize=segmentIndexes.m_intArray.size();
                for(int seg=0;seg<nSize;seg++){
                    aSeg=segments.get(segmentIndexes.m_intArray.get(seg));
                    if(!currentPatch(layer,stripe,aSeg)){
                        aSeg.patchID=new IDNode(currentPatch);
                        patchNumbers.add(1);
                        currentPatch++;

                        if(!currentRegion(layer,stripe+yMin,aSeg)) {
                                aSeg.regionID=new IDNode(currentRegion);
                                regionNumbers.add(1);
                                currentRegion++;
                        }
                    }else
                    {
                        if(layer>0)combineRegions(layer,stripe+yMin,aSeg);
                    }
                }
            }
            addjustPatchID(layer);
        }
        addjustRegionID();
    }
    void addjustRegionID(){
        int nSize=regionNumbers.size();
        ArrayList <Integer> regionNumbers1=new ArrayList<Integer>();
        int nNumRegions=0;
        for(int i=0;i<nSize;i++){
            regionNumbers1.add(nNumRegions);
            nNumRegions+=regionNumbers.get(i);
        }

        int yMin,yMax,zMin,zMax;
        zMin=m_zRange.getMin();
        zMax=m_zRange.getMax();
        Segment aSeg;
        int index;
        for(int layer=0;layer<=zMax-zMin;layer++){
            yMin=m_yRangesLayer.get(layer).getMin();
            yMax=m_yRangesLayer.get(layer).getMax();
            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
            for(int stripe=0;stripe<=yMax-yMin;stripe++)
            {
                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
                nSize=segmentIndexes.m_intArray.size();
                for(int seg=0;seg<nSize;seg++){
                    index=segmentIndexes.m_intArray.get(seg);
                    aSeg=segments.get(index);
                    int id=aSeg.regionID.getID();
                    aSeg.regionID.setID(regionNumbers1.get(id));
                 }
            }
        }
    }
    
    void addjustPatchID(int layer){
        int nSize=patchNumbers.size();
        ArrayList <Integer> patchNumbers1=new ArrayList<Integer>();
        int nNumPatches=0;
        for(int i=0;i<nSize;i++){
            patchNumbers1.add(nNumPatches);
            nNumPatches+=patchNumbers.get(i);
        }            
        int yMin=m_yRangesLayer.get(layer).getMin();
        int yMax=m_yRangesLayer.get(layer).getMax();
        
        int index=0;
        Segment aSeg;
        int id;
        stripeIndexes=layerIndexes.m_IntArray3.get(layer);
        for(int stripe=0;stripe<=yMax-yMin;stripe++)
        {
//            if(stripeIndexes.m_IntArray2.size()==0) continue;
            segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
            nSize=segmentIndexes.m_intArray.size();
            for(int seg=0;seg<nSize;seg++){
                aSeg=segments.get(segmentIndexes.m_intArray.get(seg));
                id=aSeg.patchID.getID();
                aSeg.patchID.setID(patchNumbers1.get(id));
            }
        }
        patchNumbers.clear();
    }
    
    
    public void combineRegions(int layer0,int y0, Segment aSeg){
        intRange aRange=aSeg.getRange();
        Segment bSeg;
        
        int yMin=m_yRangesLayer.get(layer0-1).getMin();
        int yi=y0-1;
        
        if(yi<yMin) yi=yMin;
        int yMax=m_yRangesLayer.get(layer0-1).getMax();
        int yf=y0+1;
        if(yf>yMax) yf=yMax;
        
        int index;
        int stripe,nSize;
        
        for(int y=yi;y<=yf;y++ ){
            stripe=y-yMin;
            segmentIndexesP = layerIndexes.m_IntArray3.get(layer0-1).m_IntArray2.get(stripe);
            nSize=segmentIndexesP.m_intArray.size();
            for(int seg=0;seg<nSize;seg++){
                index=segmentIndexesP.m_intArray.get(seg);
                bSeg=segments.get(index);
                if(aRange.overlapOrconnected(bSeg.getRange())){
                    if(aSeg.regionID.getID()!=bSeg.regionID.getID()) {
                        regionNumbers.set(bSeg.regionID.getID(), 0);
                        bSeg.regionID.setID(aSeg.regionID.getID());
                    }
                }
            }
        }
    }
    public boolean currentRegion(int layer0, int y0, Segment aSeg){
        if(layer0==0) return false;
        intRange aRange=aSeg.getRange();
        Segment bSeg;
        
        int yMin=m_yRangesLayer.get(layer0-1).getMin();
        if(yMin>y0+1) return false;
        
        int yi=y0-1;
        if(yi<yMin) yi=yMin;
        int yMax=m_yRangesLayer.get(layer0-1).getMax();
        int yf=y0+1;
        if(yf>yMax) yf=yMax;
        int index;
        int nOverlaps=0;
        int stripe,nSize;
        
        for(int y=yi;y<=yf;y++ ){
            stripe=y-yMin;
//            if(layerIndexes.m_IntArray3.size()<=(layer0-1)||layerIndexes.m_IntArray3.get(layer0-1).m_IntArray2.size()<=stripe) continue;
            segmentIndexesP = layerIndexes.m_IntArray3.get(layer0-1).m_IntArray2.get(stripe);
            nSize=segmentIndexesP.m_intArray.size();
            for(int seg=0;seg<nSize;seg++){
                index=segmentIndexesP.m_intArray.get(seg);
                bSeg=segments.get(index);
                if(aRange.overlapOrconnected(bSeg.getRange())){
                    if(nOverlaps==0){
                        aSeg.regionID=bSeg.regionID;
                    }else{
                        if(aSeg.regionID.getID()!=bSeg.regionID.getID()){
                            regionNumbers.set(bSeg.regionID.getID(), 0);
                            bSeg.regionID.setID(aSeg.regionID.getID());
                        }
                    }
                    nOverlaps++;
                }
            }
        }
        if(nOverlaps>0) return true;
        return false;
    }
        
    public boolean currentPatch(int layer0, int stripe0, Segment aSeg){
        if(stripe0==0) return false;
        intRange aRange=aSeg.getRange();
        Segment bSeg;
        segmentIndexesP = layerIndexes.m_IntArray3.get(layer0).m_IntArray2.get(stripe0-1);
        int nSize=segmentIndexesP.m_intArray.size();
        int index;
        int nOverlaps=0;
        for(int seg=0;seg<nSize;seg++){
            index=segmentIndexesP.m_intArray.get(seg);
            bSeg=segments.get(index);
            if(aRange.overlapOrconnected(bSeg.getRange())){
                if(nOverlaps==0){
                    aSeg.patchID=bSeg.patchID;
                    aSeg.regionID=bSeg.regionID;
                }else{
                    if(aSeg.patchID.getID()!=bSeg.patchID.getID()){
                        patchNumbers.set(bSeg.patchID.getID(), 0);
                        bSeg.patchID.setID(aSeg.patchID.getID());
                    }
                    if(aSeg.regionID.getID()!=bSeg.regionID.getID()) {
                        regionNumbers.set(bSeg.regionID.getID(), 0);
                        bSeg.regionID.setID(aSeg.regionID.getID());
                    }
                }
                nOverlaps++;
            }
        }
        if(nOverlaps>0) return true;
        return false;
    }  
    public void writeSubstructure(Formatter fm){
        int nSize=segments.size();
        Segment aSeg;
        int layers,stripes,layer,stripe,segs,seg,index;
        layers=layerIndexes.m_IntArray3.size();
        for(layer=0;layer<layers;layer++){
            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
            stripes=stripeIndexes.m_IntArray2.size();
            for(stripe=0;stripe<stripes;stripe++){
                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
                segs=segmentIndexes.m_intArray.size();
                for(seg=0;seg<segs;seg++){
                   index=segmentIndexes.m_intArray.get(seg);
                    aSeg=segments.get(index);
                    try{fm.format("layer: %d,  stripe: %d,  segment: %d,  patch: %d, region: %d  z: %d  y: %d  x: %d-%d Stripe: %d Layer: %d%s", aSeg.layerID,aSeg.stripeID,aSeg.segmentID,aSeg.patchID.getID(),aSeg.regionID.getID(),aSeg.z0,aSeg.y0,aSeg.m_xRangeSegment.getMin(),aSeg.m_xRangeSegment.getMax(),stripe,layer,newline);}
                    catch (NullPointerException e ){
                        e=e;
                    }
                }
             }
           }
        
        
        //        for(int i=0;i<nSize;i++){
 //           aSeg=segments.get(i);
  //          fm.format("layer: %d,  stripe: %d,  segment: %d,  patch: %d, region: %d  z: %d  y: %d  x: %d-%d%s", aSeg.layerID,aSeg.stripeID,aSeg.segmentID,aSeg.patchID.getID(),aSeg.regionID.getID(),aSeg.z0,aSeg.y0,aSeg.m_xRangeSegment.getMin(),aSeg.m_xRangeSegment.getMax(),newline);
 //       }
        
        fm.format("%s",newline);        
        fm.format("%s",newline);        
        fm.format("%s",newline);
        nSize=m_regions.size();
        for(int i=0;i<nSize;i++){
            m_regions.get(i).writeRegion(fm);
        }
    }
    
    void buildRegions(){
        if(m_regions!=null) m_regions.clear();
        m_regions=new ArrayList<region> ();
        m_regions.add(new region());
        int cRegion=0;
        int region;
        Segment aSeg;
        int nSize=segments.size();
        int regionID;
        for(int i=0;i<nSize;i++){
            aSeg=segments.get(i);
            if(aSeg!=null){
                regionID=aSeg.getRegionID();
                if(regionID>=0){
                    if(regionID>cRegion) moreRegions(regionID);
                    m_regions.get(regionID).addSegment(aSeg);
                }
            }
        }
        
        nSize=m_regions.size();
        for(int i=0;i<nSize;i++){
            m_regions.get(i).completeRegionBuilding();
        }
        
    }
    
    void moreRegions(int regionID){
        int pRegion=m_regions.size()-1;
        for(int i=pRegion+1;i<=regionID;i++){
            region aregion=new region();
            m_regions.add(aregion);
        }
    }
    

    class region{
        intRange m_xRangeR;
        intRange m_yRangeR;
        intRange m_zRangeR;
        IntRangeArray m_xRangesLayerR;
        IntRangeArray m_yRangesLayerR;
        IntArray m_segmentIndexesR;
        IntArray2 m_stripeIndexesR;
        IntArray3 m_layerIndexesR;
        ArrayList <Segment> m_segmentsR;
        
        public region(){
            m_xRangeR=new intRange();
            m_yRangeR=new intRange();
            m_zRangeR=new intRange();
            m_layerIndexesR=new IntArray3();
            m_segmentsR=new ArrayList<Segment>();
            m_xRangesLayerR=new IntRangeArray();
            m_yRangesLayerR=new IntRangeArray();
        }
        
        public void addSegment(Segment aSeg){
            Segment bSeg=new Segment();
            aSeg.copySegment(bSeg);
            m_segmentsR.add(bSeg);
            m_xRangeR.expandRange(bSeg.getRange());
            m_yRangeR.expandRanges(bSeg.y0, bSeg.y0);
            m_zRangeR.expandRanges(bSeg.z0, bSeg.z0);
        }
        
        public void completeRegionBuilding(){
            int nSize=m_segmentsR.size();
            Segment aSeg=null;
            int zMin=m_zRangeR.getMin();
            int seg,stripe,layer;
            int cLayer=0;
            for(int i=0;i<nSize;i++){                
                aSeg=m_segmentsR.get(i);
                if(i==0)newLayer(aSeg);
                layer=aSeg.z0-zMin;
                if(layer!=cLayer){
                    closeLayer();
                    newLayer(aSeg); 
                    cLayer=layer;
                }
                stripe=aSeg.y0-yRange.getMin();
                if(stripe!=m_ncStripe) {
                     closeStripe();
                     newStripe(aSeg);
                }
                seg=aSeg.segmentID;
                currentSegment=aSeg;
                segmentIndexes.m_intArray.add(seg);
            }
            closeLayer();
        }

        void closeStripe(){            
            stripeIndexes.m_IntArray2.add(segmentIndexes);
            xRange.expandRange(currentSegment.getRange());
        }
        void newStripe(Segment aSeg){
            segmentIndexes=new IntArray();
            m_ncStripe=aSeg.y0-yRange.getMin();
            for(int i=m_npStripe+1;i<m_ncStripe;i++){
                stripeIndexes.m_IntArray2.add(segmentIndexes);
                segmentIndexes=new IntArray();
            }
            m_npStripe=m_ncStripe;
            xRange.expandRange(aSeg.getRange());
        }

        void closeLayer(){
            closeStripe();
            m_layerIndexesR.m_IntArray3.add(stripeIndexes);
            m_xRangesLayerR.m_intRangeArray.add(xRange);
            int y=currentSegment.y0;
            yRange.expandRanges(y,y);
            m_yRangesLayerR.m_intRangeArray.add(yRange);
        }


        void newLayer(Segment aSeg){
            stripeIndexes=new IntArray2();
//            for(int i=m_npLayer+1; i<m_ncLayer; i++){
//                layerIndexes.m_IntArray3.add(stripeIndexes);
//                stripeIndexes=new IntArray2();
//            }
            xRange=new intRange();
            yRange=new intRange();
            xRange.setMin(aSeg.getRange().getMin());
            yRange.setMin(aSeg.y0);
            m_npLayer=m_ncLayer;
            newStripe(aSeg);
       }
        public void writeRegion(Formatter fm){
            int nSize=m_segmentsR.size();
            Segment aSeg;
            int layers,stripes,layer,stripe,segs,seg,index;
            layers=m_layerIndexesR.m_IntArray3.size();
            for(layer=0;layer<layers;layer++){
                stripeIndexes=m_layerIndexesR.m_IntArray3.get(layer);
                stripes=stripeIndexes.m_IntArray2.size();
                for(stripe=0;stripe<stripes;stripe++){
                    segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
                    segs=segmentIndexes.m_intArray.size();
                    for(seg=0;seg<segs;seg++){
                        index=segmentIndexes.m_intArray.get(seg);
                        aSeg=segments.get(index);
                        fm.format("layer: %d,  stripe: %d,  segment: %d,  patch: %d, region: %d  z: %d  y: %d  x: %d-%d Stripe: %d Layer: %d%s", aSeg.layerID,aSeg.stripeID,aSeg.segmentID,aSeg.patchID.getID(),aSeg.regionID.getID(),aSeg.z0,aSeg.y0,aSeg.m_xRangeSegment.getMin(),aSeg.m_xRangeSegment.getMax(),stripe,layer,newline);
                    }
                }
            }
        }
    }
      //  intRange m_xRange, m_yRange, m_zRange;
    //ArrayList <intRange> m_xRangesLayer;
    //ArrayList <intRange> m_yRangesLayer;
    //BrainStructureNameNode StructureNode = new BrainStructureNameNode();
    
    //float m_fScale;
    //String m_sComments;
    //float m_fVolume;
    //int m_nNumVoxels;
    public void exportSubstructure_original(QuickFormatter qfm){
         qfm.getFormatter().format("%s,",StructureNode.StructureName);
         qfm.getFormatter().format("%s,",StructureNode.Abbreviation);
         qfm.getFormatter().format("%s,",StructureNode.ParentStruct);
         qfm.getFormatter().format("%d,",StructureNode.red);
         qfm.getFormatter().format("%d,",StructureNode.green);
         qfm.getFormatter().format("%d,",StructureNode.blue);
         qfm.getFormatter().format("%d,",StructureNode.informaticsId);
         qfm.getFormatter().format("%d%s",StructureNode.StructureId,newline);
         qfm.getFormatter().format("%s:    Voxels:  %d,  Scale:  %f%s",m_sComments,m_nNumVoxels,m_fScale,newline);
         qfm.getFormatter().format("xMin: %d,  xMax:  %d,  yMin:  %d,  yMax:  %d,  zMin:  %d,  zMax:  %d%s", m_xRange.getMin(), m_xRange.getMax(), m_yRange.getMin(), m_yRange.getMax(), m_zRange.getMin(), m_zRange.getMax(),newline);
         qfm.getFormatter().format("Layers:  %d%s", m_xRangesLayer.size(),newline);
         intRange zRange;
         for(int i=0;i<m_xRangesLayer.size();i++){
            xRange=m_xRangesLayer.get(i);
            yRange=m_yRangesLayer.get(i);
            qfm.getFormatter().format("layer: %d,  xMin: %d,  xMax:  %d,  yMin:  %d,  yMax:  %d%s",i, xRange.getMin(), xRange.getMax(), yRange.getMin(), yRange.getMax(),newline);        
         }
        Segment aSeg;        
        int stripes,segs,layers, layer,stripe,seg;
        segs=segments.size();
        qfm.getFormatter().format("Segments: %d%s", segs,newline);
        for(seg=0;seg<segs;seg++){
            aSeg=segments.get(seg);
            qfm.getFormatter().format("layer: %d,  stripe: %d,  seg: %d,  z: %d,  y: %d,  xi: %d,  xf: %d,  patch: %d,  region:  %d%s",aSeg.layerID,aSeg.stripeID,aSeg.segmentID,aSeg.z0,aSeg.y0,aSeg.getRange().getMin(),aSeg.getRange().getMax(),aSeg.patchID.getID(),aSeg.regionID.getID(),newline);
        }
        qfm.getFormatter().format("Layer structure:%s",newline);
        layers=layerIndexes.m_IntArray3.size();
        qfm.getFormatter().format("Number_of_layers: %d%s", layers,newline);
        for(layer=0;layer<layers;layer++){
            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
            stripes=stripeIndexes.m_IntArray2.size();
            qfm.getFormatter().format("Layer%d: Number_of_stripes: %d%s", layer,stripes,newline);
            for(stripe=0;stripe<stripes;stripe++){
                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
                segs=segmentIndexes.m_intArray.size();
                qfm.getFormatter().format("Layer%d: Stripe%d: Number_of_segments: %d%s", layer, stripe,segs,newline);
                for(seg=0;seg<segs-1;seg++){
                    qfm.getFormatter().format("%d,", segmentIndexes.m_intArray.get(seg));
                }
                if(segs>0) qfm.getFormatter().format("%d%s", segmentIndexes.m_intArray.get(segs-1),newline);
            }
        }
    }
    public void exportSubstructure(QuickFormatter qfm){
         qfm.getFormatter().format("%s,",StructureNode.StructureName);
         qfm.getFormatter().format("%s,",StructureNode.Abbreviation);
         qfm.getFormatter().format("%s,",StructureNode.ParentStruct);
         qfm.getFormatter().format("%d,",StructureNode.red);
         qfm.getFormatter().format("%d,",StructureNode.green);
         qfm.getFormatter().format("%d,",StructureNode.blue);
         qfm.getFormatter().format("%d,",StructureNode.informaticsId);
         qfm.getFormatter().format("%d%s",StructureNode.StructureId,newline);
         qfm.getFormatter().format("%s,%d,%f%s",m_sComments,m_nNumVoxels,m_fScale,newline);
         qfm.getFormatter().format("%d,%d,%d,%d,%d,%d%s", m_xRange.getMin(), m_xRange.getMax(), m_yRange.getMin(), m_yRange.getMax(), m_zRange.getMin(), m_zRange.getMax(),newline);
         qfm.getFormatter().format("%d%s", m_xRangesLayer.size(),newline);
         intRange zRange;
         for(int i=0;i<m_xRangesLayer.size();i++){
            xRange=m_xRangesLayer.get(i);
            yRange=m_yRangesLayer.get(i);
            qfm.getFormatter().format("%d,%d,%d,%d%s",xRange.getMin(), xRange.getMax(), yRange.getMin(), yRange.getMax(),newline);        
         }
        Segment aSeg;        
        int stripes,segs,layers, layer,stripe,seg;
        segs=segments.size();
        qfm.getFormatter().format("%d%s", segs,newline);
        for(seg=0;seg<segs;seg++){
            aSeg=segments.get(seg);
            qfm.getFormatter().format("%d,%d,%d,%d,%d,%d,%d,%d,%d%s",aSeg.layerID,aSeg.stripeID,aSeg.segmentID,aSeg.z0,aSeg.y0,aSeg.getRange().getMin(),aSeg.getRange().getMax(),aSeg.patchID.getID(),aSeg.regionID.getID(),newline);
        }
        layers=layerIndexes.m_IntArray3.size();
        qfm.getFormatter().format("%d%s", layers,newline);
        for(layer=0;layer<layers;layer++){
            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
            stripes=stripeIndexes.m_IntArray2.size();
            qfm.getFormatter().format("%d%s",stripes,newline);
            for(stripe=0;stripe<stripes;stripe++){
                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
                segs=segmentIndexes.m_intArray.size();
                qfm.getFormatter().format("%d%s",segs,newline);
                for(seg=0;seg<segs-1;seg++){
                    qfm.getFormatter().format("%d,", segmentIndexes.m_intArray.get(seg));
                }
                if(segs>0) qfm.getFormatter().format("%d%s", segmentIndexes.m_intArray.get(segs-1),newline);
            }
        }
    }
    public void exportSubstructure_Essential(QuickFormatter qfm){
         qfm.getFormatter().format("%s,",StructureNode.StructureName);
         qfm.getFormatter().format("%s,",StructureNode.Abbreviation);
         qfm.getFormatter().format("%s,",StructureNode.ParentStruct);
         qfm.getFormatter().format("%d,",StructureNode.red);
         qfm.getFormatter().format("%d,",StructureNode.green);
         qfm.getFormatter().format("%d,",StructureNode.blue);
         qfm.getFormatter().format("%d,",StructureNode.informaticsId);
         qfm.getFormatter().format("%d%s",StructureNode.StructureId,newline);
         qfm.getFormatter().format("%s,%d,%f%s",m_sComments,m_nNumVoxels,m_fScale,newline);
         qfm.getFormatter().format("%d,%d,%d,%d,%d,%d%s", m_xRange.getMin(), m_xRange.getMax(), m_yRange.getMin(), m_yRange.getMax(), m_zRange.getMin(), m_zRange.getMax(),newline);
         qfm.getFormatter().format("%d%s", m_xRangesLayer.size(),newline);
         intRange zRange;
         for(int i=0;i<m_xRangesLayer.size();i++){
            xRange=m_xRangesLayer.get(i);
            yRange=m_yRangesLayer.get(i);
            qfm.getFormatter().format("%d,%d,%d,%d%s",xRange.getMin(), xRange.getMax(), yRange.getMin(), yRange.getMax(),newline);        
         }
        Segment aSeg;        
        int stripes,segs,layers, layer,stripe,seg,index;
        layers=layerIndexes.m_IntArray3.size();
        qfm.getFormatter().format("%d%s", layers,newline);
        for(layer=0;layer<layers;layer++){
            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
            stripes=stripeIndexes.m_IntArray2.size();
            qfm.getFormatter().format("%d%s",stripes,newline);
            for(stripe=0;stripe<stripes;stripe++){
                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
                segs=segmentIndexes.m_intArray.size();
                qfm.getFormatter().format("%d%s",segs,newline);
                for(seg=0;seg<segs-1;seg++){
                    index=segmentIndexes.m_intArray.get(seg);
                    aSeg=segments.get(index);
                    qfm.getFormatter().format("%d,%d,",aSeg.getRange().getMin(), aSeg.getRange().getMax());
                }
                if(segs>0){
                    index=segmentIndexes.m_intArray.get(segs-1);
                    aSeg=segments.get(index);
                    qfm.getFormatter().format("%d,%d%s",aSeg.getRange().getMin(), aSeg.getRange().getMax(),newline);
                }
            }
        }
    }
    public void exportSubstructure_Essential(DataOutputStream dos) throws IOException {
        int length=StructureNode.StructureName.length();
        dos.writeInt(length);
        dos.writeChars(StructureNode.StructureName);
        length=StructureNode.Abbreviation.length();
        dos.writeInt(length);
        dos.writeChars(StructureNode.Abbreviation);
        length=StructureNode.ParentStruct.length();
        dos.writeInt(length);
        dos.writeChars(StructureNode.ParentStruct);
        dos.writeInt(StructureNode.red);
        dos.writeInt(StructureNode.green);
        dos.writeInt(StructureNode.blue);
        dos.writeInt(StructureNode.informaticsId);
        dos.writeInt(StructureNode.StructureId);
        length=m_sComments.length();
        dos.writeInt(length);
        dos.writeChars(m_sComments);
        dos.writeInt(m_nNumVoxels);
        dos.writeFloat(m_fScale);
        dos.writeInt(m_xRange.getMin());
        dos.writeInt(m_xRange.getMax());
        dos.writeInt(m_yRange.getMin());
        dos.writeInt(m_yRange.getMax());
        dos.writeInt(m_zRange.getMin());
        dos.writeInt(m_zRange.getMax());
        buildXZRanges();
        
        dos.writeInt(m_xRangesLayer.size());
        for(int i=0;i<m_xRangesLayer.size();i++){
            xRange=m_xRangesLayer.get(i);
            yRange=m_yRangesLayer.get(i);
            dos.writeInt(xRange.getMin());
            dos.writeInt(xRange.getMax());
            dos.writeInt(yRange.getMin());
            dos.writeInt(yRange.getMax());
        }
        
        int segs=m_xRange.getMax()-m_xRange.getMin()+1;
        dos.writeInt(segs);        
        for(int i=0;i<segs;i++){
            dos.writeInt(m_yRangesSegment[i].getMin());
            dos.writeInt(m_yRangesSegment[i].getMax());
            dos.writeInt(m_zRangesSegment[i].getMin());
            dos.writeInt(m_zRangesSegment[i].getMax());
        }
        
        int stripes=m_yRange.getMax()-m_yRange.getMin()+1;
        dos.writeInt(stripes);        
        for(int i=0;i<stripes;i++){
            dos.writeInt(m_xRangesStripe[i].getMin());
            dos.writeInt(m_xRangesStripe[i].getMax());
            dos.writeInt(m_zRangesStripe[i].getMin());
            dos.writeInt(m_zRangesStripe[i].getMax());
        }
        
        Segment aSeg;        
        int layers, layer,stripe,seg,index;
        layers=layerIndexes.m_IntArray3.size();
        dos.writeInt(layers);
        for(layer=0;layer<layers;layer++){
            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
            stripes=stripeIndexes.m_IntArray2.size();
            dos.writeInt(stripes);
            for(stripe=0;stripe<stripes;stripe++){
                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
                segs=segmentIndexes.m_intArray.size();
                dos.writeInt(segs);
                for(seg=0;seg<segs-1;seg++){
                    index=segmentIndexes.m_intArray.get(seg);
                    aSeg=segments.get(index);
                    dos.writeInt(aSeg.getRange().getMin());
                    dos.writeInt(aSeg.getRange().getMax());
                }
                if(segs>0){
                    index=segmentIndexes.m_intArray.get(segs-1);
                    aSeg=segments.get(index);
                    dos.writeInt(aSeg.getRange().getMin());
                    dos.writeInt(aSeg.getRange().getMax());
                }
            }
        }
    }
    public void importSubstructure(String path)throws IOException{
        FormattedReader fr=new FormattedReader(path);        
        String sline=fr.br.readLine();
        StringTokenizer st0=new StringTokenizer(sline,":,",false);
        int nNumTokens=st0.countTokens();
        StructureNode.StructureName=st0.nextToken();
        StructureNode.Abbreviation=st0.nextToken();
        if(nNumTokens==8)
            StructureNode.ParentStruct=st0.nextToken();
        else
            StructureNode.ParentStruct="";
        
        String s=st0.nextToken();
        Integer intw=new Integer(s);
        StructureNode.red=intw.intValue();
        s=st0.nextToken();
        intw=new Integer(s);
        StructureNode.green=intw.intValue();
        s=st0.nextToken();
        intw=new Integer(s);
        StructureNode.blue=intw.intValue();
        s=st0.nextToken();
        intw=new Integer(s);
        StructureNode.informaticsId=intw.intValue();
        try{s=st0.nextToken();}
        catch(NoSuchElementException e){
            e=e;
        }
        intw=new Integer(s);
        StructureNode.StructureId=intw.intValue();
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline," ,",false);
        m_sComments=st0.nextToken();
//        s=st0.nextToken();
        s=st0.nextToken();
        intw=new Integer(s);
        m_nNumVoxels=intw.intValue();
        m_fVolume=(float)m_nNumVoxels;
        s=st0.nextToken();
        Float fw=new Float(s);
        m_fScale=fw.floatValue();
        
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline,":, ",false);
        s=st0.nextToken();
//        intw=new Integer(s);
        m_xRange.setMin(getLastInteger(s));
        intw=new Integer(st0.nextToken());
        m_xRange.setMax(intw.intValue());
        intw=new Integer(st0.nextToken());
        m_yRange.setMin(intw.intValue());
        intw=new Integer(st0.nextToken());
        m_yRange.setMax(intw.intValue());
        intw=new Integer(st0.nextToken());
        m_zRange.setMin(intw.intValue());
        intw=new Integer(st0.nextToken());
        m_zRange.setMax(intw.intValue());
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline,":, ",false);
        s=st0.nextToken();
        intw=new Integer(s);
        int layers=intw.intValue();
        int layer;
        
        for(layer=0;layer<layers;layer++){
            xRange=new intRange();
            yRange=new intRange();
            sline=fr.br.readLine();
            st0=new StringTokenizer(sline,":, ",false);
            s=st0.nextToken();
            intw=new Integer(s);
            xRange.setMin(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            xRange.setMax(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            yRange.setMin(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            yRange.setMax(intw.intValue());
            m_xRangesLayer.add(xRange);
            m_yRangesLayer.add(yRange);            
        }
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline,":, ",false);
        intw=new Integer(st0.nextToken());
        int segs=intw.intValue();
        Segment aSeg;
        for(int seg=0;seg<segs;seg++){
            aSeg=new Segment();
            sline=fr.br.readLine();
            st0=new StringTokenizer(sline,":, ",false);
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.layerID=(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.stripeID=(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.segmentID=(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.z0=(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.y0=(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.m_xRangeSegment.setMin(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.m_xRangeSegment.setMax(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.patchID.setID(intw.intValue());
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.regionID.setID(intw.intValue());           
            segments.add(aSeg);
        } 
//        qfm.getFormatter().format("Number_of_layers: %d%s", layers,newline);
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline," ,",false);
        s=st0.nextToken();
        intw=new Integer(s);
        layers=intw.intValue();
        layerIndexes=new IntArray3(layers);
        int stripes,stripe;
        for(layer=0;layer<layers;layer++){
//            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
//            stripes=stripeIndexes.m_IntArray2.size();
//            qfm.getFormatter().format("Layer%d: Number_of_stripes: %d%s", layer,stripes,newline);
            sline=fr.br.readLine();
            st0=new StringTokenizer(sline," ,",false);
            s=st0.nextToken();
            intw=new Integer(s);
            stripes=intw.intValue();
            stripeIndexes=new IntArray2(stripes);
            for(stripe=0;stripe<stripes;stripe++){
                sline=fr.br.readLine();
                st0=new StringTokenizer(sline," ,",false);
                s=st0.nextToken();
                intw=new Integer(s);
                segs=intw.intValue();
//                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
//                segs=segmentIndexes.m_intArray.size();
//                qfm.getFormatter().format("Layer%d: Stripe%d: Number_of_segments: %d%s", layer, stripe,segs,newline);
                
                segmentIndexes=new IntArray(segs);
                if(segs>0){sline=fr.br.readLine();
                    st0=new StringTokenizer(sline," ,",false);
                    for(int seg=0;seg<segs;seg++){
                        s=st0.nextToken();
                        intw=new Integer(s);
                        segmentIndexes.m_intArray.add(intw.intValue());
                    }
                }
                stripeIndexes.m_IntArray2.add(segmentIndexes);
//                    qfm.getFormatter().format("%d,", segmentIndexes.m_intArray.get(seg));
//                }
//                if(segs>0) qfm.getFormatter().format("%d%s", segmentIndexes.m_intArray.get(segs-1),newline);
            }
            layerIndexes.m_IntArray3.add(stripeIndexes);
        }
        
//        finishLayerstructure();
    }
    
    public void importSubstructure_original(String path)throws IOException{
        FormattedReader fr=new FormattedReader(path);        
        String sline=fr.br.readLine();
        StringTokenizer st0=new StringTokenizer(sline,":,",false);
        int nNumTokens=st0.countTokens();
        StructureNode.StructureName=st0.nextToken();
        StructureNode.Abbreviation=st0.nextToken();
        if(nNumTokens==8)
            StructureNode.ParentStruct=st0.nextToken();
        else
            StructureNode.ParentStruct="";
        
        String s=st0.nextToken();
        Integer intw=new Integer(s);
        StructureNode.red=intw.intValue();
        s=st0.nextToken();
        intw=new Integer(s);
        StructureNode.green=intw.intValue();
        s=st0.nextToken();
        intw=new Integer(s);
        StructureNode.blue=intw.intValue();
        s=st0.nextToken();
        intw=new Integer(s);
        StructureNode.informaticsId=intw.intValue();
        try{s=st0.nextToken();}
        catch(NoSuchElementException e){
            e=e;
        }
        intw=new Integer(s);
        StructureNode.StructureId=intw.intValue();
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline," ,",false);
        m_sComments=st0.nextToken();
//        s=st0.nextToken();
        s=st0.nextToken();
        intw=new Integer(st0.nextToken());
        m_nNumVoxels=intw.intValue();
        m_fVolume=(float)m_nNumVoxels;
        s=st0.nextToken();
        Float fw=new Float(st0.nextToken());
        m_fScale=fw.floatValue();
        
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline,":, ",false);
        s=st0.nextToken();
        s=st0.nextToken();
//        intw=new Integer(s);
        m_xRange.setMin(getLastInteger(s));
        s=st0.nextToken();
        intw=new Integer(st0.nextToken());
        m_xRange.setMax(intw.intValue());
        s=st0.nextToken();
        intw=new Integer(st0.nextToken());
        m_yRange.setMin(intw.intValue());
        s=st0.nextToken();
        intw=new Integer(st0.nextToken());
        m_yRange.setMax(intw.intValue());
        s=st0.nextToken();
        intw=new Integer(st0.nextToken());
        m_zRange.setMin(intw.intValue());
        s=st0.nextToken();
        intw=new Integer(st0.nextToken());
        m_zRange.setMax(intw.intValue());
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline,":, ",false);
        s=st0.nextToken();
        s=st0.nextToken();
        intw=new Integer(s);
        int layers=intw.intValue();
        int layer;
        
        for(layer=0;layer<layers;layer++){
            xRange=new intRange();
            yRange=new intRange();
            sline=fr.br.readLine();
            st0=new StringTokenizer(sline,":, ",false);
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            xRange.setMin(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            xRange.setMax(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            yRange.setMin(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            yRange.setMax(intw.intValue());
            m_xRangesLayer.add(xRange);
            m_yRangesLayer.add(yRange);            
        }
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline,":, ",false);
        s=st0.nextToken();
        intw=new Integer(st0.nextToken());
        int segs=intw.intValue();
        Segment aSeg;
        for(int seg=0;seg<segs;seg++){
            aSeg=new Segment();
            sline=fr.br.readLine();
            st0=new StringTokenizer(sline,":, ",false);
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.layerID=(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.stripeID=(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.segmentID=(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.z0=(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.y0=(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.m_xRangeSegment.setMin(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.m_xRangeSegment.setMax(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.patchID.setID(intw.intValue());
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            aSeg.regionID.setID(intw.intValue());           
            segments.add(aSeg);
        } 
//        qfm.getFormatter().format("Number_of_layers: %d%s", layers,newline);
        sline=fr.br.readLine();
        sline=fr.br.readLine();
        st0=new StringTokenizer(sline," :",false);
        s=st0.nextToken();
        s=st0.nextToken();
        intw=new Integer(s);
        layers=intw.intValue();
        layerIndexes=new IntArray3(layers);
        int stripes,stripe;
        for(layer=0;layer<layers;layer++){
//            stripeIndexes=layerIndexes.m_IntArray3.get(layer);
//            stripes=stripeIndexes.m_IntArray2.size();
//            qfm.getFormatter().format("Layer%d: Number_of_stripes: %d%s", layer,stripes,newline);
            sline=fr.br.readLine();
            st0=new StringTokenizer(sline," :",false);
            s=st0.nextToken();
            s=st0.nextToken();
            s=st0.nextToken();
            intw=new Integer(s);
            stripes=intw.intValue();
            stripeIndexes=new IntArray2(stripes);
            for(stripe=0;stripe<stripes;stripe++){
                sline=fr.br.readLine();
                st0=new StringTokenizer(sline," :",false);
                s=st0.nextToken();
                try{s=st0.nextToken();}
                catch(NoSuchElementException e){
                    e=e;
                }
                s=st0.nextToken();
                s=st0.nextToken();
                intw=new Integer(s);
                segs=intw.intValue();
//                segmentIndexes=stripeIndexes.m_IntArray2.get(stripe);
//                segs=segmentIndexes.m_intArray.size();
//                qfm.getFormatter().format("Layer%d: Stripe%d: Number_of_segments: %d%s", layer, stripe,segs,newline);
                
                segmentIndexes=new IntArray(segs);
                if(segs>0){sline=fr.br.readLine();
                    st0=new StringTokenizer(sline," ,",false);
                    for(int seg=0;seg<segs;seg++){
                        s=st0.nextToken();
                        intw=new Integer(s);
                        segmentIndexes.m_intArray.add(intw.intValue());
                    }
                }
                stripeIndexes.m_IntArray2.add(segmentIndexes);
//                    qfm.getFormatter().format("%d,", segmentIndexes.m_intArray.get(seg));
//                }
//                if(segs>0) qfm.getFormatter().format("%d%s", segmentIndexes.m_intArray.get(segs-1),newline);
            }
            layerIndexes.m_IntArray3.add(stripeIndexes);
        }
        
//        finishLayerstructure();
    }
    public void finishLayerstructure(){
        int segs, seg,layers,layer,stripe,stripes,layerc=0,layerp=0,stripec=0,stripep=0,index;
        segs=segments.size();
        Segment aSeg;
        segmentIndexes=new IntArray();
        stripeIndexes=new IntArray2();
        layerIndexes=new IntArray3();
        for(seg=0;seg<segs;seg++){
            aSeg=segments.get(seg);
            layerc=aSeg.layerID;
            stripec=aSeg.stripeID;
            if(layerc!=layerp){
                stripeIndexes.m_IntArray2.add(segmentIndexes);
                layerIndexes.m_IntArray3.add(stripeIndexes);
                segmentIndexes=new IntArray();
                stripeIndexes=new IntArray2();
                for(int i=layerp+1;i<layerc;i++){
                    layerIndexes.m_IntArray3.add(stripeIndexes);
                    stripeIndexes=new IntArray2();
                }
                layerp=layerc;
                stripep=stripec;
            }
            
            if(stripec!=stripep){                
                stripeIndexes.m_IntArray2.add(segmentIndexes);
                segmentIndexes=new IntArray();
                for(int i=stripep;i<stripec;i++){
                    stripeIndexes.m_IntArray2.add(segmentIndexes);
                    segmentIndexes=new IntArray();
                }
                segmentIndexes=new IntArray();
            }
            segmentIndexes.m_intArray.add(seg);
        }
        stripeIndexes.m_IntArray2.add(segmentIndexes);
        layerIndexes.m_IntArray3.add(stripeIndexes);
    }
    void buildXZRanges(){
        int x,y,z;
        int xMin=m_xRange.getMin(),xMax=m_xRange.getMax();
        int yMin=m_yRange.getMin(),yMax=m_yRange.getMax();
        int zMin=m_zRange.getMin(),zMax=m_zRange.getMax();
        int index;
            m_yRangesSegment=new intRange[xMax-xMin+1];
            m_zRangesSegment=new intRange[xMax-xMin+1];
            m_xRangesStripe=new intRange[yMax-yMin+1];
            m_zRangesStripe=new intRange[yMax-yMin+1];
        for(x=xMin;x<=xMax;x++){
            index=x-xMin;
            m_yRangesSegment[index]=new intRange();
            m_zRangesSegment[index]=new intRange();
        }
        for(y=yMin;y<=yMax;y++){
            index=y-yMin;
            m_xRangesStripe[index]=new intRange();
            m_zRangesStripe[index]=new intRange();
        }
        int yIndex,xIndex,zIndex;
        for(y=yMin;y<=yMax;y++){
            yIndex=y-yMin;
            for(x=xMin;x<=xMax;x++){
                xIndex=x-xMin;
                for(z=zMin;z<=zMax;z++){
                    if(contains(x,y,z)!=null){
                        m_yRangesSegment[xIndex].expandRanges(y, y);
                        m_zRangesSegment[xIndex].expandRanges(z, z);
                        m_xRangesStripe[yIndex].expandRanges(x, x);
                        m_zRangesStripe[yIndex].expandRanges(z, z);
                    }
                }
            }
        }
    }
    public int getID(){
        return StructureNode.informaticsId;
    }
}


