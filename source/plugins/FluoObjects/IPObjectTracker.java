/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import FluoObjects.IPObjectTrack;
import FluoObjects.IPOArray;
import FluoObjects.IntensityPeakObject;
import FluoObjects.IntensityPeakObjectHandler;
import FluoObjects.IPObjectMatcher;
import java.util.ArrayList;
import ij.IJ;
import utilities.QuickSortInteger;
import java.awt.Point;

/**
 *
 * @author Taihao
 */
public class IPObjectTracker {
    ArrayList <IntensityPeakObjectHandler> m_vcIPOHs;
    ArrayList <IPObjectTrack> m_vcIPOTs;
    ArrayList <Integer> m_nvLinkedTrackIndexes;
    public static final int Distance=1, AmpDifference=2,ImageShapeOverlap=3;
    float m_fCutoffDist;
    int m_nFirstSlice;//m_nFirstSlice is the slice number of the first image slice in the stack for tracking, and should
    //be distinguised from the first IPOH index in m_vcIPOHs for tracking
    int m_nFirstIPOHIndex;
    int m_nCostFunctionOption;
    public IPObjectTracker(ArrayList<IntensityPeakObjectHandler> IPOHs,
            float fCutoffDist,int firstSlice, int nCostFunctionOption){
        m_nCostFunctionOption=nCostFunctionOption;
        m_vcIPOHs=IPOHs;
        m_vcIPOTs=new ArrayList();
        m_fCutoffDist=fCutoffDist;
        m_nFirstSlice=firstSlice;
        m_nFirstIPOHIndex=m_nFirstSlice-IPOHs.get(0).z-1;//the slice number starts from 1
        m_nvLinkedTrackIndexes=new ArrayList();
        buildIPOTracks();
        ArrayList<Point> IPOTPairs=IntensityPeakObjectOrganizer.checkIPOTDuplicacy(m_vcIPOTs);
        checkUntrackedIPOs();
/*        linkIPOTracks();
        ArrayList<Point> IPOTPairs1=IntensityPeakObjectOrganizer.checkIPOTDuplicacy(m_vcIPOTs);
        removeLinkedTracks();
        ArrayList<Point> IPOTPairs2=IntensityPeakObjectOrganizer.checkIPOTDuplicacy(m_vcIPOTs);
        adjustTrackIndexes();
        ArrayList<Point> IPOTPairs3=IntensityPeakObjectOrganizer.checkIPOTDuplicacy(m_vcIPOTs);
        checkUntrackedIPOs();
        ArrayList<Point> IPOTPairs4=IntensityPeakObjectOrganizer.checkIPOTDuplicacy(m_vcIPOTs);*/
    }
    public ArrayList <IPObjectTrack> getIPOTracks(){
        return m_vcIPOTs;
    }

    void buildIPOTracks(){
        m_vcIPOTs=new ArrayList<IPObjectTrack>();
        int len=m_vcIPOHs.size();
        int i,j;
        ArrayList<IntensityPeakObject> IPOs0,IPOs1,UnconnectedIPOs;
        assignTracks(m_nFirstIPOHIndex);
//        makeUnambiguousConnections(m_nFirstIPOHIndex);
        UnconnectedIPOs=getUnconnectedIPOs(m_nFirstIPOHIndex,1);
//        IPOs0=getConnectableIPOs(UnconnectedIPOs,1);
        IPOs0=UnconnectedIPOs;
        for(i=m_nFirstIPOHIndex+1;i<len;i++){
            UnconnectedIPOs=getUnconnectedIPOs(i,-1);
//            IPOs1=getConnectableIPOs(UnconnectedIPOs,-1);
            IPOs1=UnconnectedIPOs;
            if(i==29) {
                i=i;
            }
            matchIPOs(IPOs0,IPOs1,m_fCutoffDist,m_nCostFunctionOption);
            assignTracks(i);
//            makeUnambiguousConnections(i);
            UnconnectedIPOs=getUnconnectedIPOs_track(i,1);
//            IPOs0=getConnectableIPOs(UnconnectedIPOs,1);
            IPOs0=UnconnectedIPOs;
        }
    }
    void assignTracks(int index){
        assignTracks(m_vcIPOHs.get(index).IPOs);
    }

    void makeUnambiguousConnections(int index){
        IntensityPeakObjectHandler IPOH=m_vcIPOHs.get(index);
        ArrayList<IntensityPeakObject> IPOs=IPOH.getIPOs();
        IntensityPeakObject IPO0,IPO1;
        int len=IPOs.size();
        for(int i=0;i<len;i++){
            IPO0=IPOs.get(i);
            if(IPO0.cx==75&&IPO0.cy==253&&IPO0.cz==43){
                i=i;
            }
            if(unAmbiguouslyConnectable(IPO0,1)){
                IPO1=IPO0.postIPOs.get(0);
                IPO0.postIPO=IPO1;
                IPO1.preIPO=IPO0;
            }
        }
    }
    
    void matchIPOs(ArrayList <IntensityPeakObject> IPOs0,
            ArrayList <IntensityPeakObject> IPOs1, float fCutoffDist, int nCostFunctionOption){
        IPObjectMatcher aMatcher=new IPObjectMatcher();
        aMatcher.matchIPOs(IPOs0,IPOs1,fCutoffDist,nCostFunctionOption);
    }
    
    void buildUnambiguousTracks(){//not used
        IntensityPeakObjectHandler IPOH;
        IntensityPeakObject IPO,IPO0;
        ArrayList<Integer> indexes;
        int n=m_vcIPOHs.size();
        int i,j,size1,index;
        boolean unAmbiguous=false;
        int nTrackIndex=m_vcIPOTs.size();
        for(i=m_nFirstIPOHIndex;i<n;i++){
            IPOH=m_vcIPOHs.get(i);
            size1=IPOH.getIPOs().size();
            for(j=0;j<size1;j++){
                IPO=IPOH.getIPOs().get(i);
                if(IPO.cx==199&&IPO.cy==15&&IPO.cz>=442){
                    i=i;
                }
                if(IPO.preIPO!=null){
                    continue;//IPO has been already assigned
                }
                unAmbiguous=unAmbiguouslyConnectable(IPO,1);
//                if(!unAmbiguous) continue;
                //starting a new track
                ArrayList<IntensityPeakObject> IPOst=
                        new ArrayList<IntensityPeakObject>();

                while(unAmbiguous){
                    IPOst.add(IPO);
                    IPO0=IPO;
                    IPO=IPO.postIPOs.get(0);
                    IPO0.postIPO=IPO;
                    IPO.preIPO=IPO0;
                    unAmbiguous=unAmbiguouslyConnectable(IPO,1);
                }
//                if(IPO.postIPOs.size()==0) IPOst.add(IPO);
                IPOst.add(IPO);//IPO is unambiguously connectable with the 
                //its preIPO, because it has passed the previous
                //iteration in the above while loop
                IPO.postIPO=null;
                IPObjectTrack track=new IPObjectTrack(IPOst,nTrackIndex);
                m_vcIPOTs.add(track);
                nTrackIndex++;
            }
        }
    }
    
    boolean unAmbiguouslyConnectable(IntensityPeakObject IPO,
            int direction){
        //direction 1 for forward connection, -1 for backward connection
        IntensityPeakObject IPO1,IPO2;
        ArrayList<IntensityPeakObject> IPOst=proximityIPOs(IPO,direction);
        if(IPOst.size()!=1) return false;
        IPO1=IPOst.get(0);
        IPOst=proximityIPOs(IPO1,-1*direction);
        if(IPOst.size()!=1) return false;
        IPO2=IPOst.get(0);
        if(IPO2!=IPO) return false;
        return true;
    }

    ArrayList<IntensityPeakObject> proximityIPOs(IntensityPeakObject IPO,
            int direction){
        if(direction==1) return IPO.postIPOs;
        if(direction==-1) return IPO.preIPOs;
        IJ.error("direction for the method " +
                "proximityIPOs must be '1' or '-1'");
        return null;
    }
    
    ArrayList<IntensityPeakObject> getConnectableIPOs(
            ArrayList<IntensityPeakObject> IPOs0, int direction){
        ArrayList<IntensityPeakObject> IPOs=new ArrayList(),IPOst;
        IntensityPeakObject IPO;
        int len=IPOs0.size(),nSize=0;
        for(int i=0;i<len;i++){
            IPO=IPOs0.get(i);
            IPOst=proximityIPOs(IPO,direction);
            if(IPOst.size()>0) IPOs.add(IPO);
        }
        return IPOs;
    }


    ArrayList<IntensityPeakObject> getUnconnectedIPOs(int index,
            int direction)
    {
        ArrayList<IntensityPeakObject> IPOs=new ArrayList(),IPOs0;
        IPOs0=m_vcIPOHs.get(index).getIPOs();
        IntensityPeakObject IPO;
        int len=IPOs0.size();
        for(int i=0;i<len;i++){
            IPO=IPOs0.get(i);
            if(connectedIPO(IPO,direction)==null) {
                IPOs.add(IPO);
            }
        }
        return IPOs;
    }

    ArrayList<IntensityPeakObject> getUnconnectedIPOs_track(//allow only the last IPOs of the existing tracks to entered the unconnected IPO list, thus
            //track only begins only from the first slice
            int index,int direction){
        ArrayList<IntensityPeakObject> IPOs=new ArrayList(),IPOs0;
        IntensityPeakObject IPO;
        int len=m_vcIPOTs.size(),len1;
        IPObjectTrack IPOT;
        int z0=m_vcIPOHs.get(index).z,z;

        for(int i=0;i<len;i++){
            IPO=m_vcIPOTs.get(i).getLastIPO();
                if(IPO.cx==75&&IPO.cy==253&&IPO.cz==43){
                    i=i;
                }
            if(z0-IPO.cz>IPO.trackingLength) continue;
            if(connectedIPO(IPO,direction)==null) {
                IPOs.add(IPO);
            }
        }
        return IPOs;
    }

    IntensityPeakObject connectedIPO(IntensityPeakObject IPO, int
            direction){
        if(direction==1) return IPO.postIPO;
        if(direction==-1) return IPO.preIPO;
        IJ.error("direction for the method connectedIPO must " +
                "be '1' or '-1'");
        return null;
    }

    void assignTracks(ArrayList<IntensityPeakObject> IPOs){
        int len=IPOs.size();
        int TrackIndex;
        IntensityPeakObject IPO,preIPO;
        IPObjectTrack IPOT,IPOT1;
        for(int i=0;i<len;i++){
            IPO=IPOs.get(i);
            preIPO=IPO.preIPO;
            if(preIPO==null){
                if(!eligibleTrackHead(IPO)) continue;
                IPOT=getNewIPOTrack(IPO);
                m_vcIPOTs.add(IPOT);
            }else{
                TrackIndex=preIPO.TrackIndex;
                if(TrackIndex<0||TrackIndex>=m_vcIPOTs.size()) continue;
                IPOT=m_vcIPOTs.get(TrackIndex);
                //this method assumes all IPO's
                //in the previous frame have been assigned to tracks.
                IPOT.addIPO(IPO);
                IPO.TrackIndex=TrackIndex;
            }
        }
    }
    IPObjectTrack getNewIPOTrack(IntensityPeakObject IPO){
        IPO.TrackIndex=m_vcIPOTs.size();
        ArrayList <IntensityPeakObject> IPOs=new ArrayList();
        IPOs.add(IPO);
        IPObjectTrack IPOT=new IPObjectTrack(IPOs,IPO.TrackIndex);
        IPOT.m_nTrackIndex=m_vcIPOTs.size();
        IPOT.setIPOHs(m_vcIPOHs);
        return IPOT;
    }

    void linkIPOTracks(){
        int len=m_vcIPOHs.size();
        int i,j;
        ArrayList<IntensityPeakObject> trackHeadIPOs, trackEndIPOs=new ArrayList();
        ArrayList<IntensityPeakObject> newTrackEndIPOs;

        for(i=m_nFirstIPOHIndex;i<len-1;i++){
            newTrackEndIPOs=getTrackEndIPOs(i);
            appendIPOs(trackEndIPOs,newTrackEndIPOs);
            trackHeadIPOs=getTrackHeadIPOs(i+1);
            matchIPOs(trackEndIPOs,trackHeadIPOs,m_fCutoffDist,m_nCostFunctionOption);
            linkTracks(trackEndIPOs);
        }
    }
    void appendIPOs(ArrayList <IntensityPeakObject> IPO1, ArrayList <IntensityPeakObject> IPO2){
        int len=IPO2.size();
        for(int i=0;i<len;i++){
            IPO1.add(IPO2.get(i));
        }
    }
    ArrayList<IntensityPeakObject> getTrackEndIPOs(int index){
        ArrayList<IntensityPeakObject> IPOs=new ArrayList(), IPOs0=m_vcIPOHs.get(index).getIPOs();
        int len=IPOs0.size();
        IntensityPeakObject IPO;
        for(int i=0;i<len;i++){
            IPO=IPOs0.get(i);
            if(IPO.TrackIndex<0) continue;
            if(IPO.postIPO==null) IPOs.add(IPO);
        }
        return IPOs;
    }
    ArrayList<IntensityPeakObject> getTrackHeadIPOs(int index){
        ArrayList<IntensityPeakObject> IPOs=new ArrayList(), IPOs0=m_vcIPOHs.get(index).getIPOs();
        int len=IPOs0.size();
        IntensityPeakObject IPO;
        for(int i=0;i<len;i++){
            IPO=IPOs0.get(i);
            if(IPO.TrackIndex<0) continue;
            if(IPO.preIPO==null) IPOs.add(IPO);
        }
        return IPOs;
    }
    void linkTracks(ArrayList<IntensityPeakObject> IPOs){
        int len=IPOs.size(),trackIndex,postTrackIndex,index;
        IntensityPeakObject IPO,postIPO;
        IPObjectTrack IPOT,postIPOT;
        for(int i=0;i<len;i++){
            index=len-1-i;
            IPO=IPOs.get(index);
            if(IPO.TrackIndex<0) {
                continue;//should not occur this case
            }
            postIPO=IPO.postIPO;
            if(postIPO!=null){
                postTrackIndex=postIPO.TrackIndex;
                m_nvLinkedTrackIndexes.add(postTrackIndex);
                postIPOT=m_vcIPOTs.get(postTrackIndex);
                trackIndex=IPO.getTrackIndex();
                IPOT=m_vcIPOTs.get(trackIndex);
                postIPOT.setTrackIndex(trackIndex);
                appendIPOs(IPOT.getIPOs(),postIPOT.getIPOs());
                IPOs.remove(index);
            }
        }
    }
    void removeLinkedTracks(){
        int len=m_nvLinkedTrackIndexes.size(),index,trackIndex;
        QuickSortInteger.quicksort(m_nvLinkedTrackIndexes);
        for(int i=0;i<len;i++){
            index=len-1-i;
            trackIndex=m_nvLinkedTrackIndexes.get(index);
            m_vcIPOTs.remove(trackIndex);
        }
    }
    void adjustTrackIndexes(){
        int len=m_vcIPOTs.size();
        for(int i=0;i<len;i++){
            m_vcIPOTs.get(i).setTrackIndex(i);
        }
    }
    void checkUntrackedIPOs(){
        int len=m_vcIPOHs.size(),i,j;
        ArrayList<IntensityPeakObject> IPOs;
        IntensityPeakObject IPO;
        int nNumTracks=m_vcIPOTs.size(),numIPOs,trackIndex;
        int numUntrackedIPOs=0;
        for(i=m_nFirstIPOHIndex;i<len;i++){
            IPOs=m_vcIPOHs.get(i).getIPOs();
            numIPOs=IPOs.size();
            for(j=0;j<numIPOs;j++){
                IPO=IPOs.get(j);
                trackIndex=IPO.getTrackIndex();
                if(trackIndex<0||trackIndex>=nNumTracks){
                    numUntrackedIPOs++;
                }
            }
        }
    }
    boolean eligibleTrackHead(IntensityPeakObject IPO){
        if(!IPO.bTrackOpening) return false;
        return true;
    }
    boolean trackedIPO(IntensityPeakObject IPO){
        if(IPO.TrackIndex<0) return false;
        return true;
    }
}
