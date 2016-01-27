/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import BrainSimulations.DataClasses.*;
import BrainSimulations.polyhedron.*;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class BoxSimulator extends Polyhedron{
    public BoxSimulator(){
        super(8,12,6);
    }
    public BoxSimulator(float[][] vtx){
        this();
        for(int i=0;i<V;i++){
            vertices[i].setIndex(i);
            for(int j=0;j<3;j++){
                vertices[i].setCoordinate(vtx[i][j], j);
            }
        }
        constructFaces();
        setRanges();
    }
    void constructFaces(){
        ArrayList<Integer> ia=new ArrayList <Integer>();
        ia.add(0);
        ia.add(1);
        ia.add(3);
        ia.add(2);
        faces[0]=constructPolygon(ia);
        
        ia.clear();
        ia.add(2);
        ia.add(3);
        ia.add(7);
        ia.add(6);
        faces[1]=constructPolygon(ia);
        
        ia.clear();
        ia.add(6);
        ia.add(7);
        ia.add(5);
        ia.add(4);
        faces[2]=constructPolygon(ia);
               
        ia.clear();
        ia.add(4);
        ia.add(5);
        ia.add(1);
        ia.add(0);
        faces[3]=constructPolygon(ia);
        
        ia.clear();
        ia.add(5);
        ia.add(7);
        ia.add(3);
        ia.add(1);
        faces[4]=constructPolygon(ia);
        
        ia.clear();
        ia.add(0);
        ia.add(2);
        ia.add(6);
        ia.add(4);
        faces[5]=constructPolygon(ia);
    }
}        

