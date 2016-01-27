/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import utilities.io.ByteConverter;
import ij.gui.PlotWindow;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.Histogram;
import java.awt.Point;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.Non_LinearFitting.*;
import utilities.Geometry.ImageShapes.*;
import utilities.CustomDataTypes.intRange;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import utilities.io.PrintAssist;
import FluoObjects.IPOGExtention;
import ImageAnalysis.SubpixelGaussianMean;
import ij.ImagePlus;
import utilities.io.IOAssist;
import ImageAnalysis.TwoDFunction;
import utilities.CustomDataTypes.DoublePair;
/**
 *
 * @author Taihao
 */
public class IPOGTrackNode {
    public IPOGTLevelInfoNode m_cLevelInfo;
    public ArrayList<IPOGTLevelInfoNode> m_cvLevelInfoNodes;
    public ArrayList<IPOGaussianNode> m_cvIPOGs;
    public int TrackIndex,BundleIndex,firstSlice,lastSlice,lastSliceE,BundleSize;
    DoubleRange cHeightRange,cTotalSignalRange,cHeightWidthRatioRange;
    Histogram cHeightHist,cTotalSignalHist,cAreaHist,cHWRatioHist,cClusterSizeHist,cXHist,cYHist,cDriftHist;
    intRange cClusterSizeRange,cAreaRange;
    public boolean complexTrack,edgeTrack;//a track is defined as a edge track if any IPOG in the track
                                          //whose cXRange3 or cYRange3 contains image edge points.
                                          //An Image edge point is any point on any of the four lines x=0,y=0,x=w-1 and y=h-1;
    boolean bVarRangesUpdated;
    boolean bTrackExtended;
    int[][] pixelsCompensated,pixelsScratch;
    public DoubleRange cXRange2,cYRange2, cXRange3,cYRange3;//two and three sigma ranges
    public DoubleRange cXCRange,cYCRange,cDriftRange;
    IPOGaussianNodeComplex m_cHeadIPOG;
    public double meanDrift,meanHWRatio;
    int w,h,RWA_Ws;//RWA_Ws is the window size for the running window average of IPO shapes
    ImageShape cISAveIPOs;
    ArrayList<Point> m_cvHeadComboMaxima;
    int m_nComplexity;
    int m_nRisingInterval;
    int[] pnRisingIntervals;
    ArrayList<IPOGExtention> cvIPOGExts;


    public IPOGTrackNode(){
        TrackIndex=-1;
        BundleIndex=-1;
        m_cvIPOGs=new ArrayList();
        firstSlice=Integer.MAX_VALUE;
        lastSlice=Integer.MIN_VALUE;
        bVarRangesUpdated=false;
        w=-1;
        h=-1;
        RWA_Ws=-1;
        m_cLevelInfo=null;
        m_nRisingInterval=-1;
        cvIPOGExts=new ArrayList();
        m_cvLevelInfoNodes=new ArrayList();
        bTrackExtended=false;
    }

    public int getTrackLevelInfoAsString(ArrayList<String> names, ArrayList<Double> values, ArrayList<String> svValues){
        return 1;
    }
    public boolean normalTrackHeadShape(){
        if(m_cHeadIPOG!=null) 
//            return IPOGaussianNodeHandler.isNormalShape((IPOGaussianNodeComplex)m_cHeadIPOG);
        if(lastSlice-firstSlice<10) return false;
        int num=0;
        IPOGaussianNode IPOGt;
        IPOGaussianNodeComplex IPOG;
        for(int slice=firstSlice;slice<=firstSlice+9;slice++){
            IPOGt=getIPOG(slice);
            if(IPOGt==null) return false;
            IPOG=(IPOGaussianNodeComplex) IPOGt;
            if(!IPOGaussianNodeHandler.isNormalShape_Rough(IPOG)) continue;
            num++;
        }
        if(num<6) return false;
        if(m_cHeadIPOG!=null)
            return IPOGaussianNodeHandler.isNormalShape((IPOGaussianNodeComplex) m_cHeadIPOG);//m_cHeadIPOG is suposed to be sorted
        else
            return true;
    }
    public void getTrackInfo(ArrayList<String> names, ArrayList<Double> values, ArrayList<String> svValues){
        names.add("SliceI");
        values.add((double)(firstSlice));
        svValues.add(PrintAssist.ToString(firstSlice,0));
        names.add("TrkLen");
        values.add((double)(lastSlice-firstSlice+1));
        svValues.add(PrintAssist.ToString(lastSlice-firstSlice+1,0));
        names.add("BndlSize");
        values.add((double)BundleSize);
        svValues.add(PrintAssist.ToString(BundleSize,0));
        names.add("maxClst");
        values.add((double)cClusterSizeRange.getMax());
        svValues.add(PrintAssist.ToString(cClusterSizeRange.getMax(),0));
        names.add("meanDrift");
        values.add((double)meanDrift);
        svValues.add(PrintAssist.ToString(meanDrift,1));
        names.add("maxDrift");
        values.add((double)cDriftRange.getMax());
        svValues.add(PrintAssist.ToString(cDriftRange.getMax(),1));
        names.add("maxSignal");
        values.add(cTotalSignalRange.getMax());
        svValues.add(PrintAssist.ToString(cTotalSignalRange.getMax(),1));
        names.add("minX");
        values.add((double)cXCRange.getMin());
        svValues.add(PrintAssist.ToString(cXCRange.getMin(),1));
        names.add("maxX");
        values.add((double)cXCRange.getMax());
        svValues.add(PrintAssist.ToString(cXCRange.getMax(),1));
        names.add("minY");
        values.add((double)cYCRange.getMin());
        svValues.add(PrintAssist.ToString((double)cYCRange.getMin(),1));
        names.add("maxY");
        values.add((double)cYCRange.getMax());
        svValues.add(PrintAssist.ToString((double)cYCRange.getMax(),1));
        names.add("TrkId");
        values.add((double)TrackIndex);
        svValues.add(PrintAssist.ToString((double)TrackIndex,0));
        names.add("bndlId");
        values.add((double)BundleIndex);
        svValues.add(PrintAssist.ToString((double)BundleIndex,0));
        names.add("minA");
        values.add((double)cAreaRange.getMin());
        svValues.add(PrintAssist.ToString(cAreaRange.getMin(),0));
        names.add("maxA");
        values.add((double)cAreaRange.getMax());
        svValues.add(PrintAssist.ToString(cAreaRange.getMax(),0));
        names.add("meanRHW");
        values.add((double)meanHWRatio);
        svValues.add(PrintAssist.ToString(meanHWRatio,1));
        names.add("maxRWH");
        values.add(cHeightWidthRatioRange.getMax());
        svValues.add(PrintAssist.ToString(cHeightWidthRatioRange.getMax(),1));
    }
    
    public int getTrackIndex(){
        return TrackIndex;
    }

    public IPOGTrackNode(IPObjectTrack aTrack){
        this();
        int i,len=aTrack.IPOs.size();
        ArrayList<IntensityPeakObject> IPOs=aTrack.IPOs;
        TrackIndex=aTrack.m_nTrackIndex;
        BundleIndex=aTrack.m_nBundleIndex;
        IntensityPeakObject IPO;
        IPOGaussianNode IPOGNode;
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            IPOGNode=IPO.getIPOG();
            IPOGNode.setTrackIndex(TrackIndex);
            IPOGNode.setBundleIndex(BundleIndex);
            IPOGNode.setBundleTotalSignal(IPO.dTotalBundleSignal);
            m_cvIPOGs.add(IPOGNode);
        }
//        calParRanges();
    }
    public IPOGTrackNode(IPOGTrackNode aTrack, int sI, int sF){
        this();
        sI=Math.max(aTrack.firstSlice, sI);
        sF=Math.min(aTrack.lastSlice, sF);

        firstSlice=sI;
        lastSlice=sF;
        int i,len=sF-sI+1;
        ArrayList<IPOGaussianNode> IPOGs=aTrack.m_cvIPOGs;
        TrackIndex=aTrack.TrackIndex;
        BundleIndex=aTrack.BundleIndex;
        IPOGaussianNode IPOG;
        IPOGaussianNode IPOGNode;
        for(i=sI;i<=sF;i++){
            IPOGNode=aTrack.getIPOG(i);
            m_cvIPOGs.add(IPOGNode);
        }
//        calParRanges();
    }
    public int exportIPOGTrack(DataOutputStream ds) throws IOException{
        ArrayList<IPOGaussianNode> IPOGs=getNonNullIPOGs();
        IPOGs=IPOGaussianNodeHandler.getSimpleIPOGs(IPOGs);
        int len=IPOGs.size();
        ds.writeInt(TrackIndex);
        ds.writeInt(BundleIndex);
        ds.writeInt(len);
        IPOGaussianNodeHandler.exportIPOs(ds, IPOGs,IPOGaussianNode.nNumAdditionalPars);
        return 1;
    }
    public ArrayList<IPOGaussianNode> getNonNullIPOGs(){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        int i,len=m_cvIPOGs.size();
        IPOGaussianNode IPOG;
        for(i=0;i<len;i++){
            IPOG=m_cvIPOGs.get(i);
            if(IPOG!=null) IPOGs.add(IPOG);
        }
        return IPOGs;
    }
    void fillGapsWithNulls(){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        int i,len=m_cvIPOGs.size(),slice0,slice,j;
        IPOGaussianNode IPOG=m_cvIPOGs.get(len-1);
        slice=IPOG.sliceIndex;
        for(i=len-2;i>=0;i--){
            IPOG=m_cvIPOGs.get(i);
            if(IPOG==null){
                slice--;
                continue;
            }
            slice0=IPOG.sliceIndex;
            for(j=slice0+1;j<=slice-1;j++){
                m_cvIPOGs.add(i+1,null);
            }
            slice=slice0;
        }
    }
    public int importIPOGTrack(BufferedInputStream bf, int nNumAddParsPerIPO){
        byte[] bt=new byte[4];
        try{bf.read(bt);}
        catch (IOException e){
            return -1;
        }
        TrackIndex=ByteConverter.toInt(bt);
        try{bf.read(bt);}
        catch (IOException e){
            return -1;
        }
        BundleIndex=ByteConverter.toInt(bt);
        try{bf.read(bt);}
        catch (IOException e){
            return -1;
        }
        int len=ByteConverter.toInt(bt);
        m_cvIPOGs.clear();
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        try{IPOGaussianNodeHandler.importIPOs(bf, IPOGs, len,nNumAddParsPerIPO);}
        catch(IOException e){
            return -1;
        }
        IPOGaussianNodeHandler.buildIPOGVarRanges(IPOGs);
        int status=buildTrack(IPOGs);
        if(status<0) return -1;
        calParRanges();
        return 1;
    }

    int buildTrack(ArrayList<IPOGaussianNode> IPOGs0){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        int i,len=IPOGs0.size();
        intRange sliceRange=new intRange();
        IPOGaussianNode IPOG;
        IPOGaussianNodeComplex IPOGc;
        for(i=0;i<len;i++){
            IPOG=IPOGs0.get(i);
            sliceRange.expandRange(IPOG.sliceIndex);
        }
        int sliceI=sliceRange.getMin(),sliceF=sliceRange.getMax(),slice;
        firstSlice=sliceI;
        lastSlice=sliceF;
        int numSlices=sliceF-sliceI+1;
        if(numSlices<=0) 
            return -1;
        boolean complex=false;
        IPOGaussianNodeComplex[] pcIPOGs=new IPOGaussianNodeComplex[numSlices];

        int index;

        for(i=0;i<len;i++){
            IPOG=IPOGs0.get(i);
            slice=IPOG.sliceIndex;
            index=slice-sliceI;
            IPOGc=pcIPOGs[index];
            if(IPOGc==null){
                IPOGc=new IPOGaussianNodeComplex();
                IPOGc.addIPOG(IPOG);
                pcIPOGs[index]=IPOGc;
            }else{
                IPOGc.addIPOG(IPOG);
                complex=true;
            }
        }

        for(slice=sliceI;slice<=sliceF;slice++){
            index=slice-sliceI;
            IPOG=pcIPOGs[index];
            if(IPOG!=null&&!complex)IPOG=pcIPOGs[index].getIPOGs().get(0);
            m_cvIPOGs.add(pcIPOGs[index]);
        }
        bVarRangesUpdated=false;
        return 1;
    }
    public PlotWindow plotAmp(String option){
        return plotAmp(option, firstSlice,lastSlice);
    }
    public PlotWindow plotAmp(String option, int sliceI, int sliceF){
        String sTitle="Track"+TrackIndex;
        String xLabel="Slice";
        String yLabel=option;
        if(option.contentEquals("Sum")) yLabel="Total Signal";
        int len=sliceF-sliceI+1,index,slice;
        double[] pdX=new double[len],pdY=new double[len];
        IPOGaussianNode IPOG;
        double x=0,y;
        for(slice=sliceI;slice<=sliceF;slice++){
            index=slice-sliceI;
            IPOG=this.getIPOG(slice);
            if(IPOG==null){
                y=-10;
            }else{
                y=IPOG.getValue(option);
            }
            pdX[index]=slice;
            pdY[index]=y;
        }
        PlotWindow pw=new PlotWindow(sTitle,xLabel,yLabel,pdX,pdY);
        pw.draw();
        return pw;
    }
    public int calParRanges(){
        if(bVarRangesUpdated) return -1;
        ArrayList<Double> dvHeights=new ArrayList(),dvTotalSignal=new ArrayList(),dvArea=new ArrayList(),dvHWRatio=new ArrayList(),dvClusterSize=new ArrayList();
        ArrayList<Double> dvX=new ArrayList(),dvY=new ArrayList(),dvDrift=new ArrayList();
        Histogram cHeightHist,cTotalSignalHist,cAreaHist,cHWRatioHist,cClusterSizeHist,cXHist,cYHist,cDriftHist;
        int i,len=m_cvIPOGs.size(),slice;
        IPOGaussianNode IPOG;

        cHeightRange=new DoubleRange();
        cClusterSizeRange=new intRange();
        cTotalSignalRange=new DoubleRange();
        cAreaRange=new intRange();
        cHeightWidthRatioRange=new DoubleRange();

        firstSlice=Integer.MAX_VALUE;
        lastSlice=Integer.MIN_VALUE;
        cXRange2=new DoubleRange();
        cXRange3=new DoubleRange();
        cYRange2=new DoubleRange();
        cYRange3=new DoubleRange();
        cXCRange=new DoubleRange();
        cYCRange=new DoubleRange();
        cDriftRange=new DoubleRange();
        Point pt,pt0=null;
        edgeTrack=false;
        double drift;
        meanDrift=0;
        double num=0;
        for(i=0;i<len;i++){
            IPOG=m_cvIPOGs.get(i);
            if(IPOG==null) continue;
            if(IPOG.cXRange2==null)
                IPOGaussianNodeHandler.buildIPOGVarRanges(m_cvIPOGs);
            if(w>0) edgeTrack=IPOGaussianNodeHandler.isEdgeIPOG3(IPOG, w, h);
            cXRange2.expandRange(IPOG.cXRange2);
            cXRange3.expandRange(IPOG.cXRange3);
            cYRange2.expandRange(IPOG.cYRange2);
            cYRange3.expandRange(IPOG.cYRange3);
            slice=IPOG.sliceIndex;
            if(slice<firstSlice) firstSlice=slice;
            if(slice>lastSlice) lastSlice=slice;
            pt=IPOG.getCenter();
            if(i==0) pt0=pt;

            dvTotalSignal.add(IPOG.getValue("Signal"));
            dvHeights.add(IPOG.getValue("Amp"));
            dvArea.add(IPOG.getValue("Area"));
            dvClusterSize.add((double)IPOG.getClusterSize());
            dvX.add((double)pt.x);
            dvY.add((double)pt.y);
            drift=CommonMethods.getDistance(pt0.x,pt0.y,pt.x,pt.y);
            meanDrift+=drift;
            dvDrift.add(drift);
            IPOG.dDrift=drift;
            dvHWRatio.add(1.);

            cHeightRange.expandRange(IPOG.getValue("Amp"));
            cClusterSizeRange.expandRange(IPOG.getClusterSize());
            cTotalSignalRange.expandRange(IPOG.getValue("Signal"));
            cDriftRange.expandRange(drift);
            cAreaRange.expandRange((int)(IPOG.getValue("Area")+0.5));
            cHeightWidthRatioRange.expandRange(1);
            meanHWRatio+=1;
            cXCRange.expandRange(IPOG.getCenter().x);
            cYCRange.expandRange(IPOG.getCenter().y);
            pt0=pt;
            num+=1;
        }
        meanHWRatio/=num;
        meanDrift/=num;
        cTotalSignalHist=new Histogram(dvTotalSignal);
        cHeightHist=new Histogram(dvTotalSignal);
        cAreaHist=new Histogram(dvArea);
        cHWRatioHist=new Histogram(dvHWRatio);
        cClusterSizeHist=new Histogram(dvClusterSize);
        cXHist=new Histogram(dvX);
        cYHist=new Histogram(dvY);
        cDriftHist=new Histogram(dvDrift);
        bVarRangesUpdated=true;
        m_nComplexity=getComplexity();
        return 1;
    }
    public IPOGaussianNode getIPOG(int sliceIndex){//sliceIndex starts from 1
        if(sliceIndex<firstSlice||sliceIndex>lastSlice) return null;
        int index=sliceIndex-firstSlice;
        if(index>=m_cvIPOGs.size()) fillGapsWithNulls();
        IPOGaussianNode IPOG=m_cvIPOGs.get(index);
        if(IPOG==null) return null;//the gaps are already filled with null's
        if(IPOG.sliceIndex==sliceIndex) return IPOG;
        fillGapsWithNulls();
        return m_cvIPOGs.get(index);
    }
    public IPOGExtention getIPOGE(int sliceIndex){//sliceIndex starts from 1
        if(sliceIndex<firstSlice||sliceIndex>lastSliceE) return null;
        if(cvIPOGExts==null) return null;
        if(cvIPOGExts.isEmpty()) return null;
        int index=sliceIndex-firstSlice;
        IPOGExtention IPOGE=cvIPOGExts.get(index);
        return IPOGE;
    }
    public IPOGaussianNode getPreviousIPOG(int sliceIndex){//sliceIndex starts from 1
        sliceIndex--;
        if(sliceIndex<firstSlice||sliceIndex>lastSlice) return null;
        int index=sliceIndex-firstSlice;
        if(index>=m_cvIPOGs.size()) fillGapsWithNulls();
        IPOGaussianNode IPOG=m_cvIPOGs.get(index);
        while(IPOG==null){
            index--;
            if(sliceIndex<firstSlice) return null;
            IPOG=m_cvIPOGs.get(index);
        }
        return IPOG;
    }
    public IPOGaussianNode getNextIPOG(int sliceIndex){//sliceIndex starts from 1
        sliceIndex++;
        if(sliceIndex<firstSlice||sliceIndex>lastSlice) return null;
        int index=sliceIndex-firstSlice;
        if(index>=m_cvIPOGs.size()) fillGapsWithNulls();
        IPOGaussianNode IPOG=m_cvIPOGs.get(index);
        while(IPOG==null){
            index++;
            if(sliceIndex>lastSlice) return null;
            IPOG=m_cvIPOGs.get(index);
        }
        return IPOG;
    }
    public int getTrackLength(){
        return lastSlice-firstSlice+1;
    }
    void calCommonRanges(intRange xRangeCommon, intRange yRangeCommon, int sliceI, int sliceF){
        intRange xRange=new intRange((int)(cXRange3.getMin()+0.5), (int)(cXRange3.getMax()+0.5)),yRange=new intRange((int)(cYRange3.getMin()+0.5), (int)(cYRange3.getMax()+0.5));
        xRangeCommon.setRange(xRange.getMin(), xRange.getMax());
        yRangeCommon.setRange(yRange.getMin(), yRange.getMax());

        intRange xrt=new intRange(),yrt=new intRange();
        int x0=xRange.getMidpoint(),y0=yRange.getMidpoint(),slice,dx,dy;
        Point pt;
        IPOGaussianNode IPOG;
        for(slice=sliceI;slice<=sliceF;slice++){
            IPOG=m_cvIPOGs.get(slice-firstSlice);
            if(IPOG==null) continue;
            pt=IPOG.getCenter();
            dx=pt.x-x0;
            dy=pt.y-y0;
            IPOG.getRanges3(xrt, yrt);
            xrt.shiftRange(-dx);
            yrt.shiftRange(-dy);

            xRangeCommon.setCommonRange(xrt);
            yRangeCommon.setCommonRange(yrt);
        }
    }
    void calCommonRanges(intRange xRangeCommon, intRange yRangeCommon){
        calCommonRanges(xRangeCommon,yRangeCommon,firstSlice,lastSlice);
    }
    public int calRWAIPOShapes(int[][][] pixelsCompensated,int[][] pixelsScratch, int[][] pnScratcht,int rwSize){
        if(edgeTrack) return -1;
        if(RWA_Ws==rwSize) return 1;
        int w=pixelsScratch[0].length,h=pixelsScratch.length;
        int len=2*rwSize+1;
        int slice;
        int sI=Math.min(firstSlice+len-1, lastSlice),sF=lastSlice-rwSize;//the range of the slices to slide
        int i,dx,dy;
        IPOGaussianNode IPOG;
        CommonStatisticsMethods.setElements(pixelsScratch, 0);

        FittingModelNode cModel;
        intRange xRange=new intRange((int)(cXRange3.getMin()+0.5), (int)(cXRange3.getMax()+0.5)),yRange=new intRange((int)(cYRange3.getMin()+0.5), (int)(cYRange3.getMax()+0.5));
        intRange xRangeCommon=new intRange(xRange), yRangeCommon=new intRange(yRange);
        calCommonRanges(xRangeCommon,yRangeCommon);

        len=xRangeCommon.getRange()*yRangeCommon.getRange();

        cISAveIPOs=new RectangleImage(xRangeCommon.getRange(),yRangeCommon.getRange());
        cISAveIPOs.setLocation(new Point(xRangeCommon.getMin(),yRangeCommon.getMin()));
        Point p0=new Point(xRange.getMidpoint(),yRange.getMidpoint()),pt;

        ArrayList<IPOGaussianNode> cvIPOGs=new ArrayList();

        int num=0;
        int sign=1;
        for(slice=firstSlice;slice<=sI;slice++){
            i=slice-firstSlice;
            IPOG=m_cvIPOGs.get(i);
            if(IPOG==null) continue;
            num++;
            pt=IPOG.getCenter();
            dx=pt.x-p0.x;
            dy=pt.y-p0.y;
            CommonStatisticsMethods.copyArray(pixelsCompensated[slice-1], pnScratcht,yRange.getMin(), yRange.getMax(),xRange.getMin(),xRange.getMax());
            IPOGaussianNodeHandler.getSuperImposition(pnScratcht, IPOG,1);
            CommonStatisticsMethods.addArray(pnScratcht, pixelsScratch,yRange.getMin(), yRange.getMax(),xRange.getMin(),xRange.getMax(),sign,-dy,-dx);
        }
        ArrayList<Point> peaks=new ArrayList();
        peaks.add(p0);

        int wt=Math.min(21,xRangeCommon.getRange()),ht=Math.min(yRangeCommon.getRange(), 21);
        cISAveIPOs=new RectangleImage(wt,ht);

        double scale=1./(double)num;
        cModel=IPOGaussianExpander.getDefaultIPOGaussianFittingModel(pixelsScratch, cISAveIPOs, "gaussian2D_GaussianPars", peaks, scale);
        Non_Linear_Fitter.getFittedModel(cModel, cModel, 0.1, null);

        IPOGaussianNode IPOGAve=new IPOGaussianNode(cModel.pdPars);
        cModel.toGaussian2D_GaussianPars();
        IPOGAve.cModel=cModel;

        for(slice=firstSlice;slice<=firstSlice+rwSize;slice++){
            if(slice>sI) continue;//iI==lastSlice<rwSize;
            i=slice-firstSlice;
            if(m_cvIPOGs.get(i)!=null)
                cvIPOGs.add(IPOGAve);
            else
                cvIPOGs.add(null);
        }
        int io,in,slicex=0;

        for(slice=firstSlice+rwSize+1;slice<=sF;slice++){

                i=slice-firstSlice;
            io=i-rwSize-1;
            IPOG=m_cvIPOGs.get(io);
            if(IPOG!=null){
                num--;
                sign=-1;
                pt=IPOG.getCenter();
                dx=pt.x-p0.x;
                dy=pt.y-p0.y;
                CommonStatisticsMethods.copyArray(pixelsCompensated[slice-1], pnScratcht,yRange.getMin(), yRange.getMax(),xRange.getMin(),xRange.getMax());
                IPOGaussianNodeHandler.getSuperImposition(pnScratcht, m_cvIPOGs.get(i),1);
                CommonStatisticsMethods.addArray(pnScratcht, pixelsScratch,yRange.getMin(), yRange.getMax(),xRange.getMin(),xRange.getMax(),sign,-dy,-dx);
            }
            in=i+rwSize;
            if(in<=lastSlice-firstSlice){
                IPOG=m_cvIPOGs.get(in);
                if(IPOG!=null) {
                    num++;
                    sign=1;
                    pt=IPOG.getCenter();
                    dx=pt.x-p0.x;
                    dy=pt.y-p0.y;
                    CommonStatisticsMethods.copyArray(pixelsCompensated[slice-1], pnScratcht,yRange.getMin(), yRange.getMax(),xRange.getMin(),xRange.getMax());
                    IPOGaussianNodeHandler.getSuperImposition(pnScratcht, m_cvIPOGs.get(i),1);
                    CommonStatisticsMethods.addArray(pnScratcht, pixelsScratch,yRange.getMin(), yRange.getMax(),xRange.getMin(),xRange.getMax(),sign,-dy,-dx);
                }
            }
            if(m_cvIPOGs.get(i)==null) {
                cvIPOGs.add(null);
                continue;
            }
            scale=1./(double)num;
            cModel=IPOGaussianExpander.getDefaultIPOGaussianFittingModel(pixelsScratch, cISAveIPOs, "gaussian2D_GaussianPars", peaks,scale);
            Non_Linear_Fitter.getFittedModel(cModel, cModel, 0.1, null);
            cModel.toGaussian2D_GaussianPars();
            IPOGAve=new IPOGaussianNode(cModel.pdPars);
            IPOGAve.cModel=cModel;
            cvIPOGs.add(IPOGAve);
            if(slice>slicex) slicex=slice;
        }

        for(slice=sF+1;slice<=lastSlice;slice++){
            i=slice-firstSlice;
            if(m_cvIPOGs.get(i)!=null)
                cvIPOGs.add(IPOGAve);
            else
                cvIPOGs.add(null);
        }
        RWA_Ws=rwSize;
        m_cHeadIPOG=new IPOGaussianNodeComplex(cvIPOGs);
        return 1;
    }
    public void getDistToEdges(int w, int h, int sliceI, int sliceF, intRange xToEdges, intRange yToEdges){
        xToEdges.setRange(w-1,w-1);
        yToEdges.setRange(h-1,h-1);
        int slice;
        IPOGaussianNode IPOG;
        Point pt;
        for(slice=sliceI;slice<=sliceF;slice++){
            IPOG=getIPOGE(slice);
            if(IPOG==null) continue;
            pt=IPOG.getCenter();
            xToEdges.setMin(Math.min(xToEdges.getMin(),pt.x));
            yToEdges.setMin(Math.min(yToEdges.getMin(),pt.y));
            xToEdges.setMax(Math.min(xToEdges.getMax(),Math.abs(w-1-pt.x)));
            yToEdges.setMax(Math.min(yToEdges.getMax(),Math.abs(h-1-pt.y)));
        }
    }
    public IPOGaussianNodeComplex buildAveIPO(int[][][] pixelsCompensated,int[][] pixelsScratch, int[][] pnScratcht,int sliceI, int sliceF, intRange xRangeT, intRange yRangeT){
        boolean fitIPOG=true;
        return buildAveIPO(pixelsCompensated,pixelsScratch, pnScratcht,sliceI, sliceF, xRangeT, yRangeT,fitIPOG);
    }
    public IPOGaussianNodeComplex buildAveIPO(int[][][] pixelsCompensated,int[][] pixelsScratch, int[][] pnScratcht,int sliceI, int sliceF, intRange xRangeT, intRange yRangeT, boolean fitIPOG){
        int w=pixelsScratch[0].length,h=pixelsScratch.length,slices=pixelsCompensated.length;
        int slice;
        int i,dx,dy,radius=10;
        if(sliceI<firstSlice){
            sliceI=firstSlice;
        }
        if(sliceF>lastSliceE){
            sliceF=lastSliceE;
        }
        IPOGaussianNode IPOG;
        CommonStatisticsMethods.setElements(pixelsScratch, 0);

        FittingModelNode cModel;
        if(sliceI>lastSliceE||sliceF<firstSlice) return null;
        IPOG=getIPOGE(sliceI);
//        if(IPOG==null) IPOG=getNextIPOG(sliceI);
        Point p0=new Point(IPOG.getCenter()),pt;

        intRange xToEdges=new intRange(w-1,w-1), yToEdges=new intRange(h-1,h-1);
        getDistToEdges(w,h,sliceI,sliceF,xToEdges,yToEdges);
        xToEdges.setMin(Math.min(xToEdges.getMin(),radius));
        yToEdges.setMin(Math.min(yToEdges.getMin(),radius));
        xToEdges.setMax(Math.min(xToEdges.getMax(),radius));
        yToEdges.setMax(Math.min(yToEdges.getMax(),radius));
        ImageShape cISAveIPO;


        int num=0;
        num=0;
        int sign=1;
        for(slice=sliceI;slice<=sliceF;slice++){
            i=slice-firstSlice;
 //           IPOG=m_cvIPOGs.get(i);
            IPOG=cvIPOGExts.get(i);
            if(IPOG==null) continue;
            pt=IPOG.getCenter();
            dx=pt.x-p0.x;
            dy=pt.y-p0.y;
            if(i>=slices)
                continue;
            CommonStatisticsMethods.copyArray(pixelsCompensated[i], pnScratcht,pt.y-yToEdges.getMin(), pt.y+yToEdges.getMax(),pt.x-xToEdges.getMin(),pt.x+xToEdges.getMax());
            if(slice<=lastSlice){
                IPOG=m_cvIPOGs.get(i);
                if(IPOG!=null)IPOGaussianNodeHandler.getSuperImposition(pnScratcht, IPOG,1);
            }
            CommonStatisticsMethods.addArray(pnScratcht,pixelsScratch,pt.y-yToEdges.getMin(), pt.y+yToEdges.getMax(),pt.x-xToEdges.getMin(),pt.x+xToEdges.getMax(),sign,-dy,-dx);
            num++;
        }

        ArrayList<Point> peaks=new ArrayList();
        peaks.add(p0);

        double scale=1./(double)num;
        int wt=xToEdges.getMin()+xToEdges.getMax()+1,ht=yToEdges.getMin()+yToEdges.getMax()+1;
        wt=Math.min(13, wt);
        ht=Math.min(13,ht);
        cISAveIPO=new RectangleImage(wt,ht);
        cISAveIPO.setCenter(p0);
//        cISAveIPO.setLocation(new Point(p0.x-xToEdges.getMin(),p0.y-yToEdges.getMin()));
        cISAveIPO.setFrameRanges(new intRange(p0.x-xToEdges.getMin(),p0.x+xToEdges.getMax()), new intRange(p0.y-yToEdges.getMin(),p0.y+yToEdges.getMax()));
        intRange xFRange=cISAveIPO.getXFrameRange(),yFRange=cISAveIPO.getYFrameRange();
        xFRange.setMin(Math.max(0, xFRange.getMin()));
        yFRange.setMin(Math.max(0, yFRange.getMin()));
        xFRange.setMax(Math.min(w-1, xFRange.getMax()));
        yFRange.setMin(Math.min(h-1, yFRange.getMin()));
        IPOGaussianNodeComplex IPOGAve=null;
        CommonStatisticsMethods.scaleArray(pixelsScratch,scale,p0.y-yToEdges.getMin(),p0.y+yToEdges.getMax(),p0.x-xToEdges.getMin(),p0.x+xToEdges.getMax());
        CircleImage circle=new CircleImage(1);
        circle.setCenter(p0);
        circle.setFrameRanges(xFRange, yFRange);
        int pixel=(int)(ImageShapeHandler.getMean(pixelsScratch, circle)+0.5);
        pixelsScratch[p0.y][p0.x]=pixel;
        
        if(fitIPOG){
            cModel=IPOGaussianExpander.getDefaultIPOGaussianFittingModel(pixelsScratch, cISAveIPO, "gaussian2D_GaussianPars", peaks, 1.);
            Non_Linear_Fitter.getFittedModel_Simplex(cModel, cModel, 0.01, null);

            IPOGAve=new IPOGaussianNodeComplex(cModel.pdPars);
            cModel.toGaussian2D_GaussianPars();
    //        cModel.m_cIS=cISAveIPO;
            IPOGAve.cModel=cModel;
            IPOGAve.setSliceRange(sliceI, sliceF);
        }
        
        if(xRangeT!=null){
            xRangeT.setRange(p0.x-xToEdges.getMin(),p0.x+xToEdges.getMax());
            yRangeT.setRange(p0.y-yToEdges.getMin(),p0.y+yToEdges.getMax());
        }
        return IPOGAve;
    }

    public ArrayList<Point> getCompoMaximaInRegion(int[][][] pixels,int[][] pixelsScratch, int[][] pnScratcht,int sliceI, int sliceF, intRange xRangeT, intRange yRangeT){
        buildAveIPO_Raw(pixels,pixelsScratch,pnScratcht,sliceI,sliceF,xRangeT,yRangeT);
        int w=xRangeT.getRange(),h=yRangeT.getRange();
        if(w<5||h<5) return new ArrayList<Point> ();
        int[][] pixelst=new int[h][w];
        int x0=xRangeT.getMin(),y0=yRangeT.getMin();
        Point center=new Point(w/2,h/2);
        CommonStatisticsMethods.copyArray(pixelsScratch, pixelst, yRangeT.getMin(), yRangeT.getMax(), xRangeT.getMin(), xRangeT.getMax(), -y0, -x0);
        CommonStatisticsMethods.meanFiltering(pixelst, 1);
 //       CommonMethods.displayAsImage("AveIPOShape1", w, h, pixelst, ImagePlus.GRAY16);
        ArrayList<Point> comboMaxima=CommonMethods.getComboMaximaInRegion(pixelst, center);
//        CommonMethods.displayAsImage("AveIPOShape2", w, h, pixelst, ImagePlus.GRAY16);
        int i,len=comboMaxima.size();
        for(i=0;i<len;i++){
            comboMaxima.get(i).translate(x0, y0);
        }
        return comboMaxima;
    }
    public int buildAveIPO_Raw(int[][][] pixels,int[][] pixelsScratch, int[][] pnScratcht,int sliceI, int sliceF, intRange xRangeT, intRange yRangeT){
        //build shape only, no fitting
        int w=pixelsScratch[0].length,h=pixelsScratch.length;
        int slice;
        int i,dx,dy,radius=10;
        IPOGaussianNode IPOG;
        CommonStatisticsMethods.setElements(pixelsScratch, 0);

        if(sliceI<firstSlice)
            sliceI=firstSlice;
        if(sliceF>lastSliceE)
            sliceF=lastSliceE;

        if(sliceI>lastSliceE||sliceF<firstSlice) return -1;
        IPOG=getIPOG(sliceI);
        if(IPOG==null) IPOG=cvIPOGExts.get(sliceI-firstSlice);
        Point p0=new Point(IPOG.getCenter()),pt;

        intRange xToEdges=new intRange(w-1,w-1), yToEdges=new intRange(h-1,h-1);
        getDistToEdges(w,h,sliceI,sliceF,xToEdges,yToEdges);
        xToEdges.setMin(Math.min(xToEdges.getMin(),radius));
        yToEdges.setMin(Math.min(yToEdges.getMin(),radius));
        xToEdges.setMax(Math.min(xToEdges.getMax(),radius));
        yToEdges.setMax(Math.min(yToEdges.getMax(),radius));

        int num=0;
        int sign=1;
        
        for(slice=sliceI;slice<=sliceF;slice++){
            i=slice-firstSlice;
//            IPOG=m_cvIPOGs.get(i);
            IPOG=cvIPOGExts.get(i);
            if(IPOG==null) continue;
            num++;
            pt=IPOG.getCenter();
            dx=pt.x-p0.x;
            dy=pt.y-p0.y;
            CommonStatisticsMethods.addArray(pixels[i],pixelsScratch,pt.y-yToEdges.getMin(), pt.y+yToEdges.getMax(),pt.x-xToEdges.getMin(),pt.x+xToEdges.getMax(),sign,-dy,-dx);
        }


        double scale=1./(double)num;
        CommonStatisticsMethods.scaleArray(pixelsScratch,scale,p0.y-yToEdges.getMin(),p0.y+yToEdges.getMax(),p0.x-xToEdges.getMin(),p0.x+xToEdges.getMax());
        //13212
        CircleImage circle=new CircleImage(1);
        circle.setCenter(p0);
        circle.setFrameRanges(new intRange(Math.max(0,p0.x-xToEdges.getMin()),Math.min(w-1,p0.x+xToEdges.getMax())), new intRange(Math.max(0,p0.y-yToEdges.getMin()),Math.min(h-1,p0.y+yToEdges.getMax())));
        int pixel=(int)(ImageShapeHandler.getMean(pixelsScratch, circle)+0.5);
        pixelsScratch[p0.y][p0.x]=pixel;
        
        if(xRangeT!=null){
            xRangeT.setRange(Math.max(0,p0.x-xToEdges.getMin()),Math.min(w-1,p0.x+xToEdges.getMax()));
            yRangeT.setRange(Math.max(0,p0.y-yToEdges.getMin()),Math.min(h-1,p0.y+yToEdges.getMax()));
        }
        return 1;
    }
    
    public int buildAveIPO_RawPixels(int[][][] pixels,int[][] pixelsScratch, int[][] pnScratcht,int sliceI, int sliceF, intRange xRangeT, intRange yRangeT){
        //build shape only, no fitting
        int w=pixelsScratch[0].length,h=pixelsScratch.length;
        int slice;
        int i,dx,dy,radius=10;
        IPOGaussianNode IPOG;
        CommonStatisticsMethods.setElements(pixelsScratch, 0);

        if(sliceI<firstSlice)
            sliceI=firstSlice;
        if(sliceF>lastSliceE)
            sliceF=lastSliceE;

        if(sliceI>lastSliceE||sliceF<firstSlice) return -1;
        IPOG=getIPOG(sliceI);
        if(IPOG==null) IPOG=cvIPOGExts.get(sliceI-firstSlice);
        Point p0=new Point(IPOG.getCenter()),pt;

        intRange xToEdges=new intRange(w-1,w-1), yToEdges=new intRange(h-1,h-1);
        getDistToEdges(w,h,sliceI,sliceF,xToEdges,yToEdges);
        xToEdges.setMin(Math.min(xToEdges.getMin(),radius));
        yToEdges.setMin(Math.min(yToEdges.getMin(),radius));
        xToEdges.setMax(Math.min(xToEdges.getMax(),radius));
        yToEdges.setMax(Math.min(yToEdges.getMax(),radius));

        int num=0;
        int sign=1;
        
        for(slice=sliceI;slice<=sliceF;slice++){
            i=slice-firstSlice;
//            IPOG=m_cvIPOGs.get(i);
            IPOG=cvIPOGExts.get(i);
            if(IPOG==null) continue;
            num++;
            pt=IPOG.getCenter();
            dx=pt.x-p0.x;
            dy=pt.y-p0.y;
            CommonStatisticsMethods.addArray(pixels[i],pixelsScratch,pt.y-yToEdges.getMin(), pt.y+yToEdges.getMax(),pt.x-xToEdges.getMin(),pt.x+xToEdges.getMax(),sign,-dy,-dx);
        }


        double scale=1./(double)num;
        CommonStatisticsMethods.scaleArray(pixelsScratch,scale,p0.y-yToEdges.getMin(),p0.y+yToEdges.getMax(),p0.x-xToEdges.getMin(),p0.x+xToEdges.getMax());
        
        //13212
        CircleImage circle=new CircleImage(1);
        circle.setCenter(p0);
        circle.setFrameRanges(new intRange(Math.max(0,p0.x-xToEdges.getMin()),Math.min(w-1,p0.x+xToEdges.getMax())), new intRange(Math.max(0,p0.y-yToEdges.getMin()),Math.min(h-1,p0.y+yToEdges.getMax())));
        int pixel=(int)(ImageShapeHandler.getMean(pixelsScratch, circle)+0.5);
        pixelsScratch[p0.y][p0.x]=pixel;
        
        if(xRangeT!=null){
            xRangeT.setRange(p0.x-xToEdges.getMin(),p0.x+xToEdges.getMax());
            yRangeT.setRange(p0.y-yToEdges.getMin(),p0.y+yToEdges.getMax());
        }
        return 1;
    }
    
    public void setImageDim(int w, int h){
        this.w=w;
        this.h=h;
    }
    public void setLevelInfo(IPOGTLevelInfoNode levelInfo){
        int index=getLevelInfoNodeIndex(levelInfo.sQuantityName);
        m_cLevelInfo=levelInfo;        
        if(index<0){
            m_cvLevelInfoNodes.add(levelInfo);
        }else{
            m_cvLevelInfoNodes.set(index,levelInfo);
        }
    }
    public int getLevelInfoNodeIndex(String QuantityName){
        int i,len=m_cvLevelInfoNodes.size();
        for(i=0;i<len;i++){
            if(m_cvLevelInfoNodes.get(i).sQuantityName.contentEquals(QuantityName)) return i;
        }
        return -1;
    }
    public IPOGTLevelInfoNode getLevelInfo(){
        return m_cLevelInfo;
    }
    public void activateLevelInfo(String sQuantityName){
        int index=getLevelInfoNodeIndex(sQuantityName);
        if(index>=0)
            m_cLevelInfo=m_cvLevelInfoNodes.get(index);
        else
            m_cLevelInfo=null;
    }
    public int getLevelInfoAsString(StringBuffer names,StringBuffer values, String quantityName ,boolean Manual){
        activateLevelInfo(quantityName);
        if(m_cLevelInfo==null) return -1;
        int verify=1,exclusion=-1,confirmed=1;
        if(!m_cLevelInfo.verifiable())verify=-1;
        if(m_cLevelInfo.Excluded()) exclusion=1;
        if(!m_cLevelInfo.Confirmed()) confirmed=-1;
        names.append("TrackIndex");
        values.append(""+TrackIndex);
        names.append(",Valid");
        values.append(","+verify);
        names.append(",Confirmed");
        values.append(","+confirmed);
        names.append(",Nullified");
        values.append(","+exclusion);
        names.append(",xRange");
        values.append(","+cXCRange.getDataRangeAsString(1));
        names.append(",yRange");
        values.append(","+cYCRange.getDataRangeAsString(1));
        double dx=cXCRange.getRange(),dy=cYCRange.getRange();
        names.append(",totalDrift");
        values.append(","+PrintAssist.ToString(Math.sqrt(dx*dx+dy*dy),1));
        names.append(",meanDrift");
        values.append(","+PrintAssist.ToString(meanDrift,1));
        m_cLevelInfo.getLevelInfoAsString(names,values,Manual);
        return 1;
    }
    public int setHeadComboMaxima(int[][][] pixelsStack,int[][] pnScratch,int[][] pnScratch1,int ws){
        intRange xRange=new intRange(),yRange=new intRange();
        int sI=firstSlice,sF=Math.min(sI+ws, lastSlice);
        m_cvHeadComboMaxima=getCompoMaximaInRegion(pixelsStack,pnScratch,pnScratch1,sI,sF,xRange,yRange);
        m_nComplexity=getComplexity();
        return 1;
    }
    public int getComplexity(){
        if(m_cvHeadComboMaxima==null) return -1;
        return m_cvHeadComboMaxima.size();
    }
/*    public int setLevelComplexity(int[][][] pixelsStack,int[][] pixelsScratch,int[][] pnScratach){
        int i,complexity=0,len=m_cLevelInfo.m_cvLevelNodes_M.size(),num,sI,sF;
        IPOGTLevelNode lNode;
        intRange xRange=new intRange(),yRange=new intRange();        
        ArrayList<Point> comboMaxima=this.getCompoMaximaInRegion(pixelsStack, pixelsScratch, pnScratach, firstSlice, lastSlice, xRange, yRange);
        m_cLevelInfo.complexity=comboMaxima.size();
        m_cLevelInfo.m_cvComboMaixima=comboMaxima;
        for(i=0;i<len;i++){
            lNode=m_cLevelInfo.m_cvLevelNodes_M.get(i);
            xRange.resetRange();
            yRange.resetRange();
            sI=(int)(lNode.dXStart+0.5);
            sF=(int)(lNode.dXEnd+0.5);
            comboMaxima=getCompoMaximaInRegion(pixelsStack, pixelsScratch, pnScratach, sI, sF, xRange, yRange);
            num=comboMaxima.size();
            lNode.complexity=num;
            lNode.cvComboMaxima=comboMaxima;
            if(num>complexity) complexity=num;
        }
        return 1;
    }*/
    public double getValue(int slice,String plottingOption){
        IPOGaussianNode IPOG=getIPOG(slice);
        if(IPOG==null) return Double.NaN;
        return IPOG.getValue(plottingOption);
    }
    public double getHeadValue(String plottingOption){
        return getValue(firstSlice,plottingOption);
    }
    public double getHeadValueAve(String plottingOption){
        getValue(firstSlice,plottingOption);
        int sI=firstSlice,sF=Math.min(sI+5, lastSlice),i,num=0;
        IPOGaussianNode IPOG;
        double ave=0;
        for(i=sI;i<=sF;i++){
            IPOG=getIPOG(i);
            if(IPOG==null) continue;
            num++;
            ave+=IPOG.getValue(plottingOption);
        }
        if(num==0) 
            ave=0;
        else
            ave/=num;
        return ave;
    }
    public void setBundleSize(int bundleSize){
        BundleSize=bundleSize;
    }
    public int setFirstSlice(int sliceI){
        if(sliceI<=firstSlice) return -1;
        if(sliceI>=lastSlice){
            m_cvIPOGs.clear();
            return -1;
        }
        IPOGaussianNode IPOG=m_cvIPOGs.get(0),IPOGt;
        int slice=IPOG.sliceIndex;
        while(slice<sliceI){
            IPOG=getNextIPOG(slice);
            if(IPOG==null){
                m_cvIPOGs.clear();
                return -1;
            }
            slice=IPOG.sliceIndex;
            firstSlice=slice;
        }
        IPOGt=m_cvIPOGs.get(0);
        while(IPOGt!=IPOG){
            m_cvIPOGs.remove(0);
            IPOGt=m_cvIPOGs.get(0);
        }
        return 1;
    }
    public void getTrackData(ArrayList<Double> dvX, ArrayList<Double> dvY, String option){
        dvX.clear();
        dvY.clear();
        int len=m_cvIPOGs.size(),slice;
        IPOGaussianNode IPOG;
        for(slice=firstSlice;slice<=lastSlice;slice++){
            IPOG=m_cvIPOGs.get(slice-firstSlice);
            if(IPOG==null) continue;
            dvX.add((double)slice);
            dvY.add(IPOG.getValue(option));
        }
        int index;
        len=cvIPOGExts.size();
        for(slice=lastSlice+1;slice<=lastSliceE;slice++){
            index=slice-firstSlice;
            if(index>=len) break;
            IPOG=cvIPOGExts.get(index);
            if(IPOG==null) continue;
            dvX.add((double)slice);
            dvY.add(IPOG.getValue(option));
        }
    }
    public void setRisingInterval(int n){
        m_nRisingInterval=n;
    }
    public int exportLevelInfo(DataOutputStream ds){
        int len=m_cvLevelInfoNodes.size(),i;
        try {
            ds.writeInt(len);
            for(i=0;i<len;i++){
                m_cvLevelInfoNodes.get(i).exportLevelInfo(ds);
            }
        }
        catch (IOException e){
            return -1;
        }        
        return 1;
    }
    public int importLevelInfo(BufferedInputStream bf, int versionNumber){
        try{
            int len=IOAssist.readInt(bf),i;
            for(i=0;i<len;i++){
                IPOGTLevelInfoNode cLINode=new IPOGTLevelInfoNode();
                cLINode.setVersionNumber(versionNumber);
                cLINode.cIPOGT=this;
                setLevelInfo(cLINode);
                cLINode.importLevelInfo(bf);
//                m_cvLevelInfoNodes.add(cLINode);
            }
        }
        catch (IOException e){
            return -1;
        }
        return 1;
    }
    public void TrackExtended(boolean extended){
        bTrackExtended=extended;
    }
    public static IPOGaussianNodeComplex buildAveIPOG_FullModel(IPOGTrackNode IPOGT, int[][][] pixelsCompensated, int[][] pixelsScreatch, int[][] pnScreatch, int sI, int sF0,intRange xRange, intRange yRange){
        return buildAveIPOG_FullModel(IPOGT, pixelsCompensated, pixelsScreatch, pnScreatch, sI, sF0,xRange, yRange, false, false, 0);
    }
    public static IPOGaussianNodeComplex buildAveIPOG_FullModel(IPOGTrackNode IPOGT, int[][][] pixelsCompensated, int[][] pixelsScreatch, int[][] pnScreatch, int sI, int sF0,intRange xRange, intRange yRange, boolean BuildContour, boolean ShowContour, int FunctionType){
        int sF=sF0,models;
//        if(sI==59&&sF0==107){
        if(sI==59){
            sI=sI;
        }
        FittingModelNode aModel;
        boolean dummy=false;
        if(xRange==null) xRange=new intRange();
        if(yRange==null) yRange=new intRange();
        if(sF<=sI){
            sI=sI;
            return null;
        }
        IPOGaussianNodeComplex IPOG=IPOGT.buildAveIPO(pixelsCompensated, pixelsScreatch, pnScreatch, sI, sF, xRange,yRange);
        IPOGaussianNodeComplex IPOG0=IPOG;
        
        FittingResultsNode aResultsNode=null;
        
        if(IPOG!=null) aResultsNode=IPOGaussianFitter.getFullModelFitting(IPOG.cModel, 0.0001);
        if(sI==178&&sF0==208){
            sI=sI;
        }
        while(true){
            if(aResultsNode!=null){
                models=aResultsNode.m_cvModels.size();
                if(models>1)
                    aModel=aResultsNode.m_cvModels.get(models-2);
                else
                    aModel=aResultsNode.m_cvModels.get(0);
                if(!IPOGaussianFitter.invalidIPOGModel(aModel, null, null)) break;
            }
            sF-=2;
            if(sF<sI) {
                aResultsNode=new FittingResultsNode();
                aResultsNode.m_cvModels.add(IPOGaussianExpander.getDummyIPOGModel());
                dummy=true;
                break;
            }
            IPOG=IPOGT.buildAveIPO(pixelsCompensated, pixelsScreatch,pnScreatch, sI, sF,null,null);
            aResultsNode=IPOGaussianFitter.getFullModelFitting(IPOG.cModel, 0.0001);
        }
        models=aResultsNode.m_cvModels.size();
        
        if(models>1)
            aModel=aResultsNode.m_cvModels.get(models-2);
        else
            aModel=aResultsNode.m_cvModels.get(0);
//            fixInvalidAveIPOGModel(aModel,xRange,yRange);
        ArrayList<IPOGaussianNode> cvIPOGst=new ArrayList();
        IPOGaussianNodeHandler.buildIPOGaussianNode(cvIPOGst, aModel, -1, -1);
        IPOGaussianNodeComplex IPOGt;
        if(!cvIPOGst.isEmpty()){
            IPOGt=new IPOGaussianNodeComplex(cvIPOGst);
            IPOGt.setMeritPValue(aModel.dMeritPV);
        }else
            IPOGt=IPOGaussianExpander.getDummyIPOG();
        IPOGt.sortIPOGs(xRange,yRange);
        IPOGt.setSliceRange(sI, sF0);
        IPOGt.cModel=aModel;
        if(xRange.emptyRange()||yRange.emptyRange()){
            xRange=xRange;
        }
        if(BuildContour){
            TwoDFunction tdf=null;                        
            tdf=new SubpixelGaussianMean(pixelsScreatch,1.,5,xRange,yRange,IPOGContourParameterNode.GaussianMean);
            IPOGaussianNode IPOGtt=IPOGT.getIPOGE(sI);
            ((SubpixelGaussianMean)tdf).setCenter(IPOGtt.getCenter());
            if(dummy){
                DoublePair dp=tdf.getPeak();
                IPOGaussianNode IPO=IPOGt.getMainIPOG();
                DoubleRange dr=tdf.getValueRange();
                double[] pdPars=IPO.pdPars;
                pdPars[0]=dr.getMin();
                pdPars[1]=dr.getRange();
                pdPars[5]=dp.left;
                pdPars[6]=dp.right;
                IPO.cnst=tdf.getValueRange().getMin();
            }            
            IPOGt.buildContour_Percentage(tdf,IPOGContourParameterNode.GaussianMean,ShowContour);
            IPOGt.buildContour_Percentage(null,IPOGContourParameterNode.IPOG,ShowContour);
        }
        return IPOGt;
    }
    public static int buildAveIPOsRawPixelContour(IPOGaussianNodeComplex IPOG, IPOGTrackNode IPOGT,int[][][] pixelsStack, int[][] pixelsScratch, int[][] pnScratch, int sliceI, int sliceF, boolean ShowContour){
        IPOGContourParameterNode cpNode;
        if(pixelsScratch==null){
            int w=pixelsStack[0][0].length,h=pixelsStack[0].length;
            pixelsScratch=new int[h][w];
            pnScratch=new int[h][w];
        }
        intRange xRange=new intRange(), yRange=new intRange();
        IPOGT.buildAveIPO_Raw(pixelsStack, pixelsScratch, pnScratch, sliceI, sliceF, xRange, yRange);           
        if(xRange.emptyRange()||yRange.emptyRange()){
            xRange=xRange;
        }
        TwoDFunction tdf=null;                        
        tdf=new SubpixelGaussianMean(pixelsScratch,1.,5,xRange,yRange,IPOGContourParameterNode.GaussianMeanRaw);
            IPOGaussianNode IPOGtt=IPOGT.getIPOGE(sliceI);
            ((SubpixelGaussianMean)tdf).setCenter(IPOGtt.getCenter());
        if(IPOG.isDummy()){
                DoublePair dp=tdf.getPeak();
                IPOGaussianNode IPO=IPOG.getMainIPOG();
                DoubleRange dr=tdf.getValueRange();
                double[] pdPars=IPO.pdPars;
                pdPars[0]=dr.getMin();
                pdPars[1]=dr.getRange();
                pdPars[5]=dp.left;
                pdPars[6]=dp.right;
                IPO.cnst=tdf.getValueRange().getMin();
        }            
        IPOG.buildContour_Percentage(tdf,IPOGContourParameterNode.GaussianMeanRaw,ShowContour);
        return 1;
    }
    public static int buildAveIPOGaussianMeanContour(IPOGaussianNodeComplex IPOG, IPOGTrackNode IPOGT,int[][][] pixelsCompensated, int[][] pixelsScratch, int[][] pnScratch, int sliceI, int sliceF, boolean ShowContour){
        IPOGContourParameterNode cpNode;
        if(pixelsScratch==null){
            int w=pixelsCompensated[0][0].length,h=pixelsCompensated[0].length;
            pixelsScratch=new int[h][w];
            pnScratch=new int[h][w];
        }
        intRange xRange=new intRange(), yRange=new intRange();
        IPOGT.buildAveIPO(pixelsCompensated, pixelsScratch, pnScratch, sliceI, sliceF, xRange, yRange,false);           
        if(xRange.emptyRange()||yRange.emptyRange()){
            xRange=xRange;
        }
        TwoDFunction tdf=null;                        
        tdf=new SubpixelGaussianMean(pixelsScratch,1.,5,xRange,yRange,IPOGContourParameterNode.GaussianMean);
            IPOGaussianNode IPOGtt=IPOGT.getIPOGE(sliceI);
            ((SubpixelGaussianMean)tdf).setCenter(IPOGtt.getCenter());
        IPOG.buildContour_Percentage(tdf,IPOGContourParameterNode.GaussianMeanRaw,ShowContour);
        return 1;
    }
    public void removeUnfittedLevelInfoNodes(){
        if(m_cLevelInfo!=null){
            if(!m_cLevelInfo.Fitted()) m_cLevelInfo=null;
        }
        int i,len=m_cvLevelInfoNodes.size();
        for(i=len-1;i>=0;i--){
            if(!m_cvLevelInfoNodes.get(i).Fitted()) m_cvLevelInfoNodes.remove(i);
        }
    }
}
