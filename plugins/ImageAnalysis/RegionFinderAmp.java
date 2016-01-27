/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import java.awt.Point;
import utilities.CustomDataTypes.intRange;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class RegionFinderAmp {
    public static int findRegion(int[][] pixels, int[][]stamp, int w, int h, ArrayList <Point> region, ArrayList <Point> borderPoints, int index, Point p0, intRange ir){//need to be sure that p0 is matching the region
        if(ir.contains(pixels[p0.y][p0.x])&&stamp[p0.y][p0.x]!=-2){
            region.add(p0);
            stamp[p0.y][p0.x]=-2;
        }
        recruitNeighbors(pixels, stamp, w, h, region, borderPoints, p0, ir);
        index++;
        int nSize=region.size();
        if(index<nSize) findRegion(pixels,stamp,w,h,region,borderPoints, index,region.get(index),ir);
        return 1;
    }
    public static boolean matchingAmp(int[][] pixels, int[][]stamp, Point p, intRange ir){
        int x=p.x,y=p.y;
        if(!ir.contains(pixels[y][x])) return false;
        if (stamp[y][x]==-2) return false;
        return true;
    }
    public static void recruitNeighbors(int[][] pixels, int[][]stamp, int w, int h, ArrayList <Point> region, ArrayList <Point> borderPoints, Point p0, intRange ir){
        int x0=p0.x,y0=p0.y;
        int dx,dy,x,y;
        for(dy=-1;dy<=1;dy++){
            y=y0+dy;
            if(y<0||y>=h) continue;
            for(dx=-1;dx<=1;dx++){
                if(dy==0&&dx==0) continue;
                x=x0+dx;
                if(x<0||x>=w) continue;
                Point p=new Point(x,y);
                if(ir.contains(pixels[y][x])) {
                    if(stamp[y][x]!=-2){
                        region.add(p);
                        stamp[y][x]=-2;
                    }
                }else{
                    if(!ir.contains(pixels[y][x])){
                        if(CommonMethods.findPoint(borderPoints, p).size()==0) borderPoints.add(p);
                    }
                }
            }
        }
    }
}
