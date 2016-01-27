/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.awt.Point;
import java.util.ArrayList;
import utilities.statistics.MeanSem1;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CommonStatisticsMethods;

/**
 *
 * @author Taihao
 */
public class PlaneFitter {
    double s1,s2,s3,s4,t1,t2,t3,t4,u1,u2,u3,u4,z1,z2,z3;//three parameters z1,z2,z3 are the z value at (0,0),(1,0) and (0,1).
    double[][] pdData;
    double [] pdFittedData;
    double ss2;
    MeanSem1 ms;
    public PlaneFitter(){
    }
    public PlaneFitter(double[][] pdData, double[] pdFittedData){
        update(pdData,pdFittedData);
    }
    public void update(double[][] pdData, double[] pdFittedData){
        this.pdData=pdData;
        this.pdFittedData=pdFittedData;
        calParameters();
    }
    void calParameters(){
        int i,j,n=pdData.length;
        double x,y,z,m;
        s1=0;
        s2=0;
        s3=0;
        s4=0;
        t1=0;
        t2=0;t3=0;t4=0;u1=0;u2=0;u3=0;u4=0;
        for(i=0;i<n;i++){
            x=pdData[i][0];
            y=pdData[i][1];
            z=pdData[i][2];
            m=x+y-1;
            s1+=m*m;
            s2-=m*x;
            s3-=m*y;
            s4+=m*z;
            t2+=x*x;
            t3+=x*y;
            t4-=x*z;
            u3+=y*y;
            u4-=y*z;
        }
        t1=s2;
        u1=s3;
        u2=t3;
        z1=(s2*t3-s3*t2)*(t4*u3-t3*u4)-(s4*t3-s3*t4)*(t2*u3-t3*u2);
        z1/=(s1*t3-s3*t1)*(t2*u3-t3*u2)-(s2*t3-s3*t2)*(t1*u3-t3*u1);
        z2=(s1*t3-s3*t1)*(t2*u3-t3*u2)*z1+(s4*t3-s3*t4)*(t2*u3-t3*u2);
        z2/=-1.*(s2*t3-s3*t2)*(t2*u3-t3*u2);
        z3=-1.*(s1*z1+s2*z2+s4)/s3;
        
        pdFittedData=CommonStatisticsMethods.getEmptyDoubleArray(pdFittedData, n);
        ss2=0;
        DoubleRange dr=new DoubleRange();
        double zo,mean=0,mss=0,delta;
        for(i=0;i<n;i++){
            z=calZ(pdData[i][0],pdData[i][1]);
            zo=pdData[i][2];
            pdFittedData[i]=z;
            delta=z-zo;
            dr.expandRange(delta);
            mean+=delta;
            mss+=delta*delta;
        }
        mean/=n;
        mss/=n;
        ms=new MeanSem1();
        ms.updateMeanSem2(n, mean, mss, dr.getMin(),dr.getMax());
    }
    public double calZ(double x, double y){
        return (z2-z1)*x+(z3-z1)*y+z1;
    }
    public double[] getFittedData(){
        return pdFittedData;
    }
    public double getSS2(){
        return ss2;
    }
}
