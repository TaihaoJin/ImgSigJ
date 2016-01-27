/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;

/**
 *
 * @author Taihao
 */
public class IPOGDefaultNode extends IPOGaussianNode{
    public double getValue(String sID){
        if(sID.contentEquals("Amp")) return -10;
        if(sID.contentEquals("SignalCal")){
            return -10;
        }
        if(sID.contentEquals("Signal")){
            return -10;
        }
        if(sID.contentEquals("Height")){
            return -10;
        }
        if(sID.contentEquals("Background")){
            return 1000;
        }
        if(sID.contentEquals("Area")){
            return -10;
        }
        return Double.NaN;
    }
}
