/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.Non_LinearFitting;
import utilities.Non_LinearFitting.Fitting_Function;
import utilities.Non_LinearFitting.Simplex;
import java.util.ArrayList;
import utilities.Non_LinearFitting.FittingComparison;
import ij.IJ;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;

import org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.optimization.general.ConjugateGradientFormula;
import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.VectorialPointValuePair;

import utilities.CommonMethods;
import utilities.Non_LinearFitting.Constrains.*;
import ij.ImagePlus;
import java.awt.Point;
import utilities.CommonStatisticsMethods;
import utilities.Non_LinearFitting.OptimizationProcessMonitor;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import utilities.statistics.MeanSem0;
import utilities.statistics.MeanSem1;
import utilities.statistics.GaussianDistribution;
import utilities.RunningWindowRankingKeeper;
import utilities.statistics.OneDKMeans;
/**
 *
 * @author Taihao
 */
public class Non_Linear_Fitter {
    class fittingFunction implements MultivariateRealFunction{
        public fittingFunction(){

        }
        public double value (double[] x){
            double dv=fun(x);
            return dv;
        }
    }
    class partialDerivativeFunction implements MultivariateRealFunction{
        int k;
        public partialDerivativeFunction(int k){this.k=k;};
        public double value (double[] x){
            double dv=partialDerivative(x,k);//fix this tomorrow
            return dv;
        }
    }
    class gradientFunction implements MultivariateVectorialFunction{
        public gradientFunction(){
            
        }
        public double[] value(double[] x){
            double gradien[]=gradient(x);
            return gradien;
        }
    }
    class DifferentiableFittingFunction implements DifferentiableMultivariateRealFunction{
        public MultivariateRealFunction partialDerivative(int k){
            return new partialDerivativeFunction(k);
        }
        public MultivariateVectorialFunction gradient(){
            return new gradientFunction();
        }
        public double value (double[] x){
            double dv=fun(x);
            return dv;
        }
    }

    class jacobian implements MultivariateMatrixFunction{
        public double[][] value(double[] pars){
            int i,j,nData=(m_nF-m_nI)/m_nDelta+1;
            double pdV[][]=new double[nData][m_nAdjustablePars];
            gradientFunction fg=new gradientFunction();
            double[] gradient=new double[m_nPars], gradientFP=new double[m_nAdjustablePars];
            updatePars(pars);
            for(i=m_nI;i<m_nF;i+=m_nDelta){
                m_cFunc.funValueAndGradient(m_pdPars, m_pdX[i], gradient);
                updateAdjustablePars(gradient,gradientFP);
                for(j=0;j<m_nAdjustablePars;j++){
                    pdV[i][j]=gradientFP[j];
                }
            }
            return pdV;
        }
    }
    class DifferentiableVectorialFittingFunction implements DifferentiableMultivariateVectorialFunction{
        public MultivariateMatrixFunction jacobian(){
            return new jacobian();
        }
        public double[] value(double[] pars){
            int i,nData=(m_nF-m_nI)/m_nDelta+1;
            updatePars(pars);
            double[] pdYFitted=new double[nData];
            double dv=0,Y,Y0;
            for(i=m_nI;i<m_nF;i+=m_nDelta){
                Y=m_cFunc.fun(m_pdPars,m_pdX[i]);
                Y0=m_pdY[i];
                dv+=(Y-Y0)*(Y-Y0);
                pdYFitted[i]=Y;
            }
            if(dv<m_dBestScore){
                CommonStatisticsMethods.copyArray(pars, m_pdBestPars);
                m_dBestScore=dv;
            }
            return pdYFitted;
        }
    }


    public final static int Least_Square=0;
    public final static int Maximum_Likelihood=1;
    public final static int Simplex=11;
    int MinimizationOption, MinimizationMethod;
    int m_nI,m_nF,m_nDelta;//data fitting range
    double m_pdX[][], m_pdY[], m_pdYT[], m_pdPars[], m_pdAdjustablePars[],m_pdFittedPars[];
    
    double[][] m_pdSimplex,m_pdSimplex0;
    Fitting_Function m_cFunc;
    boolean m_bExpandModel;
    int m_nMaxPars,m_nMaxIterations;
    ArrayList<double[]> m_pdvFittedPars;
    fittingFunction m_cFunc_Apache;
    DifferentiableFittingFunction m_cFunc_ConjugatedGradient;
    DifferentiableVectorialFittingFunction m_cFunc_LevenbergMarquardt;
    double m_dFTol,m_dTol;
    double m_pdOrigin[],m_pdXPrime[];
    int m_pnAdjustableParIndexes[];
    int m_nPars,m_nVars,m_nPtrs,m_nAdjustablePars;//numbers of parameters, independet variables and data points
    int m_nNumConstraints;
    int m_nNumEvaluations;
    int[] m_pnFixedParIndexes;
    int m_nIterations,m_nIterations0,m_nInterval;
    long startTime,endTime;
    double m_dSimplexScale;
    ImageShape m_cIS;
    int[] m_pnDrawingVarIndexes;
    ArrayList<ConstraintNode> m_cvConstraintNodes;
    ArrayList<Double> m_dvFittedFunctionValues;
    ImagePlus implFunc;
    ConstraintExpander m_cConstraintExpander;
    NelderMead opt= new NelderMead();
    NonLinearConjugateGradientOptimizer optCG;
    LevenbergMarquardtOptimizer optLM;
    RealPointValuePair pvp;

    OptimizationProcessMonitor m_cProcessMonitor;
    ArrayList<Integer> m_nvHardConstraintIndexes;
    FittingResultsNode m_cFittingResultsNode;
    FittingModelExpander m_cModelExpander;
    FittingModelNode m_cStorageModel;
    double m_dBestScore;
    double[] m_pdBestPars;
    boolean resumingFitting;
    Simplex m_cStoredSimplex;
    VectorialPointValuePair vpvp=null;

//    Thread m_cFittingThread;

    boolean bPause;
    int m_nPausingIterations;
    boolean bStoring;
    protected ArrayList<ActionListener> m_cvListeners;
    double[] m_pdGradient,m_pdGradientFP,pdDev;
    int m_nFittingModes;
    double m_pdModePar[];
    double m_dSignalSD,m_dModePV;
    int m_nNumOutliars;
    OneDKMeans m_cKM;
    ModePars m_cModePars;
    RunningWindowRankingKeeper m_cDevRankingKeeper;

    ArrayList<Integer> nvTransformedGaussianComponents;

    public int getMinimizationOption(){
        return MinimizationOption;
    }
    public int getMinimizationMethod(){
        return MinimizationMethod;
    }
    public void setNumOutliars(int n){
        m_nNumOutliars=n;
        m_cDevRankingKeeper=new RunningWindowRankingKeeper(n,false);
    }
    public static String getMinimizationOptionName(int option){
        String name="";
        boolean invalidOption=true;
        if(option==Least_Square){
            invalidOption=false;
            name="Least_Square";
        }
        if(option==Maximum_Likelihood){
            invalidOption=false;
            name="Maximum_Likelihood";
        }
        if(invalidOption) IJ.error("invalid option for getMinimizationOptionName");
        return name;
    }
    public static int getMinimizationOption(String sOption){
        int option=-1;
        boolean invalidOption=true;
        if(sOption.equalsIgnoreCase("Least_Square")){
            invalidOption=false;
            option=Least_Square;
        }
        if(sOption.equalsIgnoreCase("Maximum_Likelihood")){
            invalidOption=false;
            option=Maximum_Likelihood;
        }
        if(invalidOption) IJ.error("invalid option for getMinimizationOptionName");
        return option;
    }
    public static String getMinimizationMethodName(int method){
        String name="";
        boolean invalidOption=true;
        if(method==Simplex){
            invalidOption=false;
            name="Simplex";
        }
        if(invalidOption) IJ.error("invalid option for getMinimizationMethodName");
        return name;
    }
    public static int getMinimizationMethod(String sMethod){
        int name=-1;
        boolean invalidOption=true;
        if(sMethod.equalsIgnoreCase("Simplex")){
            invalidOption=false;
            name=Simplex;
        }
        if(invalidOption) IJ.error("invalid option for getMinimizationMethodName");
        return name;
    }
    public Non_Linear_Fitter(double[][] pdX, double[] pdY, double[] pdPars, Fitting_Function func, int MinimizationOption, int MinimizationMethod,
            int nI, int nF, int nDelta, int[] pnFixedParIndexes){
        this();
        update(pdX,pdY,pdPars,func,MinimizationOption,MinimizationMethod,nI,nF,nDelta,pnFixedParIndexes);
    }
    public Non_Linear_Fitter(double[][] pdX, double[] pdY, double[] pdPars, Fitting_Function func, int MinimizationOption, int MinimizationMethod,
            int nI, int nF, int nDelta, int[] pnFixedParIndexes, FittingModelNode cStorageModel){
        this();
        update(pdX,pdY,pdPars,func,MinimizationOption,MinimizationMethod,nI,nF,nDelta,pnFixedParIndexes);
        m_cStorageModel=cStorageModel;
    }
    public Non_Linear_Fitter(double[][] pdX, double[] pdY, double[] pdPars, Fitting_Function func, int MinimizationOption, int MinimizationMethod,
            int nI, int nF, int nDelta, int[] pnFixedParIndexes, FittingModelNode cStorageModel, double simplexScale){
        this();
        update(pdX,pdY,pdPars,func,MinimizationOption,MinimizationMethod,nI,nF,nDelta,pnFixedParIndexes);
        m_cStorageModel=cStorageModel;
        m_dSimplexScale=simplexScale;
    }
    public void setFTol(double fTol){
        m_dFTol=fTol;
    }
    public void setTol(double dTol){
        m_dTol=dTol;
    }
    public Non_Linear_Fitter(){
        m_nFittingModes=1;
        m_nPausingIterations=Integer.MAX_VALUE;
        resumingFitting=false;
        m_bExpandModel=false;
        m_nMaxPars=m_nPars;
        m_nDelta=1;
        m_pdvFittedPars=new ArrayList();
        m_nMaxIterations=4000;
        double dMEPS=CommonMethods.MachineEpsilonDouble()*100;
        m_dFTol=2*dMEPS;
        m_dTol=2*Math.sqrt(dMEPS);
        m_pdOrigin=null;
        m_cvConstraintNodes=new ArrayList();
        m_nNumConstraints=0;
        m_dvFittedFunctionValues=new ArrayList();
        m_cFittingResultsNode=new FittingResultsNode();
        m_cModelExpander=null;
        m_cStorageModel=null;
        m_dSimplexScale=0.01;
        bPause=false;
        m_cvListeners=new ArrayList();
        nvTransformedGaussianComponents=new ArrayList();
    }

    public void setHiConvergence(){
        double dMEPS=CommonMethods.MachineEpsilonDouble()*100;
        m_dFTol=2*dMEPS;
        m_dTol=2*Math.sqrt(dMEPS);
    }
    public void setLoConvergence(){
        double dMEPS=CommonMethods.MachineEpsilonDouble()*100000000;
        m_dFTol=2*dMEPS;
        m_dTol=2*Math.sqrt(dMEPS);
    }

    public void pauseFitting(){
        bPause=true;
    }
    public void setMaxPausingIteration(){
        m_nPausingIterations=m_nMaxIterations;
    }
    public void resumeFitting(){
        bPause=false;
    }
    public void setMaxIteraxtions(int nMaxIterations){
        m_nMaxIterations=nMaxIterations;
    }
    public boolean isPausedFitting(){
        return bPause;
    }
    public void setAsExpandableModel(int nMaxPars){
        m_bExpandModel=true;
        m_nMaxPars=nMaxPars;
    }
    public void fitData(){
        boolean moreIterations=true;
        while(moreIterations){
            doFit();
            moreIterations=expandModel();
        }
    }
    public void fitData_Apache(){
        boolean moreIterations=true;
        while(moreIterations){
            doFit_Apache();
            updateFittingModel();
//            drawFunctionValues(m_pdPars);
            moreIterations=expandModel();
        }
//        setFittingResultsDescription();//shouldn't need this any more 11706
    }/*
    void setFittingResultsDescription(){
        m_cFunc.getDescriptions(m_cFittingResultsNode.svDescription, m_cFittingResultsNode.svExpression, m_cFittingResultsNode.svBaseParNames, m_cFittingResultsNode.svExpandedParNames, m_cFittingResultsNode.nvNumParameters);
    }*/
    void calVariableRanges(){
        m_cFittingResultsNode.cvVarRanges.clear();
        int numData=m_pdX.length,nVars=m_pdX[0].length,i,j;
        double dV;
        for(i=0;i<nVars;i++){
            m_cFittingResultsNode.cvVarRanges.add(new DoubleRange());
        }
        for(i=0;i<numData;i++){
            for(j=0;j<nVars;j++){
                dV=m_pdX[i][j];
                m_cFittingResultsNode.cvVarRanges.get(j).expandRange(dV);
            }
        }
    }
    void updateFittingModel(){
        bStoring=true;

        if(pvp!=null){
            CommonStatisticsMethods.copyArray(pvp.getPoint(), m_pdAdjustablePars);
            updatePars(pvp.getPoint());
        }else if(vpvp!=null){//reached iterations or evaluations
            CommonStatisticsMethods.copyArray(vpvp.getPoint(), m_pdAdjustablePars);
            updatePars(m_pdAdjustablePars);
        }else{
            CommonStatisticsMethods.copyArray(m_pdBestPars, m_pdAdjustablePars);
            updatePars(m_pdBestPars);
        }

        if(m_pdFittedPars==null) m_pdFittedPars=new double[m_pdPars.length];
        if(m_pdFittedPars.length!=m_pdPars.length) m_pdFittedPars=new double[m_pdPars.length];
        CommonStatisticsMethods.copyArray(m_pdPars,m_pdFittedPars);

//        m_pdFittedPars=m_pdPars;
//        ComposedFittingFunction.toGaussian2D_GaussianPars(m_pdFittedPars, m_cFunc.copyComponentFunctionTypes(), nvTransformedGaussianComponents);
        
        ArrayList<FittingModelNode> cvModels=m_cFittingResultsNode.m_cvModels;
        FittingModelNode newModel;
        if(m_cStorageModel!=null){
            newModel=m_cStorageModel;
        }
        else{
            newModel=new FittingModelNode();
            m_cStorageModel=newModel;
            ArrayList<String> svTypes=CommonStatisticsMethods.copyStringArray(m_cFunc.getComponentFunctionTypes());
            newModel.setFunctionTypes(svTypes, m_nFittingModes);
        }
        double SSE=fun(m_pdAdjustablePars);
//        m_cFittingResultsNode.dvSSE.add(SSE);
        newModel.SSE=SSE;
//        int nModels=m_pdvFittedPars.size();
        int nModels=cvModels.size();
        double p;
        double[] pdFittedY;
        int i,j,nVars,nPoints=m_pdX.length;

        nVars=m_pdX[0].length;
        newModel.MinimizationMethod=MinimizationMethod;
        newModel.MinimizationOption=MinimizationOption;
        newModel.nDataPoints=nPoints;
        newModel.nVars=nVars;
        newModel.nI=m_nI;
        newModel.nF=m_nF;
        newModel.nDelta=m_nDelta;
        newModel.m_pdX=m_pdX;
        newModel.m_pdY=m_pdY;
        newModel.bConverged=(m_nIterations<m_nMaxIterations);
        newModel.nComponents=m_cFunc.m_nNumComponents;

        if(m_nFittingModes>1&&m_nFittingModes!=FittingModelNode.CentralMode){
            for(i=m_nAdjustablePars;i<m_pdAdjustablePars.length;i++){
//                newModel.pdModePars[i-m_nAdjustablePars]=m_pdAdjustablePars[i];
            }
        }

        if(nModels>0) {
            double SSE0=m_cFittingResultsNode.m_cvModels.get(nModels-1).SSE;
            double df=m_pdFittedPars.length;
            double df0=m_cFittingResultsNode.m_cvModels.get(nModels-1).pdPars.length;
            p=FittingComparison.getFittingComparison_LeastSquare(SSE0, df0, SSE, df, m_nPtrs);
        }else{
            p=0;
            calVariableRanges();
            nPoints=m_pdX.length;
            if(m_cFittingResultsNode.m_pdX==null){
                m_cFittingResultsNode.m_pdX=new double[nPoints][nVars];
                m_cFittingResultsNode.m_pdY=new double[nPoints];
            }else if(m_cFittingResultsNode.m_pdX.length!=nPoints){
                m_cFittingResultsNode.m_pdX=new double[nPoints][nVars];
                m_cFittingResultsNode.m_pdY=new double[nPoints];
            }
            for(i=0;i<nPoints;i++){
                for(j=0;j<nVars;j++){
                    m_cFittingResultsNode.m_pdX[i][j]=m_pdX[i][j];
                }
                m_cFittingResultsNode.m_pdY[i]=m_pdY[i];
            }
        }
        pdFittedY=new double[nPoints];
        m_cFittingResultsNode.m_dvPValues.add(p);
//        int iMax=getLargestDeviationPosition(m_pdPars);
        int iMax=getMostUnderEstimatedPosition(m_pdPars,pdFittedY);
        double MaxDiff=m_pdY[iMax]-m_cFunc.fun(m_pdPars,m_pdX[iMax]);
        newModel.pdMaxDiffX=CommonStatisticsMethods.copyArray(m_pdX[iMax]);
        newModel.dMaxDiff=MaxDiff;
        newModel.dElapsedTimes=(endTime-startTime)/1000.;
        newModel.nIterations=m_nIterations;
        newModel.nEvaluations=m_nNumEvaluations;
        newModel.pdFittedY=CommonStatisticsMethods.copyArray(pdFittedY);
        newModel.pnFixedParIndexes=CommonStatisticsMethods.copyArray(m_pnFixedParIndexes);
        newModel.nNumPars=m_cFunc.getNumPars();
        newModel.nvNumParameters=CommonStatisticsMethods.copyIntArray(m_cFunc.getNumParsInComponents());
        newModel.sExpression=new String(m_cFunc.getFunctionExpression());
        newModel.svParNames=CommonStatisticsMethods.copyStringArray(m_cFunc.getParNames());
        newModel.cvVarRanges=m_cFittingResultsNode.cvVarRanges;
        newModel.cvConstraintNodes=m_cvConstraintNodes;
        newModel.dSignalSD=this.m_dSignalSD;
        newModel.dModePV=this.m_dModePV;


        newModel.pdPars=CommonStatisticsMethods.copyArray(m_pdFittedPars);
        if(m_nFittingModes==FittingModelNode.CentralMode) newModel.calModePars_CentralMode();
        if(m_cStorageModel==null)m_cFittingResultsNode.addModel(newModel);
        bStoring=false;
    }
    int[] getFixedParIndexes(){
        return m_pnFixedParIndexes;
    }
    public ArrayList<ConstraintNode> getConstraints(){
        return m_cvConstraintNodes;
    }
    public void updateFunction(Fitting_Function func){
        m_cFunc=func;
    }
    public void updateFixedParIndexes(int[] pnFixedParIndexes){
        init(m_pdPars, pnFixedParIndexes);
    }
    public void updateEntirePars(double[] pdPars){
        init(pdPars);
    }
    boolean expandModel(){
        if(!m_bExpandModel)return false;

        int nModels=m_cFittingResultsNode.nModels;
        if(nModels>1) if(!improvedFitting()) return false;
        m_pdvFittedPars.add(m_pdFittedPars);
        m_dvFittedFunctionValues.add(fun(m_pdAdjustablePars));
        m_cModelExpander.expandModel(this,3);//add maximum of 3 new components at one time, arbitrarily decided
        if(m_pdPars.length>m_nMaxPars) return false;
        return true;
    }
    boolean expandModelo(){
        if(!m_bExpandModel)return false;

        int nModels=m_pdvFittedPars.size();
        if(nModels>0) if(!improvedFitting()) return false;
        m_pdvFittedPars.add(m_pdFittedPars);
        m_dvFittedFunctionValues.add(fun(m_pdAdjustablePars));
//        int iMax=getMostUnderEstimatedPosition(m_pdPars);
        int iMax=getLargestDeviationPosition(m_pdPars);
        double MaxDiff=m_pdY[iMax]-m_cFunc.fun(m_pdPars,m_pdX[iMax]);
        double[] pdPars=m_cFunc.getExpandedModel(m_pdFittedPars,m_pdX[iMax],MaxDiff);

//        RelaxHardConstraints_Apache();
//        fixHardConstrainedTerms();

        if(pdPars.length>m_nMaxPars) return false;
        expandConstraints();
        init(pdPars,m_pnFixedParIndexes);
        return true;
    }
    int getLargestDeviationPosition(double[] pars){
        double dv=0,y,y0,dMax=Double.NEGATIVE_INFINITY;
        int i,iMax=0;
        updatePars(pars);
        for(i=m_nI;i<=m_nF;i+=m_nDelta){
            y0=m_pdY[i];
            y=m_cFunc.fun(m_pdPars, m_pdX[i]);
            dv=(y-y0)*(y-y0);
            if(dv>dMax){
                dMax=dv;
                iMax=i;
            }
        }
        return iMax;
    }
    int getMostUnderEstimatedPosition(double[] pars,double[] pdFittedY){
        double dv=0,y,y0,dMax=Double.NEGATIVE_INFINITY;
        int i,iMax=0;
        for(i=m_nI;i<=m_nF;i+=m_nDelta){
            y0=m_pdY[i];
            y=m_cFunc.fun(m_pdPars, m_pdX[i]);
            pdFittedY[i]=y;
            dv=y0-y;
            if(dv>dMax){
                dMax=dv;
                iMax=i;
            }
        }
        return iMax;
    }
    void pickHardConstrainedIndexes(){
        OptimizationStepNode aNode;
        aNode=m_cProcessMonitor.getLowestStep();
        m_nvHardConstraintIndexes=aNode.nvViolatedIndexes;
    }
    int RelaxHardConstraints_Apache(){
        OptimizationStepNode aNode;
        while(true){
            aNode=m_cProcessMonitor.getLowestStep();
            m_nvHardConstraintIndexes=aNode.nvViolatedIndexes;
            int len=m_nvHardConstraintIndexes.size();
            if(len==0) break;
            int index;
            for(int i=0;i<len;i++){
                index=m_nvHardConstraintIndexes.get(i);
                m_cvConstraintNodes.get(index).checker.relaxConstraint();
            }
            m_pdPars=aNode.pars;
            doFit_Apache();
        }
        return 1;
    }
    int fixHardConstrainedTerms(){
        pickHardConstrainedIndexes();
        int len=m_nvHardConstraintIndexes.size();
        if(len==0) return -1;
        int nParsPerTerm=m_cFunc.m_nNumParsPerTerm,nBaseTerms=m_cFunc.m_nNumBasePars;
        int nTerms=(m_nPars-nBaseTerms),i,j,index,len1,index1,nTerm;
        int[] HardConstrainedTerms=new int[nTerms];

        for(i=0;i<nTerms;i++){
            HardConstrainedTerms[i]=-1;
        }

        ArrayList<Integer> ConstrainedIndexes;
        for(i=0;i<len;i++){
            index=m_nvHardConstraintIndexes.get(i);
            ConstrainedIndexes=m_cvConstraintNodes.get(index).function.getConstrainedParIndexes();
            len1=ConstrainedIndexes.size();
            for(j=0;j<len1;j++){
                index1=ConstrainedIndexes.get(j);
                nTerm=(index1-nBaseTerms)/nParsPerTerm;
                HardConstrainedTerms[nTerm]=1;
            }
        }

        ArrayList<Integer> nvHardConstrainedTerms=new ArrayList();
        int offset=nBaseTerms;
        ArrayList<Integer> nvFixedParIndexes=new ArrayList();

        len=m_pnFixedParIndexes.length;
        for(i=0;i<len;i++){
            nvFixedParIndexes.add(m_pnFixedParIndexes[i]);
        }

        for(nTerm=0;nTerm<nTerms;nTerm++){
            if(HardConstrainedTerms[nTerm]==1) {
                for(j=0;j<nParsPerTerm;j++){
                    nvFixedParIndexes.add(offset+j);
                }
            };
            offset+=nParsPerTerm;
        }

        nvFixedParIndexes=CommonStatisticsMethods.removeDuplicatedEliments(nvFixedParIndexes);

        len=nvFixedParIndexes.size();
        m_pnFixedParIndexes=new int[len];
        for(i=0;i<len;i++){
            m_pnFixedParIndexes[i]=nvFixedParIndexes.get(i);
        }
        return 1;
    }
    
    void expandConstraints(){
        int nPars=m_pdPars.length;
        ArrayList<ConstraintNode> cvNodes=m_cConstraintExpander.getConstraintNodes(nPars);
        int i,len=cvNodes.size();
        for(i=0;i<len;i++){
            m_cvConstraintNodes.add(cvNodes.get(i));
        }
        m_nNumConstraints=m_cvConstraintNodes.size();
    }
    public void setConstraints(ArrayList<ConstraintNode> cvConstraintNodes){
        m_cvConstraintNodes=cvConstraintNodes;
//        m_cConstraintExpander=cExpander;
        m_nNumConstraints=m_cvConstraintNodes.size();
    }
    public void setModelExpander(FittingModelExpander expander){
        m_cModelExpander=expander;
    }

    boolean improvedFitting(){
        int nModels=m_cFittingResultsNode.nModels;
        FittingModelNode model1=m_cFittingResultsNode.m_cvModels.get(nModels-2),model2=m_cFittingResultsNode.m_cvModels.get(nModels-1);
        double[] pdPars=model2.pdPars;
        double p0=0.01,p=0;
        switch (MinimizationOption){
            case Least_Square:
//                double SSE1=fun(pdPars);11427
                double SSE1=model1.SSE;
                double df1=model1.pdPars.length;
                double SSE2=model2.SSE;
                double df2=model2.pdPars.length;
                p=FittingComparison.getFittingComparison_LeastSquare(SSE1, df1, SSE2, df2, m_nPtrs);
                break;
            case Maximum_Likelihood://need to implement later 11707
//                double LL1=fun(pdPars);11428
                double LL1=m_dvFittedFunctionValues.get(nModels-1);
                df1=pdPars.length;
                double LL2=fun(m_pdAdjustablePars);
                df2=m_pdFittedPars.length;
                p=FittingComparison.getFittingComparison_MaximumLikelihood(df1, df2, LL2/LL1);
                break;
            default:
                p=1;
        }
        return (p<p0);
    }

    boolean improvedFittingo(){
        int nModels=m_pdvFittedPars.size();
        double[] pdPars=m_pdvFittedPars.get(nModels-1);
        double p0=0.01,p=0;
        switch (MinimizationOption){
            case Least_Square:
//                double SSE1=fun(pdPars);11427
                double SSE1=m_dvFittedFunctionValues.get(nModels-1);
                double df1=pdPars.length;
                double SSE2=fun(m_pdAdjustablePars);
                double df2=m_pdFittedPars.length;
                p=FittingComparison.getFittingComparison_LeastSquare(SSE1, df1, SSE2, df2, m_nPtrs);
                break;
            case Maximum_Likelihood:
//                double LL1=fun(pdPars);11428
                double LL1=m_dvFittedFunctionValues.get(nModels-1);
                df1=pdPars.length;
                double LL2=fun(m_pdAdjustablePars);
                df2=m_pdFittedPars.length;
                p=FittingComparison.getFittingComparison_MaximumLikelihood(df1, df2, LL2/LL1);
                break;
            default:
                p=1;
        }
        return (p<p0);
    }
    double[] pdPar0;//temporarily for debugging 11426
    public void update(double[][] pdX, double[] pdY, double[] pdPars, Fitting_Function func, int MinimizationOption, int MinimizationMethod,
            int nI, int nF, int nDelta, int[] pnFixedParIndexes){
        m_nIterations=0;
        m_nInterval=100;
        m_nPars=pdPars.length;
        m_nVars=pdX[0].length;
        m_pdX=pdX;
        m_pdY=pdY;
        m_pdPars=pdPars;
        m_cFunc=func;
        this.MinimizationOption=MinimizationOption;
        this.MinimizationMethod=MinimizationMethod;
        if(m_cFunc_Apache==null) m_cFunc_Apache=new fittingFunction();
        m_nI=nI;
        m_nF=nF;
        m_nPtrs=(nF-nI)/nDelta;
        checkOrigin();
        init(pdPars,pnFixedParIndexes);

//        m_cFunc_Apache.update(MinimizationOption, MinimizationMethod, pdX, pdY, pdPars, func,nDelta);

    }
    public void update(double[] pdPars, Fitting_Function func, int[] pnFixedParIndexes){
        m_pdPars=pdPars;
        m_cFunc=func;
        m_nPars=pdPars.length;
        init(pdPars,pnFixedParIndexes);
    }
    public int[] gexFixedParIndexes(){
        return m_pnFixedParIndexes;
    }
    void checkOrigin(){
        int i;
        if(m_pdOrigin==null){
            m_pdOrigin=new double[m_nVars];
            for(i=0;i<m_nVars;i++){
                m_pdOrigin[i]=0;
            }
        }else if(m_pdOrigin.length!=m_nVars){
            m_pdOrigin=new double[m_nVars];
            for(i=0;i<m_nVars;i++){
                m_pdOrigin[i]=0;
            }
        }

        if(m_pdXPrime==null){
            m_pdXPrime=new double[m_nVars];
            for(i=0;i<m_nVars;i++){
                m_pdXPrime[i]=0;
            }
        }else if(m_pdXPrime.length!=m_nVars){
            m_pdXPrime=new double[m_nVars];
            for(i=0;i<m_nVars;i++){
                m_pdXPrime[i]=0;
            }
        }
    }

    void updatePars(double[] pdAdjustablePars){
        int index,i;
        int nPars=pdAdjustablePars.length;
        if(m_nFittingModes!=FittingModelNode.CentralMode&&nPars!=m_nAdjustablePars+m_nFittingModes-1){
      //      IJ.error("inconsistent number of adjustable parameters for the fitting function");
//            Runtime.getRuntime().halt(1);
        }
        for(i=0;i<m_nAdjustablePars;i++){
            index=m_pnAdjustableParIndexes[i];
            m_pdPars[index]=pdAdjustablePars[i];
        }
        
        if(m_nFittingModes==-1){
            pdDev=CommonStatisticsMethods.getEmptyDoubleArray(pdDev, m_pdX.length);
            DoubleRange dr=new DoubleRange();
            calDev(pdDev,dr);
            double shift=dr.getMax();
            m_pdPars[0]+=shift;
            for(i=0;i<pdDev.length;i++){
                pdDev[i]-=shift;
            }
        }
    }
    void updateAdjustablePars(double[] pdAdjustablePars){
        int index,i;
        int nPars=pdAdjustablePars.length;
        if(m_nFittingModes!=FittingModelNode.CentralMode&&nPars!=m_nAdjustablePars+m_nFittingModes-1){
            IJ.error("inconsistent number of adjustable parameters for the fitting function");
//            Runtime.getRuntime().halt(1);
        }
        for(i=0;i<nPars;i++){
            index=m_pnAdjustableParIndexes[i];
            pdAdjustablePars[i]=m_pdPars[index];
        }
    }
    void updateFPGradient(){
        int index,i;
        for(i=0;i<m_nAdjustablePars;i++){
            index=m_pnAdjustableParIndexes[i];
            m_pdGradientFP[i]=m_pdGradient[index];
        }
    }
    void updateAdjustablePars(double[] pdPars, double[] pdAdjustablePars){
        int index,i;
        for(i=0;i<m_nAdjustablePars;i++){
            index=m_pnAdjustableParIndexes[i];
            pdAdjustablePars[i]=pdPars[index];
        }
    }
    public void setPausingIterations(int iter){
        m_nPausingIterations=iter;
    }
    public void terminateIterations(){
        updateFittingModel();
        opt.setMaxIterations(m_nIterations);
    }
    public double fun(double[] pars){
        if(m_nIterations>0&&m_nIterations%m_nPausingIterations==0){
            pauseFitting();
        }
        double dv=0,y,y0,dPenalty=0.;
        int i,j,it,xt=82,x,yi=0,yt=85;
        int w=m_pdX[0].length;
        updatePars(pars);
        int interval=100;
        /*
        if(MinimizationMethod==Non_Linear_Fitter.Simplex){
            m_nvViolatedIndexes.clear();
            m_dvPenalties.clear();
        }*/
        m_nIterations=opt.getIterations();
        ArrayList<Integer> nvViolatedIndexes=new ArrayList();
        ArrayList<Double> dvPenalties=new ArrayList();
        ArrayList<Double> dvDevs=new ArrayList();
        if(m_cDevRankingKeeper!=null) m_cDevRankingKeeper.reset();
        double dev;
        if(m_nFittingModes==1){
            switch (MinimizationOption){
                case Least_Square:
                    for(i=m_nI;i<=m_nF;i+=m_nDelta){
                        y0=m_pdY[i];
                        x=(int)(m_pdX[i][0]+0.5);
                        y=m_cFunc.fun(m_pdPars, m_pdX[i]);
                        dev=(y-y0)*(y-y0);
                        if(m_nNumOutliars>0){
                            m_cDevRankingKeeper.updateRankings(dev, i);
                            dvDevs.add(dev);
                        }
                        dv+=dev;
                    }
                    for(j=0;j<m_nNumOutliars;j++){
                        it=m_cDevRankingKeeper.getIndex(j);
                        dv-=dvDevs.get(it);
                    }
                    break;
                case Maximum_Likelihood:
                    for(i=m_nI;i<=m_nF;i+=m_nDelta){
                        y0=m_pdY[i];
    //                    y=-Math.log10(m_cFunc.fun(pars, m_pdX[i],y0));
    //                    dv+=y;
                    }
                    break;
                default:
                    break;
            }
        }else if(m_nFittingModes==FittingModelNode.CentralMode) {
            dv=fun_Mulitimodal(pars);
        } else if(m_nFittingModes==-1){
            dv=fun_UpperEnveloping(new double[m_pdX.length]);
        }else{
            dv=fun_Mulitimodalo(pars);
        }
        double dPenaltyT=0;
        for(i=0;i<m_nNumConstraints;i++){
            if(m_cvConstraintNodes.get(i).checker.ConstraintsViolated(pars)){
                dPenalty=m_cvConstraintNodes.get(i).function.getPenalty(pars);
                if(MinimizationMethod==Non_Linear_Fitter.Simplex&&dPenalty>0){
                    nvViolatedIndexes.add(i);
                    dvPenalties.add(dPenalty);
                }
            }
            else{
                dPenalty=0.;
            }
            dPenaltyT+=dPenalty;
        }
        dv+=dPenaltyT;
        if(m_nNumEvaluations==0){
            if(!CommonStatisticsMethods.regularDoubleArray(m_pdY)) 
                terminateIterations();
            utilities.CommonStatisticsMethods.copyArray(m_pdPars, pdPar0);
            m_pdBestPars=CommonStatisticsMethods.copyArray(pars);
            m_dBestScore=dv;
        }else{
            if(dv<m_dBestScore){
                utilities.CommonStatisticsMethods.copyArray(pars, m_pdBestPars);
            }
        }
//        m_cProcessMonitor.addStep(pars, dv, nvViolatedIndexes, dvPenalties, dPenaltyT, m_nIterations, m_nNumEvaluations);
        m_nNumEvaluations++;
        if(m_nNumEvaluations%interval==-20000){//not to have this block executed
            drawFunctionValues(m_pdPars);
            double[] gaussPars0=StandardFittingFunction.getGaussian2DParameters(pdPar0);
            double[] gaussPars=StandardFittingFunction.getGaussian2DParameters(m_pdPars);
            i=i;
            utilities.CommonStatisticsMethods.copyArray(m_pdPars, pdPar0);
        }
        if(!bStoring){
            synchronized (this) {
                if(bPause){
                    fireActionEvent(new ActionEvent(this,0,"Paused"));
                    while (bPause) {
                        try {
                            wait();
                        } catch (Exception e) {
                        }
                    }
                    fireActionEvent(new ActionEvent(this,0,"Running"));
                }
            }
        }

        if(m_nIterations>0&&m_nIterations%1000==0){
            IJ.showStatus("Fitting Iterations: "+m_nIterations);
            if(!bStoring){
                updateFittingModel();
                fireActionEvent(new ActionEvent(this,0,"Iterations "+m_nIterations));
            }
        }
        return dv;
    }
    void calDev(double[] pdDev,DoubleRange dr){
        int i,len=pdDev.length;
        if(len!=m_pdX.length) IJ.error("Inconsistent array lengthes in getDev");
        double dy=0;
        for(i=0;i<len;i++){
            dy=m_pdY[i]-m_cFunc.fun(m_pdPars, m_pdX[i]);
            dr.expandRange(dy);
            pdDev[i]=dy;
        }
    }
    double fun_UpperEnveloping(double[] pdDev){
        double dv=0,y,dy,dMax,y0;
        DoubleRange dr=new DoubleRange();
        calDev(pdDev,dr);
        double shift=dr.getMax();
        m_pdPars[0]+=shift;
        
        int i,ix;

        dv=0;
        int num=0;
        switch (MinimizationOption){
            case Least_Square:
                ix=0;
                dMax=Double.NEGATIVE_INFINITY;
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y=m_cFunc.fun(m_pdPars, m_pdX[i]);
                    dy=m_pdY[i]-y;       
                    if(dy>dMax){
                        dMax=dy;
                        ix=i;
                    }
                    dv+=y;
                    num++;
                }
                dv+=num*dMax;
                break;
            case Maximum_Likelihood:
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
                 }
                break;
            default:
                break;
        }
        return dv;
    }
    double fun_Mulitimodal(double[] pdPars){
        double dv=0,y,dy,dyn,y0,dZatP=GaussianDistribution.getZatP(1-m_dModePV, 0, m_dSignalSD, 0.001*m_dSignalSD);
        int i,nModes=m_nFittingModes-1,in,j,nPars=pdPars.length;
        ArrayList<Double> dvY=new ArrayList(),dvYn=new ArrayList(), dvYp=new ArrayList();

        double mean=0,ss=0,meanN=0,ssN=0,meanP=0,ssP=0;
        int n=0,nN=0,nP=0;
        switch (MinimizationOption){
            case Least_Square:
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
                    y=m_cFunc.fun(m_pdPars, m_pdX[i]);
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
                MeanSem0 ms=new MeanSem0();
                ms.updateMeanSquareSum(n, mean/n, ss/n);
                dv+=n*ms.getSD()*ms.getSD();
                ms.updateMeanSquareSum(nN, meanN/nN, ssN/nN);
                dv+=nN*ms.getSD()*ms.getSD();
                ms.updateMeanSquareSum(nP, meanP/nP, ssP/nP);
                dv+=nP*ms.getSD()*ms.getSD();
                break;
            case Maximum_Likelihood:
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
                 }
                break;
            default:
                break;
        }
        return dv;
    }
    double fun_Mulitimodalo(double[] pdPars){
        double dv=0,y,dy,dyn,y0;
        int i,nModes=m_nFittingModes-1,in,j,nPars=pdPars.length;
        switch (MinimizationOption){
            case Least_Square:
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
                    y=m_cFunc.fun(m_pdPars, m_pdX[i]);
                    dy=y0-y;
                    dyn=Math.abs(dy);
                    in=-1;
                    for(j=0;j<nModes;j++){
                        dy=Math.abs(y0-y-pdPars[nPars-nModes+j]);
                        if(dy<dyn){
                            dyn=dy;
                            in=j;
                        }
                    }
                    if(in!=-1) y+=pdPars[nPars-nModes+in];
                    dv+=(y-y0)*(y-y0);
                }
                break;
            case Maximum_Likelihood:
                m_cModePars.updateModePars(m_pdAdjustablePars);
                int num=(m_nF-m_nI)/m_nDelta+1;
                m_pdYT=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYT, num);
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    m_pdYT[i]=m_cFunc.fun(m_pdPars, m_pdX[i])-m_pdY[i];
                }
                /*                
                if(m_cKM==null) 
                    m_cKM=new OneDKMeans(m_pdYT,m_nFittingModes);
                else
                    m_cKM.updateData(m_pdYT,m_nFittingModes);
                double[] pdMeans=m_cKM.getMeans(),pdWeights=m_cKM.getWeights(),pdSDs=m_cModePars.pdSDs;
                m_cModePars.nMainComponent=m_cKM.getMainCluster();
                
                m_cModePars.setMeans(pdMeans);
                m_cModePars.setWeights(pdWeights);*/
                double[] pdMeans=m_cModePars.pdMeans,pdWeights=m_cModePars.pdWeights,pdSDs=m_cModePars.pdSDs;
                double factor=1/Math.sqrt(2.*Math.PI),sd;
                
                double dLik=0,p,x;
                dv=0;
                for(i=0;i<num;i++){
                    p=0;
                    y0=m_pdYT[i];
                    for(j=0;j<m_cModePars.nModes;j++){
                        sd=pdSDs[j];
                        y=pdMeans[j];
                        p+=(pdWeights[j]/sd)*Math.exp(-(y0-y)*(y0-y)/(2*sd*sd));
                    }
                    dv-=Math.log(p);
                }
                break;
            default:
                break;
        }
        return dv;
    }
    double fun_Mulitimodaloo(double[] pdPars){
        double dv=0,y,dy,dyn,y0;
        int i,nModes=m_nFittingModes-1,in,j,nPars=pdPars.length;
        switch (MinimizationOption){
            case Least_Square:
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
                    y=m_cFunc.fun(m_pdPars, m_pdX[i]);
                    dy=y0-y;
                    dyn=Math.abs(dy);
                    in=-1;
                    for(j=0;j<nModes;j++){
                        dy=Math.abs(y0-y-pdPars[nPars-nModes+j]);
                        if(dy<dyn){
                            dyn=dy;
                            in=j;
                        }
                    }
                    if(in!=-1) y+=pdPars[nPars-nModes+in];
                    dv+=(y-y0)*(y-y0);
                }
                break;
            case Maximum_Likelihood:
                m_cModePars.updateModePars(m_pdAdjustablePars);
                int num=(m_nF-m_nI)/m_nDelta+1;
                m_pdYT=CommonStatisticsMethods.getEmptyDoubleArray(m_pdYT, num);
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    m_pdYT[i]=m_cFunc.fun(m_pdPars, m_pdX[i])-m_pdY[i];
                }
                                
                if(m_cKM==null) 
                    m_cKM=new OneDKMeans(m_pdYT,m_nFittingModes);
                else
                    m_cKM.updateData(m_pdYT,m_nFittingModes);
                double[] pdMeans=m_cKM.getMeans(),pdWeights=m_cKM.getWeights(),pdSDs=m_cModePars.pdSDs;
                m_cModePars.nMainComponent=m_cKM.getMainCluster();
                
                m_cModePars.setMeans(pdMeans);
                m_cModePars.setWeights(pdWeights);
                double factor=1/Math.sqrt(2.*Math.PI),sd;
                
                double dLik=0,p,x;
                dv=0;
                for(i=0;i<num;i++){
                    p=0;
                    y0=m_pdYT[i];
                    for(j=0;j<m_cModePars.nModes;j++){
                        sd=pdSDs[j];
                        y=pdMeans[j];
                        p+=(pdWeights[j]/sd)*Math.exp(-(y0-y)*(y0-y)/(2*sd*sd));
                    }
                    dv-=Math.log(p);
                }
                break;
            default:
                break;
        }
        return dv;
    }
    public double[] gradient0(double[] pars){//this one was writen to check the methods gradient
        double dv=0,y,y0,delta,I=fun(pars),I1;
        double[] gradient=new double[m_nPars];
        double[] gradient1=gradient0(pars);
        int i,j,nPars=pars.length;
        double[] pars1=new double[nPars];
        for(i=0;i<nPars;i++){
            delta=Math.max(0.0000000001*pars[i], 0.0000000000001);
            CommonStatisticsMethods.copyArray(pars, pars1);
            pars1[i]+=delta;
            I1=fun(pars1);
            gradient[i]=(I1-I)/delta;
        }
        return gradient;
    }
    public double[] gradient(double[] pars){
        double dv=0,y,y0;
        double[] gradient=new double[m_nPars];
        int i,j;
        int w=m_pdX[0].length;
        updatePars(pars);
        m_nIterations=opt.getIterations();
        switch (MinimizationOption){
            case Least_Square:
                for(i=0;i<m_nPars;i++){
                    m_pdGradient[i]=0;
                }
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
                    y=m_cFunc.funValueAndGradient(m_pdPars,m_pdX[i],gradient);
                    for(j=0;j<m_nPars;j++){
                        m_pdGradient[j]+=2*(y-y0)*gradient[j];
                    }
                }
                break;
            case Maximum_Likelihood://not implemented
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
//                    y=-Math.log10(m_cFunc.fun(pars, m_pdX[i],y0));
//                    dv+=y;
                }
                break;
            default:
                break;
        }
        updateFPGradient();
        return m_pdGradientFP;
    }
    public double partialDerivative(double[] pars, int k){
        double dv=0,y,y0;
        double[] gradient=new double[m_nPars];
        int i,j,index=m_pnAdjustableParIndexes[k];
        updatePars(pars);
        m_nIterations=opt.getIterations();
        switch (MinimizationOption){
            case Least_Square:
                dv=0;
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
                    y=m_cFunc.fun(m_pdPars,m_pdX[i]);
                    dv+=2*(y-y0)*m_cFunc.funPartialDerivative(pars, m_pdX[i], index);
                }
                break;
            case Maximum_Likelihood://not implemented
                for(i=m_nI;i<=m_nF;i+=m_nDelta){
                    y0=m_pdY[i];
//                    y=-Math.log10(m_cFunc.fun(pars, m_pdX[i],y0));
//                    dv+=y;
                }
                break;
            default:
                break;
        }
        updateFPGradient();
        return dv;
    }
    void drawFunctionValues(double[] pars){
        updatePars(pars);
        int i,x,y=0;
        int xt=82,yt=85;//for debugging
        double dv;
        ArrayList<Double> dvValues=new ArrayList();
        ArrayList<Point> points=new ArrayList();
        int w=m_pdX[0].length;
        for(i=m_nI;i<=m_nF;i+=m_nDelta){
            x=(int)(m_pdX[i][0]+0.5);
            if(w>1) y=(int)(m_pdX[i][1]+0.5);
            points.add(new Point(x,y));
            if(x==xt&&y==yt){
                i=i;
            }
            dv=m_cFunc.fun(m_pdPars, m_pdX[i]);
            dvValues.add(dv);
        }
        CommonMethods.showFunctionValues(implFunc,points,dvValues);
    }
    protected void doFit(){
        m_nNumEvaluations=0;
        switch (MinimizationMethod){

            case Simplex:
                Simplex cSimplex=new Simplex(m_pdAdjustablePars,this);
                double fTol=0.000001;
                cSimplex.amoeba(fTol);
                updatePars(cSimplex.getFittedPars());
                m_pdFittedPars=m_pdPars;
                break;
            default:
                IJ.error("undefined minimization method"+ "("+MinimizationMethod+")");
                break;
        }
    }
    public double[] getFittedPars(){
        return m_pdFittedPars;
    }
    public void doFit_Apache(int nIterations){
        int nMaxIterations=m_nPausingIterations;
        m_nPausingIterations=nIterations;
        doFit_Apache();
        updateFittingModel();
        m_nPausingIterations=nMaxIterations;
    }
    protected void doFit_Apache(){
//        NelderMead opt= new NelderMead();
        m_cProcessMonitor=new OptimizationProcessMonitor(m_nAdjustablePars,m_cvConstraintNodes);
        opt= new NelderMead();
        SimpleScalarValueChecker checker=new SimpleScalarValueChecker(10000*m_dFTol,100*m_dTol);
        opt.setConvergenceChecker(checker);
        m_nIterations=0;
        m_nIterations0=0;
        m_nNumEvaluations=0;
        Simplex cSimplex=new Simplex(m_pdAdjustablePars,this,m_dSimplexScale);
        m_pdSimplex=cSimplex.getSimplex();
        m_pdSimplex0=CommonStatisticsMethods.copyArray(m_pdSimplex);
        opt.setStartConfiguration(m_pdSimplex);
        opt.setMaxIterations(m_nMaxIterations);
        int maxIter=opt.getMaxIterations();
        int maxEval=opt.getMaxEvaluations();
        m_nIterations=opt.getIterations();
        m_nIterations0=m_nIterations;
        m_nNumEvaluations=0;
        m_dBestScore=Double.POSITIVE_INFINITY;

        startTime=System.currentTimeMillis();
                try{
                    pvp=opt.optimize(m_cFunc_Apache, GoalType.MINIMIZE, m_pdAdjustablePars);}
                    catch (org.apache.commons.math.FunctionEvaluationException e){
                    IJ.error(""+e);
                }
                catch (org.apache.commons.math.optimization.OptimizationException e){
        //            IJ.error(""+e);
                }

                endTime=System.currentTimeMillis();
                if(pvp!=null){
                    CommonStatisticsMethods.copyArray(pvp.getPoint(), m_pdAdjustablePars);
                    updatePars(pvp.getPoint());
                     fireActionEvent(new ActionEvent(this,0,"Finished"));
                }else{//reached iterations or evaluations
                    CommonStatisticsMethods.copyArray(m_pdBestPars, m_pdAdjustablePars);
                    updatePars(m_pdBestPars);
                    fireActionEvent(new ActionEvent(this,0,"Terminated"));
                }

                updateFittingModel();
        m_pdFittedPars=m_pdPars;
    }
     protected void doFit_ConjugateGradient(){
        optCG=new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.FLETCHER_REEVES);
        SimpleScalarValueChecker checker=new SimpleScalarValueChecker(m_dFTol,m_dTol);
        optCG.setConvergenceChecker(checker);
        m_nIterations=0;
        m_nIterations0=0;
        m_nNumEvaluations=0;
        optCG.setInitialStep(0.1);
        optCG.setMaxIterations(m_nMaxIterations);
        int maxIter=opt.getMaxIterations();
        int maxEval=opt.getMaxEvaluations();
        m_nIterations=optCG.getIterations();
        m_nIterations0=m_nIterations;
        m_nNumEvaluations=0;

        m_cFunc_ConjugatedGradient=new DifferentiableFittingFunction();
        if(m_cFunc instanceof ComposedFittingFunction){
            ArrayList<String> svFunctionTypes=m_cFunc.copyComponentFunctionTypes();
            ComposedFittingFunction.toGaussian2D(m_pdPars,svFunctionTypes,nvTransformedGaussianComponents);
            m_cFunc=new ComposedFittingFunction(svFunctionTypes);
            updateAdjustablePars(m_pdAdjustablePars);
        }
        m_dBestScore=Double.POSITIVE_INFINITY;

        startTime=System.currentTimeMillis();
                try{
                    pvp=optCG.optimize(m_cFunc_ConjugatedGradient, GoalType.MINIMIZE, m_pdAdjustablePars);}
                    catch (org.apache.commons.math.FunctionEvaluationException e){
                    IJ.error(""+e);
                }
                catch (org.apache.commons.math.optimization.OptimizationException e){
        //            IJ.error(""+e);
                }

                endTime=System.currentTimeMillis();
                if(pvp!=null){
                    CommonStatisticsMethods.copyArray(pvp.getPoint(), m_pdAdjustablePars);
                    updatePars(pvp.getPoint());
                     fireActionEvent(new ActionEvent(this,0,"Finished"));
                }else{//reached iterations or evaluations
                    CommonStatisticsMethods.copyArray(m_pdBestPars, m_pdAdjustablePars);
                    updatePars(m_pdBestPars);
                    fireActionEvent(new ActionEvent(this,0,"Terminated"));
                }

                updateFittingModel();
        m_pdFittedPars=m_pdPars;
    }
    boolean fitEntireDataPoints(){
        return (m_nI==0&&m_nF==m_pdY.length-1&&m_nDelta==1);
    }
    public void doFit_LevenbergMarquardt(){
        optLM=new LevenbergMarquardtOptimizer();
//        SimpleScalarValueChecker checker=new SimpleScalarValueChecker(m_dFTol,m_dTol);
//        optLM.setConvergenceChecker(checker);
        m_nIterations=0;
        m_nIterations0=0;
        m_nNumEvaluations=0;
        optLM.setMaxIterations(m_nMaxIterations);
        int maxIter=optLM.getMaxIterations();
        int maxEval=optLM.getMaxEvaluations();
        m_nIterations0=m_nIterations;
        m_nNumEvaluations=0;

        m_cFunc_LevenbergMarquardt=new DifferentiableVectorialFittingFunction();
        /*//this transformation should be done by callers
        if(m_cFunc instanceof ComposedFittingFunction){
            ArrayList<String> svFunctionTypes=m_cFunc.copyComponentFunctionTypes();
            ComposedFittingFunction.toGaussian2D(m_pdPars,svFunctionTypes,nvTransformedGaussianComponents);
            m_cFunc=new ComposedFittingFunction(svFunctionTypes);
        }*/
        updateAdjustablePars(m_pdAdjustablePars);

        int nData=m_pdY.length,i;
        double[] pdY=m_pdY;
        if(!fitEntireDataPoints()){
            nData=(m_nF-m_nI)/m_nDelta+1;
            pdY=new double[nData];
            int num=0;
            for(i=m_nI;i<=m_nF;i+=m_nDelta){
                pdY[num]=m_pdY[i];
            }
        }
        double[] weight=new double[nData];
        for(i=0;i<nData;i++){
            weight[i]=1.;
        }
        m_dBestScore=Double.POSITIVE_INFINITY;
        m_pdBestPars=CommonStatisticsMethods.copyArray(m_pdAdjustablePars);
        startTime=System.currentTimeMillis();
                try{
                    vpvp=optLM.optimize(m_cFunc_LevenbergMarquardt,pdY,weight,m_pdAdjustablePars);}
                    catch (org.apache.commons.math.FunctionEvaluationException e){
                    IJ.error(""+e);
                }
                catch (org.apache.commons.math.optimization.OptimizationException e){
        //            IJ.error(""+e);
                }

                endTime=System.currentTimeMillis();
                if(vpvp!=null){
                    CommonStatisticsMethods.copyArray(vpvp.getPoint(), m_pdAdjustablePars);
                    updatePars(m_pdAdjustablePars);
                    fireActionEvent(new ActionEvent(this,0,"Finished"));
                }else{//reached iterations or evaluations
                    CommonStatisticsMethods.copyArray(m_pdBestPars, m_pdAdjustablePars);
                    updatePars(m_pdBestPars);
                    fireActionEvent(new ActionEvent(this,0,"Terminated"));
                }
                m_nIterations=optLM.getIterations();

                updateFittingModel();
        m_pdFittedPars=m_pdPars;
    }

    void init(double[] pdPars){
//        if(m_nPars!=pdPars.length){
//            IJ.error("inconsistent number of parameters for ininialization");
//        }
        m_nPars=pdPars.length;
        m_pdPars=new double[m_nPars];
        for(int i=0;i<m_nPars;i++){
            m_pdPars[i]=pdPars[i];
        }
        m_pdGradient=new double[m_nPars];
    }
    public void setOrigin(double[] pdOrigin){
        m_cFunc.seOrigin(pdOrigin);
    }

    public void init(double[] pdPars, int[] pnFixedParIndexes){
        init(pdPars);
        m_pnFixedParIndexes=pnFixedParIndexes;
        int nFixedPars=0;
        if(pnFixedParIndexes!=null)
            nFixedPars=pnFixedParIndexes.length;
        else
            nFixedPars=0;

        m_nAdjustablePars=m_nPars-nFixedPars;

        m_pdGradientFP=new double[m_nAdjustablePars];

        int[] indexes=new int[m_nPars];
        int i;
        for(i=0;i<m_nPars;i++){
            indexes[i]=0;
        }
        for(i=0;i<nFixedPars;i++){
            indexes[pnFixedParIndexes[i]]=1;
        }
        int index=0;
        m_pnAdjustableParIndexes=new int[m_nAdjustablePars];
        m_pdAdjustablePars=new double[m_nAdjustablePars];
        pdPar0=new double[m_pdPars.length];//temporarily for debugging 11426
        for(i=0;i<m_nPars;i++){
            if(indexes[i]==0) {
                m_pnAdjustableParIndexes[index]=i;
                m_pdAdjustablePars[index]=m_pdPars[i];
                index++;
            }
        }
    }
    public void storeFittedResults(){
        if(m_cFittingResultsNode.m_cvModels.isEmpty()){
            m_cFittingResultsNode=new FittingResultsNode(m_cStorageModel);
        }else{
            m_cFittingResultsNode.addModel(m_cStorageModel);
        }
    }
    public void setFunctionImage(ImagePlus impl){
        implFunc=impl;
    }
    public FittingResultsNode getFittingReults(){
        return m_cFittingResultsNode;
    }
    public void setImageShape(int[] pnDrawingVarIndexes){
        if(pnDrawingVarIndexes==null){
            pnDrawingVarIndexes=new int[2];
            pnDrawingVarIndexes[0]=0;
            pnDrawingVarIndexes[1]=1;
        }
        m_pnDrawingVarIndexes=CommonStatisticsMethods.copyArray(pnDrawingVarIndexes);
        m_cIS=ImageShapeHandler.buildDrawingImageShape(m_pdX, pnDrawingVarIndexes, 2000, m_nI, m_nF, m_nDelta);
    }
    public ImageShape getImageShape(){
        return m_cIS;
    }
    public void addFitterListener(ActionListener al){
        m_cvListeners.add(al);
    }
    void fireActionEvent(ActionEvent ae){
        int len=m_cvListeners.size(),i;
        ActionListener al;
        for(i=0;i<len;i++){
            al=m_cvListeners.get(i);
            if(al==null) continue;
            al.actionPerformed(ae);
        }
    }
    public static FittingResultsNode getFittedModel(FittingModelNode cIModel, FittingModelNode cStorageModel, double simplexScale, ActionListener al){
        return getFittedModel(cIModel,cStorageModel,simplexScale,al,false);
    }
    public static FittingResultsNode getFittedModel(FittingModelNode cIModel, FittingModelNode cStorageModel, double simplexScale, ActionListener al,boolean lo){
        cIModel.makeHistFittingReady(cIModel.nSmoothingWS);
        cIModel.toGaussian2D();
        cStorageModel.toGaussian2D();
        if(cIModel.getNumFreePars()<=0) return new FittingResultsNode(cIModel);
        Non_Linear_Fitter fitter=new Non_Linear_Fitter(cIModel.m_pdX,cIModel.m_pdY,cIModel.pdPars,new ComposedFittingFunction(cIModel.svFunctionTypes),cIModel.MinimizationOption,cIModel.MinimizationMethod,cIModel.nI,cIModel.nF,cIModel.nDelta,cIModel.pnFixedParIndexes,cStorageModel,simplexScale);
        if(al!=null) fitter.addFitterListener(al);
//        if(cIModel.cvConstraintNodes!=null) fitter.setConstraints(cIModel.cvConstraintNodes);
        if(lo) fitter.setLoConvergence();
        fitter.doFit_Apache();
//        fitter.doFit_ConjugateGradient();
//        fitter.doFit_LevenbergMarquardt();
        fitter.storeFittedResults();
        return fitter.getFittingReults();
    }
    public static FittingResultsNode getFittedModel_Simplex(FittingModelNode cIModel, FittingModelNode cStorageModel, double simplexScale, ActionListener al){
        return getFittedModel_Simplex(cIModel,cStorageModel,simplexScale,al,false);
    }
    public static FittingResultsNode getFittedModel_Simplex(FittingModelNode cIModel, FittingModelNode cStorageModel, double simplexScale, ActionListener al,boolean lo){
        cIModel.makeHistFittingReady(cIModel.nSmoothingWS);
        cIModel.toGaussian2D();
        cStorageModel.toGaussian2D();
        if(cIModel.getNumFreePars()<=0) return new FittingResultsNode(cIModel);
        Non_Linear_Fitter fitter=new Non_Linear_Fitter(cIModel.m_pdX,cIModel.m_pdY,cIModel.pdPars,new ComposedFittingFunction(cIModel.svFunctionTypes),cIModel.MinimizationOption,cIModel.MinimizationMethod,cIModel.nI,cIModel.nF,cIModel.nDelta,cIModel.pnFixedParIndexes,cStorageModel,simplexScale);
        if(cIModel.nModes!=1) fitter.setAsMultimodalFitting(cIModel.cModelPars);
        if(cIModel.nNumOutliars>0) fitter.setNumOutliars(cIModel.nNumOutliars);
        if(al!=null) fitter.addFitterListener(al);
//        if(cIModel.cvConstraintNodes!=null) fitter.setConstraints(cIModel.cvConstraintNodes);
        if(lo) fitter.setLoConvergence();
        fitter.doFit_Apache();
//        fitter.doFit_ConjugateGradient();
//        fitter.doFit_LevenbergMarquardt();
        fitter.storeFittedResults();
        return fitter.getFittingReults();
    }
    int setAsMultimodalFitting(ModePars cModePars){
        m_cModePars=cModePars;
        m_nFittingModes=cModePars.nModes;
        m_dModePV=0.01;
        freezePar(0);
        m_pdAdjustablePars=cModePars.getExpandedAdjustablePars(m_pdAdjustablePars);
        return 1;
    }
    int freezePar(int index){
        if(m_pnFixedParIndexes==null) {
            m_pnFixedParIndexes=new int[1];
            m_pnFixedParIndexes[0]=index;
            init(m_pdPars,m_pnFixedParIndexes);
           return 1;
        }
        if(!isFreePar(index)) return 1;
        int i,len=m_pnFixedParIndexes.length,num=0;
        int pnT[]=new int[len+1];
        for(i=0;i<len;i++){
            if(m_pnFixedParIndexes[i]<index)
                pnT[num]=m_pnFixedParIndexes[i];
            else {
                if(num==i){
                    pnT[num]=index;
                    num++;
                }
                pnT[num]=m_pnFixedParIndexes[i];
            }
        }
        init(m_pdPars,m_pnFixedParIndexes);
        return 1;
    }
    
    boolean isFreePar(int index){
        if(m_pnFixedParIndexes==null) return true;
        int i,len=m_pnFixedParIndexes.length;
        for(i=0;i<len;i++){
            if(m_pnFixedParIndexes[i]==index) return false;
        }
        return true;
    }
    int setAsMultimodalFittingO(int nModes){
        m_nFittingModes=nModes;
        m_dModePV=0.01;
        if(nModes>1){
 //           double[] pdT=CommonStatisticsMethods.copyArray(m_pdAdjustablePars);
            double[] pdT=new double[m_nAdjustablePars];
            int len=pdT.length,i;
            for(i=0;i<m_nAdjustablePars;i++){
                pdT[i]=m_pdAdjustablePars[i];
            }

            m_pdAdjustablePars=new double[m_nAdjustablePars+nModes-1];
            for(i=0;i<m_nAdjustablePars;i++){
                m_pdAdjustablePars[i]=pdT[i];
            }

            len=m_pdY.length;
            double pdDelta[]=new double[len-2];
            for(i=0;i<len-2;i++){
                pdDelta[i]=m_pdY[i]-m_pdY[i+2];
            }

            MeanSem1 ms=new MeanSem1();
            ArrayList<Integer> nvOutliarIndexes=new ArrayList();
            CommonStatisticsMethods.findOutliars(pdDelta, m_dModePV, ms, nvOutliarIndexes);
            double sd=ms.getSD();
            m_dSignalSD=sd/Math.sqrt(2);
            if(nModes==FittingModelNode.CentralMode)
            sd/=(nModes-1);
            int index=m_nAdjustablePars;
            for(i=0;i<(nModes-1)/2;i++){
                m_pdAdjustablePars[index]=-(i+1)*sd;
                index++;
                m_pdAdjustablePars[index]=(i+1)*sd;
                index++;
            }
            if(index<m_pdAdjustablePars.length) m_pdAdjustablePars[index]=-(Math.max(1, i))*sd;
        }
        return 1;
    }
}
