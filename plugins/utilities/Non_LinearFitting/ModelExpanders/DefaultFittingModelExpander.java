/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.ModelExpanders;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.Non_LinearFitting.FittingModelExpander;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
/**
 *
 * @author Taihao
 */
public class DefaultFittingModelExpander implements FittingModelExpander{
     public boolean expandModel(Non_Linear_Fitter fitter, int nMaxNewComponents){
         return false;
     }
     public static FittingModelNode expandFittingModel(FittingModelNode aModel){
         return expandFittingModel(aModel,aModel.svFunctionTypes.get(aModel.nComponents-1));
     }
     public static FittingModelNode expandFittingModel(FittingModelNode aModel, String sFunctionType){
         FittingModelNode bModel=new FittingModelNode(aModel);
         bModel.addOneComponent(sFunctionType);
         return bModel;
     }
}
