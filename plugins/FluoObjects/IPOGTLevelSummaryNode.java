/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;

/**
 *
 * @author Taihao
 */
public class IPOGTLevelSummaryNode {
    String level,sliceI,sliceF;
    String mean,mainAmp,AmpRatio,sigmaX,sigmaY;//AmpRatio is the Ratio of the amp between the main caussian component and the second largest component.
    String dLeft,dRight,stepRatio,delta;//stepRatio=dRight/delta. delta is the difference from dRight of one level node and dLeft of
    //the immediate lower level node. 
}
