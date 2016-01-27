/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FluoObjects;
import java.util.ArrayList;
import java.util.Formatter;
import utilities.CommonStatisticsMethods;
import utilities.Gui.AnalysisMasterForm;
import utilities.io.FileAssist;
import utilities.io.PrintAssist;
/**
 *
 * @author Taihao
 */
public class IPOGTLevelInfoCollectionNode {
    public ArrayList<IPOGTLevelInfoNode> cvLevelInfos;
    public String sID;
    public int nNodeIndex;
    public int nNumTracks,nNumVerified,nNumReliableTransitions,nMaxNumLevels,nMaxNumLevelsVerified,nMaxNumLevelsReliableTransitions,nNumCurveElements=IPOGTLevelInfoNode.getNumOfCurveEvaluationElements();
    public int[] pnNumLevelsHist,pnNumLevelsHistVerified,pnNumLevelsHistReliableTransitions,pnNumTracksHist_CurveElements;
    double[] pdHistCurveElements;
    double[][] psHistCurveElementsJoint;
    public IPOGTLevelInfoCollectionNode(ArrayList<IPOGTLevelInfoNode> cvInfoNodes){
        cvLevelInfos=new ArrayList();
        for(int i=0;i<cvInfoNodes.size();i++){
            cvLevelInfos.add(cvInfoNodes.get(i));
        }
        nNumTracks=cvInfoNodes.size();
        calMaxNumLevels();
        calSummary();
    }
    void calMaxNumLevels(){
        int i,len=cvLevelInfos.size(),nLevels;
        nNumVerified=0;
        nNumReliableTransitions=0;
        nMaxNumLevels=0;
        nMaxNumLevelsVerified=0;
        nMaxNumLevelsReliableTransitions=0;
        IPOGTLevelInfoNode cInfoNode;
        for(i=0;i<len;i++){
            cInfoNode=cvLevelInfos.get(i);
            nLevels=cInfoNode.getMaxLevel_A()+1;
            if(nLevels>nMaxNumLevels) nMaxNumLevels=nLevels;            
            if(cInfoNode.Verified_Automatically()){
                if(nLevels>nMaxNumLevelsVerified) nMaxNumLevelsVerified=nLevels;
                nNumVerified++;
            }
            /*
            if(cInfoNode.isReliableTransitions()){
                nNumReliableTransitions++;
                if(nLevels>nMaxNumLevelsReliableTransitions)nMaxNumLevelsReliableTransitions=nLevels;
            }*/
        }
    }
    void calSummary(){
        pdHistCurveElements=new double[nNumCurveElements];
        psHistCurveElementsJoint=new double[nNumCurveElements][nNumCurveElements];        
        CommonStatisticsMethods.setElements(pdHistCurveElements, 0);
        CommonStatisticsMethods.setElements(psHistCurveElementsJoint, 0);
        
        pnNumLevelsHist=new int[nMaxNumLevels];
        pnNumLevelsHistVerified=new int[nMaxNumLevels];
        pnNumLevelsHistReliableTransitions=new int[nMaxNumLevels];
        CommonStatisticsMethods.setElements(pnNumLevelsHist, 0);
        CommonStatisticsMethods.setElements(pnNumLevelsHistVerified, 0);
        CommonStatisticsMethods.setElements(pnNumLevelsHistReliableTransitions, 0);
        
        ArrayList<String> svCurveEvaluations;
        String se1,se2;
        
        int i,len=cvLevelInfos.size(),nLevels,j,k,len1,iJ,iK;
        IPOGTLevelInfoNode cInfoNode;
        for(i=0;i<len;i++){
            cInfoNode=cvLevelInfos.get(i);
            nLevels=cInfoNode.getMaxLevel_A();
//            if(!cInfoNode.isNumLevelsDetermined_A()) nLevels=-1;//13131
            if(nLevels<=0) continue;
            pnNumLevelsHist[nLevels-1]++;
            if(cInfoNode.verifiable()){
                pnNumLevelsHistVerified[nLevels-1]++;
            }
            if(cInfoNode.isReliableTransitions()){
                pnNumLevelsHistReliableTransitions[nLevels-1]++;
            }
            
            svCurveEvaluations=cInfoNode.svCurveEvaluations_Automated;
            len1=svCurveEvaluations.size();
            for(j=0;j<len1;j++){
                se1=svCurveEvaluations.get(j);
                iJ=IPOGTLevelInfoNode.getEvaluationIndex(se1);
                if(iJ<0) continue;
                pdHistCurveElements[iJ]+=1.;
                for(k=0;k<len1;k++){
                    se2=svCurveEvaluations.get(k);
                    iK=IPOGTLevelInfoNode.getEvaluationIndex(se2);
                    if(iK<0) continue;
                    psHistCurveElementsJoint[iJ][iK]+=1;
                    psHistCurveElementsJoint[iK][iJ]+=1;
                }
            }
        }
    }
    void clear(StringBuffer sb){
        int len=sb.length();
        if(len>0) sb.delete(0, len);
    }
}
