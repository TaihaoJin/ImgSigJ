/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.AbfHandler;
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
import utilities.io.AsciiInputAssist;
import java.io.BufferedReader;
import java.util.StringTokenizer;

/**
 *
 * @author Taihao
 */
/*class fittingFunction extends Fitting_Function{
    String m_sExpressionType;
    public fittingFunction(String sType, int nNumPars, int nNumVars){
        m_nPars=nNumPars;
        m_nVars=nNumVars;
        m_sExpressionType=sType;
    }
    public double fun(double[] pdPars, double[] pdX){
        double dv=0;
        int i;
        boolean validFunc=false;
        if(m_sExpressionType.contentEquals("exponetial_I_t")){
            validFunc=true;
            int nTerms=m_nPars/2;
            dv=pdPars[2*nTerms];
            for(i=0;i<nTerms;i++){
                dv+=pdPars[i]*Math.exp(-pdX[0]/pdPars[nTerms+i]);
            }
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            validFunc=true;
            dv=0;
            for(i=0;i<m_nPars;i++){
                dv+=pdPars[i]*Math.pow(pdX[0], i);
            }
        }
        if(!validFunc) IJ.error("undefined expression type: "+m_sExpressionType);
        return dv;
    }
}*/
public class AbfHandler {
    Abf m_cAbf;
    public AbfHandler(){
        
    }
    public AbfHandler(Abf cAbf){
        m_cAbf=cAbf;
    }
    public void exportCurrentNoiseCurve(Hashtable hTable) {
//        String protocal=(String)CommonMethods.retrieveVariable(hTable,"protocal");
//        boolean bValidProtocal=false;
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
        double samplingInterval=trace.fSampleInterval;

        int nChannelIndex;
        int nEpisodeI=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable, "episodeI:"));
        int nEpisodeF=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable, "episodeF:"));
        int nDeltaEpisode=Integer.parseInt((String)CommonMethods.retrieveVariable(hTable, "deltaEpisode:"));
        double dTimeI=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "timeI:"));
        double dLength=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "length:"));
        double ws=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "windowSize:"));
        double stepSize=Double.parseDouble((String)CommonMethods.retrieveVariable(hTable, "stepSize:"));
        String pathISigma=FileAssist.changeExt(abfPath, "txt");
        String sPar="_t0_"+PrintAssist.ToString(dTimeI, 0)+"_lenth_"+PrintAssist.ToString(dLength, 0)+"_ws_"+PrintAssist.ToString(ws, 0);
        pathISigma=FileAssist.getExtendedFileName(pathISigma, "_ISigma"+sPar);

        int nEpisodeLen=trace.nSamplesPerEpisode;
        int offset=0;
        int nChannels=trace.nNumChannels;
        int ntI=(int)(dTimeI/samplingInterval);
        int len=(int)(dLength/samplingInterval);
        len=nChannels*(len/nChannels);
        int ntF=ntI+len-1;

        int nStep=(int)(stepSize/samplingInterval);
        nStep=(nStep/nChannels)*nChannels;
        int nWs=(int)(ws/samplingInterval);
        nWs=nChannels*(nWs/nChannels);

        ArrayList<Integer> nvWindowPositions=new ArrayList();
        CommonMethods.getWindowPositions(ntI, ntF, nWs, nStep, nvWindowPositions);
        int nNum=nvWindowPositions.size();

        int nNumEpisodes=(nEpisodeF-nEpisodeI)/nDeltaEpisode+1;

        double[][] pdISigma;
        ArrayList<double[][]>  vpdISigma=new ArrayList();

        int position,i,row=0;
        double sd;
        MeanSem0 ms;
        for(nChannelIndex=0;nChannelIndex<nChannels;nChannelIndex++){
            pdISigma=new double [nNum*nNumEpisodes][4];
            row=0;
            for(int nEpisode=nEpisodeI;nEpisode<=nEpisodeF;nEpisode+=nDeltaEpisode){
                offset+=nEpisode*nEpisodeLen+nChannelIndex;
                for(i=0;i<nNum;i++){
                    position=nvWindowPositions.get(i);
                    ms=CommonStatisticsMethods.buildMeanSem(trace.pfData, offset+position-nWs, offset+position+nWs, nChannels);
                    pdISigma[row][0]=samplingInterval*(offset+position);
                    pdISigma[row][1]=ms.mean;
                    sd=ms.getSD();
                    pdISigma[row][2]=sd;
                    pdISigma[row][3]=sd*sd;
                    row++;
                }
            }
            vpdISigma.add(pdISigma);
        }

        ArrayList<String> titles=new ArrayList();
        for(i=0;i<nChannels;i++){
            titles.add("Time(ms)_"+i+"  ");
            titles.add("Current(pA)_"+i+"  ");
            titles.add("SD(pA)_"+i+"  ");
            titles.add("Vars(pA)^2_"+i+"  ");
        }

        ArrayList<Integer> precisions=new ArrayList();
        precisions.add(1);
        precisions.add(1);
        precisions.add(2);
        precisions.add(2);

        ArrayList<Integer> nvPrintLengthes=new ArrayList();

        Formatter fm=PrintAssist.getQuickFormatter(pathISigma);

        int pl,j;
        int index=0;
        for(i=0;i<nChannels;i++){
            pdISigma=vpdISigma.get(i);
            for(j=0;j<4;j++){
                pl=PrintAssist.getPrintgLength(pdISigma, j, precisions.get(j));
                if(pl<titles.get(index).length()) pl=titles.get(index).length();
                nvPrintLengthes.add(pl);
                index++;
            }
        }
        index=0;
        for(i=0;i<nChannels;i++){
            for(j=0;j<4;j++){
                PrintAssist.printString(fm, titles.get(index), nvPrintLengthes.get(index));
                index++;
            }
        }
        PrintAssist.endLine(fm);
        int rows=vpdISigma.get(0).length;
        for(row=0;row<rows;row++){
            index=0;
            for(i=0;i<nChannels;i++){
                pdISigma=vpdISigma.get(i);
                for(j=0;j<4;j++){
                    PrintAssist.printNumber(fm, pdISigma[row][j], nvPrintLengthes.get(index),precisions.get(j));
                    index++;
                }
            }
            PrintAssist.endLine(fm);
        }

        fm.close();
    }

    public void exportISigma_hipass(String pathScript){
        int nChannelIndex,nNumSegments,nNumFiles,i,j;
        ArrayList <Integer> nvStarts=new ArrayList(), nvLength=new ArrayList();
        double samplingInterval;
        float[] pfData;
        ArrayList<Double> dvI,dvIFitered,dvSigma,dvSigmaFiltered;
        String pathAbf, pathAbfFiltered, pathISigma;
        MeanSem0 ms,msf;
        StringTokenizer stk;
        AbfNode trace,tracef;

        BufferedReader br=AsciiInputAssist.getBufferedReader(pathScript);
        String line=AsciiInputAssist.readLine(br);

        Abf cAbf=new Abf(), cAbfF=new Abf();
        int nChannels;

        double dTimeI, dLen, sd,sdf,var0=0,var;
        int ntI, len;

        nNumFiles=AsciiInputAssist.getInt(new StringTokenizer(AsciiInputAssist.readLine(br)), 1);
        for(i=0;i<nNumFiles;i++){
            pathAbf=PrintAssist.TokenizeString(AsciiInputAssist.readLine(br),new String(" "),true).get(1);
            pathAbfFiltered=FileAssist.getExtendedFileName(pathAbf, "hif");
            pathISigma=FileAssist.getExtendedFileName(pathAbf, "_ISigma");
            pathISigma=FileAssist.changeExt(pathISigma, "txt");
            Formatter fm=PrintAssist.getQuickFormatter(pathISigma);

            nChannelIndex=AsciiInputAssist.getInt(new StringTokenizer(AsciiInputAssist.readLine(br)), 1);
            nNumSegments=AsciiInputAssist.getInt(new StringTokenizer(AsciiInputAssist.readLine(br)), 1);
            try{cAbf.ReadData(pathAbf);}
            catch(FileNotFoundException e){
                IJ.error("FileNotFound for reading abf in exportISigma_hipass");
            }
            catch(IOException e){
                IJ.error("IOException for reading abf in exportISigma_hipass");
            }

            trace=cAbf.GetTrace();
            tracef=cAbf.GetTrace();
            nChannels=trace.nNumChannels;

            PrintAssist.printString(fm, "I(pA)", 12);
            PrintAssist.printString(fm, "SD(pA)", 12);
            PrintAssist.printString(fm, "If(pA)", 12);
            PrintAssist.printString(fm, "SDf(pA)", 12);
            PrintAssist.printString(fm, "Var(pA^2)", 12);
            PrintAssist.printString(fm, "Var2(pA^2)", 12);
            PrintAssist.endLine(fm);
            samplingInterval=trace.fSampleInterval*nChannels/1000.;
            for(j=0;j<nNumSegments;j++){
                line=AsciiInputAssist.readLine(br);
                StringTokenizer stkt=new StringTokenizer(line);
                dTimeI=AsciiInputAssist.getDouble(stkt, 1);
                dLen=AsciiInputAssist.getDouble(stkt, 1);

                ntI=(int)(dTimeI/samplingInterval);
                ntI=nChannels*(ntI/nChannels)+nChannelIndex;
                len=(int)(dLen/samplingInterval);
                len/=nChannels;

                ms=CommonStatisticsMethods.buildMeanSem(trace.pfData, ntI, ntI+nChannels*len, nChannels);
                msf=CommonStatisticsMethods.buildMeanSem(tracef.pfData, ntI, ntI+nChannels*len, nChannels);
                sd=ms.getSD();
                sdf=msf.getSD();
                var=sdf*sdf;
                if(j==0) var0=var;

                PrintAssist.printNumber(fm, ms.mean, 12, 2);
                PrintAssist.printNumber(fm, sd, 12, 3);
                PrintAssist.printNumber(fm, msf.mean, 12, 2);
                PrintAssist.printNumber(fm, sdf, 12, 3);
                PrintAssist.printNumber(fm, var, 12, 2);
                PrintAssist.printNumber(fm, var-var0, 12, 3);
                PrintAssist.endLine(fm);
            }
            fm.close();
        }
    }
    public void multiplyChannels(int num){
        double[] pdData=CommonStatisticsMethods.copyArray(m_cAbf.m_pfData);
        int i,j,len=pdData.length,position;
        int shift=len/(num+1);
        for(i=0;i<len;i++){
            for(j=1;j<num;j++){
                position=CommonMethods.circularAddition(len, i, shift*j);
                m_cAbf.m_pfData[i]+=pdData[position];
            }
        }
    }
    public void squarePulses(int channel, int nMaxLen, double dAmp){
        int i,num=m_cAbf.getNumChannels();
        int sign=1,position=channel;
        float[] pdData=m_cAbf.m_pfData;
        int lent=pdData.length;
        boolean more=true;
        int len0=2;
        while(more){
            for(i=0;i<len0;i++){
                if(position>=lent) {
                    more=false;
                    break;
                }
                pdData[position]=(float)(sign*dAmp);
                position+=num;
            }
            sign*=-1;
            len0+=2;
            if(len0>nMaxLen) break;
        }
    }
}
