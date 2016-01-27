/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import java.awt.Point;
import utilities.CommonMethods;
import ImageAnalysis.LandscapeAnalyzer;
import ImageAnalysis.ContourFollower;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class PixelTrailHandler {//this class is meant not to keep actual pixel array and the trail point array.
    public ArrayList <Point> m_vcTrail;
    public boolean m_bContour;
    public int m_nPeakIndex;
    public double m_dAve;
    public Point m_cContourHandle;//This is the outside point that directly contacts with the peak point.
    public Point m_cPeakPoint;
    public int m_pixels[][];
    public int m_stamp[][];
    public int m_w,m_h;
    public int m_nPeakValue;
    public intRange m_xRange;
    public intRange m_yRange;
    public ArrayList <Integer> m_vnPixels;
    public PixelTrailHandler(){

    }
    public PixelTrailHandler(int pixels[][],int stamp[][], int w, int h, ArrayList <Point> trail, boolean bContour){
        m_vcTrail=trail;
        m_bContour=bContour;
        m_pixels=pixels;
        m_stamp=stamp;
        m_w=w;
        m_h=h;
        m_xRange=new intRange();
        m_yRange=new intRange();
        buildHandler();
    }
    void buildHandler(){
        int nSize=m_vcTrail.size();
        int nx,ix=0,pixel,x,y;
        Point p;
        p=m_vcTrail.get(ix);
        nx=m_pixels[p.y][p.x];
        m_dAve=0.;
        m_xRange.resetRange();
        m_vnPixels=new ArrayList();
        int i;
        for(i=0;i<nSize;i++){
            p=m_vcTrail.get(i);
            m_xRange.expandRange(p.x);
            m_yRange.expandRange(p.y);
            pixel=m_pixels[p.y][p.x];
            m_vnPixels.add(pixel);
            if(pixel>nx){
                nx=pixel;
                ix=i;
            }
            m_dAve+=pixel;
        }
        m_dAve/=nSize;
        m_nPeakIndex=ix;
        m_nPeakValue=nx;
        m_cPeakPoint=m_vcTrail.get(ix);
        int numFoldedPoints;
        if(m_bContour){
            int index=ix;
            p=ContourFollower.getLeftsidePoint(m_vcTrail, index, -1);
            numFoldedPoints=0;
            while(CommonMethods.findPoint(m_vcTrail, p).size()>0){
                numFoldedPoints++;
                index=ContourFollower.circularAddition(nSize, index, -1);
                p=ContourFollower.getLeftsidePoint(m_vcTrail, index, -1);
            }
            if(p.x<0||p.y<0||p.x>=255||p.y>=255){
                p=p;
            }
            m_cContourHandle=new Point(p);
        }
        if(!m_bContour&&m_stamp!=null) {
            ArrayList <Point> points=new ArrayList();
            CommonMethods.getSpecialLandscapePoints_nonePixelSorting(m_pixels, m_stamp,m_w, m_h, LandscapeAnalyzer.localMaximum, m_vcTrail,points);
            nSize=points.size();
            if(nSize==0){
                int indexes[]=new int[2];
                Point extremePoints[]=new Point[2];
                int nRange[]=new int[2];
                CommonMethods.getExtremeValues(m_pixels, m_vcTrail,indexes,extremePoints,nRange);
                m_cPeakPoint=extremePoints[1];
                m_nPeakIndex=indexes[1];
                m_nPeakValue=nRange[1];
            }else{
                p=points.get(0);
                x=p.x;
                y=p.y;
                double dx=m_pixels[y][x];
                ix=0;
                for(i=1;i<nSize;i++){
                    p=points.get(i);
                    x=p.x;
                    y=p.y;
                    pixel=m_pixels[y][x];
                    if(pixel>dx){
                        dx=pixel;
                        ix=i;
                    }
                }
                p=points.get(ix);
            }
        }
    }
    public void copyTrail(int[] pixels, int w, int h){
        copyTrail(pixels, w, h,  0, 0);
    }

    public void copyTrail(int[] pixels, int w, int h,  Point corner, int lx, int ly){
        int xShift=corner.x+(lx-m_xRange.getRange())/2-m_xRange.getMin();
        int yShift=corner.y+(ly-m_yRange.getRange())/2-m_yRange.getMin();
        copyTrail(pixels,w,h,xShift,yShift);
    }

    public void copyTrail(int[] pixels, int w, int h,  int xShift, int yShift){
        int len=m_vcTrail.size();
        int x,y;
        Point p;
        int r,g,b,pixel;
        for(int i=0;i<len;i++){
            p=m_vcTrail.get(i);
            pixel=m_vnPixels.get(i);
            r=pixel;
            g=pixel;
            b=pixel;
            pixel=(r<<16)|(g<<8)|(b);
            x=p.x+xShift;
            y=p.y+yShift;
            pixels[y*w+x]=pixel;
        }
    }

    public void drawPeakPoint(int[] pixels, int w, int h, int xShift, int yShift, int pixel0){
        int x,y,pixel;
        int r=0xff&(pixel0>>16),g=0xff&(pixel0>>8),b=0xff&(pixel0);
        Point p;
        p=m_cPeakPoint;
        b=m_vnPixels.get(m_nPeakIndex);
        r=b;
        pixel=(r<<16)|(g<<8)|(b);
        x=p.x+xShift;
        y=p.y+yShift;
        pixels[y*w+x]=pixel;
    }

    public void drawPeakPoint(int[] pixels, int w, int h, Point corner, int lx, int ly, int pixel0){
        int xShift=corner.x+(lx-m_xRange.getRange())/2-m_xRange.getMin();
        int yShift=corner.y+(ly-m_yRange.getRange())/2-m_yRange.getMin();
        drawPeakPoint(pixels, w, h, xShift, yShift, pixel0);
    }

    public void drawPeakPoint(int[] pixels, int w, int h, int pixel0){
        drawPeakPoint(pixels, w, h, 0, 0, pixel0);
    }

    public void drawContour(int[] pixels, int w, int h, int xShift, int yShift, int pixel0){
        int len=m_vcTrail.size();
        int x,y,pixel;
        int r=0xff&(pixel0>>16),g=0xff&(pixel0>>8),b=0xff&(pixel0);
        Point p;
        for(int i=0;i<len;i++){
            p=m_vcTrail.get(i);
            g=m_vnPixels.get(i);
            b=g;
            pixel=(r<<16)|(g<<8)|(b);
            x=p.x+xShift;
            y=p.y+yShift;
            pixels[y*w+x]=pixel;
        }
    }
    public void drawContour(int[] pixels, int w, int h, Point corner, int lx, int ly, int pixel0){
        int xShift=corner.x+(lx-m_xRange.getRange())/2-m_xRange.getMin();
        int yShift=corner.y+(ly-m_yRange.getRange())/2-m_yRange.getMin();
        drawContour(pixels, w, h, xShift, yShift, pixel0);
    }
    public void drawContour(int[] pixels, int w, int h, int pixel0){
        drawContour(pixels, w, h, 0, 0, pixel0);
    }
}
