/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
/**
 *
 * @author Taihao
 */
public class FittingModelComponentNode extends FittingModelNode{
    public FittingModelComponentNode (FittingModelNode aNode, int nComponent){
        super(aNode);
        String sFunctionType=aNode.svFunctionTypes.get(nComponent);
        svFunctionTypes.clear();
        svFunctionTypes.add(sFunctionType);
        ComposedFittingFunction func=new ComposedFittingFunction(aNode.svFunctionTypes);
        nNumPars=ComposedFittingFunction.getNumParsPerTerm(svFunctionTypes.get(0))+1;//to include the constant term for the formality requirement
        pdPars=func.getComponentPars(aNode.pdPars, nComponent);
        svParNames=func.getComponentParNames(nComponent);
        nComponents=1;
    }
}
