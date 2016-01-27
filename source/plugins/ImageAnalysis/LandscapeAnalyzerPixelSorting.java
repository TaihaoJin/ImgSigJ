/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ij.ImagePlus;
import java.util.ArrayList;
import ij.IJ;
import utilities.ArrayofArrays.PointArray;
import utilities.ArrayofArrays.PointArray2;
import java.awt.Point;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import java.util.TreeSet;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class LandscapeAnalyzerPixelSorting {//this class stamps only local maxima, local minima and watershed points
        //special landscape points contain two parts, the last 2 bits indicating the types of the points and the higher
        //bits are to indicate the region indexes, starting from 1.
    public static final int regular=0,localMinimum=1,watershed=2,watershedLM=3,localMaximum=5,isolatedMinimum=6;//watershedLM, a local minimum point at watershed position
    public static final int[] moves={-1,-1,-1,0,-1,1,0,-1,0,1,1,-1,1,0,1,1};
    int [][]pixels,stampLandscape,stampRegions;
//    ArrayList<IntArray> m_vcPixels;//none EIPoints;
//    ArrayList<IntArray2> m_vcPixels2;//EIPoints;
    PointArray[] m_vcPoints;//none EIPoints;
    PointArray2[] m_vcPointsEI;//none EIPoints;
    PointArray2[] m_vcBorderEI;//none EIPoints;
    int[] m_pnPixelRange;
    int w,h;
    intRange xStampingRange,yStampingRange;

    public LandscapeAnalyzerPixelSorting(ImagePlus impl){
        int[] pnRange=new int[2];
        CommonMethods.getPixelValueRange_Stack(impl, pnRange);
        update(impl.getWidth(),impl.getHeight(),pnRange);
    }
    public LandscapeAnalyzerPixelSorting(int w, int h, int[] pixelRange){
        update(w,h,pixelRange);
    }
    public void update(int w, int h, int[] pixelRange){
        update(w,h,pixelRange,new intRange(0,h-1),new intRange(0,w-1));
    }
    public void update(int w, int h, int[] pixelRange,intRange yRange, intRange xRange){
        this.w=w;
        this.h=h;
        m_pnPixelRange=new int[2];
        m_pnPixelRange[0]=pixelRange[0];
        m_pnPixelRange[1]=pixelRange[1];

        pixels=CommonStatisticsMethods.getIntArray2(pixels, w, h);
//        stampLandscape=new int[h][w];//this array will be passed in by the caller
        stampRegions=CommonStatisticsMethods.getIntArray2(stampRegions, w, h);
        int len=m_pnPixelRange[1]-m_pnPixelRange[0]+1;
        if(len<0){
            len=len;
        }
        m_vcPoints=new PointArray[len];
        m_vcPointsEI=new PointArray2[len];
        m_vcBorderEI=new PointArray2[len];
        for(int i=0;i<len;i++){
            m_vcPoints[i]=new PointArray();
            m_vcPointsEI[i]=new PointArray2();
            m_vcBorderEI[i]=new PointArray2();
        }
        xStampingRange=new intRange(xRange);
        yStampingRange=new intRange(yRange);
    }
    public void updateAndStampPixels(int[][] pixels, int[][] stamp){
        updateAndStampPixels(pixels,stamp,0,pixels.length-1,0,pixels[0].length-1);
    }
    /*
    public void updateAndStampPixels0(int[][] pixels, int[][] stamp){//local minima, local maxima and watershed points will be marked
        //by the values as specified by the static variables. all the rest matrix elements of stamp is asigned by the negative value of the
        //region indexes.
        //special landscape points contain two parts, the last 2 bits indicating the types of the points and the higher bits are to indicate the region indexes, starting from 1.
        int i,j,pixel,pixel0;
        stampLandscape=stamp;

        int len=m_pnPixelRange[1]-m_pnPixelRange[0]+1;
        for(i=0;i<len;i++){
            m_vcPoints[i].m_pointArray.clear();
            m_vcPointsEI[i].m_pointArray2.clear();
            m_vcBorderEI[i].m_pointArray2.clear();
        }

        int[][]stampEI=stampLandscape,stampb=stampRegions;//stampLandscape and stampRegions will be reinitialized.
        CommonStatisticsMethods.copyArray(pixels, this.pixels);
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                stampLandscape[i][j]=0;
                stampRegions[i][j]=0;
            }
        }

        int x,y,regionIndex=0;
        Point p;
        int index;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                if(i==78&&j==147){
                    i=i;
                }
                if(stampEI[i][j]>0) continue;//part of EI region and has been stampped
                pixel0=pixels[i][j];
                index=pixel0-m_pnPixelRange[0];

                if(j<w-1){
                    pixel=pixels[i][j+1];
                    if(pixel==pixel0){//masking all connected EIPoints
                        regionIndex++;
                        ArrayList<Point> regions=new ArrayList();
                        ArrayList<Point> borderPoints=new ArrayList();
                        RegionFinderEI.findRegion(pixels, stampEI, stampb, w, h, regions, borderPoints, new Point(j,i), pixel0, regionIndex);
                        m_vcPointsEI[index].m_pointArray2.add(new PointArray(regions));
                        m_vcBorderEI[index].m_pointArray2.add(new PointArray(borderPoints));
                    }
                }

                if(i>=h-1) {
                    if(stampEI[i][j]==0) m_vcPoints[index].m_pointArray.add(new Point(j,i));
                    continue;
                }
                y=i+1;
                for(x=j-1;x<=j+1;x++){
//                    if(x<0||x>=j+1) continue;//modified to the below. it must be a bug made before
                    if(x<0||x>=w) continue;
                    if(stampEI[y][x]>0) continue;
                    pixel=pixels[y][x];
                    if(pixel==pixel0){
//                        regionIndex++;//2/26/2011
                        regionIndex++;
                        ArrayList<Point> regions=new ArrayList();
                        ArrayList<Point> borderPoints=new ArrayList();
                        RegionFinderEI.findRegion(pixels, stampEI, stampb, w, h, regions, borderPoints, new Point(j,i), pixel0, regionIndex);
                        m_vcPointsEI[index].m_pointArray2.add(new PointArray(regions));
                        m_vcBorderEI[index].m_pointArray2.add(new PointArray(borderPoints));
                    }
                }
                if(index<0){
                    index=index;
                }
                if(stampEI[i][j]==0) m_vcPoints[index].m_pointArray.add(new Point(j,i));
            }
        }
        stampPixels();
        recordRegions();
    }*/
     public static boolean isLocalMinimum(int stampValue){
         int type=getLandscapeType(stampValue);
         return (type==localMinimum||type==watershedLM);
     }
     public void updateAndStampPixels(int[][] pixels, int[][] stamp,int iI,int iF, int jI, int jF){//local minima, local maxima and watershed points will be marked
        //by the values as specified by the static variables. all the rest matrix elements of stamp is asigned by the negative value of the
        //region indexes.
        //special landscape points contain two parts, the last 2 bits indicating the types of the points and the higher bits are to indicate the region indexes, starting from 1.
        int i,j,pixel,pixel0;
        stampLandscape=stamp;

        intRange ir=CommonStatisticsMethods.getRange(pixels,iI,iF,jI,jF);
        int[] pnRange={ir.getMin(),ir.getMax()};
        update(pixels[0].length,pixels.length,pnRange,new intRange(iI,iF),new intRange(jI,jF));

        int len=m_pnPixelRange[1]-m_pnPixelRange[0]+1;
        for(i=0;i<len;i++){
            m_vcPoints[i].m_pointArray.clear();
            m_vcPointsEI[i].m_pointArray2.clear();
            m_vcBorderEI[i].m_pointArray2.clear();
        }

        int[][]stampEI=stampLandscape,stampb=stampRegions;//stampLandscape and stampRegions will be reinitialized.
        CommonStatisticsMethods.copyArray(pixels, this.pixels,iI,iF,jI,jF);
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                stampLandscape[i][j]=0;
                stampRegions[i][j]=0;
            }
        }

        int x,y,regionIndex=0;
        Point p;
        int index;
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                if(stampEI[i][j]>0) continue;//part of EI region and has been stampped
                pixel0=pixels[i][j];
                index=pixel0-m_pnPixelRange[0];

//                if(j<w-1){
                if(j<jF){
                    pixel=pixels[i][j+1];
                    if(pixel==pixel0){//masking all connected EIPoints
                        regionIndex++;
                        ArrayList<Point> regions=new ArrayList();
                        ArrayList<Point> borderPoints=new ArrayList();
                        RegionFinderEI.findRegion(pixels, stampEI, stampb, w, h, regions, borderPoints, new Point(j,i), pixel0, regionIndex,iI,iF,jI,jF);
                        m_vcPointsEI[index].m_pointArray2.add(new PointArray(regions));
                        m_vcBorderEI[index].m_pointArray2.add(new PointArray(borderPoints));
                    }
                }

//                if(i>=h-1) {
                if(i>=iF) {
                    if(stampEI[i][j]==0) m_vcPoints[index].m_pointArray.add(new Point(j,i));
                    continue;
                }
                y=i+1;
                for(x=j-1;x<=j+1;x++){
//                    if(x<0||x>=j+1) continue;//modified to the below. it must be a bug made before
//                    if(x<0||x>=w) continue;
                    if(!xStampingRange.contains(x)) continue;
                    if(stampEI[y][x]>0) continue;
                    pixel=pixels[y][x];
                    if(pixel==pixel0){
//                        regionIndex++;//2/26/2011
                        regionIndex++;
                        ArrayList<Point> regions=new ArrayList();
                        ArrayList<Point> borderPoints=new ArrayList();
                        RegionFinderEI.findRegion(pixels, stampEI, stampb, w, h, regions, borderPoints, new Point(j,i), pixel0, regionIndex,iI,iF,jI,jF);
                        m_vcPointsEI[index].m_pointArray2.add(new PointArray(regions));
                        m_vcBorderEI[index].m_pointArray2.add(new PointArray(borderPoints));
                    }
                }
                if(index<0){
                    index=index;
                }
                if(stampEI[i][j]==0) m_vcPoints[index].m_pointArray.add(new Point(j,i));
            }
        }
        stampPixels();
        recordRegions();
    }
    void recordRegions(){
        int i,j,region;
        for(i=yStampingRange.getMin();i<=yStampingRange.getMax();i++){
            for(j=xStampingRange.getMin();j<=xStampingRange.getMax();j++){
                if(getLandscapeType(stampLandscape[i][j])==regular){
                    region=-stampRegions[i][j];
                    if(region>0){
//                        stampLandscape[i][j]=region;
                        setLandscapeType(j,i,regular,region);
                    }else{
                        IJ.error("inconsistent region type at ("+j+","+i+"). region="+region);
                        CommonMethods.displayPixels(pixels, "pixels for stamping a.n. at ("+j+","+i+")", ImagePlus.GRAY16);
                    }
                }
            }
        }
    }
    public static int getLandscapeType(int type){
        return 7&type;
    }
    void setLandscapeType(int x, int y, int type, int regionIndex){
        stampLandscape[y][x]=(regionIndex<<3)|type;
    }
    public static void setLandscapeType(int[][] stamp, int x, int y, int type, int regionIndex){
        stamp[y][x]=(regionIndex<<3)|type;
    }
    void stampPixels(){        
        ArrayList<Point> localMaxima=new ArrayList();
        int i,j,pixel;

        for(i=yStampingRange.getMin();i<=yStampingRange.getMax();i++){
            for(j=xStampingRange.getMin();j<=xStampingRange.getMax();j++){
                setLandscapeType(j,i,regular,0);
                stampRegions[i][j]=0;
            }
        }

        ArrayList<Point> points;
        int indexr=1;//region index. a region is defined by a local maximum and the points within watershed boundary.
        int index,pMin=m_pnPixelRange[0],pMax=m_pnPixelRange[1];
        for(pixel=pMax;pixel>=pMin;pixel--){
            if(pixel==1111){
                pixel=pixel;
            }
            index=pixel-pMin;
            points=m_vcPoints[index].m_pointArray;
            //finished stamping with none EIPpoints.
            indexr=assignPoints(points,index,indexr,localMaxima);
            indexr=stampEIPoints(index,indexr,localMaxima);
        }
    }

    int stampEIPoints(int index,int indexr, ArrayList<Point> localMaxima){//11725
        ArrayList <Point> EIPoints0,borderPoints,EIPoints=new ArrayList(),pointsToAssign=new ArrayList();
        int len0=m_vcPointsEI[index].m_pointArray2.size();
        int i,j,lenr,lenb,numHigherNeighbors,num;
        Point p,center;
        int pixel=index+m_pnPixelRange[0];
        boolean ln=false,lx=false;
        ArrayList<Point> higherNeighbors=new ArrayList();
        for(i=0;i<len0;i++){
            ln=false;
            lx=false;
            EIPoints0=m_vcPointsEI[index].m_pointArray2.get(i).m_pointArray;
            if(CommonMethods.containsContent(EIPoints0, new Point(78,147))){
                i=i;
            }
            borderPoints=m_vcBorderEI[index].m_pointArray2.get(i).m_pointArray;
            lenb=borderPoints.size();
            numHigherNeighbors=0;
            higherNeighbors.clear();
            for(j=0;j<lenb;j++){
                p=borderPoints.get(j);
                if(pixels[p.y][p.x]>pixel){
                    higherNeighbors.add(p);
                    numHigherNeighbors++;
                }
            }

            if(numHigherNeighbors==0){
                lx=true;
                if (numHigherNeighbors == lenb){
                    lx=lx;
                }
            } else if (numHigherNeighbors == lenb)
                ln = true;
            lenr=EIPoints0.size();
            p=CommonMethods.closestPointToCenter(EIPoints0, w, h);
            center=new Point(p);
            if(lx){
                indexr=assignPoint(p,index,indexr,localMaxima);
            }
            while(lenr>0){//iteratively assigning the EI points, in each iteration assign only those
                //EI points bordered with assigned points. This is to ensure correct rigion assignment and
                //watershed aissignment for EI points.
                EIPoints.clear();
                pointsToAssign.clear();
                for(j=0;j<lenr;j++){
                    p=EIPoints0.get(j);
                    if(stampRegions[p.y][p.x]<0){
                        pointsToAssign.add(p);
                    }else if(stampRegions[p.y][p.x]==0){//point need to be asigned in later iterations
                        EIPoints.add(p);
                    }
                }
                num=pointsToAssign.size();
                if(num==0) {
                    if(!EIPoints.isEmpty())
                        handleUnAssignableEIPoints(EIPoints,pointsToAssign);
                    else
                        break;
                }
                indexr=assignPoints(pointsToAssign,index,indexr,localMaxima);
                EIPoints0=new ArrayList <Point>(EIPoints);
                lenr=EIPoints0.size();
            }
            if(ln){
//                stampLandscape[p.y][p.x]=localMinimum;
                int type=getLandscapeType(stampLandscape[center.y][center.x]);
                int region=-stampRegions[center.y][center.x];
                if(type==regular)
                    setLandscapeType(center.x,center.y,localMinimum,region);
//                else if (type==watershed)//11724
                else if (isWatershed(stampLandscape,center.x,center.y))
                    setLandscapeType(center.x,center.y,watershedLM,region);//11808 the landscape type should have been assigned the same way as nonEI points
                else
                    IJ.error("wrong landscape type at the point"+center.x+","+center.y+")");
            }else if(lx){

            }
        }
        return indexr;
    }

    void handleUnAssignableEIPoints(ArrayList<Point> EIPoints, ArrayList<Point> pointsToAssign){
        int i,len=EIPoints.size(),dx,dy,x,y;
        Point p,pt=new Point();
        int index;
        boolean claimed=false;
        for(i=len-1;i>=0;i--){
            p=EIPoints.get(i);
//            if(stampRegions[p.y][p.x]!=0){
                for(dy=-1;dy<=1;dy++){
                    y=p.y+dy;
                    if(y<0||y>=h) continue;
                    for(dx=-1;dx<=1;dx++){
                        if(dy==0&&dx==0) continue;
                        x=p.x+dx;
                        if(x<0||x>=w) continue;
                        pt.setLocation(x, y);
                        index=stampRegions[y][x];
                        if(index!=0)
                        {
//                            stampRegions[p.y][p.x]=index;
                            claimed=true;
                            break;
                        }
                    }
                    if(claimed) break;
                }                
//            }
            if(claimed) {
                pointsToAssign.add(p);
                EIPoints.remove(i);
            }
        }
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

    int assignPoint(Point p0,int index, int indexr, ArrayList<Point> localMaxima){//indexr region index. a region is defined by a local maximum
        //and the points within watershed boundary.
        //index: the index of sorted pixel values.
        int pixel=index+m_pnPixelRange[0];
        int x,y,dx,dy,x0,y0,indexrt,indexrt0,type;
        int pixeln=m_pnPixelRange[1];
        Point p,plm0,plm;
        boolean borderPoint;
        int peak0,peak;
        y0=p0.y;
        x0=p0.x;

        if(x0==78&&y0==147){
            x0=x0;
        }
        if(stampRegions[y0][x0]==0){//this point has not been claimed as a border point by any regions.
            setLandscapeType(x0,y0,localMaximum,indexr);
            stampRegions[y0][x0]=indexr;
            localMaxima.add(p0);
            borderPoint=false;
            indexrt0=indexr;
            peak0=pixel;
            indexr++;
        }else{
//            if(getLandscapeType(stampLandscape[y0][x0])==watershed){//11724
            if (isWatershed(stampLandscape,p0.x,p0.y)){//11724
                //11724
                boolean bLM=true;

                for(dy=-1;dy<=1;dy++)//checking all neighboring points
                {
                    y=y0+dy;
                    if(!yStampingRange.contains(y)) continue;
//                    if(y<0||y>=h) continue;
                    for(dx=-1;dx<=1;dx++)
                    {
                        if(dx==0&&dy==0) continue;//11322
                        x=x0+dx;
                        if(!xStampingRange.contains(x)) continue;
//                        if(x<0||x>=w) continue;
                        if(pixels[y][x]<=pixel) {
                            bLM=false;
                        }
                    }
                    if(!bLM) break;
                }

                if(bLM) setLandscapeType(p0.x,p0.y,watershedLM,-stampRegions[y0][x0]);
                //11724
                return indexr;
            }
            indexrt0=-stampRegions[y0][x0];
        }
        plm0=localMaxima.get(indexrt0-1);
        peak0=pixels[plm0.y][plm0.x];
        for(dy=-1;dy<=1;dy++)//checking all neighboring points
        {
            y=y0+dy;
            if(!yStampingRange.contains(y)) continue;
            for(dx=-1;dx<=1;dx++)
            {
                if(dx==0&&dy==0) continue;//11322
                x=x0+dx;
                if(!xStampingRange.contains(x)) continue;

                if(pixels[y][x]<pixeln){
                     pixeln=pixels[y][x];
                }
                indexrt=-stampRegions[y][x];
                if(indexrt==0){
                    stampRegions[y][x]=-indexrt0;//claiming this position as a border point of the region.
               }else{//point (x,y) has been claimed as an inner point or as a border point of another region.
                    if(indexrt>0){
                        if(indexrt0!=indexrt){
                            plm=localMaxima.get(indexrt-1);
                            peak=pixels[plm.y][plm.x];
                            if(peak0>peak)stampRegions[y][x]=-indexrt0;
                            setLandscapeType(x,y,watershed,-stampRegions[y][x]);
                        }
                    }else{//do nothing if indexrt <0, b/c this point is already be claimed as an inner point of other region (a local maxima).
                        type=getLandscapeType(stampLandscape[y][x]);//for testing
                    }
                }
            }
        }
        if(pixeln>pixel) {
//            stamp[y0][x0]=localMinimum;
//            setLandscapeType(x0,y0,localMinimum,indexr);
                type=getLandscapeType(stampLandscape[p0.y][p0.x]);
                if(type==regular)
                    setLandscapeType(p0.x,p0.y,localMinimum,-stampRegions[y0][x0]);
//                else if (type==watershed)
                else if (isWatershed(stampLandscape,p0.x,p0.y))//11724
                    setLandscapeType(p0.x,p0.y,watershedLM,-stampRegions[y0][x0]);
                else
                    IJ.error("wrong landscape type at the point"+p0.x+","+p0.y+")");
        }
        return indexr;
    }

    public int[][] getStamp(){
        return stampLandscape;
    }

    public static Point findLocalMaximum(int[][] pixels, int w, int h, int xo, int yo){
        Point p=new Point(),pt;
        int[] moves=LandscapeAnalyzerPixelSorting.moves;
        int pixel0=pixels[yo][xo],pixel=pixel0,nx=pixel;
        int x0=xo,y0=yo,x=0,y=0;
        int move=0,i;
        int xt=0,yt=0;
        while(true){
            nx=Integer.MIN_VALUE;
            for(i=0;i<8;i++){
                xt=x0+moves[move*2];
                yt=y0+moves[move*2+1];
                if(xt<0||xt>=w) {
                    move=CommonMethods.circularAddition(8, move, 1);
                     continue;
                }
                if(yt<0||yt>=h) {
                    move=CommonMethods.circularAddition(8, move, 1);
                    continue;
                }
                x=xt;
                y=yt;
                pixel=pixels[y][x];
                if(pixel>nx)  nx=pixel;
                if(pixel>pixel0){
                    break;
                }
                move=CommonMethods.circularAddition(8, move, 1);
            }
            //founnd a higher point or tried all 8 possible points
            if(nx<pixel0) {
                p.setLocation(x0,y0);
                break;
            }
            if(nx==pixel0) {
                p.setLocation(x0,y0);
                pt=findHigherPoint_EI(pixels,w,h,p);
                if(pt==null) return p;
                x=pt.x;
                y=pt.y;
                pixel=pixels[y][x];
            }
            pixel0=pixel;
            x0=x;
            y0=y;
        }
        return p;
    }
    public static int getRegionIndex(int region){//finding the region index for a regular point, a local maximum or an isolated local minimum (not connected with watershed.
        //region index starts from 1. {1,2,  n=numberOfRegions}
        int regionIndex=region>>3;
        return regionIndex;
    }
    
    public static boolean isIsolatedMinimum(int[][] stamp, int x0, int y0){
        int type=getLandscapeType(stamp[y0][x0]);
        if(type!=localMinimum)
            IJ.error("the point (x0,y0) has to be a local minimum for the method \"isIsolatedMinimum\"");
        int[] moves=LandscapeAnalyzerPixelSorting.moves;
        int h=stamp.length,w=stamp[0].length;
        int move=0,i,x,y;
        for(i=0;i<8;i++){
            x=x0+moves[move*2];
            y=y0+moves[move*2+1];
            if(x<0||x>=w) {
                move=CommonMethods.circularAddition(8, move, 1);
                continue;
            }
            if(y<0||y>=h) {
                move=CommonMethods.circularAddition(8, move, 1);
                continue;
            }
            if(getLandscapeType(stamp[y][x])==watershed) return false;
            move=CommonMethods.circularAddition(8, move, 1);
        }
        return true;
    }
    public static Point findHigherPoint_EI(int pixels[][], int w, int h, Point po){
        Point pt=null;
        int pixelo=pixels[po.y][po.x];
        ArrayList<Point> contour=ContourFollower.getContour_Out(pixels, w, h, po, pixelo, pixelo, true);
        ContourFollower.removeOffBoundPoints(w, h, contour);
        int len=contour.size(),i;
        for(i=0;i<len;i++){
            pt=contour.get(i);
            if(pixels[pt.y][pt.x]>pixelo) return pt;
        }
        return null;
    }
    public static boolean isWatershed(int[][] stamp, int x0, int y0){
        int type=getLandscapeType(stamp[y0][x0]);
        return(type==watershed||type==watershedLM);
    }
    public static int numNeighboringRegions(int[][] stamp, int xo, int yo){
        return getNeighboringRegions(stamp,xo,yo).size();
    }

    public static ArrayList<Integer> getNeighboringRegions(int[][] stamp, int xo, int yo){//excluding watersheds
        if(!isWatershed(stamp,xo,yo)) return new ArrayList<Integer>();
        int x, y,w=stamp[0].length,h=stamp.length,dy,dx,region;
        TreeSet<Integer> regions=new TreeSet();
        for(dy=-1;dy<=1;dy++){
            y=yo+dy;
            if(y<0||y>=h)continue;
            for(dx=-1;dx<=1;dx++){
                x=xo+dx;
                if(x<0||x>=w) continue;
                if(dx==0&&dy==0) continue;
                if(isWatershed(stamp,x,y)) continue;
                region=LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[y][x]);
                if(region>0) 
                    regions.add(region);
                else
                    IJ.error("Ilegal region index at the point ("+x+","+y+")");
            }
        }
        return CommonMethods.getArray(regions);
    }
    public static void getSpecialLandscapePoints(int stamp[][], int type, ArrayList<Point> points){//this method return all pixel points of the same landscape
                  //type as spicified by "type" in the argument list. local maxima will be sorted in ascending order of the region indexes. this method is newer than the
        //one used in CommonMethods, and will be used hereafter.
        int i,j,h=stamp.length,w=stamp[0].length;
        points.clear();
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                if(LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[i][j])==type){
                    points.add(new Point(j,i));
                }
            }
        }
        if(type==localMaximum){
            int len=points.size();
            int regionIndex;
            Point pt;
            for(i=0;i<len;i++){
                pt=points.get(i);
                regionIndex=getRegionIndex(stamp[pt.y][pt.x]);
                if(regionIndex==i+1) continue;
                do{
                    CommonMethods.swapPoints(points, i, regionIndex-1);
                    pt=points.get(i);
                    regionIndex=getRegionIndex(stamp[pt.y][pt.x]);
                }while(regionIndex!=i+1);
            }
        }
    }
    public static boolean isLocalMaximum(int type){
        return (getLandscapeType(type)==localMaximum);
    }
    public static void stampPixels(int[][] pixels, int[][]stamp){
        intRange ir=CommonStatisticsMethods.getRange(pixels);
        int[] pr={ir.getMin(),ir.getMax()};
        LandscapeAnalyzerPixelSorting ps=new LandscapeAnalyzerPixelSorting(pixels[0].length,pixels.length,pr);
        ps.updateAndStampPixels(pixels, stamp);
    }
}
