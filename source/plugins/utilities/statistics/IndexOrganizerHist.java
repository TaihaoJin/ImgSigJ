/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.ArrayofArrays.IntArray;
import java.util.ArrayList;
import utilities.CustomDataTypes.IntPair;
import utilities.CommonStatisticsMethods;
import ij.IJ;
/**
 *
 * @author Taihao
 */
public class IndexOrganizerHist {
	int m_nType; //0 for linear and 1 for logorithmic
	int m_nDim;
	boolean m_bUpdated;
	double m_dBase,m_dDelta;
	double m_dBase1;
	double m_log10;
     boolean m_bHandlingIndexes;
	IntArray[] m_pvIndexes;
	ArrayList<Integer> m_vnIndexesLo;//collected indexes whose corresponding values are smaller than m_dBase
	ArrayList<Integer> m_vnIndexesHi;//collected indexes whose corresponding values are larger than m_dBase+m_dDelta*m_nDim
	ArrayList<Double> [] m_pdvData;
	ArrayList<Double> m_dvDataLo;//collected indexes whose values are smaller than m_dBase
	ArrayList<Double> m_dvDataHi;//collected indexes whose values are larger than m_dBase+m_dDelta*m_nDim
    int m_nPercentilePosition;
    double m_dPercentile;
    boolean m_bCheckPercentile;//ascending order, zero percentile is the smallest number.
    int m_nSize;

    public IndexOrganizerHist()
    {
        m_bUpdated=false;
        m_log10=Math.log(10.);
        m_bHandlingIndexes=true;
        m_nPercentilePosition=0;
        m_dPercentile=0;
        m_bCheckPercentile=false;
        m_nSize=0;
    }


    public void update(int nType, int nDim, double dBase, double dDelta, boolean bHandlingIndexes, boolean bCheckPercentile)
    {//m_nType==1 indicates logrithmic bins, dBase and dDelta are the log of the values.
        m_nType=nType;
        m_bUpdated=true;
        m_nDim=nDim;
        if(nDim<2) m_nDim=2;
        m_dBase=dBase;
        m_dDelta=dDelta;
        m_dBase1=Math.exp(dBase*m_log10);
        m_pvIndexes=new IntArray[m_nDim];
        m_pdvData=new ArrayList[m_nDim];
        for(int i=0;i<m_nDim;i++){
            m_pvIndexes[i]=new IntArray();
            m_pdvData[i]=new ArrayList();
        }
        m_vnIndexesLo=new ArrayList();
        m_vnIndexesHi=new ArrayList();
        m_dvDataLo=new ArrayList();
        m_dvDataHi=new ArrayList();
        m_bCheckPercentile=bCheckPercentile;
    }

    public void addIndex(double value, int index)
    {
        if(!m_bHandlingIndexes) IJ.error("This incidence of IndexOrganizerHist is using only for non Index-Handling");
        addIndex(value,m_nType,index);
    }

    public void addIndex(double value0, int nType, int index)
    {
        double value=value0;
        if(!m_bHandlingIndexes) IJ.error("This incidence of IndexOrganizerHist is using only for non Index-Handling");
        if(nType==1){
            if(value<=0)
                value=m_dBase-1;
            else
                value=Math.log(value)/m_log10;
        }
        int position=getPosition(value);

        if(position>=0&&position<m_nDim){
            m_pvIndexes[position].m_intArray.add(index);
            m_pdvData[position].add(value0);
        }else if(position==-1){
            m_vnIndexesLo.add(index);
            m_dvDataLo.add(value0);
        }else if(position==-2){
            m_dvDataHi.add(value0);
            m_vnIndexesHi.add(index);
        }
        m_nSize++;
    }

    int getPosition(double value)
    {
        return getPosition(value,0);
    }

    int getPosition(double value, int nType)
    {
        int position;
        if(nType==1){
            if(value<=0)
                value=m_dBase-1;
            else
                value=Math.log(value)/m_log10;
        }
        if(value<m_dBase) return -1;
        position=(int)((value-m_dBase)/m_dDelta);
        if(position>=m_nDim) position=-2;
        return position;
    }

    ArrayList<Integer> getIndexes(int position){
        if(!m_bHandlingIndexes) IJ.error("This incidence of IndexOrganizerHist is using only for non Index-Handling");
        ArrayList<Integer> vnEmpty=new ArrayList();
        vnEmpty.clear();
        if(position<0){
            if(position==-1) return m_vnIndexesLo;
            if(position==-2) return m_vnIndexesHi;
            return vnEmpty;
        }
        if(position>=m_nDim) return vnEmpty;
        return m_pvIndexes[position].m_intArray;
    }

    ArrayList<Double> getDataArray(int position){
        if(!m_bHandlingIndexes) IJ.error("This incidence of IndexOrganizerHist is using only for non Index-Handling");
        ArrayList<Double> dvEmpty=new ArrayList();
        if(position<0){
            if(position==-1) return m_dvDataLo;
            if(position==-2) return m_dvDataHi;
            return dvEmpty;
        }
        if(position>=m_nDim) return dvEmpty;
        return m_pdvData[position];
    }

    int getDim()
    {
        return m_nDim;
    }

    double getDelta()
    {
        return m_dDelta;
    }

    double getValue(int index)
    {
        return getValue(index, m_nType);
    }
    double getValue(int index, int nType)
    {
        double dv=m_dBase+index*m_dDelta;
        if(nType==1) dv=Math.exp(dv*m_log10);
        return dv;
    }
    void smoothHistogram(int ws)
    {
        if(!m_bHandlingIndexes) IJ.error("This incidence of IndexOrganizerHist is using only for non Index-Handling");
        int indexi=0,indexf=m_nDim-1;
        smoothHistogram(indexi,indexf,ws);
    }
    void smoothHistogram(int indexi, int indexf, int ws)
    {
        if(!m_bHandlingIndexes) IJ.error("This incidence of IndexOrganizerHist is using only for non Index-Handling");
        boolean bLo=false, bHi=false;
        if(indexi<0){
            bLo=true;
            indexi=0;
        }
        if(indexf<0){
            bHi=true;
            indexf=m_nDim-1;
        }
        int i,j,k,ni,nf,nSize;
        IntArray[] pvIndexes=new IntArray[m_nDim];
        ArrayList<Integer> vnLo=new ArrayList(),vnHi=new ArrayList();
        ArrayList<Integer> aVint=new ArrayList();
        
        ArrayList<Double>[] pdvData=new ArrayList[m_nDim];
        ArrayList<Double> dvLo=new ArrayList(),dvHi=new ArrayList();
        ArrayList<Double> dvt=new ArrayList();
        for(i=0;i<m_nDim;i++){
            if(i>=indexi&&i<=indexf){
                ni=i-ws;
                if(ni<0) ni=0;
                nf=i+ws;
                if(nf>m_nDim-1) nf=m_nDim-1;
                for(j=ni;j<=nf;j++){
                    aVint=m_pvIndexes[j].m_intArray;
                    dvt=m_pdvData[j];
                    nSize=aVint.size();
                    for(k=0;k<nSize;k++){
                        pvIndexes[i].m_intArray.add(aVint.get(k));
                        pdvData[i].add(dvt.get(k));
                    }
                }
            }else{
                aVint=m_pvIndexes[i].m_intArray;
                dvt=m_pdvData[i];
                nSize=aVint.size();
                for(k=0;k<nSize;k++){
                    pvIndexes[i].m_intArray.add(aVint.get(k));
                    pdvData[i].add(dvt.get(k));
                }
            }
        }
        if(bLo){
            for(i=0;i<ws;i++){
                aVint=m_pvIndexes[i].m_intArray;
                dvt=m_pdvData[i];
                nSize=aVint.size();
                for(k=0;k<nSize;k++){
                    vnLo.add(aVint.get(k));
                    dvLo.add(dvt.get(k));
                }
                aVint=m_vnIndexesLo;
                dvt=m_dvDataLo;
                nSize=aVint.size();
                for(k=0;k<nSize;k++){
                    pvIndexes[i].m_intArray.add(aVint.get(k));
                    pdvData[i].add(dvt.get(k));
                }
            }
            nSize=vnLo.size();
            for(k=0;k<nSize;k++){
                m_vnIndexesLo.add(vnLo.get(k));
                m_dvDataLo.add(dvLo.get(k));
            }
        }
        if(bHi){
            for(i=m_nDim-1-ws;i<m_nDim;i++){
                aVint=m_pvIndexes[i].m_intArray;
                dvt=m_pdvData[i];
                nSize=aVint.size();
                for(k=0;k<nSize;k++){
                    vnHi.add(aVint.get(k));
                    dvHi.add(dvt.get(k));
                }
                aVint=m_vnIndexesHi;
                dvt=m_dvDataHi;
                nSize=aVint.size();
                for(k=0;k<nSize;k++){
                    pvIndexes[i].m_intArray.add(aVint.get(k));
                    pdvData[i].add(dvt.get(k));
                }
            }
            nSize=vnHi.size();
            for(k=0;k<nSize;k++){
                m_vnIndexesHi.add(vnHi.get(k));
                m_dvDataHi.add(dvHi.get(k));
            }
        }
        m_pvIndexes=pvIndexes;
        m_pdvData=pdvData;
    }
    public IntPair getDataPosition(int ranking0){//ascending, ranking starts from 0
        if(ranking0>=m_nSize) {
            IJ.error("ranking is larger than the data size: in getDataPosition");
            return null;
        }
        IntPair ip=null;
        int r,c,num=m_dvDataLo.size(),nt;
        ArrayList<Double> dvt;
        dvt=m_dvDataLo;
        nt=dvt.size();
        num=nt;
        r=-1;
        while(num<=ranking0){
            r++;
            if(r<m_nDim)
                dvt=m_pdvData[r];
            else
                dvt=m_dvDataHi;
            nt=dvt.size();
            num+=nt;
        }
        if(r>=m_nDim) r=-2;
        int ranking=ranking0-(num-nt);
        c=CommonStatisticsMethods.getRankingIndex(dvt, ranking, 1);
        ip=new IntPair(r,c);
        return ip;
    }
    public int getRankingIndex(int nRank){
        IntPair ip=getDataPosition(nRank);
        ArrayList<Integer> nvt;
        int r=ip.left,c=ip.right;
        if(r==-1) 
            nvt=m_vnIndexesLo;
        else if(r==-2)
            nvt=m_vnIndexesHi;
        else
            nvt=m_pvIndexes[r].m_intArray;
        return nvt.get(c);
    }
    public double getRankingData(int nRank){
        IntPair ip=getDataPosition(nRank);
        if(ip==null) return Double.NaN;
        ArrayList<Double> dvt;
        int r=ip.left,c=ip.right;
        if(r==-1) 
            dvt=m_dvDataLo;
        else if(r==-2)
            dvt=m_dvDataHi;
        else
            dvt=m_pdvData[r];
        return dvt.get(c);
    }
}
