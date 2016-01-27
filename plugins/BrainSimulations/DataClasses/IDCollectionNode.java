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
public class IDCollectionNode {
    int nMin;
    ArrayList<IDNode> m_IDNodes;
    IDCollectionNode(){
        nMin=intRange.m_nPosInt;
        m_IDNodes=new ArrayList<IDNode>();
    }
    IDCollectionNode(ArrayList<IDNode> aNodes){
       this();
       this.expandCollection(aNodes);
    }
    public void expandCollection(ArrayList<IDNode> aNodes){
        int nSize=aNodes.size();
        for(int i=0;i<nSize;i++){
            m_IDNodes.add(aNodes.get(i));
        }        
    }
    public ArrayList<IDNode> getNodes(){
        ArrayList <IDNode> aNodes=new ArrayList<IDNode>();
        int nSize=m_IDNodes.size();
        for(int i=0;i<nSize;i++){
            aNodes.add(m_IDNodes.get(i));
        }
        return aNodes;
    }
}
