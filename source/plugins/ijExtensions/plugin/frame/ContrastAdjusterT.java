/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ijExtensions.plugin.frame;
import ij.plugin.frame.ContrastAdjuster;
import ij.ImagePlus;

/**
 *
 * @author Taihao
 */
public class ContrastAdjusterT extends ContrastAdjuster{
	public void setMinAndMax(ImagePlus imp, double min, double max) {
            super.setMinAndMax(imp, min, max);
	}
}
