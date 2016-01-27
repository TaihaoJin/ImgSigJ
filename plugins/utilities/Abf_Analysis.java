/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;
import ij.plugin.PlugIn;
import utilities.AbfHandler.AbfAnalysisGui;

/**
 *
 * @author Taihao
 */
public class Abf_Analysis implements PlugIn{
    public void run(String arg){
        AbfAnalysisGui.main(null);
    }
}
