/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.polyhedron;

/**
 *
 * @author Taihao
 */
public class CrossPoint {
    /*A crossPoint insitance define a point where an edge of a polygon crosses
     * a plane. v indicate the cross point. The field index is introduced to 
     * be used when construct the cross_section polygon3D formed when a 
     * polyhedron crosses over a plane. When a plane crosses a polyhedron, the 
     * crosection of each face with the plane contributes an edge of the cross-
     * section polygon. The edge is formed by connecting the cross points where
     * the two edges of the face cross the plane.  The cross point of the edge 
     * whose dot product with the plane norm vector greater than 0 has the index
     * value 1, and constitue the second point (the head point) of the cross-
     * section edge. The other cross point will be assigned with index value 
     * of 0 and will form the first (tail) point of the edge.
     * 
    */
    public int index;
    vertex v;
    public CrossPoint(){
        v=new vertex();
    };
    void setCoordinate(double x0, int index){
       v.setCoordinate(x0,index);
    }
    public boolean overlap(CrossPoint cp){
        return v.overlap(cp.v);
    }
}
