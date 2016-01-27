/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.IntPair;
import utilities.CustomDataTypes.intRange;
/**
 *
 * @author Taihao
 */
public class SequenceSegmenter_ProgressiveScales {
    static public final int left=1,right=2,isolated=3;
    static public final int smaller=1,larger=2,different=3;
    ArrayList<Integer> nvScaleSize;
    ArrayList<double[]> pdvData,pdvSD;
    ArrayList<int[]> pnvSegmentIndexes;
    ArrayList<ArrayList<Integer>> nvvAnchors;
    ArrayList<ArrayList<intRange>> cvvSegmenters;
    ArrayList<ArrayList<intRange>> cvvConsolidaters;
    double dPSeg,dPCons;
    int nDataSize,nNumScales,nNeighbors,nComparison;
    int[] pnAnchorScaleIndexes, pnSegmentScaleIndexes;
    ArrayList<Integer> nvMultiscaleAnchors;
    ArrayList<IntPair[]> pcvNeighbors;
    ArrayList<double[]> pdvPValues;
    ArrayList<Double> dvPSegmentation,dvPConsolidation;
    public SequenceSegmenter_ProgressiveScales(){
        nvScaleSize=new ArrayList();
        pdvSD=new ArrayList();
        pnvSegmentIndexes=new ArrayList();
        cvvSegmenters=new ArrayList();
        dPSeg=0.05;
        dvPSegmentation=new ArrayList();
        dvPConsolidation=new ArrayList();
        cvvConsolidaters=new ArrayList();
    }
    public SequenceSegmenter_ProgressiveScales(ArrayList<double[]> pdvData, ArrayList<IntPair[]> pcvNeighbors, int nNeighbors, ArrayList<Double> dvPSegmentation,ArrayList<Double> dvPConsolidation, int comparison){
        this();
        this.nNeighbors=nNeighbors;
        this.nvvAnchors=nvvAnchors;
        nNumScales=pdvData.size();
        nDataSize=pdvData.get(0).length;
        this.pdvData=pdvData;
        this.dPSeg=dPSeg;
        this.pcvNeighbors=pcvNeighbors;
        this.nComparison=comparison;
        CommonStatisticsMethods.copyDoubleArray(dvPSegmentation, this.dvPSegmentation);
        if(dvPConsolidation!=null) 
            CommonStatisticsMethods.copyDoubleArray(dvPConsolidation, this.dvPConsolidation);
        else
            this.dvPConsolidation=null;
        segmentize();
    }
    void segmentize(){
        findSegmenters();
        buildSegments();
    }
    static public ArrayList<double[]> getRWSD_Cumulative(double[] pdData, int ws, double dPV){
        int i,len=pdData.length;
        boolean[] pbSelected=new boolean[len];
        CommonStatisticsMethods.setElements(pbSelected, true);
        for(i=0;i<len;i++){
            if(pdData[i]==0) pbSelected[i]=false;
        }
        return CommonStatisticsMethods.getRWSD_Cumulative(pdData,pbSelected, ws, dPV);
    }
    void findSegmenters(){
        int i,j,ws=30;
        double[] pdData,pdSD,pdMean,pdPValues;
        pdvPValues=new ArrayList();
        double dP;
        ArrayList<intRange> cvSegmenters=new ArrayList(),cvConsolidaters=new ArrayList();
        ArrayList<double[]> pdvMeanSD;
        for(i=0;i<nNumScales;i++){    
            dPSeg=dvPSegmentation.get(i);
            if(dvPConsolidation!=null)
                dPCons=dvPConsolidation.get(i);
            else
                dPCons=1.1;
            cvSegmenters=new ArrayList();
            cvConsolidaters=new ArrayList();
            pdvMeanSD=getRWSD_Cumulative(pdvData.get(i), ws, 0.001);
            pdMean=pdvMeanSD.get(0);
            pdSD=pdvMeanSD.get(1);
            pdData=pdvData.get(i);
            pdPValues=new double[nDataSize];
            for(j=0;j<nDataSize;j++){
                if(j==107){
                    j=j;
                }
                dP=GaussianDistribution.Phi(pdData[j], pdMean[j], pdSD[j]);
                pdPValues[j]=dP;
                if(significant(pdData[j],pdMean[j],pdSD[j])){
                    addSegmenters(cvSegmenters,i,j);
                }   
                if(Consolidatible(pdData[j],pdMean[j],pdSD[j])){
                    addSegmenters(cvConsolidaters,i,j);
                }
            }
            CommonStatisticsMethods.removeRedundancy(cvSegmenters);
            CommonStatisticsMethods.removeRedundancy(cvConsolidaters);
            cvSegmenters=CommonStatisticsMethods.excludeCommonRanges(cvSegmenters,cvConsolidaters);
            CommonStatisticsMethods.removeRedundancy(cvSegmenters);
            for(j=cvSegmenters.size()-1;j>=0;j--){
                if(cvSegmenters.get(j).getRange()<=1) cvSegmenters.remove(j);
            }
            cvvSegmenters.add(cvSegmenters);
            cvvConsolidaters.add(cvConsolidaters);
            pdvPValues.add(pdPValues);
        }
    }
    boolean significant(double x, double mean, double sd){
        double dP=GaussianDistribution.Phi(x, mean,sd);
        switch(nComparison){
            case smaller:
                dP=dP;
                break;
            case larger:
                dP=1-dP;
                break;
            case different:
                if(dP>0.5) dP=1-dP;
                break;
        }
        return dP<dPSeg;
    }
    boolean Consolidatible(double x, double mean, double sd){
        double dP=GaussianDistribution.Phi(x, mean,sd);
        switch(nComparison){
            case smaller:
                dP=dP;
                break;
            case larger:
                dP=1-dP;
                break;
            case different:
                if(dP>0.5) dP=1-dP;
                break;
        }
        return dP>dPCons;
    }
    int addSegmenters(ArrayList<intRange> segmenters, int scaleIndex, int position){
        if(position<0||position>=nDataSize) return -1;
        IntPair ip=pcvNeighbors.get(scaleIndex)[position];
        int i,l,r;
        l=ip.left;
        r=ip.right;
        switch (nNeighbors){
            case left:
                if(l<position) segmenters.add(new intRange(l,position));
                break;
            case right:
                if(r>position) segmenters.add(new intRange(position,r));
                break;
            case isolated:
                if(l<position) segmenters.add(new intRange(l,position));
                if(r>position) segmenters.add(new intRange(position,r));
                break;                
        }
        return 1;
    }
    public int combineSegmenters_AND(ArrayList<ArrayList<intRange>> cvvSegmenters){
        ArrayList<intRange> segmenters;
        for(int i=0;i<nNumScales;i++){
            segmenters=CommonStatisticsMethods.getCommonIntRanges(this.cvvSegmenters.get(i),cvvSegmenters.get(i));
            this.cvvSegmenters.set(i, segmenters);
        }
        buildSegments();
        return 1;
    }
    ArrayList<ArrayList<intRange>> getSegmenters(){
        return cvvSegmenters;
    }
    public static int[] buildSegments(ArrayList<intRange> nvSegmenters, int nDataSize){
        int[] pnSegmentIndexes=new int[nDataSize];
        intRange segmenter;
        int segIndex,i,j,len,left,right;
        len=nvSegmenters.size();
        for(i=0;i<len;i++){
            segmenter=nvSegmenters.get(i);
            left=segmenter.getMin();
            right=segmenter.getMax();
            for(j=left;j<=right;j++){
                pnSegmentIndexes[j]=-(i+1);
            }
        }
        segIndex=0;
        left=0;
        for(i=0;i<len;i++){
            segmenter=nvSegmenters.get(i);
            right=segmenter.getMin();
            for(j=left;j<=right;j++){
                pnSegmentIndexes[j]=segIndex;
            }
            segIndex++;
            left=segmenter.getMax();
        }
        for(j=left;j<nDataSize;j++){
            pnSegmentIndexes[j]=segIndex;
        }
        return pnSegmentIndexes;
    }
    void buildSegments(){
        pnvSegmentIndexes.clear();
        for(int i=0;i<nNumScales;i++){
            pnvSegmentIndexes.add(buildSegments(cvvSegmenters.get(i),nDataSize));
        }
    }
    ArrayList<int[]> getSegmentIndexes(){
        return pnvSegmentIndexes;
    }
    public void buildAnchoredSegments(ArrayList<ArrayList<Integer>> nvvAnchors){
        //compute pnAnchorScaleIndexes and pnSegmentScaleIndexes.
        int segIndex,i,j,len,left,right,anchorH,start, end,anchorIndex0,anchorIndex,nNumAnchors;
        pnSegmentScaleIndexes=new int[nDataSize];
        int[] pnSegmentIndexes;
        pnAnchorScaleIndexes=new int[nDataSize];
        CommonStatisticsMethods.setElements(pnSegmentScaleIndexes, -1);
        CommonStatisticsMethods.setElements(pnAnchorScaleIndexes, -1);
        nvMultiscaleAnchors=new ArrayList();
        this.nvvAnchors=nvvAnchors;
        
        ArrayList<Integer> nvAnchors=nvvAnchors.get(0);
        
        for(i=0;i<nvAnchors.size();i++){
            pnAnchorScaleIndexes[nvAnchors.get(i)]=0;
        }
        
        int anchor=0,position,position0=nDataSize,nAnchorPosition;
        for(i=1;i<nNumScales;i++){
            
            nvAnchors=nvvAnchors.get(i);
            nNumAnchors=nvAnchors.size();
            
            pnSegmentIndexes=pnvSegmentIndexes.get(i);
            
            
            anchorH=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnAnchorScaleIndexes, 0, 1, -1);
            //Because pnAnchorScaleIndexes is computed from higher (coarser) scale to lower (finer) scale, cnhorH is 
            //an anchor of higher scale.
            anchorIndex0=0;
            anchorIndex=0;
            end=-1;
            while(anchorH>=0){
                if(anchorH==98){
                    anchorH=anchorH;
                }
                segIndex=pnSegmentIndexes[anchorH];
                if(segIndex>=0){
                    start=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentIndexes, anchorH, -1, segIndex)+1;
                    if(start<0) start=0;
                    end=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentIndexes, anchorH, 1, segIndex)-1;
                    if(end<0) end=nDataSize-1;
                }else{// the position anchorH is not continuous at scale i
                    start=anchorH;
                    end=anchorH;
                }
                //found the starting and ending point of the segment of scale i that contains anchorH
                for(anchorIndex=anchorIndex0;anchorIndex<nNumAnchors;anchorIndex++){
                    anchor=nvAnchors.get(anchorIndex);
                    if(anchor<start)
                        continue;
                    else if(anchor<=end){
                        if(pnAnchorScaleIndexes[anchor]<0) //this anchor at scale i is not an anchor of higher scales
                            pnAnchorScaleIndexes[anchor]=i;//this position is recorded as an anchor of multiscale anchor because the segment contain (at least) a higher scale anchor
                    }else{//passed the segment
                        anchorIndex0=anchorIndex;
                        break;
                    }
                }
                
                for(position=start;position<=end;position++){
                    pnSegmentScaleIndexes[position]=i;
                }
                anchorH=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnAnchorScaleIndexes, end+1, 1, -1);
                //anchorH is the first anchor position of higher scale after the end of the scale.
            }
            
        }
        for(i=0;i<nDataSize;i++){
            if(pnAnchorScaleIndexes[i]!=-1) nvMultiscaleAnchors.add(i);
        }
        //Anchor at lower scale will be considered as an anchor at muliscale only if it is contained in the same segment (the same scale) with an anchor of higher scale
    }
    public void buildAnchoredSegments(ArrayList<double[]> pdvY, ArrayList<int[]> pnvRisingIntervals, ArrayList<ArrayList<Integer>> nvvAnchors){//11n12
        //compute pnAnchorScaleIndexes and pnSegmentScaleIndexes.
        int segIndex,i,j,left,right,anchorH,start, end,anchorIndex0,anchorIndex,nNumAnchors;
        pnSegmentScaleIndexes=new int[nDataSize];
        int[] pnSegmentIndexes;
        pnAnchorScaleIndexes=new int[nDataSize];
        CommonStatisticsMethods.setElements(pnSegmentScaleIndexes, -1);
        CommonStatisticsMethods.setElements(pnAnchorScaleIndexes, -1);
        nvMultiscaleAnchors=new ArrayList();
        this.nvvAnchors=nvvAnchors;
        
        ArrayList<Integer> nvAnchors=nvvAnchors.get(0);
        
        for(i=0;i<nvAnchors.size();i++){
            pnAnchorScaleIndexes[nvAnchors.get(i)]=0;
        }
        
        int anchor=0,position,position0=nDataSize,nAnchorPosition;
        int skippedAnchorIndexI,skippedAnchorIndexF;
        int skippedAnchorI,skippedAnchorF;
        double ratio;
        double[] pdY;
        int[] pnRisingIntervals;
        boolean valid;
        for(i=1;i<nNumScales;i++){
            pdY=pdvY.get(i);
            pnRisingIntervals=pnvRisingIntervals.get(i);
            nvAnchors=nvvAnchors.get(i);
            nNumAnchors=nvAnchors.size();
            
            pnSegmentIndexes=pnvSegmentIndexes.get(i);
            
            
            anchorH=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnAnchorScaleIndexes, 0, 1, -1);
            //Because pnAnchorScaleIndexes is computed from higher (coarser) scale to lower (finer) scale, cnhorH is 
            //an anchor of higher scale.
            anchorIndex0=0;
            anchorIndex=0;
            end=-1;
            while(anchorH>=0){
                if(anchorH==98){
                    anchorH=anchorH;
                }
                segIndex=pnSegmentIndexes[anchorH];
                if(segIndex>=0){
                    start=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentIndexes, anchorH, -1, segIndex)+1;
                    if(start<0) start=0;
                    end=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentIndexes, anchorH, 1, segIndex)-1;
                    if(end<0) end=nDataSize-1;
                }else{// the position anchorH is not continuous at scale i
                    start=anchorH;
                    end=anchorH;
                }
                //found the starting and ending point of the segment of scale i that contains anchorH
                skippedAnchorIndexI=-1;
                skippedAnchorIndexF=-1;
                for(anchorIndex=anchorIndex0;anchorIndex<nNumAnchors;anchorIndex++){
                    anchor=nvAnchors.get(anchorIndex);
                    if(anchor<start){
                        if(skippedAnchorIndexI==-1)skippedAnchorIndexI=anchorIndex;
                        skippedAnchorIndexF=anchorIndex;
                        continue;
                    } else if(anchor<=end){
                        if(pnAnchorScaleIndexes[anchor]<0) //this anchor at scale i is not an anchor of higher scales
                            pnAnchorScaleIndexes[anchor]=i;//this position is recorded as an anchor of multiscale anchor because the segment contain (at least) a higher scale anchor
                    }else{//passed the segment
                        anchorIndex0=anchorIndex;
                        break;
                    }
                }
                
                for(position=start;position<=end;position++){
                    pnSegmentScaleIndexes[position]=i;
                }
                
//              if(skippedAnchorIndexI>=0) patchMultiscaleSegment_DeltaRatioBased(pdY, pnSegmentIndexes,  pnRisingIntervals, nvAnchors, i, skippedAnchorIndexI, skippedAnchorIndexF);//12d05
                //this part should have been taken care of by the method refineMultipleScaleSegmenter of the class LineFeatureExtracter2
                
                anchorH=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnAnchorScaleIndexes, end+1, 1, -1);                                                
                //anchorH is the first anchor position of higher scale after the end of the scale.
            }
            
        }
        for(i=0;i<nDataSize;i++){
            if(pnAnchorScaleIndexes[i]!=-1) nvMultiscaleAnchors.add(i);
        }
        //Anchor at lower scale will be considered as an anchor at muliscale only if it is contained in the same segment (the same scale) with an anchor of higher scale
    }
    public int patchMultiscaleSegment_DeltaRatioBased(double[] pdY, int[] pnSegmentIndexes, int[] pnRisingIntervals, ArrayList<Integer> nvAnchors,
            int nScaleIndex, int skippedAnchorIndexI, int skippedAnchorIndexF){
        //A single unanchored segment would be anchored if the ratio deltas at two ends are larger than the cutoff.
        
        int skippedAnchorI,skippedAnchorF,start,end,position,anchorIndex,anchor,left,right;
        boolean valid;
        double ratio;
                if(skippedAnchorIndexI>=0){
                    skippedAnchorI=nvAnchors.get(skippedAnchorIndexI);
                    skippedAnchorF=nvAnchors.get(skippedAnchorIndexF);
                    
                    if(pnSegmentIndexes[skippedAnchorI]<0) skippedAnchorI++;
                    if(pnSegmentIndexes[skippedAnchorF]<0) skippedAnchorF--;
                    
                    if(pnSegmentIndexes[skippedAnchorI]==pnSegmentIndexes[skippedAnchorF]){
//                    if(true){
                        start=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentIndexes, skippedAnchorI, -1, pnSegmentIndexes[skippedAnchorI])+1;
                        if(start>skippedAnchorI) start=skippedAnchorI;
                        if(start<0) start=0;
                        
                        end=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentIndexes, skippedAnchorF, 1, pnSegmentIndexes[skippedAnchorF])-1;
                        if(end<0) end=nDataSize-1;
                        if(end<skippedAnchorF) end=skippedAnchorF;
                        
                        if(start>0&&end<nDataSize-1){
                            left=start+CommonStatisticsMethods.getRisingInterval(pnRisingIntervals, start, -1);
                            right=end+CommonStatisticsMethods.getRisingInterval(pnRisingIntervals, end,1);
                            valid=false;
                            
                            for(position=left;position<=start;position++){
                                if(pnSegmentScaleIndexes[position]==nScaleIndex){
                                    valid=true;
                                    break;
                                }
                            }
                            if(!valid) return 1;
                            
                            valid=false;
                            for(position=end;position<=right;position++){
                                if(pnSegmentScaleIndexes[position]==nScaleIndex){
                                    valid=true;
                                    break;
                                }
                            }
                            if(!valid) return 1;
                            
                            ratio=Math.abs((pdY[left]-pdY[start])/(pdY[end]-pdY[right]));
                            if(ratio>2||ratio<0.5){
                                for(position=left;position<=right;position++){
                                    pnSegmentScaleIndexes[position]=nScaleIndex;
                                }
                                for(anchorIndex=skippedAnchorIndexI;anchorIndex<=skippedAnchorIndexF;anchorIndex++){
                                    anchor=nvAnchors.get(anchorIndex);
                                    if(pnAnchorScaleIndexes[anchor]<0) 
                                        pnAnchorScaleIndexes[anchor]=nScaleIndex;
                                }
                            }
                        }
                    }
                }
        return 1;
    }
    public double[] buildMultiscaleTrail_Downward(ArrayList<double[]> pdvTrails){
        int i,hScaleIndex=nNumScales-1,scaleIndex;
        double[] pdTrail=new double[nDataSize],pdt;
        int start=CommonStatisticsMethods.getFirstPositionWithTheElement(pnSegmentScaleIndexes, 0, 1, hScaleIndex),end;
        if(start<0) start=0;
        for(i=0;i<start;i++){
            scaleIndex=pnSegmentScaleIndexes[i];
            pdTrail[i]=pdvTrails.get(scaleIndex)[i];
        }
        end=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentScaleIndexes, start, 1, hScaleIndex)-1;
        if(end<0) end=nDataSize-1;
        pdt=pdvTrails.get(hScaleIndex);
        for(i=start;i<=end;i++){
            pdTrail[i]=pdt[i];
        }
        start=CommonStatisticsMethods.getFirstPositionWithTheElement(pnSegmentScaleIndexes, end+1, 1, hScaleIndex);
        while(start>0){
            if(end==87){
                end=end;
            }
            bridgeTrail_Downward(pdvTrails,pdTrail,hScaleIndex,end,start);
            end=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentScaleIndexes, start, 1, hScaleIndex)-1;
            if(end<0) end=nDataSize-1;
            pdt=pdvTrails.get(hScaleIndex);
            for(i=start;i<=end;i++){
                pdTrail[i]=pdt[i];
            }     
            start=CommonStatisticsMethods.getFirstPositionWithTheElement(pnSegmentScaleIndexes, end+1, 1, hScaleIndex);
        }
        return pdTrail;
    }
    public double[] buildMultiscaleTrail_Upward(ArrayList<double[]> pdvTrails){
        int i,hScaleIndex=nNumScales-1,scaleIndex;
        double[] pdTrail=new double[nDataSize],pdt;
        int start=CommonStatisticsMethods.getFirstPositionWithTheElement(pnSegmentScaleIndexes, 0, 1, hScaleIndex),end;
        if(start<0) start=0;
        for(i=0;i<start;i++){
            scaleIndex=pnSegmentScaleIndexes[i];
            pdTrail[i]=pdvTrails.get(scaleIndex)[i];
        }
        end=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentScaleIndexes, start, 1, hScaleIndex)-1;
        if(end<0) end=nDataSize-1;
        pdt=pdvTrails.get(hScaleIndex);
        for(i=start;i<=end;i++){
            pdTrail[i]=pdt[i];
        }
        start=CommonStatisticsMethods.getFirstPositionWithTheElement(pnSegmentScaleIndexes, end+1, 1, hScaleIndex);
        while(start>0){
            if(end==87){
                end=end;
            }
            bridgeTrail_Upward(pdvTrails,pdTrail,hScaleIndex,end,start);
            end=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnSegmentScaleIndexes, start, 1, hScaleIndex)-1;
            if(end<0) end=nDataSize-1;
            pdt=pdvTrails.get(hScaleIndex);
            for(i=start;i<=end;i++){
                pdTrail[i]=pdt[i];
            }     
            start=CommonStatisticsMethods.getFirstPositionWithTheElement(pnSegmentScaleIndexes, end+1, 1, hScaleIndex);
        }
        return pdTrail;
    }
    void bridgeTrail_Downward(ArrayList<double[]> pdvTrails,double[] pdTrail, int left, int right){
        int scaleIndexL=pnSegmentScaleIndexes[left], scaleIndexR=pnSegmentScaleIndexes[right],scaleIndex;
        double[] pdTrailH=pdvTrails.get(nNumScales-1);
        double dL=pdvTrails.get(scaleIndexL)[left],dR=pdvTrails.get(scaleIndexR)[right];
        left++;
        right--;
        double dI,dT,dt;
        for(int i=left;i<=right;i++){
            dI=CommonMethods.getLinearIntoplation(left, dL, right, dR, i);
            scaleIndex=pnSegmentScaleIndexes[i];
            if(scaleIndex<0) scaleIndex=0;
            if(scaleIndex>=0) 
                dT=pdvTrails.get(scaleIndex)[i];
            else
                dT=dI+1;
            dt=Math.min(dT,dI);
            dt=Math.max(dt,pdTrailH[i]);
            pdTrail[i]=dt;
        }
    } 
    void bridgeTrail_Downward(ArrayList<double[]> pdvTrails,double[] pdTrail, int scaleIndex, int left, int right){
        double dL=pdvTrails.get(scaleIndex)[left],dR=pdvTrails.get(scaleIndex)[right],dn=Math.min(dL,dR);
        double[] pdt=pdvTrails.get(scaleIndex);
        
        left++;
        right--;
        double dI,dT,dt;
        for(int i=left;i<=right;i++){
            dt=pdt[i];
            dt=Math.max(dt,dn);
            pdTrail[i]=dt;
        }
    } 
    void bridgeTrail_Upward(ArrayList<double[]> pdvTrails,double[] pdTrail, int scaleIndex, int left, int right){
        double dL=pdvTrails.get(scaleIndex)[left],dR=pdvTrails.get(scaleIndex)[right],dn=Math.max(dL,dR);
        double[] pdt=pdvTrails.get(scaleIndex);
        
        left++;
        right--;
        double dt;
        for(int i=left;i<=right;i++){
            dt=pdt[i];
            dt=Math.min(dt,dn);
            pdTrail[i]=dt;
        }
    } 
    public ArrayList<Integer> getMultiscaleAnchors(){
        return nvMultiscaleAnchors;
    }
    public ArrayList<double[]> getPValues(){
        return pdvPValues;
    }
}
