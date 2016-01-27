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
public class StringArray {
    public ArrayList <String> m_stringArray;
    public StringArray(){
        m_stringArray=new ArrayList <String>();
    }
    public StringArray(ArrayList <String> sa){
        int nSize=sa.size();
        for(int i=0;i<nSize;i++){
            m_stringArray.add(sa.get(i));
        }
    }
}
