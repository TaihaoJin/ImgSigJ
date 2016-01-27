/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import utilities.io.ByteConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.DoubleRange;
import utilities.AbfHandler.Abf;
import utilities.Geometry.ImageShapes.*;
import java.awt.Point;
import java.util.Hashtable;
import utilities.CommonStatisticsMethods;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import utilities.io.FileAssist;
import utilities.io.IOAssist;
import utilities.io.PrintAssist;
import utilities.statistics.MeanSem1;
import ij.IJ;
/**
 *
 * @author Taihao
 */
public class StackIPOGTracksNode {
    public int nNumTracks,m_nAbfChannels;
    public int sliceI,sliceF;
    public ArrayList<IPOGTrackNode> m_cvIPOGTracks,m_cvSelectedIPOGTracks;
    public ArrayList<IPOGTrackBundleNode> m_cvIPOGTBs;
    DoubleRange cHeightRange,cTotalSignalRange,cHWRatioRange,cTotalDriftRange,cStepDriftRange,cHeadValueRange;
    intRange cTrackLengthRange,cIPOAreaRange,cTrackIndexRange,cClusterSizeRange,cBundleSizeRange,cFirstSliceIndexRange,cLastSliceIndexRange,cBundleIndexRange,cComplexityRange;
    ArrayList<ActionListener> m_cvActionListeners;
    int[][][] pixelsCompensated,pixels,pixelsRaw;
    int w,h;
    String sImagePath;
    Hashtable m_cStackMinima;
    double[] pdHeightCutoffPs;
    double[][] ppdHeightCutoff;

    public StackIPOGTracksNode(){
        m_cvIPOGTracks=new ArrayList();
        cHeightRange=new DoubleRange();
        cTrackLengthRange=new intRange();
        cTrackIndexRange=new intRange();
        cClusterSizeRange=new intRange();
        cBundleSizeRange=new intRange();
        m_cvIPOGTBs=new ArrayList();
        cFirstSliceIndexRange=new intRange();
        cLastSliceIndexRange=new intRange();
        cIPOAreaRange=new intRange();
        cBundleIndexRange=new intRange();
        cHWRatioRange=new DoubleRange();
        cTotalDriftRange=new DoubleRange();
        cStepDriftRange=new DoubleRange();
        cTotalSignalRange=new DoubleRange();
        cComplexityRange=new intRange();
        m_cvActionListeners=new ArrayList();
        cHeadValueRange=new DoubleRange();
    }
    public StackIPOGTracksNode(ArrayList<IPObjectTrack> IPOTs){
        this();
        updateTracks(IPOTs);
    }
    void updateTracks(ArrayList<IPObjectTrack> IPOTs){
        nNumTracks=IPOTs.size();
        IPOGTrackNode IPOGT;
        m_cvIPOGTracks.clear();
        for(int i=0;i<nNumTracks;i++){
            IPOGT=new IPOGTrackNode(IPOTs.get(i));
            m_cvIPOGTracks.add(IPOGT);
        }
        checkTrackindexes();
        setSelectedIPOGTracks(m_cvIPOGTracks);
        buildIPOGTBundles();
        calParRanges();
        assignTrackIndexesInBundles();
    }
    
    public void setSelectedIPOGTracks(ArrayList<IPOGTrackNode> IPOGTs){
        if(m_cvSelectedIPOGTracks==null) m_cvSelectedIPOGTracks=new ArrayList();
        m_cvSelectedIPOGTracks.clear();
        int i,len=IPOGTs.size();
        for(i=0;i<len;i++){
            m_cvSelectedIPOGTracks.add(IPOGTs.get(i));
        }
    }

    public int importIPOGTrackLevelInfo(String path0, String plottingOption, int nRisingInterval){
//        String path=FileAssist.changeExt(path0, "TLG");
        String path=FileAssist.changeExt(path0, "TLH");//12n25
        int versionNumber=1;
        if(!FileAssist.fileExists(path)){
            path=FileAssist.changeExt(path, "TLF");
            versionNumber=-1;
        }
        IJ.showStatus("import levelinfo: "+FileAssist.getFileName(path));
        File f=new File(path);
        FileInputStream fs=null;
        try{fs=new FileInputStream(f);}
        catch (FileNotFoundException e){
            return -1;
        }
        BufferedInputStream bf=new BufferedInputStream(fs);
        byte bt[]=new byte[4],pb[];
        IPOGTrackNode IPOGT;
        int nNumAddParsPerIPO;
        int InfoStatus,len;
        m_cvIPOGTracks.clear();
        intRange ir=new intRange();
        int status=-1,i,j,n,m,sI,sF;
        double[] pt;
        try{//need to read imagepath here
            sImagePath=IOAssist.readString(bf);
            nNumTracks=IOAssist.readInt(bf);
            nNumAddParsPerIPO=IOAssist.readInt(bf);
            
            for(i=0;i<nNumTracks;i++){
                IPOGT=new IPOGTrackNode();
                status=IPOGT.importIPOGTrack(bf,nNumAddParsPerIPO);    
                if(status<0) 
                    continue;
                m_cvIPOGTracks.add(IPOGT);
                InfoStatus=IOAssist.readInt(bf);
                 ir.expandRange(IPOGT.firstSlice);
                 ir.expandRange(IPOGT.lastSlice);
                if(InfoStatus==0) continue;
                IPOGT.importLevelInfo(bf,versionNumber);
                IJ.showStatus("Imported track"+IPOGT.TrackIndex);
                if(i==40)
                    i=i;
            }
            status=1;
            sliceI=ir.getMin();
            sliceF=ir.getMax();
            sI=IOAssist.readInt(bf);
            if(sI==ir.getMin()){
                sF=IOAssist.readInt(bf);
                n=sF-sI+1;
                m=IOAssist.readInt(bf);
                ppdHeightCutoff=new double[n][m];
                pdHeightCutoffPs=IOAssist.readDoubleArray(bf,m);
                for(i=0;i<n;i++){
                    pt=IOAssist.readDoubleArray(bf,m); 
                    for(j=0;j<m;j++){
                        ppdHeightCutoff[i][j]=pt[j];
                    }
                }
            }else{
                ppdHeightCutoff=null;//ppdHeightCutoff!=null will be interpreted as successful read in
            }
            bf.close();
            fs.close();
        }
        catch (IOException e){
            if(status==-1) return -1;
        }
        checkTrackindexes();
        setSelectedIPOGTracks(m_cvIPOGTracks);
        buildIPOGTBundles();
        calParRanges();
        checkTrackindexes();
        assignTrackIndexesInBundles();
        checkTrackindexes();
        return 1;
    }
    public int importIPOGTracks(String path){
        IJ.showStatus("importing levelinfo: "+path);
        File f=new File(path);
        FileInputStream fs=null;
        try{fs=new FileInputStream(f);}
        catch (FileNotFoundException e){
            return -1;
        }
        BufferedInputStream bf=new BufferedInputStream(fs);
        byte bt[]=new byte[4];
        IPOGTrackNode IPOGT;
        int nNumAddParsPerIPO;
        m_cvIPOGTracks.clear();
        int status=0,i,j,n,m,sI,sF;
        double[] pt;
        intRange ir=new intRange();
        
        try{
            bf.read(bt);
            nNumTracks=ByteConverter.toInt(bt);
            bf.read(bt);
            nNumAddParsPerIPO=ByteConverter.toInt(bt);
            for(i=0;i<nNumTracks;i++){
                IPOGT=new IPOGTrackNode();
                IPOGT.importIPOGTrack(bf,nNumAddParsPerIPO);
                 m_cvIPOGTracks.add(IPOGT);
                 ir.expandRange(IPOGT.firstSlice);
                 ir.expandRange(IPOGT.lastSlice);
            }
            status=1;
            sliceI=ir.getMin();
            sliceF=ir.getMax();
            sI=IOAssist.readInt(bf);
            if(sI==ir.getMin()){
                sF=IOAssist.readInt(bf);
                n=sF-sI+1;
                m=IOAssist.readInt(bf);
                ppdHeightCutoff=new double[n][m];
                pdHeightCutoffPs=IOAssist.readDoubleArray(bf,m);
                for(i=0;i<n;i++){
                    pt=IOAssist.readDoubleArray(bf,m); 
                    for(j=0;j<m;j++){
                        ppdHeightCutoff[i][j]=pt[j];
                    }
                }
            }else{
                ppdHeightCutoff=null;
            }
            bf.close();
            fs.close();
        }
        catch (IOException e){
            if(status==0) 
                return -1;
        }
        checkTrackindexes();
        setSelectedIPOGTracks(m_cvIPOGTracks);
        buildIPOGTBundles();
        calParRanges();
        assignTrackIndexesInBundles();
        return 1;
    }
    public int refitLevelInfoNodes(String QuantityName){
        IPOGTrackNode IPOGT;
        IPOGTLevelInfoNode aInfoNode;
        
        for(int i=0;i<nNumTracks;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            IPOGT.activateLevelInfo(QuantityName);
            aInfoNode=IPOGT.getLevelInfo();
            if(aInfoNode==null) continue;
            IPOGTLevelTransitionAnalyzer.detectTransitions(aInfoNode);
        }
        return 1;
    }
    public int exportTracksLevelInfo(String path){        
        path=FileAssist.changeExt(path, "TLH");
        IJ.showStatus("exporting levelinfo: "+path);
        File f=new File(path);
        FileOutputStream fs=null;
        try{fs=new FileOutputStream(f);}
        catch (FileNotFoundException e){
            return -1;
        }
        int InfoStatus=1;
        DataOutputStream ds=new DataOutputStream(fs);
        IPOGTrackNode IPOGT;
        try{
            ds.writeInt(sImagePath.length());
            ds.writeChars(sImagePath);
            ds.writeInt(nNumTracks);
            ds.writeInt(IPOGaussianNode.nNumAdditionalPars);
            
            for(int i=0;i<nNumTracks;i++){
                IPOGT=m_cvIPOGTracks.get(i);
                IPOGT.removeUnfittedLevelInfoNodes();
                IJ.showStatus("Exporting Track"+IPOGT.TrackIndex);
                IPOGT.exportIPOGTrack(ds);
                if(IPOGT.m_cLevelInfo==null)
                    InfoStatus=0;
                else
                    InfoStatus=1;
                ds.writeInt(InfoStatus);
                if(InfoStatus==0) continue;
                IPOGT.exportLevelInfo(ds);
            }
            if(IPOAnalyzerForm.ppdHeightCutoff!=null){
                int i,j,m=IPOAnalyzerForm.pdHeightCutoffPs.length;
                ds.writeInt(sliceI);
                ds.writeInt(sliceF);
                ds.writeInt(m);
                for(j=0;j<m;j++){
                    ds.writeDouble(IPOAnalyzerForm.pdHeightCutoffPs[j]);                    
                }
                for(i=sliceI;i<=sliceF;i++){
                    for(j=0;j<m;j++){
                        ds.writeDouble(IPOAnalyzerForm.ppdHeightCutoff[i-sliceI][j]);                    
                    }
                }
            }
            ds.close();
        }
        catch (IOException e){
            return -1;
        }
        return 1;
    }
    public int exportIPOGTracks(String path){
        File f=new File(path);
        FileOutputStream fs=null;
        try{fs=new FileOutputStream(f);}
        catch (FileNotFoundException e){
            return -1;
        }
        DataOutputStream ds=new DataOutputStream(fs);
        byte bt[]=new byte[4];
        IPOGTrackNode IPOGT;
        try{
            ds.writeInt(sImagePath.length());
            ds.writeChars(sImagePath);
            ds.writeInt(nNumTracks);
            ds.writeInt(IPOGaussianNode.nNumAdditionalPars);
            for(int i=0;i<nNumTracks;i++){
                IPOGT=m_cvIPOGTracks.get(i);
                IPOGT.exportIPOGTrack(ds);
            }
            ds.close();
            fs.close();

        }
        catch (IOException e){
            return -1;
        }
        return 1;
    }
    public void calSliceRange(){
        intRange ir=new intRange();
        int i,si,sf,len;
        ArrayList<IPOGaussianNode> IPOGs;
        for(i=0;i<nNumTracks;i++){
            IPOGs=m_cvIPOGTracks.get(i).m_cvIPOGs;
            if(m_cvIPOGTracks.get(i).TrackIndex==58){
                i=i;
            }
            len=IPOGs.size();
            si=IPOGs.get(0).sliceIndex;
            sf=IPOGs.get(len-1).sliceIndex;
            ir.expandRange(si);
            ir.expandRange(sf);
        }
        sliceI=ir.getMin();
        sliceF=ir.getMax();
        IPOAnalyzerForm.setLastSlice(sliceF);
    }

    public void updateComplexityRange(){
        cComplexityRange.resetRange();
        int complexity;
        int i,len=m_cvIPOGTracks.size(),nComplexity;
        IPOGTrackNode IPOGT;
        for(i=0;i<len;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            complexity=IPOGT.getComplexity();
            cComplexityRange.expandRange(complexity);
        }
    }
    public void calParRanges(){
        calParRanges("Peak1");
    }
    public void calParRanges(String plottingOption){
        cHeightRange.resetRange();
        cTrackLengthRange.resetRange();
        cTrackIndexRange.resetRange();
        cClusterSizeRange.resetRange();
        cBundleSizeRange.resetRange();
        cTotalSignalRange.resetRange();
        cBundleIndexRange.resetRange();
        cIPOAreaRange.resetRange();
        cHWRatioRange.resetRange();
        cFirstSliceIndexRange.resetRange();
        cLastSliceIndexRange.resetRange();
        cTotalDriftRange.resetRange();
        cStepDriftRange.resetRange();
        int i,len=m_cvIPOGTracks.size(),nComplexity;
        IPOGTrackNode IPOGT;
        for(i=0;i<len;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            cHeightRange.expandRange(IPOGT.cHeightRange);
            cTrackLengthRange.expandRange(IPOGT.m_cvIPOGs.size());
            cFirstSliceIndexRange.expandRange(IPOGT.m_cvIPOGs.get(0).sliceIndex);
            cClusterSizeRange.expandRange(IPOGT.cClusterSizeRange);
            cBundleSizeRange.expandRange(IPOGT.BundleSize);
            cTrackIndexRange.expandRange(IPOGT.TrackIndex);
            cBundleIndexRange.expandRange(IPOGT.BundleIndex);
            cIPOAreaRange.expandRange(IPOGT.cAreaRange);
            cHWRatioRange.expandRange(IPOGT.cHeightWidthRatioRange);
            cTotalSignalRange.expandRange(IPOGT.cTotalSignalRange);
            cTotalDriftRange.expandRange(Math.sqrt(IPOGT.cXCRange.getRange()*IPOGT.cXCRange.getRange()+IPOGT.cYCRange.getRange()*IPOGT.cYCRange.getRange()));
            cStepDriftRange.expandRange(IPOGT.cDriftRange);
            cLastSliceIndexRange.expandRange(IPOGT.lastSlice);
            cHeadValueRange.expandRange(IPOGT.getHeadValue(plottingOption));
        }
        sliceI=cFirstSliceIndexRange.getMin();
        sliceF=cFirstSliceIndexRange.getMax();
    }

    public void exportTracksToAbf_GaussianNodeGroup(String path, ArrayList<IPOGTrackNode> IPOGTs){
        int len=IPOGTs.size(),i;
        IPOGTrackNode IPOGT;

        IPOGT=IPOGTs.get(0);
        m_nAbfChannels=8;

        int nSelectedTracks=IPOGTs.size();
        int trackAbfLength=getTrackAbfLength(IPOGT)*nSelectedTracks;
        float[] pfData=new float[trackAbfLength];
        IPOGTrackNode IPOT;
        int position=0;
        for(i=0;i<len;i++){
            IPOT=IPOGTs.get(i);
            position=exportTrackToAbf(pfData,position, IPOGT);
        }

        Abf cAbf=new Abf();
        cAbf.loadHeader(m_nAbfChannels);
        IPObjectTrack.setADCChannelNamesAndUnits(cAbf.getADCChannelNames(), cAbf.getADCChannelUnits());
        cAbf.exportAsAbf(pfData, m_nAbfChannels, trackAbfLength/m_nAbfChannels, path);
    }
    public int exportTrackToAbf(float pfData[], int positionI,IPOGTrackNode IPOGT){
        int position=positionI;
        ArrayList<IPOGaussianNode> IPOGs=IPOGT.m_cvIPOGs;
        int num=IPOGs.size();
        int i;
        int cx0,cy0;
        IPOGaussianNode IPOG,IPOG0;
        IPOG0=IPOGs.get(0);
        cx0=IPOG0.xcr;
        cy0=IPOG0.ycr;
        position=exportSpacerToAbf(pfData,position,IPOGT);
        position=exportDefaultIPOTraceToAbf(pfData,position,IPOG0.xcr,IPOG0.ycr,sliceI,IPOG0.sliceIndex-2-sliceI,m_nAbfChannels,IPOG0.TrackIndex);
        position=exportIPOTMarkToAbf(pfData,position,IPOG0,IPOG0.sliceIndex-1,m_nAbfChannels);
        for(i=0;i<num;i++){
           IPOG=IPOGs.get(i);
           position=exportDefaultIPOTraceToAbf(pfData,position,IPOG0.xcr,IPOG0.ycr,IPOG0.sliceIndex+1-sliceI,IPOG.sliceIndex-sliceI,m_nAbfChannels,IPOG0.TrackIndex);
           position=exportIPOGToAbf(pfData,position,IPOG,cx0,cy0);
           IPOG0=IPOG;
        }
        if(IPOG0.sliceIndex<sliceF) position=exportIPOTMarkToAbf(pfData,position,IPOG0,IPOG0.sliceIndex+1-sliceI,m_nAbfChannels);

        position=exportDefaultIPOTraceToAbf(pfData,position,IPOG0.xcr,IPOG0.ycr,IPOG0.sliceIndex+2-sliceI,sliceF,m_nAbfChannels,IPOG0.TrackIndex);
        return position;
    }

    int exportIPOTMarkToAbf(float[]pfData,int positionI, IPOGaussianNode IPOG, int z, int nAbfChannels){
        int position=positionI,i;
        float fV=-100;

/*        if(m_nTrackExportMode==0)
            fV=-1000;
        else
            fV=-100;
*/
        pfData[position]=fV;
        position++;
        pfData[position]=fV;
        position++;
        pfData[position]=fV;
        position++;
        pfData[position]=fV;
        position++;
        pfData[position]=IPOG.xcr;
        position++;
        pfData[position]=IPOG.ycr;
        position++;
        pfData[position]=IPOG.sliceIndex;
        position++;
        for(i=8;i<=nAbfChannels;i++){
            pfData[position]=fV;
            position++;
        }
        return position;
    }

    int exportSpacerToAbf(float[] pfData, int positionI, IPOGTrackNode IPOGT){
        //numChannels the number of channels in Abf, indexT the track Index,
        //positionI the first position in pfData
        ArrayList<IPOGaussianNode> IPOGs=IPOGT.m_cvIPOGs;
        int position=positionI;
        IPOGaussianNode IPOGi,IPOGf;
        IPOGi=IPOGs.get(0);
        int size=IPOGs.size();
        IPOGf=IPOGs.get(size-1);

        int len=10;
        float fV=-100;
/*        if(m_nTrackExportMode==0){
            fV=-1000;
        }else{
            fV=-100;
        }*/
        int i,j;
        for(i=0;i<len;i++){
            pfData[position]=fV;
            position++;
            pfData[position]=IPOGT.TrackIndex;
            position++;
            for(j=2;j<m_nAbfChannels;j++){
                pfData[position]=0;
                position++;
            }
        }//export the track index, using 10*numChannels points. export to the second and third channels. indexT=Y2*100+Y3

        for(i=0;i<len;i++){
            pfData[position]=0;
            position++;
            pfData[position]=IPOGi.xcr;
            position++;
            pfData[position]=IPOGi.ycr;
            position++;
            pfData[position]=IPOGi.sliceIndex;
            position++;
            pfData[position]=(int)(IPOGi.peak1+0.5);
            position++;
            for(j=5;j<m_nAbfChannels;j++){
                pfData[position]=0;
                position++;
            }
        }//export the sliceIndex of the first IPO, using 10*numChannels points. export to the second and third channels. z=Y2*100+Y3
        //export the x and y of the firt IPO to the fourth and fifth channels.
        for(i=0;i<len;i++){
            pfData[position]=0;
            position++;
            pfData[position]=IPOGf.xcr;
            position++;
            pfData[position]=IPOGf.ycr;
            position++;
            pfData[position]=IPOGf.sliceIndex;
            position++;
            pfData[position]=(int)(IPOGf.peak1+0.5);
            position++;
            for(j=5;j<m_nAbfChannels;j++){
                pfData[position]=0;
                position++;
            }
        }
        return position;
    }

    int exportIPOGToAbf(float[]pfData,int positionI, IPOGaussianNode IPOG, int cx0, int cy0){
        int position=positionI,i;
        pfData[position]=(float)IPOG.Amp;
        position++;
        pfData[position]=(float)IPOG.peak1-1000;
        position++;
        pfData[position]=(float)(IPOG.dTotalSignal);
        position++;
        pfData[position]=(float)IPOG.area;
        position++;
        pfData[position]=(float)IPOG.preOvlp;
        position++;
        pfData[position]=IPOG.xcr-cx0;
        position++;
        pfData[position]=IPOG.ycr-cy0;
        position++;
        int slice=IPOG.sliceIndex;
        pfData[position]=slice;
        position++;
        pfData[position]=(float)IPOG.getNumIPOGs();
        position++;
        for(i=11;i<m_nAbfChannels;i++){
            pfData[position]=0;
            position++;
        }
        return position;
    }
    int getSpacerLength(IPOGTrackNode IPOGT){//assuming spacer length is not larger than 2000
        int position=0;
        float[] pfData=new float[2000];
        position=exportSpacerToAbf(pfData,position,IPOGT);
        return position/m_nAbfChannels;
    }

    int getTrackAbfLength(IPOGTrackNode IPOGT){//number of data points to write to pfData of an abf file
        return m_nAbfChannels*(sliceF-sliceI+1+getSpacerLength(IPOGT));
    }
    public int exportDefaultIPOTraceToAbf(float []pfData,int positionI, int x, int y, int Ii, int If,int numChannels,int TrackIndex){
        int i,position=positionI;
        ImageShape cIS=new CircleImage(1);
        cIS.setCenter(new Point(x,y));
        for(i=Ii;i<=If;i++){
            pfData[position]=(float)TrackIndex;
            position++;
            pfData[position]=(float)ImageShapeHandler.getMean(pixels[i], cIS);
            position++;
        }
        return position;
    }
    public void buildIPOGTBundles(){
        int i,len=m_cvIPOGTracks.size(),BId,len1,j;
        IPOGTrackNode IPOGT;
        IPOGTrackBundleNode IPOGTB;
        for(i=0;i<len;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            if(IPOGT.TrackIndex==104){
                i=i;
            }
            BId=IPOGT.BundleIndex;
            len1=m_cvIPOGTBs.size();
            for(j=len1;j<=BId;j++){
                m_cvIPOGTBs.add(new IPOGTrackBundleNode());
            }
            if(BId<0) continue;
            IPOGTB=m_cvIPOGTBs.get(BId);
            IPOGTB.addTrack(IPOGT);
        }

        int nBundles=m_cvIPOGTBs.size();
        for(i=0;i<nBundles;i++){
            m_cvIPOGTBs.get(i).finalizeBundle();
        }
    }
    public ArrayList<IPOGTrackNode> getBundlesAsTracks(){
        ArrayList<IPOGTrackNode> IPOGTst=new ArrayList();
        int i,len=m_cvIPOGTBs.size();
        if(len==0){
            buildIPOGTBundles();
            len=m_cvIPOGTBs.size();
        }
        for(i=0;i<len;i++){
            IPOGTst.add(m_cvIPOGTBs.get(i));
        }
        return IPOGTst;
    }
    public boolean validTrackIndex(int id){
        if(id<0)return false;
        if(id>=m_cvIPOGTracks.size()) {
            if(id>=IPOGTrackBundleNode.minTrackIndex&&id<IPOGTrackBundleNode.minTrackIndex+m_cvIPOGTBs.size())
                return true;
            return false;
        }
        return true;
    }
    public double[] getStackMinima(String key){
        if(m_cStackMinima==null) m_cStackMinima=new Hashtable();
        if(m_cStackMinima.containsKey(key)) return (double[])m_cStackMinima.get(key);
        buildStackMins(key);
        return getStackMinima(key);
    }
    public int[] getStackMinIndexes(String key){
        if(m_cStackMinima==null) m_cStackMinima=new Hashtable();
        if(m_cStackMinima.containsKey(key)) return (int[])m_cStackMinima.get(key+"_Index");
        buildStackMins(key);
        return getStackMinIndexes(key);
    }
    void buildStackMins(String key){
        int i,index,slice,len=sliceF-sliceI+1,sI,sF,minIndex=0;
        double[] pdv=new double[len];
        int[] pnMinIndexes=new int[len];
        CommonStatisticsMethods.setElements(pdv, Double.POSITIVE_INFINITY);
        IPOGaussianNode IPOG;
        int num=m_cvIPOGTracks.size();
        IPOGTrackNode IPOGT;
        double y;
        for(i=0;i<num;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            if(IPOGT.TrackIndex==114){
                i=i;
            }
            sI=IPOGT.firstSlice;
            sF=IPOGT.lastSlice;
            for(slice=sI;slice<=sF;slice++){
                IPOG=IPOGT.getIPOG(slice);
                if(IPOG==null) continue;
                if(!IPOGaussianNodeHandler.isNormalShape((IPOGaussianNodeComplex) IPOG)) 
                    continue;
                y=IPOG.getValue(key);
                index=slice-sliceI;
                if(index>=len){
                    index=index;
                    continue;
                }
                if(y<pdv[index]){
                    pdv[index]=y;
                    pnMinIndexes[index]=IPOGT.TrackIndex;
                }
            }
        }
        m_cStackMinima.put(key, pdv);
        m_cStackMinima.put(key+"_Index", pnMinIndexes);
    }
    IPOGTrackNode getIPOGT(int index){
        if(!validTrackIndex(index)) return null;
        if(index>=IPOGTrackBundleNode.minTrackIndex) return m_cvIPOGTBs.get(index-IPOGTrackBundleNode.minTrackIndex);
        return m_cvIPOGTracks.get(index);
    }
    void assignTrackIndexesInBundles(){
        int i,len=m_cvIPOGTBs.size(),j,len1,slice;
        IPOGTrackBundleNode IPOGTB;
        IPOGTrackNode IPOGT;
        IPOGaussianNode IPOG;
        ArrayList<Integer> trkIndexes;
        ArrayList<IPOGTrackNode> IPOGTs;
        for(i=0;i<len;i++){
            IPOGTB=m_cvIPOGTBs.get(i);
            IPOGTs=IPOGTB.IPOGTs;
            len1=IPOGTs.size();           
            for(slice=IPOGTB.firstSlice;slice<=IPOGTB.lastSlice;slice++){
                trkIndexes=new ArrayList();
                for(j=0;j<len1;j++){
                    IPOGT=IPOGTs.get(j);
                    IPOG=IPOGT.getIPOG(slice);
                    if(IPOG!=null){
                        trkIndexes.add(IPOG.TrackIndex);
                        IPOG.nvTrkIndexesInBundle=trkIndexes;
                    }
                }
            }
        }
    }
    public void fireActionEvent(ActionEvent ae){
        int i,len=m_cvActionListeners.size();
        for(i=0;i<len;i++){
            m_cvActionListeners.get(i).actionPerformed(ae);
        }
    }
    public void addActionListener(ActionListener al){
        m_cvActionListeners.add(al);
    }
    public int setFirstSlice(int sliceI){
        if(sliceI<=this.sliceI) return -1;
        this.sliceI=sliceI;
        int i,len=m_cvIPOGTracks.size();
        for(i=len-1;i>=0;i--){
            m_cvIPOGTracks.get(i).setFirstSlice(sliceI);
            if(m_cvIPOGTracks.get(i).m_cvIPOGs.isEmpty()) m_cvIPOGTracks.remove(i);
        }
        nNumTracks=m_cvIPOGTracks.size();
        return 1;
    }
    void checkTrackindexes(){
        int i,len=m_cvIPOGTracks.size();
        for(i=0;i<len;i++){
            if(m_cvIPOGTracks.get(i).TrackIndex!=i){
                continue;
            }
            i=i;
        }
    }
    public int resortTracks(){
        return 1;
    }
    public int selectTracks_Deltault(){
        calParRanges();
        m_cvSelectedIPOGTracks.clear();
        int i,len=m_cvIPOGTracks.size();
        len=len;
        IPOGTrackNode IPOGT;
        intRange BR=new intRange(0,0);
        intRange FSR=new intRange(sliceI,sliceI);
        int nMinLen=30;
        for(i=0;i<len;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            if(!FSR.contains(IPOGT.firstSlice)) continue;
//            if(!BR.contains(IPOGT.BundleSize)) continue;
            if(IPOGT.getTrackLength()<nMinLen) continue;
            m_cvSelectedIPOGTracks.add(IPOGT);
        }
        return 1;
    }
    void stampIPOs(int slice, int[][] IPOStamp, double[][] pdHeights){
        int i,j,len,x,y,h=IPOStamp.length,w=IPOStamp[0].length,nTrackIndex;
        IPOGaussianNode IPOG;
        IPOGTrackNode IPOGT;
        ArrayList<Point> innerPoints=new ArrayList();
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        CommonStatisticsMethods.setElements(IPOStamp, -1);
        CommonStatisticsMethods.setElements(pdHeights, -1);
        CircleImage circle=new CircleImage(3);
        circle.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        
        Point p;
        double dt;
        for(i=0;i<nNumTracks;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            nTrackIndex=IPOGT.TrackIndex;
            if(slice<IPOGT.firstSlice||slice>IPOGT.lastSlice) continue;
            IPOG=IPOGT.getIPOG(slice);
            if(IPOG==null) continue;
            IPOGs.clear();
            IPOG.getSimpleIPOGs(IPOGs);
            if(IPOG==null) continue;
            
            circle.setCenter(IPOG.getCenter());
//            innerPoints=IPOGaussianNodeHandler.getIS2(IPOGs,w,h);
            circle.getInnerPoints(innerPoints);
            len=innerPoints.size();
            for(j=0;j<len;j++){
               p=innerPoints.get(j); 
               x=p.x;
               y=p.y;
               dt=IPOG.getAmpAt(x, y);
               if(dt>pdHeights[y][x]){
                   pdHeights[y][x]=dt;
                   IPOStamp[y][x]=nTrackIndex;
               }
            }
        }
    }
    void copySelectedTracks(ArrayList<IPOGTrackNode> IPOGTs){
        IPOGTs.clear();
        for(int i=0;i<m_cvSelectedIPOGTracks.size();i++){
            IPOGTs.add(m_cvSelectedIPOGTracks.get(i));
        }
    }
    public IPOGTLevelInfoCollectionNode getIPOGTLevelInfoCollection(String sQuantityName){
        ArrayList<IPOGTLevelInfoNode> cvInfoNodes=new ArrayList();
        IPOGTrackNode IPOGT;
        getLevelInfoNodes(cvInfoNodes,sQuantityName);
        IPOGTLevelInfoCollectionNode aNode=new IPOGTLevelInfoCollectionNode(cvInfoNodes);
        return aNode;
    }
    public void getLevelInfoNodes(ArrayList<IPOGTLevelInfoNode> cvInfoNodes,String sQuantityName){
        cvInfoNodes.clear();
        IPOGTrackNode IPOGT;
        for(int i=0;i<nNumTracks;i++){
            IPOGT=m_cvIPOGTracks.get(i);
            IPOGT.activateLevelInfo(sQuantityName);
            if(IPOGT.m_cLevelInfo!=null) cvInfoNodes.add(IPOGT.m_cLevelInfo);
        }
    }
}
