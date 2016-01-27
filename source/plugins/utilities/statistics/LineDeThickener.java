/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;

import java.awt.Color;
import java.util.ArrayList;
import utilities.CommonGuiMethods;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.intRange;
import utilities.Gui.PlotWindowPlus;
import utilities.QuickSort;

/**
 *
 * @author Taihao
 */
public class LineDeThickener {
    class FlattenNode {
        int position,multiplicity,left,right,base,sign;
    }
    double[] pdX, pdY, pdYDethickended;
    int nMaxRisingINterval, nDataSize;
    double dRx;
    public LineDeThickener(){
        dRx=10;
    }
    public LineDeThickener(double[] pdX, double[] pdY, int nMaxRisingInterval){
        this();
        this.nMaxRisingINterval=nMaxRisingInterval;
        nDataSize=pdX.length;
        this.pdX=pdX;
        this.pdY=pdY;
        flattenLine();
        dethickenLine();
    }
    public void flattenLine(){
        pdYDethickended=CommonStatisticsMethods.copyArray(pdY);
        double[] pdYt0=CommonStatisticsMethods.copyArray(pdY),pdYt=CommonStatisticsMethods.copyArray(pdY);
        ArrayList<Integer> nvProbeContactingDLx=new ArrayList();
        ArrayList<Integer> nvProbeContactingULn=new ArrayList();
        
        double[] pdEnvD=new double[nDataSize],pdEnvU=new double[nDataSize];
        boolean[] pdContactingU=new boolean[nDataSize],pdContactingD=new boolean[nDataSize];
        
        double[] pdThickness=new double[nDataSize];
        ArrayList<Integer> nvLx=new ArrayList(),nvLn=new ArrayList();
                              
        int i,len, p;
        ArrayList<Double> dvThickness=new ArrayList();        
        int[] pnIndexes=new int[nDataSize];   
        
        int iters=0,left,right;
        intRange ir=new intRange();
        int[] pnIterations=new int[nDataSize];
        CommonStatisticsMethods.setElements(pnIterations, -1);
        PlotWindowPlus pw=CommonGuiMethods.displayNewPlotWindowPlus("Flattening Process", pdX, pdY, 1, 2, Color.BLACK);
        Color c;
        while(iters<1){
            getProbeContactingExtrema(pdX,pdYt0,dRx,nvProbeContactingDLx,nvProbeContactingULn);
            len=nvProbeContactingDLx.size();
            flattenExtrema(pdYt0,pdYt,nvProbeContactingDLx,1);
            CommonStatisticsMethods.copyArray(pdYt,pdYt0);
            CommonStatisticsMethods.setElements(pnIterations,iters);
            iters++;
            c=CommonGuiMethods.getDefaultColor(iters);
            pw.addPlot("Iterations"+iters, pdX, pdYt, 2,2, c);
        }
        
    }
    void flattenExtrema(double[] pdYt0, double[] pdYt, ArrayList<Integer> nvExtrema, int multiplicity){
        
    }
    public void getProbeContactingExtrema(double[] pdX, double[] pdYt, double dRx, ArrayList<Integer> nvProbeContactingDLx, ArrayList<Integer> nvProbeContactingULn){
        ProbingBall pb=new ProbingBall(pdX,pdYt,dRx,-1,0);
        ArrayList<Integer> nvLx,nvLn;
        nvLx=pb.getProbeContactingPositions(pb.Downward);
        nvLn=pb.getProbeContactingPositions(pb.Upward);
        int i, index, len=nvLx.size();
        for(i=0;i<len;i++){
            index=len-1-i;
            if(!CommonStatisticsMethods.isLocalExtrema(pdYt, nvLx.get(index), 1)) nvLx.remove(index);
        }
        len=nvLn.size();
        for(i=0;i<len;i++){
            index=len-1-i;
            if(!CommonStatisticsMethods.isLocalExtrema(pdYt, nvLn.get(index), -1)) nvLn.remove(index);
        }
        CommonStatisticsMethods.copyArray(nvLx, nvProbeContactingDLx);
        CommonStatisticsMethods.copyArray(nvLn, nvProbeContactingULn);
    }
    public void dethickenLine(){
        pdYDethickended=CommonStatisticsMethods.copyArray(pdY);
        double[] pdYt0=CommonStatisticsMethods.copyArray(pdY),pdYt=CommonStatisticsMethods.copyArray(pdY);
        double[] pdEnvD=new double[nDataSize],pdEnvU=new double[nDataSize];
        boolean[] pdContactingU=new boolean[nDataSize],pdContactingD=new boolean[nDataSize];
        
        double[] pdThickness=new double[nDataSize];
        ArrayList<Integer> nvLx=new ArrayList(),nvLn=new ArrayList();
                              
        int i,len, p;
        ArrayList<Double> dvThickness=new ArrayList();        
        int[] pnIndexes=new int[nDataSize];   
        
        int iters=0,left,right;
        intRange ir=new intRange();
        int[] pnIterations=new int[nDataSize];
        CommonStatisticsMethods.setElements(pnIterations, -1);
        PlotWindowPlus pw=CommonGuiMethods.displayNewPlotWindowPlus("DeThickening Process", pdX, pdY, 1, 2, Color.BLACK);
        Color c;
        while(iters<1){
            CommonStatisticsMethods.setElements(pdThickness, -1);
            getThicknessAndExtrema(pdX,pdYt0,pdEnvD,pdEnvU,pdThickness,nvLn,nvLx,pdContactingD,pdContactingU,iters);  
            CommonStatisticsMethods.getElements(pdThickness,nvLx,dvThickness);
            CommonStatisticsMethods.setSortedIndexes(pnIndexes);
            QuickSort.quicksort(CommonStatisticsMethods.copyToDoubleArray(dvThickness), pnIndexes);
            len=nvLx.size();
            for(i=0;i<len;i++){
                p=nvLx.get(pnIndexes[len-1-i]);
                if(p==16){
                    i=i;
                }
                if(!Adjustable(p,pdContactingD,pdContactingU,pnIterations,pdYt,iters,ir)) continue;
                left=ir.getMin();
                right=ir.getMax();
                adjustPeak(pdX,pdYt0,pdYt,pnIterations, left,p,right,iters);
            }
            CommonStatisticsMethods.copyArray(pdYt,pdYt0);
            CommonStatisticsMethods.setElements(pnIterations,iters);
            iters++;
            c=CommonGuiMethods.getDefaultColor(iters);
            pw.addPlot("Iterations"+iters, pdX, pdYt, 2,2, c);
        }
    }
    int getThicknessAndExtrema(double[] pdX, double[] pdYt, double[] pdEnvD, double[] pdEnvU, double[] pdThickness, ArrayList<Integer> nvLn, ArrayList<Integer>nvLx,
            boolean[] pbContactingD, boolean[] pbContactingU, int iters){
        nvLn.clear();
        nvLx.clear();
        ProbingBall pb=new ProbingBall(pdX,pdYt,dRx,-1,0);
        ArrayList<Integer> nvProbeContactingD=pb.getProbeContactingPositions(ProbingBall.Downward);
        ArrayList<Integer> nvProbeContactingU=pb.getProbeContactingPositions(ProbingBall.Upward);
        CommonMethods.LocalExtrema(pdYt, nvLn, nvLx);
        CommonStatisticsMethods.setElements(pbContactingD, false);
        CommonStatisticsMethods.setElements_AND(pbContactingD, nvProbeContactingD,nvLx,true);
        CommonStatisticsMethods.setElements(pbContactingU, false);
        CommonStatisticsMethods.setElements_AND(pbContactingU, nvProbeContactingU,nvLn,true);
        
        nvLn.clear();
        nvLx.clear();
        CommonStatisticsMethods.buildAncoredLine(pdX,pdYt,pdEnvD,nvProbeContactingD);
        CommonStatisticsMethods.buildAncoredLine(pdX,pdYt,pdEnvU,nvProbeContactingU);
        CommonStatisticsMethods.copyArray(pdEnvD,pdThickness);
        CommonStatisticsMethods.subtractArray(pdThickness, pdEnvU);        
        CommonMethods.LocalExtrema(pdThickness, nvLn, nvLx);
        PlotWindowPlus pw=CommonGuiMethods.displayNewPlotWindowPlus("DeThickening iter. "+iters, pdX, pdY, 1, 2, Color.BLACK);
        pw.addPlot("EnvLineD", pdX, pdEnvD, 2, 2, Color.BLUE);
        pw.addPlot("EnvLineU", pdX, pdEnvU, 2, 2, Color.RED);
        pw.addPlot("Thickness", pdX, pdThickness, 2, 2, Color.RED);

        return 1;
    }
    int adjustPeak(double[] pdXt, double[] pdYt0,double[] pdYt, int[] pnIterations, int left, int p0, int right, int iters){
        double peak0=pdYt[p0], base, subBase,sign=1,dL=pdYt[left],dR=pdYt[right],wb,wp,peak,dt,xb,xp;
        int p;
        if((peak0-dL)*(peak0-dR)<0) return -1;
        if(peak0<dL) sign=-1;
        if(sign*(dL-dR)>0){
            base=dL;
            subBase=dR;
            xb=pdXt[left];
            xp=pdXt[p0];
        }else{
            base=dR;
            subBase=dL;            
            xb=pdXt[right];
            xp=pdXt[p0];
        }
        wb=5;
        wp=1;
        peak=(wb*base+wp*peak0)/(wb+wp);
        for(p=left+1;p<right;p++){
            dt=pdY[p];
            if(sign*(dt-base)<0) continue;
            dt=CommonMethods.getLinearIntoplation(xb, base, xp, peak, pdXt[p]);
            pdYt[p]=dt;
            pnIterations[p]=iters;
            
        }
        return 1;
    }
    boolean Adjustable(int p,boolean[] pbContactingD, boolean[] pbContactingU, int[] pnIterations, double[] pdYt, int iters, intRange ir){
        if(pnIterations[p]>=iters) return false;
        boolean[] pbt;
        if(pbContactingD[p])
            pbt=pbContactingU;
        else
            pbt=pbContactingD;
        int left=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p-1, -1),right=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p+1, 1);
        if(left<0) left=0;
        if(right<0) right=nDataSize-1;
        ir.setRange(left, right);
        if(pnIterations[left]>=iters||pnIterations[right]>=iters) return false;
        double dp=getBaseDiff(p,pbContactingD,pbContactingU,pdYt,pnIterations,iters);
        if(Double.isNaN(dp)) return false;
        double dl=getBaseDiff(left,pbContactingD,pbContactingU,pdYt,pnIterations,iters),dr=getBaseDiff(right,pbContactingD,pbContactingU,pdYt,pnIterations,iters);
        if(Double.isNaN(dl)||Double.isNaN(dr)) return false;
        if(Math.abs(dp)>Math.min(Math.abs(dl), Math.abs(dr))) return false;
        return true;
    }
    double getBaseDiff(int p,boolean[] pbContactingD, boolean[] pbContactingU, double[] pdYt,int[] pnIterations, int iters){
        boolean[] pbt;
        if(pbContactingD[p])
            pbt=pbContactingU;
        else
            pbt=pbContactingD;
        int left=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p-1, -1),right=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p+1, 1);
        if(left<0) left=0;
        if(right<0) right=nDataSize-1;
        if(pnIterations[left]>=iters||pnIterations[right]>=iters) return Double.NaN;
        return pdYt[left]-pdYt[right];
    }    
}
