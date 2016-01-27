/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.Constrains;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;

/**
 *
 * @author Taihao
 */
public class DistantConstraintFunction extends ConstraintFunction{
    String m_sConstraintType;
    double[] m_pdOrigion,m_pdConstraintFuncPars;
    int m_nDim,m_nNumConstraintFuncPars;
    public DistantConstraintFunction(String sConstraintType, ArrayList<Integer> indexes, double[] pdOrigion, double[] pdConstraintFuncPars){
        m_sConstraintType=sConstraintType;
        m_nvConstrainedParIndexes=indexes;
        m_nDim=indexes.size();
        m_nNumConstraintFuncPars=pdConstraintFuncPars.length;
        m_pdOrigion=pdOrigion;
        m_pdConstraintFuncPars=pdConstraintFuncPars;
    }
    public ConstraintFunction copy_IndependentConstrainedParIndexes(){
        ArrayList<Integer>indexes=new ArrayList();
        CommonStatisticsMethods.copyArray(m_nvConstrainedParIndexes, indexes);
        return new DistantConstraintFunction(m_sConstraintType,indexes,m_pdOrigion,m_pdConstraintFuncPars);
    }
    public double getPenalty(double[] pdPars){
        double penalty=0;
        double dist,x0,x;
        int i,index;
        if(m_sConstraintType.contentEquals("exponential")){
            double k=m_pdConstraintFuncPars[0];
            double a=m_pdConstraintFuncPars[1];
            dist=0;
            for(i=0;i<m_nDim;i++){
                index=m_nvConstrainedParIndexes.get(i);
                x0=m_pdOrigion[i];
                x=pdPars[index];
                dist+=(x-x0)*(x-x0);
            }
            dist=Math.sqrt(dist);
            penalty=k*Math.exp(a*dist);
        }
        return penalty;
    }
}
