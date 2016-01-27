/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;

/**
 *
 * @author Taihao
 */
public class MeanSemFractional extends MeanSem1{
    public double n;
    public double sum;
    public double squareSum;
    static double dMaxN=0;
    int operation=0;
    public int num;
    public MeanSemFractional(){
        super();
        sum=0;
        n=0;
        type=1;
        squareSum=0;
        num=0;
    }
    public MeanSemFractional(MeanSemFractional ms){
        this();
        update(ms);
    }
    public MeanSemFractional(double n, double mean, double sem, double max, double min,double squareSum,int num){
        this();
        update(n, mean, sem, max, min,squareSum,num);
    }
    public void update(MeanSemFractional ms){
        update(ms.n,ms.mean,ms.sem,ms.max,ms.min,ms.squareSum,ms.num);
    }
    public void update(double n, double mean, double sem, double max, double min,double squareSum, int num){
        this.n=n;
        this.mean=mean;
        this.max=max;
        this.min=min;
        this.sem=sem;
        this.sem2=sem*sem*n;
        sum=mean*n;
        this.squareSum=squareSum;
        this.num=num;
    }
    public void updateMeanSem2(double n, double mean, double sem2, double max, double min){
        this.n=n;
        this.mean=mean;
        this.sem2=sem2;
        this.sem=Math.sqrt(sem2/n);
        this.max=max;
        this.min=min;
        sum=mean*n;
        squareSum=n*calMeanSquareSum();
    }
    
    public void updateMeanSquareSum(double n, double mean, double meanSs, double max, double min){
        this.n=n;
        this.mean=mean;
        double dt=150;
        if(n>dt){
            dt=dt;
        }
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
        sum=mean*n;
        squareSum=n*meanSs;
    }

    public double getMeanSquareSum(){
        return squareSum/n;
    }

    public double calMeanSquareSum(){
        return sem2*(n-1)/n+mean*mean;
    }

    public int mergeSems(MeanSemFractional ms){//the definition of deviation might not be meaningful for fractional meansem.
        //this method is kept from previous version. need to verify.
        this.num+=ms.num;
        if(ms.empty()) return -1;
        if(empty()) {
            update(ms);
            return 1;
        }
        double n2=ms.n;
        double mean2=ms.mean;
        double N=n+n2;
        double meanSS=this.squareSum+ms.squareSum;
        meanSS/=N;
        mean=(mean*n+mean2*n2)/N;
        max=Math.max(max, ms.max);
        min=Math.min(min, ms.min);
        updateMeanSquareSum(N,mean,meanSS,max,min);
        return 1;
    }

    public void scale(double r){
        mean*=r;
        min*=r;
        sum*=r;
        max*=r;
        squareSum=n*r*r;
    }

    public void shiftMean(double dMean){
        mean+=dMean;
        min+=dMean;
        max+=dMean;
        sum=n*mean;
        squareSum=n*calMeanSquareSum();
    }

    public boolean empty(){
        return (n<=0);
    }

    public void clear(){
        mean=0.;
        n=0;
        max=Double.NEGATIVE_INFINITY;
        min=Double.POSITIVE_INFINITY;
        sum=0;
        num=0;
        squareSum=0;
        sem=0;
        sem2=0;
    }
    public void addData(double dFraction, double dA){
        num++;
        operation=1;
        n+=dFraction;
        sum+=dFraction*dA;
        squareSum+=dFraction*dA*dA;
        mean=sum/n;
        if(dA<min) min=dA;
        if(dA>max) max=dA;
        updateMeanSquareSum(n,mean,squareSum/n,max,min);
        operation=0;
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
}
