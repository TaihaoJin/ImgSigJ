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
public abstract class ConstraintViolationChecker {
    boolean m_bConstraintOn;
    ArrayList<Integer> m_nvConstrainedParIndexes;
    public abstract boolean ConstraintsViolated(double[] pdPars);
    public void setConstrainedParIndexes(ArrayList<Integer> nvConstrainedParIndexes){
        m_nvConstrainedParIndexes=nvConstrainedParIndexes;
    }
    public abstract ConstraintViolationChecker copy_IndependentConstrainedParIndexes();//
    public abstract void applyConstraint();//making this abstract is to make sure that all subclass turn in on within the
    //constructor;
    public void relaxConstraint(){
        m_bConstraintOn=false;
    }
    public boolean ConstraintOn(){
        return m_bConstraintOn;
    }
}
