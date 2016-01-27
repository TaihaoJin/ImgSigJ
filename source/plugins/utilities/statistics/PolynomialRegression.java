/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import ij.IJ;
import ij.gui.Plot;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CommonStatisticsMethods;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import utilities.QuickSort;
import java.util.ArrayList;
import utilities.statistics.MeanSem1;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Gui.PlotWindowPlus;
import ij.gui.PlotWindow;
import java.awt.Color;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import utilities.CommonMethods;
import utilities.CustomDataTypes.intRange;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.statistics.LinearFittingFunction;
import utilities.statistics.GeneralLinearRegression;
import utilities.Non_LinearFitting.FittingComparison;
import utilities.Non_LinearFitting.ModePars;


/**
 *
 * @author Taihao
 */
public class PolynomialRegression extends SimpleRegression{
    int m_nOrder,m_nModes;
    int IndexI,IndexF,minLen,nDataSize;
    boolean[] m_pbSelected;
    boolean bValid;
    boolean bExcludePWOutliars;
    double[] m_pdSelectedX,m_pdSelectedY,pdPars;
    double[] m_pdDataX,m_pdDataY;//this is the entire data. Fitting could be a smaller x range within it. m_pdSelectedX and m_pdSelectedY is the data within the fitting range.
    double[] x;
    double m_dPValue,dOutliarRatio;
    double[] pdSD,pdSelectedSD;
    double m_dMeanY;
    double dChiSquare,dSigChiSQ;
    ComposedFittingFunction m_cFun;
    DoubleRange xDataRange,yDataRange,xFittingRange;
    FittingModelNode m_cModel,m_cOutliarExcludedModel;
    ArrayList<String> m_svFunctionTypes;
    ArrayList<Integer> m_nvSelectedIndexes;
    LinearFittingFunction m_cFunc;
    public PolynomialRegression(){
        x=new double[1];
        dOutliarRatio=0;
        bValid=false;
    };
    public PolynomialRegression(double[] pdX, double[] pdY, int order){
        this(pdX,pdY,order,1,0);
    }
    public PolynomialRegression(double[] pdX, double[] pdY, boolean[] pbSelected, int order, int start, int end, int delta){
        this();
        ArrayList<Integer> nvSelectedPositions=new ArrayList();
        
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSelected, new intRange(start, end), nvSelectedPositions);
        
        int len=nvSelectedPositions.size(),i;
        double[] pdXT=new double[len], pdYT=new double[len];
        int num=0,position;
        for(position=start;position<=end;position+=delta){
            if(!pbSelected[position]) continue;
            pdXT[num]=pdX[position];
            pdYT[num]=pdY[position];
            num++;
        }
        updateData(pdXT,pdYT,null,order,1,0);
        
    }
    public PolynomialRegression(double[] pdX, double[] pdY, boolean[] pbSelected, double[] pdSD, int order, int start, int end, int delta){
        this();
        ArrayList<Integer> nvSelectedPositions=new ArrayList();
        
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSelected, new intRange(start, end), nvSelectedPositions);
        
        int len=nvSelectedPositions.size(),i;
        double[] pdXT=new double[len], pdYT=new double[len],pdSDT=new double[len];
        int num=0,position;
        for(position=start;position<=end;position+=delta){
            if(!pbSelected[position]) continue;
            pdXT[num]=pdX[position];
            pdYT[num]=pdY[position];
            pdSDT[num]=pdSD[position];
            num++;
        }
        updateData(pdXT,pdYT,pdSDT,order,1,0.);
        
    }
    public PolynomialRegression(double[] pdX, double[] pdY, double[] pdSD,DoubleRange xRange,int order,int modes,boolean[] pbSelected,double dOutliarRatio){
        this();
        this.pdSD=pdSD;
        m_pdDataX=pdX;
        m_pdDataY=pdY;
        if(pdSD==null){
            pdSD=new double[m_pdDataX.length];
            CommonStatisticsMethods.setElements(pdSD, 1);
        }
        if(pbSelected==null){
            pbSelected=new boolean[m_pdDataX.length];
            CommonStatisticsMethods.setElements(pbSelected, true);
        }
        xFittingRange=xRange;
        double xI=xFittingRange.getMin(),xF=xFittingRange.getMax();
        m_pbSelected=pbSelected;
        minLen=order+2;
        bExcludePWOutliars=true;
        int i,len,index;
        double dx;
        IndexI=pdX.length;
        IndexF=0;
        m_nvSelectedIndexes=new ArrayList();
        for(i=0;i<pdX.length;i++){
            dx=pdX[i];
            if(dx<xI||dx>xF) continue;
            if(pbSelected!=null)
             if(!pbSelected[i]) continue;
            if(i<IndexI) IndexI=i;
            if(i>IndexF) IndexF=i;
            m_nvSelectedIndexes.add(i);
        }
        nDataSize=m_nvSelectedIndexes.size();
        double[] pdXT=new double[nDataSize],pdYT=new double[nDataSize];
        pdSelectedSD=new double[nDataSize];
        for(i=0;i<nDataSize;i++){
            index=m_nvSelectedIndexes.get(i);
            pdXT[i]=m_pdDataX[index];
            pdYT[i]=m_pdDataY[index];
            pdSelectedSD[i]=pdSD[index];
        }
        updateData(pdXT,pdYT,pdSelectedSD,order,modes,dOutliarRatio);
    }
    public PolynomialRegression(double[] pdX, double[] pdY, int order,int modes,double dOutliarRatio){
        this();
        updateData(pdX,pdY,null,order,modes,dOutliarRatio);
    };
    public int updateData(double[] pdX, double[] pdY, double[] pdSD, int order,int modes, double dOutliarRatio){//
        if(order<0) {
            buildOptimalRegression(pdX, pdY, pdSD, order, modes, dOutliarRatio);
        }else{
        
            int nMaxRisingInterval=2;
            this.dOutliarRatio=dOutliarRatio;
            int len=pdX.length;
            if(IndexI==IndexF){
                IndexI=0;
                IndexF=len-1;
            }
            m_pdSelectedX=pdX;
            m_svFunctionTypes=new ArrayList();
            m_svFunctionTypes.add("polynomial"+order);
            m_nModes=modes;

            m_pdSelectedY=CommonStatisticsMethods.copyArray(pdY);
            pdSelectedSD=pdSD;
            m_nOrder=order;
            int i;
            len=m_pdSelectedY.length;
            m_dMeanY=0;
            for(i=0;i<len;i++){
                m_dMeanY+=m_pdSelectedY[i];
            }
            m_dMeanY/=len;
            nDataSize=pdX.length;
            xFittingRange=CommonStatisticsMethods.getRange(pdX);
            if(nDataSize<3) {
                pdPars=new double[m_nOrder+1];
                CommonStatisticsMethods.setElements(pdPars, 0);
                pdPars[0]=m_dMeanY;
                if(pdSD==null){
                    pdSelectedSD=new double[nDataSize];
                    CommonStatisticsMethods.setElements(pdSelectedSD, 1);
                }
                dChiSquare=Double.POSITIVE_INFINITY;
                dSigChiSQ=0;
                return -1;
            }
            
            
            pdPars=getDefaultPolynomialPars(pdX,pdY,order);
            if(pdSD==null){
/*                if(nDataSize>nMaxRisingInterval+3)
                    pdSelectedSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdY, nMaxRisingInterval, 20);
                else {
                    pdSelectedSD=new double[nDataSize];
                    CommonStatisticsMethods.setElements(pdSelectedSD, CommonStatisticsMethods.buildMeanSem(pdY).getSD());
                }*/                    
                    pdSelectedSD=new double[nDataSize];
                    CommonStatisticsMethods.setElements(pdSelectedSD, 1);
            }
/*            if(m_nModes==1)
                completeRegression_NR();
            else
                completeRegression();*/
            completeRegression_NR();
            if(!bValid) {
                if(m_nModes==1)return -1;
                pdPars=getDefaultPolynomialPars(pdX,pdY,m_nOrder);
            }
            
            if(m_nModes!=1) completeRegression();
        }
        return 1;
    }
    public double getMeanY(){
        return m_dMeanY;
    }
    public static double[] getDefaultPolynomialPars(double[] pdX, double[] pdY, int order){
        int i,nPars=order+1,len=pdX.length;
        double[] pdPars=new double[nPars];
        double yi=0,yf=0,xi=0,xf=0;
        int ii=len-len/3,it;
        double num=0;
        for(i=0;i<len/3;i++){
            it=ii+i;
            yi+=pdY[i];
            xi+=pdX[i];
            xf+=pdX[it];
            yf+=pdY[it];
            num+=1;
        }
        yf/=num;
        yi/=num;
        xi/=num;
        xf/=num;
        double k=(yf-yi)/(xf-xi);
        pdPars[0]=yi-k*xi;
        pdPars[1]=k;
        
        double y=yi;
        double delta;
        for(i=2;i<order+1;i++){
            if(ii>=len) break;
            delta=pdY[ii]-y;
            pdPars[i]=delta/Math.pow(xi, i);
            y+=delta;
            ii++;
        }
        return pdPars;
    }
    int completeRegression(){
        if(nDataSize<minLen) return -1;
        int i,len=m_pdSelectedX.length;
        double[][] pdX=new double[len][1];
        double[] pdYT=new double[len];
        for(i=0;i<len;i++){
            pdX[i][0]=m_pdSelectedX[i];
            pdYT[i]=m_cFunc.fun(pdPars,m_pdSelectedX[i])-m_pdSelectedY[i];
        }
        m_cModel=new FittingModelNode(pdX,m_pdSelectedY);//this part should have been taken care of by completeRegression_NR()
        m_cModel.setSD(pdSelectedSD);
        m_cModel.pdPars=pdPars;
        m_cModel.setNumOutliars((int)(m_pdSelectedX.length*dOutliarRatio+0.5));
        m_cModel.setFunctionTypes(m_svFunctionTypes,m_nModes);
        
        ModePars cMP=new ModePars(m_nModes);
        MeanSem1 ms=new MeanSem1();
        CommonStatisticsMethods.findOutliars(m_pdSelectedY, 0.01, ms, null);
        double sd=ms.getSD()*Math.sqrt(2);
        for(i=0;i<m_nModes;i++){
            cMP.pdSDs[i]=sd;
            cMP.pbFixMeans[i]=false;
            cMP.pbFixSDs[i]=true;
            cMP.pbFixWeights[i]=false;
        }
        if(m_nModes>1) cMP.setDefaultPars(pdYT);
        m_cModel.cModelPars=cMP;
        if(m_nModes>1) m_cModel.MinimizationOption=Non_Linear_Fitter.Maximum_Likelihood;
        
        Non_Linear_Fitter.getFittedModel_Simplex(m_cModel, m_cModel, 0.5, null);
        m_cFun=new ComposedFittingFunction(m_svFunctionTypes);
        bValid=true;
        dChiSquare=m_cModel.getSignificance_ChiSquare();
        dSigChiSQ=m_cModel.getSignificance_ChiSquare();
        return 1;
    }
    public boolean isValid(){
        return bValid;
    }
    int completeRegression_NR(){
        if(nDataSize<minLen) return -1;
        ArrayList<String> svTypes=new ArrayList();
        for(int i=0;i<=m_nOrder;i++){
            svTypes.add("pow"+i);
        }
        m_cFunc=new LinearFittingFunction(svTypes);
        int nPars=m_nOrder+1;
        double[][] u=new double[nDataSize][nPars],v=new double[nDataSize][nPars];
        double[] w=new double[nPars];
        double chisq=GeneralLinearRegression.svdfit(m_pdSelectedX, m_pdSelectedY, pdSelectedSD, pdPars, u, v, w, m_cFunc);
        if(Double.isNaN(chisq)) return -1;
           
        bValid=true;

        int num=m_pdSelectedX.length;
        int nu=num-nPars;
        if(nu<=0) {
            IJ.error("the number of data need to be bigger than nPars+1:"+(m_nOrder+2));
            return 1;
        }
        ChiSquaredDistributionImpl dist=new ChiSquaredDistributionImpl(nu);
        double sig=-1;
        try {
            sig=1-dist.cumulativeProbability(chisq);
        }
        catch (org.apache.commons.math.MathException e){

        }
        dChiSquare=chisq;
        dSigChiSQ=sig;
        
        return 1;
    }
    public double predict(double x0){
        if(!bValid) 
            return m_dMeanY;
        if(m_nModes!=1){
            x[0]=x0;
            FittingModelNode model=m_cModel;
//            return model.getFittedValue(x);
            return m_cFun.fun(m_cModel.pdPars, x);
        }
        return m_cFunc.fun(pdPars, x0);
    }
    public double[] getFittedDataX(){
        return m_pdSelectedX;
    }
    public double[] getFittedDataY(){
        return m_pdSelectedY;
    }
    public double[] getFittedDataSD(){
        return pdSelectedSD;
    }
    public double getSignificance_ChiSquare(){
        if(m_nModes>1)
            return m_cModel.getSignificance_ChiSquare();
        else
            return dSigChiSQ;
    }
    public double getChiSquare(){
        if(m_nModes>1)
            return m_cModel.SSE;
        else
            return dChiSquare;
    }
    public ModePars getModePars(){
        return m_cModel.cModelPars;
    }
    public int getModes(){
        return m_nModes;
    }
    public FittingModelNode getModel(){
        return m_cModel;
    }
    public double[] getDataX(){
        return m_pdSelectedX;
    }
    public double[] getDataY(){
        return m_pdSelectedY;
    }
    public double[] getDataSD(){
        return pdSD;
    }
    public boolean[] getSelection(){
        return m_pbSelected;
    }
    public intRange getFittedRange(){
        return new intRange(IndexI,IndexF);
    }
    public ArrayList<Integer> getSelectedIndexes(){
        return m_nvSelectedIndexes;
    }
    public double getSSE(){
        int i,len=m_pdSelectedX.length;
        double sse=0,x,dy;
        for(i=0;i<len;i++){
            x=m_pdSelectedX[i];
            dy=m_pdSelectedY[i]-predict(x);
            sse+=dy*dy;
        }
        return sse;
    }
    public int buildOptimalRegression(double[] pdX, double[] pdY, double[] pdSD, int order,int modes, double dOutliarRatio){
        order=1;
        nDataSize=pdX.length;
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        int len=nvLn.size()+nvLx.size();
        if(nvLx.size()>0)
            if(nvLx.get(0)==0) len--;//excluding the first local maximum if it is the first element
        int nMaxOrder=Math.min(len/2, 3);
        nMaxOrder=Math.min(nMaxOrder,pdX.length-2);
        if(nMaxOrder>nDataSize-2) nMaxOrder=nDataSize-2;
        updateData(pdX, pdY, pdSD, order,modes, dOutliarRatio);
        double sigChiSQ0=dSigChiSQ,maxSig=dSigChiSQ;
        if(!isValid()) return -1;
        double sse0=getSSE(),sse,df0=order+modes,df,dPValue=0;
        double cutoff=0.0001;
        int nOptOrder=1;
        while(order<nMaxOrder){
            order++;
            updateData(pdX, pdY, pdSD, order,modes, dOutliarRatio);
            sse=getSSE();
            df=order+modes;
            dPValue=FittingComparison.getFittingComparison_LeastSquare(sse0, df0, sse, df, nDataSize);
            if(dSigChiSQ>maxSig){
                nOptOrder=order;
                maxSig=dSigChiSQ;
            }
/*            if(dPValue<cutoff) {
                nOptOrder=order;
                sse0=sse;
                df0=df;
            }*/
        }
        updateData(pdX, pdY, pdSD, nOptOrder,modes, dOutliarRatio);
        return 1;
    }
    public void showRWASignificance(){
        int ws=0;
        int lExt=5+2*ws;
        int i,len=m_pdSelectedX.length,len2=m_pdDataX.length;
        if(lExt>len) lExt=len;
        double[] pdY=new double[len], pdX=new double[len],pdXn=new double[lExt],pdYn=new double[lExt],pdXp=new double[lExt],pdYp=new double[lExt];
        for(i=0;i<len;i++){
            pdX[i]=m_pdSelectedX[i];
            pdY[i]=m_pdSelectedY[i]-predict(m_pdSelectedX[i]);
        }
        int iI=Math.max(0, IndexI-(lExt-ws)),iF=Math.min(iI+lExt-1, len2-1);
        double x,y;
        for(i=iI;i<=iF;i++){
            x=m_pdDataX[i];
            y=m_pdDataY[i];
            pdXn[i-iI]=x;
            pdYn[i-iI]=y-predict(x);
        }
        iF=Math.min(IndexF+(lExt-ws), len2-1);
        iI=Math.max(0, iF-lExt+1);
        for(i=iI;i<=iF;i++){
            x=m_pdDataX[i];
            y=m_pdDataY[i];
            pdXp[i-iI]=x;
            pdYp[i-iI]=y-predict(x);
        }
            pdY=CommonStatisticsMethods.getRWLogDevP(pdY,ws,0,pdSD);
            pdYn=CommonStatisticsMethods.getRWLogDevP(pdYn,ws,0,pdSD);
            pdYp=CommonStatisticsMethods.getRWLogDevP(pdYp,ws,0,pdSD);

        PlotWindowPlus pwp=new PlotWindowPlus(pdX,pdY,"Log p WS="+ws,"X","Log10(p)",2,PlotWindow.CROSS,Color.RED);
        double[] pdY05=new double[len],pdY01=new double[len],pdY001=new double[len],pdY0001=new double[len];
        CommonStatisticsMethods.setElements(pdY05, Math.log10(0.05));
        CommonStatisticsMethods.setElements(pdY01, Math.log10(0.01));
        CommonStatisticsMethods.setElements(pdY001, Math.log10(0.001));
        CommonStatisticsMethods.setElements(pdY0001, Math.log10(0.0001));
//        pwp.addPlot("", pdXn, pdYn, 2, PlotWindow.LINE, Color.RED);
//        pwp.addPlot("", pdXp, pdYp, 2, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY05, 1, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY01, 1, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY001, 1, PlotWindow.LINE, Color.BLACK);
        pwp.addPlot("", pdX, pdY0001, 1, PlotWindow.LINE, Color.BLACK);
    }
    int getDataSize(){
        return nDataSize;
    }
    public DoubleRange getXFittingRange(){
        return xFittingRange;
    }
    public boolean consistentTrend(double x){
        if(!xFittingRange.contains(x)) return false;
        int i=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(m_pdSelectedX, x, m_pdDataX[1]-m_pdDataX[0]),len=m_pdSelectedX.length;
        int iF=i+1;
        if(iF>=len) iF=i-1;
        return (m_pdSelectedY[iF]-m_pdSelectedY[i])*(predict(m_pdSelectedX[iF])-predict(m_pdSelectedX[i]))>=0;
    }
    public ArrayList<Double> getPredictionDeviations(){
        ArrayList<Double> dvDev=new ArrayList();
        for(int i=0;i<m_pdSelectedX.length;i++){
            dvDev.add(m_pdSelectedY[i]-predict(m_pdSelectedX[i]));
        }
        return dvDev;
    }
    public static boolean isBetterThan(PolynomialRegression cPrA, PolynomialRegression cPrB){
        if(cPrA==null) return false;
        if(cPrB==null) return true;
        if(!cPrA.isValid()){
            if(cPrB.isValid()) return false;
            return cPrA.nDataSize>cPrB.nDataSize;
        }
        if(!cPrB.isValid()) return true;
        return cPrA.dSigChiSQ>cPrB.dSigChiSQ;
    }
    public double[] getFittedParameters(){
        return pdPars;
    }
}
