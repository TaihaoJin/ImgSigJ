/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import utilities.ArrayofArrays.IntRangeArray;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class IntRangeArray2 {
    public ArrayList <IntRangeArray> m_IntRangeArray2;
    public IntRangeArray2(){
        m_IntRangeArray2=new ArrayList ();
    }
    public IntRangeArray2(ArrayList<IntRangeArray> ir2){
        this();
        for(int i=0;i<ir2.size();i++){
            m_IntRangeArray2.add(ir2.get(i));
        }
    }
}
