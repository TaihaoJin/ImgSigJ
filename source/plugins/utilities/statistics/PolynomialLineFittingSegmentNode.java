/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import ij.gui.PlotWindow;
import java.awt.Color;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CommonStatisticsMethods;
import java.util.ArrayList;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.MeanSem1;
import utilities.statistics.MeanSem0;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.MeanSem1;
import utilities.QuickSort;
import utilities.statistics.PolynomialRegression;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.statistics.LineSegmentRegressionEvaluater;
import java.util.ArrayList;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import utilities.CommonGuiMethods;
import utilities.CommonMethods;
import utilities.io.PrintAssist;
import utilities.Non_LinearFitting.Constrains.ParValidityChecker;

/**
 *
 * @author Taihao
 */
public class PolynomialLineFittingSegmentNode {
    public double[] pdX,pdY,pdSD,pdSDDev,pdTiltingSig,pdPWDevSig,pdSidenessSig;
    public boolean[] pbSelected,pbValidDev;
    PolynomialRegression cPr;
    public int nStart, nEnd,nOrder,nNumCrossing;
    public double dPChiSQ,dPPWDev,dPTilting,dPSideness,dOutliarRatio,dPEnding,dPStarting,dSD;
    public double dSigChiSQ,dSigTilting,dSigTiltingMW,dSigPWDev,dSigSideness,sse,dChiSQ,dChiSQDev,dSigStarting, dSigEnding,dSigDevChiSQ;
    public int nWsSD, nWsTilting, nWsPWDev;
    int[] m_pnLxPositions,m_pnLnPositions;
    int nMaxOutliars;
    boolean validDev;
    boolean bExcludePWOutliars,bValidPars;
    int nMaxDevPosition,nMaxRisingInterval;
    ArrayList<intRange> cvOutliarRegions;
    public PolynomialLineFittingSegmentNode(){
        
    }
    public PolynomialLineFittingSegmentNode(double[] pdX, double[] pdY, double[] pdTiltingSig, double[] pdSidenessSig, double[] pdPWDevSig, double[] pdSD, boolean[] pbSelected, boolean[] pbValidDev, int[] pnLnPositions, int[] pnLxPositions, int start, int end, int order, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ, double dPTilting, double dPSideness, double dPPWDev, double dOutliarRatio){
//        double dTerminals=0.01;
//        int nModes=1;
        this(pdX, pdY, pdTiltingSig, pdSidenessSig, pdPWDevSig, pdSD, pbSelected, pbValidDev, pnLnPositions, pnLxPositions, start, end, order, 1, nWsSD, nWsTilting, nWsPWDev,dPChiSQ, dPTilting, dPSideness, dPPWDev, 0.01,dOutliarRatio);
    }
    public PolynomialLineFittingSegmentNode(double[] pdX, double[] pdY, double[] pdTiltingSig, double[] pdSidenessSig, double[] pdPWDevSig, double[] pdSD, boolean[] pbSelected, boolean[] pbValidDev, int[] pnLnPositions, int[] pnLxPositions, int start, int end, int order, int nModes, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ, double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
        this(pdX,pdY, pdTiltingSig, pdSidenessSig, pdPWDevSig, pdSD, null, pbSelected, pbValidDev, pnLnPositions, pnLxPositions, start, end, order, nModes, nWsSD, nWsTilting, nWsPWDev, dPChiSQ, dPTilting, dPSideness, dPPWDev, dPTerminals, dOutliarRatio);
    }
    public PolynomialLineFittingSegmentNode(double[] pdX, double[] pdY, double[] pdTiltingSig, double[] pdSidenessSig, double[] pdPWDevSig, double[] pdSD, double[] pdSDDev, boolean[] pbSelected, boolean[] pbValidDev, int[] pnLnPositions, int[] pnLxPositions, int start, int end, int order, int nModes, int nWsSD, int nWsTilting, int nWsPWDev,double dPChiSQ, double dPTilting, double dPSideness, double dPPWDev,double dPTerminals, double dOutliarRatio){
        this.pdX=pdX;
        this.pdY=pdY;
        nMaxRisingInterval=0;
        bValidPars=true;
        if(start==505&&end==522){
            start=start;
        }
        this.pdSDDev=pdSDDev;
        pbValidDev=CommonStatisticsMethods.getEmptyBooleanArray(pbValidDev, pdX.length);
        CommonStatisticsMethods.setElements(pbValidDev, start, end, true);
        this.pbSelected=pbSelected;
        this.nWsSD=nWsSD;
        m_pnLnPositions=pnLnPositions;
        m_pnLxPositions=pnLxPositions;
        if(start>end){
            int nT=start;
            start=end;
            end=nT;
        }
        this.nWsTilting=nWsTilting;
         nStart=start;
        nEnd=end;
        this.nWsPWDev=nWsPWDev;
        this.dPChiSQ=dPChiSQ;
        this.dPPWDev=dPPWDev;
        this.dPTilting=dPTilting;
        this.dPSideness=dPSideness;
        dSigChiSQ=-1;
        dSigTilting=-1;
        dSigPWDev=-1;
        this.dOutliarRatio=dOutliarRatio;
        nOrder=order;
        bExcludePWOutliars=true;
        this.pdSD=pdSD;
        if(pdSD==null){
            pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdY, 2, 20);
        }
        dSD=CommonStatisticsMethods.getMean(pdSD, pbSelected, start, end, 1);
        cPr=new PolynomialRegression(pdX,pdY,pdSD,new DoubleRange(pdX[nStart],pdX[nEnd]), nOrder,nModes,this.pbSelected,dOutliarRatio);
        nMaxOutliars=(int) (cPr.nDataSize*dOutliarRatio+0.5);
        this.pdPWDevSig=pdPWDevSig;
        this.pdTiltingSig=pdTiltingSig;
        this.pdSidenessSig=pdSidenessSig;
        dPStarting=dPTerminals;
        dPEnding=dPTerminals;
        
        pdPWDevSig=CommonStatisticsMethods.getEmptyDoubleArray(pdPWDevSig, pdX.length);
        CommonStatisticsMethods.setElements(pdPWDevSig, nStart,nEnd,1.1);
        CommonStatisticsMethods.setElements(pbValidDev,nStart,nEnd, true);
        calDeviations();
        int len,index,j;
        cvOutliarRegions=new ArrayList();
        ArrayList<Integer> connectedRangeIndexes;
        intRange outliarRegion=new intRange(),ir;
        ArrayList<intRange> connectedOrOverlappedRanges=new ArrayList();
        if(nMaxOutliars>0){
            int nOutliar=0,i;
//            while(nOutliar<nMaxOutliars&&dSigTilting>dPTilting&&dSigPWDev<dPPWDev){
            while(nOutliar<nMaxOutliars&&dSigPWDev<dPPWDev){
                outliarRegion=new intRange();
                pickOutliars(outliarRegion,nStart,nEnd);//pick only one region at each iteration
                if(outliarRegion.emptyRange()) break;
                connectedRangeIndexes=CommonStatisticsMethods.getConnectedOrOverlappedIndexes(cvOutliarRegions,outliarRegion);
                if(!isValidOutliarRegion(outliarRegion)) break;

                len=connectedRangeIndexes.size();
                connectedOrOverlappedRanges.clear();
                for(j=0;j<len;j++){
                    index=connectedRangeIndexes.get(len-1-j);
                    connectedOrOverlappedRanges.add(cvOutliarRegions.get(index));
                }
                for(j=0;j<len;j++){
                    cvOutliarRegions.remove(connectedOrOverlappedRanges.get(j));
                }
                cvOutliarRegions.add(outliarRegion);

                nOutliar+=outliarRegion.getRange();
                for(index=outliarRegion.getMin();index<=outliarRegion.getMax();index++){
                    pbSelected[index]=false;
                    pbValidDev[index]=false;
                }
                
                cPr=new PolynomialRegression(pdX,pdY,pdSD,new DoubleRange(pdX[nStart],pdX[nEnd]), nOrder,1,this.pbSelected,dOutliarRatio);
                CommonStatisticsMethods.setElements(pdPWDevSig, nStart,nEnd,1.1);
                calDeviations();
            }
        }
        for(index=start;index<=end;index++){//here is to reverse the changes made in the selection in the middle of computation.
            if(!pbValidDev[index]){
                pbSelected[index]=true;
            }
        }
    }
    public ArrayList<Integer> getOutliars(){
        int i,j,len=cvOutliarRegions.size();
        ArrayList<Integer> nvOutliars=new ArrayList();
        intRange ir;
        for(i=0;i<len;i++){
            ir=cvOutliarRegions.get(i);
            for(j=ir.getMin();j<=ir.getMax();j++){
                nvOutliars.add(j);
            }
        }
        return nvOutliars;
    }
    boolean isValidOutliarRegion(intRange ir){
        boolean valid=true;
        int iI=ir.getMin(),iF=ir.getMax();
        if(getNumExtrema(iI,iF)>3) valid=false;
        ArrayList<Integer> crossingPoints=LineSegmentRegressionEvaluater.getCrossingPoints(cPr,pdX,pdY,pbSelected,iI,iF);
        if(crossingPoints.size()>0) valid=false;
        return valid;
    }
    int pickOutliars(intRange outliarRigion, int start, int end){//pick a connected region of outliars, only the region containing the largest deviation
        outliarRigion.resetRange();
        int i,in;
        double dPn,p;
        in=start;
        dPn=1.1;

        for(i=start;i<=end;i++){
            p=pdPWDevSig[i];
            if(p<dPPWDev&&pbSelected[i]) {
                 if(p<dPn){
                      in=i;
                      dPn=p;
                 }
            }
        }
        if(dPn>dPPWDev)//no outliar
            return -1;

        outliarRigion.expandRange(in);
        i=in-1;
        while(i>=start&&pdPWDevSig[i]<dPPWDev){
           outliarRigion.expandRange(i);
            i--;
        }
        i=in+1;
        while(i<=end&&pdPWDevSig[i]<dPPWDev){
            outliarRigion.expandRange(i);
            i++;
        }
        return 1;
    }
    public PolynomialRegression getRegressionNode(){
        return cPr;
    }
    public boolean isBetter(PolynomialLineFittingSegmentNode aNode){//return true if this is better than aNode
        if(aNode==null) return true;
        if(!aNode.isValidDev()){
            if(isValidDev()) return true;
        }else if(!isValidDev()){
            if(aNode.isValidDev()) return false;
        }
        if(!aNode.isValidNumExtrema()){
            if(isValidNumExtrema()) return true;
        }else if(!isValidNumExtrema()){
            if(aNode.isValidNumExtrema()) return false;
        }
        return getSignificance_ChiSquare()>aNode.getSignificance_ChiSquare();
    }
    public boolean isSmoother(PolynomialLineFittingSegmentNode aNode){//return true if this is better than aNode
        if(aNode==null) return true;
        if(!aNode.isValidLocalDeviations()||!aNode.bValidPars){
            if(isValidLocalDeviations()&&bValidPars) return true;
        }else if(!isValidLocalDeviations()||!bValidPars){
            if(aNode.isValidLocalDeviations()&&aNode.bValidPars) return false;
        }
        if(!aNode.isValidNumExtrema()){
            if(isValidNumExtrema()) return true;
        }else if(!isValidNumExtrema()){
            if(aNode.isValidNumExtrema()) return false;
        }
        return getSignificance_ChiSquare()>aNode.getSignificance_ChiSquare();
    }
    public boolean isSmoother(PolynomialLineFittingSegmentNode aNode, int position, double dRisingInterval){//return true if this is better than aNode
        if(aNode==null) return true;
        if(!aNode.isSmooth(position,dRisingInterval)){
            if(isSmooth(position,dRisingInterval)) return true;
        }else if(!isSmooth(position,dRisingInterval)){
            if(aNode.isSmooth(position,dRisingInterval)) return false;
        }
        //both are smooth segments
        return getSignificance_ChiSquare()>aNode.getSignificance_ChiSquare();
//        return getPWDevSig(position)>aNode.getPWDevSig(position);
    }
    public boolean isSmooth(int position, double dRisingInterval){
        if(position==14&&nStart==0&&nEnd==20){
            position=position;
        }
        if(!bValidPars) return false;
        if(!isValidLocalDeviations()||!bValidPars) return false;   
        double dPWDevSig=getPWDevSig(position);
        if(dPWDevSig<0.1) return false;
        if(dSigChiSQ>0.8) return true;
        if(position==nMaxDevPosition) return false;
        int next=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, position+1, 1);
        if(next==nMaxDevPosition) return false;
//        if(next<position+nRisingInterval) next=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, next+1, 1);
        if(pdX[next]-pdX[position]<dRisingInterval) next=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, next+1, 1);
        if(next==nMaxDevPosition) return false;
        return true;
    }
    public double getPWDevSig(int position){
        return 1.-GaussianDistribution.Phi(Math.abs(pdY[position]-predict(pdX[position])), 0, pdSD[position]);
    }
    public boolean isBetterStarting(PolynomialLineFittingSegmentNode aNode){//return true if this is better than aNode
        if(aNode==null) return true;
//        if(!aNode.isValidStarting()){
        
        if(!aNode.isValidLocalDeviations()){
            if(isValidLocalDeviations()) return true;
        }else if(!isValidLocalDeviations()){
            if(aNode.isValidLocalDeviations()) return false;
        }
        
        if(!(aNode.dSigStarting>0.1)){
            if(dSigStarting>0.1) return true;
        }else if(dSigStarting<0.1){
            if(aNode.dSigStarting>0.1) return false;
        }
        
        if(!aNode.isValidStarting()){
            if(isValidStarting()) return true;
        }else if(!isValidStarting()){
            if(aNode.isValidStarting()) return false;
        }
        
        if(!aNode.isValidNumExtrema()){
            if(isValidNumExtrema()) return true;
        }else if(!isValidNumExtrema()){
            if(aNode.isValidNumExtrema()) return false;
        }
        
        return getSignificance_ChiSquare()>aNode.getSignificance_ChiSquare();
    }
    public boolean isBetterEnding(PolynomialLineFittingSegmentNode aNode){//return true if this is better than aNode
        if(aNode==null) return true;
//        if(!aNode.isValidEnding()){
        if(!aNode.isValidLocalDeviations()){
            if(isValidLocalDeviations()) return true;
        }else if(!isValidLocalDeviations()){
            if(aNode.isValidLocalDeviations()) return false;
        }
        
        if(aNode.dSigEnding<0.1){
            if(dSigEnding>0.1) return true;
        }else if(dSigEnding<0.1){
            if(aNode.dSigEnding>0.1) return false;
        }
        
        if(!aNode.isValidEnding()){
            if(isValidEnding()) return true;
        }else if(!isValidEnding()){
            if(aNode.isValidEnding()) return false;
        }
        
        if(!aNode.isValidNumExtrema()){
            if(isValidNumExtrema()) return true;
        }else if(!isValidNumExtrema()){
            if(aNode.isValidNumExtrema()) return false;
        }
        
        return getSignificance_ChiSquare()>aNode.getSignificance_ChiSquare();
    }
    public boolean isBetter(PolynomialLineFittingSegmentNode aNode, double x, double y){//return true if this predict y better than aNode
        if(aNode==null) return true;
        return Math.abs(y-predict(x))<Math.abs(y-aNode.predict(x));
    }
    public double getSignificance_ChiSquare(){
        if(dSigChiSQ<0) calDeviations();
        return dSigChiSQ;
    }
    public double getSignificance_Tilting(){
        return cPr.getModel().getSignificance_Tilting();
    }
    public static PolynomialLineFittingSegmentNode getBetterFittingNode(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode){
        if(aNode==null) return bNode;
        if(aNode.isBetter(bNode)) return aNode;
        return bNode;
    }
    public static PolynomialLineFittingSegmentNode getBetterStartingNode(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode){
        if(aNode==null) return bNode;
        if(aNode.isBetterStarting(bNode)) return aNode;
        return bNode;
    }
    public static PolynomialLineFittingSegmentNode getBetterEndingNode(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode){
        if(aNode==null) return bNode;
        if(aNode.isBetterEnding(bNode)) return aNode;
        return bNode;
    }
    public boolean isValidDev(){
        return validDev&&bValidPars;
    }
    public void setParValidity(boolean valid){
        bValidPars=valid;
    }
    public boolean isValid(){
        return cPr.isValid();
    }
    public boolean isLonger(PolynomialLineFittingSegmentNode aNode){
        return nEnd-nStart>aNode.nEnd-aNode.nStart;
    }
    public static PolynomialLineFittingSegmentNode getLongerFittingNode(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode){
        if(aNode.isLonger(bNode)) return aNode;
        return aNode;
    }
    public static PolynomialLineFittingSegmentNode getLongerValidFittingNode(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode){
        if(aNode==null) return bNode;
        if(bNode==null) return aNode;
        //no null segments
        if(aNode.isValidDev()){
            if(bNode.isValidDev())
                return getLongerFittingNode(aNode,bNode);
            else
                return aNode;
        }else{
            if(bNode.isValidDev())
                return bNode;
            else
                return null;
        }
    }
    public double predict(double x){
        return cPr.predict(x);
    }
    public intRange getRange(){
        return new intRange(nStart,nEnd);
    }
    public intRange getEffectRange(){
        int start=nStart,end=nEnd,iF=pdX.length-1;
        while(start<=end&&intRange.contains(cvOutliarRegions, start)){
            start++;
        }
        while(end>=start&&intRange.contains(cvOutliarRegions, end)){
            end++;
        }
        return new intRange(start,end);
    }
    public DoubleRange getFittingRange(){
        int start=getStart(0,1),end=getStart(0,-1);
        return new DoubleRange(pdX[start],pdX[end]);
    }
    public int calDeviations(){
        validDev=true;
        if(!cPr.bValid) {
            validDev=false;
            return 1;
        }
        double[] pdFittedX=cPr.getFittedDataX();
        double[] pdFittedY=cPr.getFittedDataY();
        double[] pdFittedSD=cPr.getFittedDataSD();
        int len=pdFittedX.length;
        if(nStart==11&&nEnd==22){
            nStart=nStart;
        }
        dChiSQ=LineSegmentRegressionEvaluater.getChiSquare(cPr, pdX, pdY, pdSD, pbSelected, nStart, nEnd, 1, null);
        dSigChiSQ=LineSegmentRegressionEvaluater.getDevSignificance_Chisquare(cPr, pdX, pdY,pdSD,pbSelected,nStart,nEnd);
        if(dSigChiSQ<dPChiSQ) validDev=false;
        dSigTilting=LineSegmentRegressionEvaluater.getDevSignificance_Tilting(cPr, pdX, pdY,pdSD,pdTiltingSig,pbSelected,nStart,nEnd, nWsTilting);
        if(dSigTilting<dPTilting) validDev=false;
        dSigTiltingMW=LineSegmentRegressionEvaluater.getDevSignificance_TiltingMW(cPr, pdX, pdY,pdSD,pdTiltingSig,pbSelected,nStart,nEnd, nWsTilting);
//        if(dSigTiltingMW<dPTilting) validDev=false;
        dSigPWDev=LineSegmentRegressionEvaluater.getDevSignificance_Pointwise(cPr, pdX, pdY,pdSD,pdPWDevSig,pbSelected,nStart,nEnd, nWsPWDev);
        if(dSigPWDev<dPPWDev) validDev=false;
        dSigSideness=LineSegmentRegressionEvaluater.getDevSignificance_Sideness(cPr, pdX, pdY, pdSD, pbSelected, pdSidenessSig, nStart, nEnd);
        int nMaxLen=15;
        if(dSigSideness<dPSideness) validDev=false;
        dSigStarting=LineSegmentRegressionEvaluater.getDevSignificance_Ending(this,pbSelected,-1,nMaxLen,dSD);
        if(dSigStarting<dPStarting) validDev=false;
        dSigEnding=LineSegmentRegressionEvaluater.getDevSignificance_Ending(this,pbSelected,1,nMaxLen,dSD);
        if(dSigEnding<dPEnding) validDev=false;
        len=nEnd-nStart+1;
        double[] pdDelta=new double[len];
        int i,position;
        double dMaxDev=0,dev;
        for(i=0;i<len;i++){
            position=nStart+i;
            dev=pdY[position]-predict(pdX[position]);
            pdDelta[i]=dev;
            if(!pbSelected[position]) continue;
            if(Math.abs(dev)>Math.abs(dMaxDev)) {
                dMaxDev=dev;
                nMaxDevPosition=position;
            }
        }
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(pdDelta, 0);
        nNumCrossing=crossingPoints.size();
        calDevChiSQ();
        return 1;
    }
    public boolean isValidLocalDeviations(){
        if(dSigTilting<0||dSigPWDev<0) calDeviations();
        return (dSigTilting>dPTilting&&dSigPWDev>dPPWDev&&dSigSideness>dPSideness);
    }
    public boolean isValidNumExtrema(){
        int num=getNumExtrema(nStart,nEnd);
        return num>nOrder+1;
    }
    public int getNumExtrema(int start, int end){
        return m_pnLnPositions[end]-m_pnLnPositions[start]+m_pnLxPositions[end]-m_pnLxPositions[start];
    }
    public void getCorrectedSeg(int anchor, int start, int end, int delta, ArrayList<Double> dvX, ArrayList<Double> dvY,ArrayList<Double> dvSD){
        int index;
        double y0=predict(pdX[anchor]),yi;
        int sign=1;
        if(delta<0) sign=-1;
        for(index=start;index*sign<=sign*end;index+=delta){
            if(pbSelected!=null){
                if(!pbSelected[index]) continue;
            }
            yi=predict(pdX[index]);
            dvY.add(pdY[index]+y0-yi);
            dvX.add(pdX[index]);
            if(dvSD!=null) dvSD.add(pdSD[index]);
        }
    }
    public void getFittedData(ArrayList<double[]> pdvXYSD){
        pdvXYSD.clear();
        pdvXYSD.add(cPr.getFittedDataX());
        pdvXYSD.add(cPr.getFittedDataY());
        pdvXYSD.add(cPr.getFittedDataSD());
    }
    static public PolynomialLineFittingSegmentNode getBetterSegment(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode, double x, double y){
        if(aNode==null) return bNode;
        if(aNode.isBetter(bNode,x,y)) return aNode;
        return bNode;
    }
    static public PolynomialLineFittingSegmentNode getSmootherSegment(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode){
        if(aNode==null) return bNode;
        if(aNode.isSmoother(bNode)) return aNode;
        return bNode;
    }
    
    static public PolynomialLineFittingSegmentNode getSmootherSegment(PolynomialLineFittingSegmentNode aNode, PolynomialLineFittingSegmentNode bNode, int position, double dRisingInterval){
        if(aNode==null) return bNode;
        if(aNode.isSmoother(bNode, position,dRisingInterval)) return aNode;
        return bNode;
    }
        
    public double getDistance(double x){
        double xI=pdX[nStart],xF=pdX[nEnd];
        if(x<xI) return xI-x;
        if(x>xF) return x-xF;
        return 0;
    }
    public int getNextCrossingPoint(int index, int direction){
        intRange ir=new intRange(0,pdX.length-1);
        double dev0=pdY[index]-predict(pdX[index]),dev;
        index+=direction;
        boolean crossed=false;
        while(ir.contains(index)){
            dev=pdY[index]-predict(pdX[index]);
            if(dev*dev0<0) {
                crossed=true;
                break;
            }
            index+=direction;
        }
        index-=direction;
        if(!crossed) return -1;
        return index;
    }
    public ArrayList<Integer> getSelectedIndexes(){
        return cPr.m_nvSelectedIndexes;
    }
    public String[][] getRegressionResultsAsStringArray(String title){
        String[][] psData=null;
        ArrayList<Integer> nvSelectedIndexes=getSelectedIndexes();
//        double dPChiSQ=getDevSignificance_Chisquare(cPr, pdX, pdY,pdSD,pbSelected, iI, iF),dPPWDev=getDevSignificance_Pointwise(cPr, pdX, pdY, pdSD,null,pbSelected, iI, iF, wsPWDev);
//        double dPTilting=getDevSignificance_Tilting(cPr, pdX, pdY, pdSD,null, pbSelected,iI, iF, wsTilting),dSidenessSig=LineSegmentRegressionEvaluater.getDevSignificance_Sideness(cPr, pdX, pdY, pdSD, pbSelected, null, iI, iF);
        String[] ColumnHeads={"N","Order","SD","ChiSquare","SigChiSQ","SigTiltingWS4-6","SigTiltingWSMW","SigSideness","SigPWDev"+nWsPWDev};
        ArrayList<String> svHeads=CommonStatisticsMethods.copyStringArray(ColumnHeads);
        svHeads.add(0,"Title");
        int i;
        svHeads.add("X"+"Min");
        svHeads.add("X"+"Max");
//        double ChiSQ=getChiSquare(cPr,pdX,pdY,pdSD,pbSelected,iI,iF,nDelta,nvSelectedIndexes);
        psData=new String[2][svHeads.size()];
        ColumnHeads=CommonStatisticsMethods.copyStringArray1(svHeads);
        psData[0]=ColumnHeads;
        int index=0;
        psData[1][index]=title;
        index++;
        psData[1][index]=PrintAssist.ToString(nvSelectedIndexes.size());
        index++;
        psData[1][index]=PrintAssist.ToString(nOrder,0);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(Math.sqrt(CommonStatisticsMethods.buildMeanSem(pdSD).mean),3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dChiSQ, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dSigChiSQ, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dSigTilting, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dSigTiltingMW, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dSigSideness, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPPWDev, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(pdX[nStart],3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(pdX[nEnd],3);
        return psData;
    }
    ArrayList<Double> getPredictionDeviations(){
        ArrayList<Double> dvDev=new ArrayList();
        for(int i=nStart;i<=nEnd;i++){
            if(pbSelected==null) continue;
            if(!pbSelected[i]) continue;
            dvDev.add(pdY[i]-predict(pdX[i]));
        }
        return dvDev;
    }
    public boolean isShort(){
        return getNumExtrema(nStart,nEnd) < 3*nOrder;
    }
    public boolean isValidStarting(){
        return dSigStarting>dPStarting;
    }
    public boolean isValidEnding(){
        return dSigEnding>dPEnding;
    }
    public double getSD(){
        return dSD;
    }
    public int getStart(int len0, int direction){
        
        int i,position=nStart,len=0;
        if(direction<0) position=nEnd;
        if(pbSelected[position]) len++;
        intRange ir=new intRange(nStart,nEnd);
        while(len<=len0){
            position+=direction;
            if(!ir.contains(position)) {
                position-=direction;
                break;
            }
            if(pbSelected[position]) len++;
            
        }
        if(len<len0) return -1;
        return position;
    }
    public int getStart(boolean[] pbSelected, int len0, int direction){
        boolean[] pbt=this.pbSelected;
        this.pbSelected=pbSelected;
        int position=getStart(len0,direction);
        this.pbSelected=pbt;
        return position;
    }
    public double[] getPars(){
        return cPr.pdPars;
    }
    void calDevChiSQ(){
        double sd,ho,he,xl,x,xr,yl,y,yr,ol,o,or,dev;
        double[] pdSD=pdSDDev;
        if(pdSD==null) pdSD=this.pdSD;
        dChiSQDev=0;
        if(nStart==5&&nEnd==19){
            nStart=nStart;
        }
        int i,l,r,start=getStart(0,1),end=getStart(0,-1),num=0;
        for(i=start+1;i<end;i++){
            if(!pbSelected[i]) continue;
            num+=1;
            sd=pdSD[i];
            x=pdX[i];
            l=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, i-1, -1);
            r=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, i+1, 1);
            xl=pdX[l];
            xr=pdX[r];
            o=pdY[i];
            ol=pdY[l];
            or=pdY[r];
            y=predict(x);
            yl=predict(xl);
            yr=predict(xr);
            he=y-CommonMethods.getLinearIntoplation(xl, yl, xr, yr, x);
            ho=o-CommonMethods.getLinearIntoplation(xl, ol, xr, or, x);
            dev=he-ho;
            dChiSQDev+=dev*dev/(sd*sd);
        } 
        dChiSQDev*=((double)(num+2)/(double)num);
        int nu=num+2-(cPr.m_nOrder+1);
        if(nu>0){
            ChiSquaredDistributionImpl dist=new ChiSquaredDistributionImpl(nu);
            dSigDevChiSQ=-1;
            try {
                dSigDevChiSQ=1-dist.cumulativeProbability(dChiSQDev);
            }
            catch (org.apache.commons.math.MathException e){

            }
        }else{
            dChiSQDev=0;
            dChiSQDev=0.;
        }
    }
    public void setMaxRisingInterval(int interval){
        nMaxRisingInterval=interval;
    }
    public void showDeviation(){
        int len=nEnd-nStart+1,i,p;
        ArrayList<Double> dvX=new ArrayList(),dvY=new ArrayList();
        for(i=0;i<len;i++){
            p=i+nStart;
//            if(!CommonStatisticsMethods.isLocalExtrema(pdY, p, -1)&&!CommonStatisticsMethods.isLocalExtrema(pdY, p, 1)) continue;
            dvX.add(pdX[p]);
            dvY.add(pdY[p]-predict(pdX[p]));
        }
        double[] pdXT=CommonStatisticsMethods.copyToDoubleArray(dvX),pdYT=CommonStatisticsMethods.copyToDoubleArray(dvY);
        CommonGuiMethods.displayNewPlotWindowPlus("Y - prediction", pdXT, pdYT, 2, PlotWindow.LINE, Color.BLACK);
    }
}
