/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.CustomDataTypes;

/**
 *
 * @author Taihao
 */
public class IntPair {
    public int left;
    public int right;
    public IntPair(){
        
    }
    public IntPair(int left, int right){
        this.left=left;
        this.right=right;
    }
    public void setPair(int left, int right){
        this.left=left;
        this.right=right;
    }
    public boolean contains(int iT){
        return (left==iT||right==iT);
    }
    
    public int getPairedNumber(int iT){
        if(iT==left) return right;
        if(iT==right) return left;
        return iT;
    }
}
