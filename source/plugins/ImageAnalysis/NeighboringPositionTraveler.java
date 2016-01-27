/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.awt.Point;
import java.util.ArrayList;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class NeighboringPositionTraveler {
    static public final Point origion=new Point(0,0);
    static public final int Xo=0,Yo=0;
    static public final int[] positionIndexes0={0,1,2};
    static public final int[] positionIndexes1={7,8,3};
    static public final int[] positionIndexes2={6,5,4};
    static public final int[][]positionIndexes={positionIndexes0,positionIndexes1,positionIndexes2};
    static public final Point[] positions={new Point(-1,-1), new Point(0,-1), new Point(1,-1),
    new Point(1,0), new Point(1,1), new Point(0,1),new Point(-1,1), new Point(-1,0)};
    public static Point nextPosition(int x, int y, int direction){//this method returns the position next to the point (x,y)
        //based on direction. this method assume the position of the caller is (0,0). it returns right side neighboring position
        //when direction is 1.
        int index=positionIndexes[y+1][x+1];
        index+=direction;
        if(index<0) index=7;
        if(index>7) index=0;
        return new Point(positions[index]);
    }
    public static Point getDirectNeighbor(int index){//this method returns the position next to the point (x,y)
        //based on direction. this method assume the position of the caller is (0,0). it returns right side neighboring position
        //when direction is 1.
        return new Point(positions[2*index+1]);
    }
    public static boolean contactingOrigion(int x, int y){
        int ax=Math.abs(x),ay=Math.abs(y);
        if(ax+ay==0) return false;
        if(ax>1||ay>1) return false;
        return true;
    }
    public static ArrayList<Point> getPath(int x0, int y0, int x1, int y1, int direction){//not including the two end
        //points
        ArrayList<Point> path=new ArrayList();
        if(!contactingOrigion(x0,y0)||!contactingOrigion(x1,y1)){
//            IJ.error("the point (x0,y0) must contact with (0,0), getPath");
            return path;
        }
        Point pt=nextPosition(x0,y0,direction);
        while(pt.x!=x1||pt.y!=y1){
            path.add(pt);
            pt=nextPosition(pt.x,pt.y,direction);
        }
        return path;
    }
}
