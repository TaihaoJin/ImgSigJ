/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import java.util.*;
import utilities.ArrayofArrays.IntArray3;

/**
 *
 * @author Taihao
 */
public class IntArray4 {
    public ArrayList <IntArray3> m_IntArray4;
    IntArray4(){
        m_IntArray4=new ArrayList<IntArray3>();
    }
    IntArray4(IntArray4 a){
        this();
        int nSize=a.m_IntArray4.size();        
        for(int i=0;i<nSize;i++){
            IntArray3 b=new IntArray3(a.m_IntArray4.get(i));
            a.m_IntArray4.add(b);
        }        
    }

}
