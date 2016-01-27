/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CommonMethods;
import utilities.io.PrintAssist;
import utilities.statistics.Histogram;
import java.io.DataOutputStream;
import java.io.*;
import FluoObjects.IPOGaussianNodeHandler;
import utilities.io.ByteConverter;
import utilities.CommonStatisticsMethods;
import java.awt.Point;
import utilities.statistics.OutliarExcludingLinearRegression;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.QuickSort;
import utilities.statistics.LineSegmentNode;
import utilities.io.IOAssist;
import utilities.statistics.*;
import utilities.statistics.HypothesisTester;
import utilities.statistics.MeanSem1;
import FluoObjects.IPOGaussianNodeComplex;
import FluoObjects.IPOGaussianFitter;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import utilities.Non_LinearFitting.FittingModelNode;
import FluoObjects.IPOGContourParameterNode;
import utilities.CommonGuiMethods;
import utilities.CustomDataTypes.SelectionCriterionNode;

/**
 *
 * @author Taihao
 */
public class IPOGTLevelInfoNode {
    IPOGTrackNode cIPOGT;
    public static final int Auotomatic=0,Manual=1;
    public static final int OverDrifting=1, UpperTransitions=2, OverlappingParticles=3, UndetectibleLastTransition=4,EdgeParticle=5,OverFlickering=6;
    public static final String[] psTrackEvaluationElements={"Not Analyzed","Over drifting","Heterogeneous delta","upper transitions","Overlapping particles",//5
                                                            "No last transition","Close to edges","Too flickery","Double Flicker","Short First Level",//10
                                                            "Too noisy","NA","Not Analyzable","Inconsistent","All Confirmed",//15
                                                            "All ConfirmedE","Reliable Transitions","Shapes are not evaluated","Short Level","Minor Transition",//20
                                                            "Invalid TrackEnding","Aggregated"};
    public static final boolean[] pbExcludable={false,true,true,true,false,
                                                true,true,true,true,false,
                                                true,false,true,false,false,
                                                false,false,false,false,true,
                                                false,false};
    public static final String[] psTransitionDetectionConfidence={"Very Confident","Confident","Acceptable","Not Analyzable","No Transitions","NA"};
    public static final String[] psInfoNodeStatus={"Not Analyzed","Confirm Auto","Excluded","Verified"};
    public IPOGaussianNodeComplex m_cTrackHeadIPOG,m_cTrackTailIPOG;
    public ArrayList<IPOGTLevelNode> m_cvLevelNodes_M,m_cvLevelNodes_A;
    private ArrayList<IPOGaussianNodeComplex> m_cvStoredIPOGs;
    public DoubleRange cDeltaRangeA,cDeltaRangeM,cSignalDeltaRangeA;
    public String sQuantityName;
    public double[] pdX, pdY,pdEnvTrail,pdSD,pdSigDiff,pdSignal;
    public int nTrackIndex,nDataSize;
    public int nStackSize;
    public int nMaxRisingInterval;
    public int nEndLevelA,nEndLevelM,nLevelsMaxTransitionIndex;
    public int[] pnRisingIntervals;
    public String sTransitionConfidenceM,sTransitionConfidenceA;//Should be one of the choices in psTransitionDetectionConfidence. "NA" means the track info is not verified
    public String sInfoNodeStatus_Manual,sInfoNodeStatus_Automatic;
    public ArrayList<String> svCurveEvaluations_Manual,svCurveEvaluations_Automated;
    public boolean[] pbSelected;
    public boolean consistent,AllConfirmed,AllConfirmedE;
    int nMaxSegLength=30, nTrackHeadLength=10;
    public int versionNumber;
    MeanSem1[] pcMeanDiffMeanSems,pnSigDiff;
    public double GapIndex,TransitionIndex_A,MaxNonTransitionAmp_A,MinTransitionAmp_A;
    public double TransitionIndex_M,MaxNonTransitionAmp_M,MinTransitionAmp_M;
    public int nMaxNonTransitionSlice_M,nMaxNonTransitionSlice_A;
    double dSNR_A,dSNR_M;
    boolean bComputeShape;
    double dPChiSQ,dPTilting,dPSideness,dPPWDev,dOutliarRatio,dPTerminals;
    double AmpStabilityIndexA,AmpStabilityIndexM,dNoise,dNoiseX;
    boolean[] pbSignificant;
    ArrayList<IPOGContourParameterNode> cvStoredContourParNodes;
    public IPOGTLevelInfoNode(){
        cvStoredContourParNodes=new ArrayList();
        AllConfirmed=false;
        AllConfirmedE=false;
        versionNumber=getCurrentVersionNumber();
        sTransitionConfidenceM=psTransitionDetectionConfidence[0];
        sTransitionConfidenceA=psTransitionDetectionConfidence[0];
        m_cvLevelNodes_M=new ArrayList();
        m_cvLevelNodes_A=new ArrayList();
        svCurveEvaluations_Manual=new ArrayList();        
        svCurveEvaluations_Automated=new ArrayList();
        svCurveEvaluations_Manual.add(psTrackEvaluationElements[0]);
        sInfoNodeStatus_Manual=psInfoNodeStatus[0];
        sInfoNodeStatus_Automatic=psInfoNodeStatus[0];
        cSignalDeltaRangeA=new DoubleRange();
        pbSignificant=null;
        consistent=true;
        GapIndex=-1;
        TransitionIndex_A=-1;
        bComputeShape=true;
        dSNR_A=-1;
        dSNR_M=-1;
        m_cvStoredIPOGs=new ArrayList();
        nEndLevelM=0;
        dPChiSQ=-1.;
        dPTilting=-1.;
        dPSideness=-1.;
        dPPWDev=-1.;
        dOutliarRatio=-1.;
        dPTerminals=-1.;
        dNoise=-1.;
        pdSigDiff=null;
    }
    void resetEvaluations(){
        svCurveEvaluations_Manual.clear();
        svCurveEvaluations_Manual.add(psTrackEvaluationElements[0]);
    }
    public IPOGTLevelInfoNode(IPOGTrackNode IPOGT){
        this();
        cIPOGT=IPOGT;
    }
    public IPOGTLevelInfoNode(IPOGTrackNode IPOGT, String sQuantityName, boolean[] pbSelected, int nInterval){
        this();
        cIPOGT=IPOGT;
        this.sQuantityName=sQuantityName;
        nTrackIndex=IPOGT.TrackIndex;
        ArrayList<Double> dvX=new ArrayList(), dvY=new ArrayList();
        IPOGT.getTrackData(dvX, dvY, sQuantityName);
        pdX=CommonStatisticsMethods.copyToDoubleArray(dvX);
        pdY=CommonStatisticsMethods.copyToDoubleArray(dvY);
        if(pbSelected!=null){
            if(pbSelected.length!=pdY.length){
                pdY=pdY;
            }
        }
        if(pbSelected==null) pbSelected=CommonStatisticsMethods.getBooleanArray(pdX.length, true);
        this.pbSelected=pbSelected;
        nDataSize=pdX.length;
        nMaxRisingInterval=nInterval;
        pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        calSD();  
        calNoise();
    } 
    public void setSelction(boolean[] pbSelected){
        this.pbSelected=pbSelected;
        calSD();
    }
    public int buildLevelInfoNodes(ArrayList<LineSegmentNode> segs){
        if(segs==null) return -1;
        int i,len=segs.size();
        if(len<=0) return -1;
        
        pdX=segs.get(0).pdX;
        pdY=segs.get(0).pdY;
        LineSegmentNode seg;
        
        m_cvLevelNodes_A.clear();
        for(i=0;i<len;i++){
            seg=segs.get(i);
            m_cvLevelNodes_A.add(new IPOGTLevelNode(cIPOGT,pdX,pdY,pbSelected,(int)(seg.dStartX+0.5),(int)(seg.dEndX+0.5),seg));
        }
        calTransitionSizes();
        adjustLevelIndex();
        return 1;
    }
    public void setConsistency(boolean consistent){
        if(!consistent) this.consistent=false;
    }
    public double calMeanLevelLength(){
        int i,len=m_cvLevelNodes_A.size();
        IPOGTLevelNode lNode,lNode0;
        int nMaxLevel=m_cvLevelNodes_A.get(0).nLevel;
        int i0=len-1;
        lNode0=m_cvLevelNodes_A.get(i0);
        double start=m_cvLevelNodes_A.get(0).seg.dStartX;
        while(lNode0.nLevel<0){
            i0--;
            lNode0=m_cvLevelNodes_A.get(i0);
            if(i0<0) break;
        }
        double length=(lNode0.seg.dEndX-start)*lNode0.nLevel;
        for(i=i0;i>=0;i--){
            lNode=m_cvLevelNodes_A.get(i);
            length+=(lNode.seg.dEndX-start)*(lNode.nLevel-lNode0.nLevel);
            lNode0=lNode;
        }
        length/=nMaxLevel;
        if(length<0) length=0;
        return length;
    }
    public double calMeanQLevelLength(){
        int i,i0,len=m_cvLevelNodes_A.size(),nMaxLevel;
        double start,length=0;
        IPOGTLevelNode lNode,lNode0;
        if(len>0){
            nMaxLevel=m_cvLevelNodes_A.get(0).nLevel;
            i0=len-1;
            lNode0=m_cvLevelNodes_A.get(i0);
            start=m_cvLevelNodes_A.get(0).seg.dStartX;
            while(lNode0.nLevel<0){
                i0--;
                lNode0=m_cvLevelNodes_A.get(i0);
                if(i0<0) break;
            }
            length=(lNode0.seg.dEndX-start)*lNode0.nLevel*lNode0.mean;
            for(i=i0;i>=0;i--){
                lNode=m_cvLevelNodes_A.get(i);
                length+=(lNode.seg.dEndX-start)*(lNode.nLevel-lNode0.nLevel)*lNode.mean;
                lNode0=lNode;
            }
            length/=nMaxLevel;
            if(length<0) length=0;
        }
        return length;
    }
    void getLevelInfoAsString(StringBuffer names,StringBuffer values, boolean Manual){
        ArrayList<IPOGTLevelNode> cvLevelNodes;
        if(Manual)
            cvLevelNodes=m_cvLevelNodes_M;
        else
            cvLevelNodes=m_cvLevelNodes_A;
        
        names.append(",HeadValue");
        values.append(","+cvLevelNodes.get(0).dL);
        
        names.append(",Levels");
        values.append(","+cvLevelNodes.get(0).nLevel);
        names.append(",MeanLength");
        values.append(","+PrintAssist.ToString(calMeanLevelLength(),1));
        names.append(",MeanQLength");
        values.append(","+PrintAssist.ToString(calMeanQLevelLength(),1));
        if(m_cTrackHeadIPOG!=null) m_cTrackHeadIPOG.getMainPeakInfo(names, values);
        names.append(",Events");
        values.append(","+PrintAssist.ToString(cvLevelNodes.size()));
        int i,len=cvLevelNodes.size();
        for(i=0;i<len;i++){
            cvLevelNodes.get(i).getLevelInfoAsString(names,values);
        }
        IPOGTLevelNode lNode;
        for(i=0;i<len;i++){
            lNode=cvLevelNodes.get(i);
            if(lNode==null)continue;
            if(lNode.m_cIPOG==null)continue;
            if(lNode!=null) lNode.m_cIPOG.getMainPeakInfo(names, values);
        }
    }
    public boolean Reviewed(){
        return !sInfoNodeStatus_Manual.contentEquals(psInfoNodeStatus[0]);
    }
    public boolean Verified_Automatically(){
        return sInfoNodeStatus_Automatic.contentEquals(psInfoNodeStatus[3]);
    }
    public boolean Excluded_Automatically(){
        return sInfoNodeStatus_Automatic.contentEquals(psInfoNodeStatus[2]);
    }
    public boolean Verified_Manually(){
        if(sInfoNodeStatus_Manual.contentEquals(psInfoNodeStatus[1])) return Verified_Automatically();
        return sInfoNodeStatus_Manual.contentEquals(psInfoNodeStatus[3]);
    }
    //public static final String[] psInfoNodeStatus={"Not Analyzed","Confirm Auto","Excluded","Verified"};
    public boolean Confirmed(){
        return(sInfoNodeStatus_Manual.contentEquals(psInfoNodeStatus[1]));
    }
    public boolean Excluded_Manually(){
        if(sInfoNodeStatus_Manual.contentEquals(psInfoNodeStatus[1])) return Verified_Automatically();
        return sInfoNodeStatus_Manual.contentEquals(psInfoNodeStatus[2]);
    }
    public static boolean ValidTransitions(String confidence){
        return (confidence.contentEquals("Very Confident")||confidence.contentEquals("Confident")||confidence.contentEquals("Acceptable"));
    }
//    public static final String[] psTransitionDetectionConfidence={"Very Confident","Confident","Acceptable","Not Analyzable","No Transitions","NA"};
    public boolean NotAnalyzable(){
        if(m_cvLevelNodes_A.size()<2&&m_cvLevelNodes_M.size()<2) return true;
        return sTransitionConfidenceM.contentEquals(psTransitionDetectionConfidence[3]);
    }
    public int exportLevelInfo(DataOutputStream ds) throws IOException{
        
        int i,nNumLevels,nt;
        versionNumber=getCurrentVersionNumber();
        ds.writeInt(versionNumber);//12n24
        ds.writeInt(nTrackIndex);
        if(nTrackIndex==12) {
            nTrackIndex=nTrackIndex;
        }
        ds.writeInt(sQuantityName.length());
        ds.writeChars(sQuantityName);
        ds.writeInt(cIPOGT.lastSliceE);
        ds.writeInt(nMaxRisingInterval);
        
        if(cDeltaRangeM==null) cDeltaRangeM=new DoubleRange();
        ds.writeDouble(cDeltaRangeM.getMin());
        ds.writeDouble(cDeltaRangeM.getMax());
        if(cDeltaRangeA==null) cDeltaRangeA=new DoubleRange();
        ds.writeDouble(cDeltaRangeA.getMin());
        ds.writeDouble(cDeltaRangeA.getMax());
        int len=pdX.length;
        ds.writeInt(len);
        for(i=0;i<len;i++){
            ds.writeDouble(pdX[i]);
        }
        for(i=0;i<len;i++){
            ds.writeDouble(pdY[i]);
        }
        for(i=0;i<len;i++){
            if(pbSelected[i])
                nt=1;
            else
                nt=0;
            ds.writeInt(nt);
        }
        exportInfoNodeEvaluations(ds);
        nNumLevels=m_cvLevelNodes_M.size();
        ds.writeInt(nNumLevels);
        for(i=0;i<nNumLevels;i++){
            m_cvLevelNodes_M.get(i).exportLevelInfo(ds);
        }
        nNumLevels=m_cvLevelNodes_A.size();
        ds.writeInt(nNumLevels);
        for(i=0;i<nNumLevels;i++){
            m_cvLevelNodes_A.get(i).exportLevelInfo(ds);
        }
        exportTrackIPOGs(ds);     //12n24   
        exportAdditional(ds);
        return 1;
    }
    public int importLevelInfo(BufferedInputStream bf) throws IOException{
        byte[] pb;   
        versionNumber=IOAssist.readInt(bf);//11n25
        if(versionNumber<7||versionNumber>20){
            versionNumber=versionNumber;
        }
        nTrackIndex=IOAssist.readInt(bf);        
        sQuantityName=IOAssist.readString(bf);
        cIPOGT.lastSliceE=IOAssist.readInt(bf);
        nMaxRisingInterval=IOAssist.readInt(bf);
        double dMin,dMax;
        
        dMin=IOAssist.readDouble(bf);
        dMax=IOAssist.readDouble(bf);
        cDeltaRangeM=new DoubleRange(dMin,dMax);
        
        dMin=IOAssist.readDouble(bf);
        dMax=IOAssist.readDouble(bf);
        cDeltaRangeA=new DoubleRange(dMin,dMax);
        
        int len=IOAssist.readInt(bf);
        pb=new byte[IOAssist.DoubleSize*len];
        pdX=new double[len];
        pdY=new double[len];
        pbSelected=new boolean[len];
        bf.read(pb);
        ByteConverter.getDoubleArray(pb, 0, IOAssist.DoubleSize*len, pdX, 0, len);
        bf.read(pb);
        ByteConverter.getDoubleArray(pb, 0, IOAssist.DoubleSize*len, pdY, 0, len);
        int i,nNumLevels,nt;
        for(i=0;i<len;i++){
            nt=IOAssist.readInt(bf);
            if(nt==1) 
                pbSelected[i]=true;
            else
                pbSelected[i]=false;
        }
        calSD();
        importInfoNodeEvaluations(bf);
        nNumLevels=IOAssist.readInt(bf);
        
        IPOGTLevelNode lNode;
        m_cvLevelNodes_M=new ArrayList();
        for(i=0;i<nNumLevels;i++){
            lNode=new IPOGTLevelNode(cIPOGT,pdX,pdY,pbSelected);
            lNode.importLevelInfo(bf);
            m_cvLevelNodes_M.add(lNode);
        }
        m_cvLevelNodes_A=new ArrayList();
        nNumLevels=IOAssist.readInt(bf);
        for(i=0;i<nNumLevels;i++){
            lNode=new IPOGTLevelNode(cIPOGT,pdX,pdY,pbSelected);
            lNode.importLevelInfo(bf);
            m_cvLevelNodes_A.add(lNode);
        }
        if(versionNumber<4)
            importTrackHeadIPOG(bf);//11n25
        else
            importTrackIPOGs(bf);
        if(versionNumber>0) importAdditional(bf);
        calTransitionSizes();
        adjustEndLevel();
        adjustLevelIndex();
        clearInvalidStoredIPOGs();
        restoreIPOGs();            
        return 1;
    }
    int clearInvalidStoredIPOGs(){
        if(m_cvStoredIPOGs==null) return -1;
        for(int i=m_cvStoredIPOGs.size()-1;i>=0;i--){
            if(IPOGaussianFitter.invalidIPOG(m_cvStoredIPOGs.get(i),null,null)) m_cvStoredIPOGs.remove(i);
        }
        return 1;
    }
    void exportInfoNodeEvaluations(DataOutputStream ds)throws IOException{
        IOAssist.writeString(ds, sInfoNodeStatus_Manual);
        IOAssist.writeString(ds, sTransitionConfidenceM);
        int len=svCurveEvaluations_Manual.size();
        ds.writeInt(len);
        for(int i=0;i<len;i++){
            IOAssist.writeString(ds,svCurveEvaluations_Manual.get(i));
        }
        
        IOAssist.writeString(ds, sInfoNodeStatus_Automatic);
        IOAssist.writeString(ds, sTransitionConfidenceA);
        len=svCurveEvaluations_Automated.size();
        ds.writeInt(len);
        for(int i=0;i<len;i++){
            IOAssist.writeString(ds,svCurveEvaluations_Automated.get(i));
        }
    }
    
    void importInfoNodeEvaluations(BufferedInputStream bf)throws IOException{
        sInfoNodeStatus_Manual=IOAssist.readString(bf);
        sTransitionConfidenceM=IOAssist.readString(bf);
        int i,len=IOAssist.readInt(bf);
        for(i=0;i<len;i++){
            addEvaluation_Manual(IOAssist.readString(bf));
        }
        if(len==0) svCurveEvaluations_Manual.clear();
        sInfoNodeStatus_Automatic=IOAssist.readString(bf);
        sTransitionConfidenceA=IOAssist.readString(bf);
        len=IOAssist.readInt(bf);
        for(i=0;i<len;i++){
            addEvaluation_Auto(IOAssist.readString(bf));
        }
    }
     int exportTrackHeadIPOG(DataOutputStream ds) throws IOException {
        if(m_cTrackHeadIPOG==null) {
            ds.writeInt(-1);
            return -1;
        }
        ds.writeInt(1);
        ArrayList<IPOGaussianNode> cvIPOGs=new ArrayList();
        m_cTrackHeadIPOG.getSimpleIPOGs(cvIPOGs);
        int nNumIPOs=cvIPOGs.size();
        int nAddPars=0;        
        ds.writeInt(nNumIPOs);
        ds.writeInt(nAddPars);
        IPOGaussianNodeHandler.exportIPOs(ds, cvIPOGs, nAddPars);
        return 1;
    }
    int exportTrackIPOGs(DataOutputStream ds) throws IOException {
        ds.writeInt(m_cvStoredIPOGs.size());
         for(int i=0;i<m_cvStoredIPOGs.size();i++){
             exportTrackIPOG(ds,m_cvStoredIPOGs.get(i));
         }
         return 1;
     }
    int importTrackIPOGs(BufferedInputStream bf) throws IOException{
        int num=IOAssist.readInt(bf);
        IPOGaussianNodeComplex IPOG;
        for(int i=0;i<num;i++){
            IPOG=importTrackIPOG(bf);
            m_cvStoredIPOGs.add(IPOG);
        }
        return 1;
    }
     int exportTrackIPOG(DataOutputStream ds, IPOGaussianNodeComplex IPOG) throws IOException {
        if(IPOG==null) {
            ds.writeInt(-1);
            return -1;
        }
        ds.writeInt(1);
        ArrayList<IPOGaussianNode> cvIPOGs=new ArrayList();
        IPOG.getSimpleIPOGs(cvIPOGs);
        int nNumIPOs=cvIPOGs.size();
        int nAddPars=0;        
        ds.writeInt(nNumIPOs);
        ds.writeInt(nAddPars);
        IPOGaussianNodeHandler.exportIPOs(ds, cvIPOGs, nAddPars);
        ds.writeInt(IPOG.sliceI);
        ds.writeInt(IPOG.sliceF);
        return 1;
    }
    int importTrackHeadIPOG(BufferedInputStream bf) throws IOException{
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        int status=IOAssist.readInt(bf);
        if(status==-1){
            m_cTrackHeadIPOG=null;
            return -1;
        }
        int nNumIPOs=IOAssist.readInt(bf),nAddPars=IOAssist.readInt(bf);        
        IPOGaussianNodeHandler.importIPOs(bf, IPOGs, nNumIPOs, nAddPars);
        m_cTrackHeadIPOG=new IPOGaussianNodeComplex(IPOGs);
        m_cTrackHeadIPOG.setLevel(100);
        return 1;
    } 
    IPOGaussianNodeComplex importTrackIPOG(BufferedInputStream bf) throws IOException{
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        IPOGaussianNodeComplex IPOG;
        int status=IOAssist.readInt(bf);
        if(status==-1) return null;
        
        int nNumIPOs=IOAssist.readInt(bf),nAddPars=IOAssist.readInt(bf);        
        IPOGaussianNodeHandler.importIPOs(bf, IPOGs, nNumIPOs, nAddPars);
        IPOG=new IPOGaussianNodeComplex(IPOGs);
        int sI,sF;
        sI=IOAssist.readInt(bf);
        sF=IOAssist.readInt(bf);
        IPOG.setSliceRange(sI, sF);
        return IPOG;
    } 
    
    public void updateLevelNodes(ArrayList<IPOGTLevelNode> cvLevelNodes){
        m_cvLevelNodes_M=cvLevelNodes;
        int level;
        int len=m_cvLevelNodes_M.size(),i;
        IPOGTLevelNode lNode;
        for(i=len-1;i>=0;i--){
            lNode=m_cvLevelNodes_M.get(i);
            level=lNode.nLevel;
        }
    }
    static public String[][] getLevelInfoAsStringArray(ArrayList<IPOGTLevelNode> cvLNodes){
        ArrayList<String> names=new ArrayList(),svValues=new ArrayList();
        ArrayList<Double> values=new ArrayList();
        IPOGaussianNode IPOG;
        getLevelInfo(null,names,svValues);
        int i,len=cvLNodes.size(),len1=names.size(),j,col;
        String[][] psData=new String[len+1][len1];
        for(i=0;i<len1;i++){
            col=i;
            psData[0][col]=names.get(i);
        }
        for(i=0;i<len;i++){
            names.clear();
            values.clear();
            svValues.clear();
            getLevelInfo(cvLNodes.get(i),names,svValues);
            for(j=0;j<len1;j++){
                col=j;
                psData[i+1][col]=svValues.get(j);
            }
        }
        return psData;
    }
    public static void getLevelInfo(IPOGTLevelNode lNode,ArrayList<String> names, ArrayList<String> svValues){
        String st;
        names.add("Level");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.nLevel));

        names.add("sliceI");
        if(lNode!=null) svValues.add(""+lNode.sliceI);

        names.add("sliceF");
        if(lNode!=null) svValues.add(""+lNode.sliceF);

        names.add("Curve");
        if(lNode!=null) svValues.add(lNode.sCurveQuality);

        names.add("Shape");
        if(lNode!=null) svValues.add(lNode.sIPOGShapeQaulity);

        names.add("Delta");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.delta,1));

        names.add("S. Delta");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dSignalDelta,1));

        names.add("SNR");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.delta/lNode.cIPOGT.m_cLevelInfo.dNoise,2));

        names.add("XSNR");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.delta/lNode.cIPOGT.m_cLevelInfo.dNoiseX,2));

        names.add("LSNR");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dSNR,2));
        
        String s1,s2;
        if(lNode==null){
            s1=" ";
            s2=" ";
        }else{
            IPOGaussianNode IPOG=null;
            if(lNode.m_cIPOG!=null) IPOG=lNode.m_cIPOG.getMainIPOG();//12n26
            if(IPOG==null){
                s1="N/A";
                s2="N/A";
            }else{
                s1=PrintAssist.ToString(IPOG.sigmax, 2);
                s2=PrintAssist.ToString(IPOG.sigmay, 2);
            }
        }
        

        names.add("SigmaX");
        if(lNode!=null) svValues.add(s1);

        names.add("SigmaY");
        if(lNode!=null) svValues.add(s2);
        

        names.add("Transition");
        if(lNode!=null) svValues.add(lNode.sTransitionQuality);
        
        names.add("left");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dL,1));
                
        names.add("Peak3CalL");
        st=null;
        if(lNode!=null) {
            if(lNode.m_cIPOG!=null){
                st=PrintAssist.ToString(lNode.m_cIPOG.getPeak3Cal(),1);
            }
            if(st==null) st="N/A";
            svValues.add(st);
        }
        
        names.add("leftEnv");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dLEnv,1));
        
        names.add("Est.L");
        if(lNode!=null) svValues.add(lNode.sLRegressionQuality);

        names.add("right");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dR,1));
        
        names.add("Peak3CalR");
        st=null;
        if(lNode!=null) {
            if(lNode.m_cIPOGR!=null){
                st=PrintAssist.ToString(lNode.m_cIPOGR.getPeak3Cal(),1);
            }
            if(st==null) st="N/A";
            svValues.add(st);
        }
        names.add("rightEnv");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dREnv,1));
        
        names.add("Est.R");
        if(lNode!=null) svValues.add(lNode.sRRegressionQuality);

        names.add("mean");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.mean,1));

        names.add("RWMin");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dRWMin,1));

        names.add("RWMax");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dRWMax,1));

        names.add("Est.D");
        if(lNode!=null) svValues.add(lNode.sDeltaQuality);

        names.add("Ratio");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.dRatio,1));

        names.add("maxDrift");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.maxDrift,1));

        names.add("minX");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.xMin,1));

        names.add("maxX");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.xMax,1));

        names.add("minY");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.yMin,1));

        names.add("maxY");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.yMax,1));

        names.add("SliceI");
        if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.seg.dStartX,1));

        if(lNode!=null){
            int len=(int)(lNode.seg.dEndX-lNode.seg.dStartX+0.5)+1;
            names.add("Length");
            if(lNode!=null) svValues.add(PrintAssist.ToString(len));

            Histogram hist=lNode.cStepSizeHist;
            if(hist!=null){
                names.add("StepSize");
                if(lNode!=null) svValues.add(PrintAssist.ToString(lNode.stepSize,1));

                names.add("meanSS");
                if(lNode!=null) svValues.add(PrintAssist.ToString(hist.getMean(),1));

                names.add("sdSS");
                if(lNode!=null) svValues.add(PrintAssist.ToString(hist.getMeanSem().getSD(),1));

                names.add("maxSS");
                if(lNode!=null) svValues.add(PrintAssist.ToString(hist.getMax(),1));
            }
        }
    }
    public static String getLevelParName(int index){
        if(IPOGTLevelNode.svParNames.isEmpty()) IPOGTLevelInfoNode.getLevelInfo(null, IPOGTLevelNode.svParNames, new ArrayList());
        return IPOGTLevelNode.svParNames.get(index);
    }
    public int update(int index, int col, String value){
        int len=m_cvLevelNodes_M.size(),i,sign=1;
//        if(value<0) sign=-1;
        if(index<0||index>=len) return -1;
        IPOGTLevelNode lNode=m_cvLevelNodes_M.get(index);
        String name=getLevelParName(col);
        if(name.contentEquals("Level")){
            int level=Integer.parseInt(value);
            if(level==-1){
                if(index!=len-1) return -1;//can only nullify the last level
                int level0=lNode.nLevel;
                m_cvLevelNodes_M.get(index).setLevel(level);
                for(i=0;i<len-1;i++){
                    m_cvLevelNodes_M.get(i).setLevel(m_cvLevelNodes_M.get(i).nLevel-level0);
                }
            }else{
                int level0=lNode.nLevel;
                lNode.setLevel(level);
                if(level0==-1&&index==len-1){//reinstating the last level node
                    for(i=0;i<len-1;i++){
                        m_cvLevelNodes_M.get(i).setLevel(m_cvLevelNodes_M.get(i).nLevel+level);
                    }
                }
            }
            return 1;
        }
        lNode.updateLevelPars(col,value);
        return 1;
    }
    public int updateLevelIPOGShape(int index){
        int len=m_cvStoredIPOGs.size(),i,sign=1;
        if(index<0||index>=len) return -1;
        IPOGaussianNodeComplex IPOG=m_cvStoredIPOGs.get(index);
        String[] psIPOShapes={"Normal","Abnormal"};
        int code=-1;
        String shape=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate IPOG Shape", "IPOG Shape", psIPOShapes);
        for(i=0;i<psIPOShapes.length;i++){
            if(shape.contentEquals(psIPOShapes[i])){
                code=i;
                break;
            }
        }
        IPOG.setIPOGCode(code);
        return 1;
    }
    public int updateLevelIPOGContour(int index){
        int len=m_cvStoredIPOGs.size(),i,sign=1;
        if(index<0||index>=len) return -1;
        IPOGaussianNodeComplex IPOG=m_cvStoredIPOGs.get(index);
        String[] psIPOShapes={"Normal","Abnormal"};
        int code=-1;
        String shape=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate IPOG Shape", "IPOG Shape", psIPOShapes);
        for(i=0;i<psIPOShapes.length;i++){
            if(shape.contentEquals(psIPOShapes[i])){
                code=i;
                break;
            }
        }
        IPOG.setContourCode(code);
        return 1;
    }
    public int updateLevelIPOGShapeRaw(int index){
        int len=m_cvStoredIPOGs.size(),i,sign=1;
        if(index<0||index>=len) return -1;
        IPOGaussianNodeComplex IPOG=m_cvStoredIPOGs.get(index);
        String[] psIPOShapes={"Normal","Abnormal"};
        int code=-1;
        String shape=(String) CommonGuiMethods.getOneComboBoxSelection("Evaluate IPOG Shape", "IPOG Shape", psIPOShapes);
        for(i=0;i<psIPOShapes.length;i++){
            if(shape.contentEquals(psIPOShapes[i])){
                code=i;
                break;
            }
        }
        IPOG.setRawContourCode(code);
        return 1;
    }
    public static int getShapeCode(String shape){
        if(shape.contentEquals("Normal")) return 0;
        if(shape.contentEquals("Abnormal")) return 1;
        return -1;
    }
    public int updateLevelIPOGShapeConfirmation(ArrayList<String> svShapes){
        int len=m_cvStoredIPOGs.size(),i;
        if(len!=svShapes.size()) return -1;
        String[] options={"Confirm All","Unconfirm All", "Cancel"};
        String option=(String) CommonGuiMethods.getOneComboBoxSelection("Confirm IPOG Shape Evaluation", "Confirming Options", options);
        int index=-1;
        for(i=0;i<3;i++){
            if(option.contentEquals(options[i])){
                index=i;
                break;
            }
        }
        switch (index){
            case 0:
                for(i=0;i<len;i++){
                    m_cvStoredIPOGs.get(i).setIPOGCode(getShapeCode(svShapes.get(i)));
                }
                break;
            case 1:
                for(i=0;i<len;i++){
                    m_cvStoredIPOGs.get(i).setIPOGCode(-1);
                }
                break;
            default:
                break;
        }
        return 1;
    }
    public int updateLevelIPOGContourConfirmation(ArrayList<String> svShapes){
        int len=m_cvStoredIPOGs.size(),i;
        if(len!=svShapes.size()) return -1;
        String[] options={"Confirm All","Unconfirm All", "Cancel"};
        String option=(String) CommonGuiMethods.getOneComboBoxSelection("Confirm IPOG Shape Evaluation", "Confirming Options", options);
        int index=-1;
        for(i=0;i<3;i++){
            if(option.contentEquals(options[i])){
                index=i;
                break;
            }
        }
        switch (index){
            case 0:
                for(i=0;i<len;i++){
                    m_cvStoredIPOGs.get(i).setContourCode(getShapeCode(svShapes.get(i)));
                }
                break;
            case 1:
                for(i=0;i<len;i++){
                    m_cvStoredIPOGs.get(i).setContourCode(-1);
                }
                break;
            default:
                break;
        }
        return 1;
    }
    public int updateLevelIPOGShapeConfirmationRaw(ArrayList<String> svShapes){
        int len=m_cvStoredIPOGs.size(),i;
        if(len!=svShapes.size()) return -1;
        String[] options={"Confirm All","Unconfirm All", "Cancel"};
        String option=(String) CommonGuiMethods.getOneComboBoxSelection("Confirm IPOG Shape Evaluation", "Confirming Options", options);
        int index=-1;
        for(i=0;i<3;i++){
            if(option.contentEquals(options[i])){
                index=i;
                break;
            }
        }
        switch (index){
            case 0:
                for(i=0;i<len;i++){
                    m_cvStoredIPOGs.get(i).setRawContourCode(getShapeCode(svShapes.get(i)));
                }
                break;
            case 1:
                for(i=0;i<len;i++){
                    m_cvStoredIPOGs.get(i).setRawContourCode(-1);
                }
                break;
            default:
                break;
        }
        return 1;
    }
    public static IPOGTLevelNode getLevelNode(ArrayList<IPOGTLevelNode> cvLevelNodes, int slice){
        int index=0;
        IPOGTLevelNode lNode=null;
        int len=cvLevelNodes.size();
        if(len==0) return null;
        lNode=cvLevelNodes.get(0);
        while(lNode.seg.dEndX<slice){
            index++;
            if(index>=len) break;
            lNode=cvLevelNodes.get(index);
        }
        return lNode;
    }
    public static int getLevelNodeIndex(ArrayList<IPOGTLevelNode> cvLevelNodes, IPOGTLevelNode lNode){
        int i,len=cvLevelNodes.size();
        for(i=0;i<len;i++){
            if(lNode==cvLevelNodes.get(i)) return i;
        }
        return -1;
    }
    public int calLevelEnvValues(){
        if(pdEnvTrail==null) return -1;
        if(pnRisingIntervals==null){
            pnRisingIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        }
        IPOGTLevelNode lNode;
        int i,len,index,right;
        len=m_cvLevelNodes_M.size();
        for(i=0;i<len;i++){
            lNode=m_cvLevelNodes_M.get(i);
            index=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode.sliceI, 1);
            lNode.dLEnv=pdEnvTrail[index];
            index=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode.sliceF, 1);
            right=index+CommonStatisticsMethods.getRisingInterval(pnRisingIntervals, index, -1);
            if(right<0) right=0;
            lNode.dREnv=pdEnvTrail[right];
        }
        len=m_cvLevelNodes_A.size();
        for(i=0;i<len;i++){
            lNode=m_cvLevelNodes_A.get(i);
            index=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode.sliceI, 1);
            lNode.dLEnv=pdEnvTrail[index];
            index=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode.sliceF, 1);
            if(!CommonStatisticsMethods.isLocalExtrema(pdEnvTrail, index, 1))
                right=index+CommonStatisticsMethods.getRisingInterval(pnRisingIntervals, index, -1);
            else
                right=index;
            if(right<0) right=0;
            lNode.dREnv=pdEnvTrail[right];
        }
        return 1;
    }
    public int calTransitionSizes(){//calculating step size hist, and the transition step size
        int i,len=m_cvLevelNodes_M.size();
        if(versionNumber>1) calLevelEnvValues();
        cDeltaRangeM=new DoubleRange();
        IPOGTLevelNode lNode0,lNode;
        DoubleRange ampRange=new DoubleRange();
        double adjust=0,sigChange;
        double signalDelta;
        int left,right;
        if(len>0){
            lNode0=m_cvLevelNodes_M.get(0);
            lNode0.adjustedDelta=lNode0.delta;            
            for(i=1;i<len;i++){                
                lNode=m_cvLevelNodes_M.get(i);          
                left=lNode.nStart;
                right=lNode.nEnd;
                if(pdSignal!=null&&lNode.nLevel==1)
                    sigChange=pdSignal[right]-pdSignal[left];
                else
                    sigChange=0;
//                if(lNode.nLevel==1) adjust+=sigChange/lNode.nLevel;//do not make adjusted delta
                lNode.adjustedDelta=lNode.delta-adjust;
                if(versionNumber>1&&pdSigDiff!=null){
                    lNode0.delta=pdSigDiff[lNode0.nEnd];
                    ampRange.expandRange(lNode0.delta);
                } else {
                    lNode0.delta=lNode0.dR-lNode.dL;
                    ampRange.expandRange(lNode0.delta);
                }
                
                if(pdSigDiff[lNode0.nEnd]==-0.001){//this is unconfirmed position in the detection module. The transition is manual placed
                    lNode0.delta=lNode0.dR-lNode.dL;
                    ampRange.expandRange(lNode0.delta);
                }
                
                lNode0.dRatio=lNode0.dR/lNode0.delta;
                cDeltaRangeM.expandRange(Math.abs(lNode0.delta));
                lNode0.dSNR=lNode0.delta/pdSD[CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode0.sliceF, 1)];
                lNode0.dSignalDelta=lNode0.dR-lNode.dL;
                lNode0=lNode;
            }
            lNode=m_cvLevelNodes_M.get(len-1);
            lNode0.delta=lNode0.dR;
            lNode0.dSNR=-1;
            lNode0.dRatio=1;
            lNode0.dSignalDelta=lNode0.dR;
        }
        AmpStabilityIndexM=(ampRange.getMax()-ampRange.getMin())/(ampRange.getMax()+ampRange.getMin());
        ampRange.resetRange();
        cSignalDeltaRangeA=new DoubleRange();
        len=m_cvLevelNodes_A.size();
        cDeltaRangeA=new DoubleRange();
        adjust=0;
        if(len>0){
            lNode0=m_cvLevelNodes_A.get(0);
            lNode0.adjustedDelta=lNode0.delta;            

            for(i=1;i<len;i++){
                lNode=m_cvLevelNodes_A.get(i);
                left=lNode.nStart;
                right=lNode.nEnd;
                if(pdSignal!=null)
                    sigChange=pdSignal[right]-pdSignal[left];
                else
                    sigChange=0;
                if(lNode.nLevel==1) adjust+=sigChange/lNode.nLevel;
                lNode.adjustedDelta=lNode.delta-adjust;
                if(versionNumber>1&&pdSigDiff!=null){
                    lNode0.delta=pdSigDiff[lNode0.nEnd];
                    ampRange.expandRange(lNode0.delta);
                } else {
                    lNode0.delta=lNode0.dR-lNode.dL;
                    if(lNode.nLevel>0) ampRange.expandRange(lNode0.delta);
                }
                
                lNode0.dRatio=lNode0.dR/lNode0.delta;
                lNode0.dSNR=lNode0.delta/pdSD[CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode0.sliceF, 1)];
                cDeltaRangeA.expandRange(Math.abs(lNode0.delta));
                signalDelta=lNode0.dR-lNode.dL;
                lNode0.dSignalDelta=signalDelta;
                cSignalDeltaRangeA.expandRange(signalDelta);
                lNode0=lNode;
            }
            lNode=m_cvLevelNodes_A.get(len-1);
            lNode0.delta=lNode0.dR;
            lNode0.dSNR=-1;
            lNode0.dRatio=1;
            lNode0.dSignalDelta=lNode0.dR;
        }
        AmpStabilityIndexA=(ampRange.getMax()-ampRange.getMin())/(ampRange.getMax()+ampRange.getMin());
        return 1;
    }
    
    public ArrayList<IPOGaussianNodeComplex> getIPOs(){
        ArrayList<IPOGaussianNodeComplex> IPOGs=new ArrayList();
        IPOGaussianNodeComplex IPOG0,IPOG;
//        restoreIPOGs();
        int i,len=m_cvStoredIPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.add(m_cvStoredIPOGs.get(i));
        }
        return IPOGs;
    }
    int adjustLevel(double x1, double y1, double x2, double y2){//this method is to be used for overwriting the automatically 
        //calculated level info.
        //not likely being used anymore
        int index=getLevelNodeIndex(x1),len;
        if(index<0) return -1;
        
        IPOGTLevelNode lNode=m_cvLevelNodes_M.get(index),lNode0;
        SimpleRegression sr=new SimpleRegression();
        sr.addData(x1,y1);
        sr.addData(x2,y2);
        
        double d1=x1-lNode.seg.dStartX,d2=lNode.seg.dEndX-x2;
        if(d2<0) return -1;
        if(d1<d2) {
            lNode.leftSR=sr;
            lNode.dL=sr.predict(lNode.seg.dStartX);
            if(index>0) {
                lNode0=m_cvLevelNodes_M.get(index-1);
                lNode0.delta=lNode0.dR-lNode.dL;
                lNode0.dRatio=lNode0.dR/lNode0.delta;
            }            
        }else{
            lNode.rightSR=sr;
            lNode.dR=sr.predict(lNode.seg.dEndX);
            len=m_cvLevelNodes_M.size();
            if(index<len-1) {
                lNode0=m_cvLevelNodes_M.get(index+1);
                lNode.delta=lNode.dR-lNode0.dL;
                lNode.dRatio=lNode.dR/lNode.delta;
            }            
        }
        return 1;
    }
    int getLevelNodeIndex(double x){
        int i,len=m_cvLevelNodes_M.size();
        IPOGTLevelNode lNode;
        for(i=0;i<len;i++){
            lNode=m_cvLevelNodes_M.get(i);
            if(lNode.seg.dStartX<=x&&lNode.seg.dEndX>=x) return i;
        }
        return -1;
    }
    int nextIndex(int index, int sign){//returns the index at which dvX is at least m_nRisingInterval away
        //from 
        int nextIndex=index+sign;
        if(nextIndex<0||nextIndex>=nDataSize) return -1;
        double x0=pdX[index],x=pdX[nextIndex];
        while(sign*(x-x0)<nMaxRisingInterval){
            nextIndex+=sign;
            if(nextIndex<0||nextIndex>=nDataSize) return -1;
            x=pdX[nextIndex];
        }
        return nextIndex;
    }
    public IPOGTLevelNode removeLevelTransition(int slice){
        IPOGTLevelNode lNode=IPOGTLevelInfoNode.getLevelNode(m_cvLevelNodes_M, slice);
        int sign=1;
        int len=m_cvLevelNodes_M.size();
        if(Math.abs(lNode.sliceF-slice)<Math.abs(lNode.sliceI-slice)) sign=-1;
        int index=IPOGTLevelInfoNode.getLevelNodeIndex(m_cvLevelNodes_M,lNode);
        if(index<0) return null;
        if(sign==1){
            if(index==0) return null;
            index--;
        }else{
            if(index>=len-1) return null;
        }
        lNode=mergeLevels(m_cvLevelNodes_M.get(index),m_cvLevelNodes_M.get(index+1));
        m_cvLevelNodes_M.remove(index+1);
        m_cvLevelNodes_M.set(index, lNode);
        adjustLevelIndex();
        calTransitionSizes();
        return lNode;
    }
    public ArrayList<IPOGTLevelNode> removeLevelTransition(ArrayList<IPOGTLevelNode> cvLevelNodes,int slice){
        IPOGTLevelNode lNode=IPOGTLevelInfoNode.getLevelNode(cvLevelNodes, slice);
        int index=IPOGTLevelInfoNode.getLevelNodeIndex(cvLevelNodes,lNode);
        int sign=1;
        int len=cvLevelNodes.size();
        if(Math.abs(lNode.sliceF-slice)<Math.abs(lNode.sliceI-slice)) 
            sign=-1;
        else if(Math.abs(lNode.sliceF-slice)==Math.abs(lNode.sliceI-slice)){
            if(index==0) 
                sign=-1;//merge with the right side level node
            else{
                if(Math.abs(cvLevelNodes.get(index-1).delta)>Math.abs(lNode.delta)) sign=-1;
            }
        }
        
        if(index<0) return cvLevelNodes;
        if(sign==1){
            if(index==0) return cvLevelNodes;
            index--;
        }else{
            if(index>=len-1) return cvLevelNodes;
        }
        lNode=mergeLevels(cvLevelNodes.get(index),cvLevelNodes.get(index+1));
        cvLevelNodes.remove(index+1);
        cvLevelNodes.set(index, lNode);
        adjustLevelIndex();
        calTransitionSizes();
        return cvLevelNodes;
    }
    public void adjustLevelIndex(){
        int i,len=m_cvLevelNodes_M.size(),delta,level=nEndLevelA;
        IPOGTLevelNode lNode;
        for(i=0;i<len;i++){
            lNode=m_cvLevelNodes_M.get(len-i-1);
            if(i==0)
                level=nEndLevelM;
            else {
                delta=1;
                if(lNode.delta<0) delta=-1;
                level+=delta;
            }
            lNode.setLevel(level);
        }
        
        len=m_cvLevelNodes_A.size();
        for(i=0;i<len;i++){
            lNode=m_cvLevelNodes_A.get(len-i-1);
            if(i==0)
                level=nEndLevelA;
            else {
                delta=1;
                if(lNode.delta<0) delta=-1;
                level+=delta;
            }
            lNode.setLevel(level);
        }
    }
    public int getSliceIndex(int slice){
        int index=Math.min(pdX.length-1, slice-cIPOGT.firstSlice);
        int nX=(int)(pdX[index]+0.5);
        while(nX>slice){
            index--;
            if(index<0) return index;
            nX=(int)(pdX[index]+0.5);
        }
        return index;
    }
    public ArrayList<IPOGTLevelNode> createLevelTransition(int slice){
        ArrayList<IPOGTLevelNode> lNodes=new ArrayList();
        int position=getSliceIndex(slice);
        if(position<0) return null;
        IPOGTLevelNode lNode=IPOGTLevelInfoNode.getLevelNode(m_cvLevelNodes_M,slice);//the node to split
        if(lNode==null) return null;
        int left=lNode.sliceI,right=lNode.sliceF;
        if(slice<left||slice>right) 
                return null;//should not have this case
        
        int lIndex=IPOGTLevelInfoNode.getLevelNodeIndex(m_cvLevelNodes_M,lNode);
        
        int level=lNode.nLevel;
        lNode=buildLevelNode(level,slice+nMaxRisingInterval,right);
        lNodes.add(lNode);
        
        m_cvLevelNodes_M.set(lIndex, lNode);
        lNode=buildLevelNode(level+1,left,slice);//m_cvLevelNodes has higher level nodes stored with lower indexes
        lNodes.add(lNode);
        m_cvLevelNodes_M.add(lIndex,lNode);

        adjustLevelIndex();
        calTransitionSizes();
        return lNodes;
    }
    IPOGTLevelNode buildLevelNode(int level, int sliceI, int sliceF){
        IPOGTLevelNode lNode=new IPOGTLevelNode(cIPOGT,pdX,pdY,pbSelected,sliceI,sliceF);
        int nEnd=nDataSize-1;
        double yr;
        int ir=sliceF+(int)(nMaxRisingInterval+0.5);
        if(ir<=nEnd)
            yr=pdY[ir];
        else
            yr=0;
        
        lNode.nLevel=level;
        lNode.stepSize=lNode.dR-yr;
        return lNode;
    }
    public IPOGTLevelNode mergeLevels(IPOGTLevelNode lNode0, IPOGTLevelNode lNode1){//lNode1 is at the right and has lower level
        IPOGTLevelNode lNode;
        int iI=lNode0.sliceI,iF=lNode1.sliceF;
        lNode=buildLevelNode(lNode1.nLevel,iI,iF);
        lNode.m_cIPOG=lNode0.m_cIPOG;
        lNode.m_cIPOGR=lNode1.m_cIPOGR;
        return lNode;
    }
    public ArrayList<IPOGTLevelNode> getLevelNodes(double xI, double xF){
        int len=m_cvLevelNodes_M.size(),i;
        IPOGTLevelNode lNode;
        ArrayList<IPOGTLevelNode> lNodes=new ArrayList();
        for(i=0;i<len;i++){
            lNode=m_cvLevelNodes_M.get(i);
            if(lNode.sliceI>=xI&&lNode.sliceF<=xF){
                lNodes.add(lNode);
            }
        }
        return lNodes;
    }
    public ArrayList<Integer> getTransitionSlices(DoubleRange xRange){
        int len=m_cvLevelNodes_M.size(),i,slice;
        ArrayList<Integer> nvSlices=new ArrayList();
        for(i=0;i<len;i++){
            slice=m_cvLevelNodes_M.get(i).sliceF;
            if(xRange.contains(slice)) nvSlices.add(slice);
        }
        return nvSlices;
    }
    public void setConfidenceLevel(String sCode){
        sTransitionConfidenceM=sCode;
    }
    public void setEndLevelA(int level){
        nEndLevelA=level;
    }
    public void setEndLevelM(int level){
        nEndLevelM=level;
    }
    public boolean LevelIPOGsAreComputed(){
//        if(m_cTrackHeadIPOG==null) return false;
        if(IPOGaussianFitter.invalidIPOG(m_cTrackHeadIPOG, null, null)) {
            if(m_cTrackHeadIPOG==null) return false;
            if(m_cTrackHeadIPOG.IPOGs.size()>1) return false;
        }
        int num=m_cvLevelNodes_A.size(),i;
        for(i=0;i<num;i++){
            if(!m_cvLevelNodes_A.get(i).IPOGsAreComputed()) return false;
        }
        return true;
    }
    public void setInfoStatus(String status){
        if(status.contentEquals("Confirm Auto")){
            sInfoNodeStatus_Manual="Confirm Auto";
            CommonStatisticsMethods.copyStringArray(svCurveEvaluations_Automated, svCurveEvaluations_Manual);
            int i,len=m_cvLevelNodes_A.size();
            m_cvLevelNodes_M.clear();
            IPOGTLevelNode lNode;
            for(i=0;i<len;i++){
                lNode=m_cvLevelNodes_A.get(i).copy();
                lNode.automated(false);
                lNode.evaluate();
                m_cvLevelNodes_M.add(lNode);
            }
            nEndLevelM=nEndLevelA;
        }else{
            sInfoNodeStatus_Manual=status;
        }
    }
    public void addEvaluation_Manual(String ev){
        if(svCurveEvaluations_Manual.size()==1){
            if(svCurveEvaluations_Manual.get(0).contentEquals(psTrackEvaluationElements[0])) svCurveEvaluations_Manual.clear();
        }
        int i,index=-1,len=svCurveEvaluations_Manual.size();
        for(i=0;i<len;i++){
            if(svCurveEvaluations_Manual.get(i).contentEquals(ev)) {
                index=i;
                break;
            }
        }
        if(index<0) 
            svCurveEvaluations_Manual.add(ev);
        else
            svCurveEvaluations_Manual.remove(index);
    }
    public void clearEvaluations_Auto(){
        svCurveEvaluations_Automated.clear();
    }
    public void addEvaluation_Auto(String ev){
        svCurveEvaluations_Automated.add(ev);
        int i,len=svCurveEvaluations_Automated.size();
        boolean exist=false;
        for(i=0;i<len;i++){
            if(svCurveEvaluations_Automated.get(i).contentEquals(ev)){
                exist=true;
                break;
            }
        }
        if(!exist) svCurveEvaluations_Automated.add(ev);
    }
    public boolean Excluded(){
        //    public static final String[] psInfoNodeStatus={"Not Analyzed","Confirm Auto","Excluded","Verified"};
        String status=sInfoNodeStatus_Manual;
        if(status==psInfoNodeStatus[0]||status==psInfoNodeStatus[1])
            status=sInfoNodeStatus_Automatic;            
        return (status.contentEquals(psInfoNodeStatus[2]));
    }
    boolean manuallyEvaluated(){
        if(svCurveEvaluations_Manual.size()!=1) return true;
        if(svCurveEvaluations_Manual.get(0).contentEquals(psTrackEvaluationElements[0])) return false;
        return true;
    }
    void calMaxTransitionIndexLevels(){
        nLevelsMaxTransitionIndex=0;
        String[][] psSignal;
        ArrayList<String> names=new ArrayList(), values=new ArrayList();
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        double[] pdDA=CommonStatisticsMethods.getAbs(pdSigDiff);
        CommonMethods.LocalExtrema(pdDA, nvLn, nvLx);
        int i,len=nvLx.size(),index,index1;
        double[] pdSig=new double[len],pdGap=new double[len],pdTransitionIndexes=new double[len],pdSorting;
        int[] indexes=new int[len],pnPositions=new int[len],pnLevels=new int[len],indexes1=new int[len];
        for(i=0;i<len;i++){
            pdSig[i]=pdDA[nvLx.get(i)];
            indexes[i]=i;
        }
        pdSorting=CommonStatisticsMethods.copyArray(pdSig);
        names.add("rank");
        names.add("Position");
        names.add("Slice");
        names.add("Signal");
        names.add("Gap");
        names.add("T.I.");
        QuickSort.quicksort(pdSorting, indexes);
        for(i=0;i<len-1;i++){
            index=len-1-i;
            pdSig[i]=pdSorting[index];
            pdGap[i]=pdSorting[index]-pdSorting[index-1];
            pdTransitionIndexes[i]=(pdSorting[index]-pdSorting[index-1])/pdSorting[index-1];
            pnPositions[i]=nvLx.get(indexes[index]);
            pnLevels[i]=i+1;
            indexes1[i]=i;
            values.add(""+i);
            values.add(PrintAssist.ToString(pnPositions[i], 0));
            values.add(PrintAssist.ToString(pdX[pnPositions[i]], 0));
            values.add(PrintAssist.ToString(pdSig[i], 1));
            values.add(PrintAssist.ToString(pdGap[i], 1));
            values.add(PrintAssist.ToString(pdTransitionIndexes[i], 3));
        }
        
        int len1=names.size(),j;
        psSignal=new String[len+1][len1];
        for(i=0;i<len1;i++){
            psSignal[0][i]=names.get(i);
        }
        for(i=0;i<len-1;i++){
            for(j=0;j<len1;j++){
                psSignal[i+1][j]=values.get(i*len1+j);
            }
        }
        
//        CommonGuiMethods.displayTable("Signal Jump", psSignal);
/*        
        CommonStatisticsMethods.copyArray(pdTransitionIndexes, pdSorting);
        CommonStatisticsMethods.scaleArray(pdSorting, -1.);
        QuickSort.quicksort(pdSorting, indexes1);
        
        pdGap[len-1]=0;
//        CommonStatisticsMethods.PartialSort( pdTransitionIndexes, indexes1, 2, -1, 0, len-1, 1);
        String st1="Max TI Levels1: ", st2="Max TI Levels2: ", st3="Max TI Levels3: ", st4="Max TI Levels4: ";
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Automated, st1);
        index1=indexes1[0];
        st1+=PrintAssist.ToString(index1+1)+" TI: "+PrintAssist.ToString(pdTransitionIndexes[index1], 3)+" at: "+pnPositions[index1]+" Sig: "+PrintAssist.ToString(pdSig[index1],2); ;
        if(index>=0) 
            svCurveEvaluations_Automated.set(index, st1);
        else
            svCurveEvaluations_Automated.add(st1);
        
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Automated, st2);
        index1=indexes1[1];
        st2+=PrintAssist.ToString(index1+1)+" TI: "+PrintAssist.ToString(pdTransitionIndexes[index1], 3)+" at: "+pnPositions[index1]+" Sig: "+PrintAssist.ToString(pdSig[index1],2);
        if(index>=0) 
            svCurveEvaluations_Automated.set(index, st2);
        else
            svCurveEvaluations_Automated.add(st2);
        
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Automated, st3);
        index1=indexes1[2];
        st3+=PrintAssist.ToString(index1+1)+" TI: "+PrintAssist.ToString(pdTransitionIndexes[index1], 3)+" at: "+pnPositions[index1]+" Sig: "+PrintAssist.ToString(pdSig[index1],2);
        if(index>=0) 
            svCurveEvaluations_Automated.set(index, st3);
        else
            svCurveEvaluations_Automated.add(st3);
        
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Automated, st4);
        index1=indexes1[3];
        st4+=PrintAssist.ToString(index1+1)+" TI: "+PrintAssist.ToString(pdTransitionIndexes[index1], 3)+" at: "+pnPositions[index1]+" Sig: "+PrintAssist.ToString(pdSig[index1],2);
        if(index>=0) 
            svCurveEvaluations_Automated.set(index, st4);
        else
            svCurveEvaluations_Automated.add(st4);*/
    }
    void evaluateSignal(){
        //    public static final String[] psInfoNodeStatus={"Not Analyzed","Confirm Auto","Excluded","Verified"};
//    public static final String[] psTrackEvaluationElements={"Not Analyzed","Over drifting","Heterogeneous delta","upper transitions","Overlapping particles",//5
//                                                            "No last transition","Close to edges","Too flickery","Double Flicker","Short First Level",//10
//                                                            "Too noisy","NA","Not Analyzable","Inconsistent","All Confirmed",//15
//                                                            "All ConfirmedE","Reliable Transitions"};
        if(dNoise<0&&pdY!=null) calNoise();
        calGapIndex();
        calMaxNonTransitionAmp();
        excludeSmallTransitions();
        svCurveEvaluations_Automated.clear();
        calMaxNonTransitionAmp();
        calTransitionSizes();
        calSNR();
        if(GapIndex<0) calGapIndex();
        svCurveEvaluations_Automated.add("Gap Index= "+PrintAssist.ToString(GapIndex, 2));
        svCurveEvaluations_Automated.add("SNR= "+PrintAssist.ToString(dSNR_A, 2));
        DoubleRange dr=getSigmaRange();
//        svCurveEvaluations_Automated.add("SigmaRange= "+PrintAssist.ToString(dr.getMin(), 1)+" to "+PrintAssist.ToString(dr.getMax(), 1));        
//        svCurveEvaluations_Automated.add("Max Axis Ratio= "+PrintAssist.ToString(getMaxAixisRatio(), 2));
//        svCurveEvaluations_Automated.add("Max Axis Ratio RAW= "+PrintAssist.ToString(getMaxAixisRatioRaw(), 2));
//        svCurveEvaluations_Automated.add("Num Concave= "+PrintAssist.ToString(getNumConcaveCurves(), 0));
//        svCurveEvaluations_Automated.add("Num Concave Raw= "+PrintAssist.ToString(getNumConcaveCurvesRaw(), 0));
        
        if(!isValidGroundLevel(m_cvLevelNodes_A.size()-1)) {
            svCurveEvaluations_Automated.add(psTrackEvaluationElements[5]);
        }
        if(isTooNoisy()) svCurveEvaluations_Automated.add(psTrackEvaluationElements[10]);
        if(!isSalientTransitions()) svCurveEvaluations_Automated.add(psTrackEvaluationElements[19]);
        adjustEndLevel();
        adjustLevelIndex();
        if(isOverDrifting())svCurveEvaluations_Automated.add(psTrackEvaluationElements[1]);
        if(AllConfirmed)svCurveEvaluations_Automated.add(psTrackEvaluationElements[14]);
        if(AllConfirmedE)svCurveEvaluations_Automated.add(psTrackEvaluationElements[15]);
        if(!consistent)svCurveEvaluations_Automated.add(psTrackEvaluationElements[13]);
        if(ComputeShape()){
            if(!isNormalShape()) svCurveEvaluations_Automated.add(psTrackEvaluationElements[4]);
        } else {
            svCurveEvaluations_Automated.add(psTrackEvaluationElements[17]);
        }
        if(isHeteroDelta()) {
            
            svCurveEvaluations_Automated.add(psTrackEvaluationElements[2]);
        }
        if(containsUpperTransition())svCurveEvaluations_Automated.add(psTrackEvaluationElements[3]); 
        if(isCloseToEdge()) svCurveEvaluations_Automated.add(psTrackEvaluationElements[6]); 
//        if(isReliableTransitions())svCurveEvaluations_Automated.add(psTrackEvaluationElements[16]); 
        if(verifiable(svCurveEvaluations_Automated))
            sInfoNodeStatus_Automatic=psInfoNodeStatus[3];
        else
            sInfoNodeStatus_Automatic=psInfoNodeStatus[2];
        if(ContainShortLevel()) svCurveEvaluations_Automated.add(psTrackEvaluationElements[18]);
        if(isArregated()) svCurveEvaluations_Automated.add(psTrackEvaluationElements[21]);
        if(isInvalidTrackEnding()) svCurveEvaluations_Automated.add(psTrackEvaluationElements[20]);
        evaluateLevels();  
        calMaxNonTransitionAmp();
        
        
        int index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Manual, "Transition Index=");
        if(index>=0){
            svCurveEvaluations_Manual.set(index,"Transition Index= "+PrintAssist.ToString(TransitionIndex_M, 2));
        }else{
            svCurveEvaluations_Manual.add("Transition Index= "+PrintAssist.ToString(TransitionIndex_M, 2));
        }
        
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Manual, "Max Nontransition Slice=");
        if(index>=0){
            svCurveEvaluations_Manual.set(index,"Max Nontransition Slice= "+PrintAssist.ToString(nMaxNonTransitionSlice_M, 0));
        }else{
            svCurveEvaluations_Manual.add("Max Nontransition Slice= "+PrintAssist.ToString(nMaxNonTransitionSlice_M, 0));
        }
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Manual, "Max Nontransition Delta=");
        if(index>=0){
            svCurveEvaluations_Manual.set(index,"Max Nontransition Delta= "+PrintAssist.ToString(MaxNonTransitionAmp_M, 2));
        }else{
            svCurveEvaluations_Manual.add("Max Nontransition Delta= "+PrintAssist.ToString(MaxNonTransitionAmp_M, 2));
        }
        
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Manual, "dSNR");
        if(index>=0) svCurveEvaluations_Manual.remove(index);
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Manual, "SNR");        
        if(index>=0){
            svCurveEvaluations_Manual.set(index,"SNR= "+PrintAssist.ToString(dSNR_M, 2));
        }else{
            svCurveEvaluations_Manual.add("SNR= "+PrintAssist.ToString(dSNR_M, 2));
        }
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Manual, "Max Level= ");
        if(index>=0){
            svCurveEvaluations_Manual.set(index,"Max Level= "+m_cvLevelNodes_M.get(0).nLevel);
        }else if(m_cvLevelNodes_M.size()>0){
            svCurveEvaluations_Manual.add("Max Level= "+m_cvLevelNodes_M.get(0).nLevel);
        }
        
        svCurveEvaluations_Automated.add("Transition Index= "+PrintAssist.ToString(TransitionIndex_A, 2));
        svCurveEvaluations_Automated.add("Max Level= "+m_cvLevelNodes_A.get(0).nLevel);
        svCurveEvaluations_Automated.add("Max Nontransition Slice= "+PrintAssist.ToString(nMaxNonTransitionSlice_A, 0));
        index=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Manual, "Transition Index=");
        svCurveEvaluations_Automated.add("Max Nontransition Delta= "+PrintAssist.ToString(MaxNonTransitionAmp_A, 2));
        svCurveEvaluations_Automated.add("Noise= "+PrintAssist.ToString(dNoise, 2));
        svCurveEvaluations_Automated.add("NoiseX= "+PrintAssist.ToString(dNoiseX, 2));
        calMaxTransitionIndexLevels();
        assignStoredIPOGLevels();
    }
    public void calNoise(){
        double[] pdDelta=CommonStatisticsMethods.getDeltaArray(pdY, null, nMaxRisingInterval);
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> nv=new ArrayList();
        CommonStatisticsMethods.findOutliars(pdDelta, 0.0001, ms, nv);
        dNoise=ms.getSD()/Math.sqrt(2.);
        
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList();
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);        
        ArrayList<Double> dvXs=new ArrayList();
        for(int i=0;i<nvLx.size();i++){
            dvXs.add(pdY[nvLx.get(i)]);
        }
        double[] pdYx=CommonStatisticsMethods.copyToDoubleArray(dvXs);
        pdDelta=CommonStatisticsMethods.getDeltaArray(pdYx, null, 1);
        ms=new MeanSem1();
        CommonStatisticsMethods.findOutliars(pdDelta, 0.001, ms, nv, 0, pdDelta.length-1, 1);
        dNoiseX=ms.getSD()/Math.sqrt(2.);
    }
    public void calSNR(){
        int i,len=m_cvLevelNodes_A.size();
        if(dNoise<0&&pdY!=null){
            calNoise();
        }
        dSNR_A=0;
        double dt;
        for(i=0;i<len-1;i++){
            dt=m_cvLevelNodes_A.get(i).delta/dNoise;
//            if(dt<dSNR_A) dSNR_A=dt;
            dSNR_A+=dt;
        }
        dSNR_A/=(double)(len-1);
        dSNR_M=0;
        len=m_cvLevelNodes_M.size();
        for(i=0;i<len-1;i++){
            dt=m_cvLevelNodes_M.get(i).delta/dNoise;
//            if(dt<dSNR_M) dSNR_M=dt;
            dSNR_M+=dt;
        }
        dSNR_M/=(double)(len-1);
    }
    int adjustEndLevel0(){
        int position=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Automated, psTrackEvaluationElements[5]);
        if(position<0) {
            nEndLevelA=0;
            return -1;
        }
        if(m_cvLevelNodes_A.size()<2) return -1;
        if(!isValidSubgroudLevel(m_cvLevelNodes_A,m_cvLevelNodes_A.size()-1))return -1;
        setEndLevelA(1);
        svCurveEvaluations_Automated.remove(position);
        return 1;
    }
    int adjustEndLevel(){
        nEndLevelA=0;
        int position=CommonStatisticsMethods.findStringPosition(svCurveEvaluations_Automated, psTrackEvaluationElements[5]);
        int len=m_cvLevelNodes_A.size(),index,i,pt,p;
        ArrayList<Integer> nvPositions=new ArrayList();
        if(len<2) return -1;
        int groundLevelIndex=getGoundLevelIndex();
        if(groundLevelIndex>0) {
            ArrayList<Integer> nvSlices=new ArrayList();
            for(index=len-2;index>=groundLevelIndex;index--){
                nvSlices.add(m_cvLevelNodes_A.get(index).sliceF);
                nvPositions.add(m_cvLevelNodes_A.get(index).nEnd);
            }
            for(i=0;i<nvSlices.size();i++){
                removeLevelTransition(m_cvLevelNodes_A,nvSlices.get(i));
                pt=nvPositions.get(i);
                for(p=pt;p<=Math.min(pt+nMaxRisingInterval, pdSigDiff.length-1);p++){
 //                   pdSigDiff[p]=0;
                }
            }
            if(position>=0)svCurveEvaluations_Automated.remove(position);
            return 1;
        }
        if(!isValidSubgroudLevel(m_cvLevelNodes_A,m_cvLevelNodes_A.size()-1))return -1;
        setEndLevelA(1);
        if(position>=0)svCurveEvaluations_Automated.remove(position);
        return 1;
    }
    int getGoundLevelIndex(){
        int len=m_cvLevelNodes_A.size(),j;
        if(len<2) return 0;
        int index=len-1;
        double delta;
        for(index=1;index<len;index++){
            if(isValidGroundLevel(index)) {
                if(index==len-1){
                    delta=m_cvLevelNodes_A.get(len-2).delta;
                    for(j=0;j<len-2;j++){
//                        if(delta<0.5*m_cvLevelNodes_A.get(j).delta){
                        if(false){//13211
                            ArrayList<IPOGTLevelNode> cvLNodes=m_cvLevelNodes_M;
                            m_cvLevelNodes_M=m_cvLevelNodes_A;
                            removeLevelTransition(m_cvLevelNodes_A.get(len-2).sliceF);
                            m_cvLevelNodes_A=m_cvLevelNodes_M;
                            m_cvLevelNodes_M=cvLNodes;
                            return -1;
                        }
                    }
                }
                return index;
            }
        }
        if(index<len-1) return index+1;
        return -1;
    }
    boolean isValidGroundLevel(int index){
        int len=m_cvLevelNodes_A.size();
        if(index<1) return false;
        IPOGTLevelNode lNode=m_cvLevelNodes_A.get(index),lNode1=m_cvLevelNodes_A.get(index-1);
        double ratioCutoff=6;
        int iF=Math.min(lNode.nStart+7, lNode.nEnd-1),iI=Math.min(lNode.nStart+2,iF);
        
        double ave=CommonStatisticsMethods.getMean(pdY, iI,iF, 1);   
        if(Math.abs(lNode1.dREnv/ave)>ratioCutoff) return true;
        
        ratioCutoff=2.5;
        if(lNode.dL>40) ratioCutoff=3.;
//        double delta=Math.max(lNode1.dSignalDelta,lNode1.delta),ratio=Math.abs(delta/lNode.dL),height=Double.POSITIVE_INFINITY;
        double delta=Math.max(lNode1.dSignalDelta,lNode1.delta),ratio=Math.abs(delta/pdSignal[lNode.nStart]),height=Double.POSITIVE_INFINITY;
//        double delta=lNode1.dR-lNode.dL,ratio=Math.abs(delta/lNode.dL);
        if(delta<0) return false;
        if(ratio>ratioCutoff) return true;
        if(index==4){
            index=index;
        }
        
        int pIndex=IPOAnalyzerForm.getHeightCutoffPIndex(0.01);
        IPOGaussianNode IPOGtt;
        if(IPOAnalyzerForm.ppdHeightCutoff!=null){
            if(lNode.nStart>=IPOAnalyzerForm.ppdHeightCutoff.length){
                pIndex=pIndex;
            }
            if(pIndex>=IPOAnalyzerForm.ppdHeightCutoff[0].length-1){
                pIndex=pIndex;
            }
            double hCutoff=IPOAnalyzerForm.ppdHeightCutoff[lNode.nStart][pIndex],hCutoff2=IPOAnalyzerForm.ppdHeightCutoff[lNode.nStart][Math.max(pIndex+1, IPOAnalyzerForm.ppdHeightCutoff[0].length-1)];
            if(lNode.m_cIPOG!=null){
                if(!lNode.cvIPOGs.isEmpty()){
                    IPOGtt=lNode.cvIPOGs.get(0);
                    for(int i=1;i<lNode.cvIPOGs.size();i++){
                        if(lNode.cvIPOGs.get(i).sliceF<IPOGtt.sliceF) IPOGtt=lNode.cvIPOGs.get(i);
                    }
                    IPOGContourParameterNode aNodet=IPOGtt.getContour(IPOGContourParameterNode.GaussianMean);
                    if(aNodet!=null){
                        height=aNodet.Height;
                        if(aNodet.Height<hCutoff) {
                            if(!IPOGaussianNodeHandler.isNormalContour_RF(lNode.m_cIPOG, IPOGContourParameterNode.GaussianMean));
                                return true;
                        }
                    }
                }
            }
        }
//        if(index>0&&ratio>1.8){
            if(lNode.m_cIPOG!=null&&lNode1.m_cIPOGR!=null){
                if(IPOGaussianNodeHandler.isFlat(lNode.m_cIPOG)&&lNode1.delta/lNode.mean>4) return true;
                if(IPOGaussianNodeHandler.isSharp(lNode.m_cIPOG)&&lNode1.delta/lNode.mean>4) return true;
                IPOGContourParameterNode aNode0=m_cvLevelNodes_A.get(index-1).m_cIPOGR.getContour(IPOGContourParameterNode.GaussianMean),aNode=lNode.m_cIPOG.getContour(IPOGContourParameterNode.GaussianMean);
                if(aNode0!=null&&aNode!=null&&isStableRegion(lNode.m_cIPOG))
                    if(Math.abs(aNode0.Height/aNode.Height)>4&&(ratio>1.5||height<10)) return true;
            }
//        }
        
        double sig1,sig;
        IPOGaussianNodeComplex IPOGR=lNode1.m_cIPOGR,IPOG=lNode.m_cIPOG;
        if(!isStableRegion(IPOG)) return false;
        ratioCutoff=3.5;
        if(IPOGR!=null&&IPOG!=null){
            sig1=IPOGR.getPeak3Cal();
            sig=IPOG.getPeak3Cal();
//            if(sig1/sig>ratioCutoff) return true;//13222
        }
/*        if(ratio<1.2) return false;
//        if(m_cvLevelNodes_A.get(len-2).isRegularShape()&&!m_cvLevelNodes_A.get(len-1).isRegularShape()) return true;12928
        if(m_cvLevelNodes_A.get(index-1).m_cIPOGR==null) return false;
        if(m_cvLevelNodes_A.get(index-1).m_cIPOGR.getMainIPOG().Amp/m_cvLevelNodes_A.get(index).m_cIPOG.getMainIPOG().Amp>ratioCutoff) return true;        
*/        
        if(index==len-2){
            if(m_cvLevelNodes_A.get(len-1).isRegularShape()) return false;
        }
        return false;
    }
    boolean isValidSubgroudLevel(ArrayList<IPOGTLevelNode> cvLNodes,int levelIndex){
        if(levelIndex<=0) return false;
        IPOGTLevelNode lNode0=cvLNodes.get(levelIndex), lNode=cvLNodes.get(levelIndex-1);
        double dL,dR,dt;
        int i;
        
        dL=lNode.dL;
        for(i=0;i<levelIndex;i++){
            if(cvLNodes.get(i).delta/dL>2.) return false;
        }
                
        double ratioCutoff=3,delta=lNode.delta,remain=lNode0.dL-delta;               
        if(remain<0&&Math.abs(remain)/delta<0.5) return true;
        double ratio=lNode0.dL/remain;
        if(Math.abs(ratio)>ratioCutoff) return true;
        
        int start=Math.min(CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode0.sliceI, 1)+2,pdEnvTrail.length-1);
        int end=start+10,end0=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, cIPOGT.lastSlice, 1);
        if(end>end0) end=end0;
        end=CommonStatisticsMethods.getFirstExtremumIndex(pdY, end, 1, -1);
        if(end<start) end=start;
        double drift=Math.abs(pdEnvTrail[start]-pdEnvTrail[end]);
        if(pdSignal!=null) drift=Math.abs(pdSignal[start]-pdSignal[end]);
        if(drift>0.5*Math.abs(delta)) return false;//too drifting to make judgement
        
        //comparing Signal Amp
        int left,right;
        if(pdSignal!=null){
            left=lNode.nEnd;
            right=Math.min(lNode0.nStart+2,pdSignal.length-1);
            dL=pdSignal[left];
            dR=pdSignal[right];
            dt=dL/(2*dR);
            if(dt<1.2&&dt>0.83) return true;
        }
        
        ArrayList<Double> dvt=new ArrayList();
        double mean=0;
        for(i=start;i<=end;i++){
            dt=pdY[i]-delta;
            dvt.add(dt);
            mean+=dt;
        }
//        if(mean<0) return true;
        
        double sd=pdSD[start];
        double p=HypothesisTester.tTest(dvt,0, sd);
        double dP=0.05;
        return p>dP;
    }
    boolean verifiable(){
        // psInfoNodeStatus={"Not Analyzed","Confirm Auto","Excluded","Verified"};
        if(!ManuallyConfirmed())
            return verifiable(svCurveEvaluations_Automated);
        else
            return verifiable(svCurveEvaluations_Manual);
    }

    public boolean ManuallyConfirmed(){
        return !sInfoNodeStatus_Manual.contentEquals(psInfoNodeStatus[0]);
    }

/*    public static final String[] psTrackEvaluationElements={"Not Analyzed","Over drifting","Heterogeneous delta","upper transitions","Overlapping particles",//5
                                                            "No last transition","Close to edges","Too flickery","Double Flicker","Short First Level",//10
                                                            "Too noisy","NA","Not Analyzable","Inconsistent","All Confirmed",//15
                                                            "All ConfirmedE","Reliable Transitions"};*/
    boolean verifiable(ArrayList<String> svEvaluations){
        synchronized(svEvaluations){
            int i,len=svEvaluations.size(),index,len1=m_cvLevelNodes_A.size(),j;
            String st;
            for(i=0;i<len;i++){
                if(i>=svEvaluations.size()){
                    i=i;
                    return true;
                }
                st=svEvaluations.get(i);
                index=getEvaluationIndex(st);
                if(index<0) 
                    continue;
                if(index==5){
                    if(nEndLevelA==1) 
                        continue;                
                }
                if(index==4){
    //                if(isReliableTransitions()) continue;
                }
                if(index==18){
                    continue;
                }
                if(index==20){
                    if(nEndLevelA==1) continue;
                }
                if(index==2){//Hetero Delta
                    if(isValidSubgroudLevel(m_cvLevelNodes_A,len1-2)){
                        IPOGTLevelNode lNode;
                        DoubleRange dr=new DoubleRange(), dr1=new DoubleRange();
                        for(j=0;j<len1-2;j++){
                            lNode=m_cvLevelNodes_A.get(j);
                            dr.expandRange(Math.abs(lNode.delta));
                            dr1.expandRange(Math.abs(lNode.dSignalDelta));                        
                        }
                        if(dr.getMax()/dr.getMin()>1.9&&dr1.getMax()/dr1.getMin()>1.9) return false;
                        removeLevelTransition(m_cvLevelNodes_A,m_cvLevelNodes_A.get(len1-2).sliceF);
                        evaluateSignal();
                        continue;
                    }
                }
                if(pbExcludable[index]) 
                    return false;
            }                 
            return true;
        }
    }
    public static int getEvaluationIndex(String st){
        int i,len=psTrackEvaluationElements.length;
        for(i=0;i<len;i++){
            if(st.contentEquals(psTrackEvaluationElements[i])) return i;
        }
        return -1;
    }
    boolean isValidGroundLevel(){
        int len=m_cvLevelNodes_A.size();
        if(cIPOGT.TrackIndex==85){
            len=len;
        }
        if(len<2) return false;
        IPOGTLevelNode lNode=m_cvLevelNodes_A.get(len-1),lNode1=m_cvLevelNodes_A.get(len-2);
        double ratioCutoff=2.;
        int start=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode.sliceI, 1),end=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, lNode.sliceF, 1);
        double[] pdYRW=CommonStatisticsMethods.getRunningWindowAverage(pdY, start, Math.min(start+20,end), 5, false);
        DoubleRange dr=CommonStatisticsMethods.getRange(pdYRW);
        double dRWA=dr.getMax();
        double delta=lNode1.dR-Math.max(lNode.dL,dRWA),ratio=Math.abs(delta/Math.max(lNode.dL,dRWA));
//        double delta=lNode1.dR-lNode.dL,ratio=Math.abs(delta/lNode.dL);
        if(delta<0) return false;
        if(ratio>ratioCutoff) return true;
        ratioCutoff=3;
//        if(m_cvLevelNodes_A.get(len-2).isRegularShape()&&!m_cvLevelNodes_A.get(len-1).isRegularShape()) return true;12928
        if(m_cvLevelNodes_A.get(len-2).m_cIPOGR==null) return false;
        if(m_cvLevelNodes_A.get(len-2).m_cIPOGR.getMainIPOG().Amp/m_cvLevelNodes_A.get(len-1).m_cIPOG.getMainIPOG().Amp>ratioCutoff) return true;
        return false;
    }
    public boolean isNormalShape(){
        int i,len=m_cvLevelNodes_A.size();
//        if(!IPOGaussianNodeHandler.isNormalShape(m_cTrackHeadIPOG)) return false;
        int iF=Math.max(0, len-2);
        if(m_cTrackHeadIPOG!=null)
            if(!IPOGTLevelNode.isRegularShape(m_cTrackHeadIPOG)) return false;
        for(i=0;i<=iF;i++){
            if(!m_cvLevelNodes_A.get(i).isRegularShape()) return false;
        }
        return true;
    }
    public boolean isNormalShape(int type0){
        int i,len=m_cvStoredIPOGs.size(),type=Math.abs(type0);
        IPOGaussianNodeComplex IPOG;
        boolean normal;
        for(i=0;i<len;i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<=0) continue;
            normal=IPOGaussianNodeHandler.isNormalShape_RF(IPOG, type);
            if(type0<0&&normal) return true;
            if(type0>0&&!normal) return false;
        }
        return true;
    }
    boolean isHeteroDelta(){
        double cutoff=1.9;
        if(cDeltaRangeA.getMax()/cDeltaRangeA.getMin()<cutoff) return false;
        if(!cSignalDeltaRangeA.isRegularRange()) return true;
        if(!cSignalDeltaRangeA.isValid()) return true;
        if(cSignalDeltaRangeA.getMax()/cSignalDeltaRangeA.getMin()<cutoff) return false;
        return true;
    }
    
    boolean containsUpperTransition(){
        int i,len=m_cvLevelNodes_A.size();
        IPOGTLevelNode lNode;
        boolean ut=false;
        for(i=0;i<len-1;i++){            
            lNode=m_cvLevelNodes_A.get(i);
            if(lNode.delta<0) return true;//higher level nodes are at lower indexes
        }
        return false;
    }
    boolean isCloseToEdge(){
        int i,len=m_cvLevelNodes_A.size();
        Point ct;
        int cutoff=3;
        IPOGTLevelNode lNode;
        boolean ut=false;
        IPOGaussianNodeComplex IPOG;
        FittingModelNode cNode=new FittingModelNode();
        cIPOGT.w=255;
        cIPOGT.h=255;
        for(i=0;i<len-1;i++){            
            lNode=m_cvLevelNodes_A.get(i);
            if(lNode.m_cIPOG==null) continue;
            IPOG=lNode.m_cIPOG;
            if(IPOG==null) continue;
            cNode.pdPars=IPOG.pdPars;
            if(cNode.pdPars!=null) {
                if(IPOGaussianExpander.isDummyIPOGModel(cNode)) continue;
            }
            ct=lNode.m_cIPOG.getIPOGs().get(0).getCenter();
            if(ct.x<cutoff||ct.x>=cIPOGT.w-cutoff) return true;
            if(ct.y<cutoff||ct.y>=cIPOGT.h-cutoff) return true;
        }
        return false;
    }
    public void calSD(){
        int[] pnIntervals=CommonStatisticsMethods.calRisingIntervals(pdY, nMaxRisingInterval);
        pdSD=CommonStatisticsMethods.getDeltaBasedPositionDependentSD(pdX, pdY, pnIntervals, pbSelected, 20);
    }
    public void evaluateLevels(){
        int i,len=m_cvLevelNodes_A.size();
        for(i=0;i<len;i++){
            m_cvLevelNodes_A.get(i).evaluate();
        }
    }
    public double[] getSD(){
        return pdSD;
    }
    public double getdPPWDev(){
        if(dPPWDev>0) return dPPWDev;
        return 0.001;
    }
    public double getdPChiSQ(){
        if(dPChiSQ>0) return dPChiSQ;
        return 0.05;
    }
    public double getdPSideness(){
        if(dPSideness>0) return dPSideness;
        return 0.01;
    }
    public double getdPTilting(){
        if(dPTilting>0) return dPTilting;
        return 0.01;
    }
    public double getdOutliarRatio(){
        if(dOutliarRatio>0) return dOutliarRatio;
        return 0;
    }
    public double getdPTerminals(){
        if(dPTerminals>0) return dPTerminals;
        return 0.05;
    }
    public void allConfirmed(boolean confirmed){
        AllConfirmed=confirmed;
    }
    public void allConfirmedE(boolean confirmed){
        AllConfirmedE=confirmed;
    }
//    public LineFeatureExtracter2(String sSignalName, double[] pdX, double[] pdY, double pdRx, int nMaxInterval, double dPChiSQ,double dPPWDev,double dPTilting, double dPSideness, double dPTerminals, int nOrder, boolean bCheckClusteringOnly){
/*    public static final String[] psTrackEvaluationElements={"Not Analyzed","Over drifting","Heterogeneous delta","upper transitions","Overlapping particles",//5
                                                            "No last transition","Close to edges","Too flickery","Double Flicker","Short First Level",//10
                                                            "Too noisy","NA","Not Analyzable","Inconsistent","All Confirmed",//15
                                                            "All ConfirmedE","Reliable Transitions"};*/
    public boolean isReliableTransitions(){
        boolean reliable=false;
        if(GapIndex<0) calGapIndex();
        if(TransitionIndex_A>0.55&&dSNR_A>3) reliable=true;
        return reliable;
    }
    public void calGapIndex(){
        MeanSem1[] pcClusterMeanSems=pcMeanDiffMeanSems;
        if(pcClusterMeanSems==null){
            LineFeatureExtracter2 cFeatureExtracter=new LineFeatureExtracter2("",pdX,pdY,nMaxRisingInterval,0.1,0.1,0.1,0.1,0.1,1,true);
            OneDKMeans cKM=cFeatureExtracter.getKMeans_MeanDiff();
            pcClusterMeanSems=cKM.getClusterMeanSems();
        }
        MeanSem1 ms0=pcClusterMeanSems[0], ms1=pcClusterMeanSems[1];
        GapIndex=Math.abs((ms1.min-ms0.max)/ms0.max);        
    }
    public static int getNumOfCurveEvaluationElements(){
        return psTrackEvaluationElements.length;
    }
    public void setMeanDiffMeanSems(MeanSem1[] pcMeanDiffMeanSem){
        this.pcMeanDiffMeanSems=pcMeanDiffMeanSem;
    }
    int exportAdditional (DataOutputStream ds)throws IOException {
        versionNumber=getCurrentVersionNumber();
       if(versionNumber <0) return -1; 
//       ds.writeInt(versionNumber);
       if(pcMeanDiffMeanSems==null) {
           ds.writeInt(0);
           return -1;
       }
       exportMeanDiffMeanSems(ds);
       if(versionNumber==1) return 1;
       exportEnvTrail(ds);
       if(versionNumber==2) return 2;
       exportSigDiff(ds);
       if(versionNumber==3) return 3;
       if(versionNumber==4) return 4;
       ds.writeInt(nEndLevelA);
       ds.writeInt(nEndLevelM);
       if(versionNumber==5) return 5;
       exportContourParNodes(ds);
       if(versionNumber==6) return 6;
       if(versionNumber==7) return 7;
       if(versionNumber==8) return 8;
       if(pdSignal==null){
           pdSignal=pdSignal;
       }
       IOAssist.writeDoubleArray(ds,pdSignal);
       if(versionNumber==9) return 9;
       exportStoredIPOGShapeCodes(ds);
       if(versionNumber==10) return 10;
       exportStoredIPOGMeritPValues(ds);
       if(versionNumber==11) return 11;
       exportStoredIPOGContourCodes(ds);
       if(versionNumber==13) return 13;
       exportStoredIPOGRawContourCodes(ds);
       return 1;
    }
    void exportStoredIPOGShapeCodes(DataOutputStream ds) throws IOException{
        ds.writeInt(m_cvStoredIPOGs.size());
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            ds.writeInt(m_cvStoredIPOGs.get(i).getIPOGCode());
        }
    }
    void exportStoredIPOGContourCodes(DataOutputStream ds) throws IOException{
        ds.writeInt(m_cvStoredIPOGs.size());
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            ds.writeInt(m_cvStoredIPOGs.get(i).getContourCode());
        }
    }
    void exportStoredIPOGRawContourCodes(DataOutputStream ds) throws IOException{
        ds.writeInt(m_cvStoredIPOGs.size());
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            ds.writeInt(m_cvStoredIPOGs.get(i).getRawContourCode());
        }
    }
    void exportStoredIPOGMeritPValues(DataOutputStream ds) throws IOException{
        ds.writeInt(m_cvStoredIPOGs.size());
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            ds.writeDouble(m_cvStoredIPOGs.get(i).dMeritPValue);
        }
    }
    void exportContourParNodes(DataOutputStream ds) throws IOException{
        ds.writeInt(cvStoredContourParNodes.size());
        for(int i=0;i<cvStoredContourParNodes.size();i++){
            if(cvStoredContourParNodes.get(i).PeakPoint==null){
                i=i;
            }
            cvStoredContourParNodes.get(i).exportNode(ds);
        }
    }
    int importAdditional (BufferedInputStream bf) throws IOException {
       if(versionNumber<4) versionNumber=IOAssist.readInt(bf);
       switch(versionNumber){
           case 1:
               importMeanDiffMeanSems(bf);
               nEndLevelM=nEndLevelA;
               break;
           case 2:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               nEndLevelM=nEndLevelA;
               break;
           case 3:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelM=nEndLevelA;
               break;
           case 4:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelM=nEndLevelA;
               break;
           case 5:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               break;
           case 6:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               break;               
           case 7:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               break;               
           case 8:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               break;               
           case 9:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               pdSignal=IOAssist.readDoubleArray(bf);
               break;               
           case 10:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               pdSignal=IOAssist.readDoubleArray(bf);
               importStoredIPOGShapeCodes(bf);
               break;               
           case 11:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               pdSignal=IOAssist.readDoubleArray(bf);
               importStoredIPOGShapeCodes(bf);
               importStoredIPOGMeritPValues(bf);
               break;               
           case 12:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               pdSignal=IOAssist.readDoubleArray(bf);
               importStoredIPOGShapeCodes(bf);
               importStoredIPOGMeritPValues(bf);
               importStoredIPOGContourCodes(bf);
               break;               
           case 13:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               pdSignal=IOAssist.readDoubleArray(bf);
               importStoredIPOGShapeCodes(bf);
               importStoredIPOGMeritPValues(bf);
               importStoredIPOGContourCodes(bf);
               break;               
           case 14:
               importMeanDiffMeanSems(bf);
               importEnvTrail(bf);
               importSigDiff(bf);
               nEndLevelA=IOAssist.readInt(bf);
               nEndLevelM=IOAssist.readInt(bf);
               importContourParNodes(bf);
               pdSignal=IOAssist.readDoubleArray(bf);
               importStoredIPOGShapeCodes(bf);
               importStoredIPOGMeritPValues(bf);
               importStoredIPOGContourCodes(bf);
               importStoredIPOGRawContourCodes(bf);
               break;               
           default:
               break;
       }
       return 1;
    }
    void importStoredIPOGMeritPValues(BufferedInputStream bf) throws IOException{
        int num=IOAssist.readInt(bf);
        double pV;
        for(int i=0;i<num;i++){
            pV=IOAssist.readDouble(bf);
            m_cvStoredIPOGs.get(i).setMeritPValue(pV);
        }
    }
    void importStoredIPOGShapeCodes(BufferedInputStream bf) throws IOException{
        int num=IOAssist.readInt(bf),code;
        for(int i=0;i<num;i++){
            code=IOAssist.readInt(bf);
            m_cvStoredIPOGs.get(i).setIPOGCode(code);
        }
    }
    void importStoredIPOGContourCodes(BufferedInputStream bf) throws IOException{
        int num=IOAssist.readInt(bf),code;
        for(int i=0;i<num;i++){
            code=IOAssist.readInt(bf);
            m_cvStoredIPOGs.get(i).setContourCode(code);
        }
    }
    void importStoredIPOGRawContourCodes(BufferedInputStream bf) throws IOException{
        int num=IOAssist.readInt(bf),code;
        for(int i=0;i<num;i++){
            code=IOAssist.readInt(bf);
            m_cvStoredIPOGs.get(i).setRawContourCode(code);
        }
    }
    void importContourParNodes(BufferedInputStream bf) throws IOException{
        int num=IOAssist.readInt(bf);
        IPOGContourParameterNode aNode;
        for(int i=0;i<num;i++){
            aNode=new IPOGContourParameterNode();
            aNode.version=versionNumber;
            aNode.importNode(bf);
            if(!aNode.Contained(cvStoredContourParNodes)) cvStoredContourParNodes.add(aNode);
        }
    }
    void exportMeanDiffMeanSems(DataOutputStream ds) throws IOException {
        int i,len=pcMeanDiffMeanSems.length;
        ds.writeInt(len);
        for(i=0;i<len;i++){
            pcMeanDiffMeanSems[i].exportMeanSem(ds);
        }        
    }
    void exportEnvTrail(DataOutputStream ds) throws IOException {
        int i,len=pdEnvTrail.length;
        ds.writeInt(len);
        for(i=0;i<len;i++){
            ds.writeDouble(pdEnvTrail[i]);
        }        
    }
    void exportSigDiff(DataOutputStream ds) throws IOException {
        int i,len=pdSigDiff.length;
        ds.writeInt(len);
        for(i=0;i<len;i++){
            ds.writeDouble(pdSigDiff[i]);
        }        
    }
    void importEnvTrail(BufferedInputStream bf)throws IOException{
        int i,len=IOAssist.readInt(bf);
        pdEnvTrail=IOAssist.readDoubleArray(bf,len);
    }
    void importSigDiff(BufferedInputStream bf)throws IOException{
        int i,len=IOAssist.readInt(bf);
        pdSigDiff=IOAssist.readDoubleArray(bf,len);
    }
    void importMeanDiffMeanSems(BufferedInputStream bf)throws IOException{
        int i,len;
        len=IOAssist.readInt(bf);
        MeanSem1 ms;
        pcMeanDiffMeanSems=new MeanSem1[len];
        for(i=0;i<len;i++){
            ms=new MeanSem1();
            ms.importMeanSem(bf);
            pcMeanDiffMeanSems[i]=ms;
        }        
    }
    void setVersionNumber(int versionNumber){
        this.versionNumber=versionNumber;
    }
    public int getMaxLevel_A(){
        return m_cvLevelNodes_A.get(0).nLevel;
    }
    public boolean isNumLevelsDetermined_A(){
        return !CommonStatisticsMethods.containsString(svCurveEvaluations_Automated, psTrackEvaluationElements[5]);
    }
    public void setEnvTrail(double[] pdTrail){
        pdEnvTrail=pdTrail;
    }
    public int excludeSmallTransitions(){
        int i,len=m_cvLevelNodes_A.size(),gap=-1,index;     
        IPOGTLevelNode lNode0=m_cvLevelNodes_A.get(len-1),lNode;
        
        int lastSlice=cIPOGT.lastSlice;
        for(i=0;i<m_cvLevelNodes_A.size();i++){
            lNode=m_cvLevelNodes_A.get(i);
            if(lNode.sliceI>lastSlice+nMaxRisingInterval+1) removeLevelTransition(m_cvLevelNodes_A,lNode.sliceI);
        }
        
        
        len=m_cvLevelNodes_A.size();        
        double[] pdDelta=new double[len];
        double ratio,cutoff=1.55;
        int[] pnIndexes=new int[len];
        for(i=0;i<len;i++){
//            pdDelta[i]=Math.abs(m_cvLevelNodes_A.get(i).delta);
            pdDelta[i]=Math.abs(m_cvLevelNodes_A.get(i).dSignalDelta);//11208
            pnIndexes[i]=i;
        }
        pdDelta[len-1]=0;
        QuickSort.quicksort(pdDelta, pnIndexes);
        ArrayList<Double> dvStepSizes=new ArrayList();
        for(i=1;i<len;i++){
            if(pnIndexes[i-1]==len-1) {
                dvStepSizes.add(pdDelta[i]);
                continue;
            }
            ratio=pdDelta[i]/pdDelta[i-1];
            if(i==len-1){
                cutoff=2.5;//for the case of double transition, make it harder if there are only one valid transition
            }
            dvStepSizes.add(pdDelta[i]);
            if(ratio>cutoff) gap=i;
        }
        if(gap<0) return -1;
        double delta0=pdDelta[gap-1],delta1=pdDelta[gap];
        calGapIndex();
        double TransitionIndex=(delta1-delta0)/delta0;
        if(TransitionIndex<this.TransitionIndex_A) {            
//            if(dvStepSizes.get(gap-2)/dvStepSizes.get(0)<cutoff) return -1;//121d11
            if(Math.abs(delta0)/dNoiseX>3) return -1;//121d11
//            return -1;
        }
        this.TransitionIndex_A=TransitionIndex;
        
        ArrayList<IPOGTLevelNode> cvLNodes=new ArrayList();
        for(i=0;i<gap;i++){
            index=pnIndexes[i];
            if(index!=len-1) cvLNodes.add(m_cvLevelNodes_A.get(index));
        }
        for(i=0;i<cvLNodes.size();i++){
            lNode=cvLNodes.get(i);
            if(pbSignificant!=null){
                if(pbSignificant[lNode.nEnd]&&TransitionIndex<1.5)
                    continue;
            }
            removeLevelTransition(m_cvLevelNodes_A,lNode.sliceF);
        }
        return 1;
    }
    public int calMaxNonTransitionAmp(){
        int len=pdX.length,i;
        if(pdSigDiff==null) pdSigDiff=CommonStatisticsMethods.getDeltaArray(pdEnvTrail, null, nMaxRisingInterval);
        boolean[] pbTransition=new boolean[len];
        CommonStatisticsMethods.setElements(pbTransition, false);
        MinTransitionAmp_A=Double.POSITIVE_INFINITY;
        MaxNonTransitionAmp_A=0;
        IPOGTLevelNode lNode;
        
        double dt;
        int end=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, cIPOGT.lastSlice, 1);
        int iF=end-nMaxRisingInterval;
        int peak=0,sign,j,left,right;
        for(i=0;i<m_cvLevelNodes_A.size()-1;i++){
            lNode=m_cvLevelNodes_A.get(i);
            dt=Math.abs(lNode.delta);
            if(MinTransitionAmp_A>dt) MinTransitionAmp_A=dt;
            sign=1;
            if(lNode.delta<0) sign=-1;
            left=Math.min(CommonStatisticsMethods.getFirstExtremumIndex(pdY, lNode.nEnd, sign, -1),lNode.nEnd-nMaxRisingInterval);
            if(left<0) left=1;
            right=Math.max(CommonStatisticsMethods.getFirstExtremumIndex(pdY, lNode.nEnd, -sign, 1),lNode.nEnd+nMaxRisingInterval);
            if(right<0) right=len-1;
            for(j=left;j<=right;j++){
                if(j<0||j>=len) continue;
                pbTransition[j]=true;
            }
/*            for(j=lNode.nEnd-nMaxRisingInterval;j<=lNode.nEnd+nMaxRisingInterval;j++){
                if(j<0||j>=len) continue;
                pbTransition[j]=true;
            }*/
        }
        nMaxNonTransitionSlice_M=0;
        nMaxNonTransitionSlice_A=0;
        for(i=1;i<iF;i++){//the first position is not considered in transition detection
            sign=1;
            if(pbTransition[i]) continue;
            if(pdSigDiff[i]<0) sign=-1;
            dt=Math.abs(pdSigDiff[i]);
            if(dt>MaxNonTransitionAmp_A){ 
                MaxNonTransitionAmp_A=dt;
                nMaxNonTransitionSlice_A=(int)(pdX[i]+0.5);
            }
        }
        TransitionIndex_A=(MinTransitionAmp_A-MaxNonTransitionAmp_A)/MaxNonTransitionAmp_A;

        CommonStatisticsMethods.setElements(pbTransition, false);
        MinTransitionAmp_M=Double.POSITIVE_INFINITY;
        MaxNonTransitionAmp_M=0;
        end=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, cIPOGT.lastSlice, 1);
        iF=end-nMaxRisingInterval;
        peak=0;
        for(i=0;i<m_cvLevelNodes_M.size()-1;i++){
            lNode=m_cvLevelNodes_M.get(i);
            dt=Math.abs(lNode.delta);
            if(MinTransitionAmp_M>dt) MinTransitionAmp_M=dt;
            sign=1;
            if(lNode.delta<0) sign=-1;
            left=Math.min(CommonStatisticsMethods.getFirstExtremumIndex(pdY, lNode.nEnd, sign, -1),lNode.nEnd-nMaxRisingInterval);
            if(left<0) left=1;
            right=Math.max(CommonStatisticsMethods.getFirstExtremumIndex(pdY, lNode.nEnd, -sign, 1),lNode.nEnd+nMaxRisingInterval);
            if(right<0) right=len-1;
            for(j=left;j<=right;j++){
                if(j<0||j>=len) continue;
                pbTransition[j]=true;
            }
/*            for(j=lNode.nEnd-nMaxRisingInterval;j<=lNode.nEnd+nMaxRisingInterval;j++){
                if(j<0||j>=len) continue;
                pbTransition[j]=true;
            }*/
        }
        for(i=0;i<iF;i++){
            sign=1;
            if(pbTransition[i]) continue;
            if(pdSigDiff[i]<0) sign=-1;
            dt=Math.abs(pdSigDiff[i]);
            if(dt>MaxNonTransitionAmp_M) {
                MaxNonTransitionAmp_M=dt;
                nMaxNonTransitionSlice_M=(int)(pdX[i]+0.5);
            }
        }
        TransitionIndex_M=(MinTransitionAmp_M-MaxNonTransitionAmp_M)/MaxNonTransitionAmp_M;
        return 1;
    }
    public void setSigDiff(double[] pdSigDiff){
        this.pdSigDiff=pdSigDiff;
        versionNumber=getCurrentVersionNumber();
    }
    public int copyManualLevelInfo(IPOGTLevelInfoNode aNode){
        if(aNode==null) return -1;
        m_cvLevelNodes_M=new ArrayList();
        svCurveEvaluations_Automated=aNode.svCurveEvaluations_Automated;
        sTransitionConfidenceM=aNode.sTransitionConfidenceM;
        cDeltaRangeM=aNode.cDeltaRangeM;
        sInfoNodeStatus_Manual=aNode.sInfoNodeStatus_Manual;      
        m_cvStoredIPOGs=aNode.m_cvStoredIPOGs;
        int i,len=aNode.m_cvLevelNodes_M.size();
        m_cvLevelNodes_M.clear();
        for(i=0;i<len;i++){
            m_cvLevelNodes_M.add(aNode.m_cvLevelNodes_M.get(i));
        }
        return 1;
    }
    public static int getCurrentVersionNumber(){
//        return 4;
//        return 7;//13115
//        return 8;//13128
//       return 9;//13208
//        return 10;//13225
//        return 11;//13301
//        return 12;//13303
//        return 13;//13304
        return 14;//13304
    }
    public void setComputeShape(boolean compute){
        bComputeShape=compute;
    }
    public boolean ComputeShape(){
        return bComputeShape;
    }
    public int FillDummyIPOGs(){
        IPOGaussianNodeComplex IPOGt=new IPOGaussianNodeComplex(IPOGaussianExpander.getDummyIPOGModel().pdPars);
        if(m_cTrackHeadIPOG==null) m_cTrackHeadIPOG=IPOGt;
        IPOGTLevelNode lNode;
        for(int i=0;i<m_cvLevelNodes_A.size();i++){
            lNode=m_cvLevelNodes_A.get(i);
            if(lNode.m_cIPOG==null){
                lNode.m_cIPOG=IPOGt;
                lNode.m_cIPOGR=IPOGt;
            }
        }
        for(int i=0;i<m_cvLevelNodes_M.size();i++){
            lNode=m_cvLevelNodes_M.get(i);
            if(lNode.m_cIPOG==null){
                lNode.m_cIPOG=IPOGt;
                lNode.m_cIPOGR=IPOGt;
            }
        }
        return 1;
    }
    public boolean Stored(IPOGaussianNodeComplex IPOG0){
        IPOGaussianNodeComplex IPOG;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.sliceI==IPOG0.sliceI&&IPOG.sliceF==IPOG0.sliceF) return true;
        }
        return false;
    }
/*     public int storeIPOGs(){
        int i,len=m_cvLevelNodes_A.size(),sI,sF,nWs=30;
        IPOGTLevelNode lNode;
        IPOGaussianNodeComplex IPOG;
//        m_cvStoredIPOGs.clear();     
//        m_cTrackHeadIPOG.sliceI=sI;//12n24, remove it later
//        m_cTrackHeadIPOG.sliceF=sF;//12n24
        if(Stored(m_cTrackHeadIPOG))m_cvStoredIPOGs.add(m_cTrackHeadIPOG);
        for(i=0;i<len;i++){
            lNode=m_cvLevelNodes_A.get(i);
//            lNode.m_cIPOG.sliceI=sI;//12n24, remove it later
//            lNode.m_cIPOG.sliceF=sF;//12n24, remove it later
            if(!Stored(lNode.m_cIPOG)) m_cvStoredIPOGs.add(lNode.m_cIPOG);
            
//            lNode.m_cIPOGR.sliceI=sI;//12n24, remove it later
//            lNode.m_cIPOGR.sliceF=sF;//12n24, remove it later
            if(!Stored(lNode.m_cIPOG)) m_cvStoredIPOGs.add(lNode.m_cIPOGR);
        }
        sortStoredIPOGs(m_cvStoredIPOGs);
        return 1;
    }*/
     public int sortStoredIPOGs(ArrayList<IPOGaussianNodeComplex> cvIPOGs){
         if(cvIPOGs==null) return -1;
         if(cvIPOGs.isEmpty()) return -1;
         int in=0,i,j,len=cvIPOGs.size();
         IPOGaussianNodeComplex IPOGn=cvIPOGs.get(0),IPOG;
         for(i=0;i<len;i++){
             in=i;
             IPOGn=cvIPOGs.get(i);
             for(j=i+1;j<len;j++){
                IPOG=cvIPOGs.get(i);
                if(IPOG.sliceI<IPOGn.sliceI){
                    IPOGn=IPOG;
                    in=i;
                }else if(IPOG.sliceI==IPOGn.sliceI&&IPOG.sliceF<IPOGn.sliceF){
                    IPOGn=IPOG;
                    in=i;
                }
             }
             if(in>i){
                 IPOG=cvIPOGs.get(i);
                 cvIPOGs.set(i,IPOGn);
                 cvIPOGs.set(in, IPOG);
             }
         }
         return 1;
     }
     public int getMaxSegLength(){
         return nMaxSegLength;
     }
     public int getTrackHeadLength(){
         return nTrackHeadLength;
     }
     
    public int restoreIPOGs(){
        int i,sI=cIPOGT.firstSlice,sF=Math.min(sI+nTrackHeadLength, cIPOGT.lastSlice);
        IPOGaussianNodeComplex IPOG=retrieveStoredIPOG(sI,sF);
        m_cTrackHeadIPOG=IPOG;
        if(IPOG!=null) IPOG.setLevel(100);
        IPOGTLevelNode lNode;
        assignStoredIPOGLevels();

        for(i=0;i<m_cvLevelNodes_A.size();i++){
            lNode=m_cvLevelNodes_A.get(i);
            sI=lNode.sliceI;
            sF=Math.min(sI+nMaxSegLength,lNode.sliceF);            
            IPOG=retrieveStoredIPOG_WithinRange(sI,sF,-1);
            lNode.m_cIPOG=IPOG;
            if(IPOG!=null){
                if(IPOG.level!=100&&IPOG.level!=-100)IPOG.setLevel(lNode.nLevel);
            }
            
            sF=lNode.sliceF;
            sI=Math.max(lNode.sliceI, sF-nMaxSegLength);
            IPOG=retrieveStoredIPOG_WithinRange(sI,sF,1);
            lNode.m_cIPOGR=IPOG;
            if(IPOG!=null) {
                if(IPOG.level!=100&&IPOG.level!=-100) IPOG.setLevel(lNode.nLevel);
            }            
        }
        int len=m_cvStoredIPOGs.size();
        IPOG=m_cvStoredIPOGs.get(len-1);
        if(IPOG.level==-1) m_cTrackTailIPOG=IPOG;
        restoreContourParameterNodes();
        return 1;
    }
    public void clearIPOGs(){
        int i,len=m_cvStoredIPOGs.size(),it;
        for(i=0;i<len;i++){
            it=len-1-i;
            if(m_cvStoredIPOGs.get(it).ManuallyEvaluatedIPOG()) continue;
            m_cvStoredIPOGs.remove(it);
        }
        len=m_cvLevelNodes_A.size();
        for(i=0;i<len;i++){
            m_cvLevelNodes_A.get(i).clearIPOGs();
        }
        cvStoredContourParNodes.clear();
    }
    public int restoreContourParameterNodes(){
        int i,len=m_cvStoredIPOGs.size();
        for(i=0;i<len;i++){
            m_cvStoredIPOGs.get(i).storeContourParNodes(cvStoredContourParNodes);
        }
        return 1;
    }
    public boolean StoredIPOGs(){
        return !m_cvStoredIPOGs.isEmpty();
    }
    public IPOGaussianNodeComplex retrieveStoredIPOG(int sI, int sF){
        IPOGaussianNodeComplex IPOG;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.sliceI==sI&&IPOG.sliceF==sF) {
                IPOG.storeContourParNodes(cvStoredContourParNodes);
                return IPOG;
            }
        }
        return null;
    }
    public IPOGaussianNodeComplex retrieveStoredIPOG_WithinRange(int sI, int sF, int SelectionOption){//SelectionOption=1 to chose the one whose sliceI closest to sI, -1 to 
        //choose the one whose sliceF closest to sF
        IPOGaussianNodeComplex IPOG,IPOGt;
        intRange ir=new intRange(sI,sF);
        int in,nn,n,len,i;
        ArrayList<IPOGaussianNodeComplex> IPOGs=new ArrayList();
        for(i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(ir.contains(IPOG.sliceI)&&ir.contains(IPOG.sliceF)) {
                IPOGs.add(IPOG);
            }
        }
        len=IPOGs.size();
        if(len<1) return null;
        nn=Integer.MAX_VALUE;
        in=0;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            if(SelectionOption==1)
                n=IPOG.sliceI-sI;
            else
                n=sF-IPOG.sliceF;
            
            if(n<nn){
                nn=n;
                in=i;
            }
        }
        return IPOGs.get(in);
    }
    public int storeIPOG(IPOGaussianNodeComplex IPOG){
        if(IPOGaussianFitter.invalidIPOG(IPOG, null, null)) return -1;
        if(!Stored(IPOG)) m_cvStoredIPOGs.add(IPOG);
        storeContourParNodes(IPOG.cvContourParNodes);
        return 1;
    }
    public int storeContourParNodes(ArrayList<IPOGContourParameterNode> cvContourNodes){
        if(cvContourNodes==null) return -1;
        for(int i=0;i<cvContourNodes.size();i++){
            storeContourParNode(cvContourNodes.get(i));
        }
        return 1;
    }
    public int storeContourParNode(IPOGContourParameterNode aNode){
        if(aNode==null) return -1;
        int p=aNode.findPosition(cvStoredContourParNodes);
        if(p>=0) 
            cvStoredContourParNodes.set(p,aNode);
        else
            cvStoredContourParNodes.add(aNode);
        return 1;
    }
    public void setSignal(double[] pdSig){
        pdSignal=pdSig;
    }
    public boolean isStableRegion(int iI, int iF){
        if(iF-iI<5) return true;
        int im=(iI+iF)/2;
        double s1=CommonStatisticsMethods.getMean(pdY, iI, im, 1), s2=CommonStatisticsMethods.getMean(pdY, im,iF, 1),ratio=s1/s2;
        if(ratio<1) ratio=1./ratio;
        double cutoff=1.5;
        return ratio<cutoff;
    }
    public boolean isStableRegion(IPOGaussianNode IPOG){
        if(IPOG==null) return false;
        int iI=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, IPOG.sliceI, 1.),iF=CommonStatisticsMethods.getPositionInArrayWithMissingPoints(pdX, IPOG.sliceF, 1.);
        if(iI<0||iF<0) return false;
        return isStableRegion(iI,iF);
    }
    public boolean isOverDrifting(){
        for(int i=1;i<m_cvLevelNodes_A.size()-1;i++){//ignore the drift of the first level
            if(m_cvLevelNodes_A.get(i).isOverdrifting()) return true;
        }
        return false;
    }
    public boolean ContainShortLevel(){//the first and the last levels are allowed to be short
        for(int i=1;i<m_cvLevelNodes_A.size()-1;i++){
            if(m_cvLevelNodes_A.get(i).isShort()) return true;
        }
        return false;
    }
    public boolean isTooNoisy(){
        if(dSNR_A<2.5&&GapIndex<0.5) 
            return true;
        else if(dSNR_A<2&&TransitionIndex_A<1) 
            return true;      
        return false;
    }
    public boolean isSalientTransitions(){
        int p0,p,l,r,i,sign,len=m_cvLevelNodes_A.size();
        double dL,dt,dR,cutOff=1.5;
        boolean[] pbt=new boolean[pdY.length];
        CommonStatisticsMethods.setElements(pbt, true);
        for(i=0;i<len-1;i++){
            p=m_cvLevelNodes_A.get(i).nEnd;
            l=Math.max(0, p-nMaxRisingInterval+1);
            r=Math.min(pdY.length-1, p+nMaxRisingInterval-1);
            for(p=l;p<=r;p++){
                pbt[p]=false;
            }
        }
        for(i=0;i<len-1;i++){
            p0=m_cvLevelNodes_A.get(i).nEnd;
            dt=Math.abs(pdSigDiff[p0]);
            sign=1;
            if(pdSigDiff[p0]<0) sign=-1;
            l=CommonStatisticsMethods.getFirstExtremumIndex(pdSigDiff, p0-1, sign, -1);
            if(l<0) l=0;
            l=Math.max(l, p0-2-nMaxRisingInterval);//13203
            dL=Math.abs(pdSigDiff[l]);
            if(pbt[l]&&dt/dL<cutOff) return false;
            
            l=CommonStatisticsMethods.getFirstExtremumIndex(pdSigDiff, p0-1, -sign, -1);
            if(l<0) l=0;            
            l=Math.max(l, p0-2-nMaxRisingInterval);//13203
            dL=Math.abs(pdSigDiff[l]);
            
            if(pbt[l]&&dt/dL<cutOff) return false;
            
            r=CommonStatisticsMethods.getFirstExtremumIndex(pdSigDiff, p0+1, sign, 1);
            if(r<0) r=pdSigDiff.length-1;
            r=Math.min(r, p0+nMaxRisingInterval+2);//13203            
            dR=Math.abs(pdSigDiff[r]);
            if(pbt[r]&&dt/dR<cutOff) return false;
            
            r=CommonStatisticsMethods.getFirstExtremumIndex(pdSigDiff, p0+1, -sign, 1);
            if(r<0) r=pdSigDiff.length-1;
            r=Math.min(r, p0+nMaxRisingInterval+2);//13203            
            dR=Math.abs(pdSigDiff[r]);
            if(pbt[r]&&dt/dR<cutOff) return false;
        }
        return true;
    }
    IPOGaussianNodeComplex getTrackEndIPOG(){
        int sI=cIPOGT.lastSlice+nMaxRisingInterval,sF=cIPOGT.lastSliceE;
        IPOGaussianNodeComplex IPOG=retrieveStoredIPOG(sI,sF);
        if(IPOG==null) IPOG=m_cvLevelNodes_A.get(m_cvLevelNodes_A.size()-1).m_cIPOGR;
        return IPOG;
    }
    boolean isBleachedTrackEnd(){
        int len=m_cvLevelNodes_A.size(),sI=(int)(pdX[pdY.length-6]+0.5),sF=(int)(pdX[pdX.length-1]+0.5);
        if(len>1){
            double delta=m_cvLevelNodes_A.get(len-2).dR,cutOff=4,res=CommonStatisticsMethods.getMean(pdY,pdY.length-5,pdY.length-1,1);
            IPOGaussianNodeComplex IPOG=retrieveStoredIPOG_WithinRange(sI,sF,-1);
            if(IPOG!=null){
                IPOGContourParameterNode ipc=IPOG.getContour(IPOGContourParameterNode.GaussianMean);
                if(ipc!=null) res=ipc.Height;
                if(res<0) return true;
            }
            return Math.abs(delta/res) >cutOff;
        }
        return false;
    }
 /*   boolean isBleachedTrackEnd(){
        IPOGaussianNodeComplex IPOG=getTrackEndIPOG();
        IPOGContourParameterNode pNode=IPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw),pNodeHead=m_cTrackHeadIPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw);
        if(pNodeHead==null) return false;
        if(pNode==null) return false;
        double ave=pNodeHead.HeightIPOG/m_cvLevelNodes_A.size(),cutOff=4;
        return ave/pNode.HeightIPOG>cutOff;
    }*/
    boolean isInvalidTrackEnding(){
        return (!isBleachedTrackEnd()&&cIPOGT.lastSliceE<IPOAnalyzerForm.getLastSlice());
    }
    boolean isArregated(){
        int i,len=m_cvStoredIPOGs.size();
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode pNode;
        for(i=0;i<len;i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<1) continue;
            pNode=IPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw);
            if(pNode==null) pNode=IPOG.getContour(IPOGContourParameterNode.GaussianMean);
            if(pNode==null) continue;
            if(isAggregated(pNode)) return true;
        }
        return false;
    }
    static public boolean isAggregated(IPOGContourParameterNode pNode){
        DoubleRange sRange=pNode.getSigmaRange();
        double ratio=sRange.getMax()/sRange.getMin();
        if(ratio*ratio>2) return true;
        DoubleRange validSRange=new DoubleRange(1.2,1.9);
        if(pNode.type==IPOGContourParameterNode.GaussianMean)validSRange.setRange(1.4, 2.2);
        if(pNode.type==IPOGContourParameterNode.GaussianMeanRaw)validSRange.setRange(1.4, 2.3);
        if(pNode.Sigma>validSRange.getMax()) return true;
        if(pNode.nConcaveCurves>0) return true;
        return false;
    }
    public int getLevelInfoAsString(StringBuffer names,StringBuffer values, String quantityName ,boolean Manual){
        if(cIPOGT==null) return -1;
        cIPOGT.getLevelInfoAsString(names, values, quantityName, Manual);
        return 1;
    }
    public double getSignalInfoValue(int option){
        if(option==0) 
            return dSNR_A;
        if(option==1)
            return TransitionIndex_A;
        if(option==2) 
            return dSNR_M;
        if(option==3)
            return TransitionIndex_M;
        return Double.NaN;
    }
    public double getMaxAixisRatio(){
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode cpNode;
        DoubleRange dr=new DoubleRange();
        double ratio;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<=0)
                continue;
            if(IPOG.sliceF==cIPOGT.lastSliceE) 
                continue;
            cpNode=IPOG.getContour(IPOGContourParameterNode.GaussianMean);
            if(cpNode==null) continue;
            ratio=cpNode.cDistRangeX.getMax()/cpNode.cDistRangeN.getMin();
            dr.expandRange(ratio);
        }
        return dr.getMax();
    }
    public double getMaxAixisRatioRaw(){
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode cpNode;
        DoubleRange dr=new DoubleRange();
        double ratio;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<=0)
                continue;
            if(IPOG.sliceF==cIPOGT.lastSliceE) 
                continue;
            cpNode=IPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw);
            if(cpNode==null) continue;
            ratio=cpNode.cDistRangeX.getMax()/cpNode.cDistRangeN.getMin();
            dr.expandRange(ratio);
        }
        return dr.getMax();
    }
    public DoubleRange getSigmaRange(){
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode cpNode;
        DoubleRange dr=new DoubleRange();
        double sigma;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<=0)
                continue;
            if(IPOG.sliceF==cIPOGT.lastSliceE) 
                continue;
            cpNode=IPOG.getContour(IPOGContourParameterNode.GaussianMean);
            if(cpNode==null) continue;
            sigma=cpNode.Sigma;
            dr.expandRange(sigma);
        }
        return dr;
    }
    public DoubleRange getSigmaRangeRaw(){
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode cpNode;
        DoubleRange dr=new DoubleRange();
        double sigma;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<=0)
                continue;
            if(IPOG.sliceF==cIPOGT.lastSliceE) 
                continue;
            cpNode=IPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw);
            if(cpNode==null) continue;
            sigma=cpNode.Sigma;
            dr.expandRange(sigma);
        }
        return dr;
    }
    public int getNumConcaveCurves(){
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode cpNode;
        DoubleRange dr=new DoubleRange();
        int num;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<=0)
                continue;
            if(IPOG.sliceF==cIPOGT.lastSliceE) 
                continue;
            cpNode=IPOG.getContour(IPOGContourParameterNode.GaussianMean);
            if(cpNode==null) continue;
            num=cpNode.nConcaveCurves;
            dr.expandRange(num);
        }
        return (int)(dr.getMax()+0.5);
    }
    public int getNumConcaveCurvesRaw(){
        IPOGaussianNodeComplex IPOG;
        IPOGContourParameterNode cpNode;
        DoubleRange dr=new DoubleRange();
        int num;
        for(int i=0;i<m_cvStoredIPOGs.size();i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level<=0)
                continue;
            if(IPOG.sliceF==cIPOGT.lastSliceE) 
                continue;
            cpNode=IPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw);
            if(cpNode==null) continue;
            num=cpNode.nConcaveCurves;
            dr.expandRange(num);
        }
        return (int)(dr.getMax()+0.5);
    }
    public void markSignificantTransitions(boolean[] pbSignificant){
        this.pbSignificant=pbSignificant;
    }
    public static int SelectionStatus(IPOGTLevelInfoNode aInfo, SelectionCriterionNode cCriterion){//1 for selected, -1 for excluded, and other numbers are for ignored
        int option=cCriterion.option;
        switch(option){
            case 100://automatically verified
                break;
            case 200://automatically excluded
                break;
            case 300://manually verified
                break;
            case 400://manually excluded
                break;
            case 500://Consistent transition detection
                break;
            case 600: 
                break;
            default:
                break;
        }
        return SelectionCriterionNode.Ignore;
    }
    public int assignStoredIPOGLevels(){
        sortStoredIPOGs();
        int i,len=m_cvStoredIPOGs.size(),j,len1=m_cvLevelNodes_A.size();
        IPOGTLevelNode lNode,lNodex;
        IPOGaussianNodeComplex IPOG;
        int nOverlap,ovX,jx;
        intRange irIPOG=new intRange(), irLevel=new intRange(),irOverlap;
        for(i=0;i<len;i++){
            IPOG=m_cvStoredIPOGs.get(i);
            if(IPOG.level==100) {
                m_cTrackHeadIPOG=IPOG;
                continue;
            }
            if(IPOG.level==-100) {
                m_cTrackTailIPOG=IPOG;
                continue;
            }
            irIPOG.setRange(IPOG.sliceI, IPOG.sliceF);
            ovX=-1;
            lNodex=null;
            for(j=0;j<len1;j++){
                lNode=m_cvLevelNodes_A.get(j);
                irLevel.setRange(lNode.sliceI, lNode.sliceF);
                nOverlap=irIPOG.overlappedRange(irLevel).getRange();
                if(nOverlap>ovX){
                    ovX=nOverlap;
                    lNodex=lNode;
                }                
            }
            if(lNodex==null) continue;
            IPOG.setLevel(lNodex.nLevel);
            lNodex.cvIPOGs.add(IPOG);
        }
        return 1;
    }
    void sortStoredIPOGs(){
        int i,len=m_cvStoredIPOGs.size(),j,in,slice,sn;
        IPOGaussianNodeComplex IPOG,IPOGn,IPOGt;
        for(i=0;i<len;i++){
            IPOGn=m_cvStoredIPOGs.get(i);
            sn=IPOGn.sliceI;
            in=i;
            for(j=i+1;j<len;j++){
                IPOG=m_cvStoredIPOGs.get(j);
                slice=IPOG.sliceI;
                if(slice<sn){
                    IPOGn=IPOG;
                    sn=slice;
                    in=j;
                }else if(slice==sn&&IPOG.sliceF<IPOGn.sliceF){
                    IPOGn=IPOG;
                    sn=slice;
                    in=j;
                }
            }
            if(in==i) continue;
            IPOGt=m_cvStoredIPOGs.get(i);
            m_cvStoredIPOGs.set(i, IPOGn);
            m_cvStoredIPOGs.set(in, IPOGt);
        }
    }
    public boolean Fitted(){
        return pdSigDiff!=null;
    }
}
