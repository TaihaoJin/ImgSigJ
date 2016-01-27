/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.Enumeration;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class StringEnumeration implements Enumeration{//not completed yet
    ArrayList <String> m_vsElements;
    int index;
    public StringEnumeration(){
        index=0;
        m_vsElements=new ArrayList();
    }
    public String nextElement(){
        return m_vsElements.get(index);
    }
    public boolean hasMoreElements(){
        return index>=m_vsElements.size();
    }
}
