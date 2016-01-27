/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class IPOGBundleNode extends IPOGaussianNode{
    IPOGaussianNode IPOGx;
    ArrayList<IPOGaussianNode> IPOGs;
    public IPOGBundleNode(ArrayList<IPOGaussianNode> IPOGs0){
        this.IPOGs=IPOGaussianNodeHandler.getSimpleIPOGs(IPOGs0);
        double Ampx=Double.NEGATIVE_INFINITY;
        int i,len=IPOGs.size();
        IPOGaussianNode IPOG;
        dTotalSignalCal=0;
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            if(IPOG.Amp>Ampx){
                Ampx=IPOG.Amp;
                IPOGx=IPOG;
            }
            dTotalSignalCal+=IPOG.dTotalSignalCal;
        }
        copy(IPOGx);
        TrackIndex=IPOGTrackBundleNode.minTrackIndex+BundleIndex;
    }
    public void getParsAsStrings(ArrayList<String> names, ArrayList<String> values){
        names.clear();
        String label;
        values.clear();
        names.add("slice");
        values.add(PrintAssist.ToString(sliceIndex));
        names.add("Id");
        values.add(PrintAssist.ToString(IPOIndex));
        names.add("IPOGs");

        label="[";
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            label+=IPOGs.get(i).IPOIndex;
            if(i<len-1)label+=",";
        }
        label+="]";

        values.add(label);
        names.add("Xc");
        values.add(PrintAssist.ToString(xcr,6,1));
        names.add("Yc");
        values.add(PrintAssist.ToString(ycr,6,1));
        names.add("Amp");
        values.add(PrintAssist.ToString(Amp,6,1));
        names.add("Peak1");
        values.add(PrintAssist.ToString(peak1,6,1));
        names.add("Signal");
        values.add(PrintAssist.ToString(dTotalSignal,8,1));
        names.add("Bundle Signal");
        values.add(PrintAssist.ToString(dBundleTotalSignal,8,1));
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
    public double getAmpAt(double x,double y){
        double[]X={x,y};
        int i,len=IPOGs.size();
        double dv=0;
        for(i=0;i<len;i++){
            dv+=IPOGs.get(i).getAmpAt(x, y);
        }
        return dv;
    }
    int getClusterSize(){
        return IPOGs.size();
    }
    public ArrayList<IPOGaussianNode> getIPOGs(){
        return IPOGs;
    }
    int getNumIPOGs(){
        return IPOGs.size();
    }
}
