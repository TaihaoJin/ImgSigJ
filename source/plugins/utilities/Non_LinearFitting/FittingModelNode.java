/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.Constrains.ConstraintNode;
import utilities.Geometry.ImageShapes.ImageShape;
import java.awt.Point;
import utilities.CustomDataTypes.intRange;
import utilities.CommonMethods;
import ij.IJ;
import org.apache.commons.math.MathException;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.MeanSem0;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class FittingModelNode {
    public static final int CentralMode=101;
    public ArrayList<String> svFunctionTypes;
    public String sTitle;
    public String sHistFunctionType;
    public int[] pnFixedParIndexes;
    public double[] pdPars;
    public double[] pdMaxDiffX;
    public double[] pdFittedY;
    public double[] pdResiduals;
    public double dMaxDiff,SSE,dElapsedTimes;
    public String sExpression;
    public ArrayList<String> svParNames;
    public int nNumPars,nIterations,nEvaluations;
    public int MinimizationOption, MinimizationMethod,nVars,nDataPoints,nI,nF,nDelta;
    public double[][] m_pdX;
    public double[] m_pdY;
    public boolean bConverged;
    public ArrayList<Integer> nvNumParameters;//Number of Parameters of each component
    public int nComponents;
    public ArrayList<DoubleRange> cvVarRanges;
    public DoubleRange cDataRange;
    public ArrayList<ConstraintNode> cvConstraintNodes;
    public ImageShape m_cIS;
    public ArrayList<Integer> nvInvalidComponents;
    public ArrayList<Point> mainPeaks;
    public boolean invalid;
    public ArrayList<Integer> rIndexes;
    public ArrayList<Boolean> bvFitted;
    public int cIndex,rIndex;
    public int nModes;
    public double[] pdModePars;
    public double dSignalSD,dModePV,dMeritPV;
    int nSmoothingWS;//it is used when fitting histograms that have been smoothed
    public double[] m_pdSD;//being assigned by method setVariance when the variance of the data in m_pdY is know.
    public ArrayList<Integer>[] pvDataIndexesInModes;//data indexes in different modes.
    double ChiSquare;
    public int nNumOutliars;
    ChiSquaredDistributionImpl cChiSquareDistribution;
    public ModePars cModelPars;

    public FittingModelNode(){
        svFunctionTypes=new ArrayList();
        nSmoothingWS=0;
        sTitle="";
        sExpression="";
        svParNames=new ArrayList();
        nvNumParameters=new ArrayList();
        bConverged=false;
        cvConstraintNodes=new ArrayList();
        MinimizationOption=Non_Linear_Fitter.Least_Square;
        MinimizationMethod=Non_Linear_Fitter.Simplex;
        pdPars=new double[1];//constant 0
        pdPars[0]=0;
        svParNames.add("constant");
        nNumPars=1;
        nvInvalidComponents=new ArrayList();
        mainPeaks=new ArrayList();
        invalid=false;
        cDataRange=new DoubleRange();
        sHistFunctionType=null;
        dSignalSD=-1;
        dModePV=0.01;
        m_pdSD=null;
        pvDataIndexesInModes=null;
        nNumOutliars=0;
        nModes=1;
        dMeritPV=-1;
    }
    public void setFunctionTypes(ArrayList<String> svTypes){
        setFunctionTypes(svTypes,1);
    }
    public void setFunctionTypes(ArrayList<String> svTypes,int modes){
        svFunctionTypes=svTypes;
        nComponents=svTypes.size();
        ComposedFittingFunction func=new ComposedFittingFunction(svTypes);
        sExpression=func.getFunctionExpression().toString();
        nvNumParameters=func.m_nvNumPars;
        svParNames=func.getParNames();
        nNumPars=svParNames.size();
        nModes=modes;
        if(nModes>1&&nModes!=CentralMode) 
            pdModePars=new double[modes];
        else if(nModes==CentralMode)
            pdModePars=new double[2];
    }
    public FittingModelNode(double[][] pdX, double[] pdY){
        this();
        m_pdX=CommonStatisticsMethods.copyArray(pdX);
        m_pdY=CommonStatisticsMethods.copyArray(pdY);
        nDataPoints=m_pdX.length;
        nVars=m_pdX[0].length;
        cvVarRanges=CommonStatisticsMethods.getDoubleValueRanges(m_pdX);
        pdFittedY=new double[nDataPoints];
        pdMaxDiffX=new double[nVars];
        nI=0;
        nF=nDataPoints-1;
        nDelta=1;
    }
    public FittingModelNode(FittingModelNode aNode){
        this();
        svFunctionTypes=CommonStatisticsMethods.copyStringArray(aNode.svFunctionTypes);
        if(aNode.sExpression!=null) sExpression=new String(aNode.sExpression);
        svParNames=CommonStatisticsMethods.copyStringArray(aNode.svParNames);
        if(aNode.sTitle!=null)sTitle=new String(aNode.sTitle);
        nNumPars=aNode.nNumPars;
        nIterations=aNode.nIterations;
        nEvaluations=aNode.nEvaluations;
        
        pdPars=CommonStatisticsMethods.copyArray(aNode.pdPars);
        pdMaxDiffX=CommonStatisticsMethods.copyArray(aNode.pdMaxDiffX);
        pdFittedY=CommonStatisticsMethods.copyArray(aNode.pdFittedY);
        dMaxDiff=aNode.dMaxDiff;
        SSE=aNode.SSE;
        dElapsedTimes=aNode.dElapsedTimes;
        MinimizationOption=aNode.MinimizationOption;
        MinimizationMethod=aNode.MinimizationMethod;
        nVars=aNode.nVars;
        nDataPoints=aNode.nDataPoints;
        nI=aNode.nI;
        nF=aNode.nF;
        nDelta=aNode.nDelta;
        m_pdX=aNode.m_pdX;
        m_pdY=aNode.m_pdY;
        nModes=aNode.nModes;
        nvNumParameters=CommonStatisticsMethods.copyIntArray(aNode.nvNumParameters);
        nComponents=aNode.nComponents;
        bConverged=aNode.bConverged;
        cvVarRanges=CommonStatisticsMethods.copyDoubleRangeArray(aNode.cvVarRanges);
        dSignalSD=aNode.dSignalSD;
        dModePV=aNode.dModePV;
    }
    public int retrieveFittingResults(ArrayList<String> ParameterNames, ArrayList<Double> ParameterValues, ArrayList<String> ResultItems, ArrayList<Double> ResultValues, ArrayList<Integer> nvNumPars){

        if(svParNames==null) return -1;
        CommonStatisticsMethods.copyStringArray(svParNames, ParameterNames);
        int i,nNumPars=pdPars.length;

        for(i=0;i<nNumPars;i++){
            ParameterValues.add(pdPars[i]);
        }

        nvNumPars.clear();
        for(i=0;i<nvNumParameters.size();i++){
            nvNumPars.add(nvNumParameters.get(i));
        }

        ResultItems.add("SSE");
        ResultValues.add(SSE);
        ResultItems.add("MaxDiff");
        ResultValues.add(dMaxDiff);

        for(i=0;i<nVars;i++){
            ResultItems.add("MaxDiffX"+i);
            ResultValues.add(pdMaxDiffX[i]);
        }
        for(i=0;i<nVars;i++){
            ResultItems.add("MinX"+i);
            ResultValues.add(cvVarRanges.get(i).getMin());
        }
        for(i=0;i<nVars;i++){
            ResultItems.add("MaxX"+i);
            ResultValues.add(cvVarRanges.get(i).getMax());
        }
        ResultItems.add("pValues");
        ResultValues.add(-1.);
        ResultItems.add("Iterations");
        ResultValues.add((double)nIterations);
        ResultItems.add("Evaluations");
        ResultValues.add((double)nEvaluations);
        ResultItems.add("Elapsed-T(s)");
        ResultValues.add(dElapsedTimes);
        return 1;
    }
    public void removeOneComponent(int nComponentIndex){
        if(nComponentIndex<svFunctionTypes.size()&&nComponentIndex>=0)svFunctionTypes.remove(nComponentIndex);
        removeConstraint(nComponentIndex);
        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);
        svParNames=func.getParNames();
        nNumPars=svParNames.size();
        double[] pdt=new double[nNumPars];
        pdt[0]=pdPars[0];//the constant
        int i,nPars0=1,j,nPars=1;
        sExpression=func.getFunctionExpression().toString();
        ArrayList<Integer> indexesToRemove=new ArrayList();
        for(i=0;i<nComponents;i++){
            if(i==nComponentIndex) {
                for(j=0;j<nvNumParameters.get(i);j++){
                    indexesToRemove.add(nPars0);
                    nPars0++;
                }
                continue;
            }
            for(j=0;j<nvNumParameters.get(i);j++){
                pdt[nPars]=pdPars[nPars0];
                nPars++;
                nPars0++;
            }
        }
        pdPars=pdt;
        nComponents--;
        nvNumParameters.remove(nComponentIndex);
        unfixParameters(indexesToRemove);
    }
    public int unfixParameters(ArrayList<Integer> indexes){
        if(pnFixedParIndexes==null) return -1;
        int len=pnFixedParIndexes.length,i,index;
        ArrayList<Integer> nvT=new ArrayList();
        for(i=0;i<len;i++){
            index=pnFixedParIndexes[i];
            if(CommonMethods.containsContent(indexes,index)) continue;
            nvT.add(index);
        }
        len=nvT.size();
        pnFixedParIndexes=new int[len];
        for(i=0;i<len;i++){
            pnFixedParIndexes[i]=nvT.get(i);
        }
        return 1;
    }
    int removeConstraint(int nComponentIndex){
        if(cvConstraintNodes==null) return -1;
        int i,len=cvConstraintNodes.size(),num,nPars=1,j,len1;
        for(i=0;i<nComponentIndex;i++){
            num=nvNumParameters.get(i);
            nPars+=num;
        }
        num=nvNumParameters.get(nComponentIndex);
        intRange ir=new intRange(nPars,nPars+num-1);
        ConstraintNode aNode;
        boolean removed=false;
        for(i=len-1;i>=0;i--){
            aNode=cvConstraintNodes.get(i);
            len1=aNode.function.getConstrainedParIndexes().size();
            removed=false;
            for(j=0;j<len1;j++){
                if(ir.contains(aNode.function.getConstrainedParIndexes().get(j))) {
                    cvConstraintNodes.remove(i);
                    removed=true;
                    break;
                }
            }
        }
        return 1;
    }
    public void addOneComponent(String sType){
        updateResiduals();
        svFunctionTypes.add(sType);
        makeHistFittingReady(nSmoothingWS);
        nComponents++;
        double[] pd=ComposedFittingFunction.getDefaultPars(m_pdX,pdResiduals,sType);

        ComposedFittingFunction fuc=new ComposedFittingFunction(svFunctionTypes);
        sExpression=fuc.getFunctionExpression().toString();

        int num=pd.length-1;//not counting the constant
        pdPars[0]+=pd[0];

        nvNumParameters.add(num);
        svParNames=fuc.getParNames();
        double[] pdt=new double[nNumPars+num];
        for(int i=0;i<nNumPars;i++){
            pdt[i]=pdPars[i];
        }
        for(int i=0;i<num;i++){
            pdt[nNumPars+i]=pd[i+1];
        }
        pdPars=pdt;
        nNumPars=svParNames.size();
    }
    public int updateFittedData(){
        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);
        makeHistFittingReady(nSmoothingWS);
        int i,len=m_pdY.length;
        if(pdFittedY==null) pdFittedY=new double[len];
        if(pdFittedY.length!=len) pdFittedY=new double[len];
        if(svFunctionTypes.size()==0){
            for(i=0;i<len;i++){
                pdFittedY[i]=0;
            }
            return 1;
        }
        for(i=0;i<len;i++){
            pdFittedY[i]=func.fun(pdPars,m_pdX[i]);
        }
        return 1;
    }
    public double getFittedValue(double[] x){
        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);
        return func.fun(pdPars, x);
    }
    public int updateResiduals(){
        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);
        int i,len=m_pdY.length;
        if(pdResiduals==null) pdResiduals=new double[len];
        if(pdResiduals.length!=len) pdResiduals=new double[len];
        updateFittedData();
        for(i=0;i<len;i++){
            pdResiduals[i]=m_pdY[i]-pdFittedY[i];
        }
        return 1;
    }
    public void updateModel(ArrayList<String> svFunctionTypes, ArrayList<ConstraintNode> cvConstraints, double[] pdPars){
        this.pdPars=CommonStatisticsMethods.copyArray(pdPars);
        this.cvConstraintNodes=cvConstraints;
        this.svFunctionTypes=svFunctionTypes;
        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);

        svParNames=func.getParNames();
        sExpression=func.getExpressionType();
        nNumPars=svParNames.size();
        nComponents=svFunctionTypes.size();
        nvNumParameters=func.getNumParsArray(svFunctionTypes);
    }
    public void OptimizeOneComponentPars(int nComponent){
        int[] pnt=pnFixedParIndexes;
        int i,nPars=1,nPars0=1,num=nvNumParameters.get(nComponent),j;
        pnFixedParIndexes=new int[nNumPars-num];
        pnFixedParIndexes[0]=0;//the constant term
        for(i=0;i<nComponents;i++){
            num=nvNumParameters.get(i);
            if(i==nComponent) {
                nPars0+=num;
                continue;
            }
            for(j=0;j<num;j++){
                pnFixedParIndexes[nPars]=nPars0;
                nPars0++;
                nPars++;
            }
        }
        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);
        Non_Linear_Fitter fitter=new Non_Linear_Fitter(m_pdX,m_pdY,pdPars,func,MinimizationOption,MinimizationMethod,nI,
                nF,nDelta,pnFixedParIndexes,this,0.01);
        fitter.doFit_Apache(5000);
    }
    public int[] getFixedParIndexes(){
        int[] pnFixedIndexes=new int[nNumPars];
        int i;
        for(i=0;i<nNumPars;i++){
            pnFixedIndexes[i]=0;
        }
        int nPars=0;

        if(pnFixedParIndexes==null)
            nPars=0;
        else
            nPars=pnFixedParIndexes.length;

        for(i=0;i<nPars;i++){
            pnFixedIndexes[pnFixedParIndexes[i]]=1;
        }
        return pnFixedIndexes;
    }
    public void freeAllPars(){
        pnFixedParIndexes=null;
    }
    public void fixAllPars(){
        pnFixedParIndexes=new int[nNumPars];
        for(int i=0;i<nNumPars;i++){
            pnFixedParIndexes[i]=i;
        }
    }
    public int getNumFreePars(){
        int num=nNumPars;
        if(pnFixedParIndexes==null) return nNumPars;
        return nNumPars-pnFixedParIndexes.length;
    }
    public void deleteInvalidComponents(){
        int i,len=nvInvalidComponents.size();
        for(i=len-1;i>=0;i--){
            removeOneComponent(nvInvalidComponents.get(i));
        }
    }
    public int toGaussian2D(){//transforms fitting functions (if exists) from "gaussian2D_GaussianPars" to "gaussian2D"
        int i,nPars=1,transformed=-1;
        for(i=0;i<nComponents;i++){
            if(svFunctionTypes.get(i).contentEquals("gaussian2D_GaussianPars")){
                ComposedFittingFunction.getTransformedGaussian2DParameters(pdPars, nPars, pdPars, nPars);
                svFunctionTypes.set(i, "gaussian2D");
                transformed=1;
            }
            nPars+=nvNumParameters.get(i);
        }
        ComposedFittingFunction fuc=new ComposedFittingFunction(svFunctionTypes);
        svParNames=fuc.getParNames();
        return transformed;
    }
    public int toGaussian2D_GaussianPars(){//transforms fitting functions (if exists) from "gaussian2D" to "gaussian2D_GaussianPars"
        int i,nPars=1,transformed=-1;
        for(i=0;i<nComponents;i++){
            if(svFunctionTypes.get(i).contentEquals("gaussian2D")){
                ComposedFittingFunction.getGaussian2DParameters(pdPars, nPars, pdPars, nPars);
                svFunctionTypes.set(i, "gaussian2D_GaussianPars");
                transformed=1;
            }
            nPars+=nvNumParameters.get(i);
        }
        ComposedFittingFunction fuc=new ComposedFittingFunction(svFunctionTypes);
        svParNames=fuc.getParNames();
        return transformed;
    }
    public ArrayList<Point> getPeakPositions(){
        int num,nPars=1,i;
        ArrayList<Point> points=new ArrayList();
        for(i=0;i<nComponents;i++){
            num=nvNumParameters.get(i);
            nPars+=num;
            if(svFunctionTypes.get(i).startsWith("gaussian")){
                points.add(new Point((int)(pdPars[nPars-2]+0.5),(int)(pdPars[nPars-1]+0.5)));
            }
        }
        return points;
    }
    public void setRegionIndexes(ArrayList<Integer> rIndexes){
        this.rIndexes=rIndexes;
    }
    public void scaleData(double scale){
        int i,len=m_pdY.length;
        for(i=0;i<len;i++){
            m_pdY[i]/=scale;
        }
    }
    public void calDataRange(){
        int i,len=m_pdY.length;
        for(i=0;i<len;i++){
            cDataRange.expandRange(m_pdY[i]);
        }
    }
    public void copyData(int[][] pixels, int x0, int y0){
        int i,len=m_pdX.length,x,y,xn=Integer.MAX_VALUE,yn=Integer.MAX_VALUE;
        for(i=0;i<len;i++){
            x=(int)(m_pdX[i][0]+0.5);
            if(x<xn) xn=x;
            y=(int)(m_pdX[i][1]+0.5);
            if(y<yn) yn=y;
        }
        for(i=0;i<len;i++){
            x=(int)(m_pdX[i][0]+0.5);
            y=(int)(m_pdX[i][1]+0.5);
            pixels[y-yn+y0][x-xn+x0]=(int)(m_pdY[i]+0.5);
        }
        calDataRange();
    }
    public DoubleRange getDataRange(){
        DoubleRange dRange=new DoubleRange();
        int i,len=m_pdY.length;
        for(i=0;i<len;i++){
            dRange.expandRange(m_pdY[i]);
        }
        return dRange;
    }
    public DoubleRange getVarRange(int index){
        DoubleRange cVR=new DoubleRange();
        int i,len=m_pdX.length;
        for(i=0;i<len;i++){
            cVR.expandRange(m_pdX[i][index]);
        }
        return cVR;
    }
    public void makeHistFittingReady(int nWS){
        nSmoothingWS=nWS;
        m_pdX=getHistFittingReady(m_pdX,m_pdX,svFunctionTypes,nWS);
    }
    static public double[][] getHistFittingReady(double[][] pdX,double[][] pdXt,String sType, int nWS){
        //pdX is the original data.
        double pdXr[][]=null;
        if(sType.contentEquals("gaussian_Hist")){
            int nData=pdX.length,len=pdXt.length,i;
            double delta=(pdX[nData-1][0]-pdX[0][0])/(nData-1),x;
            pdXr=new double[len][3];
            for(i=0;i<len;i++){
                x=pdXt[i][0];
                pdXr[i][0]=x;
                pdXr[i][1]=x-delta*(nWS+0.5);
                pdXr[i][2]=x+delta*(nWS+0.5);
            }
        }
        return pdXr;
    }
    static public double[][] getHistFittingReady(double[][] pdX, double pdXt[][], ArrayList<String> svFunctionTypes, int nWS){
        int i,num=svFunctionTypes.size();
        String sType;
        double[][] pdXr=null;
        String sHistFunctionType=null;
        for(i=0;i<num;i++){
            sType=svFunctionTypes.get(i);
            if(sType.contains("_Hist")){
                if(sHistFunctionType!=null){
                    if(!sHistFunctionType.contentEquals(sType)){
                        IJ.error("Conflicting Hist Function Types");//generalize it later
                    }
                    continue;
                }
                sHistFunctionType=sType;
                pdXr=getHistFittingReady(pdX,pdXt,sType,nWS);
            }
        }
        if(sHistFunctionType==null) pdXr=pdXt;
        return pdXr;
    }
    public double getSignificance_ChiSquare(){
        if(getNModes()>1) return getSignificance_Multimodal();
        if(m_pdSD==null) IJ.error("need know variance to use method getSignificance_ChiSqaure");
        if(m_pdSD==null) return -1;
        int nu=getDF();
        if(nu<=0){
            IJ.error("the number of data point need to be larger than df+1="+(getNumFreePars()+1));
        }
        ChiSquaredDistributionImpl dist=new ChiSquaredDistributionImpl(nu);
        cChiSquareDistribution=dist;
        double sig=-1;
        calChiSquare();
        try {
            sig=1-dist.cumulativeProbability(ChiSquare);
        }
        catch (org.apache.commons.math.MathException e){

        }
        return sig;
    }
    public int calChiSquare(){
        if(m_pdSD==null) IJ.error("need know variance to use method getSignificance_ChiSqaure");
        if(m_pdSD==null) return -1;
        ComposedFittingFunction fun=new ComposedFittingFunction(svFunctionTypes);
        double dev,sd;
        ChiSquare=0;
        for(int i=nI;i<=nF;i+=nDelta){
            dev=m_pdY[i]-fun.fun(pdPars,m_pdX[i]);
            sd=m_pdSD[i];
            ChiSquare+=dev*dev/(sd*sd);
        }
        return 1;
    }
    public int getDF(){
        return nDataPoints-getNumFreePars()-1-(getNModes()-1);
    }
    public int getNModes(){
        if(nModes==FittingModelNode.CentralMode) return 3;
        return nModes;
    }
    public double getSignificance_Tilting(){
        int ws=3;
        updateResiduals();
        double devMax=CommonStatisticsMethods.getMaxDeltaDev(pdResiduals,m_pdSD, 0, ws)/ws;
        devMax=Math.abs(devMax);
        double sig=1-GaussianDistribution.Phi(devMax, 0, Math.abs(Math.sqrt(2./ws)));
        return sig;
    }
    public int getNumInMainMode(){
        if(getNModes()<=0) return nDataPoints;
        int index=getMainMode();
        return pvDataIndexesInModes[index].size();
    }
    public String[][] getFittingEvaluationAsStringArray(String title){
        int ws=3;
        String[] ColumnHeads={"N","Df","SD","ChiSquare","Significance","Mode","nData","DevSigWS"+ws,"Iterations"};
        ArrayList<String> svHeads=CommonStatisticsMethods.copyStringArray(ColumnHeads);
        svHeads.add(0,"Title");
        int vars=m_pdX[0].length,i;
        for(i=0;i<vars;i++){
            svHeads.add("X"+i+"Min");
            svHeads.add("X"+i+"Max");
        }
        String[][] psData=new String[2][svHeads.size()];
        double sig=getSignificance_ChiSquare();
        ColumnHeads=CommonStatisticsMethods.copyStringArray1(svHeads);
        psData[0]=ColumnHeads;
        int index=0;
        psData[1][index]=title;
        index++;
        psData[1][index]=PrintAssist.ToString(getNumInMainMode());
        index++;
        psData[1][index]=PrintAssist.ToString(cChiSquareDistribution.getDegreesOfFreedom(),0);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(Math.sqrt(CommonStatisticsMethods.buildMeanSem(m_pdSD).mean),3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(ChiSquare, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(sig, 3);
        index++;
        psData[1][index]=""+nModes;
        index++;
        psData[1][index]=""+nDataPoints;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(getSignificance_Tilting(), 3);
        index++;
        psData[1][index]=PrintAssist.ToString(nIterations);
        index++;
        DoubleRange dr=new DoubleRange();
        for(i=0;i<vars;i++){
            dr=getVarRange(i);
            psData[1][index]=PrintAssist.ToString(dr.getMin(), 3);
            index++;
            psData[1][index]=PrintAssist.ToString(dr.getMax(), 3);
            index++;
        }
        return psData;
    }
    public void calModePars_CentralMode(){
        this.updateFittedData();
        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);
        double y,dy,y0,dZatP=GaussianDistribution.getZatP(1-dModePV, 0, dSignalSD, 0.001*dSignalSD);
        int i;
        ArrayList<Double> dvY=new ArrayList(),dvYn=new ArrayList(), dvYp=new ArrayList();

        double mean=0,ss=0,meanN=0,ssN=0,meanP=0,ssP=0;
        int n=0,nN=0,nP=0;
        int len=m_pdX.length;
        for(i=0;i<len;i++){
            y0=m_pdY[i];
            y=func.fun(pdPars, m_pdX[i]);
            dy=y0-y;
            if(dy<-dZatP){
                nN+=1;
                meanN+=dy;
                ssN+=dy*dy;
            }
            else if(dy>dZatP)
            {
                nP+=1;
                meanP+=dy;
                ssP+=dy*dy;
            }
            else
            {
                n+=1;
                mean+=dy;
                ss+=dy*dy;
            }
        }
        if(nN>0)pdModePars[0]=meanN/nN;
        if(nP>0)pdModePars[1]=meanP/nP;
    }
    public void setSD(double[] pdSD){
        m_pdSD=pdSD;
    }
    public double[] getVar(int index){
        double[] pdX=new double[nDataPoints];
        int it=0;
        for(int i=nI;i<=nF;i+=nDelta){
            pdX[it]=m_pdX[i][index];
            it++;
        }
        return pdX;
    }
    public double getSignificance_Multimodal(){
        if(m_pdSD==null) IJ.error("need know variance to use method getSignificance_ChiSqaure");
        if(m_pdSD==null) return -1;
        double sig=-1;
        int ModeId=getMainMode(),index;
        ArrayList<Integer> indexes=pvDataIndexesInModes[ModeId];
        int i,len=indexes.size();
        updateFittedData();
        double dy;
        calChiSquare();
        int nu=len-getNumFreePars()-1-(getNModes()-1);
        if(nu<=0){
            IJ.error("the number of data point need to be larger than df+1="+(getNumFreePars()+1));
        }
        ChiSquaredDistributionImpl dist=new ChiSquaredDistributionImpl(nu);
        cChiSquareDistribution=dist;
        try {
            sig=1-dist.cumulativeProbability(ChiSquare);
        }
        catch (org.apache.commons.math.MathException e){

        }
        return sig;
    }
    public int getMainMode(){
        seperateModes();
        int n,nMax=0,i,iMax=0;
        for(i=0;i<getNModes();i++){
            n=pvDataIndexesInModes[i].size();
            if(n>nMax){
                nMax=n;
                iMax=i;
            }
        }
        if(iMax!=0){
            ArrayList<Integer> nvT=pvDataIndexesInModes[iMax];
            pvDataIndexesInModes[iMax]=pvDataIndexesInModes[0];
            pvDataIndexesInModes[0]=nvT;
            double dM=pdModePars[iMax];
            pdPars[0]+=dM;
            for(i=0;i<getNModes()-1;i++){
                pdModePars[i]-=dM;
            }
        }
        return iMax;//iMax==0
    }
    public void seperateModes(){
        int i,j,iMax,nModes=getNModes(),num;
        ArrayList<Integer>[] pvDataIndexes=new ArrayList[nModes];
        for(i=0;i<nModes;i++){
            pvDataIndexes[i]=new ArrayList();
        }

        ComposedFittingFunction func=new ComposedFittingFunction(svFunctionTypes);
        double y0,y,y1,dy,dyMax;
        for(i=nI;i<=nF;i+=nDelta){
            y=func.fun(pdPars, m_pdX[i]);
            y0=m_pdY[i];
            iMax=0;
            dyMax=Math.abs(y-y0);
            for(j=0;j<nModes-1;j++){
                y1=y+pdModePars[j];
                dy=Math.abs(y1-y0);
                if(dy>dyMax){
                    dyMax=dy;
                    iMax=j+1;
                }
            }
            pvDataIndexes[iMax].add(i);
        }
        pvDataIndexesInModes=pvDataIndexes;
    }
    public void setNumOutliars(int n){
        nNumOutliars=n;
    }
}
