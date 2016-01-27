/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import java.util.ArrayList;
import utilities.ArrayofArrays.IntPair;
/**
 *
 * @author Taihao
 */
public class IntPairArray {
    public ArrayList <IntPair> m_IntpairArray;
    public IntPairArray(IntPairArray ipa){
        this();
        int size=ipa.m_IntpairArray.size();
        for(int i=0;i<size;i++){
            m_IntpairArray.add(new IntPair(ipa.m_IntpairArray.get(i)));
        }
    }
    
    public IntPairArray(){
        m_IntpairArray=new ArrayList <IntPair>();
    }
    public boolean containContent(IntPair ip){
        int size=m_IntpairArray.size();
        for(int i=0;i<size;i++){
            if(m_IntpairArray.get(i).equals(ip)) return true;
        }
        return false;
    }
}
