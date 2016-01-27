/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.io.PrintAssist;
import utilities.statistics.MeanSem0;
import ij.IJ;
import utilities.CommonStatisticsMethods;
import utilities.io.PrintAssist;
import utilities.CustomDataTypes.DoubleRange;

/**
 *
 * @author Taihao
 */
public class Histogram {

	String m_sPositionTitle, m_sCountTitle;
	double m_dBase,m_dDelta,m_pdHist[],m_dSum,m_dMaxCount,m_dSum2,m_dCounts;
	double m_dBase0, m_dDelta0,dMin;//The original base and scale values, reset only by the function update.
	int m_nDim, m_nDim0;
        int m_nMaxBins;
        double m_dSD;
	int m_nWs; //the window size for smoothing the histogram. the counts in the i-th bin is the average of the bins from (i-m_nWs) to (i+m_nWs).
	int m_nMode;//0 for linear and 1 for logarithmic. The defaul mode is 0. It's only set by the function update.
				//the parameters such m_dBase, m_dDelta are transformed into log scale by the function that calls update(...)
				//The only functions that differentiate the different modes are addData(...) functions.
				//It is the responsibility of the functions that uses the histograms to interpret the positions of the
				//bin boundaries according to the mode of the histgram.
    double m_dLog10;
    double m_dPercentile;
    int m_nPercentilePosition;
    int m_nLowers;
    int m_nExtraBins;
    boolean m_bNormalizeCountsOutput;
    double m_dMin,m_dMax,m_dMinDelta;
    public Histogram()
    {
        m_pdHist=new double[1];
        m_nMaxBins=100;
        m_nMode=0;
        m_dSum=0.;
        m_dCounts=0.;
        m_dSum2=0.;
        m_nWs=-1; //the window size for smoothing the histogram. the counts in the i-th bin is the average of the bins from (i-m_nWs) to (i+m_nWs).
        m_dLog10=Math.log(10);
        m_dPercentile=0.5;
        m_nPercentilePosition=m_nDim/2;
        m_nLowers=0;
        m_nExtraBins=2;
        m_bNormalizeCountsOutput=false;
        m_nDim=0;
        m_sPositionTitle="value";
        m_sCountTitle="counts";
    }
    public void setMaxBinNum(int nMaxBins){
        m_nMaxBins=nMaxBins;
    }
    public Histogram(Histogram hist){
        m_pdHist=new double[hist.m_pdHist.length];
        m_nMode=hist.m_nMode;
        m_dSum=hist.m_dSum;
        m_dCounts=hist.m_dCounts;
        m_dSum2=hist.m_dSum2;
        m_nWs=hist.m_nWs; //the window size for smoothing the histogram. the counts in the i-th bin is the average of the bins from (i-m_nWs) to (i+m_nWs).
        m_dLog10=Math.log(10);
        m_dPercentile=hist.m_dPercentile;
        m_nPercentilePosition=hist.m_nPercentilePosition;
        m_nLowers=hist.m_nLowers;
        m_bNormalizeCountsOutput=hist.m_bNormalizeCountsOutput;
        m_nDim=hist.m_nDim;
        m_nExtraBins=hist.m_nExtraBins;
        m_nDim0=hist.m_nDim0;
        for(int i=0;i<m_nDim;i++){
            m_pdHist[i]=hist.m_pdHist[i];
        }
        m_dBase=hist.m_dBase;
        m_dBase0=hist.m_dBase0;
        m_dDelta=hist.m_dDelta;
        m_dDelta0=hist.m_dDelta0;
        m_dPercentile=hist.m_dPercentile;
        m_sPositionTitle=new String(hist.m_sPositionTitle);
        m_sCountTitle=new String(hist.m_sCountTitle);
        m_nMaxBins=hist.m_nMaxBins;
    }

    public Histogram(double[] pdData){
        this();
        ArrayList<Double> dvData=new ArrayList();
        CommonStatisticsMethods.copyArray(pdData,dvData);
        setData(dvData);
    }
    public Histogram(ArrayList<Double> dvData){
        this();
        setData(dvData);
    }
    public void setData(ArrayList<Double> dvData){
        if(dvData.size()==0) {
            IJ.error("Can't build the histogram of an empty array");
        }else{
            double epsilon=CommonMethods.MachineEpsilonFloat();
            double[] pdRange=new double[2];
            CommonMethods.getValueRange(dvData, pdRange);
            double dn=pdRange[0], dx=pdRange[1];
            int nMaxBins=m_nMaxBins;
//            int nMaxBins=Math.min(m_nMaxBins, dvData.size()/2);
            double dr=this.getOptimalBinSize(dvData, nMaxBins);
            if(dr<epsilon) {
                dr=Math.abs(dn);
            }//one effective bin histogram
            update(dn,dx,dr);
            addData(dvData);
        }
    }

    public void addData(ArrayList<Double> dvData){
        int len=dvData.size();
        for(int i=0;i<len;i++){
            addData(dvData.get(i));
        }
    }

    public void normalizeCountsOutput(){
        m_bNormalizeCountsOutput=true;
    }

    public void setPercentile(double per){
        m_dPercentile=per;
        updatePercentilePosition();
    }

    void resetPercentilePosition(){
        m_nLowers=0;
        m_nPercentilePosition=0;
//        updatePercentilePosition();
    }

    public int updatePercentilePosition(){
        if(m_dCounts<1) {
            m_nLowers=0;
            m_nPercentilePosition=(int)(m_dPercentile*m_nDim);
            return -1;
        }
        int index=m_nPercentilePosition;
        int lowers=m_nLowers;
        m_nLowers=(int) (m_dPercentile*m_dCounts);
        int order=0;
        if(lowers>m_nLowers) order=1;
        double dc=m_pdHist[index];

        switch(order){
            case 0:
                lowers+=dc;
                while(lowers<=m_nLowers){
                    index++;
                    dc=m_pdHist[index];
                    lowers+=dc;
                }
                break;
            case 1:
                while(lowers>m_nLowers){
                    index--;
                    if(index<0){
                        index=index;
                    }
                    dc=m_pdHist[index];
                    lowers-=dc;
                }
                break;
        }

        m_nPercentilePosition=index;
        if(order==0){
            m_nLowers=(int)(lowers-dc+0.5);
        }else{
            m_nLowers=(int)(lowers+0.5);
        }
        return 1;
    }

    public double getPercentileValue(){
        updatePercentilePosition();
        return getValue(m_nPercentilePosition);
    }

    public double getValue(int position){
        return m_dBase+m_dDelta*position;
    }

    public int removeData(double dv){
        if(m_nMode==1) dv=Math.log(dv)/m_dLog10;
        int index=getIndex(dv);
        if(index>=0){
            m_pdHist[index]-=1.;
            m_dCounts-=1.;
            m_dSum-=dv;
            m_dSum2-=dv*dv;
        }
        if(index<m_nPercentilePosition){
            m_nLowers-=1;
        }
        if(m_pdHist[index]<0) return -1;
        return 1;
    }

    public void update(int nMode, double dMin, double dMax, double dDelta){
        m_nMode=nMode;
        update(dMin,dMax,dDelta);
    }
    public double getMax(){
        return m_dMax;
    }
    public void update(double dMin, double dMax, double dDelta){
        m_dDelta=dDelta;
        m_dMax=dMax;
        m_dMin=dMin;
        double small=1/Integer.MAX_VALUE;
        if(dDelta<small)
            m_dBase=dMin;
        else
            m_dBase=((int)(dMin/dDelta))*dDelta;
        if(m_dBase>dMin) m_dBase-=dDelta;
        m_dBase-=m_nExtraBins*dDelta;
        m_nDim=(int)((dMax-m_dBase)/dDelta)+1+2*m_nExtraBins;
        if(m_nDim<0){
            m_nDim=m_nDim;
        }
        m_pdHist=new double[m_nDim];
        for(int i=0;i<m_nDim;i++)
        {
            m_pdHist[i]=0.;
        }
        m_dSum=0.;
        m_dCounts=0.;
        m_dSum2=0.;
        m_nWs=-1;
        m_dBase0=m_dBase;
        m_dDelta0=m_dDelta;
        m_nDim0=m_nDim;
    }

    int getIndex(double dValue){
        int index=(int)((dValue-m_dBase)/m_dDelta);
        if(index>=m_nDim||index<0) index=-1;
        return index;
    }

    public void addData(double dValue){
        double dV=dValue;
        if(m_nMode==1) dV=Math.log(dV)/m_dLog10;
        int index=getIndex(dV);
        if(index>=0){
            m_pdHist[index]+=1.;
            m_dCounts+=1.;
            m_dSum+=dValue;
            m_dSum2+=dValue*dValue;
            if(index<m_nPercentilePosition){
                m_nLowers+=1;
            }
        }else{
//            IJ.error("added data exceeded the range. The histogram as been expaned");
//            IJ.showMessage("added data exceeded the range. The histogram as been expaned");
            expandRange(dValue);
            addData(dValue);
        }
    }


    public double getTotalCounts(){
        return m_dCounts;
    }

    public void addData(double dValue, double num){
        double dV=dValue;
        if(m_nMode==1) dV=Math.log(dV)/m_dLog10;
        int index=getIndex(dV);
        if(index>=0){
            m_pdHist[index]+=num;
            m_dCounts+=num;
            m_dSum+=num*dValue;
            m_dSum2+=num*dValue*dValue;
        }
        if(index<m_nPercentilePosition){
            m_nLowers+=num;
        }
    }
    void addData(double pdData[], int firstIndex, int delta, int num){
        int index,i;
        double dV;
        for(i=0;i<num;i++)
        {
            index=firstIndex+i*delta;
            dV=pdData[index];
            addData(dV);
        }
    }
    void addData(float pdData[], int firstIndex, int delta, int num){
        int index;
        double dV;
        for(int i=0;i<num;i++)
        {
            index=firstIndex+i*delta;
            dV=pdData[index];
            addData(dV);
        }
    }
    public double getBase(){
        return m_dBase;
    }
    int getSize(){
        return m_nDim;
    }
    public void smoothHistogram(int nWs){
        m_nWs=nWs;
        int i,j,n,ji,jf;
        double pdHist[]=new double[m_nDim];
        for(i=0;i<m_nDim;i++){
            ji=i-m_nWs;
            jf=i+m_nWs;
            n=0;
            pdHist[i]=0.;
            for(j=ji;j<=jf;j++){
                if(j>=0&&j<m_nDim){
                    n++;
                    pdHist[i]+=m_pdHist[j];
                }
            }
            pdHist[i]/=n;
        }
        m_pdHist=pdHist;
    }
    public double getPosition(int index){
        return m_dBase+index*m_dDelta;
    }
    public int getCounts(int index){
        int nCount=0;
        if(index>=0&&index<m_nDim) nCount=(int)(m_pdHist[index]+0.5);
        return nCount;
    }
    double getCounts_printing(int index){
        double dCount=0.;
        if(index>=0&&index<m_nDim) dCount=m_pdHist[index];
        if(m_bNormalizeCountsOutput) dCount/=m_dCounts;
        return dCount;
    }
    void copyHist(Histogram aHist){
        m_dBase=aHist.getBase();
        m_dDelta=aHist.getDelta();
        m_nDim=aHist.getDim();
        m_dCounts=aHist.getTotalCounts();
        m_pdHist=new double[m_nDim];
        for(int i=0;i<m_nDim;i++){
            m_pdHist[i]=aHist.getCounts(i);
        }
        m_dBase0=aHist.getBase0();
        m_dDelta0=aHist.getDelta0();
        m_nDim0=aHist.getDim0();
        m_dSum=aHist.getSum();
        m_dSum2=aHist.getSum2();
        m_sPositionTitle=aHist.getPositionTitle();
        m_sCountTitle=aHist.getCountTitle();
        m_dMaxCount=aHist.getMaxCount();
        m_nWs=aHist.getWS();
        m_nDim0=aHist.getDim0();
        m_dDelta0=aHist.getDelta0();
        m_dBase0=aHist.getBase0();
        m_nPercentilePosition=aHist.m_nPercentilePosition;
        m_nLowers=aHist.m_nLowers;
        m_dPercentile=aHist.m_dPercentile;
    }
    void shiftting(double shift){//(verified on 8/7/09)
        m_dBase+=shift;
    }
    void scaling(double scale){//(verified on 8/7/09)
        m_dBase*=scale;
        m_dDelta*=scale;
    }
    void resetBinPosition(int index, double position){//(verified on 8/7/09)
        double dShift=position-getPosition(index);
        shiftting(dShift);
    }
    void resetDelta(double delta){//(verified on 8/7/09)
        double scale=delta/m_dDelta;
        scaling(scale);
    }

    void resample(double dDelta){
        Histogram newHist=new Histogram();
        double dmin=m_dBase;
        double dmax=m_dBase+m_nDim*m_dDelta;
        double x1,x2,count;
        newHist.update(dmin,dmax,dDelta);
        for(int i=0;i<m_nDim;i++){
            count=m_pdHist[i];
            x1=getPosition(i);
            x2=getPosition(i+1);
            newHist.addData(x1,x2,count);
        }
        copyHist(newHist);
        resetPercentilePosition();
    }

    void addData(double xi, double xf, double num){
        //this function is exclusively used for operations on the histograms.
        //m_dSum and m_dSum2 will be adjusted independently, rather then adjusting
        //within this function.
        int i1=getIndex(xi);
        if(i1<0) i1=0;
        int i2=getIndex(xf);
        if(i2>=m_nDim) i2=m_nDim-1;
        double p,x0,x1;
        for(int i=i1;i<=i2;i++){
            x0=getPosition(i);
            x1=x0+m_dDelta;
            if(xi>x0) x0=xi;
            if(xf<x1) x1=xf;
            p=(x1-x0)/(xf-xi);
            m_pdHist[i]+=p*num;
            m_dCounts+=p*num;
        }
    }
    double getCounts(double xi, double xf){//return the integration of the pdf from xi to xf
        int i1=getIndex(xi);
        int i2=getIndex(xf);
        double p,x0,x1,counts;
        counts=0.;
        for(int i=i1;i<=i2;i++){
            x0=getPosition(i);
            x1=x0+m_dDelta;
            if(xi>x0) x0=xi;
            if(xf<x1) x1=xf;
            p=(x1-x0)/m_dDelta;
            counts+=p*m_pdHist[i];
        }
        return counts;
    }
    void expandRange0(double x){//expand the range of the histogram to accomodate the position x, implemented bellow differently.
        Histogram newHist=new Histogram();
        double dMin=m_dBase+0.5*m_dDelta;
        double dMax=m_dBase+(m_nDim-1)*m_dDelta;
        if(x<dMin) dMin=x;
        if(x>dMax) dMax=x;
        newHist.update(dMin,dMax,m_dDelta);
        for(int i=0;i<m_nDim;i++){
            x=m_dBase+i*m_dDelta;
            newHist.addData(x,x+m_dDelta,m_pdHist[i]);
        }
        copyHist(newHist);
    }
    public double getMin(){
        return m_dBase;
    }
    double getMaxBin(){
        return m_dBase+(m_nDim-1)*m_dDelta;
    }
    public double getDelta(){
        return m_dDelta;
    }
    void mergeHist(Histogram hist){
        double dMin=hist.getMin();
        double dMax=hist.getMax();
        if(dMin>m_dBase) dMin=m_dBase;
        if(dMax<getMax()) dMax=getMax();
        Histogram newHist=new Histogram();
        newHist.update(dMin,dMax,m_dDelta);
        int i,size;
        double xi,xf;
        for(i=0;i<m_nDim;i++){
            xi=getPosition(i);
            if(i<m_nDim-1)
                xf=getPosition(i+1);
            else
                xf=xi+m_dDelta;

            newHist.addData(xi,xf,m_pdHist[i]);
        }
        size=hist.getSize();
        double delta=hist.getDelta();
        for(i=0;i<size;i++){

            xi=hist.getPosition(i);
            if(i<m_nDim-1)
                xf=hist.getPosition(i+1);
            else
                xf=xi+hist.m_dDelta;

            newHist.addData(xi,xf,hist.getCounts(i));
        }
        newHist.updateSums(m_dSum+hist.getSum(),m_dSum2+hist.getSum2());
        copyHist(newHist);
    }

    void transformCoordinate(double shift, double scalingFactor)
    {//shiftting first then scaling. This is order dependent.
        shiftting(shift);
        scaling(scalingFactor);
    }

    double getScaling(){
        return m_dDelta/m_dDelta0;
    }
    public double getShift(){//cumulative effect of all coordinate transforms represented by shiftting and scaling (the same order) from the
     //original coordinate system(m_dBase0 and m_dDelta);
        double scaling=m_dDelta/m_dDelta0;
        return m_dBase/scaling-m_dBase0;
    }

    public void appendHist(Histogram hist)
    {
        int nSize=hist.getSize();
        double delta=hist.getDelta();
        double xi,xf;

        for(int i=0;i<nSize;i++){
            xi=hist.getPosition(i);
            xf=xi+delta;
            addData(xi,xf,hist.getCounts(i));
        }
        m_dSum+=hist.getSum();
        m_dSum2+=hist.getSum2();
    }

    public void clearHist()
    {
        for(int i=0;i<m_nDim;i++){
            m_pdHist[i]=0.;
        }
        m_dSum=0.;
        m_dCounts=0.;
        m_dSum2=0.;
        m_nPercentilePosition=m_nDim/2;
        m_nLowers=0;
    }
    public double getMaxCount(){
        double maxCount=0.;
        for(int i=0;i<m_nDim;i++){
            if(m_pdHist[i]>maxCount) maxCount=m_pdHist[i];
        }
        return maxCount;
    }
    public void setTitles(String sPositionTitle, String sCountTitle){
            m_sPositionTitle=sPositionTitle;
            m_sCountTitle=sCountTitle;
    }
    public String getPositionTitle(){
        return m_sPositionTitle;
    }
    public String getCountTitle(){
        return m_sCountTitle;
    }
    public int getPositionPrintingLength(){
        int len=m_sPositionTitle.length();
        int nPrecision=PrintAssist.getPrintingPrecisionF(m_dDelta,10);
        int len1=PrintAssist.getPrintingLengthF(m_dBase+m_nDim*m_dDelta,nPrecision);
        if(len1>len) len=len1;
        int len2=PrintAssist.getPrintingLengthF(m_dBase,nPrecision);
        if(len2>len) len=len2;
        return len;
    }
    public int getCountPrintingLength(){
        int len=m_sCountTitle.length(),nPrecision,len1;
        if(m_bNormalizeCountsOutput){
            nPrecision=PrintAssist.getPrintingPrecisionF(1./m_dCounts, 8);
            len1=PrintAssist.getPrintingLengthF(1,nPrecision);
        }else{
            nPrecision=0;
            len1=PrintAssist.getPrintingLengthF(m_dCounts,nPrecision);
        }
        if(len1>len) len=len1;
        return len;
    }
    public int getPositionPrecision(){
        int nMaxPrecision=8;
        int nPrecision=PrintAssist.getPrintingPrecisionF(m_dDelta,nMaxPrecision);
        return nPrecision;
    }

    public int getCountPrecision(){
        int nPrecision=0;
        if(m_bNormalizeCountsOutput){
            double dv=1./m_dCounts;
            dv=CommonMethods.log10(dv);
            nPrecision=Math.abs((int)dv)+2;
        }
        return nPrecision;
    }

    public double getSum(){
        return m_dSum;
    }

    public double getSum2(){
        return m_dSum2;
    }

    public MeanSem0 getMeanSem(){
        double dMean,dSem;
        int nSize=(int)(m_dCounts+0.5);
        dMean=m_dSum/m_dCounts;
        double dSD=Math.sqrt(m_dSum2/m_dCounts-dMean*dMean);
        m_dSD=dSD;
        dSem=0.;
        if(nSize>1) dSem=dSD/Math.sqrt(m_dCounts-1);
        return new MeanSem0(nSize,dMean,dSem);
    }
    public MeanSem1 getMeanSem1(){
        double dMean,dSem;
        int nSize=(int)(m_dCounts+0.5);
        dMean=m_dSum/m_dCounts;
        double dSD=Math.sqrt(m_dSum2/m_dCounts-dMean*dMean);
        m_dSD=dSD;
        dSem=0.;
        if(nSize>1) dSem=dSD/Math.sqrt(m_dCounts-1);
        DoubleRange dr=getCountRange();
        return new MeanSem1(nSize,dMean,dSem,dr.getMin(),dr.getMax());
    }
    public DoubleRange getCountRange(){
        DoubleRange dr=new DoubleRange();
        int len=m_pdHist.length,i;
        for(i=0;i<len;i++){
            dr.expandRange(m_pdHist[i]);
        }
        return dr;
    }
    public double getMean(){
        double dMean=m_dSum/m_dCounts;
        return dMean;
    }
    public int getWS(){
        return m_nWs;
    }
    public double getDelta0(){
        return m_dDelta0;
    }
    public double getBase0(){
        return m_dBase0;
    }
    int getDim0(){
        return m_nDim0;
    }
    public int getDim(){
        return m_nDim;
    }
    void updateSums(double dSum, double dSum2){
        m_dSum=dSum;
        m_dSum2=dSum2;
    }
    int getMode(){
        return m_nMode;
    }
    public void scale(double r){
        for(int i=0;i<m_nDim;i++){
            m_pdHist[i]*=r;
        }
    }
    public void expandRange(double dV){
        if(dV<m_dBase) loweringBase(dV);
        if(dV>getMax()){
            addBins(1+(int)((dV-getMax())/m_dDelta));
        }
    }
    void addBins(int nBins){
        int nDim=m_nDim+nBins+m_nExtraBins;
        double[] pd=new double[nDim];
        for(int i=0;i<m_nDim;i++){
            pd[i]=m_pdHist[i];
        }
        m_nDim=nDim;
        m_pdHist=pd;
    }
    void loweringBase(double dV){
        int nBins=(int)((m_dBase-dV)/m_dDelta)+1+m_nExtraBins;
        int nDim=m_nDim+nBins;
        double[] pd=new double[nDim];
        for(int i=0;i<m_nDim;i++){
            pd[nBins+i]=m_pdHist[i];
        }
        m_nDim=nDim;
        m_pdHist=pd;
        m_dBase-=nBins*m_dDelta;
        m_nPercentilePosition+=nBins;
    }
    static public double getOptimalBinSize(ArrayList<Double> dvData, int nMaxBins){
        int i,len=dvData.size();
        double[] pdData=new double[len];
        for(i=0;i<len;i++){
            pdData[i]=dvData.get(i);
        }
        return getOptimalBinSize(pdData,nMaxBins);
    }
    static public double getOptimalBinSize(double[] pdData, int nMaxBins){//this is for the cases when the data are highly clustered.
        double epsilon=CommonMethods.MachineEpsilonFloat();
        int i,len=pdData.length;
        double[] pdT=new double[len];
        double d,d0,delta,dn,dx,range;
        CommonStatisticsMethods.copyArray(pdData, pdT);
        utilities.QuickSort.quicksort(pdT);
        d0=pdT[0];
        range=pdT[len-1]-d0;//data Range
        dn=range/nMaxBins;
        dx=Double.POSITIVE_INFINITY;
        for(i=1;i<len;i++){
            d=pdT[i];
            delta=d-d0;
            if(delta>=dn&&delta<dx){
                dx=delta;
            }
            d0=d;
        }
        if(dx>range) dx=dn;//dn was already big;
        return dx;
    }
    public String getDataRangeAsString(int precision){
        String st=PrintAssist.ToString(m_dMin,precision)+" to "+PrintAssist.ToString(m_dMax,precision);
        return st;
    }
    public double getSD(){
        return m_dSD;
    }
    
}
