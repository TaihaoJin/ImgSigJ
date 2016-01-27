/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis.IJExtensions.gui;
import ij.gui.ImageWindow;

/**
 *
 * @author Taihao
 */
public class ImageWindowT extends ImageWindow{
    public void setLocationAndSize(int x, int y, int width, int height) {
		setBounds(x, y, width, height);
		getCanvas().fitToWindow();
		pack();
	}
}
