/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

/**
 *
 * @author Taihao
 */
public abstract class Comparable {
    int[] nArray;
    public abstract boolean smaller(Comparable a);
    public abstract boolean greater(Comparable a);
    public boolean equal(Comparable a){
        if(smaller(a)) return false;
        if(greater(a)) return false;
        return true;
    }
}
