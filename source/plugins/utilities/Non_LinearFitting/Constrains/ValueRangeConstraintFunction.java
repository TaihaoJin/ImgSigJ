/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.Constrains;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;
import utilities.CommonMethods;
/**
 *
 * @author Taihao
 */
public class ValueRangeConstraintFunction extends ConstraintFunction{
    String m_sConstraintType;
    double dMin,dMax;
    int m_nDim,m_nNumConstraintFuncPars;
    double[] m_pdConstraintFuncPars;
    public ValueRangeConstraintFunction(String sConstraintType, ArrayList<Integer> indexes, double dMin, double dMax, double[] pdConstraintFuncPars){
        m_sConstraintType=sConstraintType;
        m_nvConstrainedParIndexes=indexes;
        m_nDim=indexes.size();
        m_nNumConstraintFuncPars=pdConstraintFuncPars.length;
        m_pdConstraintFuncPars=pdConstraintFuncPars;
        this.dMin=dMin;
        this.dMax=dMax;
    }
    public ConstraintFunction copy_IndependentConstrainedParIndexes(){
        ArrayList<Integer>indexes=new ArrayList();
        CommonStatisticsMethods.copyArray(m_nvConstrainedParIndexes, indexes);
        return new ValueRangeConstraintFunction(m_sConstraintType,indexes,dMin,dMax,m_pdConstraintFuncPars);
    }
    public double getPenalty(double[] pdPars){
        double penalty=0;
        double dist,par;
        int index=m_nvConstrainedParIndexes.get(0);
        if(m_sConstraintType.contentEquals("exponential")){
            double k=m_pdConstraintFuncPars[0];
            double a=m_pdConstraintFuncPars[1];
            dist=0;
            par=pdPars[index];
            if(par<dMin) {
                dist=dMin-par;
                penalty=k*Math.exp(a*dist);
            }
            if(par>dMax) {
                dist=par-dMax;
                penalty=k*Math.exp(a*dist);
            }
        }
        return penalty;
    }
}
