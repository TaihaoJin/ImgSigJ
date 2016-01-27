/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;

/**
 *
 * @author Taihao
 */
import utilities.CommonMethods;
public class IntHistogram {
    int pnHist[],pnRank[];
    int nMin,nMax,num;
    public IntHistogram(int[][] pnValues,int iI,int iF, int jI, int jF){
        int i,j,it,in=Integer.MAX_VALUE,ix=Integer.MIN_VALUE;
        
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                it=pnValues[i][j];
                if(it<in) in=it;
                if(it>ix) ix=it;
            }
        }
        
        num=(iF-iI+1)*(jF-jI+1);
        
        int len=ix-in+1;
        nMin=in;
        nMax=ix;
        pnHist=new int[len];
        pnRank=new int[len];
        for(i=0;i<len;i++){
            pnHist[i]=0;
        }
        
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                it=pnValues[i][j];
                pnHist[it-nMin]++;
            }
        }
        int n=0;
        for(i=0;i<len;i++){
            n+=pnHist[len-1-i];
            pnRank[i]=n;
        }
        
    }
    public int getRank(int n){
        if(n>nMax) return -1;
        if(n<nMin) return num+1;
        return pnRank[n-nMin];
    }
    public int getNumAtRank(int n){
        int i=0;
        if(n>=num) return nMin;
        while(pnRank[i]<n){
            i++;
        }
        return nMin+i;
    }
    public int getNumAtRant(double quantile){
        return getNumAtRank((int)(quantile*num));
    }
    public boolean OutOfRange(int n){
        return (n<nMin||n>nMax);
    }
    public int getSize(){
        return num;
    }
}
