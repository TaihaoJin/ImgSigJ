/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.io.FileAssist;
import utilities.io.PrintAssist;
import java.awt.Point;
import utilities.CustomDataTypes.intRange;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Geometry.ImageShapes.Ring;
import utilities.CustomDataTypes.DoublePair;
import java.awt.geom.Point2D;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;

/**
 *
 * @author Taihao
 */
public class IPOGaussianNodeComplex extends IPOGaussianNode{    
    ArrayList<IPOGaussianNode> IPOGs;
    int radius=10;
//    int TrackIndex,BundleIndex,rIndex,cIndex,sliceIndex;
    public IPOGaussianNodeComplex(){
        IPOGs=new ArrayList();
        TrackIndex=-1;
        BundleIndex=-1;
        rIndex=-1;
        cIndex=-1;
        sliceIndex=-1;
        dTotalSignalCal=0;
        Amp=0;
    }
    public IPOGaussianNodeComplex(double pdPars[]){
        this();
        int nPars=pdPars.length;
        int i,nTerms=nPars/6,j;
        double cnst=pdPars[0];
        int num=1;
        double[] pdt;
        for(i=0;i<nTerms;i++){
            pdt=new double[7];
            pdt[0]=cnst;
            for(j=1;j<7;j++){
                pdt[j]=pdPars[num];
                num++;
            }
            addIPOG(new IPOGaussianNode(pdt));
        }
    }

    public IPOGaussianNodeComplex(ArrayList<IPOGaussianNode> IPOGs){
        this();
        int i,len=IPOGs.size(),position,j;
        IPOGaussianNode IPOG;
        for(i=0;i<len;i++){
            addIPOG(IPOGs.get(i));
        }
        if(len>0){
            int num=6*len+1;
            pdPars=new double[num];
            IPOG=IPOGs.get(0);
            pdPars[0]=IPOG.cnst;
            position=1;
            for(i=0;i<len;i++){
                IPOG=IPOGs.get(i);            
                for(j=0;j<6;j++){
                    pdPars[position]=IPOG.pdPars[j+1];
                    position++;
                }
            }       
        }
    }
    public IPOGaussianNodeComplex(IPOGaussianNode IPOG){
        this();
        addIPOG(IPOG);        
        pdPars=CommonStatisticsMethods.copyArray(IPOG.pdPars);
    }
    public ArrayList<IPOGaussianNode> getIPOGs(){
        return IPOGs;
    }
    
    public void addIPOG(IPOGaussianNode IPOG0){
        IPOGaussianNode IPOG=new IPOGaussianNode();
        IPOG.copy(IPOG0);
//        IPOG=IPOG0;
        if(compatible(IPOG)) IPOGs.add(IPOG);
        if(IPOGs.size()==1){
            xcr=IPOG.xcr;
            ycr=IPOG.ycr;
            xc=xcr;
            yc=ycr;
            TrackIndex=IPOG.TrackIndex;
            BundleIndex=IPOG.BundleIndex;
            rIndex=IPOG.rIndex;
            cIndex=IPOG.cIndex;
            sliceIndex=IPOG.sliceIndex;
            dBundleTotalSignal=IPOG.dBundleTotalSignal;
            dTotalSignal=IPOG.dTotalSignal;
            dBackground=IPOG.dBackground;
            dTotalSignalCal=IPOG.dTotalSignalCal;
            area=IPOG.area;
            preOvlp=IPOG.preOvlp;
            preRid=IPOG.preRid;
            postOvlp=IPOG.postOvlp;
            postRid=IPOG.postRid;
            peak1=IPOG.peak1;
            peak3=IPOG.peak3;
            level=IPOG.level;
        }
        IPOG.cvContourParNodes=this.cvContourParNodes;
        IPOG.Contour=this.Contour;
        IPOG.ContourCutoff=this.ContourCutoff;
        IPOG.ContourMag=this.ContourMag;
        IPOG.TwoDFunctionType=this.TwoDFunctionType;
        IPOG.BundleIndex=this.BundleIndex;
        IPOG.ContourPar=this.ContourPar;
        IPOG.ShowContour=this.ShowContour;
        IPOG.SubpixelContour=this.SubpixelContour;
        IPOG.TwoDFun=this.TwoDFun;
        Amp+=IPOG.getAmpAt(xcr, ycr);
    }    
    public boolean compatible(IPOGaussianNode IPOG){
        if(IPOG.TrackIndex!=TrackIndex&&TrackIndex!=-1) return false;
        if(IPOG.rIndex!=rIndex&&rIndex!=-1) return false;
        if(IPOG.cIndex!=cIndex&&cIndex!=-1) return false;
        if(IPOG.BundleIndex!=BundleIndex&&BundleIndex!=-1) return false;
        if(IPOG.sliceIndex!=sliceIndex&&sliceIndex!=-1) return false;
        return true;
    }
    public double getValue(String sID){
        if(sID.contentEquals("Amp")) return getAmpAt(xc,yc);
        if(sID.contentEquals("SignalCal")){
            dTotalSignalCal=0;
            int i,len=IPOGs.size();
            for(i=0;i<len;i++){
                dTotalSignalCal+=IPOGs.get(i).dTotalSignalCal;
            }
            return dTotalSignalCal;
        }
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
            return peak1Raw;
        }
        if(sID.contentEquals("Peak3 Raw")){
//            return peak3Raw-dBackground;
            return peak3Raw;
        }
        if(sID.contentEquals("Background")){
            return dBackground;
        }
        if(sID.contentEquals("Area")){
            return area;
        }
        if(sID.contentEquals("Drift")){
            return dDrift;
        }
        return Double.NaN;
    }
    public double getAmpAt(double x,double y){
        double[]X={x,y};
        int i,len=IPOGs.size();
        double dv=0;
        for(i=0;i<len;i++){
            dv+=IPOGs.get(i).getAmpAt(x, y);
        }
        return dv;
    }
    public double[] getPeakPosition(){
        double[] peak={xcr,ycr};
        return peak;
    }
    void setTrackIndex(int TrackIndex){
        this.TrackIndex=TrackIndex;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setTrackIndex(TrackIndex);
        }
    }
    void setBundleIndex(int BundleIndex){
        this.BundleIndex=BundleIndex;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setBundleIndex(BundleIndex);
        }
    }
    int getClusterSize(){
        return IPOGs.size();
    }
    int getNumIPOGs(){
        return IPOGs.size();
    }
    public void getSimpleIPOGs(ArrayList<IPOGaussianNode> IPOGs){
        int i,len=this.IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.add(this.IPOGs.get(i));
        }
    }
    public void setBackground(double bkg){
        dBackground=bkg;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setBackground(bkg);
        }
    }
    public void setTotalSignal(double dSig){
        dTotalSignal=dSig;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setTotalSignal(dSig);
        }
    }
    public void setArea(int area){
        this.area=area;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setArea(area);
        }
    }
    public void getParsAsStrings(ArrayList<String> names, ArrayList<String> values){
        names.clear();
        String label;
        values.clear();
        names.add("slice");
        values.add(PrintAssist.ToString(sliceIndex));
        names.add("Id");
        values.add(PrintAssist.ToString(IPOIndex));
        names.add("Drift");
        values.add(PrintAssist.ToString(dDrift,6,1));
        names.add("IPOGs");
        label="[";
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            label+=IPOGs.get(i).IPOIndex;
            if(i<len-1)label+=",";
        }
        label+="]";
        values.add(label);
//        if(nvTrkIndexesInBundle!=null){
            names.add("Trks In Bundle");
            if(nvTrkIndexesInBundle==null)
                len=0;
            else
                len=nvTrkIndexesInBundle.size();
            
            label="[";
            for(i=0;i<len;i++){
                label+=nvTrkIndexesInBundle.get(i);
                if(i<len-1)label+=",";
            }
            label+="]";
            values.add(label);
//       }

        names.add("Xcr");
        values.add(PrintAssist.ToString(xcr,6,1));
        names.add("Ycr");
        values.add(PrintAssist.ToString(ycr,6,1));
        names.add("Amp");
        values.add(PrintAssist.ToString(Amp,6,1));
        names.add("Peak1");
        values.add(PrintAssist.ToString(peak1,6,1));
        names.add("Signal");
        values.add(PrintAssist.ToString(dTotalSignal,8,1));
        names.add("pOvlp");
        values.add(PrintAssist.ToString(preOvlp,8,1));
        names.add("Area");
        values.add(PrintAssist.ToString(area,8,1));
        names.add("nOvlp");
        values.add(PrintAssist.ToString(postOvlp,8,1));
        names.add("SignalCal");
        values.add(PrintAssist.ToString(dTotalSignalCal,6,1));
        names.add("Bkground");
        values.add(PrintAssist.ToString(dBackground,6,1));
        names.add("cnst");
        values.add(PrintAssist.ToString(cnst,1));
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
        names.add("pRid");
        values.add(PrintAssist.ToString(preRid));
        names.add("nRid");
        values.add(PrintAssist.ToString(postRid));
    }
    public void setBundleTotalSignal(double signal){
        dBundleTotalSignal=signal;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setBundleTotalSignal(signal);
        }
    }
    public void setPeak1(double pixel){
        peak1=pixel;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setPeak1(pixel);
        }
    }
    public void setPeak3(double pixel){
        peak3=pixel;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setPeak3(pixel);
        }
    }
    public Point getCenter(){
        intRange xRange=new intRange(), yRange=new intRange();
        int i,len=IPOGs.size(),x,y;
        Point pt,ct=new Point();
        double dt,dx=Double.NEGATIVE_INFINITY;
        for(i=0;i<len;i++){
            pt=IPOGs.get(i).getCenter();
            xRange.expandRange(pt.x);
            yRange.expandRange(pt.y);            
        }
        for(x=xRange.getMin();x<=xRange.getMax();x++){
            for(y=yRange.getMin();y<=yRange.getMax();y++){
                dt=getAmpAt(x,y);
                if(dt>dx){
                    dx=dt;
                    ct.setLocation(x,y);
                }
            }
        }
        xcr=ct.x;
        ycr=ct.y;
        return ct;
    }
    public void getRanges3(intRange xRange, intRange yRange){
        int i,len=IPOGs.size();
        IPOGs.get(0).getRanges3(xRange, yRange);
        intRange xRanget=new intRange(),yRanget=new intRange();
        for(i=1;i<len;i++){
            IPOGs.get(0).getRanges3(xRanget, yRanget);
            xRange.expandRanges(xRanget.getMin(), xRanget.getMax());
            yRange.expandRanges(yRanget.getMin(), yRanget.getMax());
        }
    }
    public void setLevel(int nLevel){
        this.level=nLevel;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).level=nLevel;
        }
    }
    public void setSliceRange(int sI, int sF){
        sliceI=sI;
        sliceF=sF;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOGs.get(i).setSliceRange(sI, sF);
        }
    }
    public int getMainPeakInfo(StringBuffer names, StringBuffer values){
        int i,j,len=IPOGs.size();
        double ampX=Double.NEGATIVE_INFINITY,ampX2=Double.NEGATIVE_INFINITY,amp;
        IPOGaussianNode IPOGt,IPOGx=null;
        Point ct=getCenter();
        intRange xRange=new intRange(ct.x-radius,ct.x+radius);
        intRange yRange=new intRange(ct.y-radius,ct.y+radius);
        double weight,ratio;
        
        double AmpSum=0;
        for(i=0;i<len;i++){
            IPOGt=IPOGs.get(i);
            ct=IPOGt.getCenter();
            if(!xRange.contains(ct.x)||!yRange.contains(ct.y));            
            if(IPOGt.sigmax<0.5||IPOGt.sigmay<0.5) continue;
            amp=IPOGt.Amp;
            AmpSum+=amp;
            if(amp>ampX){
                ampX=amp;
                IPOGx=IPOGt;
            }        
        }
        weight=ampX/AmpSum;
        
        if(IPOGx!=null){
            names.append(",MainAmp");
            if(level>=0) names.append(""+level);
            values.append(","+PrintAssist.ToString(IPOGx.Amp,1));
            names.append(",Weight");
            values.append(","+PrintAssist.ToString(weight, 3));
            names.append(",SigmaX");
            values.append(","+PrintAssist.ToString(IPOGx.sigmax,1));
            names.append(",SigmaY");
            values.append(","+PrintAssist.ToString(IPOGx.sigmay,1));
            ratio=IPOGx.sigmax/IPOGx.sigmay;
            if(ratio<1) ratio=1./ratio;
    //        if(IPOGx2==null) return -1;
            names.append(",Ratio");
            values.append(","+PrintAssist.ToString(ratio, preRid));
        }else{
            names.append(",MainAmp");
            if(level>=0) names.append(""+level);
            values.append(", ");
            names.append(",Weight");
            values.append(","+PrintAssist.ToString(weight, 3));
            names.append(",SigmaX");
            values.append(", ");
            names.append(",SigmaY");
            values.append(", ");
            names.append(",Ratio");
            values.append(", ");
        }
        return 1;
    }
    public int sortIPOGs(intRange xRange, intRange yRange){
        int i,len=IPOGs.size(),ix=0;
        IPOGaussianNode IPOG;
        Point ct;
        double Ampx=Double.NEGATIVE_INFINITY,Amp;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            ct=IPOG.getCenter();
            if(!xRange.contains(ct.x)||!yRange.contains(ct.y)) continue;
            Amp=IPOG.Amp;
            if(Amp>Ampx){
                Ampx=Amp;
                ix=i;
            }
        }
        if(ix>0){
            IPOG=IPOGs.get(ix);
            IPOGs.set(ix, IPOGs.get(0));
            IPOGs.set(0, IPOG);
        }
        return 1;
    }
    IPOGaussianNode getMainIPOG(){
        Point ct=getCenter();
        intRange xRange=new intRange(ct.x-radius,ct.x+radius);
        intRange yRange=new intRange(ct.y-radius,ct.y+radius);
        if(IPOGs.isEmpty()) return null;
        IPOGaussianNode IPOG=IPOGs.get(0),IPOGt;
        double Ampx=Double.NEGATIVE_INFINITY,Amp;
        if(IPOGs.size()==1) return IPOGs.get(0);
        int index=0;
        for(int i=0;i<IPOGs.size();i++){
            IPOGt=IPOGs.get(i);
            Amp=IPOGt.Amp;
            ct=IPOGt.getCenter();
            if(!xRange.contains(ct.x)) continue;
            if(!yRange.contains(ct.y)) continue;
            if(IPOGt.sigmax<1||IPOGt.sigmay<1) continue;
            if(Amp>Ampx){
                Ampx=Amp;
                IPOG=IPOGt;
                index=i;
            }
        }
        
        IPOGt=IPOGs.get(0);
        IPOGs.set(0, IPOG);
        IPOGs.set(index, IPOGt);
        
        index=1;
        IPOG=IPOGs.get(1);
        Ampx=Double.NEGATIVE_INFINITY;
        for(int i=1;i<IPOGs.size();i++){
            IPOGt=IPOGs.get(i);
            Amp=IPOGt.Amp;
            ct=IPOGt.getCenter();
            if(!xRange.contains(ct.x)) continue;
            if(!yRange.contains(ct.y)) continue;
            if(IPOGt.sigmax<1||IPOGt.sigmay<1) continue;
            if(Amp>Ampx){
                Ampx=Amp;
                IPOG=IPOGt;
                index=i;
            }
        }
        IPOGt=IPOGs.get(1);
        IPOGs.set(1, IPOG);
        IPOGs.set(index, IPOGt);
        return IPOGs.get(0);
    }
    public double getPeak3Cal(){
        int r=3,R=5,i,len;
        CircleImage circle=new CircleImage(r);
        Ring ring=new Ring(R-1,R);
        Point ct=getCenter(),pt;
        circle.setCenter(ct);
        ring.setCenter(ct);
        
        double sig=0,bkg=0,numc=0,numr=0;
        
        ArrayList<Point> points=new ArrayList();
        circle.getInnerPoints(points);
        len=points.size();
        for(i=0;i<len;i++){
            pt=points.get(i);
            sig+=getAmpAt(pt.x,pt.y);
        }
        sig/=len;
        
        points.clear();
        ring.getInnerPoints(points);
        len=points.size();
        for(i=0;i<len;i++){
            pt=points.get(i);
            bkg+=getAmpAt(pt.x,pt.y);
        }
        bkg/=len;
        
        return sig-bkg;
    }
    public double func(double x, double y){
        return getAmpAt(x,y);
    }
    public DoublePair getCenter_Subpixel(){
        Point ct=getCenter();
        DoublePair ct0=getMainIPOG().getCenter_Subpixel(),ct1=CommonMethods.subpixelWeightedCentroid(this, ct.x,ct.y);
        if(getAmpAt(ct0.left,ct0.right)>getAmpAt(ct1.left,ct1.right))
            return ct0;
        else
            return ct1;
    }
    public double getPeakAmp(){
        DoublePair ct=getCenter_Subpixel();
        return getAmpAt(ct.left,ct.right);
    }
    public void getParsAsStringArrayList(ArrayList<String[]> svValues){
        svValues.clear();
        if(sliceF>1){
            if(cvContourParNodes.isEmpty()) 
                buildContour_Percentage();
            IPOGContourParameterNode aNode=cvContourParNodes.get(0);
            aNode.setPeakAmp(getPeakAmp());
        }
//        boolean normalIPOG=IPOGaussianNodeHandler.isNormalIPOGShape(this),confirmed=nIPOGCode>=0,
//                normalContour=IPOGaussianNodeHandler.isNormalContourShape(this,IPOGContourParameterNode.GaussianMean),confirmedContour=nContourCode>=0,
//                normalRawContour=IPOGaussianNodeHandler.isNormalContourShape(this,IPOGContourParameterNode.GaussianMeanRaw),confirmedRawContour=nRawContourCode>=0;
        boolean normalIPOG=IPOGaussianNodeHandler.isNormalIPOGShape_RF(this),confirmed=nIPOGCode>=0,
                normalContour=IPOGaussianNodeHandler.isNormalContour_RF(this,IPOGContourParameterNode.GaussianMean),confirmedContour=nContourCode>=0,
                normalRawContour=IPOGaussianNodeHandler.isNormalContour_RF(this,IPOGContourParameterNode.GaussianMeanRaw),confirmedRawContour=nRawContourCode>=0;
        ArrayList<String> names=new ArrayList(),values=new ArrayList();
        IPOGs.get(0).getParsAsStrings(names, values);
        int i,len=IPOGs.size(),nPars=names.size(),j,
                index1=CommonStatisticsMethods.findStringPosition(names, "IPOG"),index2=CommonStatisticsMethods.findStringPosition(names, "ConfirmIPOG"),
                index3=CommonStatisticsMethods.findStringPosition(names, "Contour"),index4=CommonStatisticsMethods.findStringPosition(names, "ConfirmContour"),
                index5=CommonStatisticsMethods.findStringPosition(names, "RawContour"),index6=CommonStatisticsMethods.findStringPosition(names, "ConfirmRawShape");
        String[] psValues=new String[nPars+1],psNames=new String[nPars+1];
        psNames[0]="row";
        psValues[0]="0";
        for(i=0;i<nPars;i++){
            psNames[i+1]=names.get(i);
            psValues[i+1]=values.get(i);
        }
        
        if(normalIPOG)
            psValues[index1+1]="Normal";
        else
            psValues[index1+1]="Abnormal";
        if(confirmed)
            psValues[index2+1]="Confirmed";
        else
            psValues[index2+1]="Unconfirmed";
        
        if(normalContour)
            psValues[index3+1]="Normal";
        else
            psValues[index3+1]="Abnormal";
        if(confirmedContour)
            psValues[index4+1]="Confirmed";
        else
            psValues[index4+1]="Unconfirmed";
        
        if(normalRawContour)
            psValues[index5+1]="Normal";
        else
            psValues[index5+1]="Abnormal";
        if(confirmedRawContour)
            psValues[index6+1]="Confirmed";
        else
            psValues[index6+1]="Unconfirmed";
        
        svValues.add(psNames);
        svValues.add(psValues);
        for(i=1;i<len;i++){
            IPOGs.get(i).getParsAsStrings(names, values);
            psValues=new String[nPars+1];
            psValues[0]=""+i;
            for(j=0;j<nPars;j++){
                psValues[j+1]=values.get(j);
            }
            svValues.add(psValues);
        
            if(normalIPOG)
                psValues[index1+1]="Normal";
            else
                psValues[index1+1]="Abnormal";
            if(confirmed)
                psValues[index2+1]="Confirmed";
            else
                psValues[index2+1]="Unconfirmed";

            if(normalContour)
                psValues[index3+1]="Normal";
            else
                psValues[index3+1]="Abnormal";
            if(confirmedContour)
                psValues[index4+1]="Confirmed";
            else
                psValues[index4+1]="Unconfirmed";

            if(normalRawContour)
                psValues[index5+1]="Normal";
            else
                psValues[index5+1]="Abnormal";
            if(confirmedRawContour)
                psValues[index6+1]="Confirmed";
            else
                psValues[index6+1]="Unconfirmed";
        
        }
    }
    public void setMeritPValue(double pV){
        dMeritPValue=pV;
        for(int i=0;i<IPOGs.size();i++){
            IPOGs.get(i).setMeritPValue(pV);
        }
    }
}
