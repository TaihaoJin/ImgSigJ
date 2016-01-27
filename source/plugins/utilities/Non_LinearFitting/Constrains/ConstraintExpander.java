/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.Constrains;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class ConstraintExpander {
//    ArrayList<ConstraintFunction> m_cvConstriantFunctionsPerTerm;
//    ArrayList<ConstraintViolationChecker> m_cvConstraintViolationCheckersPerTerm;
    ArrayList<ConstraintNode> m_cvConstraintNodesPerTerm;
    int m_nConstraintsPerTerm;
    public ConstraintExpander(ArrayList<ConstraintFunction> functions, ArrayList<ConstraintViolationChecker> checkers){
        m_nConstraintsPerTerm=functions.size();
        for(int i=0;i<m_nConstraintsPerTerm;i++){
            ConstraintNode aNode=new ConstraintNode(functions.get(i),checkers.get(i));
            m_cvConstraintNodesPerTerm.add(aNode);
        }
    }
    public ConstraintExpander(ArrayList<ConstraintNode> cvNodes){
        m_nConstraintsPerTerm=cvNodes.size();
        m_cvConstraintNodesPerTerm=new ArrayList();
        for(int i=0;i<m_nConstraintsPerTerm;i++){
            m_cvConstraintNodesPerTerm.add(cvNodes.get(i));
        }
    }
    public ArrayList<ConstraintNode> getConstraintNodes(int nIndexOffset){
        int i,j,len;
        ArrayList<Integer> indexes,indexes0;
        ConstraintFunction function;
        ConstraintViolationChecker checker;
        ArrayList<ConstraintNode> cNodes=new ArrayList();
        for(i=0;i<m_nConstraintsPerTerm;i++){
            function=m_cvConstraintNodesPerTerm.get(i).function.copy_IndependentConstrainedParIndexes();
            checker=m_cvConstraintNodesPerTerm.get(i).checker.copy_IndependentConstrainedParIndexes();
            indexes0=function.m_nvConstrainedParIndexes;
            indexes=new ArrayList();
            len=indexes0.size();
            for(j=0;j<len;j++){
                indexes.add(indexes0.get(j)+nIndexOffset);
            }
            function.setConstrainedParIndexes(indexes);
            checker.setConstrainedParIndexes(indexes);
            cNodes.add(new ConstraintNode(function,checker));
        }
        return cNodes;
    }
}
