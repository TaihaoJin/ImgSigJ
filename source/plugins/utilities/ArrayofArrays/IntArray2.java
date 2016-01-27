/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.ArrayofArrays;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class IntArray2 {
    public ArrayList <IntArray> m_IntArray2;
    public IntArray2(){
        m_IntArray2=new ArrayList<IntArray>();
    }
    public IntArray2(int capacity){
        m_IntArray2=new ArrayList<IntArray>(capacity);
    }
    public IntArray2(IntArray2 a){
        this();
        int nSize=a.m_IntArray2.size();        
        for(int i=0;i<nSize;i++){
            IntArray b=new IntArray(a.m_IntArray2.get(i));
            a.m_IntArray2.add(b);
        }        
    }
}
