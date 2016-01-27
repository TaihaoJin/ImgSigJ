/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import java.util.ArrayList;
import utilities.io.PrintAssist;
import java.util.Formatter;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.IntPair;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.io.IOException;
import utilities.io.ReadInAssist;
//import ij.IJ;
import utilities.io.MessageAssist;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import utilities.CommonStatisticsMethods;
/**
 *
 * @author Taihao
 */
public class FittingResultsNodeo {
    public String sTitle;
    public ArrayList<double[]> pdvFittedPars;
    public ArrayList<double[]> pdvMaxDiffX;
    public ArrayList<double[]> pdvFittedY;
    public ArrayList<Double> dvMaxDiff;
    public ArrayList<Double> dvSSE;
    public ArrayList<Double> dvPValues;
    public ArrayList<String> svDescription=new ArrayList();
    public ArrayList<String> svExpression=new ArrayList();
    public ArrayList<String> svBaseParNames=new ArrayList();
    public ArrayList<String> svExpandedParNames=new ArrayList();
    public ArrayList<Integer> nvNumParameters=new ArrayList();//this array contains three elements. first: number of pars in base term, second: number of par per expanded ter, thirds: number of starting terms.
    public ArrayList<Integer> nvIterations=new ArrayList();
    public ArrayList<Integer> nvEvaluations=new ArrayList();
    public ArrayList<Double> dvElapsedTimes=new ArrayList();
    public ArrayList<DoubleRange> cvVarRanges=new ArrayList();
    public int MinimizationOption, MinimizationMethod,nVars,nDataPoints,nI,nF,nDelta;
    public double[][] m_pdX;
    public double[] m_pdY;
    public int nModels;
    ArrayList<Boolean> m_bvConverged;
    public FittingResultsNodeo(){
        pdvFittedPars=new ArrayList();
        pdvMaxDiffX=new ArrayList();
        dvMaxDiff=new ArrayList();
        dvSSE=new ArrayList();
        dvPValues=new ArrayList();
        pdvFittedY=new ArrayList();
        m_bvConverged=new ArrayList();

        svDescription=new ArrayList();
        svExpression=new ArrayList();
        svBaseParNames=new ArrayList();
        svExpandedParNames=new ArrayList();
        nvNumParameters=new ArrayList();
        nvIterations=new ArrayList();
        nvEvaluations=new ArrayList();
        dvElapsedTimes=new ArrayList();
        cvVarRanges=new ArrayList();
    }
    public Non_Linear_Fitter getFitter(){
        Non_Linear_Fitter fitter=new Non_Linear_Fitter();
        return fitter;
    }
    public void setTitle(String sTitle){
        this.sTitle=sTitle;
    }
    public void ExportFittingResults_SingleLine(Formatter fm){
        nModels=pdvFittedPars.size();
        int i;
        for(i=0;i<nModels;i++){
            PrintAssist.printString(fm, "fittedPars"+i);
            PrintAssist.printArraySingleLine(fm, pdvFittedPars.get(i), 4);
            PrintAssist.printString(fm, "MaxDiffX"+i);
            PrintAssist.printArraySingleLine(fm, pdvMaxDiffX.get(i), 4);
            PrintAssist.printString(fm, "MaxDiff"+i);
            PrintAssist.printNumber(fm, dvMaxDiff.get(i), 4);
            PrintAssist.printString(fm, "SSE"+i);
            PrintAssist.printNumber(fm, dvSSE.get(i), 4);
            PrintAssist.printString(fm, "pValue"+i);
            PrintAssist.printNumber(fm, dvPValues.get(i), 4);
        }
    }
    public void retrieveFittingResults(ArrayList<String> ParameterNames, ArrayList<Double> ParameterValues, ArrayList<String> ResultItems, ArrayList<Double> ResultValues, ArrayList<Integer> SpacerPositions,int nModel){
        double[] pdPars=pdvFittedPars.get(nModel);
        int nNumPars=pdPars.length;
        ArrayList<String[]> psvResults=getFittingResultsAsStringArray();
        String[] line=psvResults.get(1);
        String st;
        int cols=line.length;
        int len=psvResults.size(),i;
        int num=1;
        while(!isBlank(line)){
            ResultItems.add(line[0]);
            st=line[nModel+1];
            ResultValues.add(Double.parseDouble(st));
            num++;
            line=psvResults.get(num);
        }
        int nPars=0;
        String sName, sValue;
        for(i=num;i<len;i++){
            line=psvResults.get(i);
            if(!isBlank(line)){
                sName=line[0];
                sValue=line[nModel+1];
                ParameterNames.add(sName);
                ParameterValues.add(Double.parseDouble(sValue));
                nPars++;
                if(nPars>=nNumPars) break;
            }else{
                SpacerPositions.add(nPars);
            }
        }
    }
    boolean isBlank(String[] line){
        int i,j,len=line.length;
        String st;
        for(i=0;i<len;i++){
            st=line[i];
            if(!isWhightString(st)) return false;
        }

        return true;
    }
    boolean isWhightString(String st){
        int i,len=st.length();
        for(i=0;i<len;i++){
            if(st.charAt(i)!=' ') return false;
        }
        return true;
    }
    public ArrayList<String[]> getFittingResultsAsStringArray(){
        ArrayList<String[]> stringArray=new ArrayList();
        int nModels=pdvFittedPars.size();
        int i,j,nRows,num;
        num=svDescription.size();
        num=svExpression.size();

        int dataPrecision=getDataPrintingPrecision();
        ArrayList<Integer> nvVarPrecisions=getVarPrintingPrecisions();

        double[] pdPars=pdvFittedPars.get(nModels-1);
        int nPars=pdPars.length;
        int nNumBasePars=nvNumParameters.get(0);
        int nNumExpandedPars=nvNumParameters.get(1);
        int nNumStartingTerms=nvNumParameters.get(2);
        int nNumTerms=(nPars-1-nNumStartingTerms*(nNumBasePars-1))/nNumExpandedPars;

        double eT=0;
        int len=dvElapsedTimes.size();
        for(i=0;i<len;i++){
            eT+=dvElapsedTimes.get(i);
        }

        ArrayList<String> svRowTitles=new ArrayList();
        ArrayList<String> svColumnTitles=new ArrayList();
        ArrayList<Integer> nvPrecisions=new ArrayList();
        ArrayList<IntPair> spacerLines=new ArrayList();

        svRowTitles.add("Models:");
        svRowTitles.add("SSEs");
        nvPrecisions.add(2);
        svRowTitles.add("MaxDiff");
        nvPrecisions.add(2);
//        int nVars=pdvMaxDiffX.get(0).length;
        for(i=0;i<nVars;i++){
            svRowTitles.add("MaxDiffX"+i);
            nvPrecisions.add(nvVarPrecisions.get(i));
        }
        nRows=2+nVars;
        for(i=0;i<nVars;i++){
            svRowTitles.add("MinX"+i);
            nvPrecisions.add(nvVarPrecisions.get(i));
            svRowTitles.add("MaxX"+i);
            nvPrecisions.add(nvVarPrecisions.get(i));
        }
        nRows+=2*nVars;
        svRowTitles.add("pValues");
        nvPrecisions.add(5);
        nRows++;
        svRowTitles.add("Iterations");
        nvPrecisions.add(0);
        nRows++;
        svRowTitles.add("Evaluations");
        nvPrecisions.add(0);
        nRows++;
        svRowTitles.add("Elapsed-T(s)");
        nvPrecisions.add(3);
        nRows++;
        spacerLines.add(new IntPair(nRows,1));

//        nRows+=nPars;

        svRowTitles.add(svBaseParNames.get(0));
        nRows++;
        nvPrecisions.add(4);

        int o=1;
        len=svBaseParNames.size();
        for(i=0;i<nNumStartingTerms;i++){
            for(j=1;j<len;j++){
                svRowTitles.add(svBaseParNames.get(j)+i);
                nRows++;
                nvPrecisions.add(4);
            }
            if(i<nNumStartingTerms-1)spacerLines.add(new IntPair(nRows,1));
        }
        spacerLines.add(new IntPair(nRows,2));
        for(i=0;i<nNumTerms;i++){
            for(j=0;j<nNumExpandedPars;j++){
                svRowTitles.add(svExpandedParNames.get(j)+i);
                nvPrecisions.add(4);
                nRows++;
            }
            o+=nNumExpandedPars;
            spacerLines.add(new IntPair(nRows,1));
        }
        double pdParsT[][]=new double[nRows][nModels];
        for(i=0;i<nModels;i++){
            svColumnTitles.add("Model"+i);
            pdParsT[0][i]=dvSSE.get(i);
            pdParsT[1][i]=dvMaxDiff.get(i);
            o=2;
            for(j=0;j<nVars;j++){
                pdParsT[o][i]=pdvMaxDiffX.get(i)[j];
                o++;
            }
            for(j=0;j<nVars;j++){
                pdParsT[o][i]=cvVarRanges.get(j).getMin();
                o++;
                pdParsT[o][i]=cvVarRanges.get(j).getMax();
                o++;
            }
            pdParsT[o][i]=dvPValues.get(i);
            o++;
            pdParsT[o][i]=nvIterations.get(i);
            o++;
            pdParsT[o][i]=nvEvaluations.get(i);
            o++;
            pdParsT[o][i]=dvElapsedTimes.get(i);
            o++;
            pdPars=pdvFittedPars.get(i);
            nPars=pdPars.length;
            for(j=0;j<nPars;j++){
                pdParsT[o][i]=pdPars[j];
                o++;
            }
        }
        stringArray=PrintAssist.ToStringArrays(svRowTitles, svColumnTitles, nvPrecisions,spacerLines,false,pdParsT);
        return stringArray;
    }

    public void ExportFittingResults_MultipleLines(Formatter fm){
        int nModels=pdvFittedPars.size();
        int i,j,nRows,num;
        PrintAssist.printString(fm, "Fitting_Function:");
        num=svDescription.size();
        for(i=0;i<num;i++){
            PrintAssist.printString(fm, svDescription.get(i));
        }
        PrintAssist.endLine(fm);
        num=svExpression.size();
        for(i=0;i<num;i++){
            PrintAssist.printString(fm, svExpression.get(i));
        }
        PrintAssist.endLine(fm);

        int dataPrecision=getDataPrintingPrecision();
        ArrayList<Integer> nvVarPrecisions=getVarPrintingPrecisions();

        double[] pdPars=pdvFittedPars.get(nModels-1);
        int nPars=pdPars.length;
        int nNumBasePars=nvNumParameters.get(0);
        int nNumExpandedPars=nvNumParameters.get(1);
        int nNumStartingTerms=nvNumParameters.get(2);
        int nNumTerms=(nPars-1-nNumStartingTerms*(nNumBasePars-1))/nNumExpandedPars;

        PrintAssist.printString(fm, "OptimizationMethod: "+Non_Linear_Fitter.getMinimizationMethodName(MinimizationMethod));
        PrintAssist.printString(fm, "  OptimizationOption: "+Non_Linear_Fitter.getMinimizationOptionName(MinimizationOption));
        PrintAssist.printString(fm, "  NumVariables: ");
        PrintAssist.printNumber(fm, nVars, 0);
        PrintAssist.printString(fm, "  nNumBasePars: ");
        PrintAssist.printNumber(fm, nNumBasePars, 0);
        PrintAssist.printString(fm, "  nNumExpandedPars: ");
        PrintAssist.printNumber(fm, nNumExpandedPars, 0);
        PrintAssist.printString(fm, "  nNumStartingTerms: ");
        PrintAssist.printNumber(fm, nNumStartingTerms, 0);
        PrintAssist.printString(fm, "  nNumTerms: ");
        PrintAssist.printNumber(fm, nNumTerms, 0);
        PrintAssist.endLine(fm);
        PrintAssist.printString(fm, "  DataPoints: ");
        PrintAssist.printNumber(fm, nDataPoints, 0);
        PrintAssist.printString(fm, "  FirstPoint: ");
        PrintAssist.printNumber(fm, nI, 0);
        PrintAssist.printString(fm, "  LastPoint: ");
        PrintAssist.printNumber(fm, nF, 0);
        PrintAssist.printString(fm, "  Delta: ");
        PrintAssist.printNumber(fm, nDelta, 0);
        double eT=0;
        int len=dvElapsedTimes.size();
        for(i=0;i<len;i++){
            eT+=dvElapsedTimes.get(i);
        }
        PrintAssist.printString(fm, "  ElapsedTime(s): ");
        PrintAssist.printNumber(fm, eT, 3);
        PrintAssist.endLine(fm);
        PrintAssist.endLine(fm);

        ArrayList<String> svRowTitles=new ArrayList();
        ArrayList<String> svColumnTitles=new ArrayList();
        ArrayList<Integer> nvPrecisions=new ArrayList();
        ArrayList<IntPair> spacerLines=new ArrayList();

        svRowTitles.add("Models:");
        svRowTitles.add("SSEs");
        nvPrecisions.add(2);
        svRowTitles.add("MaxDiff");
        nvPrecisions.add(2);
//        int nVars=pdvMaxDiffX.get(0).length;
        for(i=0;i<nVars;i++){
            svRowTitles.add("MaxDiffX"+i);
            nvPrecisions.add(nvVarPrecisions.get(i));
        }
        nRows=2+nVars;
        for(i=0;i<nVars;i++){
            svRowTitles.add("MinX"+i);
            nvPrecisions.add(nvVarPrecisions.get(i));
            svRowTitles.add("MaxX"+i);
            nvPrecisions.add(nvVarPrecisions.get(i));
        }
        nRows+=2*nVars;
        svRowTitles.add("pValues");
        nvPrecisions.add(5);
        nRows++;
        svRowTitles.add("Iterations");
        nvPrecisions.add(0);
        nRows++;
        svRowTitles.add("Evaluations");
        nvPrecisions.add(0);
        nRows++;
        svRowTitles.add("Elapsed-T(s)");
        nvPrecisions.add(3);
        nRows++;
        spacerLines.add(new IntPair(nRows,1));

//        nRows+=nPars;

        svRowTitles.add(svBaseParNames.get(0));
        nRows++;
        nvPrecisions.add(4);

        int o=1;
        len=svBaseParNames.size();
        for(i=0;i<nNumStartingTerms;i++){
            for(j=1;j<len;j++){
                svRowTitles.add(svBaseParNames.get(j)+i);
                nRows++;
                nvPrecisions.add(4);
            }
            if(i<nNumStartingTerms-1)spacerLines.add(new IntPair(nRows,1));
        }
        spacerLines.add(new IntPair(nRows,2));
        for(i=0;i<nNumTerms;i++){
            for(j=0;j<nNumExpandedPars;j++){
                svRowTitles.add(svExpandedParNames.get(j)+i);
                nvPrecisions.add(4);
                nRows++;
            }
            o+=nNumExpandedPars;
            spacerLines.add(new IntPair(nRows,1));
        }
        double pdParsT[][]=new double[nRows][nModels];
        for(i=0;i<nModels;i++){
            svColumnTitles.add("Model"+i);
            pdParsT[0][i]=dvSSE.get(i);
            pdParsT[1][i]=dvMaxDiff.get(i);
            o=2;
            for(j=0;j<nVars;j++){
                pdParsT[o][i]=pdvMaxDiffX.get(i)[j];
                o++;
            }
            for(j=0;j<nVars;j++){
                pdParsT[o][i]=cvVarRanges.get(j).getMin();
                o++;
                pdParsT[o][i]=cvVarRanges.get(j).getMax();
                o++;
            }
            pdParsT[o][i]=dvPValues.get(i);
            o++;
            pdParsT[o][i]=nvIterations.get(i);
            o++;
            pdParsT[o][i]=nvEvaluations.get(i);
            o++;
            pdParsT[o][i]=dvElapsedTimes.get(i);
            o++;
            pdPars=pdvFittedPars.get(i);
            nPars=pdPars.length;
            for(j=0;j<nPars;j++){
                pdParsT[o][i]=pdPars[j];
                o++;
            }
        }
        PrintAssist.printArrays(fm, svRowTitles, svColumnTitles, nvPrecisions,spacerLines,false,pdParsT);
        PrintAssist.endLine(fm);
        PrintAssist.endLine(fm);
        PrintAssist.printString(fm, "DataPoints");
        PrintAssist.endLine(fm);

        for(i=0;i<nDataPoints;i++){
            for(j=0;j<nVars;j++){
                PrintAssist.printNumber(fm, m_pdX[i][j], nvVarPrecisions.get(j));
            }
            PrintAssist.printNumber(fm, m_pdY[i], dataPrecision);
        }
    }

    public void ImportFittingResults_MultipleLines(String path){
        File file = new File(path);
        FileReader f=null;
        try{
            f=new FileReader(file);
        }
        catch (FileNotFoundException e){
            MessageAssist.error(e+" in ImportFittingResults_MultipleLines(String path)");
        }
        BufferedReader br=new BufferedReader(f);
        try{
            ImportFittingResults_MultipleLines(br);
        }
        catch (IOException e){
            MessageAssist.error("IOException when import FittingResults");
        }
    }


    public int ImportFittingResults_MultipleLines(BufferedReader br) throws IOException{

        int status=1;
        int i,j;

        svDescription.clear();
        String sLine,sType,st;
        sLine=ReadInAssist.gotoLine_contains(br,"Fitting_Function:");
        if(sLine==null) {
//            MessageAssist.error("the file does not contain Fitting_Function: for ImportFittingResults_MultipleLines");
            return -1;//passing the satus to the caller to handle the unsuccesful reading.
        }
        StringTokenizer stk=new StringTokenizer(sLine);
        sType=stk.nextToken();
        sType=stk.nextToken();
        svDescription.add(sType);

        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        svExpression.add(st);

        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        st=stk.nextToken();
        MinimizationMethod=Non_Linear_Fitter.getMinimizationMethod(st);

        st=stk.nextToken();
        st=stk.nextToken();
        MinimizationOption=Non_Linear_Fitter.getMinimizationOption(st);

        st=stk.nextToken();
        st=stk.nextToken();
        nVars=Integer.parseInt(st);

        st=stk.nextToken();
        st=stk.nextToken();
        nvNumParameters.add(Integer.parseInt(st));

        st=stk.nextToken();
        st=stk.nextToken();
        nvNumParameters.add(Integer.parseInt(st));

        st=stk.nextToken();
        st=stk.nextToken();
        nvNumParameters.add(Integer.parseInt(st));

        st=stk.nextToken();
        st=stk.nextToken();
        int nTerms=Integer.parseInt(st);
        nModels=nTerms+1;
        
        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        st=stk.nextToken();
        nDataPoints=Integer.parseInt(st);
        st=stk.nextToken();
        st=stk.nextToken();
        nI=Integer.parseInt(st);
        st=stk.nextToken();
        st=stk.nextToken();
        nF=Integer.parseInt(st);
        st=stk.nextToken();
        st=stk.nextToken();
        nDelta=Integer.parseInt(st);

        sLine=ReadInAssist.gotoLine_Contains(br, "SSEs");
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();

        for(i=0;i<nModels;i++){
            st=stk.nextToken();
            dvSSE.add(Double.parseDouble(st));
        }

        nModels=nTerms+1;

        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            st=stk.nextToken();
            dvMaxDiff.add(Double.parseDouble(st));
        }

        for(i=0;i<nModels;i++){
            pdvMaxDiffX.add(new double[nVars]);
            if(i<nVars) cvVarRanges.add(new DoubleRange());
        }
        double[] pdTemp;

        for(i=0;i<nVars;i++){
            sLine=br.readLine();
            stk=new StringTokenizer(sLine);
            st=stk.nextToken();
            for(j=0;j<nModels;j++){
                st=stk.nextToken();
                pdvMaxDiffX.get(j)[i]=Double.parseDouble(st);
            }
        }

        for(i=0;i<nVars;i++){
            sLine=br.readLine();
            stk=new StringTokenizer(sLine);
            st=stk.nextToken();
            st=stk.nextToken();
            cvVarRanges.get(i).expandRange(Double.parseDouble(st));

            sLine=br.readLine();
            stk=new StringTokenizer(sLine);
            st=stk.nextToken();
            st=stk.nextToken();
            cvVarRanges.get(i).expandRange(Double.parseDouble(st));
        }

        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            st=stk.nextToken();
            dvPValues.add(Double.parseDouble(st));
        }

        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            st=stk.nextToken();
            nvIterations.add(Integer.parseInt(st));
        }

        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            st=stk.nextToken();
            nvEvaluations.add(Integer.parseInt(st));
        }

        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            st=stk.nextToken();
            dvElapsedTimes.add(Double.parseDouble(st));
        }

        int nPars;
        int nNumBasePars=nvNumParameters.get(0);
        int nNumExpandedPars=nvNumParameters.get(1);
        int nNumStartingTerms=nvNumParameters.get(2);

        ArrayList<Integer> nvPars=new ArrayList();
        for(i=0;i<nModels;i++){
            nTerms=i;
            nPars=nNumStartingTerms*nNumBasePars+nTerms*nNumExpandedPars;
            pdvFittedPars.add(new double[nPars]);
            nvPars.add(nPars);
        }

        double[] pdPars;
 
        nTerms=nModels-1;
        int nParMax=nNumStartingTerms*nNumBasePars+nTerms*nNumExpandedPars;

        int nLines=0;
        while(nLines<nParMax){
            sLine=br.readLine();
            stk=new StringTokenizer(sLine);
            if(!stk.hasMoreTokens()) continue;//an empty spacer line
            st=stk.nextToken();
            for(i=0;i<nModels;i++){
                nPars=nvPars.get(i);
                st=stk.nextToken();
                if(nLines<nPars) pdvFittedPars.get(i)[nLines]=Double.parseDouble(st);
            }
            nLines++;
        }

        sLine=ReadInAssist.gotoLine_contains(br, "DataPoints");
        if(sLine==null) {
            MessageAssist.error("the file does not contain DataPoints for ImportFittingResults_MultipleLines");
            return -1;
        }
        sLine=br.readLine();
        stk=new StringTokenizer(sLine);
        m_pdX=new double[nDataPoints][nVars];
        m_pdY=new double[nDataPoints];
        for(i=0;i<nDataPoints;i++){
            for(j=0;j<nVars;j++){
                st=stk.nextToken();
                m_pdX[i][j]=Double.parseDouble(st);
            }
            st=stk.nextToken();
            m_pdY[i]=Double.parseDouble(st);
        }
        retrieveParNames();
        return status;
    }
    ArrayList<Integer> getVarPrintingPrecisions(){
        int nVars=m_pdX[0].length,i,precision,len=m_pdX.length-1;
        ArrayList<Integer> precisions=new ArrayList();
        double accuracy=0.0001;
        for(i=0;i<nVars;i++){
            precision=PrintAssist.getPrintingPrecision_Column(m_pdX, i, nI, nF, nDelta, accuracy);
            precisions.add(precision);
        }
        return precisions;
    }
    int getDataPrintingPrecision(){
        double accuracy=0.0001;
        return PrintAssist.getPrintingPrecisionF(m_pdY, nI, nF, nDelta, accuracy);
    }
    void retrieveParNames(){
        StandardFittingFunction func;
        String sFuncType=svDescription.get(0);
        func=new StandardFittingFunction(sFuncType);
        func.getDescriptions(null, null, svBaseParNames, svExpandedParNames, null);
    }
}
