/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class EEUNPack {
    public ArrayList<EdgeElementUnitNode> m_eeuns;
    public EEUNPack(){
        m_eeuns=new ArrayList<EdgeElementUnitNode>();
    }
    public EEUNPack(EdgeElementUnitNode eeun){
        this();
        m_eeuns.add(eeun);
    }
    public void merge(EEUNPack eeunp){
        int size=eeunp.m_eeuns.size();
        for(int i=0;i<size;i++){
            m_eeuns.add(eeunp.m_eeuns.get(i));
        }
    }
}
