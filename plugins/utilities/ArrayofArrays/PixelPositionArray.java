/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;
import java.util.ArrayList;
import ImageAnalysis.PixelPositionNode;

/**
 *
 * @author Taihao
 */
public class PixelPositionArray {
    public ArrayList <PixelPositionNode> m_PixelPositionArray;
    public PixelPositionArray (){
        m_PixelPositionArray= new ArrayList <PixelPositionNode>();
    }
    public int findNext_XY(PixelPositionNode ppn0, int i0){
        int index=-1,i;
        int size=m_PixelPositionArray.size();
        for(i=i0;i<size;i++){
            if(m_PixelPositionArray.get(i).equals_XY(ppn0)){
                index=i;;
                break;
            }
        }
        return index;
    }
    public int findNext_OL(PixelPositionNode ppn0, int i0){
        int index=-1,i;
        int size=m_PixelPositionArray.size();
        for(i=i0;i<size;i++){
            if(m_PixelPositionArray.get(i).equals_LO(ppn0)){
                index=i;;
                break;
            }
        }
        return index;
    }
}
