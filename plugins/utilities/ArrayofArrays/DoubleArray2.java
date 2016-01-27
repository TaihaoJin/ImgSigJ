/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.ArrayofArrays;
import java.util.ArrayList;
import utilities.ArrayofArrays.*;
/**
 *
 * @author Taihao
 */
public class DoubleArray2 {
    public ArrayList <DoubleArray> m_DoubleArray2;
    public DoubleArray2(){
        m_DoubleArray2=new ArrayList<DoubleArray>();
    }
    public DoubleArray2(int capacity){
        m_DoubleArray2=new ArrayList<DoubleArray>(capacity);
    }
    public DoubleArray2(DoubleArray2 a){
        this();
        int nSize=a.m_DoubleArray2.size();
        for(int i=0;i<nSize;i++){
            DoubleArray b=new DoubleArray(a.m_DoubleArray2.get(i));
            a.m_DoubleArray2.add(b);
        }
     }
    public void add(DoubleArray a){
        m_DoubleArray2.add(a);
    }
}
