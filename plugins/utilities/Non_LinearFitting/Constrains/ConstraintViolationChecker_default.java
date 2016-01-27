/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.Constrains;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class ConstraintViolationChecker_default extends ConstraintViolationChecker{
    public ConstraintViolationChecker_default(ArrayList<Integer> indexes){
        m_nvConstrainedParIndexes=indexes;
        applyConstraint();
    }
    public boolean ConstraintsViolated(double[] pdPars){
        if(!m_bConstraintOn) return false;
        return true;
    }
    public void setConstrainedParIndexes(ArrayList<Integer> nvConstrainedParIndexes){
        m_nvConstrainedParIndexes=nvConstrainedParIndexes;
    }
    public ConstraintViolationChecker copy_IndependentConstrainedParIndexes(){
        ArrayList <Integer> indexes=new ArrayList();
        utilities.CommonStatisticsMethods.copyArray(m_nvConstrainedParIndexes, indexes);
        return new ConstraintViolationChecker_default(indexes);
    }
    public void applyConstraint(){
        m_bConstraintOn=true;
    }
}
