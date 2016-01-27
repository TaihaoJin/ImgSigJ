/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import FluoObjects.IPObjectTrack;
import FluoObjects.IPOArray;
import FluoObjects.IntensityPeakObject;
import FluoObjects.IntensityPeakObjectHandler;
import FluoObjects.IPObjectMatcher;
import java.util.ArrayList;
import ij.IJ;
import assignment.AssignmentAlgorithm;
import assignment.HungarianAlgorithm;
import utilities.ArrayofArrays.IntArray;
import java.awt.Point;
import utilities.BlockDiagonalizer;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.CommonMethods;


/**
 *
 * @author Taihao
 */
public class IPObjectMatcher {
    int m_nDim;
    float[][] costMatrix,distMatrix;
    int[][] pnBlockMatrix;
    float m_fCutoffDist,m_fDistAdjust,m_fDistAdjust2;
    ArrayList <IntensityPeakObject> m_vcIPOs0;
    ArrayList <IntensityPeakObject> m_vcIPOs1;
    ArrayList <IntArray> m_viaRowIndexes;
    ArrayList <IntArray> m_viaColumnIndexes;
    int m_nCostFunctionOption;

    public IPObjectMatcher(){
    }
    public int matchIPOs(ArrayList <IntensityPeakObject> IPOs0,
            ArrayList <IntensityPeakObject> IPOs1,float fCutoffDist,int nCostFunctionOption){
        m_nCostFunctionOption=nCostFunctionOption;
        int len0=IPOs0.size(),len1=IPOs1.size();
        m_nDim=len0;
        if(len1>m_nDim) m_nDim=len1;
        if(m_nDim==0) return -1;
        m_vcIPOs0=IPOs0;
        m_vcIPOs1=IPOs1;
        m_fCutoffDist=fCutoffDist;
        m_fDistAdjust=4;
        m_fDistAdjust2=m_fDistAdjust*m_fDistAdjust;

        calCostMatrix();
        blockdiagonalizeCostMatrix();
        int blocks=m_viaRowIndexes.size(),bi;
        ArrayList<Integer> rowIndexes, columnIndexes;
        int nDim=0;

        float dist2=m_fCutoffDist*m_fCutoffDist;
        float[][] cm;
        int i,j,ri,ci;
        int index0,index1;
        IntensityPeakObject IPO0,IPO1;
        float rn,cost;

        for(bi=0;bi<blocks;bi++){
            rowIndexes=m_viaRowIndexes.get(bi).m_intArray;
            columnIndexes=m_viaColumnIndexes.get(bi).m_intArray;
            len0=rowIndexes.size();
            len1=columnIndexes.size();
            if(len0==0||len1==0){
                continue;
            }

            if(CommonMethods.containsContent(rowIndexes, 38)&&CommonMethods.containsContent(columnIndexes, 31)){
                bi=bi;
            }
/*
            if(len0==1){
                ri=rowIndexes.get(0);
                index0=ri;
                index1=columnIndexes.get(0);
                rn=costMatrix[index0][index1];
                for(j=1;j<len1;j++){
                    ci=columnIndexes.get(j);
                    cost=costMatrix[ri][ci];
                    if(cost<rn){
                        index1=ci;
                        rn=cost;
                    }
                }
                IPO0=m_vcIPOs0.get(index0);
                IPO1=m_vcIPOs1.get(index1);
                IPO0.postIPO=IPO1;
                IPO1.preIPO=IPO0;
                continue;
            }

            if(len1==1){
                ci=columnIndexes.get(0);
                index1=ci;
                index0=rowIndexes.get(0);
                rn=costMatrix[index0][index1];
                for(j=1;j<len1;j++){
                    ri=rowIndexes.get(j);
                    cost=costMatrix[ri][ci];
                    if(cost<rn){
                        index0=ri;
                        rn=cost;
                    }
                }
                IPO0=m_vcIPOs0.get(index0);
                IPO1=m_vcIPOs1.get(index1);
                IPO0.postIPO=IPO1;
                IPO1.preIPO=IPO0;
                continue;
            }

*/
            nDim=len0;
            if(len1>nDim) nDim=len1;
            if(nDim==0) continue;

            cm=new float[len0][len1];

            for(i=0;i<len0;i++){
                ri=rowIndexes.get(i);
                if(ri==38){
                    i=i;
                }
                for(j=0;j<len1;j++){
                    ci=columnIndexes.get(j);
                    cm[i][j]=costMatrix[ri][ci];
                }
            }
            if(CommonMethods.containsContent(rowIndexes, 80)&&CommonMethods.containsContent(columnIndexes, 58)){
                bi=bi;
            }

            if(len0>1||len1>1){
                len0=len0;
            }
            if(len0>1&&len1>1){
                len0=len0;
            }
            HungarianAlgorithm ha=new HungarianAlgorithm();
            int assignment[][]=ha.computeAssignments(cm);

            for(i=0;i<assignment.length;i++){
                if(assignment==null) return -1;
                if(assignment[i]==null) continue;
                index0=assignment[i][0];
                index1=assignment[i][1];
                if(index0<len0){
                    ri=rowIndexes.get(index0);
                    IPO0=m_vcIPOs0.get(ri);
                }
                else{
                    IPO0=null;
                    ri=m_nDim-1;
                    continue;
                }

                if(index1<len1)
                {
                    ci=columnIndexes.get(index1);
                    IPO1=m_vcIPOs1.get(ci);
                }
                else{
                    ci=m_nDim-1;
                    IPO1=null;
                    continue;
                }

                if(distMatrix[ri][ci]<=dist2){
                    if(IPO0==null||IPO1==null){
                        i=i;
                        i=i;
                    }
                    IPO0.postIPO=IPO1;
                    IPO1.preIPO=IPO0;
                }else{
                    if(IPO0!=null) IPO0.postIPO=null;
                    if(IPO1!=null) IPO1.preIPO=null;
                }
            }
        }
/*
            float fm[][]=new float[5][4];
            float[] f0={10,19,8,15};
            float[] f1={10,18,7,17};
            float[] f2={13,16,9,14};
            float[] f3={12,19,8,18};
            float[] f4={14,17,10,19};
            fm[0]=f0;
            fm[1]=f1;
            fm[2]=f2;
            fm[3]=f3;
            fm[4]=f4;
            HungarianAlgorithm ha=new HungarianAlgorithm();
            int assignment[][]=ha.computeAssignments(fm);
 */
 /*              correctly picked 
  *         assignment[0]={0,0}
  *         assignment[1]={4,1}
  *         assignment[2]={1,2}
  *         assignment[3]={2,3}
  *         assignment[4]=null
 */

        return 1;
    }
    
    float [][] copyOfCostMatrix(){
        int i,j;
        float[][] a=new float[m_nDim][m_nDim];
        for(i=0;i<m_nDim;i++){
            for(j=0;j<m_nDim;j++){
                a[i][j]=costMatrix[i][j];
            }
        }
        return a;
    }
    void calCostMatrix(){
        switch(m_nCostFunctionOption){
            case IPObjectTracker.Distance:
                calCostMatrix_Dist();
                break;
            case IPObjectTracker.AmpDifference:
                calCostMatrix_SignalDiff();
                break;
            case IPObjectTracker.ImageShapeOverlap:
                calCostMatrix_ImageShapeOverlap();
                break;
            default:
                calCostMatrix_Dist();
                break;
        }
    }
    void calCostMatrix_Dist(){
        costMatrix=new float[m_nDim][m_nDim];
        pnBlockMatrix=new int[m_nDim][m_nDim];
        int i,j;
        float dist0=m_fCutoffDist*m_fCutoffDist;
        for(i=0;i<m_nDim;i++){
            for(j=0;j<m_nDim;j++){
                costMatrix[i][j]=dist0+m_fDistAdjust2+1+(float)Math.random();
                pnBlockMatrix[i][j]=0;
            }
        }
        distMatrix=copyOfCostMatrix();
        IntensityPeakObject IPO0,IPO1;
        ArrayList <IntensityPeakObject> IPOst;
        int len0=m_vcIPOs0.size(),len1=m_vcIPOs1.size(),len;
        float fDist;
        for(i=0;i<len0;i++){
            IPO0=m_vcIPOs0.get(i);
            if(IPO0.cx==19&&IPO0.cy==182&&IPO0.cz==309){
                i=i;
            }
            for(j=0;j<len1;j++){
                IPO1=m_vcIPOs1.get(j);
                fDist=dist2(IPO0,IPO1);
                if(fDist>dist0) continue;
                distMatrix[i][j]=fDist;
                costMatrix[i][j]=fDist+distAdjust(IPO0);
                pnBlockMatrix[i][j]=1;
            }
        }
    }

    void calCostMatrix_SignalDiff(){
        costMatrix=new float[m_nDim][m_nDim];
        pnBlockMatrix=new int[m_nDim][m_nDim];
        int i,j;
        double ratio0=0.0000001;

        for(i=0;i<m_nDim;i++){
            for(j=0;j<m_nDim;j++){
                costMatrix[i][j]=(float)(1/(ratio0*ratio0)+Math.random());
                pnBlockMatrix[i][j]=0;
            }
        }

        distMatrix=copyOfCostMatrix();
        IntensityPeakObject IPOi,IPOj;
        ArrayList <IntensityPeakObject> IPOst;
        int len0=m_vcIPOs0.size(),len1=m_vcIPOs1.size(),len;
        float fDist;
        double xi,yi,xj,yx,ratio,ratioi,ratioj,aii,aij,aji,ajj;
        double[] Xi,Xj;
        for(i=0;i<len0;i++){
            IPOi=m_vcIPOs0.get(i);
            Xi=IPOi.getPeakPosition();
            aii=IPOi.getAmpAt(Xi[0], Xi[1]);
            for(j=0;j<len1;j++){
                IPOj=m_vcIPOs1.get(j);
                Xj=IPOj.getPeakPosition();
                ajj=IPOj.getAmpAt(Xj[0], Xj[1]);
                aij=IPOi.getAmpAt(Xj[0], Xj[1]);
                aji=IPOj.getAmpAt(Xi[0], Xi[1]);

                ratioi=Math.min(aij/ajj, ajj/aij);
                if(Double.isNaN(ratioi)||ratioi>1) continue;
                ratioj=Math.min(aji/aii, aii/aji);
                if(Double.isNaN(ratioj)||ratioj>1) continue;
                ratio=ratioi*ratioj;
                if(ratio<ratio0) continue;
                fDist=dist2(IPOi,IPOj);
                distMatrix[i][j]=fDist;
                costMatrix[i][j]=(float)(1/ratio);
                pnBlockMatrix[i][j]=1;
            }
        }
    }
    void calCostMatrix_ImageShapeOverlap(){//assign overlap here
        costMatrix=new float[m_nDim][m_nDim];
        pnBlockMatrix=new int[m_nDim][m_nDim];
        int i,j;
        double ratio0=0.0000001;

        for(i=0;i<m_nDim;i++){
            for(j=0;j<m_nDim;j++){
                costMatrix[i][j]=(float)(1/(ratio0*ratio0)+Math.random());
                pnBlockMatrix[i][j]=0;
            }
        }

        distMatrix=copyOfCostMatrix();
        IntensityPeakObject IPOi,IPOj;
        ArrayList <IntensityPeakObject> IPOst;
        int len0=m_vcIPOs0.size(),len1=m_vcIPOs1.size(),len;
        float fDist;
        double ratio,areai,areaj,overlap;
        ImageShape cISi,cISj,cISoverlap;
        double[] Xi,Xj;
        for(i=0;i<len0;i++){
            IPOi=m_vcIPOs0.get(i);
            areai=IPOi.getRegionArea();
            cISi=IPOi.cIS;
            for(j=0;j<len1;j++){
                IPOj=m_vcIPOs1.get(j);
                cISj=IPOj.cIS;
                if(cISi.contains(71,106)&&cISj.contains(71,106)&&IPOi.cz==28){
                    j=j;
                }
                if(!cISi.overlapping(cISj)) continue;
                areaj=IPOj.getRegionArea();
                fDist=dist2(IPOi,IPOj);
                cISoverlap=new ImageShape(cISj);
                cISoverlap.overlapShape(cISi);
                overlap=cISoverlap.getArea();

                if(overlap>IPOi.postOvlp){
                    IPOi.postOvlp=overlap;
                    IPOi.postRid=IPOj.rIndex;
                }

                if(overlap>IPOj.preOvlp){
                    IPOj.preOvlp=overlap;
                    IPOj.preRid=IPOi.rIndex;
                }

                ratio=2*overlap/(areai+areaj);
                
                if(ratio<ratio0) continue;

                distMatrix[i][j]=fDist;
                costMatrix[i][j]=(float)(1/ratio);
                pnBlockMatrix[i][j]=1;
            }
        }
    }

    float distAdjust(IntensityPeakObject IPO){//additional penalizing cost function value for IPOs
        float da=0;
        int z0=IPO.cz,z;
        if(IPO.preIPO==null) return m_fDistAdjust2;//penalty for the opening of a new track
        IPO=IPO.preIPO;
        z=IPO.cz;
        if((z0-z)>1) return m_fDistAdjust2;//penalty for creating a gap
        if(IPO.preIPO==null) return m_fDistAdjust2/2;//penalty (half) for extending the second object of a new track
        z=IPO.cz;
        if((z0-z)>1) return m_fDistAdjust2/2;//penalty (half) for extending theobject after a gap
        return da;//no panelty for other cases
    }
    
    float dist2(IntensityPeakObject IPO0,IntensityPeakObject IPO1){
        int dx=IPO0.getCX()-IPO1.getCX();
        int dy=IPO0.getCY()-IPO1.getCY();
        float dist=dx*dx+dy*dy;
        return dist;
    }
    void blockdiagonalizeCostMatrix(){
        int [][] AM=new int[m_nDim][m_nDim];//assignability matrix
        int i,j;
        float dist2=m_fCutoffDist*m_fCutoffDist;
        for(i=0;i<m_nDim;i++){
            for(j=0;j<m_nDim;j++){
                if(distMatrix[i][j]>dist2)
                    AM[i][j]=0;
                else
                    AM[i][j]=1;
            }
        }
        blockdiagonalizeIntMatrix(AM);
    }
    
    void blockdiagonalizeIntMatrix(int[][] IM){
        m_viaRowIndexes=new ArrayList();
        m_viaColumnIndexes=new ArrayList();
        BlockDiagonalizer bd=new BlockDiagonalizer();
        bd.blockdiagonalizeIntMatrix(IM, m_viaRowIndexes, m_viaColumnIndexes);
    }

    void blockdiagonalizeIntMatrix0(int[][] IM){
        ArrayList<Integer> vnaBlockIndexes_Column[]=new ArrayList[m_nDim], vnaBlockIndexes_Row[]=new ArrayList[m_nDim];
        int i,j;
        for(i=0;i<m_nDim;i++){
            vnaBlockIndexes_Column[i]=null;
            vnaBlockIndexes_Row[i]=null;
        }
        ArrayList<Integer> vnBIC,vnBIR,vnBlockIndex;
        int nbic,nbir,nbi;
        int numBlocks=0;

        for(i=0;i<m_nDim;i++){
            for(j=0;j<m_nDim;j++){
                if(IM[i][j]!=0){
                    vnBIR=vnaBlockIndexes_Row[i];
                    vnBIC=vnaBlockIndexes_Column[j];
                    if(vnBIR==null&&vnBIC==null){
                        vnBlockIndex=new ArrayList();
                        nbi=numBlocks;
                        vnBlockIndex.add(nbi);
                        vnaBlockIndexes_Row[i]=vnBlockIndex;
                        vnaBlockIndexes_Column[j]=vnBlockIndex;
                        numBlocks++;
                    }else if(vnBIR==null&&vnBIC!=null){
                        vnaBlockIndexes_Row[i]=vnBIC;
                    }else if(vnBIR!=null&&vnBIC==null){
                        vnaBlockIndexes_Column[j]=vnBIR;
                    }else if(vnBIR!=null&&vnBIC!=null){
                        nbic=vnBIC.get(0);
                        nbir=vnBIR.get(0);
                        if(nbic<nbir){
                            vnBIR.set(0, nbic);
                        }else{
                            vnBIC.set(0, nbir);
                        }
                    }
                }
            }
        }

        int maxIndexR=-1;
        for(i=0;i<m_nDim;i++){
            vnBIR=vnaBlockIndexes_Row[i];
            if(vnBIR!=null){
                nbir=vnBIR.get(0);
                if(nbir>maxIndexR) maxIndexR=nbir;
            }
        }

        int maxIndexC=-1;
        for(i=0;i<m_nDim;i++){
            vnBIC=vnaBlockIndexes_Column[i];
            if(vnBIC!=null){
                nbic=vnBIC.get(0);
                if(nbic>maxIndexC) maxIndexC=nbic;
            }
        }

        numBlocks=maxIndexC+1;

        m_viaRowIndexes=new ArrayList();
        m_viaColumnIndexes=new ArrayList();
        for(i=0;i<numBlocks;i++){
            m_viaRowIndexes.add(new IntArray());
            m_viaColumnIndexes.add(new IntArray());
        }

        for(i=0;i<m_nDim;i++){
            vnBIR=vnaBlockIndexes_Row[i];
            if(vnBIR!=null){
                nbir=vnBIR.get(0);
                m_viaRowIndexes.get(nbir).m_intArray.add(i);
            }
            vnBIC=vnaBlockIndexes_Column[i];
            if(vnBIC!=null){
                nbic=vnBIC.get(0);
                m_viaColumnIndexes.get(nbic).m_intArray.add(i);
            }
        }
    }
}
