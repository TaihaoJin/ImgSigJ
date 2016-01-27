/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FluoObjects;
import ij.IJ;
import java.io.*;
import java.util.ArrayList;
import utilities.io.PrintAssist;
import utilities.io.IOAssist;
import utilities.CommonMethods;
import utilities.CommonGuiMethods;
import utilities.CommonStatisticsMethods;
import java.util.StringTokenizer;
import FluoObjects.IPOGTLevelInfoCollectionNode;
import utilities.io.FileAssist;
import java.util.Formatter;
import utilities.Gui.AnalysisMasterForm;
import utilities.QuickFormatter;
/**
 *
 * @author Taihao
 */
public class IPOGTLevelInfoAnalyzer {
    String sListFilePath,sQuantityName;
    ArrayList<IPOGTLevelInfoNode> cvInfoNodes;
    ArrayList<String> svTLFFilePathes;
    ArrayList<IPOGTLevelInfoCollectionNode> cvInfoCollections;
    int nNumCollections,nNumCurveFeatures=IPOGTLevelInfoNode.getNumOfCurveEvaluationElements();
    int nMaxNumLevels,nNumTracks,nNumVerified,nNumReliableTransitions;
    int[][] pnNumLevelsHist,pnNumLevelsHistVerified,pnNumLevelsHistReliableTransitions;
    double[][] pdHistCurveElements;
    double[][] pdHistCurveElementsJoint;
    boolean bRefit,bExport;
    public IPOGTLevelInfoAnalyzer(){
        bRefit=false;
        cvInfoNodes=new ArrayList();
        svTLFFilePathes=new ArrayList();
        cvInfoCollections=new ArrayList();
    }
    public IPOGTLevelInfoAnalyzer(String sListFilePath, String sQuantityName){
        this();
        this.sListFilePath=sListFilePath;
        this.sQuantityName=sQuantityName;
//        bRefit=true;
        readInfoNodes();
        importLevelInfoCollections();
        showLevelInfo();
        calSummary();
        exportSummary();
    }
    public void readInfoNodes(){
        importTLFFilePathes();
    }
    void importTLFFilePathes(){
        svTLFFilePathes.clear();
        File file = new File(sListFilePath);
        FileReader f=null;
        try{f=new FileReader(file);}
        catch (FileNotFoundException e){
            IJ.error("the script file not found");
        }
        BufferedReader br=new BufferedReader(f);
        String line=IOAssist.readLine(br);
        nNumCollections=Integer.parseInt(line);
        for(int i=0;i<nNumCollections;i++){
            line=IOAssist.readLine(br);
            if(line.contentEquals("$End")) {
                break;
            }
            line=FileAssist.changeExt(line, "TLH");
            svTLFFilePathes.add(line);
        }
        nNumCollections=svTLFFilePathes.size();        
    }
    void importLevelInfoCollections(){
        int i,len=svTLFFilePathes.size(),j;
        StackIPOGTracksNode aStackNode=new StackIPOGTracksNode();
        IPOGTLevelInfoCollectionNode aCollectionNode;
        String path;
        bExport=false;
        bRefit=false;
        cvInfoCollections.clear();
        nMaxNumLevels=0;
        int nLevels,status;
        for(i=0;i<len;i++){
            if(i==1) 
                i=i;
            path=svTLFFilePathes.get(i);
            status=aStackNode.importIPOGTrackLevelInfo(path, sQuantityName, 2);  
            if(bRefit) 
                aStackNode.refitLevelInfoNodes(sQuantityName);
            if(bExport) aStackNode.exportTracksLevelInfo(path);
            IJ.showStatus("ImportLevelInfoCollections"+i);
            if(status<0)
                continue;
            aCollectionNode=aStackNode.getIPOGTLevelInfoCollection(sQuantityName);
            aCollectionNode.sID=path;
            aCollectionNode.nNodeIndex=i;
            nLevels=aCollectionNode.nMaxNumLevels;
            if(nLevels>nMaxNumLevels) nMaxNumLevels=nLevels;        
            cvInfoCollections.add(aCollectionNode);
        }
    }
    void calSummary(){
        int i,j,k;
        nNumCollections=cvInfoCollections.size();
        pdHistCurveElements=new double[nNumCollections+1][nNumCurveFeatures];
        pdHistCurveElementsJoint=new double[nNumCurveFeatures][nNumCurveFeatures];        
        CommonStatisticsMethods.setElements(pdHistCurveElements, 0);
        CommonStatisticsMethods.setElements(pdHistCurveElementsJoint, 0);
        
        pnNumLevelsHist=new int[nNumCollections+1][nMaxNumLevels];
        pnNumLevelsHistVerified=new int[nNumCollections+1][nMaxNumLevels];
        pnNumLevelsHistReliableTransitions=new int[nNumCollections+1][nMaxNumLevels];
        CommonStatisticsMethods.setElements(pnNumLevelsHist, 0);
        CommonStatisticsMethods.setElements(pnNumLevelsHistVerified, 0);
        CommonStatisticsMethods.setElements(pnNumLevelsHistReliableTransitions, 0);
        
        IPOGTLevelInfoCollectionNode aCollectionNode;
        int num;
        double dNum;
        nNumTracks=0;
        nNumVerified=0;
        nNumReliableTransitions=0;
        
        for(i=0;i<nNumCollections;i++){
            aCollectionNode=cvInfoCollections.get(i);
            nNumTracks+=aCollectionNode.nNumTracks;
            nNumVerified+=aCollectionNode.nNumVerified;
            nNumReliableTransitions+=aCollectionNode.nNumReliableTransitions;
            for(j=0;j<aCollectionNode.nMaxNumLevels;j++){
                num=aCollectionNode.pnNumLevelsHist[j];
                pnNumLevelsHist[i][j]=num;
                pnNumLevelsHist[nNumCollections][j]+=num;
                
                num=aCollectionNode.pnNumLevelsHistVerified[j];
                pnNumLevelsHistVerified[i][j]=num;
                pnNumLevelsHistVerified[nNumCollections][j]+=num;
                
                num=aCollectionNode.pnNumLevelsHistReliableTransitions[j];
                pnNumLevelsHistReliableTransitions[i][j]=num;
                pnNumLevelsHistReliableTransitions[nNumCollections][j]+=num;
            }
            
            for(j=0;j<nNumCurveFeatures;j++){
                dNum=aCollectionNode.pdHistCurveElements[j];
                pdHistCurveElements[i][j]=dNum;
                pdHistCurveElements[nNumCollections][j]=dNum;
                for(k=0;k<nNumCurveFeatures;k++){
                    dNum=aCollectionNode.psHistCurveElementsJoint[j][k];
                    pdHistCurveElementsJoint[j][k]+=dNum;
                    pdHistCurveElementsJoint[k][j]+=dNum;
                }
            }
        }
    }
    void exportSummary(){
        String sSummaryFilePath=FileAssist.changeExt(sListFilePath, "sum");
        String newLine=PrintAssist.newline;
        Formatter fm=FileAssist.getFormatter(sSummaryFilePath);
        PrintAssist.printString(fm,"Level Histogram"+PrintAssist.newline);
        PrintAssist.printString(fm,PrintAssist.newline);
        
        int i,j,k,l,fw=55,fw1=15;
        
        ArrayList<String> names=new ArrayList();
        for(i=0;i<nNumCollections;i++){
            names.add(FileAssist.getFileName(cvInfoCollections.get(i).sID));
        }
        fw=CommonStatisticsMethods.getMaxLength(names);
        
        PrintAssist.printString(fm, "File Name",fw);
        for(i=0;i<nNumCollections;i++){
            PrintAssist.printString(fm, "NumWithLevel"+i,fw1);
        }
        PrintAssist.printString(fm,PrintAssist.newline);
        for(i=0;i<nNumCollections;i++){
            PrintAssist.printString(fm, FileAssist.getFileName(cvInfoCollections.get(i).sID),fw);    
            for(j=0;j<nMaxNumLevels;j++){
                PrintAssist.printString(fm, ""+pnNumLevelsHist[i][j], fw1);
            }
            PrintAssist.printString(fm, newLine);
        }        
        PrintAssist.printString(fm, "Total:",fw);    
        for(j=0;j<nMaxNumLevels;j++){
            PrintAssist.printString(fm, ""+pnNumLevelsHist[nNumCollections][j], fw1);
        }
        PrintAssist.printString(fm, newLine);
        PrintAssist.printString(fm, newLine);
        PrintAssist.printString(fm, newLine);
        
        
        PrintAssist.printString(fm,"In verified tracks"+PrintAssist.newline);
        PrintAssist.printString(fm,PrintAssist.newline);
        PrintAssist.printString(fm, "File Name",fw);
        for(i=0;i<nNumCollections;i++){
            PrintAssist.printString(fm, "NumWithLevel"+i,fw1);
        }
        PrintAssist.printString(fm,PrintAssist.newline);
        for(i=0;i<nNumCollections;i++){
            PrintAssist.printString(fm, FileAssist.getFileName(cvInfoCollections.get(i).sID),fw);    
            for(j=0;j<nMaxNumLevels;j++){
                PrintAssist.printString(fm, ""+pnNumLevelsHistVerified[i][j], fw1);
            }
            PrintAssist.printString(fm, newLine);
        }        
        PrintAssist.printString(fm, "Total:",fw);    
        for(j=0;j<nMaxNumLevels;j++){
            PrintAssist.printString(fm, ""+pnNumLevelsHistVerified[nNumCollections][j], fw1);
        }
        PrintAssist.printString(fm, newLine);
        PrintAssist.printString(fm, newLine);
        PrintAssist.printString(fm, newLine);
        
        
        PrintAssist.printString(fm,"In reliable transition tracks"+PrintAssist.newline);
        PrintAssist.printString(fm,PrintAssist.newline);
        PrintAssist.printString(fm, "File Name",fw);
        for(i=0;i<nNumCollections;i++){
            PrintAssist.printString(fm, "NumWithLevel"+i,fw1);
        }
        PrintAssist.printString(fm,PrintAssist.newline);
        for(i=0;i<nNumCollections;i++){
            PrintAssist.printString(fm, FileAssist.getFileName(cvInfoCollections.get(i).sID),fw);    
            for(j=0;j<nMaxNumLevels;j++){
                PrintAssist.printString(fm, ""+pnNumLevelsHistReliableTransitions[i][j], fw1);
            }
            PrintAssist.printString(fm, newLine);
        }        
        PrintAssist.printString(fm, "Total:",fw);    
        for(j=0;j<nMaxNumLevels;j++){
            PrintAssist.printString(fm, ""+pnNumLevelsHistReliableTransitions[nNumCollections][j], fw1);
        }
        PrintAssist.printString(fm, newLine);
        PrintAssist.printString(fm, newLine);
        PrintAssist.printString(fm, newLine);
        
        double dNum;
        int precision=3;
        
        
        String[] psFeatures=IPOGTLevelInfoNode.psTrackEvaluationElements;
        
        PrintAssist.printString(fm,"Curve feature probabilities:"+PrintAssist.newline);
        PrintAssist.printString(fm,PrintAssist.newline);
        PrintAssist.printString(fm, "File Name",fw);
        for(i=0;i<nNumCurveFeatures;i++){
            PrintAssist.printString(fm, psFeatures[i],fw1);
        }
        PrintAssist.printString(fm,PrintAssist.newline);
        
        for(i=0;i<nNumCollections;i++){
            PrintAssist.printString(fm, FileAssist.getFileName(svTLFFilePathes.get(i)),fw);    
            dNum=(double) cvInfoCollections.get(i).nNumTracks;
            for(j=0;j<nNumCurveFeatures;j++){
                PrintAssist.printNumber(fm,pdHistCurveElements[i][j]/dNum,fw1,precision);
            }
            PrintAssist.printString(fm, newLine);
        } 
        
        PrintAssist.printString(fm, "Total:",fw);    
        dNum=(double) nNumTracks;
        for(j=0;j<nNumCurveFeatures;j++){
            PrintAssist.printNumber(fm,pdHistCurveElements[nNumCollections][j]/dNum,fw1,precision);
        }
        PrintAssist.printString(fm, newLine);
        
        PrintAssist.printString(fm, newLine);
        PrintAssist.printString(fm, newLine);    
        
        
        PrintAssist.printString(fm,"Conditional curve feature probabilities:"+PrintAssist.newline);
        PrintAssist.printString(fm,PrintAssist.newline);
        PrintAssist.printString(fm, "Feature name",fw);
        for(i=0;i<nNumCurveFeatures;i++){
            PrintAssist.printString(fm, psFeatures[i],fw);
        }
        PrintAssist.printString(fm,PrintAssist.newline);
        
        for(i=0;i<nNumCurveFeatures;i++){
            PrintAssist.printString(fm, psFeatures[i],fw);    
            dNum=pdHistCurveElements[nNumCollections][i];
            for(j=0;j<nNumCurveFeatures;j++){
                PrintAssist.printNumber(fm,pdHistCurveElementsJoint[i][j]/dNum,fw1,precision);
            }
            PrintAssist.printString(fm, newLine);
        }     
        
    }
    public void showLevelInfo(){        
        IPOGTLevelInfoCollectionNode aCollectionNode;
        String sSummaryFilePath=FileAssist.changeExt(sListFilePath, "txt");
        String newLine=PrintAssist.newline;
        Formatter fm=FileAssist.getFormatter(sSummaryFilePath);
        int len=cvInfoCollections.size(),i,j,len1;
        ArrayList<IPOGTLevelInfoNode> cvInfos;
        
        String LevelInfo=null;
        StringBuffer names=new StringBuffer(),values=new StringBuffer();
        IPOGTLevelInfoNode cInfoNode=cvInfoCollections.get(0).cvLevelInfos.get(0);
        cInfoNode.cIPOGT.getLevelInfoAsString(names, values, sQuantityName, false);
        names.append("file");
        int lines=0;
        
        PrintAssist.printString(fm, newLine);
        for(i=0;i<len;i++){    
            aCollectionNode=cvInfoCollections.get(i);  
            cInfoNode.cIPOGT.getLevelInfoAsString(names, values, sQuantityName, false);
            PrintAssist.printString(fm, names.toString());            
            PrintAssist.printString(fm, newLine);
            cvInfos=aCollectionNode.cvLevelInfos;
            len1=cvInfos.size();
            for(j=0;j<len1;j++){
                lines++;
                cInfoNode=aCollectionNode.cvLevelInfos.get(j);
                clear(names);
                clear(values);
                cInfoNode.cIPOGT.getLevelInfoAsString(names, values, sQuantityName,false);
                values.append(aCollectionNode.sID);
                PrintAssist.printString(fm, values.toString());
                PrintAssist.printString(fm, newLine);
            }
        }
        fm.close();
        
    }
    void clear(StringBuffer sb){
        int len=sb.length();
        if(len>0) sb.delete(0, len);
    }
}
