/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import ImageAnalysis.ContourFollower;
import utilities.CommonMethods;
import java.util.ArrayList;
import java.awt.*;
import ImageAnalysis.RegionNode;
import ImageAnalysis.RegionComplexNode;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.CommonStatisticsMethods;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.IndexValuePairNode;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.ArrayofArrays.IntArray;
import utilities.BlockDiagonalizer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import utilities.io.ByteConverter;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import utilities.CustomDataTypes.intRange;
import utilities.QuickSort;
import utilities.CustomDataTypes.DoublePair;
import java.util.Formatter;
import utilities.io.PrintAssist;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;
import weka.classifiers.trees.RandomForest;
import utilities.MachineLearning.WekaDriver;
import weka.core.Instance;
import weka.core.Instances;
import java.util.StringTokenizer;

/**
 *
 * @author Taihao
 */
public class IPOGaussianNodeHandler {
    ArrayList<IPOGaussianNode> m_cvIPOGaussianNodes;
    ArrayList<IPOGaussianNodeCluster> m_cvIPOGaussianClusters;
//    ArrayList<IndexValuePairNode>[][] m_pcvIPOAmpNodes;
    int w,h;
    double overlappingCutoff;
    double[][] pnOverlap;
    public IPOGaussianNodeHandler(){
        
    }
    
    static RandomForest IPOGShapeClassifier=null,IPOGContourClassifier=null;
    static Instances IPOGShapeDataset=null,IPOGContourDataset;
    static public void loadIPOGShapeContourClassifier(){
        ArrayList<RandomForest> classifiers=new ArrayList();
        ArrayList<Instances> cvDatasets=new ArrayList();
        WekaDriver.getIPOGClassifiers(classifiers,cvDatasets);
        IPOGShapeClassifier=classifiers.get(0);
        IPOGContourClassifier=classifiers.get(1);
        IPOGShapeDataset=cvDatasets.get(0);
        IPOGContourDataset=cvDatasets.get(1);
    }
    
    public IPOGaussianNodeHandler(ArrayList<RegionNode> cvRegionNodes,ArrayList<RegionComplexNode> cvComplexNodes, double overlappingCutoff,int w, int h){
        m_cvIPOGaussianNodes=buildIPOGaussianNodes(cvRegionNodes,cvComplexNodes);
        this.w=w;
        this.h=h;
        updateIPOGaussianNodes(m_cvIPOGaussianNodes);
    }
    public ArrayList<IPOGaussianNode> getIPONodes(){
        return m_cvIPOGaussianNodes;
    }
    public static ArrayList<IPOGaussianNode> buildIPOGaussianNodes(ArrayList<RegionNode> cvRegionNodes,ArrayList<RegionComplexNode> cvComplexNodes){
        RegionNode rNode;
        RegionComplexNode cNode;
        int i,len=cvRegionNodes.size(),j;
        ArrayList<IPOGaussianNode> IPOs=new ArrayList();

        for(i=0;i<len;i++){
            rNode=cvRegionNodes.get(i);
            if(rNode.complexIndex>=0) continue;//complexIndex starts from 0, but regionIndex starts from 1
            if(rNode.fittingModel==null) continue;
            if(rNode.fittingModel.invalid)continue;
            buildIPOGaussianNode(IPOs,rNode.fittingModel,rNode.regionIndex,-1);
        }
        len=cvComplexNodes.size();

        for(i=0;i<len;i++){
            cNode=cvComplexNodes.get(i);
            if(cNode.cIS.contains(165,163)){
                i=i;
            }
            if(cNode.fittingModel==null) continue;
            if(cNode.fittingModel.invalid)continue;
            buildIPOGaussianNode(IPOs,cNode.fittingModel,-1,cNode.complexIndex);
        }
        return IPOs;
    }
    void setOverlapCutoff(double cutoff){
        overlappingCutoff=cutoff;
    }
    public int updateIPOGaussianNodes(ArrayList<IPOGaussianNode> IPOs){
        if(IPOs==null) return -1;
        if(m_cvIPOGaussianNodes!=null) {
//            if(m_cvIPOGaussianNodes!=IPOs)m_cvIPOGaussianNodes.clear();
        }
        m_cvIPOGaussianNodes=IPOs;
        int len,i,j;

        len=m_cvIPOGaussianNodes.size();
        IPOGaussianNode IPO,IPO1,IPO2;
        pnOverlap=new double[len][len];
        for(i=0;i<len;i++){
            IPO=m_cvIPOGaussianNodes.get(i);
            IPO.cvIndexesInCluster.clear();
            IPO.cvNeighboringIPOs.clear();
            IPO.cvOverlaps.clear();
        }
        for(i=0;i<len;i++){
            IPO1=m_cvIPOGaussianNodes.get(i);
            for(j=i+1;j<len;j++){
                IPO2=m_cvIPOGaussianNodes.get(j);
                buildIPOGaussianNodeOverlaps(IPO1,IPO2);
            }
        }
        buildIPOClusters();
        return 1;
    }

    void buildIPOClusters(){
        ArrayList<IntArray> viaRowIndexes=new ArrayList();
        ArrayList<IntArray> viaColumnIndexes=new ArrayList();
        int len=m_cvIPOGaussianNodes.size(),i,len1,j;
        int[][] pnOverlappingMT=new int[len][len];
        CommonStatisticsMethods.setElements(pnOverlappingMT, 0);
        m_cvIPOGaussianClusters=new ArrayList();
        IPOGaussianNodeCluster cluster;

        IPOGaussianNode IPO;
        for(i=0;i<len;i++){
            pnOverlappingMT[i][i]=1;
            IPO=m_cvIPOGaussianNodes.get(i);
            len1=IPO.cvNeighboringIPOs.size();
            for(j=0;j<len1;j++){
                pnOverlappingMT[i][IPO.cvNeighboringIPOs.get(j).IPOIndex]=1;
            }
        }
        BlockDiagonalizer bd=new BlockDiagonalizer();
        bd.blockdiagonalizeIntMatrix(pnOverlappingMT, viaRowIndexes, viaColumnIndexes);
        IPO=null;
        len=viaRowIndexes.size();
        int index;
        ArrayList<Integer> indexes;
        for(i=0;i<len;i++){
            indexes=viaRowIndexes.get(i).m_intArray;
            if(CommonMethods.containsContent(indexes, 3)){
                i=i;
            }
            ArrayList<IPOGaussianNode> IPOs=new ArrayList();
            len1=indexes.size();
            for(j=0;j<len1;j++){
                index=indexes.get(j);
                IPO=m_cvIPOGaussianNodes.get(index);
                IPO.cvIndexesInCluster=indexes;
                IPOs.add(IPO);
            }
            cluster=new IPOGaussianNodeCluster(IPOs);
            cluster.cIndex=i;
            m_cvIPOGaussianClusters.add(cluster);
        }
    }
    public ArrayList <IPOGaussianNodeCluster> getIPOClusters(){
        return m_cvIPOGaussianClusters;
    }
    public static void buildIPOGaussianNode(ArrayList<IPOGaussianNode> IPOs, FittingModelNode aModel, int rIndex, int cIndex){
        aModel.toGaussian2D_GaussianPars();
        IPOGaussianNode IPO;
        int len=IPOs.size(),i,j,len1,index;
        double pdPars[]=aModel.pdPars, cnst,td;
        cnst=pdPars[0];
        int num,nPars=1;
        index=len-1;
        for(i=0;i<aModel.nComponents;i++){
            num=aModel.nvNumParameters.get(i);
            if(aModel.svFunctionTypes.get(i).startsWith("gaussian2D_")){
                index++;
                IPO=new IPOGaussianNode();
                IPO.pdPars=new double[num+1];
                IPO.pdPars[0]=cnst;
                for(j=0;j<num;j++){
                    IPO.pdPars[j+1]=pdPars[nPars+j];
                }
                IPO.IPOIndex=index;
                IPO.cnst=cnst;
                IPO.Amp=pdPars[nPars];
                nPars++;
                IPO.sigmax=pdPars[nPars];
                nPars++;
                IPO.sigmay=pdPars[nPars];
                nPars++;
                td=pdPars[nPars]*180/Math.PI;
                td-=(int)(td/180);
                IPO.thetaDegree=td;
                nPars++;
                IPO.xc=pdPars[nPars];
                nPars++;
                IPO.yc=pdPars[nPars];
                nPars++;
                IPO.cIndex=cIndex;
                if(aModel==null){
                    aModel=aModel;
                }else if(aModel.rIndexes==null&&cIndex>=0){
                    i=i;
                }
                if(cIndex>=0) rIndex=aModel.rIndexes.get(i);//this is a complex
                IPO.rIndex=rIndex;
                IPO.xcr=-1;
                IPO.ycr=-1;
                buildIPOGVarRanges(IPO);
                IPOs.add(IPO);
                IPO.dTotalSignalCal=2.*IPO.Amp*IPO.sigmax*IPO.sigmay*Math.PI;
                if(aModel.bvFitted==null){
                    IPO.converged=aModel.bConverged;
                }else{
                    IPO.converged=aModel.bvFitted.get(i);
                }
            }else{
                nPars+=num;
            }
        }
    }
    public static void buildIPOGaussianNodeArray(ArrayList<IPOGaussianNode> IPOs, ArrayList<double[]> pdvPars, ArrayList<double[]> pdvAdditionalPars){
        IPOGaussianNode IPO;
        int len=pdvPars.size(),i,j,len1,index;
        int TrackIndex,BundleIndex;
        int sign=1;
        int nNumAddParsPerIPO=0,nNumAddPars=0;

        double pdPars[], td, pdAddPars[];
//        index=IPOs.size();
        for(i=0;i<len;i++){
            if(i==0)nNumAddParsPerIPO=pdvAdditionalPars.get(0).length;
                IPO=new IPOGaussianNode();
                pdPars=pdvPars.get(i);
                IPO.pdPars=pdPars;
//                IPO.IPOIndex=index;
                IPO.cnst=pdPars[0];
                IPO.Amp=pdPars[1];
                IPO.sigmax=pdPars[2];
                IPO.sigmay=pdPars[3];
                td=pdPars[4]*180/Math.PI;
                td-=(int)(td/180);
                IPO.thetaDegree=td;
                IPO.xc=pdPars[5];
                IPO.yc=pdPars[6];


                IPO.cIndex=-1;
                IPO.rIndex=-1;
                buildIPOGVarRanges(IPO);
                IPOs.add(IPO);
                IPO.dTotalSignalCal=2.*IPO.Amp*IPO.sigmax*IPO.sigmay*Math.PI;
                IPO.converged=true;

                nNumAddPars=0;
                if(nNumAddPars==nNumAddParsPerIPO) continue;
                pdAddPars=pdvAdditionalPars.get(i);

                sign=1;
                if(pdAddPars[0]<0) sign=-1;
                IPO.IPOIndex=(int)(pdAddPars[0]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[1]<0) sign=-1;
                IPO.sliceIndex=(int)(pdAddPars[1]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[2]<0) sign=-1;
                IPO.rIndex=(int)(pdAddPars[2]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[3]<0) sign=-1;
                IPO.cIndex=(int)(pdAddPars[3]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[4]<0) sign=-1;
                IPO.xcr=(int)(pdAddPars[4]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[5]<0) sign=-1;
                IPO.ycr=(int)(pdAddPars[5]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[6]<0) sign=-1;
                IPO.TrackIndex=(int)(pdAddPars[6]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[7]<0) sign=-1;
                IPO.BundleIndex=(int)(pdAddPars[7]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                IPO.dBackground=pdAddPars[8];
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                IPO.dTotalSignal=pdAddPars[9];
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[11]<0) sign=-1;
                IPO.area=(int)(pdAddPars[10]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                IPO.preOvlp=pdAddPars[11];
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[12]<0) sign=-1;
                IPO.preRid=(int)(pdAddPars[12]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                IPO.postOvlp=pdAddPars[13];
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                sign=1;
                if(pdAddPars[14]<0) sign=-1;
                IPO.postRid=(int)(pdAddPars[14]+.5*sign);
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                IPO.dBundleTotalSignal=pdAddPars[15];
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                IPO.peak1=pdAddPars[16];
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;

                IPO.peak3=pdAddPars[17];
                nNumAddPars++;
                if(nNumAddPars==nNumAddParsPerIPO) continue;
         }
    }
    int buildIPOGaussianNodeOverlaps(IPOGaussianNode IPO1, IPOGaussianNode IPO2){
        DoubleRange xRange=IPO1.cXRange2.getOverlapRange(IPO2.cXRange2);
        DoubleRange yRange=IPO1.cYRange2.getOverlapRange(IPO2.cYRange2);
        if(!xRange.isValid()) return -1;
        if(!yRange.isValid()) return -1;
        xRange=IPO1.cXRange3.getOverlapRange(IPO2.cXRange3);
        yRange=IPO1.cYRange3.getOverlapRange(IPO2.cYRange3);
        int iI=(int) (yRange.getMin()+0.5), iF=(int)(yRange.getMax()+0.5),jI=(int)(xRange.getMin()+0.5),jF=(int)(xRange.getMax()+0.5);
        int i,j;
        ArrayList<String> sv1=new ArrayList(), sv2=new ArrayList();
        sv1.add(IPO1.sType);
        sv2.add(IPO2.sType);
        ComposedFittingFunction fun1=new ComposedFittingFunction(sv1);
        ComposedFittingFunction fun2=new ComposedFittingFunction(sv2);
        double pdX[]=new double[2];
        double[] pd1=IPO1.pdPars, pd2=IPO2.pdPars;
        double y1,y2,overlap=0,y;
        double cnst1=IPO1.cnst,cnst2=IPO2.cnst;
        for(i=iI;i<=iF;i++){
            pdX[1]=i;
            for(j=jI;j<=jF;j++){
                pdX[0]=j;
                y1=fun1.fun(pd1, pdX)-cnst1;
                y2=fun2.fun(pd2, pdX)-cnst2;
                y=Math.sqrt(y1*y2);
                overlap+=y;
            }
        }
        overlap/=Math.sqrt(IPO1.dTotalSignal*IPO2.dTotalSignal);//Bhattacharyya coefficient
        pnOverlap[IPO1.IPOIndex][IPO2.IPOIndex]=overlap;
        pnOverlap[IPO2.IPOIndex][IPO1.IPOIndex]=overlap;
        int id1=IPO1.IPOIndex,id2=IPO2.IPOIndex;
        if(id1==273&&id2==417){
//            id1=id1;
        }
        if(overlap<overlappingCutoff) return -1;
        IPO1.cvNeighboringIPOs.add(IPO2);
        IPO2.cvNeighboringIPOs.add(IPO1);
        IPO1.cvOverlaps.add(new IndexValuePairNode(id2,overlap));
        IPO2.cvOverlaps.add(new IndexValuePairNode(id1,overlap));
        return 1;
    }
    public static void buildIPOGVarRanges(ArrayList<IPOGaussianNode> IPOs){
        int i,len=IPOs.size();
        for(i=0;i<len;i++){
            buildIPOGVarRanges(IPOs.get(i));
        }
    }
    public static int buildIPOGVarRanges(IPOGaussianNode IPO){
        if(IPO==null) return -1;
        int i,len;
        if(IPO instanceof IPOGaussianNodeComplex||IPO instanceof IPOGBundleNode){
            DoubleRange xRange2=new DoubleRange(),xRange3=new DoubleRange(),yRange2=new DoubleRange(),yRange3=new DoubleRange();
            IPOGaussianNode IPOG;
            ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
            IPO.getSimpleIPOGs(IPOGs);
            len=IPOGs.size();
            for(i=0;i<len;i++){
                IPOG=IPOGs.get(i);
                buildIPOGVarRanges(IPOG);
                xRange2.expandRange(IPOG.cXRange2);
                xRange3.expandRange(IPOG.cXRange3);
                yRange2.expandRange(IPOG.cYRange2);
                yRange3.expandRange(IPOG.cYRange3);
            }
            IPO.cXRange2=xRange2;
            IPO.cXRange3=xRange3;
            IPO.cYRange2=yRange2;
            IPO.cYRange3=yRange3;
            return 1;
        }
        ArrayList<Integer> nvT=new ArrayList();
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction.toGaussian2D(IPO.pdPars, svT, nvT);
        double Amp=IPO.pdPars[1],a=IPO.pdPars[2],b=IPO.pdPars[3],c=IPO.pdPars[4];
        double z2=0.1353*Amp,z3=Amp*0.0111;//value at two and three sigma away from the peak, respectively
        double D2=(c*Math.log(z2/Amp))/(b*b-a*c),D3=(c*Math.log(z3/Amp))/(b*b-a*c);
        double dx=Math.sqrt(D2);
        IPO.cXRange2=new DoubleRange(IPO.xc-dx,IPO.xc+dx);
        dx=Math.sqrt(D3);
        IPO.cXRange3=new DoubleRange(IPO.xc-dx,IPO.xc+dx);

        D2=(a*Math.log(z2/Amp))/(b*b-a*c);
        D3=(a*Math.log(z3/Amp))/(b*b-a*c);
        double dy=Math.sqrt(D2);
        IPO.cYRange2=new DoubleRange(IPO.yc-dy,IPO.yc+dy);
        dy=Math.sqrt(D3);
        IPO.cYRange3=new DoubleRange(IPO.yc-dy,IPO.yc+dy);
        if(nvT.size()>0) ComposedFittingFunction.toGaussian2D_GaussianPars(IPO.pdPars, svT, nvT);
        return 1;
    }
    
    public static boolean isEdgeIPOG2(IPOGaussianNode IPOG, int w, int h){
        if(IPOG.cXRange2.getMin()<0.5) return true;
        if(IPOG.cYRange2.getMin()<0.5) return true;
        if(IPOG.cXRange2.getMax()>w-1.5) return true;
        if(IPOG.cYRange2.getMax()<h-1.5) return true;
        return false;
    }

    public static boolean isEdgeIPOG3(IPOGaussianNode IPOG, int w, int h){
        if(IPOG.cXRange3.getMin()<0.5) return true;
        if(IPOG.cYRange3.getMin()<0.5) return true;
        if(IPOG.cXRange3.getMax()>w-1.5) return true;
        if(IPOG.cYRange3.getMax()<h-1.5) return true;
        return false;
    }

    public static String[][] getIPOsAsStrings0(ArrayList<IPOGaussianNodeComplex> cvIPOGs){
        IPOGaussianNode IPO;
        IPOGaussianNodeComplex IPOGc;
        ArrayList<String> svParNames=new ArrayList(), svParValues=new ArrayList();
        ArrayList<String[]> psvData=new ArrayList(),psvt=new ArrayList();
        int i,j,nNumPars=svParNames.size(),nIPOs=cvIPOGs.size();
        String[][] ppsPars;
        if(cvIPOGs.isEmpty()){
            IPO=new IPOGaussianNode();
            IPO.getParsAsStrings(svParNames, svParValues);
            nNumPars=svParNames.size();
            ppsPars=new String[2][nNumPars+1];
            ppsPars[0][0]="Row";
            for(i=0;i<nNumPars;i++){
                ppsPars[0][i+1]=svParNames.get(i);
                ppsPars[0][i+1]="empty";
            }
            return ppsPars;
        }
        for(i=0;i<nIPOs;i++){
            IPO=cvIPOGs.get(i);
            if(IPO==null) continue;
            if(!(IPO instanceof IPOGaussianNode)) 
                IPOGc=new IPOGaussianNodeComplex(IPO);
            else
                IPOGc=(IPOGaussianNodeComplex) IPO;
            IPOGc.getParsAsStringArrayList(psvt);
            if(i==0) psvData.add(psvt.get(0));
            for(j=1;j<psvt.size();j++){
                psvData.add(psvt.get(j));
            }
       }
       ppsPars=new String[psvData.size()][];
       for(i=0;i<psvData.size();i++){
           ppsPars[i]=psvData.get(i);
           if(i>0) ppsPars[i][0]=""+i;
       }
       return ppsPars;
    }
    public static String[][] getIPOsAsStringsComplex(ArrayList<IPOGaussianNodeComplex> cvIPOGs){
        IPOGaussianNode IPO;
        IPOGaussianNodeComplex IPOGc;
        ArrayList<String> svParNames=new ArrayList(), svParValues=new ArrayList();
        ArrayList<String[]> psvData=new ArrayList(),psvt=new ArrayList();
        int i,j,nNumPars=svParNames.size(),nIPOs=cvIPOGs.size();
        String[][] ppsPars;
        if(cvIPOGs.isEmpty()){
            IPO=new IPOGaussianNode();
            IPO.getParsAsStrings(svParNames, svParValues);
            nNumPars=svParNames.size();
            ppsPars=new String[2][nNumPars+1];
            ppsPars[0][0]="Row";
            for(i=0;i<nNumPars;i++){
                ppsPars[0][i+1]=svParNames.get(i);
                ppsPars[0][i+1]="empty";
            }
            return ppsPars;
        }
        for(i=0;i<nIPOs;i++){
            IPO=cvIPOGs.get(i);
            if(IPO==null) continue;
            if(!(IPO instanceof IPOGaussianNode)) 
                IPOGc=new IPOGaussianNodeComplex(IPO);
            else
                IPOGc=(IPOGaussianNodeComplex) IPO;
            IPOGc.getParsAsStringArrayList(psvt);
            if(i==0) psvData.add(psvt.get(0));
            for(j=1;j<psvt.size();j++){
                psvData.add(psvt.get(j));
            }
       }
       ppsPars=new String[psvData.size()][];
       for(i=0;i<psvData.size();i++){
           ppsPars[i]=psvData.get(i);
           if(i>0) ppsPars[i][0]=""+i;
       }
       return ppsPars;
    }
    public static String[][] getIPOsAsStrings(ArrayList<IPOGaussianNode> cvIPOGs, boolean simpleIPOGs){
        ArrayList<IPOGaussianNode> IPOs=cvIPOGs;
        boolean normal;
        if(simpleIPOGs) IPOs=IPOGaussianNodeHandler.getSimpleIPOGs(cvIPOGs);
        
        ArrayList<String> svParNames=new ArrayList(), svParValues=new ArrayList();
        IPOGaussianNode IPO;
        boolean empty=false;
        if(IPOs.isEmpty()){
            IPO=new IPOGaussianNode();
            empty=true;
        }else{
            IPO=IPOs.get(0);
        }
        IPO.getParsAsStrings(svParNames, svParValues);
        int nNumPars=svParNames.size(),nNumIPOs=IPOs.size();
        String[][] ppsPars=new String[nNumIPOs+1][nNumPars+1];
        int i,j;
        if(empty){
            ppsPars=new String[nNumIPOs+1][nNumPars+1];
            ppsPars[0][0]="Row";
            for(i=0;i<nNumPars;i++){
                ppsPars[0][i+1]=svParNames.get(i);
            }
            return ppsPars;
        }
        for(i=0;i<nNumPars;i++){
            ppsPars[0][i+1]=svParNames.get(i);
            ppsPars[1][i+1]=svParValues.get(i);
        }
        ppsPars[0][0]="Row";
        ppsPars[1][0]=""+1;
        for(i=1;i<nNumIPOs;i++){
            IPO=IPOs.get(i);
            if(IPO==null) continue;
            IPO.getParsAsStrings(svParNames, svParValues);
            for(j=0;j<nNumPars;j++){
                ppsPars[i+1][j+1]=svParValues.get(j);
            }
            ppsPars[i+1][0]=""+(i+1);
        }
        return ppsPars;
    }
    public static ArrayList<Point> getIPOCenters(ArrayList<IPOGaussianNode> IPOs){
        ArrayList<Point> points=new ArrayList();
        int i,len=IPOs.size();
        for(i=0;i<len;i++){
            points.add(getIPOLocation(IPOs.get(i)));
        }
        return points;
    }
    public static ArrayList<Point> getIPOContour2(ArrayList<IPOGaussianNode> IPOs){//TODO: need to fix it. 12725
        ArrayList<Point> points=new ArrayList(),contour;
        int i,len=IPOs.size(),j,len1;
        IPOGaussianNode IPO;
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            contour=buildIPOContour(IPO,IPO.Amp*0.1353);
            len1=contour.size();
            for(j=0;j<len1;j++){
                points.add(contour.get(j));
            }
        }
        return points;
    }
    public static ArrayList<Point> getIPOContour3(ArrayList<IPOGaussianNode> IPOs){//TODO: need to fix it. 12725
        ArrayList<Point> points=new ArrayList(),contour;
        int i,len=IPOs.size(),j,len1;
        IPOGaussianNode IPO;
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            contour=buildIPOContour(IPO,IPO.Amp*0.0111);
            len1=contour.size();
            for(j=0;j<len1;j++){
                points.add(contour.get(j));
            }
        }
        return points;
    }
    public static ArrayList<Point> getIS2(ArrayList<IPOGaussianNode> IPOs,int w, int h){
        ArrayList<Point> points=new ArrayList(),contour,points1=new ArrayList();
        ImageShape cIS=null,cISt;
        int i,len=IPOs.size(),j,len1;
        IPOGaussianNode IPO;
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            contour=buildIPOContour(IPO,IPO.Amp*0.1353);
            cISt=ImageShapeHandler.buildImageShape(contour);
            if(i==0) 
                cIS=cISt;
            else
                cIS.mergeShape(cISt);
            cIS.getInnerPoints(points1);
        }
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cIS.getInnerPointsY(points);
        return points;
    }
    public static ArrayList<Point> getIS3(ArrayList<IPOGaussianNode> IPOs,int w, int h){
        ArrayList<Point> points=new ArrayList(),contour,points1=new ArrayList();
        ImageShape cIS=null,cISt;
        int i,len=IPOs.size(),j,len1;
        IPOGaussianNode IPO;
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            contour=buildIPOContour(IPO,IPO.Amp*0.0111);
            cISt=ImageShapeHandler.buildImageShape(contour);
            if(i==0) 
                cIS=cISt;
            else
                cIS.mergeShape(cISt);
            cIS.getInnerPoints(points1);
        }
        cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        cIS.getInnerPointsY(points);
        return points;
    }
    public static Point getIPOLocation(IPOGaussianNode IPO){
        int x=(int)(IPO.xc+0.5),y=(int)(IPO.yc+0.5);
        return new Point(x,y);
    }
    public static ArrayList<Point> buildIPOContour(IPOGaussianNode IPO, double z0){
        ArrayList<Integer> nvT=new ArrayList();
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction.toGaussian2D(IPO.pdPars, svT, nvT);
        double Amp=IPO.pdPars[1],a=IPO.pdPars[2],b=IPO.pdPars[3],c=IPO.pdPars[4];
        double A,B,C;
        double dx,dy1,dy2,xc=IPO.pdPars[5],yc=IPO.pdPars[6];
        int xi=(int)IPO.cXRange2.getMin()-1,xf=(int)IPO.cXRange2.getMax()+1;
        int x,y,nDy;
        ArrayList<Point> points=new ArrayList(),upperLine=new ArrayList(), lowerLine=new ArrayList();
        ComposedFittingFunction fun=new ComposedFittingFunction(svT);
        double[] pdX=new double[2];
        double Y1,Y2,dt;
        Point p1,p2;
        for(x=xi;x<=xf;x++){
            dx=x-xc;
            A=c;
            B=2*dx*b;
            C=a*dx*dx+Math.log(z0/Amp);
            if((B*B-4*A*C)<0){
                continue;
            }

            dy1=(Math.sqrt(B*B-4*A*C)-B)/(2*A);
            dy2=(-Math.sqrt(B*B-4*A*C)-B)/(2*A);

            if(dy1<dy2){
                dt=dy1;
                dy1=dy2;
                dy2=dt;
            }
            upperLine.add(new Point(x,(int)(yc+dy2)));
            lowerLine.add(new Point(x,(int)(yc+dy1)+1));
        }
        p1=new Point(lowerLine.get(0));
        p1.translate(-1, 0);
        p2=new Point(upperLine.get(0));
        p2.translate(-1, 0);
        x=p1.x;
        for(y=p1.y-1;y>=p2.y;y--){
            points.add(new Point(x,y));
        }
        int len=upperLine.size(),i,delta=1;
        p1=upperLine.get(0);
        points.add(p1);
        int y1=p1.y,y2;
        for(i=1;i<=len;i++){
            if(i<len)
                p2=upperLine.get(i);
            else {
                p2=new Point(p2);
                p2.translate(1, 0);
            }
            y2=p2.y;
            delta=1;
            if(y2<y1) delta=-1;
            x=p1.x;
            for(y=y1+delta;delta*(y-y2)<=0;y+=delta){
                points.add(new Point(x,y));
            }
            p1=p2;
            y1=p1.y;
            points.add(p1);
        }

        p2=new Point(lowerLine.get(len-1));
        p2.translate(1, 0);
        y1=p1.y;
        y2=p2.y;
        x=p1.x;
        for(y=y1+1;y<=y2;y++){
            points.add(new Point(x,y));
        }
        p1=lowerLine.get(len-1);
        points.add(p1);
        y1=p1.y;
        for(i=len-2;i>=-1;i--){
            if(i>=0)
                p2=lowerLine.get(i);
            else {
                p2=new Point(p2);
                p2.translate(-1, 0);
            }
            y2=p2.y;
            delta=1;
            if(y2<y1) delta=-1;
            x=p1.x;
            for(y=y1+delta;delta*(y-y2)<=0;y+=delta){
                points.add(new Point(x,y));
            }
            p1=p2;
            y1=p1.y;
            points.add(p1);
        }
        
        if(nvT.size()>0) ComposedFittingFunction.toGaussian2D_GaussianPars(IPO.pdPars, svT, nvT);
        return points;
    }
    public static ImageShape buildIPOShape(IPOGaussianNode IPO, double z0){
        ArrayList<Point> contour=buildIPOContour(IPO,z0);
        ImageShape cIS=ImageShapeHandler.buildImageShape(contour);
        return cIS;
    }
    public static void exportIPOs (DataOutputStream df, ArrayList<IPOGaussianNode> IPOs,int nNumAdditinalParsPerIPO) throws IOException {
        int i,len=IPOs.size(),o;
        IPOGaussianNode IPO;
        int nNumAddPars=0;
        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            df.writeDouble(IPO.cnst);
            df.writeDouble(IPO.Amp);
            df.writeDouble(IPO.sigmax);
            df.writeDouble(IPO.sigmay);
            df.writeDouble(IPO.thetaDegree*Math.PI/180);
            df.writeDouble(IPO.xc);
            df.writeDouble(IPO.yc);

            nNumAddPars=0;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.IPOIndex);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.sliceIndex);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.rIndex);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.cIndex);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.xcr);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.ycr);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.TrackIndex);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.BundleIndex);
            nNumAddPars++;

            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;
            df.writeDouble(IPO.dBackground);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.dTotalSignal);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.area);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.preOvlp);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.preRid);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.postOvlp);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.postRid);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.dBundleTotalSignal);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.peak1);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;

            df.writeDouble(IPO.peak3);
            nNumAddPars++;
            if(nNumAddPars==nNumAdditinalParsPerIPO) continue;
        }
    }
    public static int importIPOs(BufferedInputStream bf, ArrayList<IPOGaussianNode> IPOs, int nIPOs, int nAdditionalParsPerIPO) throws IOException{
        int nIPOsPerInput=200;
        int nParsPerIPO=7,bytesPerDouble=8;
        int nTotalParsPerInput=nIPOsPerInput*(nParsPerIPO+nAdditionalParsPerIPO);
        double[] pdPars=new double[nTotalParsPerInput];
        byte[] pbT=new byte[nTotalParsPerInput*bytesPerDouble];
        int num=0,numToRead=nIPOs-num,IPOsThisTime,bytesThisTime,doublesThisTime;
        int bytesRead;
        while(numToRead>0){
            IPOsThisTime=Math.min(numToRead, nIPOsPerInput);
            doublesThisTime=IPOsThisTime*(nParsPerIPO+nAdditionalParsPerIPO);
            bytesThisTime=doublesThisTime*bytesPerDouble;
            bytesRead=bf.read(pbT, 0, bytesThisTime);
            if(bytesRead!=bytesThisTime){
                return -1;
            }
            ByteConverter.getDoubleArray(pbT, 0, bytesThisTime, pdPars, 0, doublesThisTime);
            buildIPOs(IPOs,pdPars,IPOsThisTime,nAdditionalParsPerIPO);
            num+=IPOsThisTime;
            numToRead=nIPOs-num;
        }
        return 1;
//        bf.read(pbT);
    }
    public static void buildIPOs(ArrayList<IPOGaussianNode> IPOs, double[] pdPars, int nIPOs, int nAdditionalParsPerIPO){
        int i,j,numPars=0,nParsPerIPO=7;
        double[] pdT,pdTAdd;
        ArrayList<double[]> pdvPars=new ArrayList(),pdvAddPars=new ArrayList();
        for(i=0;i<nIPOs;i++){
            pdT=new double[nParsPerIPO];
            pdTAdd=new double[nAdditionalParsPerIPO];
            for(j=0;j<nParsPerIPO;j++){
                pdT[j]=pdPars[numPars+j];
            }
            pdvPars.add(pdT);
            numPars+=nParsPerIPO;
            for(j=0;j<nAdditionalParsPerIPO;j++){
                pdTAdd[j]=pdPars[numPars+j];
            }
            pdvAddPars.add(pdTAdd);
            numPars+=nAdditionalParsPerIPO;
        }
        buildIPOGaussianNodeArray(IPOs,pdvPars,pdvAddPars);
    }
    public static void getCanvusPixels(int[][] pixelsCanvus, int background, ArrayList<IPOGaussianNode> IPOs){
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction func=new ComposedFittingFunction(svT);
        int len=IPOs.size(),i;
        for(i=0;i<len;i++){
            getCanvusPixels(pixelsCanvus,background,IPOs.get(i));
        }
    }
    public static void getCanvusPixels_Progressive(int[][] pixelsCanvus, double[][] pixelsTotalSignal, ArrayList<IPOGaussianNode> IPOs){
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction func=new ComposedFittingFunction(svT);
        int len=IPOs.size(),i;
        for(i=0;i<len;i++){
            getCanvusPixels_Progressive(pixelsCanvus,pixelsTotalSignal,IPOs.get(i),func);
        }
    }
    public static void getSuperImposition(int[][] pnPixels,ArrayList<IPOGaussianNode> IPOGs){
        getSuperImposition(pnPixels,IPOGs,1);
    }
    public static void getSuperImposition(int[][] pnPixels,ArrayList<IPOGaussianNode> IPOGs,int sign){
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            getSuperImposition(pnPixels,IPOGs.get(i),sign);
        }
    }
    public static void getSuperImposition(double[][] pdPixels,ArrayList<IPOGaussianNode> IPOs){
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction func=new ComposedFittingFunction(svT);
        int len=IPOs.size(),i;
        for(i=0;i<len;i++){
            getSuperImposition(pdPixels,IPOs.get(i),func);
        }
    }
    public static int getRanges3(ArrayList<IPOGaussianNode> IPOGs, intRange xRange, intRange yRange){
        xRange.resetRange();
        yRange.resetRange();
        IPOGaussianNode IPOG;
        int i,len=IPOGs.size();
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            xRange.expandRange((int)(IPOG.cXRange3.getMin()+0.5));
            xRange.expandRange((int)(IPOG.cXRange3.getMax()+0.5));
            yRange.expandRange((int)(IPOG.cYRange3.getMin()+0.5));
            yRange.expandRange((int)(IPOG.cYRange3.getMax()+0.5));
        }
        return 1;
    }
    public static int getSuperImposition(int[][] pixels,IPOGaussianNode IPO){
        return getSuperImposition(pixels,IPO,1);
    }
    public static int getSuperImposition(int[][] pixels,IPOGaussianNode IPO, int sign){
        int i,len;
        if(IPO==null){
            return -1;
        }
        if(IPO instanceof IPOGaussianNodeComplex){
            IPOGaussianNodeComplex IPOt=(IPOGaussianNodeComplex)IPO;
            len=IPOt.IPOGs.size();
            for(i=0;i<len;i++){
                getSuperImposition(pixels,IPOt.IPOGs.get(i));
            }
            return 1;
        }
        if(IPO instanceof IPOGBundleNode){
            IPOGBundleNode IPOt=(IPOGBundleNode)IPO;
            len=IPOt.IPOGs.size();
            for(i=0;i<len;i++){
                getSuperImposition(pixels,IPOt.IPOGs.get(i));
            }
            return 1;
        }
        int w=pixels[0].length,h=pixels.length;
        int iI=(int)(IPO.cYRange3.getMin()+0.5),iF=(int)(IPO.cYRange3.getMax()+0.5);
        int jI=(int)(IPO.cXRange3.getMin()+0.5),jF=(int)(IPO.cXRange3.getMax()+0.5);
        iI=Math.max(0, iI);
        iF=Math.min(iF, h-1);
        jI=Math.max(0, jI);
        jF=Math.min(jF, w-1);
        int j;
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        ComposedFittingFunction func=new ComposedFittingFunction(svT);
        double pdPars[]=IPO.pdPars;
        double[] pdX=new double[2];
        double cnst=IPO.cnst;
        for(i=iI;i<=iF;i++){
            pdX[1]=i;
            for(j=jI;j<jF;j++){
                pdX[0]=j;
                pixels[i][j]+=(int)((func.fun(pdPars, pdX)+0.5-cnst)*sign);
            }
        }
        return 1;
    }
    public static void getSuperImposition(double[][] pixels,IPOGaussianNode IPO,ComposedFittingFunction func){
        int w=pixels[0].length,h=pixels.length;
        int iI=(int)(IPO.cYRange3.getMin()+0.5),iF=(int)(IPO.cYRange3.getMax()+0.5);
        int jI=(int)(IPO.cXRange3.getMin()+0.5),jF=(int)(IPO.cXRange3.getMax()+0.5);
        iI=Math.max(0, iI);
        iF=Math.min(iF, h-1);
        jI=Math.max(0, jI);
        jF=Math.min(jF, w-1);
        int i,j;
        ArrayList<String> svT=new ArrayList();
        svT.add("gaussian2D_GaussianPars");
        double pdPars[]=IPO.pdPars;
        double[] pdX=new double[2];
        double cnst=IPO.cnst;
        for(i=iI;i<=iF;i++){
            pdX[1]=i;
            for(j=jI;j<jF;j++){
                pdX[0]=j;
                pixels[i][j]+=(func.fun(pdPars, pdX)-cnst);
            }
        }
    }

    public static void getCanvusPixels(int[][] pixelsCanvus,int background, IPOGaussianNode IPO){
        int w=pixelsCanvus[0].length,h=pixelsCanvus.length;
        int iI=(int)(IPO.cYRange3.getMin()+0.5),iF=(int)(IPO.cYRange3.getMax()+0.5);
        int jI=(int)(IPO.cXRange3.getMin()+0.5),jF=(int)(IPO.cXRange3.getMax()+0.5);
        iI=Math.max(0, iI);
        iF=Math.min(iF, h-1);
        jI=Math.max(0, jI);
        jF=Math.min(jF, w-1);
        int i,j,pixel;
        int cnst=(int)(IPO.cnst+0.5);
        for(i=iI;i<=iF;i++){
            for(j=jI;j<jF;j++){
                pixel=pixelsCanvus[i][j];
                if(pixel==background) pixel=cnst;
                pixelsCanvus[i][j]=pixel;
            }
        }
    }
    public static void getCanvusPixels_Progressive(int[][] pixelsCanvus,double[][] pixelsTotalSignal, IPOGaussianNode IPO, ComposedFittingFunction func){
        int w=pixelsCanvus[0].length,h=pixelsCanvus.length;
        int iI=(int)(IPO.cYRange3.getMin()+0.5),iF=(int)(IPO.cYRange3.getMax()+0.5);
        int jI=(int)(IPO.cXRange3.getMin()+0.5),jF=(int)(IPO.cXRange3.getMax()+0.5);
        iI=Math.max(0, iI);
        iF=Math.min(iF, h-1);
        jI=Math.max(0, jI);
        jF=Math.min(jF, w-1);
        int i,j,pixel;
        double pdPars[]=IPO.pdPars,sig0,sig;
        double pdX[]=new double[2];
        double cnst=IPO.cnst;
        for(i=iI;i<=iF;i++){
            pdX[1]=i;
            for(j=jI;j<jF;j++){
                pdX[0]=j;
                sig0=pixelsTotalSignal[i][j];
                sig=func.fun(pdPars, pdX)-cnst;
                pixelsCanvus[i][j]+=(int)(sig*cnst/(sig0)+0.5);
            }
        }
    }
    public static  StackIPOGaussianNode importFittedStackIPOs(String path){
        File f=new File(path);
        FileInputStream fs=null;
        try{fs=new FileInputStream(f);}
        catch(FileNotFoundException e){
            return null;
        }
        BufferedInputStream bf=new BufferedInputStream(fs);
        int[] pnT=new int[4];
        byte[] pbTD=new byte[8];

        byte[] pbT=new byte[4];
        try{bf.read(pbT);}
        catch (IOException e){
            return null;
        }
        ByteConverter.getIntArray(pbT, 0, 4, pnT, 0, 1);
        int slices=pnT[0],nIPOs;

        int nNumAdditionalParsPerIPO;
        try{bf.read(pbT);}
        catch (IOException e){
            return null;
        }
        ByteConverter.getIntArray(pbT, 0, 4, pnT, 0, 1);
        nNumAdditionalParsPerIPO=pnT[0];

        int firstSlice=Integer.MAX_VALUE,lastSlice=-1;
        StackIPOGaussianNode cStackIPOs=new StackIPOGaussianNode();
        int i,j,slice;
        ArrayList<IPOGaussianNode> IPOs=null;
        SliceIPOGaussianNode SliceIPOs;
        ArrayList<SliceIPOGaussianNode> cvSliceIPOs=new ArrayList();
        int status,bytesRead;
        double HeightCutoff,TotalSignalCutoff;
        for(i=0;i<slices;i++){
            try{bytesRead=bf.read(pbT);}
            catch (IOException e){
                return null;
            }
            if(bytesRead<4) break;
            slice=ByteConverter.toInt(pbT);
            try{bf.read(pbT);}
            catch (IOException e){
                return null;
            }
            nIPOs=ByteConverter.toInt(pbT);
            try{
                bf.read(pbTD);
                HeightCutoff=ByteConverter.toDouble(pbTD);
                bf.read(pbTD);
                TotalSignalCutoff=ByteConverter.toDouble(pbTD);
            }
            catch(IOException e){
                return null;
            }
            IPOs=new ArrayList();
            try{status=IPOGaussianNodeHandler.importIPOs(bf,IPOs,nIPOs,nNumAdditionalParsPerIPO);}
            catch (IOException e){
                return null;
            }
            if(status==-1) break;
            SliceIPOs=new SliceIPOGaussianNode(IPOs,slice);
            SliceIPOs.RegionHCutoff=HeightCutoff;
            SliceIPOs.TotalSignalCutoff=TotalSignalCutoff;
            cvSliceIPOs.add(SliceIPOs);
            if(slice<firstSlice) firstSlice=slice;
            if(slice>lastSlice) lastSlice=slice;
        }
        cStackIPOs.setSliceIPOs(cvSliceIPOs);
        return cStackIPOs;
    }
    public static IPOGaussianNode getClosetIPOG(ArrayList<IPOGaussianNode> IPOGs,double x, double y){
        int i,len=IPOGs.size();
        double dn=Double.POSITIVE_INFINITY,dist2;
        IPOGaussianNode IPOG=null,IPOGt;
        for(i=0;i<len;i++){
            IPOGt=IPOGs.get(i);
            if(!IPOGt.contains2(x, y)) continue;
            dist2=IPOGt.getDist2(x, y);
            if(dist2<dn){
                dn=dist2;
                IPOG=IPOGt;
            }
        }
        return IPOG;
    }
    public static ArrayList<IPOGaussianNode> getSimpleIPOGs(ArrayList<IPOGaussianNode> IPOGs0){
        ArrayList<IPOGaussianNode> IPOGs=new ArrayList();
        int i,len=IPOGs0.size();
        IPOGaussianNode IPOG;
        for(i=0;i<len;i++){
            IPOG=IPOGs0.get(i);
            if(IPOG==null) continue;
            IPOG.getSimpleIPOGs(IPOGs);
        }
        return IPOGs;
    }
    
    public static double getAmpAt(ArrayList<IPOGaussianNode> IPOGs0,double x, double y){
        ArrayList<IPOGaussianNode> IPOGs=getSimpleIPOGs(IPOGs0);
        int i,len=IPOGs.size();
        double amp=0;
        for(i=0;i<len;i++){
            amp+=IPOGs.get(i).getAmpAt(x, y);
        }
        return amp;
    }
    public static void sortIPOGs(ArrayList<IPOGaussianNode> IPOGs, String sID){
        int i,len=IPOGs.size();
        double pdv[]=new double[len];
        int[] indexes=new int[len];
        IPOGaussianNode pcIPOGs[]=new IPOGaussianNode[len];
        for(i=0;i<len;i++){
            indexes[i]=i;
            pdv[i]=IPOGs.get(i).getValue(sID);
            pcIPOGs[i]=IPOGs.get(i);
        }
        QuickSort.quicksort(pdv, indexes);
        int index;
        IPOGs.clear();
        for(i=len-1;i>=0;i--){
            index=indexes[i];
            IPOGs.add(pcIPOGs[index]);
        }
    }
    public static boolean isFlat(IPOGaussianNodeComplex IPOG){
        if(IPOG==null) return false;
        IPOGContourParameterNode aNode=IPOG.getContour(IPOGContourParameterNode.IPOG);
        DoubleRange cSigmaRange;
        if(aNode!=null){
            cSigmaRange=aNode.getSigmaRange();
        }else{
            IPOGaussianNode IPO=IPOG.getMainIPOG();
            cSigmaRange=new DoubleRange(Math.min(IPOG.sigmax, IPO.sigmay),Math.max(IPOG.sigmax, IPO.sigmay));
        }
        return cSigmaRange.getMin()>2.5;
    }
    public static boolean isSharp(IPOGaussianNodeComplex IPOG){
        if(IPOG==null) return false;
        IPOGContourParameterNode aNode=IPOG.getContour(IPOGContourParameterNode.IPOG);
        DoubleRange cSigmaRange;
        if(aNode!=null){
            cSigmaRange=aNode.getSigmaRange();
        }else{
            IPOGaussianNode IPO=IPOG.getMainIPOG();
            cSigmaRange=new DoubleRange(Math.min(IPOG.sigmax, IPO.sigmay),Math.max(IPOG.sigmax, IPO.sigmay));
        }
        return cSigmaRange.getMax()<1.;
    }
    public static boolean isNormalContourShape(IPOGaussianNodeComplex IPOG,int type){ 
        int code;
        if(type==IPOGContourParameterNode.GaussianMean){
            code=IPOG.getContourCode();
        }else{
            code=IPOG.getRawContourCode();
        }
        if(code==0) return true;
        if(code==1) return false;
        IPOGContourParameterNode IPOGContour=IPOG.getContour(type);
        return isNormalContourShape(IPOGContour);
    }       
    public static boolean isNormalContour_RF(IPOGaussianNodeComplex IPOG, int type){   
//        IPOGContourClassifier=null;
        int code;
        if(type==IPOGContourParameterNode.GaussianMean){
            code=IPOG.getContourCode();
        }else{
            code=IPOG.getRawContourCode();
        }
        if(code==0) return true;
        if(code==1) return false;
        if(IPOGContourClassifier==null) loadIPOGShapeContourClassifier();
        String line=IPOGaussianNodeHandler.getIPOGContourARFFData(IPOG, type);
        if(line==null) return false;
        StringTokenizer stk=new StringTokenizer(line,",");
        String st;
        int i,len=stk.countTokens();
        Instance event=new Instance(len);
        event.setDataset(IPOGContourDataset);
        double dt;
        for(i=0;i<len-1;i++){
            st=stk.nextToken();
            dt=Double.parseDouble(st);
            event.setValue(i, dt);
        }
        int index=0;
        try {
            index=(int)(IPOGContourClassifier.classifyInstance(event)+0.5);
        }
        catch(Exception e){};
        return index==0;
    }     
    public static boolean isNormalIPOGShape_RF(IPOGaussianNodeComplex IPOG){   
        if(IPOG==null) return false;
        int code=IPOG.getIPOGCode();
        if(code==0) return true;
        if(code==1) return false;
        
        if(!isNormalContour_RF(IPOG,IPOGContourParameterNode.IPOG)){
            if(IPOG.getContour(IPOGContourParameterNode.IPOG)!=null) return false;
        }
        
        if(IPOGShapeClassifier==null) loadIPOGShapeContourClassifier();
        String line=IPOGaussianNodeHandler.getIPOGShapeAFRRData(IPOG);
        StringTokenizer stk=new StringTokenizer(line,",");
        String st;
        int i,len=stk.countTokens();
                
        
        Instance event=new Instance(len);
        
        event.setDataset(IPOGContourDataset);
        double dt;
        double[] pdt=new double[len-1];
        for(i=0;i<len-1;i++){
            st=stk.nextToken();
            dt=Double.parseDouble(st);
            event.setValue(i, dt);
        }
        
        int index=0;
        try {
            index=(int)(IPOGShapeClassifier.classifyInstance(event)+0.5);
        }
        catch(Exception e){
            st=e.toString();
        };
        return index==0;
    }     
    public static boolean isNormalContourShape(IPOGContourParameterNode aNode){        
        DoubleRange sigmaRange=new DoubleRange(1.2,2.1);
        DoubleRange SizeRange=new DoubleRange(2,15);
        if(aNode==null) return false;
        if(aNode.ReliableContour()){  
            if(Math.abs(aNode.cValueRange.getMax()/aNode.cValueRange.getMin())>3){
                 if(aNode.nConcaveCurves>0) return false;
                     double ratio=aNode.cDistRangeX.getMax()/aNode.cDistRangeN.getMin();
                     if(ratio*ratio>1.85) return false;
                     if(aNode.nMainPeaks>2) 
                         return false;
//                        if(Math.max(aNode.width/aNode.length, aNode.length/aNode.width)<1.11) return true;
                     if(Math.max(aNode.width/aNode.length, aNode.length/aNode.width)>1.25) return false;
                     if(!sigmaRange.contains(aNode.Sigma)) return false;
                 }
             } else {
                 return false;//13228
        }
        return true; 
    }
    
   
    public static boolean isNormalShape(IPOGaussianNodeComplex IPOG){      
        if(!isNormalIPOGShape(IPOG)) return false;
        if(!isNormalContourShape(IPOG.getContour(IPOGContourParameterNode.GaussianMean))) return false;
        if(!isNormalContourShape(IPOG.getContour(IPOGContourParameterNode.GaussianMeanRaw))) return false;
        return true; 
    }
    
    public static boolean isNormalShape_RF(IPOGaussianNodeComplex IPOG){      
        if(!isNormalIPOGShape_RF(IPOG)) return false;
        if(!isNormalContour_RF(IPOG,IPOGContourParameterNode.GaussianMean)) return false;
        if(!isNormalContour_RF(IPOG,IPOGContourParameterNode.GaussianMeanRaw)) return false;
        return true; 
    }
    
    public static boolean isNormalShape_RF(IPOGaussianNodeComplex IPOG, int option){
        if(option==1) return isNormalShape_RF(IPOG);
        if(option==2) if(isNormalIPOGShape_RF(IPOG)&&isNormalContour_RF(IPOG,IPOGContourParameterNode.GaussianMean)) return true;
        return false;
    }
    
     public static boolean isNormalIPOGShape(IPOGaussianNodeComplex IPOG){        
        if(IPOG==null) return false;
        int code=IPOG.getIPOGCode();
        if(code==0) return true;
        if(code==1) return false;
        
        if(isFlat(IPOG)) return false;
        if(isSharp(IPOG)) return false;
        
        double radius2=10*10;
        IPOGaussianNode IPOGm=IPOG.getMainIPOG(),IPOGt;
        if(IPOGm==null) return false;
        double mainAmpWeightCutoff=0.800,ratioCutoff=5;;
        int nComponents=IPOG.getNumIPOGs();
        if(!isNormalShape(IPOGm)) return false;
        if(nComponents==1) return true;
        double w=MainAmpWeight(IPOG,10);
        if(w>mainAmpWeightCutoff) return true;
        if(IPOGm.Amp/IPOG.IPOGs.get(1).Amp>ratioCutoff) return true;
        
        Point ct,ctm=IPOGm.getCenter();
        for(int i=0;i<nComponents;i++){
            IPOGt=IPOG.getIPOGs().get(i);
            if(IPOGt.Amp<0) continue;
            ct=IPOGt.getCenter();
            if(CommonMethods.getDist2(ctm.x, ctm.y, ct.x, ct.y)>radius2) continue;
            w=IPOGm.getAmpAt(ct.x, ct.y)/IPOG.getAmpAt(ct.x, ct.y);
 //           if(w<WeighAtOffCenterCutoff) return false;//13302
        }
        return true; 
    }
    public static boolean isNormalShape(IPOGaussianNode IPOG){
        double sigma=(IPOG.sigmax+IPOG.sigmay)/2.,ratio=Math.max(IPOG.sigmay/IPOG.sigmax,IPOG.sigmax/IPOG.sigmay);
        if(ratio*ratio>1.8) return false;
        if(sigma>1.85) return false;
        if(sigma<1.2) return false;
        return true;
    }
    public static IPOGaussianNodeComplex removeOffCenterIPOGs(IPOGaussianNodeComplex IPOG, double radius){
        ArrayList<IPOGaussianNode> IPOGs=IPOG.IPOGs;
        int i,len=IPOGs.size();
        double r2=radius*radius,dist2;
        IPOGaussianNode IPOGm=IPOG.getMainIPOG();
        for(i=0;i<len;i++){
            if(getDist2(IPOGm,IPOGs.get(len-1-i))>r2) IPOGs.remove(len-1-i);
        }
        return new IPOGaussianNodeComplex(IPOGs);
    }
    public static double getDist2(IPOGaussianNode IPOG1, IPOGaussianNode IPOG2){
        return CommonMethods.getDist2(IPOG1.xc, IPOG1.yc, IPOG2.xc, IPOG2.yc);
    }
    public static double MainAmpWeight(IPOGaussianNodeComplex IPOG, double radius){
        if(IPOG==null) return -1;
        IPOGaussianNode IPOGm=IPOG.getMainIPOG(),IPOGt;
        int nComponents=IPOG.getNumIPOGs();
        double dist2=radius*radius;
        Point ctm=IPOGm.getCenter(),ct;
        
        double Amp0=IPOGm.Amp,sum=0,Amp;
        for(int i=0;i<nComponents;i++){
            IPOGt=IPOG.getIPOGs().get(i);
            Amp=IPOGt.Amp;
            if(Amp<0) continue;
            ct=IPOGt.getCenter();
            if(CommonMethods.getDist2(ctm.x, ctm.y, ct.x, ct.y)>dist2) continue;;
            sum+=Amp;
        }
        double ratio=Amp0/sum;
        return ratio;
    }
    public static boolean isNormalShape_Rough(IPOGaussianNodeComplex IPOG){
        if(IPOG==null) return false;
        IPOGaussianNode IPOGm=IPOG.getMainIPOG(),IPOGt;
        double firstAmpRatioCutoff=2;
        DoubleRange cRRange=new DoubleRange(0.9,2.3);
        int nComponents=IPOG.getNumIPOGs();
        if(!cRRange.contains(IPOGm.sigmax)) return false;
        if(!cRRange.contains(IPOGm.sigmay)) return false;
        double Amp0=IPOGm.Amp,sum=0,Amp;
        for(int i=0;i<nComponents;i++){
            IPOGt=IPOG.getIPOGs().get(i);
            if(IPOGt==IPOGm) continue;
            Amp=IPOGt.Amp;
            if(Amp<0) continue;
            sum+=Amp;
        }
        double ratio=Amp0/sum;
        return ratio>firstAmpRatioCutoff; 
    }
    public static int exportIPOGContourAsWekaInsitances(String path, ArrayList<String> CommentLines, ArrayList<IPOGaussianNodeComplex> IPOGs, int type){
        IPOGContourParameterNode contour;
        Formatter fm=CommonMethods.QuickFormatter(path);
        int i,len=CommentLines.size();
        String st,line,newLine=PrintAssist.newline;
        for(i=0;i<len;i++){
            st=CommentLines.get(i);
            PrintAssist.printString(fm, path);
            PrintAssist.endLine(fm);
        }
        PrintAssist.printString(fm, "@RELATION ContourShape"+newLine);
        
        PrintAssist.printString(fm, "@ATTRIBUTE NumPeaks NUMERIC"+newLine);
        PrintAssist.printString(fm, "@ATTRIBUTE NumConcaveCurve NUMERIC"+newLine);
        PrintAssist.printString(fm, "@ATTRIBUTE DistRatio NUMERIC"+newLine);
        PrintAssist.printString(fm, "@ATTRIBUTE WLRatio NUMERIC"+newLine);
        PrintAssist.printString(fm, "@ATTRIBUTE MaxEMCPSideLength NUMERIC"+newLine);
        PrintAssist.printString(fm, "@ATTRIBUTE Sigma NUMERIC"+newLine);
        PrintAssist.printString(fm, "@ATTRIBUTE RatioC NUMERIC"+newLine);
        PrintAssist.printString(fm, "@ATTRIBUTE PeakShift NUMERIC"+newLine);
        
        PrintAssist.printString(fm, "@ARIBUTE class {Normal, Abnormal}");
        
        PrintAssist.printString(fm, "@Data");
        int numPeaks,numConcave;
        double dDistRatio,dWLRatio, dMaxMECSideLength,dSigma,dRatioC,dPeakShift;
        
        IPOGaussianNode IPOG;
        
        for(i=0;i<len;i++){
            IPOG=IPOGs.get(i);
            contour=IPOG.getContour(type);
            numPeaks=contour.nDistMaxima;
            numConcave=contour.nConcaveCurves;
            dDistRatio=contour.cDistRangeX.getMax()/contour.cDistRangeN.getMin();
            dWLRatio=contour.width/contour.length;
            dSigma=contour.Sigma;
            dMaxMECSideLength=contour.dMaxSideLength/(dSigma*contour.nMag);
            dRatioC=Math.abs(contour.cValueRange.getMax()/contour.cValueRange.getMin());
        }
        fm.close();
        return 1;
    }
    public static ArrayList<String> getIPOGContourAFRRHeader(){
        ArrayList<String> lines=new ArrayList();
        lines.add("@RELATION ContourShape");
        lines.add("@ATTRIBUTE NumPeaks NUMERIC");
        lines.add("@ATTRIBUTE NumConcaveCurve NUMERIC");
        lines.add("@ATTRIBUTE DistRatio NUMERIC");
        lines.add("@ATTRIBUTE WLRatio NUMERIC");
        lines.add("@ATTRIBUTE MaxEMCPSideLength NUMERIC");
        lines.add("@ATTRIBUTE Sigma NUMERIC");
        lines.add("@ATTRIBUTE RatioC NUMERIC");
        lines.add("@ATTRIBUTE PeakShift NUMERIC");
        lines.add("@ATTRIBUTE class {Normal, Abnormal}");
        lines.add("@Data");
        return lines;
    }
    static public void exportIPOGShapeHeader(Formatter fmShape){
        String newline=PrintAssist.newline;
        ArrayList<String> lines=getIPOGShapeAFRRHeader();
        String line;
        int i,len=lines.size();
        for(i=0;i<len;i++){
            line=lines.get(i)+newline;
            PrintAssist.printString(fmShape, line);
        }
    }
    static public void exportIPOGContourHeader(Formatter fmContour){
        String newline=PrintAssist.newline;
        ArrayList<String> lines=getIPOGContourAFRRHeader();
        String line;
        int i,len=lines.size();
        for(i=0;i<len;i++){
            line=lines.get(i)+newline;
            PrintAssist.printString(fmContour, line);
        }
    }
    public static ArrayList<String> getIPOGShapeAFRRHeader(){
        ArrayList<String> lines=new ArrayList();
        lines.add("@RELATION IPOGShape");
        lines.add("@ATTRIBUTE MinSigma NUMERIC");//Sigma of the main IPOG of IPOGComplexNode
        lines.add("@ATTRIBUTE MaxSigma NUMERIC");
        lines.add("@ATTRIBUTE SigmaRatio NUMERIC");
        lines.add("@ATTRIBUTE AveSigma NUMERIC");
        lines.add("@ATTRIBUTE MainAmpWeight NUMERIC");//AmpM/Sum(Amp)
        lines.add("@ATTRIBUTE AmpRatio NUMERIC");//AmpM/AmpS, AmpM and AmpS are amp of the main and second largest component, respectively.
        lines.add("@ATTRIBUTE MainAmpWeigtAtOffCenter NUMERIC");
        lines.add("@ATTRIBUTE class {Normal, Abnormal}");
        lines.add("@Data");
        return lines;
    }
    public static String getIPOGShapeAFRRData(IPOGaussianNodeComplex IPOGc){
        String line="";
        IPOGaussianNode IPOG,IPOGm=IPOGc.getMainIPOG();
        double sx=IPOGm.sigmax,sy=IPOGm.sigmay;
        double dMinSigma=Math.min(sx,sy);
        double dMaxSigma=Math.max(sx,sy);
        double dSigmaRatio=Math.max(sx/sy,sy/sx);
        double dAveSigma=0.5*(sx+sy);
        double AmpM=IPOGm.Amp,sum=AmpM,AmpS=0.02*AmpM,amp,wx=1,w;   
        int i,len=IPOGc.IPOGs.size();
        Point ct;
        
        for(i=0;i<len;i++){
            IPOG=IPOGc.IPOGs.get(i);
            if(IPOG==IPOGm) continue;
            amp=IPOG.Amp;
            if(amp>AmpS) AmpS=amp;
            sum+=amp;
            
            ct=IPOG.getCenter();
            w=IPOGm.getAmpAt(ct.x, ct.y)/IPOGc.getAmpAt(ct.x, ct.y);
            if(w<wx) wx=w;
        }
        double dMainW=AmpM/sum,dAmpRatio=AmpM/AmpS,dMainWeightAtOffcenter=wx;
        line+=PrintAssist.ToString(dMinSigma, 3)+", ";
        line+=PrintAssist.ToString(dMaxSigma, 3)+", ";
        line+=PrintAssist.ToString(dSigmaRatio, 3)+", ";
        line+=PrintAssist.ToString(dAveSigma, 3)+", ";
        line+=PrintAssist.ToString(dMainW, 3)+", ";
        line+=PrintAssist.ToString(dAmpRatio, 3)+", ";
        line+=PrintAssist.ToString(dMainWeightAtOffcenter, 3)+", ";
        int nCode=IPOGc.getIPOGCode();
        if(nCode<0) 
            line+="? ";
        else if(nCode==0) 
            line+="Normal";
        else
            line+="Abnormal";
        return line;
   }
   public static String getIPOGContourARFFData(IPOGaussianNodeComplex IPOGc, int type){
        String line="";
        IPOGContourParameterNode contour=IPOGc.getContour(type);
        if(contour==null) 
            return null;
        line+=contour.nMainPeaks+", ";
        line+=contour.nConcaveCurves+", ";
        double dRatioD=Math.abs(contour.cDistRangeX.getMax()/contour.cDistRangeN.getMin());
        line+=PrintAssist.ToString(dRatioD, 3) +", ";
        double dWLRatio=contour.width/contour.length;
        dWLRatio=Math.max(dWLRatio, 1./dWLRatio);
        line+=PrintAssist.ToString(dWLRatio, 3) +", ";
        line+=PrintAssist.ToString(contour.dMaxSideLength/contour.Sigma, 3) +", ";
        line+=PrintAssist.ToString(contour.Sigma, 3) +", ";
        double dRatioC=Math.abs(contour.cValueRange.getMax()/contour.cValueRange.getMin());
        line+=PrintAssist.ToString(dRatioC, 3) +", ";
        double dCenterShift=contour.getCentralPeakShiftDist();
        line+=PrintAssist.ToString(dCenterShift, 3) +", ";
        int nCode=IPOGc.getContourShapeCode(type);
        if(nCode<0) 
            line+="? ";
        else if(nCode==0) 
            line+="Normal";
        else
            line+="Abnormal";
        return line;
    }
}
