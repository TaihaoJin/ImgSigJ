/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class IPOGTrackBundleNode extends IPOGTrackNode{
    static public final int minTrackIndex=1000000;
    ArrayList<IPOGTrackNode> IPOGTs;
    
    public IPOGTrackBundleNode(){
        IPOGTs=new ArrayList();
        TrackIndex=-1;
        BundleIndex=-1;
        m_cvIPOGs=new ArrayList();
        cHeightRange=new DoubleRange();
        cClusterSizeRange=new intRange();
        cTotalSignalRange=new DoubleRange();
        firstSlice=Integer.MAX_VALUE;
        lastSlice=Integer.MIN_VALUE;
    }
    public void addTrack(IPOGTrackNode IPOGT){
        IPOGTs.add(IPOGT);
        if(IPOGTs.size()==1){
            TrackIndex=IPOGT.BundleIndex+this.minTrackIndex;
            BundleIndex=IPOGT.BundleIndex;
        }
    }
    public void finalizeBundle(){
        int i,len=IPOGTs.size(),len1,si,sf,z,j;
        IPOGTrackNode IPOGT;
        IPOGaussianNode IPOG;
        ArrayList<IPOGTrackNode> IPOGTst;

        IntensityPeakObject IPO;

        for(i=0;i<len;i++){
            IPOGT=IPOGTs.get(i);
            len1=IPOGT.m_cvIPOGs.size();
            si=IPOGT.m_cvIPOGs.get(0).sliceIndex;
            sf=IPOGT.m_cvIPOGs.get(len1-1).sliceIndex;
            if(si<firstSlice) firstSlice=si;
            if(sf>lastSlice) lastSlice=sf;
        }

        int nIPOGs=lastSlice-firstSlice+1;
        ArrayList<IPOGaussianNode>[] pcvIPOGBs=new ArrayList[nIPOGs];
        for(i=0;i<nIPOGs;i++){
            pcvIPOGBs[i]=new ArrayList();
        }

        int slice;
        for(i=0;i<len;i++){
            IPOGT=IPOGTs.get(i);
            len1=IPOGT.m_cvIPOGs.size();
            for(j=0;j<len1;j++){
                IPOG=IPOGT.m_cvIPOGs.get(j);
                if(IPOG==null) continue;
                slice=IPOG.sliceIndex;
                pcvIPOGBs[slice-firstSlice].add(IPOG);
            }
        }
        ArrayList<IPOGaussianNode> IPOGs;
        for(slice=firstSlice;slice<=lastSlice;slice++){
            IPOGs=pcvIPOGBs[slice-firstSlice];
            if(IPOGs.isEmpty()) {
                m_cvIPOGs.add(null);
                continue;
            }
            m_cvIPOGs.add(new IPOGBundleNode(IPOGs));
        }
        BundleSize=IPOGTs.size();
        for(i=0;i<BundleSize;i++){
            IPOGTs.get(i).setBundleSize(BundleSize);
        }
    }
}
