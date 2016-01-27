/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.AbfHandler;
import utilities.Non_LinearFitting.Line_Fitter;

/**
 *
 * @author Taihao
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Hashtable;
import utilities.CommonMethods;
import java.io.FileNotFoundException;
import java.io.IOException;
import ij.IJ;
import java.util.ArrayList;
import utilities.statistics.MeanSem0;
import utilities.CommonStatisticsMethods;
import utilities.io.PrintAssist;
import utilities.io.FileAssist;
import java.util.Formatter;
//import utilities.Non_LinearFitting.Fitting_Function_Expression;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import java.util.StringTokenizer;
import utilities.Non_LinearFitting.Fitting_Function;
import utilities.Non_LinearFitting.StandardFittingFunction;
import utilities.Non_LinearFitting.Line_Fitter_Coninuous;

/**
 *
 * @author Taihao
 */
public class AbfTraceFitter {
    Runtime m_cRuntime=Runtime.getRuntime();
    Abf m_cAbf;
    public void exportFittedTrace(Hashtable hTable) {
//        String protocal=(String)CommonMethods.retrieveVariable(hTable,"protocal");
//        boolean bValidProtocal=false;
        long startTime = System.currentTimeMillis();
        String abfPath=(String)CommonMethods.retrieveVariable(hTable,"abfPath:");
        m_cAbf=new Abf();
        try{
            m_cAbf.ReadData(abfPath);
        }
        catch(FileNotFoundException e){
            IJ.error("FileNotFoundException in exportCurrentNoiseCurve of AbfHandler");
        }
        catch(IOException e){
            IJ.error("IOException in exportCurrentNoiseCurve of AbfHandler");
        }
        AbfNode trace=m_cAbf.GetTrace();
        double samplingInterval=trace.fSampleInterval/1000;

        int nChannelIndex=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable, "channelIndex:"));
        double dTimeI=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "timeI:"));
        double dLength=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "length:"));
        double ws=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "windowSize:"));
        double stepSize=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "stepSize:"));
        String pathFittedTrace=FileAssist.getExtendedFileName(abfPath, "_trace");
        pathFittedTrace=FileAssist.changeExt(pathFittedTrace, "txt");
        String expressionType=(String)CommonMethods.retrieveVariable(hTable,"expressionType:");
        int nPars=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"nPars:"));
        int numScannings=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"numScannings"));
        int i;

        double pdPars[]=new double[nPars];
        for(i=0;i<nPars;i++){
            pdPars[i]=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"par"+i+":"));
        }

        Non_Linear_Fitter fitter=new Non_Linear_Fitter();
        StandardFittingFunction func=new StandardFittingFunction(expressionType);
//        StandardFittingFunction func=new StandardFittingFunction(expressionType,nPars,1);//11626
        int nMaxPars=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"maxPars:"));
        int nDataReduction=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"DataReductionFactor:"));
        fitter.setAsExpandableModel(nMaxPars);
        double dMaxDeviationLength=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"maxDeviationLength(ms):"));
        double dSlopeFactor=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"slopeFactor:"));
        double dMinWs=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"minWindowSize:"));

        int offset=0;
        int nChannels=trace.nNumChannels;
        int ntI=(int)(dTimeI/samplingInterval);
        ntI=nChannels*(ntI/nChannels)+nChannelIndex;

        int len=(int)(dLength/samplingInterval);
        len/=(nDataReduction*nChannels);

        int ntF=ntI+(len-1)*nDataReduction*nChannels;

        int nWs=(int)(ws/samplingInterval);
        nWs/=(nDataReduction*nChannels);

        int nMinWs=(int)(dMinWs/samplingInterval);
        nMinWs/=(nDataReduction*nChannels);

        int len1=2*nWs+1;

        int nSegments=len/len1;
        if((len%len1)>0) nSegments++;
        nSegments+=2;

        len=len1*nSegments;


        ntI-=len1*nChannels*nDataReduction;
        ntF=ntI+(len-1)*nChannels*nDataReduction;

        double pdI[]=new double[len];
        double pdTime[][]=new double[len][1];

        int index;
        int step=nChannels*nDataReduction;
        double dTime;

        int nMaxDevLen=(int)(dMaxDeviationLength/samplingInterval);
        nMaxDevLen/=(nChannels*nDataReduction);

        for(i=0;i<len;i++){
            index=ntI+i*step;
            dTime=index*samplingInterval;
            pdTime[i][0]=dTime;
            pdI[i]=trace.pfData[index];
        }
        ArrayList<double[]> pdvFittedPars=new ArrayList();
        Line_Fitter lf=new Line_Fitter();

        ArrayList <double[]> pdvResults=lf.fitLine(0, pdI.length-1,1, nWs, nMinWs, fitter, func, pdTime, pdI, pdPars,pdvFittedPars,nMaxDevLen);
        ArrayList <double[]> pdvData=new ArrayList();
        Abf cAbf1=new Abf();

        cAbf1.importData("D:\\Taihao\\MyProjects\\NetBeans Projects\\Java Projects\\netbeans6.5\\ImageJNB65V5\\abf header files\\2channelHeader.abf");
        cAbf1.setChannelName("fitted", 1);
        cAbf1.setChannelUnit(m_cAbf.getChannelUnit(0), 0);
        cAbf1.setChannelUnit(m_cAbf.getChannelUnit(0), 1);
        pdvData.add(pdI);
        int nSize=pdvResults.size();

        double pdLinec[]=pdvResults.get(0);
        pdvData.add(pdLinec);
        cAbf1.exportAsAbf(pdvData, samplingInterval, FileAssist.getExtendedFileName(abfPath, "_fitted"));


        ArrayList<String> titles=new ArrayList();
        titles.add("Time(ms)");
        titles.add("Current(pA)");
        titles.add("FittedLine)");

        ArrayList<Integer> precisions=new ArrayList();
        precisions.add(1);
        precisions.add(1);
        precisions.add(2);


        String fileFitted=pathFittedTrace;
        Formatter fm=PrintAssist.getQuickFormatter(fileFitted);
        int pst=3, psd=2, psl=3;
        int lt=PrintAssist.getPrintgLength(pdTime, 0, pst)+2,
                ld=PrintAssist.getPrintgLength(pdI, psd)+2,ll=PrintAssist.getPrintgLength(pdLinec, psl)+2,pl;

        pl=titles.get(0).length()+2;
        if(lt<pl) lt=pl;

        pl=titles.get(1).length()+2;
        if(ld<pl) ld=pl;

        pl=titles.get(2).length()+2;
        if(ll<pl) ll=pl;

        PrintAssist.printString(fm, titles.get(0), lt);
        PrintAssist.printString(fm, titles.get(1), ld);
        PrintAssist.printString(fm, titles.get(2), ll);
//        PrintAssist.printString(fm, "Line1", ll);
//        PrintAssist.printString(fm, "Line2", ll);
        PrintAssist.printString(fm, "RWA", ll);
        PrintAssist.printString(fm, "Adjusted", ll);
        for(i=0;i<numScannings;i++){
            PrintAssist.printString(fm, "Line"+i+1, ll);
        }
        PrintAssist.endLine(fm);

        int j;
        double[] pdRWA=CommonStatisticsMethods.getRunningWindowAverage(pdI, 0, len-1, nWs, false);
        int[] pnLags=CommonStatisticsMethods.toIntArrayRound(pdvResults.get(nSize-1));


        for(i=len1;i<=len-len1-1;i++){
            PrintAssist.printNumber(fm, pdTime[i][0], lt, pst);
            PrintAssist.printNumber(fm, pdI[i], ld, psd);
            PrintAssist.printNumber(fm, pdLinec[i], ll, psl);
            PrintAssist.printNumber(fm, pdRWA[i], ll, psl);
            PrintAssist.printNumber(fm, pdI[i]-pdLinec[i], ll, psl);
            for(j=0;j<numScannings;j++){
                PrintAssist.printNumber(fm, pdvResults.get(j+1)[i-pnLags[j]], ll, psl);
            }
            PrintAssist.endLine(fm);
        }
        fm.close();

        long endTime = System.currentTimeMillis();
        String filePars=FileAssist.changeExt(fileFitted,"par");
        fm=PrintAssist.getQuickFormatter(filePars);
        PrintAssist.printString(fm, "Fitting Function: ");
        PrintAssist.printString(fm, expressionType, expressionType.length()+2);
        PrintAssist.endLine(fm);
        PrintAssist.printString(fm, "ElapsedTime (ms):  ");
        PrintAssist.printString(fm, Long.toString(endTime-startTime));

        nSegments=pdvFittedPars.size();
        double[] pdFittedPars;
        int nNumPars;
        for(i=0;i<nSegments;i++){
            pdFittedPars=pdvFittedPars.get(i);
            nNumPars=pdFittedPars.length;
            for(j=0;j<nNumPars;j++){
                PrintAssist.printString(fm,"Parameter"+j+":    ");
                PrintAssist.printNumber(fm, pdFittedPars[j], 8);
                PrintAssist.endLine(fm);
            }
            PrintAssist.endLine(fm);
        }
        fm.close();

    }
    public void exportFittedTrace_Coninuous(Hashtable hTable) {
//        String protocal=(String)CommonMethods.retrieveVariable(hTable,"protocal");
//        boolean bValidProtocal=false;
        long startTime = System.currentTimeMillis();
        String abfPath=(String)CommonMethods.retrieveVariable(hTable,"abfPath:");
        m_cAbf=new Abf();
        try{
            m_cAbf.ReadData(abfPath);
        }
        catch(FileNotFoundException e){
            IJ.error("FileNotFoundException in exportCurrentNoiseCurve of AbfHandler");
        }
        catch(IOException e){
            IJ.error("IOException in exportCurrentNoiseCurve of AbfHandler");
        }
        AbfNode trace=m_cAbf.GetTrace();
        double samplingInterval=trace.fSampleInterval/1000;

        int nChannelIndex=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable, "channelIndex:"));
        double dTimeI=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "timeI:"));
        double dLength=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "length:"));
        double ws=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "windowSize:"));
        double stepSize=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "stepSize:"));
        String pathFittedTrace=FileAssist.getExtendedFileName(abfPath, "_trace");
        pathFittedTrace=FileAssist.changeExt(pathFittedTrace, "txt");
        String expressionType=(String)CommonMethods.retrieveVariable(hTable,"expressionType:");
        int nPars=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"nPars:"));
        int numScannings=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"numScannings"));
        int i;

        double pdPars[]=new double[nPars];
        for(i=0;i<nPars;i++){
            pdPars[i]=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"par"+i+":"));
        }

        Non_Linear_Fitter fitter=new Non_Linear_Fitter();
 //       StandardFittingFunction func=new StandardFittingFunction(expressionType,nPars,1);11626
        StandardFittingFunction func=new StandardFittingFunction(expressionType);
        int nMaxPars=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"maxPars:"));
        int nDataReduction=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"DataReductionFactor:"));
        fitter.setAsExpandableModel(nMaxPars);
        double dMaxDeviationLength=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"maxDeviationLength(ms):"));
        double dSlopeFactor=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"slopeFactor:"));
        double dMinWs=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"minWindowSize:"));

        int nChannels=trace.nNumChannels;
        int ntI=(int)(dTimeI/samplingInterval);
        ntI=nChannels*(ntI/nChannels)+nChannelIndex;

        int len=(int)(dLength/samplingInterval);
        len/=(nDataReduction*nChannels);

        int ntF=ntI+(len-1)*nDataReduction*nChannels;

        int nWs=(int)(ws/samplingInterval);
        nWs/=(nDataReduction*nChannels);

        int nMinWs=(int)(dMinWs/samplingInterval);
        nMinWs/=(nDataReduction*nChannels);

        int len1=2*nWs+1;

        int nSegments=len/len1;
        if((len%len1)>0) nSegments++;
        nSegments+=2;

        len=len1*nSegments;


        ntI-=len1*nChannels*nDataReduction;
        ntF=ntI+(len-1)*nChannels*nDataReduction;

        double pdI[]=new double[len];
        double pdTime[][]=new double[len][1];

        int index;
        int step=nChannels*nDataReduction;
        double dTime;

        int nMaxDevLen=(int)(dMaxDeviationLength/samplingInterval);
        nMaxDevLen/=(nChannels*nDataReduction);

        for(i=0;i<len;i++){
            index=ntI+i*step;
            dTime=index*samplingInterval;
            pdTime[i][0]=dTime;
            pdI[i]=trace.pfData[index];
        }

        ArrayList<double[]> pdvFittedPars=new ArrayList();
        ArrayList<Integer> nvSegmentingPoints=new ArrayList();
        double pdLinec[]=Line_Fitter_Coninuous.fitLine(0, pdI.length-1,1, nWs, nMinWs, fitter, func, pdTime,pdI, pdPars,pdvFittedPars,nMaxDevLen,nvSegmentingPoints, dSlopeFactor);

        ArrayList <double[]> pdvData=new ArrayList();
        Abf cAbf1=new Abf();

        cAbf1.importData("D:\\Taihao\\MyProjects\\NetBeans Projects\\Java Projects\\netbeans6.5\\ImageJNB65V5\\abf header files\\2channelHeader.abf");
        cAbf1.setChannelName("fitted", 1);
        cAbf1.setChannelUnit(m_cAbf.getChannelUnit(0), 0);
        cAbf1.setChannelUnit(m_cAbf.getChannelUnit(0), 1);
        pdvData.add(pdI);

        double pdIAdjusted[]=new double [len];
        for(i=0;i<len;i++){
            pdIAdjusted[i]=pdI[i]-pdLinec[i];
        }
        pdvData.add(pdIAdjusted);

        cAbf1.exportAsAbf(pdvData, samplingInterval, FileAssist.getExtendedFileName(abfPath, "_fitted"));


        ArrayList<String> titles=new ArrayList();
        titles.add("Time(ms)");
        titles.add("Current(pA)");
        titles.add("FittedLine)");

        ArrayList<Integer> precisions=new ArrayList();
        precisions.add(1);
        precisions.add(1);
        precisions.add(2);


        String fileFitted=pathFittedTrace;
        Formatter fm=PrintAssist.getQuickFormatter(fileFitted);
        int pst=3, psd=2, psl=3;
        int lt=PrintAssist.getPrintgLength(pdTime, 0, pst)+2,
                ld=PrintAssist.getPrintgLength(pdI, psd)+2,ll=PrintAssist.getPrintgLength(pdLinec, psl)+2,pl;

        pl=titles.get(0).length()+2;
        if(lt<pl) lt=pl;

        pl=titles.get(1).length()+2;
        if(ld<pl) ld=pl;

        pl=titles.get(2).length()+2;
        if(ll<pl) ll=pl;

        PrintAssist.printString(fm, titles.get(0), lt);
        PrintAssist.printString(fm, titles.get(1), ld);
        PrintAssist.printString(fm, titles.get(2), ll);
//        PrintAssist.printString(fm, "Line1", ll);
//        PrintAssist.printString(fm, "Line2", ll);
        PrintAssist.printString(fm, "RWA", ll);
        PrintAssist.printString(fm, "Adjusted", ll);
        PrintAssist.endLine(fm);

        int j;
        double[] pdRWA=CommonStatisticsMethods.getRunningWindowAverage(pdI, 0, len-1, 20, false);


        for(i=len1;i<=len-len1-1;i++){
            PrintAssist.printNumber(fm, pdTime[i][0], lt, pst);
            PrintAssist.printNumber(fm, pdI[i], ld, psd);
            PrintAssist.printNumber(fm, pdLinec[i], ll, psl);
            PrintAssist.printNumber(fm, pdRWA[i], ll, psl);
            PrintAssist.printNumber(fm, pdI[i]-pdLinec[i], ll, psl);
            PrintAssist.endLine(fm);
        }
        fm.close();

        long endTime = System.currentTimeMillis();
        String filePars=FileAssist.changeExt(fileFitted,"par");
        fm=PrintAssist.getQuickFormatter(filePars);
        PrintAssist.printString(fm, "Fitting Function: ");
        PrintAssist.printString(fm, expressionType, expressionType.length()+2);
        PrintAssist.endLine(fm);
        PrintAssist.printString(fm, "ElapsedTime (ms):  ");
        PrintAssist.printString(fm, Long.toString(endTime-startTime));
        PrintAssist.endLine(fm);
        PrintAssist.endLine(fm);

        nSegments=pdvFittedPars.size();
        double[] pdFittedPars;
        int nNumPars;
        int left=0, right;
        for(i=1;i<nSegments;i++){
            right=nvSegmentingPoints.get(i);
            pdFittedPars=pdvFittedPars.get(i);
            nNumPars=pdFittedPars.length;
            PrintAssist.printString(fm,"Segment"+i+"  From "+left+"  to  "+right);
            PrintAssist.printString(fm, "  From "+pdTime[left][0]+"ms  to  "+pdTime[right][0]+"ms");
            PrintAssist.printString(fm, "      Length: "+(pdTime[right][0]-pdTime[left][0]));
            PrintAssist.endLine(fm);
            for(j=0;j<nNumPars;j++){
                PrintAssist.printString(fm,"Parameter"+j+":    ");
                PrintAssist.printNumber(fm, pdFittedPars[j], 8);
                PrintAssist.endLine(fm);
            }
            PrintAssist.endLine(fm);
            PrintAssist.endLine(fm);
            left=right;
        }
        fm.close();
    }
    
    public void exportFittedTrace_Enveloping(Hashtable hTable) {
        long startTime = System.currentTimeMillis();
        String abfPath=(String)CommonMethods.retrieveVariable(hTable,"abfPath:");
        m_cAbf=new Abf();
        try{
            m_cAbf.ReadData(abfPath);
        }
        catch(FileNotFoundException e){
            IJ.error("FileNotFoundException in exportCurrentNoiseCurve of AbfHandler");
        }
        catch(IOException e){
            IJ.error("IOException in exportCurrentNoiseCurve of AbfHandler");
        }
        AbfNode trace=m_cAbf.GetTrace();
        double samplingInterval=trace.fSampleInterval/1000;

        int nChannelIndex=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable, "channelIndex:"));
        double dTimeI=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "timeI:"));
        double dLength=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "length:"));
        double ws=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "windowSize:"));
        String expressionType=(String)CommonMethods.retrieveVariable(hTable,"expressionType:");
        int nPars=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"nPars:"));
        int nRanking=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"anchorRanking:"));

        int i;

        double pdPars[]=new double[nPars];
        for(i=0;i<nPars;i++){
            pdPars[i]=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"par"+i+":"));
        }

        Non_Linear_Fitter fitter=new Non_Linear_Fitter();
        StandardFittingFunction func=new StandardFittingFunction(expressionType);//626
//        StandardFittingFunction func=new StandardFittingFunction(expressionType,nPars,1);
        int nMaxPars=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"maxPars:"));
        int nDataReduction=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable,"DataReductionFactor:"));
        fitter.setAsExpandableModel(nMaxPars);
        double dMaxDeviationLength=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"maxDeviationLength(ms):"));
        double dSlopeFactor=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"slopeFactor:"));
        double dMinWs=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"minWindowSize:"));
        double dEnvlpEdge=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"envelopeEdge(ms):"));
        double dMaxPeakToPeak=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable,"maxPeakToPeak:"));

        int nChannels=trace.nNumChannels;
        int ntI=(int)(dTimeI/samplingInterval);
        ntI=nChannels*(ntI/nChannels)+nChannelIndex;

        int len=(int)(dLength/samplingInterval);
        len/=(nDataReduction*nChannels);

        int ntF=ntI+(len-1)*nDataReduction*nChannels;

        int nWs=(int)(ws/samplingInterval);
        nWs/=(nDataReduction*nChannels);

        double pdI[]=new double[len];
        double pdTime[][]=new double[len][1];

        int index;
        int step=nChannels*nDataReduction;
        double dTime;

        for(i=0;i<len;i++){
            index=ntI+i*step;
            dTime=index*samplingInterval;
            pdTime[i][0]=dTime;
            pdI[i]=trace.pfData[index];
        }

        ArrayList<double[]> pdvFittedPars=new ArrayList();
        ArrayList<Integer> nvSegmentingPoints=new ArrayList();
        ArrayList<Integer> nvLoLine=new ArrayList(), nvHiLine=new ArrayList();
        double[] pdLoLine=new double[len], pdHiLine=new double[len];
        double pdLinec[]=Line_Fitter_Coninuous.fitLine_Enveloping(nWs, fitter, func, pdTime,pdI, pdPars,pdvFittedPars,nvSegmentingPoints,
                pdLoLine,pdHiLine,nRanking,nvHiLine,nvLoLine);

        ArrayList <double[]> pdvData=new ArrayList();
        Abf cAbf1=new Abf();

        cAbf1.importData("D:\\Taihao\\MyProjects\\NetBeans Projects\\Java Projects\\netbeans6.5\\ImageJNB65V5\\abf header files\\2channelHeader.abf");
        cAbf1.setChannelName("fitted", 1);
        cAbf1.setChannelUnit(m_cAbf.getChannelUnit(0), 0);
        cAbf1.setChannelUnit(m_cAbf.getChannelUnit(0), 1);
        pdvData.add(pdI);

        double pdIAdjusted[]=new double [len];
        for(i=0;i<len;i++){
            pdIAdjusted[i]=pdI[i]-pdLinec[i];
        }
        pdvData.add(pdIAdjusted);

        String sPar="_t0_"+PrintAssist.ToString(dTimeI, 0)+"_lenth_"+PrintAssist.ToString(dLength, 0)+"_ws_"+PrintAssist.ToString(ws, 0);

        cAbf1.exportAsAbf(pdvData, samplingInterval, FileAssist.getExtendedFileName(abfPath, "_fitted"+sPar));


        ArrayList<String> titles=new ArrayList();
        titles.add("Time(ms)");
        titles.add("Current(pA)");
        titles.add("FittedLine)");

        ArrayList<Integer> precisions=new ArrayList();
        precisions.add(1);
        precisions.add(1);
        precisions.add(2);


        String pathFittedTrace=FileAssist.getExtendedFileName(abfPath, "_trace"+sPar);
        pathFittedTrace=FileAssist.changeExt(pathFittedTrace, "txt");
        String fileFitted=pathFittedTrace;
        Formatter fm=PrintAssist.getQuickFormatter(fileFitted);
        int pst=3, psd=2, psl=3;
        int lt=PrintAssist.getPrintgLength(pdTime, 0, pst)+2,
                ld=PrintAssist.getPrintgLength(pdI, psd)+2,ll=PrintAssist.getPrintgLength(pdLinec, psl)+2,pl;

        pl=titles.get(0).length()+2;
        if(lt<pl) lt=pl;

        pl=titles.get(1).length()+2;
        if(ld<pl) ld=pl;

        pl=titles.get(2).length()+2;
        if(ll<pl) ll=pl;

        PrintAssist.printString(fm, titles.get(0), lt);
        PrintAssist.printString(fm, titles.get(1), ld);
        PrintAssist.printString(fm, titles.get(2), ll);
//        PrintAssist.printString(fm, "Line1", ll);
//        PrintAssist.printString(fm, "Line2", ll);
        PrintAssist.printString(fm, "RWA", ll);
        PrintAssist.printString(fm, "Adjusted", ll);
        PrintAssist.printString(fm, "LoLine", ll);
        PrintAssist.printString(fm, "HiLine", ll);
        PrintAssist.endLine(fm);

        int j;
        double[] pdRWA=CommonStatisticsMethods.getRunningWindowAverage(pdI, 0, len-1, 20, false);


        for(i=0;i<len;i++){
            PrintAssist.printNumber(fm, pdTime[i][0], lt, pst);
            PrintAssist.printNumber(fm, pdI[i], ld, psd);
            PrintAssist.printNumber(fm, pdLinec[i], ll, psl);
            PrintAssist.printNumber(fm, pdRWA[i], ll, psl);
            PrintAssist.printNumber(fm, pdI[i]-pdLinec[i], ll, psl);
            PrintAssist.printNumber(fm, pdLoLine[i], ll, psl);
            PrintAssist.printNumber(fm, pdHiLine[i], ll, psl);
            PrintAssist.endLine(fm);
        }
        fm.close();

        long endTime = System.currentTimeMillis();
        String filePars=FileAssist.changeExt(fileFitted,"par");
        fm=PrintAssist.getQuickFormatter(filePars);
        PrintAssist.printString(fm, "Fitting Function: ");
        PrintAssist.printString(fm, expressionType, expressionType.length()+2);
        PrintAssist.endLine(fm);
        PrintAssist.printString(fm, "ElapsedTime (ms):  ");
        PrintAssist.printString(fm, Long.toString(endTime-startTime));
        PrintAssist.endLine(fm);
        PrintAssist.endLine(fm);

        int nSegments=pdvFittedPars.size();
        double[] pdFittedPars;
        int nNumPars;
        int left=0, right;
        for(i=1;i<nSegments;i++){
            right=nvSegmentingPoints.get(i);
            pdFittedPars=pdvFittedPars.get(i);
            nNumPars=pdFittedPars.length;
            PrintAssist.printString(fm,"Segment"+i+"  From "+left+"  to  "+right);
            PrintAssist.printString(fm, "  From "+pdTime[left][0]+"ms  to  "+pdTime[right][0]+"ms");
            PrintAssist.printString(fm, "      Length: "+(pdTime[right][0]-pdTime[left][0]));
            PrintAssist.endLine(fm);
            for(j=0;j<nNumPars;j++){
                PrintAssist.printString(fm,"Parameter"+j+":    ");
                PrintAssist.printNumber(fm, pdFittedPars[j], 8);
                PrintAssist.endLine(fm);
            }
            PrintAssist.endLine(fm);
            PrintAssist.endLine(fm);
            left=right;
        }
        fm.close();

        String fileAnchors=FileAssist.changeExt(abfPath,"txt");
        fileAnchors=FileAssist.getExtendedFileName(fileAnchors, "anchors"+sPar);
        fm=PrintAssist.getQuickFormatter(fileAnchors);
        PrintAssist.printString(fm, "ElapsedTime (ms):  ");
        PrintAssist.printString(fm, Long.toString(endTime-startTime));
        PrintAssist.endLine(fm);
        PrintAssist.endLine(fm);

        int nAnchorsL=nvLoLine.size(),nAnchorsH=nvHiLine.size();
        int numAnchors=Math.max(nAnchorsL, nAnchorsH);
        int p;

        for(i=0;i<numAnchors;i++){
            if(i<nAnchorsL){
                p=nvLoLine.get(i);
                PrintAssist.printString(fm,"    AnchorL"+i+"  Point: "+p);
                PrintAssist.printString(fm, "  Time(ms)  "+pdTime[p][0]);
                PrintAssist.printString(fm, "  Current(ms)  "+pdI[p]);
            }

            if(i<nAnchorsH){
                p=nvHiLine.get(i);
                PrintAssist.printString(fm,"    AnchorH"+i+"  Point: "+p);
                PrintAssist.printString(fm, "  Time(ms)  "+pdTime[p][0]+"ms");
                PrintAssist.printString(fm, "  Current(ms)  "+pdI[p]);
            }
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
}
