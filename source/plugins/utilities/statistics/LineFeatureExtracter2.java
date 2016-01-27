/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import FluoObjects.IPOAnalyzerForm;
import ij.gui.PlotWindow;
import utilities.statistics.LevelTransitionDetector_LevelComparison;
import java.util.ArrayList;
import java.awt.Color;
import utilities.CommonMethods;
import utilities.CommonGuiMethods;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.intRange;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.statistics.Histogram;
import utilities.Non_LinearFitting.LineEnveloper;
import utilities.statistics.RunningWindowRegressionLiner;
import utilities.Non_LinearFitting.StraightlineFitter;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CustomDataTypes.DoubleRange;
import utilities.statistics.PolynomialRegression;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import utilities.statistics.LineFitter_AdaptivePolynomial;
import utilities.statistics.LineSegmentRegressionEvaluater;
import utilities.statistics.SignalTransitionDetector;
import utilities.statistics.*;
import utilities.Gui.PlotWindowPlus;
import utilities.statistics.OneDKMeans;
import utilities.statistics.OneDNearestNeighbor;
import utilities.Gui.AnalysisMasterForm;
import utilities.QuickSort;
import utilities.io.PrintAssist;
import utilities.statistics.PolynomialRegression;
import utilities.statistics.SpikeHandler;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.OneDKMeans_ExtremExcluded;
import utilities.CustomDataTypes.IntPair;
import utilities.Non_LinearFitting.Constrains.ParValidityChecker;

/**
 *
 * @author Taihao
 */
public class LineFeatureExtracter2 {    
    class PNParChecker extends ParValidityChecker{
        public PNParChecker(){
            
        }
        public boolean invalidPars(double[] pars, double xi, double xf){
            if(pars.length!=3) return false;
            double dAx=0.1;
            if(Math.abs(pars[2])>dAx) {
//            if(pars[2]<0) {
                double xp=(-pars[1]/(2*pars[2]));                
                if(xp>xi&&xp<xf) return true;
            }            
            return false;
        }
        public boolean invalidPars(PolynomialLineFittingSegmentNode seg,double xi, double xf){
            double[] pars=seg.getPars();
            double sig=0;
            int start=seg.getStart(0, 1),end=seg.getStart(0, -1),minLen=5;
            if(start==47&&end==66){
                start=start;
            }
            
            double dP=0.001,dPChiSQ=0.5;
/*&            if(seg.dSigChiSQ<dPChiSQ) {
                if(seg.pbSelected==pbSemiballContactingLx_Downward)
                    sig=LineSegmentRegressionEvaluater.getDevSignificance_Chisquare(seg.cPr, seg.pdX, seg.pdY,seg.pdSD,pbSemiballContactingConvex_Downward,seg.nStart,seg.nEnd);
                else if(seg.pbSelected==pbSemiballContactingConvex_Downward)
                    sig=LineSegmentRegressionEvaluater.getDevSignificance_Chisquare(seg.cPr, seg.pdX, seg.pdY,seg.pdSD,pbSemiballContactingLx_Downward,seg.nStart,seg.nEnd);
                if(sig<dPChiSQ) return true;
            }*/
            if(seg.dSigChiSQ<dPChiSQ) return true;
            for(int i=start;i<=end;i++){
                if(pdBallContactingPointsDevPValues_Downward[i]<dP) return true;
            }
//            if(seg.cPr.nDataSize<minLen) return true;
//            if(!CommonStatisticsMethods.isLocalExtrema(seg.pdY, start, 1)&&!seg.pbSelected[start+1]) return true;
//            if(!CommonStatisticsMethods.isLocalExtrema(seg.pdY, end, 1)&&!seg.pbSelected[end-1]) return true;
            if(pars.length!=3) return false;
//            double sd=seg.getSD();
            double sd=pdSD[(start+end)/2];
            double dAx=0.2;
            double xp=(-pars[1]/(2*pars[2])); 
            double[] pdX=seg.pdX;
            if(((xp-pdX[start])*(xp-pdX[end])<0)) return true;
            if(Math.abs(pars[2]/sd)>dAx) {                               
                if(xp>xi&&xp<xf) return true;
            }            
            return false;
        }
     }
     
     class SignalTransitionStatisticsTests{
        public PolynomialLineFittingSegmentNode segL, segR, segT;//
        public int position,ll,lr,rl,rr;
        public int numTests;
        public ArrayList<Integer> nvSelectedPositionsL,nvSelectedPositionsR;
//        public String[] psTestNames={"TTest","chiSquareTest","MannWhitneyUTest","CorrectedT","CorrectedChiSQ","CorrectedMW","JumpSig","ModelCompSig","Projection","ProjectionMW"};
        public String[] psTestNames={"TTest","MannWhitneyUTest","CorrectedT","CorrectedMW","ConnectionSig","JumpSig","ModelCompSig","Projection","ProjectionMW"};
//        public String[] psTestNames={"TTest","chiSquareTest","MannWhitneyUTest","CorrectedT","CorrectedChiSQ","CorrectedMW","JumpSig","Projection","ProjectionMW"};
        public double[] pdPValues;
        public double dSD;
        int nMaxLen;
        SignalTransitionStatisticsTests(){      
            nvSelectedPositionsL=new ArrayList();
            nvSelectedPositionsR=new ArrayList();
            nMaxLen=30;           
        }   
        SignalTransitionStatisticsTests(int position){     
                        
            this();
            
            int flag=pnHighDiffClusterPosition[position];
            if(CommonMethods.getDigit(flag,3)==1) flag/=10;//to temporally bypass the first block.
            if(flag==111) {
                flag=flag;
            }
            
            int segLen=20,i,left,right,rl;
            ArrayList<Integer> positions=new ArrayList();
            left=Math.max(0,position-segLen);
            rl=Math.min(position+nMaxRisingInterval,nDataSize-1);
            
            right=Math.min(rl+segLen, nDataSize-1);
            CommonStatisticsMethods.getNonZeroPositionsWithinRange(pnHighDiffClusterPosition, position-1, -1, new intRange(Math.max(left-nMaxRisingInterval,0),position), positions);
            if(!positions.isEmpty()) left=positions.get(0)+nMaxRisingInterval;
            CommonStatisticsMethods.getNonZeroPositionsWithinRange(pnHighDiffClusterPosition, rl, 1, new intRange(rl,right), positions);
            if(!positions.isEmpty()) right=positions.get(0);
            if(rl>right){
                rl=rl;
            }
            calTests(position,left,position,rl,right);
       }
       SignalTransitionStatisticsTests(int position, int ll, int lr, int rl, int rr){
           this();
            calTests(position,ll,lr,rl,rr);
        }
        void calTests(int position, int ll, int lr, int rl, int rr){
            if(lr-ll>nMaxLen) ll=lr-nMaxLen+1;
            if(rr-rl>nMaxLen) rr=rl+nMaxLen-1;
            this.position=position;
            if(position==134) {
                position=position;
            }
            this.ll=ll;
            this.lr=lr;
            this.rl=rl;
            this.rr=rr;
//            dSD=cMsDeltaYE.getSD()/Math.sqrt(2.);
            dSD=Math.max(cMsDeltaYE.getSD()/Math.sqrt(2.),CommonStatisticsMethods.getMean(pdSD, null, ll, rr, 1));
            numTests=psTestNames.length;
            int nExt=20;
            double dTTest=1.1,dChiSQ=1.1,dMW=1.1,dJump=1.1,dModelComp=1.1,dProjection=1.1,dProjectionMW=1.1,dCorrectedT=1.1,dCorrectedChiSQ=1.1,dCorrectedMW=1.1,dConnection=1.1;
            double shift=0,sigt;
            segL=buildLineSegment(ll,lr);
            segR=buildLineSegment(rl,rr);
            
            segT=buildLineSegment(ll,rr);
           
            ArrayList <Double> dvL=CommonStatisticsMethods.copySelectedDataToArrayList(pdY, pbSelected, ll, lr, 1),dvR=CommonStatisticsMethods.copySelectedDataToArrayList(pdY, pbSelected, rl, rr, 1);
            ArrayList <Double> dvDevL=new ArrayList(),dvDevR=new ArrayList(),dvDevProjectionL=new ArrayList(),dvDevProjectionR=new ArrayList(),dvDevConnectionL=new ArrayList(),dvDevConnectionR=new ArrayList();
            
//            if(segL.isValid()){ 
                CommonStatisticsMethods.getSimpleRegressionDev(segL.cPr, pdX, pdY,pbSelected,ll,lr,nvSelectedPositionsL,dvDevL);
                CommonStatisticsMethods.getSimpleRegressionDev(segL.cPr, pdX, pdY,pbSelected,rl,Math.min(rl+nExt, rr),nvSelectedPositionsR,dvDevProjectionL);
//            }
            
//            if(segR.isValid()){ 
                CommonStatisticsMethods.getSimpleRegressionDev(segR.cPr, pdX, pdY,pbSelected,rl,rr,nvSelectedPositionsR,dvDevR);
                CommonStatisticsMethods.getSimpleRegressionDev(segR.cPr, pdX, pdY,pbSelected,Math.max(lr-nExt, ll),lr,nvSelectedPositionsR,dvDevProjectionR);
//            }
                CommonStatisticsMethods.trimDoubleArrayList(dvDevConnectionL,3,true);
                CommonStatisticsMethods.trimDoubleArrayList(dvDevConnectionR,3,true);
                CommonStatisticsMethods.getSimpleRegressionDev(segT.cPr, pdX, pdY,pbSelected,ll,lr,nvSelectedPositionsL,dvDevConnectionL);
                CommonStatisticsMethods.getSimpleRegressionDev(segT.cPr, pdX, pdY,pbSelected,rl,Math.min(rl+nExt, rr),nvSelectedPositionsR,dvDevConnectionR);
/*            CommonStatisticsMethods.getSimpleRegressionDev_SameSideNeighbors(segT.cPr,pdX, pdY,pbFullSelection, lr, ir,nvSelectedPositionsL, dvDevConnectionL);            
            CommonStatisticsMethods.getSimpleRegressionDev_SameSideNeighbors(segT.cPr,pdX, pdY,pbFullSelection, rl, ir,nvSelectedPositionsR, dvDevConnectionR); 
                int lConnection=2;
                for(int i=0;i<lConnection;i++){
                    position=lr-i;
                    if(position<0) continue;
                    dvDevConnectionL.add(pdY[position]-segT.predict(pdX[position]));
                    position=rl+i;
                    if(position>=nDataSize) continue;
                    dvDevConnectionR.add(pdY[position]-segT.predict(pdX[position]));
                }*/
            
            if(!dvL.isEmpty()&&!dvR.isEmpty()) dTTest=HypothesisTester.tTestGivenSD(dvL, dvR,dSD);
//            dChiSQ=HypothesisTester.chiSquareTestDataSetsComparison(dvL, dvR);
            if(!dvL.isEmpty()&&!dvR.isEmpty()) dMW=HypothesisTester.MannWhitneyUTest(dvL, dvR);
                
            if(segL.isValid()&&segR.isValid()){
                dJump=LineSegmentRegressionEvaluater.getSignalJumpSignificance_ModelComparison(segL, segR, nOrder, null);
                dModelComp=LineSegmentRegressionEvaluater.getDifferenceSignificance_ModelComparison(segL, segR);
//                dCorrectedChiSQ=LineSegmentRegressionEvaluater.getDifferenceSignificance_ChiSQ(segL,segR);
                dProjection=HypothesisTester.generalTTest(dvDevL, dvDevProjectionL, dSD);
                sigt=HypothesisTester.tTestGivenSD(dvDevR, dvDevProjectionR, dSD);
                if(sigt>dProjection) dProjection=sigt;
                
                dProjectionMW=HypothesisTester.MannWhitneyUTest(dvDevL, dvDevProjectionL);
                sigt=dProjectionMW=HypothesisTester.MannWhitneyUTest(dvDevR, dvDevProjectionR);
                if(sigt>dProjectionMW) dProjectionMW=sigt;                
                
                shift=segL.predict(pdX[lr])-segR.predict(pdX[rl]);                
                dCorrectedT=HypothesisTester.generalTTest(dvDevL, CommonStatisticsMethods.copyDoubleArrayList(dvDevR, shift), dSD);
                sigt=HypothesisTester.tTestGivenSD(dvDevR, CommonStatisticsMethods.copyDoubleArrayList(dvDevR, -shift), dSD);
                if(sigt>dCorrectedT) dCorrectedT=sigt;
                
                dCorrectedMW=HypothesisTester.MannWhitneyUTest(dvDevL, CommonStatisticsMethods.copyDoubleArrayList(dvDevR, shift));
                sigt=dCorrectedMW=HypothesisTester.MannWhitneyUTest(dvDevR, CommonStatisticsMethods.copyDoubleArrayList(dvDevL, -shift));
                if(sigt>dCorrectedMW) dCorrectedMW=sigt;
            } else if(segL.isValid()){
                dProjection=HypothesisTester.tTestGivenSD(dvDevL, dvDevProjectionL, dSD);
                if(!dvDevL.isEmpty()&&!dvDevProjectionL.isEmpty()) dProjectionMW=HypothesisTester.MannWhitneyUTest(dvDevL, dvDevProjectionL);
                shift=segL.predict(pdX[lr])-segR.predict(pdX[rl]);                
                dCorrectedT=HypothesisTester.tTestGivenSD(dvDevL, CommonStatisticsMethods.copyDoubleArrayList(dvDevR, shift), dSD);
                if(!dvDevL.isEmpty()&&!dvDevR.isEmpty()) dCorrectedMW=HypothesisTester.MannWhitneyUTest(dvDevL, CommonStatisticsMethods.copyDoubleArrayList(dvDevR, shift));
            }  else if(segR.isValid()){
                dProjection=HypothesisTester.tTestGivenSD(dvDevR, dvDevProjectionR, dSD);
                if(!dvDevR.isEmpty()&&!dvDevProjectionR.isEmpty()) dProjectionMW=HypothesisTester.MannWhitneyUTest(dvDevR, dvDevProjectionR);
                shift=segL.predict(pdX[lr])-segR.predict(pdX[rl]);                
                dCorrectedT=HypothesisTester.tTestGivenSD(dvDevR, CommonStatisticsMethods.copyDoubleArrayList(dvDevL, -shift), dSD);
                if(!dvDevL.isEmpty()&&!dvDevR.isEmpty()) dCorrectedMW=HypothesisTester.MannWhitneyUTest(dvDevR, CommonStatisticsMethods.copyDoubleArrayList(dvDevL, -shift));
            }   
            if(segT.isValid())dConnection=HypothesisTester.tTestGivenSD(dvDevConnectionL,dvDevConnectionR,dSD);
                
            pdPValues=new double[numTests];
            int index=0;
            pdPValues[index]=dTTest;
            index++;
//            pdPValues[index]=dChiSQ;
//            index++;
            pdPValues[index]=dMW;
            index++;
            pdPValues[index]=dCorrectedT;
            index++;
//            pdPValues[index]=dCorrectedChiSQ;
//            index++;
            pdPValues[index]=dCorrectedMW;
            index++;
            pdPValues[index]=dConnection;
            index++;
            pdPValues[index]=dJump;
            index++;
            pdPValues[index]=dModelComp;
            index++;
            pdPValues[index]=dProjection;
            index++;
            pdPValues[index]=dProjectionMW;
        }
        PolynomialLineFittingSegmentNode buildLineSegment(int start, int end){
            boolean pbt[]=m_cSignalFitter.getDataSelection();
            m_cSignalFitter.setSelection(pbSelected);
            PolynomialLineFittingSegmentNode seg=m_cSignalFitter.getLineSegment(start,end);
            m_cSignalFitter.setSelection(pbt);
            return seg;
        }
        public intRange getLeftRange(){
            return new intRange(ll,lr);
        }
        public intRange getRightRange(){
            return new intRange(rl,rr);
        }
        public String getTestsAsString(){
            String st="";
            int i;
            if(position==279){
                position=position;
            }
            st+="Position:  "+position;
            st+="      X= "+PrintAssist.ToString(pdX[position], 12, 3);
            st+="      lLen= "+PrintAssist.ToString(lr-ll+1,6,0);
            st+="      rLen= "+PrintAssist.ToString(rr-rl+1,6,0);
                
            for(i=0;i<numTests;i++){
                st+="      "+psTestNames[i]+" = "+PrintAssist.ToStringScientific(pdPValues[i], 3);
            }
            return st;
        }
        public int displaySegments(PlotWindowPlus pw){
            CommonGuiMethods.displayLineSegment(pw, segL, "Left"+position, 2, 2, Color.red);
            CommonGuiMethods.displayLineSegment(pw, segR, "Right"+position, 2, 2, Color.blue);
            CommonGuiMethods.displayLineSegment(pw, segT, "Right"+position, 2, 2, Color.black);
            return 1;
        }
//        public String[] psTestNames={"TTest","MannWhitneyUTest","CorrectedT","CorrectedMW","ConnectionSig","JumpSig","ModelCompSig","Projection","ProjectionMW"};
        public boolean isInsignificantTransition(){
            if(position==0){
                position=position;
            }
            if(rr-position<10||position-ll<10) return false;
            ArrayList<Integer> nvSelected=new ArrayList();
            CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSelected, new intRange(ll,rr), nvSelected);
            if(nvSelected.size()<8) return false;//two small sample size to nullify the delta based transition detection
            double pConnection=0.05,pTTest=0.05;
            boolean insig=false;
            double lenRatio=(double)segL.getRegressionNode().getDataSize()/(double)segR.getRegressionNode().getDataSize();
            if(pdPValues[0]>=pTTest&&pdPValues[4]>=pConnection) insig=true;
//            if(segT.dSigSideness>0.1&&segT.dSigTilting>0.1) insig=true; this condition conflict with the smooth region test.
            if(lenRatio>5||lenRatio<0.2) {
                if(pdPValues[4]>0.10) insig=true;//problem, may need to consider together with the quality of segT
            }
            if(pdPValues[0]>0.2) insig=true;
            int len=Math.min(lr-ll, rr-rl);
            if(len>15-2&&pdPValues[1]>0.01) insig=true;
            return insig;            
        }
        public boolean isSignificantTransition(){
            boolean sig=false;
            if(Math.max(Math.abs(pdPValues[1]),Math.abs(pdPValues[3]))<0.0001) sig=true;
            return sig;    
        }
    }
    
    int nMaxSegLen=20;
    ArrayList<Integer> nvMeanHighDiffClusterPositions,nvMeanLowDiffClusterPositions,nvMeanHighDiffClusterPositionsE;
    ArrayList<Integer> nvSigdiffHighDiffClusterPositions,nvSigdiffLowDiffClusterPositions,nvSigdiffHighDiffClusterPositionsE;
    ArrayList<Integer> nvEnvHighDiffClusterPositions,nvEnvLowDiffClusterPositions,nvEnvHighDiffClusterPositionsE;
    ArrayList<Integer> nvMedianHighDiffClusterPositions,nvMedianLowDiffClusterPositions,nvMedianHighDiffClusterPositionsE;
    ArrayList<Integer> nvRegressionHighDiffClusterPositions,nvRegressionLowDiffClusterPositions,nvRegressionHighDiffClusterPositionsE;
    ArrayList<Integer> nvDetectedTransitionPoints,nvDetectedTransitionPointsE,nvHighClusterPoints,nvHighClusterPointsE;
    ArrayList<Integer> nvFiltedEnvDeltaPositions,nvTransitionCandidatePositions,nvTransitionCandidatePositionsLow,nvProbeContactingPosition_Downward,nvProbeContactingPosition_Upward;
    ArrayList<Integer> nvEnvDeltaAbsLx,nvDeltaYAbsLx;
    
    double[] pdX,pdY,pdSD,pdDeltaY,pdDeltaYAbs,pdDeltaY2;
    double[] pdDeltaOLR,pdDeltaOLRModified;
    double[] pdSDSemiballContactingLx,pdSDSemiballContacting,pdSDSemiballContactingConvex;//these are used by detecting smooth sections
    double[] pdYRWMedian,pdYRWMedianDiffLR,pdYRWMeanL,pdYRWMedianL,pdYRWMeanR,pdYRWMedianR;
    
    double[] pdYRWMean_Downward,pdYRWMeanDiffLR_Downward;
    double[] pdYRWMean_Upwward,pdYRWMeanDiffLR_Upward;
    
    double[] pdProbeTrail_Downward,pdDeltaProbeTrail_Downward,pdBallTrail_Downward,pdDeltaBallTrail_Downward,pdFiltedEnv_Downward,pdFiltedEnvDelta_Downward;    
    double[] pdProbeTrail_Upward,pdDeltaProbeTrail_Upward,pdBallTrail_Upward,pdDeltaBallTrail_Upward,pdFiltedEnv_Upward,pdFiltedEnvDelta_Upward;
    double[] pdSignal;
    
/*    ArrayList<SignalTransitionStatisticsTests> cvTransitionStatisticsTestsMean,cvTransitionStatisticsTestsMeanLow;
    ArrayList<SignalTransitionStatisticsTests> cvTransitionStatisticsTestsMedian,cvTransitionStatisticsTestsMedianLow;
    ArrayList<SignalTransitionStatisticsTests> cvTransitionStatisticsTestsRegression,cvTransitionStatisticsTestsRegressionLow;*/
    
    PolynomialLineFitter m_cSignalFitter;
    ArrayList<double[]> pdvFiltedData_EdgePreserving;
    
    ArrayList<double[]> pdvProbeTrailDelta_Downward;
    ArrayList<double[]> pdvProbeTrailDelta_Upward;
    
    PolynomialLineFittingSegmentNode[] m_pcOptimalSegments,m_pcStartingSegments,m_pcEndingSegments,m_pcLongSegments,m_pcSmoothSegments;
    
    double dPChiSQ,dPPWDev,dPTilting,dPSideness;
    int nMaxRisingInterval;
    
    boolean[] pbTemp,pbEnvTransitionPoints,pbEnvHighDiffClusterPositionsE,pbEnvHighDiffClusterPositions;
    boolean[] pbSelected,pbFullSelection,pbUpperSmoothRegions,pbUpperProximityPositions,pbSelectedDelta,pbSmoothFittingSelection;//pbUpperSmoothRegions: A position in a upper smooth region is a point that is connected
    //to at least one upper (downward)probe contacting point by a series of points with all delta is smaller than the cutoff.
    
    boolean[] pbSemiBallContacting_Downward,pbEnvTransitionPointsE,pbSemiballContactingLx_Downward,pbSemiballContactingConvex_Downward;
    boolean[] pbSemiBallContacting_Upward,pbSemiballContactingLx_Upward,pbSemiballContactingConvex_Upward;
    
    boolean[] pbMultipleScaleAnchorLx_Downward,pbTopscaleAnchor_Downward;
    boolean[] pbMultipleScaleAnchorLn_Upward,pbTopscaleAnchor_Upward;
    
    boolean[] pbThickEnvelope,pbSignificantTransitions;

    ArrayList<boolean[]> pbvSemiballContacting_Downward;
    ArrayList<boolean[]> pbvSemiballContacting_Upward;
    
    int nWsSD,nOrder,nWs;
    double dOutliarRatio,dRx,dPTerminals,dSDSelected;
    String sSignalName;
    
    int[] m_pnRisingIntervals_Downward,m_pnRisingIntervals_Downward2;
    
    ArrayList<int[]> pnvTrailRisingIntervals_Downward;
    ArrayList<int[]> pnvTrailRisingIntervals_Upward;
    
    ArrayList<Double> dvFiltedEnvDelta_Downward,dvMeanDiffs_Downward,dvSigdiffs,dvMedianDiffs_Downward,dvRegressionDiffs_Downward;
    ArrayList<Double> dvFiltedEnvDelta_Upward,dvMeanDiffs_Upward,dvMedianDiffs_Upward,dvRegressionDiffs_Upward;
    
    ArrayList<OneDKMeans> cvKMeans_EffectiveAmpDiffs;
    ArrayList<OneDKMeans> cvKMeans_FiltedEnvDelta;
    
    double dFiltedEnvThickness;
    
    MeanSem1 cMsY,cMsDeltaY,cMsDeltaYE,cMsEnvThickness,cMsDeltaYSelected,cMsDeltaYSelectedE;
    int nDataSize,nWsDiff;
    ArrayList<intRange> cvLRangesMean, cvRRangesMean;
    ArrayList<intRange> cvLRangesMediab, cvRRangesMedian;
    double dEnvDeltaCutoff,smoothDeltaCutoff;
    PlotWindowPlus pw;
    SignalTransitionStatisticsTests[] pcTransitionTests;
    int[] pnHighDiffClusterPosition;//three digit numbers, non-zero first, second and third digits indicate the position is a high cluster position of Mean, Median and Regression diffs, respectively
    int[] pnLowDiffClusterPosition;//three digit numbers, non-zero first, second and third digits indicate the position is a high cluster position of Mean, Median and Regression diffs, respectively
    
    int[] pnHighDiffClusterPositionE;//Clustering without the largest element
    int[] pnLowDiffClusterPositionE;//Clustering without the largest element    
    OneDKMeans cKMDeltaEnv,cKMDeltaMean,cKMDeltaMeanE,cKMDeltaMedian,cKMDeltaRegression,cKMDeltaRegressionE,cKMDeltaPW,cKMSigDiff,cKMSigDiffE;
    OneDKMeans_ExtremExcluded cKM_EnvDiff_ExtremeExclusion,cKM_MeanDiff_ExtremeExclusion,cKM_MedianDiff_ExtremeExclusion,cKM_RegressionDiff_ExtremeExclusion;
    int[][] m_ppnSmoothRegionLength;
    int[] m_pnMinSoomthRegionLength;
    int nNumSmoothCriteria;
    int nNumBallScales;
    double dMultiscaleSegCutoff;
    boolean bCheckClusteringOnly;
    
    ArrayList<ArrayList<Integer>> nvvProbeContactingPositions_Downward,nvvAnchoredBallTrailDeltaLx_Downward,nvvBallContactingExtrema_Downward;
    ArrayList<ArrayList<Integer>> nvvProbeContactingPositions_Upward,nvvAnchoredBallTrailDeltaLn_Upward,nvvBallContactingExtrema_Upward;
    
    ArrayList<double[]> pdvProbeContactingPointsX_Downward;
    ArrayList<double[]> pdvProbeContactingPointsY_Downward;
    
    ArrayList<double[]> pdvProbeContactingPointsX_Upward;
    ArrayList<double[]> pdvProbeContactingPointsY_Upward;
    
    ArrayList<Integer> nvBallRollingWs;
    ArrayList<String> svBallRollingWs;
    
    ArrayList<double[]> pdvProbeTrail_Downward;
    ArrayList<double[]> pdvProbeContactingPointsDevX_Downward,pdvProbeContactingPointsDevY_Downward,pdvProbeContactingPointsSDX_Downward,pdvProbeContactingPointsSDY_Downward;
    ArrayList<double[]> pdvProbeTrail_Upward;
    ArrayList<double[]> pdvProbeContactingPointsDevX_Upward,pdvProbeContactingPointsDevY_Upward,pdvProbeContactingPointsSDX_Upward,pdvProbeContactingPointsSDY_Upward;

    
    ArrayList<double[]> pdvP_TrailDelta_Downward,pdvP_TrailHeight_Downward;
    ArrayList<double[]> pdvP_TrailDelta_Upward,pdvP_TrailHeight_Upward;
    
    ArrayList<int[]> pnvSegmentIndexes_Downward, pnvSegmentIndexes_Upward;
    
    ArrayList<LineSegmentNode> cvSignalLevelSegments, cvSignalLevelSegmentsE;
    
    ArrayList<ArrayList<intRange>> cvvBallTrailSegmenters_Downward;
    ArrayList<ArrayList<intRange>> cvvBallTrailSegmenters_Upward;
    ArrayList<Double> dvPSegmentation,dvPConsolidation;
    ArrayList<ParValidityChecker> cvParCheckers;
//    int[] pnProbeGrade;//index of nvBallRollingWs//12d03
//    double[] pdOptimalProbeTrail;//12d03
    double[] pdSigDiff;    
    double[] pdBallContactingPointsDevPValues_Downward;
    boolean bFeatureLinesOnly;
    boolean bSmoothCriteria;
    boolean bFiltedEnvCutoff,bAligningToDeltaExtrema;;
    double[] pdYDethickended;
    ArrayList<double[]> pdvSigdiffs;
    public LineFeatureExtracter2(){
        bFeatureLinesOnly=false;
        nWsDiff=5;
        nvBallRollingWs=new ArrayList();
        
        double dPSeg=0.15,dPCons=0.3;
//        nvBallRollingWs.add(25);
        nvBallRollingWs.add(15);
//        nvBallRollingWs.add(5);
        nvBallRollingWs.add(5);
//        nvBallRollingWs.add(3);
        nNumBallScales=nvBallRollingWs.size();
        
        svBallRollingWs=new ArrayList();
        dvPSegmentation=new ArrayList();
        dvPConsolidation=new ArrayList();
        
        for(int i=0;i<nvBallRollingWs.size();i++){
            svBallRollingWs.add(nvBallRollingWs.get(i).toString());
            dvPSegmentation.add(dPSeg);
            dvPConsolidation.add(dPCons);
        }
        svBallRollingWs.add("multiscale");
        cvParCheckers=new ArrayList();
        cvParCheckers.add(new PNParChecker());
        bSmoothCriteria=false;
        bFiltedEnvCutoff=true;
        bAligningToDeltaExtrema=true;
        pdvSigdiffs=new ArrayList();
    }
    
    public LineFeatureExtracter2(String sSignalName, double[] pdX, double[] pdY){
        this();
        nDataSize=pdX.length;
        bFeatureLinesOnly=true;
        this.pdX=pdX;
        this.pdY=pdY;
        this.sSignalName=sSignalName;
        nMaxRisingInterval=2;
        pbSignificantTransitions=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbSignificantTransitions, false);
        buildFeatureLines(null);
    }
    
    public LineFeatureExtracter2(String sSignalName, double[] pdX, double[] pdY, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder){
        this(sSignalName,pdX,pdY,nMaxInterval,dPChiSQ,dPPWDev,dPTilting, dPSideness,dPTerminals,nOrder,false);
    }
    public LineFeatureExtracter2(String sSignalName, double[] pdX, double[] pdY, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder, boolean bCheckClusteringOnly){
        this();
        extractFeature(sSignalName, pdX,pdY, nMaxInterval, dPChiSQ,dPPWDev,dPTilting,dPSideness, dPTerminals, nOrder, bCheckClusteringOnly,false);
    }
    public LineFeatureExtracter2(String sSignalName, double[] pdX, double[] pdY, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder, boolean bCheckClusteringOnly, boolean bSmooth){
        this();
        extractFeature(sSignalName, pdX,pdY, nMaxInterval, dPChiSQ,dPPWDev,dPTilting,dPSideness, dPTerminals, nOrder, bCheckClusteringOnly,bSmooth);
    }
    public void extractFeature(String sSignalName, double[] pdX, double[] pdY, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder, boolean bCheckClusteringOnly, boolean bSmooth){
        AnalysisMasterForm.appendText(sSignalName+" Features:"+PrintAssist.newline);
//        AnalysisMasterForm.appendText(sSignalName+" Features:"+PrintAssist.newline);
        long time1,time2,dt;
        bSmoothCriteria=bSmooth;
        if(pdX.length<nvBallRollingWs.get(0)){
            nvBallRollingWs.remove(0);
            nNumBallScales=nvBallRollingWs.size();
        } 
        this.bCheckClusteringOnly=bCheckClusteringOnly;
        this.sSignalName=sSignalName;
        this.pdX=pdX;
        this.pdY=pdY;
        dOutliarRatio=0;
        nMaxRisingInterval=nMaxInterval;
        nWsSD=20;
        this.dPChiSQ=dPChiSQ;
        this.dPPWDev=dPPWDev;
        this.dPTilting=dPTilting;
        this.dPSideness=dPSideness;
        this.dPTerminals=dPTerminals;
        this.nOrder=nOrder;
        nDataSize=pdX.length;
        
        pbSignificantTransitions=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbSignificantTransitions, false);
        pdYRWMeanL=new double[nDataSize];
        pdYRWMeanR=new double[nDataSize];
        pdYRWMeanL=new double[nDataSize];
        pdYRWMeanR=new double[nDataSize];
        pdYRWMeanDiffLR_Downward=new double[nDataSize];
            
        pdYRWMedianL=new double[nDataSize];
        pdYRWMedianR=new double[nDataSize];
        pdYRWMedianL=new double[nDataSize];
        pdYRWMedianR=new double[nDataSize];
        pdYRWMedianDiffLR=new double[nDataSize];
        pcTransitionTests=new SignalTransitionStatisticsTests[nDataSize];
        pbTemp=new boolean[nDataSize];
        
        m_pnRisingIntervals_Downward=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        m_pnRisingIntervals_Downward2=CommonStatisticsMethods.calRisingIntervals(pdY, 2*nMaxRisingInterval);
        pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdY, nMaxRisingInterval, nWsSD);
        pbFullSelection=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbFullSelection, true);

        //removing the last maximum
        /*
        int i,iI,iF=pdY.length-1;
        iI=CommonStatisticsMethods.getFirstExtremumIndex(pdY, iF, -1, -1);
        if(iI<0) iI=0;
        double dtt=pdY[iI];
        for(i=iI;i<=iF;i++){
            pdY[i]=dtt;
        }*/
        
        
        buildBallTrails();
        filterPartialTransitions();
        time1=System.currentTimeMillis();
//        fitTrackSignal_Addaptive(nOrder,dPChiSQ,dPTilting,dPSideness,dPPWDev,dOutliarRatio);
        time2=System.currentTimeMillis();
        dt=time2-time1;
        
        pdYRWMean_Downward=CommonStatisticsMethods.getRunningWindowAverage(pdY, 0, nDataSize-1, nWsDiff, false);
        ArrayList<Integer> nvIndexes=null;
        pdYRWMedian=CommonStatisticsMethods.getRunningWindowQuantile(pdY, nWsDiff, nWsDiff, nvIndexes);
        pdDeltaY=CommonStatisticsMethods.getDeltaArray(pdY, m_pnRisingIntervals_Downward, nMaxRisingInterval);
        pdDeltaY2=CommonStatisticsMethods.getDeltaArray(pdY, m_pnRisingIntervals_Downward2, 2*nMaxRisingInterval);
        
        pdDeltaProbeTrail_Upward=CommonStatisticsMethods.getDeltaArray(pdProbeTrail_Upward, null, nMaxRisingInterval);
        pdDeltaProbeTrail_Downward=CommonStatisticsMethods.getDeltaArray(pdProbeTrail_Downward, null, nMaxRisingInterval);
        pdDeltaYAbs=CommonStatisticsMethods.getAbs(pdDeltaY);
 //       nvDeltaYAbsLx=new ArrayList();
//        CommonMethods.LocalExtrema(pdDeltaYAbs, new ArrayList<Integer>(), nvDeltaYAbsLx);
        cMsY=CommonStatisticsMethods.buildMeanSem1(pdY, 0, nDataSize-1, 1);
        cMsDeltaY=CommonStatisticsMethods.buildMeanSem1(pdDeltaYAbs, 0, pdDeltaYAbs.length-1, 1);
        cMsDeltaYE=new MeanSem1();
        CommonStatisticsMethods.findOutliars(pdDeltaYAbs, 0.01, cMsDeltaYE, null);
        smoothDeltaCutoff=GaussianDistribution.getZatP(1-0.1, cMsDeltaYE.mean, cMsDeltaYE.getSD(), 0.001*cMsDeltaYE.getSD());
//        smoothDeltaCutoff=GaussianDistribution.getZatP(1-0.1, cMsDeltaYE.mean, cMsDeltaYE.getSD(), 0.001*cMsDeltaYE.getSD());//12829
        extractFeatures();
//        AnalysisMasterForm.appendText("SignalMeanSem: "+cMsY.toString()+PrintAssist.newline);
        if(!bCheckClusteringOnly) AnalysisMasterForm.appendText("DeltaMeanSem: "+cMsDeltaY.toString()+PrintAssist.newline);
        if(!bCheckClusteringOnly) AnalysisMasterForm.appendText("DeltaMeanSemE: "+cMsDeltaYE.toString()+PrintAssist.newline);
        if(!bCheckClusteringOnly) AnalysisMasterForm.appendText("EnvThicknessMeanSem: "+cMsEnvThickness.toString()+PrintAssist.newline);        
    }
   
    void displayFeatures(){
        PlotWindowPlus pw;
        pw=CommonGuiMethods.displayNewPlotWindowPlus("FeatureLine", pdX, pdY, 1, 2, Color.BLACK);
        int i,len=pdvFiltedData_EdgePreserving.size(),ci=0;;
        for(i=0;i<len;i++){
            ci++;
            pw=CommonGuiMethods.addPlot(pw, "Iteration"+i, pdX, pdvFiltedData_EdgePreserving.get(i), 2, 2, CommonGuiMethods.getDefaultColor(ci), true);
            ci++;
            pw=CommonGuiMethods.addPlot(pw, "Iteration"+i, pdX, pdFiltedEnv_Downward, 2, 2, CommonGuiMethods.getDefaultColor(ci), true);
            ci++;
            pw=CommonGuiMethods.addPlot(pw, "Iteration"+i, pdX, pdFiltedEnv_Upward, 2, 2, CommonGuiMethods.getDefaultColor(ci), true);
        }
    }
//    public LineFitter_AdaptivePolynomial(double[] pdX, double[] pdY, boolean[] pbSelected, int nRisingInterval, int nOrder, int maxSegLen, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
    void locateSpecialPoints(){
        pbSemiballContactingLx_Downward=new boolean[nDataSize];
        pbSemiballContactingConvex_Downward=new boolean[nDataSize];
        ArrayList<Integer> nvx=new ArrayList(), nvn=new ArrayList(),convexPoints;
        CommonMethods.LocalExtrema(pdY, nvn, nvx);
        convexPoints=CommonStatisticsMethods.getConvexPoints(pdX, pdY, 1, 1);
        
//        CommonStatisticsMethods.setElements(pbt, false);
        CommonStatisticsMethods.setElements(pbSemiballContactingLx_Downward, false);
        CommonStatisticsMethods.setElements(pbSemiballContactingConvex_Downward, false);
        
        CommonStatisticsMethods.setElements_AND(pbSemiballContactingLx_Downward, nvvProbeContactingPositions_Downward.get(nvvProbeContactingPositions_Downward.size()-1),nvx,true);//12o31
        CommonStatisticsMethods.setElements_AND(pbSemiballContactingConvex_Downward, nvvProbeContactingPositions_Downward.get(nvvProbeContactingPositions_Downward.size()-1),convexPoints,true);//12n13
    }
    void lowerIntermediateMaxima(){
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        int i,len=nvLx.size();
        
    }
    void calSmoothSegLengths(){
        int nWsTilting=3,nWsPWDev=0,nOrder;
        PlotWindowPlus pw=null;
//        int nTerminalLength=3;
        int nTerminalLength=2000;
        nNumSmoothCriteria=4;
        m_ppnSmoothRegionLength=new int[nNumSmoothCriteria][];
        m_pnMinSoomthRegionLength=new int[nNumSmoothCriteria];
        
        m_pnMinSoomthRegionLength[0]=1000;
        m_pnMinSoomthRegionLength[1]=14000;
        m_pnMinSoomthRegionLength[2]=nTerminalLength+nTerminalLength+nMaxRisingInterval+2000;
//        m_pnMinSoomthRegionLength[2]=12;
        locateSpecialPoints();
        nOrder=1;
        LineFitter_AdaptivePolynomial cSignalFitter=new LineFitter_AdaptivePolynomial(pdX,pdY,pbFullSelection,nMaxRisingInterval,nOrder,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,0.15,0.15,0.15,dPTerminals, dOutliarRatio);
        m_ppnSmoothRegionLength[0]=CommonStatisticsMethods.copyArray(cSignalFitter.getSmoothSegLengths());
//        if(IPOAnalyzer.isInteractive())pw=CommonGuiMethods.displayNewPlotWindowPlus("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[0]), 2, 2, Color.blue);
 
        cSignalFitter=new LineFitter_AdaptivePolynomial(pdX,pdY,pbSelected,nMaxRisingInterval,1,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,0.05,0.05,0.05,dPTerminals, dOutliarRatio,0.34);
        m_ppnSmoothRegionLength[1]=CommonStatisticsMethods.copyArray(cSignalFitter.getSmoothSegLengths());
//        if(IPOAnalyzer.isInteractive())pw.addPlot("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[1]), 2, 2, Color.red);
 
        cSignalFitter=new LineFitter_AdaptivePolynomial();
        double[] pdSD=calSD(pdX,pdY,pbSelected,nMaxRisingInterval,20);
        cSignalFitter.setSmoothSegTerminalLength(new IntPair(nTerminalLength,nTerminalLength));
        cSignalFitter.setSD(pdSD);
        cSignalFitter.fit(pdX,pdProbeTrail_Downward,pbFullSelection,nMaxRisingInterval,nOrder,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,0.05,0.05,0.05,0, dOutliarRatio);
        m_ppnSmoothRegionLength[2]=CommonStatisticsMethods.copyArray(cSignalFitter.getSmoothSegLengths());
//        if(IPOAnalyzer.isInteractive())pw.addPlot("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[2]), 2, 2, Color.BLACK);
        
        boolean[] pbt;
        pdSDSemiballContactingLx=new double[nDataSize];
        pdSDSemiballContactingConvex=new double[nDataSize];
        
        cSignalFitter=new LineFitter_AdaptivePolynomial();
//        CommonStatisticsMethods.setElements_AND(pbt, nvvProbeContactingPositions_Downward.get(0),convexPoints,true);//12o31
//        CommonStatisticsMethods.setElements(pbt, nvvProbeContactingPositions_Downward.get(nvvProbeContactingPositions_Downward.size()-1),true);//12n13
        
        nTerminalLength=2;
        m_pnMinSoomthRegionLength[3]=3000;
        cSignalFitter.setSmoothSegTerminalLength(new IntPair(nTerminalLength,nTerminalLength));
//        pdSD=calSD(pdX,pdY,pbt,nMaxRisingInterval,20);
//        CommonStatisticsMethods.setElements(pbt, false);
        
        ArrayList<double[]> pdvMeanSD=CommonStatisticsMethods.calRWSD_SelectedDataDevBased(pdX, pdY, pbSemiballContactingLx_Downward, nMaxRisingInterval, nWsSD, 0.01);
        pdSDSemiballContactingLx=pdvMeanSD.get(1);
        
        pdvMeanSD=CommonStatisticsMethods.calRWSD_SelectedDataDevBased(pdX, pdY, pbSemiballContactingConvex_Downward, nMaxRisingInterval, nWsSD, 0.01);
        pdSDSemiballContactingConvex=pdvMeanSD.get(1);
        
        nOrder=2;
        int nMinLen=4;
        double dPChiSQ=0.5,dPPWDev=0.1,dPTilting=0.1,dPSideness=0.1,dPTerminals=0.;
        cSignalFitter.setLineFeatureExtracter(this);
        cSignalFitter.setParValidityCheckters(cvParCheckers);
        
        pdSD=pdSDSemiballContactingLx;
        pbt=pbSemiballContactingLx_Downward;
        pbSmoothFittingSelection=pbt;
        cSignalFitter.setSD(pdSD);        
        cSignalFitter.setRisingInterval(nMaxRisingInterval);
        cSignalFitter.fit(pdX,pdY,pbt,nMaxRisingInterval,nOrder,nMinLen,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals, dOutliarRatio);
        m_pcSmoothSegments=cSignalFitter.getLongRegressions();

        nMinLen=6;
        pdSD=pdSDSemiballContactingConvex;
        pbt=pbSemiballContactingConvex_Downward;
        pbSmoothFittingSelection=pbt;
        cSignalFitter.setSD(pdSD);      
        cSignalFitter.setRisingInterval(nMaxRisingInterval);
        cSignalFitter.fit(pdX,pdY,pbt,nMaxRisingInterval,nOrder,nMinLen,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals, dOutliarRatio);
        PolynomialLineFittingSegmentNode[] segs=cSignalFitter.getLongRegressions();
        
        PolynomialLineFittingSegmentNode seg;
        int i;
        for(i=0;i<nDataSize;i++){
            seg=segs[i];
            m_pcSmoothSegments[i]=seg.getSmootherSegment(seg, m_pcSmoothSegments[i],i,(double)nMaxRisingInterval);
        }        
        
        cSignalFitter.setParValidityCheckters(null);
        m_ppnSmoothRegionLength[3]=CommonStatisticsMethods.copyArray(cSignalFitter.getSmoothSegLengths());
//        if(IPOAnalyzer.isInteractive())pw.addPlot("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[3]), 2, 2, Color.ORANGE);
    }
    void extractFeatures(){
//        CommonGuiMethods.displayNewPlotWindowPlus("test1", pdX, pdY, 1, 2, Color.black);
        makeSelection();
        FiltData_EdgePreserving();
//        CommonGuiMethods.displayNewPlotWindowPlus("test2", pdX, pdY, 1, 2, Color.black);
        buildFiltedEnvlines();
        extractedFiltedEnvFeatures();
        calSmoothSegLengths();//13125
        calDeltaArrays_ProbeTrail();
        buildAndMarkDiffClusters();
        if(!bCheckClusteringOnly){
            fitTrackSignal_Addaptive(nOrder,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals,dOutliarRatio);
//            calSmoothSegLengths();
            PolynomialLineFittingSegmentNode segL,segR;
            int i,ll,rr,r,len=pdX.length;
            pdDeltaOLR=new double[nDataSize];
            pdDeltaOLRModified=new double[nDataSize];
            double delta,xL,xR,yL,yR;
            int len1=m_pnRisingIntervals_Downward.length;
            for(i=0;i<len;i++){
                if(i==0){
                    i=i;
                }
                if(i<len1)
                    r=i+m_pnRisingIntervals_Downward[i];
                else
                    r=nDataSize-1;
                xL=pdX[i];
                xR=pdX[r];
                segL=m_pcEndingSegments[i];
                segR=m_pcStartingSegments[r];

                if(segL!=null) 
                    yL=segL.predict(xL);
                else
                    yL=pdYRWMean_Downward[i];

                if(segR!=null)
                    yR=segR.predict(xR);
                else
                    yR=pdYRWMean_Downward[r];

                pdDeltaOLR[i]=yL-yR;
                if(segL==null||segR==null)
                    pdDeltaOLRModified[i]=yL-yR;
                else
                    pdDeltaOLRModified[i]=getModifiedDelta(segL.getRegressionNode(),segR.getRegressionNode(),pdX,pdYRWMean_Downward,i,r);
            }
            adjustStartingAndEndingSegments();
            buildAndMarkRegressionClusters();
            calStatisticsTests();
            markHigherClusterPoints();
            displayTransitionInfo();
            cvSignalLevelSegments=buildSegments(nvDetectedTransitionPoints);
            cvSignalLevelSegmentsE=buildSegments(nvDetectedTransitionPointsE);
//            if(IPOAnalyzer.isInteractive()) displayFeatures();
//            calSigDiff();
        }
    }
    public void extractedFiltedEnvFeatures(){
        int i,len=pdFiltedEnv_Downward.length;
        dFiltedEnvThickness=0;
        nvFiltedEnvDeltaPositions=new ArrayList();
        double delta;
        double[] pdDelEnv=new double[len];
        DoubleRange envDiffRange=new DoubleRange();
        for(i=0;i<len;i++){
            delta=pdFiltedEnv_Downward[i]-pdFiltedEnv_Upward[i];
            pdDelEnv[i]=delta;
            if(delta>dFiltedEnvThickness)dFiltedEnvThickness=delta;
        }
        cMsEnvThickness=CommonStatisticsMethods.buildMeanSem1(pdDelEnv, 0, len-1, 1);
        pdFiltedEnvDelta_Downward=CommonStatisticsMethods.getDeltaArray(pdFiltedEnv_Downward, null, nMaxRisingInterval);
        pdFiltedEnvDelta_Upward=CommonStatisticsMethods.getDeltaArray(pdFiltedEnv_Upward, null, nMaxRisingInterval);
        
        dvFiltedEnvDelta_Downward=new ArrayList();
        nvEnvDeltaAbsLx=new ArrayList();
        ArrayList<Integer> nvLn=new ArrayList();
        len=pdFiltedEnvDelta_Downward.length;
        double[] pdDiff=new double[len];
        for(i=0;i<len;i++){
            pdDiff[i]=Math.abs(pdFiltedEnvDelta_Downward[i]);
        }
        
        CommonMethods.LocalExtrema(pdDiff, nvLn, nvEnvDeltaAbsLx);
        DoubleRange dr=CommonStatisticsMethods.getRange(pdFiltedEnvDelta_Downward);
        double dMaxDelta=Math.max(Math.abs(dr.getMin()),Math.abs(dr.getMax()));
        double ratio=0.2;
        dEnvDeltaCutoff=ratio*dMaxDelta;
        
        len=nvEnvDeltaAbsLx.size();
        int[] indexes=new int[len]; 
        int position;
        double diffAbs;
        for(i=0;i<len;i++){
            position=nvEnvDeltaAbsLx.get(i);
            diffAbs=pdDiff[position];
            envDiffRange.expandRange(diffAbs);
            dvFiltedEnvDelta_Downward.add(diffAbs);
            if(diffAbs>=dEnvDeltaCutoff)nvFiltedEnvDeltaPositions.add(position);
            indexes[i]=i;
        }
        
        if(dvFiltedEnvDelta_Downward.size()>2) cKM_EnvDiff_ExtremeExclusion=new OneDKMeans_ExtremExcluded("Exctreme Excluded Env KMean",dvFiltedEnvDelta_Downward,2,1);
        
        
        QuickSort.quicksort(CommonStatisticsMethods.copyToDoubleArray(dvFiltedEnvDelta_Downward), indexes);
        int[]pnClusterIndexes=new int[len];
        int nx=indexes[len-1],j,K;
        OneDKMeans cKM;
        AnalysisMasterForm.newLine();
        cvKMeans_FiltedEnvDelta=new ArrayList();
        for(i=0;i<2;i++){
            if(dvFiltedEnvDelta_Downward.size()<i+2) continue;
            K=i+2;
            for(j=0;j<K;j++){
                nx=indexes[len-1-j];
                pnClusterIndexes[nx]=K-1-j;
            }
            cKM=new OneDKMeans("Filted EnvLine",dvFiltedEnvDelta_Downward,K,pnClusterIndexes);
            cvKMeans_FiltedEnvDelta.add(cKM);
            AnalysisMasterForm.newLine();
            AnalysisMasterForm.appendStrings(cKM.toStrings());
        }
        pbEnvHighDiffClusterPositions=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbEnvHighDiffClusterPositions,false);
        
        cKM=cvKMeans_FiltedEnvDelta.get(0);
        boolean[] pbt=new boolean[nvEnvDeltaAbsLx.size()];
        CommonStatisticsMethods.markHighClusterPositions(cKM, pbt, 1);
        if(cKM_EnvDiff_ExtremeExclusion!=null) pnClusterIndexes=cKM_EnvDiff_ExtremeExclusion.getClusterIndexes();
        double maxDiff=envDiffRange.getMax();
        double diffCutoff=0.25*maxDiff;
        
        for(i=0;i<pbt.length;i++){
//            if(!pbt[i]) continue;
            position=nvEnvDeltaAbsLx.get(i);
            if(pdDiff[position]<diffCutoff) continue;
            if(pnClusterIndexes[i]<0||pnClusterIndexes[i]>=1) pbEnvHighDiffClusterPositions[position]=true;//12920
        }
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.newLine();
        ArrayList<String> sv=new ArrayList();
        sv.add("cKM_EnvDiff_ExtremeExclusion has not been computed");
        if(cKM_EnvDiff_ExtremeExclusion!=null)  sv=cKM_EnvDiff_ExtremeExclusion.toStrings();
        AnalysisMasterForm.appendStrings(sv);
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.newLine();
    }
    void selectUperSmoothRetions(){
        int i,position,left, len=nvProbeContactingPosition_Downward.size(),right,signL,signR;
        for(i=0;i<len;i++){
            position=nvProbeContactingPosition_Downward.get(i);
            if(position==63){
                i=i;
            }
            if(position>pdDeltaYAbs.length-1){
                left=position;
                signL=0;
            } else {
                left=CommonStatisticsMethods.getNextLargeAbsPosition(pdDeltaY, position, -1, smoothDeltaCutoff);
                if(left<0)
                    signL=0;
                else if(pdDeltaY[left]>0)
                    signL=1;
                else
                    signL=-1;                
            }
            
            if(left<0) 
                left=0;
            else
                left=Math.min(left+1,position);
            
            if(position>pdDeltaYAbs.length-1){
                right=position;
                signR=0;
            } else if(pdDeltaYAbs[position]>smoothDeltaCutoff){
                right=position;
                if(pdDeltaY[right]>0)
                    signR=1;
                else
                    signR=-1;
            } else {
                right=CommonStatisticsMethods.getNextLargeAbsPosition(pdDeltaY, position, 1, smoothDeltaCutoff);            
                if(right<0){ 
                    right=pdDeltaYAbs.length-1;
                    signR=0;
                } else if(right==0){
                    right=0;
                    if(pdDeltaY[right]>0)
                        signR=1;
                    else
                        signR=-1;
                } else {
                    if(pdDeltaY[right]>0)
                        signR=1;
                    
                    else
                        signR=-1;
                    right+=(m_pnRisingIntervals_Downward[right-1]-1);
                }
            }
//            if(signL>0&&signR<0) 
//                continue;//this group of data points belong to a downward spike.
            for(position=left;position<=right;position++){
                pbUpperSmoothRegions[position]=true;
            }
            
        }
    }
    void selectUperProximityPositions(){
        int i,position,left, len=pdX.length;
        pbUpperProximityPositions=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbUpperProximityPositions, false);
        double[] pdMultiscaleBallTrail=pdvProbeTrail_Downward.get(pdvProbeTrail_Downward.size()-1);
        double pdLine[]=CommonStatisticsMethods.copyArray(pdMultiscaleBallTrail);
        CommonStatisticsMethods.subtractArray(pdLine, pdY);
        MeanSem1 ms=new MeanSem1(),ms1=new MeanSem1();
        double small=Double.MIN_NORMAL;
        boolean pbNonZero[]=new boolean[len];
        for(i=0;i<len;i++){
            if(Math.abs(pdLine[i])>small)
                pbNonZero[i]=true;
            else
                pbNonZero[i]=false;
        }
        CommonStatisticsMethods.findOutliars(pdLine, pbNonZero, 0.01, ms, null);
//        CommonStatisticsMethods.findOutliars(pdLine, 0.01, ms1, null);
        double sd=ms.getSD();
//        double sd1=ms1.getSD();
        double cutoff=Math.abs(GaussianDistribution.getZatP(1.-0.1, ms.mean, sd, 0.0001*sd));
//        double cutoff1=Math.abs(GaussianDistribution.getZatP(1.-0.05, ms1.mean, sd1, 0.0001*sd));
        for(i=0;i<len;i++){
            if(Math.abs(pdLine[i])<cutoff)
//            if(pdLine[i]<cutoff&&pdLine[i]>0)
                pbUpperProximityPositions[i]=true;
            else
                pbUpperProximityPositions[i]=false;
        }
    }
    
    void makeSelection(){
        pbUpperSmoothRegions=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbUpperSmoothRegions, false);
//        selectUperSmoothRetions();
        selectUperProximityPositions();
        setSelection(pbUpperProximityPositions);
        setSelection(pbFullSelection);//13226
                
    }
    public void setSelection(boolean[] pbSelected){
        this.pbSelected=pbSelected;
        calSelectedDevs();
    }
    public void calSelectedDevs(){
        pbSelectedDelta=CommonStatisticsMethods.getDeltaSelection_AND(pbSelected, m_pnRisingIntervals_Downward);
        cMsDeltaYSelected=CommonStatisticsMethods.buildMeanSem1_Selected(pdDeltaY, pbSelectedDelta, 0, pdDeltaY.length-1, 1);
        cMsDeltaYSelectedE=new MeanSem1();
        CommonStatisticsMethods.findOutliars(pdDeltaY, pbSelectedDelta, 0.01, cMsDeltaYSelectedE, new ArrayList());
        
        pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdX, pdY, m_pnRisingIntervals_Downward, pbSelected, nWsSD);
    }
    
    void calDeltaArrays(){//this methods adjust the runing window mean and median delta according to the transition candidate suggusted by 
        //the cutoff of the filted envilope lines, 
        cvLRangesMean=new ArrayList();
        cvRRangesMean=new ArrayList();
        int i;
        boolean[] pbt;
//        pbt=new boolean[nDataSize];
//        CommonStatisticsMethods.markSelections(pbt,nvFiltedEnvDeltaPositions);
        pbt=pbEnvHighDiffClusterPositions;//12829       
        intRange ir=new intRange();
        ArrayList<Integer> positions=new ArrayList();
        int ll,rl,rr,lr;
        boolean bAdjust;
        double lMean,rMean,lMedian,rMedian,meanDiff,sign,meanDiffL,meanDiffR,correction,delta;
        int nDataSize=pdX.length,lls,lrs,rls,rrs,num;
        PolynomialLineFittingSegmentNode smoothSeg;
        
        for(i=0;i<nDataSize;i++){
            if(i==75) {
                i=i;
            }
            bAdjust=false;
            lr=i;
            ll=Math.max(0, lr-nWsDiff);
            rl=Math.min(i+nMaxRisingInterval,nDataSize-1);
            rr=Math.min(rl+nWsDiff, nDataSize-1);
            pdYRWMeanL[lr]=0;
            pdYRWMeanR[ll]=0;
            pdYRWMeanL[rr]=0;
            pdYRWMeanR[rl]=0;
            pdYRWMeanDiffLR_Downward[lr]=0;
            
            pdYRWMedianL[lr]=0;
            pdYRWMedianR[ll]=0;
            pdYRWMedianL[rr]=0;
            pdYRWMedianR[rl]=0;
            pdYRWMedianDiffLR[lr]=0;
            
            if(!pbSelected[i]) {
            
                cvLRangesMean.add(new intRange(ll,lr));
                cvRRangesMean.add(new intRange(rl,rr));
                continue;
            }
            if(i>0){
                ir.setRange(Math.max(ll-nMaxRisingInterval,0), lr);
                CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, lr-1, -1, ir, positions);
                if(!positions.isEmpty()){
                    ll=positions.get(0);
                    ll=Math.min(ll+nMaxRisingInterval, lr);
                    bAdjust=true;
                }
            }
                
            if(i<nDataSize-1){
                ir.setRange(rl, rr);
                CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, rl, 1, ir, positions);
                if(!positions.isEmpty()){
                    rr=positions.get(0);
                    bAdjust=true;
                }                 
            }
            
            cvLRangesMean.add(new intRange(ll,lr));
            cvRRangesMean.add(new intRange(rl,rr));
            
//            if(pbSelected[lr]){
            if(true){
                lMean=CommonStatisticsMethods.getMean(pdY, pbSelected,ll,lr, 1);
//                lMedian=CommonStatisticsMethods.getMedian(pdY, pbSelected, ll,lr, 1);
                lMean=getMean(ll,lr);
                lMedian=getMedian(ll,lr);
            } else {
//                lMean=pdProbeTrail_Downward[lr];
//                lMedian=lMean;               
            }
            
            if(true){
                rMean=pdProbeTrail_Downward[rl];//12n17          
//                rMedian=rMean;
                rMean=getMean(rl,rr);
                rMedian=getMedian(rl,rr);
            } else {
                rMean=CommonStatisticsMethods.getMean(pdY, pbSelected, rl,rr, 1);            
                rMedian=CommonStatisticsMethods.getMedian(pdY, pbSelected, rl,rr, 1);
            }
            
            if(Double.isNaN(rMean)) rMean=lMean;
            if(Double.isNaN(rMedian)) rMedian=lMedian;
            pdYRWMeanL[lr]=lMean;
            pdYRWMeanR[ll]=lMean;
            pdYRWMeanL[rr]=rMean;
            pdYRWMeanR[rl]=rMean;
//            meanDiff=pdProbeTrail_Downward[lr]-pdProbeTrail_Downward[rl];
            meanDiff=pdProbeTrail_Downward[lr]-pdProbeTrail_Downward[rl];
            sign=1.;
            if(lr==3){
                lr=lr;
            }
            if(meanDiff<0) sign=-1.;
/*            if(lr>nMaxRisingInterval){
                if(rl<nDataSize-nMaxRisingInterval){
                    meanDiffL=pdProbeTrail_Downward[lr-nMaxRisingInterval]-pdProbeTrail_Downward[lr];
                    meanDiffR=pdProbeTrail_Downward[rl]-pdProbeTrail_Downward[rl+nMaxRisingInterval];
                    correction=meanDiffL;
                    if(sign*(meanDiffR-meanDiffL)>0) correction=meanDiffR;
                }else{
                    correction=pdProbeTrail_Downward[lr-nMaxRisingInterval]-pdProbeTrail_Downward[lr];
                }
            }else{
                if(rl<nDataSize-nMaxRisingInterval){
                    correction=pdProbeTrail_Downward[rl]-pdProbeTrail_Downward[rl+nMaxRisingInterval];
                }else{
                    correction=0;
                }
            }*/
            correction=0;
            delta=0;
            if(isSmoothProfile(i)){
                smoothSeg=m_pcSmoothSegments[i];
                num=0;
                lls=Math.max(lr-nMaxRisingInterval,smoothSeg.nStart);
                lrs=lr;
                rls=rl;
                rrs=Math.min(smoothSeg.nEnd,rl+nMaxRisingInterval);
                rrs=Math.min(rrs, nDataSize-1);
                if(lrs>lls) {
                    delta=(smoothSeg.predict(pdX[lrs])-smoothSeg.predict(pdX[lls]))/(pdX[lrs]-pdX[lls]);
                    num++;
                }
                if(rrs>rls) {
                    delta+=(smoothSeg.predict(pdX[rrs])-smoothSeg.predict(pdX[rls]))/(pdX[rrs]-pdX[rls]);
                    num++;
                }
                if(num>0)
                    delta/=num;         
            }
            correction=delta*(rl-lr);
            correction=0;//12n28
//            if(sign*correction<0) correction=0;
            meanDiff+=correction;
            if(sign*meanDiff<0) meanDiff=0;
            pdYRWMeanDiffLR_Downward[lr]=meanDiff;
//            pdYRWMeanDiffLR_Downward[lr]=lMean-rMean;
            
            pdYRWMedianL[lr]=lMedian;
            pdYRWMedianR[ll]=lMedian;
            pdYRWMedianL[rr]=rMedian;
            pdYRWMedianR[rl]=rMedian;
            pdYRWMedianDiffLR[lr]=lMedian-rMedian;
        }
    }
    void calDeltaArrays_ProbeTrail(){//this methods adjust the runing window mean and median delta according to the transition candidate suggusted by 
        //the cutoff of the filted envilope lines, 
        cvLRangesMean=new ArrayList();
        cvRRangesMean=new ArrayList();
        int i;
        boolean[] pbt;
//        pbt=new boolean[nDataSize];
//        CommonStatisticsMethods.markSelections(pbt,nvFiltedEnvDeltaPositions);
        pbt=pbEnvHighDiffClusterPositions;//12829       
        intRange ir=new intRange();
        ArrayList<Integer> positions=new ArrayList();
        int ll,rl,rr,lr;
        boolean bAdjust;
        double lMean,rMean,lMedian,rMedian,meanDiff,sign,meanDiffL,meanDiffR,correction,delta;
        int nDataSize=pdX.length,lls,lrs,rls,rrs,num;
        PolynomialLineFittingSegmentNode smoothSeg;
        
        for(i=0;i<nDataSize;i++){
            if(i==75) {
                i=i;
            }
            bAdjust=false;
            lr=i;
            ll=Math.max(0, lr-nWsDiff);
            rl=Math.min(i+nMaxRisingInterval,nDataSize-1);
            rr=Math.min(rl+nWsDiff, nDataSize-1);
            pdYRWMeanL[lr]=0;
            pdYRWMeanR[ll]=0;
            pdYRWMeanL[rr]=0;
            pdYRWMeanR[rl]=0;
            pdYRWMeanDiffLR_Downward[lr]=0;
            
            pdYRWMedianL[lr]=0;
            pdYRWMedianR[ll]=0;
            pdYRWMedianL[rr]=0;
            pdYRWMedianR[rl]=0;
            pdYRWMedianDiffLR[lr]=0;
            
            if(!pbSelected[i]) {
            
                cvLRangesMean.add(new intRange(ll,lr));
                cvRRangesMean.add(new intRange(rl,rr));
                continue;
            }
            if(i>0){
                ir.setRange(Math.max(ll-nMaxRisingInterval,0), lr);
                CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, lr-1, -1, ir, positions);
                if(!positions.isEmpty()){
                    ll=positions.get(0);
                    ll=Math.min(ll+nMaxRisingInterval, lr);
                    bAdjust=true;
                }
            }
                
            if(i<nDataSize-1){
                ir.setRange(rl, rr);
                CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, rl, 1, ir, positions);
                if(!positions.isEmpty()){
                    rr=positions.get(0);
                    bAdjust=true;
                }                 
            }
            
            cvLRangesMean.add(new intRange(ll,lr));
            cvRRangesMean.add(new intRange(rl,rr));
            
//            if(pbSelected[lr]){
            if(true){
                lMean=CommonStatisticsMethods.getMean(pdY, pbSelected,ll,lr, 1);
//                lMedian=CommonStatisticsMethods.getMedian(pdY, pbSelected, ll,lr, 1);
                lMean=getMean(ll,lr);
                lMedian=getMedian(ll,lr);
            } else {
//                lMean=pdProbeTrail_Downward[lr];
//                lMedian=lMean;               
            }
            
            if(true){
                rMean=pdProbeTrail_Downward[rl];//12n17          
//                rMedian=rMean;
                rMean=getMean(rl,rr);
                rMedian=getMedian(rl,rr);
            } else {
                rMean=CommonStatisticsMethods.getMean(pdY, pbSelected, rl,rr, 1);            
                rMedian=CommonStatisticsMethods.getMedian(pdY, pbSelected, rl,rr, 1);
            }
            
            if(Double.isNaN(rMean)) rMean=lMean;
            if(Double.isNaN(rMedian)) rMedian=lMedian;
            pdYRWMeanL[lr]=lMean;
            pdYRWMeanR[ll]=lMean;
            pdYRWMeanL[rr]=rMean;
            pdYRWMeanR[rl]=rMean;
//            meanDiff=pdProbeTrail_Downward[lr]-pdProbeTrail_Downward[rl];
            meanDiff=pdProbeTrail_Downward[lr]-pdProbeTrail_Downward[rl];
            sign=1.;
            if(lr==3){
                lr=lr;
            }
            if(meanDiff<0) sign=-1.;
/*            if(lr>nMaxRisingInterval){
                if(rl<nDataSize-nMaxRisingInterval){
                    meanDiffL=pdProbeTrail_Downward[lr-nMaxRisingInterval]-pdProbeTrail_Downward[lr];
                    meanDiffR=pdProbeTrail_Downward[rl]-pdProbeTrail_Downward[rl+nMaxRisingInterval];
                    correction=meanDiffL;
                    if(sign*(meanDiffR-meanDiffL)>0) correction=meanDiffR;
                }else{
                    correction=pdProbeTrail_Downward[lr-nMaxRisingInterval]-pdProbeTrail_Downward[lr];
                }
            }else{
                if(rl<nDataSize-nMaxRisingInterval){
                    correction=pdProbeTrail_Downward[rl]-pdProbeTrail_Downward[rl+nMaxRisingInterval];
                }else{
                    correction=0;
                }
            }*/
            correction=0;
            delta=0;
            if(isSmoothProfile(i)){
                smoothSeg=m_pcSmoothSegments[i];
                num=0;
                lls=Math.max(lr-nMaxRisingInterval,smoothSeg.nStart);
                lrs=lr;
                rls=rl;
                rrs=Math.min(smoothSeg.nEnd,rl+nMaxRisingInterval);
                rrs=Math.min(rrs, nDataSize-1);
                if(lrs>lls) {
                    delta=(smoothSeg.predict(pdX[lrs])-smoothSeg.predict(pdX[lls]))/(pdX[lrs]-pdX[lls]);
                    num++;
                }
                if(rrs>rls) {
                    delta+=(smoothSeg.predict(pdX[rrs])-smoothSeg.predict(pdX[rls]))/(pdX[rrs]-pdX[rls]);
                    num++;
                }
                if(num>0)
                    delta/=num;         
            }
            correction=delta*(rl-lr);
            correction=0;//12n28
//            if(sign*correction<0) correction=0;
            meanDiff+=correction;
            if(sign*meanDiff<0) meanDiff=0;
            pdYRWMeanDiffLR_Downward[lr]=meanDiff;
//            pdYRWMeanDiffLR_Downward[lr]=lMean-rMean;
            
            pdYRWMedianL[lr]=lMedian;
            pdYRWMedianR[ll]=lMedian;
            pdYRWMedianL[rr]=rMedian;
            pdYRWMedianR[rl]=rMedian;
            pdYRWMedianDiffLR[lr]=lMedian-rMedian;
        }
    }
    double getMean(int left, int right){
//        double mean=CommonStatisticsMethods.getMean(pdY, pbSelected,left,right, 1);
        double mean=CommonStatisticsMethods.getMean(pdProbeTrail_Downward,pbFullSelection,left,right, 1);//12o25
        return mean;
    }
    double getMedian(int left, int right){
//        double median=CommonStatisticsMethods.getMedian(pdY, pbSelected,left,right, 1);
        double median=CommonStatisticsMethods.getMedian(pdProbeTrail_Downward,pbFullSelection,left,right, 1);//12o25
        return median;
    }
    int adjustStartingAndEndingSegments(){ 
        //the cutoff of the filted envilope lines, 
        int p,position=0,iF=nDataSize-1;
//        if(true) return -1;//do not do it with this version
        boolean[] pbt=new boolean[nDataSize];
        ArrayList<Integer> nvNonZeroPositions=new ArrayList();
        CommonStatisticsMethods.getNonZeroPositionsWithinRange(pnHighDiffClusterPosition, 0, 1, new intRange(0,nDataSize-1), nvNonZeroPositions);
        CommonStatisticsMethods.markSelections(pbt,nvNonZeroPositions);
                
        intRange ir=new intRange();
        ArrayList<Integer> positions=new ArrayList();
        int ll=0,rr=0,lr,rl;
        boolean bAdjustL,bAdjustR,bAdjust;
        PolynomialLineFittingSegmentNode lSeg,rSeg;
        
        for(position=0;position<nDataSize;position++){
            bAdjustL=false;
            bAdjustR=false;
            if(position==67) {
                position=position;
            }
            lSeg=m_pcEndingSegments[position];
            rl=position+nMaxRisingInterval;
            if(rl<=iF)
                rSeg=m_pcStartingSegments[rl];
            else
                rSeg=null;
            
            if(lSeg!=null&&rSeg!=null){
                bAdjust=false;
                ll=lSeg.nStart;
                lr=lSeg.nEnd;
                rl=rSeg.nStart;
                rr=rSeg.nEnd;
                ir.setRange(Math.max(0,ll-nMaxRisingInterval), lr);
                CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, lr-1, -1, ir, positions);
                if(!positions.isEmpty()){
                    ll=Math.min(lr, positions.get(0)+nMaxRisingInterval);
                    lSeg=m_cSignalFitter.getLineSegment(ll, lr);
                    bAdjust=true;
                }
                
                ir.setRange(rl, rr);
                CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, rl+1, 1, ir, positions);
                if(!positions.isEmpty()){
                    rr=positions.get(0);
                    rSeg=m_cSignalFitter.getLineSegment(rl,rr);
                    bAdjust=true;
                }     
                p=(lr+rl)/2;
                pdDeltaOLR[position]=lSeg.predict(pdX[lr])-rSeg.predict(pdX[rl]);
                pdDeltaOLRModified[position]=lSeg.predict(pdX[p])-rSeg.predict(pdX[p]);
            }
            
            if(lSeg==null)
                bAdjustL=true;
            else if(!lSeg.isValid())
                bAdjustL=true;
            else if(!lSeg.isValidLocalDeviations())
                bAdjustL=true;
            
            if(rSeg==null)
                bAdjustL=true;
            else if(!rSeg.isValid())
                bAdjustL=true;
            else if(!rSeg.isValidLocalDeviations())
                bAdjustL=true;
            positions.clear();
            
            CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, position,position+nMaxRisingInterval, ir, positions);
            if(positions.size()<nMaxRisingInterval+1) bAdjust=true;
            if(!bAdjustL&&!bAdjustR) continue;
            pdDeltaOLR[position]=pdYRWMeanDiffLR_Downward[position];
            pdDeltaOLRModified[position]=pdYRWMeanDiffLR_Downward[position];
        }
        return 1;
    }
    
    OneDKMeans buildEnvDeltaCutoffBasedDiffCluster(String title, double[] pdDiffs,int digitIndex, ArrayList<Double> dvDiffs, ArrayList<Integer> nvHighDiffClusterPositions, ArrayList<Integer> nvLowDiffClusterPositions, 
            ArrayList<Integer> nvHighDiffClusterPositionsE,int[] pnHighDiffClusterPosition, int[] pnLowDiffClusterPosition, int[] pnHighDiffClusterPositionE){
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        dvDiffs.clear();//12d05
        int i, len=pdDiffs.length, position,positionp;
        double[] pdDiffAbs=new double[len];
        for(i=0;i<len;i++){
            pdDiffAbs[i]=Math.abs(pdDiffs[i]);
        }
        pdDiffAbs[0]=0;//12d01 this is to eliminate the transition from a single-point first level.
        
        CommonMethods.LocalExtrema(pdDiffAbs, nvLn, nvLx);   
        if(nvLn.isEmpty()||nvLx.isEmpty()) {//this is to avoid crash, and need to implement real fix later.//13209
            nvLn.add(0);
            nvLx.add(1);
        }
        len=nvLx.size();
        if(nvLx.get(len-1)>=pdFiltedEnvDelta_Downward.length){
            nvLx.remove(len-1);
            len--;
        }        
        
        int[] pnIndexes=new int[len];
        double diffAbs;
        for(i=0;i<len;i++){
            pnIndexes[i]=i;
            position=nvLx.get(i);  
            if(position==14){
                i=i;
            }
            if(position>=pdFiltedEnvDelta_Downward.length) {
                diffAbs=pdDiffAbs[position];
            }else if(Math.abs(pdFiltedEnvDelta_Downward[position])<dEnvDeltaCutoff&&bFiltedEnvCutoff){
                positionp=getOptimalJumpingPosition(pdFiltedEnvDelta_Downward,position,pdDiffs[position]);
                if(Math.abs(pdFiltedEnvDelta_Downward[positionp])<dEnvDeltaCutoff)
                    diffAbs=Math.abs(CommonStatisticsMethods.getMinAbsElement(pdDiffs[position],pdFiltedEnvDelta_Downward[position]));
                else
                    diffAbs=pdDiffAbs[position];
            }else{
                diffAbs=pdDiffAbs[position];
            }
            dvDiffs.add(diffAbs);
        }
        
        int[] pnClusterIndexes=new int[nvLx.size()];
        
        utilities.QuickSort.quicksort(CommonStatisticsMethods.copyToDoubleArray(dvDiffs), pnIndexes);
        pnClusterIndexes[pnIndexes[nvLx.size()-1]]=1;
        
        OneDKMeans cKM=new OneDKMeans(title, dvDiffs,2,pnClusterIndexes);
        if(cKM==null) return null;
        OneDKMeans cKME;
/*        
        if(cKM.getClusterSizes()[1]==1&&cKM.m_nSize>2)
            cKME=CommonStatisticsMethods.OneDKMeans_LargestAttenuated(title, dvDiffs, 2);
        else
            cKME=cKM;*/
        //new OneDKMeans_ExtremExcluded(title+"_E", dvDiffs,2,1);
        cKME=CommonStatisticsMethods.OneDKMeans_LargestAttenuated(title, dvDiffs, 2);
        if(cKME==null) 
            cKME=cKM;
        pnClusterIndexes=cKM.getClusterIndexes();
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.appendStrings(cKM.toStrings());
        
        int[] pnClusterIndexesE=cKME.getClusterIndexes();
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.appendStrings(cKME.toStrings());
        
        int positionDx;
        
        int gapPosition=CommonStatisticsMethods.findGapPosition(dvDiffs, pnIndexes);
        len=pnIndexes.length;
        String st=title+" Optimal Gap Position:"+gapPosition;
        for(i=0;i<=gapPosition;i++){
            pnClusterIndexes[pnIndexes[len-1-i]]=1;
        }
        
        AnalysisMasterForm.appendText(PrintAssist.newline);
        AnalysisMasterForm.appendText(st);
        AnalysisMasterForm.appendText(PrintAssist.newline);
        
        for(i=0;i<nvLx.size();i++){
            position=nvLx.get(i);
            if(position==68){
                i=i;
            }
            if(position>=pdFiltedEnvDelta_Downward.length) continue;
            if(bAligningToDeltaExtrema)
                positionDx=getOptimalTransitionPosition(position,pdDiffs[position]);
            else
                positionDx=position;
            
            if(pnClusterIndexes[i]==0) {
                nvLowDiffClusterPositions.add(position);
                pnLowDiffClusterPosition[positionDx]=CommonMethods.setDigit(pnLowDiffClusterPosition[positionDx], digitIndex, 1);                
            }
            if(pnClusterIndexes[i]==1) {
                nvHighDiffClusterPositions.add(position);            
                pnHighDiffClusterPosition[positionDx]=CommonMethods.setDigit(pnHighDiffClusterPosition[positionDx], digitIndex, 1);   
            }
            
            if(pnClusterIndexesE[i]!=0) {
                nvHighDiffClusterPositionsE.add(position);            
                pnHighDiffClusterPositionE[positionDx]=CommonMethods.setDigit(pnHighDiffClusterPositionE[positionDx], digitIndex, 1);   
            }
        }
        return cKM;
    } 
    int getOptimalJumpingPosition(double[] pdT, int position, double dSign){
        if(position<0||position>=pdT.length) return position;
        int sign=1;
        if(dSign<0) sign=-1;
        ArrayList<Integer> positions=CommonStatisticsMethods.getExtremaPositions(pdT, position, sign);
        int pp=CommonStatisticsMethods.getLargestPositionInRange(pdT, positions, new intRange(Math.max(0,position-nMaxRisingInterval),Math.min(position+nMaxRisingInterval,pdFiltedEnvDelta_Downward.length)), sign);
        if(pp<0) 
            pp=position;
        return pp;
    }
    int getOptimalTransitionPosition(int position, double dSign){
        if(position<0||position>=pdFiltedEnvDelta_Downward.length) return position;        
        int pp=getOptimalJumpingPosition(pdFiltedEnvDelta_Downward,position,dSign);
        int pt=getOptimalJumpingPosition(pdDeltaY, pp, dSign);
        if(pt>=0)
            return pt;
        else
            return position;
    }
    void buildAndMarkDiffClusters(){//this methods adjust the runing window mean and median delta according to the transition candidate suggusted by 
        int len;
        pnLowDiffClusterPosition=CommonStatisticsMethods.getEmptyIntArray(pnLowDiffClusterPosition, nDataSize);
        pnHighDiffClusterPosition=CommonStatisticsMethods.getEmptyIntArray(pnHighDiffClusterPosition, nDataSize);
        pnHighDiffClusterPositionE=CommonStatisticsMethods.getEmptyIntArray(pnHighDiffClusterPositionE, nDataSize);
        
        cKMDeltaPW=CommonStatisticsMethods.buildAbsKMeans(pdDeltaY, 2);
        
        dvMeanDiffs_Downward=new ArrayList();
        dvSigdiffs=new ArrayList();
        dvMedianDiffs_Downward=new ArrayList();
        
        nvMeanHighDiffClusterPositions=new ArrayList();
        nvMeanHighDiffClusterPositionsE=new ArrayList();
        nvMeanLowDiffClusterPositions=new ArrayList();
        
        nvSigdiffHighDiffClusterPositions=new ArrayList();
        nvSigdiffHighDiffClusterPositionsE=new ArrayList();
        nvSigdiffLowDiffClusterPositions=new ArrayList();
        
        nvMedianHighDiffClusterPositions=new ArrayList();
        nvMedianHighDiffClusterPositionsE=new ArrayList();
        nvMedianLowDiffClusterPositions=new ArrayList();
        
        nvRegressionHighDiffClusterPositions=new ArrayList();
        nvRegressionHighDiffClusterPositionsE=new ArrayList();
        nvRegressionLowDiffClusterPositions=new ArrayList();
        
        nvEnvHighDiffClusterPositions=new ArrayList();
        nvEnvHighDiffClusterPositionsE=new ArrayList();
        nvEnvLowDiffClusterPositions=new ArrayList();
        dvFiltedEnvDelta_Downward.clear();
        calSigDiff();
//        calSigDiff_EdgeProtectedFilter();
        bFiltedEnvCutoff=false;
        bAligningToDeltaExtrema=false;
        cKMDeltaMean=buildEnvDeltaCutoffBasedDiffCluster("Mean diff", pdYRWMeanDiffLR_Downward,3,dvMeanDiffs_Downward,nvMeanHighDiffClusterPositions,nvMeanLowDiffClusterPositions,nvMeanHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
        cKMSigDiff=buildEnvDeltaCutoffBasedDiffCluster("Sig diff", pdSigDiff,0,dvSigdiffs,nvSigdiffHighDiffClusterPositions,nvSigdiffLowDiffClusterPositions,nvSigdiffHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
        bFiltedEnvCutoff=true;
        bAligningToDeltaExtrema=true;
//        cKMDeltaMedian=buildEnvDeltaCutoffBasedDiffCluster("Median diff", pdYRWMedianDiffLR,1,dvMedianDiffs_Downward,nvMedianHighDiffClusterPositions,nvMedianLowDiffClusterPositions,nvMedianHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
//        cKMDeltaEnv=buildEnvDeltaCutoffBasedDiffCluster("Env diff", pdFiltedEnvDelta_Downward,3,dvFiltedEnvDelta_Downward,nvEnvHighDiffClusterPositions,nvEnvLowDiffClusterPositions,nvEnvHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
        
//        if(cKMDeltaMean.getDataDv().size()>2) cKM_MeanDiff_ExtremeExclusion=new OneDKMeans_ExtremExcluded("Mean diff E", cKMDeltaMean.getDataDv(), 2, 1);
//        if(cKMDeltaMedian.getDataDv().size()>2) cKM_MedianDiff_ExtremeExclusion=new OneDKMeans_ExtremExcluded("Median diff E", cKMDeltaMedian.getDataDv(), 2, 1);
        if(cKMDeltaMean.getDataDv().size()>2) 
            cKMDeltaMeanE=CommonStatisticsMethods.OneDKMeans_LargestAttenuated("Mean diff E", cKMDeltaMean.getDataDv(), 2);     
        else
            cKMDeltaMeanE=cKMDeltaMean;
        
        if(cKMSigDiff.getDataDv().size()>2) 
            cKMSigDiffE=CommonStatisticsMethods.OneDKMeans_LargestAttenuated("Mean diff E", cKMSigDiff.getDataDv(), 2);     
        else
            cKMSigDiffE=cKMSigDiff;
        
        len=nvEnvDeltaAbsLx.size();
        if(nvEnvDeltaAbsLx.get(len-1)>=pdFiltedEnvDelta_Downward.length){
            nvEnvDeltaAbsLx.remove(len-1);
            len--;
        }
    }
    
    void buildAndMarkRegressionClusters(){//this methods adjust the runing window mean and median delta according to the transition candidate suggusted by 
        dvRegressionDiffs_Downward=new ArrayList();
        nvRegressionHighDiffClusterPositions=new ArrayList();
        nvRegressionHighDiffClusterPositionsE=new ArrayList();
        nvRegressionLowDiffClusterPositions=new ArrayList();
        cKMDeltaRegression=buildEnvDeltaCutoffBasedDiffCluster("Regression diff", pdDeltaOLR,2,dvRegressionDiffs_Downward,nvRegressionHighDiffClusterPositions,nvRegressionLowDiffClusterPositions,nvRegressionHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
//        if(cKMDeltaRegression.getDataDv().size()>2) cKM_RegressionDiff_ExtremeExclusion=new OneDKMeans_ExtremExcluded("Regression diff E", cKMDeltaRegression.getDataDv(), 2, 1);
        if(cKMDeltaRegression.getDataDv().size()>2) 
            cKMDeltaRegressionE=CommonStatisticsMethods.OneDKMeans_LargestAttenuated("Regression diff E", cKMDeltaRegression.getDataDv(), 2);     
        else
            cKMDeltaRegressionE=cKMDeltaRegression;
    }
       
    int displayTransitionInfo(String headLine, ArrayList<Integer> nvPositions){
        int i, position,positionDx,len=nvPositions.size();
        
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.appendText(headLine);
        AnalysisMasterForm.newLine();
        
        SignalTransitionStatisticsTests test;
        
        for(i=0;i<len;i++){
            position=nvPositions.get(i);
            displaySignalDiffs(position);
            positionDx=CommonStatisticsMethods.getMaxAbsPosition(pdDeltaYAbs, position);
            AnalysisMasterForm.appendText("  transition point X="+PrintAssist.ToString(pdX[positionDx],3)+"  ");
            test=pcTransitionTests[position];
            if(test==null) {
                AnalysisMasterForm.newLine();
                continue;
            }
            AnalysisMasterForm.appendText("  "+test.getTestsAsString());
            AnalysisMasterForm.newLine();
//            if(pnHighDiffClusterPosition[position]>0)test.displaySegments(pw);
        }
        return 1;
    }
    
    void displayTransitionInfo(){
//        pw=new PlotWindowPlus(pdX,pdY,"Feature Info. Mean","X","Y",1,2,Color.black);
    
        displayTransitionInfo("Confirmed Transitions", nvDetectedTransitionPoints);
        displayTransitionInfo("Confirmed Transitions_E", nvDetectedTransitionPointsE);
        
        displayTransitionInfo("Mean Diff. in High Clusters",nvMeanHighDiffClusterPositions);
        displayTransitionInfo("Mean Diff. in High Clusters_E",nvMeanHighDiffClusterPositionsE);
        
        displayTransitionInfo("Sig Diff. in High Clusters",nvSigdiffHighDiffClusterPositions);
        displayTransitionInfo("Sig Diff. in High Clusters_E",nvSigdiffHighDiffClusterPositionsE);
//        displayTransitionInfo("Mean Diff. in Low Clusters",nvMeanLowDiffClusterPositions);
        
        displayTransitionInfo("Median Diff. in High Clusters",nvMedianHighDiffClusterPositions);
        displayTransitionInfo("Median Diff. in High Clusters_E",nvMedianHighDiffClusterPositionsE);
//        displayTransitionInfo("Median Diff. in Low Clusters",nvMedianLowDiffClusterPositions);
        
        displayTransitionInfo("Regression Diff. in High Clusters",nvRegressionHighDiffClusterPositions);
        displayTransitionInfo("Regression Diff. in High Clusters_E",nvRegressionHighDiffClusterPositionsE);
//        displayTransitionInfo("Regression Diff. in Low Clusters",nvRegressionLowDiffClusterPositions);
        
        int i,lenD=pdFiltedEnvDelta_Downward.length;
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.appendText("All Signal Diff");
        AnalysisMasterForm.newLine();
        for(i=0;i<lenD;i++){
            displaySignalDiffs(i);
            AnalysisMasterForm.newLine();
        }
    }
    void displaySignalDiffs(int position){
        double delta;
        if(position<pdDeltaY.length) 
            delta=pdDeltaY[position];
        else
            delta=0;
        
        String st="";
        st="x"+PrintAssist.ToString(position,4,0)+" = "+PrintAssist.ToString(pdX[position], 12, 3);
        st+="  lLen"+" = "+PrintAssist.ToString(cvLRangesMean.get(position).getRange(), 6, 0);
        st+="  rLen"+" = "+PrintAssist.ToString(cvRRangesMean.get(position).getRange(), 6, 0);
        st+="  meanDiff"+" = "+PrintAssist.ToString(pdYRWMeanDiffLR_Downward[position], 12, 3);
        st+="  sigdiff"+" = "+PrintAssist.ToString(pdSigDiff[position], 12, 3);
        st+="  meanDiff_U"+" = "+PrintAssist.ToString(pdvProbeTrailDelta_Upward.get(nNumBallScales)[position], 12, 3);
        st+="  medianDiff"+" = "+PrintAssist.ToString(pdYRWMedianDiffLR[position], 12, 3);
        if(position<pdFiltedEnvDelta_Downward.length) {
            st+="  EnvDiff"+" = "+PrintAssist.ToString(pdFiltedEnvDelta_Downward[position], 12, 3);
            st+="  DeltaYf"+" = "+PrintAssist.ToString(delta, 12, 3);
        } else {
            st+="  EnvDiff"+" = "+PrintAssist.ToString(0, 12, 3);
            st+="  DeltaY"+" = "+PrintAssist.ToString(delta, 12, 3);
        }
            
        st+="  RegressionDiff"+" = "+PrintAssist.ToString(pdDeltaOLR[position], 12, 3);
        st+="  RegressionDiffM"+" = "+PrintAssist.ToString(pdDeltaOLRModified[position], 12, 3);
        AnalysisMasterForm.appendText(st);
    }
    
    public ArrayList<Integer> getDetectedTransitionPoints(){
        return nvDetectedTransitionPoints;
    }
    public ArrayList<Integer> getDetectedTransitionPointsE(){
        return nvDetectedTransitionPointsE;
    }
    public ArrayList<Integer> getHighClusterPoints(){
        return nvHighClusterPoints;
    }
    public ArrayList<Integer> getHighClusterPointsE(){
        return nvHighClusterPointsE;
    }
    
    public static double getModifiedDelta(SimpleRegression segL, SimpleRegression segR, double[] pdX, double[] pdYRWMean, int l, int r){
        double dXL=pdX[l],dXR=pdX[r];
        double dYLL,dYLR,dYRL,dYRR;
        if(segL!=null) {
            dYLL=segL.predict(dXL);
            dYLR=segL.predict(dXR);
        } else {
            dYLL=pdYRWMean[l];
            dYLR=pdYRWMean[r];
        }
        if(segR!=null) {
            dYRL=segR.predict(dXL);
            dYRR=segR.predict(dXR);
        } else {
            dYRL=pdYRWMean[l];
            dYRR=pdYRWMean[r];
        }
        double delta=0.5*(dYLL+dYLR-dYRL-dYRR);
        return delta;
    }
    public PolynomialLineFitter fitTrackSignal_Addaptive(int order, double dPChiSQ,double dPTilting,double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
        nMaxSegLen=20;
        int len=pdX.length,i,j;
        int nWsTilting=3,nWsPWDev=0;
        this.dPChiSQ=dPChiSQ;
        this.dPPWDev=dPPWDev;
        this.dPTilting=dPTilting;
        this.dPSideness=dPSideness;
       
        m_cSignalFitter=new LineFitter_AdaptivePolynomial(pdX,pdY,pbSelected,nMaxRisingInterval,order,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals, dOutliarRatio);
        
        m_pcOptimalSegments=m_cSignalFitter.getOptimalRegressions();
        m_pcStartingSegments=m_cSignalFitter.getStartingRegressions();
        m_pcEndingSegments=m_cSignalFitter.getEndingRegressions();
        m_pcLongSegments=m_cSignalFitter.getLongRegressions();
        return m_cSignalFitter;
    }       
    public PolynomialLineFitter fitTrackSignal_ProgressiveSegmentation(String sFitterOption, String sDataSelectionOption, int order, double pPDelta,double dPChiSQ,double dPTilting,double dPSideness, double dPPWDev,double dOutliarRatio, boolean defaultCompletion){
        if(sFitterOption==null) sFitterOption="Progressive Segmentation";
        int nMaxSegLen=30;
        int len=pdX.length,i,j;
        int nWsTilting=3,nWsPWDev=0;
        this.dPChiSQ=dPChiSQ;
        this.dPPWDev=dPPWDev;
        this.dPTilting=dPTilting;
        this.dPSideness=dPSideness;
       
        m_cSignalFitter=new PiecewisePolynomialLineFitter_ProgressiveSegmenting(sSignalName,pdX,pdY,pdSD,null,pbSelected,nMaxRisingInterval,order,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dOutliarRatio,defaultCompletion);
        
        
        m_pcOptimalSegments=m_cSignalFitter.getOptimalRegressions();
        m_pcStartingSegments=m_cSignalFitter.getStartingRegressions();
        m_pcEndingSegments=m_cSignalFitter.getEndingRegressions();
        m_pcLongSegments=m_cSignalFitter.getLongRegressions();
        return m_cSignalFitter;
    } 
   public void buildBallTrails(){
        double dRx=50;
        int i,nRanking=0,len=nvBallRollingWs.size(),j,len1,index,len0,it,jt,position0,position;
        double[] pdXT,pdYT;
        pdvProbeContactingPointsDevX_Downward=new ArrayList();
        pdvProbeContactingPointsSDX_Downward=new ArrayList();
        pdvProbeContactingPointsDevY_Downward=new ArrayList();
        pdvProbeContactingPointsSDY_Downward=new ArrayList();    
        pdvProbeContactingPointsDevX_Upward=new ArrayList();
        pdvProbeContactingPointsSDX_Upward=new ArrayList();
        pdvProbeContactingPointsDevY_Upward=new ArrayList();
        pdvProbeContactingPointsSDY_Upward=new ArrayList();    
        
        pdvProbeContactingPointsX_Downward=new ArrayList();
        pdvProbeContactingPointsY_Downward=new ArrayList();        
        pdvProbeContactingPointsX_Upward=new ArrayList();
        pdvProbeContactingPointsY_Upward=new ArrayList();
        
        pdvProbeTrail_Downward=new ArrayList();
        pdvProbeTrailDelta_Downward=new ArrayList();
        nvvBallContactingExtrema_Downward=new ArrayList();
        pdvProbeTrail_Upward=new ArrayList();
        pdvProbeTrailDelta_Upward=new ArrayList();
        nvvBallContactingExtrema_Upward=new ArrayList();
        
        nvvProbeContactingPositions_Downward=new ArrayList();
        pnvTrailRisingIntervals_Downward=new ArrayList();
        pbvSemiballContacting_Downward=new ArrayList();
        nvvProbeContactingPositions_Upward=new ArrayList();
        pnvTrailRisingIntervals_Upward=new ArrayList();
        pbvSemiballContacting_Upward=new ArrayList();
        
        pbTopscaleAnchor_Downward=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbTopscaleAnchor_Downward, false);
        
        pbTopscaleAnchor_Upward=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbTopscaleAnchor_Upward, false);
        
        boolean[] pbSemiballContacting_Downward, pbSemiballContacting_Upward;        
        int[] pnIntervals_Downward,pnIntervals_Upward;
        
        int nMaxMultiplicity=2;
        double[] pdYDespiked;        
        double dPValue=0.001;
//        SpikeHandler detector=new SpikeHandler(pdX,pdY,nMaxMultiplicity,dPValue);
//        pdYDespiked=detector.removeSpikes_Progressive(nMaxMultiplicity);
        pdYDespiked=pdY;
        
//        PlotWindowPlus pwTest=CommonGuiMethods.displayNewPlotWindowPlus("Test Ball Trail", pdX, pdY, 1, 2, Color.black);
//        pdYDespiked=pdY;//13124
          
       
        for(i=0;i<len;i++){
            dRx=nvBallRollingWs.get(i);
            ProbingBall pb=new ProbingBall(pdX,pdYDespiked,dRx,-1,nRanking);
            pdProbeTrail_Downward=pb.getProbeTrail(ProbingBall.Downward);     
//            pwTest.addPlot("Rx: "+PrintAssist.ToString(dRx, 1), pdX, pdProbeTrail_Downward, 1, 2, CommonGuiMethods.getDefaultColor(i+1));
            pdvProbeTrail_Downward.add(pdProbeTrail_Downward);
            pnIntervals_Downward=CommonStatisticsMethods.calRisingIntervals(pdProbeTrail_Downward, nMaxRisingInterval);
            pnvTrailRisingIntervals_Downward.add(pnIntervals_Downward);
            
            pdProbeTrail_Upward=pb.getProbeTrail(ProbingBall.Upward);        
            pdvProbeTrail_Upward.add(pdProbeTrail_Upward);
            pnIntervals_Upward=CommonStatisticsMethods.calRisingIntervals(pdProbeTrail_Upward, nMaxRisingInterval);
            pnvTrailRisingIntervals_Upward.add(pnIntervals_Upward);
            
            nvProbeContactingPosition_Downward=pb.getProbeContactingPositions(ProbingBall.Downward);
            pbSemiballContacting_Downward=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbSemiballContacting_Downward, false);
            CommonStatisticsMethods.setElements(pbSemiballContacting_Downward, nvProbeContactingPosition_Downward, true);
            pbvSemiballContacting_Downward.add(pbSemiballContacting_Downward);
            
            nvProbeContactingPosition_Upward=pb.getProbeContactingPositions(ProbingBall.Upward);
            pbSemiballContacting_Upward=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbSemiballContacting_Upward, false);
            CommonStatisticsMethods.setElements(pbSemiballContacting_Upward, nvProbeContactingPosition_Upward, true);
            pbvSemiballContacting_Upward.add(pbSemiballContacting_Upward);
            
            nvvProbeContactingPositions_Downward.add(nvProbeContactingPosition_Downward);
            nvvProbeContactingPositions_Upward.add(nvProbeContactingPosition_Upward);
            
//            if(i==0){//12d21
                CommonStatisticsMethods.setElements(pbTopscaleAnchor_Downward, false);
                CommonStatisticsMethods.setElements(pbTopscaleAnchor_Downward, nvProbeContactingPosition_Downward,true);
                ArrayList<Integer> nv0=pb.getBallContactingPositions(ProbingBall.Downward),nv=new ArrayList();
                int iit;
                for(int k=0;k<nv0.size();k++){
                    iit=nv0.get(k);
                    if(CommonStatisticsMethods.isLocalExtrema(pdY, iit, 1)) nv.add(iit);                                                
                }
                nvvBallContactingExtrema_Downward.add(nv);
                
                CommonStatisticsMethods.setElements(pbTopscaleAnchor_Upward, false);
                CommonStatisticsMethods.setElements(pbTopscaleAnchor_Upward, nvProbeContactingPosition_Upward,true);
                nv0=pb.getBallContactingPositions(ProbingBall.Upward);
                nv=new ArrayList();
                for(int k=0;k<nv0.size();k++){
                    iit=nv0.get(k);
                    if(CommonStatisticsMethods.isLocalExtrema(pdY, iit, -1)) nv.add(iit);                                                
                }
                nvvBallContactingExtrema_Upward.add(nv);
//            }
            
            pdYT=CommonStatisticsMethods.getDeltaArray(pdProbeTrail_Downward, pnIntervals_Downward, nMaxRisingInterval);
            pdvProbeTrailDelta_Downward.add(pdYT);   
            
            pdYT=CommonStatisticsMethods.getDeltaArray(pdProbeTrail_Upward, pnIntervals_Upward, nMaxRisingInterval);
            pdvProbeTrailDelta_Upward.add(pdYT);            
            
            len1=nvProbeContactingPosition_Downward.size();
            pdXT=new double[len1];
            pdYT=new double[len1];
            for(j=0;j<len1;j++){
                index=nvProbeContactingPosition_Downward.get(j);
                pdXT[j]=pdX[index];
                pdYT[j]=pdY[index];
            }
            pdvProbeContactingPointsX_Downward.add(pdXT);
            pdvProbeContactingPointsY_Downward.add(pdYT);
            
            len1=nvProbeContactingPosition_Upward.size();
            pdXT=new double[len1];
            pdYT=new double[len1];
            for(j=0;j<len1;j++){
                index=nvProbeContactingPosition_Upward.get(j);
                pdXT[j]=pdX[index];
                pdYT[j]=pdY[index];
            }
            pdvProbeContactingPointsX_Upward.add(pdXT);
            pdvProbeContactingPointsY_Upward.add(pdYT);
            
            pdYT=CommonStatisticsMethods.getDevArray(pdX, pdProbeTrail_Downward, pnIntervals_Downward);            
            pdvProbeContactingPointsDevX_Downward.add(pdX);
            pdvProbeContactingPointsDevY_Downward.add(pdYT);
            
            
            pdYT=SequenceSegmenter_ProgressiveScales.getRWSD_Cumulative(pdYT, 20,0.01).get(1);            
            pdvProbeContactingPointsSDX_Downward.add(pdX);
            pdvProbeContactingPointsSDY_Downward.add(pdYT);
            
            
            pdYT=CommonStatisticsMethods.getDevArray(pdX, pdProbeTrail_Upward, pnIntervals_Downward);            
            pdvProbeContactingPointsDevX_Upward.add(pdX);
            pdvProbeContactingPointsDevY_Upward.add(pdYT);
            
            pdYT=SequenceSegmenter_ProgressiveScales.getRWSD_Cumulative(pdYT, 20,0.01).get(1);            
            pdvProbeContactingPointsSDX_Upward.add(pdX);
            pdvProbeContactingPointsSDY_Upward.add(pdYT);
        }
        calBallContactingPointsDevPValues();
        if(nDataSize>0){
            buildMiltiscaleBallTrails();   
            
            pdProbeTrail_Downward=pdvProbeTrail_Downward.get(nNumBallScales);//getting the multiscale trail
            pdProbeTrail_Upward=pdvProbeTrail_Upward.get(nNumBallScales);//getting the multiscale trail
            
            pdYT=CommonStatisticsMethods.getDeltaArray(pdProbeTrail_Downward, null, nMaxRisingInterval);
            pdvProbeTrailDelta_Downward.add(pdYT);               
            pdYT=CommonStatisticsMethods.getDeltaArray(pdProbeTrail_Upward, null, nMaxRisingInterval);
            pdvProbeTrailDelta_Upward.add(pdYT);            
            
            nvProbeContactingPosition_Downward=nvvProbeContactingPositions_Downward.get(nNumBallScales);
            nvProbeContactingPosition_Upward=nvvProbeContactingPositions_Upward.get(nNumBallScales);
            
            ArrayList<Integer> ln=new ArrayList(),lx=new ArrayList();
            CommonMethods.LocalExtrema(pdY, ln, lx);
            
            pbMultipleScaleAnchorLx_Downward=new boolean[nDataSize];            
            CommonStatisticsMethods.setElements(pbMultipleScaleAnchorLx_Downward, false);
            CommonStatisticsMethods.setElements_AND(pbMultipleScaleAnchorLx_Downward, nvvProbeContactingPositions_Downward.get(nNumBallScales), lx, true);
            pbMultipleScaleAnchorLn_Upward=new boolean[nDataSize];            
            CommonStatisticsMethods.setElements(pbMultipleScaleAnchorLn_Upward, false);
            CommonStatisticsMethods.setElements_AND(pbMultipleScaleAnchorLn_Upward, nvvProbeContactingPositions_Upward.get(nNumBallScales), ln, true);
            
            pbSemiballContacting_Downward=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbSemiballContacting_Downward, false);
            CommonStatisticsMethods.setElements(pbSemiballContacting_Downward, nvProbeContactingPosition_Downward, true);
            pbvSemiballContacting_Downward.add(pbSemiballContacting_Downward);
            pbSemiballContacting_Upward=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbSemiballContacting_Upward, false);
            CommonStatisticsMethods.setElements(pbSemiballContacting_Upward, nvProbeContactingPosition_Upward, true);
            pbvSemiballContacting_Downward.add(pbSemiballContacting_Downward);
            
        }else{
            pdProbeTrail_Downward=pdvProbeTrail_Downward.get(nNumBallScales-1);
            nvProbeContactingPosition_Downward=nvvProbeContactingPositions_Downward.get(nNumBallScales-1);
            pbMultipleScaleAnchorLx_Downward=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbMultipleScaleAnchorLx_Downward, false);
            ArrayList<Integer> ln=new ArrayList(),lx=new ArrayList();
            CommonMethods.LocalExtrema(pdY, ln, lx);
            CommonStatisticsMethods.setElements_AND(pbMultipleScaleAnchorLx_Downward, nvvProbeContactingPositions_Downward.get(nNumBallScales-1), lx, true);
        }
//        refineMultipleScaleTrails();//12d05
    }
    void calBallContactingPointsDevPValues(){
        int i;
        ArrayList<Integer> positions=nvvBallContactingExtrema_Downward.get(0);
        ArrayList<double[]> line=CommonStatisticsMethods.getDevArray(pdX, pdY, positions);
        double[] pdt=CommonStatisticsMethods.getPValues(line.get(1), 0, positions.size()-1, 1, 0.001);
        pdBallContactingPointsDevPValues_Downward=new double[nDataSize];
        CommonStatisticsMethods.setElements(pdBallContactingPointsDevPValues_Downward,1.1);
        for(i=0;i<positions.size();i++){
            pdBallContactingPointsDevPValues_Downward[positions.get(i)]=pdt[i];
        }
    }
    int buildMiltiscaleBallTrails(){
        int nScale;
        
        double[] pdY,pdYT;
        
        ArrayList<double[]> pdvD_Downward=new ArrayList(),pdvH_Downward=new ArrayList();
        ArrayList<double[]> pdvD_Upward=new ArrayList(),pdvH_Upward=new ArrayList();
        
        nvvAnchoredBallTrailDeltaLx_Downward=new ArrayList();
        nvvAnchoredBallTrailDeltaLn_Upward=new ArrayList();
        ArrayList<Integer> nvAnchors,nvLx=new ArrayList(),nvLn=new ArrayList(),nvSP=new ArrayList(),nvAnchoredDeltaLx=new ArrayList();
        cvvBallTrailSegmenters_Downward=new ArrayList();
        cvvBallTrailSegmenters_Upward=new ArrayList();
        
        boolean[] pbAnchored=new boolean[nDataSize];
        boolean[] pbSelectedDelta;
        boolean[] pbSelectedRisingNeighbors;
        int[] pnRisingIntervals;
        
        ArrayList<IntPair[]> pcvRisingNeighbors_Downward=new ArrayList();
        ArrayList<IntPair[]> pcvRisingNeighbors_Upward=new ArrayList();
        
        for(nScale=0;nScale<nvBallRollingWs.size();nScale++){
            //downward
            nvAnchors=nvvProbeContactingPositions_Downward.get(nScale);
            CommonStatisticsMethods.setElements(pbAnchored, false);
            CommonStatisticsMethods.setElements(pbAnchored, nvAnchors, true);            
            
            pnRisingIntervals=pnvTrailRisingIntervals_Downward.get(nScale);
            pcvRisingNeighbors_Downward.add(CommonStatisticsMethods.getRisingNeighbors(pnRisingIntervals, nMaxRisingInterval,nDataSize));
            
            pbSelectedDelta=CommonStatisticsMethods.getDeltaSelection_OR(pbAnchored, pnRisingIntervals);
            pbSelectedRisingNeighbors=CommonStatisticsMethods.getSelectedRisingNeighbors(pbAnchored, pnRisingIntervals);
            
            pdY=pdvProbeTrailDelta_Downward.get(nScale);
            pdYT=new double[nDataSize];
            CommonStatisticsMethods.setElements(pdYT, 0);
            CommonStatisticsMethods.copyArray(pdY, pdYT, pbSelectedDelta);
            pdvD_Downward.add(CommonStatisticsMethods.getAbs(pdYT));
           
            pdY=pdvProbeContactingPointsDevY_Downward.get(nScale);
            pdYT=new double[nDataSize];
            CommonStatisticsMethods.setElements(pdYT, 0);
            CommonStatisticsMethods.copyArray(pdY,pdYT,pbSelectedRisingNeighbors);
            pdvH_Downward.add(CommonStatisticsMethods.getAbs(pdYT));
            
            //now upward
            
            nvAnchors=nvvProbeContactingPositions_Upward.get(nScale);
            CommonStatisticsMethods.setElements(pbAnchored, false);
            CommonStatisticsMethods.setElements(pbAnchored, nvAnchors, true);            
            
            pnRisingIntervals=pnvTrailRisingIntervals_Upward.get(nScale);
            pcvRisingNeighbors_Upward.add(CommonStatisticsMethods.getRisingNeighbors(pnRisingIntervals, nMaxRisingInterval,nDataSize));
            
            pbSelectedDelta=CommonStatisticsMethods.getDeltaSelection_OR(pbAnchored, pnRisingIntervals);
            pbSelectedRisingNeighbors=CommonStatisticsMethods.getSelectedRisingNeighbors(pbAnchored, pnRisingIntervals);
            
            pdY=pdvProbeTrailDelta_Upward.get(nScale);
            pdYT=new double[nDataSize];
            CommonStatisticsMethods.setElements(pdYT, 0);
            CommonStatisticsMethods.copyArray(pdY, pdYT, pbSelectedDelta);
            pdvD_Upward.add(CommonStatisticsMethods.getAbs(pdYT));
           
            pdY=pdvProbeContactingPointsDevY_Upward.get(nScale);
            pdYT=new double[nDataSize];
            CommonStatisticsMethods.setElements(pdYT, 0);
            CommonStatisticsMethods.copyArray(pdY,pdYT,pbSelectedRisingNeighbors);
            pdvH_Upward.add(CommonStatisticsMethods.getAbs(pdYT));
        }
        //downward
        SequenceSegmenter_ProgressiveScales segmenterH=new SequenceSegmenter_ProgressiveScales(pdvH_Downward,pcvRisingNeighbors_Downward,SequenceSegmenter_ProgressiveScales.isolated,dvPSegmentation,null,SequenceSegmenter_ProgressiveScales.larger);    
        SequenceSegmenter_ProgressiveScales segmenterD=new SequenceSegmenter_ProgressiveScales(pdvD_Downward,pcvRisingNeighbors_Downward,SequenceSegmenter_ProgressiveScales.right,dvPSegmentation,dvPConsolidation,SequenceSegmenter_ProgressiveScales.larger); 
        pdvP_TrailDelta_Downward=segmenterD.getPValues();
        pdvP_TrailHeight_Downward=segmenterH.getPValues();
        
        segmenterD.combineSegmenters_AND(segmenterH.cvvSegmenters);        
        refineMultipleScaleSegmenter(segmenterD, true);
        
        segmenterD.buildAnchoredSegments(pdvProbeTrail_Downward,pnvTrailRisingIntervals_Downward,nvvProbeContactingPositions_Downward);
        pdvProbeTrail_Downward.add(segmenterD.buildMultiscaleTrail_Downward(pdvProbeTrail_Downward));
        pnvTrailRisingIntervals_Downward.add(CommonStatisticsMethods.calRisingIntervals(pdvProbeTrail_Downward.get(nNumBallScales), nMaxRisingInterval));
        nvvProbeContactingPositions_Downward.add(segmenterD.getMultiscaleAnchors());
        cvvBallTrailSegmenters_Downward=segmenterD.getSegmenters();
        cvvBallTrailSegmenters_Downward.add(cvvBallTrailSegmenters_Downward.get(cvvBallTrailSegmenters_Downward.size()-1));
        pnvSegmentIndexes_Downward=segmenterD.getSegmentIndexes();
        //upward
        segmenterH=new SequenceSegmenter_ProgressiveScales(pdvH_Upward,pcvRisingNeighbors_Upward,SequenceSegmenter_ProgressiveScales.isolated,dvPSegmentation,null,SequenceSegmenter_ProgressiveScales.larger);    
        segmenterD=new SequenceSegmenter_ProgressiveScales(pdvD_Upward,pcvRisingNeighbors_Upward,SequenceSegmenter_ProgressiveScales.right,dvPSegmentation,dvPConsolidation,SequenceSegmenter_ProgressiveScales.larger); 
        pdvP_TrailDelta_Upward=segmenterD.getPValues();
        pdvP_TrailHeight_Upward=segmenterH.getPValues();
        
        segmenterD.combineSegmenters_AND(segmenterH.cvvSegmenters);
        refineMultipleScaleSegmenter(segmenterD, false);
        segmenterD.buildAnchoredSegments(pdvProbeTrail_Upward,pnvTrailRisingIntervals_Upward,nvvProbeContactingPositions_Upward);
        pdvProbeTrail_Upward.add(segmenterD.buildMultiscaleTrail_Downward(pdvProbeTrail_Upward));
        pnvTrailRisingIntervals_Upward.add(CommonStatisticsMethods.calRisingIntervals(pdvProbeTrail_Upward.get(nNumBallScales), nMaxRisingInterval));
        nvvProbeContactingPositions_Upward.add(segmenterD.getMultiscaleAnchors());
        cvvBallTrailSegmenters_Upward=segmenterD.getSegmenters();
        cvvBallTrailSegmenters_Upward.add(cvvBallTrailSegmenters_Upward.get(cvvBallTrailSegmenters_Upward.size()-1));
        pnvSegmentIndexes_Upward=segmenterD.getSegmentIndexes();
        
        return 1;
    }
    void refineMultipleScaleTrails(){   
        int num=pdvProbeTrail_Downward.size(),i,p,len,index;
        int[] pnRisingIntervals=pnvTrailRisingIntervals_Downward.get(num-1);
        int[] pnFineScale=pnvTrailRisingIntervals_Downward.get(nNumBallScales-1),pnCoarsScale=pnvTrailRisingIntervals_Downward.get(0);
        double[] pdDelta=CommonStatisticsMethods.getDeltaArray(pdvProbeTrail_Downward.get(num-1), pnRisingIntervals, nMaxRisingInterval);
        double[] pdDeltaFineScale=CommonStatisticsMethods.getDeltaArray(pdvProbeTrail_Downward.get(nNumBallScales-1),pnFineScale , nMaxRisingInterval);
        double[] pdDeltaCoarsScale=CommonStatisticsMethods.getDeltaArray(pdvProbeTrail_Downward.get(0),pnCoarsScale , nMaxRisingInterval);
        double[] pdAbs=CommonStatisticsMethods.getAbs(pdDelta);
        ArrayList<Integer> nvLx=new ArrayList(),nvLn=new ArrayList();
        CommonMethods.LocalExtrema(pdAbs, nvLn, nvLx);
        ArrayList<Double> dvDiffs=new ArrayList();
        double dt;
        len=nvLx.size();
        int[] pnClusterIndexes=new int[len], pnIndexes=new int[len];
        for(i=0;i<nvLx.size();i++){
            p=nvLx.get(i);
            dt=pdDelta[p];
            dvDiffs.add(dt);
            pnIndexes[i]=i;
            pnClusterIndexes[i]=0;
        }        
        
        utilities.QuickSort.quicksort(CommonStatisticsMethods.copyToDoubleArray(dvDiffs), pnIndexes);
        pnClusterIndexes[pnIndexes[nvLx.size()-1]]=1;
        
        OneDKMeans cKM=new OneDKMeans(" ", dvDiffs,2,pnClusterIndexes);
        OneDKMeans cKME;
        
        if(cKM.getClusterSizes()[1]==1&&cKM.m_nSize>2)
            cKME=CommonStatisticsMethods.OneDKMeans_LargestAttenuated(" ", dvDiffs, 2);
        else
            cKME=cKM;
        //new OneDKMeans_ExtremExcluded(title+"_E", dvDiffs,2,1);
        
        pnClusterIndexes=cKME.getClusterIndexes();
        
        for(i=0;i<len;i++){
            if(pnClusterIndexes[i]==0) continue;
            fixMultiscaleTrail(pdDeltaCoarsScale,pdDelta,pdDeltaFineScale,pnCoarsScale, pnRisingIntervals, pnFineScale,nvLx.get(i));
        }
        ArrayList<Integer> nvMultiscaleContactingPositions=new ArrayList();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbvSemiballContacting_Downward.get(num-1), new intRange(0,nDataSize-1), nvMultiscaleContactingPositions);
        nvvProbeContactingPositions_Downward.set(num-1, nvMultiscaleContactingPositions);
    }
    void refineMultipleScaleSegmenter(SequenceSegmenter_ProgressiveScales segD, boolean downward){   
        int num,i,p,len,index;
        int[] pnRisingIntervals;
        double[] pdDelta;
        boolean[] pbTopscaleAnchor;
        intRange[] pcRanges=new intRange[nDataSize];
        CommonStatisticsMethods.setElements(pcRanges, null);
        if(downward){
            num=pdvProbeTrail_Downward.size();
            pnRisingIntervals=pnvTrailRisingIntervals_Downward.get(num-1);
            pdDelta=CommonStatisticsMethods.getDeltaArray(pdvProbeTrail_Downward.get(num-1), pnRisingIntervals, nMaxRisingInterval);
            pbTopscaleAnchor=pbTopscaleAnchor_Downward;
        } else {
            num=pdvProbeTrail_Upward.size();
            pnRisingIntervals=pnvTrailRisingIntervals_Upward.get(num-1);            
            pdDelta=CommonStatisticsMethods.getDeltaArray(pdvProbeTrail_Upward.get(num-1), pnRisingIntervals, nMaxRisingInterval);
            pbTopscaleAnchor=pbTopscaleAnchor_Upward;
        }
        
        double[] pdAbs=CommonStatisticsMethods.getAbs(pdDelta);
        ArrayList<Integer> nvLx=new ArrayList(),nvLn=new ArrayList(),nvLe;
        CommonMethods.LocalExtrema(pdAbs, nvLn, nvLx);

        nvLe=nvLx;
        
        ArrayList<Double> dvDiffs=new ArrayList();
        double dt;
        len=nvLe.size();
        int[] pnClusterIndexes=new int[len], pnIndexes=new int[len];
        for(i=0;i<len;i++){
            p=nvLe.get(i);
            dt=pdAbs[p];
            if(dt<0.0000001*Math.abs(pdY[0])) continue;
            dvDiffs.add(dt);
            pnIndexes[i]=i;
            pnClusterIndexes[i]=0;
        }        
        
        utilities.QuickSort.quicksort(CommonStatisticsMethods.copyToDoubleArray(dvDiffs), pnIndexes);
        pnClusterIndexes[pnIndexes[nvLe.size()-1]]=1;
        
        OneDKMeans cKM=new OneDKMeans(" ", dvDiffs,2,pnClusterIndexes);
        OneDKMeans cKME;
        
        if(cKM.getClusterSizes()[1]==1&&cKM.m_nSize>2)
            cKME=CommonStatisticsMethods.OneDKMeans_LargestAttenuated(" ", dvDiffs, 2);
        else
            cKME=cKM;
        //new OneDKMeans_ExtremExcluded(title+"_E", dvDiffs,2,1);
        
        double ratio=0.8,dMinDelta=cKME.pcClusterMeanSems[0].max*ratio,delta;
        if(downward) dMultiscaleSegCutoff=dMinDelta;
        ArrayList<ArrayList<intRange>> cvvSegmenters=segD.getSegmenters();
        num=cvvSegmenters.size();
        ArrayList<intRange> segmenters=cvvSegmenters.get(num-1);
        intRange ir;
        int l,r,r0,nScale;
        
        for(nScale=0;nScale<num;nScale++){
/*            if(downward){
                pnRisingIntervals=pnvTrailRisingIntervals_Downward.get(num-1);
                pdDelta=CommonStatisticsMethods.getDeltaArray(pdvProbeTrail_Downward.get(num-1), pnRisingIntervals, nMaxRisingInterval);
            }else{
                pnRisingIntervals=pnvTrailRisingIntervals_Upward.get(num-1);
                pdDelta=CommonStatisticsMethods.getDeltaArray(pdvProbeTrail_Upward.get(num-1), pnRisingIntervals, nMaxRisingInterval);
            }*/   
            
            segmenters=cvvSegmenters.get(nScale);
            
            for(i=segmenters.size()-1;i>=0;i--){
                ir=segmenters.get(i);
                l=ir.getMin();
                r0=ir.getMax();
                r=r0-1;
                while(r>=l){
                    delta=pdDelta[r];
                    if(Math.abs(delta)>dMinDelta) {
                        r=Math.min(r0,r+pnRisingIntervals[r]);
                        break;
                    }else{
                        r0=r;
                    }
                    r--;
                }
                if(r<l) 
                    segmenters.remove(i);
                else
                    ir.setMax(r);
            }
        }
        
        segmenters=cvvSegmenters.get(nNumBallScales-1);
        len=segmenters.size();        
        for(i=0;i<len;i++){
            ir=segmenters.get(i);
            pcRanges[ir.getMin()]=ir;
        }
        
        int start=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor, true, 0, 1); 
        int end=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor, true, start+1, 1);
        int ix,is,position;
        ArrayList<intRange> irs=new ArrayList();
        ArrayList<Integer> positions0=new ArrayList(),positions=new ArrayList(),positionst=new ArrayList(),indexes=new ArrayList();
        ArrayList<Double> dvt=new ArrayList();
        double ratioCutoff=2;
        while(end>0){
            CommonStatisticsMethods.getRangesWithinRange(pcRanges, irs, positions0,start, end);
            len=irs.size();
            if(len<2) {
                start=end;
                end=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor, true, start+1, 1);
                continue;
            }
            dvt.clear();
            indexes.clear();
            positions.clear();
            for(i=0;i<len;i++){
                ir=irs.get(i);
                indexes.add(i);
                dvt.add(Math.abs(pdDelta[ir.getMin()]));                
            }
            CommonStatisticsMethods.PartialSort(dvt, indexes, 1, -1);
            ix=indexes.get(0);
            is=indexes.get(1);            
            if(dvt.get(0)/dvt.get(1)<ratioCutoff) {
                start=end;
                end=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor, true, start+1, 1);
                continue;
            }
            for(i=0;i<len;i++){
                if(i==ix) continue;
                position=positions0.get(i);
                pcRanges[position]=null;
            }
            start=end;
            end=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor, true, start+1, 1);
        }
        segmenters.clear();
        CommonStatisticsMethods.getRangesWithinRange(pcRanges, segmenters, positionst, 0, nDataSize-1);
        segD.buildSegments();
    }
    int fixMultiscaleTrail(double[]pdDeltaCoarsScale,double[] pdDeltaMultiScale,double[] pdDeltaFineScale, int[] pnCoarsScale, int[] pnRisingIntervals,int[] pnFineScale, int p0){
        int pl=p0+CommonStatisticsMethods.getRisingInterval(pnFineScale, p0, -1),nSil=getScaleIndex(p0),pr=p0+CommonStatisticsMethods.getRisingInterval(pnFineScale, p0, 1),nSir=getScaleIndex(pr),
                left=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor_Downward, true, p0, -1),
                right=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor_Downward, true, pr, 1);
        double diffRatioCutoff=2.,similarRatioCutoff=1.3,ratio,delta0,delta1,sign=pdDeltaMultiScale[p0],delta;
        if(left<0) left=0;
        if(right<0) right=nDataSize-1;
        int nMultiscaleIndex=pdvProbeTrail_Downward.size()-1,nFinescaleIndex=nNumBallScales-1;
        int iF=nDataSize-nMaxRisingInterval-1,p;
        boolean bBreak;
        if(right>iF) right=iF;
        if(sign>0){
            pr=p0+CommonStatisticsMethods.getRisingInterval(pnFineScale, p0, 1);
            delta0=pdDeltaFineScale[p0];
            nSir=getScaleIndex(pr);
            if(nSir!=nNumBallScales-1){
                
                bBreak=false;
                for(p=pr;p<=right;p++){
                    delta=pdDeltaFineScale[p];
                    if(Math.abs(delta0/delta)<diffRatioCutoff){
                        bBreak=true;
                        break;
                    }
                }
                if(!bBreak)replaceMultiscaleSection(nMultiscaleIndex,nFinescaleIndex,p0,right);
                
            }else{
/*                bBreak=false;//12d04
                for(p=pr;p<=right;p++){
                    delta=pdDeltaFineScale[p];
                    if(delta>0) continue;
                    if(Math.abs(delta0/delta)<similarRatioCutoff){
                        bBreak=true;
                        break;
                    }
                }
                if(bBreak)replaceMultiscaleSection(nMultiscaleIndex,0,p0,right);*/
            }            
        }
        
        if(sign<0){
            pr=p0+pnFineScale[p0];
            pr=p0+CommonStatisticsMethods.getRisingInterval(pnFineScale, p0, 1);
            pl=p0+CommonStatisticsMethods.getRisingInterval(pnFineScale, p0, -1);
            nSil=getScaleIndex(p0);
            delta0=pdDeltaFineScale[p0];
            if(nSil!=nNumBallScales-1){
                bBreak=false;
                for(p=left;p<=pl;p++){
                    delta=pdDeltaFineScale[p];
                    if(Math.abs(delta0/delta)<diffRatioCutoff){
                        bBreak=true;
                        break;
                    }
                }
                if(!bBreak)replaceMultiscaleSection(nMultiscaleIndex,nFinescaleIndex,left,p0);
                
            }else{
/*                bBreak=false;
                for(p=left;p<=pl;p++){
                    delta=pdDeltaFineScale[p];
                    if(delta<0) continue;
                    if(Math.abs(delta0/delta)<similarRatioCutoff){
                        bBreak=true;
                        break;
                    }
                }
                if(bBreak)replaceMultiscaleSection(nMultiscaleIndex,0,left,p0);*/
            }            
        }
        return 1;
    }
    int replaceMultiscaleSection(int nMultiscaleIndex, int nScaleIndex, int pI, int pF){
        int p;
        double[] pdTrail=pdvProbeTrail_Downward.get(nScaleIndex),pdMultiscaleTrail=pdvProbeTrail_Downward.get(nMultiscaleIndex);
        boolean[] pbSemiballContacting=pbvSemiballContacting_Downward.get(nScaleIndex),pbMultiscaleSemiballContacting=pbvSemiballContacting_Downward.get(nMultiscaleIndex);
        for(p=pI;p<=pF;p++){
            pdMultiscaleTrail[p]=pdTrail[p];
            pbMultiscaleSemiballContacting[p]=pbSemiballContacting[p];
        }
        return 1;
    }
    int getScaleIndex(int p){
        int num=pdvProbeTrail_Downward.size();
        double dt=pdvProbeTrail_Downward.get(num-1)[p];
        for(int i=0;i<nNumBallScales;i++){
            if(dt==pdvProbeTrail_Downward.get(i)[p]) return i;
        }
        return -1;//should not be happening
    }
    public void buildFiltedEnvlines(){
        if(pdvFiltedData_EdgePreserving==null) FiltData_EdgePreserving();
        double dRx=10;
        int nRanking=0;                          
        ProbingBall pb=new ProbingBall(pdX,pdvFiltedData_EdgePreserving.get(pdvFiltedData_EdgePreserving.size()-1),dRx,-1,nRanking);  
        pdFiltedEnv_Downward=pb.getProbeTrail(ProbingBall.Downward);        
        pdFiltedEnv_Upward=pb.getProbeTrail(ProbingBall.Upward);        
    }
    public void FiltData_EdgePreserving(){
        int iterations=1;
        pdvFiltedData_EdgePreserving=new ArrayList();
        double[] filtedY=CommonStatisticsMethods.copyArray(pdY);
        int nMaxMultiplicity=2;
        
        double[] pdFiltedY=CommonStatisticsMethods.copyArray(pdY);
        for(int i=0;i<iterations;i++){
            EdgePreservingFilter filter=new EdgePreservingFilter(pdX,pdFiltedY,nMaxMultiplicity);
            filtedY=filter.getTransitionPreservingFiltedData(Math.abs(nMaxMultiplicity),iterations);
            CommonStatisticsMethods.copyArray(filtedY, pdFiltedY);
            nMaxMultiplicity++;
            pdvFiltedData_EdgePreserving.add(filtedY);
        }
    }
    void refineDiffArrays(){
        
    }
    public void calStatisticsTests(){
        for(int position=0;position<nDataSize;position++){
            if(pnHighDiffClusterPosition[position]>0||pnLowDiffClusterPosition[position]>0) 
                pcTransitionTests[position]=new SignalTransitionStatisticsTests(position);
        }
    }
    void markTransitionEnvPoints(OneDKMeans cKM, ArrayList<Integer> nvPositions,boolean[] pbTransition){
        int i,K=cKM.K,h=K-1,position,positionp,len=cKM.m_nSize;
        int[] pnClusterIndexes=cKM.getClusterIndexes();
        int num=0;
        for(i=0;i<len;i++){
            if(pnClusterIndexes[i]!=h) continue;
            position=nvPositions.get(num);
            positionp=CommonStatisticsMethods.getMaxAbsPosition(pdFiltedEnvDelta_Downward, position);
            pbTransition[positionp]=true;
            num++;
        }
    }
    void markHigherClusterPoints(){
        pbEnvTransitionPoints=CommonStatisticsMethods.getEmptyBooleanArray(pbEnvTransitionPoints, nDataSize);
        nvHighClusterPoints=pickTransitionalCandidates(pnHighDiffClusterPosition);
        nvDetectedTransitionPoints=pickConfirmedTransitoinPoints(nvHighClusterPoints);
        int i,len=nvDetectedTransitionPoints.size(),position;
        for(i=0;i<len;i++){
            position=nvDetectedTransitionPoints.get(i);
            pbEnvTransitionPoints[position]=true;
        }
        
        pbEnvTransitionPointsE=CommonStatisticsMethods.getEmptyBooleanArray(pbEnvTransitionPointsE, nDataSize);
        nvHighClusterPointsE=pickTransitionalCandidates(pnHighDiffClusterPositionE);
        nvDetectedTransitionPointsE=pickConfirmedTransitoinPoints(nvHighClusterPointsE);
        len=nvDetectedTransitionPointsE.size();
        for(i=0;i<len;i++){
            position=nvDetectedTransitionPointsE.get(i);
            pbEnvTransitionPointsE[position]=true;
        }
    }
    boolean isSmooth(int position){
//        return pnSmoothSegLengths[position]>14;
        for(int i=0;i<nNumSmoothCriteria;i++){
            if(m_ppnSmoothRegionLength[i][position]>m_pnMinSoomthRegionLength[i]) return true;
        }
        if(isSmoothProfile(position)) return true;
        return false;
    }
    ArrayList<Integer> pickConfirmedTransitoinPoints(ArrayList<Integer> nvInitTransitions){
        
        boolean[] pbConfirmingPositions=new boolean[nDataSize];
        
        CommonStatisticsMethods.setElements(pbConfirmingPositions, false);
        CommonStatisticsMethods.setElements(pbConfirmingPositions,nvInitTransitions, true);
        
        ArrayList<Integer> confirmedPts=new ArrayList();
        int i,len=nvInitTransitions.size();
        int[] pnConfirmed=new int[len];
        int itL,itR,itLL,itRR,pt1,pt2,sign;
        CommonStatisticsMethods.setElements(pnConfirmed, 0);
        int num=CommonStatisticsMethods.getNumOfZero(pnConfirmed),index,it0,it,it1,ll,lr,rl,rr,rl1,rr1;
        SignalTransitionStatisticsTests cTest,cTestt;
        intRange ir;
        int confirmingIndex,confirmingPosition;
        
        boolean[] pbBallContactingMaxima=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbBallContactingMaxima, false);
        CommonStatisticsMethods.setElements(pbBallContactingMaxima, nvvBallContactingExtrema_Downward.get(0),true);
        int position,left,right,confirmingIndex0,lt,rt,p,numPs,numNs,direction;
        double cutoff=2.,dt,dt1,dt2,ratio,ratioCutoff=1.7,y0,y;
        while(num>0){
            ir=getShortestTransition(nvInitTransitions,pnConfirmed);
            itL=ir.getMin();
            itR=ir.getMax();
            if(itL<0){
                ll=0;
                lr=nvInitTransitions.get(itR);
                rl=lr+nMaxRisingInterval;
                itRR=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnConfirmed,itR+1,1,-1);
                if(itRR<0)
                    rr=nDataSize-1;
                else
                    rr=nvInitTransitions.get(itRR);                
                
                if(ll>lr) ll=lr;
                if(rl>rr) rl=rr;
                cTest=new SignalTransitionStatisticsTests(lr,ll,lr,rl,rr);
                confirmingIndex=itR;
                confirmingPosition=lr;
//                pnConfirmed[itR]=1;
//                if(cTest.isInsignificantTransition()) pnConfirmed[itR]=-1;
//                if(isSmooth(lr)) pnConfirmed[itR]=-1;
            } else if(itR<0) {
                itLL=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnConfirmed,itL-1,-1,-1);
                if(itLL<0)
                    ll=0;
                else
                    ll=nvInitTransitions.get(itLL)+nMaxRisingInterval;
                lr=nvInitTransitions.get(itL);
                rl=lr+nMaxRisingInterval;
                rr=nDataSize-1;
                
                if(ll>lr) ll=lr;
                if(rl>rr) rl=rr;
                cTest=new SignalTransitionStatisticsTests(lr,ll,lr,rl,rr);
                confirmingIndex=itL;
                confirmingPosition=lr;
                
//                pnConfirmed[itL]=1;
//                if(cTest.isInsignificantTransition()) pnConfirmed[itL]=-1;
//                if(isSmooth(lr)) pnConfirmed[itL]=-1;                
            } else {
                itLL=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnConfirmed,itL-1,-1,-1);
                if(itLL<0)
                    ll=0;
                else
                    ll=nvInitTransitions.get(itLL)+nMaxRisingInterval;
                
                lr=nvInitTransitions.get(itL);
                rl=lr+nMaxRisingInterval;                
                rr=nvInitTransitions.get(itR);
                rl1=rr+nMaxRisingInterval;
                itRR=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnConfirmed,itR+1,1,-1);
                if(itRR<0)
                    rr1=nDataSize-1;
                else
                    rr1=nvInitTransitions.get(itRR);
                
                if(ll>lr) ll=lr;
                if(rl>rr) rl=rr;
                if(rl1>rr1) rl1=rr1;
                    
                 double m1=CommonStatisticsMethods.getMean(pdY, pbSelected, ll, lr, 1);
                 double m2=CommonStatisticsMethods.getMean(pdY, pbSelected, rl, rr, 1);
                 double m3=CommonStatisticsMethods.getMean(pdY, pbSelected, rl1, rr1, 1);
                 
                 double dL=Math.abs(m2-m1),dR=Math.abs(m2-m3);
                 if(pnConfirmed[itL]!=0) dL+=dR;
                 if(pnConfirmed[itR]!=0) dR+=dL;
                 
                 //at least one of pnConfirmed[itL] and pnConfirmed[itR] is zero
                 if(dL<dR){
                    cTest=new SignalTransitionStatisticsTests(lr,ll,lr,rl,rr);
                    confirmingIndex=itL;
                    confirmingPosition=lr;
                    
//                    pnConfirmed[itL]=1;
//                    if(cTest.isInsignificantTransition()) pnConfirmed[itL]=-1;
//                    if(isSmooth(lr)) pnConfirmed[itL]=-1;
                 } else {
                    cTest=new SignalTransitionStatisticsTests(rr,rl,rr,rl1,rr1);
                    confirmingIndex=itR;
                    confirmingPosition=rr;

//                    pnConfirmed[itR]=1;
//                    if(cTest.isInsignificantTransition()) pnConfirmed[itR]=-1;
//                    if(isSmooth(rr)) pnConfirmed[itR]=-1;
                 }
            } 
            if(pnConfirmed[confirmingIndex]!=0) confirmingIndex=CommonStatisticsMethods.getFirstPositionWithTheElement(pnConfirmed, 0, 1, 0);
            if(confirmingIndex<0) break;
            pnConfirmed[confirmingIndex]=1;
            if(confirmingPosition==0){
                num=num;
            }
            ratio=Math.abs(pdFiltedEnvDelta_Downward[confirmingPosition]/pdDeltaProbeTrail_Downward[confirmingPosition]);
//            if(ratio>ratioCutoff||ratio<1./ratioCutoff) pnConfirmed[confirmingIndex]=-1; //12d09, now that it is using the probe trail, this check point is needed.
            if(cTest.isInsignificantTransition()) pnConfirmed[confirmingIndex]=-1;
            if(isSmooth(confirmingPosition)) pnConfirmed[confirmingIndex]=-1;  
//            if(confirmingPosition==0) pnConfirmed[confirmingIndex]=-1;  //12n13
            if(pdYRWMeanDiffLR_Downward[confirmingPosition]<0&&confirmingPosition<5) pnConfirmed[confirmingIndex]=-1;  //12n13
            if(!pairedIntervalTransitions(confirmingPosition))pnConfirmed[confirmingIndex]=-1; 
            if(cTest.isSignificantTransition()){
                pbSignificantTransitions[confirmingPosition]=true;
                pnConfirmed[confirmingIndex]=1; 
            }
            
            cTestt=pcTransitionTests[confirmingPosition];
            if(cTest.ll<cTestt.ll||cTest.rr>cTestt.rr)
                pcTransitionTests[confirmingPosition]=cTest;
            
            
/*            if(pnConfirmed[confirmingIndex]==1){
                left=CommonStatisticsMethods.getFirstSelectedPosition(pbBallContactingMaxima,true, confirmingPosition, -1);
                if(left<0) left=0;
                right=CommonStatisticsMethods.getFirstSelectedPosition(pbBallContactingMaxima,true, confirmingPosition+CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals_Downward, confirmingPosition, 1), 1);
                if(right<0) right=nDataSize-1;
                if(right<m_pnRisingIntervals_Downward.length) 
                    right+=CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals_Downward, right, -1);
                else
                    right=m_pnRisingIntervals_Downward.length-1;
                dt=pdDeltaProbeTrail_Downward[confirmingPosition];
                for(position=left;position<=right;position++){
                    if(position==confirmingPosition) continue;
                    dt1=pdDeltaProbeTrail_Downward[position];
                    dt1=dt/dt1;
                    if(dt1>-cutoff&&dt1<0){
                        sign=1;
                        if(dt<0) sign=-1;                        
                        extrema=CommonStatisticsMethods.getExtremaPositions(pdFiltedEnvDelta_Downward, confirmingPosition, sign);
                        pt1=extrema.get(0);
                        dt1=pdFiltedEnvDelta_Downward[pt1];//the confirming position should not be a minimum, so extrema should have one element, 12n13
                        if(position==1){
                            position=position;
                        }
                        extrema=CommonStatisticsMethods.getExtremaPositions(pdFiltedEnvDelta_Downward, position, -sign);
                        pt2=extrema.get(0);
                        if(pt2<0) {
                            pt2=pt2;
                        }
                        dt2=pdFiltedEnvDelta_Downward[pt2];
                        dt1/=dt2;
                        if(dt1/dt2>-cutoff&&dt1<0){
                            pnConfirmed[confirmingIndex]=-1;
                            break;
                        }                       
                    }
                }
            }*/
            if(pnConfirmed[confirmingIndex]==-1){
                lt=CommonStatisticsMethods.getFirstSelectedPosition(pbConfirmingPositions, true, confirmingPosition-1, -1);
                if(lt<0) lt=0;
                left=Math.max(confirmingPosition+CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals_Downward, confirmingPosition, -1),lt+1);
                
                rt=CommonStatisticsMethods.getFirstSelectedPosition(pbConfirmingPositions, true, confirmingPosition+1, 1);
                if(rt<0) rt=nDataSize-1;
                right=Math.min(confirmingPosition+CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals_Downward, confirmingPosition, 1),rt-1);
                for(position=left;position<=right;position++){
                    if(position>=pdSigDiff.length) break;
                    pdSigDiff[position]=-0.001;//so that it can be recognized.
                }
            }else{
                pdSigDiff[confirmingPosition]=pdvSigdiffs.get(0)[confirmingPosition];//13126
            }
            num=CommonStatisticsMethods.getNumOfZero(pnConfirmed);                        
        }
        for(i=0;i<len;i++){
            if(pnConfirmed[i]==1) confirmedPts.add(nvInitTransitions.get(i));
        }
        if(confirmedPts.size()>0)
            if(confirmedPts.get(0) ==0&&pdDeltaY[0]<0) {
                confirmedPts.remove(0);
            }
        
        ArrayList<IntPair> cvShortSegments=new ArrayList();
        len=confirmedPts.size();
        if(len<2) return confirmedPts;
        
        //checking short opposite transitions
        int p0=confirmedPts.get(0),lenCutoff=8;;
        for(i=1;i<len;i++){
            p=confirmedPts.get(i);
            if(p-p0<lenCutoff) cvShortSegments.add(new IntPair(i-1,i));
            p0=p;
        }
        
        len=cvShortSegments.size();
        IntPair ip;
        ArrayList<Integer> nvInvalidTransitions=new ArrayList();
        for(i=0;i<len;i++){            
            ip=cvShortSegments.get(i);
            lr=confirmedPts.get(ip.left);
            rl=confirmedPts.get(ip.right);
            if(pdSigDiff[lr]*pdSigDiff[rl]>0) continue;
            ratio=Math.abs(pdSigDiff[lr]/pdSigDiff[rl]);
            if(ratio<1) ratio=1./ratio;
//            if(ratio<1.5) {
            if(true) {
                if(!CommonMethods.containsContent(nvInvalidTransitions, ip.left))nvInvalidTransitions.add(ip.left);
                if(!CommonMethods.containsContent(nvInvalidTransitions, ip.right))nvInvalidTransitions.add(ip.right);
                continue;
            }
            rl+=nMaxRisingInterval;
            if(ip.left==0)
                ll=0;
            else
                ll=confirmedPts.get(ip.left-1);
            if(ip.right==confirmedPts.size()-1)
                rr=pdY.length-1;
            else
                rr=confirmedPts.get(ip.right+1);
            cTest=new SignalTransitionStatisticsTests(lr,ll,lr,rl,rr);
            if(cTest.isInsignificantTransition()){
                if(!CommonMethods.containsContent(nvInvalidTransitions, ip.left)) nvInvalidTransitions.add(ip.left);
                if(!CommonMethods.containsContent(nvInvalidTransitions, ip.right)) nvInvalidTransitions.add(ip.right);
            }
        }
        len=nvInvalidTransitions.size();
        int j;
        for(i=len-1;i>=0;i--){
            index=nvInvalidTransitions.get(i);
            p=confirmedPts.get(index);
            confirmedPts.remove(index);
            for(j=p;j>=p+nMaxRisingInterval;j++){
                if(j>=pdSigDiff.length) break;
                pdSigDiff[j]=-0.001;
            }
        }
        return confirmedPts;
    }
    double getOverlapedDelta(int position){
        double delta=pdDeltaY[position];
        int p,p2,sign=1;
        if(delta<0) sign=-1;
        ArrayList<Integer> positions=CommonStatisticsMethods.getExtremaPositions(pdDeltaY, position, sign);
        if(positions.isEmpty()) return 0;
        p=positions.get(0);
        if(p<0) return 0;
        if(p>=pdDeltaY2.length) p=pdDeltaY2.length-1;
        positions=CommonStatisticsMethods.getExtremaPositions(pdDeltaY2, p, sign);
        if(positions.isEmpty()) return 0;
        p2=positions.get(0);
        if(p2<0) return 0;
        if(p2>=pdDeltaY2.length) p2=pdDeltaY2.length-1;
        return pdDeltaY2[p2]-pdDeltaY[p];
    }
    boolean pairedIntervalTransitions(int confirmingPosition){
        int[] pnIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        int i,len,p;
        double[] pdAbsDeltaY=CommonStatisticsMethods.getAbs(pdDeltaY);
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdDeltaY, nvLn, nvLx);
        boolean[] pbSelection=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbSelection, false);
        
        len=nvLx.size();
        for(i=0;i<len;i++){
            p=nvLx.get(i);
            if(pdDeltaY[p]>0) pbSelection[p]=true;
        }
        
        len=nvLn.size();
        for(i=0;i<len;i++){
            p=nvLn.get(i);
            if(pdDeltaY[p]<0) pbSelection[p]=true;
        }
        
        int numPs,numNs,sign,direction,pt1;
        double dt,dt1,dt2,dt20,cutoff;
        ArrayList<Double> dvPs=new ArrayList(),dvNs=new ArrayList(),dvPSs=new ArrayList(),dvNSs=new ArrayList();
       
        numPs=0;
        numNs=0;
        sign=1;
        direction=1;
        dt=pdDeltaY[confirmingPosition];
        if(dt<0) {
            direction=-1;
        }
        pt1=CommonStatisticsMethods.getFirstSelectedPosition(pbSemiballContactingLx_Downward, true, confirmingPosition+direction, direction);
        pt1=pt1+CommonStatisticsMethods.getRisingInterval(pnIntervals, pt1, -1);
        ArrayList<Integer> positions;
//        ArrayList<Integer> positions=CommonStatisticsMethods.getExtremaPositions(pdAbsDeltaY, pt1, 1);
//        pt1=positions.get(0);
        positions=CommonStatisticsMethods.getExtremaPositions(pdAbsDeltaY, confirmingPosition, 1);
        p=positions.get(0);
        if(p<0){
             if(direction>0) 
                 p=0;
             else
                 p=nDataSize-1;
        }
        dt=pdDeltaY[p];
        double ratio1=1.6;
        dt2=getOverlapedDelta(p);
        if(dt/dt2<ratio1) {
            numPs++;
            dvPs.add(dt2);
        }else{
            dvPSs.add(dt2);
        }
        dt20=dt2;
        while (true){
            p=CommonStatisticsMethods.getFirstSelectedPosition(pbSelection, true, p+direction, direction);
            if(p<0) break;
            if(direction*(p-pt1)>0) break;
            dt1=pdDeltaY[p];
            dt2=getOverlapedDelta(p);
            if(dt2/dt1>1) continue;
            if(Math.abs(dt/dt1)<ratio1){
                if(dt*dt1>0){
                    numPs++;
                    dvPs.add(Math.abs(dt1));
                }else{
                    numNs++;
                    dvNs.add(Math.abs(dt1));
                }
            }else{
                if(dt*dt1>0){
                    dvPSs.add(Math.abs(dt1));
                }else{
                    dvNSs.add(Math.abs(dt1));
                }
            }
            if(Math.abs(dt/dt2)<ratio1){
                if(dt*dt2>0){
                    numPs++;
                    dvPs.add(Math.abs(dt2));
                }else{
                    numNs++;
                    dvNs.add(Math.abs(dt2));
                }
            }else{
                if(dt*dt2>0){
                    dvPSs.add(Math.abs(dt2));
                }else{
                    dvNSs.add(Math.abs(dt2));
                }
            }
        }            
        if(numPs>=numNs) return true;
        int numD=Math.abs(numPs-numNs);
        
//        if(Math.abs(numPs-numNs)>1) return false;
        ArrayList<Double> dv,dvS;
        if(numPs>numNs){
            dv=dvPs;
            dvS=dvNSs;
        }else{
            dv=dvNs;
            dvS=dvPSs;
        }
        ArrayList<Integer> nv=new ArrayList(),nvS=new ArrayList();
        len=dv.size();
        if(len==0) return false;
        for(i=0;i<len;i++){
            nv.add(i);
        }
        len=dvS.size();
        if(len==0) return false;
        for(i=0;i<len;i++){
            nvS.add(i);
        }
        
        if(numD>Math.min(dvS.size(),dv.size())) return false;
        
        CommonStatisticsMethods.PartialSort(dvS, nvS, numD-1, -1);
        CommonStatisticsMethods.PartialSort(dv, nv, numD-1, 1);
        
        boolean pass=true;
        for(i=0;i<numD;i++){
            if(dvS.get(i)/dv.get(numD-1-i)<0.6) pass=false;
        }
        if(pass) return true;
//        if(dvS.get(0)/dv.get(0)>0.6) return true;
        if((dt20+dt-dv.get(0))/dv.get(0)>0.6) return true;
        return false;
    }
    boolean pairedIntervalTransitions0(int confirmingPosition){
        int[] pnIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        double[] pdAbsDeltaY=CommonStatisticsMethods.getAbs(pdDeltaY);
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdAbsDeltaY, nvLn, nvLx);
        boolean[] pbSelection=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbSelection, false);
        CommonStatisticsMethods.setElements(pbSelection, nvLx, true);
        
        int numPs,numNs,sign,direction,p,pt1;
        double dt,dt1,y0,y,cutoff;
        ArrayList<Double> dvPs=new ArrayList(),dvNs=new ArrayList(),dvPSs=new ArrayList(),dvNSs=new ArrayList();
       
        numPs=0;
        numNs=0;
        sign=1;
        direction=1;
        dt=pdDeltaY[confirmingPosition];
        if(dt<0) {
                direction=-1;
        }
        pt1=CommonStatisticsMethods.getFirstSelectedPosition(pbSemiballContactingLx_Downward, true, confirmingPosition+direction, direction);
        p=CommonStatisticsMethods.getFirstExtremumIndex(pdY, confirmingPosition, 1, -direction);
        if(p<0){
             if(direction>0) 
                 p=0;
             else
                 p=nDataSize-1;
        }
        y0=pdY[p];
            
        p=CommonStatisticsMethods.getFirstExtremumIndex(pdY, p+direction, -1, direction);
        if(p<0){
            if(direction>0) 
                p=0;
            else
                p=nDataSize-1;
        }
        y=pdY[p];
        dt=y0-y;
        y0=y;
            
        while (true){
            p=CommonStatisticsMethods.getFirstExtremumIndex(pdY, p+direction, sign, direction); 
            if(p<0) break;
            if(direction*(p-pt1)>0) break;
            y=pdY[p];
            dt1=y0-y;
            if(Math.abs(dt/dt1)<1.5){
                if(dt*dt1>0){
                    numPs++;
                    dvPs.add(Math.abs(dt1));
                }else{
                    numNs++;
                    dvNs.add(Math.abs(dt1));
                }
            }else{
                if(dt*dt1>0){
                    dvPSs.add(Math.abs(dt1));
                }else{
                    dvNSs.add(Math.abs(dt1));
                }
            }
            y0=y;
            sign*=-1;
        }            
        if(numPs==numNs) return true;
        if(Math.abs(numPs-numNs)>1) return false;
        ArrayList<Double> dv,dvS;
        if(numPs>numNs){
            dv=dvPs;
            dvS=dvNSs;
        }else{
            dv=dvNs;
            dvS=dvPs;
        }
        ArrayList<Integer> nv=new ArrayList(),nvS=new ArrayList();
        int i,len=dv.size();
        if(len==0) return false;
        for(i=0;i<len;i++){
            nv.add(i);
        }
        len=dvS.size();
        if(len==0) return false;
        for(i=0;i<len;i++){
            nvS.add(i);
        }
        CommonStatisticsMethods.PartialSort(dvS, nvS, 0, -1);
        CommonStatisticsMethods.PartialSort(dv, nv, 0, 1);
        if(dvS.get(0)/dv.get(0)>0.6) return true;
        return false;
    }
    boolean toConfirm(int itL,int itR, int[]pnConfirmed){
        int iF=pnConfirmed.length-1;
        if(itL>=0&&itL<=iF)
            if(pnConfirmed[itL]==0) return true;
        if(itR>=0&&itR<=iF)
            if(pnConfirmed[itR]==0) return true;
        return false;
    }
    intRange getShortestTransition(ArrayList<Integer> nvInitPts,int[] pnConfirmed){
        int lt,ln,it0,it,it1,left,right;
        intRange irn=new intRange();
        ln=Integer.MAX_VALUE;
        it=CommonStatisticsMethods.getFirstPositionWithTheElement(pnConfirmed,0,1,0);
        boolean lConfirmed,rConfirmed;
        
        while(it>=0){
            it0=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnConfirmed, it-1, -1, -1);
            if(it0<0)
                left=0;
            else
                left=nvInitPts.get(it0)+nMaxRisingInterval;
            
            right=nvInitPts.get(it);
            
            lt=right-left+1;
            if(lt<ln){
                if(toConfirm(it0,it,pnConfirmed)){
                    ln=lt;
                    irn.setRange(it0, it);
                }
            }
            
            left=right+nMaxRisingInterval;
            it1=CommonStatisticsMethods.getFirstPositionWithDifferentElement(pnConfirmed, it+1, 1, -1);
            
            if(it1<0) 
                right=nDataSize-1;
            else
                right=nvInitPts.get(it1);
            
            lt=right-left+1;
            if(lt<ln){
                if(toConfirm(it,it1,pnConfirmed)){
                    ln=lt;
                    irn.setRange(it, it1);
                }
            }
            it=CommonStatisticsMethods.getFirstPositionWithTheElement(pnConfirmed,it+1,1,0);
        }
        return irn;
    }
    ArrayList<intRange> getEventRanges(ArrayList<Integer> nvTransitionPoints, int pI, int pF){//not verified yet, 12829
        intRange ir;
        ArrayList<intRange> cvRanges=new ArrayList();
        int left, i, right;
        left=pI;                
        for(i=0;i<nvTransitionPoints.size();i++){
            right=nvTransitionPoints.get(i);
            if(right<pI) continue;
            if(right>pF) break;
            if(left>right) left=right;
            cvRanges.add(new intRange(left,right));
            left=right+nMaxRisingInterval;
        }
        if(left>pF) left=pF;
        cvRanges.add(new intRange(left,pF));
        return cvRanges;
    }
    ArrayList<Integer> pickTransitionalCandidates(int[] pnHighDiffClusterPosition){
        ArrayList<Integer> nvInitialPts=new ArrayList();
        int i,flag;
        for(i=0;i<nDataSize;i++){
            if(i==59) {
                i=i;
            }
            flag=pnHighDiffClusterPosition[i];
            if(CommonMethods.getDigit(flag,3)==1) flag=flag%1000;//to temporally bypass the first block.
            if(nWsDiff<15) flag=CommonMethods.setDigit(flag, 1, 1);//to bypass the midian criteria when the window size is small
//            if(flag!=111) continue;
            if(CommonMethods.getDigit(flag, 0)!=1) continue;
            nvInitialPts.add(i);
        }
        return nvInitialPts;
    }
    public boolean[] getSelection(){
        return pbSelected;
    }
//    public PolynomialLineFittingSegmentNode(double[] pdX, double[] pdY, double[] pdTiltingSig, double[] pdSidenessSig, double[] pdPWDevSig, double[] pdSD, boolean[] pbSelected, boolean[] pbValidDev, int[] pnLnPositions, int[] pnLxPositions, int start, int end, int order, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ, double dPTilting, double dPSideness, double dPPWDev, double dOutliarRatio){
//    public LineSegmentNode(double[] pdX, double[] pdY, boolean[] pbSelected, int indexI, int indexF, int minLen, int maxLen, int nOrder){
    public ArrayList<LineSegmentNode> buildSegments(ArrayList<Integer> points){
        PolynomialLineFittingSegmentNode segL,segR;
        ArrayList<LineSegmentNode> segs=new ArrayList();
        
        LineSegmentNode seg;
        int left=0,right,i,len=points.size();
        for(i=0;i<len;i++){
            right=points.get(i);
            if(left>right) left=right;
            segL=getFirstSegment(m_pcStartingSegments,left,1);
            if(segL!=null){
                if(segL.nEnd>right) segL=null;
            }
            segR=getFirstSegment(m_pcEndingSegments,right,-1);
            if(segR!=null){
                if(segR.nStart<left) segR=null;
            }
            if(segL==null||segR==null){
                seg=new LineSegmentNode(pdX,pdY,pbSelected,left,right,4,30,1);
                if(segL!=null) seg.segRegressionNodeL(segL.cPr, segL.nStart, segL.nEnd);
                if(segR!=null) seg.segRegressionNodeL(segR.cPr, segR.nStart, segR.nEnd);
            }else
                seg=new LineSegmentNode(pdX,pdY,left,right,segL,segR);
            segs.add(seg);
            left=right+nMaxRisingInterval;
//            left=right+4;//13130
        }
        right=pdX.length-1;
        segL=getFirstSegment(m_pcStartingSegments,left,1);
        if(segL!=null){
            if(segL.nEnd>right) segL=null;
        }
        segR=getFirstSegment(m_pcEndingSegments,right,-1);
        if(segR!=null){
            if(segR.nStart<left) segR=null;
        }
        if(segL==null||segR==null){
            seg=new LineSegmentNode(pdX,pdY,pbSelected,left,right,4,30,1);
            if(segL!=null) seg.segRegressionNodeL(segL.cPr, segL.nStart, segL.nEnd);
            if(segR!=null) seg.segRegressionNodeL(segR.cPr, segR.nStart, segR.nEnd);
        }else
            seg=new LineSegmentNode(pdX,pdY,left,right,segL,segR);
        segs.add(seg);
        return segs;
    }
    public PolynomialLineFittingSegmentNode getFirstSegment(PolynomialLineFittingSegmentNode[] segs, int position0, int step){
        int position=position0,iF=segs.length-1;
        if(position<0||position>iF) 
            return null;
        PolynomialLineFittingSegmentNode seg=segs[position];
        while(seg==null){
            position+=step;
            if(position<0||position>iF) 
                return null;
            seg=segs[position];
        }
        return seg;
    }
    public ArrayList<LineSegmentNode> getSignalLevelSegments(){
        return cvSignalLevelSegments;
    }
    public boolean validClusteringExtention(){
        if(nvDetectedTransitionPoints.isEmpty()) return true;
        if(cKMSigDiffE.getGapIndexes().get(0)>cKMSigDiff.getGapIndexes().get(0)) return true;
        for(int i=0;i<nvDetectedTransitionPointsE.size();i++){
            if(pbSignificantTransitions[nvDetectedTransitionPointsE.get(i)]) return true;
        }
//        if(cKMDeltaMeanE.getGapIndexes().get(0)<0.7) return false;
//        if(!CommonStatisticsMethods.equalContents(getDetectedTransitionPointsE(), getHighClusterPointsE())) return false;//13208
//        if(nvMeanHighDiffClusterPositionsE.size()!=getDetectedTransitionPointsE().size()||nvRegressionHighDiffClusterPositionsE.size()!=getDetectedTransitionPointsE().size()) return false; //13208
        return true;
    }
    public ArrayList<LineSegmentNode> getSignalLevelSegmentsE(){
        return cvSignalLevelSegmentsE;
    }
    public PolynomialLineFitter getSignalFitter(){
        return m_cSignalFitter;
    }
    public OneDKMeans getKMeans_MeanDiff(){
        return cKMDeltaMean;
    }
    public OneDKMeans getKMeans_MeanDiffE(){
        return cKMDeltaMeanE;
    }
    public OneDKMeans getKMeans_RegressionDiff(){
        return cKMDeltaRegression;
    }
    public OneDKMeans getKMeans_RegressionDiffE(){
        return cKMDeltaRegressionE;
    }
    public static double[] calSD(double[] pdX, double[] pdY, boolean[] pbSelected, int nMaxRisingInterval, int nWsSD){
        int[] pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdY,nMaxRisingInterval);
        return CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdX,pdY, pnRisingIntervals,pbSelected, nWsSD);        
    }
    public static ArrayList<String> getFeatureLineNames(){
        ArrayList<String> names=new ArrayList();
        names.add("Default");
        names.add("SemiBallTrail Downward");
        names.add("FiltedEnv Downward");
        names.add("SemiBall Contacking Downward");
        names.add("Ball Contacking Downward");
        names.add("SemiBalltrailDownward Delta");
        
        names.add("SemiBallTrail Upward");
        names.add("FiltedEnv Upward");
        names.add("SemiBall Contacking Upward");
        names.add("Ball Contacking Upward");
        names.add("SemiBalltrailUpward Delta");
        
        names.add("Envelope thickness");
        names.add("Signal Diff");
        
        names.add("SemiBall Contacking Point Dev");
        names.add("Ball Contacking Point Dev");
        names.add("Ball Contacking Point Dev PValues");
        names.add("SemiBall Contacking Point SD");
        names.add("P Values-Trail Delta");
        names.add("P Values-Trail Height");
        return names;
    }
    
    public int buildFeatureLines(ArrayList<Integer> dvRx){
        buildBallTrails();
        return 1;
    }

    public ArrayList<double[]> getFeatureLine(String name, String sWs, boolean showSegments){
        ArrayList<double[]> line=new ArrayList();
        int index;
        double[] pdXT,pdYT;
        if(name.contentEquals("Default")){
            line.add(pdX);
            line.add(pdProbeTrail_Downward);
        }
        if(name.contentEquals("SemiBallTrail Downward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeTrail_Downward.size()) return line;
            line.add(pdX);
            line.add(pdvProbeTrail_Downward.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeTrail_Downward.get(index), cvvBallTrailSegmenters_Downward.get(index));
            int i;
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("SemiBallTrail Upward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeTrail_Upward.size()) return line;
            line.add(pdX);
            line.add(pdvProbeTrail_Upward.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeTrail_Upward.get(index), cvvBallTrailSegmenters_Upward.get(index));
            int i;
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("FiltedEnv Downward")){
            line.add(pdX);
            line.add(pdFiltedEnv_Downward);
        }
        if(name.contentEquals("FiltedEnv Upward")){
            line.add(pdX);
            line.add(pdFiltedEnv_Upward);
        }
        if(name.contentEquals("SemiBall Contacking Downward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvProbeContactingPositions_Downward.size()) return line;
            ArrayList<Integer> positions=nvvProbeContactingPositions_Downward.get(index);
            int i,len=positions.size(),position;
            pdXT=new double[len];
            pdYT=new double[len];
            for(i=0;i<len;i++){
                position=positions.get(i);
                pdXT[i]=pdX[position];
                pdYT[i]=pdY[position];
            }
            line.add(pdXT);
            line.add(pdYT);
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdXT, pdYT, pdX,cvvBallTrailSegmenters_Downward.get(index));
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        
        if(name.contentEquals("SemiBall Contacking Upward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvProbeContactingPositions_Upward.size()) return line;
            ArrayList<Integer> positions=nvvProbeContactingPositions_Upward.get(index);
            int i,len=positions.size(),position;
            pdXT=new double[len];
            pdYT=new double[len];
            for(i=0;i<len;i++){
                position=positions.get(i);
                pdXT[i]=pdX[position];
                pdYT[i]=pdY[position];
            }
            line.add(pdXT);
            line.add(pdYT);
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdXT, pdYT, pdX,cvvBallTrailSegmenters_Upward.get(index));
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        
        if(name.contentEquals("Ball Contacking Downward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvBallContactingExtrema_Downward.size()) return line;
            ArrayList<Integer> positions=nvvBallContactingExtrema_Downward.get(index);
            int i,len=positions.size(),position;
            pdXT=new double[len];
            pdYT=new double[len];
            for(i=0;i<len;i++){
                position=positions.get(i);
                pdXT[i]=pdX[position];
                pdYT[i]=pdY[position];
            }
            line.add(pdXT);
            line.add(pdYT);
            if(!showSegments) return line;
        }
        if(name.contentEquals("Signal Diff")){
            line.add(pdX);
            line.add(pdSigDiff);
            return line;
        }
        if(name.contentEquals("Ball Contacking Upward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvBallContactingExtrema_Upward.size()) return line;
            ArrayList<Integer> positions=nvvBallContactingExtrema_Upward.get(index);
            int i,len=positions.size(),position;
            pdXT=new double[len];
            pdYT=new double[len];
            for(i=0;i<len;i++){
                position=positions.get(i);
                pdXT[i]=pdX[position];
                pdYT[i]=pdY[position];
            }
            line.add(pdXT);
            line.add(pdYT);
            if(!showSegments) return line;
        }
        
        if(name.contentEquals("Envelope thickness")){
            double[] pdY1,pdY2;
            pdXT=this.pdX;
            int i1,i2,j,num=pdvProbeTrail_Downward.size();
            for(i1=0;i1<2;i1++){
                if(i1==0)
                    pdY1=pdvProbeTrail_Downward.get(num-1);
                else
                    pdY1=pdvProbeTrail_Downward.get(0);
                
                for(i2=0;i2<2;i2++){
                    if(i1+i2>0) continue;
                    line.add(pdXT);
                    if(i2==0)
                        pdY2=pdvProbeTrail_Upward.get(num-1);
                    else
                        pdY2=pdvProbeTrail_Upward.get(0);
                    
                    pdYT=new double[nDataSize];
                    
                    for(j=0;j<nDataSize;j++){
                        pdYT[j]=pdY1[j]-pdY2[j];
                    }
                    line.add(pdYT);
                }
            }
            return line;
        }
        if(name.contentEquals("SemiBall Contacking Point Dev")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeContactingPointsDevY_Downward.size()) return line;
            line.add(pdX);
            line.add(pdvProbeContactingPointsDevY_Downward.get(index));
            if(!showSegments) return line;
            int i;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeContactingPointsDevY_Downward.get(index), cvvBallTrailSegmenters_Downward.get(index));
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        
        if(name.contentEquals("Ball Contacking Point Dev")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvBallContactingExtrema_Downward.size()) return line;
            line=CommonStatisticsMethods.getDevArray(pdX, pdY, nvvBallContactingExtrema_Downward.get(index));
        }
        
        if(name.contentEquals("Ball Contacking Point Dev PValues")){
            line.add(pdX);
            line.add(pdBallContactingPointsDevPValues_Downward);
        }
        
        if(name.contentEquals("SemiBall Contacking Point SD")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeContactingPointsSDY_Downward.size()) return line;
            line.add(pdX);
            line.add(pdvProbeContactingPointsSDY_Downward.get(index));
        }   
        if(name.contentEquals("SemiBalltrailDownward Delta")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeTrailDelta_Downward.size()) return line;
            line.add(pdX);
            line.add(pdvProbeTrailDelta_Downward.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeTrailDelta_Downward.get(index), pdX,cvvBallTrailSegmenters_Downward.get(index));
            for(int i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("SemiBalltrailUpward Delta")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeTrailDelta_Upward.size()) return line;
            line.add(pdX);
            line.add(pdvProbeTrailDelta_Upward.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeTrailDelta_Upward.get(index), pdX,cvvBallTrailSegmenters_Upward.get(index));
            for(int i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("P Values-Trail Delta")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvP_TrailDelta_Downward.size()) return line;
            line.add(pdX);
            line.add(pdvP_TrailDelta_Downward.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvP_TrailDelta_Downward.get(index), pdX,cvvBallTrailSegmenters_Downward.get(index));
            for(int i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("P Values-Trail Height")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvP_TrailHeight_Downward.size()) return line;
            line.add(pdX);
            line.add(pdvP_TrailHeight_Downward.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvP_TrailHeight_Downward.get(index), pdX, cvvBallTrailSegmenters_Downward.get(index));
            for(int i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        return line;
    }
    public int getWindowSizes(String name, ArrayList<String> svWs){
        ArrayList<double[]> line=new ArrayList();
        svWs.clear();
        for(int i=0;i<svBallRollingWs.size();i++){
            svWs.add(svBallRollingWs.get(i));
        }
        return 1;
    }
    public MeanSem1[] getMeanDiffMeanSems(){
        MeanSem1[] MSs;
        if(validClusteringExtention())
            MSs=cKMSigDiffE.getClusterMeanSems();
        else
            MSs=cKMSigDiff.getClusterMeanSems();
        return MSs;
    }
    public PolynomialLineFittingSegmentNode[] getSmoothSegments(){
        return m_pcSmoothSegments;
    }
    public intRange getValidSmoothRange(PolynomialLineFittingSegmentNode seg){
        
        if(seg.getStart(0,1)==0&&seg.getStart(0,-1)==25){
            seg.nEnd=seg.nEnd;
        }
        int left=Math.min(seg.getStart(pbSemiballContactingConvex_Downward,2, 1),seg.getStart(pbSemiballContactingLx_Downward,1, 1)),right=Math.max(seg.getStart(pbSemiballContactingConvex_Downward,2, -1),seg.getStart(pbSemiballContactingLx_Downward,1, -1)),lenExt=1,start=seg.getStart(0, 1),end=seg.getStart(0, -1);
//        left=start;
//        right=end;
//        while(left<=end){
//            if(pbSmoothFittingSelection[left]){
//                if(left-start>=lenExt) break;
 //           }        
//            left++;
//        }
//        while(right>=start){
//            if(pbSmoothFittingSelection[right]){
//                if(end-right>=lenExt) break;
//            }        
//            right--;
 //       }
        right-=nMaxRisingInterval;
        return new intRange(left,right);
    }
    public boolean isSmoothProfile(int position){
        if(position>=0) return false;//12d01
        if(m_pcSmoothSegments==null) return false;
        if(position==12){
            position=position;
        }
        PolynomialLineFittingSegmentNode smoothSeg;
        smoothSeg=m_pcSmoothSegments[position];
        if(smoothSeg==null) return false;
        if(!smoothSeg.isSmooth(position,(double)nMaxRisingInterval)) return false;
        
        int nMaxNumDelta=3;
        ArrayList<Double> dvDeltas=new ArrayList();
        ArrayList<Integer> nvPositions=new ArrayList();
//            public static int getSortedIndependetDelta(double[] pdX,double[] pdY,int iI, int iF,double dMaxRisingInterval,int nMaxNumDelta,ArrayList<Double> dvDeltas,ArrayList<Integer> nvPositions){
        int start=CommonStatisticsMethods.getFirstSelectedPosition(pbSemiballContactingLx_Downward, true, position, -1);
        int end;
        
        if(position<m_pnRisingIntervals_Downward.length)
            end=CommonStatisticsMethods.getFirstSelectedPosition(pbSemiballContactingLx_Downward, true, position+m_pnRisingIntervals_Downward[position], 1);
        else
            end=smoothSeg.nEnd;
        
        CommonStatisticsMethods.getSortedIndependetDelta(pdX,pdProbeTrail_Downward,start,end, (double) nMaxRisingInterval,nMaxNumDelta,dvDeltas,nvPositions);
        int len=nvPositions.size();
        if(len<2) return false;
        double d0=dvDeltas.get(0),d1,ratio,rCutoff=2;
        for(int i=1;i<len;i++){
            d1=dvDeltas.get(i);
            ratio=d0/d1;
//            if(Math.abs(ratio)>rCutoff) return false;
            d0=d1;
        }
        
        return position<=getValidSmoothRange(smoothSeg).getMax();
    }
    public double[] getEnvTrail(){
        return pdProbeTrail_Downward;
    }
    public double[] getSigDiff(){
        return pdSigDiff;
    }
    int filterPartialTransitions(){
        locateSpecialPoints();
        double[] pdFiltedY=pdvProbeTrail_Downward.get(nNumBallScales);
        int[] pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdFiltedY, nMaxRisingInterval);        
        pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdFiltedY, pnIntevals, nMaxRisingInterval);      
        
        DoubleRange dr=CommonStatisticsMethods.getRange(pdSigDiff);
        double base,peak,peak0,yl,y,yr,yl0,yr0;
        double dMax=Math.max(Math.abs(dr.getMin()), Math.abs(dr.getMax())),cutoff=0.5*dMax;
//        double[] pdt=pdvProbeTrail_Downward.get(nNumBallScales);
        double[] pdt=pdY;
        
        
        ArrayList<Integer> nvLn=new ArrayList(),nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdY, nvLx, nvLx);
        boolean[] pbt=new boolean[nDataSize];
        CommonStatisticsMethods.setElements_AND(pbt, nvLx, nvvProbeContactingPositions_Downward.get(nNumBallScales-1), bSmoothCriteria);
        nvLx.clear();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbt, new intRange(0,nDataSize-1), nvLx);
        
        int i, len=nvLx.size(),p,ext=3,j;
        int position,left,right,left0,right0;
        double yl0t,yr0t;
        DoubleRange drL=new DoubleRange(),drR=new DoubleRange();
        for(i=1;i<len-1;i++){
            position=nvLx.get(i);
            if(position==43){
                i=i;
            }
            right0=nvLx.get(i+1);
            left0=nvLx.get(i-1);
            y=pdt[position];
            yl0=pdt[left0];
            yr0=pdt[right0];
            
            if((y-yl0)*(y-yr0)>0) continue;
            if(Math.abs(yl0-yr0)<cutoff) continue;
            if(Math.abs(yl0-y)<0.5*cutoff) continue;
            if(Math.abs(y-yr0)<0.5*cutoff) continue;
            left=CommonStatisticsMethods.getFirstExtremumIndex(pdY, position, -1, -1);
            right=CommonStatisticsMethods.getFirstExtremumIndex(pdY, position, -1, 1);            
            yl=pdY[left];
            yr=pdY[right];
            
            if((yl0-yl)*(yr0-yl)<0||(yl0-yr)*(yr0-yr)<0) continue;
            drL.setRange(yl0, yl0);
            drR.setRange(yr0, yr0);
            
            
                if(yl0>=yr0){
                    peak=yr0;//13312
                    for(j=2;j<=ext;j++){
                        if(i+j<len){
                            right0=nvLx.get(Math.min(len-1,i+j));
                            yr0t=pdt[right0];
                            if(yr0t<yr0) yr0=yr0t;
                            drR.expandRange(yr0t);
                        }
                    }
//                    peak=drR.getMax();//13312
                }else{
                    peak=yl0;//13312
                    for(j=2;j<=ext;j++){
                        if(j<=i){
                            left0=nvLx.get(i-j);
                            yl0t=pdt[left0];
                            if(yl0t<yl0) yl0=yl0t;
                            drL.expandRange(yl0t);
                        }                    
                    }
//                    peak=drL.getMax();//13312
                }
            
            if((yl0-yl)*(yr0-yl)<0||(yl0-yr)*(yr0-yr)<0) 
                continue;
            
            base=Math.max(pdY[left], pdY[right]);
            peak0=pdY[position];
            for(p=left+1;p<right;p++){
                pdY[p]=CommonMethods.getLinearIntoplation(base, base, peak0, peak, pdY[p]);
            }
        }
        
        buildBallTrails();
        return 1;
    }
    public int calSigDiff(){
//        filterPartialTransitions();
        boolean showMedian=IPOAnalyzerForm.isInteractive();
        boolean trimming=true;
        Color c;
        PlotWindowPlus pwt=null;
        if(showMedian){
            pwt=CommonGuiMethods.displayNewPlotWindowPlus("Trail and median filters", pdX, pdY, 1, 2, Color.BLACK);
        }
        double[] pdFiltedY=pdvProbeTrail_Downward.get(nNumBallScales),pdYFMultiscale=CommonStatisticsMethods.copyArray(pdFiltedY);
        int scale;
        int i,j, len,p,p0;
        double dMin,dMax,dRx,ratioCutoff=2.;
        int[] pnIntevals;   
        int interval=nMaxRisingInterval;
        interval=4;//13119
        interval=nMaxRisingInterval;//13130
        pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdY, interval);        
        ArrayList<Double> dvRsU=new ArrayList(), dvRsD=new ArrayList();
        dvRsU.add(5.);
        dvRsD.add(5.);
        dvRsU.add(5.);
        dvRsD.add(15.);
        int nRanking=0;
        ArrayList<double[]> pdvTrailsD=new ArrayList(), pdvTrailsU=new ArrayList();
        double[] trailD0,trailD,trailU0,trailU=null;
        ProbingBall pb;
        ArrayList<Integer> nvContacting,nvLx,nvLn;
        trailD0=pdY;
        pdvSigdiffs.clear();
        
        int index=0;
        for(scale=0;scale<dvRsD.size();scale++){
//        for(scale=0;scale<1;scale++){
            index++;
            dRx=dvRsD.get(scale);
            pb=new ProbingBall(pdX,trailD0,dRx,-1,nRanking);
            trailD=pb.getProbeTrail(pb.Downward);
            if(scale>0)
                trailD=CommonStatisticsMethods.getMultiscaleFiltedLine_EdgeProtection(pdY, trailD0, trailD, interval, ratioCutoff, null);
           if(trimming&&scale==0){
                nvContacting=pb.m_nvProbeContactingPointsDownward;
                nvLx=new ArrayList();
                for(i=0;i<nvContacting.size();i++){
                    p=nvContacting.get(i);
                    if(CommonStatisticsMethods.isLocalExtrema(trailD0, p, 1)) nvLx.add(p);
                }
                p0=nvLx.get(0);
                len=nvLx.size();
                for(i=1;i<len;i++){
                    p=nvLx.get(i);
                    dMin=Math.min(trailD[p0], trailD[p]);
                    for(j=p0;j<p;j++){
                        if(j==34){
                            j=j;
                        }
                        trailD[j]=Math.max(dMin, trailD[j]);
                    }
                    p0=p;
                }
            }
            c=CommonGuiMethods.getDefaultColor(index);
            if(showMedian) pwt.addPlot("trail"+index, pdX, trailD, 2, PlotWindow.LINE,c,true);
//            if(true) continue;
            trailU0=trailD;
            if(scale>0){
                dRx=dvRsU.get(scale);
                pb=new ProbingBall(pdX,trailU0,dRx,-1,nRanking);
                trailU=pb.getProbeTrail(pb.Upward);
                trailU=CommonStatisticsMethods.getMultiscaleFiltedLine_EdgeProtection(pdY, trailU0, trailU, interval, ratioCutoff, null);
            }else{
                trailU=trailU0;
            }
            
            if(false){
                nvContacting=pb.m_nvProbeContactingPointsUpward;
                nvLn=new ArrayList();
                for(i=0;i<nvContacting.size();i++){
                    p=nvContacting.get(i);
                    if(CommonStatisticsMethods.isLocalExtrema(trailU0, p, -1)) nvLn.add(p);
                }
                p0=nvLn.get(0);
                len=nvLn.size();
                for(i=1;i<len;i++){
                    p=nvLn.get(i);
                    dMax=Math.max(trailU[p0], trailU[p]);
                    for(j=p0;j<p;j++){
                        if(j==34){
                            j=j;
                        }
                        trailU[j]=Math.min(dMax, trailU[j]);
                    }
                    p0=p;
                } 
            }
            index++;
            c=CommonGuiMethods.getDefaultColor(index);
            if(showMedian) pwt.addPlot("trail"+index, pdX, trailU, 2, PlotWindow.LINE,c,true);
            trailD0=trailU;
            pdSigDiff=CommonStatisticsMethods.getDeltaArray(trailU, pnIntevals, nMaxRisingInterval);
            pdvSigdiffs.add(pdSigDiff);
        }
        pdSignal=trailU;
        return 1;
    }
/*     public int calSigDiff1(){
        locateSpecialPoints();
        double[] pdFiltedY=pdvProbeTrail_Downward.get(nNumBallScales),pdYFMultiscale=CommonStatisticsMethods.copyArray(pdFiltedY);
        int scale;
        int i,j, len,p,p0;
        double dMin;
        int[] pnIntevals;   
        
        ArrayList<Integer> nvContacting,nvLx;
                
//        PlotWindowPlus pwTest=CommonGuiMethods.displayNewPlotWindowPlus("Test Ball Trail", pdX, pdY, 1, 2, Color.black);
//        pwTest.addPlot("Ball Trail", pdX, pdvProbeTrail_Downward.get(nNumBallScales-1), 2, 2, Color.red);
        
        for(scale=0;scale<=nNumBallScales;scale++){  
            nvContacting=nvvProbeContactingPositions_Downward.get(scale);
            nvLx=new ArrayList();
            for(i=0;i<nvContacting.size();i++){
                p=nvContacting.get(i);
                if(CommonStatisticsMethods.isLocalExtrema(pdY, p, 1)) nvLx.add(p);
            }
            
            pdFiltedY=pdvProbeTrail_Downward.get(scale);
            p0=nvLx.get(0);
            len=nvLx.size();
            for(i=1;i<len;i++){
                p=nvLx.get(i);
                dMin=Math.min(pdFiltedY[p0], pdFiltedY[p]);
                for(j=p0;j<p;j++){
                    if(j==34){
                        j=j;
                    }
                    pdFiltedY[j]=Math.max(dMin, pdFiltedY[j]);
                }
                p0=p;
            }            
        }        
//        pwTest.addPlot("Ball Trail", pdX, pdvProbeTrail_Downward.get(nNumBallScales-1), 2, 2, Color.blue);
        
        int iterations=3,ws=5;
        locateSpecialPoints();
        ArrayList<String> svLabels=new ArrayList(), svTexts=new ArrayList();
        String title="Running Window Size";
        svLabels.add("Half Window Size");
        svTexts.add("5");
        double ratioCutoff=2;
        double[] pdYFilted0=CommonStatisticsMethods.copyArray(pdY);
        ArrayList<int[]> pnvIndexes=new ArrayList();
        ArrayList<double[]> pdvYs=new ArrayList();
        int index;
        
//        double[] trail=pdvProbeTrail_Downward.get(nNumBallScales);
        double[] trail=pdvProbeTrail_Downward.get(nNumBallScales-1);
        Color c;
        len=trail.length;
//        trail=CommonStatisticsMethods.getExtremaLine(pdY);
        int[] pnIndexes=new int[len],pnIndexes0=new int[len];
//        CommonStatisticsMethods.copyArray(pdY, pdYFilted0);
        boolean showMedian=IPOAnalyzer.isInteractive();
        
        int interval=nMaxRisingInterval;
        interval=4;//13119
        pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdY, interval);        
        trail=CommonStatisticsMethods.getMultiscaleFiltedLine_EdgeProtection(pdY, pdvProbeTrail_Downward.get(nNumBallScales-1), trail, interval, ratioCutoff, pdYFMultiscale);
        CommonStatisticsMethods.copyArray(trail, pdYFilted0);

        PlotWindowPlus pwt=null;
        if(showMedian){
            pwt=CommonGuiMethods.displayNewPlotWindowPlus("Trail and median filters", pdX, pdY, 1, 2, Color.BLACK);
            pwt.addPlot("Trail and median filters", pdX, trail, 2, 2, Color.BLACK);
        }
        
        for(i=0;i<len;i++){
            pnIndexes0[i]=i;
        }
        
        for(i=0;i<iterations;i++){
            pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(pdYFilted0,ws,ws,pnIndexes);
            pdYFMultiscale=CommonStatisticsMethods.getMultiscaleFiltedLine_EdgeProtection(pdY, pdYFilted0, pdFiltedY, interval, ratioCutoff, pdYFMultiscale);
//            pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdFiltedY, pnIntevals, nMaxRisingInterval);
            pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdYFMultiscale, pnIntevals, nMaxRisingInterval);
            pdvSigdiffs.add(pdSigDiff);
            int[] pnIndexest=new int[len];
            for(j=0;j<len;j++){
                index=pnIndexes[j];
                pnIndexest[j]=pnIndexes0[index];
                pdFiltedY[j]=pdYFilted0[index];
            }
            pnvIndexes.add(pnIndexes);
            pdvYs.add(pdFiltedY);
            pnIndexes0=pnIndexest;            
            
            c=CommonGuiMethods.getDefaultColor(i+1);
            if(showMedian) pwt.addPlot("Median Filter WS="+ws, pdX, pdYFMultiscale, 2, PlotWindow.LINE,c,true);
            CommonStatisticsMethods.copyArray(pdYFMultiscale, pdYFilted0);//not good to filt the filtered data of the previous iterations.
            ws*=2;
        }
        if(showMedian) pwt.addPlot("Median Filter WS="+ws, pdX, pdYFMultiscale, 2, PlotWindow.LINE,Color.orange,true);
        pdFiltedY=pdvYs.get(iterations-1);
        pdSignal=pdYFMultiscale;
//        pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdFiltedY, nMaxRisingInterval);         
                
        return 1;
    }*/
    public int calSigDiff0(){//13115
        locateSpecialPoints();
        double[] pdFiltedY=pdvProbeTrail_Downward.get(nNumBallScales);
        int[] pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdFiltedY, nMaxRisingInterval);        
        pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdFiltedY, pnIntevals, nMaxRisingInterval);        
        
        DoubleRange dr=CommonStatisticsMethods.getRange(pdSigDiff);
        double base,peak,peak0,yl,y,yr,yl0,yr0;
        double dMax=Math.max(Math.abs(dr.getMin()), Math.abs(dr.getMax())),cutoff=0.5*dMax;
        double[] pdt=pdvProbeTrail_Downward.get(nNumBallScales);
        ArrayList<Integer> nvLx=new ArrayList();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSemiballContactingLx_Downward, new intRange(0,nDataSize-1), nvLx);
        int i, len=nvLx.size(),p;
        int position,left,right,left0,right0;
        for(i=1;i<len-1;i++){
            position=nvLx.get(i);
            right0=nvLx.get(i+1);
            left0=nvLx.get(i-1);
            y=pdt[position];
            yl0=pdt[left0];
            yr0=pdt[right0];
            
            if((y-yl0)*(y-yr0)>0) continue;
            if(Math.abs(yl0-yr0)<cutoff) continue;
            if(Math.abs(yl0-y)<0.5*cutoff) continue;
            if(Math.abs(y-yr0)<0.5*cutoff) continue;
            left=CommonStatisticsMethods.getFirstExtremumIndex(pdY, position, -1, -1);
            right=CommonStatisticsMethods.getFirstExtremumIndex(pdY, position, -1, 1);            
            yl=pdY[left];
            yr=pdY[right];
            
            if((yl0-yl)*(yr0-yl)<0||(yl0-yr)*(yr0-yr)<0) continue;
            
            peak=Math.min(pdY[left0],pdY[right0]);
            base=Math.max(pdY[left], pdY[right]);
            peak0=pdY[position];
            for(p=left+1;p<right;p++){
                pdY[p]=CommonMethods.getLinearIntoplation(base, base, peak0, peak, pdY[p]);
            }
        }
        
        buildBallTrails();
        
        int ws=5;
        locateSpecialPoints();
        ArrayList<String> svLabels=new ArrayList(), svTexts=new ArrayList();
        String title="Running Window Size";
        svLabels.add("Half Window Size");
        svTexts.add("5");
        double[] pdYFilted0=CommonStatisticsMethods.copyArray(pdY);
        ArrayList<int[]> pnvIndexes=new ArrayList();
        ArrayList<double[]> pdvYs=new ArrayList();
        int iterations=2,j,index;
        
        double[] trail=pdvProbeTrail_Downward.get(nNumBallScales);
        Color c;
        len=trail.length;
//        trail=CommonStatisticsMethods.getExtremaLine(pdY);
        int[] pnIndexes=new int[len],pnIndexes0=new int[len];
        CommonStatisticsMethods.copyArray(trail, pdYFilted0);
//        CommonStatisticsMethods.copyArray(pdY, pdYFilted0);
        PlotWindowPlus pwt=CommonGuiMethods.displayNewPlotWindowPlus("Trail and median filters", pdX, pdY, 1, 2, Color.BLACK);
        pwt.addPlot("Trail and median filters", pdX, trail, 2, 2, Color.BLACK);
        
        for(i=0;i<len;i++){
            pnIndexes0[i]=i;
        }
        
        for(i=0;i<iterations;i++){
            pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(pdYFilted0,ws,ws,pnIndexes);
            
            int[] pnIndexest=new int[len];
            for(j=0;j<len;j++){
                index=pnIndexes[j];
                pnIndexest[j]=pnIndexes0[index];
                pdFiltedY[j]=pdYFilted0[index];
                pnvIndexes.add(pnIndexes);
                pdvYs.add(pdFiltedY);
            }
            pnIndexes0=pnIndexest;
            
            c=CommonGuiMethods.getDefaultColor(i+1);
            pwt.addPlot("Median Filter WS="+ws, pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
            CommonStatisticsMethods.copyArray(pdFiltedY, pdYFilted0);//not good to filt the filtered data of the previous iterations.
            ws*=2;
        }
        
        pdFiltedY=pdvYs.get(0);
        pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdFiltedY, nMaxRisingInterval);        
        pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdFiltedY, pnIntevals, nMaxRisingInterval);        
        return 1;
    }
    
    void adjustIntervediatePeaks(){
        
        double[] pdFiltedY=pdvProbeTrail_Downward.get(nNumBallScales);
        int[] pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdFiltedY, nMaxRisingInterval);        
        pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdFiltedY, pnIntevals, nMaxRisingInterval);        
        
        DoubleRange dr=CommonStatisticsMethods.getRange(pdSigDiff);
        double base,peak,peak0,yl,y,yr,yl0,yr0;
        double dMax=Math.max(Math.abs(dr.getMin()), Math.abs(dr.getMax())),cutoff=0.5*dMax;
        double[] pdt=pdvProbeTrail_Downward.get(nNumBallScales);
        ArrayList<Integer> nvLx=new ArrayList();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSemiballContactingLx_Downward, new intRange(0,nDataSize-1), nvLx);
        int i, len=nvLx.size(),p;
        int position,left,right,left0,right0;
        for(i=1;i<len-1;i++){
            position=nvLx.get(i);
            right0=nvLx.get(i+1);
            left0=nvLx.get(i-1);
            y=pdt[position];
            yl0=pdt[left0];
            yr0=pdt[right0];
            
            if((y-yl0)*(y-yr0)>0) continue;
            if(Math.abs(yl0-yr0)<cutoff) continue;
            if(Math.abs(yl0-y)<0.5*cutoff) continue;
            if(Math.abs(y-yr0)<0.5*cutoff) continue;
            left=CommonStatisticsMethods.getFirstExtremumIndex(pdY, position, -1, -1);
            right=CommonStatisticsMethods.getFirstExtremumIndex(pdY, position, -1, 1);            
            yl=pdY[left];
            yr=pdY[right];
            
            if((yl0-yl)*(yr0-yl)<0||(yl0-yr)*(yr0-yr)<0) continue;
            
            peak=Math.min(pdY[left0],pdY[right0]);
            base=Math.max(pdY[left], pdY[right]);
            peak0=pdY[position];
            for(p=left+1;p<right;p++){
                pdY[p]=CommonMethods.getLinearIntoplation(base, base, peak0, peak, pdY[p]);
            }
        }
        
        buildBallTrails();
        
        int ws=5;
        locateSpecialPoints();
        ArrayList<String> svLabels=new ArrayList(), svTexts=new ArrayList();
        String title="Running Window Size";
        svLabels.add("Half Window Size");
        svTexts.add("5");
        double[] pdYFilted0=CommonStatisticsMethods.copyArray(pdY);
        ArrayList<int[]> pnvIndexes=new ArrayList();
        ArrayList<double[]> pdvYs=new ArrayList();
        int iterations=2,j,index;
        
        double[] trail=pdvProbeTrail_Downward.get(nNumBallScales);
        Color c;
        len=trail.length;
//        trail=CommonStatisticsMethods.getExtremaLine(pdY);
        int[] pnIndexes=new int[len],pnIndexes0=new int[len];
        CommonStatisticsMethods.copyArray(trail, pdYFilted0);
//        CommonStatisticsMethods.copyArray(pdY, pdYFilted0);
        PlotWindowPlus pwt=CommonGuiMethods.displayNewPlotWindowPlus("Trail and median filters", pdX, pdYFilted0, 1, 2, Color.BLACK);
        
        for(i=0;i<len;i++){
            pnIndexes0[i]=i;
        }
        
        for(i=0;i<iterations;i++){
            pdFiltedY=CommonStatisticsMethods.getRunningWindowQuantile(pdYFilted0,ws,ws,pnIndexes);
            
            int[] pnIndexest=new int[len];
            for(j=0;j<len;j++){
                index=pnIndexes[j];
                pnIndexest[j]=pnIndexes0[index];
                pdFiltedY[j]=pdYFilted0[index];
                pnvIndexes.add(pnIndexes);
                pdvYs.add(pdFiltedY);
            }
            pnIndexes0=pnIndexest;
            
            c=CommonGuiMethods.getDefaultColor(i+1);
            pwt.addPlot("Median Filter WS="+ws, pdX, pdFiltedY, 2, PlotWindow.LINE,c,true);
            CommonStatisticsMethods.copyArray(pdFiltedY, pdYFilted0);//not good to filt the filtered data of the previous iterations.
            ws*=2;
        }
        
        pdFiltedY=pdvYs.get(0);
        pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdFiltedY, nMaxRisingInterval);        
        pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdFiltedY, pnIntevals, nMaxRisingInterval);        
        
        pnIntevals=CommonStatisticsMethods.calRisingIntervals(pdFiltedY, nMaxRisingInterval);
        pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdFiltedY, pnIntevals, nMaxRisingInterval);        
    }
    public int calSigDiff1(){
        findThickEnvelope();
        locateSpecialPoints();
        dethickenLine();
        ArrayList<Integer> nvLx=new ArrayList(),nvLn=new ArrayList(),nvp=new ArrayList();
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        boolean[] pbLocalMaxima=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbLocalMaxima, false);
        CommonStatisticsMethods.setElements(pbLocalMaxima, nvLx,true);
        int[] pnRisingInterval=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        
        double[] pdTopscaleDeltaD=pdvProbeTrailDelta_Downward.get(0);
        
        double[] pdTrailD=pdvProbeTrail_Downward.get(nNumBallScales),pdTrailU=pdvProbeTrail_Upward.get(nNumBallScales);
        pdSigDiff=new double[nDataSize];
        CommonStatisticsMethods.setElements(pdSigDiff,0);
        double[] pdDeltaD=pdvProbeTrailDelta_Downward.get(nNumBallScales),pdDeltaU=pdvProbeTrailDelta_Upward.get(nNumBallScales);
        double[] pdAbsDeltaU=CommonStatisticsMethods.getAbs(pdDeltaU);
        
        double[] pdDeltaTopScaleD=pdvProbeTrailDelta_Downward.get(0);
        int[] pnIntervals=pnvTrailRisingIntervals_Downward.get(nNumBallScales-1);
        int[] pnSegIndexes=pnvSegmentIndexes_Downward.get(nNumBallScales-1);
        int[] pnSegIndexesU0=pnvSegmentIndexes_Upward.get(0);
        int[] pnSegIndexesU=pnvSegmentIndexes_Upward.get(nNumBallScales-1);
        int p,left,right,right0,len1=pdDeltaD.length,r,pt,i,ix;
        pdSigDiff[0]=0;
        double validJumpRatio=1.8,delta,ratio,dD,dU;
        double cutoff,dx,dt;
        for(p=1;p<Math.min(pdDeltaD.length,pdDeltaU.length);p++){
            dD=Math.abs(pdDeltaD[p]);
            dU=Math.abs(pdDeltaU[p]);        
            if(p>=len1){
                pdSigDiff[p]=pdDeltaD[len1-1];
                continue;
            }
            
            r=p+CommonStatisticsMethods.getRisingInterval(pnIntervals, p, 1);
            left=CommonStatisticsMethods.getFirstSelectedPosition(pbThickEnvelope,pbMultipleScaleAnchorLx_Downward,false,true,p,-1);
            right=CommonStatisticsMethods.getFirstSelectedPosition(pbThickEnvelope,pbMultipleScaleAnchorLx_Downward,false,true,r,1);
            if(left<0) left=nvLx.get(0);
            if(left>p) left=p;
            if(right<0) right=nvLx.get(nvLx.size()-1);
            if(right<r) right=r;
            
            if(pnSegIndexes[p]==pnSegIndexes[r]){
                pdSigDiff[p]=pdDeltaD[p];
            }else{
                if(Math.abs(pdDeltaTopScaleD[p])<dMultiscaleSegCutoff) {
                    pdSigDiff[p]=pdDeltaTopScaleD[p];                
                }else{
/*                    if(pnSegIndexes[left]==pnSegIndexes[p]&&pnSegIndexes[r]==pnSegIndexes[right]){
                        pdSigDiff[p]=pdDeltaD[p];
                        if(pnSegIndexesU[left]==pnSegIndexesU[p]&&pnSegIndexesU[r]==pnSegIndexesU[right]){
                            if(dU>dD&&p>5)pdSigDiff[p]=pdDeltaU[p];
                            if(pdTrailU[r]<0) pdSigDiff[p]=pdDeltaD[p];
                        }
                    }else if((pdTrailD[left]-pdTrailD[p])*(pdTrailD[p]-pdTrailD[right])>0){
                        pdSigDiff[p]=pdDeltaD[p];
                        if(pnSegIndexesU[left]==pnSegIndexesU[p]&&pnSegIndexesU[r]==pnSegIndexesU[right]){
                            if(dU>dD&&p>5)pdSigDiff[p]=pdDeltaU[p];
                            if(pdTrailU[r]<0) pdSigDiff[p]=pdDeltaD[p];
                        }
                    }else{
                        pt=CommonStatisticsMethods.getLargestAbsPosition(pdAbsDeltaU, p, p+CommonStatisticsMethods.getRisingInterval(pnRisingInterval, p, 1), 1, null, 0);
                        dU=pdAbsDeltaU[pt];
                        pdSigDiff[p]=CommonStatisticsMethods.getMinAbsElement(pdDeltaU[pt], pdDeltaD[p]);
                    }*/
                    if(isSpike(pdDeltaD,pnRisingInterval,p)){
                        if(pdDeltaD[p]>0)
                            pt=CommonStatisticsMethods.getLargestAbsPosition(pdAbsDeltaU, p, p+CommonStatisticsMethods.getRisingInterval(pnRisingInterval, p, 1), 1, null, 0);
                        else
                            pt=CommonStatisticsMethods.getLargestAbsPosition(pdAbsDeltaU, p+CommonStatisticsMethods.getRisingInterval(pnRisingInterval, p, -1), p,  1, null, 0);
                        dU=pdAbsDeltaU[pt];
                        pdSigDiff[p]=CommonStatisticsMethods.getMinAbsElement(pdDeltaU[pt], pdDeltaD[p]);
                    }else{
                        pdSigDiff[p]=pdDeltaD[p];/*
                        if(pnSegIndexesU[left]==pnSegIndexesU[p]&&pnSegIndexesU[r]==pnSegIndexesU[right]){
                            if(dU>dD&&p>5)pdSigDiff[p]=pdDeltaU[p];
                            if(pdTrailU[r]<0) pdSigDiff[p]=pdDeltaD[p];
                        }*/
                    }
                }
            }
            if(p==170){
                p=p;
            }
            
//            if(pdDeltaD[p]<0) continue;
            if(true) continue;//12D11
            r=p+CommonStatisticsMethods.getRisingInterval(pnRisingInterval, p, 1);
            if(pdY[r]>=pdTrailD[r]) continue;
            //extend the multiple scale trail to "right"   
            
            right0=CommonStatisticsMethods.getFirstSelectedPosition(pbLocalMaxima, true, r, 1);
            if(right0<0) right0=nvLx.get(nvLx.size()-1);
            if(right0<=r) continue;
            if(pdSigDiff[p]>dMultiscaleSegCutoff){           
                ix=0;
                if(!pbMultipleScaleAnchorLx_Downward[right0]){
                    right=CommonStatisticsMethods.getFirstSelectedPosition(pbMultipleScaleAnchorLx_Downward, true, r, 1);
                    if(right<=r) continue;
                    if(right<0) right=nvLx.get(nvLx.size()-1);
                    CommonStatisticsMethods.getSelectedPositionsWithinRange(pbLocalMaxima, new intRange(right0,right-1), nvp);
                    delta=Math.abs(pdY[p]-pdY[r]);
                    cutoff=Math.min(0.5*delta, dMultiscaleSegCutoff);
//                    cutoff=0.5*delta;//12d10
                    for(i=nvp.size()-1;i>=0;i--){
                        pt=nvp.get(i);
                        if(Math.abs(pdY[right]-pdY[pt])<cutoff){
                            ix=i;
                            right=pt;
                        }else
                            break;
                    }
                    dx=pdY[right];
                    for(i=0;i<ix;i++){
                        pt=nvp.get(i);
                        dt=pdY[pt];
                        if(dt>dx) dx=dt;
                    }
                    dt=pdY[r];
                    if(dt>dx) dx=dt;
                    dt=pdY[p]-dx;
                    if(dt>pdSigDiff[p]) pdSigDiff[p]=dt;
                }
            }
//            pdSigDiff[i]=pdTopscaleDeltaD[i];
        }
        return 1;
    }
    int calSigDiff_EdgeProtectedFilter(){
        int nMultiplicity=2,iterations=1;
        double[] pdFiltedY=CommonStatisticsMethods.copyArray(pdY);
        EdgePreservingFilter filter=new EdgePreservingFilter(pdX,pdFiltedY,iterations);
        pdFiltedY=filter.getTransitionPreservingFiltedData(nMultiplicity,iterations);
        double pdt[]=CommonStatisticsMethods.getDeltaArray(pdFiltedY, null, nMaxRisingInterval);
        pdSigDiff=new double[nDataSize];
        for(int i=0;i<pdt.length;i++){
            pdSigDiff[i]=pdt[i];
        }
        return 1;
    }
    boolean isSpike(double[] pdDelta, int[] pnRisingInterval, int p0){
        int lLimit=CommonStatisticsMethods.getFirstSelectedPosition(pbSemiballContactingLx_Downward, true, 0, 1);
        
        int ws=15,left=Math.max(0, p0-ws),right=Math.min(nDataSize-1, p0+ws),p;
        if(right>=pdDelta.length) return false;
        
        double ratioCutoff=0.5,delta0=pdDelta[p0],absD0=Math.abs(delta0),delta;
        lLimit=Math.max(lLimit, p0+CommonStatisticsMethods.getRisingInterval(pnRisingInterval, p0, -1));
        for(p=lLimit;p>=left;p--){
            delta=pdDelta[p];
            if(Math.abs(delta)/absD0>ratioCutoff){
                if(delta*delta0>0)
                    break;
                else
                    return true;
            }
        }
        for(p=p0+CommonStatisticsMethods.getRisingInterval(pnRisingInterval, p0, 1);p<right;p++){
            delta=pdDelta[p];
            if(Math.abs(delta)/absD0>ratioCutoff){
                if(delta*delta0>0)
                    return false;
                else
                    return true;
            }
        }
        return false;
    }
    int findThickEnvelope(){
        int i,j,num=pdvProbeTrail_Downward.size();
        double[] pdEnvelopeThickness=CommonStatisticsMethods.copyArray(pdvProbeTrail_Downward.get(num-1));
        CommonStatisticsMethods.subtractArray(pdEnvelopeThickness,pdvProbeTrail_Upward.get(num-1));
        
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> nvOutliers=new ArrayList();
        
        double pValue=0.05,pOutlier=0.001;
        CommonStatisticsMethods.findOutliars(pdY, pOutlier, ms, nvOutliers);
        
        nvOutliers=CommonStatisticsMethods.findOutliers(pdY,ms,pValue,CommonStatisticsMethods.Large);
        
        pbThickEnvelope=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbThickEnvelope,false);
        CommonStatisticsMethods.setElements(pbThickEnvelope,nvOutliers,true);
        return 1;
    }
    int getThicknessAndExtrema(double[] pdX, double[] pdYt, double[] pdEnvD, double[] pdEnvU, double[] pdThickness, ArrayList<Integer> nvLn, ArrayList<Integer>nvLx,
            boolean[] pbContactingD, boolean[] pbContactingU, int iters){
        nvLn.clear();
        nvLx.clear();
        dRx=10;
        ProbingBall pb=new ProbingBall(pdX,pdYt,dRx,-1,0);
        ArrayList<Integer> nvProbeContactingD=pb.getProbeContactingPositions(ProbingBall.Downward);
        ArrayList<Integer> nvProbeContactingU=pb.getProbeContactingPositions(ProbingBall.Upward);
        CommonMethods.LocalExtrema(pdYt, nvLn, nvLx);
        CommonStatisticsMethods.setElements(pbContactingD, false);
        CommonStatisticsMethods.setElements_AND(pbContactingD, nvProbeContactingD,nvLx,true);
        CommonStatisticsMethods.setElements(pbContactingU, false);
        CommonStatisticsMethods.setElements_AND(pbContactingU, nvProbeContactingU,nvLn,true);
        
        nvLn.clear();
        nvLx.clear();
        CommonStatisticsMethods.buildAncoredLine(pdX,pdYt,pdEnvD,nvProbeContactingD);
        CommonStatisticsMethods.buildAncoredLine(pdX,pdYt,pdEnvU,nvProbeContactingU);
        CommonStatisticsMethods.copyArray(pdEnvD,pdThickness);
        CommonStatisticsMethods.subtractArray(pdThickness, pdEnvU);        
        CommonMethods.LocalExtrema(pdThickness, nvLn, nvLx);
//        PlotWindowPlus pw=CommonGuiMethods.displayNewPlotWindowPlus("DeThickening iter. "+iters, pdX, pdY, 1, 2, Color.BLACK);
//        pw.addPlot("EnvLineD", pdX, pdEnvD, 2, 2, Color.BLUE);
//        pw.addPlot("EnvLineU", pdX, pdEnvU, 2, 2, Color.RED);
//        pw.addPlot("Thickness", pdX, pdThickness, 2, 2, Color.RED);

        return 1;
    }
    public void dethickenLine(){
        pdYDethickended=CommonStatisticsMethods.copyArray(pdY);
        double[] pdYt0=CommonStatisticsMethods.copyArray(pdY),pdYt=CommonStatisticsMethods.copyArray(pdY);
        double[] pdEnvD=new double[nDataSize],pdEnvU=new double[nDataSize];
        boolean[] pdContactingU=new boolean[nDataSize],pdContactingD=new boolean[nDataSize];
        
        double[] pdThickness=new double[nDataSize];
        ArrayList<Integer> nvLx=new ArrayList(),nvLn=new ArrayList();
                              
        int i,len, p;
        ArrayList<Double> dvThickness=new ArrayList();        
        int[] pnIndexes=new int[nDataSize];   
        
        int iters=0,left,right;
        intRange ir=new intRange();
        int[] pnIterations=new int[nDataSize];
        CommonStatisticsMethods.setElements(pnIterations, -1);
        PlotWindowPlus pw=CommonGuiMethods.displayNewPlotWindowPlus("DeThickening Process", pdX, pdY, 1, 2, Color.BLACK);
        Color c;
        while(iters<50){
            CommonStatisticsMethods.setElements(pdThickness, -1);
            getThicknessAndExtrema(pdX,pdYt0,pdEnvD,pdEnvU,pdThickness,nvLn,nvLx,pdContactingD,pdContactingU,iters);  
            CommonStatisticsMethods.getElements(pdThickness,nvLx,dvThickness);
            CommonStatisticsMethods.setSortedIndexes(pnIndexes);
            QuickSort.quicksort(CommonStatisticsMethods.copyToDoubleArray(dvThickness), pnIndexes);
            len=nvLx.size();
            for(i=0;i<len;i++){
                p=nvLx.get(pnIndexes[len-1-i]);
                if(p==73){
                    i=i;
                }
                if(!Adjustable(p,pdContactingD,pdContactingU,pnIterations,pdYt,iters,ir)) continue;
                left=ir.getMin();
                right=ir.getMax();
                adjustPeak(pdX,pdYt0,pdYt,pnIterations, left,p,right,iters);
            }
            CommonStatisticsMethods.copyArray(pdYt,pdYt0);
            CommonStatisticsMethods.setElements(pnIterations,iters);
            iters++;
            c=CommonGuiMethods.getDefaultColor(iters);
            pw.addPlot("Iterations"+iters, pdX, pdYt, 2,2, c);
        }
    }
    int adjustPeak(double[] pdXt, double[] pdYt0,double[] pdYt, int[] pnIterations, int left, int p0, int right, int iters){
        double peak0=pdYt[p0], base, subBase,sign=1,dL=pdYt[left],dR=pdYt[right],wb,wp,peak,dt,xb,xp;
        int p;
        if((peak0-dL)*(peak0-dR)<0) return -1;
        if(peak0<dL) sign=-1;
        if(sign*(dL-dR)>0){
            base=dL;
            subBase=dR;
            xb=pdXt[left];
            xp=pdXt[p0];
        }else{
            base=dR;
            subBase=dL;            
            xb=pdXt[right];
            xp=pdXt[p0];
        }
        wb=5;
        wp=1;
        peak=(wb*base+wp*peak0)/(wb+wp);
        for(p=left+1;p<right;p++){
            dt=pdY[p];
            if(sign*(dt-base)<0) continue;
            dt=CommonMethods.getLinearIntoplation(xb, base, xp, peak, pdXt[p]);
            pdYt[p]=dt;
            pnIterations[p]=iters;
            
        }
        return 1;
    }
    boolean Adjustable(int p,boolean[] pbContactingD, boolean[] pbContactingU, int[] pnIterations, double[] pdYt, int iters, intRange ir){
        if(pnIterations[p]>=iters) return false;
        boolean[] pbt;
        if(pbContactingD[p])
            pbt=pbContactingU;
        else
            pbt=pbContactingD;
        int left=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p-1, -1),right=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p+1, 1);
        if(left<0) left=0;
        if(right<0) right=nDataSize-1;
        ir.setRange(left, right);
        if(pnIterations[left]>=iters||pnIterations[right]>=iters) return false;
        double dp=getBaseDiff(p,pbContactingD,pbContactingU,pdYt,pnIterations,iters);
        if(Double.isNaN(dp)) return false;
        double dl=getBaseDiff(left,pbContactingD,pbContactingU,pdYt,pnIterations,iters),dr=getBaseDiff(right,pbContactingD,pbContactingU,pdYt,pnIterations,iters);
        if(Double.isNaN(dl)||Double.isNaN(dr)) return false;
        if(Math.abs(dp)>Math.min(Math.abs(dl), Math.abs(dr))) return false;
        return true;
    }
    double getBaseDiff(int p,boolean[] pbContactingD, boolean[] pbContactingU, double[] pdYt,int[] pnIterations, int iters){
        boolean[] pbt;
        if(pbContactingD[p])
            pbt=pbContactingU;
        else
            pbt=pbContactingD;
        int left=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p-1, -1),right=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p+1, 1);
        if(left<0) left=0;
        if(right<0) right=nDataSize-1;
        if(pnIterations[left]>=iters||pnIterations[right]>=iters) return Double.NaN;
        return pdYt[left]-pdYt[right];
    }
    public double[] getSignal(){
        return pdSignal;
    }
    public boolean[] getSignificantTransitions(){
        return pbSignificantTransitions;
    }
}
