/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.Non_LinearFitting;
import java.util.ArrayList;
import utilities.statistics.OneDKMeans;
import utilities.statistics.MeanSem1;
import utilities.CommonStatisticsMethods;
/**
 *
 * @author Taihao
 */
public class ModePars {
    public int nModes,nMainComponent;
    public double[] pdMeans,pdWeights,pdSDs;
    public boolean[] pbFixMeans,pbFixWeights,pbFixSDs;
    public OneDKMeans m_cKM;
    public ModePars(int nModes){
        this.nModes=nModes;
        if(nModes>0){
            pdMeans=new double[nModes];
            pdWeights=new double[nModes];
            pdSDs=new double[nModes];
            pbFixMeans=new boolean[nModes];
            pbFixSDs=new boolean[nModes];
            pbFixWeights=new boolean[nModes];
            nMainComponent=0;
        }
    }
    public double[] getExpandedAdjustablePars(double[] pdAdjustablePars){
        if(nModes<0) return pdAdjustablePars;
        ArrayList<Double> dvPars=new ArrayList();
        int i;
        for(i=0;i<nModes;i++){
            if(!pbFixMeans[i]) dvPars.add(pdMeans[i]);
            if(!pbFixSDs[i]) dvPars.add(pdSDs[i]);
            if(!pbFixWeights[i]) dvPars.add(pdWeights[i]);
        }
        
        int nPars=pdAdjustablePars.length,num=dvPars.size();
        double pdPars[]=new double[nPars+num];
        for(i=0;i<nPars+num;i++){
            if(i<nPars)
                pdPars[i]=pdAdjustablePars[i];
            else
                pdPars[i]=dvPars.get(i-nPars);
        }
        return pdPars;
    }
    public void setDefaultPars(double[] pdData){
                if(m_cKM==null) 
                    m_cKM=new OneDKMeans(pdData,nModes);
                else
                    m_cKM.updateData(pdData,nModes);
                double[] pdMeans=m_cKM.getMeans(),pdWeights=m_cKM.getWeights();
                nMainComponent=m_cKM.getMainCluster();
                MeanSem1 ms=CommonStatisticsMethods.buildMeanSem1(pdData, 0, pdData.length-1, 1);
                double sd=ms.getSD();
                for(int i=0;i<nModes;i++){
                    this.pdMeans[i]=pdMeans[i];
                    this.pdWeights[i]=pdWeights[i];
                    pdSDs[i]=sd;
                }
    }
    public void updateModePars(double[] pdAdjustablePars){
        int i,nPars=pdAdjustablePars.length,it;
        for(i=0;i<nModes;i++){
            it=nModes-1-i;
            if(!pbFixWeights[it]) {
                nPars--;
                pdWeights[it]=pdAdjustablePars[nPars];
            }
            if(!pbFixSDs[it]) {
                nPars--;
                pdSDs[it]=pdAdjustablePars[nPars];
            }
            if(!pbFixMeans[it]) {
                nPars--;
                pdMeans[it]=pdAdjustablePars[nPars];
            }
        }
    }
    public void setMeans(double[] pdMeans){
        for(int i=0;i<nModes;i++){
            this.pdMeans[i]=pdMeans[i];
        }
    }
    public void setSDs(double[] pdSDs){
        for(int i=0;i<nModes;i++){
            this.pdSDs[i]=pdSDs[i];
        }
    }
    public void setWeights(double[] pdWeights){
        for(int i=0;i<nModes;i++){
            this.pdWeights[i]=pdWeights[i];
        }
    }
}
