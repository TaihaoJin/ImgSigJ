/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.Constrains;
import utilities.Geometry.ImageShapes.ImageShape;
import java.util.ArrayList;
import java.awt.Point;
import utilities.CommonStatisticsMethods;

/**
 *
 * @author Taihao
 */
public class ConstraintLocationChecker_ImageShape extends ConstraintViolationChecker{
    ImageShape m_cIS;
    Point location;
    public ConstraintLocationChecker_ImageShape(ArrayList<Integer> indexes, ImageShape cIS){
        m_nvConstrainedParIndexes=indexes;
        m_cIS=cIS;
        applyConstraint();
    }
    public ConstraintViolationChecker copy_IndependentConstrainedParIndexes(){
        ArrayList <Integer> indexes=new ArrayList();
        CommonStatisticsMethods.copyArray(m_nvConstrainedParIndexes, indexes);
        return new ConstraintLocationChecker_ImageShape(indexes,m_cIS);
    }
    public boolean ConstraintsViolated(double[] pdPars){
        if(!m_bConstraintOn) return false;
        int x=(int)(pdPars[m_nvConstrainedParIndexes.get(0)]+0.5);
        int y=(int)(pdPars[m_nvConstrainedParIndexes.get(1)]+0.5);
        location=new Point(x,y);
        return !m_cIS.contains(location);
    }
    public void applyConstraint(){
        m_bConstraintOn=true;
    }
}
