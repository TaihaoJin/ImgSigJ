/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;

/**
 *
 * @author Taihao
 */
public class OneDKMeans_ExtremExcluded {
    OneDKMeans cKM;
    int nNumExclusion;
    int[] pnClusterIndexes;
    public OneDKMeans_ExtremExcluded(String title, ArrayList<Double> dvData, int K, int nNumExclusion){
        this(title,CommonStatisticsMethods.copyToDoubleArray(dvData),K,nNumExclusion);
    }
    public OneDKMeans_ExtremExcluded(String title, double[] pdData0, int K, int nNumExclusion){
        this.nNumExclusion=nNumExclusion;
        int num=Math.abs(nNumExclusion),len0=pdData0.length,len=len0-num,i;
        int[] indexes=new int[len0], pnClusterIndexes=new int[len];
        double[] pdData=new double[len];
        
        for(i=0;i<len0;i++){
            indexes[i]=i;
        }    
        utilities.QuickSort.quicksort(pdData0, indexes);
        int shift=0;
        if(shift<0) shift=nNumExclusion;
        
        for(i=0;i<len;i++){
            pdData[i]=pdData0[i+shift];
            pnClusterIndexes[i]=0;
        }
        
        int nx;
        for(i=0;i<K-1;i++){
            nx=K-1-i;
            pnClusterIndexes[len-1-i]=nx;
        }
        
        cKM=new OneDKMeans(title,pdData,K,pnClusterIndexes);     
        this.pnClusterIndexes=new int[len0];
        
        pnClusterIndexes=cKM.getClusterIndexes();
        
        int index,position;
        for(i=0;i<len;i++){
            index=pnClusterIndexes[i];
            position=indexes[i+shift];
            this.pnClusterIndexes[position]=index;
        }
        
        int sign=1;
        if(nNumExclusion<0) sign=-1;
        for(i=0;i<num;i++){
            if(sign>0)
                this.pnClusterIndexes[indexes[len+i]]=100;
            else
                this.pnClusterIndexes[indexes[i]]=-100;
        }
    }
    public int[] getClusterIndexes(){
        return pnClusterIndexes;
    }
    public ArrayList<String> toStrings(){
        ArrayList<String> strings=cKM.toStrings();
        strings.add("Exclusion= "+nNumExclusion);
        return strings;
    }
}
