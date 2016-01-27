/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import utilities.ArrayofArrays.IntPair;

/**
 *
 * @author Taihao
 */
public class IntPair2 {
    public IntPair pair1, pair2;
    public IntPair2(){
        pair1=new IntPair();
        pair2=new IntPair();
    }
    public IntPair2(IntPair ip1, IntPair ip2){        
        pair1=new IntPair(ip1);
        pair2=new IntPair(ip2);
    }
}
