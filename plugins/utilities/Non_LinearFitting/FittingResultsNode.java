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
import utilities.Non_LinearFitting.FittingModelNode;
/**
 *
 * @author Taihao
 */
public class FittingResultsNode {
    public String sTitle;
    public ArrayList<FittingModelNode> m_cvModels;
    public ArrayList<Double> m_dvPValues;
    public ArrayList<DoubleRange> cvVarRanges=new ArrayList();
    public double[][] m_pdX;
    public double[] m_pdY;
    public int nModels;
    double m_dElapsedTime;
    public double pCutoff;
    int nVars;
    public FittingResultsNode(){
        m_cvModels=new ArrayList();
        cvVarRanges=new ArrayList();
        m_dvPValues=new ArrayList();
    }
    public FittingResultsNode(FittingModelNode aModel){
        this();
        m_cvModels.add(aModel);
        cvVarRanges=aModel.cvVarRanges;
        m_dvPValues.add(0.);
        m_pdX=aModel.m_pdX;
        m_pdY=aModel.m_pdY;
        nModels=m_cvModels.size();
        m_dElapsedTime=aModel.dElapsedTimes;
    }
    public FittingResultsNode(FittingResultsNode aNode, int nModelI, int nModelF, int nDelta, boolean copyData){
        this();
        int i;
        cvVarRanges=CommonStatisticsMethods.copyDoubleRangeArray(aNode.cvVarRanges);
        m_dElapsedTime=0.;
        FittingModelNode aModel;
        if(copyData){
            m_pdX=CommonStatisticsMethods.copyArray(aNode.m_pdX);
            m_pdY=CommonStatisticsMethods.copyArray(aNode.m_pdY);
        }else{
            m_pdX=aNode.m_pdX;
            m_pdY=aNode.m_pdY;
        }
        for(i=nModelI;i<=nModelF;i++){
            aModel=aNode.m_cvModels.get(i);
            m_cvModels.add(new FittingModelNode(aModel));
            m_dElapsedTime+=aModel.dElapsedTimes;
            m_dvPValues.add(aNode.m_dvPValues.get(i));
        }
        nModels=m_cvModels.size();
        nVars=aNode.nVars;
        sTitle="";
    }
    public Non_Linear_Fitter getFitter(){
        Non_Linear_Fitter fitter=new Non_Linear_Fitter();
        return fitter;
    }
    public void setTitle(String sTitle){
        this.sTitle=sTitle;
    }
    public void retrieveFittingResults(ArrayList<String> ParameterNames, ArrayList<Double> ParameterValues, ArrayList<String> ResultItems, ArrayList<Double> ResultValues, ArrayList<Integer> nvNumPars,int nModel){
        FittingModelNode aNode=m_cvModels.get(nModel);
        double[] pdPars=m_cvModels.get(nModel).pdPars;
        CommonStatisticsMethods.copyStringArray(aNode.svParNames, ParameterNames);
        int i,nNumPars=pdPars.length;

        for(i=0;i<nNumPars;i++){
            ParameterValues.add(pdPars[i]);
        }

        nvNumPars.clear();
        for(i=0;i<aNode.nvNumParameters.size();i++){
            nvNumPars.add(aNode.nvNumParameters.get(i));
        }

        ResultItems.add("SSE");
        ResultValues.add(aNode.SSE);
        ResultItems.add("MaxDiff");
        ResultValues.add(aNode.dMaxDiff);
        int nVars=aNode.nVars;

        for(i=0;i<nVars;i++){
            ResultItems.add("MaxDiffX"+i);
            ResultValues.add(aNode.pdMaxDiffX[i]);
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
        ResultValues.add(m_dvPValues.get(nModel));
        ResultItems.add("Iterations");
        ResultValues.add((double)aNode.nIterations);
        ResultItems.add("Evaluations");
        ResultValues.add((double)aNode.nEvaluations);
        ResultItems.add("Elapsed-T(s)");
        ResultValues.add(aNode.dElapsedTimes);
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
        int nModels=m_cvModels.size();
        int i,j,nRows,nHeadingLines;
        if(nModels==0){
            nModels=nModels;
        }
        FittingModelNode lastModel=m_cvModels.get(nModels-1);

        int dataPrecision=getDataPrintingPrecision();
        ArrayList<Integer> nvVarPrecisions=getVarPrintingPrecisions();

        double[] pdPars=m_cvModels.get(nModels-1).pdPars;
        int nPars=pdPars.length,o;

        double eT=0;
        int len=nModels;
        for(i=0;i<len;i++){
            eT+=m_cvModels.get(i).dElapsedTimes;
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

        int nVars=m_pdX[0].length;
        for(i=0;i<nVars;i++){
            svRowTitles.add("MaxDiffX"+i);
            nvPrecisions.add(nvVarPrecisions.get(i));
        }
        nRows=2+nVars;//nRows is the number of rows for numbers. It does not include the row for column titles.
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

        ArrayList<String> svParNames=m_cvModels.get(nModels-1).svParNames;
        nPars=svParNames.size();

        nHeadingLines=nRows;
        spacerLines.add(new IntPair(nRows,2));

        int nComponents=lastModel.nvNumParameters.size();

        int num=0;
        svRowTitles.add(svParNames.get(num));
        nvPrecisions.add(4);
        num++;
        nRows++;//these lines are for the constant term

        for(i=0;i<nComponents;i++){
            for(j=0;j<lastModel.nvNumParameters.get(i);j++){
                svRowTitles.add(svParNames.get(num));
                nvPrecisions.add(4);
                num++;
                nRows++;
            }
            spacerLines.add(new IntPair(nRows,1));
        }

//        nRows+=nPars;


        double pdParsT[][]=new double[nRows][nModels];
        FittingModelNode model;
        for(i=0;i<nModels;i++){
            model=m_cvModels.get(i);
            svColumnTitles.add("Model"+i);
            pdParsT[0][i]=model.SSE;
            pdParsT[1][i]=model.dMaxDiff;
            o=2;
            for(j=0;j<nVars;j++){
                pdParsT[o][i]=model.pdMaxDiffX[j];
                o++;
            }
            for(j=0;j<nVars;j++){
                pdParsT[o][i]=cvVarRanges.get(j).getMin();
                o++;
                pdParsT[o][i]=cvVarRanges.get(j).getMax();
                o++;
            }
            pdParsT[o][i]=m_dvPValues.get(i);
            o++;
            pdParsT[o][i]=model.nIterations;
            o++;
            pdParsT[o][i]=model.nEvaluations;
            o++;
            pdParsT[o][i]=model.dElapsedTimes;
            o++;
            pdPars=model.pdPars;
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
        int nModels=m_cvModels.size(),nDataPoints=m_pdX.length;

        int dataPrecision=getDataPrintingPrecision();
        ArrayList<Integer> nvVarPrecisions=getVarPrintingPrecisions();

        int i,j,nRows,num,index;
        PrintAssist.printString(fm, "Fitting results: "+sTitle);
        PrintAssist.endLine(fm);
        PrintAssist.printString(fm, ""+nDataPoints+" data Points");
        PrintAssist.printString(fm, ""+nModels+" models    "+m_pdX[0].length+" variables");
        double eT=0;

        for(i=0;i<nModels;i++){
            eT+=m_cvModels.get(i).dElapsedTimes;
        }
        PrintAssist.printString(fm, "   ElapsedTime(s): ");
        PrintAssist.printNumber(fm, eT, 3);
        PrintAssist.endLine(fm);

        PrintAssist.endLine(fm);
        PrintAssist.endLine(fm);
        FittingModelNode model;
        String indentation="    ";
        for(i=0;i<nModels;i++){
            model=m_cvModels.get(i);
            model.toGaussian2D_GaussianPars();
            num=model.svFunctionTypes.size();
            PrintAssist.printString(fm, "Model"+i+":  Components: "+num+"  Parameters: "+model.nNumPars+"  Y="+model.sExpression);
            PrintAssist.endLine(fm);

            PrintAssist.printString(fm, indentation);
            PrintAssist.printString(fm, "Functin types:");
            for(j=0;j<num;j++){
                PrintAssist.printString(fm, model.svFunctionTypes.get(j)+";");
            }
            PrintAssist.endLine(fm);

            PrintAssist.printString(fm, indentation);

            if(model.pnFixedParIndexes!=null)
                num=model.pnFixedParIndexes.length;
            else
                num=0;
            
            PrintAssist.printString(fm, "Fixed pars: "+num);
            for(j=0;j<num;j++){
                index=model.pnFixedParIndexes[j];
                PrintAssist.printString(fm, index+" "+model.svParNames.get(index));
            }
            PrintAssist.printString(fm, "OptimizationMethod: "+Non_Linear_Fitter.getMinimizationMethodName(model.MinimizationMethod));
            PrintAssist.printString(fm, "  OptimizationOption: "+Non_Linear_Fitter.getMinimizationOptionName(model.MinimizationOption));

            PrintAssist.printString(fm, "  FirstPoint: ");
            PrintAssist.printNumber(fm, model.nI, 0);
            PrintAssist.printString(fm, "  LastPoint: ");
            PrintAssist.printNumber(fm, model.nF, 0);
            PrintAssist.printString(fm, "  Delta: ");
            PrintAssist.printNumber(fm, model.nDelta, 0);
            PrintAssist.endLine(fm);
            PrintAssist.endLine(fm);
        }

        ArrayList<String[]> stringArray=getFittingResultsAsStringArray();
        String[] line;
        int lines=stringArray.size();
        for(i=0;i<lines;i++){
            line=stringArray.get(i);
            for(j=0;j<line.length;j++){
                PrintAssist.printString(fm,line[j]);
            }
            PrintAssist.endLine(fm);
        }

        PrintAssist.endLine(fm);
        int nVars=m_pdX[0].length;
        PrintAssist.printString(fm, "DataPoints: "+nDataPoints+" Points");
        PrintAssist.endLine(fm);

        for(i=0;i<nDataPoints;i++){
            for(j=0;j<nVars;j++){
                PrintAssist.printNumber(fm, m_pdX[i][j], nvVarPrecisions.get(j));
            }
            PrintAssist.printNumber(fm, m_pdY[i], dataPrecision);
            if((i+1)%10==0) PrintAssist.endLine(fm);
        }
        PrintAssist.endLine(fm);

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
        FittingModelNode model;//changed on 11802
        int status=1;
        int i,j,index,len,nPars=0,nParsX=0,nDataPoints;

        String sLine,sType,st;
        sLine=ReadInAssist.gotoLine_contains(br,"Fitting results");
        if(sLine==null) {
//            MessageAssist.error("the file does not contain Fitting_Function: for ImportFittingResults_MultipleLines");
            return -1;//passing the satus to the caller to handle the unsuccesful reading.
        }
        index=sLine.indexOf(':');
        sTitle=sLine.substring(index+2);

        sLine=ReadInAssist.nextNoneBlankLine(br);
        StringTokenizer stk=new StringTokenizer(sLine);

        nDataPoints=Integer.parseInt(stk.nextToken());
        stk.nextElement();
        stk.nextElement();

        st=stk.nextToken();
        nModels=Integer.parseInt(st);
        st=stk.nextToken();
        
        st=stk.nextToken();
        nVars=Integer.parseInt(st);
        st=stk.nextToken();

        st=stk.nextToken();
        st=stk.nextToken();
        m_dElapsedTime=Double.parseDouble(st);

        for(i=0;i<nVars;i++){
            cvVarRanges.add(new DoubleRange());
        }

        m_pdX=new double[nDataPoints][nVars];
        m_pdY=new double[nDataPoints];
        for(i=0;i<nModels;i++){
            model=new FittingModelNode();
            model.svParNames.clear();//has a constant upon construction, but will be read in here (11802)
            m_cvModels.add(model);


            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine," :");
            st=stk.nextToken();
            st=stk.nextToken();
            st=stk.nextToken();
            model.nComponents=Integer.parseInt(st);

            st=stk.nextToken();
            st=stk.nextToken();
            nPars=Integer.parseInt(st);
            model.nNumPars=nPars;
            model.pdPars=new double[nPars];
            if(nPars>nParsX) nParsX=nPars;

            model.sExpression=stk.nextToken();
            
            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine," :;");
            st=stk.nextToken();
            st=stk.nextToken();
            for(j=0;j<model.nComponents;j++){
                model.svFunctionTypes.add(st=stk.nextToken());
            }

            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine," :");
            stk.nextToken();
            stk.nextToken();
            len=Integer.parseInt(stk.nextToken());
            if(len==0){
                model.pnFixedParIndexes=null;
            }else{
                model.pnFixedParIndexes=new int[len];
                for(j=0;j<len;j++){
                    model.pnFixedParIndexes[j]=Integer.parseInt(stk.nextToken());
                    stk.nextToken();
                }
            }
            st=stk.nextToken();
            st=stk.nextToken();
            model.MinimizationMethod=Non_Linear_Fitter.getMinimizationMethod(st);

            st=stk.nextToken();
            st=stk.nextToken();
            model.MinimizationOption=Non_Linear_Fitter.getMinimizationOption(st);

            st=stk.nextToken();
            st=stk.nextToken();
            model.nI=Integer.parseInt(st);

            st=stk.nextToken();
            st=stk.nextToken();
            model.nF=Integer.parseInt(st);

            st=stk.nextToken();
            st=stk.nextToken();
            model.nDelta=Integer.parseInt(st);
            model.pdFittedY=new double[nDataPoints];
            model.m_pdX=m_pdX;
            model.m_pdY=m_pdY;
            model.pdMaxDiffX=new double[nVars];
            model.nvNumParameters=ComposedFittingFunction.getNumParsArray(model.svFunctionTypes);
            model.nVars=m_pdX[0].length;
            model.nDataPoints=nDataPoints;
            model.cvVarRanges=cvVarRanges;
        }

        sLine=ReadInAssist.gotoLine_Contains(br, "SSEs");
        stk=new StringTokenizer(sLine);

        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            m_cvModels.get(i).SSE=Double.parseDouble(stk.nextToken());
        }

        sLine=ReadInAssist.nextNoneBlankLine(br);
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            m_cvModels.get(i).dMaxDiff=Double.parseDouble(stk.nextToken());
        }

        for(i=0;i<nVars;i++){
            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine);
            st=stk.nextToken();
            for(j=0;j<nModels;j++){
                m_cvModels.get(j).pdMaxDiffX[i]=Double.parseDouble(stk.nextToken());
            }
        }

        for(i=0;i<nVars;i++){
            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine);
            st=stk.nextToken();
            cvVarRanges.get(i).expandRange(Double.parseDouble(stk.nextToken()));

            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine);
            st=stk.nextToken();
            cvVarRanges.get(i).expandRange(Double.parseDouble(stk.nextToken()));
        }

        sLine=ReadInAssist.nextNoneBlankLine(br);
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            m_dvPValues.add(Double.parseDouble(stk.nextToken()));
        }

        sLine=ReadInAssist.nextNoneBlankLine(br);
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            m_cvModels.get(i).nIterations=Integer.parseInt(stk.nextToken());
        }

        sLine=ReadInAssist.nextNoneBlankLine(br);
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            m_cvModels.get(i).nEvaluations=Integer.parseInt(stk.nextToken());
        }

        sLine=ReadInAssist.nextNoneBlankLine(br);
        stk=new StringTokenizer(sLine);
        st=stk.nextToken();
        for(i=0;i<nModels;i++){
            m_cvModels.get(i).dElapsedTimes=Double.parseDouble(stk.nextToken());
        }

        nPars=0;
        String sParName;
        double dPar;
        while(nPars<nParsX){
            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine);
            sParName=stk.nextToken();
            for(i=0;i<nModels;i++){
                model=m_cvModels.get(i);
                st=stk.nextToken();
                if(model.nNumPars>nPars){
                    model.svParNames.add(new String(sParName));
                    model.pdPars[nPars]=Double.parseDouble(st);
                }
            }
            nPars++;
        }

        sLine=ReadInAssist.gotoLine_contains(br, "DataPoints");
        if(sLine==null) {
            MessageAssist.error("the file does not contain DataPoints for ImportFittingResults_MultipleLines");
            return -1;
        }
        stk=new StringTokenizer(sLine);
        stk.nextToken();
        nDataPoints=Integer.parseInt(stk.nextToken());

        int num=0;
        while(num<nDataPoints){
            sLine=ReadInAssist.nextNoneBlankLine(br);
            stk=new StringTokenizer(sLine);
            while(stk.hasMoreTokens()){
                for(i=0;i<nVars;i++){
                    m_pdX[num][i]=Double.parseDouble(stk.nextToken());
                }
                m_pdY[num]=Double.parseDouble(stk.nextToken());
                num++;
            }
        }
        return status;
    }
    ArrayList<Integer> getVarPrintingPrecisions(){
        int nVars=m_pdX[0].length,i,precision,len=m_pdX.length-1;
        ArrayList<Integer> precisions=new ArrayList();
        double accuracy=0.0001;
        int nF=m_pdX.length-1;
        for(i=0;i<nVars;i++){
            precision=PrintAssist.getPrintingPrecision_Column(m_pdX, i, 0, nF, 1, accuracy);
            precisions.add(precision);
        }
        return precisions;
    }
    int getDataPrintingPrecision(){
        double accuracy=0.0001;
        int nF=m_pdX.length-1;
        return PrintAssist.getPrintingPrecisionF(m_pdY, 0, nF, 1, accuracy);
    }
    ArrayList<String> retrieveParNames(int nModel){
        return m_cvModels.get(nModel).svParNames;
    }
    public void addModel(FittingModelNode aNode){
        m_cvModels.add(aNode);
        nModels=m_cvModels.size();
        m_dElapsedTime+=aNode.dElapsedTimes;
    }
    public ArrayList<double[]> getFittedPars_allModels(){
        ArrayList<double[]> dvPars=new ArrayList();
        for(int i=0;i<nModels;i++){
            dvPars.add(m_cvModels.get(i).pdPars);
        }
        return dvPars;
    }
    public FittingModelNode getFittedModel(){
        nModels=m_cvModels.size();
        int nIters=m_dvPValues.size();
        if(m_dvPValues.get(nIters-1) >pCutoff)
            return m_cvModels.get(nModels-2);
        else
            return m_cvModels.get(nModels-1);
    }
}
