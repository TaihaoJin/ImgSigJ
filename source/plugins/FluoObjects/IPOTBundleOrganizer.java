/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.CommonMethods;
import java.awt.Color;
import ij.ImagePlus;
import utilities.ArrayofArrays.IntArray;
import utilities.AbfHandler.Abf;
import utilities.io.FileAssist;
import utilities.io.PrintAssist;
import FluoObjects.IntensityPeakObjectHandler;
import utilities.Geometry.ImageShapes.*;
/**
 *
 * @author Taihao
 */
public class IPOTBundleOrganizer {
    ArrayList<IPOTBundle> IPOTBs;
    ArrayList<IPObjectTrack> IPOTs;
    int m_nMaxBundleSize;
    ArrayList<IntArray> m_vcBundleIndexes;
    ArrayList<IntensityPeakObjectHandler> m_cvIPOHs;
    int zI,zF;
    public IPOTBundleOrganizer (ArrayList<IPObjectTrack> IPOTs,int zI,int zF){
        this.zI=zI;
        this.zF=zF;
        this.IPOTs=IPOTs;
        IPOTBs=new ArrayList();
        buildIPOTBundles_ImageShapeOverlap();
        finalizeBundleInfo();
    }
    public ArrayList<IPOTBundle> getIPOTBs(){
        return IPOTBs;
    }
    public void markIPOTBs(ImagePlus impl, int minPeakPercentageIndex){
        int len=IPOTBs.size();
        Color cb;
        for(int i=0;i<len;i++){
            cb=CommonMethods.randomColor();
            IPOTBs.get(i).markIPOTB(impl,cb,minPeakPercentageIndex);
        }
    }
    void buildIPOTBundles_ImageShapeOverlap(){
        int numTracks=IPOTs.size();
        int bundleIndexes[]=new int[numTracks];
        int i,j,len;
        for(i=0;i<numTracks;i++){
            bundleIndexes[i]=-1;
        }
        IntensityPeakObject IPO,IPO1;
        IPObjectTrack IPOT,IPOTt;
        int tIndex;
        int bIndex=0;
//        IPOTBundle IPOTB=null,IPOTBt;
        for(i=0;i<numTracks;i++){
           IPOT=IPOTs.get(i);
           len=IPOT.IPOs.size();
           IPO=IPOT.IPOs.get(0);

           if(IPO.TrackIndex==114){
               IPO=IPO;
           }
           IPO1=getOverlappingIPO(IPO,-1);

           if(IPO1!=null){
               tIndex=IPO1.TrackIndex;
               if(tIndex<0){
                   continue;
               }
               IPOTt=IPOTs.get(tIndex);
               bIndex=joinIPOTToBundle(IPOT,IPOTt,bIndex);
           }

           IPO=IPOT.IPOs.get(len-1);
           IPO1=getOverlappingIPO(IPO,1);
           if(IPO1!=null){
               tIndex=IPO1.TrackIndex;
               if(tIndex<0){
                   continue;
               }
               IPOTt=IPOTs.get(tIndex);
               bIndex=joinIPOTToBundle(IPOT,IPOTt,bIndex);
           }
        }
    }
    
    int joinIPOTToBundle(IPObjectTrack IPOT1, IPObjectTrack IPOT2, int bundleId){
        if(IPOT1.m_nTrackIndex==IPOT2.m_nTrackIndex) return bundleId;
        int bundleId1=IPOT1.m_nBundleIndex,bundleId2=IPOT2.m_nBundleIndex;
        IPOTBundle IPOTB;
        IPObjectTrack IPOTm=null, IPOTa=null;
        if(bundleId1==bundleId2){
            if(bundleId1!=-1) return bundleId;
            IPOTB=new IPOTBundle();
            IPOTB.m_nBundleIndex=bundleId;
            IPOTB.addIPOT(IPOT1);
            IPOTB.addIPOT(IPOT2);
            IPOTBs.add(IPOTB);
            bundleId++;
            return bundleId;
        }
        if(bundleId1<bundleId2){
            if(bundleId1==-1){
                IPOTm=IPOT2;
                IPOTa=IPOT1;
            }else{
                IPOTm=IPOT1;
                IPOTa=IPOT2;
            }
        }else{
            if(bundleId2==-1){
                IPOTm=IPOT1;
                IPOTa=IPOT2;
            }else{
                IPOTm=IPOT2;
                IPOTa=IPOT1;
            }
        }
        int bundleIdm=IPOTm.m_nBundleIndex,bundleIda=IPOTa.m_nBundleIndex;

        IPOTB=IPOTBs.get(bundleIdm);
        if(bundleIda==-1)
            IPOTB.addIPOT(IPOTa);
        else
            IPOTB.mergeBundle(IPOTBs.get(bundleIda));

        return bundleId;
    }
    void buildIPOTBundles(){
        int numTracks=IPOTs.size();
        int bundleIndexes[]=new int[numTracks];
        ArrayList <IPObjectTrack> IPOTTA=new ArrayList();
        int i,j,len,len1;
        for(i=0;i<numTracks;i++){
            bundleIndexes[i]=-1;
        }
        IntensityPeakObject IPO,IPO1;
        ArrayList <IntensityPeakObject> preIPOs,postIPOs,currentIPOs;
        IPObjectTrack IPOT;
        int tIndex,tIndex1,len2,k;
        int bIndex=0;
        for(i=0;i<numTracks;i++){
            if(bundleIndexes[i]!=-1) continue;
            IPOTTA.clear();
            IPOTTA.add(IPOTs.get(i));
            len=IPOTTA.size();
            bundleIndexes[i]=bIndex;
            IPOTBundle IPOTB=new IPOTBundle();
            while(len>0){
                IPOT=IPOTTA.get(len-1);
                IPOTB.addIPOT(IPOT);
                IPOTTA.remove(len-1);
                len1=IPOT.getLength();
                for(j=0;j<len1;j++){
                    IPO=IPOT.getIPO(j);
                    preIPOs=IPO.preIPOs;
                    postIPOs=IPO.postIPOs;
                    currentIPOs=IPO.currentIPOs;
                    tIndex=IPO.TrackIndex;
                    if(bundleIndexes[tIndex]==-1) {//a new track in the bundle
                        bundleIndexes[tIndex]=bIndex;
                        IPOTTA.add(IPOTs.get(tIndex));
                    }
                    if(preIPOs!=null){
                        len2=preIPOs.size();
                        for(k=0;k<len2;k++){
                            tIndex1=preIPOs.get(k).TrackIndex;
                            if(tIndex1<0) continue;
                            if(bundleIndexes[tIndex1]==-1) {
                                bundleIndexes[tIndex1]=bIndex;
                                IPOTTA.add(IPOTs.get(tIndex1));
                            }
                        }
                    }
                    if(postIPOs!=null){
                        len2=postIPOs.size();
                        for(k=0;k<len2;k++){
                            tIndex1=postIPOs.get(k).TrackIndex;
                            if(tIndex1<0) continue;
                            if(tIndex1>=numTracks){
                                k=k;
                            }
                            if(bundleIndexes[tIndex1]==-1) {
                                bundleIndexes[tIndex1]=bIndex;
                                IPOTTA.add(IPOTs.get(tIndex1));
                            }
                        }
                    }
                    if(currentIPOs!=null){
                        len2=currentIPOs.size();
                        for(k=0;k<len2;k++){
                            tIndex1=currentIPOs.get(k).TrackIndex;
                            if(tIndex1<0) continue;
                            if(bundleIndexes[tIndex1]==-1) {
                                bundleIndexes[tIndex1]=bIndex;
                                IPOTTA.add(IPOTs.get(tIndex1));
                            }
                        }
                    }
                }
                len=IPOTTA.size();
            }
            bIndex++;
            IPOTBs.add(IPOTB);
        }
    }
    void arrangeBundleIndexes(){
        int nx=getMaxBundleSize();
        m_vcBundleIndexes=new ArrayList();
        int i,it;
        for(i=0;i<nx+1;i++){
            m_vcBundleIndexes.add(new IntArray());
        }
        int len=IPOTBs.size();
        for(i=0;i<len;i++){
            it=IPOTBs.get(i).getBundleSize();
            m_vcBundleIndexes.get(it).m_intArray.add(i);
        }
    }
    int getMaxBundleSize(){
        if(IPOTBs.isEmpty()) return 1;
        m_nMaxBundleSize=IPOTBs.get(0).getBundleSize();
        int len=IPOTBs.size();
        int size;
        for(int i=1;i<len;i++){
            size=IPOTBs.get(i).getBundleSize();
            if(size>m_nMaxBundleSize) m_nMaxBundleSize=size;
        }
        return m_nMaxBundleSize;
    }
    public void exportBundlesToAbf(String path0){
        String path;
        Abf cAbf;
        int len;
        int i,it,position;
        IPObjectTrack IPOT;
        IPOTBundle IPOTB;
        ArrayList <Integer> indexes;
        int AbfDataSize;
        float pfData[];
        for(i=1;i<m_nMaxBundleSize;i++){
            indexes=m_vcBundleIndexes.get(i).m_intArray;
            len=indexes.size();
            if(len==0) continue;
            IPOT=IPOTs.get(0);
            AbfDataSize=IPOT.getTrackAbfLengthChannel()*len*i;
            pfData=new float[AbfDataSize];
            position=0;
            for(it=0;it<len;it++){
                IPOTB=IPOTBs.get(indexes.get(it));
                position=IPOTB.exportBundleToAbf(pfData,position);
            }
            cAbf=new Abf();
            path=new String(path0);
            path=FileAssist.getExtendedFileName(path, " bundle size "+PrintAssist.ToString(i));
            cAbf.exportAsAbf(pfData, i, AbfDataSize/i, path);
            if(i>=16) break;
        }
    }
    void finalizeBundleInfo(){
        arrangeBundleIndexes();
        int len=IPOTBs.size();
        IPOTBundle IPOTB;
        for(int i=0;i<len;i++){
            IPOTB=IPOTBs.get(i);
            IPOTB.setBundleSizeAndIndexToTracks(i);
            IPOTB.updateSliceRangeAndTotalSignal(zI, zF);
        }
    }
    IntensityPeakObject getOverlappingIPO(IntensityPeakObject IPO, int sideness){//now based on the contour shapes
        ArrayList<IntensityPeakObject> IPOs=IPO.preIPOs;
        if(sideness>0) IPOs=IPO.postIPOs;
        IntensityPeakObject IPOt,IPOx=null;
        int i,len=IPOs.size();
        int ovlp=0;
        int ovlpx=0;
        ImageShape cIS;
        for(i=0;i<len;i++){
            IPOt=IPOs.get(i);
            cIS=new ImageShape(IPOt.cContourShape);
            cIS.overlapShape(IPO.cContourShape);
            ovlp=cIS.getArea();
            if(ovlp>ovlpx) {
                ovlpx=ovlp;
                IPOx=IPOt;
            }
        }
        
        if(IPOx==null) return null;

        if(sideness>0){
            IPO.postOvlp=ovlpx;
            IPO.postRid=IPOx.rIndex;
        } else {
            IPO.preOvlp=ovlpx;
            IPO.preRid=IPOx.rIndex;
        }
        IPO.updateOvlpForIPOGs();
        if(ovlp>IPO.cContourShape.getArea()/2) return IPOx;
        return null;
    }
}
