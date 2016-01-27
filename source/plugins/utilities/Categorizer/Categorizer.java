/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Categorizer;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
import utilities.CommonMethods;
import ij.IJ;

/**
 *
 * @author Taihao//checked on 3/1/2011
 */
public class Categorizer {
    int m_nDim;
    boolean m_bAscending=true;
    ArrayList<double[]> m_vpdDelimiters;
    int nDivisions,nMinDivisions;
    ArrayList<SegmentIndexNode> m_cIndexNodes;
    double[] m_pdMins, m_pdMaxs;
    public Categorizer(){
        nDivisions=10;
        nMinDivisions=4;//this is arbitraryly given here.
    }
    public Categorizer(ArrayList<double[]>vpdDelimiters, boolean bAscending){
        this();
        update(vpdDelimiters,bAscending);
    }
    public Categorizer(double[] pdDelimiters, boolean bAscending){
        this();
        ArrayList<double[]> vpdDelimiters=new ArrayList();
        vpdDelimiters.add(pdDelimiters);
        update(vpdDelimiters,bAscending);
    }
    public void update(ArrayList<double[]>vpdDelimiters, boolean bAscending){
        m_vpdDelimiters=vpdDelimiters;
        m_nDim=m_vpdDelimiters.size();
        m_bAscending=true;
        double[] pdDelimiters;

        m_cIndexNodes=new ArrayList();
        if(!bAscending) reverseDelimiters();
        m_pdMins=new double[m_nDim];
        m_pdMaxs=new double[m_nDim];
        int len;
        for(int i=0;i<m_nDim;i++){
            pdDelimiters=m_vpdDelimiters.get(i);
            verifyDelimiters(pdDelimiters);
            len=pdDelimiters.length;
            m_cIndexNodes.add(new SegmentIndexNode(pdDelimiters,0,pdDelimiters.length-1,nDivisions,nMinDivisions));
            m_pdMins[i]=pdDelimiters[0];
            m_pdMaxs[i]=pdDelimiters[len-1];
        }
    }
    boolean verifyDelimiters(double pdDelimiters[]){
        boolean bValid=verifySorting(pdDelimiters);
        if(!bValid)IJ.error("the delimiters in the class \"Cattegorizer\" is not sorted");
        return true;
    }
    public static boolean verifySorting(double pdData[]){
        int len=pdData.length;
        double d0=pdData[0],d=pdData[1],d1;
        for(int i=1;i<len-1;i++){
            d1=pdData[i+1];
            if((d-d0)*(d1-d)<0){
                return false;
            }
            d0=d;
            d=d1;
        }
        return true;
    }
    public void getCatIndexes(double[] pdV, int[] indexes){
        for(int i=0;i<m_nDim;i++){
            indexes[i]=getCatIndex(pdV[i],i);
        }
    }
    public int getCatIndex(double dV){
        return getCatIndex(dV,0);
    }
    int getCatIndex(double dV, int index){
        if(dV<m_pdMins[index])
            return 0;
        else if(dV>=m_pdMaxs[index])
            return m_vpdDelimiters.get(index).length;
        return m_cIndexNodes.get(index).getSegmentIndex(dV);
    }

    void reverseDelimiters(){
        int i,j,len;
        double[] pdDelimiters;
        for(i=0;i<m_nDim;i++){
            pdDelimiters=m_vpdDelimiters.get(i);
            len=pdDelimiters.length;
            for(j=0;j<len/2;j++){
                CommonMethods.swapElements(pdDelimiters, j, len-1-j);
            }
        }
    }
    static public int getLinearIndex(double dMin, double delta, double dV){
        return (int)((dV-dMin)/delta);
    }



    class SegmentIndexNode{
        int nNumCategories,nDivisions,dIndexI,dIndexF,nMinDivisions;
        double[] pdDelimiters;
        boolean bEndNode;
        SegmentIndexNode[] pcIndexNodes;
        SegmentIndexNode(double[] pdDelimiters, int iI, int iF, int nDivisions, int nMinDivisions){//there are two indexes differed by 1, the index used to retrieve
            //the delimiter and the segment index used out side of this class. The segment indexe for a value dV, d(i)<=dV <d(i+1) is i. iI and iF are indexes
            //for the delimiters. This class determined the segment indexes for values only between the delimiters d(iI) and d(iF).
            this.pdDelimiters=pdDelimiters;
            dIndexI=iI;
            dIndexF=iF;
            this.nDivisions=nDivisions;
            this.nMinDivisions=nMinDivisions;
            bEndNode=constructNodes();
        }
        boolean constructNodes(){
            int len=dIndexF-dIndexI;//number of segments
            if(len<=nMinDivisions){//a leaf node
                pcIndexNodes=null;
                return true;
            }else{
                pcIndexNodes=new SegmentIndexNode[nDivisions];
            }

            intRange[] deliIndexRanges=calDeliIndexRanges();
            intRange deliRange;//delimiter index range
            for(int i=0;i<nDivisions;i++){
                deliRange=deliIndexRanges[i];
                pcIndexNodes[i]=new SegmentIndexNode(pdDelimiters,deliRange.getMin(),deliRange.getMax(),nDivisions,nMinDivisions);
            }
            return false;
        }
        intRange[] calDeliIndexRanges(){
            double dMin=pdDelimiters[dIndexI];
            int i,j;
            double dMax=pdDelimiters[dIndexF];
            double delta=(dMax-dMin)/(nDivisions);
            intRange[] pcDeliRanges=new intRange[nDivisions];//the i-th element contains a pair of delimiter index iI and iF
            //satisfying pdDelimiters[iI]<=dMin+i*delta<pdDelimiter[iI+1] and pdDelimiter[iF-1]<=dMin+(i+1)*delta<pdDelimiter[iF];
            for(i=0;i<nDivisions;i++){
                pcDeliRanges[i]=new intRange();
            }
            int lIndex,lIndex0,dIndex;//linear section index and category index.
            lIndex0=0;
            int iI=dIndexI;
            pcDeliRanges[lIndex0]=new intRange(iI,iI);
            double delimiter;
            for(dIndex=dIndexI;dIndex<=dIndexF;dIndex++){
                delimiter=pdDelimiters[dIndex];
                lIndex=getLinearIndex(dMin,delta,delimiter);
                if(lIndex>=nDivisions) lIndex=nDivisions-1;//depends on the numerical roundoff
                if(lIndex>lIndex0){
                    pcDeliRanges[lIndex0].expandRange(dIndex);
                    for(j=lIndex0+1;j<lIndex;j++){
                        pcDeliRanges[j]=new intRange(dIndex-1,dIndex);
                    }
                    lIndex0=lIndex;
                    iI=dIndex-1;
                    pcDeliRanges[lIndex0]=new intRange(iI,iI);
                }
            }
            pcDeliRanges[nDivisions-1].expandRange(dIndexF);
            return pcDeliRanges;
        }
        public int getSegmentIndex(double dV){
            int sIndex=dIndexI+1;
            if(bEndNode){
                if(dV<pdDelimiters[dIndexI]||dV>=pdDelimiters[dIndexF]){
                    IJ.error("the value is out of range for getCategoryIndex");
                }
                for(int i=dIndexI;i<=dIndexF;i++){
                    if(pdDelimiters[i]>=dV) {
                        break;
                    }
                    sIndex=i+1;
                }
            }else{
                double dMin=pdDelimiters[dIndexI],dMax=pdDelimiters[dIndexF];
                int index=getLinearIndex(dMin,(dMax-dMin)/nDivisions,dV);
                if(index>=nDivisions){//might be able to cause by round off
                    index=nDivisions-1;
                }
                sIndex=pcIndexNodes[index].getSegmentIndex(dV);
            }
            return sIndex;
        }
    }
}
