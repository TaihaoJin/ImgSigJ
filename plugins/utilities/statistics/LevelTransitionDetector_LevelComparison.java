/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.QuickSort;
import utilities.QuickSortInteger;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.CommonStatisticsMethods;
import utilities.statistics.HypothesisTester;
import utilities.CustomDataTypes.intRange;
import FluoObjects.IPOGTLevelNode;
import ij.gui.PlotWindow;
import utilities.CommonGuiMethods;
import utilities.io.PrintAssist;
import utilities.Non_LinearFitting.FittingComparison;
import FluoObjects.IPOGTLevelInfoNode;
import utilities.statistics.OutliarExcludingLinearRegression;
import utilities.statistics.PartialArraySorter;

/**
 *
 * @author Taihao
 */
public class LevelTransitionDetector_LevelComparison {
    ArrayList<Double> dvXs,dvYs;
//    double[] m_pdY,m_pdX;
    int m_nMaxLevels, m_nMinSegLength;
    ArrayList<Integer> m_nvLx, m_nvLn;
    ArrayList<Integer> m_nvTransitions;
    double[] m_pdLx,m_pdRx;
    double[] m_pdLn,m_pdRn;
    double[] m_pdX,m_pdN;
    int m_nNumLocalMaxima,m_nData,m_nRulerSize;//m_nRulerSize is used for initial segmentation. It comparison of extremea within m_nRulersize.
    ArrayList<IPOGTLevelNode> m_cvLevelNodes;
    double m_dThreshold,m_dRisingInterval; //m_dThreshold is the threshold of the significant level (p Value).
    ArrayList<Double> m_dvX,m_dvY,m_dvYo,m_dvP,m_dvP1;
    int[] m_pnLxPositions,m_pnLnPositions;
    ArrayList<PlotWindow> m_cvTransitionPlotWindow;
    boolean showTransitions;
    int nEnvLineRanking;

    public LevelTransitionDetector_LevelComparison(){
        m_dThreshold=0.00001;
    }
/*
//    public LevelTransitionDetector_Downward(double[] pdX,double[] pdY, int nMaxLevels, int nRisingInterval){
    public LevelTransitionDetector_LevelComparison(ArrayList<Double> dvX, ArrayList<Double> dvY, int nMaxLevels, double dRisingInterval, ArrayList<PlotWindow> pws){
        //pdX is not used in this version. pdY is assumed to be contineouse equal interval data points
        this();
//        updateLevelNodes(pdX,pdY,nMaxLevels,nRisingInterval);
        updateLevelNodes(dvX,dvY,nMaxLevels,dRisingInterval,pws,false);
    }
//    public void updateLevelNodes(double[] pdX,double[] pdY, int nMaxLevels, int nRisingInterval){
    public void updateLevelNodes(ArrayList<Double> dvX, ArrayList<Double> dvY,  int nMaxLevels, double dRisingInterval,ArrayList<PlotWindow> pws,boolean showTransitions){
//        m_pdX=pdX;
        this.showTransitions=showTransitions;
        m_cvTransitionPlotWindow=pws;
        m_dvX=dvX;
        m_dvYo=dvY;
        m_dvY=new ArrayList();
        CommonStatisticsMethods.copyDoubleArray(m_dvYo,m_dvY);
        m_dvP=new ArrayList();
        m_dvP1=new ArrayList();
        if(m_nvLn==null)
            m_nvLn=new ArrayList();
        else
            m_nvLn.clear();
        if(m_nvLx==null)
            m_nvLx=new ArrayList();
        else
            m_nvLx.clear();
        m_nMaxLevels=nMaxLevels;
        m_nData=m_dvY.size();
        m_dRisingInterval=dRisingInterval;
        m_nMinSegLength=3*(int)m_dRisingInterval;
        m_nRulerSize=15;
        nEnvLineRanking=1;
        CommonMethods.LocalExtrema(m_dvY, m_nvLn,m_nvLx);
        markLocalExtremaPositions();
        calLeftAndRightMaxs_WS();
        calLeftAndRightMins_WS();
        detectPreliminaryTransitions();
        detectTransitoins_Downward();
        buildLevelNodes();
    }

    int getLeftLxIndex(int index){
        return m_pnLxPositions[index]-1;
    }

    int getLeftLnIndex(int index){
        return m_pnLxPositions[index]-1;
    }

    void markLocalExtremaPositions(){//index=m_pnLxPositions[i], index is the index of the local maximum at the immediate
        //right side of i. i is the index of a data point
        m_pnLxPositions=new int[m_nData];
        m_pnLnPositions=new int[m_nData];
        CommonStatisticsMethods.setElements(m_pnLxPositions,-1);
        CommonStatisticsMethods.setElements(m_pnLnPositions,-1);
        int i,index,len=m_nvLx.size(),j;
        int index0=0;
        for(i=0;i<len;i++){
            index=m_nvLx.get(i);
            for(j=index0;j<index;j++){
                m_pnLxPositions[j]=i;
            }
            index0=index;
        }

        len=m_nvLn.size();
        index0=0;
        for(i=0;i<len;i++){
            index=m_nvLn.get(i);
            for(j=index0;j<index;j++){
                m_pnLnPositions[j]=i;
            }
            index0=index;
        }
    }
    void calLeftAndRightMaxs_WS(){
        int i,j;
        double dx=Double.NEGATIVE_INFINITY,dt;
        PartialArraySorter pas=new PartialArraySorter(nEnvLineRanking,false);
        m_nNumLocalMaxima=m_nvLx.size();
        //use rulerSize;
        m_pdLx=new double[m_nData];
        m_pdRx=new double[m_nData];
        m_pdX=new double[m_nData];

        int i1;
        for(i=0;i<m_nData;i++){
            m_pdX[i]=Double.POSITIVE_INFINITY;
        }
        for(i=0;i<m_nData;i++){
                i1=Math.min(m_nData-1, i+m_nRulerSize);
                pas.reset();
                pas.updateRankings(m_dvY,i,i1);
//                dx=m_dvY.get(i);
//            for(j=i;j<=i1;j++){
//                dt=m_dvY.get(j);
//                if(dt>dx){
//                    dx=dt;
//                }
//            }
            pas.getDataSize();
            dx=pas.getElement(Math.min(nEnvLineRanking-1,pas.getDataSize()-1));
            for(j=i;j<=i1;j++){
                if(m_pdX[j]>dx) m_pdX[j]=dx;
            }
            m_pdRx[i]=dx;
        }

        dx=Double.POSITIVE_INFINITY;
        for(i=m_nData-1;i>=0;i--){
            i1=Math.max(0, i-m_nRulerSize);
//            dx=m_dvY.get(i);
//            for(j=i;j>=i1;j--){
//                dt=m_dvY.get(j);
//                if(dt>dx){
//                    dx=dt;
//                }
//            }
            if(i==0){
                i=i;
            }
            pas.reset();
            pas.updateRankings(m_dvY,i1,i);
            dx=pas.getElement(Math.min(nEnvLineRanking-1,pas.getDataSize()-1));
            m_pdLx[i]=dx;
        }
        for(i=0;i<m_nData;i++){
            if(m_pdLx[i]<m_pdX[i]) m_pdX[i]=m_pdLx[i];
            if(m_pdRx[i]<m_pdX[i]) m_pdX[i]=m_pdRx[i];
        }
    }
    void calLeftAndRightMins_WS(){
        int i,index0,index,len,j;
        double dn=Double.POSITIVE_INFINITY,dt;
        m_pdLn=new double[m_nData];
        m_pdRn=new double[m_nData];
        m_pdN=new double[m_nData];

        len=m_nvLx.size();

        index0=0;
        int i1;
        for(i=0;i<m_nData;i++){
            m_pdN[i]=Double.NEGATIVE_INFINITY;
        }
        PartialArraySorter pas=new PartialArraySorter(nEnvLineRanking, true);
        for(i=0;i<m_nData;i++){
            i1=Math.min(m_nData-1, i+m_nRulerSize);
//            dn=m_dvY.get(i);
//            for(j=i;j<=i1;j++){
//                dt=m_dvY.get(j);
//                if(dt<dn){
//                    dn=dt;
//                }
//            }
            pas.reset();
            pas.updateRankings(m_dvY,i,i1);
            dn=pas.getElement(Math.min(nEnvLineRanking-1,pas.getDataSize()-1));
            for(j=i;j<=i1;j++){
                if(m_pdN[j]<dn)m_pdN[j]=dn;
            }
            m_pdRn[i]=dn;
        }

        dn=Double.POSITIVE_INFINITY;;
        for(i=m_nData-1;i>=0;i--){
            i1=Math.max(0, i-m_nRulerSize);
//            dn=m_dvY.get(i);
//            for(j=i;j>=i1;j--){
//                dt=m_dvY.get(j);
//                if(dt<dn){
//                    dn=dt;
//                }
//            }
            pas.reset();
            pas.updateRankings(m_dvY,i1,i);
            dn=pas.getElement(Math.min(nEnvLineRanking-1,pas.getDataSize()-1));
            m_pdLn[i]=dn;
        }
        for(i=0;i<m_nData;i++){
            if(m_pdLn[i]>m_pdN[i]) m_pdN[i]=m_pdLn[i];
            if(m_pdRn[i]>m_pdN[i]) m_pdN[i]=m_pdRn[i];
        }
    }

    int nextIndex(int index, int sign){//returns the index at which dvX is at least m_nRisingInterval away
        //from 
        int nextIndex=index+sign;
        if(nextIndex<0||nextIndex>=m_nData) return -1;
        double x0=m_dvX.get(index),x=m_dvX.get(nextIndex);
        while(sign*(x-x0)<m_dRisingInterval){
            nextIndex+=sign;
            if(nextIndex<0||nextIndex>=m_nData) return -1;
            x=m_dvX.get(nextIndex);
        }
        return nextIndex;
    }
    void detectPreliminaryTransitions(){
        m_nvTransitions=new ArrayList();
        int i,len,j,jI,jF,lastIndex=nextIndex(m_nData-1,-1),nextIndex;
        double y,y0,dy;
        double[] pdDelta=new double[lastIndex+1];
        int[] pnIndexes=new int[lastIndex+1];
        for(i=0;i<=lastIndex;i++){
            nextIndex=nextIndex(i,1);
            y0=m_pdRx[i];
            y=m_pdRx[nextIndex];
            dy=y0-y;
            pdDelta[i]=-dy;
            pnIndexes[i]=i;
        }

        double[] pdDelta0=CommonStatisticsMethods.copyArray(pdDelta);
        QuickSort.quicksort(pdDelta, pnIndexes);

        ArrayList<intRange> irs=getLargeSlopeRanges(pnIndexes);
        intRange ir;

        double delta,dx;
        len=irs.size();
        int ix;//choose the largest decline (within m_nRisingInteval) in each contineous range as possible transition point.
        for(i=0;i<len;i++){
            dx=Double.NEGATIVE_INFINITY;
            ir=irs.get(i);
            ix=ir.getMin();
            jI=ir.getMin();
            jF=nextIndex(ir.getMax(),-1);
            for(j=jI;j<=jF;j++){
                nextIndex=nextIndex(j,1);
                delta=m_pdRx[j]-m_pdRx[nextIndex];
                if(delta>dx){
                    ix=j;
                    dx=delta;
                }
            }
            m_nvTransitions.add(ix);
        }
    }
    ArrayList<intRange> getLargeSlopeRanges(int[] pnIndexes){
        ArrayList<intRange> ira=new ArrayList();
        intRange ir;
        int num=0,len,nRanking=0,index,i,indext,nextIndex;
        ArrayList<Integer> nvOverlappingRangeIndexes=new ArrayList();
        intRange irt;

        while(num<m_nMaxLevels-1){
            if(nRanking>=pnIndexes.length) break;
            index=pnIndexes[nRanking];
            nextIndex=nextIndex(index,1);
            if(nextIndex>m_nData-1) nextIndex=m_nData-1;
            ir=new intRange(index,nextIndex);
            nvOverlappingRangeIndexes.clear();
            for(i=0;i<num;i++){
                irt=ira.get(i);
                if(ir.overlapOrconnected(irt)) nvOverlappingRangeIndexes.add(i);
            }
            len=nvOverlappingRangeIndexes.size();
            for(i=len-1;i>=1;i--){
                indext=nvOverlappingRangeIndexes.get(i);
                irt=ira.get(indext);
                ir.expandRange(irt);
                ira.remove(indext);
//                nvOverlappingRangeIndexes.remove(i);//will be cleared at the begining of the next iteration
            }
            if(len==0)
                ira.add(ir);
            else{
                ira.get(nvOverlappingRangeIndexes.get(i)).expandRange(ir);
            }
            num=ira.size();
            nRanking++;
        }
        return ira;
    }
    void detectTransitoins_Downward(){
        int index,left,right,i,ld,rd,ldt,rdt,indext,j;
//        QuickSortInteger.quicksort(m_nvTransitions);
        int nTransitions=m_nvTransitions.size();
        boolean[] pbValid=new boolean[nTransitions];
        for(i=0;i<nTransitions;i++){
            pbValid[i]=true;
        }
        for(i=nTransitions-1;i>=0;i--){
            index=m_nvTransitions.get(i);
            ld=m_nData;
            rd=m_nData;
            left=-1;
            right=-1;
            for(j=0;j<nTransitions;j++){
                if(!pbValid[j]) continue;
                indext=m_nvTransitions.get(j);
                if(indext<index){
                    ldt=index-indext;
                    if(ldt<ld){
                        ld=ldt;
                        left=indext;
                    }
                }
                if(indext>index){
                    rdt=indext-index;
                    if(rdt<rd){
                        rd=rdt;
                        right=indext;
                    }
                }
            }
            if(left<0) left=0;
            if(right<0) right=m_nData-1;
            pbValid[i]=isValidBreakPoint_ModelComparison_Downward(left,index,right);
        }
        ArrayList<Integer> nvValidTransitions=new ArrayList();
        for(i=nTransitions-1;i>=0;i--){
            if(!pbValid[i]) continue;
            nvValidTransitions.add(m_nvTransitions.get(i));
        }
        QuickSortInteger.quicksort(nvValidTransitions);
        m_nvTransitions=nvValidTransitions;
    }
    boolean isValidBreakPoint_ModelComparison_Downward(int left, int index, int right){
        if(left>0) left=nextIndex(left,1);
        if(index-left<2) return false;

        int nWS=10;
        int indext=nextIndex(index,1);

        left=Math.max(left, index-nWS);//11n18
        right=Math.min(right, indext+nWS);

        double sse1=getDeviationSquareSum_SimpleRegression(left,right);
        double sse2=getDeviationSquareSum_SimpleRegression(left,index,right);
        double p=FittingComparison.getFittingComparison_LeastSquare(sse1, 2, sse2, 4, right-left+1);

        SimpleRegression sr1=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, left, index);

        ArrayList<Double> dvL=getLinearDriftCorrectedData(sr1,left,index,index);
        SimpleRegression sr2=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, indext, right);
        ArrayList<Double> dvR=getLinearDriftCorrectedData(sr2,index+1,right,index);

        if(sr1.predict(m_dvX.get(index))<sr2.predict(m_dvX.get(index))) return false;

        ArrayList<Double> dvY=CommonStatisticsMethods.copyDoubleArray(dvL);
        CommonStatisticsMethods.appendDoubleArray(dvR, dvY);

        ArrayList<Double> dvX=new ArrayList();
        CommonStatisticsMethods.appendDoubleArray(m_dvX, dvX,left,right);

        double sse3=getDeviationSquareSum_SimpleRegression(dvX,dvY,left-left,right-left);
        double p1=FittingComparison.getFittingComparison_LeastSquare(sse3, 2, sse2, 4, right-left+1);

        if(p1<m_dThreshold){
            m_dvP.add(p);
            m_dvP1.add(p1);
        }
        if(m_cvTransitionPlotWindow!=null&&showTransitions) displayLinearDriftCorrectedTrace(left,index,right,p,p1);
        return p1<m_dThreshold;
    }

    double getDeviationSquareSum_SimpleRegression(int iI, int iF){
        return getDeviationSquareSum_SimpleRegression(m_dvX,m_dvY,iI,iF);
    }

    double getDeviationSquareSum_SimpleRegression(ArrayList<Double>dvX, ArrayList<Double> dvY, int iI, int iF){
        SimpleRegression sr=CommonStatisticsMethods.getSimpleLinearRegression(dvX,dvY, iI, iF);
        double x,dy,sse=0;
        for(int i=iI;i<=iF;i++){
            x=dvX.get(i);
            dy=dvY.get(i)-sr.predict(x);
            sse+=dy*dy;
        }
        return sse;
    }

    double getDeviationSquareSum_SimpleRegression(int left,int index, int right){
        double sse=0;

        sse=getDeviationSquareSum_SimpleRegression(left,index);
        double x,x1,x2,y1,y2,dy;
        int indext=nextIndex(index,1),i;
        if(left>0) left=nextIndex(left,1);
        SimpleRegression srL=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, left,index);
        SimpleRegression srR=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, indext,right);

        x1=m_dvX.get(index);
        x2=m_dvX.get(indext);
        y1=srL.predict(x1);
        y2=srR.predict(x2);


        double k=(y2-y1)/(x2-x1);
        for(i=index+1;i<=indext-1;i++){
            dy=m_dvY.get(i)-(y1+k*(m_dvX.get(i)-x1));
            sse+=dy*dy;
        }
        sse+=getDeviationSquareSum_SimpleRegression(indext,right);
        return sse;
    }

    ArrayList<Double> getLinearDriftCorrectedData(int index1, int index2, int anchor){
        SimpleRegression sr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX,m_dvY, index1, index2);
        return getLinearDriftCorrectedData(sr,index1,index2,anchor);
    }
    ArrayList<Double> getLinearDriftCorrectedData(SimpleRegression sr, int index1, int index2, int anchor){
        ArrayList<Double> dvYc=new ArrayList();
        int len=index2-index1+1,index;
        if(len<2) {
            for(index=index1;index<=index2;index++){
                dvYc.add(m_dvY.get(index));
            }
            return dvYc;
        }
        double x;
        x=m_dvX.get(anchor);
        double d0=sr.predict(x);
        for(index=index1;index<=index2;index++){
            x=m_dvX.get(index);
            dvYc.add(m_dvY.get(index)-sr.predict(x)+d0);
        }
        return dvYc;
    }
    public ArrayList<Integer> getTransitions(){
        return m_nvTransitions;
    }
    public void buildLevelNodes(){
        ArrayList<Double> dvT=m_dvY;
        m_dvY=m_dvYo;
        IPOGTLevelNode lNode;
        int i,len=m_nvTransitions.size(),index0,index,nEnd=m_dvX.size()-1;

        m_cvLevelNodes=new ArrayList();

        index0=0;
        SimpleRegression sr;
        double x,yr;
        int level=0,ir;
        for(i=0;i<len;i++){
            level=i;
            index=m_nvTransitions.get(i);
            lNode=buildLevelNode(level,index0,index);
            m_cvLevelNodes.add(lNode);
            index0=nextIndex(index,1);
        }
        level++;
        index=m_nData-1;
        lNode=buildLevelNode(level,index0,index);
        m_cvLevelNodes.add(lNode);
        m_dvY=dvT;
        adjustLevelIndex(m_cvLevelNodes);
    }
    void displayLinearDriftCorrectedTrace(){
        int i,j,len=m_nvTransitions.size(),len1;
        ArrayList<Double> dvY=new ArrayList(),dvy=new ArrayList(),dvX=new ArrayList(),dvL,dvR;
        ArrayList<Integer> nvIndexes=new ArrayList();
        int index0=0,index=0,indext;
        for(i=0;i<len;i++){
            dvX.clear();
            dvY.clear();
            index=m_nvTransitions.get(i);

            dvL=getLinearDriftCorrectedData(index0,index,index);
            len1=dvL.size();
            for(j=index0;j<=index;j++){
                dvX.add(m_dvX.get(j));
                dvY.add(dvL.get(j-index0));

            }

            indext=nextIndex(index,1);
            if(i<len-1){
                index=m_nvTransitions.get(i+1);
            }else{
                index=m_nData-1;
            }
            dvR=getLinearDriftCorrectedData(indext,index,indext);
            len1=dvR.size();
            for(j=0;j<len1;j++){
                dvX.add(m_dvX.get(indext+j));
                dvY.add(dvR.get(j));

            }
            CommonGuiMethods.displayCurve("Transition at "+PrintAssist.ToString(m_dvX.get(m_nvTransitions.get(i)), 1)+"p= "+PrintAssist.ToStringScientific(m_dvP.get(i), 1)+" p1= "+PrintAssist.ToStringScientific(m_dvP1.get(i), 1) , "X", "Y", dvX, dvY);
            index0=nextIndex(m_nvTransitions.get(i),1);
        }
    }

    void displayLinearDriftCorrectedTrace(int left, int index, int right, double p1, double p2){
        int j,len1;
        double x=m_dvX.get(index);
        ArrayList<Double> dvY=new ArrayList(),dvy=new ArrayList(),dvX=new ArrayList(),dvL,dvR;
        int indext;

        dvL=getLinearDriftCorrectedData(left,index,index);
        len1=dvL.size();
        for(j=left;j<=index;j++){
            dvX.add(m_dvX.get(j));
            dvY.add(dvL.get(j-left));
        }

        indext=nextIndex(index,1);
        SimpleRegression sr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, indext,right);

        dvR=getLinearDriftCorrectedData(sr,index+1,right,index);
        len1=dvR.size();
        for(j=0;j<len1;j++){
            dvX.add(m_dvX.get(index+1+j));
            dvY.add(dvR.get(j));

        }
        PlotWindow pw=CommonGuiMethods.displayCurve("Transition at "+PrintAssist.ToString(m_dvX.get(index), 1)+"p= "+PrintAssist.ToStringScientific(p1, 1)+" p1= "+PrintAssist.ToStringScientific(p2, 1) , "X", "Y", dvX, dvY);
        if(m_cvTransitionPlotWindow!=null) m_cvTransitionPlotWindow.add(pw);
    }
/*
    void correctLinearDrift(int iI, int iF, int anchor){
        SimpleRegression sr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, iI,iF);
        correctLinearDrift(sr,iI,iF,anchor);
    }*//*
    void correctLinearDrift(SimpleRegression sr, int iI, int iF, int anchor){
        if(iF-iI+1>m_nMinSegLength){
            double x=m_dvX.get(anchor);
            double y0=sr.predict(x),yc;
            for(int i=iI;i<=iF;i++){
                x=m_dvX.get(i);
                yc=m_dvY.get(i)-sr.predict(x)+y0;
                m_dvY.set(i, yc);
            }
        }
    }

    void correctLinearDrift(){
        int i,len=m_nvTransitions.size();
        int right=m_nData-1,index,left,indext;
        SimpleRegression sr;
        QuickSortInteger.quicksort(m_nvTransitions);
        for(i=len-1;i>=0;i--){
            index=m_nvTransitions.get(i);
            indext=nextIndex(index,1);
            if(i==len-1&&m_nData-indext>m_nMinSegLength){
                sr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, indext,right);
                correctLinearDrift(sr,indext,right,indext);
    //            straightenSegment(index,right);
            }

            if(i>0)
                left=nextIndex(m_nvTransitions.get(i-1),1);
//                left=m_nvTransitions.get(i-1);
            else
                left=0;

            sr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, left,index);
            correctLinearDrift(sr,0,index,index);
//            CommonGuiMethods.displayCurve("Correct Linear Drift "+PrintAssist.ToString(m_dvX.get(left),1)+ " to "+PrintAssist.ToString(m_dvX.get(index), 1), "X", "Y", m_dvX,m_dvY);
//            straightenSegment(left,index);
//            CommonGuiMethods.displayCurve("Straighten "+PrintAssist.ToString(m_dvX.get(left),1)+ " to "+PrintAssist.ToString(m_dvX.get(index), 1), "X", "Y", m_dvX,m_dvY);
       }
    }

    int straightenSegment(int left, int right){
        SimpleRegression sr;
        if(right-left<2*m_nMinSegLength+1) return -1;
        int i,len=m_nvTransitions.size();
        int vertex;
        boolean furtherStraighten=true;
        double sse1=getDeviationSquareSum_SimpleRegression(left,right),ssex=Double.POSITIVE_INFINITY,sse2,p;
        while(furtherStraighten){
//            sse1=getDeviationSquareSum_SimpleRegression(left,right);
            vertex=left+m_nMinSegLength-1;
            for(i=left+m_nMinSegLength-1;i<right-m_nMinSegLength+1;i++){
                sse2=getDeviationSquareSum_SimpleRegression(left,i,right);
                if(sse2<ssex) {
                    ssex=sse2;
                    vertex=i;
                }
            }
            sr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, nextIndex(vertex,1),right);
            correctLinearDrift(sr,0,right,right);
            sr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, left,vertex);
            correctLinearDrift(sr,0,vertex,vertex);
            p=FittingComparison.getFittingComparison_LeastSquare(sse1, 2, ssex, 4, right-left+1);
            furtherStraighten=(p<0.01);
            sse1=ssex;
        }
        return 1;
    }

    public ArrayList<IPOGTLevelNode> getLevelNodes(){
        return m_cvLevelNodes;
    }

    public IPOGTLevelNode removeLevelTransition(int slice){
        IPOGTLevelNode lNode=IPOGTLevelInfoNode.getLevelNode(m_cvLevelNodes, slice);
        int sign=1;
        int len=m_cvLevelNodes.size();
        if(Math.abs(lNode.dXEnd-slice)<Math.abs(lNode.dXStart-slice)) sign=-1;
        int index=IPOGTLevelInfoNode.getLevelNodeIndex(m_cvLevelNodes,lNode);
        if(index<0) return null;
        if(sign==1){
            if(index==0) return null;
            index--;
        }else{
            if(index>=len-1) return null;
        }
        lNode=mergeLevels(m_cvLevelNodes.get(index),m_cvLevelNodes.get(index+1));
        m_cvLevelNodes.remove(index+1);
        m_cvLevelNodes.set(index, lNode);
        adjustLevelIndex(m_cvLevelNodes);
        return lNode;
    }
    public static void adjustLevelIndex(ArrayList<IPOGTLevelNode> lNodes){
        int i,len=lNodes.size();
        for(i=0;i<len;i++){
            lNodes.get(i).setLevel(len-i);
        }
    }
    public int getSliceIndex(int slice){
        int index=Math.min(m_nData-1, slice);
        int nX=(int)(m_dvX.get(index)+0.5);
        while(nX>slice){
            index--;
            if(index<0) return index;
            nX=(int)(m_dvX.get(index)+0.5);
        }
        return index;
    }
    public ArrayList<IPOGTLevelNode> createLevelTransition(int slice){
        ArrayList<IPOGTLevelNode> lNodes=new ArrayList();
        int position=getSliceIndex(slice);
        if(position<0) return null;
        IPOGTLevelNode lNode=IPOGTLevelInfoNode.getLevelNode(m_cvLevelNodes,slice);
        if(lNode==null) return null;
        int left=lNode.indexI,right=lNode.indexF;
        int lIndex=IPOGTLevelInfoNode.getLevelNodeIndex(m_cvLevelNodes,lNode),lxIndex=getLeftLxIndex(position);
        if(lIndex<0||lxIndex<0) return null;
        int index=m_nvLx.get(lxIndex);
        if(index<=lNode.indexI) return null;
        int indext=nextIndex(index,1);
        if(indext>=lNode.indexF) return null;
        int level=lNode.nLevel;
        lNode=buildLevelNode(level,indext,right);
        lNodes.add(lNode);
        m_cvLevelNodes.set(lIndex, lNode);
        lNode=buildLevelNode(level+1,left,index);
        lNodes.add(lNode);
        m_cvLevelNodes.add(lIndex,lNode);

        adjustLevelIndex(m_cvLevelNodes);
        return lNodes;
    }
    IPOGTLevelNode buildLevelNode(int level, int left, int right){
        IPOGTLevelNode lNode=new IPOGTLevelNode();
        int nEnd=m_dvY.size()-1;
        double yr;
        int ir=right+(int)(m_dRisingInterval+0.5);
        if(ir<=nEnd)
            yr=m_dvY.get(ir);
        else
            yr=0;
        lNode.cSr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX,m_dvY, left,right);
        lNode.dSlope=lNode.cSr.getSlope();
        lNode.dYStart=lNode.cSr.predict(m_dvX.get(left));
        lNode.dYEnd=lNode.cSr.predict(m_dvX.get(right));
        lNode.dXStart=m_dvX.get(left);
        lNode.dXEnd=m_dvX.get(right);
        lNode.cSr=CommonStatisticsMethods.getSimpleLinearRegression(m_dvX, m_dvY, left,right);
        lNode.indexI=left;
        lNode.indexF=right;
        lNode.nLevel=level;
        lNode.stepSize=m_dvY.get(right)-yr;
        return lNode;
    }
    public IPOGTLevelNode mergeLevels(IPOGTLevelNode lNode0, IPOGTLevelNode lNode1){//lNode1 is at the right and has lower level
        IPOGTLevelNode lNode;
        int iI=lNode0.indexI,iF=lNode1.indexF;
        lNode=buildLevelNode(lNode1.nLevel,iI,iF);
        return lNode;
    }
    public void setLevelNodes(ArrayList<IPOGTLevelNode> cvLevelNodes){
        m_cvLevelNodes=cvLevelNodes;
    }
    public void buildTerminalRegresions(IPOGTLevelNode lNode){
        int len=lNode.nRulerSize;
        int nOutliars=(int)(0.2*len);
        lNode.leftSR=new OutliarExcludingLinearRegression(m_dvX,m_dvY,lNode.indexI,Math.min(lNode.indexF, lNode.indexI+len),nOutliars);
        lNode.rightSR=new OutliarExcludingLinearRegression(m_dvX,m_dvY,Math.max(lNode.indexI,lNode.indexF-len), lNode.indexF,nOutliars);
    }
    public double[] getEnvelopingLinesLow(){
        return m_pdN;
    }
    public double[] getEnvelopingLinesHi(){
        return m_pdX;
    }*/
}
