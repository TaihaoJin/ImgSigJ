/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry.ImageShapes;
import utilities.Geometry.CocentricCircles_Rings;
import java.awt.Point;
import utilities.CustomDataTypes.intRange;
import java.util.ArrayList;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
//public class SubpixelAreaInRingsLUT extends ImageShape{
public class SubpixelAreaInRingsLUT{
    int nResolution,nSegments;//the range [-0.5,0.5] will be devided into nSegments=2*nResolution+1 segments.
    CocentricCircles_Rings m_cCCRing;
    int nX,nY,nXc,nYc,nInnerPoints,nSubPixelGrids;
    //nX, nY are the number of subPixel divisions that the center of the m_cCCRing away from the center
    //(nXc, nYc) along x and y directions, respectively. nXc and nYc are the center of the square (using
    //the left upper corner as (0,0)) enclosing the cocentric ring m_cCCRing.
    //nInnerPoints is the number of pixels in the square.
    double dX,dY,delta;//delta is the length of one subPixel division
    Point[] m_pcPoints;
    double[][][][] m_pdAreaInRings;
    intRange[][][] m_pcRingRanges;
    RectangleImage m_cEnclosingRectangle;//The enclosing rectangle of m_cSubpixelIS
    public int minDistToBorder,maxDistToBorder;

    int w,h;

    public SubpixelAreaInRingsLUT(ArrayList<Double> dvRs, int nResolution){
        update(new CocentricCircles_Rings(dvRs),nResolution);
    }
    public SubpixelAreaInRingsLUT(CocentricCircles_Rings ccring, int nResolution){
        update(ccring,nResolution);
    }
    void update(CocentricCircles_Rings ccring, int nResolution){
        m_cCCRing=ccring;
        this.nResolution=nResolution;
        int R=(int)m_cCCRing.getMaxR()+1;
        w=2*R+1;
        h=2*R+1;
        nSegments=2*nResolution+1;
        delta=0.5/nResolution;
        nInnerPoints=w*h;
        nSubPixelGrids=nSegments*nSegments;
        m_pdAreaInRings=new double[nSegments][nSegments][w*h][];
        m_pcRingRanges=new intRange[nSegments][nSegments][nInnerPoints];
        nXc=w/2;//this value is assigned only at this constructor
        nYc=h/2;//this value is assigned only at this constructor
        m_cEnclosingRectangle=new RectangleImage(w,h);
        buildAreaInRings();
    }
    public double[][][][] getAreaInRings(){
        return m_pdAreaInRings;
    }
    public void setCenter_Subpixel(double x, double y){//checked on 11217
        dX=x-(int)(x+0.5);
        dY=y-(int)(y+0.5);
        int nSign=1;
        if(dX<0) nSign=-1;
        nX=(int)(Math.abs(dX)/delta+0.5);
        if(nX>nResolution) nX=nResolution;//to make nX=nResolution for dX==0.5
        nX*=nSign;
        if(dY>=0)
            nSign=1;
        else
            nSign=-1;

        nY=(int)(Math.abs(dY)/delta+0.5);
        if(nY>nResolution) nY=nResolution;//to make nX=nResolution for dX==0.5
        nY*=nSign;
    }
    void buildAreaInRings(){
        int nNumRings=m_cCCRing.getNumRings();
        int i,j,k,x,y,nX,nY,kn,kx,o,l;
        double dx,dy;
        Point p;
        double areaInRings[];
        double dTA;
        double[] areaInRingsT=new double[nNumRings];
        double[] areaInRingsDiff=new double[nNumRings];
        for(i=-nResolution;i<=nResolution;i++){
            dy=i*delta;
            for(j=-nResolution;j<=nResolution;j++){
                dx=j*delta;
                m_cCCRing.setCenter(nXc+dx,nYc+dy);
                for(l=0;l<nNumRings;l++){
                    areaInRingsT[l]=0;
                    areaInRingsDiff[l]=Math.PI*0.2*0.2*(2*l+1);
                }
                for(y=0;y<h;y++){
                    o=y*w;
                    for(x=0;x<w;x++){
                        areaInRings=new double[nNumRings];
                        m_cCCRing.areaInRings_Squre(x-0.5,y-0.5, 1, areaInRings);
                        dTA=0;
                        for(l=0;l<nNumRings;l++){
                            dTA+=areaInRings[l];
                            areaInRingsT[l]+=areaInRings[l];
                            areaInRingsDiff[l]-=areaInRings[l];
                        }
                        m_pdAreaInRings[i+nResolution][j+nResolution][o+x]=areaInRings;
                        kn=nNumRings;
                        kx=0;
                        for(k=0;k<nNumRings;k++){
                            if(areaInRings[k]>0){
                                if(k<kn) kn=k;
                                kx=k;
                            }
                        }
                        m_pcRingRanges[i+nResolution][j+nResolution][o+x]=new intRange(kn,kx);
                    }
                }
                for(l=0;l<nNumRings;l++){
                     if(Math.abs(areaInRingsDiff[l])>0.000000005){
                         dx=dx;
                     };
                }

            }
        }
    }
    public int getResolution(){
        return nResolution;
    }
    public intRange[][][] getRingRanges(){
        return m_pcRingRanges;
    }
    public double[] getAreaInRings(int x, int y){
        Point p=innerCoordinates(x,y);
        int dx=p.x,dy=p.y;
        if(dx<0||dx>=w) return null;
        if(dy<0||dy>=h) return null;
        return m_pdAreaInRings[nY+nResolution][nX+nResolution][dy*w+dx];
    }
    public intRange getRingRanges(int x, int y){
        Point p=innerCoordinates(x,y);
        int dx=p.x,dy=p.y;
        if(dx<0||dx>=w) return null;
        if(dy<0||dy>=h) return null;
        return m_pcRingRanges[nY+nResolution][nX+nResolution][dy*w+dx];
    }
    Point innerCoordinates(int x, int y){
        Point p=new Point(x,y),p0=m_cEnclosingRectangle.getLocation();
        p.translate(-p0.x, -p0.y);
        return p;
    }
    public double getMaxCCRingRadius(){
        int n=m_cCCRing.getRs().size();
        return m_cCCRing.getRs().get(n-1);
    }
    public CocentricCircles_Rings getCCRings(){
        return m_cCCRing;
    }
    public int getNumRings(){
        return m_cCCRing.getNumRings();
    }
    public Point getCenter(){
        return new Point(w/2, h/2);
    }
    public void setEnclosingRectangleCenter(Point p){
        m_cEnclosingRectangle.setCenter(p);
    }
    public void setFrameRanges(intRange xRange, intRange yRange){
        m_cEnclosingRectangle.setFrameRanges(xRange, yRange);
    }
    public RectangleImage getEnclosingRectangle(){
        return m_cEnclosingRectangle;
    }
}
