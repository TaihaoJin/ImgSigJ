/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;
import ij.plugin.PlugIn;
import utilities.Gui.HTSAnalysisForm;

/**
 *
 * @author Taihao
 */
public class HTS_Analysis implements PlugIn{
    public void run(String arg){
        HTSAnalysisForm.main(null);
    }
}
