/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.QuickSort;
import utilities.QuickSortInteger;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CommonStatisticsMethods;
import utilities.statistics.HypothesisTester;
import utilities.CustomDataTypes.intRange;
import FluoObjects.IPOGTLevelNode;
import ij.gui.PlotWindow;
import utilities.CommonGuiMethods;
import utilities.io.PrintAssist;
import utilities.Non_LinearFitting.FittingComparison;

/**
 *
 * @author Taihao
 */
public class OutliarExcludingLinearRegression extends SimpleRegression{
    ArrayList <Double> m_dvX, m_dvY;
    int m_nMaxOutliars;
    public OutliarExcludingLinearRegression(){

    }
    public OutliarExcludingLinearRegression(ArrayList<Double> dvX, ArrayList<Double> dvY, int nMaxOutliars,int iI, int iF){
        m_dvX=CommonStatisticsMethods.copyDoubleArray(dvX);
        m_dvY=CommonStatisticsMethods.copyDoubleArray(dvY);
        m_nMaxOutliars=nMaxOutliars;
        buildLinearRegression();
    }
    void buildLinearRegression(){
        addData(m_dvX,m_dvY);
        int i,len=m_dvX.size(),j;
        int[] pnIndexes=new int[len];
        double devX=0;
        int ix;
        double dy,dev;
        for(i=0;i<m_nMaxOutliars;i++){
            devX=0;
            ix=0;
            len=m_dvX.size();
            for(j=0;j<len;j++){
                dy=m_dvY.get(j)-predict(m_dvX.get(j));
                dev=dy*dy;
                if(dev>devX){
                    devX=dev;
                    ix=j;
                }
            }
            removeData(ix);
        }
    }
    void removeData(int index){
        removeData(m_dvX.get(index),m_dvY.get(index));
        m_dvX.remove(index);
        m_dvY.remove(index);
    }
    void addData(ArrayList<Double> dvX, ArrayList<Double> dvY){
        int i,len=dvX.size();
        for(i=0;i<len;i++){
            addData(dvX.get(i),dvY.get(i));
        }
    }
}
