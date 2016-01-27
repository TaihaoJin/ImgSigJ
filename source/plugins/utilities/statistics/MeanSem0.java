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
public class MeanSem0 {
    int debugginPause;
    public int n;
    public double mean, sem2,sem;//mean and the square of the sample standard deviation (not standard deviation of sampe), could have been named as s2
    public MeanSem0(){
        mean=0.;
        n=0;
        sem2=0.;
        sem=0.;
    }
    public MeanSem0(ArrayList<Double> dv){
        this();
        int n=dv.size(),i;
        Double mean=0.,mSS=0.,dt;
        for(i=0;i<n;i++){
            dt=dv.get(i);
            mean+=dt;
            mSS+=dt*dt;
        }
        mean/=n;
        mSS/=n;
        updateMeanSquareSum(n, mean, mSS);       
    }
    public MeanSem0(double[] pdV){
        this();
        int n=pdV.length,i;
        Double mean=0.,mSS=0.,dt;
        for(i=0;i<n;i++){
            dt=pdV[i];
            mean+=dt;
            mSS+=dt*dt;
        }
        mean/=n;
        mSS/=n;
        updateMeanSquareSum(n, mean, mSS);       
    }
    public MeanSem0(MeanSem0 ms){
        update(ms);
    }
    public MeanSem0(int n, double mean, double sem){
        update(n, mean, sem);
    }
    public void update(MeanSem0 ms){
        mean=ms.mean;
        sem2=ms.sem2;
        n=ms.n;
        sem=ms.sem;
    }
    public void update(int n, double mean, double sem){
        this.n=n;
        this.mean=mean;
        this.sem=sem;
        this.sem2=sem*sem*n;
    }
    public void updateMeanSem2(int n, double mean, double sem2){
        this.n=n;
        this.mean=mean;
        this.sem2=sem2;
        this.sem=Math.sqrt(sem2/n);
    }
    public void updateMeanSquareSum(int n, double mean, double meanSs){
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
    }
    public int mergeSems(MeanSem0 ms){
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
        updateMeanSquareSum(N,mean,meanSS);
        return 1;
    }
    public void scale(double r){
        mean*=r;
        sem2*=r*r;
        sem*=r;
    }
    public void shiftMean(double dMean){
        mean+=dMean;
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
    }
}
