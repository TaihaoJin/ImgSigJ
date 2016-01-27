/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import utilities.CommonStatisticsMethods;
import utilities.QuickSort;
import java.util.ArrayList;
import utilities.CommonMethods;
/**
 *
 * @author Taihao
 */
public class OneDNearestNeighbor {
    double[] m_pdData,m_pdDensity;
    ArrayList<Integer> m_cvPeaks,m_cvValleys;
    ArrayList<Double> m_dvValleySizes,m_dvPeakSizes;
    int m_nNeighbors,m_nBins;
    double m_dMax,m_dMin;
    int[] m_pnT;
    public OneDNearestNeighbor(double[] pdData,int nNumNeighbors,int nNumBins){
        updateData(pdData,nNumNeighbors,nNumBins);
    }
    public void updateData(double[] pdData, int numNeighbors, int nBins){
        m_pdData=CommonStatisticsMethods.getEmptyDoubleArray(m_pdData, pdData.length);
        m_pdDensity=CommonStatisticsMethods.getEmptyDoubleArray(m_pdDensity, pdData.length);
        CommonStatisticsMethods.copyArray(pdData,m_pdData);
        m_nNeighbors=numNeighbors;
        
        if(m_cvValleys==null)
            m_cvValleys=new ArrayList();
        else
            m_cvValleys.clear();
        
        if(m_cvPeaks==null)
            m_cvPeaks=new ArrayList();
        else
            m_cvPeaks.clear();
        
        if(m_dvValleySizes==null)
            m_dvValleySizes=new ArrayList();
        else
            m_dvValleySizes.clear();
        
        if(m_dvPeakSizes==null)
            m_dvPeakSizes=new ArrayList();
        else
            m_dvPeakSizes.clear();
        m_nBins=nBins;
        
        QuickSort.quicksort(m_pdData);
        buildNeighborDensity();
        calPeakSizes();
    }
    void buildNeighborDensity(){
        int i,j,iL,iH,len=m_pdData.length,len1=m_nNeighbors-1,it,num;
        m_pdDensity=CommonStatisticsMethods.getEmptyDoubleArray(m_pdDensity, m_nBins);
        m_dMax=m_pdData[len-1];
        m_dMin=m_pdData[0];
        double dt,dPosition,dStepSize=(m_dMax-m_dMin)/m_nBins,delta,dn,dx,dl,dh,dR;
        it=0;
        for(i=0;i<m_nBins;i++){
            dPosition=m_dMin+i*dStepSize;
            dt=m_pdData[it+1];
            
            while(dt<dPosition&&it<len-1){
                it++;
                dt=m_pdData[it+1];
            }
            
            iL=it;
            iH=it+1;
            
            num=0;
            dn=Double.POSITIVE_INFINITY;
            dx=Double.NEGATIVE_INFINITY;
            while(num<m_nNeighbors){
                if(iL>=0) 
                    dl=m_pdData[iL];
                else
                    dl=Double.POSITIVE_INFINITY;
                
                if(iH<len) 
                    dh=m_pdData[iH];
                else
                    dh=Double.POSITIVE_INFINITY;
                
                if(dl==Double.POSITIVE_INFINITY&&dh==Double.POSITIVE_INFINITY) break;
                if(dl!=Double.POSITIVE_INFINITY&&dPosition-dl<dh-dPosition){
                    num++;
                    iL--;
                    if(dl<dn) dn=dl;
                    if(dl>dx) dx=dl;
                }else{
                    num++;
                    iH++;
                    if(dh>dx) dx=dh;
                    if(dh<dn) dn=dh;
                }                    
            }
            dR=Math.max(Math.abs(dPosition-dn),Math.abs(dx-dPosition));
            if(dR<0.0001*(m_dMax-m_dMin)/m_nBins) dR=0.0001*(m_dMax-m_dMin)/m_nBins;//can happen when m_nNeighbors==1
            m_pdDensity[i]=0.5/dR;
        }
        CommonMethods.LocalExtrema(m_pdDensity, m_cvValleys, m_cvPeaks);
        len=m_cvPeaks.size();
        len1=m_cvValleys.size();
        if(len1>0)
            if(m_cvValleys.get(len1-1)>m_cvPeaks.get(len-1)) m_cvPeaks.add(m_pdData.length-1);
        if(!m_cvValleys.isEmpty()){
            if(m_cvValleys.get(0)<m_cvPeaks.get(0)) m_cvPeaks.add(0,0);
            calValleySizes();
        }
    }
    void calValleySizes(){
        double dt,dm,size,dl,dr;
        int i,it,l,r;
        for(i=0;i<m_cvValleys.size();i++){
            l=m_cvPeaks.get(i);
            r=m_cvPeaks.get(i+1);
            dl=m_pdDensity[l];
            dr=m_pdDensity[r];
            size=0;
            for(it=l;it<=r;it++){
                size+=CommonMethods.getLinearIntoplation(l, dl, r, dr, it)-m_pdDensity[it];
            }
            m_dvValleySizes.add(size);
        }
    }
    int[] getMainValleyPositions(int num0){
        int num=Math.min(m_cvValleys.size(), num0);
        int len=m_cvValleys.size(),i,j,ix,it;
        m_pnT=CommonStatisticsMethods.getEmptyIntArray(m_pnT, len);
        for(i=0;i<len;i++){
            m_pnT[i]=m_cvValleys.get(i);
        }
        int[] pnValleys=new int[num];
        double dt,dx;
        double[] pdT=CommonStatisticsMethods.copyToDoubleArray(m_dvValleySizes);
        for(i=0;i<num;i++){
            dx=pdT[i];
            ix=i;
            for(j=i+1;j<len;j++){
                dt=pdT[j];
                if(dt>dx){
                    dx=dt;
                    ix=j;
                }
            }
            if(ix>i){
                dt=pdT[i];
                pdT[i]=pdT[ix];
                pdT[ix]=dt;
                
                it=m_pnT[i];
                m_pnT[i]=m_pnT[ix];
                m_pnT[ix]=it;                
            }
            pnValleys[i]=m_pnT[i];
        }
        return pnValleys;
    }
    
    int[] getMainPeakPositions(int num0){
        int num=Math.min(m_cvPeaks.size(), num0);
        int len=m_cvPeaks.size(),i,j,ix,it;
        m_pnT=CommonStatisticsMethods.getEmptyIntArray(m_pnT, len);
        for(i=0;i<len;i++){
            m_pnT[i]=m_cvPeaks.get(i);
        }
        int[] pnPeaks=new int[num];
        double dt,dx;
        double[] pdT=CommonStatisticsMethods.copyToDoubleArray(m_dvPeakSizes);
        for(i=0;i<num;i++){
            dx=pdT[i];
            ix=i;
            for(j=i+1;j<len;j++){
                dt=pdT[j];
                if(dt>dx){
                    dx=dt;
                    ix=j;
                }
            }
            if(ix>i){
                dt=pdT[i];
                pdT[i]=pdT[ix];
                pdT[ix]=dt;
                
                it=m_pnT[i];
                m_pnT[i]=m_pnT[ix];
                m_pnT[ix]=it;                
            }
            pnPeaks[i]=m_pnT[i];
        }
        return pnPeaks;
    }
    
    public double[] getSortedData(){
        return m_pdData;
    } 
    
    public double[] getMainValleys(int num){
        int[] pnIndexes=getMainValleyPositions(num);
        num=pnIndexes.length;
        double[] pdValleys=new double[num];
        double delta=(m_dMax-m_dMin)/(m_nBins);
        for(int i=0;i<num;i++){
            pdValleys[i]=pnIndexes[i]*delta;
        }
        QuickSort.quicksort(pdValleys);
        return pdValleys;
    }
    public double[] getMainPeaks(int num){
        int[] pnIndexes=getMainPeakPositions(num);
        num=pnIndexes.length;
        double[] pdPeaks=new double[num];
        double delta=(m_dMax-m_dMin)/(m_nBins);
        for(int i=0;i<num;i++){
            pdPeaks[i]=pnIndexes[i]*delta+m_dMin;
        }
        QuickSort.quicksort(pdPeaks);
        return pdPeaks;
    }
    void calPeakSizes(){
        int num=m_cvPeaks.size();
        for(int i=0;i<num;i++){
            m_dvPeakSizes.add(calPeakSize(i));
        }
    }
    double calPeakSize(int index){
        int i,len=m_cvPeaks.size(),lp,rp,lv,rv,v;
        
        if(len==0) return m_pdDensity[m_cvPeaks.get(0)];
        
        double size=0,dp,dpl,dpr,dvl,dvr,dt;
        dp=m_pdDensity[m_cvPeaks.get(index)];
        lp=index;
        if(index==0)
            dvl=m_pdDensity[0];
        else
            dvl=Double.POSITIVE_INFINITY;
        
        lv=-1;
        while(lp>=0){
            lp--;
            if(lp<0) break;
            dpl=m_pdDensity[m_cvPeaks.get(lp)];
            dt=m_pdDensity[m_cvValleys.get(lp)];
            if(dt<dvl){
                lv=lp;
                dvl=dt;
            }
            if(dpl>=dp) break;
        }
        rp=index;
        rv=-1;
        if(index==len-1)
            dvr=m_pdDensity[len-1];
        else
            dvr=Double.POSITIVE_INFINITY;
        
        while(rp<len){
            rp++;
            if(rp>=len) break;
            dpr=m_pdDensity[m_cvPeaks.get(rp)];
            dt=m_pdDensity[m_cvValleys.get(rp-1)];
            if(dt<dvr){
                rv=rp-1;
                dvr=dt;
            }
            if(dpr>=dp) break;
        }
        if(lp<0) dvl=dvr;
        if(rp>=len) dvr=dvl;
        return dp-Math.max(dvl, dvr);
    }
}
