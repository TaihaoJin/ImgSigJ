/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.DataClasses;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class IDNode {
    int m_ID;
    public IDNode(){        
    }
    public IDNode(int ID){
        m_ID=ID;
    }
    public void setID(int ID){
        m_ID=ID;
    }
    public int getID(){
        return m_ID;
    }
}
