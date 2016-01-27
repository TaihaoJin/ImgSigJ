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
public class EdgePreservingFilter {
    public static final int EdgeProtection=1, RankingExtrema=2;
    class ExtremaAdjustNode{
        public int left,right,start,end,peak,base,sign,subPeak,subBase;
        ArrayList<Integer> subpeakExtrema;
        public ExtremaAdjustNode(){
            subPeak=-1;
            subpeakExtrema=new ArrayList();
        }
        //sign==1 for local maxima and -1 for local minima
        //start and end are the indexes of same sign extrema, indicating the start and the end of the node.
        //the multiplicity of the node equals to end-start+1.
        //left and right the indexes of the oposite sign extrema. The extremum (oposite sign) left is immediately left to the extremum (same sign) start. also right is immediately right to the extremum end.
        //peak is indexe of same sign extremum. Y(peak)*sign>=Y(i)*sign, where Y(i) indicate the value of a same sign extremum whose index is i.
        //base is left or right, Y(base)*sign=max(Y(left)*sign,Y(right)*sign),
        //all oposite sign extrema in the node (between left and right) insure that Y(left)*sign<Y(j) and Y(right)*sign<Y(j).
        //subBase is left or right that is not base. subPeak is the the same sign extrema whose value is 
        //closest to base among all same sign extrema whose values are in between base and subBase
    }
    ArrayList<Integer> m_nvLx,m_nvLn,m_nvLXX,m_nvLNN,m_nvExtrema,m_nvRankingMaxima,m_nvRankingMinima;
    public ArrayList<Integer> m_nvBasePointsN,m_nvBasePointsP;
    MeanSem1 msXX,msXN,msNN;
    int m_nMaxMultiplicity;
    int m_nAdjustmentType;
    double[] m_pdX,m_pdY,m_pdYFilted0,m_pdYFilted;
    ArrayList<ExtremaAdjustNode> m_cvAdjustNodes;
    boolean[] pbValid,pbModified;
    int[] m_pnProbeContacting;//1 for touching down (the probe is on upper side), and -1 for touching up, and 0 for none probe contacting points, respectively.
    public EdgePreservingFilter(double[] pdX, double[] pdY,int nMaxMultiplicity){
        m_pdY=pdY;
        m_pdX=pdX;
        m_nMaxMultiplicity=Math.abs(nMaxMultiplicity);
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
    public double[] getTransitionPreservingFiltedData(int multiplicity, int iterations){
        m_pdYFilted=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted, m_pdY.length);
        m_pdYFilted0=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted0, m_pdY.length);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted0);
        m_cvAdjustNodes=new ArrayList();
        multiplicity=3;
        iterations=2;
        for(int i=0;i<iterations;i++){
//            multiplicity=1;
            m_nMaxMultiplicity=multiplicity;
            m_cvAdjustNodes.clear();
            buildAdjustNodes(multiplicity,-1);
            filterData_AdjustingNode(-1);
            CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdYFilted0);
            if(false) continue;
            m_cvAdjustNodes.clear();
            buildAdjustNodes(multiplicity,1);
            filterData_AdjustingNode(1);
            CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdYFilted0);
            multiplicity++;
        }
        return m_pdYFilted;
    }
    public double[] getTransitionPreservingFiltedData_RankingExtrema(ArrayList<Integer> nvX, ArrayList<Integer> nvN, int iterations){
        m_pdYFilted=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted, m_pdY.length);
        m_pdYFilted0=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted0, m_pdY.length);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted0);
        m_nvRankingMaxima=nvX;
        m_nvRankingMinima=nvN;
        for(int i=0;i<iterations;i++){
//            getTransitionPreservingFiltedData1(multiplicity);
            m_cvAdjustNodes.clear();
            buildAdjustNodes_RankingExtrema();
            filterData_AdjustingNode(-1);
            CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdYFilted0);
            m_cvAdjustNodes.clear();
            buildAdjustNodes_RankingExtrema();
            filterData_AdjustingNode(-1);
            CommonStatisticsMethods.copyArray(m_pdYFilted, m_pdYFilted0);
        }
        return m_pdYFilted;
    }
    void filterData_AdjustingNode(int sign){
        int i,len=m_cvAdjustNodes.size();
        ExtremaAdjustNode adjNode;
        for(i=0;i<len;i++){
            adjNode=m_cvAdjustNodes.get(i);
            if(adjNode.sign!=sign) continue;
            filterData_peaks(adjNode);
            filterData_AdjustingNode(adjNode);
        }
    }
    void filterData_peaks(ExtremaAdjustNode adjNode){
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
        if(left==-1&&right==-1){
            right=nv1.size()-1;
        }
        if(left==-1){
//            if(nv0.get(0)>0){
                dBase=m_pdYFilted0[0];
                if(sign*(dBase-m_pdYFilted0[nv1.get(right)])<0) dBase = m_pdYFilted0[nv1.get(right)];
/*            } else {
                dBase = m_pdYFilted0[nv1.get(right)];
            }*/
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
            wb=10;
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

    }
    void filterData_AdjustingNode(ExtremaAdjustNode adjNode){//this is after the peak positions have been adjusted
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
        int shift=0;
        if(adjNode.sign<0){
            nv0=m_nvLn;
            nv1=m_nvLx;
            shift=1;
        }
        int index,left=adjNode.left,right=adjNode.right,sign=adjNode.sign,peak=nv0.get(adjNode.peak),base=nv1.get(adjNode.base);
        double dBase0=m_pdYFilted0[base],dBase=m_pdYFilted[base],dPeak0=m_pdYFilted0[peak],dPeak=m_pdYFilted[peak],dY0,dY;
        if(left<0&&right<0){
            left=0;
            right=nv1.size()-1;
        }else if(left<0)
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
    }
    public void buildAdjustNodes(int multiplicity,int sign){
        m_nAdjustmentType=EdgeProtection;
        m_nvBasePointsN=new ArrayList();
        m_nvBasePointsP=new ArrayList();
        int i,len=m_pdY.length,ix,start,end,peak,lenX=m_nvLx.size(),lenN=m_nvLn.size(),lastIndex=-1;

        double dx,dxx,dxn,dxt,dl;
        DoubleRange cYRange=CommonStatisticsMethods.getRange(m_pdYFilted0);
        m_cvAdjustNodes=new ArrayList();
        ExtremaAdjustNode adjNode;

        int left=0, right,j,rt;
        boolean bEnd;
        if(sign==1){
            for(i=0;i<m_nvLx.size();i++){//m_nvLn.get(i)<m_nvLx.get(i)<m_nvLn.get(i),lenX>=lenN
                if(i<=lastIndex) continue;
                left=-1;
                right=-1;
                start=i;
                end=i;
                peak=i;

                if(i>0) {
                    left=i-1;
                    dl=m_pdYFilted0[m_nvLn.get(left)];
                }else{
                    dl=m_pdYFilted0[0];
                }

                ix=m_nvLx.get(i);
                dx=m_pdYFilted0[ix];
                if(ix==22){
                    ix=ix;
                }

                dxn=cYRange.getMax();
                for(j=i;j<Math.min(i+multiplicity,m_nvLx.size());j++){
                    if(j<m_nvLn.size()){
                        rt=m_nvLn.get(j);
                        dxt=m_pdYFilted0[rt];
                    }else{
                        rt=m_pdY.length-1;
                        dxt=m_pdYFilted[rt];
                    }
                    if(dxt<dxn){
                        if(j<m_nvLn.size())
                            right=j;
                        else{
                            right=-1;
                        }
                        dxn=dxt;
                        end=j;
                    }
                    if(dxt<=dl) break;
                }

                peak=start;
                dxx=m_pdYFilted0[m_nvLx.get(peak)];
                for(j=start;j<=end;j++){
                    ix=m_nvLx.get(j);
                    dxt=m_pdYFilted0[ix];
                    if(dxt>dxx) {
                        peak=j;
                        dxx=dxt;
                    }
                }

                adjNode=new ExtremaAdjustNode();
                adjNode.start=start;
                adjNode.end=end;
                adjNode.left=left;
                adjNode.right=right;
                adjNode.sign=1;
                adjNode.peak=peak;

                if(left<0&&right<0){//happening when m_nvLn.size()<nMultiplicity
                    if (m_pdYFilted0[m_nvLn.get(0)] >= m_pdYFilted0[m_nvLn.get(m_nvLn.size()-1)]){
                        adjNode.base=0;
                    } else {
                        adjNode.base=m_nvLn.size()-1;
                    }
                }else if(left<0){
                    adjNode.base=right;
                }else if(right<0){
                    adjNode.base=left;
                } else if (m_pdYFilted0[m_nvLn.get(left)] >= m_pdYFilted0[m_nvLn.get(right)])
                    adjNode.base=left;
                else
                    adjNode.base=right;
                if(adjNode.base==-1){
                    i=i;
                }
                m_nvBasePointsP.add(m_nvLn.get(adjNode.base));
                m_cvAdjustNodes.add(adjNode);
                lastIndex=end;
            }
        }
        
        if(sign==-1){
            lastIndex=-1;
            for(i=0;i<m_nvLn.size();i++){
                if(i<=lastIndex) continue;
                left=i;
                right=-1;
                start=i;
                end=i;

                ix=m_nvLn.get(i);
                dx=m_pdYFilted0[ix];
                if(ix==32){
                    ix=ix;
                }

                dxn=dx;
                dxx=cYRange.getMin();
                dl=m_pdYFilted0[m_nvLx.get(left)];

                for(j=i+1;j<=Math.min(i+multiplicity,m_nvLx.size()-1);j++){
                    if(j<m_nvLx.size()){
                        rt=m_nvLx.get(j);
                        if(rt==32){
                            rt=rt;
                        }
                        dxt=m_pdYFilted0[rt];
                    }else {
                        rt=m_pdY.length-1;
                        dxt=m_pdYFilted0[rt];
                    }
                    if(dxt>dxx){
                        right=j;
                        dxx=dxt;
                        end=j-1;
                    }
                    if(dxt>dl) break;
                }

                peak=start;
                dxn=m_pdYFilted0[m_nvLn.get(peak)];
                for(j=start;j<=end;j++){
                    ix=m_nvLn.get(j);
                    dxt=m_pdYFilted0[ix];
                    if(dxt<dxn){
                        peak=j;
                        dxn=dxt;
                    }
                }

                adjNode=new ExtremaAdjustNode();
                adjNode.start=start;
                adjNode.end=end;
                adjNode.left=left;
                adjNode.right=right;
                adjNode.sign=-1;
                adjNode.peak=peak;
                if(right<0){
                    adjNode.base=left;
                }else if(m_pdYFilted0[m_nvLx.get(left)]<=m_pdYFilted0[m_nvLx.get(right)])
                    adjNode.base=left;
                else
                    adjNode.base=right;
                m_nvBasePointsN.add(m_nvLx.get(adjNode.base));
                m_cvAdjustNodes.add(adjNode);
                lastIndex=end;
            }
        }
    }
    public void buildAdjustNodes_RankingExtrema(){
        m_nAdjustmentType=RankingExtrema;
        m_nvBasePointsN=new ArrayList();
        m_nvBasePointsP=new ArrayList();
        int i,len=m_pdY.length,ix,start,end,peak,lenX=m_nvLx.size(),lenN=m_nvLn.size(),lastIndex=-1,base,subBase,subPeak;

        double dx,dxx,dxn,dxns,dxxs,dBase;
        DoubleRange cYRange=CommonStatisticsMethods.getRange(m_pdYFilted0);
        m_cvAdjustNodes=new ArrayList();
        ExtremaAdjustNode adjNode;

        int left=0, right,j,rt;
        boolean bEnd;

        int index0=0,index=0,lastMaximum=m_nvLx.get(lenX-1),lastMinimum=m_nvLn.get(lenN-1);
        len=m_nvRankingMinima.size();
        for(i=0;i<=len;i++){//m_nvLn.get(i)<m_nvLx.get(i)<m_nvLn.get(i),lenX>=lenN
            if(i==0){
                left=-1;//indication for adjusting method
                start=0;
                index=m_nvRankingMinima.get(i);
                right=index;
                end=right;
            }else if(i==len){
                left=index0;
                if(left>=lastMaximum) continue;//the signal ends with the last minimum
                right=-1;
                start=left+1;
                end=lenX-1;
            } else {
                index=m_nvRankingMinima.get(i);
                left=index0;
                start=left+1;
                right=index;
                end=Math.min(right, lenX-1);
            }
            if(end<start) continue;
            if(i==0){
                base=right;
                subBase=0;
            } else if(i==len){
                base=left;
                subBase=len-1;
            } else {
                if(m_pdY[m_nvLn.get(left)]<m_pdY[m_nvLn.get(right)]){
                    base=right;
                    subBase=left;
                } else {
                    base=left;
                    subBase=right;
                }
            }
            
            peak=-1;
            subPeak=-1;
            dBase=m_pdY[m_nvLn.get(base)];
            dxx=Double.NEGATIVE_INFINITY;
            dxxs=Double.NEGATIVE_INFINITY;
            for(j=start;j<=end;j++){
                ix=m_nvLx.get(j);
                dx=m_pdY[ix];
                if(dx>dxx){
                    dxx=dx;
                    peak=j;
                }
                if(dx>dxxs&&dx<dBase){
                    dxxs=dx;
                    subPeak=j;
                }                
            }
            
            adjNode=new ExtremaAdjustNode();
            adjNode.start=start;
            adjNode.end=end;
            adjNode.left=left;
            adjNode.right=right;
            adjNode.sign=1;
            adjNode.peak=peak;
            adjNode.base=base;
            adjNode.subBase=subBase;
            adjNode.subPeak=subPeak;
            m_nvBasePointsN.add(m_nvLx.get(adjNode.base));
            m_cvAdjustNodes.add(adjNode);
            index0=index;
        }

        index0=0;
        len=m_nvRankingMaxima.size();
        for(i=0;i<len;i++){
            if(i==0){
                index=m_nvRankingMaxima.get(i);
                if(index==0) continue;
                left=-1;//indication for adjusting method
                start=0;
                right=index;
                end=right-1;
            }else if(i==len){
                left=index0;
                if(left>=lastMinimum) continue;//the signal ends with the last maximum
                right=-1;
                start=left;
                end=lenN-1;
            } else {
                index=m_nvRankingMaxima.get(i);
                left=index0;
                start=left;
                right=index;
                end=Math.min(lenN-1,right-1);
            }
            if(end<start) continue;
            
            if(i==0){
                base=right;
                subBase=0;
            } else if(i==len){
                base=left;
                subBase=len-1;
            } else {
                if(m_pdY[m_nvLx.get(left)]>m_pdY[m_nvLx.get(right)]){
                    base=right;
                    subBase=left;
                
                } else {
                    base=left;
                    subBase=right;
                }
            }

            peak=-1;
            subPeak=-1;
            dBase=m_pdY[m_nvLx.get(base)];
            dxn=Double.POSITIVE_INFINITY;
            dxns=Double.POSITIVE_INFINITY;
            for(j=start;j<=end;j++){
                ix=m_nvLn.get(j);
                dx=m_pdY[ix];
                if(dx<dxn){
                    dxn=dx;
                    peak=j;
                }
                if(dx>dBase&&dx<dxns){
                    dxns=dx;
                    subPeak=j;
                }
            }

            adjNode=new ExtremaAdjustNode();
            adjNode.start=start;
            adjNode.end=end;
            adjNode.left=left;
            adjNode.right=right;
            adjNode.sign=-1;
            adjNode.peak=peak;
            adjNode.base=base;
            adjNode.subBase=subBase;
            adjNode.subPeak=subPeak;
            m_nvBasePointsN.add(m_nvLx.get(adjNode.base));
            m_cvAdjustNodes.add(adjNode);
            index0=index;
        }
     }
/*     public void buildAdjustNodes1(int multiplicity){
        int i,len=m_pdY.length,ix,start,end,peak,lenX=m_nvLx.size(),lenN=m_nvLn.size();

        double dx,dxx,dxn,dxt,dl;
        DoubleRange cYRange=CommonStatisticsMethods.getRange(m_pdYFilted0);
        m_cvAdjustNodes=new ArrayList();
        ExtremaAdjustNode adjNode;

        int left=0, right,j,rt;
        int lastPosition=-1;
        boolean bEnd;

        for(i=0;i<m_nvLx.size();i++){//m_nvLn.get(i)<m_nvLx.get(i)<m_nvLn.get(i),lenX>=lenN
            if(m_nvLx.get(i)>lastPosition){
                bEnd=false;
                left=-1;
                right=-1;
                start=i;
                end=i;
                peak=i;

                if(i>0) {
                    left=i-1;
                    dl=m_pdYFilted0[m_nvLn.get(left)];
                }else{
                    dl=m_pdYFilted0[0];
                }

                ix=m_nvLx.get(i);
                dx=m_pdYFilted0[ix];
                if(ix==124){
                    ix=ix;
                }

                dxn=cYRange.getMax();
                dxx=dx;
//                lastExtremum=Math.min(lenN-1,lenX-1);
                for(j=i;j<Math.min(i+multiplicity,m_nvLx.size());j++){
                    if(j<m_nvLn.size()){
                        rt=m_nvLn.get(j);
                        dxt=m_pdYFilted0[rt];
                    }else{
                        rt=m_pdY.length-1;
                        dxt=m_pdYFilted[rt];
                    }
                    if(dxt<dxn){
                        if(j<m_nvLn.size())
                            right=j;
                        else
                            right=-1;
                        dxn=dxt;
                        end=j;
                    }
                    if(dxt<=dl) bEnd=true;
                    ix=m_nvLx.get(j);
                    dxt=m_pdYFilted0[ix];
                    if(dxt>dxx) {
                        peak=j;
                        dxx=dxt;
                    }
                    if(bEnd) break;
                }
                adjNode=new ExtremaAdjustNode();
                adjNode.start=start;
                adjNode.end=end;
                adjNode.left=left;
                adjNode.right=right;
                adjNode.sign=1;
                adjNode.peak=peak;

                if(left<0){
                    adjNode.base=right;
                }else if(right<0){
                    adjNode.base=left;
                } else if (m_pdYFilted0[m_nvLn.get(left)] >= m_pdYFilted0[m_nvLn.get(right)])
                    adjNode.base=left;
                else
                    adjNode.base=right;
                m_cvAdjustNodes.add(adjNode);
//                filterData_peaks(adjNode);
                lastPosition=m_nvLx.get(end);
            }
            if(i>=m_nvLn.size()) continue;

            if(m_nvLn.get(i)>lastPosition){
                bEnd=false;
                left=i;
                right=-1;
                peak=i;
                start=i;
                end=i;

                ix=m_nvLn.get(i);
                if(ix==77){
                    ix=ix;
                }
                dx=m_pdYFilted0[ix];

                dxn=dx;
                dxx=cYRange.getMin();
                dl=m_pdYFilted0[left];

                for(j=i+1;j<=Math.min(i+multiplicity,m_nvLx.size()-1);j++){
                    if(j<m_nvLx.size()){
                        rt=m_nvLx.get(j);
                        dxt=m_pdYFilted0[rt];
                    }else {
                        rt=m_pdY.length-1;
                        dxt=m_pdYFilted0[rt];
                    }
                    if(dxt>dxx){
                        right=j;
                        dxx=dxt;
                        end=j-1;
                    }
                    if(dxt>=dl) bEnd=true;
                    ix=m_nvLn.get(j-1);
                    dxt=m_pdYFilted0[ix];
                    if(dxt<dxn){
                        peak=j-1;
                        dxn=dxt;
                    }
                    if(bEnd) break;
                }
                adjNode=new ExtremaAdjustNode();
                adjNode.start=start;
                adjNode.end=end;
                adjNode.left=left;
                adjNode.right=right;
                adjNode.sign=-1;
                adjNode.peak=peak;
                if(right<0){
                    adjNode.base=left;
                }else if(m_pdYFilted0[m_nvLx.get(left)]<=m_pdYFilted0[m_nvLx.get(right)])
                    adjNode.base=left;
                else
                    adjNode.base=right;
                m_cvAdjustNodes.add(adjNode);
//                filterData_peaks(adjNode);
                lastPosition=m_nvLn.get(end);
            }
        }
    }*/
    public void getTransitionPreservingFiltedData(int multiplicity){//this methods and getTransitionPreservingFiltedData1 differ in the order of filtering local maxima and local minima.
         //this methods filter the extrema left to right order, and alternately. getTransitionPreservingFiltedData1 filter all local maxima and then filter local minima on the next iteration.
         //both methods may functionally replaced by the methods using adjustNode
        int sign=1,i,len=m_pdY.length,ix;

        double ds=msXN.getSD(),dx0,dx1,dx,dxx,dxn,delta,dxt;
        DoubleRange cYRange=CommonStatisticsMethods.getRange(m_pdY);


        int left=0, right,j,rt;

        for(i=0;i<m_nvLx.size();i++){//m_nvLn.get(i)<m_nvLx.get(i)<m_nvLn.get(i)
            ix=m_nvLx.get(i);
            dx=m_pdYFilted[ix];
            if(i>0){
                left=m_nvLn.get(i-1);
                dx0=m_pdYFilted[left];
            }else if(ix>0) {
                left=0;
                dx0=m_pdYFilted[left];
            }else{
                dx0=cYRange.getMin()-10*ds;
            }

            dx1=cYRange.getMax()+11*ds;
            right=i;
            for(j=i;j<i+multiplicity;j++){
                if(j<m_nvLn.size()){
                    rt=m_nvLn.get(j);
                    dxt=m_pdY[rt];
                }else if(ix<len-1) {
                    rt=len-1;
                    dxt=m_pdY[rt];
                }else{
                    rt=len-1;
                    dxt=cYRange.getMin()-10*ds;
                }
                if(dxt<dx1){
                    right=rt;
                    dx1=dxt;
                }
            }
            if(dx0<cYRange.getMin()) dx0=m_pdYFilted[m_nvLn.get(i)];
            if(dx1<cYRange.getMin()) dx1=dx0;
//            if(dx0>cYRange.getMin())
                m_pdYFilted[ix]=getDeltaPreservingWeightedAverage(dx0,dx,dx1,ds,1);

        }
        for(i=0;i<m_nvLn.size();i++){
            ix=m_nvLn.get(i);
            dx=m_pdYFilted[ix];

            left=m_nvLx.get(i);
            dx0=m_pdYFilted[left];

            right=i+1;
            dx1=cYRange.getMin()-11*ds;
            for(j=i+1;j<i+1+multiplicity;j++){
                if(i<m_nvLx.size()-1){
                    rt=m_nvLx.get(i+1);
                    dxt=m_pdYFilted[rt];
                }else if(ix<len-1){
                    rt=len-1;
                    dxt=m_pdYFilted[rt];
                }else{
                    rt=len-1;
                    dxt=cYRange.getMax()+10*ds;
                }
                if(dxt>dx1){
                    right=rt;
                    dx1=dxt;
                }
            }
            if(dx1>cYRange.getMax()) dx1=dx0;
            m_pdYFilted[ix]=getDeltaPreservingWeightedAverage(dx0,dx,dx1,ds,-1);
        }

        for(i=0;i<m_nvLx.size();i++){
            ix=m_nvLx.get(i);
            dx=m_pdYFilted[ix];
            if(i>0){
                left=m_nvLn.get(i-1);
                dx0=m_pdYFilted[left];
                for(j=left+1;j<ix;j++){
                    m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[left], dx0, m_pdY[ix], dx, m_pdY[j]);
                }
            }else if(ix>0){
                left=0;
                dx0=m_pdYFilted[left];
                for(j=left+1;j<ix;j++){
                    m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[left], dx0, m_pdY[ix], dx, m_pdY[j]);
                }
            }
            if(i>=m_nvLn.size()){
                if(ix<len-1){
                    right=len-1;
                    dx1=m_pdYFilted[right];
                    for(j=ix+1;j<right;j++){
                        m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[ix], dx, m_pdY[right], dx1, m_pdY[j]);
                    }
                }
                break;
            }
            right=m_nvLn.get(i);
            dx1=m_pdYFilted[right];
            for(j=ix+1;j<right;j++){
                m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[ix], dx, m_pdY[right], dx1, m_pdY[j]);
            }
        }
    }
    public void getTransitionPreservingFiltedData1(int multiplicity){
        int sign=1,i,len=m_pdY.length,ix;

        double ds=msXN.getSD(),dx0,dx1,dx,dxx,dxn,delta,dxt;
        DoubleRange cYRange=CommonStatisticsMethods.getRange(m_pdY);


        int left=0, right,j,rt;

        for(i=0;i<m_nvLx.size();i++){//m_nvLn.get(i)<m_nvLx.get(i)<m_nvLn.get(i)
            ix=m_nvLx.get(i);
            dx=m_pdY[ix];
            if(i>0){
                left=m_nvLn.get(i-1);
                dx0=m_pdY[left];
            }else if(ix>0) {
                left=0;
                dx0=m_pdY[left];
            }else{
                dx0=cYRange.getMin()-10*ds;
            }

            dx1=cYRange.getMax()+10*ds;
            right=i;
            for(j=i;j<i+multiplicity;j++){
                if(j<m_nvLn.size()){
                    rt=m_nvLn.get(j);
                    dxt=m_pdY[rt];
                }else if(ix<len-1) {
                    rt=len-1;
                    dxt=m_pdY[rt];
                }else{
                    rt=len-1;
                    dxt=cYRange.getMin()-10*ds;
                }
                if(dxt<dx1){
                    right=rt;
                    dx1=dxt;
                }
            }
            if(dx0<cYRange.getMin()) dx0=m_pdY[m_nvLn.get(i)];
            if(dx1<cYRange.getMin()) dx1=dx0;
//            if(dx0>cYRange.getMin())
            m_pdYFilted[ix]=getDeltaPreservingWeightedAverage(dx0,dx,dx1,ds,1);

            if(i>=m_nvLn.size()) continue;

            ix=m_nvLn.get(i);
            dx=m_pdY[ix];

            left=m_nvLx.get(i);
            dx0=m_pdY[left];

            right=i+1;
            dx1=dxt=cYRange.getMin()-10*ds;
            for(j=i+1;j<i+1+multiplicity;j++){
                if(i<m_nvLx.size()-1){
                    rt=m_nvLx.get(i+1);
                    dxt=m_pdY[rt];
                }else if(ix<len-1){
                    rt=len-1;
                    dxt=m_pdY[rt];
                }else{
                    rt=len-1;
                    dxt=cYRange.getMax()+10*ds;
                }
                if(dxt>dx1){
                    right=rt;
                    dx1=dxt;
                }
            }
            if(dx1>cYRange.getMax()) dx1=dx0;
            m_pdYFilted[ix]=getDeltaPreservingWeightedAverage(dx0,dx,dx1,ds,-1);
        }

        for(i=0;i<m_nvLx.size();i++){
            ix=m_nvLx.get(i);
            dx=m_pdYFilted[ix];
            if(i>0){
                left=m_nvLn.get(i-1);
                dx0=m_pdYFilted[left];
                for(j=left+1;j<ix;j++){
                    m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[left], dx0, m_pdY[ix], dx, m_pdY[j]);
                }
            }else if(ix>0){
                left=0;
                dx0=m_pdYFilted[left];
                for(j=left+1;j<ix;j++){
                    m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[left], dx0, m_pdY[ix], dx, m_pdY[j]);
                }
            }
            if(i>=m_nvLn.size()){
                if(ix<len-1){
                    right=len-1;
                    dx1=m_pdYFilted[right];
                    for(j=ix+1;j<right;j++){
                        m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[ix], dx, m_pdY[right], dx1, m_pdY[j]);
                    }
                }
                break;
            }
            right=m_nvLn.get(i);
            dx1=m_pdYFilted[right];
            for(j=ix+1;j<right;j++){
                m_pdYFilted[j]=CommonMethods.getLinearIntoplation(m_pdY[ix], dx, m_pdY[right], dx1, m_pdY[j]);
            }
        }
     }
}
