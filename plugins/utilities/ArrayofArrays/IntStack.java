/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import java.util.Stack;

/**
 *
 * @author Taihao
 */
public class IntStack {
    public Stack <Integer> m_intStack;
    public IntStack(){
        m_intStack=new Stack <Integer>();
    }
    public IntStack(int a){
        this();
        m_intStack.push(a);
    }
}
