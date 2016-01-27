/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class RunningWindowRankingKeeper {
    int m_nDim;
    double[] m_pdData;
    int[] m_pnIndexes;
    boolean m_bAscending;
    public RunningWindowRankingKeeper(int nDim, boolean bAscending){
        m_nDim=nDim;
        m_bAscending=bAscending;
        m_pdData=new double[m_nDim];
        m_pnIndexes=new int[m_nDim];
        double dInit=Double.POSITIVE_INFINITY;
        if(!m_bAscending){
            dInit=Double.NEGATIVE_INFINITY;
        }
        for(int i=0;i<m_nDim;i++){
            m_pdData[i]=dInit;
            m_pnIndexes[i]=-1;
        }
    }
    public void reset(){
        double dInit=Double.POSITIVE_INFINITY;
        if(!m_bAscending){
            dInit=Double.NEGATIVE_INFINITY;
        }
        for(int i=0;i<m_nDim;i++){
            m_pdData[i]=dInit;
            m_pnIndexes[i]=-1;
        }
    }
    public void updateRankings(double dData, int index0){
        int index,it,it0,i;
        double dt,dt0;
        if(m_bAscending)
            index=CommonMethods.getNumOfSmallerOrEqualElements(m_pdData, dData);
        else
            index=CommonMethods.getNumOfLargerOrEqualElements(m_pdData, dData);
        if(index<m_nDim){
            dt0=m_pdData[index];
            it0=m_pnIndexes[index];
            m_pdData[index]=dData;
            m_pnIndexes[index]=index0;
            for(i=index+1;i<m_nDim;i++){
                dt=m_pdData[i];
                it=m_pnIndexes[i];
                m_pdData[i]=dt0;
                m_pnIndexes[i]=it0;
                dt0=dt;
                it0=it;
            }
        }
    }
    public int getIndex(int nRanking){
        return m_pnIndexes[nRanking];
    }
    public double getElement(int nRanking){
        return m_pdData[nRanking];
    }
    public int getDim(){
        return m_nDim;
    }
    public void merge(RunningWindowRankingKeeper rk){
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
}
