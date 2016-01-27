/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FluoObjects;
import utilities.CustomDataTypes.DoublePair;
import utilities.CustomDataTypes.DoubleRange;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import utilities.io.IOAssist;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.io.PrintAssist;
import utilities.CustomDataTypes.IntPair;

/**
 *
 * @author Taihao
 */
public class IPOGContourParameterNode {
    public static final int IPOG=0, GaussianMean=1, GaussianMeanRaw=2;
    public int sliceI,sliceF;
    public int type,area; //IPOG or GaussianMean
    public int nMag;
    public int nCurvatureMaxima,nDistMaxima,nConcaveCurves,Peak;
    public DoublePair origin,PeakIPOG;
    public Point PeakPoint,Centroid;
    public DoubleRange cValueRange,cCurvatureRangeX,cCurvatureRangeN,cDistRangeX,cDistRangeN,cSigmaRange;
    public double CircularDistAsymmetry, CircularCurvatureAsymmetry,LinearityDevL,LinearityDevS,PeakAmp,MeanDist;
    public ArrayList<Double> dvDistancesMaxima, dvCurvaturesMaxima, dvDistancesMinima, dvCurvaturesMinima, dvSmoothedDist, dvSmoothedDentDepth;
    public int PhaseShift,nMainPeaks;
    public double Sigma,Signal,Height,HeightIPOG,HeightPValue;
    public double width,length,dMaxSideLength;
    public DoublePair CentralPeakShift;
    public ArrayList<Point> cMECPolygon;
    int version;
    public IPOGContourParameterNode(){
        cValueRange=new DoubleRange();
        cCurvatureRangeX=new DoubleRange();
        cCurvatureRangeN=new DoubleRange();
        cDistRangeX=new DoubleRange();
        cDistRangeN=new DoubleRange();
        origin=new DoublePair(0,0);
        dvDistancesMaxima=new ArrayList();
        dvCurvaturesMaxima=new ArrayList();
        dvDistancesMinima=new ArrayList();
        dvCurvaturesMinima=new ArrayList();
        CentralPeakShift=new DoublePair(0,0);
        cMECPolygon=new ArrayList();
        MeanDist=-1;
    }
    public void setSlices(int sliceI,int sliceF){
        this.sliceI=sliceI;
        this.sliceF=sliceF;
    }
    int exportNode (DataOutputStream ds)throws IOException {
        ds.writeInt(type);
        ds.writeInt(nMag);
        ds.writeInt(PhaseShift);
        ds.writeInt(sliceI);
        ds.writeInt(sliceF);
        ds.writeInt(nDistMaxima);
        ds.writeInt(nCurvatureMaxima);
        ds.writeInt(nConcaveCurves);
        ds.writeDouble(origin.left);
        ds.writeDouble(origin.right);
        ds.writeInt(PeakPoint.x);
        ds.writeInt(PeakPoint.y);
        ds.writeInt(Centroid.x);
        ds.writeInt(Centroid.y);
        ds.writeDouble(cValueRange.getMin());
        ds.writeDouble(cValueRange.getMax());
        ds.writeDouble(CircularDistAsymmetry);
        ds.writeDouble(CircularCurvatureAsymmetry);
        ds.writeDouble(LinearityDevS);
        ds.writeDouble(LinearityDevL);
        IOAssist.writeDoubleArrayList(ds,dvDistancesMaxima);
        IOAssist.writeDoubleArrayList(ds,dvCurvaturesMaxima);
        IOAssist.writeDoubleArrayList(ds,dvDistancesMinima);
        IOAssist.writeDoubleArrayList(ds,dvCurvaturesMinima);
        ds.writeDouble(MeanDist);
        ds.writeDouble(Signal);
        ds.writeInt(area);
        ds.writeDouble(Height);
        //version 8 and higher of IPOGTLevelInfoNode
        if(PeakIPOG==null) PeakIPOG=new DoublePair(0,0);
        ds.writeDouble(PeakIPOG.left);
        ds.writeDouble(PeakIPOG.right);
        ds.writeDouble(HeightIPOG);     
        //version 10 and higher of IPOGTLevelInfoNode
        ds.writeDouble(width);
        ds.writeDouble(length);
        ds.writeDouble(CentralPeakShift.left);
        ds.writeDouble(CentralPeakShift.right);
        //version 13 and higher
        IOAssist.writePointArrayList(ds, cMECPolygon);
        return 1;
    }
    int importNode (BufferedInputStream bf) throws IOException {
        type=IOAssist.readInt(bf);
        nMag=IOAssist.readInt(bf);
        PhaseShift=IOAssist.readInt(bf);
        sliceI=IOAssist.readInt(bf);
        sliceF=IOAssist.readInt(bf);       
        nDistMaxima=IOAssist.readInt(bf);
        nCurvatureMaxima=IOAssist.readInt(bf);
        nConcaveCurves=IOAssist.readInt(bf);
        origin=new DoublePair();
        origin.left=IOAssist.readDouble(bf);
        origin.right=IOAssist.readDouble(bf);
        
        int x=IOAssist.readInt(bf),y=IOAssist.readInt(bf);
        PeakPoint=new Point(x,y);
        x=IOAssist.readInt(bf);
        y=IOAssist.readInt(bf);
        Centroid=new Point(x,y);
        double dMin,dMax;
        dMin=IOAssist.readDouble(bf);
        dMax=IOAssist.readDouble(bf);
        cValueRange=new DoubleRange(dMin,dMax);
        CircularDistAsymmetry=IOAssist.readDouble(bf);;
        CircularCurvatureAsymmetry=IOAssist.readDouble(bf);;
        LinearityDevS=IOAssist.readDouble(bf);;
        LinearityDevL=IOAssist.readDouble(bf);;
        IOAssist.readDoubleArrayList(bf,dvDistancesMaxima);
        IOAssist.readDoubleArrayList(bf,dvCurvaturesMaxima);
        IOAssist.readDoubleArrayList(bf,dvDistancesMinima);
        IOAssist.readDoubleArrayList(bf,dvCurvaturesMinima);
        MeanDist=IOAssist.readDouble(bf);
        Signal=IOAssist.readDouble(bf);
        area=IOAssist.readInt(bf);
        Height=IOAssist.readDouble(bf);
        if(version>7){
            PeakIPOG=new DoublePair();
            PeakIPOG.left=IOAssist.readDouble(bf);
            PeakIPOG.right=IOAssist.readDouble(bf);
            HeightIPOG=IOAssist.readDouble(bf);
        }
        if(version>9){
            width=IOAssist.readDouble(bf);
            length=IOAssist.readDouble(bf);
        }
        if(version>10){
            CentralPeakShift.left=IOAssist.readDouble(bf);
            CentralPeakShift.right=IOAssist.readDouble(bf);
        }
        if(version>12){
            IOAssist.readPointArrayList(bf, cMECPolygon);
        }
        calDistRanges();
        return 1;
    }
    public boolean Contained(ArrayList<IPOGContourParameterNode> parNodes){
        for(int i=0;i<parNodes.size();i++){
            if(parNodes.get(i).Equivalent(this)) return true;
        }
        return false;
    }
    public int findPosition(ArrayList<IPOGContourParameterNode> parNodes){
        for(int i=0;i<parNodes.size();i++){
            if(parNodes.get(i).Equivalent(this)) return i;
        }
        return -1;
    }
    public boolean Equivalent(IPOGContourParameterNode aNode){
        if(aNode.type!=type) return false;
        if(aNode.sliceI!=sliceI) return false;
        if(aNode.sliceF!=sliceF) return false;
        double precision=0.001;
        double dMin=cValueRange.getMin(),dMina=aNode.cValueRange.getMin(),dMax=cValueRange.getMax(),dMaxa=aNode.cValueRange.getMax();
        if(dMin!=dMina){
            if(Math.abs((dMin-dMina)/(dMin+dMina))>precision) return false;
        }
        if(dMax!=dMaxa){
            if(Math.abs((dMax-dMaxa)/(dMax+dMaxa))>precision) return false;
        }
        return true;
    }
    public String[][] getContourParsAsStrings(){
        ArrayList<String>names=new ArrayList(),values=new ArrayList();
        getContourParsAsStrings(names,values);
        int i,len=names.size();
        String[][] psData=new String[2][len];
        for(i=0;i<len;i++){
            psData[0][i]=names.get(i);
            psData[1][i]=values.get(i);
        }
        return psData;
    }
    public int getContourParsAsStrings(ArrayList<String> names, ArrayList<String> values){
        calMainPeaks();
        String st;
        double AsymmetryDL=(cDistRangeX.getMax()-cDistRangeX.getMin())/(cDistRangeX.getMax()+cDistRangeX.getMin());
        double AsymmetryDS=(cDistRangeN.getMax()-cDistRangeN.getMin())/(cDistRangeN.getMax()+cDistRangeN.getMin());
        double distL=0.5*(cDistRangeX.getMin()+cDistRangeX.getMax()),distS=0.5*(cDistRangeN.getMin()+cDistRangeN.getMax());
//        names.add("nXc+type");
//        values.add(PrintAssist.ToString(nCurvatureMaxima));
        names.add("nXd"+type);
        values.add(PrintAssist.ToString(nDistMaxima));
        names.add("nPeaksD"+type);
        values.add(PrintAssist.ToString(nMainPeaks));
        names.add("nNCV"+type);
        values.add(PrintAssist.ToString(nConcaveCurves));
        names.add("PShift"+type);
        double x=CentralPeakShift.left,y=CentralPeakShift.right;
        values.add(PrintAssist.ToString(Math.sqrt(x*x+y*y),3));
//        if(nDistMaxima>2) return -1;       
        double ratio=PeakAmp/cValueRange.getMin();
        names.add("Rx"+type);
        values.add(PrintAssist.ToString(distL,3));
        names.add("Rn"+type);
        values.add(PrintAssist.ToString(distS,3));
        double sigma=0.5*(distL+distS)/Math.sqrt(2.*Math.log(ratio));
        names.add("Sigma"+type);
        values.add(PrintAssist.ToString(Sigma,3));
        names.add("Amp"+type);
        if(cValueRange.isRegularRange())
            st=PrintAssist.ToString(cValueRange.getRange(),1);
        else
            st=PrintAssist.ToString(0,1);
        values.add(st);
        names.add("Signal"+type);
        values.add(PrintAssist.ToString(Signal/area,1));
        names.add("Height"+type);
        values.add(PrintAssist.ToString(Height,1));
        names.add("HIPOG"+type);
        values.add(PrintAssist.ToString(HeightIPOG,1));
        names.add("RationC"+type);
        values.add(PrintAssist.ToString(cValueRange.getMax()/cValueRange.getMin(),3));
        names.add("RatioD"+type);
        values.add(PrintAssist.ToString(distL/distS,3));
        names.add("RatioXN"+type);
        values.add(PrintAssist.ToString(cDistRangeX.getMax()/cDistRangeN.getMin(),3));
        names.add("ACSd"+type);
        values.add(PrintAssist.ToString(CircularDistAsymmetry,4));
/*        names.add("ASD"+type);
        values.add(PrintAssist.ToString(Math.max(AsymmetryDL, AsymmetryDS),3));
        names.add("ACSd"+type);
        values.add(PrintAssist.ToString(CircularDistAsymmetry,4));
        names.add("L. Dev"+type);
        values.add(PrintAssist.ToString(cDistRangeX.getMax()/cDistRangeN.getMin(),3));*/
        
        names.add("Lethg"+type);
        values.add(PrintAssist.ToString(length,1));
        names.add("Width"+type);
        values.add(PrintAssist.ToString(width,1));
        names.add("RatioLW"+type);
        values.add(PrintAssist.ToString(Math.max(length/width,width/length),3));
        names.add("MaxSideLen"+type);
        values.add(PrintAssist.ToString(dMaxSideLength/(nMag*Sigma),3));
        names.add("H-PValue"+type);
        values.add(PrintAssist.ToStringScientific(HeightPValue,3));
        return 1;
    }
    public double calSigma(double peak){        
        double ratio=cValueRange.getMax()/cValueRange.getMin();
        if(!cValueRange.isRegularRange()) ratio=PeakAmp/cValueRange.getMin();
        if(MeanDist<0){
            double distL=0.5*(cDistRangeX.getMin()+cDistRangeX.getMax()),distS=0.5*(cDistRangeN.getMin()+cDistRangeN.getMax());
            MeanDist=0.5*(distL+distS);
        }
        Sigma=MeanDist/Math.sqrt(2.*Math.log(ratio));
        double sMin=cDistRangeN.getMin()/Math.sqrt(2.*Math.log(ratio)),sMax=cDistRangeX.getMax()/Math.sqrt(2.*Math.log(ratio));
        cSigmaRange=new DoubleRange(sMin,sMax);        
        return Sigma;
    }
    public void calDistRanges(){
        cDistRangeX=CommonStatisticsMethods.getRange(dvDistancesMaxima);        
        cDistRangeN=CommonStatisticsMethods.getRange(dvDistancesMinima);        
        cCurvatureRangeX=CommonStatisticsMethods.getRange(dvCurvaturesMaxima);        
        cCurvatureRangeN=CommonStatisticsMethods.getRange(dvCurvaturesMinima);
        calSigma(cValueRange.getMax());
        calMainPeaks();
        dMaxSideLength=0;
        if(cMECPolygon==null){
            cMECPolygon=new ArrayList();
        } else if(!cMECPolygon.isEmpty()){
            int i,len=cMECPolygon.size();
            Point p0=cMECPolygon.get(len-1),p;
            double dist;
            for(i=0;i<len;i++){
                p=cMECPolygon.get(i);
                dist=CommonMethods.getDistance(p0.x,p0.y,p.x,p.y);
                if(dist>dMaxSideLength) dMaxSideLength=dist;
                p0=p;
            }
        }
    }
    public void calMainPeaks(){
        ArrayList<Double> dvHeights=new ArrayList();
        double hMax=cDistRangeX.getMax()-cDistRangeN.getMin(),hMin;
        double height,cutoff=0.1;
        int i,len=dvDistancesMaxima.size(),iMin;
        nMainPeaks=len;
        IntPair ip=new IntPair();
        
        ArrayList<Double> dv=new ArrayList();
        for(i=0;i<len;i++){
            dv.add(dvDistancesMaxima.get(i));
            dv.add(dvDistancesMinima.get((i+PhaseShift)%len));
        }
        
        int left, right;
        len=dv.size();
        boolean[] pbValid=new boolean[len];
        CommonStatisticsMethods.setElements(pbValid, true);
        while(true){
            hMin=hMax;
            ip.setPair(-1, -1);
            for(i=0;i<len;i+=2){
                if(!pbValid[i]) continue;
                left=CommonStatisticsMethods.getFirstSelectedPosition_Circular(pbValid, CommonStatisticsMethods.getCircularIndex(i-1, len), -2);
                if(left<0) {
                    break;
                }
                height=dv.get(i)-dv.get(left);
                if(height<hMin) {
                    hMin=height;
                    ip.setPair(left, i);
                }
                
                right=CommonStatisticsMethods.getFirstSelectedPosition_Circular(pbValid, CommonStatisticsMethods.getCircularIndex(i+1, len), 2);
                if(right<0) {
                    break;
                }
                height=dv.get(i)-dv.get(right);
                if(height<hMin) {
                    hMin=height;
                    ip.setPair(i, right);
                }
            }
            if(ip.contains(-1)) break;
            if(hMin/MeanDist<cutoff){
                pbValid[ip.left]=false;
                pbValid[ip.right]=false;
                nMainPeaks--;
            }else{
                break;
            }
        }       
        if(dvSmoothedDentDepth!=null){
            len=dvSmoothedDentDepth.size();
            ArrayList<Integer> nv=CommonStatisticsMethods.getCrossingPositions(CommonStatisticsMethods.copyToDoubleArray(dvSmoothedDentDepth), 1);
            nConcaveCurves=(nv.size()+1)/2;
        }
    }
    public void setPeakAmp(double amp){
        PeakAmp=amp;
    }
    public DoubleRange getSigmaRange(){
        return cSigmaRange;
    }
    public boolean ReliableContour(){
        if(type==IPOGContourParameterNode.IPOG)return true;
        if(Math.abs(cValueRange.getMax()/cValueRange.getMin())<3) return false;
        double x=CentralPeakShift.left,y=CentralPeakShift.right;
        double shift=Math.sqrt(x*x+y*y);
        if(shift>2.5) return false;
        return true;
    }
    public double getCentralPeakShiftDist(){
        double x=CentralPeakShift.left,y=CentralPeakShift.right;
        return Math.sqrt(x*x+y*y);
    }
}
