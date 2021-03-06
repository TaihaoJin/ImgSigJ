/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ImageAnalysis.ContourFollower;
import utilities.CustomDataTypes.intRange;
import ij.ImagePlus;
import java.util.ArrayList;
import ij.IJ;
import utilities.ArrayofArrays.IntArray;
import java.awt.Point;
import ImageAnalysis.GeneralGraphicalObject;
import utilities.ArrayofArrays.PointArray2;
import utilities.ArrayofArrays.GeneralGraphicalObjectArray;

/**
 *
 * @author Taihao
 */
public class LandscapeAnalyzer {
    public static final int regular=1, localMinimum=2, groove1=3, groove2=4, saddle1=5, saddle2=6, ridge1=7,ridge2=8, localMaximum=9,watershed=10;
    public static final int nNumCats=10;
    public static void stampPixels(int w, int h, int[][] pixels, int[][] stamp){
        //
        LocalMaximaStampper.stampLocalMaxima(pixels, stamp, w, h);//Will be returning to the original form later 2/26
/*
        int i,j,k,di,dj,n,dp,p0,len,key;
        int[] p=new int [8];
        boolean ln,lx;
        Point pt;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                stamp[i][j]=-1;
            }
        }
        boolean ep=true;//equal intensity as neighbors.
        int cat;
        for(i=1;i<h-1;i++){
            for(j=1;j<w-1;j++){
                cat=stamp[i][j];
                ln=true;
                lx=true;
                if(cat==localMaximum||cat==localMinimum) continue;
                if(cat!=-1) {//this position has been stampped as one of EI points, but not local a extremum
                    ln=false;
                    lx=false;
                }
                n=0;
                p0=pixels[i][j];
                ep=false;
                for(dj=-1;dj<=1;dj++){//checking the three contacting points on the upper line
                    dp=p0-pixels[i-1][j+dj];
                    if(dp>0) ln=false;
                    else if(dp<0) lx=false;
                    else {
//                        dp=p0-nonEIPoint(pixels,w,h,j,i,dj,-1,p0);
//                        if(dp>0) ln=false;
//                        else if(dp<0) lx=false;
                        if(cat==-1) {
                            stampEIPoints(pixels,stamp,w,h,j,i);
                            cat=stamp[i][j];
                        }
                    }
                    p[n]=dp;
                    n++;
                }
                if(cat==localMaximum||cat==localMinimum) continue;
                if(cat!=-1) {//this position has been stampped as one of EI points, but not local a extremum
                    ln=false;
                    lx=false;
                }
                dp=p0-pixels[i][j+1];
                if(dp>0) ln=false;
                else if(dp<0) lx=false;
                else {
//                    dp=p0-nonEIPoint(pixels,w,h,j,i,1,0,p0);
//                    if(dp>0) ln=false;
//                    else if(dp<0) lx=false;
                    if(cat==-1) {
                        stampEIPoints(pixels,stamp,w,h,j,i);
                        cat=stamp[i][j];
                    }
                }
                if(cat==localMaximum||cat==localMinimum) continue;
                if(cat!=-1) {//this position has been stampped as one of EI points, but not local a extremum
                    ln=false;
                    lx=false;
                }
                p[n]=dp;
                n++;
                for(dj=1;dj>=-1;dj--){
                    dp=p0-pixels[i+1][j+dj];
                    if(dp>0) ln=false;
                    else if(dp<0) lx=false;
                    else {
//                        dp=p0-nonEIPoint(pixels,w,h,j,i,dj,1,p0);
//                        if(dp>0) ln=false;
//                        else if(dp<0) lx=false;
                        if(cat==-1) {
                            stampEIPoints(pixels,stamp,w,h,j,i);
                            cat=stamp[i][j];
                        }
                    }
                    p[n]=dp;
                    n++;
                }
                if(cat==localMaximum||cat==localMinimum) continue;
                if(cat!=-1) {//this position has been stampped as one of EI points, but not local a extremum
                    ln=false;
                    lx=false;
                }
                dp=p0-pixels[i][j-1];
                if(dp>0) ln=false;
                else if(dp<0) lx=false;
                else {
//                    dp=p0-nonEIPoint(pixels,w,h,j,i,-1,0,p0);
//                    if(dp>0) ln=false;
//                    else if(dp<0) lx=false;
                    if(cat==-1) {
                        stampEIPoints(pixels,stamp,w,h,j,i);
                        cat=stamp[i][j];
                    }
                }
                if(cat==localMaximum||cat==localMinimum) continue;
                if(cat!=-1) {//this position has been stampped as one of EI points, but not local a extremum
                    ln=false;
                    lx=false;
                }
                p[n]=dp;

                if(ln) {
                    stamp[i][j]=localMinimum;
                    continue;
                }
                else if(lx){
                    stamp[i][j]=localMaximum;
                    continue;
                }

                cat=regular;
                cat=landscapeCategory(p[0],p[4],p[2],p[6],0);
                if(cat!=regular){
                    stamp[i][j]=cat;
                    continue;
                }
                cat=landscapeCategory(p[1],p[5],p[3],p[7],0);
                if(cat!=regular){
                    stamp[i][j]=cat;
                    continue;
                }

                int s1=p[0]+p[1]+p[2],s2=p[4]+p[5]+p[6],s3=p[2]+p[3]+p[4],s4=p[6]+p[7]+p[0];

                cat=landscapeCategory(s1,s2,s3,s4,1);
                if(cat!=regular){
                    stamp[i][j]=cat;
                    continue;
                }

                s1=p[7]+p[0]+p[1];
                s2=p[3]+p[4]+p[5];
                s3=p[1]+p[2]+p[3];
                s4=p[5]+p[6]+p[7];
                cat=landscapeCategory(s1,s2,s3,s4,1);
                stamp[i][j]=cat;
            }
        }*/
    }
    public static int nonEIPoint(int[][] pixels, int w, int h, int x, int y, int dx, int dy, int p0){//this method searches the images from
        //the point (x,y) along the direction dx,dy and return the first pixel value that is not equal to that of (x,y).
        int p=p0;
        while(p==p0){
            x+=dx;
            y+=dy;
            if(x>=0&&x<w&&y>=0&&y<h) p=pixels[y][x];
            else p=p0-1;
        }
        return p;
    }
    public static int circularAddition(int size, int position, int delta){
        int sum=(position+delta)%size;
        if(sum<0) sum+=(-sum/size+1)*size;
        return sum;
    }
    public static double getAverage(ArrayList<Integer> ir){
        int size=ir.size();
        double ave=0.;
        for(int i=0;i<size;i++){
            ave+=ir.get(i);
        }
        return ave/size;
    }

    public static void stampEIPoints(int[][] pixels, int[][] stamp, int w, int h, int x, int y){
        int p0=pixels[y][x],cat,i,index;
        Point pt;
        ArrayList<Point> contour=ContourFollower.getContour_Out(pixels, w, h, new Point(x,y), p0, p0);
        GeneralGraphicalObject ggo=new GeneralGraphicalObject();
        ggo.setContour(pixels, contour, new intRange(p0,p0));
        ArrayList<Point> innerPoints=ggo.getInnerPoints();
        cat=landscapeCategoryEI(pixels, w,h, ggo, p0);
        int len=innerPoints.size();
        for(i=0;i<len;i++){
            pt=innerPoints.get(i);
            stamp[pt.y][pt.x]=cat;
        }
    }
    public static int landscapeCategory(int p1, int p2, int p3, int p4, int shift){
        //shift==0 when p1 ... p4 are the pixel values (subtracted p0) of the points indicated below and shift==1 when p1 ... p4 are the pixel values of the average of the points with  their neighbors
        /*
         *   x  p1  x    p1`  x  p3
         *   p3 p0 p4     x  p0  x
         *   x  p2  x    p4   x  p2
         */
        int cat=regular;
        if(p1>0&&p2>0){
            if(p3<0&&p4<0){
                return saddle1+shift;
            }else{
                return ridge1+shift;
            }
        }
        else if(p1<0&&p2<0){
            if(p3>0&&p4>0){
                return saddle1+shift;
            }
            else{
                return groove1+shift;
            }
       }else if(p1*p2<0){
            if(p3>0&&p4>0){
                return ridge1+shift;
            }else if(p3<0&&p4<0){
                return groove1+shift;
            }
       }
       return cat;
    }

    public static int landscapeCategoryEI(int[][] pixels, int w, int h, GeneralGraphicalObject ggo, int pixel0){
        int cat=regular;
        boolean ss=true;
        boolean ln=true,lx=true;
        int x,y,pixel;
        ArrayList <Point> contour=ggo.getContour();
        int size=contour.size(),i,size0,j;
        Point p;
        for(i=0;i<size;i++){
            p=contour.get(i);
            pixel=pixels[p.y][p.x];
            if(pixel>pixel0) lx=false;
            else ln=false;
        }
        PointArray2 innerContours =ggo.getInnerContours();
        size0=innerContours.m_pointArray2.size();
        for(i=0;i<size0;i++){
            contour=innerContours.m_pointArray2.get(i).m_pointArray;
            size=contour.size();
            for(j=0;j<size;j++){
                p=contour.get(j);
                pixel=pixels[p.y][p.x];
                if(pixel>pixel0) lx=false;
                else ln=false;
            }
        }
        if(lx) return localMaximum;
        if(ln) return localMinimum;
        return regular;
    }

    public static boolean stationarySaddle(int[][] pixels, int w, int h, ArrayList<Point> contour,int pixel){
        boolean ss=true;
        Point c=getCentroid(pixels,contour,new intRange(pixel,pixel)),p;
        ArrayList<Integer>  points1=new ArrayList<Integer>(),points2=new ArrayList<Integer>(),points3=new ArrayList<Integer>(),points4=new ArrayList<Integer>();
        int size=contour.size(),i;
        for(i=0;i<size;i++){
            p=contour.get(i);
            if(p.x<c.x) points1.add(pixels[p.y][p.x]);
            if(p.x>c.x) points2.add(pixels[p.y][p.x]);
            if(p.y<c.y) points3.add(pixels[p.y][p.x]);
            if(p.y>c.y) points4.add(pixels[p.y][p.x]);
        }
        double a1=getAverage(points1),a2=getAverage(points2),a3=getAverage(points3),a4=getAverage(points4);
        double d1=a1-pixel,d2=a2-pixel,d3=a3-pixel,d4=a4-pixel;
        if(d1*d2>0&&d3*d4>0&&d1*d3<0) return true;
        points1.clear();
        points2.clear();
        points3.clear();
        points4.clear();
        for(i=0;i<size;i++){
            p=contour.get(i);
            if(p.x>c.x&&p.y>c.y) points1.add(pixels[p.y][p.x]);
            if(p.x<c.x&&p.y<c.y) points2.add(pixels[p.y][p.x]);
            if(p.x>c.x&&p.y<c.y) points3.add(pixels[p.y][p.x]);
            if(p.x<c.x&&p.y>c.y) points4.add(pixels[p.y][p.x]);
        }
        a1=getAverage(points1);
        a2=getAverage(points2);
        a3=getAverage(points3);
        a4=getAverage(points4);
        d1=a1-pixel;
        d2=a2-pixel;
        d3=a3-pixel;
        d4=a4-pixel;
        if(d1*d2>0&&d3*d4>0&&d1*d3<0) return true;
        return false;
    }
    public static Point getCentroid(int[][] pixels, ArrayList<Point> contour, intRange intensityRange){
        Point p;
        ArrayList <Point> innerPoints=getInnerPoints(pixels, contour, intensityRange);
        int size=innerPoints.size();
        if(size==0) {
            IJ.error("the number of inner points is zero");
        }
        int i, x=0, y=0;
        for(i=0;i<size;i++){
            p=innerPoints.get(i);
            x+=p.x;
            y+=p.y;
        }
        return new Point(x/size,y/size);
    }
    public static ArrayList<Point> getInnerPoints(int[][] pixels, ArrayList<Point> contour, intRange intensityRange){
        GeneralGraphicalObject ggo=new GeneralGraphicalObject();
        ggo.setContour(pixels, contour, intensityRange);
        return ggo.getInnerPoints();
    }
    public static ArrayList<GeneralGraphicalObject> getGrooveNetworks(int[][] stamp, int w, int h){
        ArrayList<GeneralGraphicalObject> gns=new ArrayList<GeneralGraphicalObject>();
        ArrayList<Point> localMinima=new ArrayList<Point>();
        int i,j,size,len;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                if(stamp[i][j]==localMinimum) localMinima.add(new Point(j,i));
            }
        }
        size=localMinima.size();
        Point p;
        GeneralGraphicalObject ggo;
        ArrayList <Point> contour;
        boolean wn;//within a network
        for(i=0;i<size;i++){
            wn=false;
            p=localMinima.get(i);
            len=gns.size();
            for(j=0;j<len;j++){
                ggo=gns.get(j);
                if(ggo.contains(p)){
                    wn=true;
                    break;
                }
            }
            if(wn) continue;
            contour=ContourFollower.getContour_Out(stamp, w, h, p, localMinimum, groove2);
            ggo=new GeneralGraphicalObject();
            ggo.setContour(stamp, contour, new intRange(localMinimum, groove2));
            gns.add(ggo);
        }
        return gns;
    }
    public static void markGrooveNetworks(int[]pixels, int[][]stamp, int w, int h){
        ArrayList<GeneralGraphicalObject> gns=getGrooveNetworks(stamp, w, h);
        ArrayList <Point> innerPoints;
        GeneralGraphicalObject ggo;
        int i,j,size=gns.size(),len,x,y,pixel,cat,r,g,b,o;
        Point p;
        for(i=0;i<size;i++){
            ggo=gns.get(i);
            innerPoints=ggo.getInnerPoints();
            len=innerPoints.size();
            for(j=0;j<len;j++){
                p=innerPoints.get(j);
                x=p.x;
                y=p.y;
                pixel=pixels[y*w+x];
                cat=stamp[y][x];
                if(cat==localMaximum){
                    r=0;
                    g=0;
                    b=pixel;
                }else if(cat==localMinimum){
                    r=pixel;
                    g=0;
                    b=0;
                }else{
                    r=pixel;
                    g=pixel;
                    b=((i+1))*1%250;
                }
                pixels[y*w+x]=(250<<16)|(250<<8)|b;
            }
        }
    }
    public static int stampPixelsGray8(ImagePlus impl, int[][] stamp, int type, ArrayList <Point> points){
        if(impl.getType()!=ImagePlus.GRAY8){
            IJ.error("stampPixelsGray8 is implemented only for Gray8");
            return -1;
        }
        ArrayList<Point> localMaxima=new ArrayList();
        ArrayList<IntArray> vnPixels=new ArrayList();
        int i,j,o,pixel;
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        int scratch[][]=new int[h][w];
        for(i=0;i<256;i++){
            vnPixels.add(new IntArray());
        }
        byte[] bp=(byte[])impl.getProcessor().getPixels();
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixel=0xff&bp[o+j];
                pixels[i][j]=pixel;
                stamp[i][j]=0;
                scratch[i][j]=0;
            }
        }


        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixel=0xff&bp[o+j];
                vnPixels.get(pixel).m_intArray.add(o+j);
                pixels[i][j]=pixel;
                stamp[i][j]=-1;
                scratch[i][j]=0;
            }
        }
        int len,x,y,dx,dy,x0,y0,xt,yt,indexrt,indexrt0;
        ArrayList<Integer> vnPixelst;
        int indexr=1;//region index. a region is defined by a local maximum and the points within watershed boundary.
        Point p,p0;
        int position0;
        boolean lm=true;
        int index;
        boolean borderPoint;
        for(i=0;i<256;i++){
            index=255-i;
            vnPixelst=vnPixels.get(index).m_intArray;
            len=vnPixelst.size();
            for(j=0;j<len;j++){
                position0=vnPixelst.get(j);
                y0=position0/w;
                 x0=position0-y0*w;
                if(x0==34&&y0==23){
                    x0=x0;
                }
                 if(scratch[2][5]==-1){
                     x0=x0;
                 }
                 if(index==138&&j==2){
                     x0=x0;
                 }
                if(scratch[y0][x0]==0){
                    stamp[y0][x0]=localMaximum;
                    scratch[y0][x0]=indexr;
                    localMaxima.add(new Point(x0,y0));
                    for(dy=-1;dy<=1;dy++)
                    {
                        y=y0+dy;
                        if(y<0||y>=h) continue;
                        for(dx=-1;dx<=1;dx++)
                        {
                            x=x0+dx;
                            if(x<0||x>=w) continue;
                            indexrt=-scratch[y][x];
                            if(indexrt==0){
                                scratch[y][x]=-indexr;//claiming this position as a border point of the region.
                            }else{//point (x,y) has been claimed as a border point of another region.
                                if(indexrt!=indexr&&indexrt>0){//not a border point of the same region
                                    stamp[y][x]=watershed;
                                    p=localMaxima.get(indexrt-1);//region index starts from 1
                                    if(pixels[x0][y0]>pixels[p.y][p.x]) scratch[y][x]=-indexr;
                                }
                            }
                        }
                    }

                    indexr++;
                }else{//point (x,y) has been claimed as a border point of another region.
//                    if(stamp[y0][x0]==watershed){
                        if(stamp[y0][x0]==watershed)
                            borderPoint=true;
                        else
                            borderPoint=false;

                        indexrt0=-scratch[y0][x0];
                        p0=localMaxima.get(indexrt0-1);
                        scratch[y0][x0]=indexrt0;
                        lm=true;
                        for(dy=-1;dy<=1;dy++)
                        {
                            y=y0+dy;
                            if(y<0||y>=h) continue;
                            for(dx=-1;dx<=1;dx++)
                            {
                                x=x0+dx;
                                if(x<0||x>=w) continue;
                                indexrt=-scratch[y][x];
                                if(indexrt==0){
                                    if(!borderPoint) scratch[y][x]=-indexrt0;//claiming this position as a border point of the region.
                                    lm=false;
                                }else{//point (x,y) has been claimed as an inner point or as a border point of another region.
                                    if(indexrt>0){
                                        lm=false;//
                                        if(indexrt0!=indexrt&&!borderPoint){
                                            if(!borderPoint) stamp[y][x]=watershed;
                                            p=localMaxima.get(indexrt-1);
                                            if(pixels[p0.y][p0.x]>pixels[p.y][p.x])scratch[y][x]=-indexrt0;
                                        }
                                    }//do nothing if indexrt <0, b/c this point is already be claimed as an inner point of other region.
                                }
                            }
                        }
                        if(lm) stamp[y0][x0]=localMinimum;
//                    }
                        /*else{
                        indexrt0=-scratch[y0][x0];
                        scratch[y0][x0]=indexrt0;
                        p0=localMaxima.get(indexrt0-1);
                        for(dy=-1;dy<=1;dy++)
                        {
                            y=y0+dy;
                            if(y<0||y>=h) continue;
                            for(dx=-1;dx<=1;dx++)
                            {
                                x=x0+dx;
                                if(x<0||x>=w) continue;
                                indexrt=-scratch[y][x];
                                if(indexrt==0){
                                    scratch[y][x]=-indexrt0;//claiming this position as a border point of the region.
                                }else{//point (x,y) has been claimed as an inner point or as a border point of another region.
                                    if(indexrt>0){
                                        if(indexrt!=indexrt0){
                                            stamp[y][x]=watershed;
                                            p=localMaxima.get(indexrt-1);
                                            if(pixels[p0.y][p0.x]>pixels[p.y][p.x])scratch[y][x]=-indexrt0;
                                        }
                                    }//do nothing if indexrt <0, b/c this point is already be claimed as an inner point of other region.
                                }
                            }
                        }
                    }*/
                }
            }
        }
        return 1;
    }
}
