/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import java.util.ArrayList;
import utilities.ArrayofArrays.IntPairArray;
/**
 *
 * @author Taihao
 */
public class IntPairArray2 {
    public ArrayList <IntPairArray> m_IntPairArray2;
    public IntPairArray2(){
        m_IntPairArray2=new ArrayList <IntPairArray>();
    }
    public IntPairArray2(IntPairArray2 ipa2){
        this();
        int size=ipa2.m_IntPairArray2.size();
        for(int i=0;i<size;i++){
            m_IntPairArray2.add(new IntPairArray(ipa2.m_IntPairArray2.get(i)));
        }
    }
}
