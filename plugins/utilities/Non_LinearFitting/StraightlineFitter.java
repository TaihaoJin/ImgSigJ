/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CommonStatisticsMethods;
import java.util.ArrayList;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.MeanSem1;
import utilities.statistics.MeanSem0;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.MeanSem1;
import utilities.QuickSort;
/**
 *
 * @author Taihao
 */
public class StraightlineFitter {
    
    class LineFittingSegmentNode{
        public ArrayList<Integer> nvCrossPoints;
        public MeanSem1 m_cMS;
        int start,end;
        boolean valid;
        boolean[] validPoints;
        public SimpleRegression cSR;
        public LineFittingSegmentNode(){

        }
        public void update(int start, int end){
            this.start=start;
            this.end=end;
            this.cSR=CommonStatisticsMethods.getSimpleLinearRegression(m_pdX, m_pdY, start, end);

            int len=end-start+1;
            validPoints=new boolean[len];
            for(int i=0;i<len;i++){
                validPoints[i]=true;
            }
            calDeviation();
            valid=isValid();
        }
        void calDeviation(){
            double mss=0,mean=0,dev,min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY,dev0=m_pdY[start]-cSR.predict(m_pdX[start]);;
            int num=0,i;
            nvCrossPoints=new ArrayList();
            for(i=start;i<=end;i++){
                dev=m_pdY[i]-cSR.predict(m_pdX[i]);
                mean+=dev;
                mss+=dev*dev;
                if(dev>max) max=dev;
                if(dev<min) min=dev;
                if(dev*dev0<=0) nvCrossPoints.add(i);
                dev0=dev;
                num++;
            }
            m_cMS=new MeanSem1();
            m_cMS.updateMeanSquareSum(num, mean, mss, max, min);
        }
        public LineFittingSegmentNode getBetterSegment(LineFittingSegmentNode seg1, LineFittingSegmentNode seg2, int position){
            if(seg1==null) return seg2;
            if(!seg1.valid) return seg2;
            if(!seg1.isValid(position)) return seg2;
            if(seg2==null) return seg1;
            if(!seg2.valid) return seg1;
            if(!seg2.isValid(position)) return seg1;
            if(seg1.IsBetter(seg2))return seg1;            
            return seg2;
        }
        public boolean IsBetter(LineFittingSegmentNode seg2){//returns true if this is better than seg2
//            if(seg2.nvCrossPoints.size()>nvCrossPoints.size()) return false;
//            if(seg2.nvCrossPoints.size()<nvCrossPoints.size()) return true;
            int len1=end-start, len2=seg2.end-seg2.start,minLen=2*nRisingInterval;
            if(len1<minLen&&len2>=minLen) return false;
            if(len1>=minLen&&len2<minLen) return true;
            if(seg2.m_cMS.sem<m_cMS.sem) return false;
            return true;
        }
        public intRange getSegRange(){
            return new intRange(start,end);
        }
        public void setValidity(boolean validity){
            this.valid=validity;
            int i,len=end-start+1;
            for(i=0;i<len;i++){
                validPoints[i]=validity;
            }
        }
        public boolean isValid(){
            if(end==start+1){
                setValidity(true);
                return true;
            }

            int r=getNextCrosspoint(cSR,start,-1),r0=getNextCrosspoint(cSR,end,1),l,len=end-start+1,i;
/*            if(r0==-1) {
                setValidity(false);
                return false;
            }

            if(r==-1) {
                setValidity(false);
                return false;
            }*/

            if(r0==-1) r0=nDataSize-1;
            int num=nvCrossPoints.size();
            int nRegions=num+1;
            double[] pdTotalDeviations=new double[nRegions];
            intRange[] ranges=new intRange[nRegions];
            int[] indexes=new int[nRegions];

            for(i=0;i<num;i++){
                l=r+1;
                r=nvCrossPoints.get(i)-1;
                pdTotalDeviations[i]=getTotalDeviation(cSR,l,r);
                indexes[i]=i;
                if(!isAcceptableDeviation_Sum(cSR,l,r,1)){
                    if(i>0){
                        setValidity(false);
                        return false;
                    }else{
                        start=nvCrossPoints.get(0);
                    }
                }
            }

            pdTotalDeviations[nRegions-1]=getTotalDeviation(cSR,r+1,r0);
            indexes[nRegions-1]=nRegions-1;

            if(!isAcceptableDeviation_Sum(cSR,r+1,r0,1)) {
//                setValidity(false);
//                return false;
                end=nvCrossPoints.get(nvCrossPoints.size()-1);
            }

            QuickSort.quicksort(pdTotalDeviations, indexes);
            for(i=Math.max(3, nRegions/2);i<nRegions;i++){
 //               validPoints[indexes[i]]=false;
            }
            calDeviation();
            return true;
        }
        public boolean isValid(int position){
            if(position<start||position>end) return false;
            return validPoints[position-start];
        }
    }

    double[] m_pdX,m_pdY;
    int nDataSize,nRisingInterval;
    double m_dSD,m_dPCutoff;
    int m_nLWs,m_nRWs,m_nWs,m_nLimitL,m_nLimitR;
    intRange[] m_pcStraightRanges;
    double[] m_pdPredictions;
    intRange[] m_pcStraightRangesL;
    double[] m_pdPredictionsL;
    intRange[] m_pcStraightRangesR;
    double[] m_pdPredictionsR;
    boolean m_bHardRange;
    SimpleRegression[] m_pcOptRegressions,m_pcStartingRegressions,m_pcEndRegressions;
    LineFittingSegmentNode[] m_pcOptimalSegments,m_pcOptimalStartingSegments,m_pcOptimalEndingSegments;
    public StraightlineFitter(ArrayList<Double> dvX, ArrayList<Double> dvY, int nRisingInterval, int nLWs, int nRWs, double pCutoff){
        nDataSize=dvX.size();
        m_bHardRange=false;
        m_pdX=new double[nDataSize];
        m_pdY=new double[nDataSize];
        m_pdPredictions=new double[nDataSize];
        m_pcStraightRanges=new intRange[nDataSize];
        m_nLWs=nLWs;
        m_nRWs=nRWs;
        for(int i=0;i<nDataSize;i++){
            m_pdX[i]=dvX.get(i);
            m_pdY[i]=dvY.get(i);
        }
        m_dPCutoff=0.001;
        this.nRisingInterval=nRisingInterval;
        calSD();
        calStraightRanges();
    }
    public StraightlineFitter(ArrayList<Double> dvX, ArrayList<Double> dvY, int nRisingInterval, int nWs, double pCutoff){
        nDataSize=dvX.size();
        m_bHardRange=true;
        m_pdX=new double[nDataSize];
        m_pdY=new double[nDataSize];
        m_pdPredictions=new double[nDataSize];
        m_pcStraightRanges=new intRange[nDataSize];
        m_pdPredictionsL=new double[nDataSize];
        m_pcStraightRangesL=new intRange[nDataSize];
        m_pdPredictionsR=new double[nDataSize];
        m_pcStraightRangesR=new intRange[nDataSize];

        m_pcOptRegressions=new SimpleRegression[nDataSize];
        m_pcStartingRegressions=new SimpleRegression[nDataSize];
        m_pcEndRegressions=new SimpleRegression[nDataSize];
        
        for(int i=0;i<nDataSize;i++){
            m_pdX[i]=dvX.get(i);
            m_pdY[i]=dvY.get(i);
        }
        m_dPCutoff=0.0001;
        m_nWs=nWs;
        this.nRisingInterval=nRisingInterval;
        calSD();
//        calStraightRanges_TripleChoice();
//        calStraightRanges_DoubleChoice();
        calStraightRanges_MinimalSem(m_nWs);
    }
    void calStraightRanges(){
        int i;
        intRange ir;
        SimpleRegression sr;
        for(i=0;i<nDataSize;i++){
            ir=new intRange();
            sr=getLongestSimpleRegression(i,Math.max(0, i-m_nLWs),Math.min(nDataSize-1, i+m_nRWs),ir);
            m_pcStraightRanges[i]=ir;
            m_pdPredictions[i]=sr.predict(m_pdX[i]);
        }
    }
    void calStraightRanges_TripleChoice(){
        int i,j,index;
        intRange ir;
        SimpleRegression sr;
        intRange LimittingRange=new intRange(-m_nWs,m_nWs);
        intRange FittedRanges[]=new intRange[3];

        for(i=0;i<3;i++){
            FittedRanges[i]=new intRange();
        }

        SimpleRegression[] srs=new SimpleRegression[3];

        for(i=0;i<nDataSize;i++){
            for(j=0;j<3;j++){
                m_nLimitL=Math.max(0,i-(2-j)*m_nWs);
                m_nLimitR=Math.min(nDataSize-1,m_nLimitL+2*m_nWs);
                ir=FittedRanges[j];
                srs[j]=getLongestSimpleRegression(i,m_nLimitL,m_nLimitR,ir);
            }
            index=getBestFitterIndex(srs,i);

            m_pcStraightRanges[i]=FittedRanges[index];
            m_pdPredictions[i]=srs[index].predict(m_pdX[i]);
        }
    }
    void calStraightRanges_DoubleChoice(){
        int i,j,index;
        intRange ir;
        SimpleRegression sr;
        intRange LimittingRange=new intRange(-m_nWs,m_nWs);
        intRange FittedRanges[]=new intRange[2];

        for(i=0;i<2;i++){
            FittedRanges[i]=new intRange();
        }
        int[] directions={-1,1};

        SimpleRegression[] srs=new SimpleRegression[2];

        for(i=0;i<nDataSize;i++){
            for(j=0;j<2;j++){
                ir=FittedRanges[j];
                srs[j]=getOptimalStraigSegment_OneDirection(i,i,directions[j],ir);
            }

            index=getBestFitterIndex(srs,i);

            m_pcStraightRanges[i]=new intRange(FittedRanges[index]);

            if(srs[index]!=null)
                m_pdPredictions[i]=srs[index].predict(m_pdX[i]);
            else
                m_pdPredictions[i]=Double.NaN;

            m_pcStraightRangesL[i]=new intRange(FittedRanges[0]);
            if(srs[0]!=null)
                m_pdPredictionsL[i]=srs[0].predict(m_pdX[i]);
            else
                m_pdPredictionsL[i]=Double.NaN;

            m_pcStraightRangesR[i]=new intRange(FittedRanges[1]);
            if(srs[1]!=null)
                m_pdPredictionsR[i]=srs[1].predict(m_pdX[i]);
            else
                m_pdPredictionsR[i]=Double.NaN;
        }
    }

    void calStraightRanges_MinimalSem(int ws){
        m_pcOptimalSegments=new LineFittingSegmentNode[nDataSize];
        m_pcOptimalStartingSegments=new LineFittingSegmentNode[nDataSize];
        m_pcOptimalEndingSegments=new LineFittingSegmentNode[nDataSize];
        LineFittingSegmentNode cCommonNode=new LineFittingSegmentNode();
        LineFittingSegmentNode bestSeg;

        int i,j,index,minLen=2,start,end;
        SimpleRegression sr;

        for(index=0;index<nDataSize;index++){
            if(index==150){
                index=index;
            }
            bestSeg=getBestSegment_Comprehensive(index,minLen,ws);
            start=bestSeg.start;
            end=bestSeg.end;

            for(i=start;i<=end;i++){
                m_pcOptimalSegments[i]=bestSeg.getBetterSegment(bestSeg, m_pcOptimalSegments[i],i);
            }
            m_pcOptimalStartingSegments[start]=bestSeg.getBetterSegment(bestSeg, m_pcOptimalStartingSegments[start],start);
            m_pcOptimalEndingSegments[end]=bestSeg.getBetterSegment(bestSeg, m_pcOptimalEndingSegments[end],end);
        }

        for(index=0;index<nDataSize;index++){
            if(m_pcOptimalStartingSegments[index]==null) {
                m_pcOptimalStartingSegments[index]=m_pcOptimalSegments[index];
            }
            if(m_pcOptimalEndingSegments[index]==null) {
                m_pcOptimalEndingSegments[index]=m_pcOptimalSegments[index];
            }
        }

        for(i=0;i<nDataSize;i++){
            if(m_pcOptimalSegments[i]==null)
                continue;
            m_pcOptRegressions[i]=m_pcOptimalSegments[i].cSR;
            m_pcStartingRegressions[i]=m_pcOptimalStartingSegments[i].cSR;
            m_pcEndRegressions[i]=m_pcOptimalEndingSegments[i].cSR;

            m_pcStraightRanges[i]=new intRange(m_pcOptimalSegments[i].getSegRange());
            m_pcStraightRangesR[i]=new intRange(m_pcOptimalStartingSegments[i].getSegRange());
            m_pcStraightRangesL[i]=new intRange(m_pcOptimalEndingSegments[i].getSegRange());

            m_pdPredictions[i]=m_pcOptRegressions[i].predict(m_pdX[i]);
            m_pdPredictionsR[i]=m_pcStartingRegressions[i].predict(m_pdX[i]);
            m_pdPredictionsL[i]=m_pcEndRegressions[i].predict(m_pdX[i]);
        }
    }

    LineFittingSegmentNode getBestSegment_Comprehensive(int start, int minLen, int ws){
        if(start>nDataSize-minLen) 
            start=nDataSize-1-minLen;
        LineFittingSegmentNode bestSeg=null,currentSeg;
        int end;
        int len;
        for(len=minLen;len<=ws;len++){
            end=start+len-1;
            if(end>=nDataSize) return bestSeg;
            currentSeg=new LineFittingSegmentNode();
            currentSeg.update(start, end);
            registerSegment(currentSeg);
            bestSeg=currentSeg.getBetterSegment(bestSeg, currentSeg,start);
        }
        return bestSeg;
    }
    LineFittingSegmentNode getBestSegment(int start, int minLen, int ws){
        if(start>nDataSize-1-minLen) start=nDataSize-1-minLen;
        LineFittingSegmentNode bestSeg=null,currentSeg;
        int end,num;
        SimpleRegression sr;
        intRange irCovered;
        ArrayList<Integer> cvCrossPoints=new ArrayList();
        int len,index;
        for(len=minLen;len<=ws;len++){
            cvCrossPoints.clear();
            end=Math.min(nDataSize-1, start+len-1);
            sr=CommonStatisticsMethods.getSimpleLinearRegression(m_pdX, m_pdY, start,end);
            irCovered=pickAcceptableRange_Ondirection(sr,cvCrossPoints,start,1);
            num=cvCrossPoints.size();
            if(num>0){
                end=Math.min(end, cvCrossPoints.get(num-1)-1);
                if(end<start) end=start;
            }else{
                end=start;
            }
            if(end-start+1<minLen) {
                if(len==minLen)
                    end=start+len-1;
                else continue;
            }
            currentSeg=new LineFittingSegmentNode();
            currentSeg.update(start, end);
            bestSeg=currentSeg.getBetterSegment(bestSeg, currentSeg,start);
        }
        return bestSeg;
    }
    void registerSegment(LineFittingSegmentNode seg){
        int i,start=seg.start,end=seg.end;
        for(i=start;i<=end;i++){
            m_pcOptimalSegments[i]=seg.getBetterSegment(seg, m_pcOptimalSegments[i],i);
        }
        m_pcOptimalStartingSegments[start]=seg.getBetterSegment(seg, m_pcOptimalStartingSegments[start],start);
        m_pcOptimalEndingSegments[end]=seg.getBetterSegment(seg, m_pcOptimalEndingSegments[end],end);
    }
    void calStraightRanges_DoubleChoice0(){
        int i,j,index;
        intRange ir;
        SimpleRegression sr;
        intRange LimittingRange=new intRange(-m_nWs,m_nWs);
        intRange FittedRanges[]=new intRange[2];

        for(i=0;i<2;i++){
            FittedRanges[i]=new intRange();
        }

        SimpleRegression[] srs=new SimpleRegression[2];

        for(i=0;i<nDataSize;i++){
            for(j=0;j<2;j++){
                m_nLimitL=Math.max(0,i+(j-1)*2*m_nWs);
                m_nLimitR=Math.min(nDataSize-1,i+j*2*m_nWs);
                if(m_nLimitR==m_nLimitL){
                    if(i==0)
                        m_nLimitR=1;
                    else
                        m_nLimitL=nDataSize-2;
                }
                ir=FittedRanges[j];
                srs[j]=getLongestSimpleRegression(i,m_nLimitL,m_nLimitR,ir);
            }


            index=getBestFitterIndex(srs,i);

            m_pcStraightRanges[i]=new intRange(FittedRanges[index]);
            m_pdPredictions[i]=srs[index].predict(m_pdX[i]);
            m_pcStraightRangesL[i]=new intRange(FittedRanges[0]);
            m_pdPredictionsL[i]=srs[0].predict(m_pdX[i]);
            m_pcStraightRangesR[i]=new intRange(FittedRanges[1]);
            m_pdPredictionsR[i]=srs[1].predict(m_pdX[i]);
        }
    }
    int getBestFitterIndex(SimpleRegression[] srs, int index){
        int i,in=0;
        double dn=Double.POSITIVE_INFINITY,dt;
        for(i=0;i<srs.length;i++){
            if(srs[i]==null) continue;
            dt=Math.abs(m_pdY[index]-srs[i].predict(m_pdX[index]));
            if(dt<dn){
                in=i;
                dn=dt;
            }
        }
        return in;
    }
    void  calSD(){
        double pdDelta[]=new double[nDataSize-nRisingInterval];
        int i;
        for(i=0;i<nDataSize-nRisingInterval;i++){
            pdDelta[i]=m_pdY[i]-m_pdY[i+nRisingInterval];
        }
        CommonStatisticsMethods.makeMedianReflection(pdDelta);
        MeanSem0 ms=CommonStatisticsMethods.buildMeanSem(pdDelta);
        m_dSD=ms.getSD()/Math.sqrt(2);
    }
    void findLinearRanges(){

    }
    SimpleRegression getLongestSimpleRegression(int index, int left0, int right0, intRange indexRange){
        int left=left0,right=right0;

        SimpleRegression sr=CommonStatisticsMethods.getSimpleLinearRegression(m_pdX,m_pdY, left, right);
//        ArrayList<Integer> indexes=getCrosspoints(sr,left, right);
//        ArrayList<intRange> ranges=getExcessivelyDeviatedRanges(sr,indexes);
        intRange ir=pickAcceptableRange(sr,index);
        if(m_bHardRange) ir.setRange(Math.max(m_nLimitL, ir.getMin()), Math.min(m_nLimitR, ir.getMax()));
        ArrayList<intRange> ranges=new ArrayList();
        ArrayList<intRange> TrialRanges=new ArrayList();
        ArrayList<SimpleRegression> srs=new ArrayList();
        int delta=0;
        while(!containsContent(ranges,ir)){
            if(ir.emptyRange()){
                if(index>left){
                    delta=Math.max((index-left)/3,1);
                    left+=delta;
                    if(left==right) left--;
                }
                if(right>index){
                    delta=Math.max((right-index)/3, 1);
                    right-=delta;
                    if(right==left) right++;
                }
                if(containsContent(TrialRanges,new intRange(left,right))) break;
//                if(right<index+1) right=Math.min(nDataSize-1,index+1);
            }else{
                left=ir.getMin();
                right=ir.getMax();
            }

            if(!ir.emptyRange()){
                ranges.add(ir);
                srs.add(sr);
            }else{
                TrialRanges.add(new intRange(left,right));
            }

            sr=CommonStatisticsMethods.getSimpleLinearRegression(m_pdX,m_pdY, left, right);
            ir=pickAcceptableRange(sr,index);
            if(m_bHardRange) ir.setRange(Math.max(m_nLimitL, ir.getMin()), Math.min(m_nLimitR, ir.getMax()));
        }
        int ix=getLongestRangeIndex(ranges);
        if(!ranges.isEmpty())
            ir=ranges.get(ix);
        else{
            ir=new intRange(index,index);
            if(index==0){
                left=0;
                right=1;
            }else{
                left=index-1;
                right=index;
            }
            return CommonStatisticsMethods.getSimpleLinearRegression(m_pdX,m_pdY, left, right);
        }

        indexRange.setRange(ir.getMin(), ir.getMax());
        return srs.get(ix);
    }

    SimpleRegression getOptimalStraigSegment_OneDirection(int index0, int left0, int direction, intRange indexRange){
        int left=left0,right=index0+direction;
        int limit=nDataSize-1;
        if(direction<0) limit=0;
        if((limit-right)*direction<0){
            right-=direction;
            if(left==right) left-=direction;
        }
        intRange ir;
        ArrayList<Integer> crossPoints=new ArrayList();

        ArrayList<SimpleRegression> srs=new ArrayList();
        ArrayList<intRange> ranges=new ArrayList();
        ArrayList<Integer> nvNumCrossPoints=new ArrayList();
        ArrayList<Integer> nvLastCrossPoints=new ArrayList();
        int num,nX=0;
        SimpleRegression sr=null;

        while((limit-right)*direction>=0){
            crossPoints.clear();
            sr=CommonStatisticsMethods.getSimpleLinearRegression(m_pdX,m_pdY, left, right,direction);
            ir=pickAcceptableRange_Ondirection(sr,crossPoints,left,direction);
            num=crossPoints.size();
            right+=direction;
            if(num==0) continue;
            if(num<nX) continue;
            if(num>nX) nX=num;
            srs.add(sr);
            nvLastCrossPoints.add(crossPoints.get(num-1));
            nvNumCrossPoints.add(crossPoints.size());
        }

        right-=direction;

        int i,j,len=srs.size(),in=0;
        for(i=len-1;i>=0;i--){
            if(nvNumCrossPoints.get(i)<nX){
                nvNumCrossPoints.remove(i);
                srs.remove(i);
                nvLastCrossPoints.remove(i);
            }
        }
        
        len=srs.size();
        double devN=Double.POSITIVE_INFINITY,sse,y,yt;
        for(i=0;i<len;i++){
            sse=0;
            sr=srs.get(i);
            j=index0;
            num=0;
            while((nvLastCrossPoints.get(i)-j)*direction>=0){
                y=m_pdY[j];
                yt=sr.predict(m_pdX[j]);
                sse+=(y-yt)*(y-yt);
                j+=direction;
                num++;
            }
            if(num==0) continue;
            sse/=num;
            if(sse<devN){
                devN=sse;
                in=i;
            }
        }

        int end;

        if(nvLastCrossPoints.size()>in){
            sr=srs.get(in);
            end=nvLastCrossPoints.get(in);
        }
        else{
            end=left0;
        }

        if(direction>0)
            indexRange.setRange(index0, end);
        else
            indexRange.setRange(end,index0);
        
        return sr;
    }

    boolean containsContent(ArrayList<intRange> ranges, intRange ir){
        int len=ranges.size(),i;
        for(i=0;i<len;i++){
            if(ranges.get(i).equivalent(ir)) return true;
        }
        return false;
    }

    int getLongestRangeIndex(ArrayList<intRange> ranges){
        int len=ranges.size(),i;
        intRange ir;
        int lx=-1,lt,ix=0;
        for(i=0;i<len;i++){
            ir=ranges.get(i);
            lt=ir.getRange();
            if(lt>lx){
                lx=lt;
                ix=i;
            }
        }
        return ix;
    }

    intRange pickAcceptableRange(SimpleRegression sr, int index0){
        if(!isAcceptableDeviation(sr,index0)) return new intRange();
        int l0=index0,r0=index0,l,r;
        l=getNextCrosspoint(sr,index0,-1);
        if(l<0) l=0;
        l0=l;
        r=getNextCrosspoint(sr,index0,1);
        while(isAcceptableDeviation(sr,l,Math.max(r-1, l))){
            r0=r;
            l=r;
            r=getNextCrosspoint(sr,r,1);
            if(r==-1) {
                r=nDataSize-1;
                if(isAcceptableDeviation(sr,l,r)){
                    r0=r;
                }
                break;
           }
       }

//        if(l0==l&&r<nDataSize-1) return new intRange();//the section that contains index is invalid
        while(isAcceptableDeviation(sr,r0)){
            r0++;
            if(r0>=nDataSize){
                break;
            }
        }
        r0--;

        r=l0;
        l=getNextCrosspoint(sr,r,-1);
        if(l==-1) l=0;
        while(isAcceptableDeviation(sr,Math.min(l+1, r),r)){
            l0=l;
            r=l;
            l=getNextCrosspoint(sr,l,-1);
            if(l==-1) {
                l=0;
                if(isAcceptableDeviation(sr,l,r)){
                    l0=l;
                }
                break;
            }
        }
        while(isAcceptableDeviation(sr,l0)){
            l0--;
            if(l0<0){
                break;
            }
        }
        l0++;
        return new intRange(l0,r0);

    }
    intRange pickAcceptableRange_Onedirection(SimpleRegression sr,int index0, int direction){
        int index=index0;
        int limit=nDataSize-1;
        if(direction<0) limit=0;
        while(isAcceptableDeviation(sr,index)){
             index+=direction;
             if((limit-index)*direction<0) break;
        }
        intRange ir;
        if(direction>0)
            ir=new intRange(index0,index-direction);
        else
            ir=new intRange(index-direction,index0);
        return ir;
    }
    intRange pickAcceptableRange_Ondirection(SimpleRegression sr, ArrayList<Integer> crossPoints,int index0, int direction){
        if(!isAcceptableDeviation(sr,index0)) return new intRange();
        int limit=nDataSize-1,it;
        if(direction<0) limit=0;
        int start=index0,end=getNextCrosspoint(sr,index0,direction);
        if(end<0) {
            return pickAcceptableRange_Onedirection(sr,index0,direction);
        }

        intRange ir;
        while(isAcceptableDeviation(sr,start,end-1,direction)){
            crossPoints.add(end);
            start=end;
            end=getNextCrosspoint(sr,start,direction);
            if(end<0){
                end=limit;
                break;
            }
        }

        if(crossPoints.size()>0)
            end=crossPoints.get(crossPoints.size()-1)-1;
        else
            end=index0;
/*
        ir=pickAcceptableRange_Onedirection(sr,end,direction);
        if(direction>0)
            ir=new intRange(index0,ir.getMax());
        else
            ir=new intRange(ir.getMin(),index0);*/

        return new intRange(start,end);
    }
    ArrayList<Integer> getCrosspoints(SimpleRegression sr, int left, int right){
        ArrayList<Integer> cps=new ArrayList();
        int index=getNextCrosspoint(sr,left,-1);
        if(index>=0) cps.add(index);
        if(m_pdY[left]==sr.predict(m_pdX[left])) cps.add(left);
        index=getNextCrosspoint(sr,left,1);
        while(index>0){
            cps.add(index);
            if(index>=right) break;
            index=getNextCrosspoint(sr,index,1);
        }
        return cps;
    }
    int getNextCrosspoint(SimpleRegression sr, int index, int direction){
        double d0=m_pdY[index]-sr.predict(m_pdX[index]),d;
        index+=direction;
        if(index<0||index>=nDataSize) return -1;
        d=m_pdY[index]-sr.predict(m_pdX[index]);
        while(d*d0>0){
            index+=direction;
            if(index<0||index>=nDataSize) return -1;
            d=m_pdY[index]-sr.predict(m_pdX[index]);
        }
        return index;
    }
    boolean isAcceptableDeviation(SimpleRegression sr, int start,int end){
        return isAcceptableDeviation(sr,start,end,1);
    }
    boolean isAcceptableDeviation(SimpleRegression sr, int start,int end, int delta){
        double mean=0;
        int i=start,num=0;
        while((end-i)*delta>=0){
            mean+=m_pdY[i]-sr.predict(m_pdX[i]);
            i+=delta;
            num++;
        }
        mean/=num;
        double p=1-GaussianDistribution.Phi(Math.abs(mean), 0, m_dSD/Math.sqrt(num));
        return p>m_dPCutoff;
    }
    boolean isAcceptableDeviation_Sum(SimpleRegression sr, int start,int end, int delta){
        double mean=0;
        int i=start,num=0;
        while((end-i)*delta>=0){
            mean+=m_pdY[i]-sr.predict(m_pdX[i]);
            i+=delta;
            num++;
        }
//        mean/=num;
        double p=1-GaussianDistribution.Phi(Math.abs(mean), 0, m_dSD/Math.sqrt(num));
        return p>m_dPCutoff;
    }
    boolean isAcceptableDeviation(SimpleRegression sr, int index){
        double p=1-GaussianDistribution.Phi(Math.abs(m_pdY[index]-sr.predict(m_pdX[index])), 0, m_dSD);
        return p>m_dPCutoff;
    }
    public intRange[] getStraightRanges(){
        return m_pcStraightRanges;
    }
    public double[] getPredictions(){
        return m_pdPredictions;
    }
    public intRange[] getStraightRangesL(){
        return m_pcStraightRangesL;
    }
    public double[] getPredictionsL(){
        return m_pdPredictionsL;
    }
    public intRange[] getStraightRangesR(){
        return m_pcStraightRangesR;
    }
    public double[] getPredictionsR(){
        return m_pdPredictionsR;
    }
    public SimpleRegression[] getOptimalRegressions(){
        return m_pcOptRegressions;
    }
    public SimpleRegression[] getStartingRegressions(){
        return m_pcStartingRegressions;
    }
    public SimpleRegression[] getEndingRegressions(){
        return m_pcEndRegressions;
    }
    double getTotalDeviation(SimpleRegression sr, int start, int end){
        double dev=0;
        for(int i=start;i<end;i++){
            dev+=Math.abs(m_pdY[i]-sr.predict(m_pdX[i]));
        }
        return dev;
    }
}
