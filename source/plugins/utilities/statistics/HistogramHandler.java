/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.io.PrintAssist;
import utilities.statistics.MeanSem0;
import utilities.statistics.Histogram;
import java.util.Formatter;
import utilities.CommonStatisticsMethods;

/**
 *
 * @author Taihao
 */
public class HistogramHandler {

	double m_dBase,m_dDelta,m_pdHist[],m_dSum,m_dSum2,m_dCounts;
	int m_nDim,m_nPeaks,m_nValleys;
	double m_dPeakMinRatio;
	ArrayList<Integer> m_vnPeakIndexes;
	ArrayList<Integer> m_vnValleyIndexes;

    public HistogramHandler()
    {
        m_dPeakMinRatio=0.2;
	m_vnPeakIndexes=new ArrayList();
	m_vnValleyIndexes=new ArrayList();
    }

    public void update(Histogram aHist){
        m_dBase=aHist.getBase();
        m_dDelta=aHist.getDelta();
        m_nDim=aHist.getDim();
        m_dCounts=aHist.getTotalCounts();
        m_pdHist=CommonStatisticsMethods.getEmptyDoubleArray(m_pdHist, m_nDim);
        for(int i=0;i<m_nDim;i++){
            m_pdHist[i]=aHist.getCounts(i);
        }
        findPeaks();
        findValleys();
        m_dSum=aHist.getSum();
        m_dSum2=aHist.getSum2();
    }
    public ArrayList <Integer> getPeakIndexess(){
        findPeaks();
        return m_vnPeakIndexes;
    }
    public int getLastPeakPosition(){
        findPeaks();
        int size=m_vnPeakIndexes.size();
        return m_vnPeakIndexes.get(size-1);
    }
    public int getFirstPeakPosition(){
        findPeaks();
        if(m_vnPeakIndexes.size()>0) return m_vnPeakIndexes.get(0);
        return -1;
    }
    public void setPeakCriteria(double peakMinRatio){
        m_dPeakMinRatio=peakMinRatio;
    }
    void findPeaks(){
        m_vnPeakIndexes.clear();
        double x0=m_pdHist[0];
        double x=m_pdHist[1];
        double x1;
        int i,j;
        for(i=1;i<m_nDim-1;i++){
            x1=m_pdHist[i+1];
            if((x-x1)>0&&(x-x0)>0){
                m_vnPeakIndexes.add(i);
            }
            if(x1!=x){
                x0=x;
                x=x1;
            }
        }
        m_nPeaks=m_vnPeakIndexes.size();
    }
    void findValleys(){
        m_vnValleyIndexes.clear();
        if(m_dCounts<0.001)
        {
            //empty histogram
            m_nValleys=0;
        }else{
            int peaks=m_vnPeakIndexes.size();
            int i,j,pmin=0,ji=0,jf;
            double dmin,x;
            if(peaks>0)ji=m_vnPeakIndexes.get(0);
            for(i=1;i<peaks;i++){
                jf=m_vnPeakIndexes.get(i);
                dmin=m_pdHist[ji];
                for(j=ji;j<=jf;j++){
                    x=m_pdHist[j];
                    if(x<dmin){
                        dmin=x;
                        pmin=j;
                    }
                }
                m_vnValleyIndexes.add(pmin);
                ji=jf;
            }
            m_nValleys=m_vnValleyIndexes.size();
        }
    }
    void removeLowestPeak(){
        int pIndexes[]=new int[2];
        int peakIndex,valleyIndex;
        findLowestPeak(pIndexes);
        peakIndex=pIndexes[0];
        valleyIndex=pIndexes[1];
        removePeak(peakIndex,valleyIndex);//checked
    }
    boolean removeLowestPeak(double threshold){
        int pIndexes[]=new int[2];
        int peakIndex,valleyIndex;
        boolean removed=false;
        findLowestPeak(pIndexes);
        peakIndex=pIndexes[0];
        valleyIndex=pIndexes[1];
        if((m_pdHist[peakIndex]-m_pdHist[valleyIndex])<threshold){
            removePeak(peakIndex,valleyIndex);
            removed=true;
        }
        return removed;
    }
//    void findLowestPeak(int &peakIndex, int &valleyIndex){
    void findLowestPeak(int pIndexes[]){//the hist have to have at leas two peaks to have a valley
        int peakIndex,valleyIndex;
        int hmin=0,vmin=0,h,v;
        double dmin,x;
        boolean first=true;
        int i;
        dmin=0;
        for(i=0;i<m_nValleys;i++){
            h=m_vnPeakIndexes.get(i);
            v=m_vnValleyIndexes.get(i);
            x=m_pdHist[h]-m_pdHist[v];
            if(first){
                dmin=x+1;
                first=false;
            }
            if(x<dmin){
                vmin=v;
                hmin=h;
                dmin=x;
            }
            h=m_vnPeakIndexes.get(i+1);
            x=m_pdHist[h]-m_pdHist[v];
            if(x<dmin){
                vmin=v;
                hmin=h;
                dmin=x;
            }
        }
        peakIndex=hmin;
        valleyIndex=vmin;//checked
        pIndexes[0]=peakIndex;
        pIndexes[1]=valleyIndex;
    }

    void removePeak(int peakIndex, int valleyIndex){
        ArrayList<Integer> vi=new ArrayList(),hi=new ArrayList();
        int i,v,h;
        for(i=0;i<m_nValleys;i++){
            v=m_vnValleyIndexes.get(i);
            if(v!=valleyIndex) vi.add(v);
        }
        for(i=0;i<m_nPeaks;i++){
            h=m_vnPeakIndexes.get(i);
            if(h!=peakIndex) hi.add(h);
        }
        m_vnPeakIndexes.clear();
        m_vnPeakIndexes=hi;
        m_vnValleyIndexes.clear();
        m_vnValleyIndexes=vi;
        m_nValleys=vi.size();
        m_nPeaks=hi.size();//checked
    }

    int findPeaks(ArrayList<Integer> peakIndexes, ArrayList<Integer> valleyIndexes, int numPeaks){//find numPeaks most prominent (acording to the peak/valley difference) peaks. It will modify the numPeaks
                                                                   //if the histogram contains less peaks than it.
        if(numPeaks>m_nPeaks) numPeaks=m_nPeaks;
        int h,v;
        while(m_nPeaks>numPeaks){
            removeLowestPeak();
        }
        int i,j;
        peakIndexes.clear();
        valleyIndexes.clear();
        for(i=0;i<m_nPeaks;i++){
            h=m_vnPeakIndexes.get(i);
            peakIndexes.set(i, h);
//            peakIndexes[i]=h;
        }
        for(i=0;i<m_nValleys;i++){
            v=m_vnValleyIndexes.get(i);
//            valleyIndexes[i]=v;
            valleyIndexes.set(i, v);
        }
        findPeaks();
        findValleys();
        return numPeaks;
    }
    void findSignificantPeaks(){
        if(m_dCounts>0.1){
            double threshold=calPeakThreshold();
            while(removeLowestPeak(threshold)){
            }
        }
    }
    void findSignificantPeaks(ArrayList<Integer> peakIndexes, ArrayList<Integer> valleyIndexes){
        findSignificantPeaks();
        ArrayList<Integer> hi,vi;
        hi=getPeakIndexes();
        int size,i;
        size=hi.size();
        for(i=0;i<size;i++){
//            peakIndexes[i]=hi[i];
            peakIndexes.set(i, hi.get(i));
        }
        vi=getValleyIndexes();
        size=vi.size();
        for(i=0;i<size;i++){
//            valleyIndexes[i]=vi[i];
            valleyIndexes.set(i, vi.get(i));
        }
    }
    int getLastSignificantPeak(){
        findSignificantPeaks();
        int size=m_vnPeakIndexes.size();
        return m_vnPeakIndexes.get(size-1);
    }
    ArrayList<Integer> getPeakIndexes(){
        ArrayList<Integer> hi=new ArrayList();
        int size=m_vnPeakIndexes.size();
        for(int i=0;i<size;i++){
            hi.add(m_vnPeakIndexes.get(i));
        }
        return hi;
    }
    ArrayList<Integer> getValleyIndexes(){
        ArrayList<Integer> vi=new ArrayList();
        int size=m_vnValleyIndexes.size();
        for(int i=0;i<size;i++){
            vi.add(m_vnPeakIndexes.get(i));
        }
        return vi;
    }
    double calPeakThreshold(){
        ArrayList<Integer> hi=new ArrayList();
        ArrayList<Integer> vi=new ArrayList();
        int peaks=2;
        findPeaks(hi,vi,peaks);
        double x0,x,height;
        int v,h;
        v=vi.get(0);
        h=hi.get(0);
        x=m_pdHist[h];
        h=hi.get(1);
        x0=m_pdHist[h];
        if(x0<x) x=x0;
        height=x-m_pdHist[v];
        return height*m_dPeakMinRatio;
    }
    void normalizePeakRange(int bins, int key, Histogram hist){
        Histogram hist0=new Histogram();
        hist0.copyHist(hist);
        update(hist);
        findSignificantPeaks();
        int n=m_vnPeakIndexes.size();
        int i1=m_vnPeakIndexes.get(0);
        int i2=m_vnPeakIndexes.get(n-1);
        double x1=0.;
        double x2=1.;
        if(key<0){
            x1=-1.;
            x2=0.;
        }
        transformCoordinate(i1,x1,i2,x2,hist);
        hist.resample(1./bins);
        update(hist0);
    }
    void normalizePeakRange(double x1, double x2, Histogram hist){
        Histogram hist0=new Histogram();
        hist0.copyHist(hist);
        update(hist);
        findSignificantPeaks();
        int n=m_vnPeakIndexes.size();
        int i1=m_vnPeakIndexes.get(0);
        int i2=m_vnPeakIndexes.get(n-1);
        transformCoordinate(i1,x1,i2,x2,hist);
        update(hist0);
    }
    void transformCoordinate(int index1, double x1, int index2, double x2, Histogram hist){
        double base, delta;
        double d=(x2-x1)/(index2-index1);
        hist.resetDelta(d);
        hist.resetBinPosition(index1,x1);
    }


    public int getMainPeak()//return the index of the histogram with maximun counts
    {

        int hmax=0,h;
        double dmax,x;
        boolean first=true;
        int i;
        dmax=0;
        for(i=0;i<m_nPeaks;i++){
            h=m_vnPeakIndexes.get(i);
            x=m_pdHist[h];
            if(first){
                dmax=x;
                first=false;
                hmax=h;
            }
            if(x>dmax){
                hmax=h;
                dmax=x;
            }
        }
        return hmax;
    }


    public int getNumPeaks()
    {
        return m_nPeaks;
    }

    public int getNumValleys()
    {
        return m_nValleys;
    }

    public double getPeakPosition(int index)
    {
        int i=m_vnPeakIndexes.get(index);
        return m_dBase+i*m_dDelta;
    }
    public double getValleyPosition(int index)
    {
        int i=m_vnValleyIndexes.get(index);
        return m_dBase+i*m_dDelta;
    }
    public static void exportHistograms(Formatter fp, ArrayList <Histogram> hists){
        int nSize=hists.size();
        ArrayList<Integer> nvPw=new ArrayList(),nvCw=new ArrayList(),nvPp=new ArrayList(),nvCp=new ArrayList();
        ArrayList <String> sCTs=new ArrayList();
        ArrayList <Double> dvTotalCounts=new ArrayList(), dvSums=new ArrayList();
        
        String sPT;
        int nPw,nCw,nPp,nCp,nSw=8,nSp=4;
        int i,j,nDim;
        Histogram hist;
//        PrintAssist cpa;
        double dMean, dSem;
        int nExtraSpace=2;
        for(i=0;i<nSize;i++){
            hist=hists.get(i);
            nPw=hist.getPositionPrintingLength()+nExtraSpace;
            nCw=hist.getCountPrintingLength()+nExtraSpace;
            nPp=hist.getPositionPrecision();
            nCp=hist.getCountPrecision();
            nvPw.add(nPw);
            nvCw.add(nCw);
            nvPp.add(nPp);
            nvCp.add(nCp);
            sCTs.add(hist.getCountTitle());
            dvTotalCounts.add(hist.getTotalCounts());
            dvSums.add(0.);
        }
    //	cpa.printString(fp,sPT,nvPw[0]);
        int nMax=0;
        int nSum;
        int nMaxDim=0;
        for(i=0;i<nSize;i++){
            hist=hists.get(i);
            sPT=hist.getPositionTitle();
            nDim=hist.getDim();
            if(nDim>nMaxDim) nMaxDim=nDim;
            PrintAssist.printString(fp,sPT,nvPw.get(i));
            PrintAssist.printString(fp,hist.getCountTitle(),nvCw.get(i));
            PrintAssist.printString(fp,"Sum"+i,nSw);
            nSum=(int)(hist.getTotalCounts()+0.5);
            if(nSum>nMax) nMax=nSum;
        }
        int len=PrintAssist.getDigits(nMax)+2;
        int len1=12;
        if(len<14) len=14;
        PrintAssist.printString(fp,"TotalCounts",len);
        PrintAssist.printString(fp,"Mean",len1);
        PrintAssist.printString(fp,"Sem",len1);
        nDim=hists.get(0).getSize();
        PrintAssist.endLine(fp);
        double dLog10=Math.log(10);
        int nMode;
        MeanSem0 meanSem;
        double count,sum;
        double dPosition;
        for(i=0;i<nMaxDim;i++){
            for(j=0;j<nSize;j++){
                dPosition=hists.get(j).getPosition(i);
                if(hists.get(j).getMode()==1) dPosition=Math.exp(dPosition*dLog10);
                PrintAssist.printNumber(fp,dPosition,nvPw.get(j),nvPp.get(j));
                count=hists.get(j).getCounts_printing(i);
                PrintAssist.printNumber(fp,count,nvCw.get(j),nvCp.get(j));
                sum=dvSums.get(j);
                sum+=count;
                PrintAssist.printNumber(fp,sum/hists.get(j).getTotalCounts(),nSw,nSp);
                dvSums.set(j, sum);
            }
            if(i<nSize){
                PrintAssist.printNumber(fp,hists.get(i).getTotalCounts(),len,0);
                meanSem=hists.get(i).getMeanSem();
                dMean=meanSem.mean;
                dSem=meanSem.sem;
                PrintAssist.printNumberScientific(fp,dMean,len1,4);
                PrintAssist.printNumberScientific(fp,dSem,len1,4);
            }
            PrintAssist.endLine(fp);
        }
    }

    public static void exportHistograms(Formatter fp, ArrayList <Histogram> hists, int nWS){
        int nSize=hists.size();
        int nIT=1;
        exportHistograms(fp,hists,nWS,nIT);
    }
    public static void exportHistogram(Formatter fp, Histogram hist){
        ArrayList <Histogram> hists=new ArrayList();
        hists.add(hist);
        exportHistograms(fp,hists);
    }
    public static void exportHistogram(Formatter fp, Histogram hist, int nWS){
        ArrayList <Histogram> hists=new ArrayList();
        hists.add(hist);
        exportHistograms(fp,hists,nWS);
    }
    public static void exportHistograms(Formatter fp, ArrayList <Histogram> hists0, int nWS, int nIT){
        ArrayList <Histogram> hists=hists0;
        int nSize=hists.size();
        int i,j;
        if(nIT*nWS>0){
            hists=new ArrayList();
            for(i=0;i<nSize;i++){
                hists.add(new Histogram (hists0.get(i)));
            }
        }
        for(i=0;i<nSize;i++){
            for(j=0;j<nIT;j++){
                hists.get(i).smoothHistogram(nWS);
            }
        }
        exportHistograms(fp,hists);
    }
}
