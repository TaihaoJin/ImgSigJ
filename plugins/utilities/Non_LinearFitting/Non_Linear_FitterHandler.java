/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import utilities.Non_LinearFitting.ModelExpanders.DefaultFittingModelExpander;

/**
 *
 * @author Taihao
 */
public class Non_Linear_FitterHandler {
    static public FittingResultsNode getFullModelFitting(FittingModelNode aModel,double pThreshold){
        FittingResultsNode aResultsNode=Non_Linear_Fitter.getFittedModel_Simplex(aModel, aModel, 0.1, null);
        FittingModelNode bModel=DefaultFittingModelExpander.expandFittingModel(aModel);
        Non_Linear_Fitter.getFittedModel_Simplex(bModel, bModel, 0.1, null);
        double pValue=FittingComparison.getFittingComparison(aModel, bModel);

        while(pValue<pThreshold&&bModel!=null){
            aResultsNode.m_cvModels.add(bModel);
            aResultsNode.m_dvPValues.add(pValue);
            aModel=bModel;
            bModel=DefaultFittingModelExpander.expandFittingModel(aModel);
            Non_Linear_Fitter.getFittedModel_Simplex(bModel, bModel, 0.1, null);
            pValue=FittingComparison.getFittingComparison(aModel, bModel);
        }
        if(bModel==null){
            bModel=aModel;
            pValue=1;
        }
        aResultsNode.m_cvModels.add(bModel);
        aResultsNode.m_dvPValues.add(pValue);
        return aResultsNode;
    }

}
