/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.io.PrintAssist;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import utilities.io.IOAssist;
import java.util.ArrayList;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;

/**
 *
 * @author Taihao
 */
public class MeanSem1 implements StatisticalSummary{
    public static final int Integer=0,Fraction=1;
    int debugginPause;
    int type;
    public int n;
    public double mean, sem2,sem;//mean and the square of the sample standard deviation (not standard deviation of sampe), could have been named as s2
    public double max, min;
    public MeanSem1(ArrayList<Double> dv){
        this();
        int n=dv.size(),i;
        Double mean=0.,mSS=0.,dt;
        double dn=Double.POSITIVE_INFINITY,dx=Double.NEGATIVE_INFINITY;
        for(i=0;i<n;i++){
            dt=dv.get(i);
            if(dt<dn) dn=dt;
            if(dt>dx) dx=dt;
            mean+=dt;
            mSS+=dt*dt;
        }
        mean/=n;
        mSS/=n;
        updateMeanSquareSum(n, mean, mSS,dx,dn);       
    }
    public MeanSem1(double[] pdY){
        this();
        int n=pdY.length,i;
        Double mean=0.,mSS=0.,dt;
        double dn=Double.POSITIVE_INFINITY,dx=Double.NEGATIVE_INFINITY;
        for(i=0;i<n;i++){
            dt=pdY[i];
            if(dt<dn) dn=dt;
            if(dt>dx) dx=dt;
            mean+=dt;
            mSS+=dt*dt;
        }
        mean/=n;
        mSS/=n;
        updateMeanSquareSum(n, mean, mSS,dx,dn);       
    }
    public double getMin(){
        return min;
    }
    public double getMax(){
        return max;
    }
   public double getMean(){
       return mean;
   };

    public MeanSem1(){
        mean=0.;
        n=0;
        sem2=0.;
        sem=0.;
        max=Double.NEGATIVE_INFINITY;
        min=Double.POSITIVE_INFINITY;
        type=0;
    }
    public void exportMeanSem(DataOutputStream ds)throws IOException{
        ds.writeInt(n);
        ds.writeDouble(mean);
        ds.writeDouble(sem);
        ds.writeDouble(min);
        ds.writeDouble(max);
        ds.writeInt(type);
    }
    public int importMeanSem(BufferedInputStream bf)throws IOException{
        n=IOAssist.readInt(bf);
        mean=IOAssist.readDouble(bf);
        sem=IOAssist.readDouble(bf);
        min=IOAssist.readDouble(bf);
        max=IOAssist.readDouble(bf);
        type=IOAssist.readInt(bf);
        update(n,mean,sem,max,min);
        return 1;
    }
    public MeanSem1(MeanSem1 ms){
        this();
        update(ms);
    }
    public MeanSem1(int n, double mean, double sem, double max, double min){
        this();
        update(n, mean, sem, max, min);
    }
    public void update(MeanSem1 ms){
        update(ms.n,ms.mean,ms.sem,ms.max,ms.min);
    }
    public void update(int n, double mean, double sem, double max, double min){
        this.n=n;
        this.mean=mean;
        this.sem=sem;
        this.sem2=sem*sem*n;
        this.max=max;
        this.min=min;
    }
    public void updateMeanSem2(int n, double mean, double sem2, double max, double min){
        this.n=n;
        this.mean=mean;
        this.sem2=sem2;
        this.sem=Math.sqrt(sem2/n);
        this.max=max;
        this.min=min;
    }
    public void updateMeanSquareSum(int n, double mean, double meanSs, double max, double min){
        this.n=n;
        this.mean=mean;
        if(n>1){
            sem2=(meanSs-mean*mean)*n/(double)(n-1);
            sem=Math.sqrt(sem2/n);
        }
        else
        {
            sem2=0;
            sem=0;
        }
        this.max=max;
        this.min=min;
    }
    public int mergeSems(MeanSem1 ms){
        if(ms.empty()) return -1;
        if(empty()) {
            update(ms);
            return 1;
        }
        int n2=ms.n;
        double mean2=ms.mean,sem22=ms.sem2;
        int N=n+n2;
        double meanSS=(sem2*(n-1))+(sem22*(n2-1))+mean*mean*n+mean2*mean2*n2;
        meanSS/=N;
        mean=(mean*n+mean2*n2)/N;
        max=Math.max(max, ms.max);
        min=Math.min(min, ms.min);
        updateMeanSquareSum(N,mean,meanSS,max,min);
        return 1;
    }
    public void scale(double r){
        mean*=r;
        sem2*=r*r;
        sem*=r;
        min*=r;
        max*=r;
    }
    public void shiftMean(double dMean){
        mean+=dMean;
        min+=dMean;
        max+=dMean;
    }
    public boolean empty(){
        return (n<=0);
    }
    public double getSD(){
        return Math.sqrt(sem2);
    }
    public void clear(){
        mean=0.;
        n=0;
        sem2=0.;
        sem=0.;
        max=Double.NEGATIVE_INFINITY;
        min=Double.POSITIVE_INFINITY;
    }
    public void addData( double n, double value){//do nothing and not to be used
        //by an instance of sem1. It's for MeanSemFractional
        
    }
    public int getType(){
        return type;
    }
    public double getSum(){
        return mean*n;
    }
    public double getSize(){
        return n;
    }
    public long getN(){
        return (long) (n+0.5);
    }
    public double getVariance(){
        return sem2;
    };

    public double getStandardDeviation(){
        return getSD();
    };
    public String[][] getMeanSemAsStringArray(){
        int cols=6,rows=2;
        String[][] psData=new String[rows][cols];
        String pst[]={"Mean","S.D.","S.E.","Min","Max","N"};
        psData[0]=pst;
        psData[1][0]=PrintAssist.ToStringScientific(mean, 3);
        psData[1][1]=PrintAssist.ToStringScientific(getSD(),3);
        psData[1][2]=PrintAssist.ToStringScientific(sem, 3);
        psData[1][3]=PrintAssist.ToStringScientific(min, 3);
        psData[1][4]=PrintAssist.ToStringScientific(max, 3);
        psData[1][5]=PrintAssist.ToStringScientific(n, 3);
        return psData;
    }
    public String toString(){
        String st="";
        st+="Mean="+PrintAssist.ToStringScientific(mean, 3);
        st+="  S.D.="+PrintAssist.ToStringScientific(getSD(), 3);
        st+="  Sem="+PrintAssist.ToStringScientific(sem, 3);
        st+="  Min="+PrintAssist.ToStringScientific(min, 3);
        st+="  Max="+PrintAssist.ToStringScientific(max, 3);
        st+="  n="+PrintAssist.ToString(n, 0);
        st+=PrintAssist.newline;
        return st;
    }
}
