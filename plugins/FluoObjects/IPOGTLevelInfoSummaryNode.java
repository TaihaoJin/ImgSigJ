/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import utilities.CustomDataTypes.DoubleRange;
import java.util.StringTokenizer;

/**
 *
 * @author Taihao
 */
public class IPOGTLevelInfoSummaryNode {

    public String nLevels,Events;
    public int nEvents,nNumBasePars,nParsPerEvent;
    String nTrackIndex,exclusion,verification;
    String xRange,yRange;
    String meanDrift,meanLength,meanQLength,mainAmp,sigmaX,sigmaY,AmpRatio;
    String totalDrift;
    ArrayList<IPOGTLevelSummaryNode> m_cvLevelNodes;
    String recordPath;
    public IPOGTLevelInfoSummaryNode(String line){
//    TrackIndex,Verification,Exclusion,xRange,yRange,meanDrift,Levels,MeanLength,MeanQLength,MainAmp,SigmaX,SigmaY,Ratio,Events,level,start,end,Mean,level,start,end,Mean,level,start,end,Mean,level,start,end,Mean,MainAmp,SigmaX,SigmaY,MainAmp,SigmaX,SigmaY,Ratio,MainAmp,SigmaX,SigmaY,MainAmp,SigmaX,SigmaY,Ratio,recordPath
        StringTokenizer stk=new StringTokenizer(line,",");
        nNumBasePars=0;
        nTrackIndex=stk.nextToken();
        nNumBasePars++;
        verification=stk.nextToken();
        nNumBasePars++;
        exclusion=stk.nextToken();
        nNumBasePars++;
        xRange=stk.nextToken();
        nNumBasePars++;
        yRange=stk.nextToken();
        nNumBasePars++;
        totalDrift=stk.nextToken();
        nNumBasePars++;
        meanDrift=stk.nextToken();
        nNumBasePars++;
        nLevels=stk.nextToken();
        nNumBasePars++;
        meanLength=stk.nextToken();
        nNumBasePars++;
        meanQLength=stk.nextToken();
        nNumBasePars++;
        mainAmp=stk.nextToken();
        nNumBasePars++;
        sigmaX=stk.nextToken();
        nNumBasePars++;
        sigmaY=stk.nextToken();
        nNumBasePars++;
        AmpRatio=stk.nextToken();
        nNumBasePars++;
        Events=stk.nextToken();
        nNumBasePars++;
        nEvents=Integer.parseInt(Events);

        m_cvLevelNodes=new ArrayList();
        IPOGTLevelSummaryNode lNode;
        for(int i=0;i<nEvents;i++){
            lNode=new IPOGTLevelSummaryNode();
            lNode.level=stk.nextToken();
            lNode.sliceI=stk.nextToken();
            lNode.sliceF=stk.nextToken();
            lNode.dLeft=stk.nextToken();
            lNode.dRight=stk.nextToken();
            lNode.mean=stk.nextToken();
            lNode.delta=stk.nextToken();
            lNode.stepRatio=stk.nextToken();
            m_cvLevelNodes.add(lNode);
        }
        for(int i=0;i<nEvents;i++){
            lNode=m_cvLevelNodes.get(i);
            lNode.mainAmp=stk.nextToken();
            lNode.sigmaX=stk.nextToken();
            lNode.sigmaY=stk.nextToken();
            lNode.AmpRatio=stk.nextToken();
        }
        recordPath=stk.nextToken();
        nNumBasePars++;
        nParsPerEvent=12;
    }
    int getNumEvents(int len){
        int nEvents=(len-nNumBasePars)/nParsPerEvent;
        return nEvents;
    }
    public String[] getParsAsStringArray(int len){
        int i,j;
        String[] psPars=new String[len];
        int index=0;
        psPars[index]=nTrackIndex;
        index++;
        psPars[index]=verification;
        index++;
        psPars[index]=exclusion;
        index++;
        psPars[index]=xRange;
        index++;
        psPars[index]=yRange;
        index++;
        psPars[index]=totalDrift;
        index++;
        psPars[index]=meanDrift;
        index++;
        psPars[index]=nLevels;
        index++;
        psPars[index]=meanLength;
        index++;
        psPars[index]=meanQLength;
        index++;
        psPars[index]=mainAmp;
        index++;
        psPars[index]=sigmaX;
        index++;
        psPars[index]=sigmaY;
        index++;
        psPars[index]=AmpRatio;
        index++;
        psPars[index]=Events;
        index++;

        IPOGTLevelSummaryNode lNode;
        int level=-1,indext=nEvents-1;
        int num=0;
        while(indext>=0){
            num++;
            lNode=m_cvLevelNodes.get(indext);
            if(Integer.parseInt(lNode.level)>level){
                for(j=0;j<nParsPerEvent-4;j++){
                    psPars[index]="  ";
                    index++;
                }
                level++;
                if(level==0) level++;
                continue;
            }
            psPars[index]=lNode.level;
            index++;
            psPars[index]=lNode.sliceI;
            index++;
            psPars[index]=lNode.sliceF;
            index++;
            psPars[index]=lNode.dLeft;
            index++;
            psPars[index]=lNode.dRight;
            index++;
            psPars[index]=lNode.mean;
            index++;
            psPars[index]=lNode.delta;
            index++;
            psPars[index]=lNode.stepRatio;
            index++;
            level++;
            if(level==0) level++;
            indext--;
        }
        int nEmptyEvents=getNumEvents(len)-num;
        for(i=0;i<nEmptyEvents;i++){
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
        }
        level=-1;
        indext=nEvents-1;
        while(indext>=0){
            lNode=m_cvLevelNodes.get(indext);
            if(Integer.parseInt(lNode.level)>level){
                for(j=0;j<4;j++){
                    psPars[index]="  ";
                    index++;
                }
                level++;
                if(level==0) level++;
                continue;
            }
            psPars[index]=lNode.mainAmp;
            index++;
            psPars[index]=lNode.sigmaX;
            index++;
            psPars[index]=lNode.sigmaY;
            index++;
            psPars[index]=lNode.AmpRatio;
            index++;
            level++;
            indext--;
            if(level==0) level++;
        }
        for(i=0;i<nEmptyEvents;i++){
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
            psPars[index]=" ";
            index++;
        }
        psPars[index]=recordPath;
        index++;
        return psPars;
    }
    public String[] getParNamesAsStringArray(int len){
        String[] psPars=new String[len];
        int nEvents=getNumEvents(len);
        int index=0;
        psPars[index]="TrkIndex";
        index++;
        psPars[index]="verified";
        index++;
        psPars[index]="exclusion";
        index++;
        psPars[index]="xRange";
        index++;
        psPars[index]="yRange";
        index++;
        psPars[index]="totalDrift";
        index++;
        psPars[index]="meanDrift";
        index++;
        psPars[index]="Levels";
        index++;
        psPars[index]="meanLen";
        index++;
        psPars[index]="meanQLen";
        index++;
        psPars[index]="mainAmp";
        index++;
        psPars[index]="sigmaX";
        index++;
        psPars[index]="sigmaY";
        index++;
        psPars[index]="AmpRatio";
        index++;
        psPars[index]="Events";
        index++;

        for(int i=0;i<nEvents;i++){
            psPars[index]="level"+i;
            index++;
            psPars[index]="sI"+i;
            index++;
            psPars[index]="sF"+i;
            index++;
            psPars[index]="dLeft"+i;
            index++;
            psPars[index]="dRight"+i;
            index++;
            psPars[index]="mean"+i;
            index++;
            psPars[index]="delta"+i;
            index++;
            psPars[index]="stepRatio"+i;
            index++;
        }
        for(int i=0;i<nEvents;i++){
            psPars[index]="mainAmp"+i;
            index++;
            psPars[index]="sigmaX"+i;
            index++;
            psPars[index]="sigmaY"+i;
            index++;
            psPars[index]="AmpRatio"+i;
            index++;
        }
        psPars[index]="recordPath";
        index++;
        return psPars;
    }
    public int getNumString(int nEvents){
        return nNumBasePars+nEvents*nParsPerEvent;
    }
}
