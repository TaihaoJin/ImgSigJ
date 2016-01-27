/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class MeanSemHandler {
    public int n;
    ArrayList<Double> adDate;
    int[] pnData;
    public double mean,var,sd,sem,rms2;//var: the variance, sd: standard deviation, sem: standard error of mean, rms2: squar of rms (the mean of squares).
    public double dmax,dmin;
    boolean firstMax,firstMin;
    public MeanSemHandler(){
        reset();
    }
    public MeanSemHandler(ArrayList<Double> data){
        this();
        n=data.size();
        double d;
        for(int i=0;i<n;i++){
            d=data.get(i);
            if(firstMax){
                dmax=d;
                firstMax=false;
            }
            else if(d>dmax) dmax=d;
            if(firstMin){
                dmin=d;
                firstMin=false;
            }
            else if(d<dmin) dmin=d;
            mean+=d;
            rms2+=d*d;
        }
        mean/=n;
        rms2/=n;
        var=rms2-mean*mean;
        sd=Math.sqrt(var);
        if(n>1) sem=Math.sqrt(var/(n-1));
    }
    public MeanSemHandler(int[] data){
        this();
        n=data.length;
        double d;
        for(int i=0;i<n;i++){
            d=data[i];
            if(firstMax){
                dmax=d;
                firstMax=false;
            }
            else if(d>dmax) dmax=d;
            if(firstMin){
                dmin=d;
                firstMin=false;
            }
            else if(d<dmin) dmin=d;
            mean+=d;
            rms2+=d*d;
        }
        mean/=n;
        rms2/=n;
        var=rms2-mean*mean;
        sd=Math.sqrt(var);
        if(n>1) sem=Math.sqrt(var/(n-1));
    }
    public void reset(){
        mean=0.;
        n=0;
        rms2=0.;
        sem=0.;
        firstMax=true;
        firstMin=true;
    }
}
