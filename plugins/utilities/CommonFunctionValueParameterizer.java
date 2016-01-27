/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import utilities.statistics.MeanSem1;
import utilities.statistics.Histogram;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.DoubleRange;

/**
 *
 * @author Taihao
 */
public class CommonFunctionValueParameterizer {
    double[][] pdX;
    double[] pdY;
    double dMax,dMin,mean;
    int maxPosition,minPosition;
    double m_dSD;//population sd
    public CommonFunctionValueParameterizer(double[][] pdX, double[] pdY){
        this.pdX=pdX;
        this.pdY=pdY;
        init();
    }
    void init(){
        int len=pdX.length,i,iMin=0,iMax=0;
        dMax=Double.NEGATIVE_INFINITY;
        dMin=Double.POSITIVE_INFINITY;
        mean=0;
        double dt;
        for(i=0;i<len;i++){
            dt=pdY[i];
            mean+=dt;
            if(dt>dMax){
                dMax=dt;
                iMax=i;
            }
            if(dt<dMin){
                dMin=dt;
                iMin=i;
            }
        }
        mean/=len;
        maxPosition=iMax;
        minPosition=iMin;
    }
    public double getMean(){
        return mean;
    }
    public double getMax(){
        return dMax;
    }
    public double getMin(){
        return dMin;
    }
    public int getMaxPosition(){
        return maxPosition;
    }
    public int getMinPosition(){
        return minPosition;
    }
    public int getDataSize(){
        return pdX.length;
    }
    public MeanSem1 getPopulationMeansem(int index){
        DoubleRange dr=CommonStatisticsMethods.getRange(pdX,index),ydr=CommonStatisticsMethods.getRange(pdY);
        double dMin=dr.getMin(),dMax=dr.getMax(),yMin=ydr.getMin();
        Histogram hist=new Histogram();
        int len=pdX.length,i;
        double dDelta=(dr.getMax()-dr.getMin())/(len-1);
        hist.update(dMin-0.5*dDelta, dMax+0.5*dDelta, dDelta);
        for(i=0;i<len;i++){
            hist.addData(pdX[i][index],pdY[i]-yMin);
        }
        MeanSem1 ms=hist.getMeanSem1();
        ms.shiftMean(yMin);
        if(ms.n>1)
            m_dSD=ms.getSD();
        else
            m_dSD=hist.getSD();
        return ms;
    }
    public double getSD(){
        return m_dSD;
    }
}
