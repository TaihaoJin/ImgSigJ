/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.ArrayList;
import utilities.CommonMethods;
/**
 *
 * @author Taihao
 */
public class SortedDoubleArray {
    //sorted into ascending order
    ArrayList<Double> m_dvData;
    ArrayList<Integer> m_nvIndexes;
    public SortedDoubleArray(){
        m_dvData=new ArrayList();
        m_nvIndexes=new ArrayList();
    }
    public void addData(double dv, int index){
        int Ranking=CommonMethods.getNumOfSmallerElements(m_dvData, dv, 1);
        if(Ranking<m_dvData.size()){
            m_dvData.add(Ranking, dv);
            m_nvIndexes.add(Ranking,index);
        }else{
            m_dvData.add(dv);
            m_nvIndexes.add(index);
        }
    }
    public void removeData(double dv, int index){
        int Ranking=CommonMethods.getNumOfSmallerElements(m_dvData, dv, 1);
        int i,len=m_dvData.size();
        for(i=Ranking;i<len;i++){
            if(m_nvIndexes.get(i)==index) {
                m_dvData.remove(i);
                m_nvIndexes.remove(i);
                break;
            }
        }
    }
    public void addData(double[] pdY,int iI,int iF, int delta){
        for(int i=iI;i<=iF;i+=delta){
            addData(pdY[i],i);
        }
    }
    public int getDataSize(){
        return m_dvData.size();
    }
    public int getIndex(int Ranking){
        return m_nvIndexes.get(Ranking);
    }
    public double getData(int Ranking){
        return m_dvData.get(Ranking);
    }
}
