/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.CommonStatisticsMethods;
import utilities.CommonMethods;
import utilities.CustomDataTypes.intRange;
import java.util.ArrayList;
import utilities.statistics.MeanSem1;
import utilities.CustomDataTypes.DoubleRange;
import java.util.ArrayDeque;

/**
 *
 * @author Taihao
 */
public class SpikeHandler {
    public static final int RelativePeak=1, HeteroPeak=2;//RelativePeak indicates a spike is detected for abnormal height relative to its neighboring, same sign extrema.
    //HeteroPeak indicates a spike is detected based on its abnormally height relative to the
    class SpikeNode{
        int peak,base,start,end,left,right,sign,type;
        //start, end, and peak are extrema indexes. sign 1 stants of upward spikes, and sign -1 indicates downward spikes.
        //left and right are the indexes of the oposite sign extrema immediately left and right to the start and end extremas, respectively.
        //base is left or right based on the Y value at the positions. Y(base)*sign>=Y(left)*sign && Y(base)*sign>=Y(right)*sign
        public intRange getAffectedRange(){
            if(Math.abs(type)==RelativePeak) {
                if(sign>0)
                    return new intRange(m_nvLx.get(start-1),m_nvLx.get(end+1));
                else
                    return new intRange(m_nvLn.get(start-1),m_nvLn.get(end+1));
            }else{
                if(sign>0)
                    return new intRange(m_nvLn.get(left),m_nvLn.get(right));
                else
                    return new intRange(m_nvLx.get(left),m_nvLx.get(right));
            }
        }
        public boolean affected(){
            intRange ir=getAffectedRange();
            for(int i=ir.getMin();i<=ir.getMax();i++){
                if(pbModified[i]) return true;
            }
            return false;
        }
    }
    ArrayList<Integer> m_nvLx,m_nvLn,m_nvLXX,m_nvLNN,m_nvExtrema;
    ArrayList<intRange> m_cvSpikeRanges,m_cvSpikeRangesDownward,m_cvSpikeRangesUpward;
    MeanSem1 msXX,msXN,msNN;
    int m_nMaxMultiplicity;
    double[] m_pdX,m_pdY,m_pdYFilted0,m_pdYFilted,m_pdAveDiffs;
    double m_dPValue;
    boolean[] pbValid,pbModified;
    int[] m_pnSpikeTypes;
    ArrayList<SpikeNode> m_cvSpikeNodes;
    public SpikeHandler(double[] pdX, double[] pdY,int nMaxMultiplicity, double pValue){        
        m_dPValue=pValue;
        m_pdY=pdY;
        m_pdX=pdX;
        m_nMaxMultiplicity=Math.abs(nMaxMultiplicity);
        if(m_nvLn==null) m_nvLn=new ArrayList();
        m_nvLn.clear();
        if(m_nvLx==null) m_nvLx=new ArrayList();
        m_nvLx.clear();
        pbValid=CommonStatisticsMethods.getEmptyBooleanArray(pbValid, m_pdX.length);
        CommonStatisticsMethods.setElements(pbValid, true);
        m_cvSpikeNodes=new ArrayList();
        
//        if(nMaxMultiplicity>0)
//            findSpikes();
//        else
//            calDeviations();
    }
    void findSpikes(){
        m_cvSpikeNodes.clear();
        CommonMethods.LocalExtrema(m_pdY, m_nvLn, m_nvLx);
        int status=1;
        calDeviations();
        status=calDeviationsXX();
        if(status>0) findSpikes(1);
        if(status>0) findSpikes(-1);
        findSpikes_HeteralExtrema(1);
        findSpikes_HeteralExtrema(-1);
        m_cvSpikeRanges=CommonStatisticsMethods.mergeRanges(m_cvSpikeRangesUpward,m_cvSpikeRangesDownward);
    }
    
    public void calDeviations(){
        if(msXX==null) msXX=new MeanSem1();
        if(msXN==null) msXN=new MeanSem1();
        if(msNN==null) msNN=new MeanSem1();
        
        ArrayList<Integer> nvLx=new ArrayList(), nvLn=new ArrayList();

        int i,nn=m_nvLn.size(),nx=m_nvLx.size(),ix,in,ix0,in0,ix1,in1,index;
        
        for(i=0;i<nn;i++){
            index=m_nvLn.get(i);
            if(pbValid[index]) nvLn.add(index);
        }
        
        for(i=0;i<nx;i++){
            index=m_nvLx.get(i);
            if(pbValid[index]) nvLx.add(index);
        }
        
        nn=nvLn.size();
        nx=nvLx.size();
        
        int len=Math.min(nn,nx);
        double[] pdXX=new double[len-1],pdXN=new double[2*(len-1)],pdNN=new double[len-1];
        ix0=nvLx.get(0);
        in0=nvLn.get(0);
        int ixn=0;
        for(i=1;i<len;i++){
            ix=m_nvLx.get(i);
            if(ix==35){
                i=i;
            }
            in=m_nvLn.get(i);
            pdXX[i-1]=m_pdY[ix]-m_pdY[ix0];
            pdXN[ixn]=Math.abs(m_pdY[ix]-m_pdY[in0]);
            ixn++;
            pdXN[ixn]=Math.abs(m_pdY[in]-m_pdY[ix]);
            ixn++;
            pdNN[i-1]=m_pdY[in]-m_pdY[in0];
            ix0=ix;
            in0=in;
        }
        ArrayList<Integer> nvOutliars=new ArrayList();
        CommonStatisticsMethods.findOutliars(pdXX, 0.01, msXX, nvOutliars);
        CommonStatisticsMethods.findOutliars(pdXN, 0.01, msXN, nvOutliars);
        CommonStatisticsMethods.findOutliars(pdNN, 0.01, msNN, nvOutliars);
    }
    int calDeviationsXX(){
        int nMinLen=5;
        if(msXX==null) msXX=new MeanSem1();
        if(msNN==null) msNN=new MeanSem1();

        int i,nn=m_nvLn.size(),nx=m_nvLx.size(),ix,in,ix0,in0,ix1,in1;
        int len=Math.max(nx, nn);
        double[] pdXX=new double[nx],pdNN=new double[nn];
        ix0=m_nvLx.get(0);
        in0=m_nvLn.get(0);
        for(i=0;i<len;i++){
            if(i<nx){
                ix=m_nvLx.get(i);
                pdXX[i]=m_pdY[ix];
            }
            if(i<nn){
                in=m_nvLn.get(i);
                pdNN[i]=m_pdY[in];
            }
        }
        m_nvLXX=new ArrayList();
        m_nvLNN=new ArrayList();

        CommonMethods.LocalExtrema(pdXX, m_nvLNN, m_nvLXX);
        double delta,dx,dn;

        nx=m_nvLXX.size();
        if(nx<=2) return -1;
        pdXX=new double[nx-2];
        for(i=1;i<nx-1;i++){
            ix=m_nvLXX.get(i);
            ix0=ix-1;
            ix1=ix+1;
            ix=m_nvLx.get(ix);
            dx=m_pdY[ix];

            ix0=m_nvLx.get(ix0);
            ix1=m_nvLx.get(ix1);
            delta=dx-Math.max(m_pdY[ix0],m_pdY[ix1]);

            pdXX[i-1]=delta;
        }
        ArrayList<Integer> nvOutliars=new ArrayList();
        if(pdXX.length>nMinLen) CommonStatisticsMethods.findOutliars(pdXX, 0.01, msXX, nvOutliars);

        ArrayList<Integer> nvT=new ArrayList();
        CommonMethods.LocalExtrema(pdNN, m_nvLNN, nvT);
        nn=m_nvLNN.size();
        if(nn<=2) return -1;
        pdNN=new double[nn-2];
        for(i=1;i<nn-1;i++){
            in=m_nvLNN.get(i);
            in0=in-1;
            in1=in+1;
            if(in1>=m_nvLn.size()){
                i=i;
            }
            in=m_nvLn.get(in);
            dn=m_pdY[in];

            in0=m_nvLn.get(in0);
            in1=m_nvLn.get(in1);
            delta=Math.min(m_pdY[in0],m_pdY[in1])-dn;

            pdNN[i-1]=delta;
        }
        if(pdNN.length>nMinLen) CommonStatisticsMethods.findOutliars(pdNN, 0.01, msNN, nvOutliars);
        return 1;
    }
    
    public void findSpikes(int sign){//detect spikes based on neighboring same sign extrema.
        if(m_cvSpikeRangesUpward==null) m_cvSpikeRangesUpward=new ArrayList();
        if(m_cvSpikeRangesDownward==null) m_cvSpikeRangesDownward=new ArrayList();
//        m_nMaxMultiplicity=2;
        ArrayList<Integer> nvExtrema=m_nvLx;
        ArrayList<intRange> spikeRanges=m_cvSpikeRangesUpward;
        ArrayList<Integer> nvExtremaC=m_nvLn;
        int shift=-1;
        MeanSem1 ms=msXX,msC=msNN;

        if(sign<0){
            nvExtrema=m_nvLn;
            nvExtremaC=m_nvLx;
            shift=0;
            spikeRanges=m_cvSpikeRangesDownward;
            ms=msNN;
            msC=msXX;
        }
        spikeRanges.clear();

        int nn=m_nvLn.size(),nx=m_nvLx.size(),ix,ix0=0,ix1=0,peak,base;
        int len=Math.min(nn,nx);

        double ds=ms.getSD();
        double cutoffXX=GaussianDistribution.getZatP(1-m_dPValue, ms.mean, ds, ds*0.01);
        double dLowCutoff=GaussianDistribution.getZatP(0.95, msC.mean, msC.getSD(), ds*0.01);

        double dx0,dx,dx1,delta,dPeak;
        boolean spike=false;
        int left,right,start,end;
        left=0;
        right=0;
        while(right<len){
            ix0=nvExtrema.get(left);
            dx0=m_pdY[ix0];

            dx1=dx0;

            start=left+1;
            if(start>=len) break;
            ix=nvExtrema.get(start);
            dx=m_pdY[ix];
            if(ix==56){
                ix=ix;
            }
            delta=dx-dx0;
            if(delta*sign<cutoffXX||!pbValid[start]) {
                left=start;
                continue;
            }

            end=start;
            peak=start;
            dPeak=m_pdY[nvExtrema.get(peak)];
            while(end-start<m_nMaxMultiplicity){
                if(!pbValid[end]){
                    end++;
                    dx=m_pdY[nvExtrema.get(end)];
                    continue;
                }
                spike=false;
                right=end+1;
                if(right>=len) break;
                ix1=nvExtrema.get(right);
                dx1=m_pdY[ix1];
                delta=dx-dx1;
                if(sign*(dx-dPeak)>0){
                    peak=end;
                    dPeak=dx;
                }
                if(delta*sign>cutoffXX&&Math.abs(dx1-dx0)<dLowCutoff) {
//                if(delta*sign>cutoffXX) {
                    spike=true;
                    break;
                }
                end=right;
                ix=ix1;
                dx=dx1;
            }
            if(right>=len) break;

            if(nvExtrema.get(peak)==83){
                peak=peak;
            }
            if(spike){//a spike has been detected
//                spikeRanges.add(new intRange(nvExtremaC.get(left+shift)+1, nvExtremaC.get(right+shift-1)-1));
                spikeRanges.add(new intRange(nvExtremaC.get(start+shift)+1, nvExtremaC.get(end+shift+1)-1));
                SpikeNode spikeNode=new SpikeNode();
                spikeNode.type=sign;
                spikeNode.sign=sign;
                spikeNode.start=start;
                spikeNode.end=end;
                spikeNode.peak=peak;
                if(sign>0){
                    spikeNode.left=left;
                    spikeNode.right=end;
                }else{
                    spikeNode.left=start;
                    spikeNode.right=right;
                }
                spikeNode.base=spikeNode.left;
                if(sign*(m_pdY[nvExtremaC.get(spikeNode.right)]-m_pdY[nvExtremaC.get(spikeNode.left)])>0) spikeNode.base=spikeNode.right;
                m_cvSpikeNodes.add(spikeNode);
                left=right;
            }else{
                left++;
            }
        }
    }

    void findSpikes_HeteralExtrema(int sign){//detect spikes based on neighboring oposite sign extrema.
        if(m_cvSpikeRangesUpward==null) m_cvSpikeRangesUpward=new ArrayList();
        if(m_cvSpikeRangesDownward==null) m_cvSpikeRangesDownward=new ArrayList();
//        m_nMaxMultiplicity=1;
        ArrayList<Integer> nvExtrema=m_nvLx;
        ArrayList<intRange> spikeRanges=m_cvSpikeRangesUpward;
        ArrayList<Integer> nvExtremaC=m_nvLn;
        int shift=1;
        MeanSem1 ms=msXN,msLow=msNN;

        if(sign<0){
            nvExtrema=m_nvLn;
            nvExtremaC=m_nvLx;
            shift=0;
            spikeRanges=m_cvSpikeRangesDownward;
            msLow=msXX;
        }
//        spikeRanges.clear();

        int nn=m_nvLn.size(),nx=m_nvLx.size(),ix,ix0=0,ix1=0,peak;
        int len=Math.min(nn,nx);

        double ds=ms.getSD();
        double cutoffXN=GaussianDistribution.getZatP(1-m_dPValue, ms.mean, ds, ds*0.01);
        
        if(sign>0)
            ds=msNN.getSD();
        else
            ds=msXX.getSD();
        double dLowCutoff=GaussianDistribution.getZatP(0.99, msLow.mean, ds, ds*0.01);

        double dx0,dx,dx1=0,delta,dPeak;
        boolean spike=false;
        int left,right,start,end;
        left=0;
        right=0;
        int numExtrema=nvExtrema.size();
        while(right<len){
            if(left>=nvExtremaC.size()) break;
            ix0=nvExtremaC.get(left);
            dx0=m_pdY[ix0];

            start=left+shift;
            if(start>=len) break;
            ix=nvExtrema.get(start);
            dx=m_pdY[ix];
            if(ix==47){
                ix=ix;
            }
            delta=dx-dx0;
            if(delta*sign<cutoffXN) {
                left++;
                continue;
            }
            peak=start;
            dPeak=dx;

            end=start;
            right=left+1;
            while(end-start<m_nMaxMultiplicity){
                spike=false;
                if(right>=len||end>=len) break;
                ix1=nvExtremaC.get(right);
                dx1=m_pdY[ix1];
                delta=dx-dx1;
//                if(delta*sign>cutoffXN&&Math.abs(dx1-dx0)<dLowCutoff) {
                if(delta*sign>cutoffXN) {
                    spike=true;
                    break;
                }
                end++;
                if(end>=numExtrema) break;
                ix=nvExtrema.get(end);
                if(ix==39){
                    ix=ix;
                }
                dx=m_pdY[ix];
                if(sign*(dx-dPeak)>0){
                    peak=end;
                    dPeak=dx;
                }
                right++;
            }
            if(right>=len) break;

            if(spike){//a spike has been detected
                spikeRanges.add(new intRange(ix0+1,ix1-1));
                SpikeNode spikeNode=new SpikeNode();
                spikeNode.type=2*sign;
                spikeNode.sign=sign;
                spikeNode.start=start;
                spikeNode.end=end;
                spikeNode.peak=peak;
                spikeNode.base=left;
                if(sign*(dx1-dx0)>0) spikeNode.base=right;
                spikeNode.left=left;
                spikeNode.right=right;
                m_cvSpikeNodes.add(spikeNode);
                left=right;
            }else{
                left++;
            }
        }
    }
    public ArrayList<intRange> getSpikeRanges(){
        return m_cvSpikeRanges;
    }
    public ArrayList<intRange> getSpikeRanges(int sign){
        if(sign>0) return m_cvSpikeRangesUpward;
        if(sign<0) return m_cvSpikeRangesDownward;
        return m_cvSpikeRanges;
    }
    public double[] removeSpikes_Progressive(int nMaxMultiplicity){
        double[] pbYOriginal=CommonStatisticsMethods.copyArray(m_pdY);
        pbModified=CommonStatisticsMethods.getEmptyBooleanArray(pbModified, m_pdY.length);
        m_pdYFilted=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted, m_pdY.length);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted);
        for(int multiplicity=1;multiplicity<=nMaxMultiplicity;multiplicity++){
            m_nMaxMultiplicity=multiplicity;
            findSpikes();
            calAveDiffs();
            SpikeNode spike;
            int i,len=m_cvSpikeNodes.size();
            int maxIter=10;
            int iter=0;
            while(len>0&&iter<=maxIter){
                CommonStatisticsMethods.setElements(pbModified, false);
                sortSpikes();
                for(i=0;i<len;i++){
                    spike=m_cvSpikeNodes.get(i);
                    if(spike.sign>0) continue;
                    if(spike.affected()) continue;
                    if(Math.abs(spike.type)==SpikeHandler.HeteroPeak)
                        removeSpike_Hetero(spike);
//                    else
//                        removeSpike_Relative(spike);
                }
                for(i=0;i<len;i++){
                    spike=m_cvSpikeNodes.get(i);
                    if(spike.sign<0) continue;
                    if(spike.affected()) continue;
                    if(Math.abs(spike.type)==SpikeHandler.HeteroPeak)
                        removeSpike_Hetero(spike);
//                    else
//                        removeSpike_Relative(spike);
                }
                CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdY);
                findSpikes();
                len=m_cvSpikeNodes.size();
                iter++;
            }
        }
        CommonStatisticsMethods.copyArray(pbYOriginal, m_pdY);
        return m_pdYFilted;
    }
    void sortSpikes(){
        int i,len=m_cvSpikeNodes.size();
        SpikeNode spike;
        ArrayList<SpikeNode> cvSpikes=new ArrayList();
        int[] indexes=new int[len];
        double[] pdStarts=new double[len];
        for(i=0;i<len;i++){
            spike=m_cvSpikeNodes.get(i);
            if(spike.sign>0)
                pdStarts[i]=m_nvLx.get(spike.start);
            else
                pdStarts[i]=m_nvLn.get(spike.start);
            indexes[i]=i;
        }
        utilities.QuickSort.quicksort(pdStarts, indexes);

        int index;
        for(i=0;i<len;i++){
            index=indexes[i];
            cvSpikes.add(m_cvSpikeNodes.get(index));
        }
        m_cvSpikeNodes.clear();
        m_cvSpikeNodes=cvSpikes;
    }
    void calAveDiffs(){
        int i,j,position,type0,type,len=m_pdY.length,num=10,len0=m_nvLx.size(),len1=m_nvLn.size(),position0,left,right,sign,base;
        ArrayList<Integer> nvExtrema,nvExtremaC;
        m_pnSpikeTypes=CommonStatisticsMethods.getEmptyIntArray(m_pnSpikeTypes, len);
        CommonStatisticsMethods.setElements(m_pnSpikeTypes, 0);
        double dBase;
        SpikeNode spike;
        for(i=0;i<m_cvSpikeNodes.size();i++){
            spike=m_cvSpikeNodes.get(i);
            sign=spike.sign;
            if(sign>0){
                nvExtrema=m_nvLx;
                nvExtremaC=m_nvLn;
            }else{
                nvExtrema=m_nvLn;
                nvExtremaC=m_nvLx;
            }
            type=spike.type;
            left=nvExtremaC.get(spike.left);
            right=nvExtremaC.get(spike.right);
            base=nvExtremaC.get(spike.base);
            dBase=m_pdY[base];
            for(position=left+1;position<=right;position++){
                if(sign*(m_pdY[position]-dBase)<0) continue;
                type0=m_pnSpikeTypes[position];
                if(Math.abs(type)>Math.abs(type0)) m_pnSpikeTypes[position]=type;
            }
        }

        m_pdAveDiffs=CommonStatisticsMethods.getEmptyDoubleArray(m_pdAveDiffs, len);
        ArrayDeque<Double> diffs=new ArrayDeque();
        double sd=msXN.getSD(),cutoff=GaussianDistribution.getZatP(1-m_dPValue, msXN.mean, sd, 0.01*sd),delta,ave,mean=msXN.mean;
        
        for(i=0;i<num;i++){
            diffs.add(mean);
        }

        position0=0;
        ave=mean*num;

        CommonStatisticsMethods.setElements(m_pdAveDiffs, ave/num);

        for(i=0;i<len0;i++){
            position=m_nvLx.get(i);
            if(i>0){
                position0=m_nvLn.get(i-1);
                delta=Math.abs(m_pdY[position]-m_pdY[position0]);
                if(delta<cutoff){
                    diffs.add(delta);
                    ave+=delta;
                    ave-=diffs.poll();
                }
            }else{
                position0=0;
            }
            for(j=position0;j<position;j++){
                m_pdAveDiffs[j]=ave/num;
            }
            position0=position;
            if(i<len1){
                position=m_nvLn.get(i);
                delta=Math.abs(m_pdY[position]-m_pdY[position0]);
                if(delta<cutoff){
                    diffs.add(delta);
                    ave+=delta;
                    ave-=diffs.poll();
                }
                for(j=position0;j<position;j++){
                    m_pdAveDiffs[j]=ave/num;
                }
            }else{
                position=len-1;
                for(j=position0;j<=position;j++){
                    m_pdAveDiffs[j]=ave/num;
                }
            }
        }
    }
    int removeSpike_Relative(SpikeNode spike){
        if(Math.abs(spike.type)!=RelativePeak) return -1;
        ArrayList<Integer> nvExtrema,nvExtremaC;
        if(spike.sign>0){
            nvExtrema=m_nvLx;
            nvExtremaC=m_nvLn;
        }else{
            nvExtrema=m_nvLn;
            nvExtremaC=m_nvLx;
        }
        int i,len=nvExtrema.size(),type0,type=spike.type=Math.abs(spike.type),sign=spike.sign;
//        if(spike.left<1||spike.right>=len) return -1;
        int left=nvExtremaC.get(spike.left),right=nvExtremaC.get(spike.right),i0=nvExtrema.get(spike.start-1),i1=nvExtrema.get(spike.end+1),peak=nvExtrema.get(spike.peak),base=nvExtremaC.get(spike.base);
        double y0=m_pdY[i0],y1=m_pdY[i1],dPeak0=m_pdY[peak],dBase=m_pdY[base],dPeak=CommonMethods.getLinearIntoplation(i0,y0,i1,y1,peak),dt;

        if(peak==35){
            peak=peak;
        }

        for(i=left+1;i<right;i++){
            dt=m_pdY[i];
            if(sign*(dt-dBase)<0) continue;
            if(pbModified[i]) break;//
            m_pdYFilted[i]=CommonMethods.getLinearIntoplation(dBase, dBase, dPeak0, dPeak, m_pdY[i]);
            pbModified[i]=true;
        }
        return 1;
    }
    int removeSpike_Hetero(SpikeNode spike){
        if(Math.abs(spike.type)!=HeteroPeak) return -1;
        ArrayList<Integer> nvExtrema,nvExtremaC;
        MeanSem1 ms;
        if(spike.sign>0){
            ms=msNN;
            nvExtrema=m_nvLx;
            nvExtremaC=m_nvLn;
        }else{
            ms=msXX;
            nvExtrema=m_nvLn;
            nvExtremaC=m_nvLx;
        }
        int i,len0=nvExtrema.size(),len1=nvExtremaC.size(),type0,type=spike.type=Math.abs(spike.type);
        int left=nvExtremaC.get(spike.left),right=nvExtremaC.get(spike.right),peak=nvExtrema.get(spike.peak),base=nvExtremaC.get(spike.base);
        double dBase=m_pdY[base],dPeak0=m_pdY[peak],dPeak=dBase+spike.sign*m_pdAveDiffs[peak],dt;
        if(left==68){
            left=left;
        }
        
        if(peak==69){
            left=left;
        }
        if(left<69&&right>69){
            left=left;
        }
        double sd=ms.getSD(),delta=m_pdY[left]-m_pdY[right],wb=10*Math.exp(-delta*delta/(sd*sd));
        dPeak=(wb*dPeak+dPeak0)/(wb+1);
        if(peak==35||peak==34||peak==39){
            peak=peak;
        }
        for(i=left+1;i<right;i++){
            dt=m_pdY[i];
            if(spike.sign*(dt-dBase)<0) continue;
            m_pdYFilted[i]=CommonMethods.getLinearIntoplation(dBase, dBase, dPeak0, dPeak, dt);
            pbModified[i]=true;
        }
        return 1;

    }
    public void setSelection(ArrayList<Integer> nvSelection, boolean bAppend){
        int i,len=nvSelection.size();
        if(!bAppend) CommonStatisticsMethods.setElements(pbValid, false);
        for(i=0;i<len;i++){
            pbValid[nvSelection.get(i)]=true;
        }
    }
}
