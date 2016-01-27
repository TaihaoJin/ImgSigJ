/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.CommonMethods;
import java.awt.Color;
import ij.ImagePlus;

/**
 *
 * @author Taihao
 */
public class IPOTBundle {
    int m_nBundleIndex,zI,zF;
    double dTotalSignal;
    ArrayList <IPObjectTrack> IPOTs;
    public IPOTBundle(){
        IPOTs=new ArrayList();
        dTotalSignal=-1;
        zI=Integer.MAX_VALUE;
        zF=Integer.MIN_VALUE;
    }
    public void addIPOTs(ArrayList<IPObjectTrack> IPOTs){
        int len=IPOTs.size();
        for(int i=0;i<len;i++){
            this.IPOTs.add(IPOTs.get(i));
        }
        len=IPOTs.size();
        setBundleSizeAndIndexToTracks(m_nBundleIndex);
    }
    public void addIPOT(IPObjectTrack IPOT){
        IPOT.m_nBundleIndex=m_nBundleIndex;
        IPOTs.add(IPOT);
/*        int i,len=IPOT.IPOs.size();
        int nBundleSize=IPOTs.size();
        IPObjectTrack IPOTt;
        for(i=0;i<nBundleSize;i++){
            IPOTt=IPOTs.get(i);
            IPOTt.m_nBundleIndex=m_nBundleIndex;
            IPOTt.setBundleSizeAndIndex(nBundleSize, m_nBundleIndex);//do it after finish the bundle buiding
        }
 *
 */
    }
    public ArrayList<IPObjectTrack> getIPOTs(){
        return IPOTs;
    }
    public void markIPOTB(ImagePlus impl, Color cb, int minPeakPercentageIndex){
        Color ct;
        int len=IPOTs.size();
        for(int i=0;i<len;i++){
            ct=CommonMethods.randomColor();
            IPOTs.get(i).markTrack(impl, cb, ct, minPeakPercentageIndex);
        }
    }
    public int getBundleSize(){
        return IPOTs.size();
    }
    public int exportBundleToAbf(float[] pfData0,int position0){
        int len=IPOTs.size();
        if(len==0) return position0;
        IPObjectTrack IPOT=IPOTs.get(0);
        int AbfDataLength=IPOT.getTrackAbfLengthChannel();
        float[] pfData=new float[AbfDataLength];
        int i,j,num=0;
        for(i=0;i<len;i++){
            IPOT=IPOTs.get(i);
            IPOT.exportTrackToAbfChannel(pfData, 0);
            for(j=0;j<AbfDataLength;j++){
                pfData0[position0+j*len+i]=pfData[j];
                num++;
            }
        }
        return position0+num;
    }
    public void setBundleSizeAndIndexToTracks(int bIndex){
        m_nBundleIndex=bIndex;
        int len=IPOTs.size();
        for(int i=0;i<len;i++){
            IPOTs.get(i).setBundleSizeAndIndex(len,m_nBundleIndex);
        }
    }
    public void mergeBundle(IPOTBundle IPOTB){
        int i,len=IPOTB.IPOTs.size();
        IPObjectTrack IPOT;
        for(i=0;i<len;i++){
            IPOT=IPOTB.IPOTs.get(i);
            IPOT.m_nBundleIndex=m_nBundleIndex;
            IPOTs.add(IPOT);
        }
//        setBundleSizeAndIndexToTracks(m_nBundleIndex); //do it after finish the bundle buiding
    }
    public void updateSliceRangeAndTotalSignal(int zFirst, int zLast){
        int i,len=IPOTs.size(),len1,zi,zf,z,j;
        dTotalSignal=0;
        IPObjectTrack IPOT;
        double pdSignals[]=new double[zLast+1];
        for(z=zFirst;z<=zLast;z++){
            pdSignals[z]=0;
        }

        IntensityPeakObject IPO;
        for(i=0;i<len;i++){
            IPOT=IPOTs.get(i);
            len1=IPOT.IPOs.size();
            zi=IPOT.IPOs.get(0).cz;
            zf=IPOT.IPOs.get(len1-1).cz;
            for(j=0;j<len1;j++){
                IPO=IPOT.IPOs.get(j);
                if(IPO==null) continue;
                z=IPO.cz;
                pdSignals[z-zFirst]+=IPO.dTotalSignal;
            }
            if(zi<zI) zI=zi;
            if(zf>zF) zF=zf;
        }
        
        for(i=0;i<len;i++){
            IPOT=IPOTs.get(i);
            len1=IPOT.IPOs.size();
            for(j=0;j<len1;j++){
                IPO=IPOT.IPOs.get(j);
                if(IPO==null) continue;
                z=IPO.cz;
//               IPO.dTotalBundleSignal=pdSignals[z-zFirst];
                IPO.setBundleTotalSignal(pdSignals[z-zFirst]);
            }
        }
    }
}
