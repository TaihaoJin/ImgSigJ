/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.CommonMethods;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class PartialArraySorter {
    int m_nDim, m_nDataSize;
    double[] m_pdRankedData;
    int[] m_pnIndexes;
    boolean m_bAscending;
    public PartialArraySorter(int nDim, boolean bAscending){
        m_nDim=nDim;
        m_bAscending=bAscending;
        m_pdRankedData=new double[m_nDim];
        m_pnIndexes=new int[m_nDim];
        double dInit=Double.POSITIVE_INFINITY;
        if(!m_bAscending){
            dInit=Double.NEGATIVE_INFINITY;
        }
        for(int i=0;i<m_nDim;i++){
            m_pdRankedData[i]=dInit;
            m_pnIndexes[i]=-1;
        }
        m_nDataSize=0;
    }
    public void updateRankings(double[] pdData, int iI, int iF){
        for(int i=iI;i<=iF;i++){
            updateRankings(pdData[i],i);
        }
    }
    public void updateRankings(ArrayList<Double> dvData, int iI, int iF){
        for(int i=iI;i<=iF;i++){
            updateRankings(dvData.get(i),i);
        }
    }
    public void reset(){
        double dInit=Double.POSITIVE_INFINITY;
        if(!m_bAscending){
            dInit=Double.NEGATIVE_INFINITY;
        }
        for(int i=0;i<m_nDim;i++){
            m_pdRankedData[i]=dInit;
            m_pnIndexes[i]=-1;
        }
        m_nDataSize=0;
    }
    public void updateRankings(double dData, int index0){
        int index,it,it0,i;
        double dt,dt0;
        if(m_bAscending)
            index=CommonMethods.getNumOfSmallerOrEqualElements(m_pdRankedData, dData);
        else
            index=CommonMethods.getNumOfLargerOrEqualElements(m_pdRankedData, dData);
        if(index<m_nDim){
            dt0=m_pdRankedData[index];
            it0=m_pnIndexes[index];
            m_pdRankedData[index]=dData;
            m_pnIndexes[index]=index0;
            for(i=index+1;i<m_nDim;i++){
                dt=m_pdRankedData[i];
                it=m_pnIndexes[i];
                m_pdRankedData[i]=dt0;
                m_pnIndexes[i]=it0;
                dt0=dt;
                it0=it;
            }
        }
        m_nDataSize++;
    }
    public int getIndex(int nRanking){
        return m_pnIndexes[nRanking];
    }
    public double getElement(int nRanking){
        return m_pdRankedData[nRanking];
    }
    public int getDim(){
        return m_nDim;
    }
    public void merge(PartialArraySorter rk){
        int nDim=rk.getDim();
        for(int i=0;i<nDim;i++){
            updateRankings(rk.getElement(i),rk.getIndex(i));
        }
    }
    public boolean containsIndex(int index){
        for(int i=0;i<m_nDim;i++){
            if(index==m_pnIndexes[i]) return true;
        }
        return false;
    }
    public int getDataSize(){
        return m_nDataSize;
    }
}
