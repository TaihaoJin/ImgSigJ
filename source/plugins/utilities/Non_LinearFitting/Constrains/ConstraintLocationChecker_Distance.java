/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.Constrains;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CommonStatisticsMethods;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class ConstraintLocationChecker_Distance extends ConstraintViolationChecker{
    int m_nDim;
    double[] m_pdOrigin;
    DoubleRange m_cDistRange;
    public ConstraintLocationChecker_Distance(ArrayList<Integer> indexes, double[] pdOrigin, DoubleRange cDistRange){
        m_nvConstrainedParIndexes=indexes;
        m_nDim=pdOrigin.length;
        m_nvConstrainedParIndexes=new ArrayList();
        m_pdOrigin=new double[m_nDim];
        m_cDistRange=new DoubleRange(cDistRange);
        for(int i=0;i<m_nDim;i++){
            m_pdOrigin[i]=pdOrigin[i];
            m_nvConstrainedParIndexes.add(indexes.get(i));
        }
        applyConstraint();
    }
    public ConstraintViolationChecker copy_IndependentConstrainedParIndexes(){
        ArrayList <Integer> indexes=new ArrayList();
        CommonStatisticsMethods.copyArray(m_nvConstrainedParIndexes, indexes);
        return new ConstraintLocationChecker_Distance(indexes,m_pdOrigin,m_cDistRange);
    }
    public boolean ConstraintsViolated(double[] pdPars){
        if(!m_bConstraintOn) return false;
        double dist=0;
        double x0,x;
        for(int i=0;i<m_nDim;i++){
            x0=m_pdOrigin[i];
            x=pdPars[m_nvConstrainedParIndexes.get(i)];
            dist+=(x-x0)*(x-x0);
        }
        dist=Math.sqrt(dist);
        boolean bViolated=!m_cDistRange.contains(dist);
        return bViolated;
    }
    public void applyConstraint(){
        m_bConstraintOn=true;
    }
}
