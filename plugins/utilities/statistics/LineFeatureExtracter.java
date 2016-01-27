/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;

/**
 *
 * @author Taihao
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class LineFeatureExtracter {    
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
                if(seg.pbSelected==pbSemiballContactingLx)
                    sig=LineSegmentRegressionEvaluater.getDevSignificance_Chisquare(seg.cPr, seg.pdX, seg.pdY,seg.pdSD,pbSemiballContactingConvex,seg.nStart,seg.nEnd);
                else if(seg.pbSelected==pbSemiballContactingConvex)
                    sig=LineSegmentRegressionEvaluater.getDevSignificance_Chisquare(seg.cPr, seg.pdX, seg.pdY,seg.pdSD,pbSemiballContactingLx,seg.nStart,seg.nEnd);
                if(sig<dPChiSQ) return true;
            }*/
            if(seg.dSigChiSQ<dPChiSQ) return true;
            for(int i=start;i<=end;i++){
                if(pdBallContactingPointsDevPValues[i]<dP) return true;
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
            if(position==12) {
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
        public boolean isInsignificantTransition(){
            if(position==0){
                position=position;
            }
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
            if(pdPValues[0]>0.1) insig=true;
            return insig;            
        }
    }
    
    int nMaxSegLen=20;
    ArrayList<Integer> nvMeanHighDiffClusterPositions,nvMeanLowDiffClusterPositions,nvMeanHighDiffClusterPositionsE;
    ArrayList<Integer> nvEnvHighDiffClusterPositions,nvEnvLowDiffClusterPositions,nvEnvHighDiffClusterPositionsE;
    ArrayList<Integer> nvMedianHighDiffClusterPositions,nvMedianLowDiffClusterPositions,nvMedianHighDiffClusterPositionsE;
    ArrayList<Integer> nvRegressionHighDiffClusterPositions,nvRegressionLowDiffClusterPositions,nvRegressionHighDiffClusterPositionsE;
    ArrayList<Integer> nvDetectedTransitionPoints,nvDetectedTransitionPointsE,nvHighClusterPoints,nvHighClusterPointsE;
    ArrayList<Integer> nvFiltedEnvDeltaPositions,nvTransitionCandidatePositions,nvTransitionCandidatePositionsLow,nvProbeContactingPosition;
    ArrayList<Integer> nvEnvDeltaAbsLx,nvDeltaYAbsLx;
    double[] pdX,pdY,pdSD,pdSDSemiballContactingLx,pdSDSemiballContacting,pdSDSemiballContactingConvex,pdDeltaY,pdDeltaYAbs;
    double[] pdBallTrailUpward,pdSemiballTrailDownward,pdDeltaBallTrailUpward,pdDeltaBallTrailDownward,pdDeltaOLR,pdDeltaOLRModified;
    double[] pdYRWMean,pdYRWMedian,pdYRWMeanDiffLR,pdYRWMedianDiffLR,pdYRWMeanL,pdYRWMedianL,pdYRWMeanR,pdYRWMedianR;
    double[] pdFiltedEnvUpper,pdFiltedEnvLower,pdFiltedEnvDeltaUpper,pdFiltedEnvDeltaLower;
    
/*    ArrayList<SignalTransitionStatisticsTests> cvTransitionStatisticsTestsMean,cvTransitionStatisticsTestsMeanLow;
    ArrayList<SignalTransitionStatisticsTests> cvTransitionStatisticsTestsMedian,cvTransitionStatisticsTestsMedianLow;
    ArrayList<SignalTransitionStatisticsTests> cvTransitionStatisticsTestsRegression,cvTransitionStatisticsTestsRegressionLow;*/
    
    PolynomialLineFitter m_cSignalFitter;
    ArrayList<double[]> pdvFiltedData_EdgePreserving;
    ArrayList<double[]> pdvProbeTrailDelta;
    PolynomialLineFittingSegmentNode[] m_pcOptimalSegments,m_pcStartingSegments,m_pcEndingSegments,m_pcLongSegments,m_pcSmoothSegments;
    double dPChiSQ,dPPWDev,dPTilting,dPSideness;
    int nMaxRisingInterval;
    boolean[] pbSelected,pbProbeContacting_Downward,pbFullSelection,pbUpperSmoothRegions,pbUpperProximityPositions,pbSelectedDelta,pbSmoothFittingSelection;//pbUpperSmoothRegions: A position in a upper smooth region is a point that is connected
    //to at least on upper (downward)probe contacting point by a series of points with all delta is smaller than the cutoff.
    int nWsSD,nOrder,nWs;
    double dOutliarRatio,dRx,dPTerminals,dSDSelected;
    String sSignalName;
    int[] m_pnRisingIntervals;
    ArrayList<int[]> pnvTrailRisingIntervals;
    ArrayList<Double> dvFiltedEnvDelta,dvMeanDiffs,dvMedianDiffs,dvRegressionDiffs;
    ArrayList<OneDKMeans> cvKMeans_EffectiveAmpDiffs;
    ArrayList<OneDKMeans> cvKMeans_FiltedEnvDelta;
    double dFiltedEnvThickness;
    MeanSem1 cMsY,cMsDeltaY,cMsDeltaYE,cMsEnvThickness,cMsDeltaYSelected,cMsDeltaYSelectedE;
    int nDataSize,nWsDiff;
    ArrayList<intRange> cvLRangesMean, cvRRangesMean;
    ArrayList<intRange> cvLRangesMediab, cvRRangesMedian;
    double dEnvDeltaCutoff,smoothDeltaCutoff;
    PlotWindowPlus pw;
    boolean pbTemp[],pbEnvTransitionPoints[],pbEnvHighDiffClusterPositions[],pbSemiBallContacting[],pbSemiballContactingLx[],pbSemiballContactingConvex[];
    boolean pbEnvTransitionPointsE[],pbEnvHighDiffClusterPositionsE[],pbMultipleScaleAnchorLx[],pbTopscaleAnchor[];
    LineFeatureExtracter.SignalTransitionStatisticsTests[] pcTransitionTests;
    int[] pnHighDiffClusterPosition;//three digit numbers, non-zero first, second and third digits indicate the position is a high cluster position of Mean, Median and Regression diffs, respectively
    int[] pnLowDiffClusterPosition;//three digit numbers, non-zero first, second and third digits indicate the position is a high cluster position of Mean, Median and Regression diffs, respectively
    
    int[] pnHighDiffClusterPositionE;//Clustering without the largest element
    int[] pnLowDiffClusterPositionE;//Clustering without the largest element    
    OneDKMeans cKMDeltaEnv,cKMDeltaMean,cKMDeltaMeanE,cKMDeltaMedian,cKMDeltaRegression,cKMDeltaRegressionE,cKMDeltaPW;
    OneDKMeans_ExtremExcluded cKM_EnvDiff_ExtremeExclusion,cKM_MeanDiff_ExtremeExclusion,cKM_MedianDiff_ExtremeExclusion,cKM_RegressionDiff_ExtremeExclusion;
    int[][] m_ppnSmoothRegionLength;
    int[] m_pnMinSoomthRegionLength;
    int nNumSmoothCriteria;
    int nNumBallScales;
    boolean bCheckClusteringOnly;
    ArrayList<ArrayList<Integer>> nvvProbeContactingPositions,nvvAnchoredBallTrailDeltaLx,nvvBallContactingExtrema;
    ArrayList<double[]> pdvProbeContactingPointsX;
    ArrayList<double[]> pdvProbeContactingPointsY;
    ArrayList<Integer> nvBallRollingWs;
    ArrayList<String> svBallRollingWs;
    ArrayList<double[]> pdvProbeTrailDownward;
    ArrayList<double[]> pdvProbeContactingPointsDevX;
    ArrayList<double[]> pdvProbeContactingPointsSDX;
    ArrayList<double[]> pdvProbeContactingPointsDevY;
    ArrayList<double[]> pdvProbeContactingPointsSDY;
    ArrayList<double[]> pdvP_TrailDelta,pdvP_TrailHeight;
    ArrayList<LineSegmentNode> cvSignalLevelSegments, cvSignalLevelSegmentsE;
    ArrayList<ArrayList<intRange>> cvvBallTrailSegmenters;
    ArrayList<Double> dvPSegmentation,dvPConsolidation;
    ArrayList<ParValidityChecker> cvParCheckers;
    int[] pnProbeGrade;//index of nvBallRollingWs
    double[] pdOptimalProbeTrail;
    double[] pdSigDiff;
    double[] pdBallContactingPointsDevPValues;
    boolean bFeatureLinesOnly;
    boolean bSmoothCriteria;
    ArrayList<boolean[]> pbvSemiballContacting;
    public LineFeatureExtracter(){
        bFeatureLinesOnly=false;
        nWsDiff=5;
        nvBallRollingWs=new ArrayList();
        nvvProbeContactingPositions=new ArrayList();
        pdvProbeTrailDownward=new ArrayList();
        
        double dPSeg=0.15,dPCons=0.3;
        nvBallRollingWs.add(15);
//        nvBallRollingWs.add(15);
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
        cvParCheckers.add(new LineFeatureExtracter.PNParChecker());
        bSmoothCriteria=false;
    }
    
    public LineFeatureExtracter(String sSignalName, double[] pdX, double[] pdY){
        this();
        nDataSize=pdX.length;
        bFeatureLinesOnly=true;
        this.pdX=pdX;
        this.pdY=pdY;
        this.sSignalName=sSignalName;
        nMaxRisingInterval=2;
        buildFeatureLines(null);
    }
    
    public LineFeatureExtracter(String sSignalName, double[] pdX, double[] pdY, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder){
        this(sSignalName,pdX,pdY,nMaxInterval,dPChiSQ,dPPWDev,dPTilting, dPSideness,dPTerminals,nOrder,false);
    }
    public LineFeatureExtracter(String sSignalName, double[] pdX, double[] pdY, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder, boolean bCheckClusteringOnly){
        this();
        extractFeature(sSignalName, pdX,pdY, nMaxInterval, dPChiSQ,dPPWDev,dPTilting,dPSideness, dPTerminals, nOrder, bCheckClusteringOnly,false);
    }
    public LineFeatureExtracter(String sSignalName, double[] pdX, double[] pdY, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder, boolean bCheckClusteringOnly, boolean bSmooth){
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
        
        pdYRWMeanL=new double[nDataSize];
        pdYRWMeanR=new double[nDataSize];
        pdYRWMeanL=new double[nDataSize];
        pdYRWMeanR=new double[nDataSize];
        pdYRWMeanDiffLR=new double[nDataSize];
            
        pdYRWMedianL=new double[nDataSize];
        pdYRWMedianR=new double[nDataSize];
        pdYRWMedianL=new double[nDataSize];
        pdYRWMedianR=new double[nDataSize];
        pdYRWMedianDiffLR=new double[nDataSize];
        pcTransitionTests=new LineFeatureExtracter.SignalTransitionStatisticsTests[nDataSize];
        pbTemp=new boolean[nDataSize];
        
        m_pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdY, nMaxRisingInterval, nWsSD);
        pbFullSelection=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbFullSelection, true);
        buildBallTrails();
        time1=System.currentTimeMillis();
//        fitTrackSignal_Addaptive(nOrder,dPChiSQ,dPTilting,dPSideness,dPPWDev,dOutliarRatio);
        time2=System.currentTimeMillis();
        dt=time2-time1;
        pdYRWMean=CommonStatisticsMethods.getRunningWindowAverage(pdY, 0, nDataSize-1, nWsDiff, false);
        ArrayList<Integer> nvIndexes=null;
        pdYRWMedian=CommonStatisticsMethods.getRunningWindowQuantile(pdY, nWsDiff, nWsDiff, nvIndexes);
        pdDeltaY=CommonStatisticsMethods.getDeltaArray(pdY, m_pnRisingIntervals, nMaxRisingInterval);
        
        pdDeltaBallTrailUpward=CommonStatisticsMethods.getDeltaArray(pdBallTrailUpward, null, nMaxRisingInterval);
        pdDeltaBallTrailDownward=CommonStatisticsMethods.getDeltaArray(pdSemiballTrailDownward, null, nMaxRisingInterval);
        
        pdDeltaYAbs=CommonStatisticsMethods.getAbs(pdDeltaY);
        nvDeltaYAbsLx=new ArrayList();
        CommonMethods.LocalExtrema(pdDeltaYAbs, new ArrayList<Integer>(), nvDeltaYAbsLx);
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
            pw=CommonGuiMethods.addPlot(pw, "Iteration"+i, pdX, pdFiltedEnvUpper, 2, 2, CommonGuiMethods.getDefaultColor(ci), true);
            ci++;
            pw=CommonGuiMethods.addPlot(pw, "Iteration"+i, pdX, pdFiltedEnvLower, 2, 2, CommonGuiMethods.getDefaultColor(ci), true);
        }
    }
//    public LineFitter_AdaptivePolynomial(double[] pdX, double[] pdY, boolean[] pbSelected, int nRisingInterval, int nOrder, int maxSegLen, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ,double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
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
        nOrder=1;
        LineFitter_AdaptivePolynomial cSignalFitter=new LineFitter_AdaptivePolynomial(pdX,pdY,pbFullSelection,nMaxRisingInterval,nOrder,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,0.15,0.15,0.15,dPTerminals, dOutliarRatio);
        m_ppnSmoothRegionLength[0]=CommonStatisticsMethods.copyArray(cSignalFitter.getSmoothSegLengths());
        if(IPOAnalyzerForm.isInteractive())pw=CommonGuiMethods.displayNewPlotWindowPlus("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[0]), 2, 2, Color.blue);
 
        cSignalFitter=new LineFitter_AdaptivePolynomial(pdX,pdY,pbSelected,nMaxRisingInterval,1,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,0.05,0.05,0.05,dPTerminals, dOutliarRatio,0.34);
        m_ppnSmoothRegionLength[1]=CommonStatisticsMethods.copyArray(cSignalFitter.getSmoothSegLengths());
        if(IPOAnalyzerForm.isInteractive())pw.addPlot("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[1]), 2, 2, Color.red);
 
        cSignalFitter=new LineFitter_AdaptivePolynomial();
        double[] pdSD=calSD(pdX,pdY,pbSelected,nMaxRisingInterval,20);
        cSignalFitter.setSmoothSegTerminalLength(new IntPair(nTerminalLength,nTerminalLength));
        cSignalFitter.setSD(pdSD);
        cSignalFitter.fit(pdX,pdSemiballTrailDownward,pbFullSelection,nMaxRisingInterval,nOrder,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,0.05,0.05,0.05,0, dOutliarRatio);
        m_ppnSmoothRegionLength[2]=CommonStatisticsMethods.copyArray(cSignalFitter.getSmoothSegLengths());
        if(IPOAnalyzerForm.isInteractive())pw.addPlot("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[2]), 2, 2, Color.BLACK);
        
        boolean[] pbt;
        pbSemiballContactingLx=new boolean[nDataSize];
        pbSemiballContactingConvex=new boolean[nDataSize];
        pdSDSemiballContactingLx=new double[nDataSize];
        pdSDSemiballContactingConvex=new double[nDataSize];
        
        cSignalFitter=new LineFitter_AdaptivePolynomial();
        ArrayList<Integer> nvx=new ArrayList(), nvn=new ArrayList(),convexPoints;
        CommonMethods.LocalExtrema(pdY, nvn, nvx);
        convexPoints=CommonStatisticsMethods.getConvexPoints(pdX, pdY, 1, 1);
        
//        CommonStatisticsMethods.setElements(pbt, false);
        CommonStatisticsMethods.setElements(pbSemiballContactingLx, false);
        CommonStatisticsMethods.setElements(pbSemiballContactingConvex, false);
        
        CommonStatisticsMethods.setElements_AND(pbSemiballContactingLx, nvvProbeContactingPositions.get(nvvProbeContactingPositions.size()-1),nvx,true);//12o31
        CommonStatisticsMethods.setElements_AND(pbSemiballContactingConvex, nvvProbeContactingPositions.get(nvvProbeContactingPositions.size()-1),convexPoints,true);//12n13
//        CommonStatisticsMethods.setElements_AND(pbt, nvvProbeContactingPositions.get(0),convexPoints,true);//12o31
//        CommonStatisticsMethods.setElements(pbt, nvvProbeContactingPositions.get(nvvProbeContactingPositions.size()-1),true);//12n13
        
        nTerminalLength=2;
        m_pnMinSoomthRegionLength[3]=3000;
        cSignalFitter.setSmoothSegTerminalLength(new IntPair(nTerminalLength,nTerminalLength));
//        pdSD=calSD(pdX,pdY,pbt,nMaxRisingInterval,20);
//        CommonStatisticsMethods.setElements(pbt, false);
        
        ArrayList<double[]> pdvMeanSD=CommonStatisticsMethods.calRWSD_SelectedDataDevBased(pdX, pdY, pbSemiballContactingLx, nMaxRisingInterval, nWsSD, 0.01);
        pdSDSemiballContactingLx=pdvMeanSD.get(1);
        
        pdvMeanSD=CommonStatisticsMethods.calRWSD_SelectedDataDevBased(pdX, pdY, pbSemiballContactingConvex, nMaxRisingInterval, nWsSD, 0.01);
        pdSDSemiballContactingConvex=pdvMeanSD.get(1);
        
        nOrder=2;
        int nMinLen=4;
        double dPChiSQ=0.5,dPPWDev=0.1,dPTilting=0.1,dPSideness=0.1,dPTerminals=0.;
//        cSignalFitter.setLineFeatureExtracter(this);//11d03
        cSignalFitter.setParValidityCheckters(cvParCheckers);
        
        pdSD=pdSDSemiballContactingLx;
        pbt=pbSemiballContactingLx;
        pbSmoothFittingSelection=pbt;
        cSignalFitter.setSD(pdSD);        
        cSignalFitter.setRisingInterval(nMaxRisingInterval);
        cSignalFitter.fit(pdX,pdY,pbt,nMaxRisingInterval,nOrder,nMinLen,nMaxSegLen,nWsSD,nWsTilting,nWsPWDev,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals, dOutliarRatio);
        m_pcSmoothSegments=cSignalFitter.getLongRegressions();

        nMinLen=6;
        pdSD=pdSDSemiballContactingConvex;
        pbt=pbSemiballContactingConvex;
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
        if(IPOAnalyzerForm.isInteractive())pw.addPlot("SmoothSegLength", pdX, CommonStatisticsMethods.copyToDoubleArray(m_ppnSmoothRegionLength[3]), 2, 2, Color.ORANGE);
    }
    void extractFeatures(){
        makeSelection();
        FiltData_EdgePreserving();
        buildFiltedEnvlines();
        extractedFiltedEnvFeatures();
//        calSmoothSegLengths();
        calDeltaArrays();
        buildAndMarkDiffClusters();
        if(!bCheckClusteringOnly){
            fitTrackSignal_Addaptive(nOrder,dPChiSQ,dPTilting,dPSideness,dPPWDev,dPTerminals,dOutliarRatio);
//            calSmoothSegLengths();
            PolynomialLineFittingSegmentNode segL,segR;
            int i,ll,rr,r,len=pdX.length;
            pdDeltaOLR=new double[nDataSize];
            pdDeltaOLRModified=new double[nDataSize];
            double delta,xL,xR,yL,yR;
            int len1=m_pnRisingIntervals.length;
            for(i=0;i<len;i++){
                if(i==0){
                    i=i;
                }
                if(i<len1)
                    r=i+m_pnRisingIntervals[i];
                else
                    r=nDataSize-1;
                xL=pdX[i];
                xR=pdX[r];
                segL=m_pcEndingSegments[i];
                segR=m_pcStartingSegments[r];

                if(segL!=null) 
                    yL=segL.predict(xL);
                else
                    yL=pdYRWMean[i];

                if(segR!=null)
                    yR=segR.predict(xR);
                else
                    yR=pdYRWMean[r];

                pdDeltaOLR[i]=yL-yR;
                if(segL==null||segR==null)
                    pdDeltaOLRModified[i]=yL-yR;
                else
                    pdDeltaOLRModified[i]=getModifiedDelta(segL.getRegressionNode(),segR.getRegressionNode(),pdX,pdYRWMean,i,r);
            }
            adjustStartingAndEndingSegments();
            buildAndMarkRegressionClusters();
            calStatisticsTests();
            markHigherClusterPoints();
            displayTransitionInfo();
            cvSignalLevelSegments=buildSegments(nvDetectedTransitionPoints);
            cvSignalLevelSegmentsE=buildSegments(nvDetectedTransitionPointsE);
            if(IPOAnalyzerForm.isInteractive()) displayFeatures();
        }
    }
    public void extractedFiltedEnvFeatures(){
        int i,len=pdFiltedEnvUpper.length;
        dFiltedEnvThickness=0;
        nvFiltedEnvDeltaPositions=new ArrayList();
        double delta;
        double[] pdDelEnv=new double[len];
        DoubleRange envDiffRange=new DoubleRange();
        for(i=0;i<len;i++){
            delta=pdFiltedEnvUpper[i]-pdFiltedEnvLower[i];
            pdDelEnv[i]=delta;
            if(delta>dFiltedEnvThickness)dFiltedEnvThickness=delta;
        }
        cMsEnvThickness=CommonStatisticsMethods.buildMeanSem1(pdDelEnv, 0, len-1, 1);
        pdFiltedEnvDeltaUpper=CommonStatisticsMethods.getDeltaArray(pdFiltedEnvUpper, null, nMaxRisingInterval);
        pdFiltedEnvDeltaLower=CommonStatisticsMethods.getDeltaArray(pdFiltedEnvLower, null, nMaxRisingInterval);
        
        dvFiltedEnvDelta=new ArrayList();
        nvEnvDeltaAbsLx=new ArrayList();
        ArrayList<Integer> nvLn=new ArrayList();
        len=pdFiltedEnvDeltaUpper.length;
        double[] pdDiff=new double[len];
        for(i=0;i<len;i++){
            pdDiff[i]=Math.abs(pdFiltedEnvDeltaUpper[i]);
        }
        
        CommonMethods.LocalExtrema(pdDiff, nvLn, nvEnvDeltaAbsLx);
        DoubleRange dr=CommonStatisticsMethods.getRange(pdFiltedEnvDeltaUpper);
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
            dvFiltedEnvDelta.add(diffAbs);
            if(diffAbs>=dEnvDeltaCutoff)nvFiltedEnvDeltaPositions.add(position);
            indexes[i]=i;
        }
        
        if(dvFiltedEnvDelta.size()>2) cKM_EnvDiff_ExtremeExclusion=new OneDKMeans_ExtremExcluded("Exctreme Excluded Env KMean",dvFiltedEnvDelta,2,1);
        
        
        QuickSort.quicksort(CommonStatisticsMethods.copyToDoubleArray(dvFiltedEnvDelta), indexes);
        int[]pnClusterIndexes=new int[len];
        int nx=indexes[len-1],j,K;
        OneDKMeans cKM;
        AnalysisMasterForm.newLine();
        cvKMeans_FiltedEnvDelta=new ArrayList();
        for(i=0;i<2;i++){
            if(dvFiltedEnvDelta.size()<i+2) continue;
            K=i+2;
            for(j=0;j<K;j++){
                nx=indexes[len-1-j];
                pnClusterIndexes[nx]=K-1-j;
            }
            cKM=new OneDKMeans("Filted EnvLine",dvFiltedEnvDelta,K,pnClusterIndexes);
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
        int i,position,left, len=nvProbeContactingPosition.size(),right,signL,signR;
        for(i=0;i<len;i++){
            position=nvProbeContactingPosition.get(i);
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
                    right+=(m_pnRisingIntervals[right-1]-1);
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
        double[] pdMultiscaleBallTrail=pdvProbeTrailDownward.get(pdvProbeTrailDownward.size()-1);
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
        pbProbeContacting_Downward=new boolean[nDataSize];
        pbUpperSmoothRegions=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbUpperSmoothRegions, false);
        CommonStatisticsMethods.markSelections(pbProbeContacting_Downward, nvProbeContactingPosition);
//        selectUperSmoothRetions();
        selectUperProximityPositions();
        setSelection(pbUpperProximityPositions);
                
    }
    public void setSelection(boolean[] pbSelected){
        this.pbSelected=pbSelected;
        calSelectedDevs();
    }
    public void calSelectedDevs(){
        pbSelectedDelta=CommonStatisticsMethods.getDeltaSelection_AND(pbSelected, m_pnRisingIntervals);
        cMsDeltaYSelected=CommonStatisticsMethods.buildMeanSem1_Selected(pdDeltaY, pbSelectedDelta, 0, pdDeltaY.length-1, 1);
        cMsDeltaYSelectedE=new MeanSem1();
        CommonStatisticsMethods.findOutliars(pdDeltaY, pbSelectedDelta, 0.01, cMsDeltaYSelectedE, new ArrayList());
        
        pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdX, pdY, m_pnRisingIntervals, pbSelected, nWsSD);
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
            pdYRWMeanDiffLR[lr]=0;
            
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
//                lMean=pdSemiballTrailDownward[lr];
//                lMedian=lMean;               
            }
            
            if(true){
                rMean=pdSemiballTrailDownward[rl];//12n17          
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
//            meanDiff=pdSemiballTrailDownward[lr]-pdSemiballTrailDownward[rl];
            meanDiff=pdSemiballTrailDownward[lr]-pdSemiballTrailDownward[rl];
            sign=1.;
            if(lr==3){
                lr=lr;
            }
            if(meanDiff<0) sign=-1.;
/*            if(lr>nMaxRisingInterval){
                if(rl<nDataSize-nMaxRisingInterval){
                    meanDiffL=pdSemiballTrailDownward[lr-nMaxRisingInterval]-pdSemiballTrailDownward[lr];
                    meanDiffR=pdSemiballTrailDownward[rl]-pdSemiballTrailDownward[rl+nMaxRisingInterval];
                    correction=meanDiffL;
                    if(sign*(meanDiffR-meanDiffL)>0) correction=meanDiffR;
                }else{
                    correction=pdSemiballTrailDownward[lr-nMaxRisingInterval]-pdSemiballTrailDownward[lr];
                }
            }else{
                if(rl<nDataSize-nMaxRisingInterval){
                    correction=pdSemiballTrailDownward[rl]-pdSemiballTrailDownward[rl+nMaxRisingInterval];
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
            pdYRWMeanDiffLR[lr]=meanDiff;
//            pdYRWMeanDiffLR[lr]=lMean-rMean;
            
            pdYRWMedianL[lr]=lMedian;
            pdYRWMedianR[ll]=lMedian;
            pdYRWMedianL[rr]=rMedian;
            pdYRWMedianR[rl]=rMedian;
            pdYRWMedianDiffLR[lr]=lMedian-rMedian;
        }
    }
    double getMean(int left, int right){
//        double mean=CommonStatisticsMethods.getMean(pdY, pbSelected,left,right, 1);
        double mean=CommonStatisticsMethods.getMean(pdSemiballTrailDownward,pbFullSelection,left,right, 1);//12o25
        return mean;
    }
    double getMedian(int left, int right){
//        double median=CommonStatisticsMethods.getMedian(pdY, pbSelected,left,right, 1);
        double median=CommonStatisticsMethods.getMedian(pdSemiballTrailDownward,pbFullSelection,left,right, 1);//12o25
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
            pdDeltaOLR[position]=pdYRWMeanDiffLR[position];
            pdDeltaOLRModified[position]=pdYRWMeanDiffLR[position];
        }
        return 1;
    }
    
    OneDKMeans buildEnvDeltaCutoffBasedDiffCluster(String title, double[] pdDiffs,int digitIndex, ArrayList<Double> dvDiffs, ArrayList<Integer> nvHighDiffClusterPositions, ArrayList<Integer> nvLowDiffClusterPositions, 
            ArrayList<Integer> nvHighDiffClusterPositionsE,int[] pnHighDiffClusterPosition, int[] pnLowDiffClusterPosition, int[] pnHighDiffClusterPositionE){
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        
        int i, len=pdDiffs.length, position,positionp;
        double[] pdDiffAbs=new double[len];
        for(i=0;i<len;i++){
            pdDiffAbs[i]=Math.abs(pdDiffs[i]);
        }
        pdDiffAbs[0]=0;//12d01 this is to eliminate the transition from a single-point first level.
        
        CommonMethods.LocalExtrema(pdDiffAbs, nvLn, nvLx);    
        len=nvLx.size();
        if(nvLx.get(len-1)>=pdFiltedEnvDeltaUpper.length){
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
            if(position>=pdFiltedEnvDeltaUpper.length) {
                diffAbs=pdDiffAbs[position];
            }else if(Math.abs(pdFiltedEnvDeltaUpper[position])<dEnvDeltaCutoff){
                positionp=getOptimalJumpingPosition(pdFiltedEnvDeltaUpper,position,pdDiffs[position]);
                if(Math.abs(pdFiltedEnvDeltaUpper[positionp])<dEnvDeltaCutoff)
                    diffAbs=Math.abs(CommonStatisticsMethods.getMinAbsElement(pdDiffs[position],pdFiltedEnvDeltaUpper[position]));
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
        OneDKMeans cKME;
        
        if(cKM.getClusterSizes()[1]==1&&cKM.m_nSize>2)
            cKME=CommonStatisticsMethods.OneDKMeans_LargestAttenuated(title, dvDiffs, 2);
        else
            cKME=cKM;
        //new OneDKMeans_ExtremExcluded(title+"_E", dvDiffs,2,1);
        
        pnClusterIndexes=cKM.getClusterIndexes();
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.appendStrings(cKM.toStrings());
        
        int[] pnClusterIndexesE=cKME.getClusterIndexes();
        AnalysisMasterForm.newLine();
        AnalysisMasterForm.appendStrings(cKME.toStrings());
        
        int positionDx;
        
        for(i=0;i<nvLx.size();i++){
            position=nvLx.get(i);
            if(position==68){
                i=i;
            }
            if(position>=pdFiltedEnvDeltaUpper.length) continue;
            positionDx=getOptimalTransitionPosition(position,pdDiffs[position]);
            
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
        int pp=CommonStatisticsMethods.getLargestPositionInRange(pdT, positions, new intRange(Math.max(0,position-nMaxRisingInterval),Math.min(position+nMaxRisingInterval,pdFiltedEnvDeltaUpper.length)), sign);
        if(pp<0) 
            pp=position;
        return pp;
    }
    int getOptimalTransitionPosition(int position, double dSign){
        if(position<0||position>=pdFiltedEnvDeltaUpper.length) return position;        
        int pp=getOptimalJumpingPosition(pdFiltedEnvDeltaUpper,position,dSign);
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
        
        dvMeanDiffs=new ArrayList();
        dvMedianDiffs=new ArrayList();
        
        nvMeanHighDiffClusterPositions=new ArrayList();
        nvMeanHighDiffClusterPositionsE=new ArrayList();
        nvMeanLowDiffClusterPositions=new ArrayList();
        
        nvMedianHighDiffClusterPositions=new ArrayList();
        nvMedianHighDiffClusterPositionsE=new ArrayList();
        nvMedianLowDiffClusterPositions=new ArrayList();
        
        nvRegressionHighDiffClusterPositions=new ArrayList();
        nvRegressionHighDiffClusterPositionsE=new ArrayList();
        nvRegressionLowDiffClusterPositions=new ArrayList();
        
        nvEnvHighDiffClusterPositions=new ArrayList();
        nvEnvHighDiffClusterPositionsE=new ArrayList();
        nvEnvLowDiffClusterPositions=new ArrayList();
        dvFiltedEnvDelta.clear();
        
        cKMDeltaMean=buildEnvDeltaCutoffBasedDiffCluster("Mean diff", pdYRWMeanDiffLR,0,dvMeanDiffs,nvMeanHighDiffClusterPositions,nvMeanLowDiffClusterPositions,nvMeanHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
        cKMDeltaMedian=buildEnvDeltaCutoffBasedDiffCluster("Median diff", pdYRWMedianDiffLR,1,dvMedianDiffs,nvMedianHighDiffClusterPositions,nvMedianLowDiffClusterPositions,nvMedianHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
        cKMDeltaEnv=buildEnvDeltaCutoffBasedDiffCluster("Env diff", pdFiltedEnvDeltaUpper,3,dvFiltedEnvDelta,nvEnvHighDiffClusterPositions,nvEnvLowDiffClusterPositions,nvEnvHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
        
//        if(cKMDeltaMean.getDataDv().size()>2) cKM_MeanDiff_ExtremeExclusion=new OneDKMeans_ExtremExcluded("Mean diff E", cKMDeltaMean.getDataDv(), 2, 1);
//        if(cKMDeltaMedian.getDataDv().size()>2) cKM_MedianDiff_ExtremeExclusion=new OneDKMeans_ExtremExcluded("Median diff E", cKMDeltaMedian.getDataDv(), 2, 1);
        if(cKMDeltaMean.getDataDv().size()>2) 
            cKMDeltaMeanE=CommonStatisticsMethods.OneDKMeans_LargestAttenuated("Mean diff E", cKMDeltaMean.getDataDv(), 2);     
        else
            cKMDeltaMeanE=cKMDeltaMean;
        
        len=nvEnvDeltaAbsLx.size();
        if(nvEnvDeltaAbsLx.get(len-1)>=pdFiltedEnvDeltaUpper.length){
            nvEnvDeltaAbsLx.remove(len-1);
            len--;
        }
    }
    
    void buildAndMarkRegressionClusters(){//this methods adjust the runing window mean and median delta according to the transition candidate suggusted by 
        dvRegressionDiffs=new ArrayList();
        nvRegressionHighDiffClusterPositions=new ArrayList();
        nvRegressionHighDiffClusterPositionsE=new ArrayList();
        nvRegressionLowDiffClusterPositions=new ArrayList();
        cKMDeltaRegression=buildEnvDeltaCutoffBasedDiffCluster("Regression diff", pdDeltaOLR,2,dvRegressionDiffs,nvRegressionHighDiffClusterPositions,nvRegressionLowDiffClusterPositions,nvRegressionHighDiffClusterPositionsE,pnHighDiffClusterPosition,pnLowDiffClusterPosition,pnHighDiffClusterPositionE);
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
        
        LineFeatureExtracter.SignalTransitionStatisticsTests test;
        
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
//        displayTransitionInfo("Mean Diff. in Low Clusters",nvMeanLowDiffClusterPositions);
        
        displayTransitionInfo("Median Diff. in High Clusters",nvMedianHighDiffClusterPositions);
        displayTransitionInfo("Median Diff. in High Clusters_E",nvMedianHighDiffClusterPositionsE);
//        displayTransitionInfo("Median Diff. in Low Clusters",nvMedianLowDiffClusterPositions);
        
        displayTransitionInfo("Regression Diff. in High Clusters",nvRegressionHighDiffClusterPositions);
        displayTransitionInfo("Regression Diff. in High Clusters_E",nvRegressionHighDiffClusterPositionsE);
//        displayTransitionInfo("Regression Diff. in Low Clusters",nvRegressionLowDiffClusterPositions);
        
        int i,lenD=pdFiltedEnvDeltaUpper.length;
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
        st+="  meanDiff"+" = "+PrintAssist.ToString(pdYRWMeanDiffLR[position], 12, 3);
        st+="  medianDiff"+" = "+PrintAssist.ToString(pdYRWMedianDiffLR[position], 12, 3);
        if(position<pdFiltedEnvDeltaUpper.length) {
            st+="  EnvDiff"+" = "+PrintAssist.ToString(pdFiltedEnvDeltaUpper[position], 12, 3);
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
        pdvProbeContactingPointsDevX=new ArrayList();
        pdvProbeContactingPointsSDX=new ArrayList();
        pdvProbeContactingPointsDevY=new ArrayList();
        pdvProbeContactingPointsSDY=new ArrayList();
        
        pdvProbeContactingPointsX=new ArrayList();
        pdvProbeContactingPointsY=new ArrayList();
        pdvProbeTrailDelta=new ArrayList();
        nvvBallContactingExtrema=new ArrayList();
        
        pnProbeGrade=new int[nDataSize];
        pdOptimalProbeTrail=new double[nDataSize];
        pbTopscaleAnchor=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbTopscaleAnchor, false);
        pnvTrailRisingIntervals=new ArrayList();
        pbvSemiballContacting=new ArrayList();
        
        boolean[] pbSemiballContacting;
        
        int[] pnIntervals;
        
        ArrayList<Integer> nvCP0,nvCP;
        
        for(i=0;i<len;i++){
            dRx=nvBallRollingWs.get(i);
            double[] pdYDespiked;
            int nMaxMultiplicity=2;
            double dPValue=0.001;
            SpikeHandler detector=new SpikeHandler(pdX,pdY,nMaxMultiplicity,dPValue);
            pdYDespiked=detector.removeSpikes_Progressive(nMaxMultiplicity);
            pdYDespiked=pdY;
            ProbingBall pb=new ProbingBall(pdX,pdYDespiked,dRx,-1,nRanking);  
            pdSemiballTrailDownward=pb.getProbeTrail(ProbingBall.Downward);        
            pdvProbeTrailDownward.add(pdSemiballTrailDownward);
            pnIntervals=CommonStatisticsMethods.calRisingIntervals(pdSemiballTrailDownward, nMaxRisingInterval);
            pnvTrailRisingIntervals.add(pnIntervals);
            
            pdBallTrailUpward=pb.getProbeTrail(ProbingBall.Upward); 

            nvProbeContactingPosition=pb.getProbeContactingPositions(ProbingBall.Downward);
            pbSemiballContacting=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbSemiballContacting, nvProbeContactingPosition, true);
            pbvSemiballContacting.add(pbSemiballContacting);
            
            nvvProbeContactingPositions.add(nvProbeContactingPosition);
            
            if(i==0){
                ArrayList<Integer> nv0=pb.getBallContactingPositions(ProbingBall.Downward),nv=new ArrayList();
                CommonStatisticsMethods.setElements(pbTopscaleAnchor, nvProbeContactingPosition,true);
                int iit;
                for(int k=0;k<nv0.size();k++){
                    iit=nv0.get(k);
                    if(CommonStatisticsMethods.isLocalExtrema(pdY, iit, 1)) nv.add(iit);                                                
                }
                nvvBallContactingExtrema.add(nv);
            }
            
            pdYT=CommonStatisticsMethods.getDeltaArray(pdSemiballTrailDownward, pnIntervals, nMaxRisingInterval);
            pdvProbeTrailDelta.add(pdYT);            
            
            len1=nvProbeContactingPosition.size();
            pdXT=new double[len1];
            pdYT=new double[len1];
            for(j=0;j<len1;j++){
                index=nvProbeContactingPosition.get(j);
                pdXT[j]=pdX[index];
                pdYT[j]=pdY[index];
            }
            pdvProbeContactingPointsX.add(pdXT);
            pdvProbeContactingPointsY.add(pdYT);
            
            pdYT=CommonStatisticsMethods.getDevArray(pdX, pdSemiballTrailDownward, pnIntervals);
           
            pdvProbeContactingPointsDevX.add(pdX);
            pdvProbeContactingPointsDevY.add(pdYT);
            
            pdYT=SequenceSegmenter_ProgressiveScales.getRWSD_Cumulative(pdYT, 20,0.01).get(1);
            
            pdvProbeContactingPointsSDX.add(pdX);
            pdvProbeContactingPointsSDY.add(pdYT);
            
            if(i==0){
                CommonStatisticsMethods.copyArray(pdSemiballTrailDownward, pdOptimalProbeTrail);
                CommonStatisticsMethods.setElements(pnProbeGrade, 0);
            }else{
                nvCP0=nvvProbeContactingPositions.get(i-1);
                nvCP=nvvProbeContactingPositions.get(i);
                len0=nvCP0.size();
                for(it=0;it<len0;it++){
                    position0=nvCP0.get(it);
                }
            }                        
        }
        calBallContactingPointsDevPValues();
        if(nDataSize>30){
            buildMiltiscaleBallTrails();   
            pdSemiballTrailDownward=pdvProbeTrailDownward.get(nNumBallScales);//getting the multiscale trail
            nvProbeContactingPosition=nvvProbeContactingPositions.get(nNumBallScales);
            pbMultipleScaleAnchorLx=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbMultipleScaleAnchorLx, false);
            ArrayList<Integer> ln=new ArrayList(),lx=new ArrayList();
            CommonMethods.LocalExtrema(pdY, ln, lx);
            CommonStatisticsMethods.setElements_AND(pbMultipleScaleAnchorLx, nvvProbeContactingPositions.get(nNumBallScales), lx, true);
            
            pbSemiballContacting=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbSemiballContacting, nvProbeContactingPosition, true);
            pbvSemiballContacting.add(pbSemiballContacting);
            
        }else{
            pdSemiballTrailDownward=pdvProbeTrailDownward.get(nNumBallScales-1);
            nvProbeContactingPosition=nvvProbeContactingPositions.get(nNumBallScales-1);
            pbMultipleScaleAnchorLx=new boolean[nDataSize];
            CommonStatisticsMethods.setElements(pbMultipleScaleAnchorLx, false);
            ArrayList<Integer> ln=new ArrayList(),lx=new ArrayList();
            CommonMethods.LocalExtrema(pdY, ln, lx);
            CommonStatisticsMethods.setElements_AND(pbMultipleScaleAnchorLx, nvvProbeContactingPositions.get(nNumBallScales-1), lx, true);
        }
        refineMultipleScaleTrails();
    }
    void calBallContactingPointsDevPValues(){
        int i;
        ArrayList<Integer> positions=nvvBallContactingExtrema.get(0);
        ArrayList<double[]> line=CommonStatisticsMethods.getDevArray(pdX, pdY, positions);
        double[] pdt=CommonStatisticsMethods.getPValues(line.get(1), 0, positions.size()-1, 1, 0.001);
        pdBallContactingPointsDevPValues=new double[nDataSize];
        CommonStatisticsMethods.setElements(pdBallContactingPointsDevPValues,1.1);
        for(i=0;i<positions.size();i++){
            pdBallContactingPointsDevPValues[positions.get(i)]=pdt[i];
        }
    }
    int buildMiltiscaleBallTrails(){
        int i,j,nScale,len;
        double[] pdY,pdAbs,pdYT;
        ArrayList<double[]> pdvD=new ArrayList(),pdvH=new ArrayList();
        ArrayList<ArrayList<intRange>> cvvAdjustedSegmenters;
        nvvAnchoredBallTrailDeltaLx=new ArrayList();
        ArrayList<Integer> nvAnchors,nvLx=new ArrayList(),nvLn=new ArrayList(),nvSP=new ArrayList(),nvAnchoredDeltaLx=new ArrayList();
        cvvBallTrailSegmenters=new ArrayList();
        boolean[] pbAnchoredAbsLx=new boolean[nDataSize];
        boolean[] pbAnchored=new boolean[nDataSize];
        boolean[] pbSelectedDelta;
        boolean[] pbSelectedRisingNeighbors;
        int[] pnRisingIntervals;
        ArrayList<IntPair[]> pcvRisingNeighbors=new ArrayList();
        
        for(nScale=0;nScale<nvBallRollingWs.size();nScale++){
            
            nvAnchors=nvvProbeContactingPositions.get(nScale);
//            nvAnchoredDeltaLx=CommonStatisticsMethods.getAbsMaximaPositions(pdY, nvAnchors);
//            CommonStatisticsMethods.setElements(pbAnchoredAbsLx, false);
            CommonStatisticsMethods.setElements(pbAnchored, false);
//            CommonStatisticsMethods.setElements(pbAnchoredAbsLx, nvAnchoredDeltaLx, true);
            CommonStatisticsMethods.setElements(pbAnchored, nvAnchors, true);            
//            nvvAnchoredBallTrailDeltaLx.add(nvAnchoredDeltaLx);
            
            pnRisingIntervals=pnvTrailRisingIntervals.get(nScale);
            pcvRisingNeighbors.add(CommonStatisticsMethods.getRisingNeighbors(pnRisingIntervals, nMaxRisingInterval,nDataSize));
            
            pbSelectedDelta=CommonStatisticsMethods.getDeltaSelection_OR(pbAnchored, pnRisingIntervals);
            pbSelectedRisingNeighbors=CommonStatisticsMethods.getSelectedRisingNeighbors(pbAnchored, pnRisingIntervals);
            
            pdY=pdvProbeTrailDelta.get(nScale);
            pdYT=new double[nDataSize];
            CommonStatisticsMethods.setElements(pdYT, 0);
//            CommonStatisticsMethods.copyArray(pdY, pdYT, nvAnchoredDeltaLx);
//            CommonStatisticsMethods.copyArray(pdY, pdYT, pbAnchored);
            CommonStatisticsMethods.copyArray(pdY, pdYT, pbSelectedDelta);
            pdvD.add(CommonStatisticsMethods.getAbs(pdYT));
           
            pdY=pdvProbeContactingPointsDevY.get(nScale);
//            pdAbs=CommonStatisticsMethods.getAbs(pdY);
//            CommonMethods.LocalExtrema(pdAbs, nvLn, nvLx);
//            nvSP=CommonStatisticsMethods.getAbsMaximaSegmenter(pdvProbeTrailDelta.get(nScale), nvLx);
/*            
            len=nvSP.size();
            for(i=len-1;i>=0;i--){
                if(!pbAnchoredAbsLx[i]) {
                    nvSP.remove(i);
                    nvLx.remove(i);
                }
            }
*/          
            pdYT=new double[nDataSize];
            CommonStatisticsMethods.setElements(pdYT, 0);
            CommonStatisticsMethods.copyArray(pdY,pdYT,pbSelectedRisingNeighbors);
            pdvH.add(CommonStatisticsMethods.getAbs(pdYT));
        }
        SequenceSegmenter_ProgressiveScales segmenterH=new SequenceSegmenter_ProgressiveScales(pdvH,pcvRisingNeighbors,SequenceSegmenter_ProgressiveScales.isolated,dvPSegmentation,null,SequenceSegmenter_ProgressiveScales.larger);    
        SequenceSegmenter_ProgressiveScales segmenterD=new SequenceSegmenter_ProgressiveScales(pdvD,pcvRisingNeighbors,SequenceSegmenter_ProgressiveScales.right,dvPSegmentation,dvPConsolidation,SequenceSegmenter_ProgressiveScales.larger); 
        pdvP_TrailDelta=segmenterD.getPValues();
        pdvP_TrailHeight=segmenterH.getPValues();
        cvvAdjustedSegmenters=new ArrayList();
        ArrayList<intRange> cvSegmenters,cvAdjustedSegmenters;
        for(nScale=0;nScale<nvBallRollingWs.size();nScale++){
            pdY=pdvProbeTrailDelta.get(nScale);
            cvSegmenters=segmenterH.cvvSegmenters.get(nScale);
            cvAdjustedSegmenters=CommonStatisticsMethods.getAbsMaximaSegmenter(pdY, cvSegmenters);
            cvvAdjustedSegmenters.add(cvAdjustedSegmenters);
        }        
        
        segmenterD.combineSegmenters_AND(segmenterH.cvvSegmenters);
        segmenterD.buildAnchoredSegments(pdvProbeTrailDownward,pnvTrailRisingIntervals,nvvProbeContactingPositions);
        pdvProbeTrailDownward.add(segmenterD.buildMultiscaleTrail_Downward(pdvProbeTrailDownward));
        pnvTrailRisingIntervals.add(CommonStatisticsMethods.calRisingIntervals(pdvProbeTrailDownward.get(nNumBallScales), nMaxRisingInterval));
        nvvProbeContactingPositions.add(segmenterD.getMultiscaleAnchors());
        cvvBallTrailSegmenters=segmenterD.getSegmenters();
        cvvBallTrailSegmenters.add(cvvBallTrailSegmenters.get(cvvBallTrailSegmenters.size()-1));
        return 1;
    }
    void refineMultipleScaleTrails(){   
        int num=pdvProbeTrailDownward.size(),i,p,len,index;
        int[] pnRisingIntervals=pnvTrailRisingIntervals.get(num-1);
        int[] pnFineScale=pnvTrailRisingIntervals.get(nNumBallScales-1),pnCoarsScale=pnvTrailRisingIntervals.get(0);
        double[] pdDelta=CommonStatisticsMethods.getDeltaArray(pdvProbeTrailDownward.get(num-1), pnRisingIntervals, nMaxRisingInterval);
        double[] pdDeltaFineScale=CommonStatisticsMethods.getDeltaArray(pdvProbeTrailDownward.get(nNumBallScales-1),pnFineScale , nMaxRisingInterval);
        double[] pdDeltaCoarsScale=CommonStatisticsMethods.getDeltaArray(pdvProbeTrailDownward.get(0),pnCoarsScale , nMaxRisingInterval);
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
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbvSemiballContacting.get(num-1), new intRange(0,nDataSize-1), nvMultiscaleContactingPositions);
        nvvProbeContactingPositions.set(num-1, nvMultiscaleContactingPositions);
    }
    int fixMultiscaleTrail(double[]pdDeltaCoarsScale,double[] pdDeltaMultiScale,double[] pdDeltaFineScale, int[] pnCoarsScale, int[] pnRisingIntervals,int[] pnFineScale, int p0){
        int pl=p0+CommonStatisticsMethods.getRisingInterval(pnFineScale, p0, -1),nSil=getScaleIndex(p0),pr=p0+CommonStatisticsMethods.getRisingInterval(pnFineScale, p0, 1),nSir=getScaleIndex(pr),
                left=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor, bFeatureLinesOnly, p0, -1),
                right=CommonStatisticsMethods.getFirstSelectedPosition(pbTopscaleAnchor, true, pr, 1);
        double diffRatioCutoff=2.,similarRatioCutoff=1.3,ratio,delta0,delta1,sign=pdDeltaMultiScale[p0],delta;
        if(left<0) left=0;
        if(right<0) right=nDataSize-1;
        int nMultiscaleIndex=pdvProbeTrailDownward.size()-1,nFinescaleIndex=nNumBallScales-1;
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
                bBreak=false;
                for(p=pr;p<=right;p++){
                    delta=pdDeltaFineScale[p];
                    if(delta>0) continue;
                    if(Math.abs(delta0/delta)<similarRatioCutoff){
                        bBreak=true;
                        break;
                    }
                }
                if(!bBreak)replaceMultiscaleSection(nMultiscaleIndex,0,p0,right);
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
                bBreak=false;
                for(p=left;p<=pl;p++){
                    delta=pdDeltaFineScale[p];
                    if(delta<0) continue;
                    if(Math.abs(delta0/delta)<similarRatioCutoff){
                        bBreak=true;
                        break;
                    }
                }
                if(!bBreak)replaceMultiscaleSection(nMultiscaleIndex,0,left,p0);
            }            
        }
        return 1;
    }
    int replaceMultiscaleSection(int nMultiscaleIndex, int nScaleIndex, int pI, int pF){
        int p;
        double[] pdTrail=pdvProbeTrailDownward.get(nScaleIndex),pdMultiscaleTrail=pdvProbeTrailDownward.get(nMultiscaleIndex);
        boolean[] pbSemiballContacting=pbvSemiballContacting.get(nScaleIndex),pbMultiscaleSemiballContacting=pbvSemiballContacting.get(nMultiscaleIndex);
        for(p=pI;p<=pF;p++){
            pdMultiscaleTrail[p]=pdTrail[p];
            pbMultiscaleSemiballContacting[p]=pbSemiballContacting[p];
        }
        return 1;
    }
    int getScaleIndex(int p){
        int num=pdvProbeTrailDownward.size();
        double dt=pdvProbeTrailDownward.get(num-1)[p];
        for(int i=0;i<nNumBallScales;i++){
            if(dt==pdvProbeTrailDownward.get(i)[p]) return i;
        }
        return -1;//should not be happening
    }
    public void buildFiltedEnvlines(){
        if(pdvFiltedData_EdgePreserving==null) FiltData_EdgePreserving();
        double dRx=10;
        int nRanking=0;                          
        ProbingBall pb=new ProbingBall(pdX,pdvFiltedData_EdgePreserving.get(pdvFiltedData_EdgePreserving.size()-1),dRx,-1,nRanking);  
        pdFiltedEnvUpper=pb.getProbeTrail(ProbingBall.Downward);        
        pdFiltedEnvLower=pb.getProbeTrail(ProbingBall.Upward);        
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
                pcTransitionTests[position]=new LineFeatureExtracter.SignalTransitionStatisticsTests(position);
        }
    }
    void markTransitionEnvPoints(OneDKMeans cKM, ArrayList<Integer> nvPositions,boolean[] pbTransition){
        int i,K=cKM.K,h=K-1,position,positionp,len=cKM.m_nSize;
        int[] pnClusterIndexes=cKM.getClusterIndexes();
        int num=0;
        for(i=0;i<len;i++){
            if(pnClusterIndexes[i]!=h) continue;
            position=nvPositions.get(num);
            positionp=CommonStatisticsMethods.getMaxAbsPosition(pdFiltedEnvDeltaUpper, position);
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
        pdSigDiff=CommonStatisticsMethods.copyArray(pdYRWMeanDiffLR);
        ArrayList<Integer> confirmedPts=new ArrayList();
        int i,len=nvInitTransitions.size();
        int[] pnConfirmed=new int[len];
        int itL,itR,itLL,itRR,pt1,pt2;
        CommonStatisticsMethods.setElements(pnConfirmed, 0);
        int num=CommonStatisticsMethods.getNumOfZero(pnConfirmed),index,it0,it,it1,ll,lr,rl,rr,rl1,rr1;
        LineFeatureExtracter.SignalTransitionStatisticsTests cTest;
        intRange ir;
        int confirmingIndex,confirmingPosition;
        ArrayList<Integer> extrema;
        int sign;
        
        boolean[] pbBallContactingMaxima=new boolean[nDataSize];
        CommonStatisticsMethods.setElements(pbBallContactingMaxima, false);
        CommonStatisticsMethods.setElements(pbBallContactingMaxima, nvvBallContactingExtrema.get(0),true);
        int position,left,right,confirmingIndex0;
        double cutoff=2.,dt,dt1,dt2,ratio,ratioCutoff=1.7;
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
                cTest=new LineFeatureExtracter.SignalTransitionStatisticsTests(lr,ll,lr,rl,rr);
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
                cTest=new LineFeatureExtracter.SignalTransitionStatisticsTests(lr,ll,lr,rl,rr);
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
                    cTest=new LineFeatureExtracter.SignalTransitionStatisticsTests(lr,ll,lr,rl,rr);
                    confirmingIndex=itL;
                    confirmingPosition=lr;
                    
//                    pnConfirmed[itL]=1;
//                    if(cTest.isInsignificantTransition()) pnConfirmed[itL]=-1;
//                    if(isSmooth(lr)) pnConfirmed[itL]=-1;
                 } else {
                    cTest=new LineFeatureExtracter.SignalTransitionStatisticsTests(rr,rl,rr,rl1,rr1);
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
            ratio=Math.abs(pdFiltedEnvDeltaUpper[confirmingPosition]/pdDeltaBallTrailDownward[confirmingPosition]);
            if(ratio>ratioCutoff||ratio<1./ratioCutoff) pnConfirmed[confirmingIndex]=-1; 
            if(cTest.isInsignificantTransition()) pnConfirmed[confirmingIndex]=-1;
            if(isSmooth(confirmingPosition)) pnConfirmed[confirmingIndex]=-1;  
//            if(confirmingPosition==0) pnConfirmed[confirmingIndex]=-1;  //12n13
            if(pdYRWMeanDiffLR[confirmingPosition]<0&&confirmingPosition<5) pnConfirmed[confirmingIndex]=-1;  //12n13
            if(pnConfirmed[confirmingIndex]==1){
                left=CommonStatisticsMethods.getFirstSelectedPosition(pbBallContactingMaxima,true, confirmingPosition, -1);
                if(left<0) left=0;
                right=CommonStatisticsMethods.getFirstSelectedPosition(pbBallContactingMaxima,true, confirmingPosition+CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals, confirmingPosition, 1), 1);
                if(right<0) right=nDataSize-1;
                if(right<m_pnRisingIntervals.length) 
                    right+=CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals, right, -1);
                else
                    right=m_pnRisingIntervals.length-1;
                dt=pdDeltaBallTrailDownward[confirmingPosition];
                for(position=left;position<=right;position++){
                    if(position==confirmingPosition) continue;
                    dt1=pdDeltaBallTrailDownward[position];
                    dt1=dt/dt1;
                    if(dt1>-cutoff&&dt1<0){
                        sign=1;
                        if(dt<0) sign=-1;                        
                        extrema=CommonStatisticsMethods.getExtremaPositions(pdFiltedEnvDeltaUpper, confirmingPosition, sign);
                        pt1=extrema.get(0);
                        dt1=pdFiltedEnvDeltaUpper[pt1];//the confirming position should not be a minimum, so extrema should have one element, 12n13
                        if(position==1){
                            position=position;
                        }
                        extrema=CommonStatisticsMethods.getExtremaPositions(pdFiltedEnvDeltaUpper, position, -sign);
                        pt2=extrema.get(0);
                        if(pt2<0) {
                            pt2=pt2;
                        }
                        dt2=pdFiltedEnvDeltaUpper[pt2];
                        if(dt1/dt2>-cutoff){
                            pnConfirmed[confirmingIndex]=-1;
                            break;
                        }                       
                    }
                }
            }
            if(pnConfirmed[confirmingIndex]==-1){
                left=confirmingPosition+CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals, confirmingPosition, -1);
                right=confirmingPosition+CommonStatisticsMethods.getRisingInterval(m_pnRisingIntervals, confirmingPosition, 1);
                for(position=left;position<=right;position++){
                    pdSigDiff[position]=0;
                }
            }
            num=CommonStatisticsMethods.getNumOfZero(pnConfirmed);                        
        }
        for(i=0;i<len;i++){
            if(pnConfirmed[i]==1) confirmedPts.add(nvInitTransitions.get(i));
        }
        if(confirmedPts.size()>0)
            if(confirmedPts.get(0) ==0&&pdDeltaY[0]<0) confirmedPts.remove(0);
        return confirmedPts;
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
        if(cKMDeltaMeanE.getGapIndexes().get(0)<1) return false;
        if(!CommonStatisticsMethods.equalContents(getDetectedTransitionPointsE(), getHighClusterPointsE())) return false;
        if(nvMeanHighDiffClusterPositionsE.size()!=getDetectedTransitionPointsE().size()||nvRegressionHighDiffClusterPositionsE.size()!=getDetectedTransitionPointsE().size()) return false; 
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
        names.add("SemiBall Contacking Point Dev");
        names.add("Ball Contacking Point Dev");
        names.add("Ball Contacking Point Dev PValues");
        names.add("SemiBall Contacking Point SD");
        names.add("SemiBalltrailDownward Delta");
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
            line.add(pdSemiballTrailDownward);
        }
        if(name.contentEquals("SemiBallTrail Downward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeTrailDownward.size()) return line;
            line.add(pdX);
            line.add(pdvProbeTrailDownward.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeTrailDownward.get(index), cvvBallTrailSegmenters.get(index));
            int i;
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("FiltedEnv Downward")){
            line.add(pdX);
            line.add(pdFiltedEnvUpper);
        }
        if(name.contentEquals("SemiBall Contacking Downward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvProbeContactingPositions.size()) return line;
            ArrayList<Integer> positions=nvvProbeContactingPositions.get(index);
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
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdXT, pdYT, pdX,cvvBallTrailSegmenters.get(index));
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        
        if(name.contentEquals("Ball Contacking Downward")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvBallContactingExtrema.size()) return line;
            ArrayList<Integer> positions=nvvBallContactingExtrema.get(index);
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
/*            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdXT, pdYT, pdX,cvvBallTrailSegmenters.get(index));
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }*/
        }
        
        if(name.contentEquals("SemiBall Contacking Point Dev")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeContactingPointsDevY.size()) return line;
            line.add(pdX);
            line.add(pdvProbeContactingPointsDevY.get(index));
            if(!showSegments) return line;
            int i;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeContactingPointsDevY.get(index), cvvBallTrailSegmenters.get(index));
            for(i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        
        if(name.contentEquals("Ball Contacking Point Dev")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=nvvBallContactingExtrema.size()) return line;
            line=CommonStatisticsMethods.getDevArray(pdX, pdY, nvvBallContactingExtrema.get(index));
        }
        
        if(name.contentEquals("Ball Contacking Point Dev PValues")){
            line.add(pdX);
            line.add(pdBallContactingPointsDevPValues);
        }
        
        if(name.contentEquals("SemiBall Contacking Point SD")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeContactingPointsSDY.size()) return line;
            line.add(pdX);
            line.add(pdvProbeContactingPointsSDY.get(index));
        }   
        if(name.contentEquals("SemiBalltrailDownward Delta")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvProbeTrailDelta.size()) return line;
            line.add(pdX);
            line.add(pdvProbeTrailDelta.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvProbeTrailDelta.get(index), pdX,cvvBallTrailSegmenters.get(index));
            for(int i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("P Values-Trail Delta")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvP_TrailDelta.size()) return line;
            line.add(pdX);
            line.add(pdvP_TrailDelta.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvP_TrailDelta.get(index), pdX,cvvBallTrailSegmenters.get(index));
            for(int i=0;i<pdvSegmentedLines.size();i++){
                line.add(pdvSegmentedLines.get(i));
            }
        }
        if(name.contentEquals("P Values-Trail Height")){
            index=CommonStatisticsMethods.getFirstStringPosition(svBallRollingWs,sWs);
            if(index<0||index>=pdvP_TrailHeight.size()) return line;
            line.add(pdX);
            line.add(pdvP_TrailHeight.get(index));
            if(!showSegments) return line;
            ArrayList<double[]> pdvSegmentedLines=CommonStatisticsMethods.getSegmentLines(pdX, pdvP_TrailHeight.get(index), pdX, cvvBallTrailSegmenters.get(index));
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
        return cKMDeltaMean.getClusterMeanSems();
    }
    public PolynomialLineFittingSegmentNode[] getSmoothSegments(){
        return m_pcSmoothSegments;
    }
    public intRange getValidSmoothRange(PolynomialLineFittingSegmentNode seg){
        
        if(seg.getStart(0,1)==0&&seg.getStart(0,-1)==25){
            seg.nEnd=seg.nEnd;
        }
        int left=Math.min(seg.getStart(pbSemiballContactingConvex,2, 1),seg.getStart(pbSemiballContactingLx,1, 1)),right=Math.max(seg.getStart(pbSemiballContactingConvex,2, -1),seg.getStart(pbSemiballContactingLx,1, -1)),lenExt=1,start=seg.getStart(0, 1),end=seg.getStart(0, -1);
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
        if(position==67){
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
        int start=CommonStatisticsMethods.getFirstSelectedPosition(pbSemiballContactingLx, true, position, -1);
        int end;
        
        if(position<m_pnRisingIntervals.length)
            end=CommonStatisticsMethods.getFirstSelectedPosition(pbSemiballContactingLx, true, position+m_pnRisingIntervals[position], 1);
        else
            end=smoothSeg.nEnd;
        
        CommonStatisticsMethods.getSortedIndependetDelta(pdX,pdSemiballTrailDownward,start,end, (double) nMaxRisingInterval,nMaxNumDelta,dvDeltas,nvPositions);
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
        return pdSemiballTrailDownward;
    }
    public double[] getSigDiff(){
        return pdSigDiff;
    }
}
