/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.DataClasses;
/**
 *
 * @author Taihao
 */
public class AtlasVoxelNode  {
    public int x,y,z,id;//The coordinate of the voxel and its informatic id    
    public boolean greater(AtlasVoxelNode aNode){
        if(id>aNode.id)return true;
        if(id<aNode.id)return false;
        if(z>aNode.z) return true;        
        if(z<aNode.z) return false;        
        if(y>aNode.y) return true;
        if(y<aNode.y) return false;
        if(x>aNode.x) return true;
        if(x<aNode.x) return false;
        return false;
    }
    
    public boolean smaller(AtlasVoxelNode aNode){
        if(id<aNode.id)return true;
        if(id>aNode.id)return false;
        if(z<aNode.z) return true;        
        if(z>aNode.z) return false;        
        if(y<aNode.y) return true;
        if(y>aNode.y) return false;
        if(x<aNode.x) return true;
        if(x>aNode.x) return false;
        return false;
    }
}
