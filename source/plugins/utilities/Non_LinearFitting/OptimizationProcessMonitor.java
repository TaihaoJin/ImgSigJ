/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.Constrains.ConstraintViolationChecker;
import utilities.Non_LinearFitting.Constrains.ConstraintNode;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class OptimizationProcessMonitor {
    int m_nDim;
    ArrayList<ConstraintNode> m_cvConstraintNodes;
    ArrayList<ConstraintViolationChecker> m_cvViolationCheckers;
    DoubleRange[] m_pcParRanges;
    int m_nIterations,m_nEvaluations;
    int m_nIterations0;
    int m_nTrailLength=1000;
    int m_nHi,m_nLo;
    double m_dHi, m_dLo;
    OptimizationStepNode[] m_pcOptimizationSteps;

    public OptimizationProcessMonitor(int nDim,ArrayList<ConstraintNode> cvConstraintNodes){
        m_pcOptimizationSteps=new OptimizationStepNode[m_nTrailLength];
        m_cvViolationCheckers=new ArrayList();
        m_nDim=nDim;
        m_dHi=Double.NEGATIVE_INFINITY;
        m_dLo=Double.POSITIVE_INFINITY;

        m_pcParRanges=new DoubleRange[m_nDim];
        m_nIterations=0;
        int nConstraints=cvConstraintNodes.size();
        for(int i=0;i<nConstraints;i++){
            m_cvViolationCheckers.add(cvConstraintNodes.get(i).checker);
        }
    }
    void addStep(double[] pdPars, double dV, ArrayList<Integer> nvViolatedIndexes, ArrayList<Double> dvPenalties,double penalty, int nIterations, int nEvaluations){
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

        OptimizationStepNode aNode=m_pcOptimizationSteps[index];
        if(aNode==null){
            aNode=new OptimizationStepNode(m_nDim);
            m_pcOptimizationSteps[index]=aNode;
        }
        aNode.update(dV, pdPars, nvViolatedIndexes, dvPenalties, penalty, bAcepted);
    }
    public boolean GeneuenlyConverged(){
        return (getLowerStepIndexes().size()==0);
    }
    public ArrayList<Integer> getLowerStepIndexes(){//this method returns the indexes of the vertices at whitch
        //the optimization function values without counting the penalties are lower than the final converged vertex
        int i,index=m_nEvaluations%m_nTrailLength;
        int len=Math.min(m_nTrailLength, m_nEvaluations);
        ArrayList<Integer> lowerVertexIndexes=new ArrayList();

        OptimizationStepNode vNode=m_pcOptimizationSteps[index];
        double dv0=vNode.dV,dv,penalty0=vNode.penalty,penalty;
        dv0-=penalty0;
        for(i=0;i<len-1;i++){
            index=CommonMethods.circularAddition(len, index, -1);
            vNode=m_pcOptimizationSteps[index];
            dv=vNode.dV;
            penalty=vNode.penalty;
            if(penalty>0){
                penalty=penalty;
            }
            dv-=penalty;
            if(dv<dv0) {
                lowerVertexIndexes.add(index);
            }
        }
        return lowerVertexIndexes;
    }
    public OptimizationStepNode getLowestStep(){
        if(m_nEvaluations-m_nLo<m_nTrailLength) return m_pcOptimizationSteps[m_nLo];
        return getLowestStep(getLowerStepIndexes());
    }
    public OptimizationStepNode getLowestStep(ArrayList<Integer> indexes){
        int len=indexes.size();
        if(len==0) return m_pcOptimizationSteps[m_nLo];
        OptimizationStepNode vNode;
        int i,index,nlo=0;
        double dlo=Double.POSITIVE_INFINITY,dv;
        for(i=0;i<len;i++){
            index=indexes.get(i);
            vNode=m_pcOptimizationSteps[index];
            dv=vNode.dV-vNode.penalty;
            if(dv<dlo){
                dlo=dv;
                nlo=index;
            }
        }
        vNode=m_pcOptimizationSteps[nlo];
        return vNode;
    }
}
