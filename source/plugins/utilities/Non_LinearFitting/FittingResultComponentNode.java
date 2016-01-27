/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;

/**
 *
 * @author Taihao
 */
public class FittingResultComponentNode extends FittingResultsNode{
    public FittingResultComponentNode(FittingResultsNode aNode, int nModel){
        super(aNode,nModel,nModel,1,true);
        FittingModelNode aModel=m_cvModels.get(0);
        nModels=aModel.nComponents;
        m_cvModels.clear();
        for(int i=0;i<nModels;i++){
            m_cvModels.add(new FittingModelComponentNode(aModel,i));
        }
    }
}
