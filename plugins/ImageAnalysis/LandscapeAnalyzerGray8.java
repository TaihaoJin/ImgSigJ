/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ij.ImagePlus;
import java.util.ArrayList;
import ij.IJ;
import utilities.ArrayofArrays.IntArray;
import utilities.ArrayofArrays.IntArray2;
import utilities.ArrayofArrays.PointArray;
import utilities.ArrayofArrays.PointArray2;
import java.awt.Point;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class LandscapeAnalyzerGray8 {
    public static final int regular=1, localMinimum=2, groove1=3, groove2=4, saddle1=5, saddle2=6, ridge1=7,ridge2=8, localMaximum=9,watershed=10;
    int [][]pixels,stampLandscape,stampRegions;
//    ArrayList<IntArray> m_vcPixels;//none EIPoints;
//    ArrayList<IntArray2> m_vcPixels2;//EIPoints;
    PointArray[] m_vcPoints;//none EIPoints;
    PointArray2[] m_vcPointsEI;//none EIPoints;
    PointArray2[] m_vcBorderEI;//none EIPoints;
    ImagePlus impl;
    int w,h,pMax;
    public LandscapeAnalyzerGray8(ImagePlus impl){
        pMax=255;
        update(impl);
    }
    public void update(ImagePlus impl){
        if(impl.getType()!=ImagePlus.GRAY8){
            IJ.error("LandscapeAnalyzerGray8 only support a gray8 image!");
        }
        w=impl.getWidth();
        h=impl.getHeight();
        pixels=new int[h][w];
        stampLandscape=new int[h][w];
        stampRegions=new int[h][w];
        this.impl=impl;
        m_vcPoints=new PointArray[pMax+1];
        m_vcPointsEI=new PointArray2[pMax+1];
        m_vcBorderEI=new PointArray2[pMax+1];
        for(int i=0;i<=pMax;i++){
            m_vcPoints[i]=new PointArray();
            m_vcPointsEI[i]=new PointArray2();
            m_vcBorderEI[i]=new PointArray2();
        }
        updatePixels();
        stampPixels();
    }
    void updatePixels(){
        int i,j,o,pixel,pixel0,k;
        int[][]stampEI=stampLandscape,stampb=stampRegions;
        byte bp[]=(byte[])impl.getProcessor().getPixels();
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixel=0xff&bp[o+j];
                pixels[i][j]=pixel;
                stampLandscape[i][j]=0;
                stampRegions[i][j]=0;
            }
        }
        
        int x,y,regionIndex=0,len;
        Point p;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                if(stampEI[i][j]>0) continue;//part of EI region and has been stampped
                pixel0=pixels[i][j];

                if(j<w-1){
                    pixel=pixels[i][j+1];
                    if(pixel==pixel0){//masking all connected EIPoints
                        regionIndex++;
                        ArrayList<Point> regions=new ArrayList();
                        ArrayList<Point> borderPoints=new ArrayList();
                        RegionFinderEI.findRegion(pixels, stampEI, stampb, w, h, regions, borderPoints, new Point(j,i), pixel0, regionIndex,0,h-1,0,w-1);
                        m_vcPointsEI[pixel0].m_pointArray2.add(new PointArray(regions));
                        m_vcBorderEI[pixel0].m_pointArray2.add(new PointArray(borderPoints));
                    }
                }

                if(i>=h-1) continue;
                y=i+1;
                for(x=j-1;x<=j+1;x++){
                    if(x<0||x>=j+1) continue;
                    if(stampEI[y][x]>0) continue;
                    pixel=pixels[y][x];
                    if(pixel==pixel0){
                        regionIndex++;
                        regionIndex++;
                        ArrayList<Point> regions=new ArrayList();
                        ArrayList<Point> borderPoints=new ArrayList();
                        RegionFinderEI.findRegion(pixels, stampEI, stampb, w, h, regions, borderPoints, new Point(j,i), pixel0, regionIndex,0,h-1,0,w-1);
                        m_vcPointsEI[pixel0].m_pointArray2.add(new PointArray(regions));
                        m_vcBorderEI[pixel0].m_pointArray2.add(new PointArray(borderPoints));
                    }
                }
                if(stampEI[i][j]==0) m_vcPoints[pixel0].m_pointArray.add(new Point(j,i));
            }
        }
    }
    void stampPixels(){
        int[][]stamp=stampLandscape;
        int[][]scratch=stampRegions;
        ArrayList<Point> localMaxima=new ArrayList();
        ArrayList<Point> localMinima=new ArrayList();
        ArrayList<IntArray> vnPixels=new ArrayList();
        int i,j,o,pixel;
        int w=impl.getWidth(),h=impl.getHeight();

        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                stamp[i][j]=-1;
                scratch[i][j]=0;
            }
        }
        
        int len,x,y,dx,dy,x0,y0,xt,yt,indexrt,indexrt0;
        ArrayList<Point> points;
        int indexr=1;//region index. a region is defined by a local maximum and the points within watershed boundary.
        Point p,p0;
        boolean lm=true;
        int index;
        for(i=0;i<=pMax;i++){
            index=pMax-i;
            points=m_vcPoints[index].m_pointArray;
//            if(points.size()==0) continue;
            //finished stamping with none EIPpoints.
            indexr=assignPoints(points,index,indexr,localMaxima);
            indexr=stampEIPoints(index,indexr,localMaxima);
        }
    }

    int stampEIPoints(int index,int indexr, ArrayList<Point> localMaxima){
        ArrayList <Point> EIPoints0,borderPoints,EIPoints=new ArrayList(),pointsToAssign=new ArrayList();
        ArrayList <Point> localMinima=new ArrayList();
        int len0=m_vcPointsEI[index].m_pointArray2.size();
        int i,j,lenr,lenb,x,y,x0,y0,num;
        Point p,lastPoint;
//        int indexr0=indexr;
        boolean ln=false,lx=true;
        for(i=0;i<len0;i++){
            EIPoints0=m_vcPointsEI[index].m_pointArray2.get(i).m_pointArray;
            borderPoints=m_vcBorderEI[index].m_pointArray2.get(i).m_pointArray;
            lenb=borderPoints.size();
            num=0;
            for(j=0;j<lenb;j++){
                p=borderPoints.get(j);
                if(pixels[p.y][p.x]>index){
                    num++;
                }
            }
            if(num==0){//local maximum
                lx=true;
                p=CommonMethods.closestPointToCenter(EIPoints0, w, h);
//                stampLandscape[p.y][p.x]=localMaximum;
//                stampRegions[p.y][p.x]=indexr;
//                indexr0=indexr;
                indexr=assignPoint(p,index,indexr,localMaxima);
//                indexr++;
            }
            if(num==lenb) ln=true;
            lenr=EIPoints0.size();
            while(lenr>0){
                EIPoints.clear();
                pointsToAssign.clear();
                for(j=0;j<lenr;j++){
                    p=EIPoints0.get(j);
                    if(stampRegions[p.y][p.x]<0){
                        pointsToAssign.add(p);
                    }else if(stampRegions[p.y][p.x]==0){
                        EIPoints.add(p);
                    }
                }
                num=pointsToAssign.size();
                if(num==0) break;
                indexr=assignPoints(pointsToAssign,index,indexr,localMaxima);
                EIPoints0=new ArrayList <Point>(EIPoints);
                lenr=EIPoints0.size();
            }
        }
        return indexr;
    }

    int assignPoints(ArrayList <Point> points,int index, int indexr, ArrayList<Point> localMaxima){
        //indexr region index. a region is defined by a local maximum and the points within watershed boundary.
        //index: the index of sorted pixel values, equal as the pixel value.
        //this methods assuming all points in "points" are equal pixel value (index). The EI points may be connected, but all
        //points must be connected with at list one point that is not included in "points".
        int len=points.size();
        for(int i=0;i<len;i++){
            indexr=assignPoint(points.get(i),index,indexr,localMaxima);
        }
        return indexr;
    }

    public int[][] getStamp(){
        return stampLandscape;
    }

    int assignPoint(Point p0,int index, int indexr, ArrayList<Point> localMaxima){//indexr region index. a region is defined by a local maximum and the points within watershed boundary.
        //index: the index of sorted pixel values, equal as the pixel value.
        int[][]stamp=stampLandscape;
        int[][]scratch=stampRegions;
        int x,y,dx,dy,x0,y0,indexrt,indexrt0;
        int pixeln=pMax;
        Point p;
        boolean borderPoint;

        y0=p0.y;
        x0=p0.x;
        if(scratch[y0][x0]==0){
            stamp[y0][x0]=localMaximum;
            scratch[y0][x0]=indexr;
            localMaxima.add(p0);
            borderPoint=false;
            indexrt0=indexr;
            indexr++;
        }else{
            indexrt0=-scratch[y0][x0];
            if(stamp[y0][x0]==watershed)
                borderPoint=true;
            else
                borderPoint=false;
        }

        for(dy=-1;dy<=1;dy++)
        {
            y=y0+dy;
            if(y<0||y>=h) continue;
            for(dx=-1;dx<=1;dx++)
            {
                x=x0+dx;
                if(x<0||x>=w) continue;
                if(pixels[y][x]<pixeln){
                    if(!(dx==0&&dy==0)){
                        pixeln=pixels[y][x];
                    }
                }
                indexrt=-scratch[y][x];

                if(indexrt==0){
                    if(!borderPoint) scratch[y][x]=-indexrt0;//claiming this position as a border point of the region.
                }else{//point (x,y) has been claimed as an inner point or as a border point of another region.
                    if(indexrt>0){
                        if(indexrt0!=indexrt&&!borderPoint){
                            if(!borderPoint) stamp[y][x]=watershed;
                            p=localMaxima.get(indexrt-1);
                            if(pixels[y0][x0]>pixels[p.y][p.x])scratch[y][x]=-indexrt0;
                        }
                    }//do nothing if indexrt <0, b/c this point is already be claimed as an inner point of other region.
               }
            }
        }
        if(pixeln>index) {
            stamp[y0][x0]=localMinimum;
        }
        return indexr;
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
                if(scratch[y0][x0]==0){
                    lm=false;
                    stamp[y0][x0]=localMaximum;
                    scratch[y0][x0]=indexr;
                    localMaxima.add(new Point(x0,y0));
                    borderPoint=false;
                    indexrt0=indexr;
                    indexr++;
                }else{
                    indexrt0=-scratch[y0][x0];
                    if(stamp[y0][x0]==watershed)
                        borderPoint=true;
                    else
                        borderPoint=false;
                }

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
                                    if(pixels[y0][x0]>pixels[p.y][p.x])scratch[y][x]=-indexrt0;
                                }
                            }//do nothing if indexrt <0, b/c this point is already be claimed as an inner point of other region.
                        }
                    }
                }
                if(lm) stamp[y0][x0]=localMinimum;
           }
        }
        return 1;
    }
    public static int stampPixels(ImagePlus impl, int[][] stamp){
        int pixels[][]=CommonMethods.getPixelValues(impl);
        stampPixels(pixels,stamp);
        return 1;
    }
    public static int stampPixels(int[][]pixels, int[][] stamp){
        ArrayList<Point> localMaxima=new ArrayList();
        ArrayList<IntArray> vnPixels=new ArrayList();
        int i,j,o,pixel;
        int w=pixels[0].length,h=pixels.length;
        intRange pixelRange=CommonStatisticsMethods.getRange(pixels);
        int nMinPixel=pixelRange.getMin(), nMaxPixel=pixelRange.getMax();

        int scratch[][]=new int[h][w];
        for(i=nMinPixel;i<=nMaxPixel;i++){
            vnPixels.add(new IntArray());
        }

        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                stamp[i][j]=0;
                scratch[i][j]=0;
            }
        }

        int index;
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixel=pixels[i][j];
                index=pixel-nMinPixel;
                vnPixels.get(index).m_intArray.add(o+j);
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
        boolean borderPoint;
        int nDim=nMaxPixel-nMinPixel+1;
        for(pixel=nMinPixel;pixel<=nMaxPixel;pixel++){
            index=nMaxPixel-pixel;
            vnPixelst=vnPixels.get(index).m_intArray;
            len=vnPixelst.size();
            for(j=0;j<len;j++){
                position0=vnPixelst.get(j);
                y0=position0/w;
                x0=position0-y0*w;
                if(scratch[y0][x0]==0){
                    lm=false;
                    stamp[y0][x0]=localMaximum;
                    scratch[y0][x0]=indexr;
                    localMaxima.add(new Point(x0,y0));
                    borderPoint=false;
                    indexrt0=indexr;
                    indexr++;
                }else{
                    indexrt0=-scratch[y0][x0];
                    if(stamp[y0][x0]==watershed)
                        borderPoint=true;
                    else
                        borderPoint=false;
                }

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
                                    if(pixels[y0][x0]>pixels[p.y][p.x])scratch[y][x]=-indexrt0;
                                }
                            }//do nothing if indexrt <0, b/c this point is already be claimed as an inner point of other region.
                        }
                    }
                }
                if(lm) stamp[y0][x0]=localMinimum;
           }
        }
        return 1;
    }
}
