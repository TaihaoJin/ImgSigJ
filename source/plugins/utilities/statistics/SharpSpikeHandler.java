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
public class SharpSpikeHandler {
    class SharpnessNode{
        public int sign,left,right,start,end,delta,multiplicity;
        public double area,height,width,sharpness,dl,dr,xl,xr,xc,base;
    }
    ArrayList<Integer> m_nvLx,m_nvLn,m_nvLXX,m_nvLNN,m_nvExtrema;
    ArrayList<intRange> m_cvSpikeRanges,m_cvSpikeRangesDownward,m_cvSpikeRangesUpward;
    ArrayList<double[]> m_pdvShapness;
    MeanSem1 msXX,msXN,msNN;
    int m_nMaxMultiplicity;
    double[] m_pdX,m_pdY,m_pdYFilted0,m_pdYFilted;
    double m_dPValue;
    ArrayList<SharpnessNode>[] m_pcvSharpnessNodes;
    ArrayList<MeanSem1> m_cvSharpnessMS;
    int[] m_pnCorrected;
    boolean[] pbValid;
//    boolean m_pbInnerAdjustNodes[];
    public SharpSpikeHandler(double[] pdX, double[] pdY,int nMaxMultiplicity, double pValue){
        m_dPValue=pValue;
        m_pdY=pdY;
        m_pdX=pdX;
        m_pnCorrected=new int[m_pdX.length];
        CommonStatisticsMethods.setElements(m_pnCorrected, 0);
        m_nMaxMultiplicity=Math.abs(nMaxMultiplicity);
        if(m_nvLn==null) m_nvLn=new ArrayList();
        m_nvLn.clear();
        if(m_nvLx==null) m_nvLx=new ArrayList();
        m_nvLx.clear();
        CommonMethods.LocalExtrema(pdY, m_nvLn, m_nvLx);
        pbValid=CommonStatisticsMethods.getEmptyBooleanArray(pbValid, m_pdX.length);
        CommonStatisticsMethods.setElements(pbValid, true);
        calDeviations();
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
        if(pdXX.length>nMinLen) CommonStatisticsMethods.findOutliars(pdXX, 0.01, msXX, nvOutliars);

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
        if(pdNN.length>nMinLen) CommonStatisticsMethods.findOutliars(pdNN, 0.01, msNN, nvOutliars);
    }

    public ArrayList<intRange> getSpikeRanges(){
        return m_cvSpikeRanges;
    }
    public ArrayList<intRange> getSpikeRanges(int sign){
        if(sign>0) return m_cvSpikeRangesUpward;
        if(sign<0) return m_cvSpikeRangesDownward;
        return m_cvSpikeRanges;
    }

    public double[] removeSpikes_Sharpness(int nMaxMultiplicity){
        m_pdYFilted=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted, m_pdY.length);
        m_pdYFilted0=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYFilted, m_pdY.length);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted);
        CommonStatisticsMethods.copyArray(m_pdY, m_pdYFilted0);
        m_pcvSharpnessNodes=new ArrayList[nMaxMultiplicity];
        m_cvSharpnessMS=new ArrayList();
        int multiplicity,len,i,index,sign=1,r,left,right;
        MeanSem1 ms;
        double[] pdSP;
        double sd,cutoff;
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn,vn;
        if(nv0.get(0)>nv1.get(0)){
            sign=-1;
            nv0=m_nvLn;
            nv1=m_nvLx;
        }

        ArrayList<SharpnessNode> cvSP;
        ArrayList<Integer> outliars=new ArrayList();
        for(multiplicity=1;multiplicity<=nMaxMultiplicity;multiplicity++){
            cvSP=new ArrayList();
            m_pcvSharpnessNodes[multiplicity-1]=cvSP;
            SharpnessNode cSP;
            pdSP=calSharpness(multiplicity);
            ms=new MeanSem1();
//            CommonStatisticsMethods.findOutliars(pdSP, m_dPValue, ms, outliars, 1, pdSP.length-1, 1);
            ms=CommonStatisticsMethods.buildMeanSem1(pdSP, 1, pdSP.length-2, 1);
            m_cvSharpnessMS.add(ms);
            cutoff=GaussianDistribution.getZatP(1-m_dPValue, ms.mean, ms.getSD(), 0.001*ms.getSD());
            len=pdSP.length;
            for(i=1;i<len;i++){
                if(pdSP[i]>cutoff){
                    index=i/2;
                    r=i-2*index;
                    if(r==0){
                        left=nv1.get(index-1);
                        right=nv1.get(index);
                        cSP=calSharpness(m_pdX,m_pdY,sign,left,right);
                        cSP.multiplicity=multiplicity;
                        cvSP.add(cSP);
                    }else{
                        left=nv0.get(index);
                        if(index<nv0.size()-1)
                            right=nv0.get(index+1);
                        else
                            right=m_pdY.length-1;
                        cSP=calSharpness(m_pdX,m_pdY,-sign,left,right);
                        cSP.multiplicity=multiplicity;
                        cvSP.add(cSP);
                    }
                }
            }
        }
        removeSpikes_Sharpness();
        return m_pdYFilted;
    }
    public int removeSpikes_Sharpness(){
        int nMaxM=m_pcvSharpnessNodes.length,num=0,mul,i,len,status;
        CommonStatisticsMethods.setElements(m_pnCorrected, 0);
        ArrayList<SharpnessNode> cvSPs;
        SharpnessNode cSP;
        double cutoff;
        MeanSem1 ms;
        for(mul=1;mul<=nMaxM;mul++){
            cvSPs=m_pcvSharpnessNodes[mul-1];
            ms=m_cvSharpnessMS.get(mul-1);
            cutoff=GaussianDistribution.getZatP(1-m_dPValue,ms.mean, ms.getSD(), 0.001*ms.getSD());
            len=cvSPs.size();
            for(i=0;i<len;i++){
                cSP=cvSPs.get(i);
                if(isCorrected(cSP)) {
                    adjustSharpness(m_pdX,m_pdYFilted,cSP);
//                    if(cSP.sharpness<cutoff) continue;
                    if(cSP.height<cutoff) continue;
                }
                removeSpike(cSP);
            }
        }
        return num;
    }
    boolean isCorrected(SharpnessNode cSP){
        double y,base=m_pdYFilted0[cSP.start];
        int sign=cSP.sign;
        for(int i=cSP.left;i<=cSP.right;i++){
            y=m_pdY[i];
            if(sign*(y-base)>0&&m_pnCorrected[i]!=0) return true;
        }
        return false;
    }
    int removeSpike(SharpnessNode cSP){
        int i,last=cSP.end,sign=cSP.sign,mul=cSP.multiplicity;
        double x,y,base=m_pdY[cSP.start],dMSP=m_cvSharpnessMS.get(mul-1).mean;

//        double peak0=base+sign*cSP.height,peak=base+sign*cSP.height*dMSP/cSP.sharpness;
        double peak0=base+sign*cSP.height,peak=base+dMSP;
        for(i=cSP.left;i<=cSP.right;i++){
            y=m_pdYFilted0[i];
            if(sign*(y-base)<0) continue;
            m_pdYFilted[i]=CommonMethods.getLinearIntoplation(base, base, peak0, peak, y);
            m_pnCorrected[i]=mul;
        }
        return 1;
    }
    public double[] calSharpness(int multiplicity){
        ArrayList<Integer> nv0=m_nvLx,nv1=m_nvLn;
        int sign=1;

        if(m_nvLx.get(0)>m_nvLn.get(0)){
            sign=-1;
            nv0=m_nvLn;
            nv1=m_nvLx;
        }

        int len0=nv0.size(),len1=nv1.size(),len=Math.min(len0, len1)-multiplicity+1, i,left,right;

        int position=0;
        left=0;
        right=0;

        double[] pdSharpness=new double[2*len];
        CommonStatisticsMethods.setElements(pdSharpness, -1);
        SharpnessNode cSP;
        position=0;
        for(i=0;i<len;i++){
            if(i<len0){
                if(i==0){
                    left=0;
                }else{
                    left=nv1.get(i-1);
                }
                if(i<len1-multiplicity+1)
                    right=nv1.get(i+multiplicity-1);
                else
                    right=m_pdY.length-1;

                cSP=calSharpness(m_pdX,m_pdY,sign,left,right);
                cSP.multiplicity=multiplicity;
                pdSharpness[position]=cSP.height;
//                pdSharpness[position]=cSP.sharpness;
                position++;
            }
            if(i<len1){
                if(i<len0)
                    left=nv0.get(i);
                else
                    left=nv0.get(len0-1);
                if(i<len0-multiplicity)
                    right=nv0.get(i+multiplicity);
                else
                    right=m_pdY.length-1;

                if(position==17)
                    position=position;
                cSP=calSharpness(m_pdX,m_pdY,-sign,left,right);
                cSP.multiplicity=multiplicity;
                pdSharpness[position]=cSP.height;
//                pdSharpness[position]=cSP.sharpness;
                position++;
            }
        }
        return pdSharpness;
    }
    public SharpnessNode calSharpness(double[] pdX, double[] pdY, int sign, int left, int right){
        double area=0,height,dl=pdY[left],dr=pdY[right],dx=dl,dt,dn=dr,dt0,x0,x,xc,xStart=pdX[left],sharpness,dh;
        int position=left, delta=1,start=left,end=right,peak,direction=1;
        if(sign*(dl-dr)<0){
            start=right;
            end=left;
            delta=-1;
            dx=dr;
            dn=dl;
            xStart=pdX[right];
            direction=-1;
        }

        position=start+delta;
        dt=0;
        dt0=0;//using dx as the reference
        x0=pdX[start];
        x=x0;
        xc=pdX[end];
        peak=start;
        height=0;
        intRange range=new intRange(left,right);
        while(range.contains(position)){
            x=pdX[position];
            dt=pdY[position]-dx;
            dh=sign*dt;
            if(sign*dt>=0){
                xc=x;
                if(sign*dt0<0){
                    x0=CommonMethods.getLinearIntoplation(dt0, x0, dt, x, 0);
                    dt=0;
                }
                area=direction*sign*(xc-x0)*(dt0+dt)*0.5;
                if(dh>height){
                    height=dh;
                    peak=position;
                }
            }else{
                if(sign*dt0>=0){
                    xc=CommonMethods.getLinearIntoplation(dt0, x0, dt, x, 0);
                    area=direction*sign*(xc-x0)*(dt0)*0.5;
                }
            }
            dt0=dt;
            x0=x;
            position+=delta;
        }
//        sharpness=area/Math.abs(xc-xStart);
        sharpness=area/Math.abs(xc-xStart);
        if(Double.isNaN(sharpness)) sharpness=0;
        SharpnessNode cSP=new SharpnessNode();
        cSP.area=area;
        cSP.sharpness=sharpness;
        cSP.dl=dl;
        cSP.dr=dr;
        cSP.end=end;
        cSP.height=height;
        cSP.sign=sign;
        cSP.start=start;
        cSP.width=xc-start;
        cSP.xc=xc;
        cSP.base=pdY[start];
        cSP.delta=delta;
        cSP.left=left;
        cSP.right=right;
        return cSP;
    }
    public SharpnessNode adjustSharpness(double[] pdX, double[] pdY, SharpnessNode cSP){
        int left=cSP.left,right=cSP.right,sign=cSP.sign;
        double area=0,height,dl=pdY[left],dr=pdY[right],dx=dl,dt,dn=dr,dt0,x0,x,xc,xStart=pdX[left],sharpness,dh;
        int position=left, delta=1,start=left,end=right,peak,direction=1;
        if(sign*(dl-dr)<0){
            start=right;
            end=left;
            delta=-1;
            dx=dr;
            dn=dl;
            xStart=pdX[right];
            sign*=-1;
            direction=-1;
        }

        position=start+1;
        dt=0;
        dt0=0;
        x0=pdX[start];
        x=x0;
        xc=pdX[end];
        peak=start;
        height=0;
        intRange range=new intRange(left,right);
        while(range.contains(position)){
            x=pdX[position];
            dt=pdY[position]-dx;
            dh=sign*dt;
            if(sign*dt>=0){
                xc=x;
                if(sign*dt0<=0){
                    xc=CommonMethods.getLinearIntoplation(dt0, x0, dt, x, dx);
                }
                area=direction*sign*(x-xc)*(dt+dt0)*0.5;
                if(dh>height){
                    height=dh;
                    peak=position;
                }
            }else{
                if(sign*dt0>=0){
                    xc=CommonMethods.getLinearIntoplation(dt0, x0, dt, x, dx);
                    area=direction*sign*(xc-x0)*(dt+dt0)*0.5;
                }
            }
            dt0=dt;
            x0=x;
            position+=delta;
        }
        sharpness=area/(xc-xStart);
        cSP.area=area;
        cSP.sharpness=sharpness;
        cSP.dl=dl;
        cSP.dr=dr;
        cSP.end=end;
        cSP.height=height;
        cSP.sign=sign;
        cSP.start=start;
        cSP.width=xc-start;
        cSP.xc=xc;
        cSP.delta=delta;
        return cSP;
    }
    public void setSelection(ArrayList<Integer> nvSelection, boolean bAppend){
        int i,len=nvSelection.size();
        if(!bAppend) CommonStatisticsMethods.setElements(pbValid, false);
        for(i=0;i<len;i++){
            pbValid[nvSelection.get(i)]=true;
        }
    }
}
