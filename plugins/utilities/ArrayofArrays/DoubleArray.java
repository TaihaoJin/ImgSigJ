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
public class DoubleArray {
    public ArrayList <Double> m_DoubleArray;
    public DoubleArray(){m_DoubleArray=new ArrayList <Double>();}
    public DoubleArray(int capacity){m_DoubleArray=new ArrayList <Double>(capacity);}
    public DoubleArray(DoubleArray a){
        this();
        int nSize=a.m_DoubleArray.size();
        double nTemp;
        for(int i=0;i<nSize;i++){
            nTemp=a.m_DoubleArray.get(i);
            m_DoubleArray.add(nTemp);
        }
    }
    public DoubleArray(ArrayList<Double> da){
        this();
        int nSize=da.size();
        double dTemp;
        for(int i=0;i<nSize;i++){
            dTemp=da.get(i);
            m_DoubleArray.add(dTemp);
        }
    }
    public boolean containsContent(int n){
        int size=m_DoubleArray.size();
        for(int i=0;i<size;i++){
            if(n==m_DoubleArray.get(i)) return true;
        }
        return false;
    }
}
