/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.CustomDataTypes;

/**
 *
 * @author Taihao
 */
public class DoublePair {
    public double left,right;
    public DoublePair(){        
    }
    public DoublePair(DoublePair p){   
        left=p.left;
        right=p.right;
    }
    public DoublePair(double left, double right){
        this.left=left;
        this.right=right;
    }
    public void setValue(DoublePair p){
        left=p.left;
        right=p.right;
    }
    public void setValue(double left, double right){
        this.left=left;
        this.right=right;
    }
}
