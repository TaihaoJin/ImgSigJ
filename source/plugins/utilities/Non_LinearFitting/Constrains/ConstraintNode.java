/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.Constrains;

/**
 *
 * @author Taihao
 */
public class ConstraintNode {
    public ConstraintFunction function;
    public ConstraintViolationChecker checker;
    public ConstraintNode(ConstraintFunction function, ConstraintViolationChecker checker){
        this.function=function;
        this.checker=checker;
    }
}
