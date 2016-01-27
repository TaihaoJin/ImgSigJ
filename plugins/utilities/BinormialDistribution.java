/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;
/**
 *
 * @author Taihao
 */
public class BinormialDistribution {
    public static double binormialCoeffient(int n, int k){
        double co=1.;
        int m=n-k;
        if(m>k) {
            int t=k;
            k=m;
            m=t;
        }
        for(int i=1;i<=m;i++){
            co*=(double)(n-i+1)/(double)(m-i+1);
        }
        return co;        
    }
    public static double prob(int n, int k, double p){
        double prob=0.;
        double q=1.-p;
        prob=binormialCoeffient(n,k)*Math.pow(p, k)*Math.pow(q, n-k);        
        return prob;
    }
}
