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
public class IPOGTrackHandler {
    public static String[][] getIPOGTracksAsStrings(ArrayList<IPOGTrackNode> cvIPOGTs, String option){
        String[][] IPOTStrings=null;
        int rows=0,cols=cvIPOGTs.size(),extra=2;

        IPOGTrackNode IPOGT;
        IPOGaussianNode IPOG,IPOG1;
        int i,j,k,len;
        ArrayList<Integer> nvLengths=new ArrayList();
        for(i=0;i<cols;i++){
            IPOGT=cvIPOGTs.get(i);
            len=IPOGT.m_cvIPOGs.size();
            nvLengths.add(len);
            if(len>rows) rows=len;
        }
        IPOTStrings=new String[rows+1+extra][cols];
        
        double h;

        String emptyCell="";
        String gapCell="";
        int cellWidth=18+6+6;
        for(i=0;i<cellWidth;i++){
            emptyCell+=" ";
            if(i<20)
                gapCell+=" ";
            else
                gapCell+="-";
        }
        String normalHead;
        for(i=0;i<rows;i++){
            for(j=0;j<cols;j++){
                IPOGT=cvIPOGTs.get(j);
                len=IPOGT.m_cvIPOGs.size();
                if(i<len){
                    IPOG=IPOGT.m_cvIPOGs.get(i);
                    if(IPOG==null){
                        IPOTStrings[i+1][j]=gapCell;
                        continue;
                    }
                    IPOTStrings[i+1+extra][j]=" ("+PrintAssist.ToString(IPOG.xc, 5, 1);
                    IPOTStrings[i+1+extra][j]+=","+PrintAssist.ToString(IPOG.yc, 5, 1);
                    IPOTStrings[i+1+extra][j]+=","+PrintAssist.ToString(IPOG.sliceIndex, 3,0)+")";
                    IPOTStrings[i+1+extra][j]+=PrintAssist.ToString(IPOG.Amp, 5, 0)+",";
                    IPOTStrings[i+1+extra][j]+=PrintAssist.ToString(IPOG.dTotalSignal, 6, 0);
                }else{
                    IPOTStrings[i+1+extra][j]=emptyCell;
                }
            }
        }

        for(i=0;i<cols;i++){
            IPOGT=cvIPOGTs.get(i);            
            IPOTStrings[0][i]="Tk"+IPOGT.TrackIndex+"(";
            len=IPOGT.m_cvIPOGs.size();
            IPOG=IPOGT.m_cvIPOGs.get(0);
            IPOTStrings[0][i]+=IPOG.sliceIndex+"-";
            IPOG=IPOGT.m_cvIPOGs.get(len-1);
            IPOTStrings[0][i]+=IPOG.sliceIndex+")";
            
            if(IPOGT.normalTrackHeadShape())
                IPOTStrings[1][i]="NormalShape";
            else
                IPOTStrings[1][i]="AbnormalShape";
            h=IPOGT.getHeadValueAve(option);
            IPOTStrings[2][i]=PrintAssist.ToString(h, 1);
        }
        return IPOTStrings;
    }
}
