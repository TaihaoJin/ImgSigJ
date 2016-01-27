/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import utilities.CommonStatisticsMethods;
import utilities.QuickSort;
import utilities.statistics.OneDNearestNeighbor;
import java.util.ArrayList;
import utilities.io.PrintAssist;
import utilities.statistics.MeanSem1;
/**
 *
 * @author Taihao
 */
public class OneDKMeans {
    int m_nSize,K,iterations;
    double m_dMin,m_dMax;
    double[] m_pdDelimiters;
    double[] m_pdData;
    double[] m_pdMeans;
    int[] m_pnClusterIndexes,m_pnClusterSizes;
    MeanSem1[] pcClusterMeanSems;
    double m_pdWeights[];
    int m_nMainCluster;    
    String title;
    
    OneDNearestNeighbor m_cNearestNeighbor;
    public OneDKMeans(){
        title="";
    }
    public OneDKMeans(String title){
        this.title=title;
    }
    public OneDKMeans(double[] pdData,int K){
        this();
        updateData(pdData,K);
    }
    
    public void updateData(double[] pdData,int K){
        this.K=K;
        m_nSize=pdData.length;
        m_pdData=pdData;
        init();
        buildClusters();        
    }
    public void updateData(double[] pdData,int K,int[] pnClusterIndexes){
        this.K=K;
        m_nSize=pdData.length;
        m_pdData=pdData;
        init(pnClusterIndexes);
        buildClusters();        
    }
    public ArrayList<String> toStrings(){
        ArrayList<String> svSts=new ArrayList();
        String st=title+" KMean: K="+K+" Data Size = "+m_nSize+PrintAssist.newline;
        svSts.add(st);
        double Gap;
        for(int i=0;i<K;i++){
            st="Mean"+i+" = "+PrintAssist.ToString(m_pdMeans[i], 12, 3)+"     Cluster size"+i+" = "+PrintAssist.ToString(m_pnClusterSizes[i], 6, 0)+"    Weight"+i+" = "+PrintAssist.ToString(m_pdWeights[i], 12, 3);
            if(i==0) 
                Gap=0;
            else
                Gap=pcClusterMeanSems[i].min-pcClusterMeanSems[i-1].max;
            st+="  Min = "+PrintAssist.ToString(pcClusterMeanSems[i].min, 12, 3)+"  Max = "+PrintAssist.ToString(pcClusterMeanSems[i].max, 12, 3)+"  Gap = "+PrintAssist.ToString(Gap, 12, 3)+PrintAssist.newline;
            svSts.add(st);
        }
        return svSts;
    }
    public OneDKMeans(ArrayList<Double> dvData,int K){
        updateData(CommonStatisticsMethods.copyToDoubleArray(dvData),K);
    }
    
    public OneDKMeans(String title, ArrayList<Double> dvData,int K,int[] nvClusterIndexes){
        this(title);
        updateData(CommonStatisticsMethods.copyToDoubleArray(dvData),K,nvClusterIndexes);
    }
    
    public OneDKMeans(double[] pdData,int K,int[] nvClusterIndexes){
        updateData(pdData,K,nvClusterIndexes);
    }
    public OneDKMeans(String title, double[] pdData,int K,int[] nvClusterIndexes){
        this.title=title;
        updateData(pdData,K,nvClusterIndexes);
    }
    
    void init(){
        int i,j,nDelta=(m_nSize+1)/K,index;
        m_pdMeans=new double[K];
        m_pnClusterSizes=new int[K];
        m_pnClusterIndexes=new int[m_nSize];
        CommonStatisticsMethods.setElements(m_pdMeans, 0);        
        CommonStatisticsMethods.setElements(m_pnClusterSizes, 0);        
        
        double dt,dn=Double.POSITIVE_INFINITY,dx=Double.NEGATIVE_INFINITY;
        double[] pdMeans=new double[K];
        
        for(i=0;i<m_nSize;i++){
            dt=m_pdData[i];
            if(dt<dn) dn=dt;
            if(dt>dx) dx=dt;
        }
                
        double delta=(dx-dn)/(K+1);
        m_dMin=dn;
        m_dMax=dx;
        
        int nNeighbors=m_pdData.length/(K+3);
        if(m_cNearestNeighbor==null) 
            m_cNearestNeighbor=new OneDNearestNeighbor(m_pdData,nNeighbors,m_pdData.length);
        else
            m_cNearestNeighbor.updateData(m_pdData, nNeighbors,m_pdData.length);
        
         pdMeans=m_cNearestNeighbor.getMainPeaks(K);
         while(pdMeans.length<K&&nNeighbors>1){
             nNeighbors--;
             m_cNearestNeighbor.updateData(m_pdData, nNeighbors,m_pdData.length);
             pdMeans=m_cNearestNeighbor.getMainPeaks(K);
         }
         
         if(pdMeans.length<K){
             double pdt[]=new double[K];
             for(i=0;i<K;i++){
                 if(i<pdMeans.length)
                     pdt[i]=pdMeans[i];
                 else
                     pdt[i]=(m_dMax-m_dMin)*Math.random()+m_dMin;
             }
             QuickSort.quicksort(pdt);
             pdMeans=pdt;
         }
        double[] pdValleys=m_cNearestNeighbor.getMainValleys(K-1);
/*        for(i=0;i<K;i++){
            if(i<K-1)
                dx=pdValleys[i];
            else
                dx=m_dMax;
            pdMeans[i]=0.5*(dn+dx);
            dn=dx;
        }
*/
        for(i=0;i<m_nSize;i++){
            dt=m_pdData[i];
            index=0;
            delta=Math.abs(dt-pdMeans[0]);
            dn=delta;
            for(j=0;j<K;j++){
                delta=Math.abs(dt-pdMeans[j]);
                if(delta<dn){
                    index=j;
                    dn=delta;
                }
            }
            m_pnClusterIndexes[i]=index;
            m_pdMeans[index]+=m_pdData[i];
            m_pnClusterSizes[index]+=1;
        }
    }
    
    void init(int[] pnClusterIndexes){
        int i,index;        
        m_pnClusterIndexes=new int[m_nSize];
        m_pdMeans=new double[K];
        m_pnClusterSizes=new int[K];
        for(i=0;i<m_nSize;i++){
            index=pnClusterIndexes[i];
            m_pnClusterIndexes[i]=index;
            m_pdMeans[index]+=m_pdData[i];
            m_pnClusterSizes[index]+=1;
       }
    }
    
    void buildClusters(){        
        int i,j,num=-1,index0,index;
        double[] pdMeans=new double[K];
        double dn,delta,dx;
        int nMaxiterations=50;
        iterations=0;
        while(num!=0){
            num=0;
            for(i=0;i<K;i++){
                if(m_pnClusterSizes[i]>0)
                    pdMeans[i]=m_pdMeans[i]/m_pnClusterSizes[i];
                else
                    pdMeans[i]=m_dMin+Math.random()*(m_dMax-m_dMin);
            }
            for(i=0;i<m_nSize;i++){
                dx=m_pdData[i];
                index0=m_pnClusterIndexes[i];
                index=index0;
                delta=Math.abs(dx-pdMeans[index]);
                dn=delta;
                for(j=0;j<K;j++){
                    delta=Math.abs(dx-pdMeans[j]);
                    if(delta<dn){
                        index=j;
                        dn=delta;
                    }
                }
                
                if(index!=index0){
                    num++;
                    m_pnClusterSizes[index0]--;
                    m_pnClusterSizes[index]++;
                    m_pdMeans[index]+=dx;
                    m_pdMeans[index0]-=dx;
                    m_pnClusterIndexes[i]=index;
                }
            }
            if(num==0){
                for(i=0;i<K;i++){
                    if(m_pnClusterSizes[i]==0){
                        num=-1;//to force it for more iterations
                        break;
                    }
                }
            }
            iterations++;
            if(iterations>nMaxiterations) break;
        }
        m_pdWeights=CommonStatisticsMethods.getEmptyDoubleArray(m_pdWeights, K);
        double size=-1,nx=-1;
        for(i=0;i<K;i++){
            size=m_pnClusterSizes[i];
            m_pdMeans[i]/=size;
            m_pdWeights[i]=size/m_nSize;
            if(size>nx){
                nx=size;
                m_nMainCluster=i;
            }
        }
        sortClusters();
        buildClusterMeanSems();
        buildDelimiters();
    }
    void buildDelimiters(){
        m_pdDelimiters=new double[K-1];
        for(int i=1;i<K;i++){
            m_pdDelimiters[i-1]=0.5*(pcClusterMeanSems[i].min+pcClusterMeanSems[i-1].max);
        }
    }
    void buildClusterMeanSems(){
        ArrayList<Double>[] pcv=new ArrayList[K];
        int i;
        for(i=0;i<K;i++){
            pcv[i]=new ArrayList();
        }
        for(i=0;i<m_nSize;i++){
            pcv[m_pnClusterIndexes[i]].add(m_pdData[i]);
        }
        pcClusterMeanSems=new MeanSem1[K];
        for(i=0;i<K;i++){
            pcClusterMeanSems[i]=CommonStatisticsMethods.buildMeanSem1(pcv[i], 0, pcv[i].size()-1,1);
        }        
    }
    public MeanSem1[] getClusterMeanSems(){
        return pcClusterMeanSems;
    }
    public int getK(){
        return K;
    }
    public double[] getMeans(){
        return m_pdMeans;
    }
    public int[] getClusterSizes(){
        return m_pnClusterSizes;
    }
    public int[] getClusterIndexes(){
        return m_pnClusterIndexes;
    }
    public int getMainCluster(){
        return m_nMainCluster;
    }
    public double[] getWeights(){
        return m_pdWeights;
    }
    public int getLowestClusterIndex(int sign){
        double dn=m_pdMeans[0],mean;
        int index=0;
        for(int i=1;i<K;i++){
            mean=m_pdMeans[i];
            if(sign*(mean-dn)<0){
                dn=mean;
                index=i;
            }
        }
        return index;
    }
    public void sortClusters(){
        double[] pdMeans=new double[K];
        int i,num=0,nt,it;
        double dt;
        int[] indexes=new int[K],ranking=new int[K];
        for(i=0;i<K;i++){
            indexes[i]=i;
            ranking[i]=i;
            if(m_pnClusterSizes[i]>0)
                pdMeans[i]=m_pdMeans[i]/m_pnClusterSizes[i];
            else
                pdMeans[i]=m_dMin+Math.random()*(m_dMax-m_dMin);
        }
       utilities.QuickSort.quicksort(pdMeans, indexes);
       for(i=0;i<K;i++){
            ranking[indexes[i]]=i;
        }
        double[] pdW=CommonStatisticsMethods.copyArray(m_pdWeights);
        CommonStatisticsMethods.copyArray(m_pdMeans, pdMeans);
        int[] pnNum=CommonStatisticsMethods.copyArray(m_pnClusterSizes);
        for(i=0;i<K;i++){
            it=indexes[i];
            m_pnClusterSizes[i]=pnNum[it];
            m_pdMeans[i]=pdMeans[it];
            m_pdWeights[i]=pdW[it];
        }
        
        for(i=0;i<m_nSize;i++){
            it=m_pnClusterIndexes[i];
            m_pnClusterIndexes[i]=ranking[it];
        }
    }
    public double[] getDelimiters(){
        return m_pdDelimiters;
    }
    public void getData(ArrayList<Double> dvData){
        dvData.clear();
        for(int i=0;i<m_pdData.length;i++){
            dvData.add(m_pdData[i]);
        }
    }
    public ArrayList<Double> getDataDv(){
        ArrayList<Double> dv=new ArrayList();
        getData(dv);
        return dv;
    }
    public ArrayList<Double> getGapIndexes(){
        ArrayList<Double> dv=new ArrayList();
        MeanSem1 ms,ms0=pcClusterMeanSems[0];
        for(int i=1;i<K;i++){
            ms=pcClusterMeanSems[i];
            dv.add((ms.min-ms0.max)/ms0.max);
            ms0=ms;
        }
        return dv;
    }
}
