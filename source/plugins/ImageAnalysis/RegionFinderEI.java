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
public class RegionFinderEI {
    static int defaultRegionIndex=0;
    public static int findRegion(int[][] pixels, int[][]stamp, int [][]stampb, int w, int h, ArrayList <Point> region, ArrayList <Point> borderPoints, Point p0, int pixel0){//need to be sure that p0 is matching the region
        return findRegion(pixels,stamp,stampb,w,h,region,borderPoints,p0,pixel0,0,h-1,0,w-1);
    }
    public static int findRegion(int[][] pixels, int[][]stamp, int [][]stampb, int w, int h, ArrayList <Point> region, ArrayList <Point> borderPoints, Point p0, int pixel0,int iI,int iF,int jI,int jF){//need to be sure that p0 is matching the region
        defaultRegionIndex++;
        return findRegion(pixels,stamp,stampb,w,h,region,borderPoints,p0,pixel0,defaultRegionIndex, iI,iF,jI,jF);
    }
    public static int findRegion(int[][] pixels, int[][] stamp, int[][] stampb, int w, int h, ArrayList<Point> region, ArrayList<Point> borderPoints, Point p0, int pixel0, int regionIndex, int iI,int iF, int jI,int jF) {//need to be sure that p0 is matching the region
        //this method fills "region" with points in the region of points have equal pixel values as pixel0 and stamp with regionIndex, it also fills "borderPoints" with all Points directly 
        //contacting with the points in the "region" and labels the position of the points in stamb with -regionIndex.
        //stampb is initialized with zero (every element is zero)
        region.clear();
        borderPoints.clear();
        region.add(p0);
        stamp[p0.y][p0.x]=regionIndex;
        int index=0;
        int nSize=region.size();
        while(index<nSize){
            recruitNeighbors(pixels,stamp,stampb,w, h,region,borderPoints,region.get(index),pixel0,regionIndex,iI,iF,jI,jF);
            index++;
            nSize=region.size();
        }
        if(regionIndex>defaultRegionIndex)defaultRegionIndex=regionIndex;//to ensure the default regionIndex always work
        return 1;
    }
    public int getDefaultRegionIndex(){
        return defaultRegionIndex;
    }
    public static void recruitNeighbors(int[][] pixels, int[][]stamp, int [][]stampb, int w, int h, ArrayList <Point> region, ArrayList <Point> borderPoints, Point p0, int pixel0,int regionIndex,int iI, int iF, int jI, int jF){
        int x0=p0.x,y0=p0.y;
        int dx,dy,x,y;
        for(dy=-1;dy<=1;dy++){
            y=y0+dy;
            if(y<iI||y>iF) continue;
            for(dx=-1;dx<=1;dx++){
                if(dy==0&&dx==0) continue;
                x=x0+dx;
                if(x<jI||x>jF) continue;
                Point p=new Point(x,y);
                if(pixels[p.y][p.x]==pixel0) {
                    if(stamp[y][x]!=regionIndex){
                        region.add(p);
                        stamp[y][x]=regionIndex;
                    }
                }else{
                    if(stampb[y][x]!=-regionIndex){
                        borderPoints.add(p);
                        stampb[y][x]=-regionIndex;
                    }
                }
            }
        }
    }
}
