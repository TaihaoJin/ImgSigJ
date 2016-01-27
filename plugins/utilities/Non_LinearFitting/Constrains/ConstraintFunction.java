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
abstract public class  ConstraintFunction {
    ArrayList<Integer> m_nvConstrainedParIndexes;
    public abstract double getPenalty(double[] pdPars);
    public void setConstrainedParIndexes(ArrayList<Integer> nvConstrainedParIndexes){
        m_nvConstrainedParIndexes=nvConstrainedParIndexes;
    }
    public ArrayList<Integer> getConstrainedParIndexes(){
        return m_nvConstrainedParIndexes;
    }
    public abstract ConstraintFunction copy_IndependentConstrainedParIndexes();//
}
