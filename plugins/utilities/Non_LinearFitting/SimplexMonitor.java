/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.Constrains.ConstraintViolationChecker;
import utilities.CommonMethods;
/**
 *
 * @author Taihao
 */
public class SimplexMonitor {
    double[][] m_pdSimplex,m_pdSimplex0;
    /*
    ArrayList<double[][]> m_pdvOptTrail;
    ArrayList<Double> m_dvOptFunctionValues;
    ArrayList<double[]> m_pdvVertexHeights;
    ArrayList<int[]> m_pnvViolationIndexes;
    ArrayList<Integer> m_nvHiIndexes,m_nvLoIndexes;
    */
    int m_nDim;
    /*
    class SimplexTrailNode{
        public double[][] simplex;
        public double dV;
        public double[] pdVertexHeights;
        public ArrayList <Integer> pnViolatedIndexes;
        public ArrayList <Double> pdPenalties;
        public int nLo,nHi;
        ArrayList<VertexNode> cvRejectedVertices;
    }
     *
     */
    class VertexNode{
        public double dV;//the penalty is included
        double pars[];
        public ArrayList <Integer> nvViolatedIndexes;
        public ArrayList <Double> dvPenalties;
        public double penalty;
        public boolean accepted;
        public VertexNode(){

        }
        void update(double dV, double[] pars, ArrayList <Integer> nvViolatedIndexes,
                ArrayList <Double> dvPenalties,double penalty, boolean accepted){
            this.dV=dV;
            this.pars=pars;
            this.nvViolatedIndexes=nvViolatedIndexes;
            this.dvPenalties=dvPenalties;
            this.penalty=penalty;
            this.accepted=accepted;
        }
    }
    ArrayList<ConstraintViolationChecker> m_cvViolationCheckers;
    DoubleRange[] m_pcParRanges;
    int m_nIterations,m_nEvaluations;
    int m_nIterations0;
    int m_nTrailLength=100;
    int m_nHi,m_nLo;
    double m_dHi, m_dLo;
    VertexNode[] m_pcVertexTrail;

    public SimplexMonitor(int nDim,ArrayList<ConstraintViolationChecker> checkers){
        m_pcVertexTrail=new VertexNode[m_nTrailLength];
        m_cvViolationCheckers=checkers;
        m_nDim=nDim;

        /*
        m_pdvOptTrail=new ArrayList();
        m_pdvOptTrail.add(CommonStatisticsMethods.copyArray(simplex));
        m_pdvVertexHeights=new ArrayList();
        m_pdvVertexHeights.add(CommonStatisticsMethods.copyArray(pdVertexHeights));
        m_nvHiIndexes=new ArrayList();
        m_nvLoIndexes=new ArrayList();
        m_pnvViolationIndexes=new ArrayList();*/
        m_pcParRanges=new DoubleRange[m_nDim];
        m_nIterations=0;
//        findHiANDLoIndexes(pdVertexHeights);
    }
    /*
    void findHiANDLoIndexes(double[] pdVertexHeights){
        int i;
        m_dHi=Double.NEGATIVE_INFINITY;
        m_dLo=Double.POSITIVE_INFINITY;
        double dV;
        for(i=0;i<m_nDim+1;i++){
            dV=pdVertexHeights[i];
            if(dV>m_dHi){
                m_dHi=dV;
                m_nHi=i;
            }
            if(dV<m_dLo){
                m_dLo=dV;
                m_nLo=i;
            }
        }
    }
     *
     */
    void updateTrail(double[] pdPars, double dV, ArrayList<Integer> nvViolatedIndexes, ArrayList<Double> dvPenalties,double penalty, int nIterations, int nEvaluations){
        m_nIterations0=m_nIterations;
        m_nIterations=nIterations;
        m_nEvaluations=nEvaluations;
        boolean bAcepted=false;
        int index=nEvaluations%m_nTrailLength;

        if(dV<m_dLo){
            m_nLo=index;
            m_dLo=dV;
            bAcepted=true;
        }else if(dV>m_dHi){
            m_nHi=index;
            m_dHi=dV;
        }
        
        VertexNode aNode=m_pcVertexTrail[index];
        if(aNode==null){
            aNode=new VertexNode();
            m_pcVertexTrail[index]=aNode;
        }
        aNode.update(dV, pdPars, nvViolatedIndexes, dvPenalties, penalty, bAcepted);
    }
    public boolean GeneuenlyConverged(){
        return (getLowerVertices().size()==0);
    }
    public ArrayList<Integer> getLowerVertices(){//this method returns the indexes of the vertices at whitch
        //the optimization function values without counting the penalties are lower than the final converged vertex
        int i,index=m_nEvaluations%m_nTrailLength;
        int len=Math.min(m_nTrailLength, m_nEvaluations);
        ArrayList<Integer> lowerVertexIndexes=new ArrayList();

        VertexNode vNode=m_pcVertexTrail[index];
        double dv0=vNode.dV,dv,penalty0=vNode.penalty,penalty;
        dv0-=penalty0;
        for(i=0;i<len-1;i++){
            index=CommonMethods.circularAddition(len, index, -1);
            vNode=m_pcVertexTrail[index];
            dv=vNode.dV;
            penalty=vNode.penalty;
            dv-=penalty;
            if(dv<dv0) {
                lowerVertexIndexes.add(i);
            }
        }
        return lowerVertexIndexes;
    }
    public ArrayList<Integer> getHardConstraintIndexes(){
        ArrayList<Integer> indexes=getLowerVertices();
        int len=indexes.size();
        if(len==0) return indexes;
        VertexNode vNode;
        int i,index,nlo=0;
        double dlo=Double.POSITIVE_INFINITY,dv;
        for(i=0;i<len;i++){
            index=indexes.get(i);
            vNode=m_pcVertexTrail[index];
            dv=vNode.dV-vNode.penalty;
            if(dv<dlo){
                dlo=dv;
                nlo=index;
            }
        }
        vNode=m_pcVertexTrail[nlo];
        ArrayList<Integer> nvHardConstraintIndexes=vNode.nvViolatedIndexes;
        return nvHardConstraintIndexes;
    }
}
