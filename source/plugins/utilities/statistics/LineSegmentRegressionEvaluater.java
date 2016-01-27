/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.CommonStatisticsMethods;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CustomDataTypes.intRange;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.MathException;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.HypothesisTester;
import java.util.ArrayList;
import utilities.Non_LinearFitting.FittingComparison;
import ij.IJ;
import utilities.io.PrintAssist;
import utilities.statistics.HypothesisTester;
import utilities.CustomDataTypes.DoubleRange;
import utilities.statistics.MeanSem1;

/**
 *
 * @author Taihao
 */
public class LineSegmentRegressionEvaluater {
    static public final String newString=";",newLine=PrintAssist.newline;
    static public boolean validSignificance(PolynomialLineFittingSegmentNode cSeg){
        return validSignificance(cSeg,0.05);
    }
    static public boolean validSignificance(PolynomialLineFittingSegmentNode cSeg, double p){
        return cSeg.getSignificance_ChiSquare()>p;
    }
    static public boolean validTilting(PolynomialLineFittingSegmentNode cSeg){
        return validTilting(cSeg,0.01);
    }
    static public boolean validTilting(PolynomialLineFittingSegmentNode cSeg, double p){
        return cSeg.getSignificance_Tilting()>p;
    }

    static public boolean validDeviation(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdTiltingSig,double[] pdSideness, double[] pdPWDevSig, double[] pdSD, boolean[] pbSelected, int iI, int iF, double pChiSQ, double pTilting,double pSideness,double pPWDev, int wsTilting, int wsPWDev){
        boolean valid=true;
        if(!cPr.isValid()) return false;
        if(pChiSQ>0){
            if(!validDeviation_Chisquare(cPr,pdX,pdY,pdSD,pbSelected,iI,iF,pChiSQ)) return false;
        }
        if(pTilting>0) {
            if(!validDeviation_Tilting(cPr,pdX,pdY,pdSD,pdTiltingSig, pbSelected,iI,iF,pTilting,wsTilting)) return false;
        }
        if(pSideness>0) {
            if(!validDeviation_Sideness(cPr,pdX,pdY,pdSD,pdSideness, pbSelected,iI,iF,pSideness)) return false;
        }
        if(pPWDev>0) {
            if(!validPointwiseDeviation(cPr,pdX,pdY,pdSD,pdPWDevSig,pbSelected,iI,iF,pPWDev,wsPWDev)) return false;
        }
        return valid;
    }

    static public String[][] getRegressionResultsAsStringArray(String title,PolynomialLineFittingSegmentNode seg){
        String[][] psData=null;
        double dPStarting,dPEnding;
        int nMaxLen=15;
        MeanSem0 ms;
        dPStarting=seg.dPStarting;
        dPEnding=seg.dPEnding;
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        double dPChiSQ=seg.dSigChiSQ;
        double dPTilting=seg.dSigTilting,dSidenessSig=seg.dSigSideness;
        String[] ColumnHeads={"N","Order","SD","ChiSquare","SigChiSQ","ChiSquareDev","SigChiSQDev","SigTiltingWS"+seg.nWsTilting,"SigSideness","SigPWDev"+seg.nWsPWDev,"SigStarting","SigEnding"};
        ArrayList<String> svHeads=CommonStatisticsMethods.copyStringArray(ColumnHeads);
        svHeads.add(0,"Title");
        int i;
        svHeads.add("X"+"Min");
        svHeads.add("X"+"Max");
        
        svHeads.add("Const");
        for(i=0;i<seg.nOrder;i++){
            svHeads.add("A"+(i+1));
        }
        for(i=0;i<seg.nOrder;i++){
            svHeads.add("AN"+(i+1));
        }
        if(seg.nOrder==2) svHeads.add("Peak");
        
        double ChiSQ=seg.dChiSQ;
        psData=new String[2][svHeads.size()];
        ColumnHeads=CommonStatisticsMethods.copyStringArray1(svHeads);
        psData[0]=ColumnHeads;
        CommonStatisticsMethods.getSelectedPositionsWithinRange(seg.pbSelected, new intRange(seg.nStart,seg.nEnd), nvSelectedIndexes);
        int index=0;
        psData[1][index]=title;
        index++;
        psData[1][index]=PrintAssist.ToString(nvSelectedIndexes.size());
        index++;
        psData[1][index]=PrintAssist.ToString(seg.nOrder,0);
        index++;
        ms=CommonStatisticsMethods.buildMeanSem(seg.pdSD);
        psData[1][index]=PrintAssist.ToStringScientific(ms.mean,3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(ChiSQ, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPChiSQ, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(seg.dChiSQDev, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(seg.dSigDevChiSQ, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPTilting, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dSidenessSig, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(seg.dSigPWDev, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(seg.dSigStarting, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(seg.dSigEnding, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(seg.pdX[seg.nStart],3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(seg.pdX[seg.nEnd],3);
        index++;
        
        double[] pars=seg.cPr.pdPars;
        
        for(i=0;i<seg.nOrder+1;i++){
            psData[1][index]=PrintAssist.ToStringScientific(pars[i],3);
            index++;
        }
        
        for(i=1;i<seg.nOrder+1;i++){
            psData[1][index]=PrintAssist.ToStringScientific(pars[i]/ms.mean,3);
            index++;
        }
        if(seg.nOrder==2){
            psData[1][index]=PrintAssist.ToStringScientific(pars[1]/(-2*pars[2]), 3);
        }
        return psData;
    }
    static public String[][] getRegressionResultsAsStringArray(String title,PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, boolean[] pbSelected, int iI, int iF, int nDelta, int wsTilting, int wsPWDev,double sd){
        String[][] psData=null;
        double dPStarting,dPEnding;
        int nMaxLen=15;
        dPStarting=LineSegmentRegressionEvaluater.getDevSignificance_Terminum(cPr, pdX, pdY, iI, -1, nMaxLen,sd);
        dPEnding=LineSegmentRegressionEvaluater.getDevSignificance_Terminum(cPr, pdX, pdY, iF, 1, nMaxLen,sd);
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        double dPChiSQ=getDevSignificance_Chisquare(cPr, pdX, pdY,pdSD,pbSelected, iI, iF),dPPWDev=getDevSignificance_Pointwise(cPr, pdX, pdY, pdSD,null,pbSelected, iI, iF, wsPWDev);
        double dPTilting=getDevSignificance_Tilting(cPr, pdX, pdY, pdSD,null, pbSelected,iI, iF, wsTilting),dSidenessSig=LineSegmentRegressionEvaluater.getDevSignificance_Sideness(cPr, pdX, pdY, pdSD, pbSelected, null, iI, iF);
        String[] ColumnHeads={"N","Order","SD","ChiSquare","SigChiSQ","SigTiltingWS"+wsTilting,"SigSideness","SigPWDev"+wsPWDev,"SigStarting","SigEnding"};
        ArrayList<String> svHeads=CommonStatisticsMethods.copyStringArray(ColumnHeads);
        svHeads.add(0,"Title");
        int i;
        svHeads.add("X"+"Min");
        svHeads.add("X"+"Max");
        double ChiSQ=getChiSquare(cPr,pdX,pdY,pdSD,pbSelected,iI,iF,nDelta,nvSelectedIndexes);
        psData=new String[2][svHeads.size()];
        ColumnHeads=CommonStatisticsMethods.copyStringArray1(svHeads);
        psData[0]=ColumnHeads;
        int index=0;
        psData[1][index]=title;
        index++;
        psData[1][index]=PrintAssist.ToString(nvSelectedIndexes.size());
        index++;
        psData[1][index]=PrintAssist.ToString(cPr.m_nOrder,0);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(Math.sqrt(CommonStatisticsMethods.buildMeanSem(pdSD).mean),3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(ChiSQ, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPChiSQ, 3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPTilting, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dSidenessSig, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPPWDev, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPStarting, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(dPEnding, 3);;
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(pdX[iI],3);
        index++;
        psData[1][index]=PrintAssist.ToStringScientific(pdX[iF],3);
        return psData;
    }

    static public boolean validPointwiseDeviation(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, double[] pdSig, boolean[] pbSelected, int iI, int iF, double p, int ws){
        return getDevSignificance_Pointwise(cPr,pdX,pdY,pdSD,pdSig,pbSelected, iI,iF,ws)>p;
    }
    static public double getDevSignificance_Pointwise(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, double[] pdSig, boolean[] pbSelected, int iI, int iF, int ws){
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        int minLen=3;
        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(cPr, pdX, pdY,pbSelected,iI,iF,nvSelectedIndexes);
        if(pdSig!=null) CommonStatisticsMethods.setElements(pdSig, iI, iF, 1.1);
        ArrayList<Double> dvSD=CommonStatisticsMethods.copySelectedDataToArrayList(pdSD, pbSelected, iI,iF, 1);
        if(pdDev.length<3) return -1;
        CommonStatisticsMethods.divideArrays(pdDev, dvSD);
        double[] pdDevRWA=CommonStatisticsMethods.getRunningWindowAverage(pdDev, 0, pdDev.length-1, 1, ws, false);
        double dMaxAbsDev=0,dev,sig;
        int i,len=pdDev.length;
        for(i=0;i<len;i++){
            dev=Math.abs(pdDevRWA[i]);
            sig=1.-GaussianDistribution.Phi(dev, 0, 1/Math.sqrt(2*ws+1));
            if(dev>dMaxAbsDev) dMaxAbsDev=dev;
            if(pdSig!=null) pdSig[nvSelectedIndexes.get(i)]=sig;
        }
        sig=1.-GaussianDistribution.Phi(dMaxAbsDev, 0, 1/Math.sqrt(2*ws+1));
        return sig;
    }

    static public boolean validDeviation_Tilting(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, double[] pdSig, boolean[] pbSelected, int iI, int iF, double p,int ws){
        return getDevSignificance_Tilting(cPr,pdX,pdY,pdSD,pdSig,pbSelected,iI, iF,ws)>p;
    }
    static public boolean validDeviation_Sideness(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, double[] pdSideness, boolean[] pbSelected, int iI, int iF, double p){
        return getDevSignificance_Sideness(cPr,pdX,pdY,pdSD,pbSelected,pdSideness,iI, iF)>p;
    }

    static public double getDevSignificance_Tilting(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, double[] pdSig, boolean[] pbSelected,int iI, int iF, int ws){
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(cPr, pdX, pdY,pbSelected,iI, iF,nvSelectedIndexes);
        MeanSem1 ms=CommonStatisticsMethods.buildMeanSem1(pdDev, 0, pdDev.length-1, 1);
//        double sd=ms.getSD();
        double sd=CommonStatisticsMethods.getMean(pdSD, pbSelected, iI, iF, 1);
        if(pdSig!=null) CommonStatisticsMethods.setElements(pdSig, iI, iF, 1.1);
        int i,j,len=nvSelectedIndexes.size(),p,n,index;
        len=nvSelectedIndexes.size();
        int[] pnws={3,4,5,6};
        int lWs=pnws.length;
        if(len<2*pnws[0]) return 1.1;
//        for(i=0;i<len;i++){
//            pdDev[i]/=pdSD[nvSelectedIndexes.get(i)];//12824
//        }
        ArrayList<Double> dvYL=new ArrayList(),dvYR=new ArrayList();
        double pV=0,sig=1.1,sigt;
        for(i=0;i<lWs;i++){
            ws=pnws[i];
            for(j=ws-1;j<=len-ws-1;j++){
                CommonStatisticsMethods.copyArrayToList(pdDev,dvYL,j-(ws-1),j,1);
                CommonStatisticsMethods.copyArrayToList(pdDev,dvYR,j+1,j+ws,1);
//                pV=HypothesisTester.tTest(dvYL, dvYR);
                pV=HypothesisTester.tTestGivenSD(dvYL, dvYR, sd);//12824
                index=nvSelectedIndexes.get(j);
                if(pV<sig)
                    sig=pV;
                if(pdSig==null) continue;
                sigt=pdSig[index];
                if(pV<sigt) pdSig[index]=pV;
            }
        }
        if(pdSig==null) return sig;
        for(i=0;i<len;i++){
            sigt=pdSig[nvSelectedIndexes.get(i)];
            if(sigt<0.0000000000001) sigt=0.0000000000001;
            if(pdSig!=null){
                pdSig[nvSelectedIndexes.get(i)]=Math.log10(sigt);
            }
        }
        return sig;
    }
    static public double getDevSignificance_TiltingMW(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, double[] pdSig, boolean[] pbSelected,int iI, int iF, int ws){
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(cPr, pdX, pdY,pbSelected,iI, iF,nvSelectedIndexes);
        if(pdSig!=null) CommonStatisticsMethods.setElements(pdSig, iI, iF, 1.1);
        int i,j,len=nvSelectedIndexes.size(),p,n,index;
        len=nvSelectedIndexes.size();
        int[] pnws={3,4,5,6};
        int lWs=pnws.length;
        if(len<2*pnws[0]) return 1.1;
        for(i=0;i<len;i++){
            pdDev[i]/=pdSD[nvSelectedIndexes.get(i)];
        }
        ArrayList<Double> dvYL=new ArrayList(),dvYR=new ArrayList();
        double pV=0,sig=1.1,sigt;
        for(i=0;i<lWs;i++){
            ws=pnws[i];
            for(j=ws-1;j<=len-ws-1;j++){
                CommonStatisticsMethods.copyArrayToList(pdDev,dvYL,j-(ws-1),j,1);
                CommonStatisticsMethods.copyArrayToList(pdDev,dvYR,j+1,j+ws,1);
                pV=HypothesisTester.MannWhitneyUTest(dvYL, dvYR);
                index=nvSelectedIndexes.get(j);
                if(pV<sig)
                    sig=pV;
                if(pdSig==null) continue;
                sigt=pdSig[index];
                if(pV<sigt) pdSig[index]=pV;
            }
        }
        if(pdSig==null) return sig;
        for(i=0;i<len;i++){
            sigt=pdSig[nvSelectedIndexes.get(i)];
            if(sigt<0.0000000000001) sigt=0.0000000000001;
            if(pdSig!=null){
                pdSig[nvSelectedIndexes.get(i)]=Math.log10(sigt);
            }
        }
        return sig;
    }
    static public double getDevSignificance_Tilting1(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, boolean[] pbSelected,int iI, int iF, int ws){
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(cPr, pdX, pdY,pbSelected,iI, iF,nvSelectedIndexes);
        int i,j,len=nvSelectedIndexes.size(),p,n;
        len=nvSelectedIndexes.size();
        for(i=0;i<len;i++){
            pdDev[i]/=pdSD[nvSelectedIndexes.get(i)];
        }
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(pdDev, 0, 0, pdDev.length-1, 1);
        if(len==0) return 1;
        ArrayList<Double> dvYL=new ArrayList(),dvYR=new ArrayList();
        p=0;
        n=crossingPoints.get(0);
        CommonStatisticsMethods.copyArrayToList(pdDev,dvYL,p,n,1);
        double pV,sig=1;
        len=crossingPoints.size();
        for(i=0;i<len;i++){
            p=n+1;
            if(i==len-1)
                n=pdDev.length-1;
            else
                n=crossingPoints.get(i+1);
            CommonStatisticsMethods.copyArrayToList(pdDev,dvYR,p,n,1);
            if(dvYL.size()>1&&dvYR.size()>1)
                pV=HypothesisTester.tTest(dvYL, dvYR);
            else pV=1;
            if(pV<sig)
                sig=pV;
            dvYL.clear();
            for(j=0;j<dvYR.size();j++){
                dvYL.add(dvYR.get(j));
            }
        }
        return sig;
    }
    static public double getDevSignificance_Sideness(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, boolean[] pbSelected,double[] pdDevSigSideness, int iI, int iF){
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        int i,j,len=nvSelectedIndexes.size(),iIt,iFt,ext=0;
        iIt=Math.max(0, iI-ext);
        iFt=Math.min(pdX.length-1, iF+ext);

        ArrayList<Integer> nvExcluded=new ArrayList();
        for(i=iIt;i<iI;i++){
            if(!pbSelected[i]){
                pbSelected[i]=true;
                nvExcluded.add(i);
            }
        }
        for(i=iF+1;i<=iFt;i++){
            if(!pbSelected[i]){
                pbSelected[i]=true;
                nvExcluded.add(i);
            }
        }

        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(cPr, pdX, pdY,pbSelected,iIt, iFt,nvSelectedIndexes);//The array indexes used for pdDev and dvY,dvYR are within 0 to pdDev.length-1

        int index;
        for(i=0;i<nvExcluded.size();i++){
            index=nvExcluded.get(i);
            pbSelected[index]=false;
        }


        len=nvSelectedIndexes.size();
        for(i=0;i<len;i++){
//            pdDev[i]/=pdSD[nvSelectedIndexes.get(i)];
        }
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(pdDev, 0, 0, pdDev.length-1, 1);//good

//        if(len==0) return 1;
        ArrayList<Double> dvY=new ArrayList(),dvYR=new ArrayList();
        int pp,np,cp;
        pp=0;
        cp=0;
        np=-1;
        double pV,sig=1,sigt,sd;
        for(cp=0;cp<crossingPoints.size();cp++){
            pp=np+1;
            np=crossingPoints.get(cp);
            CommonStatisticsMethods.copyArrayToList(pdDev,dvY,pp,np,1);
            sd=CommonStatisticsMethods.getMean(pdSD,null,pp,np,1);
            if(np-pp<=6)
                pV=HypothesisTester.tTest(dvY, 0,sd);
            else
                pV = HypothesisTester.tTest(dvY, 0);
            if(pV<sig) sig=pV;
            if(pdDevSigSideness!=null) {
                for(j=pp;j<=np;j++){
                    index=nvSelectedIndexes.get(j);
                    if(index<iI||index>iF) continue;
                    pdDevSigSideness[index]=pV;
                }
            }
        }
        pp=np+1;
        np=pdDev.length-1;
        CommonStatisticsMethods.copyArrayToList(pdDev,dvY,pp,np,1);
        sd=CommonStatisticsMethods.getMean(pdSD,null,pp,np,1);
        if(np-pp<=6)
            pV=HypothesisTester.tTest(dvY, 0,sd);
        else
            pV = HypothesisTester.tTest(dvY, 0);
        if(pV<sig) sig=pV;
        if(pdDevSigSideness==null) return sig;

        for(j=pp;j<=np;j++){
            index=nvSelectedIndexes.get(j);
            if(index<iI||index>iF) continue;
            pdDevSigSideness[index]=pV;
        }

        len=nvSelectedIndexes.size();
        for(i=0;i<len;i++){
            index=nvSelectedIndexes.get(i);
            if(index<iI||index>iF) continue;
            sigt=pdDevSigSideness[index];
            if(sigt<0.0000000000001) sigt=0.0000000000001;
            pdDevSigSideness[index]=Math.log10(sigt);
        }
        return sig;
    }
    static public double getBreakSignificance_Projection(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR, int nMaxLen){
        return Math.max(getDevSignificance_Projection(segL,1,nMaxLen,segR.nEnd),getDevSignificance_Projection(segR,-1,nMaxLen,segL.nStart));
    }
    static public double getBreakSignificance_ProjectionMW(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR, int nMaxLen){
        return Math.max(getDevSignificance_ProjectionMW(segL,1,nMaxLen,segR.nEnd),getDevSignificance_ProjectionMW(segR,-1,nMaxLen,segL.nStart));
    }
    static public double getDevSignificance_Projection(PolynomialLineFittingSegmentNode seg, int direction, int nMaxLen, int limit0){
        double[] pdX=seg.pdX;
        int limit=Math.min(pdX.length-1,limit0);
        int iI=Math.min(seg.nEnd+1,limit),iF=Math.min(seg.nEnd+nMaxLen, limit),iIt,iFt;

        if(direction<0) {
            limit=Math.max(0, limit0);
            iI=Math.max(limit, seg.nStart-nMaxLen);
            iF=Math.max(limit, seg.nStart-1);
        }

        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        int i,j,len=nvSelectedIndexes.size();
        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(seg.cPr, seg.pdX, seg.pdY,seg.pbSelected,iI,iF,nvSelectedIndexes);//The array indexes used for pdDev and dvY,dvYR are within 0 to pdDev.length-1

        len=nvSelectedIndexes.size();
//        for(i=0;i<len;i++){
//            pdDev[i]/=pdSD[nvSelectedIndexes.get(i)];
//        }
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(pdDev, 0, 0, pdDev.length-1, 1);//good
        len=crossingPoints.size();
        if(direction>0) {
            iIt=0;
            iFt=pdDev.length-1;/*
            if(len>0)
                iFt=crossingPoints.get(0);
            else
                iFt=pdDev.length-1;*/
        }else{
            iFt=pdDev.length-1;/*
            if(len>0)
                iIt=crossingPoints.get(len-1);
            else
                iIt=0;*/
            iIt=0;
        }
        ArrayList<Double> dvY=new ArrayList();
        CommonStatisticsMethods.copyArrayToList(pdDev,dvY,iIt,iFt,1);
        double sd=CommonStatisticsMethods.getMean(seg.pdSD,null,iI,iF,1),sig;
//        if(dvY.size()<=5)
            sig=HypothesisTester.tTest(dvY, 0,sd);
//        else
//            sig = HypothesisTester.tTestGivenSD(dvY, 0);

        return sig;
    }
    static public double getDevSignificance_Ending(PolynomialLineFittingSegmentNode seg, int direction, int nMaxLen,double sd){
        int position=seg.nEnd;
        if(direction<0) position=seg.nStart;
        return getDevSignificance_Terminum(seg.cPr,seg.pdX,seg.pdY,position,direction,nMaxLen, sd);
    }
    static public double getDevSignificance_Ending(PolynomialLineFittingSegmentNode seg, boolean[] pbSelection, int direction, int nMaxLen,double sd){
        int position=seg.nEnd;
        if(direction<0) position=seg.nStart;
        return getDevSignificance_Terminum(seg.cPr,seg.pdX,seg.pdY,pbSelection, position,direction,nMaxLen, sd);
    }
    static public double getDevSignificance_Terminum(PolynomialRegression cPr,double[] pdX, double[] pdY, int position, int direction, int nMaxLen, double sd){
        int iI=position,iF=Math.min(position+nMaxLen, pdX.length-1);

        if(direction<0) {
            iF=Math.max(position-nMaxLen,0);
        }
        
        if(iI<0||iF<0) {
            iI=iI;
        }
        
        iF=CommonStatisticsMethods.getNextCrossingPosition(cPr, pdX,pdY, iI, iF,direction);
        if(iF<0) 
            iF=-iF;//no crossing points
        else
            iF-=direction;//including only same side points

        ArrayList<Integer> nvSelectedIndexes=new ArrayList();

        ArrayList<Double> dvY=new ArrayList(),dvY0=cPr.getPredictionDeviations();
        int i=iI;
        while(!((i-iI)*(i-iF)>0)){
            dvY.add(pdY[i]-cPr.predict(pdX[i]));
            i+=direction;
        }
        if(sd<0){
            MeanSem1 ms=CommonStatisticsMethods.buildMeanSem1(dvY0, 0, dvY0.size()-1, 1);
            sd=ms.getSD();
        }
//        double sd=CommonStatisticsMethods.getMean(cPr.pdSD,null,iI,iF,direction),sig;
//        double sig=HypothesisTester.generalTTest(dvY, dvY0, sd);
        double sig=HypothesisTester.tTestGivenSD(dvY, dvY0, sd);

        return sig;
    }
    
    static public double getDevSignificance_Terminum(PolynomialRegression cPr,double[] pdX, double[] pdY, boolean[] pbSelection, int position, int direction, int nMaxLen, double sd){
        int iI=position,iF0=Math.min(position+nMaxLen, pdX.length-1);

        if(direction<0) {
            iF0=Math.max(position-nMaxLen,0);
        }
        
        if(iI<0||iF0<0) {
            iI=iI;
        }
        
        int iF=CommonStatisticsMethods.getNextCrossingPosition(cPr, pdX,pdY,pbSelection, iI, iF0,direction);
        if(iF<0) 
            iF=iF0;//no crossing points
        else
            iF-=direction;//including only same side points

        ArrayList<Double> dvY=new ArrayList(),dvY0=cPr.getPredictionDeviations();
        int i=iI;
        while(!((i-iI)*(i-iF)>0)){
            if(!pbSelection[i]) {
                i+=direction;
                continue;
            }
            dvY.add(pdY[i]-cPr.predict(pdX[i]));
            i+=direction;
        }
        if(sd<0){
            MeanSem1 ms=CommonStatisticsMethods.buildMeanSem1(dvY0, 0, dvY0.size()-1, 1);
            sd=ms.getSD();
        }
//        double sd=CommonStatisticsMethods.getMean(cPr.pdSD,null,iI,iF,direction),sig;
//        double sig=HypothesisTester.generalTTest(dvY, dvY0, sd);
        double sig=HypothesisTester.tTestGivenSD(dvY, dvY0, sd);

        return sig;
    }

    static public double getDevSignificance_ProjectionMW(PolynomialLineFittingSegmentNode seg, int direction, int nMaxLen, int limit0){
        double[] pdX=seg.pdX;
        int limit=Math.min(pdX.length-1,limit0);
        int iI=Math.min(seg.nEnd+1,limit),iF=Math.min(seg.nEnd+nMaxLen, limit),iIt,iFt;

        if(direction<0) {
            limit=Math.max(0, limit0);
            iI=Math.max(limit, seg.nStart-nMaxLen);
            iF=Math.max(limit, seg.nStart-1);
        }

        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        int len=nvSelectedIndexes.size();
        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(seg.cPr, seg.pdX, seg.pdY,seg.pbSelected,iI,iF,nvSelectedIndexes);//The array indexes used for pdDev and dvY,dvYR are within 0 to pdDev.length-1

        len=nvSelectedIndexes.size();
//        for(i=0;i<len;i++){
//            pdDev[i]/=pdSD[nvSelectedIndexes.get(i)];
//        }
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(pdDev, 0, 0, pdDev.length-1, 1);//good
        len=crossingPoints.size();
        if(direction>0) {
            iIt=0;/*
            if(len>0)
                iFt=crossingPoints.get(0);
            else
                iFt=pdDev.length-1;*/
            iFt=pdDev.length-1;
        }else{
            iFt=pdDev.length-1;/*
            if(len>0)
                iIt=crossingPoints.get(len-1);
            else
                iIt=0;*/
                iIt=0;
        }
        ArrayList<Double> dvY=new ArrayList(),dvYR=seg.getPredictionDeviations();
        CommonStatisticsMethods.copyArrayToList(pdDev,dvY,iIt,iFt,1);
        double sig=HypothesisTester.MannWhitneyUTest(dvY, dvYR);
        return sig;
    }

    static public boolean validDeviation_Chisquare(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, boolean[] pbSelected, int iI, int iF, double p){
        return getDevSignificance_Chisquare(cPr,pdX,pdY,pdSD,pbSelected,iI,iF)>p;
    }
    static public double getDevSignificance_Chisquare(PolynomialRegression cPr, double[] pdX, double[] pdY,double[] pdSD, boolean[] pbSelected, int iI, int iF){
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        double chiSQ=getChiSquare(cPr,pdX,pdY,pdSD,pbSelected,iI,iF,1,nvSelectedIndexes);
        int num=nvSelectedIndexes.size();
         int nu=num-(cPr.m_nOrder+1);
        if(nu<=0) {
//            IJ.error("the number of data need to be bigger than nPars+1:"+(cPr.m_nOrder+2));
            return 1.1;
        }
        ChiSquaredDistributionImpl dist=new ChiSquaredDistributionImpl(nu);
        double sig=-1;
        try {
            sig=1-dist.cumulativeProbability(chiSQ);
        }
        catch (org.apache.commons.math.MathException e){

        }
        return sig;
    }
    static public double getDifferenceSignificance_ChiSQ(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR){
        double sig=1.1;
        int nWs=8;
        ArrayList<Double> dvLx=new ArrayList(),dvLy=new ArrayList();
        ArrayList<Double> dvRx=new ArrayList(),dvRy=new ArrayList();
        segL.getCorrectedSeg(segL.nEnd,segL.nEnd,segL.nStart,-1,dvLx,dvLy,null);
        segR.getCorrectedSeg(segR.nStart,segR.nStart,segR.nEnd,1,dvRx,dvRy,null);
        sig=HypothesisTester.chiSquareTestDataSetsComparison(dvLy, dvRy);
        return sig;
    }
    static public double getDifferenceSignificance_TTest(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR){
        double sig=1.1;
        int nWs=8;
        ArrayList<Double> dvLx=new ArrayList(),dvLy=new ArrayList();
        ArrayList<Double> dvRx=new ArrayList(),dvRy=new ArrayList();
        segL.getCorrectedSeg(segL.nEnd,segL.nEnd,segL.nStart,-1,dvLx,dvLy,null);
        segR.getCorrectedSeg(segR.nStart,segR.nStart,segR.nEnd,1,dvRx,dvRy,null);
        sig=HypothesisTester.tTest(dvLy, dvRy);
        return sig;
    }
    static public double getDifferenceSignificance_MW(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR){
        double sig=1.1;
        int nWs=8;
        ArrayList<Double> dvLx=new ArrayList(),dvLy=new ArrayList();
        ArrayList<Double> dvRx=new ArrayList(),dvRy=new ArrayList();
        segL.getCorrectedSeg(segL.nEnd,segL.nEnd,segL.nStart,-1,dvLx,dvLy,null);
        segR.getCorrectedSeg(segR.nStart,segR.nStart,segR.nEnd,1,dvRx,dvRy,null);
        sig=HypothesisTester.MannWhitneyUTest(dvRx, dvRy);
        return sig;
    }
    static public void getLine(ArrayList<PolynomialLineFittingSegmentNode> segs, ArrayList<Double> dvXs, double deltaX, ArrayList<double[]> dvXY){
        ArrayList<Double> dvX=new ArrayList(),dvY=new ArrayList();
        int index=0,len=dvXs.size();
        double x=dvXs.get(0), right=dvXs.get(1),limit=dvXs.get(len-1);
        PolynomialLineFittingSegmentNode seg=segs.get(index);
        while(x<=limit){
            if(x>right){
                index++;
                right=dvXs.get(index+1);
                seg=segs.get(index);
            }
            dvX.add(x);
            dvY.add(seg.predict(x));
            x+=deltaX;
        }
        double[] pdX=CommonStatisticsMethods.getDoubleArray(dvX);
        double[] pdY=CommonStatisticsMethods.getDoubleArray(dvY);
        dvXY.clear();
        dvXY.add(pdX);
        dvXY.add(pdY);
    }
    static public double getDifferenceSignificance_ModelComparison(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR){
        if(segL==null||segR==null) return 1.1;
        double sig=1.1,sseL,sseR;
        int len,order=segL.nOrder+segR.nOrder+1;
        PolynomialRegression cPr;
        cPr=segL.cPr;
        len=cPr.nDataSize;
        sseL=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
        cPr=segR.cPr;
        len=cPr.nDataSize;
        sseR=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);        
        double[] pdX=CommonStatisticsMethods.mergeArray(segL.cPr.m_pdSelectedX, segR.cPr.m_pdSelectedX);
        double[] pdY=CommonStatisticsMethods.mergeArray(segL.cPr.m_pdSelectedY, segR.cPr.m_pdSelectedY);
        double[] pdSD=CommonStatisticsMethods.mergeArray(segL.cPr.pdSelectedSD, segR.cPr.pdSelectedSD);
//        cPr=new PolynomialRegression(pdX,pdY,pdSD,new DoubleRange(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY),segL.nOrder,1,null,0);
        cPr=new PolynomialRegression(pdX,pdY,pdSD,new DoubleRange(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY),order,1,null,0);
        len=cPr.nDataSize;
        double sseT=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
//        sig=FittingComparison.getFittingComparison_LeastSquare(sseT, segL.nOrder+1,sseL+sseR, 2*(segL.nOrder+1), pdX.length);
        sig=FittingComparison.getFittingComparison_LeastSquare(sseT, cPr.m_nOrder,sseL+sseR, 2*(segL.nOrder+1)+1, pdX.length);//2*(segL.nOrder+1)+1 the 1 is for the freedom of choosing the breaking point

        return sig;
    }
    static public double getSignalJumpSignificance_ModelComparison(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR, int nOrder, StringBuffer sData){
        double sig=1.1;
//        if(nOrder<0) nOrder=segL.nOrder+segR.nOrder+1;
        nOrder=segL.nOrder;
        if(sData!=null)
            if(sData.length()>0) sData.delete(0, sData.length()-1);
        ArrayList<Double> dvLx=new ArrayList(),dvLy=new ArrayList(),dvSDL=new ArrayList();
        ArrayList<Double> dvRx=new ArrayList(),dvRy=new ArrayList(),dvSDR=new ArrayList();
        segL.getCorrectedSeg(segL.nEnd,segL.nStart,segL.nEnd,1,dvLx,dvLy,dvSDL);
        segR.getCorrectedSeg(segR.nStart,segR.nStart,segR.nEnd,1,dvRx,dvRy,dvSDR);
        PolynomialRegression cPr;
        int len;
        double sseL,sseR;
        cPr=segL.cPr;
        len=cPr.nDataSize;
        sseL=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
        cPr=segR.cPr;
        len=cPr.nDataSize;
        sseR=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
        double[] pdX=CommonStatisticsMethods.mergeArray(segL.cPr.m_pdSelectedX, segR.cPr.m_pdSelectedX);
        double[] pdY=CommonStatisticsMethods.mergeArray(segL.cPr.m_pdSelectedY, segR.cPr.m_pdSelectedY);
        double[] pdSD=CommonStatisticsMethods.mergeArray(segL.cPr.pdSelectedSD, segR.cPr.pdSelectedSD);
        cPr=new PolynomialRegression(pdX,pdY,pdSD,new DoubleRange(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY),nOrder,1,null,0);
        len=cPr.nDataSize;
        double sseT=LineSegmentRegressionEvaluater.getDevSS(cPr, cPr.m_pdSelectedX, cPr.m_pdSelectedY, null, 0, len-1, 1, null);
        sig=FittingComparison.getFittingComparison_LeastSquare(sseT, cPr.m_nOrder+1,sseL+sseR, segL.nOrder+segR.nOrder+3, pdX.length);
        double dL=dvLx.get(dvLx.size()-1),dR=dvRx.get(0),delta=segL.predict(dL)-segR.predict(dL);
        double sign=1;
        if(delta<0) sign=-1;
        delta=Math.min(Math.abs(delta),Math.abs(segL.predict(dL)-segR.predict(dR)));
        delta=Math.min(delta,Math.abs(segL.predict(dR)-segR.predict(dL)));
        delta=Math.min(delta,Math.abs(segL.predict(dR)-segR.predict(dR)));
        delta*=sign;
        if(sData==null) return sig;
        double sd=CommonStatisticsMethods.getMean(segL.pdSD,segL.pbSelected,segL.nStart,segR.nEnd,1);
        String[][] psL=LineSegmentRegressionEvaluater.getRegressionResultsAsStringArray("SegL", segL.cPr, segL.pdX, segL.pdY, segL.pdSD, segL.pbSelected, segL.nStart, segL.nEnd, 1, segL.nWsTilting, segL.nWsPWDev,sd);
        String[][] psR=LineSegmentRegressionEvaluater.getRegressionResultsAsStringArray("SegR", segR.cPr, segR.pdX, segR.pdY, segR.pdSD, segR.pbSelected, segR.nStart, segR.nEnd, 1, segR.nWsTilting, segR.nWsPWDev,sd);
        String[][] psT=LineSegmentRegressionEvaluater.getRegressionResultsAsStringArray("SegT", cPr, pdX, pdY, pdSD, null, 0, pdX.length-1, 1, segL.nWsTilting, segL.nWsPWDev,sd);
        String line0="Break"+newString+"X"+newString+"Delta"+newString+"significance"+newString+CommonStatisticsMethods.getStringArrayAsDelimitedString(psL[0],newString);
        line0+=CommonStatisticsMethods.getStringArrayAsDelimitedString(psR[0],newString);
        line0+=CommonStatisticsMethods.getStringArrayAsDelimitedString(psT[0],newString)+newLine;
        line0+=""+0+newString+PrintAssist.ToStringScientific(pdX[dvLx.size()-1],3)+newString+PrintAssist.ToStringScientific(delta, 3)+newString+PrintAssist.ToStringScientific(sig, 3)+newString+CommonStatisticsMethods.getStringArrayAsDelimitedString(psL[1],newString);
        line0+=CommonStatisticsMethods.getStringArrayAsDelimitedString(psR[1],newString);
        line0+=CommonStatisticsMethods.getStringArrayAsDelimitedString(psT[1],newString)+newLine;
        sData.append(line0);
        return sig;
    }
    static public PolynomialLineFittingSegmentNode mergeSegments(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR,int nMaxSegLen, int nOrder, double dPPWDev, double dOutliarRatio){
        if(dPPWDev<0) dPPWDev=segL.dPPWDev;
        int start=Math.max(segL.nStart, segR.nStart-nMaxSegLen),end=Math.min(segR.nEnd, segL.nEnd+nMaxSegLen);
        PolynomialLineFittingSegmentNode segM=new PolynomialLineFittingSegmentNode(segL.pdX,segL.pdY,null,null,null,segL.pdSD,segL.pbSelected,null,segL.m_pnLnPositions,segL.m_pnLxPositions,start,end,nOrder,segL.nWsSD,segL.nWsTilting,segL.nWsPWDev,segL.dPChiSQ,segL.dPTilting,segL.dPSideness,dPPWDev,dOutliarRatio);
        return segM;
    }
    static public double getChiSquare(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdSD, boolean[] pbSelected, int start, int end, int delta,ArrayList<Integer> nvSelectedIndexes){
        intRange ir=new intRange(Math.min(start,end), Math.max(start, end));
        int i=start;
        double dev,sd,ChiSQ=0;
        boolean selective=(pbSelected!=null);
        while(ir.contains(i)){
            if(selective){
                if(!pbSelected[i]) {
                    i+=delta;
                    continue;
                }
            }
            dev=pdY[i]-cPr.predict(pdX[i]);
            sd=pdSD[i];

            ChiSQ+=dev*dev/(sd*sd);
            i+=delta;
            if(nvSelectedIndexes!=null) nvSelectedIndexes.add(i);
        }
        return ChiSQ;
    }
    static public double getDevSS(PolynomialRegression cPr, double[] pdX, double[] pdY, boolean[] pbSelected, int start, int end, int delta,ArrayList<Integer> nvSelectedIndexes){
        intRange ir=new intRange(Math.min(start,end), Math.max(start, end));
        int i=start;
        double dev,devSS=0;
        boolean selective=(pbSelected!=null);
        while(ir.contains(i)){
            if(selective){
                if(!pbSelected[i]) {
                    i+=delta;
                    continue;
                }
            }
            dev=pdY[i]-cPr.predict(pdX[i]);

            devSS+=dev*dev;
            i+=delta;
            if(nvSelectedIndexes!=null) nvSelectedIndexes.add(i);
        }
        return devSS;
    }
    static public void getPredictedValues(PolynomialRegression cPr, ArrayList<double[]> pdvXY){
        synchronized (cPr){
            int i,len=cPr.m_pdSelectedX.length;
            double[] pdY=new double[len],pdX=new double[len];
            double x;
            for(i=0;i<len;i++){
                x=cPr.m_pdSelectedX[i];
                pdX[i]=x;
                pdY[i]=cPr.predict(x);
            }
            pdvXY.clear();
            pdvXY.add(pdX);
            pdvXY.add(pdY);
        }
    }
    static public void getPredictedValues(PolynomialLineFittingSegmentNode seg, ArrayList<double[]> pdvXY){
        int i,len=seg.nEnd-seg.nStart+1;
        double[] pdX=new double[len], pdY=new double[len];
        double x;
        for(i=0;i<len;i++){
            x=seg.pdX[i+seg.nStart];
            pdX[i]=x;
            pdY[i]=seg.predict(x);
        }
        pdvXY.clear();
        pdvXY.add(pdX);
        pdvXY.add(pdY);
    }
    static public void markExcludedPoints(PolynomialLineFittingSegmentNode seg){
        boolean[] pbSelected=seg.pbSelected;
        int i,len,start=seg.nStart,end=seg.nEnd;
        CommonStatisticsMethods.setElements(pbSelected, false,start,end,1);
        ArrayList<Integer> nvT=seg.getSelectedIndexes();
        len=nvT.size();
        for(i=0;i<len;i++){
            pbSelected[nvT.get(i)]=true;
        }
    }
    static public void markExcludedPoints(ArrayList<PolynomialLineFittingSegmentNode> segs){
        int i,len=segs.size();
        for(i=0;i<len;i++){
            markExcludedPoints(segs.get(i));
        }
    }
    static public ArrayList<Integer> getCrossingPoints(PolynomialRegression cPr, double[] pdX, double[] pdY, boolean[] pbSelected, int start, int end){
        ArrayList<Integer> nvSelectedIndexes=new ArrayList();
        double[] pdDev=CommonStatisticsMethods.getSimpleRegressionDev(cPr, pdX, pdY,pbSelected,start, end,nvSelectedIndexes);
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(pdDev, 0, 0, pdDev.length-1, 1);
        return crossingPoints;
    }
}
