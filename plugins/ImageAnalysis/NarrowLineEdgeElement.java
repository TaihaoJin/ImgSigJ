/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import utilities.ArrayofArrays.IntPair;
import utilities.ArrayofArrays.IntPair2;
/**
 *
 * @author Taihao
 */
public class NarrowLineEdgeElement {
    public IntPair center;
    int[] direction; 
    /*
    //this is the direction of the edge, which is perpendicular to the gradient.
    //the direction of the gradient is related to the direction of the edge by dxo=-dy and dyo=dx;
    //The direction is such that the inside is on left side when edges are closed, and the direction of the gradient pointing from inside
     * to outside. 
     * */
    double gradient;
    ArrayList <IntPair2> IOBounds;//Inner and outer boundary
    ArrayList <IntPair2> IOLayers;//Inner and outer layer
    /*The elements of the outerBounds and innerBounds need to be paired, i.e., the elements with
     */ 
    public NarrowLineEdgeElement(){
        IOBounds=new ArrayList <IntPair2>();
        IOLayers=new ArrayList <IntPair2>();
        direction=new int[2];
        center=new IntPair();
    }
    public NarrowLineEdgeElement(int dx, int dy, IntPair center, double g, ArrayList <IntPair> outerBounds,
            ArrayList <IntPair> inenrBounds,ArrayList <IntPair> outlayers, ArrayList <IntPair> innerLayers){
        
    }
    public void getBoundsAndLayers(){
        int dy=direction[0], dx=direction[1];
        IOBounds.clear();
        IOLayers.clear();
        int i0=center.y,j0=center.x;
        int o=dy*10+dx;
        int i,ws=2;
        switch (o){
            case 1: //direction 1, dy=0, dx=1
                for(i=-1;i<1;i++){
                    IOBounds.add(new IntPair2(new IntPair(j0+i,i0-1),new IntPair(j0+i,i0+1)));
                    IOLayers.add(new IntPair2(new IntPair(j0+i,i0-2),new IntPair(j0+i,i0+2)));
                }
                
            case -8: //direction 2, dy=-1, dx=2
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0),new IntPair(j0,i0+2)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0-1),new IntPair(j0,i0+1)));
                IOBounds.add(new IntPair2(new IntPair(j0,i0-1),new IntPair(j0+1,i0+1)));
                IOBounds.add(new IntPair2(new IntPair(j0,i0-2),new IntPair(j0+1,i0)));
                                        
                IOLayers.add(new IntPair2(new IntPair(j0-3,i0-1),new IntPair(j0-1,i0+3)));
                IOLayers.add(new IntPair2(new IntPair(j0-1,i0-2),new IntPair(j0+1,i0+2)));
                IOLayers.add(new IntPair2(new IntPair(j0+1,i0-3),new IntPair(j0+3,i0+1)));
                
            case -9: //direction 3, dy=-1, dx=1
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0),new IntPair(j0,i0+1)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0-1),new IntPair(j0+1,i0+1)));
                IOBounds.add(new IntPair2(new IntPair(j0,i0-1),new IntPair(j0+1,i0)));
                                        
                IOLayers.add(new IntPair2(new IntPair(j0-2,i0-1),new IntPair(j0+1,i0+2)));
                IOLayers.add(new IntPair2(new IntPair(j0-2,i0-2),new IntPair(j0+2,i0+2)));
                IOLayers.add(new IntPair2(new IntPair(j0-1,i0-2),new IntPair(j0+2,i0+1)));
                    
            case -19: //direction 4, dy=-2, dx=1
                IOBounds.add(new IntPair2(new IntPair(j0-2,i0),new IntPair(j0,i0+1)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0),new IntPair(j0+1,i0+1)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0-1),new IntPair(j0+1,i0)));
                IOBounds.add(new IntPair2(new IntPair(j0,i0-1),new IntPair(j0+2,i0)));
                                        
                IOLayers.add(new IntPair2(new IntPair(j0-3,i0+1),new IntPair(j0+1,i0+3)));
                IOLayers.add(new IntPair2(new IntPair(j0-2,i0-1),new IntPair(j0+2,i0+1)));
                IOLayers.add(new IntPair2(new IntPair(j0-1,i0-3),new IntPair(j0+3,i0-1)));
                
            case 10: //direction 1, dy=0, dx=1
                for(i=-1;i<1;i++){
                    IOBounds.add(new IntPair2(new IntPair(j0-1,i0+i),new IntPair(j0+1,i0+i)));
                    IOLayers.add(new IntPair2(new IntPair(j0-2,i0+i),new IntPair(j0+2,i0+i)));
                }
            case -21: //direction 6, dy=-2, dx=-1
                IOBounds.add(new IntPair2(new IntPair(j0,i0+1),new IntPair(j0+2,i0)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0+1),new IntPair(j0+1,i0)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0),new IntPair(j0+1,i0-1)));
                IOBounds.add(new IntPair2(new IntPair(j0-2,i0),new IntPair(j0,i0-1)));
                                        
                IOLayers.add(new IntPair2(new IntPair(j0-1,i0+3),new IntPair(j0+3,i0+1)));
                IOLayers.add(new IntPair2(new IntPair(j0-2,i0+1),new IntPair(j0+2,i0-1)));
                IOLayers.add(new IntPair2(new IntPair(j0-3,i0-1),new IntPair(j0+1,i0-3)));
                
            case -11: //direction 7, dy=-1, dx=-1
                IOBounds.add(new IntPair2(new IntPair(j0,i0+1),new IntPair(j0+1,i0)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0+1),new IntPair(j0+1,i0-1)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0),new IntPair(j0,i0-1)));
                                        
                IOLayers.add(new IntPair2(new IntPair(j0-1,i0+2),new IntPair(j0+2,i0-1)));
                IOLayers.add(new IntPair2(new IntPair(j0-2,i0+2),new IntPair(j0+2,i0-2)));
                IOLayers.add(new IntPair2(new IntPair(j0-2,i0+1),new IntPair(j0+1,i0-2)));
                    
            case -12: //direction 8, dy=-1, dx=-2
                IOBounds.add(new IntPair2(new IntPair(j0,i0+2),new IntPair(j0+1,i0)));
                IOBounds.add(new IntPair2(new IntPair(j0,i0+1),new IntPair(j0+1,i0-1)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0+1),new IntPair(j0,i0-1)));
                IOBounds.add(new IntPair2(new IntPair(j0-1,i0),new IntPair(j0,i0-2)));
                                        
                IOLayers.add(new IntPair2(new IntPair(j0+1,i0+3),new IntPair(j0+3,i0-1)));
                IOLayers.add(new IntPair2(new IntPair(j0-1,i0+2),new IntPair(j0+1,i0-2)));
                IOLayers.add(new IntPair2(new IntPair(j0-3,i0+1),new IntPair(j0-1,i0-3)));
        }
    }
    public void getBoundsAndLayers_Iterate(){
        int dy=direction[0], dx=direction[1];
        IOBounds.clear();
        IOLayers.clear();
        int i0=center.y,j0=center.x;
        int o=dy*10+dx;
        int i,ws=2;
    }
}
