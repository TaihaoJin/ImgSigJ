/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.CustomDataTypes;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class SelectionCriterionNode {
    public static final int Select=1,Exclude=-1,Ignore=0;
    public int option;
    public ArrayList<Object> ovCriteria;
    public ArrayList<Integer> nvLogicalOptions;
    public SelectionCriterionNode(int option){
        this.option=option;
        ovCriteria=new ArrayList();
        nvLogicalOptions=new ArrayList();
    }
    public SelectionCriterionNode(int option, Object criterion, int lo){
        this(option);
        addCriterion(criterion,lo);
    }
    public void addCriterion(Object criterion, int lo){
        ovCriteria.add(criterion);
        nvLogicalOptions.add(lo);
    }
}
