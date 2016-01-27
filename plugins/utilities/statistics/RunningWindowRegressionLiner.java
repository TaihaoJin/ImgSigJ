/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import java.util.ArrayList;
import org.apache.commons.math.stat.regression.SimpleRegression;
import ij.IJ;


/**
 *
 * @author Taihao
 */
public class RunningWindowRegressionLiner {
    protected int nDataSize,numPredictions;
    double dWSL,dWSR;
    protected ArrayList<Double> m_dvX,m_dvY;
    protected double[] m_pdPredictions, m_pdSlopes;
    public RunningWindowRegressionLiner(ArrayList<Double> dvX, ArrayList<Double> dvY, double dWSL, double dWSR){
        this.dWSL=dWSL;
        this.dWSR=dWSR;
        this.m_dvX=dvX;
        this.m_dvY=dvY;
        nDataSize=m_dvX.size();
        m_pdPredictions=new double[nDataSize];
        m_pdSlopes=new double[nDataSize];
        build();
    }
    void build(){
        int index,l,r;
        double x,xl,xr;
        SimpleRegression sr=new SimpleRegression();
        l=0;r=0;
        xl=m_dvX.get(l);
        xr=m_dvX.get(r);
        long nData;

        ArrayList <Integer> nvSkippedIndexes=new ArrayList();
        for(index=0;index<nDataSize;index++){
            x=m_dvX.get(index);
            while(x-xl>dWSL&&l>=0){
                sr.removeData(xl,m_dvY.get(l));
                l++;
                xl=m_dvX.get(l);
            }
            while(xr-x<=dWSR&&r<nDataSize){
                sr.addData(xr,m_dvY.get(r));
                r++;
                if(r>=nDataSize) break;
                xr=m_dvX.get(r);
            }
            nData=sr.getN();
            if(nData<2) {
                nvSkippedIndexes.add(index);
                continue;
            }
            m_pdPredictions[index]=sr.predict(x);
            m_pdSlopes[index]=sr.getSlope();
        }
        completeSkippedIndexes(nvSkippedIndexes);
    }
    void completeSkippedIndexes(ArrayList<Integer> nvSkippedIndexes){
        int direction=1,position,l,r,index,it;
        int len=nvSkippedIndexes.size();
        double xl,xr,x;
        for(position=0;position<len;position++){
            index=nvSkippedIndexes.get(position);
            x=m_dvX.get(index);
            l=nextPredictedIndex(nvSkippedIndexes,position,-1);
            r=nextPredictedIndex(nvSkippedIndexes,position,1);
            if(l<0&&r<0) IJ.error("errors in completeSkippedIndexes");
            if(l<0){
                m_pdSlopes[index]=m_pdSlopes[r];
                m_pdPredictions[index]=predict(r,x);
            }else if(r<0){
                m_pdSlopes[index]=m_pdSlopes[l];
                m_pdPredictions[index]=predict(l,x);
            }else{
                xl=m_dvX.get(l);
                xr=m_dvX.get(r);
                it=l;
                if(x-xl>xr-x) it=r;
                m_pdSlopes[index]=m_pdSlopes[it];
                m_pdPredictions[index]=predict(r,x);
            }
        }       
    }
    int nextPredictedIndex(ArrayList<Integer> nvSkippedIndexes, int position, int direction){
        int delta=1,nextIndex=-1,index0=nvSkippedIndexes.get(position),index;
        if(direction<0) delta=-1;
        position+=delta;
        while(position>=0&&position<nvSkippedIndexes.size()){
            index=nvSkippedIndexes.get(position);
            if(Math.abs(index-index0)>1) break;
            index0=index;
        }
        nextIndex=index0+delta;
        if(nextIndex>=0&&nextIndex<nDataSize) return nextIndex;
        return -1;
    }
    public double predict(int index, double x){
        double y0=m_pdPredictions[index],x0=m_dvX.get(index),slope=m_pdSlopes[index];
        return y0+slope*(x-x0);
    }
    public double predict(int index){
        return m_pdPredictions[index];
    }
    public double getSlope(int index){
        return m_pdSlopes[index];
    }
}
