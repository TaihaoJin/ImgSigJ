/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import java.util.*;
import utilities.ArrayofArrays.IntArray2;

/**
 *
 * @author Taihao
 */
public class IntArray3 {
    public ArrayList <IntArray2> m_IntArray3;
    public IntArray3(){
        m_IntArray3=new ArrayList<IntArray2>();
    }
    public IntArray3(int capacity){
        m_IntArray3=new ArrayList<IntArray2>(capacity);
    }
    IntArray3(IntArray3 a){
        this(a.m_IntArray3.size());
        int nSize=a.m_IntArray3.size();        
        for(int i=0;i<nSize;i++){
            IntArray2 b=new IntArray2(a.m_IntArray3.get(i));
            a.m_IntArray3.add(b);
        }        
    }
}

