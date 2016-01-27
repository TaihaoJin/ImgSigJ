/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;
import utilities.CustomDataTypes.DoublePair;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public interface TwoDFunction {
    public double func(double x, double y);
    public DoublePair getPeak();
    public DoubleRange getValueRange();
    public void getFrameRanges(DoubleRange xFRange, DoubleRange yFRange);
    public double getHeight();
    public double getHeight(DoublePair dp);
    public int getTowDFunctionType();
}
