/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.Gui;

/**
 *
 * @author Taihao
 */
public class GeneralManager{
    public static AnalysisMasterForm MasterForm;
    public static AnalysisMasterForm getMasterForm(){
        if(MasterForm==null){
            AnalysisMasterForm.main(null);
            while(MasterForm==null){
                try{Thread.sleep(100);}
                catch (InterruptedException e){};
            }
        }
        return MasterForm;
    }
}
