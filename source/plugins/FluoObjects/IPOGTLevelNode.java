/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import org.apache.commons.math.stat.regression.SimpleRegression;
import java.util.ArrayList;
import utilities.statistics.Histogram;
import java.io.*;
import utilities.io.ByteConverter;
import utilities.io.PrintAssist;
import java.awt.Point;
import utilities.CommonGuiMethods;
import utilities.CommonStatisticsMethods;
import utilities.statistics.LineSegmentNode;
import utilities.CustomDataTypes.intRange;
import utilities.CommonMethods;
import utilities.io.IOAssist;
import java.awt.Component;
import javax.swing.JCheckBox;
import FluoObjects.IPOGaussianNode;
import FluoObjects.IPOGaussianNodeHandler;
import utilities.statistics.LineSegmentRegressionEvaluater;
import utilities.statistics.PolynomialRegression;
import utilities.CustomDataTypes.DoubleRange;


/**
 *
 * @author Taihao
 */
public class IPOGTLevelNode {    
    public static final String[] psIPOShapes={"Normal","Abnormal"};
    public static final String[] psSignalQuality={"Normal","Spky","Multimodal","Iragular Driffting","Short","Not Analyzable"};
    public static final String[] psTransitionQuality={"Confident","Acceptable","Unacceptable"};
    public static final String[] psRegressionQuality={"Good","Acceptable","Under Est","Over Est","Not Good"};
    public static final String[] psDeltaQuality={"Good","Acceptable","Small","Large"};
    public static ArrayList<String> svParNames=new ArrayList();
    public int nLevel,sliceI,sliceF,nStart,nEnd;
    public IPOGaussianNodeComplex m_cIPOG,m_cIPOGR;
    public double maxDrift,delta,dSignalDelta,dRatio,adjustedDelta;
    public double  xMin,xMax,yMin,yMax;
    public double dL,dR,dRWMax,dRWMin,dLEnv,dREnv;
    public double mean;
    public double stepSize;
    public Histogram cStepSizeHist;
    public static int nRulerSize=30;
    public LineSegmentNode seg;
    public boolean automated;
    public String sCurveQuality,sLRegressionQuality,sRRegressionQuality,sIPOGShapeQaulity,sTransitionQuality,sDeltaQuality;
//    public String sCurveQualityM,sLRegressionQualityM,sRRegressionQualityM,sIPOGShapeQaulityM,sTransitionQualityM,sDeltaQualityM;
    IPOGTrackNode cIPOGT;
    public ArrayList<IPOGaussianNodeComplex> cvIPOGs;
    double[] pdX,pdY;
    boolean[] pbSelected;
    double dSNR;
    public SimpleRegression leftSR,rightSR;//from old version.
    public IPOGTLevelNode(){
        automated=true;
        sCurveQuality=psSignalQuality[0];
        sLRegressionQuality=psRegressionQuality[0];
        sRRegressionQuality=psRegressionQuality[0];
        sIPOGShapeQaulity=psIPOShapes[0];
        sTransitionQuality=psTransitionQuality[0];
        sDeltaQuality=psDeltaQuality[0];
        if(svParNames.size()==0) IPOGTLevelInfoNode.getLevelInfo(null, svParNames, new ArrayList());
        cvIPOGs=new ArrayList();
        dSNR=-1;
    }
    public IPOGTLevelNode copy(){
        IPOGTLevelNode lNode=new IPOGTLevelNode(cIPOGT,pdX,pdY,pbSelected,sliceI,sliceF);
        lNode.automated=automated;
        lNode.m_cIPOG=m_cIPOG;
        lNode.svParNames=new ArrayList();
        CommonStatisticsMethods.copyStringArray(svParNames, lNode.svParNames);
        lNode.nLevel=nLevel;
        lNode.delta=delta;
        lNode.dLEnv=dLEnv;
        lNode.dREnv=dREnv;
        lNode.m_cIPOGR=m_cIPOGR;
        lNode.m_cIPOGR=m_cIPOGR;
        return lNode;
    }
    public IPOGTLevelNode(IPOGTrackNode IPOGT, double[] pdX, double[] pdY, boolean[] pbSelected){
        this();
        cIPOGT=IPOGT;
        this.pdX=pdX;
        this.pdY=pdY;
        if(pbSelected==null){
            this.pbSelected=new boolean[pdX.length];
            CommonStatisticsMethods.setElements(this.pbSelected,false);
        }else{
            this.pbSelected=CommonStatisticsMethods.copyArray(pbSelected);
        }
    }
    public void automated(boolean automation){
        automated=automation;
    }
    public boolean isAutomated(){
        return automated;
    }
    public IPOGTLevelNode(IPOGTrackNode IPOGT, double[] pdX, double[] pdY, boolean[] pbSelected, int sliceI, int sliceF, LineSegmentNode seg){
        this();
        cIPOGT=IPOGT;
        this.pdX=pdX;
        this.pdY=pdY;
        this.sliceI=sliceI;
        this.sliceF=sliceF;
        if(pbSelected==null){
            this.pbSelected=new boolean[pdX.length];
            CommonStatisticsMethods.setElements(this.pbSelected,true);
        }else{
            this.pbSelected=CommonStatisticsMethods.copyArray(pbSelected);
        }
        this.seg=seg;
        calLevelInfo();        
    }
    public IPOGTLevelNode(IPOGTrackNode IPOGT, double[] pdX, double[] pdY, boolean[] pbSelected, int sliceI, int sliceF){
        this();
        cIPOGT=IPOGT;
        this.pdX=pdX;
        this.pdY=pdY;
        this.sliceI=sliceI;
        this.sliceF=sliceF;
        if(pbSelected==null){
            this.pbSelected=new boolean[pdX.length];
            CommonStatisticsMethods.setElements(this.pbSelected,true);
        }else{
            this.pbSelected=CommonStatisticsMethods.copyArray(pbSelected);
        }
        buildLineSegment();
        calLevelInfo();        
    }
    public int importLevelIPOGs(BufferedInputStream bf) throws IOException{
        int nNumIPOGs,i,len,addPars;
        nNumIPOGs=IOAssist.readInt(bf);
        ArrayList<IPOGaussianNodeComplex> cvIPOGs=new ArrayList();
        IPOGaussianNodeComplex IPOG;
        ArrayList<IPOGaussianNode> cvIPOGst;
        for(i=0;i<nNumIPOGs;i++){
            len=IOAssist.readInt(bf);
            addPars=IOAssist.readInt(bf);
            if(len>0){
                cvIPOGst=new ArrayList();
                IPOGaussianNodeHandler.importIPOs(bf, cvIPOGst, len, addPars);
                IPOG=new IPOGaussianNodeComplex(cvIPOGst);
                IPOG.setLevel(nLevel);
                cvIPOGs.add(IPOG);
            }
        }
        if(nNumIPOGs==0) return -1;
        m_cIPOG=cvIPOGs.get(0);
        m_cIPOGR=cvIPOGs.get(nNumIPOGs-1);
        return 1;
    }
    public int exportLevelInfo(DataOutputStream ds) throws IOException{
        ds.writeInt(nLevel);
        ds.writeInt(sliceI);
        ds.writeInt(sliceF);

        //Exporting Level Evaluations
        ds.writeInt(sCurveQuality.length());
        ds.writeChars(sCurveQuality);
        
        ds.writeInt(sLRegressionQuality.length());
        ds.writeChars(sLRegressionQuality);
         
        ds.writeInt(sRRegressionQuality.length());
        ds.writeChars(sRRegressionQuality);
        
        ds.writeInt(sIPOGShapeQaulity.length());
        ds.writeChars(sIPOGShapeQaulity);
        
        ds.writeInt(sTransitionQuality.length());
        ds.writeChars(sTransitionQuality);
        
        ds.writeInt(sDeltaQuality.length());
        ds.writeChars(sDeltaQuality);       
        ds.writeDouble(adjustedDelta);
        ds.writeDouble(delta);
        return 1;
    }
    public void setLineSegment(LineSegmentNode seg){
        this.seg=seg;
        sliceI=(int)(seg.dStartX+0.5);
        sliceF=(int)(seg.dEndX+0.5);
        dL=seg.dStartY;
        dR=seg.dEndY;
    }
    public int importLevelInfo(BufferedInputStream bf) throws IOException{
        nLevel=IOAssist.readInt(bf);
        sliceI=IOAssist.readInt(bf);
        sliceF=IOAssist.readInt(bf);
        if(cIPOGT.m_cLevelInfo.versionNumber<4) importLevelIPOGs(bf);//12n25
        buildLineSegment();
        calLevelInfo();
        sCurveQuality=IOAssist.readString(bf);
        sLRegressionQuality=IOAssist.readString(bf);
        sRRegressionQuality=IOAssist.readString(bf);
        sIPOGShapeQaulity=IOAssist.readString(bf);
        sTransitionQuality=IOAssist.readString(bf);
        sDeltaQuality=IOAssist.readString(bf);
        if(cIPOGT.m_cLevelInfo.versionNumber>6) adjustedDelta=IOAssist.readDouble(bf);
        if(cIPOGT.m_cLevelInfo.versionNumber>6) delta=IOAssist.readDouble(bf);
        return 1;
    }
    public int calLevelInfo(){
        double mean=0, minX=0, maxX=0, minY=0, maxY=0, maxDrift=0;
        nStart=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, sliceI, 1);
        nEnd=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, sliceF, 1);
        int len=sliceF-sliceI+1;
        int ls=Math.min(len, nRulerSize);
        int start=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, sliceI, 1),end=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, sliceF, 1);
        if(start<0){
            start=start;
            start=0;
        }
        double[] pdYRW=CommonStatisticsMethods.getRunningWindowAverage(pdY, start, end, 5, false);
        DoubleRange dr=CommonStatisticsMethods.getRange(pdYRW);
        dRWMax=dr.getMax();
        dRWMin=dr.getMin();

        intRange xRange=new intRange(), yRange=new intRange();
        IPOGaussianNode IPOG;
        IPOG=cIPOGT.getIPOG(sliceI);
        if(IPOG==null) IPOG=cIPOGT.getIPOGE(sliceI);
        if(IPOG==null)
            return -1;
        int x0=IPOG.getCenter().x,y0=IPOG.getCenter().y,x,y;
        double drift;
        double num=0;
        mean=0;
        for(int slice=sliceI;slice<=sliceF;slice++){
            IPOG=cIPOGT.getIPOG(slice);
            if(IPOG==null) IPOG=cIPOGT.getIPOGE(slice);
            if(IPOG==null) continue;
            x=IPOG.getXc();
            y=IPOG.getYc();
            xRange.expandRange(x);
            yRange.expandRange(y);
            drift=CommonMethods.getDistance(x0, y0, x, y);
            if(drift>maxDrift) maxDrift=drift;
            num+=1;
        }
        
        if(seg==null) buildLineSegment();

        xMin=xRange.getMin();
        xMax=xRange.getMax();
        yMin=yRange.getMin();
        yMax=yRange.getMax();
        maxDrift=maxDrift;
        mean=CommonStatisticsMethods.getMean(pdY, null, seg.start, seg.end, 1);
        this.mean=mean;
        
        start=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, start, 1);
        end=CommonStatisticsMethods.getFirstSelectedPosition(pbSelected, true, end,-1);
        int nWs=5;
        int lr,rl;
//    static public boolean validDeviation(PolynomialRegression cPr, double[] pdX, double[] pdY, double[] pdTiltingSig,double[] pdSideness, double[] pdPWDevSig, double[] pdSD, boolean[] pbSelected, int iI, int iF, double pChiSQ, double pTilting,double pSideness,double pPWDev, int wsTilting, int wsPWDev){
        boolean validRegression=false;
        double[] pdDataX;
        double xL,xR;
        if(seg.cPrL!=null){ 
            if(seg.cPrL.isValid()){
                pdDataX=seg.cPrL.getDataX();
                xL=pdDataX[pdDataX.length-1];
                lr=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, xL, 1);
                if(LineSegmentRegressionEvaluater.validDeviation(seg.cPrL, pdX, pdY, null, null, null, cIPOGT.m_cLevelInfo.pdSD, pbSelected, start, lr, 0.1, 0.05, 0.05, 0.05, 3, 0)){
                    validRegression=true;
                    dL=seg.cPrL.predict(pdX[start]);                    
                }
            }
        }
        if(!validRegression) dL=CommonStatisticsMethods.getMean(pdY, pbSelected, start, Math.min(start+nWs, end), 1);
        validRegression=false;
        if(seg.cPrR!=null){ 
            if(seg.cPrR.isValid()){
                pdDataX=seg.cPrR.getDataX();
                xR=pdDataX[0];
                rl=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, xR, 1);
                if(LineSegmentRegressionEvaluater.validDeviation(seg.cPrR, pdX, pdY, null, null, null, cIPOGT.m_cLevelInfo.pdSD, pbSelected, rl, end, 0.1, 0.05, 0.05, 0.05, 3, 0)){
                    validRegression=true;
                    dR=seg.cPrR.predict(pdX[end]);                    
                }
            }
        }
        if(!validRegression) dR=CommonStatisticsMethods.getMean(pdY, pbSelected, Math.max(start, end-nWs), end, 1);    
        double[] pdEnvTrail=cIPOGT.m_cLevelInfo.pdEnvTrail;
        dR=CommonStatisticsMethods.getMedian(pdY, pbSelected, Math.max(nEnd-5, nStart), nEnd,1);
        
        if(pdEnvTrail!=null){
            dLEnv=pdEnvTrail[start];
            dREnv=pdEnvTrail[end];
        }
        
        dL=CommonStatisticsMethods.getMedian(pdY, pbSelected, nStart, Math.min(nStart+5, nEnd), 1);
        if(Double.isNaN(dL)) dL=CommonStatisticsMethods.getMedian(pdY, null, nStart, Math.min(nStart+5, nEnd), 1);

        double[] pdSignal=cIPOGT.m_cLevelInfo.pdSignal;
        if(pdSignal!=null){
            start=nStart+2;
            end=Math.min(start+5, nEnd);
            if(start>end) start=end;
            dL=CommonStatisticsMethods.getMean(pdSignal,start,end,1);

            end=nEnd;
            start=Math.max(nStart+2, end-5);        
            if(start>end) start=end;
            dR=CommonStatisticsMethods.getMean(pdSignal,start,end,1);
        }
        if(nEnd-nStart<15&&pdEnvTrail!=null) {
            dL=pdEnvTrail[nStart];
            dR=pdEnvTrail[nEnd];
        }
        return 1;
    }
    public int buildLineSegment(){
        int start=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, sliceI, 1),end=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, sliceF, 1);
        if(start<0) start=0;
        if(end>=pdX.length) end=pdX.length-1;
        seg=new LineSegmentNode(pdX, pdY,pbSelected, cIPOGT.m_cLevelInfo.pdSD, start,end,5,30,1);
        return 1;
    }
    public void getLevelInfoAsString(StringBuffer names, StringBuffer values){
        names.append(",level");
        values.append(","+nLevel);       
        names.append(",sliceI");
        values.append(","+(int)(seg.dStartX+0.5));
        names.append(",sliceF");
        values.append(","+(int)(seg.dEndX+0.5));
        names.append(",startY");
        values.append(","+PrintAssist.ToString(dL,1));
        names.append(",endY");
        values.append(","+PrintAssist.ToString(dR,1));
        names.append(",Mean");
        values.append(","+PrintAssist.ToString(mean,1));
        names.append(",detal");
        values.append(","+PrintAssist.ToString(dRWMin,1));
        names.append(",RWMin");
        values.append(","+PrintAssist.ToString(dRWMax,1));
        names.append(",RWMax");
        values.append(","+PrintAssist.ToString(delta,1));
        names.append(",stepRatio");
        values.append(","+PrintAssist.ToString(dRatio,2));
    }
    public int updateLevelPars(int index, String value){
        String par=svParNames.get(index);
        if(par.contentEquals("Curve")){
            sCurveQuality=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate Curve Quality", "Curve Quality", psSignalQuality);
            return 1;
        }
        
        if(par.contentEquals("Shape")){
            ArrayList<Component> cvComps=new ArrayList();
            JCheckBox cb=new JCheckBox();
            cb.setText("Apply to all levels");
            cvComps.add(cb);
            sIPOGShapeQaulity=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate IPOG Shape Quality", "Shape Quality", psIPOShapes,cvComps);            
            return 1;
        }
        
        if(par.contentEquals("Transition")){
            sTransitionQuality=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate Right Transition Quality", "Transition Quality", psTransitionQuality);
            return 1;
        }
        
        if(par.contentEquals("left")){
            dL=Double.parseDouble((String) CommonGuiMethods.getOneTextInput("Modify the left end value of the level", "Left end", PrintAssist.ToString(dL, 3)));
            return 1;
        }
        
        if(par.contentEquals("Est.L")){
            sLRegressionQuality=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate the left end regression quality", "Regression", psRegressionQuality);
            return 1;
        }
        
        if(par.contentEquals("right")){
            dR=Double.parseDouble((String) CommonGuiMethods.getOneTextInput("Modify the right end value of the level", "Right end", PrintAssist.ToString(dL, 3)));
            return 1;
        }
        
        if(par.contentEquals("Est.R")){
            sRRegressionQuality=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate right end regression Quality", "Regression", psRegressionQuality);
            return 1;
        }
        
        if(par.contentEquals("Est.D")){
            sDeltaQuality=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate the delta Quality", "Delta", psDeltaQuality);
            return 1;
        }
        return -1;        
    }
/*    public static final String[] psIPOShapes={"Normal","Abnormal"};
    public static final String[] psSignalQuality={"Normal","Spky","Multimodal","Iragular Driffting","Short","Not Analyzable"};
    public static final String[] psTransitionQuality={"Confident","Acceptable","Unacceptable"};
    public static final String[] psRegressionQuality={"Good","Acceptable","Under Est","Over Est"};
    public static final String[] psDeltaQuality={"Good","Acceptable","Small","Large"};*/
    public int evaluate(){
        evaluateSignalQuality();
        evaluateRegressionQualities();
        if(isRegularShape())
            sIPOGShapeQaulity=psIPOShapes[0];
        else
            sIPOGShapeQaulity=psIPOShapes[1];
        sTransitionQuality=psTransitionQuality[0];
        if(delta>0.5*cIPOGT.m_cLevelInfo.cDeltaRangeA.getMax())
            sDeltaQuality=psDeltaQuality[0];
        else
            sDeltaQuality=psDeltaQuality[1];
        return 0;
    }
    void evaluateSignalQuality(){
        int lenCutoff=10;
        int len=seg.end-seg.start+1;
        String se="";
        if(len<lenCutoff) se+=psSignalQuality[4];
        ArrayList<Integer> nvSelected=new ArrayList();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSelected, new intRange(seg.start,seg.end), nvSelected);
        if((double)nvSelected.size()/(double)len<0.8) se+="; "+psSignalQuality[1];
        
        if(se.contentEquals("")) se+=psSignalQuality[0];
        this.sCurveQuality=se;
    }
    int evaluateRegressionQualities(){
        double[] pdSD=cIPOGT.m_cLevelInfo.getSD();
        PolynomialRegression cPr=seg.cPrL;
        int start=seg.cSmoothRangeL.getMin(),end=seg.cSmoothRangeL.getMax();
        boolean validDev=validRegression(cPr,pdSD,start,end);
        if(validDev)
            sLRegressionQuality=psRegressionQuality[0];
        else
            sLRegressionQuality=psRegressionQuality[4];
        
        cPr=seg.cPrR;
        start=seg.cSmoothRangeR.getMin();
        end=seg.cSmoothRangeR.getMax();
        validDev=validRegression(cPr,pdSD,start,end);
        if(validDev)
            sRRegressionQuality=psRegressionQuality[0];
        else
            sRRegressionQuality=psRegressionQuality[4];        
        return 1;        
    }
    boolean validRegression(PolynomialRegression cPr, double[] pdSD, int start, int end){
        IPOGTLevelInfoNode aInfoNode=cIPOGT.m_cLevelInfo;
        double dPPWDev=aInfoNode.getdPPWDev(),dPChiSQ=aInfoNode.getdPChiSQ(),dPTilting=aInfoNode.getdPTilting(),dPSideness=aInfoNode.getdPSideness();
        double dSigChiSQ,dSigTilting,dSigSideness,dSigPWDev,dChiSQ;
        boolean validDev=true;
        int minLen=3;
        if(cPr.getDataX().length<minLen) 
            return false;
        if(!cPr.isValid())             
            return false;
        dChiSQ=LineSegmentRegressionEvaluater.getChiSquare(cPr, pdX, pdY, pdSD, pbSelected, start, end, 1, null);
        dSigChiSQ=LineSegmentRegressionEvaluater.getDevSignificance_Chisquare(cPr, pdX, pdY,pdSD,pbSelected,start,end);
        if(dSigChiSQ<dPChiSQ) validDev=false;
        dSigTilting=LineSegmentRegressionEvaluater.getDevSignificance_Tilting(cPr, pdX, pdY,pdSD,null,pbSelected,start,end, 3);
        if(dSigTilting<dPTilting) validDev=false;
        dSigPWDev=LineSegmentRegressionEvaluater.getDevSignificance_Pointwise(cPr, pdX, pdY,pdSD,null,pbSelected,start,end, 0);
        if(dSigPWDev<dPPWDev) validDev=false;
        dSigSideness=LineSegmentRegressionEvaluater.getDevSignificance_Sideness(cPr, pdX, pdY, pdSD, pbSelected, null, start, end);
        if(dSigSideness<dPSideness) validDev=false;
        return validDev;
    }
    
    public boolean isRegularShape(){
/*        if(!isRegularShape(m_cIPOG)) 
            return false;
        if(!isRegularShape(m_cIPOGR)) 
            return false;*/
        for(int i=0;i<cvIPOGs.size();i++){
            if(!isRegularShape(cvIPOGs.get(i))) 
            return false;
        }
        return true;
    }

    public static boolean isRegularShape(IPOGaussianNodeComplex IPOG){
        if(IPOG==null) return false;
        if(IPOGaussianNodeHandler.isNormalShape(IPOG)) return true;   
//        if(IPOGaussianNodeHandler.isNormalShape(IPOGaussianNodeHandler.removeOffCenterIPOGs(IPOG,5))) return true;  
        return false;        
    }
    public boolean IPOGsAreComputed(){
        if(IPOGaussianFitter.invalidIPOG(m_cIPOG, null, null)) {
            if(m_cIPOG==null) return false;
            if(m_cIPOG.IPOGs.size()>1) return false;//recalculate
        }
        int nMaxLen=cIPOGT.m_cLevelInfo.getMaxSegLength(),sI=sliceI,sF=Math.min(sI+nMaxLen,sliceF);
        if(sI!=m_cIPOG.sliceI||sF!=m_cIPOG.sliceF) return false;
        
        if(IPOGaussianFitter.invalidIPOG(m_cIPOGR, null, null)){
            if(m_cIPOGR==null) return false;
            if(m_cIPOGR.IPOGs.size()>1) return false;//recalculate
        }
        sF=sliceF;
        sI=Math.max(sliceI, sF-nMaxLen);
        if(sI!=m_cIPOGR.sliceI||sF!=m_cIPOGR.sliceF) return false;
        return true;
    }
    public boolean isOverdrifting(){        
        double drift=(dR-dL)/delta;
        double cutoff=1.;
        return drift>cutoff;
    }
    public boolean isShort(){
        int ix=CommonStatisticsMethods.getFirstExtremumIndex(pdY, nStart, 1, 1);
        int numCutoff=2,num=0;
        
        if(ix<0) return true;//there is no maxima in this level
        while(ix<=nEnd){
            num++;
            if(num>=numCutoff) return false;
            ix=CommonStatisticsMethods.getFirstExtremumIndex(pdY, ix+1, 1, 1);
        }
        return true;
    }
    public int retriveIPOGs(){
        cvIPOGs.clear();
        ArrayList<IPOGaussianNodeComplex> IPOGs=cIPOGT.m_cLevelInfo.getIPOs();
        int i,len=IPOGs.size();
        intRange ir=new intRange(sliceI,sliceF);
        IPOGaussianNodeComplex IPOG;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            if(ir.contains(IPOG.sliceI)&&ir.contains(IPOG.sliceF)) {
                cvIPOGs.add(IPOG);
                IPOG.setLevel(nLevel);
            }
        }
        if(cvIPOGs.isEmpty()) 
            retriveIPOGsApproximate();
        return 1;
    }
    public void clearIPOGs(){
        cvIPOGs.clear();
        m_cIPOG=null;
        m_cIPOGR=null;
    }
    public int retriveIPOGsApproximate(){
        cvIPOGs.clear();
        ArrayList<IPOGaussianNodeComplex> IPOGs=cIPOGT.m_cLevelInfo.getIPOs();
        int i,len=IPOGs.size();
        intRange ir=new intRange(sliceI,sliceF);
        IPOGaussianNodeComplex IPOG;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            if(ir.contains(IPOG.sliceI)||ir.contains(IPOG.sliceF)) cvIPOGs.add(IPOG);
            IPOG.setLevel(nLevel);
        }
        return 1;
    }
    public void setLevel(int level){
        nLevel=level;
        if(m_cIPOG!=null){
            if(m_cIPOG.level!=100&&m_cIPOG.level!=-100) m_cIPOG.setLevel(level);
        }
        if(m_cIPOGR!=null){
            if(m_cIPOGR.level!=100&&m_cIPOGR.level!=-100) m_cIPOGR.setLevel(level);
        }
    }
}
