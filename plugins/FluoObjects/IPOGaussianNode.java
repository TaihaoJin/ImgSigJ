/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import ImageAnalysis.ContourFollower;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.Constrains.ConstraintNode;
import utilities.Geometry.ImageShapes.ImageShape;
import java.awt.Point;
import utilities.CustomDataTypes.intRange;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.CustomDataTypes.IndexValuePairNode;
import utilities.io.PrintAssist;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.io.PrintAssist;
import ImageAnalysis.TwoDFunction;
import utilities.CommonMethods;
import utilities.CustomDataTypes.DoublePair;
import ImageAnalysis.ContourParameterNode;
import ij.ImagePlus;
import utilities.Gui.AnalysisMasterForm;
import utilities.CommonGuiMethods;
import ImageAnalysis.SubpixelGaussianMean;
import FluoObjects.IPOGContourParameterNode;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Gui.MasterFittingGUI;
/**
 *
 * @author Taihao
 */
//public class IPOGaussianNode extends IntensityPeakObject {
public class IPOGaussianNode implements TwoDFunction{
    public int IPOIndex,sliceIndex,TrackIndex,BundleIndex;
    public String sType="gaussian2D_GaussianPars";
    public int rIndex,cIndex;
    public int area,level;
    public ArrayList<IPOGaussianNode> cvNeighboringIPOs;
    public ArrayList<IndexValuePairNode>  cvOverlaps;
    public double cnst,Amp,sigmax,sigmay,thetaDegree;
    public double xc,yc;
    public double dTotalSignalCal,dTotalSignal,peak1,peak3,dBackground;
    public DoubleRange cXRange2,cYRange2, cXRange3,cYRange3;//two and three sigma ranges
    public double lowEnd;
    public double[] pdPars;
    public boolean converged;
    public ArrayList<Integer> cvIndexesInCluster;
    public static ComposedFittingFunction Gaussian2D_GaussianPars;
    public int xcr,ycr;//the peak position of the region
    public static final int nNumAdditionalPars=18;
    double preOvlp,postOvlp;
    public double dDrift;
    public int preRid,postRid;
    public double dBundleTotalSignal;
    public double peak1Raw,peak3Raw;
    public ArrayList<Integer> nvTrkIndexesInBundle;
    public FittingModelNode cModel;
    public int sliceI,sliceF;
    public double ContourCutoff;
    boolean Dummy;
    ContourParameterNode ContourPar;
    ArrayList<Point> Contour;
    ArrayList<DoublePair> SubpixelContour;
    ArrayList<IPOGContourParameterNode> cvContourParNodes;
    int ContourMag;
    int TwoDFunctionType;
    TwoDFunction TwoDFun;
    boolean ShowContour;
    int nIPOGCode,nContourCode,nRawContourCode;
    double dMeritPValue,dHPValue;
    public IPOGaussianNode(){
        Dummy=false;
        cvContourParNodes=new ArrayList();
        cvNeighboringIPOs=new ArrayList();
        cvOverlaps=new ArrayList();
        cvIndexesInCluster=new ArrayList();
        lowEnd=10;//lowEnd is the value used to determine the effective variable ranges.
        TrackIndex=-1;
        BundleIndex=-1;
        IPOIndex=-1;
        sliceIndex=-1;
        level=-1;
        dBundleTotalSignal=0;
        nIPOGCode=-1;
        nContourCode=-1;
        nRawContourCode=-1;
        if(Gaussian2D_GaussianPars==null){
            ArrayList<String> svTypes=new ArrayList();
            String type="gaussian2D_GaussianPars";
            svTypes.add(type);
            Gaussian2D_GaussianPars=new ComposedFittingFunction(svTypes);
        }
        dMeritPValue=-1;
    }
    public void copy(IPOGaussianNode IPOG){
            if(IPOG.pdPars!=null) 
                pdPars=CommonStatisticsMethods.copyArray(IPOG.pdPars);
            else
                pdPars=IPOG.pdPars;
            Amp=IPOG.Amp;
            xcr=IPOG.xcr;
            ycr=IPOG.ycr;
            xc=IPOG.xc;
            yc=IPOG.yc;
            TrackIndex=IPOG.TrackIndex;
            BundleIndex=IPOG.BundleIndex;
            rIndex=IPOG.rIndex;
            cIndex=IPOG.cIndex;
            sliceIndex=IPOG.sliceIndex;
            dTotalSignal=IPOG.dTotalSignal;
//            dTotalSignalCal=IPOG.dTotalSignalCal;
            dBundleTotalSignal=IPOG.dBundleTotalSignal;
            dBackground=IPOG.dBackground;
            area=IPOG.area;
            preOvlp=IPOG.preOvlp;
            preRid=IPOG.preRid;
            postOvlp=IPOG.postOvlp;
            postRid=IPOG.postRid;
            peak1=IPOG.peak1;
            peak3=IPOG.peak3;
            peak1Raw=IPOG.peak1Raw;
            peak3Raw=IPOG.peak3Raw;
            cnst=IPOG.cnst;
            Amp=IPOG.Amp;
            sigmax=IPOG.sigmax;
            sigmay=IPOG.sigmay;
            thetaDegree=IPOG.thetaDegree;
            sliceI=IPOG.sliceI;
            sliceF=IPOG.sliceF;
            level=IPOG.level;
    }
    public IPOGaussianNode(double[] pdPars){//pars are Gaussian Pars, not transformed GaissianPars, callers should make sure about it
        this();
        this.pdPars=pdPars;
        cnst=pdPars[0];
        Amp=pdPars[1];
        sigmax=pdPars[2];
        sigmay=pdPars[3];
        thetaDegree=pdPars[4]*180/Math.PI;
        xc=pdPars[5];
        yc=pdPars[6];
    }
    public void getParsAsStrings(ArrayList<String> names, ArrayList<String> values){
        names.clear();
        values.clear();
        names.add("sID");
        values.add(PrintAssist.ToString(sliceIndex));
        names.add("Id");
        values.add(PrintAssist.ToString(IPOIndex));
        names.add("Xc");
        values.add(PrintAssist.ToString(xc,6,1));
        names.add("Yc");
        values.add(PrintAssist.ToString(yc,6,1));
        names.add("Level");
        values.add(PrintAssist.ToString(level,6,0));
        names.add("sliceI");
        values.add(PrintAssist.ToString(sliceI,6,0));
        names.add("sliceF");
        values.add(PrintAssist.ToString(sliceF,6,0));
        
        names.add("IPOG");
        if(IPOGaussianNodeHandler.isNormalShape(this))
            values.add("Normal");
        else
            values.add("Abnormal");
        names.add("ConfirmIPOG");
        if(nIPOGCode>=0)
            values.add("Confirmed");
        else
            values.add("Unconfirmed");
        
        names.add("Contour");
        values.add("Normal");
        names.add("ConfirmContour");
        if(nContourCode>=0)
            values.add("Confirmed");
        else
            values.add("Unconfirmed");
        
        names.add("RawContour");
        values.add("Normal");
        names.add("ConfirmRawShape");
        if(nRawContourCode>=0)
            values.add("Confirmed");
        else
            values.add("Unconfirmed");
        
        names.add("Amp");
        values.add(PrintAssist.ToString(Amp,6,1));
        names.add("MeritPValue");
        values.add(PrintAssist.ToStringScientific(dMeritPValue, 3));
        names.add("sigmax");
        values.add(PrintAssist.ToString(sigmax,6,3));
        names.add("sigmay");
        values.add(PrintAssist.ToString(sigmay,6,3));
        names.add("theta");
        values.add(PrintAssist.ToString(thetaDegree,6,1));
        names.add("cnst");       
        values.add(PrintAssist.ToString(cnst,1));
        IPOGContourParameterNode aNode=getContour(IPOGContourParameterNode.IPOG),bNode=getContour(IPOGContourParameterNode.GaussianMean),cNode=getContour(IPOGContourParameterNode.GaussianMeanRaw);
        if(aNode==null) aNode= new IPOGContourParameterNode();
        if(bNode==null) bNode= new IPOGContourParameterNode();
        if(cNode==null) cNode= new IPOGContourParameterNode();
        if(aNode!=null) aNode.getContourParsAsStrings(names,values);
        if(bNode!=null) bNode.getContourParsAsStrings(names,values);
        if(cNode!=null) cNode.getContourParsAsStrings(names,values);
        names.add("numNbrs");
        values.add(PrintAssist.ToString(cvIndexesInCluster.size(),0));
        names.add("Neighbors");
        values.add(cvIndexesInCluster.toString());
        names.add("TKId");
        values.add(PrintAssist.ToString(TrackIndex));
        names.add("BNDLId");
        values.add(PrintAssist.ToString(BundleIndex));
        names.add("rIndex");
        values.add(PrintAssist.ToString(rIndex));
        names.add("cIndex");
        values.add(PrintAssist.ToString(cIndex));
        names.add("Overlaps");
        String ov="";
        int i,len=cvOverlaps.size();
        IndexValuePairNode ip;
        for(i=0;i<len;i++){
            ip=cvOverlaps.get(i);
            ov+=ip.index+":"+PrintAssist.ToString(ip.value, 4)+"; ";
        }
        values.add(ov);
    }
    public int getXc(){
        return (int)(xc+0.5);
    }
    public int getYc(){
        return (int)(yc+0.5);
    }
    public Point getCenter(){
        return new Point(getXc(),getYc());
    }
    public double getDist(IPOGaussianNode ipog){
        double dx=xc-ipog.xc,dy=yc-ipog.yc;
        return Math.sqrt(dx*dx+dy*dy);
    }
    public int getZ(){
        return sliceIndex-1;
    }
    public boolean contains2(double x, double y){
        if(!cXRange2.contains(x)) return false;
        if(!cYRange2.contains(y)) return false;
        return true;
    }
    public double getDist2(double x, double y){
        double dx=x-xc,dy=y-yc;
        return dx*dx+dy*dy;
    }
    static public String getDefaultRefID(String sID){
        String ref="Background";
        if(sID.contentEquals("Amp")) ref="0";
        if(sID.contentEquals("SignalCal")) ref="0";
        if(sID.contentEquals("Signal")){
            return "0";
        }
        if(sID.contentEquals("Height")){
            return "Background";
        }
        if(sID.contentEquals("Peak1")){
            return "Background";
        }
        if(sID.contentEquals("Peak3")){
            return "Background";
        }
        if(sID.contentEquals("peak1 Raw")){
            return "Background";
        }
        if(sID.contentEquals("peak3 Raw")){
            return "Background";
        }
        if(sID.contentEquals("Background")) return "Background";
        if(sID.contentEquals("Signal")) ref="0";
        if(sID.contentEquals("Area")) ref="0";
        if(sID.contentEquals("BundleTotal")) ref="0";
        if(sID.contentEquals("Drift")) ref="0";
        return ref;
    }
    public double getValue(String sID){
        if(sID.contentEquals("Amp")) return Amp;
        if(sID.contentEquals("SignalCal")) return dTotalSignalCal;
        if(sID.contentEquals("Signal")){
            return dTotalSignal;
        }
        if(sID.contentEquals("Height")){
            return peak1-dBackground;
        }
        if(sID.contentEquals("Peak1")){
            return peak1-dBackground;
        }
        if(sID.contentEquals("Peak3")){
            return peak3-dBackground;
        }
        if(sID.contentEquals("Peak1 Raw")){
//            return peak1Raw-dBackground;
            return peak1Raw;//13220
        }
        if(sID.contentEquals("Peak3 Raw")){
//            return peak3Raw-dBackground;
            return peak3Raw;//13220
        }
        if(sID.contentEquals("Background")) return dBackground;
        if(sID.contentEquals("Signal")) return dTotalSignal;
        if(sID.contentEquals("Area")) return area;
        if(sID.contentEquals("BundleTotal")) return dBundleTotalSignal;
        if(sID.contentEquals("Drift")) return dDrift;
        return Double.NaN;
    }
    public double getReference(String refID){
        double ref=0;
        if(refID.contentEquals("0")) return ref=0;
        if(refID.contentEquals("1000")) return ref=1000;
        if(refID.contentEquals("background")) return ref=dBackground;
        return ref;
    }
    public double getValue(String sID, String refID){
        double value,ref=getReference(refID);
        if(sID.contentEquals("Amp")) return Amp;
        if(sID.contentEquals("SignalCal")) return dTotalSignalCal;
        if(sID.contentEquals("Signal")){
            return dTotalSignal;
        }
        if(sID.contentEquals("Height")){
            return peak1-dBackground;
        }
        if(sID.contentEquals("Peak1")){
            return peak1-ref;
        }
        if(sID.contentEquals("Peak3")){
            return peak3-ref;
        }
         if(sID.contentEquals("Peak1 Raw")){
//            return peak1Raw-ref;
            return peak1Raw;//13220
        }
        if(sID.contentEquals("Peak3 Raw")){
//            return peak3Raw-ref;
            return peak3Raw;//13220
        }
        if(sID.contentEquals("Background")) return dBackground;
        if(sID.contentEquals("Signal")) return dTotalSignal;
        if(sID.contentEquals("Area")) return area;
        if(sID.contentEquals("BundleTotal")) return dBundleTotalSignal;
        if(sID.contentEquals("Drift")) return dDrift;
        return Double.NaN;
    }
    public String getValueLable(String sID){
        if(sID.contentEquals("Amp")) return "Amp";
        if(sID.contentEquals("SignalCal")) return "Total Signal (cal)";
        if(sID.contentEquals("Height")) return "Peak1-1000";
        if(sID.contentEquals("Background")) return "Background";
        if(sID.contentEquals("Signal")) return "Total Signal";
        if(sID.contentEquals("BundleTotal")) return "Bundle Total Signal";
        if(sID.contentEquals("Peak1")) return "Peak1";
        if(sID.contentEquals("Peak3")) return "Peak3";
        return sID;
    }
    public double getAmpAt(double x,double y){
        double[]X={x,y};
        double dv=Gaussian2D_GaussianPars.fun(pdPars, X)-cnst;
        return dv;
    }
    public double[] getPeakPosition(){
        double[] peak={pdPars[5],pdPars[6]};
        return peak;
    }
    void setTrackIndex(int TrackIndex){
        this.TrackIndex=TrackIndex;
    }
    void setBundleIndex(int BundleIndex){
        this.BundleIndex=BundleIndex;
    }
    int getClusterSize(){
        if(cvOverlaps==null) return -1;
        return cvOverlaps.size();
    }
    int getNumIPOGs(){
        return 1;
    }
    public void getSimpleIPOGs(ArrayList<IPOGaussianNode> IPOGs){
        IPOGs.add(this);
    }
    public void setLevel(int nLevel){
        level=nLevel;
    }
    public void setOverlappingInfo(double preOvlp, int preRid, double postOvlp, int postRid){
        this.preOvlp=preOvlp;
        this.preRid=preRid;
        this.postOvlp=postOvlp;
        this.postRid=postRid;
    }
    public void setBundleTotalSignal(double signal){
        dBundleTotalSignal=signal;
    }
    public void setBackground(double bkg){
        dBackground=bkg;
    }
    public void setTotalSignal(double dSig){
        dTotalSignal=dSig;
    }
    public void setArea(int area){
        this.area=area;
    }
    public void setPeak1(double pixel){
        peak1=pixel;
    }
    public void setPeak3(double pixel){
        peak3=pixel;
    }
    public void getRanges3(intRange xRange, intRange yRange){
        xRange.setMin((int)(cXRange3.getMin()+0.5));
        xRange.setMax((int)(cXRange3.getMax()+0.5));
        yRange.setMin((int)(cYRange3.getMin()+0.5));
        yRange.setMax((int)(cYRange3.getMax()+0.5));
    }
    public int getMainPeakInfo(StringBuffer names, StringBuffer values){
        names.append(",MainAmp");
        values.append(","+PrintAssist.ToString(Amp,1));
        names.append(",SigmaX");
        values.append(","+PrintAssist.ToString(sigmax,1));
        names.append(",SigmaY");
        values.append(","+PrintAssist.ToString(sigmax,1));
        return 1;
    }
    public void setSliceRange(int sI, int sF){
        sliceI=sI;
        sliceF=sF;
    }
    public double func(double x, double y){
        return getAmpAt(x,y);
    }
    public DoublePair getCenter_Subpixel(){
        return new DoublePair(pdPars[5],pdPars[6]);
    }
    public double getPeakAmp(){
        return pdPars[1];
    }
    public ArrayList<IPOGContourParameterNode> getContourNodes(){
        return cvContourParNodes;
    }
    public int storeContourParNodes(ArrayList<IPOGContourParameterNode> cvNodes){
        for(int i=0;i<cvNodes.size();i++){
            storeContourParNode(cvNodes.get(i));
        }
        return 1;
    }
    public int storeContourParNode(IPOGContourParameterNode aNode){
        if(aNode==null) return -1;
        if(aNode.sliceI!=sliceI||aNode.sliceF!=sliceF) return -1;
        if(aNode.Contained(cvContourParNodes)) return -1;
        int p=aNode.findPosition(cvContourParNodes);
        if(p>=0) 
            cvContourParNodes.set(p, aNode);
        else
            cvContourParNodes.add(aNode);
        return 1;
    }
    public ArrayList<IPOGContourParameterNode> getContourParNodes(){
        return cvContourParNodes;
    }
    public Point getMagCenter(int mag){
        DoublePair ct=getCenter_Subpixel();
        return new Point((int)(ct.left*mag+0.5),(int)(ct.right*mag+0.5));
    }
    public DoublePair getSubpixelShift(int mag){
        Point magCt=getMagCenter(mag);
        DoublePair ctSub=getCenter_Subpixel();
        return new DoublePair(ctSub.left-magCt.x/((double)mag),ctSub.right-magCt.y/((double)mag));
    }
    public int buildContour_Percentage(){
        return buildContour_Percentage(this,ContourParameterNode.IPOG,false);
    }
    public int buildContour_Percentage(TwoDFunction fun, int functionType, boolean show){
        Contour=new ArrayList();
        SubpixelContour=new ArrayList();
        if(fun!=null) TwoDFunctionType=fun.getTowDFunctionType();
        TwoDFun=fun;        
        ShowContour=show;
        double r1=1./2.71828,r2=r1*r1,r3=1./4.;    
        double percentage=r3;
        
        ContourMag=5;
        DoubleRange dr;
        SubpixelGaussianMean sgMean;
        int[][] pixels;
        int pIndex;
        double pV=0.0001,dBorderCutoff=Double.POSITIVE_INFINITY;
        
        if(functionType==ContourParameterNode.GaussianMean){
            sgMean=(SubpixelGaussianMean)fun;
            dr=new DoubleRange(sgMean.getValueRange());
            if(!sgMean.isCentered()) 
                dr.setMax(sgMean.getCenterPeakValue());
            ContourCutoff=percentage*dr.getRange();        
            dr.setRange(ContourCutoff,dr.getRange());
            DoublePair Peak=fun.getPeak();
            boolean withinFrame=true;
            DoubleRange xRange=new DoubleRange(),yRange=new DoubleRange();
            fun.getFrameRanges(xRange, yRange);
            pixels=sgMean.getPixels();
            if(show) CommonMethods.displayAsImage("Original pixels of GaussianMean", pixels[0].length, pixels.length, pixels, ImagePlus.GRAY32);
            if(sliceI>0){
                pIndex=IPOAnalyzerForm.getHeightCutoffPIndex(pV);
                int index=Math.max(sliceI-15, 0);
                index=Math.min(index, IPOAnalyzerForm.ppdGroovePixelValueCutoff.length-1);
                dBorderCutoff=IPOAnalyzerForm.ppdGroovePixelValueCutoff[index][pIndex]-sgMean.getValueRange().getMin();
//                if(dBorderCutoff>dr.getMin()) dr.setMin(dBorderCutoff);
            }
/*            if(!xRange.contains(Peak.left)||!yRange.contains(Peak.right))
                withinFrame=false;
            else if(Math.min(xRange.getMax()-Peak.left, Peak.left-xRange.getMin())<4||Math.min(yRange.getMax()-Peak.right, Peak.right-yRange.getMin())<4)
               withinFrame=false; 
            if(!withinFrame)
                fun=this;*/
            
        } else if(functionType==IPOGContourParameterNode.GaussianMeanRaw){
            sgMean=(SubpixelGaussianMean)fun;
            dr=new DoubleRange(sgMean.getValueRange());
            if(!sgMean.isCentered()) 
                dr.setMax(sgMean.getCenterPeakValue());
            ContourCutoff=percentage*dr.getRange();        
            dr.setRange(ContourCutoff,dr.getRange());
            DoublePair Peak=fun.getPeak();
            
            boolean withinFrame=true;            
            DoubleRange xRange=new DoubleRange(),yRange=new DoubleRange();
            fun.getFrameRanges(xRange, yRange);
            pixels=sgMean.getPixels();
            if(show) CommonMethods.displayAsImage("Original pixels of GaussianMeanRaw", pixels[0].length, pixels.length, pixels, ImagePlus.GRAY32);
            
            if(sliceI>0){
                int index=Math.max(sliceI-15, 0);
                index=Math.min(index, IPOAnalyzerForm.ppdGroovePixelValueCutoff.length-1);
                pIndex=IPOAnalyzerForm.getHeightCutoffPIndex(pV);
                dBorderCutoff=IPOAnalyzerForm.ppdGroovePixelValueCutoff[index][pIndex]-sgMean.getValueRange().getMin();
//                if(dBorderCutoff>dr.getMin()) dr.setMin(dBorderCutoff);
            }
            
/*            
            if(!xRange.contains(Peak.left)||!yRange.contains(Peak.right))
                withinFrame=false;
            else if(Math.min(xRange.getMax()-Peak.left, Peak.left-xRange.getMin())<4||Math.min(yRange.getMax()-Peak.right, Peak.right-yRange.getMin())<4)
               withinFrame=false; 
            
            if(!withinFrame){
                Peak=getCenter_Subpixel();
                double PeakAmp=fun.func(Peak.left, Peak.right);
                if(PeakAmp<ContourCutoff) ContourCutoff=PeakAmp-0.1;
                dr.setMin(ContourCutoff);
            }*/
            
        } else {
            fun=this;
            double peak=getPeakAmp();
            dr=new DoubleRange(percentage*peak,peak);
        }        
        return buildContour(fun,ContourMag,dr,Contour,SubpixelContour,show,dBorderCutoff);
    }
    public int buildContour(TwoDFunction tdf, int mag, DoubleRange cValueRange, ArrayList<Point> Contour, ArrayList<DoublePair> SubpixelContour, boolean showContour, double BorderCutoff){
        double dx=1/(double)mag,dy=1/(double)mag;
        DoublePair subShift=getSubpixelShift(mag);
        double x0=subShift.left,y0=subShift.right;
        Point ctMag=new Point();
        int x,y;
        if(tdf.getTowDFunctionType()==IPOGContourParameterNode.IPOG){
            ctMag=getMagCenter(mag);
        }else{
            DoubleRange xRange=new DoubleRange(), yRange=new DoubleRange();        
            tdf.getFrameRanges(xRange, yRange);
            x=(int)(xRange.getMidpoint()+0.5);
            y=(int)(yRange.getMidpoint()+0.5);
            ctMag.setLocation(x*mag,y*mag);
        }
        double dt=tdf.func(subShift.left+ctMag.x*dx, subShift.right+ctMag.y*dy);
        if(Double.isNaN(ContourCutoff)){
            x0=x0;
        }
        if(dt<cValueRange.getMin()){
            ContourCutoff=dt-1.;
            cValueRange.setMin(ContourCutoff);
        }
        
        ContourPar=new ContourParameterNode(tdf, subShift, mag);
        boolean contourCompleted=false;
        if(tdf instanceof SubpixelGaussianMean){
//        if(false){
            double boundaryPeakValue=ContourPar.getRegionBondaryPeakValue(ctMag);        
            if(ContourCutoff<boundaryPeakValue&&boundaryPeakValue<BorderCutoff){
                if(boundaryPeakValue+3<((SubpixelGaussianMean)tdf).getCenterPeakValue()-((SubpixelGaussianMean)tdf).getValueRange().getMin()){
                    double cutoff0=ContourCutoff;
                    ContourCutoff=boundaryPeakValue+.1;
                    cValueRange.setMin(ContourCutoff);                    
                    ContourPar.buildContourParameterNode(ctMag, getCenter_Subpixel(), cValueRange, ContourParameterNode.LowerBound, showContour);
                    if(!ContourPar.IsRegular()) {
                        ContourCutoff=cutoff0;
                        cValueRange.setMin(ContourCutoff);
                    }else
                        contourCompleted=true;
                }
            }
        }
        
        if(!contourCompleted){
            ContourPar=new ContourParameterNode(tdf, subShift, mag);
            ContourPar.buildContourParameterNode(ctMag, getCenter_Subpixel(), cValueRange, ContourParameterNode.LowerBound, showContour);
        }
        if(!ContourPar.IsRegular()) return -1;
//        ContourPar=new ContourParameterNode(tdf, subShift, mag, ctMag, getCenter_Subpixel(), cValueRange,ContourParameterNode.LowerBound,showContour);
        int iter=0,maxIter=15;
/*        while(!ContourPar.ConfinedContour()){
            iter++;
            if(iter>maxIter) break;
//            MasterFittingGUI.closeAllTempWindows();
            double step=0.2*cValueRange.getRange();
            cValueRange.setMin(cValueRange.getMin()+step);
            ContourPar=new ContourParameterNode(tdf,subShift, mag, ctMag, getCenter_Subpixel(), cValueRange,ContourParameterNode.LowerBound,showContour);
        }*/
        
        ContourPar.getContours(Contour,SubpixelContour);
        ArrayList<Double> dv=new ArrayList();
        DoublePair p;
        
        for(int i=0;i<SubpixelContour.size();i++){
            p=SubpixelContour.get(i);          
            dt=tdf.func(p.left, p.right);
            dv.add(dt);
        }
        if(sliceI==59){
            sliceI=sliceI;
        }
        DoubleRange dr=CommonStatisticsMethods.getRange(dv);
        IPOGContourParameterNode aNode=ContourPar.getIPOGContourParameterNode();
        aNode.sliceI=sliceI;
        aNode.sliceF=sliceF;
        DoublePair sc=getCenter_Subpixel();
        aNode.PeakIPOG=sc;
        aNode.HeightIPOG=tdf.func(sc.left,sc.right);
        
        aNode.HeightPValue=IPOAnalyzerForm.getHeightPValue(Math.max(sliceI-20, 0), aNode.Height);
        storeContourParNode(aNode);     
        return 1;
    }
    public IPOGContourParameterNode getContour(int type){
        IPOGContourParameterNode aNode=null;
        for(int i=0;i<cvContourParNodes.size();i++){
            aNode=cvContourParNodes.get(i);
            if(type==aNode.type) return aNode;
        }
        return null;
    }
    public DoublePair getPeak(){
        return getCenter_Subpixel();
    }
    public DoubleRange getValueRange(){
        return new DoubleRange(cnst,cnst+getPeakAmp());
    }
    public void getFrameRanges(DoubleRange xFRange, DoubleRange yFRange){
        xFRange.setRange(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
        yFRange.setRange(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
    }
    
    public double getHeight(DoublePair dp){
        CircleImage circle=new CircleImage(3);
        Point ct=getCenter();
        circle.setCenter(new Point((int) dp.left,(int)dp.right));
//        circle.setFrameRanges(xFRange, yFRange);
        ArrayList<Point> points=new ArrayList();
        circle.getInnerPoints(points);
        
        int x=(int) dp.left,y=(int)dp.right;
        DoublePair SubpixelShift=new DoublePair(dp.left-x,dp.right-y);
        
        double  h=0;
        Point p;
        for(int i=0;i<points.size();i++){
            p=points.get(i);
            h+=func(p.x+SubpixelShift.left,p.y+SubpixelShift.right);
        }
        h/=points.size();
        return h;
    }
    public boolean isDummy(){
        return Dummy;
    }
    public void setAsDummy(){
        Dummy=true;
    }
    public int getTowDFunctionType(){
        return IPOGContourParameterNode.IPOG;
    }
    public double getHeight(){
        DoublePair dp=getCenter_Subpixel();
        return getAmpAt(dp.left,dp.right);
    }
    public boolean ManuallyEvaluatedIPOG(){
        return nIPOGCode>=0;
    }
    public boolean ManuallyEvaluatedContour(){
        return nContourCode>=0;
    }
    public boolean ManuallyEvaluatedRawContour(){
        return nRawContourCode>=0;
    }
    public void setIPOGCode(int shapeCode){
        nIPOGCode=shapeCode;
    }
    public void setContourCode(int shapeCode){
        nContourCode=shapeCode;
    }
    public void setRawContourCode(int shapeCode){
        nRawContourCode=shapeCode;
    }
    public int getIPOGCode(){
        return nIPOGCode;
    }
    public int getContourCode(){
        return nContourCode;
    }
    public int getRawContourCode(){
        return nRawContourCode;
    }
    public void setMeritPValue(double pV){
        dMeritPValue=pV;
    }
    public int getContourShapeCode(int type){
        if(type==IPOGContourParameterNode.GaussianMean) return nContourCode;
        if(type==IPOGContourParameterNode.GaussianMeanRaw) return nRawContourCode;
        return nIPOGCode;
    }
}
