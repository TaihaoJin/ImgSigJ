/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import FluoObjects.IPOGaussianNode;
import java.util.Set;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class IPOGaussianNodeCluster {
    public ArrayList<IPOGaussianNode> m_cvIPONodes;
    public int cIndex;
    public IPOGaussianNodeCluster(ArrayList<IPOGaussianNode> IPOs){
        m_cvIPONodes=IPOs;
    }
}
