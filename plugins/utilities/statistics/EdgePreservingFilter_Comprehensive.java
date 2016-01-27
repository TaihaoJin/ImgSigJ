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
import javax.xml.stream.events.StartDocument;

/**
 *
 * @author Taihao
 */
public class EdgePreservingFilter_Comprehensive {
    public static final int EdgeProtection=1, RankingExtrema=2;
    class ExtremaAdjustNode{
        public int left,right,start,end,peak,base,sign,subPeak,subBase;
        ArrayList<Integer> subpeakExtrema;
        ArrayList<intRange> nvApplicableRanges;
        double dL,dR,dPeak,dBase,dSubBase;
        public ExtremaAdjustNode(){
            subPeak=-1;
            subpeakExtrema=new ArrayList();
            nvApplicableRanges=new ArrayList();
        }
        public ExtremaAdjustNode(int sign, int leftE, int rightE){
            this();
            this.sign=sign;            
            ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
            
            if(sign==-1){
                nv0=m_nvLn;
                nv1=m_nvLx;
            }
            int l0=nv0.size(),l1=nv1.size();
                        
            if(nv0.get(0)<nv1.get(0)){
                start=leftE+1;
                end=rightE;
            }else{
                start=leftE;
                end=rightE-1;
            }
                        
            if(leftE==-1){//this situation should not happen under new definition, but this block is kept incase for possible changes in the future
                left=0;
                right=nv1.get(rightE);
                dR=m_pdY[nv1.get(rightE)];
                dL=dR;
                base=right;
                subBase=base;
            }else if(rightE==l1){//this situation should not happen under new definition, but this block is kept incase for possible changes in the future
                left=nv0.get(leftE);
                right=m_pdY.length-1;
                dL=m_pdY[nv1.get(leftE)];
                dR=dL;
                base=left;
                subBase=base;
            }else{//this is the only case should be happing under the new definition
                left=nv1.get(leftE);
                right=nv1.get(rightE);
                dL=m_pdY[left];
                dR=m_pdY[left];
                if(sign*(dL-dR)>0) {
                    base=left;
                    subBase=right;
                }else{
                    base=right;
                    subBase=left;
                }
                if(leftE==0)
                    left=0;
                if(rightE==l1-1)
                    right=m_pdY.length-1;
            }            
            
            dBase=m_pdY[base];
            dSubBase=m_pdY[subBase];
            int ix=nv0.get(start),i;
            dPeak=m_pdY[ix];
            peak=start;
            double dt;
            subpeakExtrema=new ArrayList();
            for(i=start+1;i<=end;i++){
                ix=nv0.get(i);
                dt=m_pdY[ix];
                if(sign*(dt-dPeak)>0){
                    dPeak=dt;
                    peak=i;
                }
                if(sign*(dt-dBase)<0&&sign*(dt-dSubBase)>0){
                    subpeakExtrema.add(i);
                }
            }
        }
        public void applyFilter(){
            intRange ir;
            MeanSem1 ms=msNN;
            if(sign<0){
                ms=msXX;
            }
            int i,len=nvApplicableRanges.size(),j;
            double ds=ms.getSD(),delta=dL-dR,wb=10*Math.exp(-delta*delta/(ds*ds)),wp=1,dY0,dY;
    //        if(m_nMaxMultiplicity>1){
            if(true){//not to use this right now
                wb=4*Math.exp(-delta*delta/(ds*ds));
                wp=Math.exp(-1);
            }else{
                wb=4;
                wp=1;
            }
    //        dPeak=0.5*(dPeak0+dBase);
            double dPeak1=(dPeak*wp+dBase*wb)/(wb+wp);
            int position;
            for(i=0;i<len;i++){
                ir=nvApplicableRanges.get(i);
                for(position=ir.getMin();position<=ir.getMax();position++){
                    dY0=m_pdYFilted0[position];
        /*            if((dY0-dBase)*sign<=0) {
                        if(adjNode.subPeak>=0){
                            subBase=nv1.get(adjNode.subBase);
                            subPeak=nv0.get(adjNode.subPeak);
                            dSubPeak0=m_pdYFilted0[subPeak];
                            dSubBase=m_pdYFilted0[subBase];
                            dSubPeak=(wp*dSubPeak0+dSubBase*wb)/(wp+wb);
                            dY=CommonMethods.getLinearIntoplation(dSubBase, dSubBase, dSubPeak0, dSubPeak, dY0);
                            m_pdYFilted[position]=dY;
                            adjNode.subpeakExtrema.add(index);
                        }
                        continue;
                    }*/
                    dY=CommonMethods.getLinearIntoplation(dBase, dBase, dPeak, dPeak1, dY0);
                    m_pdYFilted[position]=dY;
                }
            }
        }
        
        //sign==1 for local maxima and -1 for local minima
        //"start" and "end" are the indexes of same sign extrema, indicating the first and last extrema of the node.
        //the multiplicity of the node equals to end-start+1.
        //"left" and "right" the positions (note, unlike "start" and "end", "left" and "right" is not the indexes of extrema.)
        //of the oposite sign extrema. The extremum (oposite sign) "left" is the oposite sign extrema immediately left to the extremum (same sign) "start". 
        //"right" is immediately right to the extremum "end".
        //"peak" is indexe of same sign extremum. Y[peak]*sign>=Y[i]*sign, where Y[i] indicate the value of a same sign extremum whose index is i.
        //"base" is either "left" or "right", Y[base]*sign=max(Y[left]*sign,Y[right]*sign),
        //all oposite sign extrema in the node (between left and right) insure that Y[left]*sign<Y[j] and Y[right]*sign<Y[j].
        //subBase is left or right that is not base. subPeak is the the same sign extrema whose value is 
        //closest to base among all same sign extrema whose values are in between base and subBase
    }
    ArrayList<Integer> m_nvLx,m_nvLn,m_nvLXX,m_nvLNN,m_nvExtrema;
    public ArrayList<Integer> m_nvBasePointsN,m_nvBasePointsP;
    MeanSem1 msXX,msXN,msNN;
    int m_nMaxMultiplicity;
    double[] m_pdX,m_pdY,m_pdYFilted0,m_pdYFilted;
//    ArrayList<ExtremaAdjustNode> m_cvAdjustNodes;
    boolean[] pbValid,pbModified;
    int[] m_pnProbeContacting;//1 for touching down (the probe is on upper side), and -1 for touching up, and 0 for none probe contacting points, respectively.
    
    double[] pdDeltaMatrixX,pdDeltaMatrixN;// The matrix of delta Y between neighboring extrema.
    int[] pnMinDeltaLn,pnMinDeltaLx;//For any given extrema, this array stores index of the same sign extrema (within nMaxMulitiplicity) with smallest delta Y     
    ExtremaAdjustNode[] pcOptimalAdjNodesX, pcOptimalAdjNodesN; //adjust nodes between two min delta extrema
    ExtremaAdjustNode[] pcOptimalAdjNodes; //adjust nodes of 
    
    public EdgePreservingFilter_Comprehensive(double[] pdX, double[] pdY){
        m_pdY=pdY;
        m_pdX=pdX;
        if(m_nvLn==null) m_nvLn=new ArrayList();
        m_nvLn.clear();
        if(m_nvLx==null) m_nvLx=new ArrayList();
        m_nvLx.clear();
        CommonMethods.LocalExtrema(pdY, m_nvLn, m_nvLx);
        pbValid=CommonStatisticsMethods.getEmptyBooleanArray(pbValid, m_pdX.length);
        CommonStatisticsMethods.setElements(pbValid, true);
        setDefaultProbeContacting();
        calDeviations();
    }
    public void setDefaultProbeContacting(){
        int i,len=m_nvLx.size();
        m_pnProbeContacting=CommonStatisticsMethods.getEmptyIntArray(m_pnProbeContacting, m_pdY.length);
        CommonStatisticsMethods.setElements(m_pnProbeContacting, 0);
        for(i=0;i<len;i++){
            m_pnProbeContacting[m_nvLx.get(i)]=1;
        }
        len=m_nvLn.size();
        for(i=0;i<len;i++){
            m_pnProbeContacting[m_nvLn.get(i)]=-1;
        }
    }
    public void setProbeContacting(ArrayList<Integer> nvPoints, int status){
        int i,len=nvPoints.size();
        for(i=0;i<len;i++){
            m_pnProbeContacting[i]=status;
        }
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
    void calDeviationsXX(){
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
        if(pdXX.length>nMinLen) CommonStatisticsMethods.findOutliars(pdXX, 0.001, msXX, nvOutliars);

        ArrayList<Integer> nvT=new ArrayList();
        CommonMethods.LocalExtrema(pdNN, m_nvLNN, nvT);
        nn=m_nvLNN.size();
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
        if(pdNN.length>nMinLen) CommonStatisticsMethods.findOutliars(pdNN, 0.001, msNN, nvOutliars);
    }
    public static double getDeltaPreservingWeightedAverage(double dx0, double dx, double dx1, double ds,double sign){//assume (dx-dx0)*(dx-dx1)>0
        double dxn=Math.min(dx0, dx1),dxx=Math.max(dx0, dx1),ave,delta0,delta=dxx-dxn,w,wn,wx,var=ds*ds,dxm;
        if(sign>0){//sign should not be zero
            delta0=dx-dxx-ds/2;
            w=Math.exp(-delta0*delta0/var);
            w=1;
            wx=1;
            wn=Math.exp(-delta*delta/var);
            wn=0;
        }else{
            delta0=dxn-dx-ds/2;
            w=Math.exp(-delta0*delta0/var);
            w=1;
            wn=1;
            wx=Math.exp(-delta*delta/var);
            wx=0;
        }
        ave=dxn*wn+dxx*wx+dx*w;
        ave/=w+wn+wx;
        return ave;
    }
/*    double[] pdDeltaMatrixX,pdDeltaMatrixN;// The matrix of delta Y between neighboring extrema.
    int[] pnMinDeltaLn,pnMinDeltaLx;//For any given extrema, this array stores index of the same sign extrema (within nMaxMulitiplicity) with smallest delta Y     
    ExtremaAdjustNode[] pcOptimalAdjNodesX, pcOptimalAdjNodesN; //adjust nodes between two min delta extrema
    ExtremaAdjustNode[] pcOptimalAdjNodes; //adjust nodes of */
    void buildAdjustNodes(int multiplicity,int sign){
        ArrayList<Integer> nvExtrema=m_nvLx;
        if(sign>0){
            nvExtrema=m_nvLn;
        }
        int i,len=nvExtrema.size();
        double[][] pdDeltaMatrix=CommonStatisticsMethods.getDeltaYMatrix(m_pdY,nvExtrema,Double.POSITIVE_INFINITY,m_nMaxMultiplicity);
        ExtremaAdjustNode[] pcOptimalAdjNodes=buildMinDeltaAdjNodes(nvExtrema,sign,pdDeltaMatrix,multiplicity);
        findApplicableRanges(pcOptimalAdjNodes,sign);
        
        if(sign>0)
            this.pcOptimalAdjNodesX=pcOptimalAdjNodes;
        else
            this.pcOptimalAdjNodesN=pcOptimalAdjNodes;
    }
    
    void findApplicableRanges(ExtremaAdjustNode[] pcOptimalAdjNodes,int sign){
        int i,j,len=m_pdY.length,in;
        int[] pnBestNodeIndexes=new int[len];
        double[] pdMinDelta=new double[len];
        ExtremaAdjustNode aNode;
        CommonStatisticsMethods.setElements(pdMinDelta, Double.POSITIVE_INFINITY);
        CommonStatisticsMethods.setElements(pnBestNodeIndexes, -1);
        double delta,y,dBase;
        for(i=0;i<pcOptimalAdjNodes.length;i++){
            aNode=pcOptimalAdjNodes[i];
            delta=Math.abs(aNode.dL-aNode.dR);
            dBase=aNode.dBase;
            for(j=aNode.left;j<=aNode.right;j++){
                if(sign*(m_pdY[j]-dBase)<=0) continue;
                if(delta<pdMinDelta[j]){
                    pdMinDelta[j]=delta;
                    pnBestNodeIndexes[j]=i;
                }
            }
        }
        
        int index0=-1,index;
        intRange ir=null;
        for(i=0;i<len;i++){
            index=pnBestNodeIndexes[i];
            if(index<0) continue;
            if(index!=index0){
                aNode=pcOptimalAdjNodes[index];
                ir=new intRange(i,i);
                aNode.nvApplicableRanges.add(ir);
            }else{
                ir.expandRange(i);
            }
            index0=index;
        }
    }
    
    ExtremaAdjustNode[] buildMinDeltaAdjNodes(ArrayList<Integer> nvExtrema, int sign, double[][] pdDeltaMatrix, int ws){
        int len=nvExtrema.size();
        ExtremaAdjustNode[] pcAdjNodes=new ExtremaAdjustNode[len];
        int i,iT,left,right;
        for(i=0;i<len;i++){
            iT=CommonStatisticsMethods.minAbsDeltaPosition(pdDeltaMatrix[i],ws,0,2*ws);
            iT+=(i-ws);
            if(iT>i){
                left=i;
                right=iT;
            }else{
                left=iT;
                right=i;
            }
            pcAdjNodes[i]=new ExtremaAdjustNode(sign,left,right);
        }
        return pcAdjNodes;
    }
    public double[] getTransitionPreservingFiltedData(int multiplicity, int iterations){
        m_pdYFilted=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted, m_pdY.length);
        m_pdYFilted0=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted0, m_pdY.length);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted0);
        iterations=1;
        multiplicity=5;
        for(int i=0;i<iterations;i++){
//            multiplicity=1;
            m_nMaxMultiplicity=multiplicity;
            buildAdjustNodes(multiplicity,-1);
            filterData_AdjustingNode(-1);
            if(i<10000) continue;
            CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdYFilted0);
            buildAdjustNodes(multiplicity,1);
            filterData_AdjustingNode(1);
            CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdYFilted0);
            multiplicity++;
        }
        return m_pdYFilted;
    }
    void filterData_AdjustingNode(int sign){
        ExtremaAdjustNode[] pcAdjNodes=pcOptimalAdjNodesX;
        if(sign<0) pcAdjNodes=pcOptimalAdjNodesN;
        int i,len=pcAdjNodes.length,j,len1;
        ExtremaAdjustNode adjNode;
        for(i=0;i<len;i++){
            adjNode=pcAdjNodes[i];
            adjNode.applyFilter();
        }
    }
/*    void filterData_peaks(ExtremaAdjustNode adjNode){
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
        MeanSem1 ms=msNN;
        if(adjNode.sign<0){
            nv0=m_nvLn;
            nv1=m_nvLx;
            ms=msXX;
        }
        int index,left=adjNode.left,right=adjNode.right,sign=adjNode.sign,peak=nv0.get(adjNode.peak),position;
        int subPeak,subBase;
        double dBase=m_pdYFilted[nv1.get(adjNode.base)],dPeak0=m_pdYFilted0[peak],dPeak,dY0,dY,dLeft,dRight;
        double dSubBase,dSubPeak,dSubPeak0;

        if(left==-1){
            if(nv0.get(0)>0){
                dBase=m_pdYFilted0[0];
            } else {
                dBase = m_pdYFilted0[nv1.get(right)];
            }
            dLeft=dBase;
            dRight=m_pdYFilted0[nv1.get(right)];
        }else if(right==-1){
            right=m_pdY.length-1;
            dBase=m_pdYFilted0[right];
            left=nv1.get(left);
            if(sign*(dPeak0-dBase)>sign*(dPeak0-m_pdYFilted0[left])) dBase=m_pdYFilted0[left];
            dRight=m_pdYFilted0[left];
            dLeft=m_pdYFilted0[left];
        }else{
            dLeft=m_pdYFilted0[nv1.get(left)];
            dRight=m_pdYFilted0[nv1.get(right)];
        }
        
        if(peak==32){
            peak=peak;
        }

        if(nv1.get(Math.max(0,adjNode.left))==40){
            left=left;
        }
        double ds=ms.getSD(),delta=dLeft-dRight,wb=10*Math.exp(-delta*delta/(ds*ds)),wp=1;
//        if(m_nMaxMultiplicity>1){
        if(false){//not to use this right now
            wb=4*Math.exp(-delta*delta/(ds*ds));
            wp=Math.exp(-1);
        }else{
            wb=4;
            wp=1;
        }
//        dPeak=0.5*(dPeak0+dBase);
        dPeak=(dPeak0*wp+dBase*wb)/(wb+wp);

        for(index=adjNode.start;index<=adjNode.end;index++){
            position=nv0.get(index);
            dY0=m_pdYFilted0[position];
            if(position==19){
                position=position;
            }
            if((dY0-dBase)*sign<=0) {
                if(adjNode.subPeak>=0){
                    subBase=nv1.get(adjNode.subBase);
                    subPeak=nv0.get(adjNode.subPeak);
                    dSubPeak0=m_pdYFilted0[subPeak];
                    dSubBase=m_pdYFilted0[subBase];
                    dSubPeak=(wp*dSubPeak0+dSubBase*wb)/(wp+wb);
                    dY=CommonMethods.getLinearIntoplation(dSubBase, dSubBase, dSubPeak0, dSubPeak, dY0);
                    m_pdYFilted[position]=dY;
                    adjNode.subpeakExtrema.add(index);
                }
                continue;
            }
            dY=CommonMethods.getLinearIntoplation(dBase, dBase, dPeak0, dPeak, dY0);
            m_pdYFilted[position]=dY;
        }

    }*/
    void filterData_peaks(ExtremaAdjustNode adjNode, intRange ir){
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
        MeanSem1 ms=msNN;
        if(adjNode.sign<0){
            nv0=m_nvLn;
            nv1=m_nvLx;
            ms=msXX;
        }

        double dLeft=adjNode.dL,dRight=adjNode.dR,dPeak0=adjNode.dPeak,dBase=adjNode.dBase,dY0,dY;
        double ds=ms.getSD(),delta=dLeft-dRight,wb=10*Math.exp(-delta*delta/(ds*ds)),wp=1;
//        if(m_nMaxMultiplicity>1){
        if(true){//not to use this right now
            wb=4*Math.exp(-delta*delta/(ds*ds));
            wp=Math.exp(-1);
        }else{
            wb=4;
            wp=1;
        }
//        dPeak=0.5*(dPeak0+dBase);
        double dPeak=(dPeak0*wp+dBase*wb)/(wb+wp);
        int position;

        for(int index=ir.getMin();index<=ir.getMax();index++){
            position=nv0.get(index);
            dY0=m_pdYFilted0[position];
/*            if((dY0-dBase)*sign<=0) {
                if(adjNode.subPeak>=0){
                    subBase=nv1.get(adjNode.subBase);
                    subPeak=nv0.get(adjNode.subPeak);
                    dSubPeak0=m_pdYFilted0[subPeak];
                    dSubBase=m_pdYFilted0[subBase];
                    dSubPeak=(wp*dSubPeak0+dSubBase*wb)/(wp+wb);
                    dY=CommonMethods.getLinearIntoplation(dSubBase, dSubBase, dSubPeak0, dSubPeak, dY0);
                    m_pdYFilted[position]=dY;
                    adjNode.subpeakExtrema.add(index);
                }
                continue;
            }*/
            dY=CommonMethods.getLinearIntoplation(dBase, dBase, dPeak0, dPeak, dY0);
            m_pdYFilted[position]=dY;
        }

    }
    void filterData_AdjustingNode(ExtremaAdjustNode adjNode, intRange ir){//this is after the peak positions have been adjusted
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
        int shift=0;
        if(adjNode.sign<0){
            nv0=m_nvLn;
            nv1=m_nvLx;
            shift=1;
        }
        int index,left=adjNode.left,right=adjNode.right,sign=adjNode.sign,peak=nv0.get(adjNode.peak),base=nv1.get(adjNode.base);
        double dBase0=m_pdYFilted0[base],dPeak0=m_pdYFilted0[peak],dPeak=m_pdYFilted[peak],dY0,dY;
        if(left<0)
            left=0;
        else
            left=nv1.get(left);
        if(right<0)
            right=m_pdY.length-1;
        else
            right=nv1.get(right);

        if(left==16){
            left=left;
        }
        for(index=left;index<right;index++){
            dY0=m_pdYFilted0[index];
            if(sign*(dY0-dBase0)>=0){
                dY=CommonMethods.getLinearIntoplation(dBase0, dBase0, dPeak0, dPeak, dY0);

                m_pdYFilted[index]=dY;
            }
        }
/*          
        int sing=adjNode.sign,position;
        if(adjNode.subPeak>=0){
            int subBase=nv1.get(adjNode.subBase);
            int subPeak=nv0.get(adjNode.subPeak);
            double dSubPeak0=m_pdYFilted0[subPeak];
            double dSubBase=m_pdYFilted0[subBase];
            double dSubPeak=m_pdYFilted[subPeak];
            for(int i=0;i<adjNode.subpeakExtrema.size();i++){
                index=adjNode.subpeakExtrema.get(i);
                if(index==0){
                    left=0;
                    if(sign>0)
                        right=nv1.get(index);
                    else
                        right=nv1.get(index+1);
                } else if(index==nv0.size()-1){
                    right=m_pdY.length-1;
                    if(sign>0){
                        left=nv1.get(index-1);
                    }else{
                        left=nv1.get(index);
                    }
                }else{
                    if(sign>0){
                        left=nv1.get(index-1);
                        right=nv1.get(index);
                    }else{
                        left=nv1.get(index);
                        right=nv1.get(index+1);
                    }
                }
                for(position=left;position<=right;position++){
                    dY0=m_pdYFilted0[position];
                    dY=CommonMethods.getLinearIntoplation(dSubBase, dSubBase, dSubPeak0, dSubPeak, dY0);
                    m_pdYFilted[position]=dY;
                }
            }
        }*/
    }
/*    void filterData_AdjustingNode(ExtremaAdjustNode adjNode){//this is after the peak positions have been adjusted
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
        int shift=0;
        if(adjNode.sign<0){
            nv0=m_nvLn;
            nv1=m_nvLx;
            shift=1;
        }
        int index,left=adjNode.left,right=adjNode.right,sign=adjNode.sign,peak=nv0.get(adjNode.peak),base=nv1.get(adjNode.base);
        double dBase0=m_pdYFilted0[base],dBase=m_pdYFilted[base],dPeak0=m_pdYFilted0[peak],dPeak=m_pdYFilted[peak],dY0,dY;
        if(left<0)
            left=0;
        else
            left=nv1.get(left);
        if(right<0)
            right=m_pdY.length-1;
        else
            right=nv1.get(right);

        if(left==16){
            left=left;
        }
        for(index=left;index<right;index++){
            dY0=m_pdYFilted0[index];
            if(sign*(dY0-dBase0)>=0){
                dY=CommonMethods.getLinearIntoplation(dBase0, dBase0, dPeak0, dPeak, dY0);

                m_pdYFilted[index]=dY;
            }
        }
          
        int sing=adjNode.sign,position;
        if(adjNode.subPeak>=0){
            int subBase=nv1.get(adjNode.subBase);
            int subPeak=nv0.get(adjNode.subPeak);
            double dSubPeak0=m_pdYFilted0[subPeak];
            double dSubBase=m_pdYFilted0[subBase];
            double dSubPeak=m_pdYFilted[subPeak];
            for(int i=0;i<adjNode.subpeakExtrema.size();i++){
                index=adjNode.subpeakExtrema.get(i);
                if(index==0){
                    left=0;
                    if(sign>0)
                        right=nv1.get(index);
                    else
                        right=nv1.get(index+1);
                } else if(index==nv0.size()-1){
                    right=m_pdY.length-1;
                    if(sign>0){
                        left=nv1.get(index-1);
                    }else{
                        left=nv1.get(index);
                    }
                }else{
                    if(sign>0){
                        left=nv1.get(index-1);
                        right=nv1.get(index);
                    }else{
                        left=nv1.get(index);
                        right=nv1.get(index+1);
                    }
                }
                for(position=left;position<=right;position++){
                    dY0=m_pdYFilted0[position];
                    dY=CommonMethods.getLinearIntoplation(dSubBase, dSubBase, dSubPeak0, dSubPeak, dY0);
                    m_pdYFilted[position]=dY;
                }
            }
        }
    }*/
}
