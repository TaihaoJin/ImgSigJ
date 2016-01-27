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
public class IntArray {
    public ArrayList <Integer> m_intArray;
    public IntArray(){m_intArray=new ArrayList <Integer>();}
    public IntArray(int capacity){m_intArray=new ArrayList <Integer>(capacity);}
    public IntArray(ArrayList<Integer> ir){
        this();
        int len=ir.size(),i;
        for(i=0;i<len;i++){
            m_intArray.add(ir.get(i));
        }
    }
    public IntArray(IntArray a){
        this();
        int nSize=a.m_intArray.size();
        int nTemp;
        for(int i=0;i<nSize;i++){
            nTemp=a.m_intArray.get(i);
            m_intArray.add(nTemp);
        }
    }
    public void appendContents(IntArray iar){
        int size=m_intArray.size();
        int size2=iar.m_intArray.size();
        int i,j,n;
        for(i=0;i<size2;i++){
            n=iar.m_intArray.get(i);
            if(!containsContent(n)) m_intArray.add(n);
        }
    }
    public boolean containsContent(int n){        
        int size=m_intArray.size();
        for(int i=0;i<size;i++){
            if(n==m_intArray.get(i)) return true;
        }
        return false;
    }
}
