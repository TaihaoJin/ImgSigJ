/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ImageAnalysis.RegionFinderEI;
import java.util.ArrayList;
import java.awt.Point;
import utilities.CommonMethods;
import ImageAnalysis.LandscapeAnalyzer;

/**
 *
 * @author Taihao
 */
public class LocalMaximaStampper {
    public static final int localMaximum=LandscapeAnalyzer.localMaximum;
    public static final int regular=LandscapeAnalyzer.regular;
    public static final int initial=3;
//    public static void stampLocalMaxima(int [][]pixels, int [][]stamp, int w, int h, ArrayList<Point> localMaxima){
    public static void stampLocalMaxima(int [][]pixels, int [][]stamp, int w, int h){
        ArrayList <Point> region=new ArrayList();
        ArrayList <Point> border=new ArrayList();
//        localMaxima.clear();
        int i,j;
        int regionIndex=0;
        int[][] stampEI=new int[h][w];
        int[][] stampEIb=new int[h][w];
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                stamp[i][j]=localMaximum;
                stampEI[i][j]=0;
                stampEIb[i][j]=0;
            }
        }

        for(j=0;j<w;j++){
            stamp[0][j]=regular;
        }
        for(j=0;j<w;j++){
            stamp[h-1][j]=regular;
        }
        for(i=0;i<h;i++){
            stamp[i][0]=regular;
        }
        for(i=0;i<h;i++){
            stamp[i][w-1]=regular;
        }

        int cat=regular;
        int pixel,pixel0;
        int x,y;
        for(i=1;i<h-1;i++){
            for(j=1;j<w-1;j++){
                if(stampEI[i][j]>0) continue;//part of EI region and has been stampped
                pixel0=pixels[i][j];

                pixel=pixels[i][j+1];
                if(pixel==pixel0){
                    regionIndex++;
                    stampEIPoints(pixels,stamp, stampEI, stampEIb, w, h, region, border, new Point(j,i), pixel0, regionIndex);
                }else if(pixel0>pixel){
                    stamp[i][j+1]=regular;
                }else{
                    stamp[i][j]=regular;
                }
                y=i+1;
                for(x=j-1;x<=j+1;x++){
                    if(stampEI[y][x]>0) continue;
                    pixel=pixels[y][x];
                    if(pixel==pixel0){
                        regionIndex++;
                        stampEIPoints(pixels,stamp, stampEI, stampEIb, w, h, region, border, new Point(j,i), pixel0, regionIndex);
                    }else if(pixel0>pixel){
                        stamp[y][x]=regular;
                    }else{
                        stamp[i][j]=regular;
                    }
                }
            }
        }
    }
    public static void stampEIPoints(int[][] pixels,int [][]stamp, int[][] stampEI, int [][] stampEIb, int w, int h,ArrayList <Point> region, ArrayList<Point> border,Point p0, int pixel0, int regionIndex){

        RegionFinderEI.findRegion(pixels, stampEI, stampEIb, w, h, region, border, p0, pixel0, regionIndex,0,pixels.length-1,0,pixels[0].length-1);
        int len=border.size();
        Point p;
        int i,x,y;
        int cat=localMaximum;
        for(i=0;i<len;i++){
            p=border.get(i);
            x=p.x;
            y=p.y;
            if(pixels[y][x]>pixel0){
                cat=regular;
            }else{
                stamp[y][x]=regular;
            }
        }
        len=region.size();
        for(i=0;i<len;i++){
            p=region.get(i);
            stamp[p.y][p.x]=regular;
        }
        if(cat==localMaximum){
            p=CommonMethods.closestPointToCenter(region,w,h);
            stamp[p.y][p.x]=localMaximum;
        }
    }
}
