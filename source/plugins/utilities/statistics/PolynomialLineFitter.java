/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public abstract class PolynomialLineFitter {
    public abstract PolynomialLineFittingSegmentNode[] getOptimalRegressions();
    public abstract PolynomialLineFittingSegmentNode[] getStartingRegressions();
    public abstract PolynomialLineFittingSegmentNode[] getEndingRegressions();
    public abstract PolynomialLineFittingSegmentNode[] getLongRegressions();
    public abstract double[] getDataX();
    public abstract double[] getDataY();
    public abstract int getMaxRisingInterval();
    public abstract boolean[] getDataSelection();
    public abstract int[] getRisingIntervals();
    public abstract PolynomialLineFittingSegmentNode getLineSegment(int start, int end);
    public abstract void setSelection(boolean[] pbSelected);
    public abstract intRange getSmoothRange(int position);
}
