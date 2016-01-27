/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.Non_LinearFitting.Constrains;
import utilities.statistics.PolynomialLineFittingSegmentNode;

/**
 *
 * @author Taihao
 */
public abstract class ParValidityChecker {
    public abstract boolean invalidPars(double[] pars, double xi, double xf);
    public abstract boolean invalidPars(PolynomialLineFittingSegmentNode seg, double xi, double xf);
}
